package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.repository.OfflineFocusRepository
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.time.BusinessDay
import com.rrrrz.tinyvow.data.usage.AppSession
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.data.usage.UsageRepository
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.LocalReportColors
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCardContent
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowEmptyState
import com.rrrrz.tinyvow.ui.theme.TinyVowPageBackground
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSectionHeader
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import com.rrrrz.tinyvow.ui.theme.TinyVowStatusPill
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private const val STAT_CHART_ANIMATIONS_ENABLED = false
private const val SharePosterMinExportWidthPx = 2880f
private const val SharePosterMaxExportScale = 4f
private val ReportTopControlHeight = 36.dp
private val ReportNavigatorArrowSize = 24.dp
private val ReportTopControlShape = RoundedCornerShape(10.dp)

internal enum class SharePosterModule {
    BEHAVIOR,
    TIME_TIDE,
    FOCUS,
    OFFLINE,
    APPS,
    RHYTHM,
    POINTS,
    INSIGHTS,
    OVERVIEW,
    TREND,
    HEATMAP,
    STRUCTURE,
}

private data class ReportCacheKey(
    val tab: ReportTab,
    val value: String,
)

class StatsReportMemoryCache {
    private val reportStates = linkedMapOf<ReportCacheKey, DailyReportUiState>()
    private var recentArchives: List<DailyArchiveEntity>? = null
    private var lastState: DailyReportUiState? = null

    internal fun restoreState(): DailyReportUiState? = lastState

    internal fun getRecentArchives(): List<DailyArchiveEntity>? = recentArchives

    internal fun putRecentArchives(archives: List<DailyArchiveEntity>) {
        recentArchives = archives
    }

    internal fun getReport(
        selectedTab: ReportTab,
        selectedArchiveDate: String?,
        selectedWeekStart: LocalDate?,
        selectedMonth: YearMonth?,
        selectedYear: Int?,
    ): DailyReportUiState? =
        reportCacheKey(
            selectedTab = selectedTab,
            selectedArchiveDate = selectedArchiveDate,
            selectedWeekStart = selectedWeekStart,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
        )?.let(reportStates::get)

    internal fun rememberReport(state: DailyReportUiState) {
        if (!state.isPermissionGranted || state.isRefreshing) return
        val today = BusinessDay.today(ZoneId.systemDefault(), BusinessDay.cachedStartHour())
        val includesToday =
            when (state.selectedTab) {
                ReportTab.DAY -> state.selectedArchiveDate == today.toString()
                ReportTab.WEEK -> state.selectedWeekStart?.let { today in it..it.plusDays(6) } == true
                ReportTab.MONTH -> state.selectedMonth == YearMonth.from(today)
                ReportTab.YEAR -> state.selectedYear == today.year
            }
        if (includesToday) return
        val cacheKey =
            reportCacheKey(
                selectedTab = state.selectedTab,
                selectedArchiveDate = state.selectedArchiveDate,
                selectedWeekStart = state.selectedWeekStart,
                selectedMonth = state.selectedMonth,
                selectedYear = state.selectedYear,
            ) ?: return
        val cachedState = state.copy(animateValues = false)
        reportStates[cacheKey] = cachedState
        lastState = cachedState
    }

    internal fun rememberLastState(state: DailyReportUiState) {
        if (state.isPermissionGranted && !state.isRefreshing) {
            lastState = state.copy(animateValues = false)
        }
    }

    internal fun clear() {
        reportStates.clear()
        recentArchives = null
        lastState = null
    }
}

@Composable
fun StatsRoute(
    usageAccessStatus: UsageAccessStatus,
    groupsWithApps: List<AppGroupWithApps>,
    userPoints: Double,
    todayPoints: Double,
    profileDisplayName: String?,
    profileAvatarUri: String?,
    archiveRepository: DailyArchiveRepository,
    reportMemoryCache: StatsReportMemoryCache,
    screenEnterReplayToken: Int,
    isProActive: Boolean,
    offlineFocusEnabled: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onRequestUsageAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val offlineFocusRepository = remember(context) {
        OfflineFocusRepository(context, AppDatabase.getDatabase(context))
    }
    val zoneId = remember { ZoneId.systemDefault() }
    val restoredState = remember(reportMemoryCache) { reportMemoryCache.restoreState() }
    var selectedTab by remember(reportMemoryCache) { mutableStateOf(restoredState?.selectedTab ?: ReportTab.DAY) }
    var selectedArchiveDate by remember(reportMemoryCache) { mutableStateOf(restoredState?.selectedArchiveDate) }
    var selectedWeekStart by remember(reportMemoryCache) { mutableStateOf(restoredState?.selectedWeekStart) }
    var selectedMonth by remember(reportMemoryCache) { mutableStateOf(restoredState?.selectedMonth) }
    var selectedYear by remember(reportMemoryCache) { mutableStateOf(restoredState?.selectedYear) }
    var uiState by remember(reportMemoryCache) {
        mutableStateOf(
            restoredState?.copy(
                isPermissionGranted = usageAccessStatus == UsageAccessStatus.GRANTED,
                isRefreshing = false,
                animateValues = false,
            ) ?: DailyReportUiState(
                isPermissionGranted = usageAccessStatus == UsageAccessStatus.GRANTED,
                selectedTab = selectedTab,
            ),
        )
    }

    fun applyUiState(nextState: DailyReportUiState) {
        uiState = nextState
        reportMemoryCache.rememberLastState(nextState)
    }

    LaunchedEffect(
        usageAccessStatus,
        selectedTab,
        selectedArchiveDate,
        selectedWeekStart,
        selectedMonth,
        selectedYear,
        screenEnterReplayToken,
    ) {
        if (usageAccessStatus != UsageAccessStatus.GRANTED) {
            applyUiState(DailyReportUiState(
                isPermissionGranted = false,
                selectedTab = selectedTab,
                isRefreshing = false,
                selectedArchiveDate = selectedArchiveDate,
                selectedWeekStart = selectedWeekStart,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
            ))
            return@LaunchedEffect
        }

        val businessToday = BusinessDay.today(zoneId, BusinessDay.cachedStartHour())

        applyUiState(
            createRefreshingUiState(
                selectedTab = selectedTab,
                previous = uiState,
                selectedArchiveDate = selectedArchiveDate,
                selectedWeekStart = selectedWeekStart,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
            )
        )

        val historicalArchives =
            withContext(Dispatchers.IO) {
                archiveRepository
                    .getRecentArchives(limit = 3650)
                    .first()
                    .sortedByDescending { it.archiveDate }
            }.also(reportMemoryCache::putRecentArchives)
        val liveDaySnapshot =
            withContext(Dispatchers.IO) {
                archiveRepository.buildLiveDayReportSnapshot()
            }
        val recentArchives =
            (listOf(liveDaySnapshot.archive) + historicalArchives)
                .distinctBy { it.archiveDate }
                .sortedByDescending { it.archiveDate }

        when (selectedTab) {
            ReportTab.DAY -> {
                val normalizedSelectedDate =
                    when {
                        recentArchives.isEmpty() -> null
                        selectedArchiveDate != null &&
                            recentArchives.any { it.archiveDate == selectedArchiveDate } -> selectedArchiveDate
                        else -> recentArchives.first().archiveDate
                    }
                if (normalizedSelectedDate != selectedArchiveDate) {
                    selectedArchiveDate = normalizedSelectedDate
                    return@LaunchedEffect
                }
                if (normalizedSelectedDate != liveDaySnapshot.archive.archiveDate) {
                    val archivesDesc = recentArchives.sortedByDescending { it.archiveDate }
                    val selectedIndex = archivesDesc.indexOfFirst { it.archiveDate == normalizedSelectedDate }
                    reportMemoryCache.getReport(
                        selectedTab = ReportTab.DAY,
                        selectedArchiveDate = normalizedSelectedDate,
                        selectedWeekStart = null,
                        selectedMonth = null,
                        selectedYear = null,
                    )?.let { cachedState ->
                        applyUiState(cachedState.copy(
                            isPermissionGranted = true,
                            selectedTab = selectedTab,
                            isRefreshing = false,
                            animateValues = false,
                            previousArchiveDate = archivesDesc.getOrNull(selectedIndex + 1)?.archiveDate,
                            nextArchiveDate = archivesDesc.getOrNull(selectedIndex - 1)?.archiveDate,
                            availableArchiveDates = archivesDesc.map { it.archiveDate },
                        ))
                        return@LaunchedEffect
                    }
                }
                buildArchivedDayReportUiState(
                    selectedDate = normalizedSelectedDate,
                    recentArchives = recentArchives,
                    archiveRepository = archiveRepository,
                    offlineFocusRepository = offlineFocusRepository,
                    liveDaySnapshot = liveDaySnapshot,
                    updateState = { transform ->
                        val nextState = transform(uiState)
                        applyUiState(nextState)
                        reportMemoryCache.rememberReport(nextState)
                    },
                )
                return@LaunchedEffect
            }
            ReportTab.WEEK, ReportTab.MONTH, ReportTab.YEAR -> {
                val availableWeekStarts = buildAvailableWeekStarts(recentArchives)
                val availableMonths = buildAvailableMonths(recentArchives)
                val availableYears = buildAvailableYears(recentArchives)
                val normalizedWeekStart =
                    selectedWeekStart
                        ?.takeIf { it in availableWeekStarts }
                        ?: availableWeekStarts.firstOrNull()
                val normalizedMonth =
                    selectedMonth
                        ?.takeIf { it in availableMonths }
                        ?: availableMonths.firstOrNull()
                val normalizedYear =
                    selectedYear
                        ?.takeIf { it in availableYears }
                        ?: availableYears.firstOrNull()
                if (selectedTab == ReportTab.WEEK && normalizedWeekStart != selectedWeekStart) {
                    selectedWeekStart = normalizedWeekStart
                    return@LaunchedEffect
                }
                if (selectedTab == ReportTab.MONTH && normalizedMonth != selectedMonth) {
                    selectedMonth = normalizedMonth
                    return@LaunchedEffect
                }
                if (selectedTab == ReportTab.YEAR && normalizedYear != selectedYear) {
                    selectedYear = normalizedYear
                    return@LaunchedEffect
                }
                val normalizedSelectionIncludesToday =
                    when (selectedTab) {
                        ReportTab.WEEK -> normalizedWeekStart?.let { businessToday in it..it.plusDays(6) } == true
                        ReportTab.MONTH -> normalizedMonth == YearMonth.from(businessToday)
                        ReportTab.YEAR -> normalizedYear == businessToday.year
                        ReportTab.DAY -> false
                    }
                if (!normalizedSelectionIncludesToday) {
                    val selectedWeekIndex = availableWeekStarts.indexOf(normalizedWeekStart)
                    val selectedMonthIndex = availableMonths.indexOf(normalizedMonth)
                    val selectedYearIndex = availableYears.indexOf(normalizedYear)
                    reportMemoryCache.getReport(
                        selectedTab = selectedTab,
                        selectedArchiveDate = null,
                        selectedWeekStart = normalizedWeekStart,
                        selectedMonth = normalizedMonth,
                        selectedYear = normalizedYear,
                    )?.let { cachedState ->
                        applyUiState(cachedState.copy(
                            isPermissionGranted = true,
                            selectedTab = selectedTab,
                            isRefreshing = false,
                            animateValues = false,
                            previousWeekStart = availableWeekStarts.getOrNull(selectedWeekIndex + 1),
                            nextWeekStart = availableWeekStarts.getOrNull(selectedWeekIndex - 1),
                            availableWeekStarts = availableWeekStarts,
                            previousMonth = availableMonths.getOrNull(selectedMonthIndex + 1),
                            nextMonth = availableMonths.getOrNull(selectedMonthIndex - 1),
                            availableMonths = availableMonths,
                            previousYear = availableYears.getOrNull(selectedYearIndex + 1),
                            nextYear = availableYears.getOrNull(selectedYearIndex - 1),
                            availableYears = availableYears,
                        ))
                        return@LaunchedEffect
                    }
                }
                buildArchivedWindowReportUiState(
                    selectedTab = selectedTab,
                    zoneId = zoneId,
                    archiveRepository = archiveRepository,
                    offlineFocusRepository = offlineFocusRepository,
                    recentArchives = recentArchives,
                    liveDaySnapshot = liveDaySnapshot,
                    selectedWeekStart = normalizedWeekStart,
                    selectedMonth = normalizedMonth,
                    selectedYear = normalizedYear,
                    updateState = { transform ->
                        val nextState = transform(uiState)
                        applyUiState(nextState)
                        reportMemoryCache.rememberReport(nextState)
                    },
                )
                return@LaunchedEffect
            }
        }
    }

    StatsScreenLayout(
        state = uiState,
        onTabSelected = { selectedTab = it },
        onPreviousArchiveDate = {
            uiState.previousArchiveDate?.let { previousDate ->
                selectedArchiveDate = previousDate
            }
        },
        onNextArchiveDate = {
            uiState.nextArchiveDate?.let { nextDate ->
                selectedArchiveDate = nextDate
            }
        },
        onSelectArchiveDate = { date ->
            selectedArchiveDate = date
        },
        onPreviousWeek = {
            uiState.previousWeekStart?.let { selectedWeekStart = it }
        },
        onNextWeek = {
            uiState.nextWeekStart?.let { selectedWeekStart = it }
        },
        onSelectWeekStart = { weekStart ->
            selectedWeekStart = weekStart
        },
        onPreviousMonth = {
            uiState.previousMonth?.let { selectedMonth = it }
        },
        onNextMonth = {
            uiState.nextMonth?.let { selectedMonth = it }
        },
        onSelectMonth = { month ->
            selectedMonth = month
        },
        onPreviousYear = {
            uiState.previousYear?.let { selectedYear = it }
        },
        onNextYear = {
            uiState.nextYear?.let { selectedYear = it }
        },
        onSelectYear = { year ->
            selectedYear = year
        },
        isProActive = isProActive,
        offlineFocusEnabled = offlineFocusEnabled,
        profileDisplayName = profileDisplayName,
        profileAvatarUri = profileAvatarUri,
        onShowProUpsell = onShowProUpsell,
        onRequestUsageAccess = onRequestUsageAccess,
        screenEnterReplayToken = screenEnterReplayToken,
        modifier = modifier,
    )
}

