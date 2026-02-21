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
            .addMigrations(PatiCatDatabase.MIGRATION_8_9)
            .fallbackToDestructiveMigration()
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
}
