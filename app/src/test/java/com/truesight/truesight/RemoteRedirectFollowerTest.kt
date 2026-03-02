package com.truesight.truesight

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteRedirectFollowerTest {
    @Test
    fun readBodyWithLimitReturnsEntireBodyWhenUnderLimit() {
        val body = "{\"kind\":\"Listing\",\"data\":{\"children\":[]}}"

        val result = RemoteRedirectFollower.readBodyWithLimit(
            inputStream = ByteArrayInputStream(body.toByteArray()),
            maxChars = body.length + 10
        )

        assertEquals(body, result)
    }

    @Test
    fun readBodyWithLimitTruncatesWhenOverLimit() {
        val body = "abcdefghijklmnopqrstuvwxyz"

        val result = RemoteRedirectFollower.readBodyWithLimit(
            inputStream = ByteArrayInputStream(body.toByteArray()),
            maxChars = 8
        )

        assertEquals("abcdefgh", result)
    }
}
