package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.i18n.AppText
import java.time.LocalDate
import java.time.YearMonth

internal enum class ReportTab(private val labelKey: String) {
    DAY("stats_tab_daily"),
    WEEK("stats_tab_weekly"),
    MONTH("stats_tab_monthly"),
    YEAR("stats_tab_yearly");

    fun label(): String = AppText.t(labelKey)
}

internal data class InstalledAppsState(
    val apps: List<ManagedApp> = emptyList(),
    val isLoading: Boolean = false,
)

internal data class AppIdentity(
    val packageName: String,
    val label: String,
    val isLaunchable: Boolean,
)

internal data class DailyTimelineBucket(
    val hour: Int,
    val label: String,
    val deviceMillis: Long,
)

internal data class DailyReportSummary(
    val title: String,
    val subtitle: String,
    val capturedAt: String,
    val message: String,
    val primaryValue: String,
    val secondaryValue: String,
    val tertiaryValue: String,
    val tags: List<String>,
)

internal data class ScopeOverview(
    val totalUsageMillis: Long,
    val openCount: Int,
    val activeBucketCount: Int,
    val topApp: AppDisplayItem?,
)

internal data class AppDisplayItem(
    val packageName: String,
    val label: String,
    val value: Long,
)

internal data class PeriodUsageStat(
    val label: String,
    val deviceMillis: Long,
)

internal data class BehaviorAppMoment(
    val label: String,
    val packageName: String? = null,
    val appLabel: String? = null,
)

internal data class UsageBehaviorInsight(
    val peakHourLabel: String,
    val peakHourMillis: Long,
    val peakTwoHourLabel: String,
    val peakTwoHourMillis: Long,
    val nightUsageMillis: Long,
    val longestSession: AppDisplayItem?,
    val averageSessionMillis: Long,
    val activeHourCount: Int,
    val shortSessionRatio: Float,
    val reopenIntensity: Float,
    val predictedSleepLabel: String,
    val predictedSleepDurationLabel: String,
    val beforeSleep: BehaviorAppMoment,
    val afterWake: BehaviorAppMoment,
)

internal data class ComparisonMetric(
    val label: String,
    val todayValue: String,
    val yesterdayDelta: String?,
    val averageDelta: String?,
    val chartData: ComparisonChartData? = null,
)

internal data class ComparisonChartData(
    val currentValue: Long,
    val previousValue: Long?,
    val averageValue: Long?,
    val currentLabel: String,
    val previousLabel: String? = null,
    val averageLabel: String? = null,
)

internal data class WindowMetrics(
    val deviceUsageMillis: Long,
    val deviceOpenCount: Int,
    val longestSessionMillis: Long,
    val nightUsageMillis: Long,
)

internal data class HeroSectionData(
    val summary: DailyReportSummary,
    val overview: ScopeOverview,
    val nightUsageMillis: Long,
    val dailyGoalMillis: Long = 0L,
    val goalCompletionProgress: Float? = null,
)

internal data class TimelineSectionData(
    val buckets: List<DailyTimelineBucket>,
    val periodUsage: List<PeriodUsageStat>,
    val peakHourLabel: String,
    val peakHourMillis: Long,
    val peakTwoHourLabel: String,
    val peakTwoHourMillis: Long,
    val nightUsageMillis: Long,
    val targetMillisPerBucket: Long? = null,
)

internal data class TopAppsSectionData(
    val usageTopApps: List<AppDisplayItem>,
)

internal data class BehaviorSectionData(
    val behaviorInsight: UsageBehaviorInsight? = null,
    val structure: DailyBehaviorStructureData? = null,
)

internal data class DailyBehaviorStructureData(
    val metrics: List<DailyBehaviorStructureMetric>,
)

internal data class DailyBehaviorStructureMetric(
    val label: String,
    val value: String,
    val visualRatio: Float,
)

internal data class ComparisonSectionData(
    val comparisons: List<ComparisonMetric>,
)

internal data class DailyFocusSectionData(
    val control: DailyModeSummary,
    val encourage: DailyModeSummary,
)

internal data class DailyModeSummary(
    val title: String,
    val description: String,
    val primaryLabel: String,
    val primaryValue: String,
    val metrics: List<DailyFocusMetric>,
    val progress: Float,
    val spotlightLabel: String,
    val spotlightValue: String,
    val isWarning: Boolean = false,
)

