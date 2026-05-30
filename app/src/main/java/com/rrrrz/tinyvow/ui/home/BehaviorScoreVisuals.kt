package com.rrrrz.tinyvow.ui.home

import androidx.compose.ui.graphics.Color

private val BehaviorScoreAccentColors =
    listOf(
        Color(0xFF7B6CE1),
        Color(0xFF34B6A4),
        Color(0xFF4B7FDE),
        Color(0xFFE0A13C),
        Color(0xFFD95F86),
    )

internal fun behaviorScoreAccentColor(index: Int): Color =
    BehaviorScoreAccentColors.getOrElse(index) { BehaviorScoreAccentColors.last() }
