package com.rrrrz.tinyvow.data.steps

import com.rrrrz.tinyvow.data.db.StepDayEntity

internal object StepDayAccumulator {
    fun fromSensorValue(
        date: String,
        sensorSteps: Long,
        previousLastSensorSteps: Long?,
        existing: StepDayEntity?,
        nowMillis: Long,
    ): StepDayEntity {
        val safeSensorSteps = sensorSteps.coerceAtLeast(0L)
        return when {
            existing == null -> {
                val steps =
                    when {
                        previousLastSensorSteps != null && safeSensorSteps >= previousLastSensorSteps ->
                            safeSensorSteps - previousLastSensorSteps
                        previousLastSensorSteps != null -> safeSensorSteps
                        else -> 0L
                    }
                StepDayEntity(
                    id = date,
                    stepDate = date,
                    steps = steps.coerceToStepCount(),
                    sensorBaseSteps = sensorSteps - steps,
                    lastSensorSteps = sensorSteps,
                    updatedAt = nowMillis,
                )
            }

            safeSensorSteps < existing.lastSensorSteps ->
                StepDayEntity(
                    id = date,
                    stepDate = date,
                    steps = safeSensorSteps.coerceToStepCount(),
                    sensorBaseSteps = 0L,
                    lastSensorSteps = safeSensorSteps,
                    updatedAt = nowMillis,
                )

            else -> {
                val deltaSteps = safeSensorSteps - existing.lastSensorSteps
                val updatedSteps = existing.steps.toLong() + deltaSteps
                existing.copy(
                    steps = updatedSteps.coerceAtLeast(0L).coerceToStepCount(),
                    sensorBaseSteps = safeSensorSteps - updatedSteps,
                    lastSensorSteps = safeSensorSteps,
                    updatedAt = nowMillis,
                )
            }
        }
    }

    private fun Long.coerceToStepCount(): Int =
        coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}
