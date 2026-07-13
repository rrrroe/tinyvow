package com.rrrrz.tinyvow.data.steps

import android.content.Context
import android.os.Build
import android.os.ext.SdkExtensions
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal enum class HealthConnectStepProbeStatus {
    XIAOMI_SOURCE_FOUND,
    XIAOMI_SOURCE_DIAGNOSTICS_INCOMPLETE,
    XIAOMI_SOURCE_WITH_MANUAL_RECORDS,
    MULTIPLE_XIAOMI_SOURCES,
    SYSTEM_DATA_ONLY,
    NO_RECORDS,
}

internal data class HealthConnectStepProbeSnapshot(
    val date: String,
    val status: HealthConnectStepProbeStatus,
    val xiaomiSteps: Long?,
    val systemSteps: Long?,
    val sourcePackage: String?,
    val xiaomiSourceTotals: Map<String, Long>,
    val originPackages: List<String>,
    val rawRecordCount: Int,
    val xiaomiRecordCount: Int,
    val manualRecordCount: Int,
    val recordingMethods: List<Int>,
    val latestXiaomiRecordEndMillis: Long?,
    val latestXiaomiModifiedMillis: Long?,
    val rawRecordsComplete: Boolean,
    val queryStartMillis: Long,
    val queryEndMillis: Long,
    val fetchedAtMillis: Long,
    val uExtensionVersion: Int,
)

internal enum class HealthConnectStepProbeCacheWarning {
    CORRUPT_RESET,
    WRITE_FAILED,
}

internal data class HealthConnectStepProbeHistoryResult(
    val retainedHistory: List<HealthConnectStepProbeSnapshot>,
    val cacheWarning: HealthConnectStepProbeCacheWarning?,
)

internal data class HealthConnectStepProbeReadResult(
    val snapshot: HealthConnectStepProbeSnapshot,
    val retainedHistory: List<HealthConnectStepProbeSnapshot>,
    val cacheWarning: HealthConnectStepProbeCacheWarning?,
)

internal class HealthConnectStepProbe(context: Context) {
    private val appContext = context.applicationContext
    private val preferences =
        appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val readMutex = Mutex()

    fun isAvailable(): Boolean =
        try {
            HealthConnectClient.getSdkStatus(appContext) == HealthConnectClient.SDK_AVAILABLE
        } catch (_: Exception) {
            false
        }

    suspend fun hasReadStepsPermission(): Boolean =
        if (!isAvailable()) {
            false
        } else {
            healthConnectClient()
                .permissionController
                .getGrantedPermissions()
                .containsAll(STEP_PERMISSIONS)
        }

    suspend fun loadRetainedHistory(
        today: LocalDate = LocalDate.now(ZoneId.systemDefault()),
    ): HealthConnectStepProbeHistoryResult =
        withContext(Dispatchers.IO) {
            val decoded = decodeHistory(preferences.getString(KEY_HISTORY, null))
            val retained = retainRecentSevenDays(decoded.snapshots, today)
            var warning = decoded.warning
            if (retained != decoded.snapshots || decoded.warning != null) {
                if (!writeHistory(retained)) {
                    warning = HealthConnectStepProbeCacheWarning.WRITE_FAILED
                }
            }
            HealthConnectStepProbeHistoryResult(
                retainedHistory = retained,
                cacheWarning = warning,
            )
        }

