package com.truesight.truesight.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UrlCleanerCoreTest {
    @Test
    fun unwrapsGoogleRedirectAndStripsTracking() {
        val dirty = "https://www.google.com/url?q=https%3A%2F%2Fexample.com%2Farticle%3Futm_source%3Dnewsletter%26id%3D42&sa=D"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/article?id=42", cleaned)
    }

    @Test
    fun unwrapsAmazonSlredirectAndCleansParams() {
        val dirty = "https://www.amazon.com/gp/slredirect/picassoRedirect.html?url=https%3A%2F%2Fwww.amazon.com%2Fdp%2FB000123456%3Ftag%3Dmytag-20%26linkCode%3Dll1%26psc%3D1"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://www.amazon.com/dp/B000123456?psc=1", cleaned)
    }

    @Test
    fun unwrapsAmpCacheVariants() {
        val dirty = "https://example-com.cdn.ampproject.org/v/s/example.com/story%3Famp%3D1%26id%3D11"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/story?id=11", cleaned)
    }

    @Test
    fun keepsAmazonTagWhenSettingDisabled() {
        val dirty = "https://www.amazon.com/dp/B000123456?tag=mytag-20&linkCode=ll1&psc=1&id=7"
        val cleaned = UrlCleanerCore.clean(dirty, CleanerPolicy(amazonRemoveAffiliateTagEnabled = false))
        assertEquals("https://www.amazon.com/dp/B000123456?tag=mytag-20&psc=1&id=7", cleaned)
    }

    @Test
    fun rewritesTwitterToNitterWhenEnabled() {
        val dirty = "https://twitter.com/someuser/status/123?utm_source=share&id=9"
        val cleaned = UrlCleanerCore.clean(dirty, CleanerPolicy(twitterToNitterEnabled = true))
        assertEquals("https://nitter.net/someuser/status/123?id=9", cleaned)
    }

    @Test
    fun usesInjectedRedirectResolver() {
        val dirty = "https://share.google.com/some/link"
        val seen = mutableListOf<String>()
        val cleaned = UrlCleanerCore.clean(dirty) { incoming ->
            seen += incoming
            "https://example.com/story?utm_source=reddit&id=10"
        }
        assertEquals("https://example.com/story?id=10", cleaned)
        assertTrue(seen.first().startsWith("https://share.google.com"))
    }

    @Test
    fun appliesStripPolicyUsingFinalResolvedHost() {
        val dirty = "https://share.google.com/some/link"
        val cleaned = UrlCleanerCore.clean(
            dirty,
            CleanerPolicy(googleShareStripEnabled = false)
        ) {
            "https://example.com/path?utm_source=share&id=9"
        }

        assertEquals("https://example.com/path?id=9", cleaned)
    }

    @Test
    fun engineUsesRedirectFollowerContract() {
        val engine = UrlCleanerEngine(
            RedirectFollower { _, _ -> "https://example.com/path?utm_source=x&id=9" }
        )
        val cleaned = engine.clean("https://amzn.to/abc123", CleanerPolicy())
        assertEquals("https://example.com/path?id=9", cleaned)
    }

    @Test
    fun stripsRedditShareParamsFromPermalinks() {
        val dirty = "https://www.reddit.com/r/BestofRedditorUpdates/comments/1r6eevy/my_husband_32m_is_insisting_that_we_impregnate/?share_id=GEbCOYXAewJQ0u9cvWf5V&rdt=12345&id=9"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals(
            "https://www.reddit.com/r/BestofRedditorUpdates/comments/1r6eevy/my_husband_32m_is_insisting_that_we_impregnate/?id=9",
            cleaned
        )
    }

    @Test
    fun keepsShareIdAndRdtOnNonRedditHosts() {
        val dirty = "https://example.com/path?share_id=abc&rdt=xyz&id=9"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/path?share_id=abc&rdt=xyz&id=9", cleaned)
    }

    @Test
    fun stripsMediumTrackingParamsOnMediumHosts() {
        val dirty = "https://medium.com/@team/story-title-abc123?source=linkShare-abc&sk=deadbeef&id=9"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://medium.com/@team/story-title-abc123?id=9", cleaned)
    }

    @Test
    fun keepsMediumTrackingParamsOnNonMediumHosts() {
        val dirty = "https://example.com/path?source=linkShare-abc&sk=deadbeef&id=9"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/path?source=linkShare-abc&sk=deadbeef&id=9", cleaned)
    }

    @Test
    fun stripsUtmParamsByDefaultAcrossHosts() {
        val dirty = "https://example.com/path?utm_source=share&utm_medium=social&id=9"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/path?id=9", cleaned)
    }

    @Test
    fun keepsUtmParamsWhenToggleIsDisabled() {
        val dirty = "https://example.com/path?utm_source=share&utm_medium=social&id=9"
        val cleaned = UrlCleanerCore.clean(
            dirty,
            CleanerPolicy(utmTrackingStripEnabled = false)
        )
        assertEquals("https://example.com/path?utm_source=share&utm_medium=social&id=9", cleaned)
    }

    @Test
    fun stripsMixedCaseTrackingKeys() {
        val dirty = "https://example.com/path?UTM_Source=share&GCLID=abc&id=9"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/path?id=9", cleaned)
    }

    @Test
    fun preservesDuplicateNonTrackingParamsWhileStrippingDuplicatesOfTrackingParams() {
        val dirty = "https://example.com/path?id=1&utm_source=a&id=2&utm_medium=b"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/path?id=1&id=2", cleaned)
    }

    @Test
    fun stripsKnownGoogleAdsParamsByDefault() {
        val dirty = "https://example.com/path?gad_source=1&gad_campaignid=55&gbraid=abc&rdclid=77&id=9"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/path?id=9", cleaned)
    }

    @Test
    fun stripsCommonNonGoogleAdNetworkParamsByDefault() {
        val dirty = "https://example.com/path?fbclid=9&fbadid=0&msclkid=1&ttclid=2&ttadid=22&twclid=3&li_fat_id=4&epik=5&sccid=6&sc_click_id=7&id=9"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/path?id=9", cleaned)
    }

    @Test
    fun keepsAdParamsWhenTheirGroupsAreDisabled() {
        val dirty = "https://example.com/path?fbclid=9&fbadid=0&msclkid=1&ttclid=2&ttadid=22&twclid=3&li_fat_id=4&epik=5&sccid=6&id=9"
        val cleaned = UrlCleanerCore.clean(
            dirty,
            CleanerPolicy(
                metaAdsTrackingStripEnabled = false,
                microsoftAdsTrackingStripEnabled = false,
                tiktokAdsTrackingStripEnabled = false,
                twitterAdsTrackingStripEnabled = false,
                linkedInAdsTrackingStripEnabled = false,
                pinterestAdsTrackingStripEnabled = false,
                snapchatAdsTrackingStripEnabled = false
            )
        )
        assertEquals(
            "https://example.com/path?fbclid=9&fbadid=0&msclkid=1&ttclid=2&ttadid=22&twclid=3&li_fat_id=4&epik=5&sccid=6&id=9",
            cleaned
        )
    }

    @Test
    fun keepsUnknownGadParamWhenAggressiveModeIsDisabled() {
        val dirty = "https://example.com/path?gad_custom=123&id=9"
        val cleaned = UrlCleanerCore.clean(dirty)
        assertEquals("https://example.com/path?gad_custom=123&id=9", cleaned)
    }

    @Test
    fun stripsUnknownGadParamWhenAggressiveModeIsEnabled() {
        val dirty = "https://example.com/path?gad_custom=123&id=9"
        val cleaned = UrlCleanerCore.clean(
            dirty,
            CleanerPolicy(aggressiveGoogleAdsStrippingEnabled = true)
        )
        assertEquals("https://example.com/path?id=9", cleaned)
    }

    @Test
    fun keepsUnknownGadParamWhenGoogleAdsGroupIsDisabled() {
        val dirty = "https://example.com/path?gad_custom=123&gad_source=1&id=9"
        val cleaned = UrlCleanerCore.clean(
            dirty,
            CleanerPolicy(
                googleAdsTrackingStripEnabled = false,
                aggressiveGoogleAdsStrippingEnabled = true
            )
        )
        assertEquals("https://example.com/path?gad_custom=123&gad_source=1&id=9", cleaned)
    }
}
