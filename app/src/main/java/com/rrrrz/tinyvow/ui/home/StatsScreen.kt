package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlinx.coroutines.delay

private data class StatsUiState(
    val isLoading: Boolean = true,
    val isPermissionGranted: Boolean = false,
    val managedAppCount: Int = 0,
    val groupCount: Int = 0,
    val todayUsageMillis: Long = 0L,
    val weeklyUsageMillis: Long = 0L,
    val monthlyUsageMillis: Long = 0L,
    val controlCompletedCount: Int = 0,
    val controlTotalCount: Int = 0,
    val encourageCompletedCount: Int = 0,
    val encourageTotalCount: Int = 0,
    val groupItems: List<StatsGroupItem> = emptyList(),
)

private data class StatsGroupItem(
    val id: String,
    val name: String,
    val type: GroupType,
    val period: LimitPeriod,
    val packageCount: Int,
    val limitMinutes: Int,
    val usedMillis: Long,
    val progress: Float,
    val deltaMillis: Long,
)

@Composable
fun StatsRoute(
    usageAccessStatus: UsageAccessStatus,
    groupsWithApps: List<AppGroupWithApps>,
    userPoints: Double,
    todayPoints: Double,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var statsUiState by remember { mutableStateOf(StatsUiState()) }

    LaunchedEffect(usageAccessStatus, groupsWithApps) {
        if (usageAccessStatus != UsageAccessStatus.GRANTED) {
            statsUiState = StatsUiState(
                isLoading = false,
                isPermissionGranted = false,
            )
            return@LaunchedEffect
        }

        val usageRepository = UsageStatsUsageRepository(context)
        while (true) {
            statsUiState = buildStatsUiState(
                groupsWithApps = groupsWithApps,
                usageRepository = usageRepository,
            )
            delay(15_000L)
        }
    }

    StatsScreen(
        state = statsUiState,
        userPoints = userPoints,
        todayPoints = todayPoints,
        modifier = modifier,
    )
}

