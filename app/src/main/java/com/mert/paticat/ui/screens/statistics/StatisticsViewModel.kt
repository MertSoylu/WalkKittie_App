package com.mert.paticat.ui.screens.statistics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.R
import com.mert.paticat.data.local.toDbString
import com.mert.paticat.data.local.toDomain
import com.mert.paticat.data.local.dao.DailyStatsDao
import com.mert.paticat.domain.model.DailyStats
import com.mert.paticat.domain.model.InteractionSummary
import com.mert.paticat.domain.repository.HealthRepository
import com.mert.paticat.domain.repository.InteractionRepository
import com.mert.paticat.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

enum class StatsRange {
    DAILY,
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
    val completionRate: Int = 0,
)

data class StatisticsUiState(
    val selectedRange: StatsRange = StatsRange.WEEKLY,
    val todayStats: DailyStats = DailyStats(LocalDate.now()),
    val detailedStats: DetailedStat = DetailedStat(),
    val stepGoal: Int = 10000,
    val waterGoal: Int = 2000,
    val calorieGoal: Int = 2000,
    val chartData: List<Int> = emptyList(),
    val chartLabels: List<String> = emptyList(),
    val dateRangeLabel: String = "",
    val nativeAd: com.google.android.gms.ads.nativead.NativeAd? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val lastAddedWater: Int? = null,
    // Cat Care
    val catCareRange: StatsRange = StatsRange.DAILY,
    val catCareSummary: InteractionSummary = InteractionSummary(),
    val careChartData: List<Int> = emptyList(),
    val careChartLabels: List<String> = emptyList()
)

