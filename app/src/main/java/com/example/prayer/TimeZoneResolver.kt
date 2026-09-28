package com.example.prayer

import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Chooses the timezone for a location instead of blindly trusting the phone's zone, which is
 * wrong for coordinates in another country or a phone with a mis-set zone.
 */
object TimeZoneResolver {

    /** The phone's zone is kept when it belongs to the location's country; else the country zone nearest in longitude. */
    fun resolve(
        longitude: Double,
        countryCode: String?,
        deviceZone: TimeZone = TimeZone.getDefault(),
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        val country = countryCode?.trim()?.uppercase(Locale.ROOT)?.takeIf { it.length == 2 }
        val countryZones = country?.let {
            runCatching { android.icu.util.TimeZone.getAvailableIDs(it).toList() }.getOrNull()
        }.orEmpty()
        if (countryZones.isNotEmpty()) {
            if (deviceZone.id in countryZones) return deviceZone.id
            return countryZones.minBy { id -> offsetDistanceHours(TimeZone.getTimeZone(id), longitude, nowMillis) }
        }
        // Offline (no country): keep the phone's zone unless it is clearly on another continent.
        if (offsetDistanceHours(deviceZone, longitude, nowMillis) <= MAX_PLAUSIBLE_OFFSET_ERROR_HOURS) {
            return deviceZone.id
        }
        val hours = (longitude / 15.0).roundToInt().coerceIn(-12, 14)
        // Etc/GMT signs are inverted: Etc/GMT-5 is UTC+5.
        return when {
            hours == 0 -> "Etc/GMT"
            hours > 0 -> "Etc/GMT-$hours"
            else -> "Etc/GMT+${-hours}"
        }
    }

    private fun offsetDistanceHours(zone: TimeZone, longitude: Double, nowMillis: Long): Double =
        abs(zone.getOffset(nowMillis) / 3_600_000.0 - longitude / 15.0)

    private const val MAX_PLAUSIBLE_OFFSET_ERROR_HOURS = 3.5
}
