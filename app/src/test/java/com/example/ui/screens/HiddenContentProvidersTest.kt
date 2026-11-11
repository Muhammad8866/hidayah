package com.example.ui.screens

import com.example.festival.FestivalAamal
import com.example.festival.FestivalCautions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

/** Screens show scripture and book references, never the site or publisher the content was taken from. */
class HiddenContentProvidersTest {
    private val providers = Regex(
        "Dawat-e-Islami|dawateislami|Maktaba-tul-Madinah|QuranicNames|Tanzil|quran\\.com|understandquran|" +
            "Understand Al-Qur'an|EveryAyah|QuranFor\\.me",
        RegexOption.IGNORE_CASE
    )
    private val festivalIds = listOf(
        "new_year", "ashura", "mawlid", "gyarhvi", "rajab_start", "miraj",
        "baraat", "ramadan", "qadr", "eid_fitr", "arafah", "eid_adha"
    )

    @Test
    fun festivalReferencesKeepTheCitationButNameNoProvider() {
        festivalIds.forEach { id ->
            val references = FestivalCautions.forFestival(id).flatMap { listOf(it.source, it.englishSource) } +
                FestivalAamal.forFestival(id).flatMap { listOf(it.source, it.englishSource) }
            references.forEach { assertFalse("$id names a provider: $it", providers.containsMatchIn(it)) }
        }
        assertEquals("The month of Safar is not accursed; Quran 9:36", FestivalCautions.forFestival("new_year").single().source)
        assertEquals("Aaqa Ka Mahina; Fatawa Razawiyya", FestivalCautions.forFestival("baraat").single().source)
    }

    @Test
    fun quizQuranReferenceIsOnlyTheAyah() {
        assertEquals("Quran 2:255", quranReference(2, 255))
        assertEquals("Quran 114:6", quranReference(114, 6))
    }

    @Test
    fun screensRenderNoProviderNames() {
        listOf(
            "ui/screens/BabyNamesScreen.kt", "ui/screens/QaidaTajweedScreen.kt", "ui/screens/QaidaComponents.kt",
            "ui/screens/MirathScreen.kt", "ui/screens/ZakatScreen.kt", "ui/screens/FestivalsScreen.kt",
            "ui/tour/steps/CalcTourSteps.kt", "ui/tour/steps/QaidaTourSteps.kt"
        ).forEach { path ->
            val text = File("src/main/java/com/example/$path").readText()
            assertFalse("$path names a provider: ${providers.find(text)?.value}", providers.containsMatchIn(text))
        }
    }
}
