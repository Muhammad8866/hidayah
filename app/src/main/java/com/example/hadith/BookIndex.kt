package com.example.hadith

import com.example.core.search.AppSearch

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

    /**
     * Nesting only means something in reading order, so every other view is a flat list. A search
     * runs the app's shared engine over the titles ([AppSearch]: any spelling or script, typos, a
     * half-typed word, any word order). In the default page order the best match comes first; any
     * other order sorts the matches. A query with no searchable word shows the whole index.
     */
    fun visible(entries: List<IndexEntry>, query: String, sort: IndexSort): List<IndexEntry> {
        val needle = query.trim()
        // The engine reads only the start of a query, so a pasted page is not folded whole here.
        val searching = BookSearchText.normalize(needle.take(SEARCHED_CHARS)).isNotEmpty()
        val matching = if (searching) search(entries).search(needle) else entries
        val nested = !searching && sort == IndexSort.PAGE_ASC
        val shown = if (nested) matching else matching.map { it.copy(depth = 0) }
        val byTitle = compareBy<IndexEntry, String>(BookTitles.naturalOrder) { it.title }.thenBy { it.page }
        return when (sort) {
            IndexSort.PAGE_ASC -> shown
            IndexSort.PAGE_DESC -> shown.sortedByDescending { it.page }
            IndexSort.TITLE_ASC -> shown.sortedWith(byTitle)
            IndexSort.TITLE_DESC -> shown.sortedWith(byTitle.reversed())
        }
    }

    private const val SEARCHED_CHARS = 200

    /** The open book's index, made searchable once and reused for every keystroke. */
    @Volatile private var lastSearch: Pair<List<IndexEntry>, AppSearch.Ranker<IndexEntry>>? = null

    private fun search(entries: List<IndexEntry>): AppSearch.Ranker<IndexEntry> {
        lastSearch?.let { (list, ranker) -> if (list === entries) return ranker }
        return AppSearch.Ranker(entries) { AppSearch.Fields(title = it.title) }.also { lastSearch = entries to it }
    }

    /** The entry being read on [page]: the deepest heading that starts on or before it. */
    fun current(entries: List<IndexEntry>, page: Int): IndexEntry? = entries.lastOrNull { it.page <= page }
}