    suspend fun readToday(
        now: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): HealthConnectStepProbeReadResult =
        readMutex.withLock {
            check(isAvailable()) { "Health Connect unavailable" }
            check(hasReadStepsPermission()) { "READ_STEPS permission not granted" }

            val today = now.atZone(zoneId).toLocalDate()
            val start = today.atStartOfDay(zoneId).toInstant()
            val range = TimeRangeFilter.between(start, now)
            val client = healthConnectClient()
            val systemAggregation = aggregateSteps(client, range)
            val systemSteps = systemAggregation.stepTotalOrNull()
            val rawRecordsResult = readRawRecords(client, range)
            val rawRecords = rawRecordsResult.records

            val sourceTotals =
                XIAOMI_SOURCE_PACKAGES.mapNotNull { packageName ->
                    val aggregation =
                        aggregateSteps(
                            client = client,
                            range = range,
                            dataOrigins = setOf(DataOrigin(packageName)),
                        )
                    aggregation.stepTotalOrNull()?.let { total -> packageName to total }
                }
            val sourceTotalsMap = sourceTotals.toMap()
            val selectedSource = sourceTotals.singleOrNull()
            val selectedPackage = selectedSource?.first
            val xiaomiRecords =
                rawRecords.filter {
                    it.metadata.dataOrigin.packageName in XIAOMI_SOURCE_PACKAGES
                }
            val manualRecordCount =
                xiaomiRecords.count {
                    it.metadata.recordingMethod == Metadata.RECORDING_METHOD_MANUAL_ENTRY
                }
            val status =
                when {
                    sourceTotals.size > 1 -> HealthConnectStepProbeStatus.MULTIPLE_XIAOMI_SOURCES
                    selectedSource != null &&
                        (!rawRecordsResult.complete || xiaomiRecords.isEmpty()) ->
                        HealthConnectStepProbeStatus.XIAOMI_SOURCE_DIAGNOSTICS_INCOMPLETE
                    selectedSource != null && manualRecordCount > 0 ->
                        HealthConnectStepProbeStatus.XIAOMI_SOURCE_WITH_MANUAL_RECORDS
                    selectedSource != null -> HealthConnectStepProbeStatus.XIAOMI_SOURCE_FOUND
                    systemSteps != null -> HealthConnectStepProbeStatus.SYSTEM_DATA_ONLY
                    else -> HealthConnectStepProbeStatus.NO_RECORDS
                }
            val originPackages =
                buildSet {
                    addAll(systemAggregation.dataOrigins.map { it.packageName })
                    addAll(rawRecords.map { it.metadata.dataOrigin.packageName })
                }.filter { it.isNotBlank() }.sorted()
            val fetchedAtMillis = Instant.now().toEpochMilli()
            val snapshot =
                HealthConnectStepProbeSnapshot(
                    date = today.toString(),
                    status = status,
                    xiaomiSteps = selectedSource?.second,
                    systemSteps = systemSteps,
                    sourcePackage = selectedPackage,
                    xiaomiSourceTotals = sourceTotalsMap,
                    originPackages = originPackages,
                    rawRecordCount = rawRecords.size,
                    xiaomiRecordCount = xiaomiRecords.size,
                    manualRecordCount = manualRecordCount,
                    recordingMethods =
                        xiaomiRecords.map { it.metadata.recordingMethod }.distinct().sorted(),
                    latestXiaomiRecordEndMillis =
                        xiaomiRecords.maxOfOrNull { it.endTime.toEpochMilli() },
                    latestXiaomiModifiedMillis =
                        xiaomiRecords.maxOfOrNull { it.metadata.lastModifiedTime.toEpochMilli() },
                    rawRecordsComplete = rawRecordsResult.complete,
                    queryStartMillis = start.toEpochMilli(),
                    queryEndMillis = now.toEpochMilli(),
                    fetchedAtMillis = fetchedAtMillis,
                    uExtensionVersion = currentUExtensionVersion(),
                )
            val retained =
                withContext(Dispatchers.IO) {
                    val existing = decodeHistory(preferences.getString(KEY_HISTORY, null))
                    val updated =
                        if (snapshot.status == HealthConnectStepProbeStatus.XIAOMI_SOURCE_FOUND) {
                            retainRecentSevenDays(existing.snapshots + snapshot, today)
                        } else {
                            retainRecentSevenDays(existing.snapshots, today)
                        }
                    val writeWarning =
                        if (writeHistory(updated)) {
                            null
                        } else {
                            HealthConnectStepProbeCacheWarning.WRITE_FAILED
                        }
                    HealthConnectStepProbeHistoryResult(
                        retainedHistory = updated,
                        cacheWarning = writeWarning ?: existing.warning,
                    )
                }
            HealthConnectStepProbeReadResult(
                snapshot = snapshot,
                retainedHistory = retained.retainedHistory,
                cacheWarning = retained.cacheWarning,
            )
        }

    private fun healthConnectClient(): HealthConnectClient =
        HealthConnectClient.getOrCreate(appContext)

