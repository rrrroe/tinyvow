package com.rrrrz.tinyvow.ui.home

import android.content.Context
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.usage.AppSession
import com.rrrrz.tinyvow.data.usage.UsageRepository
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.i18n.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
internal fun createRefreshingUiState(
    selectedTab: ReportTab,
    previous: DailyReportUiState? = null,
    selectedArchiveDate: String? = previous?.selectedArchiveDate,
    selectedWeekStart: LocalDate? = previous?.selectedWeekStart,
    selectedMonth: YearMonth? = previous?.selectedMonth,
    selectedYear: Int? = previous?.selectedYear,
): DailyReportUiState {
    return DailyReportUiState(
        isPermissionGranted = true,
        selectedTab = selectedTab,
        isRefreshing = true,
        heroState = previous?.heroState ?: SectionState.Loading,
        dailyFocusState =
            if (selectedTab == ReportTab.DAY) {
                previous?.dailyFocusState ?: SectionState.Loading
            } else {
                SectionState.Empty
            },
        windowFocusState =
            if (selectedTab == ReportTab.DAY) {
                SectionState.Empty
            } else {
                previous?.windowFocusState ?: SectionState.Loading
            },
        heatmapState =
            if (selectedTab == ReportTab.MONTH || selectedTab == ReportTab.YEAR) {
                previous?.heatmapState ?: SectionState.Loading
            } else {
                SectionState.Empty
            },
        yearDualScopeState =
            if (selectedTab == ReportTab.YEAR) {
                previous?.yearDualScopeState ?: SectionState.Loading
            } else {
                SectionState.Empty
            },
        shareState = previous?.shareState ?: SectionState.Loading,
        timelineState = previous?.timelineState ?: SectionState.Loading,
        topAppsState = previous?.topAppsState ?: SectionState.Loading,
        behaviorState = previous?.behaviorState ?: SectionState.Loading,
        comparisonState = previous?.comparisonState ?: SectionState.Loading,
        periodReportState =
            if (selectedTab == ReportTab.DAY) {
                SectionState.Empty
            } else {
                previous?.periodReportState ?: SectionState.Loading
            },
        selectedArchiveDate = selectedArchiveDate,
        previousArchiveDate = previous?.previousArchiveDate,
        nextArchiveDate = previous?.nextArchiveDate,
        availableArchiveDates = previous?.availableArchiveDates.orEmpty(),
        selectedWeekStart = selectedWeekStart,
        previousWeekStart = previous?.previousWeekStart,
        nextWeekStart = previous?.nextWeekStart,
        availableWeekStarts = previous?.availableWeekStarts.orEmpty(),
        selectedMonth = selectedMonth,
        previousMonth = previous?.previousMonth,
        nextMonth = previous?.nextMonth,
        availableMonths = previous?.availableMonths.orEmpty(),
        selectedYear = selectedYear,
        previousYear = previous?.previousYear,
        nextYear = previous?.nextYear,
        availableYears = previous?.availableYears.orEmpty(),
    )
}

internal suspend fun buildArchivedWindowReportUiState(
    selectedTab: ReportTab,
    zoneId: ZoneId,
    archiveRepository: DailyArchiveRepository,
    recentArchives: List<DailyArchiveEntity>,
    selectedWeekStart: LocalDate?,
    selectedMonth: YearMonth?,
    selectedYear: Int?,
    updateState: ((DailyReportUiState) -> DailyReportUiState) -> Unit,
) {
    val availableWeekStarts = buildAvailableWeekStarts(recentArchives)
    val availableMonths = buildAvailableMonths(recentArchives)
    val availableYears = buildAvailableYears(recentArchives)
    val periodBounds =
        resolvePeriodBounds(
            selectedTab = selectedTab,
            zoneId = zoneId,
            selectedWeekStart = selectedWeekStart,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
        )

    if (recentArchives.isEmpty() || periodBounds == null) {
        updateState { current ->
            current.copy(
                isRefreshing = false,
                heroState = SectionState.Empty,
                dailyFocusState = SectionState.Empty,
                windowFocusState = SectionState.Empty,
                heatmapState = SectionState.Empty,
                yearDualScopeState = SectionState.Empty,
                shareState = SectionState.Empty,
                timelineState = SectionState.Empty,
                topAppsState = SectionState.Empty,
                behaviorState = SectionState.Empty,
                comparisonState = SectionState.Empty,
                periodReportState = SectionState.Empty,
                selectedArchiveDate = null,
                previousArchiveDate = null,
                nextArchiveDate = null,
                availableArchiveDates = emptyList(),
                selectedWeekStart = selectedWeekStart,
                previousWeekStart = null,
                nextWeekStart = null,
                availableWeekStarts = availableWeekStarts,
                selectedMonth = selectedMonth,
                previousMonth = null,
                nextMonth = null,
                availableMonths = availableMonths,
                selectedYear = selectedYear,
                previousYear = null,
                nextYear = null,
                availableYears = availableYears,
                placeholderTitle = AppText.t("stats_no_archived_daily_reports_yet"),
                placeholderDescription = AppText.t("stats_daily_reports_only_show_yesterday_and_earlier_archived"),
            )
        }
        return
    }

    val currentFrom = ArchiveDateUtils.formatDate(periodBounds.startDate)
    val currentTo = ArchiveDateUtils.formatDate(periodBounds.endDate)
    val previousFrom = ArchiveDateUtils.formatDate(periodBounds.previousStartDate)
    val previousTo = ArchiveDateUtils.formatDate(periodBounds.previousEndDate)
    val currentArchives = archiveRepository.getArchivesByRange(currentFrom, currentTo).first()
    val previousArchives = archiveRepository.getArchivesByRange(previousFrom, previousTo).first()
    val currentAppArchives = archiveRepository.getAppArchivesByRange(currentFrom, currentTo).first()
    val previousAppArchives = archiveRepository.getAppArchivesByRange(previousFrom, previousTo).first()
    val currentGroupArchives = archiveRepository.getGroupArchivesByRange(currentFrom, currentTo).first()
    val currentSnapshots = mergeArchivedAppSnapshots(currentAppArchives)
    val previousSnapshots = mergeArchivedAppSnapshots(previousAppArchives)
    val currentMetrics = buildArchivedWindowMetrics(currentSnapshots)
    val previousMetrics = buildArchivedWindowMetrics(previousSnapshots)
    val topApps =
        archiveRepository
            .getTopAppsByRange(
                from = currentFrom,
                to = currentTo,
                limit = 10,
            ).first()
            .map {
                AppDisplayItem(
                    packageName = it.packageName,
                    label = it.appLabel,
                    value = it.totalUsageMillis,
                )
            }
    val overview =
        ScopeOverview(
            totalUsageMillis = currentArchives.sumOf { it.totalUsageMillis },
            openCount = currentMetrics.deviceOpenCount,
            activeBucketCount = currentArchives.size,
            topApp = topApps.firstOrNull(),
        )
    val averagePerDayUsage =
        if (currentArchives.isEmpty()) {
            0L
        } else {
            currentArchives.sumOf { it.totalUsageMillis } / currentArchives.size
        }
    val daySummaries = buildPeriodDaySummaries(periodBounds.startDate, periodBounds.endDate, currentArchives, currentSnapshots)
    val hourBuckets = buildPeriodHourBuckets(currentSnapshots)
    val behaviorInsight = buildArchivedDayBehaviorInsight(currentSnapshots, hourBuckets)
    val behaviorStructure = buildArchivedDayBehaviorStructure(currentSnapshots, hourBuckets)
    val windowFocusData =
        buildWindowFocusSectionData(
            selectedTab = selectedTab,
            archives = currentArchives,
            groupArchives = currentGroupArchives,
            activeDayCount = currentArchives.size,
        )
    val comparisonData =
        buildArchivedComparisonMetrics(
            selectedTab = selectedTab,
            overview = overview,
            currentMetrics = currentMetrics,
            previousMetrics = previousMetrics,
            averagePerDayUsage = averagePerDayUsage,
        ).takeIf { it.isNotEmpty() }?.let { ComparisonSectionData(it) }
    val reportData =
        buildPeriodReportData(
            selectedTab = selectedTab,
            bounds = periodBounds,
            archives = currentArchives,
            daySummaries = daySummaries,
            snapshots = currentSnapshots,
            topApps = topApps,
            windowFocus = windowFocusData,
            behavior =
                if (behaviorStructure == null) {
                    null
                } else {
                    BehaviorSectionData(
                        behaviorInsight = behaviorInsight,
                        structure = behaviorStructure,
                    )
                },
            comparison = comparisonData,
        )
    val selectedWeekIndex = availableWeekStarts.indexOf(selectedWeekStart)
    val selectedMonthIndex = availableMonths.indexOf(selectedMonth)
    val selectedYearIndex = availableYears.indexOf(selectedYear)

    updateState { current ->
        current.copy(
            isRefreshing = false,
            heroState = SectionState.Empty,
            dailyFocusState = SectionState.Empty,
            windowFocusState = SectionState.Empty,
            heatmapState = SectionState.Empty,
            yearDualScopeState = SectionState.Empty,
            shareState = SectionState.Empty,
            timelineState = SectionState.Empty,
            topAppsState = SectionState.Empty,
            behaviorState = SectionState.Empty,
            comparisonState = SectionState.Empty,
            periodReportState = SectionState.Ready(reportData),
            selectedArchiveDate = null,
            previousArchiveDate = null,
            nextArchiveDate = null,
            availableArchiveDates = emptyList(),
            selectedWeekStart = selectedWeekStart,
            previousWeekStart = availableWeekStarts.getOrNull(selectedWeekIndex + 1),
            nextWeekStart = availableWeekStarts.getOrNull(selectedWeekIndex - 1),
            availableWeekStarts = availableWeekStarts,
            selectedMonth = selectedMonth,
            previousMonth = availableMonths.getOrNull(selectedMonthIndex + 1),
            nextMonth = availableMonths.getOrNull(selectedMonthIndex - 1),
            availableMonths = availableMonths,
            selectedYear = selectedYear,
            previousYear = availableYears.getOrNull(selectedYearIndex + 1),
            nextYear = availableYears.getOrNull(selectedYearIndex - 1),
            availableYears = availableYears,
            placeholderTitle = null,
            placeholderDescription = null,
        )
    }
}

internal fun buildAvailableWeekStarts(recentArchives: List<DailyArchiveEntity>): List<LocalDate> =
    recentArchives
        .map { LocalDate.parse(it.archiveDate).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)) }
        .distinct()
        .sortedDescending()

internal fun buildAvailableMonths(recentArchives: List<DailyArchiveEntity>): List<YearMonth> =
    recentArchives
        .map { YearMonth.from(LocalDate.parse(it.archiveDate)) }
        .distinct()
        .sortedDescending()

internal fun buildAvailableYears(recentArchives: List<DailyArchiveEntity>): List<Int> =
    recentArchives
        .map { LocalDate.parse(it.archiveDate).year }
        .distinct()
        .sortedDescending()

internal fun resolvePeriodBounds(
    selectedTab: ReportTab,
    zoneId: ZoneId,
    selectedWeekStart: LocalDate?,
    selectedMonth: YearMonth?,
    selectedYear: Int?,
): PeriodBounds? {
    val yesterday = LocalDate.now(zoneId).minusDays(1)
    return when (selectedTab) {
        ReportTab.WEEK -> {
            val start = selectedWeekStart ?: return null
            val end = minOf(start.plusDays(6), yesterday)
            val daySpan = (end.toEpochDay() - start.toEpochDay()).coerceAtLeast(0L)
            val previousStart = start.minusWeeks(1)
            PeriodBounds(
                startDate = start,
                endDate = end,
                previousStartDate = previousStart,
                previousEndDate = previousStart.plusDays(daySpan),
            )
        }
        ReportTab.MONTH -> {
            val month = selectedMonth ?: return null
            val start = month.atDay(1)
            val end = minOf(month.atEndOfMonth(), yesterday)
            val daySpan = (end.toEpochDay() - start.toEpochDay()).coerceAtLeast(0L)
            val previousMonth = month.minusMonths(1)
            val previousStart = previousMonth.atDay(1)
            PeriodBounds(
                startDate = start,
                endDate = end,
                previousStartDate = previousStart,
                previousEndDate = minOf(previousMonth.atEndOfMonth(), previousStart.plusDays(daySpan)),
            )
        }
        ReportTab.YEAR -> {
            val year = selectedYear ?: return null
            val start = LocalDate.of(year, 1, 1)
            val end = minOf(LocalDate.of(year, 12, 31), yesterday)
            val daySpan = (end.toEpochDay() - start.toEpochDay()).coerceAtLeast(0L)
            val previousStart = LocalDate.of(year - 1, 1, 1)
            PeriodBounds(
                startDate = start,
                endDate = end,
                previousStartDate = previousStart,
                previousEndDate = minOf(LocalDate.of(year - 1, 12, 31), previousStart.plusDays(daySpan)),
            )
        }
        ReportTab.DAY -> null
    }
}

internal fun buildPeriodDaySummaries(
    startDate: LocalDate,
    endDate: LocalDate,
    archives: List<DailyArchiveEntity>,
    snapshots: List<ArchivedAppSnapshot>,
): List<PeriodDaySummary> {
    val archiveByDate = archives.associateBy { LocalDate.parse(it.archiveDate) }
    val openCountsByDate = snapshots.groupBy { LocalDate.parse(it.archiveDate) }.mapValues { (_, items) -> items.sumOf { it.openCount } }
    val nightUsageByDate = snapshots.groupBy { LocalDate.parse(it.archiveDate) }.mapValues { (_, items) -> items.sumOf { it.nightUsageMillis } }
    val longestByDate = snapshots.groupBy { LocalDate.parse(it.archiveDate) }.mapValues { (_, items) -> items.maxOfOrNull { it.longestSessionMillis } ?: 0L }
    return generateDateSequence(startDate, endDate).map { date ->
        val archive = archiveByDate[date]
        PeriodDaySummary(
            date = date,
            usageMillis = archive?.totalUsageMillis ?: 0L,
            controlUsageMillis = archive?.controlUsageMillis ?: 0L,
            encourageUsageMillis = archive?.encourageUsageMillis ?: 0L,
            savedMillis = archive?.savedMillis ?: 0L,
            pointsNet = archive?.pointsNet ?: 0.0,
            openCount = openCountsByDate[date] ?: 0,
            nightUsageMillis = nightUsageByDate[date] ?: 0L,
            longestSessionMillis = longestByDate[date] ?: 0L,
            exceeded = (archive?.controlExceededGroupCount ?: 0) > 0,
            blockCount = archive?.controlBlockEventCount ?: 0,
        )
    }
}

internal fun buildPeriodHourBuckets(items: List<ArchivedAppSnapshot>): List<DailyTimelineBucket> =
    (0 until 24).map { hour ->
        DailyTimelineBucket(
            hour = hour,
            label = dayHourLabel(hour),
            deviceMillis = items.sumOf { snapshot -> snapshot.hourlyBuckets.getOrElse(hour) { 0L } },
        )
    }

