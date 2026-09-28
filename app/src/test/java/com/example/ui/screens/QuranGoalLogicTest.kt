package com.example.ui.screens

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.quran.QuranPdfCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuranGoalLogicTest {
    private val today = LocalDate.of(2026, 9, 27)
    private lateinit var store: QuranGoalStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        store = QuranGoalStore(prefs)
    }

    @Test
    fun `old goal, log and khatm keys are still read`() {
        val prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
        prefs.edit().putInt("quran_goal", 5).putInt("quran_$today", 3).putInt("quran_khatm", 610).commit()
        assertEquals(5, store.dailyGoal)
        assertEquals(3, store.pagesOn(today))
        assertEquals(1, store.completedKhatms)
        assertEquals(6, store.position)
    }

    @Test
    fun `logging moves the khatm and undo never goes below zero`() {
        store.log(today, 5, today)
        store.log(today, -8, today)
        assertEquals(0, store.pagesOn(today))
        assertEquals(0, store.totalPages)
    }

    @Test
    fun `crossing 604 pages records a dated khatm and undo removes it`() {
        store.setPosition(600)
        assertTrue(store.log(today, 6, today))
        assertEquals(1, store.completedKhatms)
        assertEquals(2, store.position)
        assertEquals(listOf(today), store.khatmDates)
        store.log(today, -6, today)
        assertEquals(0, store.completedKhatms)
        assertTrue(store.khatmDates.isEmpty())
    }

    @Test
    fun `starting a new khatm keeps completed khatms`() {
        store.setPosition(600)
        store.log(today, 10, today)
        store.startNewKhatm()
        assertEquals(1, store.completedKhatms)
        assertEquals(0, store.position)
    }

    @Test
    fun `finish-by target spreads remaining pages and stays fixed while reading today`() {
        store.mode = QuranGoalMode.FINISH_BY
        store.finishBy = today.plusDays(9) // 10 days including today
        assertEquals(61, store.targetOn(today, today)) // ceil(604 / 10)
        store.log(today, 20, today)
        assertEquals(61, store.targetOn(today, today))
        assertEquals(10, store.daysLeft(today))
    }

    @Test
    fun `streak counts days that met that day's goal and allows today in progress`() {
        store.dailyGoal = 2
        store.log(today.minusDays(2), 2, today)
        store.log(today.minusDays(1), 3, today)
        assertEquals(2, store.currentStreak(today))
        store.log(today, 2, today)
        assertEquals(3, store.currentStreak(today))
        assertEquals(3, store.bestStreak(today))
        // Raising the goal later does not break days already logged against the old goal.
        store.dailyGoal = 10
        assertEquals(2, store.currentStreak(today))
    }

    @Test
    fun `forecast uses the last fourteen days`() {
        assertNull(store.forecastFinish(today))
        store.log(today, 28, today) // 2 pages a day over 14 days
        assertEquals(today.plusDays(288), store.forecastFinish(today)) // (604 - 28) / 2
    }

    @Test
    fun `reader counts forward page turns once a day`() {
        store.recordReaderPage("book", 10, 1, 1f, today)
        store.recordReaderPage("book", 11, 1, 1f, today)
        store.recordReaderPage("book", 12, 1, 1f, today)
        store.recordReaderPage("book", 11, 1, 1f, today) // back
        store.recordReaderPage("book", 12, 1, 1f, today) // already counted
        store.recordReaderPage("book", 40, 1, 1f, today) // jump
        assertEquals(2, store.pagesOn(today))
        assertEquals(2, store.readerPagesOn(today))
    }

    @Test
    fun `reader pages are scaled and auto count can be turned off`() {
        for (index in 0..4) store.recordReaderPage("big", index, 0, 0.5f, today)
        assertEquals(2, store.pagesOn(today))
        store.readerAutoLog = false
        for (index in 5..10) store.recordReaderPage("big", index, 0, 1f, today)
        assertEquals(2, store.pagesOn(today))
        assertFalse(store.readerAutoLog)
    }

    @Test
    fun `mushaf pages map to madani pages by para`() {
        assertEquals(1, madaniParaOf(1))
        assertEquals(30, madaniParaOf(604))
        assertEquals(2, madaniParaOf(22))
        QuranPdfCatalog.all.forEach { book ->
            book.paraPages.forEachIndexed { index, start ->
                assertEquals(book.id, MADANI_PARA_STARTS[index], toMadaniPage(start, book.paraPages, book.pageCount))
            }
            assertEquals(book.id, 1, toMadaniPage(1, book.paraPages, book.pageCount))
            assertTrue(book.id, toMadaniPage(book.pageCount, book.paraPages, book.pageCount) in MADANI_PARA_STARTS[29]..MADANI_PAGES)
            assertTrue(book.id, madaniPagesPerPage(book.paraPages) in 0.3f..1.2f)
        }
    }
}
