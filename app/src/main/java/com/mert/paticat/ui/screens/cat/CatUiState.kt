package com.mert.paticat.ui.screens.cat

import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.model.CatMood
import com.mert.paticat.domain.model.ShopItem
import com.google.android.gms.ads.rewarded.RewardedAd

/**
 * Core UI State for Cat Screen — cat status, ads, inventory, network.
 * Game state is separated into [GameUiState] to prevent recomposition of the
 * entire CatScreen when only game state changes.
 */
data class CatUiState(
    val isLoading: Boolean = true,
    val cat: Cat = Cat(),
    val showFeedAnimation: Boolean = false,
    val showPetAnimation: Boolean = false,
    val userMessage: String? = null,
    // Ad states
    val sleepAdCount: Int = 0,
    val isAdLoading: Boolean = false,
    val isNetworkAvailable: Boolean = true,
    val nativeAd: com.google.android.gms.ads.nativead.NativeAd? = null,
    val adLoadError: Boolean = false,
    val foodAdState: AdState = AdState.Idle,
    val sleepAdState: AdState = AdState.Idle,
    // Shop & Inventory
    val inventory: Map<ShopItem, Int> = emptyMap(),
    val dailyGoldAdsRemaining: Int = ShopItem.MAX_GOLD_ADS_PER_DAY,
    // Gold tutorial (shown once after update)
    val showGoldTutorial: Boolean = false,
    // Boosts
    val stepBoostExpiresAt: Long = 0L,
    val xpBoostExpiresAt: Long = 0L,
    val comboBoostExpiresAt: Long = 0L
) {
    val currentMood: CatMood
        get() = cat.mood
    
    val canFeed: Boolean
        get() = inventory.any { it.value > 0 } && cat.hunger < 95
    
    val moodEmoji: String
        get() = when (currentMood) {
            CatMood.IDLE -> "😸"
            CatMood.HAPPY -> "😻"
            CatMood.HUNGRY -> "😿"
            CatMood.SLEEPING -> "😴"
            CatMood.EXCITED -> "✨"
        }
    
    val moodTextResId: Int
        get() = when (currentMood) {
            CatMood.HAPPY -> com.mert.paticat.R.string.cat_stat_happiness_label
            CatMood.EXCITED -> com.mert.paticat.R.string.mood_excited
            CatMood.HUNGRY -> com.mert.paticat.R.string.cat_stat_hunger_label
            CatMood.SLEEPING -> com.mert.paticat.R.string.mood_sleeping
            CatMood.IDLE -> com.mert.paticat.R.string.mood_idle
        }
}

/**
 * Separated game UI state — prevents full CatScreen recomposition for game-only changes.
 */
data class GameUiState(
    val activeGame: GameType? = null,
    val miniGameState: MiniGameState = MiniGameState.IDLE,
    // Slots
    val slotResults: List<String> = listOf("🐱", "🐱", "🐱"),
    val isSpinning: Boolean = false,
    // Memory
    val memoryCards: List<MemoryCard> = emptyList(),
    val memoryFlippedIndices: List<Int> = emptyList(),
    val memoryMatchedPairs: Int = 0,
    val memoryMoves: Int = 0,
    // Reflex
    val reflexTargets: List<ReflexTarget> = emptyList(),
    val reflexScore: Int = 0,
    val reflexRound: Int = 0,
    val reflexMaxRounds: Int = 10,
    val reflexIsWaiting: Boolean = false,
    // Catch – score & lives passed back from UI when game ends
    val catchScore: Int = 0,
    val catchLives: Int = 3,
    // Result
    val lastReward: MiniGameReward? = null,
)

enum class GameType(val energyCost: Int, val minLevel: Int) {
    RPS(8, 1), SLOTS(12, 3), MEMORY(10, 5), REFLEX(10, 7), CATCH(15, 9)
}

enum class MiniGameState {
    IDLE, PRE_GAME, PLAYING, RESULT_WIN, RESULT_LOSE, RESULT_DRAW
}

// Rock Paper Scissors
enum class RockPaperScissors {
    ROCK, PAPER, SCISSORS;
    
    fun beats(other: RockPaperScissors): Boolean {
        return (this == ROCK && other == SCISSORS) ||
               (this == PAPER && other == ROCK) ||
               (this == SCISSORS && other == PAPER)
    }
}

data class MiniGameReward(val gold: Int = 0, val happy: Int = 0, val xp: Long = 0)

// Memory Game
data class MemoryCard(
    val id: Int,
    val emoji: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

// Reflex Game
data class ReflexTarget(
    val id: Int,
    val row: Int,
    val col: Int,
    val emoji: String,
    val isVisible: Boolean = true
)

sealed interface AdState {
    data object Idle : AdState
    data object Loading : AdState
    data object Error : AdState
    data class Loaded(val ad: RewardedAd) : AdState
}
