package com.mert.paticat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mert.paticat.data.local.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for DailyStatsEntity operations.
 */
@Dao
interface DailyStatsDao {
    
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun getStatsForDate(date: String): Flow<DailyStatsEntity?>
    
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatsForDateOnce(date: String): DailyStatsEntity?
    
    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :limit")
    fun getRecentStats(limit: Int = 7): Flow<List<DailyStatsEntity>>
    
    @Query("SELECT * FROM daily_stats WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getStatsInRange(startDate: String, endDate: String): Flow<List<DailyStatsEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStats(stats: DailyStatsEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: DailyStatsEntity)
    
    @Update
    suspend fun updateStats(stats: DailyStatsEntity)
    
    @Query("UPDATE daily_stats SET steps = :steps WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int)
    
    @Query("UPDATE daily_stats SET waterMl = :waterMl WHERE date = :date")
    suspend fun updateWater(date: String, waterMl: Int)
    
    @Query("UPDATE daily_stats SET caloriesBurned = :calories WHERE date = :date")
    suspend fun updateCalories(date: String, calories: Int)

    @Query("UPDATE daily_stats SET caloriesConsumed = :calories WHERE date = :date")
    suspend fun updateConsumedCalories(date: String, calories: Int)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getDailyStatsOnce(date: String): DailyStatsEntity?

    @Query("SELECT SUM(steps) FROM daily_stats WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalStepsInRange(startDate: String, endDate: String): Int?
    
    @Query("SELECT AVG(steps) FROM daily_stats WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageStepsInRange(startDate: String, endDate: String): Double?
}
