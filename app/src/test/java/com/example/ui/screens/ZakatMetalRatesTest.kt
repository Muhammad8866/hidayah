package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal

/** Robolectric supplies the real org.json that the rate parser uses. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ZakatMetalRatesTest {
    @Test fun liveRatesAreConvertedFromTroyOuncesToTheChosenUnit() {
        val body = """{"date":"2026-09-26","xau":{"pkr":1200000.5,"usd":4286.2}}"""
        val (date, prices) = ZakatMetalRates.parse(body, "xau", listOf("PKR", "USD", "INR"))
        assertEquals("2026-09-26", date.toString())
        assertEquals(setOf("PKR", "USD"), prices.keys)
        val snapshot = ZakatMetalRates.Snapshot(date, 0L, prices, emptyMap())
        // 4286.2 / 31.1034768 × 11.664 = 1607.35 per tola.
        assertEquals(BigDecimal("1607.35"), snapshot.rate(gold = true, "USD", MetalUnit.TOLA))
        assertEquals(BigDecimal("137.80"), snapshot.rate(gold = true, "USD", MetalUnit.GRAM))
        assertNull(snapshot.rate(gold = false, "USD", MetalUnit.GRAM))
    }
}
