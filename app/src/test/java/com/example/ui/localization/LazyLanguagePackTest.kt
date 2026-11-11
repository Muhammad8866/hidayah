package com.example.ui.localization

import com.example.viewmodel.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every process start (prayer alarms, widget refreshes, boot, app update) reads the saved
 * language on the main thread. Building all ~50 language packs then, with every regional
 * machine-draft class, made background wakes slow. Packs are built one language at a time,
 * on first use. Caches are process-wide, so these checks compare before/after snapshots and
 * stay valid whatever other tests ran first.
 */
class LazyLanguagePackTest {
    @Test
    fun `a fresh registry builds no pack until one language is used`() {
        val registry = newLanguagePackRegistry()
        assertTrue(registry.values.none { it.isInitialized() })
        val indicBefore = IndicDraftPhrases.loadedLanguages()

        registry.getValue("gu").value

        assertEquals(setOf("gu"), registry.filterValues { it.isInitialized() }.keys)
        assertTrue((IndicDraftPhrases.loadedLanguages() - indicBefore).all { it == "gu" })
        assertTrue("gu" in IndicDraftPhrases.loadedLanguages())
    }

    @Test
    fun `registry covers exactly the supported non English languages`() {
        assertEquals(
            AppLanguage.supported.map { it.code }.filterNot { it == "en" }.toSet(),
            newLanguagePackRegistry().keys
        )
    }

    @Test
    fun `resolving the saved language builds no pack`() {
        val before = builtLanguagePacks()
        val indicBefore = IndicDraftPhrases.loadedLanguages()

        assertEquals("gu", supportedAppLanguageOrEnglish("gu"))
        assertEquals("hinglish", supportedAppLanguageOrEnglish("hinglish"))
        assertEquals("en", supportedAppLanguageOrEnglish("en"))
        assertEquals("en", supportedAppLanguageOrEnglish("xx"))
        assertEquals("en", supportedAppLanguageOrEnglish(""))
        assertEquals("en", supportedAppLanguageOrEnglish("GU"))
        assertEquals("en", supportedAppLanguageOrEnglish(null))

        assertEquals(before, builtLanguagePacks())
        assertEquals(indicBefore, IndicDraftPhrases.loadedLanguages())
    }

    @Test
    fun `english and unknown languages build no pack`() {
        val before = builtLanguagePacks()
        val indicBefore = IndicDraftPhrases.loadedLanguages()

        assertEquals("Duas", localizeUiText("Duas", "en"))
        assertEquals("Duas", localizePhrase("Duas", "en"))
        assertEquals("Duas", localizePhrase("Duas", "xx"))
        assertEquals(null, uiLanguagePack("xx"))

        assertEquals(before, builtLanguagePacks())
        assertEquals(indicBefore, IndicDraftPhrases.loadedLanguages())
    }

    @Test
    fun `localizing builds only the language asked for`() {
        val before = builtLanguagePacks()
        val indicBefore = IndicDraftPhrases.loadedLanguages()

        assertNotEquals("Duas", localizePhrase("Duas", "ta"))

        assertTrue("ta" in builtLanguagePacks())
        assertTrue((builtLanguagePacks() - before).all { it == "ta" })
        assertTrue((IndicDraftPhrases.loadedLanguages() - indicBefore).all { it == "ta" })
    }

    @Test
    fun `lazy packs keep the merge precedence of their sources`() {
        // Later maps win a Kotlin map merge, so the last source of each pack shows through.
        listOf("hinglish", "hi", "ur").forEach { code ->
            val pack = uiLanguagePack(code)!!
            analyticsPhrases.getValue(code).forEach { (key, value) ->
                assertEquals("$code: $key", value, pack.phrases[key])
            }
            controlWordsByLanguage.getValue(code).forEach { (key, value) ->
                assertEquals("$code word: $key", value, pack.words[key])
            }
        }
        listOf("gu", "ta", "ar", "fr").forEach { code ->
            val pack = uiLanguagePack(code)!!
            duaScreenPhrases.getValue(code).forEach { (key, value) ->
                if (key != "Community Masjid") assertEquals("$code: $key", value, pack.phrases[key])
            }
            controlWordsByLanguage.getValue(code).forEach { (key, value) ->
                assertEquals("$code word: $key", value, pack.words[key])
            }
        }
    }

    @Test
    fun `regional packs keep drafts and the composed Community Masjid label`() {
        val gu = uiLanguagePack("gu")!!
        val masjid = gujaratiPack.phrases.getValue("Masjid")
        val community = gu.phrases.getValue("Community Masjid")
        assertNotEquals("Community Masjid", community)
        assertTrue(community, community.endsWith(" $masjid"))

        val duaScreen = duaScreenPhrases.getValue("gu")
        IndicDraftPhrases.forLanguage("gu").forEach { (key, value) ->
            val reviewed = gujaratiPack.phrases[key]
            if (key != "Community Masjid" && key !in duaScreen && (reviewed == null || reviewed == key)) {
                assertEquals("gu draft: $key", value, gu.phrases[key])
            }
        }
        // A reviewed translation is never replaced by its machine draft.
        gujaratiPack.phrases.forEach { (key, value) ->
            if (key != "Community Masjid" && key !in duaScreen && value != key) {
                assertEquals("gu reviewed: $key", value, gu.phrases[key])
            }
        }
    }

    @Test
    fun `the same pack is returned on every use`() {
        val first = uiLanguagePack("hi")
        assertTrue(first === uiLanguagePack("hi"))
    }
}
