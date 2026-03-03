package com.mert.paticat.ui.screens.cat

import android.content.Context
import com.mert.paticat.R
import com.mert.paticat.domain.model.InteractionType
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.InteractionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Encapsulates ALL game logic extracted from CatViewModel.
 * Manages game state independently to prevent recomposition of non-game UI.
 *
 * This is NOT a ViewModel — it's a stateful delegate owned by CatViewModel.
 * Its lifecycle is tied to the owning ViewModel.
 */
class GameDelegate(
    private val scope: CoroutineScope,
    private val catRepository: CatRepository,
    private val interactionRepository: InteractionRepository,
    private val context: Context,
    private val onMessage: (String) -> Unit
) {
    private val _gameUiState = MutableStateFlow(GameUiState())
    val gameUiState: StateFlow<GameUiState> = _gameUiState.asStateFlow()

    private val _playerChoice = MutableStateFlow<RockPaperScissors?>(null)
    private val _catChoice = MutableStateFlow<RockPaperScissors?>(null)

    val playerChoice: StateFlow<RockPaperScissors?> = _playerChoice.asStateFlow()
    val catChoice: StateFlow<RockPaperScissors?> = _catChoice.asStateFlow()

    // ===== Game entry =====

    fun startGame(type: GameType, catEnergy: Int, catLevel: Int, isSleeping: Boolean, sleepTimeStr: String) {
        if (catLevel < type.minLevel) return
        if (isSleeping) {
            onMessage(context.getString(R.string.cat_msg_sleeping, sleepTimeStr))
            return
        }
        if (catEnergy < type.energyCost) {
            onMessage(context.getString(R.string.cat_msg_too_tired, type.energyCost))
            return
        }

        when (type) {
            GameType.RPS -> {
                _gameUiState.value = _gameUiState.value.copy(
                    activeGame = type, miniGameState = MiniGameState.PLAYING, lastReward = null
                )
                _playerChoice.value = null; _catChoice.value = null
            }
            GameType.SLOTS -> _gameUiState.update {
                it.copy(activeGame = type, miniGameState = MiniGameState.PLAYING,
                    slotResults = listOf("🐱", "🐱", "🐱"), isSpinning = false, lastReward = null)
            }
            GameType.MEMORY -> _gameUiState.update {
                it.copy(activeGame = type, miniGameState = MiniGameState.PRE_GAME,
                    memoryCards = emptyList(), memoryFlippedIndices = emptyList(),
                    memoryMatchedPairs = 0, memoryMoves = 0, lastReward = null)
            }
            GameType.REFLEX -> _gameUiState.update {
                it.copy(activeGame = type, miniGameState = MiniGameState.PRE_GAME,
                    reflexScore = 0, reflexRound = 0, reflexMaxRounds = 10,
                    reflexTargets = emptyList(), reflexIsWaiting = false, lastReward = null)
            }
            GameType.CATCH -> _gameUiState.update {
                it.copy(activeGame = type, miniGameState = MiniGameState.PRE_GAME,
                    catchScore = 0, catchLives = 3, lastReward = null)
            }
        }
    }

    fun closeMiniGame() {
        _gameUiState.value = GameUiState() // reset to default
    }

    // ===== RPS =====

    fun playRPS(choice: RockPaperScissors) {
        if (_gameUiState.value.miniGameState != MiniGameState.PLAYING) return
        scope.launch {
            _playerChoice.value = choice
            delay(1500)
            val opponentChoice = RockPaperScissors.values()[Random.nextInt(RockPaperScissors.values().size)]
            _catChoice.value = opponentChoice
            processGameResult(
                win = choice.beats(opponentChoice), draw = choice == opponentChoice,
                lose = opponentChoice.beats(choice), energyCost = GameType.RPS.energyCost,
                gameType = GameType.RPS,
                winRewards = MiniGameReward(gold = 8, happy = 12, xp = 20),
                drawRewards = MiniGameReward(gold = 4, happy = 4, xp = 10),
                loseRewards = MiniGameReward(gold = 0, happy = 4, xp = 5)
            )
        }
    }

    // ===== SLOTS =====
    private val slotEmojis = listOf("🐱", "🐾", "🐟", "🧶", "🐭", "🦋", "🥛", "😺")

    fun spinSlots() {
        if (_gameUiState.value.isSpinning || _gameUiState.value.miniGameState != MiniGameState.PLAYING) return
        _gameUiState.update { it.copy(isSpinning = true) }

        scope.launch {
            repeat(8) {
                _gameUiState.update { it.copy(slotResults = listOf(slotEmojis.random(), slotEmojis.random(), slotEmojis.random())) }
                delay(150)
            }
            val roll = Random.nextDouble()
            val finalResults = when {
                roll < 0.005 -> { val s = slotEmojis.random(); listOf(s, s, s) }
                roll < 0.20 -> {
                    val s = slotEmojis.random(); val o = (slotEmojis - s).random()
                    mutableListOf(s, s, o).also { it.shuffle() }
                }
                else -> slotEmojis.shuffled().take(3)
            }
            _gameUiState.update { it.copy(slotResults = finalResults, isSpinning = false) }
            delay(500)

            val allMatch = finalResults[0] == finalResults[1] && finalResults[1] == finalResults[2]
            val twoMatch = finalResults[0] == finalResults[1] || finalResults[1] == finalResults[2] || finalResults[0] == finalResults[2]
            when {
                allMatch -> {
                    onMessage(context.getString(R.string.game_msg_jackpot))
                    processGameResult(true, false, false, GameType.SLOTS.energyCost, GameType.SLOTS,
                        MiniGameReward(gold = 24, happy = 16, xp = 40), MiniGameReward(), MiniGameReward())
                }
                twoMatch -> {
                    onMessage(context.getString(R.string.game_msg_match_two))
                    processGameResult(true, false, false, GameType.SLOTS.energyCost, GameType.SLOTS,
                        MiniGameReward(gold = 8, happy = 8, xp = 15), MiniGameReward(), MiniGameReward())
                }
                else -> {
                    onMessage(context.getString(R.string.game_msg_unlucky))
                    processGameResult(false, false, true, GameType.SLOTS.energyCost, GameType.SLOTS,
                        MiniGameReward(), MiniGameReward(), MiniGameReward(gold = 0, happy = 3, xp = 5))
                }
            }
        }
    }

    // ===== MEMORY GAME =====
    private var memoryCheckJob: Job? = null

    fun startMemoryGame() {
        if (_gameUiState.value.activeGame != GameType.MEMORY) return
        val emojis = listOf("🐱", "🐾", "🐟", "🧶", "🐭", "🦋")
        val cards = (emojis + emojis).mapIndexed { index, emoji -> MemoryCard(id = index, emoji = emoji) }.shuffled()
        _gameUiState.update { it.copy(miniGameState = MiniGameState.PLAYING, memoryCards = cards) }
    }

    fun flipMemoryCard(index: Int) {
        val state = _gameUiState.value
        if (state.miniGameState != MiniGameState.PLAYING) return
        val card = state.memoryCards[index]
        if (card.isFlipped || card.isMatched || state.memoryFlippedIndices.size >= 2) return

        val newCards = state.memoryCards.toMutableList()
        newCards[index] = card.copy(isFlipped = true)
        val newFlipped = state.memoryFlippedIndices + index
        _gameUiState.update { it.copy(memoryCards = newCards, memoryFlippedIndices = newFlipped, memoryMoves = state.memoryMoves + 1) }

        if (newFlipped.size == 2) {
            memoryCheckJob?.cancel()
            memoryCheckJob = scope.launch { delay(600); checkMemoryMatch(newFlipped[0], newFlipped[1]) }
        }
    }

    private suspend fun checkMemoryMatch(idx1: Int, idx2: Int) {
        val state = _gameUiState.value
        val cards = state.memoryCards.toMutableList()
        if (cards[idx1].emoji == cards[idx2].emoji) {
            cards[idx1] = cards[idx1].copy(isMatched = true, isFlipped = true)
            cards[idx2] = cards[idx2].copy(isMatched = true, isFlipped = true)
            val newPairs = state.memoryMatchedPairs + 1
            _gameUiState.update { it.copy(memoryCards = cards, memoryFlippedIndices = emptyList(), memoryMatchedPairs = newPairs) }
            if (newPairs >= 6) {
                delay(500)
                val moves = _gameUiState.value.memoryMoves
                val reward = when {
                    moves <= 14 -> MiniGameReward(gold = 8, happy = 8, xp = 10)
                    moves <= 20 -> MiniGameReward(gold = 4, happy = 4, xp = 5)
                    else -> MiniGameReward(gold = 2, happy = 2, xp = 2)
                }
                processGameResult(true, false, false, GameType.MEMORY.energyCost, GameType.MEMORY, reward, MiniGameReward(), MiniGameReward())
            }
        } else {
            cards[idx1] = cards[idx1].copy(isFlipped = false); cards[idx2] = cards[idx2].copy(isFlipped = false)
            _gameUiState.update { it.copy(memoryCards = cards, memoryFlippedIndices = emptyList()) }
        }
    }

    // ===== REFLEX GAME =====
    private val reflexEmojis = listOf("🐾", "🐱", "🐟", "🧶", "🐭")
    private var reflexJob: Job? = null

    fun startReflexGame() {
        if (_gameUiState.value.activeGame != GameType.REFLEX) return
        _gameUiState.update { it.copy(miniGameState = MiniGameState.PLAYING) }
        nextReflexRound()
    }

    fun nextReflexRound() {
        val state = _gameUiState.value
        val round = state.reflexRound + 1
        if (round > state.reflexMaxRounds) { finishReflexGame(); return }

        val targetCount = if (round <= 3) 1 else if (round <= 7) 2 else 3
        val usedPositions = mutableSetOf<Pair<Int, Int>>()
        val targets = (0 until targetCount).map { i ->
            var row: Int; var col: Int
            do { row = Random.nextInt(0, 4); col = Random.nextInt(0, 4) } while (Pair(row, col) in usedPositions)
            usedPositions.add(Pair(row, col))
            ReflexTarget(id = round * 10 + i, row = row, col = col, emoji = reflexEmojis.random())
        }
        _gameUiState.update { it.copy(reflexRound = round, reflexTargets = targets, reflexIsWaiting = false) }

        reflexJob?.cancel()
        reflexJob = scope.launch {
            val timeout = (2000L - (round * 100L)).coerceAtLeast(800L)
            delay(timeout)
            if (_gameUiState.value.miniGameState == MiniGameState.PLAYING && _gameUiState.value.reflexRound == round) {
                _gameUiState.update { it.copy(reflexTargets = emptyList(), reflexIsWaiting = true) }
                delay(500); nextReflexRound()
            }
        }
    }

    fun tapReflexTarget(targetId: Int) {
        val state = _gameUiState.value
        if (state.miniGameState != MiniGameState.PLAYING) return
        val target = state.reflexTargets.find { it.id == targetId } ?: return
        if (!target.isVisible) return

        val remaining = state.reflexTargets.map { if (it.id == targetId) it.copy(isVisible = false) else it }
        val newScore = state.reflexScore + 1
        _gameUiState.update { it.copy(reflexTargets = remaining, reflexScore = newScore) }

        if (remaining.none { it.isVisible }) {
            reflexJob?.cancel()
            scope.launch {
                _gameUiState.update { it.copy(reflexIsWaiting = true) }
                delay(400); nextReflexRound()
            }
        }
    }

    private fun finishReflexGame() {
        val score = _gameUiState.value.reflexScore
        val reward = when {
            score >= 15 -> MiniGameReward(gold = 8, happy = 8, xp = 10)
            score >= 10 -> MiniGameReward(gold = 4, happy = 4, xp = 5)
            score >= 5 -> MiniGameReward(gold = 2, happy = 2, xp = 2)
            else -> MiniGameReward(gold = 0, happy = 1, xp = 1)
        }
        val isWin = score >= 8
        scope.launch { processGameResult(isWin, false, !isWin, GameType.REFLEX.energyCost, GameType.REFLEX, reward, MiniGameReward(), reward) }
    }

    // ===== CATCH GAME =====

    fun startCatchGame() {
        if (_gameUiState.value.activeGame != GameType.CATCH) return
        _gameUiState.update { it.copy(miniGameState = MiniGameState.PLAYING, catchScore = 0, catchLives = 3) }
    }

    fun finishCatchGame(score: Int) {
        val reward = when {
            score >= 30 -> MiniGameReward(gold = 12, happy = 12, xp = 25)
            score >= 20 -> MiniGameReward(gold = 8,  happy = 8,  xp = 15)
            score >= 10 -> MiniGameReward(gold = 4,  happy = 5,  xp = 8)
            else        -> MiniGameReward(gold = 1,  happy = 2,  xp = 3)
        }
        val isWin = score >= 15
        scope.launch {
            processGameResult(
                win = isWin, draw = false, lose = !isWin,
                energyCost = GameType.CATCH.energyCost,
                gameType = GameType.CATCH,
                winRewards = reward, drawRewards = MiniGameReward(), loseRewards = reward
            )
        }
    }

    // ===== Shared result processing =====

    private suspend fun processGameResult(
        win: Boolean, draw: Boolean, lose: Boolean, energyCost: Int,
        gameType: GameType,
        winRewards: MiniGameReward, drawRewards: MiniGameReward, loseRewards: MiniGameReward
    ) {
        val reward = when { win -> winRewards; lose -> loseRewards; else -> drawRewards }
        val gameState = when { win -> MiniGameState.RESULT_WIN; lose -> MiniGameState.RESULT_LOSE; else -> MiniGameState.RESULT_DRAW }

        catRepository.updateEnergy(-energyCost)
        catRepository.updateHappiness(reward.happy)
        catRepository.addCoins(reward.gold)
        catRepository.addXp(reward.xp.toInt())

        val interactionType = when (gameType) {
            GameType.RPS    -> InteractionType.GAME_RPS
            GameType.SLOTS  -> InteractionType.GAME_SLOTS
            GameType.MEMORY -> InteractionType.GAME_MEMORY
            GameType.REFLEX -> InteractionType.GAME_REFLEX
            GameType.CATCH  -> InteractionType.GAME_REFLEX
        }
        val result = when { win -> "WIN"; lose -> "LOSE"; else -> "DRAW" }
        interactionRepository.logInteraction(type = interactionType, details = result)

        _gameUiState.update { it.copy(miniGameState = gameState, lastReward = reward) }
    }
}
