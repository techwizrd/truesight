package com.truesight.truesight.shared

class SharedRedirectResolver {
    fun followWithResult(
        url: String,
        policy: CleanerPolicy,
        locationFetcher: RedirectLocationFetcher,
        bodyFetcher: RedirectBodyFetcher
    ): RedirectFollowResult {
        val resolved = RemoteRedirectResolver.followWithResult(
            url = url,
            policy = policy,
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { requestUrl ->
                locationFetcher.fetchRedirectLocation(requestUrl)
            },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { requestUrl ->
                bodyFetcher.fetchBody(requestUrl)
            }
        )

        return RedirectFollowResult(
            resolvedUrl = resolved.resolvedUrl,
            redirectCount = resolved.redirectCount
        )
    }
}
