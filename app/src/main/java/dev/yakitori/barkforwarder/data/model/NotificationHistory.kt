package dev.yakitori.barkforwarder.data.model

data class NotificationHistory(
    val id: String,
    val packageName: String,
    val sourceLabel: String,
    val title: String,
    val body: String,
    val renderedTitle: String,
    val renderedBody: String,
    val postedAt: Long,
    val forwardedAt: Long,
)
