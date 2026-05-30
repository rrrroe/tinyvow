package com.rrrrz.tinyvow.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class BehaviorScoreCalculatorTest {
    @Test
    fun guardScoreUsesWeightedRemainingTime() {
        val scores =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups =
                        listOf(
                            BehaviorControlScoreInput(
                                usedMillis = 30L * 60_000L,
                                effectiveLimitMillis = 60L * 60_000L,
                                completed = true,
                            ),
                            BehaviorControlScoreInput(
                                usedMillis = 120L * 60_000L,
                                effectiveLimitMillis = 120L * 60_000L,
                                completed = true,
                            ),
                        ),
                    encourageGroups = emptyList(),
                    packageStats = emptyList(),
                    controlPackageNames = emptySet(),
                    encouragePackageNames = emptySet(),
                    nightOutsideEncourageMillis = 0L,
                ),
            )

        assertEquals(67, scores.guardScore)
    }

    @Test
    fun gainScoreTreatsSingleCompletedGroupAsEightyAndDoubleAsHundred() {
        val completedAtTarget =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups = emptyList(),
                    encourageGroups =
                        listOf(
                            BehaviorEncourageScoreInput(
                                usedMillis = 60L * 60_000L,
                                targetMillis = 60L * 60_000L,
                                completed = true,
                            ),
                        ),
                    packageStats = emptyList(),
                    controlPackageNames = emptySet(),
                    encouragePackageNames = emptySet(),
                    nightOutsideEncourageMillis = 0L,
                ),
            )
        val doubled =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups = emptyList(),
                    encourageGroups =
                        listOf(
                            BehaviorEncourageScoreInput(
                                usedMillis = 120L * 60_000L,
                                targetMillis = 60L * 60_000L,
                                completed = true,
                            ),
                        ),
                    packageStats = emptyList(),
                    controlPackageNames = emptySet(),
                    encouragePackageNames = emptySet(),
                    nightOutsideEncourageMillis = 0L,
                ),
            )

        assertEquals(80, completedAtTarget.gainScore)
        assertEquals(100, doubled.gainScore)
    }

    @Test
    fun focusAndRestraintHandleZeroDenominatorGracefully() {
        val scores =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups = emptyList(),
                    encourageGroups = emptyList(),
                    packageStats =
                        listOf(
                            BehaviorPackageScoreInput(
                                packageName = "encourage.app",
                                usageMillis = 90L * 60_000L,
                                openCount = 8,
                            ),
                        ),
                    controlPackageNames = setOf("control.app"),
                    encouragePackageNames = setOf("encourage.app"),
                    nightOutsideEncourageMillis = 0L,
                ),
            )

        assertEquals(100, scores.focusScore)
        assertEquals(100, scores.restraintScore)
    }

    @Test
    fun gainScoreUsesClosestGroupProgressBeforeSixty() {
        val scores =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups = emptyList(),
                    encourageGroups =
                        listOf(
                            BehaviorEncourageScoreInput(
                                usedMillis = 30L * 60_000L,
                                targetMillis = 60L * 60_000L,
                                completed = false,
                            ),
                            BehaviorEncourageScoreInput(
                                usedMillis = 20L * 60_000L,
                                targetMillis = 60L * 60_000L,
                                completed = false,
                            ),
                        ),
                    packageStats = emptyList(),
                    controlPackageNames = emptySet(),
                    encouragePackageNames = emptySet(),
                    nightOutsideEncourageMillis = 0L,
                ),
            )

        assertEquals(30, scores.gainScore)
    }

    @Test
    fun restraintScoreUsesFullRatioAsSixtyAndDoubleRatioAsHundred() {
        val fullRatio =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups = emptyList(),
                    encourageGroups = emptyList(),
                    packageStats =
                        listOf(
                            BehaviorPackageScoreInput("encourage.app", usageMillis = 0L, openCount = 10),
                            BehaviorPackageScoreInput("control.app", usageMillis = 0L, openCount = 10),
                        ),
                    controlPackageNames = setOf("control.app"),
                    encouragePackageNames = setOf("encourage.app"),
                    nightOutsideEncourageMillis = 0L,
                ),
            )
        val doubleRatio =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups = emptyList(),
                    encourageGroups = emptyList(),
                    packageStats =
                        listOf(
                            BehaviorPackageScoreInput("encourage.app", usageMillis = 0L, openCount = 20),
                            BehaviorPackageScoreInput("control.app", usageMillis = 0L, openCount = 10),
                        ),
                    controlPackageNames = setOf("control.app"),
                    encouragePackageNames = setOf("encourage.app"),
                    nightOutsideEncourageMillis = 0L,
                ),
            )

        assertEquals(60, fullRatio.restraintScore)
        assertEquals(100, doubleRatio.restraintScore)
    }

    @Test
    fun rhythmScoreUsesNightAnchors() {
        val zeroNight =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups = emptyList(),
                    encourageGroups = emptyList(),
                    packageStats = emptyList(),
                    controlPackageNames = emptySet(),
                    encouragePackageNames = emptySet(),
                    nightOutsideEncourageMillis = 0L,
                ),
            )
        val oneHourNight =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups = emptyList(),
                    encourageGroups = emptyList(),
                    packageStats = emptyList(),
                    controlPackageNames = emptySet(),
                    encouragePackageNames = emptySet(),
                    nightOutsideEncourageMillis = 60L * 60_000L,
                ),
            )
        val threeHoursNight =
            calculateBehaviorScores(
                BehaviorScoreInputs(
                    controlGroups = emptyList(),
                    encourageGroups = emptyList(),
                    packageStats = emptyList(),
                    controlPackageNames = emptySet(),
                    encouragePackageNames = emptySet(),
                    nightOutsideEncourageMillis = 3L * 60L * 60_000L,
                ),
            )

        assertEquals(100, zeroNight.rhythmScore)
        assertEquals(60, oneHourNight.rhythmScore)
        assertEquals(0, threeHoursNight.rhythmScore)
    }
}
