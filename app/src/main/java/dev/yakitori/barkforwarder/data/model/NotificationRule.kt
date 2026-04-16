package dev.yakitori.barkforwarder.data.model

data class NotificationRule(
    val id: String,
    val packageName: String,
    val packageLabelAtCreation: String,
    val appNamePattern: String?,
    val titlePattern: String?,
    val bodyPattern: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
