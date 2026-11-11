package com.example.hadith

import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URLEncoder
import java.text.Normalizer
import java.util.IdentityHashMap
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/** One book of Dawat-e-Islami's library as kept in the local index. [languageCode] is the library's own code. */
internal data class DiBook(
    val id: Long,
    val languageCode: String,
    val fileId: Long,
    val year: Int,
    val pages: Int,
    val slug: String,
    val title: String,
    val nativeName: String,
    val normalName: String,
    val categories: List<Int>,
    val writerId: Int,
    val readCount: Long,
    val date: String
)

internal class DiIndex(val books: List<DiBook>, val categories: List<WorldCategory>, val updatedAt: Long) {
    val byId: Map<Long, DiBook> by lazy { books.associateBy { it.id } }

    private data class SearchKey(val words: String, val language: String, val categoryId: Int?, val sort: WorldSort)

    private val orders = ConcurrentHashMap<WorldSort, List<DiBook>>()
    @Volatile private var last: Pair<SearchKey, List<DiBook>>? = null

    /** The search engine over [books] in their stored order; built once, on the first search or by [warmSearchText]. */
    private val engine: BookCatalogSearch.Index by lazy {
        BookCatalogSearch.Index(DawateIslamiIndex.searchEntries(books, categories))
    }

    /** Each book's place in [books], where its engine score is. By identity, as every order holds these same books. */
    private val positions: IdentityHashMap<DiBook, Int> by lazy {
        IdentityHashMap<DiBook, Int>(books.size * 2).apply { books.forEachIndexed { index, book -> put(book, index) } }
    }

    /** Every book in [sort] order, sorted once for this list. */
    fun sorted(sort: WorldSort): List<DiBook> = orders.getOrPut(sort) { DawateIslamiIndex.sorted(books, sort) }

    /**
     * The books of the language and category that match [query], best match first (see
     * [DawateIslamiIndex.ranked]); a query with no searchable word lists them all in [sort] order.
     * The same books as [DawateIslamiIndex.filter]. The last answer is kept, so the next page of the
     * same search does not search again.
     */
    fun search(query: String, language: String, categoryId: Int?, sort: WorldSort): List<DiBook> {
        val key = SearchKey(DawateIslamiIndex.searchKey(query), language, categoryId, sort)
        val previous = last
        if (previous != null && previous.first == key) return previous.second
        val shelf = sorted(sort).filter { DawateIslamiIndex.onShelf(it, language, categoryId) }
        // Typos only when no book of this language and category matches without them.
        val scores = DawateIslamiIndex.shelfScores({ typos -> engine.scores(query, typos) }) { scores ->
            shelf.any { scores[positions.getValue(it)] > 0 }
        }
        val found = if (scores == null) shelf else {
            val matched = shelf.mapNotNull { book -> scores[positions.getValue(book)].takeIf { it > 0 }?.let { book to it } }
            DawateIslamiIndex.ranked(matched, sort)
        }
        return found.also { last = key to it }
    }

    /** Builds the search engine ahead of the first typed search; called off the main thread. */
    fun warmSearchText() {
        engine
        positions
    }
}

/**
 * Dawat-e-Islami's library (dawateislami.net/bookslibrary) held as one local index.
 *
 * The library's paged search is not stable: paging Urdu 50 at a time returned 1,611 of 1,911 books.
 * One request for every book returns each book exactly once, so the whole list is fetched in one
 * answer, checked to be complete, and searched, filtered, sorted and paged on the phone.
 */
