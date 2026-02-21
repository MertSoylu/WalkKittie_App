package com.mert.paticat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for step counter service.
 * Prioritizes Health Connect over foreground service to save battery.
 * 
 * Strategy:
 * 1. If Health Connect is available and has permissions -> Use Health Connect (no battery drain)
 * 2. Otherwise -> Use foreground service with optimized batching
 */
@Singleton
class StepCounterManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: com.mert.paticat.data.local.preferences.UserPreferencesRepository
) {
    // Removed legacy prefs: private val prefs = context.getSharedPreferences("walkkittie_settings", Context.MODE_PRIVATE)
    
    private val _liveSteps = MutableStateFlow(0)
    val liveSteps: StateFlow<Int> = _liveSteps.asStateFlow()
    
    fun updateLiveSteps(steps: Int) {
        _liveSteps.value = steps
    }

    /**
     * Check if step counting is currently enabled.
     * Note: This is a suspend function or requires runBlocking if called synchronously.
     * Only use blocking for legacy compat, prefer async flow collection where possible.
     */
    val isEnabled: Boolean
        get() = kotlinx.coroutines.runBlocking {
            userPreferencesRepository.stepCountingEnabled.first()
        }
    
    /**
     * Check if device has step sensor hardware
     */
    fun hasStepSensor(): Boolean {
        return try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER) != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if ACTIVITY_RECOGNITION permission is granted
     */
    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    /**
     * Start step counting
     * Prioritizes Health Connect if available, otherwise uses foreground service
     */
    fun startStepCounting() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            userPreferencesRepository.updateStepCountingEnabled(true)
        }
        
        // Always start the foreground service to keep the notification visible.
        // The service is optimized for battery (hardware batching).
        // Health Connect will still be used for syncing detailed data in the background/ViewModel.
        if (hasStepSensor() && hasPermission()) {
             StepCounterService.startService(context)
        }
    }
    
    /**
     * Initialize step counting on app start.
     * Only starts the service if the user has NOT explicitly disabled it.
     */
    fun initStepCounting() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val enabled = userPreferencesRepository.stepCountingEnabled.first()
            if (enabled && hasStepSensor() && hasPermission()) {
                 StepCounterService.startService(context)
            }
        }
    }

    /**
     * Stop step counting completely
     */
    fun stopStepCounting() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            userPreferencesRepository.updateStepCountingEnabled(false)
        }
        StepCounterService.stopService(context)
    }
    
    /**
     * Toggle step counting on/off
     */
    fun toggleStepCounting(): Boolean {
        val currentlyEnabled = isEnabled
        return if (currentlyEnabled) {
            stopStepCounting()
            false
        } else {
            startStepCounting()
            true
        }
    }
    
    /**
     * Get required permissions based on Android version
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            emptyArray()
        }
    }
    
    /**
     * Calculate calories burned from steps
     * Uses MET (Metabolic Equivalent of Task) formula for accuracy:
     * Calories = MET × Weight(kg) × Time(hours)
     * 
     * For walking: MET ≈ 3.5
     * Average step takes ~0.5 seconds = 0.000139 hours
     * 
     * Simplified formula: Calories per step = 0.04 × (weight/70)
     * Default weight: 70kg -> 0.04 kcal/step
     * 
     * @param steps Number of steps
     * @param weightKg User's weight in kg (default 70kg)
     * @return Estimated calories burned
     */
    fun calculateCalories(steps: Int, weightKg: Float = 70f): Int {
        // Base: 0.04 kcal per step for 70kg person
        // Adjusted for user's weight
        val caloriesPerStep = 0.04f * (weightKg / 70f)
        return (steps * caloriesPerStep).toInt()
    }
    
    companion object {
        // Standard calories per step (for 70kg person)
        const val BASE_CALORIES_PER_STEP = 0.04f
        
        // Steps needed to earn 1 food point (MP)
        const val STEPS_PER_FOOD_POINT = 100
    }
}
