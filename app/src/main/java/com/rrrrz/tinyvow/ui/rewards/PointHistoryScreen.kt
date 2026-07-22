package com.rrrrz.tinyvow.ui.rewards

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.data.db.PointLedgerEntity
import com.rrrrz.tinyvow.data.db.PointLedgerEntryType
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.home.AppIconStack
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowEmptyState
import com.rrrrz.tinyvow.ui.theme.TinyVowPageBackground
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong
import org.json.JSONArray

@Composable
fun PointHistoryScreen(
    userPoints: Double,
    entries: List<PointLedgerEntity>,
    groups: List<AppGroupWithApps>,
    onBack: () -> Unit,
) {
    val days = remember(entries) {
        entries
            .groupBy { it.ledgerDate }
            .map { (ledgerDate, dayEntries) -> PointHistoryDay(ledgerDate, dayEntries) }
    }
    var expandedGroupKey by remember { mutableStateOf<String?>(null) }
    BackHandler(onBack = onBack)
    TinyVowPageBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = TinyVowSpacing.PageHorizontal,
                vertical = TinyVowSpacing.CardGap,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            item { PointHistoryBalanceCard(userPoints) }
            if (days.isEmpty()) {
                item {
                    TinyVowEmptyState(
                        title = AppText.t("point_history_empty_title"),
                        body = AppText.t("point_history_empty_body"),
                    )
                }
            } else {
                items(days, key = { it.ledgerDate }) { day ->
                    PointHistoryDayCard(
                        day = day,
                        groups = groups,
                        expandedGroupKey = expandedGroupKey,
                        onExpand = { key ->
                            expandedGroupKey = key.takeUnless { it == expandedGroupKey }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PointHistoryBalanceCard(userPoints: Double) {
    val themeColors = LocalThemeColors.current
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = AppText.t("redeem_current_points"),
                style = MaterialTheme.typography.labelMedium,
                color = themeColors.inkMuted,
            )
            Text(
                text = AppText.t("redeem_points_balance_value", userPoints),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.encourage,
            )
        }
    }
}

@Composable
private fun PointHistoryDayCard(
    day: PointHistoryDay,
    groups: List<AppGroupWithApps>,
    expandedGroupKey: String?,
    onExpand: (String) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val earnedPoints = day.entries.filter { it.deltaPoints > 0.0 }.sumOf { it.deltaPoints }
    val spentPoints = day.entries.filter { it.deltaPoints < 0.0 }.sumOf { -it.deltaPoints }
    val groupSummaries = remember(day, groups) {
        buildPointHistoryGroups(day.entries.filter { it.entryType != PointLedgerEntryType.REWARD_SPEND }, groups)
    }
    val spendEntries = remember(day) { day.entries.filter { it.entryType == PointLedgerEntryType.REWARD_SPEND } }
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CompactCardHorizontal,
                vertical = TinyVowSpacing.CompactCardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
            ) {
                Text(
                    text = formatPointHistoryDate(day.ledgerDate),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = AppText.t("point_history_day_summary", earnedPoints, spentPoints),
                    style = MaterialTheme.typography.labelSmall,
                    color = themeColors.inkMuted,
                    maxLines = 1,
                )
            }
            if (groupSummaries.isNotEmpty()) {
                PointHistorySectionTitle(AppText.t("point_history_group_section"))
                groupSummaries.forEach { summaryGroup ->
                    val groupKey = "${day.ledgerDate}:${summaryGroup.key}"
                    PointHistoryGroupRow(
                        summaryGroup = summaryGroup,
                        expanded = expandedGroupKey == groupKey,
                        onExpand = { onExpand(groupKey) },
                    )
                }
            }
            if (spendEntries.isNotEmpty()) {
                HorizontalDivider(color = themeColors.dividerSoft)
                val spendKey = "${day.ledgerDate}:spend"
                PointHistorySpendSummaryRow(
                    entries = spendEntries,
                    expanded = expandedGroupKey == spendKey,
                    onExpand = { onExpand(spendKey) },
                )
            }
        }
    }
}

@Composable
private fun PointHistorySectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = LocalThemeColors.current.inkMuted,
        modifier = Modifier.padding(top = 2.dp),
    )
}

