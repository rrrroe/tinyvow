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
    val backgroundColor: Int,
    val surfaceColor: Int,
    val textColor: Int,
    val mutedTextColor: Int,
    val primaryColor: Int,
    val progressColor: Int,
    val controlColor: Int,
    val encourageColor: Int,
    val baseColor: Int,
    val neutralAccentColor: Int,
    val isCustom: Boolean = false,
)

@Stable
data class ThemeTokens(
    val seed: ThemeSeed,
    val colorScheme: ColorScheme,
    val inkStrong: Color,
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val inkOnAccent: Color,
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
    val glassOverlay: Color,
    val subtleAccent: Color,
    val progressAccent: Color,
    val navSelectedContainer: Color,
    val navUnselected: Color,
)

val ThemePresets = listOf(
    ThemeSeed(
        id = "preset_porcelain_grove",
        name = "Porcelain Grove",
        backgroundColor = 0xFFF4F8F6.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF1F312F.toInt(),
        mutedTextColor = 0xFF74837E.toInt(),
        primaryColor = 0xFF32685B.toInt(),
        progressColor = 0xFFA6C85F.toInt(),
        controlColor = 0xFFC36F64.toInt(),
        encourageColor = 0xFF4E806B.toInt(),
        baseColor = 0xFF32685B.toInt(),
        neutralAccentColor = 0xFFDDEAE4.toInt(),
    ),
    ThemeSeed(
        id = "preset_linen_amber",
        name = "Linen Amber",
        backgroundColor = 0xFFF8F3EA.toInt(),
        surfaceColor = 0xFFFFFEFA.toInt(),
        textColor = 0xFF33291F.toInt(),
        mutedTextColor = 0xFF897E70.toInt(),
        primaryColor = 0xFF6F6146.toInt(),
        progressColor = 0xFFC7A64C.toInt(),
        controlColor = 0xFFC9765E.toInt(),
        encourageColor = 0xFF64785D.toInt(),
        baseColor = 0xFF6F6146.toInt(),
        neutralAccentColor = 0xFFEAE0D0.toInt(),
    ),
    ThemeSeed(
        id = "preset_lotus_graphite",
        name = "Lotus Graphite",
        backgroundColor = 0xFFF7F3F6.toInt(),
        surfaceColor = 0xFFFFFCFE.toInt(),
        textColor = 0xFF302532.toInt(),
        mutedTextColor = 0xFF877B86.toInt(),
        primaryColor = 0xFF70566A.toInt(),
        progressColor = 0xFFB7C765.toInt(),
        controlColor = 0xFFC46D73.toInt(),
        encourageColor = 0xFF60775F.toInt(),
        baseColor = 0xFF70566A.toInt(),
        neutralAccentColor = 0xFFE9DDE5.toInt(),
    ),
)

val MemberThemePresets = listOf(
    ThemeSeed(
        id = "member_noir_moss",
        name = "Noir Moss",
        backgroundColor = 0xFFF2F1EC.toInt(),
        surfaceColor = 0xFFFFFEFA.toInt(),
        textColor = 0xFF1B211A.toInt(),
        mutedTextColor = 0xFF777A72.toInt(),
        primaryColor = 0xFF2F462E.toInt(),
        progressColor = 0xFFAEC900.toInt(),
        controlColor = 0xFFB85F50.toInt(),
        encourageColor = 0xFF2F462E.toInt(),
        baseColor = 0xFF2F462E.toInt(),
        neutralAccentColor = 0xFFDFDED6.toInt(),
    ),
    ThemeSeed(
        id = "member_porcelain_blue",
        name = "Porcelain Blue",
        backgroundColor = 0xFFF4F6F7.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF17283A.toInt(),
        mutedTextColor = 0xFF74818C.toInt(),
        primaryColor = 0xFF2D5D74.toInt(),
        progressColor = 0xFF9FC6D4.toInt(),
        controlColor = 0xFFC67461.toInt(),
        encourageColor = 0xFF4F776D.toInt(),
        baseColor = 0xFF2D5D74.toInt(),
        neutralAccentColor = 0xFFDDE7EC.toInt(),
    ),
    ThemeSeed(
        id = "member_lotus_paper",
        name = "Lotus Paper",
        backgroundColor = 0xFFF8F2F4.toInt(),
        surfaceColor = 0xFFFFFEFC.toInt(),
        textColor = 0xFF34202A.toInt(),
        mutedTextColor = 0xFF897D84.toInt(),
        primaryColor = 0xFF765A66.toInt(),
        progressColor = 0xFFC1C96B.toInt(),
        controlColor = 0xFFC96D65.toInt(),
        encourageColor = 0xFF65785E.toInt(),
        baseColor = 0xFF765A66.toInt(),
        neutralAccentColor = 0xFFE9DCE1.toInt(),
    ),
)

val DefaultThemeSeed = ThemePresets[0]

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
        backgroundColor = softSurface(base, 0.982f).toArgb(),
        surfaceColor = Color.White.toArgb(),
        textColor = readableDark(base).toArgb(),
        mutedTextColor = lerp(readableDark(base), Color.White, 0.38f).toArgb(),
        primaryColor = base.toArgb(),
        progressColor = rotateHue(base, 54f).toArgb(),
        controlColor = rotateHue(base, -28f).toArgb(),
        encourageColor = rotateHue(base, 96f).toArgb(),
        baseColor = base.toArgb(),
        neutralAccentColor = tone(base, saturationMultiplier = 0.10f, value = 0.92f).toArgb(),
        isCustom = true,
    )
}

