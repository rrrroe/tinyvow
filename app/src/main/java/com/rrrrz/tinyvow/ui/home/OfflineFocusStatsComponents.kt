package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.i18n.AppText
import java.time.ZoneId
import kotlin.math.roundToInt

@Composable
internal fun OfflineFocusDailyCard(
    state: SectionState<OfflineFocusSectionData>,
) {
    when (state) {
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth(),
                    height = 120.dp,
                    shape = RoundedCornerShape(22.dp),
                )
            }
        }
        SectionState.Empty -> {
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.Timer,
                    title = AppText.t("offline_focus_daily_title"),
                    subtitle = AppText.t("offline_focus_daily_empty"),
                )
            }
        }
        is SectionState.Ready -> {
            val data = state.data
            if (data.completedCount == 0 && data.sessions.isEmpty()) {
                ReportCard {
                    SectionHeader(
                        icon = Icons.Default.Timer,
                        title = AppText.t("offline_focus_daily_title"),
                        subtitle = AppText.t("offline_focus_daily_empty"),
                    )
                }
                return
            }
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.Timer,
                    title = AppText.t("offline_focus_daily_title"),
                    subtitle = AppText.t("offline_focus_daily_quote"),
                    trailing = "+${data.pointsAwarded.roundToInt()}",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OfflineFocusStatTile(
                        label = AppText.t("offline_focus_today_minutes"),
                        value = formatDuration(data.totalMillis),
                        modifier = Modifier.weight(1f),
                    )
                    OfflineFocusStatTile(
                        label = AppText.t("offline_focus_today_sessions"),
                        value = data.completedCount.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
                OfflineFocusTimelineBar(data = data)
                if (data.sessions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        data.sessions.take(4).forEach { session ->
                            OfflineFocusSessionRow(session = session)
                        }
                    }
                }
                if (data.categories.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = AppText.t("offline_focus_category_breakdown"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        data.categories.forEach { category ->
                            OfflineFocusCategoryRow(category = category)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun OfflineFocusPomodoroRhythmCard(
    state: SectionState<OfflineFocusSectionData>,
) {
    when (state) {
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth(),
                    height = 138.dp,
                    shape = RoundedCornerShape(24.dp),
                )
            }
        }
        SectionState.Empty -> {
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.Timer,
                    title = AppText.t("offline_focus_rhythm_title"),
                    subtitle = AppText.t("offline_focus_daily_empty"),
                )
            }
        }
        is SectionState.Ready -> {
            val data = state.data
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.Timer,
                    title = AppText.t("offline_focus_rhythm_title"),
                    subtitle = AppText.t("offline_focus_rhythm_subtitle"),
                    trailing = formatDuration(data.totalMillis),
                )
                OfflineFocusPomodoroCanvas(data = data)
                if (data.sessions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        data.sessions.takeLast(5).forEach { session ->
                            OfflineFocusSessionRow(session = session)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun OfflineFocusMarksCard(
    state: SectionState<OfflineFocusSectionData>,
) {
    when (state) {
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth(),
                    height = 170.dp,
                    shape = RoundedCornerShape(24.dp),
                )
            }
        }
        SectionState.Empty -> {
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.Timeline,
                    title = AppText.t("offline_focus_marks_title"),
                    subtitle = AppText.t("offline_focus_daily_empty"),
                )
            }
        }
        is SectionState.Ready -> {
            val data = state.data
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.Timeline,
                    title = AppText.t("offline_focus_marks_title"),
                    subtitle = AppText.t("offline_focus_marks_subtitle"),
                    trailing = formatDuration(data.totalMillis),
                )
                OfflineFocusRhythmProfileStrip(data = data)
                OfflineFocusRhythmInsightStrip(data = data)
            }
        }
    }
}

