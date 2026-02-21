package com.mert.paticat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mert.paticat.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for UserProfileEntity operations.
 */
@Dao
interface UserProfileDao {
    
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>
    
    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)
    
    @Update
    suspend fun updateProfile(profile: UserProfileEntity)
    
    @Query("UPDATE user_profile SET currentStreak = :streak WHERE id = 1")
    suspend fun updateStreak(streak: Int)
    
    @Query("UPDATE user_profile SET longestStreak = :streak WHERE id = 1")
    suspend fun updateLongestStreak(streak: Int)
    
    @Query("UPDATE user_profile SET dailyStepGoal = :goal WHERE id = 1")
    suspend fun updateStepGoal(goal: Int)
    
    @Query("UPDATE user_profile SET dailyWaterGoalMl = :goal WHERE id = 1")
    suspend fun updateWaterGoal(goal: Int)
}
