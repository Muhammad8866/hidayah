package com.example.hadith

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.core.net.FastNet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.Inflater

/**
 * Finds the page of a Hadith or Islamic book PDF that a question is about. The index is built
 * from the mirrored PDFs by tools/book_search/build_index.py and ships inside the app, so
 * searching needs no download and runs on the device. Only the short preview text of a matching
 * page is fetched from the network, a few kilobytes at a time, and without it a hit still names
 * its book and page. A question is first turned into concepts through the index's dictionary, so
 * "wuzu", "वुज़ू" and "ablution" all reach the same pages; words outside the dictionary are looked
 * up by sound. Only books whose PDF carries real text or real bookmarks are covered.
 */
object BookSearchIndex {
    private const val RELEASE =
        "https://github.com/Muhammad8866/hidayah/releases/download/search-v2"
    private const val CORE_ASSET = "book_search/core.db"
    private const val VERSION_ASSET = "book_search/version.txt"
    private const val PREFS = "book_search"
    /** Version of the bundled index last copied to app storage. */
    private const val KEY_VERSION = "core_version"
    /** Layout of the index file; must equal FORMAT in build_index.py. */
    private const val FORMAT = "3"
    private const val MAX_PHRASE = 6
    private const val MAX_RESULTS = 40
    private const val SNIPPET_BEFORE = 70
    private const val SNIPPET_LENGTH = 230
    private const val CACHED_SEARCHES = 12
    private const val KNOWN_WORD_PAGES = 3
    private const val CACHED_PAGES = 256
    private const val CACHED_PAGE_CHARS = 2_000_000
    /** Upper bound for one page, packed and unpacked; a damaged file must not exhaust memory. */
    internal const val MAX_PAGE_BYTES = 64 * 1024
    /**
     * Previews closer together than this share one request. A packed page is about 850 bytes, so
     * a wider gap mostly downloads pages nobody asked for.
     */
    internal const val RANGE_GAP = 4L * 1024
    internal const val RANGE_GROUP = 1L shl 20
    /** Small requests to one host; more at once keeps a slow network from queueing them. */
    private const val PARALLEL_FETCHES = 6
    /** After the bundled index could not be stored (e.g. a full disk), searches wait this long before copying again. */
    private const val INSTALL_RETRY_MS = 60_000L
    private val BODIES_NAME = Regex("book-search-bodies-[A-Za-z0-9._-]{1,64}\\.bin")

    /** One place to open: a chapter when [heading] is set, otherwise a page with matching text. */
    data class Hit(
        val book: HadithBook,
        val page: Int,
        val heading: String?,
        /** Words around the match; empty for a page whose preview has not been fetched. */
        val snippet: String,
        val highlights: List<IntRange>,
        internal val doc: Int = 0
    )

    /** [understood] names what the question was read as, e.g. "Safar" and "Namaz". */
    data class Outcome(
        val hits: List<Hit>,
        val understood: List<String>,
        /** What a preview is matched against: each part's word and its dictionary phrases. */
        internal val parts: List<Pair<String?, List<List<String>>>> = emptyList()
    )

    /** The index bundled with the app: its version and a way to read the database. */
    internal class Seed(val version: String, val open: () -> InputStream)

    /** Where the bundled index comes from; tests supply a fixture instead of the app's asset. */
    internal var bundled: (Context) -> Seed = { context ->
        val version = context.assets.open(VERSION_ASSET).bufferedReader().use { it.readText().trim() }
        Seed(version) { context.assets.open(CORE_ASSET) }
    }

    /** Fetches bytes [from]..[to] of a file; tests replace the network. */
    internal var fetchRange: suspend (url: String, from: Long, to: Long) -> ByteArray =
        { url, from, to -> FastNet.range(url, from, to) }

    /** How long previews may take before the hits are shown without the missing ones. */
    internal var fetchTimeoutMs = 8_000L

