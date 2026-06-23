package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyCheckInEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyCheckInMonthStateTest {
    @Test
    fun buildDailyCheckInMonthState_marksCheckInAndArchivedBadges() {
        val state =
            buildDailyCheckInMonthState(
                month = YearMonth.of(2026, 6),
                today = LocalDate.of(2026, 6, 10),
                checkIns = listOf(checkIn("2026-06-01"), checkIn("2026-06-10")),
                archives = listOf(archive("2026-06-01")),
                groupArchives =
                    listOf(
                        groupArchive("2026-06-01", "control-1", GroupType.CONTROL, completed = true),
                        groupArchive("2026-06-01", "control-2", GroupType.CONTROL, completed = true),
                        groupArchive("2026-06-01", "encourage-1", GroupType.ENCOURAGE, completed = true),
                    ),
                bufferCardCount = 3,
            )

        val juneFirst = state.days.first { it.date == LocalDate.of(2026, 6, 1) }
        val today = state.days.first { it.date == LocalDate.of(2026, 6, 10) }
        assertTrue(juneFirst.checkedIn)
        assertTrue(juneFirst.allControlKept)
        assertTrue(juneFirst.encourageCompleted)
        assertTrue(juneFirst.hasArchivedSignals)
        assertTrue(today.checkedIn)
        assertFalse(today.allControlKept)
        assertFalse(today.encourageCompleted)
        assertEquals(2, state.checkedInDays)
        assertEquals(1, state.allControlKeptDays)
        assertEquals(1, state.encourageCompletedDays)
        assertEquals(3, state.bufferCardCount)
    }

    @Test
    fun buildDailyCheckInMonthState_doesNotTreatEmptyGroupsAsCompleted() {
        val state =
            buildDailyCheckInMonthState(
                month = YearMonth.of(2026, 6),
                today = LocalDate.of(2026, 6, 10),
                checkIns = emptyList(),
                archives = listOf(archive("2026-06-02")),
                groupArchives = emptyList(),
                bufferCardCount = 0,
            )

        val day = state.days.first { it.date == LocalDate.of(2026, 6, 2) }
        assertFalse(day.allControlKept)
        assertFalse(day.encourageCompleted)
        assertTrue(day.hasArchivedSignals)
    }

    @Test
    fun buildDailyCheckInMonthState_requiresAllControlGroupsCompleted() {
        val state =
            buildDailyCheckInMonthState(
                month = YearMonth.of(2026, 6),
                today = LocalDate.of(2026, 6, 10),
                checkIns = emptyList(),
                archives = listOf(archive("2026-06-03")),
                groupArchives =
                    listOf(
                        groupArchive("2026-06-03", "control-1", GroupType.CONTROL, completed = true),
                        groupArchive("2026-06-03", "control-2", GroupType.CONTROL, completed = false),
                    ),
                bufferCardCount = 0,
            )

        assertFalse(state.days.first { it.date == LocalDate.of(2026, 6, 3) }.allControlKept)
    }

    private fun checkIn(date: String): DailyCheckInEntity =
        DailyCheckInEntity(
            id = "checkin-$date",
            checkInDate = date,
            checkedInAt = 0L,
            rewardBuiltinKey = EMERGENCY_UNLOCK_REWARD_KEY,
            rewardInventoryId = "inventory",
        )

    private fun archive(date: String): DailyArchiveEntity =
        DailyArchiveEntity(
            id = date,
            archiveDate = date,
            dayStartAt = 0L,
            dayEndAt = 0L,
            createdAt = 0L,
            updatedAt = 0L,
        )

    private fun groupArchive(
        date: String,
        groupId: String,
        groupType: GroupType,
        completed: Boolean,
    ): DailyGroupArchiveEntity =
        DailyGroupArchiveEntity(
            id = "$date-$groupId",
            archiveDate = date,
            groupId = groupId,
            groupName = groupId,
            groupType = groupType,
            limitPeriod = LimitPeriod.DAILY,
            limitMinutes = 60,
            completed = completed,
            createdAt = 0L,
            updatedAt = 0L,
        )
}
