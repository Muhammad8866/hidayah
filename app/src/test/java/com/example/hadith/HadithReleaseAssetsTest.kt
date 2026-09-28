package com.example.hadith

import com.tom_roush.pdfbox.pdmodel.PDDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class HadithReleaseAssetsTest {
    @Test fun `all staged release PDFs open and contain pages`() {
        val directory = File("../hadith-release-assets")
        assumeTrue("Release staging directory is not present", directory.isDirectory)
        val files = directory.listFiles { file -> file.extension.equals("pdf", true) }
            ?.sortedBy { it.name }
            .orEmpty()
        assertEquals(70, files.size)
        files.forEach { file ->
            PDDocument.load(file).use { document ->
                assertTrue("${file.name} has no pages", document.numberOfPages > 0)
            }
        }
    }
}
