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

    private class FakeSpecialOverride(
        private val enabledTypes: Set<GroupType>,
        private val replacement: Long?,
    ) : SpecialUsageOverride {
        override suspend fun isReplacementEnabled(groupType: GroupType?): Boolean =
            groupType == null || groupType in enabledTypes

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
    ) : UsageRepository {
        override suspend fun getTodayUsageMillis(packageName: String): Long = usage[packageName] ?: 0L
        override suspend fun getUsageInPeriod(packageName: String, period: LimitPeriod): Long = usage[packageName] ?: 0L
        override suspend fun getUsageStatsInPeriod(period: LimitPeriod): Map<String, Long> = usage
        override suspend fun getYesterdayUsageMillis(packageName: String): Long = 0L
        override suspend fun getUsageMillis(packageName: String, startMillis: Long, endMillis: Long): Long = usage[packageName] ?: 0L
        override suspend fun getUsageStats(startMillis: Long, endMillis: Long): Map<String, Long> = usage
        override suspend fun getUsageSessions(startMillis: Long, endMillis: Long): List<AppSession> = emptyList()
        override suspend fun getAppOpenCount(startMillis: Long, endMillis: Long): Map<String, Int> = emptyMap()
    }
}
