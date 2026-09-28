package com.example.feature.communitymasjid.domain

import com.example.domain.model.AsrJuristicMethod
import com.example.domain.model.PrayerTimeConfig
import com.example.feature.communitymasjid.model.CmPrayer
import com.example.feature.communitymasjid.model.CmTimings
import com.example.prayer.AstronomicalPrayerCalculator
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure business rules for community timings. The numbers here are mirrored in
 * `firestore.rules`; change both together.
 */
object CmTimingRules {
    const val PRESENCE_RADIUS_METRES = 200
    const val MAX_LOCATION_ACCURACY_METRES = 100
    /** Down votes needed (and they must outnumber up votes) to hide a masjid or revert a timing. */
    const val VOTE_THRESHOLD = 3
    const val MAX_AZAN_TO_JAMAAT_MINUTES = 90

    /** The masjid is placed at the adder's GPS fix, so adding needs a tighter fix than confirming. */
    const val ADD_MOSQUE_MAX_ACCURACY_METRES = 50
    /** Adding is refused when an already listed masjid is this close (likely the same one). */
    const val DUPLICATE_MOSQUE_RADIUS_METRES = 40
    const val MOSQUE_NAME_MIN = 3
    const val MOSQUE_NAME_MAX = 120
    const val MOSQUE_ADDRESS_MAX = 200

    /** Allowed slack against the calculated start, for rounding and minor method differences. */
    private const val START_TOLERANCE_MINUTES = 2
    /** Eid salah is makrooh right after sunrise; it may start once the sun has risen this far. */
    private const val EID_AFTER_SUNRISE_MINUTES = 15

    private val hhmm = Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$")

    fun isValidHm(value: String): Boolean = hhmm.matches(value)

    fun minutes(value: String): Int = value.substring(0, 2).toInt() * 60 + value.substring(3, 5).toInt()

    fun format(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

    /** Returns the list of problems; empty means valid. */
    fun validate(
        timings: CmTimings,
        latitude: Double,
        longitude: Double,
        today: Calendar = Calendar.getInstance(TimeZone.getDefault())
    ): List<String> {
        val errors = mutableListOf<String>()
        CmPrayer.daily.forEach { if (timings[it] == null) errors += "${it.label}: time is required" }
        timings.times.forEach { (prayer, time) ->
            if (!isValidHm(time.azan) || !isValidHm(time.jamaat)) {
                errors += "${prayer.label}: invalid time format"
                return@forEach
            }
            val azan = minutes(time.azan)
            val jamaat = minutes(time.jamaat)
            if (jamaat < azan) errors += "${prayer.label}: Jamaat cannot be before Azan"
            if (jamaat - azan > MAX_AZAN_TO_JAMAAT_MINUTES) {
                errors += "${prayer.label}: Jamaat is more than $MAX_AZAN_TO_JAMAAT_MINUTES min after Azan"
            }
        }
        if (errors.isEmpty()) errors += astronomicalProblems(timings, latitude, longitude, today)
        return errors
    }

    /**
     * Rejects times outside the real prayer window at the masjid's coordinates. Uses the most
     * lenient common conventions (earliest Fajr/Isha angles, earliest Asr) so no valid local
     * practice is rejected; only clearly impossible times are.
     */
    private fun astronomicalProblems(
        timings: CmTimings,
        latitude: Double,
        longitude: Double,
        today: Calendar
    ): List<String> {
        val lenient = runCatching {
            AstronomicalPrayerCalculator.calculateTimes(
                today, latitude, longitude,
                PrayerTimeConfig(
                    asrMethod = AsrJuristicMethod.STANDARD,
                    fajrAngleOverride = 19.5,
                    ishaAngleOverride = 15.0,
                    maghribIhtiyatMin = 0
                )
            )
        }.getOrNull() ?: return emptyList()
        val hanafiAsr = runCatching {
            AstronomicalPrayerCalculator.calculateTimes(
                today, latitude, longitude, PrayerTimeConfig(asrMethod = AsrJuristicMethod.HANAFI, maghribIhtiyatMin = 0)
            ).asrHours
        }.getOrNull() ?: lenient.asrHours
        // Near the poles some angles are never reached; skip the check rather than reject everything.
        val all = listOf(lenient.fajrHours, lenient.sunriseHours, lenient.dhuhrHours, lenient.asrHours,
            lenient.maghribHours, lenient.ishaHours, hanafiAsr)
        if (all.any { it.isNaN() || it.isInfinite() }) return emptyList()

        fun toMin(hours: Double) = (hours * 60).toInt()
        val fajrStart = toMin(lenient.fajrHours)
        val sunrise = toMin(lenient.sunriseHours)
        val dhuhrStart = toMin(lenient.dhuhrHours)
        val asrStart = toMin(lenient.asrHours)
        val maghribStart = toMin(lenient.maghribHours)
        val ishaStart = toMin(lenient.ishaHours)

        val problems = mutableListOf<String>()
        fun checkStart(prayer: CmPrayer, start: Int) {
            val time = timings[prayer] ?: return
            if (minutes(time.jamaat) < start - START_TOLERANCE_MINUTES) {
                problems += "${prayer.label}: Jamaat ${time.jamaat} is before the prayer starts (${format(start)})"
            }
            if (minutes(time.azan) < start - START_TOLERANCE_MINUTES) {
                problems += "${prayer.label}: Azan ${time.azan} is before the prayer starts (${format(start)})"
            }
        }
        fun checkEnd(prayer: CmPrayer, end: Int, endName: String) {
            val time = timings[prayer] ?: return
            if (minutes(time.jamaat) >= end) {
                problems += "${prayer.label}: Jamaat ${time.jamaat} is after $endName (${format(end)})"
            }
        }
        checkStart(CmPrayer.FAJR, fajrStart); checkEnd(CmPrayer.FAJR, sunrise, "sunrise")
        checkStart(CmPrayer.DHUHR, dhuhrStart); checkEnd(CmPrayer.DHUHR, toMin(hanafiAsr), "Asr time")
        checkStart(CmPrayer.ASR, asrStart); checkEnd(CmPrayer.ASR, maghribStart, "sunset")
        checkStart(CmPrayer.MAGHRIB, maghribStart)
        checkStart(CmPrayer.ISHA, ishaStart)
        checkStart(CmPrayer.JUMUAH, dhuhrStart); checkEnd(CmPrayer.JUMUAH, toMin(hanafiAsr), "Asr time")
        for (eid in listOf(CmPrayer.EID_FITR, CmPrayer.EID_ADHA)) {
            checkStart(eid, sunrise + EID_AFTER_SUNRISE_MINUTES); checkEnd(eid, dhuhrStart, "Zuhr time")
        }
        // Taraweeh may run past midnight, so only evening times are checked against Isha.
        timings[CmPrayer.TARAWEEH]?.let { time ->
            val jamaat = minutes(time.jamaat)
            if (jamaat >= 12 * 60 && jamaat < ishaStart - START_TOLERANCE_MINUTES) {
                problems += "${CmPrayer.TARAWEEH.label}: ${time.jamaat} is before Isha time (${format(ishaStart)})"
            }
        }
        return problems
    }

    fun isSameAs(a: CmTimings, b: CmTimings?): Boolean = b != null && a.times == b.times

    /** Mirrors `hidden()` / the revert condition in firestore.rules. */
    fun isVotedDown(up: Int, down: Int): Boolean = down >= VOTE_THRESHOLD && down > up

    fun distanceMetres(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return 6_371_000.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
