package com.example.ui.screens

import org.junit.Assert.*
import org.junit.Test

class MirathCalculatorTest {
    private fun input() = MirathInput(estate = "120000", confirmed = true)
    private fun fraction(result: MirathResult, heir: String) = result.shares.first { it.heir == heir }.fraction.toString()

    @Test fun wifeParentsAndChildren() {
        val r = calculateMirath(input().copy(spouses = 1, father = true, mother = true, sons = 1, daughters = 1))
        assertNull(r.error)
        assertEquals("1/8", fraction(r, "Wife"))
        assertEquals("1/6", fraction(r, "Father"))
        assertEquals("1/6", fraction(r, "Mother"))
        assertEquals("13/36", fraction(r, "Son"))
        assertEquals("13/72", fraction(r, "Daughter"))
        assertEquals(0L, r.remainder.numerator)
    }

    @Test fun roundingDifferenceUsesIndividualAmounts() {
        val below = calculateMirath(input().copy(estate = "100", sons = 3))
        assertEquals("0.01", below.roundingDifference.toPlainString())
        val above = calculateMirath(input().copy(estate = "100", sons = 6))
        assertEquals("-0.02", above.roundingDifference.toPlainString())
        val exact = calculateMirath(input().copy(sons = 1))
        assertEquals(0, exact.roundingDifference.signum())
    }

    @Test fun awlReducesSharesProportionally() {
        val r = calculateMirath(input().copy(male = false, spouses = 1, father = true, mother = true, daughters = 2))
        assertEquals("1/5", fraction(r, "Husband"))
        assertEquals("2/15", fraction(r, "Father"))
        assertEquals("8/15", fraction(r, "Daughter"))
        assertTrue(r.adjustment!!.startsWith("Awl"))
        assertEquals(0L, r.remainder.numerator)
    }

    @Test fun motherTakesThirdOfRemainderWithSpouseAndFather() {
        val r = calculateMirath(input().copy(male = false, spouses = 1, father = true, mother = true))
        assertEquals("1/6", fraction(r, "Mother"))
        assertEquals("1/3", fraction(r, "Father"))
    }

    @Test fun excludedSiblingsStillReduceMotherShare() {
        val r = calculateMirath(input().copy(spouses = 1, father = true, mother = true, siblings = 2))
        assertEquals("1/6", fraction(r, "Mother"))
        assertEquals("7/12", fraction(r, "Father"))
    }

    @Test fun raddExcludesSpouse() {
        val r = calculateMirath(input().copy(spouses = 1, daughters = 1, mother = true))
        assertEquals("1/8", fraction(r, "Wife"))
        assertEquals("21/32", fraction(r, "Daughter"))
        assertEquals("7/32", fraction(r, "Mother"))
        assertTrue(r.adjustment!!.startsWith("Radd"))
    }

    @Test fun fatherReceivesResidueWithDaughter() {
        val r = calculateMirath(input().copy(father = true, daughters = 1))
        assertEquals("1/2", fraction(r, "Father"))
    }

    @Test fun wivesShareCollectively() {
        val r = calculateMirath(input().copy(spouses = 4, sons = 1))
        assertEquals("1/32", r.shares.first { it.heir == "Wife" }.each.toString())
    }

    @Test fun deductionsAndBequestLimit() {
        val r = calculateMirath(input().copy(funeral = "10000", debts = "20000", bequest = "30000", sons = 1))
        assertEquals("60000", r.net.toPlainString())
        assertNotNull(calculateMirath(input().copy(funeral = "10000", debts = "20000", bequest = "30000.01", sons = 1)).error)
    }

    @Test fun incompleteAndSpecialCasesBlocked() {
        assertNotNull(calculateMirath(input().copy(confirmed = false)).error)
        assertNotNull(calculateMirath(input().copy(otherHeirs = true, sons = 1)).error)
        assertNotNull(calculateMirath(input().copy(siblings = 1, daughters = 1)).error)
        assertEquals("1/2", calculateMirath(input().copy(male = false, spouses = 1)).remainder.toString())
    }

    @Test fun rejectsInvalidInputs() {
        listOf("-1", "NaN", "1,000", "1.001", "1e3", "1E-100000", "1,00", "+100", "100.").forEach { assertNotNull(calculateMirath(input().copy(estate = it)).error) }
        assertNotNull(calculateMirath(input().copy(male = false, spouses = 2)).error)
        assertNotNull(calculateMirath(input().copy(debts = "120000")).error)
    }

    @Test fun localDigitsAndWhitespaceAreAccepted() {
        assertEquals("120000.50", parseMirathAmount(" १२००००.५० ")!!.toPlainString())
        assertEquals("120000.50", parseMirathAmount("١٢٠٠٠٠.٥٠")!!.toPlainString())
        assertNull(calculateMirath(input().copy(estate = " ١٢٠٠٠٠ ", sons = 1)).error)
        assertEquals("0", parseMirathAmount(" ")!!.toPlainString())
    }

    @Test fun everySupportedFamilyConservesEstate() {
        for (male in listOf(true, false)) for (spouses in 0..(if (male) 4 else 1))
            for (sons in 0..3) for (daughters in 0..3) for (father in listOf(true, false))
                for (mother in listOf(true, false)) for (siblings in 0..2) {
                    val r = calculateMirath(input().copy(male = male, spouses = spouses, sons = sons,
                        daughters = daughters, father = father, mother = mother, siblings = siblings))
                    if (r.error != null) continue
                    val total = r.shares.fold(r.remainder) { a, share ->
                        assertTrue(share.fraction.numerator >= 0)
                        assertEquals(share.fraction, share.each * MirathFraction.of(share.count.toLong()))
                        a + share.fraction
                    }
                    assertEquals(MirathFraction.of(1), total)
                    if (sons > 0 && daughters > 0) assertEquals(
                        r.shares.first { it.heir == "Son" }.each,
                        r.shares.first { it.heir == "Daughter" }.each * MirathFraction.of(2))
                }
    }
}
