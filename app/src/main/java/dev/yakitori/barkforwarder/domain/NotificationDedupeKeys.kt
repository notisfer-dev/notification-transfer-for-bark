package dev.yakitori.barkforwarder.domain

import java.util.Locale

data class NotificationDedupeKeys(
    val primaryKey: String,
    val relatedKeys: List<String>,
    val comparableText: String,
)

object NotificationDedupeKeyFactory {
    private const val CONTENT_ONLY_MIN_CHARS = 6
    private val whitespaceRegex = Regex("\\s+")
    private val zeroWidthRegex = Regex("[\\u200B-\\u200D\\uFEFF]")
    private val separatorRegex = Regex("[^\\p{L}\\p{N}]+")

    fun forNotification(
        packageName: String,
        sourceLabel: String,
        title: String,
        body: String,
        groupKey: String?,
        notificationTag: String?,
        notificationId: Int,
        isGroupSummary: Boolean,
    ): NotificationDedupeKeys {
        val normalizedTitle = normalizeExact(title)
        val normalizedBody = normalizeExact(body)
        val primaryKey = NotificationFingerprint.fromParts(
            "notification",
            packageName,
            normalizedTitle,
            normalizedBody,
        )

        val normalizedBodyComparable = normalizeComparable(body)
        val comparableText = buildComparableText(sourceLabel, title, body)
        val relatedKeys = buildList {
            if (comparableText.isBlank()) return@buildList

            groupKey?.takeIf { it.isNotBlank() }?.let {
                add(
                    NotificationFingerprint.fromParts(
                        "notification-content-group",
                        packageName,
                        comparableText,
                        it,
                    ),
                )
            }
            notificationTag?.takeIf { it.isNotBlank() }?.let {
                add(
                    NotificationFingerprint.fromParts(
                        "notification-content-tag",
                        packageName,
                        comparableText,
                        it,
                    ),
                )
            }
            if (notificationId >= 0) {
                add(
                    NotificationFingerprint.fromParts(
                        "notification-content-id",
                        packageName,
                        comparableText,
                        notificationId.toString(),
                    ),
                )
            }
            if ((isGroupSummary || !groupKey.isNullOrBlank()) && normalizedBodyComparable.length >= CONTENT_ONLY_MIN_CHARS) {
                add(NotificationFingerprint.fromParts("notification-content", packageName, comparableText))
            }
        }.distinct().filterNot { it == primaryKey }

        return NotificationDedupeKeys(
            primaryKey = primaryKey,
            relatedKeys = relatedKeys,
            comparableText = comparableText,
        )
    }

    private fun buildComparableText(sourceLabel: String, title: String, body: String): String {
        val normalizedSource = normalizeComparable(sourceLabel)
        val normalizedTitle = normalizeComparable(title)
        val normalizedBody = normalizeComparable(body)

        return buildList {
            if (normalizedTitle.isNotBlank() && normalizedTitle != normalizedSource) add(normalizedTitle)
            if (normalizedBody.isNotBlank()) add(normalizedBody)
            if (isEmpty() && normalizedTitle.isNotBlank()) add(normalizedTitle)
        }.joinToString(" ").trim()
    }

    private fun normalizeExact(value: String?): String {
        val withoutZeroWidth = zeroWidthRegex.replace(value.orEmpty(), "")
        return whitespaceRegex.replace(withoutZeroWidth.trim(), " ")
            .lowercase(Locale.ROOT)
    }

    private fun normalizeComparable(value: String?): String {
        val exact = normalizeExact(value)
        return separatorRegex.replace(exact, " ").trim()
    }
}
