package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookSearchTextTest {
    /**
     * book_search_vectors.tsv is written by tools/book_search/build_index.py with the folding the
     * index was built with: every dictionary term plus samples in each script. A mismatch here
     * means queries would silently stop matching the downloaded index.
     */
    @Test fun `folding matches the script that builds the index`() {
        val lines = checkNotNull(javaClass.classLoader?.getResourceAsStream("book_search_vectors.tsv"))
            .bufferedReader(Charsets.UTF_8).readLines().filter { it.isNotBlank() }
        assertTrue(lines.size > 2000)
        lines.forEach { line ->
            val (text, tokens, keys) = line.split('\t')
            val folded = BookSearchText.normalize(text)
            assertEquals(text, tokens, folded.joinToString(" "))
            assertEquals(text, keys, folded.joinToString(" ", transform = BookSearchText::soundKey))
        }
    }

    @Test fun `one word sounds the same in every script and spelling`() {
        listOf(
            listOf("namaz", "namaaz", "nemaz", "नमाज़", "नमाज", "નમાઝ", "নামাজ"),
            listOf("wuzu", "wudhu", "wuzoo", "वुज़ू", "વુઝૂ"),
            listOf("roza", "rozah", "रोज़ा", "રોઝા", "রোজা"),
            listOf("zakat", "zakaat", "ज़कात", "ઝકાત", "যাকাত"),
            listOf("ramzan", "रमज़ान"),
            listOf("maghrib", "मग़रिब"),
            listOf("taraweeh", "tarawih"),
            listOf("quran", "koran"),
            listOf("hadith", "hadees"),
            listOf("aisha", "ayesha"),
            listOf("eid", "ईद")
        ).forEach { spellings ->
            val keys = spellings.map { BookSearchText.soundKey(BookSearchText.normalize(it).single()) }
            assertEquals(spellings.toString(), 1, keys.distinct().size)
        }
    }

    @Test fun `different words keep different keys`() {
        assertNotEquals(BookSearchText.soundKey("roza"), BookSearchText.soundKey("raza"))
        assertNotEquals(BookSearchText.soundKey("hajj"), BookSearchText.soundKey("haiz"))
        assertEquals("v2", BookSearchText.soundKey("v2"))
        assertEquals(listOf("والدين"), BookSearchText.normalize("والدین"))
    }
}
