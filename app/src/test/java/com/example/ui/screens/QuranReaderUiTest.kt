package com.example.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.unit.LayoutDirection
import com.example.domain.model.MushafLine
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuranReaderUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val pageState = MainUiState(
        quranPageNumber = 2,
        quranPageLines = listOf(
            MushafLine(
                type = "ayah",
                isCentered = true,
                text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِیْمِ"
            )
        ),
        isQuranLoading = false
    )

    @Test
    fun `immersive reader exposes stable page controls in RTL app language`() {
        var requestedPage = 0
        var immersive = true
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MyApplicationTheme {
                    QuranReader(
                        uiState = pageState,
                        loadPage = { requestedPage = it },
                        isImmersive = immersive,
                        onImmersiveChange = { immersive = it }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("quran_immersive_controls", useUnmergedTree = true).assertExists()
        val previous = composeRule.onNodeWithTag("quran_previous_page", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val next = composeRule.onNodeWithTag("quran_next_page", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue("Previous must stay left of Next in every app language", previous.center.x < next.center.x)

        composeRule.onNodeWithTag("quran_next_page", useUnmergedTree = true).performClick()
        composeRule.runOnIdle {
            assertEquals(3, requestedPage)
            assertTrue("Page controls must not dismiss immersive mode", immersive)
        }

        composeRule.onNodeWithTag("quran_exit_full_screen", useUnmergedTree = true).performClick()
        composeRule.runOnIdle { assertFalse(immersive) }
    }

    @Test
    fun `loaded page remains visible while next page is loading`() {
        composeRule.setContent {
            MyApplicationTheme {
                QuranReader(
                    uiState = pageState.copy(isQuranLoading = true, quranPendingPage = 3),
                    loadPage = {}
                )
            }
        }

        composeRule.onNodeWithTag("mushaf_page_2", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("quran_page_loading", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `verification failure is visible and retries the requested page`() {
        var requestedPage = 0
        val error = "Quran page could not be verified and was not displayed."
        composeRule.setContent {
            MyApplicationTheme {
                QuranReader(
                    uiState = pageState.copy(
                        quranPageLines = emptyList(),
                        quranPendingPage = 12,
                        quranLoadError = error
                    ),
                    loadPage = { requestedPage = it }
                )
            }
        }

        composeRule.onNodeWithText(error).assertExists()
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.runOnIdle { assertEquals(12, requestedPage) }
    }
}
