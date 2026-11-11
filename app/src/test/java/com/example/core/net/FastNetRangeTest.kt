package com.example.core.net

import kotlinx.coroutines.runBlocking
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

/** Book search previews accept only the exact bytes they asked for; nothing else is read. */
class FastNetRangeTest {
    private val bytes = ByteArray(10) { it.toByte() }

    private fun response(code: Int, contentRange: String?, body: ResponseBody): Response =
        Response.Builder()
            .request(Request.Builder().url("https://example.com/book-search-bodies-1.bin").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("test")
            .apply { if (contentRange != null) header("Content-Range", contentRange) }
            .body(body)
            .build()

    @Test fun exactPartialAnswerIsRead() {
        val answer = response(206, "bytes 100-109/5000", bytes.toResponseBody(null))
        assertArrayEquals(bytes, FastNet.readRange(answer, 100, 109))
    }

    @Test fun partialAnswerOfUnknownLengthIsRead() {
        val answer = response(206, "bytes 100-109/5000", Buffer().write(bytes).asResponseBody(null, -1))
        assertArrayEquals(bytes, FastNet.readRange(answer, 100, 109))
    }

    @Test fun wholeFileAnswerIsClosedUnread() {
        val file = Buffer().write(ByteArray(4096))
        val answer = response(200, null, file.asResponseBody(null, 4096))
        assertThrows(IOException::class.java) { FastNet.readRange(answer, 100, 109) }
        // A server ignoring the range would otherwise stream the whole file.
        assertEquals(4096L, file.size)
    }

    @Test fun anotherRangeIsRefused() {
        val answer = response(206, "bytes 0-9/5000", bytes.toResponseBody(null))
        assertThrows(IOException::class.java) { FastNet.readRange(answer, 100, 109) }
        val missing = response(206, null, bytes.toResponseBody(null))
        assertThrows(IOException::class.java) { FastNet.readRange(missing, 100, 109) }
    }

    @Test fun wrongDeclaredLengthIsRefused() {
        val answer = response(206, "bytes 100-109/5000", ByteArray(12).toResponseBody(null))
        assertThrows(IOException::class.java) { FastNet.readRange(answer, 100, 109) }
    }

    @Test fun shortOrLongStreamsAreRefused() {
        val short = response(206, "bytes 100-109/5000", Buffer().write(ByteArray(6)).asResponseBody(null, -1))
        assertEquals("Incomplete range", assertThrows(IOException::class.java) { FastNet.readRange(short, 100, 109) }.message)
        val long = response(206, "bytes 100-109/5000", Buffer().write(ByteArray(15)).asResponseBody(null, -1))
        assertEquals("Range longer than asked", assertThrows(IOException::class.java) { FastNet.readRange(long, 100, 109) }.message)
    }

    @Test fun badOrHugeRangesFailBeforeAnyRequest() {
        listOf(-1L to 10L, 50L to 10L, 0L to (3L shl 20)).forEach { (from, to) ->
            assertThrows(IOException::class.java) { runBlocking { FastNet.range("https://example.invalid/x.bin", from, to) } }
        }
    }
}
