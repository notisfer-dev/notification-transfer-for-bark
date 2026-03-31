package dev.yakitori.barkforwarder.data.model

data class BarkConfig(
    val serverUrl: String = "https://api.day.app/push",
    val deviceKey: String = "",
    val notificationsEnabled: Boolean = true,
    val smsEnabled: Boolean = true,
    val callsEnabled: Boolean = true,
    val ringIncomingCalls: Boolean = true,
)

