package com.example.names

import com.example.hadith.HadithCatalog
import com.example.hadith.IslamicBooksCatalog
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedMoreSubtitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RabbanaAndProphetNamesTest {

    @Test fun fortyCompleteRabbanaDuas() {
        val duas = RabbanaDuas.duas
        assertEquals(40, duas.size)
        assertEquals((1..40).toList(), duas.map { it.number })
        assertEquals(40, duas.map { it.arabic }.toSet().size)
        assertTrue(duas.all { "رَبَّنَا" in it.arabic })
        assertTrue(duas.all { dua ->
            listOf(dua.arabic, dua.transliteration, dua.reference, dua.english, dua.urdu, dua.hindi, dua.hinglish)
                .all(String::isNotBlank)
        })
        assertEquals("Al-Baqarah 2:127", duas.first().reference)
        assertEquals("At-Tahrim 66:8", duas.last().reference)
    }

    @Test fun ninetyNineCompleteUniqueProphetNames() {
        val names = ProphetNames.names
        assertEquals(99, names.size)
        assertEquals((1..99).toList(), names.map { it.number })
        assertEquals(99, names.map { it.arabic }.toSet().size)
        assertEquals(99, names.map { it.transliteration }.toSet().size)
        assertTrue(names.all { name ->
            listOf(name.arabic, name.transliteration, name.english, name.urdu, name.hindi, name.hinglish).all(String::isNotBlank)
        })
        assertEquals("Muhammad", names.first().transliteration)
        assertEquals("Rasul al-Malahim", names.last().transliteration)
    }

    @Test fun newScreenCopyIsTranslated() {
        listOf("hinglish", "hi", "ur").forEach { language ->
            listOf(
                "99 Names of the Prophet ﷺ", ProphetNames.HADITH, ProphetNames.LIST_SOURCE,
                "40 Rabbana Duas", RabbanaDuas.INTRO, RabbanaDuas.SOURCE, "Search dua, meaning or surah",
                "No dua found.", "Islamic Books", "Classical books of Ahle Sunnat"
            ).forEach { text -> assertNotEquals("$language: $text", text, localizeUiText(text, language)) }
            listOf("Asma un-Nabi ﷺ with meanings", "Quranic duas with meaning", "Bahar-e-Shariat & more")
                .forEach { assertTrue(!localizedMoreSubtitle(it, language).isNullOrBlank()) }
        }
    }

    @Test fun booksCatalogCoversAllFourBooksFromTheirSources() {
        val books = IslamicBooksCatalog.books
        assertEquals(
            setOf(IslamicBooksCatalog.BAHAR, IslamicBooksCatalog.ANWAR, IslamicBooksCatalog.SHAWAHID, IslamicBooksCatalog.MAARIJ),
            books.map { it.collectionId }.toSet()
        )
        assertEquals(books.size, books.map { it.id }.toSet().size)
        assertTrue(books.none { book -> HadithCatalog.books.any { it.id == book.id } })
        books.forEach { book ->
            assertEquals(listOf(book.downloadUrl), book.downloadUrls)
            assertTrue(book.downloadUrl, book.downloadUrl.startsWith("https://data2.dawateislami.net/") ||
                book.downloadUrl.startsWith("https://archive.org/download/"))
            assertTrue(book.downloadUrl.endsWith(".pdf") && ' ' !in book.downloadUrl)
        }
        assertEquals(setOf("ur", "hi", "en"), books.filter { it.collectionId == IslamicBooksCatalog.BAHAR }.map { it.languageCode }.toSet())
    }
}
