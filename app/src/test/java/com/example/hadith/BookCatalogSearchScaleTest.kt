package com.example.hadith

import com.example.hadith.BookCatalogSearch.Entry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Load harness: synthetic libraries of 1,000, 10,000 and 20,000 books (the most the Dawat-e-Islami
 * list may hold, [DawateIslamiIndex.MAX_BOOKS]) searched with a mix of exact, prefix, typo,
 * multi-word and no-hit queries. Bounds are generous so a slow build machine does not fail it;
 * the printed numbers are the measurement.
 */
class BookCatalogSearchScaleTest {
    private val words = listOf(
        "faizan", "sunnat", "bahar", "shariat", "anwar", "namaz", "roza", "zakat", "hajj", "quran",
        "tafseer", "hadees", "seerat", "mustafa", "madina", "makkah", "imam", "ahmad", "raza", "khan",
        "ilyas", "qadri", "attar", "fatawa", "razvia", "kanz", "iman", "noor", "irfan", "tazkira",
        "auliya", "karamat", "ghaus", "azam", "jilani", "khwaja", "gharib", "nawaz", "dua", "durood",
        "salam", "mehfil", "milad", "ishq", "rasool", "aqeeda", "tauheed", "risalat", "akhirat", "qabr",
        "hashr", "jannat", "dozakh", "tauba", "sabr", "shukr", "ikhlas", "taqwa", "zuhd", "adab",
        "walidain", "aulad", "nikah", "talaq", "wirasat", "tijarat", "sood", "halal", "haram", "makrooh",
        "wuzu", "ghusl", "tayammum", "azan", "iqamat", "jamaat", "juma", "eid", "qurbani", "aqiqa",
        "ramzan", "taraweeh", "itikaf", "shab", "barat", "qadr", "meraj", "hijrat", "ghazwa", "sahaba",
        "ahle", "bait", "khulafa", "rashideen", "siddiq", "farooq", "usman", "ali", "hasan", "husain",
        "karbala", "shuhada", "madani", "phool", "bayan", "islah", "nafs", "qalb", "zikr", "fikr",
        "behisht", "zewar", "jannati", "zevar", "islami", "behno", "masail", "ahkam", "fazail", "barakat"
    )
    private val joiners = listOf("e", "ul", "al", "ki", "ka", "ke", "aur", "tul", "un")
    private val urdu = listOf("فیضان", "سنت", "بہار", "شریعت", "نماز", "روزہ", "زکوٰۃ", "قرآن", "حدیث", "سیرت", "مدینہ")
    private val hindi = listOf("नमाज़", "रोज़ा", "ज़कात", "क़ुरआन", "हदीस", "सीरत", "मदीना", "बहारे", "शरीअत")

    private fun library(size: Int, random: Random): List<Entry> {
        val authors = (1..200).map { "Maulana ${words.random(random).replaceFirstChar(Char::uppercase)} ${words.random(random)} Qadri" }
        return List(size) {
            val count = 3 + random.nextInt(6)
            val title = (1..count).joinToString(" ") { i ->
                if (i % 2 == 0 && random.nextInt(3) == 0) joiners.random(random) else words.random(random).replaceFirstChar(Char::uppercase)
            }
            val native = when (random.nextInt(4)) {
                0 -> (1..2 + random.nextInt(3)).joinToString(" ") { urdu.random(random) }
                1 -> (1..2 + random.nextInt(3)).joinToString(" ") { hindi.random(random) }
                else -> ""
            }
            Entry(title, native, authors.random(random), listOf("Jild ${1 + random.nextInt(9)}", words.random(random)))
        }
    }

    private fun typo(word: String, random: Random): String {
        if (word.length < 5) return word
        val at = random.nextInt(word.length - 1)
        return word.substring(0, at) + word[at + 1] + word[at] + word.substring(at + 2)
    }

    private fun queries(count: Int, random: Random): List<String> = List(count) {
        when (it % 6) {
            0 -> words.random(random)
            1 -> words.random(random).let { w -> w.take(1 + random.nextInt(w.length)) }
            2 -> typo(words.random(random), random)
            3 -> "${words.random(random)} e ${words.random(random)}"
            4 -> "${words.random(random)}${words.random(random)}"
            else -> (1..8).map { 'a' + random.nextInt(26) }.joinToString("")
        }
    }

    private fun usedHeap(): Long {
        val runtime = Runtime.getRuntime()
        repeat(2) { System.gc() }
        return runtime.totalMemory() - runtime.freeMemory()
    }

    private fun percentile(sorted: LongArray, p: Double) = sorted[((sorted.size - 1) * p).toInt()]

    @Test fun `search holds from 1,000 to 20,000 books`() {
        val random = Random(42)
        for (size in listOf(1_000, 10_000, 20_000)) {
            val books = library(size, random)
            val before = usedHeap()
            val built = System.nanoTime()
            val index = BookCatalogSearch.Index(books)
            val buildMs = (System.nanoTime() - built) / 1_000_000
            val heapMb = (usedHeap() - before).coerceAtLeast(0) / (1 shl 20)
            val asked = queries(5_000, random)
            asked.take(200).forEach { index.scores(it) } // warm the JIT
            var matched = 0L
            val times = LongArray(asked.size) { i ->
                val start = System.nanoTime()
                val scores = checkNotNull(index.scores(asked[i]))
                val took = System.nanoTime() - start
                matched += scores.count { it > 0 }
                took
            }.also { it.sort() }
            val p50 = percentile(times, 0.50) / 1_000
            val p99 = percentile(times, 0.99) / 1_000
            val max = times.last() / 1_000
            println("books=$size build=${buildMs}ms heap=${heapMb}MB p50=${p50}us p99=${p99}us max=${max}us avgHits=${matched / asked.size}")
            assertEquals(size, index.size)
            assertTrue("build took ${buildMs}ms", buildMs < 5_000)
            assertTrue("p99 was ${p99}us", p99 < 100_000)
            assertTrue("index used ${heapMb}MB", heapMb < 256)
        }
    }

    @Test fun `abuse and match-everything queries stay bounded at 20,000 books`() {
        val random = Random(7)
        val books = library(20_000, random)
        val index = BookCatalogSearch.Index(books)
        val garbage = List(1_000) { (1..200).map { 'a' + random.nextInt(26) }.joinToString("") }
        val started = System.nanoTime()
        garbage.forEach { checkNotNull(index.scores(it)) }
        val garbageMs = (System.nanoTime() - started) / 1_000_000
        // Every synthetic book carries a "Jild" volume name, so this matches all of them.
        val everyStart = System.nanoTime()
        val every = checkNotNull(index.scores("jild"))
        val everyMs = (System.nanoTime() - everyStart) / 1_000_000
        val prefixStart = System.nanoTime()
        val oneLetter = checkNotNull(index.scores("m"))
        val prefixMs = (System.nanoTime() - prefixStart) / 1_000_000
        val ranked = BookCatalogSearch.rank("jild", books) { it }
        println("abuse: 1000x200-char=${garbageMs}ms matchAll=${everyMs}ms oneLetter=${prefixMs}ms (${oneLetter.count { it > 0 }} hits)")
        assertEquals(20_000, every.count { it > 0 })
        assertEquals(20_000, ranked.size)
        assertTrue("1,000 garbage queries took ${garbageMs}ms", garbageMs < 20_000)
        assertTrue("match-all took ${everyMs}ms", everyMs < 2_000)
        assertTrue("one-letter prefix took ${prefixMs}ms", prefixMs < 2_000)
    }
}
