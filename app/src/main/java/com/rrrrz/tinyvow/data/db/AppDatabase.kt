package com.rrrrz.tinyvow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AppGroupEntity::class,
        GroupAppCrossRef::class,
        RedemptionEntity::class,
        BonusTimeEntity::class,
        AchievementEntity::class,
        RedemptionHistoryEntity::class,
        DailyArchiveEntity::class,
        DailyGroupArchiveEntity::class,
        DailyAppArchiveEntity::class,
        PointLedgerEntity::class,
        DailyArchiveStateEntity::class,
        BlockEventEntity::class,
    ],
    version = 14,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appGroupDao(): AppGroupDao
    abstract fun crossRefDao(): CrossRefDao
    abstract fun redemptionDao(): RedemptionDao
    abstract fun bonusTimeDao(): BonusTimeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun redemptionHistoryDao(): RedemptionHistoryDao
    abstract fun dailyArchiveDao(): DailyArchiveDao
    abstract fun dailyGroupArchiveDao(): DailyGroupArchiveDao
    abstract fun dailyAppArchiveDao(): DailyAppArchiveDao
    abstract fun pointLedgerDao(): PointLedgerDao
    abstract fun dailyArchiveStateDao(): DailyArchiveStateDao
    abstract fun blockEventDao(): BlockEventDao

    companion object {
        private const val DEFAULT_DATABASE_NAME = "tinyvow_database"

        val MIGRATION_9_10 =
            object : Migration(9, 10) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
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
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_archives_archive_date` ON `daily_archives` (`archive_date`)"
                    )
                    db.execSQL(
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
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_group_archives_archive_date_group_id` ON `daily_group_archives` (`archive_date`, `group_id`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_daily_group_archives_archive_date` ON `daily_group_archives` (`archive_date`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_daily_group_archives_group_type_archive_date` ON `daily_group_archives` (`group_type`, `archive_date`)"
                    )
                    db.execSQL(
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
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_point_ledger_ledger_date` ON `point_ledger` (`ledger_date`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_point_ledger_occurred_at` ON `point_ledger` (`occurred_at`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_point_ledger_group_id_ledger_date` ON `point_ledger` (`group_id`, `ledger_date`)"
                    )
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_point_ledger_source_ref_id` ON `point_ledger` (`source_ref_id`)"
                    )
                    db.execSQL(
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
            }

        val MIGRATION_10_11 =
            object : Migration(10, 11) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
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
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_app_archives_archive_date_package_name_group_id` ON `daily_app_archives` (`archive_date`, `package_name`, `group_id`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_archive_date` ON `daily_app_archives` (`archive_date`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_group_id_archive_date` ON `daily_app_archives` (`group_id`, `archive_date`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_package_name_archive_date` ON `daily_app_archives` (`package_name`, `archive_date`)"
                    )
                }
            }

        val MIGRATION_11_12 =
            object : Migration(11, 12) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE `daily_app_archives` RENAME TO `daily_app_archives_v11`")
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `daily_app_archives` (
                            `id` TEXT NOT NULL,
                            `archive_date` TEXT NOT NULL,
                            `package_name` TEXT NOT NULL,
                            `app_label` TEXT NOT NULL,
                            `scope_key` TEXT NOT NULL,
                            `is_grouped` INTEGER NOT NULL,
                            `group_id` TEXT,
                            `group_name` TEXT,
                            `group_type` TEXT,
                            `limit_period` TEXT,
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
                    db.execSQL(
                        """
                        INSERT INTO `daily_app_archives` (
                            `id`,
                            `archive_date`,
                            `package_name`,
                            `app_label`,
                            `scope_key`,
                            `is_grouped`,
                            `group_id`,
                            `group_name`,
                            `group_type`,
                            `limit_period`,
                            `daily_usage_millis`,
                            `open_count`,
                            `session_count`,
                            `longest_session_millis`,
                            `night_usage_millis`,
                            `earned_points`,
                            `completed`,
                            `hour_00_millis`,
                            `hour_01_millis`,
                            `hour_02_millis`,
                            `hour_03_millis`,
                            `hour_04_millis`,
                            `hour_05_millis`,
                            `hour_06_millis`,
                            `hour_07_millis`,
                            `hour_08_millis`,
                            `hour_09_millis`,
                            `hour_10_millis`,
                            `hour_11_millis`,
                            `hour_12_millis`,
                            `hour_13_millis`,
                            `hour_14_millis`,
                            `hour_15_millis`,
                            `hour_16_millis`,
                            `hour_17_millis`,
                            `hour_18_millis`,
                            `hour_19_millis`,
                            `hour_20_millis`,
                            `hour_21_millis`,
                            `hour_22_millis`,
                            `hour_23_millis`,
                            `created_at`,
                            `updated_at`
                        )
                        SELECT
                            `id`,
                            `archive_date`,
                            `package_name`,
                            `app_label`,
                            `group_id`,
                            1,
                            `group_id`,
                            `group_name`,
                            `group_type`,
                            `limit_period`,
                            `daily_usage_millis`,
                            `open_count`,
                            `session_count`,
                            `longest_session_millis`,
                            `night_usage_millis`,
                            `earned_points`,
                            `completed`,
                            `hour_00_millis`,
                            `hour_01_millis`,
                            `hour_02_millis`,
                            `hour_03_millis`,
                            `hour_04_millis`,
                            `hour_05_millis`,
                            `hour_06_millis`,
                            `hour_07_millis`,
                            `hour_08_millis`,
                            `hour_09_millis`,
                            `hour_10_millis`,
                            `hour_11_millis`,
                            `hour_12_millis`,
                            `hour_13_millis`,
                            `hour_14_millis`,
                            `hour_15_millis`,
                            `hour_16_millis`,
                            `hour_17_millis`,
                            `hour_18_millis`,
                            `hour_19_millis`,
                            `hour_20_millis`,
                            `hour_21_millis`,
                            `hour_22_millis`,
                            `hour_23_millis`,
                            `created_at`,
                            `updated_at`
                        FROM `daily_app_archives_v11`
                        """.trimIndent()
                    )
                    db.execSQL("DROP TABLE `daily_app_archives_v11`")
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_app_archives_archive_date_package_name_scope_key` ON `daily_app_archives` (`archive_date`, `package_name`, `scope_key`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_archive_date` ON `daily_app_archives` (`archive_date`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_group_id_archive_date` ON `daily_app_archives` (`group_id`, `archive_date`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_package_name_archive_date` ON `daily_app_archives` (`package_name`, `archive_date`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_daily_app_archives_is_grouped_archive_date` ON `daily_app_archives` (`is_grouped`, `archive_date`)"
                    )
                }
            }

        val MIGRATION_12_13 =
            object : Migration(12, 13) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE `daily_archives` ADD COLUMN `control_block_event_count` INTEGER NOT NULL DEFAULT 0"
                    )
                    db.execSQL(
                        "ALTER TABLE `daily_group_archives` ADD COLUMN `block_event_count` INTEGER NOT NULL DEFAULT 0"
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `block_events` (
                            `id` TEXT NOT NULL,
                            `event_date` TEXT NOT NULL,
                            `occurred_at` INTEGER NOT NULL,
                            `package_name` TEXT NOT NULL,
                            `group_id` TEXT NOT NULL,
                            `group_name_snapshot` TEXT NOT NULL,
                            `exceeded_millis` INTEGER NOT NULL,
                            `created_at` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_block_events_event_date` ON `block_events` (`event_date`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_block_events_group_id_event_date` ON `block_events` (`group_id`, `event_date`)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_block_events_package_name_event_date` ON `block_events` (`package_name`, `event_date`)"
                    )
                }
            }

        val MIGRATION_13_14 =
            object : Migration(13, 14) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE `app_groups` ADD COLUMN `sort_order` INTEGER NOT NULL DEFAULT 0"
                    )
                    db.execSQL(
                        """
                        UPDATE `app_groups`
                        SET `sort_order` = (
                            SELECT COUNT(*)
                            FROM `app_groups` AS newer
                            WHERE newer.`type` = `app_groups`.`type`
                                AND newer.`is_deleted` = 0
                                AND (
                                    newer.`created_at` > `app_groups`.`created_at`
                                    OR (
                                        newer.`created_at` = `app_groups`.`created_at`
                                        AND newer.`id` < `app_groups`.`id`
                                    )
                                )
                        )
                        WHERE `is_deleted` = 0
                        """.trimIndent()
                    )
                }
            }

        @Volatile
        private var INSTANCE: AppDatabase? = null
        @Volatile
        private var instanceDatabaseName: String? = null

        fun getDatabase(
            context: Context,
            databaseName: String = DEFAULT_DATABASE_NAME,
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val existing = INSTANCE
                if (existing != null && existing.isOpen && instanceDatabaseName == databaseName) {
                    return existing
                }
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        databaseName,
                    )
                        .addMigrations(
                            MIGRATION_9_10,
                            MIGRATION_10_11,
                            MIGRATION_11_12,
                            MIGRATION_12_13,
                            MIGRATION_13_14,
                        )
                        .build()
                INSTANCE = instance
                instanceDatabaseName = databaseName
                instance
            }
        }
    }
}
