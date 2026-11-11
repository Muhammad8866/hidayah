package com.example.hadith

import com.example.hadith.BookCatalogSearch.Entry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The book-list search engine on its own, with small hand-made lists. */
class BookCatalogSearchTest {
    private val bahar = Entry("Bahar-e-Shariat", "بہار شریعت", "Mufti Amjad Ali Azmi", listOf("बहारे शरीअत", "Jild 1"))
    private val anwar = Entry("Anwar-e-Shariat", "انوار شریعت", "Mufti Ahmad Yar Khan")
    private val faizan = Entry("Faizan-e-Sunnat", "فیضان سنت", "Maulana Ilyas Qadri")
    private val namaz = Entry("Namaz", "نماز", "Maktaba-tul-Madina")
    private val shawahid = Entry("Shawahid-un-Nubuwwat", "شواہد النبوۃ", "Maulana Abdur Rahman Jami")
    private val chadar = Entry("Chadar Aur Char Diwari", author = "Mufti Khalil Khan Barkati")
    private val sisters = Entry("Salah for Islamic Sisters", other = listOf("इस्लामी बहनों की नमाज़", "IRMÃS MUÇULMANAS"))
    private val books = listOf(bahar, anwar, faizan, namaz, shawahid, chadar, sisters)
    private val index = BookCatalogSearch.Index(books)

    private fun found(query: String, list: List<Entry> = books) = BookCatalogSearch.rank(query, list) { it }
    private fun first(query: String) = found(query).firstOrNull()
    private fun score(query: String, entry: Entry) = checkNotNull(BookCatalogSearch.Index(listOf(entry)).scores(query))[0]

    // region Normal searches

    @Test fun `a long typed word that folds to one letter is not a prefix of everything`() {
        // "zzzz" folds to the sound key "j"; it must not match every word starting with "j".
        assertEquals(emptyList<Entry>(), found("zzzz"))
        // One typed letter is still a prefix while the word is being typed.
        assertTrue(namaz in found("n"))
    }

    @Test fun `best scores try typos only when nothing matches without them`() {
        val list = BookCatalogSearch.Index(listOf(Entry("Namaz"), Entry("Nawaz")))
        // "namaz" is a title itself, so "Nawaz", one letter off, is not listed.
        val exact = checkNotNull(list.bestScores("namaz"))
        assertTrue(exact[0] > 0)
        assertEquals(0, exact[1])
        // Nothing is spelt "namax", so the title one letter off is offered instead.
        assertTrue(checkNotNull(list.bestScores("namax"))[0] > 0)
        assertNull(list.bestScores("  "))
    }

    @Test fun `without typos a one-letter-off word does not match, and the default still allows it`() {
        val strict = checkNotNull(BookCatalogSearch.Index(listOf(namaz)).scores("namax", typos = false))
        assertEquals(0, strict[0])
        assertTrue(checkNotNull(BookCatalogSearch.Index(listOf(namaz)).scores("namax"))[0] > 0)
    }

    @Test fun `an exact title finds its book first`() {
        assertEquals(bahar, first("Bahar-e-Shariat"))
        assertEquals(chadar, first("Chadar Aur Char Diwari"))
        assertEquals(namaz, first("namaz"))
    }

    @Test fun `case and accents do not matter`() {
        assertEquals(bahar, first("BAHAR-E-SHARIAT"))
        assertEquals(faizan, first("FAIZĀN-E-SUNNAT"))
        assertEquals(sisters, first("irmas muculmanas"))
    }

    @Test fun `one word in any spelling or Indic script is the same word`() {
        listOf("namaz", "namaaz", "नमाज़", "নামাজ").forEach { assertEquals(it, namaz, first(it)) }
        assertTrue(score("bukhari", Entry("बुखारी")) > 0)
        assertTrue(score("sunat", faizan) > 0)
    }

    @Test fun `a sound-alike word in another script reaches a book through its other names`() {
        assertTrue(sisters in found("namaz"))
        assertTrue(sisters in found("namaaz"))
    }

    @Test fun `joined or split spellings find the same book`() {
        assertEquals(bahar, first("bahare shariat"))
        assertEquals(bahar, first("bahar e shariat"))
        assertEquals(bahar, first("baharishariat"))
        assertEquals(faizan, first("faizane sunnat"))
        assertTrue(score("miraatul manajeeh", Entry("Mir-aat-ul-Manajeeh")) > 0)
        assertTrue(score("maale virasat", Entry("Maal-e-Virasat")) > 0)
        assertTrue(score("maalevirasat", Entry("Maal-e-Virasat")) > 0)
        // A query split where the title is one word.
        assertTrue(score("faizan e sunnat", Entry("Faizanesunnat")) > 0)
    }

