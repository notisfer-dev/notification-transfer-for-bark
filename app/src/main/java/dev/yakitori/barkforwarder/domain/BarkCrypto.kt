package dev.yakitori.barkforwarder.domain

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object BarkCrypto {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BITS = 128
    private const val IV_LENGTH = 12
    private const val KEY_LENGTH = 32

    fun encrypt(payloadJson: String, key: String, iv: String): String {
        require(key.toByteArray(StandardCharsets.UTF_8).size == KEY_LENGTH) {
            "Bark AES-256-GCM key must be exactly 32 characters."
        }
        require(iv.toByteArray(StandardCharsets.UTF_8).size == IV_LENGTH) {
            "Bark GCM IV must be exactly 12 characters."
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val params = GCMParameterSpec(TAG_LENGTH_BITS, iv.toByteArray(StandardCharsets.UTF_8))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, params)
        return Base64.getEncoder().encodeToString(cipher.doFinal(payloadJson.toByteArray(StandardCharsets.UTF_8)))
    }

    fun decrypt(ciphertext: String, key: String, iv: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val params = GCMParameterSpec(TAG_LENGTH_BITS, iv.toByteArray(StandardCharsets.UTF_8))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, params)
        val bytes = Base64.getDecoder().decode(ciphertext)
        return cipher.doFinal(bytes).toString(StandardCharsets.UTF_8)
    }
}
