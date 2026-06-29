package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import com.rrrrz.tinyvow.data.db.OfflineFocusMode
import com.rrrrz.tinyvow.data.repository.OfflineFocusCategory
import com.rrrrz.tinyvow.data.repository.OfflineFocusSession
import com.rrrrz.tinyvow.data.repository.OfflineFocusTodaySummary
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OfflineFocusHomeCard(
    categories: List<OfflineFocusCategory>,
    activeSession: OfflineFocusSession?,
    todaySummary: OfflineFocusTodaySummary,
    defaultCategoryId: String?,
    defaultDurationMinutes: Int,
    defaultMode: OfflineFocusMode,
    isProActive: Boolean,
    onStart: (String, Int, OfflineFocusMode) -> Unit,
    onLocked: () -> Unit,
    onUpsertCategory: (String?, String, String, String?, Int, Double) -> Unit,
    onFinishEarly: (String) -> Unit,
    onAbandon: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStartSheet by remember { mutableStateOf(false) }
    val themeColors = LocalThemeColors.current
    val defaultCategory =
        remember(categories, defaultCategoryId) {
            categories.firstOrNull { it.id == defaultCategoryId } ?: categories.firstOrNull()
        }
    val accent = Color((activeSession?.colorArgb ?: defaultCategory?.colorArgb) ?: 0xFF3F7CAC.toInt())

    TinyVowCard(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (isProActive) {
                            if (activeSession == null) showStartSheet = true
                        } else {
                            onLocked()
                        }
                    },
                    onLongClick = {
                        if (isProActive) {
                            if (activeSession == null) showStartSheet = true
                        } else {
                            onLocked()
                        }
                    },
                ),
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        borderAlpha = 0.30f,
        shadowElevation = TinyVowElevation.FeaturedCard,
    ) {
        if (activeSession == null) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    FocusHomeIcon(modifier = Modifier.size(32.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = AppText.t("offline_focus_title"),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.inkStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = AppText.t("offline_focus_home_subtitle"),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.8.sp, lineHeight = 14.sp),
                            fontWeight = FontWeight.Medium,
                            color = themeColors.inkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                OfflineFocusStartButton(
                    onClick = {
                        if (isProActive) {
                            showStartSheet = true
                        } else {
                            onLocked()
                        }
                    },
                )
            }
        } else {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 12.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        FocusHomeIcon(modifier = Modifier.size(30.dp))
                        Text(
                            text = AppText.t("offline_focus_title"),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.inkStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    OfflineFocusCountdownRing(
                        session = activeSession,
                        color = accent,
                        modifier = Modifier.size(70.dp),
                    )
                }
                OfflineFocusRunningContent(
                    session = activeSession,
                    color = accent,
                    onFinishEarly = { onFinishEarly(activeSession.id) },
                    onAbandon = { onAbandon(activeSession.id) },
                )
            }
        }
    }

    if (showStartSheet) {
        OfflineFocusStartSheet(
            categories = categories,
            defaultCategoryId = defaultCategory?.id,
            defaultDurationMinutes = defaultDurationMinutes,
            defaultMode = defaultMode,
            onUpsertCategory = onUpsertCategory,
            onDismiss = { showStartSheet = false },
            onStart = { categoryId, minutes, mode ->
                showStartSheet = false
                onStart(categoryId, minutes, mode)
            },
        )
    }
}

