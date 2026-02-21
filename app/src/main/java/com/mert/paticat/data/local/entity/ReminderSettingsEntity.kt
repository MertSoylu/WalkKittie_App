package com.mert.paticat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for water reminder settings.
 */
@Entity(tableName = "reminder_settings")
data class ReminderSettingsEntity(
    @PrimaryKey
    val id: Long = 1,
    val waterReminderEnabled: Boolean = true,
    val waterReminderIntervalMinutes: Int = 60,
    val waterReminderStartHour: Int = 8,
    val waterReminderEndHour: Int = 22,
    val stepReminderEnabled: Boolean = true,
    val stepReminderHour: Int = 20
)
