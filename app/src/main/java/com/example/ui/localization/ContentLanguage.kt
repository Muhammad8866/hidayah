package com.example.ui.localization

/**
 * Bundled religious content (dua meanings, festival guidance) is written in simple Roman Hindi,
 * which is also the right copy for Hinglish, Hindi and Urdu readers, with a separate genuine
 * English copy. Other languages get a reviewed whole-phrase translation where one exists (see
 * [localizePhrase]) and otherwise fall back to English — word-by-word replacement could change
 * the religious meaning. [fallbackLanguage] names the shown language when it is a fallback.
 */
data class DisplayMeaning(val text: String, val fallbackLanguage: String? = null)

private val ROMAN_FALLBACK_LANGUAGES = setOf("hinglish", "hi", "ur")

fun bilingualContent(roman: String, english: String, languageCode: String): DisplayMeaning {
    if (languageCode == "en") return DisplayMeaning(english)
    if (languageCode == "hinglish") return DisplayMeaning(roman)
    localizePhrase(roman, languageCode).takeIf { it != roman }?.let { return DisplayMeaning(it) }
    localizePhrase(english, languageCode).takeIf { it != english }?.let { return DisplayMeaning(it) }
    return if (languageCode in ROMAN_FALLBACK_LANGUAGES) DisplayMeaning(roman, "Hinglish (Roman Hindi)")
        else DisplayMeaning(english, "English")
}
