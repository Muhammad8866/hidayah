package com.example.hadith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The libraries and publishers the books are fetched from never reach the screen. */
class ProviderNamesTest {
    @Test fun `an author line that is only a provider becomes blank`() {
        listOf(
            "Maktaba-tul-Madina", "Maktabat-ul-Madina", "Al Madina-tul-Ilmiyah", "Al-Madinatul Ilmiyah",
            "Dawat-e-Islami", "dawat e islami", "Internet Archive", "archive.org", "Raza Academy",
            "alahazrat.net", "i-sunni.blogspot.com", "Markazi Majlis-e-Shoora", "Kanzul Madaris Board",
            "Al-Madinatul Ilmiyah · Dawat-e-Islami"
        ).forEach { assertEquals(it, "", ProviderNames.hideIn(it)) }
    }

    @Test fun `only the provider part of a mixed line is dropped`() {
        assertEquals("Allama Abdul Mustafa Azmi", ProviderNames.hideIn("Allama Abdul Mustafa Azmi · Al-Madinatul Ilmiyah edition"))
        assertEquals("Imam Ahmad Raza Khan", ProviderNames.hideIn("Imam Ahmad Raza Khan; Maktaba tul Madina"))
        assertEquals("Imam Ahmad Raza Khan", ProviderNames.hideIn("Imam Ahmad Raza Khan (Dawat-e-Islami)"))
        assertEquals("Majlis Taukiyat", ProviderNames.hideIn("Majlis Taukiyat Paishkash Majlis Madina-tul-Ilmia"))
        assertEquals(
            "Hazrat Allama Qazi Shams-ud-Deen Ahmed Jonpary",
            ProviderNames.hideIn("Hazrat Allama Qazi Shams-ud-Deen Ahmed Jonpary Paishkash Majlis Madina-tul-Ilmia")
        )
    }

    @Test fun `author names are kept exactly as written`() {
        listOf(
            "Ameer-e-Ahl-e-Sunnat", "Imam-e-Ahl-e-Sunnat Ahmad Raza Khan", "Ala Hazrat", "alahazrat",
            "Imam Muhammad Bin Ismail Bukhari", "Sadr ash-Shariah Mufti Amjad Ali Azmi · d. 1367 AH",
            "Khalifa e Ameer e Ahle Sunnat Maulana Abu Usaid Haji Ubaid Raza Attari Madani",
            "Hazrat Allama Syed Ahmed Saeed Kazmi علیہ رحمۃُ اللہ القوی", ""
        ).forEach { assertEquals(it, it, ProviderNames.hideIn(it)) }
    }

    @Test fun `every provider writer in the library's author list is hidden`() {
        assertTrue(ProviderNames.isProvider(DawateIslamiIndex.writerName(4)))
        assertTrue(ProviderNames.isProvider(DawateIslamiIndex.writerName(5)))
        assertTrue(ProviderNames.isProvider(DawateIslamiIndex.writerName(17)))
        assertFalse(ProviderNames.isProvider(DawateIslamiIndex.writerName(3)))
        assertEquals("", ProviderNames.hideIn(DawateIslamiIndex.writerName(4)))
    }

    @Test fun `no book card author line in the catalogues names a provider`() {
        IslamicBooksCatalog.works.forEach { work ->
            assertFalse(work.author, ProviderNames.isProvider(work.author))
        }
    }
}
