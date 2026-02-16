package com.truesight.truesight

import com.truesight.truesight.shared.UrlCleanerCore
import org.junit.Assert.assertEquals
import org.junit.Test

class UrlCleanerTest {
    @Test
    fun cleanMatchesSharedCoreForDefaultPolicy() {
        val dirty = "https://example.com/path?utm_source=x&fbclid=y&id=99"

        val sharedResult = UrlCleanerCore.clean(dirty)
        val appResult = UrlCleaner.clean(dirty)

        assertEquals(sharedResult, appResult)
    }

    @Test
    fun cleanWithPolicyAndResolverUsesSharedPipeline() {
        val policy = CleanerPolicy(twitterToNitterEnabled = true)
        val dirty = "https://share.google.com/some/link"

        val appResult = UrlCleaner.clean(dirty, policy) {
            "https://twitter.com/someuser/status/123?utm_source=share&id=9"
        }

        assertEquals("https://nitter.net/someuser/status/123?id=9", appResult)
    }

    @Test
    fun cleanWithPolicyMatchesSharedCore() {
        val policy = CleanerPolicy(twitterToNitterEnabled = true)
        val dirty = "https://x.com/someuser/status/123?utm_source=share&id=9"

        val sharedResult = UrlCleanerCore.clean(dirty, policy)
        val appResult = UrlCleaner.clean(dirty, policy)

        assertEquals(sharedResult, appResult)
    }
}