    private lateinit var appContext: Context
    private val lock = Mutex()
    private var database: SQLiteDatabase? = null
    private var seed: Seed? = null
    private var legacyRemoved = false
    private var installFailedAt = 0L
    private var bodiesUrl: String? = null
    private var bodiesSize = 0L
    private var stopwords: Set<String> = emptySet()
    private var assets: Map<String, Int> = emptyMap()
    private var labels: Map<Int, String> = emptyMap()
    private var dictionary: List<String> = emptyList()
    private val recent = object : LinkedHashMap<Pair<String, List<String>>, Outcome>(16, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Pair<String, List<String>>, Outcome>?) =
            size > CACHED_SEARCHES
    }

    /** Page texts already fetched, so a page reached again shows its preview at once and offline. */
    internal val pages = TextCache(CACHED_PAGES, CACHED_PAGE_CHARS)

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
    }

    /** Starts over from [context] as a fresh process would; files and preferences are kept. For tests. */
    internal fun resetForTest(context: Context) {
        database?.close()
        database = null
        seed = null
        legacyRemoved = false
        installFailedAt = 0L
        bodiesUrl = null
        bodiesSize = 0L
        pages.clear()
        synchronized(recent) { recent.clear() }
        appContext = context.applicationContext
    }

    internal fun cachedSearches(): Int = synchronized(recent) { recent.size }

    private fun indexFile() = File(File(appContext.filesDir, "book_search"), "core.db")

    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun open(file: File): SQLiteDatabase = SQLiteDatabase.openDatabase(
        file.path, null, SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
    )

    private fun SQLiteDatabase.strings(sql: String): List<String> =
        rawQuery(sql, null).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getString(0)) } }

    /** Version of an index this build can read; fails for any other file. */
    private fun versionOf(db: SQLiteDatabase): String {
        check(db.strings("SELECT value FROM meta WHERE key = 'format'").firstOrNull() == FORMAT) {
            "Search index has another format"
        }
        return db.strings("SELECT value FROM meta WHERE key = 'version'").first()
    }

    /** Frees the space of the index earlier versions of the app downloaded. */
    private fun removeLegacyIndex() {
        val folder = File(appContext.filesDir, "book_search")
        listOf("index.db", "index.db.gz", "index.db.part").forEach { File(folder, it).delete() }
        val prefs = prefs()
        if (prefs.contains("version") || prefs.contains("checked")) prefs.edit().remove("version").remove("checked").apply()
    }

    /**
     * Opens the index on first use and loads the small lookup tables every search needs. The copy
     * in app storage is replaced by the bundled one when it is missing, damaged or from an older
     * version of the app.
     */
    private fun ready(): SQLiteDatabase? {
        database?.let { return it }
        if (!::appContext.isInitialized) return null
        if (!legacyRemoved) {
            runCatching { removeLegacyIndex() }
            legacyRemoved = true
        }
        val source = seed ?: runCatching { bundled(appContext) }.getOrNull()?.also { seed = it } ?: return null
        // Every keystroke searches; without this pause a full disk would repeat a 12 MB copy each time.
        if (installFailedAt != 0L && System.currentTimeMillis() - installFailedAt < INSTALL_RETRY_MS) return null
        val file = indexFile()
        repeat(2) { attempt ->
            try {
                if (attempt > 0 || !file.isFile || prefs().getString(KEY_VERSION, null) != source.version) install(source, file)
                return load(open(file))
            } catch (error: Exception) {
                // A damaged copy can never be searched; the next attempt copies the bundled index again.
                file.delete()
                prefs().edit().remove(KEY_VERSION).apply()
            }
        }
        installFailedAt = System.currentTimeMillis()
        return null
    }

    private fun install(source: Seed, target: File) {
        target.parentFile?.mkdirs()
        val part = File(target.path + ".part")
        try {
            source.open().use { input -> part.outputStream().use { input.copyTo(it) } }
            open(part).use(::versionOf)
            target.delete()
            check(part.renameTo(target)) { "Could not store the search index" }
            prefs().edit().putString(KEY_VERSION, source.version).apply()
        } finally {
            part.delete()
        }
    }

    private fun load(db: SQLiteDatabase): SQLiteDatabase = try {
        versionOf(db)
        val meta = db.rawQuery("SELECT key, value FROM meta", null).use { cursor ->
            buildMap<String, String?> { while (cursor.moveToNext()) put(cursor.getString(0), cursor.getString(1)) }
        }
        bodiesUrl = meta["bodies"]?.takeIf { BODIES_NAME.matches(it) }?.let { "$RELEASE/$it" }
        bodiesSize = meta["bodies_size"]?.toLongOrNull() ?: 0L
        stopwords = db.strings("SELECT token FROM stopword").toSet()
        assets = db.rawQuery("SELECT asset, id FROM book", null).use { cursor ->
            buildMap { while (cursor.moveToNext()) put(cursor.getString(0), cursor.getInt(1)) }
        }
        labels = db.rawQuery("SELECT id, label FROM concept", null).use { cursor ->
            buildMap { while (cursor.moveToNext()) put(cursor.getInt(0), cursor.getString(1)) }
        }
        dictionary = db.strings("SELECT DISTINCT key FROM term WHERE key NOT LIKE '% %'")
        // Page numbers of another index version would name the wrong pages.
        pages.clear()
        synchronized(recent) { recent.clear() }
        database = db
        db
    } catch (error: Exception) {
        db.close()
        throw error
    }

    /**
     * The index's name for [book]'s PDF. A shelf book now read from the library list keeps the name
     * its PDF was indexed under before; every other book is named by its mirror asset.
     */
    private fun assetOf(book: HadithBook) =
        IslamicBooksCatalog.legacyAsset(book.id) ?: book.mirrorAssetName.removeSuffix(".pdf")

    private fun cacheKey(query: String, books: List<HadithBook>) = query.trim() to books.map { it.id }

    /** The answer to an earlier identical search, available at once when a reader returns to the list. */
    fun remembered(query: String, books: List<HadithBook>): Outcome? =
        synchronized(recent) { recent[cacheKey(query, books)] }

    /**
     * Best places for [query] within [books], most relevant first; no hits when nothing matches.
     * Runs on the device only: a page whose preview was not fetched before has an empty snippet,
     * which [withExcerpts] fills in.
     */
    suspend fun search(query: String, books: List<HadithBook>): Outcome {
        remembered(query, books)?.let { return it }
        return withContext(Dispatchers.Default) {
            lock.withLock {
                val db = ready() ?: return@withLock Outcome(emptyList(), emptyList())
                try {
                    find(db, query, books).also { synchronized(recent) { recent[cacheKey(query, books)] = it } }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Outcome(emptyList(), emptyList())
                }
            }
        }
    }

    /**
     * [outcome] with the previews of its pages fetched from the network, also for the next
     * [remembered]. Offline, or when a preview fails, the hits stay as they are.
     */
    suspend fun withExcerpts(query: String, books: List<HadithBook>, outcome: Outcome): Outcome {
        if (outcome.hits.none(::needsText)) return outcome
        return try {
            val filled = fill(outcome)
            if (filled != outcome) synchronized(recent) { recent[cacheKey(query, books)] = filled }
            filled
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            outcome
        }
    }

    private fun needsText(hit: Hit) = hit.heading == null && hit.snippet.isEmpty()

    /** Where a page's packed text lies in the previews file. */
    internal class Span(val doc: Int, val at: Long, val length: Int)

    /** One request: bytes [from]..[to] of the previews file, holding [spans]. */
    internal class Group(val from: Long, val to: Long, val spans: List<Span>)

    private class Wanted(val url: String, val spans: List<Span>)

    private suspend fun fill(outcome: Outcome): Outcome {
        val docs = outcome.hits.filter(::needsText).map { it.doc }.distinct()
        val texts = HashMap<Int, String>()
        docs.forEach { doc -> pages.get(doc)?.let { texts[doc] = it } }
        val wanted = withContext(Dispatchers.Default) {
            lock.withLock {
                val db = ready() ?: return@withLock null
                val url = bodiesUrl ?: return@withLock null
                Wanted(url, spansOf(db, docs.filter { it !in texts }))
            }
        }
        if (wanted != null && wanted.spans.isNotEmpty()) {
            fetch(wanted.url, wanted.spans).forEach { (doc, text) ->
                pages.put(doc, text)
                texts[doc] = text
            }
        }
        if (texts.isEmpty()) return outcome
        return withContext(Dispatchers.Default) {
            val matcher = SnippetMatcher(outcome.parts)
            outcome.copy(hits = outcome.hits.map { hit ->
                val text = texts[hit.doc]
                if (!needsText(hit) || text == null) hit
                else matcher.excerpt(text, whole = false).let { (snippet, highlights) -> hit.copy(snippet = snippet, highlights = highlights) }
            })
        }
    }

    /** Locations of [docs]' texts; a location outside the previews file is left out. */
    private fun spansOf(db: SQLiteDatabase, docs: List<Int>): List<Span> {
        if (docs.isEmpty() || bodiesSize <= 0L) return emptyList()
        return db.rawQuery(
            "SELECT id, body_at, body_len FROM doc WHERE id IN (${docs.joinToString(",")}) " +
                "AND body_at IS NOT NULL AND body_len IS NOT NULL",
            null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val at = cursor.getLong(1)
                    val length = cursor.getLong(2)
                    if (at >= 0L && length in 1L..MAX_PAGE_BYTES.toLong() && at + length <= bodiesSize) {
                        add(Span(cursor.getInt(0), at, length.toInt()))
                    }
                }
            }
        }
    }

    /** Nearby texts merged into requests of at most [RANGE_GROUP] bytes, in file order. */
    internal fun coalesce(spans: List<Span>): List<Group> {
        val groups = mutableListOf<Group>()
        var current = mutableListOf<Span>()
        var from = 0L
        var end = 0L // exclusive
        for (span in spans.sortedBy { it.at }) {
            val spanEnd = span.at + span.length
            if (current.isNotEmpty() && span.at - end <= RANGE_GAP && maxOf(end, spanEnd) - from <= RANGE_GROUP) {
                current += span
                end = maxOf(end, spanEnd)
            } else {
                if (current.isNotEmpty()) groups += Group(from, end - 1, current)
                current = mutableListOf(span)
                from = span.at
                end = spanEnd
            }
        }
        if (current.isNotEmpty()) groups += Group(from, end - 1, current)
        return groups
    }

    /** The texts that arrived within [fetchTimeoutMs]; a failed or damaged request is skipped. */
    private suspend fun fetch(url: String, spans: List<Span>): Map<Int, String> = withContext(Dispatchers.Default) {
        val found = ConcurrentHashMap<Int, String>()
        val permits = Semaphore(PARALLEL_FETCHES)
        withTimeoutOrNull(fetchTimeoutMs) {
            coroutineScope {
                coalesce(spans).forEach { group ->
                    launch {
                        permits.withPermit {
                            val bytes = try {
                                fetchRange(url, group.from, group.to)
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (error: Exception) {
                                return@withPermit
                            }
                            if (bytes.size.toLong() != group.to - group.from + 1) return@withPermit
                            group.spans.forEach { span ->
                                inflatePage(bytes, (span.at - group.from).toInt(), span.length)?.let { found[span.doc] = it }
                            }
                        }
                    }
                }
            }
        }
        found
    }

    /** The text packed in [length] bytes of [data] at [offset], or null when it is damaged or too long. */
    internal fun inflatePage(data: ByteArray, offset: Int, length: Int): String? {
        if (offset < 0 || length <= 0 || offset + length > data.size) return null
        val inflater = Inflater()
        return try {
            inflater.setInput(data, offset, length)
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = inflater.inflate(buffer)
                out.write(buffer, 0, count)
                if (out.size() > MAX_PAGE_BYTES) return null
                if (inflater.finished()) break
                // Needs more input or a dictionary, or makes no progress: not a whole page.
                if (count == 0) return null
            }
            out.toByteArray().decodeToString()
        } catch (error: Exception) {
            null
        } finally {
            inflater.end()
        }
    }

    /** One part of a question: dictionary concepts, or a single word looked up by its sound. */
    private class Part(val concepts: List<Int>, val word: String?, val label: String)

    private fun conceptsOf(db: SQLiteDatabase, keys: List<String>): List<Int> = db.rawQuery(
        "SELECT DISTINCT concept FROM term WHERE key IN (${keys.joinToString(",") { "?" }})", keys.toTypedArray()
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getInt(0)) } }

    private fun labelOf(concepts: List<Int>) =
        concepts.mapNotNull { labels[it] }.distinct().take(2).joinToString(" / ") { it.replaceFirstChar(Char::uppercase) }

    /** Longest dictionary phrases become concepts; what is left and is not filler stays a word. */
    private fun parse(db: SQLiteDatabase, query: String): List<Part> {
        val tokens = BookSearchText.normalize(query)
        val keys = tokens.map(BookSearchText::soundKey)
        val parts = mutableListOf<Part>()
        var index = 0
        while (index < tokens.size) {
            var taken = 0
            for (size in minOf(MAX_PHRASE, tokens.size - index) downTo 1) {
                val candidates = mutableListOf(keys.subList(index, index + size).joinToString(" "))
                if (size == 1 && keys[index].endsWith("s")) candidates += keys[index].dropLast(1)
                val concepts = conceptsOf(db, candidates)
                if (concepts.isNotEmpty()) {
                    parts += Part(concepts, null, labelOf(concepts))
                    taken = size
                    break
                }
            }
            if (taken == 0) {
                val token = tokens[index]
                if (token.length > 1 && token !in stopwords) parts += corrected(db, keys[index]) ?: Part(emptyList(), keys[index], token)
                taken = 1
            }
            index += taken
        }
        return parts.distinctBy { it.concepts to it.word }
    }

    /**
     * A misspelt dictionary word ("namz", "tahjud"). Only tried for a word the books do not use, so a
     * real word that merely resembles a dictionary entry ("Raza" beside "roza") is left alone.
     */
    private fun corrected(db: SQLiteDatabase, key: String): Part? {
        if (key.length < 4) return null
        // A couple of pages may hold the same letters by accident of a scan; a real word is on many.
        val known = db.rawQuery("SELECT 1 FROM words WHERE words MATCH ? LIMIT $KNOWN_WORD_PAGES", arrayOf(key)).use { it.count }
        if (known >= KNOWN_WORD_PAGES) return null
        val limit = if (key.length <= 6) 1 else 2
        var best = limit + 1
        var matches = mutableListOf<String>()
        for (entry in dictionary) {
            if (entry[0] != key[0] || kotlin.math.abs(entry.length - key.length) > limit) continue
            val distance = editDistance(key, entry, limit)
            if (distance < best) {
                best = distance
                matches = mutableListOf(entry)
            } else if (distance == best && distance <= limit) matches += entry
        }
        if (best > limit) return null
        val concepts = conceptsOf(db, matches)
        return concepts.takeIf { it.isNotEmpty() }?.let { Part(it, null, labelOf(it)) }
    }

    /** Edits (insert, delete, replace, swap of neighbours) turning [a] into [b]; above [limit] the exact value is not needed. */
    private fun editDistance(a: String, b: String, limit: Int): Int {
        var previous = IntArray(b.length + 1) { it }
        var beforePrevious = IntArray(b.length + 1)
        for (i in 1..a.length) {
            val current = IntArray(b.length + 1)
            current[0] = i
            var rowBest = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                var value = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) value = minOf(value, beforePrevious[j - 2] + 1)
                current[j] = value
                rowBest = minOf(rowBest, value)
            }
            if (rowBest > limit) return limit + 1
            beforePrevious = previous
            previous = current
        }
        return previous[b.length]
    }

    private fun find(db: SQLiteDatabase, query: String, books: List<HadithBook>): Outcome {
        val byId = books.mapNotNull { book -> assets[assetOf(book)]?.let { it to book } }.toMap()
        val parts = parse(db, query)
        val understood = parts.map { it.label }
        if (byId.isEmpty() || parts.isEmpty()) return Outcome(emptyList(), understood)
        val bookFilter = byId.keys.joinToString(",")

        // matched = how many parts of the question a place answers; total = how strongly.
        val matched = HashMap<Int, Int>()
        val total = HashMap<Int, Int>()
        val bookOf = HashMap<Int, Int>()
        fun add(doc: Int, book: Int, score: Int) {
            matched[doc] = (matched[doc] ?: 0) + 1
            total[doc] = (total[doc] ?: 0) + score
            bookOf[doc] = book
        }
        parts.forEach { part ->
            if (part.word == null) db.rawQuery(
                "SELECT h.doc, d.book, MAX(h.score) FROM hit h JOIN doc d ON d.id = h.doc " +
                    "WHERE h.concept IN (${part.concepts.joinToString(",")}) AND d.book IN ($bookFilter) GROUP BY h.doc",
                null
            ).use { cursor -> while (cursor.moveToNext()) add(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2)) }
            else db.rawQuery(
                "SELECT w.docid, d.book, d.heading IS NOT NULL FROM words w JOIN doc d ON d.id = w.docid " +
                    "WHERE words MATCH ? AND d.book IN ($bookFilter)",
                arrayOf("${part.word}*")
            ).use { cursor ->
                while (cursor.moveToNext()) add(cursor.getInt(0), cursor.getInt(1), if (cursor.getInt(2) != 0) 3000 else 1000)
            }
        }

        // Someone typing in Arabic script reads Arabic chapter titles; everyone else is better
        // served by an edition in Latin script first.
        val arabicQuery = query.any { it in '؀'..'ۿ' }
        val preferred = byId.filterValues { (it.languageCode in setOf("ar", "ur", "fa")) == arabicQuery }.keys
        fun strength(doc: Int) = total.getValue(doc) * (if (bookOf[doc] in preferred) 5 else 4)
        val ranked = matched.keys.sortedWith(
            compareByDescending<Int> { matched.getValue(it) }.thenByDescending(::strength).thenBy { it }
        )

        // No single volume may crowd out the rest, unless only a few volumes are being searched.
        val perBookLimit = (MAX_RESULTS / byId.size).coerceIn(3, 15)
        val perBook = HashMap<Int, Int>()
        val chosen = ranked.asSequence().filter { doc ->
            val book = bookOf.getValue(doc)
            val seen = perBook[book] ?: 0
            (seen < perBookLimit).also { if (it) perBook[book] = seen + 1 }
        }.take(MAX_RESULTS).toList()

        val matcherParts = parts.map { part -> part.word to termKeys(db, part.concepts) }
        val matcher = SnippetMatcher(matcherParts)
        val hits = chosen.mapNotNull { doc ->
            db.rawQuery("SELECT page, heading FROM doc WHERE id = ?", arrayOf(doc.toString())).use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val book = byId.getValue(bookOf.getValue(doc))
                val heading = if (cursor.isNull(1)) null else cursor.getString(1)
                // A page's text is in memory only once its preview has been fetched.
                val text = heading ?: pages.get(doc)
                    ?: return@use Hit(book, cursor.getInt(0), null, "", emptyList(), doc)
                val (snippet, highlights) = matcher.excerpt(text, whole = heading != null)
                Hit(book, cursor.getInt(0), heading, snippet, highlights, doc)
            }
        }
        return Outcome(hits, understood, matcherParts)
    }

    private fun termKeys(db: SQLiteDatabase, concepts: List<Int>): List<List<String>> =
        if (concepts.isEmpty()) emptyList()
        else db.strings("SELECT key FROM term WHERE concept IN (${concepts.joinToString(",")})").map { it.split(' ') }

    /** Most recently used texts, bounded both in count and in total length. */
    internal class TextCache(private val capacity: Int, private val maxChars: Int) {
        private val map = LinkedHashMap<Int, String>(16, .75f, true)
        private var chars = 0L

        @Synchronized fun get(doc: Int): String? = map[doc]

        @Synchronized fun put(doc: Int, text: String) {
            map.remove(doc)?.let { chars -= it.length }
            map[doc] = text
            chars += text.length
            val eldest = map.entries.iterator()
            while ((map.size > capacity || chars > maxChars) && map.size > 1 && eldest.hasNext()) {
                chars -= eldest.next().value.length
                eldest.remove()
            }
        }

        @Synchronized fun clear() {
            map.clear()
            chars = 0L
        }

        @Synchronized fun size(): Int = map.size

        @Synchronized fun totalChars(): Long = chars
    }

    /** Locates the words of a page that answered the question, to show them in context. */
    private class SnippetMatcher(parts: List<Pair<String?, List<List<String>>>>) {
        /** First sound of a dictionary phrase -> (phrase, part of the question), longest phrase first. */
        private val byFirst: Map<String, List<Pair<List<String>, Int>>> = parts
            .flatMapIndexed { part, (_, phrases) -> phrases.map { it to part } }
            .groupBy { it.first.first() }
            .mapValues { (_, list) -> list.sortedByDescending { it.first.size } }
        private val words = parts.mapIndexedNotNull { part, (word, _) -> word?.let { it to part } }

        private class Found(val range: IntRange, val part: Int)

        fun excerpt(text: String, whole: Boolean): Pair<String, List<IntRange>> {
            // Every word of the page with its position and its sound.
            val spans = mutableListOf<IntRange>()
            val sounds = mutableListOf<String>()
            var start = -1
            for (index in 0..text.length) {
                val inWord = index < text.length && (BookSearchText.isWordChar(text[index]) || text[index] == '\'' || text[index] == '’')
                if (inWord && start < 0) start = index
                if (!inWord && start >= 0) {
                    val token = BookSearchText.normalize(text.substring(start, index)).joinToString("")
                    if (token.isNotEmpty()) {
                        spans += start until index
                        sounds += BookSearchText.soundKey(token)
                    }
                    start = -1
                }
            }
            val found = mutableListOf<Found>()
            var position = 0
            while (position < spans.size) {
                val (length, part) = match(sounds, position)
                if (length > 0) found += Found(spans[position].first..spans[position + length - 1].last, part)
                position += maxOf(length, 1)
            }
            // Matched words with only spaces between them read as one name or phrase ("Abu Hurairah").
            val marks = mutableListOf<IntRange>()
            for (range in found.map { it.range }) {
                val last = marks.lastOrNull()
                if (last != null && (last.last + 1 until range.first).all { text[it].isWhitespace() }) {
                    marks[marks.lastIndex] = last.first..range.last
                } else marks += range
            }
            if (whole || text.length <= SNIPPET_LENGTH) return text to marks

            // Start where the most parts of the question appear close together.
            val reach = SNIPPET_LENGTH - SNIPPET_BEFORE
            val anchor = found.maxByOrNull { first ->
                found.filter { it.range.first >= first.range.first && it.range.last < first.range.first + reach }
                    .map { it.part }.distinct().size * 100_000 - first.range.first / 100
            }?.range?.first ?: 0
            var from = (anchor - SNIPPET_BEFORE).coerceAtLeast(0)
            while (from > 0 && from < anchor && !text[from - 1].isWhitespace()) from++
            var to = (from + SNIPPET_LENGTH).coerceAtMost(text.length)
            while (to < text.length && to > from && !text[to - 1].isWhitespace()) to--
            val prefix = if (from > 0) "… " else ""
            val snippet = prefix + text.substring(from, to).trim() + if (to < text.length) " …" else ""
            val shift = prefix.length - from
            return snippet to marks.filter { it.first >= from && it.last < to }
                .map { (it.first + shift)..(it.last + shift) }
                .filter { it.last < snippet.length }
        }

        /** Length in words of the match starting at [position] and the part it answers, or 0. */
        private fun match(sounds: List<String>, position: Int): Pair<Int, Int> {
            val sound = sounds[position]
            val candidates = byFirst[sound] ?: byFirst[sound.removeSuffix("s")]?.filter { it.first.size == 1 }
            candidates?.forEach { (phrase, part) ->
                if (phrase.size == 1) return 1 to part
                if (position + phrase.size <= sounds.size && sounds.subList(position, position + phrase.size) == phrase) {
                    return phrase.size to part
                }
            }
            words.firstOrNull { sound.startsWith(it.first) }?.let { return 1 to it.second }
            return 0 to -1
        }
    }
}
