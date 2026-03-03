package com.mert.paticat.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Profile Screen.
 * Manages user settings and profile data.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val catRepository: CatRepository,
    private val userProfileRepository: UserProfileRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    val currentThemeColor = preferencesRepository.selectedTheme
    val isDarkMode = preferencesRepository.isDarkMode
    
    init {
        initializeProfile()
        observeData()
    }
    
    private fun initializeProfile() {
        viewModelScope.launch {
            userProfileRepository.initializeProfileIfNeeded()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    private fun observeData() {
        viewModelScope.launch {
            catRepository.getCat().collect { cat ->
                _uiState.update { it.copy(cat = cat) }
            }
        }
        
        viewModelScope.launch {
            userProfileRepository.getUserProfile().collect { profile ->
                profile?.let {
                    _uiState.update { it.copy(userProfile = profile) }
                }
            }
        }
        
        viewModelScope.launch {
            preferencesRepository.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }
    }
    
    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDarkMode(enabled)
        }
    }

    fun selectThemeColor(colorName: String) {
        viewModelScope.launch {
            preferencesRepository.updateTheme(colorName)
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationsEnabled(enabled)
        }
    }
    
    fun updateStepGoal(goal: Int) {
        viewModelScope.launch {
            userProfileRepository.updateStepGoal(goal)
        }
    }
    
    fun updateWaterGoal(goal: Int) {
        viewModelScope.launch {
            userProfileRepository.updateWaterGoal(goal)
        }
    }
    
    fun updateUserName(name: String) {
        viewModelScope.launch {
            val current = userProfileRepository.getUserProfileOnce()
            current?.let {
                userProfileRepository.updateProfile(it.copy(name = name))
            }
        }
    }

    fun updateGender(gender: String) {
        viewModelScope.launch {
            val current = userProfileRepository.getUserProfileOnce()
            current?.let {
                userProfileRepository.updateProfile(it.copy(gender = gender))
            }
        }
    }

    fun updateCatName(name: String) {
        viewModelScope.launch {
            val cat = catRepository.getCatOnce()
            catRepository.updateCat(cat.copy(name = name))
        }
    }
    
    fun updateLocale(languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.updateLocale(languageCode)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
