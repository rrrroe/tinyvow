package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps

import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip

@Composable
fun GroupDashboard(
    groupsWithApps: List<AppGroupWithApps>,
    usageMap: Map<String, Long>,
    isLoadingApps: Boolean,
    installedApps: List<ManagedApp>,
    onSaveGroup: (id: String?, name: String, limit: Int, type: GroupType, period: LimitPeriod, pts: Double, pkgs: List<String>) -> Unit,
    onDeleteGroup: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<AppGroupWithApps?>(null) }
    var forcedType by remember { mutableStateOf(GroupType.CONTROL) }

    val controlGroups = remember(groupsWithApps) { groupsWithApps.filter { it.group.type == GroupType.CONTROL } }
    val encourageGroups = remember(groupsWithApps) { groupsWithApps.filter { it.group.type == GroupType.ENCOURAGE } }

    Column(modifier = modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Little Vow Section
        SectionCard(
            title = "我的小约定",
            subtitle = "限制类的应用管控",
            groups = controlGroups,
            onEdit = { 
                editingGroup = it
                forcedType = it.group.type
                showDialog = true 
            },
            onAdd = {
                editingGroup = null
                forcedType = GroupType.CONTROL
                showDialog = true
            },
            onDelete = onDeleteGroup,
            usageMap = usageMap
        )

        // Little Encouragement Section
        SectionCard(
            title = "我的小鼓励",
            subtitle = "坚持使用可获积分",
            groups = encourageGroups,
            onEdit = { 
                editingGroup = it
                forcedType = it.group.type
                showDialog = true 
            },
            onAdd = {
                editingGroup = null
                forcedType = GroupType.ENCOURAGE
                showDialog = true
            },
            onDelete = onDeleteGroup,
            isBooster = true,
            usageMap = usageMap
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showDialog) {
        GroupEditDialog(
            group = editingGroup,
            forcedType = forcedType,
            installedApps = installedApps,
            onDismiss = { showDialog = false },
            onSave = { name, limit, type, period, pts, pkgs ->
                onSaveGroup(editingGroup?.group?.id, name, limit, type, period, pts, pkgs)
                showDialog = false
            },
            onDelete = {
                editingGroup?.group?.id?.let { onDeleteGroup(it) }
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
    onEdit: (AppGroupWithApps) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit,
    usageMap: Map<String, Long>,
    isBooster: Boolean = false
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (groups.isEmpty()) {
                Text(
                    "暂未设置任何计划",
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                groups.forEach { groupData ->
                    GroupCard(
                        groupData = groupData,
                        usedMinutes = (usageMap[groupData.group.id] ?: 0L) / 60_000L,
                        onClick = { onEdit(groupData) },
                        onDelete = { onDelete(groupData.group.id) },
                        color = if (isBooster) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                    )
                    if (groupData != groups.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupCard(
    groupData: AppGroupWithApps,
    usedMinutes: Long,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    color: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Overlap Icons (Max 4 for better balanced look)
        Box(modifier = Modifier.size(width = 80.dp, height = 40.dp), contentAlignment = Alignment.CenterStart) {
            groupData.packageNames.take(4).forEachIndexed { index, pkg ->
                val iconPainter = remember(pkg) {
                    try { context.packageManager.getApplicationIcon(pkg) } catch (_: Exception) { null }
                }
                if (iconPainter != null) {
                    Surface(
                        modifier = Modifier
                            .offset(x = (index * 16).dp)
                            .size(36.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                        shadowElevation = 2.dp
                    ) {
                        AsyncImage(model = iconPainter, contentDescription = null)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Side: Info and Progress
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = groupData.group.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                val progress = if (groupData.group.limitMinutes > 0) {
                    (usedMinutes.toFloat() / groupData.group.limitMinutes).coerceIn(0f, 1.2f)
                } else 0f
                
                LinearProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier
                        .width(60.dp)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${usedMinutes}/${groupData.group.limitMinutes} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (usedMinutes >= groupData.group.limitMinutes) MaterialTheme.colorScheme.error else color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupEditDialog(
    group: AppGroupWithApps?,
    forcedType: GroupType,
    installedApps: List<ManagedApp>,
    onDismiss: () -> Unit,
    onSave: (name: String, limit: Int, type: GroupType, period: LimitPeriod, pts: Double, pkgs: List<String>) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(group?.group?.name ?: "") }
    var limitMinutes by remember { mutableFloatStateOf(group?.group?.limitMinutes?.toFloat() ?: 60f) }
    var selectedPackages by remember { mutableStateOf(group?.packageNames?.toSet() ?: emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf(group?.group?.type ?: forcedType) }
    var limitPeriod by remember { mutableStateOf(group?.group?.limitPeriod ?: LimitPeriod.DAILY) }
    var pointsPerMinute by remember { mutableFloatStateOf(group?.group?.pointsPerMinute?.toFloat() ?: 1f) }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { 
            it.appName.contains(searchQuery, ignoreCase = true) || 
            it.packageName.contains(searchQuery, ignoreCase = true) 
        }
    }

    // 计算最大时长用于进度条
    val maxUsageTime = remember(installedApps) { 
        installedApps.maxOfOrNull { it.usageTimeInMs }?.coerceAtLeast(1L) ?: 1L 
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f) // 占据 90% 高度，避免由于空间不足导致的局促感
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 顶部操作栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.group_edit_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (group != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.group_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // 分组类型已锁定为：${if (groupType == GroupType.CONTROL) "小约定" else "小鼓励"}
                

                if (groupType == GroupType.ENCOURAGE) {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "鼓励金速率",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%.1f PT / min", pointsPerMinute),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            "坚持使用小鼓励内的 App，每分钟可获得相应积分奖励。",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = pointsPerMinute,
                            onValueChange = { pointsPerMinute = it },
                            valueRange = 0.5f..5f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.tertiary,
                                activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                inactiveTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (groupType == GroupType.CONTROL) "每日限额" else "今日达成目标",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${limitMinutes.toInt()} min",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = if (groupType == GroupType.CONTROL) "达到此时长后，系统将弹出阻断层引导您放下手机。" 
                               else "每日使用达标可获大奖：${(limitMinutes * pointsPerMinute).toInt()} 积分！建议作为您的专注动力。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = limitMinutes,
                        onValueChange = { limitMinutes = it },
                        valueRange = 10f..300f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }

                // 搜索框 (仿 reference 设计)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    }
                )

                // 列表头
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "App 名称",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "周使用时间",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // 应用选择列表 (高密度布局)
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppSelectionItem(
                            app = app,
                            isSelected = selectedPackages.contains(app.packageName),
                            maxUsageTime = maxUsageTime,
                            onToggle = {
                                selectedPackages = if (selectedPackages.contains(app.packageName)) {
                                    selectedPackages - app.packageName
                                } else {
                                    selectedPackages + app.packageName
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onSave(name, limitMinutes.toInt(), groupType, limitPeriod, pointsPerMinute.toDouble(), selectedPackages.toList()) },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(stringResource(R.string.action_save), modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSelectionItem(
    app: ManagedApp,
    isSelected: Boolean,
    maxUsageTime: Long,
    onToggle: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val iconPainter = remember(app.packageName) {
        try { context.packageManager.getApplicationIcon(app.packageName) } catch (_: Exception) { null }
    }
    
    // 格式化时长
    val formattedTime = remember(app.usageTimeInMs) {
        val seconds = (app.usageTimeInMs / 1000) % 60
        val minutes = (app.usageTimeInMs / (1000 * 60)) % 60
        val hours = (app.usageTimeInMs / (1000 * 60 * 60))
        
        buildString {
            if (hours > 0) append("${hours}小时")
            if (minutes > 0 || hours > 0) append("${minutes}分")
            append("${seconds}秒")
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp)
    ) {
        // 图标
        if (iconPainter != null) {
            AsyncImage(
                model = iconPainter,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
            )
        } else {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (app.isLaunchable) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // 进度条可视化时长
            LinearProgressIndicator(
                progress = { (app.usageTimeInMs.toFloat() / maxUsageTime.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Checkbox(
            checked = isSelected,
            onCheckedChange = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
