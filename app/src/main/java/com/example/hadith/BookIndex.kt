package com.example.hadith

enum class IndexSort(val label: String) {
    PAGE_ASC("Page first-last"), PAGE_DESC("Page last-first"),
    TITLE_ASC("Title A-Z"), TITLE_DESC("Title Z-A")
}

/** One line of a book's index. [depth] is its nesting level in the PDF's own bookmarks. */
data class IndexEntry(val title: String, val page: Int, val depth: Int = 0)

/** Turns a PDF's bookmarks into an index that is in true page order and can be re-sorted. */
object BookIndex {
    private val spaces = Regex("""\s+""")

    /**
     * Bookmarks are stored with two leading spaces per nesting level. Entries pointing outside the
     * book are dropped, and the rest are put in page order: a PDF may list its bookmarks in any
     * order, and a stable sort keeps a heading ahead of the sub-headings that share its page.
     */
    fun entries(chapters: List<HadithChapter>, pageCount: Int): List<IndexEntry> = chapters
        .mapNotNull { chapter ->
            val title = chapter.title.trim().replace(spaces, " ")
            if (title.isEmpty() || chapter.page !in 1..pageCount) return@mapNotNull null
            IndexEntry(title, chapter.page, chapter.title.takeWhile { it == ' ' }.length / 2)
        }
        .distinctBy { it.title to it.page }
        .sortedBy { it.page }

    /** Nesting only means something in reading order, so every other view is a flat list. */
    fun visible(entries: List<IndexEntry>, query: String, sort: IndexSort): List<IndexEntry> {
        val needle = query.trim()
        val nested = needle.isEmpty() && sort == IndexSort.PAGE_ASC
        val matching = entries.filter { it.title.contains(needle, ignoreCase = true) }
            .let { list -> if (nested) list else list.map { it.copy(depth = 0) } }
        val byTitle = compareBy<IndexEntry, String>(BookTitles.naturalOrder) { it.title }.thenBy { it.page }
        return when (sort) {
            IndexSort.PAGE_ASC -> matching
            IndexSort.PAGE_DESC -> matching.sortedByDescending { it.page }
            IndexSort.TITLE_ASC -> matching.sortedWith(byTitle)
            IndexSort.TITLE_DESC -> matching.sortedWith(byTitle.reversed())
        }
    }

    /** The entry being read on [page]: the deepest heading that starts on or before it. */
    fun current(entries: List<IndexEntry>, page: Int): IndexEntry? = entries.lastOrNull { it.page <= page }
}
