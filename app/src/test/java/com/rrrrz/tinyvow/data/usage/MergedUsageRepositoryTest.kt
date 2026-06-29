package com.rrrrz.tinyvow.data.usage

import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.special.WEREAD_PACKAGE_NAME
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MergedUsageRepositoryTest {
    @Test
    fun getUsageStats_replacesWeReadOnlyForEnabledGroupType() = runBlocking {
        val repository =
            MergedUsageRepository(
                baseRepository = FakeUsageRepository(mapOf(WEREAD_PACKAGE_NAME to 5_000L, "other" to 7_000L)),
                specialRepository = FakeSpecialOverride(enabledTypes = setOf(GroupType.ENCOURAGE), replacement = 30_000L),
            )

        val control = repository.getUsageStats(0L, 10_000L, GroupType.CONTROL)
        val encourage = repository.getUsageStats(0L, 10_000L, GroupType.ENCOURAGE)

        assertEquals(5_000L, control[WEREAD_PACKAGE_NAME])
        assertEquals(30_000L, encourage[WEREAD_PACKAGE_NAME])
        assertEquals(7_000L, encourage["other"])
    }

    @Test
    fun getUsageStats_fallsBackWhenReplacementIsMissing() = runBlocking {
        val repository =
            MergedUsageRepository(
                baseRepository = FakeUsageRepository(mapOf(WEREAD_PACKAGE_NAME to 5_000L)),
                specialRepository = FakeSpecialOverride(enabledTypes = setOf(GroupType.CONTROL), replacement = null),
            )

        val usage = repository.getUsageStats(0L, 10_000L, GroupType.CONTROL)

        assertEquals(5_000L, usage[WEREAD_PACKAGE_NAME])
    }

    @Test
    fun getYesterdayUsageMillis_usesUnifiedReplacementPath() = runBlocking {
        val repository =
            MergedUsageRepository(
                baseRepository = FakeUsageRepository(mapOf(WEREAD_PACKAGE_NAME to 5_000L)),
                specialRepository = FakeSpecialOverride(enabledTypes = emptySet(), replacement = 30_000L),
            )

        val usage = repository.getYesterdayUsageMillis(WEREAD_PACKAGE_NAME)

        assertEquals(30_000L, usage)
    }

    @Test
    fun detailBehaviorDataUsesBaseRepository() = runBlocking {
        val sessions = listOf(AppSession(WEREAD_PACKAGE_NAME, 1L, 2L))
        val repository =
            MergedUsageRepository(
                baseRepository = FakeUsageRepository(
                    usage = mapOf(WEREAD_PACKAGE_NAME to 5_000L),
                    sessions = sessions,
                    openCounts = mapOf(WEREAD_PACKAGE_NAME to 3),
                ),
                specialRepository = FakeSpecialOverride(enabledTypes = emptySet(), replacement = 30_000L),
            )

        assertEquals(sessions, repository.getUsageSessions(0L, 10_000L))
        assertEquals(mapOf(WEREAD_PACKAGE_NAME to 3), repository.getAppOpenCount(0L, 10_000L))
    }

    @Test
    fun getUsageStats_replacesConfiguredMediaApps() = runBlocking {
        val mediaPackage = "app.podcast.cosmos"
        val repository =
            MergedUsageRepository(
                baseRepository = FakeUsageRepository(mapOf(mediaPackage to 120_000L, "other" to 7_000L)),
                specialRepository = FakeSpecialOverride(enabledTypes = emptySet(), replacement = null),
                mediaRepository = FakeSpecialOverride(
                    enabledTypes = setOf(GroupType.CONTROL),
                    replacement = 45_000L,
                    packageNames = setOf(mediaPackage),
                ),
            )

        val usage = repository.getUsageStats(0L, 10_000L, GroupType.CONTROL)

        assertEquals(45_000L, usage[mediaPackage])
        assertEquals(7_000L, usage["other"])
    }

    private class FakeSpecialOverride(
        private val enabledTypes: Set<GroupType>,
        private val replacement: Long?,
        private val packageNames: Set<String> = setOf(WEREAD_PACKAGE_NAME),
    ) : SpecialUsageOverride {
        override suspend fun isReplacementEnabled(groupType: GroupType?): Boolean =
            groupType == null || groupType in enabledTypes

        override suspend fun replacementPackageNames(groupType: GroupType?): Set<String> =
            if (isReplacementEnabled(groupType)) packageNames else emptySet()

        override suspend fun replacementUsageMillis(
            packageName: String,
            startMillis: Long,
            endMillis: Long,
            groupType: GroupType?,
        ): Long? = replacement

        override suspend fun getUsageInPeriod(period: LimitPeriod): Long = replacement ?: 0L
    }

    private class FakeUsageRepository(
        private val usage: Map<String, Long>,
        private val sessions: List<AppSession> = emptyList(),
        private val openCounts: Map<String, Int> = emptyMap(),
    ) : UsageRepository {
        override suspend fun getTodayUsageMillis(packageName: String): Long = usage[packageName] ?: 0L
        override suspend fun getUsageInPeriod(packageName: String, period: LimitPeriod): Long = usage[packageName] ?: 0L
        override suspend fun getUsageStatsInPeriod(period: LimitPeriod): Map<String, Long> = usage
        override suspend fun getYesterdayUsageMillis(packageName: String): Long = 0L
        override suspend fun getUsageMillis(packageName: String, startMillis: Long, endMillis: Long): Long = usage[packageName] ?: 0L
        override suspend fun getUsageStats(startMillis: Long, endMillis: Long): Map<String, Long> = usage
        override suspend fun getUsageSessions(startMillis: Long, endMillis: Long): List<AppSession> = sessions
        override suspend fun getAppOpenCount(startMillis: Long, endMillis: Long): Map<String, Int> = openCounts
    }
}
