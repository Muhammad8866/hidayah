package com.example.quran

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class QuranReaderPreferencesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val book = QuranPdfCatalog.byId("lines_11")!!

    @Before fun clear() {
        context.getSharedPreferences("quran_reader_choices", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun `bookmarks survive reopening and remain isolated between editions`() {
        val reader = QuranReaderPreferences(context, book)
        reader.toggleBookmark(10)
        reader.toggleFavorite(2)
        reader.saveReadingMode("vertical")
        val reopened = QuranReaderPreferences(context, book)
        assertEquals(setOf(10), reopened.bookmarks())
        assertEquals(setOf(2), reopened.favorites())
        assertEquals("vertical", reopened.readingMode())
        val other = QuranReaderPreferences(context, QuranPdfCatalog.byId("lines_9")!!)
        assertTrue(other.bookmarks().isEmpty())
        assertTrue(other.favorites().isEmpty())
        assertEquals("gallery", other.readingMode())
        reopened.toggleBookmark(10)
        reopened.toggleFavorite(2)
        assertTrue(reader.bookmarks().isEmpty())
        assertTrue(reader.favorites().isEmpty())
    }

    @Test fun `corrupt and out of range saved values cannot navigate outside the mushaf`() {
        context.getSharedPreferences("quran_reader_choices", Context.MODE_PRIVATE).edit()
            .putStringSet("lines_11_pages", setOf("0", "-1", "bad", "9999", "10"))
            .putStringSet("lines_11_surahs", setOf("0", "115", "bad", "114")).commit()
        val reader = QuranReaderPreferences(context, book)
        assertEquals(setOf(10), reader.bookmarks())
        assertEquals(setOf(114), reader.favorites())
    }
}