internal object DawateIslamiIndex {
    const val SEARCH_API = "https://www.dawateislami.net/searchapi/book-library/search"
    const val FULL_REQUEST = """{"keyword":"","language":"","categories":[],"pn":1,"ps":10000}"""
    const val LIBRARY_URL = "https://www.dawateislami.net/bookslibrary/"
    private const val DOWNLOAD = "https://data2.dawateislami.net/Data/Books/Download"
    const val MAX_BOOKS = 20_000
    /** The real answer is about 12 MB. */
    const val MAX_BYTES = 24L shl 20
    /** The longest text in the real answer is about 8 KB; nesting is 5 deep. */
    const val MAX_STRING_BYTES = 64 * 1024
    const val MAX_DEPTH = 32
    /** Below this share of readable books the answer is treated as broken, never as removals. */
    const val MIN_ACCEPTED_SHARE = 0.99
    const val MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000
    private const val CACHE_VERSION = 1

    private val publicationFolder = Regex("/Read/[a-z]+/(\\d{4})/")
    private val languageCodePattern = Regex("[a-z]{2,3}")
    private val marks = Regex("\\p{M}+")
    private val separators = Regex("[^\\p{L}\\p{N}]+")
    private val whitespace = Regex("\\s+")

    /**
     * The library's languages with the names it gives them, most books first. Arabic is not in its
     * language menu but has books. Codes are the library's own; [appLanguage] maps them for the app.
     */
    val languages: List<Pair<String, String>> = listOf(
        "ur" to "Urdu", "en" to "English", "ar" to "Arabic", "hi" to "Hindi", "bn" to "Bengali",
        "si" to "Sindhi", "rd" to "Roman Urdu", "ps" to "Pashto", "fr" to "French", "gu" to "Gujarati",
        "pt" to "Portuguese", "id" to "Indonesian", "it" to "Italian", "te" to "Telugu", "zh" to "中文",
        "nl" to "Dutch", "sw" to "Swahili", "de" to "German", "tr" to "Turkish", "ta" to "Tamil",
        "no" to "Norwegian", "cr" to "Creole", "ld" to "Luganda", "fa" to "فارسی", "es" to "Spanish",
        "ny" to "Chichewa", "pa" to "Punjabi", "skr" to "Saraiki", "da" to "Danish", "hu" to "Hausa",
        "my" to "Malay", "sq" to "Albanian", "np" to "Nepali", "bl" to "Balochi", "sn" to "Sinhala",
        "th" to "Thai", "kr" to "Korean", "ru" to "Russian", "kn" to "Kannada", "gr" to "Greek",
        "et" to "Estonian", "sv" to "Swedish", "so" to "Somalian", "ky" to "Kyrgyz", "gb" to "Gujarati Bhasha"
    )

    /** Library codes that differ from the app's (ISO) codes; every other code is the same in both. */
    private val appCodes = mapOf(
        "si" to "sd", "sn" to "si", "my" to "ms", "hu" to "ha", "np" to "ne",
        "kr" to "ko", "gr" to "el", "ld" to "lg", "bl" to "bal"
    )

    fun appLanguage(libraryCode: String): String = appCodes[libraryCode] ?: libraryCode

