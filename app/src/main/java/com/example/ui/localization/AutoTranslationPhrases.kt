package com.example.ui.localization

import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Machine-draft phrase packs written by tools/auto_translate.py as Java resources
 * (auto_translations/<code>.tsv), loaded lazily per language. Kept out of Kotlin
 * source so the packs no longer slow down compilation.
 */
internal object AutoTranslationPhrases {
    private val loaded = ConcurrentHashMap<String, Map<String, String>>()
    private val languageCode = Regex("[a-z]{2,3}|hinglish")

    fun lookup(text: String, language: String): String? {
        if (!languageCode.matches(language)) return null
        return loaded.getOrPut(language) {
            runCatching {
                javaClass.classLoader?.getResourceAsStream("auto_translations/$language.tsv")?.use(::parse)
            }.getOrNull() ?: emptyMap()
        }[text]
    }

    internal fun parse(input: InputStream): Map<String, String> {
        val pack = HashMap<String, String>()
        input.bufferedReader(Charsets.UTF_8).forEachLine { line ->
            val tab = line.indexOf('\t')
            if (tab <= 0) return@forEachLine
            val key = unescape(line.substring(0, tab)) ?: return@forEachLine
            val value = unescape(line.substring(tab + 1)) ?: return@forEachLine
            pack[key] = value
        }
        return pack
    }

    private fun unescape(raw: String): String? {
        if ('\\' !in raw) return raw
        val out = StringBuilder(raw.length)
        var i = 0
        while (i < raw.length) {
            val c = raw[i++]
            if (c != '\\') { out.append(c); continue }
            if (i == raw.length) return null
            out.append(when (raw[i++]) { 't' -> '\t'; 'n' -> '\n'; 'r' -> '\r'; '\\' -> '\\'; else -> return null })
        }
        return out.toString()
    }
}
