package com.mert.paticat.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.paticat.data.local.dao.UserProfileDao
import com.mert.paticat.data.local.entity.UserProfileEntity
import com.mert.paticat.data.local.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val userProfileDao: UserProfileDao,
    private val catRepository: com.mert.paticat.domain.repository.CatRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            _isOnboardingCompleted.value = preferencesRepository.isOnboardingCompleted.first()
            _isLoading.value = false
        }
    }

    fun saveUserProfile(name: String, catName: String, gender: String, stepGoal: Int, waterGoal: Int, calorieGoal: Int) {
        viewModelScope.launch {
            // Update Cat Name
            catRepository.initializeCat()
            val cat = catRepository.getCatOnce()
            catRepository.updateCat(cat.copy(name = catName))

            // Update Room Database
            val existingProfile = userProfileDao.getUserProfileOnce()
            if (existingProfile != null) {
                userProfileDao.updateProfile(
                    existingProfile.copy(
                        name = name,
                        gender = gender,
                        dailyStepGoal = stepGoal,
                        dailyWaterGoalMl = waterGoal,
                        dailyCalorieGoal = calorieGoal
                    )
                )
            } else {
                userProfileDao.insertProfile(
                    UserProfileEntity(
                        name = name,
                        gender = gender,
                        dailyStepGoal = stepGoal,
                        dailyWaterGoalMl = waterGoal,
                        dailyCalorieGoal = calorieGoal
                    )
                )
            }

            // Save preference flag
            preferencesRepository.setOnboardingCompleted(true)
            preferencesRepository.setUserName(name)
            _isOnboardingCompleted.value = true
        }
    }
}
