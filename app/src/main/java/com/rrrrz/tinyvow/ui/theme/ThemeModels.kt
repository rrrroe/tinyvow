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
import java.time.LocalDate
import java.lang.Math.floorMod

@Stable
data class ThemeSeed(
    val id: String,
    val name: String,
    val backgroundColor: Int,
    val surfaceColor: Int,
    val textColor: Int,
    val mutedTextColor: Int,
    val primaryColor: Int,
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
    val surfaceSoft: Color,
    val surfaceGlass: Color,
    val borderSoft: Color,
    val dividerSoft: Color,
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
    val save: Color,
    val onSave: Color,
    val saveContainer: Color,
    val onSaveContainer: Color,
    val restraint: Color,
    val onRestraint: Color,
    val restraintContainer: Color,
    val onRestraintContainer: Color,
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
    val navSelectedContainer: Color,
    val navUnselected: Color,
)

const val DailyRandomThemeId = "daily_random"

val ThemePresets = listOf(
    ThemeSeed(
        id = "preset_fresh_glow",
        name = "Default",
        backgroundColor = 0xFFF6FAFF.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF1B2A3A.toInt(),
        mutedTextColor = 0xFF667789.toInt(),
        primaryColor = 0xFF78A6D8.toInt(),
        controlColor = 0xFFF38181.toInt(),
        encourageColor = 0xFF72D6B0.toInt(),
        baseColor = 0xFF78A6D8.toInt(),
        neutralAccentColor = 0xFFE3EEF9.toInt(),
    ),
    ThemeSeed(
        id = "preset_soft_mist_blue",
        name = "Soft Mist Blue",
        backgroundColor = 0xFFF2F7FA.toInt(),
        surfaceColor = 0xFFFFFEFC.toInt(),
        textColor = 0xFF1F2D38.toInt(),
        mutedTextColor = 0xFF687986.toInt(),
        primaryColor = 0xFF6F9EC7.toInt(),
        controlColor = 0xFFE27F73.toInt(),
        encourageColor = 0xFF63BDA7.toInt(),
        baseColor = 0xFF6F9EC7.toInt(),
        neutralAccentColor = 0xFFDCE9F0.toInt(),
    ),
    ThemeSeed(
        id = "preset_sage_morning",
        name = "Sage Morning",
        backgroundColor = 0xFFF4F8F3.toInt(),
        surfaceColor = 0xFFFFFEFB.toInt(),
        textColor = 0xFF243226.toInt(),
        mutedTextColor = 0xFF6F806F.toInt(),
        primaryColor = 0xFF7BA982.toInt(),
        controlColor = 0xFFE08A67.toInt(),
        encourageColor = 0xFF5EAFC0.toInt(),
        baseColor = 0xFF7BA982.toInt(),
        neutralAccentColor = 0xFFDDEBDD.toInt(),
    ),
    ThemeSeed(
        id = "preset_lavender_haze",
        name = "Lavender Haze",
        backgroundColor = 0xFFF6F4FA.toInt(),
        surfaceColor = 0xFFFFFEFF.toInt(),
        textColor = 0xFF2A2638.toInt(),
        mutedTextColor = 0xFF77718A.toInt(),
        primaryColor = 0xFF9A8BC9.toInt(),
        controlColor = 0xFFE28A92.toInt(),
        encourageColor = 0xFF69B99C.toInt(),
        baseColor = 0xFF9A8BC9.toInt(),
        neutralAccentColor = 0xFFE6E1F0.toInt(),
    ),
    ThemeSeed(
        id = "preset_apricot_clay",
        name = "Apricot Clay",
        backgroundColor = 0xFFF8F4EF.toInt(),
        surfaceColor = 0xFFFFFEFA.toInt(),
        textColor = 0xFF342922.toInt(),
        mutedTextColor = 0xFF80756D.toInt(),
        primaryColor = 0xFFD09A72.toInt(),
        controlColor = 0xFFC8787E.toInt(),
        encourageColor = 0xFF62AD91.toInt(),
        baseColor = 0xFFD09A72.toInt(),
        neutralAccentColor = 0xFFECE0D4.toInt(),
    ),
    ThemeSeed(
        id = "preset_seasalt_teal",
        name = "Seasalt Teal",
        backgroundColor = 0xFFF2F8F7.toInt(),
        surfaceColor = 0xFFFFFFFC.toInt(),
        textColor = 0xFF213233.toInt(),
        mutedTextColor = 0xFF6E8180.toInt(),
        primaryColor = 0xFF63A9A4.toInt(),
        controlColor = 0xFFE08A6E.toInt(),
        encourageColor = 0xFF7AA8D8.toInt(),
        baseColor = 0xFF63A9A4.toInt(),
        neutralAccentColor = 0xFFDCEDEA.toInt(),
    ),
)

