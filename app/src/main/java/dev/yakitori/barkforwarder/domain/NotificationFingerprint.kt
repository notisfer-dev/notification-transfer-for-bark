package dev.yakitori.barkforwarder.domain

import java.security.MessageDigest

object NotificationFingerprint {
    fun fromParts(vararg parts: String?): String {
        val normalized = parts.joinToString(separator = "|") { it.orEmpty().trim().lowercase() }
        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}

