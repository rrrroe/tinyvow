package com.rrrrz.tinyvow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rrrrz.tinyvow.data.db.migration.AppDatabaseMigrations

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
        RewardInventoryEntity::class,
        ActiveRewardEffectEntity::class,
        RewardEffectBenefitEntity::class,
        StreakShieldPendingEntity::class,
        RewardUseHistoryEntity::class,
        SpecialAppConfigEntity::class,
        SpecialAppUsageSnapshotEntity::class,
        SpecialAppPointCreditEntity::class,
        ProtectionEventEntity::class,
        DailyCheckInEntity::class,
        DailyAppTimeSliceArchiveEntity::class,
        StepDayEntity::class,
        StepPointCreditEntity::class,
        MediaAppConfigEntity::class,
        MediaAppPlaybackDayEntity::class,
        MediaAppPlaybackSegmentEntity::class,
        LockScreenTimerAppConfigEntity::class,
        LockScreenTimerAppDayEntity::class,
        LockScreenTimerAppSegmentEntity::class,
        OfflineFocusCategoryEntity::class,
        OfflineFocusSessionEntity::class,
    ],
    version = 31,
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
    abstract fun rewardInventoryDao(): RewardInventoryDao
    abstract fun activeRewardEffectDao(): ActiveRewardEffectDao
    abstract fun rewardEffectBenefitDao(): RewardEffectBenefitDao
    abstract fun streakShieldPendingDao(): StreakShieldPendingDao
    abstract fun rewardUseHistoryDao(): RewardUseHistoryDao
    abstract fun specialAppConfigDao(): SpecialAppConfigDao
    abstract fun specialAppUsageSnapshotDao(): SpecialAppUsageSnapshotDao
    abstract fun specialAppPointCreditDao(): SpecialAppPointCreditDao
    abstract fun protectionEventDao(): ProtectionEventDao
    abstract fun dailyCheckInDao(): DailyCheckInDao
    abstract fun dailyAppTimeSliceArchiveDao(): DailyAppTimeSliceArchiveDao
    abstract fun stepDayDao(): StepDayDao
    abstract fun stepPointCreditDao(): StepPointCreditDao
    abstract fun mediaAppConfigDao(): MediaAppConfigDao
    abstract fun mediaAppPlaybackDayDao(): MediaAppPlaybackDayDao
    abstract fun mediaAppPlaybackSegmentDao(): MediaAppPlaybackSegmentDao
    abstract fun lockScreenTimerAppConfigDao(): LockScreenTimerAppConfigDao
    abstract fun lockScreenTimerAppDayDao(): LockScreenTimerAppDayDao
    abstract fun lockScreenTimerAppSegmentDao(): LockScreenTimerAppSegmentDao
    abstract fun offlineFocusCategoryDao(): OfflineFocusCategoryDao
    abstract fun offlineFocusSessionDao(): OfflineFocusSessionDao

    companion object {
        const val DEFAULT_DATABASE_NAME = "tinyvow_database"

        val MIGRATION_9_10 = AppDatabaseMigrations.MIGRATION_9_10
        val MIGRATION_10_11 = AppDatabaseMigrations.MIGRATION_10_11
        val MIGRATION_11_12 = AppDatabaseMigrations.MIGRATION_11_12
        val MIGRATION_12_13 = AppDatabaseMigrations.MIGRATION_12_13
        val MIGRATION_13_14 = AppDatabaseMigrations.MIGRATION_13_14
        val MIGRATION_14_15 = AppDatabaseMigrations.MIGRATION_14_15
        val MIGRATION_15_16 = AppDatabaseMigrations.MIGRATION_15_16
        val MIGRATION_16_17 = AppDatabaseMigrations.MIGRATION_16_17
        val MIGRATION_17_18 = AppDatabaseMigrations.MIGRATION_17_18
        val MIGRATION_18_19 = AppDatabaseMigrations.MIGRATION_18_19
        val MIGRATION_19_20 = AppDatabaseMigrations.MIGRATION_19_20
        val MIGRATION_20_21 = AppDatabaseMigrations.MIGRATION_20_21
        val MIGRATION_21_22 = AppDatabaseMigrations.MIGRATION_21_22
        val MIGRATION_22_23 = AppDatabaseMigrations.MIGRATION_22_23
        val MIGRATION_23_24 = AppDatabaseMigrations.MIGRATION_23_24
        val MIGRATION_24_25 = AppDatabaseMigrations.MIGRATION_24_25
        val MIGRATION_25_26 = AppDatabaseMigrations.MIGRATION_25_26
        val MIGRATION_26_27 = AppDatabaseMigrations.MIGRATION_26_27
        val MIGRATION_27_28 = AppDatabaseMigrations.MIGRATION_27_28
        val MIGRATION_28_29 = AppDatabaseMigrations.MIGRATION_28_29
        val MIGRATION_29_30 = AppDatabaseMigrations.MIGRATION_29_30
        val MIGRATION_30_31 = AppDatabaseMigrations.MIGRATION_30_31

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
                        .addMigrations(*AppDatabaseMigrations.ALL)
                        .build()
                INSTANCE = instance
                instanceDatabaseName = databaseName
                instance
            }
        }

        fun closeActiveInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                instanceDatabaseName = null
            }
        }
    }
}

