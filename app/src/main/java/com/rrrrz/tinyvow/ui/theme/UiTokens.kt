package com.rrrrz.tinyvow.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

object TinyVowSpacing {
    val PageHorizontal = 16.dp
    val PageTop = 10.dp
    val SectionGap = 14.dp
    val CardGap = 12.dp
    val CardHorizontal = 16.dp
    val CardVertical = 16.dp
    val CompactCardHorizontal = 12.dp
    val CompactCardVertical = 12.dp
}

object TinyVowRadius {
    val FeaturedCard = 28.dp
    val Card = 24.dp
    val ItemCard = 18.dp
    val Control = 14.dp
    val Pill = 999.dp
}

object TinyVowElevation {
    val Flat = 0.dp
    val Card = 1.dp
    val FeaturedCard = 2.dp
    val SelectedCard = 3.dp
}

@Composable
fun tinyVowCardBorder(alpha: Float = 0.28f): BorderStroke =
    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha))

@Composable
fun TinyVowCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(TinyVowRadius.Card),
    color: Color = MaterialTheme.colorScheme.surface,
    borderAlpha: Float = 0.28f,
    shadowElevation: Dp = TinyVowElevation.Card,
    tonalElevation: Dp = TinyVowElevation.Flat,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        border = tinyVowCardBorder(borderAlpha),
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation,
        content = content,
    )
}

enum class TinyVowButtonTone {
    Neutral,
    Primary,
    Danger,
}

@Composable
fun TinyVowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    tone: TinyVowButtonTone = TinyVowButtonTone.Neutral,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val selectedContainer = MaterialTheme.colorScheme.primaryContainer
    val selectedContent = MaterialTheme.colorScheme.primary
    val neutralContainer = MaterialTheme.colorScheme.surface
    val neutralContent = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryContainer = themeColors.baseContainer
    val primaryContent = themeColors.base
    val dangerContainer = MaterialTheme.colorScheme.errorContainer
    val dangerContent = MaterialTheme.colorScheme.error
    val disabledContainer = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    val disabledContent = themeColors.inkMuted.copy(alpha = 0.58f)

    val containerColor =
        when {
            !enabled -> disabledContainer
            selected -> selectedContainer
            tone == TinyVowButtonTone.Primary -> primaryContainer
            tone == TinyVowButtonTone.Danger -> dangerContainer
            else -> neutralContainer
        }
    val contentColor =
        when {
            !enabled -> disabledContent
            selected -> selectedContent
            tone == TinyVowButtonTone.Primary -> primaryContent
            tone == TinyVowButtonTone.Danger -> dangerContent
            else -> neutralContent
        }
    val border =
        if (selected || tone != TinyVowButtonTone.Neutral || !enabled) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f))
        }

    Surface(
        modifier = modifier.defaultMinSize(minHeight = 44.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        border = border,
        enabled = enabled,
        onClick = onClick,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content,
                )
            }
        }
    }
}

@Composable
fun TinyVowAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
    containerColor: Color? = null,
    iconContentColor: Color? = null,
    titleContentColor: Color? = null,
    textContentColor: Color? = null,
    tonalElevation: Dp = TinyVowElevation.Flat,
    properties: DialogProperties = DialogProperties(),
) {
    val themeColors = LocalThemeColors.current
    val resolvedContainer = containerColor ?: MaterialTheme.colorScheme.surface
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Box(modifier = Modifier.padding(top = 2.dp)) {
                confirmButton()
            }
        },
        modifier = modifier,
        dismissButton = dismissButton?.let { button ->
            {
                Box(modifier = Modifier.padding(top = 2.dp)) {
                    button()
                }
            }
        },
        icon = icon,
        title = title?.let { titleContent ->
            {
                ProvideTextStyle(
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                ) {
                    titleContent()
                }
            }
        },
        text = text?.let { textContent ->
            {
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    textContent()
                }
            }
        },
        shape = shape,
        containerColor = resolvedContainer,
        iconContentColor = iconContentColor ?: themeColors.base,
        titleContentColor = titleContentColor ?: themeColors.inkStrong,
        textContentColor = textContentColor ?: themeColors.ink,
        tonalElevation = tonalElevation,
        properties = properties,
    )
}

@Composable
fun TinyVowSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(horizontal = TinyVowSpacing.PageHorizontal),
    ) { snackbarData ->
        Snackbar(
            snackbarData = snackbarData,
            shape = RoundedCornerShape(18.dp),
            containerColor = themeColors.baseContainer,
            contentColor = themeColors.inkStrong,
            actionColor = themeColors.base,
            dismissActionContentColor = themeColors.inkMuted,
        )
    }
}

@Composable
fun TinyVowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    tone: TinyVowButtonTone = TinyVowButtonTone.Neutral,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
) {
    TinyVowButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        selected = selected,
        tone = tone,
        contentPadding = contentPadding,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
