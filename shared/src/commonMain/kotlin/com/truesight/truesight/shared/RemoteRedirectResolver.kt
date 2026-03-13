package com.truesight.truesight.shared

object RemoteRedirectResolver {
    enum class LogLevel { DEBUG, WARN }

    data class FollowResult(
        val resolvedUrl: String,
        val redirectCount: Int
    )

    fun interface RedirectNetworkClient {
        fun fetchRedirectLocation(url: String): String?
    }

    fun interface BodyFetcher {
        fun fetchBody(url: String): String?
    }

    fun followIfNeeded(
        url: String,
        policy: CleanerPolicy,
        networkClient: RedirectNetworkClient,
        bodyFetcher: BodyFetcher,
        logger: (level: LogLevel, message: String, error: Throwable?) -> Unit = { _, _, _ -> }
    ): String {
        return followWithResult(url, policy, networkClient, bodyFetcher, logger).resolvedUrl
    }

    fun followWithResult(
        url: String,
        policy: CleanerPolicy,
        networkClient: RedirectNetworkClient,
        bodyFetcher: BodyFetcher,
        logger: (level: LogLevel, message: String, error: Throwable?) -> Unit = { _, _, _ -> }
    ): FollowResult {
        if (!shouldFollow(url, policy)) {
            logger(LogLevel.DEBUG, "Skipping remote follow for url=$url", null)
            return FollowResult(resolvedUrl = url, redirectCount = 0)
        }

        logger(LogLevel.DEBUG, "Starting remote follow for url=$url", null)
        val chainResult = followRedirectChain(url, networkClient, logger)
        val followed = chainResult.resolvedUrl
        logger(LogLevel.DEBUG, "Redirect chain resolved to=$followed", null)

        val redditDestination = resolveRedditDestination(followed, bodyFetcher, logger)
        val finalUrl = redditDestination ?: followed
        val redditRedirectCount = if (redditDestination != null && redditDestination != followed) 1 else 0
        val redirectCount = chainResult.redirectCount + redditRedirectCount
        logger(LogLevel.DEBUG, "Final resolved url=$finalUrl", null)
        return FollowResult(resolvedUrl = finalUrl, redirectCount = redirectCount)
    }

    private data class RedirectChainResult(
        val resolvedUrl: String,
        val redirectCount: Int
    )

    private fun shouldFollow(url: String, policy: CleanerPolicy): Boolean {
        val host = UrlPartsParser.parse(url)?.host ?: return false
        if (!policy.isRedirectEnabledForHost(host)) {
            return false
        }

        return DomainMatchers.isGoogleShareHost(host) ||
            DomainMatchers.isRedditHost(host) ||
            host == "amzn.to" ||
            host == "a.co"
    }

    private fun followRedirectChain(
        url: String,
        networkClient: RedirectNetworkClient,
        logger: (level: LogLevel, message: String, error: Throwable?) -> Unit
    ): RedirectChainResult {
        var current = url
        var redirectCount = 0
        repeat(5) { hop ->
            val location = networkClient.fetchRedirectLocation(current)
                ?: return RedirectChainResult(resolvedUrl = current, redirectCount = redirectCount)
            val next = resolveLocation(current, location)
            if (next == current) {
                logger(LogLevel.DEBUG, "Hop ${hop + 1}: redirect target same as current, stopping at=$current", null)
                return RedirectChainResult(resolvedUrl = current, redirectCount = redirectCount)
            }
            logger(LogLevel.DEBUG, "Hop ${hop + 1}: $current -> $next", null)
            current = next
            redirectCount += 1
        }
        logger(LogLevel.DEBUG, "Reached max redirect hops at=$current", null)
        return RedirectChainResult(resolvedUrl = current, redirectCount = redirectCount)
    }

    private fun resolveLocation(baseUrl: String, location: String): String {
        val absolute = UrlPartsParser.parse(location)
        if (absolute != null) {
            return location
        }

        val base = UrlPartsParser.parse(baseUrl) ?: return baseUrl
        return when {
            location.startsWith("//") -> "${base.scheme}:$location"
            location.startsWith("#") -> {
                base.copy(rawFragment = location.removePrefix("#")).build()
            }
            location.startsWith("?") -> {
                base.copy(
                    rawQuery = location.removePrefix("?"),
                    rawFragment = null
                ).build()
            }
            else -> resolveRelativeLocation(base, location)
        }
    }

