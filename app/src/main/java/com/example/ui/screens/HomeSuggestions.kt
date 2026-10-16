package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.PrayerType
import com.example.ui.components.AppCard
import com.example.ui.components.IconBadge
import com.example.ui.components.Spacing
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.viewmodel.MainUiState
import java.util.Calendar

/** Where a contextual Home suggestion leads. */
internal enum class SuggestionTarget { RAMADAN, CALENDAR, QURAN, ADHKAR, TRACKER }

/** One already-localized "for you now" card; [key] is stable for LazyColumn and tests. */
@Immutable
internal data class HomeSuggestion(
    val key: String,
    val title: String,
    val body: String,
    val icon: ImageVector,
    val accent: Color,
    val target: SuggestionTarget
)

private val FARD = listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA)

/**
 * Picks at most three cards that matter right now: Ramadan or a near festival first, then
 * Jumu'ah, then the adhkar of this part of the day, then salah logging that has fallen behind.
 * Pure function of state and clock, so it is cheap to recompute once a minute.
 */
internal fun buildHomeSuggestions(
    uiState: MainUiState,
    progress: TodayProgress,
    nowMillis: Long,
    language: String
): List<HomeSuggestion> {
    fun t(text: String) = localizeUiText(text, language)
    fun tpl(template: String, vararg values: Pair<String, String>) =
        localizeUiTemplate(template, language, values.toMap())
    val out = mutableListOf<HomeSuggestion>()

    // Ramadan in progress: today's Sehri and Iftar at a glance.
    val ramadan = uiState.ramadanSchedule
    val upcoming = uiState.upcomingRamadan
    if (ramadan != null) {
        val day = ramadan.days.firstOrNull { it.roza == ramadan.currentRoza }
        if (day != null) {
            val body = when {
                nowMillis < day.sehriEndsMillis -> tpl("Sehri ends at {time}", "time" to ltr(day.sehriFormatted))
                nowMillis < day.iftarMillis -> tpl("Iftar at {time}", "time" to ltr(day.iftarFormatted))
                else -> tpl("Sehri ends {sehri} · Iftar {iftar}", "sehri" to ltr(day.sehriFormatted), "iftar" to ltr(day.iftarFormatted))
            }
            out += HomeSuggestion("ramadan", tpl("Ramadan · Roza {roza}", "roza" to day.roza.toString()), body,
                Icons.Default.NightsStay, Color(0xFF6B5BAE), SuggestionTarget.RAMADAN)
        }
    } else if (upcoming != null) {
        val days = ((upcoming.startsAtMillis - nowMillis) / 86_400_000L).toInt()
        if (days in 0..30) {
            out += HomeSuggestion("ramadan_soon",
                if (days == 0) t("Ramadan begins tonight") else tpl("Ramadan begins in {days} days", "days" to days.toString()),
                t("Plan your fasts and see the tentative timetable."),
                Icons.Default.NightsStay, Color(0xFF6B5BAE), SuggestionTarget.RAMADAN)
        }
    }

    // The nearest observance within the next ten days.
    uiState.festivalSchedule?.festivals
        ?.filter { it.daysRemaining in 0..10 }
        ?.minByOrNull { it.daysRemaining }
        ?.let { festival ->
            val name = t(festival.definition.name)
            val title = when (festival.daysRemaining) {
                0 -> tpl("{name} is today", "name" to name)
                1 -> tpl("{name} is tomorrow", "name" to name)
                else -> tpl("{name} in {days} days", "name" to name, "days" to festival.daysRemaining.toString())
            }
            out += HomeSuggestion("festival_${festival.definition.id}", title,
                t("Open the Islamic calendar for dates and details."),
                Icons.Default.Celebration, Color(0xFFB08A2E), SuggestionTarget.CALENDAR)
        }

    // Friday in the city's own time zone.
    if (homeDayOfWeek(uiState, nowMillis) == Calendar.FRIDAY) {
        out += HomeSuggestion("jummah", t("Jumu'ah Mubarak"),
            t("Read Surah Al-Kahf and send abundant salawat today."),
            Icons.AutoMirrored.Filled.MenuBook, Color(0xFF237A63), SuggestionTarget.QURAN)
    }

    // Adhkar of the current part of the day, while unfinished.
    if (progress.adhkarTotal > 0 && progress.adhkarDone < progress.adhkarTotal) {
        val morning = progress.adhkarIsMorning
        out += HomeSuggestion(if (morning) "adhkar_morning" else "adhkar_evening",
            t(if (morning) "Morning adhkar" else "Evening adhkar"),
            tpl("{done} of {total} completed · a few minutes of remembrance", "done" to progress.adhkarDone.toString(),
                "total" to progress.adhkarTotal.toString()),
            if (morning) Icons.Default.WbTwilight else Icons.Default.NightsStay, Color(0xFFA9812A), SuggestionTarget.ADHKAR)
    }

    // Prayers whose time has begun today but are not logged yet.
    if (uiState.rawPrayerTimes != null) {
        val begun = FARD.count { type -> prayerInstantToday(uiState, type, nowMillis)?.let { nowMillis >= it } == true }
        if (begun > 0 && progress.salahPrayed < begun) {
            out += HomeSuggestion("log_salah", t("Log today's prayers"),
                tpl("{prayed} of {begun} prayers so far are marked as prayed", "prayed" to progress.salahPrayed.toString(),
                    "begun" to begun.toString()),
                Icons.Default.Mosque, Color(0xFF237A63), SuggestionTarget.TRACKER)
        }
    }
    return out.take(3)
}

@Composable
internal fun HomeSuggestionsSection(suggestions: List<HomeSuggestion>, onOpen: (SuggestionTarget) -> Unit) {
    if (suggestions.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen)
            .testTag("home_suggestions"),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        HomeSectionTitle("For you now")
        suggestions.forEach { s -> SuggestionCard(s, onOpen) }
    }
}

@Composable
private fun SuggestionCard(s: HomeSuggestion, onOpen: (SuggestionTarget) -> Unit) {
    val description = s.title + ". " + s.body
    AppCard(
        onClick = com.example.core.analytics.AppAnalytics.trackItemAction("home_screen", "open_suggestion", s.key) { onOpen(s.target) },
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description }
            .testTag("suggestion_${s.key}"),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = if (s.target == SuggestionTarget.TRACKER) Icons.Default.TaskAlt else s.icon, accent = s.accent, size = 44.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Already localized by buildHomeSuggestions (templates are translated before substitution).
                Text(
                    text = s.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = s.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
