package com.example.hadith

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HadithRemovedEditionsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val saved get() = File(context.filesDir, "hadith_pdfs")
    private val cached get() = File(context.cacheDir, "hadith_pdfs")
    private val prefs get() = context.getSharedPreferences("hadith_pdf_reader", Context.MODE_PRIVATE)

    @Before fun clean() {
        saved.deleteRecursively()
        cached.deleteRecursively()
        File(context.cacheDir, "pdf_ranges").deleteRecursively()
        prefs.edit().clear().commit()
    }

    private fun touch(file: File) = file.apply { parentFile?.mkdirs(); writeText("%PDF-1.4") }

    @Test fun `files and reading state of removed editions are deleted and current books are kept`() {
        val removed = listOf(
            touch(File(saved, "hi_h00-bukhari-1.pdf")), touch(File(saved, "hi_h00-bukhari-1.pdf.verified")),
            touch(File(saved, "hi_h00-bukhari-1.pdf.outline")), touch(File(saved, "hi_h00-bukhari-1.pdf.part")),
            touch(File(cached, "en_e-bukhari.pdf")), touch(File(cached, "en_e-bukhari.pdf.verified")),
            touch(File(context.cacheDir, "pdf_ranges/e-bukhari/0"))
        )
        val kept = listOf(
            touch(File(saved, "ar_00-bukhari-1.pdf")), touch(File(saved, "ar_00-bukhari-1.pdf.verified")),
            touch(File(cached, "ur_di-anwaar-ul-hadees-ur.pdf"))
        )
        prefs.edit()
            .putInt("last_e-bukhari", 40).putLong("opened_e-bukhari", 1L).putString("bookmarks_e-bukhari", "3,9")
            .putInt("last_h00-bukhari-1", 7).putInt("last_00-bukhari-1", 12)
            .commit()

        assertTrue(HadithPdfStore.purgeRemovedEditions(context))

        removed.forEach { assertFalse(it.path, it.exists()) }
        assertFalse(File(context.cacheDir, "pdf_ranges/e-bukhari").exists())
        kept.forEach { assertTrue(it.path, it.exists()) }
        assertFalse(prefs.contains("last_e-bukhari"))
        assertFalse(prefs.contains("opened_e-bukhari"))
        assertFalse(prefs.contains("bookmarks_e-bukhari"))
        assertFalse(prefs.contains("last_h00-bukhari-1"))
        assertEquals(12, prefs.getInt("last_00-bukhari-1", 0))
    }

    @Test fun `cleanup runs once`() {
        assertTrue(HadithPdfStore.purgeRemovedEditions(context))
        val again = touch(File(saved, "en_e-muslim.pdf"))
        assertFalse(HadithPdfStore.purgeRemovedEditions(context))
        assertTrue(again.exists())
    }
}
