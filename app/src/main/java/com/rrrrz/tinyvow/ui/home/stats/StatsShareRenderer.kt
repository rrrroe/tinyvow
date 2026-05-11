package com.rrrrz.tinyvow.ui.home

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.ReportColors
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

internal fun shareReportBitmap(
    context: Context,
    bitmap: Bitmap,
) {
    val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
    val file = File(shareDir, "tinyvow-report-${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    val uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, AppText.t("stats_tiny_vow_report"))
            clipData = ClipData.newUri(context.contentResolver, AppText.t("stats_tiny_vow_report"), uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(intent, AppText.t("stats_share_report")))
}

internal fun renderShareReportBitmapV2(
    context: Context,
    data: ShareReportData,
    primary: Color,
    surface: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    palette: List<Color>,
): Bitmap {
    val width = 1080
    val height = 2320
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val primaryArgb = primary.toArgb()
    val textArgb = onSurface.toArgb()
    val mutedArgb = onSurfaceVariant.toArgb()
    val positiveArgb = palette.getOrElse(2) { primary }.toArgb()
    val warningArgb = palette.getOrElse(1) { primary }.toArgb()
    val displayTypeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    val titleTypeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    val bodyTypeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)

    drawSharePosterBackgroundV2(context, canvas, width, height, primary)
    canvas.drawRoundRect(
        RectF(50f, 58f, width - 50f, height - 58f),
        58f,
        58f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surface.copy(alpha = 0.94f).toArgb() },
    )

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 54f
        typeface = titleTypeface
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 30f
        typeface = bodyTypeface
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 26f
        typeface = bodyTypeface
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 30f
        typeface = bodyTypeface
    }
    val sectionTitlePaint = Paint(titlePaint).apply { textSize = 36f }

    drawSharePosterIcon(context, canvas, context.packageName, "T", 104f, 106f, 82f, primaryArgb)
    canvas.drawText(data.title, 208f, 142f, titlePaint)
    canvas.drawText(data.subtitle, 208f, 190f, subtitlePaint)
    drawShareStatusPill(
        canvas,
        RectF(width - 320f, 116f, width - 104f, 176f),
        positiveArgb,
        when (data.tab) {
            ReportTab.DAY -> AppText.t("stats_daily_report")
            ReportTab.WEEK -> AppText.t("stats_weekly_report")
            ReportTab.MONTH -> AppText.t("stats_monthly_report")
            ReportTab.YEAR -> AppText.t("stats_yearly_report")
        },
    )
    val sloganRect = RectF(104f, 214f, 620f, 274f)
    canvas.drawRoundRect(
        sloganRect,
        30f,
        30f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.1f).toArgb() },
    )
    canvas.drawText(
        data.slogan,
        sloganRect.left + 24f,
        sloganRect.centerY() + 10f,
        Paint(bodyPaint).apply {
            color = primaryArgb
            textSize = 28f
            typeface = titleTypeface
        },
    )

    val heroRect = RectF(88f, 320f, width - 88f, 660f)
    val softPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.08f).toArgb() }
    canvas.drawRoundRect(heroRect, 44f, 44f, softPaint)
    canvas.drawRoundRect(
        heroRect,
        44f,
        44f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primary.copy(alpha = 0.16f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 2f
        },
    )
    drawShareTransparentAppIcon(context, canvas, heroRect.right - 360f, heroRect.top + 8f, 360f, 22)
    canvas.drawCircle(132f, heroRect.top + 50f, 11f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = positiveArgb })
    canvas.drawText(
        AppText.t("stats_today_status"),
        156f,
        heroRect.top + 60f,
        Paint(bodyPaint).apply {
            color = positiveArgb
            textSize = 28f
            typeface = titleTypeface
        },
    )
    canvas.drawText(data.statusTitle, 126f, heroRect.top + 124f, Paint(titlePaint).apply { textSize = 42f })
    canvas.drawText(
        data.primaryValue,
        126f,
        heroRect.top + 242f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.getOrElse(0) { primary }.copy(alpha = 0.8f).toArgb()
            textSize = 98f
            typeface = displayTypeface
        },
    )
    canvas.drawText(data.primaryLabel, 126f, heroRect.top + 286f, labelPaint)
    val goalDelta = data.goalMillis - data.totalUsageMillis
    val reviewText =
        if (data.goalMillis > 0L) {
            if (goalDelta >= 0L) AppText.t("stats_remaining_value", formatDuration(goalDelta)) else AppText.t("stats_over_by_value", formatDuration(-goalDelta))
        } else {
            data.comparisonLabel
        }
    canvas.drawText(
        reviewText,
        126f,
        heroRect.top + 334f,
        Paint(bodyPaint).apply {
            color = if (data.goalMillis > 0L && goalDelta < 0L) warningArgb else positiveArgb
            textSize = 30f
            typeface = titleTypeface
        },
    )
    drawShareHeroMetric(
        canvas = canvas,
        rect = RectF(heroRect.right - 292f, heroRect.top + 58f, heroRect.right - 38f, heroRect.top + 144f),
        label = data.metrics.getOrNull(0)?.label ?: AppText.t("stats_label_5"),
        value = data.metrics.getOrNull(0)?.value ?: AppText.t("stats_none"),
        accent = primaryArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )
    drawShareHeroMetric(
        canvas = canvas,
        rect = RectF(heroRect.right - 292f, heroRect.top + 160f, heroRect.right - 38f, heroRect.top + 246f),
        label = data.metrics.getOrNull(1)?.label ?: AppText.t("stats_net_points"),
        value = data.metrics.getOrNull(1)?.value ?: AppText.t("stats_none"),
        accent = warningArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )
    drawShareHeroMetric(
        canvas = canvas,
        rect = RectF(heroRect.right - 292f, heroRect.top + 262f, heroRect.right - 38f, heroRect.top + 348f),
        label = AppText.t("stats_top_apps"),
        value = data.topApps.firstOrNull()?.label ?: AppText.t("stats_none"),
        accent = positiveArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )

    drawShareFocusCards(
        canvas = canvas,
        data = data,
        left = 88f,
        top = 716f,
        width = width - 176f,
        primaryArgb = primaryArgb,
        positiveArgb = positiveArgb,
        warningArgb = warningArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
        surfaceArgb = android.graphics.Color.argb(226, 255, 255, 255),
    )
    drawShareTimelineSection(
        canvas = canvas,
        rect = RectF(88f, 994f, width - 88f, 1388f),
        data = data,
        primary = primary,
        warningArgb = warningArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )
    drawShareTopAppsSection(
        context = context,
        canvas = canvas,
        rect = RectF(88f, 1424f, width - 88f, 1900f),
        apps = data.topApps,
        palette = palette,
        primary = primary,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
    )
    val summaryRect = RectF(88f, 1940f, width - 88f, 2188f)
    canvas.drawRoundRect(summaryRect, 36f, 36f, softPaint)
    canvas.drawText(AppText.t("stats_share_review_sentence"), 124f, summaryRect.top + 58f, sectionTitlePaint)
    canvas.drawText(
        data.subtitle,
        124f,
        summaryRect.top + 100f,
        Paint(subtitlePaint).apply { textSize = 26f },
    )
    drawMultilineText(
        canvas,
        buildSharePosterInsight(data),
        124f,
        summaryRect.top + 154f,
        summaryRect.width() - 72f,
        Paint(bodyPaint).apply { textSize = 30f },
        40f,
        3,
    )
    canvas.drawText(AppText.t("stats_share_footer"), 104f, height - 100f, Paint(subtitlePaint).apply { textSize = 28f })
    return bitmap
}

