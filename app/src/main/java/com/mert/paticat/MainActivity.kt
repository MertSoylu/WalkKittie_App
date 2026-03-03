package com.mert.paticat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import com.mert.paticat.ui.theme.WalkkittieTheme
import androidx.activity.result.contract.ActivityResultContracts
import com.mert.paticat.ui.screens.main.MainScreen
import javax.inject.Inject
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.activity.result.IntentSenderRequest
import com.mert.paticat.update.InAppUpdateManager

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var stepCounterManager: StepCounterManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACTIVITY_RECOGNITION] == true) {
            // Permission granted, start step counting explicitly
            stepCounterManager.startStepCounting()
        }
    }

    // In-App Update
    private lateinit var inAppUpdateManager: InAppUpdateManager

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Log.w("InAppUpdate", "Update flow cancelled or failed, resultCode: ${result.resultCode}")
        }
    }

    @Inject
    lateinit var userPreferencesRepository: com.mert.paticat.data.local.preferences.UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize In-App Update Manager
        inAppUpdateManager = InAppUpdateManager(this, updateResultLauncher)
        inAppUpdateManager.checkForUpdate()

        // Sync Locale from DataStore
        lifecycleScope.launch {
            val isLanguageSelected = userPreferencesRepository.isLanguageSelected.first()
            if (isLanguageSelected) {
                val savedLanguage = userPreferencesRepository.localeLanguage.first()
                val currentAppLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
                val currentLang = if (!currentAppLocales.isEmpty) currentAppLocales.get(0)?.language else null
                
                if (savedLanguage != currentLang) {
                        val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(savedLanguage)
                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
                }
            }
        }

        // Request necessary permissions on start
        checkPermissions()
        
        // GDPR Consent (UMP) — ad initialization handled by AdManager.initialize() in Application class
        requestConsentInfoUpdate()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentThemeColor by viewModel.currentThemeColor.collectAsState()
            
            WalkkittieTheme(darkTheme = isDarkMode, themeColor = currentThemeColor) {
                MainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateManager.onResume()
    }

    override fun onDestroy() {
        inAppUpdateManager.onDestroy()
        super.onDestroy()
    }

    private fun checkPermissions() {
        val permissions = stepCounterManager.getRequiredPermissions()
        if (permissions.isNotEmpty()) {
            val missingPermissions = permissions.filter {
                androidx.core.content.ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
            }
            if (missingPermissions.isNotEmpty()) {
                requestPermissionLauncher.launch(missingPermissions.toTypedArray())
            } else {
                // Permissions already granted, initialize service if enabled
                stepCounterManager.initStepCounting()
            }
        } else {
            // No permissions needed (old Android), initialize service if enabled
            stepCounterManager.initStepCounting()
        }
    }
    
    private fun requestConsentInfoUpdate() {
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(this)

        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        if (BuildConfig.DEBUG) {
                            android.util.Log.w("AdMob", "${loadAndShowError.errorCode}: ${loadAndShowError.message}")
                        }
                    }
                    // Ad initialization is handled by AdManager.initialize() in PatiCatApp —
                    // no need to call MobileAds.initialize() again here.
                }
            },
            { requestConsentError ->
                if (BuildConfig.DEBUG) {
                    android.util.Log.w("AdMob", "${requestConsentError.errorCode}: ${requestConsentError.message}")
                }
            }
        )
    }
}
