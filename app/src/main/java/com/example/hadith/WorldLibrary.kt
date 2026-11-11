package com.example.hadith

import android.content.Context
import android.content.SharedPreferences
import com.example.core.net.FastNet
import com.example.core.search.AppSearch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * One book of the library list; [WorldLibrary.volumes] gives its PDF. Each entry is a single PDF,
 * so it carries its addresses and needs no second request.
 */
data class WorldItem(
    val identifier: String,
    val title: String,
    val creator: String = "",
    val languageCode: String = WorldLibrary.UNKNOWN_LANGUAGE,
    val downloads: Long = 0,
    val nativeTitle: String? = null,
    val pageCount: Int = 0,
    val remoteUrl: String? = null,
    val sourceUrl: String? = null,
    val categories: List<Int> = emptyList()
)

/** A subject of Dawat-e-Islami's library, with how many books of the index it holds. */
data class WorldCategory(val id: Int, val name: String, val count: Int)

/**
 * The local copy of Dawat-e-Islami's library list. [Loading] means there is no list at all yet and
 * one is downloading; while a list is in use and a newer one downloads, the state is [Ready] with
 * [Ready.refreshing] set.
 */
sealed interface DiIndexState {
    object Idle : DiIndexState
    object Loading : DiIndexState
    data class Ready(val count: Int, val updatedAt: Long, val refreshing: Boolean = false) : DiIndexState
    data class Failed(val message: String) : DiIndexState
}

data class WorldPage(val total: Int, val items: List<WorldItem>)

/** How the list is ordered; the whole list is sorted, not only the loaded page. */
enum class WorldSort(val label: String) {
    POPULAR("Most read"), TITLE_ASC("Title A-Z"), TITLE_DESC("Title Z-A"), NEWEST("Newest")
}

/**
 * Books by Ahle Sunnat wal Jamaat scholars in every language, all from one source: the
 * dawateislami.net book library. Its whole list is fetched in one answer, kept on the phone as
 * [DawateIslamiIndex] and refreshed weekly, because its paged search skips books. A book removed
 * there disappears here at the next refresh, with any saved copy. No other site is used; saved
 * books of earlier versions from other sites are deleted once by [LibraryMigration].
 */
object WorldLibrary {
    const val PAGE_SIZE = 50
    const val UNKNOWN_LANGUAGE = "xx"
    private const val PREFS = "world_library"

    class Language(val code: String, val name: String, vararg val terms: String)

    /** Names in their own script for the languages that have one here; the rest keep the library's name. */
    private val nativeNames = mapOf(
        "ar" to "العربية", "ur" to "اردو", "en" to "English", "hi" to "हिन्दी",
        "bn" to "বাংলা", "gu" to "ગુજરાતી", "fa" to "فارسی", "tr" to "Türkçe",
        "id" to "Indonesia", "ms" to "Melayu", "ta" to "தமிழ்", "te" to "తెలుగు",
        "ml" to "മലയാളം", "kn" to "ಕನ್ನಡ", "mr" to "मराठी", "pa" to "ਪੰਜਾਬੀ",
        "sd" to "سنڌي", "ps" to "پښتو", "fr" to "Français", "de" to "Deutsch",
        "es" to "Español", "ru" to "Русский", "so" to "Soomaali", "sw" to "Kiswahili",
        "ha" to "Hausa", "zh" to "中文"
    )

    /**
     * The library's languages under app (ISO) codes, most books first, named in their own script
     * where [nativeNames] has one. [Language.terms] holds the library's own code.
     */
    val languages: List<Language> = DawateIslamiIndex.languages.map { (libraryCode, name) ->
        val code = DawateIslamiIndex.appLanguage(libraryCode)
        Language(code, nativeNames[code] ?: name, libraryCode)
    }

    fun languageName(code: String): String? = languages.firstOrNull { it.code == code }?.name

    private suspend fun <T> request(block: () -> T): Result<T> = try {
        Result.success(withContext(Dispatchers.IO) { block() })
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }

    /** The books of [languageCode] and [categoryId] that match [query], one page at a time (see [searchDi]). */
    suspend fun search(
        query: String, languageCode: String, page: Int, sort: WorldSort = WorldSort.POPULAR, categoryId: Int? = null
    ): Result<WorldPage> = try {
        searchDi(query, languageCode, page, sort, categoryId)
    } catch (cancelled: CancellationException) { throw cancelled }
      catch (error: Exception) { Result.failure(error) }

