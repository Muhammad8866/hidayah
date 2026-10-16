package com.example.ui.screens

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RozaTrackerStoreTest {
    private val today = LocalDate.of(2026, 10, 10)
    private lateinit var store: RozaTrackerStore

    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
        prefs.edit().clear().putInt("roza_qaza", 2).commit()
        store = RozaTrackerStore(prefs)
    }

    @Test fun `existing balance persists and missed logging is idempotent`() {
        store.setMark(today, RozaMark.MISSED, today)
        store.setMark(today, RozaMark.MISSED, today)
        assertEquals(3, store.balance)
        store.setMark(today, RozaMark.RAMADAN, today)
        assertEquals(2, store.balance)
    }

    @Test fun `qaza completion and undo update balance once`() {
        store.setMark(today, RozaMark.QAZA, today)
        store.setMark(today, RozaMark.QAZA, today)
        assertEquals(1, store.balance)
        store.setMark(today, null, today)
        assertEquals(2, store.balance)
        assertNull(store.mark(today))
    }

    @Test fun `cannot log future fasts or pay more than balance`() {
        assertFalse(store.setMark(today.plusDays(1), RozaMark.MISSED, today))
        store.setMark(today, RozaMark.QAZA, today)
        store.setMark(today.minusDays(1), RozaMark.QAZA, today)
        assertFalse(store.setMark(today.minusDays(2), RozaMark.QAZA, today))
        assertEquals(0, store.balance)
    }

    @Test fun `nafl logs leave balance unchanged and backlog can be added`() {
        store.setMark(today, RozaMark.NAFL, today)
        store.addMissed(8)
        assertEquals(10, store.balance)
        assertEquals(RozaMark.NAFL, store.mark(today))
    }

    @Test fun `removing repaid missed fast is rejected without losing balance`() {
        store.setMark(today.minusDays(3), RozaMark.MISSED, today)
        (0..2).forEach { store.setMark(today.minusDays(it.toLong()), RozaMark.QAZA, today) }
        assertEquals(0, store.balance)
        assertFalse(store.setMark(today.minusDays(3), null, today))
        assertFalse(store.setMark(today.minusDays(3), RozaMark.NAFL, today))
        assertEquals(RozaMark.MISSED, store.mark(today.minusDays(3)))
        assertEquals(0, store.balance)
        assertTrue(store.setMark(today, null, today))
        assertTrue(store.setMark(today.minusDays(3), null, today))
        assertEquals(0, store.balance)
    }

    @Test fun `replacing a missed fast with qaza needs two available fasts`() {
        store.setMark(today.minusDays(1), RozaMark.QAZA, today)
        store.setMark(today.minusDays(2), RozaMark.QAZA, today)
        store.setMark(today, RozaMark.MISSED, today)
        assertFalse(store.setMark(today, RozaMark.QAZA, today))
        assertEquals(1, store.balance)
        store.addMissed(1)
        assertTrue(store.setMark(today, RozaMark.QAZA, today))
        assertEquals(0, store.balance)
    }

    @Test fun `history contains valid entries and survives store recreation`() {
        store.setMark(today, RozaMark.NAFL, today)
        val prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
        prefs.edit().putString("roza_log_bad-date", "NAFL").putString("roza_log_${today.minusDays(1)}", "UNKNOWN").commit()
        assertEquals(mapOf(today to RozaMark.NAFL), RozaTrackerStore(prefs).logs())
    }

    @Test fun `notes are trimmed capped and removed with the day's log`() {
        store.setMark(today, RozaMark.NAFL, today)
        store.setNote(today, "  Monday fast  ")
        store.setNote(today.minusDays(1), "x".repeat(250))
        assertEquals("Monday fast", store.note(today))
        assertEquals(200, store.note(today.minusDays(1))!!.length)
        assertEquals(setOf(today, today.minusDays(1)), store.notes().keys)
        store.setNote(today.minusDays(1), "   ")
        assertNull(store.note(today.minusDays(1)))
        store.setMark(today, null, today)
        assertNull(store.note(today))
        assertTrue(store.notes().isEmpty())
    }

    @Test fun `bulk marks are all or nothing`() {
        val days = (0..2).map { today.minusDays(it.toLong()) }
        assertFalse(store.setMarks(days, RozaMark.QAZA, today))
        assertTrue(store.logs().isEmpty())
        assertEquals(2, store.balance)
        assertFalse(store.setMarks(days + today.plusDays(1), RozaMark.MISSED, today))
        assertTrue(store.logs().isEmpty())
        // Duplicate dates count once.
        assertTrue(store.setMarks(days + today, RozaMark.MISSED, today))
        assertEquals(5, store.balance)
        assertFalse(store.setMarks(days, RozaMark.QAZA, today))
        assertEquals(days.associateWith { RozaMark.MISSED }, store.logs())
        assertTrue(store.setMarks(days, null, today))
        assertTrue(store.logs().isEmpty())
        assertEquals(2, store.balance)
    }

    @Test fun `bulk qaza over missed days nets the balance`() {
        val days = (0..1).map { today.minusDays(it.toLong()) }
        store.setMarks(days, RozaMark.MISSED, today)
        assertEquals(4, store.balance)
        assertTrue(store.setMarks(days, RozaMark.QAZA, today))
        assertEquals(0, store.balance)
        assertFalse(store.setMarks(days, RozaMark.QAZA, today.minusDays(1)))
    }

    @Test fun `balance can be corrected down but not below zero`() {
        assertFalse(store.reduceBalance(0))
        assertFalse(store.reduceBalance(3))
        assertTrue(store.reduceBalance(2))
        assertEquals(0, store.balance)
    }

    @Test fun `restore replaces every log note and the balance`() {
        store.setMark(today, RozaMark.NAFL, today)
        store.setNote(today, "old")
        val restored = today.minusDays(5)
        store.replaceAll(7, mapOf(restored to RozaMark.MISSED), mapOf(restored to " travelling "))
        assertEquals(7, store.balance)
        assertEquals(mapOf(restored to RozaMark.MISSED), store.logs())
        assertEquals(mapOf(restored to "travelling"), store.notes())
    }

    @Test fun `overflow is rejected without changing logs or balance`() {
        val prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
        prefs.edit().putInt("roza_qaza", Int.MAX_VALUE).commit()
        assertFalse(store.setMark(today, RozaMark.MISSED, today))
        assertNull(store.mark(today))
        assertEquals(Int.MAX_VALUE, store.balance)
        assertThrows(IllegalArgumentException::class.java) { store.addMissed(1) }
        assertEquals(Int.MAX_VALUE, store.balance)
    }
}

