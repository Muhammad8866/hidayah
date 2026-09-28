package com.example.domain.model

enum class CalculationMethod(
    val id: String,
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null,
    val dhuhrAdjustmentMinutes: Int = 1,
    val description: String
) {
    KARACHI(
        id = "karachi",
        title = "University of Islamic Sciences, Karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0,
        description = "Widely used across India, Pakistan, Bangladesh & South Asia"
    ),
    ISNA(
        id = "isna",
        title = "Islamic Society of North America (ISNA)",
        fajrAngle = 15.0,
        ishaAngle = 15.0,
        description = "Standard method in North America"
    ),
    MWL(
        id = "mwl",
        title = "Muslim World League (Makkah)",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        description = "Used across Europe, Far East & parts of Americas"
    ),
    EGYPT(
        id = "egypt",
        title = "Egyptian General Authority of Survey",
        fajrAngle = 19.5,
        ishaAngle = 17.5,
        description = "Standard in Egypt, Levant, and parts of Africa"
    ),
    MAKKAH(
        id = "makkah",
        title = "Umm al-Qura University, Makkah",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90,
        dhuhrAdjustmentMinutes = 0,
        description = "Used in Saudi Arabia; Isha is 90 min after Maghrib, 120 min in Ramadan"
    )
}

enum class AsrJuristicMethod(
    val id: String,
    val title: String,
    val factor: Double,
    val description: String
) {
    HANAFI(
        id = "hanafi",
        title = "Hanafi (Shadow ratio 2:1)",
        factor = 2.0,
        description = "Shadow of object equals twice its length plus noon shadow"
    ),
    STANDARD(
        id = "standard",
        title = "Shafi'i, Maliki, Hanbali (Shadow ratio 1:1)",
        factor = 1.0,
        description = "Shadow of object equals its length plus noon shadow"
    )
}

enum class HighLatitudeRule(val title: String) {
    AUTOMATIC("Automatic (recommended)"), // real twilight; middle of the night only when it never ends
    NONE("No adjustment"),
    MIDDLE_OF_NIGHT("Middle of the night"),
    SEVENTH_OF_NIGHT("Seventh of the night"),
    TWILIGHT_ANGLE("Twilight angle")
}

data class PrayerTimeConfig(
    val method: CalculationMethod = CalculationMethod.KARACHI,
    val asrMethod: AsrJuristicMethod = AsrJuristicMethod.HANAFI,
    val fajrAngleOverride: Double? = null,
    val ishaAngleOverride: Double? = null,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.AUTOMATIC,
    val manualFajrOffsetMin: Int = 0,
    val manualDhuhrOffsetMin: Int = 0,
    val manualAsrOffsetMin: Int = 0,
    val manualMaghribOffsetMin: Int = 0,
    val manualIshaOffsetMin: Int = 0,
    /** Ihtiyat (precaution) after sunset so Maghrib/Iftar is never announced before the sun has set. */
    val maghribIhtiyatMin: Int = 2
)

enum class PrayerType(val displayName: String, val arabicName: String) {
    FAJR("Fajr", "الفجر"),
    SUNRISE("Sunrise", "الشروق"),
    DHUHR("Dhuhr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء")
}

data class DailyPrayerSchedule(
    val dateString: String,
    val fajrTime: String,
    val sunriseTime: String,
    val dhuhrTime: String,
    val asrTime: String,
    val maghribTime: String,
    val ishaTime: String,
    val currentOrNextPrayer: PrayerType,
    val nextPrayerTimeFormatted: String,
    val minutesUntilNextPrayer: Long,
    val isCountingDown: Boolean,
    val locationName: String,
    val calculationMethodName: String,
    val asrMethodName: String,
    val hijriDateFormatted: String
)
