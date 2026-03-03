package com.mert.paticat.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for logging every cat interaction (feeding, games, sleeping, petting).
 */
@Entity(
    tableName = "cat_interactions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["type"])
    ]
)
data class CatInteractionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String,           // Format: yyyy-MM-dd
    val type: String,           // InteractionType name: FEED, GAME_RPS, GAME_SLOTS, GAME_MEMORY, GAME_REFLEX, SLEEP, PET
    val foodItemId: String? = null,  // Only for FEED type — ShopItem id
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null // Extra info — e.g. game result: WIN, LOSE, DRAW
)
