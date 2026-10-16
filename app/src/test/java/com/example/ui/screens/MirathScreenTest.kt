package com.example.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.AnnotatedString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MirathScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun successfulCalculationAndEditingInvalidatesResult() {
        compose.setContent { MaterialTheme { MirathScreen(onBack = {}) } }
        fun scrollTo(tag: String) = compose.onNodeWithTag("mirath_screen").performScrollToNode(hasTestTag(tag))
        scrollTo("mirath_estate")
        compose.onNodeWithTag("mirath_estate").performTextInput("120000")
        scrollTo("mirath_Sons_increase")
        compose.onNodeWithTag("mirath_Sons_increase").performClick()
        compose.onNodeWithTag("mirath_screen").performScrollToNode(hasText("I have checked all surviving heirs and no special circumstances apply"))
        compose.onNodeWithText("I have checked all surviving heirs and no special circumstances apply").performClick()
        scrollTo("mirath_calculate")
        compose.onNodeWithTag("mirath_calculate").performClick()
        scrollTo("mirath_share")
        compose.onNodeWithTag("mirath_share").performScrollTo().assertIsDisplayed()
        scrollTo("mirath_estate")
        compose.onNodeWithTag("mirath_estate").performTextReplacement("240000")
        compose.onNodeWithTag("mirath_result").assertDoesNotExist()
        scrollTo("mirath_calculate")
        compose.onNodeWithText("Details changed. Calculate again to update your shares.").assertIsDisplayed()
    }

    @Test fun invalidEstateShowsErrorAndResetClearsForm() {
        compose.setContent { MaterialTheme { DailyToolScreen(DailyTool.MIRATH, onBack = {}) } }
        compose.onNodeWithTag("mirath_screen").assertIsDisplayed()
        compose.onNodeWithTag("mirath_screen").performScrollToNode(hasTestTag("mirath_estate"))
        compose.onNodeWithTag("mirath_estate").performTextInput("-1")
        compose.onNodeWithTag("mirath_screen").performScrollToNode(hasTestTag("mirath_calculate"))
        compose.onNodeWithTag("mirath_calculate").performClick()
        compose.onNodeWithTag("mirath_screen").performScrollToNode(hasTestTag("mirath_result"))
        compose.onNodeWithTag("mirath_error").assertTextEquals("Enter valid non-negative amounts with up to 2 decimal places (no commas).").assertIsDisplayed()
        compose.onNodeWithTag("mirath_screen").performScrollToNode(hasText("Reset"))
        compose.onNodeWithText("Reset").performClick()
        compose.onNodeWithTag("mirath_screen").performScrollToNode(hasTestTag("mirath_estate"))
        compose.onNodeWithTag("mirath_estate").assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")))
        compose.onNodeWithTag("mirath_result").assertDoesNotExist()
    }
}
