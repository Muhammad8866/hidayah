package com.example.core.search

import com.example.hadith.BookCatalogSearch

/**
 * The one search behaviour shared by every search box in the app, so each list finds things the
 * same way a search engine does. It runs [BookCatalogSearch] (built for the book lists) over any
 * list and adds what other lists need:
 * - any case, accents, Arabic vowel marks, spelling and Indic script ("namaz", "नमाज़", "নামাজ");
 * - words joined or split ("yasin", "ya sin"), any word order, a half-typed last word;
 * - small typos, but only when nothing matches without them ("did you mean"), so a correct
 *   word is never buried under look-alikes;
 * - every word must match; the title counts most, then the script title, the author, other text;
 * - a number finds the item with that number first ("2" is Al-Baqarah, not every surah from 20),
 *   in any digits (٢, २);
 * - long text (a dua, a translation) is searched to its last word.
 * A query with no searchable word (blank, punctuation, emoji) keeps the list as it is.
 *
 * Pure Kotlin with no Android types, so it is unit-tested on the JVM. [BookCatalogSearch] and
 * BookSearchText are not changed here: BookSearchText is shared with the book-search index tool.
 */
object AppSearch {
    /**
     * The searchable text of one item. [title] counts most, then [nativeTitle] (its name in its own
     * script), [author], and [other] (meanings, translations, localized names, body text). [number]
     * is the item's own number (a surah's, a lesson's), found exactly by a typed number.
     */
    data class Fields(
        val title: String,
        val nativeTitle: String = "",
        val author: String = "",
        val other: List<String> = emptyList(),
        val number: Int? = null
    )

    /** Longer text is split into pieces this size, below the engine's limit of 64 words per field. */
    private const val CHUNK_WORDS = 40
    private const val CHUNK_CHARS = 800
    /** A typed number longer than this is not an item number. */
    private const val MAX_NUMBER_DIGITS = 9
    private val spaces = Regex("\\s+")

    /**
     * A list made searchable once; [search] then costs one pass over the query per keystroke.
     * Build it once per list (remember it), not once per keystroke. Safe to call from any thread.
     */
    class Ranker<T>(private val items: List<T>, fields: (T) -> Fields) {
        private val numbers: List<Int?>
        private val index: BookCatalogSearch.Index

        init {
            val all = items.map(fields)
            numbers = all.map { it.number }
            index = BookCatalogSearch.Index(all.map { entry(it) })
        }

        @Volatile private var last: Pair<String, List<T>>? = null

        /** The items that match [query], best first; equal matches keep the list's order. */
        fun search(query: String): List<T> {
            val key = query.trim()
            last?.let { (previous, found) -> if (previous == key) return found }
            return find(key).also { last = key to it }
        }

        private fun find(query: String): List<T> {
            val number = asNumber(query)
            // Typos only when nothing matches without them: "ayesha" lists Aisha alone, not Aida and
            // Raisah one letter away, while "baqra" (no exact match) still finds Al-Baqarah.
            val scores = index.bestScores(query)
            if (number == null) {
                scores ?: return items
                return items.indices.filter { scores[it] > 0 }.sortedByDescending { scores[it] }.map(items::get)
            }
            // A typed number: that number first, then numbers that start with it, then any text match.
            val typed = number.toString()
            val exact = items.indices.filter { numbers[it] == number }
            val starting = items.indices
                .filter { numbers[it] != null && numbers[it] != number && numbers[it].toString().startsWith(typed) }
                .sortedBy { numbers[it] }
            val text = scores?.let { s -> items.indices.filter { s[it] > 0 }.sortedByDescending { s[it] } }.orEmpty()
            return (exact + starting + text).distinct().map(items::get)
        }
    }

    /** The items of [items] that match [query], best first. Builds the search once; for one-off lists. */
    fun <T> rank(query: String, items: List<T>, fields: (T) -> Fields): List<T> = Ranker(items, fields).search(query)

    /** Whether [fields] match [query]; a query with no searchable word matches everything. */
    fun matches(query: String, fields: Fields): Boolean = rank(query, listOf(fields)) { it }.isNotEmpty()

    /**
     * The query as a whole number, in any script's digits ("٣٦", "३६" and "36" are 36), or null
     * when it is not only digits.
     */
    internal fun asNumber(query: String): Int? {
        val text = query.trim()
        if (text.isEmpty() || text.length > MAX_NUMBER_DIGITS || !text.all { it.isDigit() }) return null
        val ascii = buildString { text.forEach { append(Character.digit(it, 10).coerceAtLeast(0)) } }
        return ascii.toIntOrNull()
    }

    private fun entry(fields: Fields) = BookCatalogSearch.Entry(
        title = fields.title,
        nativeTitle = fields.nativeTitle,
        author = fields.author,
        other = fields.other.flatMap(::chunks)
    )

    /** [text] in pieces short enough for the engine to read every word. */
    internal fun chunks(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.trim().split(spaces)
        if (words.size <= CHUNK_WORDS && text.length <= CHUNK_CHARS) return listOf(text)
        val out = ArrayList<String>()
        val piece = StringBuilder()
        var count = 0
        for (word in words) {
            // A single word longer than a piece is cut, so no piece passes the limit.
            val part = word.take(CHUNK_CHARS)
            if (piece.isNotEmpty() && (count == CHUNK_WORDS || piece.length + part.length + 1 > CHUNK_CHARS)) {
                out += piece.toString()
                piece.setLength(0)
                count = 0
            }
            if (piece.isNotEmpty()) piece.append(' ')
            piece.append(part)
            count++
        }
        if (piece.isNotEmpty()) out += piece.toString()
        return out
    }
}
