package com.rrrrz.tinyvow.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.managedAppDataStore by preferencesDataStore(name = "managed_app_preferences")

class ManagedAppPreferences(
    private val context: Context,
) {
    private object Keys {
        val selectedPackageName = stringPreferencesKey("selected_package_name")
        val userPoints = doublePreferencesKey("user_points")
        val lastSummaryShownDate = stringPreferencesKey("last_summary_shown_date")
        val selectedTheme = intPreferencesKey("selected_theme")
        val todayPoints = doublePreferencesKey("today_points")
        val lastPointsResetDate = stringPreferencesKey("last_points_reset_date")
        val dismissedPermissionPrompts = stringSetPreferencesKey("dismissed_permission_prompts")
    }

    val selectedPackageName: Flow<String?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.selectedPackageName]
    }

    val userPoints: Flow<Double> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.userPoints] ?: 0.0
    }

    val lastSummaryShownDate: Flow<String?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.lastSummaryShownDate]
    }

    val selectedTheme: Flow<Int> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.selectedTheme] ?: 0
    }

    val todayPoints: Flow<Double> = context.managedAppDataStore.data.map { preferences ->
        val lastReset = preferences[Keys.lastPointsResetDate]
        val today = java.time.LocalDate.now().toString()
        if (lastReset == today) {
            preferences[Keys.todayPoints] ?: 0.0
        } else {
            0.0
        }
    }

    val dismissedPermissionPrompts: Flow<Set<String>> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.dismissedPermissionPrompts].orEmpty()
    }

    suspend fun addUserPoints(points: Double) {
        context.managedAppDataStore.edit { preferences ->
            val currentTotal = preferences[Keys.userPoints] ?: 0.0
            preferences[Keys.userPoints] = currentTotal + points
            
            val today = java.time.LocalDate.now().toString()
            val lastReset = preferences[Keys.lastPointsResetDate]
            if (lastReset == today) {
                val currentToday = preferences[Keys.todayPoints] ?: 0.0
                preferences[Keys.todayPoints] = currentToday + points
            } else {
                preferences[Keys.lastPointsResetDate] = today
                preferences[Keys.todayPoints] = points
            }
        }
    }

    suspend fun setLastSummaryShownDate(date: String) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.lastSummaryShownDate] = date
        }
    }

    suspend fun setSelectedTheme(theme: Int) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.selectedTheme] = theme
        }
    }

    fun dailyLimitMinutes(packageName: String): Flow<Int?> = context.managedAppDataStore.data.map { preferences ->
        preferences[intPreferencesKey("daily_limit_minutes_$packageName")]
    }

    suspend fun setSelectedPackageName(packageName: String) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.selectedPackageName] = packageName
        }
    }

    val isAutoStartDismissed: Flow<Boolean> = context.managedAppDataStore.data.map { preferences ->
        preferences[booleanPreferencesKey("is_autostart_dismissed")] ?: false
    }

    suspend fun setAutoStartDismissed(dismissed: Boolean) {
        context.managedAppDataStore.edit { preferences ->
            preferences[booleanPreferencesKey("is_autostart_dismissed")] = dismissed
        }
    }

    suspend fun setPermissionPromptDismissed(promptId: String, dismissed: Boolean) {
        context.managedAppDataStore.edit { preferences ->
            val current = preferences[Keys.dismissedPermissionPrompts].orEmpty()
            preferences[Keys.dismissedPermissionPrompts] = if (dismissed) {
                current + promptId
            } else {
                current - promptId
            }
        }
    }

    suspend fun setDailyLimitMinutes(packageName: String, minutes: Int) {
        context.managedAppDataStore.edit { preferences ->
            preferences[intPreferencesKey("daily_limit_minutes_$packageName")] = minutes
        }
    }

    suspend fun clearDailyLimitMinutes(packageName: String) {
        context.managedAppDataStore.edit { preferences ->
            preferences.remove(intPreferencesKey("daily_limit_minutes_$packageName"))
        }
    }

    suspend fun getSelectedPackageNameOnce(): String? {
        return selectedPackageName.first()
    }

    suspend fun getDailyLimitMinutesOnce(packageName: String): Int? {
        return dailyLimitMinutes(packageName).first()
    }

    suspend fun getLastReminderDateOnce(packageName: String): String? {
        return context.managedAppDataStore.data.map { preferences ->
            preferences[stringPreferencesKey("last_reminder_date_$packageName")]
        }.first()
    }

    suspend fun setLastReminderDate(packageName: String, value: String) {
        context.managedAppDataStore.edit { preferences ->
            preferences[stringPreferencesKey("last_reminder_date_$packageName")] = value
        }
    }

    suspend fun clearLastReminderDate(packageName: String) {
        context.managedAppDataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey("last_reminder_date_$packageName"))
        }
    }
}
