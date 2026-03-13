package com.truesight.truesight

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UrlCleaner {
    data class CleanedLinkResult(
        val cleanedUrl: String,
        val paramsRemoved: Int,
        val redirectsFollowed: Int
    )

    fun clean(url: String): String {
        return com.truesight.truesight.shared.UrlCleanerCore.clean(url)
    }

    fun clean(url: String, resolveRedirect: (String) -> String): String {
        return com.truesight.truesight.shared.UrlCleanerCore.clean(url, resolveRedirect)
    }

    fun clean(url: String, policy: CleanerPolicy): String {
        return com.truesight.truesight.shared.UrlCleanerCore.clean(url, policy)
    }

    fun clean(url: String, policy: CleanerPolicy, resolveRedirect: (String) -> String): String {
        return com.truesight.truesight.shared.UrlCleanerCore.clean(url, policy, resolveRedirect)
    }

    suspend fun cleanWithResolvedRedirects(url: String): String {
        return cleanWithResolvedRedirects(url, CleanerPolicy())
    }

    suspend fun cleanWithResolvedRedirects(url: String, policy: CleanerPolicy): String {
        return cleanWithResolvedRedirectsWithStats(url, policy).cleanedUrl
    }

    suspend fun cleanWithResolvedRedirectsWithStats(url: String, policy: CleanerPolicy): CleanedLinkResult {
        return withContext(Dispatchers.IO) {
            var redirectsFollowed = 0
            val cleaned = com.truesight.truesight.shared.UrlCleanerCore.clean(url, policy) { input ->
                val followResult = RemoteRedirectFollower.followWithResult(input, policy)
                redirectsFollowed += followResult.redirectCount
                followResult.resolvedUrl
            }

            CleanedLinkResult(
                cleanedUrl = cleaned,
                paramsRemoved = estimateRemovedParams(url, cleaned),
                redirectsFollowed = redirectsFollowed
            )
        }
    }

    private fun estimateRemovedParams(originalUrl: String, cleanedUrl: String): Int {
        val originalCount = countQueryParams(originalUrl)
        val cleanedCount = countQueryParams(cleanedUrl)
        return (originalCount - cleanedCount).coerceAtLeast(0)
    }

    private fun countQueryParams(url: String): Int {
        val rawQuery = Uri.parse(url).encodedQuery ?: return 0
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
