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
import androidx.compose.ui.graphics.toArgb
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
) {
    when (val periodState = state.periodReportState) {
        SectionState.Loading -> {
            PeriodReportSkeleton(selectedTab = state.selectedTab)
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
            PeriodHeroCard(
                hero = data.hero,
                animateValues = animateValues,
            )
            PeriodFocusCard(
                data = data.windowFocus,
                animateValues = animateValues,
            )
            OfflineFocusDailyCard(state = SectionState.Ready(data.offlineFocus))
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
