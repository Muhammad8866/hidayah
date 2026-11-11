package com.example.ui.screens

import com.example.domain.model.DuaDatasetRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuaGradingNoteTest {
    @Test fun romanHindiNoteReadsInEnglishAndStaysRomanInHinglish() {
        assertEquals("Hasan (through its combined chains)", displayGradingNote("Hasan (majmoo'i turuq se)", "en"))
        assertEquals("Hasan (majmoo'i turuq se)", displayGradingNote("Hasan (majmoo'i turuq se)", "hinglish"))
    }

    @Test fun englishNoteIsShownAsWritten() {
        assertEquals("Sahih (Ibn Hibban)", displayGradingNote("Sahih (Ibn Hibban)", "en"))
    }

    /** No dua shows a Roman-Hindi grading note to an English reader. */
    @Test fun noRomanHindiGradingNoteInEnglish() {
        val romanHindi = Regex("""\b(ki|ka|ke|se|ne|mein|kaha|kiya|kalaam|baaz|saabit|majmoo'i|qaul|amal|Qurani)\b""")
        val shown = DuaDatasetRepository.DUAS.filter { it.authenticityGrading != it.gradeLabel }
            .map { displayGradingNote(it.authenticityGrading, "en") }.distinct()
        assertTrue(shown.isNotEmpty())
        for (note in shown) assertTrue(note, !romanHindi.containsMatchIn(note))
    }
}
