package com.truesight.truesight

import android.content.Context
import com.truesight.truesight.shared.CleanerPolicyStore

class CleanerSettingsStore(context: Context) : CleanerPolicyStore {
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override fun loadPolicy(): CleanerPolicy {
        return CleanerPolicy(
            googleShareRedirectEnabled = prefs.getBoolean(keyGoogleShareRedirect, true),
            googleShareStripEnabled = prefs.getBoolean(keyGoogleShareStrip, true),
            redditRedirectEnabled = prefs.getBoolean(keyRedditRedirect, true),
            redditStripEnabled = prefs.getBoolean(keyRedditStrip, true),
            amazonRedirectEnabled = prefs.getBoolean(keyAmazonRedirect, true),
            amazonStripEnabled = prefs.getBoolean(keyAmazonStrip, true),
            amazonRemoveAffiliateTagEnabled = prefs.getBoolean(keyAmazonRemoveAffiliateTag, true),
            instagramRedirectEnabled = prefs.getBoolean(keyInstagramRedirect, true),
            instagramStripEnabled = prefs.getBoolean(keyInstagramStrip, true),
            ampCacheRedirectEnabled = prefs.getBoolean(keyAmpCacheRedirect, true),
            ampCacheStripEnabled = prefs.getBoolean(keyAmpCacheStrip, true),
            twitterToNitterEnabled = prefs.getBoolean(keyTwitterToNitter, false),
            googleAdsTrackingStripEnabled = prefs.getBoolean(keyGoogleAdsTrackingStrip, true),
            metaAdsTrackingStripEnabled = prefs.getBoolean(keyMetaAdsTrackingStrip, true),
            microsoftAdsTrackingStripEnabled = prefs.getBoolean(keyMicrosoftAdsTrackingStrip, true),
            tiktokAdsTrackingStripEnabled = prefs.getBoolean(keyTiktokAdsTrackingStrip, true),
            twitterAdsTrackingStripEnabled = prefs.getBoolean(keyTwitterAdsTrackingStrip, true),
            linkedInAdsTrackingStripEnabled = prefs.getBoolean(keyLinkedInAdsTrackingStrip, true),
            pinterestAdsTrackingStripEnabled = prefs.getBoolean(keyPinterestAdsTrackingStrip, true),
            snapchatAdsTrackingStripEnabled = prefs.getBoolean(keySnapchatAdsTrackingStrip, true),
            aggressiveGoogleAdsStrippingEnabled = prefs.getBoolean(keyAggressiveGoogleAdsStripping, false)
        )
    }

    override fun savePolicy(policy: CleanerPolicy) {
        prefs.edit()
            .putBoolean(keyGoogleShareRedirect, policy.googleShareRedirectEnabled)
            .putBoolean(keyGoogleShareStrip, policy.googleShareStripEnabled)
            .putBoolean(keyRedditRedirect, policy.redditRedirectEnabled)
            .putBoolean(keyRedditStrip, policy.redditStripEnabled)
            .putBoolean(keyAmazonRedirect, policy.amazonRedirectEnabled)
            .putBoolean(keyAmazonStrip, policy.amazonStripEnabled)
            .putBoolean(keyAmazonRemoveAffiliateTag, policy.amazonRemoveAffiliateTagEnabled)
            .putBoolean(keyInstagramRedirect, policy.instagramRedirectEnabled)
            .putBoolean(keyInstagramStrip, policy.instagramStripEnabled)
            .putBoolean(keyAmpCacheRedirect, policy.ampCacheRedirectEnabled)
            .putBoolean(keyAmpCacheStrip, policy.ampCacheStripEnabled)
            .putBoolean(keyTwitterToNitter, policy.twitterToNitterEnabled)
            .putBoolean(keyGoogleAdsTrackingStrip, policy.googleAdsTrackingStripEnabled)
            .putBoolean(keyMetaAdsTrackingStrip, policy.metaAdsTrackingStripEnabled)
            .putBoolean(keyMicrosoftAdsTrackingStrip, policy.microsoftAdsTrackingStripEnabled)
            .putBoolean(keyTiktokAdsTrackingStrip, policy.tiktokAdsTrackingStripEnabled)
            .putBoolean(keyTwitterAdsTrackingStrip, policy.twitterAdsTrackingStripEnabled)
            .putBoolean(keyLinkedInAdsTrackingStrip, policy.linkedInAdsTrackingStripEnabled)
            .putBoolean(keyPinterestAdsTrackingStrip, policy.pinterestAdsTrackingStripEnabled)
            .putBoolean(keySnapchatAdsTrackingStrip, policy.snapchatAdsTrackingStripEnabled)
            .putBoolean(keyAggressiveGoogleAdsStripping, policy.aggressiveGoogleAdsStrippingEnabled)
            .apply()
    }

    private companion object {
        private const val prefsName = "cleaner_settings"

        private const val keyGoogleShareRedirect = "google_share_redirect_enabled"
        private const val keyGoogleShareStrip = "google_share_strip_enabled"
        private const val keyRedditRedirect = "reddit_redirect_enabled"
        private const val keyRedditStrip = "reddit_strip_enabled"
        private const val keyAmazonRedirect = "amazon_redirect_enabled"
        private const val keyAmazonStrip = "amazon_strip_enabled"
        private const val keyAmazonRemoveAffiliateTag = "amazon_remove_affiliate_tag_enabled"
        private const val keyInstagramRedirect = "instagram_redirect_enabled"
        private const val keyInstagramStrip = "instagram_strip_enabled"
        private const val keyAmpCacheRedirect = "amp_cache_redirect_enabled"
        private const val keyAmpCacheStrip = "amp_cache_strip_enabled"
        private const val keyTwitterToNitter = "twitter_to_nitter_enabled"
        private const val keyGoogleAdsTrackingStrip = "google_ads_tracking_strip_enabled"
        private const val keyMetaAdsTrackingStrip = "meta_ads_tracking_strip_enabled"
        private const val keyMicrosoftAdsTrackingStrip = "microsoft_ads_tracking_strip_enabled"
        private const val keyTiktokAdsTrackingStrip = "tiktok_ads_tracking_strip_enabled"
        private const val keyTwitterAdsTrackingStrip = "twitter_ads_tracking_strip_enabled"
        private const val keyLinkedInAdsTrackingStrip = "linkedin_ads_tracking_strip_enabled"
        private const val keyPinterestAdsTrackingStrip = "pinterest_ads_tracking_strip_enabled"
        private const val keySnapchatAdsTrackingStrip = "snapchat_ads_tracking_strip_enabled"
        private const val keyAggressiveGoogleAdsStripping = "aggressive_google_ads_stripping_enabled"
    }
}
