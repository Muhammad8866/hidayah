package com.example.salah

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer

class SalahContentTest {
    private val everyGuide = buildList {
        for (form in GuideForm.entries) for (school in Madhhab.entries)
            for (worshipper in Worshipper.entries) for (prayingAs in PrayingAs.entries)
                add(form to SalahContent.prayerSteps(form, school, worshipper, prayingAs))
    }

    @Test fun fiveDailyFardCountsAndHanafiTotals() {
        assertEquals(listOf("fajr", "zuhr", "asr", "maghrib", "isha"), SalahContent.daily.map { it.id })
        assertEquals(listOf(2, 4, 4, 3, 4), SalahContent.daily.map { it.fard })
        assertEquals(listOf(4, 12, 8, 7, 17), SalahContent.daily.map { it.total })
        assertEquals(2, SalahContent.jumuah.fard)
        assertEquals(14, SalahContent.jumuah.total)
        assertTrue(SalahContent.allPrayers.all { prayer -> prayer.units.count { it.kind == SalahKind.FARD } == 1 })
        assertTrue(SalahContent.allPrayers.flatMap { it.units }.all { it.count > 0 })
    }

    @Test fun everyUnitOfADailyPrayerCanBeLearntStepByStep() {
        SalahContent.allPrayers.forEach { prayer ->
            prayer.units.forEach { unit ->
                val form = unit.form
                assertTrue("${prayer.id} ${unit.kind}", form != null && form.rakats == unit.count)
            }
        }
        assertEquals(GuideForm.WITR_3, SalahContent.daily.last().units.first { it.kind == SalahKind.WITR }.form)
        assertEquals("1+", Rakats(3, SalahKind.WITR, SalahKind.WITR.title).countLabel(Madhhab.SHAFII))
    }

    @Test fun guidesCoverEveryRakahAndAllReferencedRecitations() {
        everyGuide.forEach { (form, steps) ->
            assertEquals("prepare", steps.first().id)
            assertEquals("after", steps.last().id)
            assertEquals(steps.map { it.id }.distinct(), steps.map { it.id })
            listOf("qiraat_", "ruku_", "qawmah_", "sujud_").forEach { prefix ->
                assertEquals("$form $prefix", form.rakats, steps.count { it.id.startsWith(prefix) })
            }
            assertEquals(form.rakats > 2, steps.any { it.id == "qadah_ula" })
            assertEquals(form == GuideForm.WITR_3, steps.any { it.id == "qunut" })
            assertTrue(steps.map { it.id }.containsAll(listOf("niyyah", "takbir", "thana", "qadah_akhirah", "salam")))
            assertTrue(steps.flatMap { it.recitationIds }.all { it in SalahContent.recitations })
        }
    }

    @Test fun surahIsAddedInEveryRakahExceptTheLastTwoOfFard() {
        fun recited(form: GuideForm, rakah: Int) =
            SalahContent.prayerSteps(form, Madhhab.HANAFI).first { it.id == "qiraat_$rakah" }.recitationIds
        assertEquals(2, recited(GuideForm.FARD_4, 2).size)
        assertEquals(listOf("fatiha"), recited(GuideForm.FARD_4, 3))
        assertEquals(listOf("fatiha"), recited(GuideForm.FARD_3, 3))
        assertEquals(2, recited(GuideForm.SUNNAH_4, 4).size)
        assertEquals(2, recited(GuideForm.WITR_3, 3).size)
    }

    @Test fun witrQunutComesBeforeTheThirdRukuWithTheHanafiWording() {
        val ids = SalahContent.prayerSteps(GuideForm.WITR_3, Madhhab.HANAFI).map { it.id }
        assertEquals(ids.indexOf("qiraat_3") + 1, ids.indexOf("qunut"))
        assertEquals(ids.indexOf("qunut") + 1, ids.indexOf("ruku_3"))
        val qunut = SalahContent.prayerSteps(GuideForm.WITR_3, Madhhab.HANAFI).first { it.id == "qunut" }
        assertEquals("qunut_hanafi", qunut.recitationIds.first())
        assertTrue(SalahContent.recitations.getValue("qunut_hanafi").arabic.startsWith("اللَّهُمَّ إِنَّا نَسْتَعِينُكَ"))
        assertEquals(Ruling.WAJIB, qunut.ruling)
    }

