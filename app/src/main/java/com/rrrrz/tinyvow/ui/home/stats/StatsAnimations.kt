package com.rrrrz.tinyvow.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private const val STAT_ANIMATIONS_ENABLED = true

@Composable
internal fun animateLongValue(
    targetValue: Long,
    label: String,
    durationMillis: Int = 800,
    delayMillis: Int = 0,
): Long {
    val sanitizedTarget = targetValue.coerceAtLeast(0L)
    if (!STAT_ANIMATIONS_ENABLED) return sanitizedTarget

    val delayedTargetValue = rememberDelayedLongTarget(
        targetValue = sanitizedTarget,
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue.toFloat(),
        animationSpec = tween(durationMillis = slowedMetricDuration(durationMillis)),
        label = label,
    )
    return animatedValue.roundToLong().coerceAtLeast(0L)
}

@Composable
internal fun animateIntValue(
    targetValue: Int,
    label: String,
    durationMillis: Int = 700,
    delayMillis: Int = 0,
): Int {
    val sanitizedTarget = targetValue.coerceAtLeast(0)
    if (!STAT_ANIMATIONS_ENABLED) return sanitizedTarget

    val delayedTargetValue = rememberDelayedIntTarget(
        targetValue = sanitizedTarget,
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue.toFloat(),
        animationSpec = tween(durationMillis = slowedMetricDuration(durationMillis)),
        label = label,
    )
    return animatedValue.roundToInt().coerceAtLeast(0)
}

@Composable
internal fun animateFractionValue(
    targetValue: Float,
    label: String,
    durationMillis: Int = 760,
    delayMillis: Int = 0,
): Float {
    val sanitizedTarget = targetValue.coerceIn(0f, 1f)
    if (!STAT_ANIMATIONS_ENABLED) return sanitizedTarget

    val delayedTargetValue = rememberDelayedFloatTarget(
        targetValue = sanitizedTarget,
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue,
        animationSpec = tween(durationMillis = slowedMetricDuration(durationMillis)),
        label = label,
    )
    return animatedValue.coerceIn(0f, 1f)
}

@Composable
internal fun animateReplayFractionValue(
    targetValue: Float,
    replayKey: Any?,
    durationMillis: Int = 760,
    delayMillis: Int = 0,
): Float {
    val sanitizedTarget = targetValue.coerceIn(0f, 1f)
    if (!STAT_ANIMATIONS_ENABLED) return sanitizedTarget

    val animatable = remember { Animatable(0f) }
    LaunchedEffect(replayKey) {
        animatable.snapTo(0f)
        if (delayMillis > 0) {
            delay(delayMillis.toLong())
        }
        animatable.animateTo(
            targetValue = sanitizedTarget,
            animationSpec = tween(durationMillis = slowedMetricDuration(durationMillis)),
        )
    }
    LaunchedEffect(sanitizedTarget) {
        if (animatable.value != 0f) {
            animatable.animateTo(
                targetValue = sanitizedTarget,
                animationSpec = tween(durationMillis = slowedMetricDuration(durationMillis)),
            )
        }
    }
    return animatable.value.coerceIn(0f, 1f)
}

@Composable
internal fun animateDecimalValue(
    targetValue: Float,
    label: String,
    durationMillis: Int = 760,
    delayMillis: Int = 0,
): Float {
    val sanitizedTarget = targetValue.coerceAtLeast(0f)
    if (!STAT_ANIMATIONS_ENABLED) return sanitizedTarget

    val delayedTargetValue = rememberDelayedFloatTarget(
        targetValue = sanitizedTarget,
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue,
        animationSpec = tween(durationMillis = slowedMetricDuration(durationMillis)),
        label = label,
    )
    return animatedValue.coerceAtLeast(0f)
}

