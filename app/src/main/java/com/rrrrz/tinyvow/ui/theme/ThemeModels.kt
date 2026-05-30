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
    val navSelectedContainer: Color,
    val navUnselected: Color,
)

const val DailyRandomThemeId = "daily_random"

val ThemePresets = listOf(
    ThemeSeed(
        id = "preset_porcelain_grove",
        name = "Porcelain Grove",
        backgroundColor = 0xFFF4F8F6.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF1F312F.toInt(),
        mutedTextColor = 0xFF74837E.toInt(),
        primaryColor = 0xFF275F58.toInt(),
        controlColor = 0xFFB8762C.toInt(),
        encourageColor = 0xFF2E8066.toInt(),
        baseColor = 0xFF275F58.toInt(),
        neutralAccentColor = 0xFFDDEAE4.toInt(),
    ),
    ThemeSeed(
        id = "preset_linen_amber",
        name = "Linen Amber",
        backgroundColor = 0xFFF8F3EA.toInt(),
        surfaceColor = 0xFFFFFEFA.toInt(),
        textColor = 0xFF33291F.toInt(),
        mutedTextColor = 0xFF897E70.toInt(),
        primaryColor = 0xFF8A6A2F.toInt(),
        controlColor = 0xFF3F6E9A.toInt(),
        encourageColor = 0xFF9B6A28.toInt(),
        baseColor = 0xFF8A6A2F.toInt(),
        neutralAccentColor = 0xFFEAE0D0.toInt(),
    ),
    ThemeSeed(
        id = "preset_lotus_graphite",
        name = "Lotus Graphite",
        backgroundColor = 0xFFF7F3F6.toInt(),
        surfaceColor = 0xFFFFFCFE.toInt(),
        textColor = 0xFF302532.toInt(),
        mutedTextColor = 0xFF877B86.toInt(),
        primaryColor = 0xFF6E5270.toInt(),
        controlColor = 0xFF9D4C8B.toInt(),
        encourageColor = 0xFF6D7F36.toInt(),
        baseColor = 0xFF6E5270.toInt(),
        neutralAccentColor = 0xFFE9DDE5.toInt(),
    ),
    ThemeSeed(
        id = "preset_glacier_steel",
        name = "Glacier Steel",
        backgroundColor = 0xFFF3F7FA.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF1D2B36.toInt(),
        mutedTextColor = 0xFF74818A.toInt(),
        primaryColor = 0xFF37677E.toInt(),
        controlColor = 0xFF5265A8.toInt(),
        encourageColor = 0xFFC46B55.toInt(),
        baseColor = 0xFF37677E.toInt(),
        neutralAccentColor = 0xFFDCE8EE.toInt(),
    ),
    ThemeSeed(
        id = "preset_cedar_mint",
        name = "Cedar Mint",
        backgroundColor = 0xFFF3F7F1.toInt(),
        surfaceColor = 0xFFFFFFFC.toInt(),
        textColor = 0xFF253225.toInt(),
        mutedTextColor = 0xFF788573.toInt(),
        primaryColor = 0xFF3E6C45.toInt(),
        controlColor = 0xFF8A5A32.toInt(),
        encourageColor = 0xFF3C8A6D.toInt(),
        baseColor = 0xFF3E6C45.toInt(),
        neutralAccentColor = 0xFFDDE8D8.toInt(),
    ),
    ThemeSeed(
        id = "preset_rose_quartz",
        name = "Rose Quartz",
        backgroundColor = 0xFFF9F3F4.toInt(),
        surfaceColor = 0xFFFFFCFC.toInt(),
        textColor = 0xFF34262A.toInt(),
        mutedTextColor = 0xFF8A7B80.toInt(),
        primaryColor = 0xFF8A5F68.toInt(),
        controlColor = 0xFFA64E6B.toInt(),
        encourageColor = 0xFF4E7890.toInt(),
        baseColor = 0xFF8A5F68.toInt(),
        neutralAccentColor = 0xFFECDDE1.toInt(),
    ),
    ThemeSeed(
        id = "preset_indigo_mist",
        name = "Indigo Mist",
        backgroundColor = 0xFFF4F5FA.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF22283D.toInt(),
        mutedTextColor = 0xFF777D90.toInt(),
        primaryColor = 0xFF4D5E95.toInt(),
        controlColor = 0xFF6A58A8.toInt(),
        encourageColor = 0xFF3B8D84.toInt(),
        baseColor = 0xFF4D5E95.toInt(),
        neutralAccentColor = 0xFFE0E3F0.toInt(),
    ),
    ThemeSeed(
        id = "preset_terracotta_sage",
        name = "Terracotta Sage",
        backgroundColor = 0xFFF8F1EA.toInt(),
        surfaceColor = 0xFFFFFCF8.toInt(),
        textColor = 0xFF35261D.toInt(),
        mutedTextColor = 0xFF8B7C70.toInt(),
        primaryColor = 0xFF986249.toInt(),
        controlColor = 0xFFB75A3D.toInt(),
        encourageColor = 0xFF697E55.toInt(),
        baseColor = 0xFF986249.toInt(),
        neutralAccentColor = 0xFFEADDD0.toInt(),
    ),
    ThemeSeed(
        id = "preset_marine_sand",
        name = "Marine Sand",
        backgroundColor = 0xFFF2F7F7.toInt(),
        surfaceColor = 0xFFFFFFFD.toInt(),
        textColor = 0xFF203333.toInt(),
        mutedTextColor = 0xFF738585.toInt(),
        primaryColor = 0xFF2D7474.toInt(),
        controlColor = 0xFF2E5F88.toInt(),
        encourageColor = 0xFFB99A42.toInt(),
        baseColor = 0xFF2D7474.toInt(),
        neutralAccentColor = 0xFFDCE9E8.toInt(),
    ),
    ThemeSeed(
        id = "preset_orchid_ash",
        name = "Orchid Ash",
        backgroundColor = 0xFFF7F5F8.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF2C2935.toInt(),
        mutedTextColor = 0xFF817B89.toInt(),
        primaryColor = 0xFF6F6384.toInt(),
        controlColor = 0xFF8B5AA0.toInt(),
        encourageColor = 0xFF2F8A9A.toInt(),
        baseColor = 0xFF6F6384.toInt(),
        neutralAccentColor = 0xFFE7E1EA.toInt(),
    ),
    ThemeSeed(
        id = "preset_copper_pearl",
        name = "Copper Pearl",
        backgroundColor = 0xFFF8F4EF.toInt(),
        surfaceColor = 0xFFFFFEFB.toInt(),
        textColor = 0xFF332821.toInt(),
        mutedTextColor = 0xFF897F77.toInt(),
        primaryColor = 0xFF896343.toInt(),
        controlColor = 0xFFA86235.toInt(),
        encourageColor = 0xFF3B8065.toInt(),
        baseColor = 0xFF896343.toInt(),
        neutralAccentColor = 0xFFE9E0D6.toInt(),
    ),
    ThemeSeed(
        id = "preset_pine_lime",
        name = "Pine Lime",
        backgroundColor = 0xFFF4F7EF.toInt(),
        surfaceColor = 0xFFFFFEFA.toInt(),
        textColor = 0xFF22311E.toInt(),
        mutedTextColor = 0xFF778370.toInt(),
        primaryColor = 0xFF365F31.toInt(),
        controlColor = 0xFF9B4F64.toInt(),
        encourageColor = 0xFF79A83E.toInt(),
        baseColor = 0xFF365F31.toInt(),
        neutralAccentColor = 0xFFE0E9D6.toInt(),
    ),
    ThemeSeed(
        id = "preset_slate_apricot",
        name = "Slate Apricot",
        backgroundColor = 0xFFF5F6F4.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF252D30.toInt(),
        mutedTextColor = 0xFF798184.toInt(),
        primaryColor = 0xFF4D666C.toInt(),
        controlColor = 0xFF7A5A88.toInt(),
        encourageColor = 0xFFC2874B.toInt(),
        baseColor = 0xFF4D666C.toInt(),
        neutralAccentColor = 0xFFE0E5E4.toInt(),
    ),
    ThemeSeed(
        id = "preset_cobalt_calm",
        name = "Cobalt Calm",
        backgroundColor = 0xFFF2F5FA.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF1E2A40.toInt(),
        mutedTextColor = 0xFF727D91.toInt(),
        primaryColor = 0xFF315E99.toInt(),
        controlColor = 0xFF91425D.toInt(),
        encourageColor = 0xFF3B78A4.toInt(),
        baseColor = 0xFF315E99.toInt(),
        neutralAccentColor = 0xFFDDE5F1.toInt(),
    ),
    ThemeSeed(
        id = "preset_mulberry_leaf",
        name = "Mulberry Leaf",
        backgroundColor = 0xFFF8F3F6.toInt(),
        surfaceColor = 0xFFFFFCFE.toInt(),
        textColor = 0xFF332431.toInt(),
        mutedTextColor = 0xFF897A86.toInt(),
        primaryColor = 0xFF7F4E71.toInt(),
        controlColor = 0xFF8E426C.toInt(),
        encourageColor = 0xFF6F873C.toInt(),
        baseColor = 0xFF7F4E71.toInt(),
        neutralAccentColor = 0xFFEADDE6.toInt(),
    ),
    ThemeSeed(
        id = "preset_seafoam_coral",
        name = "Seafoam Coral",
        backgroundColor = 0xFFF1F8F5.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF1F322D.toInt(),
        mutedTextColor = 0xFF72847F.toInt(),
        primaryColor = 0xFF34806F.toInt(),
        controlColor = 0xFFC85E61.toInt(),
        encourageColor = 0xFF2E8C79.toInt(),
        baseColor = 0xFF34806F.toInt(),
        neutralAccentColor = 0xFFD9ECE5.toInt(),
    ),
    ThemeSeed(
        id = "preset_walnut_cream",
        name = "Walnut Cream",
        backgroundColor = 0xFFF7F2EA.toInt(),
        surfaceColor = 0xFFFFFEFA.toInt(),
        textColor = 0xFF31261D.toInt(),
        mutedTextColor = 0xFF877D72.toInt(),
        primaryColor = 0xFF6E5540.toInt(),
        controlColor = 0xFF8A5D3B.toInt(),
        encourageColor = 0xFF7B6791.toInt(),
        baseColor = 0xFF6E5540.toInt(),
        neutralAccentColor = 0xFFE7DDD0.toInt(),
    ),
    ThemeSeed(
        id = "preset_aqua_graphite",
        name = "Aqua Graphite",
        backgroundColor = 0xFFF2F8F9.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF1F3038.toInt(),
        mutedTextColor = 0xFF72848B.toInt(),
        primaryColor = 0xFF25798A.toInt(),
        controlColor = 0xFF6E5AA0.toInt(),
        encourageColor = 0xFFB8A33B.toInt(),
        baseColor = 0xFF25798A.toInt(),
        neutralAccentColor = 0xFFD9EAED.toInt(),
    ),
    ThemeSeed(
        id = "preset_plum_oat",
        name = "Plum Oat",
        backgroundColor = 0xFFF7F2EF.toInt(),
        surfaceColor = 0xFFFFFEFB.toInt(),
        textColor = 0xFF342631.toInt(),
        mutedTextColor = 0xFF887B82.toInt(),
        primaryColor = 0xFF76556C.toInt(),
        controlColor = 0xFF8E4C75.toInt(),
        encourageColor = 0xFF9C7A38.toInt(),
        baseColor = 0xFF76556C.toInt(),
        neutralAccentColor = 0xFFE8DDD9.toInt(),
    ),
    ThemeSeed(
        id = "preset_bamboo_ink",
        name = "Bamboo Ink",
        backgroundColor = 0xFFF4F6EF.toInt(),
        surfaceColor = 0xFFFFFEFA.toInt(),
        textColor = 0xFF243021.toInt(),
        mutedTextColor = 0xFF78816F.toInt(),
        primaryColor = 0xFF526C37.toInt(),
        controlColor = 0xFF8A623C.toInt(),
        encourageColor = 0xFF365F80.toInt(),
        baseColor = 0xFF526C37.toInt(),
        neutralAccentColor = 0xFFE2E7D6.toInt(),
    ),
    ThemeSeed(
        id = "preset_plum_ink",
        name = "Plum Ink",
        backgroundColor = 0xFFFBF6FA.toInt(),
        surfaceColor = 0xFFFFF8FC.toInt(),
        textColor = 0xFF2F2630.toInt(),
        mutedTextColor = 0xFF887786.toInt(),
        primaryColor = 0xFF8A637A.toInt(),
        controlColor = 0xFFA67893.toInt(),
        encourageColor = 0xFF657B73.toInt(),
        baseColor = 0xFF8A637A.toInt(),
        neutralAccentColor = 0xFFEADDE5.toInt(),
    ),
    ThemeSeed(
        id = "preset_paper_latte",
        name = "Paper Latte",
        backgroundColor = 0xFFFAF3E9.toInt(),
        surfaceColor = 0xFFFFFDF8.toInt(),
        textColor = 0xFF342B27.toInt(),
        mutedTextColor = 0xFF8D7E74.toInt(),
        primaryColor = 0xFFA36F4C.toInt(),
        controlColor = 0xFFB46C48.toInt(),
        encourageColor = 0xFF6F7B57.toInt(),
        baseColor = 0xFFA36F4C.toInt(),
        neutralAccentColor = 0xFFE6D6C3.toInt(),
    ),
    ThemeSeed(
        id = "preset_amber_lamp",
        name = "Amber Lamp",
        backgroundColor = 0xFFFCF2E2.toInt(),
        surfaceColor = 0xFFFFF9EE.toInt(),
        textColor = 0xFF3A2A20.toInt(),
        mutedTextColor = 0xFF8B7A6B.toInt(),
        primaryColor = 0xFF9B6E34.toInt(),
        controlColor = 0xFFD39A54.toInt(),
        encourageColor = 0xFF6F7543.toInt(),
        baseColor = 0xFF9B6E34.toInt(),
        neutralAccentColor = 0xFFE9D7B5.toInt(),
    ),
    ThemeSeed(
        id = "preset_iris_cover",
        name = "Iris Cover",
        backgroundColor = 0xFFF5F4FB.toInt(),
        surfaceColor = 0xFFFFFEFF.toInt(),
        textColor = 0xFF262538.toInt(),
        mutedTextColor = 0xFF7F7A8F.toInt(),
        primaryColor = 0xFF6B668D.toInt(),
        controlColor = 0xFF8B86B5.toInt(),
        encourageColor = 0xFF557D8A.toInt(),
        baseColor = 0xFF6B668D.toInt(),
        neutralAccentColor = 0xFFE2E0EF.toInt(),
    ),
    ThemeSeed(
        id = "preset_mist_blue",
        name = "Mist Blue",
        backgroundColor = 0xFFF3F7F9.toInt(),
        surfaceColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFF22313B.toInt(),
        mutedTextColor = 0xFF71808A.toInt(),
        primaryColor = 0xFF5C7D90.toInt(),
        controlColor = 0xFF6B6F86.toInt(),
        encourageColor = 0xFF5D8C8A.toInt(),
        baseColor = 0xFF5C7D90.toInt(),
        neutralAccentColor = 0xFFDDE7EC.toInt(),
    ),
    ThemeSeed(
        id = "preset_forest_shelf",
        name = "Forest Shelf",
        backgroundColor = 0xFFF5F4EC.toInt(),
        surfaceColor = 0xFFFFFCF4.toInt(),
        textColor = 0xFF243020.toInt(),
        mutedTextColor = 0xFF7B8173.toInt(),
        primaryColor = 0xFF60794F.toInt(),
        controlColor = 0xFF8B6A47.toInt(),
        encourageColor = 0xFF6F8D55.toInt(),
        baseColor = 0xFF60794F.toInt(),
        neutralAccentColor = 0xFFE3E0CE.toInt(),
    ),
    ThemeSeed(
        id = "preset_espresso_note",
        name = "Espresso Note",
        backgroundColor = 0xFFF8F0E8.toInt(),
        surfaceColor = 0xFFFFFDF8.toInt(),
        textColor = 0xFF30251F.toInt(),
        mutedTextColor = 0xFF88796E.toInt(),
        primaryColor = 0xFF7B5946.toInt(),
        controlColor = 0xFF8C6953.toInt(),
        encourageColor = 0xFF5E7665.toInt(),
        baseColor = 0xFF7B5946.toInt(),
        neutralAccentColor = 0xFFE7D8C8.toInt(),
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
        controlColor = 0xFFB85F50.toInt(),
        encourageColor = 0xFF547A35.toInt(),
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
        controlColor = 0xFFB6647B.toInt(),
        encourageColor = 0xFF4F7B91.toInt(),
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
        controlColor = 0xFFB85878.toInt(),
        encourageColor = 0xFF718044.toInt(),
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
    today: LocalDate = LocalDate.now(),
): ThemeSeed {
    if (selectedThemeId == DailyRandomThemeId) return dailyRandomThemeSeed(today)
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
): String =
    if (selectedThemeId == DailyRandomThemeId) {
        AppText.t("theme_random_daily_selected", dailyRandomThemeSeed(today).localizedName())
    } else {
        resolveThemeSeed(selectedThemeId, customThemes, today).localizedName()
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
    val controlContainer = roleContainer(surface, background, control, neutralAccent, 0.28f)
    val encourageContainer = roleContainer(surface, background, encourage, neutralAccent, 0.30f)
    val primaryContainer = lerp(surface, primary, 0.13f)
    val danger = lerp(control, Color(0xFF7F3E35), 0.18f)
    val success = lerp(encourage, Color(0xFF385D42), 0.16f)
    val warning = lerp(base, control, 0.28f)

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
        control,
        encourage,
        lerp(base, encourage, 0.34f),
        lerp(base, neutralAccent, 0.38f),
        lerp(control, neutralAccent, 0.36f),
        lerp(encourage, neutralAccent, 0.34f),
        lerp(base, Color.White, 0.34f),
        lerp(control, Color.White, 0.30f),
        lerp(encourage, Color.White, 0.28f),
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

