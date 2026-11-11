package com.example.ui.screens

import com.example.hadith.HadithBook
import com.example.hadith.HadithCatalog
import com.example.hadith.IslamicBooksCatalog.Shelf
import com.example.hadith.IslamicBooksCatalog.Work

/** Which featured shelves the Islamic Books home page shows: everything, or only the Ladies / Muslimah titles. */
internal enum class BooksFilter {
    ALL, MUSLIMAH;

    companion object {
        /** The stored choice, or every book when nothing (or an unknown name) was stored. */
        fun fromName(name: String?): BooksFilter = entries.firstOrNull { it.name == name } ?: ALL
    }
}

/**
 * What the Islamic Books home page lists, kept apart from the screen so it is unit-tested on the
 * JVM. Featured works and the full library are one library: a featured book has the same id as
 * the same book in the full list, so it is matched to its work by id, never by collection.
 */
internal object IslamicBooksHome {
    /** The classical and Ladies / Muslimah shelves as they are shown. */
    data class Shelves(val classical: List<Work>, val muslimah: List<Work>)

    /**
     * Books opened before, the one read most recently first, each once (a featured book can also
     * be on the saved shelf), at most [limit] of them.
     */
    fun continueReading(candidates: List<HadithBook>, limit: Int = 4, lastOpened: (HadithBook) -> Long): List<HadithBook> =
        candidates.distinctBy { it.id }
            .map { it to lastOpened(it) }
            .filter { it.second > 0L }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }

    /** The work [book] belongs to, by the book's id. */
    fun workOf(book: HadithBook, works: List<Work>): Work? = works.firstOrNull { work -> work.books.any { it.id == book.id } }

    /** [books] as the shelf filter shows them: all, or only books of Ladies / Muslimah works. */
    fun forFilter(books: List<HadithBook>, filter: BooksFilter, works: List<Work>): List<HadithBook> =
        if (filter == BooksFilter.ALL) books else books.filter { workOf(it, works)?.shelf == Shelf.MUSLIMAH }

    /** Every work for every language, or the works that have an edition in [language]. */
    fun forLanguage(works: List<Work>, language: String): List<Work> =
        if (language == HadithCatalog.ALL_LANGUAGES) works else works.filter { language in it.languages }

    /** The two featured shelves; the Ladies / Muslimah filter hides the classical one. */
    fun shelves(works: List<Work>, filter: BooksFilter): Shelves = Shelves(
        classical = if (filter == BooksFilter.ALL) works.filter { it.shelf == Shelf.CLASSICAL } else emptyList(),
        muslimah = works.filter { it.shelf == Shelf.MUSLIMAH }
    )

    /**
     * The featured works a search or the shelves may show under the chosen pickers: those with an
     * edition in the chosen language, and none while a category narrows the list (featured works
     * are not listed by category).
     */
    fun featuredFor(works: List<Work>, language: String, categoryActive: Boolean): List<Work> =
        if (categoryActive) emptyList() else forLanguage(works, language)
}
