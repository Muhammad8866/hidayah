package com.example.ui.screens

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RecitationStoreTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val prefs get() = context.getSharedPreferences("recitation_collection", Context.MODE_PRIVATE)
    private var date = LocalDate.of(2026, 10, 10)
    private fun store() = RecitationStore(context) { date }

    @Before fun clearPreferences() { prefs.edit().clear().commit() }

    @Test fun countsPersistAndAreCoerced() {
        val store = store()
        store.setCount("a", 5, 3)
        assertEquals(3, store.count("a", 3))
        assertEquals(3, prefs.getInt("count_a", 0))
        store.setCount("a", -2, 3)
        assertEquals(0, store.count("a", 3))
        store.setCount("b", 7, 33)
        assertEquals(7, store().count("b", 33))
        assertEquals(5, store().count("b", 5))
        assertEquals(0, store.count("missing", 3))
        store.resetAll(listOf("b"))
        assertEquals(0, store.count("b", 33))
        assertFalse(prefs.contains("count_b"))
    }

    @Test fun legacyCountsAndFavoritesAreRead() {
        prefs.edit().putInt("count_quran_1_1_7", 1).putStringSet("favorites", setOf("x")).putBoolean("meaning", false).commit()
        val store = store()
        assertEquals(1, store.count("quran_1_1_7", 1))
        assertEquals(setOf("x"), store.state.value.favorites)
        assertFalse(store.state.value.settings.showMeaning)
        store.toggleFavorite("y")
        store.toggleFavorite("x")
        assertEquals(setOf("y"), store().state.value.favorites)
    }

    @Test fun dailyResetClearsCountsButKeepsEverythingElse() {
        val store = store()
        store.setCount("a", 2, 3)
        store.toggleFavorite("a")
        store.updateSettings { it.copy(fontStep = 3, haptics = false) }
        store.markRead(1, "a")
        store.recordCompletion(0)
        date = date.plusDays(1)
        store.refreshDay()
        assertEquals(0, store.count("a", 3))
        assertFalse(prefs.contains("count_a"))
        val state = store().state.value
        assertEquals(setOf("a"), state.favorites)
        assertEquals(3, state.settings.fontStep)
        assertFalse(state.settings.haptics)
        assertEquals(1, state.streak)
        assertEquals("a", state.lastRead[1])
    }

    @Test fun dailyResetOffKeepsCounts() {
        val store = store()
        store.updateSettings { it.copy(dailyReset = false) }
        store.setCount("a", 2, 3)
        date = date.plusDays(3)
        store.refreshDay()
        assertEquals(2, store.count("a", 3))
        assertEquals(2, store().count("a", 3))
    }

    @Test fun newStoreOnALaterDayResets() {
        store().setCount("a", 2, 3)
        date = date.plusDays(1)
        assertEquals(0, store().count("a", 3))
    }

    @Test fun streakIncrementsBreaksAndIsIdempotent() {
        val store = store()
        store.recordCompletion(0)
        store.recordCompletion(2)
        assertEquals(1, store.state.value.streak)
        date = date.plusDays(1)
        store.refreshDay()
        store.recordCompletion(1)
        assertEquals(2, store.state.value.streak)
        assertEquals(2, store().state.value.streak)
        date = date.plusDays(2)
        store.refreshDay()
        assertEquals(0, store.state.value.streak)
        store.recordCompletion(0)
        assertEquals(1, store.state.value.streak)
    }

    @Test fun clockMovingBackwardsDoesNotInflateStreakOrClearCounts() {
        val store = store()
        store.recordCompletion(0)
        store.setCount("a", 1, 3)
        date = date.minusDays(5)
        store.refreshDay()
        store.recordCompletion(0)
        assertEquals(1, store.state.value.streak)
        assertEquals(1, store.count("a", 3))
    }

    @Test fun largeTextMigratesToFontStepOnce() {
        prefs.edit().putBoolean("large_text", true).commit()
        val store = store()
        assertEquals(2, store.state.value.settings.fontStep)
        store.updateSettings { it.copy(fontStep = 0) }
        assertEquals(0, store().state.value.settings.fontStep)
    }

    @Test fun corruptValuesFallBackToDefaults() {
        prefs.edit().putString("count_a", "x").putInt("favorites", 1).putString("font_step", "big")
            .putString("day", "today").putString("streak_day", "y").putInt("streak", -4)
            .putString("last_tab", "z").putInt("last_read_0", 3).putString("meaning", "yes").commit()
        val store = store()
        assertEquals(RecitationStoreState(), store.state.value)
        store.recordCompletion(0)
        store.setCount("a", 1, 1)
        store.toggleFavorite("a")
        assertEquals(1, store.state.value.streak)
        assertEquals(1, store.count("a", 1))
    }

    @Test fun settingsAndTabAreCoerced() {
        val store = store()
        store.updateSettings { it.copy(fontStep = 9) }
        store.setTab(7)
        assertEquals(3, store.state.value.settings.fontStep)
        assertEquals(2, store().state.value.lastTab)
    }

    @Test fun fontSizes() {
        assertEquals(25 to 46, recitationFontSize(0))
        assertEquals(32 to 56, recitationFontSize(2))
        assertEquals(recitationFontSize(3), recitationFontSize(10))
        assertEquals(recitationFontSize(0), recitationFontSize(-1))
    }

    @Test fun shareTextSkipsBlankParts() {
        assertEquals("Title\n\nArabic\n\nMeaning\n\nSource",
            recitationShareText(" Title ", "Source", "Arabic", "  ", "Meaning"))
        assertEquals("", recitationShareText("", "", "", "", ""))
    }

    @Test fun matchesAllWords() {
        assertTrue(recitationMatches("", "anything"))
        assertTrue(recitationMatches("  kursi AYAT ", "Ayat al Kursi 2:255"))
        assertFalse(recitationMatches("kursi falaq", "Ayat al Kursi 2:255"))
    }

    @Test fun wazaifCategoriesCoverWazaif() {
        assertEquals(3, RecitationCollection.wazaifCategories.size)
        assertEquals(RecitationCollection.wazaifCategories.toSet(), RecitationCollection.wazaif.map { it.category }.toSet())
    }

    @Test fun resumePrefersLastReadThenWrapsToFirstUnfinished() {
        val ids = listOf("a", "b", "c", "d")
        assertEquals(0, recitationResumeIndex(ids, null) { false })
        assertEquals(2, recitationResumeIndex(ids, "c") { false })
        // Last read is finished: move on to the next unfinished item after it.
        assertEquals(3, recitationResumeIndex(ids, "b") { it in setOf("b", "c") })
        // Nothing unfinished after it: wrap to the top.
        assertEquals(0, recitationResumeIndex(ids, "d") { it == "d" })
        assertEquals(-1, recitationResumeIndex(ids, "a") { true })
        assertEquals(-1, recitationResumeIndex(emptyList(), null) { false })
        // An id that is filtered out of the list falls back to the first unfinished item.
        assertEquals(1, recitationResumeIndex(ids, "zz") { it == "a" })
    }

    @Test fun ayahMarksUseArabicIndicDigits() {
        assertEquals("﴿٢٥٥﴾", recitationAyahMark(255))
        assertEquals("﴿٧﴾", recitationAyahMark(7))
    }
}
