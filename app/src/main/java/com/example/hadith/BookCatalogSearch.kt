package com.example.hadith

import java.util.Arrays
import kotlin.math.abs

/**
 * The search engine behind the book lists (Islamic Books and World Library). It finds a book by
 * its title, script title, author or other names however the words are typed:
 * - in any case, with or without accents and Arabic vowel marks ([BookSearchText.normalize]);
 * - in any spelling or Indic script: namaz, namaaz, नमाज़ and নামাজ are one word ([BookSearchText.soundKey]);
 * - joined or split: "bahare shariat", "bahar e shariat" and "baharishariat" all find Bahar-e-Shariat;
 * - with a small typo, and while the last word is still being typed (prefix);
 * - in any word order.
 * Every word of the query must be found, except short joining words ("e", "ul", "ki") that the
 * title may not carry. Matches are ranked: a title counts most, then the script title, the author
 * and other names; exact words beat sound-alike words, which beat prefixes and typos.
 *
 * A score depends only on the book and the query, never on the rest of the list, so the same book
 * scores the same in any list. Pure Kotlin with no Android types, so it is unit-tested on the JVM.
 * It only reads [BookSearchText]; that file is shared with tools/book_search/build_index.py and is
 * not changed here.
 */
object BookCatalogSearch {
    /** Where a word was found; a title word counts most. */
    enum class Field(val weight: Int) { TITLE(10), NATIVE_TITLE(8), AUTHOR(5), OTHER(3) }

    /** The searchable text of one book. [other] holds volume titles, subjects and other names. */
    data class Entry(
        val title: String,
        val nativeTitle: String = "",
        val author: String = "",
        val other: List<String> = emptyList()
    )

    /** No match. */
    const val TIER_NONE = 0
    /** Matched only through other fields, sound-alike words, prefixes or typos. */
    const val TIER_LOOSE = 1
    /** Every word found in the title or script title. */
    const val TIER_TITLE_WORDS = 2
    /** The title or script title holds the query as a phrase, or is the query. */
    const val TIER_TITLE_PHRASE = 3

    /** Each tier is worth more than any score within a lower one, so the tier reads back from the score. */
    private const val TIER_UNIT = 10_000_000

    /** A query is cut to this many characters and words, so a pasted page cannot stall a keystroke. */
    private const val MAX_QUERY_CHARS = 200
    private const val MAX_TERMS = 12
    /** One field of a book is read up to this many characters and words. */
    private const val MAX_FIELD_CHARS = 1_000
    private const val MAX_FIELD_WORDS = 64
    /** A query word this short ("e", "ul", "ki") is a joining word that a title may leave out. */
    private const val SHORT_WORD = 2
    /** A word this short may sit inside a joined spelling of three words ("maal e virasat", "mir aat ul"). */
    private const val JOINING_WORD = 3

    // How well a query word matched a book's word, before the field's weight.
    private const val EXACT = 100
    private const val SOUND = 90
    private const val JOINED = 80
    private const val PREFIX = 60
    private const val TYPO_ONE = 50
    private const val TYPO_TWO = 40
    /** Below the step between two kinds of match, so an exact word still beats a sound-alike at the start. */
    private const val STARTS_BONUS = 40
    private const val WHOLE_BONUS = 80

    // What a vocabulary string is for a book: one of its words, a word's sound key, or adjacent sound keys joined.
    private const val AS_WORD = 0
    private const val AS_SOUND = 1
    private const val AS_JOINED = 2

    // Where a query sits in a title.
    private const val NO_PHRASE = 0
    private const val INSIDE = 1
    private const val STARTS = 2
    private const val WHOLE = 3

    private val weights = Field.entries.map { it.weight }.toIntArray()
    private val titleFields = (1 shl Field.TITLE.ordinal) or (1 shl Field.NATIVE_TITLE.ordinal)

    /**
     * Coarse relevance of a score from [Index.scores], for lists that keep a chosen sort (A-Z,
     * newest) while better matches still lead: higher is better, [TIER_NONE] is no match.
     */
    fun tier(score: Int): Int = if (score <= 0) TIER_NONE else (score / TIER_UNIT).coerceIn(TIER_LOOSE, TIER_TITLE_PHRASE)

    /** Edits allowed between two sound keys, from the shorter one's length: none below four letters. */
    private fun typoLimit(length: Int) = when {
        length >= 7 -> 2
        length >= 4 -> 1
        else -> 0
    }

    /** A growing list of postings for one vocabulary string while the index is built. */
    private class Postings {
        var data = IntArray(2)
        var size = 0

        fun add(posting: Int) {
            if (size > 0 && data[size - 1] == posting) return
            if (size == data.size) data = data.copyOf(size * 2)
            data[size++] = posting
        }
    }

