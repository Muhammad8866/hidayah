package com.example.hadith

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.core.net.FastNet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

/**
 * Finds the page of a Hadith or Islamic book PDF that a question is about. The index is built
 * from the mirrored PDFs by tools/book_search/build_index.py and downloaded once; searching then
 * runs on the device. A question is first turned into concepts through the index's dictionary, so
 * "wuzu", "वुज़ू" and "ablution" all reach the same pages; words outside the dictionary are looked
 * up by sound. Only books whose PDF carries real text or real bookmarks are covered.
 */
object BookSearchIndex {
    private const val RELEASE =
        "https://github.com/Muhammad8866/deen-companion-hadith-pdfs/releases/download/search-v1"
    private const val INDEX_URL = "$RELEASE/book-search.db.gz"
    private const val VERSION_URL = "$RELEASE/book-search-version.txt"
    private const val PREFS = "book_search"
    /** Layout of the index file; must equal FORMAT in build_index.py. */
    private const val FORMAT = "2"
    private const val CHECK_INTERVAL_MS = 24L * 60 * 60 * 1000
    private const val MAX_PHRASE = 6
    private const val MAX_RESULTS = 40
    private const val SNIPPET_BEFORE = 70
    private const val SNIPPET_LENGTH = 230
    private const val CACHED_SEARCHES = 12
    private const val KNOWN_WORD_PAGES = 3

    sealed interface State {
        data class Missing(val failed: Boolean = false) : State
        data class Downloading(val progress: Float) : State
        data class Ready(val updateAvailable: Boolean = false) : State
    }

    /** One place to open: a chapter when [heading] is set, otherwise a page with matching text. */
    data class Hit(
        val book: HadithBook,
        val page: Int,
        val heading: String?,
        val snippet: String,
        val highlights: List<IntRange>
    )

    /** [understood] names what the question was read as, e.g. "Safar" and "Namaz". */
    data class Outcome(val hits: List<Hit>, val understood: List<String>)

    private lateinit var appContext: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val lock = Mutex()
    private var database: SQLiteDatabase? = null
    private var stopwords: Set<String> = emptySet()
    private var assets: Map<String, Int> = emptyMap()
    private var labels: Map<Int, String> = emptyMap()
    private var dictionary: List<String> = emptyList()
    private val recent = object : LinkedHashMap<Pair<String, List<String>>, Outcome>(16, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Pair<String, List<String>>, Outcome>?) =
            size > CACHED_SEARCHES
    }

    private val _state = MutableStateFlow<State>(State.Missing())
    val state: StateFlow<State> = _state.asStateFlow()

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        if (indexFile().isFile) _state.value = State.Ready()
    }

    private fun indexFile() = File(File(appContext.filesDir, "book_search"), "index.db")

    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /**
     * Fetches the index in the background; leaving the screen does not interrupt it. An older
     * index stays in use until the new file has opened correctly.
     */
    fun download() {
        val before = _state.value
        if (before is State.Downloading) return
        _state.value = State.Downloading(0f)
        scope.launch {
            _state.value = try {
                fetch()
                State.Ready()
            } catch (error: Exception) {
                if (before is State.Ready) State.Ready(updateAvailable = true) else State.Missing(failed = true)
            }
        }
    }

    private suspend fun fetch() = withContext(Dispatchers.IO) {
        val target = indexFile()
        val packed = File(target.path + ".gz")
        val unpacked = File(target.path + ".part")
        try {
            FastNet.download(INDEX_URL, packed) { received, total ->
                if (total > 0L) _state.value = State.Downloading((received.toDouble() / total).toFloat().coerceIn(0f, 1f))
            }
            GZIPInputStream(packed.inputStream().buffered()).use { input ->
                unpacked.outputStream().use(input::copyTo)
            }
            val version = open(unpacked).use(::versionOf)
            lock.withLock {
                database?.close()
                database = null
                synchronized(recent) { recent.clear() }
                target.delete()
                check(unpacked.renameTo(target)) { "Could not store the search index" }
            }
            prefs().edit().putString("version", version).putLong("checked", System.currentTimeMillis()).apply()
        } finally {
            packed.delete()
            unpacked.delete()
        }
    }

    /** At most once a day, notes whether a newer index (more books or dictionary words) is published. */
    suspend fun checkForUpdate() {
        if (_state.value != State.Ready()) return
        val now = System.currentTimeMillis()
        if (now - prefs().getLong("checked", 0L) < CHECK_INTERVAL_MS) return
        val latest = withContext(Dispatchers.IO) { runCatching { FastNet.text(VERSION_URL).trim() }.getOrNull() } ?: return
        prefs().edit().putLong("checked", now).apply()
        if (latest.isNotEmpty() && latest != prefs().getString("version", null) && _state.value == State.Ready()) {
            _state.value = State.Ready(updateAvailable = true)
        }
    }

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

    /** Opens the index on first use and loads the small lookup tables every search needs. */
    private fun ready(): SQLiteDatabase? {
        database?.let { return it }
        val file = indexFile()
        if (!file.isFile) return null
        return try {
            open(file).also { db ->
                versionOf(db)
                stopwords = db.strings("SELECT token FROM stopword").toSet()
                assets = db.rawQuery("SELECT asset, id FROM book", null).use { cursor ->
                    buildMap { while (cursor.moveToNext()) put(cursor.getString(0), cursor.getInt(1)) }
                }
                labels = db.rawQuery("SELECT id, label FROM concept", null).use { cursor ->
                    buildMap { while (cursor.moveToNext()) put(cursor.getInt(0), cursor.getString(1)) }
                }
                dictionary = db.strings("SELECT DISTINCT key FROM term WHERE key NOT LIKE '% %'")
                database = db
            }
        } catch (error: Exception) {
            // A damaged or outdated file can never be searched; removing it brings the download prompt back.
            file.delete()
            _state.value = State.Missing()
            null
        }
    }

    private fun assetOf(book: HadithBook) = book.mirrorAssetName.removeSuffix(".pdf")

    private fun cacheKey(query: String, books: List<HadithBook>) = query.trim() to books.map { it.id }

    /** The answer to an earlier identical search, available at once when a reader returns to the list. */
    fun remembered(query: String, books: List<HadithBook>): Outcome? =
        synchronized(recent) { recent[cacheKey(query, books)] }

    /** Best places for [query] within [books], most relevant first; no hits when nothing matches. */
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

        val matcher = SnippetMatcher(parts.map { part -> part.word to termKeys(db, part.concepts) })
        val hits = chosen.mapNotNull { doc ->
            db.rawQuery("SELECT page, heading, body FROM doc WHERE id = $doc", null).use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val heading = if (cursor.isNull(1)) null else cursor.getString(1)
                val text = heading ?: InflaterInputStream(cursor.getBlob(2).inputStream()).use { it.readBytes().decodeToString() }
                val (snippet, highlights) = matcher.excerpt(text, whole = heading != null)
                Hit(byId.getValue(bookOf.getValue(doc)), cursor.getInt(0), heading, snippet, highlights)
            }
        }
        return Outcome(hits, understood)
    }

    private fun termKeys(db: SQLiteDatabase, concepts: List<Int>): List<List<String>> =
        if (concepts.isEmpty()) emptyList()
        else db.strings("SELECT key FROM term WHERE concept IN (${concepts.joinToString(",")})").map { it.split(' ') }

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
            if (whole || text.length <= SNIPPET_LENGTH) return text to found.map { it.range }

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
            return snippet to found.map { it.range }.filter { it.first >= from && it.last < to }
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
