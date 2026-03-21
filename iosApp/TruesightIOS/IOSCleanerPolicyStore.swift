import Foundation
import SharedCleaner

final class IOSCleanerPolicyStore: CleanerPolicyStore {
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func loadPolicy() -> CleanerPolicy {
        CleanerPolicy(
            googleShareRedirectEnabled: defaults.object(forKey: Keys.googleShareRedirect) as? Bool ?? true,
            googleShareStripEnabled: defaults.object(forKey: Keys.googleShareStrip) as? Bool ?? true,
            redditRedirectEnabled: defaults.object(forKey: Keys.redditRedirect) as? Bool ?? true,
            redditStripEnabled: defaults.object(forKey: Keys.redditStrip) as? Bool ?? true,
            amazonRedirectEnabled: defaults.object(forKey: Keys.amazonRedirect) as? Bool ?? true,
            amazonStripEnabled: defaults.object(forKey: Keys.amazonStrip) as? Bool ?? true,
            amazonRemoveAffiliateTagEnabled: defaults.object(forKey: Keys.amazonRemoveAffiliateTag) as? Bool ?? true,
            instagramRedirectEnabled: defaults.object(forKey: Keys.instagramRedirect) as? Bool ?? true,
            instagramStripEnabled: defaults.object(forKey: Keys.instagramStrip) as? Bool ?? true,
            ampCacheRedirectEnabled: defaults.object(forKey: Keys.ampCacheRedirect) as? Bool ?? true,
            ampCacheStripEnabled: defaults.object(forKey: Keys.ampCacheStrip) as? Bool ?? true,
            twitterToNitterEnabled: defaults.object(forKey: Keys.twitterToNitter) as? Bool ?? false,
            adTracking: AdTrackingPolicy(
                googleEnabled: defaults.object(forKey: Keys.googleAdsTrackingStrip) as? Bool ?? true,
                googleAggressiveEnabled: defaults.object(forKey: Keys.aggressiveGoogleAdsStripping) as? Bool ?? false,
                metaEnabled: defaults.object(forKey: Keys.metaAdsTrackingStrip) as? Bool ?? true,
                microsoftEnabled: defaults.object(forKey: Keys.microsoftAdsTrackingStrip) as? Bool ?? true,
                tiktokEnabled: defaults.object(forKey: Keys.tiktokAdsTrackingStrip) as? Bool ?? true,
                twitterEnabled: defaults.object(forKey: Keys.twitterAdsTrackingStrip) as? Bool ?? true,
                linkedInEnabled: defaults.object(forKey: Keys.linkedInAdsTrackingStrip) as? Bool ?? true,
                pinterestEnabled: defaults.object(forKey: Keys.pinterestAdsTrackingStrip) as? Bool ?? true,
                snapchatEnabled: defaults.object(forKey: Keys.snapchatAdsTrackingStrip) as? Bool ?? true
            ),
            utmTrackingStripEnabled: defaults.object(forKey: Keys.utmTrackingStrip) as? Bool ?? true
        )
    }

    func savePolicy(policy: CleanerPolicy) {
        defaults.set(policy.googleShareRedirectEnabled, forKey: Keys.googleShareRedirect)
        defaults.set(policy.googleShareStripEnabled, forKey: Keys.googleShareStrip)
        defaults.set(policy.redditRedirectEnabled, forKey: Keys.redditRedirect)
        defaults.set(policy.redditStripEnabled, forKey: Keys.redditStrip)
        defaults.set(policy.amazonRedirectEnabled, forKey: Keys.amazonRedirect)
        defaults.set(policy.amazonStripEnabled, forKey: Keys.amazonStrip)
        defaults.set(policy.amazonRemoveAffiliateTagEnabled, forKey: Keys.amazonRemoveAffiliateTag)
        defaults.set(policy.instagramRedirectEnabled, forKey: Keys.instagramRedirect)
        defaults.set(policy.instagramStripEnabled, forKey: Keys.instagramStrip)
        defaults.set(policy.ampCacheRedirectEnabled, forKey: Keys.ampCacheRedirect)
        defaults.set(policy.ampCacheStripEnabled, forKey: Keys.ampCacheStrip)
        defaults.set(policy.twitterToNitterEnabled, forKey: Keys.twitterToNitter)
        defaults.set(policy.adTracking.googleEnabled, forKey: Keys.googleAdsTrackingStrip)
        defaults.set(policy.adTracking.googleAggressiveEnabled, forKey: Keys.aggressiveGoogleAdsStripping)
        defaults.set(policy.adTracking.metaEnabled, forKey: Keys.metaAdsTrackingStrip)
        defaults.set(policy.adTracking.microsoftEnabled, forKey: Keys.microsoftAdsTrackingStrip)
        defaults.set(policy.adTracking.tiktokEnabled, forKey: Keys.tiktokAdsTrackingStrip)
        defaults.set(policy.adTracking.twitterEnabled, forKey: Keys.twitterAdsTrackingStrip)
        defaults.set(policy.adTracking.linkedInEnabled, forKey: Keys.linkedInAdsTrackingStrip)
        defaults.set(policy.adTracking.pinterestEnabled, forKey: Keys.pinterestAdsTrackingStrip)
        defaults.set(policy.adTracking.snapchatEnabled, forKey: Keys.snapchatAdsTrackingStrip)
        defaults.set(policy.utmTrackingStripEnabled, forKey: Keys.utmTrackingStrip)
    }
}

private enum Keys {
    static let googleShareRedirect = "google_share_redirect_enabled"
    static let googleShareStrip = "google_share_strip_enabled"
    static let redditRedirect = "reddit_redirect_enabled"
    static let redditStrip = "reddit_strip_enabled"
    static let amazonRedirect = "amazon_redirect_enabled"
    static let amazonStrip = "amazon_strip_enabled"
    static let amazonRemoveAffiliateTag = "amazon_remove_affiliate_tag_enabled"
    static let instagramRedirect = "instagram_redirect_enabled"
    static let instagramStrip = "instagram_strip_enabled"
    static let ampCacheRedirect = "amp_cache_redirect_enabled"
    static let ampCacheStrip = "amp_cache_strip_enabled"
    static let twitterToNitter = "twitter_to_nitter_enabled"
    static let googleAdsTrackingStrip = "google_ads_tracking_strip_enabled"
    static let metaAdsTrackingStrip = "meta_ads_tracking_strip_enabled"
    static let microsoftAdsTrackingStrip = "microsoft_ads_tracking_strip_enabled"
    static let tiktokAdsTrackingStrip = "tiktok_ads_tracking_strip_enabled"
    static let twitterAdsTrackingStrip = "twitter_ads_tracking_strip_enabled"
    static let linkedInAdsTrackingStrip = "linkedin_ads_tracking_strip_enabled"
    static let pinterestAdsTrackingStrip = "pinterest_ads_tracking_strip_enabled"
    static let snapchatAdsTrackingStrip = "snapchat_ads_tracking_strip_enabled"
    static let aggressiveGoogleAdsStripping = "aggressive_google_ads_stripping_enabled"
    static let utmTrackingStrip = "utm_tracking_strip_enabled"
}
