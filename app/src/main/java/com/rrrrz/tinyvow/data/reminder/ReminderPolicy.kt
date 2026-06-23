package com.rrrrz.tinyvow.data.reminder

import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class NotificationReminderSettings(
    val enabled: Boolean,
    val controlRemainingReminderMinutes: Int,
    val encourageReminderTimesMinutes: List<Int>,
)

data class EncourageProgressReminder(
    val groupName: String,
    val remainingMinutes: Int,
)

object ReminderPolicy {
    fun effectiveSettings(
        enabled: Boolean,
        controlRemainingReminderMinutes: Int,
        encourageReminderTimesMinutes: List<Int>,
        isProActive: Boolean,
    ): NotificationReminderSettings {
        return NotificationReminderSettings(
            enabled = enabled,
            controlRemainingReminderMinutes =
                if (isProActive) {
                    controlRemainingReminderMinutes.coerceIn(
                        ManagedAppPreferences.MIN_CONTROL_REMAINING_REMINDER_MINUTES,
                        ManagedAppPreferences.MAX_CONTROL_REMAINING_REMINDER_MINUTES,
                    )
                } else {
                    ManagedAppPreferences.DEFAULT_CONTROL_REMAINING_REMINDER_MINUTES
                },
            encourageReminderTimesMinutes =
                if (isProActive) normalizeReminderTimes(encourageReminderTimesMinutes) else
                    ManagedAppPreferences.DEFAULT_ENCOURAGE_REMINDER_TIMES_MINUTES,
        )
    }

    fun normalizeReminderTimes(times: List<Int>): List<Int> =
        times
            .filter { it in 0 until MINUTES_PER_DAY }
            .distinct()
            .sorted()
            .ifEmpty { ManagedAppPreferences.DEFAULT_ENCOURAGE_REMINDER_TIMES_MINUTES }

    fun shouldSendControlRemaining(
        remainingMillis: Long,
        thresholdMinutes: Int,
        reminderKey: String,
        sentKeys: Set<String>,
    ): Boolean =
        remainingMillis > 0L &&
            remainingMillis <= thresholdMinutes.coerceAtLeast(1) * MILLIS_PER_MINUTE &&
            reminderKey !in sentKeys

    fun shouldSendEncourageIncomplete(
        incompleteCount: Int,
        reminderKey: String,
        sentKeys: Set<String>,
    ): Boolean = incompleteCount > 0 && reminderKey !in sentKeys

    fun encourageRemainingMinutes(
        usedMillis: Long,
        targetMinutes: Int,
    ): Int {
        val remainingMillis = targetMinutes.coerceAtLeast(0) * MILLIS_PER_MINUTE - usedMillis
        if (remainingMillis <= 0L) return 0
        return ((remainingMillis + MILLIS_PER_MINUTE - 1L) / MILLIS_PER_MINUTE).toInt().coerceAtLeast(1)
    }

    fun controlReminderKey(
        date: LocalDate,
        period: LimitPeriod,
        groupId: String,
    ): String = "control:${date}:$period:$groupId"

    fun encourageReminderKey(
        date: LocalDate,
        timeMinutes: Int,
    ): String = "encourage:${date}:${timeMinutes.coerceIn(0, MINUTES_PER_DAY - 1)}"

    fun encourageCompletedReminderKey(
        date: LocalDate,
        groupId: String,
    ): String = "encourage_completed:$date:$groupId"

    fun nextEncourageDelayMillis(
        nowMillis: Long,
        reminderTimesMinutes: List<Int>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long {
        val now = java.time.Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val targetMinute = nextEncourageTimeMinutes(nowMillis, reminderTimesMinutes, zoneId)
        val nowMinute = minuteOfDay(now.toLocalTime())
        val targetDate = if (targetMinute > nowMinute) now.toLocalDate() else now.toLocalDate().plusDays(1)
        val targetMillis =
            targetDate
                .atTime(LocalTime.of(targetMinute / 60, targetMinute % 60))
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
        return (targetMillis - nowMillis).coerceAtLeast(MIN_WORK_DELAY_MILLIS)
    }

    fun nextEncourageTimeMinutes(
        nowMillis: Long,
        reminderTimesMinutes: List<Int>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Int {
        val times = normalizeReminderTimes(reminderTimesMinutes)
        val now = java.time.Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val nowMinute = minuteOfDay(now.toLocalTime())
        return times.firstOrNull { it > nowMinute } ?: times.first()
    }

    private fun minuteOfDay(time: LocalTime): Int = time.hour * 60 + time.minute

    fun scheduledTimeForCurrentMinute(
        nowMillis: Long,
        reminderTimesMinutes: List<Int>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Int? {
        val now = java.time.Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val nowMinute = minuteOfDay(now.toLocalTime())
        return normalizeReminderTimes(reminderTimesMinutes).firstOrNull { it == nowMinute }
    }

    private const val MINUTES_PER_DAY = 24 * 60
    private const val MILLIS_PER_MINUTE = 60_000L
    private const val MIN_WORK_DELAY_MILLIS = 1_000L
}
