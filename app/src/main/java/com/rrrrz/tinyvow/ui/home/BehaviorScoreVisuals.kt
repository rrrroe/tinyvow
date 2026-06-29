package com.rrrrz.tinyvow.ui.home

import androidx.compose.ui.graphics.Color

private val BehaviorScoreAccentColors =
    listOf(
        Color(0xFF7B68EE),
        Color(0xFF35BFA1),
        Color(0xFF5CA9F2),
        Color(0xFFF5A13A),
        Color(0xFFF36A9A),
    )

internal fun behaviorScoreAccentColor(index: Int): Color =
    BehaviorScoreAccentColors.getOrElse(index) { BehaviorScoreAccentColors.last() }
