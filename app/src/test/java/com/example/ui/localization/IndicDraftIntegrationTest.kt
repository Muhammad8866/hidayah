package com.example.ui.localization

import com.example.domain.model.i18n.DuaTranslations
import com.example.domain.model.LearningDatasetRepository
import com.example.domain.model.DuaDatasetRepository
import com.example.names.AsmaUlHusna
import com.example.names.ProphetNames
import com.example.names.RabbanaDuas
import com.example.salah.GuideForm
import com.example.salah.Madhhab
import com.example.salah.SalahContent
import com.example.ui.screens.DuaSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IndicDraftIntegrationTest {
    @Test
    fun regionalDailyToolsHaveEveryReferencePhrase() {
        val codes = listOf("as", "bn", "brx", "doi", "gu", "kn", "ks", "kok", "mai", "ml",
            "mni", "mr", "ne", "or", "pa", "sa", "sat", "sd", "ta", "te")
        codes.forEach { code ->
            val pack = uiLanguagePack(code)!!
            val missing = dailyToolPhrases.getValue("hi").keys - pack.phrases.keys
            assertTrue("$code missing daily-tool phrases: $missing", missing.isEmpty())
            dailyToolPhrases.getValue("hi").keys.forEach { key ->
                assertTrue("$code blank: $key", pack.phrases.getValue(key).isNotBlank())
                assertEquals(pack.phrases.getValue(key), localizePhrase(key, code))
                val placeholders = Regex("\\{[A-Za-z0-9_]+\\}")
                assertEquals("$code placeholders: $key",
                    placeholders.findAll(key).map { it.value }.toList().sorted(),
                    placeholders.findAll(pack.phrases.getValue(key)).map { it.value }.toList().sorted())
            }
        }
    }

    @Test
    fun generatedDuaQuizUsesSelectedDuaPack() {
        val source = DuaSource(DuaDatasetRepository.DUAS, "as")
        val question = (0 until source.size).firstNotNullOf { source.question(it) }
        val titles = DuaTranslations.of("as").values.map { it.title }.toSet()
        assertTrue(question.options.all { it in titles })
    }

    @Test
    fun regionalSalahCopyKeepsCompositionAndPlaceholdersLocalized() {
        val first = SalahContent.Notes.DAILY
        val second = SalahContent.Notes.ALOUD
        assertNotEquals(first.en, first.inLanguage("as"))
        assertEquals("${first.inLanguage("as")} ${second.inLanguage("as")}",
            (first then second).inLanguage("as"))
        val qunut = SalahContent.prayerSteps(GuideForm.WITR_3, Madhhab.HANAFI)
            .first { it.id == "qunut" }.instruction
        assertNotEquals(qunut.en, qunut.inLanguage("as"))
        assertTrue(!qunut.inLanguage("as").contains("{hands}"))
        assertTrue(!qunut.inLanguage("as").contains("to the ears"))
        assertNotEquals("The Opening", localizePhrase("The Opening", "as"))
    }

    @Test
    fun missingContentIsConnectedToRegionalScreens() {
        assertEquals(298, DuaTranslations.of("as").size)
        assertEquals(298, DuaTranslations.of("kn").size)
        assertNotEquals("Prayer tracker", localizePhrase("Prayer tracker", "as"))
        assertNotEquals(
            "Welcome to Hidayah. Take a quick tour of everything the app offers.",
            featureTourCopy("as").steps.first().second
        )
        assertNotNull(localizedMoreSubtitle("Tools & resources", "brx"))
        assertNotEquals(AsmaUlHusna.names.first().english, AsmaUlHusna.names.first().meaning("as"))
        assertNotEquals(ProphetNames.names.first().english, ProphetNames.names.first().meaning("as"))
        assertNotEquals(RabbanaDuas.duas.first().english, RabbanaDuas.duas.first().meaning("as"))
        val lesson = LearningDatasetRepository.LESSONS.first()
        assertNotEquals(lesson.contentMarkdown, localizePhrase(lesson.contentMarkdown, "as"))
        assertNotEquals(
            "First day of Muharram and the start of the new Hijri year.",
            localizePhrase("First day of Muharram and the start of the new Hijri year.", "as")
        )
        val reminder = localizePhrase("Roza {n} · Iftar at {time}", "as")
        assertTrue(reminder.contains("{n}"))
        assertTrue(reminder.contains("{time}"))
    }
}
