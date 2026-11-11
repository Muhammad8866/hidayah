package com.example.ui.screens

import com.example.hadith.HadithBook
import com.example.hadith.HadithCatalog
import com.example.hadith.IslamicBooksCatalog.Shelf
import com.example.hadith.IslamicBooksCatalog.Work
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/** The merged Islamic Books home page: what it lists, in which order, for each shelf and filter. */
class IslamicBooksHomeTest {
    private fun book(id: String, language: String = "ur", collection: String = "wl-di:$id") = HadithBook(
        id = "di-$id", collectionId = collection, collectionName = "Book $id", romanTitle = "Book $id",
        languageCode = language, pageCount = 0, fileId = 0, slug = "di:$id", volume = 1, volumeCount = 1
    )

    private val baharUr = book("1885")
    private val baharHi = book("8807", language = "hi")
    private val namazUr = book("573")
    private val namazEn = book("1827", language = "en")
    private val parda = book("3219")
    private val other = book("42")

    private val bahar = Work("bahar_e_shariat", "Bahar-e-Shariat", "بہارِ شریعت", "", Shelf.CLASSICAL, listOf(baharUr, baharHi))
    private val namaz = Work("muslimah_namaz", "Salah for Islamic Sisters", "اسلامی بہنوں کی نماز", "", Shelf.MUSLIMAH, listOf(namazUr, namazEn))
    private val pardaWork = Work("muslimah_parda", "Aurat Aur Parda", "عورت اور پردہ", "", Shelf.MUSLIMAH, listOf(parda))
    private val works = listOf(bahar, namaz, pardaWork)

    // region Continue reading

    @Test fun `books read most recently come first and unread books are left out`() {
        val opened = mapOf(baharUr.id to 10L, namazEn.id to 30L, other.id to 20L)
        val found = IslamicBooksHome.continueReading(listOf(baharUr, baharHi, namazEn, other)) { opened[it.id] ?: 0L }
        assertEquals(listOf(namazEn, other, baharUr), found)
    }

    @Test fun `a book listed twice (featured and saved) is shown once`() {
        val opened = mapOf(baharUr.id to 10L)
        val found = IslamicBooksHome.continueReading(listOf(baharUr, baharUr.copy(romanTitle = "saved copy"))) { opened[it.id] ?: 0L }
        assertEquals(listOf(baharUr.id), found.map { it.id })
    }

    @Test fun `at most the limit is shown`() {
        val many = (1..10).map { book("$it") }
        val found = IslamicBooksHome.continueReading(many, limit = 4) { it.id.removePrefix("di-").toLong() }
        assertEquals(listOf("di-10", "di-9", "di-8", "di-7"), found.map { it.id })
    }

    @Test fun `nothing opened means nothing to continue`() {
        assertEquals(emptyList<HadithBook>(), IslamicBooksHome.continueReading(listOf(baharUr, parda)) { 0L })
        assertEquals(emptyList<HadithBook>(), IslamicBooksHome.continueReading(emptyList()) { 1L })
    }

    @Test fun `the Muslimah filter keeps only books of Muslimah works`() {
        val recent = listOf(baharUr, namazEn, other, parda)
        assertEquals(recent, IslamicBooksHome.forFilter(recent, BooksFilter.ALL, works))
        assertEquals(listOf(namazEn, parda), IslamicBooksHome.forFilter(recent, BooksFilter.MUSLIMAH, works))
    }

    // endregion

    // region Shelves

    @Test fun `a book is found in its work by id, whatever its collection id`() {
        assertSame(bahar, IslamicBooksHome.workOf(baharHi, works))
        assertSame(namaz, IslamicBooksHome.workOf(namazUr.copy(collectionId = "anything"), works))
        assertNull(IslamicBooksHome.workOf(other, works))
    }

    @Test fun `every language shows every work, one language shows works with an edition in it`() {
        assertEquals(works, IslamicBooksHome.forLanguage(works, HadithCatalog.ALL_LANGUAGES))
        assertEquals(listOf(bahar), IslamicBooksHome.forLanguage(works, "hi"))
        assertEquals(listOf(namaz), IslamicBooksHome.forLanguage(works, "en"))
        assertEquals(emptyList<Work>(), IslamicBooksHome.forLanguage(works, "fr"))
    }

    @Test fun `the shelves split classical and Muslimah works and the Muslimah filter hides the classical shelf`() {
        val all = IslamicBooksHome.shelves(works, BooksFilter.ALL)
        assertEquals(listOf(bahar), all.classical)
        assertEquals(listOf(namaz, pardaWork), all.muslimah)
        val ladies = IslamicBooksHome.shelves(works, BooksFilter.MUSLIMAH)
        assertEquals(emptyList<Work>(), ladies.classical)
        assertEquals(listOf(namaz, pardaWork), ladies.muslimah)
    }

    @Test fun `a stored filter name is read back, and an unknown one falls back to every book`() {
        assertEquals(BooksFilter.MUSLIMAH, BooksFilter.fromName("MUSLIMAH"))
        assertEquals(BooksFilter.ALL, BooksFilter.fromName("ALL"))
        assertEquals(BooksFilter.ALL, BooksFilter.fromName(null))
        assertEquals(BooksFilter.ALL, BooksFilter.fromName("CLASSICAL"))
        assertEquals(BooksFilter.ALL, BooksFilter.fromName(""))
    }

    // endregion

    // region Typed search

    @Test fun `featured matches follow the language filter and step aside for a category`() {
        assertEquals(works, IslamicBooksHome.featuredFor(works, HadithCatalog.ALL_LANGUAGES, categoryActive = false))
        assertEquals(listOf(namaz), IslamicBooksHome.featuredFor(works, "en", categoryActive = false))
        assertEquals(emptyList<Work>(), IslamicBooksHome.featuredFor(works, HadithCatalog.ALL_LANGUAGES, categoryActive = true))
    }

    // endregion
}
