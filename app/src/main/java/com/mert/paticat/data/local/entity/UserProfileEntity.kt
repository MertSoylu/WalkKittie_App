package com.mert.paticat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing user profile.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = 1L,
    val name: String = "User",
    val gender: String = "FEMALE", // "MALE" or "FEMALE"
    val weight: Float = 70f,       // Weight in kg for calorie calculation
    val dailyStepGoal: Int = 10000,
    val dailyWaterGoalMl: Int = 2000,
    val dailyCalorieGoal: Int = 2000,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalXpEarned: Long = 0
)
