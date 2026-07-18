package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import android.content.Context
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScatterPlot
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
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
import kotlinx.coroutines.launch
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
internal fun DailyTimeTideCard(
    timeTideState: SectionState<DailyTimeTideSectionData>,
    animateValues: Boolean = false,
) {
    when (timeTideState) {
        SectionState.Empty -> Unit
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth(),
                    height = 330.dp,
                    shape = RoundedCornerShape(24.dp),
                )
            }
        }
        is SectionState.Ready -> {
            val themeColors = LocalThemeColors.current
            val reportColors = LocalReportColors.current
            val colorScheme = MaterialTheme.colorScheme
            val colors =
                TimeTideColors(
                    current = themeColors.base,
                    previous = lerp(themeColors.control, reportColors.danger, 0.42f),
                    average = themeColors.encourage,
                    control = themeColors.control,
                    saved = reportColors.positive,
                    surface = colorScheme.surface,
                    onSurface = colorScheme.onSurface,
                    muted = colorScheme.onSurfaceVariant,
                    backgroundStart = lerp(colorScheme.surface, themeColors.baseContainer, 0.62f),
                    backgroundMid = lerp(colorScheme.surface, themeColors.encourageContainer, 0.38f),
                    backgroundEnd = lerp(colorScheme.surface, themeColors.controlContainer, 0.30f),
                )
            ReportCard {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = colors.backgroundStart.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.24f)),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        TimeTideHeader(colors = colors)
                        TimeTideWavePanel(
                            data = timeTideState.data,
                            colors = colors,
                            animateValues = animateValues,
                        )
                        TimeTideMetricGrid(
                            metrics = timeTideState.data.metrics,
                            colors = colors,
                            animateValues = animateValues,
                        )
                        TimeTideSummaryPanel(
                            title = timeTideState.data.summaryTitle,
                            body = timeTideState.data.summaryBody,
                            colors = colors,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeTideHeader(colors: TimeTideColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = AppText.t("stats_time_tide_title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = colors.current.copy(alpha = 0.13f),
                    border = BorderStroke(1.dp, colors.current.copy(alpha = 0.18f)),
                ) {
                    Text(
                        text = AppText.t("stats_time_tide_badge"),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.current,
                    )
                }
            }
            Text(
                text = AppText.t("stats_time_tide_description"),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
        }
        Icon(
            imageVector = Icons.Default.Timeline,
            contentDescription = null,
            tint = colors.current,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun TimeTideWavePanel(
    data: DailyTimeTideSectionData,
    colors: TimeTideColors,
    animateValues: Boolean,
) {
    val maxValue =
        (
            data.currentHourlyMillis +
                data.previousHourlyMillis +
                data.averageHourlyMillis
            ).maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val currentPeak = data.currentHourlyMillis.peakPoint()
    val previousPeak = data.previousHourlyMillis.peakPoint()
    val averagePeak = data.averageHourlyMillis.peakPoint()
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.08f)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colors.backgroundStart,
                                colors.backgroundMid,
                                colors.backgroundEnd,
                            ),
                        ),
                    ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawTimeTideBackdrop(colors = colors)
                drawTimeTideAxis(colors = colors)
                drawTimeTideCurve(
                    values = data.averageHourlyMillis,
                    maxValue = maxValue,
                    color = colors.average,
                    strokeWidth = 3.dp.toPx(),
                    alpha = 0.72f,
                )
                drawTimeTideCurve(
                    values = data.previousHourlyMillis,
                    maxValue = maxValue,
                    color = colors.previous,
                    strokeWidth = 3.5.dp.toPx(),
                    alpha = 0.78f,
                )
                drawTimeTideCurve(
                    values = data.currentHourlyMillis,
                    maxValue = maxValue,
                    color = colors.current,
                    strokeWidth = 5.dp.toPx(),
                    alpha = 0.95f,
                )
                drawTimeTidePoint(currentPeak, maxValue, colors.current, 7.dp.toPx())
                drawTimeTidePoint(previousPeak, maxValue, colors.previous, 5.5.dp.toPx())
                drawTimeTidePoint(averagePeak, maxValue, colors.average, 5.5.dp.toPx())
            }
            TimeTideAxisLabels(
                colors = colors,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .offset(y = 6.dp)
                        .padding(horizontal = 30.dp),
            )
            TimeTidePeakBubble(
                label = data.currentLabel,
                valueMillis = data.currentTotalMillis,
                color = colors.current,
                animateValues = animateValues,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp),
            )
            Row(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimeTideLegendDot(label = data.currentLabel, color = colors.current)
                TimeTideLegendDot(label = data.previousLabel, color = colors.previous)
                TimeTideLegendDot(label = data.averageLabel, color = colors.average)
            }
        }
    }
}

