package dev.yakitori.barkforwarder.domain

import dev.yakitori.barkforwarder.data.model.BarkConfig
import dev.yakitori.barkforwarder.data.model.CaptureSource
import dev.yakitori.barkforwarder.data.model.CryptoConfig
import dev.yakitori.barkforwarder.data.model.EventType
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import dev.yakitori.barkforwarder.data.model.NotificationFilterConfig
import dev.yakitori.barkforwarder.data.prefs.SettingsRepository
import dev.yakitori.barkforwarder.data.repo.AppRuleRepository
import dev.yakitori.barkforwarder.data.repo.DedupeRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class ForwardingEngine(
    private val settingsRepository: SettingsRepository,
    private val appRuleRepository: AppRuleRepository,
    private val dedupeRepository: DedupeRepository,
    private val playIconResolver: PlayIconResolver,
    private val barkPushClient: BarkPushClient,
) {
    private val json = Json { explicitNulls = false }

    suspend fun process(event: ForwardEvent) {
        val barkConfig = settingsRepository.barkConfig.first()
        val cryptoConfig = settingsRepository.cryptoConfig.first()
        val filterConfig = settingsRepository.notificationFilterConfig.first()

        if (!shouldHandleEvent(event, barkConfig)) return
        if (barkConfig.deviceKey.isBlank()) return
        if (cryptoConfig.key.isBlank() || cryptoConfig.fixedIv.isBlank()) return

        val packageName = event.packageName
        if (!packageName.isNullOrBlank()) {
            appRuleRepository.ensureRule(packageName, event.sourceLabel)
            val rule = appRuleRepository.getRule(packageName)
            if (rule?.excluded == true) return
        }

        val dedupeWindow = dedupeWindowSeconds(event, filterConfig)
        if (!dedupeRepository.shouldForward(
                dedupeKey = event.dedupeKey,
                relatedKeys = event.dedupeAliases,
                eventType = event.type,
                windowSeconds = dedupeWindow,
                nowMillis = event.postedAt,
            )
        ) return

        val iconUrl = when {
            !event.iconUrl.isNullOrBlank() -> event.iconUrl
            packageName.isNullOrBlank() -> null
            else -> playIconResolver.resolve(packageName)
        }

        val payload = createPayload(event, iconUrl, barkConfig)
        val payloadJson = json.encodeToString(payload)
        val iv = cryptoConfig.fixedIv
        val ciphertext = BarkCrypto.encrypt(payloadJson, cryptoConfig.key, iv)
        barkPushClient.sendEncrypted(
            serverUrl = barkConfig.serverUrl,
            deviceKey = barkConfig.deviceKey,
            ciphertext = ciphertext,
            iv = iv,
        )
    }

    private fun shouldHandleEvent(event: ForwardEvent, barkConfig: BarkConfig): Boolean {
        return when (event.type) {
            EventType.NOTIFICATION -> barkConfig.notificationsEnabled
            EventType.SMS -> barkConfig.smsEnabled
            EventType.INCOMING_CALL,
            EventType.MISSED_CALL,
            -> barkConfig.callsEnabled
        }
    }

    private fun dedupeWindowSeconds(event: ForwardEvent, filterConfig: NotificationFilterConfig): Int {
        return when {
            event.type == EventType.NOTIFICATION -> filterConfig.duplicateWindowSeconds
            event.captureSource == CaptureSource.NOTIFICATION_FALLBACK -> FALLBACK_DEDUPE_SECONDS
            else -> 0
        }
    }

    private fun createPayload(event: ForwardEvent, iconUrl: String?, barkConfig: BarkConfig): BarkPushPayload {
        val titleBody = when (event.type) {
            EventType.NOTIFICATION -> {
                val lines = listOfNotNull(
                    event.title.takeIf { it.isNotBlank() && it != event.sourceLabel },
                    event.body.takeIf { it.isNotBlank() },
                )
                event.sourceLabel to lines.joinToString("\n").ifBlank { "Notification received" }
            }

            EventType.SMS -> {
                val sender = event.sender ?: event.phoneNumber ?: "Unknown sender"
                "SMS: $sender" to event.body.ifBlank { event.title.ifBlank { "New SMS received" } }
            }

            EventType.INCOMING_CALL -> {
                "Incoming call" to (event.sender ?: event.phoneNumber ?: "Unknown caller")
            }

            EventType.MISSED_CALL -> {
                "Missed call" to (event.sender ?: event.phoneNumber ?: "Unknown caller")
            }
        }

        val group = when (event.type) {
            EventType.NOTIFICATION -> event.packageName ?: "notifications"
            EventType.SMS -> "sms"
            EventType.INCOMING_CALL,
            EventType.MISSED_CALL,
            -> "calls"
        }

        val level = if (event.type == EventType.INCOMING_CALL) "timeSensitive" else "active"
        val call = if (event.type == EventType.INCOMING_CALL && barkConfig.ringIncomingCalls && event.shouldRing) 1 else null

        return BarkPushPayload(
            title = titleBody.first,
            body = titleBody.second,
            group = group,
            level = level,
            call = call,
            icon = iconUrl,
            image = event.imageUrl,
        )
    }

    private companion object {
        const val FALLBACK_DEDUPE_SECONDS = 10
    }
}