internal fun buildPeriodReportData(
    selectedTab: ReportTab,
    bounds: PeriodBounds,
    archives: List<DailyArchiveEntity>,
    daySummaries: List<PeriodDaySummary>,
    snapshots: List<ArchivedAppSnapshot>,
    topApps: List<AppDisplayItem>,
    windowFocus: WindowFocusSectionData,
    behavior: BehaviorSectionData?,
    comparison: ComparisonSectionData?,
): PeriodReportData {
    return when (selectedTab) {
        ReportTab.WEEK ->
            buildWeeklyReportData(
                bounds = bounds,
                archives = archives,
                daySummaries = daySummaries,
                snapshots = snapshots,
                topApps = topApps,
                windowFocus = windowFocus,
                behavior = behavior,
                comparison = comparison,
            )
        ReportTab.MONTH ->
            buildMonthlyReportData(
                bounds = bounds,
                archives = archives,
                daySummaries = daySummaries,
                snapshots = snapshots,
                topApps = topApps,
                windowFocus = windowFocus,
                behavior = behavior,
                comparison = comparison,
            )
        ReportTab.YEAR ->
            buildYearlyReportData(
                bounds = bounds,
                archives = archives,
                daySummaries = daySummaries,
                snapshots = snapshots,
                topApps = topApps,
                windowFocus = windowFocus,
                behavior = behavior,
                comparison = comparison,
            )
        ReportTab.DAY -> error("DAY is not supported in period report builder")
    }
}

internal fun archiveWindowDays(tab: ReportTab): Int =
    when (tab) {
        ReportTab.DAY -> 1
        ReportTab.WEEK -> 7
        ReportTab.MONTH -> 30
        ReportTab.YEAR -> 365
    }

internal fun buildWeeklyReportData(
    bounds: PeriodBounds,
    archives: List<DailyArchiveEntity>,
    daySummaries: List<PeriodDaySummary>,
    snapshots: List<ArchivedAppSnapshot>,
    topApps: List<AppDisplayItem>,
    windowFocus: WindowFocusSectionData,
    behavior: BehaviorSectionData?,
    comparison: ComparisonSectionData?,
): PeriodReportData {
    val activeDays = daySummaries.count { it.usageMillis > 0L }
    val totalUsage = daySummaries.sumOf { it.usageMillis }
    val totalNight = daySummaries.sumOf { it.nightUsageMillis }
    val weekdayDays = daySummaries.filter { it.date.dayOfWeek.value in 1..5 }
    val weekendDays = daySummaries.filter { it.date.dayOfWeek.value >= 6 }
    val bestDay = daySummaries.maxByOrNull { it.usageMillis }
    val calmDay = daySummaries.filter { it.usageMillis > 0L }.minByOrNull { it.usageMillis }
    val trendPoints =
        daySummaries.map { summary ->
            TrendPoint(
                label = summary.date.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault())),
                totalUsageMillis = summary.usageMillis,
                secondaryValue = summary.controlUsageMillis,
                tertiaryValue = summary.encourageUsageMillis,
            )
        }
    val trend =
        TrendSectionData(
            title = AppText.t("stats_weekly_rhythm_curve"),
            subtitle = AppText.t("stats_weekly_rhythm_curve_description"),
            primaryLabel = AppText.t("stats_usage_duration"),
            secondaryLabel = AppText.t("stats_control_results"),
            tertiaryLabel = AppText.t("stats_encourage_progress"),
            points = trendPoints,
            summary =
                listOf(
                    DailyFocusMetric(
                        AppText.t("stats_weekday_average"),
                        formatDuration(if (weekdayDays.isNotEmpty()) weekdayDays.sumOf { it.usageMillis } / weekdayDays.size else 0L),
                    ),
                    DailyFocusMetric(
                        AppText.t("stats_weekend_average"),
                        formatDuration(if (weekendDays.isNotEmpty()) weekendDays.sumOf { it.usageMillis } / weekendDays.size else 0L),
                    ),
                    DailyFocusMetric(
                        AppText.t("stats_highest_day"),
                        bestDay?.let { "${it.date.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault()))} · ${formatDuration(it.usageMillis)}" } ?: AppText.t("stats_none"),
                    ),
                    DailyFocusMetric(
                        AppText.t("stats_lowest_day"),
                        calmDay?.let { "${it.date.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault()))} · ${formatDuration(it.usageMillis)}" } ?: AppText.t("stats_none"),
                    ),
                ),
        )
    return PeriodReportData(
        tab = ReportTab.WEEK,
        hero =
            PeriodHeroData(
                eyebrow = "",
                title = AppText.t("stats_total_phone_usage"),
                rangeLabel = periodWeekLabel(bounds.startDate),
                primaryValue = formatDuration(totalUsage),
                message = AppText.t("stats_weekly_battle_message"),
                comparisonValue = periodUsageDeltaValue(comparison),
                averageLabel = AppText.t("stats_daily_average"),
                tertiaryValue = formatDuration(if (activeDays > 0) totalUsage / activeDays else 0L),
                tags = listOf(windowFocus.control.primaryValue, windowFocus.encourage.primaryValue, totalNight.takeIf { it > 0L }?.let(::formatDuration) ?: AppText.t("stats_none")),
                metrics =
                    listOf(
                        DailyFocusMetric(AppText.t("stats_active_days"), AppText.t("stats_value_days_2", activeDays)),
                        DailyFocusMetric(AppText.t("stats_night_use"), formatDuration(totalNight)),
                        DailyFocusMetric(AppText.t("stats_best_day"), bestDay?.let { it.date.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault())) } ?: AppText.t("stats_none")),
                        DailyFocusMetric(AppText.t("stats_calmest_day"), calmDay?.let { it.date.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault())) } ?: AppText.t("stats_none")),
                    ),
            ),
        trend = trend,
        appFocus =
            buildAppFocusSectionData(ReportTab.WEEK, topApps, snapshots, totalUsage).copy(
                weeklyTopAppRows = buildWeeklyTopAppRows(bounds.startDate, snapshots),
            ),
        windowFocus = windowFocus,
        behavior = behavior,
        comparison = comparison,
    )
}

internal fun buildMonthlyReportData(
    bounds: PeriodBounds,
    archives: List<DailyArchiveEntity>,
    daySummaries: List<PeriodDaySummary>,
    snapshots: List<ArchivedAppSnapshot>,
    topApps: List<AppDisplayItem>,
    windowFocus: WindowFocusSectionData,
    behavior: BehaviorSectionData?,
    comparison: ComparisonSectionData?,
): PeriodReportData {
    val totalUsage = daySummaries.sumOf { it.usageMillis }
    val activeDays = daySummaries.count { it.usageMillis > 0L }
    val totalSaved = daySummaries.sumOf { it.savedMillis }
    val totalNight = daySummaries.sumOf { it.nightUsageMillis }
    val overLimitDays = daySummaries.count { it.exceeded }
    val bestDay = daySummaries.maxByOrNull { it.usageMillis }
    var cumulative = 0L
    val averageDaily = if (daySummaries.isNotEmpty()) totalUsage / daySummaries.size else 0L
    val trendPoints =
        daySummaries.mapIndexed { index, summary ->
            cumulative += summary.usageMillis
            TrendPoint(
                label = summary.date.dayOfMonth.toString(),
                totalUsageMillis = cumulative,
                secondaryValue = averageDaily * (index + 1L),
            )
        }
    val monthWeeks = buildMonthlyWeekSummaries(bounds.startDate, bounds.endDate, daySummaries)
    return PeriodReportData(
        tab = ReportTab.MONTH,
        hero =
            PeriodHeroData(
                eyebrow = "",
                title = AppText.t("stats_total_phone_usage"),
                rangeLabel = periodRangeLabel(bounds.startDate, bounds.endDate),
                primaryValue = formatDuration(totalUsage),
                message = AppText.t("stats_monthly_battle_message"),
                comparisonValue = periodUsageDeltaValue(comparison),
                averageLabel = AppText.t("stats_daily_average"),
                tertiaryValue = formatDuration(if (activeDays > 0) totalUsage / activeDays else 0L),
                tags = listOf(AppText.t("stats_value_days_2", activeDays), windowFocus.control.primaryValue, windowFocus.encourage.primaryValue),
                metrics =
                    listOf(
                        DailyFocusMetric(AppText.t("stats_active_days"), AppText.t("stats_value_days_2", activeDays)),
                        DailyFocusMetric(AppText.t("stats_over_limit"), AppText.t("stats_value_times_12", overLimitDays)),
                        DailyFocusMetric(AppText.t("stats_time_saved"), formatDuration(totalSaved)),
                        DailyFocusMetric(AppText.t("stats_net_points"), formatSignedPointsLocal(archives.sumOf { it.pointsNet })),
                    ),
            ),
        trend =
            TrendSectionData(
                title = AppText.t("stats_monthly_cumulative_curve"),
                subtitle = AppText.t("stats_monthly_cumulative_curve_description"),
                primaryLabel = AppText.t("stats_usage_duration"),
                secondaryLabel = AppText.t("stats_average"),
                points = trendPoints,
                summary =
                    listOf(
                        DailyFocusMetric(AppText.t("stats_highest_day"), bestDay?.let { "${it.date.dayOfMonth} · ${formatDuration(it.usageMillis)}" } ?: AppText.t("stats_none")),
                        DailyFocusMetric(AppText.t("stats_night_use"), formatDuration(totalNight)),
                        DailyFocusMetric(AppText.t("stats_time_saved"), formatDuration(totalSaved)),
                    ),
            ),
        heatmap =
            PeriodHeatmapData(
                title = AppText.t("stats_month_calendar_heatmap"),
                subtitle = AppText.t("stats_month_calendar_heatmap_description"),
                columns = 7,
                cells =
                    buildMonthHeatmapCells(
                        startDate = bounds.startDate,
                        endDate = bounds.endDate,
                        summaries = daySummaries,
                    ),
            ),
        appFocus = buildAppFocusSectionData(ReportTab.MONTH, topApps, snapshots, totalUsage),
        windowFocus = windowFocus,
        behavior = behavior,
        comparison = comparison,
        monthStructure =
            MonthlyWeekStructureData(
                title = AppText.t("stats_month_week_structure"),
                subtitle = AppText.t("stats_month_week_structure_description"),
                weeks = monthWeeks,
            ),
    )
}

internal fun buildYearlyReportData(
    bounds: PeriodBounds,
    archives: List<DailyArchiveEntity>,
    daySummaries: List<PeriodDaySummary>,
    snapshots: List<ArchivedAppSnapshot>,
    topApps: List<AppDisplayItem>,
    windowFocus: WindowFocusSectionData,
    behavior: BehaviorSectionData?,
    comparison: ComparisonSectionData?,
): PeriodReportData {
    val monthSummaries = buildYearMonthSummaries(bounds.startDate.year, archives, snapshots)
    val activeMonths = monthSummaries.count { it.usageMillis > 0L }
    val totalUsage = monthSummaries.sumOf { it.usageMillis }
    val totalSaved = monthSummaries.sumOf { it.savedMillis }
    val bestMonth = monthSummaries.maxByOrNull { it.usageMillis }
    val trend =
        TrendSectionData(
            title = AppText.t("stats_yearly_curve"),
            subtitle = AppText.t("stats_yearly_curve_description"),
            primaryLabel = AppText.t("stats_usage_duration"),
            secondaryLabel = AppText.t("stats_time_saved"),
            points =
                monthSummaries.map {
                    TrendPoint(
                        label = it.label,
                        totalUsageMillis = it.usageMillis,
                        secondaryValue = it.savedMillis,
                    )
                },
            summary =
                listOf(
                    DailyFocusMetric(AppText.t("stats_active_months"), AppText.t("stats_value_times_12", activeMonths)),
                    DailyFocusMetric(AppText.t("stats_best_month"), bestMonth?.label ?: AppText.t("stats_none")),
                    DailyFocusMetric(AppText.t("stats_time_saved"), formatDuration(totalSaved)),
                ),
        )
    return PeriodReportData(
        tab = ReportTab.YEAR,
        hero =
            PeriodHeroData(
                eyebrow = "",
                title = AppText.t("stats_total_phone_usage"),
                rangeLabel = periodRangeLabel(bounds.startDate, bounds.endDate),
                primaryValue = formatDuration(totalUsage),
                message = AppText.t("stats_yearly_battle_message"),
                comparisonValue = periodUsageDeltaValue(comparison),
                averageLabel = AppText.t("stats_monthly_average"),
                tertiaryValue = formatDuration(if (activeMonths > 0) totalUsage / activeMonths else 0L),
                tags = listOf(formatDuration(totalSaved), formatSignedPointsLocal(archives.sumOf { it.pointsNet }), AppText.t("stats_value_times_12", activeMonths)),
                metrics =
                    listOf(
                        DailyFocusMetric(AppText.t("stats_active_months"), AppText.t("stats_value_times_12", activeMonths)),
                        DailyFocusMetric(AppText.t("stats_best_month"), bestMonth?.label ?: AppText.t("stats_none")),
                        DailyFocusMetric(AppText.t("stats_time_saved"), formatDuration(totalSaved)),
                        DailyFocusMetric(AppText.t("stats_net_points"), formatSignedPointsLocal(archives.sumOf { it.pointsNet })),
                    ),
            ),
        trend = trend,
        heatmap =
            PeriodHeatmapData(
                title = AppText.t("stats_year_month_heatmap"),
                subtitle = AppText.t("stats_year_month_heatmap_description"),
                columns = 7,
                cells =
                    buildYearHeatmapCells(
                        startDate = bounds.startDate,
                        endDate = bounds.endDate,
                        summaries = daySummaries,
                    ),
                showLabels = false,
            ),
        appFocus = buildAppFocusSectionData(ReportTab.YEAR, topApps, snapshots, totalUsage),
        windowFocus = windowFocus,
        behavior = behavior,
        comparison = comparison,
        quarterSection =
            YearQuarterSectionData(
                title = AppText.t("stats_quarter_breakdown"),
                subtitle = AppText.t("stats_quarter_breakdown_description"),
                quarters = buildQuarterSummaries(monthSummaries, snapshots),
            ),
    )
}

internal fun periodUsageDeltaValue(comparison: ComparisonSectionData?): String {
    val totalChart = comparison?.comparisons?.firstOrNull()?.chartData ?: return AppText.t("stats_not_enough_samples")
    val previousValue = totalChart.previousValue ?: return AppText.t("stats_not_enough_samples")
    return formatDuration(kotlin.math.abs(totalChart.currentValue - previousValue))
}

internal fun generateDateSequence(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
    if (endDate.isBefore(startDate)) return emptyList()
    return buildList {
        var cursor = startDate
        while (!cursor.isAfter(endDate)) {
            add(cursor)
            cursor = cursor.plusDays(1)
        }
    }
}

internal fun periodRangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault()))} - ${endDate.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault()))}"

