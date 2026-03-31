package dev.yakitori.barkforwarder.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BarkSettingsValidatorTest {
    @Test
    fun `accepts bark url plus fixed iv`() {
        val validated = BarkSettingsValidator.validate(
            barkUrlOrKey = "https://api.day.app/ABCDEFGHIJKLMNOP",
            cryptoKey = "12345678901234567890123456789012",
            fixedIv = "ABCdef123456",
        )

        assertThat(validated.endpoint.serverUrl).isEqualTo("https://api.day.app/push")
        assertThat(validated.endpoint.deviceKey).isEqualTo("ABCDEFGHIJKLMNOP")
        assertThat(validated.cryptoKey).isEqualTo("12345678901234567890123456789012")
        assertThat(validated.fixedIv).isEqualTo("ABCdef123456")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects wrong key length`() {
        BarkSettingsValidator.validate(
            barkUrlOrKey = "ABCDEFGHIJKLMNOP",
            cryptoKey = "short",
            fixedIv = "ABCdef123456",
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects wrong iv length`() {
        BarkSettingsValidator.validate(
            barkUrlOrKey = "ABCDEFGHIJKLMNOP",
            cryptoKey = "12345678901234567890123456789012",
            fixedIv = "too-short",
        )
    }
}
