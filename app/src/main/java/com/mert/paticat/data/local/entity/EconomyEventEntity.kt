package com.mert.paticat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local ledger record for coin economy operations.
 */
@Entity(tableName = "economy_events")
data class EconomyEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String,
    val timestamp: Long,
    val source: String,
    val delta: Int,
    val balanceBefore: Int,
    val balanceAfter: Int,
    val note: String? = null
)
