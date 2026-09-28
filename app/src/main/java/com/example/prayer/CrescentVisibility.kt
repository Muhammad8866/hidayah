package com.example.prayer

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Predicts whether the new crescent can be seen after sunset, using Odeh's criterion
 * (M. Odeh, "New Criterion for Lunar Crescent Visibility", Experimental Astronomy 18, 2004).
 *
 * Sun and Moon positions follow Meeus, "Astronomical Algorithms" 2nd ed.: ch. 25 (Sun), ch. 47
 * (Moon, full periodic series), ch. 22 (nutation), ch. 12 (sidereal time), ch. 40 (parallax).
 * Moon altitudes agree with the astronomy-engine library to within 0.002°.
 */
internal object CrescentVisibility {
    /** Odeh V at or above which the crescent is visible to the naked eye (zone A). */
    const val NAKED_EYE_V = 5.65

    private const val RAD = Math.PI / 180.0
    private const val DEG = 180.0 / Math.PI
    private const val J2000 = 2451545.0
    private const val EARTH_RADIUS_KM = 6378.14

    data class EclipticPosition(val longitude: Double, val latitude: Double, val distanceKm: Double)

    private class Horizontal(
        val altitude: Double,
        val azimuth: Double,
        val hourAngle: Double,
        val declination: Double,
        val sinParallax: Double,
        val eclipticLongitude: Double,
        val distanceKm: Double
    )

    private enum class Body { SUN, MOON }

    /**
     * Odeh's V for the evening of the civil date [jdn] (Julian Day Number) at the given place,
     * or null when the Moon is still before conjunction or sets before the Sun.
     */
    fun odehV(latitude: Double, longitude: Double, jdn: Long): Double? {
        val sunset = setting(Body.SUN, jdn - longitude / 360.0 + 0.25, latitude, longitude) ?: return null
        val sunAtSunset = horizontal(Body.SUN, sunset, latitude, longitude)
        val moonAtSunset = horizontal(Body.MOON, sunset, latitude, longitude)
        val elongation = normalize(moonAtSunset.eclipticLongitude - sunAtSunset.eclipticLongitude)
        if (elongation <= 0.0 || elongation >= 180.0) return null

        val moonset = setting(Body.MOON, sunset, latitude, longitude) ?: return null
        if (moonset <= sunset) return null

        // Odeh evaluates the crescent at the "best time": sunset + 4/9 of the lag.
        val bestTime = sunset + (moonset - sunset) * 4.0 / 9.0
        val moon = horizontal(Body.MOON, bestTime, latitude, longitude)
        val sun = horizontal(Body.SUN, bestTime, latitude, longitude)
        val arcv = moon.altitude - sun.altitude
        val arcl = acos(
            (sin(moon.altitude * RAD) * sin(sun.altitude * RAD) +
                cos(moon.altitude * RAD) * cos(sun.altitude * RAD) * cos((moon.azimuth - sun.azimuth) * RAD))
                .coerceIn(-1.0, 1.0)
        ) * DEG
        val semiDiameterArcmin = 358473400.0 / moon.distanceKm / 60.0 * (1 + sin(moon.altitude * RAD) * moon.sinParallax)
        val width = semiDiameterArcmin * (1 - cos(arcl * RAD))
        return arcv - (-0.1018 * width * width * width + 0.7319 * width * width - 6.3226 * width + 7.1651)
    }

    /**
     * Neo-MABIMS imkanur rukyat criterion (Indonesia, Malaysia, Brunei, Singapore since 2022): at
     * sunset on the civil date [jdn] the Moon's topocentric altitude is at least 3° and its geocentric
     * elongation from the Sun at least 6.4°, the elongation taken as published to two decimals.
     * Indonesia's Kemenag measures the altitude to the Moon's [upperLimb]; Malaysia's JAKIM to its centre.
     */
    fun meetsMabims(latitude: Double, longitude: Double, jdn: Long, upperLimb: Boolean): Boolean {
        val sunset = setting(Body.SUN, jdn - longitude / 360.0 + 0.25, latitude, longitude) ?: return false
        val moon = horizontal(Body.MOON, sunset, latitude, longitude)
        val sun = horizontal(Body.SUN, sunset, latitude, longitude)
        val longitudeDifference = normalize(moon.eclipticLongitude - sun.eclipticLongitude)
        if (longitudeDifference <= 0.0 || longitudeDifference >= 180.0) return false
        val moonLatitude = moonPosition(sunset + deltaTDays(sunset)).latitude
        val elongation = acos(cos(moonLatitude * RAD) * cos(longitudeDifference * RAD)) * DEG
        val altitude = if (upperLimb) moon.altitude + MOON_SEMI_DIAMETER_KM_DEG / moon.distanceKm else moon.altitude
        return altitude >= MABIMS_ALTITUDE && elongation >= MABIMS_ELONGATION - 0.005
    }

