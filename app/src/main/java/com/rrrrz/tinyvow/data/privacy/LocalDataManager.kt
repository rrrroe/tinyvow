package com.rrrrz.tinyvow.data.privacy

import android.content.Context
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.rrrrz.tinyvow.BuildConfig
import com.rrrrz.tinyvow.data.activation.LocalActivationSubscriptionRepository
import com.rrrrz.tinyvow.data.auth.LocalAuthRepository
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.repository.RewardIconStorage
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.special.WeReadApiKeyStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
        val secrets = createSecretBackupJson()
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
                    .put("hasWeReadApiKey", secrets != null)
                    .toString()
                    .toByteArray(Charsets.UTF_8),
            )
            zip.closeEntry()
            secrets?.let {
                zip.putNextEntry(ZipEntry(BACKUP_SECRETS_ENTRY))
                zip.write(it.toString().toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
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
                FileOutputStream(zipFile).use { output -> input.copyTo(output) }
            } ?: error("Unable to open backup file.")
            unzipBackup(zipFile, unzipRoot)
            val manifest = validateBackupManifest(File(unzipRoot, BACKUP_MANIFEST_ENTRY))
            val secrets = readSecretBackupJson(unzipRoot)
            restoreFromUnzippedFiles(unzipRoot)
            restoreSecrets(secrets)
            LocalBackupRestoreResult(
                requiresRestart = true,
                warnings = if (manifest.optBoolean("hasWeReadApiKey") && secrets == null) {
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
            RewardIconStorage.fromContext(context).clearAll()
            WeReadApiKeyStore.deleteStoredKeyMaterial()
            WeReadApiKeyStore.deletePendingRestoredKey(context)
            File(context.cacheDir, "share").deleteRecursively()
        }
    }

    private suspend fun createSecretBackupJson(): JSONObject? {
        val wereadApiKey = WeReadApiKeyStore(context, preferences).get()?.takeIf { it.isNotBlank() }
        if (wereadApiKey == null) return null
        return JSONObject()
            .put("schemaVersion", BACKUP_SCHEMA_VERSION)
            .put("wereadApiKey", wereadApiKey)
    }

    private fun readSecretBackupJson(unzipRoot: File): JSONObject? {
        val file = File(unzipRoot, BACKUP_SECRETS_ENTRY)
        if (!file.isFile) return null
        return JSONObject(file.readText(Charsets.UTF_8))
            .takeIf { it.optInt("schemaVersion") == BACKUP_SCHEMA_VERSION }
    }

    private suspend fun restoreSecrets(secrets: JSONObject?) {
        val wereadApiKey = secrets?.optString("wereadApiKey")?.takeIf { it.isNotBlank() }
        if (wereadApiKey != null) {
            WeReadApiKeyStore.stageRestoredKey(context, wereadApiKey)
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
        val rewardIconsDir = File(context.filesDir, "reward_icons")
        if (rewardIconsDir.isDirectory) {
            rewardIconsDir.walkTopDown()
                .filter { it.isFile }
                .forEach { file ->
                    val relative = file.relativeTo(rewardIconsDir).invariantSeparatorsPath
                    sources += BackupSource("reward_icons/$relative", file)
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
        )
        return names.map { dataStoreFile(context, it) }.filter { it.exists() && it.isFile }
    }

    private fun validateBackupManifest(file: File): JSONObject {
        require(file.isFile) { "Backup manifest is missing." }
        val json = JSONObject(file.readText(Charsets.UTF_8))
        require(json.optString("format") == BACKUP_FORMAT) { "Unsupported backup format." }
        require(json.optInt("schemaVersion") == BACKUP_SCHEMA_VERSION) { "Unsupported backup schema version." }
        return json
    }

    private fun restoreFromUnzippedFiles(unzipRoot: File) {
        database.close()
        AppDatabase.closeActiveInstance()

        restoreDatabaseFiles(unzipRoot)
        restoreDataStoreFiles(unzipRoot)
        restoreRewardIcons(unzipRoot)
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
        val targetDir = File(context.filesDir.parentFile, "datastore").apply { mkdirs() }
        listOf(
            "managed_app_preferences.preferences_pb",
            "auth_preferences.preferences_pb",
            "activation_preferences.preferences_pb",
        ).forEach { name ->
            File(targetDir, name).delete()
            val source = File(unzipRoot, "datastore/$name")
            if (source.isFile) {
                source.copyTo(File(targetDir, name), overwrite = true)
            }
        }
    }

    private fun restoreRewardIcons(unzipRoot: File) {
        val targetDir = File(context.filesDir, "reward_icons")
        targetDir.deleteRecursively()
        val sourceDir = File(unzipRoot, "reward_icons")
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

    private fun unzipBackup(zipFile: File, destination: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
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
                    FileOutputStream(target).use { zip.copyTo(it) }
                }
                zip.closeEntry()
                entry = zip.nextEntry
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

    private companion object {
        const val BACKUP_FORMAT = "tinyvow-local-backup"
        const val BACKUP_SCHEMA_VERSION = 1
        const val BACKUP_MANIFEST_ENTRY = "backup_manifest.json"
        const val BACKUP_SECRETS_ENTRY = "backup_secrets.json"

        fun dataStoreFile(context: Context, name: String): File =
            File(File(context.filesDir.parentFile, "datastore"), "$name.preferences_pb")

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

        val localDataTables = listOf(
            LocalDataTable("app_groups", "User-created control and encouragement groups."),
            LocalDataTable("group_app_cross_ref", "Package names assigned to user-created groups."),
            LocalDataTable("daily_archives", "Daily aggregate usage, saved time, points, and redemption counts."),
            LocalDataTable("daily_group_archives", "Per-group daily usage summaries."),
            LocalDataTable("daily_app_archives", "Per-app package labels, usage duration, sessions, opens, and night usage."),
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
            LocalDataTable("protection_events", "Local Super Mode and guarded-setting change history."),
        )

        val localDataStores = listOf(
            LocalDataStore(
                "managed_app_preferences",
                "DataStore preferences for points, theme, language, permission prompts, profile, debug Pro, Super Mode, and special app key material.",
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
                "reward_icons",
                "Imported custom reward icon files managed inside Tiny Vow app storage.",
            ) { context -> File(context.filesDir, "reward_icons") },
        )
    }
}
