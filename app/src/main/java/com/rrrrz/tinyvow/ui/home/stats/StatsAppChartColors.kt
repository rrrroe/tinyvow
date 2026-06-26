package com.rrrrz.tinyvow.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.settings.StoredAppColorPreferences
import com.rrrrz.tinyvow.ui.theme.LocalReportColors
import dev.sasikanth.material.color.utilities.quantize.QuantizerCelebi
import dev.sasikanth.material.color.utilities.score.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AppColorAlgorithm(
    val storageValue: String,
    val labelKey: String,
) {
    CURRENT("current", "app_color_algorithm_current"),
    WEIGHTED("weighted", "app_color_algorithm_weighted"),
    PALETTE("palette", "app_color_algorithm_palette"),
    PALETTE_DOMINANT("palette_dominant", "app_color_algorithm_palette_dominant"),
    MATERIAL("material", "app_color_algorithm_material");

    companion object {
        fun fromStorageValue(value: String?): AppColorAlgorithm =
            entries.firstOrNull { it.storageValue == value } ?: CURRENT
    }
}

@Composable
internal fun rememberAppChartColors(
    packageNames: List<String>,
): Map<String, Color> {
    val context = LocalContext.current
    val reportColors = LocalReportColors.current
    val stablePackages = remember(packageNames) { packageNames.distinct() }
    val fallbackColors = remember(reportColors) { reportColors.appChartPalette.ifEmpty { listOf(Color(0xFF4F7DFF)) } }
    val preferences = remember(context) { ManagedAppPreferences(context.applicationContext) }
    val appColorPreferences by preferences.appColorPreferences.collectAsState(
        initial = StoredAppColorPreferences(),
    )
    val initialColors =
        remember(context, stablePackages, fallbackColors, appColorPreferences) {
            resolveAppChartColors(
                context = context.applicationContext,
                packageNames = stablePackages,
                fallbackColors = fallbackColors,
                preferences = appColorPreferences,
            )
        }
    val colors by produceState(
        initialValue = initialColors,
        key1 = stablePackages,
        key2 = appColorPreferences,
        key3 = fallbackColors,
    ) {
        value = initialColors
        value = withContext(Dispatchers.Default) {
            resolveAppChartColors(
                context = context.applicationContext,
                packageNames = stablePackages,
                fallbackColors = fallbackColors,
                preferences = appColorPreferences,
            )
        }
    }
    return colors
}

internal fun resolveAppChartColors(
    context: Context,
    packageNames: List<String>,
    fallbackColors: List<Color>,
    preferences: StoredAppColorPreferences,
): Map<String, Color> {
    val palette = fallbackColors.ifEmpty { listOf(Color(0xFF4F7DFF)) }
    return packageNames.distinct().associateWith { packageName ->
        val fallback = stableAppFallbackColor(packageName, palette)
        resolveAppChartColor(
            context = context,
            packageName = packageName,
            fallback = fallback,
            preferences = preferences,
        )
    }
}

internal fun stableAppFallbackColor(
    packageName: String,
    fallbackColors: List<Color>,
): Color {
    val palette = fallbackColors.ifEmpty { listOf(Color(0xFF4F7DFF)) }
    val index = (packageName.hashCode() and Int.MAX_VALUE) % palette.size
    return palette[index]
}

internal fun resolveAppChartColor(
    context: Context,
    packageName: String,
    fallback: Color,
    preferences: StoredAppColorPreferences,
): Color {
    preferences.selections[packageName]?.let { return Color(it.argb) }
    return extractAppChartColor(
        context = context,
        packageName = packageName,
        fallback = fallback,
        algorithm = AppColorAlgorithm.fromStorageValue(preferences.defaultAlgorithm),
    )
}

internal fun extractAppChartColor(
    context: Context,
    packageName: String,
    fallback: Color,
    algorithm: AppColorAlgorithm = AppColorAlgorithm.CURRENT,
): Color {
    val cacheKey = "$packageName:${fallback.toArgb()}:${algorithm.storageValue}"
    chartColorCache.get(cacheKey)?.let { return Color(it) }
    val drawable = AppVisualCache.getIcon(context, packageName)
        ?: return fallback
    val bitmap = drawable.toBitmap(width = 128, height = 128, config = Bitmap.Config.ARGB_8888)
    val color = extractAlgorithmColor(bitmap, fallback, algorithm)
    chartColorCache.put(cacheKey, color.toArgb())
    return color
}