private fun drawShareHeroMetric(
    canvas: android.graphics.Canvas,
    rect: RectF,
    label: String,
    value: String,
    accent: Int,
    textArgb: Int,
    mutedArgb: Int,
) {
    canvas.drawRoundRect(
        rect,
        24f,
        24f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(
                24,
                android.graphics.Color.red(accent),
                android.graphics.Color.green(accent),
                android.graphics.Color.blue(accent),
            )
        },
    )
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 22f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 26f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    canvas.drawText(label, rect.left + 18f, rect.top + 30f, labelPaint)
    drawEllipsizedText(canvas, value, rect.left + 18f, rect.top + 66f, rect.width() - 36f, valuePaint)
}

private fun drawShareTimelineSection(
    canvas: android.graphics.Canvas,
    rect: RectF,
    data: ShareReportData,
    primary: Color,
    warningArgb: Int,
    textArgb: Int,
    mutedArgb: Int,
) {
    canvas.drawRoundRect(
        rect,
        40f,
        40f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.08f).toArgb() },
    )
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 34f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 22f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    canvas.drawText(
        if (data.tab == ReportTab.DAY) AppText.t("stats_24_hour_distribution") else AppText.t("stats_archive_trend"),
        rect.left + 36f,
        rect.top + 54f,
        titlePaint,
    )
    canvas.drawText(data.comparisonLabel, rect.left + 36f, rect.top + 88f, labelPaint)

    val chartLeft = rect.left + 36f
    val chartTop = rect.top + 126f
    val chartRight = rect.right - 36f
    val chartBottom = rect.top + 286f
    val values =
        when {
            data.tab == ReportTab.DAY && data.hourlyUsageMillis.isNotEmpty() -> data.hourlyUsageMillis.take(24)
            data.hourlyUsageMillis.isNotEmpty() -> data.hourlyUsageMillis
            else -> data.trendUsageMillis
        }.ifEmpty { listOf(0L, 0L, 0L, 0L) }
    val maxValue = maxOf(values.maxOrNull() ?: 0L, data.targetMillisPerBucket ?: 0L, 1L)
    val slotWidth = (chartRight - chartLeft) / values.size.toFloat()
    val barWidth = slotWidth * 0.54f
    val chartHeight = chartBottom - chartTop
    val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(28, android.graphics.Color.red(textArgb), android.graphics.Color.green(textArgb), android.graphics.Color.blue(textArgb))
        strokeWidth = 2f
    }
    repeat(4) { index ->
        val y = chartBottom - chartHeight * (index / 3f)
        canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
    }
    values.forEachIndexed { index, value ->
        val left = chartLeft + slotWidth * index + (slotWidth - barWidth) / 2f
        val heightRatio = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
        val top = chartBottom - chartHeight * heightRatio
        canvas.drawRoundRect(
            RectF(left, top, left + barWidth, chartBottom),
            barWidth / 2f,
            barWidth / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = android.graphics.LinearGradient(
                    left,
                    top,
                    left,
                    chartBottom,
                    primary.copy(alpha = 0.55f).toArgb(),
                    primary.toArgb(),
                    android.graphics.Shader.TileMode.CLAMP,
                )
            },
        )
    }
    data.targetMillisPerBucket?.takeIf { data.tab == ReportTab.DAY && it > 0L }?.let { target ->
        val y = chartBottom - chartHeight * (target.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
        canvas.drawLine(chartLeft, y, chartRight, y, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = warningArgb
            strokeWidth = 3f
        })
    }
    val footerLabels =
        if (data.tab == ReportTab.DAY) {
            listOf("00:00", "06:00", "12:00", "18:00", "24:00")
        } else {
            buildShareTimelineLabels(data.timelineLabels.ifEmpty { data.trendUsageMillis.indices.map { (it + 1).toString() } })
        }
    footerLabels.forEachIndexed { index, label ->
        val x = chartLeft + (chartRight - chartLeft) * (index / (footerLabels.size - 1).toFloat())
        val widthHalf = Paint(labelPaint).measureText(label) / 2f
        canvas.drawText(label, x - widthHalf, chartBottom + 34f, labelPaint)
    }
    val summaryTop = rect.bottom - 72f
    val metricPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 24f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val thirds = (rect.width() - 72f) / 3f
    val items = listOf(
        AppText.t("stats_peak_time") to buildSharePeakValue(data),
        AppText.t("stats_night_use") to formatDuration(data.nightUsageMillis),
        AppText.t("stats_target_complete") to buildShareGoalValue(data),
    )
    items.forEachIndexed { index, (label, value) ->
        val startX = rect.left + 24f + thirds * index
        canvas.drawText(label, startX, summaryTop, labelPaint)
        drawEllipsizedText(canvas, value, startX, summaryTop + 34f, thirds - 16f, metricPaint)
    }
}

