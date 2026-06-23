package com.rrrrz.tinyvow.ui.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.LruCache

internal object AppVisualCache {
    private val iconCache = LruCache<String, Drawable>(128)
    private val missingIcons = linkedSetOf<String>()

    @Synchronized
    fun getIcon(context: Context, packageName: String): Drawable? {
        iconCache.get(packageName)?.let { return it }
        if (packageName in missingIcons) return null
        val icon =
            runCatching {
                context.applicationContext.packageManager.getApplicationIcon(packageName)
            }.getOrNull()
        if (icon == null) {
            missingIcons += packageName
            if (missingIcons.size > 256) {
                missingIcons.remove(missingIcons.first())
            }
        } else {
            iconCache.put(packageName, icon)
        }
        return icon
    }
}
