package com.example.names

import com.example.core.search.AppSearch
import java.util.concurrent.ConcurrentHashMap

enum class BabyGender { BOY, GIRL }

data class BabyName(
    val name: String,
    val arabic: String,
    val gender: BabyGender,
    val english: String,
    val hinglish: String,
    val aliases: String,
    val sourceUrl: String,
    val sourceTitle: String,
    val sourceSpelling: String,
    val sourceMeaningUrdu: String
) {
    val id: String get() = "${gender.name}:$name"

    fun meaning(language: String) = when (language) {
        "hinglish" -> hinglish
        "ur" -> sourceMeaningUrdu
        else -> com.example.ui.localization.localizePhrase(english, language)
    }
}

/** Names from the recommended lists in Maktaba-tul-Madinah's Naam Rakhne ke Ahkam.
 * Every entry links to its source page; translations follow that source's meanings
 * and associations, rather than claiming every name is Quranic or an etymology.
 */
object BabyNames {
    val names: List<BabyName> = BabyNameCatalog.names.sortedBy { it.name }

    /** Languages whose meaning is already one of the fields every search reads. */
    private val builtInMeanings = setOf("en", "ur", "hinglish")

    /** Built once per reader language, so typing only searches (see [AppSearch]). */
    private val searches = ConcurrentHashMap<String, AppSearch.Ranker<BabyName>>()

    /**
     * What search reads of a name. The name counts most, then its other spellings ("Ayesha" for
     * Aisha), then the Arabic (also written without spaces, as "عبدالرحمن"), then the source's
     * spelling and every meaning, including the reader's language.
     */
    private fun fields(name: BabyName, language: String): AppSearch.Fields {
        val joinedArabic = name.arabic.replace(" ", "")
        return AppSearch.Fields(
            title = name.name,
            nativeTitle = name.aliases,
            author = if (joinedArabic == name.arabic) name.arabic else "${name.arabic} $joinedArabic",
            other = listOfNotNull(
                name.sourceSpelling, name.english, name.hinglish, name.sourceMeaningUrdu,
                name.meaning(language).takeIf { language !in builtInMeanings }
            )
        )
    }

    /**
     * Names that match [query], best match first, of [gender] when one is given. A query with no
     * searchable word lists every name of that gender in alphabetical order.
     */
    fun search(query: String, gender: BabyGender? = null, language: String = "en"): List<BabyName> {
        val key = if (language in builtInMeanings) "en" else language
        val search = searches.getOrPut(key) { AppSearch.Ranker(names) { fields(it, key) } }
        return search.search(query).filter { gender == null || it.gender == gender }
    }
}
