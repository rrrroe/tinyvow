package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.usage.AppSession
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.data.usage.UsageRepository
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.math.absoluteValue

private enum class StatsMode(val label: String) {
    DAY("日"),
    WEEK("周"),
    MONTH("月"),
    YEAR("年"),
}

private data class StatsRange(
    val mode: StatsMode,
    val startMillis: Long,
    val endMillis: Long,
    val displayLabel: String,
    val bucketStarts: List<Long>,
    val bucketLabels: List<String>,
    val footerLabels: List<String>,
    val canNavigateForward: Boolean,
)

private data class SleepSummary(
    val windowStartMillis: Long,
    val windowEndMillis: Long,
    val durationMillis: Long,
    val startMillis: Long,
    val endMillis: Long,
    val preSleepTopPackage: String? = null,
    val preSleepTopMillis: Long = 0L,
    val postWakeTopPackage: String? = null,
    val postWakeTopMillis: Long = 0L,
)

private data class BucketInsight(
    val label: String,
    val totalMillis: Long,
    val topPackage: String? = null,
    val topPackageMillis: Long = 0L,
)

private data class StatsUiState(
    val isLoading: Boolean = true,
    val isPermissionGranted: Boolean = false,
    val mode: StatsMode = StatsMode.DAY,
    val range: StatsRange? = null,
    val managedPackages: List<String> = emptyList(),
    val totalUsageMillis: Long = 0L,
    val usageBuckets: List<Long> = emptyList(),
    val totalOpenCount: Int = 0,
    val topApps: List<Pair<String, Long>> = emptyList(),
    val topOpenedApps: List<Pair<String, Int>> = emptyList(),
    val longestSessions: List<AppSession> = emptyList(),
    val sessions: List<AppSession> = emptyList(),
    val sleepSummary: SleepSummary? = null,
    val bucketInsights: List<BucketInsight> = emptyList(),
    val activeBucketCount: Int = 0,
    val longestActiveStreak: Int = 0,
    val peakBucketShare: Float = 0f,
    val averageBucketUsageMillis: Long = 0L,
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
    val zoneId = remember { ZoneId.systemDefault() }
    var statsMode by remember { mutableStateOf(StatsMode.DAY) }
    var anchorDate by remember { mutableStateOf(LocalDate.now(zoneId)) }
    var statsUiState by remember { mutableStateOf(StatsUiState(mode = statsMode)) }

    LaunchedEffect(usageAccessStatus, groupsWithApps, statsMode, anchorDate) {
        if (usageAccessStatus != UsageAccessStatus.GRANTED) {
            statsUiState = StatsUiState(isLoading = false, isPermissionGranted = false, mode = statsMode)
            return@LaunchedEffect
        }

        val usageRepository = UsageStatsUsageRepository(context)
        while (true) {
            statsUiState = buildStatsUiState(
                mode = statsMode,
                anchorDate = anchorDate,
                groupsWithApps = groupsWithApps,
                usageRepository = usageRepository,
                zoneId = zoneId,
            )
            delay(if (statsMode == StatsMode.DAY) 30_000L else 120_000L)
        }
    }

    StatsScreenLayout(
        state = statsUiState,
        userPoints = userPoints,
        todayPoints = todayPoints,
        onModeChange = { mode ->
            statsMode = mode
            anchorDate = anchorDate.coerceAtMost(LocalDate.now(zoneId))
        },
        onNavigatePrevious = {
            anchorDate = shiftAnchorDate(anchorDate, statsMode, -1)
        },
        onNavigateNext = {
            val candidate = shiftAnchorDate(anchorDate, statsMode, 1)
            if (!candidate.isAfter(LocalDate.now(zoneId))) {
                anchorDate = candidate
            }
        },
        modifier = modifier,
    )
}

