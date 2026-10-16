package com.example.ui.screens

import com.example.quran.FullQuranCatalog
import org.junit.Assert.*
import org.junit.Test

class RecitationCollectionTest {
    @Test fun manzilContainsCompleteTraditionalSelection() {
        assertEquals(18, RecitationCollection.manzil.size)
        assertEquals(79, RecitationCollection.manzil.sumOf { it.last - it.first + 1 })
        assertEquals(RecitationPassage(1, 1, 7), RecitationCollection.manzil.first())
        assertEquals(RecitationPassage(114, 1, 6), RecitationCollection.manzil.last())
        assertEquals(18, RecitationCollection.manzil.map { it.id }.toSet().size)
        (RecitationCollection.manzil + RecitationCollection.ruqyah).forEach { passage ->
            val surah = FullQuranCatalog.surahs.first { it.number == passage.surah }
            assertTrue(passage.first in 1..passage.last)
            assertTrue(passage.last <= surah.totalAyat)
        }
    }

    @Test fun duaCollectionsHaveSourcesAndValidCounts() {
        assertTrue(RecitationCollection.ruqyahDuas.isNotEmpty())
        assertTrue(RecitationCollection.wazaif.isNotEmpty())
        (RecitationCollection.ruqyahDuas + RecitationCollection.wazaif).forEach {
            assertTrue(it.arabicText.isNotBlank())
            assertTrue(it.sourceReference.isNotBlank())
            assertTrue(it.recommendedCount > 0)
        }
    }
}
