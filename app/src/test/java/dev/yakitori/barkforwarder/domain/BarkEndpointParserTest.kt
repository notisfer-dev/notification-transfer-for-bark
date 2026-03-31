package dev.yakitori.barkforwarder.domain

import com.google.common.truth.Truth.assertThat
import dev.yakitori.barkforwarder.data.model.BarkConfig
import org.junit.Test

class BarkEndpointParserTest {
    @Test
    fun `parses api day app url into push endpoint and key`() {
        val endpoint = BarkEndpointParser.parse("https://api.day.app/ABCDEFGHIJKLMNOP")

        assertThat(endpoint).isEqualTo(
            BarkEndpoint(
                serverUrl = "https://api.day.app/push",
                deviceKey = "ABCDEFGHIJKLMNOP",
            ),
        )
    }

    @Test
    fun `parses api day app url with trailing slash`() {
        val endpoint = BarkEndpointParser.parse("https://api.day.app/ABCDEFGHIJKLMNOP/")

        assertThat(endpoint).isEqualTo(
            BarkEndpoint(
                serverUrl = "https://api.day.app/push",
                deviceKey = "ABCDEFGHIJKLMNOP",
            ),
        )
    }

    @Test
    fun `parses bare key`() {
        val endpoint = BarkEndpointParser.parse("ABCDEFGHIJKLMNOP")

        assertThat(endpoint).isEqualTo(
            BarkEndpoint(
                serverUrl = "https://api.day.app/push",
                deviceKey = "ABCDEFGHIJKLMNOP",
            ),
        )
    }

    @Test
    fun `keeps self hosted path structure when normalizing`() {
        val endpoint = BarkEndpointParser.parse("https://bark.example.com/custom/ABCDEFGHIJKLMNOP")

        assertThat(endpoint).isEqualTo(
            BarkEndpoint(
                serverUrl = "https://bark.example.com/custom/push",
                deviceKey = "ABCDEFGHIJKLMNOP",
            ),
        )
    }

    @Test
    fun `builds display value from stored config`() {
        val display = BarkEndpointParser.toDisplayValue(
            BarkConfig(
                serverUrl = "https://api.day.app/push",
                deviceKey = "ABCDEFGHIJKLMNOP",
            ),
        )

        assertThat(display).isEqualTo("https://api.day.app/ABCDEFGHIJKLMNOP")
    }
}

