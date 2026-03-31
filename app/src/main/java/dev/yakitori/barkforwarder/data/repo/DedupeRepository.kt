package dev.yakitori.barkforwarder.data.repo

import dev.yakitori.barkforwarder.data.db.DedupeDao
import dev.yakitori.barkforwarder.data.db.DedupeEntryEntity
import dev.yakitori.barkforwarder.data.model.EventType

class DedupeRepository(private val dao: DedupeDao) {
    suspend fun shouldForward(
        dedupeKey: String,
        relatedKeys: List<String>,
        eventType: EventType,
        windowSeconds: Int,
        nowMillis: Long,
    ): Boolean {
        if (windowSeconds <= 0) return true
        val candidateKeys = listOf(dedupeKey) + relatedKeys.filter { it.isNotBlank() }
        val uniqueKeys = candidateKeys.distinct()
        val existing = dao.getByKeys(uniqueKeys)
        val minAllowed = nowMillis - (windowSeconds * 1000L)
        val shouldAllow = existing.none { it.lastSentAtMillis >= minAllowed }
        if (shouldAllow) {
            dao.upsertAll(
                uniqueKeys.map {
                    DedupeEntryEntity(
                        dedupeKey = it,
                        eventType = eventType.name,
                        lastSentAtMillis = nowMillis,
                    )
                },
            )
        }
        dao.deleteOlderThan(nowMillis - CLEANUP_WINDOW_MILLIS)
        return shouldAllow
    }

    private companion object {
        const val CLEANUP_WINDOW_MILLIS = 7L * 24L * 60L * 60L * 1000L
    }
}
