package com.example.ui.localization

import com.example.ai.SourceConstrainedIslamicAssistant
import com.example.domain.model.DuaDatasetRepository
import com.example.domain.model.LearningDatasetRepository
import com.example.domain.model.i18n.textIn
import com.example.festival.FestivalAamal
import com.example.festival.FestivalCautions
import com.example.festival.IslamicFestivalRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Languages listed here must carry every dua, lesson, festival and assistant answer in their own script. */
class ContentCoverageTest {
    private val fullyTranslated = listOf("ne", "pa", "ta")

    // Pure Arabic text (duas quoted in the guidance) is intentionally shown unchanged.
    private fun untranslated(language: String, phrases: List<String>) =
        phrases.filter { phrase -> phrase.any { it in 'A'..'Z' || it in 'a'..'z' } && localizePhrase(phrase, language) == phrase }

    @Test
    fun `every dua has a translation`() {
        fullyTranslated.forEach { language ->
            val missing = DuaDatasetRepository.DUAS.filter { it.textIn(language) == null }.map { it.id }
            assertTrue("$language is missing duas: $missing", missing.isEmpty())
        }
    }

    @Test
    fun `every lesson is fully translated`() {
        val phrases = LearningDatasetRepository.LESSONS.flatMap {
            listOf(it.title, it.overview, it.contentMarkdown) + it.practicalTakeaways
        }
        fullyTranslated.forEach { language ->
            val missing = untranslated(language, phrases)
            assertTrue("$language is missing lesson text: $missing", missing.isEmpty())
        }
    }

    @Test
    fun `every festival description, aamal and caution is translated`() {
        val festivals = IslamicFestivalRepository.FESTIVALS
        val phrases = festivals.flatMap { listOf(it.name, it.description) } +
            festivals.flatMap { FestivalAamal.forFestival(it.id) }.flatMap { listOf(it.englishTitle, it.englishDetail) } +
            festivals.flatMap { FestivalCautions.forFestival(it.id) }.map { it.englishAdvice }
        fullyTranslated.forEach { language ->
            val missing = untranslated(language, phrases)
            assertTrue("$language is missing festival text: $missing", missing.isEmpty())
        }
    }

    @Test
    fun `pure arabic guidance is never reported as a fallback`() {
        val arabic = "لَا إِلٰهَ إِلَّا اللّٰهُ"
        (fullyTranslated + "hi").forEach { language ->
            assertEquals(language, DisplayMeaning(arabic), bilingualContent(arabic, arabic, language))
        }
    }

    @Test
    fun `every assistant answer is translated`() {
        val phrases = listOf("divorce", "patience", "parents", "wudu", "charity").flatMap { query ->
            val response = SourceConstrainedIslamicAssistant.answerQuery(query)
            listOfNotNull(response.summaryAnswer, response.scholarlyContext, response.recognizedDifferences)
        }
        fullyTranslated.forEach { language ->
            val missing = untranslated(language, phrases)
            assertTrue("$language is missing assistant text: $missing", missing.isEmpty())
        }
    }
}
