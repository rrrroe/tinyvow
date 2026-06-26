package com.rrrrz.tinyvow.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
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

    val MIGRATION_14_15 =
        object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `redemptions` ADD COLUMN `builtin_key` TEXT")
                db.execSQL("ALTER TABLE `redemption_history` ADD COLUMN `reward_builtin_key` TEXT")
                db.execSQL("ALTER TABLE `point_ledger` ADD COLUMN `message_key` TEXT")
                db.execSQL("ALTER TABLE `point_ledger` ADD COLUMN `message_args_json` TEXT")
                db.execSQL(
                    "UPDATE `redemptions` SET `builtin_key` = 'reward_time_pack_30' WHERE `title` = '30\u5206\u949f \u4e34\u65f6\u7eed\u547d\u5361' AND `bonus_minutes` = 30"
                )
                db.execSQL(
                    "UPDATE `redemptions` SET `builtin_key` = 'reward_time_pack_60' WHERE `title` = '1\u5c0f\u65f6 \u81ea\u7531\u51b2\u6d6a\u5361' AND `bonus_minutes` = 60"
                )
                db.execSQL(
                    "UPDATE `redemptions` SET `builtin_key` = 'reward_offline_treat' WHERE `title` = '\u5927\u5feb\u6735\u9890 (\u7ebf\u4e0b\u5956\u52b1)'"
                )
                db.execSQL(
                    "UPDATE `redemption_history` SET `reward_builtin_key` = 'reward_time_pack_30' WHERE `reward_title` = '30\u5206\u949f \u4e34\u65f6\u7eed\u547d\u5361' AND `bonus_minutes` = 30"
                )
                db.execSQL(
                    "UPDATE `redemption_history` SET `reward_builtin_key` = 'reward_time_pack_60' WHERE `reward_title` = '1\u5c0f\u65f6 \u81ea\u7531\u51b2\u6d6a\u5361' AND `bonus_minutes` = 60"
                )
                db.execSQL(
                    "UPDATE `redemption_history` SET `reward_builtin_key` = 'reward_offline_treat' WHERE `reward_title` = '\u5927\u5feb\u6735\u9890 (\u7ebf\u4e0b\u5956\u52b1)'"
                )
            }
        }

    val MIGRATION_15_16 =
        object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM `redemptions` WHERE `builtin_key` IS NOT NULL")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_redemptions_builtin_key` ON `redemptions` (`builtin_key`)"
                )
            }
        }

    val MIGRATION_16_17 =
        object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `redemptions` ADD COLUMN `payload_json` TEXT")
                db.execSQL("ALTER TABLE `redemption_history` ADD COLUMN `payload_json` TEXT")
                db.execSQL("ALTER TABLE `daily_group_archives` ADD COLUMN `reward_exempted` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `daily_group_archives` ADD COLUMN `reward_exempt_type` TEXT")
                db.execSQL("ALTER TABLE `daily_group_archives` ADD COLUMN `reward_bonus_points` REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `daily_group_archives` ADD COLUMN `reward_effect_snapshot_json` TEXT")
                db.execSQL("UPDATE `redemptions` SET `reward_type` = 'TIME_ADD' WHERE `reward_type` = 'TIME_PACK'")
                db.execSQL("UPDATE `redemption_history` SET `history_type` = 'TIME_ADD' WHERE `history_type` = 'TIME_PACK'")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reward_inventory` (
                        `id` TEXT NOT NULL,
                        `reward_id` TEXT NOT NULL,
                        `reward_builtin_key` TEXT,
                        `quantity` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_reward_inventory_reward_id` ON `reward_inventory` (`reward_id`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_reward_inventory_reward_builtin_key` ON `reward_inventory` (`reward_builtin_key`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `active_reward_effects` (
                        `id` TEXT NOT NULL,
                        `effect_type` TEXT NOT NULL,
                        `source_reward_id` TEXT NOT NULL,
                        `source_builtin_key` TEXT,
                        `target_group_id` TEXT,
                        `target_group_type` TEXT,
                        `start_at` INTEGER NOT NULL,
                        `expire_at` INTEGER NOT NULL,
                        `period_start_date` TEXT,
                        `period_end_date` TEXT,
                        `status` TEXT NOT NULL,
                        `payload_json` TEXT,
                        `created_at` INTEGER NOT NULL,
                        `consumed_at` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_active_reward_effects_target_group_id_status_expire_at` ON `active_reward_effects` (`target_group_id`, `status`, `expire_at`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_active_reward_effects_effect_type_status_expire_at` ON `active_reward_effects` (`effect_type`, `status`, `expire_at`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_active_reward_effects_source_reward_id` ON `active_reward_effects` (`source_reward_id`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `streak_shield_pending` (
                        `id` TEXT NOT NULL,
                        `archive_date` TEXT NOT NULL,
                        `shield_target` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `resolved_at` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_streak_shield_pending_archive_date_shield_target` ON `streak_shield_pending` (`archive_date`, `shield_target`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_streak_shield_pending_status_created_at` ON `streak_shield_pending` (`status`, `created_at`)"
                )
            }
        }

    val MIGRATION_17_18 =
        object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reward_use_history` (
                        `id` TEXT NOT NULL,
                        `reward_id` TEXT NOT NULL,
                        `reward_title` TEXT NOT NULL,
                        `reward_type` TEXT NOT NULL,
                        `reward_builtin_key` TEXT,
                        `target_group_name` TEXT,
                        `payload_json` TEXT,
                        `used_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_reward_use_history_used_at` ON `reward_use_history` (`used_at`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_reward_use_history_reward_id` ON `reward_use_history` (`reward_id`)"
                )

                db.execSQL("UPDATE `redemptions` SET `reward_type` = 'DOUBLE_POINTS_DAY' WHERE `reward_type` = 'BONUS_TARGET'")
                db.execSQL("UPDATE `redemption_history` SET `history_type` = 'DOUBLE_POINTS_DAY' WHERE `history_type` = 'BONUS_TARGET'")
                db.execSQL("UPDATE `active_reward_effects` SET `effect_type` = 'DOUBLE_POINTS_DAY' WHERE `effect_type` = 'BONUS_TARGET'")
                db.execSQL(
                    """
                    UPDATE `active_reward_effects`
                    SET `source_builtin_key` = 'reward_double_points_day'
                    WHERE `source_builtin_key` IN ('reward_bonus_target_30', 'reward_bonus_target_50')
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    UPDATE `redemptions`
                    SET
                        `builtin_key` = 'reward_double_points_day',
                        `point_cost` = 10,
                        `bonus_minutes` = 0,
                        `updated_at` = `updated_at`
                    WHERE `builtin_key` = 'reward_bonus_target_30'
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE `redemptions`
                    SET
                        `builtin_key` = 'reward_double_points_day',
                        `point_cost` = 10,
                        `bonus_minutes` = 0,
                        `updated_at` = `updated_at`
                    WHERE `builtin_key` = 'reward_bonus_target_50'
                        AND NOT EXISTS (
                            SELECT 1 FROM `redemptions` WHERE `builtin_key` = 'reward_double_points_day'
                        )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT OR REPLACE INTO `reward_inventory` (
                        `id`,
                        `reward_id`,
                        `reward_builtin_key`,
                        `quantity`,
                        `created_at`,
                        `updated_at`
                    )
                    SELECT
                        COALESCE(
                            (SELECT `id` FROM `reward_inventory` WHERE `reward_id` = keep_reward.`id` LIMIT 1),
                            (SELECT `id` FROM `reward_inventory` WHERE `reward_id` = duplicate_reward.`id` LIMIT 1),
                            'inventory:' || keep_reward.`id`
                        ),
                        keep_reward.`id`,
                        'reward_double_points_day',
                        COALESCE(
                            (SELECT SUM(`quantity`) FROM `reward_inventory` WHERE `reward_id` IN (keep_reward.`id`, duplicate_reward.`id`)),
                            0
                        ),
                        COALESCE(
                            (SELECT MIN(`created_at`) FROM `reward_inventory` WHERE `reward_id` IN (keep_reward.`id`, duplicate_reward.`id`)),
                            CAST(strftime('%s','now') AS INTEGER) * 1000
                        ),
                        COALESCE(
                            (SELECT MAX(`updated_at`) FROM `reward_inventory` WHERE `reward_id` IN (keep_reward.`id`, duplicate_reward.`id`)),
                            CAST(strftime('%s','now') AS INTEGER) * 1000
                        )
                    FROM `redemptions` AS keep_reward, `redemptions` AS duplicate_reward
                    WHERE keep_reward.`builtin_key` = 'reward_double_points_day'
                        AND duplicate_reward.`builtin_key` = 'reward_bonus_target_50'
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE `reward_inventory`
                    SET `reward_builtin_key` = 'reward_double_points_day'
                    WHERE `reward_id` IN (
                        SELECT `id` FROM `redemptions`
                        WHERE `builtin_key` = 'reward_double_points_day'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE `active_reward_effects`
                    SET
                        `source_reward_id` = (
                            SELECT `id` FROM `redemptions` WHERE `builtin_key` = 'reward_double_points_day' LIMIT 1
                        ),
                        `source_builtin_key` = 'reward_double_points_day'
                    WHERE `source_reward_id` IN (
                        SELECT `id` FROM `redemptions` WHERE `builtin_key` = 'reward_bonus_target_50'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE `redemption_history`
                    SET `reward_builtin_key` = 'reward_double_points_day'
                    WHERE `reward_builtin_key` IN ('reward_bonus_target_30', 'reward_bonus_target_50')
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE `point_ledger`
                    SET `reward_id` = (
                        SELECT `id` FROM `redemptions` WHERE `builtin_key` = 'reward_double_points_day' LIMIT 1
                    )
                    WHERE `reward_id` IN (
                        SELECT `id` FROM `redemptions` WHERE `builtin_key` = 'reward_bonus_target_50'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    DELETE FROM `reward_inventory`
                    WHERE `reward_id` IN (
                        SELECT `id` FROM `redemptions` WHERE `builtin_key` = 'reward_bonus_target_50'
                    )
                    """.trimIndent()
                )
                db.execSQL("DELETE FROM `redemptions` WHERE `builtin_key` = 'reward_bonus_target_50'")
            }
        }

    val MIGRATION_18_19 =
        object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `redemptions` ADD COLUMN `icon_source` TEXT")
                db.execSQL("ALTER TABLE `redemptions` ADD COLUMN `icon_value` TEXT")
            }
        }

    val MIGRATION_19_20 =
        object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `daily_app_archives` ADD COLUMN `usage_source` TEXT")
                db.execSQL("ALTER TABLE `daily_app_archives` ADD COLUMN `usage_source_synced_at` INTEGER")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `special_app_configs` (
                        `provider` TEXT NOT NULL,
                        `package_name` TEXT NOT NULL,
                        `enabled_for_control` INTEGER NOT NULL,
                        `enabled_for_encourage` INTEGER NOT NULL,
                        `sync_enabled` INTEGER NOT NULL,
                        `last_sync_at` INTEGER NOT NULL,
                        `last_success_at` INTEGER NOT NULL,
                        `last_error` TEXT,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`provider`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `special_app_usage_snapshots` (
                        `id` TEXT NOT NULL,
                        `provider` TEXT NOT NULL,
                        `package_name` TEXT NOT NULL,
                        `usage_date` TEXT NOT NULL,
                        `usage_millis` INTEGER NOT NULL,
                        `source_synced_at` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_special_app_usage_snapshots_provider_usage_date` ON `special_app_usage_snapshots` (`provider`, `usage_date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_special_app_usage_snapshots_package_name_usage_date` ON `special_app_usage_snapshots` (`package_name`, `usage_date`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `special_app_point_credits` (
                        `id` TEXT NOT NULL,
                        `provider` TEXT NOT NULL,
                        `group_id` TEXT NOT NULL,
                        `credit_date` TEXT NOT NULL,
                        `credited_usage_millis` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_special_app_point_credits_provider_group_id_credit_date` ON `special_app_point_credits` (`provider`, `group_id`, `credit_date`)"
                )
            }
        }

    val MIGRATION_20_21 =
        object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `special_app_configs` ADD COLUMN `usage_preference` TEXT NOT NULL DEFAULT 'READING_FIRST'"
                )
                db.execSQL(
                    "ALTER TABLE `special_app_usage_snapshots` ADD COLUMN `reading_bucket_available` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `special_app_usage_snapshots` ADD COLUMN `phone_usage_millis` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `special_app_usage_snapshots` ADD COLUMN `phone_collected_at` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    """
                    UPDATE `special_app_usage_snapshots`
                    SET `reading_bucket_available` = CASE WHEN `usage_millis` > 0 THEN 1 ELSE 0 END
                    """.trimIndent()
                )
            }
        }

    val MIGRATION_21_22 =
        object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `active_reward_effects` ADD COLUMN `source_inventory_id` TEXT")
                db.execSQL("ALTER TABLE `active_reward_effects` ADD COLUMN `target_group_name_snapshot` TEXT")
                db.execSQL("ALTER TABLE `active_reward_effects` ADD COLUMN `effect_value_json` TEXT")
                db.execSQL("ALTER TABLE `active_reward_effects` ADD COLUMN `benefit_json` TEXT")
                db.execSQL("ALTER TABLE `active_reward_effects` ADD COLUMN `confirm_deadline_at` INTEGER")
                db.execSQL("ALTER TABLE `active_reward_effects` ADD COLUMN `confirmed_at` INTEGER")
                db.execSQL("ALTER TABLE `active_reward_effects` ADD COLUMN `canceled_at` INTEGER")
                db.execSQL(
                    """
                    UPDATE `active_reward_effects`
                    SET `confirmed_at` = `created_at`
                    WHERE `status` = 'ACTIVE'
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reward_effect_benefits` (
                        `id` TEXT NOT NULL,
                        `effect_id` TEXT NOT NULL,
                        `reward_id` TEXT NOT NULL,
                        `reward_builtin_key` TEXT,
                        `reward_type` TEXT NOT NULL,
                        `archive_date` TEXT NOT NULL,
                        `target_group_id` TEXT,
                        `target_group_name_snapshot` TEXT,
                        `benefit_type` TEXT NOT NULL,
                        `benefit_minutes` INTEGER NOT NULL,
                        `benefit_points` REAL NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_reward_effect_benefits_effect_id_archive_date` ON `reward_effect_benefits` (`effect_id`, `archive_date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_reward_effect_benefits_archive_date` ON `reward_effect_benefits` (`archive_date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_reward_effect_benefits_reward_id` ON `reward_effect_benefits` (`reward_id`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_reward_effect_benefits_target_group_id_archive_date` ON `reward_effect_benefits` (`target_group_id`, `archive_date`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `protection_events` (
                        `id` TEXT NOT NULL,
                        `event_type` TEXT NOT NULL,
                        `event_date` TEXT NOT NULL,
                        `occurred_at` INTEGER NOT NULL,
                        `title_key` TEXT NOT NULL,
                        `message_key` TEXT NOT NULL,
                        `message_args_json` TEXT,
                        `target_id` TEXT,
                        `target_label` TEXT,
                        `before_json` TEXT,
                        `after_json` TEXT,
                        `within_window` INTEGER,
                        `protection_enabled` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_protection_events_event_date` ON `protection_events` (`event_date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_protection_events_event_type_event_date` ON `protection_events` (`event_type`, `event_date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_protection_events_occurred_at` ON `protection_events` (`occurred_at`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_protection_events_target_id` ON `protection_events` (`target_id`)"
                )
            }
        }

    val MIGRATION_22_23 =
        object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_checkins` (
                        `id` TEXT NOT NULL,
                        `checkin_date` TEXT NOT NULL,
                        `checked_in_at` INTEGER NOT NULL,
                        `reward_builtin_key` TEXT NOT NULL,
                        `reward_inventory_id` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_checkins_checkin_date` ON `daily_checkins` (`checkin_date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_daily_checkins_checked_in_at` ON `daily_checkins` (`checked_in_at`)"
                )
            }
        }

    val MIGRATION_23_24 =
        object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_app_time_slice_archives` (
                        `archive_date` TEXT NOT NULL,
                        `slice_index` INTEGER NOT NULL,
                        `package_name` TEXT NOT NULL,
                        `usage_millis` INTEGER NOT NULL,
                        PRIMARY KEY(`archive_date`, `slice_index`, `package_name`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_daily_app_time_slice_archives_archive_date` ON `daily_app_time_slice_archives` (`archive_date`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_daily_app_time_slice_archives_archive_date_slice_index` ON `daily_app_time_slice_archives` (`archive_date`, `slice_index`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_daily_app_time_slice_archives_archive_date_package_name` ON `daily_app_time_slice_archives` (`archive_date`, `package_name`)"
                )
            }
        }

    val MIGRATION_24_25 =
        object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `app_groups` ADD COLUMN `encourage_metric` TEXT NOT NULL DEFAULT 'APP_USAGE'")
                db.execSQL("ALTER TABLE `app_groups` ADD COLUMN `step_target` INTEGER NOT NULL DEFAULT 8000")
                db.execSQL("ALTER TABLE `app_groups` ADD COLUMN `points_per_step` REAL NOT NULL DEFAULT 0.01")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `step_days` (
                        `id` TEXT NOT NULL,
                        `step_date` TEXT NOT NULL,
                        `steps` INTEGER NOT NULL,
                        `sensor_base_steps` INTEGER NOT NULL,
                        `last_sensor_steps` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_step_days_step_date` ON `step_days` (`step_date`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `step_point_credits` (
                        `id` TEXT NOT NULL,
                        `group_id` TEXT NOT NULL,
                        `credit_date` TEXT NOT NULL,
                        `credited_steps` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_step_point_credits_group_id_credit_date` ON `step_point_credits` (`group_id`, `credit_date`)"
                )
            }
        }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_13_14,
        MIGRATION_14_15,
        MIGRATION_15_16,
        MIGRATION_16_17,
        MIGRATION_17_18,
        MIGRATION_18_19,
        MIGRATION_19_20,
        MIGRATION_20_21,
        MIGRATION_21_22,
        MIGRATION_22_23,
        MIGRATION_23_24,
        MIGRATION_24_25,
    )
}
