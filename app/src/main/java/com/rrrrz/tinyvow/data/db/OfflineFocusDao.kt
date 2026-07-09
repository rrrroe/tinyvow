package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineFocusCategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(categories: List<OfflineFocusCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: OfflineFocusCategoryEntity)

    @Query(
        """
        SELECT *
        FROM offline_focus_categories
        WHERE is_deleted = 0
            AND (:includeArchived = 1 OR is_archived = 0)
        ORDER BY sort_order ASC, created_at ASC
        """
    )
    fun observeAll(includeArchived: Boolean = false): Flow<List<OfflineFocusCategoryEntity>>

    @Query(
        """
        SELECT *
        FROM offline_focus_categories
        WHERE is_deleted = 0
            AND (:includeArchived = 1 OR is_archived = 0)
        ORDER BY sort_order ASC, created_at ASC
        """
    )
    suspend fun getAll(includeArchived: Boolean = false): List<OfflineFocusCategoryEntity>

    @Query("SELECT * FROM offline_focus_categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): OfflineFocusCategoryEntity?

    @Query("SELECT COUNT(*) FROM offline_focus_categories")
    suspend fun count(): Int

    @Query(
        """
        UPDATE offline_focus_categories
        SET name = :name,
            icon_key = :iconKey,
            custom_icon_path = :customIconPath,
            color_argb = :colorArgb,
            points_per_minute = :pointsPerMinute,
            sort_order = :sortOrder,
            is_archived = :isArchived,
            is_deleted = :isDeleted,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateEditableFields(
        id: String,
        name: String,
        iconKey: String,
        customIconPath: String?,
        colorArgb: Int,
        pointsPerMinute: Double,
        sortOrder: Int,
        isArchived: Boolean,
        isDeleted: Boolean,
        updatedAt: Long,
    )
}

@Dao
interface OfflineFocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: OfflineFocusSessionEntity)

    @Update
    suspend fun update(session: OfflineFocusSessionEntity)

    @Query("SELECT * FROM offline_focus_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): OfflineFocusSessionEntity?

    @Query("SELECT * FROM offline_focus_sessions WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<OfflineFocusSessionEntity?>

    @Query("SELECT * FROM offline_focus_sessions WHERE settled_ledger_id = :ledgerId LIMIT 1")
    suspend fun getByLedgerId(ledgerId: String): OfflineFocusSessionEntity?

    @Query(
        """
        SELECT *
        FROM offline_focus_sessions
        WHERE status IN ('RUNNING', 'PAUSED')
        ORDER BY started_at DESC
        LIMIT 1
        """
    )
    fun observeActiveSession(): Flow<OfflineFocusSessionEntity?>

    @Query(
        """
        SELECT *
        FROM offline_focus_sessions
        WHERE status IN ('RUNNING', 'PAUSED')
        ORDER BY started_at DESC
        LIMIT 1
        """
    )
    suspend fun getActiveSession(): OfflineFocusSessionEntity?

    @Query(
        """
        SELECT *
        FROM offline_focus_sessions
        WHERE started_at < :endMillis
            AND COALESCE(completed_at, abandoned_at, started_at + planned_duration_millis) > :startMillis
        ORDER BY started_at ASC
        """
    )
    fun observeSessionsOverlapping(startMillis: Long, endMillis: Long): Flow<List<OfflineFocusSessionEntity>>

    @Query(
        """
        SELECT *
        FROM offline_focus_sessions
        WHERE started_at < :endMillis
            AND COALESCE(completed_at, abandoned_at, started_at + planned_duration_millis) > :startMillis
        ORDER BY started_at ASC
        """
    )
    suspend fun getSessionsOverlapping(startMillis: Long, endMillis: Long): List<OfflineFocusSessionEntity>
}
