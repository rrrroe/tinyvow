package com.rrrrz.tinyvow.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryType
import com.rrrrz.tinyvow.data.db.RewardIconSource
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.db.RewardUseHistoryEntity
import com.rrrrz.tinyvow.data.db.StreakShieldTarget
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.CustomRewardDraft
import com.rrrrz.tinyvow.data.repository.InventoryRewardItem
import com.rrrrz.tinyvow.data.repository.PendingStreakShieldItem
import com.rrrrz.tinyvow.data.repository.RewardIconCatalog
import com.rrrrz.tinyvow.data.repository.RewardIconSpec
import com.rrrrz.tinyvow.data.repository.RewardSaveValidationError
import com.rrrrz.tinyvow.data.repository.RewardStoreUnavailableReason
import com.rrrrz.tinyvow.data.repository.RewardStoreItem
import com.rrrrz.tinyvow.data.repository.evaluateRewardStoreAvailability
import com.rrrrz.tinyvow.data.repository.parseRewardPayload
import com.rrrrz.tinyvow.data.repository.validateCustomRewardInput
import com.rrrrz.tinyvow.data.supermode.GuardedAction
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.home.ProUpsellSource
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class InventorySubsection {
    ITEMS,
    PURCHASES,
    USES,
}

private val RewardFilledFieldHeight = 52.dp
private val RewardFilledFieldShape = RoundedCornerShape(16.dp)

@Composable
fun RedeemScreen(
    userPoints: Double,
    storeItems: List<RewardStoreItem>,
    groups: List<AppGroupWithApps>,
    onPurchase: (RedemptionEntity) -> Unit,
    onAddReward: (CustomRewardDraft) -> Unit,
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
    fun purchaseWithGuard(reward: RedemptionEntity) {
        val action = GuardedAction.fromRewardType(reward.rewardType)
        if (action == null) {
            onPurchase(reward)
        } else {
            onGuardAction(action) {
                onPurchase(reward)
            }
        }
    }

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
            contentPadding = PaddingValues(
                horizontal = TinyVowSpacing.PageHorizontal,
                vertical = TinyVowSpacing.PageTop,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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
                        onPurchase = { purchaseWithGuard(item.reward) },
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
                        onPurchase = { purchaseWithGuard(item.reward) },
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
                        onPurchase = { purchaseWithGuard(item.reward) },
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
                        onPurchase = { purchaseWithGuard(item.reward) },
                        onEdit = null,
                        onArchive = null,
                    )
                }
            }

            item {
                TinyVowButton(
                    text = AppText.t("redeem_custom_config"),
                    onClick = { showConfigPage = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showAddDialog) {
        RewardEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { draft ->
                onGuardAction(GuardedAction.ADD_CUSTOM_REWARD) {
                    onAddReward(draft)
                    showAddDialog = false
                }
            },
        )
    }

    editingReward?.let { reward ->
        RewardEditDialog(
            reward = reward,
            onDismiss = { editingReward = null },
            onConfirm = { draft ->
                val updatedReward =
                    if (reward.builtinKey != null) {
                        reward.copy(pointCost = draft.pointCost)
                    } else {
                        reward.copy(
                            title = draft.title,
                            pointCost = draft.pointCost,
                            stock = draft.stock,
                            description = draft.description,
                            iconSource = draft.iconSpec?.source,
                            iconValue = draft.iconSpec?.value,
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
                TinyVowButton(
                    text = AppText.t("group_delete"),
                    onClick = {
                        onArchiveReward(reward)
                        archivingReward = null
                    },
                    tone = TinyVowButtonTone.Danger,
                )
            },
            dismissButton = {
                TinyVowButton(
                    text = AppText.t("group_cancel"),
                    onClick = { archivingReward = null },
                )
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
        contentPadding = PaddingValues(
            horizontal = TinyVowSpacing.PageHorizontal,
            vertical = TinyVowSpacing.PageTop,
        ),
        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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
                primaryActionDescription = AppText.t("redeem_edit_builtin_reward_cost"),
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
                    primaryActionDescription = AppText.t("redeem_edit_reward"),
                    onPrimaryAction = { onEditReward(item.reward) },
                    primaryEnabled = canEdit,
                    secondaryActionDescription = AppText.t("redeem_archive_custom_reward"),
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
        contentPadding = PaddingValues(
            horizontal = TinyVowSpacing.PageHorizontal,
            vertical = TinyVowSpacing.PageTop,
        ),
        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            themeColors.baseContainer.copy(alpha = 0.76f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        },
        border = BorderStroke(
            1.dp,
            if (selected) {
                themeColors.base.copy(alpha = 0.18f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
            },
        ),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) themeColors.base else themeColors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun InventorySubButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
) {
    val themeColors = LocalThemeColors.current
    val container =
        when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
            primary -> themeColors.encourageContainer.copy(alpha = 0.70f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
        }
    val contentColor =
        when {
            !enabled -> themeColors.inkMuted.copy(alpha = 0.52f)
            primary -> themeColors.encourage
            else -> themeColors.inkMuted
        }
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 34.dp),
        shape = RoundedCornerShape(12.dp),
        color = container,
        border = BorderStroke(
            1.dp,
            if (primary) themeColors.encourage.copy(alpha = 0.16f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
        ),
        enabled = enabled,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CompactPointsSummaryCard(userPoints: Double) {
    val themeColors = LocalThemeColors.current
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppText.t("redeem_current_points"),
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColors.inkMuted,
                )
                Text(
                    text = "%.1f PT".format(userPoints),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.base,
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.64f),
            ) {
                Text(
                    text = AppText.t("redeem_store_manual_use_hint"),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = themeColors.inkStrong,
                )
            }
        }
    }
}

@Composable
private fun RewardSectionTitle(title: String) {
    val themeColors = LocalThemeColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = themeColors.inkStrong,
    )
}

@Composable
private fun EmptyRewardsCard(text: String) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CompactCardVertical,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
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
    val themeColors = LocalThemeColors.current
    val reward = item.reward
    val availability =
        evaluateRewardStoreAvailability(
            item = item,
            userPoints = userPoints,
            controlGroupCount = controlGroupCount,
            encourageGroupCount = encourageGroupCount,
        )
    val canPurchase = availability.canPurchase
    val canPurchaseFromCard = canPurchase && onEdit == null && onArchive == null
    val availabilityText =
        storeAvailabilityText(
            item = item,
            unavailableReason = availability.unavailableReason,
        )

    TinyVowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = canPurchaseFromCard,
                onClickLabel = AppText.t("redeem_store_purchase_action"),
                role = Role.Button,
                onClick = onPurchase,
            ),
        shape = RoundedCornerShape(TinyVowRadius.Card),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CompactCardVertical,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RewardIcon(reward = reward, size = 56.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = reward.localizedTitle(),
                    style = MaterialTheme.typography.titleSmall,
                    color = themeColors.ink,
                )
                Text(
                    text = reward.localizedDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                    maxLines = 2,
                )
                Text(
                    text = storeRuleSummary(reward),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                Text(
                    text = availabilityText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (canPurchase) themeColors.inkMuted else MaterialTheme.colorScheme.error,
                    maxLines = 2,
                )
            }
            if (onEdit != null || onArchive != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
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
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = canPurchase,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "${reward.pointCost} PT",
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

    }
}

