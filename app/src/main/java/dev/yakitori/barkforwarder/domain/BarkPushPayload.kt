package dev.yakitori.barkforwarder.domain

import kotlinx.serialization.Serializable

@Serializable
data class BarkPushPayload(
    val title: String,
    val body: String,
    val group: String,
    val level: String,
    val call: Int? = null,
    val icon: String? = null,
    val image: String? = null,
)

