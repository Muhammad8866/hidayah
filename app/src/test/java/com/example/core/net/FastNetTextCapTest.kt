package com.example.core.net

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class FastNetTextCapTest {
    @Test fun smallBodyReadsLikeString() {
        val body = "{\"a\":\"م\"}".toResponseBody("application/json; charset=utf-8".toMediaType())
        assertEquals("{\"a\":\"م\"}", FastNet.cappedString(body))
    }

    @Test fun emptyBodyIsEmptyString() {
        assertEquals("", FastNet.cappedString("".toResponseBody(null)))
    }

    @Test fun bodyExactlyAtLimitIsAccepted() {
        assertEquals("x".repeat(10), FastNet.cappedString("x".repeat(10).toResponseBody(null), limit = 10))
    }

    @Test fun declaredLengthOverLimitFails() {
        val error = assertThrows(IOException::class.java) {
            FastNet.cappedString("x".repeat(11).toResponseBody(null), limit = 10)
        }
        assertEquals("Response too large", error.message)
    }

    @Test fun unknownLengthOverLimitFails() {
        val chunked = Buffer().writeUtf8("y".repeat(11)).asResponseBody(null, -1)
        assertThrows(IOException::class.java) { FastNet.cappedString(chunked, limit = 10) }
    }

    @Test fun unknownLengthUnderLimitIsRead() {
        val chunked = Buffer().writeUtf8("hello").asResponseBody(null, -1)
        assertEquals("hello", FastNet.cappedString(chunked, limit = 10))
    }

    @Test fun defaultLimitIsEightMegabytes() {
        assertEquals(8L * 1024 * 1024, FastNet.MAX_TEXT_BYTES)
    }
}
