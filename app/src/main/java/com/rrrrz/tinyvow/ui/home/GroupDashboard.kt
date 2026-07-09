package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.ActiveRewardEffectEntity
import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.EncourageMetric
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.repository.parseRewardPayload
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.supermode.GuardedAction
import com.rrrrz.tinyvow.data.time.BusinessDay
import com.rrrrz.tinyvow.data.usage.AppSession
import com.rrrrz.tinyvow.data.usage.MergedUsageRepository
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowEmptyState
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowMetricTile
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSectionHeader
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import com.rrrrz.tinyvow.ui.theme.TinyVowStatusPill
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
    periodUsageMap: Map<String, Long> = emptyMap(),
    todayAppUsageMap: Map<String, Long> = emptyMap(),
    todayAppOpenCountMap: Map<String, Int> = emptyMap(),
    todaySessions: List<AppSession> = emptyList(),
    todayStepCount: Int = 0,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    appIconCache: Map<String, Drawable> = emptyMap(),
    onAppIconLoaded: (String, Drawable) -> Unit = { _, _ -> },
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
    createGroupRequest: Int = 0,
    archiveRepository: DailyArchiveRepository?,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onGuardAction: (GuardedAction, () -> Unit) -> Unit,
    openGroupDetailRequest: Int = 0,
    openGroupDetailGroup: AppGroupWithApps? = null,
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
        groupsWithApps.filter { it.group.type == GroupType.ENCOURAGE && it.group.encourageMetric != EncourageMetric.STEPS }
    }
    val requestEditGroup: (AppGroupWithApps) -> Unit = { groupWithApps ->
        val groupsForType =
            if (groupWithApps.group.type == GroupType.CONTROL) {
                controlGroups
            } else {
                encourageGroups
            }
        val index = groupsForType.indexOfFirst { it.group.id == groupWithApps.group.id }
        if (ProFeatureGate.canEditGroup(isProActive, index)) {
            onGuardAction(GuardedAction.EDIT_GROUP) {
                editingGroup = groupWithApps
                forcedType = groupWithApps.group.type
                showDialog = true
            }
        } else {
            onShowProUpsell(ProUpsellSource.GROUP_LIMIT)
        }
    }

    LaunchedEffect(createGroupRequest) {
        if (createGroupRequest <= 0) return@LaunchedEffect
        if (ProFeatureGate.canAddGroup(isProActive, GroupType.CONTROL, controlGroups.size)) {
            editingGroup = null
            forcedType = GroupType.CONTROL
            showDialog = true
        } else {
            onShowProUpsell(ProUpsellSource.GROUP_LIMIT)
        }
    }

    LaunchedEffect(openGroupDetailRequest) {
        if (openGroupDetailRequest > 0) {
            detailGroup = openGroupDetailGroup
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
            verticalAlignment = Alignment.Top,
        ) {
            SectionCard(
                title = AppText.t("group_commitment"),
                groups = controlGroups,
                usageMap = usageMap,
                activeRewardEffects = activeRewardEffects,
                appIconCache = appIconCache,
                onAppIconLoaded = onAppIconLoaded,
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
                onEdit = requestEditGroup,
                modifier = Modifier.weight(1f),
            )

            SectionCard(
                title = AppText.t("group_small_encouragement"),
                groups = encourageGroups,
                usageMap = usageMap,
                activeRewardEffects = activeRewardEffects,
                appIconCache = appIconCache,
                onAppIconLoaded = onAppIconLoaded,
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
                onEdit = requestEditGroup,
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

    }

    if (showDialog) {
        GroupEditDialog(
            group = editingGroup,
            forcedType = forcedType,
            installedApps = installedApps,
            appIconCache = appIconCache,
            onAppIconLoaded = onAppIconLoaded,
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
        GroupDetailSheet(
            groupData = group,
            todayUsageMillis = usageMap[group.group.id] ?: 0L,
            periodUsageMillis = periodUsageMap[group.group.id] ?: usageMap[group.group.id] ?: 0L,
            todayAppUsageMap = todayAppUsageMap,
            todayAppOpenCountMap = todayAppOpenCountMap,
            todaySessions = todaySessions,
            todayStepCount = todayStepCount,
            activeEffects = activeRewardEffects.filter { it.targetGroupId == group.group.id },
            installedApps = installedApps,
            appIconCache = appIconCache,
            onAppIconLoaded = onAppIconLoaded,
            onEdit = {
                detailGroup = null
                requestEditGroup(it)
            },
            archiveRepository = archiveRepository,
            onDismiss = { detailGroup = null },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SectionCard(
    title: String,
    groups: List<AppGroupWithApps>,
    usageMap: Map<String, Long>,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    appIconCache: Map<String, Drawable>,
    onAppIconLoaded: (String, Drawable) -> Unit,
    accent: Color,
    onAdd: () -> Unit,
    onSort: () -> Unit,
    onOpen: (AppGroupWithApps) -> Unit,
    onEdit: (AppGroupWithApps) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    TinyVowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
        borderAlpha = 0.30f,
        shadowElevation = TinyVowElevation.Card,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TinyVowSectionHeader(
                title = title,
                trailing = {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(accent.copy(alpha = 0.13f))
                            .combinedClickable(
                                onClick = onAdd,
                                onLongClick = onSort,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = AppText.t("group_add_group"),
                            tint = accent,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                },
            )

            if (groups.isEmpty()) {
                Text(
                    text = AppText.t("group_no_groups_yet"),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )
            } else {
                groups.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f))
                    }
                    GroupCard(
                        groupData = item,
                        usedMinutes = ((usageMap[item.group.id] ?: 0L) / 60_000L).toInt(),
                        activeEffects = activeRewardEffects.filter { it.targetGroupId == item.group.id },
                        appIconCache = appIconCache,
                        onAppIconLoaded = onAppIconLoaded,
                        accent = accent,
                        onClick = { onOpen(item) },
                        onLongClick = { onEdit(item) },
                    )
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
    activeEffects: List<ActiveRewardEffectEntity>,
    appIconCache: Map<String, Drawable>,
    onAppIconLoaded: (String, Drawable) -> Unit,
    accent: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val themeColors = LocalThemeColors.current
    val periodLabel = when (groupData.group.limitPeriod) {
        LimitPeriod.DAILY -> AppText.t("group_daily_short")
        LimitPeriod.WEEKLY -> AppText.t("group_weekly_short")
        LimitPeriod.MONTHLY -> AppText.t("group_monthly_short")
    }
    val rewardSummary = remember(activeEffects, groupData.group.type) {
        groupActiveRewardSummary(activeEffects, groupData.group.type)
    }
    val effectLabels = groupActiveRewardLabels(rewardSummary, groupData.group.type)
    val effectiveLimitMinutes =
        if (groupData.group.type == GroupType.CONTROL) {
            groupData.group.limitMinutes + rewardSummary.extraMinutes
        } else {
            groupData.group.limitMinutes
        }
    val detailText = AppText.t("group_usage_value_limit_minutes_with_period", usedMinutes, effectiveLimitMinutes, periodLabel)
    val rawProgress = if (effectiveLimitMinutes > 0) {
        usedMinutes.toFloat() / effectiveLimitMinutes.toFloat()
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
    val iconSize = 34
    val iconOffset = iconSize / 2
    val maxIcons = 3
    val iconWidth = iconSize + (iconPackages.size.coerceIn(1, maxIcons) - 1) * iconOffset

    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progress,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow,
        ),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TinyVowRadius.Control))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Box(modifier = Modifier.width(iconWidth.dp), contentAlignment = Alignment.CenterStart) {
                iconPackages.forEachIndexed { index, packageName ->
                    // 异步加载图标，避免同步 Binder IPC 阻塞主线程
                    val icon = appIconCache[packageName]
                    LaunchedEffect(packageName, icon) {
                        if (icon != null) return@LaunchedEffect
                        val loadedIcon = withContext(Dispatchers.IO) {
                            AppVisualCache.getIcon(context, packageName)
                        }
                        if (loadedIcon != null) {
                            onAppIconLoaded(packageName, loadedIcon)
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .padding(start = (index * iconOffset).dp)
                            .size(iconSize.dp)
                            .zIndex((maxIcons - index).toFloat()),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.95f)),
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

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = groupData.group.name,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.ink,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = detailText,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.4.sp),
                    color = themeColors.inkMuted,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (effectLabels.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        effectLabels.take(2).forEachIndexed { index, label ->
                            if (index > 0) Spacer(modifier = Modifier.width(4.dp))
                            GroupEffectPill(text = label, accent = accent)
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(progressColor)
            )
        }
    }
}

private data class GroupActiveRewardSummary(
    val extraMinutes: Int = 0,
    val hasPeriodPass: Boolean = false,
    val pointsMultiplier: Double = 1.0,
)

private fun groupActiveRewardSummary(
    effects: List<ActiveRewardEffectEntity>,
    groupType: GroupType,
): GroupActiveRewardSummary {
    val extraMinutes =
        if (groupType == GroupType.CONTROL) {
            effects
                .filter { it.effectType == RewardType.TIME_ADD || it.effectType == RewardType.EMERGENCY_UNLOCK }
                .sumOf { parseRewardPayload(it.payloadJson).minutes }
        } else {
            0
        }
    val hasPeriodPass = groupType == GroupType.CONTROL && effects.any { it.effectType == RewardType.PERIOD_PASS }
    val pointsMultiplier =
        if (groupType == GroupType.ENCOURAGE) {
            effects
                .filter { it.effectType == RewardType.DOUBLE_POINTS_DAY }
                .maxOfOrNull { parseRewardPayload(it.payloadJson).pointsMultiplier.coerceAtLeast(1.0) }
                ?: 1.0
        } else {
            1.0
        }
    return GroupActiveRewardSummary(
        extraMinutes = extraMinutes,
        hasPeriodPass = hasPeriodPass,
        pointsMultiplier = pointsMultiplier,
    )
}

private fun groupActiveRewardLabels(
    summary: GroupActiveRewardSummary,
    groupType: GroupType,
): List<String> =
    buildList {
        if (groupType == GroupType.CONTROL && summary.extraMinutes > 0) {
            add(AppText.t("home_group_effect_extra_minutes", summary.extraMinutes))
        }
        if (groupType == GroupType.CONTROL && summary.hasPeriodPass) {
            add(AppText.t("home_group_effect_period_pass"))
        }
        if (groupType == GroupType.ENCOURAGE && summary.pointsMultiplier > 1.0) {
            add(AppText.t("home_points_multiplier_badge", trimTrailingZero(summary.pointsMultiplier)))
        }
    }

@Composable
private fun GroupEffectPill(
    text: String,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = accent.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
            fontWeight = FontWeight.SemiBold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
                                    fontWeight = FontWeight.Medium,
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
            TinyVowButton(
                text = AppText.t("group_save"),
                onClick = { onSave(orderedGroups.map { it.group.id }) },
                tone = TinyVowButtonTone.Primary,
            )
        },
        dismissButton = {
            TinyVowButton(
                text = AppText.t("group_cancel"),
                onClick = onDismiss,
            )
        },
    )
}

