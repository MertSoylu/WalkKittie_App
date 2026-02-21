package com.mert.paticat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mert.paticat.data.local.entity.ReminderSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Reminder Settings.
 */
@Dao
interface ReminderDao {
    
    @Query("SELECT * FROM reminder_settings WHERE id = 1")
    fun getSettings(): Flow<ReminderSettingsEntity?>
    
    @Query("SELECT * FROM reminder_settings WHERE id = 1")
    suspend fun getSettingsOnce(): ReminderSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: ReminderSettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: ReminderSettingsEntity)
    
    @Query("UPDATE reminder_settings SET waterReminderEnabled = :enabled WHERE id = 1")
    suspend fun updateWaterReminderEnabled(enabled: Boolean)
    
    @Query("UPDATE reminder_settings SET waterReminderIntervalMinutes = :interval WHERE id = 1")
    suspend fun updateWaterReminderInterval(interval: Int)
    
    @Query("UPDATE reminder_settings SET stepReminderEnabled = :enabled WHERE id = 1")
    suspend fun updateStepReminderEnabled(enabled: Boolean)
}
