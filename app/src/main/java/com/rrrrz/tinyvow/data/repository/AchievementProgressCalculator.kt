package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.StreakShieldPendingEntity
import com.rrrrz.tinyvow.data.db.StreakShieldPendingStatus
import com.rrrrz.tinyvow.data.db.StreakShieldTarget
import java.time.LocalDate

private data class CompletionSignal(
    val completedDates: Set<LocalDate>,
    val shieldedDates: Set<LocalDate>,
)

internal fun calculateAchievementProgress(
    earnedPointsTotal: Double,
    redeemedPointsTotal: Double,
    archives: List<DailyArchiveEntity>,
    groupArchives: List<DailyGroupArchiveEntity> = emptyList(),
    shieldPendings: List<StreakShieldPendingEntity> = emptyList(),
): AchievementProgress {
    val sortedArchives = archives.sortedBy { it.archiveDate }
    if (groupArchives.isEmpty()) {
        return AchievementProgress(
            earnedPointsTotal = earnedPointsTotal,
            redeemedPointsTotal = redeemedPointsTotal,
            controlDaysTotal = sortedArchives.count { it.controlCompletedGroupCount > 0 },
            controlStreak = calculateLegacyCompletedArchiveStreak(sortedArchives) { it.controlCompletedGroupCount > 0 },
            encourageDaysTotal = sortedArchives.count { it.encourageCompletedGroupCount > 0 },
            encourageStreak = calculateLegacyCompletedArchiveStreak(sortedArchives) { it.encourageCompletedGroupCount > 0 },
        )
    }
    val groupedByDate = groupArchives.groupBy { it.archiveDate }
    val usedPendings = shieldPendings.filter { it.status == StreakShieldPendingStatus.USED }
    val controlCompletedDates =
        completionDatesForGroupType(
            groupedByDate = groupedByDate,
            groupType = GroupType.CONTROL,
        )
    val encourageCompletedDates =
        completionDatesForGroupType(
            groupedByDate = groupedByDate,
            groupType = GroupType.ENCOURAGE,
        )
    val controlSignal =
        buildCompletionSignal(
            groupedByDate = groupedByDate,
            groupType = GroupType.CONTROL,
            shieldTarget = StreakShieldTarget.CONTROL_STREAK,
            usedPendings = usedPendings,
        )
    val encourageSignal =
        buildCompletionSignal(
            groupedByDate = groupedByDate,
            groupType = GroupType.ENCOURAGE,
            shieldTarget = StreakShieldTarget.ENCOURAGE_STREAK,
            usedPendings = usedPendings,
        )
    return AchievementProgress(
        earnedPointsTotal = earnedPointsTotal,
        redeemedPointsTotal = redeemedPointsTotal,
        controlDaysTotal = controlCompletedDates.size,
        controlStreak = calculateCompletedArchiveStreak(sortedArchives, controlSignal),
        encourageDaysTotal = encourageCompletedDates.size,
        encourageStreak = calculateCompletedArchiveStreak(sortedArchives, encourageSignal),
    )
}

private fun calculateLegacyCompletedArchiveStreak(
    sortedArchives: List<DailyArchiveEntity>,
    isCompleted: (DailyArchiveEntity) -> Boolean,
): Int {
    var expectedDate: LocalDate? = null
    var streak = 0
    for (archive in sortedArchives.asReversed()) {
        val archiveDate = runCatching { LocalDate.parse(archive.archiveDate) }.getOrNull() ?: break
        val currentExpectedDate = expectedDate
        if (currentExpectedDate != null && archiveDate != currentExpectedDate) break
        if (!isCompleted(archive)) break
        streak += 1
        expectedDate = archiveDate.minusDays(1)
    }
    return streak
}

private fun buildCompletionSignal(
    groupedByDate: Map<String, List<DailyGroupArchiveEntity>>,
    groupType: GroupType,
    shieldTarget: StreakShieldTarget,
    usedPendings: List<StreakShieldPendingEntity>,
): CompletionSignal {
    val completedDates = completionDatesForGroupType(groupedByDate = groupedByDate, groupType = groupType)
    val shieldedDates =
        usedPendings
            .filter { it.shieldTarget == shieldTarget }
            .mapNotNull { runCatching { LocalDate.parse(it.archiveDate) }.getOrNull() }
            .toSet()
    return CompletionSignal(completedDates = completedDates, shieldedDates = shieldedDates)
}

internal fun completionDatesForGroupType(
    groupedByDate: Map<String, List<DailyGroupArchiveEntity>>,
    groupType: GroupType,
): Set<LocalDate> =
    groupedByDate
        .mapNotNull { (archiveDate, archives) ->
            val sameTypeArchives = archives.filter { it.groupType == groupType }
            if (sameTypeArchives.isEmpty()) {
                return@mapNotNull null
            }
            val isCompleted =
                when (groupType) {
                    GroupType.CONTROL -> sameTypeArchives.all { it.completed }
                    GroupType.ENCOURAGE -> sameTypeArchives.any { it.completed }
                }
            if (!isCompleted) {
                return@mapNotNull null
            }
            runCatching { LocalDate.parse(archiveDate) }.getOrNull()
        }
        .toSet()

private fun calculateCompletedArchiveStreak(
    sortedArchives: List<DailyArchiveEntity>,
    signal: CompletionSignal,
): Int {
    var expectedDate: LocalDate? = null
    var streak = 0
    for (archive in sortedArchives.asReversed()) {
        val archiveDate = runCatching { LocalDate.parse(archive.archiveDate) }.getOrNull() ?: break
        val currentExpectedDate = expectedDate
        if (currentExpectedDate != null && archiveDate != currentExpectedDate) break
        if (archiveDate !in signal.completedDates && archiveDate !in signal.shieldedDates) break
        streak += 1
        expectedDate = archiveDate.minusDays(1)
    }
    return streak
}
