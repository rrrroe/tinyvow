package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PointLedgerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PointLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entry: PointLedgerEntity): Long

    @Query("DELETE FROM point_ledger WHERE id = :entryId")
    suspend fun deleteById(entryId: String)

    @Query("SELECT * FROM point_ledger WHERE source_ref_id = :sourceRefId LIMIT 1")
    suspend fun getBySourceRefId(sourceRefId: String): PointLedgerEntity?

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND delta_points > 0
        """
    )
    suspend fun sumEarnedByDate(date: String): Double

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND delta_points > 0
        """
    )
    fun observeEarnedByDate(date: String): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND entry_type = 'OFFLINE_FOCUS' AND delta_points > 0
        """
    )
    suspend fun sumOfflineFocusEarnedByDate(date: String): Double

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND delta_points > 0 AND group_id IS NULL
        """
    )
    suspend fun sumUngroupedEarnedByDate(date: String): Double

    @Query(
        """
        SELECT COALESCE(ABS(SUM(delta_points)), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND delta_points < 0
        """
    )
    suspend fun sumSpentByDate(date: String): Double

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND group_id = :groupId AND delta_points > 0
        """
    )
    suspend fun sumEarnedByDateAndGroup(date: String, groupId: String): Double

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE delta_points > 0
        """
    )
    suspend fun sumEarnedTotal(): Double

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE delta_points > 0
        """
    )
    fun observeEarnedTotal(): Flow<Double>

    @Query(
        """
        SELECT COALESCE(ABS(SUM(delta_points)), 0)
        FROM point_ledger
        WHERE entry_type = 'REWARD_SPEND' AND delta_points < 0
        """
    )
    suspend fun sumRewardSpentTotal(): Double

    @Query(
        """
        SELECT COALESCE(ABS(SUM(delta_points)), 0)
        FROM point_ledger
        WHERE entry_type = 'REWARD_SPEND' AND delta_points < 0
        """
    )
    fun observeRewardSpentTotal(): Flow<Double>
}