@Composable
private fun StatsScreenLayout(
    state: DailyReportUiState,
    onTabSelected: (ReportTab) -> Unit,
    onPreviousArchiveDate: () -> Unit,
    onNextArchiveDate: () -> Unit,
    onSelectArchiveDate: (String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onSelectYear: (Int) -> Unit,
    isProActive: Boolean,
    offlineFocusEnabled: Boolean,
    profileDisplayName: String?,
    profileAvatarUri: String?,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onRequestUsageAccess: () -> Unit,
    screenEnterReplayToken: Int,
    modifier: Modifier = Modifier,
) {
    TinyVowPageBackground(modifier = modifier) {
        when {
            !state.isPermissionGranted -> PermissionRequiredState(
                onRequestUsageAccess = onRequestUsageAccess,
            )
            state.placeholderTitle != null -> PlaceholderReportScreen(
                state = state,
                onTabSelected = onTabSelected,
            )
            else -> DailyReportScreen(
                state = state,
                onTabSelected = onTabSelected,
                onPreviousArchiveDate = onPreviousArchiveDate,
                onNextArchiveDate = onNextArchiveDate,
                onSelectArchiveDate = onSelectArchiveDate,
                onPreviousWeek = onPreviousWeek,
                onNextWeek = onNextWeek,
                onSelectWeekStart = onSelectWeekStart,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onSelectMonth = onSelectMonth,
                onPreviousYear = onPreviousYear,
                onNextYear = onNextYear,
                onSelectYear = onSelectYear,
                isProActive = isProActive,
                offlineFocusEnabled = offlineFocusEnabled,
                profileDisplayName = profileDisplayName,
                profileAvatarUri = profileAvatarUri,
                onShowProUpsell = onShowProUpsell,
                screenEnterReplayToken = screenEnterReplayToken,
            )
        }
    }
}

@Composable
private fun DailyReportScreen(
    state: DailyReportUiState,
    onTabSelected: (ReportTab) -> Unit,
    onPreviousArchiveDate: () -> Unit,
    onNextArchiveDate: () -> Unit,
    onSelectArchiveDate: (String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onSelectYear: (Int) -> Unit,
    isProActive: Boolean,
    offlineFocusEnabled: Boolean,
    profileDisplayName: String?,
    profileAvatarUri: String?,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    screenEnterReplayToken: Int,
) {
    val listState = rememberLazyListState()
    val reportColors = LocalReportColors.current
    val canShare = isReportShareReady(state = state, isProActive = isProActive)
    var showSharePreview by remember(
        state.selectedTab,
        state.selectedArchiveDate,
        state.selectedWeekStart,
        state.selectedMonth,
        state.selectedYear,
    ) {
        mutableStateOf(false)
    }
    LaunchedEffect(
        state.selectedTab,
        state.selectedWeekStart,
        state.selectedMonth,
        state.selectedYear,
    ) {
        listState.scrollToItem(0)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(reportColors.pageGradient))
                .padding(
                    horizontal = TinyVowSpacing.PageHorizontal,
                    vertical = 6.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ReportPeriodControl(
                    state = state,
                    onTabSelected = onTabSelected,
                    onPreviousArchiveDate = onPreviousArchiveDate,
                    onNextArchiveDate = onNextArchiveDate,
                    onSelectArchiveDate = onSelectArchiveDate,
                    onPreviousWeek = onPreviousWeek,
                    onNextWeek = onNextWeek,
                    onSelectWeekStart = onSelectWeekStart,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onSelectMonth = onSelectMonth,
                    onPreviousYear = onPreviousYear,
                    onNextYear = onNextYear,
                    onSelectYear = onSelectYear,
                    modifier = Modifier.weight(1f),
                )
                ReportShareIconCard(
                    enabled = canShare,
                    onClick = { showSharePreview = true },
                    modifier = Modifier.size(ReportTopControlHeight),
                )
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TinyVowSpacing.PageHorizontal),
                    verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
                ) {
                    ReportPageContent(
                        state = state,
                        isProActive = isProActive,
                        offlineFocusEnabled = offlineFocusEnabled,
                        onShowProUpsell = onShowProUpsell,
                        screenEnterReplayToken = screenEnterReplayToken,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showSharePreview) {
        ReportPageSharePreviewDialog(
            state = state,
            isProActive = isProActive,
            offlineFocusEnabled = offlineFocusEnabled,
            profileDisplayName = profileDisplayName,
            profileAvatarUri = profileAvatarUri,
            onDismiss = { showSharePreview = false },
        )
    }
}

@Composable
private fun ReportPeriodControl(
    state: DailyReportUiState,
    onTabSelected: (ReportTab) -> Unit,
    onPreviousArchiveDate: () -> Unit,
    onNextArchiveDate: () -> Unit,
    onSelectArchiveDate: (String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onSelectYear: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(ReportTopControlHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReportTabSegmentedRow(
            selectedTab = state.selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.wrapContentWidth(),
        )
        Spacer(modifier = Modifier.width(4.dp))
        ReportNavigator(
            state = state,
            onPreviousArchiveDate = onPreviousArchiveDate,
            onNextArchiveDate = onNextArchiveDate,
            onSelectArchiveDate = onSelectArchiveDate,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek,
            onSelectWeekStart = onSelectWeekStart,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onSelectMonth = onSelectMonth,
            onPreviousYear = onPreviousYear,
            onNextYear = onNextYear,
            onSelectYear = onSelectYear,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReportTabSegmentedRow(
    selectedTab: ReportTab,
    onTabSelected: (ReportTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val inset = 2.dp
    val selectedIndex = ReportTab.entries.indexOf(selectedTab).coerceAtLeast(0)
    val segmentSize = ReportTopControlHeight - inset * 2
    val controlWidth = segmentSize * ReportTab.entries.size + inset * 2
    Surface(
        modifier = modifier.width(controlWidth).height(ReportTopControlHeight),
        shape = ReportTopControlShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f)),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val segmentWidth = (maxWidth - inset * 2) / ReportTab.entries.size
            val selectedOffset by animateDpAsState(
                targetValue = inset + segmentWidth * selectedIndex,
                animationSpec = tween(durationMillis = 180),
                label = "reportTabSlider",
            )
            Surface(
                modifier = Modifier
                    .offset(x = selectedOffset, y = inset)
                    .width(segmentWidth)
                    .height(maxHeight - inset * 2),
                shape = RoundedCornerShape(8.dp),
                color = themeColors.baseContainer.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, themeColors.base.copy(alpha = 0.30f)),
            ) {}
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inset),
            ) {
                ReportTab.entries.forEach { tab ->
                    val selected = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onTabSelected(tab) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab.compactLabel(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) themeColors.base else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportPageContent(
    state: DailyReportUiState,
    isProActive: Boolean,
    offlineFocusEnabled: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    screenEnterReplayToken: Int,
    modifier: Modifier = Modifier,
    animateValues: Boolean = state.animateValues,
    shareModules: List<SharePosterModule>? = null,
) {
    if (state.isRefreshing) return

    val isDayReport = state.selectedTab == ReportTab.DAY
    val defaultDayModules =
        buildList {
            add(SharePosterModule.BEHAVIOR)
            add(SharePosterModule.FOCUS)
            add(SharePosterModule.RHYTHM)
            add(SharePosterModule.INSIGHTS)
            if (offlineFocusEnabled) {
                add(SharePosterModule.OFFLINE)
            }
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
    ) {
        if (!isProActive && !isDayReport) {
            LockedAdvancedReportCard(onClick = { onShowProUpsell(ProUpsellSource.ADVANCED_REPORT) })
        } else if (isDayReport) {
            (shareModules ?: defaultDayModules).forEach { module ->
                val moduleContent: @Composable () -> Unit = {
                    when (module) {
                        SharePosterModule.BEHAVIOR -> {
                            DailyBehaviorProfileCard(
                                heroState = state.heroState,
                                focusState = state.dailyFocusState,
                                behaviorState = state.behaviorState,
                            )
                        }
                        SharePosterModule.TIME_TIDE -> DailyTimeTideCard(
                            timeTideState = state.timeTideState,
                            animateValues = animateValues,
                        )
                        SharePosterModule.FOCUS -> {
                            DailyFocusCard(
                                focusState = state.dailyFocusState,
                                compactLayout = false,
                                animateValues = animateValues,
                                screenEnterReplayToken = screenEnterReplayToken,
                            )
                        }
                        SharePosterModule.OFFLINE -> {
                            OfflineFocusMarksCard(state = state.offlineFocusState)
                        }
                        SharePosterModule.APPS -> Unit
                        SharePosterModule.RHYTHM -> DailyRhythmCard(
                            timelineState = state.timelineState,
                            focusState = state.dailyFocusState,
                        )
                        SharePosterModule.POINTS -> Unit
                        SharePosterModule.INSIGHTS -> {
                            DailyInsightCard(behaviorMapState = state.behaviorMapState)
                        }
                        SharePosterModule.OVERVIEW,
                        SharePosterModule.TREND,
                        SharePosterModule.HEATMAP,
                        SharePosterModule.STRUCTURE -> Unit
                    }
                }
                if (shareModules == null && !isProActive && module.isLockedDailyPreviewModule()) {
                    ProLockedDailyPreview(onClick = { onShowProUpsell(ProUpsellSource.ADVANCED_REPORT) }) {
                        moduleContent()
                    }
                } else {
                    moduleContent()
                }
            }
        } else {
            PeriodReportScreen(
                state = state,
                animateValues = animateValues,
                offlineFocusEnabled = offlineFocusEnabled,
                modules = shareModules,
            )
        }
    }
}

@Composable
private fun ProLockedDailyPreview(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    val themeColors = LocalThemeColors.current
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(onClick = onClick),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .blur(3.dp)
                    .graphicsLayer { alpha = 0.92f },
        ) {
            content()
        }
        Canvas(
            modifier =
                Modifier
                    .matchParentSize()
        ) {
            drawRoundRect(
                brush =
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            themeColors.baseContainer.copy(alpha = 0.16f),
                            Color.Black.copy(alpha = 0.10f),
                        ),
                    ),
                cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx()),
            )
        }
        Row(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = AppText.t("pro_report_preview_value"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Surface(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.36f)),
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = AppText.t("pro_locked_preview_title"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = AppText.t("pro_locked_preview_subtitle"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun SharePosterModule.isLockedDailyPreviewModule(): Boolean =
    when (this) {
        SharePosterModule.BEHAVIOR,
        SharePosterModule.FOCUS -> false
        SharePosterModule.TIME_TIDE,
        SharePosterModule.OFFLINE,
        SharePosterModule.APPS,
        SharePosterModule.RHYTHM,
        SharePosterModule.POINTS,
        SharePosterModule.INSIGHTS,
        SharePosterModule.OVERVIEW,
        SharePosterModule.TREND,
        SharePosterModule.HEATMAP,
        SharePosterModule.STRUCTURE -> true
    }

@Composable
private fun ReportNavigator(
    state: DailyReportUiState,
    onPreviousArchiveDate: () -> Unit,
    onNextArchiveDate: () -> Unit,
    onSelectArchiveDate: (String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onSelectYear: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val waitingForDayDate = state.selectedTab == ReportTab.DAY && state.selectedArchiveDate == null
    val waitingForPeriod =
        when (state.selectedTab) {
            ReportTab.WEEK -> state.selectedWeekStart == null
            ReportTab.MONTH -> state.selectedMonth == null
            ReportTab.YEAR -> state.selectedYear == null
            ReportTab.DAY -> false
    }
    if (state.isRefreshing && (waitingForDayDate || waitingForPeriod)) {
        Spacer(modifier = modifier.height(ReportTopControlHeight))
        return
    }
    if (state.selectedTab == ReportTab.DAY) {
        state.selectedArchiveDate?.let { selectedDate ->
            ArchiveDateNavigator(
                selectedArchiveDate = selectedDate,
                previousArchiveDate = state.previousArchiveDate,
                nextArchiveDate = state.nextArchiveDate,
                availableArchiveDates = state.availableArchiveDates,
                onPreviousArchiveDate = onPreviousArchiveDate,
                onNextArchiveDate = onNextArchiveDate,
                onSelectArchiveDate = onSelectArchiveDate,
                modifier = modifier,
            )
        } ?: Spacer(modifier = modifier.height(ReportTopControlHeight))
    } else {
        PeriodNavigator(
            selectedTab = state.selectedTab,
            selectedWeekStart = state.selectedWeekStart,
            previousWeekStart = state.previousWeekStart,
            nextWeekStart = state.nextWeekStart,
            availableWeekStarts = state.availableWeekStarts,
            selectedMonth = state.selectedMonth,
            previousMonth = state.previousMonth,
            nextMonth = state.nextMonth,
            availableMonths = state.availableMonths,
            selectedYear = state.selectedYear,
            previousYear = state.previousYear,
            nextYear = state.nextYear,
            availableYears = state.availableYears,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek,
            onSelectWeekStart = onSelectWeekStart,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onSelectMonth = onSelectMonth,
            onPreviousYear = onPreviousYear,
            onNextYear = onNextYear,
            onSelectYear = onSelectYear,
            modifier = modifier,
        )
    }
}

@Composable
private fun ReportNavigatorSkeleton(
    selectedTab: ReportTab,
    modifier: Modifier = Modifier,
) {
    val title =
        when (selectedTab) {
            ReportTab.DAY -> AppText.t("stats_choose_archive_date")
            else -> AppText.t("stats_choose_period")
        }
    Surface(
        modifier = modifier.height(ReportTopControlHeight),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            SkeletonCircle(size = ReportNavigatorArrowSize)
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = ReportTopControlShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.86f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            SkeletonCircle(size = ReportNavigatorArrowSize)
        }
    }
}
@Composable
private fun PeriodNavigator(
    selectedTab: ReportTab,
    selectedWeekStart: LocalDate?,
    previousWeekStart: LocalDate?,
    nextWeekStart: LocalDate?,
    availableWeekStarts: List<LocalDate>,
    selectedMonth: YearMonth?,
    previousMonth: YearMonth?,
    nextMonth: YearMonth?,
    availableMonths: List<YearMonth>,
    selectedYear: Int?,
    previousYear: Int?,
    nextYear: Int?,
    availableYears: List<Int>,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onSelectYear: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember(selectedTab) { mutableStateOf(false) }
    val topControlColor = LocalThemeColors.current.base
    val title =
        when (selectedTab) {
            ReportTab.WEEK -> selectedWeekStart?.let(::periodWeekCompactLabel) ?: AppText.t("stats_choose_period")
            ReportTab.MONTH -> selectedMonth?.format(DateTimeFormatter.ofPattern(AppText.t("stats_mmmm_yyyy"), Locale.getDefault())) ?: AppText.t("stats_choose_period")
            ReportTab.YEAR -> selectedYear?.let { AppText.t("stats_year_value", it) } ?: AppText.t("stats_choose_period")
            ReportTab.DAY -> ""
        }
    val canGoPrevious =
        when (selectedTab) {
            ReportTab.WEEK -> previousWeekStart != null
            ReportTab.MONTH -> previousMonth != null
            ReportTab.YEAR -> previousYear != null
            ReportTab.DAY -> false
        }
    val canGoNext =
        when (selectedTab) {
            ReportTab.WEEK -> nextWeekStart != null
            ReportTab.MONTH -> nextMonth != null
            ReportTab.YEAR -> nextYear != null
            ReportTab.DAY -> false
        }
    Surface(
        modifier = modifier.height(ReportTopControlHeight),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            ReportNavigatorArrowButton(
                modifier = Modifier.size(ReportNavigatorArrowSize),
                onClick = {
                    when (selectedTab) {
                        ReportTab.WEEK -> onPreviousWeek()
                        ReportTab.MONTH -> onPreviousMonth()
                        ReportTab.YEAR -> onPreviousYear()
                        ReportTab.DAY -> Unit
                    }
                },
                enabled = canGoPrevious,
                symbol = AppText.t("stats_previous_symbol"),
                contentDescription = AppText.t("stats_previous"),
            )
            Surface(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxHeight(),
                onClick = { showDialog = true },
                shape = ReportTopControlShape,
                color = Color.Transparent,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = topControlColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            ReportNavigatorArrowButton(
                modifier = Modifier.size(ReportNavigatorArrowSize),
                onClick = {
                    when (selectedTab) {
                        ReportTab.WEEK -> onNextWeek()
                        ReportTab.MONTH -> onNextMonth()
                        ReportTab.YEAR -> onNextYear()
                        ReportTab.DAY -> Unit
                    }
                },
                enabled = canGoNext,
                symbol = AppText.t("stats_next_symbol"),
                contentDescription = AppText.t("stats_next"),
            )
        }
    }
    if (showDialog) {
        when (selectedTab) {
            ReportTab.WEEK ->
                PeriodWeekPickerDialog(
                    selectedWeekStart = selectedWeekStart,
                    availableWeekStarts = availableWeekStarts,
                    onDismiss = { showDialog = false },
                    onSelectWeekStart = {
                        showDialog = false
                        onSelectWeekStart(it)
                    },
                )
            ReportTab.MONTH ->
                PeriodMonthPickerDialog(
                    selectedMonth = selectedMonth,
                    availableMonths = availableMonths,
                    onDismiss = { showDialog = false },
                    onSelectMonth = {
                        showDialog = false
                        onSelectMonth(it)
                    },
                )
            ReportTab.YEAR ->
                PeriodYearPickerDialog(
                    selectedYear = selectedYear,
                    availableYears = availableYears,
                    onDismiss = { showDialog = false },
                    onSelectYear = {
                        showDialog = false
                        onSelectYear(it)
                    },
                )
            ReportTab.DAY -> Unit
        }
    }
}

@Composable
private fun ReportNavigatorArrowButton(
    symbol: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        onClick = onClick,
        enabled = enabled,
        shape = ReportTopControlShape,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) themeColors.base else themeColors.inkMuted.copy(alpha = 0.42f),
            )
        }
    }
}

@Composable
private fun PeriodWeekPickerDialog(
    selectedWeekStart: LocalDate?,
    availableWeekStarts: List<LocalDate>,
    onDismiss: () -> Unit,
    onSelectWeekStart: (LocalDate) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_close"))
            }
        },
        title = {
            Text(AppText.t("stats_choose_week"), style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableWeekStarts.forEach { weekStart ->
                    val selected = weekStart == selectedWeekStart
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectWeekStart(weekStart) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) themeColors.baseContainer.copy(alpha = 0.76f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                        border = BorderStroke(
                            1.dp,
                            if (selected) themeColors.base.copy(alpha = 0.18f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
                        ),
                    ) {
                        Text(
                            text = periodWeekLabel(weekStart),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) themeColors.base else themeColors.ink,
                        )
                    }
                }
            }
        },
    )
}

