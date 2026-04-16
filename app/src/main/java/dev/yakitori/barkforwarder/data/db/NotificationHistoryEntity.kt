package dev.yakitori.barkforwarder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.yakitori.barkforwarder.data.model.NotificationHistory

@Entity(tableName = "notification_history")
data class NotificationHistoryEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val sourceLabel: String,
    val title: String,
    val body: String,
    val renderedTitle: String,
    val renderedBody: String,
    val postedAt: Long,
    val forwardedAt: Long,
) {
    fun toModel(): NotificationHistory {
        return NotificationHistory(
            id = id,
            packageName = packageName,
            sourceLabel = sourceLabel,
            title = title,
            body = body,
            renderedTitle = renderedTitle,
            renderedBody = renderedBody,
            postedAt = postedAt,
            forwardedAt = forwardedAt,
        )
    }
}
