package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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

/** Screenshots are written only by `recordRoborazziDebug`; plain test runs just assert the screen works. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ZakatScreenTest {

    @get:Rule
    val composeRule = createComposeRule()
    private val prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("daily_tools", Context.MODE_PRIVATE)

    @Before
    fun setUp() {
        prefs.edit().clear()
            .putString("zakat_currency", "PKR")
            .putString("zakat_unit", MetalUnit.TOLA.name)
            .putString("zakat_gold_weight", "5")
            .putString("zakat_gold_rate", "250000")
            .putString("zakat_silver_rate", "2800")
            .putString("zakat_cash", "200000")
            .putString("zakat_rate_source", "MANUAL")
            .commit()
    }

    private fun show(language: String = "en", dark: Boolean = false) {
        composeRule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme(darkTheme = dark) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        ZakatScreen(onBack = {}, modifier = Modifier)
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `saved figures reach the silver nisab and the lunar year makes zakat due`() {
        show()
        composeRule.onNodeWithTag("zakat_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("zakat_due").assertTextEquals("PKR 0.00")
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/zakat_en.png")
        composeRule.onNodeWithTag("zakat_screen").performScrollToNode(hasTestTag("zakat_hawl"))
        composeRule.onNodeWithTag("zakat_hawl").performClick()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/zakat_en_nisab.png")
        composeRule.onNodeWithTag("zakat_screen").performScrollToNode(hasTestTag("zakat_due"))
        // 5 tola × 250,000 + 200,000 cash = 1,450,000; 2.5% = 36,250.
        composeRule.onNodeWithTag("zakat_due").assertTextEquals("PKR 36,250.00")
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/zakat_en_due.png")
    }

    @Test
    fun `zakat renders in urdu dark mode`() {
        show(language = "ur", dark = true)
        composeRule.onNodeWithTag("zakat_screen").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/zakat_ur_dark.png")
    }
}
