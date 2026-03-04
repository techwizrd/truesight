package com.truesight.truesight.shared

data class AdTrackingPolicy(
    val googleEnabled: Boolean = true,
    val googleAggressiveEnabled: Boolean = false,
    val metaEnabled: Boolean = true,
    val microsoftEnabled: Boolean = true,
    val tiktokEnabled: Boolean = true,
    val twitterEnabled: Boolean = true,
    val linkedInEnabled: Boolean = true,
    val pinterestEnabled: Boolean = true,
    val snapchatEnabled: Boolean = true
)

data class CleanerPolicy(
    val googleShareRedirectEnabled: Boolean = true,
    val googleShareStripEnabled: Boolean = true,
    val redditRedirectEnabled: Boolean = true,
    val redditStripEnabled: Boolean = true,
    val amazonRedirectEnabled: Boolean = true,
    val amazonStripEnabled: Boolean = true,
    val amazonRemoveAffiliateTagEnabled: Boolean = true,
    val instagramRedirectEnabled: Boolean = true,
    val instagramStripEnabled: Boolean = true,
    val ampCacheRedirectEnabled: Boolean = true,
    val ampCacheStripEnabled: Boolean = true,
    val twitterToNitterEnabled: Boolean = false,
    val adTracking: AdTrackingPolicy = AdTrackingPolicy(),
    val utmTrackingStripEnabled: Boolean = true
) {
    fun isRedirectEnabledForHost(host: String): Boolean {
        return resolveHostPolicy(host) { it.redirectEnabled(this) }
    }

    fun isStripEnabledForHost(host: String): Boolean {
        return resolveHostPolicy(host) { it.stripEnabled(this) }
    }

    private fun resolveHostPolicy(host: String, selector: (HostPolicyRule) -> Boolean): Boolean {
        val rule = hostPolicyRules.firstOrNull { it.matches(host) } ?: return true
        return selector(rule)
    }

    private data class HostPolicyRule(
        val matches: (String) -> Boolean,
        val redirectEnabled: (CleanerPolicy) -> Boolean,
        val stripEnabled: (CleanerPolicy) -> Boolean
    )

    private companion object {
        private val hostPolicyRules = listOf(
            HostPolicyRule(
                matches = DomainMatchers::isGoogleShareHost,
                redirectEnabled = { it.googleShareRedirectEnabled },
                stripEnabled = { it.googleShareStripEnabled }
            ),
            HostPolicyRule(
                matches = DomainMatchers::isRedditHost,
                redirectEnabled = { it.redditRedirectEnabled },
                stripEnabled = { it.redditStripEnabled }
            ),
            HostPolicyRule(
                matches = DomainMatchers::isAmazonHost,
                redirectEnabled = { it.amazonRedirectEnabled },
                stripEnabled = { it.amazonStripEnabled }
            ),
            HostPolicyRule(
                matches = DomainMatchers::isInstagramHost,
                redirectEnabled = { it.instagramRedirectEnabled },
                stripEnabled = { it.instagramStripEnabled }
            ),
            HostPolicyRule(
                matches = DomainMatchers::isAmpCacheHost,
                redirectEnabled = { it.ampCacheRedirectEnabled },
                stripEnabled = { it.ampCacheStripEnabled }
            )
        )
    }
}