internal fun isoWeekLabel(weekStart: LocalDate): String {
    val weekYear = weekStart.get(IsoFields.WEEK_BASED_YEAR)
    val weekNumber = weekStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    return "$weekYear-W${weekNumber.toString().padStart(2, '0')}"
}

internal fun periodWeekLabel(weekStart: LocalDate): String =
    "${isoWeekLabel(weekStart)} · ${periodRangeLabel(weekStart, weekStart.plusDays(6))}"

internal fun buildMonthHeatmapCells(
    startDate: LocalDate,
    endDate: LocalDate,
    summaries: List<PeriodDaySummary>,
): List<HeatmapDayData> {
    val summaryByDate = summaries.associateBy { it.date }
    val leadingBlankDays = (startDate.dayOfWeek.value + 6) % 7
    val cells = MutableList(leadingBlankDays) { HeatmapDayData("", 0L, exceeded = false, selected = false) }
    val maxUsage = summaries.maxOfOrNull { it.usageMillis } ?: 0L
    generateDateSequence(startDate, endDate).forEach { date ->
        val summary = summaryByDate[date]
        cells += HeatmapDayData(
            label = date.dayOfMonth.toString(),
            valueMillis = summary?.usageMillis ?: 0L,
            exceeded = summary?.exceeded == true,
            selected = summary != null && summary.usageMillis == maxUsage && maxUsage > 0L,
        )
    }
    return cells
}

internal fun buildYearHeatmapCells(
    startDate: LocalDate,
    endDate: LocalDate,
    summaries: List<PeriodDaySummary>,
): List<HeatmapDayData> {
    val summaryByDate = summaries.associateBy { it.date }
    val leadingBlankDays = (startDate.dayOfWeek.value + 6) % 7
    val cells = MutableList(leadingBlankDays) { HeatmapDayData("", 0L, exceeded = false, selected = false) }
    val maxUsage = summaries.maxOfOrNull { it.usageMillis } ?: 0L
    generateDateSequence(startDate, endDate).forEach { date ->
        val summary = summaryByDate[date]
        cells += HeatmapDayData(
            label = date.dayOfMonth.toString(),
            valueMillis = summary?.usageMillis ?: 0L,
            exceeded = summary?.exceeded == true,
            selected = summary != null && summary.usageMillis == maxUsage && maxUsage > 0L,
        )
    }
    return cells
}

internal fun buildMonthlyWeekSummaries(
    startDate: LocalDate,
    endDate: LocalDate,
    summaries: List<PeriodDaySummary>,
): List<MonthlyWeekSummary> {
    val summaryByDate = summaries.associateBy { it.date }
    val weeks = mutableListOf<List<LocalDate>>()
    var cursor = startDate
    while (!cursor.isAfter(endDate)) {
        val weekEnd = minOf(cursor.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)), endDate)
        weeks += generateDateSequence(cursor, weekEnd)
        cursor = weekEnd.plusDays(1)
    }
    return weeks.mapIndexed { index, dates ->
        val items = dates.mapNotNull { summaryByDate[it] }
        val totalUsage = items.sumOf { it.usageMillis }
        val peakDay = items.maxByOrNull { it.usageMillis }
        MonthlyWeekSummary(
            label = AppText.t("stats_week_number", index + 1),
            totalUsageMillis = totalUsage,
            averageUsageMillis = if (items.isNotEmpty()) totalUsage / items.size else 0L,
            peakDayLabel = peakDay?.date?.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault())) ?: AppText.t("stats_none"),
        )
    }
}

internal fun buildYearMonthSummaries(
    year: Int,
    archives: List<DailyArchiveEntity>,
    snapshots: List<ArchivedAppSnapshot>,
): List<PeriodMonthSummary> {
    val snapshotsByMonth = snapshots.groupBy { LocalDate.parse(it.archiveDate).monthValue }
    return (1..12).map { month ->
        val monthArchives = archives.filter { LocalDate.parse(it.archiveDate).monthValue == month }
        val topAppLabel =
            snapshotsByMonth[month]
                ?.groupBy { it.packageName to it.label }
                ?.maxByOrNull { entry -> entry.value.sumOf { it.usageMillis } }
                ?.key
                ?.second
                ?: AppText.t("stats_none")
        PeriodMonthSummary(
            month = month,
            label = LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault())),
            usageMillis = monthArchives.sumOf { it.totalUsageMillis },
            savedMillis = monthArchives.sumOf { it.savedMillis },
            pointsNet = monthArchives.sumOf { it.pointsNet },
            activeDays = monthArchives.size,
            exceededMonths = monthArchives.any { it.controlExceededGroupCount > 0 || it.controlBlockEventCount > 0 },
            topAppLabel = topAppLabel,
        )
    }
}

internal fun buildQuarterSummaries(
    monthSummaries: List<PeriodMonthSummary>,
    snapshots: List<ArchivedAppSnapshot>,
): List<YearQuarterSummary> {
    return (0 until 4).map { index ->
        val quarterMonths = monthSummaries.filter { ((it.month - 1) / 3) == index }
        val quarterSnapshotTopApp =
            snapshots
                .filter { ((LocalDate.parse(it.archiveDate).monthValue - 1) / 3) == index }
                .groupBy { it.packageName to it.label }
                .maxByOrNull { entry -> entry.value.sumOf { it.usageMillis } }
                ?.key
                ?.second
                ?: AppText.t("stats_none")
        val bestMonth = quarterMonths.maxByOrNull { it.usageMillis }
        YearQuarterSummary(
            label = "Q${index + 1}",
            totalUsageMillis = quarterMonths.sumOf { it.usageMillis },
            bestMonthLabel = bestMonth?.label ?: AppText.t("stats_none"),
            bestMonthUsageMillis = bestMonth?.usageMillis ?: 0L,
            topAppLabel = quarterSnapshotTopApp,
        )
    }
}

internal fun buildAppFocusSectionData(
    tab: ReportTab,
    topApps: List<AppDisplayItem>,
    snapshots: List<ArchivedAppSnapshot>,
    totalUsage: Long,
): AppFocusSectionData {
    val stableApp =
        snapshots
            .groupBy { it.packageName to it.label }
            .maxWithOrNull(
                compareBy<Map.Entry<Pair<String, String>, List<ArchivedAppSnapshot>>> { entry ->
                    entry.value.map { it.archiveDate }.distinct().size
                }.thenBy { entry ->
                    entry.value.sumOf { it.usageMillis }
                },
            )
    val burstApp =
        snapshots
            .maxByOrNull { it.usageMillis }
    val totalUsageLabel =
        when (tab) {
            ReportTab.WEEK -> AppText.t("stats_weekly_top_apps_total", formatDuration(totalUsage))
            ReportTab.MONTH -> AppText.t("stats_monthly_top_apps_total", formatDuration(totalUsage))
            ReportTab.YEAR -> AppText.t("stats_yearly_top_apps_total", formatDuration(totalUsage))
            ReportTab.DAY -> formatDuration(totalUsage)
        }
    return AppFocusSectionData(
        title = AppText.t("stats_app_focus"),
        subtitle = AppText.t("stats_app_focus_description"),
        totalUsageLabel = totalUsageLabel,
        topApps = topApps,
        insights =
            listOf(
                AppFocusInsight(
                    title = AppText.t("stats_most_stable_app"),
                    value = stableApp?.key?.second ?: AppText.t("stats_none"),
                    detail =
                        stableApp?.value?.let { items ->
                            AppText.t(
                                "stats_app_focus_days_usage",
                                items.map { it.archiveDate }.distinct().size,
                                formatDuration(items.sumOf { it.usageMillis }),
                            )
                        } ?: AppText.t("stats_no_app_details_yet"),
                ),
                AppFocusInsight(
                    title = AppText.t("stats_burst_app"),
                    value = burstApp?.label ?: AppText.t("stats_none"),
                    detail =
                        burstApp?.let {
                            "${formatArchiveDate(it.archiveDate, "M/d")} · ${formatDuration(it.usageMillis)}"
                        } ?: AppText.t("stats_no_app_details_yet"),
                ),
            ),
    )
}

internal fun buildWeeklyTopAppRows(
    startDate: LocalDate,
    snapshots: List<ArchivedAppSnapshot>,
): List<WeeklyTopAppsRow> {
    val snapshotsByDate = snapshots.groupBy { LocalDate.parse(it.archiveDate) }
    return generateDateSequence(startDate, startDate.plusDays(6)).map { date ->
        WeeklyTopAppsRow(
            dayCode = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
            packages =
                snapshotsByDate[date]
                    .orEmpty()
                    .sortedByDescending { it.usageMillis }
                    .take(7)
                    .map { it.packageName },
        )
    }
}

internal fun buildDailyFocusSectionData(
    archive: DailyArchiveEntity,
    groupArchives: List<DailyGroupArchiveEntity>,
): DailyFocusSectionData {
    val controlGroups = groupArchives.filter { it.groupType == GroupType.CONTROL }
    val encourageGroups = groupArchives.filter { it.groupType == GroupType.ENCOURAGE }
    val exceededControlGroup = controlGroups.maxByOrNull { it.exceededMillisAtClose }
        ?.takeIf { it.exceededMillisAtClose > 0L }
    val bestControlGroup =
        exceededControlGroup
            ?: controlGroups.maxWithOrNull(
                compareBy<DailyGroupArchiveEntity> { it.remainingMillisAtClose }
                    .thenByDescending { it.completed }
                    .thenBy { it.dailyUsageMillis },
            )
    val bestEncourageGroup =
        encourageGroups.maxWithOrNull(
            compareBy<DailyGroupArchiveEntity> { it.earnedPoints }
                .thenBy { it.dailyUsageMillis }
                .thenByDescending { it.completed },
        )

    val controlProgress =
        when {
            controlGroups.isNotEmpty() ->
                archive.controlCompletedGroupCount.toFloat() / controlGroups.size.toFloat()
            archive.savedMillis > 0L -> 1f
            else -> 0f
        }.coerceIn(0f, 1f)
    val encourageProgress =
        when {
            encourageGroups.isNotEmpty() ->
                archive.encourageCompletedGroupCount.toFloat() / encourageGroups.size.toFloat()
            archive.encourageUsageMillis > 0L -> 1f
            else -> 0f
        }.coerceIn(0f, 1f)

    return DailyFocusSectionData(
        control =
            DailyModeSummary(
                title = AppText.t("stats_control_results"),
                description = if (archive.controlExceededGroupCount > 0) AppText.t("stats_today_over_limit_tighten_groups") else AppText.t("stats_control_groups_were_stable_overall"),
                primaryLabel = AppText.t("stats_time_saved"),
                primaryValue = formatDuration(archive.savedMillis),
                metrics =
                    listOf(
                        DailyFocusMetric(AppText.t("stats_met"), AppText.t("stats_value_groups_short", archive.controlCompletedGroupCount)),
                        DailyFocusMetric(AppText.t("stats_over_limit"), AppText.t("stats_value_groups_short", archive.controlExceededGroupCount)),
                        DailyFocusMetric(AppText.t("group_blocks"), AppText.t("stats_value_times_short", archive.controlBlockEventCount)),
                    ),
                progress = controlProgress,
                spotlightLabel =
                    when {
                        exceededControlGroup != null -> AppText.t("stats_label_7")
                        bestControlGroup != null -> AppText.t("stats_label")
                        else -> AppText.t("stats_control_group")
                    },
                spotlightValue =
                    when {
                        exceededControlGroup != null ->
                            AppText.t("stats_value_over_by_value_2", exceededControlGroup.groupName, formatDuration(exceededControlGroup.exceededMillisAtClose))
                        bestControlGroup != null && bestControlGroup.remainingMillisAtClose > 0L ->
                            AppText.t("stats_value_value_left_2", bestControlGroup.groupName, formatDuration(bestControlGroup.remainingMillisAtClose))
                        bestControlGroup != null -> bestControlGroup.groupName
                        else -> AppText.t("stats_no_group_archive")
                    },
                isWarning = archive.controlExceededGroupCount > 0 || archive.controlBlockEventCount > 0,
                groupItems = buildControlGroupProgressItems(controlGroups),
            ),
        encourage =
            DailyModeSummary(
                title = AppText.t("stats_encourage_progress"),
                description = if (archive.pointsNet >= 0.0) AppText.t("stats_encourage_usage_produced_positive_point_gains") else AppText.t("stats_negative_net_points_watch_redemptions"),
                primaryLabel = AppText.t("stats_net_points"),
                primaryValue = formatSignedPointsLocal(archive.pointsNet),
                metrics =
                    listOf(
                        DailyFocusMetric(AppText.t("stats_duration"), formatDuration(archive.encourageUsageMillis)),
                        DailyFocusMetric(AppText.t("stats_met"), AppText.t("stats_value_groups_short", archive.encourageCompletedGroupCount)),
                        DailyFocusMetric(AppText.t("stats_redemption"), AppText.t("stats_value_times_short", archive.redemptionCount)),
                    ),
                progress = encourageProgress,
                spotlightLabel = if (bestEncourageGroup != null) AppText.t("stats_best_encourage_group") else AppText.t("stats_encourage_group"),
                spotlightValue =
                    if (bestEncourageGroup != null) {
                        val points = formatSignedPointsLocal(bestEncourageGroup.earnedPoints)
                        "${bestEncourageGroup.groupName} · $points / ${formatDuration(bestEncourageGroup.dailyUsageMillis)}"
                    } else {
                        AppText.t("stats_no_group_archive")
                    },
                isWarning = archive.pointsNet < 0.0,
                groupItems = buildEncourageGroupProgressItems(encourageGroups),
            ),
    )
}

private fun buildControlGroupProgressItems(
    groups: List<DailyGroupArchiveEntity>,
): List<DailyGroupProgressItem> =
    groups
        .sortedWith(
            compareBy<DailyGroupArchiveEntity> { controlGroupSortRank(it) }
                .thenBy { it.remainingMillisAtClose.takeIf { remaining -> remaining > 0L } ?: Long.MAX_VALUE }
                .thenByDescending { it.dailyUsageMillis },
        )
        .map { group ->
            val targetMillis = controlTargetMillis(group)
            val periodUsage = group.periodUsageMillisAtClose.coerceAtLeast(0L)
            val progress =
                when {
                    targetMillis > 0L -> periodUsage.toFloat() / targetMillis.toFloat()
                    periodUsage > 0L -> 1f
                    else -> 0f
                }.coerceIn(0f, 1f)
            val exceeded = group.exceededMillisAtClose > 0L
            val atRisk = !exceeded && targetMillis > 0L && group.remainingMillisAtClose in 1L..(targetMillis * 15L / 100L).coerceAtLeast(1L)
            val started = group.dailyUsageMillis > 0L || periodUsage > 0L
            DailyGroupProgressItem(
                groupName = group.groupName,
                statusLabel =
                    when {
                        group.completed -> AppText.t("stats_met")
                        atRisk -> AppText.t("stats_status_tight")
                        started -> AppText.t("stats_status_in_progress")
                        else -> AppText.t("stats_status_not_started")
                    },
                leadingLabel = AppText.t("stats_period_usage"),
                leadingValue = AppText.t("stats_value_slash_value", formatDuration(periodUsage), formatDuration(targetMillis)),
                trailingLabel =
                    if (exceeded) {
                        AppText.t("stats_over_short")
                    } else {
                        AppText.t("stats_remaining_short")
                    },
                trailingValue =
                    if (exceeded) {
                        formatDuration(group.exceededMillisAtClose)
                    } else {
                        formatDuration(group.remainingMillisAtClose.coerceAtLeast(0L))
                    },
                progress = progress,
                progressLabel =
                    when {
                        exceeded -> AppText.t("stats_over_by_value_3", formatDuration(group.exceededMillisAtClose))
                        group.remainingMillisAtClose > 0L -> AppText.t("stats_remaining_value", formatDuration(group.remainingMillisAtClose))
                        else -> AppText.t("stats_met")
                    },
                helperLabel = controlGroupHelperLabel(group),
                isWarning = exceeded,
                isMuted = !started,
            )
        }