@Composable
private fun PointHistoryGroupRow(
    summaryGroup: PointHistoryGroup,
    expanded: Boolean,
    onExpand: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val netPoints = summaryGroup.entries.sumOf { it.deltaPoints }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            Text(
                text = summaryGroup.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = themeColors.inkStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (netPoints >= 0.0) {
                    AppText.t("point_history_points_earned", netPoints)
                } else {
                    AppText.t("point_history_points_spent", -netPoints)
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (netPoints >= 0.0) themeColors.encourage else themeColors.restraint,
                maxLines = 1,
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = themeColors.inkMuted,
            )
        }
        if (expanded) {
            HorizontalDivider(color = themeColors.dividerSoft)
            summaryGroup.entries.forEach { entry ->
                PointHistoryEntryRow(
                    entry = entry,
                    appPackages = summaryGroup.packageNames,
                    pointsPerMinute = summaryGroup.pointsPerMinute,
                )
            }
        }
    }
}

@Composable
private fun PointHistorySpendSummaryRow(
    entries: List<PointLedgerEntity>,
    expanded: Boolean,
    onExpand: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val spentPoints = entries.sumOf { -it.deltaPoints }.coerceAtLeast(0.0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            Text(
                text = AppText.t("point_history_spend_section"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = themeColors.inkStrong,
            )
            Text(
                text = AppText.t("point_history_points_spent", spentPoints),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.restraint,
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = themeColors.inkMuted,
            )
        }
        if (expanded) {
            HorizontalDivider(color = themeColors.dividerSoft)
            entries.forEach { entry ->
                PointHistoryEntryRow(
                    entry = entry,
                    appPackages = emptyList(),
                    pointsPerMinute = 0.0,
                )
            }
        }
    }
}

