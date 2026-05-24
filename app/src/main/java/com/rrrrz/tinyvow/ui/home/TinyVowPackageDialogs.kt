package com.rrrrz.tinyvow.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import com.rrrrz.tinyvow.ui.theme.TinyVowAlertDialog
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun AlertDialog(
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
    TinyVowAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
    )
}