private fun buildEncourageGroupProgressItems(
    groups: List<DailyGroupArchiveEntity>,
): List<DailyGroupProgressItem> =
    groups
        .sortedWith(
            compareBy<DailyGroupArchiveEntity> { encourageGroupSortRank(it) }
                .thenByDescending { encourageTargetMillis(it).let { target -> if (target > 0L) it.dailyUsageMillis.toFloat() / target.toFloat() else 0f } }
                .thenByDescending { it.earnedPoints + it.rewardBonusPoints },
        )
        .map { group ->
            val targetMillis = encourageTargetMillis(group)
            val usage = group.dailyUsageMillis.coerceAtLeast(0L)
            val progress =
                when {
                    targetMillis > 0L -> usage.toFloat() / targetMillis.toFloat()
                    usage > 0L -> 1f
                    else -> 0f
                }.coerceIn(0f, 1f)
            val shortMillis = (targetMillis - usage).coerceAtLeast(0L)
            val overMillis = (usage - targetMillis).coerceAtLeast(0L)
            val points = group.earnedPoints + group.rewardBonusPoints
            DailyGroupProgressItem(
                groupName = group.groupName,
                statusLabel =
                    when {
                        group.completed -> AppText.t("stats_target_complete")
                        usage > 0L -> AppText.t("stats_status_in_progress")
                        else -> AppText.t("stats_status_not_started")
                    },
                leadingLabel = AppText.t("stats_goal_usage"),
                leadingValue = AppText.t("stats_value_slash_value", formatDuration(usage), formatDuration(targetMillis)),
                trailingLabel = AppText.t("stats_points_earned"),
                trailingValue = formatSignedPointsLocal(points),
                progress = progress,
                progressLabel =
                    when {
                        group.completed && overMillis > 0L -> AppText.t("stats_over_by_value_3", formatDuration(overMillis))
                        group.completed -> AppText.t("stats_met")
                        shortMillis > 0L -> AppText.t("stats_short_by_value", formatDuration(shortMillis))
                        else -> AppText.t("stats_status_not_started")
                    },
                helperLabel =
                    group.rewardBonusPoints
                        .takeIf { it > 0.0 }
                        ?.let { AppText.t("stats_bonus_points_value", formatSignedPointsLocal(it)) },
                isWarning = false,
                isMuted = usage <= 0L,
            )
        }

private fun controlTargetMillis(group: DailyGroupArchiveEntity): Long =
    group.effectiveLimitMillisAtClose.takeIf { it > 0L }
        ?: ((group.limitMinutes + group.bonusMinutes).coerceAtLeast(0).toLong() * 60_000L)

private fun encourageTargetMillis(group: DailyGroupArchiveEntity): Long =
    group.effectiveLimitMillisAtClose.takeIf { it > 0L }
        ?: (group.limitMinutes.coerceAtLeast(0).toLong() * 60_000L)

private fun controlGroupSortRank(group: DailyGroupArchiveEntity): Int {
    val targetMillis = controlTargetMillis(group)
    val atRisk =
        group.exceededMillisAtClose <= 0L &&
            targetMillis > 0L &&
            group.remainingMillisAtClose in 1L..(targetMillis * 15L / 100L).coerceAtLeast(1L)
    return when {
        group.exceededMillisAtClose > 0L -> 0
        atRisk || group.blockEventCount > 0 -> 1
        group.completed -> 2
        group.dailyUsageMillis > 0L || group.periodUsageMillisAtClose > 0L -> 3
        else -> 4
    }
}

private fun encourageGroupSortRank(group: DailyGroupArchiveEntity): Int =
    when {
        !group.completed && group.dailyUsageMillis > 0L -> 0
        group.completed -> 1
        else -> 2
    }

private fun controlGroupHelperLabel(group: DailyGroupArchiveEntity): String? =
    when {
        group.rewardExempted -> AppText.t("stats_reward_pass_used")
        group.bonusMinutes > 0 -> AppText.t("stats_bonus_time_value", formatDuration(group.bonusMinutes.toLong() * 60_000L))
        else -> null
    }

internal fun buildWindowFocusSectionData(
    selectedTab: ReportTab,
    archives: List<DailyArchiveEntity>,
    groupArchives: List<DailyGroupArchiveEntity>,
    activeDayCount: Int,
): WindowFocusSectionData {
    val controlGroups = groupArchives.filter { it.groupType == GroupType.CONTROL }
    val encourageGroups = groupArchives.filter { it.groupType == GroupType.ENCOURAGE }
    val controlGroupSummaries =
        controlGroups
            .groupBy { it.groupId to it.groupName }
            .map { (key, items) ->
                GroupWindowSummary(
                    groupId = key.first,
                    groupName = key.second,
                    usageMillis = items.sumOf { it.dailyUsageMillis },
                    remainingMillis = items.sumOf { it.remainingMillisAtClose },
                    exceededMillis = items.sumOf { it.exceededMillisAtClose },
                    blockCount = items.sumOf { it.blockEventCount },
                    earnedPoints = items.sumOf { it.earnedPoints },
                    completedCount = items.count { it.completed },
                )
            }
    val encourageGroupSummaries =
        encourageGroups
            .groupBy { it.groupId to it.groupName }
            .map { (key, items) ->
                GroupWindowSummary(
                    groupId = key.first,
                    groupName = key.second,
                    usageMillis = items.sumOf { it.dailyUsageMillis },
                    remainingMillis = items.sumOf { it.remainingMillisAtClose },
                    exceededMillis = items.sumOf { it.exceededMillisAtClose },
                    blockCount = items.sumOf { it.blockEventCount },
                    earnedPoints = items.sumOf { it.earnedPoints },
                    completedCount = items.count { it.completed },
                )
            }
    val totalSaved = archives.sumOf { it.savedMillis }
    val totalExceededGroups = archives.sumOf { it.controlExceededGroupCount }
    val totalControlCompleted = archives.sumOf { it.controlCompletedGroupCount }
    val totalBlockEvents = archives.sumOf { it.controlBlockEventCount }
    val totalEncourageCompleted = archives.sumOf { it.encourageCompletedGroupCount }
    val totalEncourageUsage = archives.sumOf { it.encourageUsageMillis }
    val totalRedemptions = archives.sumOf { it.redemptionCount }
    val pointsNet = archives.sumOf { it.pointsNet }
    val severeControl = controlGroupSummaries.maxByOrNull { it.exceededMillis }
        ?.takeIf { it.exceededMillis > 0L }
    val bestControl = severeControl ?: controlGroupSummaries.maxByOrNull { it.remainingMillis }
    val bestEncourage = encourageGroupSummaries.maxWithOrNull(
        compareBy<GroupWindowSummary> { it.earnedPoints }
            .thenBy { it.completedCount }
            .thenBy { it.usageMillis },
    )
    val controlProgress =
        if (controlGroups.isNotEmpty()) {
            totalControlCompleted.toFloat() / controlGroups.size.toFloat()
        } else {
            0f
        }.coerceIn(0f, 1f)
    val encourageProgress =
        if (encourageGroups.isNotEmpty()) {
            totalEncourageCompleted.toFloat() / encourageGroups.size.toFloat()
        } else {
            0f
        }.coerceIn(0f, 1f)
    val dayUnit =
        when (selectedTab) {
            ReportTab.WEEK -> AppText.t("group_this_week")
            ReportTab.MONTH -> AppText.t("stats_this_month")
            ReportTab.YEAR -> AppText.t("stats_last_365_days")
            ReportTab.DAY -> AppText.t("group_today")
        }

    return WindowFocusSectionData(
        control =
            DailyModeSummary(
                title = AppText.t("stats_control_results"),
                description = if (totalExceededGroups > 0) AppText.t("stats_value_had_value_group_over_limit_events", dayUnit, totalExceededGroups) else AppText.t("stats_value_control_stable_overall", dayUnit),
                primaryLabel = AppText.t("stats_time_saved"),
                primaryValue = formatDuration(totalSaved),
                metrics =
                    listOf(
                        DailyFocusMetric(AppText.t("stats_met"), AppText.t("stats_value_times_short", totalControlCompleted)),
                        DailyFocusMetric(AppText.t("stats_over_limit"), AppText.t("stats_value_times_short", totalExceededGroups)),
                        DailyFocusMetric(AppText.t("group_blocks"), AppText.t("stats_value_times_short", totalBlockEvents)),
                    ),
                progress = controlProgress,
                spotlightLabel = if (severeControl != null) AppText.t("stats_label_7") else AppText.t("stats_label"),
                spotlightValue =
                    when {
                        severeControl != null -> AppText.t("stats_value_over_by_value", severeControl.groupName, formatDuration(severeControl.exceededMillis))
                        bestControl != null && bestControl.remainingMillis > 0L -> AppText.t("stats_value_value_left", bestControl.groupName, formatDuration(bestControl.remainingMillis))
                        bestControl != null -> bestControl.groupName
                        else -> AppText.t("stats_no_archived_control_groups_yet")
                    },
                isWarning = totalExceededGroups > 0 || totalBlockEvents > 0,
            ),
        encourage =
            DailyModeSummary(
                title = AppText.t("stats_encourage_progress"),
                description = if (pointsNet >= 0.0) AppText.t("stats_net_points_stayed_positive", dayUnit) else AppText.t("stats_value_redemptions_exceeded_point_earnings", dayUnit),
                primaryLabel = AppText.t("stats_net_points"),
                primaryValue = formatSignedPointsLocal(pointsNet),
                metrics =
                    listOf(
                        DailyFocusMetric(AppText.t("stats_duration"), formatDuration(totalEncourageUsage)),
                        DailyFocusMetric(AppText.t("stats_met"), AppText.t("stats_value_times_short", totalEncourageCompleted)),
                        DailyFocusMetric(AppText.t("stats_redemption"), AppText.t("stats_value_times_short", totalRedemptions)),
                    ),
                progress = encourageProgress,
                spotlightLabel = if (bestEncourage != null) AppText.t("stats_best_encourage_group") else AppText.t("stats_encourage_group"),
                spotlightValue =
                    if (bestEncourage != null) {
                        "${bestEncourage.groupName} · ${formatSignedPointsLocal(bestEncourage.earnedPoints)} / ${formatDuration(bestEncourage.usageMillis)}"
                    } else {
                        AppText.t("stats_no_archived_encourage_groups_yet")
                    },
                isWarning = pointsNet < 0.0,
            ),
        highlights =
            listOf(
                DailyFocusMetric(AppText.t("stats_active_days"), AppText.t("stats_value_days_2", activeDayCount)),
                DailyFocusMetric(AppText.t("stats_daily_average"), formatDuration(if (activeDayCount > 0) archives.sumOf { it.totalUsageMillis } / activeDayCount else 0L)),
                DailyFocusMetric(AppText.t("stats_label_8"), formatDuration(totalSaved)),
            ),
    )
}

internal data class GroupWindowSummary(
    val groupId: String,
    val groupName: String,
    val usageMillis: Long,
    val remainingMillis: Long,
    val exceededMillis: Long,
    val blockCount: Int,
    val earnedPoints: Double,
    val completedCount: Int,
)

internal fun buildYearTimelineBuckets(archives: List<DailyArchiveEntity>): List<DailyTimelineBucket> {
    return (1..12).map { month ->
        val monthArchives = archives.filter { LocalDate.parse(it.archiveDate).monthValue == month }
        DailyTimelineBucket(
            hour = month - 1,
            label = AppText.t("stats_value", month),
            deviceMillis = monthArchives.sumOf { it.totalUsageMillis },
        )
    }
}

internal fun buildHeatmapSectionData(
    selectedTab: ReportTab,
    startDate: LocalDate,
    endDate: LocalDate,
    archives: List<DailyArchiveEntity>,
): HeatmapSectionData {
    val archiveByDate = archives.associateBy { it.archiveDate }
    return if (selectedTab == ReportTab.YEAR) {
        val monthValues =
            (1..12).map { month ->
                val monthArchives = archives.filter { LocalDate.parse(it.archiveDate).monthValue == month }
                val total = monthArchives.sumOf { it.totalUsageMillis }
                HeatmapDayData(
                    label = AppText.t("stats_value", month),
                    valueMillis = total,
                    exceeded = monthArchives.any { it.controlExceededGroupCount > 0 },
                    selected = total == archives
                        .groupBy { LocalDate.parse(it.archiveDate).monthValue }
                        .maxOfOrNull { entry -> entry.value.sumOf { it.totalUsageMillis } },
                )
            }
        HeatmapSectionData(
            title = AppText.t("stats_year_month_heatmap"),
            subtitle = AppText.t("stats_darker_colors_mark_months_that_deserve_more_review"),
            days = monthValues,
        )
    } else {
        val days = mutableListOf<HeatmapDayData>()
        var date = startDate
        val maxUsage = archives.maxOfOrNull { it.totalUsageMillis } ?: 0L
        while (!date.isAfter(endDate)) {
            val archive = archiveByDate[ArchiveDateUtils.formatDate(date)]
            days += HeatmapDayData(
                label = date.dayOfMonth.toString(),
                valueMillis = archive?.totalUsageMillis ?: 0L,
                exceeded = (archive?.controlExceededGroupCount ?: 0) > 0,
                selected = archive != null && archive.totalUsageMillis == maxUsage && maxUsage > 0L,
            )
            date = date.plusDays(1)
        }
        HeatmapSectionData(
            title = AppText.t("stats_month_calendar_heatmap"),
            subtitle = AppText.t("stats_each_day_gets_one_cell_making_peaks_dips"),
            days = days,
        )
    }
}

