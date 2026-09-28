package com.example.domain.model

data class QuranWord(
    val id: Int,
    val arabic: String,
    val transliteration: String,
    val englishMeaning: String,
    val urduMeaning: String,
    val rootLetters: String? = null,
    val grammarRole: String? = null
)

data class MushafLine(
    val type: String,
    val isCentered: Boolean,
    val surahNumber: Int? = null,
    val text: String = ""
)

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val transliteration: String,
    val englishTranslation: String,
    val urduTranslation: String,
    val words: List<QuranWord> = emptyList(),
    val tafsirSummary: String,
    val tafsirSource: String = "Tafsir Ibn Kathir / Ma'ariful Quran (Verified)",
    val revelationType: String = "Meccan",
    val juzNumber: Int = 1
)

data class Surah(
    val number: Int,
    val arabicName: String,
    val englishName: String,
    val englishMeaning: String,
    val totalAyat: Int,
    val revelationType: String,
    val startJuz: Int
)

data class HifzItem(
    val surahNumber: Int,
    val ayahNumber: Int,
    val status: HifzStatus = HifzStatus.LEARNING,
    val confidenceScore: Int = 1, // 1 to 4
    val lastReviewedTimestamp: Long = System.currentTimeMillis()
)

enum class HifzStatus(val label: String) {
    NEW("New"),
    LEARNING("Learning"),
    NEEDS_REVIEW("Needs Review"),
    STRONG("Strong")
}

data class Bookmark(
    val id: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val ayahSnippet: String,
    val timestamp: Long = System.currentTimeMillis()
)
