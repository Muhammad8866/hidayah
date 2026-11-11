package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * The Sirat-ul-Jinan volumes in the Read tab are rows of the library list shipped with the app,
 * read from the publisher's own files, each with the identity the full library gives that book,
 * so a volume downloaded or read in either place is the same book.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SiratUlJinanPdfsTest {
    private val seed: DiIndex by lazy {
        val file = generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "app/src/main/assets/world_library/di-index.json") }.first { it.isFile }
        checkNotNull(DawateIslamiIndex.readCache(file))
    }

    private fun diId(book: HadithBook) = book.id.removePrefix("di-").toLong()

    @Test fun `there are ten Urdu and three Hindi volumes with no duplicates`() {
        assertEquals(10, SiratUlJinanPdfs.urdu.size)
        assertEquals(3, SiratUlJinanPdfs.hindi.size)
        assertEquals(13, SiratUlJinanPdfs.all.size)
        assertEquals(13, SiratUlJinanPdfs.all.map { it.book.id }.distinct().size)
        assertTrue(SiratUlJinanPdfs.urdu.all { it.book.languageCode == "ur" })
        assertTrue(SiratUlJinanPdfs.hindi.all { it.book.languageCode == "hi" })
    }

    @Test fun `every volume is a Sirat-ul-Jinan row of the shipped library list with that row's addresses`() {
        val category = seed.categories.first { it.name == "Sirat ul Jinan" }.id
        SiratUlJinanPdfs.all.forEach { volume ->
            val book = volume.book
            val row = seed.byId[diId(book)]
            assertNotNull(book.id, row)
            row!!
            assertTrue(book.id, category in row.categories)
            assertEquals(book.id, "wl-di:${row.id}", book.collectionId)
            assertEquals(book.id, "di:${row.id}", book.slug)
            assertEquals(book.id, DawateIslamiIndex.appLanguage(row.languageCode), book.languageCode)
            assertEquals(book.id, row.fileId.toInt(), book.fileId)
            assertEquals(book.id, row.year, book.publicationYear)
            assertEquals(book.id, row.title, book.romanTitle)
            assertEquals(book.id, DawateIslamiIndex.downloadUrl(row), book.remoteUrl)
            assertEquals(book.id, DawateIslamiIndex.pageUrl(row), book.sourcePageUrl)
            assertEquals(book.id, "sirat-ul-jinan-jild-${volume.jild}", row.slug)
        }
    }

    @Test fun `a volume has the identity the full library gives the same book`() {
        SiratUlJinanPdfs.all.forEach { volume ->
            val book = volume.book
            val full = WorldLibrary.diBook(DawateIslamiIndex.toItem(seed.byId.getValue(diId(book))))
            assertEquals(full.id, book.id)
            assertEquals(full.collectionId, book.collectionId)
            assertEquals(full.slug, book.slug)
            assertEquals(full.languageCode, book.languageCode)
            assertEquals(full.languageName, book.languageName)
            assertEquals(full.romanTitle, book.romanTitle)
            assertEquals(full.nativeTitle, book.nativeTitle)
            assertEquals(full.downloadUrls, book.downloadUrls)
            assertEquals(full.sourcePageUrl, book.sourcePageUrl)
            assertEquals(full.pageCount, book.pageCount)
            assertEquals(full.mirrored, book.mirrored)
        }
    }

    @Test fun `every volume is read only from the publisher's file, never a mirror`() {
        SiratUlJinanPdfs.all.forEach { volume ->
            val book = volume.book
            assertFalse(book.id, book.mirrored)
            assertEquals(book.id, listOf(book.remoteUrl), book.downloadUrls)
            assertTrue(book.id, book.downloadUrl.startsWith("https://data2.dawateislami.net/Data/Books/Download/"))
            assertTrue(book.id, book.downloadUrl.endsWith("/${book.fileId}-1.pdf"))
            assertTrue(book.id, volume.sizeBytes > 0)
        }
    }

    @Test fun `the Urdu jild cover paras 1 to 30 in order and Hindi jild 1 to 3`() {
        assertEquals((1..10).toList(), SiratUlJinanPdfs.urdu.map { it.jild })
        assertEquals((1..30).toList(), SiratUlJinanPdfs.urdu.flatMap { it.firstPara..it.lastPara })
        assertEquals((1..3).toList(), SiratUlJinanPdfs.hindi.map { it.jild })
        assertEquals((1..9).toList(), SiratUlJinanPdfs.hindi.flatMap { it.firstPara..it.lastPara })
        // The Hindi jild are the same books as the Urdu ones: the publisher gives them the same file numbers.
        SiratUlJinanPdfs.hindi.forEach { hindi ->
            assertEquals(SiratUlJinanPdfs.urdu[hindi.jild - 1].book.fileId, hindi.book.fileId)
        }
    }

    @Test fun `volumes are found by book id and dropped when the library list no longer has them`() {
        val first = SiratUlJinanPdfs.urdu.first()
        assertEquals(first, SiratUlJinanPdfs.byId(first.book.id))
        assertNull(SiratUlJinanPdfs.byId(null))
        assertNull(SiratUlJinanPdfs.byId("di-0"))
        assertEquals(SiratUlJinanPdfs.urdu, SiratUlJinanPdfs.available(SiratUlJinanPdfs.urdu) { true })
        val kept = SiratUlJinanPdfs.available(SiratUlJinanPdfs.urdu) { it != 1238L }
        assertEquals(9, kept.size)
        assertTrue(kept.none { it.book.id == "di-1238" })
    }
}
