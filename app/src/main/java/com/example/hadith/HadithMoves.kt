package com.example.hadith

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Muwatta, Musnad Ahmad and Sunan ad-Darimi left the Hadith Library for the World Library, where
 * Dawat-e-Islami's books are listed by their library id. A copy saved on this phone moves with its
 * reading position and bookmarks instead of being downloaded again; nothing else is touched.
 */
internal object HadithMoves {
    private const val PREFS = "hadith_pdf_reader"
    private const val DONE = "moved_to_world_library_v1"

    /** One moved volume: its old Hadith Library id and the book as Dawat-e-Islami's library lists it. */
    private class Moved(
        val oldId: String, val diId: Int, val fileId: Int, val pages: Int, val slug: String, val title: String
    )

    private val moved = listOf(
        Moved("06-muwatta-1", 1551, 920, 270, "imam-malik-jild-1", "الموطأ الجزء الأول"),
        Moved("06-muwatta-2", 1552, 921, 190, "imam-malik-jild-2", "الموطأ الجزء الثاني"),
        Moved("07-musnad_ahmad-1", 1524, 900, 642, "musnad-al-imam-ahmed-bin-hanbal-jild-1", "المسند للإمام أحمد بن حنبل الجزء الأول"),
        Moved("07-musnad_ahmad-2", 1525, 901, 827, "musnad-al-imam-ahmed-bin-hanbal-jild-2", "المسند للإمام أحمد بن حنبل الجزء الثاني"),
        Moved("07-musnad_ahmad-3", 1526, 902, 740, "musnad-al-imam-ahmed-bin-hanbal-jild-3", "المسند للإمام أحمد بن حنبل الجزء الثالث"),
        Moved("07-musnad_ahmad-4", 1527, 903, 657, "musnad-al-imam-ahmed-bin-hanbal-jild-4", "المسند للإمام أحمد بن حنبل الجزء الرابع"),
        Moved("07-musnad_ahmad-5", 1528, 904, 643, "musnad-al-imam-ahmed-bin-hanbal-jild-5", "المسند للإمام أحمد بن حنبل الجزء الخامس"),
        Moved("07-musnad_ahmad-6", 1529, 905, 607, "musnad-al-imam-ahmed-bin-hanbal-jild-6", "المسند للإمام أحمد بن حنبل الجزء السادس"),
        Moved("08-darimi-1", 1530, 906, 307, "sunan-al-darmi-jild-1", "سنن الدارمي المجلد الأول"),
        Moved("08-darimi-2", 1531, 907, 352, "sunan-al-darmi-jild-2", "سنن الدارمي المجلد الثاني")
    )

    private val suffixes = listOf("", ".verified", ".outline")

    /**
     * Moves saved copies once; call before the World Library reads its saved list. Returns false when
     * the move already ran.
     */
    @Synchronized
    fun migrate(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(DONE, false)) return false
        val savedFolder = File(context.filesDir, "hadith_pdfs")
        val cacheFolder = File(context.cacheDir, "hadith_pdfs")
        val worldSaved = File(File(context.filesDir, "world_library"), "saved.json")
        val entries = runCatching { JSONArray(worldSaved.readText()) }.getOrDefault(JSONArray())
        val listed = (0 until entries.length()).mapNotNull { entries.optJSONObject(it)?.optString("id") }.toMutableSet()
        var added = false
        val edit = prefs.edit()
        moved.forEach { book ->
            val newId = "di-${book.diId}"
            val oldPdf = File(savedFolder, "ar_${book.oldId}.pdf")
            val newPdf = File(savedFolder, "ar_$newId.pdf")
            val onPhone = when {
                oldPdf.isFile -> {
                    suffixes.forEach { suffix ->
                        val from = File(oldPdf.path + suffix)
                        val to = File(newPdf.path + suffix)
                        if (from.exists()) { to.delete(); if (!from.renameTo(to)) { from.copyTo(to, overwrite = true); from.delete() } }
                    }
                    true
                }
                else -> newPdf.isFile
            }
            File(oldPdf.path + ".part").delete()
            (suffixes + ".part").forEach { File(File(cacheFolder, "ar_${book.oldId}.pdf").path + it).delete() }
            File(context.cacheDir, "pdf_ranges/${book.oldId}").deleteRecursively()
            prefs.all["last_${book.oldId}"]?.let { (it as? Int)?.let { page -> edit.putInt("last_$newId", page) } }
            prefs.all["opened_${book.oldId}"]?.let { (it as? Long)?.let { time -> edit.putLong("opened_$newId", time) } }
            prefs.all["bookmarks_${book.oldId}"]?.let { (it as? String)?.let { marks -> edit.putString("bookmarks_$newId", marks) } }
            edit.remove("last_${book.oldId}").remove("opened_${book.oldId}").remove("bookmarks_${book.oldId}")
            if (onPhone && listed.add(newId)) {
                entries.put(
                    JSONObject().put("id", newId).put("identifier", "di:${book.diId}")
                        .put("collection", book.title).put("title", book.title)
                        .put("language", "ar").put("volume", 1).put("volumeCount", 1)
                        .put("url", "https://data2.dawateislami.net/Data/Books/Download/ar/pdf/2014/${book.fileId}-1.pdf")
                        .put("volumeTitle", "").put("source", "dawateislami")
                        .put("sourceUrl", "https://www.dawateislami.net/bookslibrary/ar/${book.slug}")
                        .put("pageCount", book.pages)
                )
                added = true
            }
        }
        if (added) {
            worldSaved.parentFile?.mkdirs()
            val temporary = File(worldSaved.path + ".tmp")
            temporary.writeText(entries.toString())
            if (!temporary.renameTo(worldSaved)) { worldSaved.writeText(entries.toString()); temporary.delete() }
        }
        edit.putBoolean(DONE, true).commit()
        return true
    }
}
