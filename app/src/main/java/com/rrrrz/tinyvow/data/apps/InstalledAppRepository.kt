package com.rrrrz.tinyvow.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.app.usage.UsageStatsManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class InstalledAppRepository(
    private val context: Context,
) {
    suspend fun getLaunchableApps(): List<ManagedApp> = withContext(Dispatchers.Default) {
        val packageManager = context.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvedActivities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launchIntent,
                PackageManager.ResolveInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launchIntent, 0)
        }

        resolvedActivities
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == context.packageName) {
                    return@mapNotNull null
                }

                val appName = resolveInfo.loadLabel(packageManager)
                    ?.toString()
                    ?.takeIf { it.isNotBlank() }
                    ?: packageName

                ManagedApp(
                    packageName = packageName,
                    appName = appName,
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    suspend fun getAllInstalledApps(): List<ManagedApp> = withContext(Dispatchers.Default) {
        val packageManager = context.packageManager
        
        // Step 1: 获取过去 7 天的应用使用时长，用于排序
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        val usageStats = usageStatsManager?.queryAndAggregateUsageStats(startTime, endTime) ?: emptyMap()

        // Step 2: 拉取所有具有 Launcher 的应用，确定哪些是“桌面显示的名字”
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val launchActivities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(launchIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launchIntent, 0)
        }
        
        val launchablePackageNames = launchActivities.mapNotNull { it.activityInfo?.packageName }.toSet()
        val launchableNames = launchActivities.associateBy(
            keySelector = { it.activityInfo?.packageName ?: "" },
            valueTransform = { it.loadLabel(packageManager).toString() }
        )

        // Step 3: 只合并可启动应用和 UsageStats 已出现过的应用，避免使用 QUERY_ALL_PACKAGES。
        val scopedPackageNames = (launchablePackageNames + usageStats.keys)

        scopedPackageNames
            .mapNotNull { packageName ->
                if (packageName == context.packageName) return@mapNotNull null

                // 优先使用 Launcher 里的名字，如果没有则用 AppInfo 的 label
                val appName = launchableNames[packageName] 
                    ?: loadAppLabel(packageManager, packageName)
                    ?: packageName

                val totalTime = usageStats[packageName]?.totalTimeInForeground ?: 0L

                ManagedApp(
                    packageName = packageName,
                    appName = appName,
                    isLaunchable = launchablePackageNames.contains(packageName),
                    usageTimeInMs = totalTime
                )
            }
            .distinctBy { it.packageName }
            // 排序规则：首先按使用频率越高（时长越长）排在前面，如果时长相同或没有时长，则按 Launcher 属性和名称
            .sortedWith(
                compareByDescending<ManagedApp> { it.usageTimeInMs }
                    .thenByDescending { it.isLaunchable }
                    .thenBy { it.appName.lowercase() }
            )
    }

    private fun loadAppLabel(
        packageManager: PackageManager,
        packageName: String,
    ): String? {
        return runCatching {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            appInfo.loadLabel(packageManager).toString().takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