    /**
     * Built once per list of books, so a search per keystroke only folds the query and never
     * re-folds every book. Safe to read from several threads once built.
     */
    class Index(entries: List<Entry>) {
        val size: Int = entries.size

        /** Every distinct word, sound key and joined key of the list, sorted, so a prefix is one range. */
        private val vocabulary: Array<String>
        /**
         * The postings of vocabulary[i] are postings[postingStart[i] until postingStart[i + 1]], each
         * `book shl 4 or field shl 2 or kind`.
         */
        private val postingStart: IntArray
        private val postings: IntArray
        /** Vocabulary positions by length: strings of length n are byLength[lengthStart[n] until lengthStart[n + 1]]. */
        private val byLength: IntArray
        private val lengthStart: IntArray
        /** Sound keys of each book's title and script title, and which of those words are short, for phrases. */
        private val titleKeys: Array<Array<String>>
        private val titleShort: Array<BooleanArray>
        private val nativeKeys: Array<Array<String>>
        private val nativeShort: Array<BooleanArray>

        init {
            require(size < 1 shl 27) { "Too many books for one search index" }
            val sounds = HashMap<String, String>()
            val lists = HashMap<String, Postings>()
            fun add(text: String, posting: Int) = lists.getOrPut(text) { Postings() }.add(posting)
            val none = emptyArray<String>()
            val noFlags = BooleanArray(0)
            val tKeys = Array(size) { none }
            val tShort = Array(size) { noFlags }
            val nKeys = Array(size) { none }
            val nShort = Array(size) { noFlags }

            /** Adds one field's words; returns their sound keys and which words are short. */
            fun read(book: Int, field: Field, text: String): Pair<Array<String>, BooleanArray>? {
                if (text.isBlank()) return null
                val words = BookSearchText.normalize(text.take(MAX_FIELD_CHARS)).take(MAX_FIELD_WORDS)
                if (words.isEmpty()) return null
                val keys = Array(words.size) { sounds.getOrPut(words[it]) { BookSearchText.soundKey(words[it]) } }
                val base = (book shl 4) or (field.ordinal shl 2)
                for (i in words.indices) {
                    add(words[i], base or AS_WORD)
                    add(keys[i], base or AS_SOUND)
                }
                if (field != Field.OTHER) for (i in 0 until words.size - 1) {
                    add(keys[i] + keys[i + 1], base or AS_JOINED)
                    if (i + 2 < words.size && words[i + 1].length <= JOINING_WORD) {
                        add(keys[i] + keys[i + 1] + keys[i + 2], base or AS_JOINED)
                    }
                }
                return keys to BooleanArray(words.size) { words[it].length <= SHORT_WORD }
            }

            entries.forEachIndexed { book, entry ->
                read(book, Field.TITLE, entry.title)?.let { tKeys[book] = it.first; tShort[book] = it.second }
                read(book, Field.NATIVE_TITLE, entry.nativeTitle)?.let { nKeys[book] = it.first; nShort[book] = it.second }
                read(book, Field.AUTHOR, entry.author)
                entry.other.forEach { read(book, Field.OTHER, it) }
            }
            titleKeys = tKeys
            titleShort = tShort
            nativeKeys = nKeys
            nativeShort = nShort

            val words = lists.keys.toTypedArray()
            words.sort()
            val starts = IntArray(words.size + 1)
            var total = 0
            for (i in words.indices) {
                starts[i] = total
                total += lists.getValue(words[i]).size
            }
            starts[words.size] = total
            val flat = IntArray(total)
            for (i in words.indices) {
                val list = lists.getValue(words[i])
                System.arraycopy(list.data, 0, flat, starts[i], list.size)
            }
            vocabulary = words
            postingStart = starts
            postings = flat

            val longest = words.maxOfOrNull { it.length } ?: 0
            val lengths = IntArray(longest + 2)
            for (word in words) lengths[word.length + 1]++
            for (n in 1 until lengths.size) lengths[n] += lengths[n - 1]
            val ordered = IntArray(words.size)
            val fill = lengths.copyOf()
            for (i in words.indices) ordered[fill[words[i].length]++] = i
            lengthStart = lengths
            byLength = ordered
        }

        /**
         * The score of every entry by its position in the list given: 0 when it does not match,
         * higher for a better match. Null when [query] holds no searchable word, which means every
         * entry is shown as listed. [typos] false finds only exact, sound-alike, joined and
         * prefix matches, for a first pass that tries typos only when nothing else matched.
         */
        fun scores(query: String, typos: Boolean = true): IntArray? {
            val words = BookSearchText.normalize(query.take(MAX_QUERY_CHARS)).take(MAX_TERMS)
            if (words.isEmpty()) return null
            val out = IntArray(size)
            if (size == 0) return out
            Search(words, words.map(BookSearchText::soundKey), typos).run(out)
            return out
        }

