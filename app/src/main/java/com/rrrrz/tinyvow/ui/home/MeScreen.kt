package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MeScreen(
    userPoints: Double,
    currentTheme: Int,
    onSetTheme: (Int) -> Unit,
    onNavigateToLaboratory: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToRedeem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Theme-aware background
            .verticalScroll(rememberScrollState())
    ) {
        // Upper Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Placeholder
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "自律达人",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "ID: 20260322",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Stats Cards overlaps the header slightly
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-40).dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Points card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MeStatItem(value = userPoints.toInt().toString(), label = "当前积分", color = Color(0xFFE91E63))
                    Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.LightGray)
                    MeStatItem(value = "0", label = "累计自律", color = Color(0xFF2196F3))
                    Divider(modifier = Modifier.width(1.dp).height(40.dp), color = Color.LightGray)
                    MeStatItem(value = "1", label = "坚持天数", color = Color(0xFF4CAF50))
                }
            }

            // Action Items
            MeMenuSection("核心入口") {
                MeMenuItem(icon = Icons.Default.EmojiEvents, title = "成果与成就", onClick = onNavigateToAchievements)
                MeMenuItem(icon = Icons.Default.ShoppingCart, title = "商城兑换", onClick = onNavigateToRedeem)
            }

            MeMenuSection("外观主题") {
                ThemeSelectorRow(currentTheme = currentTheme, onThemeSelected = onSetTheme)
            }

            MeMenuSection("高级中心") {
                MeMenuItem(icon = Icons.Default.Science, title = "实验室 (调试工具)", onClick = onNavigateToLaboratory, color = Color(0xFF9C27B0))
                MeMenuItem(icon = Icons.Default.History, title = "使用历史", onClick = { /* TODO */ })
                MeMenuItem(icon = Icons.Default.HelpOutline, title = "帮助与反馈", onClick = { /* TODO */ })
            }
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
        ThemeOption(6, "柑橘晚霞", Color(0xFFFB923C), Color(0xFFFFB37B), Color(0xFFFFD8A8))
    )

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            themes.forEach { theme ->
                val isSelected = currentTheme == theme.id
                Column(
                    modifier = Modifier
                        .clickable { onThemeSelected(theme.id) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .padding(4.dp)
                            .let {
                                if (isSelected) it.then(Modifier.background(Color.White, RoundedCornerShape(12.dp)))
                                else it
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(modifier = Modifier.size(32.dp)) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(theme.p, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(theme.s))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(theme.t, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)))
                        }
                    }
                    Text(
                        theme.name,
                        style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private data class ThemeOption(val id: Int, val name: String, val p: Color, val s: Color, val t: Color)

@Composable
fun MeStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun MeMenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title, 
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp), 
            style = MaterialTheme.typography.labelMedium, 
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun MeMenuItem(icon: ImageVector, title: String, onClick: () -> Unit, color: Color = Color(0xFF444444)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = color)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF333333))
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
    }
}
