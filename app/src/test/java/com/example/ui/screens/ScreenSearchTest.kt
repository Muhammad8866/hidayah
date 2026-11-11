package com.example.ui.screens

import com.example.azan.AzanTrack
import com.example.core.search.AppSearch
import com.example.domain.model.CityLocation
import com.example.domain.model.PredefinedLocations
import com.example.hadith.HadithCatalog
import com.example.naat.NaatTrack
import com.example.viewmodel.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The search boxes of the Azan, Naat, city, language, Qaida and Hadith screens, which all run the
 * shared [AppSearch] engine: precise words, spelling and script variants, typos, numbers, ranking.
 */
class ScreenSearchTest {

    // region Azan

    private fun azan(id: String, title: String, muezzin: String, region: String, fajr: Boolean = false) =
        AzanTrack(id, title, muezzin, region, fajr, "https://example.org/$id.mp3", 0, 0)

    private val azans = listOf(
        azan("makkah-1", "Makkah Azan", "Sheikh Ali Mullah", "Makkah"),
        azan("sonbel", "Madinah Fajr Azan", "Sheikh Omar Sunbul", "Madinah", fajr = true),
        azan("cairo", "Egyptian Azan", "Sheikh Mishary Alafasy", "Egypt"),
        azan("istanbul", "Turkish Azan", "Hafiz Mustafa", "Turkey")
    )
    private val azanSearch = AppSearch.Ranker(azans, ::azanSearchFields)
    private fun azanIds(query: String) = azanSearch.search(query).map { it.id }

    @Test fun `azan is found by title, muezzin or place, in any order and case`() {
        assertEquals("sonbel", azanIds("sunbul").first())
        assertEquals("sonbel", azanIds("SUNBUL madinah").first())
        assertEquals("cairo", azanIds("egypt").first())
        assertEquals("istanbul", azanIds("mustafa turkey").first())
    }

    @Test fun `azan search forgives spelling and a half typed word`() {
        assertEquals("sonbel", azanIds("sonbol").first())
        assertEquals("makkah-1", azanIds("makka").first())
        assertEquals("cairo", azanIds("alafas").first())
    }

    @Test fun `a Fajr azan is found by the word fajr`() {
        assertEquals(listOf("sonbel"), azanIds("fajr"))
    }

    @Test fun `azan search drops what does not match and keeps the list for a blank query`() {
        assertEquals(emptyList<String>(), azanIds("zzqqxx"))
        assertEquals(azans.map { it.id }, azanIds("   "))
        assertEquals(azans.map { it.id }, azanIds("🙂"))
    }

    // endregion

    // region Naat

    private fun naat(id: String, title: String, performer: String = "Hafiz Muhammad Tahir Qadri", category: String = "Naat") =
        NaatTrack(id, title, performer, category, "https://example.org/$id.mp3")

    private val naats = listOf(
        naat("tahir-madinay-ka-safar-hai", "Madinay Ka Safar Hai"),
        naat("tahir-mustafa-jaan-e-rehmat", "Mustafa Jaan-e-Rehmat Pe Lakhon Salam", category = "Salam"),
        naat("tahir-tajdar-e-haram", "Tajdar-e-Haram"),
        naat("owais-aahista-chal", "Aahista Chal", performer = "Muhammad Owais Raza Qadri")
    )
    private fun naatIds(query: String) = AppSearch.Ranker(naats, ::naatSearchFields).search(query).map { it.id }

    @Test fun `naat is found in any Roman Urdu spelling`() {
        assertEquals("tahir-madinay-ka-safar-hai", naatIds("medine").first())
        assertEquals("tahir-madinay-ka-safar-hai", naatIds("madina ka safar").first())
        assertEquals("tahir-mustafa-jaan-e-rehmat", naatIds("mustapha jane rehmat").first())
        assertEquals("tahir-tajdar-e-haram", naatIds("tajdare haram").first())
    }

