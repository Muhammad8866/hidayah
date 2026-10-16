package com.example.hadith

import android.content.Context
import com.example.core.net.FastNet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.security.MessageDigest

/** One book entry at the source; its PDFs are listed by [WorldLibrary.volumes]. */
data class WorldItem(
    val identifier: String,
    val title: String,
    val creator: String = "",
    val languageCode: String = WorldLibrary.UNKNOWN_LANGUAGE,
    val downloads: Long = 0
)

data class WorldPage(val total: Int, val items: List<WorldItem>)

/** [order] is the source's own sort field, so a sort covers every book and not only the loaded page. */
enum class WorldSort(val label: String, val order: String) {
    POPULAR("Most read", "downloads+desc"), TITLE_ASC("Title A-Z", "titleSorter+asc"),
    TITLE_DESC("Title Z-A", "titleSorter+desc"), NEWEST("Newest", "addeddate+desc")
}

/**
 * Books by Ahle Sunnat wal Jamaat scholars in every language, read live from the Internet Archive.
 * No book is listed in the app itself: every screen is a fresh answer from the source limited to
 * [WorldLibrary.authors], so a book added there appears here and a book removed there disappears,
 * including any copy saved for offline reading.
 */
object WorldLibrary {
    const val PAGE_SIZE = 50
    const val UNKNOWN_LANGUAGE = "xx"
    const val SOURCE_URL = "https://archive.org/"

    private const val SEARCH = "https://archive.org/advancedsearch.php"
    private const val METADATA = "https://archive.org/metadata/"
    private const val SYNC_INTERVAL_MS = 24L * 60 * 60 * 1000
    private const val PREFS = "world_library"
    private val responseCache = ConcurrentHashMap<String, Pair<Long, String>>()
    private val responseLocks = Array(16) { Mutex() }

    /** Brief reuse for back navigation; refreshes from source after one minute. */
    private suspend fun response(url: String): String = responseLocks[(url.hashCode() and Int.MAX_VALUE) % responseLocks.size].withLock {
        val now = System.currentTimeMillis()
        responseCache[url]?.takeIf { now - it.first < 60_000 }?.let { return@withLock it.second }
        val text = FastNet.textAsync(url)
        // Only successful, parseable source responses are cached.
        withContext(Dispatchers.IO) { JSONObject(text) }
        responseCache.entries.removeAll { now - it.value.first >= 60_000 }
        if (responseCache.size >= 32) responseCache.clear()
        responseCache[url] = now to text
        text
    }

    class Language(val code: String, val name: String, vararg val terms: String)

    /** [Language.terms] are the spellings uploaders use in the source's language field. */
    val languages = listOf(
        Language("ar", "العربية", "arabic", "ara"),
        Language("ur", "اردو", "urdu", "urd"),
        Language("en", "English", "english", "eng"),
        Language("hi", "हिन्दी", "hindi", "hin"),
        Language("bn", "বাংলা", "bengali", "bangla", "ben"),
        Language("gu", "ગુજરાતી", "gujarati", "guj"),
        Language("fa", "فارسی", "persian", "farsi", "per", "fas"),
        Language("tr", "Türkçe", "turkish", "tur"),
        Language("id", "Indonesia", "indonesian", "ind"),
        Language("ms", "Melayu", "malay", "may", "msa"),
        Language("ta", "தமிழ்", "tamil", "tam"),
        Language("te", "తెలుగు", "telugu", "tel"),
        Language("ml", "മലയാളം", "malayalam", "mal"),
        Language("kn", "ಕನ್ನಡ", "kannada", "kan"),
        Language("mr", "मराठी", "marathi", "mar"),
        Language("pa", "ਪੰਜਾਬੀ", "punjabi", "panjabi", "pan"),
        Language("sd", "سنڌي", "sindhi", "snd"),
        Language("ps", "پښتو", "pashto", "pushto", "pus"),
        Language("fr", "Français", "french", "fre", "fra"),
        Language("de", "Deutsch", "german", "ger", "deu"),
        Language("es", "Español", "spanish", "spa"),
        Language("ru", "Русский", "russian", "rus"),
        Language("so", "Soomaali", "somali", "som"),
        Language("sw", "Kiswahili", "swahili", "swa"),
        Language("ha", "Hausa", "hausa", "hau"),
        Language("zh", "中文", "chinese", "chi", "zho")
    )

    fun languageName(code: String): String? = languages.firstOrNull { it.code == code }?.name

