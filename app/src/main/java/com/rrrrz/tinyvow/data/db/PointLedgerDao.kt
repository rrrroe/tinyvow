package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PointLedgerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PointLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entry: PointLedgerEntity): Long

    @Update
    suspend fun update(entry: PointLedgerEntity)

    @Query("DELETE FROM point_ledger WHERE id = :entryId")
    suspend fun deleteById(entryId: String)

    @Query("SELECT * FROM point_ledger WHERE source_ref_id = :sourceRefId LIMIT 1")
    suspend fun getBySourceRefId(sourceRefId: String): PointLedgerEntity?

    @Query(
        """
        SELECT *
        FROM point_ledger
        WHERE ledger_date = :date
            AND source_ref_id LIKE :sourceRefPrefix || '%'
        """
    )
    suspend fun getStepEarnEntriesByDate(
        date: String,
        sourceRefPrefix: String,
    ): List<PointLedgerEntity>

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
        WHERE ledger_date = :date
            AND entry_type = 'OFFLINE_FOCUS'
            AND delta_points > 0
            AND id != :excludedEntryId
        """
    )
    suspend fun sumOfflineFocusEarnedByDateExcludingEntry(
        date: String,
        excludedEntryId: String,
    ): Double

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
        SELECT *
        FROM point_ledger
        ORDER BY occurred_at DESC, created_at DESC
        """
    )
    fun observeAllEntries(): Flow<List<PointLedgerEntity>>

    @Query(
        """
        SELECT
            ledger_date AS ledgerDate,
            COALESCE(SUM(CASE WHEN delta_points > 0 THEN delta_points ELSE 0 END), 0) AS earnedPoints,
            COALESCE(ABS(SUM(CASE WHEN delta_points < 0 THEN delta_points ELSE 0 END)), 0) AS spentPoints,
            COALESCE(SUM(delta_points), 0) AS netPoints
        FROM point_ledger
        GROUP BY ledger_date
        ORDER BY ledger_date ASC
        """
    )
    fun observeDailyStats(): Flow<List<PointLedgerDailyStats>>

    @Query(
        """
        SELECT
            ledger_date AS ledgerDate,
            COALESCE(SUM(CASE WHEN delta_points > 0 THEN delta_points ELSE 0 END), 0) AS earnedPoints,
            COALESCE(ABS(SUM(CASE WHEN delta_points < 0 THEN delta_points ELSE 0 END)), 0) AS spentPoints,
            COALESCE(SUM(delta_points), 0) AS netPoints
        FROM point_ledger
        WHERE ledger_date BETWEEN :from AND :to
        GROUP BY ledger_date
        ORDER BY ledger_date ASC
        """
    )
    suspend fun getDailyStatsByRange(from: String, to: String): List<PointLedgerDailyStats>

    @Query(
        """
        SELECT *
        FROM point_ledger
        WHERE ledger_date BETWEEN :from AND :to
        ORDER BY occurred_at ASC
        """,
    )
    suspend fun getEntriesByDateRange(from: String, to: String): List<PointLedgerEntity>

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE ledger_date < :date
        """
    )
    suspend fun sumNetBeforeDate(date: String): Double

    @Query(
        """
        SELECT
            id AS id,
            ledger_date AS ledgerDate,
            occurred_at AS occurredAt,
            ABS(delta_points) AS points,
            reward_title_snapshot AS rewardTitleSnapshot,
            note AS note
        FROM point_ledger
        WHERE delta_points < 0
        ORDER BY occurred_at DESC
        """
    )
    fun observeSpendRecords(): Flow<List<PointLedgerSpendRecord>>

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

data class PointLedgerDailyStats(
    val ledgerDate: String,
    val earnedPoints: Double,
    val spentPoints: Double,
    val netPoints: Double,
)

data class PointLedgerSpendRecord(
    val id: String,
    val ledgerDate: String,
    val occurredAt: Long,
    val points: Double,
    val rewardTitleSnapshot: String?,
    val note: String,
)
