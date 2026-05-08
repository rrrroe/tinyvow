package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import android.content.Context
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.usage.AppSession
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.data.usage.UsageRepository
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.ui.theme.LocalReportColors
import com.rrrrz.tinyvow.ui.theme.ReportColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private enum class ReportTab(private val labelKey: String) {
    DAY("stats_tab_daily"),
    WEEK("stats_tab_weekly"),
    MONTH("stats_tab_monthly"),
    YEAR("stats_tab_yearly");

    fun label(): String = AppText.t(labelKey)
}

private data class InstalledAppsState(
    val apps: List<ManagedApp> = emptyList(),
    val isLoading: Boolean = false,
)

private data class AppIdentity(
    val packageName: String,
    val label: String,
    val isLaunchable: Boolean,
)

private data class DailyTimelineBucket(
    val hour: Int,
    val label: String,
    val deviceMillis: Long,
)

private data class DailyReportSummary(
    val title: String,
    val subtitle: String,
    val capturedAt: String,
    val message: String,
    val primaryValue: String,
    val secondaryValue: String,
    val tertiaryValue: String,
    val tags: List<String>,
)

private data class ScopeOverview(
    val totalUsageMillis: Long,
    val openCount: Int,
    val activeBucketCount: Int,
    val topApp: AppDisplayItem?,
)

private data class AppDisplayItem(
    val packageName: String,
    val label: String,
    val value: Long,
)

private data class PeriodUsageStat(
    val label: String,
    val deviceMillis: Long,
)

private data class BehaviorAppMoment(
    val label: String,
    val packageName: String? = null,
    val appLabel: String? = null,
)

