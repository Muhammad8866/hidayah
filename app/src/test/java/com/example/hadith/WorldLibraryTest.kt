package com.example.hadith

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WorldLibraryTest {
    private val item = WorldItem("AnwarEShariat", "Anwar e Shariat", languageCode = "ur")

    @Test fun `search query keeps to Islamic PDFs and escapes what the user types`() {
        val query = WorldLibrary.searchQuery("fatawa: (razvia)", "ur")
        assertTrue(query.startsWith("mediatype:texts AND format:PDF AND creator:("))
        assertTrue(query.contains("\"ahmad raza khan\""))
        assertTrue(query.contains("AND NOT access-restricted-item:true"))
        assertTrue(query.contains("AND language:(urdu OR urd)"))
        assertTrue(query.endsWith("AND (title:(fatawa razvia) OR creator:(fatawa razvia))"))
        assertFalse(WorldLibrary.searchQuery("", HadithCatalog.ALL_LANGUAGES).contains("language:"))
        assertTrue(WorldLibrary.searchUrl("", "ar", 3).contains("&page=3&"))
    }

    @Test fun `search results accept single and repeated fields`() {
        val page = WorldLibrary.parseSearch(
            """{"response":{"numFound":176542,"docs":[
                {"identifier":"a","title":"Kitab","creator":["One","Two"],"language":"urd","downloads":7},
                {"identifier":"b"}]}}"""
        )
        assertEquals(176542, page.total)
        assertEquals(WorldItem("a", "Kitab", "One", "ur", 7), page.items[0])
        assertEquals(WorldItem("b", "B"), page.items[1])
    }

    @Test fun `volumes are the book PDFs and download only from the source`() {
        val volumes = WorldLibrary.parseVolumes(
            item,
            """{"files":[
                {"name":"anwar e shariat.pdf","format":"Image Container PDF","size":"2461605"},
                {"name":"anwar e shariat_text.pdf","format":"Additional Text PDF","size":"11057624"},
                {"name":"__ia_thumb.jpg","format":"JPEG Thumb"}]}"""
        )!!
        val book = volumes.single()
        assertEquals(listOf("https://archive.org/download/AnwarEShariat/anwar%20e%20shariat.pdf"), book.downloadUrls)
        assertEquals("Anwar e Shariat · 2 MB", book.volumeTitle)
        assertEquals(WorldLibrary.bookId("AnwarEShariat", "anwar e shariat.pdf"), book.id)
    }

    @Test fun `an item the source deleted or took down is reported as removed`() {
        assertNull(WorldLibrary.parseVolumes(item, "{}"))
        assertNull(WorldLibrary.parseVolumes(item, """{"is_dark":true,"metadata":{}}"""))
        assertFalse(WorldLibrary.isRemoved(JSONObject("""{"files":[]}""")))
    }

    @Test fun `an unexpected answer is an error and never a removal`() {
        assertTrue(runCatching { WorldLibrary.isRemoved(JSONObject("""{"error":"busy"}""")) }.isFailure)
    }

    @Test fun `recent searches put the newest first without duplicates and keep only a few`() {
        val recent = listOf("Fatawa Razvia", "Kanzul Iman")
        assertEquals(listOf("kanzul iman", "Fatawa Razvia"), WorldLibrary.withRecent(recent, "  kanzul   iman "))
        assertEquals(recent, WorldLibrary.withRecent(recent, "   "))
        val many = (1..10).fold(emptyList<String>()) { list, n -> WorldLibrary.withRecent(list, "book $n") }
        assertEquals(6, many.size)
        assertEquals("book 10", many.first())
    }

    @Test fun `saved books stay searchable offline by every word`() {
        val book = WorldLibrary.parseVolumes(
            item, """{"files":[{"name":"anwar e shariat.pdf","format":"Image Container PDF","size":"2461605"}]}"""
        )!!.single()
        assertEquals(listOf(book), WorldLibrary.matchSaved(listOf(book), ""))
        assertEquals(listOf(book), WorldLibrary.matchSaved(listOf(book), "SHARIAT anwar"))
        assertTrue(WorldLibrary.matchSaved(listOf(book), "anwar fatawa").isEmpty())
    }
}
