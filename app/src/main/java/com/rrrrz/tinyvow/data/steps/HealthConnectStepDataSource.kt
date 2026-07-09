package com.rrrrz.tinyvow.data.steps

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

class HealthConnectStepDataSource(
    private val context: Context,
) {
    fun isAvailable(): Boolean =
        runCatching {
            HealthConnectClient.getSdkStatus(context, HEALTH_CONNECT_PROVIDER_PACKAGE) ==
                HealthConnectClient.SDK_AVAILABLE
        }.getOrDefault(false)

    suspend fun hasReadStepsPermission(): Boolean =
        runCatching {
            if (!isAvailable()) return@runCatching false
            healthConnectClient()
                .permissionController
                .getGrantedPermissions()
                .containsAll(STEP_PERMISSIONS)
        }.getOrDefault(false)

    suspend fun readSteps(startMillis: Long, endMillis: Long): Long? =
        runCatching {
            if (!hasReadStepsPermission()) return@runCatching null
            val response =
                healthConnectClient().aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter =
                            TimeRangeFilter.between(
                                Instant.ofEpochMilli(startMillis),
                                Instant.ofEpochMilli(endMillis),
                            ),
                    ),
                )
            response[StepsRecord.COUNT_TOTAL]?.coerceAtLeast(0L) ?: 0L
        }.getOrNull()

    private fun healthConnectClient(): HealthConnectClient =
        HealthConnectClient.getOrCreate(context)

    companion object {
        const val HEALTH_CONNECT_PROVIDER_PACKAGE = "com.google.android.apps.healthdata"
        val STEP_PERMISSIONS: Set<String> = setOf(HealthPermission.getReadPermission(StepsRecord::class))
    }
}
