package com.example.domain.model.i18n

import com.example.domain.model.Dua

/**
 * A dua's title, meaning, context and virtue in one app language. [meaning] is null where the
 * dataset already carries that language (Urdu uses Dua.urduTranslation); [benefit] defaults to
 * [context] for the entries whose dataset benefit repeats the context.
 */
class DuaText(val title: String, val meaning: String?, val context: String, val benefit: String = context)

/**
 * Per-language dua copy, keyed by dua id. English lives in DuaEnglish and Hinglish is the
 * dataset itself; every other app language has a pack here. Packs load on first use so the
 * app only holds the selected language in memory.
 */
object DuaTranslations {
    private val loaders: Map<String, () -> Map<String, DuaText>> = mapOf(
        "hi" to { DuaHi1.MAP + DuaHi2.MAP + DuaHi3.MAP },
        "ur" to { DuaUr1.MAP + DuaUr2.MAP + DuaUr3.MAP },
        "bn" to { DuaBn1.MAP + DuaBn2.MAP + DuaBn3.MAP },
        "gu" to { DuaGu1.MAP + DuaGu2.MAP + DuaGu3.MAP },
        "mr" to { DuaMr1.MAP + DuaMr2.MAP + DuaMr3.MAP },
        "ne" to { DuaNe1.MAP + DuaNe2.MAP + DuaNe3.MAP },
        "pa" to { DuaPa1.MAP + DuaPa2.MAP + DuaPa3.MAP },
        "ta" to { DuaTa1.MAP },
        "ar" to { DuaAr1.MAP + DuaAr2.MAP + DuaAr3.MAP },
        "fr" to { DuaFr1.MAP + DuaFr2.MAP + DuaFr3.MAP },
        "es" to { DuaEs1.MAP },
        "tr" to { DuaTr1.MAP },
        "id" to { DuaId1.MAP },
        "ru" to { DuaRu1.MAP },
        "de" to { DuaDe1.MAP + DuaDe2.MAP },
        "pt" to { DuaPt1.MAP + DuaPt2.MAP + DuaPt3.MAP + DuaPt4.MAP + DuaPt5.MAP },
        "zh" to { DuaZh1.MAP },
        "sw" to { DuaSw1.MAP },
        "it" to { DuaIt1.MAP },
        "nl" to { DuaNl1.MAP },
        "vi" to { DuaVi1.MAP },
        "th" to { DuaTh1.MAP },
        "tl" to { DuaTl1.MAP },
        "ja" to { DuaJa1.MAP },
        "ko" to { DuaKo1.MAP },
        "ha" to { DuaHa1.MAP },
        "ms" to { DuaMs1.MAP },
        "pl" to { DuaPl1.MAP },
        "ps" to { DuaPs1.MAP },
        "el" to { DuaEl1.MAP },
        "uk" to { DuaUk1.MAP },
        "so" to { DuaSo1.MAP },
        "am" to { DuaAm1.MAP },
        "uz" to { DuaUz1.MAP },
        "he" to { DuaHe1.MAP },
        "si" to { DuaSi1.MAP },
        "kn" to { DuaKn1.MAP },
        "ml" to { DuaMl1.MAP },
        "te" to { DuaTe1.MAP },
        "or" to { DuaOr1.MAP },
    )

    private val loaded = java.util.concurrent.ConcurrentHashMap<String, Map<String, DuaText>>()

    val languages: Set<String> get() = loaders.keys

    fun of(language: String): Map<String, DuaText> =
        loaders[language]?.let { load -> loaded.getOrPut(language) { load() } } ?: emptyMap()
}

fun Dua.textIn(language: String): DuaText? = DuaTranslations.of(language)[id]
