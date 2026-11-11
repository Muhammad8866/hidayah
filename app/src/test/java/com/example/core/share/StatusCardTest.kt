package com.example.core.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusCardTest {

    @Test
    fun `fitTextSize keeps start size when text fits`() {
        assertEquals(60f, StatusCardLayout.fitTextSize(60f, 20f, 1000) { it.toInt() * 10 }, 0f)
    }

    @Test
    fun `fitTextSize shrinks until text fits`() {
        val size = StatusCardLayout.fitTextSize(60f, 20f, 400) { (it * 10).toInt() }
        assertTrue(size <= 40f)
        assertTrue(size >= 36f)
    }

    @Test
    fun `fitTextSize never goes below minimum`() {
        assertEquals(20f, StatusCardLayout.fitTextSize(60f, 20f, 1) { 5000 }, 0f)
    }

    @Test
    fun `clamp leaves short text untouched`() {
        assertEquals("short", StatusCardLayout.clamp("  short  ", 100))
    }

    @Test
    fun `clamp cuts long text on a word and adds ellipsis`() {
        val out = StatusCardLayout.clamp("one two three four five", 12)
        assertEquals("one two…", out)
        assertTrue(out.length <= 12)
    }

    @Test
    fun `clamp handles blank and control characters`() {
        assertEquals("", StatusCardLayout.clamp("   ", 10))
        assertEquals("a b", StatusCardLayout.clamp("a\u0000\u0007 b", 10))
    }

    @Test
    fun `clamp keeps newlines`() {
        assertEquals("a\nb", StatusCardLayout.clamp("a\nb", 10))
    }

    @Test
    fun `card normalises its text fields`() {
        val card = StatusCard.of(arabic = " بِسْمِ ", translation = "x".repeat(2000), reference = " Quran 1:1 ")
        assertEquals("بِسْمِ", card.arabic)
        assertEquals("Quran 1:1", card.reference)
        assertTrue(card.translation.length <= StatusCardLayout.MAX_TRANSLATION)
    }
}
