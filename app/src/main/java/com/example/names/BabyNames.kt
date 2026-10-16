package com.example.names

import com.example.domain.model.DuaDatasetRepository

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
        else -> english
    }
}

/** Names from the recommended lists in Maktaba-tul-Madinah's Naam Rakhne ke Ahkam.
 * Every entry links to its source page; translations follow that source's meanings
 * and associations, rather than claiming every name is Quranic or an etymology.
 */
object BabyNames {
    val names: List<BabyName> = BabyNameCatalog.names.sortedBy { it.name }

    private fun normalize(text: String): String = DuaDatasetRepository.normalizeForSearch(
        text.replace('ک', 'ك').replace('ی', 'ي').replace('ہ', 'ه').replace('ھ', 'ه')
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ة', 'ه')
    )

    // Build this once: typing only filters the cached search text.
    private val searchIndex = names.map { name ->
        name to normalize("${name.name} ${name.arabic} ${name.sourceSpelling} ${name.english} " +
            "${name.hinglish} ${name.aliases} ${name.sourceMeaningUrdu} ${name.arabic.replace(" ", "")}")
    }

    fun search(query: String, gender: BabyGender? = null): List<BabyName> {
        val words = normalize(query).split(' ').filter(String::isNotBlank)
        return searchIndex.filter { (name, text) ->
            (gender == null || name.gender == gender) && words.all(text::contains)
        }.map { it.first }
    }
}
