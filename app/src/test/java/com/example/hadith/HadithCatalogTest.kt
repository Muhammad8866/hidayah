package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HadithCatalogTest {
    @Test fun `catalog contains all six major collections and only whole sets`() {
        val required = setOf("bukhari", "muslim", "abu_dawud", "tirmidhi", "nasai", "ibn_majah")
        assertTrue(HadithCatalog.books.map { it.collectionId }.toSet().containsAll(required))
        HadithCatalog.books.groupBy { it.collectionId to it.languageCode }.values.forEach { volumes ->
            assertEquals((1..volumes.first().volumeCount).toList(), volumes.map { it.volume }.sorted())
        }
        assertEquals(70, HadithCatalog.books.size)
        assertEquals(34, HadithCatalog.books.count { it.languageCode == "hi" })
        assertEquals(7, HadithCatalog.books.count { it.languageCode == "en" })
        assertEquals(92, HadithCatalog.books.single { it.id == "e-bukhari" }.chapters.size)
    }

    @Test fun `collection and volume sorting covers every PDF`() {
        val sorted = HadithCatalog.visibleBooks(
            HadithCatalog.books, "", HadithCatalog.ALL_LANGUAGES, HadithSort.COLLECTION
        )
        assertEquals(HadithCatalog.books.map { it.id }, sorted.map { it.id })
        assertEquals(listOf(1, 1, 1), HadithCatalog.visibleBooks(
            HadithCatalog.books, "", HadithCatalog.ALL_LANGUAGES, HadithSort.VOLUME_ASC
        ).take(3).map { it.volume })
    }

    @Test fun `search accepts Arabic and Urdu collection names`() {
        val arabic = HadithCatalog.visibleBooks(HadithCatalog.books, "صحيح البخاري", "ar", HadithSort.COLLECTION)
        assertTrue(arabic.isNotEmpty())
        assertTrue(arabic.all { it.collectionId == "bukhari" })

        val urdu = HadithCatalog.visibleBooks(HadithCatalog.books, "صحیح بخاری", "hi", HadithSort.COLLECTION)
        assertTrue(urdu.isNotEmpty())
        assertTrue(urdu.all { it.collectionId == "bukhari" })
    }

    @Test fun `every item is an unchanged official-source PDF URL`() {
        HadithCatalog.books.forEach {
            assertTrue(it.mirrorUrl.startsWith("https://github.com/Muhammad8866/deen-companion-hadith-pdfs/releases/download/hadith-pdfs-v1/"))
            assertTrue(it.downloadUrls.size >= 2)
            assertTrue(it.downloadUrl.startsWith("https://data2.dawateislami.net/") ||
                it.downloadUrl.startsWith("https://drive.usercontent.google.com/") ||
                it.downloadUrl.startsWith("https://www.emaanlibrary.com/") ||
                it.downloadUrl.startsWith("https://archive.org/"))
            assertTrue(!it.downloadUrl.contains("translate", ignoreCase = true))
        }
    }
}
