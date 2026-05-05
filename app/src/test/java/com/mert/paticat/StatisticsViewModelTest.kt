package com.mert.paticat

import com.mert.paticat.data.ads.AdManager
import com.mert.paticat.data.local.dao.DailyStatsDao
import com.mert.paticat.data.local.entity.DailyStatsEntity
import com.mert.paticat.domain.model.DailyInteractionCount
import com.mert.paticat.domain.model.InteractionSummary
import com.mert.paticat.domain.model.UserProfile
import com.mert.paticat.domain.repository.HealthRepository
import com.mert.paticat.domain.repository.InteractionRepository
import com.mert.paticat.domain.repository.UserProfileRepository
import com.mert.paticat.ui.screens.statistics.StatisticsViewModel
import com.mert.paticat.ui.screens.statistics.StatsRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var dailyStatsDao: DailyStatsDao
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var healthRepository: HealthRepository
    private lateinit var interactionRepository: InteractionRepository
    private lateinit var adManager: AdManager
    private lateinit var stepCounterManager: StepCounterManager
    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dailyStatsDao = mock()
        userProfileRepository = mock()
        healthRepository = mock()
        interactionRepository = mock()
        adManager = mock()
        stepCounterManager = mock()
        context = mock()

        whenever(userProfileRepository.getUserProfile()).thenReturn(
            flowOf(UserProfile(dailyStepGoal = 10000))
        )
        whenever(dailyStatsDao.getStatsForDate(any())).thenReturn(flowOf(null))
        whenever(dailyStatsDao.getStatsInRange(any(), any())).thenReturn(flowOf(emptyList()))
        whenever(stepCounterManager.liveSteps).thenReturn(MutableStateFlow(0))
        whenever(adManager.nativeAd).thenReturn(MutableStateFlow(null))
        whenever(interactionRepository.getSummaryForRange(any<LocalDate>(), any<LocalDate>()))
            .thenReturn(flowOf(InteractionSummary()))
        whenever(interactionRepository.getDailyInteractionCounts(any<LocalDate>(), any<LocalDate>()))
            .thenReturn(flowOf(emptyList<DailyInteractionCount>()))

        // Stub context.getString — non-null label for chart day labels.
        whenever(context.getString(any<Int>())).thenReturn("Day")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel() = StatisticsViewModel(
        dailyStatsDao = dailyStatsDao,
        userProfileRepository = userProfileRepository,
        healthRepository = healthRepository,
        interactionRepository = interactionRepository,
        adManager = adManager,
        stepCounterManager = stepCounterManager,
        context = context
    )

    @Test
    fun `average uses active days not full range days`() = runTest {
        val today = LocalDate.now()
        val todayEntity = DailyStatsEntity(date = today.toString(), steps = 8000)
        // Only 2 days have steps; the other 5 days in the week are empty
        whenever(dailyStatsDao.getStatsForDate(any())).thenReturn(flowOf(todayEntity))
        whenever(dailyStatsDao.getStatsInRange(any(), any())).thenReturn(
            flowOf(
                listOf(
                    todayEntity,
                    DailyStatsEntity(date = today.minusDays(1).toString(), steps = 6000),
                )
            )
        )

        val vm = makeViewModel()
        advanceTimeBy(600L) // let debounce(500L) fire
        advanceUntilIdle()

        // total = 14000, active days = 2 → avg = 7000 (NOT 14000/7 = 2000)
        assertEquals(7000, vm.uiState.value.detailedStats.avgSteps)
    }

    @Test
    fun `monthly chart current month label ends with asterisk`() = runTest {
        val vm = makeViewModel()
        vm.selectRange(StatsRange.MONTHLY)
        advanceTimeBy(600L)
        advanceUntilIdle()

        val labels = vm.uiState.value.chartLabels
        assertEquals("Expected 6 monthly labels", 6, labels.size)
        assertTrue("Current month label must have MTD marker", labels.last().endsWith("*"))
    }

    @Test
    fun `range switch preserves error-free state`() = runTest {
        val vm = makeViewModel()
        advanceTimeBy(600L)
        advanceUntilIdle()

        vm.selectRange(StatsRange.MONTHLY)
        advanceTimeBy(600L)
        advanceUntilIdle()

        assertNull(vm.uiState.value.error)
        assertEquals(StatsRange.MONTHLY, vm.uiState.value.selectedRange)
    }

    @Test
    fun `addWater updates lastAddedWater`() = runTest {
        val vm = makeViewModel()
        advanceUntilIdle()

        vm.addWater(300)
        advanceUntilIdle()

        assertEquals(300, vm.uiState.value.lastAddedWater)
    }

    @Test
    fun `addWater coerces negative input to at least 1`() = runTest {
        val vm = makeViewModel()
        advanceUntilIdle()

        vm.addWater(-500)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.lastAddedWater)
    }

    @Test
    fun `clearError nulls the error state`() = runTest {
        val vm = makeViewModel()
        advanceUntilIdle()

        // Force error state
        val errorField = StatisticsViewModel::class.java.getDeclaredField("_uiState")
        errorField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = errorField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<com.mert.paticat.ui.screens.statistics.StatisticsUiState>
        flow.value = flow.value.copy(error = "test error")

        vm.clearError()
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `monthly range handles month boundary correctly`() = runTest {
        val vm = makeViewModel()
        vm.selectRange(StatsRange.MONTHLY)
        advanceTimeBy(600L)
        advanceUntilIdle()

        val labels = vm.uiState.value.chartLabels
        assertEquals(6, labels.size)
        // Last label should always be current month with MTD marker
        assertTrue(labels.last().endsWith("*"))
    }
}
