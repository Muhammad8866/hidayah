package com.example.ui.screens

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
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
class QuranScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun showQuran() {
        val viewModel = MainViewModel(ApplicationProvider.getApplicationContext())
        composeRule.setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsState()
                QuranScreen(uiState = uiState, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun readLibrary() {
        showQuran()
        composeRule.onNodeWithTag("quran_pdf_list").assertExists()
        composeRule.onRoot().captureRoboImage("src/test/screenshots/quran_read.png")
    }

    @Test
    fun audioLibrary() {
        showQuran()
        composeRule.onNodeWithTag("quran_tab_audio").performClick()
        composeRule.onNodeWithTag("quran_audio_search").assertExists()
        composeRule.onRoot().captureRoboImage("src/test/screenshots/quran_audio.png")
    }
}
