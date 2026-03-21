package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.AppGroupEntity
import com.rrrrz.tinyvow.data.db.GroupAppCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.util.UUID

data class AppGroupWithApps(
    val group: AppGroupEntity,
    val packageNames: List<String>
)

class AppLimitRepository(private val database: AppDatabase) {
    private val groupDao = database.appGroupDao()
    private val crossRefDao = database.crossRefDao()

    /**
     * 实时暴露出所有未软删除的分组及其关联的应用包名列表
     */
    fun getAllGroupsWithApps(): Flow<List<AppGroupWithApps>> {
        return combine(
            groupDao.getAllGroups(),
            crossRefDao.getAllValidCrossRefs()
        ) { groups, crossRefs ->
            groups.map { group ->
                val groupApps = crossRefs
                    .filter { it.groupId == group.id }
                    .map { it.packageName }
                AppGroupWithApps(group, groupApps)
            }
        }
    }

    /**
     * 创建或全量覆盖更新一个管控分组
     */
    suspend fun createOrUpdateGroup(id: String?, name: String, limitMinutes: Int): String {
        return withContext(Dispatchers.IO) {
            val groupId = id ?: UUID.randomUUID().toString()
            val entity = AppGroupEntity(
                id = groupId,
                name = name,
                dailyLimitMinutes = limitMinutes,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            groupDao.insertGroup(entity)
            groupId
        }
    }

    /**
     * 软删除：标记整个分组及它下面挂载的所有中间表记录为已被删除
     */
    suspend fun deleteGroup(groupId: String) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            groupDao.softDeleteGroup(groupId, now)
            crossRefDao.softDeleteAllForGroup(groupId, now)
        }
    }

    /**
     * 更新某个分组下的全部 App (先软删历史关联，再插入最新关联)
     */
    suspend fun updateGroupApps(groupId: String, packageNames: List<String>) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            crossRefDao.softDeleteAllForGroup(groupId, now)
            
            if (packageNames.isNotEmpty()) {
                val newRefs = packageNames.map {
                    GroupAppCrossRef(
                        packageName = it,
                        groupId = groupId,
                        updatedAt = now
                    )
                }
                crossRefDao.insertCrossRefs(newRefs)
            }
        }
    }
}
