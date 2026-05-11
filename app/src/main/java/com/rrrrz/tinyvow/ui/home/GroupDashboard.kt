package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.supermode.GuardedAction
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

private val DialogHorizontalPadding = 28.dp
private val CompactFieldHeight = 50.dp
private val CompactFieldShape = RoundedCornerShape(16.dp)

@Composable
fun GroupDashboard(
    groupsWithApps: List<AppGroupWithApps>,
    usageMap: Map<String, Long>,
    isLoadingApps: Boolean,
    installedApps: List<ManagedApp>,
    onSaveGroup: (
        id: String?,
        name: String,
        limit: Int,
        type: GroupType,
        period: LimitPeriod,
        pts: Double,
        pkgs: List<String>
    ) -> Unit,
    onDeleteGroup: (id: String) -> Unit,
    onReorderGroups: (GroupType, List<String>) -> Unit,
    archiveRepository: DailyArchiveRepository?,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onGuardAction: (GuardedAction, () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    var showDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<AppGroupWithApps?>(null) }
    var detailGroup by remember { mutableStateOf<AppGroupWithApps?>(null) }
    var sortingType by remember { mutableStateOf<GroupType?>(null) }
    var forcedType by remember { mutableStateOf(GroupType.CONTROL) }

    val controlGroups = remember(groupsWithApps) {
        groupsWithApps.filter { it.group.type == GroupType.CONTROL }
    }
    val encourageGroups = remember(groupsWithApps) {
        groupsWithApps.filter { it.group.type == GroupType.ENCOURAGE }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            SectionCard(
                title = AppText.t("group_commitment"),
                subtitle = AppText.t("group_limit_type"),
                groups = controlGroups,
                usageMap = usageMap,
                accent = themeColors.control,
                onAdd = {
                    if (ProFeatureGate.canAddGroup(isProActive, GroupType.CONTROL, controlGroups.size)) {
                        editingGroup = null
                        forcedType = GroupType.CONTROL
                        showDialog = true
                    } else {
                        onShowProUpsell(ProUpsellSource.GROUP_LIMIT)
                    }
                },
                onSort = { sortingType = GroupType.CONTROL },
                onOpen = { detailGroup = it },
                onEdit = {
                    val index = controlGroups.indexOfFirst { group -> group.group.id == it.group.id }
                    if (ProFeatureGate.canEditGroup(isProActive, index)) {
                        onGuardAction(GuardedAction.EDIT_GROUP) {
                            editingGroup = it
                            forcedType = it.group.type
                            showDialog = true
                        }
                    } else {
                        onShowProUpsell(ProUpsellSource.GROUP_LIMIT)
                    }
                },
                modifier = Modifier.weight(1f),
            )

            SectionCard(
                title = AppText.t("group_small_encouragement"),
                subtitle = AppText.t("group_points_target"),
                groups = encourageGroups,
                usageMap = usageMap,
                accent = themeColors.encourage,
                onAdd = {
                    if (ProFeatureGate.canAddGroup(isProActive, GroupType.ENCOURAGE, encourageGroups.size)) {
                        editingGroup = null
                        forcedType = GroupType.ENCOURAGE
                        showDialog = true
                    } else {
                        onShowProUpsell(ProUpsellSource.GROUP_LIMIT)
                    }
                },
                onSort = { sortingType = GroupType.ENCOURAGE },
                onOpen = { detailGroup = it },
                onEdit = {
                    val index = encourageGroups.indexOfFirst { group -> group.group.id == it.group.id }
                    if (ProFeatureGate.canEditGroup(isProActive, index)) {
                        onGuardAction(GuardedAction.EDIT_GROUP) {
                            editingGroup = it
                            forcedType = it.group.type
                            showDialog = true
                        }
                    } else {
                        onShowProUpsell(ProUpsellSource.GROUP_LIMIT)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }

        if (isLoadingApps) {
            Text(
                text = AppText.t("group_loading_app_list"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showDialog) {
        GroupEditDialog(
            group = editingGroup,
            forcedType = forcedType,
            installedApps = installedApps,
            isProActive = isProActive,
            onShowProUpsell = onShowProUpsell,
            onDismiss = { showDialog = false },
            onSave = { name, limit, type, period, points, packages ->
                val saveAction = editingGroup?.let { GuardedAction.EDIT_GROUP }
                val saveBlock = {
                    onSaveGroup(editingGroup?.group?.id, name, limit, type, period, points, packages)
                    showDialog = false
                }
                if (saveAction == null) {
                    saveBlock()
                } else {
                    onGuardAction(saveAction, saveBlock)
                }
            },
            onDelete = {
                onGuardAction(GuardedAction.DELETE_GROUP) {
                    editingGroup?.group?.id?.let(onDeleteGroup)
                    showDialog = false
                }
            }
        )
    }

    sortingType?.let { type ->
        GroupSortDialog(
            title = if (type == GroupType.CONTROL) AppText.t("group_commitmentsort") else AppText.t("group_encourage_sort_title"),
            groups = if (type == GroupType.CONTROL) controlGroups else encourageGroups,
            onDismiss = { sortingType = null },
            onSave = { orderedIds ->
                onReorderGroups(type, orderedIds)
                sortingType = null
            },
        )
    }

    detailGroup?.let { group ->
        GroupDetailDialog(
            groupData = group,
            todayUsageMillis = usageMap[group.group.id] ?: 0L,
            archiveRepository = archiveRepository,
            onDismiss = { detailGroup = null },
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    groups: List<AppGroupWithApps>,
    usageMap: Map<String, Long>,
    accent: Color,
    onAdd: () -> Unit,
    onSort: () -> Unit,
    onOpen: (AppGroupWithApps) -> Unit,
    onEdit: (AppGroupWithApps) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(
                    onClick = onSort,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = AppText.t("group_sort"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = AppText.t("group_add_group"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (groups.isEmpty()) {
                Text(
                    text = AppText.t("group_no_groups_yet"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )
            } else {
                groups.forEachIndexed { index, item ->
                    GroupCard(
                        groupData = item,
                        usedMinutes = ((usageMap[item.group.id] ?: 0L) / 60_000L).toInt(),
                        accent = accent,
                        onClick = { onOpen(item) },
                        onLongClick = { onEdit(item) },
                    )
                    if (index < groups.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupCard(
    groupData: AppGroupWithApps,
    usedMinutes: Int,
    accent: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val themeColors = LocalThemeColors.current
    val periodLabel = when (groupData.group.limitPeriod) {
        LimitPeriod.DAILY -> AppText.t("group_daily")
        LimitPeriod.WEEKLY -> AppText.t("group_weekly")
        LimitPeriod.MONTHLY -> AppText.t("group_monthly")
    }
    val detailText = if (groupData.group.type == GroupType.ENCOURAGE) {
        AppText.t("group_value_value_value_min_value_pts_min", periodLabel, usedMinutes, groupData.group.limitMinutes, trimTrailingZero(groupData.group.pointsPerMinute))
    } else {
        AppText.t("group_value_value_value_min", periodLabel, usedMinutes, groupData.group.limitMinutes)
    }
    val rawProgress = if (groupData.group.limitMinutes > 0) {
        usedMinutes.toFloat() / groupData.group.limitMinutes.toFloat()
    } else {
        0f
    }
    val progress = rawProgress.coerceIn(0f, 1f)
    val progressColor =
        when {
            groupData.group.type == GroupType.CONTROL && rawProgress >= 1f -> themeColors.control
            groupData.group.type == GroupType.ENCOURAGE -> themeColors.encourage
            else -> accent
        }
    val trackColor = when (groupData.group.type) {
        GroupType.ENCOURAGE -> themeColors.encourage.copy(alpha = 0.14f)
        else -> themeColors.control.copy(alpha = 0.14f)
    }
    val iconPackages = groupData.packageNames.take(3)
    val iconSize = 26
    val iconOffset = 12
    val maxIcons = 3
    val iconWidth = iconSize + (maxIcons - 1) * iconOffset

    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progress,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessVeryLow
        ),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(modifier = Modifier.width(iconWidth.dp), contentAlignment = Alignment.CenterStart) {
                iconPackages.forEachIndexed { index, packageName ->
                    // 异步加载图标，避免同步 Binder IPC 阻塞主线程
                    var icon by remember(packageName) { mutableStateOf<android.graphics.drawable.Drawable?>(null) }
                    LaunchedEffect(packageName) {
                        icon = withContext(Dispatchers.IO) {
                            runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .padding(start = (index * iconOffset).dp)
                            .size(iconSize.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                        shadowElevation = 2.dp
                    ) {
                        if (icon != null) {
                            AsyncImage(
                                model = icon,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .graphicsLayer {
                                        scaleX = 1.08f
                                        scaleY = 1.08f
                                    }
                            )
                        }
                    }
                }
            }

            Text(
                text = groupData.group.name,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = detailText,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    progressColor.copy(alpha = 0.6f),
                                    progressColor
                                )
                            )
                        )
                )
            }
        }
    }
}


@Composable
private fun GroupSortDialog(
    title: String,
    groups: List<AppGroupWithApps>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    var orderedGroups by remember(groups) { mutableStateOf(groups) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (orderedGroups.isEmpty()) {
                    Text(AppText.t("group_no_groups_yet"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    orderedGroups.forEachIndexed { index, item ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = item.group.name,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            orderedGroups = orderedGroups.toMutableList().also {
                                                val current = it[index]
                                                it[index] = it[index - 1]
                                                it[index - 1] = current
                                            }
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = AppText.t("group_move_up"))
                                }
                                IconButton(
                                    onClick = {
                                        if (index < orderedGroups.lastIndex) {
                                            orderedGroups = orderedGroups.toMutableList().also {
                                                val current = it[index]
                                                it[index] = it[index + 1]
                                                it[index + 1] = current
                                            }
                                        }
                                    },
                                    enabled = index < orderedGroups.lastIndex,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = AppText.t("group_move_down"))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(orderedGroups.map { it.group.id }) }) {
                Text(AppText.t("group_save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_cancel"))
            }
        },
    )
}

@Composable
private fun GroupDetailDialog(
    groupData: AppGroupWithApps,
    todayUsageMillis: Long,
    archiveRepository: DailyArchiveRepository?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val themeColors = LocalThemeColors.current
    val zoneId = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zoneId) }
    val weekStart = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val historyFrom = remember(today) { today.minusDays(29).toString() }
    val historyTo = remember(today) { today.toString() }
    val historyItems by (
        archiveRepository
            ?.getGroupArchivesByRange(historyFrom, historyTo)
            ?.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
            ?: remember { mutableStateOf(emptyList<DailyGroupArchiveEntity>()) }
    )
    val groupHistory = remember(historyItems, groupData.group.id) {
        historyItems.filter { it.groupId == groupData.group.id }.sortedByDescending { it.archiveDate }
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var weekUsageByDay by remember(groupData.group.id) { mutableStateOf<List<Pair<LocalDate, Long>>>(emptyList()) }
    val shareBackground = MaterialTheme.colorScheme.background.toArgb()
    val shareSurface = MaterialTheme.colorScheme.surface.toArgb()
    val shareTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val shareMutedColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val shareAccent = if (groupData.group.type == GroupType.CONTROL) {
        themeColors.control.toArgb()
    } else {
        themeColors.encourage.toArgb()
    }

    LaunchedEffect(groupData.group.id, groupData.packageNames) {
        val usageRepository = UsageStatsUsageRepository(context)
        weekUsageByDay = withContext(Dispatchers.IO) {
            val result = mutableListOf<Pair<LocalDate, Long>>()
            var date = weekStart
            while (!date.isAfter(today)) {
                val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val usage = usageRepository.getUsageStats(start, end)
                result += date to groupData.packageNames.sumOf { usage[it] ?: 0L }
                date = date.plusDays(1)
            }
            result
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = groupData.group.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    onClick = {
                        shareGroupDetailBitmap(
                            context = context,
                            groupData = groupData,
                            tabLabel = listOf(AppText.t("group_today"), AppText.t("group_this_week"), AppText.t("group_history"))[selectedTab],
                            todayUsageMillis = todayUsageMillis,
                            weekUsageMillis = weekUsageByDay.sumOf { it.second },
                            historyItems = groupHistory,
                            background = shareBackground,
                            surface = shareSurface,
                            accent = shareAccent,
                            textColor = shareTextColor,
                            mutedColor = shareMutedColor,
                        )
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(Icons.Default.Share, contentDescription = AppText.t("group_share"))
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Close, contentDescription = AppText.t("group_close"))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AppText.t("group_today"), AppText.t("group_this_week"), AppText.t("group_history")).forEachIndexed { index, label ->
                        Surface(
                            onClick = { selectedTab = index },
                            shape = RoundedCornerShape(999.dp),
                            color = if (selectedTab == index) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            },
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTab == index) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }

                when (selectedTab) {
                    0 -> GroupTodayPanel(groupData, todayUsageMillis)
                    1 -> GroupWeekPanel(groupData, weekUsageByDay)
                    else -> GroupHistoryPanel(groupData, groupHistory)
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun GroupTodayPanel(
    groupData: AppGroupWithApps,
    usageMillis: Long,
) {
    val targetMillis = groupData.group.limitMinutes * 60_000L
    val delta = targetMillis - usageMillis
    val completed = if (groupData.group.type == GroupType.CONTROL) delta >= 0L else usageMillis >= targetMillis
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DetailMetricGrid(
            items = listOf(
                AppText.t("group_today_usage") to formatUsageDuration(usageMillis),
                AppText.t("group_target_limit_label") to AppText.t("group_value_min", groupData.group.limitMinutes),
                AppText.t("group_app_count_label") to AppText.t("group_app_count_value", groupData.packageNames.size),
                AppText.t("group_status") to if (completed) AppText.t("group_completed") else if (groupData.group.type == GroupType.CONTROL) AppText.t("group_over_by") else AppText.t("group_label_2"),
            ),
        )
        Text(
            text = if (delta >= 0L) AppText.t("group_remaining_value", formatUsageDuration(delta)) else AppText.t("group_over_by_value", formatUsageDuration(-delta)),
            style = MaterialTheme.typography.bodyMedium,
            color = if (delta >= 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun GroupWeekPanel(
    groupData: AppGroupWithApps,
    usageByDay: List<Pair<LocalDate, Long>>,
) {
    val totalUsage = usageByDay.sumOf { it.second }
    val targetMillis = groupData.group.limitMinutes * 60_000L
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DetailMetricGrid(
            items = listOf(
                AppText.t("group_week_total_label") to formatUsageDuration(totalUsage),
                AppText.t("group_configured_target") to AppText.t("group_value_min", groupData.group.limitMinutes),
                AppText.t("group_record_days_label") to AppText.t("group_value_days_5", usageByDay.size),
                AppText.t("group_label_3") to "${((totalUsage.toFloat() / targetMillis.coerceAtLeast(1).toFloat()) * 100).toInt()}%",
            ),
        )
        usageByDay.forEach { (date, usage) ->
            DetailProgressRow(
                label = "${date.monthValue}/${date.dayOfMonth}",
                value = formatUsageDuration(usage),
                progress = usage.toFloat() / targetMillis.coerceAtLeast(1).toFloat(),
            )
        }
    }
}

@Composable
private fun GroupHistoryPanel(
    groupData: AppGroupWithApps,
    items: List<DailyGroupArchiveEntity>,
) {
    val completedCount = items.count { it.completed }
    val totalUsage = items.sumOf { it.dailyUsageMillis }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DetailMetricGrid(
            items = listOf(
                AppText.t("group_last_30_days") to AppText.t("group_value_days", items.size),
                AppText.t("group_complete") to AppText.t("group_value_days_4", completedCount),
                AppText.t("group_total_usage") to formatUsageDuration(totalUsage),
                if (groupData.group.type == GroupType.ENCOURAGE) AppText.t("group_points") to AppText.t("group_points_value", trimTrailingZero(items.sumOf { it.earnedPoints })) else AppText.t("group_blocks") to AppText.t("group_value_times", items.sumOf { it.blockEventCount }),
            ),
        )
        if (items.isEmpty()) {
            Text(AppText.t("group_no_archived_history_yet"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            items.take(8).forEach { item ->
                DetailProgressRow(
                    label = item.archiveDate.substring(5),
                    value = formatUsageDuration(item.dailyUsageMillis),
                    progress = item.periodUsageMillisAtClose.toFloat() /
                        item.effectiveLimitMillisAtClose.coerceAtLeast(1).toFloat(),
                )
            }
        }
    }
}

@Composable
private fun DetailMetricGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (label, value) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                value,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailProgressRow(
    label: String,
    value: String,
    progress: Float,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, modifier = Modifier.width(44.dp), style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
        )
        Text(
            value,
            modifier = Modifier.width(58.dp),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

@Composable
private fun GroupEditDialog(
    group: AppGroupWithApps?,
    forcedType: GroupType,
    installedApps: List<ManagedApp>,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Int, GroupType, LimitPeriod, Double, List<String>) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val homePackage = remember(context) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        runCatching {
            context.packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
        }.getOrNull()
    }
    val excludedPackages = remember(context.packageName, homePackage) {
        setOfNotNull(
            context.packageName,
            homePackage,
            "com.miui.home",
            "com.android.launcher",
            "com.android.launcher3"
        )
    }

    var groupName by remember(group) { mutableStateOf(group?.group?.name.orEmpty()) }
    var limitText by remember(group) { mutableStateOf((group?.group?.limitMinutes ?: 60).toString()) }
    var pointRateText by remember(group) {
        mutableStateOf(
            if (group?.group?.type == GroupType.ENCOURAGE && group.group.pointsPerMinute > 0) {
                trimTrailingZero(group.group.pointsPerMinute)
            } else {
                "0.1"
            }
        )
    }
    var selectedPeriod by remember(group) {
        mutableStateOf(group?.group?.limitPeriod ?: LimitPeriod.DAILY)
    }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyUsedInSevenDays by remember { mutableStateOf(true) }
    var selectedPackages by remember(group) {
        mutableStateOf(group?.packageNames?.toSet().orEmpty())
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val appLimit = ProFeatureGate.limits(isProActive).appsPerGroupLimit

    val canSaveBase = groupName.trim().isNotBlank() &&
        (limitText.toIntOrNull()?.coerceIn(1, 1440) != null) &&
        (forcedType != GroupType.ENCOURAGE || pointRateText.toDoubleOrNull() != null)
    val appCountAllowed = ProFeatureGate.canSaveGroupApps(isProActive, selectedPackages.size)
    val canSave = canSaveBase && appCountAllowed

    val visibleApps = remember(installedApps, excludedPackages, showOnlyUsedInSevenDays, selectedPackages, searchQuery) {
        installedApps
            .asSequence()
            .filterNot { it.packageName in excludedPackages }
            .filter {
                !showOnlyUsedInSevenDays ||
                    it.usageTimeInMs > 60_000L ||
                    it.packageName in selectedPackages
            }
            .filter {
                if (searchQuery.isBlank()) {
                    true
                } else {
                    it.appName.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
            .sortedByDescending { it.usageTimeInMs }
            .toList()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        SyncDialogSystemBars()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = DialogHorizontalPadding, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = AppText.t("group_back"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (group != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = AppText.t("group_delete"),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (!appCountAllowed) {
                                onShowProUpsell(ProUpsellSource.GROUP_APPS)
                                return@IconButton
                            }
                            val name = groupName.trim()
                            val limit = limitText.toIntOrNull()?.coerceIn(1, 1440) ?: return@IconButton
                            val points = if (forcedType == GroupType.ENCOURAGE) {
                                pointRateText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: return@IconButton
                            } else {
                                0.0
                            }
                            onSave(name, limit, forcedType, selectedPeriod, points, selectedPackages.toList())
                        },
                        enabled = canSaveBase
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = AppText.t("group_save"),
                            tint = if (canSaveBase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                UnifiedInputField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = AppText.t("group_name_placeholder"),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Text,
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactPeriodSelector(
                        period = selectedPeriod,
                        groupType = forcedType,
                        onPeriodChange = { selectedPeriod = it },
                        modifier = Modifier.weight(1f)
                    )
                    UnifiedInputField(
                        value = limitText,
                        onValueChange = { limitText = it.filter(Char::isDigit).take(4) },
                        placeholder = "0",
                        suffix = AppText.t("group_minutes"),
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number,
                        textAlign = TextAlign.End
                    )
                        if (forcedType == GroupType.ENCOURAGE) {
                        UnifiedInputField(
                            value = pointRateText,
                            onValueChange = { pointRateText = sanitizeDecimalInput(it) },
                            placeholder = "0",
                            prefix = AppText.t("group_minutes_2"),
                            suffix = AppText.t("group_pts"),
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Decimal,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = { showOnlyUsedInSevenDays = !showOnlyUsedInSevenDays },
                        modifier = Modifier.height(CompactFieldHeight),
                        shape = CompactFieldShape,
                        color = if (showOnlyUsedInSevenDays) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (showOnlyUsedInSevenDays) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (showOnlyUsedInSevenDays) AppText.t("group_active_last_7_days") else AppText.t("group_all_apps"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (showOnlyUsedInSevenDays) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                    SearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                Text(
                    text = AppText.t("pro_group_app_limit_status", selectedPackages.size, appLimit),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedPackages.size > appLimit) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 56.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(
                        items = visibleApps,
                        key = { it.packageName }
                    ) { app ->
                        AppSelectionItem(
                            app = app,
                            checked = app.packageName in selectedPackages,
                            onCheckedChange = { checked ->
                                selectedPackages = if (checked) {
                                    if (selectedPackages.size >= appLimit) {
                                        onShowProUpsell(ProUpsellSource.GROUP_APPS)
                                        selectedPackages
                                    } else {
                                        selectedPackages + app.packageName
                                    }
                                } else {
                                    selectedPackages - app.packageName
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(AppText.t("group_delete_group")) },
            text = { Text(AppText.t("group_delete_confirmation")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text(AppText.t("group_delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(AppText.t("group_cancel"))
                }
            }
        )
    }
}

@Composable
private fun UnifiedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    keyboardType: KeyboardType,
    textAlign: TextAlign
) {
    FieldContainer(modifier = modifier) {
        if (prefix != null) {
            Text(
                text = prefix,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.titleSmall.merge(
                TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = textAlign
                )
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = textAlign
                        )
                    }
                    innerTextField()
                }
            }
        )
        if (suffix != null) {
            Text(
                text = suffix,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FieldContainer(modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = MaterialTheme.typography.titleSmall.merge(
                TextStyle(color = MaterialTheme.colorScheme.onSurface)
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isBlank()) {
                        Text(
                            text = AppText.t("group_search_apps"),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun FieldContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.height(CompactFieldHeight),
        shape = CompactFieldShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun AppSelectionItem(
    app: ManagedApp,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    // 异步加载图标，避免同步 Binder IPC 阻塞主线程
    var icon by remember(app.packageName) { mutableStateOf<android.graphics.drawable.Drawable?>(null) }
    LaunchedEffect(app.packageName) {
        icon = withContext(Dispatchers.IO) {
            runCatching { context.packageManager.getApplicationIcon(app.packageName) }.getOrNull()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            if (icon != null) {
                AsyncImage(
                    model = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatUsageDuration(app.usageTimeInMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        Checkbox(
            checked = checked,
            onCheckedChange = null, // Controlled by Row click
            colors = androidx.compose.material3.CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun CompactPeriodSelector(
    period: LimitPeriod,
    groupType: GroupType,
    onPeriodChange: (LimitPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (period) {
        LimitPeriod.DAILY -> if (groupType == GroupType.ENCOURAGE) AppText.t("group_daily_target") else AppText.t("group_daily_limit_label")
        LimitPeriod.WEEKLY -> if (groupType == GroupType.ENCOURAGE) AppText.t("group_weekly_target") else AppText.t("group_weekly_limit_label")
        LimitPeriod.MONTHLY -> if (groupType == GroupType.ENCOURAGE) AppText.t("group_monthly_target") else AppText.t("group_monthly_limit_label")
    }

    Box(modifier = modifier) {
        FieldContainer(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(LimitPeriod.DAILY, LimitPeriod.WEEKLY, LimitPeriod.MONTHLY).forEach { option ->
                val optionLabel = when (option) {
                    LimitPeriod.DAILY -> if (groupType == GroupType.ENCOURAGE) AppText.t("group_daily_target") else AppText.t("group_daily_limit_label")
                    LimitPeriod.WEEKLY -> if (groupType == GroupType.ENCOURAGE) AppText.t("group_weekly_target") else AppText.t("group_weekly_limit_label")
                    LimitPeriod.MONTHLY -> if (groupType == GroupType.ENCOURAGE) AppText.t("group_monthly_target") else AppText.t("group_monthly_limit_label")
                }
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        onPeriodChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SyncDialogSystemBars() {
    val view = LocalView.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val backgroundArgb = backgroundColor.toArgb()
    val lightIcons = backgroundColor.luminance() > 0.5f
    val dialogWindow = remember(view) { (view.parent as? DialogWindowProvider)?.window }
    val activityWindow = remember(view.context) { view.context.findActivity()?.window }

    DisposableEffect(dialogWindow, activityWindow, backgroundArgb, lightIcons) {
        val previousDialogState = dialogWindow?.snapshot()
        val previousActivityState = activityWindow?.snapshot()

        dialogWindow?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            window.setBackgroundDrawable(ColorDrawable(backgroundArgb))
            window.statusBarColor = backgroundArgb
            window.navigationBarColor = backgroundArgb
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = lightIcons
                isAppearanceLightNavigationBars = lightIcons
            }
        }

        activityWindow?.let { window ->
            window.statusBarColor = backgroundArgb
            window.navigationBarColor = backgroundArgb
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = lightIcons
                isAppearanceLightNavigationBars = lightIcons
            }
        }

        onDispose {
            dialogWindow?.restore(previousDialogState)
            activityWindow?.restore(previousActivityState)
        }
    }
}

private data class WindowSnapshot(
    val statusBarColor: Int,
    val navigationBarColor: Int
)

private fun Window.snapshot(): WindowSnapshot {
    return WindowSnapshot(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor
    )
}

private fun Window.restore(snapshot: WindowSnapshot?) {
    if (snapshot == null) return
    statusBarColor = snapshot.statusBarColor
    navigationBarColor = snapshot.navigationBarColor
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun formatUsageDuration(durationMillis: Long): String {
    if (durationMillis <= 0L) return AppText.t("group_duration_less_than_one_minute")
    val totalMinutes = durationMillis / 60_000L
    if (totalMinutes <= 0L) return AppText.t("group_duration_less_than_one_minute")
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> AppText.t("group_duration_hours_minutes_compact", hours, minutes)
        hours > 0 -> AppText.t("duration_value_h", hours)
        else -> AppText.t("duration_value_min", minutes)
    }
}

private fun shareGroupDetailBitmap(
    context: Context,
    groupData: AppGroupWithApps,
    tabLabel: String,
    todayUsageMillis: Long,
    weekUsageMillis: Long,
    historyItems: List<DailyGroupArchiveEntity>,
    background: Int,
    surface: Int,
    accent: Int,
    textColor: Int,
    mutedColor: Int,
) {
    val bitmap = renderGroupDetailBitmap(
        groupName = groupData.group.name,
        typeLabel = if (groupData.group.type == GroupType.CONTROL) AppText.t("group_commitment") else AppText.t("group_small_encouragement"),
        tabLabel = tabLabel,
        todayUsage = formatUsageDuration(todayUsageMillis),
        weekUsage = formatUsageDuration(weekUsageMillis),
        historyDays = historyItems.size,
        completedDays = historyItems.count { it.completed },
        packageCount = groupData.packageNames.size,
        background = background,
        surface = surface,
        accent = accent,
        textColor = textColor,
        mutedColor = mutedColor,
        limitText = AppText.t("group_value_minutes", groupData.group.limitMinutes),
    )
    val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
    val file = File(shareDir, "tinyvow-group-${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TITLE, AppText.t("group_tiny_vow_group_data"))
        clipData = ClipData.newUri(context.contentResolver, AppText.t("group_tiny_vow_group_data"), uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, AppText.t("group_share_group_data")))
}

private fun renderGroupDetailBitmap(
    groupName: String,
    typeLabel: String,
    tabLabel: String,
    todayUsage: String,
    weekUsage: String,
    historyDays: Int,
    completedDays: Int,
    packageCount: Int,
    background: Int,
    surface: Int,
    accent: Int,
    textColor: Int,
    mutedColor: Int,
    limitText: String,
): Bitmap {
    val width = 1080
    val height = 1440
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = background }
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surface }
    val primary = accent
    val muted = mutedColor
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 58f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = muted
        textSize = 30f
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primary
        textSize = 62f
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
    canvas.drawRoundRect(RectF(58f, 70f, width - 58f, height - 70f), 54f, 54f, cardPaint)
    canvas.drawText("Tiny Vow · $typeLabel", 110f, 155f, labelPaint)
    drawBitmapEllipsizedText(canvas, groupName, 110f, 240f, width - 220f, titlePaint)
    canvas.drawRoundRect(
        RectF(110f, 300f, 360f, 366f),
        33f,
        33f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(32, 91, 139, 153) },
    )
    canvas.drawText(tabLabel, 188f, 344f, Paint(labelPaint).apply {
        color = primary
        textSize = 34f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    })

    val metrics = listOf(
        AppText.t("group_today_usage") to todayUsage,
        AppText.t("group_week_total_label") to weekUsage,
        AppText.t("group_target_limit_label") to limitText,
        AppText.t("group_app_count_label") to AppText.t("group_app_count_value", packageCount),
        AppText.t("group_history_days_label") to AppText.t("group_value_days_2", historyDays),
        AppText.t("group_completed_days_label") to AppText.t("group_value_days_3", completedDays),
    )
    metrics.forEachIndexed { index, metric ->
        val row = index / 2
        val col = index % 2
        val left = 110f + col * 430f
        val top = 430f + row * 210f
        canvas.drawRoundRect(
            RectF(left, top, left + 380f, top + 162f),
            34f,
            34f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(
                32,
                android.graphics.Color.red(primary),
                android.graphics.Color.green(primary),
                android.graphics.Color.blue(primary),
            )
        },
        )
        canvas.drawText(metric.first, left + 32f, top + 52f, labelPaint)
        drawBitmapEllipsizedText(canvas, metric.second, left + 32f, top + 120f, 316f, valuePaint)
    }
    canvas.drawText(AppText.t("group_label"), 110f, height - 142f, Paint(labelPaint).apply { textSize = 34f })
    return bitmap
}

private fun drawBitmapEllipsizedText(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    maxWidth: Float,
    paint: Paint,
) {
    val output = TextUtils.ellipsize(text, TextPaint(paint), maxWidth, TextUtils.TruncateAt.END).toString()
    canvas.drawText(output, x, y, paint)
}

private fun sanitizeDecimalInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' }
    if (filtered.isBlank()) return ""
    val firstDot = filtered.indexOf('.')
    return if (firstDot == -1) {
        filtered
    } else {
        val integerPart = filtered.substring(0, firstDot + 1)
        val decimalPart = filtered.substring(firstDot + 1).replace(".", "")
        integerPart + decimalPart.take(2)
    }
}

private fun trimTrailingZero(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString().trimEnd('0').trimEnd('.')
    }
}





