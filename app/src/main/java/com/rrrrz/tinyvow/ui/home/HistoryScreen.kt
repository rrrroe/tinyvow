package com.rrrrz.tinyvow.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private enum class HistoryListSort(val label: String) {
    DATE("\u6700\u8fd1"),
    USAGE("\u65f6\u957f"),
    POINTS("\u51c0\u79ef\u5206"),
    REDEMPTIONS("\u5151\u6362"),
}

private enum class HistoryRangeFilter(val label: String) {
    LAST_7("\u8fd1 7 \u5929"),
    LAST_30("\u8fd1 30 \u5929"),
    ALL("\u5168\u90e8"),
}

private enum class HistoryGroupFilter(val label: String) {
    ALL("\u5168\u90e8"),
    CONTROL("\u63a7\u5236\u7ec4"),
    ENCOURAGE("\u9f13\u52b1\u7ec4"),
}

private enum class GroupSortMode(val label: String) {
    DEFAULT("\u9ed8\u8ba4"),
    USAGE("\u4f7f\u7528"),
    POINTS("\u79ef\u5206"),
    STATUS("\u72b6\u6001"),
}

private enum class AppSortMode(val label: String) {
    USAGE("\u65f6\u957f"),
    OPENS("\u6253\u5f00"),
    SESSIONS("\u4f1a\u8bdd"),
    NIGHT("\u591c\u95f4"),
    POINTS("\u79ef\u5206"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRoute(
    archiveRepository: DailyArchiveRepository,
    onBack: () -> Unit,
) {
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }
    var listSortMode by rememberSaveable { mutableStateOf(HistoryListSort.DATE) }
    var rangeFilter by rememberSaveable { mutableStateOf(HistoryRangeFilter.LAST_30) }
    var groupFilter by rememberSaveable { mutableStateOf(HistoryGroupFilter.ALL) }
    var refreshingDate by rememberSaveable { mutableStateOf<String?>(null) }
    var refreshError by rememberSaveable { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val archives by archiveRepository.getRecentArchives().collectAsState(initial = emptyList())
    val currentArchive by (selectedDate?.let { archiveRepository.getArchiveByDate(it) })
        ?.collectAsState(initial = null) ?: remember { mutableStateOf<DailyArchiveEntity?>(null) }
    val groupArchives by (selectedDate?.let { archiveRepository.getGroupArchivesByDate(it) })
        ?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val appArchives by (selectedDate?.let { archiveRepository.getAppArchivesByDate(it) })
        ?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }

    val archiveDatesDesc = remember(archives) { archives.map { it.archiveDate }.sortedDescending() }
    val selectedIndex = remember(selectedDate, archiveDatesDesc) { archiveDatesDesc.indexOf(selectedDate) }
    val previousDate = selectedIndex.takeIf { it >= 0 }?.let { archiveDatesDesc.getOrNull(it + 1) }
    val nextDate = selectedIndex.takeIf { it >= 0 }?.let { archiveDatesDesc.getOrNull(it - 1) }

    BackHandler(enabled = selectedDate != null) {
        selectedDate = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedDate == null) {
                            "\u4f7f\u7528\u5386\u53f2"
                        } else {
                            "\u5f52\u6863\u8be6\u60c5"
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedDate == null) {
                                onBack()
                            } else {
                                selectedDate = null
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        if (selectedDate == null) {
            HistoryListScreen(
                archives = archives,
                sortMode = listSortMode,
                onSortModeChange = { listSortMode = it },
                rangeFilter = rangeFilter,
                onRangeFilterChange = { rangeFilter = it },
                groupFilter = groupFilter,
                onGroupFilterChange = { groupFilter = it },
                onSelectDate = { selectedDate = it },
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            HistoryDetailScreen(
                archive = currentArchive,
                groupArchives = groupArchives,
                appArchives = appArchives,
                previousDate = previousDate,
                nextDate = nextDate,
                onSelectDate = { selectedDate = it },
                onRefreshDate = { date ->
                    coroutineScope.launch {
                        refreshingDate = date
                        refreshError = null
                        try {
                            archiveRepository.refreshArchiveForDate(date)
                        } catch (error: Exception) {
                            refreshError = error.message ?: error.javaClass.simpleName
                        } finally {
                            refreshingDate = null
                        }
                    }
                },
                isRefreshing = refreshingDate == selectedDate,
                refreshError = refreshError,
                preferredGroupFilter = groupFilter,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryListScreen(
    archives: List<DailyArchiveEntity>,
    sortMode: HistoryListSort,
    onSortModeChange: (HistoryListSort) -> Unit,
    rangeFilter: HistoryRangeFilter,
    onRangeFilterChange: (HistoryRangeFilter) -> Unit,
    groupFilter: HistoryGroupFilter,
    onGroupFilterChange: (HistoryGroupFilter) -> Unit,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (archives.isEmpty()) {
        EmptyHistoryState(
            title = "\u8fd8\u6ca1\u6709\u53ef\u67e5\u770b\u7684\u5f52\u6863",
            body = "\u5386\u53f2\u9875\u53ea\u5c55\u793a\u6628\u5929\u548c\u66f4\u65e9\u7684\u5f52\u6863\u6570\u636e\u3002\u7b49\u5230\u660e\u5929\u518d\u56de\u6765\uff0c\u5c31\u80fd\u770b\u5230\u7b2c\u4e00\u6761\u8bb0\u5f55\u3002",
            modifier = modifier,
        )
        return
    }

    val latestArchiveDate = remember(archives) {
        archives.maxOf { archive -> LocalDate.parse(archive.archiveDate) }
    }
    val rangeFilteredArchives = remember(archives, rangeFilter, latestArchiveDate) {
        archives.filter { archive ->
            val date = LocalDate.parse(archive.archiveDate)
            when (rangeFilter) {
                HistoryRangeFilter.LAST_7 -> !date.isBefore(latestArchiveDate.minusDays(6))
                HistoryRangeFilter.LAST_30 -> !date.isBefore(latestArchiveDate.minusDays(29))
                HistoryRangeFilter.ALL -> true
            }
        }
    }
    val filteredArchives = remember(rangeFilteredArchives, groupFilter) {
        rangeFilteredArchives.filter { archive -> archiveMatchesGroupFilter(archive, groupFilter) }
    }
    val sortedArchives = remember(filteredArchives, sortMode, groupFilter) {
        when (sortMode) {
            HistoryListSort.DATE -> filteredArchives.sortedByDescending { it.archiveDate }
            HistoryListSort.USAGE -> filteredArchives.sortedByDescending { archive -> usageForGroupFilter(archive, groupFilter) }
            HistoryListSort.POINTS -> filteredArchives.sortedByDescending { archive -> pointsForGroupFilter(archive, groupFilter) }
            HistoryListSort.REDEMPTIONS -> filteredArchives.sortedByDescending { it.redemptionCount }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "\u5386\u53f2\u603b\u89c8",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    HistoryMetricRow(label = "\u5f52\u6863\u5929\u6570", value = "${sortedArchives.size} \u5929")
                    HistoryMetricRow(
                        label = primarySummaryLabel(groupFilter),
                        value = formatHistoryDuration(sortedArchives.sumOf { archive -> usageForGroupFilter(archive, groupFilter) }),
                    )
                    HistoryMetricRow(
                        label = summaryLabelForGroupFilter(groupFilter),
                        value = summaryValueForGroupFilter(sortedArchives, groupFilter),
                    )
                    HistoryMetricRow(label = "\u7d2f\u8ba1\u5151\u6362", value = "${sortedArchives.sumOf { it.redemptionCount }} \u6b21")

                    Text(
                        text = "\u65f6\u95f4\u8303\u56f4",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HistoryRangeFilter.entries.forEach { mode ->
                            HistoryToggleChip(
                                text = mode.label,
                                selected = rangeFilter == mode,
                                onClick = { onRangeFilterChange(mode) },
                            )
                        }
                    }

                    Text(
                        text = "\u5206\u7ec4\u8fc7\u6ee4",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HistoryGroupFilter.entries.forEach { mode ->
                            HistoryToggleChip(
                                text = mode.label,
                                selected = groupFilter == mode,
                                onClick = { onGroupFilterChange(mode) },
                            )
                        }
                    }

                    Text(
                        text = "\u6392\u5e8f\u65b9\u5f0f",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HistoryListSort.entries.forEach { mode ->
                            HistoryToggleChip(
                                text = mode.label,
                                selected = sortMode == mode,
                                onClick = { onSortModeChange(mode) },
                            )
                        }
                    }
                }
            }
        }

        if (sortedArchives.isEmpty()) {
            item {
                ListEmptyStateCard(
                    title = "\u5f53\u524d\u7b5b\u9009\u4e0b\u6ca1\u6709\u8bb0\u5f55",
                    body = "\u8bd5\u8bd5\u653e\u5bbd\u65f6\u95f4\u8303\u56f4\uff0c\u6216\u8005\u5207\u6362\u5206\u7ec4\u8fc7\u6ee4\u548c\u6392\u5e8f\u6761\u4ef6\u3002",
                )
            }
            return@LazyColumn
        }

        items(sortedArchives, key = { it.id }) { archive ->
            ElevatedCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDate(archive.archiveDate) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = archive.archiveDate,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = listCardHeadline(archive, groupFilter),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ArrowOutward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CompactMetricBlock(
                            modifier = Modifier.weight(1f),
                            label = primarySummaryLabel(groupFilter),
                            value = formatHistoryDuration(usageForGroupFilter(archive, groupFilter)),
                        )
                        CompactMetricBlock(
                            modifier = Modifier.weight(1f),
                            label = summaryLabelForGroupFilter(groupFilter),
                            value = cardSecondaryValue(archive, groupFilter),
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HistoryTagChip("\u5151\u6362 ${archive.redemptionCount} \u6b21")
                        when (groupFilter) {
                            HistoryGroupFilter.ALL -> {
                                HistoryTagChip("\u63a7\u5236\u5b8c\u6210 ${archive.controlCompletedGroupCount}")
                                HistoryTagChip("\u63a7\u5236\u8d85\u989d ${archive.controlExceededGroupCount}")
                                HistoryTagChip("\u9f13\u52b1\u8fbe\u6210 ${archive.encourageCompletedGroupCount}")
                            }
                            HistoryGroupFilter.CONTROL -> {
                                HistoryTagChip("\u5b8c\u6210 ${archive.controlCompletedGroupCount}")
                                HistoryTagChip("\u8d85\u989d ${archive.controlExceededGroupCount}")
                                if (archive.savedMillis > 0L) {
                                    HistoryTagChip("\u8282\u7701 ${formatHistoryDuration(archive.savedMillis)}")
                                }
                            }
                            HistoryGroupFilter.ENCOURAGE -> {
                                HistoryTagChip("\u8fbe\u6210 ${archive.encourageCompletedGroupCount}")
                                if (archive.pointsEarned > 0.0) {
                                    HistoryTagChip("\u83b7\u5f97 +${formatPoints(archive.pointsEarned)}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryDetailScreen(
    archive: DailyArchiveEntity?,
    groupArchives: List<DailyGroupArchiveEntity>,
    appArchives: List<DailyAppArchiveEntity>,
    previousDate: String?,
    nextDate: String?,
    onSelectDate: (String) -> Unit,
    onRefreshDate: (String) -> Unit,
    isRefreshing: Boolean,
    refreshError: String?,
    preferredGroupFilter: HistoryGroupFilter,
    modifier: Modifier = Modifier,
) {
    if (archive == null) {
        EmptyHistoryState(
            title = "\u8fd9\u4e00\u5929\u7684\u5f52\u6863\u6682\u4e0d\u53ef\u7528",
            body = "\u6570\u636e\u53ef\u80fd\u8fd8\u6ca1\u6709\u5b8c\u6210\u8bfb\u53d6\uff0c\u7a0d\u540e\u518d\u8bd5\u3002",
            modifier = modifier,
        )
        return
    }

    val controlGroups = groupArchives.filter { it.groupType == GroupType.CONTROL }
    val encourageGroups = groupArchives.filter { it.groupType == GroupType.ENCOURAGE }
    val appArchivesByGroup =
        appArchives
            .filter { it.isGrouped && it.groupId != null }
            .groupBy { it.groupId.orEmpty() }
    val ungroupedAppArchives = appArchives.filter { !it.isGrouped }
    val orderedSections = when (preferredGroupFilter) {
        HistoryGroupFilter.ENCOURAGE -> listOf(
            Triple("\u9f13\u52b1\u7ec4", MaterialTheme.colorScheme.tertiary, encourageGroups),
            Triple("\u63a7\u5236\u7ec4", MaterialTheme.colorScheme.primary, controlGroups),
        )
        else -> listOf(
            Triple("\u63a7\u5236\u7ec4", MaterialTheme.colorScheme.primary, controlGroups),
            Triple("\u9f13\u52b1\u7ec4", MaterialTheme.colorScheme.tertiary, encourageGroups),
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailDateNavigator(
            currentDate = archive.archiveDate,
            previousDate = previousDate,
            nextDate = nextDate,
            onSelectDate = onSelectDate,
            onRefreshDate = onRefreshDate,
            isRefreshing = isRefreshing,
            refreshError = refreshError,
        )

        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = archive.archiveDate,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "\u5f53\u5929\u603b\u89c8",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u603b\u65f6\u957f",
                        value = formatHistoryDuration(archive.totalUsageMillis),
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u51c0\u79ef\u5206",
                        value = formatSignedPoints(archive.pointsNet),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u63a7\u5236\u7ec4",
                        value = formatHistoryDuration(archive.controlUsageMillis),
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u9f13\u52b1\u7ec4",
                        value = formatHistoryDuration(archive.encourageUsageMillis),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u83b7\u5f97\u79ef\u5206",
                        value = "+${formatPoints(archive.pointsEarned)}",
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u5151\u6362\u6b21\u6570",
                        value = "${archive.redemptionCount} \u6b21",
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HistoryTagChip("\u63a7\u5236\u5b8c\u6210 ${archive.controlCompletedGroupCount}")
                    HistoryTagChip("\u63a7\u5236\u8d85\u989d ${archive.controlExceededGroupCount}")
                    HistoryTagChip("\u9f13\u52b1\u8fbe\u6210 ${archive.encourageCompletedGroupCount}")
                    if (archive.savedMillis > 0L) {
                        HistoryTagChip("\u8282\u7701 ${formatHistoryDuration(archive.savedMillis)}")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                HistoryMetricRow(label = "\u5f52\u6863\u5199\u5165", value = formatArchiveTimestamp(archive.updatedAt))
                HistoryMetricRow(label = "\u5f52\u6863\u7248\u672c", value = "v${archive.archiveVersion}")
            }
        }

        orderedSections.forEach { (title, color, items) ->
            GroupArchiveSection(
                title = title,
                iconTint = color,
                items = items,
                appArchivesByGroup = appArchivesByGroup,
                emptyText = "\u5f53\u5929\u6ca1\u6709${title}\u5f52\u6863\u3002",
            )
        }

        UngroupedAppArchiveSection(appItems = ungroupedAppArchives)
    }
}

@Composable
private fun DetailDateNavigator(
    currentDate: String,
    previousDate: String?,
    nextDate: String?,
    onSelectDate: (String) -> Unit,
    onRefreshDate: (String) -> Unit,
    isRefreshing: Boolean,
    refreshError: String?,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = { previousDate?.let(onSelectDate) },
                enabled = previousDate != null,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "\u4e0a\u4e00\u5929")
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "\u53ea\u5728\u5df2\u6709\u5f52\u6863\u65e5\u671f\u95f4\u8df3\u8f6c",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = { nextDate?.let(onSelectDate) },
                enabled = nextDate != null,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "\u4e0b\u4e00\u5929")
            }
            IconButton(
                onClick = { onRefreshDate(currentDate) },
                enabled = !isRefreshing,
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "\u5237\u65b0\u5f53\u5929\u5e94\u7528\u8bb0\u5f55")
            }
        }
        if (isRefreshing || refreshError != null) {
            Text(
                text =
                    if (isRefreshing) {
                        "\u6b63\u5728\u4ece\u7cfb\u7edf\u5237\u65b0\u8fd9\u4e00\u5929\u7684\u5e94\u7528\u8bb0\u5f55..."
                    } else {
                        "\u5237\u65b0\u5931\u8d25\uff1a${refreshError.orEmpty()}"
                    },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (isRefreshing) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupArchiveSection(
    title: String,
    iconTint: Color,
    items: List<DailyGroupArchiveEntity>,
    appArchivesByGroup: Map<String, List<DailyAppArchiveEntity>>,
    emptyText: String,
) {
    var sortMode by remember(title) { mutableStateOf(GroupSortMode.DEFAULT) }
    val sortedItems = remember(items, sortMode) {
        when (sortMode) {
            GroupSortMode.DEFAULT -> items.sortedBy { it.sortOrder }
            GroupSortMode.USAGE -> items.sortedByDescending { it.dailyUsageMillis }
            GroupSortMode.POINTS -> items.sortedByDescending { it.earnedPoints - it.spentPoints }
            GroupSortMode.STATUS -> items.sortedWith(
                compareBy<DailyGroupArchiveEntity> { !it.completed }.thenByDescending { it.exceededMillisAtClose },
            )
        }
    }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Insights, contentDescription = null, tint = iconTint)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (items.isEmpty()) {
                ListEmptyStateCard(
                    title = "\u8fd9\u4e00\u5929\u6ca1\u6709${title}",
                    body = emptyText,
                )
            } else {
                val totalUsage = items.sumOf { it.dailyUsageMillis }
                val totalPoints = items.sumOf { it.earnedPoints - it.spentPoints }
                val completedCount = items.count { it.completed }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u7ec4\u6570",
                        value = "${items.size} \u4e2a",
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u603b\u65f6\u957f",
                        value = formatHistoryDuration(totalUsage),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u51c0\u79ef\u5206",
                        value = formatSignedPoints(totalPoints),
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = "\u5b8c\u6210\u6570",
                        value = "$completedCount / ${items.size}",
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GroupSortMode.entries.forEach { mode ->
                        HistoryToggleChip(
                            text = mode.label,
                            selected = sortMode == mode,
                            onClick = { sortMode = mode },
                        )
                    }
                }

                sortedItems.forEachIndexed { index, item ->
                    GroupArchiveCard(
                        item = item,
                        appItems = appArchivesByGroup[item.groupId].orEmpty(),
                    )
                    if (index != sortedItems.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupArchiveCard(
    item: DailyGroupArchiveEntity,
    appItems: List<DailyAppArchiveEntity>,
) {
    var showMoreMetrics by remember(item.id) { mutableStateOf(false) }
    var appSortMode by remember(item.id) { mutableStateOf(AppSortMode.USAGE) }

    val rankedApps = remember(appItems, appSortMode) {
        when (appSortMode) {
            AppSortMode.USAGE -> appItems.sortedByDescending { it.dailyUsageMillis }
            AppSortMode.OPENS -> appItems.sortedByDescending { it.openCount }
            AppSortMode.SESSIONS -> appItems.sortedByDescending { it.sessionCount }
            AppSortMode.NIGHT -> appItems.sortedByDescending { it.nightUsageMillis }
            AppSortMode.POINTS -> appItems.sortedByDescending { it.earnedPoints }
        }
    }
    val totalOpenCount = appItems.sumOf { it.openCount }
    val totalSessionCount = appItems.sumOf { it.sessionCount }
    val totalNightUsage = appItems.sumOf { it.nightUsageMillis }
    val completedAppCount = appItems.count { it.completed }
    val coveredUsage = rankedApps.sumOf { it.dailyUsageMillis }
    val coverageRatio =
        if (item.dailyUsageMillis > 0L) {
            (coveredUsage.toFloat() / item.dailyUsageMillis.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.groupName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color =
                    if (item.completed) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    },
            ) {
                Text(
                    text =
                        if (item.completed) {
                            "\u5df2\u5b8c\u6210"
                        } else {
                            "\u672a\u5b8c\u6210"
                        },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color =
                        if (item.completed) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HistoryTagChip(groupTypeLabel(item.groupType))
            HistoryTagChip(limitPeriodLabel(item.limitPeriod))
            HistoryTagChip("\u57fa\u7840\u989d\u5ea6 ${item.limitMinutes} \u5206\u949f")
            if (item.bonusMinutes > 0) {
                HistoryTagChip("\u5956\u52b1 ${item.bonusMinutes} \u5206\u949f")
            }
            if (item.pointsPerMinute > 0.0) {
                HistoryTagChip("\u500d\u7387 ${formatPoints(item.pointsPerMinute)} / \u5206\u949f")
            }
        }

        HistoryMetricRow(label = "\u5f53\u5929\u4f7f\u7528", value = formatHistoryDuration(item.dailyUsageMillis))
        HistoryMetricRow(label = "\u5468\u671f\u7d2f\u8ba1", value = formatHistoryDuration(item.periodUsageMillisAtClose))
        HistoryMetricRow(label = "\u5269\u4f59\u989d\u5ea6", value = formatHistoryDuration(item.remainingMillisAtClose))
        HistoryMetricRow(label = "\u8d85\u989d\u65f6\u957f", value = formatHistoryDuration(item.exceededMillisAtClose))
        HistoryMetricRow(label = "\u51c0\u79ef\u5206", value = formatSignedPoints(item.earnedPoints - item.spentPoints))
        HistoryMetricRow(label = "\u5206\u7ec4\u5e94\u7528\u6570", value = "${item.packageCount} \u4e2a")
        HistoryMetricRow(label = "\u6253\u5f00\u6b21\u6570", value = "$totalOpenCount \u6b21")
        HistoryMetricRow(label = "\u4f1a\u8bdd\u6570", value = "$totalSessionCount \u6b21")

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
            modifier = Modifier.clickable { showMoreMetrics = !showMoreMetrics },
        ) {
            Text(
                text =
                    if (showMoreMetrics) {
                        "\u6536\u8d77\u66f4\u591a\u6307\u6807"
                    } else {
                        "\u5c55\u5f00\u66f4\u591a\u6307\u6807"
                    },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (showMoreMetrics) {
            HistoryMetricRow(label = "\u5468\u671f\u989d\u5ea6", value = formatHistoryDuration(item.effectiveLimitMillisAtClose))
            HistoryMetricRow(label = "\u79ef\u5206\u83b7\u5f97", value = "+${formatPoints(item.earnedPoints)}")
            HistoryMetricRow(label = "\u79ef\u5206\u6d88\u8d39", value = "-${formatPoints(item.spentPoints)}")
            HistoryMetricRow(label = "\u591c\u95f4\u4f7f\u7528", value = formatHistoryDuration(totalNightUsage))
            HistoryMetricRow(label = "\u5b8c\u6210\u5e94\u7528", value = "$completedAppCount / ${appItems.size}")
            if (rankedApps.isNotEmpty()) {
                HistoryMetricRow(label = "\u5f52\u6863\u8986\u76d6", value = "${(coverageRatio * 100).toInt()}%")
            }
        }

        if (rankedApps.size > 1) {
            AppComparisonPanel(appItems = rankedApps)
        }

        if (rankedApps.isEmpty()) {
            ListEmptyStateCard(
                title = "\u8be5\u7ec4\u6ca1\u6709\u5e94\u7528\u5f52\u6863",
                body = "\u5f53\u5929\u6ca1\u6709\u751f\u6210\u8be5\u7ec4\u5185\u5e94\u7528\u7684\u5f52\u6863\u5feb\u7167\u3002",
            )
        } else {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "\u7ec4\u5185\u5e94\u7528",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "\u9ed8\u8ba4\u53ea\u5c55\u793a\u524d 5 \u4e2a\u5e94\u7528\uff0c\u70b9\u5f00\u53ef\u770b 24 \u5c0f\u65f6\u5206\u5e03\u548c\u4f1a\u8bdd\u6307\u6807\u3002",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppSortMode.entries.forEach { mode ->
                            HistoryToggleChip(
                                text = mode.label,
                                selected = appSortMode == mode,
                                onClick = { appSortMode = mode },
                            )
                        }
                    }
                    rankedApps.take(5).forEachIndexed { index, appItem ->
                        AppArchiveRow(appItem = appItem)
                        if (index != minOf(rankedApps.lastIndex, 4)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppComparisonPanel(appItems: List<DailyAppArchiveEntity>) {
    val usageLeaders = appItems.take(3)
    val maxUsage = usageLeaders.maxOfOrNull { it.dailyUsageMillis }?.coerceAtLeast(1L) ?: 1L
    val mostOpened = appItems.maxByOrNull { it.openCount }
    val longestSessionLeader = appItems.maxByOrNull { it.longestSessionMillis }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "\u7ec4\u5185\u5bf9\u6bd4",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            usageLeaders.forEach { appItem ->
                AppUsageCompareRow(
                    appItem = appItem,
                    maxUsage = maxUsage,
                )
            }
            if (mostOpened != null || longestSessionLeader != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    mostOpened?.let { leader ->
                        ComparisonBadge(
                            modifier = Modifier.weight(1f),
                            label = "\u6253\u5f00\u6b21\u6570\u6700\u591a",
                            value = "${leader.appLabel} / ${leader.openCount} \u6b21",
                        )
                    }
                    longestSessionLeader?.let { leader ->
                        ComparisonBadge(
                            modifier = Modifier.weight(1f),
                            label = "\u6700\u957f\u5355\u6b21\u4f1a\u8bdd",
                            value = "${leader.appLabel} / ${formatHistoryDuration(leader.longestSessionMillis)}",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUsageCompareRow(
    appItem: DailyAppArchiveEntity,
    maxUsage: Long,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconCircle(appItem.packageName)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = appItem.appLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatHistoryDuration(appItem.dailyUsageMillis)} / ${appItem.openCount} \u6b21\u6253\u5f00",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp),
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {}
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth((appItem.dailyUsageMillis.toFloat() / maxUsage.toFloat()).coerceIn(0.06f, 1f))
                        .height(8.dp),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
            ) {}
        }
    }
}

@Composable
private fun ComparisonBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppArchiveRow(appItem: DailyAppArchiveEntity) {
    var expanded by remember(appItem.id) { mutableStateOf(false) }
    val hourlyBuckets = remember(appItem) { appHourlyBuckets(appItem) }
    val peakHour = remember(hourlyBuckets) { hourlyBuckets.indices.maxByOrNull { hourlyBuckets[it] } ?: 0 }
    val activeHourCount = remember(hourlyBuckets) { hourlyBuckets.count { it > 0L } }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { expanded = !expanded },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconCircle(appItem.packageName)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = appItem.appLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text =
                        buildString {
                            append(formatHistoryDuration(appItem.dailyUsageMillis))
                            append(" / ")
                            append(appItem.openCount)
                            append(" \u6b21\u6253\u5f00")
                            if (appItem.nightUsageMillis > 0L) {
                                append(" / \u591c\u95f4 ")
                                append(formatHistoryDuration(appItem.nightUsageMillis))
                            }
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (appItem.isGrouped) {
                        HistoryTagChip(
                            if (appItem.completed) {
                                "\u5df2\u5b8c\u6210"
                            } else {
                                "\u672a\u5b8c\u6210"
                            },
                        )
                        HistoryTagChip(limitPeriodLabel(appItem.limitPeriod))
                    } else {
                        HistoryTagChip("\u672a\u5206\u7ec4")
                    }
                    HistoryTagChip("${appItem.sessionCount} \u6b21\u4f1a\u8bdd")
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "+${formatPoints(appItem.earnedPoints)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text =
                        if (expanded) {
                            "\u6536\u8d77\u8be6\u60c5"
                        } else {
                            "\u5c55\u5f00\u8be6\u60c5"
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (expanded) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HistoryMetricRow(
                        label = "\u6700\u957f\u5355\u6b21\u4f1a\u8bdd",
                        value = formatHistoryDuration(appItem.longestSessionMillis),
                    )
                    HistoryMetricRow(
                        label = "\u6253\u5f00\u6b21\u6570",
                        value = "${appItem.openCount} \u6b21",
                    )
                    HistoryMetricRow(
                        label = "\u4f1a\u8bdd\u6570",
                        value = "${appItem.sessionCount} \u6b21",
                    )
                    HistoryMetricRow(
                        label = "\u6d3b\u8dc3\u5c0f\u65f6",
                        value = "$activeHourCount \u5c0f\u65f6",
                    )
                    HistoryMetricRow(
                        label = "\u5cf0\u503c\u65f6\u6bb5",
                        value = "${peakHour.toString().padStart(2, '0')}:00",
                    )
                    HistoryMetricRow(
                        label = "\u591c\u95f4\u4f7f\u7528",
                        value = formatHistoryDuration(appItem.nightUsageMillis),
                    )
                    HistoryMetricRow(
                        label = "\u79ef\u5206\u83b7\u5f97",
                        value = "+${formatPoints(appItem.earnedPoints)}",
                    )
                    HistoryMetricRow(
                        label = "\u6240\u5c5e\u5206\u7ec4",
                        value = appItem.groupName ?: "\u672a\u5206\u7ec4\u5e94\u7528",
                    )
                    HistoryMetricRow(
                        label = "\u5468\u671f",
                        value = limitPeriodLabel(appItem.limitPeriod),
                    )
                    Text(
                        text = "24 \u5c0f\u65f6\u5206\u5e03",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AppHourlyDistributionChart(hourlyBuckets = hourlyBuckets)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UngroupedAppArchiveSection(appItems: List<DailyAppArchiveEntity>) {
    var appSortMode by remember { mutableStateOf(AppSortMode.USAGE) }
    val rankedApps =
        remember(appItems, appSortMode) {
            when (appSortMode) {
                AppSortMode.USAGE -> appItems.sortedByDescending { it.dailyUsageMillis }
                AppSortMode.OPENS -> appItems.sortedByDescending { it.openCount }
                AppSortMode.SESSIONS -> appItems.sortedByDescending { it.sessionCount }
                AppSortMode.NIGHT -> appItems.sortedByDescending { it.nightUsageMillis }
                AppSortMode.POINTS -> appItems.sortedByDescending { it.earnedPoints }
            }
        }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "\u672a\u5206\u7ec4\u5e94\u7528",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "\u4ec5\u5f52\u6863\u5f53\u5929\u4f7f\u7528\u8d85\u8fc7 1 \u5206\u949f\u7684\u672a\u5206\u7ec4\u5e94\u7528\u3002",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (rankedApps.isEmpty()) {
                ListEmptyStateCard(
                    title = "\u8fd9\u4e00\u5929\u6ca1\u6709\u672a\u5206\u7ec4\u5e94\u7528",
                    body = "\u672a\u5206\u7ec4\u5e94\u7528\u4f7f\u7528\u4e0d\u8db3 1 \u5206\u949f\u65f6\u4e0d\u4f1a\u8fdb\u5165\u957f\u671f\u5f52\u6863\u3002",
                )
                return@ElevatedCard
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DashboardMetric(
                    modifier = Modifier.weight(1f),
                    label = "\u5e94\u7528\u6570",
                    value = "${rankedApps.size} \u4e2a",
                )
                DashboardMetric(
                    modifier = Modifier.weight(1f),
                    label = "\u603b\u65f6\u957f",
                    value = formatHistoryDuration(rankedApps.sumOf { it.dailyUsageMillis }),
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppSortMode.entries.forEach { mode ->
                    HistoryToggleChip(
                        text = mode.label,
                        selected = appSortMode == mode,
                        onClick = { appSortMode = mode },
                    )
                }
            }

            rankedApps.take(10).forEachIndexed { index, appItem ->
                AppArchiveRow(appItem = appItem)
                if (index != minOf(rankedApps.lastIndex, 9)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f))
                }
            }
        }
    }
}

@Composable
private fun AppHourlyDistributionChart(hourlyBuckets: LongArray) {
    val maxValue = hourlyBuckets.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val barColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(84.dp),
        ) {
            val slotWidth = size.width / hourlyBuckets.size
            val barWidth = slotWidth * 0.58f

            repeat(3) { index ->
                val y = size.height - (index * (size.height / 2f))
                drawLine(
                    color = guideColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
            }

            hourlyBuckets.forEachIndexed { index, value ->
                val x = slotWidth * index + (slotWidth - barWidth) / 2f
                val barHeight =
                    if (value > 0L) {
                        maxOf(4f, size.height * (value.toFloat() / maxValue.toFloat()))
                    } else {
                        0f
                    }
                drawRoundRect(
                    color = barColor.copy(alpha = if (value > 0L) 0.88f else 0.18f),
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("00:00", "06:00", "12:00", "18:00", "24:00").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ListEmptyStateCard(
    title: String,
    body: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistoryMetricRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(0.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CompactMetricBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DashboardMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun HistoryTagChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun HistoryToggleChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color =
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            },
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

private fun archiveMatchesGroupFilter(
    archive: DailyArchiveEntity,
    filter: HistoryGroupFilter,
): Boolean {
    return when (filter) {
        HistoryGroupFilter.ALL -> true
        HistoryGroupFilter.CONTROL ->
            archive.controlUsageMillis > 0L ||
                archive.controlCompletedGroupCount > 0 ||
                archive.controlExceededGroupCount > 0 ||
                archive.savedMillis > 0L
        HistoryGroupFilter.ENCOURAGE ->
            archive.encourageUsageMillis > 0L ||
                archive.encourageCompletedGroupCount > 0 ||
                archive.pointsEarned > 0.0
    }
}

private fun usageForGroupFilter(
    archive: DailyArchiveEntity,
    filter: HistoryGroupFilter,
): Long {
    return when (filter) {
        HistoryGroupFilter.ALL -> archive.totalUsageMillis
        HistoryGroupFilter.CONTROL -> archive.controlUsageMillis
        HistoryGroupFilter.ENCOURAGE -> archive.encourageUsageMillis
    }
}

private fun pointsForGroupFilter(
    archive: DailyArchiveEntity,
    filter: HistoryGroupFilter,
): Double {
    return when (filter) {
        HistoryGroupFilter.ALL -> archive.pointsNet
        HistoryGroupFilter.CONTROL -> archive.savedMillis.toDouble()
        HistoryGroupFilter.ENCOURAGE -> archive.pointsEarned
    }
}

private fun primarySummaryLabel(filter: HistoryGroupFilter): String {
    return when (filter) {
        HistoryGroupFilter.ALL -> "\u7d2f\u8ba1\u4f7f\u7528"
        HistoryGroupFilter.CONTROL -> "\u63a7\u5236\u7ec4\u65f6\u957f"
        HistoryGroupFilter.ENCOURAGE -> "\u9f13\u52b1\u7ec4\u65f6\u957f"
    }
}

private fun summaryLabelForGroupFilter(filter: HistoryGroupFilter): String {
    return when (filter) {
        HistoryGroupFilter.ALL -> "\u51c0\u79ef\u5206\u53d8\u5316"
        HistoryGroupFilter.CONTROL -> "\u63a7\u5236\u7ec4\u8282\u7701"
        HistoryGroupFilter.ENCOURAGE -> "\u83b7\u5f97\u79ef\u5206"
    }
}

private fun summaryValueForGroupFilter(
    archives: List<DailyArchiveEntity>,
    filter: HistoryGroupFilter,
): String {
    return when (filter) {
        HistoryGroupFilter.ALL -> formatSignedPoints(archives.sumOf { it.pointsNet })
        HistoryGroupFilter.CONTROL -> formatHistoryDuration(archives.sumOf { it.savedMillis })
        HistoryGroupFilter.ENCOURAGE -> "+${formatPoints(archives.sumOf { it.pointsEarned })}"
    }
}

private fun listCardHeadline(
    archive: DailyArchiveEntity,
    filter: HistoryGroupFilter,
): String {
    return when (filter) {
        HistoryGroupFilter.ALL -> "\u63a7\u5236 ${archive.controlCompletedGroupCount} \u5b8c\u6210 / \u9f13\u52b1 ${archive.encourageCompletedGroupCount} \u8fbe\u6210"
        HistoryGroupFilter.CONTROL -> "\u5b8c\u6210 ${archive.controlCompletedGroupCount} / \u8d85\u989d ${archive.controlExceededGroupCount}"
        HistoryGroupFilter.ENCOURAGE -> "\u8fbe\u6210 ${archive.encourageCompletedGroupCount} / \u83b7\u5f97 +${formatPoints(archive.pointsEarned)}"
    }
}

private fun cardSecondaryValue(
    archive: DailyArchiveEntity,
    filter: HistoryGroupFilter,
): String {
    return when (filter) {
        HistoryGroupFilter.ALL -> formatSignedPoints(archive.pointsNet)
        HistoryGroupFilter.CONTROL -> {
            if (archive.savedMillis > 0L) {
                formatHistoryDuration(archive.savedMillis)
            } else {
                "\u5b8c\u6210 ${archive.controlCompletedGroupCount} / \u8d85\u989d ${archive.controlExceededGroupCount}"
            }
        }
        HistoryGroupFilter.ENCOURAGE -> {
            if (archive.pointsEarned > 0.0) {
                "+${formatPoints(archive.pointsEarned)}"
            } else {
                "\u8fbe\u6210 ${archive.encourageCompletedGroupCount}"
            }
        }
    }
}

private fun formatPoints(value: Double): String = String.format(Locale.CHINA, "%.1f", value)

private fun formatSignedPoints(value: Double): String {
    return if (value >= 0) {
        "+${formatPoints(value)}"
    } else {
        formatPoints(value)
    }
}

private fun formatHistoryDuration(durationMillis: Long): String {
    if (durationMillis <= 0L) return "0m"
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private fun groupTypeLabel(groupType: GroupType): String {
    return when (groupType) {
        GroupType.CONTROL -> "\u63a7\u5236\u7ec4"
        GroupType.ENCOURAGE -> "\u9f13\u52b1\u7ec4"
    }
}

private fun limitPeriodLabel(limitPeriod: LimitPeriod?): String {
    return when (limitPeriod) {
        LimitPeriod.DAILY -> "\u6309\u65e5"
        LimitPeriod.WEEKLY -> "\u6309\u5468"
        LimitPeriod.MONTHLY -> "\u6309\u6708"
        null -> "\u672a\u5206\u7ec4"
    }
}

private fun formatArchiveTimestamp(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--"
    return Instant
        .ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("M/d HH:mm", Locale.CHINA))
}

private fun appHourlyBuckets(appItem: DailyAppArchiveEntity): LongArray {
    return longArrayOf(
        appItem.hour00Millis,
        appItem.hour01Millis,
        appItem.hour02Millis,
        appItem.hour03Millis,
        appItem.hour04Millis,
        appItem.hour05Millis,
        appItem.hour06Millis,
        appItem.hour07Millis,
        appItem.hour08Millis,
        appItem.hour09Millis,
        appItem.hour10Millis,
        appItem.hour11Millis,
        appItem.hour12Millis,
        appItem.hour13Millis,
        appItem.hour14Millis,
        appItem.hour15Millis,
        appItem.hour16Millis,
        appItem.hour17Millis,
        appItem.hour18Millis,
        appItem.hour19Millis,
        appItem.hour20Millis,
        appItem.hour21Millis,
        appItem.hour22Millis,
        appItem.hour23Millis,
    )
}
