package com.rrrrz.tinyvow.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.room.withTransaction
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.OfflineFocusCategoryEntity
import com.rrrrz.tinyvow.data.db.OfflineFocusAbandonReason
import com.rrrrz.tinyvow.data.db.OfflineFocusMode
import com.rrrrz.tinyvow.data.db.OfflineFocusPauseReason
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionEntity
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import com.rrrrz.tinyvow.data.db.PointLedgerEntity
import com.rrrrz.tinyvow.data.db.PointLedgerEntryType
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.time.BusinessDay
import com.rrrrz.tinyvow.i18n.AppText
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class OfflineFocusCategory(
    val id: String,
    val name: String,
    val iconKey: String,
    val customIconPath: String? = null,
    val colorArgb: Int,
    val pointsPerMinute: Double = 1.0,
    val sortOrder: Int,
    val isBuiltIn: Boolean,
    val isArchived: Boolean,
    val isDeleted: Boolean = false,
)

data class OfflineFocusSession(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val iconKey: String,
    val customIconPath: String? = null,
    val colorArgb: Int,
    val pointsPerMinute: Double = 1.0,
    val plannedDurationMillis: Long,
    val actualDurationMillis: Long,
    val status: OfflineFocusSessionStatus,
    val focusMode: OfflineFocusMode = OfflineFocusMode.NORMAL,
    val pauseReason: OfflineFocusPauseReason? = null,
    val abandonedReason: OfflineFocusAbandonReason? = null,
    val violationPackageName: String? = null,
    val startedAt: Long,
    val pausedAt: Long?,
    val completedAt: Long?,
    val abandonedAt: Long?,
    val pointsAwarded: Double,
)

data class OfflineFocusCategorySummary(
    val categoryName: String,
    val iconKey: String,
    val customIconPath: String? = null,
    val colorArgb: Int,
    val totalMillis: Long,
    val completedCount: Int,
    val pointsAwarded: Double,
)

data class OfflineFocusTodaySummary(
    val totalMillis: Long = 0L,
    val completedCount: Int = 0,
    val pointsAwarded: Double = 0.0,
    val sessions: List<OfflineFocusSession> = emptyList(),
    val categories: List<OfflineFocusCategorySummary> = emptyList(),
)

data class OfflineFocusDebugSessionInput(
    val sessionId: String? = null,
    val categoryId: String?,
    val categoryNameSnapshot: String?,
    val categoryIconKeySnapshot: String?,
    val categoryCustomIconPathSnapshot: String?,
    val categoryColorArgbSnapshot: Int?,
    val pointsPerMinuteSnapshot: Double,
    val plannedDurationMillis: Long,
    val actualDurationMillis: Long,
    val status: OfflineFocusSessionStatus,
    val focusMode: OfflineFocusMode,
    val startedAt: Long,
    val pausedAt: Long?,
    val resumedAt: Long?,
    val completedAt: Long?,
    val abandonedAt: Long?,
    val pauseReason: OfflineFocusPauseReason?,
    val abandonedReason: OfflineFocusAbandonReason?,
    val violationStartedAt: Long?,
    val violationPackageName: String?,
    val pointsAwarded: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val createLedger: Boolean,
    val ledgerDate: String?,
    val ledgerOccurredAt: Long?,
)

private data class OfflineFocusFinishResult(
    val session: OfflineFocusSessionEntity,
    val insertedLedger: Boolean,
    val awardedPoints: Double,
)

