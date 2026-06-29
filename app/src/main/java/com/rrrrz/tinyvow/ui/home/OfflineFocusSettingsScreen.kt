package com.rrrrz.tinyvow.ui.home

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.OfflineFocusMode
import com.rrrrz.tinyvow.data.repository.OfflineFocusCategory
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private enum class OfflineFocusSettingsPage {
    MAIN,
    DEFAULTS,
    WHITELIST,
    CATEGORIES,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun OfflineFocusSettingsScreen(
    categories: List<OfflineFocusCategory>,
    installedApps: List<ManagedApp>,
    enabled: Boolean,
    defaultCategoryId: String?,
    defaultDurationMinutes: Int,
    defaultMode: OfflineFocusMode,
    whitelistPackages: Set<String>,
    continueOnLock: Boolean,
    dailyPointCap: Int,
    onBack: () -> Unit,
    onSetEnabled: (Boolean) -> Unit,
    onSelectDefaultCategory: (String) -> Unit,
    onSelectDefaultDuration: (Int) -> Unit,
    onSelectDefaultMode: (OfflineFocusMode) -> Unit,
    onSetWhitelistPackages: (Set<String>) -> Unit,
    onSetContinueOnLock: (Boolean) -> Unit,
    onSetDailyPointCap: (Int) -> Unit,
    onUpsertCategory: (String?, String, String, String?, Int, Double) -> Unit,
    onImportCategoryIcon: (String, Uri) -> Unit,
    onMoveCategory: (String, Int) -> Unit,
    onSetCategoryArchived: (String, Boolean) -> Unit,
    onDeleteCategory: (String) -> Unit,
) {
    var capDraft by remember(dailyPointCap) { mutableStateOf(dailyPointCap.toString()) }
    var editingCategoryId by remember { mutableStateOf<String?>(null) }
    var showNewCategoryEditor by remember { mutableStateOf(false) }
    var showWhitelistAppDialog by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(OfflineFocusSettingsPage.MAIN) }
    var pendingIconCategoryId by remember { mutableStateOf<String?>(null) }
    val iconPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val categoryId = pendingIconCategoryId
            pendingIconCategoryId = null
            if (categoryId != null && uri != null) {
                onImportCategoryIcon(categoryId, uri)
            }
        }
    val activeCategories = categories.filterNot { it.isArchived }
    val editingCategory = categories.firstOrNull { it.id == editingCategoryId }

    BackHandler(
        enabled =
            currentPage != OfflineFocusSettingsPage.MAIN &&
                !showNewCategoryEditor &&
                editingCategoryId == null &&
                !showWhitelistAppDialog,
    ) {
        currentPage = OfflineFocusSettingsPage.MAIN
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentPage) {
                            OfflineFocusSettingsPage.MAIN -> AppText.t("offline_focus_settings_title")
                            OfflineFocusSettingsPage.DEFAULTS -> AppText.t("offline_focus_settings_defaults")
                            OfflineFocusSettingsPage.WHITELIST -> AppText.t("offline_focus_whitelist")
                            OfflineFocusSettingsPage.CATEGORIES -> AppText.t("offline_focus_settings_categories")
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentPage == OfflineFocusSettingsPage.MAIN) {
                                onBack()
                            } else {
                                currentPage = OfflineFocusSettingsPage.MAIN
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("group_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = TinyVowSpacing.PageHorizontal, vertical = TinyVowSpacing.PageTop),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
        ) {
            when (currentPage) {
                OfflineFocusSettingsPage.MAIN -> {
                    FocusSettingsMainPage(
                        enabled = enabled,
                        continueOnLock = continueOnLock,
                        capDraft = capDraft,
                        onCapDraftChange = { capDraft = it },
                        onSetEnabled = onSetEnabled,
                        onSetContinueOnLock = onSetContinueOnLock,
                        onSetDailyPointCap = onSetDailyPointCap,
                        onOpenDefaults = { currentPage = OfflineFocusSettingsPage.DEFAULTS },
                        onOpenWhitelist = { currentPage = OfflineFocusSettingsPage.WHITELIST },
                        onOpenCategories = { currentPage = OfflineFocusSettingsPage.CATEGORIES },
                    )
                }
                OfflineFocusSettingsPage.DEFAULTS -> {
                    FocusDefaultsPage(
                        activeCategories = activeCategories,
                        defaultCategoryId = defaultCategoryId,
                        defaultDurationMinutes = defaultDurationMinutes,
                        defaultMode = defaultMode,
                        onSelectDefaultCategory = onSelectDefaultCategory,
                        onSelectDefaultDuration = onSelectDefaultDuration,
                        onSelectDefaultMode = onSelectDefaultMode,
                    )
                }
                OfflineFocusSettingsPage.WHITELIST -> {
                    FocusWhitelistPage(
                        installedApps = installedApps,
                        whitelistPackages = whitelistPackages,
                        onAddApp = { showWhitelistAppDialog = true },
                        onRemoveApp = { packageName -> onSetWhitelistPackages(whitelistPackages - packageName) },
                    )
                }
                OfflineFocusSettingsPage.CATEGORIES -> {
                    FocusCategoriesPage(
                        categories = categories,
                        editingCategoryId = editingCategoryId,
                        onAddCategory = {
                            showNewCategoryEditor = true
                            editingCategoryId = null
                        },
                        onEditCategory = { categoryId ->
                            showNewCategoryEditor = false
                            editingCategoryId = categoryId
                        },
                        onMoveCategory = onMoveCategory,
                        onSetCategoryArchived = onSetCategoryArchived,
                        onDeleteCategory = onDeleteCategory,
                    )
                }
            }
        }
    }

    if (showNewCategoryEditor) {
        OfflineFocusCategoryEditorDialog(
            category = null,
            onDismiss = { showNewCategoryEditor = false },
            onSave = { id, name, iconKey, customIconPath, colorArgb, pointsPerMinute ->
                onUpsertCategory(id, name, iconKey, customIconPath, colorArgb, pointsPerMinute)
                showNewCategoryEditor = false
            },
            onImportIcon = {},
            onDelete = {},
        )
    }
    editingCategory?.let { category ->
        OfflineFocusCategoryEditorDialog(
            category = category,
            onDismiss = { editingCategoryId = null },
            onSave = { id, name, iconKey, customIconPath, colorArgb, pointsPerMinute ->
                onUpsertCategory(id, name, iconKey, customIconPath, colorArgb, pointsPerMinute)
                editingCategoryId = null
            },
            onImportIcon = { categoryId ->
                pendingIconCategoryId = categoryId
                iconPicker.launch("image/*")
            },
            onDelete = { categoryId ->
                onDeleteCategory(categoryId)
                editingCategoryId = null
            },
        )
    }
    if (showWhitelistAppDialog) {
        FocusWhitelistAppDialog(
            apps = installedApps,
            excludedPackageNames = whitelistPackages,
            onDismiss = { showWhitelistAppDialog = false },
            onAdd = { app ->
                onSetWhitelistPackages(whitelistPackages + app.packageName)
                showWhitelistAppDialog = false
            },
        )
    }
}

