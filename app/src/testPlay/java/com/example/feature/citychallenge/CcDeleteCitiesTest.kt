package com.example.feature.citychallenge

import org.junit.Assert.assertEquals
import org.junit.Test

class CcDeleteCitiesTest {
    @Test
    fun includesEveryPublishedCityAndTheCurrentOne() {
        assertEquals(
            listOf("pk-karachi", "in-delhi", "pk-lahore"),
            ccCitiesToDelete("pk-karachi", linkedSetOf("in-delhi", "pk-lahore", "pk-karachi"))
        )
    }

    @Test
    fun deletesPublishedCitiesEvenWithoutAProfile() {
        assertEquals(listOf("in-delhi"), ccCitiesToDelete(null, setOf("in-delhi")))
    }

    @Test
    fun skipsBlankIdsAndReturnsEmptyWhenNothingKnown() {
        assertEquals(emptyList<String>(), ccCitiesToDelete(null, emptySet()))
        assertEquals(listOf("pk-lahore"), ccCitiesToDelete(" ", setOf("", "pk-lahore")))
    }
}
