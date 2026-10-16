package com.example.ui.localization

import com.example.domain.model.DuaDatasetRepository
import com.example.domain.model.i18n.DuaTranslations
import com.example.festival.FestivalAamal
import com.example.festival.IslamicFestivalRepository
import com.example.names.AsmaUlHusna
import com.example.names.ProphetNames
import com.example.names.RabbanaDuas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldDraftIntegrationTest {
    private val codes = listOf("fa", "tr", "id", "es", "ru", "zh", "it", "ja", "ar", "fr", "de", "pt")

    @Test fun everyRequestedLanguageHasCompleteStructuredDuaContent() {
        val expected = DuaDatasetRepository.DUAS.map { it.id }.toSet()
        assertEquals(298, expected.size)
        codes.forEach { code ->
            val rows = DuaTranslations.of(code)
            assertEquals(code, expected, rows.keys)
            rows.forEach { (id, row) ->
                assertTrue("$code/$id", row.title.isNotBlank() && !row.meaning.isNullOrBlank() &&
                    row.context.isNotBlank() && row.benefit.isNotBlank())
            }
        }
    }

    @Test fun existingDuaTranslationsRemainAuthoritative() {
        assertEquals("Uyanırken", DuaTranslations.of("tr").getValue("dua_waking_up").title)
    }

    @Test fun wholeContentLookupKeepsTemplatesAndParagraphsIntact() {
        val placeholders = Regex("\\{[A-Za-z0-9_]+\\}|%\\d*\\$?[sdfox]")
        codes.forEach { code ->
            val phrases = WorldDraftPhrases.forLanguage(code)
            assertFalse(code, phrases.isEmpty())
            phrases.forEach { (english, translated) ->
                assertTrue("$code/$english", translated.isNotBlank())
                assertEquals("$code/$english", placeholders.findAll(english).map { it.value }.toList().sorted(),
                    placeholders.findAll(translated).map { it.value }.toList().sorted())
                assertEquals("$code/$english", english.count { it == '\n' }, translated.count { it == '\n' })
            }
            val template = "{school} school"
            val rendered = localizeUiTemplate(template, code, mapOf("school" to "HANAFI"))
            assertTrue(code, rendered.contains("HANAFI"))
            assertFalse(code, rendered.contains("{school}"))
        }
    }

    @Test fun namesTourAndLearningContentUseTheSelectedLanguage() {
        codes.forEach { code ->
            assertNotEquals(code, AsmaUlHusna.names.first().english, AsmaUlHusna.names.first().meaning(code))
            assertNotEquals(code, ProphetNames.names.first().english, ProphetNames.names.first().meaning(code))
            assertNotEquals(code, RabbanaDuas.duas.first().english, RabbanaDuas.duas.first().meaning(code))
            assertNotEquals(code, featureTourCopy("en").steps.first().second, featureTourCopy(code).steps.first().second)
            assertNotEquals(code, "The Opening", localizePhrase("The Opening", code))
        }
    }

    @Test fun registeredProperNamesDoNotForceAnEnglishLessonFallback() {
        codes.forEach { code ->
            assertEquals(code, completeContentLanguage(code, listOf("Al-Fatihah", "The Opening")))
            assertEquals("en", completeContentLanguage(code, listOf("Al-Fatihah", "An unknown lesson sentence")))
        }
    }

    @Test fun festivalGuidanceAndNamesNeverFallBackToEnglishInArabicFrenchGermanPortuguese() {
        listOf("ar", "fr", "de", "pt").forEach { code ->
            IslamicFestivalRepository.FESTIVALS.flatMap { FestivalAamal.forFestival(it.id) }.forEach { item ->
                assertEquals("$code/${item.englishTitle}", null, bilingualContent(item.title, item.englishTitle, code).fallbackLanguage)
                assertEquals("$code/${item.englishDetail}", null, bilingualContent(item.detail, item.englishDetail, code).fallbackLanguage)
            }
            val names = AsmaUlHusna.names.map { it.english to it.meaning(code) } +
                ProphetNames.names.map { it.english to it.meaning(code) } +
                RabbanaDuas.duas.map { it.english to it.meaning(code) }
            assertTrue("$code untranslated names", names.count { (english, meaning) -> english == meaning } < 10)
        }
    }
}
