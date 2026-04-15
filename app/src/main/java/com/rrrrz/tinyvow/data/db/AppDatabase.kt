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
    exportSchema = false
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
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
