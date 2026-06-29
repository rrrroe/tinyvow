package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import com.rrrrz.tinyvow.data.repository.OfflineFocusCategorySummary
import com.rrrrz.tinyvow.data.repository.OfflineFocusSession
import com.rrrrz.tinyvow.data.repository.OfflineFocusTodaySummary
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineFocusReportBuildersTest {
    @Test
    fun offlineFocusSectionKeepsTimelineAndCategoryColor() {
        val summary =
            OfflineFocusTodaySummary(
                totalMillis = 45 * MINUTE,
                completedCount = 2,
                pointsAwarded = 45.0,
                sessions =
                    listOf(
                        session("reading", "Reading", 0xFF3F7CAC.toInt(), 1_000L, 26 * MINUTE, 25.0),
                        session("writing", "Writing", 0xFF9A6BFF.toInt(), 4_000L, 21 * MINUTE, 20.0),
                    ),
                categories =
                    listOf(
                        OfflineFocusCategorySummary(
                            categoryName = "Reading",
                            iconKey = "reading",
                            colorArgb = 0xFF3F7CAC.toInt(),
                            totalMillis = 25 * MINUTE,
                            completedCount = 1,
                            pointsAwarded = 25.0,
                        ),
                        OfflineFocusCategorySummary(
                            categoryName = "Writing",
                            iconKey = "writing",
                            colorArgb = 0xFF9A6BFF.toInt(),
                            totalMillis = 20 * MINUTE,
                            completedCount = 1,
                            pointsAwarded = 20.0,
                        ),
                    ),
            )

        val section =
            buildOfflineFocusSectionData(
                summary = summary,
                dayStartMillis = 0L,
                dayEndMillis = 24 * HOUR,
            )

        assertEquals(45 * MINUTE, section.totalMillis)
        assertEquals(2, section.completedCount)
        assertEquals(45.0, section.pointsAwarded, 0.0)
        assertEquals("Reading", section.sessions.first().categoryName)
        assertEquals(0xFF3F7CAC.toInt(), section.sessions.first().colorArgb)
        assertEquals(26 * MINUTE, section.sessions.first().durationMillis)
        assertEquals("Writing", section.categories[1].categoryName)
        assertEquals(20 * MINUTE, section.categories[1].totalMillis)
    }

    private fun session(
        categoryId: String,
        categoryName: String,
        colorArgb: Int,
        startedAt: Long,
        durationMillis: Long,
        points: Double,
    ): OfflineFocusSession =
        OfflineFocusSession(
            id = categoryId,
            categoryId = categoryId,
            categoryName = categoryName,
            iconKey = categoryId,
            colorArgb = colorArgb,
            plannedDurationMillis = 25 * MINUTE,
            actualDurationMillis = durationMillis,
            status = OfflineFocusSessionStatus.SETTLED,
            startedAt = startedAt,
            pausedAt = null,
            completedAt = startedAt + durationMillis,
            abandonedAt = null,
            pointsAwarded = points,
        )

    private companion object {
        const val MINUTE = 60_000L
        const val HOUR = 60 * MINUTE
    }
}
