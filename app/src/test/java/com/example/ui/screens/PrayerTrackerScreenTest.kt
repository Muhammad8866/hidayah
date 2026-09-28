package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.isRtlAppLanguage
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
import java.time.LocalDate

/** Screenshots are written only by `recordRoborazziDebug`; plain test runs just assert the screen works. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class PrayerTrackerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()
    private val prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("daily_tools", Context.MODE_PRIVATE)

    @Before
    fun setUp() {
        prefs.edit().clear().commit()
        val store = PrayerTrackerStore(prefs)
        val today = LocalDate.now()
        (1L..20L).forEach { offset ->
            val date = today.minusDays(offset)
            TRACKED_PRAYERS.forEachIndexed { index, prayer ->
                val mark = when {
                    prayer == "Fajr" && offset % 3 == 0L -> PrayerMark.MISSED
                    index % 2 == 0 -> PrayerMark.JAMAAT
                    else -> PrayerMark.ON_TIME
                }
                store.setMark(date, prayer, mark)
            }
        }
    }

    private fun show(language: String = "en", dark: Boolean = false) {
        composeRule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme(darkTheme = dark) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        PrayerTrackerScreen(onBack = {}, modifier = Modifier)
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `tracker renders and missed prayer feeds qaza`() {
        show()
        composeRule.onNodeWithTag("prayer_tracker_screen").assertIsDisplayed()
        val before = prefs.getInt("qaza_Fajr", 0)
        composeRule.onNodeWithTag("prayer_card_Fajr").assertIsDisplayed()
        composeRule.onNodeWithTag("tracker_day_${LocalDate.now().minusDays(1)}").performClick()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/prayer_tracker_en.png")
        composeRule.onNodeWithTag("prayer_tracker_screen").performScrollToNode(hasTestTag("missed_Isha"))
        composeRule.onNodeWithTag("missed_Isha").performClick()
        assertEquals(1, prefs.getInt("qaza_Isha", 0))
        assertEquals(before, prefs.getInt("qaza_Fajr", 0))
        composeRule.onNodeWithTag("prayer_tracker_screen").performScrollToNode(hasTestTag("qaza_total"))
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/prayer_tracker_en_stats.png")
    }

    @Test
    fun `tracker renders in urdu dark mode`() {
        show(language = "ur", dark = true)
        composeRule.onNodeWithTag("prayer_tracker_screen").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/prayer_tracker_ur_dark.png")
    }
}