    private suspend fun aggregateSteps(
        client: HealthConnectClient,
        range: TimeRangeFilter,
        dataOrigins: Set<DataOrigin> = emptySet(),
    ): AggregationResult =
        client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = range,
                dataOriginFilter = dataOrigins,
            ),
        )

    private suspend fun readRawRecords(
        client: HealthConnectClient,
        range: TimeRangeFilter,
    ): RawRecordsResult {
        val records = mutableListOf<StepsRecord>()
        val seenTokens = mutableSetOf<String>()
        var pageToken: String? = null
        var pageCount = 0
        var complete = true
        do {
            val response =
                client.readRecords(
                    ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = range,
                        dataOriginFilter = emptySet(),
                        ascendingOrder = true,
                        pageSize = RAW_PAGE_SIZE,
                        pageToken = pageToken,
                    ),
                )
            records += response.records
            pageCount += 1
            val nextToken = response.pageToken?.takeIf { it.isNotBlank() }
            if (nextToken == null) {
                pageToken = null
            } else if (!seenTokens.add(nextToken) || pageCount >= MAX_RAW_PAGES) {
                pageToken = null
                complete = false
            } else {
                pageToken = nextToken
            }
        } while (pageToken != null)
        return RawRecordsResult(records = records, complete = complete)
    }

    private fun AggregationResult.stepTotalOrNull(): Long? =
        if (contains(StepsRecord.COUNT_TOTAL)) {
            this[StepsRecord.COUNT_TOTAL]?.coerceAtLeast(0L)
        } else {
            null
        }

    private fun writeHistory(history: List<HealthConnectStepProbeSnapshot>): Boolean {
        val array = JSONArray()
        history.forEach { snapshot -> array.put(snapshot.toJson()) }
        val root =
            JSONObject()
                .put("schemaVersion", CACHE_SCHEMA_VERSION)
                .put("snapshots", array)
        return preferences.edit().putString(KEY_HISTORY, root.toString()).commit()
    }

    private data class RawRecordsResult(
        val records: List<StepsRecord>,
        val complete: Boolean,
    )

    private data class DecodedHistory(
        val snapshots: List<HealthConnectStepProbeSnapshot>,
        val warning: HealthConnectStepProbeCacheWarning?,
    )

    companion object {
        val STEP_PERMISSIONS: Set<String> =
            setOf(HealthPermission.getReadPermission(StepsRecord::class))

        internal val XIAOMI_SOURCE_PACKAGES =
            listOf(
                "com.mi.health",
                "com.xiaomi.wearable",
            )

        private const val PREFERENCES_NAME = "health_connect_step_probe"
        private const val KEY_HISTORY = "recent_snapshots"
        private const val CACHE_SCHEMA_VERSION = 1
        private const val RAW_PAGE_SIZE = 1000
        private const val MAX_RAW_PAGES = 100
        private const val RETAINED_DAYS = 7L

        internal fun clearStoredHistory(context: Context): Boolean =
            context.deleteSharedPreferences(PREFERENCES_NAME)

        internal fun storedHistoryFile(context: Context): File =
            File(
                context.applicationInfo.dataDir,
                "shared_prefs/$PREFERENCES_NAME.xml",
            )

        internal fun retainRecentSevenDays(
            snapshots: List<HealthConnectStepProbeSnapshot>,
            today: LocalDate,
        ): List<HealthConnectStepProbeSnapshot> {
            val oldest = today.minusDays(RETAINED_DAYS - 1L)
            return snapshots
                .mapNotNull { snapshot ->
                    runCatching { LocalDate.parse(snapshot.date) }
                        .getOrNull()
                        ?.let { date -> date to snapshot }
                }
                .filter { (date, _) -> !date.isBefore(oldest) && !date.isAfter(today) }
                .groupBy { (date, _) -> date }
                .map { (date, values) ->
                    date to values.maxBy { (_, snapshot) -> snapshot.fetchedAtMillis }.second
                }
                .sortedByDescending { (date, _) -> date }
                .take(RETAINED_DAYS.toInt())
                .map { (_, snapshot) -> snapshot }
        }

        private fun decodeHistory(raw: String?): DecodedHistory =
            if (raw.isNullOrBlank()) {
                DecodedHistory(emptyList(), null)
            } else {
                runCatching {
                    val root = JSONObject(raw)
                    check(root.optInt("schemaVersion", 0) == CACHE_SCHEMA_VERSION) {
                        "Unsupported step probe cache version"
                    }
                    val array = root.getJSONArray("snapshots")
                    val snapshots = buildList {
                        for (index in 0 until array.length()) {
                            array.getJSONObject(index).toSnapshot()
                                .let(::add)
                        }
                    }
                    DecodedHistory(snapshots, null)
                }.getOrElse {
                    DecodedHistory(
                        snapshots = emptyList(),
                        warning = HealthConnectStepProbeCacheWarning.CORRUPT_RESET,
                    )
                }
            }

        private fun currentUExtensionVersion(): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            } else {
                0
            }
    }
}

