package com.rrrrz.tinyvow.ui.rewards

import com.rrrrz.tinyvow.i18n.AppText

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
            containerColor = MaterialTheme.colorScheme.surface,
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
            divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
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
                                animated = false,
                            )
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) tab.palette.accent
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
    val colors = MaterialTheme.colorScheme
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
                color = colors.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
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
            items(
                items = unlockedList,
                key = { it.id }
            ) { achievement ->
                AchievementCard(
                    achievement = achievement,
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
            items(
                items = lockedList,
                key = { it.id }
            ) { achievement ->
                AchievementCard(
                    achievement = achievement,
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
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (subdued) colors.onSurfaceVariant else colors.onSurface
        )
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = colors.surfaceContainerHighest,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = colors.outlineVariant.copy(alpha = 0.75f)
            )
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (subdued) colors.onSurfaceVariant else palette.accent
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = colors.outlineVariant.copy(alpha = if (subdued) 0.55f else 0.75f)
        )
    }
}

// ──────── 等级头部装饰 ────────

@Composable
private fun TierHeader(tab: TierTab, unlocked: Int, total: Int) {
    val progress = if (total > 0) unlocked.toFloat() / total else 0f
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f)),
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
                    animated = false,
                )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppText.t("achievement_value_achievements", tab.label),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
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
                    color = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                TierProgressBar(
                    progress = progress,
                    palette = tab.palette,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = AppText.t("achievement_value_value_completed", unlocked, total),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
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
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.surfaceContainerHighest)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(999.dp))
                .background(
                    color = palette.accentStrong
                )
        )
    }
}


// ──────── 单个成就卡片 ────────

@Composable
private fun AchievementCard(
    achievement: AchievementEntity,
    achievementProgress: AchievementProgress,
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

// ──────── 已解锁卡片 ────────

@Composable
private fun UnlockedAchievementCard(achievement: AchievementEntity) {
    val palette = remember(achievement.tier) { tierPaletteFor(achievement.tier) }
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.72f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AchievementBadge(
                achievement = achievement,
                modifier = Modifier.size(54.dp),
                animated = false,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 1.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = achievement.localizedTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                Text(
                    text = achievement.localizedDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier
                    .widthIn(min = 88.dp)
                    .fillMaxHeight()
                    .padding(vertical = 1.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = colors.surfaceContainerHighest,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.7f))
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = AppText.t("me_unlocked"),
                        tint = palette.accentStrong,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
                achievement.unlockedAt?.let { millis ->
                    Text(
                        text = remember(millis) {
                            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(millis))
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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
    val palette = remember(achievement.tier) { tierPaletteFor(achievement.tier) }
    val colors = MaterialTheme.colorScheme
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

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.72f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 14.dp, vertical = 13.dp),
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
                        .fillMaxSize(),
                    locked = true,
                    animated = false,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 1.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = achievement.localizedTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = achievement.localizedDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    TierProgressBar(
                        progress = progressRatio,
                        palette = palette,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(min = 88.dp)
                    .fillMaxHeight()
                    .padding(vertical = 1.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = colors.surfaceContainerHighest,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.7f))
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
                Text(
                    text = "${formatAchievementValue(currentValue)} / ${formatAchievementValue(targetValue)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
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
