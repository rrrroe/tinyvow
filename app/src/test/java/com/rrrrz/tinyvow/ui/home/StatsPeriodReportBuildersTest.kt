package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyAppTimeSliceArchiveEntity
import com.rrrrz.tinyvow.data.db.PointLedgerDailyStats
import com.rrrrz.tinyvow.data.time.BusinessDay
import com.rrrrz.tinyvow.data.usage.AppSession
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsPeriodReportBuildersTest {
    @Test
    fun currentPeriodBoundsIncludeTodayAndCompareAgainstAFullPreviousWindow() {
        val zoneId = ZoneId.systemDefault()
        val today = BusinessDay.today(zoneId, BusinessDay.cachedStartHour())
        val weekStart = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

        val bounds =
            requireNotNull(
                resolvePeriodBounds(
                    selectedTab = ReportTab.WEEK,
                    zoneId = zoneId,
                    selectedWeekStart = weekStart,
                    selectedMonth = null,
                    selectedYear = null,
                ),
            )

        assertEquals(today, bounds.endDate)
        assertEquals(
            bounds.endDate.toEpochDay() - bounds.startDate.toEpochDay(),
            bounds.previousEndDate.toEpochDay() - bounds.previousStartDate.toEpochDay(),
        )
    }

    @Test
    fun periodDaySummariesIncludeAnInMemoryTodayRowOnlyOnce() {
        val today = LocalDate.of(2026, 7, 13)
        val yesterday = today.minusDays(1)
        val summaries =
            buildPeriodDaySummaries(
                startDate = yesterday,
                endDate = today,
                archives =
                    listOf(
                        archive(yesterday, HOUR),
                        archive(today, 30 * MINUTE),
                    ),
                snapshots =
                    listOf(
                        archivedSnapshot(yesterday.toString(), "pkg.reader", "Reader", HOUR),
                        archivedSnapshot(today.toString(), "pkg.reader", "Reader", 30 * MINUTE),
                    ),
            )

        assertEquals(listOf(yesterday, today), summaries.map { it.date })
        assertEquals(listOf(HOUR, 30 * MINUTE), summaries.map { it.usageMillis })
    }

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
                offlineFocus = emptyOfflineFocus(),
                behavior = null,
                comparison = null,
            )

        assertEquals(ReportTab.WEEK, report.tab)
        assertEquals(7, report.trend.points.size)
        assertEquals("4h", report.hero.tertiaryValue)
        assertEquals(28 * HOUR, report.totalUsageMillis)
        assertEquals(21 * HOUR, report.controlUsageMillis)
        assertEquals(7 * HOUR, report.encourageUsageMillis)
        assertEquals(7, report.appFocus.weeklyTopAppRows.size)
        assertEquals("pkg.reader", report.appFocus.weeklyTopAppRows.first().packages.first())
    }

    @Test
    fun periodHourBucketsRetainPerAppSegmentsForWeeklyTimeMarks() {
        val readerHours = LongArray(24).apply { this[9] = 30 * MINUTE }
        val videoHours = LongArray(24).apply { this[9] = 45 * MINUTE }

        val bucket =
            buildPeriodHourBuckets(
                listOf(
                    archivedSnapshot("2026-05-04", "pkg.reader", "Reader", 30 * MINUTE, readerHours),
                    archivedSnapshot("2026-05-04", "pkg.video", "Video", 45 * MINUTE, videoHours),
                ),
            )[9]

        assertEquals(75 * MINUTE, bucket.deviceMillis)
        assertEquals(listOf("pkg.video", "pkg.reader"), bucket.appSegments.map { it.packageName })
    }

    @Test
    fun weeklyTimelineSlicesUseTwoRowsPerDayAndTwentyFourColumns() {
        val cells =
            buildWeeklyTimelineSliceCells(
                startDate = LocalDate.of(2026, 5, 4),
                items =
                    listOf(
                        DailyAppTimeSliceArchiveEntity("2026-05-04", 0, "pkg.reader", 5 * MINUTE),
                        DailyAppTimeSliceArchiveEntity("2026-05-04", 144, "pkg.video", 5 * MINUTE),
                        DailyAppTimeSliceArchiveEntity("2026-05-10", 287, "pkg.reader", 5 * MINUTE),
                    ),
            )

        assertEquals(listOf(0, 12, 167), cells.map { it.sliceIndex })
    }

    @Test
    fun weeklyTimelineAppSlicesRetainAppsThatAreNotDominantInTheHour() {
        val items =
            listOf(
                DailyAppTimeSliceArchiveEntity("2026-05-04", 0, "pkg.reader", 5 * MINUTE),
                DailyAppTimeSliceArchiveEntity("2026-05-04", 1, "app.podcast.cosmos", 2 * MINUTE),
            )

        val appCells = buildWeeklyTimelineAppSliceCells(LocalDate.of(2026, 5, 4), items)
        val dominantCells = buildWeeklyTimelineSliceCells(LocalDate.of(2026, 5, 4), items)

        assertEquals(listOf("pkg.reader", "app.podcast.cosmos"), appCells.map { it.packageName })
        assertEquals(2 * MINUTE, appCells.last().millis)
        assertEquals("pkg.reader", dominantCells.single().packageName)
        assertEquals(7 * MINUTE, dominantCells.single().totalMillis)
    }

    @Test
    fun dailyTimelineAppSlicesRetainNonDominantAppInTheSameFiveMinuteCell() {
        val items =
            listOf(
                DailyAppTimeSliceArchiveEntity("2026-05-04", 42, "pkg.reader", 4 * MINUTE),
                DailyAppTimeSliceArchiveEntity("2026-05-04", 42, "app.podcast.cosmos", MINUTE),
            )

        val appCells = buildTimelineAppSliceCells(items)
        val dominantCells = buildTimelineSliceCells(items)

        assertEquals(2, appCells.size)
        assertEquals(MINUTE, appCells.last().millis)
        assertEquals("pkg.reader", dominantCells.single().packageName)
        assertEquals(5 * MINUTE, dominantCells.single().totalMillis)
    }

    @Test
    fun liveTimelineAppSlicesRetainOverlappingNonDominantSession() {
        val cells =
            buildLiveTimelineAppSliceCells(
                dayStartMillis = 0L,
                endMillis = 5 * MINUTE,
                sessions =
                    listOf(
                        AppSession("pkg.reader", 0L, 5 * MINUTE),
                        AppSession("app.podcast.cosmos", MINUTE, 2 * MINUTE),
                    ),
            )

        assertEquals(2, cells.size)
        assertEquals(MINUTE, cells.last().millis)
    }

    @Test
    fun weeklyBehaviorSnapshotsAggregateEachAppAcrossTheWholeWeek() {
        val aggregated =
            aggregateWeeklyBehaviorMapSnapshots(
                listOf(
                    archivedSnapshot("2026-05-04", "pkg.reader", "Reader", HOUR).copy(openCount = 2),
                    archivedSnapshot("2026-05-05", "pkg.reader", "Reader", 2 * HOUR).copy(openCount = 3),
                    archivedSnapshot("2026-05-05", "pkg.video", "Video", 4 * HOUR).copy(openCount = 1),
                ),
            )

        assertEquals(listOf("pkg.video", "pkg.reader"), aggregated.map { it.packageName })
        assertEquals(3 * HOUR, aggregated.last().usageMillis)
        assertEquals(5, aggregated.last().openCount)
    }

    @Test
    fun monthlyPointsTrajectoryContainsEveryCalendarDay() {
        val start = LocalDate.of(2026, 7, 1)
        val data =
            buildWeeklyPointsSectionData(
                weekStart = start,
                periodEnd = start.withDayOfMonth(31),
                stats =
                    listOf(
                        PointLedgerDailyStats("2026-07-01", 5.0, 0.0, 5.0),
                        PointLedgerDailyStats("2026-07-13", 2.0, 1.0, 1.0),
                    ),
                openingBalance = 10.0,
            )

        assertEquals(31, data.days.size)
        assertEquals("1", data.days.first().label)
        assertEquals("31", data.days.last().label)
        assertEquals(16.0, data.days.last().closingBalance, 0.001)
    }

    @Test
    fun monthlyAppFocusBuildsOneColumnForEveryCalendarDay() {
        val start = LocalDate.of(2026, 7, 1)
        val days =
            buildWeeklyAppFocusDays(
                startDate = start,
                endDate = start.withDayOfMonth(31),
                snapshots = listOf(archivedSnapshot("2026-07-13", "pkg.reader", "Reader", HOUR)),
            )

        assertEquals(31, days.size)
        assertEquals("1", days.first().dayCode)
        assertEquals("31", days.last().dayCode)
        assertEquals("pkg.reader", days[12].apps.single().packageName)
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
                offlineFocus = emptyOfflineFocus(),
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
        hourlyBuckets: LongArray = LongArray(24),
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
            hourlyBuckets = hourlyBuckets,
        )

    private fun fakeWindowFocus(): WindowFocusSectionData =
        WindowFocusSectionData(
            control = fakeModeSummary("control"),
            encourage = fakeModeSummary("encourage"),
            highlights = emptyList(),
        )

    private fun emptyOfflineFocus(): OfflineFocusSectionData =
        OfflineFocusSectionData(
            totalMillis = 0L,
            completedCount = 0,
            pointsAwarded = 0.0,
            dayStartMillis = 0L,
            dayEndMillis = 0L,
            sessions = emptyList(),
            categories = emptyList(),
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
