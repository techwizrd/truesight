package com.truesight.truesight

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UrlCleaner {
    private val sharedEngine = com.truesight.truesight.shared.UrlCleanerEngine(RemoteRedirectFollower)

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
        return withContext(Dispatchers.IO) {
            sharedEngine.clean(url, policy)
        }
    }
}