@OptIn(FlowPreview::class)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val dailyStatsDao: DailyStatsDao,
    private val userProfileRepository: UserProfileRepository,
    private val healthRepository: HealthRepository,
    private val interactionRepository: InteractionRepository,
    private val adManager: com.mert.paticat.data.ads.AdManager,
    private val stepCounterManager: com.mert.paticat.StepCounterManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        // Move side-effecting work off the constructor's call stack.
        viewModelScope.launch { loadData() }
        viewModelScope.launch { loadCatCareData() }
        viewModelScope.launch { observeAds() }
    }

    private fun observeAds() {
        viewModelScope.launch {
            adManager.nativeAd.collect { ad ->
                _uiState.update { it.copy(nativeAd = ad) }
            }
        }
    }

    private var loadJob: kotlinx.coroutines.Job? = null
    private var careJob: kotlinx.coroutines.Job? = null

    fun selectRange(range: StatsRange) {
        _uiState.update { it.copy(selectedRange = range, isLoading = true, error = null) }
        loadData()
    }

    fun selectCatCareRange(range: StatsRange) {
        // Clear chart data immediately — if the new load fails or is slow, stale
        // data from the previous range won't linger on screen.
        _uiState.update {
            it.copy(
                catCareRange = range,
                careChartData = emptyList(),
                careChartLabels = emptyList(),
            )
        }
        loadCatCareData()
    }

    private fun loadCatCareData() {
        careJob?.cancel()
        careJob = viewModelScope.launch {
            val today = LocalDate.now()
            val range = _uiState.value.catCareRange
            
            val startDate = when (range) {
                StatsRange.DAILY -> today
                StatsRange.WEEKLY -> today.minusDays(6)
                StatsRange.MONTHLY -> today.withDayOfMonth(1)
            }

            // Combine summary and chart data flows
            combine(
                interactionRepository.getSummaryForRange(startDate, today),
                interactionRepository.getDailyInteractionCounts(startDate, today)
            ) { summary, dailyCounts ->
                // Build chart data: total interactions per day
                val rangeDays = ChronoUnit.DAYS.between(startDate, today).toInt() + 1
                val chartDataPoints = mutableListOf<Int>()
                val chartLabelsList = mutableListOf<String>()

                for (i in 0 until rangeDays) {
                    val dateToCheck = startDate.plusDays(i.toLong())
                    val dateStr = dateToCheck.toDbString()
                    val dayTotal = dailyCounts.filter { it.date == dateStr }.sumOf { it.count }
                    chartDataPoints.add(dayTotal)
                    
                    val label = if (rangeDays <= 7)
                        getDayLabel(dateToCheck.dayOfWeek)
                    else
                        dateToCheck.dayOfMonth.toString()
                    chartLabelsList.add(label)
                }

                Triple(summary, chartDataPoints, chartLabelsList)
            }.collect { (summary, chartData, chartLabels) ->
                _uiState.update {
                    it.copy(
                        catCareSummary = summary,
                        careChartData = chartData,
                        careChartLabels = chartLabels
                    )
                }
            }
        }
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val todayStr = today.toDbString()
                val range = _uiState.value.selectedRange

                val startDate = when (range) {
                    StatsRange.DAILY, StatsRange.WEEKLY -> today.minusDays(6)
                    StatsRange.MONTHLY -> today.minusMonths(5).withDayOfMonth(1)
                }
                val startDateStr = startDate.toDbString()

                val profileFlow = userProfileRepository.getUserProfile()
                val todayStatsFlow = dailyStatsDao.getStatsForDate(todayStr)
                val recentStatsFlow = dailyStatsDao.getStatsInRange(startDateStr, todayStr)

                combine(profileFlow, todayStatsFlow, recentStatsFlow, stepCounterManager.liveSteps.debounce(500L).distinctUntilChanged()) { profile, todayStats, allRecentStats, liveSteps ->
                    val stepGoal = profile?.dailyStepGoal ?: 10000
                    val waterGoal = profile?.dailyWaterGoalMl ?: 2000
                    val calorieGoal = profile?.dailyCalorieGoal ?: 2000

                    var todayDomain = todayStats?.toDomain() ?: DailyStats(LocalDate.now())

                    if (todayDomain.date == LocalDate.now() && liveSteps > todayDomain.steps) {
                        todayDomain = todayDomain.copy(
                            steps = liveSteps,
                            distanceKm = (liveSteps * 0.75) / 1000.0
                        )
                    }

                    val historyDomain = allRecentStats.map {
                        val domain = it.toDomain()
                        if (domain.date == LocalDate.now()) todayDomain else domain
                    }.toMutableList()

                    if (historyDomain.none { it.date == LocalDate.now() }) {
                        historyDomain.add(0, todayDomain)
                    }

                    val rangeDays = ChronoUnit.DAYS.between(startDate, today).toInt() + 1

                    val filteredHistory = historyDomain.filter {
                        !it.date.isBefore(startDate) && !it.date.isAfter(today)
                    }

                    val chartDataPoints = mutableListOf<Int>()
                    val chartLabelsList = mutableListOf<String>()

                    if (range == StatsRange.MONTHLY) {
                        for (i in 5 downTo 0) {
                            val monthDate = today.minusMonths(i.toLong())
                            val monthStart = monthDate.withDayOfMonth(1)
                            val monthEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth())
                            val monthSteps = filteredHistory
                                .filter { !it.date.isBefore(monthStart) && !it.date.isAfter(monthEnd) }
                                .sumOf { it.steps }
                            chartDataPoints.add(monthSteps)
                            val monthLabel = monthDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            // Current month bar contains only MTD data — mark with asterisk
                            chartLabelsList.add(if (i == 0) "$monthLabel*" else monthLabel)
                        }
                    } else {
                        for (i in 0 until rangeDays) {
                            val dateToCheck = startDate.plusDays(i.toLong())
                            val statsForDay = filteredHistory.find { it.date == dateToCheck }
                            chartDataPoints.add(statsForDay?.steps ?: 0)
                            chartLabelsList.add(getDayLabel(dateToCheck.dayOfWeek))
                        }
                    }

                    val totalSteps = filteredHistory.sumOf { it.steps }
                    // Use rangeDays consistently for both avg and completion rate.
                    // Previously avgSteps used activeDays (days with steps > 0) while
                    // completionRate used rangeDays — denominators disagreed and made
                    // "average" sensitive to skipped days.
                    val safeDays = rangeDays.coerceAtLeast(1)

                    val avgSteps = totalSteps / safeDays
                    val bestDay = filteredHistory.maxOfOrNull { it.steps } ?: 0
                    val totalCal = filteredHistory.sumOf { it.caloriesBurned }
                    val avgCal = totalCal / safeDays
                    val totalWater = filteredHistory.sumOf { it.waterMl }

                    val goalsMet = filteredHistory.count { it.steps >= stepGoal }
                    val completionRate = (goalsMet * 100) / safeDays

                    val dateRangeLabel = when (range) {
                        StatsRange.DAILY, StatsRange.WEEKLY -> {
                            val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
                            "${startDate.format(fmt)} – ${today.format(fmt)}"
                        }
                        StatsRange.MONTHLY -> {
                            val fmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
                            "${startDate.format(fmt)} – ${today.format(fmt)}"
                        }
                    }

                    StatisticsUiState(
                        selectedRange = range,
                        todayStats = todayDomain,
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
                        dateRangeLabel = dateRangeLabel,
                        nativeAd = _uiState.value.nativeAd,
                        isLoading = false,
                        lastAddedWater = _uiState.value.lastAddedWater,
                        catCareRange = _uiState.value.catCareRange,
                        catCareSummary = _uiState.value.catCareSummary,
                        careChartData = _uiState.value.careChartData,
                        careChartLabels = _uiState.value.careChartLabels
                    )
                }.collect { newState ->
                    _uiState.update { newState }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addWater(amountMl: Int) {
        val sanitized = amountMl.coerceAtLeast(1)
        viewModelScope.launch {
            try {
                healthRepository.addWater(sanitized)
                _uiState.update { it.copy(lastAddedWater = sanitized, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun removeWater(amountMl: Int) {
        viewModelScope.launch {
            try {
                healthRepository.removeWater(amountMl)
                _uiState.update { it.copy(lastAddedWater = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
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
