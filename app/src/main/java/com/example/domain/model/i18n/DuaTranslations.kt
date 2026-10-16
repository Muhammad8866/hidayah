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
        "fa" to { DuaFaDraft1.MAP + DuaFaDraft2.MAP + DuaFaDraft3.MAP + DuaFaDraft4.MAP + DuaFaDraft5.MAP + DuaFaDraft6.MAP + DuaFaDraft7.MAP + DuaFaDraft8.MAP },
        "sd" to { DuaSdDraft1.MAP + DuaSdDraft2.MAP + DuaSdDraft3.MAP + DuaSdDraft4.MAP + DuaSdDraft5.MAP + DuaSdDraft6.MAP + DuaSdDraft7.MAP + DuaSdDraft8.MAP },
        "sat" to { DuaSatDraft1.MAP + DuaSatDraft2.MAP + DuaSatDraft3.MAP + DuaSatDraft4.MAP + DuaSatDraft5.MAP + DuaSatDraft6.MAP + DuaSatDraft7.MAP + DuaSatDraft8.MAP },
        "sa" to { DuaSaDraft1.MAP + DuaSaDraft2.MAP + DuaSaDraft3.MAP + DuaSaDraft4.MAP + DuaSaDraft5.MAP + DuaSaDraft6.MAP + DuaSaDraft7.MAP + DuaSaDraft8.MAP },
        "mni" to { DuaMniDraft1.MAP + DuaMniDraft2.MAP + DuaMniDraft3.MAP + DuaMniDraft4.MAP + DuaMniDraft5.MAP + DuaMniDraft6.MAP + DuaMniDraft7.MAP + DuaMniDraft8.MAP },
        "mai" to { DuaMaiDraft1.MAP + DuaMaiDraft2.MAP + DuaMaiDraft3.MAP + DuaMaiDraft4.MAP + DuaMaiDraft5.MAP + DuaMaiDraft6.MAP + DuaMaiDraft7.MAP + DuaMaiDraft8.MAP },
        "kok" to { DuaKokDraft1.MAP + DuaKokDraft2.MAP + DuaKokDraft3.MAP + DuaKokDraft4.MAP + DuaKokDraft5.MAP + DuaKokDraft6.MAP + DuaKokDraft7.MAP + DuaKokDraft8.MAP },
        "ks" to { DuaKsDraft1.MAP + DuaKsDraft2.MAP + DuaKsDraft3.MAP + DuaKsDraft4.MAP + DuaKsDraft5.MAP + DuaKsDraft6.MAP + DuaKsDraft7.MAP + DuaKsDraft8.MAP },
        "doi" to { DuaDoiDraft1.MAP + DuaDoiDraft2.MAP + DuaDoiDraft3.MAP + DuaDoiDraft4.MAP + DuaDoiDraft5.MAP + DuaDoiDraft6.MAP + DuaDoiDraft7.MAP + DuaDoiDraft8.MAP },
        "brx" to { DuaBrxDraft1.MAP + DuaBrxDraft2.MAP + DuaBrxDraft3.MAP + DuaBrxDraft4.MAP + DuaBrxDraft5.MAP + DuaBrxDraft6.MAP + DuaBrxDraft7.MAP + DuaBrxDraft8.MAP },
        "as" to { DuaAsDraft1.MAP + DuaAsDraft2.MAP + DuaAsDraft3.MAP + DuaAsDraft4.MAP + DuaAsDraft5.MAP + DuaAsDraft6.MAP + DuaAsDraft7.MAP + DuaAsDraft8.MAP },
        "hi" to { DuaHi1.MAP + DuaHi2.MAP + DuaHi3.MAP },
        "ur" to { DuaUr1.MAP + DuaUr2.MAP + DuaUr3.MAP },
        "bn" to { DuaBn1.MAP + DuaBn2.MAP + DuaBn3.MAP },
        "gu" to { DuaGu1.MAP + DuaGu2.MAP + DuaGu3.MAP },
        "mr" to { DuaMr1.MAP + DuaMr2.MAP + DuaMr3.MAP },
        "ne" to { DuaNe1.MAP + DuaNe2.MAP + DuaNe3.MAP },
        "pa" to { DuaPa1.MAP + DuaPa2.MAP + DuaPa3.MAP },
        "ta" to { DuaTa1.MAP + DuaTa2.MAP + DuaTa3.MAP },
        "ar" to { DuaAr1.MAP + DuaAr2.MAP + DuaAr3.MAP },
        "fr" to { DuaFr1.MAP + DuaFr2.MAP + DuaFr3.MAP },
        "es" to { DuaEs1.MAP + DuaEsDraft1.MAP + DuaEsDraft2.MAP + DuaEsDraft3.MAP + DuaEsDraft4.MAP + DuaEsDraft5.MAP },
        "tr" to { DuaTr1.MAP + DuaTrDraft1.MAP + DuaTrDraft2.MAP + DuaTrDraft3.MAP + DuaTrDraft4.MAP + DuaTrDraft5.MAP },
        "id" to { DuaId1.MAP + DuaIdDraft1.MAP + DuaIdDraft2.MAP + DuaIdDraft3.MAP + DuaIdDraft4.MAP + DuaIdDraft5.MAP },
        "ru" to { DuaRu1.MAP + DuaRuDraft1.MAP + DuaRuDraft2.MAP + DuaRuDraft3.MAP + DuaRuDraft4.MAP + DuaRuDraft5.MAP },
        "de" to { DuaDe1.MAP + DuaDe2.MAP + DuaDe3.MAP },
        "pt" to { DuaPt1.MAP + DuaPt2.MAP + DuaPt3.MAP + DuaPt4.MAP + DuaPt5.MAP },
        "zh" to { DuaZh1.MAP + DuaZhDraft1.MAP + DuaZhDraft2.MAP + DuaZhDraft3.MAP + DuaZhDraft4.MAP + DuaZhDraft5.MAP },
        "sw" to { DuaSw1.MAP + DuaSwDraft1.MAP + DuaSwDraft2.MAP + DuaSwDraft3.MAP + DuaSwDraft4.MAP + DuaSwDraft5.MAP + DuaSwDraft6.MAP + DuaSwDraft7.MAP + DuaSwDraft8.MAP },
        "it" to { DuaIt1.MAP + DuaItDraft1.MAP + DuaItDraft2.MAP + DuaItDraft3.MAP + DuaItDraft4.MAP + DuaItDraft5.MAP },
        "nl" to { DuaNl1.MAP + DuaNlDraft1.MAP + DuaNlDraft2.MAP + DuaNlDraft3.MAP + DuaNlDraft4.MAP + DuaNlDraft5.MAP + DuaNlDraft6.MAP + DuaNlDraft7.MAP + DuaNlDraft8.MAP },
        "vi" to { DuaVi1.MAP + DuaViDraft1.MAP + DuaViDraft2.MAP + DuaViDraft3.MAP + DuaViDraft4.MAP + DuaViDraft5.MAP + DuaViDraft6.MAP + DuaViDraft7.MAP + DuaViDraft8.MAP },
        "th" to { DuaTh1.MAP + DuaThDraft1.MAP + DuaThDraft2.MAP + DuaThDraft3.MAP + DuaThDraft4.MAP + DuaThDraft5.MAP + DuaThDraft6.MAP + DuaThDraft7.MAP + DuaThDraft8.MAP },
        "tl" to { DuaTl1.MAP + DuaTlDraft1.MAP + DuaTlDraft2.MAP + DuaTlDraft3.MAP + DuaTlDraft4.MAP + DuaTlDraft5.MAP + DuaTlDraft6.MAP + DuaTlDraft7.MAP + DuaTlDraft8.MAP },
        "ja" to { DuaJa1.MAP + DuaJaDraft1.MAP + DuaJaDraft2.MAP + DuaJaDraft3.MAP + DuaJaDraft4.MAP + DuaJaDraft5.MAP },
        "ko" to { DuaKo1.MAP + DuaKoDraft1.MAP + DuaKoDraft2.MAP + DuaKoDraft3.MAP + DuaKoDraft4.MAP + DuaKoDraft5.MAP + DuaKoDraft6.MAP + DuaKoDraft7.MAP + DuaKoDraft8.MAP },
        "ha" to { DuaHa1.MAP + DuaHaDraft1.MAP + DuaHaDraft2.MAP + DuaHaDraft3.MAP + DuaHaDraft4.MAP + DuaHaDraft5.MAP + DuaHaDraft6.MAP + DuaHaDraft7.MAP + DuaHaDraft8.MAP },
        "ms" to { DuaMs1.MAP + DuaMsDraft1.MAP + DuaMsDraft2.MAP + DuaMsDraft3.MAP + DuaMsDraft4.MAP + DuaMsDraft5.MAP + DuaMsDraft6.MAP + DuaMsDraft7.MAP + DuaMsDraft8.MAP },
        "pl" to { DuaPl1.MAP + DuaPlDraft1.MAP + DuaPlDraft2.MAP + DuaPlDraft3.MAP + DuaPlDraft4.MAP + DuaPlDraft5.MAP + DuaPlDraft6.MAP + DuaPlDraft7.MAP + DuaPlDraft8.MAP },
        "ps" to { DuaPs1.MAP + DuaPsDraft1.MAP + DuaPsDraft2.MAP + DuaPsDraft3.MAP + DuaPsDraft4.MAP + DuaPsDraft5.MAP + DuaPsDraft6.MAP + DuaPsDraft7.MAP + DuaPsDraft8.MAP },
        "el" to { DuaEl1.MAP + DuaElDraft1.MAP + DuaElDraft2.MAP + DuaElDraft3.MAP + DuaElDraft4.MAP + DuaElDraft5.MAP + DuaElDraft6.MAP + DuaElDraft7.MAP + DuaElDraft8.MAP },
        "uk" to { DuaUk1.MAP + DuaUkDraft1.MAP + DuaUkDraft2.MAP + DuaUkDraft3.MAP + DuaUkDraft4.MAP + DuaUkDraft5.MAP + DuaUkDraft6.MAP + DuaUkDraft7.MAP + DuaUkDraft8.MAP },
        "so" to { DuaSo1.MAP + DuaSoDraft1.MAP + DuaSoDraft2.MAP + DuaSoDraft3.MAP + DuaSoDraft4.MAP + DuaSoDraft5.MAP + DuaSoDraft6.MAP + DuaSoDraft7.MAP + DuaSoDraft8.MAP },
        "am" to { DuaAm1.MAP + DuaAmDraft1.MAP + DuaAmDraft2.MAP + DuaAmDraft3.MAP + DuaAmDraft4.MAP + DuaAmDraft5.MAP + DuaAmDraft6.MAP + DuaAmDraft7.MAP + DuaAmDraft8.MAP },
        "uz" to { DuaUz1.MAP + DuaUzDraft1.MAP + DuaUzDraft2.MAP + DuaUzDraft3.MAP + DuaUzDraft4.MAP + DuaUzDraft5.MAP + DuaUzDraft6.MAP + DuaUzDraft7.MAP + DuaUzDraft8.MAP },
        "he" to { DuaHe1.MAP + DuaHeDraft1.MAP + DuaHeDraft2.MAP + DuaHeDraft3.MAP + DuaHeDraft4.MAP + DuaHeDraft5.MAP + DuaHeDraft6.MAP + DuaHeDraft7.MAP + DuaHeDraft8.MAP },
        "si" to { DuaSi1.MAP + DuaSiDraft1.MAP + DuaSiDraft2.MAP + DuaSiDraft3.MAP + DuaSiDraft4.MAP + DuaSiDraft5.MAP + DuaSiDraft6.MAP + DuaSiDraft7.MAP + DuaSiDraft8.MAP },
        "kn" to { DuaKn1.MAP + DuaKnDraft1.MAP + DuaKnDraft2.MAP + DuaKnDraft3.MAP + DuaKnDraft4.MAP + DuaKnDraft5.MAP + DuaKnDraft6.MAP + DuaKnDraft7.MAP + DuaKnDraft8.MAP },
        "ml" to { DuaMl1.MAP + DuaMlDraft1.MAP + DuaMlDraft2.MAP + DuaMlDraft3.MAP + DuaMlDraft4.MAP + DuaMlDraft5.MAP + DuaMlDraft6.MAP + DuaMlDraft7.MAP + DuaMlDraft8.MAP },
        "te" to { DuaTe1.MAP + DuaTeDraft1.MAP + DuaTeDraft2.MAP + DuaTeDraft3.MAP + DuaTeDraft4.MAP + DuaTeDraft5.MAP + DuaTeDraft6.MAP + DuaTeDraft7.MAP + DuaTeDraft8.MAP },
        "or" to { DuaOr1.MAP + DuaOrDraft1.MAP + DuaOrDraft2.MAP + DuaOrDraft3.MAP + DuaOrDraft4.MAP + DuaOrDraft5.MAP + DuaOrDraft6.MAP + DuaOrDraft7.MAP + DuaOrDraft8.MAP },
    )

    private val loaded = java.util.concurrent.ConcurrentHashMap<String, Map<String, DuaText>>()

    val languages: Set<String> get() = loaders.keys

    fun of(language: String): Map<String, DuaText> =
        loaders[language]?.let { load -> loaded.getOrPut(language) { load() } } ?: emptyMap()
}

fun Dua.textIn(language: String): DuaText? = DuaTranslations.of(language)[id]
