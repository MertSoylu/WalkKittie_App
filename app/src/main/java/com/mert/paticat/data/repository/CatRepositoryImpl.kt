package com.mert.paticat.data.repository

import android.content.Context
import com.mert.paticat.data.local.dao.CatDao
import com.mert.paticat.data.local.toDomain
import com.mert.paticat.data.local.toEntity
import com.mert.paticat.data.local.entity.CatEntity
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.repository.CatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of CatRepository.
 * Handles all cat-related data operations with balanced logic.
 */
@Singleton
class CatRepositoryImpl @Inject constructor(
    private val catDao: CatDao,
    @ApplicationContext private val context: Context
) : CatRepository {

    private val petPrefs by lazy {
        context.getSharedPreferences("paticat_pet_state", Context.MODE_PRIVATE)
    }
    
    override fun getCat(): Flow<Cat> = catDao.getCat().map { entity ->
        entity?.let {
            // Apply decay logic whenever we read the cat, but don't blocking-write on flow (avoid loop)
            // Ideally this should be done periodically, but for UI responsiveness we calculate "virtual" state or update on app resume.
            // For now, we return the entity as is, but ensure `decreaseHungerOverTime` is called by ViewModel/WorkManager.
             it.toDomain() 
        } ?: Cat()
    }
    
    override suspend fun getCatOnce(): Cat {
        val entity = catDao.getCatOnce()
        if (entity != null) {
            var currentEntity = entity
            // Check for decay update
            val currentTime = System.currentTimeMillis()
            val elapsed = currentTime - entity.lastUpdated
            
            if (elapsed > TimeUnit.MINUTES.toMillis(20)) {
                val decayedCat = applyDecayLogic(entity, currentTime)
                if (decayedCat.hunger != entity.hunger || 
                    decayedCat.energy != entity.energy || 
                    decayedCat.happiness != entity.happiness ||
                    decayedCat.isSleeping != entity.isSleeping) {
                    currentEntity = decayedCat
                }
            }

            // Consistency Check: Level up if XP exceeds threshold (handles formula changes)
            var newLevel = currentEntity.level
            var xpForNext = Cat.xpForLevel(newLevel + 1)
            while (currentEntity.xp >= xpForNext) {
                newLevel++
                xpForNext = Cat.xpForLevel(newLevel + 1)
            }

            if (newLevel != currentEntity.level || currentEntity !== entity) {
                val finalEntity = currentEntity.copy(level = newLevel)
                catDao.updateCat(finalEntity)
                return finalEntity.toDomain()
            }
            
            return entity.toDomain()
        }
        return Cat()
    }
    
    override suspend fun initializeCat() {
        val existing = catDao.getCatOnce()
        if (existing == null) {
            catDao.insertCat(CatEntity())
        }
    }
    
    override suspend fun updateCat(cat: Cat) {
        catDao.updateCat(cat.toEntity())
    }
    
    override suspend fun feedCat(foodPoints: Int) {
        val cat = catDao.getCatOnce() ?: return
        
        // Feeding Logic:
        // 10 FoodPoints = +10% Hunger (Saturation) + 5 Happiness
        // Cap at 100.
        
        val pointsToConsume = foodPoints
        if (cat.foodPoints < pointsToConsume) return 
        
        val newHunger = (cat.hunger + 10).coerceIn(0, 100) // +10% hunger per feed
        val newHappiness = (cat.happiness + 5).coerceIn(0, 100)
        val newFoodPoints = (cat.foodPoints - pointsToConsume).coerceAtLeast(0)
        
        // Also gain small XP for caring
        val newXp = cat.xp + 2
        
        catDao.updateCat(
            cat.copy(
                hunger = newHunger,
                happiness = newHappiness,
                foodPoints = newFoodPoints,
                xp = newXp,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }
    
    override suspend fun addXp(amount: Int) {
        val cat = catDao.getCatOnce() ?: return
        var newXp = cat.xp + amount
        var newLevel = cat.level
        
        // Check for level up
        var xpForNextLevel = Cat.xpForLevel(newLevel + 1)
        while (newXp >= xpForNextLevel) {
            newLevel++
            xpForNextLevel = Cat.xpForLevel(newLevel + 1)
        }
        
        catDao.updateXpAndLevel(newXp, newLevel)
    }
    
    override suspend fun addFoodPoints(amount: Int) {
        val cat = catDao.getCatOnce() ?: return
        // Cap food points at 150 to prevent hoarding (approx 3 full feeds)
        val newPoints = (cat.foodPoints + amount).coerceAtMost(150)
        catDao.updateFoodPoints(newPoints)
    }
    
    override suspend fun addCoins(amount: Int) {
        val cat = catDao.getCatOnce() ?: return
        val newCoins = (cat.coins + amount).coerceAtLeast(0)
        catDao.updateCoins(newCoins)
    }
    
    override suspend fun updateHappiness(delta: Int) {
        val cat = catDao.getCatOnce() ?: return
        val newHappiness = (cat.happiness + delta).coerceIn(0, 100)
        catDao.updateHappiness(newHappiness)
    }
    
    override suspend fun updateEnergy(delta: Int) {
        val cat = catDao.getCatOnce() ?: return
        val newEnergy = (cat.energy + delta).coerceIn(0, 100)
        catDao.updateEnergy(newEnergy)
    }
    
    /**
     * Smart Decay Logic
     * Calculates stats based on real elapsed time.
     * After cat wakes up, energy is FROZEN until user opens app.
     * Hunger still decreases slowly (-1/hr) while user is away.
     */
    override suspend fun decreaseHungerOverTime() {
        val cat = catDao.getCatOnce() ?: return
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - cat.lastUpdated
        
        // Only update if significant time passed (> 20 mins) to prevent database trashing and truncation loss
        if (elapsed > TimeUnit.MINUTES.toMillis(20)) {
            val decayedCat = applyDecayLogic(cat, currentTime)
            
            // Fix: Only update if anything actually changed
            if (decayedCat.hunger != cat.hunger || 
                decayedCat.energy != cat.energy || 
                decayedCat.happiness != cat.happiness ||
                decayedCat.isSleeping != cat.isSleeping) {
                catDao.updateCat(decayedCat)
            }
        }
    }
    
    /**
     * Called when user opens the app. Updates lastInteractionTime 
     * and triggers decay calculation from that point.
     */
    override suspend fun markUserInteraction() {
        val cat = catDao.getCatOnce() ?: return
        val currentTime = System.currentTimeMillis()
        
        // First apply any pending decay up to now (Threshold 20 mins minimizes truncation loss)
        val elapsed = currentTime - cat.lastUpdated
        if (elapsed > TimeUnit.MINUTES.toMillis(20)) {
            val updatedCat = applyDecayLogic(cat, currentTime)
            // Then mark interaction time
            catDao.updateCat(updatedCat.copy(lastInteractionTime = currentTime))
        } else {
            catDao.updateCat(cat.copy(lastInteractionTime = currentTime))
        }
    }
    
    private suspend fun applyDecayLogic(cat: CatEntity, currentTime: Long): CatEntity {
        // If no time passed, return as is
        if (currentTime <= cat.lastUpdated) return cat
        
        var tempCat = cat
        val wasSleeping = tempCat.isSleeping
        
        // --- Segment 1: Sleep Period ---
        if (wasSleeping) {
            val wakeTime = tempCat.sleepEndTime
            
            if (currentTime < wakeTime) {
                // Case 1: Still sleeping the entire duration
                tempCat = calculateSegmentDecay(tempCat, tempCat.lastUpdated, currentTime, isSleeping = true)
            } else {
                // Case 2: Woke up during this period
                // Part A: Sleep Duration (lastUpdated -> wakeTime)
                if (wakeTime > tempCat.lastUpdated) {
                    tempCat = calculateSegmentDecay(tempCat, tempCat.lastUpdated, wakeTime, isSleeping = true)
                }
                
                // Wake up event
                tempCat = tempCat.copy(
                    isSleeping = false,
                    xp = tempCat.xp + 5, // Bonus XP for full sleep
                    lastUpdated = wakeTime 
                )
                
                // Part B: Awake Duration (wakeTime -> currentTime)
                if (currentTime > wakeTime) {
                    tempCat = calculateSegmentDecay(tempCat, wakeTime, currentTime, isSleeping = false)
                }
            }
        } else {
            // --- Segment 1 (Only): Awake Period ---
            tempCat = calculateSegmentDecay(tempCat, tempCat.lastUpdated, currentTime, isSleeping = false)
        }
        
        // Final update of timestamp
        return tempCat.copy(lastUpdated = currentTime)
    }

    private fun calculateSegmentDecay(
        cat: CatEntity, 
        startTime: Long, 
        endTime: Long, 
        isSleeping: Boolean
    ): CatEntity {
        if (endTime <= startTime) return cat
        
        val durationMillis = endTime - startTime
        val hoursPassed = (durationMillis.toDouble() / TimeUnit.HOURS.toMillis(1).toDouble())
        
        // 1. Hunger Calculation
        // Rule: Sleeping -> Less decrease (-3/hr). Awake -> Normal decrease (-5/hr).
        val hungerLossPerHour = if (isSleeping) 3.0 else 5.0
        val hungerLoss = kotlin.math.round(hoursPassed * hungerLossPerHour).toInt()
        val newHunger = (cat.hunger - hungerLoss).coerceIn(0, 100)
        
        // 2. Energy Calculation
        // Rule: Sleeping -> Recover (+34/hr). 
        // Awake -> If waiting/inactive, energy change = 0.
        // User specified: "Energy won't decrease until user logs in". 
        // Since this function calculates decay over elapsed time (user away), Awake energy change is 0.
        val energyChangePerHour = if (isSleeping) 34.0 else 0.0
        val energyChange = kotlin.math.round(energyChangePerHour * hoursPassed).toInt()
        val newEnergy = (cat.energy + energyChange).coerceIn(0, 100)
        
        // 3. Happiness Calculation
        var happinessChangePerHour = 0.0
        
        if (isSleeping) {
            happinessChangePerHour = when {
                newHunger < 10 -> -10.0
                newHunger < 30 -> -5.0
                else -> 2.0 // Standard sleep happiness
            }
        } else {
            // Base decay: -6/hr (faster than before)
            happinessChangePerHour = -6.0
            
            // Good State: +2/hr (well-fed and full energy)
            if (newHunger > 80 && newEnergy > 80) {
                 happinessChangePerHour = 2.0
            } else {
                // Critical State Penalty: -4 (additional)
                if (newHunger < 20 || newEnergy < 20) {
                    happinessChangePerHour -= 4.0
                }
            }
        }
        
        val happinessChange = kotlin.math.round(hoursPassed * happinessChangePerHour).toInt()
        val newHappiness = (cat.happiness + happinessChange).coerceIn(0, 100)
        
        return cat.copy(
            hunger = newHunger,
            energy = newEnergy,
            happiness = newHappiness
        )
    }

    override suspend fun petCat(): Boolean {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val storedHour = petPrefs.getInt("pet_hour_key", -1)
        val count = if (storedHour == currentHour) petPrefs.getInt("pet_count_hour", 0) else 0

        return if (count < 10) {
            petPrefs.edit()
                .putInt("pet_hour_key", currentHour)
                .putInt("pet_count_hour", count + 1)
                .apply()
            updateHappiness(2)
            true
        } else {
            false
        }
    }

}
