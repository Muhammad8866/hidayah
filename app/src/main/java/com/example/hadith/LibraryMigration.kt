package com.example.hadith

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Islamic Books and the World Library became one library read only from dawateislami.net. This
 * runs once, before the library reads its saved list:
 * - a curated shelf book whose PDF the library has moves to the library's own id ("bk-bahar-ur-1"
 *   becomes "di-1885") with its saved or cached file, reading position, bookmarks and last-opened
 *   time; a saved one joins the library's saved list instead of being downloaded again;
 * - a shelf book the library does not have (from the Internet Archive or another copy) and every
 *   Internet Archive book of the World Library ("wl-" plus 16 hex digits) is deleted with its
 *   files, partial downloads, page ranges, reading state and saved-list entry.
 * Saved books deleted are counted in [REMOVED_KEY] so the screen can say so once. Only exact
 * names are touched; Hadith Library books and library books ("di-n") are left alone.
 */
internal object LibraryMigration {
    private const val READER_PREFS = "hadith_pdf_reader"
    private const val LIBRARY_PREFS = "world_library"
    private const val DONE = "library_dawateislami_only_v1"
    /** In the library's preferences: saved books from other sites that were deleted, until the screen reads it. */
    const val REMOVED_KEY = "removed_other_sources"

    private val pdfSuffixes = listOf("", ".verified", ".outline")
    private val statePrefixes = listOf("last_", "opened_", "bookmarks_")
    private val archiveId = Regex("wl-[0-9a-f]{16}")
    private val archiveFile = Regex("[a-z]{2,3}_wl-[0-9a-f]{16}\\.pdf(?:\\.part|\\.verified|\\.outline)?")
    private val archiveState = Regex("(?:last|opened|bookmarks)_wl-[0-9a-f]{16}")

    /**
     * Runs the move once; returns false when it already ran. A step that fails on a disk error
     * leaves the move unfinished, so it runs again at the next open; every step can run twice.
     */
    @Synchronized
    fun migrate(
        context: Context,
        moves: Map<String, HadithBook> = IslamicBooksCatalog.legacyMoves,
        removed: List<Pair<String, String>> = IslamicBooksCatalog.removedEditions
    ): Boolean {
        val reader = context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE)
        if (reader.getBoolean(DONE, false)) return false
        val savedFolder = File(context.filesDir, "hadith_pdfs")
        val folders = listOf(savedFolder, File(context.cacheDir, "hadith_pdfs"))
        val ranges = File(context.cacheDir, "pdf_ranges")
        val worldSaved = File(File(context.filesDir, "world_library"), "saved.json")
        val edit = reader.edit()
        var complete = true
        fun step(block: () -> Unit) { if (runCatching(block).isFailure) complete = false }

        // The saved list without other sites' entries. An unreadable list reads as empty, as the library reads it.
        val kept = JSONArray()
        val listed = mutableSetOf<String>()
        var listChanged = false
        var removedEntries = 0
        val original = runCatching { JSONArray(worldSaved.readText()) }.getOrNull()
        if (original != null) for (index in 0 until original.length()) {
            val entry = original.opt(index) as? JSONObject
            if (entry != null && WorldLibrary.isLibraryEntry(entry)) {
                kept.put(entry)
                listed += entry.optString("id")
            } else {
                listChanged = true
                if (!entry?.optString("id").isNullOrBlank()) removedEntries++
            }
        }

        var removedBooks = 0
        removed.forEach { (id, language) ->
            step {
                if (File(savedFolder, "${language}_$id.pdf").isFile) removedBooks++
                folders.forEach { folder -> (pdfSuffixes + ".part").forEach { File(folder, "${language}_$id.pdf$it").delete() } }
                File(ranges, id).deleteRecursively()
            }
            statePrefixes.forEach { edit.remove(it + id) }
        }

        step {
            folders.forEach { folder -> folder.listFiles()?.filter { archiveFile.matches(it.name) }?.forEach { it.delete() } }
            ranges.listFiles()?.filter { it.isDirectory && archiveId.matches(it.name) }?.forEach { it.deleteRecursively() }
        }
        // Read once: every change below goes through [edit], so this is the state before the move.
        val state = reader.all
        state.keys.filter(archiveState::matches).forEach { edit.remove(it) }

        moves.forEach { (oldId, book) ->
            step {
                val language = book.languageCode
                var savedHere = false
                folders.forEach { folder ->
                    val from = File(folder, "${language}_$oldId.pdf")
                    val to = File(folder, "${language}_${book.id}.pdf")
                    if (from.isFile) {
                        // A copy already under the library's id is the same PDF; it is kept as it is.
                        if (!to.isFile) pdfSuffixes.forEach { move(File(from.path + it), File(to.path + it)) }
                        if (folder == savedFolder) savedHere = true
                    }
                    (pdfSuffixes + ".part").forEach { File(from.path + it).delete() }
                }
                File(ranges, oldId).deleteRecursively()
                if (savedHere && File(savedFolder, "${language}_${book.id}.pdf").isFile && listed.add(book.id)) {
                    kept.put(WorldLibrary.savedEntry(book))
                    listChanged = true
                }
            }
            moveState(state, edit, oldId, book.id)
        }

        if (listChanged) step {
            worldSaved.parentFile?.mkdirs()
            val temporary = File(worldSaved.path + ".tmp")
            temporary.writeText(kept.toString())
            if (!temporary.renameTo(worldSaved)) {
                worldSaved.writeText(kept.toString())
                temporary.delete()
            }
            removedBooks += removedEntries
        }

        if (removedBooks > 0) {
            val library = context.getSharedPreferences(LIBRARY_PREFS, Context.MODE_PRIVATE)
            val pending = runCatching { library.getInt(REMOVED_KEY, 0) }.getOrDefault(0)
            library.edit().putInt(REMOVED_KEY, pending + removedBooks).commit()
        }
        if (complete) edit.putBoolean(DONE, true)
        edit.commit()
        return true
    }

    /** Moves the reading position, bookmarks and last-opened time; state already under [newId] wins. */
    private fun moveState(all: Map<String, *>, edit: SharedPreferences.Editor, oldId: String, newId: String) {
        statePrefixes.forEach { prefix ->
            val value = all[prefix + oldId] ?: return@forEach
            if (all[prefix + newId] == null) when (value) {
                is Int -> edit.putInt(prefix + newId, value)
                is Long -> edit.putLong(prefix + newId, value)
                is String -> edit.putString(prefix + newId, value)
            }
            edit.remove(prefix + oldId)
        }
    }

    private fun move(from: File, to: File) {
        if (!from.exists()) return
        to.delete()
        if (!from.renameTo(to)) {
            from.copyTo(to, overwrite = true)
            from.delete()
        }
    }
}
