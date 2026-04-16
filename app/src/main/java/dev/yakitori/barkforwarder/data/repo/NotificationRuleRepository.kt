package dev.yakitori.barkforwarder.data.repo

import dev.yakitori.barkforwarder.data.db.NotificationRuleDao
import dev.yakitori.barkforwarder.data.db.NotificationRuleEntity
import dev.yakitori.barkforwarder.data.model.ForwardEvent
import dev.yakitori.barkforwarder.data.model.NotificationRule
import dev.yakitori.barkforwarder.domain.NotificationRuleMatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NotificationRuleRepository(
    private val dao: NotificationRuleDao,
) {
    fun observeRules(): Flow<List<NotificationRule>> {
        return dao.observeAll().map { entities -> entities.map { it.toModel() } }
    }

    suspend fun findMatchingRule(event: ForwardEvent): NotificationRule? {
        val packageName = event.packageName ?: return null
        return dao.getByPackageName(packageName)
            .map { it.toModel() }
            .firstOrNull { NotificationRuleMatcher.matches(it, event) }
    }

    suspend fun saveRule(
        id: String? = null,
        packageName: String,
        packageLabelAtCreation: String,
        appNamePattern: String?,
        titlePattern: String?,
        bodyPattern: String?,
        nowMillis: Long = System.currentTimeMillis(),
    ): NotificationRule {
        val cleanedAppName = NotificationRuleMatcher.cleanPatternInput(appNamePattern)
        val cleanedTitle = NotificationRuleMatcher.cleanPatternInput(titlePattern)
        val cleanedBody = NotificationRuleMatcher.cleanPatternInput(bodyPattern)

        require(!cleanedTitle.isNullOrBlank() || !cleanedBody.isNullOrBlank()) {
            "Title or body is required."
        }

        val rule = NotificationRuleEntity(
            id = id ?: UUID.randomUUID().toString(),
            packageName = packageName,
            packageLabelAtCreation = packageLabelAtCreation,
            appNamePattern = cleanedAppName,
            titlePattern = cleanedTitle,
            bodyPattern = cleanedBody,
            createdAt = nowMillis,
            updatedAt = nowMillis,
        )
        dao.upsert(rule)
        return rule.toModel()
    }

    suspend fun updateRule(
        existing: NotificationRule,
        appNamePattern: String?,
        titlePattern: String?,
        bodyPattern: String?,
        nowMillis: Long = System.currentTimeMillis(),
    ): NotificationRule {
        val cleanedAppName = NotificationRuleMatcher.cleanPatternInput(appNamePattern)
        val cleanedTitle = NotificationRuleMatcher.cleanPatternInput(titlePattern)
        val cleanedBody = NotificationRuleMatcher.cleanPatternInput(bodyPattern)

        require(!cleanedTitle.isNullOrBlank() || !cleanedBody.isNullOrBlank()) {
            "Title or body is required."
        }

        val updated = NotificationRuleEntity(
            id = existing.id,
            packageName = existing.packageName,
            packageLabelAtCreation = existing.packageLabelAtCreation,
            appNamePattern = cleanedAppName,
            titlePattern = cleanedTitle,
            bodyPattern = cleanedBody,
            createdAt = existing.createdAt,
            updatedAt = nowMillis,
        )
        dao.upsert(updated)
        return updated.toModel()
    }

    suspend fun deleteRule(id: String) {
        dao.deleteById(id)
    }
}
