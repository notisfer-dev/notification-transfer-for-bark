package dev.yakitori.barkforwarder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.yakitori.barkforwarder.data.model.AppRule

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val excluded: Boolean,
    val manualIconUrl: String?,
    val cachedResolvedIconUrl: String?,
    val iconResolvedAt: Long?,
) {
    fun toModel(): AppRule {
        return AppRule(
            packageName = packageName,
            appLabel = appLabel,
            excluded = excluded,
            manualIconUrl = manualIconUrl,
            cachedResolvedIconUrl = cachedResolvedIconUrl,
            iconResolvedAt = iconResolvedAt,
        )
    }
}

