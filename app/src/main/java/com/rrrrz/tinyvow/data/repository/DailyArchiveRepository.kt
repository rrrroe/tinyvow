package com.rrrrz.tinyvow.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import androidx.room.withTransaction
import com.rrrrz.tinyvow.data.db.ActiveRewardEffectStatus
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
import com.rrrrz.tinyvow.data.db.RewardEffectBenefitEntity
import com.rrrrz.tinyvow.data.db.RewardEffectBenefitType
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.db.TopAppArchiveSummary
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.special.SpecialAppUsageRepository
import com.rrrrz.tinyvow.data.usage.UsageRepository
import com.rrrrz.tinyvow.data.usage.MergedUsageRepository
import com.rrrrz.tinyvow.domain.limit.isControlTimeoutForStats
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.roundToInt

class DailyArchiveRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val usageRepository: UsageRepository = MergedUsageRepository(context),
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
    private val activeRewardEffectDao = database.activeRewardEffectDao()
    private val rewardEffectBenefitDao = database.rewardEffectBenefitDao()
    private val redemptionHistoryDao = database.redemptionHistoryDao()
    private val dailyArchiveDao = database.dailyArchiveDao()
    private val dailyGroupArchiveDao = database.dailyGroupArchiveDao()
    private val dailyAppArchiveDao = database.dailyAppArchiveDao()
    private val pointLedgerDao = database.pointLedgerDao()
    private val stateDao = database.dailyArchiveStateDao()
    private val blockEventDao = database.blockEventDao()
    private val preferences = ManagedAppPreferences(context)
    private val specialAppUsageRepository = SpecialAppUsageRepository(context, database)

    fun getRecentArchives(limit: Int = 90): Flow<List<DailyArchiveEntity>> = dailyArchiveDao.getRecent(limit)

    fun getArchiveByDate(date: String): Flow<DailyArchiveEntity?> = dailyArchiveDao.getByDate(date)

    fun getArchivesByRange(from: String, to: String): Flow<List<DailyArchiveEntity>> =
        dailyArchiveDao.getByDateRange(from, to)

    fun getGroupArchivesByDate(date: String): Flow<List<DailyGroupArchiveEntity>> = dailyGroupArchiveDao.getByDate(date)

    fun getGroupArchivesByRange(from: String, to: String): Flow<List<DailyGroupArchiveEntity>> =
        dailyGroupArchiveDao.getByDateRange(from, to)

    fun getAppArchivesByDate(date: String): Flow<List<DailyAppArchiveEntity>> = dailyAppArchiveDao.getByDate(date)

    fun getRewardEffectBenefitsByDate(date: String): Flow<List<RewardEffectBenefitEntity>> =
        rewardEffectBenefitDao.observeByDate(date)

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
            AppLimitRepository(context, database).refreshStreakShieldPending()
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
                AppLimitRepository(context, database).refreshStreakShieldPending()
                checkAchievementsAfterArchive()
                return@withContext
            }

            val archiveStartDate = LocalDate.parse(state.archiveStartDate)
            if (!today.isAfter(archiveStartDate)) {
                repairArchivesMissingAppSnapshots()
                AppLimitRepository(context, database).refreshStreakShieldPending()
                checkAchievementsAfterArchive()
                return@withContext
            }

            val endDate = today.minusDays(1)
            val nextDate =
                state.lastArchivedDate?.let { LocalDate.parse(it).plusDays(1) } ?: archiveStartDate
            if (nextDate.isAfter(endDate)) {
                repairArchivesMissingAppSnapshots()
                AppLimitRepository(context, database).refreshStreakShieldPending()
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
                AppLimitRepository(context, database).refreshStreakShieldPending()
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
        val launchablePackageNames = loadLaunchablePackageNames()
        dailyArchiveDao.getAllArchiveDatesAsc().forEach { dateString ->
            val date = LocalDate.parse(dateString)
            val dayStart = ArchiveDateUtils.startOfDayMillis(date, zoneId)
            val nextDayStart = ArchiveDateUtils.nextDayStartMillis(date, zoneId)
            val activePackageNames =
                usageRepository
                    .getUsageStats(dayStart, nextDayStart)
                    .filterValues { it >= MIN_UNGROUPED_APP_ARCHIVE_USAGE_MILLIS }
                    .keys
            val hasUngroupedCandidates =
                selectUngroupedLaunchablePackages(
                    activePackageNames = activePackageNames,
                    groupedPackageNames = groupedPackageNames,
                    launchablePackageNames = launchablePackageNames,
                ).isNotEmpty()
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
        val dailyUsageByType =
            mapOf(
                GroupType.CONTROL to usageRepository.getUsageStats(dayStart, nextDayStart, GroupType.CONTROL),
                GroupType.ENCOURAGE to usageRepository.getUsageStats(dayStart, nextDayStart, GroupType.ENCOURAGE),
            )
        val dailyUsageByPackage = usageRepository.getUsageStats(dayStart, nextDayStart)
        val dailyOpenCountByPackage = usageRepository.getAppOpenCount(dayStart, nextDayStart)
        val groupedPackageNames =
            (crossRefs.asSequence().map { it.packageName } +
                existingGroupedAppsByGroup.values.asSequence().flatten().map { it.packageName })
                .distinct()
                .toSet()
        val launchablePackageNames = loadLaunchablePackageNames()
        val activePackageNames =
            dailyUsageByPackage
                .filterValues { it >= MIN_UNGROUPED_APP_ARCHIVE_USAGE_MILLIS }
                .keys
        val ungroupedLaunchablePackages =
            selectUngroupedLaunchablePackages(
                activePackageNames = activePackageNames,
                groupedPackageNames = groupedPackageNames,
                launchablePackageNames = launchablePackageNames,
            )
        val packagesToArchive = selectPackagesToArchive(groupedPackageNames, ungroupedLaunchablePackages)
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
                .map { ArchiveDateUtils.periodStart(date, it.limitPeriod) to it.type }
                .distinct()
                .associateWith { (periodStart, groupType) ->
                    usageRepository.getUsageStats(
                        ArchiveDateUtils.startOfDayMillis(periodStart, zoneId),
                        nextDayStart,
                        groupType,
                    )
                }
        val periodUsageBeforeDayByStart =
            groupConfigs
                .map { ArchiveDateUtils.periodStart(date, it.limitPeriod) to it.type }
                .distinct()
                .associateWith { (periodStart, groupType) ->
                    val periodStartMillis = ArchiveDateUtils.startOfDayMillis(periodStart, zoneId)
                    if (periodStartMillis >= dayStart) {
                        emptyMap()
                    } else {
                        usageRepository.getUsageStats(periodStartMillis, dayStart, groupType)
                    }
                }

        val groupBuildResults =
            groupConfigs.mapIndexed { index, group ->
                buildGroupArchive(
                    date = date,
                    archiveDate = archiveDate,
                    group = group,
                    dayStart = dayStart,
                    dayEnd = dayEnd,
                    nextDayStart = nextDayStart,
                    archiveTime = archiveTime,
                    sortOrder = index,
                    dailyUsageByPackage = dailyUsageByType[group.type].orEmpty(),
                    periodUsageByPackage =
                        periodUsageByStart[
                            ArchiveDateUtils.periodStart(date, group.limitPeriod) to group.type
                        ].orEmpty(),
                    periodUsageBeforeDayByPackage =
                        periodUsageBeforeDayByStart[
                            ArchiveDateUtils.periodStart(date, group.limitPeriod) to group.type
                        ].orEmpty(),
                    sessionsByPackage = sessionsByPackage,
                    blockEventCount = blockEventDao.countByDateAndGroup(archiveDate, group.id),
                )
            }
        val groupSnapshots = groupBuildResults.map { it.archive }
        val rewardEffectBenefits = groupBuildResults.flatMap { it.rewardEffectBenefits }
        val groupedAppArchives =
            groupBuildResults.flatMap { groupResult ->
                val allocatedEarnedPoints =
                    allocateGroupEarnedPoints(
                        totalPoints = groupResult.archive.earnedPoints,
                        packageNames = groupResult.packageNames,
                        usageByPackage = groupResult.dailyUsageByPackage,
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
                        dailyUsageMillis = groupResult.dailyUsageByPackage[packageName] ?: 0L,
                        openCount = dailyOpenCountByPackage[packageName] ?: 0,
                        earnedPoints = allocatedEarnedPoints[packageName] ?: 0.0,
                        usageSource = specialAppUsageRepository.usageSourceForDate(
                            packageName = packageName,
                            date = archiveDate,
                            groupType = groupResult.archive.groupType,
                        ),
                        archiveTime = archiveTime,
                    )
                }
            }
        val ungroupedAppArchives =
            ungroupedLaunchablePackages.map { packageName ->
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
                    usageSource = specialAppUsageRepository.usageSourceForDate(
                        packageName = packageName,
                        date = archiveDate,
                        groupType = null,
                    ),
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
                controlUsageMillis = controlPackageNames.sumOf { packageName -> dailyUsageByType[GroupType.CONTROL]?.get(packageName) ?: 0L },
                encourageUsageMillis = encouragePackageNames.sumOf { packageName -> dailyUsageByType[GroupType.ENCOURAGE]?.get(packageName) ?: 0L },
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
            rewardEffectBenefitDao.deleteByDate(archiveDate)
            rewardEffectBenefits.forEach { benefit -> rewardEffectBenefitDao.upsert(benefit) }
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
        dayStart: Long,
        dayEnd: Long,
        nextDayStart: Long,
        archiveTime: Long,
        sortOrder: Int,
        dailyUsageByPackage: Map<String, Long>,
        periodUsageByPackage: Map<String, Long>,
        periodUsageBeforeDayByPackage: Map<String, Long>,
        sessionsByPackage: Map<String, List<com.rrrrz.tinyvow.data.usage.AppSession>>,
        blockEventCount: Int,
    ): GroupArchiveBuildResult {
        val packageNames = group.packageNames
        val dailyUsageMillis = packageNames.sumOf { packageName -> dailyUsageByPackage[packageName] ?: 0L }
        val periodUsageMillisAtClose = packageNames.sumOf { packageName -> periodUsageByPackage[packageName] ?: 0L }
        val periodUsageMillisBeforeDay = packageNames.sumOf { packageName -> periodUsageBeforeDayByPackage[packageName] ?: 0L }
        val archiveEffects = activeRewardEffectDao.getEffectsForGroupOnDate(group.id, archiveDate)
        val bonusMinutes = group.bonusMinutes
        val rewardExtraMinutes =
            archiveEffects
                .filter { it.effectType == RewardType.TIME_ADD || it.effectType == RewardType.EMERGENCY_UNLOCK }
                .sumOf { parseRewardPayload(it.payloadJson).minutes }
        val hasPeriodPass = archiveEffects.any { it.effectType == RewardType.PERIOD_PASS }
        val activeDoublePointsEffect =
            archiveEffects.firstOrNull {
                it.effectType == RewardType.DOUBLE_POINTS_DAY &&
                    it.status == ActiveRewardEffectStatus.ACTIVE
            }
        val pointsMultiplier = parseRewardPayload(activeDoublePointsEffect?.payloadJson).pointsMultiplier.coerceAtLeast(1.0)
        val effectiveLimitMillisAtClose = (group.limitMinutes + bonusMinutes + rewardExtraMinutes) * 60_000L
        val remainingMillisAtClose = max(effectiveLimitMillisAtClose - periodUsageMillisAtClose, 0L)
        val exceededMillisAtClose = max(periodUsageMillisAtClose - effectiveLimitMillisAtClose, 0L)
        val controlCompleted =
            hasPeriodPass || !isControlTimeoutForStats(exceededMillisAtClose)
        val completed =
            when (group.type) {
                GroupType.CONTROL -> controlCompleted
                GroupType.ENCOURAGE -> periodUsageMillisAtClose >= effectiveLimitMillisAtClose
            }
        val targetMillis = group.limitMinutes * 60_000L
        val targetReachedDuringDay =
            group.type == GroupType.ENCOURAGE &&
                periodUsageMillisBeforeDay < targetMillis &&
                periodUsageMillisAtClose >= targetMillis
        val doubledUsageMillis =
            if (group.type == GroupType.ENCOURAGE && activeDoublePointsEffect != null && pointsMultiplier > 1.0) {
                sumSessionUsageInRange(
                    packageNames = packageNames,
                    sessionsByPackage = sessionsByPackage,
                    rangeStart = activeDoublePointsEffect.startAt.coerceAtLeast(dayStart),
                    rangeEnd = nextDayStart,
                )
            } else {
                0L
            }
        val normalUsageMillis = (dailyUsageMillis - doubledUsageMillis).coerceAtLeast(0L)
        val dailyUsageBeforeDouble =
            if (group.type == GroupType.ENCOURAGE && activeDoublePointsEffect != null) {
                sumSessionUsageInRange(
                    packageNames = packageNames,
                    sessionsByPackage = sessionsByPackage,
                    rangeStart = dayStart,
                    rangeEnd = activeDoublePointsEffect.startAt.coerceAtLeast(dayStart),
                )
            } else {
                dailyUsageMillis
            }
        val targetReachedAfterDoubleActivation =
            group.type == GroupType.ENCOURAGE &&
                targetReachedDuringDay &&
                activeDoublePointsEffect != null &&
                (periodUsageMillisBeforeDay + dailyUsageBeforeDouble) < targetMillis
        val rewardBonusPoints =
            if (group.type == GroupType.ENCOURAGE && pointsMultiplier > 1.0) {
                val doubledUsagePoints =
                    calculateUsageEarnedPoints(doubledUsageMillis, group.pointsPerMinute) * (pointsMultiplier - 1.0)
                val doubledTargetBonusPoints =
                    if (targetReachedAfterDoubleActivation) {
                        calculateTargetBonusPoints(group.limitMinutes, group.pointsPerMinute) * (pointsMultiplier - 1.0)
                    } else {
                        0.0
                    }
                doubledUsagePoints + doubledTargetBonusPoints
            } else {
                0.0
            }
        val rewardEffectBenefits =
            buildRewardEffectBenefits(
                archiveDate = archiveDate,
                archiveTime = archiveTime,
                group = group,
                effects = archiveEffects,
                rewardExtraMinutes = rewardExtraMinutes,
                hasPeriodPass = hasPeriodPass,
                exceededMillisAtClose = exceededMillisAtClose,
                rewardBonusPoints = rewardBonusPoints,
            )
        val earnedPoints =
            when (group.type) {
                GroupType.CONTROL -> pointLedgerDao.sumEarnedByDateAndGroup(archiveDate, group.id)
                GroupType.ENCOURAGE ->
                    calculateUsageEarnedPoints(normalUsageMillis, group.pointsPerMinute) +
                        calculateUsageEarnedPoints(doubledUsageMillis, group.pointsPerMinute) * pointsMultiplier +
                        if (targetReachedDuringDay) {
                            calculateTargetBonusPoints(group.limitMinutes, group.pointsPerMinute) *
                                if (targetReachedAfterDoubleActivation) pointsMultiplier else 1.0
                        } else {
                            0.0
                        }
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
                    rewardExempted = hasPeriodPass,
                    rewardExemptType = if (hasPeriodPass) RewardType.PERIOD_PASS.name else null,
                    rewardBonusPoints = rewardBonusPoints,
                    rewardEffectSnapshotJson =
                        if (archiveEffects.isEmpty()) {
                            null
                        } else {
                            JSONObject()
                                .put("effectTypes", archiveEffects.joinToString(",") { it.effectType.name })
                                .put("extraMinutes", rewardExtraMinutes)
                                .put("pointsMultiplier", pointsMultiplier)
                                .put("bonusPoints", rewardBonusPoints)
                                .toString()
                        },
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
            dailyUsageByPackage = dailyUsageByPackage,
            rewardBonusPoints = rewardBonusPoints,
            rewardEffectBenefits = rewardEffectBenefits,
        )
    }

    private fun buildRewardEffectBenefits(
        archiveDate: String,
        archiveTime: Long,
        group: ArchiveGroupConfig,
        effects: List<com.rrrrz.tinyvow.data.db.ActiveRewardEffectEntity>,
        rewardExtraMinutes: Int,
        hasPeriodPass: Boolean,
        exceededMillisAtClose: Long,
        rewardBonusPoints: Double,
    ): List<RewardEffectBenefitEntity> =
        buildList {
            val timeEffects =
                effects.filter { it.effectType == RewardType.TIME_ADD || it.effectType == RewardType.EMERGENCY_UNLOCK }
            if (timeEffects.isNotEmpty() && rewardExtraMinutes > 0) {
                val usedExtraMinutes =
                    minOf(rewardExtraMinutes, (exceededMillisAtClose / 60_000L).toInt().coerceAtLeast(0))
                timeEffects.forEach { effect ->
                    val payload = parseRewardPayload(effect.payloadJson)
                    val share =
                        if (rewardExtraMinutes > 0) {
                            (usedExtraMinutes * (payload.minutes.toDouble() / rewardExtraMinutes.toDouble())).roundToInt()
                        } else {
                            0
                        }
                    if (share > 0) {
                        add(
                            RewardEffectBenefitEntity(
                                id = "${effect.id}:$archiveDate",
                                effectId = effect.id,
                                rewardId = effect.sourceRewardId,
                                rewardBuiltinKey = effect.sourceBuiltinKey,
                                rewardType = effect.effectType,
                                archiveDate = archiveDate,
                                targetGroupId = group.id,
                                targetGroupNameSnapshot = effect.targetGroupNameSnapshot ?: group.name,
                                benefitType =
                                    if (effect.effectType == RewardType.EMERGENCY_UNLOCK) {
                                        RewardEffectBenefitType.EMERGENCY_UNLOCK_USED
                                    } else {
                                        RewardEffectBenefitType.EXTRA_TIME_USED
                                    },
                                benefitMinutes = share,
                                createdAt = archiveTime,
                            ),
                        )
                    }
                }
            }
            val periodPass = effects.firstOrNull { it.effectType == RewardType.PERIOD_PASS }
            if (hasPeriodPass && periodPass != null) {
                val exemptedMinutes = (exceededMillisAtClose / 60_000L).toInt().coerceAtLeast(0)
                if (exemptedMinutes > 0) {
                    add(
                        RewardEffectBenefitEntity(
                            id = "${periodPass.id}:$archiveDate",
                            effectId = periodPass.id,
                            rewardId = periodPass.sourceRewardId,
                            rewardBuiltinKey = periodPass.sourceBuiltinKey,
                            rewardType = periodPass.effectType,
                            archiveDate = archiveDate,
                            targetGroupId = group.id,
                            targetGroupNameSnapshot = periodPass.targetGroupNameSnapshot ?: group.name,
                            benefitType = RewardEffectBenefitType.PERIOD_PASS_EXEMPTED,
                            benefitMinutes = exemptedMinutes,
                            createdAt = archiveTime,
                        ),
                    )
                }
            }
            val doublePoints = effects.firstOrNull { it.effectType == RewardType.DOUBLE_POINTS_DAY }
            if (doublePoints != null && rewardBonusPoints > 0.0) {
                add(
                    RewardEffectBenefitEntity(
                        id = "${doublePoints.id}:$archiveDate",
                        effectId = doublePoints.id,
                        rewardId = doublePoints.sourceRewardId,
                        rewardBuiltinKey = doublePoints.sourceBuiltinKey,
                        rewardType = doublePoints.effectType,
                        archiveDate = archiveDate,
                        targetGroupId = group.id,
                        targetGroupNameSnapshot = doublePoints.targetGroupNameSnapshot ?: group.name,
                        benefitType = RewardEffectBenefitType.DOUBLE_POINTS_EARNED,
                        benefitPoints = rewardBonusPoints,
                        createdAt = archiveTime,
                    ),
                )
            }
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
        usageSource: com.rrrrz.tinyvow.data.special.SpecialAppUsageSource?,
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
            usageSource = usageSource?.provider,
            usageSourceSyncedAt = usageSource?.syncedAt,
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

    private fun loadLaunchablePackageNames(): Set<String> {
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvedActivities =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    launchIntent,
                    PackageManager.ResolveInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(launchIntent, 0)
            }
        return resolvedActivities
            .asSequence()
            .mapNotNull { it.activityInfo?.packageName }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun sumSessionUsageInRange(
        packageNames: List<String>,
        sessionsByPackage: Map<String, List<com.rrrrz.tinyvow.data.usage.AppSession>>,
        rangeStart: Long,
        rangeEnd: Long,
    ): Long {
        if (rangeEnd <= rangeStart) return 0L
        return packageNames.sumOf { packageName ->
            sessionsByPackage[packageName]
                .orEmpty()
                .sumOf { session ->
                    val overlapStart = maxOf(session.startTime, rangeStart)
                    val overlapEnd = minOf(session.endTime, rangeEnd)
                    (overlapEnd - overlapStart).coerceAtLeast(0L)
                }
        }
    }

    private data class GroupArchiveBuildResult(
        val archive: DailyGroupArchiveEntity,
        val packageNames: List<String>,
        val dailyUsageByPackage: Map<String, Long>,
        val rewardBonusPoints: Double = 0.0,
        val rewardEffectBenefits: List<RewardEffectBenefitEntity> = emptyList(),
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

internal fun selectUngroupedLaunchablePackages(
    activePackageNames: Set<String>,
    groupedPackageNames: Set<String>,
    launchablePackageNames: Set<String>,
): Set<String> = (activePackageNames - groupedPackageNames).intersect(launchablePackageNames)

internal fun selectPackagesToArchive(
    groupedPackageNames: Set<String>,
    ungroupedLaunchablePackages: Set<String>,
): Set<String> = groupedPackageNames + ungroupedLaunchablePackages
