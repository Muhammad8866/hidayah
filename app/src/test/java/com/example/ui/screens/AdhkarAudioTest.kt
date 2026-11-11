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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            requireNotNull(AdhkarAudioCatalog.urisFor(dua.id)).forEach { uri ->
                assertTrue("Not bundled for ${dua.id}: $uri", uri.startsWith("asset:///"))
                context.assets.open(uri.removePrefix("asset:///")).use {
                    assertTrue("Missing audio for ${dua.id}", it.readBytes().size > 1000)
                }
            }
        }
    }

    @Test
    fun unlicensedHisnRecordingsAreNoLongerBundled() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        listOf("dua_sayyidul_istighfar", "dua_evening_witness", "dua_kalimat_tammat").forEach { id ->
            assertFalse(AdhkarAudioCatalog.has(id))
            assertTrue(runCatching { context.assets.open("adhkar_audio/$id.mp3").close() }.isFailure)
        }
    }

    @Test
    fun remoteMappingKeepsOnlyHttpsHashedLicensedEntries() {
        val hash = "a".repeat(64)
        fun entry(id: String, url: String, sha: String = hash, evidence: String = "https://example.org/licence") =
            """{"duaId":"$id","url":"$url","sha256":"$sha","wordingSource":"w","license":"CC-BY-4.0","licenseEvidenceUrl":"$evidence"}"""
        val parsed = DuaRemoteAudio.parse("""{"recordings":[${listOf(
            entry("ok", "https://example.org/a.mp3"),
            entry("plain_http", "http://example.org/a.mp3"),
            entry("bad_hash", "https://example.org/a.mp3", sha = "123"),
            entry("no_evidence", "https://example.org/a.mp3", evidence = "")
        ).joinToString(",")}]}""")
        assertEquals(setOf("ok"), parsed.keys)
    }

    @Test
    fun bundledRemoteMappingParses() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val json = context.assets.open("dua_audio_remote.json").bufferedReader().use { it.readText() }
        DuaRemoteAudio.parse(json).values.forEach { assertTrue(it.url.startsWith("https://")) }
    }

    @Test
    fun failedRecordingShowsRetryInsteadOfSilentlyDisappearing() {
        val dua = requireNotNull((AdhkarDatasetRepository.MORNING_ADHKAR + AdhkarDatasetRepository.EVENING_ADHKAR).firstOrNull { it.id == "dua_muawwidhat" })
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
