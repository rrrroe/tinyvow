package com.rrrrz.tinyvow.data.steps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.PointLedgerEntryType
import com.rrrrz.tinyvow.data.db.StepDayEntity
import com.rrrrz.tinyvow.data.db.StepPointCreditEntity
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.repository.PointsRepository
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class TodayStepState(
    val date: String,
    val steps: Int,
    val available: Boolean,
    val permissionGranted: Boolean,
)

class StepTrackingRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
) {
    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val preferences = ManagedAppPreferences(appContext)
    private val pointsRepository = PointsRepository(appContext, database)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var listener: SensorEventListener? = null

    fun hasStepCounter(): Boolean = stepCounterSensor != null

    fun hasActivityRecognitionPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

    fun observeToday(date: String): Flow<TodayStepState> =
        database.stepDayDao().observeByDate(date).map { day ->
            TodayStepState(
                date = date,
                steps = day?.steps ?: 0,
                available = hasStepCounter(),
                permissionGranted = hasActivityRecognitionPermission(),
            )
        }

    fun start() {
        if (listener != null || stepCounterSensor == null || !hasActivityRecognitionPermission()) return
        val newListener =
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val sensorSteps = event.values.firstOrNull()?.toLong() ?: return
                    scope.launch {
                        handleSensorSteps(sensorSteps.coerceAtLeast(0L), System.currentTimeMillis())
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
        listener = newListener
        sensorManager.registerListener(newListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        listener?.let(sensorManager::unregisterListener)
        listener = null
    }

    fun restart() {
        stop()
        start()
    }

    suspend fun refreshFromSensorValueForTest(sensorSteps: Long, nowMillis: Long) {
        handleSensorSteps(sensorSteps, nowMillis)
    }

    suspend fun creditTodayStepsIfEligible(
        steps: Int,
        date: String,
        pointsPerStep: Double,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        if (steps <= 0 || pointsPerStep <= 0.0) return
        mutex.withLock {
            val credit = database.stepPointCreditDao().get(GLOBAL_STEP_POINT_SOURCE_ID, date)
            val creditedSteps = credit?.creditedSteps ?: 0
            val deltaSteps = (steps - creditedSteps).coerceAtLeast(0)
            if (deltaSteps <= 0) return@withLock

            pointsRepository.record(
                deltaPoints = deltaSteps * pointsPerStep,
                entryType = PointLedgerEntryType.USAGE_EARN,
                occurredAt = nowMillis,
                sourceRefId = "steps:$date:$steps",
                note = "Home step earn",
            )
            database.stepPointCreditDao().upsert(
                StepPointCreditEntity(
                    id = "$GLOBAL_STEP_POINT_SOURCE_ID:$date",
                    groupId = GLOBAL_STEP_POINT_SOURCE_ID,
                    creditDate = date,
                    creditedSteps = steps,
                    updatedAt = nowMillis,
                )
            )
        }
    }

    private suspend fun handleSensorSteps(sensorSteps: Long, nowMillis: Long) {
        mutex.withLock {
            val zoneId = ZoneId.systemDefault()
            val dayStartHour = preferences.getDayBoundaryHourOnce()
            val today = ArchiveDateUtils.localDateAt(nowMillis, zoneId, dayStartHour)
            val date = ArchiveDateUtils.formatDate(today)
            val existing = database.stepDayDao().getByDate(date)
            val previousBase = if (existing == null) {
                database.stepDayDao().getLatestBefore(date)?.lastSensorSteps
            } else {
                null
            }
            val updated =
                when {
                    existing == null -> {
                        val baseSteps = previousBase?.takeIf { sensorSteps >= it } ?: sensorSteps
                        StepDayEntity(
                            id = date,
                            stepDate = date,
                            steps = (sensorSteps - baseSteps).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                            sensorBaseSteps = baseSteps,
                            lastSensorSteps = sensorSteps,
                            updatedAt = nowMillis,
                        )
                    }

                    sensorSteps < existing.lastSensorSteps ->
                        StepDayEntity(
                            id = date,
                            stepDate = date,
                            steps = 0,
                            sensorBaseSteps = sensorSteps,
                            lastSensorSteps = sensorSteps,
                            updatedAt = nowMillis,
                        )

                    else ->
                        existing.copy(
                            steps = (sensorSteps - existing.sensorBaseSteps).coerceAtLeast(0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                            lastSensorSteps = sensorSteps,
                            updatedAt = nowMillis,
                        )
            }
            database.stepDayDao().upsert(updated)
        }
    }

    companion object {
        const val DEFAULT_POINTS_PER_STEP = 0.001
        private const val GLOBAL_STEP_POINT_SOURCE_ID = "home_steps"
    }
}
