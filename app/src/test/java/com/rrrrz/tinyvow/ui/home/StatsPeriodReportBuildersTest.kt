package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsPeriodReportBuildersTest {
    @Test
    fun weeklyReportBuildsSevenDayTrendAndTopAppRows() {
        val start = LocalDate.of(2026, 5, 4)
        val summaries =
            (0 until 7).map { index ->
                periodDaySummary(
                    date = start.plusDays(index.toLong()),
                    usageMillis = (index + 1L) * HOUR,
                    controlUsageMillis = index * HOUR,
                    encourageUsageMillis = HOUR,
                )
            }
        val snapshots =
            listOf(
                archivedSnapshot("2026-05-04", "pkg.reader", "Reader", 3 * HOUR),
                archivedSnapshot("2026-05-04", "pkg.video", "Video", HOUR),
                archivedSnapshot("2026-05-05", "pkg.video", "Video", 4 * HOUR),
            )

        val report =
            buildWeeklyReportData(
                bounds = periodBounds(start, start.plusDays(6)),
                archives = summaries.map { archive(it.date, it.usageMillis) },
                daySummaries = summaries,
                snapshots = snapshots,
                topApps = listOf(AppDisplayItem("pkg.reader", "Reader", 3 * HOUR)),
                windowFocus = fakeWindowFocus(),
                behavior = null,
                comparison = null,
            )

        assertEquals(ReportTab.WEEK, report.tab)
        assertEquals(7, report.trend.points.size)
        assertEquals("4h", report.hero.tertiaryValue)
        assertEquals(7, report.appFocus.weeklyTopAppRows.size)
        assertEquals("pkg.reader", report.appFocus.weeklyTopAppRows.first().packages.first())
    }

    @Test
    fun monthHeatmapAddsLeadingBlanksAndMarksPeakDay() {
        val start = LocalDate.of(2026, 5, 1)
        val end = LocalDate.of(2026, 5, 31)
        val cells =
            buildMonthHeatmapCells(
                startDate = start,
                endDate = end,
                summaries =
                    listOf(
                        periodDaySummary(start, usageMillis = HOUR),
                        periodDaySummary(start.plusDays(1), usageMillis = 3 * HOUR, exceeded = true),
                    ),
            )

        assertEquals(35, cells.size)
        assertTrue(cells.take(4).all { it.label.isBlank() })
        assertEquals("1", cells[4].label)
        assertEquals("2", cells[5].label)
        assertTrue(cells[5].selected)
        assertTrue(cells[5].exceeded)
        assertFalse(cells[4].selected)
    }

    @Test
    fun yearlyReportAggregatesMonthsAndQuarters() {
        val start = LocalDate.of(2026, 1, 1)
        val archives =
            listOf(
                archive(LocalDate.of(2026, 1, 2), HOUR, savedMillis = 15 * MINUTE),
                archive(LocalDate.of(2026, 3, 5), 2 * HOUR, savedMillis = 30 * MINUTE),
            )
        val snapshots =
            listOf(
                archivedSnapshot("2026-01-02", "pkg.reader", "Reader", HOUR),
                archivedSnapshot("2026-03-05", "pkg.video", "Video", 2 * HOUR),
            )

        val report =
            buildYearlyReportData(
                bounds = periodBounds(start, LocalDate.of(2026, 12, 31)),
                archives = archives,
                daySummaries = archives.map { periodDaySummary(LocalDate.parse(it.archiveDate), it.totalUsageMillis) },
                snapshots = snapshots,
                topApps = listOf(AppDisplayItem("pkg.video", "Video", 2 * HOUR)),
                windowFocus = fakeWindowFocus(),
                behavior = null,
                comparison = null,
            )

        assertEquals(ReportTab.YEAR, report.tab)
        assertEquals(12, report.trend.points.size)
        assertEquals("3h", report.hero.primaryValue)
        assertEquals(4, report.quarterSection?.quarters?.size)
        assertEquals(3 * HOUR, report.quarterSection?.quarters?.first()?.totalUsageMillis)
        assertEquals("Video", report.quarterSection?.quarters?.first()?.topAppLabel)
    }

    private fun periodBounds(start: LocalDate, end: LocalDate): PeriodBounds =
        PeriodBounds(
            startDate = start,
            endDate = end,
            previousStartDate = start.minusDays(7),
            previousEndDate = start.minusDays(1),
        )

    private fun periodDaySummary(
        date: LocalDate,
        usageMillis: Long,
        controlUsageMillis: Long = 0L,
        encourageUsageMillis: Long = 0L,
        exceeded: Boolean = false,
    ): PeriodDaySummary =
        PeriodDaySummary(
            date = date,
            usageMillis = usageMillis,
            controlUsageMillis = controlUsageMillis,
            encourageUsageMillis = encourageUsageMillis,
            savedMillis = 0L,
            pointsNet = 0.0,
            openCount = 0,
            nightUsageMillis = 0L,
            longestSessionMillis = 0L,
            exceeded = exceeded,
            blockCount = 0,
        )

    private fun archive(
        date: LocalDate,
        usageMillis: Long,
        savedMillis: Long = 0L,
    ): DailyArchiveEntity =
        DailyArchiveEntity(
            id = date.toString(),
            archiveDate = date.toString(),
            dayStartAt = 0L,
            dayEndAt = 0L,
            totalUsageMillis = usageMillis,
            savedMillis = savedMillis,
            createdAt = 0L,
            updatedAt = 0L,
        )

    private fun archivedSnapshot(
        date: String,
        packageName: String,
        label: String,
        usageMillis: Long,
    ): ArchivedAppSnapshot =
        ArchivedAppSnapshot(
            archiveDate = date,
            packageName = packageName,
            label = label,
            usageMillis = usageMillis,
            openCount = 0,
            sessionCount = 0,
            longestSessionMillis = 0L,
            nightUsageMillis = 0L,
            hourlyBuckets = LongArray(24),
        )

    private fun fakeWindowFocus(): WindowFocusSectionData =
        WindowFocusSectionData(
            control = fakeModeSummary("control"),
            encourage = fakeModeSummary("encourage"),
            highlights = emptyList(),
        )

    private fun fakeModeSummary(title: String): DailyModeSummary =
        DailyModeSummary(
            title = title,
            description = "",
            primaryLabel = "",
            primaryValue = "0m",
            metrics = emptyList(),
            progress = 0f,
            spotlightLabel = "",
            spotlightValue = "",
        )

    private companion object {
        const val MINUTE = 60_000L
        const val HOUR = 60 * MINUTE
    }
}
