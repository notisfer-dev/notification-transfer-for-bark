package dev.yakitori.barkforwarder.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {
    @Query("SELECT * FROM app_rules ORDER BY lower(appLabel) ASC")
    fun observeAll(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules ORDER BY lower(appLabel) ASC")
    suspend fun getAll(): List<AppRuleEntity>

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): AppRuleEntity?

    @Upsert
    suspend fun upsert(rule: AppRuleEntity)

    @Upsert
    suspend fun upsertAll(rules: List<AppRuleEntity>)
}

