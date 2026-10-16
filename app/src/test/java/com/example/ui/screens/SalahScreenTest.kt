package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.prayer.PrayerSettingsStore
import com.example.salah.Madhhab
import com.example.salah.SalahContent
import com.example.salah.SalahSettingsStore
import com.example.salah.Worshipper
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.localization.localizeUiText
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Screenshots are written only by `recordRoborazziDebug`; plain test runs just assert the flows. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class SalahScreenTest {
    @get:Rule val rule = createComposeRule()

    private val prefs get() = PrayerSettingsStore.prefs(ApplicationProvider.getApplicationContext<Context>())

    private fun show(language: String = "en", dark: Boolean = false) {
        rule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme(darkTheme = dark) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        SalahScreen(onBack = {}, onOpenQibla = {})
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun scrollTo(tag: String) = rule.onNodeWithTag("salah_screen").performScrollToNode(hasTestTag(tag))

    private fun openTab(name: String) {
        scrollTo("salah_tab_$name")
        rule.onNodeWithTag("salah_tab_$name").performClick()
        rule.waitForIdle()
    }

    @Test fun dailyPrayerOpensBeginnerGuideAndSchoolPersists() {
        SalahSettingsStore.save(prefs, Madhhab.SHAFII)
        show()
        rule.onNodeWithTag("salah_screen").assertExists()
        rule.onNodeWithTag("salah_fajr").performClick()
        assertEquals(Madhhab.SHAFII, SalahSettingsStore.load(prefs))
        scrollTo("salah_learn_fard")
        rule.onNodeWithTag("salah_learn_fard").performClick()
        rule.onNodeWithTag("salah_beginner_switch").assertExists()
        scrollTo("salah_step_count")
        rule.onNodeWithTag("salah_step_count").assertTextEquals("Step 1 of 15")
        scrollTo("salah_next_step")
        rule.onNodeWithTag("salah_next_step").performClick()
        rule.waitForIdle()
        scrollTo("salah_step_count")
        rule.onNodeWithTag("salah_step_count").assertTextEquals("Step 2 of 15")
        scrollTo("salah_form_fard_4")
        rule.onNodeWithTag("salah_form_fard_4").performClick()
        scrollTo("salah_step_count")
        rule.onNodeWithTag("salah_step_count").assertTextEquals("Step 1 of 24")
    }

    @Test fun witrIsTaughtFromTheIshaCardAndRemembersTheWorshipper() {
        SalahSettingsStore.save(prefs, Madhhab.HANAFI)
        SalahSettingsStore.saveWorshipper(prefs, Worshipper.MAN)
        show("hinglish")
        scrollTo("salah_isha")
        rule.onNodeWithTag("salah_isha").performClick()
        // Units: 4 Sunnah, 4 Fard, 2 Sunnah, 2 Nafl, 3 Witr, 2 Nafl.
        scrollTo("salah_learn_unit_4")
        rule.onNodeWithTag("salah_learn_unit_4").performClick()
        rule.waitForIdle()
        scrollTo("salah_step_count")
        rule.onNodeWithTag("salah_step_count").assertTextEquals("Qadam 1 / 21")
        scrollTo("salah_for_woman")
        rule.onNodeWithTag("salah_for_woman").performClick()
        assertEquals(Worshipper.WOMAN, SalahSettingsStore.loadWorshipper(prefs))
        rule.onNodeWithTag("salah_beginner_switch").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("salah_screen").performScrollToNode(hasText("Dua-e-Qunoot"))
        rule.onRoot().captureRoboImage("build/outputs/roborazzi/salah_witr_hinglish.png")
    }

    @Test fun masailOpenInHindi() {
        SalahSettingsStore.save(prefs, Madhhab.HANAFI)
        show("hi")
        rule.onRoot().captureRoboImage("build/outputs/roborazzi/salah_daily_hi.png")
        openTab("rules")
        scrollTo("salah_topic_wajibat")
        rule.onNodeWithTag("salah_topic_wajibat").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("नमाज़ के वाजिबात").assertExists()
        rule.onRoot().captureRoboImage("build/outputs/roborazzi/salah_wajibat_hi.png")
        rule.onNodeWithTag("salah_topic_wajibat").assertDoesNotExist()
    }

    @Test fun specialPrayerShowsItsDuaInUrdu() {
        SalahSettingsStore.save(prefs, Madhhab.HANAFI)
        show("ur", dark = true)
        openTab("special")
        scrollTo("salah_topic_janazah")
        rule.onNodeWithTag("salah_topic_janazah").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("نمازِ جنازہ").assertExists()
        rule.onNodeWithTag("salah_screen").performScrollToNode(hasText("جنازے کی دعا — بالغ"))
        rule.onRoot().captureRoboImage("build/outputs/roborazzi/salah_janazah_ur_dark.png")
    }

    @Test fun otherLanguagesKeepLocalizedControlsAndWholeEnglishContent() {
        SalahSettingsStore.save(prefs, Madhhab.HANAFI)
        show("gu")
        rule.onNodeWithTag("salah_madhhab").assertTextEquals(localizeUiText("Change", "gu"))
        assertNotEquals("Change", localizeUiText("Change", "gu"))
        // Religious copy is never translated word by word.
        rule.onNodeWithText(SalahContent.Notes.DAILY.en).assertExists()
        openTab("guide")
        rule.onNodeWithTag("salah_form_witr_3").assertExists()
        rule.onRoot().captureRoboImage("build/outputs/roborazzi/salah_guide_controls_gu.png")
    }

    @Test fun recitationsAreGroupedAndStepsRender() {
        SalahSettingsStore.save(prefs, Madhhab.HANAFI)
        SalahSettingsStore.saveWorshipper(prefs, Worshipper.MAN)
        show()
        openTab("duas")
        rule.onNodeWithText("Inside the prayer").assertExists()
        rule.onNodeWithTag("salah_screen").performScrollToNode(hasText("Thana (opening praise)"))
        rule.onRoot().captureRoboImage("build/outputs/roborazzi/salah_recitations_en.png")
        rule.onNodeWithTag("salah_screen").performScrollToNode(hasText("Dua-e-Qunut"))
        openTab("guide")
        repeat(4) {
            scrollTo("salah_next_step")
            rule.onNodeWithTag("salah_next_step").performClick()
            rule.waitForIdle()
        }
        scrollTo("salah_step_count")
        rule.onNodeWithTag("salah_step_count").assertTextEquals("Step 5 of 15")
        rule.onRoot().captureRoboImage("build/outputs/roborazzi/salah_guide_en.png")
    }
}
