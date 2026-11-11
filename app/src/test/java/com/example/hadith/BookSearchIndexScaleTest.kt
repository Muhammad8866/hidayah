package com.example.hadith

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Load on the book search as many readers would put it: thousands of different questions, many
 * at once, over a network that is slow, failing or sending damaged bytes. Sizes stay small by
 * default for a low-memory machine; BOOK_SEARCH_SCALE (environment) or -Dbook.search.scale raises
 * them, e.g. 10000 then 100000.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BookSearchIndexScaleTest {
    private val scale = (System.getProperty("book.search.scale") ?: System.getenv("BOOK_SEARCH_SCALE"))
        ?.toIntOrNull()?.coerceAtLeast(100) ?: 2_000

    private val books = listOf(fixtureBook("e-bukhari", "bukhari"), fixtureBook("e-muslim", "muslim")) +
        HadithCatalog.books.filter { it.id == "00-bukhari-1" }

    private val words = listOf(
        "namaz", "wuzu", "roza", "safar", "raza", "honey", "abu", "hurairah", "journey", "prayer", "fasting",
        "market", "trade", "caravan", "well", "town", "sunset", "narrator", "ablution", "ramadan", "namz", "वुज़ू"
    )

    private lateinit var bodies: ByteArray

    private fun fixtureBook(id: String, collectionId: String) = HadithBook(
        id = id, collectionId = collectionId, collectionName = collectionId, romanTitle = id,
        languageCode = "en", languageName = "English", pageCount = 0, fileId = 0, slug = id,
        volume = 1, volumeCount = 1, mirrored = false
    )

    private fun resource(name: String) = checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { name }

    private fun query(random: Random, salt: Int) =
        List(1 + random.nextInt(3)) { words[random.nextInt(words.size)] }.joinToString(" ") + " q$salt"

    private fun millis(started: Long) = (System.nanoTime() - started) / 1_000_000

    @Before fun installIndex() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        BookSearchIndex.resetForTest(context)
        File(context.filesDir, "book_search").deleteRecursively()
        context.getSharedPreferences("book_search", Context.MODE_PRIVATE).edit().clear().commit()
        bodies = resource("book_search_fixture_bodies.bin").use { it.readBytes() }
        BookSearchIndex.bundled = { BookSearchIndex.Seed("scale") { resource("book_search_fixture.db") } }
        BookSearchIndex.fetchRange = { _, from, to -> bodies.copyOfRange(from.toInt(), to.toInt() + 1) }
        BookSearchIndex.fetchTimeoutMs = 8_000L
        BookSearchIndex.resetForTest(context)
        BookSearchIndex.init(context)
    }

    @Test fun `thousands of different questions keep every cache bounded`() = runBlocking {
        val random = Random(7)
        val started = System.nanoTime()
        var hits = 0L
        repeat(scale) { i ->
            val needle = query(random, i)
            val found = BookSearchIndex.search(needle, books)
            hits += found.hits.size
            if (i % 10 == 0) BookSearchIndex.withExcerpts(needle, books, found)
        }
        val elapsed = millis(started)
        println("book search scale: $scale searches, $hits hits, ${elapsed} ms, ${elapsed * 1000.0 / scale} µs each")
        assertTrue(hits > 0)
        assertTrue(BookSearchIndex.cachedSearches() <= 12)
        assertTrue(BookSearchIndex.pages.size() <= 256)
    }

    @Test fun `many readers at once over a flaky network never fail`() = runBlocking {
        val calls = AtomicInteger()
        BookSearchIndex.fetchRange = { _, from, to ->
            val call = calls.incrementAndGet()
            delay(Random.nextLong(0, 4))
            when (call % 5) {
                0 -> throw IOException("flaky")
                1 -> ByteArray((to - from + 1).toInt()) // damaged bytes
                2 -> ByteArray(1) // short answer
                else -> bodies.copyOfRange(from.toInt(), to.toInt() + 1)
            }
        }
        val workers = 16
        val errors = AtomicInteger()
        val filledPages = AtomicInteger()
        val started = System.nanoTime()
        withContext(Dispatchers.Default) {
            (0 until workers).map { worker ->
                async {
                    val random = Random(worker)
                    repeat(scale / workers) { i ->
                        try {
                            val needle = query(random, worker * scale + i)
                            val found = BookSearchIndex.search(needle, books)
                            val filled = BookSearchIndex.withExcerpts(needle, books, found)
                            check(filled.hits.size == found.hits.size)
                            check(filled.hits.zip(found.hits).all { (a, b) -> a.page == b.page && a.book == b.book })
                            check(filled.hits.all { hit -> hit.highlights.all { it.first >= 0 && it.last < hit.snippet.length } })
                            filledPages.addAndGet(filled.hits.count { it.heading == null && it.snippet.isNotEmpty() })
                        } catch (error: Exception) {
                            errors.incrementAndGet()
                        }
                    }
                }
            }.awaitAll()
        }
        println("book search concurrency: $workers readers, ${scale / workers * workers} searches, ${calls.get()} range requests, " +
            "${filledPages.get()} previews, ${errors.get()} errors, ${millis(started)} ms")
        assertEquals(0, errors.get())
        assertTrue(BookSearchIndex.cachedSearches() <= 12)
        assertTrue(BookSearchIndex.pages.size() <= 256)
    }

    @Test fun `a network that never answers costs one timeout per search`() = runBlocking {
        BookSearchIndex.fetchTimeoutMs = 100L
        BookSearchIndex.fetchRange = { _, _, _ -> delay(60_000); ByteArray(0) }
        val searches = 20
        val started = System.nanoTime()
        repeat(searches) { i ->
            val needle = "raza q$i"
            val after = BookSearchIndex.withExcerpts(needle, books, BookSearchIndex.search(needle, books))
            assertTrue(after.hits.isNotEmpty() && after.hits.all { it.heading != null || it.snippet.isEmpty() })
        }
        val elapsed = millis(started)
        println("book search timeouts: $searches searches, $elapsed ms")
        assertTrue(elapsed < searches * 1_000L)
    }

    @Test fun `the preview cache stays bounded in entries and characters`() {
        val cache = BookSearchIndex.TextCache(256, 2_000_000)
        val started = System.nanoTime()
        repeat(scale * 10) { cache.put(it, "x".repeat(200 + it % 3_000)) }
        assertTrue(cache.size() <= 256)
        assertTrue(cache.totalChars() <= 2_000_000)
        repeat(50) { cache.put(1_000_000 + it, "y".repeat(200_000)) }
        assertTrue(cache.size() <= 256)
        assertTrue(cache.totalChars() <= 2_000_000)
        // The text just stored is always kept.
        assertEquals(200_000, cache.get(1_000_049)?.length)
        println("book search page cache: ${scale * 10 + 50} puts, ${cache.size()} kept, ${cache.totalChars()} chars, ${millis(started)} ms")
    }

    @Test fun `grouping a huge number of page ranges stays fast and correct`() {
        val count = scale * 50
        val random = Random(11)
        val fileSize = 16L shl 20
        val spans = List(count) { BookSearchIndex.Span(it, random.nextLong(0, fileSize - 4_000), 300 + random.nextInt(3_000)) }
        val started = System.nanoTime()
        val groups = BookSearchIndex.coalesce(spans)
        val elapsed = millis(started)
        assertEquals(count, groups.sumOf { it.spans.size })
        assertTrue(groups.all { it.to - it.from + 1 <= BookSearchIndex.RANGE_GROUP })
        assertTrue(groups.all { group -> group.spans.all { it.at >= group.from && it.at + it.length - 1 <= group.to } })
        assertTrue(groups.zipWithNext().all { (a, b) -> a.from <= b.from })
        println("book search grouping: $count ranges into ${groups.size} requests in $elapsed ms")
    }
}
