package com.mert.paticat.ui.screens.games

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mert.paticat.R
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.theme.GameVibrantCatch
import com.mert.paticat.ui.theme.GameVibrantCatchLight
import com.mert.paticat.ui.theme.GameVibrantMemory
import com.mert.paticat.ui.theme.GameVibrantMemoryLight
import com.mert.paticat.ui.theme.GameVibrantRPS
import com.mert.paticat.ui.theme.GameVibrantRPSLight
import com.mert.paticat.ui.theme.GameVibrantReflex
import com.mert.paticat.ui.theme.GameVibrantReflexLight
import com.mert.paticat.ui.theme.GameVibrantSlots
import com.mert.paticat.ui.theme.GameVibrantSlotsLight

// UI-screen magic numbers extracted from GamesScreen.kt and split per-game files.
// Keep separate from domain.model.GameConstants (gameplay economy constants).

internal const val GAME_ROOM_LEVEL_LOCK_DISABLED = false

// Slots reel emoji-cycle frame delay (was hardcoded 80L in SpinningReel).
internal const val REEL_SPIN_FRAME_DELAY_MS = 80L

// Reflex round time-out curve: 2000ms shrinking by 100ms per round, floor 800ms.
// Extracted from ReflexGame line ~1552.
internal fun reflexDelayMs(round: Int): Long =
    (2000L - round * 100L).coerceAtLeast(800L)

// Lobby card stagger delays for softEntrance (was inline 0/60/120/180/240).
internal val GAME_CARD_STAGGER_DELAYS_MS: List<Long> = listOf(0L, 60L, 120L, 180L, 240L)

// Lobby GameCard min height (was hardcoded 178.dp at lobby line ~529).
internal val GAME_CARD_MIN_HEIGHT = 178.dp

/**
 * Static metadata for one lobby card. The dynamic state (energy, lock, level)
 * is computed in GamesScreen at render time.
 *
 * @property id          GameType discriminant — used for keying + dispatch.
 * @property emoji       Single-grapheme emoji shown in the floating circle.
 * @property titleResId  R.string title key.
 * @property descResId   R.string short description.
 * @property rewardResId R.string reward hint.
 * @property gradient    Two-stop gradient (top, light); bottom darkens automatically.
 * @property accent      Accent color used for glow + border tints.
 */
internal data class GameEntry(
    val id: GameType,
    val emoji: String,
    val titleResId: Int,
    val descResId: Int,
    val rewardResId: Int,
    val gradient: List<Color>,
    val accent: Color,
)

internal val LOBBY_GAME_LIST: List<GameEntry> = listOf(
    GameEntry(
        id = GameType.RPS,
        emoji = "✊",
        titleResId = R.string.game_rps_title,
        descResId = R.string.game_rps_desc,
        rewardResId = R.string.game_reward_rps,
        gradient = listOf(GameVibrantRPS, GameVibrantRPSLight),
        accent = GameVibrantRPS,
    ),
    GameEntry(
        id = GameType.SLOTS,
        emoji = "🎰",
        titleResId = R.string.game_slots_title,
        descResId = R.string.game_slots_desc,
        rewardResId = R.string.game_reward_slots,
        gradient = listOf(GameVibrantSlots, GameVibrantSlotsLight),
        accent = GameVibrantSlots,
    ),
    GameEntry(
        id = GameType.MEMORY,
        emoji = "🧠",
        titleResId = R.string.game_memory_title,
        descResId = R.string.game_memory_desc,
        rewardResId = R.string.game_reward_memory,
        gradient = listOf(GameVibrantMemory, GameVibrantMemoryLight),
        accent = GameVibrantMemory,
    ),
    GameEntry(
        id = GameType.REFLEX,
        emoji = "⚡",
        titleResId = R.string.game_reflex_title,
        descResId = R.string.game_reflex_desc,
        rewardResId = R.string.game_reward_reflex,
        gradient = listOf(GameVibrantReflex, GameVibrantReflexLight),
        accent = GameVibrantReflex,
    ),
    GameEntry(
        id = GameType.CATCH,
        emoji = "🧳",
        titleResId = R.string.game_catch_title,
        descResId = R.string.game_catch_desc,
        rewardResId = R.string.game_reward_catch,
        gradient = listOf(GameVibrantCatch, GameVibrantCatchLight),
        accent = GameVibrantCatch,
    ),
)
