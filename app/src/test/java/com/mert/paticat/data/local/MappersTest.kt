package com.mert.paticat.data.local

import com.mert.paticat.data.local.entity.DailyStatsEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun `daily stats mapper preserves stored distance`() {
        val domain = DailyStatsEntity(
            date = "2026-04-29",
            steps = 1234,
            distanceKm = 1.23
        ).toDomain()

        assertEquals(1.23, domain.distanceKm, 0.001)
    }
}
