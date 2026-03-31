package dev.yakitori.barkforwarder.data.model

data class AppRule(
    val packageName: String,
    val appLabel: String,
    val excluded: Boolean,
    val manualIconUrl: String?,
    val cachedResolvedIconUrl: String?,
    val iconResolvedAt: Long?,
)

