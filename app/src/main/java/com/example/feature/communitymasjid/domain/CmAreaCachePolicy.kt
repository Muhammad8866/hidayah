package com.example.feature.communitymasjid.domain

import com.example.feature.communitymasjid.model.CmUserLocation

/** Reuse a community search only near its original point and before its three-day expiry. */
object CmAreaCachePolicy {
    const val TTL_MS = 3L * 24 * 60 * 60 * 1000
    private const val REUSE_METRES = 500.0

    fun isReusable(
        location: CmUserLocation,
        queryLatitude: Double,
        queryLongitude: Double,
        fetchedAtMs: Long,
        nowMs: Long
    ): Boolean = fetchedAtMs <= nowMs && nowMs - fetchedAtMs < TTL_MS &&
        CmTimingRules.distanceMetres(
            location.latitude, location.longitude, queryLatitude, queryLongitude
        ) <= REUSE_METRES
}
