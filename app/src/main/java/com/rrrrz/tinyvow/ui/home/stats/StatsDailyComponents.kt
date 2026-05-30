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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
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
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.LocalReportColors
import com.rrrrz.tinyvow.ui.theme.ReportColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
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
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DailyBattleHeroCard(
    heroState: SectionState<HeroSectionData>,
    animateValues: Boolean = false,
) {
    val data = (heroState as? SectionState.Ready)?.data
    val summary = data?.summary
    val overview = data?.overview
    val reportColors = LocalReportColors.current
    val themeColors = LocalThemeColors.current
    ReportCard {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.78f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (data == null || summary == null || overview == null) {
                    Text(
                        text = AppText.t("stats_total_phone_usage"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                    SkeletonLine(width = 176.dp, height = 44.dp)
                    SkeletonLine(fill = true, height = 20.dp)
                    AdaptiveRowGrid(
                        itemCount = 2,
                        compactColumns = 2,
                        expandedColumns = 2,
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp,
                    ) { modifier, index ->
                        BattleHeadlineSkeletonChip(
                            label = if (index == 0) AppText.t("stats_vs_previous_day_decreased") else AppText.t("stats_last_7_days_daily_average"),
                            modifier = modifier,
                        )
                    }
                    AdaptiveRowGrid(
                        itemCount = 2,
                        compactColumns = 2,
                        expandedColumns = 2,
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp,
                    ) { modifier, index ->
                        BattleMetricSkeletonTile(
                            label = if (index == 0) AppText.t("stats_launches") else AppText.t("stats_night_use"),
                            accent = if (index == 0) reportColors.danger else reportColors.positive,
                            modifier = modifier,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SkeletonPill(width = 86.dp)
                        SkeletonPill(width = 76.dp)
                        SkeletonPill(width = 96.dp)
                    }
                } else {
                    Text(
                        text = summary.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                    if (animateValues) {
                        AnimatedMetricText(
                            rawText = summary.primaryValue,
                            label = "daily_battle_primary_${summary.title}_${summary.primaryValue}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            delayMillis = 80,
                        )
                    } else {
                        Text(
                            text = summary.primaryValue,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = summary.message,
                        style = MaterialTheme.typography.titleSmall,
                        color = themeColors.ink,
                    )
                    AdaptiveRowGrid(
                        itemCount = 2,
                        compactColumns = 2,
                        expandedColumns = 2,
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp,
                    ) { modifier, index ->
                        when (index) {
                            0 -> BattleHeadlineChip(
                                label = AppText.t("stats_vs_previous_day_decreased"),
                                value = summary.secondaryValue,
                                accent = reportColors.danger,
                                animateValue = animateValues,
                                modifier = modifier,
                            )
                            else -> BattleHeadlineChip(
                                label = AppText.t("stats_last_7_days_daily_average"),
                                value = summary.tertiaryValue,
                                accent = reportColors.positive,
                                animateValue = animateValues,
                                modifier = modifier,
                            )
                        }
                    }
                    AdaptiveRowGrid(
                        itemCount = 2,
                        compactColumns = 2,
                        expandedColumns = 2,
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp,
                    ) { modifier, index ->
                        when (index) {
                            0 -> BattleMetricTile(
                                label = AppText.t("stats_launches"),
                                value = AppText.t("stats_value_times_12", overview.openCount),
                                accent = reportColors.danger,
                                animateValue = animateValues,
                                modifier = modifier,
                            )
                            else -> BattleMetricTile(
                                label = AppText.t("stats_night_use"),
                                value = formatDuration(data.nightUsageMillis),
                                accent = reportColors.positive,
                                animateValue = animateValues,
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
private fun BattleHeadlineSkeletonChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SkeletonLine(width = 68.dp, height = 16.dp)
        }
    }
}

@Composable
private fun BattleMetricSkeletonTile(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.inkFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SkeletonLine(width = 52.dp, height = 15.dp)
        }
    }
}

@Composable
internal fun BattleHeadlineChip(
    label: String,
    value: String,
    accent: Color,
    showValue: Boolean = true,
    animateValue: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = themeColors.inkMuted,
            )
            if (showValue) {
                if (animateValue) {
                    AnimatedMetricText(
                        rawText = value,
                        label = "battle_headline_${label}_${value}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        delayMillis = 180,
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
internal fun BattleMetricTile(
    label: String,
    value: String,
    accent: Color,
    animateValue: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val displayValue =
        if (animateValue) {
            animateMetricDisplayText(
                rawText = value,
                label = "battle_metric_${label.hashCode()}",
                delayMillis = 180,
            )
        } else {
            value
        }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.inkFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun DailyRhythmCard(
    timelineState: SectionState<TimelineSectionData>,
) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.Timeline,
                title = AppText.t("stats_time_flow"),
                subtitle = AppText.t("stats_time_flow_description"),
            )
            when (timelineState) {
                SectionState.Loading -> {
                    SkeletonTimelineChart()
                    TimelineFooter(labels = buildTimelineFooterLabels(ReportTab.DAY, emptyList()))
                    AdaptiveRowGrid(
                        itemCount = 3,
                        compactColumns = 1,
                        expandedColumns = 3,
                        horizontalSpacing = 10.dp,
                        verticalSpacing = 10.dp,
                    ) { modifier, index ->
                        MiniInsightSkeletonCard(
                            label = when (index) {
                                0 -> AppText.t("stats_peak_time")
                                1 -> AppText.t("stats_over_2h")
                                else -> AppText.t("stats_night_use")
                            },
                            compact = true,
                            modifier = modifier,
                        )
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
internal fun DailyAppFocusCard(
    topAppsState: SectionState<TopAppsSectionData>,
) {
    val usageTopApps = (topAppsState as? SectionState.Ready)?.data?.usageTopApps.orEmpty()
    val appColors = rememberAppChartColors(usageTopApps.map { it.packageName })
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.BarChart,
                title = AppText.t("stats_app_focus"),
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
        }
    }
}

@Composable
internal fun DailyBehaviorProfileCard(
    heroState: SectionState<HeroSectionData>,
    focusState: SectionState<DailyFocusSectionData>,
    behaviorState: SectionState<BehaviorSectionData>,
) {
    val structure = (behaviorState as? SectionState.Ready)?.data?.structure
    val heroData = (heroState as? SectionState.Ready)?.data
    val focusData = (focusState as? SectionState.Ready)?.data
    val themeColors = LocalThemeColors.current
    ReportCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionHeader(
                icon = Icons.Default.Insights,
                title = AppText.t("stats_behavior_analysis"),
                subtitle = AppText.t("stats_behavior_structure_description"),
            )
            when {
                behaviorState == SectionState.Loading -> {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth(),
                        height = 300.dp,
                        shape = RoundedCornerShape(22.dp),
                    )
                }
                structure == null || structure.metrics.isEmpty() -> {
                    Text(
                        text = AppText.t("stats_this_archived_day_does_not_have_enough_behavior"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    val totalUsageMillis = heroData?.overview?.totalUsageMillis ?: 0L
                    val controlUsageMillis = focusData?.controlUsageMillis ?: 0L
                    val encourageUsageMillis = focusData?.encourageUsageMillis ?: 0L
                    val controlSavedMillis = focusData?.controlSavedMillis ?: 0L
                    val savedAccent = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                    BehaviorRadarPanel(
                        metrics = structure.scoreMetrics,
                        comparisonMetrics = structure.comparisonScoreMetrics,
                        cornerMetrics =
                            listOf(
                                BehaviorCornerMetric(
                                    label = AppText.t("stats_behavior_corner_usage"),
                                    value = formatBehaviorMetricMinutes(totalUsageMillis),
                                    unit = AppText.t("stats_behavior_unit_minutes_short"),
                                    accent = themeColors.base,
                                    rawMillis = totalUsageMillis,
                                    align = Alignment.TopStart,
                                ),
                                BehaviorCornerMetric(
                                    label = AppText.t("stats_behavior_corner_investment"),
                                    value = formatBehaviorMetricMinutes(encourageUsageMillis),
                                    unit = AppText.t("stats_behavior_unit_minutes_short"),
                                    accent = themeColors.encourage,
                                    rawMillis = encourageUsageMillis,
                                    align = Alignment.TopEnd,
                                ),
                                BehaviorCornerMetric(
                                    label = AppText.t("stats_behavior_corner_control"),
                                    value = formatBehaviorMetricMinutes(controlUsageMillis),
                                    unit = AppText.t("stats_behavior_unit_minutes_short"),
                                    accent = themeColors.control,
                                    rawMillis = controlUsageMillis,
                                    align = Alignment.BottomStart,
                                ),
                                BehaviorCornerMetric(
                                    label = AppText.t("stats_behavior_corner_savings"),
                                    value = formatBehaviorMetricMinutes(controlSavedMillis),
                                    unit = AppText.t("stats_behavior_unit_minutes_short"),
                                    accent = savedAccent,
                                    rawMillis = controlSavedMillis,
                                    align = Alignment.BottomEnd,
                                ),
                            ),
                        totalMetric =
                            BehaviorTotalMetric(
                                label = AppText.t("stats_behavior_total_score"),
                                value = structure.scoreMetrics.takeIf { it.isNotEmpty() }?.map { it.score }?.average()?.roundToInt()?.toString()
                                    ?: "0",
                                unit = "",
                                accent = themeColors.base,
                            ),
                    )
                }
            }
        }
    }
}

internal data class BehaviorCornerMetric(
    val label: String,
    val value: String,
    val unit: String,
    val accent: Color,
    val rawMillis: Long = 0L,
    val align: Alignment,
)

internal data class BehaviorTotalMetric(
    val label: String,
    val value: String,
    val unit: String,
    val accent: Color,
)

@Composable
internal fun DailyInsightCard(
    comparisonState: SectionState<ComparisonSectionData>,
) {
    ReportCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionHeader(
                icon = Icons.AutoMirrored.Filled.CompareArrows,
                title = AppText.t("stats_daily_insights"),
                subtitle = AppText.t("stats_daily_insights_description"),
            )
            when (comparisonState) {
                SectionState.Loading -> {
                    repeat(2) {
                        SkeletonBlock(
                            modifier = Modifier.fillMaxWidth(),
                            height = 92.dp,
                            shape = RoundedCornerShape(18.dp),
                        )
                    }
                }
                SectionState.Empty -> {
                    Text(
                        text = AppText.t("stats_not_enough_earlier_archive_samples"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is SectionState.Ready -> {
                    AdaptiveRowGrid(
                        itemCount = comparisonState.data.comparisons.size,
                        compactColumns = 1,
                        expandedColumns = 2,
                        horizontalSpacing = 10.dp,
                        verticalSpacing = 10.dp,
                    ) { modifier, index ->
                        ComparisonRow(
                            item = comparisonState.data.comparisons[index],
                            showChips = true,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BehaviorRadarPanel(
    metrics: List<DailyBehaviorScoreMetric>,
    comparisonMetrics: List<DailyBehaviorScoreMetric> = emptyList(),
    cornerMetrics: List<BehaviorCornerMetric> = emptyList(),
    totalMetric: BehaviorTotalMetric? = null,
    modifier: Modifier = Modifier,
) {
    val displayMetrics = metrics.take(5)
    val comparisonByLabel = comparisonMetrics.associateBy { it.label }
    val displayComparisonMetrics =
        displayMetrics.mapNotNull { metric ->
            comparisonByLabel[metric.label]
        }
    val themeColors = LocalThemeColors.current
    val primary = MaterialTheme.colorScheme.primary
    val radarLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f)
    val radarAxisColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)
    val previousColor = themeColors.inkMuted
    val showSurroundingMetrics = cornerMetrics.isNotEmpty() || totalMetric != null
    var selectedMetric by remember { mutableStateOf<DailyBehaviorScoreMetric?>(null) }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, primary.copy(alpha = 0.16f)),
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (displayMetrics.size < 3) {
                Text(
                    text = AppText.t("stats_this_archived_day_does_not_have_enough_behavior"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(22.dp),
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(if (showSurroundingMetrics) 312.dp else 278.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showSurroundingMetrics) {
                        BehaviorOverviewGradientBackdrop(
                            cornerMetrics = cornerMetrics,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Box(
                        modifier =
                            if (showSurroundingMetrics) {
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = 32.dp)
                                    .size(240.dp)
                            } else {
                                Modifier.size(width = 250.dp, height = 218.dp)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (showSurroundingMetrics) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                                border = BorderStroke(1.dp, primary.copy(alpha = 0.16f)),
                            ) {}
                        }
                        Canvas(modifier = Modifier.size(if (showSurroundingMetrics) 210.dp else 208.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.minDimension * 0.39f
                            val axisCount = displayMetrics.size
                            fun pointFor(index: Int, ratio: Float): Offset {
                                val angle = -PI.toFloat() / 2f + (2f * PI.toFloat() * index / axisCount)
                                return Offset(
                                    x = center.x + cos(angle) * radius * ratio,
                                    y = center.y + sin(angle) * radius * ratio,
                                )
                            }
                            fun pointsFor(scoreMetrics: List<DailyBehaviorScoreMetric>): List<Offset> =
                                scoreMetrics.mapIndexed { index, metric ->
                                    pointFor(index, (metric.score / 100f).coerceIn(0.08f, 1f))
                                }
                            fun pathFor(points: List<Offset>): Path =
                                Path().apply {
                                    points.forEachIndexed { index, point ->
                                        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                                    }
                                    close()
                                }
                            repeat(4) { ringIndex ->
                                val ratio = (ringIndex + 1) / 4f
                                val path = Path()
                                repeat(axisCount) { index ->
                                    val point = pointFor(index, ratio)
                                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                                }
                                path.close()
                                drawPath(
                                    path = path,
                                    color = radarLineColor,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.1f),
                                )
                            }
                            repeat(axisCount) { index ->
                                val edge = pointFor(index, 1f)
                                drawLine(
                                    color = radarAxisColor,
                                    start = center,
                                    end = edge,
                                    strokeWidth = 1.7f,
                                )
                            }
                            if (displayComparisonMetrics.size == displayMetrics.size) {
                                val previousPath = pathFor(pointsFor(displayComparisonMetrics))
                                drawPath(
                                    path = previousPath,
                                    color = previousColor.copy(alpha = 0.12f),
                                )
                                drawPath(
                                    path = previousPath,
                                    color = previousColor.copy(alpha = 0.36f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.2f),
                                )
                            }
                            val metricPoints = pointsFor(displayMetrics)
                            metricPoints.forEachIndexed { index, start ->
                                val end = metricPoints[(index + 1) % metricPoints.size]
                                val startColor = behaviorScoreAccentColor(displayMetrics[index].accentIndex)
                                val endColor = behaviorScoreAccentColor(displayMetrics[(index + 1) % displayMetrics.size].accentIndex)
                                drawLine(
                                    brush =
                                        Brush.linearGradient(
                                            colors = listOf(startColor, endColor),
                                            start = start,
                                            end = end,
                                        ),
                                    start = start,
                                    end = end,
                                    strokeWidth = 5f,
                                )
                            }
                            displayMetrics.forEachIndexed { index, metric ->
                                val point = pointFor(index, (metric.score / 100f).coerceIn(0.08f, 1f))
                                drawCircle(
                                    color = behaviorScoreAccentColor(metric.accentIndex),
                                    radius = 8f,
                                    center = point,
                                )
                            }
                        }
                        displayMetrics.getOrNull(0)?.let { metric ->
                            BehaviorScoreVertexLabel(
                                metric = metric,
                                onClick = { selectedMetric = metric },
                                modifier =
                                    Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-2).dp)
                                        .size(width = 58.dp, height = 48.dp),
                            )
                        }
                        displayMetrics.getOrNull(1)?.let { metric ->
                            BehaviorScoreVertexLabel(
                                metric = metric,
                                onClick = { selectedMetric = metric },
                                modifier =
                                    Modifier
                                        .align(Alignment.CenterEnd)
                                        .offset(x = 0.dp, y = (-22).dp)
                                        .size(width = 58.dp, height = 48.dp),
                            )
                        }
                        displayMetrics.getOrNull(2)?.let { metric ->
                            BehaviorScoreVertexLabel(
                                metric = metric,
                                onClick = { selectedMetric = metric },
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-26).dp, y = (-22).dp)
                                        .size(width = 58.dp, height = 48.dp),
                            )
                        }
                        displayMetrics.getOrNull(3)?.let { metric ->
                            BehaviorScoreVertexLabel(
                                metric = metric,
                                onClick = { selectedMetric = metric },
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomStart)
                                        .offset(x = 26.dp, y = (-22).dp)
                                        .size(width = 58.dp, height = 48.dp),
                            )
                        }
                        displayMetrics.getOrNull(4)?.let { metric ->
                            BehaviorScoreVertexLabel(
                                metric = metric,
                                onClick = { selectedMetric = metric },
                                modifier =
                                    Modifier
                                        .align(Alignment.CenterStart)
                                        .offset(x = 0.dp, y = (-22).dp)
                                        .size(width = 58.dp, height = 48.dp),
                            )
                        }
                    }
                    cornerMetrics.forEach { metric ->
                        BehaviorCornerMetricBlock(
                            metric = metric,
                            modifier =
                                Modifier
                                    .align(metric.align)
                                    .offset(
                                        x =
                                            when (metric.align) {
                                                Alignment.TopStart, Alignment.BottomStart -> 9.dp
                                                Alignment.TopEnd, Alignment.BottomEnd -> (-9).dp
                                                else -> 0.dp
                                            },
                                        y =
                                            when (metric.align) {
                                                Alignment.TopStart, Alignment.TopEnd -> 9.dp
                                                Alignment.BottomStart, Alignment.BottomEnd -> (-9).dp
                                                else -> 0.dp
                                            },
                                    ),
                        )
                    }
                    totalMetric?.let {
                        BehaviorTotalMetricBlock(
                            metric = it,
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(x = 0.dp, y = (-38).dp),
                        )
                    }
                }
            }
        }
    }
    selectedMetric?.let { metric ->
        BehaviorScoreMetricDetailDialog(
            title = metric.label,
            score = metric.score,
            accentColor = behaviorScoreAccentColor(metric.accentIndex),
            formulaLines = metric.explanation?.formulaLines.orEmpty(),
            comparisonRows = metric.explanation?.comparisonRows.orEmpty(),
            onDismiss = { selectedMetric = null },
        )
    }
}

@Composable
private fun BehaviorOverviewGradientBackdrop(
    cornerMetrics: List<BehaviorCornerMetric>,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val totalMetric = cornerMetrics.firstOrNull { it.align == Alignment.TopStart }
    val investmentMetric = cornerMetrics.firstOrNull { it.align == Alignment.TopEnd }
    val controlMetric = cornerMetrics.firstOrNull { it.align == Alignment.BottomStart }
    val savedMetric = cornerMetrics.firstOrNull { it.align == Alignment.BottomEnd }
    val totalRingColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)
    val investmentColor = investmentMetric?.accent ?: themeColors.encourage
    val controlColor = controlMetric?.accent ?: themeColors.control
    val savedColor = Color.White
    val totalMillis = totalMetric?.rawMillis ?: 0L
    val investmentMillis = investmentMetric?.rawMillis ?: 0L
    val controlMillis = controlMetric?.rawMillis ?: 0L
    val savedMillis = savedMetric?.rawMillis ?: 0L

    Canvas(modifier = modifier.clip(RoundedCornerShape(24.dp))) {
        val circleCenter = Offset(size.width / 2f, 32.dp.toPx() + 120.dp.toPx())
        val innerRadius = 120.dp.toPx()
        val ringWidth = 16.dp.toPx()
        val innerRingRadius = innerRadius + ringWidth / 2f
        val innerStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = ringWidth, cap = StrokeCap.Round)
        val totalAndSavedMillis = (totalMillis + savedMillis).coerceAtLeast(1L)
        val savedSweep =
            if (savedMillis <= 0L) {
                0f
            } else {
                (360f * savedMillis.toFloat() / totalAndSavedMillis.toFloat()).coerceIn(0f, 360f)
        }
        val savedCenter = 45f
        val savedEnd = savedCenter + savedSweep / 2f
        val totalSweep = 360f - savedSweep

        fun normalizeAngle(angle: Float): Float = ((angle % 360f) + 360f) % 360f

        fun arcProgress(
            angle: Float,
            startAngle: Float,
        ): Float {
            return normalizeAngle(angle - startAngle)
        }

        fun totalProgress(angle: Float): Float = arcProgress(angle, savedEnd)

        fun roleArc(
            center: Float,
            millis: Long,
        ): Pair<Float, Float>? {
            if (millis <= 0L || totalMillis <= 0L || totalSweep <= 0.5f) return null
            val centerProgress = totalProgress(center)
            if (centerProgress !in 0f..totalSweep) return null
            val ratio = (millis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
            val sweepAngle = (totalSweep * ratio).coerceIn(0f, totalSweep)
            if (sweepAngle <= 0.5f) return null
            val startProgress =
                (centerProgress - sweepAngle / 2f)
                    .coerceIn(0f, totalSweep - sweepAngle)
            return savedEnd + startProgress to sweepAngle
        }

        val investmentArc = roleArc(center = 315f, millis = investmentMillis)
        val controlArc = roleArc(center = 135f, millis = controlMillis)
        val innerTopLeft = Offset(circleCenter.x - innerRingRadius, circleCenter.y - innerRingRadius)
        val innerSize = Size(innerRingRadius * 2f, innerRingRadius * 2f)
        val capInsetSweep =
            (atan((ringWidth / 2f) / innerRingRadius) * 180f / PI.toFloat())
                .coerceIn(0.5f, 12f)

        fun pointOnRing(angle: Float): Offset {
            val radians = angle / 180f * PI.toFloat()
            return Offset(
                x = circleCenter.x + cos(radians) * innerRingRadius,
                y = circleCenter.y + sin(radians) * innerRingRadius,
            )
        }

        fun drawDataArc(
            color: Color,
            startAngle: Float,
            sweepAngle: Float,
        ) {
            when {
                sweepAngle >= 359.5f -> {
                    drawCircle(
                        color = color,
                        radius = innerRingRadius,
                        center = circleCenter,
                        style = innerStroke,
                    )
                }
                sweepAngle > capInsetSweep * 2f -> {
                    drawArc(
                        color = color,
                        startAngle = startAngle + capInsetSweep,
                        sweepAngle = sweepAngle - capInsetSweep * 2f,
                        useCenter = false,
                        topLeft = innerTopLeft,
                        size = innerSize,
                        style = innerStroke,
                    )
                }
                sweepAngle > 0.5f -> {
                    drawCircle(
                        color = color,
                        radius = ringWidth / 2f,
                        center = pointOnRing(startAngle + sweepAngle / 2f),
                    )
                }
            }
        }

        drawDataArc(
            color = totalRingColor,
            startAngle = savedEnd,
            sweepAngle = totalSweep,
        )
        drawDataArc(
            color = savedColor,
            startAngle = savedCenter - savedSweep / 2f,
            sweepAngle = savedSweep,
        )
        investmentArc?.let { (startAngle, sweepAngle) ->
            drawDataArc(
                color = investmentColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
            )
        }
        controlArc?.let { (startAngle, sweepAngle) ->
            drawDataArc(
                color = controlColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
            )
        }
    }
}

@Composable
private fun BehaviorCornerMetricBlock(
    metric: BehaviorCornerMetric,
    modifier: Modifier = Modifier,
) {
    val textAlign = when (metric.align) {
        Alignment.TopStart, Alignment.BottomStart -> TextAlign.Start
        Alignment.TopEnd, Alignment.BottomEnd -> TextAlign.End
        else -> TextAlign.Center
    }
    val horizontalAlignment =
        when (metric.align) {
            Alignment.TopStart, Alignment.BottomStart -> Alignment.Start
            Alignment.TopEnd, Alignment.BottomEnd -> Alignment.End
            else -> Alignment.CenterHorizontally
        }
    Column(
        modifier = modifier.size(width = 112.dp, height = 76.dp),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement =
            if (metric.align == Alignment.BottomStart || metric.align == Alignment.BottomEnd) {
                Arrangement.Bottom
            } else {
                Arrangement.Top
            },
    ) {
        val labelContent: @Composable () -> Unit = {
            Text(
                text = "${metric.label}${metric.unit}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = metric.accent,
                textAlign = textAlign,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val valueContent: @Composable () -> Unit = {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = metric.accent,
                textAlign = textAlign,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (metric.align == Alignment.BottomStart || metric.align == Alignment.BottomEnd) {
            valueContent()
            Spacer(modifier = Modifier.height(1.dp))
            labelContent()
        } else {
            labelContent()
            Spacer(modifier = Modifier.height(1.dp))
            valueContent()
        }
    }
}

@Composable
private fun BehaviorTotalMetricBlock(
    metric: BehaviorTotalMetric,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.size(width = 88.dp, height = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = metric.value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = metric.accent,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = metric.label,
            modifier = Modifier.offset(y = (-6).dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = metric.accent,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun formatBehaviorMetricMinutes(durationMillis: Long): String {
    if (durationMillis <= 0L) return "0"
    return ((durationMillis + 59_999L) / 60_000L).toString()
}

@Composable
private fun BehaviorScoreVertexLabel(
    metric: DailyBehaviorScoreMetric,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = behaviorScoreAccentColor(metric.accentIndex)
    Column(
        modifier =
            modifier.then(
                if (metric.explanation != null) {
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            onClickLabel = AppText.t("stats_score_metric_open_detail", metric.label),
                            onClick = onClick,
                        )
                        .padding(vertical = 2.dp)
                } else {
                    Modifier
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = metric.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = metric.score.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun BehaviorScoreMetricDetailDialog(
    title: String,
    score: Int,
    accentColor: Color,
    formulaLines: List<String>,
    comparisonRows: List<BehaviorScoreMetricComparisonRow>,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
                Text(
                    text = AppText.t("stats_score_value", score),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                )
                BehaviorScoreMetricDetailSection(
                    title = AppText.t("stats_score_metric_formula_title"),
                    titleColor = accentColor,
                    lines = formulaLines,
                )
                BehaviorScoreMetricComparisonTable(
                    accentColor = accentColor,
                    rows = comparisonRows,
                )
                TinyVowButton(
                    text = AppText.t("stats_score_info_close"),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    tone = TinyVowButtonTone.Primary,
                )
            }
        }
    }
}

@Composable
private fun BehaviorScoreMetricDetailSection(
    title: String,
    titleColor: Color,
    lines: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
        )
        lines.forEach { line ->
            Text(
                text = "- $line",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BehaviorScoreMetricComparisonTable(
    accentColor: Color,
    rows: List<BehaviorScoreMetricComparisonRow>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = AppText.t("stats_score_metric_current_data_title"),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = accentColor,
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f)),
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "",
                        modifier = Modifier.weight(1.2f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = AppText.t("stats_score_metric_today_column"),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                    )
                    Text(
                        text = AppText.t("stats_score_metric_yesterday_column"),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                    )
                }
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = row.label,
                            modifier = Modifier.weight(1.2f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = row.todayValue,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = row.yesterdayValue,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CompactLockedAnalysisPanel(
    onClick: () -> Unit,
) {
    ReportCard {
        Column(
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
            TinyVowButton(
                text = AppText.t("pro_view_benefits"),
                onClick = onClick,
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun LockedAdvancedReportCard(onClick: () -> Unit) {
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
            TinyVowButton(
                text = AppText.t("pro_view_benefits"),
                onClick = onClick,
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
