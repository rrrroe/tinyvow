package com.rrrrz.tinyvow.data.privacy

import android.content.Context
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.rrrrz.tinyvow.BuildConfig
import com.rrrrz.tinyvow.data.activation.LocalActivationSubscriptionRepository
import com.rrrrz.tinyvow.data.auth.LocalAuthRepository
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.repository.FocusIconStorage
import com.rrrrz.tinyvow.data.repository.RewardIconStorage
import com.rrrrz.tinyvow.data.server.BackendSubscriptionStore
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.special.WeReadApiKeyStore
import com.rrrrz.tinyvow.data.steps.HealthConnectStepProbe
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class LocalBackupRestoreResult(
    val requiresRestart: Boolean,
    val warnings: List<String>,
)

class LocalDataManager(
    private val context: Context,
    private val database: AppDatabase,
    private val preferences: ManagedAppPreferences,
) {
    suspend fun exportPrivacyReport(runtimeDiagnostics: String? = null): File = withContext(Dispatchers.IO) {
        val snapshot = LocalPrivacySnapshot(
            exportedAtMillis = System.currentTimeMillis(),
            tableSummaries = localDataTables.map { table ->
                LocalDataTableSummary(
                    tableName = table.name,
                    description = table.description,
                    rowCount = countRows(table.name),
                )
            },
            localStoreSummaries = localDataStores.map { store ->
                val file = store.resolve(context)
                LocalStoreSummary(
                    name = store.name,
                    description = store.description,
                    present = file.exists(),
                    fileCount = countFiles(file),
                    byteCount = sumBytes(file),
                )
            },
            runtimeDiagnostics = runtimeDiagnostics,
        )
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        File(shareDir, "tinyvow-local-data-${snapshot.exportedAtMillis}.json").also { file ->
            file.writeText(LocalPrivacyReportFormatter.format(snapshot), Charsets.UTF_8)
        }
    }

    suspend fun exportLocalBackup(): File = withContext(Dispatchers.IO) {
        val exportedAtMillis = System.currentTimeMillis()
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val output = File(shareDir, "tinyvow-local-backup-$exportedAtMillis.zip")
        val tempOutput = File(shareDir, "tinyvow-local-backup-$exportedAtMillis.tmp")

        val sources = collectBackupSources()
        val requiresWeReadKeyReentry = WeReadApiKeyStore(context, preferences).hasKey()
        ZipOutputStream(FileOutputStream(tempOutput)).use { zip ->
            zip.putNextEntry(ZipEntry(BACKUP_MANIFEST_ENTRY))
            zip.write(
                JSONObject()
                    .put("format", BACKUP_FORMAT)
                    .put("schemaVersion", BACKUP_SCHEMA_VERSION)
                    .put("packageName", context.packageName)
                    .put("appVersionName", BuildConfig.VERSION_NAME)
                    .put("appVersionCode", BuildConfig.VERSION_CODE)
                    .put("exportedAtMillis", exportedAtMillis)
                    .put("requiresWeReadKeyReentry", requiresWeReadKeyReentry)
                    .toString()
                    .toByteArray(Charsets.UTF_8),
            )
            zip.closeEntry()
            sources.forEach { source ->
                zip.putNextEntry(ZipEntry(source.pathInZip))
                FileInputStream(source.file).use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
        tempOutput.copyTo(output, overwrite = true)
        tempOutput.delete()
        output
    }

    suspend fun restoreLocalBackup(sourceUri: Uri): LocalBackupRestoreResult = withContext(Dispatchers.IO) {
        val tempRoot = File(context.cacheDir, "backup-restore-${UUID.randomUUID()}").apply { mkdirs() }
        val zipFile = File(tempRoot, "import.zip")
        val unzipRoot = File(tempRoot, "unzipped").apply { mkdirs() }
        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(zipFile).use { output ->
                    copyWithLimit(
                        input = input,
                        output = output,
                        maxBytes = MAX_BACKUP_ZIP_BYTES,
                    )
                }
            } ?: error("Unable to open backup file.")
            extractBackupZipWithLimits(zipFile, unzipRoot)
            val manifest = validateBackupManifest(File(unzipRoot, BACKUP_MANIFEST_ENTRY))
            restoreFromUnzippedFiles(unzipRoot)
            LocalBackupRestoreResult(
                requiresRestart = true,
                warnings = if (manifest.optBoolean("requiresWeReadKeyReentry")) {
                    listOf("weread_key_material_not_restored")
                } else {
                    emptyList()
                },
            )
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    suspend fun clearLocalData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
            preferences.clearAll()
            LocalAuthRepository.clearStoredSession(context)
            LocalActivationSubscriptionRepository.clearStoredActivationData(context)
            BackendSubscriptionStore.clearStoredData(context)
            RewardIconStorage.fromContext(context).clearAll()
            FocusIconStorage.fromContext(context).clearAll()
            HealthConnectStepProbe.clearStoredHistory(context)
            WeReadApiKeyStore.deleteStoredKeyMaterial()
            WeReadApiKeyStore.deletePendingRestoredKey(context)
            File(context.cacheDir, "share").deleteRecursively()
        }
    }

    private fun collectBackupSources(): List<BackupSource> {
        val sources = mutableListOf<BackupSource>()
        collectDatabaseFiles().forEach { file ->
            sources += BackupSource("databases/${file.name}", file)
        }
        collectDataStoreFiles().forEach { file ->
            sources += BackupSource("datastore/${file.name}", file)
        }
        listOf("reward_icons", "focus_icons").forEach { directoryName ->
            val iconDir = File(context.filesDir, directoryName)
            if (iconDir.isDirectory) {
                iconDir.walkTopDown()
                    .filter { it.isFile }
                    .forEach { file ->
                        val relative = file.relativeTo(iconDir).invariantSeparatorsPath
                        sources += BackupSource("$directoryName/$relative", file)
                    }
            }
        }
        return sources
    }

    private fun collectDatabaseFiles(): List<File> {
        val base = context.getDatabasePath(AppDatabase.DEFAULT_DATABASE_NAME)
        val candidates = listOf(
            base,
            File(base.parentFile, "${base.name}-wal"),
            File(base.parentFile, "${base.name}-shm"),
        )
        return candidates.filter { it.exists() && it.isFile }
    }

    private fun collectDataStoreFiles(): List<File> {
        val names = listOf(
            "managed_app_preferences",
            "auth_preferences",
            "activation_preferences",
            "backend_subscription_preferences",
        )
        return names.map { dataStoreFile(context, it) }.filter { it.exists() && it.isFile }
    }

    private fun validateBackupManifest(file: File): JSONObject {
        require(file.isFile) { "Backup manifest is missing." }
        val json = JSONObject(file.readText(Charsets.UTF_8))
        validateBackupManifestFields(
            manifest = json,
            expectedPackageName = context.packageName,
            currentVersionCode = BuildConfig.VERSION_CODE,
        )
        return json
    }

    private fun restoreFromUnzippedFiles(unzipRoot: File) {
        database.close()
        AppDatabase.closeActiveInstance()

        restoreDatabaseFiles(unzipRoot)
        restoreDataStoreFiles(unzipRoot)
        restoreRewardIcons(unzipRoot)
        restoreFocusIcons(unzipRoot)
    }

    private fun restoreDatabaseFiles(unzipRoot: File) {
        val base = context.getDatabasePath(AppDatabase.DEFAULT_DATABASE_NAME)
        val dbDir = base.parentFile ?: error("Database directory not found.")
        dbDir.mkdirs()
        listOf(base, File(dbDir, "${base.name}-wal"), File(dbDir, "${base.name}-shm")).forEach {
            if (it.exists()) it.delete()
        }
        val sourceDir = File(unzipRoot, "databases")
        if (!sourceDir.isDirectory) return
        sourceDir.listFiles()?.filter { it.isFile }?.forEach { source ->
            source.copyTo(File(dbDir, source.name), overwrite = true)
        }
    }

    private fun restoreDataStoreFiles(unzipRoot: File) {
        val targetDir = dataStoreDirectory(context).apply { mkdirs() }
        listOf(
            "managed_app_preferences.preferences_pb",
            "auth_preferences.preferences_pb",
            "activation_preferences.preferences_pb",
            "backend_subscription_preferences.preferences_pb",
        ).forEach { name ->
            File(targetDir, name).delete()
            val source = File(unzipRoot, "datastore/$name")
            if (source.isFile) {
                source.copyTo(File(targetDir, name), overwrite = true)
            }
        }
    }

    private fun restoreRewardIcons(unzipRoot: File) {
        restoreManagedFileDirectory(unzipRoot, "reward_icons")
    }

    private fun restoreFocusIcons(unzipRoot: File) {
        restoreManagedFileDirectory(unzipRoot, "focus_icons")
    }

    private fun restoreManagedFileDirectory(
        unzipRoot: File,
        directoryName: String,
    ) {
        val targetDir = File(context.filesDir, directoryName)
        targetDir.deleteRecursively()
        val sourceDir = File(unzipRoot, directoryName)
        if (!sourceDir.isDirectory) return
        sourceDir.walkTopDown().forEach { source ->
            val relative = source.relativeTo(sourceDir).invariantSeparatorsPath
            if (relative.isBlank()) return@forEach
            val target = File(targetDir, relative)
            if (source.isDirectory) {
                target.mkdirs()
            } else {
                target.parentFile?.mkdirs()
                source.copyTo(target, overwrite = true)
            }
        }
    }

    private fun countRows(tableName: String): Int {
        database.query(SimpleSQLiteQuery("SELECT COUNT(*) FROM `$tableName`")).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private data class LocalDataTable(
        val name: String,
        val description: String,
    )

    private data class LocalDataStore(
        val name: String,
        val description: String,
        val resolve: (Context) -> File,
    )

    private data class BackupSource(
        val pathInZip: String,
        val file: File,
    )

    companion object {
        const val BACKUP_FORMAT = "tinyvow-local-backup"
        const val BACKUP_SCHEMA_VERSION = 1
        const val BACKUP_MANIFEST_ENTRY = "backup_manifest.json"
        const val MAX_BACKUP_ZIP_BYTES = 256L * 1024L * 1024L
        const val MAX_BACKUP_UNZIPPED_BYTES = 256L * 1024L * 1024L
        const val MAX_BACKUP_ENTRY_BYTES = 64L * 1024L * 1024L
        const val MAX_BACKUP_ZIP_ENTRIES = 5_000

        fun dataStoreDirectory(context: Context): File = File(context.filesDir, "datastore")

        fun dataStoreFile(context: Context, name: String): File =
            File(dataStoreDirectory(context), "$name.preferences_pb")

        internal fun dataStoreFile(filesDir: File, name: String): File =
            File(File(filesDir, "datastore"), "$name.preferences_pb")

        fun countFiles(file: File): Int =
            when {
                !file.exists() -> 0
                file.isFile -> 1
                else -> file.walkTopDown().count { it.isFile }
            }

        fun sumBytes(file: File): Long =
            when {
                !file.exists() -> 0L
                file.isFile -> file.length()
                else -> file.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            }

        private val localDataTables = listOf(
            LocalDataTable("app_groups", "User-created control and encouragement groups."),
            LocalDataTable("group_app_cross_ref", "Package names assigned to user-created groups."),
            LocalDataTable("daily_archives", "Daily aggregate usage, saved time, points, and redemption counts."),
            LocalDataTable("daily_group_archives", "Per-group daily usage summaries."),
            LocalDataTable("daily_app_archives", "Per-app package labels, usage duration, sessions, opens, and night usage."),
            LocalDataTable("daily_app_time_slice_archives", "Per-app five-minute foreground usage slices used by daily attention maps."),
            LocalDataTable("step_days", "Local daily step totals used by step encouragement groups."),
            LocalDataTable("step_point_credits", "Local counters preventing duplicate step point credits."),
            LocalDataTable("point_ledger", "Local points earned or spent."),
            LocalDataTable("redemptions", "Local reward catalog."),
            LocalDataTable("redemption_history", "Local reward redemption history."),
            LocalDataTable("reward_inventory", "Local owned reward inventory."),
            LocalDataTable("reward_use_history", "Local reward use history."),
            LocalDataTable("bonus_times", "Temporary time-pack bonus records."),
            LocalDataTable("active_reward_effects", "Currently active local reward effects."),
            LocalDataTable("reward_effect_benefits", "Local benefit summaries produced by reward effects."),
            LocalDataTable("streak_shield_pending", "Pending streak shield decisions."),
            LocalDataTable("achievements", "Local achievement progress and unlock state."),
            LocalDataTable("block_events", "Local records of app blocking events."),
            LocalDataTable("daily_archive_state", "Local archive job state."),
            LocalDataTable("special_app_configs", "Local special app data-source settings without API keys."),
            LocalDataTable("special_app_usage_snapshots", "Local cached special app WeRead reading durations and phone foreground durations."),
            LocalDataTable("special_app_point_credits", "Local counters preventing duplicate special app point credits."),
            LocalDataTable("media_app_configs", "Local podcast, music, and audiobook app playback monitoring settings."),
            LocalDataTable("media_app_playback_days", "Local trusted background playback durations and untrusted gaps for monitored media apps."),
            LocalDataTable("media_app_playback_segments", "Local trusted playback intervals used to merge media playback with foreground usage without double counting."),
            LocalDataTable("lock_screen_timer_app_configs", "Local lock-screen timer app monitoring settings."),
            LocalDataTable("lock_screen_timer_app_days", "Local trusted lock-screen timer durations for monitored apps."),
            LocalDataTable("lock_screen_timer_app_segments", "Local trusted lock-screen intervals used to merge timer activity with foreground usage without double counting."),
            LocalDataTable("offline_focus_categories", "Local focus types, colors, icons, point rates, sort order, and archive state."),
            LocalDataTable("offline_focus_sessions", "Local focus timer sessions, mode, duration snapshots, and awarded points."),
            LocalDataTable("protection_events", "Local Super Mode and guarded-setting change history."),
            LocalDataTable("daily_checkins", "Local daily check-in records and granted buffer item references."),
        )

        private val localDataStores = listOf(
            LocalDataStore(
                "managed_app_preferences",
                "DataStore preferences for points, theme, language, permission prompts, profile, app color choices, focus defaults, debug Pro, Super Mode, and encrypted special app key metadata.",
            ) { context -> dataStoreFile(context, "managed_app_preferences") },
            LocalDataStore(
                "auth_preferences",
                "DataStore preferences for the local user session used by the domestic activation channel.",
            ) { context -> dataStoreFile(context, "auth_preferences") },
            LocalDataStore(
                "activation_preferences",
                "DataStore preferences for domestic activation status, used code IDs, and wall-clock rollback checks.",
            ) { context -> dataStoreFile(context, "activation_preferences") },
            LocalDataStore(
                "backend_subscription_preferences",
                "DataStore preferences for the domestic backend device session, cached account profile, pending orders, and Pro entitlement. Login passwords are never stored here.",
            ) { context -> dataStoreFile(context, "backend_subscription_preferences") },
            LocalDataStore(
                "reward_icons",
                "Imported custom reward icon files managed inside Tiny Vow app storage.",
            ) { context -> File(context.filesDir, "reward_icons") },
            LocalDataStore(
                "focus_icons",
                "Imported custom focus type icon files managed inside Tiny Vow app storage.",
            ) { context -> File(context.filesDir, "focus_icons") },
            LocalDataStore(
                "health_connect_step_probe",
                "Debug-only Health Connect step probe snapshots retained for at most seven calendar days.",
            ) { context -> HealthConnectStepProbe.storedHistoryFile(context) },
        )
    }
}

internal data class BackupImportLimits(
    val maxUnzippedBytes: Long = LocalDataManager.MAX_BACKUP_UNZIPPED_BYTES,
    val maxEntryBytes: Long = LocalDataManager.MAX_BACKUP_ENTRY_BYTES,
    val maxEntries: Int = LocalDataManager.MAX_BACKUP_ZIP_ENTRIES,
)

internal fun validateBackupManifestFields(
    manifest: JSONObject,
    expectedPackageName: String,
    currentVersionCode: Int,
) {
    validateBackupManifestFields(
        format = manifest.optString("format"),
        schemaVersion = manifest.optInt("schemaVersion"),
        packageName = manifest.optString("packageName"),
        appVersionCode = manifest.optInt("appVersionCode", Int.MAX_VALUE),
        expectedPackageName = expectedPackageName,
        currentVersionCode = currentVersionCode,
    )
}

internal fun validateBackupManifestFields(
    format: String,
    schemaVersion: Int,
    packageName: String,
    appVersionCode: Int,
    expectedPackageName: String,
    currentVersionCode: Int,
) {
    require(format == LocalDataManager.BACKUP_FORMAT) {
        "Unsupported backup format."
    }
    require(schemaVersion == LocalDataManager.BACKUP_SCHEMA_VERSION) {
        "Unsupported backup schema version."
    }
    require(packageName == expectedPackageName) {
        "Backup package does not match this app."
    }
    require(appVersionCode <= currentVersionCode) {
        "Backup was created by a newer app version."
    }
}

internal fun copyWithLimit(
    input: InputStream,
    output: OutputStream,
    maxBytes: Long,
): Long {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var copied = 0L
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        copied += read.toLong()
        require(copied <= maxBytes) { "Backup file is too large." }
        output.write(buffer, 0, read)
    }
    return copied
}