    /**
     * Searches the local list. It waits for the network only when there is no list at all; an old
     * list is searched while a newer one downloads. The list is kept sorted, so no sort runs here.
     */
    private suspend fun searchDi(
        query: String, languageCode: String, page: Int, sort: WorldSort, categoryId: Int?
    ): Result<WorldPage> {
        refreshDiIndex()
        val index = diIndex ?: return Result.failure(
            (diIndexState.value as? DiIndexState.Failed)?.let { IOException(it.message) } ?: IOException("Library not loaded")
        )
        return request {
            val found = index.search(query, languageCode, categoryId, sort)
            WorldPage(found.size, DawateIslamiIndex.page(found, page, PAGE_SIZE).map(DawateIslamiIndex::toItem))
        }
    }

    /** The one PDF of [item], as the list of volumes the book page shows. */
    suspend fun volumes(item: WorldItem): Result<List<HadithBook>?> = Result.success(listOf(diBook(item)))

    /** The one PDF of a library entry. Its slug is the entry's identifier ("di:n"). */
    internal fun diBook(item: WorldItem): HadithBook {
        val number = item.identifier.removePrefix("di:")
        return HadithBook(
            id = "di-$number",
            collectionId = "wl-${item.identifier}",
            collectionName = item.title,
            romanTitle = item.title,
            nativeTitle = item.nativeTitle,
            languageCode = item.languageCode,
            languageName = languageName(item.languageCode) ?: "",
            pageCount = item.pageCount,
            fileId = number.toLongOrNull()?.let { diIndex?.byId?.get(it)?.fileId?.toInt() } ?: 0,
            slug = item.identifier,
            volume = 1,
            volumeCount = 1,
            remoteUrl = item.remoteUrl,
            sourceUrl = item.sourceUrl ?: DawateIslamiIndex.LIBRARY_URL,
            mirrored = false
        )
    }

    // Dawat-e-Islami's library list, kept on the phone.

    /** The list shipped with the app, in the cache's format; it is used until one has been downloaded. */
    private const val SEED_ASSET = "world_library/di-index.json"
    /** A background refresh that failed is not tried again before this; a pull to refresh always is. */
    private const val RETRY_AFTER_FAILURE_MS = 15L * 60 * 1000

    @Volatile private var diIndex: DiIndex? = null
    private val diLock = Mutex()
    private val _diIndexState = MutableStateFlow<DiIndexState>(DiIndexState.Idle)
    val diIndexState: StateFlow<DiIndexState> = _diIndexState.asStateFlow()

    /** The download, the shipped copy, the clock and the saved file; tests replace them. */
    internal var diFetcher: suspend () -> DiIndex = { fetchDiIndex() }
    internal var diSeed: () -> InputStream? = { openSeed() }
    internal var diClock: () -> Long = { System.currentTimeMillis() }
    internal var diCachePath: () -> File? = { diCacheFile() }
    /** When the last background refresh failed, or null after a success. */
    @Volatile private var diFailedAt: Long? = null

    /** The library's subjects in the loaded index, largest first; empty until the index is loaded. */
    fun diCategories(): List<WorldCategory> = diIndex?.categories.orEmpty()

    /**
     * Whether the loaded list has the book numbered [diId]; true while no list is loaded, so the
     * curated shelves show before the list arrives.
     */
    fun isListed(diId: Long): Boolean = diIndex?.byId?.containsKey(diId) ?: true

    private fun diCacheFile(): File? = if (::appContext.isInitialized)
        File(File(appContext.filesDir, "world_library").apply { mkdirs() }, "di-index.json") else null

    private fun openSeed(): InputStream? =
        if (::appContext.isInitialized) runCatching { appContext.assets.open(SEED_ASSET) }.getOrNull() else null

    /** The download runs here, not in a caller's scope: a search that is cancelled does not restart it. */
    private val diScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val diJobLock = Any()
    private var diJob: Deferred<Result<Int>>? = null
    private var diJobForced = false

    /**
     * Makes sure a list is in memory: the saved one, else the copy shipped with the app. Only when
     * there is neither does this wait for the download. A list a week old stays in use while a new
     * one downloads behind it, so searching never waits on the library's slow server.
     * [force] (a pull to refresh) waits for a new download, after any normal one already running,
     * and reports whether it worked. A failed download keeps the older list in use.
     */
    suspend fun refreshDiIndex(force: Boolean = false): Result<Int> {
        val current = localDiIndex()
        if (force || current == null) return diDownload(force).await()
        if (isStale(current)) refreshInBackground()
        return Result.success(current.books.size)
    }

