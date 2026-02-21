package com.mert.paticat

import android.os.Bundle
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
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

@Inject
    lateinit var userPreferencesRepository: com.mert.paticat.data.local.preferences.UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        
        // Initialize Mobile Ads SDK
        // MobileAds.initialize(this) {} // Moved to after consent
        
        // GDPR Consent (UMP)
        requestConsentInfoUpdate()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentThemeColor by viewModel.currentThemeColor.collectAsState()
            
            WalkkittieTheme(darkTheme = isDarkMode, themeColor = currentThemeColor) {
                MainScreen()
            }
        }
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
        
        // Uncomment to test:
        // consentInformation.reset() 

        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        android.util.Log.w("AdMob", "${loadAndShowError.errorCode}: ${loadAndShowError.message}")
                    }

                    if (consentInformation.canRequestAds()) {
                        MobileAds.initialize(this) {}
                    }
                }
            },
            { requestConsentError ->
                android.util.Log.w("AdMob", "${requestConsentError.errorCode}: ${requestConsentError.message}")
            }
        )
    }
}
