package dev.yakitori.barkforwarder.data.repo

import com.google.common.truth.Truth.assertThat
import dev.yakitori.barkforwarder.data.db.DedupeDao
import dev.yakitori.barkforwarder.data.db.DedupeEntryEntity
import dev.yakitori.barkforwarder.data.model.EventType
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DedupeRepositoryTest {
    @Test
    fun `shared alias blocks duplicate inside window`() = runTest {
        val repository = DedupeRepository(FakeDedupeDao())

        val first = repository.shouldForward(
            dedupeKey = "strict-1",
            relatedKeys = listOf("shared"),
            eventType = EventType.NOTIFICATION,
            windowSeconds = 5,
            nowMillis = 1_000,
        )
        val second = repository.shouldForward(
            dedupeKey = "strict-2",
            relatedKeys = listOf("shared"),
            eventType = EventType.NOTIFICATION,
            windowSeconds = 5,
            nowMillis = 2_000,
        )

        assertThat(first).isTrue()
        assertThat(second).isFalse()
    }

    @Test
    fun `duplicate passes again after timeout expires`() = runTest {
        val repository = DedupeRepository(FakeDedupeDao())

        val first = repository.shouldForward(
            dedupeKey = "strict-1",
            relatedKeys = listOf("shared"),
            eventType = EventType.NOTIFICATION,
            windowSeconds = 5,
            nowMillis = 1_000,
        )
        val second = repository.shouldForward(
            dedupeKey = "strict-2",
            relatedKeys = listOf("shared"),
            eventType = EventType.NOTIFICATION,
            windowSeconds = 5,
            nowMillis = 7_001,
        )

        assertThat(first).isTrue()
        assertThat(second).isTrue()
    }

    private class FakeDedupeDao : DedupeDao {
        private val entries = linkedMapOf<String, DedupeEntryEntity>()

        override suspend fun getByKeys(dedupeKeys: List<String>): List<DedupeEntryEntity> {
            return dedupeKeys.mapNotNull(entries::get)
        }

        override suspend fun upsert(entry: DedupeEntryEntity) {
            entries[entry.dedupeKey] = entry
        }

        override suspend fun upsertAll(entries: List<DedupeEntryEntity>) {
            entries.forEach { upsert(it) }
        }

        override suspend fun deleteOlderThan(thresholdMillis: Long) {
            entries.entries.removeAll { it.value.lastSentAtMillis < thresholdMillis }
        }
    }
}
