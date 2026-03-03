package com.mert.paticat.di

import android.content.Context
import androidx.room.Room
import com.mert.paticat.data.local.PatiCatDatabase
import com.mert.paticat.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PatiCatDatabase {
        return Room.databaseBuilder(
            context,
            PatiCatDatabase::class.java,
            PatiCatDatabase.DATABASE_NAME
        )
            .addMigrations(PatiCatDatabase.MIGRATION_8_9, PatiCatDatabase.MIGRATION_9_10, PatiCatDatabase.MIGRATION_10_11)
            // Only allow destructive migration from versions before explicit migrations existed.
            // Versions 8+ are covered by explicit migrations — a missing migration will crash
            // instead of silently wiping user data.
            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6, 7)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCatDao(database: PatiCatDatabase): CatDao = database.catDao()
    
    @Provides
    @Singleton
    fun provideDailyStatsDao(database: PatiCatDatabase): DailyStatsDao = database.dailyStatsDao()
    
    @Provides
    @Singleton
    fun provideMissionDao(database: PatiCatDatabase): MissionDao = database.missionDao()
    
    @Provides
    @Singleton
    fun provideUserProfileDao(database: PatiCatDatabase): UserProfileDao = database.userProfileDao()
    
    @Provides
    @Singleton
    fun provideReminderDao(database: PatiCatDatabase): ReminderDao = database.reminderDao()
    
    @Provides
    @Singleton
    fun provideMealDao(database: PatiCatDatabase): MealDao = database.mealDao()
    
    @Provides
    @Singleton
    fun provideInventoryDao(database: PatiCatDatabase): InventoryDao = database.inventoryDao()
    
    @Provides
    @Singleton
    fun provideCatInteractionDao(database: PatiCatDatabase): CatInteractionDao = database.catInteractionDao()
}