    @Test fun `naat is found by reciter or category, and a typo still matches`() {
        assertEquals("owais-aahista-chal", naatIds("owais").first())
        assertEquals("tahir-mustafa-jaan-e-rehmat", naatIds("salam").first())
        assertEquals("owais-aahista-chal", naatIds("ahista").first())
    }

    @Test fun `naat search drops what does not match`() {
        assertEquals(emptyList<String>(), naatIds("zzqqxx"))
        assertEquals(naats.map { it.id }, naatIds(""))
    }

    @Test fun `the category name in the app language is searchable too`() {
        val urdu = mapOf("Salam" to "سلام")
        val found = AppSearch.Ranker(naats) { naatSearchFields(it) { text -> urdu[text] ?: text } }.search("سلام")
        assertEquals("tahir-mustafa-jaan-e-rehmat", found.first().id)
    }

    // endregion

    // region City

    private val cities = citySearch(PredefinedLocations.CITIES)
    private fun cityNames(query: String) = cities.search(query).map { it.name }

    @Test fun `a city is found by name, state or country`() {
        assertEquals("Lucknow", cityNames("lucknow").first())
        assertEquals("Lucknow", cityNames("uttar pradesh").first())
        assertEquals(listOf("Makkah", "Madinah"), cityNames("saudi"))
        assertTrue("Junagadh" in cityNames("gujarat"))
    }

    @Test fun `a city is found in another spelling or with a typo`() {
        assertEquals("Makkah", cityNames("mecca").first())
        assertEquals("Madinah", cityNames("medina").first())
        assertEquals("Bengaluru", cityNames("bengalur").first())
        assertEquals("Hyderabad", cityNames("hydrabad").first())
        assertEquals("New York", cityNames("newyork").first())
    }

    @Test fun `a city is found by its name in the app language`() {
        val hindi = mapOf("Delhi, India" to "दिल्ली, भारत")
        val search = citySearch(PredefinedLocations.CITIES) { hindi[it] ?: it }
        assertEquals("Delhi", search.search("दिल्ली").first().name)
    }

    @Test fun `city search keeps the list for a blank query and drops what does not match`() {
        assertEquals(PredefinedLocations.CITIES, cities.search(" "))
        assertEquals(emptyList<CityLocation>(), cities.search("zzqqxx"))
        // A pasted page is cut short instead of stalling the keystroke.
        assertEquals("Delhi", cities.search("delhi ".repeat(2_000)).first().name)
        assertEquals(emptyList<CityLocation>(), cities.search("delhi " + "x".repeat(5_000)))
    }

    // endregion

    // region Language picker

    private val languages = LanguageSearch(AppLanguage.supported)
    private fun codes(query: String) = languages.search(query).map { it.code }

    @Test fun `a typed language code comes first`() {
        assertEquals("ur", codes("ur").first())
        assertEquals("hi", codes("HI").first())
        assertEquals("hinglish", codes("hinglish").first())
    }

    @Test fun `a language is found by its English or native name, in any script`() {
        assertEquals("ur", codes("urdu").first())
        assertEquals("ur", codes("اردو").first())
        assertEquals("hi", codes("हिंदी").first())
        assertEquals("bn", codes("bengali").first())
        assertEquals("ta", codes("tamil").first())
    }

    @Test fun `a language name with a typo or half typed still matches`() {
        assertEquals("ml", codes("malayalm").first())
        assertEquals("ar", codes("arab").first())
    }

    @Test fun `language search keeps the list for a blank query and drops what does not match`() {
        assertEquals(AppLanguage.supported, languages.search(""))
        assertEquals(emptyList<String>(), codes("zzqqxx"))
    }

    // endregion

    // region Qaida

    @Test fun `a qaida lesson is found by its title in either language`() {
        assertEquals("shadda", qaidaSearch("tashdeed", roman = false).first().id)
        assertEquals("shadda", qaidaSearch("shadda", roman = true).first().id)
        assertEquals("ikhfa", qaidaSearch("ikhfa", roman = true).first().id)
    }

