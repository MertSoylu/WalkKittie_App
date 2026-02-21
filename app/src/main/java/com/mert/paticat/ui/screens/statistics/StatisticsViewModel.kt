package com.mert.paticat.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.data.local.toDbString
import com.mert.paticat.data.local.toDomain
import com.mert.paticat.data.local.dao.DailyStatsDao
import com.mert.paticat.data.local.dao.UserProfileDao
import com.mert.paticat.data.local.entity.DailyStatsEntity
import com.mert.paticat.domain.model.DailyStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import com.mert.paticat.R

enum class StatsRange {
    WEEKLY,
    MONTHLY
}

data class DetailedStat(
    val totalSteps: Int = 0,
    val avgSteps: Int = 0,
    val bestDaySteps: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val avgCaloriesBurned: Int = 0,
    val totalWater: Int = 0,
    val completionRate: Int = 0, // Percentage of days goal met

)

data class StatisticsUiState(
    val selectedRange: StatsRange = StatsRange.WEEKLY,
    val todayStats: DailyStats = DailyStats(LocalDate.now()),
    val historyStats: List<DailyStats> = emptyList(),
    val detailedStats: DetailedStat = DetailedStat(),
    val stepGoal: Int = 10000,
    val waterGoal: Int = 2000,
    val calorieGoal: Int = 2000,
    val chartData: List<Int> = emptyList(), // Steps for chart
    val chartLabels: List<String> = emptyList(),
    val nativeAd: com.google.android.gms.ads.nativead.NativeAd? = null,
    val isLoading: Boolean = true,
    val lastAddedWater: Int? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val dailyStatsDao: DailyStatsDao,
    private val userProfileDao: UserProfileDao,
    private val healthRepository: com.mert.paticat.domain.repository.HealthRepository,
    private val adManager: com.mert.paticat.data.ads.AdManager,
    private val stepCounterManager: com.mert.paticat.StepCounterManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeAds()
    }

    private fun observeAds() {
        viewModelScope.launch {
            adManager.nativeAd.collect { ad ->
                _uiState.update { it.copy(nativeAd = ad) }
            }
        }
    }

    private var loadJob: kotlinx.coroutines.Job? = null

    fun selectRange(range: StatsRange) {
        _uiState.value = _uiState.value.copy(selectedRange = range, isLoading = true)
        loadData()
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val today = LocalDate.now()
            val todayStr = today.toDbString()
            val range = _uiState.value.selectedRange
            
            // Calculate proper date range
            val startDate = if (range == StatsRange.WEEKLY) {
                today.minusDays(6)
            } else {
                today.withDayOfMonth(1) // Start of current month
            }
            val startDateStr = startDate.toDbString()
            
            // Flows
            val profileFlow = userProfileDao.getUserProfile()
            val todayStatsFlow = dailyStatsDao.getStatsForDate(todayStr)
            // Use getStatsInRange for proper date-based filtering
            val recentStatsFlow = dailyStatsDao.getStatsInRange(startDateStr, todayStr)
            
            combine(profileFlow, todayStatsFlow, recentStatsFlow, stepCounterManager.liveSteps) { profile, todayStats, allRecentStats, liveSteps ->
                val stepGoal = profile?.dailyStepGoal ?: 10000
                val waterGoal = profile?.dailyWaterGoalMl ?: 2000
                val calorieGoal = profile?.dailyCalorieGoal ?: 2000
                
                var todayDomain = todayStats?.toDomain() ?: DailyStats(LocalDate.now())
                
                // Update with live steps if needed
                if (todayDomain.date == LocalDate.now() && liveSteps > todayDomain.steps) {
                     todayDomain = todayDomain.copy(steps = liveSteps)
                }

                val historyDomain = allRecentStats.map { 
                    val domain = it.toDomain()
                    if (domain.date == LocalDate.now()) todayDomain else domain
                }.toMutableList()
                
                // If today is newly created and not yet in recentStats, add it
                if (historyDomain.none { it.date == LocalDate.now() }) {
                    historyDomain.add(0, todayDomain)
                }

                // --- Chart Data Logic (Fill missing days with 0) ---
                val rangeDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, today).toInt() + 1
                
                // Filter history to only include days within the selected range for accurate stats
                val filteredHistory = historyDomain.filter { 
                    !it.date.isBefore(startDate) && !it.date.isAfter(today) 
                }
                
                val chartDataPoints = mutableListOf<Int>()
                val chartLabelsList = mutableListOf<String>()

                for (i in 0 until rangeDays) {
                    val dateToCheck = startDate.plusDays(i.toLong())
                    // Find stats for this specific date
                    val statsForDay = filteredHistory.find { it.date == dateToCheck }
                    
                    // Add steps (or 0 if null)
                    chartDataPoints.add(statsForDay?.steps ?: 0)
                    
                    // Add Label
                    val label = if (range == StatsRange.WEEKLY) 
                        getDayLabel(dateToCheck.dayOfWeek)
                    else
                        dateToCheck.dayOfMonth.toString()
                    chartLabelsList.add(label)
                }
                
                // Detailed Calculation (Based on FILTERED history)
                val totalSteps = filteredHistory.sumOf { it.steps }
                // Count days in range with data, or just rangeDays? 
                // Usually Average is "Total / Number of days with data" or "Total / 7"
                // Let's stick to "days with data" to avoid diluting average with future/unknowns, 
                // OR "days in range" for a strict period average.
                // Given the user wants 0s to appear, "Total / 7" is probably more honest for "Weekly Average".
                // But let's keep consistency with previous logic: count of entries found.
                // If 0s are explicitly added to chart, maybe average should be over rangeDays?
                // Let's use filteredHistory.size for now as it represents 'active days'.
                // Update: User asked for 0s in chart. This implies empty days ARE part of the dataset.
                // So Average should probably be Total / rangeDays.
                val daysDivider = rangeDays // filteredHistory.size.coerceAtLeast(1)
                
                val avgSteps = totalSteps / daysDivider
                val bestDay = filteredHistory.maxOfOrNull { it.steps } ?: 0
                val totalCal = filteredHistory.sumOf { it.caloriesBurned }
                val avgCal = totalCal / daysDivider
                val totalWater = filteredHistory.sumOf { it.waterMl }

                val goalsMet = filteredHistory.count { it.steps >= stepGoal }
                val completionRate = if (daysDivider > 0) (goalsMet * 100 / daysDivider) else 0

                StatisticsUiState(
                    selectedRange = range,
                    todayStats = todayDomain,
                    historyStats = filteredHistory.sortedByDescending { it.date }, 
                    detailedStats = DetailedStat(
                        totalSteps = totalSteps,
                        avgSteps = avgSteps,
                        bestDaySteps = bestDay,
                        totalCaloriesBurned = totalCal,
                        avgCaloriesBurned = avgCal,
                        totalWater = totalWater,
                        completionRate = completionRate
                    ),
                    stepGoal = stepGoal,
                    waterGoal = waterGoal,
                    calorieGoal = calorieGoal,
                    chartData = chartDataPoints,
                    chartLabels = chartLabelsList,
                    isLoading = false,
                    lastAddedWater = _uiState.value.lastAddedWater
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            healthRepository.addWater(amountMl)
            _uiState.update { it.copy(lastAddedWater = amountMl) }
        }
    }
    
    fun removeWater(amountMl: Int) {
        viewModelScope.launch {
            healthRepository.removeWater(amountMl)
            _uiState.update { it.copy(lastAddedWater = null) }
        }
    }

    private fun getDayLabel(dayOfWeek: java.time.DayOfWeek): String {
        val resId = when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> R.string.chart_day_mon
            java.time.DayOfWeek.TUESDAY -> R.string.chart_day_tue
            java.time.DayOfWeek.WEDNESDAY -> R.string.chart_day_wed
            java.time.DayOfWeek.THURSDAY -> R.string.chart_day_thu
            java.time.DayOfWeek.FRIDAY -> R.string.chart_day_fri
            java.time.DayOfWeek.SATURDAY -> R.string.chart_day_sat
            java.time.DayOfWeek.SUNDAY -> R.string.chart_day_sun
        }
        return context.getString(resId)
    }

}
