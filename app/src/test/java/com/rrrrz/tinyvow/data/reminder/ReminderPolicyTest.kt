package com.rrrrz.tinyvow.data.reminder

import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPolicyTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")

    @Test
    fun controlReminderOnlySendsInsideThresholdAndOncePerKey() {
        val key = ReminderPolicy.controlReminderKey(LocalDate.of(2026, 5, 25), LimitPeriod.DAILY, "group-a")

        assertFalse(
            ReminderPolicy.shouldSendControlRemaining(
                remainingMillis = 11 * 60_000L,
                thresholdMinutes = 10,
                reminderKey = key,
                sentKeys = emptySet(),
            )
        )
        assertTrue(
            ReminderPolicy.shouldSendControlRemaining(
                remainingMillis = 10 * 60_000L,
                thresholdMinutes = 10,
                reminderKey = key,
                sentKeys = emptySet(),
            )
        )
        assertFalse(
            ReminderPolicy.shouldSendControlRemaining(
                remainingMillis = 10 * 60_000L,
                thresholdMinutes = 10,
                reminderKey = key,
                sentKeys = setOf(key),
            )
        )
        assertFalse(
            ReminderPolicy.shouldSendControlRemaining(
                remainingMillis = 0L,
                thresholdMinutes = 10,
                reminderKey = key,
                sentKeys = emptySet(),
            )
        )
    }

    @Test
    fun controlReminderKeyChangesAcrossDatesAndPeriods() {
        val groupId = "group-a"

        assertEquals(
            "control:2026-05-25:DAILY:group-a",
            ReminderPolicy.controlReminderKey(LocalDate.of(2026, 5, 25), LimitPeriod.DAILY, groupId),
        )
        assertFalse(
            ReminderPolicy.controlReminderKey(LocalDate.of(2026, 5, 25), LimitPeriod.DAILY, groupId) ==
                ReminderPolicy.controlReminderKey(LocalDate.of(2026, 5, 26), LimitPeriod.DAILY, groupId)
        )
        assertFalse(
            ReminderPolicy.controlReminderKey(LocalDate.of(2026, 5, 25), LimitPeriod.DAILY, groupId) ==
                ReminderPolicy.controlReminderKey(LocalDate.of(2026, 5, 25), LimitPeriod.WEEKLY, groupId)
        )
    }

    @Test
    fun encourageReminderSendsOnlyWithIncompleteGroupsAndOncePerTime() {
        val key = ReminderPolicy.encourageReminderKey(LocalDate.of(2026, 5, 25), 8 * 60)

        assertFalse(ReminderPolicy.shouldSendEncourageIncomplete(0, key, emptySet()))
        assertTrue(ReminderPolicy.shouldSendEncourageIncomplete(2, key, emptySet()))
        assertFalse(ReminderPolicy.shouldSendEncourageIncomplete(2, key, setOf(key)))
    }

    @Test
    fun defaultReminderTimesAndFreeEffectiveSettingsUseDefaultTiming() {
        val settings = ReminderPolicy.effectiveSettings(
            enabled = true,
            controlRemainingReminderMinutes = 30,
            encourageReminderTimesMinutes = listOf(7 * 60),
            isProActive = false,
        )

        assertEquals(ManagedAppPreferences.DEFAULT_CONTROL_REMAINING_REMINDER_MINUTES, settings.controlRemainingReminderMinutes)
        assertEquals(ManagedAppPreferences.DEFAULT_ENCOURAGE_REMINDER_TIMES_MINUTES, settings.encourageReminderTimesMinutes)
    }

    @Test
    fun proEffectiveSettingsUseNormalizedCustomTiming() {
        val settings = ReminderPolicy.effectiveSettings(
            enabled = true,
            controlRemainingReminderMinutes = 30,
            encourageReminderTimesMinutes = listOf(20 * 60, 8 * 60, 8 * 60, -1),
            isProActive = true,
        )

        assertEquals(30, settings.controlRemainingReminderMinutes)
        assertEquals(listOf(8 * 60, 20 * 60), settings.encourageReminderTimesMinutes)
    }

    @Test
    fun nextEncourageReminderUsesNextConfiguredTimeOrTomorrow() {
        val now = LocalDateTime.of(2026, 5, 25, 7, 59)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        assertEquals(8 * 60, ReminderPolicy.nextEncourageTimeMinutes(now, listOf(8 * 60, 18 * 60), zoneId))

        val afterLast = LocalDateTime.of(2026, 5, 25, 20, 1)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        assertEquals(8 * 60, ReminderPolicy.nextEncourageTimeMinutes(afterLast, listOf(8 * 60, 18 * 60), zoneId))
    }
}
