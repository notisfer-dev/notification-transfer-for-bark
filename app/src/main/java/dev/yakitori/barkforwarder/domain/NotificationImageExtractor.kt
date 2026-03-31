package dev.yakitori.barkforwarder.domain

import android.app.Notification
import android.net.Uri
import android.os.Bundle

object NotificationImageExtractor {
    private val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)

    fun extractRemoteImageUrl(notification: Notification): String? {
        return findUrl(notification.extras)
    }

    private fun findUrl(bundle: Bundle?): String? {
        if (bundle == null) return null
        bundle.keySet().forEach { key ->
            val value = bundle.get(key)
            extractValue(value)?.let { return it }
        }
        return null
    }

    private fun extractValue(value: Any?): String? {
        return when (value) {
            is String -> urlRegex.find(value)?.value
            is CharSequence -> urlRegex.find(value.toString())?.value
            is Uri -> value.toString().takeIf { it.startsWith("http://") || it.startsWith("https://") }
            is Bundle -> findUrl(value)
            is Array<*> -> value.firstNotNullOfOrNull { extractValue(it) }
            is Iterable<*> -> value.firstNotNullOfOrNull { extractValue(it) }
            else -> null
        }
    }
}