    /**
     * Saved books are dropped only when the new list is not much shorter than the last one, so a
     * list that lost most of its books by mistake cannot delete everyone's downloads.
     */
    internal fun shouldDropMissingDi(previousCount: Int?, freshCount: Int): Boolean =
        previousCount == null || freshCount.toLong() * 10 >= previousCount.toLong() * 9

    /** A week old, or dated in the future by a clock that was wrong when it was saved. */
    private fun isStale(index: DiIndex): Boolean = diClock() - index.updatedAt !in 0 until DawateIslamiIndex.MAX_AGE_MS

    /** The list in memory, else the saved one, else the shipped copy. Never the network. */
    private suspend fun localDiIndex(): DiIndex? = diIndex ?: diLock.withLock {
        diIndex ?: withContext(Dispatchers.IO) { readLocalDiIndex() }?.also { loaded ->
            diIndex = loaded
            _diIndexState.value = DiIndexState.Ready(loaded.books.size, loaded.updatedAt)
            warmSearch(loaded)
        }
    }

    private fun readLocalDiIndex(): DiIndex? =
        diCachePath()?.let { DawateIslamiIndex.readCache(it) }
            ?: runCatching { diSeed() }.getOrNull()?.let { DawateIslamiIndex.readCache(it) }

    /** One background download, unless one is running or the last one failed only minutes ago. */
    private fun refreshInBackground() {
        val start = synchronized(diJobLock) {
            val failedAt = diFailedAt
            diJob?.isActive != true && (failedAt == null || diClock() - failedAt !in 0 until RETRY_AFTER_FAILURE_MS)
        }
        if (start) diDownload(force = false)
    }

    /** Callers share one download in flight; a forced one waits for a running normal one, then fetches. */
    private fun diDownload(force: Boolean): Deferred<Result<Int>> = synchronized(diJobLock) {
        val running = diJob?.takeIf { it.isActive }
        if (running != null && (diJobForced || !force)) running
        else diScope.async { running?.join(); fetchDiList(force) }.also { diJob = it; diJobForced = force }
    }

    /**
     * Downloads the whole list and saves it. Only a downloaded list replaces the saved file or
     * removes saved books, never the saved or shipped copy it replaces.
     */
    private suspend fun fetchDiList(force: Boolean): Result<Int> = try {
        val previous = localDiIndex()
        _diIndexState.value = previous?.let { DiIndexState.Ready(it.books.size, it.updatedAt, refreshing = true) }
            ?: DiIndexState.Loading
        val fresh = diFetcher()
        withContext(Dispatchers.IO) { diCachePath()?.let { DawateIslamiIndex.writeCache(it, fresh) } }
        diIndex = fresh
        diFailedAt = null
        _diIndexState.value = DiIndexState.Ready(fresh.books.size, fresh.updatedAt)
        warmSearch(fresh)
        if (shouldDropMissingDi(previous?.books?.size, fresh.books.size)) withContext(Dispatchers.IO) { dropMissingDi(fresh) }
        Result.success(fresh.books.size)
    } catch (cancelled: CancellationException) {
        _diIndexState.value = diIndex?.let { DiIndexState.Ready(it.books.size, it.updatedAt) } ?: DiIndexState.Idle
        throw cancelled
    } catch (error: Exception) {
        if (!force) diFailedAt = diClock()
        val older = diIndex
        _diIndexState.value = if (older != null) DiIndexState.Ready(older.books.size, older.updatedAt)
            else DiIndexState.Failed(error.message ?: "Could not load the library")
        Result.failure(error)
    }

    /** Folds every title once in the background, so the first typed search does not have to. */
    private fun warmSearch(index: DiIndex) {
        diScope.launch { index.warmSearchText() }
    }

    /** Forgets the list, any download and the back-off, and puts back the real download and files. */
    internal fun resetDiForTest() {
        val job = synchronized(diJobLock) {
            diJob.also { diJob = null; diJobForced = false }
        }
        // Waited for, so a cancelled download cannot write its state into the next test.
        job?.let { runBlocking { it.cancelAndJoin() } }
        diIndex = null
        diFailedAt = null
        diFetcher = { fetchDiIndex() }
        diSeed = { openSeed() }
        diClock = { System.currentTimeMillis() }
        diCachePath = { diCacheFile() }
        _diIndexState.value = DiIndexState.Idle
    }