class OfflineFocusRepository(
    private val context: Context,
    private val database: AppDatabase,
) {
    private val preferences = ManagedAppPreferences(context.applicationContext)
    private val categoryDao = database.offlineFocusCategoryDao()
    private val sessionDao = database.offlineFocusSessionDao()
    private val pointLedgerDao = database.pointLedgerDao()
    private val zoneId = ZoneId.systemDefault()
    private var cachedHomeLauncherPackages: Set<String>? = null
    private var cachedInputMethodPackages: Set<String>? = null
    private var cachedInputMethodPackagesAtMillis: Long = 0L

    fun observeCategories(includeArchived: Boolean = false): Flow<List<OfflineFocusCategory>> =
        categoryDao.observeAll(includeArchived).map { entities ->
            entities.map(::toCategory)
        }

    fun observeActiveSession(): Flow<OfflineFocusSession?> =
        sessionDao.observeActiveSession().map { it?.let(::toSession) }

    fun observeSession(sessionId: String): Flow<OfflineFocusSession?> =
        sessionDao.observeById(sessionId).map { it?.let(::toSession) }

    fun observeSummaryForDay(
        dayStartMillis: Long,
        dayEndMillis: Long,
    ): Flow<OfflineFocusTodaySummary> =
        sessionDao.observeSessionsOverlapping(dayStartMillis, dayEndMillis).map { sessions ->
            buildSummary(sessions, dayStartMillis, dayEndMillis)
        }

    suspend fun getSummaryForDay(
        dayStartMillis: Long,
        dayEndMillis: Long,
    ): OfflineFocusTodaySummary =
        withContext(Dispatchers.IO) {
            buildSummary(sessionDao.getSessionsOverlapping(dayStartMillis, dayEndMillis), dayStartMillis, dayEndMillis)
        }

    suspend fun ensureBuiltInCategories() {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            categoryDao.insertIgnore(
                builtInDefinitions.map { definition ->
                    OfflineFocusCategoryEntity(
                        id = definition.id,
                        name = definition.fallbackName,
                        iconKey = definition.iconKey,
                        customIconPath = null,
                        colorArgb = definition.colorArgb,
                        pointsPerMinute = 1.0,
                        sortOrder = definition.sortOrder,
                        isBuiltIn = true,
                        isArchived = false,
                        isDeleted = false,
                        createdAt = now,
                        updatedAt = now,
                    )
                },
            )
        }
    }

    suspend fun startSession(
        categoryId: String?,
        durationMinutes: Int,
        focusMode: OfflineFocusMode = OfflineFocusMode.NORMAL,
        nowMillis: Long = System.currentTimeMillis(),
    ): OfflineFocusSession? {
        return withContext(Dispatchers.IO) {
            ensureBuiltInCategories()
            sessionDao.getActiveSession()?.let(::toSession)?.let { return@withContext it }
            val category = resolveCategory(categoryId) ?: return@withContext null
            val normalizedMinutes =
                durationMinutes.coerceIn(
                    ManagedAppPreferences.MIN_OFFLINE_FOCUS_DURATION_MINUTES,
                    ManagedAppPreferences.MAX_OFFLINE_FOCUS_DURATION_MINUTES,
                )
            val session =
                OfflineFocusSessionEntity(
                    id = UUID.randomUUID().toString(),
                    categoryId = category.id,
                    categoryNameSnapshot = displayName(category),
                    categoryIconKeySnapshot = category.iconKey,
                    categoryCustomIconPathSnapshot = category.customIconPath,
                    categoryColorArgbSnapshot = category.colorArgb,
                    pointsPerMinuteSnapshot = category.pointsPerMinute,
                    focusMode = focusMode,
                    plannedDurationMillis = normalizedMinutes * MINUTE_MILLIS,
                    actualDurationMillis = 0L,
                    status = OfflineFocusSessionStatus.RUNNING,
                    startedAt = nowMillis,
                    pointsAwarded = 0.0,
                    createdAt = nowMillis,
                    updatedAt = nowMillis,
                )
            sessionDao.insert(session)
            preferences.setOfflineFocusDefaultCategoryId(category.id)
            preferences.setOfflineFocusDefaultDurationMinutes(normalizedMinutes)
            preferences.setOfflineFocusCategoryDefaults(category.id, normalizedMinutes, focusMode)
            toSession(session)
        }
    }

    suspend fun completeSession(
        sessionId: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): OfflineFocusSession? =
        finishSession(
            sessionId = sessionId,
            nowMillis = nowMillis,
            forceComplete = true,
        )

    suspend fun stopSessionEarly(
        sessionId: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): OfflineFocusSession? =
        finishSession(
            sessionId = sessionId,
            nowMillis = nowMillis,
            forceComplete = false,
        )

    suspend fun abandonSession(
        sessionId: String,
        reason: OfflineFocusAbandonReason = OfflineFocusAbandonReason.USER,
        nowMillis: Long = System.currentTimeMillis(),
    ): OfflineFocusSession? {
        return withContext(Dispatchers.IO) {
            val current = sessionDao.getById(sessionId) ?: return@withContext null
            if (current.status != OfflineFocusSessionStatus.RUNNING && current.status != OfflineFocusSessionStatus.PAUSED) {
                return@withContext toSession(current)
            }
            val actualMillis = elapsedMillis(current, nowMillis)
            val updated =
                current.copy(
                    actualDurationMillis = actualMillis,
                    status = OfflineFocusSessionStatus.ABANDONED,
                    abandonedReason = reason,
                    abandonedAt = nowMillis,
                    pointsAwarded = 0.0,
                    updatedAt = nowMillis,
                )
            sessionDao.update(updated)
            toSession(updated)
        }
    }

    suspend fun pauseSession(
        sessionId: String,
        reason: OfflineFocusPauseReason = OfflineFocusPauseReason.USER,
        nowMillis: Long = System.currentTimeMillis(),
    ): OfflineFocusSession? {
        return withContext(Dispatchers.IO) {
            val current = sessionDao.getById(sessionId) ?: return@withContext null
            if (current.status != OfflineFocusSessionStatus.RUNNING) {
                return@withContext toSession(current)
            }
            val updated =
                current.copy(
                    actualDurationMillis = elapsedMillis(current, nowMillis),
                    status = OfflineFocusSessionStatus.PAUSED,
                    pauseReason = reason,
                    pausedAt = nowMillis,
                    updatedAt = nowMillis,
                )
            sessionDao.update(updated)
            toSession(updated)
        }
    }

    suspend fun resumeSession(
        sessionId: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): OfflineFocusSession? {
        return withContext(Dispatchers.IO) {
            val current = sessionDao.getById(sessionId) ?: return@withContext null
            if (current.status != OfflineFocusSessionStatus.PAUSED) {
                return@withContext toSession(current)
            }
            val pausedAt = current.pausedAt ?: nowMillis
            val pausedDuration = (nowMillis - pausedAt).coerceAtLeast(0L)
            val updated =
                current.copy(
                    status = OfflineFocusSessionStatus.RUNNING,
                    startedAt = current.startedAt + pausedDuration,
                    pausedAt = null,
                    pauseReason = null,
                    resumedAt = nowMillis,
                    updatedAt = nowMillis,
                )
            sessionDao.update(updated)
            toSession(updated)
        }
    }

    suspend fun getActiveSessionOnce(): OfflineFocusSession? =
        withContext(Dispatchers.IO) {
            sessionDao.getActiveSession()?.let(::toSession)
        }

    suspend fun getSessionOnce(sessionId: String): OfflineFocusSession? =
        withContext(Dispatchers.IO) {
            sessionDao.getById(sessionId)?.let(::toSession)
        }

    suspend fun adjustCompletedSessionEndEarlier(
        sessionId: String,
        minutesEarlier: Int,
    ): OfflineFocusSession? {
        return withContext(Dispatchers.IO) {
            val dayBoundaryHour = preferences.getDayBoundaryHourOnce()
            val dailyCap = preferences.getOfflineFocusDailyPointCapOnce().toDouble()
            val result =
                database.withTransaction {
                    val current = sessionDao.getById(sessionId) ?: return@withTransaction null
                    val isBelowThresholdAbandoned =
                        current.status == OfflineFocusSessionStatus.ABANDONED &&
                            current.abandonedReason == OfflineFocusAbandonReason.BELOW_THRESHOLD
                    if (
                        current.status != OfflineFocusSessionStatus.COMPLETED &&
                        current.status != OfflineFocusSessionStatus.SETTLED &&
                        !isBelowThresholdAbandoned
                    ) {
                        return@withTransaction OfflineFocusFinishResult(
                            session = current,
                            insertedLedger = false,
                            awardedPoints = 0.0,
                        )
                    }
                    val endAt = current.completedAt ?: current.abandonedAt ?: return@withTransaction OfflineFocusFinishResult(
                        session = current,
                        insertedLedger = false,
                        awardedPoints = 0.0,
                    )
                    val maxEarlierMinutes =
                        (((endAt - current.startedAt).coerceAtLeast(0L) / MINUTE_MILLIS) - 1L)
                            .coerceAtLeast(0L)
                            .coerceAtMost(60L)
                            .toInt()
                    val normalizedMinutes = minutesEarlier.coerceIn(0, maxEarlierMinutes)
                    val adjustedEndAt = endAt - normalizedMinutes * MINUTE_MILLIS
                    val adjustedActualMillis =
                        (adjustedEndAt - current.startedAt)
                            .coerceIn(MINUTE_MILLIS, current.plannedDurationMillis.coerceAtLeast(MINUTE_MILLIS))
                    if (isBelowThresholdAbandoned) {
                        val adjusted =
                            current.copy(
                                actualDurationMillis = adjustedActualMillis,
                                abandonedAt = adjustedEndAt,
                                pointsAwarded = 0.0,
                                updatedAt = System.currentTimeMillis(),
                            )
                        sessionDao.update(adjusted)
                        return@withTransaction OfflineFocusFinishResult(
                            session = adjusted,
                            insertedLedger = false,
                            awardedPoints = 0.0,
                        )
                    }
                    val sourceRefId = sourceRefId(sessionId)
                    val existingLedger = pointLedgerDao.getBySourceRefId(sourceRefId)
                    val ledgerId = existingLedger?.id ?: UUID.randomUUID().toString()
                    val ledgerDate =
                        ArchiveDateUtils.formatDate(
                            BusinessDay.dateAt(adjustedEndAt, zoneId, dayBoundaryHour),
                        )
                    val alreadyAwarded =
                        pointLedgerDao.sumOfflineFocusEarnedByDateExcludingEntry(
                            date = ledgerDate,
                            excludedEntryId = existingLedger?.id.orEmpty(),
                        )
                    val rawPoints =
                        ((adjustedActualMillis / MINUTE_MILLIS).toDouble() * current.pointsPerMinuteSnapshot)
                            .coerceAtLeast(0.0)
                    val adjustedPoints =
                        rawPoints.coerceAtMost((dailyCap - alreadyAwarded).coerceAtLeast(0.0))
                    val messageArgs =
                        JSONArray(listOf(current.categoryNameSnapshot, adjustedActualMillis / MINUTE_MILLIS)).toString()
                    if (existingLedger == null) {
                        pointLedgerDao.insertIgnore(
                            PointLedgerEntity(
                                id = ledgerId,
                                occurredAt = adjustedEndAt,
                                ledgerDate = ledgerDate,
                                entryType = PointLedgerEntryType.OFFLINE_FOCUS,
                                deltaPoints = adjustedPoints,
                                sourceRefId = sourceRefId,
                                messageKey = "ledger_offline_focus",
                                messageArgsJson = messageArgs,
                                note = current.categoryNameSnapshot,
                                createdAt = System.currentTimeMillis(),
                            ),
                        )
                    } else {
                        pointLedgerDao.update(
                            existingLedger.copy(
                                occurredAt = adjustedEndAt,
                                ledgerDate = ledgerDate,
                                deltaPoints = adjustedPoints,
                                messageArgsJson = messageArgs,
                                note = current.categoryNameSnapshot,
                            ),
                        )
                    }
                    val adjusted =
                        current.copy(
                            actualDurationMillis = adjustedActualMillis,
                            status = OfflineFocusSessionStatus.SETTLED,
                            completedAt = adjustedEndAt,
                            pointsAwarded = adjustedPoints,
                            settledLedgerId = ledgerId,
                            updatedAt = System.currentTimeMillis(),
                        )
                    sessionDao.update(adjusted)
                    OfflineFocusFinishResult(
                        session = adjusted,
                        insertedLedger = true,
                        awardedPoints = adjustedPoints - (existingLedger?.deltaPoints ?: 0.0),
                    )
                } ?: return@withContext null
            if (result.awardedPoints != 0.0) {
                preferences.addUserPoints(result.awardedPoints)
                AppLimitRepository(context, database).checkAchievements()
            }
            toSession(result.session)
        }
    }

    suspend fun getDefaultCategory(): OfflineFocusCategory? {
        return withContext(Dispatchers.IO) {
            ensureBuiltInCategories()
            resolveCategory(preferences.getOfflineFocusDefaultCategoryIdOnce())?.let(::toCategory)
        }
    }

    suspend fun shouldContinueOnLock(): Boolean = preferences.getOfflineFocusContinueOnLockOnce()

    suspend fun handleForegroundPackageForFocus(
        packageName: String,
        ownPackageName: String,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        withContext(Dispatchers.IO) {
            val current = sessionDao.getActiveSession() ?: return@withContext
            val whitelist = preferences.getOfflineFocusWhitelistPackagesOnce() + ownPackageName
            val allowed = packageName in whitelist || isNeutralFocusForegroundPackage(packageName, ownPackageName)
            if (allowed) {
                if (current.status == OfflineFocusSessionStatus.PAUSED &&
                    current.pauseReason == OfflineFocusPauseReason.NON_WHITELIST_APP
                ) {
                    resumeSession(current.id, nowMillis)
                }
                return@withContext
            }
            when (current.focusMode) {
                OfflineFocusMode.STRICT -> {
                    val actualMillis = elapsedMillis(current, nowMillis)
                    sessionDao.update(
                        current.copy(
                            actualDurationMillis = actualMillis,
                            status = OfflineFocusSessionStatus.ABANDONED,
                            abandonedReason = OfflineFocusAbandonReason.STRICT_VIOLATION,
                            violationStartedAt = nowMillis,
                            violationPackageName = packageName,
                            abandonedAt = nowMillis,
                            pointsAwarded = 0.0,
                            updatedAt = nowMillis,
                        ),
                    )
                }
                OfflineFocusMode.NORMAL -> {
                    if (current.status == OfflineFocusSessionStatus.RUNNING) {
                        sessionDao.update(
                            current.copy(
                                actualDurationMillis = elapsedMillis(current, nowMillis),
                                status = OfflineFocusSessionStatus.PAUSED,
                                pausedAt = nowMillis,
                                pauseReason = OfflineFocusPauseReason.NON_WHITELIST_APP,
                                violationStartedAt = nowMillis,
                                violationPackageName = packageName,
                                updatedAt = nowMillis,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun isNeutralFocusForegroundPackage(
        packageName: String,
        ownPackageName: String,
    ): Boolean {
        val packageLower = packageName.lowercase()
        return packageName == ownPackageName ||
            packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName == "com.google.android.systemui" ||
            packageName in homeLauncherPackages() ||
            packageName in inputMethodPackages() ||
            packageName in knownHomeLauncherPackages ||
            packageLower.contains("launcher")
    }

    private fun homeLauncherPackages(): Set<String> {
        cachedHomeLauncherPackages?.let { return it }
        val intent =
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
        val packages =
            runCatching {
                context.packageManager
                    .queryIntentActivities(intent, 0)
                    .mapNotNull { it.activityInfo?.packageName }
                    .toSet()
            }.getOrDefault(emptySet())
        val resolvedHome =
            runCatching {
                context.packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
            }.getOrNull()
        val resolvedPackages = packages + setOfNotNull(resolvedHome)
        cachedHomeLauncherPackages = resolvedPackages
        return resolvedPackages
    }

    private fun inputMethodPackages(): Set<String> {
        val now = System.currentTimeMillis()
        cachedInputMethodPackages
            ?.takeIf { now - cachedInputMethodPackagesAtMillis <= INPUT_METHOD_PACKAGE_CACHE_MILLIS }
            ?.let { return it }
        val resolver = context.contentResolver
        val values =
            listOfNotNull(
                runCatching {
                    Settings.Secure.getString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                }.getOrNull(),
                runCatching {
                    Settings.Secure.getString(resolver, Settings.Secure.ENABLED_INPUT_METHODS)
                }.getOrNull(),
            )
        val packages =
            values
                .flatMap { value -> value.split(':', ';') }
                .mapNotNull { entry ->
                    entry.substringBefore('/').trim().takeIf { it.contains('.') }
                }
                .toSet()
        cachedInputMethodPackages = packages
        cachedInputMethodPackagesAtMillis = now
        return packages
    }

    suspend fun updateCategoryColor(
        categoryId: String,
        colorArgb: Int,
    ) {
        withContext(Dispatchers.IO) {
            val category = categoryDao.getById(categoryId) ?: return@withContext
            categoryDao.updateEditableFields(
                id = category.id,
                name = category.name,
                iconKey = category.iconKey,
                colorArgb = colorArgb,
                customIconPath = category.customIconPath,
                pointsPerMinute = category.pointsPerMinute,
                sortOrder = category.sortOrder,
                isArchived = category.isArchived,
                isDeleted = category.isDeleted,
                updatedAt = System.currentTimeMillis(),
            )
        }
    }

    suspend fun upsertCategory(
        categoryId: String?,
        name: String,
        iconKey: String,
        customIconPath: String?,
        colorArgb: Int,
        pointsPerMinute: Double,
    ): OfflineFocusCategory? {
        return withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val normalizedName = name.trim().takeIf { it.isNotBlank() } ?: return@withContext null
            val normalizedPoints = pointsPerMinute.coerceIn(MIN_POINTS_PER_MINUTE, MAX_POINTS_PER_MINUTE)
            val existing = categoryId?.let { categoryDao.getById(it) }
            if (existing == null) {
                val nextOrder = (categoryDao.getAll(includeArchived = true).maxOfOrNull { it.sortOrder } ?: -1) + 1
                val entity =
                    OfflineFocusCategoryEntity(
                        id = UUID.randomUUID().toString(),
                        name = normalizedName,
                        iconKey = iconKey.ifBlank { DEFAULT_CUSTOM_ICON_KEY },
                        customIconPath = customIconPath,
                        colorArgb = colorArgb,
                        pointsPerMinute = normalizedPoints,
                        sortOrder = nextOrder,
                        isBuiltIn = false,
                        isArchived = false,
                        isDeleted = false,
                        createdAt = now,
                        updatedAt = now,
                    )
                categoryDao.upsert(entity)
                toCategory(entity)
            } else {
                categoryDao.updateEditableFields(
                    id = existing.id,
                    name = normalizedName,
                    iconKey = iconKey.ifBlank { existing.iconKey },
                    customIconPath = customIconPath,
                    colorArgb = colorArgb,
                    pointsPerMinute = normalizedPoints,
                    sortOrder = existing.sortOrder,
                    isArchived = existing.isArchived,
                    isDeleted = false,
                    updatedAt = now,
                )
                categoryDao.getById(existing.id)?.let(::toCategory)
            }
        }
    }

    suspend fun importCategoryIcon(
        categoryId: String,
        sourceUri: Uri,
    ): OfflineFocusCategory? {
        return withContext(Dispatchers.IO) {
            val category = categoryDao.getById(categoryId) ?: return@withContext null
            val storage = FocusIconStorage.fromContext(context)
            val importedPath = storage.importImage(context.contentResolver, sourceUri)
            categoryDao.updateEditableFields(
                id = category.id,
                name = category.name,
                iconKey = category.iconKey,
                customIconPath = importedPath,
                colorArgb = category.colorArgb,
                pointsPerMinute = category.pointsPerMinute,
                sortOrder = category.sortOrder,
                isArchived = category.isArchived,
                isDeleted = category.isDeleted,
                updatedAt = System.currentTimeMillis(),
            )
            categoryDao.getById(category.id)?.let(::toCategory)
        }
    }

    suspend fun moveCategory(
        categoryId: String,
        direction: Int,
    ) {
        withContext(Dispatchers.IO) {
            if (direction == 0) return@withContext
            val categories = categoryDao.getAll(includeArchived = true)
            val index = categories.indexOfFirst { it.id == categoryId }
            if (index < 0) return@withContext
            val targetIndex = (index + direction.coerceIn(-1, 1)).coerceIn(0, categories.lastIndex)
            if (targetIndex == index) return@withContext
            val current = categories[index]
            val target = categories[targetIndex]
            val now = System.currentTimeMillis()
            database.withTransaction {
                categoryDao.updateEditableFields(
                    id = current.id,
                    name = current.name,
                    iconKey = current.iconKey,
                    customIconPath = current.customIconPath,
                    colorArgb = current.colorArgb,
                    pointsPerMinute = current.pointsPerMinute,
                    sortOrder = target.sortOrder,
                    isArchived = current.isArchived,
                    isDeleted = current.isDeleted,
                    updatedAt = now,
                )
                categoryDao.updateEditableFields(
                    id = target.id,
                    name = target.name,
                    iconKey = target.iconKey,
                    customIconPath = target.customIconPath,
                    colorArgb = target.colorArgb,
                    pointsPerMinute = target.pointsPerMinute,
                    sortOrder = current.sortOrder,
                    isArchived = target.isArchived,
                    isDeleted = target.isDeleted,
                    updatedAt = now,
                )
            }
        }
    }

    suspend fun setCategoryArchived(
        categoryId: String,
        archived: Boolean,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val category = categoryDao.getById(categoryId) ?: return@withContext false
            categoryDao.updateEditableFields(
                id = category.id,
                name = category.name,
                iconKey = category.iconKey,
                colorArgb = category.colorArgb,
                customIconPath = category.customIconPath,
                pointsPerMinute = category.pointsPerMinute,
                sortOrder = category.sortOrder,
                isArchived = archived,
                isDeleted = category.isDeleted,
                updatedAt = System.currentTimeMillis(),
            )
            if (archived && preferences.getOfflineFocusDefaultCategoryIdOnce() == categoryId) {
                val replacement = categoryDao.getAll(includeArchived = false).firstOrNull { it.id != categoryId }
                preferences.setOfflineFocusDefaultCategoryId(replacement?.id)
            }
            true
        }
    }

    suspend fun deleteCategory(categoryId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val category = categoryDao.getById(categoryId) ?: return@withContext false
            categoryDao.updateEditableFields(
                id = category.id,
                name = category.name,
                iconKey = category.iconKey,
                customIconPath = category.customIconPath,
                colorArgb = category.colorArgb,
                pointsPerMinute = category.pointsPerMinute,
                sortOrder = category.sortOrder,
                isArchived = true,
                isDeleted = true,
                updatedAt = System.currentTimeMillis(),
            )
            if (preferences.getOfflineFocusDefaultCategoryIdOnce() == categoryId) {
                val replacement = categoryDao.getAll(includeArchived = false).firstOrNull { it.id != categoryId }
                preferences.setOfflineFocusDefaultCategoryId(replacement?.id)
            }
            true
        }
    }

    suspend fun createSettledTestSession(
        categoryId: String?,
        durationMinutes: Int,
        endedAtMillis: Long = System.currentTimeMillis(),
    ): OfflineFocusSession? {
        return withContext(Dispatchers.IO) {
            ensureBuiltInCategories()
            val normalizedMinutes =
                durationMinutes.coerceIn(
                    ManagedAppPreferences.MIN_OFFLINE_FOCUS_DURATION_MINUTES,
                    ManagedAppPreferences.MAX_OFFLINE_FOCUS_DURATION_MINUTES,
                )
            val dayBoundaryHour = preferences.getDayBoundaryHourOnce()
            val dailyCap = preferences.getOfflineFocusDailyPointCapOnce().toDouble()
            val result =
                database.withTransaction {
                    val category = resolveCategory(categoryId) ?: return@withTransaction null
                    val sessionId = UUID.randomUUID().toString()
                    val ledgerId = UUID.randomUUID().toString()
                    val durationMillis = normalizedMinutes * MINUTE_MILLIS
                    val startedAt = (endedAtMillis - durationMillis).coerceAtLeast(0L)
                    val rawPoints = normalizedMinutes.toDouble() * category.pointsPerMinute
                    val ledgerDate =
                        ArchiveDateUtils.formatDate(
                            BusinessDay.dateAt(endedAtMillis, zoneId, dayBoundaryHour),
                        )
                    val alreadyAwarded = pointLedgerDao.sumOfflineFocusEarnedByDate(ledgerDate)
                    val awardedPoints = rawPoints.coerceAtMost((dailyCap - alreadyAwarded).coerceAtLeast(0.0))
                    val categoryName = displayName(category)
                    pointLedgerDao.insertIgnore(
                        PointLedgerEntity(
                            id = ledgerId,
                            occurredAt = endedAtMillis,
                            ledgerDate = ledgerDate,
                            entryType = PointLedgerEntryType.OFFLINE_FOCUS,
                            deltaPoints = awardedPoints,
                            sourceRefId = sourceRefId(sessionId),
                            messageKey = "ledger_offline_focus",
                            messageArgsJson = JSONArray(listOf(categoryName, normalizedMinutes)).toString(),
                            note = categoryName,
                            createdAt = endedAtMillis,
                        ),
                    )
                    val session =
                        OfflineFocusSessionEntity(
                            id = sessionId,
                            categoryId = category.id,
                            categoryNameSnapshot = categoryName,
                            categoryIconKeySnapshot = category.iconKey,
                            categoryCustomIconPathSnapshot = category.customIconPath,
                            categoryColorArgbSnapshot = category.colorArgb,
                            pointsPerMinuteSnapshot = category.pointsPerMinute,
                            plannedDurationMillis = durationMillis,
                            actualDurationMillis = durationMillis,
                            status = OfflineFocusSessionStatus.SETTLED,
                            focusMode = preferences.getOfflineFocusDefaultModeOnce(),
                            startedAt = startedAt,
                            completedAt = endedAtMillis,
                            pointsAwarded = awardedPoints,
                            settledLedgerId = ledgerId,
                            createdAt = endedAtMillis,
                            updatedAt = endedAtMillis,
                        )
                    sessionDao.insert(session)
                    OfflineFocusFinishResult(
                        session = session,
                        insertedLedger = true,
                        awardedPoints = awardedPoints,
                    )
                }
            result ?: return@withContext null
            if (result.awardedPoints > 0.0) {
                preferences.addUserPoints(result.awardedPoints)
                AppLimitRepository(context, database).checkAchievements()
            }
            toSession(result.session)
        }
    }

    suspend fun createDebugSession(input: OfflineFocusDebugSessionInput): OfflineFocusSession? {
        return withContext(Dispatchers.IO) {
            ensureBuiltInCategories()
            val result =
                database.withTransaction {
                    val category = resolveCategory(input.categoryId) ?: return@withTransaction null
                    val now = System.currentTimeMillis()
                    val sessionId = input.sessionId?.trim()?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
                    val ledgerId = if (input.createLedger) UUID.randomUUID().toString() else null
                    val categoryName = input.categoryNameSnapshot?.trim()?.takeIf { it.isNotBlank() } ?: displayName(category)
                    val ledgerDate =
                        input.ledgerDate?.trim()?.takeIf { it.isNotBlank() }
                            ?: ArchiveDateUtils.formatDate(
                                BusinessDay.dateAt(input.ledgerOccurredAt ?: input.completedAt ?: input.startedAt, zoneId, preferences.getDayBoundaryHourOnce()),
                            )
                    if (input.createLedger && ledgerId != null) {
                        pointLedgerDao.insertIgnore(
                            PointLedgerEntity(
                                id = ledgerId,
                                occurredAt = input.ledgerOccurredAt ?: input.completedAt ?: now,
                                ledgerDate = ledgerDate,
                                entryType = PointLedgerEntryType.OFFLINE_FOCUS,
                                deltaPoints = input.pointsAwarded,
                                sourceRefId = sourceRefId(sessionId),
                                messageKey = "ledger_offline_focus",
                                messageArgsJson =
                                    JSONArray(
                                        listOf(
                                            categoryName,
                                            (input.actualDurationMillis / MINUTE_MILLIS).coerceAtLeast(0L),
                                        ),
                                    ).toString(),
                                note = categoryName,
                                createdAt = input.createdAt,
                            ),
                        )
                    }
                    val session =
                        OfflineFocusSessionEntity(
                            id = sessionId,
                            categoryId = category.id,
                            categoryNameSnapshot = categoryName,
                            categoryIconKeySnapshot = input.categoryIconKeySnapshot?.trim()?.takeIf { it.isNotBlank() } ?: category.iconKey,
                            categoryCustomIconPathSnapshot = input.categoryCustomIconPathSnapshot?.trim()?.takeIf { it.isNotBlank() },
                            categoryColorArgbSnapshot = input.categoryColorArgbSnapshot ?: category.colorArgb,
                            pointsPerMinuteSnapshot = input.pointsPerMinuteSnapshot,
                            plannedDurationMillis = input.plannedDurationMillis,
                            actualDurationMillis = input.actualDurationMillis,
                            status = input.status,
                            focusMode = input.focusMode,
                            startedAt = input.startedAt,
                            pausedAt = input.pausedAt,
                            resumedAt = input.resumedAt,
                            completedAt = input.completedAt,
                            abandonedAt = input.abandonedAt,
                            pauseReason = input.pauseReason,
                            abandonedReason = input.abandonedReason,
                            violationStartedAt = input.violationStartedAt,
                            violationPackageName = input.violationPackageName?.trim()?.takeIf { it.isNotBlank() },
                            pointsAwarded = input.pointsAwarded,
                            settledLedgerId = ledgerId,
                            createdAt = input.createdAt,
                            updatedAt = input.updatedAt,
                        )
                    sessionDao.insert(session)
                    OfflineFocusFinishResult(
                        session = session,
                        insertedLedger = input.createLedger,
                        awardedPoints = input.pointsAwarded,
                    )
                }
            result ?: return@withContext null
            if (result.insertedLedger && result.awardedPoints != 0.0) {
                preferences.addUserPoints(result.awardedPoints)
                AppLimitRepository(context, database).checkAchievements()
            }
            toSession(result.session)
        }
    }

    private suspend fun finishSession(
        sessionId: String,
        nowMillis: Long,
        forceComplete: Boolean,
    ): OfflineFocusSession? {
        return withContext(Dispatchers.IO) {
            val dayBoundaryHour = preferences.getDayBoundaryHourOnce()
            val dailyCap = preferences.getOfflineFocusDailyPointCapOnce().toDouble()
            val result =
                database.withTransaction {
                    val current = sessionDao.getById(sessionId) ?: return@withTransaction null
                    if (current.status != OfflineFocusSessionStatus.RUNNING && current.status != OfflineFocusSessionStatus.PAUSED) {
                        return@withTransaction OfflineFocusFinishResult(
                            session = current,
                            insertedLedger = false,
                            awardedPoints = 0.0,
                        )
                    }

                    val actualMillis = elapsedMillis(current, nowMillis)
                    val completionRatio =
                        if (current.plannedDurationMillis <= 0L) {
                            0f
                        } else {
                            actualMillis.toFloat() / current.plannedDurationMillis.toFloat()
                        }
                    val shouldAward = forceComplete || completionRatio >= EARLY_COMPLETE_THRESHOLD
                    if (!shouldAward) {
                        val abandoned =
                            current.copy(
                                actualDurationMillis = actualMillis,
                                status = OfflineFocusSessionStatus.ABANDONED,
                                abandonedReason = OfflineFocusAbandonReason.BELOW_THRESHOLD,
                                abandonedAt = nowMillis,
                                pointsAwarded = 0.0,
                                updatedAt = nowMillis,
                            )
                        sessionDao.update(abandoned)
                        return@withTransaction OfflineFocusFinishResult(
                            session = abandoned,
                            insertedLedger = false,
                            awardedPoints = 0.0,
                        )
                    }

                    val actualForPoints =
                        if (forceComplete) {
                            current.plannedDurationMillis.coerceAtMost(actualMillis.coerceAtLeast(current.plannedDurationMillis))
                        } else {
                            actualMillis
                        }
                    val sourceRefId = sourceRefId(sessionId)
                    val existingLedger = pointLedgerDao.getBySourceRefId(sourceRefId)
                    val ledgerId = existingLedger?.id ?: UUID.randomUUID().toString()
                    var insertedLedger = false
                    val awardPoints =
                        if (existingLedger != null) {
                            existingLedger.deltaPoints
                        } else {
                            val rawPoints =
                                ((actualForPoints / MINUTE_MILLIS).toDouble() * current.pointsPerMinuteSnapshot)
                                    .coerceAtLeast(0.0)
                            val ledgerDate =
                                ArchiveDateUtils.formatDate(
                                    BusinessDay.dateAt(nowMillis, zoneId, dayBoundaryHour),
                                )
                            val alreadyAwarded = pointLedgerDao.sumOfflineFocusEarnedByDate(ledgerDate)
                            val cappedPoints = rawPoints.coerceAtMost((dailyCap - alreadyAwarded).coerceAtLeast(0.0))
                            val entry =
                                PointLedgerEntity(
                                    id = ledgerId,
                                    occurredAt = nowMillis,
                                    ledgerDate = ledgerDate,
                                    entryType = PointLedgerEntryType.OFFLINE_FOCUS,
                                    deltaPoints = cappedPoints,
                                    sourceRefId = sourceRefId,
                                    messageKey = "ledger_offline_focus",
                                    messageArgsJson =
                                        JSONArray(
                                            listOf(
                                                current.categoryNameSnapshot,
                                                actualForPoints / MINUTE_MILLIS,
                                            ),
                                        ).toString(),
                                    note = current.categoryNameSnapshot,
                                    createdAt = nowMillis,
                                )
                            insertedLedger = pointLedgerDao.insertIgnore(entry) != -1L
                            cappedPoints
                        }
                    val completed =
                        current.copy(
                            actualDurationMillis = actualForPoints,
                            status = OfflineFocusSessionStatus.SETTLED,
                            completedAt = nowMillis,
                            pointsAwarded = awardPoints,
                            settledLedgerId = ledgerId,
                            updatedAt = nowMillis,
                        )
                    sessionDao.update(completed)
                    OfflineFocusFinishResult(
                        session = completed,
                        insertedLedger = insertedLedger,
                        awardedPoints = awardPoints,
                    )
                } ?: return@withContext null
            if (result.insertedLedger && result.awardedPoints > 0.0) {
                preferences.addUserPoints(result.awardedPoints)
                AppLimitRepository(context, database).checkAchievements()
            }
            toSession(result.session)
        }
    }

    private suspend fun resolveCategory(categoryId: String?): OfflineFocusCategoryEntity? {
        val candidates = categoryDao.getAll(includeArchived = false)
        val selected = categoryId?.let { id -> candidates.firstOrNull { it.id == id } }
        return selected ?: candidates.firstOrNull { it.id == DEFAULT_CATEGORY_ID } ?: candidates.firstOrNull()
    }

    private fun elapsedMillis(
        session: OfflineFocusSessionEntity,
        nowMillis: Long,
    ): Long {
        val referenceNow =
            if (session.status == OfflineFocusSessionStatus.PAUSED) {
                session.pausedAt ?: nowMillis
            } else {
                nowMillis
            }
        val end = maxOf(referenceNow, session.startedAt)
        return (end - session.startedAt).coerceIn(0L, session.plannedDurationMillis)
    }

    private fun buildSummary(
        sessions: List<OfflineFocusSessionEntity>,
        windowStartMillis: Long? = null,
        windowEndMillis: Long? = null,
    ): OfflineFocusTodaySummary {
        val completed =
            sessions.filter {
                it.status == OfflineFocusSessionStatus.COMPLETED ||
                    it.status == OfflineFocusSessionStatus.SETTLED
            }
        val clippedDurations =
            completed
                .associateWith { session ->
                    clippedCompletedDurationMillis(session, windowStartMillis, windowEndMillis)
                }
                .filterValues { it > 0L }
        val totalMillis = clippedDurations.values.sum()
        val summaries =
            completed
                .filter { clippedDurations.containsKey(it) }
                .groupBy { it.categoryId }
                .map { (_, items) ->
                    val first = items.first()
                    OfflineFocusCategorySummary(
                        categoryName = first.categoryNameSnapshot,
                        iconKey = first.categoryIconKeySnapshot,
                        customIconPath = first.categoryCustomIconPathSnapshot,
                        colorArgb = first.categoryColorArgbSnapshot,
                        totalMillis = items.sumOf { clippedDurations[it] ?: 0L },
                        completedCount = items.size,
                        pointsAwarded = items.sumOf { pointsAwardedInWindow(it, windowStartMillis, windowEndMillis) },
                    )
                }
                .sortedByDescending { it.totalMillis }
        return OfflineFocusTodaySummary(
            totalMillis = totalMillis,
            completedCount = clippedDurations.size,
            pointsAwarded = completed.sumOf { pointsAwardedInWindow(it, windowStartMillis, windowEndMillis) },
            sessions = sessions.map(::toSession),
            categories = summaries,
        )
    }

    private fun pointsAwardedInWindow(
        session: OfflineFocusSessionEntity,
        windowStartMillis: Long?,
        windowEndMillis: Long?,
    ): Double {
        if (windowStartMillis == null || windowEndMillis == null) return session.pointsAwarded
        val awardedAt = session.completedAt ?: return 0.0
        return if (awardedAt >= windowStartMillis && awardedAt < windowEndMillis) {
            session.pointsAwarded
        } else {
            0.0
        }
    }

    private fun clippedCompletedDurationMillis(
        session: OfflineFocusSessionEntity,
        windowStartMillis: Long?,
        windowEndMillis: Long?,
    ): Long {
        val duration = session.actualDurationMillis.coerceAtLeast(0L)
        if (duration == 0L) return 0L
        if (windowStartMillis == null || windowEndMillis == null) return duration
        val actualEnd =
            (session.completedAt ?: session.abandonedAt ?: (session.startedAt + duration))
                .coerceAtMost(session.startedAt + duration)
        val actualStart = actualEnd - duration
        val clippedStart = maxOf(actualStart, windowStartMillis)
        val clippedEnd = minOf(actualEnd, windowEndMillis)
        return (clippedEnd - clippedStart).coerceAtLeast(0L)
    }

    private fun toCategory(entity: OfflineFocusCategoryEntity): OfflineFocusCategory =
        OfflineFocusCategory(
            id = entity.id,
            name = displayName(entity),
            iconKey = entity.iconKey,
            customIconPath = entity.customIconPath,
            colorArgb = entity.colorArgb,
            pointsPerMinute = entity.pointsPerMinute,
            sortOrder = entity.sortOrder,
            isBuiltIn = entity.isBuiltIn,
            isArchived = entity.isArchived,
            isDeleted = entity.isDeleted,
        )

    private fun toSession(entity: OfflineFocusSessionEntity): OfflineFocusSession =
        OfflineFocusSession(
            id = entity.id,
            categoryId = entity.categoryId,
            categoryName = entity.categoryNameSnapshot,
            iconKey = entity.categoryIconKeySnapshot,
            customIconPath = entity.categoryCustomIconPathSnapshot,
            colorArgb = entity.categoryColorArgbSnapshot,
            pointsPerMinute = entity.pointsPerMinuteSnapshot,
            plannedDurationMillis = entity.plannedDurationMillis,
            actualDurationMillis = entity.actualDurationMillis,
            status = entity.status,
            focusMode = entity.focusMode,
            pauseReason = entity.pauseReason,
            abandonedReason = entity.abandonedReason,
            violationPackageName = entity.violationPackageName,
            startedAt = entity.startedAt,
            pausedAt = entity.pausedAt,
            completedAt = entity.completedAt,
            abandonedAt = entity.abandonedAt,
            pointsAwarded = entity.pointsAwarded,
        )

    private fun displayName(category: OfflineFocusCategoryEntity): String {
        val definition = builtInDefinitions.firstOrNull { it.id == category.id }
        val isUnmodifiedBuiltInName = definition != null && category.name == definition.fallbackName
        return if (isUnmodifiedBuiltInName) {
            definition.labelKey.let(AppText::t).takeIf { it.isNotBlank() } ?: category.name
        } else {
            category.name
        }
    }

    companion object {
        const val DEFAULT_CATEGORY_ID = "offline_focus_reading"
        private const val MINUTE_MILLIS = 60_000L
        private const val EARLY_COMPLETE_THRESHOLD = 0.8f
        private const val MIN_POINTS_PER_MINUTE = 0.0
        private const val MAX_POINTS_PER_MINUTE = 20.0
        private const val DEFAULT_CUSTOM_ICON_KEY = "custom"
        private const val INPUT_METHOD_PACKAGE_CACHE_MILLIS = 60_000L

        fun sourceRefId(sessionId: String): String = "offline_focus:$sessionId"

        private val builtInDefinitions =
            listOf(
                BuiltInOfflineFocusCategory("offline_focus_reading", "Reading", "focus_icon_words", "offline_focus_category_reading", 0xFFFF6161.toInt(), 0),
                BuiltInOfflineFocusCategory("offline_focus_fitness", "Fitness", "focus_icon_morning", "offline_focus_category_fitness", 0xFF35D870.toInt(), 1),
            )

        private val knownHomeLauncherPackages =
            setOf(
                "com.miui.home",
                "com.android.launcher",
                "com.android.launcher3",
                "com.google.android.apps.nexuslauncher",
                "com.huawei.android.launcher",
                "com.hihonor.android.launcher",
                "com.oppo.launcher",
                "com.vivo.launcher",
                "com.sec.android.app.launcher",
                "com.motorola.launcher3",
                "miui.systemui.plugin",
            )
    }
}

private data class BuiltInOfflineFocusCategory(
    val id: String,
    val fallbackName: String,
    val iconKey: String,
    val labelKey: String,
    val colorArgb: Int,
    val sortOrder: Int,
)
