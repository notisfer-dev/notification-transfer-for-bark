package dev.yakitori.barkforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dev.yakitori.barkforwarder.data.model.EventType
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import dev.yakitori.barkforwarder.domain.ForwardingScheduler
import dev.yakitori.barkforwarder.domain.NotificationFingerprint

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return

        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        val sender = messages.firstOrNull()?.displayOriginatingAddress.orEmpty()
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context)

        val event = ForwardEvent(
            type = EventType.SMS,
            packageName = defaultSmsPackage,
            sourceLabel = "SMS",
            title = sender,
            body = body,
            sender = sender,
            phoneNumber = sender,
            postedAt = System.currentTimeMillis(),
            dedupeKey = NotificationFingerprint.fromParts("sms", sender, body),
        )
        ForwardingScheduler.enqueue(context, event)
    }
}

