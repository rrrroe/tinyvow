package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
