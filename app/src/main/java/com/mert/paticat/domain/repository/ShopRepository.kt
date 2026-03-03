package com.mert.paticat.domain.repository

import com.mert.paticat.domain.model.ShopItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for shop and inventory operations.
 */
interface ShopRepository {
    /** Returns a flow of inventory: map of ShopItem to quantity owned */
    fun getInventory(): Flow<Map<ShopItem, Int>>

    /** Buy a food item with gold. Returns true if purchase succeeded. */
    suspend fun buyFood(item: ShopItem): Boolean

    /** Feed the cat with an item from inventory. Returns true if feeding succeeded. */
    suspend fun feedCatWithItem(item: ShopItem): Boolean
}
