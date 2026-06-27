package com.truesight.truesight.shared

class SharedCleanerService(
    private val policyStore: CleanerPolicyStore,
    private val redirectFollower: RedirectFollower
) {
    private val engine = UrlCleanerEngine(redirectFollower)

    data class CleanFirstUrlResult(
        val originalUrl: String,
        val cleanedUrl: String,
        val paramsRemoved: Int,
        val redirectsFollowed: Int
    )

    fun cleanUrl(url: String): String {
        return engine.clean(url, policyStore.loadPolicy())
    }

    fun cleanFirstUrlFromText(text: String): String? {
        val firstUrl = UrlExtractor.extractFirstUrl(text) ?: return null
        return cleanUrl(firstUrl)
    }

    fun cleanFirstUrlFromTextWithResult(text: String): CleanFirstUrlResult? {
        val firstUrl = UrlExtractor.extractFirstUrl(text) ?: return null
        val cleanResult = cleanUrlWithStats(firstUrl)
        return CleanFirstUrlResult(
            originalUrl = firstUrl,
            cleanedUrl = cleanResult.cleanedUrl,
            paramsRemoved = estimateRemovedParams(firstUrl, cleanResult.cleanedUrl),
            redirectsFollowed = cleanResult.redirectsFollowed
        )
    }

    fun loadPolicy(): CleanerPolicy {
        return policyStore.loadPolicy()
    }

    fun savePolicy(policy: CleanerPolicy) {
        policyStore.savePolicy(policy)
    }

    private fun estimateRemovedParams(originalUrl: String, cleanedUrl: String): Int {
        val originalCount = countQueryParams(originalUrl)
        val cleanedCount = countQueryParams(cleanedUrl)
        return (originalCount - cleanedCount).coerceAtLeast(0)
    }

    private data class CleanUrlResult(
        val cleanedUrl: String,
        val redirectsFollowed: Int
    )

    private fun cleanUrlWithStats(url: String): CleanUrlResult {
        val policy = policyStore.loadPolicy()
        var redirectsFollowed = 0
        val cleanedUrl = UrlCleanerCore.clean(url, policy) { input ->
            val followed = (redirectFollower as? RedirectFollowerWithStats)
                ?.followWithResult(input, policy)
            if (followed != null) {
                redirectsFollowed += followed.redirectCount
                followed.resolvedUrl
            } else {
                redirectFollower.follow(input, policy)
            }
        }
        return CleanUrlResult(cleanedUrl = cleanedUrl, redirectsFollowed = redirectsFollowed)
    }

    private fun countQueryParams(url: String): Int {
        val rawQuery = UrlPartsParser.parse(url)?.rawQuery ?: return 0
        if (rawQuery.isBlank()) {
            return 0
        }

        var count = 0
        var start = 0
        while (start <= rawQuery.length) {
            val separatorIndex = rawQuery.indexOf('&', start)
            val end = if (separatorIndex == -1) rawQuery.length else separatorIndex
            if (end > start) {
                count += 1
            }
            if (separatorIndex == -1) {
                break
            }
            start = separatorIndex + 1
        }
        return count
    }
}
