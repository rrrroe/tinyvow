package com.rrrrz.tinyvow.data.db

import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val databaseName = "migration-test.db"

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrate9To12_createsArchiveAndLedgerTables() {
        context.deleteDatabase(databaseName)
        createVersion9Database()

        val database = AppDatabase.getDatabase(context, databaseName)
        database.openHelper.writableDatabase

        assertTrue(tableExists("daily_archives"))
        assertTrue(tableExists("daily_group_archives"))
        assertTrue(tableExists("daily_app_archives"))
        assertTrue(tableExists("point_ledger"))
        assertTrue(tableExists("daily_archive_state"))
        assertTrue(indexExists("index_daily_archives_archive_date"))
        assertTrue(indexExists("index_daily_group_archives_archive_date_group_id"))
        assertTrue(indexExists("index_daily_app_archives_archive_date_package_name_scope_key"))
        assertTrue(indexExists("index_daily_app_archives_is_grouped_archive_date"))
        assertTrue(indexExists("index_point_ledger_source_ref_id"))
        assertTrue(tableRowCount("daily_app_archives") == 0)

        database.close()
    }

    @Test
    fun migrate11To12_preservesGroupedAppArchives() {
        context.deleteDatabase(databaseName)
        createVersion11DatabaseWithAppArchive()

        val database = AppDatabase.getDatabase(context, databaseName)
        database.openHelper.writableDatabase

        assertEquals(1, tableRowCount("daily_app_archives"))
        assertEquals("group-a", stringValue("SELECT scope_key FROM daily_app_archives WHERE id = 'app-archive-1'"))
        assertEquals(1, intValue("SELECT is_grouped FROM daily_app_archives WHERE id = 'app-archive-1'"))
        assertEquals("group-a", stringValue("SELECT group_id FROM daily_app_archives WHERE id = 'app-archive-1'"))
        assertTrue(indexExists("index_daily_app_archives_archive_date_package_name_scope_key"))

        database.close()
    }

    private fun createVersion9Database() {
        val dbFile = context.getDatabasePath(databaseName)
        dbFile.parentFile?.mkdirs()
        val sqliteDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `app_groups` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `limit_period` TEXT NOT NULL,
                `limit_minutes` INTEGER NOT NULL,
                `points_per_minute` REAL NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                `last_bonus_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `group_app_cross_ref` (
                `package_name` TEXT NOT NULL,
                `group_id` TEXT NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                PRIMARY KEY(`package_name`, `group_id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_group_app_cross_ref_group_id` ON `group_app_cross_ref` (`group_id`)"
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `redemptions` (
                `id` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `point_cost` INTEGER NOT NULL,
                `reward_type` TEXT NOT NULL,
                `bonus_minutes` INTEGER NOT NULL,
                `is_active` INTEGER NOT NULL,
                `stock` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `bonus_times` (
                `id` TEXT NOT NULL,
                `target_group_id` TEXT NOT NULL,
                `extra_minutes` INTEGER NOT NULL,
                `expiry_time` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `achievements` (
                `id` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `requirement` TEXT NOT NULL,
                `tier` INTEGER NOT NULL,
                `icon_emoji` TEXT NOT NULL,
                `is_unlocked` INTEGER NOT NULL,
                `unlocked_at` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `redemption_history` (
                `id` TEXT NOT NULL,
                `reward_title` TEXT NOT NULL,
                `point_cost` INTEGER NOT NULL,
                `history_type` TEXT NOT NULL,
                `bonus_minutes` INTEGER NOT NULL,
                `target_group_name` TEXT,
                `redeemed_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)"
        )
        sqliteDatabase.execSQL(
            "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f7afc071aeaadadae693e30b380405d0')"
        )
        sqliteDatabase.version = 9
        sqliteDatabase.close()
    }

    private fun createVersion11DatabaseWithAppArchive() {
        val dbFile = context.getDatabasePath(databaseName)
        dbFile.parentFile?.mkdirs()
        val sqliteDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        createVersion9Tables(sqliteDatabase)
        createVersion10ArchiveTables(sqliteDatabase)
        createVersion11DailyAppArchiveTable(sqliteDatabase)
        sqliteDatabase.execSQL(
            """
            INSERT INTO daily_app_archives (
                id,
                archive_date,
                package_name,
                app_label,
                group_id,
                group_name,
                group_type,
                limit_period,
                daily_usage_millis,
                open_count,
                session_count,
                longest_session_millis,
                night_usage_millis,
                earned_points,
                completed,
                hour_00_millis,
                hour_01_millis,
                hour_02_millis,
                hour_03_millis,
                hour_04_millis,
                hour_05_millis,
                hour_06_millis,
                hour_07_millis,
                hour_08_millis,
                hour_09_millis,
                hour_10_millis,
                hour_11_millis,
                hour_12_millis,
                hour_13_millis,
                hour_14_millis,
                hour_15_millis,
                hour_16_millis,
                hour_17_millis,
                hour_18_millis,
                hour_19_millis,
                hour_20_millis,
                hour_21_millis,
                hour_22_millis,
                hour_23_millis,
                created_at,
                updated_at
            ) VALUES (
                'app-archive-1',
                '2026-04-23',
                'demo.app',
                'Demo',
                'group-a',
                'Group A',
                'CONTROL',
                'DAILY',
                120000,
                2,
                1,
                120000,
                0,
                0.0,
                1,
                0,0,0,0,0,0,120000,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                1,
                1
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)"
        )
        sqliteDatabase.execSQL(
            "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'legacy-v11')"
        )
        sqliteDatabase.version = 11
        sqliteDatabase.close()
    }

    private fun createVersion9Tables(sqliteDatabase: SQLiteDatabase) {
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `app_groups` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `limit_period` TEXT NOT NULL,
                `limit_minutes` INTEGER NOT NULL,
                `points_per_minute` REAL NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                `last_bonus_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `group_app_cross_ref` (
                `package_name` TEXT NOT NULL,
                `group_id` TEXT NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                PRIMARY KEY(`package_name`, `group_id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_group_app_cross_ref_group_id` ON `group_app_cross_ref` (`group_id`)"
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `redemptions` (
                `id` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `point_cost` INTEGER NOT NULL,
                `reward_type` TEXT NOT NULL,
                `bonus_minutes` INTEGER NOT NULL,
                `is_active` INTEGER NOT NULL,
                `stock` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `bonus_times` (
                `id` TEXT NOT NULL,
                `target_group_id` TEXT NOT NULL,
                `extra_minutes` INTEGER NOT NULL,
                `expiry_time` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `achievements` (
                `id` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `requirement` TEXT NOT NULL,
                `tier` INTEGER NOT NULL,
                `icon_emoji` TEXT NOT NULL,
                `is_unlocked` INTEGER NOT NULL,
                `unlocked_at` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `redemption_history` (
                `id` TEXT NOT NULL,
                `reward_title` TEXT NOT NULL,
                `point_cost` INTEGER NOT NULL,
                `history_type` TEXT NOT NULL,
                `bonus_minutes` INTEGER NOT NULL,
                `target_group_name` TEXT,
                `redeemed_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }

    private fun createVersion10ArchiveTables(sqliteDatabase: SQLiteDatabase) {
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `daily_archives` (
                `id` TEXT NOT NULL,
                `archive_date` TEXT NOT NULL,
                `day_start_at` INTEGER NOT NULL,
                `day_end_at` INTEGER NOT NULL,
                `control_usage_millis` INTEGER NOT NULL,
                `encourage_usage_millis` INTEGER NOT NULL,
                `total_usage_millis` INTEGER NOT NULL,
                `saved_millis` INTEGER NOT NULL,
                `control_exceeded_group_count` INTEGER NOT NULL,
                `control_completed_group_count` INTEGER NOT NULL,
                `encourage_completed_group_count` INTEGER NOT NULL,
                `points_earned` REAL NOT NULL,
                `points_spent` REAL NOT NULL,
                `points_net` REAL NOT NULL,
                `redemption_count` INTEGER NOT NULL,
                `archive_version` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_archives_archive_date` ON `daily_archives` (`archive_date`)"
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `daily_group_archives` (
                `id` TEXT NOT NULL,
                `archive_date` TEXT NOT NULL,
                `group_id` TEXT NOT NULL,
                `group_name` TEXT NOT NULL,
                `group_type` TEXT NOT NULL,
                `limit_period` TEXT NOT NULL,
                `limit_minutes` INTEGER NOT NULL,
                `bonus_minutes` INTEGER NOT NULL,
                `points_per_minute` REAL NOT NULL,
                `package_count` INTEGER NOT NULL,
                `daily_usage_millis` INTEGER NOT NULL,
                `period_usage_millis_at_close` INTEGER NOT NULL,
                `effective_limit_millis_at_close` INTEGER NOT NULL,
                `remaining_millis_at_close` INTEGER NOT NULL,
                `exceeded_millis_at_close` INTEGER NOT NULL,
                `earned_points` REAL NOT NULL,
                `spent_points` REAL NOT NULL,
                `completed` INTEGER NOT NULL,
                `sort_order` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_group_archives_archive_date_group_id` ON `daily_group_archives` (`archive_date`, `group_id`)"
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_daily_group_archives_archive_date` ON `daily_group_archives` (`archive_date`)"
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_daily_group_archives_group_type_archive_date` ON `daily_group_archives` (`group_type`, `archive_date`)"
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `point_ledger` (
                `id` TEXT NOT NULL,
                `occurred_at` INTEGER NOT NULL,
                `ledger_date` TEXT NOT NULL,
                `entry_type` TEXT NOT NULL,
                `delta_points` REAL NOT NULL,
                `group_id` TEXT,
                `group_name_snapshot` TEXT,
                `reward_id` TEXT,
                `reward_title_snapshot` TEXT,
                `source_ref_id` TEXT,
                `note` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_point_ledger_ledger_date` ON `point_ledger` (`ledger_date`)"
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_point_ledger_occurred_at` ON `point_ledger` (`occurred_at`)"
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_point_ledger_group_id_ledger_date` ON `point_ledger` (`group_id`, `ledger_date`)"
        )
        sqliteDatabase.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_point_ledger_source_ref_id` ON `point_ledger` (`source_ref_id`)"
        )
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `daily_archive_state` (
                `id` TEXT NOT NULL,
                `archive_start_date` TEXT NOT NULL,
                `last_archived_date` TEXT,
                `last_attempted_at` INTEGER NOT NULL,
                `last_succeeded_at` INTEGER NOT NULL,
                `last_error_message` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }

    private fun createVersion11DailyAppArchiveTable(sqliteDatabase: SQLiteDatabase) {
        sqliteDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `daily_app_archives` (
                `id` TEXT NOT NULL,
                `archive_date` TEXT NOT NULL,
                `package_name` TEXT NOT NULL,
                `app_label` TEXT NOT NULL,
                `group_id` TEXT NOT NULL,
                `group_name` TEXT NOT NULL,
                `group_type` TEXT NOT NULL,
                `limit_period` TEXT NOT NULL,
                `daily_usage_millis` INTEGER NOT NULL,
                `open_count` INTEGER NOT NULL,
                `session_count` INTEGER NOT NULL,
                `longest_session_millis` INTEGER NOT NULL,
                `night_usage_millis` INTEGER NOT NULL,
                `earned_points` REAL NOT NULL,
                `completed` INTEGER NOT NULL,
                `hour_00_millis` INTEGER NOT NULL,
                `hour_01_millis` INTEGER NOT NULL,
                `hour_02_millis` INTEGER NOT NULL,
                `hour_03_millis` INTEGER NOT NULL,
                `hour_04_millis` INTEGER NOT NULL,
                `hour_05_millis` INTEGER NOT NULL,
                `hour_06_millis` INTEGER NOT NULL,
                `hour_07_millis` INTEGER NOT NULL,
                `hour_08_millis` INTEGER NOT NULL,
                `hour_09_millis` INTEGER NOT NULL,
                `hour_10_millis` INTEGER NOT NULL,
                `hour_11_millis` INTEGER NOT NULL,
                `hour_12_millis` INTEGER NOT NULL,
                `hour_13_millis` INTEGER NOT NULL,
                `hour_14_millis` INTEGER NOT NULL,
                `hour_15_millis` INTEGER NOT NULL,
                `hour_16_millis` INTEGER NOT NULL,
                `hour_17_millis` INTEGER NOT NULL,
                `hour_18_millis` INTEGER NOT NULL,
                `hour_19_millis` INTEGER NOT NULL,
                `hour_20_millis` INTEGER NOT NULL,
                `hour_21_millis` INTEGER NOT NULL,
                `hour_22_millis` INTEGER NOT NULL,
                `hour_23_millis` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqliteDatabase.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_app_archives_archive_date_package_name_group_id` ON `daily_app_archives` (`archive_date`, `package_name`, `group_id`)"
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_archive_date` ON `daily_app_archives` (`archive_date`)"
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_group_id_archive_date` ON `daily_app_archives` (`group_id`, `archive_date`)"
        )
        sqliteDatabase.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_package_name_archive_date` ON `daily_app_archives` (`package_name`, `archive_date`)"
        )
    }

    private fun tableExists(tableName: String): Boolean {
        val sqliteDatabase = SQLiteDatabase.openDatabase(context.getDatabasePath(databaseName).path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor =
            sqliteDatabase.rawQuery(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
                arrayOf(tableName),
            )
        cursor.use {
            val exists = it.moveToFirst()
            sqliteDatabase.close()
            return exists
        }
    }

    private fun indexExists(indexName: String): Boolean {
        val sqliteDatabase = SQLiteDatabase.openDatabase(context.getDatabasePath(databaseName).path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor =
            sqliteDatabase.rawQuery(
                "SELECT name FROM sqlite_master WHERE type = 'index' AND name = ?",
                arrayOf(indexName),
            )
        cursor.use {
            val exists = it.moveToFirst()
            sqliteDatabase.close()
            return exists
        }
    }

    private fun tableRowCount(tableName: String): Int {
        val sqliteDatabase = SQLiteDatabase.openDatabase(context.getDatabasePath(databaseName).path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = sqliteDatabase.rawQuery("SELECT COUNT(*) FROM `$tableName`", null)
        cursor.use {
            it.moveToFirst()
            val count = it.getInt(0)
            sqliteDatabase.close()
            return count
        }
    }

    private fun stringValue(query: String): String {
        val sqliteDatabase = SQLiteDatabase.openDatabase(context.getDatabasePath(databaseName).path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = sqliteDatabase.rawQuery(query, null)
        cursor.use {
            it.moveToFirst()
            val value = it.getString(0)
            sqliteDatabase.close()
            return value
        }
    }

    private fun intValue(query: String): Int {
        val sqliteDatabase = SQLiteDatabase.openDatabase(context.getDatabasePath(databaseName).path, null, SQLiteDatabase.OPEN_READONLY)
        val cursor = sqliteDatabase.rawQuery(query, null)
        cursor.use {
            it.moveToFirst()
            val value = it.getInt(0)
            sqliteDatabase.close()
            return value
        }
    }
}
