package com.rrrrz.tinyvow.ui.rewards

import com.rrrrz.tinyvow.i18n.AppText

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrrrz.tinyvow.data.db.AchievementEntity
import com.rrrrz.tinyvow.data.db.AchievementTier
import com.rrrrz.tinyvow.data.repository.AchievementProgress
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class TierPalette(
    val accent: Color,
    val accentStrong: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val border: Color,
    val muted: Color,
    val track: Color,
    val pageGlow: Color,
)

private fun tierPaletteFor(tier: Int): TierPalette = when (tier) {
    AchievementTier.BRONZE -> TierPalette(
        accent = Color(0xFF7A5426),
        accentStrong = Color(0xFFD8923C),
        surface = Color(0xFFFFF7EF),
        surfaceRaised = Color(0xFFFFF0DF),
        border = Color(0xFFE5C59C),
        muted = Color(0xFF8B7153),
        track = Color(0xFFF1E1CF),
        pageGlow = Color(0x33E6A14A),
    )
    AchievementTier.SILVER -> TierPalette(
        accent = Color(0xFF3E5D88),
        accentStrong = Color(0xFF77A9E8),
        surface = Color(0xFFF5F8FF),
        surfaceRaised = Color(0xFFEBF1FF),
        border = Color(0xFFC7D5EE),
        muted = Color(0xFF697C99),
        track = Color(0xFFDEE7F7),
        pageGlow = Color(0x336AA6FF),
    )
    AchievementTier.GOLD -> TierPalette(
        accent = Color(0xFF556E72),
        accentStrong = Color(0xFFE0A22A),
        surface = Color(0xFFF4FBFB),
        surfaceRaised = Color(0xFFE8F5F4),
        border = Color(0xFFC2DDDB),
        muted = Color(0xFF6D8386),
        track = Color(0xFFD9ECEA),
        pageGlow = Color(0x33F0B548),
    )
    AchievementTier.DIAMOND -> TierPalette(
        accent = Color(0xFF3B5C96),
        accentStrong = Color(0xFF67B9F7),
        surface = Color(0xFFF4F8FF),
        surfaceRaised = Color(0xFFEAF1FF),
        border = Color(0xFFC5D6F6),
        muted = Color(0xFF687A9B),
        track = Color(0xFFDCE6F8),
        pageGlow = Color(0x336DCBFF),
    )
    AchievementTier.LEGENDARY -> TierPalette(
        accent = Color(0xFF6B4E8F),
        accentStrong = Color(0xFFC07AF3),
        surface = Color(0xFFFBF6FF),
        surfaceRaised = Color(0xFFF1E8FA),
        border = Color(0xFFE2D1F2),
        muted = Color(0xFF8A74A3),
        track = Color(0xFFEADFF6),
        pageGlow = Color(0x33D786FF),
    )
    else -> tierPaletteFor(AchievementTier.BRONZE)
}

// ──────── Tab 定义 ────────

private data class TierTab(
    val tier: Int,
    val label: String,
    val palette: TierPalette,
)

private fun tierTabs() = listOf(
    TierTab(AchievementTier.BRONZE, AppText.t("achievement_bronze"), tierPaletteFor(AchievementTier.BRONZE)),
    TierTab(AchievementTier.SILVER, AppText.t("achievement_silver"), tierPaletteFor(AchievementTier.SILVER)),
    TierTab(AchievementTier.GOLD, AppText.t("achievement_gold"), tierPaletteFor(AchievementTier.GOLD)),
    TierTab(AchievementTier.DIAMOND, AppText.t("achievement_diamond"), tierPaletteFor(AchievementTier.DIAMOND)),
    TierTab(AchievementTier.LEGENDARY, AppText.t("achievement_legendary"), tierPaletteFor(AchievementTier.LEGENDARY)),
)