    private const val MABIMS_ALTITUDE = 3.0
    private const val MABIMS_ELONGATION = 6.4

    /** Moon's semi-diameter in degrees times its distance in km (15.5' at 385,000 km). */
    private const val MOON_SEMI_DIAMETER_KM_DEG = 358473400.0 / 3600.0

    /** Geocentric Moon position for the mean equinox of date, Meeus ch. 47. [jde] is in Terrestrial Time. */
    fun moonPosition(jde: Double): EclipticPosition {
        val t = (jde - J2000) / 36525.0
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t
        val meanLongitude = 218.3164477 + 481267.88123421 * t - 0.0015786 * t2 + t3 / 538841 - t4 / 65194000
        val elongation = 297.8501921 + 445267.1114034 * t - 0.0018819 * t2 + t3 / 545868 - t4 / 113065000
        val sunAnomaly = 357.5291092 + 35999.0502909 * t - 0.0001536 * t2 + t3 / 24490000
        val moonAnomaly = 134.9633964 + 477198.8675055 * t + 0.0087414 * t2 + t3 / 69699 - t4 / 14712000
        val latitudeArgument = 93.2720950 + 483202.0175233 * t - 0.0036539 * t2 - t3 / 3526000 + t4 / 863310000
        val a1 = 119.75 + 131.849 * t
        val a2 = 53.09 + 479264.290 * t
        val a3 = 313.45 + 481266.484 * t
        val eccentricity = 1 - 0.002516 * t - 0.0000074 * t2

        var sumLongitude = 0.0
        var sumDistance = 0.0
        for (term in LONGITUDE_DISTANCE_TERMS) {
            val arg = (term[0] * elongation + term[1] * sunAnomaly + term[2] * moonAnomaly + term[3] * latitudeArgument) * RAD
            val e = eccentricityFactor(term[1], eccentricity)
            sumLongitude += term[4] * e * sin(arg)
            sumDistance += term[5] * e * cos(arg)
        }
        var sumLatitude = 0.0
        for (term in LATITUDE_TERMS) {
            val arg = (term[0] * elongation + term[1] * sunAnomaly + term[2] * moonAnomaly + term[3] * latitudeArgument) * RAD
            sumLatitude += term[4] * eccentricityFactor(term[1], eccentricity) * sin(arg)
        }
        sumLongitude += 3958 * sin(a1 * RAD) + 1962 * sin((meanLongitude - latitudeArgument) * RAD) + 318 * sin(a2 * RAD)
        sumLatitude += -2235 * sin(meanLongitude * RAD) + 382 * sin(a3 * RAD) +
            175 * sin((a1 - latitudeArgument) * RAD) + 175 * sin((a1 + latitudeArgument) * RAD) +
            127 * sin((meanLongitude - moonAnomaly) * RAD) - 115 * sin((meanLongitude + moonAnomaly) * RAD)

        return EclipticPosition(
            longitude = normalize(meanLongitude + sumLongitude / 1e6),
            latitude = sumLatitude / 1e6,
            distanceKm = 385000.56 + sumDistance / 1000
        )
    }

    private fun eccentricityFactor(sunAnomalyMultiple: Int, eccentricity: Double): Double = when (abs(sunAnomalyMultiple)) {
        1 -> eccentricity
        2 -> eccentricity * eccentricity
        else -> 1.0
    }

    /** Apparent geocentric Sun longitude (aberration included, nutation added by the caller), Meeus ch. 25. */
    private fun sunPosition(jde: Double): EclipticPosition {
        val t = (jde - J2000) / 36525.0
        val meanLongitude = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val anomaly = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
        val center = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(anomaly * RAD) +
            (0.019993 - 0.000101 * t) * sin(2 * anomaly * RAD) + 0.000289 * sin(3 * anomaly * RAD)
        val eccentricity = 0.016708634 - 0.000042037 * t
        val radiusAu = 1.000001018 * (1 - eccentricity * eccentricity) / (1 + eccentricity * cos((anomaly + center) * RAD))
        return EclipticPosition(normalize(meanLongitude + center - 0.00569), 0.0, radiusAu * 149597870.7)
    }