internal fun extractBackupZipWithLimits(
    zipFile: File,
    destination: File,
    limits: BackupImportLimits = BackupImportLimits(),
) {
    ZipInputStream(FileInputStream(zipFile)).use { zip ->
        var entry = zip.nextEntry
        var entryCount = 0
        var totalBytes = 0L
        while (entry != null) {
            entryCount += 1
            require(entryCount <= limits.maxEntries) {
                "Backup contains too many files."
            }
            if (entry.size >= 0L) {
                require(entry.size <= limits.maxEntryBytes) {
                    "Backup entry is too large."
                }
            }
            val target = File(destination, entry.name)
            val targetPath = target.canonicalPath
            val rootPath = destination.canonicalPath + File.separator
            require(targetPath.startsWith(rootPath) || targetPath == destination.canonicalPath) {
                "Invalid backup entry path."
            }
            if (entry.isDirectory) {
                target.mkdirs()
            } else {
                target.parentFile?.mkdirs()
                FileOutputStream(target).use {
                    val copied =
                        copyZipEntryWithLimit(
                            input = zip,
                            output = it,
                            maxEntryBytes = limits.maxEntryBytes,
                            currentTotalBytes = totalBytes,
                            maxTotalBytes = limits.maxUnzippedBytes,
                        )
                    totalBytes += copied
                }
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }
}

private fun copyZipEntryWithLimit(
    input: InputStream,
    output: OutputStream,
    maxEntryBytes: Long,
    currentTotalBytes: Long,
    maxTotalBytes: Long,
): Long {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var entryBytes = 0L
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        entryBytes += read.toLong()
        require(entryBytes <= maxEntryBytes) { "Backup entry is too large." }
        require(currentTotalBytes + entryBytes <= maxTotalBytes) {
            "Backup is too large after extraction."
        }
        output.write(buffer, 0, read)
    }
    return entryBytes
}
