package com.example.hadith

import java.text.Normalizer
import java.util.Locale

/**
 * Text folding shared with tools/book_search/build_index.py, which builds the search index.
 * Both sides must fold a word the same way or a query stops matching what was indexed, so any
 * change here needs the same change there and a rebuilt index. BookSearchTextTest checks this
 * file against vectors that script writes.
 */
object BookSearchText {
    private val charMap = mapOf(
        'ى' to 'ي', 'ی' to 'ي', 'ے' to 'ي', 'ک' to 'ك', 'ہ' to 'ه', 'ھ' to 'ه', 'ة' to 'ه', 'ۃ' to 'ه'
    )
    private val dropped = setOf(0x640, 0x670, 0x93C, 0x200C, 0x200D, 0x27, 0x60, 0x2018, 0x2019, 0x2BC)
    private val articlePrefixes = listOf("وال", "بال", "فال", "كال")
    private val consonantH = Regex("(?<=[^aeiou])h")
    private val hardC = Regex("c(?!h)")
    private val repeats = Regex("(.)\\1+")

    // The Indic scripts share one layout, each in its own 128-character block, so one table read
    // by offset within the block covers Devanagari, Bengali, Gurmukhi, Gujarati, Odia, Tamil,
    // Telugu, Kannada and Malayalam.
    private const val INDIC_FIRST = 0x900
    private const val INDIC_LAST = 0xD7F
    private const val INDIC_VIRAMA = 0x4D
    private const val INDIC_NUKTA = 0x3C
    /** Written "ya", said "ja": যাকাত is zakat. */
    private const val BENGALI_YA = '\u09AF'
    private val indicConsonants = mapOf(
        0x15 to "k", 0x16 to "kh", 0x17 to "g", 0x18 to "gh", 0x19 to "ng", 0x1A to "ch", 0x1B to "chh",
        0x1C to "j", 0x1D to "jh", 0x1E to "ny", 0x1F to "t", 0x20 to "t", 0x21 to "d", 0x22 to "d",
        0x23 to "n", 0x24 to "t", 0x25 to "t", 0x26 to "d", 0x27 to "d", 0x28 to "n", 0x29 to "n",
        0x2A to "p", 0x2B to "ph", 0x2C to "b", 0x2D to "bh", 0x2E to "m", 0x2F to "y", 0x30 to "r",
        0x31 to "r", 0x32 to "l", 0x33 to "l", 0x34 to "l", 0x35 to "v", 0x36 to "sh", 0x37 to "sh",
        0x38 to "s", 0x39 to "h", 0x5C to "r", 0x5D to "r", 0x5F to "y"
    )
    private val indicVowels = mapOf(
        0x05 to "a", 0x06 to "aa", 0x07 to "i", 0x08 to "ee", 0x09 to "u", 0x0A to "oo", 0x0B to "ri",
        0x0D to "e", 0x0E to "e", 0x0F to "e", 0x10 to "ai", 0x11 to "o", 0x12 to "o", 0x13 to "o", 0x14 to "au"
    )
    private val indicSigns = mapOf(
        0x3E to "aa", 0x3F to "i", 0x40 to "ee", 0x41 to "u", 0x42 to "oo", 0x43 to "ri", 0x45 to "e",
        0x46 to "e", 0x47 to "e", 0x48 to "ai", 0x49 to "o", 0x4A to "o", 0x4B to "o", 0x4C to "au"
    )
    private val indicNasals = mapOf(0x01 to "n", 0x02 to "n", 0x03 to "h")

    private fun isDropped(code: Int) =
        code in 0x300..0x36F || code in 0x64B..0x65F || code in 0x6D6..0x6ED || code in dropped

    internal fun isWordChar(ch: Char): Boolean = when (Character.getType(ch).toByte()) {
        Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER, Character.TITLECASE_LETTER,
        Character.MODIFIER_LETTER, Character.OTHER_LETTER, Character.NON_SPACING_MARK,
        Character.COMBINING_SPACING_MARK, Character.ENCLOSING_MARK, Character.DECIMAL_DIGIT_NUMBER -> true
        else -> false
    }

