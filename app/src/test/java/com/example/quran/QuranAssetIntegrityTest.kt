package com.example.quran

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuranAssetIntegrityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `bundled Mushaf checksum and complete surah index are valid`() = runBlocking {
        val bytes = context.assets.open("taj_13_line_pages.json").use { it.readBytes() }
        RemoteQuranRepository.verifyTajAsset(bytes)

        val firstPage = RemoteQuranRepository.loadMushafPage(context, 1)
        val surahPages = RemoteQuranRepository.loadSurahStartPages(context)

        assertTrue(firstPage.isNotEmpty())
        assertTrue(firstPage.any { it.type == "ayah" && it.text.isNotBlank() })
        assertEquals((1..114).toSet(), surahPages.keys)
        assertEquals(1, surahPages.getValue(1))
    }

    @Test
    fun `modified Mushaf asset fails closed`() {
        val bytes = context.assets.open("taj_13_line_pages.json").use { it.readBytes() }
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()

        assertThrows(IllegalStateException::class.java) {
            RemoteQuranRepository.verifyTajAsset(bytes)
        }
    }
}
