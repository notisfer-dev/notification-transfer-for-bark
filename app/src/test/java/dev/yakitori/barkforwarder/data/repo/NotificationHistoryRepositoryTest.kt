package dev.yakitori.barkforwarder.data.repo

import com.google.common.truth.Truth.assertThat
import dev.yakitori.barkforwarder.data.db.NotificationHistoryDao
import dev.yakitori.barkforwarder.data.db.NotificationHistoryEntity
import dev.yakitori.barkforwarder.data.model.CaptureSource
import dev.yakitori.barkforwarder.data.model.EventType
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotificationHistoryRepositoryTest {
    @Test
    fun `records only forwarded notification events`() = runTest {
        val dao = FakeNotificationHistoryDao()
        val repository = NotificationHistoryRepository(dao)

        repository.recordForwardedNotification(
            event = forwardEvent(type = EventType.NOTIFICATION, packageName = "jp.naver.line.android"),
            renderedTitle = "LINE",
            renderedBody = "Battery low",
            forwardedAt = 2_000L,
        )
        repository.recordForwardedNotification(
            event = forwardEvent(type = EventType.SMS, packageName = "jp.naver.line.android"),
            renderedTitle = "SMS",
            renderedBody = "Ignored",
            forwardedAt = 3_000L,
        )

        assertThat(dao.entries).hasSize(1)
        assertThat(dao.entries.single().renderedBody).isEqualTo("Battery low")
    }

    @Test
    fun `prunes history to latest 200 entries`() = runTest {
        val dao = FakeNotificationHistoryDao()
        val repository = NotificationHistoryRepository(dao)

        repeat(205) { index ->
            repository.recordForwardedNotification(
                event = forwardEvent(
                    type = EventType.NOTIFICATION,
                    packageName = "jp.naver.line.android",
                    postedAt = index.toLong(),
                ),
                renderedTitle = "LINE",
                renderedBody = "Message $index",
                forwardedAt = index.toLong(),
            )
        }

        assertThat(dao.entries).hasSize(200)
        val bodies = dao.entries.map { it.renderedBody }
        assertThat(bodies).doesNotContain("Message 0")
        assertThat(bodies).contains("Message 204")
    }

    private fun forwardEvent(
        type: EventType,
        packageName: String,
        postedAt: Long = 1_000L,
    ): ForwardEvent {
        return ForwardEvent(
            type = type,
            captureSource = CaptureSource.DIRECT,
            packageName = packageName,
            sourceLabel = "LINE",
            title = "Battery low",
            body = "Right now",
            postedAt = postedAt,
            dedupeKey = "dedupe-$postedAt",
        )
    }

    private class FakeNotificationHistoryDao : NotificationHistoryDao {
        val entries = mutableListOf<NotificationHistoryEntity>()
        private val flow = MutableStateFlow<List<NotificationHistoryEntity>>(emptyList())

        override fun observeAll(): Flow<List<NotificationHistoryEntity>> = flow

        override suspend fun insert(entry: NotificationHistoryEntity) {
            entries += entry
            publish()
        }

        override suspend fun pruneToLatest(limit: Int) {
            val kept = entries.sortedByDescending { it.forwardedAt }.take(limit).map { it.id }.toSet()
            entries.removeAll { it.id !in kept }
            publish()
        }

        private fun publish() {
            flow.value = entries.sortedByDescending { it.forwardedAt }
        }
    }
}
