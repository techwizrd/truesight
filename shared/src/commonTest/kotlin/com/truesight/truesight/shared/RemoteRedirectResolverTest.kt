package com.truesight.truesight.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteRedirectResolverTest {
    @Test
    fun followsShortLinkRedirectChain() {
        val redirects = mapOf(
            "https://amzn.to/abc123" to "https://a.co/xyz456",
            "https://a.co/xyz456" to "https://www.amazon.com/dp/B000123456?tag=mytag-20"
        )

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = "https://amzn.to/abc123",
            policy = CleanerPolicy(),
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { redirects[it] },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { null }
        )

        assertEquals("https://www.amazon.com/dp/B000123456?tag=mytag-20", resolved)
    }

    @Test
    fun resolvesRedditPostDestinationFromJsonBody() {
        val jsonBody = """
            [
              {
                "data": {
                  "children": [
                    {
                      "data": {
                        "url_overridden_by_dest": "https:\/\/example.com\/article?utm_source=reddit&id=42"
                      }
                    }
                  ]
                }
              }
            ]
        """.trimIndent()

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = "https://www.reddit.com/r/test/comments/abc123/some-post/",
            policy = CleanerPolicy(),
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { null },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { jsonBody }
        )

        assertEquals("https://example.com/article?utm_source=reddit&id=42", resolved)
    }

    @Test
    fun keepsRedditPermalinkForSelfTextPosts() {
        val permalink = "https://www.reddit.com/r/test/comments/abc123/some-post/"
        val jsonBody = """
            [
              {
                "data": {
                  "children": [
                    {
                      "kind": "t3",
                      "data": {
                        "is_self": true,
                        "post_hint": "self",
                        "url": "https:\/\/www.reddit.com\/r\/test\/comments\/abc123\/some-post\/",
                        "url_overridden_by_dest": "https:\/\/preview.redd.it\/thumbnail.jpg"
                      }
                    }
                  ]
                }
              }
            ]
        """.trimIndent()

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = permalink,
            policy = CleanerPolicy(),
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { null },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { jsonBody }
        )

        assertEquals(permalink, resolved)
    }

    @Test
    fun ignoresThumbnailUrlsOutsidePostDataWhenResolvingRedditLinkPosts() {
        val jsonBody = """
            [
              {
                "data": {
                  "preview": {
                    "images": [
                      {"source": {"url": "https:\/\/preview.redd.it\/thumb.jpg"}}
                    ]
                  },
                  "children": [
                    {
                      "kind": "t3",
                      "data": {
                        "is_self": false,
                        "url_overridden_by_dest": "https:\/\/example.com\/story",
                        "url": "https:\/\/example.com\/story"
                      }
                    }
                  ]
                }
              }
            ]
        """.trimIndent()

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = "https://www.reddit.com/r/test/comments/abc123/some-post/",
            policy = CleanerPolicy(),
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { null },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { jsonBody }
        )

        assertEquals("https://example.com/story", resolved)
    }

    @Test
    fun followsRedditShortShareToCommentsPermalinkWhenPostDestinationIsInternalGallery() {
        val shareUrl = "https://www.reddit.com/r/test/s/ae0Q4m65Hh"
        val permalink = "https://www.reddit.com/r/test/comments/1r6eevy/example-post/"
        val redirects = mapOf(shareUrl to permalink)

        val jsonBody = """
            [
              {
                "data": {
                  "children": [
                    {
                      "kind": "t3",
                      "data": {
                        "is_self": false,
                        "post_hint": "link",
                        "url_overridden_by_dest": "https:\/\/www.reddit.com\/gallery\/1r6eevy"
                      }
                    }
                  ]
                }
              }
            ]
        """.trimIndent()

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = shareUrl,
            policy = CleanerPolicy(),
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { redirects[it] },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { jsonBody }
        )

        assertEquals(permalink, resolved)
    }

    @Test
    fun resolvesRelativeRedirectLocationWithDotSegments() {
        val redirects = mapOf(
            "https://amzn.to/r/start" to "../landing/page?utm_source=ad&id=9"
        )

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = "https://amzn.to/r/start",
            policy = CleanerPolicy(),
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { redirects[it] },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { null }
        )

        assertEquals("https://amzn.to/landing/page?utm_source=ad&id=9", resolved)
    }

    @Test
    fun resolvesQueryOnlyRedirectLocationAgainstBasePath() {
        val redirects = mapOf(
            "https://amzn.to/r/start?old=1" to "?id=9"
        )

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = "https://amzn.to/r/start?old=1",
            policy = CleanerPolicy(),
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { redirects[it] },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { null }
        )

        assertEquals("https://amzn.to/r/start?id=9", resolved)
    }

    @Test
    fun resolvesFragmentOnlyRedirectLocationAgainstBasePathAndQuery() {
        val redirects = mapOf(
            "https://amzn.to/r/start?id=9" to "#details"
        )

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = "https://amzn.to/r/start?id=9",
            policy = CleanerPolicy(),
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { redirects[it] },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { null }
        )

        assertEquals("https://amzn.to/r/start?id=9#details", resolved)
    }

    @Test
    fun keepsPermalinkWhenRedditDestinationIsInternalMediaHost() {
        val permalink = "https://www.reddit.com/r/test/comments/abc123/some-post/"
        val jsonBody = """
            [
              {
                "data": {
                  "children": [
                    {
                      "kind": "t3",
                      "data": {
                        "is_self": false,
                        "url_overridden_by_dest": "https:\/\/preview.redd.it\/thumb.jpg"
                      }
                    }
                  ]
                }
              }
            ]
        """.trimIndent()

        val resolved = RemoteRedirectResolver.followIfNeeded(
            url = permalink,
            policy = CleanerPolicy(),
            networkClient = RemoteRedirectResolver.RedirectNetworkClient { null },
            bodyFetcher = RemoteRedirectResolver.BodyFetcher { jsonBody }
        )

        assertEquals(permalink, resolved)
    }
}
