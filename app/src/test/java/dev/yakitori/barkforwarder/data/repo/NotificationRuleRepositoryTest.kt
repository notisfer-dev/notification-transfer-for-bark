package dev.yakitori.barkforwarder.data.repo

import com.google.common.truth.Truth.assertThat
import dev.yakitori.barkforwarder.data.db.NotificationRuleDao
import dev.yakitori.barkforwarder.data.db.NotificationRuleEntity
import dev.yakitori.barkforwarder.data.model.CaptureSource
import dev.yakitori.barkforwarder.data.model.EventType
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotificationRuleRepositoryTest {
    @Test
    fun `saved rule matches later notifications from same package`() = runTest {
        val repository = NotificationRuleRepository(FakeNotificationRuleDao())

        repository.saveRule(
            packageName = "jp.naver.line.android",
            packageLabelAtCreation = "LINE",
            appNamePattern = "line",
            titlePattern = "battery",
            bodyPattern = "right now",
            nowMillis = 10L,
        )

        val match = repository.findMatchingRule(
            ForwardEvent(
                type = EventType.NOTIFICATION,
                captureSource = CaptureSource.DIRECT,
                packageName = "jp.naver.line.android",
                sourceLabel = "LINE",
                title = "Battery warning",
                body = "It is happening right now",
                postedAt = 1_000L,
                dedupeKey = "dedupe",
            ),
        )

        assertThat(match).isNotNull()
        assertThat(match?.packageLabelAtCreation).isEqualTo("LINE")
    }

    @Test
    fun `rule requires title or body`() = runTest {
        val repository = NotificationRuleRepository(FakeNotificationRuleDao())

        val error = runCatching {
            repository.saveRule(
                packageName = "jp.naver.line.android",
                packageLabelAtCreation = "LINE",
                appNamePattern = "line",
                titlePattern = "   ",
                bodyPattern = "\u200B",
            )
        }.exceptionOrNull()

        assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
    }

    private class FakeNotificationRuleDao : NotificationRuleDao {
        private val entries = linkedMapOf<String, NotificationRuleEntity>()
        private val flow = MutableStateFlow<List<NotificationRuleEntity>>(emptyList())

        override fun observeAll(): Flow<List<NotificationRuleEntity>> = flow

        override suspend fun getByPackageName(packageName: String): List<NotificationRuleEntity> {
            return entries.values
                .filter { it.packageName == packageName }
                .sortedByDescending { it.updatedAt }
        }

        override suspend fun upsert(rule: NotificationRuleEntity) {
            entries[rule.id] = rule
            publish()
        }

        override suspend fun deleteById(id: String) {
            entries.remove(id)
            publish()
        }

        private fun publish() {
            flow.value = entries.values.sortedByDescending { it.updatedAt }
        }
    }
}
