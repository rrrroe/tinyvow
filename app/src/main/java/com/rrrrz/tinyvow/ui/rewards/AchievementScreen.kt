package com.rrrrz.tinyvow.ui.rewards

import com.rrrrz.tinyvow.i18n.AppText

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrrrz.tinyvow.data.db.AchievementEntity
import com.rrrrz.tinyvow.data.db.AchievementTier
import com.rrrrz.tinyvow.data.repository.AchievementProgress
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.ThemeTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ──────── 等级颜色方案 ────────

// ──────── Tab 定义 ────────

private data class TierTab(
    val tier: Int,
    val emoji: String,
    val label: String,
    val accentColor: Color,
    val bgColor: Color
)

private fun tierTabsForTheme(tokens: ThemeTokens) = listOf(
    TierTab(AchievementTier.BRONZE, "🥉", AppText.t("achievement_bronze"), lerp(tokens.control, tokens.base, 0.28f), tokens.controlContainer.copy(alpha = 0.48f)),
    TierTab(AchievementTier.SILVER, "🥈", AppText.t("achievement_silver"), lerp(tokens.base, tokens.encourage, 0.22f), tokens.baseContainer.copy(alpha = 0.50f)),
    TierTab(AchievementTier.GOLD, "🥇", AppText.t("achievement_gold"), lerp(tokens.encourage, tokens.base, 0.18f), tokens.encourageContainer.copy(alpha = 0.52f)),
    TierTab(AchievementTier.DIAMOND, "💎", AppText.t("achievement_diamond"), tokens.base, tokens.baseContainer.copy(alpha = 0.60f)),
    TierTab(AchievementTier.LEGENDARY, "🌟", AppText.t("achievement_legendary"), tokens.encourage, lerp(tokens.encourageContainer, tokens.controlContainer, 0.32f).copy(alpha = 0.62f)),
)

// ──────── 主屏幕 ────────

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AchievementScreen(
    achievements: List<AchievementEntity>,
    achievementProgress: AchievementProgress = AchievementProgress(),
    onBack: () -> Unit
) {
    val themeTokens = LocalThemeColors.current
    val themedTierTabs = remember(themeTokens) { tierTabsForTheme(themeTokens) }
    val grouped = remember(achievements, themedTierTabs) {
        themedTierTabs.map { tab ->
            tab to achievements.filter { it.tier == tab.tier }
        }
    }

    val pagerState = rememberPagerState(pageCount = { themedTierTabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ──── 等级 Tab 栏 ────
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        height = 3.dp,
                        color = themedTierTabs[pagerState.currentPage].accentColor
                    )
                }
            },
            divider = {}
        ) {
            themedTierTabs.forEachIndexed { index, tab ->
                val isSelected = pagerState.currentPage == index
                
                Tab(
                    selected = isSelected,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = tab.emoji,
                                fontSize = 16.sp
                            )
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) tab.accentColor
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        // ──── Pager 页面 ────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val (tab, list) = grouped[page]
            TierPage(
                tab = tab,
                achievements = list,
                achievementProgress = achievementProgress
            )
        }
    }
}

// ──────── 单个等级 Tab 页面 ────────

