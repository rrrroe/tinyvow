package com.rrrrz.tinyvow.ui.home

internal fun <T> mergeLiveDayRows(
    archivedRows: List<T>,
    liveDate: String?,
    liveRows: List<T>,
    archiveDateOf: (T) -> String,
): List<T> {
    if (liveDate == null) return archivedRows
    return archivedRows.filterNot { archiveDateOf(it) == liveDate } + liveRows
}
