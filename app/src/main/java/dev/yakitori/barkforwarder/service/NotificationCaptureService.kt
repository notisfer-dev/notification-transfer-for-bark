package dev.yakitori.barkforwarder.service

import android.app.Notification
import android.content.Context
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager
import dev.yakitori.barkforwarder.BarkBridgeApp
import dev.yakitori.barkforwarder.data.model.CaptureSource
import dev.yakitori.barkforwarder.data.model.EventType
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import dev.yakitori.barkforwarder.domain.AppPermissions
import dev.yakitori.barkforwarder.domain.ForwardingScheduler
import dev.yakitori.barkforwarder.domain.NotificationDedupeKeyFactory
import dev.yakitori.barkforwarder.domain.NotificationImageExtractor

class NotificationCaptureService : NotificationListenerService() {
    override fun onListenerConnected() {
        ForwardingScheduler.enqueueInstalledAppSync(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        if (packageName == applicationContext.packageName) return

        val appRuleRepository = BarkBridgeApp.from(applicationContext).container.appRuleRepository
        val sourceLabel = appRuleRepository.resolveAppLabel(packageName)
        val extras = sbn.notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val body = (bigText.ifBlank { text }).trim()
        val category = sbn.notification.category ?: extras?.getString("android.template")
        val isGroupSummary = sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        val dedupeKeys = NotificationDedupeKeyFactory.forNotification(
            packageName = packageName,
            sourceLabel = sourceLabel,
            title = title,
            body = body,
            groupKey = sbn.groupKey,
            notificationTag = sbn.tag,
            notificationId = sbn.id,
            isGroupSummary = isGroupSummary,
        )

        val typeAndSource = resolveTypeAndSource(applicationContext, packageName, category)
            ?: return

        val event = ForwardEvent(
            type = typeAndSource.first,
            captureSource = typeAndSource.second,
            packageName = packageName,
            sourceLabel = sourceLabel,
            title = title,
            body = body,
            category = category,
            postedAt = sbn.postTime.takeIf { it > 0 } ?: System.currentTimeMillis(),
            shouldRing = typeAndSource.first == EventType.INCOMING_CALL,
            notificationId = sbn.id,
            notificationTag = sbn.tag,
            groupKey = sbn.groupKey,
            isGroupSummary = isGroupSummary,
            imageUrl = NotificationImageExtractor.extractRemoteImageUrl(sbn.notification),
            dedupeKey = dedupeKeys.primaryKey,
            dedupeAliases = dedupeKeys.relatedKeys,
        )
        ForwardingScheduler.enqueue(this, event)
    }

    private fun resolveTypeAndSource(
        context: Context,
        packageName: String,
        category: String?,
    ): Pair<EventType, CaptureSource>? {
        val smsPackage = Telephony.Sms.getDefaultSmsPackage(context)
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        val dialerPackage = telecomManager?.defaultDialerPackage

        if (packageName == smsPackage) {
            return if (AppPermissions.hasSmsAccess(context)) {
                null
            } else {
                EventType.SMS to CaptureSource.NOTIFICATION_FALLBACK
            }
        }

        if (packageName == dialerPackage) {
            return if (AppPermissions.hasCallAccess(context)) {
                null
            } else {
                val type = if (category == Notification.CATEGORY_MISSED_CALL) EventType.MISSED_CALL else EventType.INCOMING_CALL
                type to CaptureSource.NOTIFICATION_FALLBACK
            }
        }

        return EventType.NOTIFICATION to CaptureSource.DIRECT
    }
}
