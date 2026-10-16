package com.example.prayer

import com.example.domain.model.CalculationMethod
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.PrayerTimeConfig
import com.example.prayer.domain.policy.HighLatitudePolicy
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

/**
 * Offline astronomical prayer-time calculator.
 *
 * Solar coordinates use the NOAA/Meeus equations and are re-evaluated at each
 * event instead of using one low-precision declination value for the whole day.
 * Times are local wall-clock hours. Isha is intentionally allowed to be >= 24
 * when a high-latitude night places it after civil midnight.
 */
object AstronomicalPrayerCalculator {

    data class SolarEphemeris(
        val julianDate: Double,
        val declinationDeg: Double,
        val equationOfTimeMinutes: Double,
        val solarNoonHours: Double,
        val rightAscensionHours: Double
    )

    data class RawPrayerTimes(
        val fajrHours: Double,
        val sunriseHours: Double,
        val dhuhrHours: Double,
        val asrHours: Double,
        val maghribHours: Double,
        val ishaHours: Double,
        /** Last minute to eat Sehri: Fajr rounded down, whereas [fajrHours] is rounded up. */
        val sehriEndHours: Double = fajrHours
    )

    private data class SolarCoordinates(
        val declinationDeg: Double,
        val equationOfTimeMinutes: Double,
        val rightAscensionHours: Double
    )

    private data class HorizonTimes(val sunriseHours: Double, val sunsetHours: Double)

    fun calculateSolarParameters(
        calendar: Calendar,
        latitude: Double,
        longitude: Double
    ): SolarEphemeris {
        validateCoordinates(latitude, longitude)
        val date = localNoon(calendar)
        val baseJulianDate = julianDate(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH)
        )
        val offsetMinutes = date.timeZone.getOffset(date.timeInMillis) / 60_000.0

        var noonMinutes = 720.0 - (4.0 * longitude) + offsetMinutes
        var coordinates = solarCoordinates(baseJulianDate + (noonMinutes - offsetMinutes) / 1440.0)
        repeat(2) {
            noonMinutes = 720.0 - (4.0 * longitude) - coordinates.equationOfTimeMinutes + offsetMinutes
            coordinates = solarCoordinates(baseJulianDate + (noonMinutes - offsetMinutes) / 1440.0)
        }

