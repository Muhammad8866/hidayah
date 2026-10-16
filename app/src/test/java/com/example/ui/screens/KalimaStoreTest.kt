package com.example.ui.screens

import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KalimaStoreTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test fun startsWithDefaults() {
        assertEquals(KalimaUserState(), KalimaStore(context).state.value)
    }

    @Test fun choicesSurviveReload() {
        val store = KalimaStore(context)
        store.toggleFavorite("tayyibah")
        store.toggleFavorite("mufassal")
        store.toggleFavorite("mufassal")
        store.setMemorized("shahadat", true)
        store.setMemorized("tamjeed", true)
        store.setMemorized("tamjeed", false)
        store.setLastRead("tauheed")
        store.setArabicScale(1.4f)
        store.setMeaning(KalimaMeaning.ENGLISH)
        store.setShowRoman(false)
        store.setShowMeaning(false)
        repeat(3) { store.incrementCount("tamjeed") }
        store.incrementCount("istighfar")
        store.resetCount("istighfar")

        val expected = KalimaUserState(
            favorites = setOf("tayyibah"), memorized = setOf("shahadat"), lastReadId = "tauheed",
            arabicScale = 1.4f, meaning = KalimaMeaning.ENGLISH, showRoman = false, showMeaning = false,
            counts = mapOf("tamjeed" to 3)
        )
        assertEquals(expected, store.state.value)
        assertEquals(expected, KalimaStore(context).state.value)
    }

    @Test fun clampsScaleAndIgnoresUnknownIds() {
        val store = KalimaStore(context)
        store.setArabicScale(5f)
        assertEquals(1.8f, store.state.value.arabicScale)
        store.setArabicScale(0.1f)
        assertEquals(0.8f, store.state.value.arabicScale)
        store.toggleFavorite("nope")
        store.setMemorized("nope", true)
        store.setLastRead("nope")
        store.incrementCount("nope")
        assertEquals(KalimaUserState(arabicScale = 0.8f), store.state.value)

        context.getSharedPreferences("kalimas", Context.MODE_PRIVATE).edit()
            .putStringSet("favorites", setOf("removed_entry", "mujmal"))
            .putStringSet("memorized", setOf("removed_entry"))
            .putString("last_read", "removed_entry")
            .putString("meaning", "KLINGON")
            .putInt("count_removed_entry", 9)
            .commit()
        assertEquals(KalimaUserState(favorites = setOf("mujmal"), arabicScale = 0.8f), KalimaStore(context).state.value)
    }
}
