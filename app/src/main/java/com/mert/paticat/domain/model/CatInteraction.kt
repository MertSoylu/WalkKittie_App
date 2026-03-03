package com.mert.paticat.domain.model

import java.time.LocalDate

/**
 * Types of cat interactions that are tracked.
 */
enum class InteractionType {
    FEED,
    GAME_RPS,
    GAME_SLOTS,
    GAME_MEMORY,
    GAME_REFLEX,
    SLEEP,
    PET
}

/**
 * Domain model for a single cat interaction event.
 */
data class CatInteraction(
    val id: Long = 0L,
    val date: LocalDate,
    val type: InteractionType,
    val foodItemId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null
)

/**
 * Summary of cat interactions for a date or date range.
 */
data class InteractionSummary(
    val feedCount: Int = 0,
    val petCount: Int = 0,
    val sleepCount: Int = 0,
    val gameRpsCount: Int = 0,
    val gameSlotsCount: Int = 0,
    val gameMemoryCount: Int = 0,
    val gameReflexCount: Int = 0
) {
    val totalGames: Int get() = gameRpsCount + gameSlotsCount + gameMemoryCount + gameReflexCount
    val totalInteractions: Int get() = feedCount + petCount + sleepCount + totalGames

    /**
     * Care Score (0-100): Weighted sum of interactions, normalized.
     * Feeding ×3, Games ×2, Sleep ×2, Petting ×1
     * Max daily score target: ~10 interactions → 100 points
     */
    val careScore: Int
        get() {
            val raw = (feedCount * 3) + (totalGames * 2) + (sleepCount * 2) + (petCount * 1)
            return (raw * 100 / 30).coerceIn(0, 100) // 30 weighted points = 100%
        }
}
