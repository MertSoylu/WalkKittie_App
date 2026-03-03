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
import com.mert.paticat.domain.repository.InteractionRepository
import com.mert.paticat.domain.repository.ShopRepository
import com.mert.paticat.domain.model.InteractionType
import com.mert.paticat.domain.model.ShopItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val adManager: com.mert.paticat.data.ads.AdManager,
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
        context = context,
        onMessage = ::setMessage
    )
    val gameUiState: StateFlow<GameUiState> = gameDelegate.gameUiState
    val playerChoice: StateFlow<RockPaperScissors?> = gameDelegate.playerChoice
    val catChoice: StateFlow<RockPaperScissors?> = gameDelegate.catChoice

    // ===== Pet animation =====
    private val _petResult = MutableStateFlow<Boolean?>(null)
    val petResult: StateFlow<Boolean?> = _petResult.asStateFlow()

    // ===== SharedPreferences for ad tracking =====
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
            catRepository.addCoins(50)
            _uiState.update { it.copy(showGoldTutorial = true) }
        }
    }

    fun dismissGoldTutorial() {
        prefs.edit().putBoolean(KEY_GOLD_TUTORIAL, true).apply()
        _uiState.update { it.copy(showGoldTutorial = false) }
    }

    // ===== Initialization =====

    private fun refreshDailyAdCount() {
        val today = java.time.LocalDate.now().toString()
        val storedDate = prefs.getString(KEY_GOLD_AD_DATE, "") ?: ""
        if (storedDate != today) {
            prefs.edit().putInt(KEY_GOLD_AD_COUNT, 0).putString(KEY_GOLD_AD_DATE, today).apply()
            _uiState.update { it.copy(dailyGoldAdsRemaining = ShopItem.MAX_GOLD_ADS_PER_DAY) }
        } else {
            val usedToday = prefs.getInt(KEY_GOLD_AD_COUNT, 0)
            _uiState.update { it.copy(dailyGoldAdsRemaining = (ShopItem.MAX_GOLD_ADS_PER_DAY - usedToday).coerceAtLeast(0)) }
        }
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

    private fun observeCat() {
        viewModelScope.launch {
            catRepository.getCat().collect { cat ->
                _uiState.value = _uiState.value.copy(
                    cat = cat, isLoading = false,
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

    // ===== Cat Actions =====

    fun isCatSleeping(): Boolean = _uiState.value.cat.isSleeping

    fun petCat() {
        if (isCatSleeping()) return
        viewModelScope.launch {
            val allowed = catRepository.petCat()
            _petResult.value = allowed
            if (!allowed) setMessage(context.getString(R.string.cat_pet_limit_reached))
            delay(50); _petResult.value = null
        }
    }

    fun getSleepRemainingTime(): String {
        val diff = _uiState.value.cat.sleepEndTime - System.currentTimeMillis()
        if (diff <= 0) return ""
        val minutes = (diff / 1000 / 60).toInt()
        val hours = minutes / 60; val mins = minutes % 60
        return if (hours > 0) context.getString(R.string.time_fmt_hm, hours, mins)
        else context.getString(R.string.time_fmt_m, mins)
    }

    fun feedCatWithItem(item: ShopItem) {
        if (isCatSleeping()) { setMessage(context.getString(R.string.cat_msg_sleeping, getSleepRemainingTime())); return }
        if (_uiState.value.cat.hunger >= 95) { setMessage(context.getString(R.string.cat_msg_full)); return }
        if ((_uiState.value.inventory[item] ?: 0) <= 0) { setMessage(context.getString(R.string.shop_error_no_stock)); return }
        viewModelScope.launch {
            val success = shopRepository.feedCatWithItem(item)
            if (success) setMessage(context.getString(R.string.cat_msg_yummy))
        }
    }

    fun buyFood(item: ShopItem) {
        val currentQty = _uiState.value.inventory[item] ?: 0
        if (currentQty >= ShopItem.MAX_INVENTORY_PER_ITEM) {
            setMessage(context.getString(R.string.shop_error_inventory_full, ShopItem.MAX_INVENTORY_PER_ITEM)); return
        }
        if (_uiState.value.cat.coins < item.price) { setMessage(context.getString(R.string.shop_error_no_coin)); return }
        viewModelScope.launch {
            val success = shopRepository.buyFood(item)
            if (success) setMessage(context.getString(R.string.shop_msg_purchased, item.emoji, ""))
            else setMessage(context.getString(R.string.shop_error_no_coin))
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
        val cat = _uiState.value.cat
        val boostedEnergy = (cat.energy + 25).coerceAtMost(100)
        val neededEnergy = 100 - boostedEnergy
        val isWakingUp = neededEnergy <= 0

        val newEndTime = if (isWakingUp) 0L else {
            val remainingMillis = (neededEnergy / 34.0 * 60 * 60 * 1000).toLong()
            System.currentTimeMillis() + remainingMillis
        }

        val newCount = prefs.getInt(KEY_SLEEP_AD_COUNT, 0) + 1
        prefs.edit().putInt(KEY_SLEEP_AD_COUNT, newCount).apply()

        viewModelScope.launch {
            catRepository.updateSleepState(isSleeping = !isWakingUp, sleepEndTime = newEndTime, energy = boostedEnergy, lastUpdated = System.currentTimeMillis())
            _uiState.value = _uiState.value.copy(sleepAdCount = newCount)
            if (isWakingUp) setMessage(context.getString(R.string.cat_msg_woke_up))
            else setMessage(context.getString(R.string.cat_msg_sleep_reduced))
        }
    }

    // ===== Game delegation =====

    fun startGame(type: GameType) = gameDelegate.startGame(type, _uiState.value.cat.energy, isCatSleeping(), getSleepRemainingTime())
    fun closeMiniGame() = gameDelegate.closeMiniGame()
    fun playRPS(choice: RockPaperScissors) = gameDelegate.playRPS(choice)
    fun spinSlots() = gameDelegate.spinSlots()
    fun startMemoryGame() = gameDelegate.startMemoryGame()
    fun flipMemoryCard(index: Int) = gameDelegate.flipMemoryCard(index)
    fun startReflexGame() = gameDelegate.startReflexGame()
    fun nextReflexRound() = gameDelegate.nextReflexRound()
    fun tapReflexTarget(targetId: Int) = gameDelegate.tapReflexTarget(targetId)

    // ===== Ads =====

    fun addGoldForAd() {
        viewModelScope.launch {
            catRepository.addCoins(ShopItem.GOLD_PER_AD)
            val today = java.time.LocalDate.now().toString()
            val storedDate = prefs.getString(KEY_GOLD_AD_DATE, "") ?: ""
            val currentCount = if (storedDate == today) prefs.getInt(KEY_GOLD_AD_COUNT, 0) else 0
            val newCount = currentCount + 1
            prefs.edit().putInt(KEY_GOLD_AD_COUNT, newCount).putString(KEY_GOLD_AD_DATE, today).apply()
            _uiState.update { it.copy(dailyGoldAdsRemaining = (ShopItem.MAX_GOLD_ADS_PER_DAY - newCount).coerceAtLeast(0)) }
            setMessage(context.getString(R.string.cat_msg_gold_added, ShopItem.GOLD_PER_AD))
        }
    }

    fun loadFoodAd() {
        val currentState = _uiState.value.foodAdState
        if (currentState is AdState.Loading || currentState is AdState.Loaded) return
        _uiState.update { it.copy(foodAdState = AdState.Loading) }
        adManager.loadRewardedAd(adManager.FOOD_AD_ID,
            onAdLoaded = { ad -> _uiState.update { it.copy(foodAdState = AdState.Loaded(ad)) } },
            onAdFailed = { _uiState.update { it.copy(foodAdState = AdState.Error) } }
        )
    }

    fun showFoodAd(activity: Activity) {
        val state = _uiState.value.foodAdState
        if (state is AdState.Loaded) {
            state.ad.show(activity) { _ -> addGoldForAd(); _uiState.update { it.copy(foodAdState = AdState.Idle) } }
        }
    }

    fun loadSleepAd() {
        val currentState = _uiState.value.sleepAdState
        if (currentState is AdState.Loading || currentState is AdState.Loaded) return
        _uiState.update { it.copy(sleepAdState = AdState.Loading) }
        adManager.loadRewardedAd(adManager.SLEEP_AD_ID,
            onAdLoaded = { ad -> _uiState.update { it.copy(sleepAdState = AdState.Loaded(ad)) } },
            onAdFailed = { _uiState.update { it.copy(sleepAdState = AdState.Error) } }
        )
    }

    fun showSleepAd(activity: Activity) {
        val state = _uiState.value.sleepAdState
        if (state is AdState.Loaded) {
            state.ad.show(activity) { _ -> reduceSleepTime(); _uiState.update { it.copy(sleepAdState = AdState.Idle) } }
        }
    }

    fun setAdLoading(isLoading: Boolean) { _uiState.update { it.copy(isAdLoading = isLoading, adLoadError = false) } }
    fun setAdError(hasError: Boolean) { _uiState.update { it.copy(adLoadError = hasError, isAdLoading = false) } }
    fun showGoldStatus() { /* no-op */ }

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

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) { _uiState.update { it.copy(isNetworkAvailable = true) } }
            override fun onLost(network: android.net.Network) { _uiState.update { it.copy(isNetworkAvailable = false) } }
        }
        try { cm.registerDefaultNetworkCallback(networkCallback!!) } catch (e: Exception) { networkCallback = null }
    }

    override fun onCleared() {
        super.onCleared()
        networkCallback?.let {
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).unregisterNetworkCallback(it)
        }
    }
}
