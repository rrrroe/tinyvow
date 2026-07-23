package com.rrrrz.tinyvow.ui.home

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.time.BusinessDay
import com.rrrrz.tinyvow.i18n.AppText
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

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
                )
                if (data.interruptionCount > 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.42f),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = AppText.t("offline_focus_interruptions_title"),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text =
                                    data.topInterruptionPackage?.let { packageName ->
                                        AppText.t(
                                            "offline_focus_interruptions_body_with_app",
                                            data.interruptionCount,
                                            packageName,
                                        )
                                    } ?: AppText.t(
                                        "offline_focus_interruptions_body",
                                        data.interruptionCount,
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                DailyFocusBottleDetailLayout(data = data)
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
    emptyMessage: String = AppText.t("offline_focus_daily_empty"),
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
                val emptyData = emptyOfflineFocusMarksData()
                OfflineFocusMarksHeader()
                OfflineFocusReportSummary(data = emptyData)
                OfflineFocusRhythmProfileStrip(data = emptyData, showCellIcons = false)
                DailyFocusBottleDetailLayout(data = emptyData)
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        is SectionState.Ready -> {
            val data = state.data
            ReportCard {
                OfflineFocusMarksHeader()
                OfflineFocusReportSummary(data = data)
                OfflineFocusRhythmProfileStrip(data = data, showCellIcons = false)
                DailyFocusBottleDetailLayout(data = data)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WeeklyOfflineFocusPebblesCard(
    state: SectionState<OfflineFocusSectionData>,
    emptyMessage: String = AppText.t("offline_focus_weekly_empty"),
) {
    when (state) {
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth(),
                    height = 220.dp,
                    shape = RoundedCornerShape(24.dp),
                )
            }
        }
        SectionState.Empty -> WeeklyOfflineFocusPebblesEmptyCard(emptyMessage)
        is SectionState.Ready -> {
            WeeklyOfflineFocusPebblesReadyCard(state.data)
        }
    }
}

@Composable
internal fun MonthlyOfflineFocusPebblesCard(
    state: SectionState<OfflineFocusSectionData>,
    emptyMessage: String,
) {
    when (state) {
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth(),
                    height = 420.dp,
                    shape = RoundedCornerShape(24.dp),
                )
            }
        }
        SectionState.Empty -> {
            ReportCard {
                WeeklyFocusPebblesHeader()
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        is SectionState.Ready -> {
            ReportCard {
                WeeklyFocusPebblesHeader()
                OfflineFocusReportSummary(data = state.data)
                MonthlyFocusJarCalendar(data = state.data)
                if (state.data.categories.isNotEmpty()) {
                    WeeklyFocusCategorySummaryList(categories = state.data.categories)
                }
            }
        }
    }
}