        /**
         * [scores] as a search engine answers: typos are tried only when nothing matches without
         * them ("did you mean"), so a correct word is not buried under look-alikes one letter off.
         * Null, as in [scores], when [query] holds no searchable word.
         */
        fun bestScores(query: String): IntArray? {
            val strict = scores(query, typos = false) ?: return null
            return if (strict.any { it > 0 }) strict else scores(query, typos = true)
        }

        /** One query run against the index; it holds that query's working arrays. */
        private inner class Search(
            private val words: List<String>,
            private val keys: List<String>,
            private val withTypos: Boolean = true
        ) {
            private val terms = words.size
            /** Best value of each query word in each book, at book * terms + word. */
            private val best = IntArray(size * terms)
            /** Bits of the query words found in each book's title or script title. */
            private val inTitle = IntArray(size)
            private val touched = IntArray(size)
            private var touchedCount = 0
            private val seen = BooleanArray(size)
            // Distance rows, reused for every comparison of this query.
            private var before = IntArray(0)
            private var previous = IntArray(0)
            private var current = IntArray(0)

            /** Position of [text] in the vocabulary, or a negative number when it is not there. */
            private fun find(text: String): Int = Arrays.binarySearch(vocabulary, text)

            private inline fun forPostings(word: Int, action: (Int) -> Unit) {
                if (word < 0) return
                for (k in postingStart[word] until postingStart[word + 1]) action(postings[k])
            }

            /** Every vocabulary string longer than [prefix] that starts with it. */
            private inline fun forLonger(prefix: String, action: (Int) -> Unit) {
                val found = find(prefix)
                var at = if (found >= 0) found + 1 else -found - 1
                while (at < vocabulary.size && vocabulary[at].startsWith(prefix)) action(at++)
            }

            private fun hit(posting: Int, term: Int, kind: Int) {
                val book = posting ushr 4
                val field = (posting ushr 2) and 3
                if (!seen[book]) {
                    seen[book] = true
                    touched[touchedCount++] = book
                }
                val slot = book * terms + term
                val value = kind * weights[field]
                if (value > best[slot]) best[slot] = value
                if (((1 shl field) and titleFields) != 0) inTitle[book] = inTitle[book] or (1 shl term)
            }

            fun run(out: IntArray) {
                for (term in 0 until terms) match(term)
                matchSplit()
                finish(out)
            }

            private fun match(term: Int) {
                val word = words[term]
                val key = keys[term]
                forPostings(find(word)) { if ((it and 3) == AS_WORD) hit(it, term, EXACT) }
                forPostings(find(key)) { hit(it, term, if ((it and 3) == AS_JOINED) JOINED else SOUND) }
                // Only the last word may still be half typed; any other needs two letters to count as a prefix.
                val shortest = if (term == terms - 1) 1 else 2
                if (word.length >= shortest) forLonger(word) { at -> forPostings(at) { if ((it and 3) == AS_WORD) hit(it, term, PREFIX) } }
                // A sound key folded much shorter than what was typed ("zzzz" is "j") is no prefix of
                // every "j" word; a single typed letter still is.
                if (key.length >= maxOf(shortest, minOf(word.length, 2))) forLonger(key) { at -> forPostings(at) { if ((it and 3) != AS_WORD) hit(it, term, PREFIX) } }
                if (withTypos) typos(term, key)
            }

            /** Sound keys and joined keys a few edits from [key], found by length bucket. */
            private fun typos(term: Int, key: String) {
                val widest = typoLimit(key.length)
                if (widest == 0) return
                val rowSize = key.length + widest + 1
                if (previous.size < rowSize) {
                    before = IntArray(rowSize)
                    previous = IntArray(rowSize)
                    current = IntArray(rowSize)
                }
                val from = (key.length - widest).coerceAtLeast(1)
                val to = minOf(key.length + widest, lengthStart.size - 2)
                for (length in from..to) {
                    val limit = typoLimit(minOf(key.length, length))
                    if (limit == 0 || abs(length - key.length) > limit) continue
                    for (j in lengthStart[length] until lengthStart[length + 1]) {
                        val at = byLength[j]
                        val distance = distance(key, vocabulary[at], limit)
                        if (distance == 0 || distance > limit) continue
                        val kind = if (distance == 1) TYPO_ONE else TYPO_TWO
                        forPostings(at) { if ((it and 3) != AS_WORD) hit(it, term, kind) }
                    }
                }
            }

            /** Query words typed apart that a title writes as one: "bahar e" for "Bahare", "faizan e sunnat" for "Faizanesunnat". */
            private fun matchSplit() {
                for (count in 2..3) for (start in 0..terms - count) {
                    val joined = buildString { for (term in start until start + count) append(keys[term]) }
                    forPostings(find(joined)) { posting -> for (term in start until start + count) hit(posting, term, JOINED) }
                }
            }

            private fun finish(out: IntArray) {
                val allShort = words.all { it.length <= SHORT_WORD }
                var required = 0
                for (term in 0 until terms) if (allShort || words[term].length > SHORT_WORD) required = required or (1 shl term)
                val whole = keys.joinToString("")
                val withoutShort = keys.filterIndexed { term, _ -> words[term].length > SHORT_WORD }.joinToString("")
                for (i in 0 until touchedCount) {
                    val book = touched[i]
                    var sum = 0
                    var found = 0
                    for (term in 0 until terms) {
                        val value = best[book * terms + term]
                        if (value > 0) {
                            sum += value
                            found = found or (1 shl term)
                        }
                    }
                    if ((found and required) != required) continue
                    val phrase = maxOf(
                        phrase(titleKeys[book], titleShort[book], whole, withoutShort),
                        phrase(nativeKeys[book], nativeShort[book], whole, withoutShort)
                    )
                    val tier = when {
                        phrase != NO_PHRASE -> TIER_TITLE_PHRASE
                        (inTitle[book] and required) == required -> TIER_TITLE_WORDS
                        else -> TIER_LOOSE
                    }
                    sum += when (phrase) {
                        WHOLE -> WHOLE_BONUS
                        STARTS -> STARTS_BONUS
                        else -> 0
                    }
                    out[book] = tier * TIER_UNIT + sum.coerceIn(1, TIER_UNIT - 1)
                }
            }

            /**
             * Optimal string alignment distance of [a] and [b], or more than [limit] as soon as it
             * is sure to be. A row whose smallest value passes the limit ends it: the next rows,
             * swaps included, can only be larger.
             */
            private fun distance(a: String, b: String, limit: Int): Int {
                val n = a.length
                val m = b.length
                if (abs(n - m) > limit) return limit + 1
                var older = before
                var last = previous
                var row = current
                for (j in 0..m) last[j] = j
                for (i in 1..n) {
                    row[0] = i
                    var smallest = i
                    for (j in 1..m) {
                        val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                        var value = minOf(last[j] + 1, row[j - 1] + 1, last[j - 1] + cost)
                        if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) value = minOf(value, older[j - 2] + 1)
                        row[j] = value
                        if (value < smallest) smallest = value
                    }
                    if (smallest > limit) return limit + 1
                    val spare = older
                    older = last
                    last = row
                    row = spare
                }
                return minOf(last[m], limit + 1)
            }
        }
    }

    /** How a query sits in one title: [NO_PHRASE], [INSIDE], [STARTS] or [WHOLE]. */
    private fun phrase(keys: Array<String>, short: BooleanArray, whole: String, withoutShort: String): Int {
        if (keys.isEmpty()) return NO_PHRASE
        val full = lineUp(keys, short, skipShort = false, query = whole)
        if (full == WHOLE || withoutShort.isEmpty()) return full
        // Joining words left out on either side still make a phrase: "faizan sunnat" in "Faizan-e-Sunnat".
        return maxOf(full, lineUp(keys, short, skipShort = true, query = withoutShort))
    }

    /**
     * Whether [query] (sound keys run together) reads from the start of one of the title's words
     * onwards, the last word allowed to be half typed. The words run together, so a query
     * written joined or split still lines up: "bahare shariat" with "bahar e shariat".
     */
    private fun lineUp(keys: Array<String>, short: BooleanArray, skipShort: Boolean, query: String): Int {
        if (query.isEmpty()) return NO_PHRASE
        var level = NO_PHRASE
        var first = true
        for (start in keys.indices) {
            if (skipShort && short[start]) continue
            val atStart = first
            first = false
            var used = 0
            var at = start
            var matched = false
            var whole = false
            while (at < keys.size) {
                if (skipShort && short[at]) {
                    at++
                    continue
                }
                val key = keys[at]
                val length = minOf(key.length, query.length - used)
                if (!key.regionMatches(0, query, used, length)) break
                used += length
                if (used == query.length) {
                    matched = true
                    if (length == key.length) {
                        var next = at + 1
                        while (next < keys.size && skipShort && short[next]) next++
                        whole = next == keys.size
                    }
                    break
                }
                at++
            }
            if (!matched) continue
            val found = when {
                !atStart -> INSIDE
                whole -> WHOLE
                else -> STARTS
            }
            if (found > level) level = found
            if (level == WHOLE) return level
        }
        return level
    }

    /**
     * The items that match [query], best first; equal scores keep the order of [items]. A query
     * with no searchable word returns [items] unchanged.
     */
    fun <T> rank(query: String, items: List<T>, entry: (T) -> Entry): List<T> {
        val scores = Index(items.map(entry)).scores(query) ?: return items
        return items.indices.filter { scores[it] > 0 }.sortedByDescending { scores[it] }.map { items[it] }
    }
}
