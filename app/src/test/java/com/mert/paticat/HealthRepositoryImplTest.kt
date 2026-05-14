package com.mert.paticat

import com.mert.paticat.data.local.dao.DailyStatsDao
import com.mert.paticat.data.local.entity.DailyStatsEntity
import com.mert.paticat.data.repository.HealthRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class HealthRepositoryImplTest {

    private lateinit var dao: DailyStatsDao
    private lateinit var repository: HealthRepositoryImpl

    @Before
    fun setup() {
        dao = mock()
        repository = HealthRepositoryImpl(dao)
    }

    @Test
    fun `addWater inserts new entity when no record exists`() = runTest {
        whenever(dao.getStatsForDateOnce(any())).thenReturn(null)

        repository.addWater(300)

        val captor = argumentCaptor<DailyStatsEntity>()
        verify(dao).insertDailyStats(captor.capture())
        assertEquals(300, captor.firstValue.waterMl)
    }

    @Test
    fun `addWater accumulates on top of existing water`() = runTest {
        val today = LocalDate.now().toString()
        whenever(dao.getStatsForDateOnce(any())).thenReturn(
            DailyStatsEntity(date = today, waterMl = 500)
        )

        repository.addWater(300)

        verify(dao).updateWater(any(), eq(800))
    }

    @Test
    fun `removeWater clamps result at zero`() = runTest {
        val today = LocalDate.now().toString()
        whenever(dao.getStatsForDateOnce(any())).thenReturn(
            DailyStatsEntity(date = today, waterMl = 100)
        )

        repository.removeWater(200)

        verify(dao).updateWater(any(), eq(0))
    }
}