    private fun stripArticle(token: String): String = when {
        // والدين (parents) and its family begin with the same letters as "and the ...".
        token.length > 5 && articlePrefixes.any(token::startsWith) && !token.startsWith("والد") -> token.substring(3)
        token.length > 4 && (token.startsWith("ال") || token.startsWith("لل")) -> token.substring(2)
        else -> token
    }

    /** Lower-cased words with accents, Arabic vowel marks and the Arabic article removed. */
    fun normalize(text: String): List<String> {
        // Plain English, most of what a page holds, needs no Unicode decomposition.
        val plain = text.all { it.code < 0x80 }
        val source = if (plain) text.lowercase(Locale.ROOT)
        else Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        val out = StringBuilder(source.length)
        for (ch in source) {
            if (isDropped(ch.code)) continue
            val mapped = if (plain) ch else charMap[ch] ?: ch
            out.append(if (isWordChar(mapped)) mapped else ' ')
        }
        if (plain) return out.split(' ').filter { it.isNotEmpty() }
        // Recomposing rejoins vowel signs that decomposition split in two (Bengali ো, Tamil ொ).
        return Normalizer.normalize(out, Normalizer.Form.NFC).split(' ').filter { it.isNotEmpty() }.map(::stripArticle)
    }

    private class Syllable(val consonant: String, var vowel: String, var unwritten: Boolean)

    /** Roman spelling of a word in an Indic script, or the word unchanged when it is not one. */
    fun romanize(token: String): String {
        if (token.isEmpty() || token.any { it.code !in INDIC_FIRST..INDIC_LAST }) return token
        val units = mutableListOf<Syllable>()
        // The true "ya" of Bengali is its own letter, or "ya" with a dot.
        for (ch in token.replace("\u09AF\u09BC", "\u09DF")) {
            val offset = ch.code and 0x7F
            val consonant = indicConsonants[offset]
            val vowel = indicVowels[offset]
            val sign = indicSigns[offset]
            val nasal = indicNasals[offset]
            when {
                ch == BENGALI_YA -> units += Syllable("j", "a", true)
                consonant != null -> units += Syllable(consonant, "a", true)
                vowel != null -> units += Syllable("", vowel, false)
                sign != null -> units.lastOrNull()?.let { it.vowel = sign; it.unwritten = false }
                offset == INDIC_VIRAMA -> units.lastOrNull()?.let { it.vowel = ""; it.unwritten = false }
                nasal != null -> units += Syllable(nasal, "", false)
                offset != INDIC_NUKTA -> return token
            }
        }
        if (units.isEmpty()) return token
        // The unwritten "a" is silent at the end of a word and between two voiced syllables
        // (रमज़ान is ramzan, not ramazana); deciding from the end backwards keeps मुसलमान "musalman".
        if (units.size > 1 && units.last().unwritten) units.last().let { it.vowel = ""; it.unwritten = false }
        for (index in units.size - 2 downTo 1) {
            val unit = units[index]
            if (unit.unwritten && units[index - 1].vowel.isNotEmpty() && units[index + 1].vowel.isNotEmpty()) {
                unit.vowel = ""
                unit.unwritten = false
            }
        }
        return units.joinToString("") { it.consonant + it.vowel }
    }

    /**
     * Folds the spellings of one word together: namaz, namaaz, nemaz, नमाज़, નમાઝ and নামাজ all
     * give "namaj". Words in other scripts (Arabic, Urdu) pass through unchanged.
     */
    fun soundKey(token: String): String {
        var word = romanize(token)
        if (word.isEmpty() || word.any { it !in 'a'..'z' }) return token
        word = word.replace("ph", "f").replace("dh", "z").replace("th", "s").replace("ck", "k")
        word = word.replace("ee", "i").replace("oo", "u").replace("q", "k").replace("w", "v").replace("z", "j")
        word = hardC.replace(word, "k")
        word = consonantH.replace(word, "")
        if (word.length > 2 && word.endsWith("h")) word = word.dropLast(1)
        word = word.replace('y', 'i').replace("ie", "i").replace("ei", "i")
        word = word.replace('e', 'a').replace('o', 'u')
        return repeats.replace(word, "$1")
    }
}
