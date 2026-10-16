package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.azan.AzanAlertStore
import com.example.azan.AzanAudioManager
import com.example.azan.AzanCatalog
import com.example.prayer.PrayerSettingsStore
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Screenshots are written only by `recordRoborazziDebug`; plain test runs just assert the screen renders. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AzanScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After fun reset() {
        AzanAudioManager.stop()
        AzanAudioManager.clearAlert(fajr = true)
        AzanAudioManager.clearAlert(fajr = false)
        AzanAlertStore.audioDir(context).deleteRecursively()
    }

    private fun show(language: String = "en") {
        composeRule.setContent {
            val direction = if (isRtlAppLanguage(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalAppLanguage provides language, LocalLayoutDirection provides direction) {
                MyApplicationTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AzanScreen(onBack = {}, modifier = Modifier)
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test fun bundledCatalogCoversTheWorld() {
        val tracks = AzanCatalog.load(context)
        assertTrue(tracks.size > 150)
        assertEquals(tracks.size, tracks.map { it.id }.toSet().size)
        assertTrue(tracks.all { it.url.startsWith("https://archive.org/download/") && it.sizeBytes > 0 })
        val regions = tracks.map { it.region }.toSet()
        assertTrue(regions.containsAll(listOf("Makkah", "Madinah", "Al-Aqsa", "Egypt", "Turkey", "Pakistan", "Indonesia")))
        assertTrue(tracks.any { it.fajr } && tracks.any { !it.fajr })
    }

    @Test fun alertUsesTheChosenAzanOnlyOnceItIsDownloaded() {
        val prefs = PrayerSettingsStore.prefs(context)
        val fajr = AzanCatalog.load(context).first { it.fajr }
        AzanAlertStore.select(prefs, fajr = true, id = fajr.id)
        assertNull(AzanAlertStore.fileFor(context, "Fajr"))

        AzanAlertStore.audioFile(context, fajr.id).apply { parentFile?.mkdirs() }.writeText("audio")
        assertEquals(AzanAlertStore.audioFile(context, fajr.id), AzanAlertStore.fileFor(context, "Fajr"))
        // The Fajr azan is never used for the other prayers.
        assertNull(AzanAlertStore.fileFor(context, "Maghrib"))
        AzanAlertStore.select(prefs, fajr = true, id = null)
    }

    @Test fun deletingTheDownloadReturnsTheAlertToTheDefaultSound() {
        val track = AzanCatalog.load(context).first { !it.fajr }
        AzanAlertStore.audioFile(context, track.id).apply { parentFile?.mkdirs() }.writeText("audio")
        AzanAudioManager.init(context)
        AzanAudioManager.setAsAlert(track)
        assertEquals(track.id, AzanAudioManager.state.value.otherAlertId)

        AzanAudioManager.delete(track)
        assertNull(AzanAudioManager.state.value.otherAlertId)
        assertNull(AzanAlertStore.fileFor(context, "Asr"))
    }

    @Test fun filteringByRegionAndSearching() {
        show()
        composeRule.onNodeWithTag("azan_alert_card").assertIsDisplayed()
        composeRule.onNodeWithTag("azan_filter_madinah").performClick()
        composeRule.onNodeWithTag("azan_search").performTextInput("sunbul")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("azan_athan-sound-f-amr-sonbel").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/azan_madinah_search.png")
    }

    @Test fun urduLayout() {
        show(language = "ur")
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/azan_urdu.png")
    }
}