    /** Authors as the library's own author list names them; its "All"/"Others" entries name no one. */
    private val writers: Map<Int, String> = mapOf(
        2 to "Ameer-e-Ahl-e-Sunnat", 3 to "Imam-e-Ahl-e-Sunnat Ahmad Raza Khan", 4 to "Al Madina-tul-Ilmiyah",
        5 to "Maktaba-tul-Madina", 7 to "Mufti Muhammad Shareef-ul-Haq Amjadi", 8 to "Mufti Ahmad yar khan Naeemi",
        9 to "Shaikh-ul-Hadees Allama Abdul Mustafa Al-Aazami", 10 to "Majlis-e-Ifta",
        11 to "Hujjat-ul-Islam Maulana Haamid Raza khan", 12 to "Allama Maulana Hasan Raza Khan",
        13 to "Markazi Majlis-e-Shoora", 14 to "Majlis e Tauqeet", 15 to "Maulana Muhammad Haneef Khan Razavi",
        17 to "Maktabat-ul-Madina", 18 to "Imam Muhammad Bin Ismail Bukhari", 19 to "Imam Muslim Bin Hajjaj Qushairi",
        20 to "Imam Abu Isa Tirmizi", 21 to "Imam Abu Dawood Sulaiman", 22 to "Imam Ahmad Alnisai",
        23 to "Imam Muhammad Bin Majah", 24 to "Imam Malik Bin Anas", 25 to "Imam Ahmad Bin Hambal",
        26 to "Imam Muhammad Darmi", 27 to "Imam Abdur Rahman Jozi", 28 to "Imam Sharf uddin Nawawi",
        29 to "Imam Muhammad Ghazali", 30 to "Imam Muhammad Bin Ahmad Zahbi", 31 to "Imam Jalal ud Din Suyuti",
        32 to "Mufti Muhammad Qasim Attari", 33 to "Mufti Ali Asghar", 34 to "Maulana Abdul Razzaq",
        35 to "Majlis Madrasa Almadinah", 36 to "Majlis e Tauqeet",
        37 to "Hazrat Allama Qazi Shams-ud-Deen Ahmed Jonpary Paishkash Majlis Madina-tul-Ilmia",
        38 to "Khalil Milat Mufti Khalil Khan Qadri Barkati Qadas Sarah-ul-Aziz",
        39 to "Professor Doctor Muhammad Massod Ahmed Qadas Sarah-ul-Aziz", 40 to "Majlis-e-Tehqiqat-e-Sharia",
        41 to "Majlis Taukiyat Paishkash Majlis Madina-tul-Ilmia", 42 to "Raees-ul-Tahrer Allama Arshad-ul-Qadri",
        43 to "Naeemi", 44 to "Shaikh-ul-Hadees Hazrat Allama Abdul Mustafa Azeemi", 45 to "Imam Tabrani",
        46 to "Shaikh Abdul Qadir Jeelani", 47 to "Al Habib Umar Bin Hafiz", 48 to "Faqeeh Abul Lais Samarqandi",
        49 to "Shaikh Abdullah Siraj ud Din", 50 to "Muhammad Ramdan Saeed Al-Booti",
        51 to "Imam Abdullah Bin As-ad Al Yafi", 52 to "Imam Fakhar ud Din Razi",
        53 to "Shihab ud Deen Bin Ahmad al Ashbahi", 54 to "Syed Muhammad Bin Muhammad Al Husaini",
        55 to "Shaik Muhammad Mutawalli Sharavi", 56 to "Syed Alwi Al Malki", 57 to "Abn ataullah sakandari",
        58 to "Yousuf Bin Ismail Nabhani", 59 to "Shaikh Abdul Hadi Al Kharasah", 60 to "Dr Abdul Rahman Rafat Basha",
        61 to "Imam Abul Qasim Qushairi", 62 to "Imam Abn e Hajar Asqalani", 63 to "Imam Abu Zakria An Nawawi",
        64 to "Imam Bayhaqi", 65 to "Allama Abdul Raoof Manavi", 66 to "Qadii Ayyaz Malki",
        67 to "Imam Ahmad Bin Muhammad Qastalani", 68 to "Imam Abdul Rahman Ibn e Jawzi",
        69 to "Al Hafiz Ali Bin Umar Al-Daraqutni", 70 to "Imam Badar ul Deen Ayini", 71 to "Allama Abu Ishaq Nisha puri",
        72 to "Allama Ismaeel Haqqi Hanafi", 73 to "Nizam ud Din Neshapuri", 74 to "Noor ud Din Ali Bin Ahmad As Smhoodi",
        75 to "Imam Hussain Bin Masood Al Baghawi", 76 to "Imam Abu Issa Tirmizi", 77 to "Hafiz Abdul Raheem Iraqi",
        78 to "Arabic Department", 79 to "Imam Muhammad Bin Yousu Al Salihi", 80 to "Allama Zurqani",
        81 to "Shaik Aiman Yasir Al Bakkar", 82 to "Mulla Ali Al Qari", 83 to "Imam Taqi ud Din As-subki",
        84 to "As Shaikh Abu Bakar Al Adani abn Ali Al Mashhor", 85 to "Imam Ibn e Aseer Al Jazri",
        86 to "Hafiz Nur al-Din al-Haythami", 87 to "Abdul Haleem Mahmood", 88 to "Shaik e Akbar Muhyid deen Ibn e Arabi",
        89 to "Imam Muhammad Bin Jarir al Tabri", 90 to "Translation Department", 91 to "Ibn Hajar al-Haytami",
        92 to "Imam Badar ul Deen Zarkashi", 93 to "Imam Ibn Abi al-Dunya", 94 to "Imam Abn e abdul bar an namari",
        95 to "Imam Taqi al-Din al-Fasi", 96 to "Imam Muhammad Bin Ahmad Zahbi", 97 to "Majlis e Tauqeet",
        98 to "Haji Abdul Habib Attari", 99 to "Mufti Muhammad Sajid Attari", 100 to "Kanzul Madaris Board",
        101 to "Rukn e Shura Abu Majid Muhammad Shahid Attari",
        102 to "Khalifa e Ameer e Ahle Sunnat Maulana Abu Usaid Haji Ubaid Raza Attari Madani",
        103 to "Hazrat Allama Syed Ahmed Saeed Kazmi علیہ رحمۃُ اللہ القوی"
    )

