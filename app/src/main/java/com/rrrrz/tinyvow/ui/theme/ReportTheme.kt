package com.rrrrz.tinyvow.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

@Stable
data class ReportColors(
    val pageGradient: List<Color>,
    val periodPalette: List<Color>,
    val appChartPalette: List<Color>,
    val skeletonBase: Color,
    val skeletonHighlight: Color,
    val skeletonAccent: Color,
    val positive: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
)

internal val LocalReportColors = staticCompositionLocalOf {
    reportColorsFromScheme(lightColorScheme())
}

internal fun reportColorsFromTokens(
    tokens: ThemeTokens,
): ReportColors {
    val colorScheme = tokens.colorScheme
    return ReportColors(
        pageGradient = tokens.pageGradient,
        periodPalette = listOf(
            tokens.base,
            tokens.control,
            tokens.encourage,
            lerp(tokens.base, tokens.encourage, 0.45f),
        ),
        appChartPalette = tokens.chartPalette,
        skeletonBase = colorScheme.surfaceContainerHighest.copy(alpha = 0.46f),
        skeletonHighlight = colorScheme.surface.copy(alpha = 0.92f),
        skeletonAccent = colorScheme.outlineVariant.copy(alpha = 0.32f),
        positive = tokens.encourage,
        warning = tokens.warning,
        danger = tokens.control,
        info = tokens.base,
    )
}

internal fun reportColorsFromScheme(
    colorScheme: ColorScheme,
): ReportColors {
    val primarySoft = lerp(colorScheme.primary, colorScheme.secondary, 0.35f)
    val primaryBright = lerp(colorScheme.primary, Color.White, 0.18f)
    val secondaryBright = lerp(colorScheme.secondary, Color.White, 0.12f)
    val tertiaryWarm = lerp(colorScheme.tertiary, colorScheme.primary, 0.22f)
    val tertiarySoft = lerp(colorScheme.tertiary, colorScheme.primary, 0.45f)
    val surfaceTint = lerp(colorScheme.surfaceContainerLow, colorScheme.primaryContainer, 0.45f)
    val info = colorScheme.primary
    val positive = colorScheme.tertiary
    val warning = lerp(colorScheme.secondary, colorScheme.tertiary, 0.36f)
    val danger = colorScheme.secondary

    return ReportColors(
        pageGradient = listOf(
            colorScheme.background,
            surfaceTint.copy(alpha = 0.98f),
            colorScheme.background,
        ),
        periodPalette = listOf(
            info,
            tertiaryWarm,
            primaryBright,
            positive,
        ),
        appChartPalette = listOf(
            colorScheme.primary,
            colorScheme.secondary,
            colorScheme.tertiary,
            primarySoft,
            tertiarySoft,
            info,
            positive,
            warning,
            secondaryBright,
            lerp(colorScheme.primary, colorScheme.surface, 0.2f),
        ),
        skeletonBase = colorScheme.surfaceContainerHighest.copy(alpha = 0.46f),
        skeletonHighlight = colorScheme.surface.copy(alpha = 0.92f),
        skeletonAccent = colorScheme.outlineVariant.copy(alpha = 0.32f),
        positive = positive,
        warning = warning,
        danger = danger,
        info = info,
    )
}
