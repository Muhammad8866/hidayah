package com.example.ui.screens

import org.junit.Assert.*
import org.junit.Test

class KalimaAudioTest {
    private val entries = listOf(listOf("a", "b"), listOf("c"), listOf("d", "e", "f"))

    @Test fun plansPhrasesInOrderWithRepeatsPerEntry() {
        val plan = planKalimaQueue(entries.take(2), repeat = 2)
        assertEquals(listOf("a", "b", "a", "b", "c", "c"), plan.map { it.text })
        assertEquals(listOf(0, 0, 1, 1, 0, 1), plan.map { it.pass })
        assertEquals(listOf(0, 1, 0, 1, 0, 0), plan.map { it.phraseIndex })
        assertEquals(listOf(0, 0, 0, 0, 1, 1), plan.map { it.entryIndex })
        assertEquals(plan, planKalimaQueue(entries.take(2), repeat = 2, startIndex = -3))
        assertEquals(3, planKalimaQueue(entries.take(2), repeat = 0).size)
    }

    @Test fun playAllStartsFromOffsetAndKeepsEntryIndices() {
        val plan = planKalimaQueue(entries, startIndex = 1)
        assertEquals(listOf("c", "d", "e", "f"), plan.map { it.text })
        assertEquals(listOf(1, 2, 2, 2), plan.map { it.entryIndex })
        assertTrue(planKalimaQueue(entries, startIndex = 3).isEmpty())
    }

    @Test fun splitsLongPhrasesIntoChunksAndSkipsBlankOnes() {
        val plan = planKalimaQueue(listOf(listOf("one two three", " ", "x")), limit = 8)
        assertEquals(listOf("one two", "three", "x"), plan.map { it.text })
        assertEquals(listOf(0, 0, 2), plan.map { it.phraseIndex })
        assertEquals(listOf(0, 1, 0), plan.map { it.chunk })
    }

    @Test fun utteranceIdsRoundTripAndRejectGaps() {
        val utterance = KalimaUtterance(entryIndex = 2, pass = 1, phraseIndex = 4, chunk = 3, text = "x")
        val id = kalimaUtteranceId(7, utterance)
        assertEquals("7:2:1:4:3", id)
        assertEquals(KalimaUtteranceRef(7, 2, 1, 4, 3), parseKalimaUtteranceId(id))
        assertEquals(KalimaUtteranceRef(7, 2, 1, 4, 0), parseKalimaUtteranceId("7:2:1:4"))
        assertNull(parseKalimaUtteranceId("7:gap"))
        assertNull(parseKalimaUtteranceId("7:2:1"))
        assertNull(parseKalimaUtteranceId("7:2:1:-4"))
        assertNull(parseKalimaUtteranceId(null))
    }

    @Test fun gapsGrowFromChunkToPhraseToEntry() {
        val first = KalimaUtterance(0, 0, 0, 0, "a")
        assertEquals(0L, kalimaGapMs(first, first.copy(chunk = 1)))
        assertEquals(KALIMA_PHRASE_GAP_MS, kalimaGapMs(first, first.copy(phraseIndex = 1)))
        assertEquals(KALIMA_ENTRY_GAP_MS, kalimaGapMs(first, first.copy(pass = 1)))
        assertEquals(KALIMA_ENTRY_GAP_MS, kalimaGapMs(first, first.copy(entryIndex = 1)))
    }

    @Test fun speechFallsBackToWholeArabicWithoutPhrases() {
        val bare = KalimaEntry("x", "X", "كامل", "r", "u", "e", KalimaGroup.KALIMA, 1, "", "", "", emptyList(), "", "", "")
        assertEquals(listOf("كامل"), kalimaSpeechTexts(bare))
        val phrased = bare.copy(phrases = listOf(KalimaPhrase("أ", "", "", ""), KalimaPhrase("ب", "", "", "")))
        assertEquals(listOf("أ", "ب"), kalimaSpeechTexts(phrased))
    }
}
