package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.PointLedgerDailyStats
import com.rrrrz.tinyvow.data.db.PointLedgerSpendRecord
import com.rrrrz.tinyvow.data.db.StepDayEntity
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowCardContent
import com.rrrrz.tinyvow.ui.theme.TinyVowDetailScaffold
import com.rrrrz.tinyvow.ui.theme.TinyVowEmptyState
import com.rrrrz.tinyvow.ui.theme.TinyVowMetricTile
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSection
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

private enum class MeProgressRangePreset {
    MONTH,
    QUARTER,
    YEAR,
    CUSTOM,
}

private enum class MeProgressDateTarget {
    START,
    END,
}

enum class MeProgressMetric {
    SAVED_MINUTES,
    EARNED_POINTS,
    STEPS,
}

private data class MeProgressRange(
    val start: LocalDate,
    val end: LocalDate,
)

private data class MeProgressDay(
    val date: LocalDate,
    val savedMinutes: Long,
    val earnedPoints: Double,
    val spentPoints: Double,
    val netPoints: Double,
    val currentPoints: Double,
    val cumulativeSavedMinutes: Long,
    val cumulativeEarnedPoints: Double,
    val cumulativeSpentPoints: Double,
)

private data class StepProgressDay(
    val date: LocalDate,
    val steps: Int,
    val progress: Float,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeProgressStatsScreen(
    metric: MeProgressMetric,
    archives: List<DailyArchiveEntity>,
    pointDailyStats: List<PointLedgerDailyStats>,
    pointSpendRecords: List<PointLedgerSpendRecord>,
    currentPointsBalance: Double,
    stepDays: List<StepDayEntity> = emptyList(),
    stepTarget: Int = 0,
    today: LocalDate,
    onBack: () -> Unit,
) {
    var selectedPreset by rememberSaveable { mutableStateOf(MeProgressRangePreset.MONTH) }
    var customStartText by rememberSaveable { mutableStateOf(today.minusDays(89).toString()) }
    var customEndText by rememberSaveable { mutableStateOf(today.toString()) }
    var pickingTarget by remember { mutableStateOf<MeProgressDateTarget?>(null) }

    val customStart = remember(customStartText, today) { parseDateOrToday(customStartText, today).coerceAtMost(today) }
    val customEnd = remember(customEndText, today) { parseDateOrToday(customEndText, today).coerceAtMost(today) }
    val range =
        remember(selectedPreset, customStart, customEnd, today) {
            val rawRange =
                when (selectedPreset) {
                    MeProgressRangePreset.MONTH -> {
                        val month = YearMonth.from(today)
                        MeProgressRange(month.atDay(1), today)
                    }
                    MeProgressRangePreset.QUARTER -> {
                        val quarterStartMonth = ((today.monthValue - 1) / 3) * 3 + 1
                        MeProgressRange(LocalDate.of(today.year, quarterStartMonth, 1), today)
                    }
                    MeProgressRangePreset.YEAR -> MeProgressRange(LocalDate.of(today.year, 1, 1), today)
                    MeProgressRangePreset.CUSTOM -> MeProgressRange(customStart, customEnd)
                }
            if (rawRange.start <= rawRange.end) rawRange else MeProgressRange(rawRange.end, rawRange.start)
        }
    val days =
        remember(archives, pointDailyStats, currentPointsBalance, range) {
            buildMeProgressDays(
                archives = archives,
                pointDailyStats = pointDailyStats,
                currentPointsBalance = currentPointsBalance,
                range = range,
            )
    }
    val stepProgressDays =
        remember(stepDays, range, stepTarget) {
            buildStepProgressDays(
                stepDays = stepDays,
                range = range,
                stepTarget = stepTarget,
            )
        }
    val totalSavedMinutes = days.sumOf { it.savedMinutes }
    val totalEarnedPoints = days.sumOf { it.earnedPoints }
    val totalSteps = stepProgressDays.sumOf { it.steps }
    val totalSpentPoints = days.sumOf { it.spentPoints }
    val netPoints = days.sumOf { it.netPoints }
    val latestCurrentPoints = days.lastOrNull()?.currentPoints ?: 0.0
    val spendRecordsInRange =
        remember(pointSpendRecords, range) {
            pointSpendRecords.filter { record ->
                runCatching { LocalDate.parse(record.ledgerDate) }
                    .getOrNull()
                    ?.let { it in range.start..range.end }
                    ?: false
            }
        }
    val activeDays =
        when (metric) {
            MeProgressMetric.SAVED_MINUTES -> days.count { it.savedMinutes > 0L }
            MeProgressMetric.EARNED_POINTS -> days.count { it.earnedPoints > 0.0 || it.spentPoints > 0.0 }
            MeProgressMetric.STEPS -> stepProgressDays.count { it.steps > 0 }
        }
    val rangeLabel = remember(range) { formatMeProgressRange(range) }

    TinyVowDetailScaffold(
        title =
            when (metric) {
                MeProgressMetric.SAVED_MINUTES -> AppText.t("me_progress_saved_stats_title")
                MeProgressMetric.EARNED_POINTS -> AppText.t("me_progress_points_stats_title")
                MeProgressMetric.STEPS -> AppText.t("me_progress_steps_stats_title")
            },
        onBack = onBack,
    ) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = TinyVowSpacing.PageHorizontal, vertical = TinyVowSpacing.PageTop),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
        ) {
            MeProgressRangeSelector(
                selectedPreset = selectedPreset,
                rangeLabel = rangeLabel,
                customStart = customStart,
                customEnd = customEnd,
                onSelectPreset = { selectedPreset = it },
                onPickStart = {
                    selectedPreset = MeProgressRangePreset.CUSTOM
                    pickingTarget = MeProgressDateTarget.START
                },
                onPickEnd = {
                    selectedPreset = MeProgressRangePreset.CUSTOM
                    pickingTarget = MeProgressDateTarget.END
                },
            )
            val metricColor =
                when (metric) {
                    MeProgressMetric.SAVED_MINUTES -> LocalThemeColors.current.control
                    MeProgressMetric.EARNED_POINTS -> LocalThemeColors.current.encourage
                    MeProgressMetric.STEPS -> LocalThemeColors.current.encourage
                }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TinyVowMetricTile(
                    label =
                        when (metric) {
                            MeProgressMetric.SAVED_MINUTES -> AppText.t("me_progress_total_saved")
                            MeProgressMetric.EARNED_POINTS -> AppText.t("me_progress_total_points")
                            MeProgressMetric.STEPS -> AppText.t("me_progress_total_steps")
                        },
                    value =
                        when (metric) {
                            MeProgressMetric.SAVED_MINUTES -> AppText.t("me_progress_minutes_value", formatInteger(totalSavedMinutes))
                            MeProgressMetric.EARNED_POINTS -> formatPoints(totalEarnedPoints)
                            MeProgressMetric.STEPS -> AppText.t("me_progress_steps_value", formatInteger(totalSteps.toLong()))
                        },
                    color = metricColor,
                    modifier = Modifier.weight(1f),
                )
                TinyVowMetricTile(
                    label = AppText.t("me_progress_active_days"),
                    value = AppText.t("me_progress_days_value", activeDays),
                    color = metricColor,
                    modifier = Modifier.weight(1f),
                )
            }

            val hasData =
                when (metric) {
                    MeProgressMetric.SAVED_MINUTES -> days.any { it.savedMinutes > 0L }
                    MeProgressMetric.EARNED_POINTS -> days.any { it.earnedPoints > 0.0 || it.spentPoints > 0.0 }
                    MeProgressMetric.STEPS -> stepProgressDays.any { it.steps > 0 }
                }
            if (!hasData) {
                TinyVowEmptyState(
                    icon =
                        when (metric) {
                            MeProgressMetric.SAVED_MINUTES -> Icons.Default.Timeline
                            MeProgressMetric.EARNED_POINTS -> Icons.Default.EmojiEvents
                            MeProgressMetric.STEPS -> Icons.AutoMirrored.Filled.DirectionsWalk
                        },
                    title =
                        when (metric) {
                            MeProgressMetric.SAVED_MINUTES -> AppText.t("me_progress_saved_empty_title")
                            MeProgressMetric.EARNED_POINTS -> AppText.t("me_progress_points_empty_title")
                            MeProgressMetric.STEPS -> AppText.t("me_progress_steps_empty_title")
                        },
                    body =
                        when (metric) {
                            MeProgressMetric.SAVED_MINUTES -> AppText.t("me_progress_saved_empty_body")
                            MeProgressMetric.EARNED_POINTS -> AppText.t("me_progress_points_empty_body")
                            MeProgressMetric.STEPS -> AppText.t("me_progress_steps_empty_body")
                        },
                )
            } else {
                when (metric) {
                    MeProgressMetric.SAVED_MINUTES ->
                        TinyVowSection(
                            title = AppText.t("me_progress_saved_section"),
                            subtitle = AppText.t("me_progress_saved_section_desc"),
                            icon = Icons.Default.Timeline,
                        ) {
                            MeProgressLineChartCard(
                                title = AppText.t("me_progress_saved_line"),
                                color = LocalThemeColors.current.control,
                                points = days.map { it.date to it.cumulativeSavedMinutes.toDouble() },
                                valueFormatter = { AppText.t("me_progress_minutes_value", formatInteger(it.roundToLong())) },
                            )
                            MeProgressHeatmapCard(
                                title = AppText.t("me_progress_saved_heatmap"),
                                color = LocalThemeColors.current.control,
                                days = days.map { MeProgressHeatmapDay(it.date, it.savedMinutes.toDouble()) },
                                valueFormatter = { AppText.t("me_progress_minutes_value", formatInteger(it.roundToLong())) },
                            )
                        }
                    MeProgressMetric.EARNED_POINTS -> {
                        TinyVowSection(
                            title = AppText.t("me_progress_points_section"),
                            subtitle = AppText.t("me_progress_points_section_desc"),
                            icon = Icons.Default.EmojiEvents,
                        ) {
                            MeProgressLineChartCard(
                                title = AppText.t("me_progress_points_line"),
                                color = LocalThemeColors.current.encourage,
                                points = days.map { it.date to it.cumulativeEarnedPoints },
                                valueFormatter = { formatPoints(it) },
                            )
                            MeProgressHeatmapCard(
                                title = AppText.t("me_progress_points_heatmap"),
                                color = LocalThemeColors.current.encourage,
                                days = days.map { MeProgressHeatmapDay(it.date, it.earnedPoints) },
                                valueFormatter = { formatPoints(it) },
                            )
                        }
                        MeProgressCurrentPointsSection(
                            days = days,
                            latestCurrentPoints = latestCurrentPoints,
                            netPoints = netPoints,
                        )
                        MeProgressSpentPointsSection(
                            days = days,
                            totalSpentPoints = totalSpentPoints,
                            spendRecords = spendRecordsInRange,
                        )
                    }
                    MeProgressMetric.STEPS ->
                        TinyVowSection(
                            title = AppText.t("me_progress_steps_section"),
                            subtitle = AppText.t("me_progress_steps_section_desc"),
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        ) {
                            StepProgressCalendarCard(
                                title = AppText.t("me_progress_steps_calendar"),
                                color = LocalThemeColors.current.encourage,
                                days = stepProgressDays,
                            )
                        }
                }
            }
        }
    }

    val target = pickingTarget
    if (target != null) {
        val initialDate = if (target == MeProgressDateTarget.START) customStart else customEnd
        key(target, initialDate) {
            val pickerState =
                rememberDatePickerState(
                    initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
                )
            DatePickerDialog(
                onDismissRequest = { pickingTarget = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedDate =
                                pickerState.selectedDateMillis
                                    ?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                                    ?.coerceAtMost(today)
                            if (selectedDate != null) {
                                if (target == MeProgressDateTarget.START) {
                                    customStartText = selectedDate.toString()
                                } else {
                                    customEndText = selectedDate.toString()
                                }
                            }
                            pickingTarget = null
                        },
                    ) {
                        Text(AppText.t("me_progress_date_picker_confirm"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pickingTarget = null }) {
                        Text(AppText.t("me_progress_date_picker_cancel"))
                    }
                },
            ) {
                DatePicker(state = pickerState)
            }
        }
    }
}