internal fun buildYearScopeSummary(
    title: String,
    startDate: LocalDate,
    endDate: LocalDate,
    archives: List<DailyArchiveEntity>,
): YearScopeSummary {
    val activeDays = archives.size
    val totalUsage = archives.sumOf { it.totalUsageMillis }
    val averageUsage = if (activeDays > 0) totalUsage / activeDays else 0L
    return YearScopeSummary(
        title = title,
        rangeLabel = "${startDate.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA))} - ${endDate.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA))}",
        totalUsage = formatDuration(totalUsage),
        averageUsage = formatDuration(averageUsage),
        activeDays = AppText.t("stats_value_days", activeDays),
        savedUsage = formatDuration(archives.sumOf { it.savedMillis }),
        pointsNet = formatSignedPointsLocal(archives.sumOf { it.pointsNet }),
    )
}

internal fun buildShareReportData(
    selectedTab: ReportTab,
    summary: DailyReportSummary,
    archives: List<DailyArchiveEntity>,
    windowFocus: WindowFocusSectionData?,
    topApps: List<AppDisplayItem>,
    timelineBuckets: List<DailyTimelineBucket> = emptyList(),
    dailyGoalMillis: Long = 0L,
    goalCompletionProgress: Float? = null,
    savedMillis: Long = archives.sumOf { it.savedMillis },
    pointsNet: Double = archives.sumOf { it.pointsNet },
    blockEventCount: Int = archives.sumOf { it.controlBlockEventCount },
    redemptionCount: Int = archives.sumOf { it.redemptionCount },
    nightUsageMillis: Long = timelineBuckets.filter { it.hour < 6 || it.hour >= 22 }.sumOf { it.deviceMillis },
    trendUsageMillis: List<Long> = archives.map { it.totalUsageMillis },
    comparisonLabel: String = summary.secondaryValue,
    dominantPeriod: String = "--",
): ShareReportData {
    val tabName =
        when (selectedTab) {
            ReportTab.DAY -> AppText.t("stats_daily_report")
            ReportTab.WEEK -> AppText.t("stats_weekly_report")
            ReportTab.MONTH -> AppText.t("stats_monthly_report")
            ReportTab.YEAR -> AppText.t("stats_yearly_report")
        }
    val bestDay = archives.maxByOrNull { it.totalUsageMillis }
    val calmDay = archives.filter { it.totalUsageMillis > 0L }.minByOrNull { it.totalUsageMillis }
    val insight =
        when {
            bestDay != null && calmDay != null ->
                AppText.t(
                    "stats_archived_peak_range",
                    formatArchiveDate(bestDay.archiveDate, "M/d"),
                    formatDuration(bestDay.totalUsageMillis),
                    formatArchiveDate(calmDay.archiveDate, "M/d"),
                    formatDuration(calmDay.totalUsageMillis),
                )
            bestDay != null -> AppText.t(
                "stats_archived_peak_single",
                formatArchiveDate(bestDay.archiveDate, "M/d"),
                formatDuration(bestDay.totalUsageMillis),
            )
            else -> AppText.t("stats_there_is_not_enough_archive_data_yet_the")
        }
    val metrics =
        windowFocus?.let {
            listOf(
                DailyFocusMetric(AppText.t("stats_control_savings"), it.control.primaryValue),
                DailyFocusMetric(AppText.t("stats_encourage_net_value"), it.encourage.primaryValue),
                DailyFocusMetric(AppText.t("stats_top_apps"), topApps.firstOrNull()?.label ?: AppText.t("stats_none")),
            )
        } ?: listOf(
            DailyFocusMetric(AppText.t("stats_label_5"), formatDuration(savedMillis)),
            DailyFocusMetric(AppText.t("stats_net_points"), formatSignedPointsLocal(pointsNet)),
            DailyFocusMetric(
                if (blockEventCount > 0) AppText.t("group_blocks") else AppText.t("stats_met"),
                if (blockEventCount > 0) AppText.t("stats_value_times_6", blockEventCount) else "${((goalCompletionProgress ?: 0f) * 100f).roundToInt()}%",
            ),
        )
    val statusTitle =
        when {
            dailyGoalMillis > 0L && archives.sumOf { it.totalUsageMillis } <= dailyGoalMillis -> AppText.t("stats_you_took_your_attention_back_today")
            savedMillis > 0L -> AppText.t("stats_today_kept_rhythm")
            pointsNet > 0.0 -> AppText.t("stats_you_saved_a_bit_of_momentum_today")
            else -> AppText.t("stats_you_completed_an_honest_review_today")
        }
    return ShareReportData(
        tab = selectedTab,
        title = "Tiny Vow $tabName",
        subtitle = summary.subtitle,
        slogan = AppText.t("stats_share_slogan"),
        statusTitle = statusTitle,
        primaryValue = summary.primaryValue,
        primaryLabel = AppText.t("stats_usage_duration"),
        metrics = metrics,
        insight = insight,
        topApps = topApps.take(5),
        totalUsageMillis = archives.sumOf { it.totalUsageMillis },
        goalMillis = dailyGoalMillis,
        goalProgress = goalCompletionProgress,
        savedMillis = savedMillis,
        pointsNet = pointsNet,
        blockEventCount = blockEventCount,
        redemptionCount = redemptionCount,
        controlCompletedGroupCount = archives.sumOf { it.controlCompletedGroupCount },
        controlExceededGroupCount = archives.sumOf { it.controlExceededGroupCount },
        encourageCompletedGroupCount = archives.sumOf { it.encourageCompletedGroupCount },
        encourageUsageMillis = archives.sumOf { it.encourageUsageMillis },
        nightUsageMillis = nightUsageMillis,
        hourlyUsageMillis = timelineBuckets.map { it.deviceMillis },
        timelineLabels = timelineBuckets.map { it.label },
        trendUsageMillis = trendUsageMillis,
        targetMillisPerBucket = dailyGoalMillis.takeIf { it > 0L }?.let { it / 24L },
        comparisonLabel = comparisonLabel,
        dominantPeriod = dominantPeriod,
    )
}

internal fun mergeArchivedAppSnapshots(items: List<DailyAppArchiveEntity>): List<ArchivedAppSnapshot> {
    return items
        .groupBy { it.archiveDate to it.packageName }
        .map { (_, groupedItems) ->
            val representative = groupedItems.maxByOrNull { it.dailyUsageMillis } ?: groupedItems.first()
            ArchivedAppSnapshot(
                archiveDate = representative.archiveDate,
                packageName = representative.packageName,
                label = representative.appLabel,
                usageMillis = groupedItems.maxOf { it.dailyUsageMillis },
                openCount = groupedItems.maxOf { it.openCount },
                sessionCount = groupedItems.maxOf { it.sessionCount },
                longestSessionMillis = groupedItems.maxOf { it.longestSessionMillis },
                nightUsageMillis = groupedItems.maxOf { it.nightUsageMillis },
                hourlyBuckets =
                    LongArray(24) { hour ->
                        groupedItems.maxOf { appItem -> appHourlyBucketAt(appItem, hour) }
                    },
            )
        }
}

internal fun buildArchivedWindowMetrics(items: List<ArchivedAppSnapshot>): WindowMetrics {
    return WindowMetrics(
        deviceUsageMillis = items.sumOf { it.usageMillis },
        deviceOpenCount = items.sumOf { it.openCount },
        longestSessionMillis = items.maxOfOrNull { it.longestSessionMillis } ?: 0L,
        nightUsageMillis = items.sumOf { it.nightUsageMillis },
    )
}

internal fun buildArchiveTimelineBuckets(archives: List<DailyArchiveEntity>): List<DailyTimelineBucket> {
    return archives.mapIndexed { index, archive ->
        DailyTimelineBucket(
            hour = index,
            label =
                LocalDate
                    .parse(archive.archiveDate)
                    .format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA)),
            deviceMillis = archive.totalUsageMillis,
        )
    }
}

internal fun buildArchivePeriodUsageStats(
    selectedTab: ReportTab,
    timelineBuckets: List<DailyTimelineBucket>,
): List<PeriodUsageStat> {
    if (timelineBuckets.isEmpty()) {
        return emptyList()
    }
    val labels =
        when (selectedTab) {
            ReportTab.WEEK -> listOf(AppText.t("stats_early_week"), AppText.t("stats_midweek"), AppText.t("stats_late_week"))
            ReportTab.MONTH -> listOf(AppText.t("stats_week_1"), AppText.t("stats_week_2"), AppText.t("stats_week_3"), AppText.t("stats_week_4"))
            ReportTab.YEAR -> listOf(AppText.t("stats_spring"), AppText.t("stats_summer"), AppText.t("stats_autumn"), AppText.t("stats_winter"))
            else -> emptyList()
        }
    if (labels.isEmpty()) {
        return emptyList()
    }
    val chunkSize = ceil(timelineBuckets.size.toDouble() / labels.size.toDouble()).toInt().coerceAtLeast(1)
    return labels.mapIndexed { index, label ->
        val startIndex = index * chunkSize
        val endIndex = minOf(startIndex + chunkSize, timelineBuckets.size)
        val segment =
            if (startIndex < timelineBuckets.size) {
                timelineBuckets.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        PeriodUsageStat(
            label = label,
            deviceMillis = segment.sumOf { it.deviceMillis },
        )
    }
}

internal suspend fun buildArchivedDayReportUiState(
    selectedDate: String?,
    recentArchives: List<DailyArchiveEntity>,
    archiveRepository: DailyArchiveRepository,
    updateState: ((DailyReportUiState) -> DailyReportUiState) -> Unit,
) {
    if (selectedDate == null || recentArchives.isEmpty()) {
        updateState { current ->
            current.copy(
                isRefreshing = false,
                heroState = SectionState.Empty,
                dailyFocusState = SectionState.Empty,
                windowFocusState = SectionState.Empty,
                heatmapState = SectionState.Empty,
                yearDualScopeState = SectionState.Empty,
                shareState = SectionState.Empty,
                timelineState = SectionState.Empty,
                topAppsState = SectionState.Empty,
                behaviorState = SectionState.Empty,
                comparisonState = SectionState.Empty,
                selectedArchiveDate = null,
                previousArchiveDate = null,
                nextArchiveDate = null,
                availableArchiveDates = emptyList(),
                placeholderTitle = AppText.t("stats_no_archived_daily_reports_yet"),
                placeholderDescription = AppText.t("stats_daily_reports_only_show_yesterday_and_earlier_archived"),
            )
        }
        return
    }

    val archivesDesc = recentArchives.sortedByDescending { it.archiveDate }
    val selectedIndex = archivesDesc.indexOfFirst { it.archiveDate == selectedDate }.takeIf { it >= 0 } ?: 0
    val selectedArchive = archivesDesc[selectedIndex]
    val previousArchive = archivesDesc.getOrNull(selectedIndex + 1)
    val nextArchive = archivesDesc.getOrNull(selectedIndex - 1)
    val currentSnapshots =
        mergeArchivedAppSnapshots(
            archiveRepository.getAppArchivesByDate(selectedArchive.archiveDate).first(),
        )
    val currentGroupArchives = archiveRepository.getGroupArchivesByDate(selectedArchive.archiveDate).first()
    val previousSnapshots =
        previousArchive?.let {
            mergeArchivedAppSnapshots(
                archiveRepository.getAppArchivesByDate(it.archiveDate).first(),
            )
        }.orEmpty()
    val currentMetrics = buildArchivedWindowMetrics(currentSnapshots)
    val previousMetrics = buildArchivedWindowMetrics(previousSnapshots)
    val timelineBuckets = buildArchivedDayTimelineBuckets(currentSnapshots)
    val periodUsage = buildPeriodUsageStats(timelineBuckets)
    val topApps =
        currentSnapshots
            .sortedByDescending { it.usageMillis }
            .take(10)
            .map {
                AppDisplayItem(
                    packageName = it.packageName,
                    label = it.label,
                    value = it.usageMillis,
                )
            }
    val overview =
        ScopeOverview(
            totalUsageMillis = selectedArchive.totalUsageMillis,
            openCount = currentMetrics.deviceOpenCount,
            activeBucketCount = timelineBuckets.count { it.deviceMillis > 0L },
            topApp = topApps.firstOrNull(),
        )
    val earlierArchives = archivesDesc.drop(selectedIndex + 1).take(7)
    val averagePerDayUsage =
        if (earlierArchives.isEmpty()) {
            0L
        } else {
            earlierArchives.sumOf { it.totalUsageMillis } / earlierArchives.size
        }
    val earlierMetrics =
        earlierArchives.map { archive ->
            buildArchivedWindowMetrics(
                mergeArchivedAppSnapshots(
                    archiveRepository.getAppArchivesByDate(archive.archiveDate).first(),
                ),
            )
        }
    val averageMetrics =
        WindowMetrics(
            deviceUsageMillis = averagePerDayUsage,
            deviceOpenCount = if (earlierMetrics.isEmpty()) 0 else earlierMetrics.map { it.deviceOpenCount }.average().roundToInt(),
            longestSessionMillis = earlierMetrics.map { it.longestSessionMillis }.average().roundToLongSafe(),
            nightUsageMillis = earlierMetrics.map { it.nightUsageMillis }.average().roundToLongSafe(),
        )
    val dailyGoalMillis =
        currentGroupArchives
            .filter { it.groupType == GroupType.CONTROL && it.limitPeriod == com.rrrrz.tinyvow.data.db.LimitPeriod.DAILY }
            .sumOf { it.effectiveLimitMillisAtClose }
    val controlGroupCount = currentGroupArchives.count { it.groupType == GroupType.CONTROL }
    val goalCompletionProgress =
        if (controlGroupCount > 0) {
            selectedArchive.controlCompletedGroupCount.toFloat() / controlGroupCount.toFloat()
        } else {
            null
        }
    val summary =
        buildArchivedDaySummary(
            archive = selectedArchive,
            overview = overview,
            previousMetrics = previousMetrics,
            averagePerDayUsage = averagePerDayUsage,
            dominantPeriod = periodUsage.maxByOrNull { it.deviceMillis }?.label ?: AppText.t("stats_all_day"),
        )
    val behaviorInsight = buildArchivedDayBehaviorInsight(currentSnapshots, timelineBuckets)
    val behaviorStructure = buildArchivedDayBehaviorStructure(currentSnapshots, timelineBuckets)
    val comparisons =
        buildArchivedDayComparisonMetrics(
            currentArchive = selectedArchive,
            currentMetrics = currentMetrics,
            previousArchive = previousArchive,
            previousMetrics = previousMetrics,
            averagePerDayUsage = averagePerDayUsage,
            averageMetrics = averageMetrics,
        )
    val timelineStateData =
        buildArchiveTimelineSectionData(
            selectedTab = ReportTab.DAY,
            timelineBuckets = timelineBuckets,
            nightUsageMillis = currentMetrics.nightUsageMillis,
            periodUsage = periodUsage,
            targetMillisPerBucket = dailyGoalMillis.takeIf { it > 0L }?.let { it / 24L },
        )
    val dailyFocusData =
        buildDailyFocusSectionData(
            archive = selectedArchive,
            groupArchives = currentGroupArchives,
        )
    val shareData =
        buildShareReportData(
            selectedTab = ReportTab.DAY,
            summary = summary,
            archives = listOf(selectedArchive),
            windowFocus = null,
            topApps = topApps,
            timelineBuckets = timelineBuckets,
            dailyGoalMillis = dailyGoalMillis,
            goalCompletionProgress = goalCompletionProgress,
            savedMillis = selectedArchive.savedMillis,
            pointsNet = selectedArchive.pointsNet,
            blockEventCount = selectedArchive.controlBlockEventCount,
            redemptionCount = selectedArchive.redemptionCount,
            nightUsageMillis = currentMetrics.nightUsageMillis,
            trendUsageMillis = (earlierArchives.asReversed() + selectedArchive).takeLast(7).map { it.totalUsageMillis },
            comparisonLabel = summary.secondaryValue,
            dominantPeriod = periodUsage.maxByOrNull { it.deviceMillis }?.label ?: AppText.t("stats_all_day"),
        )

    updateState { current ->
        current.copy(
            isRefreshing = false,
            heroState =
                SectionState.Ready(
                    HeroSectionData(
                        summary = summary,
                        overview = overview,
                        nightUsageMillis = currentMetrics.nightUsageMillis,
                        dailyGoalMillis = dailyGoalMillis,
                        goalCompletionProgress = goalCompletionProgress,
                    ),
                ),
            dailyFocusState = SectionState.Ready(dailyFocusData),
            windowFocusState = SectionState.Empty,
            heatmapState = SectionState.Empty,
            yearDualScopeState = SectionState.Empty,
            shareState = SectionState.Ready(shareData),
            timelineState =
                if (timelineBuckets.any { it.deviceMillis > 0L }) {
                    SectionState.Ready(timelineStateData)
                } else {
                    SectionState.Empty
                },
            topAppsState =
                if (topApps.isEmpty()) {
                    SectionState.Empty
                } else {
                    SectionState.Ready(TopAppsSectionData(usageTopApps = topApps))
                },
            behaviorState =
                if (behaviorStructure == null) {
                    SectionState.Empty
                } else {
                    SectionState.Ready(
                        BehaviorSectionData(
                            behaviorInsight = behaviorInsight,
                            structure = behaviorStructure,
                        ),
                    )
                },
            comparisonState =
                if (comparisons.isEmpty()) {
                    SectionState.Empty
                } else {
                    SectionState.Ready(ComparisonSectionData(comparisons = comparisons))
                },
            selectedArchiveDate = selectedArchive.archiveDate,
            previousArchiveDate = previousArchive?.archiveDate,
            nextArchiveDate = nextArchive?.archiveDate,
            availableArchiveDates = archivesDesc.map { it.archiveDate },
            placeholderTitle = null,
            placeholderDescription = null,
        )
    }
}

internal fun buildArchiveTimelineSectionData(
    selectedTab: ReportTab,
    timelineBuckets: List<DailyTimelineBucket>,
    nightUsageMillis: Long,
    periodUsage: List<PeriodUsageStat>,
    targetMillisPerBucket: Long? = null,
): TimelineSectionData {
    val peakBucket = timelineBuckets.maxByOrNull { it.deviceMillis }
    val peakPair =
        timelineBuckets
            .windowed(size = 2, step = 1, partialWindows = false)
            .map { buckets -> buckets to buckets.sumOf { it.deviceMillis } }
            .maxByOrNull { it.second }
    return TimelineSectionData(
        buckets = timelineBuckets,
        periodUsage = periodUsage,
        peakHourLabel = peakBucket?.label ?: "--",
        peakHourMillis = peakBucket?.deviceMillis ?: 0L,
        peakTwoHourLabel =
            peakPair?.first?.let {
                val tailLabel = if (selectedTab == ReportTab.DAY) dayHourLabel(it.last().hour + 1) else it.last().label
                "${it.first().label}-$tailLabel"
            } ?: "--",
        peakTwoHourMillis = peakPair?.second ?: 0L,
        nightUsageMillis = nightUsageMillis,
        targetMillisPerBucket = targetMillisPerBucket,
    )
}

internal fun buildArchivedReportSummary(
    selectedTab: ReportTab,
    startDate: LocalDate,
    endDate: LocalDate,
    overview: ScopeOverview,
    previousMetrics: WindowMetrics,
    dominantPeriod: String,
    pointsNet: Double,
    redemptionCount: Int,
    averagePerDayUsage: Long,
): DailyReportSummary {
    val title =
        when (selectedTab) {
            ReportTab.DAY -> AppText.t("stats_archived_daily_reports")
            ReportTab.WEEK -> AppText.t("stats_last_7_days")
            ReportTab.MONTH -> AppText.t("stats_last_30_days")
            ReportTab.YEAR -> AppText.t("stats_yearly_trend")
        }
    val subtitle =
        "${startDate.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA))} - ${endDate.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA))}"
    val usageMessage =
        when {
            previousMetrics.deviceUsageMillis > 0L &&
                overview.totalUsageMillis > previousMetrics.deviceUsageMillis * 1.15f -> AppText.t("stats_heavier_than_previous_window")
            previousMetrics.deviceUsageMillis > 0L &&
                overview.totalUsageMillis < previousMetrics.deviceUsageMillis * 0.85f -> AppText.t("stats_lighter_than_previous_window")
            else -> AppText.t("stats_close_to_previous_window")
        }
    val formattedPoints = String.format(Locale.CHINA, "%.1f", pointsNet)
    val pointTag = if (pointsNet >= 0) AppText.t("stats_net_points_value", formattedPoints) else AppText.t("stats_net_points_value_2", formattedPoints)
    return DailyReportSummary(
        title = title,
        subtitle = subtitle,
        capturedAt = AppText.t("stats_archived_until_value", endDate.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA))),
        message = AppText.t("stats_value_mainly_concentrated_in_value", usageMessage, dominantPeriod),
        primaryValue = formatDuration(overview.totalUsageMillis),
        secondaryValue = deltaDescription(overview.totalUsageMillis, previousMetrics.deviceUsageMillis, AppText.t("stats_vs_previous_window")),
        tertiaryValue = AppText.t("stats_daily_average_value", formatDuration(averagePerDayUsage)),
        tags = listOf(pointTag, AppText.t("stats_value_redemptions", redemptionCount), dominantPeriod),
    )
}

