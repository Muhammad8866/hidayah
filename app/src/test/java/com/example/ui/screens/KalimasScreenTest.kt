package com.example.ui.screens

import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KalimasScreenTest {
    @get:Rule val composeRule = createComposeRule()

    private fun launch() = composeRule.setContent { MyApplicationTheme { Surface { KalimasScreen(onBack = {}) } } }

    /** The list is lazy, so bring an item into composition before touching it. */
    private fun reveal(matcher: SemanticsMatcher): SemanticsNodeInteraction {
        composeRule.onNodeWithTag("kalimas_screen").performScrollToNode(matcher)
        return composeRule.onNode(matcher)
    }
    private fun reveal(tag: String) = reveal(hasTestTag(tag))

    @Test fun searchFindsImanAndRecoversFromNoResults() {
        launch()
        reveal("kalima_tayyibah").assertIsDisplayed()
        reveal("kalimas_search").performTextInput("mufassal")
        reveal("kalima_mufassal").assertIsDisplayed()
        composeRule.onNodeWithTag("kalima_tayyibah").assertDoesNotExist()

        // Meaning language lives in the reading-settings sheet.
        reveal("kalimas_settings").performClick()
        composeRule.onNodeWithTag("kalimas_meaning_english").performScrollTo().performClick()
        composeRule.onNodeWithTag("kalimas_settings_done").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Opening the card shows the full English meaning in the reader; back returns to the list.
        reveal("kalima_mufassal").performClick()
        composeRule.onNodeWithText(KalimasContent.entries.last().english).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("kalimas_reader_back").performClick()

        reveal("kalimas_search").performTextReplacement("no-matching-entry")
        // The floating "Play all" bar can sit over the action on small screens; trigger it semantically.
        reveal(hasText("Clear search")).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick)
        reveal("kalima_tayyibah").assertIsDisplayed()
    }

    @Test fun favouriteFilterShowsOnlyStarredEntries() {
        launch()
        reveal("kalima_fav_tayyibah").performClick()
        reveal("kalimas_filters")
        composeRule.onNodeWithTag("kalimas_filter_favourites").performScrollTo().performClick()
        reveal("kalima_tayyibah").assertIsDisplayed()
        composeRule.onNodeWithTag("kalima_shahadat").assertDoesNotExist()
        composeRule.onNodeWithTag("kalima_mufassal").assertDoesNotExist()
    }

    @Test fun memorisedToggleUpdatesProgress() {
        launch()
        reveal("kalimas_progress").assertTextEquals("0 of 8 memorised")
        reveal("kalima_memorised_tayyibah").performClick()
        reveal("kalimas_progress").assertTextEquals("1 of 8 memorised")
    }

    @Test fun readerMovesBetweenKalimasAndContinueResumesThere() {
        launch()
        reveal("kalima_tayyibah").performClick()
        composeRule.onNodeWithTag("kalimas_reader").assertIsDisplayed()
        composeRule.onNodeWithTag("kalimas_reader_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("kalimas_reader_page_shahadat").assertExists()
        // Marking from the reader counts towards the list's progress.
        composeRule.onNodeWithTag("kalimas_reader_memorised").performClick()
        composeRule.onNodeWithTag("kalimas_reader_back").performClick()
        reveal("kalimas_progress").assertTextEquals("1 of 8 memorised")
        // "Continue" reopens the Kalima where reading stopped.
        reveal("kalimas_resume").performClick()
        composeRule.onNodeWithTag("kalimas_reader_page_shahadat").assertExists()
    }

    @Test fun memoriseModeRevealsPhrasesAndOffersToMarkMemorised() {
        launch()
        reveal("kalima_tayyibah").performClick()
        composeRule.onNodeWithTag("kalimas_mode_memorise").performClick()
        composeRule.onNodeWithTag("kalima_tayyibah_phrase_0").performScrollTo().performClick()
        composeRule.onNodeWithTag("kalima_tayyibah_phrase_1").performScrollTo().performClick()
        composeRule.onNodeWithTag("kalima_tayyibah_memorise_done").performScrollTo().performClick()
        composeRule.onNodeWithTag("kalimas_reader_back").performClick()
        reveal("kalimas_progress").assertTextEquals("1 of 8 memorised")
    }
}
