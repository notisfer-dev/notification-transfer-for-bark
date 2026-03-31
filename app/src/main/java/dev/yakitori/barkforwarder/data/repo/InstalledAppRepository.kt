package dev.yakitori.barkforwarder.data.repo

import android.content.Context
import android.os.Build
import dev.yakitori.barkforwarder.data.model.InstalledApp

class InstalledAppRepository(
    private val context: Context,
    private val appRuleRepository: AppRuleRepository,
) {
    fun observeRules() = appRuleRepository.observeRules()

    suspend fun refreshInstalledApps() {
        appRuleRepository.syncInstalledApps(loadInstalledApps())
    }

    private fun loadInstalledApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(android.content.pm.PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(0)
        }

        return apps
            .map { info ->
                InstalledApp(
                    packageName = info.packageName,
                    appLabel = packageManager.getApplicationLabel(info).toString(),
                )
            }
            .sortedBy { it.appLabel.lowercase() }
    }
}

