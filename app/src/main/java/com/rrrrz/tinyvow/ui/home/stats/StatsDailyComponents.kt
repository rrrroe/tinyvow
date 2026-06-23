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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

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
    focusState: SectionState<DailyFocusSectionData> = SectionState.Empty,
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
                    val focusData = (focusState as? SectionState.Ready)?.data
                    DailyRhythmProfileStrip(
                        data = timelineState.data,
                    )
                    TimelineFooter(labels = buildTimelineFooterLabels(ReportTab.DAY, timelineState.data.buckets))
                    DailyRhythmSignalGrid(
                        data = timelineState.data,
                        focusData = focusData,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyRhythmProfileStrip(
    data: TimelineSectionData,
) {
    val themeColors = LocalThemeColors.current
    val visibleLegend = data.appLegend.filter { it.millis > 0L }
    val appPackages =
        visibleLegend
            .filter { it.packageName != TIMELINE_OTHER_APPS_PACKAGE_NAME }
            .map { it.packageName }
    val appColors = rememberAppChartColors(appPackages)
    val palette = LocalReportColors.current.appChartPalette
    val otherAppsColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
    val legendColors =
        visibleLegend.mapIndexed { index, item ->
            item.packageName to
                when (item.packageName) {
                    TIMELINE_OTHER_APPS_PACKAGE_NAME -> otherAppsColor
                    else -> appColors[item.packageName] ?: palette[index % palette.size]
                }
        }.toMap()
    val maxUsage = data.buckets.maxOfOrNull { it.deviceMillis }?.coerceAtLeast(1L) ?: 1L
    val peakHour = data.buckets.maxByOrNull { it.deviceMillis }?.hour

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = AppText.t("stats_rhythm_strip_title"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                    Text(
                        text = AppText.t(
                            "stats_rhythm_active_hours",
                            data.buckets.count { it.deviceMillis > 0L },
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = themeColors.inkMuted,
                    )
                }
                RhythmLegendPill(
                    color = MaterialTheme.colorScheme.primary,
                    label = AppText.t("stats_rhythm_legend_peak"),
                )
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                data.buckets.forEach { bucket ->
                    val dominantSegment = bucket.appSegments.maxByOrNull { it.millis }
                    val accent =
                        dominantSegment?.let { legendColors[it.packageName] }
                            ?: MaterialTheme.colorScheme.primary
                    val usageRatio =
                        if (bucket.deviceMillis > 0L) {
                            (bucket.deviceMillis.toFloat() / maxUsage.toFloat()).coerceIn(0.16f, 1f)
                        } else {
                            0.06f
                        }
                    val isNight = bucket.hour < 6 || bucket.hour >= 22
                    val isPeak = bucket.hour == peakHour && bucket.deviceMillis > 0L
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        if (isNight) {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                                        } else {
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.38f)
                                        },
                                    ),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(usageRatio)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                accent.copy(alpha = if (bucket.deviceMillis > 0L) 0.95f else 0.18f),
                                                accent.copy(alpha = if (bucket.deviceMillis > 0L) 0.40f else 0.08f),
                                            ),
                                        ),
                                    ),
                        )
                        if (isPeak) {
                            Box(
                                modifier =
                                    Modifier
                                        .align(Alignment.TopCenter)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visibleLegend.take(3).forEach { item ->
                    RhythmLegendPill(
                        color = legendColors[item.packageName] ?: MaterialTheme.colorScheme.primary,
                        label = "${item.label} · ${formatDuration(item.millis)}",
                    )
                }
                RhythmLegendPill(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    label = AppText.t("stats_rhythm_legend_night"),
                    muted = true,
                )
            }
        }
    }
}

