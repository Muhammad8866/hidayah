package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
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
class HajjUmrahScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before fun clearSavedProgress() {
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("daily_tools", Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun show(language: String = "en", dark: Boolean = false) {
        composeRule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme(darkTheme = dark) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        DailyToolScreen(DailyTool.HAJJ_UMRAH, onBack = {}, modifier = Modifier)
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test fun tawafCounterAdvancesAndTicksTheStep() {
        show()
        composeRule.onNodeWithTag("hajj_done_ihram").performScrollTo().performClick()
        composeRule.onNodeWithTag("hajj_progress").assertTextEquals("⁦1 / 5⁩ steps done")
        // Ticking Ihram opens Tawaf, which carries the round counter.
        repeat(3) { composeRule.onNodeWithTag("hajj_counter_add").performScrollTo().performClick() }
        composeRule.onNodeWithTag("hajj_counter_value").assertTextEquals("3")
        composeRule.onNodeWithTag("hajj_counter_hint").assertTextEquals("Walk at a normal pace")
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/hajj_umrah_counter.png")
    }

    @Test fun rendersEachTab() {
        show()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/hajj_umrah_tab.png")
        composeRule.onNodeWithTag("hajj_tab_hajj").performClick()
        composeRule.onNodeWithTag("hajj_type_qiran").performClick()
        composeRule.onNodeWithTag("hajj_step_q_ihram").assertExists()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/hajj_hajj_tab.png")
        composeRule.onNodeWithTag("hajj_tab_essentials").performClick()
        composeRule.onNodeWithTag("hajj_prep").assertExists()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/hajj_essentials_tab.png")
    }

    @Test fun rendersInUrduDark() {
        show(language = "ur", dark = true)
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/hajj_umrah_ur_dark.png")
    }
}
