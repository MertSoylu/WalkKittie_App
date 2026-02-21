package com.mert.paticat.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore repository for storing simple user preferences.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed_v2")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val SELECTED_THEME = stringPreferencesKey("selected_theme")
    private val LOCALE_LANGUAGE = stringPreferencesKey("locale_language")
    private val IS_LANGUAGE_SELECTED = booleanPreferencesKey("is_language_selected_v2")
    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    val selectedTheme: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_THEME] ?: "Standard"
        }

    val localeLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LOCALE_LANGUAGE] ?: "tr"
        }

    val isLanguageSelected: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LANGUAGE_SELECTED] ?: false
        }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: false
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
    
    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    suspend fun updateTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_THEME] = themeName
        }
    }

    suspend fun updateLocale(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LOCALE_LANGUAGE] = languageCode
            preferences[IS_LANGUAGE_SELECTED] = true
        }
    }

    suspend fun updateDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    private val STEP_COUNTING_ENABLED = booleanPreferencesKey("step_counting_enabled_v2")
    private val PENDING_REWARD_XP = androidx.datastore.preferences.core.intPreferencesKey("pending_reward_xp")
    private val PENDING_REWARD_FOOD = androidx.datastore.preferences.core.intPreferencesKey("pending_reward_food")
    private val LAST_SEEN_LEVEL = androidx.datastore.preferences.core.intPreferencesKey("last_seen_level")

    val stepCountingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[STEP_COUNTING_ENABLED] ?: true
        }

    val pendingRewardXp: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PENDING_REWARD_XP] ?: 0
        }

    val pendingRewardFood: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PENDING_REWARD_FOOD] ?: 0
        }

    suspend fun updateStepCountingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[STEP_COUNTING_ENABLED] = enabled
        }
    }

    suspend fun addPendingRewards(xp: Int, food: Int) {
        context.dataStore.edit { preferences ->
            val currentXp = preferences[PENDING_REWARD_XP] ?: 0
            val currentFood = preferences[PENDING_REWARD_FOOD] ?: 0
            preferences[PENDING_REWARD_XP] = currentXp + xp
            preferences[PENDING_REWARD_FOOD] = currentFood + food
        }
    }

    suspend fun clearPendingRewards() {
        context.dataStore.edit { preferences ->
            preferences[PENDING_REWARD_XP] = 0
            preferences[PENDING_REWARD_FOOD] = 0
        }
    }

    val lastSeenLevel: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SEEN_LEVEL] ?: 1
        }

    suspend fun updateLastSeenLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SEEN_LEVEL] = level
        }
    }
}
