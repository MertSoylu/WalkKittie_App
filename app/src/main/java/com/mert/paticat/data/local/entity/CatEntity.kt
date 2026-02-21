package com.mert.paticat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing cat state in the database.
 */
@Entity(tableName = "cat_state")
data class CatEntity(
    @PrimaryKey
    val id: Long = 1L,
    val name: String = "Mochi",
    val hunger: Int = 50,      // Initial: 50%
    val happiness: Int = 50,   // Initial: 50%
    val energy: Int = 50,      // Initial: 50%
    val xp: Long = 0,
    val level: Int = 1,
    val foodPoints: Int = 30,  // Initial: 30 MP
    val coins: Int = 0,
    val isSleeping: Boolean = false,
    val sleepEndTime: Long = 0L,
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastInteractionTime: Long = System.currentTimeMillis()
)
