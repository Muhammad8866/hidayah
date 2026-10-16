package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import com.example.ui.components.AppCard
import com.example.ui.components.TonalPill
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.formatHijriDate
import com.example.ui.localization.localizedUiText
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal data class RozaSunnahDay(val date: LocalDate, val title: String, val reference: String)

/** Recommended (Sunnah/Nafl) fasts, each with its hadith reference. Dates follow the app's Hijri calendar. */
internal object RozaSunnahFasts {
    const val ARAFAH = "Day of Arafah (9 Dhu al-Hijjah)"
    const val ASHURA = "Day of Ashura (10 Muharram)"
    const val TASUA = "9 Muharram (fasted with Ashura)"
    const val WHITE_DAYS = "Ayyam al-Beed (13, 14, 15)"
    const val SHAWWAL = "Six fasts of Shawwal"
    const val MONDAY = "Monday fast"
    const val THURSDAY = "Thursday fast"

    fun occasion(date: LocalDate): RozaSunnahDay? = occasion(date, shawwal = true)

    /** The six of Shawwal may be kept on any days of the month, so the window is listed only once. */
    fun upcoming(today: LocalDate, count: Int = 5): List<RozaSunnahDay> {
        val result = ArrayList<RozaSunnahDay>()
        var shawwalShown = false
        var date = today
        repeat(SEARCH_DAYS) {
            if (result.size >= count) return result
            occasion(date, shawwal = !shawwalShown)?.let {
                if (it.title == SHAWWAL) shawwalShown = true
                result += it
            }
            if (RozaLogic.hijri(date).month != 10) shawwalShown = false
            date = date.plusDays(1)
        }
        return result
    }

    private fun occasion(date: LocalDate, shawwal: Boolean): RozaSunnahDay? {
        if (RozaLogic.isRamadan(date) || RozaLogic.isForbidden(date)) return null
        val hijri = RozaLogic.hijri(date)
        fun day(title: String, reference: String) = RozaSunnahDay(date, title, reference)
        return when {
            hijri.month == 12 && hijri.day == 9 -> day(ARAFAH, "Sahih Muslim 1162")
            hijri.month == 1 && hijri.day == 10 -> day(ASHURA, "Sahih Muslim 1162")
            hijri.month == 1 && hijri.day == 9 -> day(TASUA, "Sahih Muslim 1134")
            hijri.day in 13..15 && !(hijri.month == 12 && hijri.day == 13) -> day(WHITE_DAYS, "Jami' at-Tirmidhi 761")
            shawwal && hijri.month == 10 && hijri.day >= 2 -> day(SHAWWAL, "Sahih Muslim 1164")
            date.dayOfWeek == DayOfWeek.MONDAY -> day(MONDAY, "Jami' at-Tirmidhi 745")
            date.dayOfWeek == DayOfWeek.THURSDAY -> day(THURSDAY, "Jami' at-Tirmidhi 745")
            else -> null
        }
    }

    private const val SEARCH_DAYS = 400
}

internal object RozaQazaPlanner {
    const val PREF_KEY = "roza_plan_per_week"

    fun weeksNeeded(balance: Int, perWeek: Int): Long? =
        if (balance <= 0 || perWeek !in 1..7) null else (balance.toLong() + perWeek - 1) / perWeek

    fun finishDate(balance: Int, perWeek: Int, today: LocalDate): LocalDate? =
        weeksNeeded(balance, perWeek)?.let { today.plusWeeks(it) }
}

@Composable
internal fun RozaSunnahFastsCard(today: LocalDate, modifier: Modifier = Modifier) {
    val locale = LocalAppLocale.current
    val language = LocalAppLanguage.current
    val dateFormat = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    val weekdayFormat = remember(locale) { DateTimeFormatter.ofPattern("EEEE", locale) }
    val days = remember(today) { RozaSunnahFasts.upcoming(today) }
    AppCard(modifier.fillMaxWidth().testTag("roza_sunnah_card"), contentPadding = PaddingValues(0.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Upcoming Sunnah fasts")
            days.forEachIndexed { index, day ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LocalizedText(day.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        if (day.date == today) TonalPill(localizedUiText("Today"))
                    }
                    val window = if (day.title == RozaSunnahFasts.SHAWWAL) "${localizedUiText("Any six days of Shawwal, from")} " else ""
                    Text("$window${day.date.format(weekdayFormat)}, ${day.date.format(dateFormat)}", style = MaterialTheme.typography.bodyMedium)
                    Text(formatHijriDate(RozaLogic.hijri(day.date), language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${localizedUiText("Reference")}: ${day.reference}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            LocalizedText("Hijri dates depend on local moon sighting and may differ by a day.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun RozaQazaPlanCard(balance: Int, today: LocalDate, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE) }
    var perWeek by remember { mutableIntStateOf(prefs.getInt(RozaQazaPlanner.PREF_KEY, 2).coerceIn(1, 7)) }
    val locale = LocalAppLocale.current
    val numbers = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val dateFormat = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    fun setPerWeek(value: Int) {
        perWeek = value.coerceIn(1, 7)
        prefs.edit().putInt(RozaQazaPlanner.PREF_KEY, perWeek).apply()
    }
    AppCard(modifier.fillMaxWidth().testTag("roza_plan_card"), contentPadding = PaddingValues(0.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Qaza completion plan")
            val weeks = RozaQazaPlanner.weeksNeeded(balance, perWeek)
            val finish = RozaQazaPlanner.finishDate(balance, perWeek, today)
            if (weeks == null || finish == null) {
                LocalizedText("Alhamdulillah, no Qaza fasts are pending. Keep it up with Sunnah fasts.", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }
            LocalizedText("Qaza fasts per week", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalIconButton(onClick = { setPerWeek(perWeek - 1) }, enabled = perWeek > 1, modifier = Modifier.size(48.dp).testTag("roza_plan_less")) {
                    Icon(Icons.Default.Remove, contentDescription = localizedUiText("Decrease"))
                }
                Text(numbers.format(perWeek), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("roza_plan_per_week"))
                FilledTonalIconButton(onClick = { setPerWeek(perWeek + 1) }, enabled = perWeek < 7, modifier = Modifier.size(48.dp).testTag("roza_plan_more")) {
                    Icon(Icons.Default.Add, contentDescription = localizedUiText("Increase"))
                }
            }
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(numbers.format(weeks), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("roza_plan_weeks"))
                        LocalizedText("Weeks needed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(finish.format(dateFormat), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("roza_plan_finish"))
                        LocalizedText("Expected completion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            LocalizedText("An estimate at this pace. Qaza fasts cannot be kept on the days of Eid and Tashreeq.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
