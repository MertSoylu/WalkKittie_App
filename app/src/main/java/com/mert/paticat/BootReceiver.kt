package com.mert.paticat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mert.paticat.StepCounterService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Boot receiver to restart step counter service when device restarts.
 * Moved to main package for maximum visibility and to prevent ClassNotFound issues.
 */
@dagger.hilt.android.AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @javax.inject.Inject
    lateinit var userPreferencesRepository: com.mert.paticat.data.local.preferences.UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val pendingResult = goAsync()
            
            // Use a coroutine to read DataStore safely
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val stepCountingEnabled = userPreferencesRepository.stepCountingEnabled.first()
                    
                    if (stepCountingEnabled) {
                        try {
                            StepCounterService.startService(context)
                        } catch (e: Exception) {
                            android.util.Log.e("BootReceiver", "Failed to start StepCounterService", e)
                        }
                    }
                } catch (e: Exception) {
                     android.util.Log.e("BootReceiver", "Error reading preferences", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
