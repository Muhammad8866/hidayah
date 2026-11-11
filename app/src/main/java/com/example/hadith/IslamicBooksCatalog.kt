package com.example.hadith

/**
 * The curated shelves of the Islamic Books library: Bahar-e-Shariat and the Ladies / Muslimah
 * shelf. Every book is an edition of the dawateislami.net library list, with the very identity
 * [WorldLibrary.diBook] gives it (id "di-n", collection "wl-di:n"), so a book downloaded, read or
 * bookmarked from a shelf is the same book in the full list. Nothing comes from any other site.
 * The rows are fixed, so the shelves show before the list has loaded; [availableWorks] leaves out
 * a book the loaded list no longer has. IslamicBooksCatalogTest checks every row against the list
 * shipped with the app.
 */
object IslamicBooksCatalog {
    const val BAHAR = "bahar_e_shariat"
    private const val BAHAR_NAME = "Bahar-e-Shariat"

    private const val DOWNLOAD = "https://data2.dawateislami.net/Data/Books/Download"

    /**
     * One edition: a row of the library list ([diId], kept under the library's own [libraryCode])
     * and how a shelf shows it. [legacyId] is the id this same PDF had on the shelf before it was
     * read from the library list; null for an edition the shelf did not have.
     */
    internal class Edition(
        val workId: String,
        val workName: String,
        val diId: Long,
        val libraryCode: String,
        val fileId: Int,
        val year: Int,
        val pages: Int,
        val slug: String,
        val title: String,
        val nativeTitle: String?,
        val volume: Int = 1,
        val volumeCount: Int = 1,
        val volumeTitle: String? = null,
        val legacyId: String? = null
    ) {
        val languageCode: String get() = DawateIslamiIndex.appLanguage(libraryCode)

        /**
         * The name the page search index (built from the earlier shelf's PDFs) gives this PDF, so
         * its indexed pages still open it. Null when the shelf did not have this PDF.
         */
        val legacyAsset: String? get() = legacyId?.let { "$languageCode-${workId.replace('_', '-')}-v$volume" }

        fun book(): HadithBook = HadithBook(
            id = "di-$diId",
            collectionId = "wl-di:$diId",
            collectionName = workName,
            romanTitle = title,
            nativeTitle = nativeTitle,
            languageCode = languageCode,
            languageName = WorldLibrary.languageName(languageCode).orEmpty(),
            pageCount = pages,
            publicationYear = year,
            fileId = fileId,
            slug = "di:$diId",
            volume = volume,
            volumeCount = volumeCount,
            remoteUrl = "$DOWNLOAD/$libraryCode/pdf/$year/$fileId-1.pdf",
            sourceUrl = DawateIslamiIndex.LIBRARY_URL + libraryCode + "/" + slug,
            volumeTitle = volumeTitle,
            mirrored = false
        )
    }

    private val jilds = listOf(1 to "A", 1 to "B", 2 to "A", 2 to "B", 3 to "A", 3 to "B")

    /** Maktaba-tul-Madinah's Urdu edition: three jild, each published as two PDFs. */
    private val baharUrdu = jilds.mapIndexed { index, (jild, part) ->
        Edition(
            BAHAR, BAHAR_NAME, diId = 1885L + index, libraryCode = "ur", fileId = 1172 + index, year = 2014,
            pages = listOf(740, 605, 540, 765, 787, 554)[index], slug = "bahar-e-shariat-jild-$jild-${part.lowercase()}",
            title = "$BAHAR_NAME — Urdu · Jild $jild, Part $part", nativeTitle = "بہارِ شریعت",
            volume = index + 1, volumeCount = 6, volumeTitle = "Jild $jild · Part $part", legacyId = "bk-bahar-ur-${index + 1}"
        )
    }

    /** The Hindi edition; only Jild 1 is published in the library so far. */
    private val baharHindi = listOf(Triple(8807L, "A", 756), Triple(8796L, "B", 609)).mapIndexed { index, (diId, part, pages) ->
        Edition(
            BAHAR, BAHAR_NAME, diId = diId, libraryCode = "hi", fileId = 1172 + index, year = 2026, pages = pages,
            slug = "bahar-e-shariat-jild-1-${part.lowercase()}", title = "$BAHAR_NAME — Hindi · Jild 1, Part $part",
            nativeTitle = "बहारे शरीअत", volume = index + 1, volumeCount = 6, volumeTitle = "Jild 1 · Part $part"
        )
    }

    private val editions: List<Edition> = baharUrdu + baharHindi + MuslimahBooksCatalog.editions

    /** Every curated edition, the classical shelf first. */
    val books: List<HadithBook> = baharUrdu.map(Edition::book) + baharHindi.map(Edition::book) + MuslimahBooksCatalog.books

    /** The classical shelf's books (Bahar-e-Shariat), without the Ladies / Muslimah shelf. */
    val classicalBooks: List<HadithBook> = books.filter { book -> book.collectionName == BAHAR_NAME }

    /** Which shelf a work sits on in the Islamic Books library. */
    enum class Shelf { CLASSICAL, MUSLIMAH }

