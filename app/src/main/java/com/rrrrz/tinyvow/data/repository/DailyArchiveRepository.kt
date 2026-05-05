package com.rrrrz.tinyvow.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.data.db.AppGroupEntity
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyArchiveStateEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupAppCrossRef
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.PointLedgerEntity
import com.rrrrz.tinyvow.data.db.PointLedgerEntryType
import com.rrrrz.tinyvow.data.db.TopAppArchiveSummary
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.UsageRepository
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.domain.limit.isControlTimeoutForStats
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.max

class DailyArchiveRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val usageRepository: UsageRepository = UsageStatsUsageRepository(context),
) {
    private companion object {
        private const val MIN_UNGROUPED_APP_ARCHIVE_USAGE_MILLIS = 60_000L
        private const val UNGROUPED_SCOPE_KEY = "__ungrouped__"
    }

    private val zoneId = ZoneId.systemDefault()
    private val packageManager = context.packageManager
    private val groupDao = database.appGroupDao()
    private val crossRefDao = database.crossRefDao()
    private val bonusTimeDao = database.bonusTimeDao()
    private val redemptionHistoryDao = database.redemptionHistoryDao()
    private val dailyArchiveDao = database.dailyArchiveDao()
    private val dailyGroupArchiveDao = database.dailyGroupArchiveDao()
    private val dailyAppArchiveDao = database.dailyAppArchiveDao()
    private val pointLedgerDao = database.pointLedgerDao()
    private val stateDao = database.dailyArchiveStateDao()
    private val blockEventDao = database.blockEventDao()
    private val preferences = ManagedAppPreferences(context)

    fun getRecentArchives(limit: Int = 90): Flow<List<DailyArchiveEntity>> = dailyArchiveDao.getRecent(limit)

    fun getArchiveByDate(date: String): Flow<DailyArchiveEntity?> = dailyArchiveDao.getByDate(date)

    fun getArchivesByRange(from: String, to: String): Flow<List<DailyArchiveEntity>> =
        dailyArchiveDao.getByDateRange(from, to)

    fun getGroupArchivesByDate(date: String): Flow<List<DailyGroupArchiveEntity>> = dailyGroupArchiveDao.getByDate(date)

    fun getGroupArchivesByRange(from: String, to: String): Flow<List<DailyGroupArchiveEntity>> =
        dailyGroupArchiveDao.getByDateRange(from, to)

    fun getAppArchivesByDate(date: String): Flow<List<DailyAppArchiveEntity>> = dailyAppArchiveDao.getByDate(date)

    fun getUngroupedAppArchivesByDate(date: String): Flow<List<DailyAppArchiveEntity>> =
        dailyAppArchiveDao.getUngroupedByDate(date)

    fun getAppArchivesByRange(
        from: String,
        to: String,
    ): Flow<List<DailyAppArchiveEntity>> = dailyAppArchiveDao.getByDateRange(from, to)

    fun getAppArchivesByGroupAndRange(
        groupId: String,
        from: String,
        to: String,
    ): Flow<List<DailyAppArchiveEntity>> = dailyAppArchiveDao.getByGroupAndRange(groupId, from, to)

    fun getAppArchivesByPackageAndRange(
        packageName: String,
        from: String,
        to: String,
    ): Flow<List<DailyAppArchiveEntity>> = dailyAppArchiveDao.getByPackageAndRange(packageName, from, to)

    fun getTopAppsByRange(
        from: String,
        to: String,
        groupId: String? = null,
        limit: Int = 10,
    ): Flow<List<TopAppArchiveSummary>> = dailyAppArchiveDao.getTopAppsByRange(groupId, from, to, limit)

    suspend fun refreshArchiveForDate(date: String) {
        withContext(Dispatchers.IO) {
            val targetDate = LocalDate.parse(date)
            val today = LocalDate.now(zoneId)
            require(targetDate.isBefore(today)) {
                "Only completed days can be refreshed."
            }
            archiveDate(targetDate)
            checkAchievementsAfterArchive()
        }
    }

    suspend fun ensureArchivesUpToYesterday() {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val today = LocalDate.now(zoneId)
            var state = stateDao.get()
            if (state == null) {
                val todayString = ArchiveDateUtils.formatDate(today)
                state = DailyArchiveStateEntity(
                    id = "main",
                    archiveStartDate = todayString,
                    createdAt = now,
                    updatedAt = now,
                )
                stateDao.upsert(state)
                repairArchivesMissingAppSnapshots()
                checkAchievementsAfterArchive()
                return@withContext
            }

            val archiveStartDate = LocalDate.parse(state.archiveStartDate)
            if (!today.isAfter(archiveStartDate)) {
                repairArchivesMissingAppSnapshots()
                checkAchievementsAfterArchive()
                return@withContext
            }

            val endDate = today.minusDays(1)
            val nextDate =
                state.lastArchivedDate?.let { LocalDate.parse(it).plusDays(1) } ?: archiveStartDate
            if (nextDate.isAfter(endDate)) {
                repairArchivesMissingAppSnapshots()
                checkAchievementsAfterArchive()
                return@withContext
            }

            var mutableState =
                state.copy(
                    lastAttemptedAt = now,
                    updatedAt = now,
                )
            stateDao.upsert(mutableState)

            try {
                var date = nextDate
                while (!date.isAfter(endDate)) {
                    archiveDate(date)
                    val succeededAt = System.currentTimeMillis()
                    mutableState =
                        mutableState.copy(
                            lastArchivedDate = ArchiveDateUtils.formatDate(date),
                            lastSucceededAt = succeededAt,
                            lastErrorMessage = "",
                            updatedAt = succeededAt,
                        )
                    stateDao.upsert(mutableState)
                    date = date.plusDays(1)
                }
                repairArchivesMissingAppSnapshots()
                checkAchievementsAfterArchive()
            } catch (error: Exception) {
                stateDao.upsert(
                    mutableState.copy(
                        lastErrorMessage = error.message ?: error.javaClass.simpleName,
                        updatedAt = System.currentTimeMillis(),
                    )
                )
                throw error
            }
        }
    }

    private suspend fun checkAchievementsAfterArchive() {
        AppLimitRepository(context, database).checkAchievements()
    }

    private suspend fun repairArchivesMissingAppSnapshots() {
        val groupedPackageNames =
            crossRefDao
                .getAllValidCrossRefsSync()
                .map { it.packageName }
                .toSet()
        dailyArchiveDao.getAllArchiveDatesAsc().forEach { dateString ->
            val date = LocalDate.parse(dateString)
            val dayStart = ArchiveDateUtils.startOfDayMillis(date, zoneId)
            val nextDayStart = ArchiveDateUtils.nextDayStartMillis(date, zoneId)
            val hasUngroupedCandidates =
                usageRepository
                    .getUsageStats(dayStart, nextDayStart)
                    .any { (packageName, usageMillis) ->
                        usageMillis >= MIN_UNGROUPED_APP_ARCHIVE_USAGE_MILLIS &&
                            packageName !in groupedPackageNames
                    }
            val hasAppSnapshots = dailyAppArchiveDao.countByDate(dateString) > 0
            val hasUngroupedSnapshots = dailyAppArchiveDao.countUngroupedByDate(dateString) > 0
            val hadGroupedApps = dailyGroupArchiveDao.countGroupsWithPackagesByDate(dateString) > 0
            if ((!hasAppSnapshots && (hadGroupedApps || hasUngroupedCandidates)) ||
                (hasUngroupedCandidates && !hasUngroupedSnapshots)
            ) {
                archiveDate(date)
            }
        }
    }

    private suspend fun archiveDate(date: LocalDate) {
        val archiveDate = ArchiveDateUtils.formatDate(date)
        val dayStart = ArchiveDateUtils.startOfDayMillis(date, zoneId)
        val dayEnd = ArchiveDateUtils.endOfDayMillis(date, zoneId)
        val nextDayStart = ArchiveDateUtils.nextDayStartMillis(date, zoneId)
        val groups = groupDao.getAllGroupsSync()
        val crossRefs = crossRefDao.getAllValidCrossRefsSync()
        val existingGroupSnapshots = dailyGroupArchiveDao.getByDateSync(archiveDate)
        val existingGroupedAppsByGroup =
            dailyAppArchiveDao
                .getGroupedByDateSync(archiveDate)
                .groupBy { it.groupId.orEmpty() }
        val archiveTime = System.currentTimeMillis()
        val dailyUsageByPackage = usageRepository.getUsageStats(dayStart, nextDayStart)
        val dailyOpenCountByPackage = usageRepository.getAppOpenCount(dayStart, nextDayStart)
        val groupedPackageNames =
            (crossRefs.asSequence().map { it.packageName } +
                existingGroupedAppsByGroup.values.asSequence().flatten().map { it.packageName })
                .distinct()
                .toSet()
        val activePackageNames =
            dailyUsageByPackage
                .filterValues { it >= MIN_UNGROUPED_APP_ARCHIVE_USAGE_MILLIS }
                .keys
        val packagesToArchive = (groupedPackageNames + activePackageNames).toSet()
        val sessionsByPackage =
            usageRepository
                .getUsageSessions(dayStart, nextDayStart)
                .groupBy { it.packageName }
        val packageLabels =
            packagesToArchive
                .asSequence()
                .associateWith(::resolveAppLabel)
        val groupConfigs =
            buildArchiveGroupConfigs(
                currentGroups = groups,
                currentCrossRefs = crossRefs,
                existingGroupSnapshots = existingGroupSnapshots,
                existingGroupedAppsByGroup = existingGroupedAppsByGroup,
                dayStart = dayStart,
                dayEnd = dayEnd,
            )
        val periodUsageByStart =
            groupConfigs
                .map { ArchiveDateUtils.periodStart(date, it.limitPeriod) }
                .distinct()
                .associateWith { periodStart ->
                    usageRepository.getUsageStats(
                        ArchiveDateUtils.startOfDayMillis(periodStart, zoneId),
                        nextDayStart,
                    )
                }
        val periodUsageBeforeDayByStart =
            groupConfigs
                .map { ArchiveDateUtils.periodStart(date, it.limitPeriod) }
                .distinct()
                .associateWith { periodStart ->
                    val periodStartMillis = ArchiveDateUtils.startOfDayMillis(periodStart, zoneId)
                    if (periodStartMillis >= dayStart) {
                        emptyMap()
                    } else {
                        usageRepository.getUsageStats(periodStartMillis, dayStart)
                    }
                }

        val groupBuildResults =
            groupConfigs.mapIndexed { index, group ->
                buildGroupArchive(
                    date = date,
                    archiveDate = archiveDate,
                    group = group,
                    dayEnd = dayEnd,
                    archiveTime = archiveTime,
                    sortOrder = index,
                    dailyUsageByPackage = dailyUsageByPackage,
                    periodUsageByPackage =
                        periodUsageByStart[
                            ArchiveDateUtils.periodStart(date, group.limitPeriod)
                        ].orEmpty(),
                    periodUsageBeforeDayByPackage =
                        periodUsageBeforeDayByStart[
                            ArchiveDateUtils.periodStart(date, group.limitPeriod)
                        ].orEmpty(),
                    blockEventCount = blockEventDao.countByDateAndGroup(archiveDate, group.id),
                )
            }
        val groupSnapshots = groupBuildResults.map { it.archive }
        val groupedAppArchives =
            groupBuildResults.flatMap { groupResult ->
                val allocatedEarnedPoints =
                    allocateGroupEarnedPoints(
                        totalPoints = groupResult.archive.earnedPoints,
                        packageNames = groupResult.packageNames,
                        usageByPackage = dailyUsageByPackage,
                    )
                groupResult.packageNames.map { packageName ->
                    val behaviorSummary =
                        summarizeAppBehavior(
                            sessions = sessionsByPackage[packageName].orEmpty(),
                            dayStart = dayStart,
                            nextDayStart = nextDayStart,
                        )
                    buildDailyAppArchive(
                        archiveDate = archiveDate,
                        packageName = packageName,
                        appLabel = packageLabels[packageName] ?: packageName,
                        groupArchive = groupResult.archive,
                        behaviorSummary = behaviorSummary,
                        dailyUsageMillis = dailyUsageByPackage[packageName] ?: 0L,
                        openCount = dailyOpenCountByPackage[packageName] ?: 0,
                        earnedPoints = allocatedEarnedPoints[packageName] ?: 0.0,
                        archiveTime = archiveTime,
                    )
                }
            }
        val ungroupedAppArchives =
            (activePackageNames - groupedPackageNames).map { packageName ->
                val behaviorSummary =
                    summarizeAppBehavior(
                        sessions = sessionsByPackage[packageName].orEmpty(),
                        dayStart = dayStart,
                        nextDayStart = nextDayStart,
                    )
                buildDailyAppArchive(
                    archiveDate = archiveDate,
                    packageName = packageName,
                    appLabel = packageLabels[packageName] ?: packageName,
                    groupArchive = null,
                    behaviorSummary = behaviorSummary,
                    dailyUsageMillis = dailyUsageByPackage[packageName] ?: 0L,
                    openCount = dailyOpenCountByPackage[packageName] ?: 0,
                    earnedPoints = 0.0,
                    archiveTime = archiveTime,
                )
            }
        val appArchives = groupedAppArchives + ungroupedAppArchives
        val archiveEarnEntries =
            groupSnapshots
                .filter { it.groupType == GroupType.ENCOURAGE && it.earnedPoints > 0.0 }
                .mapNotNull { snapshot ->
                    val alreadyEarned = pointLedgerDao.sumEarnedByDateAndGroup(archiveDate, snapshot.groupId)
                    val missingPoints = snapshot.earnedPoints - alreadyEarned
                    if (missingPoints > 0.0001) {
                        PointLedgerEntity(
                            id = UUID.randomUUID().toString(),
                            occurredAt = dayEnd,
                            ledgerDate = archiveDate,
                            entryType = PointLedgerEntryType.USAGE_EARN,
                            deltaPoints = missingPoints,
                            groupId = snapshot.groupId,
                            groupNameSnapshot = snapshot.groupName,
                            sourceRefId = "archive-earn:$archiveDate:${snapshot.groupId}:${UUID.randomUUID()}",
                            note = "Daily archive earned points reconciliation",
                            createdAt = archiveTime,
                        )
                    } else {
                        null
                    }
                }

        val pointsEarned =
            groupSnapshots.sumOf { it.earnedPoints } +
                pointLedgerDao.sumUngroupedEarnedByDate(archiveDate)
        val pointsSpent = pointLedgerDao.sumSpentByDate(archiveDate)
        val controlPackageNames =
            groupConfigs
                .filter { it.type == GroupType.CONTROL }
                .flatMap { it.packageNames }
                .distinct()
        val encouragePackageNames =
            groupConfigs
                .filter { it.type == GroupType.ENCOURAGE }
                .flatMap { it.packageNames }
                .distinct()
        val dailyArchive =
            DailyArchiveEntity(
                id = UUID.randomUUID().toString(),
                archiveDate = archiveDate,
                dayStartAt = dayStart,
                dayEndAt = dayEnd,
                controlUsageMillis = controlPackageNames.sumOf { packageName -> dailyUsageByPackage[packageName] ?: 0L },
                encourageUsageMillis = encouragePackageNames.sumOf { packageName -> dailyUsageByPackage[packageName] ?: 0L },
                totalUsageMillis = packagesToArchive.sumOf { packageName -> dailyUsageByPackage[packageName] ?: 0L },
                savedMillis =
                    groupSnapshots
                        .filter { it.groupType == GroupType.CONTROL && it.limitPeriod == LimitPeriod.DAILY }
                        .sumOf { it.remainingMillisAtClose },
                controlExceededGroupCount =
                    groupSnapshots.count {
                        it.groupType == GroupType.CONTROL &&
                            isControlTimeoutForStats(it.exceededMillisAtClose)
                    },
                controlBlockEventCount = blockEventDao.countByDate(archiveDate),
                controlCompletedGroupCount =
                    groupSnapshots.count {
                        it.groupType == GroupType.CONTROL &&
                            it.completed
                    },
                encourageCompletedGroupCount =
                    groupSnapshots.count {
                        it.groupType == GroupType.ENCOURAGE && it.completed
                    },
                pointsEarned = pointsEarned,
                pointsSpent = pointsSpent,
                pointsNet = pointsEarned - pointsSpent,
                redemptionCount = redemptionHistoryDao.countInRange(dayStart, nextDayStart),
                createdAt = archiveTime,
                updatedAt = archiveTime,
            )

        var appliedArchiveEarnPoints = 0.0
        database.withTransaction {
            archiveEarnEntries.forEach { entry ->
                if (pointLedgerDao.insertIgnore(entry) > 0) {
                    appliedArchiveEarnPoints += entry.deltaPoints
                }
            }
            dailyAppArchiveDao.replaceForDate(archiveDate, appArchives)
            dailyGroupArchiveDao.deleteByDate(archiveDate)
            dailyGroupArchiveDao.insertAll(groupSnapshots)
            dailyArchiveDao.upsert(dailyArchive)
        }
        if (appliedArchiveEarnPoints > 0.0) {
            preferences.addUserPoints(appliedArchiveEarnPoints)
        }
    }

    private suspend fun buildArchiveGroupConfigs(
        currentGroups: List<AppGroupEntity>,
        currentCrossRefs: List<GroupAppCrossRef>,
        existingGroupSnapshots: List<DailyGroupArchiveEntity>,
        existingGroupedAppsByGroup: Map<String, List<DailyAppArchiveEntity>>,
        dayStart: Long,
        dayEnd: Long,
    ): List<ArchiveGroupConfig> {
        val currentGroupsById = currentGroups.associateBy { it.id }
        val existingSnapshotsById = existingGroupSnapshots.associateBy { it.groupId }
        val groupIds = (currentGroups.map { it.id } + existingGroupSnapshots.map { it.groupId }).distinct()

        return groupIds.mapNotNull { groupId ->
            val current = currentGroupsById[groupId]
            val snapshot = existingSnapshotsById[groupId]
            if (current == null && snapshot == null) return@mapNotNull null

            val existingPackageNames =
                existingGroupedAppsByGroup[groupId]
                    ?.map { it.packageName }
                    ?.distinct()
                    .orEmpty()
            val currentPackageNames =
                currentCrossRefs
                    .asSequence()
                    .filter { it.groupId == groupId }
                    .map { it.packageName }
                    .distinct()
                    .toList()
            val packageNames = existingPackageNames.ifEmpty { currentPackageNames }
            val bonusMinutes =
                snapshot?.bonusMinutes
                    ?: bonusTimeDao.sumBonusMinutesAffectingDay(groupId, dayStart, dayEnd)

            ArchiveGroupConfig(
                id = groupId,
                name = snapshot?.groupName ?: current!!.name,
                type = snapshot?.groupType ?: current!!.type,
                limitPeriod = snapshot?.limitPeriod ?: current!!.limitPeriod,
                limitMinutes = snapshot?.limitMinutes ?: current!!.limitMinutes,
                bonusMinutes = bonusMinutes,
                pointsPerMinute = snapshot?.pointsPerMinute ?: current!!.pointsPerMinute,
                packageNames = packageNames,
                sortOrder = snapshot?.sortOrder ?: current?.sortOrder ?: 0,
            )
        }.sortedWith(
            compareBy<ArchiveGroupConfig> { it.type }
                .thenBy { it.sortOrder }
                .thenBy { it.name },
        )
    }

    private suspend fun buildGroupArchive(
        date: LocalDate,
        archiveDate: String,
        group: ArchiveGroupConfig,
        dayEnd: Long,
        archiveTime: Long,
        sortOrder: Int,
        dailyUsageByPackage: Map<String, Long>,
        periodUsageByPackage: Map<String, Long>,
        periodUsageBeforeDayByPackage: Map<String, Long>,
        blockEventCount: Int,
    ): GroupArchiveBuildResult {
        val dayStart = ArchiveDateUtils.startOfDayMillis(date, zoneId)
        val packageNames = group.packageNames
        val dailyUsageMillis = packageNames.sumOf { packageName -> dailyUsageByPackage[packageName] ?: 0L }
        val periodUsageMillisAtClose = packageNames.sumOf { packageName -> periodUsageByPackage[packageName] ?: 0L }
        val periodUsageMillisBeforeDay = packageNames.sumOf { packageName -> periodUsageBeforeDayByPackage[packageName] ?: 0L }
        val bonusMinutes = group.bonusMinutes
        val effectiveLimitMillisAtClose = (group.limitMinutes + bonusMinutes) * 60_000L
        val remainingMillisAtClose = max(effectiveLimitMillisAtClose - periodUsageMillisAtClose, 0L)
        val exceededMillisAtClose = max(periodUsageMillisAtClose - effectiveLimitMillisAtClose, 0L)
        val completed =
            when (group.type) {
                GroupType.CONTROL -> !isControlTimeoutForStats(exceededMillisAtClose)
                GroupType.ENCOURAGE -> periodUsageMillisAtClose >= effectiveLimitMillisAtClose
            }
        val targetMillis = group.limitMinutes * 60_000L
        val targetReachedDuringDay =
            group.type == GroupType.ENCOURAGE &&
                periodUsageMillisBeforeDay < targetMillis &&
                periodUsageMillisAtClose >= targetMillis
        val earnedPoints =
            when (group.type) {
                GroupType.CONTROL -> pointLedgerDao.sumEarnedByDateAndGroup(archiveDate, group.id)
                GroupType.ENCOURAGE ->
                    calculateEncourageEarnedPoints(
                        usageMillis = dailyUsageMillis,
                        targetMinutes = group.limitMinutes,
                        pointsPerMinute = group.pointsPerMinute,
                        targetReachedDuringWindow = targetReachedDuringDay,
                    )
            }

        return GroupArchiveBuildResult(
            archive =
                DailyGroupArchiveEntity(
                    id = UUID.randomUUID().toString(),
                    archiveDate = archiveDate,
                    groupId = group.id,
                    groupName = group.name,
                    groupType = group.type,
                    limitPeriod = group.limitPeriod,
                    limitMinutes = group.limitMinutes,
                    bonusMinutes = bonusMinutes,
                    pointsPerMinute = group.pointsPerMinute,
                    packageCount = packageNames.size,
                    dailyUsageMillis = dailyUsageMillis,
                    periodUsageMillisAtClose = periodUsageMillisAtClose,
                    effectiveLimitMillisAtClose = effectiveLimitMillisAtClose,
                    remainingMillisAtClose = remainingMillisAtClose,
                    exceededMillisAtClose = exceededMillisAtClose,
                    blockEventCount = blockEventCount,
                    earnedPoints = earnedPoints,
                    spentPoints = 0.0,
                    completed = completed,
                    sortOrder =
                        when (group.type) {
                            GroupType.CONTROL -> sortOrder
                            GroupType.ENCOURAGE -> 10_000 + sortOrder
                        },
                    createdAt = archiveTime,
                    updatedAt = archiveTime,
                ),
            packageNames = packageNames,
        )
    }

    private fun buildDailyAppArchive(
        archiveDate: String,
        packageName: String,
        appLabel: String,
        groupArchive: DailyGroupArchiveEntity?,
        behaviorSummary: AppBehaviorSummary,
        dailyUsageMillis: Long,
        openCount: Int,
        earnedPoints: Double,
        archiveTime: Long,
    ): DailyAppArchiveEntity {
        val hourlyUsage = behaviorSummary.hourlyUsageMillis
        return DailyAppArchiveEntity(
            id = UUID.randomUUID().toString(),
            archiveDate = archiveDate,
            packageName = packageName,
            appLabel = appLabel,
            scopeKey = groupArchive?.groupId ?: UNGROUPED_SCOPE_KEY,
            isGrouped = groupArchive != null,
            groupId = groupArchive?.groupId,
            groupName = groupArchive?.groupName,
            groupType = groupArchive?.groupType,
            limitPeriod = groupArchive?.limitPeriod,
            dailyUsageMillis = dailyUsageMillis,
            openCount = openCount,
            sessionCount = behaviorSummary.sessionCount,
            longestSessionMillis = behaviorSummary.longestSessionMillis,
            nightUsageMillis = behaviorSummary.nightUsageMillis,
            earnedPoints = earnedPoints,
            completed = groupArchive?.completed ?: false,
            hour00Millis = hourlyUsage[0],
            hour01Millis = hourlyUsage[1],
            hour02Millis = hourlyUsage[2],
            hour03Millis = hourlyUsage[3],
            hour04Millis = hourlyUsage[4],
            hour05Millis = hourlyUsage[5],
            hour06Millis = hourlyUsage[6],
            hour07Millis = hourlyUsage[7],
            hour08Millis = hourlyUsage[8],
            hour09Millis = hourlyUsage[9],
            hour10Millis = hourlyUsage[10],
            hour11Millis = hourlyUsage[11],
            hour12Millis = hourlyUsage[12],
            hour13Millis = hourlyUsage[13],
            hour14Millis = hourlyUsage[14],
            hour15Millis = hourlyUsage[15],
            hour16Millis = hourlyUsage[16],
            hour17Millis = hourlyUsage[17],
            hour18Millis = hourlyUsage[18],
            hour19Millis = hourlyUsage[19],
            hour20Millis = hourlyUsage[20],
            hour21Millis = hourlyUsage[21],
            hour22Millis = hourlyUsage[22],
            hour23Millis = hourlyUsage[23],
            createdAt = archiveTime,
            updatedAt = archiveTime,
        )
    }

    private fun resolveAppLabel(packageName: String): String =
        runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0),
            ).toString().takeIf { it.isNotBlank() } ?: packageName
        }.getOrDefault(packageName)

    private data class GroupArchiveBuildResult(
        val archive: DailyGroupArchiveEntity,
        val packageNames: List<String>,
    )

    private data class ArchiveGroupConfig(
        val id: String,
        val name: String,
        val type: GroupType,
        val limitPeriod: LimitPeriod,
        val limitMinutes: Int,
        val bonusMinutes: Int,
        val pointsPerMinute: Double,
        val packageNames: List<String>,
        val sortOrder: Int,
    )
}
