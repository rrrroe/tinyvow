package com.rrrrz.tinyvow.ui.home

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.ColorDrawable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps

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
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<AppGroupWithApps?>(null) }
    var forcedType by remember { mutableStateOf(GroupType.CONTROL) }

    val controlGroups = remember(groupsWithApps) {
        groupsWithApps.filter { it.group.type == GroupType.CONTROL }
    }
    val encourageGroups = remember(groupsWithApps) {
        groupsWithApps.filter { it.group.type == GroupType.ENCOURAGE }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(
            title = "小约定",
            subtitle = "管理限制类应用",
            groups = controlGroups,
            usageMap = usageMap,
            accent = MaterialTheme.colorScheme.secondary,
            onAdd = {
                editingGroup = null
                forcedType = GroupType.CONTROL
                showDialog = true
            },
            onEdit = {
                editingGroup = it
                forcedType = it.group.type
                showDialog = true
            }
        )

        SectionCard(
            title = "小鼓励",
            subtitle = "完成目标后获得积分",
            groups = encourageGroups,
            usageMap = usageMap,
            accent = MaterialTheme.colorScheme.tertiary,
            onAdd = {
                editingGroup = null
                forcedType = GroupType.ENCOURAGE
                showDialog = true
            },
            onEdit = {
                editingGroup = it
                forcedType = it.group.type
                showDialog = true
            }
        )

        if (isLoadingApps) {
            Text(
                text = "正在加载应用列表…",
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
            onDismiss = { showDialog = false },
            onSave = { name, limit, type, period, points, packages ->
                onSaveGroup(editingGroup?.group?.id, name, limit, type, period, points, packages)
                showDialog = false
            },
            onDelete = {
                editingGroup?.group?.id?.let(onDeleteGroup)
                showDialog = false
            }
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
    onEdit: (AppGroupWithApps) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onAdd) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "新增分组",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (groups.isEmpty()) {
                Text(
                    text = "暂无分组",
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
                        onClick = { onEdit(item) }
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
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val periodLabel = when (groupData.group.limitPeriod) {
        LimitPeriod.DAILY -> "每日"
        LimitPeriod.WEEKLY -> "每周"
        LimitPeriod.MONTHLY -> "每月"
    }
    val detailText = if (groupData.group.type == GroupType.ENCOURAGE) {
        "$periodLabel ${usedMinutes}/${groupData.group.limitMinutes}分 · ${trimTrailingZero(groupData.group.pointsPerMinute)} 积分/分"
    } else {
        "$periodLabel ${usedMinutes}/${groupData.group.limitMinutes}分"
    }
    val rawProgress = if (groupData.group.limitMinutes > 0) {
        usedMinutes.toFloat() / groupData.group.limitMinutes.toFloat()
    } else {
        0f
    }
    val progress = rawProgress.coerceIn(0f, 1f)
    val progressColor = if (rawProgress >= 1f) MaterialTheme.colorScheme.error else accent
    val iconPackages = groupData.packageNames.take(5)
    val iconSize = 34
    val iconOffset = 17
    val maxIcons = 5
    val iconWidth = iconSize + (maxIcons - 1) * iconOffset

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onClick)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.width(iconWidth.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                iconPackages.forEachIndexed { index, packageName ->
                    val icon = remember(packageName) {
                        runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
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

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = groupData.group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
            targetValue = progress,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessVeryLow
            ),
            label = "progress"
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f))
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


@Composable
private fun GroupEditDialog(
    group: AppGroupWithApps?,
    forcedType: GroupType,
    installedApps: List<ManagedApp>,
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
                "1"
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

    val canSave = groupName.trim().isNotBlank() &&
        (limitText.toIntOrNull()?.coerceIn(1, 1440) != null) &&
        (forcedType != GroupType.ENCOURAGE || pointRateText.toDoubleOrNull() != null)

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
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (group != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            val name = groupName.trim()
                            val limit = limitText.toIntOrNull()?.coerceIn(1, 1440) ?: return@IconButton
                            val points = if (forcedType == GroupType.ENCOURAGE) {
                                pointRateText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: return@IconButton
                            } else {
                                0.0
                            }
                            onSave(name, limit, forcedType, selectedPeriod, points, selectedPackages.toList())
                        },
                        enabled = canSave
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "保存",
                            tint = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                UnifiedInputField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = "分组名称（如：游戏、视频）",
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
                        suffix = "分钟",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number,
                        textAlign = TextAlign.End
                    )
                    if (forcedType == GroupType.ENCOURAGE) {
                        UnifiedInputField(
                            value = pointRateText,
                            onValueChange = { pointRateText = sanitizeDecimalInput(it) },
                            placeholder = "0",
                            prefix = "每分钟",
                            suffix = "积分",
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
                            text = if (showOnlyUsedInSevenDays) "近7天活跃" else "全部应用",
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
                                    selectedPackages + app.packageName
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
            title = { Text("删除分组") },
            text = { Text("删除后该分组配置会被移除，是否继续？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
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
                            text = "搜索应用",
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
    val icon = remember(app.packageName) {
        runCatching { context.packageManager.getApplicationIcon(app.packageName) }.getOrNull()
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
        LimitPeriod.DAILY -> if (groupType == GroupType.ENCOURAGE) "每日目标" else "每日限额"
        LimitPeriod.WEEKLY -> if (groupType == GroupType.ENCOURAGE) "每周目标" else "每周限额"
        LimitPeriod.MONTHLY -> if (groupType == GroupType.ENCOURAGE) "每月目标" else "每月限额"
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
                    LimitPeriod.DAILY -> if (groupType == GroupType.ENCOURAGE) "每日目标" else "每日限额"
                    LimitPeriod.WEEKLY -> if (groupType == GroupType.ENCOURAGE) "每周目标" else "每周限额"
                    LimitPeriod.MONTHLY -> if (groupType == GroupType.ENCOURAGE) "每月目标" else "每月限额"
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
    if (durationMillis <= 0L) return "<1m"
    val totalMinutes = durationMillis / 60_000L
    if (totalMinutes <= 0L) return "<1m"
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
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
