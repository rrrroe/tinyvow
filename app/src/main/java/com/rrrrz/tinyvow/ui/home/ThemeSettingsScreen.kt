package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.ThemePresets
import com.rrrrz.tinyvow.ui.theme.ThemeSeed
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import com.rrrrz.tinyvow.ui.theme.localizedName
import com.rrrrz.tinyvow.ui.theme.resolveThemeSeed
import com.rrrrz.tinyvow.ui.theme.selectedThemeDisplayName
import com.rrrrz.tinyvow.ui.theme.themeTokensFromSeed

@OptIn(ExperimentalMaterial3Api::class)
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
    val allThemes = ThemePresets
    val themeColors = LocalThemeColors.current
    val effectiveSelectedThemeId = resolveThemeSeed(selectedThemeId, customThemes).id
    val currentThemeName = selectedThemeDisplayName(selectedThemeId, customThemes)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppText.t("me_appearance_theme"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("group_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = TinyVowSpacing.PageHorizontal),
            contentPadding = PaddingValues(
                top = TinyVowSpacing.PageTop,
                bottom = TinyVowSpacing.PageTop,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            item {
                TinyVowCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    borderAlpha = 0.18f,
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = TinyVowSpacing.CardHorizontal,
                            vertical = TinyVowSpacing.CardVertical,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = AppText.t("theme_manage_preset_and_custom_three_color_themes"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                        )
                        Text(
                            text = AppText.t("theme_current_selection", currentThemeName),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            items(allThemes, key = { it.id }) { theme ->
                ThemeStyleCard(
                    theme = theme,
                    selected = effectiveSelectedThemeId == theme.id,
                    locked = false,
                    onClick = { onSelectTheme(theme.id) },
                )
            }
        }
    }
}

@Composable
private fun ThemeStyleCard(
    theme: ThemeSeed,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
) {
    val tokens = themeTokensFromSeed(theme)
    val borderColor = if (selected) tokens.base else tokens.colorScheme.outlineVariant

    TinyVowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(TinyVowRadius.Card),
        color = tokens.colorScheme.surface,
        borderAlpha = 0f,
        shadowElevation = if (selected) TinyVowElevation.SelectedCard else TinyVowElevation.Card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(TinyVowRadius.Card))
                .border(
                    BorderStroke(1.dp, borderColor.copy(alpha = if (selected) 0.72f else 0.28f)),
                    RoundedCornerShape(TinyVowRadius.Card),
                )
                .background(tokens.colorScheme.background.copy(alpha = 0.36f))
                .padding(TinyVowSpacing.CardHorizontal),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ThemeMark(theme = theme, selected = selected, locked = locked)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = theme.localizedName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.inkStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = AppText.t("theme_${theme.id}_description"),
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.inkMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = tokens.base)
                }
            }

            ThemeRoleSwatches(theme = theme)
        }
    }
}

@Composable
private fun ThemeMark(
    theme: ThemeSeed,
    selected: Boolean,
    locked: Boolean,
) {
    val tokens = themeTokensFromSeed(theme)
    Surface(
        modifier = Modifier.size(42.dp),
        shape = RoundedCornerShape(14.dp),
        color = tokens.base,
        shadowElevation = if (selected) 3.dp else 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 11.dp, top = 11.dp)
                    .size(if (locked) 8.dp else 10.dp)
                    .clip(CircleShape)
                    .background(tokens.control),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 11.dp, bottom = 11.dp)
                    .size(if (locked) 8.dp else 10.dp)
                    .clip(CircleShape)
                    .background(tokens.encourage),
            )
        }
    }
}

@Composable
private fun ThemeRoleSwatches(theme: ThemeSeed) {
    val tokens = themeTokensFromSeed(theme)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ThemeRoleSwatch(
            label = AppText.t("theme_control_role"),
            color = tokens.control,
            modifier = Modifier.weight(1f),
        )
        ThemeRoleSwatch(
            label = AppText.t("theme_encourage_role"),
            color = tokens.encourage,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ThemeRoleSwatch(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = themeColors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
