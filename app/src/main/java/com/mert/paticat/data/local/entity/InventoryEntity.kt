package com.mert.paticat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing the player's food inventory.
 * Each row represents a food item type and its quantity.
 */
@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey
    val foodItemId: String,
    val quantity: Int = 0
)
