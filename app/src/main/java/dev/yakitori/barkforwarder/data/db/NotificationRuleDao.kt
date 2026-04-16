package dev.yakitori.barkforwarder.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationRuleDao {
    @Query("SELECT * FROM notification_rules ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NotificationRuleEntity>>

    @Query("SELECT * FROM notification_rules WHERE packageName = :packageName ORDER BY updatedAt DESC")
    suspend fun getByPackageName(packageName: String): List<NotificationRuleEntity>

    @Upsert
    suspend fun upsert(rule: NotificationRuleEntity)

    @Query("DELETE FROM notification_rules WHERE id = :id")
    suspend fun deleteById(id: String)
}
