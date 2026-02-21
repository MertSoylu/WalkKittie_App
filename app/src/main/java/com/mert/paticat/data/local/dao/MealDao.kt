package com.mert.paticat.data.local.dao

import androidx.room.*
import com.mert.paticat.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE date = :date ORDER BY timestamp DESC")
    fun getMealsForDate(date: String): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)
    
    @Query("SELECT SUM(calories) FROM meals WHERE date = :date")
    suspend fun getTotalCaloriesForDate(date: String): Int?
}