private suspend fun buildStatsUiState(
    mode: StatsMode,
    anchorDate: LocalDate,
    groupsWithApps: List<AppGroupWithApps>,
    usageRepository: UsageRepository,
    zoneId: ZoneId,
): StatsUiState {
    val range = buildStatsRange(mode, anchorDate, zoneId)
    val uniquePackages = groupsWithApps.flatMap { it.packageNames }.distinct()

    if (uniquePackages.isEmpty()) {
        return StatsUiState(
            isLoading = false,
            isPermissionGranted = true,
            mode = mode,
            range = range,
        )
    }

    val usageByPackage = usageRepository.getUsageStats(range.startMillis, range.endMillis)
        .filterKeys { uniquePackages.contains(it) }
    val totalUsage = usageByPackage.values.sum()

    val topApps = usageByPackage.toList()
        .sortedByDescending { it.second }
        .filter { it.second > 0L }
        .take(3)

    val bucketInsights = if (mode == StatsMode.DAY) {
        emptyList()
    } else {
        buildBucketInsights(range, uniquePackages, usageRepository)
    }
    val usageBuckets = bucketInsights.map { it.totalMillis }

    val managedSessions = if (mode == StatsMode.DAY) {
        usageRepository.getUsageSessions(range.startMillis, range.endMillis)
            .filter { uniquePackages.contains(it.packageName) }
    } else {
        emptyList()
    }

    val openCounts = if (mode == StatsMode.DAY) {
        usageRepository.getAppOpenCount(range.startMillis, range.endMillis)
            .filterKeys { uniquePackages.contains(it) }
    } else {
        emptyMap()
    }

    val topOpenedApps = openCounts.toList().sortedByDescending { it.second }.take(3)
    val longestSessions = managedSessions.sortedByDescending { it.endTime - it.startTime }.take(3)
    val sleepSummary = if (mode == StatsMode.DAY) {
        buildSleepSummary(anchorDate, uniquePackages, usageRepository, zoneId)
    } else {
        null
    }

    return StatsUiState(
        isLoading = false,
        isPermissionGranted = true,
        mode = mode,
        range = range,
        managedPackages = uniquePackages,
        totalUsageMillis = totalUsage,
        usageBuckets = usageBuckets,
        totalOpenCount = openCounts.values.sum(),
        topApps = topApps,
        topOpenedApps = topOpenedApps,
        longestSessions = longestSessions,
        sessions = managedSessions,
        sleepSummary = sleepSummary,
        bucketInsights = bucketInsights,
        activeBucketCount = usageBuckets.count { it > 0L },
        longestActiveStreak = buildLongestActiveStreak(usageBuckets),
        peakBucketShare = if (totalUsage > 0L) {
            (usageBuckets.maxOrNull() ?: 0L).toFloat() / totalUsage.toFloat()
        } else {
            0f
        },
        averageBucketUsageMillis = if (usageBuckets.isNotEmpty()) totalUsage / usageBuckets.size else 0L,
    )
}

