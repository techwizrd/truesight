package com.truesight.truesight.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SharedCleanerServiceTest {
    @Test
    fun usesPolicyStoreAndRedirectFollower() {
        val store = object : CleanerPolicyStore {
            override fun loadPolicy(): CleanerPolicy = CleanerPolicy(twitterToNitterEnabled = true)
            override fun savePolicy(policy: CleanerPolicy) = Unit
        }

        val follower = RedirectFollower { _, _ ->
            "https://x.com/someuser/status/123?utm_source=share&id=9"
        }

        val service = SharedCleanerService(store, follower)
        val cleaned = service.cleanUrl("https://amzn.to/abc")

        assertEquals("https://nitter.net/someuser/status/123?id=9", cleaned)
    }

    @Test
    fun returnsNullWhenNoUrlInText() {
        val store = object : CleanerPolicyStore {
            override fun loadPolicy(): CleanerPolicy = CleanerPolicy()
            override fun savePolicy(policy: CleanerPolicy) = Unit
        }
        val follower = RedirectFollower { url, _ -> url }
        val service = SharedCleanerService(store, follower)

        assertNull(service.cleanFirstUrlFromText("no links here"))
    }

    @Test
    fun cleanFirstUrlFromTextWithResultReturnsOriginalAndCleanedValues() {
        val store = object : CleanerPolicyStore {
            override fun loadPolicy(): CleanerPolicy = CleanerPolicy()
            override fun savePolicy(policy: CleanerPolicy) = Unit
        }
        val follower = object : RedirectFollower, RedirectFollowerWithStats {
            override fun follow(url: String, policy: CleanerPolicy): String {
                return followWithResult(url, policy).resolvedUrl
            }

            override fun followWithResult(url: String, policy: CleanerPolicy): RedirectFollowResult {
                return RedirectFollowResult(
                    resolvedUrl = "https://example.com/article?utm_source=share&id=42",
                    redirectCount = 2
                )
            }
        }
        val service = SharedCleanerService(store, follower)

        val result = service.cleanFirstUrlFromTextWithResult(
            "open https://amzn.to/demo?utm_source=share&id=42 now"
        )

        requireNotNull(result)
        assertEquals("https://amzn.to/demo?utm_source=share&id=42", result.originalUrl)
        assertEquals("https://example.com/article?id=42", result.cleanedUrl)
        assertEquals(1, result.paramsRemoved)
        assertEquals(2, result.redirectsFollowed)
    }

    @Test
    fun cleanFirstUrlFromTextWithResultReturnsNullForNoUrl() {
        val store = object : CleanerPolicyStore {
            override fun loadPolicy(): CleanerPolicy = CleanerPolicy()
            override fun savePolicy(policy: CleanerPolicy) = Unit
        }
        val follower = RedirectFollower { url, _ -> url }
        val service = SharedCleanerService(store, follower)

        assertNull(service.cleanFirstUrlFromTextWithResult("still no links here"))
    }
}
