package com.truesight.truesight.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RedditPostDataParserTest {
    @Test
    fun parsesFirstChildPostData() {
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
                        "url_overridden_by_dest": "https:\/\/example.com\/article?id=42"
                      }
                    }
                  ]
                }
              }
            ]
        """.trimIndent()

        val postData = RedditPostDataParser.parseFirstPostData(jsonBody)

        assertNotNull(postData)
        assertEquals(false, postData.isSelf)
        assertEquals("link", postData.postHint)
        assertEquals("https://example.com/article?id=42", postData.destinationUrl)
    }

    @Test
    fun fallsBackToUrlWhenOverrideIsMissing() {
        val jsonBody = """
            [
              {
                "data": {
                  "children": [
                    {
                      "kind": "t3",
                      "data": {
                        "is_self": false,
                        "url": "https:\/\/example.com\/fallback"
                      }
                    }
                  ]
                }
              }
            ]
        """.trimIndent()

        val postData = RedditPostDataParser.parseFirstPostData(jsonBody)

        assertNotNull(postData)
        assertEquals("https://example.com/fallback", postData.destinationUrl)
    }

    @Test
    fun returnsNullWhenChildrenArrayIsMissing() {
        val jsonBody = """
            [
              {
                "data": {
                  "before": null,
                  "after": null
                }
              }
            ]
        """.trimIndent()

        val postData = RedditPostDataParser.parseFirstPostData(jsonBody)

        assertNull(postData)
    }
}
