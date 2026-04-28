package com.rrrrz.tinyvow.ui.home

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("外观主题", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "管理预设和自定义三色主题",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = {
                    editingTheme = createCustomTheme(
                        name = "自定义主题",
                        controlColor = ThemePresets.first().controlColor,
                        encourageColor = ThemePresets.first().encourageColor,
                        baseColor = ThemePresets.first().baseColor,
                    )
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("新建")
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
                                name = "${theme.name} 自定义",
                                controlColor = theme.controlColor,
                                encourageColor = theme.encourageColor,
                                baseColor = theme.baseColor,
                            )
                        }
                    },
                    onCopy = {
                        editingTheme = createCustomTheme(
                            name = "${theme.name} 副本",
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
                    if (theme.isCustom) "自定义" else "预设",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(theme.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "限制 ${argbToHex(theme.controlColor)} · 鼓励 ${argbToHex(theme.encourageColor)} · 基础 ${argbToHex(theme.baseColor)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "复制")
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = LocalThemeColors.current.control)
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
        title = { Text("编辑主题", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("主题名称") },
                    singleLine = true,
                )
                ColorControl("限制色", "限制组、阻断、超额和风险状态", control, onColorChange = { control = it })
                ColorControl("鼓励色", "鼓励组、积分、奖励和达成状态", encourage, onColorChange = { encourage = it })
                ColorControl("基础色", "导航、按钮、卡片和普通组件", base, onColorChange = { base = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialTheme.copy(
                            name = name.ifBlank { "自定义主题" },
                            controlColor = control,
                            encourageColor = encourage,
                            baseColor = base,
                            isCustom = true,
                        )
                    )
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
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
            label = { Text("颜色代码") },
            placeholder = { Text("#AABBCC") },
            singleLine = true,
        )
        Text("色相 Hue：${hue.toInt()}°", style = MaterialTheme.typography.labelSmall)
        Slider(value = hue, onValueChange = { hue = it; emit() }, valueRange = 0f..360f)
        Text("饱和度 Saturation：${(saturation * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
        Slider(value = saturation, onValueChange = { saturation = it; emit() }, valueRange = 0.05f..0.80f)
        Text("明暗 Brightness：${(brightness * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
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
