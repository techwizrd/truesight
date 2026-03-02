package com.truesight.truesight.shared

internal object DomainMatchers {
    private fun isHostOrSubdomain(host: String, root: String): Boolean {
        return host == root || host.endsWith(".$root")
    }

    fun isGoogleShareHost(host: String): Boolean {
        return host == "share.google.com" || host == "share.google"
    }

    fun isRedditHost(host: String): Boolean {
        return host == "redd.it" || isHostOrSubdomain(host, "reddit.com")
    }

    fun isAmazonHost(host: String): Boolean {
        return host == "amzn.to" || host == "a.co" || host.startsWith("amazon.") || host.contains(".amazon.")
    }

    fun isInstagramHost(host: String): Boolean {
        return isHostOrSubdomain(host, "instagram.com")
    }

    fun isAmpCacheHost(host: String): Boolean {
        return isHostOrSubdomain(host, "cdn.ampproject.org")
    }

    fun isZillowHost(host: String): Boolean {
        return isHostOrSubdomain(host, "zillow.com")
    }

    fun isRedfinHost(host: String): Boolean {
        return isHostOrSubdomain(host, "redfin.com")
    }

    fun isMediumHost(host: String): Boolean {
        return isHostOrSubdomain(host, "medium.com")
    }
}
