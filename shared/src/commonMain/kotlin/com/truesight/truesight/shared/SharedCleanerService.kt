package com.truesight.truesight.shared

class SharedCleanerService(
    private val policyStore: CleanerPolicyStore,
    private val redirectFollower: RedirectFollower
) {
    private val engine = UrlCleanerEngine(redirectFollower)

    fun cleanUrl(url: String): String {
        return engine.clean(url, policyStore.loadPolicy())
    }

    fun cleanFirstUrlFromText(text: String): String? {
        val firstUrl = UrlExtractor.extractFirstUrl(text) ?: return null
        return cleanUrl(firstUrl)
    }

    fun loadPolicy(): CleanerPolicy {
        return policyStore.loadPolicy()
    }

    fun savePolicy(policy: CleanerPolicy) {
        policyStore.savePolicy(policy)
    }
}