internal fun extractAlgorithmColor(
    bitmap: Bitmap,
    fallback: Color,
    algorithm: AppColorAlgorithm,
): Color =
    when (algorithm) {
        AppColorAlgorithm.CURRENT -> extractTinyVowChartColor(bitmap, fallback)
        AppColorAlgorithm.WEIGHTED -> extractWeightedDominantColor(bitmap, fallback)
        AppColorAlgorithm.PALETTE -> extractPaletteChartColor(bitmap, fallback)
        AppColorAlgorithm.PALETTE_DOMINANT -> extractPaletteDominantColor(bitmap, fallback)
        AppColorAlgorithm.MATERIAL -> extractMaterialUtilitiesColor(bitmap, fallback)
    }

internal fun extractAllAlgorithmColors(
    bitmap: Bitmap,
    fallback: Color,
): Map<AppColorAlgorithm, Color> =
    AppColorAlgorithm.entries.associateWith { algorithm ->
        extractAlgorithmColor(bitmap, fallback, algorithm)
    }

internal fun sourceForAlgorithm(algorithm: AppColorAlgorithm): String = algorithm.storageValue

internal fun algorithmForAppColorSource(source: String): AppColorAlgorithm? =
    AppColorAlgorithm.entries.firstOrNull { it.storageValue == source }

internal fun isManualAppColorSource(source: String): Boolean =
    source == ManagedAppPreferences.APP_COLOR_SOURCE_MANUAL

internal fun extractTinyVowChartColor(
    bitmap: Bitmap,
    fallback: Color,
): Color {
    val rgb = extractDominantBitmapColor(bitmap) ?: return fallback
    return normalizeChartColor(Color(rgb), fallback)
}

private fun extractWeightedDominantColor(
    bitmap: Bitmap,
    fallback: Color,
): Color {
    val scaled = Bitmap.createScaledBitmap(bitmap, 40, 40, true)
    val buckets = HashMap<Int, Float>()
    for (x in 0 until scaled.width) {
        for (y in 0 until scaled.height) {
            val pixel = scaled.getPixel(x, y)
            val alpha = android.graphics.Color.alpha(pixel)
            if (alpha < 96) continue

            val red = android.graphics.Color.red(pixel)
            val green = android.graphics.Color.green(pixel)
            val blue = android.graphics.Color.blue(pixel)
            val max = maxOf(red, green, blue)
            val min = minOf(red, green, blue)
            val saturation = if (max == 0) 0f else (max - min).toFloat() / max.toFloat()
            val luminance = (0.2126f * red + 0.7152f * green + 0.0722f * blue) / 255f
            val luminanceWeight =
                when {
                    luminance < 0.10f -> 0.22f
                    luminance < 0.22f -> 0.44f
                    luminance > 0.94f -> 0.18f
                    luminance > 0.84f -> 0.44f
                    else -> 1f
                }
            val edgeWeight =
                if (x == 0 || y == 0 || x == scaled.width - 1 || y == scaled.height - 1) {
                    0.45f
                } else {
                    1f
                }
            val weight = (alpha.toFloat() / 255f) * (0.22f + saturation.coerceIn(0f, 1f) * 0.78f) * luminanceWeight * edgeWeight
            if (weight <= 0.02f) continue
            val bucket = ((red shr 4) shl 8) or ((green shr 4) shl 4) or (blue shr 4)
            buckets[bucket] = buckets.getOrDefault(bucket, 0f) + weight
        }
    }
    val bucket = buckets.maxByOrNull { it.value }?.key ?: return fallback
    val red = ((bucket shr 8) and 0xF) * 17
    val green = ((bucket shr 4) and 0xF) * 17
    val blue = (bucket and 0xF) * 17
    return Color(android.graphics.Color.rgb(red, green, blue))
}

private fun extractPaletteChartColor(
    bitmap: Bitmap,
    fallback: Color,
): Color {
    val palette = Palette.from(bitmap)
        .maximumColorCount(16)
        .generate()
    val swatch = listOf(
        palette.vibrantSwatch,
        palette.darkVibrantSwatch,
        palette.lightVibrantSwatch,
        palette.mutedSwatch,
        palette.dominantSwatch,
    ).filterNotNull().firstOrNull()
    return swatch?.rgb?.let { Color(it) } ?: fallback
}

private fun extractPaletteDominantColor(
    bitmap: Bitmap,
    fallback: Color,
): Color {
    val palette = Palette.from(bitmap)
        .maximumColorCount(16)
        .generate()
    return palette.dominantSwatch?.rgb?.let { Color(it) } ?: fallback
}

private fun extractMaterialUtilitiesColor(
    bitmap: Bitmap,
    fallback: Color,
): Color {
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    val scored =
        runCatching {
            Score.score(
                QuantizerCelebi.quantize(pixels, 128),
            )
        }.getOrNull().orEmpty()
    return scored.firstOrNull()?.let { Color(it) } ?: fallback
}