@Composable
private fun TimeTideAxisLabels(
    colors: TimeTideColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            AppText.t("stats_time_tide_axis_06"),
            AppText.t("stats_time_tide_axis_12"),
            AppText.t("stats_time_tide_axis_18"),
            AppText.t("stats_time_tide_axis_24"),
        ).forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.muted.copy(alpha = 0.78f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TimeTidePeakBubble(
    label: String,
    valueMillis: Long,
    color: Color,
    animateValues: Boolean,
    modifier: Modifier = Modifier,
) {
    val valueText = formatTideMinutes(valueMillis)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
            if (animateValues) {
                AnimatedMetricText(
                    rawText = valueText,
                    label = "time_tide_peak_$valueText",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            } else {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TimeTideLegendDot(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
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
}

@Composable
private fun TimeTideMetricGrid(
    metrics: List<TimeTideMetric>,
    colors: TimeTideColors,
    animateValues: Boolean,
) {
    AdaptiveRowGrid(
        itemCount = metrics.size,
        compactColumns = 2,
        expandedColumns = 4,
        horizontalSpacing = 10.dp,
        verticalSpacing = 10.dp,
    ) { modifier, index ->
        metrics.getOrNull(index)?.let { metric ->
            TimeTideMetricCard(
                metric = metric,
                color = colors.metricColor(metric.type),
                animateValues = animateValues,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun TimeTideMetricCard(
    metric: TimeTideMetric,
    color: Color,
    animateValues: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = timeTideMetricIcon(metric.type),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(19.dp),
                )
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val valueText = formatTideMinutes(metric.currentMillis)
            if (animateValues) {
                AnimatedMetricText(
                    rawText = valueText,
                    label = "time_tide_metric_${metric.type}_$valueText",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            } else {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                TimeTideDeltaLine(
                    label = AppText.t("stats_time_tide_vs_previous"),
                    currentMillis = metric.currentMillis,
                    baselineMillis = metric.previousMillis,
                    positiveIsGood = metric.type.positiveDeltaIsGood(),
                )
                TimeTideDeltaLine(
                    label = AppText.t("stats_time_tide_vs_average"),
                    currentMillis = metric.currentMillis,
                    baselineMillis = metric.averageMillis,
                    positiveIsGood = metric.type.positiveDeltaIsGood(),
                )
            }
        }
    }
}

@Composable
private fun TimeTideDeltaLine(
    label: String,
    currentMillis: Long,
    baselineMillis: Long?,
    positiveIsGood: Boolean,
) {
    val delta = baselineMillis?.let { currentMillis - it }
    val color =
        when {
            delta == null || delta == 0L -> MaterialTheme.colorScheme.onSurfaceVariant
            (delta > 0L) == positiveIsGood -> LocalReportColors.current.positive
            else -> LocalReportColors.current.danger
        }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = formatTideDelta(delta),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun TimeTideSummaryPanel(
    title: String,
    body: String,
    colors: TimeTideColors,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = lerp(MaterialTheme.colorScheme.surface, colors.current, 0.10f).copy(alpha = 0.92f),
        border = BorderStroke(1.dp, colors.current.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = colors.current,
                modifier = Modifier.size(20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
            }
        }
    }
}

private data class TimeTideColors(
    val current: Color,
    val previous: Color,
    val average: Color,
    val control: Color,
    val saved: Color,
    val surface: Color,
    val onSurface: Color,
    val muted: Color,
    val backgroundStart: Color,
    val backgroundMid: Color,
    val backgroundEnd: Color,
)

private fun TimeTideColors.metricColor(type: TimeTideMetricType): Color =
    when (type) {
        TimeTideMetricType.TOTAL -> current
        TimeTideMetricType.ENCOURAGE -> average
        TimeTideMetricType.CONTROL -> control
        TimeTideMetricType.SAVED -> saved
    }

private fun timeTideMetricIcon(type: TimeTideMetricType): ImageVector =
    when (type) {
        TimeTideMetricType.TOTAL -> Icons.Default.Schedule
        TimeTideMetricType.ENCOURAGE -> Icons.Default.RocketLaunch
        TimeTideMetricType.CONTROL -> Icons.Default.Bolt
        TimeTideMetricType.SAVED -> Icons.Default.WbSunny
    }

private fun TimeTideMetricType.positiveDeltaIsGood(): Boolean =
    when (this) {
        TimeTideMetricType.TOTAL,
        TimeTideMetricType.CONTROL -> false
        TimeTideMetricType.ENCOURAGE,
        TimeTideMetricType.SAVED -> true
    }

private fun List<Long>.peakPoint(): Pair<Int, Long> {
    val index = indices.maxByOrNull { this[it] } ?: 0
    return index to getOrElse(index) { 0L }
}

private fun DrawScope.drawTimeTideBackdrop(colors: TimeTideColors) {
    drawCircle(
        color = colors.saved.copy(alpha = 0.22f),
        radius = 18.dp.toPx(),
        center = Offset(size.width * 0.12f, size.height * 0.34f),
    )
    val moonCenter = Offset(size.width * 0.88f, size.height * 0.22f)
    drawCircle(
        color = colors.surface.copy(alpha = 0.88f),
        radius = 17.dp.toPx(),
        center = moonCenter,
    )
    drawCircle(
        color = colors.backgroundStart,
        radius = 15.dp.toPx(),
        center = moonCenter + Offset(7.dp.toPx(), (-5).dp.toPx()),
    )
    val starColor = colors.onSurface.copy(alpha = 0.30f)
    listOf(
        0.22f to 0.18f,
        0.41f to 0.14f,
        0.58f to 0.20f,
        0.76f to 0.16f,
        0.94f to 0.35f,
    ).forEach { (x, y) ->
        drawCircle(
            color = starColor,
            radius = 2.dp.toPx(),
            center = Offset(size.width * x, size.height * y),
        )
    }
    drawPath(
        path =
            Path().apply {
                moveTo(0f, size.height * 0.72f)
                cubicTo(size.width * 0.26f, size.height * 0.56f, size.width * 0.48f, size.height * 0.82f, size.width, size.height * 0.62f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            },
        color = colors.average.copy(alpha = 0.10f),
    )
}

private fun DrawScope.drawTimeTideAxis(colors: TimeTideColors) {
    val rect = timeTideChartRect()
    val axisColor = colors.muted.copy(alpha = 0.36f)
    drawLine(
        color = axisColor,
        start = Offset(rect.left, rect.centerY()),
        end = Offset(rect.right, rect.centerY()),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 8.dp.toPx())),
    )
    listOf(6, 12, 18, 23).forEach { hour ->
        val x = rect.left + rect.width * (hour / 23f)
        drawCircle(
            color = colors.muted.copy(alpha = 0.52f),
            radius = 3.dp.toPx(),
            center = Offset(x, rect.centerY()),
        )
    }
}

private fun DrawScope.drawTimeTideCurve(
    values: List<Long>,
    maxValue: Long,
    color: Color,
    strokeWidth: Float,
    alpha: Float,
) {
    val points = timeTidePoints(values, maxValue)
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.zipWithNext().forEach { (from, to) ->
            val midX = (from.x + to.x) / 2f
            cubicTo(midX, from.y, midX, to.y, to.x, to.y)
        }
    }
    drawPath(
        path = path,
        color = color.copy(alpha = 0.16f * alpha),
        style = Stroke(width = strokeWidth + 5.dp.toPx(), cap = StrokeCap.Round),
    )
    drawPath(
        path = path,
        color = color.copy(alpha = alpha),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawTimeTidePoint(
    peak: Pair<Int, Long>,
    maxValue: Long,
    color: Color,
    radius: Float,
) {
    val point = timeTidePoint(peak.first, peak.second, maxValue)
    drawCircle(
        color = Color.White.copy(alpha = 0.92f),
        radius = radius + 3.dp.toPx(),
        center = point,
    )
    drawCircle(
        color = color,
        radius = radius,
        center = point,
    )
}

private data class TimeTideChartRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    fun centerY(): Float = top + height * 0.55f
}

private fun DrawScope.timeTideChartRect(): TimeTideChartRect =
    TimeTideChartRect(
        left = 22.dp.toPx(),
        top = 78.dp.toPx(),
        right = size.width - 22.dp.toPx(),
        bottom = size.height - 54.dp.toPx(),
    )

private fun DrawScope.timeTidePoints(values: List<Long>, maxValue: Long): List<Offset> =
    (0 until 24).map { hour ->
        timeTidePoint(hour, values.getOrElse(hour) { 0L }, maxValue)
    }

private fun DrawScope.timeTidePoint(hour: Int, value: Long, maxValue: Long): Offset {
    val rect = timeTideChartRect()
    val x = rect.left + rect.width * (hour.coerceIn(0, 23) / 23f)
    val ratio = kotlin.math.sqrt((value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f))
    val y = rect.bottom - rect.height * (0.12f + ratio * 0.78f)
    return Offset(x, y)
}

private fun formatTideMinutes(millis: Long): String {
    val minutes = (millis / 60_000L).coerceAtLeast(0L)
    return AppText.t("stats_time_tide_minutes_value", minutes)
}

private fun formatTideDelta(deltaMillis: Long?): String {
    if (deltaMillis == null) return AppText.t("stats_not_enough_samples")
    val minutes = kotlin.math.abs(deltaMillis / 60_000L)
    return when {
        deltaMillis > 0L -> AppText.t("stats_time_tide_delta_positive", minutes)
        deltaMillis < 0L -> AppText.t("stats_time_tide_delta_negative", minutes)
        else -> AppText.t("stats_time_tide_delta_flat")
    }
}

@Composable
internal fun DailyRhythmCard(
    timelineState: SectionState<TimelineSectionData>,
    focusState: SectionState<DailyFocusSectionData> = SectionState.Empty,
    subtitle: String = AppText.t("stats_time_flow_description"),
    emptyMessage: String = AppText.t("stats_this_archived_day_does_not_have_enough_usage"),
    weeklyAppFocusDays: List<WeeklyAppFocusDay> = emptyList(),
    behaviorMapData: BehaviorMapSectionData? = null,
) {
    val context = LocalContext.current
    val preferences = remember(context) { ManagedAppPreferences(context.applicationContext) }
    val showCellIcons by preferences.dailyRhythmCellIconsEnabled.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    var weeklySortByOpens by rememberSaveable { mutableStateOf(true) }
    var selectedRhythmAppPackage by remember(timelineState) { mutableStateOf<String?>(null) }
    val isWeekly = (timelineState as? SectionState.Ready)?.data?.let(::isPeriodRhythmTimeline) == true
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (timelineState is SectionState.Ready) {
                DailyRhythmHeader(
                    subtitle = subtitle,
                    showIconToggle = true,
                    isWeekly = isWeekly,
                    weeklySortByOpens = weeklySortByOpens,
                    onWeeklySortChange = { weeklySortByOpens = it },
                    showCellIcons = showCellIcons,
                    onShowCellIconsChange = { enabled ->
                        scope.launch(Dispatchers.IO) {
                            preferences.setDailyRhythmCellIconsEnabled(enabled)
                        }
                    },
                )
            } else {
                SectionHeader(
                    icon = Icons.Default.Timeline,
                    title = AppText.t("stats_time_flow"),
                    subtitle = subtitle,
                )
            }
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
                                0 -> AppText.t("stats_today_usage")
                                1 -> AppText.t("stats_hours")
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
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is SectionState.Ready -> {
                    DailyRhythmProfileStrip(
                        data = timelineState.data,
                        showCellIcons = showCellIcons,
                        selectedPackageName = selectedRhythmAppPackage,
                        onPackageClick = { packageName ->
                            selectedRhythmAppPackage =
                                packageName.takeUnless { it == selectedRhythmAppPackage }
                        },
                    )
                    if (weeklyAppFocusDays.isNotEmpty()) {
                        WeeklyRhythmAppFocusSection(
                            days = weeklyAppFocusDays,
                            sortByOpens = weeklySortByOpens,
                            selectedPackageName = selectedRhythmAppPackage,
                            onPackageClick = { packageName ->
                                selectedRhythmAppPackage =
                                    packageName.takeUnless { it == selectedRhythmAppPackage }
                            },
                        )
                        selectedRhythmAppPackage
                            ?.let { selectedPackage ->
                                behaviorMapData?.points?.firstOrNull { it.packageName == selectedPackage }
                            }
                            ?.let { selectedPoint ->
                                BehaviorMapSelectedPointCard(point = selectedPoint)
                            }
                    } else {
                        DailyRhythmInsightStrip(data = timelineState.data)
                    }
                }
            }
        }
    }
}

private fun isPeriodRhythmTimeline(data: TimelineSectionData): Boolean =
    data.gridRows > 1 && data.sliceCellsAreGridOrdered

@Composable
private fun DailyRhythmHeader(
    subtitle: String,
    showIconToggle: Boolean,
    isWeekly: Boolean,
    weeklySortByOpens: Boolean,
    onWeeklySortChange: (Boolean) -> Unit,
    showCellIcons: Boolean,
    onShowCellIconsChange: (Boolean) -> Unit,
) {
    DailyReportSectionHeader(
        icon = Icons.Default.Schedule,
        title = AppText.t("stats_time_flow"),
        subtitle = subtitle,
        accent = LocalThemeColors.current.base,
        trailing =
            if (isWeekly || showIconToggle) {
                {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isWeekly) {
                            WeeklyRhythmSortToggle(
                                sortByOpens = weeklySortByOpens,
                                onSortByOpensChange = onWeeklySortChange,
                            )
                        }
                        if (showIconToggle) {
                            FlatRhythmIconToggle(
                                checked = showCellIcons,
                                onCheckedChange = onShowCellIconsChange,
                            )
                        }
                    }
                }
            } else {
                null
            },
    )
}

