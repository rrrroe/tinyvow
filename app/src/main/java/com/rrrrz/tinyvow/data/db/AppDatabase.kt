package com.rrrrz.tinyvow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppGroupEntity::class, 
        GroupAppCrossRef::class,
        RedemptionEntity::class,
        BonusTimeEntity::class,
        AchievementEntity::class,
        RedemptionHistoryEntity::class
    ],
    version = 9,
    exportSchema = true // 设置为true，以便配合 AutoMigration 使用
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appGroupDao(): AppGroupDao
    abstract fun crossRefDao(): CrossRefDao
    abstract fun redemptionDao(): RedemptionDao
    abstract fun bonusTimeDao(): BonusTimeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun redemptionHistoryDao(): RedemptionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tinyvow_database"
                )
                // ⚠️ 警告: 这里是导致用户数据丢失的根源！
                // fallbackToDestructiveMigration 会在数据库版本号更新（没有提供迁移逻辑或不匹配）时清空所有表。
                // 建议：如果你需要在正式环境中更新表结构，请使用自动迁移（AutoMigration）并在未来移除这行破坏性迁移代码
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
