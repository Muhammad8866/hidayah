package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.AdhkarDatasetRepository
import com.example.domain.model.DuaDatasetRepository
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdhkarAudioTest {
    @get:Rule
    val compose = createComposeRule()

    @After
    fun reset() {
        AdhkarAudioPlayer.release()
    }

    @Test
    fun everyAdvertisedSessionRecordingIsBundledAndNonEmpty() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sessions = AdhkarDatasetRepository.MORNING_ADHKAR + AdhkarDatasetRepository.EVENING_ADHKAR
        sessions.distinctBy { it.id }.filter { AdhkarAudioCatalog.has(it.id) }.forEach { dua ->
            val asset = requireNotNull(AdhkarAudioCatalog.uriFor(dua.id)).removePrefix("asset:///")
            context.assets.open(asset).use {
                assertTrue("Missing audio for ${dua.id}", it.readBytes().size > 1000)
            }
        }
    }

    @Test
    fun eveningUsesItsOwnWordsRatherThanMorningRecordings() {
        listOf("allahumma_bika", "witness", "blessing").forEach { part ->
            val morning = if (part == "allahumma_bika") "dua_allahumma_bika_asbahna" else "dua_morning_$part"
            val evening = if (part == "allahumma_bika") "dua_allahumma_bika_amsayna" else "dua_evening_$part"
            assertTrue(AdhkarAudioCatalog.has(evening))
            assertNotEquals(AdhkarAudioCatalog.uriFor(morning), AdhkarAudioCatalog.uriFor(evening))
        }
        assertTrue(AdhkarAudioCatalog.has("dua_amsayna_fitrah"))
        assertNotEquals(AdhkarAudioCatalog.uriFor("dua_fitratil_islam"), AdhkarAudioCatalog.uriFor("dua_amsayna_fitrah"))
    }

    @Test
    fun failedRecordingShowsRetryInsteadOfSilentlyDisappearing() {
        val dua = requireNotNull(DuaDatasetRepository.byId("dua_sayyidul_istighfar"))
        AdhkarAudioPlayer.failedId.value = dua.id
        compose.setContent { MaterialTheme { AdhkarAudioControls(dua) } }
        compose.onNodeWithTag("adhkar_audio_${dua.id}").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertIsDisplayed()
        compose.onNodeWithText("Audio could not play. Try again.").assertIsDisplayed()
    }

    @Test
    fun unrecordedWordingStillHasAnExplicitDeviceVoiceControl() {
        val dua = requireNotNull(DuaDatasetRepository.byId("dua_morning_asbahna"))
        assertFalse(AdhkarAudioCatalog.has(dua.id))
        compose.setContent {
            val speech = rememberDuaSpeech()
            CompositionLocalProvider(LocalAdhkarSpeech provides speech) {
                MaterialTheme { AdhkarAudioControls(dua) }
            }
        }
        compose.onNodeWithTag("dua_audio_${dua.id}").assertIsDisplayed()
        compose.onNodeWithText("Arabic device voice").assertIsDisplayed()
    }
}