// ──────── 主屏幕 ────────

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AchievementScreen(
    achievements: List<AchievementEntity>,
    achievementProgress: AchievementProgress = AchievementProgress(),
    onBack: () -> Unit
) {
    val themedTierTabs = remember { tierTabs() }
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
            contentColor = themedTierTabs[pagerState.currentPage].palette.accent,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        height = 3.dp,
                        color = themedTierTabs[pagerState.currentPage].palette.accentStrong
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
                            AchievementBadge(
                                achievementId = representativeAchievementIdForTier(tab.tier),
                                tier = tab.tier,
                                modifier = Modifier.size(22.dp),
                                animated = true,
                            )
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) tab.palette.accent
                                        else tab.palette.muted.copy(alpha = 0.72f)
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
    val textColor = Color(0xFFDDE8F5)
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
                color = textColor.copy(alpha = 0.76f)
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 等级头图
        item {
            TierHeader(tab = tab, unlocked = unlockedList.size, total = achievements.size)
        }

        // 已解锁
        if (unlockedList.isNotEmpty()) {
            item {
                AchievementSectionHeader(
                    title = AppText.t("achievement_completed"),
                    count = unlockedList.size,
                    palette = tab.palette,
                    subdued = false,
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
                AchievementSectionHeader(
                    title = AppText.t("achievement_locked"),
                    count = lockedList.size,
                    palette = tab.palette,
                    subdued = true,
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

@Composable
private fun AchievementSectionHeader(
    title: String,
    count: Int,
    palette: TierPalette,
    subdued: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (subdued) palette.muted else palette.accent
        )
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (subdued) palette.surfaceRaised.copy(alpha = 0.72f) else palette.accent.copy(alpha = 0.14f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (subdued) palette.border.copy(alpha = 0.32f) else palette.accentStrong.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (subdued) palette.muted else palette.accent
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = palette.border.copy(alpha = if (subdued) 0.16f else 0.24f)
        )
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
        color = tab.palette.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, tab.palette.border.copy(alpha = 0.34f)),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 等级大图标
                AchievementBadge(
                    achievementId = representativeAchievementIdForTier(tab.tier),
                    tier = tab.tier,
                    modifier = Modifier.size(50.dp),
                    animated = true,
                )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppText.t("achievement_value_achievements", tab.label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tab.palette.accent,
                )
                Spacer(modifier = Modifier.height(2.dp))
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
                    color = tab.palette.muted
                )
                Spacer(modifier = Modifier.height(8.dp))

                TierProgressBar(
                    progress = animatedProgress,
                    palette = tab.palette,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = AppText.t("achievement_value_value_completed", unlocked, total),
                    style = MaterialTheme.typography.labelSmall,
                    color = tab.palette.muted
                )
            }
        }
    }
}

@Composable
private fun TierProgressBar(
    progress: Float,
    palette: TierPalette,
    modifier: Modifier = Modifier,
) {
    val sheenTransition = rememberInfiniteTransition(label = "achievement_progress_sheen")
    val sheen by sheenTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "achievement_progress_sheen_value"
    )
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(palette.track)
            .border(1.dp, palette.border.copy(alpha = 0.24f), RoundedCornerShape(999.dp))
    ) {
        val progressWidth = maxWidth * progress.coerceIn(0f, 1f)
        val sheenStart = maxWidth.value * sheen
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(progressWidth)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            palette.accentStrong,
                            lerp(palette.accent, Color.White, 0.18f),
                            palette.accent,
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.22f),
                                Color.Transparent,
                            ),
                            start = Offset(sheenStart, 0f),
                            end = Offset(sheenStart + 90f, 200f),
                        )
                    )
            )
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
    val palette = remember(achievement.tier) { tierPaletteFor(achievement.tier) }
    val infiniteTransition = rememberInfiniteTransition(label = "unlocked_${achievement.id}")

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (achievement.tier) {
                    AchievementTier.LEGENDARY -> 2600
                    AchievementTier.DIAMOND -> 3200
                    AchievementTier.GOLD -> 3600
                    else -> 4200
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle"
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = palette.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.border.copy(alpha = 0.38f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            palette.pageGlow.copy(alpha = 0.06f),
                            Color.Transparent,
                            palette.accentStrong.copy(alpha = 0.08f + sparkleAlpha * 0.05f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                        start = Offset(shimmerOffset, 0f),
                        end = Offset(shimmerOffset + 180f, size.height)
                    )
                )
            }

            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AchievementBadge(
                    achievement = achievement,
                    modifier = Modifier.size(54.dp),
                    animated = true,
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = achievement.localizedTitle(),
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.accent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = achievement.localizedDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(
                    modifier = Modifier.widthIn(min = 78.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = palette.accent.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentStrong.copy(alpha = 0.24f))
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = AppText.t("me_unlocked"),
                            tint = palette.accentStrong,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                                .graphicsLayer {
                                    if (achievement.tier == AchievementTier.LEGENDARY) {
                                        scaleX = 1f + (sparkleAlpha - 0.5f) * 0.12f
                                        scaleY = scaleX
                                    }
                                }
                        )
                    }
                    achievement.unlockedAt?.let { millis ->
                        Spacer(modifier = Modifier.height(7.dp))
                        Text(
                            text = remember(millis) {
                                SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(millis))
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.muted,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
    val palette = remember(achievement.tier) { tierPaletteFor(achievement.tier) }
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
            .graphicsLayer { alpha = 0.96f },
        shape = RoundedCornerShape(22.dp),
        color = palette.surface.copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.border.copy(alpha = 0.3f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                AchievementBadge(
                    achievement = achievement,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(1.dp),
                    locked = true,
                    animated = true,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.localizedTitle(),
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.accent.copy(alpha = 0.94f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = achievement.localizedDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted.copy(alpha = 0.88f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                TierProgressBar(
                    progress = animatedProgress,
                    palette = palette,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )
            }

            Column(
                modifier = Modifier.widthIn(min = 78.dp),
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = palette.surfaceRaised.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.border.copy(alpha = 0.22f))
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = AppText.t("achievement_locked_2"),
                        tint = palette.muted.copy(alpha = 0.88f),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "${formatAchievementValue(currentValue)} / ${formatAchievementValue(targetValue)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.accent,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatAchievementValue(value: Double): String {
    val longValue = value.toLong()
    return String.format(Locale.getDefault(), "%,d", longValue)
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
