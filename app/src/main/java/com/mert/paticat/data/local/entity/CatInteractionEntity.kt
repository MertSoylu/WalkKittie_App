package com.mert.paticat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
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
    ],
    foreignKeys = [
        ForeignKey(
            entity = CatEntity::class,
            parentColumns = ["id"],
            childColumns = ["catId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CatInteractionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val catId: Long = 1L,               // Foreign key to CatEntity (always 1)
    val date: String,                   // Format: yyyy-MM-dd
    val type: String,                   // InteractionType name: FEED, GAME_RPS, GAME_SLOTS, GAME_MEMORY, GAME_REFLEX, GAME_CATCH, SLEEP, PET
    val foodItemId: String? = null,     // Only for FEED type — ShopItem id
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null         // Extra info — e.g. game result: WIN, LOSE, DRAW
)
