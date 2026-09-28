package com.example.domain.model

/**
 * Real English copy for a dua. The bundled `englishTranslation`, `context` and `benefit` fields
 * are Roman Hindi, so English mode reads from here instead.
 */
internal class DuaEnglish(val meaning: String, val context: String, val benefit: String = context) {
    companion object {
        private val ALL: Map<String, DuaEnglish> by lazy { DuaEnglish1.MAP + DuaEnglish2.MAP + DuaEnglish3.MAP }

        fun forId(id: String): DuaEnglish? = ALL[id]
    }
}

val Dua.englishMeaning: String get() = DuaEnglish.forId(id)?.meaning ?: englishTranslation
val Dua.englishContext: String get() = DuaEnglish.forId(id)?.context ?: context
val Dua.englishBenefit: String get() = DuaEnglish.forId(id)?.benefit ?: benefit
