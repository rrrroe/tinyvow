package com.rrrrz.tinyvow.data.steps

import com.rrrrz.tinyvow.data.db.StepDayEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StepDayAccumulatorTest {
    @Test
    fun countsDeltaFromPreviousDayWhenFirstSampleArrivesAfterDayBoundary() {
        val day =
            StepDayAccumulator.fromSensorValue(
                date = "2026-07-05",
                sensorSteps = 10_500L,
                previousLastSensorSteps = 10_000L,
                existing = null,
                nowMillis = 1L,
            )

        assertEquals(500, day.steps)
        assertEquals(10_000L, day.sensorBaseSteps)
        assertEquals(10_500L, day.lastSensorSteps)
    }

    @Test
    fun accumulatesOnlySameDaySensorDelta() {
        val existing =
            StepDayEntity(
                id = "2026-07-05",
                stepDate = "2026-07-05",
                steps = 0,
                sensorBaseSteps = 10_500L,
                lastSensorSteps = 10_500L,
                updatedAt = 1L,
            )

        val updated =
            StepDayAccumulator.fromSensorValue(
                date = "2026-07-05",
                sensorSteps = 11_000L,
                previousLastSensorSteps = null,
                existing = existing,
                nowMillis = 2L,
            )

        assertEquals(500, updated.steps)
        assertEquals(10_500L, updated.sensorBaseSteps)
        assertEquals(11_000L, updated.lastSensorSteps)
    }

    @Test
    fun startsFromZeroWhenThereIsNoEarlierBaseline() {
        val day =
            StepDayAccumulator.fromSensorValue(
                date = "2026-07-05",
                sensorSteps = 3_000L,
                previousLastSensorSteps = null,
                existing = null,
                nowMillis = 1L,
            )

        assertEquals(0, day.steps)
        assertEquals(3_000L, day.sensorBaseSteps)
        assertEquals(3_000L, day.lastSensorSteps)
    }

    @Test
    fun countsCurrentValueWhenCounterResetsBetweenDays() {
        val day =
            StepDayAccumulator.fromSensorValue(
                date = "2026-07-05",
                sensorSteps = 378L,
                previousLastSensorSteps = 42_000L,
                existing = null,
                nowMillis = 1L,
            )

        assertEquals(378, day.steps)
        assertEquals(0L, day.sensorBaseSteps)
        assertEquals(378L, day.lastSensorSteps)
    }

    @Test
    fun countsCurrentValueWhenCounterResetHappenedBeforeCurrentDay() {
        val day =
            StepDayAccumulator.fromSensorValue(
                date = "2026-07-05",
                sensorSteps = 53_841L,
                previousLastSensorSteps = 106_072L,
                existing = null,
                nowMillis = 1L,
            )

        assertEquals(53_841, day.steps)
        assertEquals(0L, day.sensorBaseSteps)
        assertEquals(53_841L, day.lastSensorSteps)
    }

    @Test
    fun replacesWithCurrentValueWhenSensorValueDrops() {
        val existing =
            StepDayEntity(
                id = "2026-07-05",
                stepDate = "2026-07-05",
                steps = 5_000,
                sensorBaseSteps = 10_000L,
                lastSensorSteps = 15_000L,
                updatedAt = 1L,
            )

        val afterReset =
            StepDayAccumulator.fromSensorValue(
                date = "2026-07-05",
                sensorSteps = 378L,
                previousLastSensorSteps = null,
                existing = existing,
                nowMillis = 2L,
            )

        assertEquals(378, afterReset.steps)
        assertEquals(0L, afterReset.sensorBaseSteps)
        assertEquals(378L, afterReset.lastSensorSteps)
    }
}
