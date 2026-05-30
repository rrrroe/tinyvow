package com.rrrrz.tinyvow.ui.home

import kotlin.math.roundToInt

internal data class BehaviorControlScoreInput(
    val usedMillis: Long,
    val effectiveLimitMillis: Long,
    val completed: Boolean,
)

internal data class BehaviorEncourageScoreInput(
    val usedMillis: Long,
    val targetMillis: Long,
    val completed: Boolean,
)

internal data class BehaviorPackageScoreInput(
    val packageName: String,
    val usageMillis: Long,
    val openCount: Int,
)

internal data class BehaviorScoreInputs(
    val controlGroups: List<BehaviorControlScoreInput>,
    val encourageGroups: List<BehaviorEncourageScoreInput>,
    val packageStats: List<BehaviorPackageScoreInput>,
    val controlPackageNames: Set<String>,
    val encouragePackageNames: Set<String>,
    val nightOutsideEncourageMillis: Long,
)

internal data class BehaviorScoreBreakdown(
    val guardScore: Int,
    val gainScore: Int,
    val focusScore: Int,
    val rhythmScore: Int,
    val restraintScore: Int,
)

internal data class BehaviorScoreAnalysis(
    val breakdown: BehaviorScoreBreakdown,
    val guard: BehaviorGuardScoreAnalysis,
    val gain: BehaviorGainScoreAnalysis,
    val focus: BehaviorRatioScoreAnalysis,
    val rhythm: BehaviorRhythmScoreAnalysis,
    val restraint: BehaviorRatioScoreAnalysis,
)

internal data class BehaviorGuardScoreAnalysis(
    val score: Int,
    val completedGroups: Int,
    val totalGroups: Int,
    val remainingMillis: Long,
    val totalLimitMillis: Long,
)

internal data class BehaviorGainScoreAnalysis(
    val score: Int,
    val completedGroups: Int,
    val totalGroups: Int,
    val completionScore: Float,
    val bonusScore: Float,
    val groups: List<BehaviorGainGroupScoreAnalysis>,
)

internal data class BehaviorGainGroupScoreAnalysis(
    val usedMillis: Long,
    val targetMillis: Long,
    val progress: Float,
    val bonusProgress: Float,
    val bonusScore: Float,
)

internal data class BehaviorRatioScoreAnalysis(
    val score: Int,
    val numerator: Long,
    val denominator: Long,
    val ratio: Float?,
)

internal data class BehaviorRhythmScoreAnalysis(
    val score: Int,
    val nightOutsideEncourageMillis: Long,
)

private const val FULL_SCORE = 100f
private const val GUARD_COMPLETION_SCORE = 60f
private const val GUARD_REMAINING_SCORE = 40f
private const val GAIN_FIRST_COMPLETION_SCORE = 60f
private const val GAIN_ALL_COMPLETION_SCORE = 80f
private const val GAIN_BONUS_POOL_SCORE = 20f
private const val NIGHT_ONE_HOUR_MILLIS = 60L * 60_000L
private const val NIGHT_THREE_HOUR_MILLIS = 3L * 60L * 60_000L

internal fun calculateBehaviorScores(inputs: BehaviorScoreInputs): BehaviorScoreBreakdown {
    return analyzeBehaviorScores(inputs).breakdown
}

internal fun analyzeBehaviorScores(inputs: BehaviorScoreInputs): BehaviorScoreAnalysis {
    val guard = analyzeGuardScore(inputs.controlGroups)
    val gain = analyzeGainScore(inputs.encourageGroups)
    val focus =
        analyzeRatioScore(
            numerator = sumUsageMillis(inputs.packageStats, inputs.encouragePackageNames),
            denominator = sumUsageMillis(inputs.packageStats, inputs.controlPackageNames),
        )
    val restraint =
        analyzeRestraintScore(
            numerator = sumOpenCount(inputs.packageStats, inputs.encouragePackageNames).toLong(),
            denominator = sumOpenCount(inputs.packageStats, inputs.controlPackageNames).toLong(),
        )
    val rhythm = analyzeRhythmScore(inputs.nightOutsideEncourageMillis)

    return BehaviorScoreAnalysis(
        breakdown =
            BehaviorScoreBreakdown(
                guardScore = guard.score,
                gainScore = gain.score,
                focusScore = focus.score,
                rhythmScore = rhythm.score,
                restraintScore = restraint.score,
            ),
        guard = guard,
        gain = gain,
        focus = focus,
        rhythm = rhythm,
        restraint = restraint,
    )
}

private fun analyzeGuardScore(groups: List<BehaviorControlScoreInput>): BehaviorGuardScoreAnalysis {
    if (groups.isEmpty()) {
        return BehaviorGuardScoreAnalysis(
            score = 60,
            completedGroups = 0,
            totalGroups = 0,
            remainingMillis = 0L,
            totalLimitMillis = 0L,
        )
    }

    val completedGroups = groups.count { it.completed }
    val completedScore = completedGroups.toFloat() / groups.size.toFloat() * GUARD_COMPLETION_SCORE
    val totalLimitMillis = groups.sumOf { it.effectiveLimitMillis.coerceAtLeast(0L) }
    val remainingMillis =
        groups.sumOf { group ->
            val limitMillis = group.effectiveLimitMillis.coerceAtLeast(0L)
            (limitMillis - group.usedMillis).coerceIn(0L, limitMillis)
        }
    val remainingRatio =
        if (totalLimitMillis > 0L) {
            remainingMillis.toFloat() / totalLimitMillis.toFloat()
        } else {
            1f
        }
    return BehaviorGuardScoreAnalysis(
        score = (completedScore + remainingRatio.coerceIn(0f, 1f) * GUARD_REMAINING_SCORE).roundToInt().coerceIn(0, 100),
        completedGroups = completedGroups,
        totalGroups = groups.size,
        remainingMillis = remainingMillis,
        totalLimitMillis = totalLimitMillis,
    )
}