@Composable
internal fun DailyReportSectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    trailing: (@Composable () -> Unit)? = null,
) {
    val themeColors = LocalThemeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            trailing?.invoke()
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyRhythmProfileStrip(
    data: TimelineSectionData,
    showCellIcons: Boolean,
    selectedPackageName: String? = null,
    onPackageClick: (String) -> Unit = {},
) {
    val visibleLegend = data.appLegend.filter { it.millis >= 60_000L }
    val appPackages =
        (
            visibleLegend.map { it.packageName } +
                data.sliceCells.mapNotNull { it.packageName } +
                data.appSliceCells.mapNotNull { it.packageName } +
                data.buckets.flatMap { bucket -> bucket.appSegments.map { it.packageName } }
            )
            .filter { it != TIMELINE_OTHER_APPS_PACKAGE_NAME }
            .distinct()
    val appColors = rememberAppChartColors(appPackages)
    val palette = LocalReportColors.current.appChartPalette
    val otherAppsColor = MaterialTheme.colorScheme.primary
    val legendColors =
        buildMap {
            put(TIMELINE_OTHER_APPS_PACKAGE_NAME, otherAppsColor)
            appPackages.forEachIndexed { index, packageName ->
                put(
                    packageName,
                    appColors[packageName] ?: stableAppFallbackColor(packageName, palette),
                )
            }
        }
    val selectedAppSliceCells =
        selectedPackageName?.let { selected ->
            data.appSliceCells.filter { it.packageName == selected }
        }.orEmpty()
    val selectedBuckets =
        selectedPackageName?.let { selected ->
            data.buckets.map { bucket ->
                val selectedSegments = bucket.appSegments.filter { it.packageName == selected }
                bucket.copy(
                    deviceMillis = selectedSegments.sumOf { it.millis },
                    appSegments = selectedSegments,
                )
            }
        } ?: data.buckets
    val selectedLegend =
        selectedPackageName?.let { selected -> visibleLegend.filter { it.packageName == selected } } ?: visibleLegend
    val allHeatCells =
        if (data.sliceCells.isNotEmpty()) {
            buildRhythmHeatCellsFromSlices(
                sliceCells = data.sliceCells,
                gridRows = data.gridRows,
                sliceCellsAreGridOrdered = data.sliceCellsAreGridOrdered,
                colors = legendColors,
                fallbackColor = otherAppsColor,
            )
        } else {
            buildMockRhythmHeatCells(
                buckets = data.buckets,
                cellsPerHour = data.cellsPerHour,
                legend = visibleLegend,
                colors = legendColors,
            )
        }
    val heatCells =
        if (selectedPackageName == null) {
            allHeatCells
        } else if (selectedAppSliceCells.isNotEmpty()) {
            buildRhythmHeatCellsFromSlices(
                sliceCells = selectedAppSliceCells,
                gridRows = data.gridRows,
                sliceCellsAreGridOrdered = data.sliceCellsAreGridOrdered,
                colors = legendColors,
                fallbackColor = otherAppsColor,
            )
        } else {
            buildMockRhythmHeatCells(
                buckets = selectedBuckets,
                cellsPerHour = data.cellsPerHour,
                legend = selectedLegend,
                colors = legendColors,
            )
        }

    if (isPeriodRhythmTimeline(data)) {
        RhythmDotChart(
            cells = heatCells,
            gridRows = data.gridRows,
            rowLabels = data.gridRowLabels,
            showCellIcons = showCellIcons,
            selectedPackageName = selectedPackageName,
            scaleMaxUsageMillis = allHeatCells.maxOfOrNull { it.usageMillis }?.coerceAtLeast(1L) ?: 1L,
        )
    } else {
        RhythmHeatMapPanel(
            cells = heatCells,
            visibleLegend = visibleLegend,
            legendColors = legendColors,
            showCellIcons = showCellIcons,
            gridRows = data.gridRows,
            rowLabels = data.gridRowLabels,
            selectedPackageName = selectedPackageName,
            onPackageClick = onPackageClick,
        )
    }
}

@Composable
private fun FlatRhythmIconToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val trackColor =
        if (checked) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }
    val thumbColor =
        if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
        }
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = AppText.t("stats_rhythm_cell_icons"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            lineHeight = MaterialTheme.typography.labelSmall.fontSize,
        )
        Box(
            modifier =
                Modifier
                    .size(width = 30.dp, height = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(trackColor)
                    .padding(2.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(thumbColor),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RhythmDotChart(
    cells: List<MockRhythmHeatCell>,
    gridRows: Int,
    rowLabels: List<String>,
    showCellIcons: Boolean,
    selectedPackageName: String?,
    scaleMaxUsageMillis: Long,
) {
    val visibleCells =
        remember(cells, selectedPackageName) {
            selectedPackageName?.let { selected ->
                cells.map { cell ->
                    if (cell.packageName == selected || cell.usageMillis <= 0L) {
                        cell
                    } else {
                        cell.copy(
                            packageName = null,
                            color = null,
                            intensity = 0f,
                            usageMillis = 0L,
                            totalMillis = 0L,
                        )
                    }
                }
            } ?: cells
        }
    val iconBitmaps = rememberRhythmHeatCellIcons(visibleCells)
    val cellsByRow = visibleCells.groupBy { it.slot }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)),
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            val labelWidth = if (rowLabels.isEmpty()) 0.dp else 28.dp
            val gridWidth = maxWidth - labelWidth
            val cellWidth = ((gridWidth - RhythmDotChartGap * 23) / 24).coerceAtLeast(2.dp)
            val cellSize = cellWidth
            val rowCount = gridRows.coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    (0 until rowCount).forEach { row ->
                    val rowCells = cellsByRow[row].orEmpty().associateBy { it.hour }
                    Row(
                        modifier = Modifier.fillMaxWidth().height(cellSize),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (labelWidth > 0.dp) {
                            Text(
                                text = rowLabels.getOrNull(row).orEmpty(),
                                modifier = Modifier.width(labelWidth),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            (0 until 24).forEach { hour ->
                                val cell = rowCells[hour]
                                val usageMillis = cell?.usageMillis ?: 0L
                                val sizeRatio = (usageMillis.toFloat() / scaleMaxUsageMillis.toFloat()).coerceIn(0f, 1f)
                                val tileSize =
                                    if (usageMillis > 0L) {
                                        (cellSize * (0.24f + 0.66f * sizeRatio)).coerceAtLeast(2.dp)
                                    } else {
                                        (cellSize * 0.18f).coerceAtLeast(2.dp)
                                    }
                                val dotColor =
                                    if (usageMillis > 0L) {
                                        cell?.color?.copy(alpha = 0.92f) ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.66f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.74f)
                                    }
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(tileSize)
                                            .clip(CircleShape)
                                            .background(dotColor),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (showCellIcons) {
                                            cell?.packageName?.let { packageName ->
                                                iconBitmaps[packageName]?.let { bitmap ->
                                                    Image(
                                                        bitmap = bitmap,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier
                                                            .fillMaxSize(0.56f)
                                                            .clip(CircleShape),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                    Row(
                        modifier = Modifier.padding(start = labelWidth).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        listOf("0", "6", "12", "18", "24").forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
            }
        }
    }
}

private val RhythmDotChartGap = 0.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RhythmHeatMapPanel(
    cells: List<MockRhythmHeatCell>,
    visibleLegend: List<DailyTimelineAppLegendItem>,
    legendColors: Map<String, Color>,
    showCellIcons: Boolean,
    gridRows: Int,
    rowLabels: List<String>,
    selectedPackageName: String?,
    onPackageClick: (String) -> Unit,
) {
    val visibleCells =
        remember(cells, selectedPackageName) {
            selectedPackageName?.let { selected ->
                cells.map { cell ->
                    if (cell.packageName == selected || cell.usageMillis <= 0L) {
                        cell
                    } else {
                        cell.copy(
                            packageName = null,
                            color = null,
                            intensity = 0f,
                            usageMillis = 0L,
                            totalMillis = 0L,
                        )
                    }
                }
            } ?: cells
        }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                RhythmHeatGrid(
                    cells = visibleCells,
                    showCellIcons = showCellIcons,
                    gridRows = gridRows,
                    rowLabels = rowLabels,
                    modifier = Modifier.fillMaxWidth(),
                )
                RhythmHeatHourScale(modifier = Modifier.fillMaxWidth())
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visibleLegend.forEach { item ->
                    RhythmLegendPill(
                        color = legendColors[item.packageName] ?: MaterialTheme.colorScheme.primary,
                        label = "${item.label} · ${formatDuration(item.millis)}",
                        packageName = item.packageName.takeIf { it != TIMELINE_OTHER_APPS_PACKAGE_NAME },
                        selected = selectedPackageName == item.packageName,
                        muted = selectedPackageName != null && selectedPackageName != item.packageName,
                        onClick =
                            item.packageName
                                .takeIf { it != TIMELINE_OTHER_APPS_PACKAGE_NAME }
                                ?.let { packageName -> { onPackageClick(packageName) } },
                    )
                }
                RhythmLegendPill(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.76f),
                    label = AppText.t("stats_rhythm_legend_idle"),
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun RhythmHeatHourScale(
    modifier: Modifier = Modifier,
) {
    val ticks =
        listOf(
            0 to "0",
            6 to "6",
            12 to "12",
            18 to "18",
            23 to "24",
        )
    Layout(
        modifier = modifier,
        content = {
            ticks.forEach { (_, label) ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        },
    ) { measurables, constraints ->
        val gapPx = RhythmHeatCellGap.roundToPx()
        val availableWidth = constraints.maxWidth.coerceAtLeast(0)
        val cellSize = ((availableWidth - gapPx * 23) / 24).coerceAtLeast(1)
        val width = cellSize * 24 + gapPx * 23
        val placeables =
            measurables.map { measurable ->
                measurable.measure(
                    androidx.compose.ui.unit.Constraints(
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight,
                    ),
                )
            }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val column = ticks[index].first
                val centerX = column * (cellSize + gapPx) + cellSize / 2
                val x = (centerX - placeable.width / 2).coerceIn(0, width - placeable.width)
                placeable.placeRelative(x = x, y = 0)
            }
        }
    }
}

@Composable
private fun RhythmHeatGrid(
    cells: List<MockRhythmHeatCell>,
    showCellIcons: Boolean,
    gridRows: Int,
    rowLabels: List<String>,
    modifier: Modifier = Modifier,
) {
    if (rowLabels.isEmpty()) {
        RhythmHeatGridLayout(
            cells = cells,
            showCellIcons = showCellIcons,
            gridRows = gridRows,
            modifier = modifier,
        )
        return
    }
    BoxWithConstraints(modifier = modifier) {
        val labelWidth = 28.dp
        val gapPx = with(LocalDensity.current) { RhythmHeatCellGap.roundToPx() }
        val gridWidthPx = with(LocalDensity.current) { (maxWidth - labelWidth).toPx().toInt() }
        val cellWidthPx = ((gridWidthPx - gapPx * 23) / 24).coerceAtLeast(1)
        val cellHeightDp = with(LocalDensity.current) { (cellWidthPx * 1.5f).toDp() }
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.width(labelWidth)) {
                rowLabels.take(gridRows).forEach { label ->
                    Box(
                        modifier = Modifier.height(cellHeightDp + RhythmHeatCellGap),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
            RhythmHeatGridLayout(
                cells = cells,
                showCellIcons = showCellIcons,
                gridRows = gridRows,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RhythmHeatGridLayout(
    cells: List<MockRhythmHeatCell>,
    showCellIcons: Boolean,
    gridRows: Int,
    modifier: Modifier = Modifier,
) {
    val iconBitmaps = rememberRhythmHeatCellIcons(cells)
    Layout(
        modifier = modifier,
        content = {
            cells.forEach { cell ->
                RhythmHeatCellBox(
                    cell = cell,
                    iconBitmap = cell.packageName?.let { iconBitmaps[it] },
                    showIcon = showCellIcons,
                )
            }
        },
    ) { measurables, constraints ->
        val gapPx = RhythmHeatCellGap.roundToPx()
        val availableWidth = constraints.maxWidth.coerceAtLeast(0)
        val cellWidth = ((availableWidth - gapPx * 23) / 24).coerceAtLeast(1)
        val cellHeight = cellWidth
        val width = cellWidth * 24 + gapPx * 23
        val height = cellHeight * gridRows + gapPx * (gridRows - 1)
        val placeables =
            measurables.map { measurable ->
                measurable.measure(
                    androidx.compose.ui.unit.Constraints.fixed(cellWidth, cellHeight),
                )
            }
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val cell = cells.getOrNull(index)
                val hour = cell?.hour ?: (index / gridRows)
                val slot = cell?.slot ?: (index % gridRows)
                placeable.placeRelative(
                    x = hour * (cellWidth + gapPx),
                    y = slot * (cellHeight + gapPx),
                )
            }
        }
    }
}

@Composable
private fun RhythmHeatCellBox(
    cell: MockRhythmHeatCell,
    iconBitmap: ImageBitmap?,
    showIcon: Boolean,
) {
    val fillColor =
        if (cell.usageMillis <= 0L) {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.76f)
        } else {
            cell.color?.copy(alpha = rhythmCellOpacity(cell.usageMillis))
                ?: MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.76f)
        }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(3.dp))
                .background(fillColor),
        contentAlignment = Alignment.Center,
    ) {
        if (showIcon && iconBitmap != null) {
            val iconOpacity = rhythmCellOpacity(cell.usageMillis)
            Box(
                modifier =
                    Modifier
                        .fillMaxSize(RhythmHeatCellIconScale)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = iconOpacity },
                )
            }
        }
    }
}

@Composable
private fun rememberRhythmHeatCellIcons(
    cells: List<MockRhythmHeatCell>,
): Map<String, ImageBitmap> {
    val context = LocalContext.current
    val packageNames =
        remember(cells) {
            cells
                .mapNotNull { it.packageName }
                .filter { it != TIMELINE_OTHER_APPS_PACKAGE_NAME }
                .distinct()
        }
    return remember(context, packageNames) {
        packageNames.mapNotNull { packageName ->
            val icon = AppVisualCache.getIcon(context, packageName) ?: return@mapNotNull null
            val bitmap =
                icon.toBitmap(width = 48, height = 48, config = Bitmap.Config.ARGB_8888)
                    .asImageBitmap()
            packageName to bitmap
        }.toMap()
    }
}

@Composable
private fun RhythmLegendPill(
    color: Color,
    label: String,
    packageName: String? = null,
    selected: Boolean = false,
    muted: Boolean = false,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            onClick?.let {
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = it)
                    .padding(horizontal = 2.dp, vertical = 1.dp)
            } ?: Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(RhythmLegendSwatchSize)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = if (muted) 0.48f else 1f))
                    .then(
                        when {
                            selected -> Modifier.border(1.5.dp, color, RoundedCornerShape(4.dp))
                            borderColor != null -> Modifier.border(0.5.dp, borderColor, RoundedCornerShape(4.dp))
                            else -> Modifier
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            packageName?.let {
                RhythmLegendAppIcon(packageName = it, muted = muted)
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (muted) 0.46f else 1f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RhythmLegendAppIcon(
    packageName: String,
    muted: Boolean,
) {
    val context = LocalContext.current
    val icon = remember(context, packageName) {
        AppVisualCache.getIcon(context, packageName)
    }
    if (icon != null) {
        val bitmap = remember(icon) {
            icon.toBitmap(width = 48, height = 48, config = Bitmap.Config.ARGB_8888).asImageBitmap()
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = if (muted) behaviorMapDisabledColorFilter() else null,
            modifier =
                Modifier
                    .size(RhythmLegendSwatchSize * RhythmLegendIconScale)
                    .clip(RoundedCornerShape(3.dp))
                    .graphicsLayer { alpha = if (muted) 0.42f else 1f },
        )
    }
}

private val RhythmLegendSwatchSize = 16.dp
private val RhythmHeatCellGap = 3.dp
private const val RhythmHeatCellIconScale = 0.52f
private const val RhythmLegendIconScale = 0.60f

private data class MockRhythmHeatCell(
    val hour: Int,
    val slot: Int,
    val packageName: String?,
    val color: Color?,
    val intensity: Float,
    val usageMillis: Long,
    val totalMillis: Long = usageMillis,
)

private fun buildMockRhythmHeatCells(
    buckets: List<DailyTimelineBucket>,
    cellsPerHour: Int,
    legend: List<DailyTimelineAppLegendItem>,
    colors: Map<String, Color>,
): List<MockRhythmHeatCell> {
    val visiblePackages = legend.map { it.packageName }.toSet()
    return buckets.flatMap { bucket ->
        val hourFillRatio = (bucket.deviceMillis.toFloat() / (60L * 60_000L).toFloat()).coerceIn(0f, 1f)
        val activeSlots =
            when {
                bucket.deviceMillis <= 0L -> 0
                else -> ceil(hourFillRatio * cellsPerHour.toFloat()).toInt().coerceIn(1, cellsPerHour)
            }
        val segments =
            bucket.appSegments
                .filter { it.millis > 0L && it.packageName in visiblePackages }
                .sortedByDescending { it.millis }
                .ifEmpty { bucket.appSegments.filter { it.millis > 0L }.sortedByDescending { it.millis } }
        val packageForSlot = buildSlotPackageSequence(segments, activeSlots)
        val activeSlotIndexes = buildMockActiveSlotIndexes(bucket, activeSlots, cellsPerHour)
        (0 until cellsPerHour).map { slot ->
            val activeIndex = activeSlotIndexes.indexOf(slot)
            val packageName = activeIndex.takeIf { it >= 0 }?.let { packageForSlot.getOrNull(it) }
            val slotFill =
                activeIndex.takeIf { it >= 0 }?.let {
                    val slotMillis = (60L * 60_000L) / cellsPerHour
                    val remainingMillis = bucket.deviceMillis - it * slotMillis
                    (remainingMillis.toFloat() / slotMillis.toFloat()).coerceIn(0.18f, 1f)
                } ?: 0f
            MockRhythmHeatCell(
                hour = bucket.hour,
                slot = slot,
                packageName = packageName,
                color = packageName?.let { colors[it] ?: colors[TIMELINE_OTHER_APPS_PACKAGE_NAME] },
                intensity = slotFill,
                usageMillis = (slotFill * ((60L * 60_000L) / cellsPerHour)).toLong(),
            )
        }
    }
}

private fun buildRhythmHeatCellsFromSlices(
    sliceCells: List<DailyTimelineSliceCell>,
    gridRows: Int,
    sliceCellsAreGridOrdered: Boolean,
    colors: Map<String, Color>,
    fallbackColor: Color,
): List<MockRhythmHeatCell> {
    val cellByIndex = sliceCells.associateBy { it.sliceIndex }
    return (0 until 24 * gridRows).map { sliceIndex ->
        val cell = cellByIndex[sliceIndex]
        val hour = if (sliceCellsAreGridOrdered) sliceIndex % 24 else sliceIndex / 12
        val slot = if (sliceCellsAreGridOrdered) sliceIndex / 24 else sliceIndex % 12
        MockRhythmHeatCell(
            hour = hour,
            slot = slot,
            packageName = cell?.packageName,
            color = cell?.packageName?.let { colors[it] ?: colors[TIMELINE_OTHER_APPS_PACKAGE_NAME] ?: fallbackColor },
            intensity = cell?.millis?.let { (it.toFloat() / (5L * 60_000L).toFloat()).coerceIn(0.12f, 1f) } ?: 0f,
            usageMillis = cell?.millis ?: 0L,
            totalMillis = cell?.totalMillis ?: 0L,
        )
    }
}

private fun buildMockActiveSlotIndexes(
    bucket: DailyTimelineBucket,
    activeSlots: Int,
    cellsPerHour: Int,
): List<Int> {
    if (activeSlots <= 0) return emptyList()
    if (activeSlots >= cellsPerHour) return (0 until cellsPerHour).toList()
    val seed =
        bucket.hour * 37 +
            bucket.deviceMillis.toInt() / 60_000 +
            bucket.appSegments.sumOf { it.packageName.hashCode() xor it.millis.toInt() }
    val preferredStart = ((seed % 12) + 12) % 12
    val step = when {
        activeSlots >= 8 -> 1
        activeSlots >= 5 -> 2
        else -> 3
    }
    val candidates =
        buildList {
            var cursor = preferredStart
        repeat(cellsPerHour) {
                add(cursor)
                cursor = (cursor + step) % 12
            }
            addAll(0 until cellsPerHour)
        }
    return candidates
        .distinct()
        .take(activeSlots)
        .sorted()
}

private fun buildSlotPackageSequence(
    segments: List<DailyTimelineAppSegment>,
    activeSlots: Int,
): List<String?> {
    if (activeSlots <= 0 || segments.isEmpty()) {
        return emptyList()
    }
    val total = segments.sumOf { it.millis }.coerceAtLeast(1L)
    return (0 until activeSlots).map { slot ->
        val cursor = ((slot + 0.5f) / activeSlots.toFloat()) * total.toFloat()
        var running = 0L
        segments.firstOrNull { segment ->
            running += segment.millis
            cursor <= running
        }?.packageName ?: segments.first().packageName
    }
}

private fun rhythmAppColor(
    packageName: String,
    index: Int,
    appColors: Map<String, Color>,
    fallbackPalette: List<Color>,
    reservedColor: Color,
): Color {
    val fallback = fallbackPalette[index % fallbackPalette.size]
    val rawColor = appColors[packageName] ?: fallback
    if (!isColorTooClose(rawColor, reservedColor)) {
        return rawColor
    }
    return fallbackPalette
        .drop(index)
        .plus(fallbackPalette.take(index))
        .firstOrNull { !isColorTooClose(it, reservedColor) }
        ?: rawColor
}

private fun isColorTooClose(
    first: Color,
    second: Color,
): Boolean {
    val distance =
        sqrt(
            (first.red - second.red) * (first.red - second.red) +
                (first.green - second.green) * (first.green - second.green) +
                (first.blue - second.blue) * (first.blue - second.blue),
        )
    return distance < 0.34f
}

private fun rhythmCellOpacity(usageMillis: Long): Float {
    val minutes = usageMillis.toFloat() / 60_000f
    return when {
        minutes <= 0f -> 0f
        minutes < 1f -> 0.2f
        minutes < 2f -> 0.4f
        minutes < 3f -> 0.6f
        minutes < 4f -> 0.8f
        else -> 1f
    }
}

private fun formatFiveMinuteSliceTime(sliceIndex: Int): String {
    val minutes = sliceIndex.coerceIn(0, 288) * 5
    return formatClockMinute(minutes)
}

private fun formatHourTime(hour: Int): String =
    formatClockMinute(hour.coerceIn(0, 24) * 60)

private fun formatClockMinute(totalMinutes: Int): String {
    val boundedMinutes = totalMinutes.coerceIn(0, 24 * 60)
    return String.format(
        Locale.getDefault(),
        "%02d:%02d",
        boundedMinutes / 60,
        boundedMinutes % 60,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyRhythmInsightStrip(
    data: TimelineSectionData,
) {
    val insights = remember(data) { buildRhythmInsights(data) }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        insights.forEach { insight ->
            RhythmInsightChip(insight)
        }
    }
}

@Composable
private fun WeeklyRhythmSortToggle(
    sortByOpens: Boolean,
    onSortByOpensChange: (Boolean) -> Unit,
) {
    val segmentWidth = 46.dp
    val indicatorOffset by animateDpAsState(
        targetValue = if (sortByOpens) 0.dp else segmentWidth,
        animationSpec = tween(
            durationMillis = 420,
            easing = FastOutSlowInEasing,
        ),
        label = "weekly_rhythm_sort_indicator",
    )
    Box(
        modifier = Modifier
            .width(segmentWidth * 2 + 4.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f))
            .padding(2.dp),
    ) {
        Surface(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
        ) {}
        listOf(
            true to AppText.t("stats_app_focus_sort_opens"),
            false to AppText.t("stats_app_focus_sort_usage"),
        ).forEachIndexed { index, (byOpens, label) ->
            val selected = sortByOpens == byOpens
            val labelAlpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0.62f,
                animationSpec = tween(durationMillis = 180),
                label = "weekly_rhythm_sort_label_$index",
            )
            Box(
                modifier = Modifier
                    .offset(x = segmentWidth * index)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSortByOpensChange(byOpens) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.graphicsLayer {
                        alpha = labelAlpha
                        scaleX = if (selected) 1f else 0.96f
                        scaleY = if (selected) 1f else 0.96f
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun WeeklyRhythmAppFocusSection(
    days: List<WeeklyAppFocusDay>,
    sortByOpens: Boolean,
    selectedPackageName: String?,
    onPackageClick: (String) -> Unit,
) {
    if (days.size <= 7) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            days.forEach { day ->
                WeeklyRhythmAppFocusDayColumn(
                    modifier = Modifier.weight(1f),
                    day = day,
                    sortByOpens = sortByOpens,
                    selectedPackageName = selectedPackageName,
                    onPackageClick = onPackageClick,
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            days.forEach { day ->
                WeeklyRhythmAppFocusDayColumn(
                    modifier = Modifier.width(44.dp),
                    day = day,
                    sortByOpens = sortByOpens,
                    selectedPackageName = selectedPackageName,
                    onPackageClick = onPackageClick,
                )
            }
        }
    }
}

@Composable
private fun WeeklyRhythmAppFocusDayColumn(
    modifier: Modifier,
    day: WeeklyAppFocusDay,
    sortByOpens: Boolean,
    selectedPackageName: String?,
    onPackageClick: (String) -> Unit,
) {
    val opensRanking = remember(day) {
        day.apps
            .sortedWith(
                compareByDescending<WeeklyAppFocusItem> { it.openCount }
                    .thenByDescending { it.usageMillis },
            )
    }
    val usageRanking = remember(day) {
        day.apps
            .sortedWith(
                compareByDescending<WeeklyAppFocusItem> { it.usageMillis }
                    .thenByDescending { it.usageMillis },
            )
    }
    val selectedRanking = if (sortByOpens) opensRanking else usageRanking
    val animatedApps = remember(opensRanking, usageRanking) {
        (opensRanking.take(7) + usageRanking.take(7)).distinctBy { it.packageName }
    }
    val maxMetric = selectedRanking.firstOrNull()
        ?.let { if (sortByOpens) it.openCount.toLong() else it.usageMillis }
        ?.coerceAtLeast(1L) ?: 1L
    val appColors = rememberAppChartColors(day.apps.map { it.packageName })
    val rankStep = 45.dp
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = day.dayCode,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .height(rankStep * 7 - 5.dp)
                .fillMaxWidth()
                .clipToBounds(),
        ) {
            animatedApps.forEach { app ->
                key(app.packageName) {
                    val targetRank = selectedRanking.indexOfFirst { it.packageName == app.packageName }
                    val visible = targetRank in 0..6
                    val targetY = if (visible) rankStep * targetRank else rankStep * 7
                    val metric = if (sortByOpens) app.openCount.toLong() else app.usageMillis
                    val ratio = (metric.toFloat() / maxMetric.toFloat()).coerceIn(0f, 1f)
                    val targetSize = (20f + ratio * 20f).dp
                    val animatedY by animateDpAsState(
                        targetValue = targetY,
                        animationSpec = tween(
                            durationMillis = 560,
                            easing = FastOutSlowInEasing,
                        ),
                        label = "weekly_rhythm_rank_y_${day.dayCode}_${app.packageName}",
                    )
                    val animatedSize by animateDpAsState(
                        targetValue = targetSize,
                        animationSpec = tween(
                            durationMillis = 560,
                            easing = FastOutSlowInEasing,
                        ),
                        label = "weekly_rhythm_rank_size_${day.dayCode}_${app.packageName}",
                    )
                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 360,
                            easing = FastOutSlowInEasing,
                        ),
                        label = "weekly_rhythm_rank_alpha_${day.dayCode}_${app.packageName}",
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = animatedY)
                            .size(40.dp)
                            .graphicsLayer { alpha = animatedAlpha }
                            .zIndex(if (visible) (7 - targetRank).toFloat() else 0f),
                        contentAlignment = Alignment.Center,
                    ) {
                        WeeklyRhythmAppFocusIcon(
                            packageName = app.packageName,
                            appLabel = app.label,
                            size = animatedSize,
                            accent = appColors[app.packageName] ?: MaterialTheme.colorScheme.primary,
                            selected = selectedPackageName == app.packageName,
                            muted =
                                selectedPackageName != null &&
                                    selectedPackageName != app.packageName,
                            onClick = { onPackageClick(app.packageName) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyRhythmAppFocusIcon(
    packageName: String,
    appLabel: String,
    size: Dp,
    accent: Color,
    selected: Boolean,
    muted: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val icon = remember(packageName) { AppVisualCache.getIcon(context, packageName) }
    val iconBitmap =
        remember(icon) {
            icon
                ?.toBitmap(width = 96, height = 96, config = Bitmap.Config.ARGB_8888)
                ?.copy(Bitmap.Config.ARGB_8888, false)
                ?.asImageBitmap()
        }
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(size),
            shape = RoundedCornerShape(size / 3),
            color = (if (muted) mutedColor else accent).copy(alpha = if (selected) 0.18f else 0.12f),
            border =
                BorderStroke(
                    if (selected) 2.dp else 1.dp,
                    (if (muted) mutedColor else accent).copy(alpha = if (selected) 0.72f else 0.22f),
                ),
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = appLabel,
                    colorFilter = if (muted) behaviorMapDisabledColorFilter() else null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(size * 0.12f)
                        .clip(RoundedCornerShape(size / 4))
                        .graphicsLayer { alpha = if (muted) 0.38f else 1f },
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Composable
private fun RhythmInsightChip(
    insight: RhythmInsight,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = insight.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = insight.value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class RhythmInsight(
    val label: String,
    val value: String,
)

private fun buildRhythmInsights(data: TimelineSectionData): List<RhythmInsight> {
    val appCount =
        data.appLegend.count {
            it.millis > 0L && it.packageName != TIMELINE_OTHER_APPS_PACKAGE_NAME
        }
    return listOf(
        RhythmInsight(
            label = AppText.t("stats_rhythm_longest_idle"),
            value = longestIdleLabel(data),
        ),
        RhythmInsight(
            label = AppText.t("stats_rhythm_active_hours_label"),
            value = AppText.t("stats_rhythm_active_hours_value", rhythmActiveHours(data)),
        ),
        RhythmInsight(
            label = AppText.t("stats_rhythm_app_count"),
            value = AppText.t("stats_rhythm_app_count_value", appCount),
        ),
    )
}

private fun rhythmActiveHours(data: TimelineSectionData): Int {
    if (data.sliceCells.isNotEmpty()) {
        return data.sliceCells
            .map { it.sliceIndex / 12 }
            .distinct()
            .count()
    }
    return data.buckets.count { it.deviceMillis > 0L }
}

private fun rhythmSwitchCount(data: TimelineSectionData): Int {
    val packages =
        if (data.sliceCells.isNotEmpty()) {
            val byIndex = data.sliceCells.associateBy { it.sliceIndex }
            (0 until 288).map { byIndex[it]?.packageName }
        } else {
            data.buckets.map { bucket ->
                bucket.appSegments.maxByOrNull { it.millis }?.packageName
            }
        }
    var previous: String? = null
    var switches = 0
    packages.forEach { packageName ->
        if (packageName != null && previous != null && previous != packageName) {
            switches += 1
        }
        if (packageName != null) {
            previous = packageName
        }
    }
    return switches
}

private fun longestIdleLabel(data: TimelineSectionData): String {
    if (data.sliceCells.isNotEmpty()) {
        val activeIndexes = data.sliceCells.map { it.sliceIndex }.toSet()
        val (start, length) = longestInactiveRun(288) { index -> index in activeIndexes }
        return if (length > 0) {
            AppText.t(
                "stats_rhythm_idle_range_value",
                formatFiveMinuteSliceTime(start),
                formatFiveMinuteSliceTime((start + length).coerceAtMost(288)),
                formatDuration(length * 5L * 60_000L),
            )
        } else {
            AppText.t("stats_none")
        }
    }
    val activeHours = data.buckets.filter { it.deviceMillis > 0L }.map { it.hour }.toSet()
    val (start, length) = longestInactiveRun(24) { hour -> hour in activeHours }
    return if (length > 0) {
        AppText.t(
            "stats_rhythm_idle_range_value",
            formatHourTime(start),
            formatHourTime((start + length).coerceAtMost(24)),
            formatDuration(length * 60L * 60_000L),
        )
    } else {
        AppText.t("stats_none")
    }
}

private fun longestInactiveRun(
    count: Int,
    isActive: (Int) -> Boolean,
): Pair<Int, Int> {
    var bestStart = 0
    var bestLength = 0
    var currentStart = 0
    var currentLength = 0
    for (index in 0 until count) {
        if (isActive(index)) {
            if (currentLength > bestLength) {
                bestStart = currentStart
                bestLength = currentLength
            }
            currentStart = index + 1
            currentLength = 0
        } else {
            currentLength += 1
        }
    }
    if (currentLength > bestLength) {
        bestStart = currentStart
        bestLength = currentLength
    }
    return bestStart to bestLength
}

@Composable
internal fun DailyAppFocusCard(
    topAppsState: SectionState<TopAppsSectionData>,
) {
    val topAppsData = (topAppsState as? SectionState.Ready)?.data
    val usageTopApps = topAppsData?.usageTopApps.orEmpty()
    val appProfiles = topAppsData?.appProfiles.orEmpty()
    val sliceCells = topAppsData?.sliceCells.orEmpty()
    val appColorPackages =
        remember(usageTopApps, sliceCells) {
            (usageTopApps.map { it.packageName } + sliceCells.mapNotNull { it.packageName })
                .distinct()
        }
    val appColors = rememberAppChartColors(appColorPackages)
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
                        DailyAppClockFocusPanel(
                            profiles = appProfiles,
                            sliceCells = sliceCells,
                            appColors = appColors,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyAppClockFocusPanel(
    profiles: List<AppFocusProfileItem>,
    sliceCells: List<DailyTimelineSliceCell>,
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
                AppUsageClockChart(
                    profiles = visibleProfiles,
                    sliceCells = sliceCells,
                    appColors = appColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 330.dp else 390.dp),
                )
                AdaptiveRowGrid(
                    itemCount = visibleProfiles.size,
                    compactColumns = 1,
                    expandedColumns = 1,
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
private fun AppUsageClockChart(
    profiles: List<AppFocusProfileItem>,
    sliceCells: List<DailyTimelineSliceCell>,
    appColors: Map<String, Color>,
    modifier: Modifier = Modifier,
) {
    val visibleProfiles = profiles.take(10)
    val outline = MaterialTheme.colorScheme.outlineVariant
    val totalUsageMillis = visibleProfiles.sumOf { it.usageMillis }.coerceAtLeast(1L)
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val reportPalette = LocalReportColors.current.appChartPalette.ifEmpty { listOf(Color(0xFF4F7DFF)) }
    val cellsByIndex = sliceCells.associateBy { it.sliceIndex }
    BoxWithConstraints(modifier = modifier) {
        val centerOffsetY = maxHeight * (UsageClockCenterYFraction - 0.5f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = usageClockCenter(size)
            val baseRadius = min(size.width, size.height) * 0.345f
            drawUsageClockBackdrop(
                center = center,
                outerRadius = baseRadius * 1.28f,
                outline = outline,
                surface = surface,
                primary = primary,
            )
            drawUsageClockHourlyRing(
                cellsByIndex = cellsByIndex,
                radius = baseRadius * 1.04f,
                emptyColor = outline,
                colorResolver = { cell ->
                    resolveUsageClockCellColor(cell, appColors, reportPalette, outline)
                },
            )
            drawUsageClockOuterGuide(
                center = center,
                radius = baseRadius * 1.30f,
                outline = outline,
            )
            drawUsageClockCenterGlow(
                center = center,
                radius = baseRadius * 0.28f,
                primary = primary,
                surface = surfaceVariant,
            )
        }
        UsageClockCenterBadge(
            totalUsageMillis = totalUsageMillis,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .offset(y = centerOffsetY),
            labelColor = onSurfaceVariant,
            valueColor = onSurface,
        )
    }
}

private const val UsageClockCenterXFraction = 0.5f
private const val UsageClockCenterYFraction = 0.52f
private const val UsageClockDayHours = 24
private const val UsageClockSlicesPerHour = 12
private const val UsageClockHourMillis = 60L * 60_000L

@Composable
private fun UsageClockCenterBadge(
    totalUsageMillis: Long,
    modifier: Modifier = Modifier,
    labelColor: Color,
    valueColor: Color,
) {
    Surface(
        modifier = modifier.size(102.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color.White.copy(alpha = 0.76f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                ),
                        ),
                    )
                    .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = AppText.t("history_total_duration"),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatDuration(totalUsageMillis),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = valueColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun usageClockCenter(size: Size): Offset =
    Offset(size.width * UsageClockCenterXFraction, size.height * UsageClockCenterYFraction)

private fun DrawScope.drawUsageClockBackdrop(
    center: Offset,
    outerRadius: Float,
    outline: Color,
    surface: Color,
    primary: Color,
) {
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        surface.copy(alpha = 0.12f),
                        primary.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                center = center,
                radius = outerRadius * 1.10f,
            ),
        radius = outerRadius * 1.10f,
        center = center,
    )
    drawCircle(
        color = outline.copy(alpha = 0.12f),
        radius = outerRadius,
        center = center,
        style = Stroke(width = max(1f, size.minDimension * 0.009f)),
    )
}

private fun DrawScope.drawUsageClockHourlyRing(
    cellsByIndex: Map<Int, DailyTimelineSliceCell>,
    radius: Float,
    emptyColor: Color,
    colorResolver: (DailyTimelineSliceCell) -> Color,
) {
    val sweep = 360f / UsageClockDayHours.toFloat()
    val particleHeight = 22.dp.toPx()
    val particleWidth = 8.dp.toPx()
    val corner = 3.2.dp.toPx()
    repeat(UsageClockDayHours) { hour ->
        val particle = resolveHourParticle(cellsByIndex, hour)
        val intensity = (particle.totalMillis.toFloat() / UsageClockHourMillis.toFloat()).coerceIn(0f, 1f)
        val color =
            particle.dominantCell
                ?.takeIf { particle.totalMillis > 0L }
                ?.let { colorResolver(it).copy(alpha = 0.42f + intensity * 0.54f) }
                ?: emptyColor.copy(alpha = 0.14f)
        val angle = -90f + (hour + 0.5f) * sweep
        val point = radialPoint(center, radius, angle)
        rotate(degrees = angle + 90f, pivot = point) {
            drawRoundRect(
                color = color,
                topLeft = Offset(point.x - particleWidth / 2f, point.y - particleHeight / 2f),
                size = Size(particleWidth, particleHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            )
        }
    }
}

private data class UsageClockParticle(
    val dominantCell: DailyTimelineSliceCell?,
    val totalMillis: Long,
)

private fun resolveHourParticle(
    cellsByIndex: Map<Int, DailyTimelineSliceCell>,
    hour: Int,
): UsageClockParticle {
    val startIndex = hour * UsageClockSlicesPerHour
    val cells = (0 until UsageClockSlicesPerHour).mapNotNull { offset -> cellsByIndex[startIndex + offset] }
    val totalMillis = cells.sumOf { it.millis }.coerceAtMost(UsageClockHourMillis)
    val millisByPackage =
        cells
            .filter { it.packageName != null && it.millis > 0L }
            .groupBy { it.packageName.orEmpty() }
            .mapValues { (_, packageCells) -> packageCells.sumOf { it.millis } }
    val dominant =
        millisByPackage
            .maxByOrNull { it.value }
            ?.key
            ?.let { packageName -> cells.firstOrNull { it.packageName == packageName } }
    return UsageClockParticle(
        dominantCell = dominant,
        totalMillis = totalMillis,
    )
}

private fun radialPoint(
    center: Offset,
    radius: Float,
    angleDegrees: Float,
): Offset {
    val radians = angleDegrees * PI.toFloat() / 180f
    return Offset(
        x = center.x + cos(radians) * radius,
        y = center.y + sin(radians) * radius,
    )
}

private fun resolveUsageClockCellColor(
    cell: DailyTimelineSliceCell,
    appColors: Map<String, Color>,
    fallbackPalette: List<Color>,
    outline: Color,
): Color {
    val packageName = cell.packageName ?: return outline
    return appColors[packageName]
        ?: fallbackPalette[(packageName.hashCode() and Int.MAX_VALUE) % fallbackPalette.size]
}

private fun DrawScope.drawUsageClockOuterGuide(
    center: Offset,
    radius: Float,
    outline: Color,
) {
    drawCircle(
        color = outline.copy(alpha = 0.16f),
        radius = radius,
        center = center,
        style = Stroke(width = max(1.4f, size.minDimension * 0.006f)),
    )
}

private fun DrawScope.drawUsageClockCenterGlow(
    center: Offset,
    radius: Float,
    primary: Color,
    surface: Color,
) {
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        surface.copy(alpha = 0.70f),
                        primary.copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                center = center,
                radius = radius * 1.45f,
            ),
        radius = radius * 1.45f,
        center = center,
    )
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
            DailyReportSectionHeader(
                icon = Icons.Default.Analytics,
                title = AppText.t("stats_behavior_analysis"),
                subtitle = AppText.t("stats_behavior_structure_description"),
                accent = themeColors.base,
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
    behaviorMapState: SectionState<BehaviorMapSectionData>,
) {
    ReportCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DailyReportSectionHeader(
                icon = Icons.Default.ScatterPlot,
                title = AppText.t("stats_behavior_map_title"),
                subtitle = AppText.t("stats_behavior_map_description"),
                accent = LocalThemeColors.current.base,
            )
            when (behaviorMapState) {
                SectionState.Loading -> {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth(),
                        height = 360.dp,
                        shape = RoundedCornerShape(22.dp),
                    )
                }
                SectionState.Empty -> {
                    Text(
                        text = AppText.t("stats_behavior_map_empty"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is SectionState.Ready -> {
                    BehaviorMapContent(data = behaviorMapState.data)
                }
            }
        }
    }
}

@Composable
private fun BehaviorMapContent(data: BehaviorMapSectionData) {
    var selectedPoint by remember(data.points) { mutableStateOf<BehaviorMapPoint?>(null) }
    var activeLegendRoles by remember(data.points) {
        mutableStateOf(setOf(BehaviorMapRole.CONTROL, BehaviorMapRole.ENCOURAGE, BehaviorMapRole.UNGROUPED))
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BehaviorMapScatter(
            data = data,
            selectedPoint = selectedPoint,
            activeLegendRoles = activeLegendRoles,
            onToggleLegendRole = { role ->
                activeLegendRoles =
                    if (role in activeLegendRoles) {
                        activeLegendRoles - role
                    } else {
                        activeLegendRoles + role
                    }
            },
            onSelectPoint = { selectedPoint = it },
        )
        selectedPoint?.let { BehaviorMapSelectedPointCard(point = it) }
        BehaviorMapMatrix(data = data)
    }
}

@Composable
private fun BehaviorMapScatter(
    data: BehaviorMapSectionData,
    selectedPoint: BehaviorMapPoint?,
    activeLegendRoles: Set<BehaviorMapRole>,
    onToggleLegendRole: (BehaviorMapRole) -> Unit,
    onSelectPoint: (BehaviorMapPoint) -> Unit,
) {
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.80f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BoxWithConstraints(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(286.dp),
            ) {
                val chartHeight = 286.dp
                val left = 12.dp
                val right = 8.dp
                val top = 14.dp
                val bottom = 38.dp
                val plotWidth = (maxWidth - left - right).coerceAtLeast(1.dp)
                val plotHeight = (chartHeight - top - bottom).coerceAtLeast(1.dp)
                val usageSamples = data.points.map { it.usageMillis.toDouble() }
                val openSamples = data.points.map { it.openCount.toDouble() }
                val thresholdXRatio = 0.5f
                val thresholdYRatio = 0.5f
                val thresholdX = left + plotWidth * thresholdXRatio
                val thresholdY = top + plotHeight * (1f - thresholdYRatio)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val leftPx = left.toPx()
                    val rightPx = size.width - right.toPx()
                    val topPx = top.toPx()
                    val bottomPx = size.height - bottom.toPx()
                    val thresholdXPx = thresholdX.toPx()
                    val thresholdYPx = thresholdY.toPx()
                    val crossLineColor = axisColor.copy(alpha = 0.34f)
                    val crossLineEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 9f), 0f)
                    drawLine(axisColor, Offset(leftPx, topPx), Offset(leftPx, bottomPx), strokeWidth = 2.4f)
                    drawLine(axisColor, Offset(leftPx, bottomPx), Offset(rightPx, bottomPx), strokeWidth = 2.4f)
                    drawLine(
                        color = crossLineColor,
                        start = Offset(thresholdXPx, topPx),
                        end = Offset(thresholdXPx, bottomPx),
                        strokeWidth = 1.8f,
                        pathEffect = crossLineEffect,
                    )
                    drawLine(
                        color = crossLineColor,
                        start = Offset(leftPx, thresholdYPx),
                        end = Offset(rightPx, thresholdYPx),
                        strokeWidth = 1.8f,
                        pathEffect = crossLineEffect,
                    )
                    drawCircle(axisColor, radius = 5f, center = Offset(leftPx, topPx))
                    drawCircle(axisColor, radius = 5f, center = Offset(rightPx, bottomPx))
                }

                BehaviorMapAxisLabel(
                    text = AppText.t("stats_behavior_map_y_axis"),
                    modifier = Modifier.offset(x = left + 34.dp, y = top - 9.dp),
                )
                BehaviorMapTickLabel(
                    text = "0",
                    modifier = Modifier.offset(x = left + 3.dp, y = chartHeight - bottom + 5.dp),
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = -right - 10.dp, y = (-19).dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BehaviorMapAxisLabel(text = AppText.t("stats_behavior_map_x_axis"))
                    BehaviorMapTickLabel(text = formatDuration(data.maxDurationMillis))
                }
                BehaviorMapTickLabel(
                    text = data.maxOpenCount.toString(),
                    modifier = Modifier.offset(x = left + 4.dp, y = top - 9.dp),
                )
                Box(
                    modifier =
                        Modifier
                            .offset(x = left, y = top - 8.dp)
                            .width(plotWidth * 0.5f),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    BehaviorMapCornerTag(text = AppText.t("stats_behavior_map_matrix_high_freq_low_time"))
                }
                Box(
                    modifier =
                        Modifier
                            .offset(x = thresholdX, y = top - 8.dp)
                            .width(plotWidth * 0.5f),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    BehaviorMapCornerTag(text = AppText.t("stats_behavior_map_matrix_high_freq_high_time"))
                }
                BehaviorMapCornerTag(
                    text = AppText.t("stats_behavior_map_matrix_low_freq_high_time"),
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = -right - 7.dp, y = -bottom - 8.dp),
                )

                data.points.forEach { point ->
                    val radius = behaviorMapIconRadius(point.attentionWeight)
                    val size = radius * 2f
                    val nodeSize = behaviorMapNodeSize(size)
                    val shadowMargin = behaviorMapIconShadowMargin(size)
                    val selected = selectedPoint?.packageName == point.packageName
                    val dimmed = point.role !in activeLegendRoles
                    val xRatio = behaviorMapBalancedRatio(
                        value = point.usageMillis.toDouble(),
                        samples = usageSamples,
                    )
                    val yRatio = behaviorMapBalancedRatio(
                        value = point.openCount.toDouble(),
                        samples = openSamples,
                    )
                    val jitterX = if (point.isHighlighted) 0.dp else behaviorMapJitter(point.packageName, salt = 0)
                    val jitterY = if (point.isHighlighted) 0.dp else behaviorMapJitter(point.packageName, salt = 11)
                    val centerX = left + plotWidth * xRatio + jitterX
                    val centerY = top + plotHeight * (1f - yRatio) + jitterY
                    val z =
                        when {
                            selected -> 10_000f
                            dimmed -> -10_000f - nodeSize.value
                            else -> 1_000f - nodeSize.value
                        }
                    BehaviorMapPointNode(
                        point = point,
                        size = size,
                        selected = selected,
                        dimmed = dimmed,
                        modifier =
                            Modifier
                                .offset(
                                    x = centerX - nodeSize / 2f - shadowMargin,
                                    y = centerY - nodeSize / 2f - shadowMargin,
                                )
                                .zIndex(z),
                        onClick = { onSelectPoint(point) },
                    )
                }
            }
            BehaviorMapLegend(
                activeRoles = activeLegendRoles,
                onToggleRole = onToggleLegendRole,
            )
        }
    }
}

private fun behaviorMapIconRadius(weight: Float): Dp {
    val normalized = weight.coerceIn(0f, 1f)
    val logScaled = kotlin.math.ln(1f + normalized * 15f) / kotlin.math.ln(16f)
    val radius = 5f + logScaled * 13f
    return radius.dp
}

private fun behaviorMapBalancedRatio(
    value: Double,
    samples: List<Double>,
): Float {
    val sortedSamples =
        samples
            .filter { it >= 0.0 }
            .sorted()
    if (sortedSamples.isEmpty()) return 0.5f
    if (sortedSamples.size == 1) return 0.5f

    val belowCount = sortedSamples.count { it < value }
    val equalCount = sortedSamples.count { it == value }.coerceAtLeast(1)
    val percentile = (belowCount + (equalCount - 1) * 0.5) / (sortedSamples.size - 1).toDouble()
    val balancedRatio =
        if (percentile <= BEHAVIOR_MAP_CENTER_PERCENTILE) {
            0.08 + percentile / BEHAVIOR_MAP_CENTER_PERCENTILE * 0.42
        } else {
            0.5 + (percentile - BEHAVIOR_MAP_CENTER_PERCENTILE) / (1.0 - BEHAVIOR_MAP_CENTER_PERCENTILE) * 0.42
        }
    return balancedRatio.toFloat().coerceIn(0.08f, 0.92f)
}

private const val BEHAVIOR_MAP_CENTER_PERCENTILE = 2.0 / 3.0

@Composable
private fun BehaviorMapAxisLabel(
    text: String,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier =
            if (vertical) {
                modifier.width(28.dp)
            } else {
                modifier
            },
    )
}

@Composable
private fun BehaviorMapTickLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
        modifier = modifier,
    )
}

@Composable
private fun BehaviorMapCornerTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    val baseColor = LocalThemeColors.current.base
    Surface(
        modifier = modifier.zIndex(80f),
        shape = RoundedCornerShape(4.dp),
        color = baseColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.18f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun behaviorMapNodeSize(iconSize: Dp): Dp {
    val ringStroke = behaviorMapRingStroke(iconSize)
    val ringSize = iconSize + ringStroke * 4f
    return maxOf(ringSize, 24.dp)
}

private fun behaviorMapRingStroke(iconSize: Dp): Dp =
    (iconSize * 0.045f).coerceIn(0.72.dp, 1.92.dp)

private fun behaviorMapIconShadowMargin(iconSize: Dp): Dp =
    (iconSize * 0.18f).coerceIn(3.dp, 8.dp)

private fun behaviorMapJitter(packageName: String, salt: Int): Dp {
    val bucket = ((packageName.hashCode() xor salt) and Int.MAX_VALUE) % 9
    return (bucket - 4).dp
}

@Composable
private fun BehaviorMapPointNode(
    point: BehaviorMapPoint,
    size: Dp,
    selected: Boolean,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val dimmedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
    val roleColor = if (dimmed) dimmedColor else behaviorMapRoleColor(point.role)
    val isGrouped = point.role != BehaviorMapRole.UNGROUPED
    val iconSize = size
    val ringStroke = behaviorMapRingStroke(size)
    val ringSize = size + ringStroke * 4f
    val outerRingWidth = ringStroke * 1.2f
    val shadowPlateSize = size + outerRingWidth * 2f
    val shadowMargin = behaviorMapIconShadowMargin(size)
    val nodeSize = behaviorMapNodeSize(size) + shadowMargin * 2f
    val iconColorFilter = if (dimmed) behaviorMapDisabledColorFilter() else null
    Box(
        modifier =
            modifier
                .size(nodeSize)
                .graphicsLayer { alpha = if (dimmed) 0.10f else 1f }
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBehaviorMapIconShadow(plateSize = shadowPlateSize, selected = selected)
        }
        Canvas(modifier = Modifier.size(ringSize)) {
            val outerRingWidthPx = outerRingWidth.toPx()
            val iconRadius = iconSize.toPx() / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRingColor = if (isGrouped) roleColor else Color.White.copy(alpha = 0.98f)
            drawCircle(
                color = outerRingColor,
                radius = iconRadius + outerRingWidthPx / 2f,
                center = center,
                style = Stroke(width = outerRingWidthPx),
            )
        }
        BehaviorMapAppIcon(
            pkg = point.packageName,
            size = size,
            iconPadding = 0.dp,
            showBorder = false,
            colorFilter = iconColorFilter,
        )
    }
}

private fun DrawScope.drawBehaviorMapIconShadow(
    plateSize: Dp,
    selected: Boolean,
) {
    val plateRadius = plateSize.toPx() / 2f
    if (plateRadius <= 0f) return

    val center = Offset(size.width / 2f, size.height / 2f)
    val lift = if (selected) 1.14f else 1f
    val shadowScale = (plateRadius / 20.dp.toPx()).coerceIn(0.70f, 1f)
    val shadowWidth = 1.35.dp.toPx() * shadowScale
    val shadowFeather = 2.15.dp.toPx() * shadowScale
    val edgeShadowRadius = plateRadius + shadowFeather
    val darkStop = (plateRadius / edgeShadowRadius).coerceIn(0f, 1f)
    val innerStop = ((plateRadius - shadowWidth) / edgeShadowRadius).coerceIn(0f, darkStop)
    val outerStop = ((plateRadius + shadowWidth) / edgeShadowRadius).coerceIn(darkStop, 1f)
    val ambientCenter = center + Offset(0.65.dp.toPx(), 1.05.dp.toPx())
    val contactCenter = center + Offset(1.10.dp.toPx(), 1.55.dp.toPx())

    drawCircle(
        brush =
            Brush.radialGradient(
                colorStops =
                    arrayOf(
                        0f to Color.Transparent,
                        innerStop to Color.Transparent,
                        darkStop to Color.Black.copy(alpha = 0.30f * lift),
                        outerStop to Color.Black.copy(alpha = 0.12f * lift),
                        1.00f to Color.Transparent,
                    ),
                center = center,
                radius = edgeShadowRadius,
            ),
        radius = edgeShadowRadius,
        center = center,
    )

    drawOval(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color.Black.copy(alpha = 0.11f * lift),
                        Color.Black.copy(alpha = 0.05f * lift),
                        Color.Transparent,
                    ),
                center = ambientCenter,
                radius = plateRadius * 0.74f,
            ),
        topLeft = Offset(ambientCenter.x - plateRadius * 0.70f, ambientCenter.y - plateRadius * 0.26f),
        size =
            Size(
                width = plateRadius * 1.40f,
                height = plateRadius * 0.52f,
            ),
    )
    drawOval(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color.Black.copy(alpha = 0.15f * lift),
                        Color.Black.copy(alpha = 0.06f * lift),
                        Color.Transparent,
                    ),
                center = contactCenter,
                radius = plateRadius * 0.52f,
            ),
        topLeft = Offset(contactCenter.x - plateRadius * 0.50f, contactCenter.y - plateRadius * 0.18f),
        size =
            Size(
                width = plateRadius,
                height = plateRadius * 0.36f,
            ),
    )
}

@Composable
private fun BehaviorMapAppIcon(
    pkg: String,
    size: Dp,
    iconPadding: Dp,
    showBorder: Boolean,
    colorFilter: ColorFilter?,
) {
    val context = LocalContext.current
    val icon = remember(pkg) {
        AppVisualCache.getIcon(context, pkg)
    }
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = if (showBorder) BorderStroke(1.dp, Color.White.copy(alpha = 0.95f)) else null,
    ) {
        if (icon != null) {
            val bitmap = remember(icon) {
                icon
                    .toBitmap(width = 96, height = 96, config = Bitmap.Config.ARGB_8888)
                    .copy(Bitmap.Config.ARGB_8888, false)
                    .asImageBitmap()
            }
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = colorFilter,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(iconPadding)
                        .clip(CircleShape)
                        .graphicsLayer {
                            scaleX = 1.08f
                            scaleY = 1.08f
                        },
            )
        }
    }
}

@Composable
private fun BehaviorMapLegend(
    activeRoles: Set<BehaviorMapRole>,
    onToggleRole: (BehaviorMapRole) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BehaviorMapLegendButton(
            label = AppText.t("stats_behavior_map_role_control"),
            color = behaviorMapRoleColor(BehaviorMapRole.CONTROL),
            active = BehaviorMapRole.CONTROL in activeRoles,
            modifier = Modifier.weight(1f),
            onClick = { onToggleRole(BehaviorMapRole.CONTROL) },
        )
        BehaviorMapLegendButton(
            label = AppText.t("stats_behavior_map_role_encourage"),
            color = behaviorMapRoleColor(BehaviorMapRole.ENCOURAGE),
            active = BehaviorMapRole.ENCOURAGE in activeRoles,
            modifier = Modifier.weight(1f),
            onClick = { onToggleRole(BehaviorMapRole.ENCOURAGE) },
        )
        BehaviorMapLegendButton(
            label = AppText.t("stats_behavior_map_role_ungrouped"),
            color = behaviorMapRoleColor(BehaviorMapRole.UNGROUPED),
            active = BehaviorMapRole.UNGROUPED in activeRoles,
            modifier = Modifier.weight(1f),
            onClick = { onToggleRole(BehaviorMapRole.UNGROUPED) },
        )
    }
}

