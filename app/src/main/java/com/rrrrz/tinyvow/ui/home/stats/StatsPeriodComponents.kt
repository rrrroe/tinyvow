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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Analytics
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.LocalReportColors
import com.rrrrz.tinyvow.ui.theme.ReportColors
import com.rrrrz.tinyvow.ui.theme.TinyVowIconSurface
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
import java.time.format.TextStyle
import java.util.Locale
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
internal fun PeriodReportScreen(
    state: DailyReportUiState,
    animateValues: Boolean = false,
    offlineFocusEnabled: Boolean = true,
    modules: List<SharePosterModule>? = null,
) {
    when (val periodState = state.periodReportState) {
        SectionState.Loading -> Unit
        SectionState.Empty -> {
            Text(
                text = AppText.t("stats_not_enough_samples"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        is SectionState.Ready -> {
            val data = periodState.data
            (modules ?: availablePeriodReportModules(data, offlineFocusEnabled)).forEach { module ->
                PeriodReportModuleContent(
                    data = data,
                    module = module,
                    animateValues = animateValues,
                )
            }
        }
    }
}

internal fun availablePeriodReportModules(
    data: PeriodReportData,
    offlineFocusEnabled: Boolean,
): List<SharePosterModule> =
    when (data.tab) {
        ReportTab.WEEK,
        ReportTab.MONTH ->
            buildList {
                add(SharePosterModule.BEHAVIOR)
                add(SharePosterModule.FOCUS)
                add(SharePosterModule.RHYTHM)
                if (data.weeklyPoints != null) add(SharePosterModule.POINTS)
                add(SharePosterModule.INSIGHTS)
                if (offlineFocusEnabled) add(SharePosterModule.OFFLINE)
            }
        ReportTab.YEAR ->
            buildList {
                add(SharePosterModule.OVERVIEW)
                add(SharePosterModule.FOCUS)
                if (offlineFocusEnabled) add(SharePosterModule.OFFLINE)
                if (data.heatmap != null) add(SharePosterModule.HEATMAP)
                add(SharePosterModule.TREND)
                if (data.monthStructure != null || data.quarterSection != null) add(SharePosterModule.STRUCTURE)
                add(SharePosterModule.APPS)
                add(SharePosterModule.INSIGHTS)
            }
        ReportTab.DAY -> emptyList()
    }

@Composable
private fun PeriodReportModuleContent(
    data: PeriodReportData,
    module: SharePosterModule,
    animateValues: Boolean,
) {
    when (module) {
        SharePosterModule.BEHAVIOR -> {
            if (data.tab == ReportTab.WEEK || data.tab == ReportTab.MONTH) {
                WeeklyBehaviorProfileCard(data = data)
            }
        }
        SharePosterModule.FOCUS -> {
            if (data.tab == ReportTab.WEEK || data.tab == ReportTab.MONTH) {
                DailyFocusCard(
                    focusState =
                        SectionState.Ready(
                            DailyFocusSectionData(
                                control = data.windowFocus.control,
                                encourage = data.windowFocus.encourage,
                            ),
                        ),
                    animateValues = animateValues,
                )
            } else {
                PeriodFocusCard(
                    data = data.windowFocus,
                    animateValues = animateValues,
                )
            }
        }
        SharePosterModule.RHYTHM -> {
            if (data.tab == ReportTab.WEEK || data.tab == ReportTab.MONTH) {
                DailyRhythmCard(
                    timelineState =
                        data.timeline
                            ?.takeIf { timeline -> timeline.buckets.any { it.deviceMillis > 0L } }
                            ?.let { SectionState.Ready(it) }
                            ?: SectionState.Empty,
                    subtitle =
                        AppText.t(
                            if (data.tab == ReportTab.MONTH) {
                                "stats_monthly_time_flow_description"
                            } else {
                                "stats_weekly_time_flow_description"
                            },
                        ),
                    weeklyAppFocusDays = data.weeklyAppFocusDays,
                    behaviorMapData = data.behaviorMap,
                )
            }
        }
        SharePosterModule.POINTS -> {
            data.weeklyPoints?.let { WeeklyPointsTrajectoryCard(it, tab = data.tab) }
        }
        SharePosterModule.INSIGHTS -> {
            if (data.tab == ReportTab.WEEK || data.tab == ReportTab.MONTH) {
                DailyInsightCard(
                    behaviorMapState = data.behaviorMap?.let { SectionState.Ready(it) } ?: SectionState.Empty,
                )
            } else {
                PeriodInsightSection(data = data)
            }
        }
        SharePosterModule.OFFLINE -> {
            if (data.tab == ReportTab.WEEK) {
                WeeklyOfflineFocusPebblesCard(
                    state = SectionState.Ready(data.offlineFocus),
                    emptyMessage = AppText.t("offline_focus_weekly_empty"),
                )
            } else if (data.tab == ReportTab.MONTH) {
                MonthlyOfflineFocusPebblesCard(
                    state = SectionState.Ready(data.offlineFocus),
                    emptyMessage = AppText.t("offline_focus_monthly_empty"),
                )
            } else {
                OfflineFocusDailyCard(state = SectionState.Ready(data.offlineFocus))
            }
        }
        SharePosterModule.OVERVIEW -> {
            if (data.tab == ReportTab.MONTH || data.tab == ReportTab.YEAR) {
                PeriodHeroCard(
                    hero = data.hero,
                    animateValues = animateValues,
                )
            }
        }
        SharePosterModule.HEATMAP -> {
            if (data.tab == ReportTab.MONTH || data.tab == ReportTab.YEAR) {
                data.heatmap?.let { PeriodHeatmapCard(it) }
            }
        }
        SharePosterModule.TREND -> {
            if (data.tab == ReportTab.MONTH || data.tab == ReportTab.YEAR) PeriodTrendCard(data.trend)
        }
        SharePosterModule.STRUCTURE -> {
            when (data.tab) {
                ReportTab.MONTH -> data.monthStructure?.let { PeriodMonthStructureCard(it) }
                ReportTab.YEAR -> data.quarterSection?.let { PeriodQuarterBreakdownCard(it) }
                ReportTab.DAY,
                ReportTab.WEEK -> Unit
            }
        }
        SharePosterModule.APPS -> {
            if (data.tab == ReportTab.MONTH || data.tab == ReportTab.YEAR) PeriodAppFocusCard(data.appFocus)
        }
        SharePosterModule.TIME_TIDE -> Unit
    }
}

@Composable
private fun WeeklyBehaviorProfileCard(data: PeriodReportData) {
    val structure = data.behavior?.structure
    val themeColors = LocalThemeColors.current
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            DailyReportSectionHeader(
                icon = Icons.Default.Analytics,
                title = AppText.t("stats_behavior_analysis"),
                subtitle = AppText.t("stats_behavior_structure_description"),
                accent = themeColors.base,
            )
            if (structure == null || structure.scoreMetrics.isEmpty()) {
                Text(
                    text = AppText.t("stats_this_archived_window_does_not_have_enough_behavior"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val savedAccent = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                BehaviorRadarPanel(
                    metrics = structure.scoreMetrics,
                    comparisonMetrics = structure.comparisonScoreMetrics,
                    cornerMetrics =
                        listOf(
                            BehaviorCornerMetric(
                                label = AppText.t("stats_behavior_corner_usage"),
                                value = formatBehaviorMetricMinutes(data.totalUsageMillis),
                                unit = AppText.t("stats_behavior_unit_minutes_short"),
                                accent = themeColors.base,
                                rawMillis = data.totalUsageMillis,
                                align = Alignment.TopStart,
                            ),
                            BehaviorCornerMetric(
                                label = AppText.t("stats_behavior_corner_investment"),
                                value = formatBehaviorMetricMinutes(data.encourageUsageMillis),
                                unit = AppText.t("stats_behavior_unit_minutes_short"),
                                accent = themeColors.encourage,
                                rawMillis = data.encourageUsageMillis,
                                align = Alignment.TopEnd,
                            ),
                            BehaviorCornerMetric(
                                label = AppText.t("stats_behavior_corner_control"),
                                value = formatBehaviorMetricMinutes(data.controlUsageMillis),
                                unit = AppText.t("stats_behavior_unit_minutes_short"),
                                accent = themeColors.control,
                                rawMillis = data.controlUsageMillis,
                                align = Alignment.BottomStart,
                            ),
                            BehaviorCornerMetric(
                                label = AppText.t("stats_behavior_corner_savings"),
                                value = formatBehaviorMetricMinutes(data.savedMillis),
                                unit = AppText.t("stats_behavior_unit_minutes_short"),
                                accent = savedAccent,
                                rawMillis = data.savedMillis,
                                align = Alignment.BottomEnd,
                            ),
                        ),
                    totalMetric =
                        BehaviorTotalMetric(
                            label = AppText.t("stats_behavior_total_score"),
                            value = structure.scoreMetrics.map { it.score }.average().roundToInt().toString(),
                            unit = "",
                            accent = themeColors.base,
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PeriodFocusCard(
    data: WindowFocusSectionData,
    animateValues: Boolean = false,
) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AdaptiveRowGrid(
                itemCount = 2,
                compactColumns = 2,
                expandedColumns = 2,
                horizontalSpacing = 12.dp,
                verticalSpacing = 12.dp,
            ) { modifier, index ->
                DailyModeSummaryCard(
                    summary = if (index == 0) data.control else data.encourage,
                    icon = if (index == 0) Icons.Default.Bolt else Icons.Default.RocketLaunch,
                    compact = true,
                    animateValues = animateValues,
                    modifier = modifier,
                )
            }
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PeriodHeroCard(
    hero: PeriodHeroData,
    animateValues: Boolean = false,
) {
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
                        if (hero.eyebrow.isNotBlank()) {
                            Text(
                                text = hero.eyebrow,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = hero.title,
                            style = MaterialTheme.typography.titleLarge,
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
                if (animateValues) {
                    AnimatedMetricText(
                        rawText = hero.primaryValue,
                        label = "period_hero_primary_${hero.title}_${hero.rangeLabel}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        delayMillis = 80,
                    )
                } else {
                    Text(
                        text = hero.primaryValue,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = hero.message,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                ) { modifier, index ->
                    BattleHeadlineChip(
                        label = if (index == 0) AppText.t("stats_vs_previous_period_decreased") else hero.averageLabel,
                        value = if (index == 0) hero.comparisonValue else hero.tertiaryValue,
                        accent = if (index == 0) reportColors.danger else reportColors.positive,
                        animateValue = animateValues,
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
                        animateValue = animateValues,
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
internal fun PeriodTrendCard(data: TrendSectionData) {
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
internal fun WeeklyPointsTrajectoryCard(
    data: WeeklyPointsSectionData,
    tab: ReportTab = ReportTab.WEEK,
) {
    val themeColors = LocalThemeColors.current
    var selectedDayIndex by remember(data.days) { mutableStateOf<Int?>(null) }
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            DailyReportSectionHeader(
                icon = Icons.AutoMirrored.Filled.ShowChart,
                title = AppText.t("stats_weekly_points_trajectory"),
                subtitle = AppText.t("stats_weekly_points_trajectory_description"),
                accent = themeColors.encourage,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PointsLegendValue(
                        color = themeColors.save,
                        label = AppText.t("stats_weekly_points_income"),
                        value = formatSignedPointsLocal(data.earnedPoints),
                    )
                    PointsLegendValue(
                        color = themeColors.restraint,
                        label = AppText.t("stats_weekly_points_expense"),
                        value = formatSignedPointsLocal(-data.spentPoints),
                    )
                    PointsLegendItem(themeColors.encourage, AppText.t("stats_weekly_points_cumulative"), line = true)
                }
            }
            WeeklyPointsTrajectoryChart(
                days = data.days,
                selectedDayIndex = selectedDayIndex,
                onDaySelected = { dayIndex ->
                    selectedDayIndex = dayIndex.takeUnless { it == selectedDayIndex }
                },
            )
            selectedDayIndex?.let { data.days.getOrNull(it) }?.let { day ->
                WeeklyPointsDayDetail(day = day)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = themeColors.encourage.copy(alpha = 0.055f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TinyVowIconSurface(
                        icon = Icons.Default.Insights,
                        contentDescription = null,
                        containerColor = themeColors.encourage.copy(alpha = 0.14f),
                        contentColor = themeColors.encourage,
                        size = 34.dp,
                        iconSize = 18.dp,
                    )
                    Text(
                        text =
                            if (data.netPoints >= 0.0) {
                                AppText.t(
                                    if (tab == ReportTab.MONTH) "stats_monthly_points_net_gain" else "stats_weekly_points_net_gain",
                                    formatSignedPointsLocal(data.netPoints),
                                )
                            } else {
                                AppText.t(
                                    if (tab == ReportTab.MONTH) "stats_monthly_points_net_loss" else "stats_weekly_points_net_loss",
                                    formatSignedPointsLocal(data.netPoints),
                                )
                            },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyPointsTrajectoryChart(
    days: List<WeeklyPointsDay>,
    selectedDayIndex: Int?,
    onDaySelected: (Int) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
    val balanceColor = themeColors.encourage
    val earnedColor = themeColors.save
    val spentColor = themeColors.restraint
    val surfaceColor = MaterialTheme.colorScheme.surface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val isMonthly = days.size > 7
    val populatedDayIndexes =
        days.indices.filter { index ->
            val day = days[index]
            day.earnedPoints > 0.0 || day.spentPoints > 0.0
        }
    val balances =
        if (isMonthly) {
            populatedDayIndexes.map { days[it].closingBalance }
        } else {
            days.map { it.closingBalance }
        }
    val rawMinBalance = balances.minOrNull() ?: 0.0
    val rawMaxBalance = balances.maxOrNull() ?: 0.0
    val rawBalanceRange = (rawMaxBalance - rawMinBalance).coerceAtLeast(1.0)
    val balancePadding = max(1.0, rawBalanceRange * 0.18)
    val minBalance = rawMinBalance - balancePadding
    val maxBalance = rawMaxBalance + balancePadding
    val balanceRange = (maxBalance - minBalance).coerceAtLeast(1.0)
    val maxFlow = days.maxOfOrNull { max(it.earnedPoints, it.spentPoints) }?.coerceAtLeast(1.0) ?: 1.0
    val maxEarnedDayIndex =
        days.indices.maxByOrNull { days[it].earnedPoints }?.takeIf { days[it].earnedPoints > 0.0 }
    val maxSpentDayIndex =
        days.indices.maxByOrNull { days[it].spentPoints }?.takeIf { days[it].spentPoints > 0.0 }
    val density = LocalDensity.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = AppText.t("stats_weekly_points_daily_net_axis"),
            style = MaterialTheme.typography.labelSmall,
            color = balanceColor,
        )
        Text(
            text = AppText.t("stats_weekly_points_cumulative_axis"),
            style = MaterialTheme.typography.labelSmall,
            color = balanceColor,
        )
    }
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(250.dp)
                .pointerInput(days) {
                    detectTapGestures { position ->
                        if (days.isEmpty()) return@detectTapGestures
                        val left = with(density) { 48.dp.toPx() }
                        val right = size.width - with(density) { 50.dp.toPx() }
                        val ratio = ((position.x - left) / (right - left).coerceAtLeast(1f)).coerceIn(0f, 1f)
                        onDaySelected((ratio * (days.size - 1)).roundToInt().coerceIn(0, days.lastIndex))
                    }
                },
    ) {
        val leftGutter = 48.dp.toPx()
        val rightGutter = 50.dp.toPx()
        val top = 18.dp.toPx()
        val bottom = size.height - 8.dp.toPx()
        val plotLeft = leftGutter
        val plotRight = size.width - rightGutter
        val plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)
        val plotHeight = bottom - top
        val zeroY = top + plotHeight * 0.48f
        val halfFlowHeight = minOf(zeroY - top, bottom - zeroY) * 0.84f
        val step = if (days.size <= 1) plotWidth else plotWidth / (days.size - 1).toFloat()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor.toArgb()
            textSize = 10.dp.toPx()
        }
        repeat(3) { index ->
            val y = top + plotHeight * index / 2f
            drawLine(gridColor, Offset(plotLeft, y), Offset(plotRight, y), strokeWidth = 1.dp.toPx())
        }
        drawLine(
            color = gridColor,
            start = Offset(plotLeft, zeroY),
            end = Offset(plotRight, zeroY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 6.dp.toPx())),
        )
        drawContext.canvas.nativeCanvas.apply {
            textPaint.textAlign = Paint.Align.RIGHT
            drawText(formatPointsChart(maxFlow), plotLeft - 11.dp.toPx(), top + 4.dp.toPx(), textPaint)
            drawText("0", plotLeft - 11.dp.toPx(), zeroY + 4.dp.toPx(), textPaint)
            drawText("−${formatPointsChart(maxFlow)}", plotLeft - 11.dp.toPx(), bottom, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            if (!isMonthly) {
                drawText(formatPointsChart(maxBalance), plotRight + 11.dp.toPx(), top + 4.dp.toPx(), textPaint)
                drawText(formatPointsChart((maxBalance + minBalance) / 2.0), plotRight + 11.dp.toPx(), top + plotHeight / 2f + 4.dp.toPx(), textPaint)
                drawText(formatPointsChart(minBalance), plotRight + 11.dp.toPx(), bottom, textPaint)
            }
        }
        val curvePointEntries =
            days.mapIndexedNotNull { index, day ->
                if (isMonthly && index !in populatedDayIndexes) return@mapIndexedNotNull null
                val x = plotLeft + if (days.size == 1) plotWidth / 2f else step * index
                val normalized = ((day.closingBalance - minBalance) / balanceRange).toFloat()
                index to Offset(x, bottom - normalized * plotHeight)
            }
        val curvePoints = curvePointEntries.map { it.second }
        val path = smoothPointsPath(curvePoints)
        val fillPath = Path().apply {
            addPath(path)
            curvePoints.lastOrNull()?.let { lineTo(it.x, bottom) }
            curvePoints.firstOrNull()?.let { lineTo(it.x, bottom) }
            close()
        }
        if (curvePoints.size >= 2) {
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(listOf(balanceColor.copy(alpha = 0.18f), balanceColor.copy(alpha = 0.015f)), top, bottom),
            )
        }
        days.forEachIndexed { index, day ->
            val x = plotLeft + if (days.size == 1) plotWidth / 2f else step * index
            val barWidth = 22.dp.toPx().coerceAtMost(step * 0.48f)
            val earnedHeight = (day.earnedPoints / maxFlow).toFloat() * halfFlowHeight
            val spentHeight = (day.spentPoints / maxFlow).toFloat() * halfFlowHeight
            if (index == selectedDayIndex) {
                drawRoundRect(
                    color = balanceColor.copy(alpha = 0.08f),
                    topLeft = Offset(x - step * 0.38f, top),
                    size = Size(step * 0.76f, plotHeight),
                    cornerRadius = CornerRadius(10.dp.toPx()),
                )
            }
            if (day.earnedPoints > 0.0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(earnedColor.copy(alpha = 0.78f), earnedColor.copy(alpha = 0.35f))),
                    topLeft = Offset(x - barWidth / 2f, zeroY - earnedHeight),
                    size = Size(barWidth, earnedHeight),
                    cornerRadius = CornerRadius(7.dp.toPx()),
                )
            }
            if (day.spentPoints > 0.0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(spentColor.copy(alpha = 0.42f), spentColor.copy(alpha = 0.76f))),
                    topLeft = Offset(x - barWidth / 2f, zeroY),
                    size = Size(barWidth, spentHeight),
                    cornerRadius = CornerRadius(7.dp.toPx()),
                )
            }
            drawContext.canvas.nativeCanvas.apply {
                textPaint.textAlign = Paint.Align.CENTER
                if (isMonthly) {
                    textPaint.textSize = 9.dp.toPx()
                    textPaint.isFakeBoldText = true
                    if (index == maxEarnedDayIndex) {
                        textPaint.color = earnedColor.toArgb()
                        drawText(formatPointsChart(day.earnedPoints), x, zeroY - earnedHeight - 6.dp.toPx(), textPaint)
                    }
                    if (index == maxSpentDayIndex) {
                        textPaint.color = spentColor.toArgb()
                        drawText(formatPointsChart(day.spentPoints), x, zeroY + spentHeight + 11.dp.toPx(), textPaint)
                    }
                    textPaint.isFakeBoldText = false
                } else {
                    textPaint.color = earnedColor.toArgb()
                    textPaint.textSize = 9.dp.toPx()
                    textPaint.isFakeBoldText = true
                    drawText(formatSignedPointsLocal(day.earnedPoints - day.spentPoints), x, zeroY - earnedHeight - 7.dp.toPx(), textPaint)
                    textPaint.isFakeBoldText = false
                    textPaint.textSize = 8.dp.toPx()
                    if (day.earnedPoints > 0.0) drawText(formatPointsChart(day.earnedPoints), x, zeroY - earnedHeight / 2f + 3.dp.toPx(), textPaint)
                    textPaint.color = spentColor.toArgb()
                    if (day.spentPoints > 0.0) drawText(formatPointsChart(day.spentPoints), x, zeroY + spentHeight + 11.dp.toPx(), textPaint)
                }
            }
        }
        if (curvePoints.isNotEmpty()) {
            drawPath(path, color = balanceColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx()))
        }
        curvePointEntries.forEach { (_, point) ->
            drawCircle(color = surfaceColor, radius = 4.dp.toPx(), center = point)
            drawCircle(color = balanceColor, radius = 2.2.dp.toPx(), center = point)
        }
        if (isMonthly && curvePointEntries.isNotEmpty()) {
            val minEntry = curvePointEntries.minByOrNull { (index, _) -> days[index].closingBalance }
            val maxEntry = curvePointEntries.maxByOrNull { (index, _) -> days[index].closingBalance }
            listOfNotNull(minEntry, maxEntry)
                .distinctBy { it.first }
                .forEach { (index, point) ->
                    drawContext.canvas.nativeCanvas.apply {
                        textPaint.textAlign = Paint.Align.CENTER
                        textPaint.color = balanceColor.toArgb()
                        textPaint.textSize = 9.dp.toPx()
                        textPaint.isFakeBoldText = true
                        val isMaximum = index == maxEntry?.first
                        val labelY =
                            if (isMaximum) {
                                (point.y - 8.dp.toPx()).coerceAtLeast(top + 9.dp.toPx())
                            } else {
                                (point.y + 14.dp.toPx()).coerceAtMost(bottom)
                            }
                        drawText(formatPointsChart(days[index].closingBalance), point.x, labelY, textPaint)
                        textPaint.isFakeBoldText = false
                    }
                }
        }
    }
    val labelIndexes =
        if (days.size <= 7) {
            days.indices.toList()
        } else {
            listOf(0, 4, 9, 14, 19, 24, days.lastIndex).distinct().filter { it in days.indices }
        }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 48.dp, end = 50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        labelIndexes.forEach { index ->
            val day = days[index]
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = day.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                if (days.size <= 7) {
                    Text(text = day.dateLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun WeeklyPointsDayDetail(day: WeeklyPointsDay) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = AppText.t("stats_weekly_points_day_detail", day.label, day.dateLabel),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (day.focusSummaries.isEmpty() && day.groupSummaries.isEmpty() && day.rewardSpends.isEmpty()) {
                Text(
                    text = AppText.t("stats_weekly_points_day_empty"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                if (day.focusSummaries.isNotEmpty()) {
                    WeeklyPointsDetailSectionTitle(AppText.t("stats_weekly_points_detail_focus"))
                }
                day.focusSummaries.forEach { focus ->
                    WeeklyPointsCompactDetailRow(
                        label =
                            "${focus.name} · ${formatDuration(focus.durationMillis)} · " +
                                AppText.t("stats_weekly_points_session_count", focus.sessionCount),
                        value = formatSignedPointsLocal(focus.pointDelta),
                        color = themeColors.save,
                    )
                }
                if (day.groupSummaries.isNotEmpty()) {
                    WeeklyPointsDetailSectionTitle(AppText.t("stats_weekly_points_detail_groups"))
                }
                day.groupSummaries.forEach { group ->
                    WeeklyPointsCompactDetailRow(
                        label =
                            "${group.name} · ${formatDuration(group.durationMillis)} · " +
                                AppText.t(
                                    if (group.completed) "stats_weekly_points_group_completed" else "stats_weekly_points_group_not_completed",
                                ),
                        value = formatSignedPointsLocal(group.pointDelta),
                        color = if (group.pointDelta >= 0.0) themeColors.encourage else themeColors.restraint,
                    )
                }
                if (day.rewardSpends.isNotEmpty()) {
                    WeeklyPointsDetailSectionTitle(AppText.t("stats_weekly_points_detail_spends"))
                }
                day.rewardSpends.forEach { spend ->
                    WeeklyPointsCompactDetailRow(
                        label = AppText.t("stats_weekly_points_spend_purchase", spend.rewardTitle),
                        value = formatSignedPointsLocal(spend.pointDelta),
                        color = themeColors.restraint,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyPointsDetailSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 3.dp),
    )
}

@Composable
private fun WeeklyPointsCompactDetailRow(
    label: String,
    value: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(color))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun PointsLegendItem(color: Color, label: String, line: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = if (line) Modifier.width(20.dp).height(3.dp).clip(CircleShape).background(color) else Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PointsLegendValue(color: Color, label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

private fun smoothPointsPath(points: List<Offset>): Path =
    Path().apply {
        if (points.isEmpty()) return@apply
        moveTo(points.first().x, points.first().y)
        points.zipWithNext().forEach { (start, end) ->
            val midX = (start.x + end.x) / 2f
            cubicTo(midX, start.y, midX, end.y, end.x, end.y)
        }
    }

private fun formatPointsChart(value: Double): String =
    if (kotlin.math.abs(value % 1.0) < 0.001) {
        String.format(Locale.getDefault(), "%,.0f", value)
    } else {
        String.format(Locale.getDefault(), "%,.1f", value)
    }

@Composable
internal fun TrendLineChart(
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
internal fun PeriodHeatmapCard(data: PeriodHeatmapData) {
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
                                    fontWeight = if (cell.selected) FontWeight.SemiBold else FontWeight.Normal,
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
internal fun PeriodAppFocusCard(data: AppFocusSectionData) {
    val palette = LocalReportColors.current.appChartPalette
    val appColors = rememberAppChartColors(data.topApps.map { it.packageName })
    val maxUsage = data.topApps.maxOfOrNull { it.value }?.coerceAtLeast(1L) ?: 1L
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(icon = Icons.Default.BarChart, title = data.title, subtitle = data.subtitle)
                Text(
                    text = data.totalUsageLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            if (data.weeklyTopAppRows.isNotEmpty()) {
                WeeklyTopAppsMatrix(rows = data.weeklyTopAppRows)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                data.topApps.take(6).forEachIndexed { index, app ->
                    val accent = appColors[app.packageName] ?: stableAppFallbackColor(app.packageName, palette)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = accent.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
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
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = formatDuration(app.value),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = accent,
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
internal fun WeeklyTopAppsMatrix(rows: List<WeeklyTopAppsRow>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        rows.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = day.dayCode,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                repeat(7) { rank ->
                    val pkg = day.packages.getOrNull(rank)
                    if (pkg != null) {
                        MatrixAppIcon(pkg = pkg)
                    } else {
                        MatrixPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
internal fun MatrixAppIcon(pkg: String) {
    val context = LocalContext.current
    val icon = remember(pkg) {
        AppVisualCache.getIcon(context, pkg)
    }
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            AsyncImage(
                model = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
            )
        } else {
            MatrixPlaceholder()
        }
    }
}

@Composable
internal fun MatrixPlaceholder() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "·",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun PeriodInsightSection(data: PeriodReportData) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.Insights,
                title = AppText.t("stats_behavior_analysis"),
                subtitle = AppText.t("stats_behavior_structure_description"),
            )
            val structure = data.behavior?.structure
            if (structure == null) {
                Text(
                    text = AppText.t("stats_this_archived_window_does_not_have_enough_behavior"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AdaptiveRowGrid(
                    itemCount = structure.metrics.size,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                ) { modifier, index ->
                    val metric = structure.metrics[index]
                    when (index) {
                        0 -> MiniInsightCard(
                            icon = Icons.Default.TouchApp,
                            label = metric.label,
                            value = metric.value,
                            visualRatio = metric.visualRatio,
                            modifier = modifier,
                        )
                        1 -> MiniInsightCard(
                            icon = Icons.Default.BarChart,
                            label = metric.label,
                            value = metric.value,
                            visualRatio = metric.visualRatio,
                            modifier = modifier,
                        )
                        2 -> MiniInsightCard(
                            icon = Icons.Default.Schedule,
                            label = metric.label,
                            value = metric.value,
                            visualRatio = metric.visualRatio,
                            modifier = modifier,
                        )
                        else -> MiniInsightCard(
                            icon = Icons.Default.NightsStay,
                            label = metric.label,
                            value = metric.value,
                            visualRatio = metric.visualRatio,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PeriodMonthStructureCard(data: MonthlyWeekStructureData) {
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
                        Text(
                            text = formatDuration(week.totalUsageMillis),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = AppText.t("stats_daily_average_value", formatDuration(week.averageUsageMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(AppText.t("stats_peak_time"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(week.peakDayLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PeriodQuarterBreakdownCard(data: YearQuarterSectionData) {
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
                        Text(
                            text = formatDuration(quarter.totalUsageMillis),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(AppText.t("stats_best_month"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${quarter.bestMonthLabel} · ${formatDuration(quarter.bestMonthUsageMillis)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(AppText.t("stats_top_apps"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(quarter.topAppLabel, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
internal fun MetricTileCompact(
    metric: DailyFocusMetric,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
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
                color = themeColors.inkMuted,
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
        }
    }
}

internal fun periodToneColor(
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
