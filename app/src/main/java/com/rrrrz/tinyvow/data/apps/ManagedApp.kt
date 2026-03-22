package com.rrrrz.tinyvow.data.apps

data class ManagedApp(
    val packageName: String,
    val appName: String,
    val isLaunchable: Boolean = false,
    val usageTimeInMs: Long = 0,
)
