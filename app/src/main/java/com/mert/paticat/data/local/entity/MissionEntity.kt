package com.mert.paticat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing user's missions.
 */
@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val xpReward: Int,
    val foodPointReward: Int = 0,
    val coinReward: Int = 0,
    val type: String,  // MissionType as string
    val isCompleted: Boolean = false,
    val date: String  // Format: yyyy-MM-dd
)
