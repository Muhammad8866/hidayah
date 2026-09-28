package com.example.quran

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HifzQuranTextRepositoryTest {
    @Test
    fun `offline coach covers every ayah in the catalog`() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val chapters = FullQuranCatalog.surahs.map { surah ->
            HifzQuranTextRepository.loadSurah(context, surah.number)
        }
        assertEquals(6236, chapters.sumOf { it.size })
        FullQuranCatalog.surahs.forEachIndexed { index, surah ->
            assertEquals(surah.totalAyat, chapters[index].size)
            assertTrue(chapters[index].all { it.arabicText.isNotBlank() })
        }
    }
}
