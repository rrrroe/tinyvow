package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyAppTimeSliceArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity

/**
 * An in-memory projection of the current, unfinished business day.
 *
 * This deliberately reuses the archived report row shapes so the report builders can consume
 * finalized and live data through one path. It must never be written to the archive tables.
 */
data class LiveDayReportSnapshot(
    val archive: DailyArchiveEntity,
    val groupArchives: List<DailyGroupArchiveEntity>,
    val appArchives: List<DailyAppArchiveEntity>,
    val timeSliceArchives: List<DailyAppTimeSliceArchiveEntity>,
    val capturedAt: Long,
)
