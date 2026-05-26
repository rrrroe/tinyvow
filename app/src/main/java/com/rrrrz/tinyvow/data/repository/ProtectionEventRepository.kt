package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.ProtectionEventEntity
import com.rrrrz.tinyvow.data.db.ProtectionEventType
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray

class ProtectionEventRepository(
    private val database: AppDatabase,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    private val dao = database.protectionEventDao()

    fun observeAll(): Flow<List<ProtectionEventEntity>> = dao.observeAll()

    fun observeByDate(date: String): Flow<List<ProtectionEventEntity>> = dao.observeByDate(date)

    suspend fun record(
        eventType: ProtectionEventType,
        titleKey: String,
        messageKey: String,
        messageArgs: List<String> = emptyList(),
        targetId: String? = null,
        targetLabel: String? = null,
        beforeJson: String? = null,
        afterJson: String? = null,
        withinWindow: Boolean? = null,
        protectionEnabled: Boolean,
        occurredAt: Long = System.currentTimeMillis(),
    ) = withContext(Dispatchers.IO) {
        dao.insert(
            ProtectionEventEntity(
                id = UUID.randomUUID().toString(),
                eventType = eventType,
                eventDate = ArchiveDateUtils.formatDate(ArchiveDateUtils.localDateAt(occurredAt, zoneId)),
                occurredAt = occurredAt,
                titleKey = titleKey,
                messageKey = messageKey,
                messageArgsJson = messageArgs.takeIf { it.isNotEmpty() }?.let(::encodeArgs),
                targetId = targetId,
                targetLabel = targetLabel,
                beforeJson = beforeJson,
                afterJson = afterJson,
                withinWindow = withinWindow,
                protectionEnabled = protectionEnabled,
            ),
        )
    }

    private fun encodeArgs(args: List<String>): String {
        val array = JSONArray()
        args.forEach(array::put)
        return array.toString()
    }
}
