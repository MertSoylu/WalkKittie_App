package com.mert.paticat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.data.local.dao.UserProfileDao
import com.mert.paticat.data.local.dao.CatDao
import com.mert.paticat.data.local.dao.DailyStatsDao
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import com.mert.paticat.ui.navigation.Screen
import com.mert.paticat.ui.theme.ThemeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val userProfileDao: UserProfileDao,
    private val catDao: CatDao,
    private val dailyStatsDao: DailyStatsDao
) : ViewModel() {

    // Start destination based on user status
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()



    private val _catName = MutableStateFlow("Mochi")
    val catName: StateFlow<String> = _catName.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentThemeColor = MutableStateFlow(ThemeColor.Pink)
    val currentThemeColor: StateFlow<ThemeColor> = _currentThemeColor.asStateFlow()

    private val _rewardNotificationData = MutableStateFlow<com.mert.paticat.ui.components.RewardNotificationData?>(null)
    val rewardNotificationData: StateFlow<com.mert.paticat.ui.components.RewardNotificationData?> = _rewardNotificationData.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent: StateFlow<Int?> = _levelUpEvent.asStateFlow()

    init {
        try {
            observeUserStatus()
            observeCatName()
            observeTheme()
            observePendingRewards()
            calculateAndSaveStreak()
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Init failed", e)
            _startDestination.value = Screen.Welcome.route
        }
    }

    private fun observeUserStatus() {
        viewModelScope.launch {
            try {
                // Combine flows to determine start destination deterministically
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
                android.util.Log.e("MainViewModel", "User status observation failed", e)
                _startDestination.value = Screen.Language.route
            }
        }
    }


    private fun observeCatName() {
        viewModelScope.launch {
            try {
                catDao.getCat().collect { cat ->
                    cat?.let {
                        _catName.value = it.name
                        
                        // Level up check
                        val lastSeenLevel = preferencesRepository.lastSeenLevel.first()
                        if (it.level > lastSeenLevel) {
                            _levelUpEvent.value = it.level
                        }
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
                    // Map legacy names or utilize new names
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
                preferencesRepository.pendingRewardFood
            ) { xp, food ->
                if (xp > 0 || food > 0) {
                    com.mert.paticat.ui.components.RewardNotificationData(
                        xp = xp,
                        foodPoints = food
                    )
                } else {
                    null
                }
            }.collect { data ->
                if (data != null && _rewardNotificationData.value == null) {
                    _rewardNotificationData.value = data
                }
            }
        }
    }

    fun clearRewardNotification() {
        viewModelScope.launch {
            _rewardNotificationData.value = null
            preferencesRepository.clearPendingRewards()
        }
    }

    fun dismissLevelUpEvent(newLevel: Int) {
        viewModelScope.launch {
            _levelUpEvent.value = null
            preferencesRepository.updateLastSeenLevel(newLevel)
        }
    }

    private fun calculateAndSaveStreak() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val userProfile = userProfileDao.getUserProfileOnce() ?: return@launch
                val stepGoal = userProfile.dailyStepGoal
                
                var streak = 0
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE
                val today = LocalDate.now()
                
                // Check today
                val todayStats = dailyStatsDao.getStatsForDateOnce(today.format(formatter))
                if (todayStats != null && todayStats.steps >= stepGoal) {
                    streak++
                }
                
                // Check past days (bounded to max 365 days to prevent infinite loop)
                var i = 1L
                while (i <= 365L) {
                    val date = today.minusDays(i)
                    val stats = dailyStatsDao.getStatsForDateOnce(date.format(formatter))
                    
                    if (stats != null && stats.steps >= stepGoal) {
                        streak++
                        i++
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
                    userProfileDao.updateProfile(updatedProfile)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Streak calculation failed", e)
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