val MemberThemePresets = emptyList<ThemeSeed>()

val DefaultThemeSeed = ThemePresets[0]

val LocalThemeColors = staticCompositionLocalOf {
    themeTokensFromSeed(DefaultThemeSeed)
}

fun resolveThemeSeed(
    selectedThemeId: String?,
    customThemes: List<ThemeSeed>,
    today: LocalDate = LocalDate.now(),
): ThemeSeed {
    if (selectedThemeId == DailyRandomThemeId) return DefaultThemeSeed
    val themes = ThemePresets + MemberThemePresets + customThemes
    return themes.firstOrNull { it.id == selectedThemeId } ?: DefaultThemeSeed
}

fun dailyRandomThemeSeed(today: LocalDate = LocalDate.now()): ThemeSeed {
    return ThemePresets[dailyRandomThemeIndex(today, ThemePresets.size)]
}

internal fun dailyRandomThemeIndex(
    today: LocalDate,
    presetCount: Int,
): Int {
    require(presetCount > 0) { "presetCount must be greater than 0." }
    val mixed = mixDailyThemeSeed(today.toEpochDay())
    return floorMod(mixed, presetCount.toLong()).toInt()
}

private fun mixDailyThemeSeed(epochDay: Long): Long {
    var value = epochDay - 7_046_029_254_386_353_131L
    value = (value xor (value ushr 30)) * -4_658_895_280_553_007_687L
    value = (value xor (value ushr 27)) * -7_723_592_293_110_705_685L
    return value xor (value ushr 31)
}

fun selectedThemeDisplayName(
    selectedThemeId: String?,
    customThemes: List<ThemeSeed>,
    today: LocalDate = LocalDate.now(),
): String = resolveThemeSeed(selectedThemeId, customThemes, today).localizedName()

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
        controlColor = rotateHue(base, -28f).toArgb(),
        encourageColor = rotateHue(base, 96f).toArgb(),
        baseColor = base.toArgb(),
        neutralAccentColor = tone(base, saturationMultiplier = 0.10f, value = 0.92f).toArgb(),
        isCustom = true,
    )
}

