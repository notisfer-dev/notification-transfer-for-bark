package dev.yakitori.barkforwarder.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class CaptureSource {
    DIRECT,
    NOTIFICATION_FALLBACK,
}

