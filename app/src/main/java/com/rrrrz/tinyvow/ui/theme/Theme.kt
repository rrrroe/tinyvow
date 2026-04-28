package com.rrrrz.tinyvow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TinyVowTheme(
    themeSeed: ThemeSeed = DefaultThemeSeed,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tokens = themeTokensFromSeed(themeSeed)
    val reportColors = reportColorsFromTokens(tokens)

    CompositionLocalProvider(
        LocalThemeColors provides tokens,
        LocalReportColors provides reportColors,
    ) {
        MaterialTheme(
            colorScheme = tokens.colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
