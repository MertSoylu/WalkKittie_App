package com.mert.paticat.ui.screens.cat

import android.content.Context
import com.mert.paticat.R
import com.mert.paticat.domain.model.EconomyConfig
import com.mert.paticat.domain.model.EconomySource
import com.mert.paticat.domain.model.GameConstants
import com.mert.paticat.domain.model.InteractionType
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.InteractionRepository
import com.mert.paticat.domain.repository.MissionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
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
    private val missionRepository: MissionRepository,
    private val context: Context,
    private val onMessage: (String) -> Unit
) {
    private val _gameUiState = MutableStateFlow(GameUiState())
    val gameUiState: StateFlow<GameUiState> = _gameUiState.asStateFlow()

    private val _playerChoice = MutableStateFlow<RockPaperScissors?>(null)
    private val _catChoice = MutableStateFlow<RockPaperScissors?>(null)

    val playerChoice: StateFlow<RockPaperScissors?> = _playerChoice.asStateFlow()
    val catChoice: StateFlow<RockPaperScissors?> = _catChoice.asStateFlow()

    // ===== Catch game state (hoisted from Composable) =====
    private val _catchGameState = MutableStateFlow(CatchGameState())
    val catchGameState: StateFlow<CatchGameState> = _catchGameState.asStateFlow()
    private var catchLoopJob: Job? = null
    private var catchSpawnTimer = 0f
    private var catchElapsed = 0f
    private var catchNextItemId = 0

    // Race-condition guard for processGameResult (G2). Mutex ensures
    // suspending mutual exclusion across coroutine boundaries.
    private val processGameResultMutex = Mutex()
    // Double-tap protection for startGame entry.
    private val startGameMutex = Mutex()

    // Daily reward cap (G5) — best effort in-memory; resets cross-day; lost on process death.
    private var todayDate: String = LocalDate.now().toString()
    private var todayXpEarned: Int = 0
    private var todayCoinEarned: Int = 0

    private fun resetDailyCountersIfNeeded() {
        val now = LocalDate.now().toString()
        if (now != todayDate) {
            todayDate = now
            todayXpEarned = 0
            todayCoinEarned = 0
        }
    }

    private fun applyDailyCap(xp: Int, coin: Int): Pair<Int, Int> {
        resetDailyCountersIfNeeded()
        val xpAllowed = (GameConstants.MAX_DAILY_GAME_XP - todayXpEarned).coerceAtLeast(0)
        val coinAllowed = (GameConstants.MAX_DAILY_GAME_COIN - todayCoinEarned).coerceAtLeast(0)
        val finalXp = xp.coerceAtMost(xpAllowed)
        val finalCoin = coin.coerceAtMost(coinAllowed)
        todayXpEarned += finalXp
        todayCoinEarned += finalCoin
        return finalXp to finalCoin
    }

    // ===== Game entry =====

    fun startGame(
        type: GameType,
        catEnergy: Int,
        catLevel: Int,
        isSleeping: Boolean,
        sleepTimeStr: String,
        ignoreEnergyLimit: Boolean = false
    ) {
        scope.launch {
            // Mutex protects against double-tap re-entry — only one startGame
            // in-flight at a time.
            startGameMutex.withLock {
                if (catLevel < type.minLevel) return@withLock
                if (isSleeping) {
                    onMessage(context.getString(R.string.cat_msg_sleeping, sleepTimeStr))
                    return@withLock
                }
                if (!ignoreEnergyLimit && catEnergy < type.energyCost) {
                    onMessage(context.getString(R.string.cat_msg_too_tired, type.energyCost))
                    return@withLock
                }

                // G6: Atomic energy debit at game start — prevents free retry on quit/back-press.
                if (!ignoreEnergyLimit) {
                    catRepository.updateEnergy(-type.energyCost)
                }

                // Standardize: ALL games begin at PRE_GAME for a consistent intro flow.
                when (type) {
                    GameType.RPS -> {
                        _gameUiState.update {
                            it.copy(activeGame = type, miniGameState = MiniGameState.PRE_GAME, lastReward = null)
                        }
                        _playerChoice.value = null; _catChoice.value = null
                    }
                    GameType.SLOTS -> _gameUiState.update {
                        it.copy(
                            activeGame = type, miniGameState = MiniGameState.PRE_GAME,
                            slotResults = listOf("🐱", "🐱", "🐱"), isSpinning = false, lastReward = null
                        )
                    }
                    GameType.MEMORY -> _gameUiState.update {
                        it.copy(
                            activeGame = type, miniGameState = MiniGameState.PRE_GAME,
                            memoryCards = emptyList(), memoryFlippedIndices = emptyList(),
                            memoryMatchedPairs = 0, memoryMoves = 0, lastReward = null
                        )
                    }
                    GameType.REFLEX -> _gameUiState.update {
                        it.copy(
                            activeGame = type, miniGameState = MiniGameState.PRE_GAME,
                            reflexScore = 0, reflexRound = 0, reflexMaxRounds = 10,
                            reflexTargets = emptyList(), reflexIsWaiting = false, lastReward = null
                        )
                    }
                    GameType.CATCH -> {
                        _gameUiState.update {
                            it.copy(
                                activeGame = type, miniGameState = MiniGameState.PRE_GAME,
                                catchScore = 0, catchLives = 3, lastReward = null
                            )
                        }
                        _catchGameState.value = CatchGameState()
                    }
                }
            }
        }
    }

    /** Promote PRE_GAME -> PLAYING for instant-start games (RPS, SLOTS). */
    fun startRpsRound() {
        if (_gameUiState.value.activeGame != GameType.RPS) return
        if (_gameUiState.value.miniGameState != MiniGameState.PRE_GAME) return
        _gameUiState.update { it.copy(miniGameState = MiniGameState.PLAYING) }
    }

    fun startSlotsRound() {
        if (_gameUiState.value.activeGame != GameType.SLOTS) return
        if (_gameUiState.value.miniGameState != MiniGameState.PRE_GAME) return
        _gameUiState.update { it.copy(miniGameState = MiniGameState.PLAYING) }
    }

    fun closeMiniGame() {
        // G4: Cancel any in-flight game jobs to prevent coroutine leaks.
        reflexJob?.cancel()
        reflexJob = null
        memoryCheckJob?.cancel()
        memoryCheckJob = null
        catchLoopJob?.cancel()
        catchLoopJob = null
        _playerChoice.value = null
        _catChoice.value = null
        _catchGameState.value = CatchGameState()
        _gameUiState.value = GameUiState() // reset to default
    }

    /** Forced cleanup hook for ViewModel.onCleared(). Safe to call multiple times. */
    fun closeMiniGameForced() = closeMiniGame()

    // ===== RPS =====

    fun playRPS(choice: RockPaperScissors) {
        // Auto-promote PRE_GAME -> PLAYING so existing UI (which treats RPS as
        // immediate-start) keeps working after the PRE_GAME standardization.
        if (_gameUiState.value.miniGameState == MiniGameState.PRE_GAME &&
            _gameUiState.value.activeGame == GameType.RPS
        ) {
            _gameUiState.update { it.copy(miniGameState = MiniGameState.PLAYING) }
        }
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
                winRewards = MiniGameReward(gold = EconomyConfig.MiniGameRewards.RPS_WIN_GOLD, happy = 12, xp = 20),
                drawRewards = MiniGameReward(gold = EconomyConfig.MiniGameRewards.RPS_DRAW_GOLD, happy = 4, xp = 10),
                loseRewards = MiniGameReward(gold = 0, happy = 4, xp = 5)
            )
        }
    }

    // ===== SLOTS =====
    private val slotEmojis = listOf("🐱", "🐾", "🐟", "🧶", "🐭", "🦋", "🥛", "😺")

    fun spinSlots() {
        // Auto-promote PRE_GAME -> PLAYING for SLOTS (back-compat with prior immediate-start flow).
        if (_gameUiState.value.miniGameState == MiniGameState.PRE_GAME &&
            _gameUiState.value.activeGame == GameType.SLOTS
        ) {
            _gameUiState.update { it.copy(miniGameState = MiniGameState.PLAYING) }
        }
        if (_gameUiState.value.isSpinning || _gameUiState.value.miniGameState != MiniGameState.PLAYING) return
        _gameUiState.update { it.copy(isSpinning = true) }

        scope.launch {
            repeat(8) {
                _gameUiState.update { it.copy(slotResults = listOf(slotEmojis.random(), slotEmojis.random(), slotEmojis.random())) }
                delay(150)
            }
            val roll = Random.nextDouble()
            val finalResults = when {
                roll < GameConstants.SLOT_JACKPOT_PROBABILITY -> { val s = slotEmojis.random(); listOf(s, s, s) }
                roll < GameConstants.SLOT_JACKPOT_PROBABILITY + GameConstants.SLOT_TWO_MATCH_PROBABILITY -> {
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
                        MiniGameReward(gold = EconomyConfig.MiniGameRewards.SLOTS_JACKPOT_GOLD, happy = 16, xp = 40), MiniGameReward(), MiniGameReward())
                }
                twoMatch -> {
                    onMessage(context.getString(R.string.game_msg_match_two))
                    processGameResult(true, false, false, GameType.SLOTS.energyCost, GameType.SLOTS,
                        MiniGameReward(gold = EconomyConfig.MiniGameRewards.SLOTS_MATCH_TWO_GOLD, happy = 8, xp = 15), MiniGameReward(), MiniGameReward())
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
                    moves <= 14 -> MiniGameReward(gold = EconomyConfig.MiniGameRewards.MEMORY_FAST_GOLD, happy = 8, xp = 10)
                    moves <= 20 -> MiniGameReward(gold = EconomyConfig.MiniGameRewards.MEMORY_MEDIUM_GOLD, happy = 4, xp = 5)
                    else -> MiniGameReward(gold = EconomyConfig.MiniGameRewards.MEMORY_SLOW_GOLD, happy = 2, xp = 2)
                }
                processGameResult(true, false, false, GameType.MEMORY.energyCost, GameType.MEMORY, reward, MiniGameReward(), MiniGameReward())
            }
        } else {
            _gameUiState.update { it.copy(memoryMismatchIndices = listOf(idx1, idx2)) }
            delay(400)
            cards[idx1] = cards[idx1].copy(isFlipped = false); cards[idx2] = cards[idx2].copy(isFlipped = false)
            _gameUiState.update { it.copy(memoryCards = cards, memoryFlippedIndices = emptyList(), memoryMismatchIndices = emptyList()) }
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
        val winReward = when {
            score >= 15 -> MiniGameReward(gold = EconomyConfig.MiniGameRewards.REFLEX_HIGH_GOLD, happy = 8, xp = 10)
            score >= 10 -> MiniGameReward(gold = EconomyConfig.MiniGameRewards.REFLEX_MEDIUM_GOLD, happy = 4, xp = 5)
            else -> MiniGameReward(gold = EconomyConfig.MiniGameRewards.REFLEX_LOW_GOLD, happy = 2, xp = 2)
        }
        val loseReward = MiniGameReward(gold = 0, happy = 2, xp = 3)
        val isWin = score >= 8
        scope.launch { processGameResult(isWin, false, !isWin, GameType.REFLEX.energyCost, GameType.REFLEX, winReward, MiniGameReward(), loseReward) }
    }

    // ===== CATCH GAME =====

    fun startCatchGame() {
        if (_gameUiState.value.activeGame != GameType.CATCH) return
        _gameUiState.update { it.copy(miniGameState = MiniGameState.PLAYING, catchScore = 0, catchLives = 3) }
        // Reset hoisted state so a fresh game starts with no leftover items.
        _catchGameState.value = CatchGameState(running = true)
        catchSpawnTimer = 0f
        catchElapsed = 0f
        catchNextItemId = 0
    }

    fun moveCatchPaddle(dx: Float, arenaWidthPx: Float, paddleWidthPx: Float) {
        if (arenaWidthPx <= 0f) return
        val deltaNorm = dx / arenaWidthPx
        // Clamp paddle position so it cannot overshoot the arena bounds.
        val halfW = (paddleWidthPx / arenaWidthPx) / 2f
        val current = _catchGameState.value.paddleX
        val next = (current + deltaNorm).coerceIn(0f + halfW, 1f - halfW)
        _catchGameState.update { it.copy(paddleX = next) }
    }

    /**
     * Advance the catch simulation by `dt` seconds. Composable's `withFrameNanos`
     * loop calls this each frame. State hoisted into delegate so config changes
     * + recompositions don't reset the game.
     */
    fun catchTick(dt: Float) {
        val state = _catchGameState.value
        if (!state.running) return
        val elapsed = state.elapsedSeconds + dt
        catchSpawnTimer -= dt

        var items = state.items
        if (catchSpawnTimer <= 0f) {
            val difficulty = (1f + elapsed / 20f).coerceAtMost(2.5f)
            val speed = (0.18f + Random.nextFloat() * 0.14f) * difficulty
            val isBomb = Random.nextFloat() < (0.25f + elapsed / 120f).coerceAtMost(0.42f)
            items = items + CatchFallingItem(
                id = catchNextItemId++,
                isBomb = isBomb,
                x = 0.06f + Random.nextFloat() * 0.88f,
                y = -0.05f,
                speed = speed,
            )
            catchSpawnTimer = (0.9f - elapsed / 60f).coerceAtLeast(0.32f)
        }

        val basketW = 0.16f
        val basketY = 0.87f
        val newItems = mutableListOf<CatchFallingItem>()
        var caughtScore = 0
        var livesLost = 0

        for (item in items) {
            val ny = item.y + item.speed * dt
            val inBasketX = item.x >= state.paddleX - basketW / 2 && item.x <= state.paddleX + basketW / 2
            val inBasketY = ny >= basketY - 0.06f && ny <= basketY + 0.12f
            when {
                inBasketX && inBasketY -> {
                    if (item.isBomb) livesLost++ else caughtScore++
                }
                ny > 1.12f -> { /* missed; no penalty */ }
                else -> newItems += item.copy(y = ny)
            }
        }

        val newScore = state.score + caughtScore
        val newLives = (state.lives - livesLost).coerceAtLeast(0)
        val running = newLives > 0 && elapsed < 30f
        _catchGameState.value = state.copy(
            items = newItems,
            score = newScore,
            lives = newLives,
            paddleX = state.paddleX,
            elapsedSeconds = elapsed,
            running = running,
            lastCatchHapticId = state.lastCatchHapticId + caughtScore + livesLost,
        )
        if (!running) finishCatchGame(newScore)
    }

    fun finishCatchGame(score: Int) {
        val winReward = when {
            score >= 30 -> MiniGameReward(gold = EconomyConfig.MiniGameRewards.CATCH_HIGH_GOLD, happy = 12, xp = 25)
            score >= 20 -> MiniGameReward(gold = EconomyConfig.MiniGameRewards.CATCH_MEDIUM_GOLD,  happy = 8,  xp = 15)
            else        -> MiniGameReward(gold = EconomyConfig.MiniGameRewards.CATCH_LOW_GOLD,  happy = 5,  xp = 8)
        }
        val loseReward = MiniGameReward(gold = 0, happy = 2, xp = 3)
        val isWin = score >= 15
        scope.launch {
            processGameResult(
                win = isWin, draw = false, lose = !isWin,
                energyCost = GameType.CATCH.energyCost,
                gameType = GameType.CATCH,
                winRewards = winReward, drawRewards = MiniGameReward(), loseRewards = loseReward
            )
        }
    }

    // ===== Shared result processing =====

    private suspend fun processGameResult(
        win: Boolean, draw: Boolean, lose: Boolean, energyCost: Int,
        gameType: GameType,
        winRewards: MiniGameReward, drawRewards: MiniGameReward, loseRewards: MiniGameReward
    ) {
        // G2: Mutex-based suspending guard — serializes reward processing across
        // coroutine boundaries. withLock ensures legitimate calls wait their turn
        // instead of being silently dropped (which would strand the player after
        // energy was already debited at startGame).
        processGameResultMutex.withLock {
            // Idempotency guard: if a prior call already transitioned to a RESULT_*
            // state for this round, this is a duplicate fire (e.g. catchTick reaching
            // !running while finishCatchGame was also queued externally). Drop safely
            // — energy already paid, reward already posted by the first call.
            val currentState = _gameUiState.value.miniGameState
            if (currentState == MiniGameState.RESULT_WIN ||
                currentState == MiniGameState.RESULT_LOSE ||
                currentState == MiniGameState.RESULT_DRAW
            ) return@withLock

            val reward = when { win -> winRewards; lose -> loseRewards; else -> drawRewards }
            val safeReward = sanitizeReward(reward)
            // G5: Apply daily anti-grind cap on coin/xp.
            val (cappedXp, cappedCoin) = applyDailyCap(safeReward.xp.toInt(), safeReward.gold)
            val finalReward = safeReward.copy(gold = cappedCoin, xp = cappedXp.toLong())
            val gameState = when { win -> MiniGameState.RESULT_WIN; lose -> MiniGameState.RESULT_LOSE; else -> MiniGameState.RESULT_DRAW }

            // Energy already debited at startGame() (G6). Only happiness + reward here.
            catRepository.updateHappiness(finalReward.happy)
            catRepository.addCoins(
                amount = finalReward.gold,
                source = EconomySource.GAME_REWARD,
                note = gameType.name
            )
            catRepository.addXp(finalReward.xp.toInt())

            val interactionType = when (gameType) {
                GameType.RPS    -> InteractionType.GAME_RPS
                GameType.SLOTS  -> InteractionType.GAME_SLOTS
                GameType.MEMORY -> InteractionType.GAME_MEMORY
                GameType.REFLEX -> InteractionType.GAME_REFLEX
                GameType.CATCH  -> InteractionType.GAME_CATCH
            }
            val result = when { win -> "WIN"; lose -> "LOSE"; else -> "DRAW" }
            runCatching {
                interactionRepository.logInteraction(type = interactionType, details = result)
                missionRepository.checkAndCompleteMissions(
                    gameCount = interactionRepository.getTodayGameCount()
                )
            }

            _gameUiState.update { it.copy(miniGameState = gameState, lastReward = finalReward) }
        }
    }

    private fun sanitizeReward(reward: MiniGameReward): MiniGameReward {
        return reward.copy(
            gold = reward.gold.coerceIn(0, 50),
            happy = reward.happy.coerceIn(0, 20),
            xp = reward.xp.coerceIn(0L, 100L)
        )
    }
}

/** Falling item for the Catch mini-game (state hoisted from Composable). */
data class CatchFallingItem(
    val id: Int,
    val isBomb: Boolean,
    val x: Float,
    val y: Float,
    val speed: Float,
)

/** Hoisted Catch-game state owned by [GameDelegate]. */
data class CatchGameState(
    val score: Int = 0,
    val lives: Int = 3,
    val paddleX: Float = 0.5f,
    val items: List<CatchFallingItem> = emptyList(),
    val elapsedSeconds: Float = 0f,
    val running: Boolean = false,
    /** Counter incremented on every catch/miss — Composable observes for haptic dispatch. */
    val lastCatchHapticId: Int = 0,
)
