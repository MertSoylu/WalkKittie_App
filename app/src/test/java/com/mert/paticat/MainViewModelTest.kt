package com.mert.paticat

import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.HealthRepository
import com.mert.paticat.domain.repository.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var catRepository: CatRepository
    private lateinit var healthRepository: HealthRepository

    private lateinit var pendingRewardXp: MutableStateFlow<Int>
    private lateinit var pendingRewardGold: MutableStateFlow<Int>
    private lateinit var stepBoostExpiresAt: MutableStateFlow<Long>
    private lateinit var xpBoostExpiresAt: MutableStateFlow<Long>
    private lateinit var comboBoostExpiresAt: MutableStateFlow<Long>

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        preferencesRepository = mock()
        userProfileRepository = mock()
        catRepository = mock()
        healthRepository = mock()

        pendingRewardXp = MutableStateFlow(0)
        pendingRewardGold = MutableStateFlow(0)
        stepBoostExpiresAt = MutableStateFlow(0L)
        xpBoostExpiresAt = MutableStateFlow(0L)
        comboBoostExpiresAt = MutableStateFlow(0L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun createViewModel(): MainViewModel {
        whenever(preferencesRepository.isLanguageSelected).thenReturn(flowOf(true))
        whenever(preferencesRepository.isOnboardingCompleted).thenReturn(flowOf(true))
        whenever(preferencesRepository.selectedTheme).thenReturn(flowOf("Standard"))
        whenever(preferencesRepository.isDarkMode).thenReturn(flowOf(false))
        whenever(preferencesRepository.lastSeenLevel).thenReturn(flowOf(1))
        whenever(preferencesRepository.pendingRewardXp).thenReturn(pendingRewardXp)
        whenever(preferencesRepository.pendingRewardGold).thenReturn(pendingRewardGold)
        whenever(preferencesRepository.stepBoostExpiresAt).thenReturn(stepBoostExpiresAt)
        whenever(preferencesRepository.xpBoostExpiresAt).thenReturn(xpBoostExpiresAt)
        whenever(preferencesRepository.comboBoostExpiresAt).thenReturn(comboBoostExpiresAt)

        whenever(catRepository.getCat()).thenReturn(flowOf(Cat()))
        whenever(userProfileRepository.getUserProfileOnce()).thenReturn(null)
        whenever(healthRepository.getStatsForDate(any())).thenReturn(flowOf(null))

        return MainViewModel(
            preferencesRepository = preferencesRepository,
            userProfileRepository = userProfileRepository,
            catRepository = catRepository,
            healthRepository = healthRepository
        )
    }

    @Test
    fun `visible reward notification updates when pending boosted rewards change`() = runTest {
        val viewModel = createViewModel()
        pendingRewardXp.value = 2
        pendingRewardGold.value = 2
        advanceUntilIdle()

        assertEquals(2, viewModel.rewardNotificationData.value?.xp)
        assertEquals(2, viewModel.rewardNotificationData.value?.gold)

        // Second notification goes to queue (ViewModel shows one at a time)
        pendingRewardXp.value = 4
        pendingRewardGold.value = 3
        advanceUntilIdle()

        assertEquals(2, viewModel.rewardNotificationData.value?.xp)
        assertEquals(2, viewModel.rewardNotificationData.value?.gold)
    }

    @Test
    fun `reward notification marks active gold and xp boosts`() = runTest {
        val viewModel = createViewModel()
        val future = System.currentTimeMillis() + 60_000L
        stepBoostExpiresAt.value = future
        xpBoostExpiresAt.value = future
        pendingRewardXp.value = 2
        pendingRewardGold.value = 2
        advanceUntilIdle()

        assertEquals(true, viewModel.rewardNotificationData.value?.isGoldBoosted)
        assertEquals(true, viewModel.rewardNotificationData.value?.isXpBoosted)
    }
}
