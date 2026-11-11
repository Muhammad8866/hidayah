package com.example.hadith

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDStream
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.random.Random

/**
 * Crashlytics issue 7f1b7f30 (OutOfMemoryError on opening a freshly downloaded 200+ MB book):
 * reading the bookmarks used pdfbox's unlimited in-memory buffering, which copies every stream of
 * the file onto the Java heap. The outline must be read with a few MB of heap and a scratch file.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PdfOutlineTest {
    @get:Rule val temp = TemporaryFolder()

    private val chapters = listOf("Kitab al-Iman", "Kitab al-Ilm", "Kitab at-Taharah")

    /** A three-page PDF with one bookmark per chapter and a stream bigger than the heap budget. */
    private fun bookWithLargeStream(): File {
        val file = temp.newFile("book.pdf")
        PDDocument().use { doc ->
            repeat(chapters.size) { doc.addPage(PDPage()) }
            // Spaces are an empty but valid content stream; 12 MB is over the 8 MB heap budget.
            val large = ByteArray(12 shl 20) { ' '.code.toByte() }
            doc.getPage(0).setContents(PDStream(doc, ByteArrayInputStream(large)))
            val outline = PDDocumentOutline()
            doc.documentCatalog.documentOutline = outline
            chapters.forEachIndexed { index, title ->
                outline.addLast(PDOutlineItem().apply {
                    this.title = title
                    destination = PDPageFitDestination().apply { page = doc.getPage(index) }
                })
            }
            doc.save(file)
        }
        return file
    }

    private val expected get() = chapters.mapIndexed { index, title -> HadithChapter(title, index + 1) }

    @Test fun `heap use is capped and the rest goes to the scratch folder`() {
        val scratch = temp.newFolder("scratch")
        val setting = PdfOutline.memorySetting(scratch)
        assertTrue(setting.useMainMemory())
        assertTrue(setting.useTempFile())
        assertTrue(setting.isMainMemoryRestricted)
        assertTrue(setting.maxMainMemoryBytes in 1..(8L shl 20))
        assertEquals(scratch, setting.tempDir)
    }

    @Test fun `outline of a book with a stream over the budget is read and the scratch file removed`() {
        val book = bookWithLargeStream()
        val scratch = temp.newFolder("scratch")
        assertEquals(expected, PdfOutline.chapters(book, scratch))
        assertTrue("scratch files left: ${scratch.list()?.toList()}", scratch.list().isNullOrEmpty())
    }

    @Test fun `missing scratch folder is created`() {
        val book = bookWithLargeStream()
        val scratch = File(temp.root, "cache/pdfbox_scratch")
        assertEquals(expected, PdfOutline.chapters(book, scratch))
        assertTrue(scratch.isDirectory)
        assertTrue(scratch.list().isNullOrEmpty())
    }

    @Test fun `scratch left by a killed parse is removed before the next one`() {
        val book = bookWithLargeStream()
        val scratch = temp.newFolder("scratch")
        val stale = File(scratch, "PDFBox123.tmp").apply { writeBytes(ByteArray(1024)) }
        assertEquals(expected, PdfOutline.chapters(book, scratch))
        assertTrue(!stale.exists())
    }

    @Test fun `a scratch folder that cannot be made gives no outline instead of unlimited heap`() {
        val book = bookWithLargeStream()
        // A plain file where the folder should be: mkdirs fails and pdfbox refuses the setting.
        val blocked = temp.newFile("not_a_folder")
        assertEquals(emptyList<HadithChapter>(), PdfOutline.chapters(book, blocked))
        assertTrue(blocked.isFile)
    }

    @Test fun `garbage and missing files give no outline and leave no scratch`() {
        val scratch = temp.newFolder("scratch")
        val garbage = temp.newFile("garbage.pdf").apply { writeBytes(Random(7).nextBytes(64 * 1024)) }
        assertEquals(emptyList<HadithChapter>(), PdfOutline.chapters(garbage, scratch))
        assertEquals(emptyList<HadithChapter>(), PdfOutline.chapters(File(temp.root, "absent.pdf"), scratch))
        assertEquals(emptyList<HadithChapter>(), PdfOutline.chapters(temp.newFile("empty.pdf"), scratch))
        assertTrue(scratch.list().isNullOrEmpty())
    }

    @Test fun `a truncated download does not throw and leaves no scratch`() {
        val book = bookWithLargeStream()
        val truncated = temp.newFile("truncated.pdf").apply { writeBytes(book.readBytes().copyOf((book.length() / 2).toInt())) }
        val scratch = temp.newFolder("scratch")
        // pdfbox may recover some bookmarks from a damaged file; it must never crash or leak scratch.
        PdfOutline.chapters(truncated, scratch)
        assertTrue(scratch.list().isNullOrEmpty())
    }

    /** A valid two-page PDF with no bookmarks at all. */
    private fun bookWithoutBookmarks(): File = temp.newFile("plain.pdf").also { file ->
        PDDocument().use { doc ->
            repeat(2) { doc.addPage(PDPage()) }
            doc.save(file)
        }
    }

    @Test fun `a book with bookmarks reads as its chapters`() {
        assertEquals(expected, PdfOutline.read(bookWithLargeStream(), temp.newFolder("scratch")))
    }

    @Test fun `a book without bookmarks reads as an empty outline, not a failure`() {
        val outline = PdfOutline.read(bookWithoutBookmarks(), temp.newFolder("scratch"))
        assertNotNull(outline)
        assertEquals(emptyList<HadithChapter>(), outline)
    }

    @Test fun `unreadable files read as a failure, not an empty outline`() {
        val scratch = temp.newFolder("scratch")
        val garbage = temp.newFile("garbage.pdf").apply { writeBytes(Random(11).nextBytes(64 * 1024)) }
        assertNull(PdfOutline.read(garbage, scratch))
        assertNull(PdfOutline.read(File(temp.root, "absent.pdf"), scratch))
        assertNull(PdfOutline.read(temp.newFile("empty.pdf"), scratch))
        assertTrue(scratch.list().isNullOrEmpty())
    }

    @Test fun `a scratch folder that cannot be made reads as a failure`() {
        assertNull(PdfOutline.read(bookWithLargeStream(), temp.newFile("not_a_folder")))
    }

    @Test fun `a failed parse is not saved, so the next open tries again`() {
        val garbage = temp.newFile("garbage.pdf").apply { writeBytes(Random(13).nextBytes(64 * 1024)) }
        val sidecar = File(garbage.path + ".outline")
        assertEquals(emptyList<HadithChapter>(), PdfOutline.cached(garbage, sidecar, temp.newFolder("scratch")))
        assertFalse("a failure was cached as an empty outline", sidecar.exists())
    }

    @Test fun `a full disk does not cost the book its bookmarks for good`() {
        val book = bookWithLargeStream()
        val sidecar = File(book.path + ".outline")
        // No room for the scratch file: this open shows no bookmarks and saves nothing...
        assertEquals(emptyList<HadithChapter>(), PdfOutline.cached(book, sidecar, temp.newFile("not_a_folder")))
        assertFalse(sidecar.exists())
        // ...and the next open, with room again, reads them.
        assertEquals(expected, PdfOutline.cached(book, sidecar, temp.newFolder("scratch")))
    }

    @Test fun `a parsed outline is saved and reused without parsing again`() {
        val book = bookWithLargeStream()
        val sidecar = File(book.path + ".outline")
        assertEquals(expected, PdfOutline.cached(book, sidecar, temp.newFolder("scratch")))
        assertTrue(sidecar.isFile)
        // A second parse would fail here (no scratch folder), so the result must come from the sidecar.
        assertEquals(expected, PdfOutline.cached(book, sidecar, temp.newFile("blocked")))
    }

    @Test fun `a book without bookmarks is saved as empty so it is not parsed again`() {
        val book = bookWithoutBookmarks()
        val sidecar = File(book.path + ".outline")
        assertEquals(emptyList<HadithChapter>(), PdfOutline.cached(book, sidecar, temp.newFolder("scratch")))
        assertTrue(sidecar.isFile)
    }

    @Test fun `a saved outline for an older download of a different size is parsed again`() {
        val book = bookWithLargeStream()
        val sidecar = File(book.path + ".outline").apply {
            writeText("""{"size":1,"chapters":[{"title":"Stale","page":1}]}""")
        }
        assertEquals(expected, PdfOutline.cached(book, sidecar, temp.newFolder("scratch")))
    }

    @Test fun `a damaged sidecar is ignored and the book parsed`() {
        val book = bookWithLargeStream()
        val sidecar = File(book.path + ".outline").apply { writeText("{not json") }
        assertEquals(expected, PdfOutline.cached(book, sidecar, temp.newFolder("scratch")))
    }

    @Test fun `page-label bookmarks are still dropped`() {
        val file = temp.newFile("scan.pdf")
        PDDocument().use { doc ->
            repeat(30) { doc.addPage(PDPage()) }
            val outline = PDDocumentOutline()
            doc.documentCatalog.documentOutline = outline
            repeat(30) { index ->
                outline.addLast(PDOutlineItem().apply {
                    title = "SSM4_%04d".format(index + 1)
                    destination = PDPageFitDestination().apply { page = doc.getPage(index) }
                })
            }
            doc.save(file)
        }
        assertEquals(emptyList<HadithChapter>(), PdfOutline.chapters(file, temp.newFolder("scratch")))
    }
}
