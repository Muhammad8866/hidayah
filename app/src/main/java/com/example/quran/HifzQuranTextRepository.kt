package com.example.quran

import android.content.Context
import com.example.domain.model.Ayah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Offline, verbatim Tanzil text used by Hifz practice. The bundled copyright block stays in the asset. */
object HifzQuranTextRepository {
    @Volatile private var cached: Map<Int, List<Ayah>>? = null

    suspend fun loadSurah(context: Context, surahNumber: Int): List<Ayah> = withContext(Dispatchers.IO) {
        val all = cached ?: synchronized(this@HifzQuranTextRepository) {
            cached ?: context.assets.open("quiz/quran-simple.txt").bufferedReader(Charsets.UTF_8).use { reader ->
                parse(reader.lineSequence()).also { cached = it }
            }
        }
        all[surahNumber].orEmpty()
    }

    internal fun parse(lines: Sequence<String>): Map<Int, List<Ayah>> {
        val verses = mutableMapOf<Int, MutableList<Ayah>>()
        lines.forEach { line ->
            if (!line.firstOrNull().let { it != null && it.isDigit() }) return@forEach
            val parts = line.split('|', limit = 3)
            require(parts.size == 3) { "Invalid Hifz Quran row" }
            val surah = parts[0].toInt()
            val ayah = parts[1].toInt()
            require(surah in 1..114 && parts[2].isNotBlank()) { "Invalid Hifz Quran reference" }
            val list = verses.getOrPut(surah) { mutableListOf() }
            require(ayah == list.size + 1) { "Missing or duplicate Hifz ayah $surah:$ayah" }
            list += Ayah(surah, ayah, parts[2], "", "", "", tafsirSummary = "", tafsirSource = "Tanzil Quran Text (Simple, Version 1.1)")
        }
        require(verses.size == 114 && verses.values.sumOf { it.size } == 6236) { "Incomplete Hifz Quran text" }
        FullQuranCatalog.surahs.forEach { surah ->
            require(verses[surah.number]?.size == surah.totalAyat) { "Incomplete ${surah.englishName}" }
        }
        return verses
    }
}
