package com.rrrrz.tinyvow.data.apps

data class ManagedApp(
    val packageName: String,
    val appName: String,
    val appNameZh: String? = null,
    val appNameEn: String? = null,
    val isLaunchable: Boolean = false,
    val usageTimeInMs: Long = 0,
)

fun ManagedApp.matchesSearchQuery(query: String): Boolean {
    val keyword = query.trim()
    if (keyword.isEmpty()) return true
    return sequenceOf(packageName, appName, appNameZh, appNameEn)
        .filterNotNull()
        .any { it.contains(keyword, ignoreCase = true) }
}
