package com.mert.paticat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing daily health statistics.
 */
@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val date: String,  // Format: yyyy-MM-dd
    val steps: Int = 0,
    val waterMl: Int = 0,
    val caloriesBurned: Int = 0,
    val caloriesConsumed: Int = 0, // Kullanıcının girdiği alınan kalori
    val distanceKm: Double = 0.0,
    val activeMinutes: Int = 0,
    val lastSyncTime: Long = System.currentTimeMillis()
)
