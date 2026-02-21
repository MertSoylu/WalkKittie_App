package com.mert.paticat.domain.repository

import com.mert.paticat.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for health-related operations.
 */
interface HealthRepository {
    fun getTodayStats(): Flow<DailyStats>
    fun getStatsForDate(date: LocalDate): Flow<DailyStats?>
    fun getWeeklyStats(): Flow<List<DailyStats>>
    fun getMonthlyStats(): Flow<List<DailyStats>>
    
    suspend fun addWater(amountMl: Int)
    suspend fun removeWater(amountMl: Int)
    suspend fun updateSteps(steps: Int)
    suspend fun updateCalories(calories: Int)
    
    suspend fun getTotalStepsThisWeek(): Int
    suspend fun getTotalStepsThisMonth(): Int
    suspend fun getAverageStepsPerDay(): Double
}