private enum class GroupDetailTab {
    OVERVIEW,
    APPS,
    RHYTHM,
    HISTORY,
}

private enum class GroupDetailAppScope {
    TODAY,
    PERIOD,
    HISTORY,
}

private data class GroupDetailAppRow(
    val packageName: String,
    val appName: String,
    val usageMillis: Long,
    val openCount: Int = 0,
    val sessionCount: Int = 0,
    val longestSessionMillis: Long = 0L,
    val activeDays: Int = 0,
    val earnedPoints: Double = 0.0,
)

private data class GroupDetailDayRow(
    val date: LocalDate,
    val usageMillis: Long,
)

private data class GroupDetailArchiveSummary(
    val archivedDays: Int,
    val completedDays: Int,
    val totalUsageMillis: Long,
    val blockEvents: Int,
    val exceededDays: Int,
    val totalEarnedPoints: Double,
    val averageRemainingMillis: Long,
    val averageExceededMillis: Long,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailSheet(
    groupData: AppGroupWithApps,
    todayUsageMillis: Long,
    periodUsageMillis: Long,
    todayAppUsageMap: Map<String, Long>,
    todayAppOpenCountMap: Map<String, Int>,
    todaySessions: List<AppSession>,
    todayStepCount: Int,
    activeEffects: List<ActiveRewardEffectEntity>,
    installedApps: List<ManagedApp>,
    appIconCache: Map<String, Drawable>,
    onAppIconLoaded: (String, Drawable) -> Unit,
    onEdit: (AppGroupWithApps) -> Unit,
    archiveRepository: DailyArchiveRepository?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val themeColors = LocalThemeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val zoneId = remember { ZoneId.systemDefault() }
    val dayStartHour = BusinessDay.cachedStartHour()
    val today = remember(dayStartHour) { BusinessDay.today(zoneId, dayStartHour) }
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
    val appHistoryItems by (
        archiveRepository
            ?.getAppArchivesByGroupAndRange(groupData.group.id, historyFrom, historyTo)
            ?.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
            ?: remember { mutableStateOf(emptyList<DailyAppArchiveEntity>()) }
    )
    var selectedTab by remember { mutableStateOf(GroupDetailTab.OVERVIEW) }
    var selectedAppScope by remember { mutableStateOf(GroupDetailAppScope.TODAY) }
    var weekUsageByDay by remember(groupData.group.id) { mutableStateOf<List<GroupDetailDayRow>>(emptyList()) }
    var periodAppRows by remember(groupData.group.id) { mutableStateOf<List<GroupDetailAppRow>>(emptyList()) }
    val appNameByPackage = remember(installedApps) {
        installedApps.associate { it.packageName to it.appName }
    }
    val isAppUsageGroup = remember(groupData.group.type, groupData.group.encourageMetric) {
        groupData.group.type == GroupType.CONTROL || groupData.group.encourageMetric == EncourageMetric.APP_USAGE
    }
    val accent = if (groupData.group.type == GroupType.CONTROL) themeColors.control else themeColors.encourage
    val rewardSummary = remember(activeEffects, groupData.group.type) {
        groupActiveRewardSummary(activeEffects, groupData.group.type)
    }
    val effectiveTargetMillis = remember(groupData.group, rewardSummary) {
        groupDetailTargetMillis(groupData, rewardSummary)
    }
    val statusLabel = groupDetailStatusLabel(
        groupData = groupData,
        usageMillis = periodUsageMillis,
        targetMillis = effectiveTargetMillis,
        todayStepCount = todayStepCount,
    )
    val progress = groupDetailProgress(
        groupData = groupData,
        usageMillis = periodUsageMillis,
        targetMillis = effectiveTargetMillis,
        todayStepCount = todayStepCount,
    )
    val todayAppRows = remember(groupData.packageNames, todayAppUsageMap, todayAppOpenCountMap, todaySessions, appNameByPackage) {
        buildCurrentAppRows(
            packageNames = groupData.packageNames,
            usageMap = todayAppUsageMap,
            openCountMap = todayAppOpenCountMap,
            sessions = todaySessions,
            appNameByPackage = appNameByPackage,
            includeZeroUsage = true,
        )
    }
    val historyAppRows = remember(appHistoryItems, appNameByPackage) {
        buildHistoryAppRows(appHistoryItems, appNameByPackage)
    }
    val archiveSummary = remember(groupHistory) { buildGroupArchiveSummary(groupHistory) }

    LaunchedEffect(groupData.group.id, groupData.packageNames, today, dayStartHour) {
        weekUsageByDay = withContext(Dispatchers.IO) {
            val result = mutableListOf<GroupDetailDayRow>()
            val usageRepository = MergedUsageRepository(context)
            var date = weekStart
            while (!date.isAfter(today)) {
                val start = BusinessDay.startOfDayMillis(date, zoneId, dayStartHour)
                val end = BusinessDay.nextDayStartMillis(date, zoneId, dayStartHour)
                val usage = usageRepository.getUsageStats(start, end, groupData.group.type)
                result += GroupDetailDayRow(
                    date = date,
                    usageMillis = groupData.packageNames.sumOf { usage[it] ?: 0L },
                )
                date = date.plusDays(1)
            }
            result
        }
    }

    LaunchedEffect(groupData.group.id, groupData.packageNames, groupData.group.limitPeriod, groupData.group.type, today, dayStartHour, appNameByPackage) {
        periodAppRows = withContext(Dispatchers.IO) {
            val usageRepository = MergedUsageRepository(context)
            val start = groupDetailPeriodStartMillis(groupData.group.limitPeriod, today, zoneId, dayStartHour)
            val now = System.currentTimeMillis()
            val usage = usageRepository.getUsageStats(start, now, groupData.group.type)
            val opens = usageRepository.getAppOpenCount(start, now)
            val sessions = usageRepository.getUsageSessions(start, now)
            buildCurrentAppRows(
                packageNames = groupData.packageNames,
                usageMap = usage,
                openCountMap = opens,
                sessions = sessions,
                appNameByPackage = appNameByPackage,
                includeZeroUsage = true,
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = TinyVowSpacing.PageHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GroupDetailHeader(
                groupData = groupData,
                periodLabel = groupDetailPeriodLabel(groupData.group.limitPeriod),
                accent = accent,
                onEdit = { onEdit(groupData) },
                onDismiss = onDismiss,
            )
            GroupDetailSummaryCard(
                groupData = groupData,
                todayUsageMillis = todayUsageMillis,
                periodUsageMillis = periodUsageMillis,
                todayStepCount = todayStepCount,
                targetMillis = effectiveTargetMillis,
                progress = progress,
                statusLabel = statusLabel,
                accent = accent,
            )
            GroupDetailTabRow(
                selectedTab = selectedTab,
                onSelect = { selectedTab = it },
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (selectedTab) {
                    GroupDetailTab.OVERVIEW -> {
                        item {
                            GroupOverviewPanel(
                                groupData = groupData,
                                todayUsageMillis = todayUsageMillis,
                                periodUsageMillis = periodUsageMillis,
                                todayStepCount = todayStepCount,
                                targetMillis = effectiveTargetMillis,
                                activeEffects = activeEffects,
                                appRows = todayAppRows,
                                isAppUsageGroup = isAppUsageGroup,
                                accent = accent,
                            )
                        }
                    }
                    GroupDetailTab.APPS -> {
                        item {
                            GroupAppScopeRow(
                                selectedScope = selectedAppScope,
                                onSelect = { selectedAppScope = it },
                                enabled = isAppUsageGroup,
                            )
                        }
                        if (!isAppUsageGroup) {
                            item {
                                TinyVowEmptyState(
                                    title = AppText.t("group_detail_steps_no_app_title"),
                                    body = AppText.t("group_detail_steps_no_app_body"),
                                )
                            }
                        } else {
                            val rows = when (selectedAppScope) {
                                GroupDetailAppScope.TODAY -> todayAppRows
                                GroupDetailAppScope.PERIOD -> periodAppRows
                                GroupDetailAppScope.HISTORY -> historyAppRows
                            }
                            if (rows.isEmpty()) {
                                item {
                                    TinyVowEmptyState(
                                        title = AppText.t("group_detail_no_app_data_title"),
                                        body = AppText.t("group_detail_no_app_data_body"),
                                    )
                                }
                            } else {
                                items(rows, key = { "${selectedAppScope.name}-${it.packageName}" }) { row ->
                                    GroupDetailAppRowItem(
                                        row = row,
                                        totalUsageMillis = rows.sumOf { it.usageMillis },
                                        scope = selectedAppScope,
                                        accent = accent,
                                        icon = appIconCache[row.packageName],
                                        onIconLoaded = onAppIconLoaded,
                                    )
                                }
                            }
                        }
                    }
                    GroupDetailTab.RHYTHM -> {
                        item {
                            GroupRhythmPanel(
                                groupData = groupData,
                                usageByDay = weekUsageByDay,
                                periodUsageMillis = periodUsageMillis,
                                targetMillis = effectiveTargetMillis,
                                accent = accent,
                            )
                        }
                    }
                    GroupDetailTab.HISTORY -> {
                        item {
                            GroupHistoryPanel(
                                groupData = groupData,
                                items = groupHistory,
                                summary = archiveSummary,
                                targetMillis = effectiveTargetMillis,
                                accent = accent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupDetailHeader(
    groupData: AppGroupWithApps,
    periodLabel: String,
    accent: Color,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
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
                TinyVowStatusPill(
                    text = if (groupData.group.type == GroupType.CONTROL) AppText.t("group_commitment") else AppText.t("group_small_encouragement"),
                    color = accent,
                    containerColor = accent.copy(alpha = 0.12f),
                )
                Text(
                    text = periodLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = groupData.group.name,
                style = MaterialTheme.typography.titleLarge,
                color = themeColors.inkStrong,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = AppText.t("group_edit_title"),
                tint = accent,
            )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Close, contentDescription = AppText.t("group_close"))
        }
    }
}

@Composable
private fun GroupDetailSummaryCard(
    groupData: AppGroupWithApps,
    todayUsageMillis: Long,
    periodUsageMillis: Long,
    todayStepCount: Int,
    targetMillis: Long,
    progress: Float,
    statusLabel: String,
    accent: Color,
) {
    val themeColors = LocalThemeColors.current
    val isStepGroup = groupData.group.type == GroupType.ENCOURAGE && groupData.group.encourageMetric == EncourageMetric.STEPS
    val primaryValue =
        if (isStepGroup) {
            AppText.t("group_step_count_value", todayStepCount)
        } else {
            formatUsageDuration(periodUsageMillis)
        }
    val targetValue =
        if (isStepGroup) {
            AppText.t("group_step_count_value", groupData.group.stepTarget)
        } else {
            formatUsageDuration(targetMillis)
        }
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
        borderAlpha = 0.36f,
        shadowElevation = TinyVowElevation.Card,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = AppText.t("group_detail_current_period"),
                        style = MaterialTheme.typography.labelMedium,
                        color = themeColors.inkMuted,
                    )
                    Text(
                        text = primaryValue,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TinyVowStatusPill(
                    text = statusLabel,
                    color = accent,
                    containerColor = accent.copy(alpha = 0.12f),
                )
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = accent,
                trackColor = accent.copy(alpha = 0.14f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TinyVowMetricTile(
                    label = if (isStepGroup) AppText.t("group_step_target_label") else AppText.t("group_target_limit_label"),
                    value = targetValue,
                    color = accent,
                    modifier = Modifier.weight(1f),
                )
                TinyVowMetricTile(
                    label = AppText.t("group_today_usage"),
                    value = if (isStepGroup) AppText.t("group_step_count_value", todayStepCount) else formatUsageDuration(todayUsageMillis),
                    color = accent,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun GroupDetailTabRow(
    selectedTab: GroupDetailTab,
    onSelect: (GroupDetailTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GroupDetailTab.entries.forEach { tab ->
            GroupDetailChip(
                text = when (tab) {
                    GroupDetailTab.OVERVIEW -> AppText.t("group_detail_overview")
                    GroupDetailTab.APPS -> AppText.t("group_detail_apps")
                    GroupDetailTab.RHYTHM -> AppText.t("group_detail_rhythm")
                    GroupDetailTab.HISTORY -> AppText.t("group_detail_history")
                },
                selected = selectedTab == tab,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun GroupAppScopeRow(
    selectedScope: GroupDetailAppScope,
    onSelect: (GroupDetailAppScope) -> Unit,
    enabled: Boolean,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GroupDetailAppScope.entries.forEach { scope ->
            GroupDetailChip(
                text = when (scope) {
                    GroupDetailAppScope.TODAY -> AppText.t("group_today")
                    GroupDetailAppScope.PERIOD -> AppText.t("group_detail_current_period")
                    GroupDetailAppScope.HISTORY -> AppText.t("group_detail_last_30_days")
                },
                selected = selectedScope == scope,
                enabled = enabled,
                onClick = { onSelect(scope) },
            )
        }
    }
}

@Composable
private fun GroupDetailChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.Pill),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else themeColors.surfaceSoft,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else themeColors.inkMuted,
        border = BorderStroke(1.dp, themeColors.borderSoft.copy(alpha = 0.36f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GroupOverviewPanel(
    groupData: AppGroupWithApps,
    todayUsageMillis: Long,
    periodUsageMillis: Long,
    todayStepCount: Int,
    targetMillis: Long,
    activeEffects: List<ActiveRewardEffectEntity>,
    appRows: List<GroupDetailAppRow>,
    isAppUsageGroup: Boolean,
    accent: Color,
) {
    val isStepGroup = groupData.group.type == GroupType.ENCOURAGE && groupData.group.encourageMetric == EncourageMetric.STEPS
    val estimatedPoints =
        if (isStepGroup) {
            todayStepCount * groupData.group.pointsPerStep
        } else if (groupData.group.type == GroupType.ENCOURAGE) {
            periodUsageMillis / 60_000.0 * groupData.group.pointsPerMinute
        } else {
            0.0
        }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailMetricGrid(
            items = buildList {
                add(AppText.t("group_today_usage") to if (isStepGroup) AppText.t("group_step_count_value", todayStepCount) else formatUsageDuration(todayUsageMillis))
                add(AppText.t("group_detail_current_period") to if (isStepGroup) AppText.t("group_step_count_value", todayStepCount) else formatUsageDuration(periodUsageMillis))
                add((if (isStepGroup) AppText.t("group_step_target_label") else AppText.t("group_target_limit_label")) to if (isStepGroup) AppText.t("group_step_count_value", groupData.group.stepTarget) else formatUsageDuration(targetMillis))
                add(AppText.t("group_status") to groupDetailStatusLabel(groupData, periodUsageMillis, targetMillis, todayStepCount))
                if (groupData.group.type == GroupType.ENCOURAGE) {
                    add(AppText.t("group_points") to AppText.t("group_points_value", trimTrailingZero(estimatedPoints)))
                }
            },
            accent = accent,
        )
        ActiveEffectsPanel(activeEffects = activeEffects, groupType = groupData.group.type, accent = accent)
        if (isAppUsageGroup) {
            MemberPreviewPanel(groupData = groupData, appRows = appRows, accent = accent)
        } else {
            StepRulePanel(groupData = groupData, todayStepCount = todayStepCount, accent = accent)
        }
    }
}

@Composable
private fun ActiveEffectsPanel(
    activeEffects: List<ActiveRewardEffectEntity>,
    groupType: GroupType,
    accent: Color,
) {
    val labels = groupActiveRewardLabels(groupActiveRewardSummary(activeEffects, groupType), groupType)
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        color = LocalThemeColors.current.surfaceSoft,
        borderAlpha = 0.30f,
        shadowElevation = TinyVowElevation.Flat,
    ) {
        Column(
            modifier = Modifier.padding(TinyVowSpacing.CompactCardHorizontal),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = AppText.t("group_detail_active_effects"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = LocalThemeColors.current.inkStrong,
            )
            if (labels.isEmpty()) {
                Text(
                    text = AppText.t("group_detail_no_active_effects"),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalThemeColors.current.inkMuted,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    labels.forEach { label ->
                        GroupEffectPill(text = label, accent = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberPreviewPanel(
    groupData: AppGroupWithApps,
    appRows: List<GroupDetailAppRow>,
    accent: Color,
) {
    val preview = appRows.take(3)
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        borderAlpha = 0.30f,
        shadowElevation = TinyVowElevation.Flat,
    ) {
        Column(
            modifier = Modifier.padding(TinyVowSpacing.CompactCardHorizontal),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = AppText.t("group_detail_members", groupData.packageNames.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = LocalThemeColors.current.inkStrong,
            )
            if (preview.isEmpty()) {
                Text(
                    text = AppText.t("group_detail_no_members"),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalThemeColors.current.inkMuted,
                )
            } else {
                preview.forEach { row ->
                    DetailProgressRow(
                        label = row.appName,
                        value = formatUsageDuration(row.usageMillis),
                        progress = row.usageMillis.toFloat() / appRows.sumOf { it.usageMillis }.coerceAtLeast(1L).toFloat(),
                        accent = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepRulePanel(
    groupData: AppGroupWithApps,
    todayStepCount: Int,
    accent: Color,
) {
    val remaining = (groupData.group.stepTarget - todayStepCount).coerceAtLeast(0)
    DetailMetricGrid(
        items = listOf(
            AppText.t("group_step_today_steps") to AppText.t("group_step_count_value", todayStepCount),
            AppText.t("group_step_target_label") to AppText.t("group_step_count_value", groupData.group.stepTarget),
            AppText.t("group_step_remaining_value", remaining) to AppText.t("group_points_value", trimTrailingZero(todayStepCount * groupData.group.pointsPerStep)),
        ),
        accent = accent,
    )
}

@Composable
private fun GroupDetailAppRowItem(
    row: GroupDetailAppRow,
    totalUsageMillis: Long,
    scope: GroupDetailAppScope,
    accent: Color,
    icon: Drawable?,
    onIconLoaded: (String, Drawable) -> Unit,
) {
    val context = LocalContext.current
    val themeColors = LocalThemeColors.current
    LaunchedEffect(row.packageName, icon) {
        if (icon != null) return@LaunchedEffect
        val loadedIcon = withContext(Dispatchers.IO) {
            AppVisualCache.getIcon(context, row.packageName)
        }
        if (loadedIcon != null) {
            onIconLoaded(row.packageName, loadedIcon)
        }
    }
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        borderAlpha = 0.30f,
        shadowElevation = TinyVowElevation.Flat,
    ) {
        Row(
            modifier = Modifier.padding(TinyVowSpacing.CompactCardHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = themeColors.surfaceSoft,
            ) {
                if (icon != null) {
                    AsyncImage(
                        model = icon,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = row.appName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatUsageDuration(row.usageMillis),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        maxLines = 1,
                    )
                }
                LinearProgressIndicator(
                    progress = { row.usageMillis.toFloat() / totalUsageMillis.coerceAtLeast(1L).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Text(
                    text = groupDetailAppMeta(row, totalUsageMillis, scope),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun GroupRhythmPanel(
    groupData: AppGroupWithApps,
    usageByDay: List<GroupDetailDayRow>,
    periodUsageMillis: Long,
    targetMillis: Long,
    accent: Color,
) {
    val totalUsage = usageByDay.sumOf { it.usageMillis }
    val activeDays = usageByDay.count { it.usageMillis > 0L }
    val average = if (usageByDay.isNotEmpty()) totalUsage / usageByDay.size else 0L
    val best = usageByDay.maxByOrNull { it.usageMillis }
    val completedDays = usageByDay.count { day ->
        if (groupData.group.type == GroupType.CONTROL) day.usageMillis <= targetMillis else day.usageMillis >= targetMillis
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailMetricGrid(
            items = listOf(
                AppText.t("group_week_total_label") to formatUsageDuration(totalUsage),
                AppText.t("group_detail_daily_average") to formatUsageDuration(average),
                AppText.t("group_detail_best_day") to (best?.let { "${it.date.monthValue}/${it.date.dayOfMonth} · ${formatUsageDuration(it.usageMillis)}" } ?: AppText.t("stats_none")),
                AppText.t("group_complete") to AppText.t("group_value_days_4", completedDays),
                AppText.t("group_detail_current_period") to formatUsageDuration(periodUsageMillis),
                AppText.t("stats_active_days") to AppText.t("group_value_days_5", activeDays),
            ),
            accent = accent,
        )
        if (usageByDay.isEmpty()) {
            TinyVowEmptyState(
                title = AppText.t("group_detail_no_rhythm_title"),
                body = AppText.t("group_detail_no_rhythm_body"),
            )
        } else {
            usageByDay.forEach { item ->
                DetailProgressRow(
                    label = "${item.date.monthValue}/${item.date.dayOfMonth}",
                    value = formatUsageDuration(item.usageMillis),
                    progress = item.usageMillis.toFloat() / targetMillis.coerceAtLeast(1L).toFloat(),
                    accent = accent,
                )
            }
        }
    }
}

@Composable
private fun GroupHistoryPanel(
    groupData: AppGroupWithApps,
    items: List<DailyGroupArchiveEntity>,
    summary: GroupDetailArchiveSummary,
    targetMillis: Long,
    accent: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailMetricGrid(
            items = buildList {
                add(AppText.t("group_last_30_days") to AppText.t("group_value_days", summary.archivedDays))
                add(AppText.t("group_complete") to AppText.t("group_value_days_4", summary.completedDays))
                add(AppText.t("group_total_usage") to formatUsageDuration(summary.totalUsageMillis))
                if (groupData.group.type == GroupType.ENCOURAGE) {
                    add(AppText.t("group_points") to AppText.t("group_points_value", trimTrailingZero(summary.totalEarnedPoints)))
                    add(AppText.t("group_detail_average_investment") to formatUsageDuration(if (summary.archivedDays > 0) summary.totalUsageMillis / summary.archivedDays else 0L))
                } else {
                    add(AppText.t("group_blocks") to AppText.t("group_value_times", summary.blockEvents))
                    add(AppText.t("group_detail_exceeded_days") to AppText.t("group_value_days_5", summary.exceededDays))
                    add(AppText.t("group_detail_average_remaining") to formatUsageDuration(summary.averageRemainingMillis))
                    add(AppText.t("group_detail_average_over") to formatUsageDuration(summary.averageExceededMillis))
                }
            },
            accent = accent,
        )
        if (items.isEmpty()) {
            TinyVowEmptyState(
                title = AppText.t("group_no_archived_history_yet"),
                body = AppText.t("group_detail_no_history_body"),
            )
        } else {
            items.take(10).forEach { item ->
                DetailProgressRow(
                    label = item.archiveDate.substring(5),
                    value = "${formatUsageDuration(item.dailyUsageMillis)} · ${if (item.completed) AppText.t("group_completed") else AppText.t("group_status_incomplete")}",
                    progress = item.periodUsageMillisAtClose.toFloat() /
                        item.effectiveLimitMillisAtClose.coerceAtLeast(targetMillis).coerceAtLeast(1L).toFloat(),
                    accent = accent,
                )
            }
        }
    }
}

@Composable
private fun DetailMetricGrid(
    items: List<Pair<String, String>>,
    accent: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (label, value) ->
                    TinyVowMetricTile(
                        label = label,
                        value = value,
                        color = accent,
                        modifier = Modifier.weight(1f),
                    )
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
    accent: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.labelMedium,
            color = LocalThemeColors.current.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape),
            color = accent,
            trackColor = accent.copy(alpha = 0.12f),
        )
        Text(
            text = value,
            modifier = Modifier.width(116.dp),
            style = MaterialTheme.typography.labelSmall,
            color = LocalThemeColors.current.inkMuted,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun buildCurrentAppRows(
    packageNames: List<String>,
    usageMap: Map<String, Long>,
    openCountMap: Map<String, Int>,
    sessions: List<AppSession>,
    appNameByPackage: Map<String, String>,
    includeZeroUsage: Boolean,
): List<GroupDetailAppRow> {
    val packageSet = packageNames.toSet()
    val sessionsByPackage = sessions
        .filter { it.packageName in packageSet && it.endTime > it.startTime }
        .groupBy { it.packageName }
    return packageNames
        .mapNotNull { packageName ->
            val usageMillis = usageMap[packageName] ?: 0L
            if (!includeZeroUsage && usageMillis <= 0L) return@mapNotNull null
            val appSessions = sessionsByPackage[packageName].orEmpty()
            GroupDetailAppRow(
                packageName = packageName,
                appName = appNameByPackage[packageName] ?: packageName,
                usageMillis = usageMillis,
                openCount = openCountMap[packageName] ?: 0,
                sessionCount = appSessions.size,
                longestSessionMillis = appSessions.maxOfOrNull { it.endTime - it.startTime } ?: 0L,
            )
        }
        .sortedWith(
            compareByDescending<GroupDetailAppRow> { it.usageMillis }
                .thenByDescending { it.openCount }
                .thenBy { it.appName },
        )
}

private fun buildHistoryAppRows(
    items: List<DailyAppArchiveEntity>,
    appNameByPackage: Map<String, String>,
): List<GroupDetailAppRow> =
    items
        .groupBy { it.packageName }
        .map { (packageName, rows) ->
            GroupDetailAppRow(
                packageName = packageName,
                appName = rows.maxByOrNull { it.updatedAt }?.appLabel ?: appNameByPackage[packageName] ?: packageName,
                usageMillis = rows.sumOf { it.dailyUsageMillis },
                openCount = rows.sumOf { it.openCount },
                sessionCount = rows.sumOf { it.sessionCount },
                longestSessionMillis = rows.maxOfOrNull { it.longestSessionMillis } ?: 0L,
                activeDays = rows.count { it.dailyUsageMillis > 0L },
                earnedPoints = rows.sumOf { it.earnedPoints },
            )
        }
        .sortedWith(
            compareByDescending<GroupDetailAppRow> { it.usageMillis }
                .thenByDescending { it.openCount }
                .thenBy { it.appName },
        )

private fun buildGroupArchiveSummary(items: List<DailyGroupArchiveEntity>): GroupDetailArchiveSummary {
    val archivedDays = items.size
    return GroupDetailArchiveSummary(
        archivedDays = archivedDays,
        completedDays = items.count { it.completed },
        totalUsageMillis = items.sumOf { it.dailyUsageMillis },
        blockEvents = items.sumOf { it.blockEventCount },
        exceededDays = items.count { it.exceededMillisAtClose > 0L },
        totalEarnedPoints = items.sumOf { it.earnedPoints },
        averageRemainingMillis = if (archivedDays > 0) items.sumOf { it.remainingMillisAtClose.coerceAtLeast(0L) } / archivedDays else 0L,
        averageExceededMillis = if (archivedDays > 0) items.sumOf { it.exceededMillisAtClose.coerceAtLeast(0L) } / archivedDays else 0L,
    )
}

private fun groupDetailAppMeta(
    row: GroupDetailAppRow,
    totalUsageMillis: Long,
    scope: GroupDetailAppScope,
): String {
    val share = if (totalUsageMillis > 0L) {
        ((row.usageMillis.toFloat() / totalUsageMillis.toFloat()) * 100).toInt()
    } else {
        0
    }
    val base = AppText.t("group_detail_app_meta", share, row.openCount, formatUsageDuration(row.longestSessionMillis))
    return if (scope == GroupDetailAppScope.HISTORY) {
        val pointsPart =
            if (row.earnedPoints > 0.0) {
                " · ${AppText.t("group_points_value", trimTrailingZero(row.earnedPoints))}"
            } else {
                ""
            }
        "$base · ${AppText.t("stats_active_days")}: ${AppText.t("group_value_days_5", row.activeDays)}$pointsPart"
    } else {
        "$base · ${AppText.t("group_detail_sessions", row.sessionCount)}"
    }
}

private fun groupDetailTargetMillis(
    groupData: AppGroupWithApps,
    rewardSummary: GroupActiveRewardSummary,
): Long =
    if (groupData.group.type == GroupType.CONTROL) {
        (groupData.group.limitMinutes + rewardSummary.extraMinutes).coerceAtLeast(1) * 60_000L
    } else {
        groupData.group.limitMinutes.coerceAtLeast(1) * 60_000L
    }

private fun groupDetailStatusLabel(
    groupData: AppGroupWithApps,
    usageMillis: Long,
    targetMillis: Long,
    todayStepCount: Int,
): String {
    if (groupData.group.type == GroupType.ENCOURAGE && groupData.group.encourageMetric == EncourageMetric.STEPS) {
        return if (todayStepCount >= groupData.group.stepTarget) AppText.t("group_completed") else AppText.t("group_label_2")
    }
    return when {
        groupData.group.type == GroupType.CONTROL && usageMillis <= targetMillis -> AppText.t("group_status_safe")
        groupData.group.type == GroupType.CONTROL -> AppText.t("group_over_by")
        usageMillis >= targetMillis -> AppText.t("group_completed")
        else -> AppText.t("group_label_2")
    }
}

private fun groupDetailProgress(
    groupData: AppGroupWithApps,
    usageMillis: Long,
    targetMillis: Long,
    todayStepCount: Int,
): Float =
    if (groupData.group.type == GroupType.ENCOURAGE && groupData.group.encourageMetric == EncourageMetric.STEPS) {
        todayStepCount.toFloat() / groupData.group.stepTarget.coerceAtLeast(1).toFloat()
    } else {
        usageMillis.toFloat() / targetMillis.coerceAtLeast(1L).toFloat()
    }

private fun groupDetailPeriodStartMillis(
    period: LimitPeriod,
    today: LocalDate,
    zoneId: ZoneId,
    dayStartHour: Int,
): Long {
    val startDate = when (period) {
        LimitPeriod.DAILY -> today
        LimitPeriod.WEEKLY -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        LimitPeriod.MONTHLY -> today.withDayOfMonth(1)
    }
    return BusinessDay.startOfDayMillis(startDate, zoneId, dayStartHour)
}

private fun groupDetailPeriodLabel(period: LimitPeriod): String =
    when (period) {
        LimitPeriod.DAILY -> AppText.t("group_daily_short")
        LimitPeriod.WEEKLY -> AppText.t("group_weekly_short")
        LimitPeriod.MONTHLY -> AppText.t("group_monthly_short")
    }

@Composable
private fun GroupEditDialog(
    group: AppGroupWithApps?,
    forcedType: GroupType,
    installedApps: List<ManagedApp>,
    appIconCache: Map<String, Drawable>,
    onAppIconLoaded: (String, Drawable) -> Unit,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Int, GroupType, LimitPeriod, Double, List<String>) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val themeColors = LocalThemeColors.current
    val editAccent = if (forcedType == GroupType.CONTROL) themeColors.control else themeColors.encourage
    val editBackground = editAccent.copy(alpha = 0.06f).compositeOver(MaterialTheme.colorScheme.background)
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

    val nameInvalid = groupName.trim().isBlank()
    val limitInvalid = limitText.toIntOrNull()?.let { it !in 1..1440 } ?: true
    val pointRateInvalid = forcedType == GroupType.ENCOURAGE && pointRateText.toDoubleOrNull() == null
    val canSaveBase = !nameInvalid && !limitInvalid && !pointRateInvalid
    val savedPackageCount = selectedPackages.size
    val appCountAllowed = ProFeatureGate.canSaveGroupApps(isProActive, savedPackageCount)
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
        SyncDialogSystemBars(backgroundColor = editBackground)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(editBackground)
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
                            tint = editAccent
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (group != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = AppText.t("group_delete"),
                                tint = editAccent
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
                            onSave(
                                name,
                                limit,
                                forcedType,
                                selectedPeriod,
                                points,
                                selectedPackages.toList(),
                            )
                        },
                        enabled = canSaveBase
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = AppText.t("group_save"),
                            tint = if (canSaveBase) editAccent else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                UnifiedInputField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = AppText.t("group_name_placeholder"),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Text,
                    textAlign = TextAlign.Start,
                    accent = editAccent,
                )
                val validationMessage = when {
                    nameInvalid -> AppText.t("group_edit_name_required")
                    limitInvalid -> AppText.t("group_edit_limit_required")
                    pointRateInvalid -> AppText.t("group_edit_points_required")
                    else -> null
                }
                if (validationMessage != null) {
                    Text(
                        text = validationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val periodFieldWeight = if (forcedType == GroupType.ENCOURAGE) 0.9f else 1f
                    val targetFieldWeight = if (forcedType == GroupType.ENCOURAGE) 0.85f else 1f
                    val pointsFieldWeight = 1.25f
                    CompactPeriodSelector(
                        period = selectedPeriod,
                        groupType = forcedType,
                        onPeriodChange = { selectedPeriod = it },
                        modifier = Modifier.weight(periodFieldWeight),
                        accent = editAccent,
                    )
                    UnifiedInputField(
                        value = limitText,
                        onValueChange = { limitText = it.filter(Char::isDigit).take(4) },
                        placeholder = "0",
                        suffix = AppText.t("group_minutes"),
                        modifier = Modifier.weight(targetFieldWeight),
                        keyboardType = KeyboardType.Number,
                        textAlign = TextAlign.End,
                        accent = editAccent,
                    )
                    if (forcedType == GroupType.ENCOURAGE) {
                        UnifiedInputField(
                            value = pointRateText,
                            onValueChange = { pointRateText = sanitizeDecimalInput(it) },
                            placeholder = "0",
                            prefix = AppText.t("group_minutes_2"),
                            suffix = AppText.t("group_pts"),
                            modifier = Modifier.weight(pointsFieldWeight),
                            keyboardType = KeyboardType.Decimal,
                            textAlign = TextAlign.End,
                            accent = editAccent,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactFilterButton(
                        text = if (showOnlyUsedInSevenDays) AppText.t("group_active_last_7_days") else AppText.t("group_all_apps"),
                        selected = showOnlyUsedInSevenDays,
                        onClick = { showOnlyUsedInSevenDays = !showOnlyUsedInSevenDays },
                        accent = editAccent,
                    )
                    SearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        accent = editAccent,
                    )
                }

                HorizontalDivider(color = editAccent.copy(alpha = 0.24f))
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
                            icon = appIconCache[app.packageName],
                            onIconLoaded = onAppIconLoaded,
                            accent = editAccent,
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
    textAlign: TextAlign,
    accent: Color = LocalThemeColors.current.base,
) {
    FieldContainer(modifier = modifier, accent = accent) {
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
            cursorBrush = SolidColor(accent),
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
    modifier: Modifier = Modifier,
    accent: Color = LocalThemeColors.current.base,
) {
    FieldContainer(modifier = modifier, accent = accent) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = accent
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
            cursorBrush = SolidColor(accent),
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
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    fillWidth: Boolean = true,
    accent: Color = LocalThemeColors.current.base,
    content: @Composable RowScope.() -> Unit
) {
    val containerColor =
        if (selected) {
            accent.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    val border =
        if (selected) {
            BorderStroke(1.dp, accent.copy(alpha = 0.34f))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    val contentColor =
        if (selected) {
            accent
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    val fieldContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.height(CompactFieldHeight),
            shape = CompactFieldShape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            content = fieldContent,
        )
    } else {
        Surface(
            modifier = modifier.height(CompactFieldHeight),
            shape = CompactFieldShape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            content = fieldContent,
        )
    }
}

@Composable
private fun CompactFilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = LocalThemeColors.current.base,
) {
    FieldContainer(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        fillWidth = false,
        accent = accent,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) {
                accent
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AppSelectionItem(
    app: ManagedApp,
    checked: Boolean,
    icon: Drawable?,
    onIconLoaded: (String, Drawable) -> Unit,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    // 异步加载图标，避免同步 Binder IPC 阻塞主线程
    LaunchedEffect(app.packageName, icon) {
        if (icon != null) return@LaunchedEffect
        val loadedIcon = withContext(Dispatchers.IO) {
            AppVisualCache.getIcon(context, app.packageName)
        }
        if (loadedIcon != null) {
            onIconLoaded(app.packageName, loadedIcon)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (checked) accent.copy(alpha = 0.08f) else Color.Transparent)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (checked) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                fontWeight = FontWeight.SemiBold,
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
                checkedColor = accent,
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
    modifier: Modifier = Modifier,
    accent: Color = LocalThemeColors.current.base,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (period) {
        LimitPeriod.DAILY -> if (groupType == GroupType.ENCOURAGE) AppText.t("group_daily_target") else AppText.t("group_daily_limit_label")
        LimitPeriod.WEEKLY -> if (groupType == GroupType.ENCOURAGE) AppText.t("group_weekly_target") else AppText.t("group_weekly_limit_label")
        LimitPeriod.MONTHLY -> if (groupType == GroupType.ENCOURAGE) AppText.t("group_monthly_target") else AppText.t("group_monthly_limit_label")
    }

    Box(modifier = modifier) {
        FieldContainer(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = true },
            accent = accent,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = accent
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
private fun SyncDialogSystemBars(
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    val view = LocalView.current
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
