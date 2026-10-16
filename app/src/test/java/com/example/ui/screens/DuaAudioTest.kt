package com.example.ui.screens

import com.example.domain.model.DuaDatasetRepository
import com.example.names.RabbanaDuas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuaAudioTest {
    @Test
    fun `all duas and rabbana texts fit speech requests without losing words`() {
        val texts = DuaDatasetRepository.DUAS.map { it.arabicText } + RabbanaDuas.duas.map { it.arabic }
        texts.forEach { text ->
            val chunks = splitDuaSpeech(text, 100)
            assertTrue(chunks.isNotEmpty())
            assertTrue(chunks.all { it.length <= 100 && it.isNotBlank() })
            assertEquals(text.trim().split(Regex("\\s+")), chunks.joinToString(" ").split(Regex("\\s+")))
        }
    }

    @Test
    fun `long text without spaces also fits engine limit`() {
        val text = "ا".repeat(301)
        val chunks = splitDuaSpeech(text, 100)
        assertTrue(chunks.all { it.length <= 100 })
        assertEquals(text, chunks.joinToString(""))
        assertTrue(splitDuaSpeech("  ", 100).isEmpty())
    }
}
