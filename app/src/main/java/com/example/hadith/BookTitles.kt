package com.example.hadith

import java.util.Locale

/**
 * Tidies titles that arrive as upload names ("ANWARE SHARIAT KANADA", "bahar-e-shariat_202001",
 * "Fatawa_Razaviyya_Vol_2.pdf", "Jaa Al Haq . جاء الحق"). Spacing, separators, punctuation and
 * letter case change, and upload leftovers ("[PDF]", a website's address) are dropped; no word of
 * the title itself is added, translated or dropped, and non-Latin scripts pass through untouched.
 */
object BookTitles {
    /** The date the source appends to make an upload name unique, e.g. "_202001". */
    private val uploadStamp = Regex("""[\s_\-]+(19|20)\d{4}(\d{2})?$""")
    private val camelBreak = Regex("""(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[A-Za-z])(?=\d)|(?<=\d)(?=[A-Za-z])""")
    private val spaces = Regex("""\s+""")
    private const val EDGE = " \t-_.,:;|/،۔"

    /** "By I-Sunni.blogspot.com", "Pdfbooksfree.pk": where the file came from, not what it is called. */
    private val website = Regex(
        """(?i)(?:\bby\s+)?(?:\bI\s)?\b[a-z0-9-]+(?:\.[a-z0-9-]+)*\.(?:com|net|org|info|pk|in|co\.[a-z]{2})\b"""
    )
    private val pdfWord = Regex("""(?i)(?<![a-z])pdf(?![a-z])""")
    private val emptyBrackets = Regex("""[(\[]\s*[)\]]""")
    /** An upload name left inside a longer title: "rafiq-ul-mutamirin رفیق المعتمرین". */
    private val slugWord = Regex("""(?<![A-Za-z0-9-])[a-z]+(?:-[a-z0-9]+){2,}(?![A-Za-z0-9-])""")
    private val punctuationRun = Regex("""([,،.۔])(?:\s*[,،.۔])+""")
    private val gapSeparators = Regex("""[,،.۔/|\\\-–—:;_·•~]""")
    private val spaceBeforeComma = Regex("""\s+([,،?!؟])""")
    private val separatorAfterOpen = Regex("""([(\[])\s*[,،.۔]+\s*""")
    /** A Latin letter typed by mistake onto the end of an Arabic-script word. */
    private val strayLetter = Regex("""(?<=[\u0600-\u06FF])[A-Za-z](?![A-Za-z])""")
    /** Not the comma inside a number such as "1,000". */
    private val commaWithoutSpace = Regex("""([,،])(?=\S)(?!(?<=\d[,،])\d)""")
    private val spaceInsideOpen = Regex("""([(\[])\s+""")
    private val spaceInsideClose = Regex("""\s+([)\]])""")
    private val openWithoutSpace = Regex("""(?<=[\p{L}\p{M}\p{N}])(?=[(\[])""")
    private val closeWithoutSpace = Regex("""(?<=[)\]])(?=[\p{L}\p{N}])""")
    private val latinWord = Regex("""[A-Za-z]+(?:['’][A-Za-z]+)*""")
    private val romanNumeral = Regex("""[IVXL]+""")

    /** Joining words of Urdu, Arabic and English titles that stay lower case mid-title. */
    private val joiners = setOf(
        "e", "o", "ul", "un", "us", "ud", "ur", "ki", "ka", "ke", "kay", "aur", "wa", "wal", "fi", "bin", "ibn",
        "of", "the", "and", "in", "on", "by", "for", "with", "par", "se"
    )

    fun clean(raw: String): String {
        var text = raw.trim().replace("&amp;", "&")
        if (text.endsWith(".pdf", ignoreCase = true)) text = text.dropLast(4)
        text = text.replace(uploadStamp, "").replace('_', ' ').replace('+', ' ')
        text = text.replace(website, " ").replace(pdfWord, " ").replace(emptyBrackets, " ").trim()
        if (text.none(Char::isWhitespace)) {
            // A single unbroken token is an upload name, not a written title.
            text = if ('-' in text) text.replace('-', ' ') else text.replace(camelBreak, " ")
        }
        text = text.replace(slugWord) { it.value.replace('-', ' ') }
        text = text.replace(punctuationRun, "$1").replace(separatorAfterOpen, "$1").replace(strayLetter, "")
        text = separateScripts(text)
        text = text.replace(spaceInsideOpen, "$1").replace(spaceInsideClose, "$1")
            .replace(openWithoutSpace, " ").replace(closeWithoutSpace, " ")
            .replace(spaceBeforeComma, "$1").replace(commaWithoutSpace, "$1 ")
        text = text.replace(spaces, " ").trim { it in EDGE }
        return fixCase(text).ifBlank { raw.trim() }
    }

    private const val LATIN = 1
    private const val OTHER = 2

    private fun isLatin(char: Char) = char.isLetter() && (char <= 'ɏ' || char in 'Ḁ'..'ỿ')