private fun storeAvailabilityText(
    item: RewardStoreItem,
    unavailableReason: RewardStoreUnavailableReason?,
): String {
    val reward = item.reward
    return when (unavailableReason) {
        RewardStoreUnavailableReason.OUT_OF_STOCK -> AppText.t("redeem_error_out_of_stock")
        RewardStoreUnavailableReason.DAILY_LIMIT_REACHED -> AppText.t("redeem_store_daily_limit_reached")
        RewardStoreUnavailableReason.NEEDS_CONTROL_GROUP -> AppText.t("redeem_no_control_group_for_time_pack")
        RewardStoreUnavailableReason.NEEDS_ENCOURAGE_GROUP -> AppText.t("redeem_no_encourage_group_for_double_points")
        RewardStoreUnavailableReason.INSUFFICIENT_POINTS -> AppText.t("redeem_error_insufficient_points")
        null -> {
            val stockText =
                if (reward.stock == -1) {
                    AppText.t("redeem_store_stock_owned_unlimited", item.ownedQuantity)
                } else {
                    AppText.t("redeem_store_stock_owned_value", reward.stock, item.ownedQuantity)
                }
            if (reward.builtinKey != null) {
                AppText.t("redeem_store_stock_owned_daily_limit", stockText, item.purchasedTodayCount)
            } else {
                stockText
            }
        }
    }
}

