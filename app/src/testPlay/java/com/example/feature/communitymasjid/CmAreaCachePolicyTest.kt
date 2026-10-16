package com.example.feature.communitymasjid

import com.example.feature.communitymasjid.domain.CmAreaCachePolicy
import com.example.feature.communitymasjid.model.CmUserLocation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CmAreaCachePolicyTest {
    private val center = CmUserLocation(28.6139, 77.209, 10f, 0L)

    @Test
    fun reusesNearbyAreaUntilThreeDaysThenExpires() {
        val fetched = 1_000_000L
        val nearby = center.copy(latitude = center.latitude + 0.002)
        assertTrue(CmAreaCachePolicy.isReusable(nearby, center.latitude, center.longitude, fetched, fetched + CmAreaCachePolicy.TTL_MS - 1))
        assertFalse(CmAreaCachePolicy.isReusable(nearby, center.latitude, center.longitude, fetched, fetched + CmAreaCachePolicy.TTL_MS))
        assertFalse(CmAreaCachePolicy.isReusable(nearby, center.latitude, center.longitude, fetched, fetched - 1))
    }

    @Test
    fun movingToAnotherAreaNeedsAnotherSnapshot() {
        val moved = center.copy(latitude = center.latitude + 0.01)
        assertFalse(CmAreaCachePolicy.isReusable(moved, center.latitude, center.longitude, 1000L, 2000L))
    }
}
