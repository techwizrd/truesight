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
}
