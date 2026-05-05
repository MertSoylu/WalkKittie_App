package com.mert.paticat.domain.model

/**
 * Static level → cumulative-XP table shown on LevelInfoScreen.
 *
 * Values are derived from `Cat.xpForLevel(level)` (200 * (level-1)^2). They are
 * duplicated here intentionally so the lookup table is a domain-layer source of
 * truth and does not require recomputation at every screen render.
 *
 * Pre-grouped into the three semantic sections used by LevelInfoScreen
 * (Erken / Orta / İleri oyun). The boundaries match getLevelTitleResId tiers.
 */
object LevelData {
    /** Maximum level surfaced anywhere in the UI. Used as bounds-check. */
    const val MAX_LEVEL: Int = 50

    /** Pair<level, cumulativeXp> rows, in display order. */
    val EARLY_GAME: List<Pair<Int, Int>> = listOf(
        2 to 200,
        3 to 800,
        4 to 1800,
        5 to 3200,
    )

    val MID_GAME: List<Pair<Int, Int>> = listOf(
        10 to 16200,
        15 to 39200,
        20 to 72200,
    )

    val LATE_GAME: List<Pair<Int, Int>> = listOf(
        50 to 480200,
    )

    /** Flat list — preserved for places that don't need section grouping. */
    val REQUIREMENTS: List<Pair<Int, Int>> = EARLY_GAME + MID_GAME + LATE_GAME
}
