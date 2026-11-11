package com.example.hadith

import com.example.hadith.IslamicBooksCatalog.Work
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Islamic Books shelf search, run on the real curated shelves through [BookCatalogSearch]. */
class IslamicBooksSearchTest {
    private fun ids(query: String) = IslamicBooksCatalog.search(query).map { it.id }
    private fun first(query: String) = ids(query).firstOrNull()
    private fun work(id: String) = checkNotNull(IslamicBooksCatalog.work(id))

    private val salah = "muslimah_islami_behno_ki_namaz"
    private val jannatiZevar = "muslimah_jannati_zevar"
    private val bahishtiZewar = "muslimah_sunni_bahishti_zewar"
    private val chadar = "muslimah_chadar_aur_char_diwari"

    @Test fun `an exact title finds its work first`() {
        assertEquals(IslamicBooksCatalog.BAHAR, first("Bahar-e-Shariat"))
        assertEquals(chadar, first("Chadar Aur Char Diwari"))
        assertEquals(salah, first("Salah for Islamic Sisters"))
    }

    @Test fun `a joined or split spelling finds the same work`() {
        assertEquals(IslamicBooksCatalog.BAHAR, first("bahare shariat"))
        assertEquals(IslamicBooksCatalog.BAHAR, first("bahar e shariat"))
        assertTrue(IslamicBooksCatalog.BAHAR in ids("baharishariat"))
        assertEquals(chadar, first("chadar aur chardiwari"))
    }

    @Test fun `a script title finds its work, with or without vowel marks`() {
        assertEquals(IslamicBooksCatalog.BAHAR, first("بہار شریعت"))
        assertEquals(IslamicBooksCatalog.BAHAR, first("بہارِ شریعت"))
        // The Hindi edition's own title, held in the work's other names.
        assertEquals(IslamicBooksCatalog.BAHAR, first("बहारे शरीअत"))
    }

    @Test fun `a roman word reaches a work through an Indic edition title`() {
        // Salah for Islamic Sisters is "इस्लामी बहनों की नमाज़" in Hindi; namaz and नमाज़ are one word.
        assertEquals(salah, first("namaz"))
        assertEquals(salah, first("namaaz"))
    }

    @Test fun `an author's name returns that author's works`() {
        assertEquals(IslamicBooksCatalog.BAHAR, first("Amjad Ali Azmi"))
        assertEquals(setOf(bahishtiZewar, chadar), ids("Khalil Khan").toSet())
    }

    @Test fun `a small typo still finds the work`() {
        assertEquals(IslamicBooksCatalog.BAHAR, first("Bahr e Shariat"))
        assertEquals(chadar, first("chadr aur char diwari"))
    }

    @Test fun `a word still being typed matches as a prefix`() {
        assertEquals(IslamicBooksCatalog.BAHAR, first("bahar e shar"))
        assertEquals("muslimah_saas_bahu_main_sulha_ka_raaz", first("Saas Bahu Main Sul"))
    }

    @Test fun `a word only in an edition title finds the parent work`() {
        // Only Salah for Islamic Sisters has a Telugu edition; the word is in no work name or author.
        assertEquals(listOf(salah), ids("Telugu"))
        // The Portuguese edition's own title, matched without its accents.
        assertEquals(salah, first("irmas muculmanas"))
        assertEquals(listOf(IslamicBooksCatalog.BAHAR), ids("Jild"))
    }

    @Test fun `exact words rank above sound-alike words`() {
        val found = ids("zevar")
        assertEquals(jannatiZevar, found.first())
        assertTrue("Zewar is the same word as Zevar", bahishtiZewar in found)
    }

    @Test fun `a title match ranks above an author-only match whatever the list order`() {
        val titled = work(jannatiZevar)
        // A made-up work that names "Zevar" only as its author, listed first.
        val authorOnly = Work("test_author_only", "Aurat Aur Parda", "", "Zevar Publications", IslamicBooksCatalog.Shelf.MUSLIMAH, emptyList())
        assertEquals(listOf(titled.id, authorOnly.id), IslamicBooksCatalog.search("Zevar", listOf(authorOnly, titled)).map { it.id })
    }

    @Test fun `a query that matches nothing returns nothing`() {
        assertEquals(emptyList<String>(), ids("zzqxv"))
        assertEquals(emptyList<String>(), ids("qwrtzpkx bvnmlj"))
    }

    @Test fun `a query with no searchable word returns nothing, so the shelves show`() {
        assertEquals(emptyList<String>(), ids(""))
        assertEquals(emptyList<String>(), ids("   "))
        assertEquals(emptyList<String>(), ids("!!! ??? — ·"))
    }

    @Test fun `bad input does not throw`() {
        IslamicBooksCatalog.search("a".repeat(10_000))
        IslamicBooksCatalog.search("bahar ".repeat(2_000))
        IslamicBooksCatalog.search("\u0000\uFFFF😀 بہار ‍")
        IslamicBooksCatalog.search("bahar", emptyList())
    }

    @Test fun `books the library does not have are not found`() {
        listOf("Anwar-e-Shariat", "Shawahid-un-Nubuwwat", "Maarij un Nubuwwat").forEach { title ->
            assertTrue(title, ids(title).none { it == "anwar_e_shariat" || it == "shawahid_un_nubuwwat" || it == "maarij_un_nubuwwat" })
        }
        assertEquals(emptyList<String>(), ids("Kannada"))
    }

    @Test fun `only works the loaded list still has are searched by default`() {
        val shown = IslamicBooksCatalog.availableWorks { it != 3220L }
        assertTrue(IslamicBooksCatalog.search("Chadar Aur Char Diwari", shown).none { it.id == chadar })
        assertEquals(chadar, IslamicBooksCatalog.search("Chadar Aur Char Diwari", IslamicBooksCatalog.works).first().id)
    }

    @Test fun `every work's search entry carries its edition titles once`() {
        IslamicBooksCatalog.works.forEach { work ->
            val entry = work.searchEntry
            assertEquals(work.name, entry.title)
            assertEquals(work.author, entry.author)
            assertEquals(entry.other.distinct(), entry.other)
            work.books.forEach { book ->
                assertTrue(book.romanTitle in entry.other)
                book.nativeTitle?.let { assertTrue(it in entry.other) }
            }
        }
    }
}
