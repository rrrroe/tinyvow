package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialAppConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: SpecialAppConfigEntity)

    @Query("SELECT * FROM special_app_configs WHERE provider = :provider LIMIT 1")
    suspend fun get(provider: SpecialAppProvider): SpecialAppConfigEntity?

    @Query("SELECT * FROM special_app_configs WHERE provider = :provider LIMIT 1")
    fun observe(provider: SpecialAppProvider): Flow<SpecialAppConfigEntity?>

    @Query(
        """
        UPDATE special_app_configs
        SET last_sync_at = :lastSyncAt,
            last_success_at = :lastSuccessAt,
            last_error = :lastError,
            updated_at = :updatedAt
        WHERE provider = :provider
        """
    )
    suspend fun updateSyncState(
        provider: SpecialAppProvider,
        lastSyncAt: Long,
        lastSuccessAt: Long,
        lastError: String?,
        updatedAt: Long,
    )
}

@Dao
interface SpecialAppUsageSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(snapshots: List<SpecialAppUsageSnapshotEntity>)

    @Query(
        """
        SELECT *
        FROM special_app_usage_snapshots
        WHERE provider = :provider AND usage_date = :date
        LIMIT 1
        """
    )
    suspend fun getByDate(provider: SpecialAppProvider, date: String): SpecialAppUsageSnapshotEntity?

    @Query(
        """
        SELECT *
        FROM special_app_usage_snapshots
        WHERE provider = :provider AND usage_date BETWEEN :from AND :to
        ORDER BY usage_date ASC
        """
    )
    suspend fun getByDateRange(
        provider: SpecialAppProvider,
        from: String,
        to: String,
    ): List<SpecialAppUsageSnapshotEntity>

    @Query(
        """
        SELECT *
        FROM special_app_usage_snapshots
        WHERE provider = :provider AND usage_date BETWEEN :from AND :to
        ORDER BY usage_date ASC
        """
    )
    fun observeByDateRange(
        provider: SpecialAppProvider,
        from: String,
        to: String,
    ): Flow<List<SpecialAppUsageSnapshotEntity>>

    @Query(
        """
        SELECT *
        FROM special_app_usage_snapshots
        WHERE provider = :provider AND usage_date < :beforeDate
        ORDER BY usage_date DESC
        LIMIT 1
        """
    )
    suspend fun getLatestBefore(
        provider: SpecialAppProvider,
        beforeDate: String,
    ): SpecialAppUsageSnapshotEntity?
}

@Dao
interface SpecialAppPointCreditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(credit: SpecialAppPointCreditEntity)

    @Query(
        """
        SELECT *
        FROM special_app_point_credits
        WHERE provider = :provider AND group_id = :groupId AND credit_date = :date
        LIMIT 1
        """
    )
    suspend fun get(
        provider: SpecialAppProvider,
        groupId: String,
        date: String,
    ): SpecialAppPointCreditEntity?
}
