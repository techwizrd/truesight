package com.truesight.truesight.shared

object UrlCleanerCore {
    fun clean(url: String): String {
        return clean(url, CleanerPolicy()) { it }
    }

    fun clean(url: String, resolveRedirect: (String) -> String): String {
        return clean(url, CleanerPolicy(), resolveRedirect)
    }

    fun clean(url: String, policy: CleanerPolicy): String {
        return clean(url, policy) { it }
    }

    fun clean(url: String, policy: CleanerPolicy, resolveRedirect: (String) -> String): String {
        return LinkSanitizationPipeline(
            policy = policy,
            resolveRedirect = resolveRedirect
        ).run(url)
    }
}