private fun HealthConnectStepProbeSnapshot.toJson(): JSONObject =
    JSONObject().apply {
        put("date", date)
        put("status", status.name)
        putNullableLong("xiaomiSteps", xiaomiSteps)
        putNullableLong("systemSteps", systemSteps)
        putNullableString("sourcePackage", sourcePackage)
        put("xiaomiSourceTotals", xiaomiSourceTotals.toJsonObject())
        put("originPackages", originPackages.toStringJsonArray())
        put("rawRecordCount", rawRecordCount)
        put("xiaomiRecordCount", xiaomiRecordCount)
        put("manualRecordCount", manualRecordCount)
        put("recordingMethods", recordingMethods.toIntJsonArray())
        putNullableLong("latestXiaomiRecordEndMillis", latestXiaomiRecordEndMillis)
        putNullableLong("latestXiaomiModifiedMillis", latestXiaomiModifiedMillis)
        put("rawRecordsComplete", rawRecordsComplete)
        put("queryStartMillis", queryStartMillis)
        put("queryEndMillis", queryEndMillis)
        put("fetchedAtMillis", fetchedAtMillis)
        put("uExtensionVersion", uExtensionVersion)
    }

private fun JSONObject.toSnapshot(): HealthConnectStepProbeSnapshot =
    HealthConnectStepProbeSnapshot(
        date = getString("date"),
        status = HealthConnectStepProbeStatus.valueOf(getString("status")),
        xiaomiSteps = nullableLong("xiaomiSteps"),
        systemSteps = nullableLong("systemSteps"),
        sourcePackage = nullableString("sourcePackage"),
        xiaomiSourceTotals = optJSONObject("xiaomiSourceTotals").toLongMap(),
        originPackages = optJSONArray("originPackages").toStringList(),
        rawRecordCount = optInt("rawRecordCount", 0),
        xiaomiRecordCount = optInt("xiaomiRecordCount", 0),
        manualRecordCount = optInt("manualRecordCount", 0),
        recordingMethods = optJSONArray("recordingMethods").toIntList(),
        latestXiaomiRecordEndMillis = nullableLong("latestXiaomiRecordEndMillis"),
        latestXiaomiModifiedMillis = nullableLong("latestXiaomiModifiedMillis"),
        rawRecordsComplete = optBoolean("rawRecordsComplete", true),
        queryStartMillis = getLong("queryStartMillis"),
        queryEndMillis = getLong("queryEndMillis"),
        fetchedAtMillis = getLong("fetchedAtMillis"),
        uExtensionVersion = optInt("uExtensionVersion", 0),
    )

private fun JSONObject.putNullableLong(name: String, value: Long?) {
    put(name, value ?: JSONObject.NULL)
}

private fun JSONObject.putNullableString(name: String, value: String?) {
    put(name, value ?: JSONObject.NULL)
}

private fun JSONObject.nullableLong(name: String): Long? =
    if (!has(name) || isNull(name)) null else getLong(name)

private fun JSONObject.nullableString(name: String): String? =
    if (!has(name) || isNull(name)) null else getString(name)

private fun Iterable<String>.toStringJsonArray(): JSONArray =
    JSONArray().also { array -> forEach(array::put) }

private fun Iterable<Int>.toIntJsonArray(): JSONArray =
    JSONArray().also { array -> forEach(array::put) }

private fun Map<String, Long>.toJsonObject(): JSONObject =
    JSONObject().also { json -> forEach { (key, value) -> json.put(key, value) } }

private fun JSONObject?.toLongMap(): Map<String, Long> =
    if (this == null) {
        emptyMap()
    } else {
        buildMap {
            keys().forEach { key -> put(key, getLong(key)) }
        }
    }

private fun JSONArray?.toStringList(): List<String> =
    if (this == null) {
        emptyList()
    } else {
        buildList {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

private fun JSONArray?.toIntList(): List<Int> =
    if (this == null) {
        emptyList()
    } else {
        buildList {
            for (index in 0 until length()) {
                add(optInt(index))
            }
        }
    }
