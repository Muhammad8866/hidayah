package com.example.ui.screens

import com.example.ui.localization.dailyToolPhrases
import com.example.ui.localization.hajjUmrahPhrases
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HajjUmrahGuideTest {
    @Test fun saiAlternatesDirectionAndEndsAtMarwah() {
        assertEquals("Safa → Marwah", counterStage(CounterKind.SAI, 0).hint)
        assertEquals("Marwah → Safa", counterStage(CounterKind.SAI, 1).hint)
        assertEquals("Safa → Marwah", counterStage(CounterKind.SAI, 6).hint)
        assertEquals("Sa'i complete. It ends at Marwah.", counterStage(CounterKind.SAI, 7).hint)
    }

    @Test fun tawafHasRamlOnlyInFirstThreeRounds() {
        assertEquals("Raml: men walk briskly with short steps", counterStage(CounterKind.TAWAF, 2).hint)
        assertEquals("Walk at a normal pace", counterStage(CounterKind.TAWAF, 3).hint)
        assertEquals(7, counterStage(CounterKind.TAWAF, 99).subCount)
    }

    @Test fun threeJamaratMoveFromUlaToAqabahWithDuaStops() {
        assertEquals("Jamrah Ula (small)", counterStage(CounterKind.RAMI_THREE, 0).hint)
        assertEquals("Make dua facing the Qibla, then go to the Wusta", counterStage(CounterKind.RAMI_THREE, 7).hint)
        assertEquals(7, counterStage(CounterKind.RAMI_THREE, 7).subCount)
        assertEquals("Jamrah Wusta (middle)", counterStage(CounterKind.RAMI_THREE, 9).hint)
        assertEquals(2, counterStage(CounterKind.RAMI_THREE, 9).subCount)
        assertEquals("Make dua facing the Qibla, then go to the Aqabah", counterStage(CounterKind.RAMI_THREE, 14).hint)
        assertEquals("Jamrat al-Aqabah (big)", counterStage(CounterKind.RAMI_THREE, 20).hint)
        assertEquals("All three Jamarat done for today", counterStage(CounterKind.RAMI_THREE, 21).hint)
    }

    @Test fun hajjOrderDependsOnType() {
        assertEquals("t_umrah", hajjSteps(HajjType.TAMATTU).first().id)
        assertEquals(listOf("q_ihram", "q_umrah"), hajjSteps(HajjType.QIRAN).take(2).map { it.id })
        assertEquals("Rami and Halq", hajjSteps(HajjType.IFRAD).first { it.id == "day10" }.title)
        assertEquals("Rami, Qurbani, Halq", hajjSteps(HajjType.QIRAN).first { it.id == "day10" }.title)
        HajjType.entries.forEach { type ->
            val ids = hajjSteps(type).map { it.id }
            assertEquals(ids.distinct(), ids)
            assertTrue(ids.indexOf("arafat") < ids.indexOf("muzdalifah"))
            assertTrue(ids.indexOf("day10") < ids.indexOf("ziyarah"))
            assertEquals("wida", ids.last())
        }
    }

    @Test fun nextPendingStepSkipsCompletedOnes() {
        assertEquals("tawaf", nextPendingStep(umrahSteps, setOf("ihram"))?.id)
        assertNull(nextPendingStep(umrahSteps, umrahSteps.map { it.id }.toSet()))
    }

    @Test fun everyGuideStringHasUrduHindiAndHinglishCopy() {
        val strings = buildSet {
            (umrahSteps + HajjType.entries.flatMap(::hajjSteps)).forEach { step ->
                step.badge?.let(::add)
                add(step.title); add(step.detail); addAll(step.points)
                step.ruling?.let { add(it.label) }
                step.dua?.let { add(it.meaning) }
                step.counter?.let { add(it.unit); add(it.countLabel) }
            }
            HajjType.entries.forEach { add(it.label); add(it.summary) }
            CounterKind.entries.forEach { kind -> (0..kind.total).forEach { add(counterStage(kind, it).hint) } }
            essentialSections.forEach { section -> add(section.title); add(section.summary); section.lines.forEach { add(it.text) } }
            preparationChecklist.forEach { group -> add(group.title); group.items.forEach { add(it.text) } }
            madinahPlaces.forEach { add(it.title); add(it.detail) }
        }
        listOf("hinglish", "hi", "ur").forEach { language ->
            val phrases = dailyToolPhrases.getValue(language) + hajjUmrahPhrases.getValue(language)
            val missing = strings.filterNot(phrases::containsKey)
            assertTrue("$language missing: $missing", missing.isEmpty())
        }
    }
}