    @Test fun `word order does not matter`() {
        assertEquals(bahar, first("shariat bahar"))
        assertEquals(chadar, first("diwari chadar"))
    }

    @Test fun `the word still being typed matches as a prefix`() {
        assertEquals(bahar, first("bahar-e-sha"))
        assertEquals(faizan, first("fai"))
        assertEquals(chadar, first("chadar aur ch"))
        assertTrue(score("maari", Entry("Maarij-un-Nubuwwat")) > 0)
    }

    @Test fun `a small typo still finds the book`() {
        assertEquals(bahar, first("bahar e shariyat"))
        assertEquals(faizan, first("faizaan e sunat"))
        assertEquals(bahar, first("bahar e sharait")) // two letters swapped
        assertEquals(faizan, first("faizn"))
        assertEquals(shawahid, first("Shwahid"))
        assertTrue(score("chadr", chadar) > 0)
        assertTrue(score("khiyant", Entry("Khiyanat")) > 0)
    }

    @Test fun `a script title is searched in its own script, with or without vowel marks`() {
        assertEquals(bahar, first("بہار شریعت"))
        assertEquals(bahar, first("بہارِ شریعت"))
        assertEquals(faizan, first("فیضان"))
        assertEquals(bahar, first("बहारे शरीअत"))
    }

    @Test fun `a short joining word is optional`() {
        val plain = Entry("Faizan Sunnat")
        assertTrue(score("faizan e sunnat", plain) > 0)
        assertTrue(score("faizan sunnat", faizan) > 0)
        // Short words are required when they are all there is.
        assertTrue(score("ul", Entry("Kanz ul Iman")) > 0)
        assertEquals(0, score("ul", Entry("Kanz Iman")))
    }

    @Test fun `every other word is required`() {
        assertEquals(listOf(chadar), found("Khalil Khan"))
        assertEquals(0, score("bahar shariat zzqxv", bahar))
        assertEquals(emptyList<Entry>(), found("xqjzvw"))
        assertEquals(emptyList<Entry>(), found("qwrtzpkx bvnmlj"))
    }

    @Test fun `a short sound key gets no typo match`() {
        // "khan" folds to "kan" and "kay" to "kai": one letter apart, but too short to guess.
        assertEquals(0, score("khan", Entry("Kay")))
        assertEquals(0, score("ilm", Entry("Ism")))
        // Four letters allow one edit, seven allow two.
        assertTrue(score("chadr", Entry("Chadar")) > 0)
        assertEquals(0, score("chdr", Entry("Chadar Aur")))
        assertTrue(score("baharisariyat", Entry("Bahar-e-Shariat")) > 0)
    }

    // endregion

    // region Ranking

    @Test fun `a title match ranks above an author-only match whatever the list order`() {
        val byAuthor = Entry("Aurat Aur Parda", author = "Ilyas Publications")
        val titled = Entry("Ilyas Ki Kahani")
        assertEquals(listOf(titled, byAuthor), found("ilyas", listOf(byAuthor, titled)))
        assertEquals(listOf(titled, byAuthor), found("ilyas", listOf(titled, byAuthor)))
    }

    @Test fun `an exact word ranks above a sound-alike, a prefix and an other-names match`() {
        val exact = Entry("Jannati Zevar")
        val alike = Entry("Sunni Bahishti Zewar")
        assertEquals(listOf(exact, alike), found("zevar", listOf(alike, exact)))
        val prefix = Entry("Zevarat Ki Duniya")
        assertEquals(exact, found("zevar", listOf(prefix, exact)).first())
        val other = Entry("Aurat Ka Libas", other = listOf("Zevar"))
        assertEquals(listOf(exact, other), found("zevar", listOf(other, exact)))
    }

    @Test fun `a phrase ranks above the same words apart, which rank above loose matches`() {
        val phrase = Entry("Faizan e Sunnat")
        val apart = Entry("Sunnat Ka Faizan")
        val loose = Entry("Madani Phool", author = "Faizan Sunnat Wale")
        assertEquals(listOf(phrase, apart, loose), found("faizan sunnat", listOf(loose, apart, phrase)))
    }

    @Test fun `the whole title or its start ranks above a phrase further in`() {
        val whole = Entry("Namaz")
        val start = Entry("Namaz Ke Ahkam")
        val inside = Entry("Faizan-e-Namaz")
        assertEquals(listOf(whole, start, inside), found("namaz", listOf(inside, start, whole)))
    }

    @Test fun `equal scores keep the order given`() {
        val one = Entry("Kitab ul Ilm", author = "A")
        val two = Entry("Kitab ul Ilm", author = "B")
        val three = Entry("Kitab ul Ilm", author = "C")
        assertEquals(listOf(two, three, one), found("kitab ilm", listOf(two, three, one)))
        assertEquals(listOf(three, one, two), found("kitab ilm", listOf(three, one, two)))
    }

