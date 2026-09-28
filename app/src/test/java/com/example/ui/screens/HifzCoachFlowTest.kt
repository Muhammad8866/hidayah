package com.example.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HifzCoachFlowTest {
    @get:Rule val composeRule = createComposeRule()

    private fun startFreshPassage() {
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .getSharedPreferences("hifz_coach", 0).edit().clear().commit()
        composeRule.setContent { MyApplicationTheme { HifzCoachScreen() } }
        composeRule.onNodeWithTag("hifz_start_passage").performScrollTo().performClick()
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithTag("hifz_test_me").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun `practice works without microphone and advances after review`() {
        startFreshPassage()
        composeRule.onNodeWithTag("hifz_test_me").performClick()
        composeRule.onNodeWithTag("hifz_reveal").performClick()
        composeRule.onNodeWithTag("hifz_grade_good").performScrollTo().performClick()
        composeRule.onNodeWithText("Ayah 2 of 5").assertExists()
    }

    @Test
    fun `forgotten ayah comes back once at the end of the session`() {
        startFreshPassage()
        composeRule.onNodeWithTag("hifz_test_me").performClick()
        composeRule.onNodeWithTag("hifz_reveal").performClick()
        composeRule.onNodeWithTag("hifz_grade_forgot").performScrollTo().performClick()
        composeRule.onNodeWithText("Ayah 2 of 6").assertExists()
    }
}
