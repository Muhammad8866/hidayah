package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class ZakatCalculationTest {
    private val grams = ZakatInput(unit = MetalUnit.GRAM, silverRate = "1", goldRate = "2")

    @Test fun exactNisabIsEligibleAfterHawl() {
        val result = calculateZakat(grams.copy(cash = "612.36", lunarYearPassed = true))
        assertEquals(BigDecimal("612.36"), result.nisab)
        assertEquals(BigDecimal("15.31"), result.due)
        assertEquals(ZakatStatus.DUE, result.status)
        assertEquals(1f, result.nisabProgress)
    }

    @Test fun liabilityAndHawlGateTheDueAmount() {
        val beforeHawl = calculateZakat(grams.copy(cash = "1000.00", business = "250.00", debts = "100.00"))
        assertEquals(BigDecimal("1150.00"), beforeHawl.net)
        assertEquals(BigDecimal("28.75"), beforeHawl.estimate)
        assertEquals(ZakatStatus.AWAITING_HAWL, beforeHawl.status)
        assertNull(beforeHawl.due)

        val belowNisab = calculateZakat(grams.copy(cash = "1000", debts = "900", lunarYearPassed = true))
        assertEquals(ZakatStatus.BELOW_NISAB, belowNisab.status)
        assertNull(belowNisab.due)
    }

    @Test fun missingRateAndExcessDebtDoNotProduceDue() {
        val result = calculateZakat(ZakatInput(cash = "50", debts = "100", lunarYearPassed = true))
        assertEquals(BigDecimal.ZERO, result.net)
        assertNull(result.nisab)
        assertEquals(ZakatStatus.NEED_SILVER_RATE, result.status)
        assertNull(result.due)
    }

    @Test fun metalWeightNeedsItsRate() {
        val result = calculateZakat(ZakatInput(goldWeight = "10", cash = "5000", silverRate = "200"))
        assertNull(result.goldValue)
        assertEquals(ZakatStatus.NEED_GOLD_RATE, result.status)
    }

    @Test fun manualBasisUsesItsOwnNisabWeight() {
        val silver = calculateZakat(grams.copy(cash = "1000", silverRate = "2", nisabMode = ZakatNisabMode.SILVER, lunarYearPassed = true))
        val gold = calculateZakat(grams.copy(cash = "1000", nisabMode = ZakatNisabMode.GOLD, lunarYearPassed = true))
        assertEquals(BigDecimal("1224.72"), silver.nisab)
        assertEquals(BigDecimal("174.96"), gold.nisab)
        assertNull(silver.due)
        assertEquals(BigDecimal("25.00"), gold.due)
    }

    @Test fun autoUsesGoldNisabOnlyWhenGoldIsTheOnlyWealth() {
        // 7 tola gold alone is below 7.5 tola, so nothing is due.
        val goldOnly = ZakatInput(goldWeight = "7", goldRate = "200000", silverRate = "2500", lunarYearPassed = true)
        val alone = calculateZakat(goldOnly)
        assertEquals(ZakatNisabBasis.GOLD, alone.basis)
        assertEquals(BigDecimal("1500000.00"), alone.nisab)
        assertEquals(ZakatStatus.BELOW_NISAB, alone.status)

        // The same gold with a little cash is measured against 52.5 tola of silver.
        val mixed = calculateZakat(goldOnly.copy(cash = "1000"))
        assertEquals(ZakatNisabBasis.SILVER, mixed.basis)
        assertEquals(BigDecimal("131250.00"), mixed.nisab)
        assertEquals(BigDecimal("1401000.00"), mixed.net)
        assertEquals(BigDecimal("35025.00"), mixed.due)
    }

    @Test fun tolaRatesConvertToTheSameNisabAsGrams() {
        val tola = calculateZakat(ZakatInput(cash = "1", silverRate = "11.664"))
        assertEquals(BigDecimal("612.36"), tola.nisab)
        assertEquals(BigDecimal("52.5"), nisabWeight(ZakatNisabBasis.SILVER, MetalUnit.TOLA))
        assertEquals(BigDecimal("7.5"), nisabWeight(ZakatNisabBasis.GOLD, MetalUnit.TOLA))
    }

    @Test fun emptyFormHasNoStatusYet() {
        assertEquals(ZakatStatus.EMPTY, calculateZakat(ZakatInput(goldRate = "1")).status)
    }

    @Test fun karatScalesGoldValueAndGoldAloneUsesItsOwnWeightForNisab() {
        // 8 tola of 22K at a 24K rate of 240,000: 8 × 220,000 = 1,760,000.
        val jewellery = ZakatInput(goldWeight = "8", goldRate = "240000", silverRate = "2500", goldKarat = GoldKarat.K22, lunarYearPassed = true)
        val alone = calculateZakat(jewellery)
        assertEquals(BigDecimal("1760000.00"), alone.goldValue)
        // Nisab is 7.5 tola of the same 22K gold, so 8 tola reaches it.
        assertEquals(BigDecimal("1650000.00"), alone.nisab)
        assertEquals(ZakatStatus.DUE, alone.status)
        assertEquals(BigDecimal("44000.00"), alone.due)

        val mixedGold = calculateZakat(jewellery.copy(cash = "1", nisabMode = ZakatNisabMode.GOLD))
        assertEquals(BigDecimal("1800000.00"), mixedGold.nisab)
    }

    @Test fun hawlFallsDueOnTheHijriAnniversary() {
        val start = HijriDay(1446, 9, 12)
        val before = hawlState(start, HijriDay(1447, 9, 11))
        assertEquals(false, before.passed)
        assertEquals(HijriDay(1447, 9, 12), before.nextDue)

        val onTheDay = hawlState(start, HijriDay(1447, 9, 12))
        assertEquals(true, onTheDay.passed)
        assertEquals(HijriDay(1447, 9, 12), onTheDay.lastDue)
        assertEquals(HijriDay(1448, 9, 12), onTheDay.nextDue)

        val yearsLater = hawlState(start, HijriDay(1450, 3, 1))
        assertEquals(HijriDay(1449, 9, 12), yearsLater.lastDue)
        assertEquals(HijriDay(1450, 9, 12), yearsLater.nextDue)
        assertEquals(start, HijriDay.parse(start.toString()))
    }
}