fun themeTokensFromSeed(seed: ThemeSeed): ThemeTokens {
    val isFreshGlow = seed.id.startsWith("preset_fresh_glow")
    val background = Color(seed.backgroundColor)
    val surface = Color(seed.surfaceColor)
    val primary = Color(seed.primaryColor)
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
    val surfaceSoft = when {
        isFreshGlow -> lerp(surface, background, 0.26f)
        else -> lerp(surface, background, 0.18f)
    }
    val surfaceGlass = if (isFreshGlow) Color.White.copy(alpha = 0.78f) else surface.copy(alpha = 0.78f)
    val borderSoft = when {
        isFreshGlow -> lerp(neutralAccent, background, 0.50f)
        else -> lerp(neutralAccent, background, 0.45f)
    }
    val dividerSoft = when {
        isFreshGlow -> lerp(neutralAccent, background, 0.66f)
        else -> lerp(neutralAccent, background, 0.58f)
    }
    val baseContainer = when {
        isFreshGlow -> roleContainer(surface, background, base, neutralAccent, 0.18f)
        else -> lerp(surface, neutralAccent, 0.74f)
    }
    val controlContainer = when {
        isFreshGlow -> roleContainer(surface, background, control, neutralAccent, 0.18f)
        else -> roleContainer(surface, background, control, neutralAccent, 0.28f)
    }
    val encourageContainer = when {
        isFreshGlow -> roleContainer(surface, background, encourage, neutralAccent, 0.18f)
        else -> roleContainer(surface, background, encourage, neutralAccent, 0.30f)
    }
    val save = when {
        isFreshGlow -> lerp(base, encourage, 0.46f)
        else -> lerp(base, Color(0xFF5CA9F2), 0.36f)
    }
    val saveContainer = when {
        isFreshGlow -> roleContainer(surface, background, save, neutralAccent, 0.16f)
        else -> roleContainer(surface, background, save, neutralAccent, 0.24f)
    }
    val restraint = when {
        isFreshGlow -> lerp(base, control, 0.46f)
        else -> lerp(control, Color(0xFFD95F86), 0.42f)
    }
    val restraintContainer = when {
        isFreshGlow -> roleContainer(surface, background, restraint, neutralAccent, 0.16f)
        else -> roleContainer(surface, background, restraint, neutralAccent, 0.22f)
    }
    val primaryContainer = if (isFreshGlow) baseContainer else lerp(surface, primary, 0.13f)
    val danger = if (isFreshGlow) lerp(control, Color(0xFF9B6A61), 0.22f) else lerp(control, Color(0xFF7F3E35), 0.18f)
    val success = if (isFreshGlow) encourage else lerp(encourage, Color(0xFF385D42), 0.16f)
    val warning = if (isFreshGlow) control else lerp(base, control, 0.28f)

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
        outlineVariant = borderSoft,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = surfaceSoft,
        surfaceContainer = background,
        surfaceContainerHigh = if (isFreshGlow) lerp(background, neutralAccent, 0.16f) else lerp(background, neutralAccent, 0.18f),
        surfaceContainerHighest = if (isFreshGlow) lerp(background, neutralAccent, 0.26f) else lerp(background, neutralAccent, 0.30f),
        surfaceDim = lerp(background, ink, 0.06f),
        surfaceBright = Color.White,
    )

    val chartPalette = listOf(
        base,
        control,
        encourage,
        save,
        restraint,
        lerp(base, encourage, 0.34f),
        lerp(control, save, 0.36f),
        lerp(encourage, save, 0.34f),
        lerp(base, Color.White, 0.34f),
        lerp(control, Color.White, 0.30f),
        lerp(encourage, Color.White, 0.28f),
        lerp(base, inkStrong, 0.08f),
    )

    val glassOverlay = if (isFreshGlow) {
        surfaceGlass
    } else {
        lerp(
            surface.copy(alpha = 0.78f),
            primaryContainer.copy(alpha = 0.74f),
            0.24f,
        )
    }
    val subtleAccent = if (isFreshGlow) lerp(neutralAccent, surface, 0.44f) else lerp(neutralAccent, primaryContainer, 0.34f)
    val navSelectedContainer = if (isFreshGlow) lerp(primaryContainer, surface, 0.32f) else lerp(primaryContainer, surface, 0.26f)
    val navUnselected = lerp(inkMuted, background, 0.18f)

    return ThemeTokens(
        seed = seed,
        colorScheme = scheme,
        surfaceSoft = surfaceSoft,
        surfaceGlass = surfaceGlass,
        borderSoft = borderSoft,
        dividerSoft = dividerSoft,
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
        save = save,
        onSave = readableOn(save),
        saveContainer = saveContainer,
        onSaveContainer = lerp(inkStrong, save, 0.16f),
        restraint = restraint,
        onRestraint = readableOn(restraint),
        restraintContainer = restraintContainer,
        onRestraintContainer = lerp(inkStrong, restraint, 0.16f),
        base = base,
        onBase = readableOn(base),
        baseContainer = baseContainer,
        onBaseContainer = inkStrong,
        success = success,
        warning = warning,
        danger = danger,
        pageGradient = listOf(background, lerp(background, primaryContainer, 0.32f), background),
        chartPalette = chartPalette,
        achievementPalette =
            if (isFreshGlow) {
                listOf(base, encourage, save, control, restraint)
            } else {
                listOf(
                    lerp(control, base, 0.18f),
                    lerp(control, encourage, 0.25f),
                    lerp(base, encourage, 0.30f),
                    encourage,
                    lerp(base, Color.White, 0.20f),
                )
            },
        glassOverlay = glassOverlay,
        subtleAccent = subtleAccent,
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

private fun roleContainer(
    surface: Color,
    background: Color,
    role: Color,
    neutralAccent: Color,
    roleWeight: Float,
): Color =
    lerp(
        lerp(surface, background, 0.28f),
        lerp(role, neutralAccent, 0.18f),
        roleWeight,
    )

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


