package com.mert.paticat.data.repository

import com.mert.paticat.data.local.dao.CatInteractionDao
import com.mert.paticat.data.local.dao.DailyInteractionCount
import com.mert.paticat.data.local.entity.CatInteractionEntity
import com.mert.paticat.data.local.toDbString
import com.mert.paticat.data.local.toLocalDate
import com.mert.paticat.domain.model.CatInteraction
import com.mert.paticat.domain.model.InteractionSummary
import com.mert.paticat.domain.model.InteractionType
import com.mert.paticat.domain.repository.InteractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionRepositoryImpl @Inject constructor(
    private val catInteractionDao: CatInteractionDao
) : InteractionRepository {

    override suspend fun logInteraction(
        type: InteractionType,
        foodItemId: String?,
        details: String?
    ) {
        val now = System.currentTimeMillis()
        val entity = CatInteractionEntity(
            date = LocalDate.now().toDbString(),
            type = type.name,
            foodItemId = foodItemId,
            timestamp = now,
            details = details
        )
        catInteractionDao.insertInteraction(entity)
    }

    override fun getTodaySummary(): Flow<InteractionSummary> {
        val todayStr = LocalDate.now().toDbString()
        return catInteractionDao.getInteractionsForDate(todayStr).map { list ->
            buildSummaryFromEntities(list)
        }
    }

    override fun getSummaryForRange(startDate: LocalDate, endDate: LocalDate): Flow<InteractionSummary> {
        return catInteractionDao.getInteractionsInRange(
            startDate.toDbString(),
            endDate.toDbString()
        ).map { list ->
            buildSummaryFromEntities(list)
        }
    }

    override fun getDailyInteractionCounts(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<com.mert.paticat.domain.model.DailyInteractionCount>> {
        return catInteractionDao.getDailyInteractionCounts(
            startDate.toDbString(),
            endDate.toDbString()
        ).map { list ->
            list.map { dao ->
                com.mert.paticat.domain.model.DailyInteractionCount(
                    date = dao.date,
                    type = dao.type,
                    count = dao.count
                )
            }
        }
    }

    override fun getInteractionsForDate(date: LocalDate): Flow<List<CatInteraction>> {
        return catInteractionDao.getInteractionsForDate(date.toDbString()).map { list ->
            list.map { it.toDomain() }
        }
    }

    private fun buildSummaryFromEntities(entities: List<CatInteractionEntity>): InteractionSummary {
        var feedCount = 0
        var petCount = 0
        var sleepCount = 0
        var gameRpsCount = 0
        var gameSlotsCount = 0
        var gameMemoryCount = 0
        var gameReflexCount = 0

        for (entity in entities) {
            when (entity.type) {
                InteractionType.FEED.name -> feedCount++
                InteractionType.PET.name -> petCount++
                InteractionType.SLEEP.name -> sleepCount++
                InteractionType.GAME_RPS.name -> gameRpsCount++
                InteractionType.GAME_SLOTS.name -> gameSlotsCount++
                InteractionType.GAME_MEMORY.name -> gameMemoryCount++
                InteractionType.GAME_REFLEX.name -> gameReflexCount++
            }
        }

        return InteractionSummary(
            feedCount = feedCount,
            petCount = petCount,
            sleepCount = sleepCount,
            gameRpsCount = gameRpsCount,
            gameSlotsCount = gameSlotsCount,
            gameMemoryCount = gameMemoryCount,
            gameReflexCount = gameReflexCount
        )
    }

    private fun CatInteractionEntity.toDomain(): CatInteraction {
        return CatInteraction(
            id = id,
            date = date.toLocalDate(),
            type = try { InteractionType.valueOf(type) } catch (e: Exception) { InteractionType.PET },
            foodItemId = foodItemId,
            timestamp = timestamp,
            details = details
        )
    }
}