        return SolarEphemeris(
            julianDate = baseJulianDate,
            declinationDeg = coordinates.declinationDeg,
            equationOfTimeMinutes = coordinates.equationOfTimeMinutes,
            solarNoonHours = noonMinutes / 60.0,
            rightAscensionHours = coordinates.rightAscensionHours
        )
    }

    fun calculateTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        config: PrayerTimeConfig
    ): RawPrayerTimes {
        validateCoordinates(latitude, longitude)
        validateConfig(config)
        val date = localNoon(calendar)
        val ephemeris = calculateSolarParameters(date, latitude, longitude)
        val horizon = horizonTimes(date, latitude, longitude)
            ?: throw IllegalStateException(
                "Sunrise or sunset does not occur on this date at the selected coordinates. " +
                    "A polar-circle resolution method is required."
            )

        val fajrAngle = config.fajrAngleOverride ?: config.method.fajrAngle
        val ishaAngle = config.ishaAngleOverride ?: config.method.ishaAngle
        val calculatedFajr = solarEventHours(date, latitude, longitude, -fajrAngle, morning = true)
        var fajr = calculatedFajr ?: Double.NEGATIVE_INFINITY

        val ishaIntervalMinutes = config.method.ishaIntervalMinutes?.let { standardInterval ->
            if (config.method == CalculationMethod.MAKKAH && isRamadan(date)) standardInterval + 30
            else standardInterval
        }
        var isha = if (ishaIntervalMinutes != null) {
            horizon.sunsetHours + ishaIntervalMinutes / 60.0
        } else {
            solarEventHours(date, latitude, longitude, -ishaAngle, morning = false)
                ?: Double.POSITIVE_INFINITY
        }

        if (config.highLatitudeRule != HighLatitudeRule.NONE) {
            val previousDate = (date.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
            val nextDate = (date.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            val previousHorizon = horizonTimes(previousDate, latitude, longitude)
            val nextHorizon = horizonTimes(nextDate, latitude, longitude)
            if (previousHorizon == null || nextHorizon == null) {
                throw IllegalStateException(
                    "A neighbouring sunrise or sunset is unavailable at this polar location."
                )
            }

            val currentSunriseMillis = formatHoursToTimeInMillis(date, horizon.sunriseHours)
            val currentSunsetMillis = formatHoursToTimeInMillis(date, horizon.sunsetHours)
            val previousSunsetMillis = formatHoursToTimeInMillis(previousDate, previousHorizon.sunsetHours)
            val nextSunriseMillis = formatHoursToTimeInMillis(nextDate, nextHorizon.sunriseHours)
            val morningNightHours = (currentSunriseMillis - previousSunsetMillis) / 3_600_000.0
            val eveningNightHours = (nextSunriseMillis - currentSunsetMillis) / 3_600_000.0
            check(morningNightHours in 0.0..24.0 && eveningNightHours in 0.0..24.0) {
                "Invalid sunset-to-sunrise duration for the selected location and timezone."
            }

            val portions = HighLatitudePolicy.nightPortions(
                rule = config.highLatitudeRule,
                latitudeDeg = latitude,
                fajrAngle = fajrAngle,
                ishaAngle = ishaAngle
            )
            val safeFajrMillis = currentSunriseMillis -
                (morningNightHours * portions.first * 3_600_000.0).roundToLong()
            val safeIshaMillis = currentSunsetMillis +
                (eveningNightHours * portions.second * 3_600_000.0).roundToLong()
            val safeFajr = hoursRelativeToDate(date, safeFajrMillis)
            val safeIsha = hoursRelativeToDate(date, safeIshaMillis)

            if (!fajr.isFinite() || fajr < safeFajr || fajr >= horizon.sunriseHours) fajr = safeFajr
            if (ishaIntervalMinutes == null &&
                (!isha.isFinite() || isha > safeIsha || isha <= horizon.sunsetHours)
            ) {
                isha = safeIsha
            }
        }

        if (!fajr.isFinite() || !isha.isFinite()) {
            throw IllegalStateException(
                "Fajr or Isha twilight does not occur; enable a high-latitude adjustment rule."
            )
        }

        val asr = asrHours(date, latitude, longitude, config.asrMethod.factor)
            ?: throw IllegalStateException("The configured Asr shadow length is not reached on this date.")
        val methodDhuhrAdjustment = config.method.dhuhrAdjustmentMinutes

        // Whole minutes, rounded in the safe direction: a prayer start is never shown before the
        // real time, and the ends (Sehri, sunrise) are never shown after it.
        val fajrWithOffset = fajr + config.manualFajrOffsetMin / 60.0
        val result = RawPrayerTimes(
            fajrHours = ceilToMinute(fajrWithOffset),
            sunriseHours = floorToMinute(horizon.sunriseHours),
            dhuhrHours = ceilToMinute(
                ephemeris.solarNoonHours + (methodDhuhrAdjustment + config.manualDhuhrOffsetMin) / 60.0
            ),
            asrHours = ceilToMinute(asr + config.manualAsrOffsetMin / 60.0),
            maghribHours = ceilToMinute(
                horizon.sunsetHours + (config.maghribIhtiyatMin + config.manualMaghribOffsetMin) / 60.0
            ),
            ishaHours = ceilToMinute(isha + config.manualIshaOffsetMin / 60.0),
            sehriEndHours = floorToMinute(fajrWithOffset)
        )
        validateOrdering(result)
        return result
    }

    /** Mirrors the device's 12/24-hour setting; set by each Android entry point before formatting. */
    @Volatile
    var use24HourClock: Boolean = false

    fun formatHoursToAmPm(hours: Double, locale: Locale = Locale.getDefault()): String {
        require(hours.isFinite()) { "Time must be finite." }
        val totalMinutes = (hours * 60.0).roundToInt()
        val minuteOfDay = Math.floorMod(totalMinutes, MINUTES_PER_DAY)
        val utc = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance(utc, locale).apply {
            clear()
            set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
            set(Calendar.MINUTE, minuteOfDay % 60)
        }
        val pattern = if (use24HourClock) "HH:mm" else "h:mm a"
        return SimpleDateFormat(pattern, locale).apply { timeZone = utc }.format(calendar.time)
    }

    /** Converts local wall-clock hours to an instant, preserving values outside 0..24. */
    fun formatHoursToTimeInMillis(calendar: Calendar, hours: Double): Long {
        require(hours.isFinite()) { "Time must be finite." }
        val totalMinutes = (hours * 60.0).roundToInt()
        val dayOffset = Math.floorDiv(totalMinutes, MINUTES_PER_DAY)
        val minuteOfDay = Math.floorMod(totalMinutes, MINUTES_PER_DAY)
        return (calendar.clone() as Calendar).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, dayOffset)
            set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
            set(Calendar.MINUTE, minuteOfDay % 60)
        }.timeInMillis
    }

    private fun horizonTimes(calendar: Calendar, latitude: Double, longitude: Double): HorizonTimes? {
        val sunrise = solarEventHours(
            calendar, latitude, longitude, STANDARD_HORIZON_ALTITUDE, morning = true
        )
        val sunset = solarEventHours(
            calendar, latitude, longitude, STANDARD_HORIZON_ALTITUDE, morning = false
        )
        return if (sunrise != null && sunset != null) HorizonTimes(sunrise, sunset) else null
    }

    /** Iterates solar coordinates at the event instant, avoiding seasonal drift. */
    private fun solarEventHours(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        altitudeDeg: Double,
        morning: Boolean
    ): Double? {
        val date = localNoon(calendar)
        val baseJulianDate = julianDate(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH)
        )
        val offsetMinutes = date.timeZone.getOffset(date.timeInMillis) / 60_000.0
        var eventMinutes = calculateSolarParameters(date, latitude, longitude).solarNoonHours * 60.0

        repeat(3) {
            val eventJulianDate = baseJulianDate + (eventMinutes - offsetMinutes) / 1440.0
            val coordinates = solarCoordinates(eventJulianDate)
            val hourAngle = sunHourAngle(altitudeDeg, latitude, coordinates.declinationDeg)
                ?: return null
            val solarNoonMinutes = 720.0 - (4.0 * longitude) -
                coordinates.equationOfTimeMinutes + offsetMinutes
            eventMinutes = solarNoonMinutes + (if (morning) -4.0 else 4.0) * hourAngle
        }
        return eventMinutes / 60.0
    }

    private fun asrHours(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        shadowFactor: Double
    ): Double? {
        val date = localNoon(calendar)
        val baseJulianDate = julianDate(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH)
        )
        val offsetMinutes = date.timeZone.getOffset(date.timeInMillis) / 60_000.0
        var asrMinutes = calculateSolarParameters(date, latitude, longitude).solarNoonHours * 60.0 + 180.0

        repeat(3) {
            val coordinates = solarCoordinates(baseJulianDate + (asrMinutes - offsetMinutes) / 1440.0)
            val noonShadow = tan(Math.toRadians(abs(latitude - coordinates.declinationDeg)))
            val altitude = Math.toDegrees(atan(1.0 / (shadowFactor + noonShadow)))
            val hourAngle = sunHourAngle(altitude, latitude, coordinates.declinationDeg)
                ?: return null
            val solarNoonMinutes = 720.0 - (4.0 * longitude) -
                coordinates.equationOfTimeMinutes + offsetMinutes
            asrMinutes = solarNoonMinutes + 4.0 * hourAngle
        }
        return asrMinutes / 60.0
    }

    private fun solarCoordinates(julianDate: Double): SolarCoordinates {
        val t = (julianDate - 2451545.0) / 36525.0
        val geometricLongitude = fixAngle(280.46646 + t * (36000.76983 + 0.0003032 * t))
        val geometricAnomaly = fixAngle(357.52911 + t * (35999.05029 - 0.0001537 * t))
        val eccentricity = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
        val equationOfCenter =
            sinDeg(geometricAnomaly) * (1.914602 - t * (0.004817 + 0.000014 * t)) +
                sinDeg(2.0 * geometricAnomaly) * (0.019993 - 0.000101 * t) +
                sinDeg(3.0 * geometricAnomaly) * 0.000289
        val trueLongitude = geometricLongitude + equationOfCenter
        val omega = 125.04 - 1934.136 * t
        val apparentLongitude = trueLongitude - 0.00569 - 0.00478 * sinDeg(omega)
        val meanObliquity = 23.0 + (26.0 +
            (21.448 - t * (46.815 + t * (0.00059 - 0.001813 * t))) / 60.0) / 60.0
        val correctedObliquity = meanObliquity + 0.00256 * cosDeg(omega)
        val declination = asinDeg(sinDeg(correctedObliquity) * sinDeg(apparentLongitude))

        val y = tan(Math.toRadians(correctedObliquity / 2.0)).pow(2)
        val equationOfTime = 4.0 * Math.toDegrees(
            y * sinDeg(2.0 * geometricLongitude) -
                2.0 * eccentricity * sinDeg(geometricAnomaly) +
                4.0 * eccentricity * y * sinDeg(geometricAnomaly) *
                cosDeg(2.0 * geometricLongitude) -
                0.5 * y * y * sinDeg(4.0 * geometricLongitude) -
                1.25 * eccentricity * eccentricity * sinDeg(2.0 * geometricAnomaly)
        )
        val rightAscension = fixAngle(
            atan2Deg(
                cosDeg(correctedObliquity) * sinDeg(apparentLongitude),
                cosDeg(apparentLongitude)
            )
        ) / 15.0
        return SolarCoordinates(declination, equationOfTime, rightAscension)
    }

    private fun sunHourAngle(
        altitudeDeg: Double,
        latitudeDeg: Double,
        declinationDeg: Double
    ): Double? {
        val denominator = cosDeg(latitudeDeg) * cosDeg(declinationDeg)
        if (abs(denominator) < 1e-12) return null
        val cosine = (sinDeg(altitudeDeg) -
            sinDeg(latitudeDeg) * sinDeg(declinationDeg)) / denominator
        if (cosine < -1.0 - ANGLE_EPSILON || cosine > 1.0 + ANGLE_EPSILON) return null
        return acosDeg(cosine.coerceIn(-1.0, 1.0))
    }

    private fun localNoon(calendar: Calendar): Calendar = (calendar.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun hoursRelativeToDate(baseDate: Calendar, instantMillis: Long): Double {
        val target = Calendar.getInstance(baseDate.timeZone).apply { timeInMillis = instantMillis }
        val baseJulian = julianDate(
            baseDate.get(Calendar.YEAR), baseDate.get(Calendar.MONTH) + 1,
            baseDate.get(Calendar.DAY_OF_MONTH)
        )
        val targetJulian = julianDate(
            target.get(Calendar.YEAR), target.get(Calendar.MONTH) + 1,
            target.get(Calendar.DAY_OF_MONTH)
        )
        val dayOffset = (targetJulian - baseJulian).roundToInt()
        return dayOffset * 24.0 + target.get(Calendar.HOUR_OF_DAY) +
            target.get(Calendar.MINUTE) / 60.0 + target.get(Calendar.SECOND) / 3600.0
    }

    private fun validateCoordinates(latitude: Double, longitude: Double) {
        require(latitude.isFinite() && latitude in -90.0..90.0) {
            "Latitude must be between -90 and 90."
        }
        require(longitude.isFinite() && longitude in -180.0..180.0) {
            "Longitude must be between -180 and 180."
        }
    }

    private fun validateConfig(config: PrayerTimeConfig) {
        val fajrAngle = config.fajrAngleOverride ?: config.method.fajrAngle
        val ishaAngle = config.ishaAngleOverride ?: config.method.ishaAngle
        require(fajrAngle in 0.0..30.0) { "Fajr angle must be between 0 and 30 degrees." }
        if (config.method.ishaIntervalMinutes == null) {
            require(ishaAngle in 0.0..30.0) { "Isha angle must be between 0 and 30 degrees." }
        }
    }

    private fun validateOrdering(times: RawPrayerTimes) {
        val values = listOf(
            times.fajrHours, times.sunriseHours, times.dhuhrHours,
            times.asrHours, times.maghribHours, times.ishaHours
        )
        check(values.all(Double::isFinite) &&
            values.zipWithNext().all { (first, second) -> second > first }
        ) {
            "Calculated prayer times are not in chronological order. " +
                "Check timezone, coordinates, and manual offsets."
        }
    }

    // The epsilon keeps an exact minute (e.g. 5:00:00.000001 from float error) from moving a whole minute.
    private fun ceilToMinute(hours: Double): Double = ceil(hours * 60.0 - MINUTE_EPSILON) / 60.0
    private fun floorToMinute(hours: Double): Double = floor(hours * 60.0 + MINUTE_EPSILON) / 60.0

    private fun isRamadan(calendar: Calendar): Boolean =
        HijriCalendarHelper.getHijriDate(calendar).month == RAMADAN_MONTH

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sinDeg(degrees: Double): Double = sin(Math.toRadians(degrees))
    private fun cosDeg(degrees: Double): Double = cos(Math.toRadians(degrees))
    private fun asinDeg(value: Double): Double = Math.toDegrees(asin(value))
    private fun acosDeg(value: Double): Double = Math.toDegrees(acos(value))
    private fun atan2Deg(y: Double, x: Double): Double = Math.toDegrees(atan2(y, x))
    private fun fixAngle(angle: Double): Double = (angle % 360.0 + 360.0) % 360.0

    private const val STANDARD_HORIZON_ALTITUDE = -0.833
    private const val ANGLE_EPSILON = 1e-10
    private const val MINUTE_EPSILON = 1e-6
    private const val MINUTES_PER_DAY = 24 * 60
    private const val RAMADAN_MONTH = 9
}