    /**
     * A title given in two scripts ("Jaa Al Haq . جاء الحق", "Sarkar Ki Amad,سرکار کی آمد") gets one
     * plain " - " between the two, whatever the uploader typed there. Brackets are left as written.
     */
    private fun separateScripts(text: String): String {
        val out = StringBuilder()
        var last = 0
        var copied = 0
        var beforeBracket = 0
        text.forEachIndexed { index, char ->
            val script = when {
                isLatin(char) -> LATIN
                // Honorific signs such as ﷺ follow a name in either script.
                char in '\uFDF0'..'\uFDFF' -> 0
                char.isLetter() -> OTHER
                // A vowel sign belongs to the letter it sits on.
                Character.getType(char).toByte() in marks -> if (copied == index && last != 0) last else OTHER
                else -> 0
            }
            if (script == 0) return@forEachIndexed
            val gap = text.substring(copied, index)
            // "Title (عنوان) Vol 1": the bracket was an aside and the title carries on in its own script.
            val resumes = gap.any { it == ')' || it == ']' } && script == beforeBracket
            if (gap.any { it == '(' || it == '[' }) beforeBracket = last
            out.append(if (last != 0 && script != last && !resumes) joinGap(gap) else gap).append(char)
            copied = index + 1
            last = script
        }
        return out.append(text, copied, text.length).toString()
    }

    private val marks = setOf(Character.NON_SPACING_MARK, Character.COMBINING_SPACING_MARK, Character.ENCLOSING_MARK)

    private fun joinGap(gap: String): String {
        val close = gap.lastIndexOfAny(charArrayOf(')', ']'))
        val head = gap.substring(0, close + 1)
        val rest = gap.substring(close + 1)
        val open = rest.indexOfAny(charArrayOf('(', '['))
        val middle = (if (open >= 0) rest.substring(0, open) else rest)
            .replace(gapSeparators, " ").replace(spaces, " ").trimEnd()
        return if (open >= 0) "$head$middle ${rest.substring(open)}" else "$head$middle - "
    }

    /** Shouted and unshouted titles become title case; a title already in mixed case keeps its capitals. */
    private fun fixCase(text: String): String {
        val words = latinWord.findAll(text).map { it.value }.toList()
        if (words.isEmpty()) return text
        val letters = words.sumOf { word -> word.count(Char::isLetter) }
        val shouted = words.sumOf { word -> word.count(Char::isUpperCase) } * 2 > letters
        val unshouted = words.all(::isLower) || (words.size > 1 && words.drop(1).all(::isLower))
        return latinWord.replace(text) { match ->
            val word = match.value
            val lower = word.lowercase(Locale.ROOT)
            val start = match.range.first
            val end = match.range.last + 1
            val before = text.getOrNull(start - 1)
            val midTitle = (before == ' ' || before == '-') &&
                text.getOrNull(start - 2).let { it != null && (it.isLetterOrDigit() || it in ")'’") } &&
                (text.getOrNull(end) == ' ' || text.getOrNull(end) == '-') && text.getOrNull(end + 1)?.let(::isLatin) == true
            val capital = lower.replaceFirstChar { it.titlecase(Locale.ROOT) }
            when {
                midTitle && lower in joiners -> lower
                word.length > 1 && word.none(Char::isLowerCase) && !romanNumeral.matches(word) &&
                    (shouted || word.length > 3) -> capital
                // Not after a digit or a dot: "4th" and "r.a." are not the start of a word.
                unshouted && isLower(word) && before?.let { it.isDigit() || it == '.' } != true -> capital
                else -> word
            }
        }
    }

    private fun isLower(word: String) = word.none(Char::isUpperCase)

    /** What tells one file of [itemTitle] from another: its cleaned name without the repeated title. */
    fun fileLabel(itemTitle: String, fileName: String): String {
        val label = clean(fileName.substringAfterLast('/'))
        val title = clean(itemTitle)
        if (label.length > title.length && label.startsWith(title, ignoreCase = true)) {
            val rest = label.substring(title.length).trim { it in EDGE }
            if (rest.isNotBlank()) return rest.replaceFirstChar { it.titlecase(Locale.ROOT) }
        }
        return label
    }

    /** Orders "Vol 2" before "Vol 10", ignoring letter case and leading zeros. */
    val naturalOrder: Comparator<String> = Comparator { a, b ->
        var i = 0
        var j = 0
        while (i < a.length && j < b.length) {
            if (a[i].isDigit() && b[j].isDigit()) {
                val x = digitsEnd(a, i)
                val y = digitsEnd(b, j)
                val left = number(a, i, x)
                val right = number(b, j, y)
                val byNumber = if (left.length != right.length) left.length - right.length else left.compareTo(right)
                if (byNumber != 0) return@Comparator byNumber
                i = x
                j = y
            } else {
                val byChar = a[i].lowercaseChar().compareTo(b[j].lowercaseChar())
                if (byChar != 0) return@Comparator byChar
                i++
                j++
            }
        }
        (a.length - i) - (b.length - j)
    }

    private fun digitsEnd(text: String, from: Int): Int {
        var end = from
        while (end < text.length && text[end].isDigit()) end++
        return end
    }

    /** ASCII digits without leading zeros, so numerals of any script compare by value. */
    private fun number(text: String, from: Int, to: Int): String =
        (from until to).joinToString("") { Character.digit(text[it], 10).toString() }.trimStart('0')
}
