package com.example.prayer.domain.policy

import com.example.domain.model.HighLatitudeRule

/** Juristic night-portion rules used when twilight is absent or unreasonably long. */
object HighLatitudePolicy {

    /**
     * Returns the maximum fractions of the sunset-to-sunrise night assigned to
     * Fajr (before sunrise) and Isha (after sunset).
     */
    fun nightPortions(
        rule: HighLatitudeRule,
        latitudeDeg: Double,
        fajrAngle: Double,
        ishaAngle: Double
    ): Pair<Double, Double> {
        // Automatic keeps the real twilight time whenever it exists and only caps it at the
        // middle of the night. Fixed portions (1/7, angle/60) would move Fajr later and Isha
        // earlier than the actual twilight, which is unsafe for Sehri and for Isha's validity.
        val effectiveRule = when (rule) {
            HighLatitudeRule.AUTOMATIC -> HighLatitudeRule.MIDDLE_OF_NIGHT
            else -> rule
        }
        return when (effectiveRule) {
            HighLatitudeRule.MIDDLE_OF_NIGHT -> 0.5 to 0.5
            HighLatitudeRule.SEVENTH_OF_NIGHT -> (1.0 / 7.0) to (1.0 / 7.0)
            HighLatitudeRule.TWILIGHT_ANGLE ->
                (fajrAngle / 60.0).coerceIn(0.0, 0.5) to
                    (ishaAngle / 60.0).coerceIn(0.0, 0.5)
            HighLatitudeRule.NONE -> 1.0 to 1.0
            HighLatitudeRule.AUTOMATIC -> error("Automatic rule must be resolved first")
        }
    }

    /** Compatibility helper retained for callers that need offsets in hours. */
    fun calculateFallbackTwilightOffsets(
        latitudeDeg: Double,
        nightDurationHours: Double,
        fajrAngle: Double,
        ishaAngle: Double
    ): Pair<Double, Double> {
        val safeNight = nightDurationHours.takeIf { it > 0.0 } ?: 8.0
        val portions = nightPortions(
            rule = HighLatitudeRule.AUTOMATIC,
            latitudeDeg = latitudeDeg,
            fajrAngle = fajrAngle,
            ishaAngle = ishaAngle
        )
        return portions.first * safeNight to portions.second * safeNight
    }
}
