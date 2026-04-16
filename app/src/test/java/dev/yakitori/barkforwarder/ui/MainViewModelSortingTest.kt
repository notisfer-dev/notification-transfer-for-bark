package dev.yakitori.barkforwarder.ui

import com.google.common.truth.Truth.assertThat
import dev.yakitori.barkforwarder.data.model.AppRule
import org.junit.Test

class MainViewModelSortingTest {
    @Test
    fun `selected excluded apps move to top while self app stays in normal section`() {
        val selfPackageName = "dev.yakitori.barkforwarder"
        val rules = listOf(
            appRule(
                packageName = selfPackageName,
                appLabel = "Notification Transfer for Bark",
                excluded = true,
            ),
            appRule(
                packageName = "com.discord",
                appLabel = "Discord",
                excluded = false,
            ),
            appRule(
                packageName = "jp.naver.line.android",
                appLabel = "LINE",
                excluded = true,
            ),
            appRule(
                packageName = "com.slack",
                appLabel = "Slack",
                excluded = true,
            ),
        )

        val sorted = sortAppRulesForAppsScreen(rules, selfPackageName)

        assertThat(sorted.map { it.packageName }).containsExactly(
            "jp.naver.line.android",
            "com.slack",
            "com.discord",
            selfPackageName,
        ).inOrder()
    }

    @Test
    fun `non excluded apps stay alphabetical when no user selected exclusions exist`() {
        val rules = listOf(
            appRule(packageName = "b.pkg", appLabel = "Beta", excluded = false),
            appRule(packageName = "a.pkg", appLabel = "Alpha", excluded = false),
            appRule(
                packageName = "dev.yakitori.barkforwarder",
                appLabel = "Notification Transfer for Bark",
                excluded = true,
            ),
        )

        val sorted = sortAppRulesForAppsScreen(rules, "dev.yakitori.barkforwarder")

        assertThat(sorted.map { it.packageName }).containsExactly(
            "a.pkg",
            "b.pkg",
            "dev.yakitori.barkforwarder",
        ).inOrder()
    }

    private fun appRule(
        packageName: String,
        appLabel: String,
        excluded: Boolean,
    ): AppRule {
        return AppRule(
            packageName = packageName,
            appLabel = appLabel,
            excluded = excluded,
            manualIconUrl = null,
            cachedResolvedIconUrl = null,
            iconResolvedAt = null,
        )
    }
}
