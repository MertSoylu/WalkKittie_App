package com.mert.paticat.domain.repository

import com.mert.paticat.domain.model.Cat
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Cat operations.
 */
interface CatRepository {
    fun getCat(): Flow<Cat>
    suspend fun getCatOnce(): Cat
    suspend fun initializeCat()
    suspend fun updateCat(cat: Cat)
    suspend fun updateSleepState(isSleeping: Boolean, sleepEndTime: Long, energy: Int, lastUpdated: Long)
    suspend fun addXp(amount: Int)
    suspend fun addCoins(amount: Int)
    suspend fun updateHappiness(delta: Int)
    suspend fun updateEnergy(delta: Int)
    suspend fun decreaseHungerOverTime()
    suspend fun markUserInteraction()
    /** Pets the cat (+2 happiness). Returns true if allowed, false if 5-hour limit (5 pets / 10 happiness per 5h) reached. */
    suspend fun petCat(): Boolean
}
