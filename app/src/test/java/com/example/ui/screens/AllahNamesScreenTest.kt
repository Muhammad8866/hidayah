package com.example.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import com.example.names.AsmaUlHusna
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedMoreSubtitle
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Screenshots are written only by `recordRoborazziDebug`; plain test runs just assert the screen renders. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AllahNamesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(language: String = "en") {
        composeRule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AllahNamesScreen(onBack = {}, modifier = Modifier)
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test fun listHasNinetyNineCompleteUniqueNames() {
        val names = AsmaUlHusna.names
        assertEquals(99, names.size)
        assertEquals((1..99).toList(), names.map { it.number })
        assertEquals(99, names.map { it.arabic }.toSet().size)
        assertEquals(99, names.map { it.transliteration }.toSet().size)
        assertTrue(names.all { name ->
            listOf(name.arabic, name.transliteration, name.english, name.urdu, name.hindi, name.hinglish).all(String::isNotBlank)
        })
        assertEquals("Ar-Rahman", names.first().transliteration)
        assertEquals("As-Saboor", names.last().transliteration)
    }

    @Test fun screenCopyIsTranslated() {
        listOf("hinglish", "hi", "ur").forEach { language ->
            listOf("99 Names of Allah", "Search name or meaning", "No name found.", AsmaUlHusna.HADITH, AsmaUlHusna.LIST_SOURCE)
                .forEach { text -> assertNotEquals("$language: $text", text, localizeUiText(text, language)) }
            assertTrue(!localizedMoreSubtitle("Asma ul Husna with meanings", language).isNullOrBlank())
        }
    }

    @Test fun searchingByMeaning() {
        show()
        composeRule.onNodeWithTag("allah_names_hadith").assertIsDisplayed()
        composeRule.onNodeWithTag("allah_name_1").assertIsDisplayed()
        composeRule.onNodeWithTag("allah_names_search").performTextInput("provider")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("allah_name_17").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/allah_names_search.png")
    }

    @Test fun urduLayout() {
        show(language = "ur")
        composeRule.onNodeWithTag("allah_name_1").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/allah_names_urdu.png")
    }

    @Test fun englishLayout() {
        show()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/allah_names.png")
    }
}
