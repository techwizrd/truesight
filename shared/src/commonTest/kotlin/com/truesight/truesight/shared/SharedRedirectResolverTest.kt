package com.truesight.truesight.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedRedirectResolverTest {
    @Test
    fun followWithResultReturnsRedirectCountFromSharedResolver() {
        val resolver = SharedRedirectResolver()

        val result = resolver.followWithResult(
            url = "https://amzn.to/demo",
            policy = CleanerPolicy(),
            locationFetcher = RedirectLocationFetcher { requestUrl ->
                if (requestUrl == "https://amzn.to/demo") {
                    "https://example.com/article?utm_source=share&id=42"
                } else {
                    null
                }
            },
            bodyFetcher = RedirectBodyFetcher { null }
        )

        assertEquals("https://example.com/article?utm_source=share&id=42", result.resolvedUrl)
        assertEquals(1, result.redirectCount)
    }
}