    /**
     * The only authors and publishers whose books are shown, in the spellings uploaders use.
     * Full names only: a bare "Ahmad Raza" or "Nabhani" also matches scholars of other schools.
     * Classical authors are left out because their books circulate in editions annotated by anyone.
     */
    val authors = listOf(
        "ahmad raza khan", "ahmed raza khan", "ahmad rida khan", "imam ahmad raza", "imam ahmed raza",
        "imam ahmad rida", "ala hazrat", "aala hazrat", "alahazrat",
        "naqi ali khan", "hamid raza khan", "mustafa raza khan", "akhtar raza khan", "akhtar rida khan",
        "dawat e islami", "dawat-e-islami", "dawateislami", "maktaba tul madina", "maktabatul madina",
        "maktaba-tul-madina", "al madinatul ilmiyah", "ilyas qadri", "ilyas attar qadri",
        "amjad ali azmi", "amjad ali aazmi", "ahmad yar khan naeemi", "ahmed yar khan naeemi",
        "ahmad yar khan naimi", "mufti ahmad yar khan", "naeemuddin muradabadi", "naeem ud din muradabadi",
        "jalaluddin amjadi", "jalal ud din amjadi", "jalaluddin ahmad amjadi",
        "abdul mustafa azmi", "abdul mustafa aazmi", "sharif ul haq amjadi", "shariful haq amjadi",
        "zafaruddin bihari", "zafar uddin bihari", "ahmad saeed kazmi", "ahmed saeed kazmi",
        "faiz ahmad owaisi", "faiz ahmed owaisi", "faiz ahmad uwaisi",
        "ghulam rasool saeedi", "ghulam rasul saeedi", "karam shah al azhari", "pir karam shah",
        "shafi okarvi", "shafee okarvi", "kaukab noorani", "abdul hakim sharaf qadri",
        "arshadul qadri", "arshad ul qadri", "fazl e haq khairabadi", "fazle haq khairabadi",
        "abdul haq muhaddith dehlvi", "abdul haq muhaddis dehlvi", "yusuf nabhani", "yusuf al nabhani",
        "khalil khan barkati", "madni miyan", "hashmi miyan",
        "sunni dawate islami", "sunni dawat e islami", "shakir noorie",
        "alahazrat network", "alahazrat.net", "raza academy", "i-sunni.blogspot.com"
    )

    private val luceneSpecial = Regex("""[+\-&|!(){}\[\]^"~*?:\\/]""")

    fun searchQuery(query: String, languageCode: String): String = buildString {
        append("mediatype:texts AND format:PDF AND creator:(")
        append(authors.joinToString(" OR ") { "\"$it\"" })
        append(") AND NOT access-restricted-item:true")
        languages.firstOrNull { it.code == languageCode }?.let {
            append(" AND language:(${it.terms.joinToString(" OR ")})")
        }
        val words = query.replace(luceneSpecial, " ").trim().split(Regex("\\s+")).filter(String::isNotBlank)
        if (words.isNotEmpty()) {
            val needle = words.joinToString(" ")
            append(" AND (title:($needle) OR creator:($needle))")
        }
    }

    fun searchUrl(query: String, languageCode: String, page: Int, sort: WorldSort = WorldSort.POPULAR): String {
        val fields = listOf("identifier", "title", "creator", "language", "downloads")
            .joinToString("") { "&fl%5B%5D=$it" }
        return "$SEARCH?q=${encode(searchQuery(query, languageCode))}$fields" +
            "&rows=$PAGE_SIZE&page=$page&output=json&sort%5B%5D=${sort.order}"
    }

    fun parseSearch(json: String): WorldPage {
        val response = JSONObject(json).getJSONObject("response")
        val docs = response.getJSONArray("docs")
        val items = (0 until docs.length()).mapNotNull { index ->
            val doc = docs.getJSONObject(index)
            val identifier = doc.optString("identifier").takeIf(String::isNotBlank) ?: return@mapNotNull null
            WorldItem(
                identifier = identifier,
                title = BookTitles.clean(first(doc.opt("title")).ifBlank { identifier }),
                creator = first(doc.opt("creator")),
                languageCode = languageCode(first(doc.opt("language"))),
                downloads = doc.optLong("downloads")
            )
        }
        return WorldPage(response.optInt("numFound"), items)
    }

    /**
     * The source answers `{}` for an item that was deleted and flags one that was taken down.
     * Any other answer without a file list is an error, not a removal, so nothing is deleted on it.
     */
    fun isRemoved(metadata: JSONObject): Boolean = when {
        metadata.length() == 0 || metadata.optBoolean("is_dark") -> true
        metadata.optJSONArray("files") == null -> throw IOException("Unexpected answer from the source")
        else -> false
    }

