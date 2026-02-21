package com.mert.paticat

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.mert.paticat.worker.CatStatusWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class PatiCatApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var adManager: com.mert.paticat.data.ads.AdManager
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
            
    override fun onCreate() {
        super.onCreate()
        adManager.initialize()
        try {
            setupBackgroundWorkers()
        } catch (e: Exception) {
            android.util.Log.e("PatiCatApp", "Background workers setup failed", e)
        }
    }
    
    private fun setupBackgroundWorkers() {
        // Enqueue workers AFTER app is created
        try {
            val workManager = WorkManager.getInstance(this)
            
            // 1. Cat Status Worker (Every 30 minutes) - Check hunger/sleep
            val catStatusRequest = PeriodicWorkRequestBuilder<CatStatusWorker>(30, TimeUnit.MINUTES)
                .setConstraints(Constraints.NONE)
                .build()
                
            workManager.enqueueUniquePeriodicWork(
                "cat_status_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                catStatusRequest
            )
            
            // 2. Widget Update Worker (Every 1 hour)
            val widgetUpdateRequest = PeriodicWorkRequestBuilder<com.mert.paticat.widget.WidgetUpdateWorker>(1, TimeUnit.HOURS)
                .setConstraints(Constraints.NONE)
                .build()
                
            workManager.enqueueUniquePeriodicWork(
                "widget_update",
                ExistingPeriodicWorkPolicy.UPDATE,
                widgetUpdateRequest
            )
            
            // 3. One-time trigger on App launch
            val oneTimeWidgetUpdate = OneTimeWorkRequestBuilder<com.mert.paticat.widget.WidgetUpdateWorker>().build()
            workManager.enqueue(oneTimeWidgetUpdate)
            
        } catch (e: Exception) {
             android.util.Log.e("PatiCatApp", "Worker setup error", e)
        }
    }
}
