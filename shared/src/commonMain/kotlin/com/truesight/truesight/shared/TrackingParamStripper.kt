package com.truesight.truesight.shared

object TrackingParamStripper {
    // TODO: Precompute host/policy flags and flatten rule checks to reduce per-param scanning cost.
    private val exactTrackingKeys = setOf(
        "yclid",
        "amp",
        "amp_js_v",
        "__amp_source_origin",
        "mc_cid",
        "mc_eid",
        "igshid",
        "referer",
        "referrer",
        "ref_url",
        "referrer_url",
        "source_url",
        "ref_src",
        "mkt_tok",
        "s_cid",
        "cmpid"
    )

    private val trackingPrefixes = listOf(
        "ga_",
        "pk_",
        "vero_"
    )

    private val amazonTrackingKeys = setOf(
        "linkcode",
        "ascsubtag",
        "camp",
        "creative",
        "creativeasin",
        "smid",
        "pd_rd_i",
        "pd_rd_r",
        "pd_rd_w",
        "pd_rd_wg",
        "pd_rd_plhdr",
        "ref_"
    )

    private val amazonTrackingPrefixes = listOf(
        "pd_rd_",
        "pf_rd_"
    )

    private val stripRules: List<StripRule> = listOf(
        ExactKeyRule(exactTrackingKeys),
        PrefixRule(trackingPrefixes),
        PolicyPrefixRule(
            policyEnabled = { it.utmTrackingStripEnabled },
            prefixes = listOf("utm_")
        ),
        PolicyExactKeyRule(
            policyEnabled = { it.googleAdsTrackingStripEnabled },
            keys = setOf(
                "gclid",
                "dclid",
                "rdclid",
                "gclsrc",
                "gbraid",
                "wbraid",
                "gad_source",
                "gad_campaignid",
                "gad_adgroupid",
                "gad_keywordid",
                "gad_adid"
            )
        ),
        PolicyExactKeyRule(
            policyEnabled = { it.metaAdsTrackingStripEnabled },
            keys = setOf("fbclid", "fbadid")
        ),
        PolicyExactKeyRule(
            policyEnabled = { it.microsoftAdsTrackingStripEnabled },
            keys = setOf("msclkid")
        ),
        PolicyExactKeyRule(
            policyEnabled = { it.tiktokAdsTrackingStripEnabled },
            keys = setOf("ttclid", "ttadid")
        ),
        PolicyExactKeyRule(
            policyEnabled = { it.twitterAdsTrackingStripEnabled },
            keys = setOf("twclid")
        ),
        PolicyExactKeyRule(
            policyEnabled = { it.linkedInAdsTrackingStripEnabled },
            keys = setOf("li_fat_id")
        ),
        PolicyExactKeyRule(
            policyEnabled = { it.pinterestAdsTrackingStripEnabled },
            keys = setOf("epik")
        ),
        PolicyExactKeyRule(
            policyEnabled = { it.snapchatAdsTrackingStripEnabled },
            keys = setOf("sccid", "sc_click_id")
        ),
        HostExactKeyRule(
            hostMatcher = DomainMatchers::isInstagramHost,
            keys = setOf("igsh", "igshid", "shid")
        ),
        HostExactKeyRule(
            hostMatcher = DomainMatchers::isZillowHost,
            keys = setOf("rtoken", "mibextid")
        ),
        HostExactKeyRule(
            hostMatcher = DomainMatchers::isRedfinHost,
            keys = setOf("riftinfo", "rf_source", "rf_referrer")
        ),
        HostExactKeyRule(
            hostMatcher = DomainMatchers::isRedditHost,
            keys = setOf("share_id", "rdt")
        ),
        HostExactKeyRule(
            hostMatcher = DomainMatchers::isMediumHost,
            keys = setOf("source", "sk")
        ),
        AggressiveGoogleAdsRule,
        AmazonRule
    )

    fun strip(url: String): String {
        return strip(url, CleanerPolicy())
    }

