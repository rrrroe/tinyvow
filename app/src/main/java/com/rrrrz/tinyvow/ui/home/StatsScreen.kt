package com.rrrrz.tinyvow.ui.home

import android.content.Context
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private enum class ReportTab(val label: String) {
    DAY("日报"),
    WEEK("周报"),
    MONTH("月报"),
    YEAR("年报"),
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
)

private data class TimelineSectionData(
    val buckets: List<DailyTimelineBucket>,
    val periodUsage: List<PeriodUsageStat>,
    val peakHourLabel: String,
    val peakHourMillis: Long,
    val peakTwoHourLabel: String,
    val peakTwoHourMillis: Long,
    val nightUsageMillis: Long,
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

private data class ShareReportData(
    val title: String,
    val subtitle: String,
    val primaryValue: String,
    val primaryLabel: String,
    val metrics: List<DailyFocusMetric>,
    val insight: String,
    val topApps: List<AppDisplayItem>,
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
    val selectedArchiveDate: String? = null,
    val previousArchiveDate: String? = null,
    val nextArchiveDate: String? = null,
    val availableArchiveDates: List<String> = emptyList(),
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
    modifier: Modifier = Modifier,
) {
    val zoneId = remember { ZoneId.systemDefault() }
    var selectedTab by remember { mutableStateOf(ReportTab.DAY) }
    var selectedArchiveDate by remember { mutableStateOf<String?>(null) }
    var uiState by remember { mutableStateOf(DailyReportUiState(selectedTab = selectedTab)) }

    LaunchedEffect(
        usageAccessStatus,
        groupsWithApps,
        selectedTab,
        selectedArchiveDate,
        userPoints,
        todayPoints,
    ) {
        if (usageAccessStatus != UsageAccessStatus.GRANTED) {
            uiState = DailyReportUiState(
                isPermissionGranted = false,
                selectedTab = selectedTab,
                isRefreshing = false,
                selectedArchiveDate = selectedArchiveDate,
            )
            return@LaunchedEffect
        }

        when (selectedTab) {
            ReportTab.DAY -> {
                uiState =
                    createRefreshingUiState(
                        selectedTab = selectedTab,
                        previous = uiState,
                        selectedArchiveDate = selectedArchiveDate,
                    )
                val recentArchives =
                    archiveRepository
                        .getRecentArchives(limit = 3650)
                        .first()
                        .sortedByDescending { it.archiveDate }
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
                    )
                buildArchivedWindowReportUiState(
                    selectedTab = selectedTab,
                    zoneId = zoneId,
                    archiveRepository = archiveRepository,
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
        modifier = modifier,
    )
}

private fun createRefreshingUiState(
    selectedTab: ReportTab,
    previous: DailyReportUiState? = null,
    selectedArchiveDate: String? = previous?.selectedArchiveDate,
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
        selectedArchiveDate = selectedArchiveDate,
        previousArchiveDate = previous?.previousArchiveDate,
        nextArchiveDate = previous?.nextArchiveDate,
        availableArchiveDates = previous?.availableArchiveDates.orEmpty(),
    )
}

private suspend fun buildArchivedWindowReportUiState(
    selectedTab: ReportTab,
    zoneId: ZoneId,
    archiveRepository: DailyArchiveRepository,
    updateState: ((DailyReportUiState) -> DailyReportUiState) -> Unit,
) {
    val today = LocalDate.now(zoneId)
    val endDate = today.minusDays(1)
    val windowDays = archiveWindowDays(selectedTab)
    val currentStart =
        if (selectedTab == ReportTab.YEAR) {
            LocalDate.of(endDate.year, 1, 1)
        } else {
            endDate.minusDays((windowDays - 1).toLong())
        }
    val actualWindowDays = (endDate.toEpochDay() - currentStart.toEpochDay() + 1L).coerceAtLeast(1L).toInt()
    val previousEnd = currentStart.minusDays(1)
    val previousStart = previousEnd.minusDays((actualWindowDays - 1).toLong())
    val currentFrom = ArchiveDateUtils.formatDate(currentStart)
    val currentTo = ArchiveDateUtils.formatDate(endDate)
    val previousFrom = ArchiveDateUtils.formatDate(previousStart)
    val previousTo = ArchiveDateUtils.formatDate(previousEnd)

    val currentArchives = archiveRepository.getArchivesByRange(currentFrom, currentTo).first()
    val previousArchives = archiveRepository.getArchivesByRange(previousFrom, previousTo).first()
    val rollingYearStart = endDate.minusDays(364)
    val rollingYearArchives =
        if (selectedTab == ReportTab.YEAR) {
            archiveRepository
                .getArchivesByRange(
                    from = ArchiveDateUtils.formatDate(rollingYearStart),
                    to = currentTo,
                ).first()
        } else {
            emptyList()
        }

    val currentAppArchives =
        archiveRepository
            .getAppArchivesByRange(
                from = currentFrom,
                to = currentTo,
            ).first()
    val previousAppArchives =
        archiveRepository
            .getAppArchivesByRange(
                from = previousFrom,
                to = previousTo,
            ).first()
    val currentGroupArchives = archiveRepository.getGroupArchivesByRange(currentFrom, currentTo).first()

    val currentSnapshots = mergeArchivedAppSnapshots(currentAppArchives)
    val previousSnapshots = mergeArchivedAppSnapshots(previousAppArchives)
    val currentMetrics = buildArchivedWindowMetrics(currentSnapshots)
    val previousMetrics = buildArchivedWindowMetrics(previousSnapshots)
    val timelineBuckets =
        if (selectedTab == ReportTab.YEAR) {
            buildYearTimelineBuckets(currentArchives)
        } else {
            buildArchiveTimelineBuckets(currentArchives)
        }
    val periodUsage = buildArchivePeriodUsageStats(selectedTab, timelineBuckets)
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
            activeBucketCount = timelineBuckets.count { it.deviceMillis > 0L },
            topApp = topApps.firstOrNull(),
        )
    val averagePerDayUsage =
        if (currentArchives.isEmpty()) {
            0L
        } else {
            currentArchives.sumOf { it.totalUsageMillis } / currentArchives.size
        }
    val pointsNet = currentArchives.sumOf { it.pointsNet }
    val redemptionCount = currentArchives.sumOf { it.redemptionCount }
    val summary =
        buildArchivedReportSummary(
            selectedTab = selectedTab,
            startDate = currentStart,
            endDate = endDate,
            overview = overview,
            previousMetrics = previousMetrics,
            dominantPeriod = periodUsage.maxByOrNull { it.deviceMillis }?.label ?: "--",
            pointsNet = pointsNet,
            redemptionCount = redemptionCount,
            averagePerDayUsage = averagePerDayUsage,
        )
    val behaviorInsight = buildArchivedDayBehaviorInsight(currentSnapshots, timelineBuckets)
    val comparisons =
        buildArchivedComparisonMetrics(
            selectedTab = selectedTab,
            overview = overview,
            currentMetrics = currentMetrics,
            previousMetrics = previousMetrics,
            averagePerDayUsage = averagePerDayUsage,
        )
    val timelineStateData =
        buildArchiveTimelineSectionData(
            selectedTab = selectedTab,
            timelineBuckets = timelineBuckets,
            nightUsageMillis = currentMetrics.nightUsageMillis,
            periodUsage = periodUsage,
        )
    val windowFocusData =
        buildWindowFocusSectionData(
            selectedTab = selectedTab,
            archives = currentArchives,
            groupArchives = currentGroupArchives,
            activeDayCount = currentArchives.size,
        )
    val heatmapData =
        if (selectedTab == ReportTab.MONTH || selectedTab == ReportTab.YEAR) {
            buildHeatmapSectionData(
                selectedTab = selectedTab,
                startDate = currentStart,
                endDate = endDate,
                archives = currentArchives,
            )
        } else {
            null
        }
    val yearDualScopeData =
        if (selectedTab == ReportTab.YEAR) {
            YearDualScopeSectionData(
                naturalYear =
                    buildYearScopeSummary(
                        title = "自然年",
                        startDate = currentStart,
                        endDate = endDate,
                        archives = currentArchives,
                    ),
                rollingYear =
                    buildYearScopeSummary(
                        title = "近 365 天",
                        startDate = rollingYearStart,
                        endDate = endDate,
                        archives = rollingYearArchives,
                    ),
            )
        } else {
            null
        }
    val shareData =
        buildShareReportData(
            selectedTab = selectedTab,
            summary = summary,
            archives = currentArchives,
            windowFocus = windowFocusData,
            topApps = topApps,
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
                    ),
                ),
            dailyFocusState = SectionState.Empty,
            windowFocusState =
                if (currentArchives.isEmpty()) {
                    SectionState.Empty
                } else {
                    SectionState.Ready(windowFocusData)
                },
            heatmapState = heatmapData?.let { SectionState.Ready(it) } ?: SectionState.Empty,
            yearDualScopeState = yearDualScopeData?.let { SectionState.Ready(it) } ?: SectionState.Empty,
            shareState = SectionState.Ready(shareData),
            timelineState =
                if (timelineBuckets.isEmpty()) {
                    SectionState.Empty
                } else {
                    SectionState.Ready(timelineStateData)
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
            selectedArchiveDate = null,
            previousArchiveDate = null,
            nextArchiveDate = null,
            placeholderTitle = null,
            placeholderDescription = null,
        )
    }
}

