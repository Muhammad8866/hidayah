package com.example.prayer

/**
 * Guidance displayed alongside the civil day's prayer schedule. Ishraq and Chasht are
 * estimates for optional prayer, not separate astronomical events or exact legal boundaries.
 */
data class OtherIslamicTimes(
    val sehriStopHours: Double,
    val ishraqGuideHours: Double?,
    val chashtGuideHours: Double?,
    val solarNoonHours: Double,
    val iftarHours: Double
) {
    companion object {
        fun from(
            prayers: AstronomicalPrayerCalculator.RawPrayerTimes,
            solarNoonHours: Double
        ): OtherIslamicTimes {
            require(solarNoonHours.isFinite() && solarNoonHours > prayers.sunriseHours) {
                "Solar noon must follow sunrise."
            }

            // A 20-minute wait is a cautious, conventional guide after sunrise.
            // The midpoint is a useful Chasht suggestion, not its opening boundary.
            val ishraq = prayers.sunriseHours + 20.0 / 60.0
            val chasht = (prayers.sunriseHours + solarNoonHours) / 2.0
            return OtherIslamicTimes(
                sehriStopHours = prayers.sehriEndHours,
                ishraqGuideHours = ishraq.takeIf { it < solarNoonHours - 15.0 / 60.0 },
                chashtGuideHours = chasht.takeIf { it > ishraq && it < solarNoonHours - 15.0 / 60.0 },
                solarNoonHours = solarNoonHours,
                iftarHours = prayers.maghribHours
            )
        }
    }
}