@Composable
internal fun animateMetricDisplayText(
    rawText: String,
    label: String,
    delayMillis: Int = 0,
): String {
    if (!STAT_ANIMATIONS_ENABLED) return rawText

    val durationMatch = Regex("""(\d+)h(?: (\d+)m(?:in)?)?|(\d+)m(?:in)?""").find(rawText)
    if (durationMatch != null) {
        val animatedDuration = animateLongValue(
            targetValue = parseDisplayDuration(durationMatch.value),
            label = "${label}_duration",
            durationMillis = 860,
            delayMillis = delayMillis,
        )
        return rawText.replaceRange(durationMatch.range, formatDuration(animatedDuration))
    }

    val percentMatch = Regex("""(\d+)%""").find(rawText)
    if (percentMatch != null) {
        val animatedPercent = animateIntValue(
            targetValue = percentMatch.groupValues[1].toIntOrNull() ?: 0,
            label = "${label}_percent",
            durationMillis = 760,
            delayMillis = delayMillis,
        )
        return rawText.replaceRange(percentMatch.range, "${animatedPercent}%")
    }

    val decimalMatch = Regex("""([+\-]?\d+\.\d+)""").find(rawText)
    if (decimalMatch != null) {
        val animatedDecimal = animateDecimalValue(
            targetValue = kotlin.math.abs(decimalMatch.groupValues[1].toFloatOrNull() ?: 0f),
            label = "${label}_decimal",
            durationMillis = 760,
            delayMillis = delayMillis,
        )
        val sign = if (decimalMatch.groupValues[1].startsWith("-")) "-" else if (decimalMatch.groupValues[1].startsWith("+")) "+" else ""
        return rawText.replaceRange(
            decimalMatch.range,
            sign + String.format(Locale.CHINA, "%.1f", animatedDecimal),
        )
    }

    val countMatch = Regex("""([+\-]?\d+)""").find(rawText)
    if (countMatch != null) {
        val rawValue = countMatch.groupValues[1]
        val animatedCount = animateIntValue(
            targetValue = kotlin.math.abs(rawValue.toIntOrNull() ?: 0),
            label = "${label}_count",
            durationMillis = 720,
            delayMillis = delayMillis,
        )
        val sign = if (rawValue.startsWith("-")) "-" else if (rawValue.startsWith("+")) "+" else ""
        return rawText.replaceRange(countMatch.range, sign + animatedCount.toString())
    }

    return rawText
}

private fun slowedMetricDuration(durationMillis: Int): Int =
    durationMillis.coerceAtMost(1_100)

@Composable
internal fun AnimatedMetricText(
    rawText: String,
    label: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = LocalContentColor.current,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    delayMillis: Int = 0,
) {
    Text(
        text = animateMetricDisplayText(
            rawText = rawText,
            label = label,
            delayMillis = delayMillis,
        ),
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
internal fun rememberDelayedLongTarget(
    targetValue: Long,
    delayMillis: Int,
): Long {
    val sanitizedTarget = targetValue.coerceAtLeast(0L)
    if (!STAT_ANIMATIONS_ENABLED) return sanitizedTarget

    val delayedTarget by produceState(
        initialValue = if (delayMillis > 0) 0L else sanitizedTarget,
        key1 = sanitizedTarget,
        key2 = delayMillis,
    ) {
        if (delayMillis > 0) {
            value = 0L
            delay(delayMillis.toLong())
        }
        value = sanitizedTarget
    }
    return delayedTarget
}

@Composable
internal fun rememberDelayedIntTarget(
    targetValue: Int,
    delayMillis: Int,
): Int {
    val sanitizedTarget = targetValue.coerceAtLeast(0)
    if (!STAT_ANIMATIONS_ENABLED) return sanitizedTarget

    val delayedTarget by produceState(
        initialValue = if (delayMillis > 0) 0 else sanitizedTarget,
        key1 = sanitizedTarget,
        key2 = delayMillis,
    ) {
        if (delayMillis > 0) {
            value = 0
            delay(delayMillis.toLong())
        }
        value = sanitizedTarget
    }
    return delayedTarget
}

@Composable
internal fun rememberDelayedFloatTarget(
    targetValue: Float,
    delayMillis: Int,
): Float {
    val sanitizedTarget = targetValue.coerceAtLeast(0f)
    if (!STAT_ANIMATIONS_ENABLED) return sanitizedTarget

    val delayedTarget by produceState(
        initialValue = if (delayMillis > 0) 0f else sanitizedTarget,
        key1 = sanitizedTarget,
        key2 = delayMillis,
    ) {
        if (delayMillis > 0) {
            value = 0f
            delay(delayMillis.toLong())
        }
        value = sanitizedTarget
    }
    return delayedTarget
}