private fun buildShareTimelineLabels(labels: List<String>): List<String> {
    if (labels.isEmpty()) return listOf("1", "2", "3", "4")
    if (labels.size <= 4) return labels
    val indexes = listOf(0, labels.lastIndex / 3, (labels.lastIndex * 2) / 3, labels.lastIndex).distinct()
    return indexes.map { labels[it] }
}

private fun buildSharePeakValue(data: ShareReportData): String {
    if (data.hourlyUsageMillis.isEmpty()) return AppText.t("stats_none")
    val index = data.hourlyUsageMillis.indices.maxByOrNull { data.hourlyUsageMillis[it] } ?: return AppText.t("stats_none")
    val label = data.timelineLabels.getOrNull(index) ?: if (data.tab == ReportTab.DAY) dayHourLabel(index) else (index + 1).toString()
    return "$label · ${formatDuration(data.hourlyUsageMillis.getOrElse(index) { 0L })}"
}

private fun buildShareGoalValue(data: ShareReportData): String {
    return if (data.goalProgress != null) {
        "${(data.goalProgress.coerceIn(0f, 1f) * 100f).roundToInt()}%"
    } else {
        data.comparisonLabel
    }
}

private fun drawShareTopAppsSection(
    context: Context,
    canvas: android.graphics.Canvas,
    rect: RectF,
    apps: List<AppDisplayItem>,
    palette: List<Color>,
    primary: Color,
    textArgb: Int,
    mutedArgb: Int,
) {
    canvas.drawRoundRect(
        rect,
        40f,
        40f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.08f).toArgb() },
    )
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 34f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 26f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 22f
    }
    canvas.drawText(AppText.t("stats_top_10_apps"), rect.left + 36f, rect.top + 54f, titlePaint)
    canvas.drawText(AppText.t("stats_current_day_top_10_apps_only"), rect.left + 36f, rect.top + 88f, labelPaint)
    val displayApps = apps.take(4)
    val maxUsage = displayApps.maxOfOrNull { it.value }?.coerceAtLeast(1L) ?: 1L
    displayApps.forEachIndexed { index, app ->
        val rowTop = rect.top + 126f + index * 82f
        val fallbackColor = palette.getOrNull(index) ?: primary
        val appColor = extractAppChartColor(context, app.packageName, fallbackColor)
        drawSharePosterIcon(context, canvas, app.packageName, app.label.take(1), rect.left + 36f, rowTop, 46f, appColor.toArgb())
        canvas.drawText("${index + 1}", rect.left + 6f, rowTop + 28f, Paint(labelPaint).apply {
            this.color = appColor.toArgb()
            textSize = 24f
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        })
        drawEllipsizedText(canvas, app.label, rect.left + 96f, rowTop + 24f, 360f, bodyPaint)
        canvas.drawText(formatDuration(app.value), rect.right - 40f, rowTop + 24f, Paint(bodyPaint).apply {
            textAlign = Paint.Align.RIGHT
        })
        val barTop = rowTop + 42f
        canvas.drawRoundRect(
            RectF(rect.left + 96f, barTop, rect.right - 40f, barTop + 12f),
            8f,
            8f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = appColor.copy(alpha = 0.14f).toArgb() },
        )
        canvas.drawRoundRect(
            RectF(
                rect.left + 96f,
                barTop,
                rect.left + 96f + (rect.right - rect.left - 136f) * (app.value.toFloat() / maxUsage.toFloat()).coerceIn(0.06f, 1f),
                barTop + 12f,
            ),
            8f,
            8f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = appColor.toArgb() },
        )
    }
}

private fun drawSharePosterBackgroundV2(
    context: Context,
    canvas: android.graphics.Canvas,
    width: Int,
    height: Int,
    primary: Color,
) {
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader =
            android.graphics.LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                intArrayOf(
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.10f),
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.18f),
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.26f),
                ),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    canvas.drawOval(
        RectF(660f, -80f, 1240f, 430f),
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.18f).toArgb() },
    )
    canvas.drawOval(
        RectF(-180f, 1320f, 440f, 2020f),
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.12f).toArgb() },
    )
    canvas.drawOval(
        RectF(500f, 1540f, 1120f, 2180f),
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.12f).toArgb() },
    )
    drawShareTransparentAppIcon(context, canvas, 612f, 255f, 560f, 18)
    drawShareTransparentAppIcon(context, canvas, 612f, 1180f, 520f, 14)
}

private fun drawShareTransparentAppIcon(
    context: Context,
    canvas: android.graphics.Canvas,
    left: Float,
    top: Float,
    size: Float,
    alpha: Int,
) {
    val rect = RectF(left, top, left + size, top + size)
    val iconBitmap =
        runCatching {
            context.packageManager.getApplicationIcon(context.packageName).toBitmap(
                width = size.roundToInt(),
                height = size.roundToInt(),
                config = Bitmap.Config.ARGB_8888,
            )
        }.getOrNull()
    if (iconBitmap != null) {
        canvas.drawBitmap(
            iconBitmap,
            null,
            rect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.alpha = alpha },
        )
    } else {
        canvas.drawCircle(
            rect.centerX(),
            rect.centerY(),
            size / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(alpha, 34, 174, 118) },
        )
    }
}

