package dev.yakitori.barkforwarder.domain

import com.google.common.truth.Truth.assertThat
import dev.yakitori.barkforwarder.data.model.CaptureSource
import dev.yakitori.barkforwarder.data.model.EventType
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import dev.yakitori.barkforwarder.data.model.NotificationRule
import org.junit.Test

class NotificationRuleMatcherTest {
    @Test
    fun `matches normalized partial title and body for same package`() {
        val rule = NotificationRule(
            id = "rule-1",
            packageName = "jp.naver.line.android",
            packageLabelAtCreation = "LINE",
            appNamePattern = "line",
            titlePattern = "battery low",
            bodyPattern = "now",
            createdAt = 1L,
            updatedAt = 1L,
        )

        val event = notificationEvent(
            packageName = "jp.naver.line.android",
            sourceLabel = " LINE ",
            title = "Battery   Low",
            body = "\u200BRight now on device",
        )

        assertThat(NotificationRuleMatcher.matches(rule, event)).isTrue()
    }

    @Test
    fun `does not match different package even with same text`() {
        val rule = NotificationRule(
            id = "rule-1",
            packageName = "jp.naver.line.android",
            packageLabelAtCreation = "LINE",
            appNamePattern = null,
            titlePattern = "battery low",
            bodyPattern = "right now",
            createdAt = 1L,
            updatedAt = 1L,
        )

        val event = notificationEvent(
            packageName = "com.discord",
            sourceLabel = "Discord",
            title = "Battery low",
            body = "Right now on device",
        )

        assertThat(NotificationRuleMatcher.matches(rule, event)).isFalse()
    }

    @Test
    fun `non notification event never matches`() {
        val rule = NotificationRule(
            id = "rule-1",
            packageName = "jp.naver.line.android",
            packageLabelAtCreation = "LINE",
            appNamePattern = null,
            titlePattern = "hello",
            bodyPattern = null,
            createdAt = 1L,
            updatedAt = 1L,
        )

        val event = notificationEvent(
            type = EventType.SMS,
            packageName = "jp.naver.line.android",
            sourceLabel = "LINE",
            title = "hello",
            body = "ignored",
        )

        assertThat(NotificationRuleMatcher.matches(rule, event)).isFalse()
    }

    private fun notificationEvent(
        type: EventType = EventType.NOTIFICATION,
        packageName: String,
        sourceLabel: String,
        title: String,
        body: String,
    ): ForwardEvent {
        return ForwardEvent(
            type = type,
            captureSource = CaptureSource.DIRECT,
            packageName = packageName,
            sourceLabel = sourceLabel,
            title = title,
            body = body,
            postedAt = 1_000L,
            dedupeKey = "dedupe",
        )
    }
}