private fun archiveWindowDays(tab: ReportTab): Int =
    when (tab) {
        ReportTab.DAY -> 1
        ReportTab.WEEK -> 7
        ReportTab.MONTH -> 30
        ReportTab.YEAR -> 365
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
                title = "管控成效",
                description = if (archive.controlExceededGroupCount > 0) "今天仍有超限，需要收紧重点分组。" else "管控分组整体稳定。",
                primaryLabel = "节省时长",
                primaryValue = formatDuration(archive.savedMillis),
                metrics =
                    listOf(
                        DailyFocusMetric("达标", "${archive.controlCompletedGroupCount} 组"),
                        DailyFocusMetric("超限", "${archive.controlExceededGroupCount} 组"),
                        DailyFocusMetric("拦截", "${archive.controlBlockEventCount} 次"),
                    ),
                progress = controlProgress,
                spotlightLabel =
                    when {
                        exceededControlGroup != null -> "重点关注"
                        bestControlGroup != null -> "表现最好"
                        else -> "管控分组"
                    },
                spotlightValue =
                    when {
                        exceededControlGroup != null ->
                            "${exceededControlGroup.groupName} · 超限 ${formatDuration(exceededControlGroup.exceededMillisAtClose)}"
                        bestControlGroup != null && bestControlGroup.remainingMillisAtClose > 0L ->
                            "${bestControlGroup.groupName} · 剩余 ${formatDuration(bestControlGroup.remainingMillisAtClose)}"
                        bestControlGroup != null -> bestControlGroup.groupName
                        else -> "暂无分组归档"
                    },
                isWarning = archive.controlExceededGroupCount > 0 || archive.controlBlockEventCount > 0,
            ),
        encourage =
            DailyModeSummary(
                title = "鼓励进度",
                description = if (archive.pointsNet >= 0.0) "鼓励使用带来正向积分收益。" else "今天积分净值为负，关注兑换节奏。",
                primaryLabel = "净积分",
                primaryValue = formatSignedPointsLocal(archive.pointsNet),
                metrics =
                    listOf(
                        DailyFocusMetric("鼓励时长", formatDuration(archive.encourageUsageMillis)),
                        DailyFocusMetric("达标", "${archive.encourageCompletedGroupCount} 组"),
                        DailyFocusMetric("兑换", "${archive.redemptionCount} 次"),
                    ),
                progress = encourageProgress,
                spotlightLabel = if (bestEncourageGroup != null) "最佳鼓励" else "鼓励分组",
                spotlightValue =
                    if (bestEncourageGroup != null) {
                        val points = formatSignedPointsLocal(bestEncourageGroup.earnedPoints)
                        "${bestEncourageGroup.groupName} · $points / ${formatDuration(bestEncourageGroup.dailyUsageMillis)}"
                    } else {
                        "暂无分组归档"
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
            ReportTab.WEEK -> "本周"
            ReportTab.MONTH -> "本月"
            ReportTab.YEAR -> "今年"
            ReportTab.DAY -> "今日"
        }

    return WindowFocusSectionData(
        control =
            DailyModeSummary(
                title = "管控成效",
                description = if (totalExceededGroups > 0) "$dayUnit 有 $totalExceededGroups 次分组超限。" else "$dayUnit 管控整体稳定。",
                primaryLabel = "节省时长",
                primaryValue = formatDuration(totalSaved),
                metrics =
                    listOf(
                        DailyFocusMetric("达标", "$totalControlCompleted 次"),
                        DailyFocusMetric("超限", "$totalExceededGroups 次"),
                        DailyFocusMetric("拦截", "$totalBlockEvents 次"),
                    ),
                progress = controlProgress,
                spotlightLabel = if (severeControl != null) "重点关注" else "表现最好",
                spotlightValue =
                    when {
                        severeControl != null -> "${severeControl.groupName} · 超限 ${formatDuration(severeControl.exceededMillis)}"
                        bestControl != null && bestControl.remainingMillis > 0L -> "${bestControl.groupName} · 剩余 ${formatDuration(bestControl.remainingMillis)}"
                        bestControl != null -> bestControl.groupName
                        else -> "暂无管控分组归档"
                    },
                isWarning = totalExceededGroups > 0 || totalBlockEvents > 0,
            ),
        encourage =
            DailyModeSummary(
                title = "鼓励进度",
                description = if (pointsNet >= 0.0) "$dayUnit 净积分保持正向。" else "$dayUnit 兑换超过积分收益。",
                primaryLabel = "净积分",
                primaryValue = formatSignedPointsLocal(pointsNet),
                metrics =
                    listOf(
                        DailyFocusMetric("鼓励时长", formatDuration(totalEncourageUsage)),
                        DailyFocusMetric("达标", "$totalEncourageCompleted 次"),
                        DailyFocusMetric("兑换", "$totalRedemptions 次"),
                    ),
                progress = encourageProgress,
                spotlightLabel = if (bestEncourage != null) "最佳鼓励" else "鼓励分组",
                spotlightValue =
                    if (bestEncourage != null) {
                        "${bestEncourage.groupName} · ${formatSignedPointsLocal(bestEncourage.earnedPoints)} / ${formatDuration(bestEncourage.usageMillis)}"
                    } else {
                        "暂无鼓励分组归档"
                    },
                isWarning = pointsNet < 0.0,
            ),
        highlights =
            listOf(
                DailyFocusMetric("活跃天数", "$activeDayCount 天"),
                DailyFocusMetric("日均时长", formatDuration(if (activeDayCount > 0) archives.sumOf { it.totalUsageMillis } / activeDayCount else 0L)),
                DailyFocusMetric("节省总量", formatDuration(totalSaved)),
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
            label = "${month}月",
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
                    label = "${month}月",
                    valueMillis = total,
                    exceeded = monthArchives.any { it.controlExceededGroupCount > 0 },
                    selected = total == archives
                        .groupBy { LocalDate.parse(it.archiveDate).monthValue }
                        .maxOfOrNull { entry -> entry.value.sumOf { it.totalUsageMillis } },
                )
            }
        HeatmapSectionData(
            title = "年度月份热力",
            subtitle = "颜色越深代表这个月越值得复盘，红点表示出现过管控超限。",
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
            title = "月历热力",
            subtitle = "每天一个格子，快速看出高峰、低谷和超限日。",
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
        activeDays = "$activeDays 天",
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
): ShareReportData {
    val tabName =
        when (selectedTab) {
            ReportTab.DAY -> "日报"
            ReportTab.WEEK -> "周报"
            ReportTab.MONTH -> "月报"
            ReportTab.YEAR -> "年报"
        }
    val bestDay = archives.maxByOrNull { it.totalUsageMillis }
    val calmDay = archives.filter { it.totalUsageMillis > 0L }.minByOrNull { it.totalUsageMillis }
    val insight =
        when {
            bestDay != null && calmDay != null ->
                "峰值 ${formatArchiveDate(bestDay.archiveDate, "M/d")} · ${formatDuration(bestDay.totalUsageMillis)}，最低 ${formatArchiveDate(calmDay.archiveDate, "M/d")} · ${formatDuration(calmDay.totalUsageMillis)}。"
            bestDay != null -> "峰值 ${formatArchiveDate(bestDay.archiveDate, "M/d")} · ${formatDuration(bestDay.totalUsageMillis)}。"
            else -> "还没有足够归档，下一次复盘会更完整。"
        }
    val metrics =
        windowFocus?.let {
            listOf(
                DailyFocusMetric("管控节省", it.control.primaryValue),
                DailyFocusMetric("鼓励净值", it.encourage.primaryValue),
                DailyFocusMetric("Top 应用", topApps.firstOrNull()?.label ?: "暂无"),
            )
        } ?: summary.tags.take(3).mapIndexed { index, tag -> DailyFocusMetric("亮点 ${index + 1}", tag) }
    return ShareReportData(
        title = "Tiny Vow $tabName",
        subtitle = summary.subtitle,
        primaryValue = summary.primaryValue,
        primaryLabel = "总使用时长",
        metrics = metrics,
        insight = insight,
        topApps = topApps.take(3),
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
            ReportTab.WEEK -> listOf("周初", "周中", "周后段")
            ReportTab.MONTH -> listOf("第 1 周", "第 2 周", "第 3 周", "第 4 周+")
            ReportTab.YEAR -> listOf("春", "夏", "秋", "冬")
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
                placeholderTitle = "暂无已归档日报",
                placeholderDescription = "日报只展示昨天和更早的归档数据。等到明天再回来，就能看到第一条记录。",
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
    val summary =
        buildArchivedDaySummary(
            archive = selectedArchive,
            overview = overview,
            previousMetrics = previousMetrics,
            averagePerDayUsage = averagePerDayUsage,
            dominantPeriod = periodUsage.maxByOrNull { it.deviceMillis }?.label ?: "全天",
        )
    val behaviorInsight = buildArchivedDayBehaviorInsight(currentSnapshots, timelineBuckets)
    val comparisons =
        buildArchivedDayComparisonMetrics(
            currentArchive = selectedArchive,
            currentMetrics = currentMetrics,
            previousArchive = previousArchive,
            previousMetrics = previousMetrics,
            averagePerDayUsage = averagePerDayUsage,
        )
    val timelineStateData =
        buildArchiveTimelineSectionData(
            selectedTab = ReportTab.DAY,
            timelineBuckets = timelineBuckets,
            nightUsageMillis = currentMetrics.nightUsageMillis,
            periodUsage = periodUsage,
        )
    val dailyFocusData = buildDailyFocusSectionData(selectedArchive, currentGroupArchives)
    val shareData =
        buildShareReportData(
            selectedTab = ReportTab.DAY,
            summary = summary,
            archives = listOf(selectedArchive),
            windowFocus = null,
            topApps = topApps,
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
            ReportTab.DAY -> "归档日报"
            ReportTab.WEEK -> "近 7 日趋势"
            ReportTab.MONTH -> "近 30 日趋势"
            ReportTab.YEAR -> "年度趋势"
        }
    val subtitle =
        "${startDate.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA))} - ${endDate.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA))}"
    val usageMessage =
        when {
            previousMetrics.deviceUsageMillis > 0L &&
                overview.totalUsageMillis > previousMetrics.deviceUsageMillis * 1.15f -> "较上一窗口更重"
            previousMetrics.deviceUsageMillis > 0L &&
                overview.totalUsageMillis < previousMetrics.deviceUsageMillis * 0.85f -> "较上一窗口更轻"
            else -> "与上一窗口接近"
        }
    val formattedPoints = String.format(Locale.CHINA, "%.1f", pointsNet)
    val pointTag = if (pointsNet >= 0) "净积分 +$formattedPoints" else "净积分 $formattedPoints"
    return DailyReportSummary(
        title = title,
        subtitle = subtitle,
        capturedAt = "归档截至 ${endDate.format(DateTimeFormatter.ofPattern("M/d", Locale.CHINA))}",
        message = "$usageMessage，主要集中在 $dominantPeriod。",
        primaryValue = formatDuration(overview.totalUsageMillis),
        secondaryValue = deltaDescription(overview.totalUsageMillis, previousMetrics.deviceUsageMillis, "较上一窗口"),
        tertiaryValue = "日均 ${formatDuration(averagePerDayUsage)}",
        tags = listOf(pointTag, "$redemptionCount 次兑换", dominantPeriod),
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
            label = "窗口总时长",
            todayValue = formatDuration(overview.totalUsageMillis),
            yesterdayDelta = deltaDescription(overview.totalUsageMillis, previousMetrics.deviceUsageMillis, "较上一窗口"),
            averageDelta = "日均 ${formatDuration(averagePerDayUsage)}",
        ),
        ComparisonMetric(
            label = "打开次数",
            todayValue = "${overview.openCount} 次",
            yesterdayDelta = deltaDescription(overview.openCount.toLong(), previousMetrics.deviceOpenCount.toLong(), "较上一窗口", countUnit = "次"),
            averageDelta = "日均 ${(overview.openCount.toFloat() / archiveWindowDays(selectedTab).coerceAtLeast(1)).roundToInt()} 次",
        ),
        ComparisonMetric(
            label = "夜间使用",
            todayValue = formatDuration(currentMetrics.nightUsageMillis),
            yesterdayDelta = deltaDescription(currentMetrics.nightUsageMillis, previousMetrics.nightUsageMillis, "较上一窗口"),
            averageDelta = null,
        ),
        ComparisonMetric(
            label = "最长单次会话",
            todayValue = formatDuration(currentMetrics.longestSessionMillis),
            yesterdayDelta = deltaDescription(currentMetrics.longestSessionMillis, previousMetrics.longestSessionMillis, "较上一窗口"),
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
                overview.totalUsageMillis > previousMetrics.deviceUsageMillis * 1.15f -> "这一天的使用明显高于上一条归档。"
            previousMetrics.deviceUsageMillis > 0L &&
                overview.totalUsageMillis < previousMetrics.deviceUsageMillis * 0.85f -> "这一天的使用明显低于上一条归档。"
            else -> "这一天的使用强度与上一条归档接近。"
        }
    return DailyReportSummary(
        title = "归档日报",
        subtitle = formatArchiveDate(archive.archiveDate, "M月d日 EEEE"),
        capturedAt = "归档日期 ${formatArchiveDate(archive.archiveDate, "M/d")}",
        message = "$message 主要集中在 $dominantPeriod。",
        primaryValue = formatDuration(overview.totalUsageMillis),
        secondaryValue = deltaDescription(overview.totalUsageMillis, previousMetrics.deviceUsageMillis, "较上一条归档"),
        tertiaryValue = if (averagePerDayUsage > 0L) "近 7 个归档日日均 ${formatDuration(averagePerDayUsage)}" else "暂无更早归档均值",
        tags = listOf("净积分 ${formatSignedPointsLocal(archive.pointsNet)}", "${archive.redemptionCount} 次兑换", "节省 ${formatDuration(archive.savedMillis)}"),
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
        predictedSleepLabel = if (nightLeader != null) "${nightLeader.label} · ${formatDuration(nightLeader.value)}" else "暂无记录",
        predictedSleepDurationLabel = if (mostOpened != null) "${mostOpened.label} · ${mostOpened.value.toInt()} 次" else "暂无记录",
        beforeSleep = BehaviorAppMoment(
            label = "夜间主导应用",
            packageName = nightLeader?.packageName,
            appLabel = nightLeader?.label,
        ),
        afterWake = BehaviorAppMoment(
            label = "打开次数最多",
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
): List<ComparisonMetric> {
    if (previousArchive == null) {
        return emptyList()
    }
    return listOf(
        ComparisonMetric(
            label = "总使用时长",
            todayValue = formatDuration(currentArchive.totalUsageMillis),
            yesterdayDelta = deltaDescription(currentArchive.totalUsageMillis, previousArchive.totalUsageMillis, "较上一条归档"),
            averageDelta = if (averagePerDayUsage > 0L) "近 7 个归档日日均 ${formatDuration(averagePerDayUsage)}" else null,
        ),
        ComparisonMetric(
            label = "打开次数",
            todayValue = "${currentMetrics.deviceOpenCount} 次",
            yesterdayDelta = deltaDescription(currentMetrics.deviceOpenCount.toLong(), previousMetrics.deviceOpenCount.toLong(), "较上一条归档", countUnit = "次"),
            averageDelta = null,
        ),
        ComparisonMetric(
            label = "夜间使用",
            todayValue = formatDuration(currentMetrics.nightUsageMillis),
            yesterdayDelta = deltaDescription(currentMetrics.nightUsageMillis, previousMetrics.nightUsageMillis, "较上一条归档"),
            averageDelta = null,
        ),
        ComparisonMetric(
            label = "最长单次会话",
            todayValue = formatDuration(currentMetrics.longestSessionMillis),
            yesterdayDelta = deltaDescription(currentMetrics.longestSessionMillis, previousMetrics.longestSessionMillis, "较上一条归档"),
            averageDelta = null,
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
        ReportTab.WEEK -> "周报筹备中"
        ReportTab.MONTH -> "月报筹备中"
        ReportTab.YEAR -> "年报筹备中"
        ReportTab.DAY -> "日报"
    }
    val description = when (tab) {
        ReportTab.WEEK -> "等日报快照稳定沉淀后，再开放周趋势与连续性洞察。"
        ReportTab.MONTH -> "月维度会基于日快照做节律、阶段和结构变化分析。"
        ReportTab.YEAR -> "年维度会沉淀成长轨迹、波峰波谷与长期自律表现。"
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
        beforeSleep?.endTime?.let { "${formatClockTime(it, zoneId)} 后" } ?: "样本不足"
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
            label = "睡前最后在用",
            packageName = beforeSleep?.packageName,
            appLabel = beforeSleep?.packageName?.let { resolveAppLabel(context, it, installedAppMap) },
        ),
        afterWake = BehaviorAppMoment(
            label = "起床后先打开",
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
            deviceOverview.totalUsageMillis > averageMetrics.deviceUsageMillis * 1.15f -> "今天手机使用偏重"
        averageMetrics.deviceUsageMillis > 0L &&
            deviceOverview.totalUsageMillis < averageMetrics.deviceUsageMillis * 0.85f -> "今天手机使用偏轻"
        else -> "今天手机使用接近平时"
    }
    val dominantPeriod = periodUsage.maxByOrNull { it.deviceMillis }?.label ?: "全天"
    val intensityTag = when {
        averageMetrics.deviceUsageMillis > 0L &&
            deviceOverview.totalUsageMillis > averageMetrics.deviceUsageMillis * 1.15f -> "重度使用"
        averageMetrics.deviceUsageMillis > 0L &&
            deviceOverview.totalUsageMillis < averageMetrics.deviceUsageMillis * 0.85f -> "使用克制"
        else -> "接近日常"
    }
    val periodTag = "${dominantPeriod}集中"
    val openTag = when {
        yesterdayMetrics.deviceOpenCount > 0 &&
            deviceOverview.openCount > yesterdayMetrics.deviceOpenCount * 1.15f -> "切换偏频繁"
        deviceOverview.openCount == 0 -> "尚无记录"
        else -> "打开节奏正常"
    }
    val formattedDate = date.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))
    val capturedAt = java.time.Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA))
    return DailyReportSummary(
        title = "今日战报",
        subtitle = formattedDate,
        capturedAt = "采样截至 $capturedAt",
        message = "$intensity，主要集中在$dominantPeriod，$openTag。",
        primaryValue = formatDuration(deviceOverview.totalUsageMillis),
        secondaryValue = deltaDescription(deviceOverview.totalUsageMillis, yesterdayMetrics.deviceUsageMillis, "较昨天"),
        tertiaryValue = deltaDescription(deviceOverview.totalUsageMillis, averageMetrics.deviceUsageMillis, "较近 7 日"),
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
            label = "全机总时长",
            todayValue = formatDuration(deviceOverview.totalUsageMillis),
            yesterdayDelta = deltaDescription(deviceOverview.totalUsageMillis, yesterdayMetrics.deviceUsageMillis, "较昨天"),
            averageDelta = deltaDescription(deviceOverview.totalUsageMillis, averageMetrics.deviceUsageMillis, "较均值"),
        ),
        ComparisonMetric(
            label = "打开次数",
            todayValue = "${deviceOverview.openCount} 次",
            yesterdayDelta = deltaDescription(deviceOverview.openCount.toLong(), yesterdayMetrics.deviceOpenCount.toLong(), "较昨天", countUnit = "次"),
            averageDelta = deltaDescription(deviceOverview.openCount.toLong(), averageMetrics.deviceOpenCount.toLong(), "较均值", countUnit = "次"),
        ),
        ComparisonMetric(
            label = "夜间使用",
            todayValue = formatDuration(behaviorInsight.nightUsageMillis),
            yesterdayDelta = deltaDescription(behaviorInsight.nightUsageMillis, yesterdayMetrics.nightUsageMillis, "较昨天"),
            averageDelta = deltaDescription(behaviorInsight.nightUsageMillis, averageMetrics.nightUsageMillis, "较均值"),
        ),
        ComparisonMetric(
            label = "最长单次会话",
            todayValue = formatDuration(longestSessionMillis),
            yesterdayDelta = deltaDescription(longestSessionMillis, yesterdayMetrics.longestSessionMillis, "较昨天"),
            averageDelta = deltaDescription(longestSessionMillis, averageMetrics.longestSessionMillis, "较均值"),
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
        "凌晨" to 0..5,
        "上午" to 6..11,
        "下午" to 12..17,
        "晚间" to 18..23,
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
        peakTwoHourLabel = peakTwoHour?.first?.let { "${it.first().label}-${it.last().hour + 1}鏃?" } ?: "--",
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
            !state.isPermissionGranted -> PermissionRequiredState()
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
) {
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
                }
                if (state.isRefreshing) {
                    LoadingHintChip(selectedTab = state.selectedTab)
                }
                DeviceHeroCard(
                    selectedTab = state.selectedTab,
                    heroState = state.heroState,
                )
                if (state.selectedTab == ReportTab.DAY) {
                    DailyFocusCard(focusState = state.dailyFocusState)
                } else {
                    WindowFocusCard(focusState = state.windowFocusState)
                }
                if (state.selectedTab == ReportTab.YEAR) {
                    YearDualScopeCard(yearState = state.yearDualScopeState)
                }
                TimelineCard(
                    selectedTab = state.selectedTab,
                    timelineState = state.timelineState,
                )
                if (state.selectedTab == ReportTab.MONTH || state.selectedTab == ReportTab.YEAR) {
                    HeatmapCard(heatmapState = state.heatmapState)
                }
                AppChartsCard(
                    selectedTab = state.selectedTab,
                    topAppsState = state.topAppsState,
                )
                BehaviorCard(
                    selectedTab = state.selectedTab,
                    behaviorState = state.behaviorState,
                )
                ComparisonCard(
                    selectedTab = state.selectedTab,
                    comparisonState = state.comparisonState,
                )
                ShareReportCard(
                    selectedTab = state.selectedTab,
                    shareState = state.shareState,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LoadingHintChip(selectedTab: ReportTab) {
    val label =
        when (selectedTab) {
            ReportTab.DAY -> "正在读取归档日报"
            ReportTab.WEEK -> "正在更新近 7 日趋势"
            ReportTab.MONTH -> "正在更新近 30 日趋势"
            ReportTab.YEAR -> "正在准备年报"
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
                    contentDescription = "前一天",
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
                            text = formatArchiveDate(selectedArchiveDate, "M月d日 EEEE"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "只可选择有归档的日期",
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
                    contentDescription = "后一天",
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
                Text("关闭")
            }
        },
        title = {
            Text(
                text = "选择归档日期",
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上个月")
                    }
                    Text(
                        text = displayedMonth.format(DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA)),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(
                        onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                        enabled = displayedMonth < maxMonth,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下个月")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf("一", "二", "三", "四", "五", "六", "日").forEach { dayLabel ->
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
                    text = "没有归档数据的日期不可选。",
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
                        text = "趋势维度待开放",
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
                        text = "当前先把日报做满，趋势页会在日快照能力稳定后开放。",
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
private fun PermissionRequiredState() {
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
                    text = "战报需要读取使用记录权限",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "开启使用记录权限后，日报才能统计手机上的全天使用痕迹、Top 应用和行为趋势。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("去开启权限")
                    }
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("系统设置")
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
                        text = tab.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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
        title = "归档日报",
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
            val dialSize = if (compact) 156.dp else 188.dp
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
                            text = data?.summary?.title ?: "归档日报",
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
                        SkeletonDonutChart(chartSize = dialSize)
                    } else {
                        UsageDialChart(
                            usageMillis = data.overview.totalUsageMillis,
                            activeBucketCount = data.overview.activeBucketCount,
                            capMillis = usageDialCapMillis(selectedTab),
                            modifier = Modifier.size(dialSize),
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
                            label = "全机时长",
                            value = data.summary.primaryValue,
                            modifier = childModifier,
                        )
                        1 -> HeroMetricChip(
                            icon = Icons.AutoMirrored.Filled.CompareArrows,
                            label = if (data.summary.title == "归档日报") "对比上一条" else "对比基线",
                            value = data.summary.secondaryValue,
                            modifier = childModifier,
                        )
                        2 -> HeroMetricChip(
                            icon = Icons.Default.TouchApp,
                            label = "打开次数",
                            value = "${overview.openCount} 次",
                            modifier = childModifier,
                        )
                        else -> HeroMetricChip(
                            icon = Icons.Default.NightsStay,
                            label = "夜间使用",
                            value = formatDuration(data.nightUsageMillis),
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
                    overview?.topApp?.let { topApp ->
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
                                    text = if (data.summary.title == "归档日报") "当日主导应用" else "窗口主导应用",
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
private fun UsageDialChart(
    usageMillis: Long,
    activeBucketCount: Int,
    capMillis: Long,
    modifier: Modifier = Modifier,
) {
    val stagedUsageMillis = rememberDelayedLongTarget(usageMillis, 40)
    val animatedUsageMillis = animateLongValue(
        targetValue = stagedUsageMillis,
        label = "usage_dial_value",
        durationMillis = 880,
    )
    val progress by animateFloatAsState(
        targetValue = (stagedUsageMillis.toFloat() / capMillis.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 180f),
        label = "usage_dial_progress",
    )
    val rotationProgress by animateFloatAsState(
        targetValue = if (stagedUsageMillis > 0L) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "usage_dial_rotation",
    )
    val arcColor = MaterialTheme.colorScheme.primary
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.1f
            val diameter = size.minDimension - stroke
            val startAngle = 145f - (1f - rotationProgress) * 360f
            drawArc(
                color = arcColor.copy(alpha = 0.14f),
                startAngle = startAngle,
                sweepAngle = 250f,
                useCenter = false,
                topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f),
                size = Size(diameter, diameter),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            )
            drawArc(
                color = arcColor,
                startAngle = startAngle,
                sweepAngle = 250f * progress,
                useCenter = false,
                topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f),
                size = Size(diameter, diameter),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = formatDuration(animatedUsageMillis),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$activeBucketCount 个活跃小时",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                    title = "管控与鼓励复盘",
                    subtitle = "把限制、奖励、积分和重点分组收拢到一个窗口里看。",
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
                    title = "年度双口径",
                    subtitle = "自然年看正式总结，近 365 天看长期惯性。",
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
                        0 -> DailyFocusMetric("日均", summary.averageUsage)
                        1 -> DailyFocusMetric("活跃", summary.activeDays)
                        2 -> DailyFocusMetric("节省", summary.savedUsage)
                        else -> DailyFocusMetric("净积分", summary.pointsNet)
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
                SectionHeader(Icons.Default.CalendarMonth, "热力分布", "暂无足够归档生成热力图。")
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
    ReportCard {
        SectionHeader(
            icon = Icons.Default.Share,
            title = "分享战报",
            subtitle = "生成一张当前${selectedTab.label}摘要海报，适合直接发给朋友或留作记录。",
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
                        onClick = {
                            runCatching {
                                shareReportImage(
                                    context = context,
                                    data = data,
                                    primary = primary,
                                    surface = surface,
                                    onSurface = onSurface,
                                    onSurfaceVariant = onSurfaceVariant,
                                    palette = reportColors.appChartPalette,
                                )
                            }.onFailure { error ->
                                Toast.makeText(context, error.message ?: "分享图生成失败", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成并分享 PNG")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyModeSummaryCard(
    summary: DailyModeSummary,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val reportColors = LocalReportColors.current
    val accent =
        when {
            summary.isWarning -> reportColors.warning
            summary.title == "鼓励进度" -> reportColors.positive
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.16f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = summary.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        GradientProgressBar(
            progress = summary.progress,
            color = accent,
            delayMillis = 160,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            summary.metrics.forEachIndexed { index, metric ->
                FocusMetricPill(
                    metric = metric,
                    accent = accent,
                    delayMillis = 180 + index * 40,
                )
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
private fun FocusMetricPill(
    metric: DailyFocusMetric,
    accent: Color,
    delayMillis: Int,
    modifier: Modifier = Modifier,
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
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                title = if (selectedTab == ReportTab.DAY) "24 小时分布" else "归档趋势",
            )
            when (timelineState) {
                SectionState.Loading -> SkeletonTimelineChart()
                SectionState.Empty -> DailyTimelineChart(emptyList())
                is SectionState.Ready -> DailyTimelineChart(timelineState.data.buckets)
            }
            TimelineFooter(
                labels =
                    buildTimelineFooterLabels(
                        selectedTab = selectedTab,
                        buckets = (timelineState as? SectionState.Ready)?.data?.buckets.orEmpty(),
                    ),
            )
            AdaptiveRowGrid(
                itemCount = 2,
                compactColumns = 1,
                expandedColumns = 2,
                horizontalSpacing = 14.dp,
                verticalSpacing = 14.dp,
            ) { modifier, index ->
                when (timelineState) {
                    SectionState.Loading -> {
                        if (index == 0) {
                            SkeletonDonutPanel(modifier = modifier)
                        } else {
                            SkeletonPeakPanel(modifier = modifier)
                        }
                    }
                    SectionState.Empty -> {
                        if (index == 0) {
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
                    is SectionState.Ready -> when (index) {
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
                val maxUsage = buckets.maxOfOrNull { it.deviceMillis }?.coerceAtLeast(1L) ?: 1L
                listOf(maxUsage, maxUsage * 2 / 3, maxUsage / 3, 0L).forEach { tick ->
                    Text(
                        text = if (tick == 0L) "0" else formatAxisDuration(tick),
                        style = MaterialTheme.typography.labelSmall,
                        color = axisTextColor,
                        maxLines = 1,
                    )
                }
            }
            Canvas(modifier = Modifier.weight(1f).height(chartHeight)) {
                if (buckets.isEmpty()) return@Canvas
                val deviceMax = buckets.maxOfOrNull { it.deviceMillis }?.coerceAtLeast(1L) ?: 1L
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
                    val rawHeight = size.height * (bucket.deviceMillis.toFloat() / deviceMax.toFloat()).coerceIn(0f, 1f)
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
                    text = "时段热力",
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
                text = "峰值时刻",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (timelineState == null) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) "这个归档日的样本还不足以判断峰值。" else "这个归档窗口的样本还不足以判断峰值。",
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
                            label = "峰值 1h",
                            value = "${behaviorInsight.peakHourLabel} · ${formatDuration(behaviorInsight.peakHourMillis)}",
                            modifier = modifier,
                        )
                        1 -> MiniInsightCard(
                            icon = Icons.AutoMirrored.Filled.CallSplit,
                            label = "连续 2h",
                            value = "${behaviorInsight.peakTwoHourLabel} · ${formatDuration(behaviorInsight.peakTwoHourMillis)}",
                            modifier = modifier,
                        )
                        else -> MiniInsightCard(
                            icon = Icons.Default.NightsStay,
                            label = "夜间",
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
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.width(28.dp),
                shape = RoundedCornerShape(999.dp),
                color = color.copy(alpha = if (isTopRank) 0.22f else 0.14f),
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 6.dp),
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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                title = "前 10 个应用",
                subtitle = if (selectedTab == ReportTab.DAY) "只看当前归档日使用时长最高的 10 个应用。" else "只看当前归档窗口内使用时长最高的 10 个应用。",
            )
            if (topAppsState == SectionState.Loading) {
                SkeletonUsageSharePanel()
                SkeletonRankingPanel()
            } else if (topAppsState == SectionState.Empty || usageTopApps.isEmpty()) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) "这个归档日还没有足够的使用记录。" else "这个归档窗口还没有足够的使用记录。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AppUsageShareCard(
                    items = usageTopApps,
                    appColors = appColors,
                )
                TopUsageRankingCard(
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
            val donutSize = if (compact) 156.dp else 186.dp
            val shareChipCount = if (compact) minOf(items.size, 4) else minOf(items.size, 6)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "使用时长占比",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (items.isEmpty()) {
                    Text(
                        text = "还没有足够的前台使用记录。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val total = items.sumOf { it.value }.coerceAtLeast(1L)
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
                                values = items.map { it.value },
                                colors = items.mapIndexed { index, item -> appColors[item.packageName] ?: fallbackChartColor(index) },
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
                                    text = "前 10 个应用总时长",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    AdaptiveRowGrid(
                        itemCount = shareChipCount,
                        compactColumns = 4,
                        expandedColumns = 6,
                        horizontalSpacing = 6.dp,
                        verticalSpacing = 6.dp,
                    ) { chipModifier, index ->
                        val item = items[index]
                        val color = appColors[item.packageName] ?: fallbackChartColor(index)
                        AppShareChip(
                            share = item.value.toFloat() / total.toFloat(),
                            packageName = item.packageName,
                            color = color,
                            delayMillis = 360 + index * 30,
                            modifier = chipModifier,
                        )
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
                text = "时长排名",
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
                title = "行为分析",
                subtitle = if (selectedTab == ReportTab.DAY) "只展示已归档数据能够直接支撑的行为指标。" else "当前只保留由归档数据稳定支持的分析项。",
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
                    text = if (selectedTab == ReportTab.DAY) "这个归档日还没有足够的行为样本。" else "当前窗口还没有足够的行为样本。",
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
                            label = "最长单次会话",
                            value = insight.longestSession?.let { session -> "${session.label} · ${formatDuration(session.value)}" } ?: "暂无",
                            visualRatio = ((insight.longestSession?.value ?: 0L).toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        1 -> MiniInsightCard(
                            icon = Icons.Default.AccessTime,
                            label = "平均单次时长",
                            value = formatDuration(insight.averageSessionMillis),
                            visualRatio = (insight.averageSessionMillis.toFloat() / (30 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        2 -> MiniInsightCard(
                            icon = Icons.Default.Timeline,
                            label = if (selectedTab == ReportTab.DAY) "高峰时段" else "高峰时段",
                            value = "${insight.peakHourLabel} · ${formatDuration(insight.peakHourMillis)}",
                            visualRatio = (insight.peakHourMillis.toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        3 -> MiniInsightCard(
                            icon = Icons.Default.RocketLaunch,
                            label = if (selectedTab == ReportTab.DAY) "活跃小时" else "重复打开强度",
                            value = if (selectedTab == ReportTab.DAY) "${insight.activeHourCount} 小时" else String.format(Locale.CHINA, "%.1f 次/活跃小时", insight.reopenIntensity),
                            visualRatio = if (selectedTab == ReportTab.DAY) (insight.activeHourCount / 24f).coerceIn(0f, 1f) else (insight.reopenIntensity / 6f).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        4 -> MiniInsightCard(
                            icon = Icons.Default.NightsStay,
                            label = if (selectedTab == ReportTab.DAY) "夜间使用" else "预测睡眠时间",
                            value = if (selectedTab == ReportTab.DAY) formatDuration(insight.nightUsageMillis) else "${insight.predictedSleepLabel} · ${insight.predictedSleepDurationLabel}",
                            visualRatio = null,
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
                            appLabel = insight.beforeSleep.appLabel ?: "暂无记录",
                            packageName = insight.beforeSleep.packageName,
                            modifier = modifier,
                        )
                        else -> BehaviorMomentCard(
                            icon = Icons.Default.WbSunny,
                            title = insight.afterWake.label,
                            appLabel = insight.afterWake.appLabel ?: "暂无记录",
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
                title = if (selectedTab == ReportTab.DAY) "归档对比" else "窗口对比",
                subtitle = if (selectedTab == ReportTab.DAY) "只比较当前归档日与上一条归档之间最稳定的指标。" else "只比较归档窗口里最稳定的核心指标。",
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
                    text = if (selectedTab == ReportTab.DAY) "更早的归档样本还不够，暂时不展示对比。" else "当前窗口样本还不够，暂时不展示对比。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val comparisons = (comparisonState as SectionState.Ready).data.comparisons
                comparisons.forEachIndexed { index, item ->
                    ComparisonRow(
                        item = item,
                        delayMillis = 660 + index * 50,
                    )
                    if (index != comparisons.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }
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
) {
    val animatedTodayValue = animateMetricDisplayText(
        rawText = item.todayValue,
        label = "comparison_${item.label.hashCode()}",
        delayMillis = delayMillis,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = item.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(text = animatedTodayValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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

private fun shareReportImage(
    context: Context,
    data: ShareReportData,
    primary: Color,
    surface: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    palette: List<Color>,
) {
    val bitmap = renderShareReportBitmap(
        data = data,
        primary = primary,
        surface = surface,
        onSurface = onSurface,
        onSurfaceVariant = onSurfaceVariant,
        palette = palette,
    )
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
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(intent, "分享战报"))
}

private fun renderShareReportBitmap(
    data: ShareReportData,
    primary: Color,
    surface: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    palette: List<Color>,
): Bitmap {
    val width = 1080
    val height = 1600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val primaryArgb = primary.toArgb()
    val surfaceArgb = surface.toArgb()
    val textArgb = onSurface.toArgb()
    val mutedArgb = onSurfaceVariant.toArgb()
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader =
            android.graphics.LinearGradient(
                0f,
                0f,
                0f,
                height.toFloat(),
                intArrayOf(surfaceArgb, primary.copy(alpha = 0.18f).toArgb(), surfaceArgb),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
    val softPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.12f).toArgb() }
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 58f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 30f
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 28f
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryArgb
        textSize = 104f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 34f
    }
    canvas.drawRoundRect(RectF(64f, 76f, width - 64f, height - 76f), 54f, 54f, cardPaint)
    canvas.drawRoundRect(RectF(104f, 116f, width - 104f, 520f), 42f, 42f, softPaint)
    canvas.drawText(data.title, 136f, 190f, titlePaint)
    canvas.drawText(data.subtitle, 136f, 242f, subtitlePaint)
    canvas.drawText(data.primaryLabel, 136f, 330f, labelPaint)
    canvas.drawText(data.primaryValue, 136f, 438f, valuePaint)

    var y = 620f
    data.metrics.take(3).forEachIndexed { index, metric ->
        val left = 104f + index * 300f
        val right = left + 276f
        val color = palette.getOrNull(index)?.copy(alpha = 0.18f)?.toArgb() ?: primary.copy(alpha = 0.14f).toArgb()
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawRoundRect(RectF(left, y, right, y + 150f), 30f, 30f, pillPaint)
        canvas.drawText(metric.label, left + 26f, y + 52f, labelPaint)
        drawEllipsizedText(canvas, metric.value, left + 26f, y + 108f, 220f, bodyPaint)
    }

    y += 250f
    val insightTitlePaint = Paint(titlePaint).apply { textSize = 42f }
    canvas.drawText("复盘高光", 104f, y, insightTitlePaint)
    y += 58f
    drawMultilineText(canvas, data.insight, 104f, y, width - 208f, bodyPaint, 48f, 3)
    y += 210f
    canvas.drawText("Top 应用", 104f, y, insightTitlePaint)
    y += 70f
    data.topApps.take(3).forEachIndexed { index, app ->
        val rankPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.getOrNull(index)?.toArgb() ?: primaryArgb
            textSize = 42f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        canvas.drawText("${index + 1}", 112f, y, rankPaint)
        drawEllipsizedText(canvas, app.label, 170f, y, 430f, bodyPaint)
        canvas.drawText(formatDuration(app.value), 780f, y, bodyPaint)
        y += 64f
    }
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 28f
    }
    canvas.drawText("Tiny Vow · 把注意力还给生活", 104f, height - 144f, footerPaint)
    return bitmap
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
    if (baseline <= 0L && current <= 0L) return "$prefix 持平"
    val delta = current - baseline
    if (delta == 0L) return "$prefix 持平"
    val direction = if (delta > 0L) "增加" else "减少"
    val deltaValue = countUnit?.let { "${kotlin.math.abs(delta)} $it" } ?: formatDuration(kotlin.math.abs(delta))
    return "$prefix $direction $deltaValue"
}

private fun Double.roundToLongSafe(): Long {
    return if (this.isNaN()) 0L else roundToLong()
}
