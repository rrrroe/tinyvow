package com.rrrrz.tinyvow.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.MemberThemePresets
import com.rrrrz.tinyvow.ui.theme.ThemePresets
import com.rrrrz.tinyvow.ui.theme.ThemeSeed
import com.rrrrz.tinyvow.ui.theme.argbToHex
import com.rrrrz.tinyvow.ui.theme.createCustomTheme
import com.rrrrz.tinyvow.ui.theme.localizedName
import com.rrrrz.tinyvow.ui.theme.parseHexColorOrNull
import kotlin.math.roundToInt

@Composable
fun ThemeSettingsScreen(
    selectedThemeId: String,
    customThemes: List<ThemeSeed>,
    isProActive: Boolean,
    isLocalActivationEnabled: Boolean,
    onSelectTheme: (String) -> Unit,
    onSaveCustomTheme: (ThemeSeed) -> Unit,
    onDeleteCustomTheme: (String) -> Unit,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onBack: () -> Unit,
) {
    var editingTheme by remember { mutableStateOf<ThemeSeed?>(null) }
    val allThemes = ThemePresets + MemberThemePresets + customThemes

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
                Text(AppText.t("me_appearance_theme"), style = MaterialTheme.typography.titleLarge)
                Text(
                    AppText.t("theme_manage_preset_and_custom_three_color_themes"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = {
                    if (ProFeatureGate.canAddCustomTheme(isProActive, customThemes.size)) {
                        editingTheme = createCustomTheme(
                            name = AppText.t("settings_custom_theme"),
                            controlColor = ThemePresets.first().controlColor,
                            encourageColor = ThemePresets.first().encourageColor,
                            baseColor = ThemePresets.first().baseColor,
                        )
                    } else {
                        onShowProUpsell(ProUpsellSource.CUSTOM_THEME)
                    }
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
                val customIndex = customThemes.indexOfFirst { it.id == theme.id }
                val themeLocked =
                    !ProFeatureGate.canSelectTheme(isProActive, theme.id) ||
                        (theme.isCustom && !ProFeatureGate.canEditCustomTheme(isProActive, customIndex))
                val lockedSource =
                    if (ProFeatureGate.isMemberTheme(theme.id)) ProUpsellSource.MEMBER_THEME else ProUpsellSource.CUSTOM_THEME
                ThemeListItem(
                    theme = theme,
                    selected = selectedThemeId == theme.id,
                    isLocked = themeLocked,
                    showCategory = ProFeatureGate.isMemberTheme(theme.id) || theme.isCustom,
                    onSelect = {
                        if (themeLocked) {
                            onShowProUpsell(lockedSource)
                        } else {
                            onSelectTheme(theme.id)
                        }
                    },
                    onEdit = if (theme.isCustom) {
                        {
                        when {
                            ProFeatureGate.isMemberTheme(theme.id) && !isProActive -> {
                                onShowProUpsell(ProUpsellSource.MEMBER_THEME)
                            }
                            theme.isCustom && !ProFeatureGate.canEditCustomTheme(isProActive, customIndex) -> {
                                onShowProUpsell(ProUpsellSource.CUSTOM_THEME)
                            }
                            theme.isCustom -> {
                                editingTheme = theme
                            }
                            ProFeatureGate.canAddCustomTheme(isProActive, customThemes.size) -> {
                                editingTheme = createCustomTheme(
                                    name = AppText.t("me_value_custom", theme.localizedName()),
                                    controlColor = theme.controlColor,
                                    encourageColor = theme.encourageColor,
                                    baseColor = theme.baseColor,
                                )
                            }
                            else -> {
                                onShowProUpsell(ProUpsellSource.CUSTOM_THEME)
                            }
                        }
                        }
                    } else null,
                    onCopy = {
                        if (ProFeatureGate.isMemberTheme(theme.id) && !isProActive) {
                            onShowProUpsell(ProUpsellSource.MEMBER_THEME)
                        } else if (ProFeatureGate.canAddCustomTheme(isProActive, customThemes.size)) {
                            editingTheme = createCustomTheme(
                                name = AppText.t("me_value_copy", theme.localizedName()),
                                controlColor = theme.controlColor,
                                encourageColor = theme.encourageColor,
                                baseColor = theme.baseColor,
                            )
                        } else {
                            onShowProUpsell(ProUpsellSource.CUSTOM_THEME)
                        }
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
    isLocked: Boolean,
    showCategory: Boolean,
    onSelect: () -> Unit,
    onEdit: (() -> Unit)?,
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier
                        .width(68.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    ThemeStrip(Color(theme.controlColor))
                    ThemeStrip(Color(theme.encourageColor))
                    ThemeStrip(Color(theme.baseColor))
                }
                if (showCategory) {
                    Text(
                        when {
                            ProFeatureGate.isMemberTheme(theme.id) -> AppText.t("theme_member")
                            theme.isCustom -> AppText.t("theme_custom")
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(theme.localizedName(), style = MaterialTheme.typography.titleMedium)
                if (isLocked) {
                    Text(
                        AppText.t("theme_member_unlock_hint"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = AppText.t("me_copy"))
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = AppText.t("me_edit"))
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = AppText.t("group_delete"), tint = LocalThemeColors.current.control)
                }
            }
        }
    }
}

private enum class ThemeColorRole {
    CONTROL,
    ENCOURAGE,
    BASE,
}

private val ThemeFieldHeight = 50.dp
private val ThemeFieldShape = RoundedCornerShape(16.dp)

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
    var selectedRole by remember(initialTheme.id) { mutableStateOf(ThemeColorRole.CONTROL) }

    val previewTheme = remember(name, control, encourage, base, initialTheme.id) {
        initialTheme.copy(
            name = name.ifBlank { AppText.t("settings_custom_theme") },
            controlColor = control,
            encourageColor = encourage,
            baseColor = base,
            isCustom = true,
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ThemeInputField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = AppText.t("me_theme_name"),
                        modifier = Modifier.fillMaxWidth()
                    )
                    ThemeColorEditorPanel(
                        role = selectedRole,
                        controlColor = control,
                        encourageColor = encourage,
                        baseColor = base,
                        onRoleSelected = { selectedRole = it },
                        color = when (selectedRole) {
                            ThemeColorRole.CONTROL -> control
                            ThemeColorRole.ENCOURAGE -> encourage
                            ThemeColorRole.BASE -> base
                        },
                        onColorChange = {
                            when (selectedRole) {
                                ThemeColorRole.CONTROL -> control = it
                                ThemeColorRole.ENCOURAGE -> encourage = it
                                ThemeColorRole.BASE -> base = it
                            }
                        },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, top = 0.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppText.t("group_cancel"))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(previewTheme) },
                    ) {
                        Text(AppText.t("group_save"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeColorRoleButton(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = 0.dp)
            .height(40.dp),
        shape = RoundedCornerShape(16.dp),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)) else null,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 2.dp else 0.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ThemeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    ThemeFieldContainer(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.titleSmall.merge(
                TextStyle(color = MaterialTheme.colorScheme.onSurface)
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun ThemeFieldContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier.height(ThemeFieldHeight),
        shape = ThemeFieldShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun ThemeColorEditorPanel(
    role: ThemeColorRole,
    controlColor: Int,
    encourageColor: Int,
    baseColor: Int,
    onRoleSelected: (ThemeColorRole) -> Unit,
    color: Int,
    onColorChange: (Int) -> Unit,
) {
    var hue by remember(role) { mutableFloatStateOf(0f) }
    var saturation by remember(role) { mutableFloatStateOf(1f) }
    var brightness by remember(role) { mutableFloatStateOf(1f) }
    var hexInput by remember(role, color) { mutableStateOf(argbToHex(color)) }

    LaunchedEffect(color, role) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
        hexInput = argbToHex(color)
    }

    fun emit(nextHue: Float = hue, nextSaturation: Float = saturation, nextBrightness: Float = brightness) {
        onColorChange(android.graphics.Color.HSVToColor(floatArrayOf(nextHue, nextSaturation, nextBrightness)))
    }

    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (role) {
                            ThemeColorRole.CONTROL -> AppText.t("me_limit_color")
                            ThemeColorRole.ENCOURAGE -> AppText.t("me_encourage_color")
                            ThemeColorRole.BASE -> AppText.t("me_base_color")
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = when (role) {
                            ThemeColorRole.CONTROL -> AppText.t("theme_limit_groups_blocking_over_limit_and_risk_states")
                            ThemeColorRole.ENCOURAGE -> AppText.t("theme_encourage_groups_points_rewards_and_completion_states")
                            ThemeColorRole.BASE -> AppText.t("theme_navigation_buttons_cards_and_common_components")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeColorRoleButton(
                    label = AppText.t("theme_control_role"),
                    color = Color(controlColor),
                    selected = role == ThemeColorRole.CONTROL,
                    onClick = { onRoleSelected(ThemeColorRole.CONTROL) },
                    modifier = Modifier.weight(1f),
                )
                ThemeColorRoleButton(
                    label = AppText.t("theme_encourage_role"),
                    color = Color(encourageColor),
                    selected = role == ThemeColorRole.ENCOURAGE,
                    onClick = { onRoleSelected(ThemeColorRole.ENCOURAGE) },
                    modifier = Modifier.weight(1f),
                )
                ThemeColorRoleButton(
                    label = AppText.t("theme_base_role"),
                    color = Color(baseColor),
                    selected = role == ThemeColorRole.BASE,
                    onClick = { onRoleSelected(ThemeColorRole.BASE) },
                    modifier = Modifier.weight(1f),
                )
            }
            ThemeColorProgressBar(
                label = AppText.t("theme_hue_value", hue.roundToInt().toString()),
                value = hue,
                valueRange = 0f..360f,
                activeColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.72f, 0.92f))),
                inactiveColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f),
                onValueChange = { next ->
                    hue = next
                    emit(nextHue = next)
                },
            )
            ThemeColorProgressBar(
                label = AppText.t("theme_saturation_value", (saturation * 100).roundToInt().toString()),
                value = saturation,
                valueRange = 0f..1f,
                activeColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))),
                inactiveColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                onValueChange = { next ->
                    saturation = next
                    emit(nextSaturation = next)
                },
            )
            ThemeColorProgressBar(
                label = AppText.t("theme_brightness_value", (brightness * 100).roundToInt().toString()),
                value = brightness,
                valueRange = 0f..1f,
                activeColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))),
                inactiveColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                onValueChange = { next ->
                    brightness = next
                    emit(nextBrightness = next)
                },
            )

            ThemeInputField(
                value = hexInput,
                onValueChange = { value ->
                    hexInput = value
                    parseHexColorOrNull(value)?.let(onColorChange)
                },
                placeholder = AppText.t("theme_color_code"),
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Ascii,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeColorProgressBar(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    inactiveColor: Color,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            thumb = {
                Surface(
                    modifier = Modifier
                        .width(10.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, activeColor),
                    shadowElevation = 1.dp,
                ) {}
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(5.dp),
                    drawStopIndicator = null,
                    thumbTrackGapSize = 0.dp,
                    colors = SliderDefaults.colors(
                        activeTrackColor = activeColor,
                        inactiveTrackColor = inactiveColor,
                    ),
                )
            },
        )
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
