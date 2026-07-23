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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import com.rrrrz.tinyvow.data.db.OfflineFocusMode
import com.rrrrz.tinyvow.data.db.OfflineFocusPauseReason
import com.rrrrz.tinyvow.data.repository.OfflineFocusCategory
import com.rrrrz.tinyvow.data.repository.OfflineFocusSession
import com.rrrrz.tinyvow.data.repository.OfflineFocusTodaySummary
import com.rrrrz.tinyvow.data.repository.elapsedDurationMillisAt
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.settings.OfflineFocusCategoryDefaults
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowEmptyState
import com.rrrrz.tinyvow.ui.theme.TinyVowIconSurface
import com.rrrrz.tinyvow.ui.theme.TinyVowMetricTile
import com.rrrrz.tinyvow.ui.theme.TinyVowPageBackground
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import com.rrrrz.tinyvow.ui.theme.TinyVowStatusPill
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OfflineFocusHomeCard(
    categories: List<OfflineFocusCategory>,
    activeSession: OfflineFocusSession?,
    detailRequestToken: Int,
    todaySummary: OfflineFocusTodaySummary,
    dailyTargetMinutes: Int,
    defaultCategoryId: String?,
    defaultDurationMinutes: Int,
    defaultMode: OfflineFocusMode,
    restReminderEnabled: Boolean,
    restReminderMinutes: Int,
    categoryDefaults: Map<String, OfflineFocusCategoryDefaults>,
    focusRulesAvailable: Boolean,
    isProActive: Boolean,
    onStart: (String, Int, OfflineFocusMode) -> Unit,
    onSetRestReminderEnabled: (Boolean) -> Unit,
    onSetRestReminderMinutes: (Int) -> Unit,
    onLocked: () -> Unit,
    onOpenFocusRulesSettings: () -> Unit,
    onUpsertCategory: (String?, String, String, String?, Int, Double) -> Unit,
    onFinishEarly: (String) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onAbandon: (String) -> Unit,
    onAllowViolationPackage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStartSheet by remember { mutableStateOf(false) }
    var showActiveDetailSheet by remember { mutableStateOf(false) }
    var showTodayDetailSheet by remember { mutableStateOf(false) }
    var pendingFinishSessionId by remember { mutableStateOf<String?>(null) }
    var pendingAbandonSessionId by remember { mutableStateOf<String?>(null) }
    var handledExternalRequestToken by rememberSaveable { mutableIntStateOf(0) }
    val themeColors = LocalThemeColors.current
    val defaultCategory =
        remember(categories, defaultCategoryId) {
            categories.firstOrNull { it.id == defaultCategoryId } ?: categories.firstOrNull()
        }
    fun startWithCurrentDefault() {
        val category = defaultCategory ?: return
        val defaults = categoryDefaults[category.id]
        val mode =
            if (!focusRulesAvailable) {
                OfflineFocusMode.NORMAL
            } else {
                defaults?.mode ?: defaultMode
        }
        showActiveDetailSheet = true
        onStart(category.id, defaults?.durationMinutes ?: defaultDurationMinutes, mode)
    }
    val accent = Color((activeSession?.colorArgb ?: defaultCategory?.colorArgb) ?: 0xFF3F7CAC.toInt())
    LaunchedEffect(detailRequestToken, activeSession?.id) {
        if (detailRequestToken > handledExternalRequestToken) {
            handledExternalRequestToken = detailRequestToken
            showTodayDetailSheet = false
            if (activeSession == null) {
                showStartSheet = true
            } else {
                showActiveDetailSheet = true
            }
        }
        if (activeSession != null) {
            showTodayDetailSheet = false
        }
    }

    TinyVowCard(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (isProActive) {
                            if (activeSession == null) {
                                showTodayDetailSheet = true
                            } else {
                                showActiveDetailSheet = true
                            }
                        } else {
                            onLocked()
                        }
                    },
                    onLongClick = {
                        if (isProActive) {
                            if (activeSession == null) startWithCurrentDefault()
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
                    OfflineFocusRunningProgressIcon(
                        session = activeSession,
                        color = accent,
                        modifier = Modifier.size(32.dp),
                    )
                    OfflineFocusRunningSummary(
                        session = activeSession,
                        modifier = Modifier.weight(1f),
                    )
                }
                OfflineFocusActionButton(
                    text = AppText.t("offline_focus_details"),
                    onClick = { showActiveDetailSheet = true },
                )
            }
        }
    }

    if (showActiveDetailSheet) {
        activeSession?.let { session ->
            OfflineFocusActiveDetailSheet(
                session = session,
                onDismiss = { showActiveDetailSheet = false },
                onPause = { onPause(session.id) },
                onResume = { onResume(session.id) },
                onFinish = {
                    showActiveDetailSheet = false
                    pendingFinishSessionId = session.id
                },
                onAbandon = {
                    showActiveDetailSheet = false
                    pendingAbandonSessionId = session.id
                },
                onAllowViolationPackage = { packageName ->
                    showActiveDetailSheet = false
                    onAllowViolationPackage(packageName)
                },
            )
        }
    }

    if (showTodayDetailSheet && activeSession == null) {
        OfflineFocusTodayDetailSheet(
            summary = todaySummary,
            dailyTargetMinutes = dailyTargetMinutes,
            onDismiss = { showTodayDetailSheet = false },
            onStartFocus = {
                showTodayDetailSheet = false
                showStartSheet = true
            },
        )
    }

    if (showStartSheet) {
        OfflineFocusStartSheet(
            categories = categories,
            defaultCategoryId = defaultCategory?.id,
            defaultDurationMinutes = defaultDurationMinutes,
            defaultMode = defaultMode,
            restReminderEnabled = restReminderEnabled,
            restReminderMinutes = restReminderMinutes,
            categoryDefaults = categoryDefaults,
            focusRulesAvailable = focusRulesAvailable,
            onUpsertCategory = onUpsertCategory,
            onOpenFocusRulesSettings = onOpenFocusRulesSettings,
            onDismiss = { showStartSheet = false },
            onSetRestReminderEnabled = onSetRestReminderEnabled,
            onSetRestReminderMinutes = onSetRestReminderMinutes,
            onStart = { categoryId, minutes, mode ->
                showStartSheet = false
                showActiveDetailSheet = true
                onStart(categoryId, minutes, mode)
            },
        )
    }

    pendingFinishSessionId?.let { sessionId ->
        AlertDialog(
            onDismissRequest = { pendingFinishSessionId = null },
            title = { Text(AppText.t("offline_focus_finish_confirm_title")) },
            text = { Text(AppText.t("offline_focus_finish_confirm_body")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingFinishSessionId = null
                        onFinishEarly(sessionId)
                    },
                ) {
                    Text(AppText.t("offline_focus_finish_early"))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingFinishSessionId = null }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }
    pendingAbandonSessionId?.let { sessionId ->
        AlertDialog(
            onDismissRequest = { pendingAbandonSessionId = null },
            title = { Text(AppText.t("offline_focus_abandon_confirm_title")) },
            text = { Text(AppText.t("offline_focus_abandon_confirm_body")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingAbandonSessionId = null
                        onAbandon(sessionId)
                    },
                ) {
                    Text(AppText.t("offline_focus_abandon"))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAbandonSessionId = null }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfflineFocusTodayDetailSheet(
    summary: OfflineFocusTodaySummary,
    dailyTargetMinutes: Int,
    onDismiss: () -> Unit,
    onStartFocus: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accent = themeColors.base
    val totalMinutes = (summary.totalMillis / 60_000L).toInt()
    val targetMinutes = dailyTargetMinutes.coerceAtLeast(1)
    val progress = (summary.totalMillis.toFloat() / (targetMinutes * 60_000L).toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progress * 100f).roundToInt()
    val completedSessions =
        remember(summary.sessions) {
            summary.sessions
                .filter {
                    (it.status == OfflineFocusSessionStatus.COMPLETED ||
                        it.status == OfflineFocusSessionStatus.SETTLED) &&
                        it.actualDurationMillis > 0L
                }
                .sortedByDescending { it.completedAt ?: it.startedAt }
                .take(3)
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = TinyVowRadius.FeaturedCard,
            topEnd = TinyVowRadius.FeaturedCard,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(
                        start = TinyVowSpacing.PageHorizontal,
                        end = TinyVowSpacing.PageHorizontal,
                        bottom = 24.dp,
                    ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TinyVowIconSurface(
                    icon = Icons.Default.Timer,
                    contentDescription = null,
                    size = 42.dp,
                    iconSize = 22.dp,
                    containerColor = themeColors.baseContainer.copy(alpha = 0.84f),
                    contentColor = accent,
                )
                Text(
                    text = AppText.t("offline_focus_today_detail_title"),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.inkStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TinyVowStatusPill(
                    text = AppText.t("offline_focus_today_target_progress", progressPercent),
                    color = accent,
                    leadingDot = false,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = AppText.t("group_close"),
                        tint = themeColors.inkMuted,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = AppText.t("offline_focus_today_minutes_value", totalMinutes),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.inkStrong,
                )
                Text(
                    text = AppText.t("offline_focus_today_target_value", targetMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.inkMuted,
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TinyVowMetricTile(
                    label = AppText.t("offline_focus_today_summary_minutes"),
                    value = AppText.t("offline_focus_today_minutes_value", totalMinutes),
                    color = accent,
                    compact = true,
                    modifier = Modifier.weight(1f),
                )
                TinyVowMetricTile(
                    label = AppText.t("offline_focus_today_summary_sessions"),
                    value = AppText.t("offline_focus_today_sessions_value", summary.completedCount),
                    color = accent,
                    compact = true,
                    modifier = Modifier.weight(1f),
                )
                TinyVowMetricTile(
                    label = AppText.t("offline_focus_today_summary_points"),
                    value = AppText.t("offline_focus_today_points_value", summary.pointsAwarded.roundToInt()),
                    color = accent,
                    compact = true,
                    modifier = Modifier.weight(1f),
                )
            }

            if (summary.completedCount == 0) {
                TinyVowEmptyState(
                    title = AppText.t("offline_focus_today_empty_title"),
                    body = AppText.t("offline_focus_today_empty_body"),
                    icon = Icons.Default.Timer,
                )
            } else {
                if (summary.categories.isNotEmpty()) {
                    Text(
                        text = AppText.t("offline_focus_today_categories_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.inkStrong,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        summary.categories.take(3).forEach { category ->
                            OfflineFocusTodayCategoryRow(
                                category = category,
                                totalMillis = summary.totalMillis,
                            )
                        }
                    }
                }

                if (completedSessions.isNotEmpty()) {
                    Text(
                        text = AppText.t("offline_focus_today_recent_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.inkStrong,
                    )
                    Column {
                        completedSessions.forEachIndexed { index, session ->
                            OfflineFocusTodaySessionRow(session = session)
                            if (index < completedSessions.lastIndex) {
                                HorizontalDivider(color = themeColors.dividerSoft)
                            }
                        }
                    }
                }
            }

            TinyVowButton(
                text = AppText.t("offline_focus_start"),
                onClick = onStartFocus,
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OfflineFocusTodayCategoryRow(
    category: com.rrrrz.tinyvow.data.repository.OfflineFocusCategorySummary,
    totalMillis: Long,
) {
    val themeColors = LocalThemeColors.current
    val color = Color(category.colorArgb)
    val progress = category.totalMillis.toFloat() / totalMillis.coerceAtLeast(1L).toFloat()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FocusTypeIcon(
            iconKey = category.iconKey,
            customIconPath = category.customIconPath,
            color = color,
            modifier = Modifier.size(36.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.12f),
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = AppText.t("offline_focus_today_minutes_value", (category.totalMillis / 60_000L).toInt()),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.ink,
            )
            Text(
                text = AppText.t("offline_focus_today_sessions_value", category.completedCount),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
        }
    }
}

@Composable
private fun OfflineFocusTodaySessionRow(session: OfflineFocusSession) {
    val themeColors = LocalThemeColors.current
    val accent = Color(session.colorArgb)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = offlineFocusTodaySessionTime(session.startedAt),
            modifier = Modifier.widthIn(min = 44.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.ink,
        )
        Text(
            text = session.categoryName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inkStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = AppText.t("offline_focus_today_minutes_value", (session.actualDurationMillis / 60_000L).toInt()),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
        )
        Text(
            text =
                AppText.t(
                    if (session.focusMode == OfflineFocusMode.STRICT) {
                        "offline_focus_mode_strict"
                    } else {
                        "offline_focus_mode_normal"
                    },
                ),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
        )
        Text(
            text = AppText.t("offline_focus_today_points_value", session.pointsAwarded.roundToInt()),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )
    }
}

private fun offlineFocusTodaySessionTime(startedAt: Long): String =
    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(startedAt))

@Composable
private fun OfflineFocusRunningProgressIcon(
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
    val elapsed = session.elapsedDurationMillisAt(now)
    val progress =
        if (session.plannedDurationMillis <= 0L) {
            0f
        } else {
            (elapsed.toFloat() / session.plannedDurationMillis.toFloat()).coerceIn(0f, 1f)
        }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        FocusTypeIcon(
            iconKey = session.iconKey,
            customIconPath = session.customIconPath,
            color = color,
            modifier =
                Modifier
                    .fillMaxSize(0.58f)
                    .alpha(0.62f),
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = color.copy(alpha = 0.20f),
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
    }
}

@Composable
private fun OfflineFocusRunningSummary(
    session: OfflineFocusSession,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val now by produceState(initialValue = System.currentTimeMillis(), session.id) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val elapsed = session.elapsedDurationMillisAt(now)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = session.categoryName,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inkStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text =
                if (session.plannedDurationMillis <= 0L) {
                    AppText.t("offline_focus_elapsed_unlimited_format", formatCountdown(elapsed))
                } else {
                    AppText.t(
                        "offline_focus_elapsed_total_format",
                        formatCountdown(elapsed),
                        formatCountdown(session.plannedDurationMillis),
                    )
                },
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.8.sp, lineHeight = 14.sp),
            fontWeight = FontWeight.Medium,
            color = themeColors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OfflineFocusActiveDetailSheet(
    session: OfflineFocusSession,
    onDismiss: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    onAbandon: () -> Unit,
    onAllowViolationPackage: (String) -> Unit,
) {
    val view = LocalView.current
    val now by produceState(initialValue = System.currentTimeMillis(), session.id) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    DisposableEffect(Unit) {
        val previous = view.keepScreenOn
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = previous
        }
    }
    val isPaused = session.status == OfflineFocusSessionStatus.PAUSED
    val elapsed = session.elapsedDurationMillisAt(now)
    val remaining =
        if (session.plannedDurationMillis <= 0L) null else (session.plannedDurationMillis - elapsed).coerceAtLeast(0L)
    val accent = Color(session.colorArgb)
    val progress =
        if (session.plannedDurationMillis <= 0L) {
            0f
        } else {
            (elapsed.toFloat() / session.plannedDurationMillis.toFloat()).coerceIn(0f, 1f)
        }
    val modeLabel =
        when (session.focusMode) {
            OfflineFocusMode.NORMAL -> AppText.t("offline_focus_mode_normal")
            OfflineFocusMode.STRICT -> AppText.t("offline_focus_mode_strict")
        }
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        TinyVowPageBackground(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = TinyVowSpacing.PageHorizontal, vertical = 10.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = AppText.t("offline_focus_active_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppText.t("group_cancel"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = 54.dp, bottom = 164.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Column(
                        modifier = Modifier.offset(y = (-20).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FocusTypeIcon(
                            iconKey = session.iconKey,
                            customIconPath = session.customIconPath,
                            color = accent,
                            modifier = Modifier.size(42.dp),
                        )
                        Text(
                            text = session.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OfflineFocusLargeProgressDial(
                        color = accent,
                        progress = progress,
                        centerText = formatCountdown(elapsed),
                        modifier = Modifier.size(224.dp),
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .offset(y = 15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TinyVowMetricTile(
                                label = AppText.t("offline_focus_active_elapsed"),
                                value = formatCountdown(elapsed),
                                color = accent,
                                compact = true,
                                modifier = Modifier.weight(1f),
                            )
                            TinyVowMetricTile(
                                label = AppText.t("offline_focus_active_remaining"),
                                value = remaining?.let(::formatCountdown) ?: AppText.t("offline_focus_unlimited"),
                                color = accent,
                                compact = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TinyVowMetricTile(
                                label = AppText.t("offline_focus_completed_metric_mode"),
                                value = modeLabel,
                                color = MaterialTheme.colorScheme.primary,
                                compact = true,
                                modifier = Modifier.weight(1f),
                            )
                            TinyVowMetricTile(
                                label = AppText.t("offline_focus_completed_metric_duration"),
                                value =
                                    if (session.plannedDurationMillis <= 0L) {
                                        AppText.t("offline_focus_unlimited")
                                    } else {
                                        formatCountdown(session.plannedDurationMillis)
                                    },
                                color = accent,
                                compact = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                session.violationPackageName
                    ?.takeIf {
                        isPaused &&
                            session.pauseReason == OfflineFocusPauseReason.NON_WHITELIST_APP
                    }
                    ?.let { packageName ->
                        Surface(
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 172.dp)
                                    .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.26f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = AppText.t("offline_focus_paused_by_app", packageName),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TinyVowButton(
                                    text = AppText.t("offline_focus_allow_once_short"),
                                    onClick = { onAllowViolationPackage(packageName) },
                                    tone = TinyVowButtonTone.Primary,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                Column(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TinyVowButton(
                        text = AppText.t(if (isPaused) "offline_focus_resume" else "offline_focus_pause"),
                        onClick = if (isPaused) onResume else onPause,
                        tone = TinyVowButtonTone.Primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TinyVowButton(
                            text = AppText.t("offline_focus_end"),
                            onClick = onFinish,
                            modifier = Modifier.weight(1f),
                        )
                        TinyVowButton(
                            text = AppText.t("offline_focus_abandon_short"),
                            onClick = onAbandon,
                            tone = TinyVowButtonTone.Danger,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
    }
}

@Composable
private fun OfflineFocusLargeProgressDial(
    color: Color,
    progress: Float,
    centerText: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = color.copy(alpha = 0.12f),
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
            text = centerText,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfflineFocusRunningActions(
    session: OfflineFocusSession,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    onAbandon: () -> Unit,
    onAllowViolationPackage: (String) -> Unit,
) {
    val isPaused = session.status == OfflineFocusSessionStatus.PAUSED
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        session.violationPackageName
            ?.takeIf {
                isPaused &&
                    session.pauseReason == OfflineFocusPauseReason.NON_WHITELIST_APP
            }
            ?.let { packageName ->
                OfflineFocusActionButton(
                    text = AppText.t("offline_focus_allow_once_short"),
                    onClick = { onAllowViolationPackage(packageName) },
                )
            }
        OfflineFocusActionButton(
            text = AppText.t(if (isPaused) "offline_focus_resume" else "offline_focus_pause"),
            onClick = if (isPaused) onResume else onPause,
        )
        OfflineFocusActionButton(
            text = AppText.t("offline_focus_end"),
            onClick = onFinish,
        )
        OfflineFocusActionButton(
            text = AppText.t("offline_focus_abandon_short"),
            onClick = onAbandon,
        )
    }
}

@Composable
private fun OfflineFocusActionButton(
    text: String,
    onClick: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier =
            Modifier
                .height(30.dp)
                .widthIn(min = 42.dp, max = 88.dp)
                .clip(RoundedCornerShape(11.dp))
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        color = themeColors.base.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, themeColors.base.copy(alpha = 0.20f)),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 13.sp),
                fontWeight = FontWeight.SemiBold,
                color = themeColors.base,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
    restReminderEnabled: Boolean,
    restReminderMinutes: Int,
    categoryDefaults: Map<String, OfflineFocusCategoryDefaults>,
    focusRulesAvailable: Boolean,
    onUpsertCategory: (String?, String, String, String?, Int, Double) -> Unit,
    onOpenFocusRulesSettings: () -> Unit,
    onDismiss: () -> Unit,
    onSetRestReminderEnabled: (Boolean) -> Unit,
    onSetRestReminderMinutes: (Int) -> Unit,
    onStart: (String, Int, OfflineFocusMode) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialCategoryId = remember(defaultCategoryId, categories) {
        defaultCategoryId ?: categories.firstOrNull()?.id
    }
    val initialDefaults = initialCategoryId?.let { categoryDefaults[it] }
    var selectedCategoryId by remember(initialCategoryId) { mutableStateOf(initialCategoryId) }
    var selectedDuration by remember(initialCategoryId, initialDefaults, defaultDurationMinutes) {
        mutableIntStateOf(initialDefaults?.durationMinutes ?: defaultDurationMinutes)
    }
    var durationDraft by remember(initialCategoryId, initialDefaults, defaultDurationMinutes) {
        mutableStateOf((initialDefaults?.durationMinutes ?: defaultDurationMinutes).toString())
    }
    var selectedMode by remember(initialCategoryId, initialDefaults, defaultMode, focusRulesAvailable) {
        mutableStateOf(
            if (!focusRulesAvailable) {
                OfflineFocusMode.NORMAL
            } else {
                initialDefaults?.mode ?: defaultMode
            },
        )
    }
    var showCategoryEditorDialog by remember { mutableStateOf(false) }
    var pendingStrictStart by remember { mutableStateOf<Triple<String, Int, OfflineFocusMode>?>(null) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    fun selectCategory(categoryId: String) {
        selectedCategoryId = categoryId
        val defaults = categoryDefaults[categoryId]
        val nextDuration = defaults?.durationMinutes ?: defaultDurationMinutes
        selectedDuration = nextDuration
        durationDraft = nextDuration.toString()
        selectedMode =
            if (!focusRulesAvailable) {
                OfflineFocusMode.NORMAL
            } else {
                defaults?.mode ?: defaultMode
            }
    }
    val startDuration =
        if (selectedDuration == ManagedAppPreferences.UNLIMITED_OFFLINE_FOCUS_DURATION_MINUTES) {
            ManagedAppPreferences.UNLIMITED_OFFLINE_FOCUS_DURATION_MINUTES
        } else {
            durationDraft.toIntOrNull()
                ?.coerceIn(
                    ManagedAppPreferences.MIN_OFFLINE_FOCUS_DURATION_MINUTES,
                    ManagedAppPreferences.MAX_OFFLINE_FOCUS_DURATION_MINUTES,
                )
        }
    val canStart = selectedCategoryId != null && startDuration != null
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
                    if (categories.isEmpty()) {
                        Text(
                            text = AppText.t("offline_focus_category_empty_start_hint"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        categories.forEach { category ->
                            val selected = category.id == selectedCategoryId
                            AssistChip(
                                onClick = { selectCategory(category.id) },
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
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppText.t("offline_focus_duration"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(
                        15,
                        25,
                        45,
                        60,
                        ManagedAppPreferences.UNLIMITED_OFFLINE_FOCUS_DURATION_MINUTES,
                    ).forEach { minutes ->
                        AssistChip(
                            onClick = {
                                selectedDuration = minutes
                                durationDraft =
                                    if (minutes == ManagedAppPreferences.UNLIMITED_OFFLINE_FOCUS_DURATION_MINUTES) {
                                        ""
                                    } else {
                                        minutes.toString()
                                    }
                            },
                            label = {
                                Text(
                                    text =
                                        if (minutes == ManagedAppPreferences.UNLIMITED_OFFLINE_FOCUS_DURATION_MINUTES) {
                                            AppText.t("offline_focus_unlimited")
                                        } else {
                                            AppText.t("offline_focus_minutes_short_format", minutes)
                                        },
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                )
                            },
                            border = BorderStroke(
                                1.dp,
                                if (selectedDuration == minutes) {
                                    selectedCategory?.colorArgb?.let(::Color) ?: MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                OutlinedTextField(
                    value = durationDraft,
                    onValueChange = { value ->
                        durationDraft = value.filter { it.isDigit() }.take(3)
                        durationDraft.toIntOrNull()?.let { selectedDuration = it }
                    },
                    label = { Text(AppText.t("offline_focus_duration_custom")) },
                    supportingText = {
                        Text(
                            AppText.t(
                                "offline_focus_duration_custom_desc",
                                ManagedAppPreferences.MIN_OFFLINE_FOCUS_DURATION_MINUTES,
                                ManagedAppPreferences.MAX_OFFLINE_FOCUS_DURATION_MINUTES,
                            ),
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppText.t("offline_focus_rest_reminder_minutes"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(5, 10, ManagedAppPreferences.UNLIMITED_OFFLINE_FOCUS_DURATION_MINUTES).forEach { minutes ->
                        val unlimited = minutes == ManagedAppPreferences.UNLIMITED_OFFLINE_FOCUS_DURATION_MINUTES
                        val selected =
                            if (unlimited) {
                                !restReminderEnabled
                            } else {
                                restReminderEnabled && restReminderMinutes == minutes
                            }
                        AssistChip(
                            onClick = {
                                if (unlimited) {
                                    onSetRestReminderEnabled(false)
                                } else {
                                    onSetRestReminderEnabled(true)
                                    onSetRestReminderMinutes(minutes)
                                }
                            },
                            label = {
                                Text(
                                    text =
                                        if (unlimited) {
                                            AppText.t("offline_focus_unlimited")
                                        } else {
                                            AppText.t("offline_focus_minutes_short_format", minutes)
                                        },
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                )
                            },
                            border = BorderStroke(
                                1.dp,
                                if (selected) {
                                    selectedCategory?.colorArgb?.let(::Color) ?: MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(2) { Spacer(modifier = Modifier.weight(1f)) }
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
                            enabled = focusRulesAvailable || mode != OfflineFocusMode.STRICT,
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
                if (!focusRulesAvailable) {
                    TinyVowCard(
                        shape = RoundedCornerShape(TinyVowRadius.Card),
                        borderAlpha = 0.28f,
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = AppText.t("offline_focus_accessibility_warning_title"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = AppText.t("offline_focus_accessibility_warning_body"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TinyVowButton(
                                text = AppText.t("offline_focus_accessibility_warning_action"),
                                onClick = onOpenFocusRulesSettings,
                                tone = TinyVowButtonTone.Neutral,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            TinyVowButton(
                text =
                    selectedCategory?.let {
                        if (startDuration == ManagedAppPreferences.UNLIMITED_OFFLINE_FOCUS_DURATION_MINUTES) {
                            AppText.t("offline_focus_start_button_unlimited_format", it.name)
                        } else {
                            AppText.t("offline_focus_start_button_format", startDuration ?: selectedDuration, it.name)
                        }
                    } ?: AppText.t("offline_focus_start"),
                onClick = {
                    val duration = startDuration ?: return@TinyVowButton
                    selectedCategoryId?.let { categoryId ->
                        if (selectedMode == OfflineFocusMode.STRICT) {
                            pendingStrictStart = Triple(categoryId, duration, selectedMode)
                        } else {
                            onStart(categoryId, duration, selectedMode)
                        }
                    }
                },
                tone = TinyVowButtonTone.Primary,
                enabled = canStart,
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
    pendingStrictStart?.let { pending ->
        AlertDialog(
            onDismissRequest = { pendingStrictStart = null },
            title = { Text(AppText.t("offline_focus_strict_confirm_title")) },
            text = { Text(AppText.t("offline_focus_strict_confirm_body")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingStrictStart = null
                        onStart(pending.first, pending.second, pending.third)
                    },
                ) {
                    Text(AppText.t("offline_focus_start"))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingStrictStart = null }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }
}

private fun formatCountdown(millis: Long): String {
    val seconds = ((millis + 999L) / 1000L).coerceAtLeast(0L)
    val minutes = seconds / 60L
    val remainSeconds = seconds % 60L
    return "%02d:%02d".format(minutes, remainSeconds)
}