@Composable
private fun OfflineFocusStartButton(onClick: () -> Unit) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier =
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(11.dp))
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        color = themeColors.base.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, themeColors.base.copy(alpha = 0.20f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = AppText.t("offline_focus_start_short"),
                tint = themeColors.base,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun OfflineFocusCompletionFeedback(summary: OfflineFocusTodaySummary) {
    val latest =
        summary.sessions
            .filter { it.completedAt != null && it.actualDurationMillis > 0L }
            .maxByOrNull { it.completedAt ?: 0L }
            ?: return
    val color = Color(latest.colorArgb)
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
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
                    text = AppText.t("offline_focus_completion_title", latest.pointsAwarded.roundToInt()),
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text =
                        AppText.t(
                            "offline_focus_completion_body",
                            latest.categoryName,
                            (latest.actualDurationMillis / 60_000L).toInt(),
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun OfflineFocusRunningContent(
    session: OfflineFocusSession,
    color: Color,
    onFinishEarly: () -> Unit,
    onAbandon: () -> Unit,
) {
    val now by produceState(initialValue = System.currentTimeMillis(), session.id) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val referenceNow =
        if (session.status == OfflineFocusSessionStatus.PAUSED) {
            session.pausedAt ?: now
        } else {
            now
        }
    val elapsed = (referenceNow - session.startedAt).coerceAtLeast(0L)
    val remaining = (session.plannedDurationMillis - elapsed).coerceAtLeast(0L)
    val plannedMinutes = (session.plannedDurationMillis / 60_000L).toInt()
    val possiblePoints = (plannedMinutes * session.pointsPerMinute).roundToInt()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text =
                if (session.status == OfflineFocusSessionStatus.PAUSED) {
                    AppText.t("offline_focus_paused_format", session.categoryName, formatCountdown(remaining))
                } else {
                    AppText.t("offline_focus_running_format", session.categoryName, formatCountdown(remaining))
                },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = AppText.t("offline_focus_points_after_complete_format", possiblePoints),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onAbandon,
                modifier = Modifier.weight(1f),
            ) {
                Text(AppText.t("offline_focus_abandon"))
            }
            TinyVowButton(
                text = AppText.t("offline_focus_finish_early"),
                onClick = onFinishEarly,
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OfflineFocusTodayStats(
    summary: OfflineFocusTodaySummary,
    accent: Color,
) {
    val totalMinutes = (summary.totalMillis / 60_000L).toInt()
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OfflineFocusMetric(AppText.t("offline_focus_today_minutes"), AppText.t("offline_focus_minutes_format", totalMinutes), accent)
                OfflineFocusMetric(AppText.t("offline_focus_today_sessions"), summary.completedCount.toString(), accent)
                OfflineFocusMetric(AppText.t("offline_focus_today_points"), "+${summary.pointsAwarded.roundToInt()}", accent)
            }
            if (summary.categories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.categories.take(3).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(item.colorArgb)),
                                )
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = AppText.t("offline_focus_minutes_format", (item.totalMillis / 60_000L).toInt()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineFocusMetric(
    label: String,
    value: String,
    accent: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun OfflineFocusStartSheet(
    categories: List<OfflineFocusCategory>,
    defaultCategoryId: String?,
    defaultDurationMinutes: Int,
    defaultMode: OfflineFocusMode,
    onUpsertCategory: (String?, String, String, String?, Int, Double) -> Unit,
    onDismiss: () -> Unit,
    onStart: (String, Int, OfflineFocusMode) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategoryId by remember(defaultCategoryId, categories) {
        mutableStateOf(defaultCategoryId ?: categories.firstOrNull()?.id)
    }
    var selectedDuration by remember(defaultDurationMinutes) { mutableIntStateOf(defaultDurationMinutes) }
    var selectedMode by remember(defaultMode) { mutableStateOf(defaultMode) }
    var showCategoryEditorDialog by remember { mutableStateOf(false) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = TinyVowSpacing.PageHorizontal, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = AppText.t("offline_focus_start_sheet_title"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = AppText.t("offline_focus_category"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OfflineFocusStartButton(onClick = { showCategoryEditorDialog = true })
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { category ->
                        val selected = category.id == selectedCategoryId
                        AssistChip(
                            onClick = { selectedCategoryId = category.id },
                            label = { Text(category.name) },
                            leadingIcon = {
                                FocusTypeIcon(
                                    iconKey = category.iconKey,
                                    customIconPath = category.customIconPath,
                                    color = Color(category.colorArgb),
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            border = BorderStroke(1.dp, if (selected) Color(category.colorArgb) else MaterialTheme.colorScheme.outlineVariant),
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppText.t("offline_focus_duration"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 25, 45, 60).forEach { minutes ->
                        AssistChip(
                            onClick = { selectedDuration = minutes },
                            label = { Text(AppText.t("offline_focus_minutes_format", minutes)) },
                            border = BorderStroke(
                                1.dp,
                                if (selectedDuration == minutes) {
                                    selectedCategory?.colorArgb?.let(::Color) ?: MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppText.t("offline_focus_mode"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OfflineFocusMode.entries.forEach { mode ->
                        AssistChip(
                            onClick = { selectedMode = mode },
                            label = {
                                Text(
                                    when (mode) {
                                        OfflineFocusMode.NORMAL -> AppText.t("offline_focus_mode_normal")
                                        OfflineFocusMode.STRICT -> AppText.t("offline_focus_mode_strict")
                                    },
                                )
                            },
                            border = BorderStroke(
                                1.dp,
                                if (selectedMode == mode) {
                                    selectedCategory?.colorArgb?.let(::Color) ?: MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                        )
                    }
                }
                Text(
                    text =
                        when (selectedMode) {
                            OfflineFocusMode.NORMAL -> AppText.t("offline_focus_mode_normal_desc")
                            OfflineFocusMode.STRICT -> AppText.t("offline_focus_mode_strict_desc")
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TinyVowButton(
                text =
                    selectedCategory?.let {
                        AppText.t("offline_focus_start_button_format", selectedDuration, it.name)
                    } ?: AppText.t("offline_focus_start"),
                onClick = {
                    selectedCategoryId?.let { onStart(it, selectedDuration, selectedMode) }
                },
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
    if (showCategoryEditorDialog) {
        OfflineFocusCategoryEditorDialog(
            category = null,
            onDismiss = { showCategoryEditorDialog = false },
            onSave = { categoryId, name, iconKey, customIconPath, colorArgb, pointsPerMinute ->
                onUpsertCategory(categoryId, name, iconKey, customIconPath, colorArgb, pointsPerMinute)
                showCategoryEditorDialog = false
            },
            onImportIcon = {},
            onDelete = {},
        )
    }
}

@Composable
private fun OfflineFocusCountdownRing(
    session: OfflineFocusSession,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val now by produceState(initialValue = System.currentTimeMillis(), session.id) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val elapsed = (now - session.startedAt).coerceAtLeast(0L)
    val progress =
        if (session.plannedDurationMillis <= 0L) {
            0f
        } else {
            (elapsed.toFloat() / session.plannedDurationMillis.toFloat()).coerceIn(0f, 1f)
        }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(70.dp)) {
            val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = color.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = stroke,
            )
        }
        Text(
            text = "${(progress * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

private fun formatCountdown(millis: Long): String {
    val seconds = ((millis + 999L) / 1000L).coerceAtLeast(0L)
    val minutes = seconds / 60L
    val remainSeconds = seconds % 60L
    return "%02d:%02d".format(minutes, remainSeconds)
}