@Composable
private fun TierPage(
    tab: TierTab,
    achievements: List<AchievementEntity>,
    achievementProgress: AchievementProgress,
) {
    val unlockedList = achievements.filter { it.isUnlocked }
    val lockedList = achievements.filter { !it.isUnlocked }

    if (achievements.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                AppText.t("achievement_no_achievements_in_this_tier_yet"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 等级头图
        item {
            TierHeader(tab = tab, unlocked = unlockedList.size, total = achievements.size)
        }

        // 已解锁
        if (unlockedList.isNotEmpty()) {
            item {
                Text(
                    AppText.t("achievement_completed"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            itemsIndexed(unlockedList) { index, achievement ->
                AchievementCard(
                    achievement = achievement,
                    animationDelay = index * 80,
                    achievementProgress = achievementProgress
                )
            }
        }

        // 未解锁
        if (lockedList.isNotEmpty()) {
            item {
                Text(
                    AppText.t("achievement_locked"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            itemsIndexed(lockedList) { index, achievement ->
                AchievementCard(
                    achievement = achievement,
                    animationDelay = index * 80,
                    achievementProgress = achievementProgress
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ──────── 等级头部装饰 ────────

@Composable
private fun TierHeader(tab: TierTab, unlocked: Int, total: Int) {
    val progress = if (total > 0) unlocked.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "tierProgress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = tab.bgColor,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 等级大图标
            Text(
                text = tab.emoji,
                fontSize = 40.sp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppText.t("achievement_value_achievements", tab.label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (tab.tier) {
                        AchievementTier.BRONZE -> AppText.t("achievement_the_beginning_of_discipline_every_step_takes_courage")
                        AchievementTier.SILVER -> AppText.t("achievement_your_will_is_growing_stronger_as_you_change")
                        AchievementTier.GOLD -> AppText.t("achievement_proof_of_strength_a_height_few_reach")
                        AchievementTier.DIAMOND -> AppText.t("achievement_you_shine_brightly_and_stand_out")
                        AchievementTier.LEGENDARY -> AppText.t("achievement_a_legend_forged_you_are_the_legend_itself")
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // 进度条
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = tab.accentColor,
                    trackColor = tab.accentColor.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppText.t("achievement_value_value_completed", unlocked, total),
                    style = MaterialTheme.typography.labelSmall,
                    color = tab.accentColor
                )
            }
        }
    }
}


// ──────── 单个成就卡片 ────────

@Composable
private fun AchievementCard(
    achievement: AchievementEntity,
    animationDelay: Int = 0,
    achievementProgress: AchievementProgress,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    ) {
        if (achievement.isUnlocked) {
            UnlockedAchievementCard(achievement)
        } else {
            LockedAchievementCard(
                achievement = achievement,
                achievementProgress = achievementProgress,
            )
        }
    }
}

// ──────── 已解锁卡片 ────────

@Composable
private fun UnlockedAchievementCard(achievement: AchievementEntity) {
    val themeTokens = LocalThemeColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "unlocked_${achievement.id}")

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (achievement.tier) {
                    AchievementTier.LEGENDARY -> 1200
                    AchievementTier.DIAMOND -> 1600
                    AchievementTier.GOLD -> 1800
                    else -> 2500
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val rainbowAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (achievement.tier == AchievementTier.LEGENDARY) 1.06f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle"
    )

    val tierBgColor = when (achievement.tier) {
        AchievementTier.LEGENDARY -> lerp(themeTokens.encourageContainer, themeTokens.controlContainer, 0.32f).copy(alpha = 0.62f)
        AchievementTier.GOLD -> themeTokens.encourageContainer.copy(alpha = 0.52f)
        AchievementTier.SILVER -> themeTokens.baseContainer.copy(alpha = 0.50f)
        else -> themeTokens.controlContainer.copy(alpha = 0.48f)
    }

    val tierGradient = when (achievement.tier) {
        AchievementTier.LEGENDARY -> Brush.linearGradient(
            listOf(themeTokens.control, themeTokens.base, themeTokens.encourage, lerp(themeTokens.control, themeTokens.encourage, 0.5f), themeTokens.control),
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 300f, 300f)
        )
        AchievementTier.GOLD -> Brush.linearGradient(
            listOf(themeTokens.encourage, lerp(themeTokens.encourage, Color.White, 0.42f), themeTokens.base, themeTokens.encourage),
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 200f, 200f)
        )
        AchievementTier.SILVER -> Brush.linearGradient(
            listOf(themeTokens.base, lerp(themeTokens.base, Color.White, 0.55f), lerp(themeTokens.base, themeTokens.encourage, 0.35f), themeTokens.base),
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 150f, 150f)
        )
        else -> Brush.linearGradient(
            listOf(themeTokens.control, lerp(themeTokens.control, themeTokens.base, 0.42f), lerp(themeTokens.control, Color.Black, 0.12f)),
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 120f, 120f)
        )
    }
    val legendaryRingColors = listOf(
        themeTokens.control,
        themeTokens.base,
        themeTokens.encourage,
        lerp(themeTokens.base, themeTokens.encourage, 0.55f),
        lerp(themeTokens.control, themeTokens.encourage, 0.50f),
        themeTokens.control,
    )

    val cardModifier = Modifier.fillMaxWidth()

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = cardModifier,
            shape = RoundedCornerShape(20.dp),
            color = tierBgColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Box(Modifier.fillMaxSize()) {
                // 背景全息光泽流光
                if (achievement.tier >= AchievementTier.GOLD) {
                    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.2f }) {
                        drawRect(brush = tierGradient)
                    }
                }
                
                // 全局粒子效果 (更高级动效)
                if (achievement.tier == AchievementTier.LEGENDARY || achievement.tier == AchievementTier.DIAMOND) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val baseColor = if (achievement.tier == AchievementTier.LEGENDARY) themeTokens.encourage else themeTokens.base
                        for (i in 0..8) {
                            val x = (size.width * 0.1f * i + shimmerOffset * 0.2f) % size.width
                            val y = (sin((x + shimmerOffset) * 0.05f) * 20f + size.height / 2)
                            drawCircle(
                                color = baseColor.copy(alpha = sparkleAlpha * 0.5f),
                                radius = ((i % 4) + 2).dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 左侧: Emoji 图标 + 等级光环
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (achievement.tier == AchievementTier.LEGENDARY) {
                            // 彩虹旋转光环
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                rotate(rainbowAngle) {
                                    drawCircle(
                                        brush = Brush.sweepGradient(legendaryRingColors),
                                        radius = size.minDimension / 2,
                                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }
                            // 闪烁超级星光粒子
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val radius = size.minDimension / 2 + 6.dp.toPx()
                                for (i in 0..7) {
                                    val angle = (i * 45 + rainbowAngle.toInt()) * PI / 180
                                    val x = center.x + (radius * cos(angle)).toFloat()
                                    val y = center.y + (radius * sin(angle)).toFloat()
                                    drawCircle(
                                        color = legendaryRingColors[i % legendaryRingColors.size]
                                            .copy(alpha = sparkleAlpha),
                                        radius = 3.dp.toPx(),
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        } else {
                            // 渐变圆形底
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(tierGradient)
                            )
                        }

                        Text(
                            text = achievement.iconEmoji,
                            fontSize = if (achievement.tier == AchievementTier.LEGENDARY) 28.sp else 24.sp,
                            modifier = Modifier.graphicsLayer {
                                if (achievement.tier >= AchievementTier.GOLD) {
                                    scaleX = pulseScale * 1.05f
                                    scaleY = pulseScale * 1.05f
                                }
                            }
                        )
                    }

                    // 中间: 名称 + 描述
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = achievement.localizedTitle(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = achievement.localizedDescription(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                        achievement.unlockedAt?.let { millis ->
                            Spacer(modifier = Modifier.height(4.dp))
                            val dateStr = remember(millis) {
                                SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(millis))
                            }
                            Text(
                                text = AppText.t("achievement_value_achievement_unlocked", dateStr),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 右侧: 完成标记
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = AppText.t("me_unlocked"),
                        tint = when (achievement.tier) {
                            AchievementTier.LEGENDARY -> themeTokens.encourage
                            AchievementTier.DIAMOND -> themeTokens.base
                            AchievementTier.GOLD -> lerp(themeTokens.encourage, themeTokens.base, 0.18f)
                            AchievementTier.SILVER -> lerp(themeTokens.base, themeTokens.encourage, 0.22f)
                            else -> lerp(themeTokens.control, themeTokens.base, 0.28f)
                        },
                        modifier = Modifier.size(32.dp).graphicsLayer {
                            if (achievement.tier == AchievementTier.LEGENDARY) {
                                scaleX = 1f + (sparkleAlpha - 0.5f) * 0.2f
                                scaleY = scaleX
                            }
                        }
                    )
                }
            }
        }
    }
}

// ──────── 未解锁卡片 ────────

@Composable
private fun LockedAchievementCard(
    achievement: AchievementEntity,
    achievementProgress: AchievementProgress,
) {
    // 解析目标进度
    val (type, targetValue) = remember(achievement.requirement) {
        try {
            val json = org.json.JSONObject(achievement.requirement)
            json.optString("type") to json.optDouble("value", 1.0)
        } catch (e: Exception) {
            "" to 1.0
        }
    }

    // 计算当前进度
    val currentValue = remember(type, achievementProgress) {
        when (type) {
            "points" -> achievementProgress.earnedPointsTotal
            "redeem_points" -> achievementProgress.redeemedPointsTotal
            "control_days" -> achievementProgress.controlDaysTotal.toDouble()
            "control_streak" -> achievementProgress.controlStreak.toDouble()
            "encourage_days" -> achievementProgress.encourageDaysTotal.toDouble()
            "encourage_streak" -> achievementProgress.encourageStreak.toDouble()
            else -> 0.0
        }
    }

    val progressRatio = (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
    
    // 进度条动画
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "locked_progress"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = 0.85f },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.iconEmoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .blur(2.dp)
                        .graphicsLayer { alpha = 0.5f }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.localizedTitle(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = achievement.localizedDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                // 进度指示器
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${currentValue.toLong()} / ${targetValue.toLong()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                Icons.Default.Lock,
                contentDescription = AppText.t("achievement_locked_2"),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun AchievementEntity.localizedTitle(): String {
    val key = "achievement_${id.lowercase()}_title"
    val value = AppText.t(key)
    return if (value == key) title else value
}

private fun AchievementEntity.localizedDescription(): String {
    val key = "achievement_${id.lowercase()}_desc"
    val value = AppText.t(key)
    return if (value == key) description else value
}
