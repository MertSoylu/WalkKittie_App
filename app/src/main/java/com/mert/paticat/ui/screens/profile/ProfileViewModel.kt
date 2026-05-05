package com.mert.paticat.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import androidx.work.await
import com.mert.paticat.data.local.PatiCatDatabase
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val catRepository: CatRepository,
    private val userProfileRepository: UserProfileRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val database: PatiCatDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _resetComplete = MutableStateFlow(false)
    val resetComplete: StateFlow<Boolean> = _resetComplete.asStateFlow()

    val currentThemeColor = preferencesRepository.selectedTheme
    val isDarkMode = preferencesRepository.isDarkMode

    init {
        viewModelScope.launch { initializeProfile() }
        observeData()
    }

    private suspend fun initializeProfile() {
        try {
            userProfileRepository.initializeProfileIfNeeded()
            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private fun observeData() {
        // Each flow is observed in its own launch with a `.catch` so a single
        // upstream failure doesn't tear down the others.
        viewModelScope.launch {
            catRepository.getCat()
                .catch { e -> _uiState.update { it.copy(observeError = e.message) } }
                .collect { cat ->
                    _uiState.update { it.copy(cat = cat) }
                }
        }

        viewModelScope.launch {
            userProfileRepository.getUserProfile()
                .catch { e -> _uiState.update { it.copy(observeError = e.message) } }
                .collect { profile ->
                    profile?.let { p ->
                        _uiState.update { it.copy(userProfile = p) }
                    }
                }
        }

        viewModelScope.launch {
            preferencesRepository.notificationsEnabled
                .catch { e -> _uiState.update { it.copy(observeError = e.message) } }
                .collect { enabled ->
                    _uiState.update { it.copy(notificationsEnabled = enabled) }
                }
        }
    }

    /**
     * Wraps a preference write so the UI can render a saving spinner / error
     * snackbar without each call site reimplementing the same boilerplate.
     */
    private fun savePreference(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPrefs = true) }
            try {
                block()
                _uiState.update { it.copy(saveError = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message) }
            } finally {
                _uiState.update { it.copy(isSavingPrefs = false) }
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) = savePreference {
        preferencesRepository.updateDarkMode(enabled)
    }

    fun selectThemeColor(colorName: String) = savePreference {
        preferencesRepository.updateTheme(colorName)
    }

    fun updateNotifications(enabled: Boolean) = savePreference {
        preferencesRepository.updateNotificationsEnabled(enabled)
    }

    fun updateStepGoal(goal: Int) = savePreference {
        userProfileRepository.updateStepGoal(goal)
    }

    fun updateWaterGoal(goal: Int) = savePreference {
        userProfileRepository.updateWaterGoal(goal)
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPrefs = true) }
            try {
                val current = userProfileRepository.getUserProfileOnce()
                if (current == null) {
                    // Profile not yet initialized — surface a save error rather than
                    // silently dropping the rename. Caller can retry once init completes.
                    _uiState.update { it.copy(saveError = "profile_not_initialized") }
                    return@launch
                }
                userProfileRepository.updateProfile(current.copy(name = name))
                _uiState.update { it.copy(saveError = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message) }
            } finally {
                _uiState.update { it.copy(isSavingPrefs = false) }
            }
        }
    }

    fun updateGender(gender: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPrefs = true) }
            try {
                val current = userProfileRepository.getUserProfileOnce()
                if (current == null) {
                    _uiState.update { it.copy(saveError = "profile_not_initialized") }
                    return@launch
                }
                userProfileRepository.updateProfile(current.copy(gender = gender))
                _uiState.update { it.copy(saveError = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message) }
            } finally {
                _uiState.update { it.copy(isSavingPrefs = false) }
            }
        }
    }

    fun updateCatName(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPrefs = true) }
            try {
                val cat = catRepository.getCatOnce()
                catRepository.updateCat(cat.copy(name = name))
                _uiState.update { it.copy(saveError = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message) }
            } finally {
                _uiState.update { it.copy(isSavingPrefs = false) }
            }
        }
    }

    /**
     * Persists a new locale code. Sets [ProfileUiState.isLocaleSwitching] so the
     * screen can show a loading veil while the activity is recreated by the
     * locale-aware base class.
     */
    fun updateLocale(languageCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocaleSwitching = true) }
            try {
                preferencesRepository.updateLocale(languageCode)
                _uiState.update { it.copy(saveError = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message) }
            } finally {
                _uiState.update { it.copy(isLocaleSwitching = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, saveError = null, observeError = null) }
    }

    fun resetAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResetting = true) }
            try {
                // 1. AWAIT WorkManager cancellation before touching DB/prefs so
                //    in-flight workers can't write back into freshly-cleared
                //    tables.
                WorkManager.getInstance(context).cancelAllWork().await()
                // 2. Clear Room tables on IO.
                withContext(Dispatchers.IO) { database.clearAllTables() }
                // 3. Atomic prefs reset (preserves locale).
                preferencesRepository.resetExceptLanguage()
                _resetComplete.value = true
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message) }
            } finally {
                _uiState.update { it.copy(isResetting = false) }
            }
        }
    }

    fun acknowledgeReset() {
        _resetComplete.value = false
    }
}