private fun drawShareStatusPill(
    canvas: android.graphics.Canvas,
    rect: RectF,
    accent: Int,
    text: String,
) {
    canvas.drawRoundRect(
        rect,
        28f,
        28f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(24, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
        },
    )
    val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    val checkPath = android.graphics.Path().apply {
        moveTo(rect.left + 30f, rect.centerY() + 2f)
        lineTo(rect.left + 42f, rect.centerY() + 14f)
        lineTo(rect.left + 62f, rect.centerY() - 10f)
    }
    canvas.drawPath(checkPath, checkPaint)
    canvas.drawText(
        text,
        rect.left + 76f,
        rect.centerY() + 12f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            textSize = 28f
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        },
    )
}

private fun drawShareAppConsumption(
    context: Context,
    canvas: android.graphics.Canvas,
    apps: List<AppDisplayItem>,
    centerX: Float,
    centerY: Float,
    radius: Float,
    palette: List<Color>,
    textArgb: Int,
    mutedArgb: Int,
    primary: Color,
) {
    val displayApps = apps.take(5)
    val total = displayApps.sumOf { it.value }.coerceAtLeast(1L)
    val colors =
        displayApps.mapIndexed { index, app ->
            extractAppChartColor(context, app.packageName, palette.getOrNull(index) ?: primary)
        }
    val chartRect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    val stroke = 46f
    canvas.drawArc(
        chartRect,
        -92f,
        360f,
        false,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primary.copy(alpha = 0.08f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.BUTT
        },
    )
    var start = -92f
    displayApps.forEachIndexed { index, app ->
        val sweep = app.value.toFloat() / total.toFloat() * 360f
        canvas.drawArc(
            chartRect,
            start,
            (sweep - 9f).coerceAtLeast(5f),
            false,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors[index].toArgb()
                style = Paint.Style.STROKE
                strokeWidth = stroke
                strokeCap = Paint.Cap.BUTT
            },
        )
        start += sweep
    }
    val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 42f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    }
    val centerLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 28f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    canvas.drawText(if (displayApps.isEmpty()) "--" else formatDuration(total), centerX, centerY - 4f, centerPaint)
    canvas.drawText("Top ${displayApps.size.coerceAtLeast(1)}", centerX, centerY + 48f, centerLabelPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 36f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    canvas.drawText(AppText.t("stats_app_usage"), 552f, centerY - 164f, titlePaint)
    canvas.drawText(
        AppText.t("stats_share"),
        930f,
        centerY - 164f,
        Paint(titlePaint).apply {
            color = mutedArgb
            textSize = 29f
        },
    )
    if (displayApps.isEmpty()) {
        canvas.drawText(AppText.t("stats_no_app_details_yet"), 552f, centerY, Paint(titlePaint).apply { textSize = 30f })
        return
    }
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 28f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 26f
        textAlign = Paint.Align.RIGHT
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val maxValue = displayApps.maxOf { it.value }.coerceAtLeast(1L)
    displayApps.forEachIndexed { index, app ->
        val y = centerY - 126f + index * 66f
        val color = colors[index]
        canvas.drawCircle(558f, y + 23f, 7f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() })
        drawSharePosterIcon(context, canvas, app.packageName, app.label.take(1), 580f, y, 42f, color.toArgb())
        drawEllipsizedText(canvas, app.label, 640f, y + 26f, 220f, namePaint)
        val percent = (app.value.toFloat() / total.toFloat() * 100f).roundToInt()
        canvas.drawText("${formatDuration(app.value)} · $percent%", 976f, y + 27f, valuePaint)
        canvas.drawRoundRect(
            RectF(640f, y + 38f, 836f, y + 49f),
            7f,
            7f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.argb(26, 126, 142, 158) },
        )
        canvas.drawRoundRect(
            RectF(640f, y + 38f, 640f + 196f * (app.value.toFloat() / maxValue.toFloat()).coerceIn(0.06f, 1f), y + 49f),
            7f,
            7f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() },
        )
    }
}

