package com.rrrrz.tinyvow.data.settings

import com.rrrrz.tinyvow.i18n.AppText

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rrrrz.tinyvow.ui.theme.DefaultThemeSeed
import com.rrrrz.tinyvow.i18n.AppLanguage
import com.rrrrz.tinyvow.ui.theme.ThemeSeed
import com.rrrrz.tinyvow.ui.theme.legacyCustomTheme
import com.rrrrz.tinyvow.ui.theme.legacyThemeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.managedAppDataStore by preferencesDataStore(name = "managed_app_preferences")

class ManagedAppPreferences(
    private val context: Context,
) {
    private object Keys {
        val selectedPackageName = stringPreferencesKey("selected_package_name")
        val userPoints = doublePreferencesKey("user_points")
        val lastSummaryShownDate = stringPreferencesKey("last_summary_shown_date")
        val selectedTheme = intPreferencesKey("selected_theme")
        val customSeedColor = intPreferencesKey("custom_seed_color")
        val customSeedColorEnabled = booleanPreferencesKey("custom_seed_color_enabled")
        val selectedThemeId = stringPreferencesKey("selected_theme_id")
        val customThemesJson = stringPreferencesKey("custom_themes_json")
        val todayPoints = doublePreferencesKey("today_points")
        val lastPointsResetDate = stringPreferencesKey("last_points_reset_date")
        val dismissedPermissionPrompts = stringSetPreferencesKey("dismissed_permission_prompts")
        val usageAccessDisclosureAccepted = booleanPreferencesKey("usage_access_disclosure_accepted")
        val accessibilityDisclosureAccepted = booleanPreferencesKey("accessibility_disclosure_accepted")
        val selectedAppLanguage = stringPreferencesKey("selected_app_language")
        val debugProExpiresAtMillis = longPreferencesKey("debug_pro_expires_at_millis")
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

    val customSeedColor: Flow<Int?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.customSeedColor]
    }

    val customSeedColorEnabled: Flow<Boolean> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.customSeedColorEnabled] ?: false
    }

    val selectedThemeId: Flow<String> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.selectedThemeId]
            ?: if ((preferences[Keys.customSeedColorEnabled] ?: false) && preferences[Keys.customSeedColor] != null) {
                "custom_legacy_seed"
            } else {
                legacyThemeId(preferences[Keys.selectedTheme] ?: 0)
            }
    }

    val customThemes: Flow<List<ThemeSeed>> = context.managedAppDataStore.data.map { preferences ->
        val customThemes = parseCustomThemes(preferences[Keys.customThemesJson])
        val legacyCustom = preferences[Keys.customSeedColor]
            ?.takeIf { preferences[Keys.customSeedColorEnabled] ?: false }
            ?.let(::legacyCustomTheme)
        if (legacyCustom == null || customThemes.any { it.id == legacyCustom.id }) {
            customThemes
        } else {
            listOf(legacyCustom) + customThemes
        }
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

    val usageAccessDisclosureAccepted: Flow<Boolean> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.usageAccessDisclosureAccepted] ?: false
    }

    val accessibilityDisclosureAccepted: Flow<Boolean> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.accessibilityDisclosureAccepted] ?: false
    }

    val selectedAppLanguage: Flow<AppLanguage> = context.managedAppDataStore.data.map { preferences ->
        AppLanguage.fromStorageValue(preferences[Keys.selectedAppLanguage])
    }

    val debugProExpiresAtMillis: Flow<Long?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.debugProExpiresAtMillis]
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

    suspend fun setCustomSeedColor(color: Int) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.customSeedColor] = color
        }
    }

    suspend fun setCustomSeedColorEnabled(enabled: Boolean) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.customSeedColorEnabled] = enabled
        }
    }

    suspend fun setSelectedThemeId(themeId: String) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.selectedThemeId] = themeId
            preferences[Keys.customSeedColorEnabled] = false
        }
    }

    suspend fun upsertCustomTheme(theme: ThemeSeed) {
        context.managedAppDataStore.edit { preferences ->
            val current = parseCustomThemes(preferences[Keys.customThemesJson])
            val normalized = theme.copy(isCustom = true)
            val next = if (current.any { it.id == normalized.id }) {
                current.map { if (it.id == normalized.id) normalized else it }
            } else {
                current + normalized
            }
            preferences[Keys.customThemesJson] = encodeCustomThemes(next)
            preferences[Keys.selectedThemeId] = normalized.id
            preferences[Keys.customSeedColorEnabled] = false
        }
    }

    suspend fun deleteCustomTheme(themeId: String) {
        context.managedAppDataStore.edit { preferences ->
            val next = parseCustomThemes(preferences[Keys.customThemesJson]).filterNot { it.id == themeId }
            preferences[Keys.customThemesJson] = encodeCustomThemes(next)
            if (preferences[Keys.selectedThemeId] == themeId) {
                preferences[Keys.selectedThemeId] = DefaultThemeSeed.id
            }
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

    suspend fun setUsageAccessDisclosureAccepted(accepted: Boolean) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.usageAccessDisclosureAccepted] = accepted
        }
    }

    suspend fun setAccessibilityDisclosureAccepted(accepted: Boolean) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.accessibilityDisclosureAccepted] = accepted
        }
    }

    suspend fun setSelectedAppLanguage(language: AppLanguage) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.selectedAppLanguage] = language.storageValue
        }
    }

    suspend fun extendDebugPro(durationDays: Int, currentExpiresAtMillis: Long? = null) {
        val now = System.currentTimeMillis()
        val durationMillis = durationDays * 86_400_000L
        context.managedAppDataStore.edit { preferences ->
            val currentDebugExpiresAt = preferences[Keys.debugProExpiresAtMillis]
            preferences[Keys.debugProExpiresAtMillis] =
                maxOf(now, currentDebugExpiresAt ?: 0L, currentExpiresAtMillis ?: 0L) + durationMillis
        }
    }

    suspend fun clearDebugPro() {
        context.managedAppDataStore.edit { preferences ->
            preferences.remove(Keys.debugProExpiresAtMillis)
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

    suspend fun getSelectedAppLanguageOnce(): AppLanguage {
        return selectedAppLanguage.first()
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

    suspend fun clearAll() {
        context.managedAppDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun parseCustomThemes(json: String?): List<ThemeSeed> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue
                    val name = item.optString("name").takeIf { it.isNotBlank() } ?: AppText.t("settings_custom_theme")
                    add(
                        ThemeSeed(
                            id = id,
                            name = name,
                            controlColor = item.getLong("controlColor").toInt(),
                            encourageColor = item.getLong("encourageColor").toInt(),
                            baseColor = item.getLong("baseColor").toInt(),
                            isCustom = true,
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeCustomThemes(themes: List<ThemeSeed>): String {
        val array = JSONArray()
        themes.filter { it.isCustom }.forEach { theme ->
            array.put(
                JSONObject()
                    .put("id", theme.id)
                    .put("name", theme.name)
                    .put("controlColor", theme.controlColor.toLong())
                    .put("encourageColor", theme.encourageColor.toLong())
                    .put("baseColor", theme.baseColor.toLong())
            )
        }
        return array.toString()
    }
}
