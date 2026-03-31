package dev.yakitori.barkforwarder.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class EventType {
    NOTIFICATION,
    SMS,
    INCOMING_CALL,
    MISSED_CALL,
}

