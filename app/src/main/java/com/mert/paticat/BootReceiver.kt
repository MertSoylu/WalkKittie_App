package com.mert.paticat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Restarts the [StepCounterService] after device boot if step counting is enabled.
 * Uses a [SupervisorJob]-backed scope for the DataStore read.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Boot completed, checking step counter state")

            // goAsync() extends the BroadcastReceiver deadline so we can safely launch a coroutine
            val pendingResult = goAsync()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            scope.launch {
                try {
                    val prefs = context.getSharedPreferences("walkkittie_step_prefs", Context.MODE_PRIVATE)
                    val isEnabled = prefs.getBoolean("step_counting_enabled_sp_backup", false)

                    if (isEnabled) {
                        Log.d(TAG, "Step counting is enabled, restarting service")
                        StepCounterService.startService(context)
                    } else {
                        Log.d(TAG, "Step counting is disabled, skipping service start")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in BootReceiver", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
