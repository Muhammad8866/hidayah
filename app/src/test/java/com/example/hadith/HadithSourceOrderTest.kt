package com.example.hadith

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

// Robolectric because touching HadithPdfStore builds its network client.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HadithSourceOrderTest {
    private val mirror = "https://github.com/Muhammad8866/hidayah/releases/download/hadith-pdfs-v1/ar-bukhari-v1.pdf"
    private val publisher = "https://data2.dawateislami.net/Data/Books/Download/ar/pdf/2014/922-1.pdf"

    @Test fun `the mirror is tried first and the publisher only when it fails`() {
        val tried = mutableListOf<String>()
        assertEquals(mirror, HadithPdfStore.fromFirstSource(listOf(mirror, publisher)) { tried += it; it })
        assertEquals(listOf(mirror), tried)

        tried.clear()
        val opened = HadithPdfStore.fromFirstSource(listOf(mirror, publisher)) { url ->
            tried += url
            if (url == mirror) throw IOException("HTTP 504") else url
        }
        assertEquals(publisher, opened)
        assertEquals(listOf(mirror, publisher), tried)
    }

    @Test fun `every book of the library streams from its first source`() {
        HadithCatalog.books.forEach { book ->
            assertEquals(book.downloadUrls.first(), HadithPdfStore.fromFirstSource(book.downloadUrls) { it })
        }
        assertEquals(mirror, HadithCatalog.books.single { it.id == "00-bukhari-1" }.downloadUrls.first())
    }

    @Test fun `the last error is reported when no source opens`() {
        val error = runCatching {
            HadithPdfStore.fromFirstSource(listOf(mirror, publisher)) { url -> throw IOException("down: $url") }
        }.exceptionOrNull()
        assertTrue(error is IOException)
        assertEquals("down: $publisher", error?.message)
    }

    @Test fun `cancellation stops at once without trying the next source`() {
        val tried = mutableListOf<String>()
        val error = runCatching {
            HadithPdfStore.fromFirstSource(listOf(mirror, publisher)) { url -> tried += url; throw CancellationException("closed") }
        }.exceptionOrNull()
        assertTrue(error is CancellationException)
        assertEquals(listOf(mirror), tried)
    }
}
