package com.example.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BabyNamesScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun genderFilterSearchAndResetWorkTogether() {
        composeRule.setContent { MaterialTheme { BabyNamesScreen(onBack = {}) } }
        composeRule.onNodeWithTag("baby_names_Boys").performClick()
        composeRule.onNodeWithTag("baby_names_search").performTextInput("ayesha")
        composeRule.onNodeWithText("No name found.").assertIsDisplayed()
        composeRule.onNodeWithText("Reset filters").performClick()
        composeRule.onNodeWithTag("baby_names_search").performTextInput("ayesha")
        composeRule.onNodeWithTag("baby_name_Aisha").assertIsDisplayed()
        composeRule.onNodeWithText("Prosperous").assertIsDisplayed()
        composeRule.onNodeWithTag("baby_name_source_GIRL:Aisha").assertDoesNotExist()
    }
}

