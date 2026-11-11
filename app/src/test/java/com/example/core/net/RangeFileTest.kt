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
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RangeFileTest {
    @get:Rule val temporary = TemporaryFolder()
    private lateinit var server: HttpServer
    private lateinit var url: String
    private val bytes = ByteArray(RangeFile.BLOCK * 4 + 17) { (it % 251).toByte() }
    @Volatile private var content = bytes
    /** Every Range header the server received, as sent ("0-1048575", "-1048576", ...). */
    private val ranges: MutableList<String> = Collections.synchronizedList(mutableListOf())
    @Volatile private var etag = "\"version-one\""
    @Volatile private var ignoreRange = false
    /** How the server answers a suffix range ("bytes=-N"). */
    @Volatile private var suffixAnswer = SuffixAnswer.CORRECT
    @Volatile private var headerDelay: CountDownLatch? = null
    /** Holds back the answer to one exact Range header until the latch opens. */
    @Volatile private var blockDelay: Pair<String, CountDownLatch>? = null

    private enum class SuffixAnswer { CORRECT, FULL, WRONG_TOTAL, OTHER_ETAG }

    private fun blockRange(index: Int) = "${index.toLong() * RangeFile.BLOCK}-${(index + 1L) * RangeFile.BLOCK - 1}"

    private val tailRange get() = "${4L * RangeFile.BLOCK}-${bytes.lastIndex}"
    private val suffixRange = "-${RangeFile.BLOCK}"
    private fun count(range: String) = synchronized(ranges) { ranges.count { it == range } }

    private val handlers = Executors.newCachedThreadPool()

    @Before fun start() {
        "%PDF-".toByteArray().copyInto(bytes)
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.executor = handlers
        server.createContext("/book") { exchange ->
            val data = content
            val header = exchange.requestHeaders.getFirst("Range").removePrefix("bytes=")
            ranges += header
            val (from, to) = header.split('-')
            val suffix = from.isEmpty()
            val start = if (suffix) maxOf(0, data.size - to.toInt()) else from.toInt()
            val end = if (suffix) data.lastIndex else minOf(to.toInt(), data.lastIndex)
            if (start == 0 && !suffix) headerDelay?.await(5, TimeUnit.SECONDS)
            blockDelay?.let { (range, gate) -> if (header == range) gate.await(5, TimeUnit.SECONDS) }
            val answer = if (suffix) suffixAnswer else SuffixAnswer.CORRECT
            exchange.responseHeaders.add("ETag", if (answer == SuffixAnswer.OTHER_ETAG) "\"version-two\"" else etag)
            if (ignoreRange || answer == SuffixAnswer.FULL) {
                exchange.sendResponseHeaders(200, data.size.toLong())
                runCatching { exchange.responseBody.use { it.write(data) } }
            } else {
                val total = if (answer == SuffixAnswer.WRONG_TOTAL) data.size + 5 else data.size
                exchange.responseHeaders.add("Content-Range", "bytes $start-$end/$total")
                exchange.sendResponseHeaders(206, (end - start + 1).toLong())
                runCatching { exchange.responseBody.use { it.write(data, start, end - start + 1) } }
            }
            exchange.close()
        }
        server.start()
        url = "http://127.0.0.1:${server.address.port}/book"
    }

    @After fun stop() {
        headerDelay?.countDown()
        blockDelay?.second?.countDown()
        server.stop(0)
        handlers.shutdownNow()
    }

    @Test fun `seek reads exact bytes and repeats reuse cache without downloading whole PDF`() {
        val directory = temporary.newFolder()
        val file = RangeFile(url, directory, OkHttpClient())
        assertEquals(bytes.size.toLong(), file.prepare())
        val result = ByteArray(100)
        val offset = RangeFile.BLOCK - 20L
        assertEquals(100, file.read(offset, 100, result))
        assertArrayEquals(bytes.copyOfRange(offset.toInt(), offset.toInt() + 100), result)
        file.read(offset, 100, result)
        // Each block crosses the network once, however often it is read.
        assertEquals(1, count("0-${RangeFile.BLOCK - 1}"))
        assertEquals(1, count("${RangeFile.BLOCK}-${2 * RangeFile.BLOCK - 1}"))
        // Reopening starts from the cached blocks without probing the header again.
        val reopened = RangeFile(url, directory, OkHttpClient())
        reopened.prepare()
        reopened.read(offset, 100, result)
        assertEquals(1, count("0-${RangeFile.BLOCK - 1}"))
        assertEquals(17, reopened.read((bytes.size - 17).toLong(), 100, result))
        assertArrayEquals(bytes.copyOfRange(bytes.size - 17, bytes.size), result.copyOf(17))
        assertEquals(0, reopened.read(bytes.size.toLong(), 100, result))
    }

    @Test fun `changed source cannot mix new bytes into open document`() {
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient())
        file.prepare()
        etag = "\"version-two\""
        try {
            // Block 3 is neither read ahead nor the tail, so it is fetched after the change.
            file.read(3L * RangeFile.BLOCK, 10, ByteArray(10))
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

    private fun readTail(file: RangeFile): ByteArray {
        val result = ByteArray(40)
        assertEquals(40, file.read((bytes.size - 40).toLong(), 40, result))
        return result
    }

    @Test fun `tail asked for with the header is served without a second tail request`() {
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient())
        file.prepare()
        assertArrayEquals(bytes.copyOfRange(bytes.size - 40, bytes.size), readTail(file))
        assertEquals(1, count(suffixRange))
        assertEquals(0, count(tailRange))
        // The tail block crosses into the previous aligned block too; that block is still fetched on its own.
        val middle = ByteArray(10)
        file.read(3L * RangeFile.BLOCK + 5, 10, middle)
        assertArrayEquals(bytes.copyOfRange(3 * RangeFile.BLOCK + 5, 3 * RangeFile.BLOCK + 15), middle)
    }

    @Test fun `suffix answered in full falls back to a ranged tail`() {
        suffixAnswer = SuffixAnswer.FULL
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient())
        assertEquals(bytes.size.toLong(), file.prepare())
        assertArrayEquals(bytes.copyOfRange(bytes.size - 40, bytes.size), readTail(file))
        assertEquals(1, count(tailRange))
    }

    @Test fun `suffix with another total falls back to a ranged tail`() {
        suffixAnswer = SuffixAnswer.WRONG_TOTAL
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient())
        file.prepare()
        assertArrayEquals(bytes.copyOfRange(bytes.size - 40, bytes.size), readTail(file))
        assertEquals(1, count(tailRange))
    }

    @Test fun `suffix with another validator falls back to a ranged tail`() {
        suffixAnswer = SuffixAnswer.OTHER_ETAG
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient())
        file.prepare()
        assertArrayEquals(bytes.copyOfRange(bytes.size - 40, bytes.size), readTail(file))
        assertEquals(1, count(tailRange))
    }

    @Test fun `two openings of one book at once make one header and one tail request`() {
        val directory = temporary.newFolder()
        val gate = CountDownLatch(1)
        headerDelay = gate
        val pool = Executors.newFixedThreadPool(2)
        try {
            val first = pool.submit<RangeFile> { RangeFile(url, directory, OkHttpClient()).also { it.prepare() } }
            val second = pool.submit<RangeFile> { RangeFile(url, directory, OkHttpClient()).also { it.prepare() } }
            Thread.sleep(200)
            gate.countDown()
            val files = listOf(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS))
            files.forEach { assertArrayEquals(bytes.copyOfRange(bytes.size - 40, bytes.size), readTail(it)) }
            assertEquals(1, count("0-${RangeFile.BLOCK - 1}"))
            assertEquals(1, count(suffixRange))
            assertEquals(0, count(tailRange))
        } finally {
            pool.shutdownNow()
        }
    }

    @Test fun `book smaller than one block opens from its header alone`() {
        val small = ByteArray(1_000) { (it % 97).toByte() }
        "%PDF-".toByteArray().copyInto(small)
        content = small
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient())
        assertEquals(1_000L, file.prepare())
        val result = ByteArray(1_000)
        assertEquals(1_000, file.read(0, 1_000, result))
        assertArrayEquals(small, result)
        assertEquals(1, count("0-${RangeFile.BLOCK - 1}"))
    }

    // ---- Demanded blocks before read-ahead ----

    private val nothing = ByteArray(0)

    @Test fun `scheduler starts a demanded load before queued read-ahead`() {
        val scheduler = BlockScheduler(1, "test-demand-first")
        val started = Collections.synchronizedList(mutableListOf<String>())
        val gate = CountDownLatch(1)
        val running = CountDownLatch(1)
        scheduler.submit(false) { started += "busy"; running.countDown(); gate.await(5, TimeUnit.SECONDS); nothing }
        assertTrue(running.await(5, TimeUnit.SECONDS))
        val first = scheduler.submit(false) { started += "ahead-1"; nothing }
        val second = scheduler.submit(false) { started += "ahead-2"; nothing }
        val demanded = scheduler.submit(true) { started += "demand"; nothing }
        gate.countDown()
        listOf(demanded, first, second).forEach { it.get(5, TimeUnit.SECONDS) }
        assertEquals(listOf("busy", "demand", "ahead-1", "ahead-2"), started.toList())
    }

    @Test fun `queued read-ahead that a reader waits on jumps the queue`() {
        val scheduler = BlockScheduler(1, "test-promote-queued")
        val started = Collections.synchronizedList(mutableListOf<String>())
        val gate = CountDownLatch(1)
        val running = CountDownLatch(1)
        scheduler.submit(false) { started += "busy"; running.countDown(); gate.await(5, TimeUnit.SECONDS); nothing }
        assertTrue(running.await(5, TimeUnit.SECONDS))
        val first = scheduler.submit(false) { started += "ahead-1"; nothing }
        val second = scheduler.submit(false) { started += "ahead-2"; nothing }
        scheduler.promote(second)
        gate.countDown()
        listOf(first, second).forEach { it.get(5, TimeUnit.SECONDS) }
        assertEquals(listOf("busy", "ahead-2", "ahead-1"), started.toList())
    }

    @Test fun `no read-ahead starts while a demanded load is running`() {
        val scheduler = BlockScheduler(2, "test-hold-ahead")
        val started = Collections.synchronizedList(mutableListOf<String>())
        val gate = CountDownLatch(1)
        val running = CountDownLatch(1)
        val demanded = scheduler.submit(true) { started += "demand"; running.countDown(); gate.await(5, TimeUnit.SECONDS); nothing }
        assertTrue(running.await(5, TimeUnit.SECONDS))
        val ahead = scheduler.submit(false) { started += "ahead"; nothing }
        Thread.sleep(200)
        // A second thread is free, yet read-ahead waits for the block the reader needs.
        assertFalse(ahead.isDone)
        gate.countDown()
        demanded.get(5, TimeUnit.SECONDS)
        ahead.get(5, TimeUnit.SECONDS)
        assertEquals(listOf("demand", "ahead"), started.toList())
    }

    @Test fun `running read-ahead that a reader waits on holds back further read-ahead`() {
        val scheduler = BlockScheduler(2, "test-promote-running")
        val started = Collections.synchronizedList(mutableListOf<String>())
        val gate = CountDownLatch(1)
        val running = CountDownLatch(1)
        val first = scheduler.submit(false) { started += "first"; running.countDown(); gate.await(5, TimeUnit.SECONDS); nothing }
        assertTrue(running.await(5, TimeUnit.SECONDS))
        scheduler.promote(first)
        val second = scheduler.submit(false) { started += "second"; nothing }
        Thread.sleep(200)
        assertFalse(second.isDone)
        gate.countDown()
        listOf(first, second).forEach { it.get(5, TimeUnit.SECONDS) }
        assertEquals(listOf("first", "second"), started.toList())
    }

    @Test fun `promoting a finished or foreign future changes nothing`() {
        val scheduler = BlockScheduler(1, "test-promote-noop")
        val done = scheduler.submit(false) { nothing }
        done.get(5, TimeUnit.SECONDS)
        scheduler.promote(done)
        scheduler.promote(java.util.concurrent.CompletableFuture.completedFuture(nothing))
        // Read-ahead still runs: no demand was left counted.
        assertArrayEquals(nothing, scheduler.submit(false) { nothing }.get(5, TimeUnit.SECONDS))
    }

    @Test fun `failed demanded load releases held back read-ahead`() {
        val scheduler = BlockScheduler(1, "test-demand-fails")
        val failed = scheduler.submit(true) { throw IOException("offline") }
        try { failed.get(5, TimeUnit.SECONDS); fail("Failure swallowed") } catch (_: java.util.concurrent.ExecutionException) { }
        assertArrayEquals(nothing, scheduler.submit(false) { nothing }.get(5, TimeUnit.SECONDS))
    }

    private fun awaitRequest(range: String) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (count(range) == 0) {
            assertTrue("No request for $range", System.nanoTime() < deadline)
            Thread.sleep(10)
        }
    }

    @Test fun `reader's block is fetched before queued read-ahead of the same book`() {
        val gate = CountDownLatch(1)
        blockDelay = blockRange(1) to gate
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient(), BlockScheduler(1, "test-book-order"))
        file.prepare()
        // Read-ahead of block 1 holds the only thread; block 2 waits in the queue behind it.
        awaitRequest(blockRange(1))
        val reader = Executors.newSingleThreadExecutor()
        try {
            val result = ByteArray(10)
            val read = reader.submit<Int> { file.read(3L * RangeFile.BLOCK + 5, 10, result) }
            Thread.sleep(200)
            gate.countDown()
            assertEquals(10, read.get(10, TimeUnit.SECONDS))
            assertArrayEquals(bytes.copyOfRange(3 * RangeFile.BLOCK + 5, 3 * RangeFile.BLOCK + 15), result)
            awaitRequest(blockRange(2))
            val order = synchronized(ranges) { ranges.toList() }
            assertTrue("Order was $order", order.indexOf(blockRange(3)) < order.indexOf(blockRange(2)))
        } finally {
            reader.shutdownNow()
        }
    }

    @Test fun `block already loading as read-ahead is reused by the reader`() {
        val gate = CountDownLatch(1)
        blockDelay = blockRange(1) to gate
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient(), BlockScheduler(2, "test-book-reuse"))
        file.prepare()
        awaitRequest(blockRange(1))
        val reader = Executors.newSingleThreadExecutor()
        try {
            val result = ByteArray(10)
            val read = reader.submit<Int> { file.read(RangeFile.BLOCK + 5L, 10, result) }
            Thread.sleep(200)
            gate.countDown()
            assertEquals(10, read.get(10, TimeUnit.SECONDS))
            assertArrayEquals(bytes.copyOfRange(RangeFile.BLOCK + 5, RangeFile.BLOCK + 15), result)
            assertEquals(1, count(blockRange(1)))
        } finally {
            reader.shutdownNow()
        }
    }

    @Test fun `many readers on one book fetch each block once on a bounded set of threads`() {
        val threads = 4
        val name = "test-scale-${System.nanoTime()}"
        val file = RangeFile(url, temporary.newFolder(), OkHttpClient(), BlockScheduler(threads, name))
        file.prepare()
        val readers = Executors.newFixedThreadPool(64)
        val peakThreads = java.util.concurrent.atomic.AtomicInteger()
        val started = System.nanoTime()
        try {
            val jobs = (0 until 64).map { reader ->
                readers.submit(java.util.concurrent.Callable {
                    val random = kotlin.random.Random(reader)
                    repeat(50) { step ->
                        val offset = random.nextLong(bytes.size.toLong())
                        val result = ByteArray(64)
                        val read = file.read(offset, 64, result)
                        assertArrayEquals(bytes.copyOfRange(offset.toInt(), offset.toInt() + read), result.copyOf(read))
                        if (step % 10 == 0) {
                            val live = Thread.getAllStackTraces().keys.count { it.name.startsWith(name) }
                            peakThreads.accumulateAndGet(live) { a, b -> maxOf(a, b) }
                        }
                    }
                })
            }
            jobs.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            readers.shutdownNow()
        }
        val millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
        println("RangeFile scale: 64 readers x 50 reads in $millis ms, peak fetch threads ${peakThreads.get()}")
        // Every block crosses the network at most once, however many readers want it.
        (1..3).forEach { assertTrue("Block $it fetched ${count(blockRange(it))} times", count(blockRange(it)) <= 1) }
        assertEquals(1, count("0-${RangeFile.BLOCK - 1}"))
        assertEquals(0, count(tailRange))
        assertTrue(peakThreads.get() <= threads)
    }
}
