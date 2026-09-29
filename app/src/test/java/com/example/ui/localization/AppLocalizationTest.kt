package com.example.ui.localization

import com.example.viewmodel.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLocalizationTest {
    @Test
    fun `every supported language localizes primary navigation`() {
        AppLanguage.supported.filterNot { it.code == "en" }.forEach { language ->
            assertNotEquals(language.code, "Duas", localizeUiText("Duas", language.code))
            // "Settings" is already the everyday Roman-Hindi word.
            if (language.code != "hinglish") {
                assertNotEquals(language.code, "Settings", localizeUiText("Settings", language.code))
            }
        }
    }

    @Test
    fun `unknown language selection falls back to english`() {
        assertEquals("en", AppLanguage.normalize("xx"))
        assertEquals("gu", AppLanguage.normalize("gu"))
        assertEquals("gu", AppLanguage.normalize("gu-IN"))
        assertEquals("hinglish", AppLanguage.normalize("hi-Latn-IN"))
        assertEquals("en", AppLanguage.normalize(null))
        assertEquals("ur", AppLanguage.normalize("ur"))
    }

    @Test
    fun `every app language maps to a valid locale and rtl metadata`() {
        AppLanguage.supported.forEach { language ->
            val locale = appLocale(language.code)
            assertTrue(language.code, locale.language.isNotBlank())
            assertNotEquals(language.code, "hinglish", locale.language)
        }
        assertEquals("hi", appLocale("hinglish").language)
        assertTrue(isRtlAppLanguage("ur"))
        assertTrue(isRtlAppLanguage("ks"))
        assertTrue(isRtlAppLanguage("sd"))
        assertTrue(!isRtlAppLanguage("hi"))
    }

    @Test
    fun `primary languages localize shared controls statuses and accessibility copy`() {
        val labels = listOf(
            "Previous page", "Next page", "Play", "Pause", "Stop", "Selected",
            "Refresh Ramadan timings", "Prayer notifications enabled and scheduled.",
            "Continue where you left off", "Finding masjids near your location…"
        )
        listOf("hinglish", "hi", "ur").forEach { language ->
            labels.forEach { label ->
                assertNotEquals("$language: $label", label, localizeUiText(label, language))
            }
        }
    }

    @Test
    fun `every supported language localizes common controls`() {
        val controls = listOf("Previous", "Play", "Pause", "Stop", "Selected", "Change", "Continue", "Open", "Submit", "Address", "Download", "Refresh", "Hide", "Show", "Delete")
        AppLanguage.supported.filterNot { it.code == "en" }.forEach { language ->
            controls.forEach { control ->
                assertNotEquals("${language.code}: $control", control, localizeUiText(control, language.code))
            }
        }
    }

    @Test
    fun `dua feature has complete hindi urdu and hinglish chrome`() {
        val labels = listOf(
            "Adhkar", "Tasbeeh", "All Duas", "Morning & Evening", "Prayer & Mosque",
            "Sleep & Awakening", "Anxiety & Distress", "Travel & Leaving Home",
            "Forgiveness & Istighfar", "Parents & Family", "Food & Gratitude", "Quranic Duas",
            "Protection & Healing", "Weather & Nature", "Death & Janaza",
            "Daily Life & Manners", "Hajj & Umrah", "Comprehensive Duas", "Ramadan & Fasting",
            "Virtue / Benefit:", "Reset", "Search duas", "Clear search", "No duas found",
            "Copy dua", "Share dua", "Copied", "Rounds"
        )

        listOf("hi", "ur", "hinglish").forEach { language ->
            labels.forEach { label ->
                assertNotEquals("$language: $label", label, localizeUiText(label, language))
            }
        }
    }

    @Test
    fun `every supported language translates the full hindi reference ui copy`() {
        AppLanguage.supported.filterNot { it.code == "en" }.forEach { language ->
            val pack = uiLanguagePack(language.code)
            assertNotNull(language.code, pack)
            val missing = referenceUiPhraseKeys - pack!!.phrases.keys
            assertTrue("${language.code} is missing: $missing", missing.isEmpty())
            assertTrue("${language.code} has blank values", pack.phrases.values.none { it.isBlank() })
        }
    }

    @Test
    fun `home dashboard shortcuts and checklist are localized in every language`() {
        // Hinglish keeps loanwords such as "Quran" and "Naat" as they are written in Roman script.
        val labels = listOf("Quick access", "Quran", "Qibla", "Tasbeeh", "Hadith", "Calendar", "Ramzan", "Naat", "Zakat",
            "Salah", "Adhkar", "Done")
        val missing = AppLanguage.supported.filterNot { it.code in setOf("en", "hinglish") }.associate { language ->
            language.code to labels.filter { localizeUiText(it, language.code) == it }
        }.filterValues { it.isNotEmpty() }
        assertTrue("Missing: $missing", missing.isEmpty())
    }

    @Test
    fun `dua content is translated as whole phrases, never word by word`() {
        // Word-level UI localization would produce mixed-script text such as "रोज़ाना Istighfar".
        assertNotEquals("Daily Istighfar", localizeUiText("Daily Istighfar", "hi"))
        assertEquals("Daily Istighfar", localizePhrase("Daily Istighfar", "hi"))
        // A reviewed whole-phrase translation is still used.
        assertEquals(localizeUiText("Morning Remembrance", "gu"), localizePhrase("Morning Remembrance", "gu"))
        assertNotEquals("Morning Remembrance", localizePhrase("Morning Remembrance", "gu"))
        assertEquals("Daily Istighfar", localizePhrase("Daily Istighfar", "en"))
    }

    @Test
    fun `hadith grades are written in every app language`() {
        assertEquals("حسن صحیح", localizeHadithGrade("Hasan Sahih", "ur"))
        assertEquals("सहीह", localizeHadithGrade("Sahih", "hi"))
        assertEquals("Mauqoof", localizeHadithGrade("Mauqoof", "en"))
        assertEquals(localizeUiText("Quran", "ta"), localizeHadithGrade("Quran", "ta"))
        AppLanguage.supported.filterNot { it.code in setOf("en", "hinglish") }.forEach { language ->
            listOf("Sahih", "Hasan", "Mauqoof").forEach { grade ->
                assertNotEquals("${language.code}: $grade", grade, localizeHadithGrade(grade, language.code))
            }
        }
    }

    @Test
    fun `hijri date uses Arabic month and year marker in RTL languages`() {
        val date = com.example.prayer.HijriCalendarHelper.create(14, 4, 1448)
        assertTrue(formatHijriDate(date, "ur").contains(date.monthNameArabic))
        assertTrue(formatHijriDate(date, "ur").endsWith("هـ"))
        assertTrue(formatHijriDate(date, "en").contains(date.monthNameEnglish))
    }

    @Test
    fun `dua screen phrase keys do not rewrite unrelated text`() {
        val unrelated = "Zzyxqwerty lorem ipsum dolor sit amet consectetur."
        AppLanguage.supported.filterNot { it.code == "en" }.forEach { language ->
            assertEquals(language.code, unrelated, localizeUiText(unrelated, language.code))
        }
    }

    @Test
    fun `fragments only replace whole english words`() {
        assertEquals("Already", localizeUiText("Already", "hi"))
        assertEquals("Countdown", localizeUiText("Countdown", "bn"))
        assertEquals("अगली नमाज़", localizeUiText("Next Prayer", "hi"))
        // A translated fragment must not be translated a second time.
        assertEquals("Save karein changes", localizeUiText("Save changes", "hinglish"))
    }

    @Test
    fun `arabic devotional text is never modified`() {
        val arabic = "اللَّهُمَّ اغْفِرْ لِي"
        AppLanguage.supported.forEach { language ->
            assertEquals(language.code, arabic, localizeUiText(arabic, language.code))
        }
    }

    @Test
    fun `more subtitles are either fully translated or hidden`() {
        val keys = listOf(
            "Tools & resources", "Direction of the Kaaba", "Hijri dates & key days",
            "Sehri & Iftar times", "Sayings of the Prophet ﷺ", "Voice-only recitations",
            "Islamic occasions & aamal", "Language, alerts & prayer times"
        )
        keys.forEach { assertEquals(it, localizedMoreSubtitle(it, "en")) }
        AppLanguage.supported.filterNot { it.code == "en" }.forEach { language ->
            val translated = keys.map { localizedMoreSubtitle(it, language.code) }
            assertTrue(language.code, translated.all { it == null } || translated.none { it == null })
            translated.filterNotNull().zip(keys).forEach { (value, key) ->
                assertNotEquals(language.code, key, value)
            }
        }
    }
}
