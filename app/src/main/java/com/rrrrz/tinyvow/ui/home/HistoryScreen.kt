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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.rrrrz.tinyvow.BuildConfig
import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.ProtectionEventEntity
import com.rrrrz.tinyvow.data.db.RewardEffectBenefitEntity
import com.rrrrz.tinyvow.data.db.RewardEffectBenefitType
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.repository.ProtectionEventRepository
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowDetailScaffold
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import org.json.JSONArray

private enum class HistoryListSort {
    DATE,
    USAGE,
    POINTS,
    REDEMPTIONS,
}

private enum class HistoryRangeFilter {
    LAST_7,
    LAST_30,
    ALL,
}

private enum class HistoryGroupFilter {
    ALL,
    CONTROL,
    ENCOURAGE,
}

private enum class GroupSortMode {
    DEFAULT,
    USAGE,
    POINTS,
    STATUS,
}

private enum class AppSortMode {
    USAGE,
    OPENS,
    SESSIONS,
    NIGHT,
    POINTS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRoute(
    archiveRepository: DailyArchiveRepository,
    protectionEventRepository: ProtectionEventRepository,
    onBack: () -> Unit,
) {
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }
    var listSortMode by rememberSaveable { mutableStateOf(HistoryListSort.DATE) }
    var rangeFilter by rememberSaveable { mutableStateOf(HistoryRangeFilter.LAST_30) }
    var groupFilter by rememberSaveable { mutableStateOf(HistoryGroupFilter.ALL) }
    var refreshingDate by rememberSaveable { mutableStateOf<String?>(null) }
    var refreshError by rememberSaveable { mutableStateOf<String?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val coroutineScope = rememberCoroutineScope()

    val archives by archiveRepository.getRecentArchives().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val protectionEvents by protectionEventRepository.observeAll().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val currentArchive by (selectedDate?.let { archiveRepository.getArchiveByDate(it) })
        ?.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle) ?: remember { mutableStateOf<DailyArchiveEntity?>(null) }
    val groupArchives by (selectedDate?.let { archiveRepository.getGroupArchivesByDate(it) })
        ?.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle) ?: remember { mutableStateOf(emptyList()) }
    val appArchives by (selectedDate?.let { archiveRepository.getAppArchivesByDate(it) })
        ?.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle) ?: remember { mutableStateOf(emptyList()) }
    val rewardEffectBenefits by (selectedDate?.let { archiveRepository.getRewardEffectBenefitsByDate(it) })
        ?.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle) ?: remember { mutableStateOf(emptyList()) }
    val themeColors = LocalThemeColors.current
    val protectionEventsByDate = remember(protectionEvents) { protectionEvents.groupBy { it.eventDate } }

    val archiveDatesDesc = remember(archives) { archives.map { it.archiveDate }.sortedDescending() }
    val selectedIndex = remember(selectedDate, archiveDatesDesc) { archiveDatesDesc.indexOf(selectedDate) }
    val previousDate = selectedIndex.takeIf { it >= 0 }?.let { archiveDatesDesc.getOrNull(it + 1) }
    val nextDate = selectedIndex.takeIf { it >= 0 }?.let { archiveDatesDesc.getOrNull(it - 1) }

    BackHandler(enabled = selectedDate != null) {
        selectedDate = null
    }

    TinyVowDetailScaffold(
        title = if (selectedDate == null) {
            AppText.t("history_usage_history")
        } else {
            AppText.t("history_archive_details")
        },
        onBack = {
            if (selectedDate == null) {
                onBack()
            } else {
                selectedDate = null
            }
        },
        navigationContentDescription = AppText.t("group_back"),
    ) {
        if (selectedDate == null) {
            HistoryListScreen(
                archives = archives,
                protectionEventsByDate = protectionEventsByDate,
                sortMode = listSortMode,
                onSortModeChange = { listSortMode = it },
                rangeFilter = rangeFilter,
                onRangeFilterChange = { rangeFilter = it },
                groupFilter = groupFilter,
                onGroupFilterChange = { groupFilter = it },
                onSelectDate = { selectedDate = it },
            )
        } else {
            HistoryDetailScreen(
                archive = currentArchive,
                groupArchives = groupArchives,
                appArchives = appArchives,
                protectionEvents = selectedDate?.let { protectionEventsByDate[it] }.orEmpty(),
                rewardEffectBenefits = rewardEffectBenefits,
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
                showDebugRebuild = BuildConfig.DEBUG,
                preferredGroupFilter = groupFilter,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryListScreen(
    archives: List<DailyArchiveEntity>,
    protectionEventsByDate: Map<String, List<ProtectionEventEntity>>,
    sortMode: HistoryListSort,
    onSortModeChange: (HistoryListSort) -> Unit,
    rangeFilter: HistoryRangeFilter,
    onRangeFilterChange: (HistoryRangeFilter) -> Unit,
    groupFilter: HistoryGroupFilter,
    onGroupFilterChange: (HistoryGroupFilter) -> Unit,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    if (archives.isEmpty()) {
        EmptyHistoryState(
            title = AppText.t("history_empty_title"),
            body = AppText.t("history_empty_body"),
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
        contentPadding = PaddingValues(
            start = TinyVowSpacing.PageHorizontal,
            top = TinyVowSpacing.PageTop,
            end = TinyVowSpacing.PageHorizontal,
            bottom = TinyVowSpacing.PageTop,
        ),
        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
    ) {
        item {
            TinyVowCard {
                Column(
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                    verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
                ) {
                    Text(
                        text = AppText.t("history_overview"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                    HistoryMetricRow(label = AppText.t("history_archived_days"), value = historyDaysValue(sortedArchives.size))
                    HistoryMetricRow(
                        label = primarySummaryLabel(groupFilter),
                        value = formatHistoryDuration(sortedArchives.sumOf { archive -> usageForGroupFilter(archive, groupFilter) }),
                    )
                    HistoryMetricRow(
                        label = summaryLabelForGroupFilter(groupFilter),
                        value = summaryValueForGroupFilter(sortedArchives, groupFilter),
                    )
                    HistoryMetricRow(label = AppText.t("history_total_redemptions"), value = historyTimesValue(sortedArchives.sumOf { it.redemptionCount }))

                    Text(
                        text = AppText.t("history_time_range"),
                        style = MaterialTheme.typography.labelLarge,
                        color = themeColors.inkFaint,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HistoryRangeFilter.entries.forEach { mode ->
                            HistoryToggleChip(
                                text = historyRangeFilterLabel(mode),
                                selected = rangeFilter == mode,
                                onClick = { onRangeFilterChange(mode) },
                            )
                        }
                    }

                    Text(
                        text = AppText.t("history_group_filter"),
                        style = MaterialTheme.typography.labelLarge,
                        color = themeColors.inkFaint,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HistoryGroupFilter.entries.forEach { mode ->
                            HistoryToggleChip(
                                text = historyGroupFilterLabel(mode),
                                selected = groupFilter == mode,
                                onClick = { onGroupFilterChange(mode) },
                            )
                        }
                    }

                    Text(
                        text = AppText.t("history_sort_mode"),
                        style = MaterialTheme.typography.labelLarge,
                        color = themeColors.inkFaint,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HistoryListSort.entries.forEach { mode ->
                            HistoryToggleChip(
                                text = historyListSortLabel(mode),
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
                    title = AppText.t("history_filter_empty_title"),
                    body = AppText.t("history_filter_empty_body"),
                )
            }
            return@LazyColumn
        }

        items(sortedArchives, key = { it.id }) { archive ->
            TinyVowCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDate(archive.archiveDate) },
                shape = RoundedCornerShape(TinyVowRadius.Card),
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                    verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = themeColors.ink,
                                )
                                Text(
                                    text = listCardHeadline(archive, groupFilter),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themeColors.inkMuted,
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ArrowOutward,
                            contentDescription = null,
                            tint = themeColors.inkFaint,
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
                        protectionEventsByDate[archive.archiveDate]?.takeIf { it.isNotEmpty() }?.let { events ->
                            HistoryTagChip(AppText.t("history_tag_key_setting_changes", events.size))
                        }
                        HistoryTagChip(AppText.t("history_tag_redemptions", archive.redemptionCount))
                        when (groupFilter) {
                            HistoryGroupFilter.ALL -> {
                                HistoryTagChip(AppText.t("history_tag_control_completed", archive.controlCompletedGroupCount))
                                HistoryTagChip(AppText.t("history_tag_control_exceeded", archive.controlExceededGroupCount))
                                HistoryTagChip(AppText.t("history_tag_encourage_completed", archive.encourageCompletedGroupCount))
                            }
                            HistoryGroupFilter.CONTROL -> {
                                HistoryTagChip(AppText.t("history_tag_completed", archive.controlCompletedGroupCount))
                                HistoryTagChip(AppText.t("history_tag_exceeded", archive.controlExceededGroupCount))
                                if (archive.savedMillis > 0L) {
                                    HistoryTagChip(AppText.t("history_tag_saved", formatHistoryDuration(archive.savedMillis)))
                                }
                            }
                            HistoryGroupFilter.ENCOURAGE -> {
                                HistoryTagChip(AppText.t("history_tag_achieved", archive.encourageCompletedGroupCount))
                                if (archive.pointsEarned > 0.0) {
                                    HistoryTagChip(AppText.t("history_tag_earned_points", formatPoints(archive.pointsEarned)))
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
    protectionEvents: List<ProtectionEventEntity>,
    rewardEffectBenefits: List<RewardEffectBenefitEntity>,
    previousDate: String?,
    nextDate: String?,
    onSelectDate: (String) -> Unit,
    onRefreshDate: (String) -> Unit,
    isRefreshing: Boolean,
    refreshError: String?,
    showDebugRebuild: Boolean,
    preferredGroupFilter: HistoryGroupFilter,
    modifier: Modifier = Modifier,
) {
    if (archive == null) {
        EmptyHistoryState(
            title = AppText.t("history_archive_unavailable_title"),
            body = AppText.t("history_archive_unavailable_body"),
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
            Triple(AppText.t("history_filter_encourage"), MaterialTheme.colorScheme.tertiary, encourageGroups),
            Triple(AppText.t("history_filter_control"), MaterialTheme.colorScheme.primary, controlGroups),
        )
        else -> listOf(
            Triple(AppText.t("history_filter_control"), MaterialTheme.colorScheme.primary, controlGroups),
            Triple(AppText.t("history_filter_encourage"), MaterialTheme.colorScheme.tertiary, encourageGroups),
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = TinyVowSpacing.PageHorizontal,
                    top = TinyVowSpacing.PageTop,
                    end = TinyVowSpacing.PageHorizontal,
                    bottom = TinyVowSpacing.PageTop,
                ),
        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
    ) {
        DetailDateNavigator(
            currentDate = archive.archiveDate,
            previousDate = previousDate,
            nextDate = nextDate,
            onSelectDate = onSelectDate,
            onRefreshDate = onRefreshDate,
            isRefreshing = isRefreshing,
            refreshError = refreshError,
            showDebugRebuild = showDebugRebuild,
        )

        TinyVowCard {
            Column(
                modifier = Modifier.padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CardVertical,
                ),
                verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
            ) {
                Text(
                    text = archive.archiveDate,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = AppText.t("history_daily_overview"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = AppText.t("history_total_duration"),
                        value = formatHistoryDuration(archive.totalUsageMillis),
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = AppText.t("history_net_points"),
                        value = formatSignedPoints(archive.pointsNet),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = AppText.t("history_filter_control"),
                        value = formatHistoryDuration(archive.controlUsageMillis),
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = AppText.t("history_filter_encourage"),
                        value = formatHistoryDuration(archive.encourageUsageMillis),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = AppText.t("history_points_earned"),
                        value = "+${formatPoints(archive.pointsEarned)}",
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = AppText.t("history_redemption_count"),
                        value = historyTimesValue(archive.redemptionCount),
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HistoryTagChip(AppText.t("history_tag_control_completed", archive.controlCompletedGroupCount))
                    HistoryTagChip(AppText.t("history_tag_control_exceeded", archive.controlExceededGroupCount))
                    HistoryTagChip(AppText.t("history_tag_encourage_completed", archive.encourageCompletedGroupCount))
                    if (archive.savedMillis > 0L) {
                        HistoryTagChip(AppText.t("history_tag_saved", formatHistoryDuration(archive.savedMillis)))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                HistoryMetricRow(label = AppText.t("history_archive_written"), value = formatArchiveTimestamp(archive.updatedAt))
                HistoryMetricRow(label = AppText.t("history_archive_version"), value = "v${archive.archiveVersion}")
            }
        }

        ProtectionEventSection(events = protectionEvents)

        RewardEffectBenefitSection(benefits = rewardEffectBenefits)

        orderedSections.forEach { (title, color, items) ->
            GroupArchiveSection(
                title = title,
                iconTint = color,
                items = items,
                appArchivesByGroup = appArchivesByGroup,
                emptyText = AppText.t("history_no_group_archive_for_type", title),
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
    showDebugRebuild: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("stats_previous_day"))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = AppText.t("history_only_jump_between_archived_dates"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = { nextDate?.let(onSelectDate) },
                    enabled = nextDate != null,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = AppText.t("stats_next_day"))
                }
                IconButton(
                    onClick = { onRefreshDate(currentDate) },
                    enabled = !isRefreshing,
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = AppText.t("stats_refresh_day"))
                }
            }
            if (showDebugRebuild) {
                OutlinedButton(
                    onClick = { onRefreshDate(currentDate) },
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                ) {
                    Text(AppText.t("history_debug_rebuild_archive"))
                }
            }
            if (isRefreshing || refreshError != null) {
                Text(
                    text =
                        if (isRefreshing) {
                            AppText.t("history_refreshing_archive")
                        } else {
                            AppText.t("history_refresh_failed", refreshError.orEmpty())
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
}

@Composable
private fun ProtectionEventSection(events: List<ProtectionEventEntity>) {
    if (events.isEmpty()) return
    TinyVowCard {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = AppText.t("history_key_setting_changes"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            events.sortedByDescending { it.occurredAt }.forEach { event ->
                ProtectionEventRow(event = event)
            }
        }
    }
}

@Composable
private fun ProtectionEventRow(event: ProtectionEventEntity) {
    val themeColors = LocalThemeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = AppText.t(event.titleKey),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatEventTime(event.occurredAt),
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.inkMuted,
            )
        }
        Text(
            text = localizedProtectionEventMessage(event),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
        )
        if (event.withinWindow == false) {
            HistoryTagChip(AppText.t("history_tag_outside_window_attempt"))
        }
    }
}

@Composable
private fun RewardEffectBenefitSection(benefits: List<RewardEffectBenefitEntity>) {
    if (benefits.isEmpty()) return
    TinyVowCard {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            Text(
                text = AppText.t("history_reward_effect_benefits"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = LocalThemeColors.current.inkStrong,
            )
            benefits.forEachIndexed { index, benefit ->
                RewardEffectBenefitRow(benefit = benefit)
                if (index != benefits.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f))
                }
            }
        }
    }
}

@Composable
private fun RewardEffectBenefitRow(benefit: RewardEffectBenefitEntity) {
    val target = benefit.targetGroupNameSnapshot ?: AppText.t("generic_target_group")
    val value =
        when (benefit.benefitType) {
            RewardEffectBenefitType.EXTRA_TIME_USED,
            RewardEffectBenefitType.EMERGENCY_UNLOCK_USED,
            RewardEffectBenefitType.PERIOD_PASS_EXEMPTED
            -> formatHistoryDuration(benefit.benefitMinutes * 60_000L)
            RewardEffectBenefitType.DOUBLE_POINTS_EARNED -> "+${formatPoints(benefit.benefitPoints)}"
            RewardEffectBenefitType.STREAK_SHIELD_USED -> AppText.t("history_value_times", 1)
        }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = benefit.rewardBuiltinKey?.let { AppText.t("${it}_title") } ?: AppText.t("redeem_effects_unknown_reward"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = LocalThemeColors.current.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = rewardEffectBenefitDescription(benefit, target),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun rewardEffectBenefitDescription(
    benefit: RewardEffectBenefitEntity,
    target: String,
): String =
    when (benefit.benefitType) {
        RewardEffectBenefitType.EXTRA_TIME_USED -> AppText.t("redeem_effects_benefit_extra_time", target, benefit.benefitMinutes)
        RewardEffectBenefitType.EMERGENCY_UNLOCK_USED -> AppText.t("redeem_effects_benefit_emergency_unlock", target, benefit.benefitMinutes)
        RewardEffectBenefitType.PERIOD_PASS_EXEMPTED -> AppText.t("redeem_effects_benefit_period_pass", target, benefit.benefitMinutes)
        RewardEffectBenefitType.DOUBLE_POINTS_EARNED -> AppText.t("redeem_effects_benefit_double_points", target, formatPoints(benefit.benefitPoints))
        RewardEffectBenefitType.STREAK_SHIELD_USED -> AppText.t("redeem_effects_benefit_streak_shield")
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

    TinyVowCard {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Insights, contentDescription = null, tint = iconTint)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (items.isEmpty()) {
                ListEmptyStateCard(
                    title = AppText.t("history_no_group_section_title", title),
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
                        label = AppText.t("history_group_count"),
                        value = historyCountValue(items.size),
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = AppText.t("history_total_duration"),
                        value = formatHistoryDuration(totalUsage),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = AppText.t("history_net_points"),
                        value = formatSignedPoints(totalPoints),
                    )
                    DashboardMetric(
                        modifier = Modifier.weight(1f),
                        label = AppText.t("history_completed_count"),
                        value = "$completedCount / ${items.size}",
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GroupSortMode.entries.forEach { mode ->
                        HistoryToggleChip(
                            text = groupSortModeLabel(mode),
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
                fontWeight = FontWeight.SemiBold,
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
                            AppText.t("history_completed")
                        } else {
                            AppText.t("history_not_completed")
                        },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color =
                        if (item.completed) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HistoryTagChip(groupTypeLabel(item.groupType))
            HistoryTagChip(limitPeriodLabel(item.limitPeriod))
            HistoryTagChip(AppText.t("history_base_limit_minutes", item.limitMinutes))
            if (item.bonusMinutes > 0) {
                HistoryTagChip(AppText.t("history_bonus_minutes", item.bonusMinutes))
            }
            if (item.pointsPerMinute > 0.0) {
                HistoryTagChip(AppText.t("history_points_rate_per_minute", formatPoints(item.pointsPerMinute)))
            }
        }

        HistoryMetricRow(label = AppText.t("history_daily_usage"), value = formatHistoryDuration(item.dailyUsageMillis))
        HistoryMetricRow(label = AppText.t("history_period_total"), value = formatHistoryDuration(item.periodUsageMillisAtClose))
        HistoryMetricRow(label = AppText.t("history_remaining_limit"), value = formatHistoryDuration(item.remainingMillisAtClose))
        HistoryMetricRow(label = AppText.t("history_exceeded_duration"), value = formatHistoryDuration(item.exceededMillisAtClose))
        HistoryMetricRow(label = AppText.t("history_net_points"), value = formatSignedPoints(item.earnedPoints - item.spentPoints))
        HistoryMetricRow(label = AppText.t("history_grouped_app_count"), value = historyCountValue(item.packageCount))
        HistoryMetricRow(label = AppText.t("history_open_count"), value = historyTimesValue(totalOpenCount))
        HistoryMetricRow(label = AppText.t("history_session_count"), value = historyTimesValue(totalSessionCount))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
            modifier = Modifier.clickable { showMoreMetrics = !showMoreMetrics },
        ) {
            Text(
                text =
                    if (showMoreMetrics) {
                        AppText.t("history_collapse_more_metrics")
                    } else {
                        AppText.t("history_expand_more_metrics")
                    },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }

        if (showMoreMetrics) {
            HistoryMetricRow(label = AppText.t("history_effective_limit"), value = formatHistoryDuration(item.effectiveLimitMillisAtClose))
            HistoryMetricRow(label = AppText.t("history_points_earned"), value = "+${formatPoints(item.earnedPoints)}")
            HistoryMetricRow(label = AppText.t("history_points_spent"), value = "-${formatPoints(item.spentPoints)}")
            HistoryMetricRow(label = AppText.t("history_night_usage"), value = formatHistoryDuration(totalNightUsage))
            HistoryMetricRow(label = AppText.t("history_completed_apps"), value = "$completedAppCount / ${appItems.size}")
            if (rankedApps.isNotEmpty()) {
                HistoryMetricRow(label = AppText.t("history_archive_coverage"), value = "${(coverageRatio * 100).toInt()}%")
            }
        }

        if (rankedApps.size > 1) {
            AppComparisonPanel(appItems = rankedApps)
        }

        if (rankedApps.isEmpty()) {
            ListEmptyStateCard(
                title = AppText.t("history_group_apps_empty_title"),
                body = AppText.t("history_group_apps_empty_body"),
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
                        text = AppText.t("history_group_apps"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = AppText.t("history_group_apps_hint"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppSortMode.entries.forEach { mode ->
                            HistoryToggleChip(
                                text = appSortModeLabel(mode),
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
                text = AppText.t("history_group_app_comparison"),
                style = MaterialTheme.typography.labelLarge,
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
                            label = AppText.t("history_most_opened"),
                            value = AppText.t("history_app_times_value", leader.appLabel, leader.openCount),
                        )
                    }
                    longestSessionLeader?.let { leader ->
                        ComparisonBadge(
                            modifier = Modifier.weight(1f),
                            label = AppText.t("history_longest_session"),
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
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = AppText.t("history_app_usage_opens_value", formatHistoryDuration(appItem.dailyUsageMillis), appItem.openCount),
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
                fontWeight = FontWeight.Medium,
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
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text =
                        buildString {
                            append(formatHistoryDuration(appItem.dailyUsageMillis))
                            append(" / ")
                            append(AppText.t("history_open_times_suffix", appItem.openCount))
                            if (appItem.nightUsageMillis > 0L) {
                                append(AppText.t("history_night_suffix"))
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
                                AppText.t("history_completed")
                            } else {
                                AppText.t("history_not_completed")
                            },
                        )
                        HistoryTagChip(limitPeriodLabel(appItem.limitPeriod))
                    } else {
                        HistoryTagChip(AppText.t("history_ungrouped"))
                    }
                    HistoryTagChip(AppText.t("history_session_times", appItem.sessionCount))
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "+${formatPoints(appItem.earnedPoints)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text =
                        if (expanded) {
                            AppText.t("history_collapse_details")
                        } else {
                            AppText.t("history_expand_details")
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
                        label = AppText.t("history_longest_session"),
                        value = formatHistoryDuration(appItem.longestSessionMillis),
                    )
                    HistoryMetricRow(
                        label = AppText.t("history_open_count"),
                        value = historyTimesValue(appItem.openCount),
                    )
                    HistoryMetricRow(
                        label = AppText.t("history_session_count"),
                        value = historyTimesValue(appItem.sessionCount),
                    )
                    HistoryMetricRow(
                        label = AppText.t("history_active_hours"),
                        value = AppText.t("history_value_hours", activeHourCount),
                    )
                    HistoryMetricRow(
                        label = AppText.t("history_peak_hour"),
                        value = "${peakHour.toString().padStart(2, '0')}:00",
                    )
                    HistoryMetricRow(
                        label = AppText.t("history_night_usage"),
                        value = formatHistoryDuration(appItem.nightUsageMillis),
                    )
                    HistoryMetricRow(
                        label = AppText.t("history_points_earned"),
                        value = "+${formatPoints(appItem.earnedPoints)}",
                    )
                    HistoryMetricRow(
                        label = AppText.t("history_belongs_to_group"),
                        value = appItem.groupName ?: AppText.t("history_ungrouped_apps"),
                    )
                    HistoryMetricRow(
                        label = AppText.t("history_period"),
                        value = limitPeriodLabel(appItem.limitPeriod),
                    )
                    Text(
                        text = AppText.t("history_24_hour_distribution"),
                        style = MaterialTheme.typography.labelLarge,
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

    TinyVowCard {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(
                    text = AppText.t("history_ungrouped_apps"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = AppText.t("history_ungrouped_apps_description"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (rankedApps.isEmpty()) {
                ListEmptyStateCard(
                    title = AppText.t("history_ungrouped_apps_empty_title"),
                    body = AppText.t("history_ungrouped_apps_empty_body"),
                )
                return@TinyVowCard
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DashboardMetric(
                    modifier = Modifier.weight(1f),
                    label = AppText.t("history_app_count"),
                    value = historyCountValue(rankedApps.size),
                )
                DashboardMetric(
                    modifier = Modifier.weight(1f),
                    label = AppText.t("history_total_duration"),
                    value = formatHistoryDuration(rankedApps.sumOf { it.dailyUsageMillis }),
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppSortMode.entries.forEach { mode ->
                    HistoryToggleChip(
                        text = appSortModeLabel(mode),
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
        TinyVowCard(
            shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CardVertical,
                ),
                verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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
                        fontWeight = FontWeight.SemiBold,
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
    val themeColors = LocalThemeColors.current
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
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
        }
    }
}

@Composable
private fun HistoryMetricRow(
    label: String,
    value: String,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.inkMuted,
        )
        Spacer(modifier = Modifier.height(0.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inkStrong,
        )
    }
}

@Composable
private fun CompactMetricBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
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
                color = themeColors.inkMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
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
                fontWeight = FontWeight.SemiBold,
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
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
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
        HistoryGroupFilter.ALL -> AppText.t("history_total_usage")
        HistoryGroupFilter.CONTROL -> AppText.t("history_control_usage")
        HistoryGroupFilter.ENCOURAGE -> AppText.t("history_encourage_usage")
    }
}

private fun summaryLabelForGroupFilter(filter: HistoryGroupFilter): String {
    return when (filter) {
        HistoryGroupFilter.ALL -> AppText.t("history_net_points_change")
        HistoryGroupFilter.CONTROL -> AppText.t("history_control_saved")
        HistoryGroupFilter.ENCOURAGE -> AppText.t("history_points_earned")
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
        HistoryGroupFilter.ALL -> AppText.t(
            "history_card_headline_all",
            archive.controlCompletedGroupCount,
            archive.encourageCompletedGroupCount,
        )
        HistoryGroupFilter.CONTROL -> AppText.t(
            "history_card_headline_control",
            archive.controlCompletedGroupCount,
            archive.controlExceededGroupCount,
        )
        HistoryGroupFilter.ENCOURAGE -> AppText.t(
            "history_card_headline_encourage",
            archive.encourageCompletedGroupCount,
            formatPoints(archive.pointsEarned),
        )
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
                AppText.t("history_card_headline_control", archive.controlCompletedGroupCount, archive.controlExceededGroupCount)
            }
        }
        HistoryGroupFilter.ENCOURAGE -> {
            if (archive.pointsEarned > 0.0) {
                "+${formatPoints(archive.pointsEarned)}"
            } else {
                AppText.t("history_tag_achieved", archive.encourageCompletedGroupCount)
            }
        }
    }
}

private fun historyListSortLabel(sort: HistoryListSort): String =
    AppText.t(
        when (sort) {
            HistoryListSort.DATE -> "history_sort_recent"
            HistoryListSort.USAGE -> "history_sort_usage"
            HistoryListSort.POINTS -> "history_sort_net_points"
            HistoryListSort.REDEMPTIONS -> "history_sort_redemptions"
        },
    )

private fun historyRangeFilterLabel(filter: HistoryRangeFilter): String =
    AppText.t(
        when (filter) {
            HistoryRangeFilter.LAST_7 -> "history_range_last_7_days"
            HistoryRangeFilter.LAST_30 -> "history_range_last_30_days"
            HistoryRangeFilter.ALL -> "history_range_all"
        },
    )

private fun historyGroupFilterLabel(filter: HistoryGroupFilter): String =
    AppText.t(
        when (filter) {
            HistoryGroupFilter.ALL -> "history_filter_all"
            HistoryGroupFilter.CONTROL -> "history_filter_control"
            HistoryGroupFilter.ENCOURAGE -> "history_filter_encourage"
        },
    )

private fun groupSortModeLabel(sort: GroupSortMode): String =
    AppText.t(
        when (sort) {
            GroupSortMode.DEFAULT -> "history_sort_default"
            GroupSortMode.USAGE -> "history_sort_usage"
            GroupSortMode.POINTS -> "history_sort_points"
            GroupSortMode.STATUS -> "history_sort_status"
        },
    )

private fun appSortModeLabel(sort: AppSortMode): String =
    AppText.t(
        when (sort) {
            AppSortMode.USAGE -> "history_sort_usage"
            AppSortMode.OPENS -> "history_sort_opens"
            AppSortMode.SESSIONS -> "history_sort_sessions"
            AppSortMode.NIGHT -> "history_sort_night"
            AppSortMode.POINTS -> "history_sort_points"
        },
    )

private fun historyDaysValue(days: Int): String = AppText.t("history_value_days", days)

private fun historyTimesValue(times: Int): String = AppText.t("history_value_times", times)

private fun historyCountValue(count: Int): String = AppText.t("history_value_count", count)

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
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }
}

private fun groupTypeLabel(groupType: GroupType): String {
    return when (groupType) {
        GroupType.CONTROL -> AppText.t("history_filter_control")
        GroupType.ENCOURAGE -> AppText.t("history_filter_encourage")
    }
}

private fun limitPeriodLabel(limitPeriod: LimitPeriod?): String {
    return when (limitPeriod) {
        LimitPeriod.DAILY -> AppText.t("history_period_daily")
        LimitPeriod.WEEKLY -> AppText.t("history_period_weekly")
        LimitPeriod.MONTHLY -> AppText.t("history_period_monthly")
        null -> AppText.t("history_ungrouped")
    }
}

private fun formatArchiveTimestamp(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--"
    return Instant
        .ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("M/d HH:mm", Locale.CHINA))
}

private fun formatEventTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--"
    return Instant
        .ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA))
}

private fun localizedProtectionEventMessage(event: ProtectionEventEntity): String {
    val args = parseProtectionEventArgs(event.messageArgsJson)
    return if (args.isEmpty()) {
        AppText.t(event.messageKey)
    } else {
        AppText.t(event.messageKey, *args.toTypedArray())
    }
}

private fun parseProtectionEventArgs(json: String?): List<String> {
    if (json.isNullOrBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(json)
        List(array.length()) { index -> array.optString(index) }
    }.getOrDefault(emptyList())
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