@Composable
private fun RewardConfigItemCard(
    reward: RedemptionEntity,
    subtitle: String,
    primaryActionDescription: String,
    onPrimaryAction: () -> Unit,
    primaryEnabled: Boolean = true,
    secondaryActionDescription: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CompactCardVertical,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RewardIcon(reward = reward, size = 56.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = reward.localizedTitle(),
                    style = MaterialTheme.typography.titleSmall,
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
                IconButton(
                    onClick = onPrimaryAction,
                    enabled = primaryEnabled,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = primaryActionDescription,
                    )
                }
                if (secondaryActionDescription != null && onSecondaryAction != null) {
                    IconButton(
                        onClick = onSecondaryAction,
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = secondaryActionDescription,
                        )
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
    TinyVowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = canUse,
                onClickLabel = AppText.t("redeem_inventory_use"),
                role = Role.Button,
                onClick = onUseClick,
            ),
        shape = RoundedCornerShape(TinyVowRadius.Card),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CompactCardVertical,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RewardIcon(reward = reward, size = 38.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.localizedTitle(),
                    style = MaterialTheme.typography.titleSmall,
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
                InventorySubButton(
                    text = AppText.t("redeem_inventory_use"),
                    onClick = onUseClick,
                    primary = true,
                )
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
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CompactCardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(item.title, style = MaterialTheme.typography.titleSmall)
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
                InventorySubButton(
                    text = AppText.t("redeem_pending_use"),
                    onClick = onUse,
                    enabled = item.ownedQuantity > 0,
                    primary = true,
                    modifier = Modifier.weight(1f),
                )
                InventorySubButton(
                    text = AppText.t("redeem_pending_skip"),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
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
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = themeColors.baseContainer.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, themeColors.base.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(group.group.name, style = MaterialTheme.typography.bodyLarge, color = themeColors.inkStrong)
            Text(
                text =
                    AppText.t(
                        "redeem_group_period_limit_apps",
                        periodLabel(group.group.limitPeriod),
                        group.group.limitMinutes,
                        group.packageNames.size,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.inkMuted,
            )
            Text(
                text = useRuleSummary(reward, group.group.limitPeriod),
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.base,
            )
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

private fun periodLabel(period: LimitPeriod): String =
    AppText.t(
        when (period) {
            LimitPeriod.DAILY -> "group_daily"
            LimitPeriod.WEEKLY -> "group_weekly"
            LimitPeriod.MONTHLY -> "group_monthly"
        },
    )

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RewardEditDialog(
    reward: RedemptionEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (CustomRewardDraft) -> Unit,
) {
    val builtinCostOnly = reward?.builtinKey != null
    var title by remember(reward?.id) { mutableStateOf(reward?.title ?: "") }
    var cost by remember(reward?.id) { mutableStateOf(reward?.pointCost?.toString() ?: "100") }
    var stock by remember(reward?.id) { mutableStateOf(reward?.stock?.takeIf { it > 0 }?.toString() ?: "1") }
    var description by remember(reward?.id) { mutableStateOf(reward?.description ?: "") }
    var isInfinite by remember(reward?.id) { mutableStateOf(reward?.stock == -1) }
    var selectedPresetKey by remember(reward?.id) {
        mutableStateOf(reward?.takeIf { it.iconSource == RewardIconSource.PRESET }?.iconValue)
    }
    var showErrors by remember(reward?.id) { mutableStateOf(false) }
    var showingIconPicker by remember(reward?.id) { mutableStateOf(false) }

    val costValue = cost.toIntOrNull() ?: 0
    val stockValue = if (isInfinite) -1 else stock.toIntOrNull() ?: 0
    val existingIconSpec =
        reward?.iconSource?.let { source ->
            reward.iconValue?.takeIf { it.isNotBlank() }?.let { value ->
                RewardIconSpec(source = source, value = value)
            }
        }
    val selectedIconSpec =
        if (builtinCostOnly) {
            null
        } else {
            selectedPresetKey?.let { RewardIconSpec(RewardIconSource.PRESET, it) } ?: existingIconSpec
        }
    val validationError =
        if (builtinCostOnly) {
            if (costValue <= 0) RewardSaveValidationError.POINT_COST_INVALID else null
        } else {
            validateCustomRewardInput(
                title = title,
                pointCost = costValue,
                stock = stockValue,
                iconSpec = selectedIconSpec,
            )
        }
    val titleError = showErrors && !builtinCostOnly && title.isBlank()
    val costError = showErrors && validationError == RewardSaveValidationError.POINT_COST_INVALID
    val stockError = showErrors && !builtinCostOnly && validationError == RewardSaveValidationError.STOCK_INVALID

    if (!builtinCostOnly && showingIconPicker) {
        Dialog(onDismissRequest = { showingIconPicker = false }) {
            TinyVowCard(
                shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CompactCardHorizontal,
                        vertical = TinyVowSpacing.CompactCardVertical,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        RewardIconCatalog.customPresetKeys.forEach { key ->
                            val selected = selectedPresetKey == key
                            Surface(
                                modifier =
                                    Modifier
                                        .size(55.dp)
                                        .semantics {
                                            contentDescription =
                                                AppText.t(
                                                    "redeem_icon_preset_accessibility",
                                                    RewardIconCatalog.presetOrdinal(key) ?: 0,
                                                )
                                        }
                                        .clickable {
                                            selectedPresetKey = key
                                            showingIconPicker = false
                                        },
                                shape = RoundedCornerShape(14.dp),
                                color =
                                    if (selected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
                                    },
                                border =
                                    BorderStroke(
                                        width = if (selected) 2.dp else 1.dp,
                                        color =
                                            if (selected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                            },
                                    ),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    RewardIconPreview(
                                        iconSource = RewardIconSource.PRESET,
                                        iconValue = key,
                                        size = 72.dp,
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showingIconPicker = false }) {
                            Text(AppText.t("group_back"))
                        }
                    }
                }
            }
        }
        return
    }

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
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (builtinCostOnly) {
                    RewardIconPreview(
                        builtinKey = reward?.builtinKey,
                        size = 68.dp,
                    )
                    Text(
                        text = reward?.localizedTitle().orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = AppText.t("redeem_builtin_reward_cost_hint"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    RewardFieldLabel(AppText.t("redeem_required_points"))
                    RewardFilledInputField(
                        value = cost,
                        onValueChange = { cost = it },
                        placeholder = AppText.t("redeem_required_points"),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardType = KeyboardType.Number,
                        textAlign = TextAlign.Start,
                    )
                    if (costError) {
                        RewardFieldAssistiveText(
                            text = AppText.t("redeem_error_point_cost_invalid"),
                            isError = true,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RewardFilledInputField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = AppText.t("redeem_item_name"),
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Text,
                            textAlign = TextAlign.Start,
                        )
                        RewardIconSelectorButton(
                            iconSource = selectedIconSpec?.source,
                            iconValue = selectedIconSpec?.value,
                            onClick = { showingIconPicker = true },
                        )
                    }
                    if (titleError) {
                        RewardFieldAssistiveText(
                            text = AppText.t("redeem_error_title_required"),
                            isError = true,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            RewardFieldLabel(AppText.t("redeem_required_points"))
                            RewardFilledInputField(
                                value = cost,
                                onValueChange = { cost = it },
                                placeholder = AppText.t("redeem_required_points"),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardType = KeyboardType.Number,
                                textAlign = TextAlign.Start,
                            )
                            if (costError) {
                                RewardFieldAssistiveText(
                                    text = AppText.t("redeem_error_point_cost_invalid"),
                                    isError = true,
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            RewardFieldLabel(AppText.t("redeem_stock_quantity"))
                            RewardFilledInputField(
                                value = stock,
                                onValueChange = { stock = it },
                                placeholder = AppText.t("redeem_stock_quantity"),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardType = KeyboardType.Number,
                                textAlign = TextAlign.Start,
                                enabled = !isInfinite,
                            )
                            if (!isInfinite && stockError) {
                                RewardFieldAssistiveText(
                                    text = AppText.t("redeem_error_stock_invalid"),
                                    isError = true,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(start = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Checkbox(checked = isInfinite, onCheckedChange = { isInfinite = it })
                        Text(AppText.t("redeem_unlimited_stock"), style = MaterialTheme.typography.bodyMedium)
                    }
                    RewardFieldLabel(AppText.t("redeem_description_optional"))
                    RewardFilledTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = AppText.t("redeem_description_optional"),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showErrors = true
                    if (validationError == null) {
                        onConfirm(
                            CustomRewardDraft(
                                title = title.trim(),
                                pointCost = costValue,
                                stock = stockValue,
                                description = description.trim(),
                                iconSpec = selectedIconSpec,
                            ),
                        )
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
private fun RewardFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun RewardFieldAssistiveText(
    text: String,
    isError: Boolean = false,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color =
            if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
    )
}

@Composable
private fun RewardIconSelectorButton(
    iconSource: RewardIconSource?,
    iconValue: String?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(RewardFilledFieldHeight)
            .clickable(onClick = onClick),
        shape = RewardFilledFieldShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            RewardIconPreview(
                iconSource = iconSource,
                iconValue = iconValue,
                size = 40.dp,
            )
        }
    }
}

@Composable
private fun RewardFilledInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType,
    textAlign: TextAlign,
    enabled: Boolean = true,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleSmall,
) {
    RewardFieldContainer(
        modifier = modifier,
        enabled = enabled,
    ) {
        BasicTextField(
            value = value,
            onValueChange = {
                if (enabled) {
                    onValueChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle =
                textStyle.merge(
                    TextStyle(
                        color =
                            if (enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            },
                        textAlign = textAlign,
                    ),
                ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = textStyle,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = textAlign,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun RewardFilledTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RewardFilledFieldShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 92.dp)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle =
                MaterialTheme.typography.bodyMedium.merge(
                    TextStyle(color = MaterialTheme.colorScheme.onSurface),
                ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun RewardFieldContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier.height(RewardFilledFieldHeight),
        shape = RewardFilledFieldShape,
        color =
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = if (enabled) 0.3f else 0.2f,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
            content = content,
        )
    }
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
                Text(title, style = MaterialTheme.typography.bodyLarge)
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

