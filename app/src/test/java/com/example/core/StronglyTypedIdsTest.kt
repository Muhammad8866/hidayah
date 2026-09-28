package com.example.core.id

import org.junit.Assert.*
import org.junit.Test

class StronglyTypedIdsTest {

    @Test
    fun testSurahNumberBounds() {
        val valid = SurahNumber(1)
        assertEquals(1, valid.value)

        val surah114 = SurahNumber(114)
        assertEquals(114, surah114.value)

        assertThrows(IllegalArgumentException::class.java) {
            SurahNumber(0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SurahNumber(115)
        }
    }

    @Test
    fun testAyahReferenceParsing() {
        val ref = AyahReference.of(2, 255)
        assertEquals("2:255", ref.toString())
        assertEquals(2, ref.surah.value)
        assertEquals(255, ref.ayah.value)

        val parsed = AyahReference.parseOrNull("36:58")
        assertNotNull(parsed)
        assertEquals(36, parsed?.surah?.value)
        assertEquals(58, parsed?.ayah?.value)

        assertNull(AyahReference.parseOrNull("invalid"))
        assertNull(AyahReference.parseOrNull("0:10"))
        assertNull(AyahReference.parseOrNull("200:1"))
    }

    @Test
    fun testGeoCoordinateInvariants() {
        val mecca = GeoCoordinate(21.4225, 39.8262)
        assertEquals(21.4225, mecca.latitude, 0.0001)
        assertEquals(39.8262, mecca.longitude, 0.0001)

        assertThrows(IllegalArgumentException::class.java) {
            GeoCoordinate(95.0, 0.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GeoCoordinate(0.0, 190.0)
        }
    }
}