    @Test fun `a score depends only on the entry and the query`() {
        val lone = score("bahar shariat", bahar)
        assertEquals(lone, checkNotNull(index.scores("bahar shariat"))[0])
        val copies = BookCatalogSearch.Index(listOf(faizan, bahar, bahar, namaz)).scores("bahar shariat")
        assertNotNull(copies)
        assertEquals(lone, copies!![1])
        assertEquals(lone, copies[2])
    }

    @Test fun `tier tells how a book matched`() {
        fun tier(query: String, entry: Entry) = BookCatalogSearch.tier(score(query, entry))
        assertEquals(BookCatalogSearch.TIER_TITLE_PHRASE, tier("bahar e shariat", bahar))
        assertEquals(BookCatalogSearch.TIER_TITLE_PHRASE, tier("bahare shariat", bahar))
        assertEquals(BookCatalogSearch.TIER_TITLE_PHRASE, tier("بہار شریعت", bahar))
        assertEquals(BookCatalogSearch.TIER_TITLE_WORDS, tier("shariat bahar", bahar))
        assertEquals(BookCatalogSearch.TIER_LOOSE, tier("amjad ali", bahar))
        assertEquals(BookCatalogSearch.TIER_LOOSE, tier("jild", bahar))
        assertEquals(BookCatalogSearch.TIER_NONE, tier("zzqxv", bahar))
        assertEquals(BookCatalogSearch.TIER_NONE, BookCatalogSearch.tier(0))
        assertEquals(BookCatalogSearch.TIER_NONE, BookCatalogSearch.tier(-5))
        assertTrue(tier("shariat bahar", bahar) >= BookCatalogSearch.TIER_TITLE_WORDS)
    }

    // endregion

    // region Edge cases and bad input

    @Test fun `a query with no searchable word gives null, so every book shows`() {
        assertNull(index.scores(""))
        assertNull(index.scores("   "))
        assertNull(index.scores("!!! ??? — ·"))
        assertNull(index.scores("😀🎉"))
        assertNull(index.scores("\u0000‍\t\n"))
        assertEquals(books, BookCatalogSearch.rank("  ", books) { it })
    }

    @Test fun `an empty list gives an empty answer`() {
        val empty = BookCatalogSearch.Index(emptyList())
        assertEquals(0, empty.size)
        assertArrayEquals(IntArray(0), empty.scores("bahar"))
        assertNull(empty.scores(""))
        assertEquals(emptyList<Entry>(), BookCatalogSearch.rank("bahar", emptyList<Entry>()) { it })
    }

    @Test fun `entries with blank fields are searched by what they have`() {
        val list = listOf(Entry(""), Entry("", "", "Ilyas Qadri"), Entry("   ", other = listOf("", "Namaz")))
        val scores = checkNotNull(BookCatalogSearch.Index(list).scores("ilyas"))
        assertEquals(0, scores[0])
        assertTrue(scores[1] > 0)
        assertEquals(0, scores[2])
        assertEquals(listOf(list[2]), BookCatalogSearch.rank("namaz", list) { it })
    }

    @Test fun `emoji and control characters around a word are ignored`() {
        assertEquals(bahar, first("😀 bahar\u0000 shariat ‍"))
        assertEquals(bahar, first("\tبہار\n"))
    }

    @Test fun `huge or repeated queries do not throw and stay bounded`() {
        index.scores("a".repeat(10_000))
        index.scores("bahar ".repeat(2_000))
        index.scores("x".repeat(500))
        index.scores("\u0000\uFFFF😀 بہار ‍")
        index.scores((1..500).joinToString(" ") { "w$it" })
        // Only the first words of a very long query count, so the answer stays the same past the cap.
        val long = "bahar shariat " + "e ".repeat(400)
        assertEquals(bahar, first(long))
    }

    @Test fun `scores has one value per entry and zero for every miss`() {
        val scores = checkNotNull(index.scores("bahar"))
        assertEquals(books.size, scores.size)
        assertTrue(scores[0] > 0)
        assertEquals(0, scores[2])
        assertTrue(scores.all { it >= 0 })
    }

    @Test fun `the same index answers from several threads alike`() {
        val expected = checkNotNull(index.scores("faizan sunnat")).toList()
        val failures = java.util.concurrent.atomic.AtomicInteger()
        val threads = (1..8).map {
            Thread { repeat(200) { if (index.scores("faizan sunnat")?.toList() != expected) failures.incrementAndGet() } }
        }
        threads.forEach(Thread::start)
        threads.forEach { it.join() }
        assertEquals(0, failures.get())
    }

    // endregion
}