internal fun buildArchivedComparisonMetrics(
    selectedTab: ReportTab,
    overview: ScopeOverview,
    currentMetrics: WindowMetrics,
    previousMetrics: WindowMetrics,
    averagePerDayUsage: Long,
): List<ComparisonMetric> {
    return listOf(
        ComparisonMetric(
            label = AppText.t("stats_window_total"),
            todayValue = formatDuration(overview.totalUsageMillis),
            yesterdayDelta = deltaDescription(overview.totalUsageMillis, previousMetrics.deviceUsageMillis, AppText.t("stats_vs_previous_window")),
            averageDelta = AppText.t("stats_daily_average_value", formatDuration(averagePerDayUsage)),
            chartData =
                ComparisonChartData(
                    currentValue = overview.totalUsageMillis,
                    previousValue = previousMetrics.deviceUsageMillis,
                    averageValue = averagePerDayUsage,
                    currentLabel = AppText.t("stats_window_total"),
                    previousLabel = AppText.t("stats_vs_previous_window"),
                    averageLabel = AppText.t("stats_daily_average"),
                ),
        ),
        ComparisonMetric(
            label = AppText.t("stats_launches"),
            todayValue = AppText.t("stats_value_times_12", overview.openCount),
            yesterdayDelta = deltaDescription(overview.openCount.toLong(), previousMetrics.deviceOpenCount.toLong(), AppText.t("stats_vs_previous_window"), countUnit = AppText.t("stats_times")),
            averageDelta = AppText.t("stats_daily_average_times", (overview.openCount.toFloat() / archiveWindowDays(selectedTab).coerceAtLeast(1)).roundToInt()),
        ),
        ComparisonMetric(
            label = AppText.t("stats_night_use"),
            todayValue = formatDuration(currentMetrics.nightUsageMillis),
            yesterdayDelta = deltaDescription(currentMetrics.nightUsageMillis, previousMetrics.nightUsageMillis, AppText.t("stats_vs_previous_window")),
            averageDelta = null,
        ),
        ComparisonMetric(
            label = AppText.t("stats_label_11"),
            todayValue = formatDuration(currentMetrics.longestSessionMillis),
            yesterdayDelta = deltaDescription(currentMetrics.longestSessionMillis, previousMetrics.longestSessionMillis, AppText.t("stats_vs_previous_window")),
            averageDelta = null,
        ),
    )
}

internal fun buildArchivedDayTimelineBuckets(items: List<ArchivedAppSnapshot>): List<DailyTimelineBucket> {
    return (0 until 24).map { hour ->
        DailyTimelineBucket(
            hour = hour,
            label = dayHourLabel(hour),
            deviceMillis = items.sumOf { snapshot -> snapshot.hourlyBuckets.getOrElse(hour) { 0L } },
        )
    }
}

internal fun buildArchivedDaySummary(
    archive: DailyArchiveEntity,
    overview: ScopeOverview,
    previousMetrics: WindowMetrics,
    averagePerDayUsage: Long,
    dominantPeriod: String,
): DailyReportSummary {
    val message =
        when {
            previousMetrics.deviceUsageMillis > 0L &&
                overview.totalUsageMillis > previousMetrics.deviceUsageMillis * 1.15f -> AppText.t("stats_usage_on_this_day_was_noticeably_higher_than")
            previousMetrics.deviceUsageMillis > 0L &&
                overview.totalUsageMillis < previousMetrics.deviceUsageMillis * 0.85f -> AppText.t("stats_usage_lower_than_previous_archive")
            else -> AppText.t("stats_usage_close_to_previous_archive")
        }
    return DailyReportSummary(
        title = AppText.t("stats_total_phone_usage"),
        subtitle = "",
        capturedAt = "",
        message = AppText.t("stats_value_was_mainly_concentrated_in_value", message, dominantPeriod),
        primaryValue = formatDuration(overview.totalUsageMillis),
        secondaryValue = formatDuration(kotlin.math.abs(overview.totalUsageMillis - previousMetrics.deviceUsageMillis)),
        tertiaryValue = formatDuration(averagePerDayUsage),
        tags = listOf(AppText.t("stats_net_points_value_3", formatSignedPointsLocal(archive.pointsNet)), AppText.t("stats_value_redemptions_2", archive.redemptionCount), AppText.t("stats_saved_duration_value", formatDuration(archive.savedMillis))),
    )
}

internal fun buildArchivedDayBehaviorInsight(
    items: List<ArchivedAppSnapshot>,
    timelineBuckets: List<DailyTimelineBucket>,
): UsageBehaviorInsight? {
    if (items.isEmpty() && timelineBuckets.none { it.deviceMillis > 0L }) {
        return null
    }
    val peakHour = timelineBuckets.maxByOrNull { it.deviceMillis }
    val peakTwoHour =
        timelineBuckets
            .windowed(size = 2, step = 1, partialWindows = false)
            .map { buckets -> buckets to buckets.sumOf { it.deviceMillis } }
            .maxByOrNull { it.second }
    val longestSession =
        items.maxByOrNull { it.longestSessionMillis }?.let {
            AppDisplayItem(
                packageName = it.packageName,
                label = it.label,
                value = it.longestSessionMillis,
            )
        }
    val mostOpened =
        items.maxByOrNull { it.openCount }?.let {
            AppDisplayItem(
                packageName = it.packageName,
                label = it.label,
                value = it.openCount.toLong(),
            )
        }
    val nightLeader =
        items.maxByOrNull { it.nightUsageMillis }?.takeIf { it.nightUsageMillis > 0L }?.let {
            AppDisplayItem(
                packageName = it.packageName,
                label = it.label,
                value = it.nightUsageMillis,
            )
        }
    val totalSessions = items.sumOf { it.sessionCount }
    val totalUsage = items.sumOf { it.usageMillis }
    val activeHours = timelineBuckets.count { it.deviceMillis > 0L }
    return UsageBehaviorInsight(
        peakHourLabel = peakHour?.label ?: "--",
        peakHourMillis = peakHour?.deviceMillis ?: 0L,
        peakTwoHourLabel =
            peakTwoHour?.first?.let { "${it.first().label}-${dayHourLabel(it.last().hour + 1)}" } ?: "--",
        peakTwoHourMillis = peakTwoHour?.second ?: 0L,
        nightUsageMillis = items.sumOf { it.nightUsageMillis },
        longestSession = longestSession,
        averageSessionMillis = if (totalSessions > 0) totalUsage / totalSessions else 0L,
        activeHourCount = activeHours,
        shortSessionRatio = 0f,
        reopenIntensity = if (activeHours > 0) items.sumOf { it.openCount }.toFloat() / activeHours.toFloat() else 0f,
        predictedSleepLabel = if (nightLeader != null) "${nightLeader.label} · ${formatDuration(nightLeader.value)}" else AppText.t("stats_no_records_yet"),
        predictedSleepDurationLabel = if (mostOpened != null) AppText.t("stats_value_value_times", mostOpened.label, mostOpened.value.toInt()) else AppText.t("stats_no_records_yet"),
        beforeSleep = BehaviorAppMoment(
            label = AppText.t("stats_night_top_app"),
            packageName = nightLeader?.packageName,
            appLabel = nightLeader?.label,
        ),
        afterWake = BehaviorAppMoment(
            label = AppText.t("stats_most_launches"),
            packageName = mostOpened?.packageName,
            appLabel = mostOpened?.label,
        ),
    )
}