@Composable
private fun RhythmLegendPill(
    color: Color,
    label: String,
    muted: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (muted) 0.48f else 1f)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DailyRhythmSignalGrid(
    data: TimelineSectionData,
    focusData: DailyFocusSectionData?,
) {
    val topApp = data.appLegend.firstOrNull { it.millis > 0L }
    AdaptiveRowGrid(
        itemCount = 4,
        compactColumns = 1,
        expandedColumns = 2,
        horizontalSpacing = 10.dp,
        verticalSpacing = 10.dp,
    ) { modifier, index ->
        when (index) {
            0 -> MiniInsightCard(
                icon = Icons.AutoMirrored.Filled.CallSplit,
                label = AppText.t("stats_over_2h"),
                value = "${data.peakTwoHourLabel} · ${formatDuration(data.peakTwoHourMillis)}",
                visualRatio = (data.peakTwoHourMillis.toFloat() / (4 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                compact = true,
                modifier = modifier,
            )
            1 -> MiniInsightCard(
                icon = Icons.Default.NightsStay,
                label = AppText.t("stats_night_use"),
                value = formatDuration(data.nightUsageMillis),
                visualRatio = (data.nightUsageMillis.toFloat() / (3 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                compact = true,
                modifier = modifier,
            )
            2 -> MiniInsightCard(
                icon = Icons.Default.PhoneAndroid,
                label = AppText.t("stats_rhythm_top_app"),
                value = topApp?.let { "${it.label} · ${formatDuration(it.millis)}" }
                    ?: AppText.t("stats_rhythm_no_top_app"),
                visualRatio = topApp?.let { app ->
                    val total = data.buckets.sumOf { it.deviceMillis }.coerceAtLeast(1L)
                    (app.millis.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                },
                compact = true,
                modifier = modifier,
            )
            else -> MiniInsightCard(
                icon = Icons.Default.Bolt,
                label = AppText.t("stats_rhythm_vow_mix"),
                value =
                    focusData?.let {
                        AppText.t(
                            "stats_rhythm_vow_mix_value",
                            formatDuration(it.controlUsageMillis),
                            formatDuration(it.encourageUsageMillis),
                        )
                    } ?: AppText.t("stats_none"),
                visualRatio = focusData?.let {
                    val total = data.buckets.sumOf { bucket -> bucket.deviceMillis }.coerceAtLeast(1L)
                    ((it.controlUsageMillis + it.encourageUsageMillis).toFloat() / total.toFloat()).coerceIn(0f, 1f)
                },
                compact = true,
                modifier = modifier,
            )
        }
    }
}

@Composable
internal fun DailyAppFocusCard(
    topAppsState: SectionState<TopAppsSectionData>,
) {
    val topAppsData = (topAppsState as? SectionState.Ready)?.data
    val usageTopApps = topAppsData?.usageTopApps.orEmpty()
    val appProfiles = topAppsData?.appProfiles.orEmpty()
    val appColors = rememberAppChartColors(usageTopApps.map { it.packageName })
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.BarChart,
                title = AppText.t("stats_app_focus"),
                subtitle = AppText.t("stats_app_focus_daily_profile_description"),
            )
            when (topAppsState) {
                SectionState.Loading -> {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth(),
                        height = 280.dp,
                        shape = RoundedCornerShape(22.dp),
                    )
                }
                SectionState.Empty -> {
                    Text(
                        text = AppText.t("stats_this_archived_day_does_not_have_enough_usage"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is SectionState.Ready -> {
                    if (appProfiles.isEmpty()) {
                        AppUsageShareCard(
                            items = usageTopApps,
                            appColors = appColors,
                        )
                    } else {
                        DailyAppSolarSystemPanel(
                            profiles = appProfiles,
                            appColors = appColors,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyAppSolarSystemPanel(
    profiles: List<AppFocusProfileItem>,
    appColors: Map<String, Color>,
    modifier: Modifier = Modifier,
) {
    val visibleProfiles = profiles.take(9)
    val maxUsage = visibleProfiles.maxOfOrNull { it.usageMillis }?.coerceAtLeast(1L) ?: 1L
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.74f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AppSolarSystemChart(
                    profiles = visibleProfiles,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 300.dp else 344.dp),
                )
                AdaptiveRowGrid(
                    itemCount = visibleProfiles.size,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 10.dp,
                    verticalSpacing = 10.dp,
                ) { itemModifier, index ->
                    visibleProfiles.getOrNull(index)?.let { item ->
                        val color = appColors[item.packageName] ?: fallbackChartColor(index)
                        DailyAppDataCard(
                            rank = index + 1,
                            item = item,
                            maxUsage = maxUsage,
                            color = color,
                            modifier = itemModifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSolarSystemChart(
    profiles: List<AppFocusProfileItem>,
    modifier: Modifier = Modifier,
) {
    val planets = profiles.take(9)
    val outline = MaterialTheme.colorScheme.outlineVariant
    val totalUsageMillis = planets.sumOf { it.usageMillis }.coerceAtLeast(1L)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            planets.forEachIndexed { index, _ ->
                val orbitRadius = min(size.width, size.height) * solarOrbitProgress(index, planets.size)
                drawCircle(
                    color = outline.copy(alpha = 0.44f),
                    radius = orbitRadius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.15f),
                )
            }
        }
        planets.forEachIndexed { index, item ->
            val planetRotation by rememberInfiniteTransition(label = "app_planet_orbit_$index").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = planetOrbitDurationMillis(index), easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "app_planet_orbit_rotation_$index",
            )
            val angle = -82f + index * 137.5f + item.peakHour * 6f + planetRotation
            val orbitProgress = solarOrbitProgress(index, planets.size)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val radius = min(size.width, size.height) * orbitProgress
                        val radians = angle * (PI.toFloat() / 180f)
                        translationX = cos(radians) * radius
                        translationY = sin(radians) * radius
                    },
                contentAlignment = Alignment.Center,
            ) {
                val areaRatio = (item.usageMillis.toFloat() / totalUsageMillis.toFloat()).coerceIn(0f, 1f)
                val iconSize = (60f * sqrt(areaRatio)).coerceAtLeast(14f).dp
                SolarSystemAppIcon(
                    pkg = item.packageName,
                    size = iconSize,
                )
            }
        }
        SolarSystemTotalUsageSun(totalUsageMillis = totalUsageMillis)
    }
}

private fun solarOrbitProgress(
    index: Int,
    count: Int,
): Float {
    if (count <= 1) return 0.32f
    val t = index.toFloat() / (count - 1).toFloat()
    val eased = t * 0.58f + (1f - (1f - t) * (1f - t)) * 0.42f
    return 0.18f + (0.43f - 0.18f) * eased
}

private fun planetOrbitDurationMillis(index: Int): Int =
    180_000 + index * 36_000

@Composable
private fun SolarSystemAppIcon(
    pkg: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val icon = remember(pkg) {
        AppVisualCache.getIcon(context, pkg)
    }
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        if (icon != null) {
            val bitmap = remember(icon, size) {
                icon.toBitmap(width = 96, height = 96, config = Bitmap.Config.ARGB_8888).asImageBitmap()
            }
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .graphicsLayer {
                        scaleX = 1.1f
                        scaleY = 1.1f
                    },
            )
        }
    }
}

@Composable
private fun SolarSystemTotalUsageSun(
    totalUsageMillis: Long,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(60.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.52f),
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                ),
                        ),
                    )
                    .padding(horizontal = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = formatDuration(totalUsageMillis).replace(" ", "\n"),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DailyAppDataCard(
    rank: Int,
    item: AppFocusProfileItem,
    maxUsage: Long,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val peakLabel = AppText.t("stats_app_focus_peak_hour_format", item.peakHour)
    val nightShare =
        if (item.usageMillis > 0L) {
            ((item.nightUsageMillis.toFloat() / item.usageMillis.toFloat()).coerceIn(0f, 1f) * 100).roundToInt()
        } else {
            0
        }
    val usageProgress =
        if (maxUsage > 0L) {
            (item.usageMillis.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = if (rank == 1) 0.1f else 0.06f),
        border = BorderStroke(1.dp, color.copy(alpha = if (rank == 1) 0.28f else 0.16f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.width(18.dp),
                textAlign = TextAlign.Center,
            )
            AppIconCircle(pkg = item.packageName, size = 36.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(item.share * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.14f)),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(usageProgress)
                                .clip(CircleShape)
                                .background(color),
                    )
                }
                Text(
                    text =
                        AppText.t(
                            "stats_app_focus_row_metrics_compact",
                            formatDuration(item.usageMillis),
                            item.openCount,
                            nightShare,
                            peakLabel,
                        ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
                trailing = heroData?.summary?.capturedAt?.takeIf { it.isNotBlank() },
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
                        totalMetric?.let { metric ->
                            BehaviorCenteredTotalMetric(
                                metric = metric,
                                modifier = Modifier.align(Alignment.Center),
                            )
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
private fun BehaviorCenteredTotalMetric(
    metric: BehaviorTotalMetric,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(width = 108.dp, height = 96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = metric.value,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = metric.accent.copy(alpha = 0.5f),
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
