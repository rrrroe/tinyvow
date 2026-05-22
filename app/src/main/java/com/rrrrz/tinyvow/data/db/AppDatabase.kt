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
        StreakShieldPendingEntity::class,
        RewardUseHistoryEntity::class,
        SpecialAppConfigEntity::class,
        SpecialAppUsageSnapshotEntity::class,
        SpecialAppPointCreditEntity::class,
    ],
    version = 21,
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
    abstract fun streakShieldPendingDao(): StreakShieldPendingDao
    abstract fun rewardUseHistoryDao(): RewardUseHistoryDao
    abstract fun specialAppConfigDao(): SpecialAppConfigDao
    abstract fun specialAppUsageSnapshotDao(): SpecialAppUsageSnapshotDao
    abstract fun specialAppPointCreditDao(): SpecialAppPointCreditDao

    companion object {
        private const val DEFAULT_DATABASE_NAME = "tinyvow_database"

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
    }
}

