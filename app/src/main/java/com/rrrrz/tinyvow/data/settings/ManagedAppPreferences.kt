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
import com.rrrrz.tinyvow.data.supermode.SuperModeStoredState
import com.rrrrz.tinyvow.data.time.BusinessDay
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
        val accessibilityServiceHeartbeatAtMillis = longPreferencesKey("accessibility_service_heartbeat_at_millis")
        val welcomeIntroCompleted = booleanPreferencesKey("welcome_intro_completed")
        val firstRunCoachmarkCompleted = booleanPreferencesKey("first_run_coachmark_completed")
        val selectedAppLanguage = stringPreferencesKey("selected_app_language")
        val dayBoundaryHour = intPreferencesKey("day_boundary_hour")
        val profileDisplayName = stringPreferencesKey("profile_display_name")
        val profileAvatarUri = stringPreferencesKey("profile_avatar_uri")
        val debugProExpiresAtMillis = longPreferencesKey("debug_pro_expires_at_millis")
        val superModeEnabled = booleanPreferencesKey("super_mode_enabled")
        val superModePasswordHash = stringPreferencesKey("super_mode_password_hash")
        val superModePasswordSalt = stringPreferencesKey("super_mode_password_salt")
        val superModeRecoveryQuestion = stringPreferencesKey("super_mode_recovery_question")
        val superModeRecoveryAnswerHash = stringPreferencesKey("super_mode_recovery_answer_hash")
        val superModeRecoveryAnswerSalt = stringPreferencesKey("super_mode_recovery_answer_salt")
        val superModeDebugBypassActive = booleanPreferencesKey("super_mode_debug_bypass_active")
        val superModeActive = booleanPreferencesKey("super_mode_active")
        val superModeLastActiveAtMillis = longPreferencesKey("super_mode_last_active_at_millis")
        val superModeWindowStartMinutes = intPreferencesKey("super_mode_window_start_minutes")
        val superModeWindowEndMinutes = intPreferencesKey("super_mode_window_end_minutes")
        val encryptedWeReadApiKey = stringPreferencesKey("encrypted_weread_api_key")
        val notificationRemindersEnabled = booleanPreferencesKey("notification_reminders_enabled")
        val controlRemainingReminderMinutes = intPreferencesKey("control_remaining_reminder_minutes")
        val encourageReminderTimesMinutes = stringPreferencesKey("encourage_reminder_times_minutes")
        val sentReminderKeys = stringSetPreferencesKey("sent_reminder_keys")
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
        val today =
            com.rrrrz.tinyvow.data.repository.ArchiveDateUtils.formatDate(
                com.rrrrz.tinyvow.data.repository.ArchiveDateUtils.localDateAt(
                    System.currentTimeMillis(),
                    java.time.ZoneId.systemDefault(),
                    BusinessDay.normalizeStartHour(preferences[Keys.dayBoundaryHour]),
                ),
            )
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

    val accessibilityServiceHeartbeatAtMillis: Flow<Long?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.accessibilityServiceHeartbeatAtMillis]
    }

    val welcomeIntroCompleted: Flow<Boolean> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.welcomeIntroCompleted] ?: false
    }

    val firstRunCoachmarkCompleted: Flow<Boolean> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.firstRunCoachmarkCompleted] ?: false
    }

    val selectedAppLanguage: Flow<AppLanguage> = context.managedAppDataStore.data.map { preferences ->
        AppLanguage.fromStorageValue(preferences[Keys.selectedAppLanguage])
    }

    val dayBoundaryHour: Flow<Int> = context.managedAppDataStore.data.map { preferences ->
        BusinessDay.normalizeStartHour(preferences[Keys.dayBoundaryHour])
    }

    val profileDisplayName: Flow<String?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.profileDisplayName]?.takeIf { it.isNotBlank() }
    }

    val profileAvatarUri: Flow<String?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.profileAvatarUri]?.takeIf { it.isNotBlank() }
    }

    val debugProExpiresAtMillis: Flow<Long?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.debugProExpiresAtMillis]
    }

    val superModeState: Flow<SuperModeStoredState> = context.managedAppDataStore.data.map { preferences ->
        SuperModeStoredState(
            enabled = preferences[Keys.superModeEnabled] ?: false,
            passwordHash = preferences[Keys.superModePasswordHash]?.takeIf { it.isNotBlank() },
            passwordSalt = preferences[Keys.superModePasswordSalt]?.takeIf { it.isNotBlank() },
            recoveryQuestion = preferences[Keys.superModeRecoveryQuestion]?.takeIf { it.isNotBlank() },
            recoveryAnswerHash = preferences[Keys.superModeRecoveryAnswerHash]?.takeIf { it.isNotBlank() },
            recoveryAnswerSalt = preferences[Keys.superModeRecoveryAnswerSalt]?.takeIf { it.isNotBlank() },
            debugBypassActive = preferences[Keys.superModeDebugBypassActive] ?: false,
            isActive = preferences[Keys.superModeActive] ?: false,
            lastActiveAtMillis = preferences[Keys.superModeLastActiveAtMillis],
            customWindowStartMinutes = preferences[Keys.superModeWindowStartMinutes],
            customWindowEndMinutes = preferences[Keys.superModeWindowEndMinutes],
        )
    }

    val encryptedWeReadApiKey: Flow<String?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.encryptedWeReadApiKey]?.takeIf { it.isNotBlank() }
    }

    val notificationRemindersEnabled: Flow<Boolean> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.notificationRemindersEnabled] ?: true
    }

    val controlRemainingReminderMinutes: Flow<Int> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.controlRemainingReminderMinutes] ?: DEFAULT_CONTROL_REMAINING_REMINDER_MINUTES
    }

    val encourageReminderTimesMinutes: Flow<List<Int>> = context.managedAppDataStore.data.map { preferences ->
        parseReminderTimes(preferences[Keys.encourageReminderTimesMinutes])
    }

    fun sharePosterModuleIds(tabKey: String): Flow<List<String>> =
        context.managedAppDataStore.data.map { preferences ->
            parseSharePosterModuleIds(preferences[sharePosterModuleKey(tabKey)])
        }

    suspend fun addUserPoints(points: Double) {
        context.managedAppDataStore.edit { preferences ->
            val currentTotal = preferences[Keys.userPoints] ?: 0.0
            preferences[Keys.userPoints] = currentTotal + points

            val today =
                com.rrrrz.tinyvow.data.repository.ArchiveDateUtils.formatDate(
                    com.rrrrz.tinyvow.data.repository.ArchiveDateUtils.localDateAt(
                        System.currentTimeMillis(),
                        java.time.ZoneId.systemDefault(),
                        BusinessDay.normalizeStartHour(preferences[Keys.dayBoundaryHour]),
                    ),
                )
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

    suspend fun touchAccessibilityServiceHeartbeat(nowMillis: Long = System.currentTimeMillis()) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.accessibilityServiceHeartbeatAtMillis] = nowMillis
        }
    }

    suspend fun setWelcomeIntroCompleted(completed: Boolean) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.welcomeIntroCompleted] = completed
        }
    }

    suspend fun setFirstRunCoachmarkCompleted(completed: Boolean) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.firstRunCoachmarkCompleted] = completed
        }
    }

    suspend fun setSelectedAppLanguage(language: AppLanguage) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.selectedAppLanguage] = language.storageValue
        }
    }

    suspend fun setDayBoundaryHour(hour: Int) {
        val normalized = BusinessDay.normalizeStartHour(hour)
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.dayBoundaryHour] = normalized
        }
        BusinessDay.updateCachedStartHour(normalized)
    }

    suspend fun setProfileDisplayName(displayName: String?) {
        context.managedAppDataStore.edit { preferences ->
            val normalized = displayName?.trim().orEmpty()
            if (normalized.isBlank()) {
                preferences.remove(Keys.profileDisplayName)
            } else {
                preferences[Keys.profileDisplayName] = normalized
            }
        }
    }

    suspend fun setProfileAvatarUri(uri: String?) {
        context.managedAppDataStore.edit { preferences ->
            val normalized = uri?.trim().orEmpty()
            if (normalized.isBlank()) {
                preferences.remove(Keys.profileAvatarUri)
            } else {
                preferences[Keys.profileAvatarUri] = normalized
            }
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

    suspend fun saveSuperModeCredentials(
        passwordHash: String,
        passwordSalt: String,
        recoveryQuestion: String,
        recoveryAnswerHash: String,
        recoveryAnswerSalt: String,
    ) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.superModeEnabled] = false
            preferences[Keys.superModePasswordHash] = passwordHash
            preferences[Keys.superModePasswordSalt] = passwordSalt
            preferences[Keys.superModeRecoveryQuestion] = recoveryQuestion
            preferences[Keys.superModeRecoveryAnswerHash] = recoveryAnswerHash
            preferences[Keys.superModeRecoveryAnswerSalt] = recoveryAnswerSalt
            preferences[Keys.superModeActive] = false
            preferences.remove(Keys.superModeLastActiveAtMillis)
        }
    }

    suspend fun setSuperModeActive(
        active: Boolean,
        lastActiveAtMillis: Long?,
        debugBypassActive: Boolean = false,
    ) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.superModeActive] = active
            preferences[Keys.superModeDebugBypassActive] = debugBypassActive
            if (lastActiveAtMillis == null) {
                preferences.remove(Keys.superModeLastActiveAtMillis)
            } else {
                preferences[Keys.superModeLastActiveAtMillis] = lastActiveAtMillis
            }
        }
    }

    suspend fun touchSuperMode(lastActiveAtMillis: Long) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.superModeActive] = true
            preferences[Keys.superModeLastActiveAtMillis] = lastActiveAtMillis
        }
    }

    suspend fun activateSuperModeDebugBypass(lastActiveAtMillis: Long) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.superModeActive] = true
            preferences[Keys.superModeDebugBypassActive] = true
            preferences[Keys.superModeLastActiveAtMillis] = lastActiveAtMillis
        }
    }

    suspend fun setSuperModeWindow(
        startMinutes: Int,
        endMinutes: Int,
    ) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.superModeWindowStartMinutes] = startMinutes
            preferences[Keys.superModeWindowEndMinutes] = endMinutes
        }
    }

    suspend fun setSuperModeEnabled(enabled: Boolean) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.superModeEnabled] = enabled
            if (!enabled) {
                preferences[Keys.superModeActive] = false
                preferences[Keys.superModeDebugBypassActive] = false
                preferences.remove(Keys.superModeLastActiveAtMillis)
            }
        }
    }

    suspend fun clearSuperMode() {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.superModeEnabled] = false
            preferences[Keys.superModeActive] = false
            preferences.remove(Keys.superModePasswordHash)
            preferences.remove(Keys.superModePasswordSalt)
            preferences.remove(Keys.superModeRecoveryQuestion)
            preferences.remove(Keys.superModeRecoveryAnswerHash)
            preferences.remove(Keys.superModeRecoveryAnswerSalt)
            preferences.remove(Keys.superModeDebugBypassActive)
            preferences.remove(Keys.superModeLastActiveAtMillis)
            preferences.remove(Keys.superModeWindowStartMinutes)
            preferences.remove(Keys.superModeWindowEndMinutes)
        }
    }

    suspend fun setEncryptedWeReadApiKey(value: String?) {
        context.managedAppDataStore.edit { preferences ->
            if (value.isNullOrBlank()) {
                preferences.remove(Keys.encryptedWeReadApiKey)
            } else {
                preferences[Keys.encryptedWeReadApiKey] = value
            }
        }
    }

    suspend fun setNotificationRemindersEnabled(enabled: Boolean) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.notificationRemindersEnabled] = enabled
        }
    }

    suspend fun setControlRemainingReminderMinutes(minutes: Int) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.controlRemainingReminderMinutes] =
                minutes.coerceIn(MIN_CONTROL_REMAINING_REMINDER_MINUTES, MAX_CONTROL_REMAINING_REMINDER_MINUTES)
        }
    }

    suspend fun setEncourageReminderTimesMinutes(times: List<Int>) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.encourageReminderTimesMinutes] = encodeReminderTimes(times)
        }
    }

    suspend fun setSharePosterModuleIds(
        tabKey: String,
        moduleIds: List<String>,
    ) {
        context.managedAppDataStore.edit { preferences ->
            preferences[sharePosterModuleKey(tabKey)] = encodeSharePosterModuleIds(moduleIds)
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

    suspend fun getDayBoundaryHourOnce(): Int {
        return dayBoundaryHour.first()
    }

    suspend fun getSuperModeStateOnce(): SuperModeStoredState {
        return superModeState.first()
    }

    suspend fun getDebugProExpiresAtMillisOnce(): Long? {
        return debugProExpiresAtMillis.first()
    }

    suspend fun getEncryptedWeReadApiKeyOnce(): String? {
        return encryptedWeReadApiKey.first()
    }

    suspend fun getNotificationRemindersEnabledOnce(): Boolean {
        return notificationRemindersEnabled.first()
    }

    suspend fun getControlRemainingReminderMinutesOnce(): Int {
        return controlRemainingReminderMinutes.first()
    }

    suspend fun getEncourageReminderTimesMinutesOnce(): List<Int> {
        return encourageReminderTimesMinutes.first()
    }

    suspend fun getSharePosterModuleIdsOnce(tabKey: String): List<String> {
        return sharePosterModuleIds(tabKey).first()
    }

    suspend fun getSentReminderKeysOnce(): Set<String> {
        return context.managedAppDataStore.data.map { preferences ->
            preferences[Keys.sentReminderKeys].orEmpty()
        }.first()
    }

    suspend fun addSentReminderKey(key: String) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.sentReminderKeys] = preferences[Keys.sentReminderKeys].orEmpty() + key
        }
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
                            backgroundColor = item.getLong("backgroundColor").toInt(),
                            surfaceColor = item.getLong("surfaceColor").toInt(),
                            textColor = item.getLong("textColor").toInt(),
                            mutedTextColor = item.getLong("mutedTextColor").toInt(),
                            primaryColor = item.getLong("primaryColor").toInt(),
                            controlColor = item.getLong("controlColor").toInt(),
                            encourageColor = item.getLong("encourageColor").toInt(),
                            baseColor = item.getLong("baseColor").toInt(),
                            neutralAccentColor = item.getLong("neutralAccentColor").toInt(),
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
                    .put("backgroundColor", theme.backgroundColor.toLong())
                    .put("surfaceColor", theme.surfaceColor.toLong())
                    .put("textColor", theme.textColor.toLong())
                    .put("mutedTextColor", theme.mutedTextColor.toLong())
                    .put("primaryColor", theme.primaryColor.toLong())
                    .put("controlColor", theme.controlColor.toLong())
                    .put("encourageColor", theme.encourageColor.toLong())
                    .put("baseColor", theme.baseColor.toLong())
                    .put("neutralAccentColor", theme.neutralAccentColor.toLong())
            )
        }
        return array.toString()
    }

    private fun parseReminderTimes(value: String?): List<Int> {
        if (value.isNullOrBlank()) return DEFAULT_ENCOURAGE_REMINDER_TIMES_MINUTES
        return value
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 0 until MINUTES_PER_DAY }
            .distinct()
            .sorted()
            .ifEmpty { DEFAULT_ENCOURAGE_REMINDER_TIMES_MINUTES }
    }

    private fun encodeReminderTimes(times: List<Int>): String =
        times
            .filter { it in 0 until MINUTES_PER_DAY }
            .distinct()
            .sorted()
            .joinToString(",")

    private fun sharePosterModuleKey(tabKey: String) =
        stringPreferencesKey("share_poster_modules_$tabKey")

    private fun parseSharePosterModuleIds(value: String?): List<String> =
        value
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            .orEmpty()

    private fun encodeSharePosterModuleIds(moduleIds: List<String>): String =
        moduleIds
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(",")

    companion object {
        const val DEFAULT_CONTROL_REMAINING_REMINDER_MINUTES = 10
        const val MIN_CONTROL_REMAINING_REMINDER_MINUTES = 1
        const val MAX_CONTROL_REMAINING_REMINDER_MINUTES = 120
        val DEFAULT_ENCOURAGE_REMINDER_TIMES_MINUTES = listOf(8 * 60, 18 * 60, 20 * 60)
        private const val MINUTES_PER_DAY = 24 * 60
    }
}
