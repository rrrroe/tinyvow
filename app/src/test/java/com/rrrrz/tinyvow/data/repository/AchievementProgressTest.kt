package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.StreakShieldPendingEntity
import com.rrrrz.tinyvow.data.db.StreakShieldPendingStatus
import com.rrrrz.tinyvow.data.db.StreakShieldTarget
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementProgressTest {
    @Test
    fun calculateAchievementProgress_usesCumulativeLedgerTotals() {
        val progress =
            calculateAchievementProgress(
                earnedPointsTotal = 1_500.0,
                redeemedPointsTotal = 1_000.0,
                archives = emptyList(),
            )

        assertEquals(1_500.0, progress.earnedPointsTotal, 0.0001)
        assertEquals(1_000.0, progress.redeemedPointsTotal, 0.0001)
    }

    @Test
    fun calculateAchievementProgress_countsCompletedArchiveDays() {
        val progress =
            calculateAchievementProgress(
                earnedPointsTotal = 0.0,
                redeemedPointsTotal = 0.0,
                archives =
                    listOf(
                        archive("2026-01-01", controlCompleted = 1, encourageCompleted = 0),
                        archive("2026-01-02", controlCompleted = 0, encourageCompleted = 2),
                        archive("2026-01-03", controlCompleted = 3, encourageCompleted = 1),
                    ),
            )

        assertEquals(2, progress.controlDaysTotal)
        assertEquals(2, progress.encourageDaysTotal)
    }

    @Test
    fun calculateAchievementProgress_breaksStreakOnFailedDay() {
        val progress =
            calculateAchievementProgress(
                earnedPointsTotal = 0.0,
                redeemedPointsTotal = 0.0,
                archives =
                    listOf(
                        archive("2026-01-01", controlCompleted = 1, encourageCompleted = 1),
                        archive("2026-01-02", controlCompleted = 0, encourageCompleted = 1),
                        archive("2026-01-03", controlCompleted = 1, encourageCompleted = 1),
                        archive("2026-01-04", controlCompleted = 1, encourageCompleted = 0),
                    ),
            )

        assertEquals(2, progress.controlStreak)
        assertEquals(0, progress.encourageStreak)
    }

    @Test
    fun calculateAchievementProgress_breaksStreakOnMissingArchiveDate() {
        val progress =
            calculateAchievementProgress(
                earnedPointsTotal = 0.0,
                redeemedPointsTotal = 0.0,
                archives =
                    listOf(
                        archive("2026-01-01", controlCompleted = 1, encourageCompleted = 1),
                        archive("2026-01-03", controlCompleted = 1, encourageCompleted = 1),
                    ),
            )

        assertEquals(1, progress.controlStreak)
        assertEquals(1, progress.encourageStreak)
    }

    @Test
    fun calculateAchievementProgress_requiresAllControlGroupsCompletedWhenGroupArchivesExist() {
        val progress =
            calculateAchievementProgress(
                earnedPointsTotal = 0.0,
                redeemedPointsTotal = 0.0,
                archives =
                    listOf(
                        archive("2026-01-01", controlCompleted = 2, encourageCompleted = 0),
                        archive("2026-01-02", controlCompleted = 1, encourageCompleted = 1),
                        archive("2026-01-03", controlCompleted = 1, encourageCompleted = 0),
                    ),
                groupArchives =
                    listOf(
                        groupArchive("2026-01-01", "c1", GroupType.CONTROL, completed = true),
                        groupArchive("2026-01-01", "c2", GroupType.CONTROL, completed = true),
                        groupArchive("2026-01-02", "c1", GroupType.CONTROL, completed = true),
                        groupArchive("2026-01-02", "c2", GroupType.CONTROL, completed = false),
                        groupArchive("2026-01-02", "e1", GroupType.ENCOURAGE, completed = true),
                        groupArchive("2026-01-03", "c1", GroupType.CONTROL, completed = true),
                    ),
            )

        assertEquals(2, progress.controlDaysTotal)
        assertEquals(1, progress.encourageDaysTotal)
    }

    @Test
    fun calculateAchievementProgress_allowsShieldedControlFailureToKeepStreak() {
        val progress =
            calculateAchievementProgress(
                earnedPointsTotal = 0.0,
                redeemedPointsTotal = 0.0,
                archives =
                    listOf(
                        archive("2026-01-01", controlCompleted = 2, encourageCompleted = 0),
                        archive("2026-01-02", controlCompleted = 1, encourageCompleted = 0),
                        archive("2026-01-03", controlCompleted = 2, encourageCompleted = 0),
                    ),
                groupArchives =
                    listOf(
                        groupArchive("2026-01-01", "c1", GroupType.CONTROL, completed = true),
                        groupArchive("2026-01-01", "c2", GroupType.CONTROL, completed = true),
                        groupArchive("2026-01-02", "c1", GroupType.CONTROL, completed = true),
                        groupArchive("2026-01-02", "c2", GroupType.CONTROL, completed = false),
                        groupArchive("2026-01-03", "c1", GroupType.CONTROL, completed = true),
                        groupArchive("2026-01-03", "c2", GroupType.CONTROL, completed = true),
                    ),
                shieldPendings =
                    listOf(
                        StreakShieldPendingEntity(
                            id = "shield-2026-01-02",
                            archiveDate = "2026-01-02",
                            shieldTarget = StreakShieldTarget.CONTROL_STREAK,
                            status = StreakShieldPendingStatus.USED,
                            createdAt = 0L,
                        ),
                    ),
            )

        assertEquals(3, progress.controlStreak)
    }

    @Test
    fun calculateStreakBeforeDate_countsShieldedDatesAsContinuation() {
        val archiveDates =
            listOf(
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-02"),
                LocalDate.parse("2026-01-03"),
            )

        val streak =
            calculateStreakBeforeDate(
                archiveDates = archiveDates,
                latestDate = LocalDate.parse("2026-01-04"),
                completedDates =
                    setOf(
                        LocalDate.parse("2026-01-01"),
                        LocalDate.parse("2026-01-03"),
                    ),
                shieldedDates = setOf(LocalDate.parse("2026-01-02")),
            )

        assertEquals(3, streak)
    }

    private fun archive(
        date: String,
        controlCompleted: Int,
        encourageCompleted: Int,
    ): DailyArchiveEntity =
        DailyArchiveEntity(
            id = date,
            archiveDate = date,
            dayStartAt = 0L,
            dayEndAt = 0L,
            controlCompletedGroupCount = controlCompleted,
            encourageCompletedGroupCount = encourageCompleted,
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
