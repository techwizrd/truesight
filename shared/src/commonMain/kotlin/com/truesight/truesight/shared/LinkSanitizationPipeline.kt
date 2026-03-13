package com.truesight.truesight.shared

internal object LinkSanitizationPipeline {
    fun run(
        inputUrl: String,
        policy: CleanerPolicy,
        resolveRedirect: (String) -> String
    ): String {
        if (inputUrl.isBlank()) {
            return inputUrl
        }

        val normalizedInput = normalize(inputUrl)
        val parsedInput = UrlPartsParser.parse(normalizedInput)
        val sourceHost = parsedInput?.host
        val shouldResolveRedirect = sourceHost?.let(policy::isRedirectEnabledForHost) ?: true

        val unwrapped = RedirectUnwrapper.unwrap(normalizedInput, parsedInput, policy)
        val followed = if (shouldResolveRedirect) resolveRedirect(unwrapped) else unwrapped
        val parsedFollowed = UrlPartsParser.parse(followed)
        val unwrappedFollowed = RedirectUnwrapper.unwrap(followed, parsedFollowed, policy)
        val stripped = UrlPartsParser.parse(unwrappedFollowed)?.let { parsedFinalUrl ->
            TrackingParamStripper.strip(parsedFinalUrl, policy)
        } ?: TrackingParamStripper.strip(unwrappedFollowed, policy)

        return if (policy.twitterToNitterEnabled) {
            TwitterToNitterRewriter.rewrite(stripped)
        } else {
            stripped
        }
    }

    private fun normalize(url: String): String {
        return url.trim().replace("&amp;", "&")
    }
}
