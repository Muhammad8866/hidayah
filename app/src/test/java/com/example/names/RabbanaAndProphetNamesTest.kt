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
        // The Quranic text writes shadda before fatha; compare in canonical (NFC) mark order.
        val rabbana = java.text.Normalizer.normalize("رَبَّنَا", java.text.Normalizer.Form.NFC)
        assertTrue(duas.all { rabbana in java.text.Normalizer.normalize(it.arabic, java.text.Normalizer.Form.NFC) })
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

    @Test fun booksCatalogHoldsOnlyLibraryBooks() {
        val works = IslamicBooksCatalog.works
        val books = IslamicBooksCatalog.books
        // Bahar-e-Shariat leads the classical shelf; every other work sits on the Muslimah shelf.
        assertEquals(IslamicBooksCatalog.BAHAR, works.first().id)
        assertEquals(listOf(IslamicBooksCatalog.BAHAR), works.filter { it.shelf == IslamicBooksCatalog.Shelf.CLASSICAL }.map { it.id })
        assertTrue(works.drop(1).isNotEmpty() && works.drop(1).all { it.shelf == IslamicBooksCatalog.Shelf.MUSLIMAH })
        assertEquals(books.toSet(), works.flatMap { it.books }.toSet())

        val bahar = works.first()
        assertEquals(setOf("ur", "hi"), bahar.languages.toSet())
        assertEquals((1885..1890).map { "di-$it" }, bahar.editions("ur").map { it.id })
        assertEquals(listOf("di-8807", "di-8796"), bahar.editions("hi").map { it.id })

        assertEquals(books.size, books.map { it.id }.toSet().size)
        assertTrue(books.none { book -> HadithCatalog.books.any { it.id == book.id } })
        books.forEach { book ->
            assertTrue(book.id, book.id.startsWith("di-"))
            assertEquals(listOf(book.downloadUrl), book.downloadUrls)
            assertTrue(book.downloadUrl, book.downloadUrl.startsWith("https://data2.dawateislami.net/"))
            assertTrue(book.downloadUrl.endsWith(".pdf") && ' ' !in book.downloadUrl)
        }
    }
}
