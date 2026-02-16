package com.truesight.truesight.shared

class UrlCleanerEngine(
    private val redirectFollower: RedirectFollower
) {
    fun clean(url: String, policy: CleanerPolicy): String {
        return UrlCleanerCore.clean(url, policy) { input ->
            redirectFollower.follow(input, policy)
        }
    }

    fun clean(url: String): String {
        return clean(url, CleanerPolicy())
    }
}
