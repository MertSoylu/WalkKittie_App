package com.mert.paticat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mert.paticat.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for InventoryEntity operations.
 */
@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory")
    fun getAll(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE foodItemId = :foodItemId")
    suspend fun getItem(foodItemId: String): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InventoryEntity)

    @Query("UPDATE inventory SET quantity = quantity - 1 WHERE foodItemId = :foodItemId AND quantity > 0")
    suspend fun decreaseQuantity(foodItemId: String)
}
