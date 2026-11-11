package com.example.hadith

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HadithMovesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val saved get() = File(context.filesDir, "hadith_pdfs")
    private val cached get() = File(context.cacheDir, "hadith_pdfs")
    private val worldSaved get() = File(File(context.filesDir, "world_library"), "saved.json")
    private val prefs get() = context.getSharedPreferences("hadith_pdf_reader", Context.MODE_PRIVATE)

    @Before fun clean() {
        saved.deleteRecursively()
        cached.deleteRecursively()
        File(context.cacheDir, "pdf_ranges").deleteRecursively()
        File(context.filesDir, "world_library").deleteRecursively()
        prefs.edit().clear().commit()
    }

    private fun touch(file: File, text: String = "%PDF-1.4") = file.apply { parentFile?.mkdirs(); writeText(text) }

    private fun entries(): List<JSONObject> =
        if (!worldSaved.exists()) emptyList()
        else JSONArray(worldSaved.readText()).let { array -> List(array.length()) { array.getJSONObject(it) } }

    @Test fun `saved Muwatta Musnad and Darimi volumes move to the World Library with their reading state`() {
        touch(File(saved, "ar_06-muwatta-1.pdf"), "muwatta")
        touch(File(saved, "ar_06-muwatta-1.pdf.verified"), "7")
        touch(File(saved, "ar_06-muwatta-1.pdf.outline"), "[]")
        touch(File(saved, "ar_08-darimi-2.pdf"), "darimi")
        touch(File(cached, "ar_07-musnad_ahmad-3.pdf"))
        touch(File(cached, "ar_07-musnad_ahmad-3.pdf.verified"))
        touch(File(context.cacheDir, "pdf_ranges/07-musnad_ahmad-3/0"))
        val kept = touch(File(saved, "ar_00-bukhari-1.pdf"))
        prefs.edit()
            .putInt("last_06-muwatta-1", 41).putLong("opened_06-muwatta-1", 99L).putString("bookmarks_06-muwatta-1", "3,9")
            .putInt("last_00-bukhari-1", 12)
            .commit()

        assertTrue(HadithMoves.migrate(context))

        assertEquals("muwatta", File(saved, "ar_di-1551.pdf").readText())
        assertEquals("7", File(saved, "ar_di-1551.pdf.verified").readText())
        assertEquals("[]", File(saved, "ar_di-1551.pdf.outline").readText())
        assertEquals("darimi", File(saved, "ar_di-1531.pdf").readText())
        listOf("ar_06-muwatta-1.pdf", "ar_06-muwatta-1.pdf.verified", "ar_06-muwatta-1.pdf.outline", "ar_08-darimi-2.pdf")
            .forEach { assertFalse(it, File(saved, it).exists()) }
        assertFalse(File(cached, "ar_07-musnad_ahmad-3.pdf").exists())
        assertFalse(File(cached, "ar_07-musnad_ahmad-3.pdf.verified").exists())
        assertFalse(File(context.cacheDir, "pdf_ranges/07-musnad_ahmad-3").exists())
        assertTrue(kept.exists())

        assertEquals(41, prefs.getInt("last_di-1551", 0))
        assertEquals(99L, prefs.getLong("opened_di-1551", 0L))
        assertEquals("3,9", prefs.getString("bookmarks_di-1551", null))
        assertFalse(prefs.contains("last_06-muwatta-1"))
        assertFalse(prefs.contains("opened_06-muwatta-1"))
        assertFalse(prefs.contains("bookmarks_06-muwatta-1"))
        assertEquals(12, prefs.getInt("last_00-bukhari-1", 0))

        // Only books that were saved on this phone become World Library downloads.
        val moved = entries()
        assertEquals(listOf("di-1551", "di-1531"), moved.map { it.getString("id") })
        val muwatta = moved.first()
        assertEquals(
            setOf("id", "identifier", "collection", "title", "language", "volume", "volumeCount", "url",
                "volumeTitle", "source", "sourceUrl", "pageCount"),
            muwatta.keys().asSequence().toSet()
        )
        assertEquals("di:1551", muwatta.getString("identifier"))
        assertEquals("الموطأ الجزء الأول", muwatta.getString("collection"))
        assertEquals("الموطأ الجزء الأول", muwatta.getString("title"))
        assertEquals("ar", muwatta.getString("language"))
        assertEquals(1, muwatta.getInt("volume"))
        assertEquals(1, muwatta.getInt("volumeCount"))
        assertEquals("https://data2.dawateislami.net/Data/Books/Download/ar/pdf/2014/920-1.pdf", muwatta.getString("url"))
        assertEquals("", muwatta.getString("volumeTitle"))
        assertEquals("dawateislami", muwatta.getString("source"))
        assertEquals("https://www.dawateislami.net/bookslibrary/ar/imam-malik-jild-1", muwatta.getString("sourceUrl"))
        assertEquals(270, muwatta.getInt("pageCount"))
        val darimi = moved.last()
        assertEquals("https://data2.dawateislami.net/Data/Books/Download/ar/pdf/2014/907-1.pdf", darimi.getString("url"))
        assertEquals("https://www.dawateislami.net/bookslibrary/ar/sunan-al-darmi-jild-2", darimi.getString("sourceUrl"))
        assertEquals("سنن الدارمي المجلد الثاني", darimi.getString("title"))
    }

    @Test fun `existing World Library downloads are kept and never duplicated`() {
        touch(File(saved, "ar_07-musnad_ahmad-1.pdf"))
        val existing = JSONArray()
            .put(JSONObject().put("id", "wl-0011223344556677").put("identifier", "some-archive-book"))
            .put(JSONObject().put("id", "di-1524").put("identifier", "di:1524"))
        touch(worldSaved, existing.toString())

        assertTrue(HadithMoves.migrate(context))

        assertEquals(listOf("wl-0011223344556677", "di-1524"), entries().map { it.getString("id") })
        assertTrue(File(saved, "ar_di-1524.pdf").exists())
    }

    @Test fun `nothing on the phone means nothing is added`() {
        assertTrue(HadithMoves.migrate(context))
        assertTrue(entries().isEmpty())
    }

    @Test fun `the move runs once`() {
        assertTrue(HadithMoves.migrate(context))
        val later = touch(File(saved, "ar_06-muwatta-2.pdf"))
        assertFalse(HadithMoves.migrate(context))
        assertTrue(later.exists())
        assertFalse(File(saved, "ar_di-1552.pdf").exists())
    }
}
