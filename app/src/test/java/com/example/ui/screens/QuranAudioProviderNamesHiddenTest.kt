package com.example.ui.screens

import com.example.quran.QuranPdfCatalog
import com.example.ui.tour.screenTourSteps
import com.example.ui.tour.tabTourSteps
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Quran, audio, dua and salah screens must not tell the user which website, publisher or API their
 * content came from. Scripture and hadith citations stay; fetch URLs stay (they are never shown).
 */
class QuranAudioProviderNamesHiddenTest {
    private val providers = Regex(
        "Tanzil|Quran\\.com|QuranCDN|Quran Foundation|EveryAyah|QuranicAudio|AlQuran|alquran\\.cloud|" +
            "Dawat-e-Islami|dawateislami|Maktaba|Internet Archive|archive\\.org|GitHub|alhajowaisrazaqadri|" +
            "Quranic Arabic Corpus|jamiat\\.org|Deeniyat|Abde Mustafa|abdemustafa",
        RegexOption.IGNORE_CASE
    )

    @Test fun `guide notes keep their citations but name no provider`() {
        listOf(FatehaGuide.content, KhutbahGuide.content).forEach { guide ->
            assertFalse(guide.sources, providers.containsMatchIn(guide.sources))
            assertTrue(guide.sources, guide.sources.contains("needs scholar review"))
        }
        assertTrue(FatehaGuide.content.sources.contains("Sahih al-Bukhari 3370"))
    }

    @Test fun `quran pdf titles name no publisher`() {
        QuranPdfCatalog.all.forEach { pdf ->
            assertFalse(pdf.title, providers.containsMatchIn(pdf.title))
            assertFalse(pdf.subtitle, providers.containsMatchIn(pdf.subtitle))
        }
    }

    @Test fun `no tour step points at a provider link`() {
        val removed = setOf("quranmodes_hifz_source", "quranmodes_tafsir_source", "recitation_source")
        val ours = Regex("^(quran|quranmodes|recitation|duas)_")
        (tabTourSteps.values + screenTourSteps.values).flatten().forEach { step ->
            assertFalse(step.target, step.target in removed)
            if (ours.containsMatchIn(step.target)) assertFalse(step.body, providers.containsMatchIn(step.title + " " + step.body))
        }
    }

    @Test fun `screen copy names no provider`() {
        val root = generateSequence(File("").absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main/java/com/example").isDirectory }
        val shown = Regex("""\b(LocalizedText|LocalizedPhraseText|localizedUiText|localizeUiText|Text|setContentText)\s*\(\s*"((?:\\.|[^"\\])*)"""")
        listOf(
            "ui/screens/QuranTafsirAudioScreen.kt", "ui/screens/QuranWordLearningScreen.kt", "ui/screens/HifzCoachScreen.kt",
            "ui/screens/NaatScreen.kt", "ui/screens/AzanScreen.kt", "ui/screens/RecitationCollectionScreen.kt",
            "ui/screens/FatehaKhutbahScreens.kt", "ui/screens/DuasAdhkarScreen.kt", "ui/screens/AdhkarTasbeehTabs.kt",
            "ui/screens/SalahScreen.kt", "quran/TafsirAudioService.kt", "quran/QuranAudioService.kt", "naat/NaatAudioService.kt"
        ).forEach { path ->
            val source = File(root, "app/src/main/java/com/example/$path").readText()
            shown.findAll(source).forEach { match ->
                assertFalse("$path: ${match.groupValues[2]}", providers.containsMatchIn(match.groupValues[2]))
            }
            assertFalse("$path still names Dawat-e-Islami", source.contains("• Dawat-e-Islami"))
        }
    }
}
