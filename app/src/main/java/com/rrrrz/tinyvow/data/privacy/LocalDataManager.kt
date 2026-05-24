package com.rrrrz.tinyvow.data.privacy

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.rrrrz.tinyvow.data.activation.LocalActivationSubscriptionRepository
import com.rrrrz.tinyvow.data.auth.LocalAuthRepository
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.repository.RewardIconStorage
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.special.WeReadApiKeyStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun clearLocalData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
            preferences.clearAll()
            LocalAuthRepository.clearStoredSession(context)
            LocalActivationSubscriptionRepository.clearStoredActivationData(context)
            RewardIconStorage.fromContext(context).clearAll()
            WeReadApiKeyStore.deleteStoredKeyMaterial()
            File(context.cacheDir, "share").deleteRecursively()
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

    private companion object {
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
            LocalDataTable("streak_shield_pending", "Pending streak shield decisions."),
            LocalDataTable("achievements", "Local achievement progress and unlock state."),
            LocalDataTable("block_events", "Local records of app blocking events."),
            LocalDataTable("daily_archive_state", "Local archive job state."),
            LocalDataTable("special_app_configs", "Local special app data-source settings without API keys."),
            LocalDataTable("special_app_usage_snapshots", "Local cached special app WeRead reading durations and phone foreground durations."),
            LocalDataTable("special_app_point_credits", "Local counters preventing duplicate special app point credits."),
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
