package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HadithCatalogTest {
    private val books = HadithCatalog.arabicBooks
    private val commentaries = HadithCatalog.books.filter { it.languageCode == "ur" }
    private val sets = listOf(
        "bukhari" to 2, "muslim" to 2, "abu_dawud" to 2, "tirmidhi" to 5, "nasai" to 6, "ibn_majah" to 2
    )
    private val commentarySets = listOf(
        "nematul_bari" to 16, "sharh_muslim" to 7, "nematul_wadood" to 5, "sharh_tirmidhi" to 4
    )
    private val release = "https://github.com/Muhammad8866/hidayah/releases/download/hadith-sharh-ur-v1/"

    @Test fun `the Arabic sets come first, then the four Urdu commentaries in order`() {
        assertEquals(51, HadithCatalog.books.size)
        assertEquals(books, HadithCatalog.books.take(19))
        assertEquals(commentaries, HadithCatalog.books.drop(19))
        assertEquals(commentarySets, commentaries.groupBy { it.collectionId }.map { (id, volumes) -> id to volumes.size })
        commentaries.groupBy { it.collectionId }.forEach { (id, volumes) ->
            assertEquals(id, (1..volumes.size).toList(), volumes.map { it.volume })
            assertTrue(id, volumes.all { it.volumeCount == volumes.size })
            assertEquals(id, (1..volumes.size).map { "sharh-$id-$it" }, volumes.map { it.id })
        }
        // Ids never collide with each other, the Arabic sets or editions removed earlier.
        assertEquals(HadithCatalog.books.size, HadithCatalog.books.map { it.id }.toSet().size)
        assertTrue(commentaries.none { book -> HadithCatalog.removedEditions.any { it.second == book.id } })
        assertTrue(commentaries.none { book -> IslamicBooksCatalog.removedEditions.any { it.first == book.id } })
    }

    @Test fun `every commentary downloads only its own release copy, never a publisher address`() {
        commentaries.forEach { book ->
            val asset = "ur-${book.collectionId.replace('_', '-')}-v${book.volume}.pdf"
            assertTrue(asset, asset.matches(Regex("ur-[a-z]+(?:-[a-z]+)*-v\\d+\\.pdf")))
            assertEquals(asset, book.mirrorAssetName)
            assertEquals(release + asset, book.mirrorUrl)
            assertEquals(listOf(release + asset), book.downloadUrls)
            assertFalse(book.id, book.mirrored)
            assertFalse(book.id, "dawateislami" in book.downloadUrl || "dawateislami" in book.sourcePageUrl)
            assertEquals("ur", book.languageCode)
            assertEquals("Urdu", book.languageName)
            assertTrue(book.id, book.pageCount > 800)
            assertFalse(book.id, book.nativeTitle.isNullOrBlank())
            // Scans carry no usable bookmarks; nothing is bundled for them.
            assertTrue(book.id, book.chapters.isEmpty())
        }
        assertEquals(32, commentaries.map { it.mirrorAssetName }.toSet().size)
        assertEquals(release + "ur-nematul-bari-v16.pdf", commentaries.single { it.id == "sharh-nematul_bari-16" }.downloadUrl)
    }

    @Test fun `only complete commentaries name their last volume`() {
        commentaries.filter { it.collectionId in setOf("nematul_bari", "sharh_muslim") }.forEach { book ->
            assertNull(book.id, book.volumeTitle)
            assertTrue(book.romanTitle, book.romanTitle.endsWith(" — Urdu · Volume ${book.volume} of ${book.volumeCount}"))
        }
        // Nimat al-Wadood and the Tirmidhi commentary are still being published.
        commentaries.filter { it.collectionId in setOf("nematul_wadood", "sharh_tirmidhi") }.forEach { book ->
            assertEquals("Volume ${book.volume}", book.volumeTitle)
            assertTrue(book.romanTitle, book.romanTitle.endsWith(" — Urdu · Volume ${book.volume}"))
        }
    }

    @Test fun `each volume keeps the title and page count printed in its own PDF`() {
        fun book(id: String) = commentaries.single { it.id == id }
        assertEquals("نعمۃ الباری فی شرح صحیح البخاری", book("sharh-nematul_bari-7").nativeTitle)
        assertEquals("نعم الباری فی شرح صحیح البخاری", book("sharh-nematul_bari-8").nativeTitle)
        assertEquals(900, book("sharh-nematul_bari-1").pageCount)
        assertEquals(1083, book("sharh-nematul_bari-15").pageCount)
        assertEquals(1340, book("sharh-sharh_muslim-1").pageCount)
        assertEquals(1245, book("sharh-sharh_muslim-6").pageCount)
        assertEquals(866, book("sharh-nematul_wadood-4").pageCount)
        assertEquals(1044, book("sharh-sharh_tirmidhi-1").pageCount)
        assertEquals("شرح جامع ترمذی", book("sharh-sharh_tirmidhi-4").nativeTitle)
    }

    @Test fun `commentaries are found by their Urdu and Roman names`() {
        fun ids(query: String) = HadithCatalog.visibleBooks(HadithCatalog.books, query, HadithCatalog.ALL_LANGUAGES, HadithSort.COLLECTION)
            .map { it.collectionId }.distinct()
        assertEquals(listOf("nematul_bari"), ids("نعمۃ الباری"))
        assertEquals(listOf("nematul_bari"), ids("Nematul Bari"))
        assertEquals(listOf("nematul_wadood"), ids("نعمۃ الودود"))
        assertEquals(listOf("sharh_tirmidhi"), ids("شرح ترمذی"))
        assertEquals(listOf("sharh_muslim"), ids("شرح صحیح مسلم"))
        // The Urdu filter shows only the commentaries.
        assertEquals(commentaries, HadithCatalog.visibleBooks(HadithCatalog.books, "", "ur", HadithSort.COLLECTION))
    }

    @Test fun `only the six Kutub al-Sittah sets are listed, in order`() {
        assertEquals(19, books.size)
        assertEquals(sets.map { it.first }, books.map { it.collectionId }.distinct())
        val ids = sets.flatMapIndexed { index, (id, volumes) -> (1..volumes).map { "%02d-%s-%d".format(index, id, it) } }
        assertEquals(ids, books.map { it.id })
        books.groupBy { it.collectionId }.forEach { (_, volumes) ->
            assertEquals((1..volumes.first().volumeCount).toList(), volumes.map { it.volume })
        }
        assertTrue(books.none { it.collectionId in setOf("muwatta", "musnad_ahmad", "darimi") })
    }

    @Test fun `every book is the original Arabic Dawat-e-Islami PDF with its mirror first`() {
        books.forEach { book ->
            assertEquals("ar", book.languageCode)
            assertTrue(book.mirrored)
            assertTrue(book.downloadUrl, book.downloadUrl.matches(
                Regex("https://data2\\.dawateislami\\.net/Data/Books/Download/ar/pdf/2014/\\d+-1\\.pdf")))
            assertEquals(listOf(book.mirrorUrl, book.downloadUrl), book.downloadUrls)
            assertTrue(book.mirrorUrl.startsWith(
                "https://github.com/Muhammad8866/hidayah/releases/download/hadith-pdfs-v1/ar-"))
            assertEquals("https://www.dawateislami.net/bookslibrary/ar/${book.slug}", book.sourcePageUrl)
        }
        val bukhari = books.single { it.id == "00-bukhari-1" }
        assertEquals("https://data2.dawateislami.net/Data/Books/Download/ar/pdf/2014/922-1.pdf", bukhari.downloadUrl)
        assertEquals("https://github.com/Muhammad8866/hidayah/releases/download/hadith-pdfs-v1/ar-bukhari-v1.pdf", bukhari.mirrorUrl)
        assertEquals("Sahih al-Bukhari — Arabic · Volume 1 of 2", bukhari.romanTitle)
        assertEquals(1035, bukhari.pageCount)
        val majah = books.single { it.id == "05-ibn_majah-2" }
        assertEquals("https://data2.dawateislami.net/Data/Books/Download/ar/pdf/2014/899-1.pdf", majah.downloadUrl)
        assertEquals("sunan-ibn-e-maja-jild-2", majah.slug)
    }

    @Test fun `removed Hindi and English editions are still listed for cleanup`() {
        val removed = HadithCatalog.removedEditions
        assertEquals(41, removed.size)
        assertEquals(34, removed.count { it.first == "hi" })
        assertEquals(7, removed.count { it.first == "en" })
        assertTrue(removed.contains("hi" to "h00-bukhari-1"))
        assertTrue(removed.contains("hi" to "h04-nasai-7"))
        assertTrue(removed.contains("en" to "e-nasai-2"))
        assertTrue(removed.none { (_, id) -> books.any { it.id == id } })
    }

    @Test fun `collection and volume sorting covers every PDF`() {
        val sorted = HadithCatalog.visibleBooks(books, "", HadithCatalog.ALL_LANGUAGES, HadithSort.COLLECTION)
        assertEquals(books.map { it.id }, sorted.map { it.id })
        assertEquals(listOf(1, 1, 1), HadithCatalog.visibleBooks(
            books, "", HadithCatalog.ALL_LANGUAGES, HadithSort.VOLUME_ASC
        ).take(3).map { it.volume })
    }

    @Test fun `search accepts Arabic and Urdu collection names`() {
        val arabic = HadithCatalog.visibleBooks(books, "صحيح البخاري", "ar", HadithSort.COLLECTION)
        assertEquals(listOf("00-bukhari-1", "00-bukhari-2"), arabic.map { it.id })

        val urdu = HadithCatalog.visibleBooks(books, "صحیح بخاری", "ar", HadithSort.COLLECTION)
        assertEquals(listOf("00-bukhari-1", "00-bukhari-2"), urdu.map { it.id })

        val majah = HadithCatalog.visibleBooks(books, "سنن ابن ماجہ", HadithCatalog.ALL_LANGUAGES, HadithSort.COLLECTION)
        assertTrue(majah.isNotEmpty() && majah.all { it.collectionId == "ibn_majah" })
    }
}
