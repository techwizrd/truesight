package com.truesight.truesight.shared

interface CleanerPolicyStore {
    fun loadPolicy(): CleanerPolicy
    fun savePolicy(policy: CleanerPolicy)
}

fun interface RedirectFollower {
    fun follow(url: String, policy: CleanerPolicy): String
}

data class RedirectFollowResult(
    val resolvedUrl: String,
    val redirectCount: Int
)

interface RedirectFollowerWithStats {
    fun followWithResult(url: String, policy: CleanerPolicy): RedirectFollowResult
}

fun interface RedirectLocationFetcher {
    fun fetchRedirectLocation(url: String): String?
}

fun interface RedirectBodyFetcher {
    fun fetchBody(url: String): String?
}
