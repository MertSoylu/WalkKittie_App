package com.mert.paticat.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.StepCounterManager
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.HealthRepository
import com.mert.paticat.domain.repository.MissionRepository
import com.mert.paticat.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import com.mert.paticat.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * ViewModel for Home Screen.
 * Manages health data, cat status, and missions.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val catRepository: CatRepository,
    private val healthRepository: HealthRepository,
    private val missionRepository: MissionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val stepCounterManager: StepCounterManager,
    private val adManager: com.mert.paticat.data.ads.AdManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var undoTimeoutJob: Job? = null

    // Guards parallel mission completion attempts that could race when steps & water
    // both cross their thresholds in the same emission window.
    private val missionCompletionMutex = Mutex()

    init {
        // Side-effecting work moved off the constructor's call stack so ViewModel
        // construction stays lightweight.
        viewModelScope.launch { initializeData() }
        viewModelScope.launch { observeData() }
        viewModelScope.launch { observeAds() }
    }

    private suspend fun observeAds() {
        // Trigger ad refresh check when ViewModel is created/resumed
        adManager.loadNativeAd()

        adManager.nativeAd.collect { ad ->
            _uiState.update { it.copy(nativeAd = ad) }
        }
    }

    private suspend fun initializeData() {
        try {
            // Initialize cat if not exists
            catRepository.initializeCat()

            // Mark that user opened the app — updates lastInteractionTime
            // and triggers decay calculation (applyDecayLogic included)
            catRepository.markUserInteraction()

            // Generate daily missions
            missionRepository.generateDailyMissions()

            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: ""
                )
            }
        }
    }

    private suspend fun observeData() {
        // Combined data flow updates a single state copy.
        viewModelScope.launch {
            combine(
                catRepository.getCat(),
                healthRepository.getTodayStats(),
                missionRepository.getTodayMissions(),
                userProfileRepository.getUserProfile(),
                stepCounterManager.liveSteps
            ) { cat, stats, missions, profile, liveSteps ->
                // Apply goals from profile if available, otherwise defaults
                val currentStepGoal = profile?.dailyStepGoal ?: 10000
                val currentWaterGoal = profile?.dailyWaterGoalMl ?: 2000

                // Use live steps only when they exceed persisted stats; otherwise fall back
                // to persisted DB value. liveSteps is non-nullable Int but may be 0 before
                // the sensor emits — `takeIf` keeps the contract explicit.
                val currentSteps = liveSteps.takeIf { it > stats.steps } ?: stats.steps
                val currentStats = stats.copy(
                    steps = currentSteps,
                    distanceKm = (currentSteps * 0.75) / 1000.0
                )

                HomeData(cat, currentStats, missions, currentStepGoal, currentWaterGoal, profile?.currentStreak ?: 0)
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        cat = data.cat,
                        todayStats = data.stats,
                        todayMissions = data.missions,
                        stepGoal = data.stepGoal,
                        waterGoal = data.waterGoal,
                        currentStreak = data.currentStreak
                    )
                }
            }
        }

        // Separate observation to instantly complete missions when their target is reached in the UI.
        // Wrapped in mutex to prevent overlapping completion runs racing on shared mission state.
        _uiState.distinctUntilChanged { old, new ->
            old.todayStats.steps == new.todayStats.steps &&
            old.todayStats.waterMl == new.todayStats.waterMl &&
            old.todayMissions == new.todayMissions
        }.collect { state ->
            missionCompletionMutex.withLock {
                val activeMissions = state.todayMissions.filter { !it.isCompleted }
                if (activeMissions.isEmpty()) return@withLock

                var reachedSteps: Int? = null
                var reachedWater: Int? = null

                activeMissions.forEach { mission ->
                    if (mission.type == com.mert.paticat.domain.model.MissionType.STEPS && state.todayStats.steps >= mission.targetValue) {
                        reachedSteps = state.todayStats.steps
                    }
                    if (mission.type == com.mert.paticat.domain.model.MissionType.WATER && state.todayStats.waterMl >= mission.targetValue) {
                        reachedWater = state.todayStats.waterMl
                    }
                }

                if (reachedSteps != null || reachedWater != null) {
                    missionRepository.checkAndCompleteMissions(steps = reachedSteps, waterMl = reachedWater)
                }
            }
        }
    }

    /**
     * Refresh data - useful when app comes to foreground
     */
    fun refreshData() {
        viewModelScope.launch {
            try {
                catRepository.markUserInteraction()
                missionRepository.generateDailyMissions()
            } catch (e: Exception) {
                // Silently fail on refresh
            }
        }
    }

    // Internal data holder for combine
    private data class HomeData(
        val cat: com.mert.paticat.domain.model.Cat,
        val stats: com.mert.paticat.domain.model.DailyStats,
        val missions: List<com.mert.paticat.domain.model.Mission>,
        val stepGoal: Int,
        val waterGoal: Int,
        val currentStreak: Int
    )

    fun addWater(amountMl: Int) {
        val sanitized = amountMl.coerceAtLeast(1)
        viewModelScope.launch {
            try {
                healthRepository.addWater(sanitized)
                _uiState.update { it.copy(lastAddedWater = sanitized) }

                // Reward for drinking a meaningful amount of water
                if (sanitized >= WATER_REWARD_THRESHOLD_ML) {
                    catRepository.addXp(5)
                    catRepository.updateHappiness(2)
                }

                undoTimeoutJob?.cancel()
                undoTimeoutJob = viewModelScope.launch {
                    delay(8_000)
                    _uiState.update { it.copy(lastAddedWater = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = context.getString(R.string.error_water_add)) }
            }
        }
    }

    fun undoWater() {
        undoTimeoutJob?.cancel()
        val lastAmount = _uiState.value.lastAddedWater ?: return
        viewModelScope.launch {
            try {
                healthRepository.removeWater(lastAmount)
                _uiState.update { it.copy(lastAddedWater = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = context.getString(R.string.error_water_undo)) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onMissionTap(mission: com.mert.paticat.domain.model.Mission) {
        val state = _uiState.value
        val isCompleted = when (mission.type) {
            com.mert.paticat.domain.model.MissionType.STEPS ->
                kotlin.math.max(mission.currentValue, state.todayStats.steps) >= mission.targetValue
            com.mert.paticat.domain.model.MissionType.WATER ->
                kotlin.math.max(mission.currentValue, state.todayStats.waterMl) >= mission.targetValue
            com.mert.paticat.domain.model.MissionType.GAME ->
                mission.currentValue >= mission.targetValue
            else -> mission.isCompleted
        }
        val msg = context.getString(
            if (isCompleted) R.string.mission_completed_feedback
            else R.string.mission_progress_feedback
        )
        _uiState.update { it.copy(userMessage = msg) }
    }

    fun onMoreMissionsClick(): Boolean {
        val state = _uiState.value
        return if (state.cat.energy >= 5) {
            true
        } else {
            _uiState.update {
                it.copy(userMessage = context.getString(R.string.home_low_energy_snackbar))
            }
            false
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private companion object {
        const val WATER_REWARD_THRESHOLD_ML = 250
    }
}
