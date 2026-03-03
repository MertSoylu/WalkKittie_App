package com.mert.paticat.data.repository

import com.mert.paticat.data.local.dao.CatDao
import com.mert.paticat.data.local.dao.CatInteractionDao
import com.mert.paticat.data.local.dao.InventoryDao
import com.mert.paticat.data.local.entity.CatInteractionEntity
import com.mert.paticat.data.local.entity.InventoryEntity
import com.mert.paticat.data.local.toDbString
import com.mert.paticat.domain.model.InteractionType
import com.mert.paticat.domain.model.ShopItem
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ShopRepository.
 * Handles purchasing food items with gold and feeding the cat from inventory.
 */
@Singleton
class ShopRepositoryImpl @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val catDao: CatDao,
    private val catRepository: CatRepository,
    private val catInteractionDao: CatInteractionDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ShopRepository {

    /** Mutex to serialize buy/feed operations and prevent race conditions */
    private val shopMutex = Mutex()

    override fun getInventory(): Flow<Map<ShopItem, Int>> {
        return inventoryDao.getAll().map { entities ->
            val result = mutableMapOf<ShopItem, Int>()
            for (entity in entities) {
                val item = ShopItem.getById(entity.foodItemId)
                if (item != null && entity.quantity > 0) {
                    result[item] = entity.quantity
                }
            }
            result
        }
    }

    override suspend fun buyFood(item: ShopItem): Boolean = shopMutex.withLock {
        val cat = catDao.getCatOnce() ?: return false

        // Check if the cat has enough gold
        if (cat.coins < item.price) return false

        if (item.isBoost) {
            val expiry = System.currentTimeMillis() + 24 * 3600 * 1000L
            when (item.id) {
                "step_multiplier" -> userPreferencesRepository.setStepBoostExpiry(expiry)
                "xp_multiplier" -> userPreferencesRepository.setXpBoostExpiry(expiry)
                "combo_multiplier" -> userPreferencesRepository.setComboBoostExpiry(expiry)
            }
            // Deduct gold for boost
            val newCoins = cat.coins - item.price
            catDao.updateCoins(newCoins)
            return true
        }

        // Check inventory cap BEFORE deducting gold
        val existing = inventoryDao.getItem(item.id)
        val currentQty = existing?.quantity ?: 0
        if (currentQty >= ShopItem.MAX_INVENTORY_PER_ITEM) {
            return false
        }

        // Deduct gold
        val newCoins = cat.coins - item.price
        catDao.updateCoins(newCoins)

        // Add to inventory (capped)
        val newQty = (currentQty + 1).coerceAtMost(ShopItem.MAX_INVENTORY_PER_ITEM)
        inventoryDao.upsert(InventoryEntity(foodItemId = item.id, quantity = newQty))

        return true
    }

    override suspend fun feedCatWithItem(item: ShopItem): Boolean = shopMutex.withLock {
        // Check inventory
        val existing = inventoryDao.getItem(item.id) ?: return false
        if (existing.quantity <= 0) return false

        val cat = catDao.getCatOnce() ?: return false

        // Check if cat is not already full
        if (cat.hunger >= 100) return false

        // Decrease inventory
        inventoryDao.decreaseQuantity(item.id)

        // Apply food effects using targeted DAO queries to avoid overwriting concurrent changes
        val newHunger = (cat.hunger + item.hungerRestore).coerceIn(0, 100)
        val newHappiness = (cat.happiness + item.happinessBoost).coerceIn(0, 100)

        catDao.updateHunger(newHunger)
        catDao.updateHappiness(newHappiness)

        // Apply energy boost if item provides it
        if (item.energyBoost > 0) catRepository.updateEnergy(item.energyBoost)

        // Add XP for caring
        catRepository.addXp(item.xpBoost)

        // Log interaction
        catInteractionDao.insertInteraction(
            CatInteractionEntity(
                date = LocalDate.now().toDbString(),
                type = InteractionType.FEED.name,
                foodItemId = item.id,
                timestamp = System.currentTimeMillis()
            )
        )

        return true
    }
}
