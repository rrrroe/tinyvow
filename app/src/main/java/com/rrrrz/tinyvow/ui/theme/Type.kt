package com.rrrrz.tinyvow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppRegular = FontWeight.Normal
private val AppMedium = FontWeight.Medium
private val AppSemiBold = FontWeight.SemiBold
private val AppBold = FontWeight.Bold
private val AppExtraBold = FontWeight.ExtraBold

private fun appTextStyle(
    fontSize: Int,
    lineHeight: Int,
    fontWeight: FontWeight,
) = TextStyle(
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
)

val Typography =
    Typography(
        displayLarge = appTextStyle(fontSize = 58, lineHeight = 62, fontWeight = AppExtraBold),
        displayMedium = appTextStyle(fontSize = 48, lineHeight = 54, fontWeight = AppBold),
        displaySmall = appTextStyle(fontSize = 38, lineHeight = 44, fontWeight = AppBold),
        headlineLarge = appTextStyle(fontSize = 32, lineHeight = 38, fontWeight = AppBold),
        headlineMedium = appTextStyle(fontSize = 27, lineHeight = 33, fontWeight = AppBold),
        headlineSmall = appTextStyle(fontSize = 23, lineHeight = 29, fontWeight = AppSemiBold),
        titleLarge = appTextStyle(fontSize = 21, lineHeight = 28, fontWeight = AppSemiBold),
        titleMedium = appTextStyle(fontSize = 17, lineHeight = 24, fontWeight = AppSemiBold),
        titleSmall = appTextStyle(fontSize = 15, lineHeight = 21, fontWeight = AppMedium),
        bodyLarge = appTextStyle(fontSize = 16, lineHeight = 24, fontWeight = AppRegular),
        bodyMedium = appTextStyle(fontSize = 14, lineHeight = 22, fontWeight = AppRegular),
        bodySmall = appTextStyle(fontSize = 12, lineHeight = 18, fontWeight = AppRegular),
        labelLarge = appTextStyle(fontSize = 14, lineHeight = 20, fontWeight = AppSemiBold),
        labelMedium = appTextStyle(fontSize = 12, lineHeight = 17, fontWeight = AppMedium),
        labelSmall = appTextStyle(fontSize = 10, lineHeight = 14, fontWeight = AppMedium),
    )
