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
internal fun HeroSkeletonCard() {
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
internal fun TimelineSkeletonCard() {
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
internal fun AppChartsSkeletonCard() {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SkeletonSectionHeader()
            SkeletonUsageSharePanel()
            SkeletonRankingPanel()
        }
    }
}

@Composable
internal fun BehaviorSkeletonCard() {
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
internal fun ComparisonSkeletonCard() {
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
internal fun SkeletonSectionHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SkeletonLine(width = 92.dp, height = 18.dp)
        SkeletonLine(width = 180.dp, height = 12.dp)
    }
}

@Composable
internal fun SkeletonMetricChip(modifier: Modifier = Modifier) {
    SkeletonBlock(
        modifier = modifier,
        height = 86.dp,
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
internal fun SkeletonPill(width: androidx.compose.ui.unit.Dp) {
    SkeletonBlock(
        modifier = Modifier.width(width),
        height = 28.dp,
        shape = RoundedCornerShape(999.dp),
    )
}

@Composable
internal fun SkeletonCircle(size: androidx.compose.ui.unit.Dp) {
    SkeletonBlock(
        modifier = Modifier.size(size),
        height = size,
        shape = CircleShape,
    )
}

@Composable
internal fun SkeletonDonutChart(chartSize: androidx.compose.ui.unit.Dp) {
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
internal fun SkeletonTimelineChart() {
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
internal fun SkeletonDonutPanel(modifier: Modifier = Modifier) {
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
internal fun SkeletonPeakPanel(modifier: Modifier = Modifier) {
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
internal fun SkeletonUsageSharePanel() {
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
internal fun SkeletonRankingPanel() {
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
internal fun SkeletonLine(
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
internal fun SkeletonBlock(
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
internal fun rememberSkeletonBrush(): Brush {
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
internal fun rememberSkeletonColors(): Pair<Color, Color> {
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

