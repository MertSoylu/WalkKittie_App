package com.mert.paticat.domain.model

import java.time.LocalDate

/**
 * Represents a daily mission/challenge for the user.
 */
data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val xpReward: Int,
    val foodPointReward: Int = 0,
    val coinReward: Int = 0,
    val type: MissionType,
    val isCompleted: Boolean = false,
    val date: LocalDate = LocalDate.now()
) {
    val progress: Float
        get() = (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
}

/**
 * Types of missions available in the app.
 */
enum class MissionType {
    STEPS,
    WATER,
    CALORIES,
    STREAK
}

/**
 * Represents user's health statistics for a specific day.
 */
data class DailyStats(
    val date: LocalDate,
    val steps: Int = 0,
    val waterMl: Int = 0,
    val caloriesBurned: Int = 0,
    val caloriesConsumed: Int = 0,
    val activeMinutes: Int = 0
) {
    /**
     * Calculate estimated distance in kilometers.
     * Average step length: 0.75 meters
     */
    val distanceKm: Double
        get() = (steps * 0.75) / 1000.0

    /**
     * Calculate estimated calories burned from steps.
     * Rough estimation: 0.04 calories per step
     */
    val estimatedCaloriesFromSteps: Int
        get() = (steps * 0.04).toInt()
        
    /**
     * Net calorie balance (Consumed - Burned)
     */
    val calorieBalance: Int
        get() = caloriesConsumed - caloriesBurned
}

/**
 * User profile containing settings and statistics.
 */
data class UserProfile(
    val id: Long = 1L,
    val name: String = "User",
    val gender: String = "FEMALE",
    val dailyStepGoal: Int = 10000,
    val dailyWaterGoalMl: Int = 2000,
    val dailyCalorieGoal: Int = 2000,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalXpEarned: Long = 0
)

/**
 * Reward types that can be earned.
 */
sealed class Reward {
    data class XP(val amount: Int) : Reward()
    data class FoodPoints(val amount: Int) : Reward()
    data class Coins(val amount: Int) : Reward()
    data class Chest(val type: ChestType) : Reward()
}

enum class ChestType {
    BRONZE,
    SILVER,
    GOLD
}
