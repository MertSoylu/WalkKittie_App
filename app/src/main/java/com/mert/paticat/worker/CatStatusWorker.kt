package com.mert.paticat.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mert.paticat.R
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.app.PendingIntent
import android.content.Intent
import com.mert.paticat.MainActivity
import kotlinx.coroutines.flow.first

@HiltWorker
class CatStatusWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val catRepository: CatRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Check global notification key
        val notificationsEnabled = userPreferencesRepository.notificationsEnabled.first()
        if (!notificationsEnabled) {
            return Result.success()
        }

        val cat = catRepository.getCatOnce()
        
        val prefs = applicationContext.getSharedPreferences("paticat_game_state", Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        val twelveHoursInMillis = 12 * 60 * 60 * 1000L
        
        // 1. Check Hunger
        if (cat.hunger < 30) {
            val lastHungerNotif = prefs.getLong("last_hunger_notif_time", 0L)
            if (currentTime - lastHungerNotif >= twelveHoursInMillis) {
                sendNotification(
                    id = 1001,
                    title = applicationContext.getString(R.string.notif_cat_hungry_title),
                    text = applicationContext.getString(R.string.notif_cat_hungry_text)
                )
                prefs.edit().putLong("last_hunger_notif_time", currentTime).apply()
            }
        }
        
        // 2. Check Sleep Wake Up — read from DB (source of truth), not SharedPrefs
        val endTime = cat.sleepEndTime
        
        if (endTime > 0 && currentTime > endTime) {
             val lastWakeNotif = prefs.getLong("last_wake_notif_time", 0L)
             if (currentTime - lastWakeNotif >= twelveHoursInMillis) {
                 sendNotification(
                     id = 1002,
                     title = applicationContext.getString(R.string.notif_cat_woke_up_title),
                     text = applicationContext.getString(R.string.notif_cat_woke_up_text)
                 )
                 prefs.edit()
                     .putLong("last_wake_notif_time", currentTime)
                     .apply()
             }
        }

        return Result.success()
    }

    private fun sendNotification(id: Int, title: String, text: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            id, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "cat_status_channel")
            .setSmallIcon(R.drawable.ic_paw_small)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }
}
