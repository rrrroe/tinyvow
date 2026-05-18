package com.rrrrz.tinyvow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppRegular = FontWeight.Normal
private val AppMedium = FontWeight.Medium
private val AppSemiBold = FontWeight.SemiBold
private val AppBold = FontWeight.Bold

private fun appTextStyle(
    fontSize: Int,
    lineHeight: Int,
    fontWeight: FontWeight,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

val Typography =
    Typography(
        displayLarge = appTextStyle(fontSize = 56, lineHeight = 60, fontWeight = AppBold),
        displayMedium = appTextStyle(fontSize = 46, lineHeight = 52, fontWeight = AppSemiBold),
        displaySmall = appTextStyle(fontSize = 36, lineHeight = 42, fontWeight = AppSemiBold),
        headlineLarge = appTextStyle(fontSize = 30, lineHeight = 36, fontWeight = AppSemiBold),
        headlineMedium = appTextStyle(fontSize = 26, lineHeight = 32, fontWeight = AppSemiBold),
        headlineSmall = appTextStyle(fontSize = 22, lineHeight = 28, fontWeight = AppSemiBold),
        titleLarge = appTextStyle(fontSize = 20, lineHeight = 26, fontWeight = AppMedium),
        titleMedium = appTextStyle(fontSize = 18, lineHeight = 24, fontWeight = AppMedium),
        titleSmall = appTextStyle(fontSize = 16, lineHeight = 22, fontWeight = AppMedium),
        bodyLarge = appTextStyle(fontSize = 16, lineHeight = 24, fontWeight = AppRegular),
        bodyMedium = appTextStyle(fontSize = 14, lineHeight = 21, fontWeight = AppRegular),
        bodySmall = appTextStyle(fontSize = 13, lineHeight = 18, fontWeight = AppRegular),
        labelLarge = appTextStyle(fontSize = 14, lineHeight = 20, fontWeight = AppRegular, letterSpacing = 0.05f),
        labelMedium = appTextStyle(fontSize = 12, lineHeight = 16, fontWeight = AppRegular, letterSpacing = 0.05f),
        labelSmall = appTextStyle(fontSize = 11, lineHeight = 14, fontWeight = AppRegular, letterSpacing = 0.1f),
    )
