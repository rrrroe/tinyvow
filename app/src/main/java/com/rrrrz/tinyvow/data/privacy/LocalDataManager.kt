package com.rrrrz.tinyvow.data.privacy

import android.content.Context
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.repository.RewardIconStorage
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.sqlite.db.SimpleSQLiteQuery

class LocalDataManager(
    private val context: Context,
    private val database: AppDatabase,
    private val preferences: ManagedAppPreferences,
) {
    suspend fun exportPrivacyReport(): File = withContext(Dispatchers.IO) {
        val snapshot = LocalPrivacySnapshot(
            exportedAtMillis = System.currentTimeMillis(),
            tableSummaries = localDataTables.map { table ->
                LocalDataTableSummary(
                    tableName = table.name,
                    description = table.description,
                    rowCount = countRows(table.name),
                )
            },
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
            RewardIconStorage.fromContext(context).clearAll()
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

    private companion object {
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
    }
}
