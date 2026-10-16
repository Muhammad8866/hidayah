package com.example.hadith

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.ProxyFileDescriptorCallback
import android.os.storage.StorageManager
import android.system.ErrnoException
import android.system.OsConstants
import com.example.core.net.Connectivity
import com.example.core.net.FastNet
import com.example.core.net.RangeFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Request
import com.example.core.pdf.PdfPageCache
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Downloads and renders publisher PDFs without altering their bytes. Saved downloads live in app
 * storage; opening an online-only book uses the disposable cache. Every completed file must have a
 * PDF signature and must be accepted by Android's PDF renderer before it can be displayed.
 */
object HadithPdfStore {
    private const val PREFS = "hadith_pdf_reader"
    private const val MAX_ATTEMPTS = 3
    private lateinit var appContext: Context
    private val client = FastNet.client.newBuilder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val fileLocks = ConcurrentHashMap<String, Mutex>()
    private val outlineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress: StateFlow<Map<String, Float>> = _progress.asStateFlow()

    private val _changes = MutableStateFlow(0)
    val changes: StateFlow<Int> = _changes.asStateFlow()

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
            PDFBoxResourceLoader.init(appContext)
            PdfPageCache.init(appContext)
        }
    }

    /** Bumped whenever a reading position or bookmark changes, so library screens can refresh. */
    private val _readingChanges = MutableStateFlow(0)
    val readingChanges: StateFlow<Int> = _readingChanges.asStateFlow()

    fun lastPage(book: HadithBook): Int = prefs().getInt("last_${book.id}", 0)

    fun saveLastPage(book: HadithBook, index: Int) {
        prefs().edit().putInt("last_${book.id}", index)
            .putLong("opened_${book.id}", System.currentTimeMillis()).apply()
        _readingChanges.update { it + 1 }
    }

    /** Zero-based pages the reader marked in [book], in page order. */
    fun bookmarks(book: HadithBook): List<Int> =
        prefs().getString("bookmarks_${book.id}", "").orEmpty()
            .split(',').mapNotNull { it.trim().toIntOrNull() }.filter { it >= 0 }.distinct().sorted()

    /** Adds or removes a bookmark on [page]; returns true when the page is now bookmarked. */
    fun toggleBookmark(book: HadithBook, page: Int): Boolean {
        val current = bookmarks(book)
        val next = if (page in current) current - page else (current + page).sorted()
        prefs().edit().putString("bookmarks_${book.id}", next.joinToString(",")).apply()
        _readingChanges.update { it + 1 }
        return page in next
    }

    fun lastOpened(book: HadithBook): Long = prefs().getLong("opened_${book.id}", 0L)

    fun isDownloaded(book: HadithBook): Boolean = savedFile(book).let { file ->
        file.isFile && verifiedMarker(file).readTextOrNull() == file.length().toString()
    }

    suspend fun download(book: HadithBook): Result<Unit> = pdfResult { withContext(Dispatchers.IO) {
        lockFor(book).withLock {
            if (isDownloaded(book)) return@withLock
            val cached = cachedFile(book)
            val target = savedFile(book)
            if (isVerifiedPdf(cached)) {
                withContext(Dispatchers.IO) { copyVerified(cached, target) }
            } else {
                fetch(book, target)
                markVerified(target)
            }
        }
        _changes.update { it + 1 }
    } }

    fun delete(book: HadithBook) {
        Documents.close(book.id)
        savedFile(book).delete()
        cachedFile(book).delete()
        File(appContext.cacheDir, "pdf_ranges/${book.id}").deleteRecursively()
        verifiedMarker(cachedFile(book)).delete()
        verifiedMarker(savedFile(book)).delete()
        outlineFile(savedFile(book)).delete()
        outlineFile(cachedFile(book)).delete()
        File(savedFile(book).path + ".part").delete()
        File(cachedFile(book).path + ".part").delete()
        _changes.update { it + 1 }
    }

    suspend fun open(book: HadithBook): Result<Document> = pdfResult {
        withContext(Dispatchers.IO) {
            lockFor(book).withLock {
                Documents.existing(book.id)?.let { return@withLock it }
                val saved = savedFile(book)
                val cached = cachedFile(book)
                when {
                    isDownloaded(book) -> Documents.open(book, saved)
                    isVerifiedPdf(cached) -> Documents.open(book, cached)
                    // Nothing local and no connection: fail at once so the reader can explain
                    // and retry on reconnect, instead of waiting out several connect timeouts.
                    !Connectivity.isOnline.value -> throw IOException("Offline")
                    Build.VERSION.SDK_INT >= 26 -> {
                        try { Documents.stream(book) }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { fetch(book, cached); Documents.open(book, cached) }
                    }
                    else -> { fetch(book, cached); Documents.open(book, cached) }
                }
            }
        }
    }

    /** Returns a validated original PDF, fetching it when needed for external sharing. */
    suspend fun shareFile(book: HadithBook): Result<File> = pdfResult { withContext(Dispatchers.IO) { localFile(book) } }

    fun release(book: HadithBook, document: Document) { Documents.release(book.id, document) }

    /** Bookmarks need the full PDF; fetch them only when the reader asks for the index. */
    suspend fun loadChapters(book: HadithBook, document: Document): Result<Unit> = pdfResult {
        if (document.chapters.isEmpty()) {
            withContext(Dispatchers.IO) {
                val file = localFile(book)
                document.chapterState.value = Documents.chapters(file).ifEmpty { book.chapters }
            }
        }
    }

    private suspend fun <T> pdfResult(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancelled: CancellationException) { throw cancelled }
      catch (error: Exception) { Result.failure(error) }

    private suspend fun localFile(book: HadithBook): File = lockFor(book).withLock {
        savedFile(book).takeIf { isDownloaded(book) }?.let { return@withLock it }
        val cached = cachedFile(book)
        if (!isVerifiedPdf(cached)) fetch(book, cached)
        cached
    }

    /** Resumes a partial official-source download when the server supports byte ranges. */
    private suspend fun fetch(book: HadithBook, target: File) = withContext(Dispatchers.IO) {
        if (!Connectivity.isOnline.value) throw IOException("Offline")
        target.parentFile?.mkdirs()
        val part = File(target.path + ".part")
        var lastError: Throwable? = null
        var complete = isRenderablePdf(part)
        _progress.update { it + (book.id to 0f) }
        try {
            for ((sourceIndex, activeUrl) in book.downloadUrls.withIndex()) {
                if (sourceIndex > 0) part.delete()
                // Several connections at once when nothing is saved yet; the resumable
                // single-connection loop below takes over if that does not complete.
                if (!complete && part.length() == 0L) {
                    try {
                        FastNet.download(activeUrl, part) { received, total ->
                            if (total > 0L) _progress.update { it + (book.id to (received.toDouble() / total).toFloat().coerceIn(0f, 1f)) }
                        }
                        complete = isRenderablePdf(part)
                        if (!complete) part.delete()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        lastError = error
                    }
                }
                repeat(MAX_ATTEMPTS) {
                if (complete) return@repeat
                val offset = part.length()
                val request = Request.Builder()
                    .url(activeUrl)
                    .header("Accept", "application/pdf")
                    .header("Accept-Encoding", "identity")
                    .apply { if (offset > 0L) header("Range", "bytes=$offset-") }
                    .build()
                try {
                    client.newCall(request).execute().use { response ->
                        if (response.code == 416) {
                            part.delete()
                            throw IOException("Saved partial PDF could not be resumed")
                        }
                        if (!response.isSuccessful) throw IOException("PDF HTTP ${response.code}")
                        val body = response.body ?: throw IOException("Empty PDF response")
                        // Google Drive answers quota/virus-scan interstitials with 200 + an HTML page.
                        if (body.contentType()?.subtype.equals("html", ignoreCase = true)) {
                            throw IOException("Source returned a web page instead of the PDF")
                        }
                        val resumed = response.code == 206 &&
                            response.header("Content-Range")?.startsWith("bytes $offset-") == true
                        val start = if (resumed) offset else 0L
                        val total = response.header("Content-Range")
                            ?.substringAfterLast('/')?.toLongOrNull()
                            ?: body.contentLength().takeIf { it > 0L }?.plus(start)
                        var received = start
                        val coroutineContext = currentCoroutineContext()
                        body.byteStream().use { input ->
                            FileOutputStream(part, resumed).use { output ->
                                val buffer = ByteArray(1 shl 16)
                                while (true) {
                                    val read = input.read(buffer)
                                    if (read < 0) break
                                    output.write(buffer, 0, read)
                                    received += read
                                    coroutineContext.ensureActive()
                                    val fraction = total?.takeIf { it > 0L }
                                        ?.let { (received.toDouble() / it).toFloat().coerceIn(0f, 1f) }
                                        ?: 0f
                                    _progress.update { it + (book.id to fraction) }
                                }
                            }
                        }
                    }
                    complete = isRenderablePdf(part)
                    if (!complete) lastError = IOException("Downloaded file is incomplete or not a valid PDF")
                } catch (error: IOException) {
                    lastError = error
                }
                // A partial file that does not start as a PDF can never be resumed into one.
                if (!complete && part.length() >= 5L && !hasPdfHeader(part)) part.delete()
                }
                if (complete) break
            }
            if (!complete) throw lastError ?: IOException("Could not download PDF")
            target.delete()
            check(part.renameTo(target)) { "Could not store ${book.romanTitle}" }
            markVerified(target)
        } finally {
            _progress.update { it - book.id }
        }
    }

    private fun copyVerified(source: File, target: File) {
        target.parentFile?.mkdirs()
        val temporary = File(target.path + ".part")
        source.inputStream().use { input -> temporary.outputStream().use(input::copyTo) }
        check(isRenderablePdf(temporary)) { "Cached file is not a complete PDF" }
        target.delete()
        check(temporary.renameTo(target)) { "Could not save PDF" }
        markVerified(target)
    }

    private fun isRenderablePdf(file: File): Boolean {
        if (!file.isFile || file.length() <= 5L) return false
        if (!hasPdfHeader(file)) return false
        return runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer -> renderer.pageCount > 0 }
            }
        }.getOrDefault(false)
    }

    private fun isVerifiedPdf(file: File): Boolean =
        (file.isFile && verifiedMarker(file).readTextOrNull() == file.length().toString()) ||
            isRenderablePdf(file).also { if (it) markVerified(file) }

    private fun hasPdfHeader(file: File): Boolean = runCatching {
        file.inputStream().buffered().use { input ->
            val header = ByteArray(5)
            input.read(header) == 5 && header.contentEquals("%PDF-".toByteArray(Charsets.US_ASCII))
        }
    }.getOrDefault(false)

    private fun markVerified(file: File) = verifiedMarker(file).writeText(file.length().toString())

    private fun verifiedMarker(file: File) = File(file.path + ".verified")

    private fun outlineFile(file: File) = File(file.path + ".outline")

    private fun File.readTextOrNull(): String? = runCatching(File::readText).getOrNull()

    private fun savedFile(book: HadithBook) =
        File(File(appContext.filesDir, "hadith_pdfs"), "${book.languageCode}_${book.id}.pdf")

    private fun cachedFile(book: HadithBook) =
        File(File(appContext.cacheDir, "hadith_pdfs"), "${book.languageCode}_${book.id}.pdf")

    private fun lockFor(book: HadithBook): Mutex = fileLocks.getOrPut(book.id) { Mutex() }

    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    class Document internal constructor(
        private val renderer: PdfRenderer,
        private val descriptor: ParcelFileDescriptor,
        chapters: List<HadithChapter>
    ) {
        internal val chapterState = MutableStateFlow(chapters)
        val chapters: List<HadithChapter> get() = chapterState.value
        val chapterUpdates: StateFlow<List<HadithChapter>> = chapterState.asStateFlow()
        val pageCount: Int = renderer.pageCount
        private val lock = Mutex()
        @Volatile private var closed = false
        /** Unique per opened document, so entries in the app-wide [PdfPageCache] never collide. */
        private val cacheId = PdfPageCache.nextId()

        fun cached(index: Int, width: Int): Bitmap? = PdfPageCache.get(cacheId, index, width)

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
                    }.getOrNull()?.also { PdfPageCache.put(cacheId, index, width, it) }
                }
            }
        }

        internal fun close() {
            closed = true
            outlineScope.launch {
                lock.withLock {
                    PdfPageCache.evict(cacheId)
                    runCatching(renderer::close)
                    runCatching(descriptor::close)
                }
            }
        }
    }

    private object Documents {
        private val open = ConcurrentHashMap<String, Pair<File, Document>>()

        fun existing(id: String): Document? = open[id]?.second
        fun chapters(file: File): List<HadithChapter> = savedOutline(file)

        fun release(id: String, document: Document) {
            val entry = open[id]
            if (entry?.second === document && open.remove(id, entry)) {
                document.close()
            }
        }

        @android.annotation.TargetApi(26)
        fun stream(book: HadithBook): Document {
            val range = RangeFile(book.downloadUrl, File(appContext.cacheDir, "pdf_ranges/${book.id}"))
            val length = range.prepare()
            val thread = HandlerThread("pdf-range-${book.id}").apply { start() }
            var descriptor: ParcelFileDescriptor? = null
            try {
                descriptor = appContext.getSystemService(StorageManager::class.java).openProxyFileDescriptor(
                    ParcelFileDescriptor.MODE_READ_ONLY,
                    object : ProxyFileDescriptorCallback() {
                        override fun onGetSize(): Long = length
                        override fun onRead(offset: Long, size: Int, data: ByteArray): Int = try {
                            range.read(offset, size, data)
                        } catch (_: IOException) { throw ErrnoException("PDF range", OsConstants.EIO) }
                        override fun onRelease() { thread.quitSafely() }
                    }, Handler(thread.looper)
                )
                return Document(PdfRenderer(descriptor), descriptor, book.chapters).also {
                    open[book.id] = File(appContext.cacheDir, "pdf_ranges/${book.id}") to it
                }
            } catch (error: Throwable) {
                descriptor?.close()
                thread.quitSafely()
                throw error
            }
        }

        fun open(book: HadithBook, file: File): Document {
            open[book.id]?.let { (existingFile, document) ->
                if (existingFile == file) return document
                document.close()
            }
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val document = try { Document(PdfRenderer(descriptor), descriptor, book.chapters) }
                catch (error: Throwable) { descriptor.close(); throw error }
            open[book.id] = file to document
            outlineScope.launch {
                document.chapterState.value = savedOutline(file).ifEmpty { book.chapters }
            }
            return document
        }

        /**
         * Parsing a large PDF for its bookmarks takes seconds, so the result is kept beside the
         * file and reused for as long as the file's size is unchanged.
         */
        private fun savedOutline(file: File): List<HadithChapter> {
            val saved = outlineFile(file)
            runCatching {
                val json = JSONObject(saved.readText())
                if (json.getLong("size") == file.length()) {
                    val items = json.getJSONArray("chapters")
                    return List(items.length()) { index ->
                        items.getJSONObject(index).let { HadithChapter(it.getString("title"), it.getInt("page")) }
                    }
                }
            }
            return extractOutline(file).also { chapters ->
                runCatching {
                    val items = JSONArray()
                    chapters.forEach { items.put(JSONObject().put("title", it.title).put("page", it.page)) }
                    saved.writeText(JSONObject().put("size", file.length()).put("chapters", items).toString())
                }
            }
        }

        private fun extractOutline(file: File): List<HadithChapter> = runCatching {
            PDDocument.load(file).use { pdf ->
                val result = mutableListOf<HadithChapter>()
                fun visit(first: PDOutlineItem?, depth: Int) {
                    var item = first
                    while (item != null) {
                        val destination = item.destination ?: (item.action as? PDActionGoTo)?.destination
                        val page = (destination as? PDPageDestination)?.retrievePageNumber()?.plus(1)
                        val title = item.title?.trim().orEmpty()
                        if (page != null && page > 0 && title.isNotBlank()) {
                            result += HadithChapter("${"  ".repeat(depth)}$title", page)
                        }
                        visit(item.firstChild, depth + 1)
                        item = item.nextSibling
                    }
                }
                visit(pdf.documentCatalog.documentOutline?.firstChild, 0)
                result.distinctBy { it.title to it.page }
            }
        }.getOrDefault(emptyList())

        fun close(id: String) {
            open.remove(id)?.second?.close()
        }
    }
}
