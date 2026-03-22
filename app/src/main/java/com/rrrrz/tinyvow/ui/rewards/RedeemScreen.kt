package com.rrrrz.tinyvow.ui.rewards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemScreen(
    userPoints: Double,
    rewards: List<RedemptionEntity>,
    groups: List<AppGroupWithApps>,
    onRedeem: (RedemptionEntity, String?) -> Unit,
    onAddCustomReward: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("积分商城") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加自定义奖励")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // 积分概览卡片
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("当前积分", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "%.1f".format(userPoints),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                "可兑换项",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rewards) { reward ->
                    RewardItem(
                        reward = reward,
                        canAfford = userPoints >= reward.pointCost,
                        groups = groups,
                        onRedeem = { groupId -> onRedeem(reward, groupId) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomRewardDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cost ->
                onAddCustomReward(name, cost)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RewardItem(
    reward: RedemptionEntity,
    canAfford: Boolean,
    groups: List<AppGroupWithApps>,
    onRedeem: (String?) -> Unit
) {
    var showGroupPicker by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reward.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (reward.rewardType == RewardType.TIME_PACK) "加时包: ${reward.bonusMinutes}分钟" else "自定义奖励",
                    style = MaterialTheme.typography.bodySmall,
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
        AlertDialog(
            onDismissRequest = { showGroupPicker = false },
            title = { Text("选择目标分组") },
            text = {
                Column {
                    groups.filter { it.group.type == com.rrrrz.tinyvow.data.db.GroupType.CONTROL }.forEach { group ->
                        TextButton(
                            onClick = {
                                onRedeem(group.group.id)
                                showGroupPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(group.group.name)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun AddCustomRewardDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加自定义奖励") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("奖励名称") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("所需积分") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, cost.toIntOrNull() ?: 100) }) {
                Text("添加")
            }
        }
    )
}

