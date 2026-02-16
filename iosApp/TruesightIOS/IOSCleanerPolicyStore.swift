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
            twitterToNitterEnabled: defaults.object(forKey: Keys.twitterToNitter) as? Bool ?? false
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
}
