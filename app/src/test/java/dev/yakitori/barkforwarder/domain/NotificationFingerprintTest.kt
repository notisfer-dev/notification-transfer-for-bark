package dev.yakitori.barkforwarder.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NotificationFingerprintTest {
    @Test
    fun `same normalized inputs produce same key`() {
        val first = NotificationFingerprint.fromParts("jp.naver.line.android", " LINE ", "Hello")
        val second = NotificationFingerprint.fromParts("jp.naver.line.android", "line", "hello")

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun `different content produces different key`() {
        val first = NotificationFingerprint.fromParts("pkg", "title", "body-1")
        val second = NotificationFingerprint.fromParts("pkg", "title", "body-2")

        assertThat(first).isNotEqualTo(second)
    }
}