private fun drawShareWeeklyTrend(
    canvas: android.graphics.Canvas,
    values: List<Long>,
    rect: RectF,
    textArgb: Int,
    mutedArgb: Int,
    positiveArgb: Int,
    warningArgb: Int,
    comparisonLabel: String,
) {
    canvas.drawRoundRect(
        rect,
        32f,
        32f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(188, 255, 255, 255) },
    )
    canvas.drawRoundRect(
        rect,
        32f,
        32f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(26, 126, 142, 158)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        },
    )
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 34f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    canvas.drawText(AppText.t("stats_weekly_screen_rhythm"), rect.left + 34f, rect.top + 60f, titlePaint)
    canvas.drawText(
        comparisonLabel,
        rect.right - 34f,
        rect.top + 60f,
        Paint(titlePaint).apply {
            color = if (comparisonLabel.contains("+") || comparisonLabel.contains(AppText.t("stats_up"))) warningArgb else positiveArgb
            textSize = 26f
            textAlign = Paint.Align.RIGHT
        },
    )
    val trend = values.takeLast(7).let { if (it.size < 7) List(7 - it.size) { 0L } + it else it }
    val maxValue = trend.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val chartLeft = rect.left + 34f
    val chartRight = rect.right - 34f
    val chartTop = rect.top + 102f
    val chartBottom = rect.bottom - 62f
    val points =
        trend.mapIndexed { index, value ->
            val x = chartLeft + (chartRight - chartLeft) * index / 6f
            val y = chartBottom - (chartBottom - chartTop) * (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
            x to y
        }
    val linePath = android.graphics.Path()
    points.forEachIndexed { index, point ->
        if (index == 0) {
            linePath.moveTo(point.first, point.second)
        } else {
            val previous = points[index - 1]
            val midX = (previous.first + point.first) / 2f
            linePath.cubicTo(midX, previous.second, midX, point.second, point.first, point.second)
        }
    }
    val fillPath = android.graphics.Path(linePath).apply {
        lineTo(chartRight, chartBottom)
        lineTo(chartLeft, chartBottom)
        close()
    }
    canvas.drawPath(
        fillPath,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader =
                android.graphics.LinearGradient(
                    0f,
                    chartTop,
                    0f,
                    chartBottom,
                    intArrayOf(android.graphics.Color.argb(58, 34, 174, 118), android.graphics.Color.argb(0, 34, 174, 118)),
                    null,
                    android.graphics.Shader.TileMode.CLAMP,
                )
        },
    )
    canvas.drawPath(
        linePath,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = positiveArgb
            strokeWidth = 7f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        },
    )
    points.drop(1).forEachIndexed { index, point ->
        if (index % 2 == 0 || index == points.size - 2) {
            canvas.drawCircle(point.first, point.second, 13f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE })
            canvas.drawCircle(point.first, point.second, 8f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = positiveArgb })
        }
    }
    val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 25f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    listOf(AppText.t("stats_mon"), AppText.t("stats_tue"), AppText.t("stats_wed"), AppText.t("stats_thu"), AppText.t("stats_fri"), AppText.t("stats_sat"), AppText.t("stats_sun")).forEachIndexed { index, label ->
        val x = chartLeft + (chartRight - chartLeft) * index / 6f
        canvas.drawText(label, x, rect.bottom - 28f, dayPaint)
    }
}

private fun renderShareReportBitmap(
    context: Context,
    data: ShareReportData,
    primary: Color,
    surface: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    palette: List<Color>,
): Bitmap {
    val width = 1080
    val height = 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val primaryArgb = primary.toArgb()
    val textArgb = onSurface.toArgb()
    val mutedArgb = onSurfaceVariant.toArgb()
    val displayTypeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    val titleTypeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    val bodyTypeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader =
            android.graphics.LinearGradient(
                0f,
                0f,
                0f,
                height.toFloat(),
                intArrayOf(
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.24f),
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.10f),
                    blendPosterColor(primary.toArgb(), android.graphics.Color.WHITE, 0.20f),
                ),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    drawSharePosterBackground(canvas, width, height, primary)

    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(238, 255, 255, 255) }
    val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(190, 255, 255, 255) }
    val softPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.11f).toArgb() }
    val warningArgb = palette.getOrElse(1) { primary }.toArgb()
    val positiveArgb = palette.getOrElse(2) { primary }.toArgb()
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 52f
        typeface = titleTypeface
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 29f
        typeface = bodyTypeface
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 27f
        typeface = bodyTypeface
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryArgb
        textSize = 118f
        typeface = displayTypeface
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 32f
        typeface = bodyTypeface
    }

    canvas.drawRoundRect(RectF(50f, 58f, width - 50f, height - 58f), 58f, 58f, cardPaint)

    drawSharePosterIcon(
        context = context,
        canvas = canvas,
        packageName = context.packageName,
        label = "T",
        left = 100f,
        top = 112f,
        size = 70f,
        fallbackColor = primaryArgb,
    )
    canvas.drawText(data.title, 192f, 145f, titlePaint)
    canvas.drawText(data.subtitle, 192f, 188f, subtitlePaint)

    val heroTop = 242f
    canvas.drawRoundRect(RectF(86f, heroTop, width - 86f, 598f), 46f, 46f, softPaint)
    canvas.drawText(data.statusTitle, 126f, heroTop + 70f, Paint(titlePaint).apply { textSize = 46f })
    canvas.drawText(data.primaryLabel, 126f, heroTop + 130f, labelPaint)
    canvas.drawText(data.primaryValue, 126f, heroTop + 250f, valuePaint)
    val goalText =
        if (data.goalMillis > 0L) {
            val delta = data.goalMillis - data.totalUsageMillis
            if (delta >= 0L) AppText.t("stats_within_target_value_left", formatDuration(delta)) else AppText.t("stats_over_by_value_3", formatDuration(-delta))
        } else {
            data.comparisonLabel
        }
    canvas.drawText(goalText, 126f, heroTop + 310f, Paint(bodyPaint).apply {
        color = if (data.goalMillis > 0L && data.totalUsageMillis > data.goalMillis) warningArgb else positiveArgb
        typeface = titleTypeface
    })

    val progressLeft = 610f
    val progressTop = heroTop + 266f
    val progressWidth = 300f
    val progressHeight = 28f
    val goalBase = data.goalMillis.takeIf { it > 0L } ?: max(data.totalUsageMillis, 1L)
    val progress = (data.totalUsageMillis.toFloat() / goalBase.toFloat()).coerceIn(0f, 1.15f)
    val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.15f).toArgb() }
    canvas.drawRoundRect(
        RectF(progressLeft, progressTop, progressLeft + progressWidth, progressTop + progressHeight),
        18f,
        18f,
        trackPaint,
    )
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader =
            android.graphics.LinearGradient(
                progressLeft,
                progressTop,
                progressLeft + progressWidth,
                progressTop,
                intArrayOf(primary.copy(alpha = 0.78f).toArgb(), if (progress > 1f) warningArgb else primaryArgb),
                null,
                android.graphics.Shader.TileMode.CLAMP,
            )
    }
    canvas.drawRoundRect(
        RectF(progressLeft, progressTop, progressLeft + progressWidth * progress.coerceIn(0f, 1f), progressTop + progressHeight),
        18f,
        18f,
        fillPaint,
    )
    canvas.drawLine(
        progressLeft + progressWidth,
        progressTop - 9f,
        progressLeft + progressWidth,
        progressTop + progressHeight + 9f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (progress > 1f) warningArgb else primaryArgb
            strokeWidth = 4f
        },
    )
    canvas.drawText(
        if (data.goalMillis > 0L) AppText.t("stats_target_value", formatDuration(data.goalMillis)) else AppText.t("stats_usage_progress"),
        progressLeft,
        progressTop + 68f,
        labelPaint,
    )
    canvas.drawText(
        "${(progress * 100f).roundToInt()}%",
        progressLeft + 250f,
        progressTop + 68f,
        Paint(bodyPaint).apply {
            color = if (progress > 1f) warningArgb else primaryArgb
            typeface = displayTypeface
        },
    )

    drawShareFocusCards(
        canvas = canvas,
        data = data,
        left = 86f,
        top = 638f,
        width = width - 172f,
        primaryArgb = primaryArgb,
        positiveArgb = positiveArgb,
        warningArgb = warningArgb,
        textArgb = textArgb,
        mutedArgb = mutedArgb,
        surfaceArgb = android.graphics.Color.argb(226, 255, 255, 255),
    )

    val sectionTitlePaint = Paint(titlePaint).apply { textSize = 40f }
    val distributionTop = 930f
    canvas.drawRoundRect(RectF(86f, distributionTop - 28f, width - 86f, distributionTop + 406f), 42f, 42f, glassPaint)
    drawShareAppDistribution(
        context = context,
        canvas = canvas,
        apps = data.topApps,
        centerX = 344f,
        centerY = distributionTop + 200f,
        radius = 188f,
        palette = palette,
        textColor = textArgb,
        mutedColor = mutedArgb,
        primaryColor = primary,
    )

    val insightTop = 1510f
    canvas.drawRoundRect(RectF(86f, insightTop - 54f, width - 86f, insightTop + 134f), 34f, 34f, softPaint)
    canvas.drawText(AppText.t("stats_review_sentence"), 126f, insightTop, Paint(sectionTitlePaint).apply { textSize = 34f })
    val posterInsight = buildSharePosterInsight(data)
    drawMultilineText(canvas, posterInsight, 126f, insightTop + 50f, width - 252f, Paint(bodyPaint).apply { textSize = 29f }, 40f, 2)

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 28f
        typeface = bodyTypeface
    }
    canvas.drawText(AppText.t("stats_share_footer"), 104f, height - 88f, footerPaint)
    return bitmap
}

