package com.rrrrz.tinyvow.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import kotlinx.coroutines.launch

private data class WelcomeIntroPage(
    val icon: ImageVector,
    val titleKey: String,
    val bodyKey: String,
    val pointKeys: List<String>,
    val accent: WelcomeIntroAccent,
)

private enum class WelcomeIntroAccent {
    Base,
    Control,
    Encourage,
}

@Composable
fun WelcomeIntroScreen(
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val coroutineScope = rememberCoroutineScope()
    val pages = remember {
        listOf(
            WelcomeIntroPage(
                icon = Icons.Default.Shield,
                titleKey = "welcome_intro_page_welcome_title",
                bodyKey = "welcome_intro_page_welcome_body",
                pointKeys =
                    listOf(
                        "welcome_intro_page_welcome_point_control",
                        "welcome_intro_page_welcome_point_encourage",
                        "welcome_intro_page_welcome_point_local",
                    ),
                accent = WelcomeIntroAccent.Base,
            ),
            WelcomeIntroPage(
                icon = Icons.Default.Shield,
                titleKey = "welcome_intro_page_control_title",
                bodyKey = "welcome_intro_page_control_body",
                pointKeys =
                    listOf(
                        "welcome_intro_page_control_point_groups",
                        "welcome_intro_page_control_point_periods",
                        "welcome_intro_page_control_point_overlay",
                    ),
                accent = WelcomeIntroAccent.Control,
            ),
            WelcomeIntroPage(
                icon = Icons.Default.Timer,
                titleKey = "welcome_intro_page_encourage_title",
                bodyKey = "welcome_intro_page_encourage_body",
                pointKeys =
                    listOf(
                        "welcome_intro_page_encourage_point_points",
                        "welcome_intro_page_encourage_point_goal",
                        "welcome_intro_page_encourage_point_special",
                    ),
                accent = WelcomeIntroAccent.Encourage,
            ),
            WelcomeIntroPage(
                icon = Icons.Default.CardGiftcard,
                titleKey = "welcome_intro_page_rewards_title",
                bodyKey = "welcome_intro_page_rewards_body",
                pointKeys =
                    listOf(
                        "welcome_intro_page_rewards_point_inventory",
                        "welcome_intro_page_rewards_point_custom",
                        "welcome_intro_page_rewards_point_reports",
                    ),
                accent = WelcomeIntroAccent.Base,
            ),
            WelcomeIntroPage(
                icon = Icons.Default.Lock,
                titleKey = "welcome_intro_page_privacy_title",
                bodyKey = "welcome_intro_page_privacy_body",
                pointKeys =
                    listOf(
                        "welcome_intro_page_privacy_point_usage",
                        "welcome_intro_page_privacy_point_accessibility",
                        "welcome_intro_page_privacy_point_export",
                    ),
                accent = WelcomeIntroAccent.Control,
            ),
        )
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.lastIndex

    BackHandler {
        if (pagerState.currentPage > 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 2.dp),
                pageSpacing = 18.dp,
            ) { page ->
                WelcomeIntroPageContent(
                    page = pages[page],
                    pageNumber = page + 1,
                    pageCount = pages.size,
                )
            }

            WelcomeIntroPageDots(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TinyVowRadius.Control),
            ) {
                if (isLastPage) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(if (isLastPage) AppText.t("welcome_intro_enter") else AppText.t("welcome_intro_next"))
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("welcome_intro_skip"))
            }
        }
    }
}

@Composable
private fun WelcomeIntroPageContent(
    page: WelcomeIntroPage,
    pageNumber: Int,
    pageCount: Int,
) {
    val themeColors = LocalThemeColors.current
    val accent = when (page.accent) {
        WelcomeIntroAccent.Base -> themeColors.base
        WelcomeIntroAccent.Control -> themeColors.control
        WelcomeIntroAccent.Encourage -> themeColors.encourage
    }
    val accentContainer = when (page.accent) {
        WelcomeIntroAccent.Base -> themeColors.baseContainer
        WelcomeIntroAccent.Control -> themeColors.controlContainer
        WelcomeIntroAccent.Encourage -> themeColors.encourageContainer
    }
    val accentContent = when (page.accent) {
        WelcomeIntroAccent.Base -> themeColors.onBaseContainer
        WelcomeIntroAccent.Control -> themeColors.onControlContainer
        WelcomeIntroAccent.Encourage -> themeColors.onEncourageContainer
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = accentContainer,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = AppText.t("welcome_intro_page_count", pageNumber, pageCount),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accentContent,
                )
            }
            WelcomeIntroFeatureMark(
                icon = page.icon,
                accent = accent,
                accentContainer = accentContainer,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = AppText.t(page.titleKey),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = themeColors.inkStrong,
        )
        Text(
            text = AppText.t(page.bodyKey),
            style = MaterialTheme.typography.bodyLarge,
            color = themeColors.ink,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            page.pointKeys.forEach { pointKey ->
                WelcomeIntroPoint(
                    text = AppText.t(pointKey),
                    accent = accent,
                )
            }
        }

        if (pageNumber == pageCount) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TinyVowRadius.ItemCard),
                color = themeColors.baseContainer,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Text(
                    text = AppText.t("welcome_intro_final_note"),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.onBaseContainer,
                )
            }
        }
    }
}

@Composable
private fun WelcomeIntroFeatureMark(
    icon: ImageVector,
    accent: Color,
    accentContainer: Color,
) {
    Box(
        modifier = Modifier.size(86.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(42.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
        )
        Surface(
            modifier = Modifier.size(62.dp),
            shape = RoundedCornerShape(22.dp),
            color = accentContainer,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun WelcomeIntroPoint(
    text: String,
    accent: Color,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = themeColors.ink,
            )
        }
    }
}

@Composable
private fun WelcomeIntroPageDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == currentPage) 20.dp else 7.dp, height = 7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (index == currentPage) {
                            themeColors.base
                        } else {
                            themeColors.inkFaint.copy(alpha = 0.34f)
                        },
                    ),
            )
        }
    }
}
