package com.rrrrz.tinyvow.ui.home

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.i18n.AppText

internal enum class ManualColorPickerSource {
    ICON,
    PALETTE,
}

@Composable
internal fun SharedManualColorPickerDialog(
    title: String,
    selectedColor: Color?,
    hexValue: String,
    selectedXRatio: Float?,
    selectedYRatio: Float?,
    source: ManualColorPickerSource,
    iconBitmap: ImageBitmap?,
    iconSourceBitmap: Bitmap?,
    iconContent: (@Composable BoxScope.() -> Unit)? = null,
    onSourceChange: (ManualColorPickerSource) -> Unit,
    onColorPicked: (Color) -> Unit,
    onTapPositionChanged: (Float, Float) -> Unit,
    onHexChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(
                    onClick = {
                        onSourceChange(
                            if (source == ManualColorPickerSource.ICON) {
                                ManualColorPickerSource.PALETTE
                            } else {
                                ManualColorPickerSource.ICON
                            },
                        )
                    },
                ) {
                    Icon(
                        imageVector =
                            if (source == ManualColorPickerSource.ICON) {
                                Icons.Default.Palette
                            } else {
                                Icons.Default.TouchApp
                            },
                        contentDescription =
                            if (source == ManualColorPickerSource.ICON) {
                                AppText.t("app_color_picker_switch_to_palette")
                            } else {
                                AppText.t("app_color_picker_switch_to_icon")
                            },
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ManualColorPickSurface(
                        source = source,
                        iconBitmap = iconBitmap,
                        iconSourceBitmap = iconSourceBitmap,
                        iconContent = iconContent,
                        selectedXRatio = selectedXRatio,
                        selectedYRatio = selectedYRatio,
                        onTapPositionChanged = onTapPositionChanged,
                        onColorPicked = onColorPicked,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ManualColorPreviewBlock(color = selectedColor)
                    Text(
                        text =
                            if (source == ManualColorPickerSource.ICON) {
                                AppText.t("app_color_manual_picker_icon_hint")
                            } else {
                                AppText.t("app_color_manual_picker_palette_hint")
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedTextField(
                    value = hexValue,
                    onValueChange = onHexChanged,
                    singleLine = true,
                    label = { Text(AppText.t("app_color_manual_hex_label")) },
                    supportingText = {
                        Text(
                            text = AppText.t("app_color_manual_hex_hint"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = selectedColor != null,
                onClick = { selectedColor?.let(onConfirm) },
            ) {
                Text(AppText.t("app_color_save_selection_confirm"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("action_cancel"))
            }
        },
    )
}

@Composable
private fun ManualColorPickSurface(
    source: ManualColorPickerSource,
    iconBitmap: ImageBitmap?,
    iconSourceBitmap: Bitmap?,
    iconContent: (@Composable BoxScope.() -> Unit)?,
    selectedXRatio: Float?,
    selectedYRatio: Float?,
    onTapPositionChanged: (Float, Float) -> Unit,
    onColorPicked: (Color) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(18.dp),
                )
                .manualColorPointerInput(
                    source = source,
                    iconSourceBitmap = iconSourceBitmap,
                    onTapPositionChanged = onTapPositionChanged,
                    onColorPicked = onColorPicked,
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (source == ManualColorPickerSource.ICON) {
            if (iconContent != null) {
                iconContent()
            } else if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                )
            }
        } else {
            ManualColorPaletteCanvas(modifier = Modifier.fillMaxSize())
        }
        if (selectedXRatio != null && selectedYRatio != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(
                    x = size.width * selectedXRatio,
                    y = size.height * selectedYRatio,
                )
                drawCircle(
                    color = Color.White,
                    radius = 10.dp.toPx(),
                    center = center,
                    style = Stroke(width = 3.dp.toPx()),
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.82f),
                    radius = 10.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
    }
}

private fun Modifier.manualColorPointerInput(
    source: ManualColorPickerSource,
    iconSourceBitmap: Bitmap?,
    onTapPositionChanged: (Float, Float) -> Unit,
    onColorPicked: (Color) -> Unit,
): Modifier =
    pointerInput(source, iconSourceBitmap) {
        fun pick(offset: Offset) {
            val xRatio = (offset.x / size.width.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            val yRatio = (offset.y / size.height.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            onTapPositionChanged(xRatio, yRatio)
            val color =
                if (source == ManualColorPickerSource.ICON) {
                    iconSourceBitmap?.let { sampleBitmapColor(it, xRatio, yRatio) }
                } else {
                    paletteColorAt(xRatio, yRatio)
                }
            color?.let(onColorPicked)
        }
        detectTapGestures { offset -> pick(offset) }
    }.pointerInput(source, iconSourceBitmap) {
        fun pick(offset: Offset) {
            val xRatio = (offset.x / size.width.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            val yRatio = (offset.y / size.height.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            onTapPositionChanged(xRatio, yRatio)
            val color =
                if (source == ManualColorPickerSource.ICON) {
                    iconSourceBitmap?.let { sampleBitmapColor(it, xRatio, yRatio) }
                } else {
                    paletteColorAt(xRatio, yRatio)
                }
            color?.let(onColorPicked)
        }
        detectDragGestures(
            onDragStart = { offset -> pick(offset) },
            onDrag = { change, _ -> pick(change.position) },
        )
    }

@Composable
private fun ManualColorPaletteCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val step = 4.dp.toPx().coerceAtLeast(2f)
        var x = 0f
        while (x < size.width) {
            var y = 0f
            while (y < size.height) {
                drawRect(
                    color = paletteColorAt(
                        xRatio = (x / size.width.coerceAtLeast(1f)).coerceIn(0f, 1f),
                        yRatio = (y / size.height.coerceAtLeast(1f)).coerceIn(0f, 1f),
                    ),
                    topLeft = Offset(x, y),
                    size = Size(step + 1f, step + 1f),
                )
                y += step
            }
            x += step
        }
    }
}

@Composable
private fun ManualColorPreviewBlock(color: Color?) {
    Box(
        modifier =
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        if (color == null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val light = Color.White.copy(alpha = 0.72f)
                val dark = Color.Gray.copy(alpha = 0.42f)
                val cell = size.width / 4f
                for (x in 0 until 4) {
                    for (y in 0 until 4) {
                        drawRect(
                            color = if ((x + y) % 2 == 0) light else dark,
                            topLeft = Offset(x * cell, y * cell),
                            size = Size(cell, cell),
                        )
                    }
                }
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(color),
            )
        }
    }
}

private fun paletteColorAt(
    xRatio: Float,
    yRatio: Float,
): Color {
    val hue = xRatio.coerceIn(0f, 1f) * 360f
    val saturation = yRatio.coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f)))
}

internal fun sampleBitmapColor(
    bitmap: Bitmap,
    xRatio: Float,
    yRatio: Float,
): Color? {
    val x = (xRatio * (bitmap.width - 1)).toInt().coerceIn(0, bitmap.width - 1)
    val y = (yRatio * (bitmap.height - 1)).toInt().coerceIn(0, bitmap.height - 1)
    nearestOpaquePixel(bitmap, x, y)?.let { return Color(it) }
    return null
}

private fun nearestOpaquePixel(
    bitmap: Bitmap,
    centerX: Int,
    centerY: Int,
): Int? {
    for (radius in 0..8) {
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val x = (centerX + dx).coerceIn(0, bitmap.width - 1)
                val y = (centerY + dy).coerceIn(0, bitmap.height - 1)
                val pixel = bitmap.getPixel(x, y)
                if (android.graphics.Color.alpha(pixel) >= 48) {
                    return android.graphics.Color.rgb(
                        android.graphics.Color.red(pixel),
                        android.graphics.Color.green(pixel),
                        android.graphics.Color.blue(pixel),
                    )
                }
            }
        }
    }
    return null
}

internal fun Color.toPickerHexString(): String {
    val argb = toArgb()
    val red = android.graphics.Color.red(argb)
    val green = android.graphics.Color.green(argb)
    val blue = android.graphics.Color.blue(argb)
    return "#%02X%02X%02X".format(red, green, blue)
}

internal fun parsePickerHexColor(value: String): Color? {
    val normalized = value.trim().removePrefix("#")
    if (normalized.length != 6 || normalized.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) {
        return null
    }
    return runCatching {
        Color(
            android.graphics.Color.rgb(
                normalized.substring(0, 2).toInt(16),
                normalized.substring(2, 4).toInt(16),
                normalized.substring(4, 6).toInt(16),
            ),
        )
    }.getOrNull()
}
