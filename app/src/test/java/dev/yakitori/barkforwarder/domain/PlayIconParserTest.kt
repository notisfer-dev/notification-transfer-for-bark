package dev.yakitori.barkforwarder.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlayIconParserTest {
    @Test
    fun `extracts og image url`() {
        val html = """
            <html>
              <head>
                <meta property="og:image" content="https://play-lh.googleusercontent.com/example-icon=w480-h960" />
              </head>
            </html>
        """.trimIndent()

        assertThat(PlayIconParser.extractIconUrl(html))
            .isEqualTo("https://play-lh.googleusercontent.com/example-icon=w480-h960")
    }

    @Test
    fun `extracts raw play image url from script blob`() {
        val html = """window.__DATA__="https://play-lh.googleusercontent.com/abc123=w480-h960";"""

        assertThat(PlayIconParser.extractIconUrl(html))
            .isEqualTo("https://play-lh.googleusercontent.com/abc123=w480-h960")
    }
}

