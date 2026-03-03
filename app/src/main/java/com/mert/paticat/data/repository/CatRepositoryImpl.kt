package com.mert.paticat.data.repository

import android.content.Context
import com.mert.paticat.data.local.dao.CatDao
import com.mert.paticat.data.local.dao.CatInteractionDao
import com.mert.paticat.data.local.toDomain
import com.mert.paticat.data.local.toDbString
import com.mert.paticat.data.local.toEntity
import com.mert.paticat.data.local.entity.CatEntity
import com.mert.paticat.data.local.entity.CatInteractionEntity
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.model.InteractionType
import com.mert.paticat.domain.model.ShopItem
import com.mert.paticat.domain.repository.CatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
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
    private val catInteractionDao: CatInteractionDao,
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

    override suspend fun updateSleepState(isSleeping: Boolean, sleepEndTime: Long, energy: Int, lastUpdated: Long) {
        catDao.updateSleepState(isSleeping, sleepEndTime, energy, lastUpdated)
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
    
    override suspend fun addCoins(amount: Int) {
        val cat = catDao.getCatOnce() ?: return
        val newCoins = (cat.coins + amount).coerceIn(0, ShopItem.MAX_GOLD)
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
                    sleepEndTime = 0L, // Reset so workers don't re-trigger wake notification
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
        // Rule: Sleeping -> Less decrease (-5/hr). Awake -> Normal decrease (-7/hr).
        val hungerLossPerHour = if (isSleeping) 5.0 else 7.0
        val hungerLoss = kotlin.math.round(hoursPassed * hungerLossPerHour).toInt()
        val newHunger = (cat.hunger - hungerLoss).coerceIn(0, 100)
        
        // 2. Energy Calculation
        // Rule: Sleeping -> Recover (+34/hr). 
        // Awake -> Passive drain (-2/hr).
        val energyChangePerHour = if (isSleeping) 34.0 else -2.0
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
        val now = System.currentTimeMillis()
        val windowStart = petPrefs.getLong("pet_window_start", 0L)
        val fiveHoursMs = 5 * 60 * 60 * 1000L
        val count = if (now - windowStart < fiveHoursMs) petPrefs.getInt("pet_count_window", 0) else 0

        return if (count < 5) {
            petPrefs.edit()
                .putLong("pet_window_start", if (count == 0) now else windowStart)
                .putInt("pet_count_window", count + 1)
                .apply()
            updateHappiness(2)
            // Log pet interaction
            catInteractionDao.insertInteraction(
                CatInteractionEntity(
                    date = LocalDate.now().toDbString(),
                    type = InteractionType.PET.name,
                    timestamp = System.currentTimeMillis()
                )
            )
            true
        } else {
            false
        }
    }

}
