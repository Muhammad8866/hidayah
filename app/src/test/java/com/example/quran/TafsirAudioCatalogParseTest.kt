package com.example.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TafsirAudioCatalogParseTest {

    private fun entry(surah: Int, url: String = "https://x/a/tafsir_$surah.mp3") =
        """{"surah":$surah,"title":"Surah $surah","url":"$url","page":"https://x/p/$surah"}"""

    private fun full() = (1..114).joinToString(",", "[", "]") { entry(it) }

    @Test fun `malformed json returns empty list instead of throwing`() {
        assertTrue(TafsirAudioCatalog.parse("{not json").isEmpty())
        assertTrue(TafsirAudioCatalog.parse("").isEmpty())
        assertTrue(TafsirAudioCatalog.parse("""[{"surah":"x"}]""").isEmpty())
    }

    @Test fun `incomplete or duplicate catalog returns empty list`() {
        assertTrue(TafsirAudioCatalog.parse("[${entry(1)}]").isEmpty())
        val dup = (1..114).joinToString(",", "[", "]") { entry(it, "https://x/a/same.mp3") }
        assertTrue(TafsirAudioCatalog.parse(dup).isEmpty())
    }

    @Test fun `valid catalog parses all 114 surahs`() {
        val tracks = TafsirAudioCatalog.parse(full())
        assertEquals(114, tracks.size)
        assertEquals("tafsir_1", tracks.first().id)
        assertEquals("https://x/p/114", tracks.last().sourceUrl)
    }
}
