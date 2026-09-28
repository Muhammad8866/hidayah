package com.example.salah

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SalahContentTest {
    @Test fun fiveDailyFardCountsAndKinds() {
        assertEquals(listOf("fajr", "zuhr", "asr", "maghrib", "isha"), SalahContent.daily.map { it.id })
        assertEquals(listOf(2, 4, 4, 3, 4), SalahContent.daily.map { it.fard })
        assertTrue(SalahContent.daily.all { prayer -> prayer.units.count { it.kind == SalahKind.FARD } == 1 })
        assertTrue(SalahContent.daily.flatMap { it.units }.all { it.count > 0 })
    }

    @Test fun guidesCoverEveryRakahAndAllReferencedRecitations() {
        for (count in 2..4) for (school in Madhhab.entries) {
            val steps = SalahContent.prayerSteps(count, school)
            assertEquals("prepare", steps.first().id)
            assertEquals("finish", steps.last().id)
            assertEquals(count, steps.count { it.id.startsWith("recite_") })
            assertEquals(count, steps.count { it.id.startsWith("bow_") })
            assertEquals(count, steps.count { it.id.startsWith("prostrate_") })
            assertEquals(count > 2, steps.any { it.id == "middle" })
            assertTrue(steps.flatMap { it.recitationIds }.all { it in SalahContent.recitations })
        }
    }

    @Test fun specialGuidesAndRecitationsHaveUsableContent() {
        val required = setOf("wudu", "jamaat", "qaza", "sahw", "jumuah", "eid", "janazah", "tahajjud", "taraweeh", "witr")
        assertTrue(SalahContent.topics.map { it.id }.containsAll(required))
        assertTrue(SalahContent.topics.all { it.steps.isNotEmpty() && it.source.isNotBlank() })
        assertTrue(SalahContent.recitations.values.all {
            it.arabic.isNotBlank() && it.transliteration.isNotBlank() && it.meaning.en.isNotBlank() && it.source.isNotBlank()
        })
        assertEquals(Madhhab.entries.toSet(), SalahContent.schoolSummaries.keys)
        val eid = SalahContent.topics.first { it.id == "eid" }
        assertEquals(SalahKind.WAJIB, eid.kindFor(Madhhab.HANAFI))
        assertEquals(SalahKind.FARD_KIFAYAH, eid.kindFor(Madhhab.HANBALI))
    }
}
