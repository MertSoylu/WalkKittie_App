package com.mert.paticat.data.repository

import com.mert.paticat.data.local.dao.DailyStatsDao
import com.mert.paticat.data.local.toDomain
import com.mert.paticat.data.local.toDbString
import com.mert.paticat.data.local.entity.DailyStatsEntity
import com.mert.paticat.domain.model.DailyStats
import com.mert.paticat.domain.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of HealthRepository.
 * Handles local storage for step and health data.
 */
@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val dailyStatsDao: DailyStatsDao
) : HealthRepository {
    
    private val today: String get() = LocalDate.now().toDbString()
    
    override fun getTodayStats(): Flow<DailyStats> {
        return dailyStatsDao.getStatsForDate(today).map { entity ->
            entity?.toDomain() ?: DailyStats(date = LocalDate.now())
        }
    }
    
    override fun getStatsForDate(date: LocalDate): Flow<DailyStats?> {
        return dailyStatsDao.getStatsForDate(date.toDbString()).map { it?.toDomain() }
    }
    
    override fun getWeeklyStats(): Flow<List<DailyStats>> {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(6)
        return dailyStatsDao.getStatsInRange(
            weekAgo.toDbString(),
            today.toDbString()
        ).map { list -> list.map { it.toDomain() } }
    }
    
    override fun getMonthlyStats(): Flow<List<DailyStats>> {
        val today = LocalDate.now()
        val startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
        return dailyStatsDao.getStatsInRange(
            startOfMonth.toDbString(),
            today.toDbString()
        ).map { list -> list.map { it.toDomain() } }
    }
    
    override suspend fun addWater(amountMl: Int) {
        val existing = dailyStatsDao.getStatsForDateOnce(today)
        if (existing != null) {
            dailyStatsDao.updateWater(today, existing.waterMl + amountMl)
        } else {
            dailyStatsDao.insertStats(
                DailyStatsEntity(date = today, waterMl = amountMl)
            )
        }
    }
    
    override suspend fun removeWater(amountMl: Int) {
        val existing = dailyStatsDao.getStatsForDateOnce(today) ?: return
        val newWater = (existing.waterMl - amountMl).coerceAtLeast(0)
        dailyStatsDao.updateWater(today, newWater)
    }
    
    override suspend fun updateSteps(steps: Int) {
        val existing = dailyStatsDao.getStatsForDateOnce(today)
        if (existing != null) {
            dailyStatsDao.updateSteps(today, steps)
        } else {
            dailyStatsDao.insertStats(
                DailyStatsEntity(date = today, steps = steps)
            )
        }
    }
    
    override suspend fun updateCalories(calories: Int) {
        val existing = dailyStatsDao.getStatsForDateOnce(today)
        if (existing != null) {
            dailyStatsDao.updateCalories(today, calories)
        } else {
            dailyStatsDao.insertStats(
                DailyStatsEntity(date = today, caloriesBurned = calories)
            )
        }
    }
    
    override suspend fun getTotalStepsThisWeek(): Int {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(6)
        return dailyStatsDao.getTotalStepsInRange(
            weekAgo.toDbString(),
            today.toDbString()
        ) ?: 0
    }
    
    override suspend fun getTotalStepsThisMonth(): Int {
        val today = LocalDate.now()
        val startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
        return dailyStatsDao.getTotalStepsInRange(
            startOfMonth.toDbString(),
            today.toDbString()
        ) ?: 0
    }
    
    override suspend fun getAverageStepsPerDay(): Double {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(6)
        return dailyStatsDao.getAverageStepsInRange(
            weekAgo.toDbString(),
            today.toDbString()
        ) ?: 0.0
    }
}
