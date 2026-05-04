package com.rrrrz.tinyvow.ui.rewards

import com.rrrrz.tinyvow.i18n.AppText

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryType
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.ui.home.ProUpsellSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemScreen(
    userPoints: Double,
    rewards: List<RedemptionEntity>,
    groups: List<AppGroupWithApps>,
    redemptionHistory: List<RedemptionHistoryEntity> = emptyList(),
    onRedeem: (RedemptionEntity, String?) -> Unit,
    onAddReward: (String, Int, Int, String) -> Unit,
    onUpdateReward: (RedemptionEntity) -> Unit,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReward by remember { mutableStateOf<RedemptionEntity?>(null) }
    val customRewards = remember(rewards) { rewards.filter { it.builtinKey == null } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppText.t("home_rewards_store"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("group_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        // 积分概览卡片
        Surface(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(AppText.t("redeem_current_points"), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.1f".format(userPoints),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        " PT",
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Text(
            AppText.t("redeem_available_rewards"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(rewards) { reward ->
                val customIndex = if (reward.builtinKey == null) {
                    customRewards.indexOfFirst { it.id == reward.id }
                } else {
                    -1
                }
                RewardItem(
                    reward = reward,
                    canAfford = userPoints >= reward.pointCost && (reward.stock == -1 || reward.stock > 0),
                    groups = groups,
                    onRedeem = { groupId -> onRedeem(reward, groupId) },
                    onLongClick = {
                        if (reward.builtinKey == null && !ProFeatureGate.canEditCustomReward(isProActive, customIndex)) {
                            onShowProUpsell(ProUpsellSource.CUSTOM_REWARD)
                        } else {
                            editingReward = reward
                        }
                    }
                )
            }

            // 新增自定义项按钮 (加号放在可兑选项中)
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
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppText.t("redeem_add_custom_reward"))
                }
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 最近兑换记录
            if (redemptionHistory.isNotEmpty()) {
                item {
                    Text(
                        AppText.t("redeem_recent_redemptions"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(redemptionHistory.take(10)) { record ->
                    RedemptionHistoryItem(record)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showAddDialog) {
        RewardEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cost, stock, desc ->
                onAddReward(name, cost, stock, desc)
                showAddDialog = false
            }
        )
    }

    if (editingReward != null) {
        RewardEditDialog(
            reward = editingReward,
            onDismiss = { editingReward = null },
            onConfirm = { name, cost, stock, desc ->
                editingReward?.let {
                    onUpdateReward(it.copy(title = name, pointCost = cost, stock = stock, description = desc))
                }
                editingReward = null
            }
        )
    }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RewardItem(
    reward: RedemptionEntity,
    canAfford: Boolean,
    groups: List<AppGroupWithApps>,
    onRedeem: (String?) -> Unit,
    onLongClick: () -> Unit
) {
    var showGroupPicker by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(
                onClick = { /* Handle normal click on individual components or just do nothing if we want separate button */ },
                onLongClick = onLongClick.takeIf { reward.builtinKey == null }
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reward.localizedTitle(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val description = reward.localizedDescription()
                if (description.isNotBlank()) {
                    Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    if (reward.rewardType == RewardType.TIME_PACK) AppText.t("redeem_time_capsule_value_minutes", reward.bonusMinutes) else AppText.t("redeem_offline_reward"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (reward.stock == -1) AppText.t("redeem_stock_unlimited") else AppText.t("redeem_stock_left_value", reward.stock),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "scale")

            Surface(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = canAfford,
                        onClick = {
                            if (reward.rewardType == RewardType.TIME_PACK) {
                                showGroupPicker = true
                            } else {
                                onRedeem(null)
                            }
                        }
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (canAfford) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${reward.pointCost} PT",
                        color = if (canAfford) Color.White else MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showGroupPicker) {
        val controlGroups =
            remember(groups) {
                groups
                    .filter { it.group.type == GroupType.CONTROL }
                    .sortedWith(
                        compareBy<AppGroupWithApps> { it.group.limitPeriod.ordinal }
                            .thenBy { it.group.sortOrder }
                            .thenBy { it.group.name },
                    )
            }
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
                    if (controlGroups.isEmpty()) {
                        Text(
                            text = AppText.t("redeem_no_control_group_for_time_pack"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        controlGroups
                            .groupBy { it.group.limitPeriod }
                            .forEach { (period, periodGroups) ->
                                Text(
                                    text = timePackPeriodSectionTitle(period),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                periodGroups.forEach { group ->
                                    TimePackTargetGroupRow(
                                        group = group,
                                        period = period,
                                        onClick = {
                                            onRedeem(group.group.id)
                                            showGroupPicker = false
                                        },
                                    )
                                }
                            }
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupPicker = false }) {
                    Text(AppText.t("group_cancel"))
                }
            }
        )
    }
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
                .clip(RoundedCornerShape(16.dp))
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
    onConfirm: (String, Int, Int, String) -> Unit
) {
    var title by remember { mutableStateOf(reward?.title ?: "") }
    var cost by remember { mutableStateOf(reward?.pointCost?.toString() ?: "100") }
    var stock by remember { mutableStateOf(reward?.stock?.toString() ?: "-1") }
    var description by remember { mutableStateOf(reward?.description ?: "") }
    var isInfinite by remember { mutableStateOf(reward?.stock == -1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (reward == null) AppText.t("redeem_add_custom_reward") else AppText.t("redeem_edit_reward")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppText.t("redeem_item_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text(AppText.t("redeem_required_points")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
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
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(AppText.t("redeem_description_optional")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stockValue = if (isInfinite) -1 else stock.toIntOrNull() ?: 1
                    onConfirm(title, cost.toIntOrNull() ?: 100, stockValue, description)
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (reward == null) AppText.t("redeem_add") else AppText.t("group_save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppText.t("group_cancel")) }
        }
    )
}

@Composable
private fun RedemptionHistoryItem(record: RedemptionHistoryEntity) {
    val dateFormatter = remember {
        SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    }
    val subtitle = when (record.historyType) {
        RedemptionHistoryType.TIME_PACK -> {
            val groupName = record.targetGroupName ?: AppText.t("redeem_target_group")
            AppText.t("redeem_value_value_minutes", groupName, record.bonusMinutes)
        }
        RedemptionHistoryType.CUSTOM -> AppText.t("redeem_custom_reward")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    record.localizedRewardTitle(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    dateFormatter.format(Date(record.redeemedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "-${record.pointCost} PT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
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
