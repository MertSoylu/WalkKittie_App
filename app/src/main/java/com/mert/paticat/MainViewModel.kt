package com.mert.paticat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.HealthRepository
import com.mert.paticat.domain.repository.UserProfileRepository
import com.mert.paticat.ui.navigation.Screen
import com.mert.paticat.ui.theme.ThemeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val userProfileRepository: UserProfileRepository,
    private val catRepository: CatRepository,
    private val healthRepository: HealthRepository
) : ViewModel() {

    // Start destination based on user status
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    // Default empty until cat repo emits — prevents tutorial/UI from showing
    // a placeholder name before user-provided value loads.
    private val _catName = MutableStateFlow("")
    val catName: StateFlow<String> = _catName.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentThemeColor = MutableStateFlow(ThemeColor.Pink)
    val currentThemeColor: StateFlow<ThemeColor> = _currentThemeColor.asStateFlow()

    private val _rewardNotificationData = MutableStateFlow<com.mert.paticat.ui.components.RewardNotificationData?>(null)
    val rewardNotificationData: StateFlow<com.mert.paticat.ui.components.RewardNotificationData?> = _rewardNotificationData.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent: StateFlow<Int?> = _levelUpEvent.asStateFlow()

    private var lastSeenLevelCache = 1

    private val _rewardQueue = ArrayDeque<com.mert.paticat.ui.components.RewardNotificationData>()
    private var _lastQueuedReward: com.mert.paticat.ui.components.RewardNotificationData? = null

    init {
        try {
            observeUserStatus()
            observeCatName()
            observeTheme()
            observePendingRewards()
            calculateAndSaveStreak()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                android.util.Log.e("MainViewModel", "Init failed", e)
            }
            _startDestination.value = Screen.Welcome.route
        }
    }

    private fun observeUserStatus() {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.flow.combine(
                    preferencesRepository.isLanguageSelected,
                    preferencesRepository.isOnboardingCompleted
                ) { isLanguageSelected, isOnboardingCompleted ->
                    if (!isLanguageSelected) {
                        Screen.Language.route
                    } else if (isOnboardingCompleted) {
                        Screen.MainApp.route
                    } else {
                        Screen.Welcome.route
                    }
                }.collect { route ->
                    _startDestination.value = route
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.e("MainViewModel", "User status observation failed", e)
                }
                _startDestination.value = Screen.Language.route
            }
        }
    }

    private fun observeCatName() {
        viewModelScope.launch {
            try {
                lastSeenLevelCache = preferencesRepository.lastSeenLevel.first()
                catRepository.getCat()
                    .distinctUntilChanged { old, new -> old.level == new.level && old.name == new.name }
                    .collect { cat ->
                        _catName.value = cat.name

                        // Level up check
                        if (cat.level > lastSeenLevelCache) {
                            _levelUpEvent.value = cat.level
                            lastSeenLevelCache = cat.level
                        }
                    }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            launch {
                preferencesRepository.selectedTheme.collect { themeName ->
                    _currentThemeColor.value = when(themeName) {
                        "Standard", "Pink" -> ThemeColor.Pink
                        "Ocean", "Blue" -> ThemeColor.Blue
                        "Nature", "Green" -> ThemeColor.Green
                        "Sunset", "Purple" -> ThemeColor.Purple
                        "Orange" -> ThemeColor.Orange
                        else -> try {
                             ThemeColor.valueOf(themeName)
                        } catch (e: Exception) {
                             ThemeColor.Pink
                        }
                    }
                }
            }
            launch {
                preferencesRepository.isDarkMode.collect { isDark ->
                    _isDarkMode.value = isDark
                }
            }
        }
    }

    private fun observePendingRewards() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                preferencesRepository.pendingRewardXp,
                preferencesRepository.pendingRewardGold,
                preferencesRepository.stepBoostExpiresAt,
                preferencesRepository.xpBoostExpiresAt,
                preferencesRepository.comboBoostExpiresAt
            ) { xp, gold, stepBoostExpiresAt, xpBoostExpiresAt, comboBoostExpiresAt ->
                if (xp > 0 || gold > 0) {
                    val now = System.currentTimeMillis()
                    val isComboBoosted = comboBoostExpiresAt > now
                    com.mert.paticat.ui.components.RewardNotificationData(
                        xp = xp,
                        gold = gold,
                        isXpBoosted = xp > 0 && (isComboBoosted || xpBoostExpiresAt > now),
                        isGoldBoosted = gold > 0 && (isComboBoosted || stepBoostExpiresAt > now)
                    )
                } else {
                    null
                }
            }.collect { data ->
                if (data == null || data == _lastQueuedReward) return@collect
                _lastQueuedReward = data
                if (_rewardNotificationData.value == null) {
                    _rewardNotificationData.value = data
                } else {
                    _rewardQueue.addLast(data)
                }
            }
        }
    }

    fun clearRewardNotification() {
        viewModelScope.launch {
            _rewardNotificationData.value = null
            _lastQueuedReward = null
            preferencesRepository.clearPendingRewards()
            dequeueNextReward()
        }
    }

    private fun dequeueNextReward() {
        if (_rewardQueue.isNotEmpty()) {
            _rewardNotificationData.value = _rewardQueue.removeFirst()
        }
    }

    fun dismissLevelUpEvent(newLevel: Int) {
        lastSeenLevelCache = newLevel
        viewModelScope.launch {
            _levelUpEvent.value = null
            preferencesRepository.updateLastSeenLevel(newLevel)
        }
    }

    private fun calculateAndSaveStreak() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val userProfile = userProfileRepository.getUserProfileOnce() ?: return@launch
                val stepGoal = userProfile.dailyStepGoal
                val today = LocalDate.now()
                val rangeStart = today.minusDays(365)

                val statsMap = healthRepository
                    .getStatsForDateRange(rangeStart, today)
                    .first()
                    .associateBy { it.date }

                var streak = 0
                var date = today
                while (date >= rangeStart) {
                    val stats = statsMap[date]
                    if (stats != null && stats.steps >= stepGoal) {
                        streak++
                        date = date.minusDays(1)
                    } else {
                        break
                    }
                }

                if (userProfile.currentStreak != streak) {
                    val newLongest = maxOf(userProfile.longestStreak, streak)
                    val updatedProfile = userProfile.copy(
                        currentStreak = streak,
                        longestStreak = newLongest
                    )
                    userProfileRepository.updateProfile(updatedProfile)
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.e("MainViewModel", "Streak calculation failed", e)
                }
            }
        }
    }

    fun updateThemeColor(color: ThemeColor) {
        viewModelScope.launch {
            preferencesRepository.updateTheme(color.name)
        }
    }

    fun updateDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDarkMode(isDark)
        }
    }

    fun setLanguageSelected(language: String) {
        viewModelScope.launch {
            preferencesRepository.updateLocale(language)
        }
    }
    
    fun checkUserStatus() {
        // No-op
    }
}
