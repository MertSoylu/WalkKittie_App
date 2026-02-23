package com.mert.paticat.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.StepCounterManager
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.HealthRepository
import com.mert.paticat.domain.repository.MissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val userProfileDao: com.mert.paticat.data.local.dao.UserProfileDao,
    private val stepCounterManager: StepCounterManager,
    private val adManager: com.mert.paticat.data.ads.AdManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        initializeData()
        observeData()
        observeAds()
    }
    
    private fun observeAds() {
        // Trigger ad refresh check when ViewModel is created/resumed
        adManager.loadNativeAd()
        
        viewModelScope.launch {
            adManager.nativeAd.collect { ad ->
                _uiState.update { it.copy(nativeAd = ad) }
            }
        }
    }
    
    private fun initializeData() {
        viewModelScope.launch {
            try {
                // Initialize cat if not exists
                catRepository.initializeCat()
                
                // Mark that user opened the app — updates lastInteractionTime
                // and triggers decay calculation
                catRepository.markUserInteraction()
                
                // Apply any pending decay
                catRepository.decreaseHungerOverTime()
                
                // Generate daily missions
                missionRepository.generateDailyMissions()
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Bir hata oluştu"
                    ) 
                }
            }
        }
    }
    
    private fun observeData() {
        viewModelScope.launch {
            combine(
                catRepository.getCat(),
                healthRepository.getTodayStats(),
                missionRepository.getTodayMissions(),
                userProfileDao.getUserProfile(),
                stepCounterManager.liveSteps
            ) { cat, stats, missions, profile, liveSteps ->
                // Apply goals from profile if available, otherwise defaults
                val currentStepGoal = profile?.dailyStepGoal ?: 10000
                val currentWaterGoal = profile?.dailyWaterGoalMl ?: 2000
                
                // Use live steps if available and greater than persistent stats
                // This ensures UI updates instantly without waiting for DB sync
                val currentSteps = if (liveSteps > stats.steps) liveSteps else stats.steps
                val currentStats = stats.copy(steps = currentSteps)
                
                HomeData(cat, currentStats, missions, currentStepGoal, currentWaterGoal)
            }.collect { data ->
                _uiState.update { 
                    it.copy(
                        cat = data.cat,
                        todayStats = data.stats,
                        todayMissions = data.missions,
                        stepGoal = data.stepGoal,
                        waterGoal = data.waterGoal
                    )
                }
                // NOTE: DB writes (checkAndCompleteMissions, updateCatBasedOnActivity)
                // moved to separate debounced coroutine below to prevent feedback loop
            }
        }
        
        // Separate observation to instantly complete missions when their target is reached in the UI.
        // It does not poll periodically, saving battery, and only writes when crossing the milestone.
        viewModelScope.launch {
            _uiState.collect { state ->
                val activeMissions = state.todayMissions.filter { !it.isCompleted }
                if (activeMissions.isEmpty()) return@collect
                
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
                // Update interactions and decay
                catRepository.markUserInteraction()
                catRepository.decreaseHungerOverTime()
                
                // Refresh missions
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
        val waterGoal: Int
    )
    
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            try {
                healthRepository.addWater(amountMl)
                _uiState.update { it.copy(lastAddedWater = amountMl) }
                
                // Reward for drinking water
                if (amountMl >= 250) {
                    catRepository.addXp(5)
                    catRepository.updateHappiness(2)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Su eklenirken hata: ${e.message}") }
            }
        }
    }
    
    fun undoWater() {
        val lastAmount = _uiState.value.lastAddedWater ?: return
        viewModelScope.launch {
            try {
                healthRepository.removeWater(lastAmount)
                _uiState.update { it.copy(lastAddedWater = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Su geri alınırken hata: ${e.message}") }
            }
        }
    }
    
    fun feedCat() {
        viewModelScope.launch {
            val cat = _uiState.value.cat
            if (cat.foodPoints >= 10) {
                catRepository.feedCat(10)
                catRepository.updateHappiness(5)
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
}
