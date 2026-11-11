package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException

/** Reads di_search_fixture.json: five hits copied from the library's own search answer. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DawateIslamiIndexTest {
    @get:Rule val folder = TemporaryFolder()

    private val fixture: String = checkNotNull(javaClass.classLoader?.getResourceAsStream("di_search_fixture.json"))
        .use { it.readBytes().toString(Charsets.UTF_8) }

    private fun parse(
        json: String = fixture, maxBytes: Long = DawateIslamiIndex.MAX_BYTES, maxBooks: Int = DawateIslamiIndex.MAX_BOOKS,
        share: Double = DawateIslamiIndex.MIN_ACCEPTED_SHARE
    ) = DawateIslamiIndex.parseResponse(json.byteInputStream(), maxBytes, maxBooks, share, now = 1_000L)

    private val index by lazy { parse() }
    private fun book(id: Long) = index.books.single { it.id == id }

    @Test fun `every hit becomes a book keyed by the library's own number`() {
        assertEquals(listOf(1553L, 1053L, 7734L, 7711L, 3506L), index.books.map { it.id })
        assertEquals(1_000L, index.updatedAt)
        val bukhari = book(1553)
        assertEquals("ar", bukhari.languageCode)
        assertEquals(922L, bukhari.fileId)
        assertEquals(2014, bukhari.year)
        assertEquals(1035, bukhari.pages)
        assertEquals("sahih-al-bukhari-jild-1", bukhari.slug)
        assertEquals(listOf(4), bukhari.categories)
        assertEquals(423L, bukhari.readCount)
        assertEquals("2014-02-17T19:00:00.000Z", bukhari.date)
        // No published date: the day it entered the library is used instead.
        assertEquals("2026-09-14T10:48:51.000Z", book(7734).date)
    }

    @Test fun `the PDF and page addresses keep the library's language code`() {
        val sindhi = book(7734)
        assertEquals("https://data2.dawateislami.net/Data/Books/Download/si/pdf/2025/3495-1.pdf", DawateIslamiIndex.downloadUrl(sindhi))
        assertEquals("https://www.dawateislami.net/bookslibrary/si/bukhari-sharif-ki-pehli-aur-aakhri-hadees", DawateIslamiIndex.pageUrl(sindhi))
        assertEquals("sd", DawateIslamiIndex.appLanguage("si"))
        assertEquals("si", DawateIslamiIndex.appLanguage("sn"))
        assertEquals("ms", DawateIslamiIndex.appLanguage("my"))
        assertEquals("ha", DawateIslamiIndex.appLanguage("hu"))
        assertEquals("rd", DawateIslamiIndex.appLanguage("rd"))
        assertEquals("ur", DawateIslamiIndex.appLanguage("ur"))
    }

    @Test fun `categories are the union of every book's categories, largest first`() {
        assertEquals(
            listOf(WorldCategory(4, "Hadees Usool-e-Hadees", 4), WorldCategory(45, "Haftawar Rasail", 3), WorldCategory(5, "Fiqh-o-Usool-e-Fiqh", 1)),
            index.categories
        )
    }

    @Test fun `authors are named as the library names them and placeholders stay blank`() {
        assertEquals("Imam Muhammad Bin Ismail Bukhari", DawateIslamiIndex.writerName(18))
        assertEquals("Ameer-e-Ahl-e-Sunnat", DawateIslamiIndex.writerName(2))
        assertEquals("", DawateIslamiIndex.writerName(6))
        assertEquals("", DawateIslamiIndex.writerName(1))
        assertEquals("", DawateIslamiIndex.writerName(99_999))
    }

    @Test fun `an incomplete or oversized answer is rejected`() {
        val short = fixture.replace("\"value\": 5", "\"value\": 6")
        assertTrue(runCatching { parse(short) }.exceptionOrNull() is IOException)
        val lowerBound = fixture.replace("\"relation\": \"eq\"", "\"relation\": \"gte\"")
        assertTrue(runCatching { parse(lowerBound) }.exceptionOrNull() is IOException)
        assertTrue(runCatching { parse("""{"total":{"value":0,"relation":"eq"},"hits":[]}""") }.exceptionOrNull() is IOException)
        assertTrue(runCatching { parse(maxBytes = 1_000) }.exceptionOrNull() is IOException)
        assertTrue(runCatching { parse(maxBooks = 4) }.exceptionOrNull() is IOException)
        val repeated = fixture.replace("\"id\": 1053,", "\"id\": 1553,")
        assertTrue(runCatching { parse(repeated) }.exceptionOrNull() is IOException)
    }

    @Test fun `a hit without a publication folder is skipped, not guessed`() {
        val noFolder = fixture.replace("https://data2.dawateislami.net/Data/Books/Read/rd/2018/1273/st1273.jpg", "")
        assertEquals(listOf(1553L, 1053L, 7734L, 7711L), parse(noFolder, share = 0.5).books.map { it.id })
    }

    @Test fun `an answer with too many unreadable books is rejected so no saved book is dropped on it`() {
        // One of five unreadable is 80%, below the 99% a real answer meets (7,354 of 7,355).
        val noFolder = fixture.replace("https://data2.dawateislami.net/Data/Books/Read/rd/2018/1273/st1273.jpg", "")
        assertTrue(runCatching { parse(noFolder) }.exceptionOrNull() is IOException)
    }

    @Test fun `a runaway text or nesting stops the read`() {
        val longText = fixture.replace("\"Hadith\"", "\"" + "x".repeat(DawateIslamiIndex.MAX_STRING_BYTES + 1) + "\"")
        assertTrue(runCatching { parse(longText) }.exceptionOrNull() is IOException)
        val deep = fixture.replaceFirst("\"max_score\": 0.0", "\"max_score\": " + "[".repeat(DawateIslamiIndex.MAX_DEPTH + 1) + "]".repeat(DawateIslamiIndex.MAX_DEPTH + 1))
        assertTrue(runCatching { parse(deep) }.exceptionOrNull() is IOException)
        // Escaped quotes inside a text do not end it early.
        val escaped = fixture.replace("\"Hadith\"", "\"say \\\"Hadith\\\" {[\"")
        assertEquals(5, parse(escaped).books.size)
    }

    private fun ids(query: String, language: String = HadithCatalog.ALL_LANGUAGES, category: Int? = null) =
        DawateIslamiIndex.filter(index.books, query, language, category, index.categories).map { it.id }

    /** The fixture searched as the World Library screen searches it: best match first. */
    private fun ranked(query: String, sort: WorldSort = WorldSort.POPULAR) =
        index.search(query, HadithCatalog.ALL_LANGUAGES, null, sort).map { it.id }

    @Test fun `search matches every word in title, script name, plain name or author without accents`() {
        assertEquals(listOf(1553L, 7711L), ids("sahih bukhari"))
        assertEquals(listOf(7711L), ids("ṣaḥīḥ FIRST"))
        assertEquals(listOf(1553L), ids("صحيح البخاري"))
        assertEquals(listOf(1053L), ids("مرآۃ المناجیح"))
        assertEquals(listOf(1053L), ids("naeemi"))
        assertEquals(listOf(7734L), ids("bukhari", language = "sd"))
        assertEquals(listOf(3506L), ids("", category = 5))
        assertEquals(listOf(7734L, 7711L), ids("bukhari", category = 45))
        assertEquals(5, ids("   ").size)
        // A word the library does not hold anywhere, not even one letter off, finds nothing.
        assertTrue(ids("xqjzvw").isEmpty())
    }

    @Test fun `a typo still finds the book`() {
        // "Khiyanat" with its second "a" missing.
        assertEquals(listOf(3506L), ids("khiyant"))
        assertEquals(listOf(3506L), ids("Maal e Virasat main Khiyant"))
    }

    @Test fun `a joined spelling finds the book whose title splits it`() {
        // Miraat-ul-Manajeeh typed as people write it, with "ul" joined on.
        assertEquals(listOf(1053L), ids("miraatul manajeeh"))
        // Maal e Virasat typed as "maale virasat" and as one word.
        assertEquals(listOf(3506L), ids("maale virasat"))
        assertEquals(listOf(3506L), ids("maalevirasat"))
    }

    @Test fun `a word typed in Devanagari finds the Roman spelling`() {
        // बुखारी sounds as bukhari: the author of 1553, the title of 7734 and the plain name of 7711.
        assertEquals(listOf(1553L, 7734L, 7711L), ids("बुखारी"))
        assertEquals(ids("bukhari"), ids("बुखारी"))
    }

    @Test fun `a subject name finds its books, below books with that word in the title`() {
        // Only 3506 is in Fiqh-o-Usool-e-Fiqh; its title does not say so.
        val fiqh = ranked("fiqh")
        assertTrue(3506L in fiqh)
        assertTrue(1053L !in fiqh && 1553L !in fiqh)
        // Haftawar Rasail holds 7734, 7711 and 3506.
        assertEquals(listOf(7734L, 7711L, 3506L), ids("rasail"))
        assertEquals(listOf(7734L, 7711L, 3506L), ids("Haftawar"))
        // The subject narrows a title word: of the Bukhari books, 1553 is not a weekly booklet.
        assertEquals(listOf(7734L, 7711L), ids("bukhari rasail"))
        // Every Hadees Usool-e-Hadees book matches "hadees"; 7734 also has the word in its title.
        val found = ranked("hadees")
        assertEquals(setOf(1553L, 1053L, 7734L, 7711L), found.toSet())
        assertTrue(found.indexOf(7734L) < found.indexOf(1053L))
        assertTrue(found.indexOf(7734L) < found.indexOf(1553L))
    }

    @Test fun `a catch-all subject is not searched by its name`() {
        val others = DiIndex(index.books, listOf(WorldCategory(4, "Others", 4), WorldCategory(45, "All", 3)), 0L)
        assertTrue(others.search("others", HadithCatalog.ALL_LANGUAGES, null, WorldSort.POPULAR).isEmpty())
        // A real subject is searched by its name.
        val named = DiIndex(index.books, listOf(WorldCategory(4, "Seerat", 4)), 0L)
        assertEquals(
            setOf(1553L, 1053L, 7734L, 7711L),
            named.search("seerat", HadithCatalog.ALL_LANGUAGES, null, WorldSort.POPULAR).map { it.id }.toSet()
        )
    }

    @Test fun `most read puts the closest match first and reads only break ties`() {
        // By reads alone 1553 (423 reads) leads 7734 (none); 7734 carries "bukhari" in its title, 1553 only as author.
        val found = ranked("bukhari")
        assertEquals(setOf(1553L, 7734L, 7711L), found.toSet())
        assertEquals(7734L, found.first())
        // Books that match equally (the same words throughout) keep the most read first.
        val same = listOf(5L to 3L, 6L to 40L, 7L to 0L, 8L to 12L).map { (id, reads) ->
            DiBook(id, "ur", 100 + id, 2020, 10, "namaz-$id", "Namaz ka Tariqa", "", "", listOf(1), 2, reads, "2024-01-01")
        }
        val tied = DiIndex(same, emptyList(), 0L).search("namaz tariqa", HadithCatalog.ALL_LANGUAGES, null, WorldSort.POPULAR)
        assertEquals(listOf(6L, 8L, 5L, 7L), tied.map { it.id })
    }

    @Test fun `a typo match in the chosen language is not hidden by an exact match in another`() {
        val books = listOf(
            DiBook(1, "ur", 101, 2020, 10, "bahar-ur", "Bahar e Shariat", "", "", listOf(1), 2, 5, "2024-01-01"),
            DiBook(2, "hi", 102, 2020, 10, "bahr-hi", "Bahr e Shariat", "", "", listOf(1), 2, 5, "2024-01-01")
        )
        val list = DiIndex(books, emptyList(), 0L)
        // Every language: the exact title is the answer, the one-letter-off title is not listed with it.
        assertEquals(listOf(1L), list.search("bahar shariat", HadithCatalog.ALL_LANGUAGES, null, WorldSort.POPULAR).map { it.id })
        // Hindi alone has no exact match, so its one-letter-off title is offered.
        assertEquals(listOf(2L), list.search("bahar shariat", "hi", null, WorldSort.POPULAR).map { it.id })
        assertEquals(listOf(2L), DawateIslamiIndex.filter(books, "bahar shariat", "hi", null).map { it.id })
        assertEquals(listOf(1L), DawateIslamiIndex.filter(books, "bahar shariat", HadithCatalog.ALL_LANGUAGES, null).map { it.id })
    }

    @Test fun `title and date orders keep their order inside each tier of match`() {
        val scores = BookCatalogSearch.Index(DawateIslamiIndex.searchEntries(index.books, index.categories)).scores("hadees")!!
        val tierOf = index.books.withIndex().associate { (place, book) -> book.id to BookCatalogSearch.tier(scores[place]) }
        for (sort in listOf(WorldSort.TITLE_ASC, WorldSort.TITLE_DESC, WorldSort.NEWEST)) {
            val found = ranked("hadees", sort)
            val chosen = index.sorted(sort).map { it.id }
            // Better tiers first; inside a tier the books stand in the chosen order.
            assertEquals("$sort", found.sortedByDescending { tierOf.getValue(it) }, found)
            found.groupBy { tierOf.getValue(it) }.values.forEach { tier ->
                assertEquals("$sort", tier.sortedBy(chosen::indexOf), tier)
            }
            // The title match outranks books that only share the subject.
            assertTrue("$sort", found.indexOf(7734L) < found.indexOf(1053L))
        }
        // Every "faizan" title is the same match, so A-Z holds exactly.
        val many = DiIndex(manyBooks(), emptyList(), 0L)
        val faizan = many.search("faizan", HadithCatalog.ALL_LANGUAGES, null, WorldSort.TITLE_ASC)
        assertEquals(DawateIslamiIndex.sorted(faizan, WorldSort.TITLE_ASC).map { it.id }, faizan.map { it.id })
    }

    @Test fun `a blank query lists the shelf in the chosen order, as before`() {
        val many = DiIndex(manyBooks(), emptyList(), 0L)
        for (sort in WorldSort.entries) for (language in listOf(HadithCatalog.ALL_LANGUAGES, "ur", "sd")) for (category in listOf(null, 2)) {
            val expected = many.sorted(sort)
                .filter { DawateIslamiIndex.onShelf(it, language, category) }.map { it.id }
            for (blank in listOf("", "   ", " - ")) {
                assertEquals("$sort/$language/$category/'$blank'", expected, many.search(blank, language, category, sort).map { it.id })
            }
        }
    }

    @Test fun `sorting covers the whole index`() {
        fun ids(sort: WorldSort) = DawateIslamiIndex.sorted(index.books, sort).map { it.id }
        assertEquals(listOf(1053L, 1553L, 7734L, 7711L, 3506L), ids(WorldSort.POPULAR))
        assertEquals(listOf(7734L, 7711L, 3506L, 1553L, 1053L), ids(WorldSort.NEWEST))
        val titles = DawateIslamiIndex.sorted(index.books, WorldSort.TITLE_ASC).map { it.title }
        assertEquals(titles.sortedWith(BookTitles.naturalOrder), titles)
        assertEquals(ids(WorldSort.TITLE_ASC).reversed(), ids(WorldSort.TITLE_DESC))
    }

    @Test fun `pages split the filtered list`() {
        assertEquals(listOf(1553L, 1053L), DawateIslamiIndex.page(index.books, 1, 2).map { it.id })
        assertEquals(listOf(3506L), DawateIslamiIndex.page(index.books, 3, 2).map { it.id })
        assertTrue(DawateIslamiIndex.page(index.books, 4, 2).isEmpty())
        assertTrue(DawateIslamiIndex.page(index.books, 0, 2).isEmpty())
    }

    @Test fun `a book becomes a World Library item with its own addresses`() {
        val item = DawateIslamiIndex.toItem(book(7734))
        assertEquals("di:7734", item.identifier)
        assertEquals("Bukhari Sharif Ki Pehli Aur Aakhri Hadees", item.title)
        assertEquals("بخاري شريف جي پهرين ۽ آخري حديث", item.nativeTitle)
        assertEquals("Ameer-e-Ahl-e-Sunnat", item.creator)
        assertEquals("sd", item.languageCode)
        assertEquals(29, item.pageCount)
        assertEquals(listOf(4, 45), item.categories)
        assertEquals(DawateIslamiIndex.downloadUrl(book(7734)), item.remoteUrl)
        assertEquals(DawateIslamiIndex.pageUrl(book(7734)), item.sourceUrl)
        // The same words in another case are not a second title.
        assertNull(DawateIslamiIndex.toItem(book(7711)).nativeTitle)
        assertNull(DawateIslamiIndex.toItem(book(1553)).nativeTitle)
    }

    @Test fun `the cache keeps every field and is replaced whole`() {
        val file = File(folder.root, "di-index.json")
        DawateIslamiIndex.writeCache(file, index)
        val read = DawateIslamiIndex.readCache(file)!!
        assertEquals(index.books, read.books)
        assertEquals(index.categories, read.categories)
        assertEquals(index.updatedAt, read.updatedAt)
        assertTrue(folder.root.listFiles()!!.none { it.name.endsWith(".tmp") })
        file.writeText("{ broken")
        assertNull(DawateIslamiIndex.readCache(file))
        assertNull(DawateIslamiIndex.readCache(File(folder.root, "missing.json")))
    }

    @Test fun `the cache also reads from a stream, as the copy shipped with the app is`() {
        val file = File(folder.root, "seed.json")
        DawateIslamiIndex.writeCache(file, index)
        val read = DawateIslamiIndex.readCache(file.inputStream())!!
        assertEquals(index.books, read.books)
        assertEquals(index.categories, read.categories)
        assertEquals(index.updatedAt, read.updatedAt)
        assertNull(DawateIslamiIndex.readCache("{ broken".byteInputStream()))
        assertNull(DawateIslamiIndex.readCache("""{"version":2,"updatedAt":1,"categories":[],"books":[]}""".byteInputStream()))
    }

    /** Many books with tied read counts, titles that differ only in case, and repeated dates. */
    private fun manyBooks(): List<DiBook> = (1..90).map { n ->
        DiBook(
            id = n.toLong(), languageCode = listOf("ur", "en", "si")[n % 3], fileId = 100L + n, year = 2020, pages = 10,
            slug = "book-$n",
            title = listOf("Faizan e Sunnat ${n % 7}", "faizan E sunnat ${n % 7}", "Namaz ka Tariqa", "Bahar-e-Shariat ${n % 11}")[n % 4],
            nativeName = "", normalName = if (n % 5 == 0) "Madani Phool" else "", categories = listOf(n % 4, 10 + n % 3),
            writerId = listOf(2, 3, 8)[n % 3], readCount = (n % 3).toLong(), date = "2024-0${1 + n % 4}-01"
        )
    }

    /**
     * The contract of [DiIndex.search]: the same books as filtering, each once; a blank query in
     * the chosen order exactly as before; otherwise ranked by score (most read) or by tier (the
     * other orders), with the chosen order kept among books that rank the same.
     */
    @Test fun `searching gives the filtered books, best match first, then the chosen order`() {
        val subjects = listOf(WorldCategory(0, "Seerat", 23), WorldCategory(1, "Aqaid", 22), WorldCategory(10, "Others", 30))
        val many = DiIndex(manyBooks(), subjects, 0L)
        val engine = BookCatalogSearch.Index(DawateIslamiIndex.searchEntries(many.books, many.categories))
        val place = many.books.withIndex().associate { (position, book) -> book.id to position }
        val queries = listOf(
            "", "  ", "faizan", "SUNNAT 3", "madani", "bahar 1", "ameer", "zzz", "bahare shariat", "faizane sunnat",
            "namaaz", "नमाज़", "tariqa namaz", "faizn", "seerat", "aqaid faizan", "others"
        )
        val languages = listOf(HadithCatalog.ALL_LANGUAGES, "ur", "en", "sd")
        val categories = listOf(null, 0, 2, 11)
        for (query in queries) {
            val scores = engine.scores(query)
            for (sort in WorldSort.entries) for (language in languages) for (category in categories) {
                val label = "$sort/$query/$language/$category"
                val filtered = DawateIslamiIndex.filter(many.books, query, language, category, many.categories)
                val found = many.search(query, language, category, sort).map { it.id }
                assertEquals(label, found.size, found.toSet().size)
                assertEquals(label, filtered.map { it.id }.toSet(), found.toSet())
                val inOrder = DawateIslamiIndex.sorted(filtered, sort)
                val expected = if (scores == null) inOrder else inOrder.sortedByDescending { book ->
                    val score = scores[place.getValue(book.id)]
                    if (sort == WorldSort.POPULAR) score else BookCatalogSearch.tier(score)
                }
                assertEquals(label, expected.map { it.id }, found)
            }
        }
        // The catch-all subject finds nothing by its name; the real ones do.
        assertTrue(many.search("others", HadithCatalog.ALL_LANGUAGES, null, WorldSort.POPULAR).isEmpty())
        assertEquals(
            many.books.filter { 0 in it.categories }.map { it.id }.toSet(),
            many.search("seerat", HadithCatalog.ALL_LANGUAGES, null, WorldSort.POPULAR).map { it.id }.toSet()
        )
    }

    @Test fun `joined, split, sound-alike and misspelt words find the same titles`() {
        val many = DiIndex(manyBooks(), emptyList(), 0L)
        fun titles(query: String) = many.search(query, HadithCatalog.ALL_LANGUAGES, null, WorldSort.POPULAR).map { it.title }.toSet()
        val bahar = manyBooks().filter { it.title.startsWith("Bahar-e-Shariat") }.map { it.title }.toSet()
        assertEquals(bahar, titles("bahar e shariat"))
        assertEquals(bahar, titles("bahare shariat"))
        assertEquals(bahar, titles("baharishariat"))
        val namaz = setOf("Namaz ka Tariqa")
        assertEquals(namaz, titles("namaz"))
        assertEquals(namaz, titles("namaaz"))
        assertEquals(namaz, titles("नमाज़"))
        assertEquals(namaz, titles("tariqa namaz"))
        val faizan = manyBooks().filter { it.title.startsWith("faizan", ignoreCase = true) }.map { it.title }.toSet()
        assertEquals(faizan, titles("faizan sunnat"))
        assertEquals(faizan, titles("faizane sunnat"))
        assertEquals(faizan, titles("faizan sunat"))
    }

    @Test fun `each order is sorted once and the next page reuses the last search`() {
        val many = DiIndex(manyBooks(), emptyList(), 0L)
        assertSame(many.sorted(WorldSort.TITLE_ASC), many.sorted(WorldSort.TITLE_ASC))
        val first = many.search("Faizan", "ur", null, WorldSort.POPULAR)
        assertSame(first, many.search("  faizan ", "ur", null, WorldSort.POPULAR))
        assertSame(first, many.search("FAIZAN", "ur", null, WorldSort.POPULAR))
        assertNotSame(first, many.search("faizan", "en", null, WorldSort.POPULAR))
        assertNotSame(first, many.search("faizan", "ur", null, WorldSort.TITLE_ASC))
        // Warming builds the search engine ahead of the first search and changes no result.
        many.warmSearchText()
        assertEquals(first.map { it.id }, many.search("faizan", "ur", null, WorldSort.POPULAR).map { it.id })
        val warm = DiIndex(manyBooks(), emptyList(), 0L).apply { warmSearchText() }
        assertEquals(first.map { it.id }, warm.search("faizan", "ur", null, WorldSort.POPULAR).map { it.id })
    }
}
