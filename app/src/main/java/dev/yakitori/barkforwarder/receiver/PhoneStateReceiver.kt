package dev.yakitori.barkforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import dev.yakitori.barkforwarder.data.model.EventType
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import dev.yakitori.barkforwarder.domain.ForwardingScheduler
import dev.yakitori.barkforwarder.domain.NotificationFingerprint
import dev.yakitori.barkforwarder.domain.PhoneCallStateStore

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        val tracker = PhoneCallStateStore(context)
        val dialerPackage = context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                tracker.lastState = state
                tracker.lastIncomingNumber = number
                tracker.callAnswered = false
                ForwardingScheduler.enqueue(
                    context,
                    ForwardEvent(
                        type = EventType.INCOMING_CALL,
                        packageName = dialerPackage,
                        sourceLabel = "Phone",
                        title = "Incoming call",
                        body = number.orEmpty(),
                        sender = number,
                        phoneNumber = number,
                        postedAt = System.currentTimeMillis(),
                        shouldRing = true,
                        dedupeKey = NotificationFingerprint.fromParts("call", "incoming", number),
                    ),
                )
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                tracker.lastState = state
                tracker.callAnswered = true
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                val previousState = tracker.lastState
                val previousNumber = tracker.lastIncomingNumber
                val wasAnswered = tracker.callAnswered
                if (previousState == TelephonyManager.EXTRA_STATE_RINGING && !wasAnswered) {
                    ForwardingScheduler.enqueue(
                        context,
                        ForwardEvent(
                            type = EventType.MISSED_CALL,
                            packageName = dialerPackage,
                            sourceLabel = "Phone",
                            title = "Missed call",
                            body = previousNumber.orEmpty(),
                            sender = previousNumber,
                            phoneNumber = previousNumber,
                            postedAt = System.currentTimeMillis(),
                            dedupeKey = NotificationFingerprint.fromParts("call", "missed", previousNumber),
                        ),
                    )
                }
                tracker.reset()
            }
        }
    }
}

