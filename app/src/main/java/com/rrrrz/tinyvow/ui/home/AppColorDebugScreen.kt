package com.rrrrz.tinyvow.ui.home

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.settings.StoredAppColorPreferences
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalReportColors
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppColorDebugScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val reportColors = LocalReportColors.current
    val themeColors = LocalThemeColors.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = remember(context) { ManagedAppPreferences(context.applicationContext) }
    val appColorPreferences by preferences.appColorPreferences.collectAsState(
        initial = StoredAppColorPreferences(),
    )
    var items by remember { mutableStateOf<List<AppColorComparisonItem>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(true) }
    var isLoadingColors by remember { mutableStateOf(false) }
    var pendingChoice by remember { mutableStateOf<PendingColorChoice?>(null) }
    var pendingManualPicker by remember { mutableStateOf<ManualColorPickerState?>(null) }

    LaunchedEffect(context, reportColors.appChartPalette) {
        isLoadingApps = true
        isLoadingColors = false
        items = loadAppColorIconItems(
            context = context.applicationContext,
            fallbackPalette = reportColors.appChartPalette,
        )
        isLoadingApps = false
        isLoadingColors = items.isNotEmpty()
        items.chunked(8).forEach { chunk ->
            val computed = withContext(Dispatchers.Default) {
                chunk.associate { item ->
                    item.packageName to extractAllAlgorithmColors(item.bitmap, item.fallback)
                }
            }
            items = items.map { item ->
                computed[item.packageName]?.let { colors -> item.copy(colors = colors) } ?: item
            }
        }
        isLoadingColors = false
    }

    pendingChoice?.let { choice ->
        ConfirmColorChoiceDialog(
            choice = choice,
            onDismiss = { pendingChoice = null },
            onConfirm = {
                pendingChoice = null
                coroutineScope.launch {
                    if (choice.clearExisting) {
                        preferences.clearAppColorSelection(choice.item.packageName)
                    } else {
                        preferences.setAppColorSelection(
                            packageName = choice.item.packageName,
                            source = choice.source,
                            argb = choice.color.toArgb(),
                        )
                    }
                }
            },
        )
    }

    pendingManualPicker?.let { picker ->
        ManualColorPickerDialog(
            state = picker,
            onColorPicked = { color ->
                pendingManualPicker = pendingManualPicker?.copy(
                    selectedColor = color,
                    hexValue = color.toHexString(),
                )
            },
            onTapPositionChanged = { xRatio, yRatio ->
                pendingManualPicker = pendingManualPicker?.copy(
                    selectedXRatio = xRatio,
                    selectedYRatio = yRatio,
                )
            },
            onHexChanged = { value ->
                pendingManualPicker = pendingManualPicker?.copy(
                    hexValue = value,
                    selectedColor = parseHexColor(value) ?: pendingManualPicker?.selectedColor,
                )
            },
            onDismiss = { pendingManualPicker = null },
            onConfirm = { color ->
                pendingManualPicker = null
                coroutineScope.launch {
                    preferences.setManualAppColor(
                        packageName = picker.item.packageName,
                        argb = color.toArgb(),
                    )
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
                        text = AppText.t("app_color_debug_title"),
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
                .padding(
                    horizontal = TinyVowSpacing.PageHorizontal,
                    vertical = TinyVowSpacing.PageTop,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            TinyVowCard {
                Column(
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = AppText.t("app_color_debug_description"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeColors.ink.copy(alpha = 0.78f),
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val selectedAlgorithm = AppColorAlgorithm.fromStorageValue(appColorPreferences.defaultAlgorithm)
                        AppColorAlgorithm.entries.forEach { algorithm ->
                            FilterChip(
                                selected = selectedAlgorithm == algorithm,
                                onClick = {
                                    coroutineScope.launch {
                                        preferences.setAppColorDefaultAlgorithm(algorithm.storageValue)
                                    }
                                },
                                label = {
                                    Text(
                                        text = AppText.t(algorithm.labelKey),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                }
            }

            if (isLoadingApps) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = AppText.t("app_color_loading"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeColors.inkMuted,
                        )
                    }
                }
            } else if (items.isEmpty()) {
                Text(
                    text = AppText.t("app_color_empty"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.inkMuted,
                    modifier = Modifier.padding(8.dp),
                )
            } else {
                if (isLoadingColors) {
                    Text(
                        text = AppText.t("app_color_loading_algorithms"),
                        style = MaterialTheme.typography.labelMedium,
                        color = themeColors.inkMuted,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = items,
                        key = { it.packageName },
                    ) { item ->
                        AppColorComparisonRow(
                            item = item,
                            preferences = appColorPreferences,
                            onAlgorithmClick = { algorithm, color, isSelected ->
                                pendingChoice =
                                    PendingColorChoice(
                                        item = item,
                                        source = algorithm.storageValue,
                                        color = color,
                                        clearExisting = isSelected,
                                    )
                            },
                            onManualClick = {
                                val manualColor = appColorPreferences.manualColors[item.packageName]?.let(::Color)
                                pendingManualPicker = ManualColorPickerState(
                                    item = item,
                                    selectedColor = manualColor,
                                    hexValue = manualColor?.toHexString().orEmpty(),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppColorComparisonRow(
    item: AppColorComparisonItem,
    preferences: StoredAppColorPreferences,
    onAlgorithmClick: (AppColorAlgorithm, Color, Boolean) -> Unit,
    onManualClick: () -> Unit,
) {
    val selection = preferences.selections[item.packageName]
    val selectedAlgorithm = selection?.source?.let(::algorithmForAppColorSource)
    val isManualSelected = selection?.source?.let(::isManualAppColorSource) == true
    val defaultAlgorithm = AppColorAlgorithm.fromStorageValue(preferences.defaultAlgorithm)
    val currentColor =
        selection?.argb?.let(::Color)
            ?: item.colors[defaultAlgorithm]
            ?: item.colors[AppColorAlgorithm.CURRENT]
            ?: item.fallback
    val manualColor = preferences.manualColors[item.packageName]?.let(::Color)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppColorBlock(
                color = currentColor,
                selected = false,
                contentDescription = AppText.t("app_color_current_color"),
                size = AppColorSwatchSize,
            )
            AppIconPreview(icon = item.icon, size = AppColorIconSize)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = AppColorIconSize),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = item.appName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )
            }
            AppColorAlgorithm.entries.forEach { algorithm ->
                AppColorBlock(
                    color = item.colors[algorithm],
                    selected = selectedAlgorithm == algorithm,
                    contentDescription = AppText.t(algorithm.labelKey),
                    size = AppColorSwatchSize,
                    isLoading = item.colors[algorithm] == null,
                    onClick = {
                        item.colors[algorithm]?.let { color ->
                            onAlgorithmClick(
                                algorithm,
                                color,
                                selectedAlgorithm == algorithm,
                            )
                        }
                    },
                )
            }
            AppColorBlock(
                color = manualColor,
                selected = isManualSelected,
                showSettings = true,
                contentDescription = AppText.t("app_color_manual_color"),
                size = AppColorSwatchSize,
                onClick = onManualClick,
            )
        }
    }
}

@Composable
private fun AppIconPreview(
    icon: ImageBitmap?,
    size: Dp,
) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
            )
        }
    }
}

@Composable
private fun AppColorBlock(
    color: Color?,
    selected: Boolean,
    contentDescription: String,
    showSettings: Boolean = false,
    size: Dp = AppColorSwatchSize,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    val background = color ?: Color.Transparent
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (color == null && !isLoading) {
            TransparentColorGrid()
        } else if (color != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(background),
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 1.5.dp,
                modifier = Modifier.size(16.dp),
            )
        }
        if (showSettings && !selected) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = contentDescription,
                tint = if (color == null) MaterialTheme.colorScheme.onSurfaceVariant else readableIconTint(color),
                modifier = Modifier.size(18.dp),
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = contentDescription,
                tint = color?.let(::readableIconTint) ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun TransparentColorGrid() {
    val light = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
    val dark = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cell = size.width / 4f
        for (x in 0 until 4) {
            for (y in 0 until 4) {
                drawRect(
                    color = if ((x + y) % 2 == 0) light else dark,
                    topLeft = androidx.compose.ui.geometry.Offset(x * cell, y * cell),
                    size = Size(cell, cell),
                )
            }
        }
    }
}

@Composable
private fun ConfirmColorChoiceDialog(
    choice: PendingColorChoice,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (choice.clearExisting) {
                    AppText.t("app_color_clear_selection_title")
                } else {
                    AppText.t("app_color_save_selection_title")
                },
            )
        },
        text = {
            Text(
                text = if (choice.clearExisting) {
                    AppText.t("app_color_clear_selection_message", choice.item.appName)
                } else {
                    AppText.t("app_color_save_selection_message", choice.item.appName)
                },
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(
                    text = if (choice.clearExisting) {
                        AppText.t("app_color_clear_selection_confirm")
                    } else {
                        AppText.t("app_color_save_selection_confirm")
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("action_cancel"))
            }
        },
    )
}

@Composable
private fun ManualColorPickerDialog(
    state: ManualColorPickerState,
    onColorPicked: (Color) -> Unit,
    onTapPositionChanged: (Float, Float) -> Unit,
    onHexChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("app_color_manual_picker_title", state.item.appName)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(18.dp),
                        )
                        .pointerInput(state.item.bitmap) {
                            detectTapGestures { offset ->
                                pickManualColorAt(
                                    bitmap = state.item.bitmap,
                                    offset = offset,
                                    width = size.width,
                                    height = size.height,
                                    onTapPositionChanged = onTapPositionChanged,
                                    onColorPicked = onColorPicked,
                                )
                            }
                        }
                        .pointerInput(state.item.bitmap) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    pickManualColorAt(
                                        bitmap = state.item.bitmap,
                                        offset = offset,
                                        width = size.width,
                                        height = size.height,
                                        onTapPositionChanged = onTapPositionChanged,
                                        onColorPicked = onColorPicked,
                                    )
                                },
                                onDrag = { change, _ ->
                                    pickManualColorAt(
                                        bitmap = state.item.bitmap,
                                        offset = change.position,
                                        width = size.width,
                                        height = size.height,
                                        onTapPositionChanged = onTapPositionChanged,
                                        onColorPicked = onColorPicked,
                                    )
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = state.item.icon,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                    )
                    if (state.selectedXRatio != null && state.selectedYRatio != null) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(
                                x = size.width * state.selectedXRatio,
                                y = size.height * state.selectedYRatio,
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 10.dp.toPx(),
                                center = center,
                                style = Stroke(width = 3.dp.toPx()),
                            )
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.82f),
                                radius = 10.dp.toPx(),
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx()),
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppColorBlock(
                        color = state.selectedColor,
                        selected = false,
                        contentDescription = AppText.t("app_color_manual_selected_color"),
                        size = AppColorSwatchSize,
                    )
                    Text(
                        text = AppText.t("app_color_manual_picker_hint"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedTextField(
                    value = state.hexValue,
                    onValueChange = onHexChanged,
                    singleLine = true,
                    label = { Text(AppText.t("app_color_manual_hex_label")) },
                    supportingText = {
                        Text(
                            text = AppText.t("app_color_manual_hex_hint"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = state.selectedColor != null,
                onClick = { state.selectedColor?.let(onConfirm) },
            ) {
                Text(AppText.t("app_color_save_selection_confirm"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("action_cancel"))
            }
        },
    )
}

private data class AppColorComparisonItem(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap,
    val bitmap: Bitmap,
    val fallback: Color,
    val colors: Map<AppColorAlgorithm, Color> = emptyMap(),
)

private data class PendingColorChoice(
    val item: AppColorComparisonItem,
    val source: String,
    val color: Color,
    val clearExisting: Boolean,
)

private data class ManualColorPickerState(
    val item: AppColorComparisonItem,
    val selectedColor: Color?,
    val hexValue: String,
    val selectedXRatio: Float? = null,
    val selectedYRatio: Float? = null,
)

private val AppColorIconSize = 32.dp
private val AppColorSwatchSize = 30.dp

private suspend fun loadAppColorIconItems(
    context: Context,
    fallbackPalette: List<Color>,
): List<AppColorComparisonItem> = withContext(Dispatchers.Default) {
    val apps = InstalledAppRepository(context).getAllInstalledApps(
        usageLookbackDays = InstalledAppRepository.APP_COLOR_USAGE_LOOKBACK_DAYS,
    )
    val palette = fallbackPalette.ifEmpty { listOf(Color(0xFF4F7DFF)) }
    apps.mapNotNull { app ->
        buildAppColorComparisonItem(
            context = context,
            app = app,
            fallback = stableAppFallbackColor(app.packageName, palette),
        )
    }
}

private fun buildAppColorComparisonItem(
    context: Context,
    app: ManagedApp,
    fallback: Color,
): AppColorComparisonItem? {
    val icon = AppVisualCache.getIcon(context, app.packageName) ?: return null
    val bitmap = icon.toBitmap(width = 160, height = 160, config = Bitmap.Config.ARGB_8888)
    return AppColorComparisonItem(
        packageName = app.packageName,
        appName = app.appName,
        icon = bitmap.asImageBitmap(),
        bitmap = bitmap,
        fallback = fallback,
    )
}

private fun sampleBitmapColor(
    bitmap: Bitmap,
    xRatio: Float,
    yRatio: Float,
): Color? {
    val x = (xRatio * (bitmap.width - 1)).toInt().coerceIn(0, bitmap.width - 1)
    val y = (yRatio * (bitmap.height - 1)).toInt().coerceIn(0, bitmap.height - 1)
    nearestOpaquePixel(bitmap, x, y)?.let { return Color(it) }
    return null
}

private fun pickManualColorAt(
    bitmap: Bitmap,
    offset: Offset,
    width: Int,
    height: Int,
    onTapPositionChanged: (Float, Float) -> Unit,
    onColorPicked: (Color) -> Unit,
) {
    val xRatio = (offset.x / width.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val yRatio = (offset.y / height.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    onTapPositionChanged(xRatio, yRatio)
    sampleBitmapColor(
        bitmap = bitmap,
        xRatio = xRatio,
        yRatio = yRatio,
    )?.let(onColorPicked)
}

private fun nearestOpaquePixel(
    bitmap: Bitmap,
    centerX: Int,
    centerY: Int,
): Int? {
    for (radius in 0..8) {
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val x = (centerX + dx).coerceIn(0, bitmap.width - 1)
                val y = (centerY + dy).coerceIn(0, bitmap.height - 1)
                val pixel = bitmap.getPixel(x, y)
                if (android.graphics.Color.alpha(pixel) >= 48) {
                    return android.graphics.Color.rgb(
                        android.graphics.Color.red(pixel),
                        android.graphics.Color.green(pixel),
                        android.graphics.Color.blue(pixel),
                    )
                }
            }
        }
    }
    return null
}

private fun readableIconTint(color: Color): Color =
    if (color.luminance() > 0.55f) Color.Black else Color.White

private fun Color.toHexString(): String {
    val argb = toArgb()
    val red = android.graphics.Color.red(argb)
    val green = android.graphics.Color.green(argb)
    val blue = android.graphics.Color.blue(argb)
    return "#%02X%02X%02X".format(red, green, blue)
}

private fun parseHexColor(value: String): Color? {
    val normalized = value.trim().removePrefix("#")
    if (normalized.length != 6 || normalized.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) {
        return null
    }
    return runCatching {
        Color(android.graphics.Color.rgb(
            normalized.substring(0, 2).toInt(16),
            normalized.substring(2, 4).toInt(16),
            normalized.substring(4, 6).toInt(16),
        ))
    }.getOrNull()
}
