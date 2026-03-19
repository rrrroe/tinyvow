package com.rrrrz.tinyvow.data.accessibility

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

class AccessibilityServiceStateChecker(
    private val context: Context,
) {
    fun isEnabled(serviceClass: Class<*>): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        val expectedComponent = ComponentName(context, serviceClass).flattenToString()
        return enabledServices
            .split(':')
            .any { it.equals(expectedComponent, ignoreCase = true) }
    }
}
