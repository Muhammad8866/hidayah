package com.example.names

import org.junit.Assert.*
import org.junit.Test

class BabyNamesTest {
    @Test fun catalogHasUniqueIdsAndReviewedSources() {
        assertEquals(798, BabyNames.names.size)
        assertEquals(BabyNames.names.size, BabyNames.names.map { it.id }.toSet().size)
        assertEquals(497, BabyNames.search("", BabyGender.BOY).size)
        assertEquals(301, BabyNames.search("", BabyGender.GIRL).size)
        assertTrue(BabyNames.names.all { listOf(it.name, it.arabic, it.english, it.hinglish,
            it.sourceUrl, it.sourceTitle, it.sourceSpelling, it.sourceMeaningUrdu).all(String::isNotBlank) })
        assertTrue(BabyNames.names.all {
            it.sourceUrl.matches(Regex("https://www\\.dawateislami\\.net/bookslibrary/ur/naam-rakhnay-kay-ahkam/page-(4[3-9]|5[0-9]|6[01])")) ||
                it.sourceUrl.matches(Regex("https://quranicnames\\.com/[a-z-]+/"))
        })
        assertEquals(BabyNames.names.sortedBy { it.name }, BabyNames.names)
    }

    @Test fun newNamesAreSearchableInEverySupportedLanguage() {
        assertTrue(BabyNames.search("adeeba", BabyGender.GIRL).any { it.name == "Adiba" })
        assertTrue(BabyNames.search("معراج", BabyGender.BOY).any { it.name == "Miraj" })
        assertTrue(BabyNames.search("raat guftagu", BabyGender.BOY).any { it.name == "Sameer" })
        assertTrue(BabyNames.search("sunrise").any { it.name == "Ishraq" })
        val devotional = BabyNames.search("amatul lateef", BabyGender.GIRL).single()
        assertEquals("Amatul Latif", devotional.name)
        assertTrue(devotional.sourceUrl.endsWith("/amatul-latif/"))
        assertTrue(devotional.meaning("ur").contains("اللہ کی بندی"))
    }

    @Test fun newUnisexEntriesAndExistingAliasIdsRemainConsistent() {
        listOf("Areej", "Daanish", "Insaf", "Rida", "Wafa", "Ishraq", "Fajr").forEach { name ->
            val entries = BabyNames.names.filter { it.name == name }
            assertEquals(BabyGender.entries.toSet(), entries.map { it.gender }.toSet())
            assertEquals(2, entries.map { it.id }.toSet().size)
        }
        assertEquals("GIRL:Rabiah", BabyNames.search("raabia", BabyGender.GIRL).single().id)
        assertEquals("BOY:Suhayl", BabyNames.search("suhail", BabyGender.BOY).single().id)
        assertFalse(BabyNames.names.any { it.name == "Rabia" || it.name == "Suhail" })
    }

    @Test fun expandedNamesHaveSearchableAliasesAndTranslatedMeanings() {
        val inaya = BabyNames.search("inaaya").single { it.name == "Inaya" }
        assertEquals("Help; care; protection", inaya.meaning("en"))
        assertEquals("مدد، دیکھ بھال، حفاظت", inaya.meaning("ur"))
        assertTrue(BabyNames.search("dekhbhal hifazat", BabyGender.GIRL).any { it.name == "Inaya" })
        assertEquals("Zidan", BabyNames.search("zaydan", BabyGender.BOY).single().name)
        assertEquals("Bushra", BabyNames.search("بشرى").single().name)
        assertEquals("Tasnim", BabyNames.search("tasneem", BabyGender.GIRL).single().name)
        assertTrue(BabyNames.names.filter { it.sourceUrl.startsWith("https://quranicnames.com/") }
            .all { it.sourceTitle == "QuranicNames — Arabic name research" })
    }

    @Test fun expandedUnisexNamesRespectBothGenderFilters() {
        listOf("Abrar", "Afnan", "Hanan", "Ihsan", "Ridwan", "Salsabil", "Tasnim").forEach { name ->
            val entries = BabyNames.names.filter { it.name == name }
            assertEquals(BabyGender.entries.toSet(), entries.map { it.gender }.toSet())
            BabyGender.entries.forEach { gender ->
                assertTrue(BabyNames.search(name, gender).any { it.name == name && it.gender == gender })
            }
        }
    }

