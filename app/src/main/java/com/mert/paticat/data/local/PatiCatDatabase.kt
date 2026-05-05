package com.mert.paticat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mert.paticat.data.local.dao.*
import com.mert.paticat.data.local.entity.*

@Database(
    entities = [
        CatEntity::class,
        DailyStatsEntity::class,
        MissionEntity::class,
        UserProfileEntity::class,
        ReminderSettingsEntity::class,
        MealEntity::class,
        InventoryEntity::class,
        CatInteractionEntity::class,
        EconomyEventEntity::class
    ],
    version = 14, // Version 14: Fixed catId defaultValue mismatch in cat_interactions
    exportSchema = false
)
abstract class PatiCatDatabase : RoomDatabase() {

    abstract fun catDao(): CatDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun missionDao(): MissionDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun reminderDao(): ReminderDao
    abstract fun mealDao(): MealDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun catInteractionDao(): CatInteractionDao
    abstract fun economyEventDao(): EconomyEventDao

    companion object {
        const val DATABASE_NAME = "paticat_database"

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add lastInteractionTime column to cat_state table with current time as default
                val now = System.currentTimeMillis()
                database.execSQL("ALTER TABLE cat_state ADD COLUMN lastInteractionTime INTEGER NOT NULL DEFAULT $now")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create inventory table for shop system
                database.execSQL("CREATE TABLE IF NOT EXISTS inventory (foodItemId TEXT NOT NULL PRIMARY KEY, quantity INTEGER NOT NULL DEFAULT 0)")
                // Migrate existing foodPoints to coins (gold) without loss
                database.execSQL("UPDATE cat_state SET coins = coins + foodPoints WHERE foodPoints > 0")
                database.execSQL("UPDATE cat_state SET foodPoints = 0")
            }
        }

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create cat_interactions table for interaction tracking
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS cat_interactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "foodItemId TEXT, " +
                    "timestamp INTEGER NOT NULL, " +
                    "details TEXT" +
                    ")"
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cat_interactions_date ON cat_interactions(date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cat_interactions_type ON cat_interactions(type)")
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS economy_events (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "timestamp INTEGER NOT NULL, " +
                        "source TEXT NOT NULL, " +
                        "delta INTEGER NOT NULL, " +
                        "balanceBefore INTEGER NOT NULL, " +
                        "balanceAfter INTEGER NOT NULL, " +
                        "note TEXT" +
                    ")"
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_economy_events_date ON economy_events(date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_economy_events_source ON economy_events(source)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_economy_events_timestamp ON economy_events(timestamp)")
            }
        }

        // cat_interactions was created in MIGRATION_10_11 without catId/ForeignKey.
        // This migration recreates the table with the correct schema.
        val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE cat_interactions_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "catId INTEGER NOT NULL DEFAULT 1, " +
                        "date TEXT NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "foodItemId TEXT, " +
                        "timestamp INTEGER NOT NULL, " +
                        "details TEXT, " +
                        "FOREIGN KEY(catId) REFERENCES cat_state(id) ON DELETE CASCADE" +
                    ")"
                )
                database.execSQL(
                    "INSERT INTO cat_interactions_new (id, catId, date, type, foodItemId, timestamp, details) " +
                    "SELECT id, 1, date, type, foodItemId, timestamp, details FROM cat_interactions"
                )
                database.execSQL("DROP TABLE cat_interactions")
                database.execSQL("ALTER TABLE cat_interactions_new RENAME TO cat_interactions")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cat_interactions_date ON cat_interactions(date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cat_interactions_type ON cat_interactions(type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cat_interactions_catId ON cat_interactions(catId)")
            }
        }

        // Room expects catId with no DEFAULT clause (defaultValue = undefined).
        // Recreate table without DEFAULT and drop the extra catId index.
        val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE cat_interactions_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "catId INTEGER NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "foodItemId TEXT, " +
                        "timestamp INTEGER NOT NULL, " +
                        "details TEXT, " +
                        "FOREIGN KEY(catId) REFERENCES cat_state(id) ON DELETE CASCADE" +
                    ")"
                )
                database.execSQL(
                    "INSERT INTO cat_interactions_new (id, catId, date, type, foodItemId, timestamp, details) " +
                    "SELECT id, catId, date, type, foodItemId, timestamp, details FROM cat_interactions"
                )
                database.execSQL("DROP TABLE cat_interactions")
                database.execSQL("ALTER TABLE cat_interactions_new RENAME TO cat_interactions")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cat_interactions_date ON cat_interactions(date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cat_interactions_type ON cat_interactions(type)")
                database.execSQL("DROP INDEX IF EXISTS index_cat_interactions_catId")
            }
        }
    }
}
