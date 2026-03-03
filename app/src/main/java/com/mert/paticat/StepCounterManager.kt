package com.mert.paticat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages step counting: decides between Health Connect and the foreground service,
 * handles permissions, and exposes live steps via a StateFlow.
 *
 * Uses a [SupervisorJob]-backed CoroutineScope to prevent orphan coroutines.
 */
@Singleton
class StepCounterManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "StepCounterManager"
    }

    /** Managed coroutine scope with SupervisorJob — child failures don't crash the parent. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _liveSteps = MutableStateFlow(0)
    val liveSteps: StateFlow<Int> = _liveSteps.asStateFlow()

    /**
     * Checks if step counting is enabled. This is a suspend function to avoid
     * blocking the calling thread with DataStore I/O.
     */
    suspend fun isEnabled(): Boolean =
        userPreferencesRepository.stepCountingEnabled.first()

    fun updateLiveSteps(steps: Int) {
        _liveSteps.value = steps
    }

    fun hasStepSensor(): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        return sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER) != null
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Toggle step counting state. Returns the new enabled state.
     * This is a suspend function — must be called from a coroutine.
     */
    suspend fun toggleStepCounting(): Boolean {
        val currentlyEnabled = isEnabled()
        return if (currentlyEnabled) {
            stopStepCounting()
            false
        } else {
            startStepCounting()
            true
        }
    }

    fun startStepCounting() {
        scope.launch {
            userPreferencesRepository.updateStepCountingEnabled(true)
        }
        if (hasStepSensor() && hasPermission()) {
            StepCounterService.startService(context)
        }
    }

    fun stopStepCounting() {
        scope.launch {
            userPreferencesRepository.updateStepCountingEnabled(false)
        }
        try {
            val intent = Intent(context, StepCounterService::class.java)
            context.stopService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping StepCounterService", e)
        }
    }

    fun startServiceIfEnabled() {
        scope.launch {
            try {
                val enabled = isEnabled()
                if (enabled && hasStepSensor() && hasPermission()) {
                    StepCounterService.startService(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking/starting step counter", e)
            }
        }
    }

    // ===== Calorie Calculation =====

    fun calculateCalories(steps: Int, weightKg: Float = 70f): Int {
        val strideLength = 0.762 // meters
        val distanceKm = steps * strideLength / 1000.0
        val weightFactor = weightKg / 70f
        return (distanceKm * 60 * weightFactor).toInt()
    }

    // ===== Permission helpers (called from MainActivity) =====

    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions
    }

    /**
     * Called from [MainActivity] after permissions are confirmed.
     * Starts the step counting service if enabled and conditions are met.
     */
    fun initStepCounting() {
        startServiceIfEnabled()
    }
}