    @Test fun searchesAliasesArabicAndBothMeanings() {
        assertEquals("Aisha", BabyNames.search("ayesha").single().name)
        assertTrue(BabyNames.search("Allah servant").any { it.name == "Abdullah" })
        assertEquals("Rayyan", BabyNames.search("Rozedaron Jannat").single().name)
        assertTrue(BabyNames.search("علي").any { it.name == "Ali" })
        assertTrue(BabyNames.search("علی").any { it.name == "Ali" })
        assertTrue(BabyNames.search("عبدالرحمن").any { it.name == "Abdur Rahman" })
        assertEquals("Fatima", BabyNames.search("فاطمة").single().name)
        assertTrue(BabyNames.search("ayesha", BabyGender.BOY).isEmpty())
        assertTrue(BabyNames.search("no-such-name").isEmpty())
    }

    @Test fun searchFindsTheIntendedNameInAnySpellingOrScriptAndNothingElse() {
        assertEquals(listOf("Aisha"), BabyNames.search("ayesha").map { it.name })
        assertEquals(listOf("Aisha"), BabyNames.search("ayisha").map { it.name })
        assertEquals(listOf("Fatima"), BabyNames.search("fatma").map { it.name })
        assertEquals(listOf("Fatima"), BabyNames.search("fatimah").map { it.name })
        assertEquals(listOf("Zaynab"), BabyNames.search("zainab").map { it.name })
        assertEquals(listOf("Abdur Rahman"), BabyNames.search("abdurrahman").map { it.name })
        assertEquals(listOf("Abdur Rahman"), BabyNames.search("abdul rehman").map { it.name })
        assertEquals(listOf("Abdur Rahman"), BabyNames.search("عبد الرحمن").map { it.name })
        assertEquals(listOf("Abdur Rahman"), BabyNames.search("عبدالرحمن").map { it.name })
        assertEquals(listOf("Aisha"), BabyNames.search("आयशा").map { it.name })
        assertEquals(listOf("Fatima"), BabyNames.search("फातिमा").map { it.name })
        assertEquals(listOf("Inaya", "Inayat"), BabyNames.search("inaaya").map { it.name })
    }

    @Test fun anExactNameComesFirst() {
        assertEquals("Ali", BabyNames.search("ali").first().name)
        assertEquals("Ali", BabyNames.search("علي").first().name)
        assertEquals("Abdullah", BabyNames.search("Allah servant").first().name)
    }

    @Test fun aTypoStillFindsTheNameWhenNothingIsSpeltThatWay() {
        assertTrue(BabyNames.search("zenab").any { it.name == "Zaynab" })
    }

    @Test fun badInputKeepsOrEmptiesTheListWithoutBreaking() {
        assertEquals(BabyNames.names, BabyNames.search(""))
        assertEquals(BabyNames.names, BabyNames.search("💥"))
        assertEquals(BabyNames.names, BabyNames.search("-- , ."))
        assertEquals(497, BabyNames.search("💥", BabyGender.BOY).size)
        assertTrue(BabyNames.search("x".repeat(5_000)).isEmpty())
        assertTrue(BabyNames.search("\u0000ayesha\u0000").any { it.name == "Aisha" })
    }

    @Test fun theReadersLanguageMeaningIsSearchableToo() {
        listOf("en", "ur", "hi", "hinglish", "ar", "fr").forEach { language ->
            assertEquals(language, "Aisha", BabyNames.search("ayesha", language = language).first().name)
        }
        val aisha = BabyNames.names.first { it.name == "Aisha" }
        val french = aisha.meaning("fr")
        assertTrue(BabyNames.search(french, BabyGender.GIRL, language = "fr").contains(aisha))
    }

    @Test fun sameNameInBothGendersHasDistinctStableIds() {
        val names = BabyNames.names.filter { it.name == "Sariyah" }
        assertEquals(BabyGender.entries.toSet(), names.map { it.gender }.toSet())
        assertEquals(2, names.map { it.id }.toSet().size)
    }

    @Test fun meaningsUseTheCitedSourceAndUrduOriginal() {
        val aisha = BabyNames.search("ayesha").single()
        assertEquals("Prosperous", aisha.meaning("en"))
        assertEquals("Khushhaal", aisha.meaning("hinglish"))
        assertEquals("خوشحال", aisha.meaning("ur"))
        assertTrue(aisha.sourceUrl.endsWith("page-57"))
    }
}
