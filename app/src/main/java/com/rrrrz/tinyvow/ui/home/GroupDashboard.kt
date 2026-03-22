package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps

import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod

@Composable
fun GroupDashboard(
    groupsWithApps: List<AppGroupWithApps>,
    installedApps: List<ManagedApp>,
    isLoadingApps: Boolean,
    onSaveGroup: (id: String?, name: String, limitMinutes: Int, type: GroupType, period: LimitPeriod, pts: Double, pkgs: List<String>) -> Unit,
    onDeleteGroup: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<AppGroupWithApps?>(null) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.group_dashboard_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (isLoadingApps) {
                    Spacer(modifier = Modifier.width(12.dp))
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
            FilledTonalButton(onClick = {
                editingGroup = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.group_create_action))
            }
        }

        if (groupsWithApps.isEmpty()) {
            Text(
                text = stringResource(R.string.group_card_empty),
                modifier = Modifier.padding(top = 24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 500.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gridItems(groupsWithApps, key = { it.group.id }) { groupData ->
                    GroupCard(
                        groupData = groupData,
                        onClick = {
                            editingGroup = groupData
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        GroupEditDialog(
            initialGroup = editingGroup,
            installedApps = installedApps,
            onDismiss = { showDialog = false },
            onSave = { name, limitMinutes, type, period, ptsRate, packages ->
                onSaveGroup(editingGroup?.group?.id, name, limitMinutes, type, period, ptsRate, packages)
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
private fun GroupCard(
    groupData: AppGroupWithApps,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = groupData.group.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = stringResource(
                    R.string.group_card_summary,
                    groupData.packageNames.size,
                    groupData.group.limitMinutes
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupEditDialog(
    initialGroup: AppGroupWithApps?,
    installedApps: List<ManagedApp>,
    onDismiss: () -> Unit,
    onSave: (String, Int, GroupType, LimitPeriod, Double, List<String>) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(initialGroup?.group?.name ?: "") }
    var limitMinutes by remember { mutableFloatStateOf(initialGroup?.group?.limitMinutes?.toFloat() ?: 60f) }
    var selectedPackages by remember { mutableStateOf(initialGroup?.packageNames?.toSet() ?: emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf(initialGroup?.group?.type ?: GroupType.CONTROL) }
    var limitPeriod by remember { mutableStateOf(initialGroup?.group?.limitPeriod ?: LimitPeriod.DAILY) }
    var pointsPerMinute by remember { mutableFloatStateOf(initialGroup?.group?.pointsPerMinute?.toFloat() ?: 1f) }

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
                    if (initialGroup != null) {
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

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.group_limit_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${limitMinutes.toInt()} min",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = limitMinutes,
                        onValueChange = { limitMinutes = it },
                        valueRange = 5f..300f,
                        steps = 59
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
