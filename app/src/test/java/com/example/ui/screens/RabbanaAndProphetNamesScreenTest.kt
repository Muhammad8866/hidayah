package com.example.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Screenshots are written only by `recordRoborazziDebug`; plain test runs just assert the screens render. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class RabbanaAndProphetNamesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(language: String = "en", content: @Composable () -> Unit) {
        composeRule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme {
                    Surface(color = MaterialTheme.colorScheme.background) { content() }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test fun rabbanaDuasSearchByMeaning() {
        show { RabbanaDuasScreen(onBack = {}) }
        composeRule.onNodeWithTag("rabbana_dua_1").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/rabbana_duas.png")
        composeRule.onNodeWithTag("rabbana_search").performTextInput("spouses")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("rabbana_dua_32").assertIsDisplayed()
    }

    @Test fun rabbanaDuasUrduLayout() {
        show(language = "ur") { RabbanaDuasScreen(onBack = {}) }
        composeRule.onNodeWithTag("rabbana_dua_1").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/rabbana_duas_urdu.png")
    }

    @Test fun prophetNamesSearchByMeaning() {
        show { ProphetNamesScreen(onBack = {}) }
        composeRule.onNodeWithTag("prophet_names_hadith").assertIsDisplayed()
        composeRule.onNodeWithTag("prophet_name_1").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/prophet_names.png")
        composeRule.onNodeWithTag("prophet_names_search").performTextInput("seal of the prophets")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("prophet_name_63").assertIsDisplayed()
    }

    @Test fun islamicBooksLibraryListsAllFourBooks() {
        show(language = "hi") { IslamicBooksScreen(onBack = {}) }
        composeRule.onNodeWithTag("hadith_collection_bahar_e_shariat").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/islamic_books.png")
    }
}