private fun extractDominantBitmapColor(bitmap: Bitmap): Int? {
    val scaled = Bitmap.createScaledBitmap(bitmap, 40, 40, true)
    val buckets = HashMap<Int, ColorBucket>()
    for (x in 0 until scaled.width) {
        for (y in 0 until scaled.height) {
            val pixel = scaled.getPixel(x, y)
            val alpha = android.graphics.Color.alpha(pixel)
            if (alpha < 96) continue

            val red = android.graphics.Color.red(pixel)
            val green = android.graphics.Color.green(pixel)
            val blue = android.graphics.Color.blue(pixel)
            val max = maxOf(red, green, blue)
            val min = minOf(red, green, blue)
            val saturation = if (max == 0) 0f else (max - min).toFloat() / max.toFloat()
            val luminance = (0.2126f * red + 0.7152f * green + 0.0722f * blue) / 255f
            val alphaWeight = alpha.toFloat() / 255f
            val saturationWeight = 0.22f + saturation.coerceIn(0f, 1f) * 0.78f
            val luminanceWeight =
                when {
                    luminance < 0.10f -> 0.22f
                    luminance < 0.22f -> 0.44f
                    luminance > 0.94f -> 0.18f
                    luminance > 0.84f -> 0.44f
                    else -> 1f
                }
            val edgeWeight =
                if (x == 0 || y == 0 || x == scaled.width - 1 || y == scaled.height - 1) {
                    0.45f
                } else {
                    1f
                }
            val weight = alphaWeight * saturationWeight * luminanceWeight * edgeWeight
            if (weight <= 0.02f) continue

            val bucket = ((red shr 4) shl 8) or ((green shr 4) shl 4) or (blue shr 4)
            buckets[bucket] = (buckets[bucket] ?: ColorBucket()).add(weight)
        }
    }

    val dominantBucket =
        buckets
            .maxByOrNull { (bucket, score) ->
                val red = ((bucket shr 8) and 0xF) * 17
                val green = ((bucket shr 4) and 0xF) * 17
                val blue = (bucket and 0xF) * 17
                score.weight * chartColorSuitability(red, green, blue)
            }?.key ?: return null
    val red = ((dominantBucket shr 8) and 0xF) * 17
    val green = ((dominantBucket shr 4) and 0xF) * 17
    val blue = (dominantBucket and 0xF) * 17
    return android.graphics.Color.rgb(red, green, blue)
        .takeIf { isUsableChartColor(red, green, blue) }
}

private data class ColorBucket(
    val weight: Float = 0f,
) {
    fun add(delta: Float): ColorBucket = copy(weight = weight + delta)
}

private fun chartColorSuitability(
    red: Int,
    green: Int,
    blue: Int,
): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(red, green, blue, hsv)
    val saturation = hsv[1]
    val value = hsv[2]
    val luminance = (0.2126f * red + 0.7152f * green + 0.0722f * blue) / 255f
    val saturationScore = 0.12f + saturation.coerceIn(0f, 1f) * 0.88f
    val luminanceScore =
        when {
            luminance < 0.12f -> 0.18f
            luminance < 0.24f -> 0.42f
            luminance > 0.92f -> 0.22f
            luminance > 0.82f -> 0.52f
            else -> 1f
        }
    val muddyPenalty = if (isMuddyBrown(hsv[0], saturation, value)) 0.18f else 1f
    return saturationScore * luminanceScore * muddyPenalty
}

private fun isUsableChartColor(
    red: Int,
    green: Int,
    blue: Int,
): Boolean {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(red, green, blue, hsv)
    val luminance = (0.2126f * red + 0.7152f * green + 0.0722f * blue) / 255f
    return hsv[1] >= 0.22f &&
        luminance in 0.10f..0.90f &&
        !isMuddyBrown(hsv[0], hsv[1], hsv[2])
}

private fun isMuddyBrown(
    hue: Float,
    saturation: Float,
    value: Float,
): Boolean =
    hue in 18f..55f && saturation < 0.62f && value < 0.68f

private fun normalizeChartColor(
    color: Color,
    fallback: Color,
): Color {
    val luminance = color.luminance()
    return when {
        luminance < 0.08f -> Color(
            red = color.red * 0.55f + fallback.red * 0.45f,
            green = color.green * 0.55f + fallback.green * 0.45f,
            blue = color.blue * 0.55f + fallback.blue * 0.45f,
            alpha = 1f,
        )
        luminance > 0.88f -> Color(
            red = color.red * 0.65f + fallback.red * 0.35f,
            green = color.green * 0.65f + fallback.green * 0.35f,
            blue = color.blue * 0.65f + fallback.blue * 0.35f,
            alpha = 1f,
        )
        else -> color.copy(alpha = 1f)
    }
}

@Composable
internal fun fallbackChartColor(index: Int): Color {
    val colors = LocalReportColors.current.appChartPalette
    return colors[index % colors.size]
}

private val chartColorCache = LruCache<String, Int>(192)