internal data class DailyFocusMetric(
    val label: String,
    val value: String,
)

internal data class WindowFocusSectionData(
    val control: DailyModeSummary,
    val encourage: DailyModeSummary,
    val highlights: List<DailyFocusMetric>,
)

internal data class HeatmapSectionData(
    val title: String,
    val subtitle: String,
    val days: List<HeatmapDayData>,
)

internal data class HeatmapDayData(
    val label: String,
    val valueMillis: Long,
    val exceeded: Boolean,
    val selected: Boolean,
)

internal data class YearDualScopeSectionData(
    val naturalYear: YearScopeSummary,
    val rollingYear: YearScopeSummary,
)

internal data class YearScopeSummary(
    val title: String,
    val rangeLabel: String,
    val totalUsage: String,
    val averageUsage: String,
    val activeDays: String,
    val savedUsage: String,
    val pointsNet: String,
)

internal data class PeriodHeroData(
    val eyebrow: String,
    val title: String,
    val rangeLabel: String,
    val primaryValue: String,
    val message: String,
    val comparisonValue: String,
    val averageLabel: String,
    val tertiaryValue: String,
    val tags: List<String>,
    val metrics: List<DailyFocusMetric>,
)

internal data class TrendPoint(
    val label: String,
    val totalUsageMillis: Long,
    val secondaryValue: Long = 0L,
    val tertiaryValue: Long = 0L,
)

internal data class TrendSectionData(
    val title: String,
    val subtitle: String,
    val primaryLabel: String,
    val secondaryLabel: String?,
    val tertiaryLabel: String? = null,
    val points: List<TrendPoint>,
    val summary: List<DailyFocusMetric>,
)

internal enum class PeriodTone {
    PRIMARY,
    POSITIVE,
    WARNING,
    SECONDARY,
    NEUTRAL,
}

internal data class ScatterPointData(
    val label: String,
    val xValue: Float,
    val yValue: Float,
    val sizeValue: Float,
    val xDisplay: String,
    val yDisplay: String,
    val detail: String,
    val tone: PeriodTone,
)

internal data class ScatterSectionData(
    val title: String,
    val subtitle: String,
    val xLabel: String,
    val yLabel: String,
    val sizeLabel: String?,
    val points: List<ScatterPointData>,
    val summary: List<DailyFocusMetric>,
)

internal data class PeriodHeatmapData(
    val title: String,
    val subtitle: String,
    val columns: Int,
    val cells: List<HeatmapDayData>,
    val showLabels: Boolean = true,
)

internal data class AppFocusInsight(
    val title: String,
    val value: String,
    val detail: String,
)

internal data class WeeklyTopAppsRow(
    val dayCode: String,
    val packages: List<String>,
)

internal data class AppFocusSectionData(
    val title: String,
    val subtitle: String,
    val totalUsageLabel: String,
    val topApps: List<AppDisplayItem>,
    val insights: List<AppFocusInsight>,
    val weeklyTopAppRows: List<WeeklyTopAppsRow> = emptyList(),
)

internal data class MonthlyWeekSummary(
    val label: String,
    val totalUsageMillis: Long,
    val averageUsageMillis: Long,
    val peakDayLabel: String,
)

internal data class MonthlyWeekStructureData(
    val title: String,
    val subtitle: String,
    val weeks: List<MonthlyWeekSummary>,
)

internal data class YearQuarterSummary(
    val label: String,
    val totalUsageMillis: Long,
    val bestMonthLabel: String,
    val bestMonthUsageMillis: Long,
    val topAppLabel: String,
)

internal data class YearQuarterSectionData(
    val title: String,
    val subtitle: String,
    val quarters: List<YearQuarterSummary>,
)

internal data class PeriodReportData(
    val tab: ReportTab,
    val hero: PeriodHeroData,
    val trend: TrendSectionData,
    val heatmap: PeriodHeatmapData? = null,
    val appFocus: AppFocusSectionData,
    val windowFocus: WindowFocusSectionData,
    val behavior: BehaviorSectionData?,
    val comparison: ComparisonSectionData?,
    val monthStructure: MonthlyWeekStructureData? = null,
    val quarterSection: YearQuarterSectionData? = null,
)

