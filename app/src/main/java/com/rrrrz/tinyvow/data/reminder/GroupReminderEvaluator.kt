package com.rrrrz.tinyvow.data.reminder

import android.content.Context
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.AppGroupEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.notification.TinyVowNotifier
import com.rrrrz.tinyvow.data.repository.parseRewardPayload
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.MergedUsageRepository
import com.rrrrz.tinyvow.i18n.AppText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroupReminderEvaluator(
    context: Context,
    private val nowMillisProvider: () -> Long = { System.currentTimeMillis() },
) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getDatabase(appContext)
    private val groupDao = database.appGroupDao()
    private val crossRefDao = database.crossRefDao()
    private val bonusTimeDao = database.bonusTimeDao()
    private val activeRewardEffectDao = database.activeRewardEffectDao()
    private val usageRepository = MergedUsageRepository(appContext)
    private val preferences = ManagedAppPreferences(appContext)
    private val notifier = TinyVowNotifier(appContext)
    private val zoneId = ZoneId.systemDefault()

    suspend fun sendDueControlRemainingReminders(settings: NotificationReminderSettings) {
        if (!settings.enabled) return
        withContext(Dispatchers.IO) {
            val nowMillis = nowMillisProvider()
            val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
            var sentKeys = preferences.getSentReminderKeysOnce()
            groupDao
                .getAllGroupsSync()
                .asSequence()
                .filter { it.type == GroupType.CONTROL }
                .forEach { group ->
                    val packages = crossRefDao.getPackageNamesForGroupSync(group.id)
                    if (packages.isEmpty()) return@forEach

                    val activeEffects = activeRewardEffectDao.getActiveForGroup(group.id, nowMillis)
                    if (activeEffects.any { it.effectType == RewardType.PERIOD_PASS }) return@forEach

                    val effectiveLimitMillis = effectiveControlLimitMillis(group, nowMillis, activeEffects)
                    val usedMillis =
                        packages.sumOf { packageName ->
                            usageRepository.getUsageInPeriod(packageName, group.limitPeriod, GroupType.CONTROL)
                        }
                    val remainingMillis = effectiveLimitMillis - usedMillis
                    val key = ReminderPolicy.controlReminderKey(today, group.limitPeriod, group.id)
                    if (
                        ReminderPolicy.shouldSendControlRemaining(
                            remainingMillis = remainingMillis,
                            thresholdMinutes = settings.controlRemainingReminderMinutes,
                            reminderKey = key,
                            sentKeys = sentKeys,
                        )
                    ) {
                        notifier.notifyControlRemaining(
                            groupId = group.id,
                            groupName = group.name,
                            remainingText = formatDuration(remainingMillis),
                        )
                        preferences.addSentReminderKey(key)
                        sentKeys = sentKeys + key
                    }
                }
        }
    }

    suspend fun sendDueEncourageReminder(
        settings: NotificationReminderSettings,
        scheduledTimeMinutes: Int,
    ) {
        if (!settings.enabled) return
        withContext(Dispatchers.IO) {
            val nowMillis = nowMillisProvider()
            val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
            val key = ReminderPolicy.encourageReminderKey(today, scheduledTimeMinutes)
            val sentKeys = preferences.getSentReminderKeysOnce()
            val incompleteGroups =
                groupDao
                    .getAllGroupsSync()
                    .filter { it.type == GroupType.ENCOURAGE }
                    .filter { group ->
                        val packages = crossRefDao.getPackageNamesForGroupSync(group.id)
                        if (packages.isEmpty()) {
                            false
                        } else {
                            val usedMillis =
                                packages.sumOf { packageName ->
                                    usageRepository.getUsageInPeriod(packageName, group.limitPeriod, GroupType.ENCOURAGE)
                                }
                            usedMillis < group.limitMinutes * 60_000L
                        }
                    }

            if (
                ReminderPolicy.shouldSendEncourageIncomplete(
                    incompleteCount = incompleteGroups.size,
                    reminderKey = key,
                    sentKeys = sentKeys,
                )
            ) {
                notifier.notifyEncourageIncomplete(
                    timeText = formatClockMinutes(scheduledTimeMinutes),
                    groupNames = incompleteGroups.map { it.name },
                )
                preferences.addSentReminderKey(key)
            }
        }
    }

    private suspend fun effectiveControlLimitMillis(
        group: AppGroupEntity,
        nowMillis: Long,
        activeEffects: List<com.rrrrz.tinyvow.data.db.ActiveRewardEffectEntity>,
    ): Long {
        val activeRewardMinutes =
            activeEffects
                .filter { it.effectType == RewardType.TIME_ADD || it.effectType == RewardType.EMERGENCY_UNLOCK }
                .sumOf { parseRewardPayload(it.payloadJson).minutes }
        val legacyBonusMinutes = bonusTimeDao.getActiveBonusTimeForGroupSync(group.id, nowMillis).sumOf { it.extraMinutes }
        return (group.limitMinutes + activeRewardMinutes + legacyBonusMinutes).coerceAtLeast(1) * 60_000L
    }

    private fun formatDuration(durationMillis: Long): String {
        val totalSeconds = durationMillis / 1_000
        val totalMinutes = durationMillis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val seconds = totalSeconds % 60
        return when {
            hours > 0 && minutes > 0 -> AppText.t("duration_value_h_value_min", hours, minutes)
            hours > 0 -> AppText.t("duration_value_h", hours)
            totalMinutes > 0L -> AppText.t("duration_value_min", minutes)
            totalSeconds > 0L -> AppText.t("duration_value_sec", seconds)
            else -> AppText.t("duration_0_sec")
        }
    }

    private fun formatClockMinutes(minutes: Int): String =
        "%02d:%02d".format(minutes / 60, minutes % 60)
}

suspend fun currentNotificationReminderSettings(
    preferences: ManagedAppPreferences,
    isProActive: Boolean,
): NotificationReminderSettings =
    ReminderPolicy.effectiveSettings(
        enabled = preferences.getNotificationRemindersEnabledOnce(),
        controlRemainingReminderMinutes = preferences.getControlRemainingReminderMinutesOnce(),
        encourageReminderTimesMinutes = preferences.getEncourageReminderTimesMinutesOnce(),
        isProActive = isProActive,
    )
