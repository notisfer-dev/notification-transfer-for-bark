package dev.yakitori.barkforwarder.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NotificationDedupeKeyFactoryTest {
    @Test
    fun `summary and child notifications share comparable text and group alias`() {
        val summary = NotificationDedupeKeyFactory.forNotification(
            packageName = "jp.naver.line.android",
            sourceLabel = "LINE",
            title = "LINE",
            body = "Alice: Hello there",
            groupKey = "line-chat-1",
            notificationTag = "summary",
            notificationId = 10,
            isGroupSummary = true,
        )
        val child = NotificationDedupeKeyFactory.forNotification(
            packageName = "jp.naver.line.android",
            sourceLabel = "LINE",
            title = "Alice",
            body = "Hello there",
            groupKey = "line-chat-1",
            notificationTag = "message",
            notificationId = 11,
            isGroupSummary = false,
        )

        val sharedGroupAlias = NotificationFingerprint.fromParts(
            "notification-content-group",
            "jp.naver.line.android",
            "alice hello there",
            "line-chat-1",
        )

        assertThat(summary.comparableText).isEqualTo("alice hello there")
        assertThat(child.comparableText).isEqualTo("alice hello there")
        assertThat(summary.relatedKeys).contains(sharedGroupAlias)
        assertThat(child.relatedKeys).contains(sharedGroupAlias)
    }

    @Test
    fun `short grouped content skips content only alias`() {
        val keys = NotificationDedupeKeyFactory.forNotification(
            packageName = "jp.naver.line.android",
            sourceLabel = "LINE",
            title = "Bob",
            body = "ok",
            groupKey = "line-chat-2",
            notificationTag = "message",
            notificationId = 12,
            isGroupSummary = false,
        )

        assertThat(keys.comparableText).isEqualTo("bob ok")
        assertThat(keys.relatedKeys).doesNotContain(
            NotificationFingerprint.fromParts(
                "notification-content",
                "jp.naver.line.android",
                "bob ok",
            ),
        )
    }
}
