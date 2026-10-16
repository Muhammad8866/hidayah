package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import com.example.domain.model.PrayerType
import com.example.prayer.AstronomicalPrayerCalculator
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.viewmodel.MainUiState
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

/**
 * Wall clock that ticks on exact [periodMillis] boundaries (second or minute), so Home countdowns
 * never lag the ViewModel's unaligned 60 s refresh by up to a minute.
 */
@Composable
internal fun rememberHomeNow(periodMillis: Long): State<Long> {
    val now = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(periodMillis) {
        while (true) {
            val t = System.currentTimeMillis()
            now.longValue = t
            delay(periodMillis - t % periodMillis)
        }
    }
    return now
}

internal fun AstronomicalPrayerCalculator.RawPrayerTimes.homeHoursOf(type: PrayerType): Double = when (type) {
    PrayerType.FAJR -> fajrHours
    PrayerType.SUNRISE -> sunriseHours
    PrayerType.DHUHR -> dhuhrHours
    PrayerType.ASR -> asrHours
    PrayerType.MAGHRIB -> maghribHours
    PrayerType.ISHA -> ishaHours
}

private fun cityCalendar(uiState: MainUiState, nowMillis: Long): Calendar =
    Calendar.getInstance(TimeZone.getTimeZone(uiState.selectedCity.timeZoneId)).apply { timeInMillis = nowMillis }

/** Instant of [type] on the city's civil day containing [nowMillis], or null while times are unknown. */
internal fun prayerInstantToday(uiState: MainUiState, type: PrayerType, nowMillis: Long): Long? {
    val raw = uiState.rawPrayerTimes ?: return null
    return runCatching {
        AstronomicalPrayerCalculator.formatHoursToTimeInMillis(cityCalendar(uiState, nowMillis), raw.homeHoursOf(type))
    }.getOrNull()
}

/**
 * Exact instant the next prayer begins. After Isha (and across midnight) the next Fajr is taken
 * from tomorrow's times on the next civil day of the city's own time zone, not the device's.
 */
internal fun nextPrayerTargetMillis(uiState: MainUiState, nowMillis: Long): Long? {
    val today = prayerInstantToday(uiState, uiState.nextPrayer, nowMillis) ?: return null
    if (today > nowMillis) return today
    if (uiState.nextPrayer != PrayerType.FAJR) return today
    val tomorrowRaw = uiState.tomorrowPrayerTimes ?: return today + DAY_MILLIS
    val cal = cityCalendar(uiState, nowMillis).apply { add(Calendar.DAY_OF_YEAR, 1) }
    return runCatching { AstronomicalPrayerCalculator.formatHoursToTimeInMillis(cal, tomorrowRaw.fajrHours) }
        .getOrDefault(today + DAY_MILLIS)
}

/** When the running prayer window began; after midnight the running Isha began yesterday. */
internal fun currentPrayerStartMillis(uiState: MainUiState, nowMillis: Long): Long? {
    val start = prayerInstantToday(uiState, uiState.currentPrayer, nowMillis) ?: return null
    return if (start > nowMillis) start - DAY_MILLIS else start
}

/** True when the highlighted next Fajr belongs to tomorrow's civil date in the city. */
internal fun isNextFajrTomorrow(uiState: MainUiState, nowMillis: Long): Boolean {
    if (uiState.nextPrayer != PrayerType.FAJR) return false
    val target = nextPrayerTargetMillis(uiState, nowMillis) ?: return false
    return homeDayKey(uiState, target) != homeDayKey(uiState, nowMillis)
}

/** Civil day number in the city's time zone; changes exactly at the city's midnight. */
internal fun homeDayKey(uiState: MainUiState, nowMillis: Long): Int {
    val cal = cityCalendar(uiState, nowMillis)
    return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
}

internal fun homeDayOfWeek(uiState: MainUiState, nowMillis: Long): Int =
    cityCalendar(uiState, nowMillis).get(Calendar.DAY_OF_WEEK)

/** Live countdown: "1h 05m" for an hour or more, "12:34" (mm:ss) in the last hour, "Now" at zero. */
internal fun formatLiveCountdown(remainingMillis: Long, language: String): String {
    if (remainingMillis <= 0L) return localizeUiText("Now", language)
    val totalSeconds = (remainingMillis + 999) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        localizeUiTemplate("{hours}h {minutes}m", language,
            mapOf("hours" to hours.toString(), "minutes" to String.format(Locale.US, "%02d", minutes)))
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

/** Minute-precision phrase for screen readers, so TalkBack is not flooded with seconds. */
internal fun spokenCountdown(remainingMillis: Long, language: String): String {
    if (remainingMillis <= 0L) return localizeUiText("Now", language)
    val totalMinutes = ((remainingMillis + 59_999) / 60_000).coerceAtLeast(1)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        localizeUiTemplate("{hours} hours {minutes} minutes", language,
            mapOf("hours" to hours.toString(), "minutes" to minutes.toString()))
    } else {
        localizeUiTemplate("{minutes} minutes", language, mapOf("minutes" to minutes.toString()))
    }
}
