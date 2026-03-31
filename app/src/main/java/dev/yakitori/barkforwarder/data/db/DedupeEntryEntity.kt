package dev.yakitori.barkforwarder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dedupe_entries")
data class DedupeEntryEntity(
    @PrimaryKey val dedupeKey: String,
    val eventType: String,
    val lastSentAtMillis: Long,
)

