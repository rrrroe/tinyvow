package com.rrrrz.tinyvow.ui.home

internal data class BehaviorScoreMetricDetail(
    val title: String,
    val score: Int,
    val formulaLines: List<String>,
    val comparisonRows: List<BehaviorScoreMetricComparisonRow>,
)

internal data class BehaviorScoreMetricComparisonRow(
    val label: String,
    val todayValue: String,
    val yesterdayValue: String,
)
