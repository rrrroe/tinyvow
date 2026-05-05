package com.rrrrz.tinyvow.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryType
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.validateCustomRewardInput
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.home.ProUpsellSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RedeemScreen(
    userPoints: Double,
    rewards: List<RedemptionEntity>,
    groups: List<AppGroupWithApps>,
    redemptionHistory: List<RedemptionHistoryEntity> = emptyList(),
    onRedeem: (RedemptionEntity, String?) -> Unit,
    onAddReward: (String, Int, Int, String) -> Unit,
    onUpdateReward: (RedemptionEntity) -> Unit,
    onArchiveReward: (RedemptionEntity) -> Unit,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReward by remember { mutableStateOf<RedemptionEntity?>(null) }
    var archivingReward by remember { mutableStateOf<RedemptionEntity?>(null) }

    val customRewards = remember(rewards) { rewards.filter { it.builtinKey == null } }
    val timePackRewards = remember(rewards) {
        rewards.filter { it.builtinKey != null && it.rewardType == RewardType.TIME_PACK }
    }
    val builtinCustomRewards = remember(rewards) {
        rewards.filter { it.builtinKey != null && it.rewardType == RewardType.CUSTOM }
    }
    val controlGroups = remember(groups) { groups.filter { it.group.type == GroupType.CONTROL } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PointsSummaryCard(userPoints = userPoints)
        }

        if (timePackRewards.isNotEmpty()) {
            item {
                RewardSectionTitle(title = AppText.t("redeem_builtin_time_packs"))
            }
            items(timePackRewards, key = { it.id }) { reward ->
                RewardItem(
                    reward = reward,
                    userPoints = userPoints,
                    controlGroups = controlGroups,
                    onRedeem = onRedeem,
                    onEdit = null,
                    onArchive = null,
                )
            }
        }

        if (builtinCustomRewards.isNotEmpty()) {
            item {
                RewardSectionTitle(title = AppText.t("redeem_builtin_offline_rewards"))
            }
            items(builtinCustomRewards, key = { it.id }) { reward ->
                RewardItem(
                    reward = reward,
                    userPoints = userPoints,
                    controlGroups = controlGroups,
                    onRedeem = onRedeem,
                    onEdit = null,
                    onArchive = null,
                )
            }
        }

        item {
            RewardSectionTitle(title = AppText.t("redeem_custom_rewards"))
        }

        if (customRewards.isEmpty()) {
            item {
                EmptyRewardsCard(text = AppText.t("redeem_custom_rewards_empty"))
            }
        } else {
            items(customRewards, key = { it.id }) { reward ->
                val customIndex = customRewards.indexOfFirst { it.id == reward.id }
                RewardItem(
                    reward = reward,
                    userPoints = userPoints,
                    controlGroups = controlGroups,
                    onRedeem = onRedeem,
                    onEdit = {
                        if (!ProFeatureGate.canEditCustomReward(isProActive, customIndex)) {
                            onShowProUpsell(ProUpsellSource.CUSTOM_REWARD)
                        } else {
                            editingReward = reward
                        }
                    },
                    onArchive = { archivingReward = reward },
                )
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    if (ProFeatureGate.canAddCustomReward(isProActive, customRewards.size)) {
                        showAddDialog = true
                    } else {
                        onShowProUpsell(ProUpsellSource.CUSTOM_REWARD)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(AppText.t("redeem_add_custom_reward"))
            }
        }

        item {
            RewardSectionTitle(title = AppText.t("redeem_more_actions"))
        }

        item {
            RewardsEntryCard(
                title = AppText.t("redeem_recent_redemptions"),
                subtitle =
                    if (redemptionHistory.isEmpty()) {
                        AppText.t("redeem_no_history_yet")
                    } else {
                        AppText.t("redeem_history_entry_summary", redemptionHistory.size)
                    },
                icon = Icons.Default.History,
                onClick = onOpenHistory,
            )
        }

        item {
            RewardsEntryCard(
                title = AppText.t("home_achievements"),
                subtitle = AppText.t("redeem_achievement_entry_summary"),
                icon = Icons.Default.EmojiEvents,
                onClick = onOpenAchievements,
            )
        }
    }

    if (showAddDialog) {
        RewardEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cost, stock, desc ->
                onAddReward(name, cost, stock, desc)
                showAddDialog = false
            },
        )
    }

    if (editingReward != null) {
        RewardEditDialog(
            reward = editingReward,
            onDismiss = { editingReward = null },
            onConfirm = { name, cost, stock, desc ->
                editingReward?.let {
                    onUpdateReward(
                        it.copy(
                            title = name,
                            pointCost = cost,
                            stock = stock,
                            description = desc,
                        )
                    )
                }
                editingReward = null
            },
        )
    }

    if (archivingReward != null) {
        AlertDialog(
            onDismissRequest = { archivingReward = null },
            title = { Text(AppText.t("redeem_archive_custom_reward")) },
            text = { Text(AppText.t("redeem_archive_custom_reward_confirmation", archivingReward!!.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        archivingReward?.let(onArchiveReward)
                        archivingReward = null
                    },
                ) {
                    Text(AppText.t("group_delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { archivingReward = null }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }
}

@Composable
fun RedemptionHistoryScreen(
    redemptionHistory: List<RedemptionHistoryEntity>,
) {
    if (redemptionHistory.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = AppText.t("redeem_no_history_yet"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(redemptionHistory, key = { it.id }) { record ->
            RedemptionHistoryItem(record = record)
        }
    }
}

@Composable
private fun PointsSummaryCard(userPoints: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .background(
                        brush =
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                )
                            ),
                        shape = RoundedCornerShape(24.dp),
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            Text(
                text = AppText.t("redeem_current_points"),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.82f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "%.1f PT".format(userPoints),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun RewardSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun EmptyRewardsCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RewardsEntryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RewardItem(
    reward: RedemptionEntity,
    userPoints: Double,
    controlGroups: List<AppGroupWithApps>,
    onRedeem: (RedemptionEntity, String?) -> Unit,
    onEdit: (() -> Unit)?,
    onArchive: (() -> Unit)?,
) {
    var showGroupPicker by remember { mutableStateOf(false) }
    val missingControlGroup =
        reward.rewardType == RewardType.TIME_PACK && controlGroups.isEmpty()
    val canAfford = userPoints >= reward.pointCost
    val inStock = reward.stock == -1 || reward.stock > 0
    val canRedeem = canAfford && inStock && !missingControlGroup

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RewardIcon(reward = reward)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reward.localizedTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    val description = reward.localizedDescription()
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RewardMetaText(reward = reward)
                    Text(
                        text =
                            if (reward.stock == -1) {
                                AppText.t("redeem_stock_unlimited")
                            } else {
                                AppText.t("redeem_stock_left_value", reward.stock)
                            },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (missingControlGroup) {
                        Text(
                            text = AppText.t("redeem_no_control_group_for_time_pack"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                if (onEdit != null && onArchive != null) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = AppText.t("redeem_edit_reward"),
                            )
                        }
                        IconButton(onClick = onArchive) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = AppText.t("redeem_archive_custom_reward"),
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (reward.rewardType == RewardType.TIME_PACK) {
                        showGroupPicker = true
                    } else {
                        onRedeem(reward, null)
                    }
                },
                enabled = canRedeem,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(AppText.t("redeem_action_redeem", reward.pointCost))
            }
        }
    }

    if (showGroupPicker) {
        AlertDialog(
            onDismissRequest = { showGroupPicker = false },
            title = { Text(AppText.t("redeem_choose_target_group")) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = AppText.t("redeem_time_pack_period_hint"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    controlGroups
                        .sortedWith(
                            compareBy<AppGroupWithApps> { it.group.limitPeriod.ordinal }
                                .thenBy { it.group.sortOrder }
                                .thenBy { it.group.name },
                        )
                        .groupBy { it.group.limitPeriod }
                        .forEach { (period, groupsForPeriod) ->
                            Text(
                                text = timePackPeriodSectionTitle(period),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            groupsForPeriod.forEach { group ->
                                TimePackTargetGroupRow(
                                    group = group,
                                    period = period,
                                    onClick = {
                                        onRedeem(reward, group.group.id)
                                        showGroupPicker = false
                                    },
                                )
                            }
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupPicker = false }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }
}

@Composable
private fun RewardIcon(reward: RedemptionEntity) {
    val icon =
        if (reward.rewardType == RewardType.TIME_PACK) {
            Icons.Default.Timer
        } else {
            Icons.Default.EmojiEvents
        }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun RewardMetaText(reward: RedemptionEntity) {
    val text =
        when (reward.rewardType) {
            RewardType.TIME_PACK -> AppText.t("redeem_time_capsule_value_minutes", reward.bonusMinutes)
            RewardType.CUSTOM -> AppText.t("redeem_offline_reward")
        }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun TimePackTargetGroupRow(
    group: AppGroupWithApps,
    period: LimitPeriod,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = group.group.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = AppText.t(
                    "redeem_group_period_limit_apps",
                    timePackPeriodLabel(period),
                    group.group.limitMinutes,
                    group.packageNames.size,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = timePackExpiryDescription(period),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun timePackPeriodSectionTitle(period: LimitPeriod): String =
    AppText.t(
        when (period) {
            LimitPeriod.DAILY -> "redeem_daily_time_pack_groups"
            LimitPeriod.WEEKLY -> "redeem_weekly_time_pack_groups"
            LimitPeriod.MONTHLY -> "redeem_monthly_time_pack_groups"
        }
    )

private fun timePackPeriodLabel(period: LimitPeriod): String =
    AppText.t(
        when (period) {
            LimitPeriod.DAILY -> "group_daily"
            LimitPeriod.WEEKLY -> "group_weekly"
            LimitPeriod.MONTHLY -> "group_monthly"
        }
    )

private fun timePackExpiryDescription(period: LimitPeriod): String =
    AppText.t(
        when (period) {
            LimitPeriod.DAILY -> "redeem_time_pack_expiry_daily"
            LimitPeriod.WEEKLY -> "redeem_time_pack_expiry_weekly"
            LimitPeriod.MONTHLY -> "redeem_time_pack_expiry_monthly"
        }
    )

@Composable
fun RewardEditDialog(
    reward: RedemptionEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, String) -> Unit,
) {
    var title by remember { mutableStateOf(reward?.title ?: "") }
    var cost by remember { mutableStateOf(reward?.pointCost?.toString() ?: "100") }
    var stock by remember { mutableStateOf(reward?.stock?.takeIf { it > 0 }?.toString() ?: "1") }
    var description by remember { mutableStateOf(reward?.description ?: "") }
    var isInfinite by remember { mutableStateOf(reward?.stock == -1) }
    var showErrors by remember { mutableStateOf(false) }

    val costValue = cost.toIntOrNull() ?: 0
    val stockValue = if (isInfinite) -1 else stock.toIntOrNull() ?: 0
    val validationError =
        validateCustomRewardInput(
            title = title,
            pointCost = costValue,
            stock = stockValue,
        )
    val titleError = showErrors && title.isBlank()
    val costError = showErrors && validationError == com.rrrrz.tinyvow.data.repository.RewardSaveValidationError.POINT_COST_INVALID
    val stockError = showErrors && validationError == com.rrrrz.tinyvow.data.repository.RewardSaveValidationError.STOCK_INVALID

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (reward == null) AppText.t("redeem_add_custom_reward") else AppText.t("redeem_edit_reward")
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppText.t("redeem_item_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text(AppText.t("redeem_error_title_required"))
                        }
                    },
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text(AppText.t("redeem_required_points")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = costError,
                    supportingText = {
                        if (costError) {
                            Text(AppText.t("redeem_error_point_cost_invalid"))
                        }
                    },
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isInfinite, onCheckedChange = { isInfinite = it })
                    Text(AppText.t("redeem_unlimited_stock"), style = MaterialTheme.typography.bodyMedium)
                }

                if (!isInfinite) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text(AppText.t("redeem_stock_quantity")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = stockError,
                        supportingText = {
                            if (stockError) {
                                Text(AppText.t("redeem_error_stock_invalid"))
                            }
                        },
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(AppText.t("redeem_description_optional")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showErrors = true
                    if (validationError == null) {
                        onConfirm(title.trim(), costValue, stockValue, description.trim())
                    }
                },
            ) {
                Text(if (reward == null) AppText.t("redeem_add") else AppText.t("group_save"))
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
private fun RedemptionHistoryItem(record: RedemptionHistoryEntity) {
    val dateFormatter = remember {
        SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    }
    val subtitle =
        when (record.historyType) {
            RedemptionHistoryType.TIME_PACK -> {
                val groupName = record.targetGroupName ?: AppText.t("redeem_target_group")
                AppText.t("redeem_value_value_minutes", groupName, record.bonusMinutes)
            }
            RedemptionHistoryType.CUSTOM -> AppText.t("redeem_custom_reward")
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.localizedRewardTitle(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = dateFormatter.format(Date(record.redeemedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "-${record.pointCost} PT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun RedemptionEntity.localizedTitle(): String =
    builtinKey?.let { AppText.t("${it}_title") } ?: title

private fun RedemptionEntity.localizedDescription(): String =
    builtinKey?.let { AppText.t("${it}_description") } ?: description

private fun RedemptionHistoryEntity.localizedRewardTitle(): String =
    rewardBuiltinKey?.let { AppText.t("${it}_title") } ?: rewardTitle
