package com.rrrrz.tinyvow.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LockClock
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
import com.rrrrz.tinyvow.data.db.RewardUseHistoryEntity
import com.rrrrz.tinyvow.data.db.StreakShieldTarget
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.InventoryRewardItem
import com.rrrrz.tinyvow.data.repository.PendingStreakShieldItem
import com.rrrrz.tinyvow.data.repository.RewardSaveValidationError
import com.rrrrz.tinyvow.data.repository.RewardStoreItem
import com.rrrrz.tinyvow.data.repository.parseRewardPayload
import com.rrrrz.tinyvow.data.repository.validateCustomRewardInput
import com.rrrrz.tinyvow.data.supermode.GuardedAction
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.home.ProUpsellSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class InventorySubsection {
    ITEMS,
    PURCHASES,
    USES,
}

@Composable
fun RedeemScreen(
    userPoints: Double,
    storeItems: List<RewardStoreItem>,
    groups: List<AppGroupWithApps>,
    onPurchase: (RedemptionEntity) -> Unit,
    onAddReward: (String, Int, Int, String) -> Unit,
    onUpdateReward: (RedemptionEntity) -> Unit,
    onArchiveReward: (RedemptionEntity) -> Unit,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onGuardAction: (GuardedAction, () -> Unit) -> Unit,
) {
    var showConfigPage by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReward by remember { mutableStateOf<RedemptionEntity?>(null) }
    var archivingReward by remember { mutableStateOf<RedemptionEntity?>(null) }
    val customRewards = remember(storeItems) { storeItems.filter { it.reward.builtinKey == null } }
    val controlGroups = remember(groups) { groups.count { it.group.type == GroupType.CONTROL } }
    val encourageGroups = remember(groups) { groups.count { it.group.type == GroupType.ENCOURAGE } }
    val primaryItems = remember(storeItems) {
        storeItems.filter {
            it.reward.rewardType == RewardType.TIME_ADD ||
                it.reward.rewardType == RewardType.PERIOD_PASS ||
                it.reward.rewardType == RewardType.EMERGENCY_UNLOCK
        }
    }
    val streakItems = remember(storeItems) { storeItems.filter { it.reward.rewardType == RewardType.STREAK_SHIELD } }
    val pointItems = remember(storeItems) { storeItems.filter { it.reward.rewardType == RewardType.DOUBLE_POINTS_DAY } }

    if (showConfigPage) {
        RewardConfigScreen(
            storeItems = storeItems,
            customRewards = customRewards,
            isProActive = isProActive,
            onBack = { showConfigPage = false },
            onAddCustomReward = {
                if (ProFeatureGate.canAddCustomReward(isProActive, customRewards.size)) {
                    onGuardAction(GuardedAction.ADD_CUSTOM_REWARD) {
                        showAddDialog = true
                    }
                } else {
                    onShowProUpsell(ProUpsellSource.CUSTOM_REWARD)
                }
            },
            onEditReward = { reward ->
                if (reward.builtinKey == null) {
                    val customIndex = customRewards.indexOfFirst { it.reward.id == reward.id }
                    if (!ProFeatureGate.canEditCustomReward(isProActive, customIndex)) {
                        onShowProUpsell(ProUpsellSource.CUSTOM_REWARD)
                    } else {
                        onGuardAction(GuardedAction.EDIT_CUSTOM_REWARD) {
                            editingReward = reward
                        }
                    }
                } else {
                    onGuardAction(GuardedAction.EDIT_REWARD_PRICE) {
                        editingReward = reward
                    }
                }
            },
            onArchiveReward = { archivingReward = it },
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { CompactPointsSummaryCard(userPoints = userPoints) }

            if (primaryItems.isNotEmpty()) {
                item { RewardSectionTitle(title = AppText.t("redeem_store_group_more_time")) }
                items(primaryItems, key = { it.reward.id }) { item ->
                    StoreRewardItemCard(
                        item = item,
                        userPoints = userPoints,
                        controlGroupCount = controlGroups,
                        encourageGroupCount = encourageGroups,
                        onPurchase = {
                            val action = GuardedAction.fromRewardType(item.reward.rewardType)
                            if (action == null) {
                                onPurchase(item.reward)
                            } else {
                                onGuardAction(action) { onPurchase(item.reward) }
                            }
                        },
                        onEdit = null,
                        onArchive = null,
                    )
                }
            }

            if (streakItems.isNotEmpty()) {
                item { RewardSectionTitle(title = AppText.t("redeem_store_group_keep_progress")) }
                items(streakItems, key = { it.reward.id }) { item ->
                    StoreRewardItemCard(
                        item = item,
                        userPoints = userPoints,
                        controlGroupCount = controlGroups,
                        encourageGroupCount = encourageGroups,
                        onPurchase = { onPurchase(item.reward) },
                        onEdit = null,
                        onArchive = null,
                    )
                }
            }

            if (pointItems.isNotEmpty()) {
                item { RewardSectionTitle(title = AppText.t("redeem_store_group_more_points")) }
                items(pointItems, key = { it.reward.id }) { item ->
                    StoreRewardItemCard(
                        item = item,
                        userPoints = userPoints,
                        controlGroupCount = controlGroups,
                        encourageGroupCount = encourageGroups,
                        onPurchase = { onPurchase(item.reward) },
                        onEdit = null,
                        onArchive = null,
                    )
                }
            }

            item { RewardSectionTitle(title = AppText.t("redeem_custom_rewards")) }
            if (customRewards.isEmpty()) {
                item { EmptyRewardsCard(text = AppText.t("redeem_custom_rewards_empty")) }
            } else {
                items(customRewards, key = { it.reward.id }) { item ->
                    StoreRewardItemCard(
                        item = item,
                        userPoints = userPoints,
                        controlGroupCount = controlGroups,
                        encourageGroupCount = encourageGroups,
                        onPurchase = { onPurchase(item.reward) },
                        onEdit = null,
                        onArchive = null,
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { showConfigPage = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(AppText.t("redeem_custom_config"))
                }
            }
        }
    }

    if (showAddDialog) {
        RewardEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cost, stock, desc ->
                onGuardAction(GuardedAction.ADD_CUSTOM_REWARD) {
                    onAddReward(name, cost, stock, desc)
                    showAddDialog = false
                }
            },
        )
    }

    editingReward?.let { reward ->
        RewardEditDialog(
            reward = reward,
            onDismiss = { editingReward = null },
            onConfirm = { name, cost, stock, desc ->
                val updatedReward =
                    if (reward.builtinKey != null) {
                        reward.copy(pointCost = cost)
                    } else {
                        reward.copy(
                            title = name,
                            pointCost = cost,
                            stock = stock,
                            description = desc,
                        )
                    }
                val action =
                    if (reward.builtinKey == null) {
                        GuardedAction.EDIT_CUSTOM_REWARD
                    } else {
                        GuardedAction.EDIT_REWARD_PRICE
                    }
                val saveBlock = {
                    onUpdateReward(updatedReward)
                    editingReward = null
                }
                onGuardAction(action, saveBlock)
            },
        )
    }

    archivingReward?.let { reward ->
        AlertDialog(
            onDismissRequest = { archivingReward = null },
            title = { Text(AppText.t("redeem_archive_custom_reward")) },
            text = { Text(AppText.t("redeem_archive_custom_reward_confirmation", reward.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        onArchiveReward(reward)
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
private fun RewardConfigScreen(
    storeItems: List<RewardStoreItem>,
    customRewards: List<RewardStoreItem>,
    isProActive: Boolean,
    onBack: () -> Unit,
    onAddCustomReward: () -> Unit,
    onEditReward: (RedemptionEntity) -> Unit,
    onArchiveReward: (RedemptionEntity) -> Unit,
) {
    val builtinItems = remember(storeItems) { storeItems.filter { it.reward.builtinKey != null } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("group_back"))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppText.t("redeem_custom_config_title"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = AppText.t("redeem_custom_config_hint"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item { RewardSectionTitle(title = AppText.t("redeem_builtin_reward_prices")) }
        items(builtinItems, key = { it.reward.id }) { item ->
            RewardConfigItemCard(
                reward = item.reward,
                subtitle = AppText.t("redeem_config_builtin_subtitle", item.reward.pointCost),
                primaryActionLabel = AppText.t("redeem_edit_builtin_reward_cost"),
                onPrimaryAction = { onEditReward(item.reward) },
            )
        }

        item { RewardSectionTitle(title = AppText.t("redeem_custom_rewards")) }
        item {
            OutlinedButton(
                onClick = onAddCustomReward,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(AppText.t("redeem_add_custom_reward"))
            }
        }
        if (customRewards.isEmpty()) {
            item { EmptyRewardsCard(text = AppText.t("redeem_custom_rewards_empty")) }
        } else {
            items(customRewards, key = { it.reward.id }) { item ->
                val customIndex = customRewards.indexOfFirst { it.reward.id == item.reward.id }
                val canEdit = ProFeatureGate.canEditCustomReward(isProActive, customIndex)
                RewardConfigItemCard(
                    reward = item.reward,
                    subtitle = AppText.t("redeem_config_custom_subtitle", item.reward.pointCost),
                    primaryActionLabel = AppText.t("redeem_edit_reward"),
                    onPrimaryAction = { onEditReward(item.reward) },
                    primaryEnabled = canEdit,
                    secondaryActionLabel = AppText.t("redeem_archive_custom_reward"),
                    onSecondaryAction = { onArchiveReward(item.reward) },
                )
            }
        }
    }
}

@Composable
fun RewardInventoryScreen(
    inventoryItems: List<InventoryRewardItem>,
    pendingItems: List<PendingStreakShieldItem>,
    groups: List<AppGroupWithApps>,
    redemptionHistory: List<RedemptionHistoryEntity>,
    rewardUseHistory: List<RewardUseHistoryEntity>,
    onUseReward: (RedemptionEntity, String?) -> Unit,
    onResolvePending: (String, Boolean) -> Unit,
) {
    var subsection by remember { mutableStateOf(InventorySubsection.ITEMS) }
    var useReward by remember { mutableStateOf<RedemptionEntity?>(null) }
    var useTargetType by remember { mutableStateOf<GroupType?>(null) }
    val controlGroups = remember(groups) { groups.filter { it.group.type == GroupType.CONTROL } }
    val encourageGroups = remember(groups) { groups.filter { it.group.type == GroupType.ENCOURAGE } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SubsectionSwitcher(
                current = subsection,
                onChange = { subsection = it },
            )
        }

        when (subsection) {
            InventorySubsection.ITEMS -> {
                if (pendingItems.isNotEmpty()) {
                    item { RewardSectionTitle(title = AppText.t("redeem_inventory_pending_title")) }
                    items(pendingItems, key = { it.pending.id }) { item ->
                        PendingShieldCard(
                            item = item,
                            onUse = { onResolvePending(item.pending.id, true) },
                            onDismiss = { onResolvePending(item.pending.id, false) },
                        )
                    }
                }

                item { RewardSectionTitle(title = AppText.t("redeem_inventory_title")) }
                if (inventoryItems.isEmpty()) {
                    item { EmptyRewardsCard(text = AppText.t("redeem_inventory_empty")) }
                } else {
                    items(inventoryItems, key = { it.reward.id }) { item ->
                        InventoryRewardCard(
                            item = item,
                            onUseClick = {
                                when (item.reward.rewardType) {
                                    RewardType.TIME_ADD,
                                    RewardType.PERIOD_PASS -> {
                                        useReward = item.reward
                                        useTargetType = GroupType.CONTROL
                                    }
                                    RewardType.DOUBLE_POINTS_DAY -> {
                                        useReward = item.reward
                                        useTargetType = GroupType.ENCOURAGE
                                    }
                                    else -> Unit
                                }
                            },
                        )
                    }
                }
            }

            InventorySubsection.PURCHASES -> {
                item { RewardSectionTitle(title = AppText.t("redeem_recent_redemptions")) }
                if (redemptionHistory.isEmpty()) {
                    item { EmptyRewardsCard(text = AppText.t("redeem_no_history_yet")) }
                } else {
                    items(redemptionHistory, key = { it.id }) { record ->
                        RedemptionHistoryItem(record = record)
                    }
                }
            }

            InventorySubsection.USES -> {
                item { RewardSectionTitle(title = AppText.t("redeem_use_history_title")) }
                if (rewardUseHistory.isEmpty()) {
                    item { EmptyRewardsCard(text = AppText.t("redeem_use_history_empty")) }
                } else {
                    items(rewardUseHistory, key = { it.id }) { record ->
                        RewardUseHistoryItemCard(record = record)
                    }
                }
            }
        }
    }

    if (useReward != null && useTargetType != null) {
        val reward = useReward!!
        val targetGroups = if (useTargetType == GroupType.CONTROL) controlGroups else encourageGroups
        AlertDialog(
            onDismissRequest = {
                useReward = null
                useTargetType = null
            },
            title = { Text(AppText.t("redeem_choose_target_group")) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    targetGroups.forEach { group ->
                        TargetGroupRow(
                            group = group,
                            reward = reward,
                            onClick = {
                                onUseReward(reward, group.group.id)
                                useReward = null
                                useTargetType = null
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        useReward = null
                        useTargetType = null
                    },
                ) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }
}

@Composable
private fun SubsectionSwitcher(
    current: InventorySubsection,
    onChange: (InventorySubsection) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InventorySwitchButton(
            selected = current == InventorySubsection.ITEMS,
            title = AppText.t("redeem_inventory_title"),
            onClick = { onChange(InventorySubsection.ITEMS) },
            modifier = Modifier.weight(1f),
        )
        InventorySwitchButton(
            selected = current == InventorySubsection.PURCHASES,
            title = AppText.t("redeem_recent_redemptions"),
            onClick = { onChange(InventorySubsection.PURCHASES) },
            modifier = Modifier.weight(1f),
        )
        InventorySwitchButton(
            selected = current == InventorySubsection.USES,
            title = AppText.t("redeem_use_history_title"),
            onClick = { onChange(InventorySubsection.USES) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InventorySwitchButton(
    selected: Boolean,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color =
            if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color =
                    if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CompactPointsSummaryCard(userPoints: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .background(
                        brush =
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                ),
                            ),
                        shape = RoundedCornerShape(18.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppText.t("redeem_current_points"),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.84f),
                )
                Text(
                    text = "%.1f PT".format(userPoints),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = AppText.t("redeem_store_manual_use_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
private fun RewardSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun EmptyRewardsCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StoreRewardItemCard(
    item: RewardStoreItem,
    userPoints: Double,
    controlGroupCount: Int,
    encourageGroupCount: Int,
    onPurchase: () -> Unit,
    onEdit: (() -> Unit)?,
    onArchive: (() -> Unit)?,
) {
    val reward = item.reward
    val canAfford = userPoints >= reward.pointCost
    val inStock = reward.stock == -1 || reward.stock > 0
    val dailyLimitReached = reward.builtinKey != null && item.purchasedTodayCount >= 1
    val needsControlGroups =
        (reward.rewardType == RewardType.TIME_ADD || reward.rewardType == RewardType.PERIOD_PASS) && controlGroupCount == 0
    val needsEncourageGroups = reward.rewardType == RewardType.DOUBLE_POINTS_DAY && encourageGroupCount == 0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RewardIcon(reward = reward, size = 38.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reward.localizedTitle(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = reward.localizedDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                    Text(
                        text = storeRuleSummary(reward),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (onEdit != null || onArchive != null) {
                    Row {
                        onEdit?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription =
                                        if (reward.builtinKey != null) AppText.t("redeem_edit_builtin_reward_cost")
                                        else AppText.t("redeem_edit_reward"),
                                )
                            }
                        }
                        onArchive?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = AppText.t("redeem_archive_custom_reward"),
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = stockSummary(reward.stock, item.ownedQuantity, reward.builtinKey != null, item.purchasedTodayCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (needsControlGroups) {
                Text(
                    text = AppText.t("redeem_no_control_group_for_time_pack"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (needsEncourageGroups) {
                Text(
                    text = AppText.t("redeem_no_encourage_group_for_double_points"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (dailyLimitReached) {
                Text(
                    text = AppText.t("redeem_store_daily_limit_reached"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = onPurchase,
                enabled = canAfford && inStock && !dailyLimitReached && !needsControlGroups && !needsEncourageGroups,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(AppText.t("redeem_store_purchase", reward.pointCost))
            }
        }
    }
}

@Composable
private fun RewardConfigItemCard(
    reward: RedemptionEntity,
    subtitle: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    primaryEnabled: Boolean = true,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RewardIcon(reward = reward, size = 34.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = reward.localizedTitle(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                TextButton(
                    onClick = onPrimaryAction,
                    enabled = primaryEnabled,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(primaryActionLabel)
                }
                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    TextButton(
                        onClick = onSecondaryAction,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(secondaryActionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryRewardCard(
    item: InventoryRewardItem,
    onUseClick: () -> Unit,
) {
    val reward = item.reward
    val canUse =
        reward.rewardType == RewardType.TIME_ADD ||
            reward.rewardType == RewardType.PERIOD_PASS ||
            reward.rewardType == RewardType.DOUBLE_POINTS_DAY
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RewardIcon(reward = reward, size = 38.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.localizedTitle(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = reward.localizedDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                Text(
                    text = AppText.t("redeem_inventory_status_line", item.quantity, item.activeCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (canUse) {
                OutlinedButton(onClick = onUseClick, shape = RoundedCornerShape(14.dp)) {
                    Text(AppText.t("redeem_inventory_use"))
                }
            } else {
                Icon(
                    imageVector =
                        if (reward.rewardType == RewardType.EMERGENCY_UNLOCK) Icons.Default.LockClock
                        else Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PendingShieldCard(
    item: PendingStreakShieldItem,
    onUse: () -> Unit,
    onDismiss: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = AppText.t("redeem_pending_archive_date_value", item.pending.archiveDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = AppText.t("redeem_pending_owned_value", item.ownedQuantity),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onUse, enabled = item.ownedQuantity > 0, modifier = Modifier.weight(1f)) {
                    Text(AppText.t("redeem_pending_use"))
                }
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(AppText.t("redeem_pending_skip"))
                }
            }
        }
    }
}

@Composable
private fun TargetGroupRow(
    group: AppGroupWithApps,
    reward: RedemptionEntity,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(group.group.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text =
                    AppText.t(
                        "redeem_group_period_limit_apps",
                        periodLabel(group.group.limitPeriod),
                        group.group.limitMinutes,
                        group.packageNames.size,
                    ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = useRuleSummary(reward, group.group.limitPeriod),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun RewardIcon(
    reward: RedemptionEntity,
    size: androidx.compose.ui.unit.Dp,
) {
    val icon =
        when (reward.rewardType) {
            RewardType.TIME_ADD,
            RewardType.EMERGENCY_UNLOCK -> Icons.Default.Timer
            RewardType.PERIOD_PASS -> Icons.Default.LockClock
            RewardType.STREAK_SHIELD,
            RewardType.DOUBLE_POINTS_DAY,
            RewardType.CUSTOM -> Icons.Default.EmojiEvents
        }
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

private fun storeRuleSummary(reward: RedemptionEntity): String =
    when (reward.rewardType) {
        RewardType.TIME_ADD -> AppText.t("redeem_rule_bind_control_group")
        RewardType.PERIOD_PASS -> AppText.t("redeem_rule_current_period_manual_use")
        RewardType.EMERGENCY_UNLOCK -> AppText.t("redeem_rule_overlay_use_only")
        RewardType.STREAK_SHIELD -> AppText.t("redeem_rule_review_then_confirm")
        RewardType.DOUBLE_POINTS_DAY -> AppText.t("redeem_rule_double_points_day")
        RewardType.CUSTOM -> AppText.t("redeem_rule_keep_in_inventory")
    }

private fun useRuleSummary(
    reward: RedemptionEntity,
    period: LimitPeriod,
): String =
    when (reward.rewardType) {
        RewardType.TIME_ADD -> AppText.t("redeem_use_rule_time_add", periodLabel(period))
        RewardType.PERIOD_PASS -> AppText.t("redeem_use_rule_period_pass", periodLabel(period))
        RewardType.DOUBLE_POINTS_DAY -> AppText.t("redeem_use_rule_double_points_day")
        else -> AppText.t("redeem_rule_keep_in_inventory")
    }

private fun stockSummary(
    stock: Int,
    owned: Int,
    isBuiltin: Boolean,
    purchasedTodayCount: Int,
): String {
    val stockText =
        if (stock == -1) {
            AppText.t("redeem_store_stock_owned_unlimited", owned)
        } else {
            AppText.t("redeem_store_stock_owned_value", stock, owned)
        }
    return if (isBuiltin) {
        AppText.t("redeem_store_stock_owned_daily_limit", stockText, purchasedTodayCount)
    } else {
        stockText
    }
}

private fun periodLabel(period: LimitPeriod): String =
    AppText.t(
        when (period) {
            LimitPeriod.DAILY -> "group_daily"
            LimitPeriod.WEEKLY -> "group_weekly"
            LimitPeriod.MONTHLY -> "group_monthly"
        },
    )

@Composable
fun RewardEditDialog(
    reward: RedemptionEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, String) -> Unit,
) {
    val builtinCostOnly = reward?.builtinKey != null
    var title by remember { mutableStateOf(reward?.title ?: "") }
    var cost by remember { mutableStateOf(reward?.pointCost?.toString() ?: "100") }
    var stock by remember { mutableStateOf(reward?.stock?.takeIf { it > 0 }?.toString() ?: "1") }
    var description by remember { mutableStateOf(reward?.description ?: "") }
    var isInfinite by remember { mutableStateOf(reward?.stock == -1) }
    var showErrors by remember { mutableStateOf(false) }

    val costValue = cost.toIntOrNull() ?: 0
    val stockValue = if (isInfinite) -1 else stock.toIntOrNull() ?: 0
    val validationError =
        if (builtinCostOnly) {
            if (costValue <= 0) RewardSaveValidationError.POINT_COST_INVALID else null
        } else {
            validateCustomRewardInput(title = title, pointCost = costValue, stock = stockValue)
        }
    val titleError = showErrors && !builtinCostOnly && title.isBlank()
    val costError = showErrors && validationError == RewardSaveValidationError.POINT_COST_INVALID
    val stockError = showErrors && !builtinCostOnly && validationError == RewardSaveValidationError.STOCK_INVALID

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when {
                    reward == null -> AppText.t("redeem_add_custom_reward")
                    builtinCostOnly -> AppText.t("redeem_edit_builtin_reward_cost")
                    else -> AppText.t("redeem_edit_reward")
                },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (builtinCostOnly) {
                    Text(
                        text = reward?.localizedTitle().orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = AppText.t("redeem_builtin_reward_cost_hint"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(AppText.t("redeem_item_name")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = titleError,
                        supportingText = { if (titleError) Text(AppText.t("redeem_error_title_required")) },
                    )
                }
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text(AppText.t("redeem_required_points")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = costError,
                    supportingText = { if (costError) Text(AppText.t("redeem_error_point_cost_invalid")) },
                )
                if (!builtinCostOnly) {
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
                            supportingText = { if (stockError) Text(AppText.t("redeem_error_stock_invalid")) },
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
            TextButton(onClick = onDismiss) { Text(AppText.t("group_cancel")) }
        },
    )
}

@Composable
private fun RedemptionHistoryItem(record: RedemptionHistoryEntity) {
    val dateFormatter = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    val subtitle =
        when (record.historyType) {
            RedemptionHistoryType.TIME_ADD -> AppText.t("redeem_history_purchase_time_add", record.bonusMinutes)
            RedemptionHistoryType.PERIOD_PASS -> AppText.t("redeem_history_purchase_period_pass")
            RedemptionHistoryType.EMERGENCY_UNLOCK -> AppText.t("redeem_history_purchase_emergency_unlock", record.bonusMinutes)
            RedemptionHistoryType.STREAK_SHIELD -> AppText.t("redeem_history_streak_shield")
            RedemptionHistoryType.DOUBLE_POINTS_DAY -> AppText.t("redeem_history_purchase_double_points_day")
            RedemptionHistoryType.CUSTOM -> AppText.t("redeem_custom_reward")
        }

    HistoryCard(
        title = record.localizedRewardTitle(),
        timestamp = dateFormatter.format(Date(record.redeemedAt)),
        subtitle = subtitle,
        trailing = "-${record.pointCost} PT",
        trailingColor = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun RewardUseHistoryItemCard(record: RewardUseHistoryEntity) {
    val dateFormatter = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    val payload = remember(record.payloadJson) { parseRewardPayload(record.payloadJson) }
    val subtitle =
        when (record.rewardType) {
            RewardType.TIME_ADD ->
                AppText.t(
                    "redeem_use_history_time_add",
                    record.targetGroupName ?: AppText.t("generic_target_group"),
                    payload.minutes,
                )
            RewardType.PERIOD_PASS ->
                AppText.t("redeem_use_history_period_pass", record.targetGroupName ?: AppText.t("generic_target_group"))
            RewardType.EMERGENCY_UNLOCK ->
                AppText.t(
                    "redeem_use_history_emergency_unlock",
                    record.targetGroupName ?: AppText.t("generic_target_group"),
                    payload.minutes,
                )
            RewardType.STREAK_SHIELD ->
                AppText.t(
                    when (payload.shieldTarget) {
                        StreakShieldTarget.ENCOURAGE_STREAK -> "redeem_use_history_streak_shield_encourage"
                        else -> "redeem_use_history_streak_shield_control"
                    },
                )
            RewardType.DOUBLE_POINTS_DAY ->
                AppText.t("redeem_use_history_double_points_day", record.targetGroupName ?: AppText.t("generic_target_group"))
            RewardType.CUSTOM -> AppText.t("redeem_rule_keep_in_inventory")
        }

    HistoryCard(
        title = record.localizedRewardTitle(),
        timestamp = dateFormatter.format(Date(record.usedAt)),
        subtitle = subtitle,
        trailing = AppText.t("redeem_use_history_used"),
        trailingColor = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun HistoryCard(
    title: String,
    timestamp: String,
    subtitle: String,
    trailing: String,
    trailingColor: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = trailingColor,
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

private fun RewardUseHistoryEntity.localizedRewardTitle(): String =
    rewardBuiltinKey?.let { AppText.t("${it}_title") } ?: rewardTitle
