package com.truesight.truesight.shared

object TrackingParamStripper {
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
        "cmpid",
        "ad_name",
        "adset_name",
        "campaign_id",
        "ad_id"
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

    private val googleAdsTrackingKeys = setOf(
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

    private val metaAdsTrackingKeys = setOf("fbclid", "fbadid")
    private val microsoftAdsTrackingKeys = setOf("msclkid")
    private val tiktokAdsTrackingKeys = setOf("ttclid", "ttadid")
    private val twitterAdsTrackingKeys = setOf("twclid")
    private val linkedInAdsTrackingKeys = setOf("li_fat_id")
    private val pinterestAdsTrackingKeys = setOf("epik")
    private val snapchatAdsTrackingKeys = setOf("sccid", "sc_click_id")

    private val instagramHostTrackingKeys = setOf("igsh", "igshid", "shid")
    private val zillowHostTrackingKeys = setOf("rtoken", "mibextid")
    private val redfinHostTrackingKeys = setOf("riftinfo", "rf_source", "rf_referrer")
    private val redditHostTrackingKeys = setOf("share_id", "rdt")
    private val mediumHostTrackingKeys = setOf("source", "sk")
    private val prefixedAdCampaignSuffixes = listOf(
        "_ad_name",
        "_adset_name",
        "_campaign_id",
        "_ad_id"
    )

    private data class StripContext(
        val isAmazonHost: Boolean,
        val isInstagramHost: Boolean,
        val isZillowHost: Boolean,
        val isRedfinHost: Boolean,
        val isRedditHost: Boolean,
        val isMediumHost: Boolean,
        val stripUtm: Boolean,
        val stripGoogleAds: Boolean,
        val stripAggressiveGoogleAds: Boolean,
        val stripMetaAds: Boolean,
        val stripMicrosoftAds: Boolean,
        val stripTikTokAds: Boolean,
        val stripTwitterAds: Boolean,
        val stripLinkedInAds: Boolean,
        val stripPinterestAds: Boolean,
        val stripSnapchatAds: Boolean,
        val stripAmazonAffiliateTag: Boolean
    )

    fun strip(url: String): String {
        return strip(url, CleanerPolicy())
    }

    fun strip(url: String, policy: CleanerPolicy): String {
        val parts = UrlPartsParser.parse(url) ?: return trimEmptyFragment(url)
        return strip(parts, policy)
    }

    internal fun strip(parts: UrlParts, policy: CleanerPolicy): String {
        val rawQuery = parts.rawQuery ?: return trimEmptyFragment(parts.build())

        if (!policy.isStripEnabledForHost(parts.host)) {
            return trimEmptyFragment(parts.build())
        }

        val context = StripContext(
            isAmazonHost = DomainMatchers.isAmazonHost(parts.host),
            isInstagramHost = DomainMatchers.isInstagramHost(parts.host),
            isZillowHost = DomainMatchers.isZillowHost(parts.host),
            isRedfinHost = DomainMatchers.isRedfinHost(parts.host),
            isRedditHost = DomainMatchers.isRedditHost(parts.host),
            isMediumHost = DomainMatchers.isMediumHost(parts.host),
            stripUtm = policy.utmTrackingStripEnabled,
            stripGoogleAds = policy.adTracking.googleEnabled,
            stripAggressiveGoogleAds = policy.adTracking.googleEnabled && policy.adTracking.googleAggressiveEnabled,
            stripMetaAds = policy.adTracking.metaEnabled,
            stripMicrosoftAds = policy.adTracking.microsoftEnabled,
            stripTikTokAds = policy.adTracking.tiktokEnabled,
            stripTwitterAds = policy.adTracking.twitterEnabled,
            stripLinkedInAds = policy.adTracking.linkedInEnabled,
            stripPinterestAds = policy.adTracking.pinterestEnabled,
            stripSnapchatAds = policy.adTracking.snapchatEnabled,
            stripAmazonAffiliateTag = policy.amazonRemoveAffiliateTagEnabled
        )

        val cleanedQuery = buildCleanedQuery(
            rawQuery = rawQuery,
            context = context
        )

        return trimEmptyFragment(parts.build(rawQueryOverride = cleanedQuery))
    }

    private fun buildCleanedQuery(rawQuery: String, context: StripContext): String? {
        val builder = StringBuilder(rawQuery.length)
        var start = 0

        while (start <= rawQuery.length) {
            val separatorIndex = rawQuery.indexOf('&', start)
            val end = if (separatorIndex == -1) rawQuery.length else separatorIndex
            if (end > start && !shouldStrip(context, rawQuery, start, end)) {
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

    private fun shouldStrip(context: StripContext, rawQuery: String, start: Int, end: Int): Boolean {
        val equalsIndex = rawQuery.indexOf('=', start).takeIf { it in (start + 1) until end } ?: end
        if (equalsIndex <= start) {
            return false
        }

        val key = rawQuery.substring(start, equalsIndex).lowercase()
        if (key in exactTrackingKeys || startsWithAny(key, trackingPrefixes)) {
            return true
        }

        if (context.stripUtm && isUtmLikeKey(key)) {
            return true
        }

        if (endsWithAny(key, prefixedAdCampaignSuffixes)) {
            return true
        }

        if (context.stripGoogleAds && key in googleAdsTrackingKeys) {
            return true
        }
        if (context.stripAggressiveGoogleAds && key.startsWith("gad_")) {
            return true
        }

        if (context.stripMetaAds && key in metaAdsTrackingKeys) {
            return true
        }
        if (context.stripMicrosoftAds && key in microsoftAdsTrackingKeys) {
            return true
        }
        if (context.stripTikTokAds && key in tiktokAdsTrackingKeys) {
            return true
        }
        if (context.stripTwitterAds && key in twitterAdsTrackingKeys) {
            return true
        }
        if (context.stripLinkedInAds && key in linkedInAdsTrackingKeys) {
            return true
        }
        if (context.stripPinterestAds && key in pinterestAdsTrackingKeys) {
            return true
        }
        if (context.stripSnapchatAds && key in snapchatAdsTrackingKeys) {
            return true
        }

        if (context.isInstagramHost && key in instagramHostTrackingKeys) {
            return true
        }
        if (context.isZillowHost && key in zillowHostTrackingKeys) {
            return true
        }
        if (context.isRedfinHost && key in redfinHostTrackingKeys) {
            return true
        }
        if (context.isRedditHost && key in redditHostTrackingKeys) {
            return true
        }
        if (context.isMediumHost && key in mediumHostTrackingKeys) {
            return true
        }

        if (!context.isAmazonHost) {
            return false
        }

        if (key in amazonTrackingKeys || key == "ref") {
            return true
        }
        if (context.stripAmazonAffiliateTag && key == "tag") {
            return true
        }
        return startsWithAny(key, amazonTrackingPrefixes)
    }

    private fun startsWithAny(key: String, prefixes: List<String>): Boolean {
        return prefixes.any { prefix -> key.startsWith(prefix) }
    }

    private fun endsWithAny(key: String, suffixes: List<String>): Boolean {
        return suffixes.any { suffix -> key.endsWith(suffix) }
    }

    private fun isUtmLikeKey(key: String): Boolean {
        if (key.startsWith("utm_")) {
            return true
        }
        val marker = "_utm_"
        val markerIndex = key.indexOf(marker)
        return markerIndex != -1 && markerIndex + marker.length < key.length
    }

    private fun trimEmptyFragment(url: String): String {
        return if (url.endsWith("#")) url.dropLast(1) else url
    }
}