private fun drawSharePosterBackground(
    canvas: android.graphics.Canvas,
    width: Int,
    height: Int,
    primary: Color,
) {
    val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(118, 255, 255, 255) }
    listOf(
        RectF(-80f, 250f, 360f, 430f),
        RectF(720f, 300f, 1160f, 470f),
        RectF(40f, 1500f, 520f, 1700f),
    ).forEach { rect ->
        canvas.drawOval(rect, cloudPaint)
    }
    val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primary.copy(alpha = 0.12f).toArgb()
        style = Paint.Style.FILL
    }
    val path = android.graphics.Path().apply {
        moveTo(0f, height * 0.72f)
        lineTo(width * 0.24f, height * 0.62f)
        lineTo(width * 0.48f, height * 0.70f)
        lineTo(width * 0.72f, height * 0.60f)
        lineTo(width.toFloat(), height * 0.69f)
        lineTo(width.toFloat(), height.toFloat())
        lineTo(0f, height.toFloat())
        close()
    }
    canvas.drawPath(path, mountainPaint)
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primary.copy(alpha = 0.11f).toArgb() }
    for (row in 0 until 8) {
        for (col in 0 until 6) {
            canvas.drawCircle(720f + col * 48f, 112f + row * 38f, 3.2f, dotPaint)
        }
    }
}

private fun blendPosterColor(foreground: Int, background: Int, ratio: Float): Int {
    val clamped = ratio.coerceIn(0f, 1f)
    val inverse = 1f - clamped
    return android.graphics.Color.rgb(
        (android.graphics.Color.red(foreground) * clamped + android.graphics.Color.red(background) * inverse).toInt(),
        (android.graphics.Color.green(foreground) * clamped + android.graphics.Color.green(background) * inverse).toInt(),
        (android.graphics.Color.blue(foreground) * clamped + android.graphics.Color.blue(background) * inverse).toInt(),
    )
}

