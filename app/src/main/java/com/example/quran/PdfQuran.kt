package com.example.quran

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Stores and renders the Quran PDFs of [QuranPdfCatalog]. Files are kept byte for byte as
 * published. The Hindi edition is delivered on demand by Google Play; other editions are fetched
 * from [QuranPdf.url]. Every copy is checked against its SHA-256 before it is rendered.
 */
object PdfQuran {

    private const val PREFS = "pdf_quran"
    private const val MAX_IDLE_ATTEMPTS = 5
    /** Under [SLOW_MIN_BYTES] in [SLOW_WINDOW_NANOS] (about 17 KB/s) counts as stalled. */
    private const val SLOW_WINDOW_NANOS = 30_000_000_000L
    private const val SLOW_MIN_BYTES = 512L * 1024
    private const val PARALLEL_PARTS = 4
    private const val PROGRESS_INTERVAL_NANOS = 200_000_000L
    private const val MIN_PARALLEL_BYTES = 4L * 1024 * 1024
    private lateinit var appContext: Context
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val fileLocks = ConcurrentHashMap<String, Mutex>()

    /** Download progress 0..1 per book id while a fetch is running. */
    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress: StateFlow<Map<String, Float>> = _progress.asStateFlow()

    /** Bumped whenever a saved copy is added or removed, so lists can refresh their status. */
    private val _changes = MutableStateFlow(0)
    val changes: StateFlow<Int> = _changes.asStateFlow()

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        // Pages of the earlier Tajweedi reader are no longer used.
        File(appContext.filesDir, "tajweedi_mushaf").deleteRecursively()
    }

    fun lastPage(book: QuranPdf): Int = prefs().getInt("last_${book.id}", 0)
    fun saveLastPage(book: QuranPdf, index: Int) =
        prefs().edit().putInt("last_${book.id}", index).putString("last_book", book.id).apply()

    /** Book the reader was last reading, for the "Continue reading" card. */
    fun lastBook(): QuranPdf? = prefs().getString("last_book", null)?.let(QuranPdfCatalog::byId)

    private fun savedFile(book: QuranPdf) = File(File(appContext.filesDir, "quran_pdfs"), book.fileName)
    private fun cachedFile(book: QuranPdf) = File(File(appContext.cacheDir, "quran_pdfs"), book.fileName)

    private fun isHindiEdition(book: QuranPdf) = book.id == "kanzul_iman"

    /** True when the book opens without internet, including an installed on-demand copy. */
    fun isOffline(book: QuranPdf) = bundledFileExists(book) || savedFile(book).length() == book.sizeBytes ||
        (isHindiEdition(book) && HindiQuranAssetPack.file(appContext)?.length() == book.sizeBytes)

    /** Downloaded by the user (and so removable), independently of a bundled fallback copy. */
    fun isDownloaded(book: QuranPdf) = savedFile(book).length() == book.sizeBytes ||
        (isHindiEdition(book) && HindiQuranAssetPack.file(appContext)?.length() == book.sizeBytes)

    /** Keeps [book] on the phone for offline reading. */
    suspend fun download(book: QuranPdf): Result<Unit> = runCatching {
        val cached = cachedFile(book)
        lockFor(book).withLock {
            if (isDownloaded(book)) return@withLock
            if (isHindiEdition(book) && !bundledFileExists(book) && verifiedPackFile(book) != null) return@withLock
            val target = savedFile(book)
            if (cached.length() == book.sizeBytes) withContext(Dispatchers.IO) { store(cached.inputStream(), book, target) }
            else fetch(book, target)
        }
        _changes.update { it + 1 }
    }

    fun delete(book: QuranPdf) {
        Documents.close(book.id)
        savedFile(book).delete()
        cachedFile(book).delete()
        if (isHindiEdition(book)) {
            HindiQuranAssetPack.remove(appContext) { _changes.update { it + 1 } }
        }
        _changes.update { it + 1 }
    }

    /**
     * The Hindi edition from its Play asset pack, or null when Play cannot deliver it (e.g. a
     * sideloaded install) so the caller falls back to the same GitHub download as other books.
     */
    private suspend fun verifiedPackFile(book: QuranPdf): File? = try {
        val packFile = HindiQuranAssetPack.fetch(appContext) { fraction ->
            _progress.update { it + (book.id to fraction) }
        }
        packFile.takeIf {
            it.length() == book.sizeBytes && withContext(Dispatchers.IO) { sha256(it) } == book.sha256
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (packError: Exception) {
        null
    } finally {
        _progress.update { it - book.id }
    }

    /** A verified local file for [book], falling back to a bundled copy if online fetch fails. */
    private suspend fun localFile(book: QuranPdf): File = lockFor(book).withLock {
        savedFile(book).takeIf { it.length() == book.sizeBytes }?.let { return@withLock it }
        val cached = cachedFile(book)
        if (cached.length() == book.sizeBytes) return@withLock cached
        if (isHindiEdition(book)) {
            if (bundledFileExists(book)) {
                val fallback = File(appContext.cacheDir, "quran_pdfs/${book.fileName}.bundled")
                withContext(Dispatchers.IO) { store(appContext.assets.open(book.fileName), book, fallback) }
                return@withLock fallback
            }
            verifiedPackFile(book)?.let {
                _changes.update { it + 1 }
                return@withLock it
            }
        }
        try {
            fetch(book, cached)
            cached
        } catch (onlineError: Exception) {
            if (!bundledFileExists(book)) throw onlineError
            val fallback = File(appContext.cacheDir, "quran_pdfs/${book.fileName}.bundled")
            withContext(Dispatchers.IO) {
                store(appContext.assets.open(book.fileName), book, fallback)
            }
            fallback
        }
    }

    private fun bundledFileExists(book: QuranPdf): Boolean = runCatching {
        appContext.assets.open(book.fileName).use { true }
    }.getOrDefault(false)

    /**
     * Downloads [book] into [target]. GitHub's CDN can stall for minutes on parts of a file it has
     * not cached yet, so a dropped or crawling connection is resumed where it stopped (the partial
     * file is kept, even across app restarts) and only [MAX_IDLE_ATTEMPTS] tries in a row without
     * progress give up.
     */
    private suspend fun fetch(book: QuranPdf, target: File) = withContext(Dispatchers.IO) {
        target.parentFile?.mkdirs()
        val part = File(target.path + ".part")
        if (part.length() > book.sizeBytes) part.delete()
        _progress.update { it + (book.id to part.length().toFloat() / book.sizeBytes) }
        try {
            downloadRemaining(book, part)
            if (sha256(part) != book.sha256) {
                part.delete()
                error("${book.fileName} checksum mismatch")
            }
            target.delete()
            check(part.renameTo(target)) { "Could not store ${book.fileName}" }
        } finally {
            _progress.update { it - book.id }
        }
    }

    /**
     * Fetches four independent byte ranges. Release-assets supports ranges and parallel requests
     * avoid one slow CDN connection limiting a large PDF. Existing partial downloads are retained.
     */
    private suspend fun downloadRemaining(book: QuranPdf, part: File) {
        val initial = part.length()
        val remaining = book.sizeBytes - initial
        if (remaining < MIN_PARALLEL_BYTES) {
            resumeSingle(book, part)
            return
        }

        val chunkSize = (remaining + PARALLEL_PARTS - 1) / PARALLEL_PARTS
        val ranges = (0 until PARALLEL_PARTS).mapNotNull { index ->
            val start = initial + index * chunkSize
            if (start >= book.sizeBytes) null else ByteRange(
                start = start,
                end = minOf(book.sizeBytes - 1, start + chunkSize - 1),
                file = File(part.path + ".$start-${minOf(book.sizeBytes - 1, start + chunkSize - 1)}")
            )
        }
        val activeRangePaths = ranges.mapTo(hashSetOf()) { it.file.path }
        part.parentFile?.listFiles { file -> file.name.startsWith(part.name + ".") }
            ?.filterNot { it.path in activeRangePaths }
            ?.forEach(File::delete)
        ranges.forEach { range ->
            if (range.file.length() > range.length) range.file.delete()
        }
        val downloaded = AtomicLong(initial + ranges.sumOf { it.file.length() })
        val lastProgress = AtomicLong(0L)
        try {
            coroutineScope {
                ranges.map { range ->
                    async { downloadRange(book, range, downloaded, lastProgress) }
                }.awaitAll()
            }
            FileOutputStream(part, true).use { output ->
                ranges.forEach { range ->
                    check(range.file.length() == range.length) { "Incomplete download range" }
                    range.file.inputStream().use { it.copyTo(output, 1 shl 16) }
                }
            }
        } catch (unsupported: RangeUnsupportedException) {
            ranges.forEach { it.file.delete() }
            resumeSingle(book, part)
        } finally {
            ranges.forEach { it.file.delete() }
        }
    }

    private suspend fun downloadRange(
        book: QuranPdf,
        range: ByteRange,
        downloaded: AtomicLong,
        lastProgress: AtomicLong
    ) {
        var idleAttempts = 0
        while (range.file.length() < range.length) {
            val before = range.file.length()
            val start = range.start + before
            val request = Request.Builder().url(book.url)
                .header("Accept-Encoding", "identity")
                .header("Range", "bytes=$start-${range.end}")
                .build()
            val error = try {
                client.newCall(request).execute().use { response ->
                    if (response.code != 206 || response.header("Content-Range")?.startsWith("bytes $start-${range.end}/") != true) {
                        throw RangeUnsupportedException()
                    }
                    val body = response.body ?: throw IOException("Empty response")
                    val context = currentCoroutineContext()
                    body.byteStream().use { source ->
                        FileOutputStream(range.file, true).use { output ->
                            val buffer = ByteArray(1 shl 16)
                            while (range.file.length() < range.length) {
                                val read = source.read(buffer, 0, minOf(buffer.size.toLong(), range.length - range.file.length()).toInt())
                                if (read < 0) break
                                output.write(buffer, 0, read)
                                val total = downloaded.addAndGet(read.toLong())
                                context.ensureActive()
                                publishProgress(book, total, lastProgress)
                            }
                        }
                    }
                }
                null
            } catch (e: RangeUnsupportedException) {
                throw e
            } catch (e: IOException) {
                e
            }
            if (range.file.length() > before) {
                idleAttempts = 0
            } else {
                if (++idleAttempts >= MAX_IDLE_ATTEMPTS) throw error ?: IOException("${book.fileName} download stalled")
                delay(1_000L * idleAttempts)
            }
        }
    }

    private fun publishProgress(book: QuranPdf, bytes: Long, lastProgress: AtomicLong) {
        val now = System.nanoTime()
        val previous = lastProgress.get()
        if (bytes < book.sizeBytes && now - previous < PROGRESS_INTERVAL_NANOS) return
        if (lastProgress.compareAndSet(previous, now)) {
            _progress.update { it + (book.id to bytes.toFloat() / book.sizeBytes) }
        }
    }

    private suspend fun resumeSingle(book: QuranPdf, part: File) {
        var idleAttempts = 0
        while (part.length() < book.sizeBytes) {
            val before = part.length()
            val error = try { appendRest(book, part); null } catch (e: IOException) { e }
            if (part.length() > before) { idleAttempts = 0; continue }
            if (++idleAttempts >= MAX_IDLE_ATTEMPTS) throw error ?: IOException("${book.fileName} download stalled")
            delay(1_000L * idleAttempts)
        }
    }

    private data class ByteRange(val start: Long, val end: Long, val file: File) {
        val length: Long get() = end - start + 1
    }

    private class RangeUnsupportedException : IOException()

    /** Appends the rest of [book] to [part], starting over if the server ignores the range. */
    private suspend fun appendRest(book: QuranPdf, part: File) {
        val offset = part.length()
        // Identity encoding keeps byte offsets exact for resuming.
        val request = Request.Builder().url(book.url).header("Accept-Encoding", "identity")
            .apply { if (offset > 0) header("Range", "bytes=$offset-") }.build()
        client.newCall(request).execute().use { response ->
            if (response.code >= 500) throw IOException("HTTP ${response.code}")
            check(response.isSuccessful) { "HTTP ${response.code}" }
            val resumed = response.code == 206 && response.header("Content-Range")?.startsWith("bytes $offset-") == true
            val body = response.body ?: throw IOException("Empty response")
            val context = currentCoroutineContext()
            var done = if (resumed) offset else 0L
            var windowStart = System.nanoTime()
            var windowBytes = 0L
            body.byteStream().use { source ->
                FileOutputStream(part, resumed).use { out ->
                    val buffer = ByteArray(1 shl 16)
                    while (true) {
                        val read = source.read(buffer)
                        if (read < 0) break
                        out.write(buffer, 0, read)
                        done += read
                        windowBytes += read
                        context.ensureActive()
                        _progress.update { it + (book.id to done.toFloat() / book.sizeBytes) }
                        // A trickle never trips the read timeout; reconnecting usually gets full speed back.
                        if (System.nanoTime() - windowStart >= SLOW_WINDOW_NANOS) {
                            if (windowBytes < SLOW_MIN_BYTES) throw IOException("Download too slow, reconnecting")
                            windowStart = System.nanoTime()
                            windowBytes = 0
                        }
                    }
                }
            }
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(1 shl 16)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /** Writes [input] to [target] through a temporary file, keeping it only if the checksum matches. */
    private fun store(input: java.io.InputStream, book: QuranPdf, target: File) {
        target.parentFile?.mkdirs()
        val temp = File(target.path + ".part")
        val digest = MessageDigest.getInstance("SHA-256")
        try {
            input.use { source ->
                temp.outputStream().use { out ->
                    val buffer = ByteArray(1 shl 16)
                    while (true) {
                        val read = source.read(buffer)
                        if (read < 0) break
                        out.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                    }
                }
            }
            val sha = digest.digest().joinToString("") { "%02x".format(it) }
            check(sha == book.sha256) { "${book.fileName} checksum mismatch" }
            target.delete()
            check(temp.renameTo(target)) { "Could not store ${book.fileName}" }
        } finally {
            temp.delete()
        }
    }

    /** Opens [book] for rendering. */
    suspend fun open(book: QuranPdf): Result<Document> = runCatching {
        val file = localFile(book)
        withContext(Dispatchers.IO) { Documents.open(book.id, file) }
    }

    /** Returns the verified original PDF, downloading it when needed for external sharing. */
    suspend fun shareFile(book: QuranPdf): Result<File> = runCatching { localFile(book) }

    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun lockFor(book: QuranPdf): Mutex = fileLocks.getOrPut(book.id) { Mutex() }

    /** One open PDF; PdfRenderer renders a single page at a time, hence the lock. */
    class Document internal constructor(private val renderer: PdfRenderer, private val descriptor: ParcelFileDescriptor) {
        val pageCount: Int = renderer.pageCount
        private val lock = Mutex()
        @Volatile private var closed = false
        /** Unique per opened document, so entries in the store-wide [PageCache] never collide. */
        private val cacheId = PageCache.nextId()

        fun cached(index: Int, width: Int): Bitmap? = PageCache.get(cacheId, index, width)

        /** Renders one page [width] pixels wide, keeping the page's own aspect ratio. */
        suspend fun render(index: Int, width: Int): Bitmap? {
            cached(index, width)?.let { return it }
            return withContext(Dispatchers.IO) {
                lock.withLock {
                    if (closed || index !in 0 until pageCount || width <= 0) return@withLock null
                    runCatching {
                        renderer.openPage(index).use { page ->
                            val height = (width.toLong() * page.height / page.width).toInt()
                            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                                eraseColor(Color.WHITE)
                                page.render(this, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            }
                        }
                    }.getOrNull()?.also { PageCache.put(cacheId, index, width, it) }
                }
            }
        }

        internal fun close() {
            closed = true
            PageCache.evict(cacheId)
            runCatching { renderer.close() }
            runCatching { descriptor.close() }
        }
    }

    /** Keeps each opened book's renderer so going back to the list and in again is instant. */
    /**
     * One bounded bitmap cache shared by every open document. Each book used to keep its own
     * cache of up to a fifth of the heap, so opening a few books could exhaust memory.
     */
    private object PageCache {
        private var counter = 0L
        private val memory = object : LruCache<Triple<Long, Int, Int>, Bitmap>((Runtime.getRuntime().maxMemory() / 6).toInt()) {
            override fun sizeOf(key: Triple<Long, Int, Int>, value: Bitmap): Int = value.byteCount
        }

        @Synchronized fun nextId(): Long = ++counter
        fun get(id: Long, index: Int, width: Int): Bitmap? = memory.get(Triple(id, index, width))
        fun put(id: Long, index: Int, width: Int, bitmap: Bitmap) { memory.put(Triple(id, index, width), bitmap) }
        fun evict(id: Long) = memory.snapshot().keys.filter { it.first == id }.forEach { memory.remove(it) }
    }

    private object Documents {
        private val open = mutableMapOf<String, Pair<File, Document>>()

        @Synchronized fun open(id: String, file: File): Document {
            open[id]?.let { (openedFile, document) -> if (openedFile == file) return document else document.close() }
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            return Document(PdfRenderer(descriptor), descriptor).also { open[id] = file to it }
        }

        @Synchronized fun close(id: String) {
            open.remove(id)?.second?.close()
        }
    }
}
