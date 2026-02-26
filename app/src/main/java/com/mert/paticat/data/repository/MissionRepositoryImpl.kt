package com.mert.paticat.data.repository

import com.mert.paticat.data.local.dao.MissionDao
import com.mert.paticat.data.local.dao.UserProfileDao
import com.mert.paticat.data.local.toDomain
import com.mert.paticat.data.local.toDbString
import com.mert.paticat.data.local.toEntity
import com.mert.paticat.domain.model.Mission
import com.mert.paticat.domain.model.MissionType
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.MissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Implementation of MissionRepository.
 * Handles mission generation based on user goals, tracking, and rewards.
 */
@Singleton
class MissionRepositoryImpl @Inject constructor(
    private val missionDao: MissionDao,
    private val catRepository: CatRepository,
    private val userProfileDao: UserProfileDao
) : MissionRepository {
    
    private val today: String get() = LocalDate.now().toDbString()
    
    override fun getTodayMissions(): Flow<List<Mission>> {
        return missionDao.getMissionsForDate(today).map { list ->
            list.map { it.toDomain() }
        }
    }
    
    override fun getActiveMissions(): Flow<List<Mission>> {
        return missionDao.getActiveMissionsForDate(today).map { list ->
            list.map { it.toDomain() }
        }
    }
    
    override fun getCompletedMissions(): Flow<List<Mission>> {
        return missionDao.getCompletedMissionsForDate(today).map { list ->
            list.map { it.toDomain() }
        }
    }
    
    override suspend fun generateDailyMissions() {
        // Load User Profile for Goals
        val profile = userProfileDao.getUserProfileOnce()
        val stepGoal = profile?.dailyStepGoal ?: 10000
        
        // Calculate milestones based on user goals: 25%, 50%, 75%, 100%
        val stepMilestone1 = (stepGoal * 0.25).roundToInt()
        val stepMilestone2 = (stepGoal * 0.50).roundToInt()
        val stepMilestone3 = (stepGoal * 0.75).roundToInt()
        val stepMilestone4 = stepGoal

        // Check if missions already exist and are valid for the current goal
        val existingMissions = missionDao.getMissionsForDateOnce(today)
        
        // Validation: Must be 4 missions AND the final goal must match user's current goal
        if (existingMissions.isNotEmpty()) {
            val tier4Mission = existingMissions.find { it.id.contains("tier4") }
            val isTargetValid = tier4Mission?.targetValue == stepGoal
            
            if (existingMissions.size == 4 && isTargetValid) {
                 return
            }
            // If targets don't match or count is wrong, we proceed to regenerate/update
        }
        
        // Generate missions dynamically
        val missions = listOf(
            // --- Step Missions ---
            Mission(
                id = "steps_tier1_${today}",
                title = "mission_steps_tier1_title", // "Adım Hedefi %25"
                description = "mission_steps_tier1_desc", // "$stepMilestone1 adım at"
                targetValue = stepMilestone1,
                xpReward = 10,
                foodPointReward = 10,
                type = MissionType.STEPS
            ),
            Mission(
                id = "steps_tier2_${today}",
                title = "mission_steps_tier2_title", // "Adım Hedefi %50"
                description = "mission_steps_tier2_desc",
                targetValue = stepMilestone2,
                xpReward = 20,
                foodPointReward = 20,
                type = MissionType.STEPS
            ),
            Mission(
                id = "steps_tier3_${today}",
                title = "mission_steps_tier3_title", // "Adım Hedefi %75"
                description = "mission_steps_tier3_desc", 
                targetValue = stepMilestone3,
                xpReward = 30,
                foodPointReward = 30,
                type = MissionType.STEPS
            ),
            Mission(
                id = "steps_tier4_${today}",
                title = "mission_steps_tier4_title", // "Adım Hedefi %100"
                description = "mission_steps_tier4_desc", 
                targetValue = stepMilestone4,
                xpReward = 50,
                foodPointReward = 50,
                type = MissionType.STEPS
            )
        )
        
        // Map to entities AND preserve current progress if missions existed
        val missionEntities = missions.map { newMission ->
            val oldMission = existingMissions.find { it.id == newMission.id }
            val currentVal = oldMission?.currentValue ?: 0
            // Re-check completion because targetValue might have changed
            val isCompleted = currentVal >= newMission.targetValue
            
            newMission.copy(
                currentValue = currentVal,
                isCompleted = isCompleted
            ).toEntity()
        }
        
        missionDao.insertMissions(missionEntities)
        
        // Clean up old missions (older than 7 days)
        val weekAgo = LocalDate.now().minusDays(7).toDbString()
        try {
            missionDao.deleteOldMissions(weekAgo)
        } catch (e: Exception) {
            // Ignore clean up errors
        }
    }
    
    override suspend fun updateMissionProgress(missionId: String, progress: Int) {
        val mission = missionDao.getMissionById(missionId) ?: return
        if (!mission.isCompleted) {
            missionDao.updateMissionProgress(missionId, progress)
            
            // Check if mission is now complete
            if (progress >= mission.targetValue) {
                completeMission(missionId)
            }
        }
    }
    
    override suspend fun completeMission(missionId: String) {
        val mission = missionDao.getMissionById(missionId) ?: return
        if (!mission.isCompleted) {
            missionDao.completeMission(missionId)
            
            // Award rewards
            catRepository.addXp(mission.xpReward)
            if (mission.foodPointReward > 0) {
                catRepository.addFoodPoints(mission.foodPointReward)
            }
            if (mission.coinReward > 0) {
                catRepository.addCoins(mission.coinReward)
            }
            
            // Increase cat happiness significantly for completing goals
            val happinessBoost = if (mission.type == MissionType.STEPS.name) 10 else 5
            // NOTE: mission.type is a String from entity; comparison is correct.
            catRepository.updateHappiness(happinessBoost)
        }
    }
    
    override suspend fun checkAndCompleteMissions(steps: Int?, waterMl: Int?) {
        val missionList = missionDao.getMissionsForDateOnce(today)
        
        missionList.forEach { mission ->
            if (!mission.isCompleted) {
                var currentProgress = mission.currentValue
                when (MissionType.valueOf(mission.type)) {
                    MissionType.STEPS -> if (steps != null) currentProgress = steps
                    MissionType.WATER -> if (waterMl != null) currentProgress = waterMl
                    else -> {}
                }
                
                if (currentProgress != mission.currentValue) {
                    updateMissionProgress(mission.id, currentProgress)
                }
            }
        }
    }
}
