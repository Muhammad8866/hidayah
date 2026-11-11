package com.example.quran

import com.example.core.search.AppSearch
import com.example.domain.model.Ayah
import com.example.domain.model.Surah
import com.example.hadith.BookSearchText
import com.example.ui.localization.localizeUiText
import java.util.concurrent.ConcurrentHashMap

/**
 * The one Quran search behind every surah list (audio, tafsir audio, word learning, PDF mushaf,
 * translation picker, Hifz picker) and the ayah text search. Both run [AppSearch], so a surah is
 * found by its English or Arabic name in any spelling or script ("yaseen", "یس", "यासीन"), by its
 * meaning, by a well-known other name ("Tabarak"), by its number in any digits, and with "Surah"
 * typed in front; an ayah by a reference ("2:255") or by any word of its text.
 */
object QuranSearch {
    /** "Surah" in the spellings people type before a name; every surah carries it, so it never narrows a search. */
    private val surahWords = listOf("Surah", "سورة")

    /** Other names a surah is widely known by, searched like its own name. */
    private val otherNames: Map<Int, List<String>> = mapOf(
        1 to listOf("Al-Hamd", "Umm al-Kitab"),
        9 to listOf("Al-Bara'ah"),
        17 to listOf("Bani Isra'il", "Bani Israel"),
        35 to listOf("Al-Mala'ikah"),
        40 to listOf("Al-Mu'min"),
        41 to listOf("Ha Mim As-Sajdah"),
        47 to listOf("Al-Qital"),
        67 to listOf("Tabarak"),
        76 to listOf("Ad-Dahr"),
        78 to listOf("Amma"),
        94 to listOf("Al-Inshirah", "Alam Nashrah"),
        98 to listOf("Lam Yakun"),
        99 to listOf("Az-Zilzal"),
        106 to listOf("Li-ilaf"),
        107 to listOf("Ara'ayta"),
        111 to listOf("Al-Lahab", "Tabbat"),
        112 to listOf("At-Tawhid")
    )

    /** What one surah is searched by; [localize] gives a text in the reader's language. */
    fun surahFields(surah: Surah, localize: (String) -> String): AppSearch.Fields {
        val localized = listOf(surah.englishName, surah.englishMeaning)
            .map(localize).filterNot { it.isBlank() || it == surah.englishName || it == surah.englishMeaning }
        return AppSearch.Fields(
            title = surah.englishName,
            nativeTitle = surah.arabicName,
            other = listOf(surah.englishMeaning) + localized + otherNames[surah.number].orEmpty() + surahWords,
            number = surah.number
        )
    }

    /** [surahs] made searchable once; [localize] gives a text in the reader's language. */
    fun surahRanker(surahs: List<Surah>, localize: (String) -> String): AppSearch.Ranker<Surah> =
        AppSearch.Ranker(surahs) { surahFields(it, localize) }

    private val rankers = ConcurrentHashMap<String, AppSearch.Ranker<Surah>>()

    /**
     * The surahs that match [query] for an app in [language], best first; a blank query lists all
     * 114 in order. The search for a language is built once and kept.
     */
    fun surahs(query: String, language: String): List<Surah> =
        rankers.getOrPut(language) { surahRanker(FullQuranCatalog.surahs) { localizeUiText(it, language) } }.search(query)

    /** Only this much of a query is read, as in [AppSearch]. */
    private const val MAX_QUERY_CHARS = 200
    private val reference = Regex("""^\s*(\p{Nd}{1,3})\s*(?:[:./]|\s)\s*(\p{Nd}{1,3})\s*$""")

    /**
     * Surah and ayah of a typed reference ("2:255", "2 255", "2.255", "36/1", in any digits), or
     * null when the query is not one or names no ayah of the Quran.
     */
    fun ayahReference(query: String): Pair<Int, Int>? {
        val match = reference.matchEntire(query.take(MAX_QUERY_CHARS)) ?: return null
        val (surah, ayah) = match.destructured.toList().map { group ->
            group.fold(0) { value, digit -> value * 10 + Character.digit(digit, 10).coerceAtLeast(0) }
        }
        val ayat = FullQuranCatalog.surahs.getOrNull(surah - 1)?.totalAyat ?: return null
        return if (ayah in 1..ayat) surah to ayah else null
    }

    /**
     * Ayah text search over [ayahs], built once: the ayah a typed reference names comes first, then
     * the ayahs whose Arabic, transliteration, English or Urdu text holds every typed word. Unlike a
     * list filter, a query with nothing searchable finds no ayah.
     */
    class AyahSearch(private val ayahs: List<Ayah>) {
        private val ranker = AppSearch.Ranker(ayahs) { ayah ->
            AppSearch.Fields(
                title = "",
                other = listOf(ayah.arabicText, ayah.transliteration, ayah.englishTranslation, ayah.urduTranslation)
            )
        }

        fun search(query: String): List<Ayah> {
            val exact = ayahReference(query)?.let { (surah, ayah) ->
                ayahs.filter { it.surahNumber == surah && it.ayahNumber == ayah }
            }.orEmpty()
            if (BookSearchText.normalize(query.take(MAX_QUERY_CHARS)).isEmpty()) return exact
            return (exact + ranker.search(query)).distinct()
        }
    }
}
