package com.example.hadith

import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/**
 * Reads a PDF's bookmarks as chapters. pdfbox parses every stream of the file while loading, and
 * its default buffering keeps all of them on the Java heap: a 200 MB scanned book filled the
 * 256 MB heap and crashed the app. So only [MAX_HEAP_BYTES] stay in memory, the rest goes to a
 * scratch file that is removed afterwards, and one book is parsed at a time.
 */
internal object PdfOutline {
    /** Stream data pdfbox may keep on the Java heap while parsing; the rest is spilled to disk. */
    private const val MAX_HEAP_BYTES = 8L shl 20
    private val lock = Any()

    internal fun memorySetting(scratchDir: File): MemoryUsageSetting =
        MemoryUsageSetting.setupMixed(MAX_HEAP_BYTES).setTempDir(scratchDir)

    /** [read], with a file that cannot be read shown as having no chapters. */
    fun chapters(file: File, scratchDir: File): List<HadithChapter> = read(file, scratchDir).orEmpty()

    /**
     * Parsing a large PDF for its bookmarks takes seconds, so the result is kept in [sidecar] and
     * reused for as long as the file's size is unchanged. A parse that failed (damaged file, no
     * room for the scratch file) is not kept, so the next open tries again instead of losing the
     * book's bookmarks for good.
     */
    fun cached(file: File, sidecar: File, scratchDir: File): List<HadithChapter> {
        runCatching {
            val json = JSONObject(sidecar.readText())
            if (json.getLong("size") == file.length()) {
                val items = json.getJSONArray("chapters")
                return List(items.length()) { index ->
                    items.getJSONObject(index).let { HadithChapter(it.getString("title"), it.getInt("page")) }
                }
            }
        }
        val chapters = read(file, scratchDir) ?: return emptyList()
        runCatching {
            val items = JSONArray()
            chapters.forEach { items.put(JSONObject().put("title", it.title).put("page", it.page)) }
            sidecar.writeText(JSONObject().put("size", file.length()).put("chapters", items).toString())
        }
        return chapters
    }

    /**
     * The chapter bookmarks of [file]: empty when it has none or only names its pages, null when it
     * cannot be read. [scratchDir] belongs to this reader: whatever is in it is removed before and after.
     */
    fun read(file: File, scratchDir: File): List<HadithChapter>? = synchronized(lock) {
        // A parse cut short by the process being killed leaves its scratch file behind.
        clear(scratchDir)
        try {
            runCatching {
                // If the folder cannot be made, pdfbox rejects the setting: no outline, never unbounded.
                scratchDir.mkdirs()
                PDDocument.load(file, memorySetting(scratchDir)).use { pdf ->
                    val result = mutableListOf<HadithChapter>()
                    fun visit(first: PDOutlineItem?, depth: Int) {
                        var item = first
                        while (item != null) {
                            val destination = item.destination ?: (item.action as? PDActionGoTo)?.destination
                            val page = (destination as? PDPageDestination)?.retrievePageNumber()?.plus(1)
                            val title = item.title?.trim().orEmpty()
                            if (page != null && page > 0 && title.isNotBlank()) {
                                result += HadithChapter("${"  ".repeat(depth)}$title", page)
                            }
                            visit(item.firstChild, depth + 1)
                            item = item.nextSibling
                        }
                    }
                    visit(pdf.documentCatalog.documentOutline?.firstChild, 0)
                    // Scans that bookmark every page by its image name have no chapter index.
                    OutlineFilter.chapterIndex(result.distinctBy { it.title to it.page })
                }
            }.getOrNull()
        } finally {
            clear(scratchDir)
        }
    }

    private fun clear(scratchDir: File) {
        scratchDir.listFiles()?.forEach { it.delete() }
    }
}
