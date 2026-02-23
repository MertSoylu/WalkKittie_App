package com.mert.paticat.domain.repository

import com.mert.paticat.domain.model.Mission
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for mission operations.
 */
interface MissionRepository {
    fun getTodayMissions(): Flow<List<Mission>>
    fun getActiveMissions(): Flow<List<Mission>>
    fun getCompletedMissions(): Flow<List<Mission>>
    
    suspend fun generateDailyMissions()
    suspend fun updateMissionProgress(missionId: String, progress: Int)
    suspend fun completeMission(missionId: String)
    suspend fun checkAndCompleteMissions(steps: Int? = null, waterMl: Int? = null)
}
