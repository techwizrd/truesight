package com.truesight.truesight.shared

internal object DomainMatchers {
    fun isGoogleShareHost(host: String): Boolean {
        return host == "share.google.com" || host == "share.google"
    }

    fun isRedditHost(host: String): Boolean {
        return host == "redd.it" || host == "reddit.com" || host.endsWith(".reddit.com")
    }

    fun isAmazonHost(host: String): Boolean {
        return host == "amzn.to" || host == "a.co" || host.startsWith("amazon.") || host.contains(".amazon.")
    }

    fun isInstagramHost(host: String): Boolean {
        return host == "instagram.com" || host.endsWith(".instagram.com")
    }

    fun isAmpCacheHost(host: String): Boolean {
        return host == "cdn.ampproject.org" || host.endsWith(".cdn.ampproject.org")
    }

    fun isZillowHost(host: String): Boolean {
        return host == "zillow.com" || host.endsWith(".zillow.com")
    }

    fun isRedfinHost(host: String): Boolean {
        return host == "redfin.com" || host.endsWith(".redfin.com")
    }
}
