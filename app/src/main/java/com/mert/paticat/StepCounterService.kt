package com.mert.paticat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.mert.paticat.data.local.dao.DailyStatsDao
import com.mert.paticat.data.local.dao.UserProfileDao
import com.mert.paticat.data.local.entity.DailyStatsEntity
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.MissionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.max

/**
 * Optimized Step Counter Foreground Service
 * 
 * Battery Optimization Strategy:
 * 1. Uses hardware batching (30 second batches) to reduce CPU wakeups
 * 2. Only syncs to DB every 100 steps OR every 10 minutes (whichever comes first)
 * 3. Uses SENSOR_DELAY_NORMAL for lowest power consumption
 * 4. Minimal notification updates
 */
@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {
    
    @Inject lateinit var dailyStatsDao: DailyStatsDao
    @Inject lateinit var catRepository: CatRepository
    @Inject lateinit var userProfileDao: UserProfileDao
    @Inject lateinit var missionRepository: MissionRepository
    @Inject lateinit var stepCounterManager: StepCounterManager
    @Inject lateinit var userPreferencesRepository: com.mert.paticat.data.local.preferences.UserPreferencesRepository
    
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    // Step tracking state
    private var initialStepCount: Int = -1
    @Volatile private var currentDaySteps: Int = 0
    private var lastSavedDate: String = ""
    @Volatile private var lastProcessedStepsForRewards: Int = 0
    
    // Battery optimization thresholds
    @Volatile private var lastSyncedSteps: Int = 0
    private var lastSyncTime: Long = 0L
    private var lastNotificationSteps: Int = 0
    
    // User weight for calorie calculation (loaded from profile)
    private var userWeightKg: Float = 70f
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    companion object {
        const val CHANNEL_ID = "step_counter_channel"
        const val NOTIFICATION_ID = 2001
        
        const val ACTION_START = "com.mert.paticat.START_STEP_COUNTER"
        const val ACTION_STOP = "com.mert.paticat.STOP_STEP_COUNTER"
        
        private const val PREFS_NAME = "walkkittie_step_prefs"
        private const val KEY_INITIAL_SENSOR_VAL = "initial_sensor_val"
        private const val KEY_LAST_DATE = "last_date"
        private const val KEY_REWARD_STEPS_CURSOR = "reward_steps_cursor"
        
        // Reward settings
        private const val STEPS_PER_FOOD_POINT = 100
        
        // Battery optimization settings
        private const val STEP_SYNC_THRESHOLD = 100      // Sync every 100 steps
        private const val TIME_SYNC_THRESHOLD = 10 * 60 * 1000L  // Or every 10 minutes
        private const val NOTIFICATION_UPDATE_THRESHOLD = 100    // Update notification every 100 steps
        private const val BATCH_LATENCY_US = 60 * 1000 * 1000    // 60 seconds batching for battery optimization

        fun startService(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (context.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) 
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                }
                
                val intent = Intent(context, StepCounterService::class.java).apply {
                    action = ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("StepCounterService", "Start service failed", e)
            }
        }
        
        fun stopService(context: Context) {
            try {
                val intent = Intent(context, StepCounterService::class.java).apply {
                    action = ACTION_STOP
                }
                context.stopService(intent)
            } catch (e: Exception) {
                android.util.Log.e("StepCounterService", "Stop service failed", e)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        try {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            createNotificationChannel()
            loadState()
            loadUserWeight()
        } catch (e: Exception) {
            android.util.Log.e("StepCounterService", "onCreate failed", e)
        }
    }
    
    private fun loadUserWeight() {
        serviceScope.launch {
            try {
                val profile = userProfileDao.getUserProfileOnce()
                userWeightKg = profile?.weight ?: 70f
            } catch (e: Exception) {
                userWeightKg = 70f
            }
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent?.action == ACTION_STOP) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            // Permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) 
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    stopSelf()
                    return START_NOT_STICKY
                }
            }

            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
                } catch (e: Exception) {
                    stopSelf()
                    return START_NOT_STICKY
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            
            registerSensor()
        } catch (e: Exception) {
            android.util.Log.e("StepCounterService", "onStartCommand failed", e)
        }
        return START_STICKY
    }
    
    private fun registerSensor() {
        stepCounterSensor?.let { sensor ->
            // Use hardware batching to reduce CPU wakeups
            // SENSOR_DELAY_NORMAL = lowest power consumption
            // BATCH_LATENCY_US = collect events in hardware for 30 seconds before waking CPU
            sensorManager.registerListener(
                this, 
                sensor, 
                SensorManager.SENSOR_DELAY_NORMAL,
                BATCH_LATENCY_US
            )
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return
        
        val sensorValue = event.values[0].toInt()
        val today = LocalDate.now().format(dateFormatter)
        
        // Handle day change
        if (lastSavedDate != today) {
            handleNewDay(today, sensorValue)
        }
        
        // Ensure we load DB state on first read to avoid data loss on reboot
        if (initialStepCount < 0) {
            val dbSteps = loadTodayStepsFromDbSync(today)
            // If DB already has 500 steps, but sensor says 0 (reboot),
            // We set initial to -500 so formula works: max(0, 0 - (-500)) = 500
            initialStepCount = sensorValue - dbSteps
            currentDaySteps = dbSteps
            lastProcessedStepsForRewards = dbSteps
            lastSyncedSteps = dbSteps
        }
        
        // Handle device reboot mid-day (sensor value resets to 0 but we already initialized)
        if (sensorValue < initialStepCount && initialStepCount > 0) {
            // E.g., we had initial=1000, sensor was 1500 (500 local steps). Devices reboot.
            // Sensor is now 0. We want next step to be 501. 
            // We set initial = sensorValue - currentDaySteps = 0 - 500 = -500
            initialStepCount = sensorValue - currentDaySteps
        }
        
        // Calculate today's steps
        val newStepsSinceBoot = max(currentDaySteps, sensorValue - initialStepCount)
        currentDaySteps = newStepsSinceBoot
        
        // Update live steps for UI
        stepCounterManager.updateLiveSteps(currentDaySteps)
        
        // Battery optimization: Only sync if threshold met
        val currentTime = System.currentTimeMillis()
        val stepDiff = kotlin.math.abs(currentDaySteps - lastSyncedSteps)
        val timeDiff = currentTime - lastSyncTime
        
        if (stepDiff >= STEP_SYNC_THRESHOLD || timeDiff >= TIME_SYNC_THRESHOLD || lastSyncedSteps == 0) {
            syncToDatabase(today)
            lastSyncedSteps = currentDaySteps
            lastSyncTime = currentTime
        }
        
        // Update notification less frequently
        if (kotlin.math.abs(currentDaySteps - lastNotificationSteps) >= NOTIFICATION_UPDATE_THRESHOLD) {
            updateNotification()
            lastNotificationSteps = currentDaySteps
        }
    }
    
    private fun handleNewDay(today: String, sensorValue: Int) {
        lastSavedDate = today
        initialStepCount = sensorValue
        currentDaySteps = 0
        lastProcessedStepsForRewards = 0
        lastSyncedSteps = 0
        lastNotificationSteps = 0
        saveState()
    }
    
    private fun syncToDatabase(date: String) {
        val steps = currentDaySteps
        serviceScope.launch {
            try {
                // Calculate calories based on user weight
                val calories = calculateCalories(steps)
                
                // Update daily stats
                val existing = dailyStatsDao.getDailyStatsOnce(date)
                if (existing != null) {
                    dailyStatsDao.updateStats(existing.copy(steps = steps, caloriesBurned = calories))
                } else {
                    dailyStatsDao.insertDailyStats(DailyStatsEntity(date = date, steps = steps, caloriesBurned = calories))
                }
                
                // Calculate rewards
                val diffForRewards = steps - lastProcessedStepsForRewards
                if (diffForRewards >= STEPS_PER_FOOD_POINT) {
                    val pointsEarned = diffForRewards / STEPS_PER_FOOD_POINT
                    val remainder = diffForRewards % STEPS_PER_FOOD_POINT
                    
                    catRepository.addFoodPoints(pointsEarned)
                    catRepository.addXp(pointsEarned)
                    
                    // Track pending rewards for the UI notification
                    userPreferencesRepository.addPendingRewards(pointsEarned, pointsEarned)
                    
                    lastProcessedStepsForRewards = steps - remainder
                }
                
                // Check missions
                missionRepository.checkAndCompleteMissions(steps, 0)
                
                // Update cat status (decay/recovery) periodically in background
                catRepository.decreaseHungerOverTime()
                
                // Save state
                saveState()
                
            } catch (e: Exception) {
                android.util.Log.e("StepCounterService", "Sync failed", e)
            }
        }
    }
    
    /**
     * Calculate calories burned based on steps and user weight
     * Formula: calories = steps × 0.04 × (weight / 70)
     * 
     * Base assumption: 70kg person burns ~0.04 kcal per step
     * Heavier people burn more, lighter people burn less
     */
    private fun calculateCalories(steps: Int): Int {
        val caloriesPerStep = 0.04f * (userWeightKg / 70f)
        return (steps * caloriesPerStep).toInt()
    }
    
    private fun loadState() {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
            val today = LocalDate.now().format(dateFormatter)
            
            if (savedDate == today) {
                // Same day, load memory state
                initialStepCount = prefs.getInt(KEY_INITIAL_SENSOR_VAL, -1)
                lastProcessedStepsForRewards = prefs.getInt(KEY_REWARD_STEPS_CURSOR, 0)
                lastSavedDate = today
            } else {
                // Different day (or first install), let the sensor init block handle it DB fetch
                initialStepCount = -1 
                lastSavedDate = today
                lastProcessedStepsForRewards = 0
            }
    }
    
    private fun loadTodayStepsFromDbSync(date: String): Int {
        var steps = 0
        // Need to run slightly synchronously since sensor event expects an immediate answer
        // But since this is first time init it's acceptable.
        kotlinx.coroutines.runBlocking(Dispatchers.IO) {
            val entity = dailyStatsDao.getDailyStatsOnce(date)
            steps = entity?.steps ?: 0
        }
        return steps
    }
    
    private fun saveState() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putInt(KEY_INITIAL_SENSOR_VAL, initialStepCount)
            putString(KEY_LAST_DATE, lastSavedDate)
            putInt(KEY_REWARD_STEPS_CURSOR, lastProcessedStepsForRewards)
            apply()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceJob.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Low importance for step counter (minimal battery/distraction)
            val channel = NotificationChannel(
                CHANNEL_ID, 
                getString(R.string.notif_channel_steps_name), 
                NotificationManager.IMPORTANCE_LOW
            ).apply { 
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
            
            // Goal achievement channel (high importance)
            val goalChannel = NotificationChannel(
                "goal_channel", 
                getString(R.string.notif_channel_goals_name), 
                NotificationManager.IMPORTANCE_HIGH
            ).apply { 
                description = getString(R.string.notif_channel_goals_desc)
            }
            notificationManager.createNotificationChannel(goalChannel)
            
            // Cat status channel
            val catChannel = NotificationChannel(
                "cat_status_channel", 
                getString(R.string.notif_channel_cat_status_name), 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notif_channel_cat_status_desc)
            }
            notificationManager.createNotificationChannel(catChannel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val calories = calculateCalories(currentDaySteps)
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_channel_steps_name))
            .setContentText(getString(R.string.notif_steps_content, currentDaySteps, calories))
            .setSmallIcon(R.drawable.ic_paw_small)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)  // No sound/vibration for battery saving
            .build()
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }
}
