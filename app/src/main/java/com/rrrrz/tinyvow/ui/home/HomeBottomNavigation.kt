package com.rrrrz.tinyvow.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation

private data class BottomNavDestination(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
internal fun QuietBottomNavigation(
    currentScreen: Screen,
    onSelect: (Screen) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val destinations =
        listOf(
            BottomNavDestination(Screen.HOME, AppText.t("home_home"), Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavDestination(Screen.STATS, AppText.t("home_report"), Icons.Filled.BarChart, Icons.Outlined.BarChart),
            BottomNavDestination(Screen.REWARDS, AppText.t("home_rewards"), Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard),
            BottomNavDestination(Screen.ME, AppText.t("home_me"), Icons.Filled.Person, Icons.Outlined.Person),
        )
    val selectedIndex = destinations.indexOfFirst { it.screen == currentScreen }.coerceAtLeast(0)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = themeColors.surfaceGlass,
        tonalElevation = 0.dp,
        shadowElevation = TinyVowElevation.Card,
    ) {
        Column {
            HorizontalDivider(
                color = themeColors.dividerSoft.copy(alpha = 0.80f),
                thickness = 0.5.dp,
            )
            BoxWithConstraints(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 22.dp, vertical = 7.dp),
            ) {
                val itemWidth = maxWidth / destinations.size
                val indicatorWidth = 28.dp
                val indicatorOffset by animateDpAsState(
                    targetValue = itemWidth * selectedIndex.toFloat() + (itemWidth - indicatorWidth) / 2f,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "bottomNavIndicatorOffset",
                )

                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = indicatorOffset)
                            .size(width = indicatorWidth, height = 3.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(themeColors.base.copy(alpha = 0.86f)),
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    destinations.forEach { destination ->
                        QuietBottomNavigationItem(
                            destination = destination,
                            selected = currentScreen == destination.screen,
                            selectedColor = themeColors.base,
                            unselectedColor = themeColors.navUnselected,
                            modifier = Modifier.width(itemWidth),
                            onClick = { onSelect(destination.screen) },
                        )
                    }
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun QuietBottomNavigationItem(
    destination: BottomNavDestination,
    selected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedColor else unselectedColor,
        animationSpec = tween(durationMillis = 180),
        label = "bottomNavContentColor",
    )
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (selected) {
                        LocalThemeColors.current.navSelectedContainer.copy(alpha = 0.70f)
                    } else {
                        Color.Transparent
                    },
                )
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
            contentDescription = destination.label,
            modifier = Modifier.size(28.dp),
            tint = contentColor,
        )
    }
}
