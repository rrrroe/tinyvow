package com.rrrrz.tinyvow.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.app.usage.UsageStatsManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class InstalledAppRepository(
    private val context: Context,
) {
    private val chinesePackageManager: PackageManager by lazy {
        localizedPackageManager(Locale.SIMPLIFIED_CHINESE)
    }
    private val englishPackageManager: PackageManager by lazy {
        localizedPackageManager(Locale.ENGLISH)
    }

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
        val chineseLaunchableNames = loadLaunchableNames(chinesePackageManager, launchIntent)
        val englishLaunchableNames = loadLaunchableNames(englishPackageManager, launchIntent)

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
                    appNameZh = chineseLaunchableNames[packageName]
                        ?: loadAppLabel(chinesePackageManager, packageName),
                    appNameEn = englishLaunchableNames[packageName]
                        ?: loadAppLabel(englishPackageManager, packageName),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    suspend fun getAllInstalledApps(usageLookbackDays: Int = DEFAULT_USAGE_LOOKBACK_DAYS): List<ManagedApp> = withContext(Dispatchers.Default) {
        val packageManager = context.packageManager
        
        // Step 1: 获取指定窗口内的应用使用时长，用于排序
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -usageLookbackDays.coerceAtLeast(1))
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
        val chineseLaunchableNames = loadLaunchableNames(chinesePackageManager, launchIntent)
        val englishLaunchableNames = loadLaunchableNames(englishPackageManager, launchIntent)

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
                    appNameZh = chineseLaunchableNames[packageName]
                        ?: loadAppLabel(chinesePackageManager, packageName),
                    appNameEn = englishLaunchableNames[packageName]
                        ?: loadAppLabel(englishPackageManager, packageName),
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

    private fun loadLaunchableNames(
        packageManager: PackageManager,
        launchIntent: Intent,
    ): Map<String, String> {
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launchIntent,
                PackageManager.ResolveInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launchIntent, 0)
        }
        return activities.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
            val label = resolveInfo.loadLabel(packageManager)
                ?.toString()
                ?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            packageName to label
        }.toMap()
    }

    private fun localizedPackageManager(locale: Locale): PackageManager {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(configuration).packageManager
    }

    companion object {
        const val DEFAULT_USAGE_LOOKBACK_DAYS = 7
        const val APP_COLOR_USAGE_LOOKBACK_DAYS = 30
    }
}