private fun drawShareAppDistribution(
    context: Context,
    canvas: android.graphics.Canvas,
    apps: List<AppDisplayItem>,
    centerX: Float,
    centerY: Float,
    radius: Float,
    palette: List<Color>,
    textColor: Int,
    mutedColor: Int,
    primaryColor: Color,
) {
    val total = apps.sumOf { it.value }.coerceAtLeast(1L)
    val displayApps = apps.take(5)
    val values =
        if (displayApps.isEmpty()) {
            listOf(1L)
        } else {
            displayApps.map { it.value.coerceAtLeast(1L) }
        }
    val colors =
        values.mapIndexed { index, _ ->
            displayApps.getOrNull(index)?.let { app ->
                extractAppChartColor(
                    context = context,
                    packageName = app.packageName,
                    fallback = palette.getOrNull(index) ?: primaryColor,
                )
            } ?: palette.getOrNull(index) ?: primaryColor
        }
    val stroke = 42f
    val chartRect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor.copy(alpha = 0.08f).toArgb()
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawArc(chartRect, 138f, 264f, false, basePaint)
    var startAngle = 138f
    values.forEachIndexed { index, value ->
        val sweep = (value.toFloat() / values.sum().toFloat()) * 264f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors[index].toArgb()
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.BUTT
        }
        canvas.drawArc(chartRect, startAngle, (sweep - 4f).coerceAtLeast(3f), false, paint)
        startAngle += sweep
    }
    val centerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 32f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val centerLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedColor
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    val topUsage = displayApps.sumOf { it.value }
    canvas.drawText(if (topUsage > 0L) formatDuration(topUsage) else "--", centerX, centerY - 8f, centerTitlePaint)
    canvas.drawText("Top ${displayApps.size.coerceAtLeast(1)}", centerX, centerY + 34f, centerLabelPaint)

    val rowLeft = centerX + radius + 92f
    val rowTop = centerY - 166f
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 27f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }
    val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedColor
        textSize = 23f
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedColor
        textSize = 23f
        textAlign = Paint.Align.RIGHT
    }
    if (displayApps.isEmpty()) {
        canvas.drawText(AppText.t("stats_no_app_details_yet"), rowLeft, centerY, namePaint)
        return
    }
    displayApps.forEachIndexed { index, app ->
        val y = rowTop + index * 72f
        val color = colors[index]
        val barLeft = rowLeft + 58f
        val barRight = rowLeft + 310f
        val barTop = y + 38f
        drawSharePosterIcon(
            context = context,
            canvas = canvas,
            packageName = app.packageName,
            label = app.label.take(1),
            left = rowLeft,
            top = y,
            size = 44f,
            fallbackColor = color.toArgb(),
        )
        drawEllipsizedText(canvas, app.label, rowLeft + 58f, y + 25f, 150f, namePaint)
        val percent = ((app.value.toFloat() / total.toFloat()) * 100f).roundToInt()
        canvas.drawText("${formatDuration(app.value)} · $percent%", rowLeft + 322f, y + 25f, valuePaint)
        canvas.drawRoundRect(
            RectF(barLeft, barTop, barRight, barTop + 11f),
            8f,
            8f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.copy(alpha = 0.13f).toArgb() },
        )
        canvas.drawRoundRect(
            RectF(
                barLeft,
                barTop,
                barLeft + (barRight - barLeft) * (app.value.toFloat() / displayApps.maxOf { it.value }.coerceAtLeast(1L).toFloat()).coerceIn(0.04f, 1f),
                barTop + 11f,
            ),
            8f,
            8f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() },
        )
        canvas.drawCircle(rowLeft - 24f, y + 23f, 8f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() })
    }
}

private fun drawShareFocusCards(
    canvas: android.graphics.Canvas,
    data: ShareReportData,
    left: Float,
    top: Float,
    width: Float,
    primaryArgb: Int,
    positiveArgb: Int,
    warningArgb: Int,
    textArgb: Int,
    mutedArgb: Int,
    surfaceArgb: Int,
) {
    val gap = 28f
    val cardWidth = (width - gap) / 2f
    val cardHeight = 236f
    drawShareFocusCard(
        canvas = canvas,
        rect = RectF(left, top, left + cardWidth, top + cardHeight),
        title = AppText.t("stats_control_results"),
        primaryLabel = AppText.t("stats_time_saved"),
        primaryValue = formatDuration(data.savedMillis),
        accent = if (data.blockEventCount > 0) warningArgb else positiveArgb,
        progress = data.goalProgress ?: if (data.controlExceededGroupCount == 0) 1f else 0f,
        metrics =
            listOf(
                DailyFocusMetric(AppText.t("stats_met"), AppText.t("stats_value_groups_3", data.controlCompletedGroupCount)),
                DailyFocusMetric(AppText.t("stats_over_limit"), AppText.t("stats_value_groups_6", data.controlExceededGroupCount)),
                DailyFocusMetric(AppText.t("group_blocks"), AppText.t("stats_value_times_13", data.blockEventCount)),
            ),
        textArgb = textArgb,
        mutedArgb = mutedArgb,
        surfaceArgb = surfaceArgb,
    )
    drawShareFocusCard(
        canvas = canvas,
        rect = RectF(left + cardWidth + gap, top, left + width, top + cardHeight),
        title = AppText.t("stats_encourage_progress"),
        primaryLabel = AppText.t("stats_net_points"),
        primaryValue = formatSignedPointsLocal(data.pointsNet),
        accent = if (data.pointsNet >= 0.0) positiveArgb else warningArgb,
        progress = if (data.encourageCompletedGroupCount > 0 || data.redemptionCount > 0) {
            data.encourageCompletedGroupCount.toFloat() /
                (data.encourageCompletedGroupCount + data.redemptionCount).coerceAtLeast(1).toFloat()
        } else {
            0f
        },
        metrics =
            listOf(
                DailyFocusMetric(AppText.t("stats_duration"), formatDuration(data.encourageUsageMillis)),
                DailyFocusMetric(AppText.t("stats_met"), AppText.t("stats_value_groups", data.encourageCompletedGroupCount)),
                DailyFocusMetric(AppText.t("stats_redemption"), AppText.t("stats_value_times_10", data.redemptionCount)),
            ),
        textArgb = textArgb,
        mutedArgb = mutedArgb,
        surfaceArgb = surfaceArgb,
    )
}

