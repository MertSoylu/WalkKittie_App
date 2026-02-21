package com.mert.paticat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mert.paticat.data.local.entity.CatEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CatEntity operations.
 */
@Dao
interface CatDao {
    
    @Query("SELECT * FROM cat_state WHERE id = 1")
    fun getCat(): Flow<CatEntity?>
    
    @Query("SELECT * FROM cat_state WHERE id = 1")
    suspend fun getCatOnce(): CatEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCat(cat: CatEntity)
    
    @Update
    suspend fun updateCat(cat: CatEntity)
    
    @Query("UPDATE cat_state SET hunger = :hunger WHERE id = 1")
    suspend fun updateHunger(hunger: Int)
    
    @Query("UPDATE cat_state SET happiness = :happiness WHERE id = 1")
    suspend fun updateHappiness(happiness: Int)
    
    @Query("UPDATE cat_state SET energy = :energy WHERE id = 1")
    suspend fun updateEnergy(energy: Int)
    
    @Query("UPDATE cat_state SET xp = :xp, level = :level WHERE id = 1")
    suspend fun updateXpAndLevel(xp: Long, level: Int)
    
    @Query("UPDATE cat_state SET foodPoints = :foodPoints WHERE id = 1")
    suspend fun updateFoodPoints(foodPoints: Int)
    
    @Query("UPDATE cat_state SET coins = :coins WHERE id = 1")
    suspend fun updateCoins(coins: Int)
}