    /** Apparent topocentric altitude/azimuth (no refraction) at [jdUt], a Julian Date in Universal Time. */
    private fun horizontal(body: Body, jdUt: Double, latitude: Double, longitude: Double): Horizontal {
        val jde = jdUt + deltaTDays(jdUt)
        val t = (jde - J2000) / 36525.0

        // Nutation and true obliquity, Meeus ch. 22 (0.5" accuracy).
        val node = (125.04452 - 1934.136261 * t) * RAD
        val sunMean = (280.4665 + 36000.7698 * t) * RAD
        val moonMean = (218.3165 + 481267.8813 * t) * RAD
        val nutationLongitude = (-17.20 * sin(node) - 1.32 * sin(2 * sunMean) - 0.23 * sin(2 * moonMean) + 0.21 * sin(2 * node)) / 3600
        val nutationObliquity = (9.20 * cos(node) + 0.57 * cos(2 * sunMean) + 0.10 * cos(2 * moonMean) - 0.09 * cos(2 * node)) / 3600
        val obliquity = (23.4392911111 - (46.8150 * t + 0.00059 * t * t - 0.001813 * t * t * t) / 3600 + nutationObliquity) * RAD

        val position = if (body == Body.MOON) moonPosition(jde) else sunPosition(jde)
        val lambda = (position.longitude + nutationLongitude) * RAD
        val beta = position.latitude * RAD
        val rightAscension = atan2(sin(lambda) * cos(obliquity) - tan(beta) * sin(obliquity), cos(lambda))
        val declination = asin(sin(beta) * cos(obliquity) + cos(beta) * sin(obliquity) * sin(lambda))

        val siderealTime = greenwichMeanSiderealTime(jdUt) + nutationLongitude * cos(obliquity)
        val hourAngle = (siderealTime + longitude) * RAD - rightAscension
        val phi = latitude * RAD

        // Topocentric correction for parallax, Meeus ch. 40 (observer at sea level).
        val sinParallax = EARTH_RADIUS_KM / position.distanceKm
        val u = atan(0.99664719 * tan(phi))
        val rhoSinPhi = 0.99664719 * sin(u)
        val rhoCosPhi = cos(u)
        val deltaRa = atan2(-rhoCosPhi * sinParallax * sin(hourAngle), cos(declination) - rhoCosPhi * sinParallax * cos(hourAngle))
        val topoDeclination = atan2(
            (sin(declination) - rhoSinPhi * sinParallax) * cos(deltaRa),
            cos(declination) - rhoCosPhi * sinParallax * cos(hourAngle)
        )
        val topoHourAngle = hourAngle - deltaRa

        val altitude = asin(sin(phi) * sin(topoDeclination) + cos(phi) * cos(topoDeclination) * cos(topoHourAngle)) * DEG
        val azimuth = atan2(sin(topoHourAngle), cos(topoHourAngle) * sin(phi) - tan(topoDeclination) * cos(phi)) * DEG
        return Horizontal(altitude, azimuth, hourAngle * DEG, declination * DEG, sinParallax, position.longitude, position.distanceKm)
    }

    /**
     * Setting time (Julian Date, UT) nearest to [guess], found by iterating on the hour angle.
     * Sunset uses the standard -0°50'; moonset uses Meeus' 0.7275·parallax - 0°34'.
     */
    private fun setting(body: Body, guess: Double, latitude: Double, longitude: Double): Double? {
        val hourAngleRate = if (body == Body.MOON) 347.81 else 360.9856
        var time = guess
        repeat(10) {
            val position = horizontal(body, time, latitude, longitude)
            val standardAltitude = if (body == Body.MOON) 0.7275 * asin(position.sinParallax) * DEG - 0.5667 else -0.8333
            val cosSettingHourAngle = (sin(standardAltitude * RAD) - sin(latitude * RAD) * sin(position.declination * RAD)) /
                (cos(latitude * RAD) * cos(position.declination * RAD))
            if (cosSettingHourAngle !in -1.0..1.0) return null
            val currentHourAngle = normalize(position.hourAngle + 180.0) - 180.0
            val step = (acos(cosSettingHourAngle) * DEG - currentHourAngle) / hourAngleRate
            time += step
            if (abs(step) < 1e-6) return time
        }
        return time
    }

