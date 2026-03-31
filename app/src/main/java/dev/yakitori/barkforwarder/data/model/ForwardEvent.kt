package dev.yakitori.barkforwarder.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ForwardEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: EventType,
    val captureSource: CaptureSource = CaptureSource.DIRECT,
    val packageName: String? = null,
    val sourceLabel: String,
    val title: String = "",
    val body: String = "",
    val sender: String? = null,
    val phoneNumber: String? = null,
    val category: String? = null,
    val postedAt: Long,
    val shouldRing: Boolean = false,
    val notificationId: Int? = null,
    val notificationTag: String? = null,
    val groupKey: String? = null,
    val isGroupSummary: Boolean = false,
    val imageUrl: String? = null,
    val iconUrl: String? = null,
    val dedupeKey: String,
    val dedupeAliases: List<String> = emptyList(),
)
