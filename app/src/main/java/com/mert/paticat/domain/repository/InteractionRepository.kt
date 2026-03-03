package com.mert.paticat.domain.repository

import com.mert.paticat.domain.model.CatInteraction
import com.mert.paticat.domain.model.DailyInteractionCount
import com.mert.paticat.domain.model.InteractionSummary
import com.mert.paticat.domain.model.InteractionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface InteractionRepository {
    /**
     * Log a cat interaction event.
     */
    suspend fun logInteraction(
        type: InteractionType,
        foodItemId: String? = null,
        details: String? = null
    )

    /**
     * Get today's interaction summary.
     */
    fun getTodaySummary(): Flow<InteractionSummary>

    /**
     * Get interaction summary for a date range.
     */
    fun getSummaryForRange(startDate: LocalDate, endDate: LocalDate): Flow<InteractionSummary>

    /**
     * Get daily interaction counts for chart data.
     */
    fun getDailyInteractionCounts(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyInteractionCount>>

    /**
     * Get all interactions for a specific date.
     */
    fun getInteractionsForDate(date: LocalDate): Flow<List<CatInteraction>>
}

