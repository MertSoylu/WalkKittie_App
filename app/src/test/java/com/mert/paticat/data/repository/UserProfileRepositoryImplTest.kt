package com.mert.paticat.data.repository

import com.mert.paticat.data.local.dao.UserProfileDao
import com.mert.paticat.data.local.entity.UserProfileEntity
import com.mert.paticat.domain.model.UserProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserProfileRepositoryImplTest {

    private lateinit var userProfileDao: UserProfileDao
    private lateinit var repository: UserProfileRepositoryImpl

    @Before
    fun setup() {
        userProfileDao = mock()
        repository = UserProfileRepositoryImpl(userProfileDao)
    }

    @Test
    fun `upsertProfile inserts profile when onboarding creates first profile`() = runTest {
        repository.upsertProfile(
            UserProfile(
                name = "Mert",
                gender = "MALE",
                dailyStepGoal = 6000,
                dailyWaterGoalMl = 2000,
                dailyCalorieGoal = 2000
            )
        )

        argumentCaptor<UserProfileEntity>().apply {
            verify(userProfileDao).insertProfile(capture())
            assertEquals(1L, firstValue.id)
            assertEquals("Mert", firstValue.name)
            assertEquals("MALE", firstValue.gender)
            assertEquals(6000, firstValue.dailyStepGoal)
        }
    }
}
