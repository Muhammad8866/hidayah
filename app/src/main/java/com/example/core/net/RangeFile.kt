package com.example.core.net

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Runs block loads on a fixed set of threads. A load a reader waits on always starts before
 * read-ahead, and no read-ahead starts while one is waiting, so on a slow link speculative blocks
 * never take the bandwidth the page on screen needs. Read-ahead already running is left to finish.
 */
internal class BlockScheduler(private val threads: Int, private val name: String) {
    companion object {
        /** Shared by every book and Quran edition, so all of them together use at most four threads. */
        val shared = BlockScheduler(4, "pdf-range")
    }

    /** One block load; [demanded] once a reader waits on it. Guarded by [lock]. */
    private class Load(work: Callable<ByteArray>) : FutureTask<ByteArray>(work) {
        var demanded = false
    }

    private val lock = ReentrantLock()
    private val changed = lock.newCondition()
    private val demand = ArrayDeque<Load>()
    private val ahead = ArrayDeque<Load>()
    /** Demanded loads queued or running; read-ahead starts only while this is zero. */
    private var waiting = 0
    private var started = 0

    fun submit(demanded: Boolean, task: Callable<ByteArray>): Future<ByteArray> {
        val load = Load(task)
        lock.withLock {
            while (started < threads) {
                started++
                Thread({ runLoads() }, "$name-$started").apply { isDaemon = true }.start()
            }
            if (demanded) {
                load.demanded = true
                waiting++
                demand.addLast(load)
            } else {
                ahead.addLast(load)
            }
            changed.signalAll()
        }
        return load
    }

    /** A reader now waits on [future]: queued read-ahead jumps the queue, running read-ahead counts as demand. */
    fun promote(future: Future<ByteArray>) {
        val load = future as? Load ?: return
        lock.withLock {
            if (load.demanded || load.isDone) return
            load.demanded = true
            waiting++
            if (ahead.remove(load)) demand.addLast(load)
            changed.signalAll()
        }
    }

    private fun runLoads() {
        while (true) {
            val load = lock.withLock {
                while (demand.isEmpty() && (waiting > 0 || ahead.isEmpty())) changed.await()
                demand.removeFirstOrNull() ?: ahead.removeFirst()
            }
            try {
                // FutureTask keeps a failure inside the future, so the worker always continues.
                load.run()
            } finally {
                lock.withLock {
                    if (load.demanded) {
                        waiting--
                        changed.signalAll()
                    }
                }
            }
        }
    }
}

/**
 * Seekable HTTP file. Only complete, validated blocks enter the disk cache.
 *
 * Opening a PDF reads its header, then the cross-reference table at the end, then objects spread
 * through the file. So the tail is asked for together with the header, the next blocks are fetched
 * ahead while the reader waits on nothing, and a reopened book starts from its cached blocks without
 * waiting for the network. Every opening of one book shares the same requests, so a warm-up and the
 * reader never fetch twice.
 */
