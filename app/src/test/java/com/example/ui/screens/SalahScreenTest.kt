package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.prayer.PrayerSettingsStore
import com.example.salah.Madhhab
import com.example.salah.SalahSettingsStore
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class SalahScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test fun dailyPrayerOpensBeginnerGuideAndSchoolPersists() {
        val prefs = PrayerSettingsStore.prefs(ApplicationProvider.getApplicationContext<Context>())
        SalahSettingsStore.save(prefs, Madhhab.SHAFII)
        rule.setContent {
            MyApplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SalahScreen(onBack = {}, onOpenQibla = {})
                }
            }
        }
        rule.onNodeWithTag("salah_screen").assertExists()
        rule.onNodeWithTag("salah_fajr").performClick()
        assertEquals(Madhhab.SHAFII, SalahSettingsStore.load(prefs))
        rule.onNodeWithTag("salah_screen").performScrollToNode(hasTestTag("salah_learn_fard"))
        rule.onNodeWithTag("salah_learn_fard").performClick()
        rule.onNodeWithTag("salah_beginner_switch").assertExists()
        rule.onNodeWithTag("salah_screen").performScrollToNode(hasTestTag("salah_next_step"))
        rule.onNodeWithTag("salah_next_step").performClick()
        rule.onNodeWithTag("salah_screen").performScrollToNode(hasTestTag("salah_4_rakat"))
        rule.onNodeWithTag("salah_4_rakat").performClick()
        rule.onNodeWithTag("salah_next_step").assertExists()
    }
}
