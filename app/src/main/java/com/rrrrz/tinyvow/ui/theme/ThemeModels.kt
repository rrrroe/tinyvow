package com.rrrrz.tinyvow.ui.theme

import com.rrrrz.tinyvow.i18n.AppText
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

@Stable
data class ThemeSeed(
    val id: String,
    val name: String,
    val controlColor: Int,
    val encourageColor: Int,
    val baseColor: Int,
    val isCustom: Boolean = false,
)

@Stable
data class ThemeTokens(
    val seed: ThemeSeed,
    val colorScheme: ColorScheme,
    val control: Color,
    val onControl: Color,
    val controlContainer: Color,
    val onControlContainer: Color,
    val encourage: Color,
    val onEncourage: Color,
    val encourageContainer: Color,
    val onEncourageContainer: Color,
    val base: Color,
    val onBase: Color,
    val baseContainer: Color,
    val onBaseContainer: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val pageGradient: List<Color>,
    val chartPalette: List<Color>,
    val achievementPalette: List<Color>,
)

val ThemePresets = listOf(
    ThemeSeed("preset_sakura_mint", "Sakura Mint", 0xFFD98B8B.toInt(), 0xFF9BC8A6.toInt(), 0xFF8FBED0.toInt()),
    ThemeSeed("preset_clear_celadon", "Clear Celadon", 0xFFDCA487.toInt(), 0xFFA8C9A1.toInt(), 0xFF91B9C9.toInt()),
    ThemeSeed("preset_iris_mist", "Iris Mist", 0xFFD59AAA.toInt(), 0xFFA7C7B6.toInt(), 0xFFA7ADD8.toInt()),
    ThemeSeed("preset_orange_blossom", "Orange Blossom", 0xFFE0A36F.toInt(), 0xFFB8CF91.toInt(), 0xFFA8BBD8.toInt()),
    ThemeSeed("preset_seasalt_linen", "Sea Salt Linen", 0xFFD6A18E.toInt(), 0xFF9FCBBE.toInt(), 0xFFA6C1D4.toInt()),
)

val MemberThemePresets = listOf(
    ThemeSeed("member_aurora_pro", "Aurora Pro", 0xFF7F6DE0.toInt(), 0xFF62C6A7.toInt(), 0xFF5F98D8.toInt()),
    ThemeSeed("member_sunrise_focus", "Sunrise Focus", 0xFFE27D75.toInt(), 0xFFE2B965.toInt(), 0xFF6DA6D8.toInt()),
    ThemeSeed("member_forest_deep", "Forest Deep", 0xFF7E9B6D.toInt(), 0xFF4AA184.toInt(), 0xFF5D88A8.toInt()),
    ThemeSeed("member_lotus_night", "Lotus Night", 0xFFB46CA2.toInt(), 0xFF7AA7C7.toInt(), 0xFF8B82D4.toInt()),
    ThemeSeed("member_coral_tide", "Coral Tide", 0xFFD76E78.toInt(), 0xFF69B7A8.toInt(), 0xFF6F9FDA.toInt()),
)

val DefaultThemeSeed = ThemePresets.first()

val LocalThemeColors = staticCompositionLocalOf {
    themeTokensFromSeed(DefaultThemeSeed)
}

fun resolveThemeSeed(
    selectedThemeId: String?,
    customThemes: List<ThemeSeed>,
): ThemeSeed {
    val themes = ThemePresets + MemberThemePresets + customThemes
    return themes.firstOrNull { it.id == selectedThemeId } ?: DefaultThemeSeed
}

fun legacyThemeId(index: Int): String =
    ThemePresets.getOrElse(index.coerceAtLeast(0) % ThemePresets.size) { DefaultThemeSeed }.id

fun legacyCustomTheme(seedColor: Int): ThemeSeed {
    val base = Color(seedColor)
    return ThemeSeed(
        id = "custom_legacy_seed",
        name = "Legacy custom",
        controlColor = rotateHue(base, -28f).toArgb(),
        encourageColor = rotateHue(base, 96f).toArgb(),
        baseColor = base.toArgb(),
        isCustom = true,
    )
}

