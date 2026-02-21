package com.mert.paticat.domain.model

/**
 * Represents the virtual cat in the game.
 * Cat's status changes based on user's real-life health activities.
 */
data class Cat(
    val id: Long = 1L,
    val name: String = "Mochi",
    val hunger: Int = 50,       // 0-100: 0 = starving, 100 = full (Initial: 50%)
    val happiness: Int = 50,    // 0-100: 0 = sad, 100 = very happy (Initial: 50%)
    val energy: Int = 50,       // 0-100: 0 = exhausted, 100 = energetic (Initial: 50%)
    val xp: Long = 0,           // Total experience points
    val level: Int = 1,         // Current level
    val foodPoints: Int = 30,   // Points to buy food (Initial: 30 MP)
    val coins: Int = 0,         // In-game currency
    val isSleeping: Boolean = false,
    val sleepEndTime: Long = 0L,
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastInteractionTime: Long = System.currentTimeMillis()
) {
    /**
     * Calculate XP needed for next level.
     * Formula: 100 * level^1.5
     */
    val xpForNextLevel: Long
        get() = xpForLevel(level + 1)

    /**
     * Calculate progress to next level as percentage (0.0 - 1.0)
     */
    val levelProgress: Float
        get() {
            val prevLevelXp = xpForLevel(level)
            val currentLevelXp = xpForNextLevel
            val xpInCurrentLevel = xp - prevLevelXp
            val xpNeededForLevel = currentLevelXp - prevLevelXp
            return if (xpNeededForLevel > 0) (xpInCurrentLevel.toFloat() / xpNeededForLevel.toFloat()).coerceIn(0f, 1f) else 0f
        }

    /**
     * Get cat's current mood based on attributes
     */
    val mood: CatMood
        get() = when {
            isSleeping -> CatMood.SLEEPING
            hunger < 20 -> CatMood.HUNGRY
            energy < 20 -> CatMood.IDLE // If tired but not sleeping
            happiness > 80 && energy > 60 -> CatMood.EXCITED
            happiness > 50 -> CatMood.HAPPY
            else -> CatMood.IDLE
        }

    companion object {
        fun xpForLevel(level: Int): Long {
            // Updated formula: 200 * (level-1)^2
            // Level 1 -> 0 XP
            // Level 2 -> 200 XP
            // Level 3 -> 800 XP
            // Level 4 -> 1800 XP
            if (level <= 1) return 0L
            return (200.0 * (level - 1) * (level - 1)).toLong()
        }
        
        fun getLevelTitleResId(level: Int): Int {
            return when (level) {
                in 1..4 -> com.mert.paticat.R.string.level_title_kitten
                in 5..9 -> com.mert.paticat.R.string.level_title_junior
                in 10..14 -> com.mert.paticat.R.string.level_title_alley_cat
                in 15..19 -> com.mert.paticat.R.string.level_title_house_cat
                in 20..29 -> com.mert.paticat.R.string.level_title_hunter
                in 30..39 -> com.mert.paticat.R.string.level_title_chonky
                in 40..49 -> com.mert.paticat.R.string.level_title_wise
                else -> com.mert.paticat.R.string.level_title_legendary
            }
        }
    }
}

/**
 * Enum representing different cat moods/states.
 * Each mood corresponds to a different Lottie animation.
 */
enum class CatMood {
    IDLE,
    HAPPY,
    HUNGRY,
    SLEEPING,
    EXCITED
}