    private fun resolveRelativeLocation(base: UrlParts, location: String): String {
        val fragment = location.substringAfter('#', "").takeIf { '#' in location }
        val withoutFragment = location.substringBefore('#')

        val hasQuery = '?' in withoutFragment
        val query = withoutFragment.substringAfter('?', "").takeIf { hasQuery }
        val relativePath = withoutFragment.substringBefore('?')

        val mergedPath = if (relativePath.startsWith('/')) {
            normalizePath(relativePath)
        } else {
            val baseDirectory = base.path.substringBeforeLast('/', "")
            val prefix = if (baseDirectory.isBlank()) "/" else "$baseDirectory/"
            normalizePath(prefix + relativePath)
        }

        return base.copy(
            path = mergedPath,
            rawQuery = query,
            rawFragment = fragment
        ).build()
    }

    private fun normalizePath(path: String): String {
        val trailingSlash = path.endsWith('/')
        val segments = mutableListOf<String>()

        path.split('/').forEach { segment ->
            when (segment) {
                "", "." -> Unit
                ".." -> if (segments.isNotEmpty()) segments.removeAt(segments.lastIndex)
                else -> segments += segment
            }
        }

        val normalized = "/${segments.joinToString("/")}"
        return if (trailingSlash && normalized != "/") "$normalized/" else normalized
    }

    private fun resolveRedditDestination(
        url: String,
        bodyFetcher: BodyFetcher,
        logger: (level: LogLevel, message: String, error: Throwable?) -> Unit
    ): String? {
        val parts = UrlPartsParser.parse(url) ?: return null
        if (!DomainMatchers.isRedditHost(parts.host)) {
            return null
        }

        if (!parts.path.contains("/comments/")) {
            logger(LogLevel.DEBUG, "Reddit url has no comments path, skipping json resolve: $url", null)
            return null
        }

        val jsonUrl = buildRedditJsonUrl(parts)
        logger(LogLevel.DEBUG, "Resolving reddit destination via json=$jsonUrl", null)
        val body = bodyFetcher.fetchBody(jsonUrl) ?: return null

        val postData = RedditPostDataParser.parseFirstPostData(body)
        if (postData == null) {
            logger(LogLevel.DEBUG, "Unable to locate reddit post data object", null)
            return null
        }

        if (postData.isSelf || postData.postHint == "self") {
            logger(LogLevel.DEBUG, "Reddit self post detected, keeping permalink", null)
            return null
        }

        val destination = postData.destinationUrl

        if (destination != null && (destination.startsWith("http://") || destination.startsWith("https://"))) {
            if (!isExternalRedditDestination(destination)) {
                logger(LogLevel.DEBUG, "Reddit destination is internal, keeping permalink", null)
                return null
            }
            logger(LogLevel.DEBUG, "Resolved reddit destination=$destination", null)
            return destination
        }

        logger(LogLevel.DEBUG, "Reddit destination missing or invalid", null)
        return null
    }

    private fun isExternalRedditDestination(destination: String): Boolean {
        val parts = UrlPartsParser.parse(destination) ?: return false
        val host = parts.host

        if (DomainMatchers.isRedditHost(host)) {
            return false
        }

        if (host == "redditmedia.com" || host.endsWith(".redditmedia.com")) {
            return false
        }

        if (host == "preview.redd.it" || host == "i.redd.it" || host == "v.redd.it") {
            return false
        }

        return true
    }

    private fun buildRedditJsonUrl(parts: UrlParts): String {
        val pathWithJson = if (parts.path.endsWith(".json")) {
            parts.path
        } else {
            "${parts.path.trimEnd('/')}.json"
        }

        val query = if (parts.rawQuery.isNullOrBlank()) {
            "raw_json=1"
        } else {
            "${parts.rawQuery}&raw_json=1"
        }

        return UrlParts(
            scheme = parts.scheme,
            authority = parts.authority,
            host = parts.host,
            path = pathWithJson,
            rawQuery = query,
            rawFragment = null
        ).build()
    }
}