@Composable
private fun PointHistoryEntryRow(
    entry: PointLedgerEntity,
    appPackages: List<String>,
    pointsPerMinute: Double,
) {
    val themeColors = LocalThemeColors.current
    val timeFormatter = remember { SimpleDateFormat("M/d HH:mm", Locale.getDefault()) }
    val isEarned = entry.deltaPoints >= 0.0
    val resolvedAppPackages = entry.sourcePackageName?.let(::listOf) ?: appPackages
    val durationMillis =
        entry.usageDurationMillis
            ?: entry.deltaPoints
                .takeIf { entry.entryType == PointLedgerEntryType.USAGE_EARN && pointsPerMinute > 0.0 }
                ?.let { points -> (abs(points) / pointsPerMinute * 60_000.0).roundToLong() }
    val detailText =
        if (entry.entryType == PointLedgerEntryType.USAGE_EARN) {
            durationMillis?.let(::formatPointHistoryMinutes) ?: AppText.t("point_history_minutes_unknown")
        } else {
            localizedPointHistoryDetailTitle(entry)
        }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = timeFormatter.format(Date(entry.occurredAt)),
            modifier = Modifier.width(74.dp),
            style = MaterialTheme.typography.labelSmall,
            color = themeColors.inkMuted,
            maxLines = 1,
        )
        Box(
            modifier = Modifier.width(42.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (resolvedAppPackages.isNotEmpty()) {
                AppIconStack(
                    packages = resolvedAppPackages,
                    size = 18.dp,
                )
            }
        }
        Text(
            text = detailText,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.inkStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (isEarned) {
                AppText.t("point_history_points_earned", entry.deltaPoints)
            } else {
                AppText.t("point_history_points_spent", -entry.deltaPoints)
            },
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isEarned) themeColors.encourage else themeColors.restraint,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

private fun localizedPointHistoryTitle(entry: PointLedgerEntity): String {
    entry.messageKey?.takeIf { it.isNotBlank() }?.let { key ->
        val args = parsePointHistoryArgs(entry.messageArgsJson)
        return if (args.isEmpty()) AppText.t(key) else AppText.t(key, *args.toTypedArray())
    }

    return when (entry.entryType) {
        PointLedgerEntryType.USAGE_EARN ->
            AppText.t("point_history_usage_earn", entry.groupNameSnapshot.orEmpty())
        PointLedgerEntryType.TARGET_BONUS_EARN ->
            AppText.t("point_history_target_bonus", entry.groupNameSnapshot.orEmpty())
        PointLedgerEntryType.OFFLINE_FOCUS -> AppText.t("point_history_offline_focus")
        PointLedgerEntryType.REWARD_SPEND ->
            AppText.t("point_history_reward_spend", entry.rewardTitleSnapshot ?: entry.note)
        PointLedgerEntryType.MANUAL_ADJUSTMENT ->
            entry.note.ifBlank { AppText.t("point_history_manual_adjustment") }
    }
}

private fun localizedPointHistoryDetailTitle(entry: PointLedgerEntity): String =
    when {
        entry.groupId != null && entry.messageKey == null ->
            when (entry.entryType) {
                PointLedgerEntryType.USAGE_EARN -> AppText.t("point_history_usage_earn_compact")
                PointLedgerEntryType.TARGET_BONUS_EARN -> AppText.t("point_history_target_bonus_compact")
                else -> localizedPointHistoryTitle(entry)
            }
        else -> localizedPointHistoryTitle(entry)
    }

private fun parsePointHistoryArgs(json: String?): List<String> {
    if (json.isNullOrBlank() || !json.trimStart().startsWith("[")) return emptyList()
    return runCatching {
        val array = JSONArray(json)
        List(array.length()) { index -> array.opt(index).toString() }
    }.getOrDefault(emptyList())
}

private fun buildPointHistoryGroups(
    entries: List<PointLedgerEntity>,
    groups: List<AppGroupWithApps>,
): List<PointHistoryGroup> {
    val currentGroupsById = groups.associateBy { it.group.id }
    return entries
        .groupBy { entry -> entry.groupId?.let { "group:$it" } ?: "source:${entry.entryType}" }
        .map { (key, groupEntries) ->
            val groupId = groupEntries.first().groupId
            val currentGroup = groupId?.let(currentGroupsById::get)
            PointHistoryGroup(
                key = key,
                title =
                    currentGroup?.group?.name
                        ?: groupEntries.firstNotNullOfOrNull { it.groupNameSnapshot?.takeIf(String::isNotBlank) }
                        ?: pointHistorySourceTitle(groupEntries.first()),
                packageNames = currentGroup?.packageNames.orEmpty(),
                pointsPerMinute = currentGroup?.group?.pointsPerMinute ?: 0.0,
                entries = groupEntries,
            )
        }
}

private fun pointHistorySourceTitle(entry: PointLedgerEntity): String =
    when (entry.entryType) {
        PointLedgerEntryType.USAGE_EARN -> AppText.t("point_history_other_earnings")
        PointLedgerEntryType.TARGET_BONUS_EARN -> AppText.t("point_history_target_bonus_compact")
        PointLedgerEntryType.OFFLINE_FOCUS -> AppText.t("point_history_offline_focus")
        PointLedgerEntryType.REWARD_SPEND -> AppText.t("point_history_reward_spends")
        PointLedgerEntryType.MANUAL_ADJUSTMENT -> AppText.t("point_history_manual_adjustment")
    }

private fun formatPointHistoryDate(ledgerDate: String): String =
    runCatching {
        LocalDate
            .parse(ledgerDate)
            .format(DateTimeFormatter.ofPattern("M/d EEE", Locale.getDefault()))
    }.getOrDefault(ledgerDate)

private fun formatPointHistoryMinutes(durationMillis: Long): String {
    val minutes = durationMillis.coerceAtLeast(0L) / 60_000.0
    val rounded = minutes.roundToLong()
    val value =
        if (abs(minutes - rounded.toDouble()) < 0.05) {
            rounded.toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", minutes)
        }
    return AppText.t("point_history_minutes_value", value)
}

private data class PointHistoryGroup(
    val key: String,
    val title: String,
    val packageNames: List<String>,
    val pointsPerMinute: Double,
    val entries: List<PointLedgerEntity>,
)

private data class PointHistoryDay(
    val ledgerDate: String,
    val entries: List<PointLedgerEntity>,
)