    @Test fun followerStaysSilentBehindTheImam() {
        val steps = SalahContent.prayerSteps(GuideForm.FARD_4, Madhhab.HANAFI, Worshipper.MAN, PrayingAs.BEHIND_IMAM)
        assertTrue(steps.filter { it.id.startsWith("qiraat_") }.all { it.recitationIds.isEmpty() })
        assertEquals(listOf("thana"), steps.first { it.id == "thana" }.recitationIds)
        assertTrue(steps.filter { it.id.startsWith("qawmah_") }.all { it.recitationIds == listOf("tahmid") })
        // Everything else is still said by the follower.
        assertEquals(listOf("tashahhud", "salawat", "dua_masura", "dua_ibrahim"), steps.first { it.id == "qadah_akhirah" }.recitationIds)
    }

    @Test fun womenGetTheirOwnPostures() {
        val man = SalahContent.prayerSteps(GuideForm.FARD_2, Madhhab.HANAFI, Worshipper.MAN).associateBy { it.id }
        val woman = SalahContent.prayerSteps(GuideForm.FARD_2, Madhhab.HANAFI, Worshipper.WOMAN).associateBy { it.id }
        listOf("takbir", "ruku_1", "sujud_1", "qadah_akhirah").forEach { id ->
            assertNotEquals(id, man.getValue(id).instruction, woman.getValue(id).instruction)
        }
        assertEquals(man.getValue("qiraat_1").instruction, woman.getValue("qiraat_1").instruction)
    }

    @Test fun masailAndSpecialPrayersHaveUsableContent() {
        val rules = setOf("sharait", "faraiz", "wajibat", "sunan", "mufsidat", "makruhat", "times", "wudu", "ghusl", "tayammum", "jamaat", "sahw", "qaza", "musafir")
        val prayers = setOf("witr", "jumuah", "eid", "janazah", "tahajjud", "taraweeh", "duha", "istikhara", "kusuf", "istisqa", "masjid", "tasbih")
        assertTrue(SalahContent.rules.map { it.id }.containsAll(rules))
        assertTrue(SalahContent.topics.map { it.id }.containsAll(prayers))
        val all = SalahContent.rules + SalahContent.topics
        assertEquals(all.map { it.id }.distinct(), all.map { it.id })
        assertTrue(all.all { it.steps.isNotEmpty() && it.source.isNotBlank() })
        assertTrue(all.flatMap { it.recitationIds }.all { it in SalahContent.recitations })
        assertEquals(6, SalahContent.rules.first { it.id == "sharait" }.steps.size)
        assertEquals(7, SalahContent.rules.first { it.id == "faraiz" }.steps.size)
        assertTrue(SalahContent.recitations.values.all {
            it.arabic.isNotBlank() && it.transliteration.isNotBlank() && it.meaning.en.isNotBlank() && it.source.isNotBlank()
        })
        assertEquals(Madhhab.entries.toSet(), SalahContent.schoolSummaries.keys)
        assertEquals(Madhhab.entries.toSet() - Madhhab.HANAFI, SalahContent.guideSchoolNotes.keys)
        val eid = SalahContent.topics.first { it.id == "eid" }
        assertEquals(SalahKind.WAJIB, eid.kindFor(Madhhab.HANAFI))
        assertEquals(SalahKind.FARD_KIFAYAH, eid.kindFor(Madhhab.HANBALI))
    }

    @Test fun niyyahNamesThePrayerInTheReadersLanguage() {
        val fajr = SalahContent.daily.first()
        val fard = fajr.units.first { it.kind == SalahKind.FARD }
        assertEquals("I intend 2 rak‘at Fard of Fajr, for Allah Ta‘ala, facing the Ka‘bah.", SalahContent.niyyah(fajr, fard, "en"))
        assertEquals(
            "Niyyat ki maine 2 rakat namaz Farz, waqt Fajr ka, waste Allah Ta'ala ke, munh mera Kaaba sharif ki taraf.",
            SalahContent.niyyah(fajr, fard, "hinglish")
        )
        assertTrue(SalahContent.niyyah(fajr, fard, "ur").contains("فرض"))
        // Regional drafts now render both the sentence and its prayer/kind placeholders.
        val gujarati = SalahContent.niyyah(fajr, fard, "gu")
        assertNotEquals(SalahContent.niyyah(fajr, fard, "en"), gujarati)
        assertTrue(gujarati.contains(fajr.title.inLanguage("gu")))
        assertTrue(gujarati.contains(fard.kind.title.inLanguage("gu")))
        assertTrue(!gujarati.contains("{prayer}") && !gujarati.contains("{kind}"))
    }

