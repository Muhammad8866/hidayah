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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.naat.NaatAudioManager
import com.example.naat.NaatCatalog
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertTrue
import org.junit.After
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
class NaatScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @After fun resetPlayer() {
        NaatAudioManager.stop()
        NaatAudioManager.clearError()
    }

    private fun show(language: String = "en", dark: Boolean = false) {
        composeRule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme(darkTheme = dark) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        NaatScreen(onBack = {}, modifier = Modifier)
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test fun bundledCatalogHasBothNaatKhawans() {
        NaatCatalog.load(ApplicationProvider.getApplicationContext())
        val byPerformer = NaatCatalog.current.value.groupBy { it.performer }
        assertTrue((byPerformer["Muhammad Owais Raza Qadri"]?.size ?: 0) > 300)
        assertTrue((byPerformer["Hafiz Muhammad Tahir Qadri"]?.size ?: 0) > 150)
        assertTrue(NaatCatalog.current.value.all { it.url.startsWith("https://") && it.sizeBytes > 0 })
    }

    @Test fun owaisListIsShownFirst() {
        show()
        composeRule.onNodeWithTag("naat_owais-aahista-chal").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/naat_owais.png")
    }

    @Test fun switchingToTahirQadriAndSearching() {
        show(dark = true)
        composeRule.onNodeWithTag("naat_khawan_tq").performClick()
        composeRule.onNodeWithTag("naat_search").performTextInput("medine")
        composeRule.waitForIdle()
        // Different Roman Urdu spelling still finds "Madinay Ka Safar Hai".
        composeRule.onNodeWithTag("naat_tahir-madinay-ka-safar-hai").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/naat_tahir_search_dark.png")
    }

    @Test fun urduLayout() {
        show(language = "ur")
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/naat_urdu.png")
    }

    @Test fun playingShowsMiniPlayer() {
        show()
        composeRule.onNodeWithTag("naat_play_all").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("naat_player").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/naat_player.png")
    }

    @Test fun downloadAllAsksFirst() {
        show()
        composeRule.onNodeWithTag("naat_download_all").performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/naat_download_dialog.png")
    }
}
