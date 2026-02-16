package com.truesight.truesight

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlExtractorTest {
    @Test
    fun extractsFirstUrlFromSentence() {
        val text = "Check this https://example.com/path?x=1 and this https://second.com"
        assertEquals("https://example.com/path?x=1", UrlExtractor.extractFirstUrl(text))
    }

    @Test
    fun trimsTrailingPunctuation() {
        val text = "Open this link: https://example.com/test?x=1)."
        assertEquals("https://example.com/test?x=1", UrlExtractor.extractFirstUrl(text))
    }

    @Test
    fun returnsNullWhenNoUrlExists() {
        assertNull(UrlExtractor.extractFirstUrl("No links here"))
    }
}
