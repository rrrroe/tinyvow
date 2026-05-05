package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
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
}
