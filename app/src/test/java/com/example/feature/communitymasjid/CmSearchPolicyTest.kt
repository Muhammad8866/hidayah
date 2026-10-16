package com.example.feature.communitymasjid

import com.example.feature.communitymasjid.domain.CmSearchBudget
import com.example.feature.communitymasjid.domain.CmSearchPolicy
import com.example.feature.communitymasjid.model.CmUserLocation
import org.junit.Assert.*
import org.junit.Test

class CmSearchPolicyTest {
    private val noon = 1_791_547_200_000L
    private fun location(lat: Double = 22.5) = CmUserLocation(lat, 70.4, 10f, noon)
    private fun budget(at: Long = noon, count: Int = 1) = CmSearchBudget(CmSearchPolicy.day(at), count, at, 22.5, 70.4)

    @Test fun firstSearchIsAllowed() {
        assertNull(CmSearchPolicy.blocked(noon, location(), null))
    }

    @Test fun travelCannotBypassCooldown() {
        assertEquals(CmSearchPolicy.Block.COOLDOWN,
            CmSearchPolicy.blocked(noon + 14 * 60_000, location(22.52), budget()))
        assertNull(CmSearchPolicy.blocked(noon + 15 * 60_000, location(22.52), budget()))
    }

    @Test fun stationaryUserCannotSearchAgainThatDay() {
        assertEquals(CmSearchPolicy.Block.SAME_AREA,
            CmSearchPolicy.blocked(noon + 60 * 60_000, location(), budget()))
        assertEquals(CmSearchPolicy.Block.SAME_AREA,
            CmSearchPolicy.blocked(noon + 60 * 60_000, location(22.503), budget()))
    }

    @Test fun travelAllowsAtMostFiveTotalRequests() {
        var state: CmSearchBudget? = null
        for (i in 0 until 5) {
            val at = noon + i * 16 * 60_000
            val place = location(22.5 + i * 0.01)
            assertNull(CmSearchPolicy.blocked(at, place, state))
            state = CmSearchPolicy.next(at, place, state)
            assertEquals(i + 1, state.count)
        }
        assertEquals(CmSearchPolicy.Block.DAILY_LIMIT,
            CmSearchPolicy.blocked(noon + 5 * 16 * 60_000, location(22.56), state))
    }

    @Test fun stationarySearchResetsOnNextIndiaDay() {
        val nextDay = noon + 24 * 60 * 60_000
        assertNull(CmSearchPolicy.blocked(nextDay, location(), budget()))
        assertEquals(1, CmSearchPolicy.next(nextDay, location(), budget(count = 5)).count)
    }

    @Test fun indiaMidnightIsTheBoundaryAndCooldownStillApplies() {
        // 1970-01-01 18:30 UTC is midnight on January 2 in India.
        val midnight = 18 * 60 * 60_000L + 30 * 60_000L
        assertEquals(0L, CmSearchPolicy.day(midnight - 1))
        assertEquals(1L, CmSearchPolicy.day(midnight))
        assertEquals(CmSearchPolicy.Block.COOLDOWN,
            CmSearchPolicy.blocked(midnight, location(), budget(midnight - 60_000)))
    }

    @Test fun clockRollbackDoesNotAllowAnotherRequest() {
        assertEquals(CmSearchPolicy.Block.COOLDOWN,
            CmSearchPolicy.blocked(noon - 60_000, location(22.52), budget()))
    }
}
