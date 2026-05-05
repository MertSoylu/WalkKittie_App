package com.mert.paticat.domain.mission

import androidx.annotation.StringRes
import com.mert.paticat.R

/**
 * Pure (non-Composable) mapping from mission string keys (as stored on
 * [com.mert.paticat.domain.model.Mission.title] and `description`) to Android
 * string resources.
 *
 * Replaces runtime `Resources.getIdentifier(...)` reflection that previously ran
 * on every recomposition of the mission row. Keep this map in sync with the
 * mission keys produced in `MissionRepositoryImpl.generateDailyMissions()` and
 * the legacy keys still referenced from older missions (tier1k / tier5k / tier10k).
 *
 * Returns 0 (a non-existent resource id) for unknown keys so callers can fall
 * back to the raw key without crashing.
 */
@StringRes
fun missionStringRes(key: String): Int = when (key) {
    // Tiered step missions (current generator output)
    "mission_steps_tier1_title" -> R.string.mission_steps_tier1_title
    "mission_steps_tier1_desc" -> R.string.mission_steps_tier1_desc
    "mission_steps_tier2_title" -> R.string.mission_steps_tier2_title
    "mission_steps_tier2_desc" -> R.string.mission_steps_tier2_desc
    "mission_steps_tier3_title" -> R.string.mission_steps_tier3_title
    "mission_steps_tier3_desc" -> R.string.mission_steps_tier3_desc
    "mission_steps_tier4_title" -> R.string.mission_steps_tier4_title
    "mission_steps_tier4_desc" -> R.string.mission_steps_tier4_desc

    // Tiered water missions (kept for missions persisted before tiered model)
    "mission_water_tier1_title" -> R.string.mission_water_tier1_title
    "mission_water_tier1_desc" -> R.string.mission_water_tier1_desc
    "mission_water_tier2_title" -> R.string.mission_water_tier2_title
    "mission_water_tier2_desc" -> R.string.mission_water_tier2_desc

    // Game missions
    "mission_game_tier1_title" -> R.string.mission_game_tier1_title
    "mission_game_tier1_desc" -> R.string.mission_game_tier1_desc

    else -> 0
}
