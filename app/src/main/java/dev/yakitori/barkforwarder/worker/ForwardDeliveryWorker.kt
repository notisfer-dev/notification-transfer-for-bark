package dev.yakitori.barkforwarder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.yakitori.barkforwarder.BarkBridgeApp
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import kotlinx.serialization.json.Json

class ForwardDeliveryWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val appContainer = BarkBridgeApp.from(context).container
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val eventJson = inputData.getString(KEY_EVENT_JSON) ?: return Result.failure()
        val event = runCatching { json.decodeFromString<ForwardEvent>(eventJson) }.getOrElse { return Result.failure() }

        return try {
            appContainer.forwardingEngine.process(event)
            Result.success()
        } catch (error: IllegalArgumentException) {
            Result.failure()
        } catch (error: IllegalStateException) {
            Result.failure()
        } catch (error: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_EVENT_JSON = "event_json"
    }
}

