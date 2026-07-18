package com.rrrrz.tinyvow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
fun TinyVowTheme(
    themeSeed: ThemeSeed = DefaultThemeSeed,
    darkTheme: Boolean = isSystemInDarkTheme(),
    appFontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val tokens = themeTokensFromSeed(themeSeed)
    val reportColors = reportColorsFromTokens(tokens)
    val systemDensity = LocalDensity.current
    val appDensity = remember(systemDensity.density, appFontScale) {
        Density(
            density = systemDensity.density,
            fontScale = appFontScale,
        )
    }

    CompositionLocalProvider(
        LocalThemeColors provides tokens,
        LocalReportColors provides reportColors,
        LocalDensity provides appDensity,
    ) {
        MaterialTheme(
            colorScheme = tokens.colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