private fun drawShareFocusCard(
    canvas: android.graphics.Canvas,
    rect: RectF,
    title: String,
    primaryLabel: String,
    primaryValue: String,
    accent: Int,
    progress: Float,
    metrics: List<DailyFocusMetric>,
    textArgb: Int,
    mutedArgb: Int,
    surfaceArgb: Int,
) {
    canvas.drawRoundRect(
        rect,
        34f,
        34f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surfaceArgb },
    )
    canvas.drawRoundRect(
        rect,
        34f,
        34f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(46, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
            style = Paint.Style.STROKE
            strokeWidth = 2f
        },
    )
    canvas.drawRoundRect(
        RectF(rect.left + 24f, rect.top + 24f, rect.left + 66f, rect.top + 66f),
        14f,
        14f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(40, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
        },
    )
    val titleTypeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textArgb
        textSize = 32f
        typeface = titleTypeface
    }
    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 24f
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        textSize = 48f
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mutedArgb
        textSize = 22f
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    canvas.drawLine(rect.left + 38f, rect.top + 45f, rect.left + 52f, rect.top + 45f, iconPaint)
    canvas.drawLine(rect.left + 45f, rect.top + 38f, rect.left + 45f, rect.top + 52f, iconPaint)
    canvas.drawText(title, rect.left + 78f, rect.top + 54f, titlePaint)
    canvas.drawText(primaryLabel, rect.left + 28f, rect.top + 104f, labelPaint)
    drawEllipsizedText(canvas, primaryValue, rect.left + 28f, rect.top + 154f, rect.width() - 154f, valuePaint)

    val ringCenterX = rect.right - 72f
    val ringCenterY = rect.top + 116f
    val ringRadius = 43f
    val ringRect = RectF(ringCenterX - ringRadius, ringCenterY - ringRadius, ringCenterX + ringRadius, ringCenterY + ringRadius)
    val ringStroke = 11f
    val ringBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(34, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
        style = Paint.Style.STROKE
        strokeWidth = ringStroke
        strokeCap = Paint.Cap.ROUND
    }
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        style = Paint.Style.STROKE
        strokeWidth = ringStroke
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawArc(ringRect, -90f, 360f, false, ringBasePaint)
    canvas.drawArc(ringRect, -90f, 360f * progress.coerceIn(0f, 1f), false, ringPaint)
    val ringTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        textSize = 22f
        textAlign = Paint.Align.CENTER
        typeface = titleTypeface
    }
    canvas.drawText("${(progress.coerceIn(0f, 1f) * 100f).roundToInt()}%", ringCenterX, ringCenterY + 8f, ringTextPaint)

    val pillTop = rect.top + 174f
    val pillGap = 8f
    val pillWidth = (rect.width() - 56f - pillGap * 2f) / 3f
    metrics.take(3).forEachIndexed { index, metric ->
        val pillLeft = rect.left + 28f + index * (pillWidth + pillGap)
        val pillRect = RectF(pillLeft, pillTop, pillLeft + pillWidth, pillTop + 42f)
        canvas.drawRoundRect(
            pillRect,
            18f,
            18f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(28, android.graphics.Color.red(accent), android.graphics.Color.green(accent), android.graphics.Color.blue(accent))
            },
        )
        drawEllipsizedText(canvas, metric.label, pillLeft + 12f, pillTop + 17f, pillWidth - 24f, bodyPaint)
        drawEllipsizedText(canvas, metric.value, pillLeft + 12f, pillTop + 35f, pillWidth - 24f, Paint(bodyPaint).apply {
            color = textArgb
            typeface = titleTypeface
        })
    }
}

private fun drawSharePosterIcon(
    context: Context,
    canvas: android.graphics.Canvas,
    packageName: String,
    label: String,
    left: Float,
    top: Float,
    size: Float,
    fallbackColor: Int,
) {
    val rect = RectF(left, top, left + size, top + size)
    val iconBitmap =
        runCatching {
            context.packageManager.getApplicationIcon(packageName).toBitmap(
                width = size.roundToInt(),
                height = size.roundToInt(),
                config = Bitmap.Config.ARGB_8888,
            )
        }.getOrNull()
    canvas.drawRoundRect(
        rect,
        size * 0.28f,
        size * 0.28f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE },
    )
    if (iconBitmap != null) {
        val path = android.graphics.Path().apply {
            addRoundRect(rect, size * 0.28f, size * 0.28f, android.graphics.Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(iconBitmap, null, rect, Paint(Paint.ANTI_ALIAS_FLAG))
        canvas.restore()
    } else {
        canvas.drawRoundRect(
            rect,
            size * 0.28f,
            size * 0.28f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fallbackColor },
        )
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = size * 0.46f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
        }
        val baseline = top + size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(label.ifBlank { "T" }.take(1), left + size / 2f, baseline, textPaint)
    }
}

private fun buildSharePosterInsight(data: ShareReportData): String {
    val nightText = if (data.nightUsageMillis > 0L) AppText.t("stats_night_value", formatDuration(data.nightUsageMillis)) else AppText.t("stats_light_night_use")
    val appText = data.topApps.firstOrNull()?.let { AppText.t("stats_top_app_is_value", it.label) } ?: AppText.t("stats_app_usage_is_spread_out")
    return AppText.t("stats_value_mainly_concentrated_in_value_value_value", data.comparisonLabel, data.dominantPeriod, nightText, appText)
}

private fun drawEllipsizedText(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    maxWidth: Float,
    paint: Paint,
) {
    var output = text
    while (paint.measureText(output) > maxWidth && output.length > 2) {
        output = output.dropLast(2) + "…"
    }
    canvas.drawText(output, x, y, paint)
}

private fun drawMultilineText(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    maxWidth: Float,
    paint: Paint,
    lineHeight: Float,
    maxLines: Int,
) {
    val words =
        if (text.contains(" ")) {
            text.split(" ")
        } else {
            text.map { it.toString() }
        }
    val lines = mutableListOf<String>()
    var current = ""
    words.forEach { word ->
        val separator = if (text.contains(" ")) " " else ""
        val candidate = if (current.isEmpty()) word else "$current$separator$word"
        if (paint.measureText(candidate) <= maxWidth) {
            current = candidate
        } else {
            if (current.isNotEmpty()) lines += current
            current = word
        }
    }
    if (current.isNotEmpty()) lines += current
    lines.take(maxLines).forEachIndexed { index, rawLine ->
        val line =
            if (index == maxLines - 1 && lines.size > maxLines) {
                var output = "$rawLine…"
                while (paint.measureText(output) > maxWidth && output.length > 2) {
                    output = output.dropLast(2) + "…"
                }
                output
            } else {
                rawLine
            }
        canvas.drawText(line, x, y + index * lineHeight, paint)
    }
}




