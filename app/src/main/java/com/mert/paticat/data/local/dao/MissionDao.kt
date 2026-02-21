package com.mert.paticat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mert.paticat.data.local.entity.MissionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MissionEntity operations.
 */
@Dao
interface MissionDao {
    
    @Query("SELECT * FROM missions WHERE date = :date")
    fun getMissionsForDate(date: String): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE date = :date")
    suspend fun getMissionsForDateOnce(date: String): List<MissionEntity>
    
    @Query("SELECT * FROM missions WHERE date = :date AND isCompleted = 0")
    fun getActiveMissionsForDate(date: String): Flow<List<MissionEntity>>
    
    @Query("SELECT * FROM missions WHERE date = :date AND isCompleted = 1")
    fun getCompletedMissionsForDate(date: String): Flow<List<MissionEntity>>
    
    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun getMissionById(id: String): MissionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<MissionEntity>)
    
    @Update
    suspend fun updateMission(mission: MissionEntity)
    
    @Query("UPDATE missions SET currentValue = :currentValue WHERE id = :id")
    suspend fun updateMissionProgress(id: String, currentValue: Int)
    
    @Query("UPDATE missions SET isCompleted = 1 WHERE id = :id")
    suspend fun completeMission(id: String)
    
    @Query("DELETE FROM missions WHERE date < :date")
    suspend fun deleteOldMissions(date: String)

    @Query("DELETE FROM missions WHERE date = :date")
    suspend fun deleteMissionsForDate(date: String)
}
