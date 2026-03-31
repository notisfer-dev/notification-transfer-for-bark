package dev.yakitori.barkforwarder.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DedupeDao {
    @Query("SELECT * FROM dedupe_entries WHERE dedupeKey IN (:dedupeKeys)")
    suspend fun getByKeys(dedupeKeys: List<String>): List<DedupeEntryEntity>

    @Upsert
    suspend fun upsert(entry: DedupeEntryEntity)

    @Upsert
    suspend fun upsertAll(entries: List<DedupeEntryEntity>)

    @Query("DELETE FROM dedupe_entries WHERE lastSentAtMillis < :thresholdMillis")
    suspend fun deleteOlderThan(thresholdMillis: Long)
}