@Composable
private fun MonthlyFocusJarCalendar(data: OfflineFocusSectionData) {
    val days = remember(data) { buildMonthlyFocusPebbleDays(data) }
    val visuals = remember(days) { buildWeeklyFocusPebbleVisuals(days) }
    val sharedJarHeight = remember(visuals) { weeklyFocusPebbleJarHeight(visuals) }
    val pebbleTextures = rememberFocusPebbleTextures()
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            (0 until 7).forEach { offset ->
                Text(
                    text =
                        LocalDate.of(2024, 1, 1)
                            .plusDays(offset.toLong())
                            .dayOfWeek
                            .getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        days.chunked(7).forEachIndexed { weekIndex, week ->
            val weekVisuals = visuals.drop(weekIndex * 7).take(7)
            Box(modifier = Modifier.fillMaxWidth().height(sharedJarHeight)) {
                WeeklyFocusPebbleJarBackdrop(
                    visibleColumns = week.map { it.showJar },
                    modifier = Modifier.fillMaxSize(),
                )
                WeeklyFocusPebbleInJarLabels(
                    days = week,
                    useDateCode = true,
                )
                WeeklyFocusPebblesCanvas(
                    visuals = weekVisuals,
                    pebbleTextures = pebbleTextures,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

private fun buildMonthlyFocusPebbleDays(data: OfflineFocusSectionData): List<WeeklyFocusPebbleDay> {
    val zoneId = ZoneId.systemDefault()
    val dayStartHour = BusinessDay.cachedStartHour()
    val month = YearMonth.from(ArchiveDateUtils.localDateAt(data.dayStartMillis, zoneId, dayStartHour))
    val sessionsByDate =
        data.sessions.groupBy { session ->
            ArchiveDateUtils.localDateAt(session.startMillis, zoneId, dayStartHour)
        }
    return buildList {
        repeat(month.atDay(1).dayOfWeek.value - 1) {
            add(
                WeeklyFocusPebbleDay(
                    dayCode = "",
                    dateCode = "",
                    sessions = emptyList(),
                    showJar = false,
                ),
            )
        }
        (1..month.lengthOfMonth()).forEach { dayOfMonth ->
            val date = month.atDay(dayOfMonth)
            add(
                WeeklyFocusPebbleDay(
                    dayCode = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    dateCode = dayOfMonth.toString(),
                    sessions = sessionsByDate[date].orEmpty(),
                ),
            )
        }
        while (size % 7 != 0) {
            add(
                WeeklyFocusPebbleDay(
                    dayCode = "",
                    dateCode = "",
                    sessions = emptyList(),
                    showJar = false,
                ),
            )
        }
    }
}

@Composable
private fun WeeklyOfflineFocusPebblesReadyCard(data: OfflineFocusSectionData) {
    ReportCard {
        WeeklyFocusPebblesHeader()
        OfflineFocusReportSummary(data = data)
        WeeklyFocusPebblesChart(
            data = data,
            modifier = Modifier.fillMaxWidth(),
        )
        if (data.categories.isNotEmpty()) {
            WeeklyFocusCategorySummaryList(categories = data.categories)
        }
    }
}

@Composable
private fun WeeklyOfflineFocusPebblesEmptyCard(emptyMessage: String) {
    ReportCard {
        WeeklyFocusPebblesHeader()
        Text(
            text = emptyMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeeklyFocusPebblesHeader() =
    SectionHeader(
        icon = Icons.Default.Layers,
        title = AppText.t("offline_focus_weekly_pebbles_title"),
        subtitle = AppText.t("offline_focus_weekly_pebbles_subtitle"),
    )

private data class WeeklyFocusPebbleDay(
    val dayCode: String,
    val dateCode: String,
    val sessions: List<OfflineFocusTimelineItem>,
    val showJar: Boolean = true,
) {
    val totalMillis: Long = sessions.sumOf { it.durationMillis }
}

private data class WeeklyFocusPebbleVisual(
    val textureIndex: Int,
    val baseColorFilter: ColorFilter,
    val durationWeight: Float,
    val sizeScale: Float,
    val tiltDegrees: Float,
    val overlapDp: Float,
)

private data class WeeklyFocusPebbleGeometry(
    val visual: WeeklyFocusPebbleVisual,
    val width: Float,
    val height: Float,
    val overlap: Float,
)

@Composable
private fun WeeklyFocusPebblesChart(
    data: OfflineFocusSectionData,
    modifier: Modifier = Modifier,
) {
    val days = remember(data) { buildWeeklyFocusPebbleDays(data) }
    val visuals = remember(days) { buildWeeklyFocusPebbleVisuals(days) }
    val jarPanelHeight = remember(visuals) { weeklyFocusPebbleJarHeight(visuals) }
    val pebbleTextures = rememberFocusPebbleTextures()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(jarPanelHeight)) {
            WeeklyFocusPebbleJarBackdrop(
                visibleColumns = days.map { it.showJar },
                modifier = Modifier.fillMaxSize(),
            )
            WeeklyFocusPebbleInJarLabels(
                days = days,
                useDateCode = false,
            )
            WeeklyFocusPebblesCanvas(
                visuals = visuals,
                pebbleTextures = pebbleTextures,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun WeeklyFocusPebbleInJarLabels(
    days: List<WeeklyFocusPebbleDay>,
    useDateCode: Boolean,
) {
    val themeColors = com.rrrrz.tinyvow.ui.theme.LocalThemeColors.current
    val labelWidth = 34.dp
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        days.forEachIndexed { index, day ->
            if (!day.showJar) return@forEachIndexed
            val x =
                (maxWidth * weeklyFocusPebbleJarCenterFraction(index) - labelWidth / 2)
                    .coerceIn(0.dp, maxWidth - labelWidth)
            Text(
                text = if (useDateCode) day.dateCode else day.dayCode,
                modifier =
                    Modifier
                        .width(labelWidth)
                        .offset(
                            x = x,
                            y = ((maxHeight - 28.dp) / 2).coerceAtLeast(0.dp),
                        ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color =
                    if (day.sessions.isNotEmpty()) {
                        themeColors.inkStrong.copy(alpha = 0.20f)
                    } else {
                        themeColors.inkMuted.copy(alpha = 0.13f)
                    },
                textAlign = TextAlign.Center,
                minLines = 1,
                maxLines = 1,
            )
        }
    }
}

private val WeeklyFocusPebbleJarArtworkCenters =
    floatArrayOf(160f, 431f, 695f, 953.5f, 1213.5f, 1477.5f, 1741f)

private const val WeeklyFocusPebbleJarArtworkWidth = 1904f

private fun weeklyFocusPebbleJarCenterFraction(index: Int): Float =
    WeeklyFocusPebbleJarArtworkCenters[index] / WeeklyFocusPebbleJarArtworkWidth

private fun weeklyFocusPebbleJarHeight(visuals: List<List<WeeklyFocusPebbleVisual>>): Dp {
    val tallestPebbleStackDp =
        visuals.maxOfOrNull { stones ->
            stones.sumOf { visual ->
                ((6.4f + visual.durationWeight * 14.4f) * visual.sizeScale).toDouble()
            }.toFloat() - stones.dropLast(1).sumOf { it.overlapDp.toDouble() }.toFloat()
        } ?: 0f
    if (tallestPebbleStackDp <= 0f) return 120.dp
    return (tallestPebbleStackDp + 70f).coerceAtMost(276f).dp
}

@Composable
private fun WeeklyFocusPebbleJarBackdrop(
    visibleColumns: List<Boolean> = List(7) { true },
    modifier: Modifier = Modifier,
) {
    val artwork = ImageBitmap.imageResource(R.drawable.focus_pebble_jars_background)
    Canvas(modifier = modifier) {
        val sourceTopHeight = 230
        val sourceBottomStart = 700
        val destinationWidth = size.width.roundToInt().coerceAtLeast(1)
        val horizontalScale = size.width / artwork.width.toFloat()
        val naturalTopHeight = sourceTopHeight * horizontalScale
        val naturalBottomHeight = (artwork.height - sourceBottomStart) * horizontalScale
        val fixedHeightScale = minOf(1f, size.height / (naturalTopHeight + naturalBottomHeight).coerceAtLeast(1f))
        val topHeight = naturalTopHeight * fixedHeightScale
        val bottomHeight = naturalBottomHeight * fixedHeightScale
        val middleHeight = (size.height - topHeight - bottomHeight).coerceAtLeast(0f)

        fun drawVerticalSlice(
            sourceY: Int,
            sourceHeight: Int,
            destinationY: Float,
            destinationHeight: Float,
        ) {
            if (destinationHeight <= 0.5f) return
            drawImage(
                image = artwork,
                srcOffset = IntOffset(0, sourceY),
                srcSize = IntSize(artwork.width, sourceHeight),
                dstOffset = IntOffset(0, destinationY.roundToInt()),
                dstSize = IntSize(destinationWidth, destinationHeight.roundToInt().coerceAtLeast(1)),
                filterQuality = FilterQuality.High,
            )
        }

        fun drawBottleArtwork() {
            drawVerticalSlice(0, sourceTopHeight, 0f, topHeight)
            drawVerticalSlice(
                sourceTopHeight,
                sourceBottomStart - sourceTopHeight,
                topHeight,
                middleHeight,
            )
            drawVerticalSlice(
                sourceBottomStart,
                artwork.height - sourceBottomStart,
                topHeight + middleHeight,
                bottomHeight,
            )
        }

        (0 until 7).forEach { index ->
            if (visibleColumns.getOrElse(index) { true }) {
                val leftFraction =
                    if (index == 0) {
                        0f
                    } else {
                        (weeklyFocusPebbleJarCenterFraction(index - 1) + weeklyFocusPebbleJarCenterFraction(index)) / 2f
                    }
                val rightFraction =
                    if (index == 6) {
                        1f
                    } else {
                        (weeklyFocusPebbleJarCenterFraction(index) + weeklyFocusPebbleJarCenterFraction(index + 1)) / 2f
                    }
                clipRect(
                    left = size.width * leftFraction,
                    right = size.width * rightFraction,
                ) {
                    drawBottleArtwork()
                }
            }
        }
    }
}

@Composable
private fun WeeklyFocusCategorySummaryList(categories: List<OfflineFocusCategoryBreakdown>) {
    val sortedCategories = remember(categories) { categories.sortedByDescending { it.totalMillis } }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        sortedCategories.forEach { category ->
            WeeklyFocusCategorySummaryRow(category = category)
        }
    }
}

@Composable
private fun WeeklyFocusCategorySummaryRow(category: OfflineFocusCategoryBreakdown) {
    val categoryColor = Color(category.colorArgb)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1.35f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FocusTypeIcon(
                iconKey = category.iconKey,
                customIconPath = category.customIconPath,
                color = categoryColor,
                modifier = Modifier.size(26.dp),
            )
            Text(
                text = category.categoryName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        WeeklyFocusCategoryTableValue(
            text = formatDuration(category.totalMillis),
            modifier = Modifier.weight(0.72f),
        )
        WeeklyFocusCategoryTableValue(
            text = AppText.t("offline_focus_weekly_category_count", category.completedCount),
            modifier = Modifier.weight(0.50f),
        )
        WeeklyFocusCategoryTableValue(
            text = AppText.t("offline_focus_weekly_category_points", category.pointsAwarded.roundToInt()),
            modifier = Modifier.weight(0.95f),
        )
    }
}

@Composable
private fun WeeklyFocusCategoryTableValue(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun WeeklyFocusPebblesCanvas(
    visuals: List<List<WeeklyFocusPebbleVisual>>,
    pebbleTextures: List<ImageBitmap>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val columnWidth = size.width / 7f
        // Leave real breathing room on both sides of the glass interior, including for texture edges.
        val maxPebbleWidth = columnWidth * 0.50f
        val rawGeometries =
            visuals.map { dayVisuals ->
                dayVisuals.map { visual ->
                    val texture = pebbleTextures[visual.textureIndex]
                    val wantedHeight =
                        (6.4f + visual.durationWeight * 14.4f) *
                            visual.sizeScale *
                            density
                    val scale =
                        minOf(
                            wantedHeight / texture.height.toFloat(),
                            maxPebbleWidth / texture.width.toFloat(),
                        )
                    val height = texture.height * scale
                    WeeklyFocusPebbleGeometry(
                        visual = visual,
                        width = texture.width * scale,
                        height = height,
                        overlap = minOf(visual.overlapDp * density, height * 0.12f),
                    )
                }
            }
        val maxRawStackHeight =
            rawGeometries.maxOfOrNull { stones ->
                stones.sumOf { it.height.toDouble() }.toFloat() -
                    stones.dropLast(1).sumOf { it.overlap.toDouble() }.toFloat()
            } ?: 1f
        // The jar height already includes the 70dp breathing room requested around the
        // tallest stack. Do not reserve another fixed top inset here, or the same pebbles
        // get scaled down merely because the bottle body is shorter.
        val innerTop = 0f
        val innerBottom = size.height - 28.dp.toPx()
        val availableInteriorHeight = (innerBottom - innerTop).coerceAtLeast(1f)
        val minimumInteriorHeight = minOf(78.dp.toPx(), availableInteriorHeight)
        val targetInteriorHeight =
            minOf(availableInteriorHeight, maxOf(minimumInteriorHeight, maxRawStackHeight + 7.dp.toPx()))
        val fitScale = minOf(1f, targetInteriorHeight / maxRawStackHeight.coerceAtLeast(1f))

        rawGeometries.forEachIndexed { dayIndex, stones ->
            val columnCenter = size.width * weeklyFocusPebbleJarCenterFraction(dayIndex)
            val innerLeft = columnCenter - columnWidth * 0.31f
            val innerRight = columnCenter + columnWidth * 0.31f
            val wallPadding = 1.dp.toPx()
            clipRect(left = innerLeft, top = innerTop, right = innerRight, bottom = innerBottom) {
                var bottom = innerBottom
                stones.forEachIndexed { stoneIndex, stone ->
                    val pebbleWidth = stone.width * fitScale
                    val pebbleHeight = stone.height * fitScale
                    val top = bottom - pebbleHeight
                    val left =
                        (columnCenter - pebbleWidth / 2f).coerceIn(
                            innerLeft + wallPadding,
                            innerRight - wallPadding - pebbleWidth,
                        )
                    drawWeeklyFocusPebble(
                        texture = pebbleTextures[stone.visual.textureIndex],
                        baseColorFilter = stone.visual.baseColorFilter,
                        left = left,
                        top = top,
                        width = pebbleWidth,
                        height = pebbleHeight,
                        tiltDegrees = stone.visual.tiltDegrees,
                        isGroundStone = stoneIndex == 0,
                    )
                    bottom = top + stone.overlap * fitScale
                }
            }
        }
    }
}

private fun DrawScope.drawWeeklyFocusPebble(
    texture: ImageBitmap,
    baseColorFilter: ColorFilter,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    tiltDegrees: Float,
    isGroundStone: Boolean,
) {
    if (height <= 0.5f || width <= 0f) return
    val center = Offset(left + width / 2f, top + height / 2f)
    rotate(degrees = tiltDegrees, pivot = center) {
        if (isGroundStone) {
            val shadowHeight = minOf(3.dp.toPx(), height * 0.22f).coerceAtLeast(1f)
            drawOval(
                color = Color.Black.copy(alpha = 0.035f),
                topLeft = Offset(left + width * 0.02f, top + height - shadowHeight * 0.18f),
                size = Size(width * 0.96f, shadowHeight * 1.65f),
            )
            drawOval(
                color = Color.Black.copy(alpha = 0.075f),
                topLeft = Offset(left + width * 0.08f, top + height + shadowHeight * 0.04f),
                size = Size(width * 0.84f, shadowHeight),
            )
            drawOval(
                color = Color.Black.copy(alpha = 0.10f),
                topLeft = Offset(left + width * 0.16f, top + height + shadowHeight * 0.18f),
                size = Size(width * 0.68f, shadowHeight * 0.48f),
            )
        } else {
            val contactHeight = minOf(1.35.dp.toPx(), height * 0.13f).coerceAtLeast(0.7f)
            drawOval(
                color = Color.Black.copy(alpha = 0.09f),
                topLeft = Offset(left + width * 0.10f, top + height - contactHeight * 0.28f),
                size = Size(width * 0.80f, contactHeight * 1.65f),
            )
            drawOval(
                color = Color.Black.copy(alpha = 0.19f),
                topLeft = Offset(left + width * 0.20f, top + height + contactHeight * 0.05f),
                size = Size(width * 0.60f, contactHeight * 0.72f),
            )
        }
        drawContext.canvas.saveLayer(Rect(left, top, left + width, top + height), Paint())
        drawImage(
            image = texture,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(texture.width, texture.height),
            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
            dstSize = IntSize(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1)),
            alpha = 0.99f,
            colorFilter = baseColorFilter,
            filterQuality = FilterQuality.High,
        )
        drawImage(
            image = texture,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(texture.width, texture.height),
            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
            dstSize = IntSize(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1)),
            colorFilter = WeeklyFocusPebbleShadowFilter,
            blendMode = BlendMode.SrcAtop,
            filterQuality = FilterQuality.High,
        )
        drawImage(
            image = texture,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(texture.width, texture.height),
            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
            dstSize = IntSize(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1)),
            colorFilter = WeeklyFocusPebbleHighlightFilter,
            blendMode = BlendMode.SrcAtop,
            filterQuality = FilterQuality.High,
        )
        drawContext.canvas.restore()
    }
}

private fun buildWeeklyFocusPebbleVisuals(
    days: List<WeeklyFocusPebbleDay>,
): List<List<WeeklyFocusPebbleVisual>> {
    val maxSessionMillis =
        days.flatMap { it.sessions }.maxOfOrNull { it.durationMillis }?.coerceAtLeast(1L) ?: 1L
    val compactTextures = intArrayOf(1, 2, 4, 6, 9, 10, 11, 13)
    val mediumTextures = intArrayOf(0, 1, 2, 4, 5, 6, 8, 9, 10, 11, 12, 13)
    val flatTextures = intArrayOf(0, 3, 5, 7, 8, 12, 14)
    return days.mapIndexed { dayIndex, day ->
        day.sessions.sortedBy { it.startMillis }.mapIndexed { sessionIndex, session ->
            val seed =
                (session.startMillis * -7_046_029_254_386_353_131L) xor
                    java.lang.Long.rotateLeft(session.durationMillis * 31L, 21) xor
                    (dayIndex * 1_000_003L + sessionIndex * 97_409L)
            val random = Random(seed)
            val durationRatio =
                (session.durationMillis.toFloat() / maxSessionMillis.toFloat()).coerceIn(0.02f, 1f)
            val durationWeight = durationRatio.pow(0.72f)
            val texturePool =
                when {
                    durationWeight >= 0.70f -> compactTextures
                    durationWeight >= 0.38f -> mediumTextures
                    else -> flatTextures
                }
            WeeklyFocusPebbleVisual(
                textureIndex = texturePool[random.nextInt(texturePool.size)],
                baseColorFilter = weeklyFocusPebbleBaseColorFilter(Color(session.colorArgb)),
                durationWeight = durationWeight,
                sizeScale = 0.94f + random.nextFloat() * 0.12f,
                tiltDegrees =
                    if (sessionIndex == 0) {
                        (random.nextFloat() - 0.5f) * 0.8f
                    } else {
                        (random.nextFloat() - 0.5f) * 2.0f
                    },
                overlapDp = 1.0f + random.nextFloat() * 0.55f,
            )
        }
    }
}

private fun weeklyFocusPebbleBaseColorFilter(categoryColor: Color): ColorFilter =
    ColorFilter.tint(
        androidx.compose.ui.graphics.lerp(categoryColor, Color(0xFFF3EEE7), 0.03f),
        BlendMode.SrcIn,
    )

private val WeeklyFocusPebbleShadowFilter =
    ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                -0.72f * 0.2126f,
                -0.72f * 0.7152f,
                -0.72f * 0.0722f,
                0f,
                0.72f * 0.66f * 255f,
            ),
        ),
    )

private val WeeklyFocusPebbleHighlightFilter =
    ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0f, 0f, 0f, 0f, 255f,
                0f, 0f, 0f, 0f, 255f,
                0f, 0f, 0f, 0f, 255f,
                1.18f * 0.2126f,
                1.18f * 0.7152f,
                1.18f * 0.0722f,
                0f,
                -1.18f * 0.58f * 255f,
            ),
        ),
    )

private fun buildWeeklyFocusPebbleDays(data: OfflineFocusSectionData): List<WeeklyFocusPebbleDay> {
    val zoneId = ZoneId.systemDefault()
    val dayStartHour = BusinessDay.cachedStartHour()
    val startDate = ArchiveDateUtils.localDateAt(data.dayStartMillis, zoneId, dayStartHour)
    val sessionsByDate =
        data.sessions.groupBy { session ->
            ArchiveDateUtils.localDateAt(session.startMillis, zoneId, dayStartHour)
        }
    return (0 until 7).map { index ->
        val date = startDate.plusDays(index.toLong())
        WeeklyFocusPebbleDay(
            dayCode = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
            dateCode = date.dayOfMonth.toString(),
            sessions = sessionsByDate[date].orEmpty(),
        )
    }
}

@Composable
private fun OfflineFocusMarksHeader() {
    val themeColors = com.rrrrz.tinyvow.ui.theme.LocalThemeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = AppText.t("offline_focus_marks_title"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = AppText.t("offline_focus_marks_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
        )
    }
}

@Composable
private fun OfflineFocusReportSummary(data: OfflineFocusSectionData) {
    val themeColors = com.rrrrz.tinyvow.ui.theme.LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OfflineFocusReportSummaryValue(
                color = themeColors.save,
                label = AppText.t("offline_focus_report_summary_duration"),
                value = formatDuration(data.totalMillis),
                modifier = Modifier.weight(1f),
            )
            OfflineFocusReportSummaryValue(
                color = themeColors.base,
                label = AppText.t("offline_focus_report_summary_sessions"),
                value = AppText.t("offline_focus_today_sessions_value", data.completedCount),
                modifier = Modifier.weight(1f),
            )
            OfflineFocusReportSummaryValue(
                color = themeColors.encourage,
                label = AppText.t("offline_focus_report_summary_points"),
                value = AppText.t("offline_focus_today_points_value", data.pointsAwarded.roundToInt()),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OfflineFocusReportSummaryValue(
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OfflineFocusPomodoroCanvas(data: OfflineFocusSectionData) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val rail = MaterialTheme.colorScheme.surfaceContainerLow
    val outline = MaterialTheme.colorScheme.outlineVariant
    val paused = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
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
            val wallDuration = (session.endMillis - session.startMillis).coerceAtLeast(1L)
            val activeSegments = offlineFocusActiveSegments(session)
            if (session.pauseIntervals.isNotEmpty()) {
                drawRoundRect(
                    color = paused,
                    topLeft = Offset(left, trackTop + 5.dp.toPx()),
                    size = Size(width, trackHeight - 10.dp.toPx()),
                    cornerRadius = CornerRadius(999f, 999f),
                )
            }
            val activePixelSegments =
                activeSegments.map { segment ->
                    val segmentLeft =
                        left + width * (segment.startMillis - session.startMillis).toFloat() / wallDuration.toFloat()
                    val segmentRight =
                        left + width * (segment.endMillis - session.startMillis).toFloat() / wallDuration.toFloat()
                    segmentLeft to segmentRight
                }
            activePixelSegments.forEach { (segmentLeft, segmentRight) ->
                val segmentWidth = (segmentRight - segmentLeft).coerceAtLeast(0f)
                if (segmentWidth > 0f) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(segmentLeft, trackTop + 5.dp.toPx()),
                        size = Size(segmentWidth, trackHeight - 10.dp.toPx()),
                        cornerRadius = CornerRadius(999f, 999f),
                    )
                }
            }
            val dotCount = (session.durationMillis / 25.minutesMillis()).toInt().coerceIn(1, 6)
            val dotRadius = 3.2.dp.toPx()
            val dotGap = 8.dp.toPx()
            val dotStart = left + 10.dp.toPx()
            repeat(dotCount) { dotIndex ->
                val x = dotStart + dotIndex * dotGap
                if (
                    x < left + width - dotRadius &&
                    activePixelSegments.any { (segmentLeft, segmentRight) ->
                        x - dotRadius >= segmentLeft && x + dotRadius <= segmentRight
                    }
                ) {
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

private data class OfflineFocusActiveSegment(
    val startMillis: Long,
    val endMillis: Long,
)

private fun offlineFocusActiveSegments(session: OfflineFocusTimelineItem): List<OfflineFocusActiveSegment> {
    if (session.pauseIntervals.isEmpty()) {
        return listOf(OfflineFocusActiveSegment(session.startMillis, session.endMillis))
    }
    return buildList {
        var cursor = session.startMillis
        session.pauseIntervals.sortedBy { it.startMillis }.forEach { pause ->
            val pauseStart = pause.startMillis.coerceIn(session.startMillis, session.endMillis)
            val pauseEnd = pause.endMillis.coerceIn(session.startMillis, session.endMillis)
            if (pauseStart > cursor) {
                add(OfflineFocusActiveSegment(cursor, pauseStart))
            }
            cursor = maxOf(cursor, pauseEnd)
        }
        if (cursor < session.endMillis) {
            add(OfflineFocusActiveSegment(cursor, session.endMillis))
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

@Composable
private fun DailyFocusBottleDetailLayout(data: OfflineFocusSectionData) {
    val sessions = remember(data.sessions) { data.sessions.sortedByDescending { it.startMillis } }
    val visuals =
        remember(data.sessions) {
            buildWeeklyFocusPebbleVisuals(
                listOf(WeeklyFocusPebbleDay(dayCode = "", dateCode = "", sessions = data.sessions)),
            ).first()
        }
    val textures = rememberFocusPebbleTextures()
    val bottleMinHeight = remember(visuals) { dailyFocusPebbleBottleMinHeight(visuals) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).heightIn(min = bottleMinHeight),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            DailyFocusPebbleBottle(
                visuals = visuals,
                pebbleTextures = textures,
                modifier = Modifier.width(DAILY_FOCUS_BOTTLE_WIDTH_DP.dp).fillMaxHeight(),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                sessions.forEach { session ->
                    DailyFocusBottleSessionRow(session = session)
                }
            }
        }
    }
}

private const val DAILY_FOCUS_BOTTLE_WIDTH_DP = 42f
private const val DAILY_FOCUS_BOTTLE_SOURCE_WIDTH = 252
private const val DAILY_FOCUS_BOTTLE_TOP_SOURCE_HEIGHT = 150
private const val DAILY_FOCUS_BOTTLE_BOTTOM_SOURCE_HEIGHT = 60
private const val DAILY_FOCUS_BOTTLE_INNER_TOP_DP = 12f
private const val DAILY_FOCUS_BOTTLE_INNER_BOTTOM_DP = 9f
private const val DAILY_FOCUS_BOTTLE_MIN_HEIGHT_DP = 64f
private const val DAILY_FOCUS_BOTTLE_FIT_SLACK_DP = 2f

private fun dailyFocusPebbleBottleMinHeight(visuals: List<WeeklyFocusPebbleVisual>): Dp {
    val rawStackHeightDp =
        (
            visuals.sumOf { visual ->
                ((6.4f + visual.durationWeight * 14.4f) * visual.sizeScale).toDouble()
            }.toFloat() - visuals.dropLast(1).sumOf { it.overlapDp.toDouble() }.toFloat()
        ).coerceAtLeast(0f)
    val bottleChromeHeightDp =
        DAILY_FOCUS_BOTTLE_WIDTH_DP *
            (DAILY_FOCUS_BOTTLE_TOP_SOURCE_HEIGHT + DAILY_FOCUS_BOTTLE_BOTTOM_SOURCE_HEIGHT) /
            DAILY_FOCUS_BOTTLE_SOURCE_WIDTH.toFloat() +
            DAILY_FOCUS_BOTTLE_INNER_TOP_DP +
            DAILY_FOCUS_BOTTLE_INNER_BOTTOM_DP
    return maxOf(
        DAILY_FOCUS_BOTTLE_MIN_HEIGHT_DP,
        rawStackHeightDp + bottleChromeHeightDp + DAILY_FOCUS_BOTTLE_FIT_SLACK_DP,
    ).dp
}

@Composable
private fun DailyFocusBottleSessionRow(session: OfflineFocusTimelineItem) {
    val zoneId = ZoneId.systemDefault()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(session.colorArgb)),
            )
            Text(
                text = session.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "${formatClockTime(session.startMillis, zoneId)}–${formatClockTime(session.endMillis, zoneId)}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Text(
            text = formatDuration(session.durationMillis),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Text(
            text = "+${session.pointsAwarded.roundToInt()}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
    }
}

@Composable
private fun DailyFocusPebbleBottle(
    visuals: List<WeeklyFocusPebbleVisual>,
    pebbleTextures: List<ImageBitmap>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        DailyFocusPebbleBottleBackdrop(modifier = Modifier.fillMaxSize())
        DailyFocusPebbleBottleCanvas(
            visuals = visuals,
            pebbleTextures = pebbleTextures,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun DailyFocusPebbleBottleBackdrop(modifier: Modifier = Modifier) {
    val artwork = ImageBitmap.imageResource(R.drawable.focus_pebble_jars_background)
    Canvas(modifier = modifier) {
        val sourceLeft = 828
        val sourceWidth = DAILY_FOCUS_BOTTLE_SOURCE_WIDTH
        val sourceTop = 80
        val sourceMiddleTop = sourceTop + DAILY_FOCUS_BOTTLE_TOP_SOURCE_HEIGHT
        val sourceBottomTop = 700
        val sourceBottom = sourceBottomTop + DAILY_FOCUS_BOTTLE_BOTTOM_SOURCE_HEIGHT
        val horizontalScale = size.width / sourceWidth.toFloat()
        val topHeight = ((sourceMiddleTop - sourceTop) * horizontalScale).coerceAtMost(size.height)
        val bottomHeight =
            ((sourceBottom - sourceBottomTop) * horizontalScale)
                .coerceAtMost((size.height - topHeight).coerceAtLeast(0f))
        val middleHeight = (size.height - topHeight - bottomHeight).coerceAtLeast(0f)

        fun drawSlice(sourceY: Int, sourceHeight: Int, destinationY: Float, destinationHeight: Float) {
            if (destinationHeight <= 0.5f) return
            drawImage(
                image = artwork,
                srcOffset = IntOffset(sourceLeft, sourceY),
                srcSize = IntSize(sourceWidth, sourceHeight),
                dstOffset = IntOffset(0, destinationY.roundToInt()),
                dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), destinationHeight.roundToInt().coerceAtLeast(1)),
                filterQuality = FilterQuality.High,
            )
        }

        drawSlice(sourceTop, sourceMiddleTop - sourceTop, 0f, topHeight)
        drawSlice(sourceMiddleTop, sourceBottomTop - sourceMiddleTop, topHeight, middleHeight)
        drawSlice(sourceBottomTop, sourceBottom - sourceBottomTop, topHeight + middleHeight, bottomHeight)
    }
}

@Composable
private fun DailyFocusPebbleBottleCanvas(
    visuals: List<WeeklyFocusPebbleVisual>,
    pebbleTextures: List<ImageBitmap>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val sourceWidth = DAILY_FOCUS_BOTTLE_SOURCE_WIDTH.toFloat()
        val topHeight = (DAILY_FOCUS_BOTTLE_TOP_SOURCE_HEIGHT * size.width / sourceWidth).coerceAtMost(size.height)
        val bottomHeight =
            (DAILY_FOCUS_BOTTLE_BOTTOM_SOURCE_HEIGHT * size.width / sourceWidth)
                .coerceAtMost((size.height - topHeight).coerceAtLeast(0f))
        val innerLeft = size.width * 0.19f
        val innerRight = size.width * 0.81f
        val innerTop = topHeight + DAILY_FOCUS_BOTTLE_INNER_TOP_DP.dp.toPx()
        val innerBottom = size.height - bottomHeight - DAILY_FOCUS_BOTTLE_INNER_BOTTOM_DP.dp.toPx()
        val maxPebbleWidth = size.width * 0.46f
        val stones =
            visuals.map { visual ->
                val texture = pebbleTextures[visual.textureIndex]
                val wantedHeight = (6.4f + visual.durationWeight * 14.4f) * visual.sizeScale * density
                val scale = minOf(wantedHeight / texture.height.toFloat(), maxPebbleWidth / texture.width.toFloat())
                WeeklyFocusPebbleGeometry(
                    visual = visual,
                    width = texture.width * scale,
                    height = texture.height * scale,
                    overlap = minOf(visual.overlapDp * density, texture.height * scale * 0.12f),
                )
            }
        val rawStackHeight =
            stones.sumOf { it.height.toDouble() }.toFloat() -
                stones.dropLast(1).sumOf { it.overlap.toDouble() }.toFloat()
        val availableHeight = (innerBottom - innerTop).coerceAtLeast(1f)
        val fitScale = minOf(1f, availableHeight / rawStackHeight.coerceAtLeast(1f))
        val centerX = size.width / 2f
        val wallPadding = 1.dp.toPx()
        clipRect(left = innerLeft, top = innerTop, right = innerRight, bottom = innerBottom) {
            var bottom = innerBottom
            stones.forEachIndexed { index, stone ->
                val width = stone.width * fitScale
                val height = stone.height * fitScale
                val left =
                    (centerX - width / 2f).coerceIn(
                        innerLeft + wallPadding,
                        innerRight - wallPadding - width,
                    )
                val top = bottom - height
                drawWeeklyFocusPebble(
                    texture = pebbleTextures[stone.visual.textureIndex],
                    baseColorFilter = stone.visual.baseColorFilter,
                    left = left,
                    top = top,
                    width = width,
                    height = height,
                    tiltDegrees = stone.visual.tiltDegrees,
                    isGroundStone = index == 0,
                )
                bottom = top + stone.overlap * fitScale
            }
        }
    }
}

@Composable
private fun rememberFocusPebbleTextures(): List<ImageBitmap> =
    listOf(
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_1),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_2),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_3),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_4),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_5),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_6),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_7),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_8),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_9),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_10),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_11),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_12),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_13),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_14),
        ImageBitmap.imageResource(R.drawable.focus_pebble_variant_15),
    )

