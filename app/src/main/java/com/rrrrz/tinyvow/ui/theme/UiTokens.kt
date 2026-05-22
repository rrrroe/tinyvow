package com.rrrrz.tinyvow.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
