package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.appLocale
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class TallyCounterScreenTest {
    @get:Rule val composeRule = createComposeRule()
    private val prefs get() = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
    private val original = TallyCounter(id = "first", name = "Daily tasks", count = 25, target = 100, step = 5)

    @Before fun prepare() {
        prefs.edit().clear().commit()
        TallyCounterStore(prefs).save(TallyCollection(listOf(original), original.id))
    }
    private fun show(dark: Boolean = false, language: String = "en") {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides language, LocalAppLocale provides appLocale(language),
                LocalLayoutDirection provides if (language == "ur") LayoutDirection.Rtl else LayoutDirection.Ltr) {
                MyApplicationTheme(darkTheme = dark) {
                    Surface(color = MaterialTheme.colorScheme.background) { TallyCounterScreen(onBack = {}) }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test fun countingUndoAndResetPersistExactValues() {
        show()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/tally_advanced_light.png")
        composeRule.onNodeWithTag("tally_increment").performClick()
        composeRule.onNodeWithTag("tally_count", useUnmergedTree = true).assertTextEquals("30")
        composeRule.onNodeWithTag("tally_undo").performScrollTo().performClick()
        composeRule.onNodeWithTag("tally_count", useUnmergedTree = true).assertTextEquals("25")
        composeRule.onNodeWithTag("tally_reset").performScrollTo().performClick()
        composeRule.onNodeWithTag("tally_confirm").performClick()
        composeRule.onNodeWithTag("tally_undo").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(25L, TallyCounterStore(prefs).load().active.count)
    }

    @Test fun createAndSwitchCountersKeepsTheirCountsSeparate() {
        show()
        composeRule.onNodeWithTag("tally_select").performClick()
        composeRule.onNodeWithTag("tally_new").performClick()
        // Text-field cursor frames should not drive Robolectric's automatic idle clock forever.
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(1000)
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/tally_advanced_editor.png")
        composeRule.onNodeWithTag("tally_name_input").performTextInput("Inventory")
        composeRule.onNodeWithTag("tally_target_input").performTextInput("500")
        composeRule.onNodeWithTag("tally_save").performClick()
        composeRule.mainClock.autoAdvance = true
        composeRule.onNodeWithTag("tally_increment").performClick()
        composeRule.waitForIdle()
        assertEquals(2, TallyCounterStore(prefs).load().counters.size)
        assertEquals(1L, TallyCounterStore(prefs).load().active.count)
        composeRule.onNodeWithTag("tally_select").performClick()
        composeRule.onNodeWithTag("tally_counter_first").performClick()
        composeRule.onNodeWithTag("tally_count", useUnmergedTree = true).assertTextEquals("25")
    }

    @Test
    @Config(qualifiers = "w320dp-h700dp-xhdpi", sdk = [36])
    fun narrowUrduDarkFocusLayout() {
        show(dark = true, language = "ur")
        composeRule.onNodeWithTag("tally_focus").performClick()
        composeRule.onNodeWithTag("tally_increment").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/tally_advanced_urdu_dark.png")
    }
}