@Composable
private fun OfflineFocusPomodoroCanvas(data: OfflineFocusSectionData) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val rail = MaterialTheme.colorScheme.surfaceContainerLow
    val outline = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(116.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(rail),
    ) {
        val horizontalPadding = 16.dp.toPx()
        val top = 20.dp.toPx()
        val trackHeight = 44.dp.toPx()
        val trackTop = top + 24.dp.toPx()
        val trackWidth = (size.width - horizontalPadding * 2f).coerceAtLeast(1f)
        val range = (data.dayEndMillis - data.dayStartMillis).coerceAtLeast(1L).toFloat()
        drawRoundRect(
            color = outline.copy(alpha = 0.34f),
            topLeft = Offset(horizontalPadding, trackTop),
            size = Size(trackWidth, trackHeight),
            cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f),
        )
        val hourCount = 8
        repeat(hourCount + 1) { index ->
            val x = horizontalPadding + trackWidth * (index / hourCount.toFloat())
            drawLine(
                color = muted.copy(alpha = if (index == 0 || index == hourCount) 0.30f else 0.16f),
                start = Offset(x, trackTop - 14.dp.toPx()),
                end = Offset(x, trackTop + trackHeight + 14.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
            )
        }
        data.sessions.forEach { session ->
            val startRatio = ((session.startMillis - data.dayStartMillis).toFloat() / range).coerceIn(0f, 1f)
            val endRatio = ((session.endMillis - data.dayStartMillis).toFloat() / range).coerceIn(0f, 1f)
            val left = horizontalPadding + trackWidth * startRatio
            val rawRight = horizontalPadding + trackWidth * endRatio
            val minWidth = 16.dp.toPx()
            val width = (rawRight - left).coerceAtLeast(minWidth).coerceAtMost(size.width - left - horizontalPadding)
            val color = Color(session.colorArgb)
            drawRoundRect(
                color = color,
                topLeft = Offset(left, trackTop + 5.dp.toPx()),
                size = Size(width, trackHeight - 10.dp.toPx()),
                cornerRadius = CornerRadius(999f, 999f),
            )
            val dotCount = (session.durationMillis / 25.minutesMillis()).toInt().coerceIn(1, 6)
            val dotRadius = 3.2.dp.toPx()
            val dotGap = 8.dp.toPx()
            val dotStart = left + 10.dp.toPx()
            repeat(dotCount) { dotIndex ->
                val x = dotStart + dotIndex * dotGap
                if (x < left + width - dotRadius) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.88f),
                        radius = dotRadius,
                        center = Offset(x, trackTop + trackHeight / 2f),
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineFocusStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun Int.minutesMillis(): Long = this * 60_000L

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfflineFocusRhythmProfileStrip(data: OfflineFocusSectionData) {
    val cells = remember(data) { buildOfflineFocusRhythmCells(data) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                OfflineFocusRhythmHeatGrid(
                    cells = cells,
                    modifier = Modifier.fillMaxWidth(),
                )
                OfflineFocusRhythmHourScale(modifier = Modifier.fillMaxWidth())
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                data.categories.forEach { category ->
                    OfflineFocusRhythmLegendPill(
                        color = Color(category.colorArgb),
                        label = "${category.categoryName} · ${formatDuration(category.totalMillis)}",
                    )
                }
                OfflineFocusRhythmLegendPill(
                    color = Color.White,
                    label = AppText.t("stats_rhythm_legend_idle"),
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun OfflineFocusRhythmHourScale(
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
        val gapPx = OfflineFocusRhythmCellGap.roundToPx()
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
private fun OfflineFocusRhythmHeatGrid(
    cells: List<OfflineFocusRhythmCell>,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            cells.forEach { cell ->
                OfflineFocusRhythmCellBox(cell = cell)
            }
        },
    ) { measurables, constraints ->
        val gapPx = OfflineFocusRhythmCellGap.roundToPx()
        val availableWidth = constraints.maxWidth.coerceAtLeast(0)
        val cellSize = ((availableWidth - gapPx * 23) / 24).coerceAtLeast(1)
        val width = cellSize * 24 + gapPx * 23
        val height = cellSize * 12 + gapPx * 11
        val placeables =
            measurables.map { measurable ->
                measurable.measure(androidx.compose.ui.unit.Constraints.fixed(cellSize, cellSize))
            }
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val cell = cells.getOrNull(index)
                val hour = cell?.hour ?: (index / 12)
                val slot = cell?.slot ?: (index % 12)
                placeable.placeRelative(
                    x = hour * (cellSize + gapPx),
                    y = slot * (cellSize + gapPx),
                )
            }
        }
    }
}

@Composable
private fun OfflineFocusRhythmCellBox(cell: OfflineFocusRhythmCell) {
    val fillColor =
        if (cell.durationMillis <= 0L) {
            Color.White
        } else {
            cell.color.copy(alpha = offlineFocusRhythmCellOpacity(cell.durationMillis))
        }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(3.dp))
                .background(fillColor),
    )
}

@Composable
private fun OfflineFocusRhythmLegendPill(
    color: Color,
    label: String,
    borderColor: Color? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(OfflineFocusRhythmLegendSwatchSize)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
                    .then(
                        borderColor?.let { Modifier.border(0.5.dp, it, RoundedCornerShape(4.dp)) } ?: Modifier,
                    ),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfflineFocusRhythmInsightStrip(data: OfflineFocusSectionData) {
    val insights = remember(data) { buildOfflineFocusRhythmInsights(data) }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        insights.forEach { insight ->
            OfflineFocusRhythmInsightChip(insight)
        }
    }
}

@Composable
private fun OfflineFocusRhythmInsightChip(insight: OfflineFocusRhythmInsight) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
        ),
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