fun themeTokensFromSeed(seed: ThemeSeed): ThemeTokens {
    val control = Color(seed.controlColor)
    val encourage = Color(seed.encourageColor)
    val base = Color(seed.baseColor)
    val background = softSurface(base, 0.982f)
    val surface = Color.White
    val baseContainer = tone(base, saturationMultiplier = 0.18f, value = 0.965f)
    val controlContainer = tone(control, saturationMultiplier = 0.16f, value = 0.968f)
    val encourageContainer = tone(encourage, saturationMultiplier = 0.16f, value = 0.968f)
    val onSurface = readableDark(base)
    val onSurfaceVariant = lerp(onSurface, Color.White, 0.38f)
    val danger = lerp(control, Color(0xFF944848), 0.22f)
    val success = lerp(encourage, Color(0xFF5E9E74), 0.18f)
    val warning = lerp(control, encourage, 0.36f)

    val scheme = lightColorScheme(
        primary = base,
        onPrimary = readableOn(base),
        primaryContainer = baseContainer,
        onPrimaryContainer = readableDark(base),
        secondary = control,
        onSecondary = readableOn(control),
        secondaryContainer = controlContainer,
        onSecondaryContainer = readableDark(control),
        tertiary = encourage,
        onTertiary = readableOn(encourage),
        tertiaryContainer = encourageContainer,
        onTertiaryContainer = readableDark(encourage),
        error = danger,
        onError = readableOn(danger),
        errorContainer = tone(danger, saturationMultiplier = 0.16f, value = 0.968f),
        onErrorContainer = readableDark(danger),
        background = background,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = lerp(background, baseContainer, 0.42f),
        onSurfaceVariant = onSurfaceVariant,
        outline = lerp(base, onSurface, 0.14f).copy(alpha = 0.62f),
        outlineVariant = lerp(baseContainer, background, 0.62f),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = lerp(surface, background, 0.36f),
        surfaceContainer = background,
        surfaceContainerHigh = lerp(background, baseContainer, 0.18f),
        surfaceContainerHighest = lerp(background, baseContainer, 0.30f),
        surfaceDim = lerp(background, onSurface, 0.06f),
        surfaceBright = Color.White,
    )

    val chartPalette = listOf(
        base,
        control,
        encourage,
        lerp(base, control, 0.36f),
        lerp(base, encourage, 0.36f),
        lerp(control, encourage, 0.48f),
        lerp(base, Color.White, 0.28f),
        lerp(control, Color.White, 0.24f),
        lerp(encourage, Color.White, 0.24f),
        lerp(base, Color.Black, 0.10f),
    )

    return ThemeTokens(
        seed = seed,
        colorScheme = scheme,
        control = control,
        onControl = readableOn(control),
        controlContainer = controlContainer,
        onControlContainer = readableDark(control),
        encourage = encourage,
        onEncourage = readableOn(encourage),
        encourageContainer = encourageContainer,
        onEncourageContainer = readableDark(encourage),
        base = base,
        onBase = readableOn(base),
        baseContainer = baseContainer,
        onBaseContainer = readableDark(base),
        success = success,
        warning = warning,
        danger = danger,
        pageGradient = listOf(background, lerp(background, baseContainer, 0.50f), background),
        chartPalette = chartPalette,
        achievementPalette = listOf(
            lerp(control, base, 0.18f),
            lerp(control, encourage, 0.25f),
            lerp(base, encourage, 0.30f),
            encourage,
            lerp(base, Color.White, 0.20f),
        ),
    )
}

fun createCustomTheme(
    name: String,
    controlColor: Int,
    encourageColor: Int,
    baseColor: Int,
): ThemeSeed = ThemeSeed(
    id = "custom_${System.currentTimeMillis()}",
    name = name.ifBlank { "Custom theme" },
    controlColor = controlColor,
    encourageColor = encourageColor,
    baseColor = baseColor,
    isCustom = true,
)

fun ThemeSeed.localizedName(): String {
    if (isCustom) return name
    val key = "theme_${id}_name"
    val value = AppText.t(key)
    return if (value == key) name else value
}

fun argbToHex(color: Int): String = "#%06X".format(color and 0x00FFFFFF)

fun parseHexColorOrNull(value: String): Int? {
    val normalized = value.trim().removePrefix("#")
    if (normalized.length !in setOf(6, 8)) return null
    if (!normalized.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
    val withAlpha = if (normalized.length == 6) "FF$normalized" else normalized
    return withAlpha.toLong(16).toInt()
}

internal fun tone(
    color: Color,
    saturationMultiplier: Float,
    value: Float,
): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[1] = (hsv[1] * saturationMultiplier).coerceIn(0f, 1f)
    hsv[2] = value.coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

internal fun rotateHue(color: Color, degrees: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[0] = (hsv[0] + degrees + 360f) % 360f
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun softSurface(color: Color, value: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[1] = (hsv[1] * 0.075f).coerceIn(0f, 1f)
    hsv[2] = value.coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun readableDark(color: Color): Color =
    lerp(Color(0xFF26282B), color, 0.14f)

private fun readableOn(color: Color): Color =
    if (color.luminance() > 0.56f) Color(0xFF202124) else Color.White
