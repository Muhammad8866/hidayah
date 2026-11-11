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

/** The one-time move to a library read only from dawateislami.net (see [LibraryMigration]). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LibraryMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val saved get() = File(context.filesDir, "hadith_pdfs")
    private val cached get() = File(context.cacheDir, "hadith_pdfs")
    private val ranges get() = File(context.cacheDir, "pdf_ranges")
    private val worldSaved get() = File(File(context.filesDir, "world_library"), "saved.json")
    private val reader get() = context.getSharedPreferences("hadith_pdf_reader", Context.MODE_PRIVATE)
    private val library get() = context.getSharedPreferences("world_library", Context.MODE_PRIVATE)

    @Before fun clean() {
        saved.deleteRecursively()
        cached.deleteRecursively()
        ranges.deleteRecursively()
        File(context.filesDir, "world_library").deleteRecursively()
        reader.edit().clear().commit()
        library.edit().clear().commit()
    }

    private fun touch(file: File, text: String = "%PDF-1.4") = file.apply { parentFile?.mkdirs(); writeText(text) }

    private fun entries(): List<JSONObject> =
        if (!worldSaved.exists()) emptyList()
        else JSONArray(worldSaved.readText()).let { array -> List(array.length()) { array.getJSONObject(it) } }

    private fun removedCount() = library.getInt(LibraryMigration.REMOVED_KEY, 0)

    @Test fun `a saved shelf book moves to the library's id with its reading state and joins the saved list`() {
        touch(File(saved, "ur_bk-bahar-ur-1.pdf"), "bahar")
        touch(File(saved, "ur_bk-bahar-ur-1.pdf.verified"), "5")
        touch(File(saved, "ur_bk-bahar-ur-1.pdf.outline"), "[]")
        touch(File(saved, "sd_bk-muslimah-islami-behno-ki-namaz-sd.pdf"), "salah")
        touch(File(cached, "ur_bk-bahar-ur-2.pdf"), "cached")
        touch(File(ranges, "bk-bahar-ur-2/0"))
        val hadith = touch(File(saved, "ar_00-bukhari-1.pdf"))
        reader.edit()
            .putInt("last_bk-bahar-ur-1", 41).putLong("opened_bk-bahar-ur-1", 99L).putString("bookmarks_bk-bahar-ur-1", "3,9")
            .putInt("last_00-bukhari-1", 12)
            .commit()

        assertTrue(LibraryMigration.migrate(context))

        assertEquals("bahar", File(saved, "ur_di-1885.pdf").readText())
        assertEquals("5", File(saved, "ur_di-1885.pdf.verified").readText())
        assertEquals("[]", File(saved, "ur_di-1885.pdf.outline").readText())
        assertEquals("salah", File(saved, "sd_di-1020.pdf").readText())
        assertEquals("cached", File(cached, "ur_di-1886.pdf").readText())
        listOf("ur_bk-bahar-ur-1.pdf", "ur_bk-bahar-ur-1.pdf.verified", "ur_bk-bahar-ur-1.pdf.outline",
            "sd_bk-muslimah-islami-behno-ki-namaz-sd.pdf").forEach { assertFalse(it, File(saved, it).exists()) }
        assertFalse(File(cached, "ur_bk-bahar-ur-2.pdf").exists())
        assertFalse(File(ranges, "bk-bahar-ur-2").exists())
        assertTrue(hadith.exists())

        assertEquals(41, reader.getInt("last_di-1885", 0))
        assertEquals(99L, reader.getLong("opened_di-1885", 0L))
        assertEquals("3,9", reader.getString("bookmarks_di-1885", null))
        listOf("last_", "opened_", "bookmarks_").forEach { assertFalse(reader.contains(it + "bk-bahar-ur-1")) }
        assertEquals(12, reader.getInt("last_00-bukhari-1", 0))

        // Only books saved on the phone (not cached) join the saved list, as library books.
        val moved = entries()
        assertEquals(listOf("di-1885", "di-1020"), moved.map { it.getString("id") })
        moved.forEach { assertEquals("dawateislami", it.getString("source")) }
        val bahar = WorldLibrary.savedBook(moved.first())
        assertEquals(IslamicBooksCatalog.book("di-1885")!!.downloadUrl, bahar.downloadUrl)
        assertEquals("wl-di:1885", bahar.collectionId)
        assertEquals(0, removedCount())
    }

    @Test fun `books of other sites are deleted with their state, and saved ones are counted`() {
        touch(File(saved, "kn_bk-anwar-kn.pdf"))
        touch(File(saved, "kn_bk-anwar-kn.pdf.verified"))
        touch(File(cached, "en_bk-bahar-en-16.pdf"))
        touch(File(ranges, "bk-maarij-fa/3"))
        touch(File(saved, "hi_bk-muslimah-islami-behno-ki-namaz-hi-2011.pdf"))
        touch(File(saved, "ur_wl-0011223344556677.pdf"))
        touch(File(saved, "ur_wl-0011223344556677.pdf.outline"))
        touch(File(cached, "ar_wl-aabbccddeeff0011.pdf.part"))
        touch(File(ranges, "wl-aabbccddeeff0011/1"))
        reader.edit()
            .putInt("last_bk-anwar-kn", 4).putString("bookmarks_bk-shawahid-fa", "1")
            .putLong("opened_wl-0011223344556677", 5L).putInt("last_wl-aabbccddeeff0011", 2)
            .commit()
        val libraryBook = JSONObject().put("id", "di-1551").put("identifier", "di:1551").put("collection", "c").put("title", "t")
            .put("language", "ar").put("volume", 1).put("volumeCount", 1)
            .put("url", "https://data2.dawateislami.net/Data/Books/Download/ar/pdf/2014/920-1.pdf").put("source", "dawateislami")
        touch(worldSaved, JSONArray()
            .put(JSONObject().put("id", "wl-0011223344556677").put("identifier", "AnwarEShariat").put("language", "ur"))
            .put(libraryBook)
            .put("not an entry")
            .toString())

        assertTrue(LibraryMigration.migrate(context))

        listOf(saved, cached).forEach { folder -> assertEquals(folder.path, emptyList<String>(), folder.list().orEmpty().toList()) }
        assertEquals(emptyList<String>(), ranges.list().orEmpty().toList())
        assertTrue(reader.all.keys.none { it.contains("anwar") || it.contains("shawahid") || it.contains("wl-") })
        assertEquals(listOf("di-1551"), entries().map { it.getString("id") })
        // Anwar-e-Shariat (Kannada), the 2011 Salah print and the archive book were saved; cached ones are not counted.
        assertEquals(3, removedCount())
    }

    @Test fun `a book already saved under its library id is kept and the old copy dropped`() {
        touch(File(saved, "ur_bk-bahar-ur-3.pdf"), "old")
        touch(File(saved, "ur_di-1887.pdf"), "library")
        touch(File(saved, "ur_di-1887.pdf.verified"), "7")
        reader.edit().putInt("last_bk-bahar-ur-3", 10).putInt("last_di-1887", 20).commit()
        val existing = WorldLibrary.savedEntry(IslamicBooksCatalog.book("di-1887")!!)
        touch(worldSaved, JSONArray().put(existing).toString())

        assertTrue(LibraryMigration.migrate(context))

        assertEquals("library", File(saved, "ur_di-1887.pdf").readText())
        assertEquals("7", File(saved, "ur_di-1887.pdf.verified").readText())
        assertFalse(File(saved, "ur_bk-bahar-ur-3.pdf").exists())
        assertEquals(20, reader.getInt("last_di-1887", 0))
        assertFalse(reader.contains("last_bk-bahar-ur-3"))
        assertEquals(listOf("di-1887"), entries().map { it.getString("id") })
    }

    @Test fun `a damaged saved list is replaced only when there is something to save`() {
        touch(worldSaved, "{not json")
        assertTrue(LibraryMigration.migrate(context))
        assertEquals("{not json", worldSaved.readText())

        clean()
        touch(worldSaved, "{not json")
        touch(File(saved, "ur_bk-muslimah-jannati-khawateen-ur.pdf"))
        assertTrue(LibraryMigration.migrate(context))
        assertEquals(listOf("di-8066"), entries().map { it.getString("id") })
    }

    @Test fun `the move runs once and the removed count is read once`() {
        touch(File(saved, "fa_bk-shawahid-fa.pdf"))
        assertTrue(LibraryMigration.migrate(context))
        val later = touch(File(saved, "ur_bk-bahar-ur-4.pdf"))
        assertFalse(LibraryMigration.migrate(context))
        assertTrue(later.exists())
        assertEquals(1, removedCount())

        assertEquals(1, WorldLibrary.takeRemovedCount(library))
        assertEquals(0, WorldLibrary.takeRemovedCount(library))
        assertFalse(library.contains(LibraryMigration.REMOVED_KEY))
    }

    @Test fun `a large saved list of earlier versions is cleaned in one pass`() {
        val count = 2_000
        val earlier = JSONArray()
        val edit = reader.edit()
        repeat(count) { n ->
            val id = "wl-%016x".format(n)
            earlier.put(JSONObject().put("id", id).put("identifier", "item$n").put("language", "ur"))
            if (n % 10 == 0) touch(File(saved, "ur_$id.pdf"))
            edit.putInt("last_$id", n)
        }
        edit.commit()
        touch(worldSaved, earlier.toString())
        val started = System.nanoTime()

        assertTrue(LibraryMigration.migrate(context))

        val tookMs = (System.nanoTime() - started) / 1_000_000
        assertTrue("took $tookMs ms", tookMs < 10_000)
        assertEquals(emptyList<String>(), saved.list().orEmpty().toList())
        assertTrue(reader.all.keys.none { it.startsWith("last_wl-") })
        assertEquals(emptyList<JSONObject>(), entries())
        assertEquals(count, removedCount())
    }

    @Test fun `a crafted saved list cannot make the move touch files outside its own names`() {
        // Files a crafted id or language would point at if they were ever joined into a path.
        val outside = touch(File(context.filesDir, "x"), "keep")
        val sibling = touch(File(context.filesDir, "keep.pdf"), "keep")
        val crafted = JSONArray()
            .put(JSONObject().put("id", "../../x").put("identifier", "x").put("language", "../").put("source", "dawateislami")
                .put("collection", "c").put("title", "t").put("volume", 1).put("volumeCount", 1).put("url", "https://x"))
            .put(JSONObject().put("id", "../keep").put("identifier", "y").put("language", "..\\"))
            .put(JSONObject().put("id", "di-1/../../x").put("identifier", "z").put("language", "ur").put("source", "dawateislami")
                .put("collection", "c").put("title", "t").put("volume", 1).put("volumeCount", 1).put("url", "https://x"))
        touch(worldSaved, crafted.toString())
        reader.edit().putInt("last_../../x", 1).commit()

        assertTrue(LibraryMigration.migrate(context))

        assertEquals("keep", outside.readText())
        assertEquals("keep", sibling.readText())
        // Entries of another site leave the list; the library's own are kept, and the library never reads a crafted one.
        assertEquals(listOf("../../x", "di-1/../../x"), entries().map { it.getString("id") })
        assertEquals(emptyList<HadithBook>(), WorldLibrary.parseSaved(worldSaved.readText()))
        // Only exact names of the old shelves and the old archive books are ever removed from the reader's state.
        assertEquals(1, reader.getInt("last_../../x", 0))
    }

    @Test fun `nothing on the phone changes nothing`() {
        assertTrue(LibraryMigration.migrate(context))
        assertFalse(worldSaved.exists())
        assertEquals(0, removedCount())
    }
}
