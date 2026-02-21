package com.mert.paticat.ui.screens.cat

import android.content.Context
import android.content.SharedPreferences
import android.app.Activity
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.R
import com.mert.paticat.domain.repository.CatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CatViewModel @Inject constructor(
    private val catRepository: CatRepository,
    private val adManager: com.mert.paticat.data.ads.AdManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatUiState())
    val uiState: StateFlow<CatUiState> = _uiState.asStateFlow()
    
    // Emits true when pet action succeeds (triggers heart animation), false when rate-limited
    private val _petResult = MutableStateFlow<Boolean?>(null)
    val petResult: StateFlow<Boolean?> = _petResult.asStateFlow()
    
    private val prefs: SharedPreferences = context.getSharedPreferences("paticat_game_state", Context.MODE_PRIVATE)
    private val KEY_SLEEP_AD_COUNT = "sleep_ad_count"
    
    private val _playerChoice = MutableStateFlow<RockPaperScissors?>(null)
    private val _catChoice = MutableStateFlow<RockPaperScissors?>(null)
    
    val playerChoice: StateFlow<RockPaperScissors?> = _playerChoice.asStateFlow()
    val catChoice: StateFlow<RockPaperScissors?> = _catChoice.asStateFlow()
    
    private var networkCallback: android.net.ConnectivityManager.NetworkCallback? = null

    init {
        performConsistencyCheck()
        observeCat()
        observeAds()
        startNetworkMonitoring()
    }

    private fun observeAds() {
        viewModelScope.launch {
            adManager.nativeAd.collect { ad ->
                _uiState.update { it.copy(nativeAd = ad) }
            }
        }
    }

    private fun observeCat() {
        viewModelScope.launch {
            catRepository.getCat().collect { cat ->
                _uiState.value = _uiState.value.copy(
                    cat = cat,
                    isLoading = false,
                    sleepAdCount = prefs.getInt(KEY_SLEEP_AD_COUNT, 0)
                )
            }
        }
    }
    
    private fun performConsistencyCheck() {
        viewModelScope.launch {
            val cat = catRepository.getCatOnce()
            if (cat.happiness <= 10) {
                 catRepository.updateHappiness(10) 
                 setMessage(context.getString(R.string.cat_msg_consistency_happy))
            }
        }
    }
    
    fun isCatSleeping(): Boolean = _uiState.value.cat.isSleeping

    fun petCat() {
        if (isCatSleeping()) return
        viewModelScope.launch {
            val allowed = catRepository.petCat()
            _petResult.value = allowed
            if (!allowed) {
                setMessage(context.getString(R.string.cat_pet_limit_reached))
            }
            // Reset so the same state can be re-emitted next tap
            kotlinx.coroutines.delay(50)
            _petResult.value = null
        }
    }
    
    fun getSleepRemainingTime(): String {
        val endTime = _uiState.value.cat.sleepEndTime
        val diff = endTime - System.currentTimeMillis()
        if (diff <= 0) return ""
        val minutes = (diff / 1000 / 60).toInt()
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) context.getString(R.string.time_fmt_hm, hours, mins) else context.getString(R.string.time_fmt_m, mins)
    }

    fun setAdLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(
            isAdLoading = isLoading,
            adLoadError = false // Loading started, clear error
        )
    }

    fun setAdError(hasError: Boolean) {
        _uiState.update { it.copy(adLoadError = hasError, isAdLoading = false) }
    }

    private fun startNetworkMonitoring() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Initial Check
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isConnected = caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        )
        _uiState.value = _uiState.value.copy(isNetworkAvailable = isConnected)

        // Callback
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                _uiState.update { it.copy(isNetworkAvailable = true) }
            }
            override fun onLost(network: android.net.Network) {
                _uiState.update { it.copy(isNetworkAvailable = false) }
            }
        }
        
        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
        } catch (e: Exception) {
            // Permission might be missing or other error
        }
    }

    fun feedCat() {
        if (isCatSleeping()) {
            setMessage(context.getString(R.string.cat_msg_sleeping, getSleepRemainingTime()))
            return
        }
    
        val cat = _uiState.value.cat
        if (cat.foodPoints < 10) { 
            setMessage(context.getString(R.string.cat_msg_no_food, 10))
            return
        }
        
        if (cat.hunger >= 95) {
            setMessage(context.getString(R.string.cat_msg_full))
            return
        }

        viewModelScope.launch {
            catRepository.feedCat(10) 
            setMessage(context.getString(R.string.cat_msg_yummy))
        }
    }
    
    private fun checkGameResources(energyCost: Int): Boolean {
        if (isCatSleeping()) {
            setMessage(context.getString(R.string.cat_msg_sleeping, getSleepRemainingTime()))
            return false
        }
    
        val currentCat = _uiState.value.cat
        if (currentCat.energy < energyCost) {
            setMessage(context.getString(R.string.cat_msg_too_tired, energyCost))
            return false
        }
        return true
    }

    fun startGame(type: GameType) {
        if (!checkGameResources(type.energyCost)) return
        
        when(type) {
            GameType.RPS -> {
                _uiState.value = _uiState.value.copy(activeGame = type, miniGameState = MiniGameState.PLAYING, lastReward = null)
                _playerChoice.value = null
                _catChoice.value = null
            }

            GameType.SLOTS -> {
                _uiState.value = _uiState.value.copy(
                    activeGame = type,
                    miniGameState = MiniGameState.PLAYING,
                    slotResults = listOf("🐱", "🐱", "🐱"),
                    isSpinning = false,
                    lastReward = null
                )
            }
            GameType.MEMORY -> {
                _uiState.value = _uiState.value.copy(
                    activeGame = type,
                    miniGameState = MiniGameState.PRE_GAME,
                    memoryCards = emptyList(),
                    memoryFlippedIndices = emptyList(),
                    memoryMatchedPairs = 0,
                    memoryMoves = 0,
                    lastReward = null
                )
            }
            GameType.REFLEX -> {
                _uiState.value = _uiState.value.copy(
                    activeGame = type,
                    miniGameState = MiniGameState.PRE_GAME,
                    reflexScore = 0,
                    reflexRound = 0,
                    reflexMaxRounds = 10,
                    reflexTargets = emptyList(),
                    reflexIsWaiting = false,
                    lastReward = null
                )
            }
        }
    }

    fun startMemoryGame() {
        if (_uiState.value.activeGame != GameType.MEMORY) return
        
        val emojis = listOf("🐱", "🐾", "🐟", "🧶", "🐭", "🦋")
        val cards = (emojis + emojis).mapIndexed { index, emoji ->
            MemoryCard(id = index, emoji = emoji)
        }.shuffled()
        
        _uiState.update { it.copy(
            miniGameState = MiniGameState.PLAYING,
            memoryCards = cards
        ) }
    }

    fun startReflexGame() {
        if (_uiState.value.activeGame != GameType.REFLEX) return
        
        _uiState.update { it.copy(
            miniGameState = MiniGameState.PLAYING
        ) }
        nextReflexRound()
    }

    fun playRPS(choice: RockPaperScissors) {
        if (_uiState.value.miniGameState != MiniGameState.PLAYING) return
        
        viewModelScope.launch {
            _playerChoice.value = choice
            delay(1500)
            val opponentChoice = RockPaperScissors.values()[Random.nextInt(RockPaperScissors.values().size)]
            _catChoice.value = opponentChoice
            
            processGameResult(
                win = choice.beats(opponentChoice),
                draw = choice == opponentChoice,
                lose = opponentChoice.beats(choice),
                energyCost = GameType.RPS.energyCost,
                winRewards = MiniGameReward(mp = 10, happy = 15, xp = 20),
                drawRewards = MiniGameReward(mp = 5, happy = 5, xp = 10),
                loseRewards = MiniGameReward(mp = 0, happy = 5, xp = 5)
            )
        }
    }
    

    
    private suspend fun processGameResult(
        win: Boolean, 
        draw: Boolean, 
        lose: Boolean, 
        energyCost: Int,
        winRewards: MiniGameReward,
        drawRewards: MiniGameReward,
        loseRewards: MiniGameReward
    ) {
        val reward = when {
            win -> winRewards
            lose -> loseRewards
            else -> drawRewards
        }
        
        val gameState = when {
            win -> MiniGameState.RESULT_WIN
            lose -> MiniGameState.RESULT_LOSE
            else -> MiniGameState.RESULT_DRAW
        }
        
        catRepository.updateEnergy(-energyCost)
        catRepository.updateHappiness(reward.happy)
        catRepository.addFoodPoints(reward.mp)
        catRepository.addXp(reward.xp.toInt())
        
        _uiState.value = _uiState.value.copy(miniGameState = gameState, lastReward = reward)
    }
    
    fun closeMiniGame() {
        _uiState.value = _uiState.value.copy(activeGame = null, miniGameState = MiniGameState.IDLE, lastReward = null)
    }

    // ===== SLOTS GAME =====
    private val slotEmojis = listOf("🐱", "🐾", "🐟", "🧶", "🐭", "🦋", "🥛", "😺")

    fun spinSlots() {
        if (_uiState.value.isSpinning || _uiState.value.miniGameState != MiniGameState.PLAYING) return
        
        _uiState.update { it.copy(isSpinning = true) }
        
        viewModelScope.launch {
            // Animate through random values
            repeat(8) {
                _uiState.update { it.copy(
                    slotResults = listOf(
                        slotEmojis.random(),
                        slotEmojis.random(),
                        slotEmojis.random()
                    )
                )}
                delay(150)
            }
            
            // Determine Outcome (Rigged for better control)
            val roll = Random.nextDouble()
            val finalResults = when {
                roll < 0.005 -> { // 0.5% Jackpot (Very Rare)
                    val symbol = slotEmojis.random()
                    listOf(symbol, symbol, symbol)
                }
                roll < 0.20 -> { // 19.5% Match Two
                    val symbol = slotEmojis.random()
                    val other = (slotEmojis - symbol).random()
                    // Create a list with 2 matching symbols and 1 different
                    val list = mutableListOf(symbol, symbol, other)
                    list.shuffle()
                    list
                }
                else -> { // 80% Loss (Ensure all 3 are different)
                    // Pick 3 distinct symbols
                    slotEmojis.shuffled().take(3)
                }
            }
            
            _uiState.update { it.copy(slotResults = finalResults, isSpinning = false) }
            
            delay(500)
            
            // Check matches
            val allMatch = finalResults[0] == finalResults[1] && finalResults[1] == finalResults[2]
            val twoMatch = finalResults[0] == finalResults[1] || finalResults[1] == finalResults[2] || finalResults[0] == finalResults[2]
            
            when {
                allMatch -> {
                    setMessage(context.getString(R.string.game_msg_jackpot))
                    processGameResult(true, false, false, GameType.SLOTS.energyCost,
                        MiniGameReward(mp = 30, happy = 20, xp = 40),
                        MiniGameReward(), MiniGameReward()
                    )
                }
                twoMatch -> {
                    setMessage(context.getString(R.string.game_msg_match_two))
                    processGameResult(true, false, false, GameType.SLOTS.energyCost,
                        MiniGameReward(mp = 10, happy = 10, xp = 15),
                        MiniGameReward(), MiniGameReward()
                    )
                }
                else -> {
                    setMessage(context.getString(R.string.game_msg_unlucky))
                    processGameResult(false, false, true, GameType.SLOTS.energyCost,
                        MiniGameReward(),
                        MiniGameReward(),
                        MiniGameReward(mp = 0, happy = 5, xp = 5)
                    )
                }
            }
            
            // No auto-close — user will choose Play Again or Close
        }
    }

    // ===== MEMORY GAME =====
    private var memoryCheckJob: kotlinx.coroutines.Job? = null

    fun flipMemoryCard(index: Int) {
        val state = _uiState.value
        if (state.miniGameState != MiniGameState.PLAYING) return
        
        val card = state.memoryCards[index]
        if (card.isFlipped || card.isMatched) return
        if (state.memoryFlippedIndices.size >= 2) return
        
        val newCards = state.memoryCards.toMutableList()
        newCards[index] = card.copy(isFlipped = true)
        val newFlipped = state.memoryFlippedIndices + index
        
        _uiState.update { it.copy(
            memoryCards = newCards,
            memoryFlippedIndices = newFlipped,
            memoryMoves = state.memoryMoves + 1
        )}
        
        if (newFlipped.size == 2) {
            memoryCheckJob?.cancel()
            memoryCheckJob = viewModelScope.launch {
                delay(600)
                checkMemoryMatch(newFlipped[0], newFlipped[1])
            }
        }
    }

    private suspend fun checkMemoryMatch(idx1: Int, idx2: Int) {
        val state = _uiState.value
        val cards = state.memoryCards.toMutableList()
        
        if (cards[idx1].emoji == cards[idx2].emoji) {
            // Match!
            cards[idx1] = cards[idx1].copy(isMatched = true, isFlipped = true)
            cards[idx2] = cards[idx2].copy(isMatched = true, isFlipped = true)
            val newPairs = state.memoryMatchedPairs + 1
            
            _uiState.update { it.copy(
                memoryCards = cards,
                memoryFlippedIndices = emptyList(),
                memoryMatchedPairs = newPairs
            )}
            
            // Check win (6 pairs total)
            if (newPairs >= 6) {
                delay(500)
                val moves = _uiState.value.memoryMoves
                val reward = when {
                moves <= 14 -> MiniGameReward(mp = 10, happy = 10, xp = 10) // Perfect
                moves <= 20 -> MiniGameReward(mp = 5, happy = 5, xp = 5) // Good
                else -> MiniGameReward(mp = 2, happy = 2, xp = 2) // OK
            }
                processGameResult(true, false, false, GameType.MEMORY.energyCost, reward, MiniGameReward(), MiniGameReward())
                // No auto-close — user will choose Play Again or Close
            }
        } else {
            // No match — flip back
            cards[idx1] = cards[idx1].copy(isFlipped = false)
            cards[idx2] = cards[idx2].copy(isFlipped = false)
            _uiState.update { it.copy(
                memoryCards = cards,
                memoryFlippedIndices = emptyList()
            )}
        }
    }

    // ===== REFLEX GAME =====
    private val reflexEmojis = listOf("🐾", "🐱", "🐟", "🧶", "🐭")
    private var reflexJob: kotlinx.coroutines.Job? = null

    fun nextReflexRound() {
        val state = _uiState.value
        val round = state.reflexRound + 1
        
        if (round > state.reflexMaxRounds) {
            // Game over — calculate rewards
            finishReflexGame()
            return
        }
        
        // Generate 1-3 targets at random grid positions (4x4)
        val targetCount = if (round <= 3) 1 else if (round <= 7) 2 else 3
        val usedPositions = mutableSetOf<Pair<Int, Int>>()
        val targets = (0 until targetCount).map { i ->
            var row: Int
            var col: Int
            do {
                row = Random.nextInt(0, 4)
                col = Random.nextInt(0, 4)
            } while (Pair(row, col) in usedPositions)
            usedPositions.add(Pair(row, col))
            ReflexTarget(id = round * 10 + i, row = row, col = col, emoji = reflexEmojis.random())
        }
        
        _uiState.update { it.copy(
            reflexRound = round,
            reflexTargets = targets,
            reflexIsWaiting = false
        )}
        
        // Auto-expire targets after timeout (gets shorter each round)
        reflexJob?.cancel()
        reflexJob = viewModelScope.launch {
            val timeout = (2000L - (round * 100L)).coerceAtLeast(800L)
            delay(timeout)
            // Targets missed — move to next round
            if (_uiState.value.miniGameState == MiniGameState.PLAYING && _uiState.value.reflexRound == round) {
                _uiState.update { it.copy(reflexTargets = emptyList(), reflexIsWaiting = true) }
                delay(500)
                nextReflexRound()
            }
        }
    }

    fun tapReflexTarget(targetId: Int) {
        val state = _uiState.value
        if (state.miniGameState != MiniGameState.PLAYING) return
        
        val target = state.reflexTargets.find { it.id == targetId } ?: return
        if (!target.isVisible) return
        
        val remaining = state.reflexTargets.map {
            if (it.id == targetId) it.copy(isVisible = false) else it
        }
        
        val newScore = state.reflexScore + 1
        _uiState.update { it.copy(reflexTargets = remaining, reflexScore = newScore) }
        
        // If all targets in this round are tapped, move to next
        if (remaining.none { it.isVisible }) {
            reflexJob?.cancel()
            viewModelScope.launch {
                _uiState.update { it.copy(reflexIsWaiting = true) }
                delay(400)
                nextReflexRound()
            }
        }
    }

    private fun finishReflexGame() {
        val score = _uiState.value.reflexScore
        val reward = when {
            score >= 15 -> MiniGameReward(mp = 10, happy = 10, xp = 10)
            score >= 10 -> MiniGameReward(mp = 5, happy = 5, xp = 5)
            score >= 5 -> MiniGameReward(mp = 2, happy = 2, xp = 2)
            else -> MiniGameReward(mp = 0, happy = 1, xp = 1)
        }
        
        val isWin = score >= 8
        viewModelScope.launch {
            processGameResult(isWin, false, !isWin, GameType.REFLEX.energyCost, reward, MiniGameReward(), reward)
            // No auto-close — user will choose Play Again or Close
        }
    }

    fun sleepCat() {
        if (isCatSleeping()) {
            setMessage(context.getString(R.string.cat_msg_already_sleeping))
            return
        }
    
        val cat = _uiState.value.cat
        if (cat.energy >= 40) {
            setMessage(context.getString(R.string.cat_msg_not_tired))
            return
        }

        viewModelScope.launch {
            // Energy recovery rate is 34/hr in CatRepositoryImpl
            val neededEnergy = 100 - cat.energy
            val sleepDurationHours = neededEnergy / 34.0
            val sleepDurationMillis = (sleepDurationHours * 60 * 60 * 1000).toLong()
            
            val endTime = System.currentTimeMillis() + sleepDurationMillis
            
            catRepository.updateCat(cat.copy(
                isSleeping = true,
                sleepEndTime = endTime,
                lastUpdated = System.currentTimeMillis()
            ))
            
            _uiState.value = _uiState.value.copy(sleepAdCount = 0)
            prefs.edit().putInt(KEY_SLEEP_AD_COUNT, 0).apply()
            
            // Format the message with calculated time
            val minutes = (sleepDurationMillis / 1000 / 60).toInt()
            val hours = minutes / 60
            val mins = minutes % 60
            val timeStr = if (hours > 0) context.getString(R.string.time_fmt_hm, hours, mins) else context.getString(R.string.time_fmt_m, mins)
            
            setMessage(context.getString(R.string.cat_msg_goodnight, timeStr))
        }
    }

    fun reduceSleepTime() {
        if (!isCatSleeping()) return
        
        val cat = _uiState.value.cat
        
        // Boost energy by 25
        val boostedEnergy = (cat.energy + 25).coerceAtMost(100)
        
        // Recalculate remaining sleep time based on new energy
        val neededEnergy = 100 - boostedEnergy
        val isWakingUp = neededEnergy <= 0
        
        val newEndTime = if (isWakingUp) {
            System.currentTimeMillis()
        } else {
            val remainingSleepHours = neededEnergy / 34.0
            val remainingSleepMillis = (remainingSleepHours * 60 * 60 * 1000).toLong()
            System.currentTimeMillis() + remainingSleepMillis
        }
        
        val newCount = prefs.getInt(KEY_SLEEP_AD_COUNT, 0) + 1
        prefs.edit().putInt(KEY_SLEEP_AD_COUNT, newCount).apply()
            
        viewModelScope.launch {
            catRepository.updateCat(cat.copy(
                sleepEndTime = newEndTime,
                isSleeping = !isWakingUp,
                energy = boostedEnergy,
                lastUpdated = System.currentTimeMillis()
            ))
            
            _uiState.value = _uiState.value.copy(sleepAdCount = newCount)
            
            if (isWakingUp) {
                setMessage(context.getString(R.string.cat_msg_woke_up))
            } else {
                setMessage(context.getString(R.string.cat_msg_sleep_reduced))
            }
        }
    }

    fun addFoodPointsForAd() {
        viewModelScope.launch {
            catRepository.addFoodPoints(15)
            setMessage(context.getString(R.string.cat_msg_food_points_added))
        }
    }

    fun showFoodStatus() {
        val cat = _uiState.value.cat
        if (cat.foodPoints >= 150) {
            setMessage(context.getString(R.string.cat_msg_food_max))
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }
    
    fun setMessage(msg: String) {
        _uiState.value = _uiState.value.copy(userMessage = msg)
    }

    // --- Ad Loading Logic ---

    fun loadFoodAd() {
        val currentState = _uiState.value.foodAdState
        if (currentState is AdState.Loading || currentState is AdState.Loaded) return

        _uiState.update { it.copy(foodAdState = AdState.Loading) }

        adManager.loadRewardedAd(
            adUnitId = adManager.FOOD_AD_ID,
            onAdLoaded = { ad ->
                _uiState.update { it.copy(foodAdState = AdState.Loaded(ad)) }
            },
            onAdFailed = {
                _uiState.update { it.copy(foodAdState = AdState.Error) }
            }
        )
    }

    fun showFoodAd(activity: Activity) {
        val state = _uiState.value.foodAdState
        if (state is AdState.Loaded) {
            state.ad.show(activity) { _ ->
                addFoodPointsForAd()
                // Reset state to Idle so it can be reloaded
                _uiState.update { it.copy(foodAdState = AdState.Idle) }
            }
        }
    }

    fun loadSleepAd() {
        val currentState = _uiState.value.sleepAdState
        if (currentState is AdState.Loading || currentState is AdState.Loaded) return

        _uiState.update { it.copy(sleepAdState = AdState.Loading) }

        adManager.loadRewardedAd(
            adUnitId = adManager.SLEEP_AD_ID,
            onAdLoaded = { ad ->
                _uiState.update { it.copy(sleepAdState = AdState.Loaded(ad)) }
            },
            onAdFailed = {
                _uiState.update { it.copy(sleepAdState = AdState.Error) }
            }
        )
    }

    fun showSleepAd(activity: Activity) {
        val state = _uiState.value.sleepAdState
        if (state is AdState.Loaded) {
            state.ad.show(activity) { _ ->
                reduceSleepTime()
                _uiState.update { it.copy(sleepAdState = AdState.Idle) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkCallback?.let {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(it)
        }
    }
}
