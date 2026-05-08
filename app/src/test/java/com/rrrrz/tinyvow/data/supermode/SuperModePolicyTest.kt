package com.rrrrz.tinyvow.data.supermode

import com.rrrrz.tinyvow.data.db.RewardType
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SuperModePolicyTest {
    private val zoneId: ZoneId = ZoneId.of("Asia/Shanghai")

    @Test
    fun `hash secret uses salt and verifies`() {
        val salt = SuperModeCrypto.newSalt()
        val hash = SuperModeCrypto.hashSecret("1234", salt)

        assertTrue(SuperModeCrypto.verifySecret("1234", salt, hash))
        assertFalse(SuperModeCrypto.verifySecret("4321", salt, hash))
        assertNotEquals(hash, SuperModeCrypto.hashSecret("1234", SuperModeCrypto.newSalt()))
    }

    @Test
    fun `build status uses free default window`() {
        val state = configuredState(customWindowStartMinutes = 12 * 60, customWindowEndMinutes = 13 * 60)
        val now = atMillis(2026, 5, 8, 7, 30)

        val status = SuperModePolicy.buildStatus(state, isProActive = false, nowMillis = now, zoneId = zoneId)

        assertTrue(status.isConfigured)
        assertTrue(status.isAvailableNow)
        assertEquals(6 * 60, status.windowStartMinutes)
        assertEquals(10 * 60, status.windowEndMinutes)
    }

    @Test
    fun `build status uses pro custom window and exits outside range`() {
        val now = atMillis(2026, 5, 8, 11, 0)
        val state =
            configuredState(
                isActive = true,
                lastActiveAtMillis = atMillis(2026, 5, 8, 10, 58),
                customWindowStartMinutes = 12 * 60,
                customWindowEndMinutes = 13 * 60,
            )

        val status = SuperModePolicy.buildStatus(state, isProActive = true, nowMillis = now, zoneId = zoneId)

        assertFalse(status.isAvailableNow)
        assertFalse(status.isActive)
        assertNull(status.expiresAt)
    }

    @Test
    fun `build status expires after idle timeout`() {
        val lastActiveAt = atMillis(2026, 5, 8, 7, 0)
        val now = lastActiveAt + SuperModePolicy.IDLE_TIMEOUT_MILLIS + 1
        val state = configuredState(isActive = true, lastActiveAtMillis = lastActiveAt)

        val status = SuperModePolicy.buildStatus(state, isProActive = false, nowMillis = now, zoneId = zoneId)

        assertFalse(status.isActive)
        assertEquals(0L, status.remainingMillis)
    }

    @Test
    fun `guarded action maps only protected rewards`() {
        assertEquals(GuardedAction.PURCHASE_TIME_ADD, GuardedAction.fromRewardType(RewardType.TIME_ADD))
        assertEquals(GuardedAction.PURCHASE_PERIOD_PASS, GuardedAction.fromRewardType(RewardType.PERIOD_PASS))
        assertEquals(GuardedAction.PURCHASE_EMERGENCY_UNLOCK, GuardedAction.fromRewardType(RewardType.EMERGENCY_UNLOCK))
        assertEquals(null, GuardedAction.fromRewardType(RewardType.STREAK_SHIELD))
        assertEquals(null, GuardedAction.fromRewardType(RewardType.DOUBLE_POINTS_DAY))
        assertEquals(null, GuardedAction.fromRewardType(RewardType.CUSTOM))
    }

    @Test
    fun `window validation rejects cross midnight or invalid values`() {
        assertTrue(SuperModePolicy.isValidWindow(6 * 60, 10 * 60))
        assertFalse(SuperModePolicy.isValidWindow(10 * 60, 6 * 60))
        assertFalse(SuperModePolicy.isValidWindow(-1, 10 * 60))
        assertFalse(SuperModePolicy.isValidWindow(6 * 60, 24 * 60 + 1))
    }

    private fun configuredState(
        isActive: Boolean = false,
        lastActiveAtMillis: Long? = null,
        customWindowStartMinutes: Int? = null,
        customWindowEndMinutes: Int? = null,
    ): SuperModeStoredState =
        SuperModeStoredState(
            enabled = true,
            passwordHash = "hash",
            passwordSalt = "salt",
            recoveryQuestion = "Question?",
            recoveryAnswerHash = "answerHash",
            recoveryAnswerSalt = "answerSalt",
            isActive = isActive,
            lastActiveAtMillis = lastActiveAtMillis,
            customWindowStartMinutes = customWindowStartMinutes,
            customWindowEndMinutes = customWindowEndMinutes,
        )

    private fun atMillis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Long =
        LocalDateTime.of(year, month, day, hour, minute)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
}