@Composable
private fun MeProgressRangeSelector(
    selectedPreset: MeProgressRangePreset,
    rangeLabel: String,
    customStart: LocalDate,
    customEnd: LocalDate,
    onSelectPreset: (MeProgressRangePreset) -> Unit,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
) {
    TinyVowCard {
        TinyVowCardContent {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = LocalThemeColors.current.base,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = AppText.t("me_progress_range_title"),
                        style = MaterialTheme.typography.titleMedium,
                        color = LocalThemeColors.current.inkStrong,
                    )
                    Text(
                        text = rangeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalThemeColors.current.inkMuted,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MeProgressRangePreset.entries.forEach { preset ->
                    TinyVowButton(
                        text = preset.label(),
                        onClick = { onSelectPreset(preset) },
                        selected = selectedPreset == preset,
                        tone = if (selectedPreset == preset) TinyVowButtonTone.Primary else TinyVowButtonTone.Neutral,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (selectedPreset == MeProgressRangePreset.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MeProgressDateChip(
                        label = AppText.t("me_progress_range_start"),
                        date = customStart,
                        onClick = onPickStart,
                        modifier = Modifier.weight(1f),
                    )
                    MeProgressDateChip(
                        label = AppText.t("me_progress_range_end"),
                        date = customEnd,
                        onClick = onPickEnd,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MeProgressDateChip(
    label: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(TinyVowRadius.Control))
                .clickable(onClick = onClick)
                .background(themeColors.surfaceSoft)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = themeColors.inkMuted,
        )
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.titleSmall,
            color = themeColors.inkStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MeProgressLineChartCard(
    title: String,
    color: Color,
    points: List<Pair<LocalDate, Double>>,
    valueFormatter: (Double) -> String,
    positiveColor: Color? = null,
    negativeColor: Color? = null,
) {
    val themeColors = LocalThemeColors.current
    val visiblePoints = points.filter { it.second != 0.0 }
    val latestValue = points.lastOrNull()?.second ?: 0.0
    TinyVowCard {
        TinyVowCardContent {
            ChartHeader(
                title = title,
                value = valueFormatter(latestValue),
                color = color,
            )
            if (visiblePoints.isEmpty()) {
                Text(
                    text = AppText.t("me_progress_chart_empty"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.inkMuted,
                )
            } else {
                Canvas(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(176.dp),
                ) {
                    val horizontalPadding = 10.dp.toPx()
                    val verticalPadding = 16.dp.toPx()
                    val chartWidth = size.width - horizontalPadding * 2f
                    val chartHeight = size.height - verticalPadding * 2f
                    val minValue = points.minOfOrNull { it.second }?.coerceAtMost(0.0) ?: 0.0
                    val maxValue = points.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0
                    val valueRange = (maxValue - minValue).coerceAtLeast(1.0)
                    val denominator = (points.size - 1).coerceAtLeast(1).toFloat()
                    fun yFor(value: Double): Float =
                        verticalPadding + chartHeight * (1f - ((value - minValue) / valueRange).toFloat().coerceIn(0f, 1f))
                    repeat(4) { gridIndex ->
                        val y = verticalPadding + chartHeight * (gridIndex / 3f)
                        drawLine(
                            color = themeColors.dividerSoft.copy(alpha = 0.72f),
                            start = Offset(horizontalPadding, y),
                            end = Offset(size.width - horizontalPadding, y),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    if (positiveColor != null && negativeColor != null) {
                        points.zipWithNext().forEachIndexed { index, (from, to) ->
                            val startX = horizontalPadding + chartWidth * (index / denominator)
                            val startY = yFor(from.second)
                            val endX = horizontalPadding + chartWidth * ((index + 1) / denominator)
                            val endY = yFor(to.second)
                            drawLine(
                                color = if (to.second >= from.second) positiveColor else negativeColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }
                    } else {
                        val path = Path()
                        points.forEachIndexed { index, point ->
                            val x = horizontalPadding + chartWidth * (index / denominator)
                            val y = yFor(point.second)
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                    val lastIndex = points.lastIndex.coerceAtLeast(0)
                    val last = points.getOrNull(lastIndex)
                    if (last != null) {
                        val x = horizontalPadding + chartWidth * (lastIndex / denominator)
                        val y = yFor(last.second)
                        drawCircle(color = color.copy(alpha = 0.18f), radius = 8.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
                    }
                }
                ChartLabels(points)
            }
        }
    }
}

private data class MeProgressHeatmapDay(
    val date: LocalDate,
    val value: Double,
)

@Composable
private fun MeProgressHeatmapCard(
    title: String,
    color: Color,
    days: List<MeProgressHeatmapDay>,
    valueFormatter: (Double) -> String,
    signedColors: Pair<Color, Color>? = null,
) {
    val maxValue =
        if (signedColors != null) {
            days.maxOfOrNull { it.value.absoluteValue }?.coerceAtLeast(1.0) ?: 1.0
        } else {
            days.maxOfOrNull { it.value }?.coerceAtLeast(1.0) ?: 1.0
        }
    val weeks = remember(days) { buildContributionWeeks(days) }
    val cellSize =
        when {
            weeks.size <= 6 -> 20.dp
            weeks.size <= 16 -> 16.dp
            else -> 13.dp
        }
    val cellGap =
        when {
            weeks.size <= 16 -> 4.dp
            else -> 3.dp
        }
    TinyVowCard {
        TinyVowCardContent {
            ChartHeader(
                title = title,
                value = valueFormatter(days.sumOf { it.value }),
                color = color,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(cellGap),
                ) {
                    Column(
                        modifier = Modifier.width(24.dp),
                        verticalArrangement = Arrangement.spacedBy(cellGap),
                    ) {
                        Spacer(Modifier.height(cellSize))
                        contributionWeekdayLabels().forEach { label ->
                            Box(
                                modifier = Modifier.height(cellSize),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LocalThemeColors.current.inkMuted,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    weeks.forEachIndexed { weekIndex, week ->
                        Column(verticalArrangement = Arrangement.spacedBy(cellGap)) {
                            Box(
                                modifier = Modifier.height(cellSize),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                val label = contributionMonthLabel(weekIndex, week)
                                if (label.isNotBlank()) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LocalThemeColors.current.inkMuted,
                                        maxLines = 1,
                                    )
                                }
                            }
                            week.forEach { day ->
                                val rawValue = day?.value ?: 0.0
                                val intensity =
                                    if (signedColors != null) {
                                        (rawValue.absoluteValue / maxValue).toFloat().coerceIn(0f, 1f)
                                    } else {
                                        (rawValue / maxValue).toFloat().coerceIn(0f, 1f)
                                    }
                                Box(
                                    modifier =
                                        Modifier
                                            .size(cellSize)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(contributionCellColor(color, intensity, rawValue, signedColors)),
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (signedColors != null) {
                        Text(
                            text = AppText.t("me_progress_heatmap_decrease"),
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalThemeColors.current.inkMuted,
                        )
                        Spacer(Modifier.width(3.dp))
                        listOf(0.65f, 0.35f).forEach { intensity ->
                            Box(
                                modifier =
                                    Modifier
                                        .size(11.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(contributionCellColor(color, intensity, -1.0, signedColors)),
                            )
                            Spacer(Modifier.width(3.dp))
                        }
                        Box(
                            modifier =
                                Modifier
                                    .size(11.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(contributionCellColor(color, 0.0f, 0.0, signedColors)),
                        )
                        Spacer(Modifier.width(3.dp))
                        listOf(0.35f, 0.65f).forEach { intensity ->
                            Box(
                                modifier =
                                    Modifier
                                        .size(11.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(contributionCellColor(color, intensity, 1.0, signedColors)),
                            )
                            Spacer(Modifier.width(3.dp))
                        }
                        Text(
                            text = AppText.t("me_progress_heatmap_increase"),
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalThemeColors.current.inkMuted,
                        )
                    } else {
                        Text(
                            text = AppText.t("me_progress_heatmap_less"),
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalThemeColors.current.inkMuted,
                        )
                        Spacer(Modifier.width(6.dp))
                        listOf(0.0f, 0.25f, 0.50f, 0.75f, 1.0f).forEach { intensity ->
                            Box(
                                modifier =
                                    Modifier
                                        .size(11.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(contributionCellColor(color, intensity, if (intensity == 0f) 0.0 else 1.0, signedColors)),
                            )
                            Spacer(Modifier.width(3.dp))
                        }
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = AppText.t("me_progress_heatmap_more"),
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalThemeColors.current.inkMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepProgressCalendarCard(
    title: String,
    color: Color,
    days: List<StepProgressDay>,
) {
    val weeks = remember(days) { buildStepCalendarWeeks(days) }
    val cellSize =
        when {
            weeks.size <= 6 -> 54.dp
            weeks.size <= 16 -> 48.dp
            else -> 42.dp
        }
    val cellGap =
        when {
            weeks.size <= 16 -> 6.dp
            else -> 4.dp
        }
    TinyVowCard {
        TinyVowCardContent {
            ChartHeader(
                title = title,
                value = AppText.t("me_progress_steps_value", formatInteger(days.sumOf { it.steps }.toLong())),
                color = color,
            )
            if (days.isEmpty()) {
                Text(
                    text = AppText.t("me_progress_chart_empty"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalThemeColors.current.inkMuted,
                )
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(cellGap),
                ) {
                    Column(
                        modifier = Modifier.width(24.dp),
                        verticalArrangement = Arrangement.spacedBy(cellGap),
                    ) {
                        Spacer(Modifier.height(18.dp))
                        contributionWeekdayLabels().forEach { label ->
                            Box(
                                modifier = Modifier.height(cellSize),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LocalThemeColors.current.inkMuted,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    weeks.forEachIndexed { weekIndex, week ->
                        Column(verticalArrangement = Arrangement.spacedBy(cellGap)) {
                            Box(
                                modifier = Modifier.height(18.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                val label = stepCalendarMonthLabel(weekIndex, week)
                                if (label.isNotBlank()) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LocalThemeColors.current.inkMuted,
                                        maxLines = 1,
                                    )
                                }
                            }
                            week.forEach { day ->
                                StepCalendarCell(
                                    day = day,
                                    color = color,
                                    cellSize = cellSize,
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
private fun StepCalendarCell(
    day: StepProgressDay?,
    color: Color,
    cellSize: androidx.compose.ui.unit.Dp,
) {
    val themeColors = LocalThemeColors.current
    Box(
        modifier =
            Modifier
                .size(cellSize)
                .clip(RoundedCornerShape(14.dp))
                .background(themeColors.surfaceSoft.copy(alpha = if (day == null) 0.34f else 0.72f))
                .padding(5.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (day != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                val inset = strokeWidth / 2f
                val arcSize = size.minDimension - strokeWidth
                drawArc(
                    color = themeColors.dividerSoft.copy(alpha = 0.72f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * day.progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = formatStepCalendarValue(day.steps),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (day.steps > 0) color else themeColors.inkMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = themeColors.inkFaint,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun buildStepCalendarWeeks(days: List<StepProgressDay>): List<List<StepProgressDay?>> {
    if (days.isEmpty()) return emptyList()
    val byDate = days.associateBy { it.date }
    val firstDate = days.first().date
    val lastDate = days.last().date
    val gridStart = firstDate.minusDays((firstDate.dayOfWeek.value - 1).toLong())
    val gridEnd = lastDate.plusDays((7 - lastDate.dayOfWeek.value).toLong())
    val totalDays = ChronoUnit.DAYS.between(gridStart, gridEnd).toInt() + 1
    return List(totalDays) { index ->
        val date = gridStart.plusDays(index.toLong())
        byDate[date]
    }.chunked(7)
}

private fun stepCalendarMonthLabel(
    weekIndex: Int,
    week: List<StepProgressDay?>,
): String {
    val firstInRange = week.firstOrNull { it != null }?.date ?: return ""
    val monthStart = week.firstOrNull { it?.date?.dayOfMonth == 1 }?.date
    if (weekIndex != 0 && monthStart == null) return ""
    return (monthStart ?: firstInRange).month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
}

@Composable
private fun MeProgressCurrentPointsSection(
    days: List<MeProgressDay>,
    latestCurrentPoints: Double,
    netPoints: Double,
) {
    val themeColors = LocalThemeColors.current
    val positiveColor = themeColors.save
    val negativeColor = themeColors.restraint
    TinyVowSection(
        title = AppText.t("me_progress_current_points_section"),
        icon = Icons.Default.Timeline,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TinyVowMetricTile(
                label = AppText.t("me_progress_current_points_latest"),
                value = formatPoints(latestCurrentPoints),
                color = themeColors.base,
                modifier = Modifier.weight(1f),
            )
            TinyVowMetricTile(
                label = AppText.t("me_progress_current_points_net"),
                value = formatSignedPoints(netPoints),
                color = if (netPoints >= 0.0) positiveColor else negativeColor,
                modifier = Modifier.weight(1f),
            )
        }
        MeProgressLineChartCard(
            title = AppText.t("me_progress_current_points_line"),
            color = themeColors.base,
            points = days.map { it.date to it.currentPoints },
            valueFormatter = { formatPoints(it) },
            positiveColor = positiveColor,
            negativeColor = negativeColor,
        )
        MeProgressHeatmapCard(
            title = AppText.t("me_progress_current_points_heatmap"),
            color = positiveColor,
            days = days.map { MeProgressHeatmapDay(it.date, it.netPoints) },
            valueFormatter = { formatSignedPoints(it) },
            signedColors = positiveColor to negativeColor,
        )
    }
}

@Composable
private fun MeProgressSpentPointsSection(
    days: List<MeProgressDay>,
    totalSpentPoints: Double,
    spendRecords: List<PointLedgerSpendRecord>,
) {
    val themeColors = LocalThemeColors.current
    TinyVowSection(
        title = AppText.t("me_progress_spent_points_section"),
        icon = Icons.Default.EmojiEvents,
    ) {
        TinyVowMetricTile(
            label = AppText.t("me_progress_spent_points_total"),
            value = formatPoints(totalSpentPoints),
            color = themeColors.restraint,
            modifier = Modifier.fillMaxWidth(),
        )
        MeProgressLineChartCard(
            title = AppText.t("me_progress_spent_points_line"),
            color = themeColors.restraint,
            points = days.map { it.date to it.cumulativeSpentPoints },
            valueFormatter = { formatPoints(it) },
        )
        MeProgressHeatmapCard(
            title = AppText.t("me_progress_spent_points_heatmap"),
            color = themeColors.restraint,
            days = days.map { MeProgressHeatmapDay(it.date, it.spentPoints) },
            valueFormatter = { formatPoints(it) },
        )
        MeProgressSpendRecordsCard(records = spendRecords)
    }
}

@Composable
private fun MeProgressSpendRecordsCard(records: List<PointLedgerSpendRecord>) {
    val themeColors = LocalThemeColors.current
    TinyVowCard {
        TinyVowCardContent {
            ChartHeader(
                title = AppText.t("me_progress_spent_records_title"),
                value = formatInteger(records.size.toLong()),
                color = themeColors.restraint,
            )
            if (records.isEmpty()) {
                Text(
                    text = AppText.t("me_progress_spent_records_empty"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.inkMuted,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    records.forEach { record ->
                        MeProgressSpendRecordRow(record = record)
                    }
                }
            }
        }
    }
}

@Composable
private fun MeProgressSpendRecordRow(record: PointLedgerSpendRecord) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(themeColors.restraint),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = record.rewardTitleSnapshot?.takeIf { it.isNotBlank() }
                    ?: record.note.takeIf { it.isNotBlank() }
                    ?: AppText.t("me_progress_spent_record_default"),
                style = MaterialTheme.typography.titleSmall,
                color = themeColors.inkStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatSpendRecordTime(record.occurredAt, record.ledgerDate),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
                maxLines = 1,
            )
        }
        Text(
            text = AppText.t("me_progress_spent_record_points", formatPoints(record.points)),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.restraint,
            maxLines = 1,
        )
    }
}

private fun buildContributionWeeks(days: List<MeProgressHeatmapDay>): List<List<MeProgressHeatmapDay?>> {
    if (days.isEmpty()) return emptyList()
    val byDate = days.associateBy { it.date }
    val firstDate = days.first().date
    val lastDate = days.last().date
    val gridStart = firstDate.minusDays((firstDate.dayOfWeek.value - 1).toLong())
    val gridEnd = lastDate.plusDays((7 - lastDate.dayOfWeek.value).toLong())
    val totalDays = ChronoUnit.DAYS.between(gridStart, gridEnd).toInt() + 1
    return List(totalDays) { index ->
        val date = gridStart.plusDays(index.toLong())
        byDate[date]
    }.chunked(7)
}

private fun contributionWeekdayLabels(): List<String> =
    listOf(AppText.t("checkin_weekday_mon"), "", AppText.t("checkin_weekday_wed"), "", AppText.t("checkin_weekday_fri"), "", "")

private fun contributionMonthLabel(
    weekIndex: Int,
    week: List<MeProgressHeatmapDay?>,
): String {
    val firstInRange = week.firstOrNull { it != null }?.date ?: return ""
    val monthStart = week.firstOrNull { it?.date?.dayOfMonth == 1 }?.date
    if (weekIndex != 0 && monthStart == null) return ""
    return (monthStart ?: firstInRange).month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
}

@Composable
private fun contributionCellColor(
    color: Color,
    intensity: Float,
    value: Double,
    signedColors: Pair<Color, Color>? = null,
): Color =
    if (value <= 0.0) {
        if (signedColors != null && value < 0.0) {
            signedColors.second.copy(
                alpha =
                    when {
                        intensity < 0.25f -> 0.20f
                        intensity < 0.50f -> 0.34f
                        intensity < 0.75f -> 0.50f
                        intensity < 1.0f -> 0.66f
                        else -> 0.82f
                    },
            )
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
        }
    } else {
        (signedColors?.first ?: color).copy(
            alpha =
                when {
                    intensity < 0.25f -> 0.20f
                    intensity < 0.50f -> 0.34f
                    intensity < 0.75f -> 0.50f
                    intensity < 1.0f -> 0.66f
                    else -> 0.82f
                },
        )
    }

@Composable
private fun ChartHeader(
    title: String,
    value: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = LocalThemeColors.current.inkStrong,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ChartLabels(points: List<Pair<LocalDate, Double>>) {
    if (points.isEmpty()) return
    val labels =
        listOfNotNull(
            points.firstOrNull()?.first,
            points.getOrNull(points.size / 2)?.first,
            points.lastOrNull()?.first,
        ).distinct()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        labels.forEach { date ->
            Text(
                text = formatDateShort(date),
                style = MaterialTheme.typography.labelSmall,
                color = LocalThemeColors.current.inkMuted,
            )
        }
    }
}

private fun buildMeProgressDays(
    archives: List<DailyArchiveEntity>,
    pointDailyStats: List<PointLedgerDailyStats>,
    currentPointsBalance: Double,
    range: MeProgressRange,
): List<MeProgressDay> {
    val archiveByDate =
        archives.associateBy {
            runCatching { LocalDate.parse(it.archiveDate) }.getOrNull()
        }
    val pointStatsByDate =
        pointDailyStats.associate {
            LocalDate.parse(it.ledgerDate) to it
        }
    val ledgerTotal = pointStatsByDate.values.sumOf { it.netPoints }
    val balanceOffset = currentPointsBalance - ledgerTotal
    var currentPoints =
        balanceOffset +
        pointStatsByDate
            .filterKeys { it.isBefore(range.start) }
            .values
            .sumOf { it.netPoints }
    val dayCount = ChronoUnit.DAYS.between(range.start, range.end).toInt().coerceAtLeast(0) + 1
    var cumulativeSaved = 0L
    var cumulativePoints = 0.0
    var cumulativeSpentPoints = 0.0
    return List(dayCount) { index ->
        val date = range.start.plusDays(index.toLong())
        val pointStats = pointStatsByDate[date]
        val savedMinutes = (archiveByDate[date]?.savedMillis ?: 0L) / 60_000L
        val earnedPoints = pointStats?.earnedPoints ?: 0.0
        val spentPoints = pointStats?.spentPoints ?: 0.0
        val netPoints = pointStats?.netPoints ?: 0.0
        cumulativeSaved += savedMinutes
        cumulativePoints += earnedPoints
        cumulativeSpentPoints += spentPoints
        currentPoints += netPoints
        MeProgressDay(
            date = date,
            savedMinutes = savedMinutes,
            earnedPoints = earnedPoints,
            spentPoints = spentPoints,
            netPoints = netPoints,
            currentPoints = currentPoints,
            cumulativeSavedMinutes = cumulativeSaved,
            cumulativeEarnedPoints = cumulativePoints,
            cumulativeSpentPoints = cumulativeSpentPoints,
        )
    }
}

private fun buildStepProgressDays(
    stepDays: List<StepDayEntity>,
    range: MeProgressRange,
    stepTarget: Int,
): List<StepProgressDay> {
    val stepByDate =
        stepDays.associate {
            LocalDate.parse(it.stepDate) to it.steps
        }
    val dayCount = ChronoUnit.DAYS.between(range.start, range.end).toInt().coerceAtLeast(0) + 1
    val target = stepTarget.coerceAtLeast(1)
    return List(dayCount) { index ->
        val date = range.start.plusDays(index.toLong())
        val steps = stepByDate[date]?.coerceAtLeast(0) ?: 0
        StepProgressDay(
            date = date,
            steps = steps,
            progress = steps.toFloat() / target.toFloat(),
        )
    }
}

private fun MeProgressRangePreset.label(): String =
    when (this) {
        MeProgressRangePreset.MONTH -> AppText.t("me_progress_range_month")
        MeProgressRangePreset.QUARTER -> AppText.t("me_progress_range_quarter")
        MeProgressRangePreset.YEAR -> AppText.t("me_progress_range_year")
        MeProgressRangePreset.CUSTOM -> AppText.t("me_progress_range_custom")
    }

private fun formatMeProgressRange(range: MeProgressRange): String =
    AppText.t("me_progress_range_value", formatDate(range.start), formatDate(range.end))

private fun parseDateOrToday(value: String, today: LocalDate): LocalDate =
    runCatching { LocalDate.parse(value) }.getOrDefault(today)

private fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ISO_LOCAL_DATE)

private fun formatDateShort(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("M/d"))

private fun formatInteger(value: Long): String =
    NumberFormat.getIntegerInstance().format(value)

private fun formatStepCalendarValue(steps: Int): String =
    when {
        steps >= 10_000 -> "${(steps / 1000.0).roundToLong()}k"
        steps >= 1_000 -> "${steps / 1000}k"
        else -> steps.toString()
    }

private fun formatPoints(value: Double): String =
    NumberFormat.getNumberInstance()
        .apply { maximumFractionDigits = if (value < 10.0 && value % 1.0 != 0.0) 1 else 0 }
        .format(value)

private fun formatSignedPoints(value: Double): String {
    val formatted = formatPoints(value.absoluteValue)
    return if (value > 0.0) "+$formatted" else if (value < 0.0) "-$formatted" else formatted
}

private fun formatSpendRecordTime(
    occurredAt: Long,
    fallbackDate: String,
): String =
    runCatching {
        java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT)
            .format(Date(occurredAt))
    }.getOrDefault(fallbackDate)

private fun LocalDate.coerceAtMost(maximumValue: LocalDate): LocalDate =
    if (this > maximumValue) maximumValue else this
