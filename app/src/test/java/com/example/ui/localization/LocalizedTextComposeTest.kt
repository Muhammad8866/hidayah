package com.example.ui.localization

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalizedTextComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `changing app language recomposes visible text immediately`() {
        lateinit var language: MutableState<String>
        composeRule.setContent {
            language = remember { mutableStateOf("en") }
            CompositionLocalProvider(LocalAppLanguage provides language.value) {
                MyApplicationTheme { LocalizedText("Settings") }
            }
        }

        composeRule.onNodeWithText("Settings").assertExists()
        composeRule.runOnIdle { language.value = "hi" }
        composeRule.onNodeWithText("सेटिंग्स").assertExists()
        composeRule.runOnIdle { language.value = "ur" }
        composeRule.onNodeWithText("ترتیبات").assertExists()
    }
}
