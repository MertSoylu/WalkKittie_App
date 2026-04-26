package com.mert.paticat

import android.content.Context
import android.content.SharedPreferences
import com.mert.paticat.data.local.dao.DailyStatsDao
import com.mert.paticat.data.local.dao.UserProfileDao
import com.mert.paticat.domain.model.EconomySource
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.MissionRepository
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class StepCounterServiceTest {

    private lateinit var service: StepCounterService
    private lateinit var dailyStatsDao: DailyStatsDao
    private lateinit var catRepository: CatRepository
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var missionRepository: MissionRepository
    private lateinit var stepCounterManager: StepCounterManager
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var context: Context
    private lateinit var sharedPrefs: SharedPreferences

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        dailyStatsDao = mock()
        catRepository = mock()
        userProfileDao = mock()
        missionRepository = mock()
        stepCounterManager = mock()
        userPreferencesRepository = mock()
        context = mock()
        sharedPrefs = mock()

        val editor = mock<SharedPreferences.Editor>()
        whenever(editor.putInt(any(), any())).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(sharedPrefs.edit()).thenReturn(editor)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPrefs)

        service = spy(StepCounterService())
        doReturn(sharedPrefs).whenever(service).getSharedPreferences(any(), any())

        // Android Context methods
        doReturn(mock<android.hardware.SensorManager>()).whenever(service).getSystemService(Context.SENSOR_SERVICE)
        // Mock getSystemService(Class) if needed
        doReturn(mock<android.app.NotificationManager>()).whenever(service).getSystemService(eq(android.app.NotificationManager::class.java))
        doReturn("mock").whenever(service).getString(any())
        service.dailyStatsDao = dailyStatsDao
        service.catRepository = catRepository
        service.userProfileDao = userProfileDao
        service.missionRepository = missionRepository
        service.stepCounterManager = stepCounterManager
        service.userPreferencesRepository = userPreferencesRepository

        val scopeField = StepCounterService::class.java.getDeclaredField("serviceScope")
        scopeField.isAccessible = true
        scopeField.set(service, kotlinx.coroutines.CoroutineScope(testDispatcher))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun invokeSyncToDatabase(steps: Int, lastProcessed: Int) {
        val stepsField = StepCounterService::class.java.getDeclaredField("currentDaySteps")
        stepsField.isAccessible = true
        stepsField.set(service, steps)

        val lastProcessedField = StepCounterService::class.java.getDeclaredField("lastProcessedStepsForRewards")
        lastProcessedField.isAccessible = true
        lastProcessedField.set(service, lastProcessed)

        val syncMethod = StepCounterService::class.java.getDeclaredMethod("syncToDatabase", String::class.java)
        syncMethod.isAccessible = true
        syncMethod.invoke(service, "2023-11-01")
    }

    @Test
    fun `reward calculation grants correct coins and xp based on step diff`() = runTest {
        // Given
        whenever(userPreferencesRepository.getComboBoostExpiry()).thenReturn(0L)
        whenever(userPreferencesRepository.getStepBoostExpiry()).thenReturn(0L)
        whenever(userPreferencesRepository.getXpBoostExpiry()).thenReturn(0L)

        // When
        invokeSyncToDatabase(steps = 250, lastProcessed = 0)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then stepdiff = 250 -> 1.66 points, grants 1 point. remainder is 100.
        verify(catRepository, times(1)).addCoins(1, EconomySource.STEP_REWARD, null)
        verify(catRepository, times(1)).addXp(1)
        verify(userPreferencesRepository, times(1)).addPendingRewards(1, 1)
    }

    @Test
    fun `boost multipliers are correctly applied`() = runTest {
        // Given
        val now = System.currentTimeMillis()
        whenever(userPreferencesRepository.getComboBoostExpiry()).thenReturn(0L)
        // Step boost active -> gold x2
        whenever(userPreferencesRepository.getStepBoostExpiry()).thenReturn(now + 100000L)
        whenever(userPreferencesRepository.getXpBoostExpiry()).thenReturn(0L)

        // When
        invokeSyncToDatabase(steps = 300, lastProcessed = 100)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then stepdiff = 200 -> 1 base point. Gold is boosted (x2) = 2. XP is not = 1.
        verify(catRepository, times(1)).addCoins(2, EconomySource.STEP_REWARD, null)
        verify(catRepository, times(1)).addXp(1)
    }
}
