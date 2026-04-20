package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppGroupDao {
    @Query("SELECT * FROM app_groups WHERE is_deleted = 0 ORDER BY created_at DESC")
    fun getAllGroups(): Flow<List<AppGroupEntity>>

    /** 同步查询：按 ID 取一条分组记录（非 Flow） */
    @Query("SELECT * FROM app_groups WHERE id = :groupId AND is_deleted = 0 LIMIT 1")
    fun getGroupByIdSync(groupId: String): AppGroupEntity?

    /** 同步批量查询：一次性按 ID 列表取多条分组记录，避免循环调用 */
    @Query("SELECT * FROM app_groups WHERE id IN (:ids) AND is_deleted = 0")
    fun getGroupsByIdsSync(ids: List<String>): List<AppGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: AppGroupEntity)

    @Query("UPDATE app_groups SET is_deleted = 1, updated_at = :updatedAt WHERE id = :groupId")
    suspend fun softDeleteGroup(groupId: String, updatedAt: Long)
}