    @Test fun everyStringIsWrittenInHinglishHindiAndUrdu() {
        val texts = buildList {
            Madhhab.entries.forEach { add(it.title) }
            SalahKind.entries.forEach { add(it.title) }
            Ruling.entries.forEach { add(it.title) }
            GuideForm.entries.forEach { add(it.title) }
            Worshipper.entries.forEach { add(it.title) }
            PrayingAs.entries.forEach { add(it.title) }
            RecitationGroup.entries.forEach { add(it.title) }
            addAll(staticTexts(SalahUi::class.java))
            addAll(staticTexts(SalahContent.Notes::class.java))
            addAll(SalahContent.schoolSummaries.values)
            addAll(SalahContent.guideSchoolNotes.values)
            SalahContent.allPrayers.forEach { prayer ->
                add(prayer.title); add(prayer.note)
                prayer.units.forEach { add(it.whenPrayed) }
            }
            SalahContent.recitationList.forEach { add(it.title); add(it.meaning); it.note?.let(::add) }
            (SalahContent.rules + SalahContent.topics).forEach { topic ->
                add(topic.title); add(topic.summary); addAll(topic.steps); addAll(topic.schoolNotes.values)
                topic.footnote?.let(::add)
            }
            everyGuide.forEach { (_, steps) ->
                steps.forEach { add(it.title); add(it.instruction); addAll(it.points) }
            }
        }.distinct()
        assertTrue(texts.size > 300)

        val placeholder = Regex("\\{[a-z]+\\}")
        texts.forEach { text ->
            assertEquals(text.en, setOf("hinglish", "hi", "ur"), text.translations.keys)
            assertTrue(text.en, text.translations.values.all { it.isNotBlank() })
            val hindi = text.translations.getValue("hi")
            val urdu = text.translations.getValue("ur")
            val hinglish = text.translations.getValue("hinglish")
            assertTrue("hi: ${text.en}", hindi.any { it in 'ऀ'..'ॿ' } && hindi.none(::isArabicLetter))
            assertTrue("ur: ${text.en}", urdu.any(::isArabicLetter) && urdu.none { it in 'ऀ'..'ॿ' })
            assertTrue("hinglish: ${text.en}", hinglish.none { it in 'ऀ'..'ॿ' || isArabicLetter(it) })
            // Hindi and Urdu copy must not leave English sentences behind.
            assertTrue("hi latin: ${text.en}", placeholder.replace(hindi, "").none { it in 'A'..'Z' || it in 'a'..'z' })
            assertTrue("ur latin: ${text.en}", placeholder.replace(urdu, "").none { it in 'A'..'Z' || it in 'a'..'z' })
            val expected = placeholder.findAll(text.en).map { it.value }.toSet()
            text.translations.forEach { (language, value) ->
                assertEquals("$language: ${text.en}", expected, placeholder.findAll(value).map { it.value }.toSet())
            }
        }
    }

    @Test fun quranicRecitationsMatchTheBundledMushafLetterForLetter() {
        val verses = File("src/main/assets/quiz/quran-simple.txt").readLines()
            .mapNotNull { line -> line.split('|', limit = 3).takeIf { it.size == 3 } }
            .associate { (surah, ayah, text) -> "$surah:$ayah" to text }
        val basmalah = normalize(verses.getValue("1:1"))
        val checked = SalahContent.recitationList.filter { it.quran != null }
        assertTrue(checked.map { it.id }.containsAll(listOf("fatiha", "ikhlas", "falaq", "nas", "kawthar", "asr", "ayat_kursi")))
        checked.forEach { recitation ->
            val (surah, range) = recitation.quran!!.split(':')
            val (from, to) = range.split('-').map(String::toInt)
            val expected = (from..to).joinToString(" ") { ayah ->
                val verse = normalize(verses.getValue("$surah:$ayah"))
                // Every surah but al-Fatihah carries the basmalah in front of its first verse.
                if (ayah == 1 && surah != "1") verse.removePrefix(basmalah).trim() else verse
            }
            val actual = normalize(recitation.arabic)
            // A dua taken from inside a verse must still be a letter-exact part of it.
            assertTrue("${recitation.id}\nexpected: $expected\nactual:   $actual", if (recitation.id == "rabbana_atina") expected.contains(actual) else expected == actual)
        }
    }

    /**
     * The Mushaf file writes the dagger alif on a tatweel and orders stacked marks (shadda, fatha)
     * differently; canonical ordering makes the same letters and marks compare equal.
     */
    private fun normalize(arabic: String): String = Normalizer.normalize(
        arabic.replace("ـ", "").replace('\n', ' ').replace(Regex("\\s+"), " ").trim(),
        Normalizer.Form.NFC
    )

    // ﷺ lives in the Arabic presentation block but is used in every language.
    private fun isArabicLetter(char: Char): Boolean = char in '؀'..'ۿ'

    private fun staticTexts(holder: Class<*>): List<SalahText> = holder.declaredFields
        .filter { it.type == SalahText::class.java }
        .map { field -> field.isAccessible = true; field.get(null) as SalahText }
}