private fun Int.minutesMillis(): Long = this * 60_000L

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfflineFocusRhythmProfileStrip(
    data: OfflineFocusSectionData,
    showCellIcons: Boolean,
) {
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
                    data = data,
                    cells = cells,
                    showCellIcons = showCellIcons,
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
                        iconKey = category.iconKey,
                        customIconPath = category.customIconPath,
                    )
                }
                if (data.sessions.any { it.pauseIntervals.isNotEmpty() }) {
                    OfflineFocusRhythmLegendPill(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f),
                        label = AppText.t("offline_focus_rhythm_legend_paused"),
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                OfflineFocusRhythmLegendPill(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
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
    data: OfflineFocusSectionData,
    cells: List<OfflineFocusRhythmCell>,
    showCellIcons: Boolean,
    modifier: Modifier = Modifier,
) {
    val pauseColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
    val sessionMarks = remember(data, pauseColor) { buildOfflineFocusRhythmSessionMarks(data, pauseColor) }
    val emptyCellColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
    Layout(
        modifier = modifier,
        content = {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawOfflineFocusRhythmBaseGrid(
                    cells = cells,
                    sessionMarks = sessionMarks.takeUnless { showCellIcons }.orEmpty(),
                    cellSize = size.width / 24f - ((OfflineFocusRhythmCellGap.toPx() * 23f) / 24f),
                    gap = OfflineFocusRhythmCellGap.toPx(),
                    emptyColor = emptyCellColor,
                )
            }
            sessionMarks.forEach { mark ->
                OfflineFocusRhythmEnergyBar(
                    mark = mark,
                    showIcon = showCellIcons && !mark.isPaused,
                )
            }
        },
    ) { measurables, constraints ->
        val gapPx = OfflineFocusRhythmCellGap.roundToPx()
        val availableWidth = constraints.maxWidth.coerceAtLeast(0)
        val cellSize = ((availableWidth - gapPx * 23) / 24).coerceAtLeast(1)
        val width = cellSize * 24 + gapPx * 23
        val height = cellSize * 12 + gapPx * 11
        val placeables =
            measurables.mapIndexed { index, measurable ->
                if (index == 0) {
                    measurable.measure(androidx.compose.ui.unit.Constraints.fixed(width, height))
                } else {
                    val mark = sessionMarks.getOrNull(index - 1)
                    val markHeight =
                        mark
                            ?.let { offlineFocusRhythmMarkHeight(it, cellSize, gapPx) }
                            ?: cellSize
                    measurable.measure(androidx.compose.ui.unit.Constraints.fixed(cellSize, markHeight))
                }
            }
        layout(width, height) {
            placeables.firstOrNull()?.placeRelative(x = 0, y = 0)
            placeables.drop(1).forEachIndexed { index, placeable ->
                val mark = sessionMarks.getOrNull(index) ?: return@forEachIndexed
                placeable.placeRelative(
                    x = mark.hour * (cellSize + gapPx),
                    y = mark.firstSlot * (cellSize + gapPx),
                )
            }
        }
    }
}