private suspend fun buildStatsUiState(
    groupsWithApps: List<AppGroupWithApps>,
    usageRepository: UsageStatsUsageRepository,
): StatsUiState {
    val uniquePackages = groupsWithApps.flatMap { it.packageNames }.distinct()

    var todayUsageMillis = 0L
    var weeklyUsageMillis = 0L
    var monthlyUsageMillis = 0L

    for (packageName in uniquePackages) {
        todayUsageMillis += usageRepository.getTodayUsageMillis(packageName)
        weeklyUsageMillis += usageRepository.getUsageInPeriod(packageName, LimitPeriod.WEEKLY)
        monthlyUsageMillis += usageRepository.getUsageInPeriod(packageName, LimitPeriod.MONTHLY)
    }

    val groupItems = groupsWithApps.map { groupWithApps ->
        var usedMillis = 0L
        for (packageName in groupWithApps.packageNames.distinct()) {
            usedMillis += usageRepository.getUsageInPeriod(packageName, groupWithApps.group.limitPeriod)
        }

        val limitMillis = groupWithApps.group.limitMinutes * 60_000L
        val progress = if (limitMillis <= 0L) {
            0f
        } else {
            min(usedMillis.toFloat() / limitMillis.toFloat(), 1f)
        }

        StatsGroupItem(
            id = groupWithApps.group.id,
            name = groupWithApps.group.name,
            type = groupWithApps.group.type,
            period = groupWithApps.group.limitPeriod,
            packageCount = groupWithApps.packageNames.distinct().size,
            limitMinutes = groupWithApps.group.limitMinutes,
            usedMillis = usedMillis,
            progress = progress,
            deltaMillis = limitMillis - usedMillis,
        )
    }.sortedByDescending { it.usedMillis }

    val controlGroups = groupItems.filter { it.type == GroupType.CONTROL }
    val encourageGroups = groupItems.filter { it.type == GroupType.ENCOURAGE }

    return StatsUiState(
        isLoading = false,
        isPermissionGranted = true,
        managedAppCount = uniquePackages.size,
        groupCount = groupsWithApps.size,
        todayUsageMillis = todayUsageMillis,
        weeklyUsageMillis = weeklyUsageMillis,
        monthlyUsageMillis = monthlyUsageMillis,
        controlCompletedCount = controlGroups.count { it.deltaMillis >= 0L },
        controlTotalCount = controlGroups.size,
        encourageCompletedCount = encourageGroups.count { it.deltaMillis <= 0L },
        encourageTotalCount = encourageGroups.size,
        groupItems = groupItems,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsScreen(
    state: StatsUiState,
    userPoints: Double,
    todayPoints: Double,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        !state.isPermissionGranted -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "先开启使用情况访问权限，统计页才能展示真实数据。",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "战报",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "先看总览，再看每个分组当前周期的进度。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OverviewCard(
                            title = "总积分",
                            value = String.format("%.1f", userPoints),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OverviewCard(
                            title = "今日积分",
                            value = "+${String.format("%.1f", todayPoints)}",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OverviewCard(
                            title = "今日时长",
                            value = formatDuration(state.todayUsageMillis),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OverviewCard(
                            title = "本周时长",
                            value = formatDuration(state.weeklyUsageMillis),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OverviewCard(
                            title = "本月时长",
                            value = formatDuration(state.monthlyUsageMillis),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "总览进度",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("已管理应用")
                                Text("${state.managedAppCount} 个", fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("已创建分组")
                                Text("${state.groupCount} 个", fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("受控组达成")
                                Text(
                                    "${state.controlCompletedCount}/${state.controlTotalCount}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("激励组达成")
                                Text(
                                    "${state.encourageCompletedCount}/${state.encourageTotalCount}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "分组明细",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (state.groupItems.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = "还没有分组。先去首页创建分组，统计页才会有内容。",
                                modifier = Modifier.padding(20.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                } else {
                    items(state.groupItems, key = { it.id }) { item ->
                        StatsGroupCard(item = item)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun StatsGroupCard(item: StatsGroupItem) {
    val accentColor = when (item.type) {
        GroupType.CONTROL -> MaterialTheme.colorScheme.primary
        GroupType.ENCOURAGE -> MaterialTheme.colorScheme.tertiary
    }

    val statusText = when (item.type) {
        GroupType.CONTROL -> {
            if (item.deltaMillis >= 0L) {
                "还剩 ${formatDuration(item.deltaMillis)}"
            } else {
                "已超出 ${formatDuration(item.deltaMillis.absoluteValue)}"
            }
        }

        GroupType.ENCOURAGE -> {
            if (item.deltaMillis <= 0L) {
                "已达成目标"
            } else {
                "还差 ${formatDuration(item.deltaMillis)}"
            }
        }
    }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${item.packageCount} 个应用 · ${periodLabel(item.period)}周期",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AssistChip(
                    onClick = {},
                    enabled = false,
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = accentColor.copy(alpha = 0.15f),
                        disabledLabelColor = accentColor,
                    ),
                    label = {
                        Text(if (item.type == GroupType.CONTROL) "限制组" else "激励组")
                    },
                )
            }

            Text(
                text = "${formatDuration(item.usedMillis)} / ${item.limitMinutes} 分钟",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier.fillMaxWidth(),
                color = accentColor,
            )

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.type == GroupType.CONTROL && item.deltaMillis < 0L) {
                    MaterialTheme.colorScheme.error
                } else {
                    accentColor
                },
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun periodLabel(period: LimitPeriod): String {
    return when (period) {
        LimitPeriod.DAILY -> "每日"
        LimitPeriod.WEEKLY -> "每周"
        LimitPeriod.MONTHLY -> "每月"
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0L -> "${hours}小时 ${minutes}分钟"
        minutes > 0L -> "${minutes}分钟"
        else -> "${durationMillis / 1000L}秒"
    }
}
