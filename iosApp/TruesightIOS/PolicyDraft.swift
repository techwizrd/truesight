import Foundation
import SharedCleaner

enum PolicyDraft {
    static func cleaner(
        from base: CleanerPolicy,
        googleShareRedirectEnabled: Bool? = nil,
        googleShareStripEnabled: Bool? = nil,
        redditRedirectEnabled: Bool? = nil,
        redditStripEnabled: Bool? = nil,
        amazonRedirectEnabled: Bool? = nil,
        amazonStripEnabled: Bool? = nil,
        amazonRemoveAffiliateTagEnabled: Bool? = nil,
        instagramRedirectEnabled: Bool? = nil,
        instagramStripEnabled: Bool? = nil,
        ampCacheRedirectEnabled: Bool? = nil,
        ampCacheStripEnabled: Bool? = nil,
        twitterToNitterEnabled: Bool? = nil,
        adTracking: AdTrackingPolicy? = nil,
        utmTrackingStripEnabled: Bool? = nil
    ) -> CleanerPolicy {
        CleanerPolicy(
            googleShareRedirectEnabled: googleShareRedirectEnabled ?? base.googleShareRedirectEnabled,
            googleShareStripEnabled: googleShareStripEnabled ?? base.googleShareStripEnabled,
            redditRedirectEnabled: redditRedirectEnabled ?? base.redditRedirectEnabled,
            redditStripEnabled: redditStripEnabled ?? base.redditStripEnabled,
            amazonRedirectEnabled: amazonRedirectEnabled ?? base.amazonRedirectEnabled,
            amazonStripEnabled: amazonStripEnabled ?? base.amazonStripEnabled,
            amazonRemoveAffiliateTagEnabled: amazonRemoveAffiliateTagEnabled ?? base.amazonRemoveAffiliateTagEnabled,
            instagramRedirectEnabled: instagramRedirectEnabled ?? base.instagramRedirectEnabled,
            instagramStripEnabled: instagramStripEnabled ?? base.instagramStripEnabled,
            ampCacheRedirectEnabled: ampCacheRedirectEnabled ?? base.ampCacheRedirectEnabled,
            ampCacheStripEnabled: ampCacheStripEnabled ?? base.ampCacheStripEnabled,
            twitterToNitterEnabled: twitterToNitterEnabled ?? base.twitterToNitterEnabled,
            adTracking: adTracking ?? base.adTracking,
            utmTrackingStripEnabled: utmTrackingStripEnabled ?? base.utmTrackingStripEnabled
        )
    }

    static func adTracking(
        from base: AdTrackingPolicy,
        googleEnabled: Bool? = nil,
        googleAggressiveEnabled: Bool? = nil,
        metaEnabled: Bool? = nil,
        microsoftEnabled: Bool? = nil,
        tiktokEnabled: Bool? = nil,
        twitterEnabled: Bool? = nil,
        linkedInEnabled: Bool? = nil,
        pinterestEnabled: Bool? = nil,
        snapchatEnabled: Bool? = nil
    ) -> AdTrackingPolicy {
        AdTrackingPolicy(
            googleEnabled: googleEnabled ?? base.googleEnabled,
            googleAggressiveEnabled: googleAggressiveEnabled ?? base.googleAggressiveEnabled,
            metaEnabled: metaEnabled ?? base.metaEnabled,
            microsoftEnabled: microsoftEnabled ?? base.microsoftEnabled,
            tiktokEnabled: tiktokEnabled ?? base.tiktokEnabled,
            twitterEnabled: twitterEnabled ?? base.twitterEnabled,
            linkedInEnabled: linkedInEnabled ?? base.linkedInEnabled,
            pinterestEnabled: pinterestEnabled ?? base.pinterestEnabled,
            snapchatEnabled: snapchatEnabled ?? base.snapchatEnabled
        )
    }
}
