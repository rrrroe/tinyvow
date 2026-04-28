package com.rrrrz.tinyvow.data.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

class AccessibilityServiceStateChecker(
    private val context: Context,
) {
    fun isEnabled(serviceClass: Class<*>): Boolean {
        val expectedComponent = ComponentName(context, serviceClass)
        if (isEnabledByAccessibilityManager(expectedComponent)) {
            return true
        }

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        val expectedNames = setOf(
            expectedComponent.flattenToString(),
            expectedComponent.flattenToShortString(),
            "${expectedComponent.packageName}/${expectedComponent.className}",
        )

        return enabledServices
            .split(':')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .any { enabled ->
                val component = ComponentName.unflattenFromString(enabled)
                component == expectedComponent ||
                    expectedNames.any { it.equals(enabled, ignoreCase = true) }
            }
    }

    private fun isEnabledByAccessibilityManager(expectedComponent: ComponentName): Boolean {
        val manager = context.getSystemService(AccessibilityManager::class.java) ?: return false
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { serviceInfo ->
                val service = serviceInfo.resolveInfo?.serviceInfo ?: return@any false
                service.packageName == expectedComponent.packageName &&
                    service.name == expectedComponent.className
            }
    }
}
