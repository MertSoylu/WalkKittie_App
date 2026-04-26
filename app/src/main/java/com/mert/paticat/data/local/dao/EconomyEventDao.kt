package com.mert.paticat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mert.paticat.data.local.entity.EconomyEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EconomyEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: EconomyEventEntity)

    @Query("SELECT * FROM economy_events WHERE date = :date ORDER BY timestamp DESC LIMIT :limit")
    fun getEventsForDate(date: String, limit: Int = 100): Flow<List<EconomyEventEntity>>

    @Query("SELECT COALESCE(SUM(delta), 0) FROM economy_events WHERE date = :date")
    suspend fun getNetDeltaForDate(date: String): Int
}
