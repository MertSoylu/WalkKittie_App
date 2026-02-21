package com.mert.paticat.data.local

import com.mert.paticat.data.local.entity.CatEntity
import com.mert.paticat.data.local.entity.DailyStatsEntity
import com.mert.paticat.data.local.entity.MissionEntity
import com.mert.paticat.data.local.entity.UserProfileEntity
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.model.DailyStats
import com.mert.paticat.domain.model.Mission
import com.mert.paticat.domain.model.MissionType
import com.mert.paticat.domain.model.UserProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

// Date helpers
fun LocalDate.toDbString(): String = this.format(dateFormatter)
fun String.toLocalDate(): LocalDate = LocalDate.parse(this, dateFormatter)

// Cat Mappers
fun CatEntity.toDomain(): Cat {
    return Cat(
        id = id,
        name = name,
        level = level,
        xp = xp,
        hunger = hunger,
        happiness = happiness,
        energy = energy,
        foodPoints = foodPoints,
        coins = coins,
        isSleeping = isSleeping,
        sleepEndTime = sleepEndTime,
        lastUpdated = lastUpdated,
        lastInteractionTime = lastInteractionTime
    )
}

fun Cat.toEntity(): CatEntity {
    return CatEntity(
        id = id,
        name = name,
        level = level,
        xp = xp,
        hunger = hunger,
        happiness = happiness,
        energy = energy,
        foodPoints = foodPoints,
        coins = coins,
        isSleeping = isSleeping,
        sleepEndTime = sleepEndTime,
        lastUpdated = lastUpdated,
        lastInteractionTime = lastInteractionTime
    )
}

// DailyStats Mappers
fun DailyStatsEntity.toDomain(): DailyStats {
    return DailyStats(
        date = date.toLocalDate(),
        steps = steps,
        caloriesBurned = caloriesBurned,
        caloriesConsumed = caloriesConsumed,
        waterMl = waterMl,
        activeMinutes = activeMinutes
    )
}

fun DailyStats.toEntity(): DailyStatsEntity {
    return DailyStatsEntity(
        date = date.toDbString(),
        steps = steps,
        caloriesBurned = caloriesBurned,
        caloriesConsumed = caloriesConsumed,
        waterMl = waterMl,
        distanceKm = distanceKm,
        activeMinutes = activeMinutes,
        lastSyncTime = System.currentTimeMillis()
    )
}

// Mission Mappers
fun MissionEntity.toDomain(): Mission {
    return Mission(
        id = id,
        title = title,
        description = description,
        type = try { MissionType.valueOf(type) } catch (e: Exception) { MissionType.STEPS },
        targetValue = targetValue,
        currentValue = currentValue,
        xpReward = xpReward,
        foodPointReward = foodPointReward,
        coinReward = coinReward,
        isCompleted = isCompleted,
        date = date.toLocalDate()
    )
}

fun Mission.toEntity(): MissionEntity {
    return MissionEntity(
        id = id,
        title = title,
        description = description,
        type = type.name,
        targetValue = targetValue,
        currentValue = currentValue,
        xpReward = xpReward,
        foodPointReward = foodPointReward,
        coinReward = coinReward,
        isCompleted = isCompleted,
        date = date.toDbString()
    )
}

// UserProfile Mappers
fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        name = name,
        gender = gender,
        dailyStepGoal = dailyStepGoal,
        dailyWaterGoalMl = dailyWaterGoalMl,
        dailyCalorieGoal = dailyCalorieGoal,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        totalXpEarned = totalXpEarned
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        name = name,
        gender = gender,
        dailyStepGoal = dailyStepGoal,
        dailyWaterGoalMl = dailyWaterGoalMl,
        dailyCalorieGoal = dailyCalorieGoal,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        totalXpEarned = totalXpEarned
    )
}
