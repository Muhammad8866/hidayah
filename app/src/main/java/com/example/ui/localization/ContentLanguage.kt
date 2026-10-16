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

private fun hasWholeContentTranslation(text: String, language: String): Boolean =
    text.none { it in 'A'..'Z' || it in 'a'..'z' } ||
        localizePhrase(text, language) != text ||
        WorldDraftPhrases.forLanguage(language).containsKey(text)

fun bilingualContent(roman: String, english: String, languageCode: String): DisplayMeaning {
    if (languageCode == "en") return DisplayMeaning(english)
    if (languageCode == "hinglish") return DisplayMeaning(roman)
    // Pure Arabic devotional text reads the same in every language, so it is never a fallback.
    if (english.none { it in 'A'..'Z' || it in 'a'..'z' }) return DisplayMeaning(english)
    localizePhrase(roman, languageCode).takeIf { it != roman }?.let { return DisplayMeaning(it) }
    localizePhrase(english, languageCode).takeIf { it != english }?.let { return DisplayMeaning(it) }
    if (WorldDraftPhrases.forLanguage(languageCode).containsKey(english)) return DisplayMeaning(english)
    return if (languageCode in ROMAN_FALLBACK_LANGUAGES) DisplayMeaning(roman, "Hinglish (Roman Hindi)")
        else DisplayMeaning(english, "English")
}

/** Keep a long-form answer or lesson in one language when any sentence lacks a translation. */
fun completeContentLanguage(languageCode: String, phrases: Collection<String>): String =
    if (languageCode != "en" && phrases.filter(String::isNotBlank).all {
            hasWholeContentTranslation(it, languageCode)
        }) languageCode else "en"
