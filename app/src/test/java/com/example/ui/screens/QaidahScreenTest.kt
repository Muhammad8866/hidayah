package com.example.ui.screens

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.example.quran.WordByWordRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QaidahScreenTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun refsParseOrRejectCleanly() {
        assertEquals(Triple(2, 1, 1), parseQaidahRef("2:1:1"))
        assertNull(parseQaidahRef("2:1"))
        assertNull(parseQaidahRef("a:b:c"))
    }

    @Test
    fun bundledLessonsLoadInOrder() = runTest {
        val lessons = QaidahRepository.lessons(context)
        assertEquals("letters", lessons.first().id)
        assertEquals(29, lessons.first().items.size)
        assertTrue(lessons.all { it.items.isNotEmpty() && it.title.isNotBlank() && it.note.isNotBlank() })
    }

    /** Every word drill must show exactly the Quran word its audio recites. */
    @Test
    fun wordDrillsMatchTheirQuranWord() = runTest {
        QaidahRepository.lessons(context).flatMap { it.items }.filter { it.ref != null }.forEach { item ->
            val (surah, ayah, word) = parseQaidahRef(item.ref!!)!!
            val quranWord = WordByWordRepository.surah(context, surah)[ayah - 1].words[word - 1]
            assertEquals(item.ref, quranWord.arabic, item.arabic)
        }
    }
}
