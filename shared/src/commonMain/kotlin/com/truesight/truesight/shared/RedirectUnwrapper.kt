package com.truesight.truesight.shared

object RedirectUnwrapper {
    private const val maxDepth = 5

    private val commonWrappedParamKeys = listOf(
        "url",
        "u",
        "q",
        "link",
        "target",
        "dest",
        "destination",
        "redirect"
    )

    private val redirectRules: List<RedirectRule> = listOf(
        AmpRule,
        ParamRule(
            matches = { host, path ->
                (host.startsWith("www.google.") || host.startsWith("google.")) && path == "/url"
            },
            keys = listOf("q", "url", "u")
        ),
        ParamRule(
            matches = { host, _ -> DomainMatchers.isGoogleShareHost(host) },
            keys = listOf("q", "url", "u", "link")
        ),
        ParamRule(
            matches = { host, path ->
                (host == "l.facebook.com" || host == "lm.facebook.com") && path.startsWith("/l.php")
            },
            keys = listOf("u", "url")
        ),
        ParamRule(
            matches = { host, _ -> host == "out.reddit.com" },
            keys = listOf("url", "u")
        ),
        ParamRule(
            matches = { host, path -> DomainMatchers.isAmazonHost(host) && path.contains("slredirect") },
            keys = listOf("url", "u", "redirecturl")
        ),
        ParamRule(
            matches = { _, path ->
                path.startsWith("/url") || path.startsWith("/redirect") || path.startsWith("/out")
            },
            keys = commonWrappedParamKeys
        )
    )

    fun unwrap(url: String): String {
        return unwrap(url, CleanerPolicy())
    }

    fun unwrap(url: String, policy: CleanerPolicy): String {
        var current = url
        repeat(maxDepth) {
            val parts = UrlPartsParser.parse(current) ?: return current
            if (!policy.isRedirectEnabledForHost(parts.host)) {
                return current
            }

            val next = redirectRules.firstNotNullOfOrNull { rule -> rule.tryUnwrap(parts) } ?: return current
            if (next == current) {
                return current
            }
            current = next
        }
        return current
    }

    private interface RedirectRule {
        fun tryUnwrap(parts: UrlParts): String?
    }

    private class ParamRule(
        private val matches: (host: String, path: String) -> Boolean,
        private val keys: List<String>
    ) : RedirectRule {
        override fun tryUnwrap(parts: UrlParts): String? {
            if (!matches(parts.host, parts.path)) {
                return null
            }
            return unwrapFromParams(parts, keys)
        }
    }

    private object AmpRule : RedirectRule {
        override fun tryUnwrap(parts: UrlParts): String? {
            val host = parts.host
            val path = parts.path

            if ((host.startsWith("www.google.") || host.startsWith("google.")) && path.startsWith("/amp/s/")) {
                val target = decodeAmpTarget(path.removePrefix("/amp/s/"))
                if (target.isNotBlank()) {
                    return "https://$target"
                }
            }

            if (host.endsWith(".cdn.ampproject.org")) {
                return when {
                    path.startsWith("/c/s/") -> asSchemeUrl("https", path.removePrefix("/c/s/"))
                    path.startsWith("/c/") -> asSchemeUrl("http", path.removePrefix("/c/"))
                    path.startsWith("/v/s/") -> asSchemeUrl("https", path.removePrefix("/v/s/"))
                    path.startsWith("/v/") -> asSchemeUrl("http", path.removePrefix("/v/"))
                    path.startsWith("/a/s/") -> asSchemeUrl("https", path.removePrefix("/a/s/"))
                    path.startsWith("/a/") -> asSchemeUrl("http", path.removePrefix("/a/"))
                    else -> null
                }
            }

            return null
        }

        private fun asSchemeUrl(scheme: String, encodedTarget: String): String? {
            val target = decodeAmpTarget(encodedTarget)
            return if (target.isBlank()) null else "$scheme://$target"
        }
    }

    private fun unwrapFromParams(parts: UrlParts, keys: List<String>): String? {
        val query = parts.rawQuery ?: return null
        val params = parseQuery(query)

        for (key in keys) {
            val value = params[key] ?: continue
            val decoded = decodeUrlValue(value)
            if (decoded.startsWith("http://") || decoded.startsWith("https://")) {
                return decoded
            }
        }
        return null
    }

    private fun decodeAmpTarget(target: String): String {
        return decodeUrlValue(target)
    }
}