@Composable
private fun FocusSettingsMainPage(
    enabled: Boolean,
    continueOnLock: Boolean,
    capDraft: String,
    onCapDraftChange: (String) -> Unit,
    onSetEnabled: (Boolean) -> Unit,
    onSetContinueOnLock: (Boolean) -> Unit,
    onSetDailyPointCap: (Int) -> Unit,
    onOpenDefaults: () -> Unit,
    onOpenWhitelist: () -> Unit,
    onOpenCategories: () -> Unit,
) {
    FocusEnableCard(
        title = AppText.t("offline_focus_enable"),
        body = AppText.t("offline_focus_enable_desc"),
        checked = enabled,
        onCheckedChange = onSetEnabled,
    )
    Column(
        modifier = Modifier.alpha(if (enabled) 1f else 0.52f),
        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
    ) {
        FocusSettingsCard(
            title = AppText.t("offline_focus_settings_rules"),
            body = AppText.t("offline_focus_settings_rules_desc"),
        ) {
            FocusSwitchRow(
                title = AppText.t("offline_focus_continue_on_lock"),
                body = AppText.t("offline_focus_continue_on_lock_desc"),
                checked = continueOnLock,
                onCheckedChange = onSetContinueOnLock,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
            OutlinedTextField(
                value = capDraft,
                onValueChange = { value -> onCapDraftChange(value.filter { it.isDigit() }.take(4)) },
                label = { Text(AppText.t("offline_focus_daily_point_cap")) },
                supportingText = { Text(AppText.t("offline_focus_daily_point_cap_desc")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TinyVowButton(
                text = AppText.t("offline_focus_save_daily_cap"),
                onClick = {
                    onSetDailyPointCap(
                        capDraft.toIntOrNull() ?: ManagedAppPreferences.DEFAULT_OFFLINE_FOCUS_DAILY_POINT_CAP,
                    )
                },
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        FocusSettingsNavRow(
            title = AppText.t("offline_focus_settings_defaults"),
            body = AppText.t("offline_focus_settings_defaults_desc"),
            onClick = onOpenDefaults,
        )
        FocusSettingsNavRow(
            title = AppText.t("offline_focus_whitelist"),
            body = AppText.t("offline_focus_whitelist_desc"),
            onClick = onOpenWhitelist,
        )
        FocusSettingsNavRow(
            title = AppText.t("offline_focus_settings_categories"),
            body = AppText.t("offline_focus_settings_categories_desc"),
            onClick = onOpenCategories,
        )
    }
}

@Composable
private fun FocusSettingsNavRow(
    title: String,
    body: String,
    onClick: () -> Unit,
) {
    TinyVowCard(shape = RoundedCornerShape(TinyVowRadius.Card)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(TinyVowSpacing.CardHorizontal),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusDefaultsPage(
    activeCategories: List<OfflineFocusCategory>,
    defaultCategoryId: String?,
    defaultDurationMinutes: Int,
    defaultMode: OfflineFocusMode,
    onSelectDefaultCategory: (String) -> Unit,
    onSelectDefaultDuration: (Int) -> Unit,
    onSelectDefaultMode: (OfflineFocusMode) -> Unit,
) {
    FocusSettingsCard(
        title = AppText.t("offline_focus_settings_defaults"),
        body = AppText.t("offline_focus_settings_defaults_desc"),
    ) {
        FocusLabel(AppText.t("offline_focus_default_category"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activeCategories.forEach { category ->
                AssistChip(
                    onClick = { onSelectDefaultCategory(category.id) },
                    label = {
                        Text(category.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    leadingIcon = {
                        FocusTypeIcon(
                            iconKey = category.iconKey,
                            customIconPath = category.customIconPath,
                            color = Color(category.colorArgb),
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    border = BorderStroke(
                        1.dp,
                        if (category.id == defaultCategoryId) Color(category.colorArgb) else MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }
        }
        FocusLabel(AppText.t("offline_focus_duration"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 25, 45, 60).forEach { minutes ->
                AssistChip(
                    onClick = { onSelectDefaultDuration(minutes) },
                    label = { Text(AppText.t("offline_focus_minutes_format", minutes)) },
                    border = BorderStroke(
                        1.dp,
                        if (minutes == defaultDurationMinutes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }
        }
        FocusLabel(AppText.t("offline_focus_mode"))
        FocusModeChips(selected = defaultMode, onSelect = onSelectDefaultMode)
    }
}

@Composable
private fun FocusWhitelistPage(
    installedApps: List<ManagedApp>,
    whitelistPackages: Set<String>,
    onAddApp: () -> Unit,
    onRemoveApp: (String) -> Unit,
) {
    FocusSettingsCard(
        title = AppText.t("offline_focus_whitelist"),
        body = AppText.t("offline_focus_whitelist_desc"),
    ) {
        TinyVowButton(
            text = AppText.t("offline_focus_whitelist_add_app"),
            onClick = onAddApp,
            tone = TinyVowButtonTone.Neutral,
            modifier = Modifier.fillMaxWidth(),
        )
        FocusWhitelistAppList(
            apps = installedApps,
            selectedPackages = whitelistPackages,
            onRemove = onRemoveApp,
        )
    }
}

@Composable
private fun FocusCategoriesPage(
    categories: List<OfflineFocusCategory>,
    editingCategoryId: String?,
    onAddCategory: () -> Unit,
    onEditCategory: (String) -> Unit,
    onMoveCategory: (String, Int) -> Unit,
    onSetCategoryArchived: (String, Boolean) -> Unit,
    onDeleteCategory: (String) -> Unit,
) {
    FocusSettingsCard(
        title = AppText.t("offline_focus_settings_categories"),
        body = AppText.t("offline_focus_settings_categories_desc"),
    ) {
        TinyVowButton(
            text = AppText.t("offline_focus_category_add"),
            onClick = onAddCategory,
            tone = TinyVowButtonTone.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
        categories.forEach { category ->
            OfflineCategoryRow(
                category = category,
                selected = category.id == editingCategoryId,
                onEdit = { onEditCategory(category.id) },
                onMoveCategory = onMoveCategory,
                onSetCategoryArchived = onSetCategoryArchived,
                onDeleteCategory = onDeleteCategory,
            )
        }
    }
}

@Composable
private fun FocusEnableCard(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    TinyVowCard(shape = RoundedCornerShape(TinyVowRadius.Card)) {
        Row(
            modifier = Modifier.padding(TinyVowSpacing.CardHorizontal),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun FocusSettingsCard(
    title: String,
    body: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    TinyVowCard(shape = RoundedCornerShape(TinyVowRadius.Card)) {
        Column(
            modifier = Modifier.padding(TinyVowSpacing.CardHorizontal),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            content()
        }
    }
}

@Composable
private fun FocusSwitchRow(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FocusWhitelistAppList(
    apps: List<ManagedApp>,
    selectedPackages: Set<String>,
    onRemove: (String) -> Unit,
) {
    val appByPackage = remember(apps) { apps.associateBy { it.packageName } }
    val selectedApps = selectedPackages.sorted().map { packageName ->
        appByPackage[packageName] ?: ManagedApp(packageName = packageName, appName = packageName)
    }
    if (selectedApps.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        ) {
            Text(
                text = AppText.t("offline_focus_whitelist_empty"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            )
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        selectedApps.forEach { app ->
            FocusWhitelistAppRow(app = app, onRemove = { onRemove(app.packageName) })
        }
    }
}

@Composable
private fun FocusWhitelistAppRow(
    app: ManagedApp,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FocusWhitelistAppIcon(app = app, modifier = Modifier.size(36.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.DeleteOutline, contentDescription = AppText.t("offline_focus_whitelist_remove_app"))
            }
        }
    }
}

@Composable
private fun FocusWhitelistAppDialog(
    apps: List<ManagedApp>,
    excludedPackageNames: Set<String>,
    onDismiss: () -> Unit,
    onAdd: (ManagedApp) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val candidates =
        remember(apps, excludedPackageNames) {
            apps
                .filterNot { it.packageName in excludedPackageNames }
                .sortedBy { it.appName.lowercase(Locale.getDefault()) }
        }
    val filteredApps =
        remember(candidates, query) {
            val normalizedQuery = query.trim().lowercase(Locale.getDefault())
            if (normalizedQuery.isBlank()) {
                candidates
            } else {
                candidates.filter {
                    it.appName.lowercase(Locale.getDefault()).contains(normalizedQuery) ||
                        it.packageName.lowercase(Locale.getDefault()).contains(normalizedQuery)
                }
            }
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("offline_focus_whitelist_add_app")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(AppText.t("offline_focus_whitelist_search_hint")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (apps.isEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(AppText.t("special_app_request_running"), style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            FocusWhitelistCandidateRow(app = app, onClick = { onAdd(app) })
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_cancel"))
            }
        },
    )
}

@Composable
private fun FocusWhitelistCandidateRow(
    app: ManagedApp,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FocusWhitelistAppIcon(app = app, modifier = Modifier.size(42.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FocusWhitelistAppIcon(
    app: ManagedApp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var icon by remember(app.packageName) { mutableStateOf<Drawable?>(null) }
    LaunchedEffect(app.packageName) {
        icon = withContext(Dispatchers.IO) {
            AppVisualCache.getIcon(context, app.packageName)
        }
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                AsyncImage(
                    model = icon,
                    contentDescription = app.appName,
                    modifier = Modifier.fillMaxSize(0.78f),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = app.appName,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize(0.48f),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusModeChips(
    selected: OfflineFocusMode,
    onSelect: (OfflineFocusMode) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OfflineFocusMode.entries.forEach { mode ->
            AssistChip(
                onClick = { onSelect(mode) },
                label = {
                    Text(
                        when (mode) {
                            OfflineFocusMode.NORMAL -> AppText.t("offline_focus_mode_normal")
                            OfflineFocusMode.STRICT -> AppText.t("offline_focus_mode_strict")
                        },
                    )
                },
                border = BorderStroke(
                    1.dp,
                    if (selected == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
            )
        }
    }
    Text(
        text =
            when (selected) {
                OfflineFocusMode.NORMAL -> AppText.t("offline_focus_mode_normal_desc")
                OfflineFocusMode.STRICT -> AppText.t("offline_focus_mode_strict_desc")
            },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun FocusLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun OfflineCategoryRow(
    category: OfflineFocusCategory,
    selected: Boolean,
    onEdit: () -> Unit,
    onMoveCategory: (String, Int) -> Unit,
    onSetCategoryArchived: (String, Boolean) -> Unit,
    onDeleteCategory: (String) -> Unit,
) {
    val categoryColor = Color(category.colorArgb)
    Surface(
        modifier = Modifier.alpha(if (category.isArchived) 0.58f else 1f),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, categoryColor.copy(alpha = if (selected) 0.70f else 0.30f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FocusTypeIcon(
                iconKey = category.iconKey,
                customIconPath = category.customIconPath,
                color = categoryColor,
                modifier = Modifier.size(34.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (category.isArchived) {
                        AppText.t("offline_focus_category_archived")
                    } else {
                        AppText.t("offline_focus_points_per_minute_value", formatPointsRate(category.pointsPerMinute))
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            CompactCategoryActionButton(
                imageVector = Icons.Default.ArrowUpward,
                color = categoryColor,
                onClick = { onMoveCategory(category.id, -1) },
                contentDescription = AppText.t("offline_focus_category_move_up"),
            )
            CompactCategoryActionButton(
                imageVector = Icons.Default.ArrowDownward,
                color = categoryColor,
                onClick = { onMoveCategory(category.id, 1) },
                contentDescription = AppText.t("offline_focus_category_move_down"),
            )
            CompactCategoryActionButton(
                imageVector = Icons.Default.Edit,
                color = categoryColor,
                onClick = onEdit,
                contentDescription = AppText.t("offline_focus_category_save"),
            )
            CompactCategoryActionButton(
                imageVector = if (category.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                color = categoryColor,
                onClick = { onSetCategoryArchived(category.id, !category.isArchived) },
                contentDescription = if (category.isArchived) AppText.t("offline_focus_category_restore") else AppText.t("offline_focus_category_archive"),
            )
            CompactCategoryActionButton(
                imageVector = Icons.Default.Delete,
                color = categoryColor,
                onClick = { onDeleteCategory(category.id) },
                contentDescription = AppText.t("offline_focus_category_delete"),
            )
        }
    }
}

@Composable
private fun CompactCategoryActionButton(
    imageVector: ImageVector,
    color: Color,
    onClick: () -> Unit,
    contentDescription: String,
) {
    Surface(
        modifier =
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.52f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = color,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
internal fun OfflineFocusCategoryEditorDialog(
    category: OfflineFocusCategory?,
    onDismiss: () -> Unit,
    onSave: (String?, String, String, String?, Int, Double) -> Unit,
    onImportIcon: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            if (category == null) {
                                AppText.t("offline_focus_category_add")
                            } else {
                                AppText.t("offline_focus_category_save")
                            },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = onDismiss) {
                        Text(AppText.t("group_cancel"))
                    }
                }
                OfflineCategoryEditor(
                    category = category,
                    onSave = onSave,
                    onImportIcon = onImportIcon,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OfflineCategoryEditor(
    category: OfflineFocusCategory?,
    onSave: (String?, String, String, String?, Int, Double) -> Unit,
    onImportIcon: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var nameDraft by remember(category?.id, category?.name) {
        mutableStateOf(category?.name.orEmpty())
    }
    var iconDraft by remember(category?.id, category?.iconKey) {
        mutableStateOf(category?.iconKey ?: FocusPresetIconKeys.first())
    }
    var customIconPathDraft by remember(category?.id, category?.customIconPath) {
        mutableStateOf(category?.customIconPath)
    }
    var colorDraft by remember(category?.id, category?.colorArgb) {
        mutableStateOf(category?.colorArgb ?: offlineFocusSettingsPalette.first())
    }
    var showCustomColorField by remember(category?.id) { mutableStateOf(false) }
    var customColorDraft by remember(category?.id, category?.colorArgb) {
        mutableStateOf(formatColorHex(category?.colorArgb ?: offlineFocusSettingsPalette.first()))
    }
    var pointsDraft by remember(category?.id, category?.pointsPerMinute) {
        mutableStateOf(formatPointsRate(category?.pointsPerMinute ?: 1.0))
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = nameDraft,
                onValueChange = { nameDraft = it.take(24) },
                label = { Text(AppText.t("offline_focus_category_name")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = pointsDraft,
                onValueChange = { value -> pointsDraft = value.filter { it.isDigit() || it == '.' }.take(5) },
                label = { Text(AppText.t("offline_focus_points_per_minute")) },
                supportingText = { Text(AppText.t("offline_focus_points_per_minute_desc")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            FocusLabel(AppText.t("offline_focus_category_icon"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FocusPresetIconKeys.forEach { iconKey ->
                    val selected = iconDraft == iconKey
                    FocusIconChoiceTile(
                        iconKey = iconKey,
                        color = Color(colorDraft),
                        selected = selected,
                        onClick = {
                            iconDraft = iconKey
                            customIconPathDraft = null
                        },
                    )
                }
                category?.let {
                    FocusUploadIconTile(
                        color = Color(colorDraft),
                        onClick = { onImportIcon(it.id) },
                    )
                }
            }
            FocusLabel(AppText.t("offline_focus_category_color"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                offlineFocusSettingsPalette.forEach { colorArgb ->
                    FocusColorSwatch(
                        color = Color(colorArgb),
                        selected = colorDraft == colorArgb,
                        onClick = {
                            colorDraft = colorArgb
                            customColorDraft = formatColorHex(colorArgb)
                        },
                    )
                }
                FocusCustomColorButton(
                    color = Color(colorDraft),
                    onClick = {
                        customColorDraft = formatColorHex(colorDraft)
                        showCustomColorField = !showCustomColorField
                    },
                )
            }
            if (showCustomColorField) {
                OutlinedTextField(
                    value = customColorDraft,
                    onValueChange = { value ->
                        customColorDraft = value.take(7)
                        parseColorHex(customColorDraft)?.let { colorDraft = it }
                    },
                    label = { Text(AppText.t("app_color_manual_hex_label")) },
                    supportingText = { Text(AppText.t("app_color_manual_hex_hint")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TinyVowButton(
                    text = if (category == null) AppText.t("offline_focus_category_add") else AppText.t("offline_focus_category_save"),
                    onClick = {
                        onSave(
                            category?.id,
                            nameDraft,
                            iconDraft,
                            customIconPathDraft,
                            colorDraft,
                            pointsDraft.toDoubleOrNull() ?: 1.0,
                        )
                    },
                    tone = TinyVowButtonTone.Primary,
                    modifier = Modifier.weight(1f),
                )
                category?.let {
                    TextButton(onClick = { onDelete(it.id) }) {
                        Text(AppText.t("offline_focus_category_delete"))
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusIconChoiceTile(
    iconKey: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) color.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            FocusTypeIcon(
                iconKey = iconKey,
                customIconPath = null,
                color = color,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Composable
private fun FocusUploadIconTile(
    color: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = AppText.t("offline_focus_category_upload_icon"),
                tint = color,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun FocusColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier =
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface),
            )
        }
        Box(
            modifier =
                Modifier
                    .size(if (selected) 24.dp else 26.dp)
                    .clip(CircleShape)
                    .background(color),
        )
    }
}

@Composable
private fun FocusCustomColorButton(
    color: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
        shape = CircleShape,
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = AppText.t("ring_settings_custom_color_label"),
                tint = color,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

private fun formatPointsRate(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.2f".format(value).trimEnd('0').trimEnd('.')

private fun formatColorHex(colorArgb: Int): String = "#%06X".format(Locale.US, colorArgb and 0xFFFFFF)

private fun parseColorHex(value: String): Int? {
    val raw = value.trim().removePrefix("#")
    if (raw.length != 6 || raw.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) return null
    return (0xFF000000 or raw.toLong(16)).toInt()
}

private val offlineFocusSettingsPalette =
    listOf(
        0xFF6F4B39.toInt(),
        0xFFCDA783.toInt(),
        0xFF39A6E8.toInt(),
        0xFF2FAE9C.toInt(),
        0xFF2F9471.toInt(),
        0xFF62BD76.toInt(),
        0xFF8BA66B.toInt(),
        0xFF6ED3C0.toInt(),
        0xFFF24E3D.toInt(),
        0xFFEF2B35.toInt(),
        0xFFF45A67.toInt(),
        0xFFF36C93.toInt(),
        0xFFD56070.toInt(),
        0xFFF36F16.toInt(),
        0xFF625BD7.toInt(),
        0xFF784ED3.toInt(),
        0xFFB28AD5.toInt(),
    )
