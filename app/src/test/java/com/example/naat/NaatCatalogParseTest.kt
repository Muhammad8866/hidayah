package com.example.naat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Remote catalog ids become file names, so unsafe ids must never reach [NaatAudioManager]. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NaatCatalogParseTest {

    private fun entry(id: String) =
        """{"id":${org.json.JSONObject.quote(id)},"title":"T","performer":"P","url":"https://archive.org/download/x/$ID_SAFE.mp3"}"""

    private fun catalog(vararg ids: String) = ids.joinToString(",", "[", "]") { entry(it) }

    @Test
    fun pathTraversalIdIsSkippedAndValidEntryKept() {
        val tracks = NaatCatalog.parse(catalog("good_naat-1", "../evil"))
        assertEquals(listOf("good_naat-1"), tracks.map(NaatTrack::id))
    }

    @Test
    fun emptySpacedAndTooLongIdsAreSkipped() {
        val tracks = NaatCatalog.parse(catalog("", "has space", "x".repeat(81), "ok", "a/b", "dir\\file", "Upper"))
        assertEquals(listOf("ok"), tracks.map(NaatTrack::id))
    }

    @Test
    fun eightyCharIdIsKept() {
        val id = "a".repeat(80)
        assertEquals(listOf(id), NaatCatalog.parse(catalog(id)).map(NaatTrack::id))
    }

    @Test
    fun catalogWithOnlyUnsafeIdsIsStillRejected() {
        assertThrows(IllegalArgumentException::class.java) { NaatCatalog.parse(catalog("../a", "..")) }
    }

    @Test
    fun duplicateIdsAreStillRejected() {
        assertThrows(IllegalArgumentException::class.java) { NaatCatalog.parse(catalog("same", "same")) }
    }

    private companion object {
        const val ID_SAFE = "file"
    }
}