    /** Every readable PDF of [item], or null when the source no longer has the item. */
    fun parseVolumes(item: WorldItem, metadataJson: String): List<HadithBook>? {
        val metadata = JSONObject(metadataJson)
        if (isRemoved(metadata)) return null
        // Natural order, so volume 2 comes before volume 10 however the files are numbered.
        val files = pdfFiles(metadata).sortedWith(compareBy(BookTitles.naturalOrder) { it.first })
        return files.mapIndexed { index, (name, size) ->
            val label = BookTitles.fileLabel(item.title, name)
            HadithBook(
                id = bookId(item.identifier, name),
                collectionId = "wl-${item.identifier}",
                collectionName = item.title,
                romanTitle = if (files.size == 1) item.title else "${item.title} — $label",
                languageCode = item.languageCode,
                languageName = languageName(item.languageCode) ?: "",
                pageCount = 0,
                fileId = 0,
                slug = item.identifier,
                volume = index + 1,
                volumeCount = files.size,
                remoteUrl = fileUrl(item.identifier, name),
                sourceUrl = "https://archive.org/details/${item.identifier}",
                volumeTitle = if (size > 0L) "$label · ${sizeLabel(size)}" else label,
                mirrored = false
            )
        }
    }

    /** The scanned or uploaded book itself; the source's extra text-layer copy is skipped. */
    private fun pdfFiles(metadata: JSONObject): List<Pair<String, Long>> {
        val files = metadata.optJSONArray("files") ?: return emptyList()
        return (0 until files.length()).mapNotNull { index ->
            val file = files.getJSONObject(index)
            val name = file.optString("name")
            if (!name.endsWith(".pdf", ignoreCase = true)) return@mapNotNull null
            if (file.optString("format") == "Additional Text PDF" || file.optString("private") == "true") return@mapNotNull null
            name to (file.optString("size").toLongOrNull() ?: 0L)
        }
    }

    fun bookId(identifier: String, fileName: String): String =
        "wl-" + MessageDigest.getInstance("MD5").digest("$identifier/$fileName".toByteArray())
            .take(8).joinToString("") { "%02x".format(it) }

    fun fileUrl(identifier: String, fileName: String): String =
        "https://archive.org/download/${encode(identifier)}/" + fileName.split('/').joinToString("/", transform = ::encode)

    private fun encode(text: String) = URLEncoder.encode(text, "UTF-8").replace("+", "%20")

    private fun sizeLabel(bytes: Long): String =
        if (bytes >= 1 shl 20) "${bytes shr 20} MB" else "${(bytes shr 10).coerceAtLeast(1)} KB"

    private fun first(value: Any?): String = when (value) {
        is JSONArray -> if (value.length() > 0) value.optString(0) else ""
        null, JSONObject.NULL -> ""
        else -> value.toString()
    }

    private fun languageCode(raw: String): String {
        val name = raw.trim().lowercase()
        return languages.firstOrNull { name in it.terms || name == it.code }?.code ?: UNKNOWN_LANGUAGE
    }

    private suspend fun <T> request(block: () -> T): Result<T> = try {
        Result.success(withContext(Dispatchers.IO) { block() })
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }

    suspend fun search(
        query: String, languageCode: String, page: Int, sort: WorldSort = WorldSort.POPULAR
    ): Result<WorldPage> = try {
        val json = response(searchUrl(query, languageCode, page, sort))
        request { parseSearch(json) }
    } catch (cancelled: CancellationException) { throw cancelled }
      catch (error: Exception) { Result.failure(error) }

    /** Null inside a success means the source removed the item; saved copies are deleted with it. */
    suspend fun volumes(item: WorldItem): Result<List<HadithBook>?> = try {
        val metadata = response(METADATA + encode(item.identifier))
        request {
            parseVolumes(item, metadata).also { volumes ->
                dropMissing(item.identifier, volumes?.map { it.id }?.toSet().orEmpty())
            }
        }
    } catch (cancelled: CancellationException) { throw cancelled }
      catch (error: Exception) { Result.failure(error) }

    // Offline copies. The books are not in any catalog, so what was saved is recorded here.

