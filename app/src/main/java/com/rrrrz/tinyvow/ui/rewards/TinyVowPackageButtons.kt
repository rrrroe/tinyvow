package com.rrrrz.tinyvow.ui.rewards

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: ButtonColors? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
    content: @Composable RowScope.() -> Unit,
) {
    TinyVowButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        tone = TinyVowButtonTone.Primary,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: ButtonColors? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
    content: @Composable RowScope.() -> Unit,
) {
    TinyVowButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: ButtonColors? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
    content: @Composable RowScope.() -> Unit,
) {
    TinyVowButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        content = content,
    )
}

