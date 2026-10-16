package com.example.core.net

import com.sun.net.httpserver.HttpServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

class RangeFileTest {
    @get:Rule val temporary = TemporaryFolder()
    private lateinit var server: HttpServer
    private lateinit var url: String
    private val bytes = ByteArray(RangeFile.BLOCK * 4 + 17) { (it % 251).toByte() }
    private val requests = AtomicInteger()
    @Volatile private var etag = "\"version-one\""
    @Volatile private var ignoreRange = false

    @Before fun start() {
        "%PDF-".toByteArray().copyInto(bytes)
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/book") { exchange ->
            requests.incrementAndGet()
            val range = exchange.requestHeaders.getFirst("Range").removePrefix("bytes=").split('-')
            val start = range[0].toInt()
            val end = minOf(range[1].toInt(), bytes.lastIndex)
            exchange.responseHeaders.add("ETag", etag)
            if (ignoreRange) {
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } else {
                exchange.responseHeaders.add("Content-Range", "bytes $start-$end/${bytes.size}")
                exchange.sendResponseHeaders(206, (end - start + 1).toLong())
                exchange.responseBody.use { it.write(bytes, start, end - start + 1) }
            }
            exchange.close()
        }
        server.start()
        url = "http://127.0.0.1:${server.address.port}/book"
    }

    @After fun stop() { server.stop(0) }

    @Test fun `seek reads exact bytes and repeats reuse cache without downloading whole PDF`() {
        val directory = temporary.newFolder()
        val file = RangeFile(url, directory, OkHttpClient())
        assertEquals(bytes.size.toLong(), file.prepare())
        val result = ByteArray(100)
        val offset = RangeFile.BLOCK - 20L
        assertEquals(100, file.read(offset, 100, result))
        assertArrayEquals(bytes.copyOfRange(offset.toInt(), offset.toInt() + 100), result)
        assertEquals(2, requests.get())
        file.read(offset, 100, result)
        assertEquals(2, requests.get())
        // Reopening probes the source, but keeps validated blocks from the previous session.
        val reopened = RangeFile(url, directory, OkHttpClient())
        reopened.prepare()
        reopened.read(offset, 100, result)
        assertEquals(3, requests.get())
        assertEquals(17, reopened.read((bytes.size - 17).toLong(), 100, result))
        assertEquals(0, reopened.read(bytes.size.toLong(), 100, result))
    }

    @Test fun `changed source cannot mix new bytes into open document`() {
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient())
        file.prepare()
        etag = "\"version-two\""
        try {
            file.read(RangeFile.BLOCK.toLong(), 10, ByteArray(10))
            fail("Changed source accepted")
        } catch (_: IOException) { }
    }

    @Test fun `server ignoring ranges selects full download fallback`() {
        ignoreRange = true
        try {
            RangeFile(url, temporary.newFolder(), OkHttpClient()).prepare()
            fail("Non-ranged response accepted")
        } catch (_: IOException) { }
    }
}
