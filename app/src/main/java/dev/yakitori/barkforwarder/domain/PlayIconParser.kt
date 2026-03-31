package dev.yakitori.barkforwarder.domain

object PlayIconParser {
    private val patterns = listOf(
        Regex("""property=["']og:image["'][^>]*content=["']([^"']*play-lh\.googleusercontent\.com[^"']+)["']""", RegexOption.IGNORE_CASE),
        Regex("""itemprop=["']image["'][^>]*content=["']([^"']*play-lh\.googleusercontent\.com[^"']+)["']""", RegexOption.IGNORE_CASE),
        Regex("""https://play-lh\.googleusercontent\.com/[A-Za-z0-9_\-./=:+?&%]+"""),
    )

    fun extractIconUrl(html: String): String? {
        val normalized = html
            .replace("\\u003d", "=")
            .replace("&amp;", "&")
        return patterns.firstNotNullOfOrNull { regex ->
            regex.find(normalized)?.groupValues?.getOrNull(1) ?: regex.find(normalized)?.value
        }
    }
}

