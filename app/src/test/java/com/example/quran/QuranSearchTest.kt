package com.example.quran

import com.example.domain.model.Ayah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The one surah search behind every Quran surah list (audio, tafsir audio, word learning, PDF
 * mushaf, translation picker, Hifz picker) and the ayah text search, on the JVM.
 */
class QuranSearchTest {
    private val all = FullQuranCatalog.surahs
    private val english = QuranSearch.surahRanker(all) { it }
    private fun first(query: String) = english.search(query).firstOrNull()?.number
    private fun numbers(query: String) = english.search(query).map { it.number }

    // region Surahs: normal

    @Test fun `a blank query lists every surah in order`() {
        assertEquals(all, english.search(""))
        assertEquals(all, english.search("   "))
    }

    @Test fun `an exact name finds its surah first`() {
        assertEquals(18, first("Al-Kahf"))
        assertEquals(36, first("Ya-Sin"))
        assertEquals(67, first("al mulk"))
        assertEquals(1, first("AL-FATIHAH"))
    }

    @Test fun `common spellings, joined or split, find the same surah`() {
        assertEquals(36, first("yasin"))
        assertEquals(36, first("yaseen"))
        assertEquals(36, first("ya sin"))
        assertEquals(2, first("baqra"))
        assertEquals(2, first("baqarah"))
        assertEquals(55, first("rehman"))
        assertEquals(56, first("waqia"))
        assertEquals(18, first("kahaf"))
        assertEquals(112, first("ikhlaas"))
    }

    @Test fun `a small typo still finds the surah`() {
        assertEquals(1, first("fatiha"))
        assertEquals(1, first("fateha"))
        assertEquals(1, first("fatihha"))
    }

    @Test fun `typing surah or surat before the name still finds it`() {
        assertEquals(36, first("surah yasin"))
        assertEquals(18, first("surat al kahf"))
        assertEquals(18, first("sura kahf"))
    }

    @Test fun `a meaning finds the surah`() {
        assertEquals(2, first("the cow"))
        assertEquals(18, first("cave"))
        assertEquals(97, first("the power"))
    }

    @Test fun `a well known other name finds the surah`() {
        assertEquals(67, first("tabarak"))
        assertEquals(17, first("bani israil"))
        assertEquals(17, first("bani israel"))
        assertEquals(94, first("alam nashrah"))
        assertEquals(111, first("lahab"))
        assertEquals(78, first("amma"))
    }

    @Test fun `a half typed name already lists the surah`() {
        assertTrue(67 in numbers("mul"))
        assertEquals(112, first("ikhl"))
    }

    @Test fun `the Arabic name finds the surah, with or without the article`() {
        assertEquals(18, first("الكهف"))
        assertEquals(18, first("كهف"))
        assertEquals(1, first("الفاتحة"))
    }

    @Test fun `Urdu spelling of the Arabic name finds the surah`() {
        assertEquals(36, first("یس"))
        assertEquals(36, first("سورہ یس"))
        assertEquals(1, first("سورۃ الفاتحہ"))
    }

    @Test fun `a name typed in Hindi script finds the surah`() {
        assertEquals(36, first("यासीन"))
        assertEquals(36, first("सूरह यासीन"))
    }

    @Test fun `a localized name or meaning finds the surah`() {
        val turkish = QuranSearch.surahRanker(all) { if (it == "The Cave") "Mağara" else it }
        assertEquals(18, turkish.search("magara").first().number)
        assertEquals(18, turkish.search("MAĞARA").first().number)
    }

    @Test fun `a clear match lists only that surah, with no look-alikes`() {
        assertEquals(listOf(36), numbers("yaseen"))
        assertEquals(listOf(67), numbers("tabarak"))
        assertEquals(listOf(2), numbers("the cow"))
        assertEquals(listOf(111), numbers("lahab"))
    }

    @Test fun `every word typed has to match`() {
        assertEquals(emptyList<Int>(), numbers("kahf zzzqqq"))
        assertEquals(emptyList<Int>(), numbers("zzzqqq"))
    }

    // endregion

    // region Surahs: numbers

    @Test fun `a number finds that surah first, then surahs whose number starts with it`() {
        assertEquals(listOf(2) + (20..29), numbers("2"))
        assertEquals(listOf(36), numbers("36"))
        assertEquals(listOf(1) + (10..19) + (100..114), numbers("1"))
    }

    @Test fun `a number in Arabic or Hindi digits finds the same surah`() {
        assertEquals(36, first("٣٦"))
        assertEquals(36, first("३६"))
        assertEquals(114, first("١١٤"))
    }

