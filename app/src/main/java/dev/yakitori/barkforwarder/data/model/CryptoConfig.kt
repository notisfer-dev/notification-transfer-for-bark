package dev.yakitori.barkforwarder.data.model

data class CryptoConfig(
    val enabled: Boolean = true,
    val key: String = "",
    val fixedIv: String = "",
)