internal data class PeriodBounds(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val previousStartDate: LocalDate,
    val previousEndDate: LocalDate,
)

internal data class PeriodDaySummary(
    val date: LocalDate,
    val usageMillis: Long,
    val controlUsageMillis: Long,
    val encourageUsageMillis: Long,
    val savedMillis: Long,
    val pointsNet: Double,
    val openCount: Int,
    val nightUsageMillis: Long,
    val longestSessionMillis: Long,
    val exceeded: Boolean,
    val blockCount: Int,
)

internal data class PeriodMonthSummary(
    val month: Int,
    val label: String,
    val usageMillis: Long,
    val savedMillis: Long,
    val pointsNet: Double,
    val activeDays: Int,
    val exceededMonths: Boolean,
    val topAppLabel: String,
)

internal data class ShareReportData(
    val tab: ReportTab,
    val title: String,
    val subtitle: String,
    val slogan: String,
    val statusTitle: String,
    val primaryValue: String,
    val primaryLabel: String,
    val metrics: List<DailyFocusMetric>,
    val insight: String,
    val topApps: List<AppDisplayItem>,
    val totalUsageMillis: Long,
    val goalMillis: Long,
    val goalProgress: Float?,
    val savedMillis: Long,
    val pointsNet: Double,
    val blockEventCount: Int,
    val redemptionCount: Int,
    val controlCompletedGroupCount: Int,
    val controlExceededGroupCount: Int,
    val encourageCompletedGroupCount: Int,
    val encourageUsageMillis: Long,
    val nightUsageMillis: Long,
    val hourlyUsageMillis: List<Long>,
    val timelineLabels: List<String>,
    val trendUsageMillis: List<Long>,
    val targetMillisPerBucket: Long?,
    val comparisonLabel: String,
    val dominantPeriod: String,
)

internal data class ArchivedAppSnapshot(
    val archiveDate: String,
    val packageName: String,
    val label: String,
    val usageMillis: Long,
    val openCount: Int,
    val sessionCount: Int,
    val longestSessionMillis: Long,
    val nightUsageMillis: Long,
    val hourlyBuckets: LongArray,
)

internal sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data object Empty : SectionState<Nothing>
    data class Ready<T>(val data: T) : SectionState<T>
}

internal data class DailyReportUiState(
    val isPermissionGranted: Boolean = false,
    val selectedTab: ReportTab = ReportTab.DAY,
    val isRefreshing: Boolean = true,
    val heroState: SectionState<HeroSectionData> = SectionState.Loading,
    val dailyFocusState: SectionState<DailyFocusSectionData> = SectionState.Loading,
    val windowFocusState: SectionState<WindowFocusSectionData> = SectionState.Loading,
    val heatmapState: SectionState<HeatmapSectionData> = SectionState.Loading,
    val yearDualScopeState: SectionState<YearDualScopeSectionData> = SectionState.Loading,
    val shareState: SectionState<ShareReportData> = SectionState.Loading,
    val timelineState: SectionState<TimelineSectionData> = SectionState.Loading,
    val topAppsState: SectionState<TopAppsSectionData> = SectionState.Loading,
    val behaviorState: SectionState<BehaviorSectionData> = SectionState.Loading,
    val comparisonState: SectionState<ComparisonSectionData> = SectionState.Loading,
    val periodReportState: SectionState<PeriodReportData> = SectionState.Empty,
    val selectedArchiveDate: String? = null,
    val previousArchiveDate: String? = null,
    val nextArchiveDate: String? = null,
    val availableArchiveDates: List<String> = emptyList(),
    val selectedWeekStart: LocalDate? = null,
    val previousWeekStart: LocalDate? = null,
    val nextWeekStart: LocalDate? = null,
    val availableWeekStarts: List<LocalDate> = emptyList(),
    val selectedMonth: YearMonth? = null,
    val previousMonth: YearMonth? = null,
    val nextMonth: YearMonth? = null,
    val availableMonths: List<YearMonth> = emptyList(),
    val selectedYear: Int? = null,
    val previousYear: Int? = null,
    val nextYear: Int? = null,
    val availableYears: List<Int> = emptyList(),
    val placeholderTitle: String? = null,
    val placeholderDescription: String? = null,
)