    fun writerName(id: Int): String = writers[id].orEmpty()

    fun downloadUrl(book: DiBook): String = "$DOWNLOAD/${book.languageCode}/pdf/${book.year}/${book.fileId}-1.pdf"

    fun pageUrl(book: DiBook): String =
        LIBRARY_URL + book.languageCode + "/" + URLEncoder.encode(book.slug, "UTF-8").replace("+", "%20")

    /** Lower case, without accents or vowel marks, punctuation turned into spaces. */
    fun fold(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD).replace(marks, "").lowercase(Locale.ROOT)
            .replace(separators, " ").trim()

    // Reading the library's answer.

    /**
     * Reads the search API's answer as it streams in. It is accepted only as one complete list:
     * the stated total must be exact and equal the number of hits, with no book twice.
     */
    fun parseResponse(
        input: InputStream, maxBytes: Long = MAX_BYTES, maxBooks: Int = MAX_BOOKS,
        minAcceptedShare: Double = MIN_ACCEPTED_SHARE, now: Long
    ): DiIndex {
        JsonReader(InputStreamReader(Capped(input, maxBytes), Charsets.UTF_8)).use { reader ->
            var total = -1L
            var hits = 0
            val books = LinkedHashMap<Long, DiBook>()
            val names = HashMap<Int, String>()
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "total" -> total = readTotal(reader)
                    "hits" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            if (++hits > maxBooks) throw IOException("Too many books in the answer")
                            readHit(reader)?.let { (book, categories) ->
                                if (books.put(book.id, book) != null) throw IOException("Book ${book.id} listed twice")
                                categories.forEach { (id, name) -> if (name.isNotBlank()) names.putIfAbsent(id, name) }
                            }
                        }
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (total <= 0 || total > maxBooks || hits.toLong() != total) {
                throw IOException("Incomplete answer: $hits of $total books")
            }
            // Many unreadable entries mean a changed or broken answer; saved books must not be dropped on it.
            if (books.size < kotlin.math.ceil(hits * minAcceptedShare)) {
                throw IOException("Only ${books.size} of $hits books could be read")
            }
            return DiIndex(books.values.toList(), categories(books.values, names), now)
        }
    }

    private fun categories(books: Collection<DiBook>, names: Map<Int, String>): List<WorldCategory> {
        val counts = LinkedHashMap<Int, Int>()
        books.forEach { book -> book.categories.forEach { counts[it] = (counts[it] ?: 0) + 1 } }
        return counts.map { (id, count) -> WorldCategory(id, names[id].orEmpty(), count) }
            .sortedWith(compareByDescending<WorldCategory> { it.count }.thenBy { it.id })
    }

    /** The total is exact only when the search engine says so ("eq"); a lower bound means more exist. */
    private fun readTotal(reader: JsonReader): Long {
        if (reader.peek() == JsonToken.NUMBER) return reader.nextLong()
        var value = -1L
        var relation = "eq"
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "value" -> value = reader.nextLong()
                "relation" -> relation = reader.stringOrNull().orEmpty()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        if (relation != "eq") throw IOException("The answer gives only a lower bound")
        return value
    }

    private fun readHit(reader: JsonReader): Pair<DiBook, List<Pair<Int, String>>>? {
        var result: Pair<DiBook, List<Pair<Int, String>>>? = null
        reader.beginObject()
        while (reader.hasNext()) {
            if (reader.nextName() == "_source" && reader.peek() == JsonToken.BEGIN_OBJECT) result = readSource(reader)
            else reader.skipValue()
        }
        reader.endObject()
        return result
    }

    /** One book, or null when it lacks what is needed to build its PDF address. */
    private fun readSource(reader: JsonReader): Pair<DiBook, List<Pair<Int, String>>>? {
        var id = 0L; var fileId = 0L; var pages = 0; var writer = 0; var reads = 0L
        var language = ""; var image = ""; var slug = ""; var title = ""; var native = ""; var normal = ""
        var categoryIds = ""; var categoryNames = ""; var published = ""; var created = ""
        var enabled = true
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (reader.peek() == JsonToken.NULL) { reader.nextNull(); continue }
            when (name) {
                "id" -> id = reader.longOrZero()
                "book_coma" -> fileId = reader.longOrZero()
                "total_pages" -> pages = reader.longOrZero().coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
                "writer_id" -> writer = reader.longOrZero().coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
                "read_count" -> reads = reader.longOrZero()
                "language_code" -> language = reader.stringOrNull().orEmpty().trim()
                "img_url" -> image = reader.stringOrNull().orEmpty()
                "roman_url" -> slug = reader.stringOrNull().orEmpty().trim()
                "title" -> title = clean(reader.stringOrNull())
                "native_name" -> native = clean(reader.stringOrNull())
                "normal_name" -> normal = clean(reader.stringOrNull())
                "cat_ids" -> categoryIds = reader.stringOrNull().orEmpty()
                "category_name" -> categoryNames = reader.stringOrNull().orEmpty()
                "published_date" -> published = reader.stringOrNull().orEmpty()
                "created_date" -> created = reader.stringOrNull().orEmpty()
                "book_enable" -> enabled = if (reader.peek() == JsonToken.BOOLEAN) reader.nextBoolean() else { reader.skipValue(); true }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        val year = publicationFolder.find(image)?.groupValues?.get(1)?.toIntOrNull()
        if (!enabled || id <= 0 || fileId <= 0 || year == null || slug.isBlank() ||
            !languageCodePattern.matches(language)) return null
        val ids = categoryIds.split(',').mapNotNull { it.trim().toIntOrNull() }
        val names = categoryNames.split(',').map { it.trim() }
        val categories = if (names.size == ids.size) ids.zip(names) else ids.map { it to "" }
        val shownTitle = title.ifBlank { native.ifBlank { normal.ifBlank { slug } } }
        val book = DiBook(
            id = id, languageCode = language, fileId = fileId, year = year, pages = pages, slug = slug,
            title = shownTitle, nativeName = native, normalName = normal, categories = ids.distinct(),
            writerId = writer, readCount = reads, date = published.ifBlank { created }
        )
        return book to categories
    }

    private fun clean(text: String?): String = text.orEmpty().replace(Regex("\\s+"), " ").trim()

    private fun JsonReader.stringOrNull(): String? = when (peek()) {
        JsonToken.STRING, JsonToken.NUMBER -> nextString()
        JsonToken.BOOLEAN -> nextBoolean().toString()
        JsonToken.NULL -> { nextNull(); null }
        else -> { skipValue(); null }
    }

    /** Read as text first, so an odd value is consumed and counts as zero instead of stopping the reader. */
    private fun JsonReader.longOrZero(): Long = when (peek()) {
        JsonToken.NUMBER, JsonToken.STRING -> nextString().trim().let { it.toLongOrNull() ?: it.toDoubleOrNull()?.toLong() ?: 0L }
        else -> { skipValue(); 0L }
    }

    /**
     * Stops reading once more than [limit] bytes arrive, a single text runs past [MAX_STRING_BYTES] or
     * nesting passes [MAX_DEPTH], so a runaway answer cannot fill memory before the parser sees it.
     */
    private class Capped(input: InputStream, private val limit: Long) : FilterInputStream(input) {
        private var count = 0L
        private var inString = false
        private var escaped = false
        private var stringBytes = 0
        private var depth = 0

        private fun scan(byte: Int) {
            if (inString) {
                when {
                    escaped -> escaped = false
                    byte == '\\'.code -> escaped = true
                    byte == '"'.code -> { inString = false; return }
                }
                if (++stringBytes > MAX_STRING_BYTES) throw IOException("A text in the answer is too long")
                return
            }
            when (byte) {
                '"'.code -> { inString = true; stringBytes = 0 }
                '{'.code, '['.code -> if (++depth > MAX_DEPTH) throw IOException("The answer is nested too deeply")
                '}'.code, ']'.code -> depth--
            }
        }

        private fun counted(n: Int): Int {
            if (n > 0) { count += n; if (count > limit) throw IOException("Answer larger than $limit bytes") }
            return n
        }

        override fun read(): Int = super.read().also { if (it >= 0) { counted(1); scan(it) } }
        override fun read(b: ByteArray, off: Int, len: Int): Int = counted(super.read(b, off, len)).also { n ->
            for (i in off until off + n) scan(b[i].toInt() and 0xFF)
        }
    }

    // Searching the index.

    /** The library's catch-all subjects; they name no topic, so they are not searched. */
    private val placeholderCategories = setOf("all", "other", "others")

    /** Whether [book] is in [language] (or every language) and in [categoryId] (or any). */
    fun onShelf(book: DiBook, language: String, categoryId: Int?): Boolean =
        (language == HadithCatalog.ALL_LANGUAGES || appLanguage(book.languageCode) == language) &&
            (categoryId == null || categoryId in book.categories)

    /**
     * What the search engine reads for each book, in the order of [books]: the title, the script
     * title, the author, and as other names the plain name and the book's subjects from
     * [categories], so "fiqh" or "seerat" finds the books of that subject below books named so.
     */
    fun searchEntries(books: List<DiBook>, categories: List<WorldCategory>): List<BookCatalogSearch.Entry> {
        val subjects = categories
            .filter { it.name.isNotBlank() && fold(it.name) !in placeholderCategories }
            .associate { it.id to it.name }
        return books.map { book ->
            BookCatalogSearch.Entry(
                title = book.title,
                nativeTitle = book.nativeName,
                author = writerName(book.writerId),
                other = listOf(book.normalName).filter(String::isNotBlank) + book.categories.mapNotNull(subjects::get)
            )
        }
    }

    /**
     * The search as it is remembered: case and spacing do not make a new search ("  Faizan " is
     * "faizan"); every other difference does, since the engine may read it.
     */
    fun searchKey(query: String): String = query.trim().lowercase(Locale.ROOT).replace(whitespace, " ")

    /**
     * The books of [books] in [language] and [categoryId] that match [query], in the order of
     * [books]; every one when [query] has no searchable word. [DiIndex.search] gives these same
     * books, ranked. Subjects are searched by name when their [categories] are given.
     */
    fun filter(
        books: List<DiBook>, query: String, language: String, categoryId: Int?,
        categories: List<WorldCategory> = emptyList()
    ): List<DiBook> {
        val index = BookCatalogSearch.Index(searchEntries(books, categories))
        val scores = shelfScores({ typos -> index.scores(query, typos) }) { scores ->
            books.indices.any { scores[it] > 0 && onShelf(books[it], language, categoryId) }
        }
        return books.filterIndexed { position, book ->
            onShelf(book, language, categoryId) && (scores == null || scores[position] > 0)
        }
    }

    /**
     * The search engine's scores as a search engine answers within one shelf: without typos when
     * any book of the shelf ([onShelfMatch]) matches that way, else with typos ("did you mean"), so
     * a word found in another language does not hide a one-letter-off match in the chosen one.
     * Null when the query has no searchable word.
     */
    internal inline fun shelfScores(score: (typos: Boolean) -> IntArray?, onShelfMatch: (IntArray) -> Boolean): IntArray? {
        val strict = score(false) ?: return null
        return if (onShelfMatch(strict)) strict else score(true)
    }

    /**
     * Matched books with their scores, already in [sort] order, best match first. The sort is
     * stable, so books that tie keep [sort]'s order. Most read ranks by score, so the closest
     * match leads and the most read wins a tie; the title and date orders rank only by
     * [BookCatalogSearch.tier], so the chosen order still holds inside each tier.
     */
    fun ranked(matched: List<Pair<DiBook, Int>>, sort: WorldSort): List<DiBook> =
        (if (sort == WorldSort.POPULAR) matched.sortedByDescending { it.second }
        else matched.sortedByDescending { BookCatalogSearch.tier(it.second) }).map { it.first }

    /** Stable sorts, so books that tie keep the library's own order. */
    fun sorted(books: List<DiBook>, sort: WorldSort): List<DiBook> = when (sort) {
        WorldSort.POPULAR -> books.sortedByDescending { it.readCount }
        WorldSort.TITLE_ASC -> books.sortedWith(compareBy(BookTitles.naturalOrder) { it.title })
        WorldSort.TITLE_DESC -> books.sortedWith(compareBy(BookTitles.naturalOrder) { it.title }).reversed()
        WorldSort.NEWEST -> books.sortedByDescending { it.date }
    }

    fun page(books: List<DiBook>, page: Int, size: Int): List<DiBook> {
        if (page < 1 || size < 1) return emptyList()
        val from = (page - 1).toLong() * size
        if (from >= books.size) return emptyList()
        return books.subList(from.toInt(), minOf(books.size, from.toInt() + size))
    }

    fun toItem(book: DiBook): WorldItem = WorldItem(
        identifier = "di:${book.id}",
        title = book.title,
        creator = writerName(book.writerId),
        languageCode = appLanguage(book.languageCode),
        downloads = book.readCount,
        nativeTitle = book.nativeName.takeIf { it.isNotBlank() && !it.equals(book.title, ignoreCase = true) && hasNonLatin(it) },
        pageCount = book.pages,
        remoteUrl = downloadUrl(book),
        sourceUrl = pageUrl(book),
        categories = book.categories
    )

    private fun hasNonLatin(text: String) = text.any { it.isLetter() && it > 'ɏ' && it !in 'Ḁ'..'ỿ' }

    // The index on disk.

    /** Writes [index] to a temporary file first, so a crash never leaves half an index behind. */
    fun writeCache(file: File, index: DiIndex) {
        file.parentFile?.mkdirs()
        val temporary = File(file.path + ".tmp")
        JsonWriter(OutputStreamWriter(temporary.outputStream().buffered(), Charsets.UTF_8)).use { writer ->
            writer.beginObject()
            writer.name("version").value(CACHE_VERSION.toLong())
            writer.name("updatedAt").value(index.updatedAt)
            writer.name("categories").beginArray()
            index.categories.forEach { category ->
                writer.beginObject().name("id").value(category.id.toLong()).name("name").value(category.name)
                    .name("count").value(category.count.toLong()).endObject()
            }
            writer.endArray()
            writer.name("books").beginArray()
            index.books.forEach { book ->
                writer.beginObject()
                    .name("i").value(book.id).name("l").value(book.languageCode).name("f").value(book.fileId)
                    .name("y").value(book.year.toLong()).name("p").value(book.pages.toLong()).name("s").value(book.slug)
                    .name("t").value(book.title).name("n").value(book.nativeName).name("m").value(book.normalName)
                    .name("w").value(book.writerId.toLong()).name("r").value(book.readCount).name("d").value(book.date)
                writer.name("c").beginArray()
                book.categories.forEach { writer.value(it.toLong()) }
                writer.endArray().endObject()
            }
            writer.endArray()
            writer.endObject()
        }
        if (!temporary.renameTo(file)) {
            file.delete()
            if (!temporary.renameTo(file)) { temporary.delete(); throw IOException("Could not save the library index") }
        }
    }

    /** The saved index, or null when there is none or it cannot be read completely. */
    fun readCache(file: File): DiIndex? = runCatching { file.inputStream() }.getOrNull()?.let { readCache(it) }

    /** An index in the cache's format, such as the copy shipped with the app; [input] is closed. */
    fun readCache(input: InputStream): DiIndex? = runCatching {
        JsonReader(InputStreamReader(input.buffered(), Charsets.UTF_8)).use { reader ->
            var version = 0L
            var updatedAt = 0L
            val categories = mutableListOf<WorldCategory>()
            val books = mutableListOf<DiBook>()
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "version" -> version = reader.nextLong()
                    "updatedAt" -> updatedAt = reader.nextLong()
                    "categories" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            var id = 0; var name = ""; var count = 0
                            reader.beginObject()
                            while (reader.hasNext()) when (reader.nextName()) {
                                "id" -> id = reader.nextInt()
                                "name" -> name = reader.nextString()
                                "count" -> count = reader.nextInt()
                                else -> reader.skipValue()
                            }
                            reader.endObject()
                            categories += WorldCategory(id, name, count)
                        }
                        reader.endArray()
                    }
                    "books" -> {
                        reader.beginArray()
                        while (reader.hasNext()) books += readCachedBook(reader)
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (version != CACHE_VERSION.toLong() || books.isEmpty()) null else DiIndex(books, categories, updatedAt)
        }
    }.getOrNull()

    private fun readCachedBook(reader: JsonReader): DiBook {
        var id = 0L; var language = ""; var fileId = 0L; var year = 0; var pages = 0; var slug = ""
        var title = ""; var native = ""; var normal = ""; var writer = 0; var reads = 0L; var date = ""
        val categories = mutableListOf<Int>()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "i" -> id = reader.nextLong()
                "l" -> language = reader.nextString()
                "f" -> fileId = reader.nextLong()
                "y" -> year = reader.nextInt()
                "p" -> pages = reader.nextInt()
                "s" -> slug = reader.nextString()
                "t" -> title = reader.nextString()
                "n" -> native = reader.nextString()
                "m" -> normal = reader.nextString()
                "w" -> writer = reader.nextInt()
                "r" -> reads = reader.nextLong()
                "d" -> date = reader.nextString()
                "c" -> { reader.beginArray(); while (reader.hasNext()) categories += reader.nextInt(); reader.endArray() }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        if (id <= 0 || fileId <= 0 || year <= 0 || slug.isBlank() || !languageCodePattern.matches(language)) {
            throw IOException("Damaged index entry")
        }
        return DiBook(id, language, fileId, year, pages, slug, title, native, normal, categories, writer, reads, date)
    }
}
