package com.example.quran

import com.example.core.id.AyahReference
import com.example.quran.data.CanonicalQuranDatasetAdapter
import com.example.quran.domain.hifz.HifzCardState
import com.example.quran.domain.hifz.HifzConfidenceLevel
import com.example.quran.domain.hifz.HifzSpacedRepetitionScheduler
import com.example.quran.domain.verifier.QuranIntegrityVerifier
import org.junit.Assert.*
import org.junit.Test

class QuranHifzDecouplingTest {

    private val facade = QuranModuleFacade()

    @Test
    fun testCanonicalAyahRetrieval() {
        val ref = AyahReference.of(1, 1)
        val result = facade.getAyah(ref)

        assertTrue(result.isSuccess)
        val ayah = result.getOrNull()!!
        assertEquals(1, ayah.surahNumber)
        assertEquals(1, ayah.ayahNumber)
        assertEquals("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", ayah.arabicText)
        assertEquals(4, ayah.words.size)
    }

    @Test
    fun testQuranIntegrityVerification_PassesForCanonicalData() {
        val verification = facade.verifyIntegrity()
        assertTrue("Canonical Al-Fatihah dataset must pass integrity check", verification.isSuccess)
    }

    @Test
    fun testSha256ChecksumComputation() {
        val text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
        val hash = QuranIntegrityVerifier.computeSha256(text)
        assertNotNull(hash)
        assertEquals(64, hash.length) // SHA-256 is 64 hex characters
    }

    @Test
    fun testHifzSpacedRepetitionProgression() {
        val ref = AyahReference.of(1, 2)
        val startTimeMs = 1774200000000L // arbitrary epoch benchmark

        // 1st review: GOOD
        val state1 = facade.recordHifzReview(ref, HifzConfidenceLevel.GOOD, startTimeMs)
        assertEquals(1, state1.repetitions)
        assertEquals(1, state1.intervalDays)
        assertEquals(startTimeMs + 86_400_000L, state1.nextReviewEpochMs)

        // 2nd review after 1 day: GOOD
        val day2Ms = startTimeMs + 86_400_000L
        val state2 = facade.recordHifzReview(ref, HifzConfidenceLevel.GOOD, day2Ms)
        assertEquals(2, state2.repetitions)
        assertEquals(2, state2.intervalDays)

        // 3rd review: AGAIN (stumble/forgetting resets repetitions to 0, interval 1 day)
        val day4Ms = day2Ms + (2 * 86_400_000L)
        val state3 = facade.recordHifzReview(ref, HifzConfidenceLevel.AGAIN, day4Ms)
        assertEquals(0, state3.repetitions)
        assertEquals(1, state3.intervalDays)
    }

    @Test
    fun testHifzCardDueStatus() {
        val ref = AyahReference.of(1, 3)
        val card = HifzCardState(
            reference = ref,
            repetitions = 1,
            easeFactor = 2.5,
            intervalDays = 2,
            lastReviewedEpochMs = 100_000L,
            nextReviewEpochMs = 100_000L + (2 * 86_400_000L)
        )

        // Before due date
        assertFalse(HifzSpacedRepetitionScheduler.isDue(card, 100_000L + 86_400_000L))
        // Exactly on or after due date
        assertTrue(HifzSpacedRepetitionScheduler.isDue(card, 100_000L + (2 * 86_400_000L)))
        assertTrue(HifzSpacedRepetitionScheduler.isDue(card, 100_000L + (3 * 86_400_000L)))
    }
}