    /**
     * One work (a title in every edition and volume the library has). [nativeTitle] and [author]
     * are catalogue metadata; blank where the publisher names none.
     */
    data class Work(
        val id: String,
        val name: String,
        val nativeTitle: String,
        val author: String,
        val shelf: Shelf,
        val books: List<HadithBook>
    ) {
        /** Edition languages, Urdu first, then the order the catalogue lists them in. */
        val languages: List<String> = books.map { it.languageCode }.distinct()
            .sortedBy { if (it == "ur") 0 else 1 }

        fun editions(language: String): List<HadithBook> =
            books.filter { it.languageCode == language }.sortedBy { it.volume }

        /** What search reads: the name, script title, author, and every edition's own titles. */
        val searchEntry: BookCatalogSearch.Entry by lazy {
            BookCatalogSearch.Entry(
                title = name,
                nativeTitle = nativeTitle,
                author = author,
                other = books.flatMap { listOfNotNull(it.romanTitle, it.nativeTitle) }.distinct()
            )
        }
    }

    private val authors = mapOf(
        BAHAR to "Sadr ash-Shariah Mufti Amjad Ali Azmi · d. 1367 AH",
        "muslimah_islami_behno_ki_namaz" to "Ameer-e-Ahle Sunnat Maulana Muhammad Ilyas Attar Qadri",
        "muslimah_jannati_zevar" to "Allama Abdul Mustafa Azmi",
        "muslimah_sunni_bahishti_zewar" to "Khalil-e-Millat Mufti Muhammad Khalil Khan Qadri Barkati",
        "muslimah_chadar_aur_char_diwari" to "Khalil-e-Millat Mufti Muhammad Khalil Khan Qadri Barkati",
        "muslimah_aurat_aur_parda" to "Professor Dr. Muhammad Masood Ahmad",
        "muslimah_jannati_khawateen" to "Maulana Abu Usaid Ubaid Raza Attari Madani"
    )

    /** Every work in catalogue order: the classical shelf first, then Ladies / Muslimah. */
    val works: List<Work> = editions.groupBy(Edition::workId).map { (id, rows) ->
        val editionBooks = rows.map { row -> books.first { it.id == "di-${row.diId}" } }
        val urdu = editionBooks.firstOrNull { it.languageCode == "ur" }
        Work(
            id = id,
            name = rows.first().workName,
            nativeTitle = (urdu ?: editionBooks.first()).nativeTitle ?: rows.first().workName,
            author = authors[id].orEmpty(),
            shelf = if (id == BAHAR) Shelf.CLASSICAL else Shelf.MUSLIMAH,
            books = editionBooks
        )
    }

    /**
     * The works with only the books the loaded library list still has; a work left with none is
     * dropped. Before any list has loaded, [isListed] answers true and every work is shown.
     */
    fun availableWorks(isListed: (Long) -> Boolean = WorldLibrary::isListed): List<Work> = works.mapNotNull { work ->
        val kept = work.books.filter { isListed(it.id.removePrefix("di-").toLong()) }
        when {
            kept.isEmpty() -> null
            kept.size == work.books.size -> work
            else -> work.copy(books = kept)
        }
    }

    /**
     * The works of [among] that match [query], best match first, in any spelling or script (see
     * [BookCatalogSearch]). Empty when [query] holds no searchable word, such as a blank query:
     * the home page shows its shelves then, not results.
     */
    fun search(query: String, among: List<Work> = availableWorks()): List<Work> {
        val scores = BookCatalogSearch.Index(among.map { it.searchEntry }).bestScores(query) ?: return emptyList()
        return among.indices.filter { scores[it] > 0 }.sortedByDescending { scores[it] }.map(among::get)
    }

    fun work(id: String?): Work? = works.firstOrNull { it.id == id }

    /** The work a curated book belongs to, by the book's id ("di-n"); null for any other book. */
    fun workOf(bookId: String?): Work? = works.firstOrNull { work -> work.books.any { it.id == bookId } }

    fun book(id: String?): HadithBook? = books.firstOrNull { it.id == id }

    /** The page search index's name for a curated book's PDF, or null when it never indexed it. */
    fun legacyAsset(bookId: String): String? = editions.firstOrNull { "di-${it.diId}" == bookId }?.legacyAsset

    /** Shelf ids of earlier versions whose PDF is now read from the library list, with the book it became. */
    internal val legacyMoves: Map<String, HadithBook> by lazy {
        editions.mapNotNull { row -> row.legacyId?.let { it to books.first { book -> book.id == "di-${row.diId}" } } }.toMap()
    }

    /**
     * Shelf editions of earlier versions that the library does not have (they came from the
     * Internet Archive or another copy), with the language their files were saved under. Their
     * downloads and reading state are deleted once by [LibraryMigration].
     */
    internal val removedEditions: List<Pair<String, String>> =
        listOf("bk-bahar-hi" to "hi") +
            listOf(1, 2, 3, 4, 5, 6, 16).map { "bk-bahar-en-$it" to "en" } +
            listOf("bk-anwar-ur" to "ur", "bk-anwar-hi" to "hi", "bk-anwar-kn" to "kn") +
            listOf("bk-shawahid-ur" to "ur", "bk-shawahid-fa" to "fa") +
            (1..3).map { "bk-maarij-ur-$it" to "ur" } + listOf("bk-maarij-fa" to "fa") +
            listOf("bk-muslimah-islami-behno-ki-namaz-hi-2011" to "hi")

    /** App languages whose readers usually read another script's edition. */
    private val readerScript = mapOf("hinglish" to "hi", "ks" to "ur", "sd" to "sd", "pa" to "ur", "ps" to "ur")

    /**
     * The edition language to show first for [work]: the reader's saved choice, then the app's
     * language, then Urdu (the original language of most of these works), then the first edition.
     */
    fun preferredLanguage(work: Work, saved: String?, appLanguage: String): String {
        val available = work.languages
        return listOfNotNull(saved, appLanguage, readerScript[appLanguage], "ur")
            .firstOrNull { it in available } ?: available.first()
    }
}
