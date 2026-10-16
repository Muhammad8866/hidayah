package com.example.quran

import android.content.Context

/** User reading choices, scoped to each edition so page numbers never cross mushafs. */
class QuranReaderPreferences(context: Context, private val book: QuranPdf) {
    private val prefs = context.applicationContext.getSharedPreferences("quran_reader_choices", Context.MODE_PRIVATE)
    private fun key(kind: String) = "${book.id}_$kind"
    private fun numbers(kind: String, range: IntRange): Set<Int> =
        prefs.getStringSet(key(kind), emptySet()).orEmpty()
            .mapNotNull { it.toIntOrNull() }.filter { it in range }.toSet()

    fun bookmarks(): Set<Int> = numbers("pages", 1..book.pageCount)
    fun favorites(): Set<Int> = numbers("surahs", 1..114)
    /** Page turn unless the reader chose another mode for this edition. */
    fun readingMode(): String = prefs.getString(key("mode"), "page")
        ?.takeIf { it in setOf("gallery", "page", "scroll", "vertical") } ?: "page"
    fun saveReadingMode(mode: String) {
        require(mode in setOf("gallery", "page", "scroll", "vertical"))
        prefs.edit().putString(key("mode"), mode).apply()
    }
    /** Shared by every edition: whether the volume keys open the reader menu. */
    fun volumeKeysMenu(): Boolean = prefs.getBoolean("volume_keys_menu", true)
    fun saveVolumeKeysMenu(enabled: Boolean) = prefs.edit().putBoolean("volume_keys_menu", enabled).apply()
    fun guideSeen(): Boolean = prefs.getBoolean("guide_seen", false)
    fun markGuideSeen() = prefs.edit().putBoolean("guide_seen", true).apply()
    fun toggleBookmark(page: Int): Set<Int> {
        require(page in 1..book.pageCount)
        return toggle("pages", page, bookmarks())
    }
    fun toggleFavorite(surah: Int): Set<Int> {
        require(surah in 1..114)
        return toggle("surahs", surah, favorites())
    }
    private fun toggle(kind: String, value: Int, old: Set<Int>): Set<Int> {
        val updated = if (value in old) old - value else old + value
        prefs.edit().putStringSet(key(kind), updated.map { it.toString() }.toSet()).apply()
        return updated
    }
}

