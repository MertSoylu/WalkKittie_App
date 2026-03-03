package com.mert.paticat.domain.repository

import com.mert.paticat.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user profile operations.
 * Encapsulates all UserProfile data access so ViewModels never touch DAOs directly.
 */
interface UserProfileRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun getUserProfileOnce(): UserProfile?
    suspend fun initializeProfileIfNeeded()
    suspend fun updateProfile(profile: UserProfile)
    suspend fun updateStepGoal(goal: Int)
    suspend fun updateWaterGoal(goal: Int)
    suspend fun updateCalorieGoal(goal: Int)
}
