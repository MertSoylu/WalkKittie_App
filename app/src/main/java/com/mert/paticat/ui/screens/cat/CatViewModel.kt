package com.mert.paticat.ui.screens.cat

import android.content.Context
import android.content.SharedPreferences
import android.app.Activity
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.R
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.InteractionRepository
import com.mert.paticat.domain.repository.MissionRepository
import com.mert.paticat.domain.repository.ShopRepository
import com.mert.paticat.domain.model.EconomyConfig
import com.mert.paticat.domain.model.EconomySource
import com.mert.paticat.domain.model.InteractionType
import com.mert.paticat.domain.model.ShopItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Cat screen.
 *
 * Responsibilities: cat state observation, feeding, sleeping, ads, inventory.
 * Game logic is delegated to [GameDelegate].
 */
@HiltViewModel
class CatViewModel @Inject constructor(
    private val catRepository: CatRepository,
    private val shopRepository: ShopRepository,
    private val interactionRepository: InteractionRepository,
    private val missionRepository: MissionRepository,
    private val adManager: com.mert.paticat.data.ads.AdManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ===== Core State =====
    private val _uiState = MutableStateFlow(CatUiState())
    val uiState: StateFlow<CatUiState> = _uiState.asStateFlow()

    // ===== Game Delegate =====
    private val gameDelegate = GameDelegate(
        scope = viewModelScope,
        catRepository = catRepository,
        interactionRepository = interactionRepository,
        missionRepository = missionRepository,
        context = context,
        onMessage = ::setMessage
    )
    val gameUiState: StateFlow<GameUiState> = gameDelegate.gameUiState
    val playerChoice: StateFlow<RockPaperScissors?> = gameDelegate.playerChoice
    val catChoice: StateFlow<RockPaperScissors?> = gameDelegate.catChoice

    // ===== Pet animation =====
    // Monotonic ID drives one-shot animation. Composables observe id changes
    // (NOT a null-reset). Keeps `petResult: StateFlow<Boolean?>` API for back-compat.
    private var petResultIdCounter = 0L
    private val _petResult = MutableStateFlow<Boolean?>(null)
    val petResult: StateFlow<Boolean?> = _petResult.asStateFlow()

    private val _petResultEvent = MutableStateFlow<PetResultEvent?>(null)
    val petResultEvent: StateFlow<PetResultEvent?> = _petResultEvent.asStateFlow()

    data class PetResultEvent(val id: Long, val allowed: Boolean)

    // ===== Feeding lock (rapid double-tap protection) =====
    // Single source of truth: uiState.isFeedingInProgress. Removed redundant
    // _isFeedingInProgress flow (was prone to desync on exception).

    // ===== Boost timer flow =====
    // Single VM-scoped flow emits remaining boost time every 1s. Replaces per-Composable
    // while-loop polling in ActiveBoostSummary / BoostItemCard.
    val boostTimeRemaining: StateFlow<List<BoostRemaining>> =
        flow {
            while (currentCoroutineContext().isActive) {
                emit(computeBoostRemaining())
                delay(1000L)
            }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private fun computeBoostRemaining(): List<BoostRemaining> {
        val now = System.currentTimeMillis()
        val state = _uiState.value
        return listOf(
            BoostRemaining(BoostKind.STEP, state.stepBoostExpiresAt, "👟"),
            BoostRemaining(BoostKind.XP, state.xpBoostExpiresAt, "🌟"),
            BoostRemaining(BoostKind.COMBO, state.comboBoostExpiresAt, "💎"),
        ).filter { it.expiresAt > now }
    }

    enum class BoostKind { STEP, XP, COMBO }
    data class BoostRemaining(val kind: BoostKind, val expiresAt: Long, val emoji: String) {
        fun remainingMs(now: Long = System.currentTimeMillis()): Long =
            (expiresAt - now).coerceAtLeast(0L)
    }

    // ===== SharedPreferences for ad tracking =====
    // TODO: Make sleepAdCount single-source-of-truth. Currently uiState.sleepAdCount
    // and prefs[KEY_SLEEP_AD_COUNT] are written in lock-step (sleepCat / reduceSleepTime).
    // Migration: expose a Flow<Int> from a SleepAdRepository (or DataStore) and observe
    // it into uiState in observeCat(). Skipped — touches sleep flow + repository surface,
    // bigger than a polish edit.
    private val prefs: SharedPreferences = context.getSharedPreferences("paticat_game_state", Context.MODE_PRIVATE)
    private val KEY_SLEEP_AD_COUNT = "sleep_ad_count"
    private val KEY_GOLD_AD_COUNT = "gold_ad_count"
    private val KEY_GOLD_AD_DATE = "gold_ad_date"
    private val KEY_GOLD_TUTORIAL = "gold_tutorial_shown_v1"

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        checkGoldTutorial()
        performConsistencyCheck()
        observeCat()
        observeAds()
        observeInventory()
        observeStepBoost()
        startNetworkMonitoring()
        refreshDailyAdCount()
    }

    private fun checkGoldTutorial() {
        if (prefs.getBoolean(KEY_GOLD_TUTORIAL, false)) return
        // Only show after the per-screen Cat tutorial is done (avoids overlap for new users).
        // For existing users upgrading, the cat tutorial is already marked done by MainPagerScreen.
        val catTutorialDone = prefs.getBoolean("tutorial_completed_v5_cat", false)
        if (!catTutorialDone) return
        viewModelScope.launch {
            catRepository.addCoins(
                amount = 50,
                source = EconomySource.TUTORIAL_REWARD,
                note = "gold_tutorial"
            )
            _uiState.update { it.copy(showGoldTutorial = true) }
        }
    }

    fun dismissGoldTutorial() {
        prefs.edit().putBoolean(KEY_GOLD_TUTORIAL, true).apply()
        _uiState.update { it.copy(showGoldTutorial = false) }
    }

    // ===== Initialization =====

    /**
     * Single source of truth for the day-rollover prefs read.
     * Returns today's gold-ad usage count and resets the counter on a new day.
     * Both [refreshDailyAdCount] and [getTodayGoldAdCount] route through this.
     */
    private fun readAndRolloverGoldAdCount(): Int {
        val today = java.time.LocalDate.now().toString()
        val storedDate = prefs.getString(KEY_GOLD_AD_DATE, "") ?: ""
        return if (storedDate != today) {
            prefs.edit().putInt(KEY_GOLD_AD_COUNT, 0).putString(KEY_GOLD_AD_DATE, today).apply()
            0
        } else {
            prefs.getInt(KEY_GOLD_AD_COUNT, 0)
        }
    }

    private fun refreshDailyAdCount() {
        val usedToday = readAndRolloverGoldAdCount()
        _uiState.update {
            it.copy(dailyGoldAdsRemaining = (EconomyConfig.DAILY_GOLD_AD_LIMIT - usedToday).coerceAtLeast(0))
        }
    }

    private fun getTodayGoldAdCount(): Int = readAndRolloverGoldAdCount()

    private fun canClaimDailyGoldAd(): Boolean {
        return getTodayGoldAdCount() < EconomyConfig.DAILY_GOLD_AD_LIMIT
    }

    private fun canUseSleepAd(): Boolean {
        val used = prefs.getInt(KEY_SLEEP_AD_COUNT, 0)
        return isCatSleeping() && used < EconomyConfig.MAX_SLEEP_ADS_PER_SLEEP
    }

    private fun observeAds() {
        viewModelScope.launch {
            adManager.nativeAd.collect { ad -> _uiState.update { it.copy(nativeAd = ad) } }
        }
    }

    private fun observeInventory() {
        viewModelScope.launch {
            shopRepository.getInventory().collect { inv -> _uiState.update { it.copy(inventory = inv) } }
        }
    }

    private fun observeStepBoost() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.stepBoostExpiresAt,
                userPreferencesRepository.xpBoostExpiresAt,
                userPreferencesRepository.comboBoostExpiresAt
            ) { step, xp, combo -> Triple(step, xp, combo) }
                .collect { (step, xp, combo) ->
                    _uiState.update {
                        it.copy(
                            stepBoostExpiresAt = step,
                            xpBoostExpiresAt = xp,
                            comboBoostExpiresAt = combo
                        )
                    }
                }
        }
    }

    private fun observeCat() {
        viewModelScope.launch {
            catRepository.getCat().collect { cat ->
                _uiState.update {
                    it.copy(
                        cat = cat,
                        isLoading = false,
                        sleepAdCount = prefs.getInt(KEY_SLEEP_AD_COUNT, 0)
                    )
                }
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

    // ===== Cat Actions =====

    fun isCatSleeping(): Boolean = _uiState.value.cat.isSleeping

    fun petCat() {
        if (isCatSleeping()) return
        viewModelScope.launch {
            val allowed = catRepository.petCat()
            // Monotonic ID — composables observe id changes to trigger animation.
            // No fragile null-reset after delay.
            petResultIdCounter += 1
            _petResultEvent.value = PetResultEvent(id = petResultIdCounter, allowed = allowed)
            // Back-compat: keep boolean flag for existing collectors.
            _petResult.value = allowed
            if (!allowed) setMessage(context.getString(R.string.cat_pet_limit_reached))
        }
    }

    private fun formatRemainingTime(expiresAtMs: Long): String {
        val diff = expiresAtMs - System.currentTimeMillis()
        if (diff <= 0) return ""
        val minutes = (diff / 1000 / 60).toInt()
        val hours = minutes / 60; val mins = minutes % 60
        return if (hours > 0) context.getString(R.string.time_fmt_hm, hours, mins)
        else context.getString(R.string.time_fmt_m, mins)
    }

    fun getSleepRemainingTime(): String = formatRemainingTime(_uiState.value.cat.sleepEndTime)

    fun getBoosterRemainingTime(expiresAt: Long): String = formatRemainingTime(expiresAt)

    data class BoosterInfo(val name: String, val expiresAt: Long, val emoji: String)

    fun getActiveBoosters(): List<BoosterInfo> {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        val boosters = mutableListOf<BoosterInfo>()

        if (state.stepBoostExpiresAt > now) {
            boosters.add(BoosterInfo("Step Boost", state.stepBoostExpiresAt, "👟"))
        }
        if (state.xpBoostExpiresAt > now) {
            boosters.add(BoosterInfo("XP Boost", state.xpBoostExpiresAt, "⭐"))
        }
        if (state.comboBoostExpiresAt > now) {
            boosters.add(BoosterInfo("Combo Boost", state.comboBoostExpiresAt, "💫"))
        }

        return boosters
    }

    fun feedCatWithItem(item: ShopItem) {
        // Single SoT: uiState.isFeedingInProgress.
        if (_uiState.value.isFeedingInProgress) return
        if (isCatSleeping()) { setMessage(context.getString(R.string.cat_msg_sleeping, getSleepRemainingTime())); return }
        if (_uiState.value.cat.hunger >= 95) { setMessage(context.getString(R.string.cat_msg_full)); return }
        if ((_uiState.value.inventory[item] ?: 0) <= 0) { setMessage(context.getString(R.string.shop_error_no_stock)); return }
        viewModelScope.launch {
            _uiState.update { it.copy(isFeedingInProgress = true) }
            try {
                val success = shopRepository.feedCatWithItem(item)
                if (success) setMessage(context.getString(R.string.cat_msg_yummy))
            } finally {
                // Always reset the flag — covers exception path too.
                _uiState.update { it.copy(isFeedingInProgress = false) }
            }
        }
    }

    private fun getPurchaseSuccessMessage(item: ShopItem): String =
        if (item.isBoost) {
            val msgRes = when (item.id) {
                ShopItem.ID_XP_MULTIPLIER -> R.string.shop_boost_xp_purchased
                ShopItem.ID_COMBO_MULTIPLIER -> R.string.shop_boost_combo_purchased
                else -> R.string.shop_boost_purchased
            }
            context.getString(msgRes)
        } else {
            context.getString(R.string.shop_msg_purchased, item.emoji, "")
        }

    fun buyFood(item: ShopItem) {
        if (!item.isBoost) {
            val currentQty = _uiState.value.inventory[item] ?: 0
            if (currentQty >= ShopItem.MAX_INVENTORY_PER_ITEM) {
                setMessage(context.getString(R.string.shop_error_inventory_full, ShopItem.MAX_INVENTORY_PER_ITEM)); return
            }
        }
        if (_uiState.value.cat.coins < item.price) { setMessage(context.getString(R.string.shop_error_no_coin)); return }
        viewModelScope.launch {
            val success = shopRepository.buyFood(item)
            if (success) {
                setMessage(getPurchaseSuccessMessage(item))
            } else setMessage(context.getString(R.string.shop_error_no_coin))
        }
    }

    // ===== Sleep =====

    fun sleepCat() {
        if (isCatSleeping()) { setMessage(context.getString(R.string.cat_msg_already_sleeping)); return }
        val cat = _uiState.value.cat
        if (cat.energy >= 40) { setMessage(context.getString(R.string.cat_msg_not_tired)); return }

        viewModelScope.launch {
            val neededEnergy = 100 - cat.energy
            val sleepDurationMillis = (neededEnergy / 34.0 * 60 * 60 * 1000).toLong()
            val endTime = System.currentTimeMillis() + sleepDurationMillis

            catRepository.updateSleepState(isSleeping = true, sleepEndTime = endTime, energy = cat.energy, lastUpdated = System.currentTimeMillis())
            _uiState.value = _uiState.value.copy(sleepAdCount = 0)
            prefs.edit().putInt(KEY_SLEEP_AD_COUNT, 0).apply()

            val minutes = (sleepDurationMillis / 1000 / 60).toInt()
            val hours = minutes / 60; val mins = minutes % 60
            val timeStr = if (hours > 0) context.getString(R.string.time_fmt_hm, hours, mins) else context.getString(R.string.time_fmt_m, mins)
            setMessage(context.getString(R.string.cat_msg_goodnight, timeStr))
            interactionRepository.logInteraction(type = InteractionType.SLEEP)
        }
    }

    fun reduceSleepTime() {
        if (!isCatSleeping()) return
        if (!canUseSleepAd()) {
            setMessage(context.getString(R.string.cat_ad_limit_reached))
            return
        }
        val cat = _uiState.value.cat
        val boostedEnergy = (cat.energy + 25).coerceAtMost(100)
        val neededEnergy = 100 - boostedEnergy
        val isWakingUp = neededEnergy <= 0

        val newEndTime = if (isWakingUp) 0L else {
            val remainingMillis = (neededEnergy / 34.0 * 60 * 60 * 1000).toLong()
            System.currentTimeMillis() + remainingMillis
        }

        viewModelScope.launch {
            // Repo write first; if it throws, the ad-counter is NOT consumed
            // (so the user keeps their remaining sleep-ad allowance).
            try {
                catRepository.updateSleepState(isSleeping = !isWakingUp, sleepEndTime = newEndTime, energy = boostedEnergy, lastUpdated = System.currentTimeMillis())
            } catch (e: Exception) {
                setMessage(context.getString(R.string.cat_ad_error))
                return@launch
            }
            val newCount = prefs.getInt(KEY_SLEEP_AD_COUNT, 0) + 1
            prefs.edit().putInt(KEY_SLEEP_AD_COUNT, newCount).apply()
            _uiState.value = _uiState.value.copy(sleepAdCount = newCount)
            if (isWakingUp) setMessage(context.getString(R.string.cat_msg_woke_up))
            else setMessage(context.getString(R.string.cat_msg_sleep_reduced))
        }
    }

    // ===== Game delegation =====

    private val _devLevelsUnlocked = MutableStateFlow(false)
    val devLevelsUnlocked: StateFlow<Boolean> = _devLevelsUnlocked.asStateFlow()

    fun toggleDevLevelUnlock() { _devLevelsUnlocked.value = !_devLevelsUnlocked.value }

    private fun effectiveLevel(ignoreLevelLock: Boolean = false): Int =
        if (ignoreLevelLock || _devLevelsUnlocked.value) Int.MAX_VALUE else _uiState.value.cat.level

    fun startGame(
        type: GameType,
        ignoreLevelLock: Boolean = false,
        ignoreEnergyLimit: Boolean = false
    ) =
        gameDelegate.startGame(
            type = type,
            catEnergy = _uiState.value.cat.energy,
            catLevel = effectiveLevel(ignoreLevelLock),
            isSleeping = isCatSleeping(),
            sleepTimeStr = getSleepRemainingTime(),
            ignoreEnergyLimit = ignoreEnergyLimit
        )
    fun closeMiniGame() = gameDelegate.closeMiniGame()
    fun playRPS(choice: RockPaperScissors) = gameDelegate.playRPS(choice)
    fun spinSlots() = gameDelegate.spinSlots()
    fun startMemoryGame() = gameDelegate.startMemoryGame()
    fun flipMemoryCard(index: Int) = gameDelegate.flipMemoryCard(index)
    fun startReflexGame() = gameDelegate.startReflexGame()
    fun nextReflexRound() = gameDelegate.nextReflexRound()
    fun tapReflexTarget(targetId: Int) = gameDelegate.tapReflexTarget(targetId)
    fun startCatchGame() = gameDelegate.startCatchGame()
    fun finishCatchGame(score: Int) = gameDelegate.finishCatchGame(score)
    fun catchTick(dt: Float) = gameDelegate.catchTick(dt)
    fun moveCatchPaddle(dx: Float, arenaWidthPx: Float, paddleWidthPx: Float) =
        gameDelegate.moveCatchPaddle(dx, arenaWidthPx, paddleWidthPx)
    val catchGameState: StateFlow<CatchGameState> get() = gameDelegate.catchGameState

    // ===== Ads =====

    fun addGoldForAd() {
        viewModelScope.launch {
            if (!canClaimDailyGoldAd()) {
                refreshDailyAdCount()
                setMessage(context.getString(R.string.cat_ad_limit_reached))
                return@launch
            }
            catRepository.addCoins(
                amount = EconomyConfig.GOLD_PER_AD,
                source = EconomySource.AD_REWARD,
                note = "rewarded_ad_gold"
            )
            val today = java.time.LocalDate.now().toString()
            val currentCount = getTodayGoldAdCount()
            val newCount = currentCount + 1
            prefs.edit().putInt(KEY_GOLD_AD_COUNT, newCount).putString(KEY_GOLD_AD_DATE, today).apply()
            _uiState.update { it.copy(dailyGoldAdsRemaining = (EconomyConfig.DAILY_GOLD_AD_LIMIT - newCount).coerceAtLeast(0)) }
            setMessage(context.getString(R.string.cat_msg_gold_added, EconomyConfig.GOLD_PER_AD))
        }
    }

    fun loadFoodAd() {
        if (!canClaimDailyGoldAd()) return
        val currentState = _uiState.value.foodAdState
        if (currentState is AdState.Loading || currentState is AdState.Loaded) return
        _uiState.update { it.copy(foodAdState = AdState.Loading) }
        adManager.loadRewardedAd(adManager.FOOD_AD_ID,
            onAdLoaded = { ad -> _uiState.update { it.copy(foodAdState = AdState.Loaded(ad)) } },
            onAdFailed = { err ->
                _uiState.update {
                    it.copy(
                        foodAdState = AdState.Error,
                        adError = context.getString(R.string.cat_ad_error)
                            + (err?.message?.let { m -> ": $m" } ?: "")
                    )
                }
            }
        )
    }

    fun showFoodAd(activity: Activity) {
        val state = _uiState.value.foodAdState
        if (!canClaimDailyGoldAd()) {
            setMessage(context.getString(R.string.cat_ad_limit_reached))
            _uiState.update { it.copy(foodAdState = AdState.Idle) }
            return
        }
        if (state is AdState.Loaded) {
            // Attach show-time failure listener (RewardedAd.show has no error callback param).
            state.ad.fullScreenContentCallback =
                object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        _uiState.update {
                            it.copy(
                                foodAdState = AdState.Error,
                                adError = context.getString(R.string.cat_ad_error) + ": ${adError.message}"
                            )
                        }
                    }
                    override fun onAdDismissedFullScreenContent() {
                        _uiState.update { it.copy(foodAdState = AdState.Idle) }
                    }
                }
            try {
                state.ad.show(activity) { _ -> addGoldForAd() }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        foodAdState = AdState.Error,
                        adError = context.getString(R.string.cat_ad_error) + ": ${e.message ?: e.javaClass.simpleName}"
                    )
                }
            }
        }
    }

    fun loadSleepAd() {
        if (!canUseSleepAd()) return
        val currentState = _uiState.value.sleepAdState
        if (currentState is AdState.Loading || currentState is AdState.Loaded) return
        _uiState.update { it.copy(sleepAdState = AdState.Loading) }
        adManager.loadRewardedAd(adManager.SLEEP_AD_ID,
            onAdLoaded = { ad -> _uiState.update { it.copy(sleepAdState = AdState.Loaded(ad)) } },
            onAdFailed = { err ->
                _uiState.update {
                    it.copy(
                        sleepAdState = AdState.Error,
                        adError = context.getString(R.string.cat_ad_error)
                            + (err?.message?.let { m -> ": $m" } ?: "")
                    )
                }
            }
        )
    }

    fun showSleepAd(activity: Activity) {
        val state = _uiState.value.sleepAdState
        if (!canUseSleepAd()) {
            setMessage(context.getString(R.string.cat_ad_limit_reached))
            _uiState.update { it.copy(sleepAdState = AdState.Idle) }
            return
        }
        if (state is AdState.Loaded) {
            state.ad.fullScreenContentCallback =
                object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        _uiState.update {
                            it.copy(
                                sleepAdState = AdState.Error,
                                adError = context.getString(R.string.cat_ad_error) + ": ${adError.message}"
                            )
                        }
                    }
                    override fun onAdDismissedFullScreenContent() {
                        _uiState.update { it.copy(sleepAdState = AdState.Idle) }
                    }
                }
            try {
                state.ad.show(activity) { _ -> reduceSleepTime() }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        sleepAdState = AdState.Error,
                        adError = context.getString(R.string.cat_ad_error) + ": ${e.message ?: e.javaClass.simpleName}"
                    )
                }
            }
        }
    }

    fun clearAdError() { _uiState.update { it.copy(adError = null) } }

    fun setAdLoading(isLoading: Boolean) { _uiState.update { it.copy(isAdLoading = isLoading, adLoadError = false) } }
    fun setAdError(hasError: Boolean) { _uiState.update { it.copy(adLoadError = hasError, isAdLoading = false) } }
    fun showGoldStatus() {
        _uiState.update { it.copy(userMessage = context.getString(R.string.cat_earn_gold_hint)) }
    }

    // ===== Messages =====

    fun clearMessage() { _uiState.update { it.copy(userMessage = null) } }
    fun setMessage(msg: String) { _uiState.update { it.copy(userMessage = msg) } }

    // ===== Network =====

    private fun startNetworkMonitoring() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val isConnected = caps != null && (
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        )
        _uiState.value = _uiState.value.copy(isNetworkAvailable = isConnected)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) { _uiState.update { it.copy(isNetworkAvailable = true) } }
            override fun onLost(network: android.net.Network) { _uiState.update { it.copy(isNetworkAvailable = false) } }
        }
        try {
            cm.registerDefaultNetworkCallback(callback)
            networkCallback = callback
        } catch (e: Exception) {
            // Silently fail if callback registration fails
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure GameDelegate cleans up any in-flight game jobs (memory/reflex/catch).
        try {
            gameDelegate.closeMiniGame()
        } catch (_: IllegalStateException) {
            // Delegate already torn down — safe to ignore.
        }
        networkCallback?.let {
            try {
                (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).unregisterNetworkCallback(it)
            } catch (e: IllegalArgumentException) {
                // Already unregistered or invalid
            }
        }
    }
}
