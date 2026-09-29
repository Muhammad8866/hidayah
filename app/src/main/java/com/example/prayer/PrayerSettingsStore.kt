package com.example.prayer

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.CityLocation
import com.example.domain.model.PrayerTimeConfig
import com.example.domain.model.PredefinedLocations

/**
 * The persisted city and prayer settings, shared by the app and by alarm receivers that run
 * without the app open (after a reboot, or when one prayer alert schedules the next).
 */
object PrayerSettingsStore {
    const val PREFS_NAME = "deen_app_preferences"
    const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    private const val NOTIFICATIONS_CHOSEN_KEY = "notifications_user_chosen"
    private const val LIVE_LOCATION_KEY = "use_live_location"
    const val HIJRI_CALENDAR_KEY = "hijri_calendar_region"

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun notificationsEnabled(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(NOTIFICATIONS_CHOSEN_KEY, false) &&
            prefs.getBoolean(NOTIFICATIONS_ENABLED_KEY, false)

    fun saveNotificationsEnabled(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(NOTIFICATIONS_CHOSEN_KEY, true)
            .putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled).apply()
    }

    fun useLiveLocation(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(LIVE_LOCATION_KEY, true)

    fun saveUseLiveLocation(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(LIVE_LOCATION_KEY, enabled).apply()
    }

    fun hasSavedCity(prefs: SharedPreferences): Boolean =
        prefs.getString("city_latitude", null)?.toDoubleOrNull() != null &&
            prefs.getString("city_longitude", null)?.toDoubleOrNull() != null

    /** Restores the chosen moon-sighting calendar and its confirmed months into the Hijri helpers. */
    fun restoreHijriPreference(prefs: SharedPreferences) {
        HijriRegion.preferred = HijriRegion.byId(prefs.getString(HIJRI_CALENDAR_KEY, null))
        HijriRegion.ALL.forEach { region ->
            prefs.getString(confirmedHijriMonthsKey(region), null)
                ?.let { HijriCalendarHelper.restoreConfirmedMonths(region, it) }
        }
    }

    fun confirmedHijriMonthsKey(region: HijriRegion) = "hijri_confirmed_months_${region.id}"

    fun loadCity(prefs: SharedPreferences, default: CityLocation = PredefinedLocations.DEFAULT_CITY) = CityLocation(
        name = prefs.getString("city_name", default.name) ?: default.name,
        stateOrCountry = prefs.getString("city_region", default.stateOrCountry) ?: default.stateOrCountry,
        latitude = prefs.getString("city_latitude", null)?.toDoubleOrNull() ?: default.latitude,
        longitude = prefs.getString("city_longitude", null)?.toDoubleOrNull() ?: default.longitude,
        timeZoneId = prefs.getString("city_timezone", default.timeZoneId) ?: default.timeZoneId
    )

    fun saveCity(prefs: SharedPreferences, city: CityLocation) {
        prefs.edit()
            .putString("city_name", city.name)
            .putString("city_region", city.stateOrCountry)
            .putString("city_latitude", city.latitude.toString())
            .putString("city_longitude", city.longitude.toString())
            .putString("city_timezone", city.timeZoneId)
            .apply()
    }

    fun loadConfig(prefs: SharedPreferences, default: PrayerTimeConfig = PrayerTimeConfig()) = PrayerTimeConfig(
        method = enumPreference(prefs, "prayer_method", default.method),
        asrMethod = enumPreference(prefs, "asr_method", default.asrMethod),
        fajrAngleOverride = prefs.getString("fajr_angle_override", null)?.toDoubleOrNull(),
        ishaAngleOverride = prefs.getString("isha_angle_override", null)?.toDoubleOrNull(),
        highLatitudeRule = enumPreference(prefs, "high_latitude_rule", default.highLatitudeRule),
        manualFajrOffsetMin = prefs.getInt("offset_fajr", 0),
        manualDhuhrOffsetMin = prefs.getInt("offset_dhuhr", 0),
        manualAsrOffsetMin = prefs.getInt("offset_asr", 0),
        manualMaghribOffsetMin = prefs.getInt("offset_maghrib", 0),
        manualIshaOffsetMin = prefs.getInt("offset_isha", 0)
    )

    fun saveConfig(prefs: SharedPreferences, config: PrayerTimeConfig) {
        prefs.edit()
            .putString("prayer_method", config.method.name)
            .putString("asr_method", config.asrMethod.name)
            .putString("high_latitude_rule", config.highLatitudeRule.name)
            .putString("fajr_angle_override", config.fajrAngleOverride?.toString())
            .putString("isha_angle_override", config.ishaAngleOverride?.toString())
            .putInt("offset_fajr", config.manualFajrOffsetMin)
            .putInt("offset_dhuhr", config.manualDhuhrOffsetMin)
            .putInt("offset_asr", config.manualAsrOffsetMin)
            .putInt("offset_maghrib", config.manualMaghribOffsetMin)
            .putInt("offset_isha", config.manualIshaOffsetMin)
            .apply()
    }

    private inline fun <reified T : Enum<T>> enumPreference(prefs: SharedPreferences, key: String, default: T): T =
        prefs.getString(key, null)?.let { saved -> enumValues<T>().firstOrNull { it.name == saved } } ?: default
}
