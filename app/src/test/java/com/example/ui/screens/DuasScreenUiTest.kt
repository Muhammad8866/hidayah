package com.example.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.AdhkarDatasetRepository
import com.example.domain.model.TasbeehPreset
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DuasScreenTab
import com.example.viewmodel.MainViewModel
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Screenshots are written only by `recordRoborazziDebug`; plain test runs just assert behaviour. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class DuasScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        viewModel = MainViewModel(ApplicationProvider.getApplicationContext())
    }

    private fun show(language: String = "en") {
        composeRule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme(darkTheme = false) {
                    val state by viewModel.uiState.collectAsState()
                    Surface(color = MaterialTheme.colorScheme.background) {
                        DuasAdhkarScreen(uiState = state, viewModel = viewModel)
                    }
                }
            }
        }
    }

    @Test
    fun `search narrows the dua list and explains an empty result`() {
        show()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/duas_list.png")

        composeRule.onNodeWithTag("dua_search_field").performTextInput("sayyid")
        composeRule.onNodeWithTag("dua_card_dua_sayyidul_istighfar").assertExists()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/duas_search.png")

        composeRule.onNodeWithTag("dua_search_field").performTextClearance()
        composeRule.onNodeWithTag("dua_search_field").performTextInput("zzzz")
        composeRule.onNodeWithTag("dua_empty_state").assertExists()
    }

    @Test
    fun `dua tab opens on occasions and back returns to them`() {
        show()
        composeRule.onNodeWithTag("dua_occasion_grid").assertExists()
        composeRule.onNodeWithTag("dua_occasion_3").performClick()
        composeRule.onNodeWithTag("dua_list_header").assertExists()
        composeRule.runOnIdle {
            assertEquals("Sleep & Awakening", viewModel.uiState.value.selectedDuaCategory)
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/duas_category.png")

        composeRule.onNodeWithTag("dua_list_back").performClick()
        composeRule.onNodeWithTag("dua_occasion_grid").assertExists()
    }

    @Test
    fun `dua sheet shows the source with copy and share actions`() {
        show()
        composeRule.onNodeWithTag("dua_occasion_3").performClick()
        composeRule.onNodeWithTag("dua_card_dua_waking_up").performClick()
        composeRule.onNodeWithTag("dua_detail_sheet").assertExists()
        composeRule.onNodeWithTag("dua_copy_button").assertExists()
        composeRule.onNodeWithTag("dua_share_button").assertExists()
        captureScreenRoboImage("build/outputs/roborazzi/duas_detail_sheet.png")

        composeRule.onNodeWithTag("dua_detail_sheet").performScrollToNode(hasTestTag("dua_next_button"))
        composeRule.onNodeWithTag("dua_next_button").performClick()
        composeRule.onNodeWithTag("dua_previous_button").assertIsEnabled()
    }

    @Test
    fun `adhkar card counts to its target and keeps the tally`() {
        viewModel.resetAdhkar(AdhkarDatasetRepository.MORNING_ADHKAR.map { "morning:${it.id}" })
        viewModel.selectDuasScreenTab(DuasScreenTab.ADHKAR)
        show()
        composeRule.onNodeWithTag("adhkar_morning_chip").performClick()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/adhkar_morning.png")

        // Programmatic lazy-list scrolling (here, the jump to the next dhikr once one is finished)
        // never lets Compose go idle under Robolectric, so the clock is stepped by hand and the
        // first card, which is on screen without scrolling, is the one counted.
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
        repeat(3) {
            composeRule.onNodeWithTag("adhkar_card_dua_muawwidhat").performClick()
            composeRule.mainClock.advanceTimeBy(500)
        }
        assertEquals(3, viewModel.uiState.value.adhkarProgress["morning:dua_muawwidhat"])
        // Finishing a dhikr brings the next one into view, ready for the next tap.
        composeRule.onNodeWithTag("adhkar_card_dua_morning_asbahna").performClick()
        composeRule.mainClock.advanceTimeBy(500)
        assertEquals(1, viewModel.uiState.value.adhkarProgress["morning:dua_morning_asbahna"])
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/adhkar_counted.png")
        // A finished dhikr keeps its tally instead of counting past the target.
        viewModel.countAdhkar("morning:dua_muawwidhat", 3)
        assertEquals(3, viewModel.uiState.value.adhkarProgress["morning:dua_muawwidhat"])
    }

    @Test
    fun `tasbeeh shows the full goal and records a round`() {
        val preset = TasbeehPreset.ALL.first()
        viewModel.setTasbeehPreset(preset.phrase, preset.goal)
        viewModel.selectDuasScreenTab(DuasScreenTab.TASBEEH)
        show()
        repeat(preset.goal) { composeRule.onNodeWithTag("tasbeeh_counter_btn").performClick() }
        composeRule.runOnIdle {
            assertEquals(preset.goal, viewModel.uiState.value.tasbeehCount)
            assertEquals(1, viewModel.uiState.value.tasbeehRounds)
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/tasbeeh.png")

        // Reset asks first, so a stray tap cannot wipe the count.
        composeRule.onNodeWithTag("tasbeeh_reset").performClick()
        composeRule.runOnIdle { assertEquals(1, viewModel.uiState.value.tasbeehRounds) }
        captureScreenRoboImage("build/outputs/roborazzi/tasbeeh_reset_dialog.png")
        composeRule.onNodeWithTag("reset_confirm_button").performClick()
        composeRule.runOnIdle { assertEquals(0, viewModel.uiState.value.tasbeehCount) }
    }

    @Test
    fun `undo takes back a stray tap and the round it finished`() {
        val preset = TasbeehPreset.ALL.first()
        viewModel.setTasbeehPreset(preset.phrase, preset.goal)
        viewModel.selectDuasScreenTab(DuasScreenTab.TASBEEH)
        show()
        repeat(preset.goal) { composeRule.onNodeWithTag("tasbeeh_counter_btn").performClick() }
        composeRule.onNodeWithTag("tasbeeh_undo").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(preset.goal - 1, viewModel.uiState.value.tasbeehCount)
            assertEquals(0, viewModel.uiState.value.tasbeehRounds)
        }
    }

    @Test
    fun `finished tasbeeh round offers the next tasbeeh-e-fatimi dhikr`() {
        val first = TasbeehPreset.ALL[0]
        viewModel.setTasbeehPreset(first.phrase, first.goal)
        viewModel.selectDuasScreenTab(DuasScreenTab.TASBEEH)
        show()
        repeat(first.goal) { composeRule.onNodeWithTag("tasbeeh_counter_btn").performClick() }
        composeRule.onNodeWithTag("tasbeeh_next_preset").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(TasbeehPreset.ALL[1].phrase, viewModel.uiState.value.tasbeehPhrase)
        }
    }

    @Test
    fun `urdu shows translated controls in a right to left layout`() {
        show("ur")
        composeRule.onNodeWithText("دعائیں تلاش کریں", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("تمام دعائیں", useUnmergedTree = true).assertExists()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/duas_list_urdu.png")
    }
}
