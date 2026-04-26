package com.mert.paticat.data.repository

import android.content.SharedPreferences
import com.mert.paticat.data.local.dao.CatDao
import com.mert.paticat.data.local.dao.CatInteractionDao
import com.mert.paticat.data.local.dao.EconomyEventDao
import com.mert.paticat.data.local.dao.MissionDao
import com.mert.paticat.data.local.entity.CatEntity
import com.mert.paticat.data.local.entity.EconomyEventEntity
import com.mert.paticat.domain.model.EconomyConfig
import com.mert.paticat.domain.model.EconomySource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit

class CatRepositoryImplTest {

    private lateinit var catDao: CatDao
    private lateinit var interactionDao: CatInteractionDao
    private lateinit var economyEventDao: EconomyEventDao
    private lateinit var context: android.content.Context
    private lateinit var petPrefs: SharedPreferences
    private lateinit var repository: CatRepositoryImpl

    @Before
    fun setup() {
        catDao = mock()
        interactionDao = mock()
        economyEventDao = mock()
        context = mock()
        petPrefs = mock()
        whenever(context.getSharedPreferences(any(), any())).thenReturn(petPrefs)

        repository = CatRepositoryImpl(
            catDao = catDao,
            catInteractionDao = interactionDao,
            economyEventDao = economyEventDao,
            context = context
        )
    }

    @Test
    fun `decreaseHungerOverTime applies decay correctly when awake`() = runTest {
        // Given
        val initialTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2) // 2 hours passed
        val initialCat = CatEntity(
            id = 1,
            name = "Test",
            hunger = 100,
            energy = 100,
            happiness = 100,
            level = 1,
            xp = 0,
            lastUpdated = initialTime,
            isSleeping = false
        )
        whenever(catDao.getCatOnce()).thenReturn(initialCat)

        // When
        repository.decreaseHungerOverTime()

        // Then
        // 2 hours awake decay:
        // Hunger: -8 * 2 = -16 -> 84
        // Energy: -2 * 2 = -4 -> 96
        // Happiness: Good condition (+1/hr) -> Max is 100

        argumentCaptor<CatEntity>().apply {
            verify(catDao).updateCat(capture())
            val updated = firstValue
            assertEquals(84, updated.hunger)
            assertEquals(96, updated.energy)
            assertEquals(100, updated.happiness)
            assertEquals(false, updated.isSleeping)
        }
    }

    @Test
    fun `decreaseHungerOverTime applies decay correctly when sleeping`() = runTest {
        // Given
        val initialTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3) // 3 hours passed
        val initialCat = CatEntity(
            id = 1,
            name = "Test",
            hunger = 50,
            energy = 20,
            happiness = 50,
            level = 1,
            xp = 0,
            lastUpdated = initialTime,
            isSleeping = true,
            sleepEndTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1) // Still sleeping
        )
        whenever(catDao.getCatOnce()).thenReturn(initialCat)

        // When
        repository.decreaseHungerOverTime()

        // Then
        // 3 hours sleeping decay:
        // Hunger: -3 * 3 = -9 -> 41
        // Energy: +25 * 3 = 95
        // Happiness: 41 hunger is > 30, so standard sleep happiness (+3/hr) -> 50 + 9 = 59

        argumentCaptor<CatEntity>().apply {
            verify(catDao).updateCat(capture())
            val updated = firstValue
            assertEquals(41, updated.hunger)
            assertEquals(95, updated.energy)
            assertEquals(59, updated.happiness)
            assertEquals(true, updated.isSleeping)
        }
    }

    @Test
    fun `markUserInteraction calculates mixed sleep and awake segments`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val lastUpdated = currentTime - TimeUnit.HOURS.toMillis(5) // 5 hours total
        val sleepEndTime = currentTime - TimeUnit.HOURS.toMillis(2) // Woke up 2 hours ago
        // Therefore, 3 hours sleeping, 2 hours awake.

        val initialCat = CatEntity(
            id = 1,
            name = "Test",
            hunger = 80,
            energy = 10,
            happiness = 50,
            level = 1,
            xp = 0,
            lastUpdated = lastUpdated,
            isSleeping = true,
            sleepEndTime = sleepEndTime
        )
        whenever(catDao.getCatOnce()).thenReturn(initialCat)

        // When
        repository.markUserInteraction()

        // Then
        // Segment 1 (Sleep 3 hours):
        // Hunger = 80 - 9 = 71
        // Energy = 10 + 75 = 85
        // Happiness = standard sleep (+3/hr) -> 50 + 9 = 59
        // After wake up event: XP += 5, isSleeping = false, sleepEndTime = 0

        // Segment 2 (Awake 2 hours):
        // Start from (Hunger 71, Energy 85, Happiness 59)
        // Hunger = 71 - 16 = 55
        // Energy = 85 - 4 = 81
        // Happiness = Not good state (hunger < 80), not critical -> base decay -4/hr * 2 = -8.
        // 59 - 8 = 51

        argumentCaptor<CatEntity>().apply {
            verify(catDao).updateCat(capture())
            val updated = firstValue
            assertEquals(55, updated.hunger)
            assertEquals(81, updated.energy)
            assertEquals(51, updated.happiness)
            assertEquals(false, updated.isSleeping)
            assertEquals(0L, updated.sleepEndTime)
            assertEquals(5L, updated.xp.toLong()) // Started at 0, +5 for wake up
        }
    }

    @Test
    fun `addCoins caps at max gold and writes economy event`() = runTest {
        val cat = CatEntity(coins = EconomyConfig.MAX_GOLD - 2)
        whenever(catDao.getCatOnce()).thenReturn(cat)

        repository.addCoins(amount = 10, source = EconomySource.STEP_REWARD, note = "test")

        verify(catDao).updateCoins(EconomyConfig.MAX_GOLD)
        argumentCaptor<EconomyEventEntity>().apply {
            verify(economyEventDao).insertEvent(capture())
            assertEquals(EconomySource.STEP_REWARD.name, firstValue.source)
            assertEquals(2, firstValue.delta)
            assertEquals(EconomyConfig.MAX_GOLD - 2, firstValue.balanceBefore)
            assertEquals(EconomyConfig.MAX_GOLD, firstValue.balanceAfter)
        }
    }

    @Test
    fun `addCoins never drops below zero`() = runTest {
        val cat = CatEntity(coins = 3)
        whenever(catDao.getCatOnce()).thenReturn(cat)

        repository.addCoins(amount = -20, source = EconomySource.SHOP_PURCHASE, note = "dry_food")

        verify(catDao).updateCoins(0)
    }
}