fun themeTokensFromSeed(seed: ThemeSeed): ThemeTokens {
    val background = Color(seed.backgroundColor)
    val surface = Color(seed.surfaceColor)
    val primary = Color(seed.primaryColor)
    val progressAccent = Color(seed.progressColor)
    val control = Color(seed.controlColor)
    val encourage = Color(seed.encourageColor)
    val base = Color(seed.baseColor)
    val neutralAccent = Color(seed.neutralAccentColor)
    val seedInk = Color(seed.textColor)
    val seedMuted = Color(seed.mutedTextColor)
    val ink = refinedInk(seedInk, base)
    val inkStrong = refinedInkStrong(seedInk, base)
    val inkMuted = refinedInkMuted(ink, seedMuted, background)
    val inkFaint = refinedInkFaint(ink, background)
    val baseContainer = lerp(surface, neutralAccent, 0.74f)
    val controlContainer = tone(control, saturationMultiplier = 0.12f, value = 0.965f)
    val encourageContainer = tone(encourage, saturationMultiplier = 0.14f, value = 0.958f)
    val primaryContainer = lerp(surface, primary, 0.13f)
    val danger = lerp(control, Color(0xFF7F3E35), 0.18f)
    val success = lerp(encourage, Color(0xFF385D42), 0.16f)
    val warning = lerp(progressAccent, control, 0.20f)

    val scheme = lightColorScheme(
        primary = primary,
        onPrimary = readableOn(primary),
        primaryContainer = primaryContainer,
        onPrimaryContainer = inkStrong,
        secondary = control,
        onSecondary = readableOn(control),
        secondaryContainer = controlContainer,
        onSecondaryContainer = lerp(inkStrong, control, 0.16f),
        tertiary = encourage,
        onTertiary = readableOn(encourage),
        tertiaryContainer = encourageContainer,
        onTertiaryContainer = lerp(inkStrong, encourage, 0.16f),
        error = danger,
        onError = readableOn(danger),
        errorContainer = tone(danger, saturationMultiplier = 0.12f, value = 0.965f),
        onErrorContainer = lerp(inkStrong, danger, 0.20f),
        background = background,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = lerp(background, neutralAccent, 0.46f),
        onSurfaceVariant = inkMuted,
        outline = lerp(inkFaint, inkMuted, 0.22f).copy(alpha = 0.66f),
        outlineVariant = lerp(neutralAccent, background, 0.45f),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = lerp(surface, background, 0.42f),
        surfaceContainer = background,
        surfaceContainerHigh = lerp(background, neutralAccent, 0.18f),
        surfaceContainerHighest = lerp(background, neutralAccent, 0.30f),
        surfaceDim = lerp(background, ink, 0.06f),
        surfaceBright = Color.White,
    )

    val chartPalette = listOf(
        base,
        progressAccent,
        control,
        encourage,
        lerp(base, neutralAccent, 0.38f),
        lerp(control, neutralAccent, 0.36f),
        lerp(encourage, neutralAccent, 0.34f),
        lerp(base, Color.White, 0.34f),
        lerp(progressAccent, Color.White, 0.28f),
        lerp(base, inkStrong, 0.08f),
    )

    val glassOverlay = lerp(
        surface.copy(alpha = 0.78f),
        primaryContainer.copy(alpha = 0.74f),
        0.24f,
    )
    val subtleAccent = lerp(neutralAccent, primaryContainer, 0.34f)
    val navSelectedContainer = lerp(primaryContainer, surface, 0.26f)
    val navUnselected = lerp(inkMuted, background, 0.18f)

    return ThemeTokens(
        seed = seed,
        colorScheme = scheme,
        inkStrong = inkStrong,
        ink = ink,
        inkMuted = inkMuted,
        inkFaint = inkFaint,
        inkOnAccent = readableOn(primary),
        control = control,
        onControl = readableOn(control),
        controlContainer = controlContainer,
        onControlContainer = lerp(inkStrong, control, 0.16f),
        encourage = encourage,
        onEncourage = readableOn(encourage),
        encourageContainer = encourageContainer,
        onEncourageContainer = lerp(inkStrong, encourage, 0.16f),
        base = base,
        onBase = readableOn(base),
        baseContainer = baseContainer,
        onBaseContainer = inkStrong,
        success = success,
        warning = warning,
        danger = danger,
        pageGradient = listOf(background, lerp(background, primaryContainer, 0.32f), background),
        chartPalette = chartPalette,
        achievementPalette = listOf(
            lerp(control, base, 0.18f),
            lerp(control, encourage, 0.25f),
            lerp(base, encourage, 0.30f),
            encourage,
            lerp(base, Color.White, 0.20f),
        ),
        glassOverlay = glassOverlay,
        subtleAccent = subtleAccent,
        progressAccent = progressAccent,
        navSelectedContainer = navSelectedContainer,
        navUnselected = navUnselected,
    )
}

private fun refinedInk(seedInk: Color, base: Color): Color =
    lerp(seedInk, base, 0.08f)

private fun refinedInkStrong(seedInk: Color, base: Color): Color =
    lerp(seedInk, base, 0.18f)

private fun refinedInkMuted(ink: Color, seedMuted: Color, background: Color): Color =
    lerp(lerp(ink, background, 0.42f), seedMuted, 0.34f)

private fun refinedInkFaint(ink: Color, background: Color): Color =
    lerp(ink, background, 0.62f)

fun ThemeSeed.localizedName(): String {
    if (isCustom) return name
    val key = "theme_${id}_name"
    val value = AppText.t(key)
    return if (value == key) name else value
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
