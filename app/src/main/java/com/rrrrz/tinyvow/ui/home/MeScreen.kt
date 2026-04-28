package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrrrz.tinyvow.R

@Composable
fun MeScreen(
    userPoints: Double,
    currentTheme: Int,
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    dismissedPermissionPrompts: Set<String>,
    onSetTheme: (Int) -> Unit,
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
) {
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
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
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
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
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
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "ID: 20260322",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
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
                    MeStatItem(value = userPoints.toInt().toString(), label = "当前积分", color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    MeStatItem(value = "0", label = "累计自律", color = MaterialTheme.colorScheme.secondary)
                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    MeStatItem(value = "1", label = "坚持天数", color = MaterialTheme.colorScheme.tertiary)
                }
            }

            MeMenuSection("核心入口") {
                MeMenuItem(icon = Icons.Default.EmojiEvents, title = "成果与成就", onClick = onNavigateToAchievements)
                MeMenuItem(icon = Icons.Default.ShoppingCart, title = "商城兑换", onClick = onNavigateToRedeem)
                MeMenuItem(icon = Icons.Default.Settings, title = "权限设置", onClick = { showPermissionSettings = true })
            }

            MeMenuSection("外观主题") {
                ThemeSelectorRow(currentTheme = currentTheme, onThemeSelected = onSetTheme)
            }

            MeMenuSection("高级中心") {
                MeMenuItem(
                    icon = Icons.Default.Science,
                    title = "实验室（调试工具）",
                    onClick = onNavigateToLaboratory,
                    color = MaterialTheme.colorScheme.tertiary,
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
    val statusColor = if (usageAccessGranted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

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
                        text = "已忽略 ${dismissedPermissionPrompts.size} 项：首页暂不显示这些提示，点击“取消忽略”后会重新显示。",
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
fun ThemeSelectorRow(currentTheme: Int, onThemeSelected: (Int) -> Unit) {
    val themes = listOf(
        ThemeOption(0, "云水谣", Color(0xFF8FB9C5), Color(0xFFA6C4CD), Color(0xFFC7D3D9)),
        ThemeOption(1, "竹影摇", Color(0xFF94B49F), Color(0xFFB1C4B8), Color(0xFFC9D6CE)),
        ThemeOption(2, "初雪辞", Color(0xFFA8B1C2), Color(0xFFC2C9D6), Color(0xFFD6DBE3)),
        ThemeOption(3, "檀木禅意", Color(0xFF8E7B6D), Color(0xFFBFAE9F), Color(0xFFABB5A8)),
        ThemeOption(4, "蜜桃气泡", Color(0xFFFF7E9D), Color(0xFFFFB2C1), Color(0xFFFFD1DC)),
        ThemeOption(5, "青柠苏打", Color(0xFF2ECD71), Color(0xFF82E0AA), Color(0xFFA9DFBF)),
        ThemeOption(6, "柑橘晚霞", Color(0xFFFB923C), Color(0xFFFFB37B), Color(0xFFFFD8A8)),
    )

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            themes.forEach { theme ->
                val isSelected = currentTheme == theme.id
                Column(
                    modifier = Modifier
                        .clickable { onThemeSelected(theme.id) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .padding(4.dp)
                            .let {
                                if (isSelected) {
                                    it.then(Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)))
                                } else {
                                    it
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(modifier = Modifier.size(32.dp)) {
                            ThemeStrip(theme.p, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                            ThemeStrip(theme.s)
                            ThemeStrip(theme.t, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        }
                    }
                    Text(
                        theme.name,
                        style = TextStyle(fontSize = 11.sp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.ThemeStrip(color: Color, shape: RoundedCornerShape = RoundedCornerShape(0.dp)) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(color, shape)
    )
}

private data class ThemeOption(val id: Int, val name: String, val p: Color, val s: Color, val t: Color)

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
