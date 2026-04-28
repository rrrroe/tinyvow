package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.ThemePresets
import com.rrrrz.tinyvow.ui.theme.ThemeSeed
import com.rrrrz.tinyvow.ui.theme.argbToHex
import com.rrrrz.tinyvow.ui.theme.createCustomTheme

@Composable
fun MeScreen(
    userPoints: Double,
    selectedThemeId: String,
    customThemes: List<ThemeSeed>,
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    dismissedPermissionPrompts: Set<String>,
    onSelectTheme: (String) -> Unit,
    onSaveCustomTheme: (ThemeSeed) -> Unit,
    onDeleteCustomTheme: (String) -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onClearDismissedPermissionPrompts: () -> Unit,
    onNavigateToLaboratory: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToRedeem: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    var showPermissionSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            themeColors.base,
                            themeColors.base.copy(alpha = 0.76f),
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = themeColors.onBase.copy(alpha = 0.14f),
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "自律达人",
                        style = MaterialTheme.typography.titleLarge,
                        color = themeColors.onBase,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "ID: 20260322",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onBase.copy(alpha = 0.78f),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-40).dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MeStatItem(value = userPoints.toInt().toString(), label = "当前积分", color = themeColors.encourage)
                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    MeStatItem(value = "0", label = "累计自律", color = themeColors.control)
                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    MeStatItem(value = "1", label = "坚持天数", color = themeColors.base)
                }
            }

            MeMenuSection("核心入口") {
                MeMenuItem(icon = Icons.Default.EmojiEvents, title = "成果与成就", onClick = onNavigateToAchievements)
                MeMenuItem(icon = Icons.Default.ShoppingCart, title = "商城兑换", onClick = onNavigateToRedeem)
                MeMenuItem(icon = Icons.Default.Settings, title = "权限设置", onClick = { showPermissionSettings = true })
            }

            MeMenuSection("外观主题") {
                MeMenuItem(icon = Icons.Default.Palette, title = "主题管理", onClick = onNavigateToThemeSettings)
            }

            MeMenuSection("高级中心") {
                MeMenuItem(
                    icon = Icons.Default.Science,
                    title = "实验室（调试工具）",
                    onClick = onNavigateToLaboratory,
                    color = themeColors.base,
                )
                MeMenuItem(icon = Icons.Default.History, title = "使用历史", onClick = onNavigateToHistory)
                MeMenuItem(icon = Icons.AutoMirrored.Filled.HelpOutline, title = "帮助与反馈", onClick = {})
            }
        }
    }

    if (showPermissionSettings) {
        PermissionSettingsSheet(
            usageAccessGranted = usageAccessGranted,
            accessibilityServiceEnabled = accessibilityServiceEnabled,
            isAutoStartDismissed = isAutoStartDismissed,
            isIgnoringBattery = isIgnoringBattery,
            notificationPermissionGranted = notificationPermissionGranted,
            dismissedPermissionPrompts = dismissedPermissionPrompts,
            onDismiss = { showPermissionSettings = false },
            onOpenUsageAccessSettings = onOpenUsageAccessSettings,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            onOpenAutoStartSettings = onOpenAutoStartSettings,
            onSetAutoStartDismissed = onSetAutoStartDismissed,
            onRequestBatteryOptimization = onRequestBatteryOptimization,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onClearDismissedPermissionPrompts = onClearDismissedPermissionPrompts,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionSettingsSheet(
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    dismissedPermissionPrompts: Set<String>,
    onDismiss: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onClearDismissedPermissionPrompts: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val statusColor = if (usageAccessGranted) themeColors.encourage else themeColors.control

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "权限设置",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "检查权限状态，或恢复首页已忽略的权限提示。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (dismissedPermissionPrompts.isNotEmpty()) {
                    Button(onClick = onClearDismissedPermissionPrompts) {
                        Text("取消忽略")
                    }
                }
            }

            if (dismissedPermissionPrompts.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Text(
                        text = "已忽略 ${dismissedPermissionPrompts.size} 项：取消忽略后，首页会重新显示这些提示。",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            PermissionProcessList(
                isMenuMode = true,
                usageAccessGranted = usageAccessGranted,
                accessibilityServiceEnabled = accessibilityServiceEnabled,
                isAutoStartDismissed = isAutoStartDismissed,
                isIgnoringBattery = isIgnoringBattery,
                notificationPermissionGranted = notificationPermissionGranted,
                statusColor = statusColor,
                onOpenUsageAccessSettings = onOpenUsageAccessSettings,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                onOpenAutoStartSettings = onOpenAutoStartSettings,
                onSetAutoStartDismissed = onSetAutoStartDismissed,
                onRequestBatteryOptimization = onRequestBatteryOptimization,
                onRequestNotificationPermission = onRequestNotificationPermission,
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ThemeManager(
    selectedThemeId: String,
    customThemes: List<ThemeSeed>,
    onSelectTheme: (String) -> Unit,
    onSaveCustomTheme: (ThemeSeed) -> Unit,
    onDeleteCustomTheme: (String) -> Unit,
) {
    var editingTheme by remember { mutableStateOf<ThemeSeed?>(null) }
    val allThemes = ThemePresets + customThemes

    Column(modifier = Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            allThemes.forEach { theme ->
                ThemePreviewCard(
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
            AddThemeCard {
                editingTheme = createCustomTheme(
                    name = "自定义主题",
                    controlColor = ThemePresets.first().controlColor,
                    encourageColor = ThemePresets.first().encourageColor,
                    baseColor = ThemePresets.first().baseColor,
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeLegendDot("限制", LocalThemeColors.current.control)
            ThemeLegendDot("鼓励", LocalThemeColors.current.encourage)
            ThemeLegendDot("基础", LocalThemeColors.current.base)
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
private fun RowScope.ThemePreviewCard(
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
            .width(156.dp)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (selected) 2.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = if (selected) 0.72f else 0.46f)),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.height(26.dp).clip(RoundedCornerShape(8.dp))) {
                ThemeStrip(Color(theme.controlColor))
                ThemeStrip(Color(theme.encourageColor))
                ThemeStrip(Color(theme.baseColor))
            }
            Text(
                text = theme.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onCopy, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(16.dp))
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp), tint = LocalThemeColors.current.control)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.AddThemeCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(126.dp)
            .height(118.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = "新建", tint = MaterialTheme.colorScheme.primary)
            Text("新建主题", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                ColorSliderGroup("限制色", control, onColorChange = { control = it })
                ColorSliderGroup("鼓励色", encourage, onColorChange = { encourage = it })
                ColorSliderGroup("基础色", base, onColorChange = { base = it })
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
private fun ColorSliderGroup(
    label: String,
    color: Int,
    onColorChange: (Int) -> Unit,
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(color) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
    }

    fun emit() {
        onColorChange(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(color))
            )
            Text("$label ${argbToHex(color)}", style = MaterialTheme.typography.labelMedium)
        }
        Slider(value = hue, onValueChange = { hue = it; emit() }, valueRange = 0f..360f)
        Slider(value = saturation, onValueChange = { saturation = it; emit() }, valueRange = 0.12f..0.82f)
        Slider(value = value, onValueChange = { value = it; emit() }, valueRange = 0.36f..0.92f)
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

@Composable
private fun ThemeLegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MeStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MeMenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun MeMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = color)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
