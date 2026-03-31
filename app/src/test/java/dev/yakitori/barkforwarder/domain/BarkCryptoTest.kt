package dev.yakitori.barkforwarder.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BarkCryptoTest {
    @Test
    fun `encrypt round-trips with decrypt`() {
        val key = "12345678901234567890123456789012"
        val iv = "ABCdef123456"
        val plain = """{"title":"LINE","body":"hello"}"""

        val ciphertext = BarkCrypto.encrypt(plain, key, iv)
        val decrypted = BarkCrypto.decrypt(ciphertext, key, iv)

        assertThat(decrypted).isEqualTo(plain)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `encrypt rejects short keys`() {
        BarkCrypto.encrypt("{}", "short", "ABCdef123456")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `encrypt rejects invalid iv length`() {
        BarkCrypto.encrypt("{}", "12345678901234567890123456789012", "tiny")
    }

    @Test
    fun `encrypt matches bark node gcm example format`() {
        val key = "12345678901234567890123456789012"
        val iv = "ABCdef123456"
        val json = """{"body":"test","sound":"birdsong"}"""

        val ciphertext = BarkCrypto.encrypt(json, key, iv)

        assertThat(ciphertext)
            .isEqualTo("OTKRegTBZc3vnyO46eZ+KzAOBjw9hEGcR/K6CZeuLl9mb0m/9nJIloPrE4d38VV3ejU=")
    }
}
