package com.example.ui.screens

import com.example.ui.tour.steps.kalimaScreenTourSteps
import com.example.ui.tour.steps.kalimaTabTourSteps
import com.example.ui.tour.steps.moreScreenTourSteps
import com.example.ui.tour.steps.moreTabTourSteps
import com.example.ui.tour.steps.namesTabTourSteps
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The app does not show which website, publisher or API its content comes from. */
class NoContentProviderCreditsTest {
    private val providers = Regex(
        "(?i)tanzil|quran\\.com|qurancdn|everyayah|quranicaudio|alquran ?cloud|dawat-?e-?islami|dawateislami|" +
            "maktaba|madinatul|internet archive|archive\\.org|abde ?mustafa|fawazahmed|understandquran|" +
            "islaminperspective|emaan library"
    )
    private val link = Regex("(?i)https?://|www\\.")

    private fun assertNoProvider(where: String, text: String) =
        assertFalse("$where names a content provider: $text", providers.containsMatchIn(text))

    /** Shown copy may not carry a web address either; source files can (contact, payment and policy links). */
    private fun assertNoProviderOrLink(where: String, text: String) {
        assertNoProvider(where, text)
        assertFalse("$where shows a web link: $text", link.containsMatchIn(text))
    }

    @Test
    fun `more, kalima and names guides name no content provider`() {
        val steps = (moreTabTourSteps + kalimaTabTourSteps + namesTabTourSteps).values +
            (moreScreenTourSteps + kalimaScreenTourSteps).values
        steps.flatten().forEach { assertNoProviderOrLink("tour ${it.target}", it.title + " " + it.body) }
        assertTrue(steps.flatten().none { it.target == "more_credits_row" || it.target == "kalima_sources" || it.target == "more_settings_sources" })
    }

    @Test
    fun `kalima texts name no content provider`() {
        assertNoProviderOrLink("kalima about", KalimasContent.ABOUT + " " + KalimasContent.ABOUT_ENGLISH)
        KalimasContent.entries.forEach { entry ->
            assertNoProviderOrLink("kalima ${entry.id}", listOf(entry.note, entry.noteEnglish, entry.reference).joinToString(" "))
            assertNoProviderOrLink("kalima share ${entry.id}", KalimasContent.shareText(entry, english = true) + KalimasContent.shareText(entry, english = false))
        }
    }

    @Test
    fun `contact and settings screens list no content providers`() {
        val screens = generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "app/src/main/java/com/example/ui/screens") }.first { it.isDirectory }
        listOf("CreditsScreen.kt", "MoreScreen.kt", "SettingsScreen.kt", "KalimasScreenParts.kt", "KalimasContent.kt").forEach { name ->
            File(screens, name).readLines().forEachIndexed { index, line ->
                assertNoProvider("$name:${index + 1}", line)
            }
        }
    }

    @Test
    fun `privacy policy keeps the Tanzil license notice`() {
        val policy = generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "app/src/main/assets/privacy-policy.html") }.first { it.exists() }.readText()
        assertTrue(policy.contains("Tanzil Quran Text (Simple, Version 1.1)"))
        assertTrue(policy.contains("Creative Commons Attribution 3.0"))
        assertTrue(policy.contains("https://tanzil.net"))
    }
}
