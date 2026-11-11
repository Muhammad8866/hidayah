package com.example.hadith

/**
 * Scanned PDFs often carry one bookmark per page named after the page's image file
 * ("SSM4_0001Nafseislam", "…_Page_0001"). Such an outline is no chapter index: it would bury the
 * reader's index under a thousand page names, so it is dropped and the book reads without one.
 */
internal object OutlineFilter {
    /** Below this many entries an outline is always kept. */
    private const val MIN_ENTRIES = 20
    private val digits = Regex("\\p{Nd}+")

    /**
     * True when nine in ten entries have the same title apart from their digits and nine in ten
     * steps go to the very next page: a page list, not chapters.
     */
    fun isPageLabels(chapters: List<HadithChapter>): Boolean {
        if (chapters.size < MIN_ENTRIES) return false
        val sameShape = chapters.groupingBy { digits.replace(it.title.trim(), "#") }.eachCount().values.maxOrNull() ?: 0
        if (sameShape * 10 < chapters.size * 9) return false
        val nextPage = chapters.zipWithNext().count { (a, b) -> b.page - a.page == 1 }
        return nextPage * 10 >= (chapters.size - 1) * 9
    }

    /** [chapters] as the reader's index, or nothing when they only name pages. */
    fun chapterIndex(chapters: List<HadithChapter>): List<HadithChapter> =
        if (isPageLabels(chapters)) emptyList() else chapters
}
