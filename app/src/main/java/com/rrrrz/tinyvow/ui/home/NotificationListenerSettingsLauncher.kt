package com.rrrrz.tinyvow.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import java.util.Locale

fun Context.openNotificationListenerSettings(componentName: ComponentName? = null) {
    notificationListenerSettingsIntents(componentName).firstOrNull { tryStartActivity(it) }
}

private fun Context.notificationListenerSettingsIntents(componentName: ComponentName?): List<Intent> =
    buildList {
        when (notificationListenerSettingsStrategy()) {
            NotificationListenerSettingsStrategy.SPECIAL_ACCESS_FIRST -> {
                addSpecialAccessEntrances()
                addNotificationListenerEntrances(componentName)
            }
            NotificationListenerSettingsStrategy.STANDARD_FIRST -> {
                addNotificationListenerEntrances(componentName)
                addSpecialAccessEntrances()
            }
        }
        add(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                Uri.fromParts("package", packageName, null),
            ),
        )
        add(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        add(Intent(Settings.ACTION_SETTINGS))
    }

private fun MutableList<Intent>.addNotificationListenerEntrances(componentName: ComponentName?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && componentName != null) {
        add(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
                Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                componentName.flattenToString(),
            ),
        )
    }
    add(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
}

private fun MutableList<Intent>.addSpecialAccessEntrances() {
    add(Intent(ACTION_MANAGE_SPECIAL_APP_ACCESSES))
    add(Intent(ACTION_NOTIFICATION_SETTINGS))
    add(Intent(Settings.ACTION_SECURITY_SETTINGS))
}

private fun notificationListenerSettingsStrategy(): NotificationListenerSettingsStrategy {
    val manufacturer = Build.MANUFACTURER.normalizedDeviceName()
    val brand = Build.BRAND.normalizedDeviceName()
    val customRomBrands =
        setOf(
            "vivo",
            "iqoo",
            "oppo",
            "realme",
            "oneplus",
            "xiaomi",
            "redmi",
            "poco",
            "honor",
            "meizu",
            "lenovo",
            "motorola",
            "zte",
            "nubia",
        )
    return if (manufacturer in customRomBrands || brand in customRomBrands) {
        NotificationListenerSettingsStrategy.SPECIAL_ACCESS_FIRST
    } else {
        NotificationListenerSettingsStrategy.STANDARD_FIRST
    }
}

private enum class NotificationListenerSettingsStrategy {
    SPECIAL_ACCESS_FIRST,
    STANDARD_FIRST,
}

private fun String?.normalizedDeviceName(): String =
    orEmpty().trim().lowercase(Locale.US)

private const val ACTION_MANAGE_SPECIAL_APP_ACCESSES = "android.settings.MANAGE_SPECIAL_APP_ACCESSES"
private const val ACTION_NOTIFICATION_SETTINGS = "android.settings.NOTIFICATION_SETTINGS"

private fun Context.tryStartActivity(intent: Intent): Boolean {
    val launchIntent =
        Intent(intent).apply {
            if (findActivity() == null) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    return runCatching {
        startActivity(launchIntent)
        true
    }.getOrDefault(false)
}

private fun Context.findActivity(): ComponentActivity? =
    when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
