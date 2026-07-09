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
import androidx.room.withTransaction
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.PointLedgerEntity
import com.rrrrz.tinyvow.data.db.PointLedgerEntryType
import com.rrrrz.tinyvow.data.db.STEP_DAY_SOURCE_HEALTH_CONNECT
import com.rrrrz.tinyvow.data.db.STEP_DAY_SOURCE_SENSOR
import com.rrrrz.tinyvow.data.db.StepDayEntity
import com.rrrrz.tinyvow.data.db.StepPointCreditEntity
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.repository.AppLimitRepository
import com.rrrrz.tinyvow.data.repository.PointsRepository
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.abs

data class TodayStepState(
    val date: String,
    val steps: Int,
    val available: Boolean,
    val permissionGranted: Boolean,
    val source: String? = null,
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
    private val healthConnectStepDataSource = HealthConnectStepDataSource(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var listener: SensorEventListener? = null

    fun hasStepCounter(): Boolean = stepCounterSensor != null

    fun hasActivityRecognitionPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

    fun isHealthConnectAvailable(): Boolean = healthConnectStepDataSource.isAvailable()

    suspend fun hasHealthConnectStepPermission(): Boolean =
        healthConnectStepDataSource.hasReadStepsPermission()

    fun observeToday(date: String): Flow<TodayStepState> =
        database.stepDayDao().observeByDate(date).map { day ->
            TodayStepState(
                date = date,
                steps = day?.steps ?: 0,
                available = hasStepCounter() || isHealthConnectAvailable(),
                permissionGranted = hasActivityRecognitionPermission(),
                source = day?.source,
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
        if (sensorManager.registerListener(newListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            listener = newListener
        }
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

    suspend fun refreshTodayFromHealthConnect(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val zoneId = ZoneId.systemDefault()
        val dayStartHour = preferences.getDayBoundaryHourOnce()
        val today = ArchiveDateUtils.localDateAt(nowMillis, zoneId, dayStartHour)
        val date = ArchiveDateUtils.formatDate(today)
        val startMillis = ArchiveDateUtils.startOfDayMillis(today, zoneId, dayStartHour)
        val endMillis = ArchiveDateUtils.startOfDayMillis(today.plusDays(1), zoneId, dayStartHour)
        val healthSteps =
            healthConnectStepDataSource
                .readSteps(startMillis, minOf(nowMillis, endMillis))
                ?.coerceAtMost(Int.MAX_VALUE.toLong())
                ?: return false
        val existing = database.stepDayDao().getByDate(date)
        if (healthSteps <= 0L && existing?.source != STEP_DAY_SOURCE_HEALTH_CONNECT) {
            return false
        }
        database.stepDayDao().upsert(
            StepDayEntity(
                id = date,
                stepDate = date,
                steps = healthSteps.toInt(),
                sensorBaseSteps = 0L,
                lastSensorSteps = existing?.lastSensorSteps ?: 0L,
                source = STEP_DAY_SOURCE_HEALTH_CONNECT,
                updatedAt = nowMillis,
            )
        )
        return true
    }

    suspend fun creditTodayStepsIfEligible(
        steps: Int,
        date: String,
        pointsPerStep: Double,
        nowMillis: Long = System.currentTimeMillis(),
        allowNewCredit: Boolean = true,
    ) {
        if (pointsPerStep <= 0.0) return
        mutex.withLock {
            val safeSteps = steps.coerceAtLeast(0)
            val credit = database.stepPointCreditDao().get(GLOBAL_STEP_POINT_SOURCE_ID, date)
            if (credit == null && !allowNewCredit) return@withLock
            val stepEntries =
                database.pointLedgerDao().getStepEarnEntriesByDate(
                    date = date,
                    sourceRefPrefix = stepSourceRefPrefix(date),
                )
            val creditedPoints = stepEntries.sumOf { it.deltaPoints }
            val targetPoints = safeSteps * pointsPerStep
            val balanceDelta = targetPoints - creditedPoints
            if (abs(balanceDelta) <= POINT_EPSILON && credit?.creditedSteps == safeSteps) {
                return@withLock
            }

            database.withTransaction {
                stepEntries.forEach { entry ->
                    database.pointLedgerDao().deleteById(entry.id)
                }
                if (targetPoints > POINT_EPSILON) {
                    database.pointLedgerDao().insert(
                        PointLedgerEntity(
                            id = UUID.randomUUID().toString(),
                            occurredAt = nowMillis,
                            ledgerDate = date,
                            entryType = PointLedgerEntryType.USAGE_EARN,
                            deltaPoints = targetPoints,
                            sourceRefId = "${stepSourceRefPrefix(date)}total",
                            note = "Home step earn",
                            createdAt = nowMillis,
                        ),
                    )
                }
                database.stepPointCreditDao().upsert(
                    StepPointCreditEntity(
                        id = "$GLOBAL_STEP_POINT_SOURCE_ID:$date",
                        groupId = GLOBAL_STEP_POINT_SOURCE_ID,
                        creditDate = date,
                        creditedSteps = safeSteps,
                        updatedAt = nowMillis,
                    )
                )
            }
            if (abs(balanceDelta) > POINT_EPSILON) {
                pointsRepository.applyBalanceDelta(balanceDelta)
                AppLimitRepository(context, database).checkAchievements()
            }
        }
    }

    private suspend fun handleSensorSteps(sensorSteps: Long, nowMillis: Long) {
        mutex.withLock {
            val zoneId = ZoneId.systemDefault()
            val dayStartHour = preferences.getDayBoundaryHourOnce()
            val today = ArchiveDateUtils.localDateAt(nowMillis, zoneId, dayStartHour)
            val date = ArchiveDateUtils.formatDate(today)
            val existing = database.stepDayDao().getByDate(date)
            if (existing?.source == STEP_DAY_SOURCE_HEALTH_CONNECT) {
                return@withLock
            }
            val previousBase =
                if (existing == null) {
                    database.stepDayDao()
                        .getLatestBefore(date)
                        ?.takeIf {
                            it.stepDate == ArchiveDateUtils.formatDate(today.minusDays(1)) &&
                                it.source == STEP_DAY_SOURCE_SENSOR
                        }
                        ?.lastSensorSteps
                } else {
                    null
                }
            val updated =
                StepDayAccumulator.fromSensorValue(
                    date = date,
                    sensorSteps = sensorSteps,
                    previousLastSensorSteps = previousBase,
                    existing = existing,
                    nowMillis = nowMillis,
                ).copy(source = STEP_DAY_SOURCE_SENSOR)
            database.stepDayDao().upsert(updated)
        }
    }

    companion object {
        const val DEFAULT_POINTS_PER_STEP = 0.001
        const val DEFAULT_REWARD_THRESHOLD = 8000
        private const val GLOBAL_STEP_POINT_SOURCE_ID = "home_steps"
        private const val POINT_EPSILON = 0.0001

        private fun stepSourceRefPrefix(date: String): String = "steps:$date:"
    }
}
