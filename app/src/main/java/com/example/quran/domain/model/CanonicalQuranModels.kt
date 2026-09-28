package com.example.quran.domain.model

import com.example.core.id.AyahReference

/**
 * Immutable canonical representation of an Ayah from the Holy Quran.
 * In accordance with the Architecture Master Prompt:
 * - Quran text is immutable and versioned.
 * - Identified by strongly-typed AyahReference.
 * - Arabic text, transliteration, translations, and morphological words.
 */
data class CanonicalAyah(
    val reference: AyahReference,
    val arabicText: String,
    val transliteration: String,
    val englishTranslation: String,
    val urduTranslation: String,
    val words: List<CanonicalWord>,
    val tafsirSummary: String,
    val tafsirSource: String,
    val juzNumber: Int,
    val sajda: Boolean = false
) {
    val surahNumber: Int get() = reference.surah.value
    val ayahNumber: Int get() = reference.ayah.value
}

data class CanonicalWord(
    val position: Int,
    val arabicText: String,
    val transliteration: String,
    val englishMeaning: String,
    val urduMeaning: String,
    val rootLetters: String,
    val grammarNote: String
)

data class CanonicalSurahMeta(
    val number: Int,
    val arabicName: String,
    val englishTransliteration: String,
    val englishMeaning: String,
    val totalVerses: Int,
    val revelationType: RevelationType,
    val startJuz: Int
)

enum class RevelationType(val title: String) {
    MECCAN("Meccan"),
    MEDINAN("Medinan")
}