private fun analyzeGainScore(groups: List<BehaviorEncourageScoreInput>): BehaviorGainScoreAnalysis {
    if (groups.isEmpty()) {
        return BehaviorGainScoreAnalysis(
            score = 60,
            completedGroups = 0,
            totalGroups = 0,
            completionScore = 60f,
            bonusScore = 0f,
            groups = emptyList(),
        )
    }

    val completedCount = groups.count { it.completed }
    val bestProgressToTarget =
        groups.maxOfOrNull { group ->
            val targetMillis = group.targetMillis.coerceAtLeast(1L)
            (group.usedMillis.toFloat() / targetMillis.toFloat()).coerceIn(0f, 1f)
        } ?: 0f
    val completionScore =
        when {
            completedCount <= 0 -> GAIN_FIRST_COMPLETION_SCORE * bestProgressToTarget
            groups.size == 1 -> GAIN_ALL_COMPLETION_SCORE
            else -> {
                val extraCompletionRatio = (completedCount - 1).toFloat() / (groups.size - 1).toFloat()
                GAIN_FIRST_COMPLETION_SCORE +
                    extraCompletionRatio.coerceIn(0f, 1f) * (GAIN_ALL_COMPLETION_SCORE - GAIN_FIRST_COMPLETION_SCORE)
            }
        }
    val perGroupBonusScore = GAIN_BONUS_POOL_SCORE / groups.size.toFloat()
    val groupAnalyses =
        groups.map { group ->
            val targetMillis = group.targetMillis.coerceAtLeast(1L)
            val progress = group.usedMillis.toFloat() / targetMillis.toFloat()
            val bonusProgress = (progress - 1f).coerceIn(0f, 1f)
            BehaviorGainGroupScoreAnalysis(
                usedMillis = group.usedMillis,
                targetMillis = targetMillis,
                progress = progress,
                bonusProgress = bonusProgress,
                bonusScore = perGroupBonusScore * bonusProgress,
            )
        }
    val bonusScore = groupAnalyses.fold(0f) { total, analysis -> total + analysis.bonusScore }

    return BehaviorGainScoreAnalysis(
        score = (completionScore + bonusScore).roundToInt().coerceIn(0, 100),
        completedGroups = completedCount,
        totalGroups = groups.size,
        completionScore = completionScore,
        bonusScore = bonusScore,
        groups = groupAnalyses,
    )
}

private fun analyzeRatioScore(
    numerator: Long,
    denominator: Long,
): BehaviorRatioScoreAnalysis {
    if (numerator <= 0L && denominator <= 0L) {
        return BehaviorRatioScoreAnalysis(
            score = 60,
            numerator = numerator,
            denominator = denominator,
            ratio = null,
        )
    }
    if (denominator <= 0L) {
        return BehaviorRatioScoreAnalysis(
            score = 100,
            numerator = numerator,
            denominator = denominator,
            ratio = null,
        )
    }

    val ratio = numerator.toFloat() / denominator.toFloat()
    val rawScore =
        if (ratio <= 1f) {
            60f * ratio.coerceAtLeast(0f)
        } else {
            60f + (ratio - 1f).coerceIn(0f, 1f) * 40f
        }
    return BehaviorRatioScoreAnalysis(
        score = rawScore.roundToInt().coerceIn(0, 100),
        numerator = numerator,
        denominator = denominator,
        ratio = ratio,
    )
}

private fun analyzeRestraintScore(
    numerator: Long,
    denominator: Long,
): BehaviorRatioScoreAnalysis = analyzeRatioScore(numerator, denominator)

private fun analyzeRhythmScore(nightOutsideEncourageMillis: Long): BehaviorRhythmScoreAnalysis {
    val clampedMillis = nightOutsideEncourageMillis.coerceAtLeast(0L)
    val rawScore =
        when {
            clampedMillis <= 0L -> FULL_SCORE
            clampedMillis < NIGHT_ONE_HOUR_MILLIS -> {
                val progress = clampedMillis.toFloat() / NIGHT_ONE_HOUR_MILLIS.toFloat()
                FULL_SCORE - progress * 40f
            }
            clampedMillis < NIGHT_THREE_HOUR_MILLIS -> {
                val progress =
                    (clampedMillis - NIGHT_ONE_HOUR_MILLIS).toFloat() /
                        (NIGHT_THREE_HOUR_MILLIS - NIGHT_ONE_HOUR_MILLIS).toFloat()
                60f - progress * 60f
            }
            else -> 0f
        }
    return BehaviorRhythmScoreAnalysis(
        score = rawScore.roundToInt().coerceIn(0, 100),
        nightOutsideEncourageMillis = clampedMillis,
    )
}

private fun sumUsageMillis(
    packageStats: List<BehaviorPackageScoreInput>,
    packageNames: Set<String>,
): Long = packageStats.filter { it.packageName in packageNames }.sumOf { it.usageMillis }

private fun sumOpenCount(
    packageStats: List<BehaviorPackageScoreInput>,
    packageNames: Set<String>,
): Int = packageStats.filter { it.packageName in packageNames }.sumOf { it.openCount }