internal fun buildArchivedDayBehaviorStructure(
    items: List<ArchivedAppSnapshot>,
    timelineBuckets: List<DailyTimelineBucket>,
): DailyBehaviorStructureData? {
    val totalUsage = items.sumOf { it.usageMillis }
    if (totalUsage <= 0L) return null

    val totalSessions = items.sumOf { it.sessionCount }
    val sessionsPerHour = totalSessions.toFloat() / (totalUsage.toFloat() / 3_600_000f).coerceAtLeast(1f)
    val fragmentationLevel =
        when {
            sessionsPerHour < 2f -> AppText.t("stats_behavior_level_low")
            sessionsPerHour < 5f -> AppText.t("stats_behavior_level_medium")
            else -> AppText.t("stats_behavior_level_high")
        }

    val topAppUsage = items.maxOfOrNull { it.usageMillis } ?: 0L
    val concentrationRatio = (topAppUsage.toFloat() / totalUsage.toFloat()).coerceIn(0f, 1f)
    val concentrationLevel =
        when {
            concentrationRatio < 0.35f -> AppText.t("stats_behavior_concentration_spread")
            concentrationRatio < 0.6f -> AppText.t("stats_behavior_concentration_balanced")
            else -> AppText.t("stats_behavior_concentration_focused")
        }

    val activeHours = timelineBuckets.filter { it.deviceMillis > 0L }.map { it.hour }
    val activeSpanHours =
        if (activeHours.isEmpty()) {
            0
        } else {
            activeHours.last() - activeHours.first() + 1
        }
    val activeSpanLevel =
        when {
            activeSpanHours <= 4 -> AppText.t("stats_behavior_span_compact")
            activeSpanHours <= 10 -> AppText.t("stats_behavior_span_moderate")
            else -> AppText.t("stats_behavior_span_long")
        }

    val nightUsage = items.sumOf { it.nightUsageMillis }
    val nightShare = (nightUsage.toFloat() / totalUsage.toFloat()).coerceIn(0f, 1f)
    val nightLevel =
        when {
            nightShare < 0.15f -> AppText.t("stats_behavior_night_low")
            nightShare < 0.35f -> AppText.t("stats_behavior_night_moderate")
            else -> AppText.t("stats_behavior_night_high")
        }

    return DailyBehaviorStructureData(
        metrics =
            listOf(
                DailyBehaviorStructureMetric(
                    label = AppText.t("stats_behavior_fragmentation"),
                    value = AppText.t("stats_behavior_sessions_per_hour_value", fragmentationLevel, sessionsPerHour),
                    visualRatio = (sessionsPerHour / 8f).coerceIn(0f, 1f),
                ),
                DailyBehaviorStructureMetric(
                    label = AppText.t("stats_behavior_app_concentration"),
                    value = AppText.t("stats_behavior_percent_value", concentrationLevel, (concentrationRatio * 100f).roundToInt()),
                    visualRatio = concentrationRatio,
                ),
                DailyBehaviorStructureMetric(
                    label = AppText.t("stats_behavior_active_span"),
                    value = AppText.t("stats_behavior_hours_value", activeSpanLevel, activeSpanHours),
                    visualRatio = (activeSpanHours / 24f).coerceIn(0f, 1f),
                ),
                DailyBehaviorStructureMetric(
                    label = AppText.t("stats_behavior_night_dependency"),
                    value = AppText.t("stats_behavior_percent_value", nightLevel, (nightShare * 100f).roundToInt()),
                    visualRatio = nightShare,
                ),
            ),
    )
}

internal fun buildArchivedDayComparisonMetrics(
    currentArchive: DailyArchiveEntity,
    currentMetrics: WindowMetrics,
    previousArchive: DailyArchiveEntity?,
    previousMetrics: WindowMetrics,
    averagePerDayUsage: Long,
    averageMetrics: WindowMetrics = WindowMetrics(0L, 0, 0L, 0L),
): List<ComparisonMetric> {
    if (previousArchive == null) {
        return emptyList()
    }
    return listOf(
        ComparisonMetric(
            label = AppText.t("stats_usage_duration"),
            todayValue = formatDuration(currentArchive.totalUsageMillis),
            yesterdayDelta = deltaDescription(currentArchive.totalUsageMillis, previousArchive.totalUsageMillis, AppText.t("stats_vs_previous_archive")),
            averageDelta = if (averagePerDayUsage > 0L) AppText.t("stats_last_7_archived_day_average", formatDuration(averagePerDayUsage)) else null,
            chartData =
                ComparisonChartData(
                    currentValue = currentArchive.totalUsageMillis,
                    previousValue = previousArchive.totalUsageMillis,
                    averageValue = averagePerDayUsage.takeIf { it > 0L },
                    currentLabel = formatDuration(currentArchive.totalUsageMillis),
                    previousLabel = formatDuration(previousArchive.totalUsageMillis),
                    averageLabel = averagePerDayUsage.takeIf { it > 0L }?.let(::formatDuration),
                ),
        ),
        ComparisonMetric(
            label = AppText.t("stats_launches"),
            todayValue = AppText.t("stats_value_times_4", currentMetrics.deviceOpenCount),
            yesterdayDelta = deltaDescription(currentMetrics.deviceOpenCount.toLong(), previousMetrics.deviceOpenCount.toLong(), AppText.t("stats_vs_previous_archive"), countUnit = AppText.t("stats_times")),
            averageDelta = null,
            chartData =
                ComparisonChartData(
                    currentValue = currentMetrics.deviceOpenCount.toLong(),
                    previousValue = previousMetrics.deviceOpenCount.toLong(),
                    averageValue = averageMetrics.deviceOpenCount.toLong().takeIf { it > 0L },
                    currentLabel = currentMetrics.deviceOpenCount.toString(),
                    previousLabel = previousMetrics.deviceOpenCount.toString(),
                    averageLabel = averageMetrics.deviceOpenCount.takeIf { it > 0 }?.toString(),
                ),
        ),
        ComparisonMetric(
            label = AppText.t("stats_night_use"),
            todayValue = formatDuration(currentMetrics.nightUsageMillis),
            yesterdayDelta = deltaDescription(currentMetrics.nightUsageMillis, previousMetrics.nightUsageMillis, AppText.t("stats_vs_previous_archive")),
            averageDelta = null,
            chartData =
                ComparisonChartData(
                    currentValue = currentMetrics.nightUsageMillis,
                    previousValue = previousMetrics.nightUsageMillis,
                    averageValue = averageMetrics.nightUsageMillis.takeIf { it > 0L },
                    currentLabel = formatDuration(currentMetrics.nightUsageMillis),
                    previousLabel = formatDuration(previousMetrics.nightUsageMillis),
                    averageLabel = averageMetrics.nightUsageMillis.takeIf { it > 0L }?.let(::formatDuration),
                ),
        ),
        ComparisonMetric(
            label = AppText.t("stats_label_11"),
            todayValue = formatDuration(currentMetrics.longestSessionMillis),
            yesterdayDelta = deltaDescription(currentMetrics.longestSessionMillis, previousMetrics.longestSessionMillis, AppText.t("stats_vs_previous_archive")),
            averageDelta = null,
            chartData =
                ComparisonChartData(
                    currentValue = currentMetrics.longestSessionMillis,
                    previousValue = previousMetrics.longestSessionMillis,
                    averageValue = averageMetrics.longestSessionMillis.takeIf { it > 0L },
                    currentLabel = formatDuration(currentMetrics.longestSessionMillis),
                    previousLabel = formatDuration(previousMetrics.longestSessionMillis),
                    averageLabel = averageMetrics.longestSessionMillis.takeIf { it > 0L }?.let(::formatDuration),
                ),
        ),
    )
}

internal suspend fun buildDailyReportUiState(
    context: Context,
    zoneId: ZoneId,
    usageRepository: UsageRepository,
    groupsWithApps: List<AppGroupWithApps>,
    installedApps: List<ManagedApp>,
    updateState: ((DailyReportUiState) -> DailyReportUiState) -> Unit,
) {
    val today = LocalDate.now(zoneId)
    val todayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val nowMillis = System.currentTimeMillis()
    val installedAppMap = installedApps.associateBy { it.packageName }
    val managedPackages = groupsWithApps.flatMap { it.packageNames }.toSet()

    val todayUsageStats = usageRepository.getUsageStats(todayStart, nowMillis)
    val todayOpenCounts = usageRepository.getAppOpenCount(todayStart, nowMillis)
    val todaySessions = usageRepository.getUsageSessions(todayStart, nowMillis).filter { it.endTime > it.startTime }
    val devicePackages = selectDevicePackages(
        context = context,
        installedAppMap = installedAppMap,
        managedPackages = managedPackages,
        packages = todayUsageStats.keys + todayOpenCounts.keys + todaySessions.map { it.packageName },
    )
    val devicePackageSet = devicePackages.keys

    val deviceUsageStats = todayUsageStats.filterKeys { it in devicePackageSet }
    val deviceOpenCounts = todayOpenCounts.filterKeys { it in devicePackageSet }
    val deviceSessions = todaySessions.filter { it.packageName in devicePackageSet }

    val timelineBuckets = buildTimelineBuckets(todayStart, nowMillis, deviceSessions)
    val periodUsage = buildPeriodUsageStats(timelineBuckets)
    val timelineInsight = buildTimelineSectionData(timelineBuckets)

    val usageTopApps = deviceUsageStats.toList()
        .sortedByDescending { it.second }
        .filter { it.second > 0L }
        .take(10)
        .map { (packageName, value) ->
            AppDisplayItem(packageName, devicePackages[packageName]?.label ?: packageName, value)
        }

    val yesterdayMetrics = buildWindowMetrics(
        context = context,
        zoneId = zoneId,
        date = today.minusDays(1),
        usageRepository = usageRepository,
        installedAppMap = installedAppMap,
        managedPackages = managedPackages,
    )
    val deviceOverview = ScopeOverview(
        totalUsageMillis = deviceUsageStats.values.sum(),
        openCount = deviceOpenCounts.values.sum(),
        activeBucketCount = timelineBuckets.count { it.deviceMillis > 0L },
        topApp = usageTopApps.firstOrNull(),
    )

    val provisionalAverageMetrics = WindowMetrics(
        deviceUsageMillis = 0L,
        deviceOpenCount = 0,
        longestSessionMillis = 0L,
        nightUsageMillis = 0L,
    )
    val summary = buildDailyReportSummary(
        date = today,
        zoneId = zoneId,
        nowMillis = nowMillis,
        deviceOverview = deviceOverview,
        periodUsage = periodUsage,
        yesterdayMetrics = yesterdayMetrics,
        averageMetrics = provisionalAverageMetrics,
    )

    updateState { current ->
        current.copy(
            heroState = SectionState.Ready(
                HeroSectionData(
                    summary = summary,
                    overview = deviceOverview,
                    nightUsageMillis = timelineInsight.nightUsageMillis,
                ),
            ),
            timelineState = if (timelineBuckets.isEmpty() && periodUsage.all { it.deviceMillis == 0L }) {
                SectionState.Empty
            } else {
                SectionState.Ready(
                    timelineInsight.copy(
                        buckets = timelineBuckets,
                        periodUsage = periodUsage,
                    ),
                )
            },
            topAppsState = if (usageTopApps.isEmpty()) {
                SectionState.Empty
            } else {
                SectionState.Ready(TopAppsSectionData(usageTopApps = usageTopApps))
            },
        )
    }

    val behaviorInsight = buildBehaviorInsight(
        context = context,
        zoneId = zoneId,
        anchorDate = today,
        usageRepository = usageRepository,
        installedAppMap = installedAppMap,
        managedPackages = managedPackages,
        timelineBuckets = timelineBuckets,
        deviceSessions = deviceSessions,
        deviceOpenCounts = deviceOpenCounts,
    )
    updateState { current ->
        current.copy(
            behaviorState = SectionState.Ready(BehaviorSectionData(behaviorInsight = behaviorInsight)),
        )
    }

    val recentMetrics = (1L..7L).map { offset ->
        buildWindowMetrics(
            context = context,
            zoneId = zoneId,
            date = today.minusDays(offset),
            usageRepository = usageRepository,
            installedAppMap = installedAppMap,
            managedPackages = managedPackages,
        )
    }
    val averageMetrics = WindowMetrics(
        deviceUsageMillis = recentMetrics.map { it.deviceUsageMillis }.average().roundToLongSafe(),
        deviceOpenCount = recentMetrics.map { it.deviceOpenCount }.average().roundToInt(),
        longestSessionMillis = recentMetrics.map { it.longestSessionMillis }.average().roundToLongSafe(),
        nightUsageMillis = recentMetrics.map { it.nightUsageMillis }.average().roundToLongSafe(),
    )
    val refinedSummary = buildDailyReportSummary(
        date = today,
        zoneId = zoneId,
        nowMillis = nowMillis,
        deviceOverview = deviceOverview,
        periodUsage = periodUsage,
        yesterdayMetrics = yesterdayMetrics,
        averageMetrics = averageMetrics,
    )
    val longestSessionValue = deviceSessions.maxOfOrNull { it.endTime - it.startTime } ?: 0L
    val comparisons = buildComparisonMetrics(
        deviceOverview = deviceOverview,
        behaviorInsight = behaviorInsight,
        longestSessionMillis = longestSessionValue,
        yesterdayMetrics = yesterdayMetrics,
        averageMetrics = averageMetrics,
    )

    updateState { current ->
        current.copy(
            isRefreshing = false,
            heroState = SectionState.Ready(
                HeroSectionData(
                    summary = refinedSummary,
                    overview = deviceOverview,
                    nightUsageMillis = timelineInsight.nightUsageMillis,
                ),
            ),
            comparisonState = if (comparisons.isEmpty()) {
                SectionState.Empty
            } else {
                SectionState.Ready(ComparisonSectionData(comparisons = comparisons))
            },
        )
    }
}

internal fun buildPlaceholderUiState(tab: ReportTab): DailyReportUiState {
    val title = when (tab) {
        ReportTab.WEEK -> AppText.t("stats_weekly_reports_are_coming_soon")
        ReportTab.MONTH -> AppText.t("stats_monthly_reports_are_coming_soon")
        ReportTab.YEAR -> AppText.t("stats_yearly_reports_are_coming_soon")
        ReportTab.DAY -> AppText.t("stats_daily_report")
    }
    val description = when (tab) {
        ReportTab.WEEK -> AppText.t("stats_weekly_trends_and_streak_insights_will_open_after")
        ReportTab.MONTH -> AppText.t("stats_monthly_views_will_analyze_rhythm_phases_and_structural")
        ReportTab.YEAR -> AppText.t("stats_year_view_description")
        ReportTab.DAY -> ""
    }
    return DailyReportUiState(
        isPermissionGranted = true,
        selectedTab = tab,
        isRefreshing = false,
        placeholderTitle = title,
        placeholderDescription = description,
    )
}

internal suspend fun buildWindowMetrics(
    context: Context,
    zoneId: ZoneId,
    date: LocalDate,
    usageRepository: UsageRepository,
    installedAppMap: Map<String, ManagedApp>,
    managedPackages: Set<String>,
): WindowMetrics {
    val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val usageStats = usageRepository.getUsageStats(startMillis, endMillis)
    val openCounts = usageRepository.getAppOpenCount(startMillis, endMillis)
    val sessions = usageRepository.getUsageSessions(startMillis, endMillis).filter { it.endTime > it.startTime }
    val devicePackages = selectDevicePackages(
        context = context,
        installedAppMap = installedAppMap,
        managedPackages = managedPackages,
        packages = usageStats.keys + openCounts.keys + sessions.map { it.packageName },
    ).keys
    val deviceSessions = sessions.filter { it.packageName in devicePackages }
    return WindowMetrics(
        deviceUsageMillis = usageStats.filterKeys { it in devicePackages }.values.sum(),
        deviceOpenCount = openCounts.filterKeys { it in devicePackages }.values.sum(),
        longestSessionMillis = deviceSessions.maxOfOrNull { it.endTime - it.startTime } ?: 0L,
        nightUsageMillis = buildTimelineBuckets(startMillis, endMillis, deviceSessions)
            .filter { it.hour < 6 || it.hour >= 22 }
            .sumOf { it.deviceMillis },
    )
}

