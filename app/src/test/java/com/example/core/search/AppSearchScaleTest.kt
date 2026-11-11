package com.example.core.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Load harness for the shared search. A search runs on one phone over one list, so "scale" is the
 * size of the list and the number of keystrokes, not other users: lists of 1,000 to 50,000 items
 * (the largest list in the app is the Quran's 6,236 ayahs in four texts), each searched with
 * 2,000 mixed keystroke queries (exact, half typed, typo, number, two words, no hit, abuse).
 * Bounds are generous so a slow build machine does not fail it; the printed numbers are the
 * measurement. Run it on its own: `gradlew :app:testDebugUnitTest --tests *AppSearchScaleTest`.
 */
class AppSearchScaleTest {
    private val words = listOf(
        "allah", "rahman", "rahim", "malik", "quddus", "salam", "mumin", "aziz", "jabbar", "khaliq",
        "subhanallah", "alhamdulillah", "bismillah", "astaghfirullah", "dua", "safar", "sona", "khana",
        "namaz", "wuzu", "ghusl", "roza", "zakat", "hajj", "kalima", "tayyab", "shahadat", "tauheed",
        "baqarah", "imran", "nisa", "maidah", "anam", "araf", "kahf", "maryam", "yasin", "mulk",
        "mercy", "forgive", "journey", "sleep", "morning", "evening", "parents", "rain", "guidance", "light"
    )
    private val arabic = listOf("الرحمن", "الرحيم", "سبحان", "الله", "الحمد", "بسم", "استغفر", "ربنا", "آتنا")
    private val hindi = listOf("नमाज़", "रोज़ा", "ज़कात", "दुआ", "अल्लाह", "रहमान", "सफ़र")

    private data class Item(val number: Int, val title: String, val native: String, val body: String)

    private fun list(size: Int, random: Random) = List(size) { n ->
        Item(
            number = n + 1,
            title = (1..2 + random.nextInt(4)).joinToString(" ") { words.random(random).replaceFirstChar(Char::uppercase) },
            native = if (random.nextBoolean()) (1..3).joinToString(" ") { arabic.random(random) } else hindi.random(random),
            // About the length of an ayah with its translation.
            body = (1..20 + random.nextInt(60)).joinToString(" ") { words.random(random) }
        )
    }

    private fun queries(count: Int, random: Random): List<String> = List(count) {
        when (it % 9) {
            0 -> words.random(random)
            1 -> words.random(random).let { w -> w.take(1 + random.nextInt(w.length)) }
            2 -> words.random(random).let { w -> if (w.length < 5) w else w.substring(0, 2) + w[3] + w[2] + w.substring(4) }
            3 -> (1 + random.nextInt(200)).toString()
            4 -> "${words.random(random)} ${words.random(random)}"
            5 -> arabic.random(random)
            6 -> hindi.random(random)
            7 -> (1..8).map { 'a' + random.nextInt(26) }.joinToString("")
            else -> "x".repeat(5_000) + " 💥 " + words.random(random)
        }
    }

    private fun usedHeap(): Long {
        val runtime = Runtime.getRuntime()
        repeat(2) { System.gc() }
        return runtime.totalMemory() - runtime.freeMemory()
    }

    private fun percentile(sorted: LongArray, p: Double) = sorted[((sorted.size - 1) * p).toInt()]

    @Test fun `search holds from 1,000 to 50,000 items`() {
        val random = Random(7)
        for (size in listOf(1_000, 10_000, 25_000, 50_000)) {
            val items = list(size, random)
            val before = usedHeap()
            val started = System.nanoTime()
            val ranker = AppSearch.Ranker(items) { AppSearch.Fields(it.title, it.native, other = listOf(it.body), number = it.number) }
            val buildMs = (System.nanoTime() - started) / 1_000_000
            val heapMb = (usedHeap() - before) / (1 shl 20)
            val times = queries(2_000, random).map { query ->
                val t = System.nanoTime()
                val found = ranker.search(query)
                assertTrue(found.size <= items.size)
                System.nanoTime() - t
            }.toLongArray().also { it.sort() }
            val p50 = percentile(times, 0.5) / 1_000
            val p95 = percentile(times, 0.95) / 1_000
            val p99 = percentile(times, 0.99) / 1_000
            println("AppSearch size=$size build=${buildMs}ms heap=${heapMb}MB p50=${p50}us p95=${p95}us p99=${p99}us")
            // A list the size of the Quran must build in well under the time of a screen opening on a CI box.
            if (size <= 25_000) assertTrue("build took ${buildMs}ms", buildMs < 15_000)
            assertTrue("p95 ${p95}us at $size", p95 < 2_000_000)
        }
    }

    @Test fun `a number always finds its own item first at every size`() {
        val random = Random(11)
        val items = list(50_000, random)
        val ranker = AppSearch.Ranker(items) { AppSearch.Fields(it.title, it.native, other = listOf(it.body), number = it.number) }
        for (n in listOf(1, 2, 9, 10, 99, 100, 255, 6_236, 49_999, 50_000)) {
            assertEquals(n, ranker.search(n.toString()).first().number)
        }
    }

    @Test fun `abusive queries finish and answer sanely`() {
        val random = Random(3)
        val items = list(10_000, random)
        val ranker = AppSearch.Ranker(items) { AppSearch.Fields(it.title, it.native, other = listOf(it.body), number = it.number) }
        val abuse = listOf(
            "", " ", "\u0000", "\uD83D", "💥".repeat(1_000), "a".repeat(100_000), "a ".repeat(50_000),
            "%s%n%x", "'; DROP TABLE x; --", "<script>alert(1)</script>", "‮evil", "٠١٢٣٤٥٦٧٨٩٠١٢",
            "99999999999999999999"
        )
        val started = System.nanoTime()
        abuse.forEach { ranker.search(it) }
        val ms = (System.nanoTime() - started) / 1_000_000
        println("AppSearch abuse x${abuse.size} = ${ms}ms")
        assertTrue("abuse took ${ms}ms", ms < 20_000)
        assertEquals(items, ranker.search(""))
    }
}
