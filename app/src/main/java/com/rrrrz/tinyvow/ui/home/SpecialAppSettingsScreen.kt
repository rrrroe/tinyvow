package com.rrrrz.tinyvow.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.data.db.SpecialAppUsagePreference
import com.rrrrz.tinyvow.data.special.SpecialAppUsageRepository
import com.rrrrz.tinyvow.data.special.WeReadApiCheckResult
import com.rrrrz.tinyvow.data.special.WeReadApiException
import com.rrrrz.tinyvow.data.special.WeReadHistoryDay
import com.rrrrz.tinyvow.data.special.WeReadHistoryRefreshSummary
import com.rrrrz.tinyvow.data.special.WeReadSettingsState
import com.rrrrz.tinyvow.data.special.WeReadSyncSummary
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import java.text.DateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date
import kotlinx.coroutines.launch

private const val WEREAD_SKILLS_URL = "https://weread.qq.com/r/weread-skills"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialAppsScreen(
    onBack: () -> Unit,
    onOpenWeRead: () -> Unit,
    onOpenAppColors: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    val repository = remember(context) { SpecialAppUsageRepository(context) }
    var state by remember { mutableStateOf<WeReadSettingsState?>(null) }

    LaunchedEffect(Unit) {
        state = repository.buildSettingsState()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppText.t("special_app_list_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("group_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = TinyVowSpacing.PageHorizontal,
                    vertical = TinyVowSpacing.PageTop,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
        ) {
            TinyVowCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
                color = MaterialTheme.colorScheme.primaryContainer,
                borderAlpha = 0.18f,
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = AppText.t("special_app_list_description"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.ink.copy(alpha = 0.78f),
                    )
                }
            }

            SpecialAppListItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = AppText.t("special_app_weread_title"),
                subtitle = AppText.t("special_app_weread_list_subtitle"),
                status = if (state?.hasApiKey == true) {
                    AppText.t("special_app_status_configured")
                } else {
                    AppText.t("special_app_status_not_configured")
                },
                active = state?.hasApiKey == true,
                onClick = onOpenWeRead,
            )

            SpecialAppListItem(
                icon = Icons.Default.Settings,
                title = AppText.t("app_color_settings_title"),
                subtitle = AppText.t("app_color_settings_description"),
                status = AppText.t("app_color_settings_status_debug"),
                active = true,
                onClick = onOpenAppColors,
            )

            Text(
                text = AppText.t("special_app_list_future_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialAppSettingsScreen(
    onBack: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(context) { SpecialAppUsageRepository(context) }
    var state by remember { mutableStateOf<WeReadSettingsState?>(null) }
    var apiKeyInput by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var testResult by remember { mutableStateOf<WeReadApiCheckResult?>(null) }
    var syncSummary by remember { mutableStateOf<WeReadSyncSummary?>(null) }
    var targetDateInput by remember { mutableStateOf(LocalDate.now().toString()) }
    var historyMonth by remember { mutableStateOf(YearMonth.now()) }
    var historyDays by remember { mutableStateOf<List<WeReadHistoryDay>>(emptyList()) }
    var selectedHistoryDate by remember { mutableStateOf(LocalDate.now()) }
    var isHistoryBusy by remember { mutableStateOf(false) }
    var historyMessage by remember { mutableStateOf<String?>(null) }
    var showAdvanced by remember { mutableStateOf(false) }
    var showClearKeyDialog by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            state = repository.buildSettingsState()
        }
    }

    suspend fun refreshStateAndHistory() {
        state = repository.buildSettingsState()
        historyDays = repository.getWeReadHistoryMonth(historyMonth)
    }

    suspend fun savePendingApiKeyIfNeeded() {
        if (apiKeyInput.isNotBlank()) {
            repository.saveWeReadApiKey(apiKeyInput)
            apiKeyInput = ""
            state = repository.buildSettingsState()
        }
    }

    LaunchedEffect(Unit) {
        state = repository.buildSettingsState()
        if (state?.hasApiKey == true) {
            isBusy = true
            isHistoryBusy = true
            try {
                repository.syncWeReadNow().onSuccess { summary ->
                    syncSummary = summary
                    message =
                        if (summary.savedSnapshotCount > 0) {
                            AppText.t("special_app_sync_saved_buckets", summary.savedSnapshotCount)
                        } else {
                            AppText.t("special_app_sync_no_daily_buckets")
                        }
                }.onFailure {
                    message = formatWeReadError(it)
                }
                repository.syncMissingWeReadHistoryUpToYesterday().onSuccess {
                    historyMessage = formatHistorySummary(it)
                }.onFailure {
                    historyMessage = formatWeReadError(it)
                }
                state = repository.buildSettingsState()
            } finally {
                isBusy = false
                isHistoryBusy = false
            }
        }
        historyDays = repository.getWeReadHistoryMonth(historyMonth)
    }

    LaunchedEffect(historyMonth) {
        historyDays = repository.getWeReadHistoryMonth(historyMonth)
    }

    if (showClearKeyDialog) {
        AlertDialog(
            onDismissRequest = { showClearKeyDialog = false },
            title = { Text(AppText.t("special_app_clear_key_title")) },
            text = {
                Text(
                    text = AppText.t("special_app_clear_key_description"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isBusy = true
                            repository.clearWeReadApiKeyAndDisable()
                            apiKeyInput = ""
                            testResult = null
                            syncSummary = null
                            message = AppText.t("special_app_key_cleared")
                            refreshStateAndHistory()
                            isBusy = false
                            showClearKeyDialog = false
                        }
                    },
                    enabled = !isBusy,
                ) {
                    Text(AppText.t("special_app_clear_key"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearKeyDialog = false }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppText.t("special_app_weread_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("group_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = TinyVowSpacing.PageHorizontal,
                    vertical = TinyVowSpacing.PageTop,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
        ) {
            SpecialAppHero(state)

            SettingsCard(title = AppText.t("special_app_connection_section")) {
                Text(
                    text = AppText.t("special_app_weread_setup_intro"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = { openExternalUrl(context, WEREAD_SKILLS_URL) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppText.t("special_app_open_weread_skills"))
                }

                if (state?.hasApiKey == true) {
                    SavedKeyPanel(
                        preview = state?.apiKeyPreview.orEmpty(),
                        onClear = { showClearKeyDialog = true },
                    )
                } else {
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text(AppText.t("special_app_weread_api_key")) },
                        placeholder = { Text(AppText.t("special_app_api_key_placeholder")) },
                        supportingText = { Text(AppText.t("special_app_api_key_hint")) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                isBusy = true
                                repository.saveWeReadApiKey(apiKeyInput)
                                apiKeyInput = ""
                                testResult = null
                                syncSummary = null
                                message = AppText.t("special_app_key_saved_next_step")
                                refreshStateAndHistory()
                                isBusy = false
                            }
                        },
                        enabled = !isBusy && apiKeyInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(AppText.t("special_app_save_key"))
                    }
                }
            }

            SettingsCard(title = AppText.t("special_app_sync_section")) {
                Text(
                    text = AppText.t("special_app_sync_explanation"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isBusy = true
                                savePendingApiKeyIfNeeded()
                                testResult = repository.testWeReadApi(LocalDate.now()).fold(
                                    onSuccess = { result ->
                                        message = if (result.hasUsableDailyBuckets) {
                                            AppText.t("special_app_test_success")
                                        } else {
                                            AppText.t("special_app_test_no_daily_buckets")
                                        }
                                        result
                                    },
                                    onFailure = {
                                        message = formatWeReadError(it)
                                        null
                                    },
                                )
                                syncSummary = null
                                isBusy = false
                            }
                        },
                        enabled = !isBusy && state?.hasApiKey == true,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(AppText.t("special_app_test_connection"))
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                isBusy = true
                                savePendingApiKeyIfNeeded()
                                testResult = null
                                message = repository.syncWeReadNow().fold(
                                    onSuccess = { summary ->
                                        syncSummary = summary
                                        refreshStateAndHistory()
                                        if (summary.savedSnapshotCount > 0) {
                                            AppText.t("special_app_sync_saved_buckets", summary.savedSnapshotCount)
                                        } else {
                                            AppText.t("special_app_sync_no_daily_buckets")
                                        }
                                    },
                                    onFailure = { formatWeReadError(it) },
                                )
                                isBusy = false
                            }
                        },
                        enabled = !isBusy && state?.hasApiKey == true,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(AppText.t("special_app_sync_now"))
                    }
                }
                BusyAndMessage(isBusy = isBusy, message = message)
                syncSummary?.let { summary ->
                    SummaryStrip(
                        firstLabel = AppText.t("special_app_sync_returned_buckets"),
                        firstValue = summary.dailyBucketCount.toString(),
                        secondLabel = AppText.t("special_app_sync_saved_snapshot_count"),
                        secondValue = summary.savedSnapshotCount.toString(),
                    )
                }
            }

            SettingsCard(title = AppText.t("special_app_replacement_section")) {
                Text(
                    text = AppText.t("special_app_replacement_intro"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ReplacementStatusPanel(state)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PreferenceButton(
                        selected = state?.config?.usagePreference == SpecialAppUsagePreference.READING_FIRST,
                        text = AppText.t("special_app_prefer_reading"),
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                repository.updateWeReadUsagePreference(SpecialAppUsagePreference.READING_FIRST)
                                state = repository.buildSettingsState()
                            }
                        },
                    )
                    PreferenceButton(
                        selected = state?.config?.usagePreference == SpecialAppUsagePreference.PHONE_FIRST,
                        text = AppText.t("special_app_prefer_phone"),
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                repository.updateWeReadUsagePreference(SpecialAppUsagePreference.PHONE_FIRST)
                                state = repository.buildSettingsState()
                            }
                        },
                    )
                }
                SwitchRow(
                    title = AppText.t("special_app_control_switch"),
                    subtitle = AppText.t("special_app_control_switch_description"),
                    checked = state?.config?.enabledForControl == true,
                    enabled = !isBusy && state?.hasApiKey == true,
                    onCheckedChange = { checked ->
                        state?.config?.let { config ->
                            scope.launch {
                                repository.updateWeReadConfig(
                                    enabledForControl = checked,
                                    enabledForEncourage = config.enabledForEncourage,
                                    syncEnabled = checked || config.enabledForEncourage,
                                )
                                refresh()
                            }
                        }
                    },
                )
                SwitchRow(
                    title = AppText.t("special_app_encourage_switch"),
                    subtitle = AppText.t("special_app_encourage_switch_description"),
                    checked = state?.config?.enabledForEncourage == true,
                    enabled = !isBusy && state?.hasApiKey == true,
                    onCheckedChange = { checked ->
                        state?.config?.let { config ->
                            scope.launch {
                                repository.updateWeReadConfig(
                                    enabledForControl = config.enabledForControl,
                                    enabledForEncourage = checked,
                                    syncEnabled = config.enabledForControl || checked,
                                )
                                refresh()
                            }
                        }
                    },
                )
                if (state?.hasApiKey != true) {
                    Text(
                        text = AppText.t("special_app_switches_need_key"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SettingsCard(title = AppText.t("special_app_cached_data_section")) {
                SummaryStrip(
                    firstLabel = AppText.t("special_app_today"),
                    firstValue = formatSpecialAppDuration(state?.todayUsageMillis ?: 0L),
                    secondLabel = AppText.t("special_app_last_7_days"),
                    secondValue = formatSpecialAppDuration(state?.weekUsageMillis ?: 0L),
                )
                MetricRow(AppText.t("special_app_this_month"), formatSpecialAppDuration(state?.monthUsageMillis ?: 0L))
                MetricRow(AppText.t("special_app_reading_today"), formatSpecialAppDuration(state?.todayReadingMillis ?: 0L))
                MetricRow(AppText.t("special_app_phone_today"), formatSpecialAppDuration(state?.todayPhoneUsageMillis ?: 0L))
                state?.config?.lastSuccessAt?.takeIf { it > 0L }?.let {
                    MetricRow(AppText.t("special_app_last_success"), DateFormat.getDateTimeInstance().format(Date(it)))
                }
                state?.config?.lastSyncAt?.takeIf { it > 0L }?.let {
                    MetricRow(AppText.t("special_app_last_sync"), DateFormat.getDateTimeInstance().format(Date(it)))
                }
                state?.config?.lastError?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = AppText.t("special_app_last_error", formatStoredWeReadError(it)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            SettingsCard(title = AppText.t("special_app_advanced_tools")) {
                Text(
                    text = AppText.t("special_app_advanced_tools_description"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { showAdvanced = !showAdvanced }) {
                    Text(
                        if (showAdvanced) {
                            AppText.t("special_app_hide_advanced")
                        } else {
                            AppText.t("special_app_show_advanced")
                        }
                    )
                }
                if (showAdvanced) {
                    AdvancedWeReadTools(
                        targetDateInput = targetDateInput,
                        onTargetDateInputChange = { targetDateInput = it },
                        isBusy = isBusy,
                        isHistoryBusy = isHistoryBusy,
                        historyMonth = historyMonth,
                        onHistoryMonthChange = { historyMonth = it },
                        historyDays = historyDays,
                        selectedHistoryDate = selectedHistoryDate,
                        onSelectedHistoryDateChange = { selectedHistoryDate = it },
                        historyMessage = historyMessage,
                        testResult = testResult,
                        syncSummary = syncSummary,
                        onTestTargetDate = {
                            scope.launch {
                                isBusy = true
                                savePendingApiKeyIfNeeded()
                                val targetDate = parseSpecialAppDate(targetDateInput)
                                if (targetDate == null) {
                                    message = AppText.t("special_app_invalid_date")
                                    testResult = null
                                    syncSummary = null
                                    isBusy = false
                                    return@launch
                                }
                                syncSummary = null
                                testResult = repository.testWeReadApi(targetDate).fold(
                                    onSuccess = { result ->
                                        message = if (result.hasUsableDailyBuckets) {
                                            AppText.t("special_app_test_success")
                                        } else {
                                            AppText.t("special_app_test_no_daily_buckets")
                                        }
                                        result
                                    },
                                    onFailure = {
                                        message = formatWeReadError(it)
                                        null
                                    },
                                )
                                isBusy = false
                            }
                        },
                        onRefreshMonth = {
                            scope.launch {
                                isHistoryBusy = true
                                historyMessage = repository.refreshWeReadHistoryMonth(historyMonth).fold(
                                    onSuccess = {
                                        state = repository.buildSettingsState()
                                        historyDays = repository.getWeReadHistoryMonth(historyMonth)
                                        formatHistorySummary(it)
                                    },
                                    onFailure = { formatWeReadError(it) },
                                )
                                isHistoryBusy = false
                            }
                        },
                        onRefreshSelectedDay = {
                            scope.launch {
                                isHistoryBusy = true
                                historyMessage = repository.refreshWeReadHistoryDate(selectedHistoryDate).fold(
                                    onSuccess = {
                                        state = repository.buildSettingsState()
                                        historyDays = repository.getWeReadHistoryMonth(historyMonth)
                                        formatHistorySummary(it)
                                    },
                                    onFailure = { formatWeReadError(it) },
                                )
                                isHistoryBusy = false
                            }
                        },
                        onRefreshPhoneMonth = {
                            scope.launch {
                                isHistoryBusy = true
                                val summary = repository.refreshPhoneHistoryMonth(historyMonth)
                                state = repository.buildSettingsState()
                                historyDays = repository.getWeReadHistoryMonth(historyMonth)
                                historyMessage = formatHistorySummary(summary)
                                isHistoryBusy = false
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecialAppListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    status: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    StatusPill(text = status, active = active)
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun SpecialAppHero(state: WeReadSettingsState?) {
    val connected = state?.hasApiKey == true && (state.config?.lastSuccessAt ?: 0L) > 0L
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        color = MaterialTheme.colorScheme.primaryContainer,
        borderAlpha = 0.18f,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppText.t("special_app_weread_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = AppText.t("special_app_weread_package_hint"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                    )
                }
                StatusPill(
                    text = if (connected) AppText.t("special_app_status_connected") else AppText.t("special_app_status_not_ready"),
                    active = connected,
                )
            }
            Text(
                text = AppText.t("special_app_settings_description"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
private fun ReplacementStatusPanel(state: WeReadSettingsState?) {
    val config = state?.config
    val status =
        buildSpecialAppReplacementStatus(
            hasApiKey = state?.hasApiKey == true,
            lastSuccessAt = config?.lastSuccessAt ?: 0L,
            enabledForControl = config?.enabledForControl == true,
            enabledForEncourage = config?.enabledForEncourage == true,
            syncEnabled = config?.syncEnabled == true,
            usagePreference = config?.usagePreference ?: SpecialAppUsagePreference.READING_FIRST,
        )
    val title =
        when (status.type) {
            SpecialAppReplacementStatusType.ACTIVE -> AppText.t("special_app_replacement_status_active")
            SpecialAppReplacementStatusType.NEEDS_KEY -> AppText.t("special_app_replacement_status_needs_key")
            SpecialAppReplacementStatusType.NEEDS_SYNC -> AppText.t("special_app_replacement_status_needs_sync")
            SpecialAppReplacementStatusType.NEEDS_SCOPE -> AppText.t("special_app_replacement_status_needs_scope")
            SpecialAppReplacementStatusType.INACTIVE -> AppText.t("special_app_replacement_status_inactive")
        }
    val description =
        when (status.type) {
            SpecialAppReplacementStatusType.ACTIVE -> AppText.t("special_app_replacement_status_active_description")
            SpecialAppReplacementStatusType.NEEDS_KEY -> AppText.t("special_app_replacement_status_needs_key_description")
            SpecialAppReplacementStatusType.NEEDS_SYNC -> AppText.t("special_app_replacement_status_needs_sync_description")
            SpecialAppReplacementStatusType.NEEDS_SCOPE -> AppText.t("special_app_replacement_status_needs_scope_description")
            SpecialAppReplacementStatusType.INACTIVE -> AppText.t("special_app_replacement_status_inactive_description")
        }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (status.active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.36f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (status.active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status.active) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                StatusPill(
                    text = if (status.active) AppText.t("special_app_status_connected") else AppText.t("special_app_status_not_ready"),
                    active = status.active,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.56f))
            MetricRow(
                AppText.t("special_app_replacement_status_api_key"),
                if (status.hasApiKey) AppText.t("special_app_api_key_saved") else AppText.t("special_app_api_key_missing"),
            )
            MetricRow(
                AppText.t("special_app_replacement_status_success_sync"),
                if (status.hasSuccessfulSync) AppText.t("special_app_yes") else AppText.t("special_app_no"),
            )
            MetricRow(
                AppText.t("special_app_replacement_status_scope"),
                replacementScopeLabel(status.controlEnabled, status.encourageEnabled),
            )
            MetricRow(
                AppText.t("special_app_replacement_status_preference"),
                if (status.usagePreference == SpecialAppUsagePreference.PHONE_FIRST) {
                    AppText.t("special_app_prefer_phone")
                } else {
                    AppText.t("special_app_prefer_reading")
                },
            )
        }
    }
}

private fun replacementScopeLabel(
    control: Boolean,
    encourage: Boolean,
): String =
    when {
        control && encourage -> AppText.t("special_app_replacement_scope_control_and_encourage")
        control -> AppText.t("special_app_replacement_scope_control_only")
        encourage -> AppText.t("special_app_replacement_scope_encourage_only")
        else -> AppText.t("special_app_replacement_scope_none")
    }

@Composable
private fun StatusPill(text: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SavedKeyPanel(
    preview: String,
    onClear: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricRow(AppText.t("special_app_saved_key"), preview.ifBlank { AppText.t("special_app_api_key_saved") })
            Text(
                text = AppText.t("special_app_saved_key_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onClear) {
                Text(AppText.t("special_app_clear_key"))
            }
        }
    }
}

@Composable
private fun BusyAndMessage(
    isBusy: Boolean,
    message: String?,
) {
    if (isBusy) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(
                text = AppText.t("special_app_request_running"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    message?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SummaryStrip(
    firstLabel: String,
    firstValue: String,
    secondLabel: String,
    secondValue: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryMetric(label = firstLabel, value = firstValue, modifier = Modifier.weight(1f))
            HorizontalDivider(
                modifier = Modifier
                    .height(38.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            SummaryMetric(label = secondLabel, value = secondValue, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AdvancedWeReadTools(
    targetDateInput: String,
    onTargetDateInputChange: (String) -> Unit,
    isBusy: Boolean,
    isHistoryBusy: Boolean,
    historyMonth: YearMonth,
    onHistoryMonthChange: (YearMonth) -> Unit,
    historyDays: List<WeReadHistoryDay>,
    selectedHistoryDate: LocalDate,
    onSelectedHistoryDateChange: (LocalDate) -> Unit,
    historyMessage: String?,
    testResult: WeReadApiCheckResult?,
    syncSummary: WeReadSyncSummary?,
    onTestTargetDate: () -> Unit,
    onRefreshMonth: () -> Unit,
    onRefreshSelectedDay: () -> Unit,
    onRefreshPhoneMonth: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = targetDateInput,
            onValueChange = onTargetDateInputChange,
            label = { Text(AppText.t("special_app_target_date")) },
            supportingText = { Text(AppText.t("special_app_target_date_hint")) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            onClick = onTestTargetDate,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(AppText.t("special_app_test_selected_date"))
        }
        testResult?.let { result ->
            DiagnosticCard {
                MetricRow(AppText.t("special_app_test_mode"), result.mode)
                MetricRow(AppText.t("special_app_test_total"), formatSpecialAppDuration(result.totalUsageMillis))
                MetricRow(AppText.t("special_app_test_bucket_count"), result.dailyBucketCount.toString())
                MetricRow(AppText.t("special_app_target_date"), result.targetDate.toString())
                MetricRow(
                    AppText.t("special_app_test_target_date_usage"),
                    result.targetDateUsageMillis?.let(::formatSpecialAppDuration) ?: AppText.t("special_app_unavailable"),
                )
                MetricRow(
                    AppText.t("special_app_test_target_date_bucket"),
                    if (result.hasTargetDateBucket) AppText.t("special_app_yes") else AppText.t("special_app_no"),
                )
                MetricRow(
                    AppText.t("special_app_test_replacement_ready"),
                    if (result.hasUsableDailyBuckets) AppText.t("special_app_yes") else AppText.t("special_app_no"),
                )
            }
        }
        syncSummary?.let { summary ->
            DiagnosticCard {
                MetricRow(AppText.t("special_app_sync_months_queried"), summary.monthsQueried.toString())
                MetricRow(AppText.t("special_app_sync_api_total"), formatSpecialAppDuration(summary.totalUsageMillis))
                MetricRow(AppText.t("special_app_sync_returned_buckets"), summary.dailyBucketCount.toString())
                MetricRow(AppText.t("special_app_sync_saved_snapshot_count"), summary.savedSnapshotCount.toString())
            }
        }
        HorizontalDivider()
        Text(
            text = AppText.t("special_app_history_calendar"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { onHistoryMonthChange(historyMonth.minusMonths(1)) },
                enabled = !isHistoryBusy,
                modifier = Modifier.weight(1f),
            ) {
                Text(AppText.t("special_app_previous_month"))
            }
            Text(
                text = historyMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedButton(
                onClick = { onHistoryMonthChange(historyMonth.plusMonths(1)) },
                enabled = !isHistoryBusy && historyMonth < YearMonth.now(),
                modifier = Modifier.weight(1f),
            ) {
                Text(AppText.t("stats_next_month"))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onRefreshMonth,
                enabled = !isHistoryBusy,
                modifier = Modifier.weight(1f),
            ) {
                Text(AppText.t("special_app_refresh_month"))
            }
            OutlinedButton(
                onClick = onRefreshSelectedDay,
                enabled = !isHistoryBusy,
                modifier = Modifier.weight(1f),
            ) {
                Text(AppText.t("special_app_refresh_selected_day"))
            }
        }
        OutlinedButton(
            onClick = onRefreshPhoneMonth,
            enabled = !isHistoryBusy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(AppText.t("special_app_refresh_phone_month"))
        }
        if (isHistoryBusy) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Text(
                    text = AppText.t("special_app_history_refreshing"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        historyMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        HistoryCalendar(
            month = historyMonth,
            days = historyDays,
            selectedDate = selectedHistoryDate,
            onSelectDate = onSelectedHistoryDateChange,
        )
        historyDays.firstOrNull { it.date == selectedHistoryDate }?.let { day ->
            DiagnosticCard {
                MetricRow(AppText.t("special_app_target_date"), day.date.toString())
                MetricRow(
                    AppText.t("special_app_test_target_date_usage"),
                    if (day.readingBucketAvailable) formatSpecialAppDuration(day.readingUsageMillis) else AppText.t("special_app_unavailable"),
                )
                MetricRow(AppText.t("special_app_phone_usage"), formatSpecialAppDuration(day.phoneUsageMillis))
                MetricRow(AppText.t("special_app_test_target_date_bucket"), if (day.readingBucketAvailable) AppText.t("special_app_yes") else AppText.t("special_app_no"))
                MetricRow(AppText.t("special_app_phone_history_available"), if (day.phoneCollectedAt > 0L) AppText.t("special_app_yes") else AppText.t("special_app_no"))
                day.readingSyncedAt.takeIf { it > 0L }?.let {
                    MetricRow(AppText.t("special_app_reading_synced_at"), DateFormat.getDateTimeInstance().format(Date(it)))
                }
                day.phoneCollectedAt.takeIf { it > 0L }?.let {
                    MetricRow(AppText.t("special_app_phone_collected_at"), DateFormat.getDateTimeInstance().format(Date(it)))
                }
            }
        }
        Text(
            text = AppText.t("special_app_calendar_legend"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider()
        Text(
            text = AppText.t("special_app_gateway_note"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = AppText.t("special_app_date_query_note"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = AppText.t("special_app_fallback_note"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PreferenceButton(
    selected: Boolean,
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TinyVowButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        selected = selected,
        modifier = modifier,
    )
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            content()
        }
    }
}

@Composable
private fun HistoryCalendar(
    month: YearMonth,
    days: List<WeReadHistoryDay>,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
) {
    val dayMap = days.associateBy { it.date }
    val firstDay = month.atDay(1)
    val leadingBlankCount = firstDay.dayOfWeek.value - 1
    val calendarDays = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val rows = (List(leadingBlankCount) { null } + calendarDays).chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        rows.forEach { rowDays ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                rowDays.forEach { date ->
                    if (date == null) {
                        Spacer(modifier = Modifier.weight(1f).height(54.dp))
                    } else {
                        val day = dayMap[date]
                        val selected = date == selectedDate
                        val marker = buildString {
                            if (day?.readingBucketAvailable == true) append(AppText.t("special_app_calendar_reading_marker"))
                            if ((day?.phoneCollectedAt ?: 0L) > 0L) append(AppText.t("special_app_calendar_phone_marker"))
                            if (isEmpty()) append(" ")
                        }
                        if (selected) {
                            Button(
                                onClick = { onSelectDate(date) },
                                modifier = Modifier.weight(1f).height(54.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp),
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium)
                                    Text(marker, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectDate(date) },
                                modifier = Modifier.weight(1f).height(54.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp),
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium)
                                    Text(marker, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                repeat((7 - rowDays.size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f).height(54.dp))
                }
            }
        }
    }
}

@Composable
private fun DiagnosticCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inkStrong,
        )
    }
}

private fun openExternalUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

private fun formatSpecialAppDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return when {
        hours > 0L && minutes > 0L -> AppText.t("duration_value_h_value_min", hours, minutes)
        hours > 0L -> AppText.t("duration_value_h", hours)
        else -> AppText.t("duration_value_min", minutes)
    }
}

private fun formatWeReadError(error: Throwable): String {
    val apiError = error as? WeReadApiException
    if (apiError?.httpCode == 401) {
        return AppText.t("special_app_error_unauthorized")
    }
    return error.message ?: AppText.t("special_app_test_failed")
}

private fun formatStoredWeReadError(error: String): String =
    if (error.startsWith("HTTP 401")) {
        AppText.t("special_app_error_unauthorized")
    } else {
        error
    }

private fun parseSpecialAppDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.trim()) }.getOrNull()

private fun formatHistorySummary(summary: WeReadHistoryRefreshSummary): String =
    AppText.t(
        "special_app_history_refresh_summary",
        summary.datesUpdated,
        summary.readingBucketCount,
        summary.phoneUsageUpdatedCount,
    )