private fun reportCacheKey(
    selectedTab: ReportTab,
    selectedArchiveDate: String?,
    selectedWeekStart: LocalDate?,
    selectedMonth: YearMonth?,
    selectedYear: Int?,
): ReportCacheKey? =
    when (selectedTab) {
        ReportTab.DAY -> selectedArchiveDate?.let { ReportCacheKey(selectedTab, it) }
        ReportTab.WEEK -> selectedWeekStart?.let { ReportCacheKey(selectedTab, it.toString()) }
        ReportTab.MONTH -> selectedMonth?.let { ReportCacheKey(selectedTab, it.toString()) }
        ReportTab.YEAR -> selectedYear?.let { ReportCacheKey(selectedTab, it.toString()) }
    }

@Composable
private fun PeriodMonthPickerDialog(
    selectedMonth: YearMonth?,
    availableMonths: List<YearMonth>,
    onDismiss: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(AppText.t("group_close")) }
        },
        title = {
            Text(AppText.t("stats_choose_month"), style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableMonths.forEach { month ->
                    val selected = month == selectedMonth
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectMonth(month) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) themeColors.baseContainer.copy(alpha = 0.76f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                        border = BorderStroke(
                            1.dp,
                            if (selected) themeColors.base.copy(alpha = 0.18f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
                        ),
                    ) {
                        Text(
                            text = month.format(DateTimeFormatter.ofPattern(AppText.t("stats_mmmm_yyyy"), Locale.getDefault())),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) themeColors.base else themeColors.ink,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun PeriodYearPickerDialog(
    selectedYear: Int?,
    availableYears: List<Int>,
    onDismiss: () -> Unit,
    onSelectYear: (Int) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(AppText.t("group_close")) }
        },
        title = {
            Text(AppText.t("stats_choose_year"), style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableYears.forEach { year ->
                    val selected = year == selectedYear
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectYear(year) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) themeColors.baseContainer.copy(alpha = 0.76f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                        border = BorderStroke(
                            1.dp,
                            if (selected) themeColors.base.copy(alpha = 0.18f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
                        ),
                    ) {
                        Text(
                            text = AppText.t("stats_year_value", year),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) themeColors.base else themeColors.ink,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun ArchiveDateNavigator(
    selectedArchiveDate: String,
    previousArchiveDate: String?,
    nextArchiveDate: String?,
    availableArchiveDates: List<String>,
    onPreviousArchiveDate: () -> Unit,
    onNextArchiveDate: () -> Unit,
    onSelectArchiveDate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCalendar by remember(selectedArchiveDate) { mutableStateOf(false) }
    val availableDates = remember(availableArchiveDates) { availableArchiveDates.map(LocalDate::parse).toSet() }
    val topControlColor = LocalThemeColors.current.base
    Surface(
        modifier = modifier.height(ReportTopControlHeight),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            ReportNavigatorArrowButton(
                modifier = Modifier.size(ReportNavigatorArrowSize),
                onClick = onPreviousArchiveDate,
                enabled = previousArchiveDate != null,
                symbol = AppText.t("stats_previous_symbol"),
                contentDescription = AppText.t("stats_previous_day"),
            )
            Surface(
                modifier =
                    Modifier
                        .weight(1f, fill = false)
                        .fillMaxHeight(),
                onClick = { showCalendar = true },
                shape = ReportTopControlShape,
                color = Color.Transparent,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = formatArchiveDate(selectedArchiveDate, AppText.t("stats_archive_date_full")),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = topControlColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            ReportNavigatorArrowButton(
                modifier = Modifier.size(ReportNavigatorArrowSize),
                onClick = onNextArchiveDate,
                enabled = nextArchiveDate != null,
                symbol = AppText.t("stats_next_symbol"),
                contentDescription = AppText.t("stats_next_day"),
            )
        }
    }
    if (showCalendar) {
        ArchiveCalendarDialog(
            selectedArchiveDate = LocalDate.parse(selectedArchiveDate),
            availableDates = availableDates,
            onDismiss = { showCalendar = false },
            onSelectDate = { date ->
                showCalendar = false
                onSelectArchiveDate(ArchiveDateUtils.formatDate(date))
            },
        )
    }
}

@Composable
private fun ArchiveCalendarDialog(
    selectedArchiveDate: LocalDate,
    availableDates: Set<LocalDate>,
    onDismiss: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    var displayedMonth by remember(selectedArchiveDate, availableDates) {
        mutableStateOf(
            availableDates
                .firstOrNull { it == selectedArchiveDate }
                ?.let { YearMonth.from(it) }
                ?: YearMonth.from(selectedArchiveDate),
        )
    }
    val minMonth = remember(availableDates) { availableDates.minOrNull()?.let { YearMonth.from(it) } ?: YearMonth.from(selectedArchiveDate) }
    val maxMonth = remember(availableDates) { availableDates.maxOrNull()?.let { YearMonth.from(it) } ?: YearMonth.from(selectedArchiveDate) }
    val firstOfMonth = displayedMonth.atDay(1)
    val leadingBlankDays = (firstOfMonth.dayOfWeek.value + 6) % 7
    val daysInMonth = displayedMonth.lengthOfMonth()
    val daySlots =
        buildList<LocalDate?> {
            repeat(leadingBlankDays) { add(null) }
            repeat(daysInMonth) { offset ->
                add(displayedMonth.atDay(offset + 1))
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_close"))
            }
        },
        title = {
            Text(
                text = AppText.t("stats_choose_archive_date"),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                        enabled = displayedMonth > minMonth,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("stats_last_month"))
                    }
                    Text(
                        text = displayedMonth.format(DateTimeFormatter.ofPattern(AppText.t("stats_mmmm_yyyy"), Locale.CHINA)),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = themeColors.inkStrong,
                    )
                    IconButton(
                        onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                        enabled = displayedMonth < maxMonth,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = AppText.t("stats_next_month"))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf(AppText.t("stats_mon"), AppText.t("stats_tue"), AppText.t("stats_wed"), AppText.t("stats_thu"), AppText.t("stats_fri"), AppText.t("stats_sat"), AppText.t("stats_sun")).forEach { dayLabel ->
                        Text(
                            text = dayLabel,
                            modifier = Modifier.width(32.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = themeColors.inkMuted,
                        )
                    }
                }
                daySlots.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        week.forEach { date ->
                            when {
                                date == null -> Spacer(modifier = Modifier.size(32.dp))
                                else -> {
                                    val isSelected = date == selectedArchiveDate
                                    val isEnabled = date in availableDates
                                    Surface(
                                        modifier =
                                            Modifier
                                                .size(32.dp)
                                                .clickable(enabled = isEnabled) { onSelectDate(date) },
                                        shape = CircleShape,
                                        color =
                                            when {
                                                isSelected -> themeColors.base
                                                isEnabled -> themeColors.baseContainer.copy(alpha = 0.40f)
                                                else -> Color.Transparent
                                            },
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = date.dayOfMonth.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color =
                                                    when {
                                                        isSelected -> themeColors.onBase
                                                        isEnabled -> themeColors.ink
                                                        else -> themeColors.ink.copy(alpha = 0.28f)
                                                    },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    }
                }
                Text(
                    text = AppText.t("stats_unarchived_dates_not_selectable"),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                )
            }
        },
    )
}

@Composable
private fun PlaceholderReportScreen(
    state: DailyReportUiState,
    onTabSelected: (ReportTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = TinyVowSpacing.PageHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
    ) {
        ReportTabRow(selectedTab = state.selectedTab, onTabSelected = onTabSelected)
        ReportCard {
            TinyVowStatusPill(
                text = AppText.t("stats_archived_reports_waiting"),
                color = LocalThemeColors.current.base,
                containerColor = LocalThemeColors.current.baseContainer,
                modifier = Modifier.align(Alignment.Start),
            )
            Text(
                text = state.placeholderTitle.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                color = LocalThemeColors.current.inkStrong,
            )
            Text(
                text = state.placeholderDescription.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = LocalThemeColors.current.inkMuted,
            )
        }
    }
}

@Composable
private fun PermissionRequiredState(
    onRequestUsageAccess: () -> Unit,
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TinyVowEmptyState(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TinyVowSpacing.PageHorizontal),
            icon = Icons.Default.BarChart,
            title = AppText.t("stats_report_needs_usage_records_permission"),
            body = AppText.t("stats_enable_usage_records_for_daily_report"),
            action = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TinyVowButton(
                        text = AppText.t("stats_view_details_and_enable"),
                        onClick = onRequestUsageAccess,
                        tone = TinyVowButtonTone.Primary,
                        modifier = Modifier.weight(1f),
                    )
                    TinyVowButton(
                        text = AppText.t("stats_open_settings"),
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            },
        )
    }
}

@Composable
private fun ReportTabRow(
    selectedTab: ReportTab,
    onTabSelected: (ReportTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReportTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            TinyVowButton(
                text = tab.label(),
                onClick = { onTabSelected(tab) },
                selected = selected,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun ReportTabDropdown(
    selectedTab: ReportTab,
    onTabSelected: (ReportTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val popupOffsetY = with(density) { (ReportTopControlHeight + 6.dp).roundToPx() }
    val themeColors = LocalThemeColors.current
    val contentColor = themeColors.base
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            onClick = { expanded = true },
            shape = RoundedCornerShape(18.dp),
            color = themeColors.baseContainer.copy(alpha = 0.46f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = selectedTab.label(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, popupOffsetY),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                Surface(
                    modifier = Modifier.width(64.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)),
                    tonalElevation = 2.dp,
                    shadowElevation = 6.dp,
                ) {
                    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        ReportTab.entries.forEach { tab ->
                            ReportTabMenuItem(
                                tab = tab,
                                selected = tab == selectedTab,
                                onClick = {
                                    expanded = false
                                    onTabSelected(tab)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportTabMenuItem(
    tab: ReportTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val themeColors = LocalThemeColors.current
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(onClick = onClick),
        shape = shape,
        color =
            if (selected) {
                themeColors.base.copy(alpha = 0.12f)
            } else {
                Color.Transparent
            },
    ) {
        Text(
            text = tab.label(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) themeColors.base else themeColors.base.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DeviceHeroCard(
    selectedTab: ReportTab,
    heroState: SectionState<HeroSectionData>,
) {
    ReportCard {
        AdaptiveRowGrid(
            itemCount = 2,
            compactColumns = 1,
            expandedColumns = 2,
            horizontalSpacing = 12.dp,
            verticalSpacing = 12.dp,
        ) { modifier, index ->
            when (index) {
                0 -> DeviceHeroVisualPanel(
                    selectedTab = selectedTab,
                    heroState = heroState,
                    modifier = modifier,
                )
                else -> DeviceHeroMetricsPanel(
                    heroState = heroState,
                    modifier = modifier,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeviceHeroVisualPanel(
    selectedTab: ReportTab,
    heroState: SectionState<HeroSectionData>,
    modifier: Modifier = Modifier,
) {
    val data = (heroState as? SectionState.Ready)?.data
    val summary = data?.summary ?: DailyReportSummary(
        title = AppText.t("stats_archived_daily_reports"),
        subtitle = "",
        capturedAt = "",
        message = "",
        primaryValue = "",
        secondaryValue = "",
        tertiaryValue = "",
        tags = emptyList(),
    )
    val overview = data?.overview ?: ScopeOverview(
        totalUsageMillis = 0L,
        openCount = 0,
        activeBucketCount = 0,
        topApp = null,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
    ) {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            val chartHeight = if (compact) 126.dp else 146.dp
            val contentPadding = if (compact) 16.dp else 18.dp
            val contentSpacing = if (compact) 12.dp else 14.dp
            Column(
                modifier = Modifier.padding(horizontal = contentPadding, vertical = contentPadding),
                verticalArrangement = Arrangement.spacedBy(contentSpacing),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (data == null) {
                            SkeletonLine(width = 88.dp, height = 12.dp)
                        } else {
                            Text(
                                text = data.summary.subtitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = data?.summary?.title ?: AppText.t("stats_archived_daily_reports"),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    if (data == null) {
                        SkeletonLine(width = 72.dp, height = 10.dp)
                    } else {
                        Text(
                            text = data.summary.capturedAt,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (data == null) {
                        SkeletonBlock(
                            modifier = Modifier.fillMaxWidth(),
                            height = chartHeight,
                            shape = RoundedCornerShape(TinyVowRadius.Card),
                        )
                    } else {
                        UsageGoalChart(
                            usageMillis = data.overview.totalUsageMillis,
                            capMillis = data.dailyGoalMillis.takeIf { selectedTab == ReportTab.DAY && it > 0L }
                                ?: usageDialCapMillis(selectedTab),
                            goalLabel = data.dailyGoalMillis.takeIf { selectedTab == ReportTab.DAY && it > 0L }
                                ?.let { AppText.t("stats_target_value_2", formatDuration(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(chartHeight),
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (data == null) {
                        SkeletonPill(width = 72.dp)
                        SkeletonPill(width = 80.dp)
                        SkeletonPill(width = 64.dp)
                    } else {
                        data.summary.tags.forEach { tag ->
                            SummaryTagChip(tag)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceHeroMetricsPanel(
    heroState: SectionState<HeroSectionData>,
    modifier: Modifier = Modifier,
) {
    val data = (heroState as? SectionState.Ready)?.data
    val overview = data?.overview ?: ScopeOverview(
        totalUsageMillis = 0L,
        openCount = 0,
        activeBucketCount = 0,
        topApp = null,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
    ) {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            val contentPadding = if (compact) 16.dp else 18.dp
            val contentSpacing = if (compact) 12.dp else 14.dp
            Column(
                modifier = Modifier.padding(horizontal = contentPadding, vertical = contentPadding),
                verticalArrangement = Arrangement.spacedBy(contentSpacing),
            ) {
                if (data == null) {
                    SkeletonLine(fill = true, height = 18.dp)
                } else {
                    Text(
                        text = data.summary.message,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                AdaptiveRowGrid(
                    itemCount = 4,
                    compactColumns = 2,
                    expandedColumns = 2,
                    horizontalSpacing = 10.dp,
                    verticalSpacing = 10.dp,
                ) { childModifier, index ->
                    if (data == null) {
                        SkeletonMetricChip(modifier = childModifier)
                        return@AdaptiveRowGrid
                    }
                    when (index) {
                        0 -> HeroMetricChip(
                            icon = Icons.Default.PhoneAndroid,
                            label = AppText.t("stats_device_usage"),
                            value = data.summary.primaryValue,
                            modifier = childModifier,
                        )
                        1 -> HeroMetricChip(
                            icon = Icons.AutoMirrored.Filled.CompareArrows,
                            label = if (data.summary.title == AppText.t("stats_archived_daily_reports")) AppText.t("stats_label_10") else AppText.t("stats_comparison_baseline"),
                            value = data.summary.secondaryValue,
                            modifier = childModifier,
                        )
                        2 -> HeroMetricChip(
                            icon = Icons.Default.TouchApp,
                            label = AppText.t("stats_launches"),
                            value = AppText.t("stats_value_times_12", overview.openCount),
                            modifier = childModifier,
                        )
                        else -> HeroMetricChip(
                            icon = Icons.Default.NightsStay,
                            label =
                                if (data.summary.title == AppText.t("stats_archived_daily_reports") && data.goalCompletionProgress != null) {
                                    AppText.t("stats_target_complete")
                                } else {
                                    AppText.t("stats_night_use")
                                },
                            value =
                                if (data.summary.title == AppText.t("stats_archived_daily_reports") && data.goalCompletionProgress != null) {
                                    "${(data.goalCompletionProgress * 100f).roundToInt()}%"
                                } else {
                                    formatDuration(data.nightUsageMillis)
                                },
                            modifier = childModifier,
                        )
                    }
                }
                if (data == null) {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth(),
                        height = 62.dp,
                        shape = RoundedCornerShape(20.dp),
                    )
                } else {
                    overview.topApp?.let { topApp ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (compact) 12.dp else 14.dp,
                                vertical = if (compact) 10.dp else 12.dp,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp),
                        ) {
                            val animatedTopAppValue = animateMetricDisplayText(
                                rawText = formatDuration(topApp.value),
                                label = "hero_top_app_${topApp.packageName}",
                                delayMillis = 240,
                            )
                            AppIconCircle(topApp.packageName)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (data.summary.title == AppText.t("stats_archived_daily_reports")) AppText.t("stats_top_app_of_the_day") else AppText.t("stats_top_app_in_window"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = topApp.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = animatedTopAppValue,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun UsageGoalChart(
    usageMillis: Long,
    capMillis: Long,
    goalLabel: String?,
    metricLabel: String? = AppText.t("stats_device_usage"),
    modifier: Modifier = Modifier,
) {
    val stagedUsageMillis = rememberDelayedLongTarget(usageMillis, 40)
    val animatedUsageMillis = animateLongValue(
        targetValue = stagedUsageMillis,
        label = "usage_dial_value",
        durationMillis = 880,
    )
    val animatedProgress = animateDecimalValue(
        targetValue = (stagedUsageMillis.toFloat() / capMillis.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1.15f),
        label = "usage_goal_bar_progress",
        durationMillis = 840,
        delayMillis = 120,
    )
    val reportColors = LocalReportColors.current
    val primary = MaterialTheme.colorScheme.primary
    val warning = reportColors.warning
    val overLimit = usageMillis > capMillis && capMillis > 0L
    val goalTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
    val goalProgressColor = if (overLimit) warning.copy(alpha = 0.88f) else primary.copy(alpha = 0.88f)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.Card),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    metricLabel?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = formatDuration(animatedUsageMillis),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                Text(
                    text =
                        goalLabel ?: if (capMillis > 0L) {
                            AppText.t("stats_reference_value", formatDuration(capMillis))
                        } else {
                            AppText.t("stats_no_targets_yet")
                        },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (overLimit) warning else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Canvas(modifier = Modifier.fillMaxWidth().height(42.dp)) {
                val trackHeight = size.height * 0.46f
                val top = (size.height - trackHeight) / 2f
                val radius = trackHeight / 2f
                drawRoundRect(
                    color = goalTrackColor,
                    topLeft = Offset(0f, top),
                    size = Size(size.width, trackHeight),
                    cornerRadius = CornerRadius(radius, radius),
                )
                val cappedProgress = animatedProgress.coerceIn(0f, 1f)
                drawRoundRect(
                    color = goalProgressColor,
                    topLeft = Offset(0f, top),
                    size = Size(size.width * cappedProgress, trackHeight),
                    cornerRadius = CornerRadius(radius, radius),
                )
                if (animatedProgress > 1f) {
                    val overWidth = size.width * (animatedProgress - 1f).coerceIn(0f, 0.15f) / 0.15f
                    drawRoundRect(
                        color = warning.copy(alpha = 0.28f),
                        topLeft = Offset(size.width - overWidth, top - 5f),
                        size = Size(overWidth, trackHeight + 10f),
                        cornerRadius = CornerRadius(radius, radius),
                    )
                }
                drawLine(
                    color = if (overLimit) warning else primary.copy(alpha = 0.55f),
                    start = Offset(size.width, top - 6f),
                    end = Offset(size.width, top + trackHeight + 6f),
                    strokeWidth = 3f,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AnimatedMetricText(
                    rawText = if (overLimit) AppText.t("stats_over_by_value_2", formatDuration(usageMillis - capMillis)) else AppText.t("stats_remaining_value_2", formatDuration((capMillis - usageMillis).coerceAtLeast(0L))),
                    label = "usage_goal_remaining_${usageMillis}_${capMillis}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (overLimit) warning else MaterialTheme.colorScheme.onSurfaceVariant,
                    delayMillis = 180,
                )
                AnimatedMetricText(
                    rawText = "${((usageMillis.toFloat() / capMillis.coerceAtLeast(1L).toFloat()) * 100f).roundToInt()}%",
                    label = "usage_goal_percent_${usageMillis}_${capMillis}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (overLimit) warning else primary,
                    fontWeight = FontWeight.SemiBold,
                    delayMillis = 200,
                )
            }
        }
    }
}

private fun usageDialCapMillis(selectedTab: ReportTab): Long =
    when (selectedTab) {
        ReportTab.DAY -> 12L * 60L * 60_000L
        ReportTab.WEEK -> 56L * 60L * 60_000L
        ReportTab.MONTH -> 180L * 60L * 60_000L
        ReportTab.YEAR -> 720L * 60L * 60_000L
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SummaryTagChip(tag: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun AdaptiveRowGrid(
    itemCount: Int,
    compactColumns: Int,
    expandedColumns: Int,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 10.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 10.dp,
    itemContent: @Composable (Modifier, Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 420.dp) compactColumns else expandedColumns
        val rows = (0 until itemCount).toList().chunked(columns.coerceAtLeast(1))
        Column(verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                ) {
                    rowItems.forEach { index ->
                        itemContent(Modifier.weight(1f), index)
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
internal fun DailyFocusCard(
    focusState: SectionState<DailyFocusSectionData>,
    compactLayout: Boolean = false,
    animateValues: Boolean = false,
    screenEnterReplayToken: Int = 0,
) {
    when (focusState) {
        SectionState.Empty -> Unit
        SectionState.Loading -> {
            FocusSummarySkeletonGrid(compactLayout = compactLayout)
        }
        is SectionState.Ready -> {
            AdaptiveRowGrid(
                itemCount = 2,
                compactColumns = if (compactLayout) 1 else 2,
                expandedColumns = 2,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
            ) { modifier, index ->
                if (index == 0) {
                    DailyModeSummaryCard(
                        summary = focusState.data.control,
                        icon = Icons.Default.Shield,
                        compact = compactLayout,
                        useHomeMetricCardStyle = true,
                        animateValues = animateValues,
                        replayToken = screenEnterReplayToken,
                        modifier = modifier,
                    )
                } else {
                    DailyModeSummaryCard(
                        summary = focusState.data.encourage,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        compact = compactLayout,
                        useHomeMetricCardStyle = true,
                        animateValues = animateValues,
                        replayToken = screenEnterReplayToken,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WindowFocusCard(
    focusState: SectionState<WindowFocusSectionData>,
) {
    when (focusState) {
        SectionState.Empty -> Unit
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                AdaptiveRowGrid(itemCount = 2, compactColumns = 1, expandedColumns = 2) { modifier, _ ->
                    SkeletonBlock(modifier = modifier, height = 210.dp, shape = RoundedCornerShape(TinyVowRadius.Card))
                }
            }
        }
        is SectionState.Ready -> {
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.EmojiEvents,
                    title = AppText.t("stats_control_and_encourage_review"),
                    subtitle = AppText.t("stats_dashboard_summary_description"),
                )
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 10.dp,
                    verticalSpacing = 10.dp,
                ) { modifier, index ->
                    if (index == 0) {
                        DailyModeSummaryCard(
                            summary = focusState.data.control,
                            icon = Icons.Default.Bolt,
                            animateValues = true,
                            modifier = modifier,
                        )
                    } else {
                        DailyModeSummaryCard(
                            summary = focusState.data.encourage,
                            icon = Icons.Default.RocketLaunch,
                            animateValues = true,
                            modifier = modifier,
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    focusState.data.highlights.forEachIndexed { index, metric ->
                        FocusMetricPill(
                            metric = metric,
                            accent = MaterialTheme.colorScheme.primary,
                            delayMillis = 180 + index * 40,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearDualScopeCard(
    yearState: SectionState<YearDualScopeSectionData>,
) {
    when (yearState) {
        SectionState.Empty -> Unit
        SectionState.Loading -> {
            ReportCard {
                SkeletonSectionHeader()
                AdaptiveRowGrid(itemCount = 2, compactColumns = 1, expandedColumns = 2) { modifier, _ ->
                    SkeletonMetricChip(modifier = modifier)
                }
            }
        }
        is SectionState.Ready -> {
            ReportCard {
                SectionHeader(
                    icon = Icons.Default.CalendarMonth,
                    title = AppText.t("stats_year_dual_view"),
                    subtitle = AppText.t("stats_year_dual_view_description"),
                )
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 10.dp,
                    verticalSpacing = 10.dp,
                ) { modifier, index ->
                    YearScopePanel(
                        summary = if (index == 0) yearState.data.naturalYear else yearState.data.rollingYear,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun YearScopePanel(
    summary: YearScopeSummary,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.Card),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(summary.title, style = MaterialTheme.typography.titleMedium)
            Text(summary.rangeLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = summary.totalUsage,
                style = MaterialTheme.typography.headlineSmall,
            )
            AdaptiveRowGrid(itemCount = 4, compactColumns = 2, expandedColumns = 2, verticalSpacing = 8.dp) { childModifier, index ->
                val metric =
                    when (index) {
                        0 -> DailyFocusMetric(AppText.t("stats_daily_average"), summary.averageUsage)
                        1 -> DailyFocusMetric(AppText.t("stats_active"), summary.activeDays)
                        2 -> DailyFocusMetric(AppText.t("stats_label_5"), summary.savedUsage)
                        else -> DailyFocusMetric(AppText.t("stats_net_points"), summary.pointsNet)
                    }
                FocusMetricPill(
                    metric = metric,
                    accent = MaterialTheme.colorScheme.primary,
                    delayMillis = 120 + index * 40,
                    modifier = childModifier,
                )
            }
        }
    }
}

@Composable
private fun HeatmapCard(
    heatmapState: SectionState<HeatmapSectionData>,
) {
    ReportCard {
        when (heatmapState) {
            SectionState.Loading -> {
                SkeletonSectionHeader()
                SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 180.dp, shape = RoundedCornerShape(TinyVowRadius.Card))
            }
            SectionState.Empty -> {
                SectionHeader(Icons.Default.CalendarMonth, AppText.t("stats_heatmap"), AppText.t("stats_not_enough_archived_data_to_build_a_heatmap"))
            }
            is SectionState.Ready -> {
                SectionHeader(
                    icon = Icons.Default.CalendarMonth,
                    title = heatmapState.data.title,
                    subtitle = heatmapState.data.subtitle,
                )
                HeatmapGrid(data = heatmapState.data)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeatmapGrid(data: HeatmapSectionData) {
    val reportColors = LocalReportColors.current
    val maxValue = data.days.maxOfOrNull { it.valueMillis }?.coerceAtLeast(1L) ?: 1L
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        data.days.forEach { day ->
            val ratio = (day.valueMillis.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
            val cellColor =
                when {
                    day.exceeded -> reportColors.warning.copy(alpha = 0.26f + ratio * 0.58f)
                    day.valueMillis > 0L -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f + ratio * 0.64f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
                }
            Surface(
                modifier = Modifier.size(if (data.days.size <= 12) 56.dp else 34.dp),
                shape = RoundedCornerShape(if (data.days.size <= 12) 16.dp else 10.dp),
                color = cellColor,
                border = if (day.selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = day.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (day.selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportShareIconCard(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val iconColor =
        if (enabled) {
            themeColors.base
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
        }
    IconButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = AppText.t("group_share"),
            tint = iconColor,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ReportShareActionCard(
    state: DailyReportUiState,
    isProActive: Boolean,
) {
    val canShare = isReportShareReady(state = state, isProActive = isProActive)
    var showPreview by remember(state.selectedTab, state.selectedArchiveDate, state.selectedWeekStart, state.selectedMonth, state.selectedYear) {
        mutableStateOf(false)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = AppText.t("stats_share_report"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = AppText.t("stats_preview_the_poster_then_share_it_with_friends"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TinyVowButton(
                text = AppText.t("stats_preview_share_poster"),
                onClick = { showPreview = true },
                enabled = canShare,
                tone = TinyVowButtonTone.Primary,
            )
        }
    }

    if (showPreview) {
        ReportPageSharePreviewDialog(
            state = state,
            isProActive = isProActive,
            offlineFocusEnabled = true,
            onDismiss = { showPreview = false },
        )
    }
}

private fun isReportShareReady(
    state: DailyReportUiState,
    isProActive: Boolean,
): Boolean {
    if (state.isRefreshing) return false
    return when (state.selectedTab) {
        ReportTab.DAY -> state.heroState is SectionState.Ready
        ReportTab.WEEK, ReportTab.MONTH, ReportTab.YEAR -> isProActive && state.periodReportState is SectionState.Ready
    }
}

private fun availableSharePosterModules(
    state: DailyReportUiState,
    isProActive: Boolean,
    offlineFocusEnabled: Boolean,
): List<SharePosterModule> {
    return if (state.selectedTab == ReportTab.DAY) {
        buildList {
            val behaviorStructure = (state.behaviorState as? SectionState.Ready)?.data?.structure
            if (behaviorStructure != null && behaviorStructure.scoreMetrics.isNotEmpty()) {
                add(SharePosterModule.BEHAVIOR)
            }
            if (state.dailyFocusState is SectionState.Ready) {
                add(SharePosterModule.FOCUS)
            }
            val timeline = (state.timelineState as? SectionState.Ready)?.data
            if (isProActive && timeline != null && (timeline.buckets.isNotEmpty() || timeline.periodUsage.any { it.deviceMillis > 0L })) {
                add(SharePosterModule.RHYTHM)
            }
            val behaviorMap = (state.behaviorMapState as? SectionState.Ready)?.data
            if (isProActive && behaviorMap != null && behaviorMap.points.isNotEmpty()) {
                add(SharePosterModule.INSIGHTS)
            }
            if (offlineFocusEnabled && state.offlineFocusState !is SectionState.Loading) {
                add(SharePosterModule.OFFLINE)
            }
        }
    } else {
        val periodData = (state.periodReportState as? SectionState.Ready)?.data
        periodData?.let { availablePeriodReportModules(it, offlineFocusEnabled) }.orEmpty()
    }
}

private fun ReportTab.sharePosterStorageKey(): String = name.lowercase(Locale.US)

private fun restoredSharePosterModules(
    moduleIds: List<String>,
    availableModules: List<SharePosterModule>,
    selectedTab: ReportTab,
): List<SharePosterModule> {
    val hasUnavailableSavedModule =
        moduleIds.any { id -> availableModules.none { module -> module.name == id } }
    if (selectedTab == ReportTab.WEEK && hasUnavailableSavedModule) {
        return defaultSharePosterModules(availableModules, selectedTab)
    }
    return moduleIds
        .mapNotNull { id -> availableModules.firstOrNull { it.name == id } }
        .distinct()
        .ifEmpty { defaultSharePosterModules(availableModules, selectedTab) }
}

private fun defaultSharePosterModules(
    availableModules: List<SharePosterModule>,
    selectedTab: ReportTab,
): List<SharePosterModule> {
    if (selectedTab != ReportTab.DAY) return availableModules.take(3)
    val preferred =
        listOf(
            SharePosterModule.BEHAVIOR,
            SharePosterModule.FOCUS,
            SharePosterModule.INSIGHTS,
        )
    return (preferred.filter { it in availableModules } + availableModules.filterNot { it in preferred })
        .take(3)
}

private fun SharePosterModule.labelKey(selectedTab: ReportTab): String =
    when (this) {
        SharePosterModule.BEHAVIOR -> "stats_behavior_analysis"
        SharePosterModule.TIME_TIDE -> "stats_share_module_time_tide"
        SharePosterModule.FOCUS -> "stats_share_module_focus"
        SharePosterModule.OFFLINE ->
            if (selectedTab == ReportTab.DAY) {
                "offline_focus_marks_title"
            } else {
                "stats_share_module_offline_focus"
            }
        SharePosterModule.APPS -> "stats_share_module_apps"
        SharePosterModule.RHYTHM -> "stats_time_flow"
        SharePosterModule.POINTS -> "stats_weekly_points_trajectory"
        SharePosterModule.INSIGHTS ->
            if (selectedTab == ReportTab.DAY || selectedTab == ReportTab.WEEK) {
                "stats_behavior_map_title"
            } else {
                "stats_share_module_behavior_overview"
            }
        SharePosterModule.OVERVIEW -> "stats_share_module_overview"
        SharePosterModule.TREND -> "stats_share_module_trend"
        SharePosterModule.HEATMAP -> "stats_share_module_heatmap"
        SharePosterModule.STRUCTURE -> "stats_share_module_structure"
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportPageSharePreviewDialog(
    state: DailyReportUiState,
    isProActive: Boolean,
    offlineFocusEnabled: Boolean,
    profileDisplayName: String? = null,
    profileAvatarUri: String? = null,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember(context) { ManagedAppPreferences(context.applicationContext) }
    val scrollState = rememberScrollState()
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val posterWidth = 390.dp
    val baseDensity = LocalDensity.current
    val exportScale = remember(baseDensity, posterWidth) {
        with(baseDensity) {
            (SharePosterMinExportWidthPx / posterWidth.toPx())
                .coerceAtLeast(1f)
                .coerceAtMost(SharePosterMaxExportScale)
        }
    }
    val exportDensity = remember(baseDensity, exportScale) {
        Density(
            density = baseDensity.density * exportScale,
            // Keep exported posters at their designed typography regardless of
            // the reader's in-app text-size preference.
            fontScale = 1f,
        )
    }
    val storageKey = remember(state.selectedTab) { state.selectedTab.sharePosterStorageKey() }
    val availableModules = remember(state, isProActive, offlineFocusEnabled) {
        availableSharePosterModules(
            state = state,
            isProActive = isProActive,
            offlineFocusEnabled = offlineFocusEnabled,
        )
    }
    val restoredModuleIds by produceState<List<String>?>(initialValue = null, preferences, storageKey, offlineFocusEnabled) {
        value = withContext(Dispatchers.IO) {
            preferences.getSharePosterModuleIdsOnce(storageKey)
        }
    }
    var selectedModules by remember(state.selectedTab, state.selectedArchiveDate, state.selectedWeekStart, state.selectedMonth, state.selectedYear, isProActive, offlineFocusEnabled) {
        mutableStateOf(defaultSharePosterModules(availableModules, state.selectedTab))
    }
    LaunchedEffect(availableModules, restoredModuleIds) {
        restoredModuleIds?.let { moduleIds ->
            selectedModules = restoredSharePosterModules(moduleIds, availableModules, state.selectedTab)
        }
    }
    var isSharing by remember { mutableStateOf(false) }

    fun updateSelectedModules(modules: List<SharePosterModule>) {
        selectedModules = modules
        scope.launch(Dispatchers.IO) {
            preferences.setSharePosterModuleIds(
                tabKey = storageKey,
                moduleIds = modules.map { it.name },
            )
        }
    }

    fun shareCurrentPreview() {
        if (isSharing) return
        scope.launch {
            isSharing = true
            runCatching {
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                shareReportBitmap(context = context, bitmap = bitmap)
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    error.message ?: AppText.t("stats_failed_to_generate_share_image"),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            isSharing = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(16.dp),
            shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = AppText.t("stats_share_preview"),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    TextButton(onClick = onDismiss) { Text(AppText.t("group_close")) }
                }
                SharePosterModuleSelector(
                    selectedTab = state.selectedTab,
                    availableModules = availableModules,
                    selectedModules = selectedModules,
                    onToggleModule = { module ->
                        updateSelectedModules(
                            if (module in selectedModules) {
                                selectedModules.filterNot { it == module }
                            } else {
                                selectedModules + module
                            },
                        )
                    },
                    onMoveModule = { module, direction ->
                        val fromIndex = selectedModules.indexOf(module)
                        if (fromIndex >= 0) {
                            val toIndex = (fromIndex + direction).coerceIn(0, selectedModules.lastIndex)
                            if (fromIndex != toIndex) {
                                updateSelectedModules(
                                    selectedModules.toMutableList().apply {
                                        removeAt(fromIndex)
                                        add(toIndex, module)
                                    },
                                )
                            }
                        }
                    },
                )
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(8.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        val previewScale = (maxWidth / posterWidth).coerceAtMost(1f)
                        val displayScale = previewScale / exportScale
                        Box(
                            modifier = Modifier
                                .requiredWidth(posterWidth)
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    layout(
                                        width = (placeable.width * displayScale).roundToInt(),
                                        height = (placeable.height * displayScale).roundToInt(),
                                    ) {
                                        placeable.placeRelativeWithLayer(0, 0) {
                                            scaleX = displayScale
                                            scaleY = displayScale
                                            transformOrigin = TransformOrigin(0f, 0f)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            CompositionLocalProvider(LocalDensity provides exportDensity) {
                                Column(
                                    modifier = Modifier
                                        .requiredWidth(posterWidth)
                                        .drawWithContent {
                                            graphicsLayer.record {
                                                this@drawWithContent.drawContent()
                                            }
                                            drawLayer(graphicsLayer)
                                        }
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(
                                            horizontal = TinyVowSpacing.PageHorizontal,
                                            vertical = TinyVowSpacing.CardVertical,
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
                                ) {
                                    SharePosterIdentityHeader(
                                        state = state,
                                        displayName = profileDisplayName,
                                        avatarUri = profileAvatarUri,
                                    )
                                    ReportPageContent(
                                        state = state,
                                        isProActive = isProActive,
                                        offlineFocusEnabled = offlineFocusEnabled,
                                        onShowProUpsell = {},
                                        screenEnterReplayToken = 0,
                                        animateValues = false,
                                        shareModules = selectedModules,
                                    )
                                    SharePosterDownloadFooter()
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TinyVowButton(
                        text = AppText.t("group_close"),
                        onClick = onDismiss,
                        enabled = !isSharing,
                    )
                    TinyVowButton(
                        onClick = { shareCurrentPreview() },
                        enabled = !isSharing,
                        tone = TinyVowButtonTone.Primary,
                    ) {
                        if (isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(AppText.t("group_share"))
                    }
                }
            }
        }
    }
}

private data class SharePosterPeriodLabel(
    val primary: String,
    val secondary: String? = null,
)

@Composable
private fun SharePosterIdentityHeader(
    state: DailyReportUiState,
    displayName: String?,
    avatarUri: String?,
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
    val periodLabel = remember(
        state.selectedTab,
        state.selectedArchiveDate,
        state.selectedWeekStart,
        state.selectedMonth,
        state.selectedYear,
        locale,
    ) {
        buildSharePosterPeriodLabel(state = state, locale = locale)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                if (avatarUri.isNullOrBlank()) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = AppText.t("me_profile_avatar"),
                        modifier = Modifier.padding(3.dp),
                    )
                } else {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = AppText.t("me_profile_avatar"),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(R.mipmap.ic_launcher_foreground),
                        error = painterResource(R.mipmap.ic_launcher_foreground),
                    )
                }
            }
            Text(
                text = displayName?.takeIf { it.isNotBlank() } ?: AppText.t("me_local_account_default_name"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = periodLabel.primary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
                periodLabel.secondary?.let { secondary ->
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun buildSharePosterPeriodLabel(
    state: DailyReportUiState,
    locale: Locale,
): SharePosterPeriodLabel {
    val isChinese = locale.language.equals("zh", ignoreCase = true)
    val today = BusinessDay.today(ZoneId.systemDefault(), BusinessDay.cachedStartHour())
    fun withLiveStatus(label: String, isLive: Boolean): String =
        if (isLive) "$label · ${AppText.t("stats_status_in_progress")}" else label
    fun monthLabel(month: Int, short: Boolean = false): String =
        if (isChinese) {
            month.toString()
        } else {
            java.time.Month.of(month).getDisplayName(if (short) TextStyle.SHORT else TextStyle.FULL, locale)
        }

    return when (state.selectedTab) {
        ReportTab.DAY -> {
            val date = state.selectedArchiveDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
            SharePosterPeriodLabel(
                primary =
                    withLiveStatus(
                        AppText.t("stats_share_period_day", date.year, monthLabel(date.monthValue), date.dayOfMonth),
                        date == today,
                    ),
                secondary = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
            )
        }
        ReportTab.WEEK -> {
            val start = state.selectedWeekStart ?: LocalDate.now().with(java.time.DayOfWeek.MONDAY)
            val end = start.plusDays(6)
            SharePosterPeriodLabel(
                primary = withLiveStatus(
                    AppText.t(
                        "stats_share_period_week",
                        start.get(IsoFields.WEEK_BASED_YEAR),
                        start.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                    ),
                    today in start..end,
                ),
                secondary = AppText.t(
                    "stats_share_period_week_range",
                    start.year,
                    monthLabel(start.monthValue, short = true),
                    start.dayOfMonth,
                    end.year,
                    monthLabel(end.monthValue, short = true),
                    end.dayOfMonth,
                ),
            )
        }
        ReportTab.MONTH -> {
            val month = state.selectedMonth ?: YearMonth.now()
            SharePosterPeriodLabel(
                primary =
                    withLiveStatus(
                        AppText.t("stats_share_period_month", month.year, monthLabel(month.monthValue)),
                        month == YearMonth.from(today),
                    ),
            )
        }
        ReportTab.YEAR -> SharePosterPeriodLabel(
            primary =
                (state.selectedYear ?: LocalDate.now().year).let { year ->
                    withLiveStatus(AppText.t("stats_share_period_year", year), year == today.year)
                },
        )
    }
}

@Composable
private fun SharePosterDownloadFooter() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.width(64.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tinyvow_share_app_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            }
            Text(
                text = AppText.t("stats_share_download_tagline"),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Serif),
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier.width(64.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pgyer_download_qr),
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SharePosterModuleSelector(
    selectedTab: ReportTab,
    availableModules: List<SharePosterModule>,
    selectedModules: List<SharePosterModule>,
    onToggleModule: (SharePosterModule) -> Unit,
    onMoveModule: (SharePosterModule, Int) -> Unit,
) {
    val unselectedModules = availableModules.filterNot { it in selectedModules }
    val displayedModules = selectedModules + unselectedModules
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = AppText.t("stats_share_modules"),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            displayedModules.forEach { module ->
                key(module) {
                    if (module in selectedModules) {
                        DraggableShareModuleButton(
                            selectedTab = selectedTab,
                            module = module,
                            canRemove = selectedModules.size > 1,
                            onToggleModule = onToggleModule,
                            onMoveModule = onMoveModule,
                        )
                    } else {
                        ShareModuleTag(
                            selectedTab = selectedTab,
                            module = module,
                            selected = false,
                            canToggle = true,
                            onClick = { onToggleModule(module) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableShareModuleButton(
    selectedTab: ReportTab,
    module: SharePosterModule,
    canRemove: Boolean,
    onToggleModule: (SharePosterModule) -> Unit,
    onMoveModule: (SharePosterModule, Int) -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 44.dp.toPx() }
    var dragOffset by remember(module) { mutableFloatStateOf(0f) }
    ShareModuleTag(
        selectedTab = selectedTab,
        module = module,
        selected = true,
        canToggle = canRemove,
        onClick = { if (canRemove) onToggleModule(module) },
        modifier =
            Modifier
                .graphicsLayer { translationX = dragOffset }
                .zIndex(if (dragOffset != 0f) 1f else 0f)
                .pointerInput(module) {
                    detectDragGesturesAfterLongPress(
                        onDragEnd = { dragOffset = 0f },
                        onDragCancel = { dragOffset = 0f },
                    ) { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount.x
                        while (dragOffset >= thresholdPx) {
                            onMoveModule(module, 1)
                            dragOffset -= thresholdPx
                        }
                        while (dragOffset <= -thresholdPx) {
                            onMoveModule(module, -1)
                            dragOffset += thresholdPx
                        }
                    }
                },
    )
}

@Composable
private fun ShareModuleTag(
    selectedTab: ReportTab,
    module: SharePosterModule,
    selected: Boolean,
    canToggle: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    val color =
        if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.82f)
        }
    val contentColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    val borderColor =
        if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.36f)
        }
    Surface(
        modifier =
            modifier
                .clip(shape)
                .clickable(enabled = canToggle, onClick = onClick),
        shape = shape,
        color = color,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = AppText.t(module.labelKey(selectedTab)),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun DailyModeSummaryCard(
    summary: DailyModeSummary,
    icon: ImageVector,
    compact: Boolean = false,
    useHomeMetricCardStyle: Boolean = false,
    animateValues: Boolean = false,
    replayToken: Int = 0,
    modifier: Modifier = Modifier,
) {
    val reportColors = LocalReportColors.current
    val themeColors = LocalThemeColors.current
    val accent =
        when {
            summary.title == AppText.t("stats_control_results") -> themeColors.control
            summary.title == AppText.t("stats_encourage_progress") -> themeColors.encourage
            summary.isWarning -> reportColors.warning
            else -> MaterialTheme.colorScheme.primary
        }
    val metricContainerColor =
        if (useHomeMetricCardStyle) {
            when {
                summary.title == AppText.t("stats_control_results") -> themeColors.controlContainer.copy(alpha = 0.28f)
                summary.title == AppText.t("stats_encourage_progress") -> themeColors.encourageContainer.copy(alpha = 0.28f)
                else -> accent.copy(alpha = 0.28f)
            }
        } else {
            accent.copy(alpha = 0.1f)
        }
    val metricBorderColor = accent.takeIf { useHomeMetricCardStyle }
    val primaryValue =
        if (animateValues) {
            animateMetricDisplayText(
                rawText = summary.primaryValue,
                label = "daily_focus_${summary.title}_${summary.primaryLabel}",
                delayMillis = 120,
            )
        } else {
            summary.primaryValue
        }
    var showAllGroups by remember(summary.title, summary.groupItems.size) { mutableStateOf(false) }
    val visibleGroupItems =
        if (showAllGroups || summary.groupItems.size <= 4) {
            summary.groupItems
        } else {
            summary.groupItems.take(4)
        }
    TinyVowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
        borderAlpha = 0.28f,
        shadowElevation = TinyVowElevation.Card,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) TinyVowSpacing.CompactCardHorizontal else TinyVowSpacing.CardHorizontal,
                vertical = if (compact) TinyVowSpacing.CompactCardVertical else TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else TinyVowSpacing.CardGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
            ) {
                Surface(
                    modifier = Modifier.size(if (compact) 32.dp else 36.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = accent.copy(alpha = 0.16f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(if (compact) 17.dp else 19.dp),
                        )
                    }
                }
                Text(
                    text = summary.title,
                    modifier = Modifier.weight(1f),
                    style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = summary.primaryLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = primaryValue,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val metricSize = ((maxWidth - 10.dp) / 2).coerceIn(
                    if (compact) 58.dp else 64.dp,
                    if (compact) 74.dp else 82.dp,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                    ) {
                        FocusProgressRing(
                            progress = summary.progress,
                            color = accent,
                            label = "${(summary.progress * 100f).roundToInt()}%",
                            animateValue = animateValues,
                            replayToken = replayToken,
                            modifier = Modifier.size(metricSize),
                        )
                        summary.metrics.getOrNull(0)?.let { metric ->
                            FocusMetricPill(
                                metric = metric,
                                accent = accent,
                                delayMillis = 180,
                                modifier = Modifier.size(metricSize),
                                emphasizeValue = true,
                                valueColor = accent,
                                containerColor = metricContainerColor,
                                borderColor = metricBorderColor,
                                animateValue = animateValues,
                            )
                        } ?: Spacer(modifier = Modifier.size(metricSize))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                    ) {
                        summary.metrics.drop(1).take(2).forEachIndexed { index, metric ->
                            FocusMetricPill(
                                metric = metric,
                                accent = accent,
                                delayMillis = 220 + index * 40,
                                modifier = Modifier.size(metricSize),
                                emphasizeValue = true,
                                valueColor = accent,
                                containerColor = metricContainerColor,
                                borderColor = metricBorderColor,
                                animateValue = animateValues,
                            )
                        }
                        repeat((2 - summary.metrics.drop(1).take(2).size).coerceAtLeast(0)) {
                            Spacer(modifier = Modifier.size(metricSize))
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = summary.spotlightLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = summary.spotlightValue,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (summary.groupItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (summary.title == AppText.t("stats_encourage_progress")) {
                                AppText.t("stats_encourage_group_details")
                            } else {
                                AppText.t("stats_group_details")
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = AppText.t("stats_value_groups_short", summary.groupItems.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    visibleGroupItems.forEach { item ->
                        DailyGroupProgressRow(
                            item = item,
                            accent = accent,
                            animateValues = animateValues,
                        )
                    }
                    if (summary.groupItems.size > 4) {
                        TextButton(
                            onClick = { showAllGroups = !showAllGroups },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text =
                                    if (showAllGroups) {
                                        AppText.t("stats_collapse_groups")
                                    } else {
                                        AppText.t("stats_show_all_groups", summary.groupItems.size)
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyGroupProgressRow(
    item: DailyGroupProgressItem,
    accent: Color,
    animateValues: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val resultColor = if (item.isMuted && !item.isWarning) accent.copy(alpha = 0.72f) else accent
    val quietColor = if (item.isMuted) themeColors.inkFaint else themeColors.inkMuted
    val progressFillColor = accent
    val displayProgress =
        if (animateValues) {
            animateFractionValue(
                targetValue = item.progress.coerceIn(0f, 1f),
                label = "daily_group_progress_${item.groupName}_${item.statusLabel}",
                delayMillis = 180,
            )
        } else {
            item.progress.coerceIn(0f, 1f)
        }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (item.isMuted) 0.46f else 0.76f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.groupName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (animateValues) {
                    AnimatedMetricText(
                        rawText = "${item.trailingLabel} ${item.trailingValue}",
                        label = "daily_group_trailing_${item.groupName}_${item.trailingValue}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = resultColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        delayMillis = 260,
                    )
                } else {
                    Text(
                        text = "${item.trailingLabel} ${item.trailingValue}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = resultColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (animateValues) {
                    AnimatedMetricText(
                        rawText = item.leadingValue,
                        label = "daily_group_leading_${item.groupName}_${item.leadingValue}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = quietColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        delayMillis = 220,
                    )
                } else {
                    Text(
                        text = item.leadingValue,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = quietColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = item.statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = quietColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(themeColors.inkFaint.copy(alpha = 0.18f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = displayProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(progressFillColor.copy(alpha = if (item.isMuted && !item.isWarning) 0.42f else 0.78f))
                )
            }
        }
    }
}

@Composable
private fun GroupProgressMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        )
    }
}

@Composable
private fun FocusProgressRing(
    progress: Float,
    color: Color,
    label: String,
    animateValue: Boolean = false,
    replayToken: Int = 0,
    modifier: Modifier = Modifier,
) {
    val targetProgress = progress.coerceIn(0f, 1f)
    val displayProgress =
        if (STAT_CHART_ANIMATIONS_ENABLED && (animateValue || replayToken > 0)) {
            animateReplayFractionValue(
                targetValue = targetProgress,
                replayKey = replayToken,
                delayMillis = if (animateValue) 160 else 0,
            )
        } else {
            targetProgress
        }
    val displayLabel =
        if (animateValue && STAT_CHART_ANIMATIONS_ENABLED) {
            animateMetricDisplayText(
                rawText = label,
                label = "focus_progress_ring_$label",
                delayMillis = 120,
            )
        } else {
            label
        }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.14f
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val chartSize = Size(diameter, diameter)
            drawArc(
                color = color.copy(alpha = 0.14f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * displayProgress,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            )
        }
        Text(
            text = displayLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun FocusMetricPill(
    metric: DailyFocusMetric,
    accent: Color,
    delayMillis: Int,
    modifier: Modifier = Modifier,
    emphasizeValue: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = accent.copy(alpha = 0.1f),
    borderColor: Color? = null,
    animateValue: Boolean = false,
) {
    val themeColors = LocalThemeColors.current
    val labelStyle =
        MaterialTheme.typography.labelSmall.copy(
            fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.2f,
        )
    val displayValue =
        if (animateValue && STAT_CHART_ANIMATIONS_ENABLED) {
            animateMetricDisplayText(
                rawText = metric.value,
                label = "daily_focus_metric_${metric.label}_${metric.value}",
                delayMillis = delayMillis,
            )
        } else {
            metric.value
        }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = borderColor?.let { BorderStroke(2.dp, it) },
    ) {
        if (emphasizeValue) {
            val valueParts = splitMetricValue(displayValue)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = metric.label,
                    style = labelStyle,
                    color = themeColors.inkFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = valueParts.number,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = valueColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (valueParts.unit.isNotBlank()) {
                        Text(
                            text = valueParts.unit,
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.inkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = metric.label,
                    style = labelStyle,
                    color = themeColors.inkFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class MetricValueParts(
    val number: String,
    val unit: String,
)

private fun splitMetricValue(value: String): MetricValueParts {
    val trimmed = value.trim()
    val match = Regex("""^([+\-]?\d+(?:[.,]\d+)?)(.*)$""").find(trimmed)
    return if (match != null) {
        MetricValueParts(
            number = match.groupValues[1],
            unit = match.groupValues[2].trim(),
        )
    } else {
        MetricValueParts(number = trimmed, unit = "")
    }
}

@Composable
private fun HeroMetricChip(
    icon: ImageVector,
    label: String,
    value: String,
    delayMillis: Int = when (icon) {
        Icons.Default.PhoneAndroid -> 80
        Icons.AutoMirrored.Filled.CompareArrows -> 120
        Icons.Default.TouchApp -> 160
        else -> 200
    },
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val animatedValue = animateMetricDisplayText(
        rawText = value,
        label = "hero_metric_${label.hashCode()}",
        delayMillis = delayMillis,
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = themeColors.inkMuted,
            )
            Text(
                text = animatedValue,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimelineCard(
    selectedTab: ReportTab,
    timelineState: SectionState<TimelineSectionData>,
) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.Timeline,
                title = if (selectedTab == ReportTab.DAY) AppText.t("stats_24_hour_distribution") else AppText.t("stats_archive_trend"),
            )
            when (timelineState) {
                SectionState.Loading -> SkeletonTimelineChart()
                SectionState.Empty -> DailyTimelineChart(emptyList())
                is SectionState.Ready -> DailyTimelineChart(
                    buckets = timelineState.data.buckets,
                    targetMillisPerBucket = timelineState.data.targetMillisPerBucket,
                    appLegend = timelineState.data.appLegend,
                )
            }
            TimelineFooter(
                labels =
                    buildTimelineFooterLabels(
                        selectedTab = selectedTab,
                        buckets = (timelineState as? SectionState.Ready)?.data?.buckets.orEmpty(),
                    ),
            )
            AdaptiveRowGrid(
                itemCount = if (selectedTab == ReportTab.DAY) 1 else 2,
                compactColumns = 1,
                expandedColumns = 2,
                horizontalSpacing = 14.dp,
                verticalSpacing = 14.dp,
            ) { modifier, index ->
                when (timelineState) {
                    SectionState.Loading -> {
                        SkeletonPeakPanel(modifier = modifier)
                    }
                    SectionState.Empty -> {
                        if (selectedTab == ReportTab.DAY) {
                            PeakMomentsCard(
                                selectedTab = selectedTab,
                                timelineState = null,
                                modifier = modifier,
                            )
                        } else if (index == 0) {
                            PeriodDistributionCard(
                                periodUsage = emptyList(),
                                modifier = modifier,
                            )
                        } else {
                            PeakMomentsCard(
                                selectedTab = selectedTab,
                                timelineState = null,
                                modifier = modifier,
                            )
                        }
                    }
                    is SectionState.Ready -> if (selectedTab == ReportTab.DAY) {
                        PeakMomentsCard(
                            selectedTab = selectedTab,
                            timelineState = timelineState.data,
                            modifier = modifier,
                        )
                    } else when (index) {
                        0 -> PeriodDistributionCard(
                            periodUsage = timelineState.data.periodUsage,
                            modifier = modifier,
                        )
                        else -> PeakMomentsCard(
                            selectedTab = selectedTab,
                            timelineState = timelineState.data,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DailyTimelineChart(
    buckets: List<DailyTimelineBucket>,
    targetMillisPerBucket: Long? = null,
    appLegend: List<DailyTimelineAppLegendItem> = emptyList(),
) {
    val deviceColor = MaterialTheme.colorScheme.primary
    val guideLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f)
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val visibleLegend = appLegend.filter { it.millis > 0L }
    val extractedAppColors =
        rememberAppChartColors(
            visibleLegend
                .filter { it.packageName != TIMELINE_OTHER_APPS_PACKAGE_NAME }
                .map { it.packageName },
        )
    val fallbackColors = LocalReportColors.current.appChartPalette
    val otherAppsColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
    val legendColors =
        visibleLegend.mapIndexed { index, item ->
            item.packageName to
                when (item.packageName) {
                    TIMELINE_OTHER_APPS_PACKAGE_NAME -> otherAppsColor
                    else -> extractedAppColors[item.packageName] ?: stableAppFallbackColor(item.packageName, fallbackColors)
                }
        }.toMap()
    val revealProgress = if (buckets.any { it.deviceMillis > 0L }) 1f else 0f
    BoxWithConstraints {
        val chartHeight = if (maxWidth < 360.dp) 138.dp else 156.dp
        val axisWidth = if (maxWidth < 360.dp) 32.dp else 40.dp
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .width(axisWidth)
                        .height(chartHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    val maxUsage = maxOf(
                        buckets.maxOfOrNull { it.deviceMillis }?.coerceAtLeast(1L) ?: 1L,
                        targetMillisPerBucket ?: 0L,
                    )
                    listOf(maxUsage, maxUsage * 2 / 3, maxUsage / 3, 0L).forEach { tick ->
                        Text(
                            text = if (tick == 0L) "0" else formatAxisDuration(tick),
                            style = MaterialTheme.typography.labelSmall,
                            color = axisTextColor,
                            maxLines = 1,
                        )
                    }
                }
                val targetLineColor = LocalReportColors.current.warning.copy(alpha = 0.78f)
                Canvas(modifier = Modifier.weight(1f).height(chartHeight)) {
                    if (buckets.isEmpty()) return@Canvas
                    val deviceMax = buckets.maxOfOrNull { it.deviceMillis }?.coerceAtLeast(1L) ?: 1L
                    val chartMax = maxOf(deviceMax, targetMillisPerBucket ?: 0L, 1L)
                    val slotWidth = size.width / buckets.size
                    val barWidth = slotWidth * 0.48f
                    val baseY = size.height

                    repeat(4) { index ->
                        val y = baseY - (index * (size.height / 3f))
                        drawLine(
                            color = guideLineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                        )
                    }

                    buckets.forEachIndexed { index, bucket ->
                        val x = slotWidth * index + (slotWidth - barWidth) / 2f
                        val rawHeight = size.height * (bucket.deviceMillis.toFloat() / chartMax.toFloat()).coerceIn(0f, 1f)
                        val deviceHeight = if (bucket.deviceMillis > 0L) {
                            maxOf(6f * revealProgress, rawHeight * revealProgress)
                        } else {
                            0f
                        }
                        val top = size.height - deviceHeight
                        val stackedSegments = stackedTimelineSegments(bucket, visibleLegend)
                        if (stackedSegments.isEmpty()) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        deviceColor.copy(alpha = 0.62f),
                                        deviceColor.copy(alpha = if (bucket.deviceMillis > 0L) 0.92f else 0.14f),
                                    ),
                                    startY = top,
                                    endY = baseY,
                                ),
                                topLeft = Offset(x, top),
                                size = Size(barWidth, deviceHeight),
                                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                            )
                        } else {
                            var bottom = baseY
                            stackedSegments.forEach { segment ->
                                val segmentHeight =
                                    deviceHeight * (segment.millis.toFloat() / bucket.deviceMillis.toFloat()).coerceIn(0f, 1f)
                                if (segmentHeight > 0f) {
                                    val segmentTop = bottom - segmentHeight
                                    drawRoundRect(
                                        color = legendColors[segment.packageName] ?: deviceColor,
                                        topLeft = Offset(x, segmentTop),
                                        size = Size(barWidth, segmentHeight),
                                        cornerRadius = CornerRadius(barWidth / 2.8f, barWidth / 2.8f),
                                    )
                                    bottom = segmentTop
                                }
                            }
                        }
                    }
                    targetMillisPerBucket?.takeIf { it > 0L }?.let { target ->
                        val targetY = size.height - size.height * (target.toFloat() / chartMax.toFloat()).coerceIn(0f, 1f)
                        val dashWidth = slotWidth * 0.42f
                        var startX = 0f
                        while (startX < size.width) {
                            drawLine(
                                color = targetLineColor,
                                start = Offset(startX, targetY),
                                end = Offset(minOf(startX + dashWidth, size.width), targetY),
                                strokeWidth = 2f,
                            )
                            startX += dashWidth * 1.8f
                        }
                    }
                }
            }
            if (visibleLegend.isNotEmpty()) {
                TimelineAppLegend(
                    items = visibleLegend,
                    colors = legendColors,
                )
            }
        }
    }
}

private fun stackedTimelineSegments(
    bucket: DailyTimelineBucket,
    legend: List<DailyTimelineAppLegendItem>,
): List<DailyTimelineAppSegment> {
    if (bucket.deviceMillis <= 0L || bucket.appSegments.isEmpty() || legend.isEmpty()) {
        return emptyList()
    }
    val visiblePackages =
        legend
            .map { it.packageName }
            .filter { it != TIMELINE_OTHER_APPS_PACKAGE_NAME }
            .toSet()
    val segmentByPackage = bucket.appSegments.associateBy { it.packageName }
    val visibleSegments =
        legend
            .asSequence()
            .filter { it.packageName != TIMELINE_OTHER_APPS_PACKAGE_NAME }
            .mapNotNull { segmentByPackage[it.packageName] }
            .filter { it.millis > 0L }
            .toList()
    val otherMillis =
        bucket.appSegments
            .filterNot { it.packageName in visiblePackages }
            .sumOf { it.millis }
    return if (legend.any { it.packageName == TIMELINE_OTHER_APPS_PACKAGE_NAME } && otherMillis > 0L) {
        visibleSegments +
            DailyTimelineAppSegment(
                packageName = TIMELINE_OTHER_APPS_PACKAGE_NAME,
                label = AppText.t("stats_other_apps"),
                millis = otherMillis,
            )
    } else {
        visibleSegments
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimelineAppLegend(
    items: List<DailyTimelineAppLegendItem>,
    colors: Map<String, Color>,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors[item.packageName] ?: MaterialTheme.colorScheme.primary),
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(72.dp),
                )
                Text(
                    text = formatDuration(item.millis),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors[item.packageName] ?: MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
internal fun TimelineFooter(labels: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun buildTimelineFooterLabels(
    selectedTab: ReportTab,
    buckets: List<DailyTimelineBucket>,
): List<String> {
    if (selectedTab == ReportTab.DAY || buckets.isEmpty()) {
        return listOf("00:00", "06:00", "12:00", "18:00", "24:00")
    }
    val candidateIndexes =
        listOf(
            0,
            buckets.lastIndex / 3,
            (buckets.lastIndex * 2) / 3,
            buckets.lastIndex,
        ).distinct()
    return candidateIndexes.map { buckets[it].label }
}

@Composable
private fun PeriodDistributionCard(
    periodUsage: List<PeriodUsageStat>,
    modifier: Modifier = Modifier,
) {
    val total = periodUsage.sumOf { it.deviceMillis }.coerceAtLeast(1L)
    val dominantIndex = periodUsage.indexOfFirst { it.deviceMillis == (periodUsage.maxOfOrNull { item -> item.deviceMillis } ?: 0L) }
    val dominantItem = dominantIndex.takeIf { it >= 0 }?.let { periodUsage[it] }
    val reportColors = LocalReportColors.current
    val colors = reportColors.periodPalette
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.Card),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
    ) {
        BoxWithConstraints {
            val donutSize = if (maxWidth < 360.dp) 148.dp else 170.dp
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = AppText.t("stats_time_heatmap"),
                    style = MaterialTheme.typography.titleMedium,
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PeriodDonutChart(
                            values = periodUsage.map { it.deviceMillis },
                            colors = colors,
                            highlightedIndex = dominantIndex.takeIf { it >= 0 },
                            delayMillis = 200,
                            modifier = Modifier.size(donutSize),
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = dominantItem?.label ?: "--",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = dominantItem?.let { formatDuration(it.deviceMillis) } ?: "--",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                periodUsage.forEachIndexed { index, item ->
                    val share = item.deviceMillis.toFloat() / total.toFloat()
                    PeriodLegendRow(
                        label = item.label,
                        value = formatDuration(item.deviceMillis),
                        share = share,
                        color = colors[index % colors.size],
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodDonutChart(
    values: List<Long>,
    colors: List<Color>,
    highlightedIndex: Int? = null,
    delayMillis: Int = 0,
    animateValue: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val total = values.sum().coerceAtLeast(1L)
    val targetProgress = if (values.any { it > 0L }) 1f else 0f
    val revealProgress =
        if (animateValue && STAT_CHART_ANIMATIONS_ENABLED) {
            val stagedRevealTarget = rememberDelayedFloatTarget(
                targetValue = targetProgress,
                delayMillis = delayMillis,
            )
            animateFloatAsState(
                targetValue = stagedRevealTarget,
                animationSpec = spring(dampingRatio = 0.92f, stiffness = 160f),
                label = "donut_reveal_progress",
            ).value
        } else {
            targetProgress
        }
    val rotationProgress =
        if (animateValue && STAT_CHART_ANIMATIONS_ENABLED) {
            val stagedRevealTarget = rememberDelayedFloatTarget(
                targetValue = targetProgress,
                delayMillis = delayMillis,
            )
            animateFloatAsState(
                targetValue = stagedRevealTarget,
                animationSpec = tween(durationMillis = 920),
                label = "donut_rotation_progress",
            ).value
        } else {
            targetProgress
        }
    Canvas(modifier = modifier) {
        val baseStroke = size.minDimension * 0.13f
        val diameter = size.minDimension - baseStroke
        var startAngle = -90f - (1f - rotationProgress) * 360f
        values.forEachIndexed { index, value ->
            val sweep = 360f * (value.toFloat() / total.toFloat()) * revealProgress
            val isHighlighted = highlightedIndex == index && value > 0L
            val stroke = if (isHighlighted) baseStroke * 1.18f else baseStroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val chartSize = Size(diameter, diameter)

            if (isHighlighted) {
                drawArc(
                    color = colors[index % colors.size].copy(alpha = 0.16f),
                    startAngle = startAngle - 2f,
                    sweepAngle = sweep + 4f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartSize,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke + 10f),
                )
            }
            drawArc(
                color = colors[index % colors.size].copy(alpha = if (value > 0L) 0.92f else 0.12f),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun PeriodLegendRow(
    label: String,
    value: String,
    share: Float,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            modifier = Modifier.width(36.dp),
            style = MaterialTheme.typography.labelMedium,
        )
        LinearProgressIndicator(
            progress = { share.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.14f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun PeakMomentsCard(
    selectedTab: ReportTab,
    timelineState: TimelineSectionData?,
    modifier: Modifier = Modifier,
) {
    val behaviorInsight = timelineState
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = AppText.t("stats_peak_time"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (timelineState == null) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) AppText.t("stats_archived_day_not_enough_peak_samples") else AppText.t("stats_archive_window_not_enough_peak_samples"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AdaptiveRowGrid(
                    itemCount = 3,
                    compactColumns = 1,
                    expandedColumns = 1,
                    verticalSpacing = 8.dp,
                ) { modifier, index ->
                    when (index) {
                        0 -> MiniInsightCard(
                            icon = Icons.Default.Bolt,
                            label = AppText.t("stats_under_1h"),
                            value = "${behaviorInsight.peakHourLabel} · ${formatDuration(behaviorInsight.peakHourMillis)}",
                            modifier = modifier,
                        )
                        1 -> MiniInsightCard(
                            icon = Icons.AutoMirrored.Filled.CallSplit,
                            label = AppText.t("stats_over_2h"),
                            value = "${behaviorInsight.peakTwoHourLabel} · ${formatDuration(behaviorInsight.peakTwoHourMillis)}",
                            modifier = modifier,
                        )
                        else -> MiniInsightCard(
                            icon = Icons.Default.NightsStay,
                            label = AppText.t("stats_night"),
                            value = formatDuration(behaviorInsight.nightUsageMillis),
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopUsageBarRow(
    rank: Int,
    item: AppDisplayItem,
    maxUsage: Long,
    totalUsage: Long,
    color: Color,
) {
    val isTopRank = rank == 1
    val share = if (totalUsage > 0L) item.value.toFloat() / totalUsage.toFloat() else 0f
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = if (isTopRank) 0.08f else 0.05f),
        border = BorderStroke(
            1.dp,
            color.copy(alpha = if (isTopRank) 0.28f else 0.18f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                modifier = Modifier.width(26.dp),
                shape = RoundedCornerShape(999.dp),
                color = color.copy(alpha = if (isTopRank) 0.22f else 0.14f),
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = color,
                    )
                }
            }
            AppIconCircle(item.packageName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isTopRank) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                GradientProgressBar(
                    progress = (item.value.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f),
                    color = color,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = formatDuration(item.value),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${(share * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun GradientProgressBar(
    progress: Float,
    color: Color,
    delayMillis: Int = 0,
    animateValue: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val displayProgress =
        if (animateValue && STAT_CHART_ANIMATIONS_ENABLED) {
            val stagedProgress = rememberDelayedFloatTarget(
                targetValue = progress.coerceIn(0f, 1f),
                delayMillis = delayMillis,
            )
            animateFloatAsState(
                targetValue = stagedProgress,
                animationSpec = spring(dampingRatio = 0.92f, stiffness = 220f),
                label = "gradient_progress",
            ).value
        } else {
            progress.coerceIn(0f, 1f)
        }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(displayProgress)
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.5f),
                            color,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun AppChartsCard(
    selectedTab: ReportTab,
    topAppsState: SectionState<TopAppsSectionData>,
) {
    val usageTopApps = (topAppsState as? SectionState.Ready)?.data?.usageTopApps.orEmpty()
    val appColors = rememberAppChartColors(usageTopApps.map { it.packageName })
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.BarChart,
                title = AppText.t("stats_top_10_apps"),
                subtitle = if (selectedTab == ReportTab.DAY) AppText.t("stats_current_day_top_10_apps_only") else AppText.t("stats_shows_only_the_10_most_used_apps_in"),
            )
            if (topAppsState == SectionState.Loading) {
                SkeletonUsageSharePanel()
                SkeletonRankingPanel()
            } else if (topAppsState == SectionState.Empty || usageTopApps.isEmpty()) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) AppText.t("stats_this_archived_day_does_not_have_enough_usage") else AppText.t("stats_archive_window_not_enough_usage_records"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AppUsageShareCard(
                    items = usageTopApps,
                    appColors = appColors,
                )
            }
        }
    }
}

@Composable
internal fun AppUsageShareCard(
    items: List<AppDisplayItem>,
    appColors: Map<String, Color>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.Card),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
    ) {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            val donutSize = if (compact) 176.dp else 216.dp
            val visibleItems = items.take(6)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = AppText.t("stats_app_duration"),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (items.isEmpty()) {
                    Text(
                        text = AppText.t("stats_no_usage_records"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val total = items.sumOf { it.value }.coerceAtLeast(1L)
                    val otherUsage = items.drop(6).sumOf { it.value }
                    val donutValues =
                        if (otherUsage > 0L) {
                            visibleItems.map { it.value } + otherUsage
                        } else {
                            visibleItems.map { it.value }
                        }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            PeriodDonutChart(
                                values = donutValues,
                                colors =
                                    donutValues.mapIndexed { index, _ ->
                                        visibleItems.getOrNull(index)?.let { appColors[it.packageName] }
                                            ?: fallbackChartColor(index)
                                    },
                                highlightedIndex = 0,
                                delayMillis = 220,
                                modifier = Modifier.size(donutSize),
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = formatDuration(total),
                                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = AppText.t("stats_top_10_total_usage"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
                    Text(
                        text = AppText.t("stats_duration_ranking"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val maxUsage = items.maxOfOrNull { it.value }?.coerceAtLeast(1L) ?: 1L
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items.forEachIndexed { index, item ->
                            val color = appColors[item.packageName] ?: fallbackChartColor(index)
                            TopUsageBarRow(
                                rank = index + 1,
                                item = item,
                                maxUsage = maxUsage,
                                totalUsage = total,
                                color = color,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppShareChip(
    share: Float,
    packageName: String,
    color: Color,
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box {
                AppIconCircle(packageName)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
            Text(
                text = "${(share.coerceIn(0f, 1f) * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TopUsageRankingCard(
    items: List<AppDisplayItem>,
    appColors: Map<String, Color>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.Card),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = AppText.t("stats_duration_ranking"),
                style = MaterialTheme.typography.titleMedium,
            )
            val maxUsage = items.maxOfOrNull { it.value }?.coerceAtLeast(1L) ?: 1L
            val totalUsage = items.sumOf { it.value }.coerceAtLeast(1L)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEachIndexed { index, item ->
                    val color = appColors[item.packageName] ?: fallbackChartColor(index)
                    TopUsageBarRow(
                        rank = index + 1,
                        item = item,
                        maxUsage = maxUsage,
                        totalUsage = totalUsage,
                        color = color,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MiniInsightCard(
    icon: ImageVector,
    label: String,
    value: String,
    visualRatio: Float? = null,
    compact: Boolean = false,
    delayMillis: Int = when (icon) {
        Icons.Default.Schedule -> 460
        Icons.Default.AccessTime -> 500
        Icons.Default.TouchApp -> 540
        Icons.Default.RocketLaunch -> 580
        else -> 620
    },
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.56f),
    ) {
        if (compact) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (visualRatio != null) {
                    GradientProgressBar(
                        progress = visualRatio.coerceIn(0f, 1f),
                        color = accent,
                        delayMillis = delayMillis,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(text = value, style = MaterialTheme.typography.titleMedium)
                if (visualRatio != null) {
                    GradientProgressBar(
                        progress = visualRatio.coerceIn(0f, 1f),
                        color = accent,
                        delayMillis = delayMillis,
                    )
                }
            }
        }
    }
}

@Composable
private fun BehaviorCard(
    selectedTab: ReportTab,
    behaviorState: SectionState<BehaviorSectionData>,
) {
    val behaviorInsight = (behaviorState as? SectionState.Ready)?.data?.behaviorInsight
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.Default.Insights,
                title = AppText.t("stats_behavior_analysis"),
                subtitle = if (selectedTab == ReportTab.DAY) AppText.t("stats_archived_data_supported_metrics_only") else AppText.t("stats_only_insights_backed_by_stable_archived_data_are"),
            )
            if (behaviorState == SectionState.Loading) {
                AdaptiveRowGrid(
                    itemCount = 5,
                    compactColumns = 2,
                    expandedColumns = 2,
                ) { modifier, _ ->
                    SkeletonMetricChip(modifier = modifier)
                }
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 12.dp,
                    verticalSpacing = 12.dp,
                ) { modifier, _ ->
                    SkeletonBlock(
                        modifier = modifier,
                        height = 72.dp,
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            } else if (behaviorInsight == null) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) AppText.t("stats_this_archived_day_does_not_have_enough_behavior") else AppText.t("stats_the_current_window_does_not_have_enough_behavior"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val insight = behaviorInsight
                AdaptiveRowGrid(
                    itemCount = 5,
                    compactColumns = 2,
                    expandedColumns = 2,
                ) { modifier, index ->
                    when (index) {
                        0 -> MiniInsightCard(
                            icon = Icons.Default.Schedule,
                            label = AppText.t("stats_label_11"),
                            value = insight.longestSession?.let { session -> "${session.label} · ${formatDuration(session.value)}" } ?: AppText.t("stats_none"),
                            visualRatio = ((insight.longestSession?.value ?: 0L).toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        1 -> MiniInsightCard(
                            icon = Icons.Default.AccessTime,
                            label = AppText.t("stats_average_session"),
                            value = formatDuration(insight.averageSessionMillis),
                            visualRatio = (insight.averageSessionMillis.toFloat() / (30 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        2 -> MiniInsightCard(
                            icon = Icons.Default.Timeline,
                            label = if (selectedTab == ReportTab.DAY) AppText.t("stats_label_9") else AppText.t("stats_label_9"),
                            value = "${insight.peakHourLabel} · ${formatDuration(insight.peakHourMillis)}",
                            visualRatio = (insight.peakHourMillis.toFloat() / (2 * 60 * 60_000L).toFloat()).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        3 -> MiniInsightCard(
                            icon = Icons.Default.TouchApp,
                            label = AppText.t("stats_launch_intensity"),
                            value = String.format(Locale.CHINA, AppText.t("stats_launches_per_active_hour_format"), insight.reopenIntensity),
                            visualRatio = (insight.reopenIntensity / 6f).coerceIn(0f, 1f),
                            modifier = modifier,
                        )
                        4 -> BehaviorRingsCard(
                            activeHourCount = insight.activeHourCount,
                            nightUsageMillis = insight.nightUsageMillis,
                            peakHourMillis = insight.peakHourMillis,
                            modifier = modifier,
                        )
                        else -> Spacer(modifier = modifier)
                    }
                }
                AdaptiveRowGrid(
                    itemCount = 2,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 12.dp,
                    verticalSpacing = 12.dp,
                ) { modifier, index ->
                    when (index) {
                        0 -> BehaviorMomentCard(
                            icon = Icons.Default.NightsStay,
                            title = insight.beforeSleep.label,
                            appLabel = insight.beforeSleep.appLabel ?: AppText.t("stats_no_records_yet"),
                            packageName = insight.beforeSleep.packageName,
                            modifier = modifier,
                        )
                        else -> BehaviorMomentCard(
                            icon = Icons.Default.WbSunny,
                            title = insight.afterWake.label,
                            appLabel = insight.afterWake.appLabel ?: AppText.t("stats_no_records_yet"),
                            packageName = insight.afterWake.packageName,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BehaviorRingsCard(
    activeHourCount: Int,
    nightUsageMillis: Long,
    peakHourMillis: Long,
    modifier: Modifier = Modifier,
) {
    val reportColors = LocalReportColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.56f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompactMetricRing(
                progress = (activeHourCount / 24f).coerceIn(0f, 1f),
                value = "$activeHourCount",
                label = AppText.t("stats_hours"),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            CompactMetricRing(
                progress = if (peakHourMillis > 0L) (nightUsageMillis.toFloat() / (peakHourMillis * 6f)).coerceIn(0f, 1f) else 0f,
                value = formatDuration(nightUsageMillis),
                label = AppText.t("stats_night"),
                color = reportColors.warning,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CompactMetricRing(
    progress: Float,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FocusProgressRing(
            progress = progress,
            color = color,
            label = value,
            modifier = Modifier.size(58.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun BehaviorMomentCard(
    icon: ImageVector,
    title: String,
    appLabel: String,
    packageName: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CompactCardHorizontal,
                vertical = TinyVowSpacing.CompactCardVertical,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            if (packageName != null) {
                AppIconCircle(packageName)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = appLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ComparisonCard(
    selectedTab: ReportTab,
    comparisonState: SectionState<ComparisonSectionData>,
) {
    ReportCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(
                icon = Icons.AutoMirrored.Filled.CompareArrows,
                title = if (selectedTab == ReportTab.DAY) AppText.t("stats_archive_comparison") else AppText.t("stats_window_comparison"),
                subtitle = if (selectedTab == ReportTab.DAY) AppText.t("stats_compare_current_day_with_previous_archive") else AppText.t("stats_compare_stable_metrics_only"),
            )
            if (comparisonState == SectionState.Loading) {
                repeat(3) { index ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonLine(width = 72.dp, height = 12.dp)
                        SkeletonLine(width = 96.dp, height = 24.dp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonPill(width = 78.dp)
                            SkeletonPill(width = 84.dp)
                        }
                    }
                    if (index != 2) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }
                }
            } else if (comparisonState == SectionState.Empty) {
                Text(
                    text = if (selectedTab == ReportTab.DAY) AppText.t("stats_not_enough_earlier_archive_samples") else AppText.t("stats_this_window_does_not_have_enough_samples_yet"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val comparisons = (comparisonState as SectionState.Ready).data.comparisons
                AdaptiveRowGrid(
                    itemCount = comparisons.size,
                    compactColumns = 1,
                    expandedColumns = 2,
                    horizontalSpacing = 12.dp,
                    verticalSpacing = 12.dp,
                ) { modifier, index ->
                    val item = comparisons[index]
                    ComparisonRow(
                        item = item,
                        delayMillis = 660 + index * 50,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ComparisonRow(
    item: ComparisonMetric,
    delayMillis: Int = 0,
    averageBarLabel: String = AppText.t("stats_average"),
    showChips: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = item.label, style = MaterialTheme.typography.titleSmall)
                Text(text = item.todayValue, style = MaterialTheme.typography.headlineSmall)
            }
            item.chartData?.let { data ->
                ComparisonMiniBars(
                    data = data,
                    delayMillis = delayMillis,
                    averageLabel = averageBarLabel,
                )
            }
            if (showChips) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item.yesterdayDelta?.let {
                        ComparisonChip(text = it)
                    }
                    item.averageDelta?.let {
                        ComparisonChip(text = it)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonMiniBars(
    data: ComparisonChartData,
    delayMillis: Int,
    averageLabel: String = AppText.t("stats_average"),
) {
    val values = listOfNotNull(data.previousValue, data.averageValue, data.currentValue)
    val maxValue = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val reportColors = LocalReportColors.current
    val bars =
        listOf(
            Triple(AppText.t("stats_previous"), data.previousValue, data.previousLabel),
            Triple(averageLabel, data.averageValue, data.averageLabel),
            Triple(AppText.t("stats_current"), data.currentValue, data.currentLabel),
        )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        bars.forEachIndexed { index, (label, value, display) ->
            val color =
                when (index) {
                    0 -> MaterialTheme.colorScheme.outline
                    1 -> reportColors.warning
                    else -> MaterialTheme.colorScheme.primary
                }
            ComparisonBar(
                label = label,
                value = value,
                display = display,
                maxValue = maxValue,
                color = color,
                delayMillis = delayMillis + index * 40,
            )
        }
    }
}

@Composable
private fun ComparisonBar(
    label: String,
    value: Long?,
    display: String?,
    maxValue: Long,
    color: Color,
    delayMillis: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (value != null && maxValue > 0L) (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f) else 0f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.width(30.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(7.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.13f)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(maxOf(0.04f, progress))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (value == null) 0.12f else 0.72f)),
            )
        }
        Text(
            text = display ?: "--",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(58.dp),
        )
    }
}

@Composable
private fun ComparisonChip(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
internal fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: String? = null,
) {
    TinyVowSectionHeader(
        title = title,
        icon = icon,
        subtitle = subtitle,
        trailing = trailing?.let { label ->
            {
                TinyVowStatusPill(
                    text = label,
                    color = LocalThemeColors.current.base,
                    containerColor = LocalThemeColors.current.baseContainer,
                    leadingDot = false,
                )
            }
        },
    )
}

@Composable
internal fun ReportCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    TinyVowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
        borderAlpha = 0.28f,
        shadowElevation = TinyVowElevation.Card,
    ) {
        TinyVowCardContent(content = content)
    }
}

@Composable
fun AppIconCircle(
    pkg: String,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    iconPadding: Dp = 0.dp,
    showBorder: Boolean = true,
) {
    val context = LocalContext.current
    val icon = remember(pkg) {
        AppVisualCache.getIcon(context, pkg)
    }
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = if (showBorder) BorderStroke(1.dp, Color.White.copy(alpha = 0.95f)) else null,
    ) {
        if (icon != null) {
            AsyncImage(
                model = icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(iconPadding)
                    .clip(CircleShape)
                    .graphicsLayer {
                        scaleX = 1.08f
                        scaleY = 1.08f
                    },
            )
        }
    }
}

@Composable
internal fun AppIconStack(
    packages: List<String>,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
) {
    val visiblePackages = packages.take(3)
    if (visiblePackages.isEmpty()) return

    val offset = size / 2f
    val width = size + offset * (visiblePackages.size - 1).toFloat()
    Box(
        modifier = modifier
            .width(width)
            .height(size),
        contentAlignment = Alignment.CenterStart,
    ) {
        visiblePackages.forEachIndexed { index, packageName ->
            AppIconCircle(
                pkg = packageName,
                size = size,
                modifier = Modifier
                    .padding(start = offset * index.toFloat())
                    .zIndex((visiblePackages.size - index).toFloat()),
            )
        }
    }
}
