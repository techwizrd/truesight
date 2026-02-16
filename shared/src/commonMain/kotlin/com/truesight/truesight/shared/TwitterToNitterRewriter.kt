package com.truesight.truesight.shared

object TwitterToNitterRewriter {
    private const val nitterHost = "nitter.net"

    fun rewrite(url: String): String {
        val parts = UrlPartsParser.parse(url) ?: return url
        if (!isTwitterHost(parts.host)) {
            return url
        }

        val newAuthority = rebuildAuthorityWithNewHost(parts.authority, nitterHost)
        return UrlParts(
            scheme = parts.scheme,
            authority = newAuthority,
            host = nitterHost,
            path = parts.path,
            rawQuery = parts.rawQuery,
            rawFragment = parts.rawFragment
        ).build()
    }

    private fun isTwitterHost(host: String): Boolean {
        return host == "twitter.com" ||
            host == "www.twitter.com" ||
            host == "mobile.twitter.com" ||
            host == "x.com" ||
            host == "www.x.com" ||
            host == "mobile.x.com"
    }

    private fun rebuildAuthorityWithNewHost(authority: String, newHost: String): String {
        val userInfo = authority.substringBefore('@', "")
        val hostAndPort = authority.substringAfter('@', authority)
        val port = if (hostAndPort.contains(':')) hostAndPort.substringAfter(':', "") else ""
        val userInfoPrefix = if (userInfo.isNotBlank()) "$userInfo@" else ""
        val portSuffix = if (port.isNotBlank()) ":$port" else ""
        return "$userInfoPrefix$newHost$portSuffix"
    }
}
