package dev.yakitori.barkforwarder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.yakitori.barkforwarder.data.model.NotificationRule

@Entity(tableName = "notification_rules")
data class NotificationRuleEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val packageLabelAtCreation: String,
    val appNamePattern: String?,
    val titlePattern: String?,
    val bodyPattern: String?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toModel(): NotificationRule {
        return NotificationRule(
            id = id,
            packageName = packageName,
            packageLabelAtCreation = packageLabelAtCreation,
            appNamePattern = appNamePattern,
            titlePattern = titlePattern,
            bodyPattern = bodyPattern,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