    private fun greenwichMeanSiderealTime(jdUt: Double): Double {
        val t = (jdUt - J2000) / 36525.0
        return normalize(280.46061837 + 360.98564736629 * (jdUt - J2000) + 0.000387933 * t * t - t * t * t / 38710000)
    }

    /** TT - UT (Espenak & Meeus polynomial for 2005–2050, adequate for visibility work either side). */
    private fun deltaTDays(jdUt: Double): Double {
        val years = (jdUt - J2000) / 365.25
        return (62.92 + 0.32217 * years + 0.005589 * years * years) / 86400.0
    }

    private fun normalize(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0

    /** Meeus Table 47.A: multiples of D, M, M', F; longitude (1e-6 deg); distance (1e-3 km). */
    private val LONGITUDE_DISTANCE_TERMS = arrayOf(
        intArrayOf(0, 0, 1, 0, 6288774, -20905355), intArrayOf(2, 0, -1, 0, 1274027, -3699111),
        intArrayOf(2, 0, 0, 0, 658314, -2955968), intArrayOf(0, 0, 2, 0, 213618, -569925),
        intArrayOf(0, 1, 0, 0, -185116, 48888), intArrayOf(0, 0, 0, 2, -114332, -3149),
        intArrayOf(2, 0, -2, 0, 58793, 246158), intArrayOf(2, -1, -1, 0, 57066, -152138),
        intArrayOf(2, 0, 1, 0, 53322, -170733), intArrayOf(2, -1, 0, 0, 45758, -204586),
        intArrayOf(0, 1, -1, 0, -40923, -129620), intArrayOf(1, 0, 0, 0, -34720, 108743),
        intArrayOf(0, 1, 1, 0, -30383, 104755), intArrayOf(2, 0, 0, -2, 15327, 10321),
        intArrayOf(0, 0, 1, 2, -12528, 0), intArrayOf(0, 0, 1, -2, 10980, 79661),
        intArrayOf(4, 0, -1, 0, 10675, -34782), intArrayOf(0, 0, 3, 0, 10034, -23210),
        intArrayOf(4, 0, -2, 0, 8548, -21636), intArrayOf(2, 1, -1, 0, -7888, 24208),
        intArrayOf(2, 1, 0, 0, -6766, 30824), intArrayOf(1, 0, -1, 0, -5163, -8379),
        intArrayOf(1, 1, 0, 0, 4987, -16675), intArrayOf(2, -1, 1, 0, 4036, -12831),
        intArrayOf(2, 0, 2, 0, 3994, -10445), intArrayOf(4, 0, 0, 0, 3861, -11650),
        intArrayOf(2, 0, -3, 0, 3665, 14403), intArrayOf(0, 1, -2, 0, -2689, -7003),
        intArrayOf(2, 0, -1, 2, -2602, 0), intArrayOf(2, -1, -2, 0, 2390, 10056),
        intArrayOf(1, 0, 1, 0, -2348, 6322), intArrayOf(2, -2, 0, 0, 2236, -9884),
        intArrayOf(0, 1, 2, 0, -2120, 5751), intArrayOf(0, 2, 0, 0, -2069, 0),
        intArrayOf(2, -2, -1, 0, 2048, -4950), intArrayOf(2, 0, 1, -2, -1773, 4130),
        intArrayOf(2, 0, 0, 2, -1595, 0), intArrayOf(4, -1, -1, 0, 1215, -3958),
        intArrayOf(0, 0, 2, 2, -1110, 0), intArrayOf(3, 0, -1, 0, -892, 3258),
        intArrayOf(2, 1, 1, 0, -810, 2616), intArrayOf(4, -1, -2, 0, 759, -1897),
        intArrayOf(0, 2, -1, 0, -713, -2117), intArrayOf(2, 2, -1, 0, -700, 2354),
        intArrayOf(2, 1, -2, 0, 691, 0), intArrayOf(2, -1, 0, -2, 596, 0),
        intArrayOf(4, 0, 1, 0, 549, -1423), intArrayOf(0, 0, 4, 0, 537, -1117),
        intArrayOf(4, -1, 0, 0, 520, -1571), intArrayOf(1, 0, -2, 0, -487, -1739),
        intArrayOf(2, 1, 0, -2, -399, 0), intArrayOf(0, 0, 2, -2, -381, -4421),
        intArrayOf(1, 1, 1, 0, 351, 0), intArrayOf(3, 0, -2, 0, -340, 0),
        intArrayOf(4, 0, -3, 0, 330, 0), intArrayOf(2, -1, 2, 0, 327, 0),
        intArrayOf(0, 2, 1, 0, -323, 1165), intArrayOf(1, 1, -1, 0, 299, 0),
        intArrayOf(2, 0, 3, 0, 294, 0), intArrayOf(2, 0, -1, -2, 0, 8752)
    )

    /** Meeus Table 47.B: multiples of D, M, M', F; latitude (1e-6 deg). */
    private val LATITUDE_TERMS = arrayOf(
        intArrayOf(0, 0, 0, 1, 5128122), intArrayOf(0, 0, 1, 1, 280602), intArrayOf(0, 0, 1, -1, 277693),
        intArrayOf(2, 0, 0, -1, 173237), intArrayOf(2, 0, -1, 1, 55413), intArrayOf(2, 0, -1, -1, 46271),
        intArrayOf(2, 0, 0, 1, 32573), intArrayOf(0, 0, 2, 1, 17198), intArrayOf(2, 0, 1, -1, 9266),
        intArrayOf(0, 0, 2, -1, 8822), intArrayOf(2, -1, 0, -1, 8216), intArrayOf(2, 0, -2, -1, 4324),
        intArrayOf(2, 0, 1, 1, 4200), intArrayOf(2, 1, 0, -1, -3359), intArrayOf(2, -1, -1, 1, 2463),
        intArrayOf(2, -1, 0, 1, 2211), intArrayOf(2, -1, -1, -1, 2065), intArrayOf(0, 1, -1, -1, -1870),
        intArrayOf(4, 0, -1, -1, 1828), intArrayOf(0, 1, 0, 1, -1794), intArrayOf(0, 0, 0, 3, -1749),
        intArrayOf(0, 1, -1, 1, -1565), intArrayOf(1, 0, 0, 1, -1491), intArrayOf(0, 1, 1, 1, -1475),
        intArrayOf(0, 1, 1, -1, -1410), intArrayOf(0, 1, 0, -1, -1344), intArrayOf(1, 0, 0, -1, -1335),
        intArrayOf(0, 0, 3, 1, 1107), intArrayOf(4, 0, 0, -1, 1021), intArrayOf(4, 0, -1, 1, 833),
        intArrayOf(0, 0, 1, -3, 777), intArrayOf(4, 0, -2, 1, 671), intArrayOf(2, 0, 0, -3, 607),
        intArrayOf(2, 0, 2, -1, 596), intArrayOf(2, -1, 1, -1, 491), intArrayOf(2, 0, -2, 1, -451),
        intArrayOf(0, 0, 3, -1, 439), intArrayOf(2, 0, 2, 1, 422), intArrayOf(2, 0, -3, -1, 421),
        intArrayOf(2, 1, -1, 1, -366), intArrayOf(2, 1, 0, 1, -351), intArrayOf(4, 0, 0, 1, 331),
        intArrayOf(2, -1, 1, 1, 315), intArrayOf(2, -2, 0, -1, 302), intArrayOf(0, 0, 1, 3, -283),
        intArrayOf(2, 1, 1, -1, -229), intArrayOf(1, 1, 0, -1, 223), intArrayOf(1, 1, 0, 1, 223),
        intArrayOf(0, 1, -2, -1, -220), intArrayOf(2, 1, -1, -1, -220), intArrayOf(1, 0, 1, 1, -185),
        intArrayOf(2, -1, -2, -1, 181), intArrayOf(0, 1, 2, 1, -177), intArrayOf(4, 0, -2, -1, 176),
        intArrayOf(4, -1, -1, -1, 166), intArrayOf(1, 0, 1, -1, -164), intArrayOf(4, 0, 1, -1, 132),
        intArrayOf(1, 0, -1, -1, -119), intArrayOf(4, -1, 0, -1, 115), intArrayOf(2, -2, 0, 1, 107)
    )
}