internal class RangeFile(
    private val url: String,
    private val directory: File,
    private val client: OkHttpClient = FastNet.client,
    private val scheduler: BlockScheduler = BlockScheduler.shared
) {
    // A larger block amortizes the redirect and TLS round trip for adjacent PDF reads.
    companion object {
        const val BLOCK = 1024 * 1024
        private const val READ_AHEAD = 2
        /** Upper bound for a demand read so a stalled fetch cannot block the renderer forever. */
        internal const val DEMAND_TIMEOUT_SECONDS = 60L
        /**
         * The tail waits here for its suffix answer, which takes seconds on a slow server, so several
         * warm-ups never hold every block thread while the reader needs one. Idle threads end.
         */
        private val tailWaiter = Executors.newCachedThreadPool { task ->
            Thread(task, "pdf-range-tail").apply { isDaemon = true }
        }
        /** Block loads in flight, keyed "<block folder>#<index>", shared by every opening of a book. */
        private val pending = ConcurrentHashMap<String, Future<ByteArray>>()
        /** One probe per book folder at a time; a second opening then starts from the cached header. */
        private val prepareLocks = ConcurrentHashMap<String, Any>()
    }
    @Volatile private var source = url
    private var validator: String? = null
    private var size = 0L
    private lateinit var blocks: File
    private val meta get() = File(directory, "source.meta")

    /** The last bytes of the file, asked for alongside the header. */
    private class Suffix(val range: Triple<Long, Long, Long>, val validator: String?, val bytes: ByteArray)

    /** [readAhead] false fetches only the header and the tail, as a warm-up before the reader opens. */
    fun prepare(readAhead: Boolean = true): Long {
        // Only the probe is serialized; nothing waited on under this lock ever takes it.
        synchronized(prepareLocks.computeIfAbsent(directory.absolutePath) { Any() }) {
            if (!reuseCached()) prepareFromNetwork()
        }
        // The renderer reads the end of the file right after the header.
        val last = (size - 1) / BLOCK
        prefetch(last)
        if (readAhead) for (index in 1..READ_AHEAD.toLong()) if (index < last) prefetch(index)
        return size
    }

    private fun key(index: Long) = "${blocks.path}#$index"

    /** A book opened before: start from its cached blocks; [fetch] still checks the validator. */
    private fun reuseCached(): Boolean {
        val lines = runCatching { meta.readLines() }.getOrNull() ?: return false
        if (lines.size < 3 || lines[0] != url) return false
        val cachedSize = lines[1].toLongOrNull() ?: return false
        val folder = File(directory, lines[2])
        val first = File(folder, "0")
        if (cachedSize <= 0 || first.length() != minOf(BLOCK.toLong(), cachedSize)) return false
        size = cachedSize
        validator = lines.getOrNull(3)?.takeIf { it.isNotEmpty() }
        blocks = folder
        folder.setLastModified(System.currentTimeMillis())
        return true
    }

    /**
     * Probes the header while the tail is asked for at the same time, so a slow server costs one
     * round trip instead of two before the renderer can read the cross-reference table.
     */
    private fun prepareFromNetwork() {
        val (suffixCall, suffix) = requestSuffix()
        try {
            probeHeader()
        } catch (error: Throwable) {
            suffixCall.cancel()
            throw error
        }
        val last = (size - 1) / BLOCK
        if (last == 0L) { suffixCall.cancel(); return }
        // The tail is registered as in flight before prepare() returns, so no reader asks for it again.
        var used = false
        pending.computeIfAbsent(key(last)) { key ->
            used = true
            tailWaiter.submit<ByteArray> {
                try { tail(last, suffix) } finally { pending.remove(key) }
            }
        }
        if (!used) suffixCall.cancel()
    }

    /** The last [BLOCK] bytes; the answer is checked against the header once both are back. */
    private fun requestSuffix(): Pair<Call, CompletableFuture<Suffix?>> {
        val answer = CompletableFuture<Suffix?>()
        val call = client.newCall(
            Request.Builder().url(url).header("Accept-Encoding", "identity").header("Range", "bytes=-$BLOCK").build()
        )
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { answer.complete(null) }
            override fun onResponse(call: Call, response: Response) {
                answer.complete(runCatching { response.use { readSuffix(it) } }.getOrNull())
            }
        })
        return call to answer
    }

    /** Null unless a range of at most one block arrived in full; a whole-file answer is closed unread. */
    private fun readSuffix(response: Response): Suffix? {
        val range = parseRange(response.header("Content-Range"))
        if (response.code != 206 || range == null || range.second - range.first + 1 > BLOCK) return null
        val body = response.body?.source() ?: return null
        if (body.request(BLOCK + 1L)) return null
        val bytes = body.readByteArray()
        if (bytes.size.toLong() != range.second - range.first + 1) return null
        return Suffix(range, validatorOf(response), bytes)
    }

    /** The last aligned block, cut from the suffix when it is the same file version, else fetched alone. */
    private fun tail(index: Long, suffix: CompletableFuture<Suffix?>): ByteArray {
        cached(index)?.let { return it }
        val answer = runCatching { suffix.get(DEMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS) }.getOrNull()
        val start = index * BLOCK
        // The suffix always covers the last aligned block, which is at most one block long.
        if (answer == null || answer.range.third != size || answer.range.second != size - 1 ||
            answer.range.first > start || answer.validator != validator) return load(index)
        val from = (start - answer.range.first).toInt()
        return store(index, answer.bytes.copyOfRange(from, from + expected(index)))
    }

    private fun validatorOf(response: Response): String? =
        response.header("ETag")?.takeUnless { it.startsWith("W/") } ?: response.header("Last-Modified")

    private fun probeHeader() {
        client.newCall(request(url, 0, BLOCK - 1L)).execute().use { response ->
            val range = parseRange(response.header("Content-Range"))
            if (response.code != 206 || range == null || range.first != 0L ||
                range.second != minOf(range.third, BLOCK.toLong()) - 1) throw IOException("Byte ranges unavailable")
            size = range.third
            source = response.request.url.toString()
            validator = validatorOf(response)
            // Keyed by the stable URL and size, so a source without a validator still reuses its blocks.
            val identity = "$url/$size/${validator.orEmpty()}"
            val hash = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray())
                .joinToString("") { "%02x".format(it) }
            blocks = File(directory, hash).apply { mkdirs() }
            val bytes = response.body?.bytes() ?: throw IOException("Empty range")
            if (bytes.size.toLong() != range.second + 1 || !bytes.take(5).toByteArray()
                    .contentEquals("%PDF-".toByteArray())) throw IOException("Invalid PDF range")
            File(blocks, "0").writeBytes(bytes)
            // The signed redirect target expires, so only the original URL is remembered.
            meta.writeText(listOf(url, size.toString(), hash, validator.orEmpty()).joinToString("\n"))
            trimCache()
        }
    }

    fun read(offset: Long, count: Int, destination: ByteArray): Int {
        if (offset < 0 || count < 0 || count > destination.size) throw IOException("Invalid read")
        if (offset >= size) return 0
        val wanted = minOf(count.toLong(), size - offset).toInt()
        var copied = 0
        while (copied < wanted) {
            val position = offset + copied
            val index = position / BLOCK
            val bytes = block(index)
            for (ahead in 1..READ_AHEAD) prefetch(index + ahead)
            val within = (position - index * BLOCK).toInt()
            val length = minOf(wanted - copied, bytes.size - within)
            bytes.copyInto(destination, copied, within, within + length)
            copied += length
        }
        return copied
    }

    private fun expected(index: Long) = minOf(BLOCK.toLong(), size - index * BLOCK).toInt()

    private fun cached(index: Long): ByteArray? =
        File(blocks, index.toString()).takeIf { it.length() == expected(index).toLong() }?.readBytes()

    private fun block(index: Long): ByteArray {
        cached(index)?.let { return it }
        val key = key(index)
        val future = pending.computeIfAbsent(key) { scheduler.submit(true) { load(index) } }
        // Read-ahead of this very block may still be queued; it now goes first.
        scheduler.promote(future)
        return try {
            future.get(DEMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } catch (error: ExecutionException) {
            throw error.cause as? IOException ?: IOException("Range failed", error.cause)
        } catch (error: TimeoutException) {
            throw IOException("Range timed out", error)
        } finally {
            pending.remove(key, future)
        }
    }

    private fun prefetch(index: Long) {
        if (index < 0 || index * BLOCK >= size || File(blocks, index.toString()).exists()) return
        pending.computeIfAbsent(key(index)) { key ->
            scheduler.submit(false) {
                try { load(index) } finally { pending.remove(key) }
            }
        }
    }

    private fun load(index: Long): ByteArray {
        cached(index)?.let { return it }
        return store(index, fetch(index * BLOCK, expected(index)))
    }

    private fun store(index: Long, bytes: ByteArray): ByteArray {
        blocks.mkdirs()
        val file = File(blocks, index.toString())
        // Unique per call in the same folder, so concurrent stores never share a file and the rename stays atomic.
        val temporary = File.createTempFile("$index.block.", ".tmp", blocks)
        temporary.writeBytes(bytes)
        if (!temporary.renameTo(file) && file.length() != bytes.size.toLong()) {
            temporary.delete(); throw IOException("Could not cache range")
        }
        temporary.delete()
        return bytes
    }

    private fun trimCache() {
        val root = directory.parentFile ?: return
        val entries = root.listFiles()?.filter { it.isDirectory }?.flatMap { book ->
            book.listFiles()?.filter { it.isDirectory }.orEmpty()
        }.orEmpty()
        val sizes = entries.associateWith { entry -> entry.listFiles()?.sumOf { it.length() } ?: 0L }
        var used = sizes.values.sum()
        for (entry in entries.sortedBy { it.lastModified() }) {
            if (used <= 256L * 1024 * 1024) break
            if (entry == blocks) continue
            if (entry.deleteRecursively()) used -= sizes.getValue(entry)
        }
    }

    private fun fetch(start: Long, count: Int): ByteArray {
        var failure: IOException? = null
        var changed = false
        repeat(2) { attempt ->
            try {
                client.newCall(request(if (attempt == 0) source else url, start, start + count - 1)).execute().use { response ->
                    val range = parseRange(response.header("Content-Range"))
                    if (response.code != 206 || range != Triple(start, start + count - 1, size)) {
                        changed = true; throw IOException("Source changed or ignored range")
                    }
                    val current = response.header("ETag")?.takeUnless { it.startsWith("W/") }
                        ?: response.header("Last-Modified")
                    if (validator != null && current != validator) { changed = true; throw IOException("PDF changed at source") }
                    val bytes = response.body?.bytes() ?: throw IOException("Empty range")
                    if (bytes.size != count) throw IOException("Incomplete range")
                    source = response.request.url.toString()
                    return bytes
                }
            } catch (error: IOException) {
                failure = error
                // Jittered pause so many readers do not retry a struggling server in lockstep.
                if (attempt == 0 && !changed) Thread.sleep(250L + kotlin.random.Random.nextLong(250L))
            }
        }
        // Only a changed source invalidates the cached description; a flaky network keeps it.
        if (changed) meta.delete()
        throw failure ?: IOException("Range failed")
    }

    private fun request(address: String, start: Long, end: Long) = Request.Builder().url(address)
        .header("Accept-Encoding", "identity").header("Range", "bytes=$start-$end")
        .apply { validator?.let { header("If-Range", it) } }.build()

    private fun parseRange(value: String?): Triple<Long, Long, Long>? {
        val match = Regex("bytes (\\d+)-(\\d+)/(\\d+)").matchEntire(value.orEmpty()) ?: return null
        val (start, end, total) = match.destructured
        return Triple(start.toLong(), end.toLong(), total.toLong()).takeIf { it.first <= it.second && it.second < it.third }
    }
}
