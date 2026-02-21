package com.mert.paticat.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for scheduling and canceling reminder workers.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule water reminder with specified interval in minutes.
     */
    fun scheduleWaterReminder(intervalMinutes: Int) {
        val waterReminderRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            intervalMinutes.toLong(),
            TimeUnit.MINUTES
        )
            .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WaterReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            waterReminderRequest
        )
    }
    
    /**
     * Cancel water reminder.
     */
    fun cancelWaterReminder() {
        workManager.cancelUniqueWork(WaterReminderWorker.WORK_NAME)
    }
    
    /**
     * Update water reminder interval.
     */
    fun updateWaterReminderInterval(intervalMinutes: Int) {
        cancelWaterReminder()
        scheduleWaterReminder(intervalMinutes)
    }
}
