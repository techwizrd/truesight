package com.truesight.truesight.shared

internal class LinkSanitizationPipeline(
    private val policy: CleanerPolicy,
    private val resolveRedirect: (String) -> String
) {
    // TODO: Reuse parsed URL parts through stages to avoid repeated parsing across pipeline and stripper.
    fun run(inputUrl: String): String {
        if (inputUrl.isBlank()) {
            return inputUrl
        }

        val normalizedInput = normalize(inputUrl)
        val sourceHost = UrlPartsParser.parse(normalizedInput)?.host
        val shouldResolveRedirect = sourceHost?.let(policy::isRedirectEnabledForHost) ?: true

        val unwrapped = RedirectUnwrapper.unwrap(normalizedInput, policy)
        val followed = if (shouldResolveRedirect) resolveRedirect(unwrapped) else unwrapped
        val unwrappedFollowed = RedirectUnwrapper.unwrap(followed, policy)
        val stripped = TrackingParamStripper.strip(unwrappedFollowed, policy)

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
