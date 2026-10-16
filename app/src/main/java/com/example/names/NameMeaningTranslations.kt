package com.example.names

/** Machine translated name meanings; linguistic review pending. */
internal object NameMeaningTranslations {
    private val loaders: Map<String, () -> Map<String, String>> = mapOf(
        "as" to { IndicAsNameDraft1.MAP + IndicAsNameDraft2.MAP + IndicAsNameDraft3.MAP + IndicAsNameDraft4.MAP },
        "bn" to { IndicBnNameDraft1.MAP + IndicBnNameDraft2.MAP + IndicBnNameDraft3.MAP + IndicBnNameDraft4.MAP },
        "brx" to { IndicBrxNameDraft1.MAP + IndicBrxNameDraft2.MAP + IndicBrxNameDraft3.MAP + IndicBrxNameDraft4.MAP },
        "doi" to { IndicDoiNameDraft1.MAP + IndicDoiNameDraft2.MAP + IndicDoiNameDraft3.MAP + IndicDoiNameDraft4.MAP },
        "gu" to { IndicGuNameDraft1.MAP + IndicGuNameDraft2.MAP + IndicGuNameDraft3.MAP + IndicGuNameDraft4.MAP },
        "kn" to { IndicKnNameDraft1.MAP + IndicKnNameDraft2.MAP + IndicKnNameDraft3.MAP + IndicKnNameDraft4.MAP },
        "ks" to { IndicKsNameDraft1.MAP + IndicKsNameDraft2.MAP + IndicKsNameDraft3.MAP + IndicKsNameDraft4.MAP },
        "kok" to { IndicKokNameDraft1.MAP + IndicKokNameDraft2.MAP + IndicKokNameDraft3.MAP + IndicKokNameDraft4.MAP },
        "mai" to { IndicMaiNameDraft1.MAP + IndicMaiNameDraft2.MAP + IndicMaiNameDraft3.MAP + IndicMaiNameDraft4.MAP },
        "ml" to { IndicMlNameDraft1.MAP + IndicMlNameDraft2.MAP + IndicMlNameDraft3.MAP + IndicMlNameDraft4.MAP },
        "mni" to { IndicMniNameDraft1.MAP + IndicMniNameDraft2.MAP + IndicMniNameDraft3.MAP + IndicMniNameDraft4.MAP },
        "mr" to { IndicMrNameDraft1.MAP + IndicMrNameDraft2.MAP + IndicMrNameDraft3.MAP + IndicMrNameDraft4.MAP },
        "ne" to { IndicNeNameDraft1.MAP + IndicNeNameDraft2.MAP + IndicNeNameDraft3.MAP + IndicNeNameDraft4.MAP },
        "or" to { IndicOrNameDraft1.MAP + IndicOrNameDraft2.MAP + IndicOrNameDraft3.MAP + IndicOrNameDraft4.MAP },
        "pa" to { IndicPaNameDraft1.MAP + IndicPaNameDraft2.MAP + IndicPaNameDraft3.MAP + IndicPaNameDraft4.MAP },
        "sa" to { IndicSaNameDraft1.MAP + IndicSaNameDraft2.MAP + IndicSaNameDraft3.MAP + IndicSaNameDraft4.MAP },
        "sat" to { IndicSatNameDraft1.MAP + IndicSatNameDraft2.MAP + IndicSatNameDraft3.MAP + IndicSatNameDraft4.MAP },
        "sd" to { IndicSdNameDraft1.MAP + IndicSdNameDraft2.MAP + IndicSdNameDraft3.MAP + IndicSdNameDraft4.MAP },
        "ta" to { IndicTaNameDraft1.MAP + IndicTaNameDraft2.MAP + IndicTaNameDraft3.MAP + IndicTaNameDraft4.MAP },
        "te" to { IndicTeNameDraft1.MAP + IndicTeNameDraft2.MAP + IndicTeNameDraft3.MAP + IndicTeNameDraft4.MAP },
    )
    private val loaded = java.util.concurrent.ConcurrentHashMap<String, Map<String, String>>()
    fun of(code: String, english: String): String =
        loaders[code]?.let { load -> loaded.getOrPut(code) { load() } }?.get(english)
            ?: com.example.ui.localization.WorldDraftPhrases.forLanguage(code)[english] ?: english
}
