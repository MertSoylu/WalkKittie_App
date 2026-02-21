package com.mert.paticat.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mert.paticat.MainActivity
import com.mert.paticat.R
import com.mert.paticat.data.local.dao.ReminderDao
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WorkManager worker for water drinking reminders.
 */
@HiltWorker
class WaterReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val reminderDao: ReminderDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "water_reminder_work"
        
        private val waterMessages = listOf(
            "💧 Su içme zamanı! Kedini mutlu et!",
            "🐱 Minik dostun seninle birlikte su içmek istiyor!",
            "💦 Sağlıklı kalmak için bir bardak su iç!",
            "🌊 Hidrate ol, mutlu ol! Su içmeyi unutma!",
            "💧 Bugün yeterince su içtin mi? Bir bardak daha!",
            "🐾 Kedinin sana hatırlatması var: Su iç!",
            "💙 Vücudun su bekliyor! Şimdi iç!"
        )
    }
    
    override suspend fun doWork(): Result {
        // Check global notification setting first
        val notificationsEnabled = userPreferencesRepository.notificationsEnabled.first()
        if (!notificationsEnabled) {
            return Result.success()
        }

        val settings = reminderDao.getSettingsOnce()
        
        if (settings?.waterReminderEnabled != true) {
            return Result.success()
        }
        
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        
        // Check if current time is within reminder hours
        if (currentHour < settings.waterReminderStartHour || currentHour >= settings.waterReminderEndHour) {
            return Result.success()
        }
        
        showNotification()
        return Result.success()
    }
    
    private fun showNotification() {
        createNotificationChannel()
        
        val intent = Intent(applicationContext, MainActivity::class.java)
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val message = waterMessages.random()
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_paw_small)
            .setContentTitle("Walkkittie 🐱")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Su Hatırlatıcı"
            val descriptionText = "Günlük su içme hatırlatmaları"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
