package com.rrrrz.tinyvow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TinyVowTheme(
    themeIndex: Int = 0,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default for custom branding
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeIndex) {
        0 -> lightColorScheme(
            primary = CloudPrimary,
            onPrimary = Color.White,
            primaryContainer = CloudPrimaryContainer,
            onPrimaryContainer = CloudPrimary,
            secondary = CloudSecondary,
            onSecondary = Color.White,
            secondaryContainer = CloudSecondaryContainer,
            onSecondaryContainer = CloudPrimary,
            tertiary = CloudTertiary,
            background = CloudBg,
            surface = CloudSurface,
            onSurface = Color(0xFF2F3133),
            surfaceVariant = CloudBg,
            onSurfaceVariant = Color(0xFF5F6266),
            outline = CloudPrimary.copy(alpha = 0.5f),
            outlineVariant = CloudPrimary.copy(alpha = 0.1f),
            surfaceContainer = CloudBg,
            surfaceContainerLow = CloudBg,
            surfaceContainerHigh = CloudBg,
            surfaceContainerHighest = CloudBg,
            surfaceDim = Color(0xFFE8EBEE),
            surfaceBright = Color.White
        )
        1 -> lightColorScheme(
            primary = BambooPrimary,
            onPrimary = Color.White,
            primaryContainer = BambooPrimaryContainer,
            onPrimaryContainer = BambooPrimary,
            secondary = BambooSecondary,
            onSecondary = Color.White,
            secondaryContainer = BambooSecondaryContainer,
            onSecondaryContainer = BambooPrimary,
            tertiary = BambooTertiary,
            background = BambooBg,
            surface = BambooSurface,
            onSurface = Color(0xFF2F3330),
            surfaceVariant = BambooBg,
            onSurfaceVariant = Color(0xFF5F6662),
            outline = BambooPrimary.copy(alpha = 0.5f),
            outlineVariant = BambooPrimary.copy(alpha = 0.1f),
            surfaceContainer = BambooBg,
            surfaceContainerLow = BambooBg,
            surfaceContainerHigh = BambooBg,
            surfaceContainerHighest = BambooBg,
            surfaceDim = Color(0xFFE8EAE9),
            surfaceBright = Color.White
        )
        2 -> lightColorScheme(
            primary = SnowPrimary,
            onPrimary = Color.White,
            primaryContainer = SnowPrimaryContainer,
            onPrimaryContainer = SnowPrimary,
            secondary = SnowSecondary,
            onSecondary = Color.White,
            secondaryContainer = SnowSecondaryContainer,
            onSecondaryContainer = SnowPrimary,
            tertiary = SnowTertiary,
            background = SnowBg,
            surface = SnowSurface,
            onSurface = Color(0xFF303133),
            surfaceVariant = SnowBg,
            onSurfaceVariant = Color(0xFF626366),
            outline = SnowPrimary.copy(alpha = 0.5f),
            outlineVariant = SnowPrimary.copy(alpha = 0.1f),
            surfaceContainer = SnowBg,
            surfaceContainerLow = SnowBg,
            surfaceContainerHigh = SnowBg,
            surfaceContainerHighest = SnowBg,
            surfaceDim = Color(0xFFEBECED),
            surfaceBright = Color.White
        )
        3 -> lightColorScheme(
            primary = ZenPrimary,
            onPrimary = Color.White,
            primaryContainer = ZenPrimaryContainer,
            onPrimaryContainer = ZenPrimary,
            secondary = ZenSecondary,
            onSecondary = Color.White,
            secondaryContainer = ZenSecondaryContainer,
            onSecondaryContainer = ZenPrimary,
            tertiary = ZenTertiary,
            background = ZenBg,
            surface = ZenSurface,
            onSurface = Color(0xFF33302F),
            surfaceVariant = ZenBg,
            onSurfaceVariant = Color(0xFF66625F),
            outline = ZenPrimary.copy(alpha = 0.5f),
            outlineVariant = ZenPrimary.copy(alpha = 0.1f),
            surfaceContainer = ZenBg,
            surfaceContainerLow = ZenBg,
            surfaceContainerHigh = ZenBg,
            surfaceContainerHighest = ZenBg,
            surfaceDim = Color(0xFFEEECED),
            surfaceBright = Color.White
        )
        4 -> lightColorScheme(
            primary = PeachPrimary,
            onPrimary = Color.White,
            primaryContainer = PeachPrimaryContainer,
            onPrimaryContainer = PeachPrimary,
            secondary = PeachSecondary,
            onSecondary = Color.White,
            secondaryContainer = PeachSecondaryContainer,
            onSecondaryContainer = PeachPrimary,
            tertiary = PeachTertiary,
            background = PeachBg,
            surface = PeachSurface,
            onSurface = Color(0xFF332F31),
            surfaceVariant = PeachBg,
            onSurfaceVariant = Color(0xFF665F61),
            outline = PeachPrimary.copy(alpha = 0.5f),
            outlineVariant = PeachPrimary.copy(alpha = 0.1f),
            surfaceContainer = PeachBg,
            surfaceContainerLow = PeachBg,
            surfaceContainerHigh = PeachBg,
            surfaceContainerHighest = PeachBg,
            surfaceDim = Color(0xFFEEE8E9),
            surfaceBright = Color.White
        )
        5 -> lightColorScheme(
            primary = LimePrimary,
            onPrimary = Color.White,
            primaryContainer = LimePrimaryContainer,
            onPrimaryContainer = LimePrimary,
            secondary = LimeSecondary,
            onSecondary = Color.White,
            secondaryContainer = LimeSecondaryContainer,
            onSecondaryContainer = LimePrimary,
            tertiary = LimeTertiary,
            background = LimeBg,
            surface = LimeSurface,
            onSurface = Color(0xFF2F3330),
            surfaceVariant = LimeBg,
            onSurfaceVariant = Color(0xFF5F6662),
            outline = LimePrimary.copy(alpha = 0.5f),
            outlineVariant = LimePrimary.copy(alpha = 0.1f),
            surfaceContainer = LimeBg,
            surfaceContainerLow = LimeBg,
            surfaceContainerHigh = LimeBg,
            surfaceContainerHighest = LimeBg,
            surfaceDim = Color(0xFFE8EEE9),
            surfaceBright = Color.White
        )
        6 -> lightColorScheme(
            primary = CitrusPrimary,
            onPrimary = Color.White,
            primaryContainer = CitrusPrimaryContainer,
            onPrimaryContainer = CitrusPrimary,
            secondary = CitrusSecondary,
            onSecondary = Color.White,
            secondaryContainer = CitrusSecondaryContainer,
            onSecondaryContainer = CitrusPrimary,
            tertiary = CitrusTertiary,
            background = CitrusBg,
            surface = CitrusSurface,
            onSurface = Color(0xFF33312F),
            surfaceVariant = CitrusBg,
            onSurfaceVariant = Color(0xFF66625F),
            outline = CitrusPrimary.copy(alpha = 0.5f),
            outlineVariant = CitrusPrimary.copy(alpha = 0.1f),
            surfaceContainer = CitrusBg,
            surfaceContainerLow = CitrusBg,
            surfaceContainerHigh = CitrusBg,
            surfaceContainerHighest = CitrusBg,
            surfaceDim = Color(0xFFEEECEA),
            surfaceBright = Color.White
        )
        else -> when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}