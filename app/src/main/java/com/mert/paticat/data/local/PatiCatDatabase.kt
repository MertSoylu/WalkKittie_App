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
        MealEntity::class
    ],
    version = 9, // Version 9: Added lastInteractionTime to CatEntity
    exportSchema = false
)
abstract class PatiCatDatabase : RoomDatabase() {
    
    abstract fun catDao(): CatDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun missionDao(): MissionDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun reminderDao(): ReminderDao
    abstract fun mealDao(): MealDao
    
    companion object {
        const val DATABASE_NAME = "paticat_database"

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add lastInteractionTime column to cat_state table with current time as default
                database.execSQL("ALTER TABLE cat_state ADD COLUMN lastInteractionTime INTEGER NOT NULL DEFAULT " + System.currentTimeMillis())
            }
        }
    }
}
