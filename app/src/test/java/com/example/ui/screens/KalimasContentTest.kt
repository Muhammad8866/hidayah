package com.example.ui.screens

import org.junit.Assert.*
import org.junit.Test

class KalimasContentTest {
    @Test fun hasSixKalimasAndBothImanDeclarations() {
        assertEquals(8, KalimasContent.entries.size)
        assertEquals(8, KalimasContent.entries.map { it.id }.toSet().size)
        assertEquals(listOf("mujmal", "mufassal"), KalimasContent.entries.takeLast(2).map { it.id })
        KalimasContent.entries.forEach {
            assertTrue(it.arabic.any { char -> char in '؀'..'ۿ' })
            assertTrue(it.roman.isNotBlank() && it.urdu.isNotBlank() && it.english.isNotBlank())
        }
    }

    @Test fun searchesArabicWithoutHarakatAndRomanMeaning() {
        assertEquals(listOf("mufassal"), KalimasContent.search("mufassal").map { it.id })
        assertEquals(listOf("istighfar", "radd_kufr"), KalimasContent.search("maafi gunahon").map { it.id })
        assertTrue(KalimasContent.search("أشهد").any { it.id == "shahadat" })
        assertTrue(KalimasContent.search("unlikely-no-match").isEmpty())
        assertEquals(8, KalimasContent.search("  ").size)
    }

    @Test fun keepsTraditionalWordingAndOrder() {
        assertEquals(listOf("tayyibah", "shahadat", "tamjeed", "tauheed", "istighfar", "radd_kufr", "mujmal", "mufassal"),
            KalimasContent.entries.map { it.id })
        assertEquals("لَا إِلٰهَ إِلَّا اللّٰهُ مُحَمَّدٌ رَسُولُ اللّٰهِ", KalimasContent.byId("tayyibah")!!.arabic)
        assertEquals("La ilaha illallahu Muhammadur Rasulullah.", KalimasContent.byId("tayyibah")!!.roman)
        assertEquals("Subhanallahi walhamdu lillahi wa la ilaha illallahu wallahu akbar. Wa la hawla wa la quwwata illa billahil aliyyil azeem.",
            KalimasContent.byId("tamjeed")!!.roman)
        assertEquals("Amantu billahi wa mala'ikatihi wa kutubihi wa rusulihi wal-yawmil-akhiri wal-qadari khayrihi wa sharrihi minallahi ta'ala wal-ba'thi ba'dal-mawt.",
            KalimasContent.byId("mufassal")!!.roman)
        assertNull(KalimasContent.byId("missing"))
    }

    @Test fun phrasesRebuildTheFullText() {
        KalimasContent.entries.forEach { entry ->
            assertTrue(entry.id, entry.phrases.size >= 2)
            assertEquals(entry.arabic, entry.phrases.joinToString(" ") { it.arabic })
            assertEquals(entry.roman, entry.phrases.joinToString(" ") { it.roman })
            entry.phrases.forEach {
                assertTrue(it.arabic.isNotBlank() && it.roman.isNotBlank() && it.urdu.isNotBlank() && it.english.isNotBlank())
                assertEquals(it.arabic, it.arabic.trim())
                assertEquals(it.roman, it.roman.trim())
            }
        }
    }

    @Test fun groupsNumbersAndNames() {
        val kalimas = KalimasContent.entries.filter { it.group == KalimaGroup.KALIMA }
        assertEquals(listOf<Int?>(1, 2, 3, 4, 5, 6), kalimas.map { it.number })
        val iman = KalimasContent.entries.filter { it.group == KalimaGroup.IMAN }
        assertEquals(listOf("mujmal", "mufassal"), iman.map { it.id })
        assertTrue(iman.all { it.number == null })
        KalimasContent.entries.forEach {
            assertTrue(it.titleArabic.any { char -> char in '؀'..'ۿ' })
            assertTrue(it.nameMeaning.isNotBlank() && it.nameMeaningEnglish.isNotBlank())
            assertEquals(it.note.isBlank(), it.noteEnglish.isBlank())
        }
    }

    @Test fun searchFiltersByGroupAndCoversPhrasesAndNames() {
        assertEquals(6, KalimasContent.search("", KalimaGroup.KALIMA).size)
        assertEquals(listOf("mujmal", "mufassal"), KalimasContent.search("", KalimaGroup.IMAN).map { it.id })
        assertTrue(KalimasContent.search("mufassal", KalimaGroup.KALIMA).isEmpty())
        assertEquals(listOf("mufassal"), KalimasContent.search("angels", KalimaGroup.IMAN).map { it.id })
        assertEquals(listOf("tamjeed"), KalimasContent.search("glorification").map { it.id })
        assertEquals(listOf("mufassal"), KalimasContent.search("jibril").map { it.id })
        assertEquals(listOf("tauheed"), KalimasContent.search("Ever-Living").map { it.id })
    }

    @Test fun shareTextCarriesTheChosenMeaning() {
        val entry = KalimasContent.byId("shahadat")!!
        val urdu = KalimasContent.shareText(entry, english = false)
        val english = KalimasContent.shareText(entry, english = true)
        listOf(urdu, english).forEach {
            assertTrue(it.startsWith(entry.title))
            assertTrue(entry.arabic in it && entry.roman in it)
            assertTrue(it.endsWith("— Hidayah"))
        }
        assertTrue(entry.urdu in urdu && entry.english !in urdu)
        assertTrue(entry.english in english && entry.urdu !in english)
    }
}
