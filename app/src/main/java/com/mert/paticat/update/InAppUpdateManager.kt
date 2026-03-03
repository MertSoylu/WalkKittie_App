package com.mert.paticat.update

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.mert.paticat.R

class InAppUpdateManager(
    private val activity: Activity,
    private val updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
) {

    companion object {
        private const val TAG = "InAppUpdate"
    }

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateDownloadedSnackbar()
        }
    }

    init {
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    /**
     * Checks if an update is available and starts the flexible update flow if so.
     */
    fun checkForUpdate() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    Log.i(TAG, "Flexible update available, starting update flow.")
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                } else {
                    Log.d(TAG, "No flexible update available.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to check for update: ${exception.message}")
            }
    }

    /**
     * Should be called from Activity's onResume.
     * If a flexible update has been downloaded while the app was in background,
     * it shows a snackbar to prompt the user to install.
     */
    fun onResume() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    showUpdateDownloadedSnackbar()
                }
            }
    }

    /**
     * Should be called from Activity's onDestroy to unregister the listener.
     */
    fun onDestroy() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    private fun showUpdateDownloadedSnackbar() {
        val rootView: View = activity.findViewById(android.R.id.content)
        Snackbar.make(
            rootView,
            activity.getString(R.string.update_downloaded),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(activity.getString(R.string.update_install)) {
                appUpdateManager.completeUpdate()
            }
            show()
        }
    }
}
