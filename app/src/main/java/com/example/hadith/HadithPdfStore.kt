package com.example.hadith

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
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
import okhttp3.OkHttpClient
import okhttp3.Request
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
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
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val fileLocks = ConcurrentHashMap<String, Mutex>()

    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress: StateFlow<Map<String, Float>> = _progress.asStateFlow()

    private val _changes = MutableStateFlow(0)
    val changes: StateFlow<Int> = _changes.asStateFlow()

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
            PDFBoxResourceLoader.init(appContext)
        }
    }

    fun lastPage(book: HadithBook): Int = prefs().getInt("last_${book.id}", 0)

    fun saveLastPage(book: HadithBook, index: Int) {
        prefs().edit().putInt("last_${book.id}", index)
            .putLong("opened_${book.id}", System.currentTimeMillis()).apply()
    }

    fun lastOpened(book: HadithBook): Long = prefs().getLong("opened_${book.id}", 0L)

    fun isDownloaded(book: HadithBook): Boolean = savedFile(book).let { file ->
        file.isFile && verifiedMarker(file).readTextOrNull() == file.length().toString()
    }

    suspend fun download(book: HadithBook): Result<Unit> = runCatching {
        lockFor(book).withLock {
            if (isDownloaded(book)) return@withLock
            val cached = cachedFile(book)
            val target = savedFile(book)
            if (isRenderablePdf(cached)) {
                withContext(Dispatchers.IO) { copyVerified(cached, target) }
            } else {
                fetch(book, target)
                markVerified(target)
            }
        }
        _changes.update { it + 1 }
    }

    fun delete(book: HadithBook) {
        Documents.close(book.id)
        savedFile(book).delete()
        cachedFile(book).delete()
        verifiedMarker(savedFile(book)).delete()
        File(savedFile(book).path + ".part").delete()
        File(cachedFile(book).path + ".part").delete()
        _changes.update { it + 1 }
    }

    suspend fun open(book: HadithBook): Result<Document> = runCatching {
        val file = localFile(book)
        withContext(Dispatchers.IO) { Documents.open(book, file) }
    }

    /** Returns a validated original PDF, fetching it when needed for external sharing. */
    suspend fun shareFile(book: HadithBook): Result<File> = runCatching { localFile(book) }

    private suspend fun localFile(book: HadithBook): File = lockFor(book).withLock {
        savedFile(book).takeIf { isDownloaded(book) }?.let { return@withLock it }
        val cached = cachedFile(book)
        if (!isRenderablePdf(cached)) fetch(book, cached)
        cached
    }

    /** Resumes a partial official-source download when the server supports byte ranges. */
    private suspend fun fetch(book: HadithBook, target: File) = withContext(Dispatchers.IO) {
        target.parentFile?.mkdirs()
        val part = File(target.path + ".part")
        var lastError: Throwable? = null
        var complete = isRenderablePdf(part)
        _progress.update { it + (book.id to 0f) }
        try {
            for ((sourceIndex, activeUrl) in book.downloadUrls.withIndex()) {
                if (sourceIndex > 0) part.delete()
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

    private fun hasPdfHeader(file: File): Boolean = runCatching {
        file.inputStream().buffered().use { input ->
            val header = ByteArray(5)
            input.read(header) == 5 && header.contentEquals("%PDF-".toByteArray(Charsets.US_ASCII))
        }
    }.getOrDefault(false)

    private fun markVerified(file: File) = verifiedMarker(file).writeText(file.length().toString())

    private fun verifiedMarker(file: File) = File(file.path + ".verified")

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
        val chapters: List<HadithChapter>
    ) {
        val pageCount: Int = renderer.pageCount
        private val lock = Mutex()
        @Volatile private var closed = false
        /** Unique per opened document, so entries in the store-wide [PageCache] never collide. */
        private val cacheId = PageCache.nextId()

        fun cached(index: Int, width: Int): Bitmap? = PageCache.get(cacheId, index, width)

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
            runCatching(renderer::close)
            runCatching(descriptor::close)
        }
    }

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

        @Synchronized
        fun open(book: HadithBook, file: File): Document {
            open[book.id]?.let { (existingFile, document) ->
                if (existingFile == file) return document
                document.close()
            }
            val chapters = extractOutline(file).ifEmpty { book.chapters }
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            return Document(PdfRenderer(descriptor), descriptor, chapters).also { open[book.id] = file to it }
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

        @Synchronized
        fun close(id: String) {
            open.remove(id)?.second?.close()
        }
    }
}