private data class UsageBehaviorInsight(
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

private data class ComparisonMetric(
    val label: String,
    val todayValue: String,
    val yesterdayDelta: String?,
    val averageDelta: String?,
    val chartData: ComparisonChartData? = null,
)

private data class ComparisonChartData(
    val currentValue: Long,
    val previousValue: Long?,
    val averageValue: Long?,
    val currentLabel: String,
    val previousLabel: String? = null,
    val averageLabel: String? = null,
)

private data class WindowMetrics(
    val deviceUsageMillis: Long,
    val deviceOpenCount: Int,
    val longestSessionMillis: Long,
    val nightUsageMillis: Long,
)

private data class HeroSectionData(
    val summary: DailyReportSummary,
    val overview: ScopeOverview,
    val nightUsageMillis: Long,
    val dailyGoalMillis: Long = 0L,
    val goalCompletionProgress: Float? = null,
)

private data class TimelineSectionData(
    val buckets: List<DailyTimelineBucket>,
    val periodUsage: List<PeriodUsageStat>,
    val peakHourLabel: String,
    val peakHourMillis: Long,
    val peakTwoHourLabel: String,
    val peakTwoHourMillis: Long,
    val nightUsageMillis: Long,
    val targetMillisPerBucket: Long? = null,
)

private data class TopAppsSectionData(
    val usageTopApps: List<AppDisplayItem>,
)

private data class BehaviorSectionData(
    val behaviorInsight: UsageBehaviorInsight?,
)

private data class ComparisonSectionData(
    val comparisons: List<ComparisonMetric>,
)

private data class DailyFocusSectionData(
    val control: DailyModeSummary,
    val encourage: DailyModeSummary,
)

private data class DailyModeSummary(
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

private data class DailyFocusMetric(
    val label: String,
    val value: String,
)

private data class WindowFocusSectionData(
    val control: DailyModeSummary,
    val encourage: DailyModeSummary,
    val highlights: List<DailyFocusMetric>,
)

private data class HeatmapSectionData(
    val title: String,
    val subtitle: String,
    val days: List<HeatmapDayData>,
)

private data class HeatmapDayData(
    val label: String,
    val valueMillis: Long,
    val exceeded: Boolean,
    val selected: Boolean,
)

private data class YearDualScopeSectionData(
    val naturalYear: YearScopeSummary,
    val rollingYear: YearScopeSummary,
)

private data class YearScopeSummary(
    val title: String,
    val rangeLabel: String,
    val totalUsage: String,
    val averageUsage: String,
    val activeDays: String,
    val savedUsage: String,
    val pointsNet: String,
)

private data class PeriodHeroData(
    val eyebrow: String,
    val title: String,
    val rangeLabel: String,
    val primaryValue: String,
    val message: String,
    val comparisonValue: String,
    val tertiaryValue: String,
    val tags: List<String>,
    val metrics: List<DailyFocusMetric>,
)

private data class TrendPoint(
    val label: String,
    val totalUsageMillis: Long,
    val secondaryValue: Long = 0L,
    val tertiaryValue: Long = 0L,
)

private data class TrendSectionData(
    val title: String,
    val subtitle: String,
    val primaryLabel: String,
    val secondaryLabel: String?,
    val tertiaryLabel: String? = null,
    val points: List<TrendPoint>,
    val summary: List<DailyFocusMetric>,
)

private enum class PeriodTone {
    PRIMARY,
    POSITIVE,
    WARNING,
    SECONDARY,
    NEUTRAL,
}

private data class ScatterPointData(
    val label: String,
    val xValue: Float,
    val yValue: Float,
    val sizeValue: Float,
    val xDisplay: String,
    val yDisplay: String,
    val detail: String,
    val tone: PeriodTone,
)

private data class ScatterSectionData(
    val title: String,
    val subtitle: String,
    val xLabel: String,
    val yLabel: String,
    val sizeLabel: String?,
    val points: List<ScatterPointData>,
    val summary: List<DailyFocusMetric>,
)

private data class PeriodHeatmapData(
    val title: String,
    val subtitle: String,
    val columns: Int,
    val cells: List<HeatmapDayData>,
    val showLabels: Boolean = true,
)

private data class AppFocusInsight(
    val title: String,
    val value: String,
    val detail: String,
)

private data class AppFocusSectionData(
    val title: String,
    val subtitle: String,
    val totalUsageLabel: String,
    val topApps: List<AppDisplayItem>,
    val insights: List<AppFocusInsight>,
)

private data class MonthlyWeekSummary(
    val label: String,
    val totalUsageMillis: Long,
    val averageUsageMillis: Long,
    val peakDayLabel: String,
)

private data class MonthlyWeekStructureData(
    val title: String,
    val subtitle: String,
    val weeks: List<MonthlyWeekSummary>,
)

private data class YearQuarterSummary(
    val label: String,
    val totalUsageMillis: Long,
    val bestMonthLabel: String,
    val bestMonthUsageMillis: Long,
    val topAppLabel: String,
)

private data class YearQuarterSectionData(
    val title: String,
    val subtitle: String,
    val quarters: List<YearQuarterSummary>,
)

private data class PeriodReportData(
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

private data class PeriodBounds(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val previousStartDate: LocalDate,
    val previousEndDate: LocalDate,
)

private data class PeriodDaySummary(
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

private data class PeriodMonthSummary(
    val month: Int,
    val label: String,
    val usageMillis: Long,
    val savedMillis: Long,
    val pointsNet: Double,
    val activeDays: Int,
    val exceededMonths: Boolean,
    val topAppLabel: String,
)

private data class ShareReportData(
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

private data class ArchivedAppSnapshot(
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

private sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data object Empty : SectionState<Nothing>
    data class Ready<T>(val data: T) : SectionState<T>
}

private data class DailyReportUiState(
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

@Composable
fun StatsRoute(
    usageAccessStatus: UsageAccessStatus,
    groupsWithApps: List<AppGroupWithApps>,
    userPoints: Double,
    todayPoints: Double,
    archiveRepository: DailyArchiveRepository,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onRequestUsageAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zoneId = remember { ZoneId.systemDefault() }
    var selectedTab by remember { mutableStateOf(ReportTab.DAY) }
    var selectedArchiveDate by remember { mutableStateOf<String?>(null) }
    var selectedWeekStart by remember { mutableStateOf<LocalDate?>(null) }
    var selectedMonth by remember { mutableStateOf<YearMonth?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var uiState by remember { mutableStateOf(DailyReportUiState(selectedTab = selectedTab)) }

    LaunchedEffect(
        usageAccessStatus,
        groupsWithApps,
        selectedTab,
        selectedArchiveDate,
        selectedWeekStart,
        selectedMonth,
        selectedYear,
        userPoints,
        todayPoints,
    ) {
        if (usageAccessStatus != UsageAccessStatus.GRANTED) {
            uiState = DailyReportUiState(
                isPermissionGranted = false,
                selectedTab = selectedTab,
                isRefreshing = false,
                selectedArchiveDate = selectedArchiveDate,
                selectedWeekStart = selectedWeekStart,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
            )
            return@LaunchedEffect
        }

        val recentArchives =
            archiveRepository
                .getRecentArchives(limit = 3650)
                .first()
                .sortedByDescending { it.archiveDate }

        when (selectedTab) {
            ReportTab.DAY -> {
                uiState =
                    createRefreshingUiState(
                        selectedTab = selectedTab,
                        previous = uiState,
                        selectedArchiveDate = selectedArchiveDate,
                        selectedWeekStart = selectedWeekStart,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                    )
                val normalizedSelectedDate =
                    when {
                        recentArchives.isEmpty() -> null
                        selectedArchiveDate != null &&
                            recentArchives.any { it.archiveDate == selectedArchiveDate } -> selectedArchiveDate
                        else -> recentArchives.first().archiveDate
                    }
                if (normalizedSelectedDate != selectedArchiveDate) {
                    selectedArchiveDate = normalizedSelectedDate
                }
                buildArchivedDayReportUiState(
                    selectedDate = normalizedSelectedDate,
                    recentArchives = recentArchives,
                    archiveRepository = archiveRepository,
                    updateState = { transform -> uiState = transform(uiState) },
                )
                return@LaunchedEffect
            }
            ReportTab.WEEK, ReportTab.MONTH, ReportTab.YEAR -> {
                uiState =
                    createRefreshingUiState(
                        selectedTab = selectedTab,
                        previous = uiState,
                        selectedArchiveDate = selectedArchiveDate,
                        selectedWeekStart = selectedWeekStart,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                    )
                val availableWeekStarts = buildAvailableWeekStarts(recentArchives)
                val availableMonths = buildAvailableMonths(recentArchives)
                val availableYears = buildAvailableYears(recentArchives)
                val normalizedWeekStart =
                    selectedWeekStart
                        ?.takeIf { it in availableWeekStarts }
                        ?: availableWeekStarts.firstOrNull()
                val normalizedMonth =
                    selectedMonth
                        ?.takeIf { it in availableMonths }
                        ?: availableMonths.firstOrNull()
                val normalizedYear =
                    selectedYear
                        ?.takeIf { it in availableYears }
                        ?: availableYears.firstOrNull()
                if (selectedTab == ReportTab.WEEK && normalizedWeekStart != selectedWeekStart) {
                    selectedWeekStart = normalizedWeekStart
                }
                if (selectedTab == ReportTab.MONTH && normalizedMonth != selectedMonth) {
                    selectedMonth = normalizedMonth
                }
                if (selectedTab == ReportTab.YEAR && normalizedYear != selectedYear) {
                    selectedYear = normalizedYear
                }
                buildArchivedWindowReportUiState(
                    selectedTab = selectedTab,
                    zoneId = zoneId,
                    archiveRepository = archiveRepository,
                    recentArchives = recentArchives,
                    selectedWeekStart = normalizedWeekStart,
                    selectedMonth = normalizedMonth,
                    selectedYear = normalizedYear,
                    updateState = { transform -> uiState = transform(uiState) },
                )
                return@LaunchedEffect
            }
        }
    }

    StatsScreenLayout(
        state = uiState,
        onTabSelected = { selectedTab = it },
        onPreviousArchiveDate = {
            uiState.previousArchiveDate?.let { previousDate ->
                selectedArchiveDate = previousDate
            }
        },
        onNextArchiveDate = {
            uiState.nextArchiveDate?.let { nextDate ->
                selectedArchiveDate = nextDate
            }
        },
        onSelectArchiveDate = { date ->
            selectedArchiveDate = date
        },
        onPreviousWeek = {
            uiState.previousWeekStart?.let { selectedWeekStart = it }
        },
        onNextWeek = {
            uiState.nextWeekStart?.let { selectedWeekStart = it }
        },
        onSelectWeekStart = { weekStart ->
            selectedWeekStart = weekStart
        },
        onPreviousMonth = {
            uiState.previousMonth?.let { selectedMonth = it }
        },
        onNextMonth = {
            uiState.nextMonth?.let { selectedMonth = it }
        },
        onSelectMonth = { month ->
            selectedMonth = month
        },
        onPreviousYear = {
            uiState.previousYear?.let { selectedYear = it }
        },
        onNextYear = {
            uiState.nextYear?.let { selectedYear = it }
        },
        onSelectYear = { year ->
            selectedYear = year
        },
        isProActive = isProActive,
        onShowProUpsell = onShowProUpsell,
        onRequestUsageAccess = onRequestUsageAccess,
        modifier = modifier,
    )
}

private fun createRefreshingUiState(
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

private suspend fun buildArchivedWindowReportUiState(
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
            behavior = behaviorInsight?.let { BehaviorSectionData(it) },
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

private fun buildAvailableWeekStarts(recentArchives: List<DailyArchiveEntity>): List<LocalDate> =
    recentArchives
        .map { LocalDate.parse(it.archiveDate).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)) }
        .distinct()
        .sortedDescending()

private fun buildAvailableMonths(recentArchives: List<DailyArchiveEntity>): List<YearMonth> =
    recentArchives
        .map { YearMonth.from(LocalDate.parse(it.archiveDate)) }
        .distinct()
        .sortedDescending()

private fun buildAvailableYears(recentArchives: List<DailyArchiveEntity>): List<Int> =
    recentArchives
        .map { LocalDate.parse(it.archiveDate).year }
        .distinct()
        .sortedDescending()

private fun resolvePeriodBounds(
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

private fun buildPeriodDaySummaries(
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

private fun buildPeriodHourBuckets(items: List<ArchivedAppSnapshot>): List<DailyTimelineBucket> =
    (0 until 24).map { hour ->
        DailyTimelineBucket(
            hour = hour,
            label = dayHourLabel(hour),
            deviceMillis = items.sumOf { snapshot -> snapshot.hourlyBuckets.getOrElse(hour) { 0L } },
        )
    }

private fun buildPeriodReportData(
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

private fun archiveWindowDays(tab: ReportTab): Int =
    when (tab) {
        ReportTab.DAY -> 1
        ReportTab.WEEK -> 7
        ReportTab.MONTH -> 30
        ReportTab.YEAR -> 365
    }

private fun buildWeeklyReportData(
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
                eyebrow = AppText.t("stats_weekly_report"),
                title = AppText.t("stats_weekly_battle_title"),
                rangeLabel = periodWeekLabel(bounds.startDate),
                primaryValue = formatDuration(totalUsage),
                message = AppText.t("stats_weekly_battle_message"),
                comparisonValue = comparison?.comparisons?.firstOrNull()?.yesterdayDelta ?: AppText.t("stats_not_enough_samples"),
                tertiaryValue = AppText.t("stats_daily_average_value", formatDuration(if (activeDays > 0) totalUsage / activeDays else 0L)),
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
        appFocus = buildAppFocusSectionData(ReportTab.WEEK, topApps, snapshots, totalUsage),
        windowFocus = windowFocus,
        behavior = behavior,
        comparison = comparison,
    )
}

private fun buildMonthlyReportData(
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
                eyebrow = AppText.t("stats_monthly_report"),
                title = AppText.t("stats_monthly_battle_title"),
                rangeLabel = periodRangeLabel(bounds.startDate, bounds.endDate),
                primaryValue = formatDuration(totalUsage),
                message = AppText.t("stats_monthly_battle_message"),
                comparisonValue = comparison?.comparisons?.firstOrNull()?.yesterdayDelta ?: AppText.t("stats_not_enough_samples"),
                tertiaryValue = AppText.t("stats_daily_average_value", formatDuration(if (activeDays > 0) totalUsage / activeDays else 0L)),
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

private fun buildYearlyReportData(
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
                eyebrow = AppText.t("stats_yearly_report"),
                title = AppText.t("stats_yearly_battle_title"),
                rangeLabel = periodRangeLabel(bounds.startDate, bounds.endDate),
                primaryValue = formatDuration(totalUsage),
                message = AppText.t("stats_yearly_battle_message"),
                comparisonValue = comparison?.comparisons?.firstOrNull()?.yesterdayDelta ?: AppText.t("stats_not_enough_samples"),
                tertiaryValue = AppText.t("stats_monthly_average_value", formatDuration(if (activeMonths > 0) totalUsage / activeMonths else 0L)),
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

private fun generateDateSequence(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
    if (endDate.isBefore(startDate)) return emptyList()
    return buildList {
        var cursor = startDate
        while (!cursor.isAfter(endDate)) {
            add(cursor)
            cursor = cursor.plusDays(1)
        }
    }
}

private fun periodRangeLabel(startDate: LocalDate, endDate: LocalDate): String =
    "${startDate.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault()))} - ${endDate.format(DateTimeFormatter.ofPattern("M/d", Locale.getDefault()))}"

private fun isoWeekLabel(weekStart: LocalDate): String {
    val weekYear = weekStart.get(IsoFields.WEEK_BASED_YEAR)
    val weekNumber = weekStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    return "$weekYear-W${weekNumber.toString().padStart(2, '0')}"
}

private fun periodWeekLabel(weekStart: LocalDate): String =
    "${isoWeekLabel(weekStart)} · ${periodRangeLabel(weekStart, weekStart.plusDays(6))}"

private fun buildMonthHeatmapCells(
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

private fun buildYearHeatmapCells(
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

private fun buildMonthlyWeekSummaries(
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

private fun buildYearMonthSummaries(
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

private fun buildQuarterSummaries(
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

private fun buildAppFocusSectionData(
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

private fun buildDailyFocusSectionData(
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
            ),
    )
}

private fun buildWindowFocusSectionData(
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

private data class GroupWindowSummary(
    val groupId: String,
    val groupName: String,
    val usageMillis: Long,
    val remainingMillis: Long,
    val exceededMillis: Long,
    val blockCount: Int,
    val earnedPoints: Double,
    val completedCount: Int,
)

private fun buildYearTimelineBuckets(archives: List<DailyArchiveEntity>): List<DailyTimelineBucket> {
    return (1..12).map { month ->
        val monthArchives = archives.filter { LocalDate.parse(it.archiveDate).monthValue == month }
        DailyTimelineBucket(
            hour = month - 1,
            label = AppText.t("stats_value", month),
            deviceMillis = monthArchives.sumOf { it.totalUsageMillis },
        )
    }
}

private fun buildHeatmapSectionData(
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

private fun buildYearScopeSummary(
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

private fun buildShareReportData(
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

private fun mergeArchivedAppSnapshots(items: List<DailyAppArchiveEntity>): List<ArchivedAppSnapshot> {
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

private fun buildArchivedWindowMetrics(items: List<ArchivedAppSnapshot>): WindowMetrics {
    return WindowMetrics(
        deviceUsageMillis = items.sumOf { it.usageMillis },
        deviceOpenCount = items.sumOf { it.openCount },
        longestSessionMillis = items.maxOfOrNull { it.longestSessionMillis } ?: 0L,
        nightUsageMillis = items.sumOf { it.nightUsageMillis },
    )
}

private fun buildArchiveTimelineBuckets(archives: List<DailyArchiveEntity>): List<DailyTimelineBucket> {
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

private fun buildArchivePeriodUsageStats(
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

private suspend fun buildArchivedDayReportUiState(
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
                if (behaviorInsight == null) {
                    SectionState.Empty
                } else {
                    SectionState.Ready(BehaviorSectionData(behaviorInsight = behaviorInsight))
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

private fun buildArchiveTimelineSectionData(
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

private fun buildArchivedReportSummary(
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

private fun buildArchivedComparisonMetrics(
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

private fun buildArchivedDayTimelineBuckets(items: List<ArchivedAppSnapshot>): List<DailyTimelineBucket> {
    return (0 until 24).map { hour ->
        DailyTimelineBucket(
            hour = hour,
            label = dayHourLabel(hour),
            deviceMillis = items.sumOf { snapshot -> snapshot.hourlyBuckets.getOrElse(hour) { 0L } },
        )
    }
}

private fun buildArchivedDaySummary(
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
        title = AppText.t("stats_archived_daily_reports"),
        subtitle = formatArchiveDate(archive.archiveDate, AppText.t("home_mmm_d_eeee")),
        capturedAt = AppText.t("stats_archive_date_value", formatArchiveDate(archive.archiveDate, "M/d")),
        message = AppText.t("stats_value_was_mainly_concentrated_in_value", message, dominantPeriod),
        primaryValue = formatDuration(overview.totalUsageMillis),
        secondaryValue = deltaDescription(overview.totalUsageMillis, previousMetrics.deviceUsageMillis, AppText.t("stats_vs_previous_archive")),
        tertiaryValue = if (averagePerDayUsage > 0L) AppText.t("stats_last_7_archived_day_average", formatDuration(averagePerDayUsage)) else AppText.t("stats_no_earlier_archive_average_yet"),
        tags = listOf(AppText.t("stats_net_points_value_3", formatSignedPointsLocal(archive.pointsNet)), AppText.t("stats_value_redemptions_2", archive.redemptionCount), AppText.t("stats_saved_duration_value", formatDuration(archive.savedMillis))),
    )
}

private fun buildArchivedDayBehaviorInsight(
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

private fun buildArchivedDayComparisonMetrics(
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

private suspend fun buildDailyReportUiState(
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

private fun buildPlaceholderUiState(tab: ReportTab): DailyReportUiState {
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

private suspend fun buildWindowMetrics(
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

private suspend fun buildBehaviorInsight(
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

private fun buildDailyReportSummary(
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

private fun buildComparisonMetrics(
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

private fun buildTimelineBuckets(
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

private fun buildPeriodUsageStats(
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

private fun buildTimelineSectionData(
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

private fun bucketDuration(
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

private fun selectDevicePackages(
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

private fun shouldExcludePackage(
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

private fun resolveAppLabel(
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

@Composable
private fun StatsScreenLayout(
    state: DailyReportUiState,
    onTabSelected: (ReportTab) -> Unit,
    onPreviousArchiveDate: () -> Unit,
    onNextArchiveDate: () -> Unit,
    onSelectArchiveDate: (String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onSelectYear: (Int) -> Unit,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onRequestUsageAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reportColors = LocalReportColors.current
    val background = Brush.verticalGradient(
        colors = reportColors.pageGradient,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
    ) {
        when {
            !state.isPermissionGranted -> PermissionRequiredState(
                onRequestUsageAccess = onRequestUsageAccess,
            )
            state.placeholderTitle != null -> PlaceholderReportScreen(
                state = state,
                onTabSelected = onTabSelected,
            )
            else -> DailyReportScreen(
                state = state,
                onTabSelected = onTabSelected,
                onPreviousArchiveDate = onPreviousArchiveDate,
                onNextArchiveDate = onNextArchiveDate,
                onSelectArchiveDate = onSelectArchiveDate,
                onPreviousWeek = onPreviousWeek,
                onNextWeek = onNextWeek,
                onSelectWeekStart = onSelectWeekStart,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onSelectMonth = onSelectMonth,
                onPreviousYear = onPreviousYear,
                onNextYear = onNextYear,
                onSelectYear = onSelectYear,
                isProActive = isProActive,
                onShowProUpsell = onShowProUpsell,
            )
        }
    }
}

@Composable
private fun DailyReportScreen(
    state: DailyReportUiState,
    onTabSelected: (ReportTab) -> Unit,
    onPreviousArchiveDate: () -> Unit,
    onNextArchiveDate: () -> Unit,
    onSelectArchiveDate: (String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onSelectYear: (Int) -> Unit,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
) {
    val isDayReport = state.selectedTab == ReportTab.DAY
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ReportTabRow(selectedTab = state.selectedTab, onTabSelected = onTabSelected)
                if (state.selectedTab == ReportTab.DAY && state.selectedArchiveDate != null) {
                    ArchiveDateNavigator(
                        selectedArchiveDate = state.selectedArchiveDate,
                        previousArchiveDate = state.previousArchiveDate,
                        nextArchiveDate = state.nextArchiveDate,
                        availableArchiveDates = state.availableArchiveDates,
                        onPreviousArchiveDate = onPreviousArchiveDate,
                        onNextArchiveDate = onNextArchiveDate,
                        onSelectArchiveDate = onSelectArchiveDate,
                    )
                } else {
                    PeriodNavigator(
                        selectedTab = state.selectedTab,
                        selectedWeekStart = state.selectedWeekStart,
                        previousWeekStart = state.previousWeekStart,
                        nextWeekStart = state.nextWeekStart,
                        availableWeekStarts = state.availableWeekStarts,
                        selectedMonth = state.selectedMonth,
                        previousMonth = state.previousMonth,
                        nextMonth = state.nextMonth,
                        availableMonths = state.availableMonths,
                        selectedYear = state.selectedYear,
                        previousYear = state.previousYear,
                        nextYear = state.nextYear,
                        availableYears = state.availableYears,
                        onPreviousWeek = onPreviousWeek,
                        onNextWeek = onNextWeek,
                        onSelectWeekStart = onSelectWeekStart,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        onSelectMonth = onSelectMonth,
                        onPreviousYear = onPreviousYear,
                        onNextYear = onNextYear,
                        onSelectYear = onSelectYear,
                    )
                }
                if (state.isRefreshing) {
                    LoadingHintChip(selectedTab = state.selectedTab)
                }
                if (!isProActive && !isDayReport) {
                    LockedAdvancedReportCard(onClick = { onShowProUpsell(ProUpsellSource.ADVANCED_REPORT) })
                    Spacer(modifier = Modifier.height(24.dp))
                    return@Column
                }
                if (isDayReport) {
                    DailyBattleHeroCard(heroState = state.heroState)
                    DailyFocusCard(
                        focusState = state.dailyFocusState,
                        compactLayout = true,
                    )
                    DailyRhythmCard(timelineState = state.timelineState)
                    DailyAppsAndAnalysisCard(
                        topAppsState = state.topAppsState,
                        behaviorState = state.behaviorState,
                        comparisonState = state.comparisonState,
                        shareState = state.shareState,
                        isProActive = isProActive,
                        onShowProUpsell = onShowProUpsell,
                    )
                } else {
                    PeriodReportScreen(state = state)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PeriodNavigator(
    selectedTab: ReportTab,
    selectedWeekStart: LocalDate?,
    previousWeekStart: LocalDate?,
    nextWeekStart: LocalDate?,
    availableWeekStarts: List<LocalDate>,
    selectedMonth: YearMonth?,
    previousMonth: YearMonth?,
    nextMonth: YearMonth?,
    availableMonths: List<YearMonth>,
    selectedYear: Int?,
    previousYear: Int?,
    nextYear: Int?,
    availableYears: List<Int>,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onSelectYear: (Int) -> Unit,
) {
    var showDialog by remember(selectedTab) { mutableStateOf(false) }
    val title =
        when (selectedTab) {
            ReportTab.WEEK -> selectedWeekStart?.let(::periodWeekLabel) ?: AppText.t("stats_choose_period")
            ReportTab.MONTH -> selectedMonth?.format(DateTimeFormatter.ofPattern(AppText.t("stats_mmmm_yyyy"), Locale.getDefault())) ?: AppText.t("stats_choose_period")
            ReportTab.YEAR -> selectedYear?.toString() ?: AppText.t("stats_choose_period")
            ReportTab.DAY -> ""
        }
    val subtitle =
        when (selectedTab) {
            ReportTab.WEEK -> AppText.t("stats_natural_week")
            ReportTab.MONTH -> AppText.t("stats_natural_month")
            ReportTab.YEAR -> AppText.t("stats_natural_year")
            ReportTab.DAY -> ""
        }
    val canGoPrevious =
        when (selectedTab) {
            ReportTab.WEEK -> previousWeekStart != null
            ReportTab.MONTH -> previousMonth != null
            ReportTab.YEAR -> previousYear != null
            ReportTab.DAY -> false
        }
    val canGoNext =
        when (selectedTab) {
            ReportTab.WEEK -> nextWeekStart != null
            ReportTab.MONTH -> nextMonth != null
            ReportTab.YEAR -> nextYear != null
            ReportTab.DAY -> false
        }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = {
                    when (selectedTab) {
                        ReportTab.WEEK -> onPreviousWeek()
                        ReportTab.MONTH -> onPreviousMonth()
                        ReportTab.YEAR -> onPreviousYear()
                        ReportTab.DAY -> Unit
                    }
                },
                enabled = canGoPrevious,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Surface(
                modifier = Modifier.weight(1f).clickable { showDialog = true },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            IconButton(
                onClick = {
                    when (selectedTab) {
                        ReportTab.WEEK -> onNextWeek()
                        ReportTab.MONTH -> onNextMonth()
                        ReportTab.YEAR -> onNextYear()
                        ReportTab.DAY -> Unit
                    }
                },
                enabled = canGoNext,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
    if (showDialog) {
        when (selectedTab) {
            ReportTab.WEEK ->
                PeriodWeekPickerDialog(
                    selectedWeekStart = selectedWeekStart,
                    availableWeekStarts = availableWeekStarts,
                    onDismiss = { showDialog = false },
                    onSelectWeekStart = {
                        showDialog = false
                        onSelectWeekStart(it)
                    },
                )
            ReportTab.MONTH ->
                PeriodMonthPickerDialog(
                    selectedMonth = selectedMonth,
                    availableMonths = availableMonths,
                    onDismiss = { showDialog = false },
                    onSelectMonth = {
                        showDialog = false
                        onSelectMonth(it)
                    },
                )
            ReportTab.YEAR ->
                PeriodYearPickerDialog(
                    selectedYear = selectedYear,
                    availableYears = availableYears,
                    onDismiss = { showDialog = false },
                    onSelectYear = {
                        showDialog = false
                        onSelectYear(it)
                    },
                )
            ReportTab.DAY -> Unit
        }
    }
}

@Composable
private fun PeriodWeekPickerDialog(
    selectedWeekStart: LocalDate?,
    availableWeekStarts: List<LocalDate>,
    onDismiss: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_close"))
            }
        },
        title = {
            Text(AppText.t("stats_choose_week"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableWeekStarts.forEach { weekStart ->
                    val selected = weekStart == selectedWeekStart
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectWeekStart(weekStart) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                    ) {
                        Text(
                            text = periodWeekLabel(weekStart),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun PeriodMonthPickerDialog(
    selectedMonth: YearMonth?,
    availableMonths: List<YearMonth>,
    onDismiss: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(AppText.t("group_close")) }
        },
        title = {
            Text(AppText.t("stats_choose_month"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableMonths.forEach { month ->
                    val selected = month == selectedMonth
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectMonth(month) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                    ) {
                        Text(
                            text = month.format(DateTimeFormatter.ofPattern(AppText.t("stats_mmmm_yyyy"), Locale.getDefault())),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun PeriodYearPickerDialog(
    selectedYear: Int?,
    availableYears: List<Int>,
    onDismiss: () -> Unit,
    onSelectYear: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(AppText.t("group_close")) }
        },
        title = {
            Text(AppText.t("stats_choose_year"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableYears.forEach { year ->
                    val selected = year == selectedYear
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectYear(year) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                    ) {
                        Text(
                            text = year.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun PeriodReportScreen(
    state: DailyReportUiState,
) {
    when (val periodState = state.periodReportState) {
        SectionState.Loading -> {
            repeat(4) {
                HeroSkeletonCard()
            }
        }
        SectionState.Empty -> {
            Text(
                text = AppText.t("stats_not_enough_samples"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        is SectionState.Ready -> {
            val data = periodState.data
            PeriodHeroCard(hero = data.hero)
            PeriodFocusCard(data.windowFocus)
            when (data.tab) {
                ReportTab.WEEK -> {
                    PeriodTrendCard(data.trend)
                    PeriodAppFocusCard(data.appFocus)
                    PeriodInsightSection(data = data)
                }
                ReportTab.MONTH -> {
                    data.heatmap?.let { PeriodHeatmapCard(it) }
                    PeriodTrendCard(data.trend)
                    data.monthStructure?.let { PeriodMonthStructureCard(it) }
                    PeriodAppFocusCard(data.appFocus)
                    PeriodInsightSection(data = data)
                }
                ReportTab.YEAR -> {
                    data.heatmap?.let { PeriodHeatmapCard(it) }
                    PeriodTrendCard(data.trend)
                    data.quarterSection?.let { PeriodQuarterBreakdownCard(it) }
                    PeriodAppFocusCard(data.appFocus)
                    PeriodInsightSection(data = data)
                }
                ReportTab.DAY -> Unit
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeriodFocusCard(data: WindowFocusSectionData) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AdaptiveRowGrid(
                itemCount = 2,
                compactColumns = 1,
                expandedColumns = 2,
                horizontalSpacing = 10.dp,
                verticalSpacing = 10.dp,
            ) { modifier, index ->
                DailyModeSummaryCard(
                    summary = if (index == 0) data.control else data.encourage,
                    icon = if (index == 0) Icons.Default.Bolt else Icons.Default.RocketLaunch,
                    compact = true,
                    modifier = modifier,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeriodHeroCard(hero: PeriodHeroData) {
    val reportColors = LocalReportColors.current
    ReportCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.54f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            reportColors.warning.copy(alpha = 0.12f),
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = hero.eyebrow,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = hero.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = hero.rangeLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = hero.primaryValue,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = hero.message,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                ) { modifier, index ->
                    BattleHeadlineChip(
                        label = if (index == 0) AppText.t("stats_vs_previous_window") else AppText.t("stats_daily_average"),
                        value = if (index == 0) hero.comparisonValue else hero.tertiaryValue,
                        accent = if (index == 0) reportColors.warning else reportColors.positive,
                        modifier = modifier,
                    )
                }
                AdaptiveRowGrid(
                    itemCount = hero.metrics.size,
                    compactColumns = 2,
                    expandedColumns = 4,
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                ) { modifier, index ->
                    val metric = hero.metrics[index]
                    BattleMetricTile(
                        label = metric.label,
                        value = metric.value,
                        accent = periodToneColor(
                            tone = if (index % 2 == 0) PeriodTone.PRIMARY else PeriodTone.POSITIVE,
                            primary = MaterialTheme.colorScheme.primary,
                            secondary = MaterialTheme.colorScheme.secondary,
                            muted = MaterialTheme.colorScheme.outline,
                            reportColors = reportColors,
                        ),
                        modifier = modifier,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    hero.tags.forEach { tag ->
                        SummaryTagChip(tag)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodTrendCard(data: TrendSectionData) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(icon = Icons.Default.Timeline, title = data.title, subtitle = data.subtitle)
            TrendLineChart(data = data)
            AdaptiveRowGrid(
                itemCount = data.summary.size,
                compactColumns = 1,
                expandedColumns = 3,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
            ) { modifier, index ->
                MetricTileCompact(metric = data.summary[index], modifier = modifier)
            }
        }
    }
}

@Composable
private fun TrendLineChart(
    data: TrendSectionData,
) {
    val reportColors = LocalReportColors.current
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)
    val primary = MaterialTheme.colorScheme.primary
    val secondary = reportColors.warning
    val tertiary = reportColors.positive
    val maxValue =
        max(
            data.points.maxOfOrNull { max(it.totalUsageMillis, max(it.secondaryValue, it.tertiaryValue)) } ?: 1L,
            1L,
        ).toFloat()
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp),
    ) {
        val chartWidth = size.width
        val chartHeight = size.height
        repeat(4) { index ->
            val y = chartHeight - chartHeight * (index / 3f)
            drawLine(
                color = outlineColor,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 2f,
            )
        }
        fun buildSeriesPath(values: List<Long>): Path {
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = if (values.size == 1) chartWidth / 2f else chartWidth * index / (values.lastIndex.toFloat())
                val y = chartHeight - (value.toFloat() / maxValue).coerceIn(0f, 1f) * chartHeight
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            return path
        }
        val primaryValues = data.points.map { it.totalUsageMillis }
        val primaryPath = buildSeriesPath(primaryValues)
        val fillPath = Path().apply {
            addPath(primaryPath)
            if (primaryValues.isNotEmpty()) {
                lineTo(chartWidth, chartHeight)
                lineTo(0f, chartHeight)
                close()
            }
        }
        drawPath(fillPath, color = primary.copy(alpha = 0.12f))
        drawPath(primaryPath, color = primary, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f))
        if (data.points.any { it.secondaryValue > 0L }) {
            drawPath(
                buildSeriesPath(data.points.map { it.secondaryValue }),
                color = secondary,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
            )
        }
        if (data.points.any { it.tertiaryValue > 0L }) {
            drawPath(
                buildSeriesPath(data.points.map { it.tertiaryValue }),
                color = tertiary,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        data.points.take(5).forEach { point ->
            Text(
                text = point.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PeriodHeatmapCard(data: PeriodHeatmapData) {
    val maxValue = data.cells.maxOfOrNull { it.valueMillis }?.coerceAtLeast(1L) ?: 1L
    val cellHeight = if (data.showLabels) 40.dp else 14.dp
    val rowSpacing = if (data.showLabels) 6.dp else 3.dp
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(icon = Icons.Default.CalendarMonth, title = data.title, subtitle = data.subtitle)
            data.cells.chunked(data.columns).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                ) {
                    row.forEach { cell ->
                        val intensity = if (cell.valueMillis > 0L) cell.valueMillis.toFloat() / maxValue.toFloat() else 0f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        cell.label.isBlank() -> MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                                        cell.exceeded -> LocalReportColors.current.warning.copy(alpha = 0.18f + intensity * 0.52f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f + intensity * 0.62f)
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (data.showLabels && cell.label.isNotBlank()) {
                                Text(
                                    text = cell.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (cell.selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (cell.exceeded) LocalReportColors.current.warning else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                    repeat((data.columns - row.size).coerceAtLeast(0)) {
                        Spacer(modifier = Modifier.weight(1f).height(cellHeight))
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodAppFocusCard(data: AppFocusSectionData) {
    val palette = LocalReportColors.current.appChartPalette
    val maxUsage = data.topApps.maxOfOrNull { it.value }?.coerceAtLeast(1L) ?: 1L
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(icon = Icons.Default.BarChart, title = data.title, subtitle = data.subtitle)
            Text(
                text = data.totalUsageLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            data.topApps.take(6).forEachIndexed { index, app ->
                val accent = palette.getOrElse(index) { MaterialTheme.colorScheme.primary }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AppIconCircle(app.packageName)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = formatDuration(app.value),
                                style = MaterialTheme.typography.labelLarge,
                                color = accent,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.14f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((app.value.toFloat() / maxUsage.toFloat()).coerceIn(0.05f, 1f))
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = 0.72f)),
                            )
                        }
                    }
                }
            }
            AdaptiveRowGrid(
                itemCount = data.insights.size,
                compactColumns = 1,
                expandedColumns = 2,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
            ) { modifier, index ->
                val insight = data.insights[index]
                Surface(
                    modifier = modifier,
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = insight.value,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = insight.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodInsightSection(data: PeriodReportData) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.RocketLaunch,
                title = AppText.t("stats_pattern_summary"),
                subtitle = AppText.t("stats_pattern_summary_description"),
            )
            data.behavior?.behaviorInsight?.let { insight ->
                AdaptiveRowGrid(
                    itemCount = 3,
                    compactColumns = 1,
                    expandedColumns = 3,
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                ) { modifier, index ->
                    when (index) {
                        0 -> MiniInsightCard(
                            icon = Icons.Default.Timeline,
                            label = AppText.t("stats_peak_time"),
                            value = "${insight.peakHourLabel} · ${formatDuration(insight.peakHourMillis)}",
                            visualRatio = (insight.peakHourMillis.toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        1 -> MiniInsightCard(
                            icon = Icons.Default.NightsStay,
                            label = AppText.t("stats_night_use"),
                            value = formatDuration(insight.nightUsageMillis),
                            visualRatio = (insight.nightUsageMillis.toFloat() / (4 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        else -> MiniInsightCard(
                            icon = Icons.Default.Schedule,
                            label = AppText.t("stats_label_11"),
                            value = insight.longestSession?.let { "${it.label} · ${formatDuration(it.value)}" } ?: AppText.t("stats_none"),
                            visualRatio = ((insight.longestSession?.value ?: 0L).toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                    }
                }
            }
            data.comparison?.comparisons?.take(3)?.forEach { item ->
                ComparisonRow(
                    item = item,
                    averageBarLabel = AppText.t("stats_average"),
                    showChips = false,
                )
            }
        }
    }
}

@Composable
private fun PeriodMonthStructureCard(data: MonthlyWeekStructureData) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(icon = Icons.AutoMirrored.Filled.CallSplit, title = data.title, subtitle = data.subtitle)
            AdaptiveRowGrid(
                itemCount = data.weeks.size,
                compactColumns = 1,
                expandedColumns = 2,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
            ) { modifier, index ->
                val week = data.weeks[index]
                Surface(
                    modifier = modifier,
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(week.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatDuration(week.totalUsageMillis), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(AppText.t("stats_daily_average_value", formatDuration(week.averageUsageMillis)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(AppText.t("stats_peak_time"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(week.peakDayLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodQuarterBreakdownCard(data: YearQuarterSectionData) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(icon = Icons.Default.CalendarMonth, title = data.title, subtitle = data.subtitle)
            AdaptiveRowGrid(
                itemCount = data.quarters.size,
                compactColumns = 1,
                expandedColumns = 2,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
            ) { modifier, index ->
                val quarter = data.quarters[index]
                Surface(
                    modifier = modifier,
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(quarter.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatDuration(quarter.totalUsageMillis), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(AppText.t("stats_best_month"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${quarter.bestMonthLabel} · ${formatDuration(quarter.bestMonthUsageMillis)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text(AppText.t("stats_top_apps"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(quarter.topAppLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTileCompact(
    metric: DailyFocusMetric,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun periodToneColor(
    tone: PeriodTone,
    primary: Color,
    secondary: Color,
    muted: Color,
    reportColors: ReportColors,
): Color {
    return when (tone) {
        PeriodTone.PRIMARY -> primary
        PeriodTone.POSITIVE -> reportColors.positive
        PeriodTone.WARNING -> reportColors.warning
        PeriodTone.SECONDARY -> secondary
        PeriodTone.NEUTRAL -> muted
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyBattleHeroCard(
    heroState: SectionState<HeroSectionData>,
) {
    val data = (heroState as? SectionState.Ready)?.data
    val summary = data?.summary
    val overview = data?.overview
    val reportColors = LocalReportColors.current
    ReportCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            reportColors.positive.copy(alpha = 0.12f),
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (data == null || summary == null || overview == null) {
                    SkeletonLine(width = 110.dp, height = 12.dp)
                    SkeletonLine(width = 156.dp, height = 28.dp)
                    SkeletonLine(fill = true, height = 18.dp)
                    AdaptiveRowGrid(
                        itemCount = 4,
                        compactColumns = 2,
                        expandedColumns = 4,
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp,
                    ) { modifier, _ ->
                        SkeletonMetricChip(modifier = modifier)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = summary.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = summary.subtitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        ) {
                            Text(
                                text = summary.capturedAt,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = summary.primaryValue,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = summary.message,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    AdaptiveRowGrid(
                        itemCount = 2,
                        compactColumns = 1,
                        expandedColumns = 2,
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp,
                    ) { modifier, index ->
                        when (index) {
                            0 -> BattleHeadlineChip(
                                label = AppText.t("stats_vs_previous_archive"),
                                value = summary.secondaryValue,
                                accent = reportColors.warning,
                                modifier = modifier,
                            )
                            else -> BattleHeadlineChip(
                                label = AppText.t("stats_daily_average"),
                                value = summary.tertiaryValue,
                                accent = reportColors.positive,
                                modifier = modifier,
                            )
                        }
                    }
                    AdaptiveRowGrid(
                        itemCount = 4,
                        compactColumns = 2,
                        expandedColumns = 4,
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp,
                    ) { modifier, index ->
                        when (index) {
                            0 -> BattleMetricTile(
                                label = AppText.t("stats_launches"),
                                value = AppText.t("stats_value_times_12", overview.openCount),
                                accent = MaterialTheme.colorScheme.primary,
                                modifier = modifier,
                            )
                            1 -> BattleMetricTile(
                                label = AppText.t("stats_night_use"),
                                value = formatDuration(data.nightUsageMillis),
                                accent = reportColors.warning,
                                modifier = modifier,
                            )
                            2 -> BattleMetricTile(
                                label = AppText.t("stats_target_complete"),
                                value = data.goalCompletionProgress?.let { "${(it * 100f).roundToInt()}%" }
                                    ?: AppText.t("stats_none"),
                                accent = reportColors.positive,
                                modifier = modifier,
                            )
                            else -> BattleMetricTile(
                                label = AppText.t("stats_top_app_of_the_day"),
                                value = overview.topApp?.label ?: AppText.t("stats_none"),
                                accent = MaterialTheme.colorScheme.secondary,
                                modifier = modifier,
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        summary.tags.forEach { tag ->
                            SummaryTagChip(tag)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleHeadlineChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BattleMetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val animatedValue = animateMetricDisplayText(
        rawText = value,
        label = "battle_metric_${label.hashCode()}",
        delayMillis = 180,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.1f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = animatedValue,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DailyRhythmCard(
    timelineState: SectionState<TimelineSectionData>,
) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.Timeline,
                title = AppText.t("stats_24_hour_distribution"),
                subtitle = AppText.t("stats_time_heatmap"),
            )
            when (timelineState) {
                SectionState.Loading -> {
                    SkeletonTimelineChart()
                    AdaptiveRowGrid(
                        itemCount = 3,
                        compactColumns = 1,
                        expandedColumns = 3,
                        horizontalSpacing = 10.dp,
                        verticalSpacing = 10.dp,
                    ) { modifier, _ ->
                        SkeletonMetricChip(modifier = modifier)
                    }
                }
                SectionState.Empty -> {
                    DailyTimelineChart(emptyList())
                    Text(
                        text = AppText.t("stats_this_archived_day_does_not_have_enough_usage"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is SectionState.Ready -> {
                    DailyTimelineChart(
                        buckets = timelineState.data.buckets,
                        targetMillisPerBucket = timelineState.data.targetMillisPerBucket,
                    )
                    TimelineFooter(labels = buildTimelineFooterLabels(ReportTab.DAY, timelineState.data.buckets))
                    AdaptiveRowGrid(
                        itemCount = 3,
                        compactColumns = 1,
                        expandedColumns = 3,
                        horizontalSpacing = 10.dp,
                        verticalSpacing = 10.dp,
                    ) { modifier, index ->
                        when (index) {
                            0 -> MiniInsightCard(
                                icon = Icons.Default.Bolt,
                                label = AppText.t("stats_peak_time"),
                                value = "${timelineState.data.peakHourLabel} · ${formatDuration(timelineState.data.peakHourMillis)}",
                                visualRatio = (timelineState.data.peakHourMillis.toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                                compact = true,
                                modifier = modifier,
                            )
                            1 -> MiniInsightCard(
                                icon = Icons.AutoMirrored.Filled.CallSplit,
                                label = AppText.t("stats_over_2h"),
                                value = "${timelineState.data.peakTwoHourLabel} · ${formatDuration(timelineState.data.peakTwoHourMillis)}",
                                visualRatio = (timelineState.data.peakTwoHourMillis.toFloat() / (4 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                                compact = true,
                                modifier = modifier,
                            )
                            else -> MiniInsightCard(
                                icon = Icons.Default.NightsStay,
                                label = AppText.t("stats_night_use"),
                                value = formatDuration(timelineState.data.nightUsageMillis),
                                visualRatio = (timelineState.data.nightUsageMillis.toFloat() / (3 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                                compact = true,
                                modifier = modifier,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyAppsAndAnalysisCard(
    topAppsState: SectionState<TopAppsSectionData>,
    behaviorState: SectionState<BehaviorSectionData>,
    comparisonState: SectionState<ComparisonSectionData>,
    shareState: SectionState<ShareReportData>,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
) {
    val usageTopApps = (topAppsState as? SectionState.Ready)?.data?.usageTopApps.orEmpty()
    val appColors = rememberAppChartColors(usageTopApps.map { it.packageName })
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.BarChart,
                title = AppText.t("stats_top_10_apps"),
                subtitle = AppText.t("stats_current_day_top_10_apps_only"),
            )
            when (topAppsState) {
                SectionState.Loading -> {
                    SkeletonUsageSharePanel()
                }
                SectionState.Empty -> {
                    Text(
                        text = AppText.t("stats_this_archived_day_does_not_have_enough_usage"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is SectionState.Ready -> {
                    AppUsageShareCard(
                        items = usageTopApps,
                        appColors = appColors,
                    )
                }
            }
            if (isProActive) {
                DailyAnalysisPanel(
                    behaviorState = behaviorState,
                    comparisonState = comparisonState,
                    shareState = shareState,
                )
            } else {
                CompactLockedAnalysisPanel(
                    onClick = { onShowProUpsell(ProUpsellSource.ADVANCED_REPORT) },
                )
            }
        }
    }
}

@Composable
private fun DailyAnalysisPanel(
    behaviorState: SectionState<BehaviorSectionData>,
    comparisonState: SectionState<ComparisonSectionData>,
    shareState: SectionState<ShareReportData>,
) {
    val insight = (behaviorState as? SectionState.Ready)?.data?.behaviorInsight
    val comparisons = (comparisonState as? SectionState.Ready)?.data?.comparisons.orEmpty().take(3)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.66f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionHeader(
                icon = Icons.Default.Insights,
                title = AppText.t("stats_behavior_analysis"),
                subtitle = AppText.t("stats_compare_current_day_with_previous_archive"),
            )
            if (behaviorState == SectionState.Loading || comparisonState == SectionState.Loading) {
                AdaptiveRowGrid(
                    itemCount = 4,
                    compactColumns = 2,
                    expandedColumns = 2,
                    horizontalSpacing = 10.dp,
                    verticalSpacing = 10.dp,
                ) { modifier, _ ->
                    SkeletonMetricChip(modifier = modifier)
                }
                repeat(2) {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth(),
                        height = 74.dp,
                        shape = RoundedCornerShape(18.dp),
                    )
                }
            } else if (insight == null && comparisons.isEmpty()) {
                Text(
                    text = AppText.t("stats_this_archived_day_does_not_have_enough_behavior"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                insight?.let {
                    AdaptiveRowGrid(
                        itemCount = 4,
                        compactColumns = 2,
                        expandedColumns = 2,
                        horizontalSpacing = 10.dp,
                        verticalSpacing = 10.dp,
                    ) { modifier, index ->
                        when (index) {
                            0 -> MiniInsightCard(
                                icon = Icons.Default.Schedule,
                                label = AppText.t("stats_label_11"),
                                value = it.longestSession?.let { session ->
                                    "${session.label} · ${formatDuration(session.value)}"
                                } ?: AppText.t("stats_none"),
                                visualRatio = ((it.longestSession?.value ?: 0L).toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                                modifier = modifier,
                            )
                            1 -> MiniInsightCard(
                                icon = Icons.Default.AccessTime,
                                label = AppText.t("stats_average_session"),
                                value = formatDuration(it.averageSessionMillis),
                                visualRatio = (it.averageSessionMillis.toFloat() / (30 * 60_000L).toFloat()).coerceIn(0f, 1f),
                                modifier = modifier,
                            )
                            2 -> MiniInsightCard(
                                icon = Icons.Default.Timeline,
                                label = AppText.t("stats_peak_time"),
                                value = "${it.peakHourLabel} · ${formatDuration(it.peakHourMillis)}",
                                visualRatio = (it.peakHourMillis.toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                                modifier = modifier,
                            )
                            else -> MiniInsightCard(
                                icon = Icons.Default.TouchApp,
                                label = AppText.t("stats_launch_intensity"),
                                value = String.format(Locale.CHINA, AppText.t("stats_launches_per_active_hour_format"), it.reopenIntensity),
                                visualRatio = (it.reopenIntensity / 6f).coerceIn(0f, 1f),
                                modifier = modifier,
                            )
                        }
                    }
                }
                if (comparisons.isEmpty()) {
                    Text(
                        text = AppText.t("stats_not_enough_earlier_archive_samples"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    comparisons.forEachIndexed { index, item ->
                        ComparisonRow(
                            item = item,
                            delayMillis = 700 + index * 40,
                            averageBarLabel = AppText.t("stats_seven_day"),
                            showChips = false,
                        )
                    }
                }
            }
            CompactShareReportRow(shareState = shareState)
        }
    }
}

@Composable
private fun CompactLockedAnalysisPanel(
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.66f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader(
                icon = Icons.Default.Insights,
                title = AppText.t("pro_report_locked_title"),
                subtitle = AppText.t("pro_upsell_advanced_report"),
            )
            AdaptiveRowGrid(
                itemCount = 4,
                compactColumns = 2,
                expandedColumns = 2,
                horizontalSpacing = 10.dp,
                verticalSpacing = 10.dp,
            ) { modifier, index ->
                MiniInsightCard(
                    icon = when (index) {
                        0 -> Icons.Default.Schedule
                        1 -> Icons.Default.Timeline
                        2 -> Icons.Default.TouchApp
                        else -> Icons.AutoMirrored.Filled.CompareArrows
                    },
                    label = AppText.t("pro_report_preview_label_${index + 1}"),
                    value = AppText.t("pro_report_preview_value"),
                    visualRatio = 0.32f + index * 0.12f,
                    modifier = modifier.graphicsLayer { alpha = 0.52f },
                )
            }
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("pro_view_benefits"))
            }
        }
    }
}

@Composable
private fun LockedAdvancedReportCard(onClick: () -> Unit) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader(
                icon = Icons.Default.Insights,
                title = AppText.t("pro_report_locked_title"),
                subtitle = AppText.t("pro_report_locked_subtitle"),
            )
            AdaptiveRowGrid(
                itemCount = 4,
                compactColumns = 2,
                expandedColumns = 2,
            ) { modifier, index ->
                MiniInsightCard(
                    icon = when (index) {
                        0 -> Icons.Default.Timeline
                        1 -> Icons.Default.CalendarMonth
                        2 -> Icons.Default.BarChart
                        else -> Icons.AutoMirrored.Filled.CompareArrows
                    },
                    label = AppText.t("pro_report_preview_label_${index + 1}"),
                    value = AppText.t("pro_report_preview_value"),
                    visualRatio = 0.42f + index * 0.12f,
                    modifier = modifier.graphicsLayer { alpha = 0.45f },
                )
            }
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("pro_view_benefits"))
            }
        }
    }
}

@Composable
private fun LoadingHintChip(selectedTab: ReportTab) {
    val label =
        when (selectedTab) {
            ReportTab.DAY -> AppText.t("stats_reading_archived_daily_reports")
            ReportTab.WEEK -> AppText.t("stats_updating_last_7_days")
            ReportTab.MONTH -> AppText.t("stats_updating_last_30_days")
            ReportTab.YEAR -> AppText.t("stats_yearly_report_label")
        }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun ArchiveDateNavigator(
    selectedArchiveDate: String,
    previousArchiveDate: String?,
    nextArchiveDate: String?,
    availableArchiveDates: List<String>,
    onPreviousArchiveDate: () -> Unit,
    onNextArchiveDate: () -> Unit,
    onSelectArchiveDate: (String) -> Unit,
) {
    var showCalendar by remember(selectedArchiveDate) { mutableStateOf(false) }
    val availableDates = remember(availableArchiveDates) { availableArchiveDates.map(LocalDate::parse).toSet() }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = onPreviousArchiveDate,
                enabled = previousArchiveDate != null,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = AppText.t("stats_previous_day"),
                )
            }
            Surface(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable { showCalendar = true },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = formatArchiveDate(selectedArchiveDate, AppText.t("home_mmm_d_eeee")),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = AppText.t("stats_only_archived_dates_selectable"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            IconButton(
                onClick = onNextArchiveDate,
                enabled = nextArchiveDate != null,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = AppText.t("stats_next_day"),
                )
            }
        }
    }
    if (showCalendar) {
        ArchiveCalendarDialog(
            selectedArchiveDate = LocalDate.parse(selectedArchiveDate),
            availableDates = availableDates,
            onDismiss = { showCalendar = false },
            onSelectDate = { date ->
                showCalendar = false
                onSelectArchiveDate(ArchiveDateUtils.formatDate(date))
            },
        )
    }
}

@Composable
private fun ArchiveCalendarDialog(
    selectedArchiveDate: LocalDate,
    availableDates: Set<LocalDate>,
    onDismiss: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
) {
    var displayedMonth by remember(selectedArchiveDate, availableDates) {
        mutableStateOf(
            availableDates
                .firstOrNull { it == selectedArchiveDate }
                ?.let { YearMonth.from(it) }
                ?: YearMonth.from(selectedArchiveDate),
        )
    }
    val minMonth = remember(availableDates) { availableDates.minOrNull()?.let { YearMonth.from(it) } ?: YearMonth.from(selectedArchiveDate) }
    val maxMonth = remember(availableDates) { availableDates.maxOrNull()?.let { YearMonth.from(it) } ?: YearMonth.from(selectedArchiveDate) }
    val firstOfMonth = displayedMonth.atDay(1)
    val leadingBlankDays = (firstOfMonth.dayOfWeek.value + 6) % 7
    val daysInMonth = displayedMonth.lengthOfMonth()
    val daySlots =
        buildList<LocalDate?> {
            repeat(leadingBlankDays) { add(null) }
            repeat(daysInMonth) { offset ->
                add(displayedMonth.atDay(offset + 1))
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_close"))
            }
        },
        title = {
            Text(
                text = AppText.t("stats_choose_archive_date"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                        enabled = displayedMonth > minMonth,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("stats_last_month"))
                    }
                    Text(
                        text = displayedMonth.format(DateTimeFormatter.ofPattern(AppText.t("stats_mmmm_yyyy"), Locale.CHINA)),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(
                        onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                        enabled = displayedMonth < maxMonth,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = AppText.t("stats_next_month"))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf(AppText.t("stats_mon"), AppText.t("stats_tue"), AppText.t("stats_wed"), AppText.t("stats_thu"), AppText.t("stats_fri"), AppText.t("stats_sat"), AppText.t("stats_sun")).forEach { dayLabel ->
                        Text(
                            text = dayLabel,
                            modifier = Modifier.width(32.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                daySlots.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        week.forEach { date ->
                            when {
                                date == null -> Spacer(modifier = Modifier.size(32.dp))
                                else -> {
                                    val isSelected = date == selectedArchiveDate
                                    val isEnabled = date in availableDates
                                    Surface(
                                        modifier =
                                            Modifier
                                                .size(32.dp)
                                                .clickable(enabled = isEnabled) { onSelectDate(date) },
                                        shape = CircleShape,
                                        color =
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                else -> Color.Transparent
                                            },
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = date.dayOfMonth.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color =
                                                    when {
                                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                                        isEnabled -> MaterialTheme.colorScheme.onSurface
                                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                                                    },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    }
                }
                Text(
                    text = AppText.t("stats_unarchived_dates_not_selectable"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun HeroSkeletonCard() {
    ReportCard {
        AdaptiveRowGrid(
            itemCount = 2,
            compactColumns = 1,
            expandedColumns = 2,
            horizontalSpacing = 16.dp,
            verticalSpacing = 16.dp,
        ) { modifier, index ->
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.76f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when (index) {
                        0 -> {
                            SkeletonLine(width = 88.dp, height = 12.dp)
                            SkeletonLine(width = 110.dp, height = 20.dp)
                            SkeletonDonutChart(chartSize = 168.dp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SkeletonPill(width = 72.dp)
                                SkeletonPill(width = 78.dp)
                            }
                        }
                        else -> {
                            SkeletonLine(fill = true, height = 18.dp)
                            AdaptiveRowGrid(
                                itemCount = 4,
                                compactColumns = 2,
                                expandedColumns = 2,
                            ) { childModifier, _ ->
                                SkeletonMetricChip(modifier = childModifier)
                            }
                            SkeletonBlock(
                                modifier = Modifier.fillMaxWidth(),
                                height = 62.dp,
                                shape = RoundedCornerShape(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineSkeletonCard() {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SkeletonSectionHeader()
            SkeletonTimelineChart()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                repeat(5) {
                    SkeletonLine(width = 18.dp, height = 10.dp)
                }
            }
            AdaptiveRowGrid(
                itemCount = 2,
                compactColumns = 1,
                expandedColumns = 2,
                horizontalSpacing = 14.dp,
                verticalSpacing = 14.dp,
            ) { modifier, index ->
                if (index == 0) {
                    SkeletonDonutPanel(modifier = modifier)
                } else {
                    SkeletonPeakPanel(modifier = modifier)
                }
            }
        }
    }
}

@Composable
private fun AppChartsSkeletonCard() {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SkeletonSectionHeader()
            SkeletonUsageSharePanel()
            SkeletonRankingPanel()
        }
    }
}

@Composable
private fun BehaviorSkeletonCard() {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SkeletonSectionHeader()
            AdaptiveRowGrid(
                itemCount = 5,
                compactColumns = 2,
                expandedColumns = 2,
            ) { modifier, _ ->
                SkeletonMetricChip(modifier = modifier)
            }
            AdaptiveRowGrid(
                itemCount = 2,
                compactColumns = 1,
                expandedColumns = 2,
                horizontalSpacing = 12.dp,
                verticalSpacing = 12.dp,
            ) { modifier, _ ->
                SkeletonBlock(
                    modifier = modifier,
                    height = 72.dp,
                    shape = RoundedCornerShape(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ComparisonSkeletonCard() {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SkeletonSectionHeader()
            repeat(3) { index ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonLine(width = 72.dp, height = 12.dp)
                    SkeletonLine(width = 96.dp, height = 24.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonPill(width = 78.dp)
                        SkeletonPill(width = 84.dp)
                    }
                }
                if (index != 2) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                }
            }
        }
    }
}

@Composable
private fun SkeletonSectionHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SkeletonLine(width = 92.dp, height = 18.dp)
        SkeletonLine(width = 180.dp, height = 12.dp)
    }
}

@Composable
private fun SkeletonMetricChip(modifier: Modifier = Modifier) {
    SkeletonBlock(
        modifier = modifier,
        height = 86.dp,
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
private fun SkeletonPill(width: androidx.compose.ui.unit.Dp) {
    SkeletonBlock(
        modifier = Modifier.width(width),
        height = 28.dp,
        shape = RoundedCornerShape(999.dp),
    )
}

@Composable
private fun SkeletonCircle(size: androidx.compose.ui.unit.Dp) {
    SkeletonBlock(
        modifier = Modifier.size(size),
        height = size,
        shape = CircleShape,
    )
}

@Composable
private fun SkeletonDonutChart(chartSize: androidx.compose.ui.unit.Dp) {
    val (baseColor, accentColor) = rememberSkeletonColors()
    Canvas(modifier = Modifier.size(chartSize)) {
        val stroke = size.minDimension * 0.12f
        val diameter = size.minDimension - stroke
        drawArc(
            color = baseColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f),
            size = Size(diameter, diameter),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        )
        drawArc(
            color = accentColor,
            startAngle = -70f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f),
            size = Size(diameter, diameter),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        )
    }
}

@Composable
private fun SkeletonTimelineChart() {
    val (baseColor, _) = rememberSkeletonColors()
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(156.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
        ) {
            repeat(4) {
                SkeletonLine(width = 24.dp, height = 10.dp)
            }
        }
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(156.dp),
        ) {
            repeat(4) { index ->
                val y = size.height - (index * (size.height / 3f))
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
            }
            val bars = listOf(0.18f, 0.42f, 0.36f, 0.55f, 0.28f, 0.62f, 0.74f, 0.31f, 0.25f, 0.44f, 0.52f, 0.38f)
            val slotWidth = size.width / 24f
            val barWidth = slotWidth * 0.5f
            bars.forEachIndexed { index, ratio ->
                val x = slotWidth * index * 2 + (slotWidth - barWidth) / 2f
                val barHeight = size.height * ratio
                drawRoundRect(
                    color = baseColor,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                )
            }
        }
    }
}

@Composable
private fun SkeletonDonutPanel(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonLine(width = 76.dp, height = 14.dp)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                SkeletonDonutChart(chartSize = 156.dp)
            }
            repeat(4) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SkeletonCircle(size = 10.dp)
                    SkeletonLine(width = 40.dp, height = 12.dp)
                    SkeletonBlock(
                        modifier = Modifier.weight(1f),
                        height = 6.dp,
                        shape = RoundedCornerShape(999.dp),
                    )
                    SkeletonLine(width = 34.dp, height = 12.dp)
                }
            }
        }
    }
}

@Composable
private fun SkeletonPeakPanel(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SkeletonLine(width = 72.dp, height = 14.dp)
            repeat(3) {
                SkeletonMetricChip(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SkeletonUsageSharePanel() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonLine(width = 92.dp, height = 14.dp)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                SkeletonDonutChart(chartSize = 176.dp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(4) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.42f),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            SkeletonCircle(size = 28.dp)
                            SkeletonLine(width = 24.dp, height = 10.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonRankingPanel() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonLine(width = 76.dp, height = 14.dp)
            repeat(5) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SkeletonPill(width = 28.dp)
                    SkeletonCircle(size = 34.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SkeletonLine(width = 88.dp, height = 12.dp)
                        SkeletonBlock(
                            modifier = Modifier.fillMaxWidth(),
                            height = 10.dp,
                            shape = RoundedCornerShape(999.dp),
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SkeletonLine(width = 38.dp, height = 12.dp)
                        SkeletonLine(width = 28.dp, height = 10.dp)
                    }
                }
                if (index != 4) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
                }
            }
        }
    }
}

@Composable
private fun SkeletonLine(
    width: androidx.compose.ui.unit.Dp = 0.dp,
    height: androidx.compose.ui.unit.Dp = 14.dp,
    fill: Boolean = false,
) {
    SkeletonBlock(
        modifier = if (fill) Modifier.fillMaxWidth() else Modifier.width(width),
        height = height,
        shape = RoundedCornerShape(999.dp),
    )
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp,
    shape: androidx.compose.ui.graphics.Shape,
) {
    val shimmerBrush = rememberSkeletonBrush()
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(shimmerBrush),
    )
}

@Composable
private fun rememberSkeletonBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton_shimmer_progress",
    )
    val reportColors = LocalReportColors.current
    val base = reportColors.skeletonBase.copy(alpha = 0.92f)
    val highlight = reportColors.skeletonHighlight
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(progress * 420f - 220f, progress * 180f - 120f),
        end = Offset(progress * 420f + 220f, progress * 180f + 120f),
    )
}

@Composable
private fun rememberSkeletonColors(): Pair<Color, Color> {
    val transition = rememberInfiniteTransition(label = "skeleton_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton_pulse_progress",
    )
    val reportColors = LocalReportColors.current
    val base = reportColors.skeletonBase.copy(alpha = 0.92f + 0.08f * pulse)
    val accent = reportColors.skeletonAccent.copy(alpha = 0.72f + 0.18f * pulse)
    return base to accent
}

@Composable
private fun PlaceholderReportScreen(
    state: DailyReportUiState,
    onTabSelected: (ReportTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ReportTabRow(selectedTab = state.selectedTab, onTabSelected = onTabSelected)
        ReportCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = AppText.t("stats_trend_views_are_coming_soon"),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Text(
                    text = state.placeholderTitle.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = state.placeholderDescription.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = AppText.t("stats_daily_reports_come_first_trend_pages_will_open"),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRequiredState(
    onRequestUsageAccess: () -> Unit,
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ReportCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = AppText.t("stats_report_needs_usage_records_permission"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = AppText.t("stats_enable_usage_records_for_daily_report"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onRequestUsageAccess,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(AppText.t("stats_view_details_and_enable"))
                    }
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(AppText.t("stats_open_settings"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportTabRow(
    selectedTab: ReportTab,
    onTabSelected: (ReportTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReportTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                },
                onClick = { onTabSelected(tab) },
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.label(),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceHeroCard(
    selectedTab: ReportTab,
    heroState: SectionState<HeroSectionData>,
) {
    ReportCard {
        AdaptiveRowGrid(
            itemCount = 2,
            compactColumns = 1,
            expandedColumns = 2,
            horizontalSpacing = 12.dp,
            verticalSpacing = 12.dp,
        ) { modifier, index ->
            when (index) {
                0 -> DeviceHeroVisualPanel(
                    selectedTab = selectedTab,
                    heroState = heroState,
                    modifier = modifier,
                )
                else -> DeviceHeroMetricsPanel(
                    heroState = heroState,
                    modifier = modifier,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeviceHeroVisualPanel(
    selectedTab: ReportTab,
    heroState: SectionState<HeroSectionData>,
    modifier: Modifier = Modifier,
) {
    val data = (heroState as? SectionState.Ready)?.data
    val summary = data?.summary ?: DailyReportSummary(
        title = AppText.t("stats_archived_daily_reports"),
        subtitle = "",
        capturedAt = "",
        message = "",
        primaryValue = "",
        secondaryValue = "",
        tertiaryValue = "",
        tags = emptyList(),
    )
    val overview = data?.overview ?: ScopeOverview(
        totalUsageMillis = 0L,
        openCount = 0,
        activeBucketCount = 0,
        topApp = null,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
    ) {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            val chartHeight = if (compact) 126.dp else 146.dp
            val contentPadding = if (compact) 16.dp else 18.dp
            val contentSpacing = if (compact) 12.dp else 14.dp
            Column(
                modifier = Modifier.padding(horizontal = contentPadding, vertical = contentPadding),
                verticalArrangement = Arrangement.spacedBy(contentSpacing),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (data == null) {
                            SkeletonLine(width = 88.dp, height = 12.dp)
                        } else {
                            Text(
                                text = data.summary.subtitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = data?.summary?.title ?: AppText.t("stats_archived_daily_reports"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    if (data == null) {
                        SkeletonLine(width = 72.dp, height = 10.dp)
                    } else {
                        Text(
                            text = data.summary.capturedAt,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (data == null) {
                        SkeletonBlock(
                            modifier = Modifier.fillMaxWidth(),
                            height = chartHeight,
                            shape = RoundedCornerShape(24.dp),
                        )
                    } else {
                        UsageGoalChart(
                            usageMillis = data.overview.totalUsageMillis,
                            capMillis = data.dailyGoalMillis.takeIf { selectedTab == ReportTab.DAY && it > 0L }
                                ?: usageDialCapMillis(selectedTab),
                            goalLabel = data.dailyGoalMillis.takeIf { selectedTab == ReportTab.DAY && it > 0L }
                                ?.let { AppText.t("stats_target_value_2", formatDuration(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(chartHeight),
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (data == null) {
                        SkeletonPill(width = 72.dp)
                        SkeletonPill(width = 80.dp)
                        SkeletonPill(width = 64.dp)
                    } else {
                        data.summary.tags.forEach { tag ->
                            SummaryTagChip(tag)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceHeroMetricsPanel(
    heroState: SectionState<HeroSectionData>,
    modifier: Modifier = Modifier,
) {
    val data = (heroState as? SectionState.Ready)?.data
    val overview = data?.overview ?: ScopeOverview(
        totalUsageMillis = 0L,
        openCount = 0,
        activeBucketCount = 0,
        topApp = null,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
    ) {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            val contentPadding = if (compact) 16.dp else 18.dp
            val contentSpacing = if (compact) 12.dp else 14.dp
            Column(
                modifier = Modifier.padding(horizontal = contentPadding, vertical = contentPadding),
                verticalArrangement = Arrangement.spacedBy(contentSpacing),
            ) {
                if (data == null) {
                    SkeletonLine(fill = true, height = 18.dp)
                } else {
                    Text(
                        text = data.summary.message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                AdaptiveRowGrid(
                    itemCount = 4,
                    compactColumns = 2,
                    expandedColumns = 2,
                    horizontalSpacing = 10.dp,
                    verticalSpacing = 10.dp,
                ) { childModifier, index ->
                    if (data == null) {
                        SkeletonMetricChip(modifier = childModifier)
                        return@AdaptiveRowGrid
                    }
                    when (index) {
                        0 -> HeroMetricChip(
                            icon = Icons.Default.PhoneAndroid,
                            label = AppText.t("stats_device_usage"),
                            value = data.summary.primaryValue,
                            modifier = childModifier,
                        )
                        1 -> HeroMetricChip(
                            icon = Icons.AutoMirrored.Filled.CompareArrows,
                            label = if (data.summary.title == AppText.t("stats_archived_daily_reports")) AppText.t("stats_label_10") else AppText.t("stats_comparison_baseline"),
                            value = data.summary.secondaryValue,
                            modifier = childModifier,
                        )
                        2 -> HeroMetricChip(
                            icon = Icons.Default.TouchApp,
                            label = AppText.t("stats_launches"),
                            value = AppText.t("stats_value_times_12", overview.openCount),
                            modifier = childModifier,
                        )
                        else -> HeroMetricChip(
                            icon = Icons.Default.NightsStay,
                            label =
                                if (data.summary.title == AppText.t("stats_archived_daily_reports") && data.goalCompletionProgress != null) {
                                    AppText.t("stats_target_complete")
                                } else {
                                    AppText.t("stats_night_use")
                                },
                            value =
                                if (data.summary.title == AppText.t("stats_archived_daily_reports") && data.goalCompletionProgress != null) {
                                    "${(data.goalCompletionProgress * 100f).roundToInt()}%"
                                } else {
                                    formatDuration(data.nightUsageMillis)
                                },
                            modifier = childModifier,
                        )
                    }
                }
                if (data == null) {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth(),
                        height = 62.dp,
                        shape = RoundedCornerShape(20.dp),
                    )
                } else {
                    overview.topApp?.let { topApp ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (compact) 12.dp else 14.dp,
                                vertical = if (compact) 10.dp else 12.dp,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp),
                        ) {
                            val animatedTopAppValue = animateMetricDisplayText(
                                rawText = formatDuration(topApp.value),
                                label = "hero_top_app_${topApp.packageName}",
                                delayMillis = 240,
                            )
                            AppIconCircle(topApp.packageName)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (data.summary.title == AppText.t("stats_archived_daily_reports")) AppText.t("stats_top_app_of_the_day") else AppText.t("stats_top_app_in_window"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = topApp.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = animatedTopAppValue,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun UsageGoalChart(
    usageMillis: Long,
    capMillis: Long,
    goalLabel: String?,
    metricLabel: String? = AppText.t("stats_device_usage"),
    modifier: Modifier = Modifier,
) {
    val stagedUsageMillis = rememberDelayedLongTarget(usageMillis, 40)
    val animatedUsageMillis = animateLongValue(
        targetValue = stagedUsageMillis,
        label = "usage_dial_value",
        durationMillis = 880,
    )
    val animatedProgress = animateDecimalValue(
        targetValue = (stagedUsageMillis.toFloat() / capMillis.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1.15f),
        label = "usage_goal_bar_progress",
        durationMillis = 840,
        delayMillis = 120,
    )
    val reportColors = LocalReportColors.current
    val primary = MaterialTheme.colorScheme.primary
    val warning = reportColors.warning
    val overLimit = usageMillis > capMillis && capMillis > 0L
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = primary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, primary.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    metricLabel?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = formatDuration(animatedUsageMillis),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text =
                        goalLabel ?: if (capMillis > 0L) {
                            AppText.t("stats_reference_value", formatDuration(capMillis))
                        } else {
                            AppText.t("stats_no_targets_yet")
                        },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (overLimit) warning else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Canvas(modifier = Modifier.fillMaxWidth().height(42.dp)) {
                val trackHeight = size.height * 0.46f
                val top = (size.height - trackHeight) / 2f
                val radius = trackHeight / 2f
                drawRoundRect(
                    color = primary.copy(alpha = 0.12f),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, trackHeight),
                    cornerRadius = CornerRadius(radius, radius),
                )
                val cappedProgress = animatedProgress.coerceIn(0f, 1f)
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.72f),
                            if (overLimit) warning.copy(alpha = 0.92f) else primary,
                        ),
                    ),
                    topLeft = Offset(0f, top),
                    size = Size(size.width * cappedProgress, trackHeight),
                    cornerRadius = CornerRadius(radius, radius),
                )
                if (animatedProgress > 1f) {
                    val overWidth = size.width * (animatedProgress - 1f).coerceIn(0f, 0.15f) / 0.15f
                    drawRoundRect(
                        color = warning.copy(alpha = 0.28f),
                        topLeft = Offset(size.width - overWidth, top - 5f),
                        size = Size(overWidth, trackHeight + 10f),
                        cornerRadius = CornerRadius(radius, radius),
                    )
                }
                drawLine(
                    color = if (overLimit) warning else primary.copy(alpha = 0.55f),
                    start = Offset(size.width, top - 6f),
                    end = Offset(size.width, top + trackHeight + 6f),
                    strokeWidth = 3f,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (overLimit) AppText.t("stats_over_by_value_2", formatDuration(usageMillis - capMillis)) else AppText.t("stats_remaining_value_2", formatDuration((capMillis - usageMillis).coerceAtLeast(0L))),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (overLimit) warning else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${((usageMillis.toFloat() / capMillis.coerceAtLeast(1L).toFloat()) * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (overLimit) warning else primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun usageDialCapMillis(selectedTab: ReportTab): Long =
    when (selectedTab) {
        ReportTab.DAY -> 12L * 60L * 60_000L
        ReportTab.WEEK -> 56L * 60L * 60_000L
        ReportTab.MONTH -> 180L * 60L * 60_000L
        ReportTab.YEAR -> 720L * 60L * 60_000L
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryTagChip(tag: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AdaptiveRowGrid(
    itemCount: Int,
    compactColumns: Int,
    expandedColumns: Int,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 10.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 10.dp,
    itemContent: @Composable (Modifier, Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 420.dp) compactColumns else expandedColumns
        val rows = (0 until itemCount).toList().chunked(columns.coerceAtLeast(1))
        Column(verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                ) {
                    rowItems.forEach { index ->
                        itemContent(Modifier.weight(1f), index)
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyFocusCard(
    focusState: SectionState<DailyFocusSectionData>,
    compactLayout: Boolean = false,
) {
    when (focusState) {
        SectionState.Empty -> Unit
        SectionState.Loading -> {
            AdaptiveRowGrid(
                itemCount = 2,
                compactColumns = 2,
                expandedColumns = 2,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
            ) { modifier, _ ->
                SkeletonBlock(
                    modifier = modifier,
                    height = 208.dp,
                    shape = RoundedCornerShape(28.dp),
                )
            }
        }
        is SectionState.Ready -> {
            AdaptiveRowGrid(
                itemCount = 2,
                compactColumns = 2,
                expandedColumns = 2,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
            ) { modifier, index ->
                if (index == 0) {
                    DailyModeSummaryCard(
                        summary = focusState.data.control,
                        icon = Icons.Default.Bolt,
                        compact = compactLayout,
                        modifier = modifier,
                    )
                } else {
                    DailyModeSummaryCard(
                        summary = focusState.data.encourage,
                        icon = Icons.Default.RocketLaunch,
                        compact = compactLayout,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WindowFocusCard(
    focusState: SectionState<WindowFocusSectionData>,
) {
    when (focusState) {
        SectionState.Empty -> Unit
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                AdaptiveRowGrid(itemCount = 2, compactColumns = 1, expandedColumns = 2) { modifier, _ ->
                    SkeletonBlock(modifier = modifier, height = 210.dp, shape = RoundedCornerShape(24.dp))
                }
            }
        }
        is SectionState.Ready -> {
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.EmojiEvents,
                    title = AppText.t("stats_control_and_encourage_review"),
                    subtitle = AppText.t("stats_dashboard_summary_description"),
                )
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 10.dp,
                    verticalSpacing = 10.dp,
                ) { modifier, index ->
                    if (index == 0) {
                        DailyModeSummaryCard(
                            summary = focusState.data.control,
                            icon = Icons.Default.Bolt,
                            modifier = modifier,
                        )
                    } else {
                        DailyModeSummaryCard(
                            summary = focusState.data.encourage,
                            icon = Icons.Default.RocketLaunch,
                            modifier = modifier,
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    focusState.data.highlights.forEachIndexed { index, metric ->
                        FocusMetricPill(
                            metric = metric,
                            accent = MaterialTheme.colorScheme.primary,
                            delayMillis = 180 + index * 40,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearDualScopeCard(
    yearState: SectionState<YearDualScopeSectionData>,
) {
    when (yearState) {
        SectionState.Empty -> Unit
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                AdaptiveRowGrid(itemCount = 2, compactColumns = 1, expandedColumns = 2) { modifier, _ ->
                    SkeletonMetricChip(modifier = modifier)
                }
            }
        }
        is SectionState.Ready -> {
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.CalendarMonth,
                    title = AppText.t("stats_year_dual_view"),
                    subtitle = AppText.t("stats_year_dual_view_description"),
                )
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 10.dp,
                    verticalSpacing = 10.dp,
                ) { modifier, index ->
                    YearScopePanel(
                        summary = if (index == 0) yearState.data.naturalYear else yearState.data.rollingYear,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun YearScopePanel(
    summary: YearScopeSummary,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(summary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(summary.rangeLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(summary.totalUsage, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            AdaptiveRowGrid(itemCount = 4, compactColumns = 2, expandedColumns = 2, verticalSpacing = 8.dp) { childModifier, index ->
                val metric =
                    when (index) {
                        0 -> DailyFocusMetric(AppText.t("stats_daily_average"), summary.averageUsage)
                        1 -> DailyFocusMetric(AppText.t("stats_active"), summary.activeDays)
                        2 -> DailyFocusMetric(AppText.t("stats_label_5"), summary.savedUsage)
                        else -> DailyFocusMetric(AppText.t("stats_net_points"), summary.pointsNet)
                    }
                FocusMetricPill(
                    metric = metric,
                    accent = MaterialTheme.colorScheme.primary,
                    delayMillis = 120 + index * 40,
                    modifier = childModifier,
                )
            }
        }
    }
}

@Composable
private fun HeatmapCard(
    heatmapState: SectionState<HeatmapSectionData>,
) {
    ReportCard {
        when (heatmapState) {
            SectionState.Loading -> {
                SkeletonSectionHeader()
                SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 180.dp, shape = RoundedCornerShape(24.dp))
            }
            SectionState.Empty -> {
                SectionHeader(Icons.Default.CalendarMonth, AppText.t("stats_heatmap"), AppText.t("stats_not_enough_archived_data_to_build_a_heatmap"))
            }
            is SectionState.Ready -> {
                SectionHeader(
                    icon = Icons.Default.CalendarMonth,
                    title = heatmapState.data.title,
                    subtitle = heatmapState.data.subtitle,
                )
                HeatmapGrid(data = heatmapState.data)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeatmapGrid(data: HeatmapSectionData) {
    val reportColors = LocalReportColors.current
    val maxValue = data.days.maxOfOrNull { it.valueMillis }?.coerceAtLeast(1L) ?: 1L
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        data.days.forEach { day ->
            val ratio = (day.valueMillis.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
            val cellColor =
                when {
                    day.exceeded -> reportColors.warning.copy(alpha = 0.26f + ratio * 0.58f)
                    day.valueMillis > 0L -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f + ratio * 0.64f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
                }
            Surface(
                modifier = Modifier.size(if (data.days.size <= 12) 56.dp else 34.dp),
                shape = RoundedCornerShape(if (data.days.size <= 12) 16.dp else 10.dp),
                color = cellColor,
                border = if (day.selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = day.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (day.selected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareReportCard(
    selectedTab: ReportTab,
    shareState: SectionState<ShareReportData>,
) {
    val context = LocalContext.current
    val reportColors = LocalReportColors.current
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val data = (shareState as? SectionState.Ready)?.data
    var previewBitmap by remember(data) { mutableStateOf<Bitmap?>(null) }
    fun generatePreview() {
        val readyData = data ?: return
        runCatching {
            renderShareReportBitmapV2(
                context = context,
                data = readyData,
                primary = primary,
                surface = surface,
                onSurface = onSurface,
                onSurfaceVariant = onSurfaceVariant,
                palette = reportColors.appChartPalette,
            )
        }.onSuccess { bitmap ->
            previewBitmap = bitmap
        }.onFailure { error ->
            Toast.makeText(context, error.message ?: AppText.t("stats_failed_to_generate_share_image"), Toast.LENGTH_SHORT).show()
        }
    }
    ReportCard {
        SectionHeader(
            icon = Icons.Default.Share,
            title = AppText.t("stats_share_report"),
            subtitle = AppText.t("stats_preview_the_poster_then_share_it_with_friends"),
        )
        if (data == null) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 126.dp, shape = RoundedCornerShape(24.dp))
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(data.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(data.insight, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = { generatePreview() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppText.t("stats_preview_share_poster"))
                    }
                }
            }
        }
    }
    previewBitmap?.let { bitmap ->
        SharePreviewDialog(
            bitmap = bitmap,
            onDismiss = { previewBitmap = null },
            onRegenerate = { generatePreview() },
            onShare = {
                runCatching {
                    shareReportBitmap(context = context, bitmap = bitmap)
                }.onFailure { error ->
                    Toast.makeText(context, error.message ?: AppText.t("stats_failed_to_generate_share_image"), Toast.LENGTH_SHORT).show()
                }
            },
        )
    }
}

@Composable
private fun CompactShareReportRow(
    shareState: SectionState<ShareReportData>,
) {
    val context = LocalContext.current
    val reportColors = LocalReportColors.current
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val data = (shareState as? SectionState.Ready)?.data
    var previewBitmap by remember(data) { mutableStateOf<Bitmap?>(null) }

    fun generatePreview() {
        val readyData = data ?: return
        runCatching {
            renderShareReportBitmapV2(
                context = context,
                data = readyData,
                primary = primary,
                surface = surface,
                onSurface = onSurface,
                onSurfaceVariant = onSurfaceVariant,
                palette = reportColors.appChartPalette,
            )
        }.onSuccess { bitmap ->
            previewBitmap = bitmap
        }.onFailure { error ->
            Toast.makeText(
                context,
                error.message ?: AppText.t("stats_failed_to_generate_share_image"),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
    ) {
        if (shareState == SectionState.Loading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SkeletonCircle(18.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SkeletonLine(width = 96.dp, height = 12.dp)
                    SkeletonLine(fill = true, height = 10.dp)
                }
                SkeletonPill(width = 74.dp)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = AppText.t("stats_share_report"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = data?.insight ?: AppText.t("stats_preview_the_poster_then_share_it_with_friends"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Button(
                    onClick = { generatePreview() },
                    enabled = data != null,
                ) {
                    Text(AppText.t("stats_preview_share_poster"))
                }
            }
        }
    }

    previewBitmap?.let { bitmap ->
        SharePreviewDialog(
            bitmap = bitmap,
            onDismiss = { previewBitmap = null },
            onRegenerate = { generatePreview() },
            onShare = {
                runCatching {
                    shareReportBitmap(context = context, bitmap = bitmap)
                }.onFailure { error ->
                    Toast.makeText(
                        context,
                        error.message ?: AppText.t("stats_failed_to_generate_share_image"),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
        )
    }
}

@Composable
private fun SharePreviewDialog(
    bitmap: Bitmap,
    onDismiss: () -> Unit,
    onRegenerate: () -> Unit,
    onShare: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = AppText.t("stats_share_preview"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(onClick = onDismiss) { Text(AppText.t("group_close")) }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(8.dp),
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = AppText.t("stats_report_poster_preview"),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.FillWidth,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = onRegenerate) {
                        Text(AppText.t("stats_regenerate"))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = onDismiss) {
                        Text(AppText.t("group_close"))
                    }
                    Button(onClick = onShare) {
                        Text(AppText.t("group_share"))
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyModeSummaryCard(
    summary: DailyModeSummary,
    icon: ImageVector,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val reportColors = LocalReportColors.current
    val accent =
        when {
            summary.isWarning -> reportColors.warning
            summary.title == AppText.t("stats_encourage_progress") -> reportColors.positive
            else -> MaterialTheme.colorScheme.primary
        }
    val animatedPrimaryValue = animateMetricDisplayText(
        rawText = summary.primaryValue,
        label = "daily_focus_${summary.title}_${summary.primaryLabel}",
        delayMillis = 120,
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 12.dp else 14.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
            ) {
                Surface(
                    modifier = Modifier.size(if (compact) 32.dp else 36.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = accent.copy(alpha = 0.16f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(if (compact) 17.dp else 19.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = summary.title,
                        style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = summary.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (compact) 1 else 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = summary.primaryLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = animatedPrimaryValue,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val metricSize = ((maxWidth - 10.dp) / 2).coerceIn(
                    if (compact) 58.dp else 64.dp,
                    if (compact) 74.dp else 82.dp,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                    ) {
                        FocusProgressRing(
                            progress = summary.progress,
                            color = accent,
                            label = "${(summary.progress * 100f).roundToInt()}%",
                            modifier = Modifier.size(metricSize),
                        )
                        summary.metrics.getOrNull(0)?.let { metric ->
                            FocusMetricPill(
                                metric = metric,
                                accent = accent,
                                delayMillis = 180,
                                modifier = Modifier.size(metricSize),
                                emphasizeValue = true,
                                valueColor = accent,
                            )
                        } ?: Spacer(modifier = Modifier.size(metricSize))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                    ) {
                        summary.metrics.drop(1).take(2).forEachIndexed { index, metric ->
                            FocusMetricPill(
                                metric = metric,
                                accent = accent,
                                delayMillis = 220 + index * 40,
                                modifier = Modifier.size(metricSize),
                                emphasizeValue = true,
                                valueColor = accent,
                            )
                        }
                        repeat((2 - summary.metrics.drop(1).take(2).size).coerceAtLeast(0)) {
                            Spacer(modifier = Modifier.size(metricSize))
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = summary.spotlightLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = summary.spotlightValue,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusProgressRing(
    progress: Float,
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    val animatedProgress = animateFractionValue(
        targetValue = progress.coerceIn(0f, 1f),
        label = "focus_progress_ring_$label",
        delayMillis = 160,
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.14f
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val chartSize = Size(diameter, diameter)
            drawArc(
                color = color.copy(alpha = 0.14f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun FocusMetricPill(
    metric: DailyFocusMetric,
    accent: Color,
    delayMillis: Int,
    modifier: Modifier = Modifier,
    emphasizeValue: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val animatedValue = animateMetricDisplayText(
        rawText = metric.value,
        label = "daily_focus_metric_${metric.label}_${metric.value}",
        delayMillis = delayMillis,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.1f),
    ) {
        if (emphasizeValue) {
            val valueParts = splitMetricValue(animatedValue)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = valueParts.number,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = valueColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (valueParts.unit.isNotBlank()) {
                        Text(
                            text = valueParts.unit,
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = animatedValue,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class MetricValueParts(
    val number: String,
    val unit: String,
)

private fun splitMetricValue(value: String): MetricValueParts {
    val trimmed = value.trim()
    val match = Regex("""^([+\-]?\d+(?:[.,]\d+)?)(.*)$""").find(trimmed)
    return if (match != null) {
        MetricValueParts(
            number = match.groupValues[1],
            unit = match.groupValues[2].trim(),
        )
    } else {
        MetricValueParts(number = trimmed, unit = "")
    }
}

@Composable
private fun HeroMetricChip(
    icon: ImageVector,
    label: String,
    value: String,
    delayMillis: Int = when (icon) {
        Icons.Default.PhoneAndroid -> 80
        Icons.AutoMirrored.Filled.CompareArrows -> 120
        Icons.Default.TouchApp -> 160
        else -> 200
    },
    modifier: Modifier = Modifier,
) {
    val animatedValue = animateMetricDisplayText(
        rawText = value,
        label = "hero_metric_${label.hashCode()}",
        delayMillis = delayMillis,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = animatedValue,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimelineCard(
    selectedTab: ReportTab,
    timelineState: SectionState<TimelineSectionData>,
) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.Timeline,
                title = if (selectedTab == ReportTab.DAY) AppText.t("stats_24_hour_distribution") else AppText.t("stats_archive_trend"),
            )
            when (timelineState) {
                SectionState.Loading -> SkeletonTimelineChart()
                SectionState.Empty -> DailyTimelineChart(emptyList())
                is SectionState.Ready -> DailyTimelineChart(
                    buckets = timelineState.data.buckets,
                    targetMillisPerBucket = timelineState.data.targetMillisPerBucket,
                )
            }
            TimelineFooter(
                labels =
                    buildTimelineFooterLabels(
                        selectedTab = selectedTab,
                        buckets = (timelineState as? SectionState.Ready)?.data?.buckets.orEmpty(),
                    ),
            )
            AdaptiveRowGrid(
                itemCount = if (selectedTab == ReportTab.DAY) 1 else 2,
                compactColumns = 1,
                expandedColumns = 2,
                horizontalSpacing = 14.dp,
                verticalSpacing = 14.dp,
            ) { modifier, index ->
                when (timelineState) {
                    SectionState.Loading -> {
                        SkeletonPeakPanel(modifier = modifier)
                    }
                    SectionState.Empty -> {
                        if (selectedTab == ReportTab.DAY) {
                            PeakMomentsCard(
                                selectedTab = selectedTab,
                                timelineState = null,
                                modifier = modifier,
                            )
                        } else if (index == 0) {
                            PeriodDistributionCard(
                                periodUsage = emptyList(),
                                modifier = modifier,
                            )
                        } else {
                            PeakMomentsCard(
                                selectedTab = selectedTab,
                                timelineState = null,
                                modifier = modifier,
                            )
                        }
                    }
                    is SectionState.Ready -> if (selectedTab == ReportTab.DAY) {
                        PeakMomentsCard(
                            selectedTab = selectedTab,
                            timelineState = timelineState.data,
                            modifier = modifier,
                        )
                    } else when (index) {
                        0 -> PeriodDistributionCard(
                            periodUsage = timelineState.data.periodUsage,
                            modifier = modifier,
                        )
                        else -> PeakMomentsCard(
                            selectedTab = selectedTab,
                            timelineState = timelineState.data,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTimelineChart(
    buckets: List<DailyTimelineBucket>,
    targetMillisPerBucket: Long? = null,
) {
    val deviceColor = MaterialTheme.colorScheme.primary
    val guideLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f)
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val stagedRevealTarget = rememberDelayedFloatTarget(
        targetValue = if (buckets.any { it.deviceMillis > 0L }) 1f else 0f,
        delayMillis = 160,
    )
    val revealProgress by animateFloatAsState(
        targetValue = stagedRevealTarget,
        animationSpec = tween(durationMillis = 720),
        label = "timeline_bar_reveal",
    )
    BoxWithConstraints {
        val chartHeight = if (maxWidth < 360.dp) 138.dp else 156.dp
        val axisWidth = if (maxWidth < 360.dp) 32.dp else 40.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .width(axisWidth)
                    .height(chartHeight),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                val maxUsage = maxOf(
                    buckets.maxOfOrNull { it.deviceMillis }?.coerceAtLeast(1L) ?: 1L,
                    targetMillisPerBucket ?: 0L,
                )
                listOf(maxUsage, maxUsage * 2 / 3, maxUsage / 3, 0L).forEach { tick ->
                    Text(
                        text = if (tick == 0L) "0" else formatAxisDuration(tick),
                        style = MaterialTheme.typography.labelSmall,
                        color = axisTextColor,
                        maxLines = 1,
                    )
                }
            }
            val targetLineColor = LocalReportColors.current.warning.copy(alpha = 0.78f)
            Canvas(modifier = Modifier.weight(1f).height(chartHeight)) {
                if (buckets.isEmpty()) return@Canvas
                val deviceMax = buckets.maxOfOrNull { it.deviceMillis }?.coerceAtLeast(1L) ?: 1L
                val chartMax = maxOf(deviceMax, targetMillisPerBucket ?: 0L, 1L)
                val slotWidth = size.width / buckets.size
                val barWidth = slotWidth * 0.48f
                val baseY = size.height

                repeat(4) { index ->
                    val y = baseY - (index * (size.height / 3f))
                    drawLine(
                        color = guideLineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                    )
                }

                buckets.forEachIndexed { index, bucket ->
                    val x = slotWidth * index + (slotWidth - barWidth) / 2f
                    val rawHeight = size.height * (bucket.deviceMillis.toFloat() / chartMax.toFloat()).coerceIn(0f, 1f)
                    val deviceHeight = if (bucket.deviceMillis > 0L) {
                        maxOf(6f * revealProgress, rawHeight * revealProgress)
                    } else {
                        0f
                    }
                    val top = size.height - deviceHeight
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                deviceColor.copy(alpha = 0.62f),
                                deviceColor.copy(alpha = if (bucket.deviceMillis > 0L) 0.92f else 0.14f),
                            ),
                            startY = top,
                            endY = baseY,
                        ),
                        topLeft = Offset(x, top),
                        size = Size(barWidth, deviceHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                    )
                }
                targetMillisPerBucket?.takeIf { it > 0L }?.let { target ->
                    val targetY = size.height - size.height * (target.toFloat() / chartMax.toFloat()).coerceIn(0f, 1f)
                    val dashWidth = slotWidth * 0.42f
                    var startX = 0f
                    while (startX < size.width) {
                        drawLine(
                            color = targetLineColor,
                            start = Offset(startX, targetY),
                            end = Offset(minOf(startX + dashWidth, size.width), targetY),
                            strokeWidth = 2f,
                        )
                        startX += dashWidth * 1.8f
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineFooter(labels: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun buildTimelineFooterLabels(
    selectedTab: ReportTab,
    buckets: List<DailyTimelineBucket>,
): List<String> {
    if (selectedTab == ReportTab.DAY || buckets.isEmpty()) {
        return listOf("00:00", "06:00", "12:00", "18:00", "24:00")
    }
    val candidateIndexes =
        listOf(
            0,
            buckets.lastIndex / 3,
            (buckets.lastIndex * 2) / 3,
            buckets.lastIndex,
        ).distinct()
    return candidateIndexes.map { buckets[it].label }
}

@Composable
private fun PeriodDistributionCard(
    periodUsage: List<PeriodUsageStat>,
    modifier: Modifier = Modifier,
) {
    val total = periodUsage.sumOf { it.deviceMillis }.coerceAtLeast(1L)
    val dominantIndex = periodUsage.indexOfFirst { it.deviceMillis == (periodUsage.maxOfOrNull { item -> item.deviceMillis } ?: 0L) }
    val dominantItem = dominantIndex.takeIf { it >= 0 }?.let { periodUsage[it] }
    val reportColors = LocalReportColors.current
    val animatedDominantMillis = animateLongValue(
        targetValue = dominantItem?.deviceMillis ?: 0L,
        label = "period_dominant_value",
        durationMillis = 840,
        delayMillis = 240,
    )
    val colors = reportColors.periodPalette
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
    ) {
        BoxWithConstraints {
            val donutSize = if (maxWidth < 360.dp) 148.dp else 170.dp
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = AppText.t("stats_time_heatmap"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PeriodDonutChart(
                            values = periodUsage.map { it.deviceMillis },
                            colors = colors,
                            highlightedIndex = dominantIndex.takeIf { it >= 0 },
                            delayMillis = 200,
                            modifier = Modifier.size(donutSize),
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = dominantItem?.label ?: "--",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = dominantItem?.let { formatDuration(animatedDominantMillis) } ?: "--",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                periodUsage.forEachIndexed { index, item ->
                    val share = item.deviceMillis.toFloat() / total.toFloat()
                    PeriodLegendRow(
                        label = item.label,
                        value = formatDuration(item.deviceMillis),
                        share = share,
                        color = colors[index % colors.size],
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodDonutChart(
    values: List<Long>,
    colors: List<Color>,
    highlightedIndex: Int? = null,
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
) {
    val total = values.sum().coerceAtLeast(1L)
    val stagedRevealTarget = rememberDelayedFloatTarget(
        targetValue = if (values.any { it > 0L }) 1f else 0f,
        delayMillis = delayMillis,
    )
    val revealProgress by animateFloatAsState(
        targetValue = stagedRevealTarget,
        animationSpec = spring(dampingRatio = 0.92f, stiffness = 160f),
        label = "donut_reveal_progress",
    )
    val rotationProgress by animateFloatAsState(
        targetValue = stagedRevealTarget,
        animationSpec = tween(durationMillis = 920),
        label = "donut_rotation_progress",
    )
    Canvas(modifier = modifier) {
        val baseStroke = size.minDimension * 0.13f
        val diameter = size.minDimension - baseStroke
        var startAngle = -90f - (1f - rotationProgress) * 360f
        values.forEachIndexed { index, value ->
            val sweep = 360f * (value.toFloat() / total.toFloat()) * revealProgress
            val isHighlighted = highlightedIndex == index && value > 0L
            val stroke = if (isHighlighted) baseStroke * 1.18f else baseStroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val chartSize = Size(diameter, diameter)

            if (isHighlighted) {
                drawArc(
                    color = colors[index % colors.size].copy(alpha = 0.16f),
                    startAngle = startAngle - 2f,
                    sweepAngle = sweep + 4f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartSize,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke + 10f),
                )
            }
            drawArc(
                color = colors[index % colors.size].copy(alpha = if (value > 0L) 0.92f else 0.12f),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun PeriodLegendRow(
    label: String,
    value: String,
    share: Float,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            modifier = Modifier.width(36.dp),
            style = MaterialTheme.typography.labelMedium,
        )
        LinearProgressIndicator(
            progress = { share.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.14f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun PeakMomentsCard(
    selectedTab: ReportTab,
    timelineState: TimelineSectionData?,
    modifier: Modifier = Modifier,
) {
    val behaviorInsight = timelineState
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = AppText.t("stats_peak_time"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (timelineState == null) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) AppText.t("stats_archived_day_not_enough_peak_samples") else AppText.t("stats_archive_window_not_enough_peak_samples"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AdaptiveRowGrid(
                    itemCount = 3,
                    compactColumns = 1,
                    expandedColumns = 1,
                    verticalSpacing = 8.dp,
                ) { modifier, index ->
                    when (index) {
                        0 -> MiniInsightCard(
                            icon = Icons.Default.Bolt,
                            label = AppText.t("stats_under_1h"),
                            value = "${behaviorInsight.peakHourLabel} · ${formatDuration(behaviorInsight.peakHourMillis)}",
                            modifier = modifier,
                        )
                        1 -> MiniInsightCard(
                            icon = Icons.AutoMirrored.Filled.CallSplit,
                            label = AppText.t("stats_over_2h"),
                            value = "${behaviorInsight.peakTwoHourLabel} · ${formatDuration(behaviorInsight.peakTwoHourMillis)}",
                            modifier = modifier,
                        )
                        else -> MiniInsightCard(
                            icon = Icons.Default.NightsStay,
                            label = AppText.t("stats_night"),
                            value = formatDuration(behaviorInsight.nightUsageMillis),
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberAppChartColors(
    packageNames: List<String>,
): Map<String, Color> {
    val context = LocalContext.current
    val reportColors = LocalReportColors.current
    val stablePackages = remember(packageNames) { packageNames.distinct() }
    val fallbackColors = remember(reportColors) { reportColors.appChartPalette }
    val colors by produceState(
        initialValue = stablePackages.mapIndexed { index, pkg -> pkg to fallbackColors[index % fallbackColors.size] }.toMap(),
        key1 = stablePackages,
    ) {
        value = withContext(Dispatchers.Default) {
            stablePackages.mapIndexed { index, packageName ->
                packageName to extractAppChartColor(context, packageName, fallbackColors[index % fallbackColors.size])
            }.toMap()
        }
    }
    return colors
}

private fun extractAppChartColor(
    context: Context,
    packageName: String,
    fallback: Color,
): Color {
    val drawable = runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
        ?: return fallback
    val bitmap = drawable.toBitmap(width = 128, height = 128, config = Bitmap.Config.ARGB_8888)
    val rgb = extractDominantBitmapColor(bitmap) ?: return fallback
    return normalizeChartColor(Color(rgb), fallback)
}

private fun extractDominantBitmapColor(bitmap: Bitmap): Int? {
    val scaled = Bitmap.createScaledBitmap(bitmap, 40, 40, true)
    val buckets = HashMap<Int, Int>()
    for (x in 0 until scaled.width) {
        for (y in 0 until scaled.height) {
            val pixel = scaled.getPixel(x, y)
            val alpha = android.graphics.Color.alpha(pixel)
            if (alpha < 180) continue

            val red = android.graphics.Color.red(pixel)
            val green = android.graphics.Color.green(pixel)
            val blue = android.graphics.Color.blue(pixel)
            val max = maxOf(red, green, blue)
            val min = minOf(red, green, blue)
            val saturation = if (max == 0) 0f else (max - min).toFloat() / max.toFloat()
            val luminance = (0.2126f * red + 0.7152f * green + 0.0722f * blue) / 255f

            if (luminance < 0.08f || luminance > 0.94f) continue
            if (saturation < 0.12f) continue

            val bucket = ((red shr 4) shl 8) or ((green shr 4) shl 4) or (blue shr 4)
            buckets[bucket] = buckets.getOrDefault(bucket, 0) + 1
        }
    }

    val dominantBucket = buckets.maxByOrNull { it.value }?.key ?: return null
    val red = ((dominantBucket shr 8) and 0xF) * 17
    val green = ((dominantBucket shr 4) and 0xF) * 17
    val blue = (dominantBucket and 0xF) * 17
    return android.graphics.Color.rgb(red, green, blue)
}

private fun normalizeChartColor(
    color: Color,
    fallback: Color,
): Color {
    val luminance = color.luminance()
    return when {
        luminance < 0.08f -> Color(
            red = color.red * 0.55f + fallback.red * 0.45f,
            green = color.green * 0.55f + fallback.green * 0.45f,
            blue = color.blue * 0.55f + fallback.blue * 0.45f,
            alpha = 1f,
        )
        luminance > 0.88f -> Color(
            red = color.red * 0.65f + fallback.red * 0.35f,
            green = color.green * 0.65f + fallback.green * 0.35f,
            blue = color.blue * 0.65f + fallback.blue * 0.35f,
            alpha = 1f,
        )
        else -> color.copy(alpha = 1f)
    }
}

@Composable
private fun fallbackChartColor(index: Int): Color {
    val colors = LocalReportColors.current.appChartPalette
    return colors[index % colors.size]
}

@Composable
private fun TopUsageBarRow(
    rank: Int,
    item: AppDisplayItem,
    maxUsage: Long,
    totalUsage: Long,
    color: Color,
) {
    val isTopRank = rank == 1
    val share = if (totalUsage > 0L) item.value.toFloat() / totalUsage.toFloat() else 0f
    val delayMillis = 300 + ((rank - 1) * 35)
    val animatedDuration = animateLongValue(
        targetValue = item.value,
        label = "top_usage_duration_${item.packageName}",
        durationMillis = 820,
        delayMillis = delayMillis,
    )
    val animatedShare = animateFractionValue(
        targetValue = share,
        label = "top_usage_share_${item.packageName}",
        durationMillis = 760,
        delayMillis = delayMillis + 30,
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isTopRank) color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.36f),
        border = BorderStroke(
            1.dp,
            if (isTopRank) color.copy(alpha = 0.24f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                modifier = Modifier.width(26.dp),
                shape = RoundedCornerShape(999.dp),
                color = color.copy(alpha = if (isTopRank) 0.22f else 0.14f),
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                    )
                }
            }
            AppIconCircle(item.packageName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isTopRank) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                GradientProgressBar(
                    progress = (item.value.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f),
                    color = color,
                    delayMillis = delayMillis,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = formatDuration(animatedDuration),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${(animatedShare * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun GradientProgressBar(
    progress: Float,
    color: Color,
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
) {
    val stagedProgress = rememberDelayedFloatTarget(
        targetValue = progress.coerceIn(0f, 1f),
        delayMillis = delayMillis,
    )
    val animatedProgress by animateFloatAsState(
        targetValue = stagedProgress,
        animationSpec = spring(dampingRatio = 0.92f, stiffness = 220f),
        label = "gradient_progress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.5f),
                            color,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun AppChartsCard(
    selectedTab: ReportTab,
    topAppsState: SectionState<TopAppsSectionData>,
) {
    val usageTopApps = (topAppsState as? SectionState.Ready)?.data?.usageTopApps.orEmpty()
    val appColors = rememberAppChartColors(usageTopApps.map { it.packageName })
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.BarChart,
                title = AppText.t("stats_top_10_apps"),
                subtitle = if (selectedTab == ReportTab.DAY) AppText.t("stats_current_day_top_10_apps_only") else AppText.t("stats_shows_only_the_10_most_used_apps_in"),
            )
            if (topAppsState == SectionState.Loading) {
                SkeletonUsageSharePanel()
                SkeletonRankingPanel()
            } else if (topAppsState == SectionState.Empty || usageTopApps.isEmpty()) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) AppText.t("stats_this_archived_day_does_not_have_enough_usage") else AppText.t("stats_archive_window_not_enough_usage_records"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AppUsageShareCard(
                    items = usageTopApps,
                    appColors = appColors,
                )
            }
        }
    }
}

@Composable
private fun AppUsageShareCard(
    items: List<AppDisplayItem>,
    appColors: Map<String, Color>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
    ) {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            val donutSize = if (compact) 176.dp else 216.dp
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = AppText.t("stats_app_duration"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (items.isEmpty()) {
                    Text(
                        text = AppText.t("stats_no_usage_records"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val total = items.sumOf { it.value }.coerceAtLeast(1L)
                    val visibleItems = items.take(6)
                    val otherUsage = items.drop(6).sumOf { it.value }
                    val donutValues =
                        if (otherUsage > 0L) {
                            visibleItems.map { it.value } + otherUsage
                        } else {
                            visibleItems.map { it.value }
                        }
                    val animatedTotal = animateLongValue(
                        targetValue = total,
                        label = "app_usage_share_total",
                        durationMillis = 860,
                        delayMillis = 260,
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            PeriodDonutChart(
                                values = donutValues,
                                colors =
                                    donutValues.mapIndexed { index, _ ->
                                        visibleItems.getOrNull(index)?.let { appColors[it.packageName] }
                                            ?: fallbackChartColor(index)
                                    },
                                highlightedIndex = 0,
                                delayMillis = 220,
                                modifier = Modifier.size(donutSize),
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = formatDuration(animatedTotal),
                                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = AppText.t("stats_top_10_total_usage"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
                    Text(
                        text = AppText.t("stats_duration_ranking"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    val maxUsage = items.maxOfOrNull { it.value }?.coerceAtLeast(1L) ?: 1L
                    items.forEachIndexed { index, item ->
                        val color = appColors[item.packageName] ?: fallbackChartColor(index)
                        TopUsageBarRow(
                            rank = index + 1,
                            item = item,
                            maxUsage = maxUsage,
                            totalUsage = total,
                            color = color,
                        )
                        if (index != items.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppShareChip(
    share: Float,
    packageName: String,
    color: Color,
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
) {
    val animatedShare = animateFractionValue(
        targetValue = share.coerceIn(0f, 1f),
        label = "app_share_chip_$packageName",
        durationMillis = 780,
        delayMillis = delayMillis,
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box {
                AppIconCircle(packageName)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
            Text(
                text = "${(animatedShare * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TopUsageRankingCard(
    items: List<AppDisplayItem>,
    appColors: Map<String, Color>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = AppText.t("stats_duration_ranking"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            val maxUsage = items.maxOfOrNull { it.value }?.coerceAtLeast(1L) ?: 1L
            val totalUsage = items.sumOf { it.value }.coerceAtLeast(1L)
            items.forEachIndexed { index, item ->
                val color = appColors[item.packageName] ?: fallbackChartColor(index)
                TopUsageBarRow(
                    rank = index + 1,
                    item = item,
                    maxUsage = maxUsage,
                    totalUsage = totalUsage,
                    color = color,
                )
                if (index != items.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
private fun MiniInsightCard(
    icon: ImageVector,
    label: String,
    value: String,
    visualRatio: Float? = null,
    compact: Boolean = false,
    delayMillis: Int = when (icon) {
        Icons.Default.Schedule -> 460
        Icons.Default.AccessTime -> 500
        Icons.Default.TouchApp -> 540
        Icons.Default.RocketLaunch -> 580
        else -> 620
    },
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val animatedValue = animateMetricDisplayText(
        rawText = value,
        label = "mini_insight_${label.hashCode()}",
        delayMillis = delayMillis,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.56f),
    ) {
        if (compact) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = animatedValue,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (visualRatio != null) {
                    GradientProgressBar(
                        progress = visualRatio.coerceIn(0f, 1f),
                        color = accent,
                        delayMillis = delayMillis,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = animatedValue, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (visualRatio != null) {
                    GradientProgressBar(
                        progress = visualRatio.coerceIn(0f, 1f),
                        color = accent,
                        delayMillis = delayMillis,
                    )
                }
            }
        }
    }
}

@Composable
private fun BehaviorCard(
    selectedTab: ReportTab,
    behaviorState: SectionState<BehaviorSectionData>,
) {
    val behaviorInsight = (behaviorState as? SectionState.Ready)?.data?.behaviorInsight
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.Insights,
                title = AppText.t("stats_behavior_analysis"),
                subtitle = if (selectedTab == ReportTab.DAY) AppText.t("stats_archived_data_supported_metrics_only") else AppText.t("stats_only_insights_backed_by_stable_archived_data_are"),
            )
            if (behaviorState == SectionState.Loading) {
                AdaptiveRowGrid(
                    itemCount = 5,
                    compactColumns = 2,
                    expandedColumns = 2,
                ) { modifier, _ ->
                    SkeletonMetricChip(modifier = modifier)
                }
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 12.dp,
                    verticalSpacing = 12.dp,
                ) { modifier, _ ->
                    SkeletonBlock(
                        modifier = modifier,
                        height = 72.dp,
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            } else if (behaviorInsight == null) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) AppText.t("stats_this_archived_day_does_not_have_enough_behavior") else AppText.t("stats_the_current_window_does_not_have_enough_behavior"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val insight = behaviorInsight
                AdaptiveRowGrid(
                    itemCount = 5,
                    compactColumns = 2,
                    expandedColumns = 2,
                ) { modifier, index ->
                    when (index) {
                        0 -> MiniInsightCard(
                            icon = Icons.Default.Schedule,
                            label = AppText.t("stats_label_11"),
                            value = insight.longestSession?.let { session -> "${session.label} · ${formatDuration(session.value)}" } ?: AppText.t("stats_none"),
                            visualRatio = ((insight.longestSession?.value ?: 0L).toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        1 -> MiniInsightCard(
                            icon = Icons.Default.AccessTime,
                            label = AppText.t("stats_average_session"),
                            value = formatDuration(insight.averageSessionMillis),
                            visualRatio = (insight.averageSessionMillis.toFloat() / (30 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        2 -> MiniInsightCard(
                            icon = Icons.Default.Timeline,
                            label = if (selectedTab == ReportTab.DAY) AppText.t("stats_label_9") else AppText.t("stats_label_9"),
                            value = "${insight.peakHourLabel} · ${formatDuration(insight.peakHourMillis)}",
                            visualRatio = (insight.peakHourMillis.toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        3 -> MiniInsightCard(
                            icon = Icons.Default.TouchApp,
                            label = AppText.t("stats_launch_intensity"),
                            value = String.format(Locale.CHINA, AppText.t("stats_launches_per_active_hour_format"), insight.reopenIntensity),
                            visualRatio = (insight.reopenIntensity / 6f).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        4 -> BehaviorRingsCard(
                            activeHourCount = insight.activeHourCount,
                            nightUsageMillis = insight.nightUsageMillis,
                            peakHourMillis = insight.peakHourMillis,
                            modifier = modifier,
                        )
                        else -> Spacer(modifier = modifier)
                    }
                }
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 12.dp,
                    verticalSpacing = 12.dp,
                ) { modifier, index ->
                    when (index) {
                        0 -> BehaviorMomentCard(
                            icon = Icons.Default.NightsStay,
                            title = insight.beforeSleep.label,
                            appLabel = insight.beforeSleep.appLabel ?: AppText.t("stats_no_records_yet"),
                            packageName = insight.beforeSleep.packageName,
                            modifier = modifier,
                        )
                        else -> BehaviorMomentCard(
                            icon = Icons.Default.WbSunny,
                            title = insight.afterWake.label,
                            appLabel = insight.afterWake.appLabel ?: AppText.t("stats_no_records_yet"),
                            packageName = insight.afterWake.packageName,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BehaviorRingsCard(
    activeHourCount: Int,
    nightUsageMillis: Long,
    peakHourMillis: Long,
    modifier: Modifier = Modifier,
) {
    val reportColors = LocalReportColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.56f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompactMetricRing(
                progress = (activeHourCount / 24f).coerceIn(0f, 1f),
                value = "$activeHourCount",
                label = AppText.t("stats_hours"),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            CompactMetricRing(
                progress = if (peakHourMillis > 0L) (nightUsageMillis.toFloat() / (peakHourMillis * 6f)).coerceIn(0f, 1f) else 0f,
                value = formatDuration(nightUsageMillis),
                label = AppText.t("stats_night"),
                color = reportColors.warning,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CompactMetricRing(
    progress: Float,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FocusProgressRing(
            progress = progress,
            color = color,
            label = value,
            modifier = Modifier.size(58.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun BehaviorMomentCard(
    icon: ImageVector,
    title: String,
    appLabel: String,
    packageName: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            if (packageName != null) {
                AppIconCircle(packageName)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = appLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ComparisonCard(
    selectedTab: ReportTab,
    comparisonState: SectionState<ComparisonSectionData>,
) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.AutoMirrored.Filled.CompareArrows,
                title = if (selectedTab == ReportTab.DAY) AppText.t("stats_archive_comparison") else AppText.t("stats_window_comparison"),
                subtitle = if (selectedTab == ReportTab.DAY) AppText.t("stats_compare_current_day_with_previous_archive") else AppText.t("stats_compare_stable_metrics_only"),
            )
            if (comparisonState == SectionState.Loading) {
                repeat(3) { index ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonLine(width = 72.dp, height = 12.dp)
                        SkeletonLine(width = 96.dp, height = 24.dp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonPill(width = 78.dp)
                            SkeletonPill(width = 84.dp)
                        }
                    }
                    if (index != 2) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }
                }
            } else if (comparisonState == SectionState.Empty) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) AppText.t("stats_not_enough_earlier_archive_samples") else AppText.t("stats_this_window_does_not_have_enough_samples_yet"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val comparisons = (comparisonState as SectionState.Ready).data.comparisons
                AdaptiveRowGrid(
                    itemCount = comparisons.size,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 12.dp,
                    verticalSpacing = 12.dp,
                ) { modifier, index ->
                    val item = comparisons[index]
                    ComparisonRow(
                        item = item,
                        delayMillis = 660 + index * 50,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComparisonRow(
    item: ComparisonMetric,
    delayMillis: Int = 0,
    averageBarLabel: String = AppText.t("stats_average"),
    showChips: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val animatedTodayValue = animateMetricDisplayText(
        rawText = item.todayValue,
        label = "comparison_${item.label.hashCode()}",
        delayMillis = delayMillis,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = item.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = animatedTodayValue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item.chartData?.let { data ->
                ComparisonMiniBars(
                    data = data,
                    delayMillis = delayMillis,
                    averageLabel = averageBarLabel,
                )
            }
            if (showChips) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item.yesterdayDelta?.let {
                        ComparisonChip(text = it)
                    }
                    item.averageDelta?.let {
                        ComparisonChip(text = it)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonMiniBars(
    data: ComparisonChartData,
    delayMillis: Int,
    averageLabel: String = AppText.t("stats_average"),
) {
    val values = listOfNotNull(data.previousValue, data.averageValue, data.currentValue)
    val maxValue = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val reportColors = LocalReportColors.current
    val bars =
        listOf(
            Triple(AppText.t("stats_previous"), data.previousValue, data.previousLabel),
            Triple(averageLabel, data.averageValue, data.averageLabel),
            Triple(AppText.t("stats_current"), data.currentValue, data.currentLabel),
        )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        bars.forEachIndexed { index, (label, value, display) ->
            val color =
                when (index) {
                    0 -> MaterialTheme.colorScheme.outline
                    1 -> reportColors.warning
                    else -> MaterialTheme.colorScheme.primary
                }
            ComparisonBar(
                label = label,
                value = value,
                display = display,
                maxValue = maxValue,
                color = color,
                delayMillis = delayMillis + index * 40,
            )
        }
    }
}

@Composable
private fun ComparisonBar(
    label: String,
    value: Long?,
    display: String?,
    maxValue: Long,
    color: Color,
    delayMillis: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (value != null && maxValue > 0L) (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress = animateFractionValue(
        targetValue = progress,
        label = "comparison_bar_${label}_${display.orEmpty()}",
        delayMillis = delayMillis,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.width(30.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(7.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.13f)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(maxOf(0.04f, animatedProgress))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (value == null) 0.12f else 0.72f)),
            )
        }
        Text(
            text = display ?: "--",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(58.dp),
        )
    }
}

@Composable
private fun ComparisonChip(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReportCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun AppIconCircle(pkg: String) {
    val context = LocalContext.current
    val icon = remember(pkg) {
        try {
            context.packageManager.getApplicationIcon(pkg)
        } catch (_: Exception) {
            null
        }
    }
    Surface(
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (icon != null) {
            AsyncImage(
                model = icon,
                contentDescription = null,
                modifier = Modifier.padding(6.dp),
            )
        }
    }
}

private fun shareReportBitmap(
    context: Context,
    bitmap: Bitmap,
) {
    val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
    val file = File(shareDir, "tinyvow-report-${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    val uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, AppText.t("stats_tiny_vow_report"))
            clipData = ClipData.newUri(context.contentResolver, AppText.t("stats_tiny_vow_report"), uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(intent, AppText.t("stats_share_report")))
}

private fun renderShareReportBitmapV2(
    context: Context,
    data: ShareReportData,
    primary: Color,
    surface: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    palette: List<Color>,
): Bitmap {
    val width = 1080
    val height = 2320
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val primaryArgb = primary.toArgb()
    val textArgb = onSurface.toArgb()
    val mutedArgb = onSurfaceVariant.toArgb()
    val positiveArgb = palette.getOrElse(2) { primary }.toArgb()
    val warningArgb = palette.getOrElse(1) { primary }.toArgb()
    val displayTypeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    val titleTypeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    val bodyTypeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)

    drawSharePosterBackgroundV2(context, canvas, width, height, primary)
    canvas.drawRoundRect(
        RectF(50f, 58f, width - 50f, height - 58f),
        58f,
        58f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surface.copy(alpha = 0.94f).toArgb() },
    )

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 54f
        typeface = titleTypeface
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 30f
        typeface = bodyTypeface
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 26f
        typeface = bodyTypeface
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 30f
        typeface = bodyTypeface
    }
    val sectionTitlePaint = Paint(titlePaint).apply { textSize = 36f }

    drawSharePosterIcon(context, canvas, context.packageName, "T", 104f, 106f, 82f, primaryArgb)
    canvas.drawText(data.title, 208f, 142f, titlePaint)
    canvas.drawText(data.subtitle, 208f, 190f, subtitlePaint)
    drawShareStatusPill(
        canvas,
        RectF(width - 320f, 116f, width - 104f, 176f),
        positiveArgb,
        when (data.tab) {
            ReportTab.DAY -> AppText.t("stats_daily_report")
            ReportTab.WEEK -> AppText.t("stats_weekly_report")
            ReportTab.MONTH -> AppText.t("stats_monthly_report")
            ReportTab.YEAR -> AppText.t("stats_yearly_report")
        },
    )
    val sloganRect = RectF(104f, 214f, 620f, 274f)
    canvas.drawRoundRect(
        sloganRect,
        30f,
        30f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.1f).toArgb() },
    )
    canvas.drawText(
        data.slogan,
        sloganRect.left + 24f,
        sloganRect.centerY() + 10f,
        Paint(bodyPaint).apply {
            color = primaryArgb
            textSize = 28f
            typeface = titleTypeface
        },
    )

    val heroRect = RectF(88f, 320f, width - 88f, 660f)
    val softPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.08f).toArgb() }
    canvas.drawRoundRect(heroRect, 44f, 44f, softPaint)
    canvas.drawRoundRect(
        heroRect,
        44f,
        44f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primary.copy(alpha = 0.16f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 2f
        },
    )
    drawShareTransparentAppIcon(context, canvas, heroRect.right - 360f, heroRect.top + 8f, 360f, 22)
    canvas.drawCircle(132f, heroRect.top + 50f, 11f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = positiveArgb })
    canvas.drawText(
        AppText.t("stats_today_status"),
        156f,
        heroRect.top + 60f,
        Paint(bodyPaint).apply {
            color = positiveArgb
            textSize = 28f
            typeface = titleTypeface
        },
    )
    canvas.drawText(data.statusTitle, 126f, heroRect.top + 124f, Paint(titlePaint).apply { textSize = 42f })
    canvas.drawText(
        data.primaryValue,
        126f,
        heroRect.top + 242f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.getOrElse(0) { primary }.copy(alpha = 0.8f).toArgb()
            textSize = 98f
            typeface = displayTypeface
        },
    )
    canvas.drawText(data.primaryLabel, 126f, heroRect.top + 286f, labelPaint)
    val goalDelta = data.goalMillis - data.totalUsageMillis
    val reviewText =
        if (data.goalMillis > 0L) {
            if (goalDelta >= 0L) AppText.t("stats_remaining_value", formatDuration(goalDelta)) else AppText.t("stats_over_by_value", formatDuration(-goalDelta))
        } else {
            data.comparisonLabel
        }
    canvas.drawText(
        reviewText,
        126f,
        heroRect.top + 334f,
        Paint(bodyPaint).apply {
            color = if (data.goalMillis > 0L && goalDelta < 0L) warningArgb else positiveArgb
            textSize = 30f
            typeface = titleTypeface
        },
    )
    drawShareHeroMetric(
        canvas = canvas,
        rect = RectF(heroRect.right - 292f, heroRect.top + 58f, heroRect.right - 38f, heroRect.top + 144f),
        label = data.metrics.getOrNull(0)?.label ?: AppText.t("stats_label_5"),
        value = data.metrics.getOrNull(0)?.value ?: AppText.t("stats_none"),
        accent = primaryArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )
    drawShareHeroMetric(
        canvas = canvas,
        rect = RectF(heroRect.right - 292f, heroRect.top + 160f, heroRect.right - 38f, heroRect.top + 246f),
        label = data.metrics.getOrNull(1)?.label ?: AppText.t("stats_net_points"),
        value = data.metrics.getOrNull(1)?.value ?: AppText.t("stats_none"),
        accent = warningArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )
    drawShareHeroMetric(
        canvas = canvas,
        rect = RectF(heroRect.right - 292f, heroRect.top + 262f, heroRect.right - 38f, heroRect.top + 348f),
        label = AppText.t("stats_top_apps"),
        value = data.topApps.firstOrNull()?.label ?: AppText.t("stats_none"),
        accent = positiveArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )

    drawShareFocusCards(
        canvas = canvas,
        data = data,
        left = 88f,
        top = 716f,
        width = width - 176f,
        primaryArgb = primaryArgb,
        positiveArgb = positiveArgb,
        warningArgb = warningArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
        surfaceArgb = android.graphics.Color.argb(226, 255, 255, 255),
    )
    drawShareTimelineSection(
        canvas = canvas,
        rect = RectF(88f, 994f, width - 88f, 1388f),
        data = data,
        primary = primary,
        warningArgb = warningArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )
    drawShareTopAppsSection(
        context = context,
        canvas = canvas,
        rect = RectF(88f, 1424f, width - 88f, 1900f),
        apps = data.topApps,
        palette = palette,
        primary = primary,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )
    val summaryRect = RectF(88f, 1940f, width - 88f, 2188f)
    canvas.drawRoundRect(summaryRect, 36f, 36f, softPaint)
    canvas.drawText(AppText.t("stats_share_review_sentence"), 124f, summaryRect.top + 58f, sectionTitlePaint)
    canvas.drawText(
        data.subtitle,
        124f,
        summaryRect.top + 100f,
        Paint(subtitlePaint).apply { textSize = 26f },
    )
    drawMultilineText(
        canvas,
        buildSharePosterInsight(data),
        124f,
        summaryRect.top + 154f,
        summaryRect.width() - 72f,
        Paint(bodyPaint).apply { textSize = 30f },
        40f,
        3,
    )
    canvas.drawText(AppText.t("stats_share_footer"), 104f, height - 100f, Paint(subtitlePaint).apply { textSize = 28f })
    return bitmap
}

private fun drawShareHeroMetric(
    canvas: android.graphics.Canvas,
    rect: RectF,
    label: String,
    value: String,
    accent: Int,
    textArgb: Int,
    mutedArgb: Int,
) {
    canvas.drawRoundRect(
        rect,
        24f,
        24f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(
                24,
                android.graphics.Color.red(accent),
                android.graphics.Color.green(accent),
                android.graphics.Color.blue(accent),
            )
        },
    )
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 22f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 26f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    canvas.drawText(label, rect.left + 18f, rect.top + 30f, labelPaint)
    drawEllipsizedText(canvas, value, rect.left + 18f, rect.top + 66f, rect.width() - 36f, valuePaint)
}

private fun drawShareTimelineSection(
    canvas: android.graphics.Canvas,
    rect: RectF,
    data: ShareReportData,
    primary: Color,
    warningArgb: Int,
    textArgb: Int,
    mutedArgb: Int,
) {
    canvas.drawRoundRect(
        rect,
        40f,
        40f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.08f).toArgb() },
    )
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 34f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 22f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    canvas.drawText(
        if (data.tab == ReportTab.DAY) AppText.t("stats_24_hour_distribution") else AppText.t("stats_archive_trend"),
        rect.left + 36f,
        rect.top + 54f,
        titlePaint,
    )
    canvas.drawText(data.comparisonLabel, rect.left + 36f, rect.top + 88f, labelPaint)

    val chartLeft = rect.left + 36f
    val chartTop = rect.top + 126f
    val chartRight = rect.right - 36f
    val chartBottom = rect.top + 286f
    val values =
        when {
            data.tab == ReportTab.DAY && data.hourlyUsageMillis.isNotEmpty() -> data.hourlyUsageMillis.take(24)
            data.hourlyUsageMillis.isNotEmpty() -> data.hourlyUsageMillis
            else -> data.trendUsageMillis
        }.ifEmpty { listOf(0L, 0L, 0L, 0L) }
    val maxValue = maxOf(values.maxOrNull() ?: 0L, data.targetMillisPerBucket ?: 0L, 1L)
    val slotWidth = (chartRight - chartLeft) / values.size.toFloat()
    val barWidth = slotWidth * 0.54f
    val chartHeight = chartBottom - chartTop
    val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(28, android.graphics.Color.red(textArgb), android.graphics.Color.green(textArgb), android.graphics.Color.blue(textArgb))
        strokeWidth = 2f
    }
    repeat(4) { index ->
        val y = chartBottom - chartHeight * (index / 3f)
        canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
    }
    values.forEachIndexed { index, value ->
        val left = chartLeft + slotWidth * index + (slotWidth - barWidth) / 2f
        val heightRatio = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
        val top = chartBottom - chartHeight * heightRatio
        canvas.drawRoundRect(
            RectF(left, top, left + barWidth, chartBottom),
            barWidth / 2f,
            barWidth / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = android.graphics.LinearGradient(
                    left,
                    top,
                    left,
                    chartBottom,
                    primary.copy(alpha = 0.55f).toArgb(),
                    primary.toArgb(),
                    android.graphics.Shader.TileMode.CLAMP,
                )
            },
        )
    }
    data.targetMillisPerBucket?.takeIf { data.tab == ReportTab.DAY && it > 0L }?.let { target ->
        val y = chartBottom - chartHeight * (target.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
        canvas.drawLine(chartLeft, y, chartRight, y, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = warningArgb
            strokeWidth = 3f
        })
    }
    val footerLabels =
        if (data.tab == ReportTab.DAY) {
            listOf("00:00", "06:00", "12:00", "18:00", "24:00")
        } else {
            buildShareTimelineLabels(data.timelineLabels.ifEmpty { data.trendUsageMillis.indices.map { (it + 1).toString() } })
        }
    footerLabels.forEachIndexed { index, label ->
        val x = chartLeft + (chartRight - chartLeft) * (index / (footerLabels.size - 1).toFloat())
        val widthHalf = Paint(labelPaint).measureText(label) / 2f
        canvas.drawText(label, x - widthHalf, chartBottom + 34f, labelPaint)
    }
    val summaryTop = rect.bottom - 72f
    val metricPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 24f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val thirds = (rect.width() - 72f) / 3f
    val items = listOf(
        AppText.t("stats_peak_time") to buildSharePeakValue(data),
        AppText.t("stats_night_use") to formatDuration(data.nightUsageMillis),
        AppText.t("stats_target_complete") to buildShareGoalValue(data),
    )
    items.forEachIndexed { index, (label, value) ->
        val startX = rect.left + 24f + thirds * index
        canvas.drawText(label, startX, summaryTop, labelPaint)
        drawEllipsizedText(canvas, value, startX, summaryTop + 34f, thirds - 16f, metricPaint)
    }
}

private fun buildShareTimelineLabels(labels: List<String>): List<String> {
    if (labels.isEmpty()) return listOf("1", "2", "3", "4")
    if (labels.size <= 4) return labels
    val indexes = listOf(0, labels.lastIndex / 3, (labels.lastIndex * 2) / 3, labels.lastIndex).distinct()
    return indexes.map { labels[it] }
}

private fun buildSharePeakValue(data: ShareReportData): String {
    if (data.hourlyUsageMillis.isEmpty()) return AppText.t("stats_none")
    val index = data.hourlyUsageMillis.indices.maxByOrNull { data.hourlyUsageMillis[it] } ?: return AppText.t("stats_none")
    val label = data.timelineLabels.getOrNull(index) ?: if (data.tab == ReportTab.DAY) dayHourLabel(index) else (index + 1).toString()
    return "$label · ${formatDuration(data.hourlyUsageMillis.getOrElse(index) { 0L })}"
}

private fun buildShareGoalValue(data: ShareReportData): String {
    return if (data.goalProgress != null) {
        "${(data.goalProgress.coerceIn(0f, 1f) * 100f).roundToInt()}%"
    } else {
        data.comparisonLabel
    }
}

private fun drawShareTopAppsSection(
    context: Context,
    canvas: android.graphics.Canvas,
    rect: RectF,
    apps: List<AppDisplayItem>,
    palette: List<Color>,
    primary: Color,
    textArgb: Int,
    mutedArgb: Int,
) {
    canvas.drawRoundRect(
        rect,
        40f,
        40f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.08f).toArgb() },
    )
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 34f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 26f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 22f
    }
    canvas.drawText(AppText.t("stats_top_10_apps"), rect.left + 36f, rect.top + 54f, titlePaint)
    canvas.drawText(AppText.t("stats_current_day_top_10_apps_only"), rect.left + 36f, rect.top + 88f, labelPaint)
    val displayApps = apps.take(4)
    val maxUsage = displayApps.maxOfOrNull { it.value }?.coerceAtLeast(1L) ?: 1L
    displayApps.forEachIndexed { index, app ->
        val rowTop = rect.top + 126f + index * 82f
        val fallbackColor = palette.getOrNull(index) ?: primary
        val appColor = extractAppChartColor(context, app.packageName, fallbackColor)
        drawSharePosterIcon(context, canvas, app.packageName, app.label.take(1), rect.left + 36f, rowTop, 46f, appColor.toArgb())
        canvas.drawText("${index + 1}", rect.left + 6f, rowTop + 28f, Paint(labelPaint).apply {
            this.color = appColor.toArgb()
            textSize = 24f
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        })
        drawEllipsizedText(canvas, app.label, rect.left + 96f, rowTop + 24f, 360f, bodyPaint)
        canvas.drawText(formatDuration(app.value), rect.right - 40f, rowTop + 24f, Paint(bodyPaint).apply {
            textAlign = Paint.Align.RIGHT
        })
        val barTop = rowTop + 42f
        canvas.drawRoundRect(
            RectF(rect.left + 96f, barTop, rect.right - 40f, barTop + 12f),
            8f,
            8f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = appColor.copy(alpha = 0.14f).toArgb() },
        )
        canvas.drawRoundRect(
            RectF(
                rect.left + 96f,
                barTop,
                rect.left + 96f + (rect.right - rect.left - 136f) * (app.value.toFloat() / maxUsage.toFloat()).coerceIn(0.06f, 1f),
                barTop + 12f,
            ),
            8f,
            8f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = appColor.toArgb() },
        )
    }
}

private fun drawSharePosterBackgroundV2(
    context: Context,
    canvas: android.graphics.Canvas,
    width: Int,
    height: Int,
    primary: Color,
) {
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader =
            android.graphics.LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                intArrayOf(
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.10f),
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.18f),
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.26f),
                ),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    canvas.drawOval(
        RectF(660f, -80f, 1240f, 430f),
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.18f).toArgb() },
    )
    canvas.drawOval(
        RectF(-180f, 1320f, 440f, 2020f),
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.12f).toArgb() },
    )
    canvas.drawOval(
        RectF(500f, 1540f, 1120f, 2180f),
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.12f).toArgb() },
    )
    drawShareTransparentAppIcon(context, canvas, 612f, 255f, 560f, 18)
    drawShareTransparentAppIcon(context, canvas, 612f, 1180f, 520f, 14)
}

private fun drawShareTransparentAppIcon(
    context: Context,
    canvas: android.graphics.Canvas,
    left: Float,
    top: Float,
    size: Float,
    alpha: Int,
) {
    val rect = RectF(left, top, left + size, top + size)
    val iconBitmap =
        runCatching {
            context.packageManager.getApplicationIcon(context.packageName).toBitmap(
                width = size.roundToInt(),
                height = size.roundToInt(),
                config = Bitmap.Config.ARGB_8888,
            )
        }.getOrNull()
    if (iconBitmap != null) {
        canvas.drawBitmap(
            iconBitmap,
            null,
            rect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.alpha = alpha },
        )
    } else {
        canvas.drawCircle(
            rect.centerX(),
            rect.centerY(),
            size / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(alpha, 34, 174, 118) },
        )
    }
}

private fun drawShareStatusPill(
    canvas: android.graphics.Canvas,
    rect: RectF,
    accent: Int,
    text: String,
) {
    canvas.drawRoundRect(
        rect,
        28f,
        28f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(24, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
        },
    )
    val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    val checkPath = android.graphics.Path().apply {
        moveTo(rect.left + 30f, rect.centerY() + 2f)
        lineTo(rect.left + 42f, rect.centerY() + 14f)
        lineTo(rect.left + 62f, rect.centerY() - 10f)
    }
    canvas.drawPath(checkPath, checkPaint)
    canvas.drawText(
        text,
        rect.left + 76f,
        rect.centerY() + 12f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            textSize = 28f
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        },
    )
}

private fun drawShareAppConsumption(
    context: Context,
    canvas: android.graphics.Canvas,
    apps: List<AppDisplayItem>,
    centerX: Float,
    centerY: Float,
    radius: Float,
    palette: List<Color>,
    textArgb: Int,
    mutedArgb: Int,
    primary: Color,
) {
    val displayApps = apps.take(5)
    val total = displayApps.sumOf { it.value }.coerceAtLeast(1L)
    val colors =
        displayApps.mapIndexed { index, app ->
            extractAppChartColor(context, app.packageName, palette.getOrNull(index) ?: primary)
        }
    val chartRect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    val stroke = 46f
    canvas.drawArc(
        chartRect,
        -92f,
        360f,
        false,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primary.copy(alpha = 0.08f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.BUTT
        },
    )
    var start = -92f
    displayApps.forEachIndexed { index, app ->
        val sweep = app.value.toFloat() / total.toFloat() * 360f
        canvas.drawArc(
            chartRect,
            start,
            (sweep - 9f).coerceAtLeast(5f),
            false,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors[index].toArgb()
                style = Paint.Style.STROKE
                strokeWidth = stroke
                strokeCap = Paint.Cap.BUTT
            },
        )
        start += sweep
    }
    val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 42f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    }
    val centerLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 28f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    canvas.drawText(if (displayApps.isEmpty()) "--" else formatDuration(total), centerX, centerY - 4f, centerPaint)
    canvas.drawText("Top ${displayApps.size.coerceAtLeast(1)}", centerX, centerY + 48f, centerLabelPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 36f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    canvas.drawText(AppText.t("stats_app_usage"), 552f, centerY - 164f, titlePaint)
    canvas.drawText(
        AppText.t("stats_share"),
        930f,
        centerY - 164f,
        Paint(titlePaint).apply {
            color = mutedArgb
            textSize = 29f
        },
    )
    if (displayApps.isEmpty()) {
        canvas.drawText(AppText.t("stats_no_app_details_yet"), 552f, centerY, Paint(titlePaint).apply { textSize = 30f })
        return
    }
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 28f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 26f
        textAlign = Paint.Align.RIGHT
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val maxValue = displayApps.maxOf { it.value }.coerceAtLeast(1L)
    displayApps.forEachIndexed { index, app ->
        val y = centerY - 126f + index * 66f
        val color = colors[index]
        canvas.drawCircle(558f, y + 23f, 7f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() })
        drawSharePosterIcon(context, canvas, app.packageName, app.label.take(1), 580f, y, 42f, color.toArgb())
        drawEllipsizedText(canvas, app.label, 640f, y + 26f, 220f, namePaint)
        val percent = (app.value.toFloat() / total.toFloat() * 100f).roundToInt()
        canvas.drawText("${formatDuration(app.value)} · $percent%", 976f, y + 27f, valuePaint)
        canvas.drawRoundRect(
            RectF(640f, y + 38f, 836f, y + 49f),
            7f,
            7f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.argb(26, 126, 142, 158) },
        )
        canvas.drawRoundRect(
            RectF(640f, y + 38f, 640f + 196f * (app.value.toFloat() / maxValue.toFloat()).coerceIn(0.06f, 1f), y + 49f),
            7f,
            7f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() },
        )
    }
}

private fun drawShareWeeklyTrend(
    canvas: android.graphics.Canvas,
    values: List<Long>,
    rect: RectF,
    textArgb: Int,
    mutedArgb: Int,
    positiveArgb: Int,
    warningArgb: Int,
    comparisonLabel: String,
) {
    canvas.drawRoundRect(
        rect,
        32f,
        32f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(188, 255, 255, 255) },
    )
    canvas.drawRoundRect(
        rect,
        32f,
        32f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(26, 126, 142, 158)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        },
    )
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 34f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    canvas.drawText(AppText.t("stats_weekly_screen_rhythm"), rect.left + 34f, rect.top + 60f, titlePaint)
    canvas.drawText(
        comparisonLabel,
        rect.right - 34f,
        rect.top + 60f,
        Paint(titlePaint).apply {
            color = if (comparisonLabel.contains("+") || comparisonLabel.contains(AppText.t("stats_up"))) warningArgb else positiveArgb
            textSize = 26f
            textAlign = Paint.Align.RIGHT
        },
    )
    val trend = values.takeLast(7).let { if (it.size < 7) List(7 - it.size) { 0L } + it else it }
    val maxValue = trend.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val chartLeft = rect.left + 34f
    val chartRight = rect.right - 34f
    val chartTop = rect.top + 102f
    val chartBottom = rect.bottom - 62f
    val points =
        trend.mapIndexed { index, value ->
            val x = chartLeft + (chartRight - chartLeft) * index / 6f
            val y = chartBottom - (chartBottom - chartTop) * (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
            x to y
        }
    val linePath = android.graphics.Path()
    points.forEachIndexed { index, point ->
        if (index == 0) {
            linePath.moveTo(point.first, point.second)
        } else {
            val previous = points[index - 1]
            val midX = (previous.first + point.first) / 2f
            linePath.cubicTo(midX, previous.second, midX, point.second, point.first, point.second)
        }
    }
    val fillPath = android.graphics.Path(linePath).apply {
        lineTo(chartRight, chartBottom)
        lineTo(chartLeft, chartBottom)
        close()
    }
    canvas.drawPath(
        fillPath,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader =
                android.graphics.LinearGradient(
                    0f,
                    chartTop,
                    0f,
                    chartBottom,
                    intArrayOf(android.graphics.Color.argb(58, 34, 174, 118), android.graphics.Color.argb(0, 34, 174, 118)),
                    null,
                    android.graphics.Shader.TileMode.CLAMP,
                )
        },
    )
    canvas.drawPath(
        linePath,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = positiveArgb
            strokeWidth = 7f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        },
    )
    points.drop(1).forEachIndexed { index, point ->
        if (index % 2 == 0 || index == points.size - 2) {
            canvas.drawCircle(point.first, point.second, 13f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE })
            canvas.drawCircle(point.first, point.second, 8f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = positiveArgb })
        }
    }
    val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 25f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    listOf(AppText.t("stats_mon"), AppText.t("stats_tue"), AppText.t("stats_wed"), AppText.t("stats_thu"), AppText.t("stats_fri"), AppText.t("stats_sat"), AppText.t("stats_sun")).forEachIndexed { index, label ->
        val x = chartLeft + (chartRight - chartLeft) * index / 6f
        canvas.drawText(label, x, rect.bottom - 28f, dayPaint)
    }
}

private fun renderShareReportBitmap(
    context: Context,
    data: ShareReportData,
    primary: Color,
    surface: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    palette: List<Color>,
): Bitmap {
    val width = 1080
    val height = 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val primaryArgb = primary.toArgb()
    val textArgb = onSurface.toArgb()
    val mutedArgb = onSurfaceVariant.toArgb()
    val displayTypeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    val titleTypeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    val bodyTypeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader =
            android.graphics.LinearGradient(
                0f,
                0f,
                0f,
                height.toFloat(),
                intArrayOf(
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.24f),
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.10f),
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.20f),
                ),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    drawSharePosterBackground(canvas, width, height, primary)

    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(238, 255, 255, 255) }
    val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(190, 255, 255, 255) }
    val softPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.11f).toArgb() }
    val warningArgb = palette.getOrElse(1) { primary }.toArgb()
    val positiveArgb = palette.getOrElse(2) { primary }.toArgb()
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 52f
        typeface = titleTypeface
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 29f
        typeface = bodyTypeface
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 27f
        typeface = bodyTypeface
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryArgb
        textSize = 118f
        typeface = displayTypeface
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 32f
        typeface = bodyTypeface
    }

    canvas.drawRoundRect(RectF(50f, 58f, width - 50f, height - 58f), 58f, 58f, cardPaint)

    drawSharePosterIcon(
        context = context,
        canvas = canvas,
        packageName = context.packageName,
        label = "T",
        left = 100f,
        top = 112f,
        size = 70f,
        fallbackColor = primaryArgb,
    )
    canvas.drawText(data.title, 192f, 145f, titlePaint)
    canvas.drawText(data.subtitle, 192f, 188f, subtitlePaint)

    val heroTop = 242f
    canvas.drawRoundRect(RectF(86f, heroTop, width - 86f, 598f), 46f, 46f, softPaint)
    canvas.drawText(data.statusTitle, 126f, heroTop + 70f, Paint(titlePaint).apply { textSize = 46f })
    canvas.drawText(data.primaryLabel, 126f, heroTop + 130f, labelPaint)
    canvas.drawText(data.primaryValue, 126f, heroTop + 250f, valuePaint)
    val goalText =
        if (data.goalMillis > 0L) {
            val delta = data.goalMillis - data.totalUsageMillis
            if (delta >= 0L) AppText.t("stats_within_target_value_left", formatDuration(delta)) else AppText.t("stats_over_by_value_3", formatDuration(-delta))
        } else {
            data.comparisonLabel
        }
    canvas.drawText(goalText, 126f, heroTop + 310f, Paint(bodyPaint).apply {
        color = if (data.goalMillis > 0L && data.totalUsageMillis > data.goalMillis) warningArgb else positiveArgb
        typeface = titleTypeface
    })

    val progressLeft = 610f
    val progressTop = heroTop + 266f
    val progressWidth = 300f
    val progressHeight = 28f
    val goalBase = data.goalMillis.takeIf { it > 0L } ?: max(data.totalUsageMillis, 1L)
    val progress = (data.totalUsageMillis.toFloat() / goalBase.toFloat()).coerceIn(0f, 1.15f)
    val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.15f).toArgb() }
    canvas.drawRoundRect(
        RectF(progressLeft, progressTop, progressLeft + progressWidth, progressTop + progressHeight),
        18f,
        18f,
        trackPaint,
    )
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader =
            android.graphics.LinearGradient(
                progressLeft,
                progressTop,
                progressLeft + progressWidth,
                progressTop,
                intArrayOf(primary.copy(alpha = 0.78f).toArgb(), if (progress > 1f) warningArgb else primaryArgb),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
    }
    canvas.drawRoundRect(
        RectF(progressLeft, progressTop, progressLeft + progressWidth * progress.coerceIn(0f, 1f), progressTop + progressHeight),
        18f,
        18f,
        fillPaint,
    )
    canvas.drawLine(
        progressLeft + progressWidth,
        progressTop - 9f,
        progressLeft + progressWidth,
        progressTop + progressHeight + 9f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (progress > 1f) warningArgb else primaryArgb
            strokeWidth = 4f
        },
    )
    canvas.drawText(
        if (data.goalMillis > 0L) AppText.t("stats_target_value", formatDuration(data.goalMillis)) else AppText.t("stats_usage_progress"),
        progressLeft,
        progressTop + 68f,
        labelPaint,
    )
    canvas.drawText(
        "${(progress * 100f).roundToInt()}%",
        progressLeft + 250f,
        progressTop + 68f,
        Paint(bodyPaint).apply {
            color = if (progress > 1f) warningArgb else primaryArgb
            typeface = displayTypeface
        },
    )

    drawShareFocusCards(
        canvas = canvas,
        data = data,
        left = 86f,
        top = 638f,
        width = width - 172f,
        primaryArgb = primaryArgb,
        positiveArgb = positiveArgb,
        warningArgb = warningArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
        surfaceArgb = android.graphics.Color.argb(226, 255, 255, 255),
    )

    val sectionTitlePaint = Paint(titlePaint).apply { textSize = 40f }
    val distributionTop = 930f
    canvas.drawRoundRect(RectF(86f, distributionTop - 28f, width - 86f, distributionTop + 406f), 42f, 42f, glassPaint)
    drawShareAppDistribution(
        context = context,
        canvas = canvas,
        apps = data.topApps,
        centerX = 344f,
        centerY = distributionTop + 200f,
        radius = 188f,
        palette = palette,
        textColor = textArgb,
        mutedColor = mutedArgb,
        primaryColor = primary,
    )

    val insightTop = 1510f
    canvas.drawRoundRect(RectF(86f, insightTop - 54f, width - 86f, insightTop + 134f), 34f, 34f, softPaint)
    canvas.drawText(AppText.t("stats_review_sentence"), 126f, insightTop, Paint(sectionTitlePaint).apply { textSize = 34f })
    val posterInsight = buildSharePosterInsight(data)
    drawMultilineText(canvas, posterInsight, 126f, insightTop + 50f, width - 252f, Paint(bodyPaint).apply { textSize = 29f }, 40f, 2)

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 28f
        typeface = bodyTypeface
    }
    canvas.drawText(AppText.t("stats_share_footer"), 104f, height - 88f, footerPaint)
    return bitmap
}

private fun drawSharePosterBackground(
    canvas: android.graphics.Canvas,
    width: Int,
    height: Int,
    primary: Color,
) {
    val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(118, 255, 255, 255) }
    listOf(
        RectF(-80f, 250f, 360f, 430f),
        RectF(720f, 300f, 1160f, 470f),
        RectF(40f, 1500f, 520f, 1700f),
    ).forEach { rect ->
        canvas.drawOval(rect, cloudPaint)
    }
    val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primary.copy(alpha = 0.12f).toArgb()
        style = Paint.Style.FILL
    }
    val path = android.graphics.Path().apply {
        moveTo(0f, height * 0.72f)
        lineTo(width * 0.24f, height * 0.62f)
        lineTo(width * 0.48f, height * 0.70f)
        lineTo(width * 0.72f, height * 0.60f)
        lineTo(width.toFloat(), height * 0.69f)
        lineTo(width.toFloat(), height.toFloat())
        lineTo(0f, height.toFloat())
        close()
    }
    canvas.drawPath(path, mountainPaint)
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.11f).toArgb() }
    for (row in 0 until 8) {
        for (col in 0 until 6) {
            canvas.drawCircle(720f + col * 48f, 112f + row * 38f, 3.2f, dotPaint)
        }
    }
}

private fun blendPosterColor(foreground: Int, background: Int, ratio: Float): Int {
    val clamped = ratio.coerceIn(0f, 1f)
    val inverse = 1f - clamped
    return android.graphics.Color.rgb(
        (android.graphics.Color.red(foreground) * clamped + android.graphics.Color.red(background) * inverse).toInt(),
        (android.graphics.Color.green(foreground) * clamped + android.graphics.Color.green(background) * inverse).toInt(),
        (android.graphics.Color.blue(foreground) * clamped + android.graphics.Color.blue(background) * inverse).toInt(),
    )
}

private fun drawShareAppDistribution(
    context: Context,
    canvas: android.graphics.Canvas,
    apps: List<AppDisplayItem>,
    centerX: Float,
    centerY: Float,
    radius: Float,
    palette: List<Color>,
    textColor: Int,
    mutedColor: Int,
    primaryColor: Color,
) {
    val total = apps.sumOf { it.value }.coerceAtLeast(1L)
    val displayApps = apps.take(5)
    val values =
        if (displayApps.isEmpty()) {
            listOf(1L)
        } else {
            displayApps.map { it.value.coerceAtLeast(1L) }
        }
    val colors =
        values.mapIndexed { index, _ ->
            displayApps.getOrNull(index)?.let { app ->
                extractAppChartColor(
                    context = context,
                    packageName = app.packageName,
                    fallback = palette.getOrNull(index) ?: primaryColor,
                )
            } ?: palette.getOrNull(index) ?: primaryColor
        }
    val stroke = 42f
    val chartRect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor.copy(alpha = 0.08f).toArgb()
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawArc(chartRect, 138f, 264f, false, basePaint)
    var startAngle = 138f
    values.forEachIndexed { index, value ->
        val sweep = (value.toFloat() / values.sum().toFloat()) * 264f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors[index].toArgb()
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.BUTT
        }
        canvas.drawArc(chartRect, startAngle, (sweep - 4f).coerceAtLeast(3f), false, paint)
        startAngle += sweep
    }
    val centerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 32f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val centerLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedColor
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    val topUsage = displayApps.sumOf { it.value }
    canvas.drawText(if (topUsage > 0L) formatDuration(topUsage) else "--", centerX, centerY - 8f, centerTitlePaint)
    canvas.drawText("Top ${displayApps.size.coerceAtLeast(1)}", centerX, centerY + 34f, centerLabelPaint)

    val rowLeft = centerX + radius + 92f
    val rowTop = centerY - 166f
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 27f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedColor
        textSize = 23f
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedColor
        textSize = 23f
        textAlign = Paint.Align.RIGHT
    }
    if (displayApps.isEmpty()) {
        canvas.drawText(AppText.t("stats_no_app_details_yet"), rowLeft, centerY, namePaint)
        return
    }
    displayApps.forEachIndexed { index, app ->
        val y = rowTop + index * 72f
        val color = colors[index]
        val barLeft = rowLeft + 58f
        val barRight = rowLeft + 310f
        val barTop = y + 38f
        drawSharePosterIcon(
            context = context,
            canvas = canvas,
            packageName = app.packageName,
            label = app.label.take(1),
            left = rowLeft,
            top = y,
            size = 44f,
            fallbackColor = color.toArgb(),
        )
        drawEllipsizedText(canvas, app.label, rowLeft + 58f, y + 25f, 150f, namePaint)
        val percent = ((app.value.toFloat() / total.toFloat()) * 100f).roundToInt()
        canvas.drawText("${formatDuration(app.value)} · $percent%", rowLeft + 322f, y + 25f, valuePaint)
        canvas.drawRoundRect(
            RectF(barLeft, barTop, barRight, barTop + 11f),
            8f,
            8f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.copy(alpha = 0.13f).toArgb() },
        )
        canvas.drawRoundRect(
            RectF(
                barLeft,
                barTop,
                barLeft + (barRight - barLeft) * (app.value.toFloat() / displayApps.maxOf { it.value }.coerceAtLeast(1L).toFloat()).coerceIn(0.04f, 1f),
                barTop + 11f,
            ),
            8f,
            8f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() },
        )
        canvas.drawCircle(rowLeft - 24f, y + 23f, 8f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() })
    }
}

private fun drawShareFocusCards(
    canvas: android.graphics.Canvas,
    data: ShareReportData,
    left: Float,
    top: Float,
    width: Float,
    primaryArgb: Int,
    positiveArgb: Int,
    warningArgb: Int,
    textArgb: Int,
    mutedArgb: Int,
    surfaceArgb: Int,
) {
    val gap = 28f
    val cardWidth = (width - gap) / 2f
    val cardHeight = 236f
    drawShareFocusCard(
        canvas = canvas,
        rect = RectF(left, top, left + cardWidth, top + cardHeight),
        title = AppText.t("stats_control_results"),
        primaryLabel = AppText.t("stats_time_saved"),
        primaryValue = formatDuration(data.savedMillis),
        accent = if (data.blockEventCount > 0) warningArgb else positiveArgb,
        progress = data.goalProgress ?: if (data.controlExceededGroupCount == 0) 1f else 0f,
        metrics =
            listOf(
                DailyFocusMetric(AppText.t("stats_met"), AppText.t("stats_value_groups_3", data.controlCompletedGroupCount)),
                DailyFocusMetric(AppText.t("stats_over_limit"), AppText.t("stats_value_groups_6", data.controlExceededGroupCount)),
                DailyFocusMetric(AppText.t("group_blocks"), AppText.t("stats_value_times_13", data.blockEventCount)),
            ),
        textArgb = textArgb,
        mutedArgb = mutedArgb,
        surfaceArgb = surfaceArgb,
    )
    drawShareFocusCard(
        canvas = canvas,
        rect = RectF(left + cardWidth + gap, top, left + width, top + cardHeight),
        title = AppText.t("stats_encourage_progress"),
        primaryLabel = AppText.t("stats_net_points"),
        primaryValue = formatSignedPointsLocal(data.pointsNet),
        accent = if (data.pointsNet >= 0.0) positiveArgb else warningArgb,
        progress = if (data.encourageCompletedGroupCount > 0 || data.redemptionCount > 0) {
            data.encourageCompletedGroupCount.toFloat() /
                (data.encourageCompletedGroupCount + data.redemptionCount).coerceAtLeast(1).toFloat()
        } else {
            0f
        },
        metrics =
            listOf(
                DailyFocusMetric(AppText.t("stats_duration"), formatDuration(data.encourageUsageMillis)),
                DailyFocusMetric(AppText.t("stats_met"), AppText.t("stats_value_groups", data.encourageCompletedGroupCount)),
                DailyFocusMetric(AppText.t("stats_redemption"), AppText.t("stats_value_times_10", data.redemptionCount)),
            ),
        textArgb = textArgb,
        mutedArgb = mutedArgb,
        surfaceArgb = surfaceArgb,
    )
}

private fun drawShareFocusCard(
    canvas: android.graphics.Canvas,
    rect: RectF,
    title: String,
    primaryLabel: String,
    primaryValue: String,
    accent: Int,
    progress: Float,
    metrics: List<DailyFocusMetric>,
    textArgb: Int,
    mutedArgb: Int,
    surfaceArgb: Int,
) {
    canvas.drawRoundRect(
        rect,
        34f,
        34f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surfaceArgb },
    )
    canvas.drawRoundRect(
        rect,
        34f,
        34f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(46, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
            style = Paint.Style.STROKE
            strokeWidth = 2f
        },
    )
    canvas.drawRoundRect(
        RectF(rect.left + 24f, rect.top + 24f, rect.left + 66f, rect.top + 66f),
        14f,
        14f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(40, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
        },
    )
    val titleTypeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 32f
        typeface = titleTypeface
    }
    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 24f
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        textSize = 48f
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 22f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    canvas.drawLine(rect.left + 38f, rect.top + 45f, rect.left + 52f, rect.top + 45f, iconPaint)
    canvas.drawLine(rect.left + 45f, rect.top + 38f, rect.left + 45f, rect.top + 52f, iconPaint)
    canvas.drawText(title, rect.left + 78f, rect.top + 54f, titlePaint)
    canvas.drawText(primaryLabel, rect.left + 28f, rect.top + 104f, labelPaint)
    drawEllipsizedText(canvas, primaryValue, rect.left + 28f, rect.top + 154f, rect.width() - 154f, valuePaint)

    val ringCenterX = rect.right - 72f
    val ringCenterY = rect.top + 116f
    val ringRadius = 43f
    val ringRect = RectF(ringCenterX - ringRadius, ringCenterY - ringRadius, ringCenterX + ringRadius, ringCenterY + ringRadius)
    val ringStroke = 11f
    val ringBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(34, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
        style = Paint.Style.STROKE
        strokeWidth = ringStroke
        strokeCap = Paint.Cap.ROUND
    }
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        style = Paint.Style.STROKE
        strokeWidth = ringStroke
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawArc(ringRect, -90f, 360f, false, ringBasePaint)
    canvas.drawArc(ringRect, -90f, 360f * progress.coerceIn(0f, 1f), false, ringPaint)
    val ringTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        textSize = 22f
        textAlign = Paint.Align.CENTER
        typeface = titleTypeface
    }
    canvas.drawText("${(progress.coerceIn(0f, 1f) * 100f).roundToInt()}%", ringCenterX, ringCenterY + 8f, ringTextPaint)

    val pillTop = rect.top + 174f
    val pillGap = 8f
    val pillWidth = (rect.width() - 56f - pillGap * 2f) / 3f
    metrics.take(3).forEachIndexed { index, metric ->
        val pillLeft = rect.left + 28f + index * (pillWidth + pillGap)
        val pillRect = RectF(pillLeft, pillTop, pillLeft + pillWidth, pillTop + 42f)
        canvas.drawRoundRect(
            pillRect,
            18f,
            18f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(28, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
            },
        )
        drawEllipsizedText(canvas, metric.label, pillLeft + 12f, pillTop + 17f, pillWidth - 24f, bodyPaint)
        drawEllipsizedText(canvas, metric.value, pillLeft + 12f, pillTop + 35f, pillWidth - 24f, Paint(bodyPaint).apply {
            color = textArgb
            typeface = titleTypeface
        })
    }
}

private fun drawSharePosterIcon(
    context: Context,
    canvas: android.graphics.Canvas,
    packageName: String,
    label: String,
    left: Float,
    top: Float,
    size: Float,
    fallbackColor: Int,
) {
    val rect = RectF(left, top, left + size, top + size)
    val iconBitmap =
        runCatching {
            context.packageManager.getApplicationIcon(packageName).toBitmap(
                width = size.roundToInt(),
                height = size.roundToInt(),
                config = Bitmap.Config.ARGB_8888,
            )
        }.getOrNull()
    canvas.drawRoundRect(
        rect,
        size * 0.28f,
        size * 0.28f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE },
    )
    if (iconBitmap != null) {
        val path = android.graphics.Path().apply {
            addRoundRect(rect, size * 0.28f, size * 0.28f, android.graphics.Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(iconBitmap, null, rect, Paint(Paint.ANTI_ALIAS_FLAG))
        canvas.restore()
    } else {
        canvas.drawRoundRect(
            rect,
            size * 0.28f,
            size * 0.28f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fallbackColor },
        )
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = size * 0.46f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
        }
        val baseline = top + size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(label.ifBlank { "T" }.take(1), left + size / 2f, baseline, textPaint)
    }
}

private fun buildSharePosterInsight(data: ShareReportData): String {
    val nightText = if (data.nightUsageMillis > 0L) AppText.t("stats_night_value", formatDuration(data.nightUsageMillis)) else AppText.t("stats_light_night_use")
    val appText = data.topApps.firstOrNull()?.let { AppText.t("stats_top_app_is_value", it.label) } ?: AppText.t("stats_app_usage_is_spread_out")
    return AppText.t("stats_value_mainly_concentrated_in_value_value_value", data.comparisonLabel, data.dominantPeriod, nightText, appText)
}

private fun drawEllipsizedText(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    maxWidth: Float,
    paint: Paint,
) {
    var output = text
    while (paint.measureText(output) > maxWidth && output.length > 2) {
        output = output.dropLast(2) + "…"
    }
    canvas.drawText(output, x, y, paint)
}

private fun drawMultilineText(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    maxWidth: Float,
    paint: Paint,
    lineHeight: Float,
    maxLines: Int,
) {
    val words =
        if (text.contains(" ")) {
            text.split(" ")
        } else {
            text.map { it.toString() }
        }
    val lines = mutableListOf<String>()
    var current = ""
    words.forEach { word ->
        val separator = if (text.contains(" ")) " " else ""
        val candidate = if (current.isEmpty()) word else "$current$separator$word"
        if (paint.measureText(candidate) <= maxWidth) {
            current = candidate
        } else {
            if (current.isNotEmpty()) lines += current
            current = word
        }
    }
    if (current.isNotEmpty()) lines += current
    lines.take(maxLines).forEachIndexed { index, rawLine ->
        val line =
            if (index == maxLines - 1 && lines.size > maxLines) {
                var output = "$rawLine…"
                while (paint.measureText(output) > maxWidth && output.length > 2) {
                    output = output.dropLast(2) + "…"
                }
                output
            } else {
                rawLine
            }
        canvas.drawText(line, x, y + index * lineHeight, paint)
    }
}

private fun formatDuration(durationMillis: Long): String {
    if (durationMillis <= 0L) return "0m"
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

@Composable
private fun animateLongValue(
    targetValue: Long,
    label: String,
    durationMillis: Int = 800,
    delayMillis: Int = 0,
): Long {
    val delayedTargetValue = rememberDelayedLongTarget(
        targetValue = targetValue.coerceAtLeast(0L),
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue.toFloat(),
        animationSpec = tween(durationMillis = durationMillis),
        label = label,
    )
    return animatedValue.roundToLong().coerceAtLeast(0L)
}

@Composable
private fun animateIntValue(
    targetValue: Int,
    label: String,
    durationMillis: Int = 700,
    delayMillis: Int = 0,
): Int {
    val delayedTargetValue = rememberDelayedIntTarget(
        targetValue = targetValue.coerceAtLeast(0),
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue.toFloat(),
        animationSpec = tween(durationMillis = durationMillis),
        label = label,
    )
    return animatedValue.roundToInt().coerceAtLeast(0)
}

@Composable
private fun animateFractionValue(
    targetValue: Float,
    label: String,
    durationMillis: Int = 760,
    delayMillis: Int = 0,
): Float {
    val delayedTargetValue = rememberDelayedFloatTarget(
        targetValue = targetValue.coerceIn(0f, 1f),
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue,
        animationSpec = tween(durationMillis = durationMillis),
        label = label,
    )
    return animatedValue.coerceIn(0f, 1f)
}

@Composable
private fun animateDecimalValue(
    targetValue: Float,
    label: String,
    durationMillis: Int = 760,
    delayMillis: Int = 0,
): Float {
    val delayedTargetValue = rememberDelayedFloatTarget(
        targetValue = targetValue.coerceAtLeast(0f),
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue,
        animationSpec = tween(durationMillis = durationMillis),
        label = label,
    )
    return animatedValue.coerceAtLeast(0f)
}

@Composable
private fun animateMetricDisplayText(
    rawText: String,
    label: String,
    delayMillis: Int = 0,
): String {
    val durationMatch = Regex("""(\d+)h(?: (\d+)m)?|(\d+)m""").find(rawText)
    if (durationMatch != null) {
        val animatedDuration = animateLongValue(
            targetValue = parseDisplayDuration(durationMatch.value),
            label = "${label}_duration",
            durationMillis = 860,
            delayMillis = delayMillis,
        )
        return rawText.replaceRange(durationMatch.range, formatDuration(animatedDuration))
    }

    val percentMatch = Regex("""(\d+)%""").find(rawText)
    if (percentMatch != null) {
        val animatedPercent = animateIntValue(
            targetValue = percentMatch.groupValues[1].toIntOrNull() ?: 0,
            label = "${label}_percent",
            durationMillis = 760,
            delayMillis = delayMillis,
        )
        return rawText.replaceRange(percentMatch.range, "${animatedPercent}%")
    }

    val decimalMatch = Regex("""(\d+\.\d+)""").find(rawText)
    if (decimalMatch != null) {
        val animatedDecimal = animateDecimalValue(
            targetValue = decimalMatch.groupValues[1].toFloatOrNull() ?: 0f,
            label = "${label}_decimal",
            durationMillis = 760,
            delayMillis = delayMillis,
        )
        return rawText.replaceRange(
            decimalMatch.range,
            String.format(Locale.CHINA, "%.1f", animatedDecimal),
        )
    }

    val countMatch = Regex("""\d+""").find(rawText)
    if (countMatch != null) {
        val animatedCount = animateIntValue(
            targetValue = countMatch.value.toIntOrNull() ?: 0,
            label = "${label}_count",
            durationMillis = 720,
            delayMillis = delayMillis,
        )
        return rawText.replaceRange(countMatch.range, animatedCount.toString())
    }

    return rawText
}

private fun parseDisplayDuration(durationText: String): Long {
    val hourMinuteMatch = Regex("""(?:(\d+)h)?(?: ?(\d+)m)?""").matchEntire(durationText)
    if (hourMinuteMatch != null) {
        val hours = hourMinuteMatch.groupValues[1].toLongOrNull() ?: 0L
        val minutes = hourMinuteMatch.groupValues[2].toLongOrNull() ?: 0L
        return (hours * 60L + minutes) * 60_000L
    }
    return 0L
}

@Composable
private fun rememberDelayedLongTarget(
    targetValue: Long,
    delayMillis: Int,
): Long {
    val sanitizedTarget = targetValue.coerceAtLeast(0L)
    val delayedTarget by produceState(
        initialValue = if (delayMillis > 0) 0L else sanitizedTarget,
        key1 = sanitizedTarget,
        key2 = delayMillis,
    ) {
        if (delayMillis > 0) {
            value = 0L
            delay(delayMillis.toLong())
        }
        value = sanitizedTarget
    }
    return delayedTarget
}

@Composable
private fun rememberDelayedIntTarget(
    targetValue: Int,
    delayMillis: Int,
): Int {
    val sanitizedTarget = targetValue.coerceAtLeast(0)
    val delayedTarget by produceState(
        initialValue = if (delayMillis > 0) 0 else sanitizedTarget,
        key1 = sanitizedTarget,
        key2 = delayMillis,
    ) {
        if (delayMillis > 0) {
            value = 0
            delay(delayMillis.toLong())
        }
        value = sanitizedTarget
    }
    return delayedTarget
}

@Composable
private fun rememberDelayedFloatTarget(
    targetValue: Float,
    delayMillis: Int,
): Float {
    val sanitizedTarget = targetValue.coerceAtLeast(0f)
    val delayedTarget by produceState(
        initialValue = if (delayMillis > 0) 0f else sanitizedTarget,
        key1 = sanitizedTarget,
        key2 = delayMillis,
    ) {
        if (delayMillis > 0) {
            value = 0f
            delay(delayMillis.toLong())
        }
        value = sanitizedTarget
    }
    return delayedTarget
}

private fun formatAxisDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60
    return when {
        hours >= 1 -> "${hours}h"
        totalMinutes > 0 -> "${totalMinutes}m"
        else -> "0"
    }
}

private fun formatClockTime(epochMillis: Long, zoneId: ZoneId): String {
    return java.time.Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA))
}

private fun formatArchiveDate(date: String, pattern: String): String {
    return LocalDate
        .parse(date)
        .format(DateTimeFormatter.ofPattern(pattern, Locale.CHINA))
}

private fun dayHourLabel(hour: Int): String {
    return "${hour.toString().padStart(2, '0')}:00"
}

private fun appHourlyBucketAt(appItem: DailyAppArchiveEntity, hour: Int): Long {
    return when (hour) {
        0 -> appItem.hour00Millis
        1 -> appItem.hour01Millis
        2 -> appItem.hour02Millis
        3 -> appItem.hour03Millis
        4 -> appItem.hour04Millis
        5 -> appItem.hour05Millis
        6 -> appItem.hour06Millis
        7 -> appItem.hour07Millis
        8 -> appItem.hour08Millis
        9 -> appItem.hour09Millis
        10 -> appItem.hour10Millis
        11 -> appItem.hour11Millis
        12 -> appItem.hour12Millis
        13 -> appItem.hour13Millis
        14 -> appItem.hour14Millis
        15 -> appItem.hour15Millis
        16 -> appItem.hour16Millis
        17 -> appItem.hour17Millis
        18 -> appItem.hour18Millis
        19 -> appItem.hour19Millis
        20 -> appItem.hour20Millis
        21 -> appItem.hour21Millis
        22 -> appItem.hour22Millis
        23 -> appItem.hour23Millis
        else -> 0L
    }
}

private fun formatSignedPointsLocal(value: Double): String {
    val formatted = String.format(Locale.CHINA, "%.1f", kotlin.math.abs(value))
    return if (value >= 0) "+$formatted" else "-$formatted"
}

private fun deltaDescription(
    current: Long,
    baseline: Long,
    prefix: String,
    countUnit: String? = null,
): String {
    if (baseline <= 0L && current <= 0L) return AppText.t("stats_value_flat", prefix)
    val delta = current - baseline
    if (delta == 0L) return AppText.t("stats_value_flat", prefix)
    val direction = if (delta > 0L) AppText.t("stats_label_2") else AppText.t("stats_label_12")
    val deltaValue = countUnit?.let { "${kotlin.math.abs(delta)} $it" } ?: formatDuration(kotlin.math.abs(delta))
    return "$prefix $direction $deltaValue"
}

private fun Double.roundToLongSafe(): Long {
    return if (this.isNaN()) 0L else roundToLong()
}
