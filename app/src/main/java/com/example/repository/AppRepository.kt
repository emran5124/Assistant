package com.example.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.data.LauncherPreferences
import com.example.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val preferences: LauncherPreferences
) {
    private val packageManager: PackageManager = context.packageManager

    // Fetch list of installed, launchable applications (excluding self and system background apps)
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        val seenPackages = mutableSetOf<String>()
        val appsList = mutableListOf<AppInfo>()

        val ourPackage = context.packageName

        for (info in resolveInfos) {
            val packageName = info.activityInfo.packageName
            if (packageName == ourPackage) continue // Skip our own launcher

            if (seenPackages.add(packageName)) {
                val label = info.loadLabel(packageManager).toString()
                
                var firstInstallTime = 0L
                var lastUpdateTime = 0L
                try {
                    val packageInfo = packageManager.getPackageInfo(packageName, 0)
                    firstInstallTime = packageInfo.firstInstallTime
                    lastUpdateTime = packageInfo.lastUpdateTime
                } catch (e: Exception) {
                    // Fail-safe default
                }

                appsList.add(
                    AppInfo(
                        packageName = packageName,
                        label = label,
                        firstInstallTime = firstInstallTime,
                        lastUpdateTime = lastUpdateTime
                    )
                )
            }
        }
        appsList
    }

    // Directly fetch AppInfo for a list of packages. Extremely fast for showing favorite apps instantly.
    suspend fun getAppInfosForPackages(packages: List<String>): List<AppInfo> = withContext(Dispatchers.IO) {
        packages.mapNotNull { packageName ->
            try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val appInfo = packageInfo.applicationInfo ?: return@mapNotNull null
                val label = appInfo.loadLabel(packageManager).toString()
                AppInfo(
                    packageName = packageName,
                    label = label,
                    firstInstallTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Resolve an app's icon lazily from PackageManager
    fun getAppIcon(packageName: String): android.graphics.drawable.Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    // Launch application by package name
    fun launchApp(packageName: String): Boolean {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }

    // Get favorite package names flow
    fun getFavorites(): Flow<List<String>> = preferences.favoritesFlow

    // Save favorite package names list
    suspend fun saveFavorites(favorites: List<String>) {
        preferences.saveFavorites(favorites)
    }

    // Record an app launch to track "most recently used" locally
    suspend fun recordAppLaunch(packageName: String) {
        preferences.recordAppLaunch(packageName)
    }

    // Sort apps helper function
    fun sortApps(
        apps: List<AppInfo>,
        sortBy: SortType,
        favoritesOrder: List<String>,
        launchHistory: Map<String, Long>
    ): List<AppInfo> {
        return when (sortBy) {
            SortType.ALPHABETICAL_AZ -> apps.sortedBy { it.label.lowercase() }
            SortType.ALPHABETICAL_ZA -> apps.sortedByDescending { it.label.lowercase() }
            SortType.RECENTLY_UPDATED -> apps.sortedByDescending { it.lastUpdateTime }
            SortType.INSTALL_DATE -> apps.sortedByDescending { it.firstInstallTime }
            SortType.MOST_RECENT_USED -> apps.sortedByDescending { launchHistory[it.packageName] ?: 0L }
            SortType.MANUAL -> {
                // Order according to favoritesOrder, and append rest alphabetically
                val orderMap = favoritesOrder.withIndex().associate { it.value to it.index }
                apps.sortedWith(
                    compareBy<AppInfo> { orderMap[it.packageName] ?: Int.MAX_VALUE }
                        .thenBy { it.label.lowercase() }
                )
            }
        }
    }
}

enum class SortType {
    ALPHABETICAL_AZ,
    ALPHABETICAL_ZA,
    RECENTLY_UPDATED,
    INSTALL_DATE,
    MOST_RECENT_USED,
    MANUAL
}
