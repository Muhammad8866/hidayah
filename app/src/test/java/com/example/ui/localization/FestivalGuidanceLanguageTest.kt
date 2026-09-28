package com.example.ui.localization

import com.example.festival.FestivalAamal
import com.example.festival.FestivalCautions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class FestivalGuidanceLanguageTest {
    private val festivalIds = listOf(
        "new_year", "ashura", "mawlid", "gyarhvi", "rajab_start", "miraj",
        "baraat", "ramadan", "qadr", "eid_fitr", "arafah", "eid_adha"
    )

    // Common Roman Hindi words that never appear in genuine English guidance.
    private val romanHindiMarkers = Regex("\\b(karein|padhein|rakhein|hai|hain|ki|ka|ke|aur|mein|se|na|samjhein|mamool)\\b")

    @Test
    fun everyGuidanceItemHasGenuineEnglishCopy() {
        festivalIds.forEach { id ->
            val english = FestivalAamal.forFestival(id).flatMap { listOf(it.englishTitle, it.englishDetail, it.englishSource) } +
                FestivalCautions.forFestival(id).flatMap { listOf(it.englishAdvice, it.englishSource) }
            english.forEach { text ->
                val latin = text.replace(Regex("[\\u0600-\\u06FF\\u0750-\\u08FF]+"), "")
                assertFalse("Roman Hindi in English copy for $id: $text", romanHindiMarkers.containsMatchIn(latin))
            }
        }
    }

    @Test
    fun englishModeShowsEnglishWithoutFallbackLabel() {
        val item = FestivalAamal.forFestival("miraj").first()
        val shown = bilingualContent(item.detail, item.englishDetail, "en")
        assertEquals(item.englishDetail, shown.text)
        assertNull(shown.fallbackLanguage)
    }

    @Test
    fun hinglishModeKeepsRomanHindi() {
        val caution = FestivalCautions.forFestival("miraj").first()
        assertEquals(caution.advice, bilingualContent(caution.advice, caution.englishAdvice, "hinglish").text)
    }
}