private data class OfflineFocusRhythmCell(
    val hour: Int,
    val slot: Int,
    val categoryName: String?,
    val color: Color,
    val durationMillis: Long,
)

private data class OfflineFocusRhythmInsight(
    val label: String,
    val value: String,
)

private fun buildOfflineFocusRhythmCells(data: OfflineFocusSectionData): List<OfflineFocusRhythmCell> =
    (0 until OFFLINE_FOCUS_RHYTHM_CELL_COUNT).map { sliceIndex ->
        val sliceStart = data.dayStartMillis + sliceIndex * OFFLINE_FOCUS_RHYTHM_CELL_MILLIS
        val sliceEnd = sliceStart + OFFLINE_FOCUS_RHYTHM_CELL_MILLIS
        val dominant =
            data.sessions
                .mapNotNull { session ->
                    val overlap =
                        (minOf(session.endMillis, sliceEnd) - maxOf(session.startMillis, sliceStart))
                            .coerceAtLeast(0L)
                    if (overlap <= 0L) null else session to overlap
                }
                .maxByOrNull { it.second }
        OfflineFocusRhythmCell(
            hour = sliceIndex / 12,
            slot = sliceIndex % 12,
            categoryName = dominant?.first?.categoryName,
            color = dominant?.first?.let { Color(it.colorArgb) } ?: Color.White,
            durationMillis = dominant?.second ?: 0L,
        )
    }

private fun buildOfflineFocusRhythmInsights(data: OfflineFocusSectionData): List<OfflineFocusRhythmInsight> =
    listOf(
        OfflineFocusRhythmInsight(
            label = AppText.t("offline_focus_rhythm_longest_focus"),
            value = formatDuration(data.sessions.maxOfOrNull { it.durationMillis } ?: 0L),
        ),
        OfflineFocusRhythmInsight(
            label = AppText.t("offline_focus_rhythm_active_hours"),
            value = AppText.t("offline_focus_rhythm_active_hours_value", offlineFocusActiveHourCount(data)),
        ),
        OfflineFocusRhythmInsight(
            label = AppText.t("offline_focus_rhythm_category_count"),
            value = AppText.t("offline_focus_rhythm_category_count_value", data.categories.size),
        ),
    )

private fun offlineFocusActiveHourCount(data: OfflineFocusSectionData): Int =
    buildOfflineFocusRhythmCells(data)
        .filter { it.durationMillis > 0L }
        .map { it.hour }
        .distinct()
        .count()

private fun offlineFocusRhythmCellOpacity(durationMillis: Long): Float {
    val minutes = durationMillis.toFloat() / 60_000f
    return when {
        minutes <= 0f -> 0f
        minutes < 1f -> 0.2f
        minutes < 2f -> 0.4f
        minutes < 3f -> 0.6f
        minutes < 4f -> 0.8f
        else -> 1f
    }
}

private val OfflineFocusRhythmCellGap = 3.dp
private val OfflineFocusRhythmLegendSwatchSize = 16.dp
private const val OFFLINE_FOCUS_RHYTHM_CELL_COUNT = 288
private const val OFFLINE_FOCUS_RHYTHM_CELL_MILLIS = 5L * 60_000L

@Composable
private fun OfflineFocusTimelineBar(data: OfflineFocusSectionData) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = AppText.t("offline_focus_timeline"),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            val range = (data.dayEndMillis - data.dayStartMillis).coerceAtLeast(1L).toFloat()
            val minWidth = 3.dp.toPx()
            data.sessions.forEach { session ->
                val startRatio = ((session.startMillis - data.dayStartMillis).toFloat() / range).coerceIn(0f, 1f)
                val endRatio = ((session.endMillis - data.dayStartMillis).toFloat() / range).coerceIn(0f, 1f)
                val left = size.width * startRatio
                val right = size.width * endRatio
                val width = (right - left).coerceAtLeast(minWidth)
                drawRoundRect(
                    color = Color(session.colorArgb),
                    topLeft = Offset(left.coerceAtMost(size.width - minWidth), 0f),
                    size = Size(width.coerceAtMost(size.width - left), size.height),
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                )
            }
        }
    }
}

@Composable
private fun OfflineFocusSessionRow(session: OfflineFocusTimelineItem) {
    val zoneId = ZoneId.systemDefault()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Color(session.colorArgb)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${formatClockTime(session.startMillis, zoneId)} - ${formatClockTime(session.endMillis, zoneId)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${formatDuration(session.durationMillis)} · +${session.pointsAwarded.roundToInt()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OfflineFocusCategoryRow(category: OfflineFocusCategoryBreakdown) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Color(category.colorArgb)),
        )
        Text(
            text = category.categoryName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = AppText.t("offline_focus_minutes_format", (category.totalMillis / 60_000L).toInt()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
