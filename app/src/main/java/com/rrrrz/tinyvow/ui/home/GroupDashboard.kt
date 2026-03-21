package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps

@Composable
fun GroupDashboard(
    groupsWithApps: List<AppGroupWithApps>,
    installedApps: List<ManagedApp>,
    isLoadingApps: Boolean,
    onSaveGroup: (id: String?, name: String, limitMinutes: Int, packageNames: List<String>) -> Unit,
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
            Text(
                text = stringResource(R.string.group_dashboard_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
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
                items(groupsWithApps, key = { it.group.id }) { groupData ->
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
            onSave = { name, limitMinutes, packages ->
                onSaveGroup(editingGroup?.group?.id, name, limitMinutes, packages)
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
                    groupData.group.dailyLimitMinutes
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
    onSave: (String, Int, List<String>) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(initialGroup?.group?.name ?: "") }
    var limitMinutes by remember { mutableFloatStateOf(initialGroup?.group?.dailyLimitMinutes?.toFloat() ?: 60f) }
    var selectedPackages by remember { mutableStateOf(initialGroup?.packageNames?.toSet() ?: emptySet()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.group_edit_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (initialGroup != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.group_delete_action),
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
                    singleLine = true
                )

                Column {
                    Text(
                        text = stringResource(R.string.group_limit_label) + ": ${limitMinutes.toInt()} min",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = limitMinutes,
                        onValueChange = { limitMinutes = it },
                        valueRange = 5f..300f,
                        steps = 59
                    )
                }

                Text(
                    text = stringResource(R.string.group_apps_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                // Multi-select list of apps
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    installedApps.forEach { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPackages = if (isSelected) {
                                        selectedPackages - app.packageName
                                    } else {
                                        selectedPackages + app.packageName
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, limitMinutes.toInt(), selectedPackages.toList()) },
                        enabled = name.isNotBlank()
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }
        }
    }
}