    /** One POST for every book, read as it streams in; cancelling the coroutine cancels the call. */
    private suspend fun fetchDiIndex(): DiIndex = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder().url(DawateIslamiIndex.SEARCH_API)
            .header("Accept", "application/json")
            .post(DawateIslamiIndex.FULL_REQUEST.toRequestBody("application/json".toMediaType()))
            .build()
        val call = FastNet.client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { continuation.resumeWithException(e) }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val index = response.use {
                        if (!it.isSuccessful) throw IOException("HTTP ${it.code}")
                        val body = it.body ?: throw IOException("Empty response")
                        if (body.contentLength() > DawateIslamiIndex.MAX_BYTES) throw IOException("Response too large")
                        DawateIslamiIndex.parseResponse(body.byteStream(), now = System.currentTimeMillis())
                    }
                    continuation.resume(index)
                } catch (error: Exception) { continuation.resumeWithException(error) }
            }
        })
    }

    /** After a complete new list, saved Dawat-e-Islami books the library no longer has are deleted. */
    private fun dropMissingDi(index: DiIndex) {
        if (!::appContext.isInitialized) return
        _saved.value.filter { it.collectionId.startsWith("wl-di:") }
            .filter { book -> book.id.removePrefix("di-").toLongOrNull()?.let { it !in index.byId } ?: false }
            .forEach(::delete)
    }

    // Offline copies of library books, shelf books included; what was saved is recorded here.

    private lateinit var appContext: Context
    private val _saved = MutableStateFlow<List<HadithBook>>(emptyList())
    val saved: StateFlow<List<HadithBook>> = _saved.asStateFlow()

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        // Moves Hadith books that now live in this library into its saved list before it is read,
        // then moves the curated shelves to the library's own ids and deletes saved books of other
        // sites. A disk error leaves a move for the next open instead of breaking the library.
        runCatching { HadithMoves.migrate(appContext) }
        runCatching { LibraryMigration.migrate(appContext) }
        HadithPdfStore.init(context)
        _saved.value = readSaved()
        _recent.value = runCatching {
            val entries = JSONArray(prefs().getString(RECENT_KEY, "[]"))
            List(entries.length()) { entries.getString(it) }.filter(String::isNotBlank).take(RECENT_LIMIT)
        }.getOrDefault(emptyList())
    }

    // Search preferences, kept on this device only: the last language and order, and recent searches.

    private const val RECENT_KEY = "recent_searches"
    private const val RECENT_LIMIT = 6
    private val _recent = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recent.asStateFlow()

    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** [query] first and tidied, without a case-insensitive duplicate, at most [limit] entries. */
    fun withRecent(recent: List<String>, query: String, limit: Int = RECENT_LIMIT): List<String> {
        val text = query.trim().replace(Regex("\\s+"), " ")
        if (text.isEmpty()) return recent
        return (listOf(text) + recent.filterNot { it.equals(text, ignoreCase = true) }).take(limit)
    }

    fun rememberSearch(query: String) {
        val next = withRecent(_recent.value, query)
        if (next == _recent.value) return
        _recent.value = next
        prefs().edit().putString(RECENT_KEY, JSONArray(next).toString()).apply()
    }

    fun clearRecentSearches() {
        _recent.value = emptyList()
        prefs().edit().remove(RECENT_KEY).apply()
    }

    /** The language last chosen, or every language; an unknown stored code falls back to every language. */
    fun preferredLanguage(): String = prefs().getString("language", null)
        ?.takeIf { code -> code == HadithCatalog.ALL_LANGUAGES || languageName(code) != null }
        ?: HadithCatalog.ALL_LANGUAGES

    /**
     * How many saved books from other sites the one-time move deleted, so the screen can say so
     * once; 0 afterwards, and before [init].
     */
    fun takeRemovedCount(): Int = if (::appContext.isInitialized) takeRemovedCount(prefs()) else 0

    internal fun takeRemovedCount(prefs: SharedPreferences): Int {
        if (!prefs.contains(LibraryMigration.REMOVED_KEY)) return 0
        val count = runCatching { prefs.getInt(LibraryMigration.REMOVED_KEY, 0) }.getOrDefault(0)
        prefs.edit().remove(LibraryMigration.REMOVED_KEY).commit()
        return count.coerceAtLeast(0)
    }

    /** The library subject last chosen, or null for every subject. */
    fun preferredCategory(): Int? = prefs().getInt("category", -1).takeIf { it >= 0 }

    fun setPreferredCategory(id: Int?) {
        val editor = prefs().edit()
        if (id == null) editor.remove("category") else editor.putInt("category", id)
        editor.apply()
    }

    fun setPreferredLanguage(code: String) = prefs().edit().putString("language", code).apply()

    fun preferredSort(): WorldSort = prefs().getString("sort", null)
        ?.let { name -> WorldSort.entries.firstOrNull { it.name == name } } ?: WorldSort.POPULAR

    fun setPreferredSort(sort: WorldSort) = prefs().edit().putString("sort", sort.name).apply()

    /**
     * Saved books that match [query] by title, script title, book, volume or language, best match
     * first, with the app's shared search (any spelling or script, a typo, a half-typed word), so
     * downloads stay searchable offline. A query with no searchable word keeps [saved] as it is.
     */
    fun matchSaved(saved: List<HadithBook>, query: String): List<HadithBook> = AppSearch.rank(query, saved) { book ->
        AppSearch.Fields(
            title = book.romanTitle,
            nativeTitle = book.nativeTitle.orEmpty(),
            other = listOfNotNull(book.collectionName, book.volumeTitle, book.languageName).filter(String::isNotBlank)
        )
    }

    private fun savedFile() = File(File(appContext.filesDir, "world_library").apply { mkdirs() }, "saved.json")

    private fun readSaved(): List<HadithBook> = runCatching { parseSaved(savedFile().readText()) }.getOrDefault(emptyList())

    private val savedId = Regex("di-[0-9]{1,12}")
    private val savedLanguage = Regex("[a-z]{2,3}")

    /**
     * The saved list, one entry at a time. Only library books are read; an entry of another site,
     * as earlier versions wrote, is skipped. Ids and language codes name files on disk, so an entry
     * whose id or language is not one this app writes is skipped instead of reaching a delete.
     */
    internal fun parseSaved(text: String): List<HadithBook> {
        val entries = JSONArray(text)
        return (0 until entries.length()).mapNotNull { index ->
            runCatching { entries.getJSONObject(index).takeIf(::isLibraryEntry)?.let(::savedBook) }.getOrNull()
                ?.takeIf { savedId.matches(it.id) && savedLanguage.matches(it.languageCode) }
        }
    }

    /** Whether a saved.json entry is a book of the library ("source": "dawateislami"). */
    internal fun isLibraryEntry(entry: JSONObject): Boolean = entry.optString("source") == DI_SOURCE

    internal const val DI_SOURCE = "dawateislami"

    /** One saved.json entry: the book's id, titles, volume, addresses, pages and library file number. */
    internal fun savedEntry(book: HadithBook): JSONObject {
        val entry = JSONObject().put("id", book.id).put("identifier", book.slug)
            .put("collection", book.collectionName).put("title", book.romanTitle)
            .put("language", book.languageCode).put("volume", book.volume)
            .put("volumeCount", book.volumeCount).put("url", book.downloadUrl)
            .put("volumeTitle", book.volumeTitle.orEmpty())
            .put("source", DI_SOURCE).put("sourceUrl", book.sourcePageUrl).put("pageCount", book.pageCount)
            .put("fileId", book.fileId)
        book.nativeTitle?.let { entry.put("nativeTitle", it) }
        return entry
    }

    /** A library book from its saved.json entry; [parseSaved] reads only library entries. */
    internal fun savedBook(entry: JSONObject): HadithBook {
        val identifier = entry.getString("identifier")
        val language = entry.getString("language")
        return HadithBook(
            id = entry.getString("id"),
            collectionId = "wl-$identifier",
            collectionName = entry.getString("collection"),
            romanTitle = entry.getString("title"),
            nativeTitle = entry.optString("nativeTitle").takeIf(String::isNotBlank),
            languageCode = language,
            languageName = languageName(language) ?: "",
            pageCount = entry.optInt("pageCount"),
            fileId = entry.optInt("fileId"),
            slug = identifier,
            volume = entry.getInt("volume"),
            volumeCount = entry.getInt("volumeCount"),
            remoteUrl = entry.getString("url"),
            sourceUrl = entry.optString("sourceUrl").takeIf(String::isNotBlank) ?: DawateIslamiIndex.LIBRARY_URL,
            volumeTitle = entry.optString("volumeTitle").takeIf(String::isNotBlank),
            mirrored = false
        )
    }

    @Synchronized
    private fun store(books: List<HadithBook>) {
        val entries = JSONArray()
        books.forEach { book -> entries.put(savedEntry(book)) }
        savedFile().writeText(entries.toString())
        _saved.value = books
    }

    internal fun setSavedForTest(books: List<HadithBook>) = store(books)

    suspend fun download(book: HadithBook): Result<Unit> = HadithPdfStore.download(book).onSuccess {
        store(_saved.value.filterNot { it.id == book.id } + book)
    }

    fun delete(book: HadithBook) {
        store(_saved.value.filterNot { it.id == book.id })
        HadithPdfStore.delete(book)
    }
}
