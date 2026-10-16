package com.example.hadith

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Searches book_search_fixture.db, a tiny index written by tools/book_search/make_test_fixture.py
 * with the real dictionary: an English Bukhari and Muslim with a few pages and an Arabic Bukhari
 * with two chapters.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BookSearchIndexTest {
    private val books = HadithCatalog.books

    @Before fun installIndex() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val target = File(File(context.filesDir, "book_search"), "index.db")
        if (!target.isFile) {
            target.parentFile?.mkdirs()
            checkNotNull(javaClass.classLoader?.getResourceAsStream("book_search_fixture.db")).use { input ->
                target.outputStream().use(input::copyTo)
            }
        }
        BookSearchIndex.init(context)
    }

    private fun search(query: String, within: List<HadithBook> = books) =
        runBlocking { BookSearchIndex.search(query, within) }

    @Test fun `a Roman Urdu question opens the chapter that answers both of its parts`() {
        val outcome = search("safar me namaz kaise padhe")
        assertEquals(listOf("Safar", "Namaz"), outcome.understood)
        val first = outcome.hits.first()
        assertEquals("e-muslim", first.book.id)
        assertEquals(404, first.page)
        assertTrue(first.heading!!.contains("journey"))
        assertTrue(first.highlights.any { first.snippet.substring(it.first, it.last + 1) == "journey" })
    }

    @Test fun `Hindi Gujarati and Bengali spellings reach the same English chapter`() {
        listOf("वुज़ू", "वुजू का तरीका", "વુઝૂ", "wuzu", "wudhu", "ablution").forEach { query ->
            val first = search(query).hits.first()
            assertEquals(query, "e-bukhari", first.book.id)
            assertEquals(query, 47, first.page)
        }
        listOf("रोज़ा", "રોઝા", "রোজা", "roza").forEach { query ->
            assertEquals(query, 431, search(query).hits.first { it.book.languageCode == "en" }.page)
        }
    }

    @Test fun `an Arabic question prefers the Arabic edition and others the English one`() {
        assertEquals("ar", search("كتاب الوضوء").hits.first().book.languageCode)
        assertEquals("ar", search("وضو کا طریقہ").hits.first().book.languageCode)
        assertEquals("en", search("wuzu").hits.first().book.languageCode)
        assertEquals(listOf(44), search("الوضوء", books.filter { it.languageCode == "ar" }).hits.map { it.page })
    }

    @Test fun `a misspelt dictionary word is corrected but a real word of the books is not`() {
        assertEquals(listOf("Namaz"), search("namz").understood)
        // "Raza" is one letter from "roza" (fasting) and must still find the pages that name him.
        val raza = search("raza")
        assertEquals(listOf("raza"), raza.understood)
        assertTrue(raza.hits.isNotEmpty() && raza.hits.all { it.book.id == "e-muslim" })
    }

    @Test fun `a page result shows the matching words in context`() {
        val hit = search("abu hurairah honey").hits.first()
        assertEquals(300, hit.page)
        assertEquals(null, hit.heading)
        val marked = hit.highlights.map { hit.snippet.substring(it.first, it.last + 1) }
        assertTrue(marked.toString(), marked.containsAll(listOf("Abu Hurairah", "honey")))
    }

    @Test fun `filler words alone and books outside the index find nothing`() {
        assertTrue(search("kya hai").hits.isEmpty())
        assertTrue(search("namaz", books.filter { it.languageCode == "hi" }).hits.isEmpty())
    }
}