    @Test fun `a number that is no surah finds nothing`() {
        assertEquals(emptyList<Int>(), numbers("999"))
        assertEquals(emptyList<Int>(), numbers("0"))
    }

    // endregion

    // region Surahs: bad input and caching

    @Test fun `emoji, punctuation and control characters keep the full list`() {
        assertEquals(all, english.search("💥"))
        assertEquals(all, english.search("-- ,. ؟"))
        assertEquals(all, english.search("\u0000"))
    }

    @Test fun `a huge pasted text does not break the search`() {
        assertEquals(18, first("kahf ".repeat(10_000)))
    }

    @Test fun `the app search is built once per language and reused`() {
        val once = QuranSearch.surahs("kahf", "en")
        assertEquals(18, once.first().number)
        assertSame(once, QuranSearch.surahs("kahf", "en"))
        assertEquals(all, QuranSearch.surahs("", "en"))
    }

    // endregion

    // region Ayahs

    private val ayahs = QuranDatasetRepository.AL_FATIHAH_AYAT + QuranDatasetRepository.AL_BAQARAH_KEY_AYAT
    private val ayahSearch = QuranSearch.AyahSearch(ayahs)
    private fun refs(query: String) = ayahSearch.search(query).map { "${it.surahNumber}:${it.ayahNumber}" }

    @Test fun `a blank or unsearchable query finds no ayah`() {
        assertEquals(emptyList<Ayah>(), ayahSearch.search(""))
        assertEquals(emptyList<Ayah>(), ayahSearch.search("   "))
        assertEquals(emptyList<Ayah>(), ayahSearch.search("💥!!"))
    }

    @Test fun `an ayah reference finds that ayah first`() {
        assertEquals("2:255", refs("2:255").first())
        assertEquals("2:255", refs("2 255").first())
        assertEquals("2:255", refs("2.255").first())
        assertEquals("2:255", refs("٢:٢٥٥").first())
        assertEquals("1:1", refs(" 1:1 ").first())
    }

    @Test fun `a reference to an ayah that is not there finds nothing`() {
        assertEquals(emptyList<String>(), refs("2:999"))
        assertEquals(emptyList<String>(), refs("999:999"))
    }

    @Test fun `an English word in the translation finds the ayah`() {
        assertEquals("2:153", refs("patience").first())
        assertEquals("2:255", refs("sleep").first())
        assertTrue(refs("merciful").containsAll(listOf("1:1", "1:3")))
        assertTrue("1:1" in refs("mercifull"))
    }

    @Test fun `Arabic text is found without vowel marks and with or without the article`() {
        assertEquals("1:2", refs("الحمد").first())
        assertEquals("1:2", refs("حمد").first())
        assertEquals("1:2", refs("الحمد لله").first())
    }

    @Test fun `Urdu translation and transliteration find the ayah`() {
        assertTrue("1:1" in refs("مہربان"))
        assertEquals("1:1", refs("bismillah").first())
        assertTrue("2:82" in refs("jannat"))
    }

    @Test fun `a clear word lists only the ayat that hold it`() {
        assertEquals(listOf("2:153"), refs("patience"))
        assertEquals(listOf("2:255"), refs("sleep"))
        assertEquals(listOf("2:255"), refs("2:255"))
    }

    @Test fun `every word has to match in an ayah`() {
        assertFalse("2:153" in refs("patience zzzqqq"))
    }

    @Test fun `a huge pasted text does not break the ayah search`() {
        assertEquals("2:153", refs("patience ".repeat(5_000)).first())
    }

    @Test fun `ayah references are read in any digits, only in range`() {
        assertEquals(2 to 255, QuranSearch.ayahReference("2:255"))
        assertEquals(2 to 255, QuranSearch.ayahReference("٢ ٢٥٥"))
        assertEquals(36 to 1, QuranSearch.ayahReference("३६/१"))
        assertEquals(null, QuranSearch.ayahReference("115:1"))
        assertEquals(null, QuranSearch.ayahReference("2:0"))
        assertEquals(null, QuranSearch.ayahReference("2:255:1"))
        assertEquals(null, QuranSearch.ayahReference("kahf 10"))
        assertEquals(null, QuranSearch.ayahReference(""))
    }

    @Test fun `the repository search uses the same ayah search`() {
        assertEquals("2:255", QuranDatasetRepository.searchQuran("2:255").first().let { "${it.surahNumber}:${it.ayahNumber}" })
        assertEquals(emptyList<Ayah>(), QuranDatasetRepository.searchQuran(""))
        assertEquals(emptyList<Ayah>(), QuranDatasetRepository.searchQuran("   "))
    }

    // endregion
}
