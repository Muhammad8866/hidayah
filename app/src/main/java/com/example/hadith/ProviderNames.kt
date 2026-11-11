package com.example.hadith

/**
 * Names of the libraries, publishers and sites the books are fetched from. They stay in the
 * fetching code but are never shown: an author line loses the part that names one, and a line
 * that is only a provider becomes blank. Author names are kept exactly as written.
 */
internal object ProviderNames {
    /** Compared against text with case, spaces and punctuation removed. */
    private val markers = listOf(
        "dawateislami", "maktabatulmadin", "maktabtulmadin", "madinatulilmi",
        "majliseshoora", "kanzulmadarisboard", "majlismadrasaalmadinah",
        "internetarchive", "archiveorg", "razaacademy", "alahazratnet", "blogspot"
    )

    /** Bracketed asides, like "(Dawat-e-Islami)", are judged on their own. */
    private val aside = Regex("""\s*[(\[][^()\[\]]*[)\]]""")

    /** Where one name ends and the next begins; "Paishkash" means "presented by". */
    private val separators = Regex("""\s*[;|·•,/]\s*|\s+-\s+|(?i)\s+(?:paishkash|peshkash)\s+""")

    fun isProvider(text: String): Boolean {
        val folded = text.lowercase().filter(Char::isLetterOrDigit)
        return markers.any { it in folded }
    }

    /** [line] without any part that names a provider; unchanged when it names none. */
    fun hideIn(line: String): String {
        if (!isProvider(line)) return line
        val withoutAsides = aside.replace(line) { if (isProvider(it.value)) "" else it.value }
        return withoutAsides.split(separators)
            .map(String::trim)
            .filter { it.isNotEmpty() && !isProvider(it) }
            .joinToString(" · ")
    }
}