    fun strip(url: String, policy: CleanerPolicy): String {
        val parts = UrlPartsParser.parse(url) ?: return trimEmptyFragment(url)
        val rawQuery = parts.rawQuery ?: return trimEmptyFragment(url)

        if (!policy.isStripEnabledForHost(parts.host)) {
            return trimEmptyFragment(url)
        }

        val cleanedQuery = buildCleanedQuery(
            rawQuery = rawQuery,
            host = parts.host,
            policy = policy
        )

        return trimEmptyFragment(parts.build(cleanedQuery))
    }

    private fun buildCleanedQuery(rawQuery: String, host: String, policy: CleanerPolicy): String? {
        val builder = StringBuilder(rawQuery.length)
        var start = 0

        while (start <= rawQuery.length) {
            val separatorIndex = rawQuery.indexOf('&', start)
            val end = if (separatorIndex == -1) rawQuery.length else separatorIndex
            if (end > start && !shouldStrip(host, policy, rawQuery, start, end)) {
                if (builder.isNotEmpty()) {
                    builder.append('&')
                }
                builder.append(rawQuery, start, end)
            }

            if (separatorIndex == -1) {
                break
            }
            start = separatorIndex + 1
        }

        return builder.toString().ifBlank { null }
    }

    private fun shouldStrip(host: String, policy: CleanerPolicy, rawQuery: String, start: Int, end: Int): Boolean {
        val equalsIndex = rawQuery.indexOf('=', start).takeIf { it in (start + 1) until end } ?: end
        if (equalsIndex <= start) {
            return false
        }

        val key = rawQuery.substring(start, equalsIndex).lowercase()
        return stripRules.any { rule -> rule.shouldStrip(host, policy, key) }
    }

    private interface StripRule {
        fun shouldStrip(host: String, policy: CleanerPolicy, key: String): Boolean
    }

    private class ExactKeyRule(private val keys: Set<String>) : StripRule {
        override fun shouldStrip(host: String, policy: CleanerPolicy, key: String): Boolean {
            return key in keys
        }
    }

    private class PrefixRule(private val prefixes: List<String>) : StripRule {
        override fun shouldStrip(host: String, policy: CleanerPolicy, key: String): Boolean {
            return prefixes.any { prefix -> key.startsWith(prefix) }
        }
    }

    private class PolicyExactKeyRule(
        private val policyEnabled: (CleanerPolicy) -> Boolean,
        private val keys: Set<String>
    ) : StripRule {
        override fun shouldStrip(host: String, policy: CleanerPolicy, key: String): Boolean {
            return policyEnabled(policy) && key in keys
        }
    }

    private class PolicyPrefixRule(
        private val policyEnabled: (CleanerPolicy) -> Boolean,
        private val prefixes: List<String>
    ) : StripRule {
        override fun shouldStrip(host: String, policy: CleanerPolicy, key: String): Boolean {
            return policyEnabled(policy) && prefixes.any { prefix -> key.startsWith(prefix) }
        }
    }

    private class HostExactKeyRule(
        private val hostMatcher: (String) -> Boolean,
        private val keys: Set<String>
    ) : StripRule {
        override fun shouldStrip(host: String, policy: CleanerPolicy, key: String): Boolean {
            return hostMatcher(host) && key in keys
        }
    }

    private object AmazonRule : StripRule {
        override fun shouldStrip(host: String, policy: CleanerPolicy, key: String): Boolean {
            if (!DomainMatchers.isAmazonHost(host)) {
                return false
            }

            if (key in amazonTrackingKeys || key == "ref") {
                return true
            }
            if (policy.amazonRemoveAffiliateTagEnabled && key == "tag") {
                return true
            }
            return amazonTrackingPrefixes.any { prefix -> key.startsWith(prefix) }
        }
    }

    private object AggressiveGoogleAdsRule : StripRule {
        override fun shouldStrip(host: String, policy: CleanerPolicy, key: String): Boolean {
            return policy.googleAdsTrackingStripEnabled &&
                policy.aggressiveGoogleAdsStrippingEnabled &&
                key.startsWith("gad_")
        }
    }

    private fun trimEmptyFragment(url: String): String {
        return if (url.endsWith("#")) url.dropLast(1) else url
    }
}
