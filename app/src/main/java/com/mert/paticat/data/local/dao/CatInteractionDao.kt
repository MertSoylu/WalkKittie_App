package com.mert.paticat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mert.paticat.data.local.entity.CatInteractionEntity
import kotlinx.coroutines.flow.Flow

data class DailyInteractionCount(
    val date: String,
    val type: String,
    val count: Int
)

@Dao
interface CatInteractionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(entity: CatInteractionEntity)

    @Query("SELECT * FROM cat_interactions WHERE date = :date ORDER BY timestamp DESC")
    fun getInteractionsForDate(date: String): Flow<List<CatInteractionEntity>>

    @Query("SELECT * FROM cat_interactions WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getInteractionsInRange(startDate: String, endDate: String): Flow<List<CatInteractionEntity>>

    @Query("SELECT COUNT(*) FROM cat_interactions WHERE date = :date AND type = :type")
    suspend fun getCountByTypeForDate(date: String, type: String): Int

    @Query("SELECT COUNT(*) FROM cat_interactions WHERE date BETWEEN :startDate AND :endDate AND type = :type")
    suspend fun getCountByTypeInRange(startDate: String, endDate: String, type: String): Int

    @Query("SELECT date, type, COUNT(*) as count FROM cat_interactions WHERE date BETWEEN :startDate AND :endDate GROUP BY date, type ORDER BY date ASC")
    fun getDailyInteractionCounts(startDate: String, endDate: String): Flow<List<DailyInteractionCount>>

    @Query("SELECT COUNT(*) FROM cat_interactions WHERE date = :date")
    suspend fun getTotalCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM cat_interactions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalCountInRange(startDate: String, endDate: String): Int
}
