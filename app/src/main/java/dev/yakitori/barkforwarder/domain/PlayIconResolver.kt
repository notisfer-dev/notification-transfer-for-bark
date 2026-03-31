package dev.yakitori.barkforwarder.domain

import dev.yakitori.barkforwarder.data.repo.AppRuleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class PlayIconResolver(
    private val httpClient: OkHttpClient,
    private val appRuleRepository: AppRuleRepository,
) {
    suspend fun resolve(packageName: String): String? {
        val rule = appRuleRepository.getRule(packageName)
        rule?.manualIconUrl?.takeIf { it.isNotBlank() }?.let { return it }

        val now = System.currentTimeMillis()
        val cached = rule?.cachedResolvedIconUrl
        if (!cached.isNullOrBlank() && now - (rule.iconResolvedAt ?: 0L) < CACHE_TTL_MILLIS) {
            return cached
        }

        val resolved = fetchFromPlay(packageName)
        appRuleRepository.cacheResolvedIconUrl(packageName, resolved)
        return resolved
    }

    private suspend fun fetchFromPlay(packageName: String): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://play.google.com/store/apps/details?id=$packageName&hl=ja")
            .header("User-Agent", "Mozilla/5.0")
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val html = response.body?.string().orEmpty()
            PlayIconParser.extractIconUrl(html)
        }
    }

    private companion object {
        const val CACHE_TTL_MILLIS = 7L * 24L * 60L * 60L * 1000L
    }
}

