package com.truesight.truesight

import android.content.Context
import com.truesight.truesight.shared.CleanerPolicyStore
import java.util.concurrent.atomic.AtomicLong

class CleanerSettingsStore(context: Context) : CleanerPolicyStore, PolicyVersionProvider {
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val policyVersion = AtomicLong(prefs.getLong(keyPolicyVersion, 0L))

    override fun currentVersion(): Long {
        return policyVersion.get()
    }

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
            adTracking = AdTrackingPolicy(
                googleEnabled = prefs.getBoolean(keyGoogleAdsTrackingStrip, true),
                metaEnabled = prefs.getBoolean(keyMetaAdsTrackingStrip, true),
                microsoftEnabled = prefs.getBoolean(keyMicrosoftAdsTrackingStrip, true),
                tiktokEnabled = prefs.getBoolean(keyTiktokAdsTrackingStrip, true),
                twitterEnabled = prefs.getBoolean(keyTwitterAdsTrackingStrip, true),
                linkedInEnabled = prefs.getBoolean(keyLinkedInAdsTrackingStrip, true),
                pinterestEnabled = prefs.getBoolean(keyPinterestAdsTrackingStrip, true),
                snapchatEnabled = prefs.getBoolean(keySnapchatAdsTrackingStrip, true),
                googleAggressiveEnabled = prefs.getBoolean(keyAggressiveGoogleAdsStripping, false)
            ),
            utmTrackingStripEnabled = prefs.getBoolean(keyUtmTrackingStrip, true)
        )
    }

    override fun savePolicy(policy: CleanerPolicy) {
        val nextVersion = policyVersion.incrementAndGet()
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
            .putBoolean(keyGoogleAdsTrackingStrip, policy.adTracking.googleEnabled)
            .putBoolean(keyMetaAdsTrackingStrip, policy.adTracking.metaEnabled)
            .putBoolean(keyMicrosoftAdsTrackingStrip, policy.adTracking.microsoftEnabled)
            .putBoolean(keyTiktokAdsTrackingStrip, policy.adTracking.tiktokEnabled)
            .putBoolean(keyTwitterAdsTrackingStrip, policy.adTracking.twitterEnabled)
            .putBoolean(keyLinkedInAdsTrackingStrip, policy.adTracking.linkedInEnabled)
            .putBoolean(keyPinterestAdsTrackingStrip, policy.adTracking.pinterestEnabled)
            .putBoolean(keySnapchatAdsTrackingStrip, policy.adTracking.snapchatEnabled)
            .putBoolean(keyAggressiveGoogleAdsStripping, policy.adTracking.googleAggressiveEnabled)
            .putBoolean(keyUtmTrackingStrip, policy.utmTrackingStripEnabled)
            .putLong(keyPolicyVersion, nextVersion)
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
        private const val keyUtmTrackingStrip = "utm_tracking_strip_enabled"
        private const val keyPolicyVersion = "policy_version"
    }
}
