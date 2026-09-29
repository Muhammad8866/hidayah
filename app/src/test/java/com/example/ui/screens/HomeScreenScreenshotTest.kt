package com.example.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.theme.MyApplicationTheme
import com.example.domain.model.PredefinedLocations
import com.example.prayer.PrayerSettingsStore
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
class HomeScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun showHome(language: String = "en", dark: Boolean = false) {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        PrayerSettingsStore.saveCity(PrayerSettingsStore.prefs(app), PredefinedLocations.DEFAULT_CITY)
        val viewModel = MainViewModel(app)
        composeRule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme(darkTheme = dark) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        val uiState by viewModel.uiState.collectAsState()
                        HomeScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateToQuran = {},
                            onNavigateToDuas = {},
                            onNavigateToQibla = {},
                            onOpenPrayerTransparency = {}
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun homeLight() {
        showHome()
        composeRule.onNodeWithTag("hero_prayer_card").assertExists()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/home_light.png")
    }

    @Test
    fun homeDark() {
        showHome(dark = true)
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/home_dark.png")
    }

    @Test
    fun homeUrdu() {
        showHome(language = "ur")
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/home_urdu.png")
    }

    @Test
    fun quickActionsRevealEveryTool() {
        showHome()
        composeRule.onNodeWithTag("action_adhkar").assertExists()
        composeRule.onNodeWithTag("quick_actions_toggle").performClick()
        composeRule.onNodeWithTag("home_screen_scroll").performScrollToNode(hasTestTag("action_zakat"))
        composeRule.onNodeWithTag("action_zakat").assertExists()
    }

    @Test
    fun moreScreen() {
        composeRule.setContent {
            MyApplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MoreScreen({}, {}, {}, {}, {}, {})
                }
            }
        }
        composeRule.onNodeWithTag("more_screen").assertExists()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/more.png")
    }
}