@Composable
private fun OfflineFocusRhythmCellIcon(
    iconKey: String?,
    customIconPath: String?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val customBitmap =
        remember(customIconPath) {
            customIconPath
                ?.takeIf { it.isNotBlank() }
                ?.let { path -> runCatching { BitmapFactory.decodeFile(File(path).absolutePath) }.getOrNull() }
        }
    val presetIconResource = remember(iconKey) { iconKey?.let(::focusPresetIconResource) }
    val presetIcon = remember(iconKey) { iconKey?.let(::focusPresetIconVector) }
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(2.dp))
                .background(if (presetIconResource == null) Color.White else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        when {
            customBitmap != null -> {
                Image(
                    bitmap = customBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(2.dp)),
                )
            }
            presetIconResource != null -> {
                Image(
                    painter = painterResource(id = presetIconResource),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            presetIcon != null -> {
                androidx.compose.material3.Icon(
                    imageVector = presetIcon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.fillMaxSize(0.74f),
                )
            }
            else -> {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.fillMaxSize(0.70f),
                )
            }
        }
    }
}

@Composable
private fun OfflineFocusRhythmEnergyBar(
    mark: OfflineFocusRhythmSessionMark,
    showIcon: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(if (showIcon) mark.color.copy(alpha = 0.10f) else Color.Transparent)
                .border(1.dp, mark.color.copy(alpha = if (showIcon) 0.34f else 0f), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (showIcon) {
            BoxWithConstraints(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clipToBounds(),
            ) {
                val iconSize = maxWidth * OfflineFocusRhythmBarIconScale
                val iconStep = iconSize * 0.78f
                val count = ((maxHeight / iconStep).toInt() + 2).coerceAtLeast(1)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    repeat(count) {
                        OfflineFocusRhythmCellIcon(
                            iconKey = mark.iconKey,
                            customIconPath = mark.customIconPath,
                            color = mark.color,
                            modifier =
                                Modifier
                                    .size(iconSize)
                                    .offset(y = if (it == 0) 0.dp else (-1).dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineFocusRhythmLegendPill(
    color: Color,
    label: String,
    iconKey: String? = null,
    customIconPath: String? = null,
    borderColor: Color? = null,
) {
    val hasIcon = !iconKey.isNullOrBlank() || !customIconPath.isNullOrBlank()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(OfflineFocusRhythmLegendSwatchSize)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (hasIcon) Color.Transparent else color)
                    .then(
                        if (!hasIcon) {
                            borderColor?.let { Modifier.border(0.5.dp, it, RoundedCornerShape(4.dp)) } ?: Modifier
                        } else {
                            Modifier
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (hasIcon) {
                FocusTypeIcon(
                    iconKey = iconKey.orEmpty(),
                    customIconPath = customIconPath,
                    color = color,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
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
    val iconKey: String?,
    val customIconPath: String?,
    val color: Color,
    val durationMillis: Long,
)

private data class OfflineFocusRhythmSessionMark(
    val hour: Int,
    val firstSlot: Int,
    val lastSlot: Int,
    val categoryName: String,
    val iconKey: String,
    val customIconPath: String?,
    val color: Color,
    val isPaused: Boolean = false,
)

private data class OfflineFocusRhythmSourceSegment(
    val session: OfflineFocusTimelineItem,
    val startMillis: Long,
    val endMillis: Long,
    val color: Color,
    val isPaused: Boolean,
)

private data class OfflineFocusRhythmInsight(
    val label: String,
    val value: String,
)

private fun emptyOfflineFocusMarksData(): OfflineFocusSectionData =
    OfflineFocusSectionData(
        totalMillis = 0L,
        completedCount = 0,
        pointsAwarded = 0.0,
        dayStartMillis = 0L,
        dayEndMillis = 24L * 60L * 60_000L,
        sessions = emptyList(),
        categories = emptyList(),
    )

private fun buildOfflineFocusRhythmCells(data: OfflineFocusSectionData): List<OfflineFocusRhythmCell> =
    (0 until OFFLINE_FOCUS_RHYTHM_CELL_COUNT).map { sliceIndex ->
        val sliceStart = data.dayStartMillis + sliceIndex * OFFLINE_FOCUS_RHYTHM_CELL_MILLIS
        val sliceEnd = sliceStart + OFFLINE_FOCUS_RHYTHM_CELL_MILLIS
        val dominant =
            data.sessions
                .flatMap { session ->
                    offlineFocusActiveSegments(session).map { segment -> session to segment }
                }
                .mapNotNull { (session, segment) ->
                    val overlap =
                        (minOf(segment.endMillis, sliceEnd) - maxOf(segment.startMillis, sliceStart))
                            .coerceAtLeast(0L)
                    if (overlap <= 0L) null else session to overlap
                }
                .maxByOrNull { it.second }
        OfflineFocusRhythmCell(
            hour = sliceIndex / 12,
            slot = sliceIndex % 12,
            categoryName = dominant?.first?.categoryName,
            iconKey = dominant?.first?.iconKey,
            customIconPath = dominant?.first?.customIconPath,
            color = dominant?.first?.let { Color(it.colorArgb) } ?: Color.White,
            durationMillis = dominant?.second ?: 0L,
        )
    }

private fun buildOfflineFocusRhythmSessionMarks(
    data: OfflineFocusSectionData,
    pauseColor: Color,
): List<OfflineFocusRhythmSessionMark> =
    data.sessions.flatMap { session ->
        val active =
            offlineFocusActiveSegments(session).map { segment ->
                OfflineFocusRhythmSourceSegment(
                    session = session,
                    startMillis = segment.startMillis,
                    endMillis = segment.endMillis,
                    color = Color(session.colorArgb),
                    isPaused = false,
                )
            }
        val pauses =
            session.pauseIntervals.map { pause ->
                OfflineFocusRhythmSourceSegment(
                    session = session,
                    startMillis = pause.startMillis,
                    endMillis = pause.endMillis,
                    color = pauseColor,
                    isPaused = true,
                )
            }
        active + pauses
    }.flatMap { source ->
        val session = source.session
        val startMillis = source.startMillis.coerceIn(data.dayStartMillis, data.dayEndMillis)
        val endMillis = source.endMillis.coerceIn(data.dayStartMillis, data.dayEndMillis)
        if (endMillis <= startMillis) return@flatMap emptyList()
        val firstHour = (((startMillis - data.dayStartMillis) / OfflineFocusRhythmHourMillis).toInt()).coerceIn(0, 23)
        val lastHour = (((endMillis - 1L - data.dayStartMillis) / OfflineFocusRhythmHourMillis).toInt()).coerceIn(0, 23)
        (firstHour..lastHour).mapNotNull { hour ->
            val hourStart = data.dayStartMillis + hour * OfflineFocusRhythmHourMillis
            val segmentStart = maxOf(startMillis, hourStart)
            val segmentEnd = minOf(endMillis, hourStart + OfflineFocusRhythmHourMillis)
            if (segmentEnd <= segmentStart) {
                null
            } else {
                OfflineFocusRhythmSessionMark(
                    hour = hour,
                    firstSlot = (((segmentStart - hourStart) / OFFLINE_FOCUS_RHYTHM_CELL_MILLIS).toInt()).coerceIn(0, 11),
                    lastSlot = (((segmentEnd - 1L - hourStart) / OFFLINE_FOCUS_RHYTHM_CELL_MILLIS).toInt()).coerceIn(0, 11),
                    categoryName = session.categoryName,
                    iconKey = session.iconKey,
                    customIconPath = session.customIconPath,
                    color = source.color,
                    isPaused = source.isPaused,
                )
            }
        }
    }

private fun DrawScope.drawOfflineFocusRhythmBaseGrid(
    cells: List<OfflineFocusRhythmCell>,
    sessionMarks: List<OfflineFocusRhythmSessionMark>,
    cellSize: Float,
    gap: Float,
    emptyColor: Color,
) {
    val corner = 3.dp.toPx()
    cells.forEach { cell ->
        drawRoundRect(
            color = emptyColor,
            topLeft = Offset(
                x = cell.hour * (cellSize + gap),
                y = cell.slot * (cellSize + gap),
            ),
            size = Size(cellSize, cellSize),
            cornerRadius = CornerRadius(corner, corner),
        )
    }
    sessionMarks.forEach { mark ->
        val x = mark.hour * (cellSize + gap)
        val y = mark.firstSlot * (cellSize + gap)
        val height = offlineFocusRhythmMarkHeight(mark, cellSize.roundToInt(), gap.roundToInt())
        drawRoundRect(
            color = mark.color.copy(alpha = if (mark.isPaused) 0.22f else 0.88f),
            topLeft = Offset(x, y),
            size = Size(cellSize, height.toFloat()),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        )
        drawRoundRect(
            color = mark.color.copy(alpha = if (mark.isPaused) 0.18f else 0.32f),
            topLeft = Offset(x, y),
            size = Size(cellSize, height.toFloat()),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
        )
    }
}

private fun offlineFocusRhythmMarkHeight(
    mark: OfflineFocusRhythmSessionMark,
    cellSize: Int,
    gapPx: Int,
): Int {
    val slotCount = (mark.lastSlot - mark.firstSlot + 1).coerceAtLeast(1)
    return cellSize * slotCount + gapPx * (slotCount - 1)
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
private val OfflineFocusRhythmLegendSwatchSize = 20.dp
private const val OfflineFocusRhythmBarIconScale = 0.82f
private const val OfflineFocusRhythmHourMillis = 60L * 60_000L
private const val OFFLINE_FOCUS_RHYTHM_CELL_COUNT = 288
private const val OFFLINE_FOCUS_RHYTHM_CELL_MILLIS = 5L * 60_000L

@Composable
private fun OfflineFocusTimelineBar(data: OfflineFocusSectionData) {
    val pauseColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
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
                val barLeft = left.coerceAtMost(size.width - minWidth)
                val barWidth = width.coerceAtMost(size.width - barLeft)
                val wallDuration = (session.endMillis - session.startMillis).coerceAtLeast(1L)
                if (session.pauseIntervals.isNotEmpty()) {
                    drawRoundRect(
                        color = pauseColor,
                        topLeft = Offset(barLeft, 0f),
                        size = Size(barWidth, size.height),
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                    )
                }
                offlineFocusActiveSegments(session).forEach { segment ->
                    val segmentLeft =
                        barLeft + barWidth * (segment.startMillis - session.startMillis).toFloat() / wallDuration.toFloat()
                    val segmentRight =
                        barLeft + barWidth * (segment.endMillis - session.startMillis).toFloat() / wallDuration.toFloat()
                    if (segmentRight > segmentLeft) {
                        drawRoundRect(
                            color = Color(session.colorArgb),
                            topLeft = Offset(segmentLeft, 0f),
                            size = Size(segmentRight - segmentLeft, size.height),
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                        )
                    }
                }
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