    @Test fun `a qaida lesson is found by its Madani Qaidah sabaq`() {
        assertTrue(qaidaSearch("madani 18", roman = false).any { it.id == "muqattaat" })
        assertTrue(qaidaSearch("madani 16", roman = true).any { it.id == "heavy_light" })
    }

    @Test fun `a typed number finds the lessons with that number first`() {
        val found = qaidaSearch("1", roman = false)
        val firsts = qaidaLessons.groupBy { it.tajweed }.values.map { it.first().id }.toSet()
        assertEquals(firsts, found.take(firsts.size).map { it.id }.toSet())
        assertEquals(firsts, qaidaSearch("١", roman = false).take(firsts.size).map { it.id }.toSet())
    }

    @Test fun `qaida search uses the title in the app language`() {
        val ranker = qaidaRanker(roman = false) { if (it == "Shadda") "शद्दा" else it }
        assertEquals("shadda", ranker.search("शद्दा").first().id)
    }

    @Test fun `qaida search keeps the list for a blank query and drops what does not match`() {
        assertEquals(qaidaLessons, qaidaSearch("  ", roman = false))
        assertTrue(qaidaSearch("zzzz", roman = false).isEmpty())
        assertTrue(qaidaSearch("zzqqxx " + "y".repeat(10_000), roman = true).isEmpty())
    }

    // endregion

    // region Hadith collections

    private val collections = HadithCatalog.books.groupBy { it.collectionId }
        .map { (id, books) -> id to books }
    private val hadithSearch = AppSearch.Ranker(collections) { (id, books) ->
        hadithCollectionSearchFields(id, books.first().collectionName, books)
    }
    private fun hadithIds(query: String) = hadithSearch.search(query).map { it.first }

    @Test fun `a hadith collection is found by name, compiler or Arabic title`() {
        assertEquals("bukhari", hadithIds("bukhari").first())
        assertEquals("bukhari", hadithIds("صحيح البخاري").first())
        assertEquals("bukhari", hadithIds("صحیح بخاری").first())
        assertEquals("ibn_majah", hadithIds("سنن ابن ماجہ").first())
        assertEquals("tirmidhi", hadithIds("abu isa").first())
    }

    @Test fun `a hadith collection is found in other spellings`() {
        assertEquals("tirmidhi", hadithIds("tirmizi").first())
        assertEquals("abu_dawud", hadithIds("abu daud").first())
        assertEquals("nasai", hadithIds("nasai").first())
        assertEquals("ibn_majah", hadithIds("ibn e maja").first())
    }

    @Test fun `an Urdu commentary is found by its title, other spellings or commentator`() {
        assertEquals("nematul_bari", hadithIds("nimat al-bari").first())
        assertEquals("nematul_bari", hadithIds("nematul bari").first())
        assertEquals("nematul_bari", hadithIds("نعم الباری").first())
        assertEquals("sharh_muslim", hadithIds("شرح صحیح مسلم").first())
        assertEquals("nematul_wadood", hadithIds("nematul wadood").first())
        assertEquals("sharh_tirmidhi", hadithIds("شرح ترمذی").first())
        assertEquals("sharh_tirmidhi", hadithIds("hashim khan").first())
        assertEquals("sharh_muslim", hadithIds("sharh muslim").first())
        // The book itself stays ahead of its commentary.
        assertEquals("bukhari", hadithIds("bukhari").first())
        assertEquals("muslim", hadithIds("muslim").first())
        assertEquals("tirmidhi", hadithIds("tirmidhi").first())
    }

    @Test fun `hadith search drops what does not match and keeps the list for a blank query`() {
        assertEquals(emptyList<String>(), hadithIds("zzqqxx"))
        assertEquals(collections.map { it.first }, hadithIds(""))
        assertFalse(hadithIds("muslim").isEmpty())
        assertEquals("muslim", hadithIds("muslim").first())
    }

    // endregion
}
