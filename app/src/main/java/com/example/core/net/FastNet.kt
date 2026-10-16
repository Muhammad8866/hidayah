package com.example.core.net

import android.content.Context
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.Inet4Address
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * The app's one HTTP stack. Every feature shares its connection pool, so a second request to the
 * same host skips the DNS, TCP and TLS handshakes, and large files are fetched over several
 * connections at once instead of being limited by one slow CDN stream.
 */
object FastNet {
    private const val WORKERS = 4
    private const val SEGMENT = 1L shl 20
    private const val ATTEMPTS = 4
    private const val PROGRESS_INTERVAL_NANOS = 100_000_000L

    /** Phones on networks with broken IPv6 otherwise wait out a timeout before trying IPv4. */
    private val ipv4First = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> =
            Dns.SYSTEM.lookup(hostname).sortedBy { if (it is Inet4Address) 0 else 1 }
    }

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(12, 5, TimeUnit.MINUTES))
        .dns(ipv4First)
        .retryOnConnectionFailure(true)
        .addInterceptor { chain ->
            val started = android.os.SystemClock.elapsedRealtime()
            try {
                val response = chain.proceed(chain.request())
                com.example.core.analytics.AppAnalytics.request(response.code,
                    android.os.SystemClock.elapsedRealtime() - started, !response.isSuccessful)
                response
            } catch (error: IOException) {
                com.example.core.analytics.AppAnalytics.request(0,
                    android.os.SystemClock.elapsedRealtime() - started, true)
                throw error
            }
        }
        .build()

    private var filesDir: File? = null

    fun init(context: Context) {
        if (filesDir == null) filesDir = context.applicationContext.filesDir
    }

    /** A file under app storage for keeping a fetched text response, or null before [init]. */
    fun storedFile(name: String): File? = filesDir?.let { File(File(it, "net_text").apply { mkdirs() }, name) }

    /** Blocking GET of a small text resource such as a JSON API response. */
    fun text(url: String): String =
        client.newCall(Request.Builder().url(url).header("Accept", "application/json").build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            response.body?.string() ?: throw IOException("Empty response")
        }

    /** Cancels obsolete searches immediately, freeing their network connection. */
    suspend fun textAsync(url: String): String = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(Request.Builder().url(url).header("Accept", "application/json").build())
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, error: IOException) { continuation.resumeWithException(error) }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val text = response.use {
                        if (!it.isSuccessful) throw IOException("HTTP ${it.code}")
                        it.body?.string() ?: throw IOException("Empty response")
                    }
                    continuation.resume(text)
                } catch (error: Exception) { continuation.resumeWithException(error) }
            }
        })
    }

    /**
     * Downloads [url] into [target] over up to [WORKERS] connections when the server supports byte
     * ranges, and over one otherwise. [target] is removed again if the download does not complete.
     * [onProgress] receives bytes done and the total (-1 while unknown) from background threads.
     */
    suspend fun download(url: String, target: File, onProgress: (Long, Long) -> Unit = { _, _ -> }) =
        withContext(Dispatchers.IO) {
            val telemetry = com.example.core.analytics.AppAnalytics.download()
            target.parentFile?.mkdirs()
            try {
                RandomAccessFile(target, "rw").use { file ->
                    file.setLength(0)
                    Transfer(url, file.channel) { copied, total ->
                        telemetry.progress(copied, total)
                        onProgress(copied, total)
                    }.run()
                }
                telemetry.finish("completed")
            } catch (e: Throwable) {
                telemetry.finish(if (e is kotlinx.coroutines.CancellationException) "cancelled" else "failed")
                target.delete()
                throw e
            }
        }

    private class Transfer(val url: String, val out: FileChannel, val onProgress: (Long, Long) -> Unit) {
        /** Where redirects ended, so later ranges skip them; reset to [url] when a signed link expires. */
        @Volatile var source = url
        @Volatile var total = -1L
        /** File size once the server has answered a range request, or null when it ignores ranges. */
        val size = CompletableDeferred<Long?>()
        val done = AtomicLong()
        val reported = AtomicLong()

        suspend fun run() {
            coroutineScope {
                val next = AtomicLong(SEGMENT)
                repeat(WORKERS - 1) { launch { size.await()?.let { drain(next, it) } } }
                first()
                size.await()?.let { drain(next, it) }
            }
            size.await()?.let { if (out.size() != it) throw IOException("Incomplete download") }
            onProgress(done.get(), total)
        }

        private suspend fun drain(next: AtomicLong, total: Long) {
            val step = (total / (WORKERS * 4)).coerceIn(SEGMENT / 2, SEGMENT * 4)
            while (true) {
                val start = next.getAndAdd(step)
                if (start >= total) break
                segment(start, minOf(total, start + step) - 1)
            }
        }

        private fun open(from: Long, to: Long): Response = client.newCall(
            Request.Builder().url(source)
                // Identity encoding keeps byte offsets exact.
                .header("Accept-Encoding", "identity")
                .header("Range", "bytes=$from-$to")
                .build()
        ).execute()

        /** The first megabyte doubles as the probe that tells whether the server honours ranges. */
        private suspend fun first() {
            var failures = 0
            var pending: Pair<Long, Long>? = null
            while (true) {
                try {
                    val rest = open(0, SEGMENT - 1).use { response ->
                        if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                        val body = response.body ?: throw IOException("Empty response")
                        // Hosts answer quota and error pages with 200 + HTML.
                        if (body.contentType()?.type.equals("text", ignoreCase = true)) {
                            error("Source returned a web page instead of the file")
                        }
                        val ranged = response.header("Content-Range")
                            ?.takeIf { response.code == 206 && it.startsWith("bytes 0-") }
                            ?.substringAfterLast('/')?.toLongOrNull()
                        if (ranged == null) {
                            if (response.code == 206) throw IOException("Unexpected partial response")
                            total = body.contentLength()
                            val end = copy(response, 0, Long.MAX_VALUE)
                            if (total > 0 && end != total) {
                                done.addAndGet(-end)
                                throw IOException("Incomplete download")
                            }
                            out.truncate(end)
                            size.complete(null)
                            return
                        }
                        total = ranged
                        source = response.request.url.toString()
                        size.complete(ranged)
                        val end = minOf(ranged, SEGMENT) - 1
                        // From here a broken stream is resumed by segment(), not by starting over.
                        val reached = try { copy(response, 0, end) } catch (e: IOException) { 0L }
                        reached to end
                    }
                    pending = rest
                    break
                } catch (e: IOException) {
                    if (++failures >= ATTEMPTS) throw e
                    delay(250L * failures)
                }
            }
            pending?.let { (from, to) -> if (from <= to) segment(from, to) }
        }

        private suspend fun segment(start: Long, end: Long) {
            var pos = start
            var failures = 0
            while (pos <= end) {
                try {
                    val reached = open(pos, end).use { response ->
                        if (response.code != 206 || response.header("Content-Range")?.startsWith("bytes $pos-") != true) {
                            throw IOException("HTTP ${response.code}")
                        }
                        source = response.request.url.toString()
                        copy(response, pos, end)
                    }
                    if (reached == pos) throw IOException("Empty range")
                    pos = reached
                    failures = 0
                } catch (e: IOException) {
                    if (++failures >= ATTEMPTS) throw e
                    source = url
                    delay(250L * failures)
                }
            }
        }

        /**
         * Writes the body at file offset [from] up to and including [to] and returns the next
         * offset. A stream that breaks after some bytes returns early so the caller resumes there.
         */
        private suspend fun copy(response: Response, from: Long, to: Long): Long {
            val context = currentCoroutineContext()
            var pos = from
            try {
                val input = response.body?.byteStream() ?: throw IOException("Empty response")
                val buffer = ByteArray(1 shl 16)
                while (pos <= to) {
                    val read = input.read(buffer, 0, minOf(buffer.size.toLong(), to - pos + 1).toInt())
                    if (read < 0) break
                    val bytes = ByteBuffer.wrap(buffer, 0, read)
                    while (bytes.hasRemaining()) out.write(bytes, pos + bytes.position())
                    pos += read
                    context.ensureActive()
                    val now = System.nanoTime()
                    val last = reported.get()
                    val sum = done.addAndGet(read.toLong())
                    if (now - last >= PROGRESS_INTERVAL_NANOS && reported.compareAndSet(last, now)) onProgress(sum, total)
                }
            } catch (e: IOException) {
                if (pos == from) throw e
            }
            return pos
        }
    }
}
