package dev.yakitori.barkforwarder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.yakitori.barkforwarder.BarkBridgeApp

class InstalledAppSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            BarkBridgeApp.from(applicationContext).container.installedAppRepository.refreshInstalledApps()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "installed-app-sync"
    }
}

