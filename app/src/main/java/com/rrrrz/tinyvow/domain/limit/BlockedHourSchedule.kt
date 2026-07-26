package com.rrrrz.tinyvow.domain.limit

/**
 * Stores a CONTROL group's local-clock unavailable hours in the lower 24 bits of a [Long].
 * A selected hour represents the half-open interval from HH:00 to the following hour.
 */
object BlockedHourSchedule {
    const val HOURS_PER_DAY = 24

    fun isBlocked(mask: Long, hour: Int): Boolean {
        require(hour in 0 until HOURS_PER_DAY)
        return mask and (1L shl hour) != 0L
    }

    fun toggle(mask: Long, hour: Int): Long {
        require(hour in 0 until HOURS_PER_DAY)
        return mask xor (1L shl hour)
    }

    fun count(mask: Long): Int = (mask and VALID_HOURS_MASK).countOneBits()

    private const val VALID_HOURS_MASK = (1L shl HOURS_PER_DAY) - 1L
}
