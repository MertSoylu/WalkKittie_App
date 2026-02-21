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
    suspend fun feedCat(foodPoints: Int)
    suspend fun addXp(amount: Int)
    suspend fun addFoodPoints(amount: Int)
    suspend fun addCoins(amount: Int)
    suspend fun updateHappiness(delta: Int)
    suspend fun updateEnergy(delta: Int)
    suspend fun decreaseHungerOverTime()
    suspend fun markUserInteraction()
    /** Pets the cat (+2 happiness). Returns true if allowed, false if hourly limit (10/hr) reached. */
    suspend fun petCat(): Boolean
}
