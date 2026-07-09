package com.rrrrz.tinyvow.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TinyVowDetailScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    navigationContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = navigationContentDescription,
                        )
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        TinyVowPageBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            content()
        }
    }
}

@Composable
fun TinyVowPageBackground(
    modifier: Modifier = Modifier,
    useGradient: Boolean = true,
    content: @Composable () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val backgroundModifier =
        if (useGradient) {
            Modifier.background(Brush.verticalGradient(themeColors.pageGradient))
        } else {
            Modifier.background(MaterialTheme.colorScheme.background)
        }
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(backgroundModifier),
    ) {
        content()
    }
}

@Composable
fun TinyVowSection(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TinyVowSectionHeader(
            title = title,
            icon = icon,
            subtitle = subtitle,
            trailing = trailing,
        )
        content()
    }
}

@Composable
fun TinyVowSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val themeColors = LocalThemeColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                TinyVowIconSurface(
                    icon = icon,
                    contentDescription = null,
                    size = 30.dp,
                    iconSize = 17.dp,
                    containerColor = themeColors.baseContainer.copy(alpha = 0.86f),
                    contentColor = themeColors.base,
                )
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.titleLarge,
                color = themeColors.inkStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (trailing != null) {
                Spacer(modifier = Modifier.weight(1f))
                trailing()
            }
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
        }
    }
}

@Composable
fun TinyVowStatusPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalThemeColors.current.base,
    containerColor: Color = color.copy(alpha = 0.12f),
    leadingDot: Boolean = true,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.Pill),
        color = containerColor,
        contentColor = color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (leadingDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color, CircleShape),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun TinyVowEmptyState(
    title: String,
    body: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val themeColors = LocalThemeColors.current
    TinyVowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        color = themeColors.surfaceSoft,
        borderAlpha = 0.36f,
        shadowElevation = TinyVowElevation.Flat,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (icon != null) {
                TinyVowIconSurface(
                    icon = icon,
                    contentDescription = null,
                    containerColor = themeColors.baseContainer,
                    contentColor = themeColors.base,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = themeColors.inkStrong,
            )
            if (!body.isNullOrBlank()) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.inkMuted,
                )
            }
            if (action != null) {
                action()
            }
        }
    }
}

@Composable
fun TinyVowMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = LocalThemeColors.current.base,
    subtitle: String? = null,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.Control),
        color = color.copy(alpha = 0.10f),
        contentColor = color,
        border = tinyVowCardBorder(alpha = 0.22f),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CompactCardHorizontal,
                vertical = TinyVowSpacing.CompactCardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = themeColors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun TinyVowIconSurface(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    iconSize: androidx.compose.ui.unit.Dp = 19.dp,
    containerColor: Color = LocalThemeColors.current.surfaceSoft,
    contentColor: Color = LocalThemeColors.current.base,
) {
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(TinyVowRadius.Control),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                tint = contentColor,
            )
        }
    }
}

@Composable
fun TinyVowSettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    TinyVowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
        borderAlpha = 0.34f,
        shadowElevation = TinyVowElevation.Card,
    ) {
        Column(content = content)
    }
}

@Composable
fun TinyVowSettingsItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconContainerColor: Color = LocalThemeColors.current.surfaceSoft,
    iconContentColor: Color = LocalThemeColors.current.base,
    titleTrailing: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val themeColors = LocalThemeColors.current
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = 14.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TinyVowIconSurface(
                icon = icon,
                contentDescription = null,
                containerColor = iconContainerColor,
                contentColor = iconContentColor,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleSmall,
                        color = themeColors.inkStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    titleTrailing?.invoke(this)
                }
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) {
                trailing()
            }
        }
    }

    if (onClick != null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = Color.Transparent,
            contentColor = LocalContentColor.current,
            onClick = onClick,
            content = content,
        )
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun TinyVowSettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = TinyVowSpacing.CardHorizontal + 48.dp),
        color = LocalThemeColors.current.dividerSoft,
        thickness = 0.5.dp,
    )
}

@Composable
fun TinyVowCardContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = TinyVowSpacing.CardHorizontal,
        vertical = TinyVowSpacing.CardVertical,
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        content = content,
    )
}

@Composable
fun TinyVowSurfaceTextScope(
    contentColor: Color,
    textStyle: androidx.compose.ui.text.TextStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        ProvideTextStyle(textStyle, content)
    }
}
