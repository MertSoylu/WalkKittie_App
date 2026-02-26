package com.mert.paticat.ui.screens.cat

import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.model.CatMood
import com.google.android.gms.ads.rewarded.RewardedAd

/**
 * UI State for Cat Screen
 */
data class CatUiState(
    val isLoading: Boolean = true,
    val cat: Cat = Cat(),
    val showFeedAnimation: Boolean = false,
    val showPetAnimation: Boolean = false,
    val userMessage: String? = null,
    val activeGame: GameType? = null,
    val miniGameState: MiniGameState = MiniGameState.IDLE,

    // Slots Game State
    val slotResults: List<String> = listOf("🐱", "🐱", "🐱"),
    val isSpinning: Boolean = false,
    // Memory Game State
    val memoryCards: List<MemoryCard> = emptyList(),
    val memoryFlippedIndices: List<Int> = emptyList(),
    val memoryMatchedPairs: Int = 0,
    val memoryMoves: Int = 0,
    // Reflex Game State
    val reflexTargets: List<ReflexTarget> = emptyList(),
    val reflexScore: Int = 0,
    val reflexRound: Int = 0,
    val reflexMaxRounds: Int = 10,
    val reflexIsWaiting: Boolean = false,
    // Result State
    val lastReward: MiniGameReward? = null,
    val sleepAdCount: Int = 0,
    val isAdLoading: Boolean = false,
    val isNetworkAvailable: Boolean = true,
    val nativeAd: com.google.android.gms.ads.nativead.NativeAd? = null,
    val adLoadError: Boolean = false, // Reklam yüklenemedi hatası için (Native/Genel)
    // Rewarded Ads States
    val foodAdState: AdState = AdState.Idle,
    val sleepAdState: AdState = AdState.Idle
) {
    val currentMood: CatMood
        get() = cat.mood
    
    val canFeed: Boolean
        get() = cat.foodPoints >= 10 && cat.hunger < 95 // Feed cost: 10 MP, guard matches ViewModel
    
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

enum class GameType(val energyCost: Int) {
    RPS(8), SLOTS(12), MEMORY(10), REFLEX(10)
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



data class MiniGameReward(val mp: Int = 0, val happy: Int = 0, val xp: Long = 0)

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