    private lateinit var appContext: Context
    private val _saved = MutableStateFlow<List<HadithBook>>(emptyList())
    val saved: StateFlow<List<HadithBook>> = _saved.asStateFlow()

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
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
        ?.takeIf { code -> code == HadithCatalog.ALL_LANGUAGES || languages.any { it.code == code } }
        ?: HadithCatalog.ALL_LANGUAGES

    fun setPreferredLanguage(code: String) = prefs().edit().putString("language", code).apply()

    fun preferredSort(): WorldSort = prefs().getString("sort", null)
        ?.let { name -> WorldSort.entries.firstOrNull { it.name == name } } ?: WorldSort.POPULAR

    fun setPreferredSort(sort: WorldSort) = prefs().edit().putString("sort", sort.name).apply()

    /**
     * Saved volumes whose book, volume or language contains every word of [query], so downloads
     * stay searchable offline. A blank query matches everything.
     */
    fun matchSaved(saved: List<HadithBook>, query: String): List<HadithBook> {
        val words = query.lowercase().split(Regex("\\s+")).filter(String::isNotBlank)
        if (words.isEmpty()) return saved
        return saved.filter { book ->
            val text = listOf(book.collectionName, book.romanTitle, book.volumeTitle.orEmpty(), book.languageName)
                .joinToString(" ").lowercase()
            words.all { it in text }
        }
    }

    private fun savedFile() = File(File(appContext.filesDir, "world_library").apply { mkdirs() }, "saved.json")

    private fun readSaved(): List<HadithBook> = runCatching {
        val entries = JSONArray(savedFile().readText())
        List(entries.length()) { index ->
            val entry = entries.getJSONObject(index)
            HadithBook(
                id = entry.getString("id"),
                collectionId = "wl-${entry.getString("identifier")}",
                // Cleaned again on reading, so books saved by an older version get tidy titles too.
                collectionName = BookTitles.clean(entry.getString("collection")),
                romanTitle = BookTitles.clean(entry.getString("title")),
                languageCode = entry.getString("language"),
                languageName = languageName(entry.getString("language")) ?: "",
                pageCount = 0,
                fileId = 0,
                slug = entry.getString("identifier"),
                volume = entry.getInt("volume"),
                volumeCount = entry.getInt("volumeCount"),
                remoteUrl = entry.getString("url"),
                sourceUrl = "https://archive.org/details/${entry.getString("identifier")}",
                volumeTitle = entry.optString("volumeTitle").takeIf(String::isNotBlank),
                mirrored = false
            )
        }
    }.getOrDefault(emptyList())

    @Synchronized
    private fun store(books: List<HadithBook>) {
        val entries = JSONArray()
        books.forEach { book ->
            entries.put(
                JSONObject().put("id", book.id).put("identifier", book.slug)
                    .put("collection", book.collectionName).put("title", book.romanTitle)
                    .put("language", book.languageCode).put("volume", book.volume)
                    .put("volumeCount", book.volumeCount).put("url", book.downloadUrl)
                    .put("volumeTitle", book.volumeTitle.orEmpty())
            )
        }
        savedFile().writeText(entries.toString())
        _saved.value = books
    }

    suspend fun download(book: HadithBook): Result<Unit> = HadithPdfStore.download(book).onSuccess {
        store(_saved.value.filterNot { it.id == book.id } + book)
    }

    fun delete(book: HadithBook) {
        store(_saved.value.filterNot { it.id == book.id })
        HadithPdfStore.delete(book)
    }

    /** Deletes saved copies of [identifier] whose file is no longer among [present] at the source. */
    private fun dropMissing(identifier: String, present: Set<String>): Int {
        val gone = _saved.value.filter { it.slug == identifier && it.id !in present }
        gone.forEach(::delete)
        return gone.size
    }

    /**
     * Checks each saved book against the source at most once a day and deletes those the source
     * has removed. A book is deleted only on the source's own answer, never on a network failure.
     */
    suspend fun syncRemovals(force: Boolean = false): Int = withContext(Dispatchers.IO) {
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        if (!force && now - prefs.getLong("synced", 0L) < SYNC_INTERVAL_MS) return@withContext 0
        var removed = 0
        var complete = true
        _saved.value.map { it.slug }.distinct().forEach { identifier ->
            runCatching {
                val metadata = JSONObject(FastNet.text(METADATA + encode(identifier)))
                val present = if (isRemoved(metadata)) emptySet()
                    else pdfFiles(metadata).map { bookId(identifier, it.first) }.toSet()
                removed += dropMissing(identifier, present)
            }.onFailure { complete = false }
        }
        if (complete) prefs.edit().putLong("synced", now).apply()
        removed
    }
}
