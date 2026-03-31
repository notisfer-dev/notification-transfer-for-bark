package dev.yakitori.barkforwarder.domain

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import dev.yakitori.barkforwarder.worker.ForwardDeliveryWorker
import dev.yakitori.barkforwarder.worker.InstalledAppSyncWorker
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

object ForwardingScheduler {
    private val json = Json { ignoreUnknownKeys = true }

    fun enqueue(context: Context, event: ForwardEvent) {
        val request = OneTimeWorkRequestBuilder<ForwardDeliveryWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ForwardDeliveryWorker.KEY_EVENT_JSON, json.encodeToString(event))
                    .build(),
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun enqueueInstalledAppSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<InstalledAppSyncWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            InstalledAppSyncWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}

