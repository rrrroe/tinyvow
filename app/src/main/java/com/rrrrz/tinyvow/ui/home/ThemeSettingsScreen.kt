package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.ThemePresets
import com.rrrrz.tinyvow.ui.theme.ThemeSeed
import com.rrrrz.tinyvow.ui.theme.argbToHex
import com.rrrrz.tinyvow.ui.theme.createCustomTheme
import com.rrrrz.tinyvow.ui.theme.localizedName
import com.rrrrz.tinyvow.ui.theme.parseHexColorOrNull

@Composable
fun ThemeSettingsScreen(
    selectedThemeId: String,
    customThemes: List<ThemeSeed>,
    onSelectTheme: (String) -> Unit,
    onSaveCustomTheme: (ThemeSeed) -> Unit,
    onDeleteCustomTheme: (String) -> Unit,
    onBack: () -> Unit,
) {
    var editingTheme by remember { mutableStateOf<ThemeSeed?>(null) }
    val allThemes = ThemePresets + customThemes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("group_back"))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(AppText.t("me_appearance_theme"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    AppText.t("theme_manage_preset_and_custom_three_color_themes"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = {
                    editingTheme = createCustomTheme(
                        name = AppText.t("settings_custom_theme"),
                        controlColor = ThemePresets.first().controlColor,
                        encourageColor = ThemePresets.first().encourageColor,
                        baseColor = ThemePresets.first().baseColor,
                    )
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(AppText.t("me_new"))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(allThemes, key = { it.id }) { theme ->
                ThemeListItem(
                    theme = theme,
                    selected = selectedThemeId == theme.id,
                    onSelect = { onSelectTheme(theme.id) },
                    onEdit = {
                        editingTheme = if (theme.isCustom) {
                            theme
                        } else {
                            createCustomTheme(
                                name = AppText.t("me_value_custom", theme.localizedName()),
                                controlColor = theme.controlColor,
                                encourageColor = theme.encourageColor,
                                baseColor = theme.baseColor,
                            )
                        }
                    },
                    onCopy = {
                        editingTheme = createCustomTheme(
                            name = AppText.t("me_value_copy", theme.localizedName()),
                            controlColor = theme.controlColor,
                            encourageColor = theme.encourageColor,
                            baseColor = theme.baseColor,
                        )
                    },
                    onDelete = if (theme.isCustom) {
                        { onDeleteCustomTheme(theme.id) }
                    } else {
                        null
                    },
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    editingTheme?.let { theme ->
        ThemeEditorDialog(
            initialTheme = theme,
            onDismiss = { editingTheme = null },
            onSave = {
                onSaveCustomTheme(it.copy(isCustom = true))
                editingTheme = null
            },
        )
    }
}

@Composable
private fun ThemeListItem(
    theme: ThemeSeed,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor.copy(alpha = if (selected) 0.70f else 0.42f)),
        tonalElevation = if (selected) 1.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier
                        .width(76.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    ThemeStrip(Color(theme.controlColor))
                    ThemeStrip(Color(theme.encourageColor))
                    ThemeStrip(Color(theme.baseColor))
                }
                Text(
                    if (theme.isCustom) AppText.t("theme_custom") else AppText.t("theme_presets"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(theme.localizedName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    AppText.t("theme_limit_value_encourage_value_base_value", argbToHex(theme.controlColor), argbToHex(theme.encourageColor), argbToHex(theme.baseColor)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = AppText.t("me_copy"))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = AppText.t("me_edit"))
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = AppText.t("group_delete"), tint = LocalThemeColors.current.control)
                }
            }
        }
    }
}

@Composable
private fun ThemeEditorDialog(
    initialTheme: ThemeSeed,
    onDismiss: () -> Unit,
    onSave: (ThemeSeed) -> Unit,
) {
    var name by remember(initialTheme.id) { mutableStateOf(initialTheme.name) }
    var control by remember(initialTheme.id) { mutableStateOf(initialTheme.controlColor) }
    var encourage by remember(initialTheme.id) { mutableStateOf(initialTheme.encourageColor) }
    var base by remember(initialTheme.id) { mutableStateOf(initialTheme.baseColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("me_edit_theme"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppText.t("me_theme_name")) },
                    singleLine = true,
                )
                ColorControl(AppText.t("me_limit_color"), AppText.t("theme_limit_groups_blocking_over_limit_and_risk_states"), control, onColorChange = { control = it })
                ColorControl(AppText.t("me_encourage_color"), AppText.t("theme_encourage_groups_points_rewards_and_completion_states"), encourage, onColorChange = { encourage = it })
                ColorControl(AppText.t("me_base_color"), AppText.t("theme_navigation_buttons_cards_and_common_components"), base, onColorChange = { base = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialTheme.copy(
                            name = name.ifBlank { AppText.t("settings_custom_theme") },
                            controlColor = control,
                            encourageColor = encourage,
                            baseColor = base,
                            isCustom = true,
                        )
                    )
                }
            ) {
                Text(AppText.t("group_save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppText.t("group_cancel")) }
        },
    )
}

@Composable
private fun ColorControl(
    label: String,
    description: String,
    color: Int,
    onColorChange: (Int) -> Unit,
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(1f) }
    var hexInput by remember(color) { mutableStateOf(argbToHex(color)) }

    LaunchedEffect(color) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
        hexInput = argbToHex(color)
    }

    fun emit() {
        onColorChange(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness)))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(color)))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        OutlinedTextField(
            value = hexInput,
            onValueChange = { value ->
                hexInput = value
                parseHexColorOrNull(value)?.let(onColorChange)
            },
            label = { Text(AppText.t("theme_color_code")) },
            placeholder = { Text("#AABBCC") },
            singleLine = true,
        )
        Text(AppText.t("theme_hue_value", hue.toInt()), style = MaterialTheme.typography.labelSmall)
        Slider(value = hue, onValueChange = { hue = it; emit() }, valueRange = 0f..360f)
        Text(AppText.t("theme_saturation_value", (saturation * 100).toInt()), style = MaterialTheme.typography.labelSmall)
        Slider(value = saturation, onValueChange = { saturation = it; emit() }, valueRange = 0.05f..0.80f)
        Text(AppText.t("theme_brightness_value", (brightness * 100).toInt()), style = MaterialTheme.typography.labelSmall)
        Slider(value = brightness, onValueChange = { brightness = it; emit() }, valueRange = 0.55f..0.98f)
    }
}

@Composable
private fun RowScope.ThemeStrip(color: Color) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(color)
    )
}
