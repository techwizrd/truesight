package com.truesight.truesight.shared

import io.ktor.http.Url

internal data class UrlParts(
    val scheme: String,
    val authority: String,
    val host: String,
    val path: String,
    val rawQuery: String?,
    val rawFragment: String?
) {
    fun build(rawQueryOverride: String? = rawQuery): String {
        val builder = StringBuilder()
            .append(scheme)
            .append("://")
            .append(authority)
            .append(path)

        if (!rawQueryOverride.isNullOrBlank()) {
            builder.append('?').append(rawQueryOverride)
        }

        if (!rawFragment.isNullOrBlank()) {
            builder.append('#').append(rawFragment)
        }

        return builder.toString()
    }
}

internal object UrlPartsParser {
    fun parse(url: String): UrlParts? {
        val parsed = runCatching { Url(url) }.getOrNull() ?: return null
        val host = parsed.host.lowercase()
        if (host.isBlank()) {
            return null
        }

        val scheme = parsed.protocol.name
        val authority = extractAuthority(url)
            ?: return null
        val path = parsed.encodedPath.ifBlank { "/" }
        val rawQuery = parsed.encodedQuery.takeIf { it.isNotBlank() }
        val rawFragment = parsed.encodedFragment.takeIf { it.isNotBlank() }

        return UrlParts(
            scheme = scheme,
            authority = authority,
            host = host,
            path = path,
            rawQuery = rawQuery,
            rawFragment = rawFragment
        )
    }

    private fun extractAuthority(url: String): String? {
        val schemeSeparator = url.indexOf("://")
        if (schemeSeparator == -1) {
            return null
        }

        val authorityStart = schemeSeparator + 3
        val authorityEnd = url.indexOfAny(charArrayOf('/', '?', '#'), authorityStart)
            .let { index -> if (index == -1) url.length else index }

        if (authorityEnd <= authorityStart) {
            return null
        }

        return url.substring(authorityStart, authorityEnd)
    }
}

internal fun decodeUrlValue(value: String): String {
    val text = value.replace('+', ' ')
    val bytes = ArrayList<Byte>(text.length)
    var index = 0
    while (index < text.length) {
        val ch = text[index]
        if (ch == '%' && index + 2 < text.length) {
            val hex = text.substring(index + 1, index + 3)
            val decoded = hex.toIntOrNull(16)
            if (decoded != null) {
                bytes.add(decoded.toByte())
                index += 3
                continue
            }
        }
        val charBytes = ch.toString().encodeToByteArray()
        bytes.addAll(charBytes.toList())
        index += 1
    }
    return bytes.toByteArray().decodeToString()
}

internal fun parseQuery(rawQuery: String): Map<String, String> {
    val params = mutableMapOf<String, String>()
    var start = 0

    while (start <= rawQuery.length) {
        val separatorIndex = rawQuery.indexOf('&', start)
        val end = if (separatorIndex == -1) rawQuery.length else separatorIndex

        if (end > start) {
            val equalsIndex = rawQuery.indexOf('=', start).takeIf { it in start until end }
            val keyEnd = equalsIndex ?: end
            val key = rawQuery.substring(start, keyEnd).lowercase()
            if (key.isNotBlank()) {
                val value = if (equalsIndex == null) {
                    ""
                } else {
                    rawQuery.substring(equalsIndex + 1, end)
                }
                params[key] = value
            }
        }

        if (separatorIndex == -1) {
            break
        }
        start = separatorIndex + 1
    }

    return params
}