private fun buildStatsRange(
    mode: StatsMode,
    anchorDate: LocalDate,
    zoneId: ZoneId,
): StatsRange {
    val today = LocalDate.now(zoneId)
    val nowMillis = System.currentTimeMillis()

    return when (mode) {
        StatsMode.DAY -> {
            val start = anchorDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val nextDay = anchorDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            StatsRange(
                mode = mode,
                startMillis = start,
                endMillis = if (anchorDate == today) minOf(nowMillis, nextDay) else nextDay,
                displayLabel = if (anchorDate == today) "今天" else anchorDate.format(DateTimeFormatter.ofPattern("M月d日")),
                bucketStarts = (0 until 24).map { hour ->
                    anchorDate.atStartOfDay(zoneId).plusHours(hour.toLong()).toInstant().toEpochMilli()
                },
                bucketLabels = (0 until 24).map { "${it}时" },
                footerLabels = listOf("0", "6", "12", "18", "24"),
                canNavigateForward = anchorDate.isBefore(today),
            )
        }

        StatsMode.WEEK -> {
            val weekStart = anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val nextWeek = weekStart.plusWeeks(1)
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            StatsRange(
                mode = mode,
                startMillis = weekStart.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                endMillis = if (weekStart == currentWeekStart) nowMillis else nextWeek.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                displayLabel = "${weekStart.format(DateTimeFormatter.ofPattern("M.d"))} - ${weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("M.d"))}",
                bucketStarts = (0 until 7).map { offset ->
                    weekStart.plusDays(offset.toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
                },
                bucketLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日"),
                footerLabels = listOf("一", "二", "三", "四", "五", "六", "日"),
                canNavigateForward = weekStart.isBefore(currentWeekStart),
            )
        }

        StatsMode.MONTH -> {
            val monthStart = anchorDate.with(TemporalAdjusters.firstDayOfMonth())
            val nextMonth = monthStart.plusMonths(1)
            val currentMonthStart = today.with(TemporalAdjusters.firstDayOfMonth())
            val daysInMonth = monthStart.lengthOfMonth()
            StatsRange(
                mode = mode,
                startMillis = monthStart.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                endMillis = if (monthStart == currentMonthStart) nowMillis else nextMonth.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                displayLabel = monthStart.format(DateTimeFormatter.ofPattern("yyyy年M月")),
                bucketStarts = (0 until daysInMonth).map { offset ->
                    monthStart.plusDays(offset.toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
                },
                bucketLabels = (1..daysInMonth).map { "${it}日" },
                footerLabels = buildMonthFooterLabels(daysInMonth),
                canNavigateForward = monthStart.isBefore(currentMonthStart),
            )
        }

        StatsMode.YEAR -> {
            val yearStart = anchorDate.with(TemporalAdjusters.firstDayOfYear())
            val nextYear = yearStart.plusYears(1)
            StatsRange(
                mode = mode,
                startMillis = yearStart.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                endMillis = if (yearStart.year == today.year) nowMillis else nextYear.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                displayLabel = yearStart.format(DateTimeFormatter.ofPattern("yyyy年")),
                bucketStarts = (0 until 12).map { offset ->
                    yearStart.plusMonths(offset.toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
                },
                bucketLabels = (1..12).map { "${it}月" },
                footerLabels = listOf("1月", "4月", "7月", "10月", "12月"),
                canNavigateForward = yearStart.year < today.year,
            )
        }
    }
}

private suspend fun buildUsageBuckets(
    range: StatsRange,
    managedPackages: List<String>,
    usageRepository: UsageRepository,
): List<Long> {
    return buildBucketInsights(range, managedPackages, usageRepository).map { it.totalMillis }
}

private suspend fun buildBucketInsights(
    range: StatsRange,
    managedPackages: List<String>,
    usageRepository: UsageRepository,
): List<BucketInsight> {
    val managedPackageSet = managedPackages.toSet()

    return range.bucketStarts.mapIndexed { index, bucketStart ->
        val bucketEnd = if (index == range.bucketStarts.lastIndex) {
            range.endMillis
        } else {
            minOf(range.bucketStarts[index + 1], range.endMillis)
        }
        if (bucketStart >= range.endMillis) {
            BucketInsight(
                label = range.bucketLabels.getOrElse(index) { "${index + 1}" },
                totalMillis = 0L,
            )
        } else {
            val usageByPackage = usageRepository.getUsageStats(bucketStart, bucketEnd)
                .filterKeys { it in managedPackageSet }
            val topEntry = usageByPackage.maxByOrNull { it.value }
            BucketInsight(
                label = range.bucketLabels.getOrElse(index) { "${index + 1}" },
                totalMillis = usageByPackage.values.sum(),
                topPackage = topEntry?.key?.takeIf { (topEntry.value) > 0L },
                topPackageMillis = topEntry?.value ?: 0L,
            )
        }
    }
}

private suspend fun buildSleepSummary(
    anchorDate: LocalDate,
    managedPackages: List<String>,
    usageRepository: UsageRepository,
    zoneId: ZoneId,
): SleepSummary? {
    val sleepWindowStart = anchorDate.minusDays(1).atTime(18, 0).atZone(zoneId).toInstant().toEpochMilli()
    val sleepWindowEnd = anchorDate.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
    val nightStart = anchorDate.minusDays(1).atTime(21, 0).atZone(zoneId).toInstant().toEpochMilli()
    val morningEnd = anchorDate.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()

    val sessions = usageRepository.getUsageSessions(sleepWindowStart, sleepWindowEnd)
        .filter { managedPackages.contains(it.packageName) }
        .sortedBy { it.startTime }

    var previousEnd = sleepWindowStart
    var bestStart = nightStart
    var bestEnd = nightStart
    var bestGap = 0L

    val anchoredSessions = if (sessions.isEmpty()) {
        emptyList()
    } else {
        sessions
    }

    anchoredSessions.forEach { session ->
        val candidateStart = maxOf(previousEnd, nightStart)
        val candidateEnd = minOf(session.startTime, morningEnd)
        val candidateGap = candidateEnd - candidateStart
        if (candidateGap > bestGap) {
            bestGap = candidateGap
            bestStart = candidateStart
            bestEnd = candidateEnd
        }
        previousEnd = maxOf(previousEnd, session.endTime)
    }

    val tailStart = maxOf(previousEnd, nightStart)
    val tailGap = morningEnd - tailStart
    if (tailGap > bestGap) {
        bestGap = tailGap
        bestStart = tailStart
        bestEnd = morningEnd
    }

    if (bestGap < 90 * 60 * 1000L) {
        return null
    }

    val preWindowUsage = collectWindowUsageByPackage(
        sessions = sessions,
        windowStart = bestStart - 60 * 60 * 1000L,
        windowEnd = bestStart,
    )
    val postWindowUsage = collectWindowUsageByPackage(
        sessions = sessions,
        windowStart = bestEnd,
        windowEnd = bestEnd + 60 * 60 * 1000L,
    )

    val preTop = preWindowUsage.maxByOrNull { it.value }
    val postTop = postWindowUsage.maxByOrNull { it.value }

    return SleepSummary(
        windowStartMillis = sleepWindowStart,
        windowEndMillis = sleepWindowEnd,
        durationMillis = bestGap,
        startMillis = bestStart,
        endMillis = bestEnd,
        preSleepTopPackage = preTop?.key,
        preSleepTopMillis = preTop?.value ?: 0L,
        postWakeTopPackage = postTop?.key,
        postWakeTopMillis = postTop?.value ?: 0L,
    )
}

private fun collectWindowUsageByPackage(
    sessions: List<AppSession>,
    windowStart: Long,
    windowEnd: Long,
): Map<String, Long> {
    if (windowEnd <= windowStart) return emptyMap()

    val usageByPackage = mutableMapOf<String, Long>()
    sessions.forEach { session ->
        val overlapStart = maxOf(session.startTime, windowStart)
        val overlapEnd = minOf(session.endTime, windowEnd)
        val overlap = overlapEnd - overlapStart
        if (overlap > 0L) {
            usageByPackage[session.packageName] = usageByPackage.getOrDefault(session.packageName, 0L) + overlap
        }
    }
    return usageByPackage
}

private fun buildMonthFooterLabels(daysInMonth: Int): List<String> {
    val markers = listOf(1, daysInMonth / 4, daysInMonth / 2, (daysInMonth * 3) / 4, daysInMonth)
    return markers.distinct().map { it.coerceAtLeast(1).toString() }
}

private fun buildLongestActiveStreak(values: List<Long>): Int {
    var best = 0
    var current = 0
    values.forEach { value ->
        if (value > 0L) {
            current += 1
            best = maxOf(best, current)
        } else {
            current = 0
        }
    }
    return best
}

private fun shiftAnchorDate(anchorDate: LocalDate, mode: StatsMode, offset: Long): LocalDate {
    return when (mode) {
        StatsMode.DAY -> anchorDate.plusDays(offset)
        StatsMode.WEEK -> anchorDate.plusWeeks(offset)
        StatsMode.MONTH -> anchorDate.plusMonths(offset)
        StatsMode.YEAR -> anchorDate.plusYears(offset)
    }
}

@Composable
private fun StatsScreenLayout(
    state: StatsUiState,
    userPoints: Double,
    todayPoints: Double,
    onModeChange: (StatsMode) -> Unit,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isPermissionGranted || state.range == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("核心权限未开启", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val context = LocalContext.current
    val appColors = remember(state.managedPackages) {
        val colors = listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFFE91E63), Color(0xFF9C27B0))
        state.managedPackages.mapIndexed { index, pkg -> pkg to colors[index % colors.size] }.toMap()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("洞察", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text(
                    "按日、周、月、年切换看使用趋势。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OverviewPill(label = "总积分", value = formatPoints(userPoints), modifier = Modifier.weight(1f))
                OverviewPill(label = "今日积分", value = "+${formatPoints(todayPoints)}", modifier = Modifier.weight(1f))
                OverviewPill(label = "管理 App", value = state.managedPackages.size.toString(), modifier = Modifier.weight(1f))
            }
        }

        // App Icons Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(state.managedPackages) { pkg ->
                    val icon = remember(pkg) { try { context.packageManager.getApplicationIcon(pkg) } catch (e: Exception) { null } }
                    val color = appColors[pkg] ?: Color.Gray
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp
                        ) {
                            if (icon != null) AsyncImage(model = icon, contentDescription = null, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.width(20.dp).height(3.dp).background(color, RoundedCornerShape(1.5.dp)))
                    }
                }
            }
        }

        // Date Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                @OptIn(ExperimentalMaterial3Api::class)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    StatsMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.mode == mode,
                            onClick = { onModeChange(mode) },
                            modifier = Modifier.semantics {
                                contentDescription = "stats-mode-${mode.name.lowercase()}"
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, StatsMode.entries.size),
                        ) {
                            Text(mode.label)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigatePrevious,
                        modifier = Modifier.semantics { contentDescription = "stats-previous-range" }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                    }
                    Text(
                        state.range.displayLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onNavigateNext,
                        enabled = state.range.canNavigateForward,
                        modifier = Modifier.semantics { contentDescription = "stats-next-range" }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        // Card 1: Usage Stats
        item {
            InsightCard(title = "使用统计", extraText = state.range.displayLabel) {
                if (state.totalUsageMillis == 0L) {
                    Text(
                        "当前时间段没有使用记录。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val h = (state.totalUsageMillis / 3600_000).toInt()
                    val m = ((state.totalUsageMillis % 3600_000) / 60_000).toInt()
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(h.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                        Text("h", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
                        Text(m.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
                        Text("m", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    if (state.mode == StatsMode.DAY) {
                        TimelineChart(
                            sessions = state.sessions,
                            startOfDay = state.range.startMillis,
                            endMillis = state.range.endMillis,
                            appColors = appColors
                        )
                    } else {
                        AggregateBarChart(
                            values = state.usageBuckets,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    FooterLabels(state.range.footerLabels)

                    if (state.mode != StatsMode.DAY) {
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OverviewPill(label = "活跃区段", value = state.activeBucketCount.toString(), modifier = Modifier.weight(1f))
                            OverviewPill(label = "连续活跃", value = formatBucketCount(state.longestActiveStreak, state.mode), modifier = Modifier.weight(1f))
                            OverviewPill(label = "平均时长", value = formatDurationCompact(state.averageBucketUsageMillis), modifier = Modifier.weight(1f))
                        }
                    }

                    if (state.topApps.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        state.topApps.forEach { (pkg, duration) ->
                            AppProgressBarRow(
                                pkg = pkg,
                                duration = duration,
                                maxDuration = maxOf(1L, state.topApps.firstOrNull()?.second ?: 1L),
                                color = appColors[pkg] ?: Color.Gray
                            )
                        }
                    }
                }
            }
        }

        if (state.mode != StatsMode.DAY && state.usageBuckets.isNotEmpty()) {
            item {
                AggregateHighlightsCard(
                    mode = state.mode,
                    range = state.range,
                    usageBuckets = state.usageBuckets,
                )
            }
            item {
                AggregateHeatmapCard(
                    mode = state.mode,
                    bucketInsights = state.bucketInsights,
                    longestActiveStreak = state.longestActiveStreak,
                    peakBucketShare = state.peakBucketShare,
                    appColors = appColors,
                )
            }
            item {
                AggregateBucketRankingCard(
                    mode = state.mode,
                    bucketInsights = state.bucketInsights,
                )
            }
        }

        // Card 2: Intensity Distribution
        if (state.mode == StatsMode.DAY) {
            item {
                InsightCard(title = "使用强度分布") {
                    IntensityViolinChart(
                        sessions = state.sessions,
                        startOfDay = state.range.startMillis,
                        endMillis = state.range.endMillis
                    )
                }
            }
        }

        // Card 3: Sleep
        if (state.mode == StatsMode.DAY) {
            item {
                InsightCard(title = "睡眠") {
                    val sleepSummary = state.sleepSummary
                    if (sleepSummary == null) {
                        Text(
                            "昨晚到今天上午之间没有足够长的连续空档，暂时无法推断稳定睡眠区间。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        val h = (sleepSummary.durationMillis / 3600_000).toInt()
                        val m = ((sleepSummary.durationMillis % 3600_000) / 60_000).toInt()
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(h.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                            Text("h", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                            Text(m.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                            Text("m", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                        }
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        val startStr = java.time.Instant.ofEpochMilli(sleepSummary.startMillis).atZone(ZoneId.systemDefault()).format(formatter)
                        val endStr = java.time.Instant.ofEpochMilli(sleepSummary.endMillis).atZone(ZoneId.systemDefault()).format(formatter)
                        Text("$startStr - $endStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(Modifier.height(16.dp))
                        SleepTimelineChart(sleepSummary)

                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("睡前 1 小时", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    if (sleepSummary.preSleepTopPackage == null) "无明显使用"
                                    else "${rememberAppLabel(sleepSummary.preSleepTopPackage)} · ${formatDurationCompact(sleepSummary.preSleepTopMillis)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("醒后 1 小时", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    if (sleepSummary.postWakeTopPackage == null) "无明显使用"
                                    else "${rememberAppLabel(sleepSummary.postWakeTopPackage)} · ${formatDurationCompact(sleepSummary.postWakeTopMillis)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 4: Single Addiction
        if (state.mode == StatsMode.DAY && state.longestSessions.isNotEmpty()) {
            item {
                InsightCard(title = "单次沉迷数据") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        state.longestSessions.forEach { session ->
                            val durationMins = (session.endTime - session.startTime) / 60000
                            val color = appColors[session.packageName] ?: Color.Gray
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AppIconCircle(session.packageName)
                                Spacer(Modifier.height(4.dp))
                                Text("${durationMins}m", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                                val s = java.time.Instant.ofEpochMilli(session.startTime).atZone(ZoneId.systemDefault()).format(formatter)
                                val e = java.time.Instant.ofEpochMilli(session.endTime).atZone(ZoneId.systemDefault()).format(formatter)
                                Text("$s-$e", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(8.dp))
                                Box(modifier = Modifier.width(4.dp).height(12.dp).background(color, RoundedCornerShape(2.dp)))
                            }
                        }
                    }
                }
            }
        }

        // Card 5: App Open Count
        if (state.mode == StatsMode.DAY) {
            item {
                InsightCard(title = "App 打开次数") {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(state.totalOpenCount.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                        Text("次", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    OpenCountScatterChart(
                        sessions = state.sessions,
                        startOfDay = state.range.startMillis,
                        endMillis = state.range.endMillis,
                        appColors = appColors
                    )

                    Spacer(Modifier.height(16.dp))

                    FooterLabels(state.range.footerLabels)

                    Spacer(Modifier.height(16.dp))

                    state.topOpenedApps.forEach { (pkg, counts) ->
                        AppProgressBarRow(
                            pkg = pkg,
                            duration = counts.toLong(),
                            maxDuration = maxOf(1L, state.topOpenedApps.firstOrNull()?.second?.toLong() ?: 1L),
                            color = appColors[pkg] ?: Color.Gray,
                            unit = "次"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AggregateHighlightsCard(
    mode: StatsMode,
    range: StatsRange,
    usageBuckets: List<Long>,
) {
    val peakIndex = usageBuckets.indices.maxByOrNull { usageBuckets[it] } ?: return
    val lightIndex = usageBuckets.indices.minByOrNull { usageBuckets[it] } ?: return
    val peakLabel = range.bucketLabels.getOrElse(peakIndex) { range.displayLabel }
    val lightLabel = range.bucketLabels.getOrElse(lightIndex) { range.displayLabel }
    val peakValue = usageBuckets[peakIndex]
    val lightValue = usageBuckets[lightIndex]
    val averageValue = if (usageBuckets.isEmpty()) 0L else usageBuckets.sum() / usageBuckets.size

    val averageLabel = when (mode) {
        StatsMode.WEEK, StatsMode.MONTH -> "日均"
        StatsMode.YEAR -> "月均"
        StatsMode.DAY -> "均值"
    }

    InsightCard(title = "聚合摘要") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverviewPill(label = "最高", value = "${peakLabel}\n${formatDurationCompact(peakValue)}", modifier = Modifier.weight(1f))
            OverviewPill(label = "最低", value = "${lightLabel}\n${formatDurationCompact(lightValue)}", modifier = Modifier.weight(1f))
            OverviewPill(label = averageLabel, value = formatDurationCompact(averageValue), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AggregateBucketRankingCard(
    mode: StatsMode,
    bucketInsights: List<BucketInsight>,
) {
    val rankedBuckets = bucketInsights.sortedByDescending { it.totalMillis }.take(5)

    val title = when (mode) {
        StatsMode.WEEK -> "本周高使用日"
        StatsMode.MONTH -> "本月高使用日"
        StatsMode.YEAR -> "本年高使用月"
        StatsMode.DAY -> "高使用区段"
    }

    InsightCard(title = title) {
        if (rankedBuckets.all { it.totalMillis == 0L }) {
            Text(
                "当前时间段没有可排序的使用记录。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            rankedBuckets.filter { it.totalMillis > 0L }.forEachIndexed { index, bucket ->
                BucketRankingRow(
                    rank = index + 1,
                    label = bucket.label,
                    usageMillis = bucket.totalMillis,
                    maxUsageMillis = rankedBuckets.firstOrNull()?.totalMillis ?: bucket.totalMillis,
                    topPackage = bucket.topPackage,
                    topPackageMillis = bucket.topPackageMillis,
                )
            }
        }
    }
}

@Composable
private fun AggregateHeatmapCard(
    mode: StatsMode,
    bucketInsights: List<BucketInsight>,
    longestActiveStreak: Int,
    peakBucketShare: Float,
    appColors: Map<String, Color>,
) {
    val title = when (mode) {
        StatsMode.WEEK -> "一周热力分布"
        StatsMode.MONTH -> "当月热力分布"
        StatsMode.YEAR -> "年度热力分布"
        StatsMode.DAY -> "热力分布"
    }
    val columnCount = when (mode) {
        StatsMode.WEEK -> 7
        StatsMode.MONTH -> 7
        StatsMode.YEAR -> 4
        StatsMode.DAY -> 6
    }
    val maxUsage = bucketInsights.maxOfOrNull { it.totalMillis } ?: 0L
    val heatRows = bucketInsights.chunked(columnCount)

    InsightCard(title = title) {
        Text(
            "颜色越深代表使用时长越高，底部圆点表示该区间主导 App。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverviewPill(
                label = "连续活跃",
                value = formatBucketCount(longestActiveStreak, mode),
                modifier = Modifier.weight(1f),
            )
            OverviewPill(
                label = "峰值占比",
                value = formatPercent(peakBucketShare),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        heatRows.forEachIndexed { index, rowBuckets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowBuckets.forEach { bucket ->
                    AggregateHeatCell(
                        bucket = bucket,
                        maxUsage = maxUsage,
                        appColor = bucket.topPackage?.let { appColors[it] } ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columnCount - rowBuckets.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (index != heatRows.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AggregateHeatCell(
    bucket: BucketInsight,
    maxUsage: Long,
    appColor: Color,
    modifier: Modifier = Modifier,
) {
    val fillRatio = if (maxUsage <= 0L) 0f else bucket.totalMillis.toFloat() / maxUsage.toFloat()
    val background = if (bucket.totalMillis == 0L) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f + 0.62f * fillRatio.coerceIn(0f, 1f))
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 86.dp)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = bucket.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (bucket.totalMillis > 0L) formatDurationCompact(bucket.totalMillis) else "--",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (bucket.topPackage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(appColor),
                    )
                    Text(
                        text = rememberAppLabel(bucket.topPackage),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Text(
                    text = "无明显使用",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun BucketRankingRow(
    rank: Int,
    label: String,
    usageMillis: Long,
    maxUsageMillis: Long,
    topPackage: String? = null,
    topPackageMillis: Long = 0L,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(28.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (usageMillis.toFloat() / maxOf(1L, maxUsageMillis).toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            )
            if (topPackage != null && topPackageMillis > 0L) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "主导 App: ${rememberAppLabel(topPackage)} · ${formatDurationCompact(topPackageMillis)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = formatDurationCompact(usageMillis),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun AppIconCircle(pkg: String) {
    val context = LocalContext.current
    val icon = remember(pkg) { try { context.packageManager.getApplicationIcon(pkg) } catch (e: Exception) { null } }
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (icon != null) AsyncImage(model = icon, contentDescription = null, modifier = Modifier.padding(6.dp))
    }
}

@Composable
private fun rememberAppLabel(packageName: String?): String {
    if (packageName == null) return ""
    val context = LocalContext.current
    return remember(packageName) {
        try {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (_: Exception) {
            packageName
        }
    }
}

private fun formatPoints(points: Double): String {
    return if (points % 1.0 == 0.0) {
        points.toInt().toString()
    } else {
        String.format("%.1f", points)
    }
}

private fun formatDurationCompact(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0L -> "${hours}h${minutes}m"
        minutes > 0L -> "${minutes}m"
        else -> "${durationMillis / 1000L}s"
    }
}

private fun formatBucketCount(count: Int, mode: StatsMode): String {
    val unit = when (mode) {
        StatsMode.WEEK, StatsMode.MONTH -> "天"
        StatsMode.YEAR -> "月"
        StatsMode.DAY -> "段"
    }
    return "${count}${unit}"
}

private fun formatPercent(value: Float): String = "${(value * 100).toInt()}%"

@Composable
fun AppProgressBarRow(pkg: String, duration: Long, maxDuration: Long, color: Color, unit: String = "") {
    val context = LocalContext.current
    val name = remember(pkg) { try { context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(pkg, 0)).toString() } catch (e: Exception) { pkg } }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconCircle(pkg)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (duration.toFloat() / maxDuration.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
        Spacer(Modifier.width(16.dp))
        val valueText = if (unit.isEmpty()) {
            val h = duration / 3600_000
            val m = (duration % 3600_000) / 60_000
            if (h > 0) "${h}h ${m}m" else "${m}m"
        } else {
            "$duration $unit"
        }
        Text(valueText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InsightCard(title: String, extraText: String = "", content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (extraText.isNotEmpty()) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(extraText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun FooterLabels(labels: List<String>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEach { label ->
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
private fun AggregateBarChart(values: List<Long>, color: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        if (values.isEmpty()) return@Canvas
        val maxValue = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
        val slotWidth = size.width / values.size
        val barWidth = slotWidth * 0.56f
        values.forEachIndexed { index, value ->
            val ratio = value.toFloat() / maxValue.toFloat()
            val barHeight = maxOf(8f, size.height * ratio.coerceIn(0f, 1f))
            val x = slotWidth * index + (slotWidth - barWidth) / 2
            drawRoundRect(
                color = if (value == 0L) color.copy(alpha = 0.12f) else color.copy(alpha = 0.88f),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

@Composable
fun TimelineChart(sessions: List<AppSession>, startOfDay: Long, endMillis: Long, appColors: Map<String, Color>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        val w = size.width
        val h = size.height
        val totalRange = maxOf(1L, endMillis - startOfDay)
        // Draw axes lines (0, 6, 12, 18, 0 hours)
        for (i in 0..4) {
            val x = w * (i / 4f)
            drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(x, 0f), Offset(x, h), strokeWidth = 2f)
        }

        // Draw sessions
        sessions.forEach { session ->
            val startRatio = (session.startTime - startOfDay).toFloat() / totalRange.toFloat()
            val endRatio = (session.endTime - startOfDay).toFloat() / totalRange.toFloat()
            if (startRatio in 0f..1f && endRatio in 0f..1f) {
                val startX = w * startRatio
                val endX = w * endRatio
                val color = appColors[session.packageName] ?: Color.Gray
                // randomly stagger y based on hash
                val yOffset = (session.packageName.hashCode().absoluteValue % 5) * (h / 6f) + 10f
                drawRoundRect(
                    color = color.copy(alpha = 0.8f),
                    topLeft = Offset(startX, yOffset),
                    size = Size(maxOf(4f, endX - startX), 12f),
                    cornerRadius = CornerRadius(6f)
                )
            }
        }
    }
}

@Composable
fun IntensityViolinChart(sessions: List<AppSession>, startOfDay: Long, endMillis: Long) {
    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        val w = size.width
        val h = size.height
        val chunks = 48
        val chunkMillis = maxOf(1L, (endMillis - startOfDay) / chunks)
        val intensity = IntArray(chunks)

        sessions.forEach { s ->
            for (i in 0 until chunks) {
                val cStart = startOfDay + i * chunkMillis
                val cEnd = if (i == chunks - 1) endMillis else cStart + chunkMillis
                val overlap = maxOf(0L, minOf(s.endTime, cEnd) - maxOf(s.startTime, cStart))
                intensity[i] += overlap.toInt()
            }
        }

        val maxI = intensity.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f

        val barWidth = w / chunks * 0.6f
        for (i in 0 until chunks) {
            val ratio = intensity[i] / maxI
            val barH = maxOf(4f, h * ratio)
            val x = w * (i.toFloat() / chunks) + (w / chunks - barWidth) / 2
            val y = (h - barH) / 2
            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.5f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2)
            )
        }
    }
}

@Composable
private fun SleepTimelineChart(summary: SleepSummary) {
    Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
        val w = size.width
        val h = size.height
        val totalRange = maxOf(1L, summary.windowEndMillis - summary.windowStartMillis)
        drawLine(
            Color.LightGray.copy(alpha = 0.3f),
            Offset(0f, h / 2),
            Offset(w, h / 2),
            strokeWidth = 8f
        )

        val sleepStartRatio = ((summary.startMillis - summary.windowStartMillis).toFloat() / totalRange.toFloat()).coerceIn(0f, 1f)
        val sleepEndRatio = ((summary.endMillis - summary.windowStartMillis).toFloat() / totalRange.toFloat()).coerceIn(0f, 1f)
        val sleepStartX = w * sleepStartRatio
        val sleepEndX = w * sleepEndRatio

        drawRoundRect(
            color = Color(0xFF5C7892),
            topLeft = Offset(sleepStartX, h / 2 - 12f),
            size = Size(maxOf(12f, sleepEndX - sleepStartX), 24f),
            cornerRadius = CornerRadius(12f)
        )
        drawCircle(color = Color(0xFF2196F3), radius = 10f, center = Offset(sleepStartX, h / 2))
        drawCircle(color = Color(0xFF2196F3), radius = 10f, center = Offset(sleepEndX, h / 2))
    }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("18", "0", "6", "12").forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
    }
}

@Composable
fun OpenCountScatterChart(sessions: List<AppSession>, startOfDay: Long, endMillis: Long, appColors: Map<String, Color>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        val w = size.width
        val h = size.height
        val totalRange = maxOf(1L, endMillis - startOfDay)

        sessions.forEach { s ->
            val startRatio = (s.startTime - startOfDay).toFloat() / totalRange.toFloat()
            if (startRatio in 0f..1f) {
                val x = w * startRatio
                val yOffset = (s.packageName.hashCode().absoluteValue % 8) * (h / 9f) + 10f
                val color = appColors[s.packageName] ?: Color.Gray
                drawCircle(color = color.copy(alpha = 0.8f), radius = 6f, center = Offset(x, yOffset))
            }
        }
    }
}