internal suspend fun buildBehaviorInsight(
    context: Context,
    zoneId: ZoneId,
    anchorDate: LocalDate,
    usageRepository: UsageRepository,
    installedAppMap: Map<String, ManagedApp>,
    managedPackages: Set<String>,
    timelineBuckets: List<DailyTimelineBucket>,
    deviceSessions: List<AppSession>,
    deviceOpenCounts: Map<String, Int>,
): UsageBehaviorInsight {
    val peakHour = timelineBuckets.maxByOrNull { it.deviceMillis }
    val peakTwoHour = timelineBuckets.windowed(size = 2, step = 1, partialWindows = false)
        .map { buckets -> buckets to buckets.sumOf { it.deviceMillis } }
        .maxByOrNull { it.second }

    val averageSessionMillis = if (deviceSessions.isNotEmpty()) {
        deviceSessions.sumOf { it.endTime - it.startTime } / deviceSessions.size
    } else {
        0L
    }
    val shortSessionRatio = if (deviceSessions.isNotEmpty()) {
        deviceSessions.count { (it.endTime - it.startTime) <= 60_000L }.toFloat() / deviceSessions.size.toFloat()
    } else {
        0f
    }
    val reopenIntensity = if (timelineBuckets.count { it.deviceMillis > 0L } > 0) {
        deviceOpenCounts.values.sum().toFloat() / timelineBuckets.count { it.deviceMillis > 0L }.toFloat()
    } else {
        0f
    }
    val longestSession = deviceSessions.maxByOrNull { it.endTime - it.startTime }?.let { session ->
        AppDisplayItem(
            packageName = session.packageName,
            label = resolveAppLabel(context, session.packageName, installedAppMap),
            value = session.endTime - session.startTime,
        )
    }

    val sleepWindowStart = anchorDate.minusDays(1).atTime(18, 0).atZone(zoneId).toInstant().toEpochMilli()
    val sleepWindowEnd = anchorDate.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
    val sleepSessions = usageRepository.getUsageSessions(sleepWindowStart, sleepWindowEnd).filter { it.endTime > it.startTime }
    val sleepPackages = selectDevicePackages(
        context = context,
        installedAppMap = installedAppMap,
        managedPackages = managedPackages,
        packages = sleepSessions.map { it.packageName },
    ).keys
    val filteredSleepSessions = sleepSessions.filter { it.packageName in sleepPackages }.sortedBy { it.startTime }
    val beforeSleep = filteredSleepSessions.lastOrNull { it.startTime < anchorDate.atTime(3, 0).atZone(zoneId).toInstant().toEpochMilli() }
    val afterWake = filteredSleepSessions.firstOrNull { it.startTime >= anchorDate.atTime(5, 0).atZone(zoneId).toInstant().toEpochMilli() }
    val predictedSleepStartMillis = beforeSleep?.endTime
    val predictedSleepEndMillis = afterWake?.startTime
    val predictedSleepLabel = if (predictedSleepStartMillis != null && predictedSleepEndMillis != null && predictedSleepEndMillis > predictedSleepStartMillis) {
        "${formatClockTime(predictedSleepStartMillis, zoneId)} - ${formatClockTime(predictedSleepEndMillis, zoneId)}"
    } else {
        beforeSleep?.endTime?.let { AppText.t("stats_after_value", formatClockTime(it, zoneId)) } ?: AppText.t("stats_not_enough_samples")
    }
    val predictedSleepDurationLabel = if (predictedSleepStartMillis != null && predictedSleepEndMillis != null && predictedSleepEndMillis > predictedSleepStartMillis) {
        formatDuration(predictedSleepEndMillis - predictedSleepStartMillis)
    } else {
        "--"
    }

    return UsageBehaviorInsight(
        peakHourLabel = peakHour?.label ?: "--",
        peakHourMillis = peakHour?.deviceMillis ?: 0L,
        peakTwoHourLabel = peakTwoHour?.first?.let { "${it.first().label}-${dayHourLabel(it.last().hour + 1)}" } ?: "--",
        peakTwoHourMillis = peakTwoHour?.second ?: 0L,
        nightUsageMillis = timelineBuckets.filter { it.hour < 6 || it.hour >= 22 }.sumOf { it.deviceMillis },
        longestSession = longestSession,
        averageSessionMillis = averageSessionMillis,
        activeHourCount = timelineBuckets.count { it.deviceMillis > 0L },
        shortSessionRatio = shortSessionRatio,
        reopenIntensity = reopenIntensity,
        predictedSleepLabel = predictedSleepLabel,
        predictedSleepDurationLabel = predictedSleepDurationLabel,
        beforeSleep = BehaviorAppMoment(
            label = AppText.t("stats_last_used_before_bed"),
            packageName = beforeSleep?.packageName,
            appLabel = beforeSleep?.packageName?.let { resolveAppLabel(context, it, installedAppMap) },
        ),
        afterWake = BehaviorAppMoment(
            label = AppText.t("stats_first_opened_after_waking"),
            packageName = afterWake?.packageName,
            appLabel = afterWake?.packageName?.let { resolveAppLabel(context, it, installedAppMap) },
        ),
    )
}

internal fun buildDailyReportSummary(
    date: LocalDate,
    zoneId: ZoneId,
    nowMillis: Long,
    deviceOverview: ScopeOverview,
    periodUsage: List<PeriodUsageStat>,
    yesterdayMetrics: WindowMetrics,
    averageMetrics: WindowMetrics,
): DailyReportSummary {
    val intensity = when {
        averageMetrics.deviceUsageMillis > 0L &&
            deviceOverview.totalUsageMillis > averageMetrics.deviceUsageMillis * 1.15f -> AppText.t("stats_phone_use_heavy_today")
        averageMetrics.deviceUsageMillis > 0L &&
            deviceOverview.totalUsageMillis < averageMetrics.deviceUsageMillis * 0.85f -> AppText.t("stats_phone_use_was_lighter_today")
        else -> AppText.t("stats_phone_use_near_usual_today")
    }
    val dominantPeriod = periodUsage.maxByOrNull { it.deviceMillis }?.label ?: AppText.t("stats_all_day")
    val intensityTag = when {
        averageMetrics.deviceUsageMillis > 0L &&
            deviceOverview.totalUsageMillis > averageMetrics.deviceUsageMillis * 1.15f -> AppText.t("stats_heavy_use")
        averageMetrics.deviceUsageMillis > 0L &&
            deviceOverview.totalUsageMillis < averageMetrics.deviceUsageMillis * 0.85f -> AppText.t("stats_usage_restrained")
        else -> AppText.t("stats_near_normal")
    }
    val periodTag = AppText.t("stats_concentrated_period_tag", dominantPeriod)
    val openTag = when {
        yesterdayMetrics.deviceOpenCount > 0 &&
            deviceOverview.openCount > yesterdayMetrics.deviceOpenCount * 1.15f -> AppText.t("stats_label_3")
        deviceOverview.openCount == 0 -> AppText.t("stats_no_records")
        else -> AppText.t("stats_launch_rhythm_normal")
    }
    val formattedDate = date.format(DateTimeFormatter.ofPattern(AppText.t("home_mmm_d_eeee"), Locale.CHINA))
    val capturedAt = java.time.Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA))
    return DailyReportSummary(
        title = AppText.t("stats_today_report"),
        subtitle = formattedDate,
        capturedAt = AppText.t("stats_peak_value", capturedAt),
        message = AppText.t("stats_value_mainly_concentrated_in_value_value", intensity, dominantPeriod, openTag),
        primaryValue = formatDuration(deviceOverview.totalUsageMillis),
        secondaryValue = deltaDescription(deviceOverview.totalUsageMillis, yesterdayMetrics.deviceUsageMillis, AppText.t("stats_vs_yesterday")),
        tertiaryValue = deltaDescription(deviceOverview.totalUsageMillis, averageMetrics.deviceUsageMillis, AppText.t("stats_vs_last_7_days")),
        tags = listOf(intensityTag, periodTag, openTag),
    )
}

internal fun buildComparisonMetrics(
    deviceOverview: ScopeOverview,
    behaviorInsight: UsageBehaviorInsight,
    longestSessionMillis: Long,
    yesterdayMetrics: WindowMetrics,
    averageMetrics: WindowMetrics,
): List<ComparisonMetric> {
    return listOf(
        ComparisonMetric(
            label = AppText.t("stats_total_device_duration"),
            todayValue = formatDuration(deviceOverview.totalUsageMillis),
            yesterdayDelta = deltaDescription(deviceOverview.totalUsageMillis, yesterdayMetrics.deviceUsageMillis, AppText.t("stats_vs_yesterday")),
            averageDelta = deltaDescription(deviceOverview.totalUsageMillis, averageMetrics.deviceUsageMillis, AppText.t("stats_vs_average")),
        ),
        ComparisonMetric(
            label = AppText.t("stats_launches"),
            todayValue = AppText.t("stats_value_times_11", deviceOverview.openCount),
            yesterdayDelta = deltaDescription(deviceOverview.openCount.toLong(), yesterdayMetrics.deviceOpenCount.toLong(), AppText.t("stats_vs_yesterday"), countUnit = AppText.t("stats_times")),
            averageDelta = deltaDescription(deviceOverview.openCount.toLong(), averageMetrics.deviceOpenCount.toLong(), AppText.t("stats_vs_average"), countUnit = AppText.t("stats_times")),
        ),
        ComparisonMetric(
            label = AppText.t("stats_night_use"),
            todayValue = formatDuration(behaviorInsight.nightUsageMillis),
            yesterdayDelta = deltaDescription(behaviorInsight.nightUsageMillis, yesterdayMetrics.nightUsageMillis, AppText.t("stats_vs_yesterday")),
            averageDelta = deltaDescription(behaviorInsight.nightUsageMillis, averageMetrics.nightUsageMillis, AppText.t("stats_vs_average")),
        ),
        ComparisonMetric(
            label = AppText.t("stats_label_11"),
            todayValue = formatDuration(longestSessionMillis),
            yesterdayDelta = deltaDescription(longestSessionMillis, yesterdayMetrics.longestSessionMillis, AppText.t("stats_vs_yesterday")),
            averageDelta = deltaDescription(longestSessionMillis, averageMetrics.longestSessionMillis, AppText.t("stats_vs_average")),
        ),
    )
}

internal fun buildTimelineBuckets(
    startMillis: Long,
    endMillis: Long,
    deviceSessions: List<AppSession>,
): List<DailyTimelineBucket> {
    return (0 until 24).map { hour ->
        val bucketStart = startMillis + hour * 60L * 60_000L
        val bucketEnd = minOf(bucketStart + 60L * 60_000L, endMillis)
        DailyTimelineBucket(
            hour = hour,
            label = dayHourLabel(hour),
            deviceMillis = bucketDuration(deviceSessions, bucketStart, bucketEnd),
        )
    }
}

internal fun buildPeriodUsageStats(
    timelineBuckets: List<DailyTimelineBucket>,
): List<PeriodUsageStat> {
    val groups = listOf(
        AppText.t("stats_late_night") to 0..5,
        AppText.t("stats_morning") to 6..11,
        AppText.t("stats_afternoon") to 12..17,
        AppText.t("stats_label_6") to 18..23,
    )
    return groups.map { (label, range) ->
        val buckets = timelineBuckets.filter { it.hour in range }
        PeriodUsageStat(
            label = label,
            deviceMillis = buckets.sumOf { it.deviceMillis },
        )
    }
}

internal fun buildTimelineSectionData(
    timelineBuckets: List<DailyTimelineBucket>,
): TimelineSectionData {
    val peakHour = timelineBuckets.maxByOrNull { it.deviceMillis }
    val peakTwoHour = timelineBuckets.windowed(size = 2, step = 1, partialWindows = false)
        .map { buckets -> buckets to buckets.sumOf { it.deviceMillis } }
        .maxByOrNull { it.second }
    return TimelineSectionData(
        buckets = timelineBuckets,
        periodUsage = buildPeriodUsageStats(timelineBuckets),
        peakHourLabel = peakHour?.label ?: "--",
        peakHourMillis = peakHour?.deviceMillis ?: 0L,
        peakTwoHourLabel = peakTwoHour?.first?.let { AppText.t("stats_hour_range_format", it.first().label, it.last().hour + 1) } ?: "--",
        peakTwoHourMillis = peakTwoHour?.second ?: 0L,
        nightUsageMillis = timelineBuckets.filter { it.hour < 6 || it.hour >= 22 }.sumOf { it.deviceMillis },
    )
}

internal fun bucketDuration(
    sessions: List<AppSession>,
    bucketStart: Long,
    bucketEnd: Long,
): Long {
    if (bucketEnd <= bucketStart) return 0L
    return sessions.sumOf { session ->
        val overlapStart = maxOf(bucketStart, session.startTime)
        val overlapEnd = minOf(bucketEnd, session.endTime)
        maxOf(0L, overlapEnd - overlapStart)
    }
}

internal fun selectDevicePackages(
    context: Context,
    installedAppMap: Map<String, ManagedApp>,
    managedPackages: Set<String>,
    packages: Iterable<String>,
): Map<String, AppIdentity> {
    val packageManager = context.packageManager
    return packages.asSequence()
        .filter { it.isNotBlank() }
        .distinct()
        .mapNotNull { packageName ->
            val managed = packageName in managedPackages
            val installed = installedAppMap[packageName]
            val label = installed?.appName ?: resolveAppLabel(context, packageName, installedAppMap)
            val isLaunchable = installed?.isLaunchable
                ?: (packageManager.getLaunchIntentForPackage(packageName) != null)
            if (!managed && !isLaunchable) return@mapNotNull null
            if (shouldExcludePackage(context, packageName, label, managed)) return@mapNotNull null
            AppIdentity(
                packageName = packageName,
                label = label,
                isLaunchable = isLaunchable,
            )
        }
        .associateBy { it.packageName }
}

internal fun shouldExcludePackage(
    context: Context,
    packageName: String,
    label: String,
    isManaged: Boolean,
): Boolean {
    if (isManaged) return false
    if (packageName == context.packageName) return true

    val packageLower = packageName.lowercase(Locale.ROOT)
    val labelLower = label.lowercase(Locale.ROOT)
    val exactNoise = setOf(
        "com.android.systemui",
        "com.google.android.packageinstaller",
        "com.android.permissioncontroller",
        "com.android.inputmethod.latin",
        "com.google.android.inputmethod.latin",
        "com.miui.home",
        "com.android.launcher",
        "com.android.launcher3",
    )
    if (packageLower in exactNoise) return true

    val noiseKeywords = listOf(
        "launcher",
        "systemui",
        "packageinstaller",
        "permissioncontroller",
        "inputmethod",
        "ime",
        "documentsui",
    )
    return noiseKeywords.any { keyword ->
        packageLower.contains(keyword) || labelLower.contains(keyword)
    }
}

internal fun resolveAppLabel(
    context: Context,
    packageName: String,
    installedAppMap: Map<String, ManagedApp>,
): String {
    installedAppMap[packageName]?.appName?.let { return it }
    return try {
        context.packageManager.getApplicationLabel(
            context.packageManager.getApplicationInfo(packageName, 0),
        ).toString()
    } catch (_: Exception) {
        packageName
    }
}

