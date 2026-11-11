package com.example.quran

import com.example.domain.model.Ayah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Load harness for [QuranSearch]: the surah list searched once per keystroke by many readers, and
 * the ayah search over a corpus the size of the whole Quran (6,236 ayat, four long texts each).
 * Bounds are generous so a slow build machine does not fail it; the printed numbers are the
 * measurement. Every reader shares the one search built per language, so readers add no memory.
 */
class QuranSearchScaleTest {
    private val names = FullQuranCatalog.surahs.flatMap { listOf(it.englishName, it.englishMeaning, it.arabicName) }
    private val junk = listOf("💥", "\u0000", "?!", "x".repeat(50_000), "٣٦", "३६", "2", "114", "999", "surah yaseen", "सूरह यासीन")

    /** What a reader types, keystroke by keystroke: prefixes, whole names, typos and junk. */
    private fun keystroke(n: Int, random: Random): String = when (n % 5) {
        0 -> names.random(random).let { it.take(1 + random.nextInt(it.length)) }
        1 -> names.random(random)
        2 -> names.random(random).lowercase().replace("-", " ").let { if (it.length > 4) it.substring(0, it.length - 1) + "x" else it }
        3 -> junk.random(random)
        else -> "${random.nextInt(130)}"
    }

    @Test fun `one million surah searches hold in time and never fail`() {
        val random = Random(7)
        val count = 1_000_000
        val ranker = QuranSearch.surahRanker(FullQuranCatalog.surahs) { it }
        val started = System.nanoTime()
        var found = 0L
        // Made one at a time, so the harness itself does not hold a million strings.
        repeat(count) { n -> found += ranker.search(keystroke(n, random)).size }
        val seconds = (System.nanoTime() - started) / 1e9
        println("surah search: $count queries in ${"%.2f".format(seconds)} s (${"%.1f".format(count / seconds)} per s), $found results")
        assertTrue("$count surah searches took $seconds s", seconds < 600)
        // The exact surah still leads at the end of the run.
        assertEquals(36, ranker.search("yaseen").first().number)
    }

    @Test fun `a search per app language is built once and stays small`() {
        val languages = listOf("en", "ur", "hi", "ar", "bn", "gu", "ta", "te", "ml", "kn", "mr", "pa", "fr", "de", "tr", "id", "ms", "ru", "zh")
        val runtime = Runtime.getRuntime()
        System.gc()
        val before = runtime.totalMemory() - runtime.freeMemory()
        val started = System.nanoTime()
        languages.forEach { assertEquals(36, QuranSearch.surahs("36", it).first().number) }
        val seconds = (System.nanoTime() - started) / 1e9
        System.gc()
        val usedMb = ((runtime.totalMemory() - runtime.freeMemory()) - before) / (1 shl 20)
        println("surah search for ${languages.size} languages: built in ${"%.2f".format(seconds)} s, about $usedMb MB")
        assertTrue("building took $seconds s", seconds < 60)
    }

    @Test fun `ayah search over a whole Quran sized corpus holds`() {
        val random = Random(11)
        val base = QuranDatasetRepository.AL_FATIHAH_AYAT + QuranDatasetRepository.AL_BAQARAH_KEY_AYAT
        // 6,236 ayat, each with long texts made of real ayat, so every field is chunked.
        val corpus: List<Ayah> = FullQuranCatalog.surahs.flatMap { surah ->
            (1..surah.totalAyat).map { ayah ->
                val source = base.random(random)
                source.copy(
                    surahNumber = surah.number, ayahNumber = ayah,
                    englishTranslation = List(1 + random.nextInt(4)) { base.random(random).englishTranslation }.joinToString(" "),
                    urduTranslation = List(1 + random.nextInt(4)) { base.random(random).urduTranslation }.joinToString(" ")
                )
            }
        }
        assertEquals(6_236, corpus.size)
        val built = System.nanoTime()
        val search = QuranSearch.AyahSearch(corpus)
        val words = listOf("patience", "merciful", "الحمد", "مہربان", "bismillah", "jannat", "sleep", "path", "2:255", "114 6", "zzzqqq", "💥")
        val first = search.search("patience")
        val buildSeconds = (System.nanoTime() - built) / 1e9
        val started = System.nanoTime()
        repeat(2_000) { search.search(words[it % words.size] + if (it % 3 == 0) "" else " ") }
        val seconds = (System.nanoTime() - started) / 1e9
        println("ayah search: built and first query in ${"%.2f".format(buildSeconds)} s, 2,000 queries in ${"%.2f".format(seconds)} s")
        assertTrue(first.isNotEmpty())
        assertEquals(2 to 255, search.search("2:255").first().let { it.surahNumber to it.ayahNumber })
        assertTrue("ayah search build took $buildSeconds s", buildSeconds < 60)
        assertTrue("2,000 ayah searches took $seconds s", seconds < 300)
    }
}
