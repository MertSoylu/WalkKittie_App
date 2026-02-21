package com.mert.paticat.ui.screens.profile

import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.model.UserProfile

/**
 * UI State for Profile Screen
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val cat: Cat = Cat(),
    val userProfile: UserProfile = UserProfile(),
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val error: String? = null
) {
    val userName: String get() = userProfile.name
    val level: Int get() = cat.level
    val totalXp: Long get() = cat.xp
    val currentStreak: Int get() = userProfile.currentStreak
    val longestStreak: Int get() = userProfile.longestStreak
    val dailyStepGoal: Int get() = userProfile.dailyStepGoal
    val dailyWaterGoal: Int get() = userProfile.dailyWaterGoalMl
    val isFemale: Boolean get() = userProfile.gender == "FEMALE"
    
    // XP Progress bar calculations
    private fun xpForLevel(lvl: Int): Long = Cat.xpForLevel(lvl)
    
    val xpForCurrentLevel: Long get() = xpForLevel(level)
    val xpForNextLevel: Long get() = xpForLevel(level + 1)
    
    val xpInCurrentLevel: Long get() = (totalXp - xpForCurrentLevel).coerceAtLeast(0)
    val xpNeededForNextLevel: Long get() = (xpForNextLevel - xpForCurrentLevel).coerceAtLeast(1)
    
    val levelProgress: Float get() = if (xpNeededForNextLevel > 0) {
        (xpInCurrentLevel.toFloat() / xpNeededForNextLevel.toFloat()).coerceIn(0f, 1f)
    } else 0f
}
