package com.rrrrz.tinyvow.domain.limit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockedHourScheduleTest {
    @Test
    fun selectedHoursRepresentWholeLocalClockHours() {
        var mask = 0L
        mask = BlockedHourSchedule.toggle(mask, 20)
        mask = BlockedHourSchedule.toggle(mask, 21)

        assertTrue(BlockedHourSchedule.isBlocked(mask, 20))
        assertTrue(BlockedHourSchedule.isBlocked(mask, 21))
        assertFalse(BlockedHourSchedule.isBlocked(mask, 22))
        assertEquals(2, BlockedHourSchedule.count(mask))
    }

    @Test
    fun togglingSelectedHourRemovesIt() {
        val selected = BlockedHourSchedule.toggle(0L, 0)
        val cleared = BlockedHourSchedule.toggle(selected, 0)

        assertEquals(0, BlockedHourSchedule.count(cleared))
    }
}
