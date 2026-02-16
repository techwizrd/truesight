package com.truesight.truesight.shared

object UrlExtractor {
    private val urlPattern = Regex("""https?://[^\s<>"]+""", RegexOption.IGNORE_CASE)
    private val trailingNoise = setOf('.', ',', ';', ':', '!', '?', ')', ']', '}')

    fun extractFirstUrl(text: String): String? {
        val match = urlPattern.find(text) ?: return null
        return sanitize(match.value)
    }

    private fun sanitize(raw: String): String {
        var value = raw.trim()
        if (value.startsWith("<") && value.endsWith(">") && value.length > 2) {
            value = value.substring(1, value.length - 1)
        }
        while (value.isNotEmpty() && trailingNoise.contains(value.last())) {
            value = value.dropLast(1)
        }
        return value
    }
}
