package com.example.feature.communitymasjid.domain

import com.example.feature.communitymasjid.model.CmUserLocation

data class CmSearchBudget(
    val day: Long,
    val count: Int,
    val lastAtMs: Long,
    val latitude: Double,
    val longitude: Double
)

/** One search in an area; extra searches require travel. Days reset at midnight IST. */
object CmSearchPolicy {
    const val MAX_PER_DAY = 5
    // Kept short: Google results are memory-only, so after an app restart a long cooldown left the list empty.
    // The daily cap is the cost ceiling. Must match firestore.rules (cm_search_quotas).
    const val COOLDOWN_MS = 2 * 60_000L
    const val TRAVEL_METRES = 2_000.0
    private const val DAY_MS = 24 * 60 * 60_000L
    private const val IST_OFFSET_MS = 330 * 60_000L

    enum class Block { COOLDOWN, SAME_AREA, DAILY_LIMIT }

    fun day(nowMs: Long): Long = Math.floorDiv(nowMs + IST_OFFSET_MS, DAY_MS)

    /**
     * [hasResults] is false when no Google results are held in memory. Google content may not be
     * persisted, so the once-per-area rule would leave the tab empty for the rest of the day after
     * an app restart; then only the daily cap and the cooldown (also enforced by Firestore rules) apply.
     */
    fun blocked(nowMs: Long, location: CmUserLocation, budget: CmSearchBudget?, hasResults: Boolean = true): Block? {
        if (budget == null) return null
        val sameDay = budget.day == day(nowMs)
        if (sameDay && budget.count >= MAX_PER_DAY) return Block.DAILY_LIMIT
        if (nowMs - budget.lastAtMs < COOLDOWN_MS) return Block.COOLDOWN
        if (hasResults && sameDay && CmTimingRules.distanceMetres(
                budget.latitude, budget.longitude, location.latitude, location.longitude
            ) < TRAVEL_METRES) return Block.SAME_AREA
        return null
    }

    fun next(nowMs: Long, location: CmUserLocation, budget: CmSearchBudget?) = CmSearchBudget(
        day = day(nowMs),
        count = if (budget?.day == day(nowMs)) budget.count + 1 else 1,
        lastAtMs = nowMs,
        latitude = location.latitude,
        longitude = location.longitude
    )

    fun message(block: Block, nowMs: Long, budget: CmSearchBudget): String = when (block) {
        // COOLDOWN_MS is at most 2 minutes, so the UI localizes each whole sentence exactly.
        Block.COOLDOWN -> when (val minutes = ((COOLDOWN_MS - (nowMs - budget.lastAtMs)).coerceAtLeast(1) + 59_999) / 60_000) {
            1L -> "Please wait 1 minute before another nearby search."
            else -> "Please wait $minutes minutes before another nearby search."
        }
        Block.SAME_AREA -> "Today's search for this area is already used. Travel at least 2 km for another search."
        Block.DAILY_LIMIT -> "Today's 5 nearby searches are used. New searches are available after midnight (India time)."
    }
}
