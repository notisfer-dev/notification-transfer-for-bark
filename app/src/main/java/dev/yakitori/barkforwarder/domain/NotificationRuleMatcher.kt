package dev.yakitori.barkforwarder.domain

import dev.yakitori.barkforwarder.data.model.EventType
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import dev.yakitori.barkforwarder.data.model.NotificationRule
import java.util.Locale

object NotificationRuleMatcher {
    private val whitespaceRegex = Regex("\\s+")
    private val zeroWidthRegex = Regex("[\\u200B-\\u200D\\uFEFF]")

    fun matches(rule: NotificationRule, event: ForwardEvent): Boolean {
        if (event.type != EventType.NOTIFICATION) return false
        if (rule.packageName != event.packageName) return false

        val appNameMatches = containsNormalized(
            target = event.sourceLabel,
            pattern = rule.appNamePattern,
        )
        val titleMatches = containsNormalized(
            target = event.title,
            pattern = rule.titlePattern,
        )
        val bodyMatches = containsNormalized(
            target = event.body,
            pattern = rule.bodyPattern,
        )

        return appNameMatches && titleMatches && bodyMatches
    }

    fun normalizePattern(value: String?): String? {
        return cleanPatternInput(value)?.let(::normalize)
    }

    fun cleanPatternInput(value: String?): String? {
        val withoutZeroWidth = zeroWidthRegex.replace(value.orEmpty(), "")
        return whitespaceRegex.replace(withoutZeroWidth.trim(), " ")
            .ifBlank { null }
    }

    private fun containsNormalized(target: String?, pattern: String?): Boolean {
        val normalizedPattern = normalizePattern(pattern) ?: return true
        return normalize(target).contains(normalizedPattern)
    }

    private fun normalize(value: String?): String {
        val withoutZeroWidth = zeroWidthRegex.replace(value.orEmpty(), "")
        return whitespaceRegex.replace(withoutZeroWidth.trim(), " ")
            .lowercase(Locale.ROOT)
    }
}