@Composable
private fun BehaviorMapLegendButton(
    label: String,
    color: Color,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f)
    val contentColor = if (active) MaterialTheme.colorScheme.onSurfaceVariant else inactiveColor
    val indicatorColor = if (active) color else inactiveColor
    Surface(
        modifier =
            modifier
                .height(34.dp)
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color =
            if (active) {
                color.copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.34f)
            },
        border = null,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(12.dp)
                        .border(1.4.dp, indicatorColor, CircleShape),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                textDecoration = if (active) TextDecoration.None else TextDecoration.LineThrough,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun behaviorMapDisabledColorFilter(): ColorFilter {
    val matrix = ColorMatrix()
    matrix.setToSaturation(0f)
    return ColorFilter.colorMatrix(matrix)
}

@Composable
private fun BehaviorMapSelectedPointCard(point: BehaviorMapPoint) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = behaviorMapRoleColor(point.role).copy(alpha = 0.10f),
        border = BorderStroke(1.dp, behaviorMapRoleColor(point.role).copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconCircle(pkg = point.packageName, size = 38.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = point.quadrantLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = formatDuration(point.usageMillis), style = MaterialTheme.typography.labelLarge)
                Text(
                    text = AppText.t("stats_value_times_4", point.openCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private enum class BehaviorMapMatrixColumn {
    HIGH_FREQ_LOW_TIME,
    LOW_FREQ_HIGH_TIME,
    HIGH_FREQ_HIGH_TIME,
}

@Composable
private fun BehaviorMapMatrix(data: BehaviorMapSectionData) {
    val roles = listOf(BehaviorMapRole.ENCOURAGE, BehaviorMapRole.UNGROUPED, BehaviorMapRole.CONTROL)
    val columns =
        listOf(
            BehaviorMapMatrixColumn.HIGH_FREQ_LOW_TIME,
            BehaviorMapMatrixColumn.LOW_FREQ_HIGH_TIME,
            BehaviorMapMatrixColumn.HIGH_FREQ_HIGH_TIME,
        )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        roles.forEach { role ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                columns.forEach { column ->
                    BehaviorMapMatrixCard(
                        role = role,
                        column = column,
                        point = data.matrixPoint(role, column),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun BehaviorMapSectionData.matrixPoint(
    role: BehaviorMapRole,
    column: BehaviorMapMatrixColumn,
): BehaviorMapPoint? =
    points
        .filter { it.role == role }
        .filter { point ->
            val highDuration = point.usageMillis >= durationThresholdMillis
            val highOpens = point.openCount >= openThreshold
            when (column) {
                BehaviorMapMatrixColumn.HIGH_FREQ_LOW_TIME -> highOpens && !highDuration
                BehaviorMapMatrixColumn.LOW_FREQ_HIGH_TIME -> !highOpens && highDuration
                BehaviorMapMatrixColumn.HIGH_FREQ_HIGH_TIME -> highOpens && highDuration
            }
        }
        .maxWithOrNull(
            compareBy<BehaviorMapPoint> { it.attentionWeight }
                .thenBy { it.usageMillis }
                .thenBy { it.openCount },
        )

@Composable
private fun BehaviorMapMatrixCard(
    role: BehaviorMapRole,
    column: BehaviorMapMatrixColumn,
    point: BehaviorMapPoint?,
    modifier: Modifier = Modifier,
) {
    val roleColor = behaviorMapRoleColor(role)
    val containerColor = behaviorMapMatrixContainerColor(role)
    val emptyColor = MaterialTheme.colorScheme.outlineVariant
    val cardColor = if (point == null) emptyColor.copy(alpha = 0.16f) else containerColor.copy(alpha = 0.28f)
    val borderColor = if (point == null) emptyColor.copy(alpha = 0.40f) else roleColor
    val borderWidth = if (point == null) 1.dp else 2.dp
    Surface(
        modifier = modifier.aspectRatio(0.86f),
        shape = RoundedCornerShape(12.dp),
        color = cardColor,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 7.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = behaviorMapMatrixTitle(role, column),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (point == null) {
                Box(
                    modifier =
                        Modifier
                            .size(34.dp)
                            .border(1.4.dp, emptyColor.copy(alpha = 0.48f), CircleShape),
                )
            } else {
                AppIconCircle(
                    pkg = point.packageName,
                    size = 34.dp,
                    iconPadding = 0.dp,
                    showBorder = false,
                )
            }
            Text(
                text =
                    AppText.t(
                        "stats_behavior_map_matrix_meta",
                        behaviorMapRoleLabel(role),
                        behaviorMapMatrixColumnLabel(column),
                    ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text =
                    if (point == null) {
                        AppText.t("stats_behavior_map_matrix_empty")
                    } else {
                        AppText.t("stats_behavior_map_matrix_value", point.openCount, formatDuration(point.usageMillis))
                    },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun behaviorMapRoleLabel(role: BehaviorMapRole): String =
    when (role) {
        BehaviorMapRole.CONTROL -> AppText.t("stats_behavior_map_role_control")
        BehaviorMapRole.ENCOURAGE -> AppText.t("stats_behavior_map_role_encourage")
        BehaviorMapRole.UNGROUPED -> AppText.t("stats_behavior_map_role_ungrouped")
    }

@Composable
private fun behaviorMapMatrixColumnLabel(column: BehaviorMapMatrixColumn): String =
    when (column) {
        BehaviorMapMatrixColumn.HIGH_FREQ_LOW_TIME -> AppText.t("stats_behavior_map_matrix_high_freq_low_time")
        BehaviorMapMatrixColumn.LOW_FREQ_HIGH_TIME -> AppText.t("stats_behavior_map_matrix_low_freq_high_time")
        BehaviorMapMatrixColumn.HIGH_FREQ_HIGH_TIME -> AppText.t("stats_behavior_map_matrix_high_freq_high_time")
    }

@Composable
private fun behaviorMapMatrixTitle(
    role: BehaviorMapRole,
    column: BehaviorMapMatrixColumn,
): String =
    when (role) {
        BehaviorMapRole.ENCOURAGE ->
            when (column) {
                BehaviorMapMatrixColumn.HIGH_FREQ_LOW_TIME -> AppText.t("stats_behavior_map_quadrant_encourage_light_checkin")
                BehaviorMapMatrixColumn.LOW_FREQ_HIGH_TIME -> AppText.t("stats_behavior_map_quadrant_encourage_deep_investment")
                BehaviorMapMatrixColumn.HIGH_FREQ_HIGH_TIME -> AppText.t("stats_behavior_map_quadrant_encourage_steady_investment")
            }
        BehaviorMapRole.UNGROUPED ->
            when (column) {
                BehaviorMapMatrixColumn.HIGH_FREQ_LOW_TIME -> AppText.t("stats_behavior_map_quadrant_ungrouped_fragment")
                BehaviorMapMatrixColumn.LOW_FREQ_HIGH_TIME -> AppText.t("stats_behavior_map_quadrant_ungrouped_long_use")
                BehaviorMapMatrixColumn.HIGH_FREQ_HIGH_TIME -> AppText.t("stats_behavior_map_quadrant_ungrouped_high_use")
            }
        BehaviorMapRole.CONTROL ->
            when (column) {
                BehaviorMapMatrixColumn.HIGH_FREQ_LOW_TIME -> AppText.t("stats_behavior_map_quadrant_control_frequent_interrupt")
                BehaviorMapMatrixColumn.LOW_FREQ_HIGH_TIME -> AppText.t("stats_behavior_map_quadrant_control_long_slip")
                BehaviorMapMatrixColumn.HIGH_FREQ_HIGH_TIME -> AppText.t("stats_behavior_map_quadrant_control_repeated_trap")
            }
    }

@Composable
private fun BehaviorMapHighlightCard(
    highlight: BehaviorMapHighlight,
    modifier: Modifier = Modifier,
) {
    val color = behaviorMapInsightColor(highlight.tone)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconCircle(pkg = highlight.point.packageName, size = 34.dp)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = highlight.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = highlight.point.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = AppText.t("stats_behavior_map_point_value", formatDuration(highlight.point.usageMillis), highlight.point.openCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun behaviorMapRoleColor(role: BehaviorMapRole): Color =
    when (role) {
        BehaviorMapRole.CONTROL -> Color(0xFFE95A3B)
        BehaviorMapRole.ENCOURAGE -> Color(0xFF2FBF71)
        BehaviorMapRole.UNGROUPED -> LocalThemeColors.current.base
    }

@Composable
private fun behaviorMapMatrixContainerColor(role: BehaviorMapRole): Color {
    val themeColors = LocalThemeColors.current
    return when (role) {
        BehaviorMapRole.CONTROL -> themeColors.controlContainer
        BehaviorMapRole.ENCOURAGE -> themeColors.encourageContainer
        BehaviorMapRole.UNGROUPED -> themeColors.baseContainer
    }
}

@Composable
private fun behaviorMapInsightColor(tone: BehaviorMapInsightTone): Color =
    when (tone) {
        BehaviorMapInsightTone.WARNING -> Color(0xFFE95A3B)
        BehaviorMapInsightTone.POSITIVE -> Color(0xFF2FBF71)
        BehaviorMapInsightTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
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

internal fun formatBehaviorMetricMinutes(durationMillis: Long): String {
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
