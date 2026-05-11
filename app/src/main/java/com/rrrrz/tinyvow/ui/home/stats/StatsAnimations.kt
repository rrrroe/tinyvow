package com.rrrrz.tinyvow.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
internal fun animateLongValue(
    targetValue: Long,
    label: String,
    durationMillis: Int = 800,
    delayMillis: Int = 0,
): Long {
    val delayedTargetValue = rememberDelayedLongTarget(
        targetValue = targetValue.coerceAtLeast(0L),
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue.toFloat(),
        animationSpec = tween(durationMillis = durationMillis),
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
    val delayedTargetValue = rememberDelayedIntTarget(
        targetValue = targetValue.coerceAtLeast(0),
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue.toFloat(),
        animationSpec = tween(durationMillis = durationMillis),
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
    val delayedTargetValue = rememberDelayedFloatTarget(
        targetValue = targetValue.coerceIn(0f, 1f),
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue,
        animationSpec = tween(durationMillis = durationMillis),
        label = label,
    )
    return animatedValue.coerceIn(0f, 1f)
}

@Composable
internal fun animateDecimalValue(
    targetValue: Float,
    label: String,
    durationMillis: Int = 760,
    delayMillis: Int = 0,
): Float {
    val delayedTargetValue = rememberDelayedFloatTarget(
        targetValue = targetValue.coerceAtLeast(0f),
        delayMillis = delayMillis,
    )
    val animatedValue by animateFloatAsState(
        targetValue = delayedTargetValue,
        animationSpec = tween(durationMillis = durationMillis),
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
    val durationMatch = Regex("""(\d+)h(?: (\d+)m)?|(\d+)m""").find(rawText)
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

    val decimalMatch = Regex("""(\d+\.\d+)""").find(rawText)
    if (decimalMatch != null) {
        val animatedDecimal = animateDecimalValue(
            targetValue = decimalMatch.groupValues[1].toFloatOrNull() ?: 0f,
            label = "${label}_decimal",
            durationMillis = 760,
            delayMillis = delayMillis,
        )
        return rawText.replaceRange(
            decimalMatch.range,
            String.format(Locale.CHINA, "%.1f", animatedDecimal),
        )
    }

    val countMatch = Regex("""\d+""").find(rawText)
    if (countMatch != null) {
        val animatedCount = animateIntValue(
            targetValue = countMatch.value.toIntOrNull() ?: 0,
            label = "${label}_count",
            durationMillis = 720,
            delayMillis = delayMillis,
        )
        return rawText.replaceRange(countMatch.range, animatedCount.toString())
    }

    return rawText
}

@Composable
internal fun rememberDelayedLongTarget(
    targetValue: Long,
    delayMillis: Int,
): Long {
    val sanitizedTarget = targetValue.coerceAtLeast(0L)
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
