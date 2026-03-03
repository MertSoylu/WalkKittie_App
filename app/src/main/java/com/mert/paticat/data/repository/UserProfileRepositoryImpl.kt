package com.mert.paticat.data.repository

import com.mert.paticat.data.local.dao.UserProfileDao
import com.mert.paticat.data.local.entity.UserProfileEntity
import com.mert.paticat.data.local.toDomain
import com.mert.paticat.domain.model.UserProfile
import com.mert.paticat.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [UserProfileRepository].
 * Wraps UserProfileDao to keep DAOs out of ViewModels.
 */
@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    override fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getUserProfileOnce(): UserProfile? {
        return userProfileDao.getUserProfileOnce()?.toDomain()
    }

    override suspend fun initializeProfileIfNeeded() {
        val existing = userProfileDao.getUserProfileOnce()
        if (existing == null) {
            userProfileDao.insertProfile(UserProfileEntity())
        }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.updateProfile(
            UserProfileEntity(
                id = profile.id,
                name = profile.name,
                gender = profile.gender,
                weight = profile.weight,
                dailyStepGoal = profile.dailyStepGoal,
                dailyWaterGoalMl = profile.dailyWaterGoalMl,
                dailyCalorieGoal = profile.dailyCalorieGoal,
                currentStreak = profile.currentStreak,
                longestStreak = profile.longestStreak,
                totalXpEarned = profile.totalXpEarned
            )
        )
    }

    override suspend fun updateStepGoal(goal: Int) {
        userProfileDao.updateStepGoal(goal)
    }

    override suspend fun updateWaterGoal(goal: Int) {
        userProfileDao.updateWaterGoal(goal)
    }

    override suspend fun updateCalorieGoal(goal: Int) {
        val current = userProfileDao.getUserProfileOnce()
        current?.let {
            userProfileDao.updateProfile(it.copy(dailyCalorieGoal = goal))
        }
    }
}
