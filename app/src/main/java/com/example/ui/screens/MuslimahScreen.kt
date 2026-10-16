@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.core.analytics.AppAnalytics
import com.example.prayer.HijriCalendarHelper
import com.example.ui.components.ProvenanceCard
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizedUiText
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.TimeZone
import java.time.format.TextStyle as DateTextStyle

private const val SCREEN = "muslimah_screen"

private enum class PickTarget { START, END }

// ---------------------------------------------------------------- Muslimah mode

@Composable
internal fun MuslimahScreen(onBack: () -> Unit, modifier: Modifier) {
    com.example.core.analytics.AnalyticsScreen(SCREEN)

    val prefs = rememberToolPrefs()
    val store = remember(prefs) { MuslimahStore(prefs) }
    val rozaStore = remember(prefs) { RozaTrackerStore(prefs) }
    // "Today" is re-read on every resume so a screen left open overnight, or after a time-zone
    // change, never logs against yesterday's date.
    var today by remember { mutableStateOf(LocalDate.now()) }
    var revision by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { today = LocalDate.now(); revision++ }
    fun changed() { today = LocalDate.now(); revision++ }

    val enabled = remember(revision) { store.enabled }
    val periods = remember(revision, today) { store.periods(today) }
    val ongoing = periods.firstOrNull { it.ongoing }
    val excusedDays = remember(revision, today) { store.excusedDays(today) }
    val stats = remember(periods) { muslimahStats(periods) }
    val rozaBalance = remember(revision) { rozaStore.balance }
    val ramadanPending = remember(revision, today) {
        excusedDays.filter { isRamadan(it) && rozaStore.mark(it) == null }.sorted()
    }
    val recentlyEnded = ongoing == null && periods.firstOrNull()?.let {
        ChronoUnit.DAYS.between(it.end, today) in 0..2
    } == true

    var picker by remember { mutableStateOf<PickTarget?>(null) }
    var editing by remember { mutableStateOf<MuslimahPeriod?>(null) }
    var deleting by remember { mutableStateOf<MuslimahPeriod?>(null) }
    var confirmDeleteAll by rememberSaveable { mutableStateOf(false) }
    var visibleMonth by remember { mutableStateOf(YearMonth.from(today)) }

    ToolScreen(DailyTool.MUSLIMAH, onBack, modifier) { accent ->
        item(key = "privacy") { PrivacyHero(accent) }

        if (!enabled) {
            item(key = "onboarding") {
                Onboarding(accent) {
                    AppAnalytics.interaction(SCREEN, "enable_muslimah_mode", "tap")
                    store.enabled = true
                    changed()
                }
            }
        } else {
            item(key = "status") {
                StatusCard(
                    accent = accent,
                    ongoing = ongoing,
                    today = today,
                    onStartToday = {
                        AppAnalytics.interaction(SCREEN, "start_period_today", "tap")
                        store.startPeriod(today, today); changed()
                    },
                    onEndToday = {
                        AppAnalytics.interaction(SCREEN, "end_period_today", "tap")
                        store.endPeriod(today, today); changed()
                    },
                    onPickStart = { picker = PickTarget.START },
                    onPickEnd = { picker = PickTarget.END }
                )
            }
            if (recentlyEnded) {
                item(key = "ghusl") {
                    InfoNote("Your logged period has ended. A reminder about ghusl (the purifying bath) before returning to salah. Please confirm the details for your situation with a scholar.")
                }
            }
            item(key = "stats") { StatsCard(accent, stats, ongoing == null) }
            item(key = "calendar") {
                CycleCalendar(
                    accent = accent,
                    month = visibleMonth,
                    today = today,
                    excused = excusedDays,
                    ongoing = ongoing,
                    estimate = stats.nextEstimate.takeIf { ongoing == null },
                    onMonthChange = { visibleMonth = it },
                    onToggle = { date ->
                        AppAnalytics.interaction(SCREEN, "toggle_calendar_day", "tap")
                        if (store.toggleDay(date, today)) changed()
                    }
                )
            }
            item(key = "fasts") {
                FastsCard(
                    accent = accent,
                    pending = ramadanPending.size,
                    balance = rozaBalance,
                    onLogPending = {
                        AppAnalytics.interaction(SCREEN, "log_ramadan_qaza", "tap")
                        rozaStore.setMarks(ramadanPending, RozaMark.MISSED, today); changed()
                    },
                    onIncrement = { if (rozaBalance < Int.MAX_VALUE) { rozaStore.addMissed(1); changed() } },
                    onDecrement = { rozaStore.reduceBalance(1); changed() }
                )
            }
            item(key = "history_title") { SectionTitle("Logged periods") }
            if (periods.isEmpty()) {
                item(key = "history_empty") {
                    InfoNote("No periods logged yet. Tap \"My period started today\" above, or tap days on the calendar.")
                }
            } else {
                periods.forEach { period ->
                    item(key = "period_${period.start}") {
                        PeriodRow(
                            accent = accent,
                            period = period,
                            onEdit = { editing = period },
                            onDelete = { deleting = period }
                        )
                    }
                }
            }
            item(key = "settings") {
                ToolCard {
                    SectionTitle("Privacy & data")
                    LocalizedText(
                        "Turning the mode off hides it and stops excusing days in the prayer tracker. Your log is kept on this phone until you delete it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                AppAnalytics.interaction(SCREEN, "disable_muslimah_mode", "tap")
                                store.enabled = false; changed()
                            },
                            modifier = Modifier.weight(1f)
                        ) { LocalizedText("Turn off", textAlign = TextAlign.Center) }
                        OutlinedButton(
                            onClick = { confirmDeleteAll = true },
                            enabled = excusedDays.isNotEmpty(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) { LocalizedText("Delete all logs", textAlign = TextAlign.Center) }
                    }
                }
            }
        }

        item(key = "hadith") {
            ProvenanceCard(
                sourceText = "We were commanded to make up the fasts, but not the prayers.",
                collectionOrReference = "Sahih Muslim 335 · Aisha (RA)"
            )
        }
        item(key = "scholar") {
            InfoNote("Estimates here are for your own planning only. They are not medical advice or a religious ruling. For your own situation, ask a qualified scholar or a trusted mufti.")
        }
    }

    // ---------------------------------------------------------------- dialogs

    picker?.let { target ->
        val earliest = if (target == PickTarget.END) ongoing?.start else null
        MuslimahDatePicker(
            initial = today,
            earliest = earliest,
            today = today,
            onDismiss = { picker = null },
            onPick = { date ->
                when (target) {
                    PickTarget.START -> store.startPeriod(date, today)
                    PickTarget.END -> store.endPeriod(date, today)
                }
                picker = null
                visibleMonth = YearMonth.from(date)
                changed()
            }
        )
    }

    editing?.let { period ->
        EditPeriodDialog(
            period = period,
            canStayOpen = period == periods.firstOrNull(),
            today = today,
            onDismiss = { editing = null },
            onSave = { start, end ->
                AppAnalytics.interaction(SCREEN, "edit_period", "tap")
                store.editPeriod(period, start, end, today)
                editing = null
                changed()
            }
        )
    }

    deleting?.let { period ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { LocalizedText("Delete this period?") },
            text = { Text(periodRangeText(period)) },
            confirmButton = {
                TextButton(onClick = {
                    AppAnalytics.interaction(SCREEN, "delete_period", "tap")
                    store.deletePeriod(period); deleting = null; changed()
                }) { LocalizedText("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { LocalizedText("Cancel") } }
        )
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null) },
            title = { LocalizedText("Delete all cycle logs?") },
            text = { LocalizedText("Every logged day is removed from this phone. This cannot be undone. Your fasts-to-make-up balance is not changed.") },
            confirmButton = {
                TextButton(onClick = {
                    AppAnalytics.interaction(SCREEN, "delete_all_cycle_logs", "tap")
                    store.deleteAll(); confirmDeleteAll = false; changed()
                }) { LocalizedText("Delete all", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteAll = false }) { LocalizedText("Cancel") } }
        )
    }
}

// ---------------------------------------------------------------- sections

@Composable
private fun PrivacyHero(accent: Color) {
    HeroCard(accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.Lock, accent, 44.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Private & on-device", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LocalizedText(
                    "Nothing here leaves your phone or syncs anywhere.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Onboarding(accent: Color, onEnable: () -> Unit) {
    ToolCard {
        SectionTitle("A gentle, private companion")
        OnboardingPoint(Icons.Default.TouchApp, accent, "Log your period's start and end with one tap.")
        OnboardingPoint(Icons.Default.TaskAlt, accent, "The prayer tracker shows those days as excused, so your streak isn't broken.")
        OnboardingPoint(Icons.Default.NightsStay, accent, "Ramadan days you missed can be added to your fasts to make up.")
        OnboardingPoint(Icons.Default.Lock, accent, "Everything is stored only on this phone. No account, no upload.")
        Button(onClick = onEnable, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            LocalizedText("Enable Muslimah mode")
        }
    }
}

@Composable
private fun OnboardingPoint(icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        LocalizedText(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatusCard(
    accent: Color,
    ongoing: MuslimahPeriod?,
    today: LocalDate,
    onStartToday: () -> Unit,
    onEndToday: () -> Unit,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit
) {
    val language = LocalAppLanguage.current
    ToolCard {
        if (ongoing != null) {
            val day = ChronoUnit.DAYS.between(ongoing.start, today) + 1
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(1f, accent, size = 64.dp, stroke = 6.dp) {
                    Text("$day", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        localizeUiTemplate("Day {day} of your period", language, mapOf("day" to "$day")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        localizeUiTemplate("Started {date}", language, mapOf("date" to formatDay(ongoing.start))),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LocalizedText(
                "The prayer tracker shows these days as excused.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onEndToday, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                LocalizedText("My period ended today")
            }
            TextButton(onClick = onPickEnd, modifier = Modifier.fillMaxWidth()) { LocalizedText("It ended on an earlier day…") }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Default.Spa, accent, 48.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    LocalizedText("No period in progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    LocalizedText(
                        "Start logging when it begins; it stays open until you end it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(onClick = onStartToday, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                LocalizedText("My period started today")
            }
            TextButton(onClick = onPickStart, modifier = Modifier.fillMaxWidth()) { LocalizedText("It started on an earlier day…") }
        }
    }
}

@Composable
private fun StatsCard(accent: Color, stats: MuslimahStats, showEstimate: Boolean) {
    val language = LocalAppLanguage.current
    ToolCard {
        SectionTitle("Your patterns")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Average period", stats.averagePeriodLength, accent, Modifier.weight(1f))
            StatTile("Average cycle", stats.averageCycleLength, accent, Modifier.weight(1f))
        }
        val next = stats.nextEstimate
        if (showEstimate && next != null) {
            Surface(shape = MaterialTheme.shapes.medium, color = accent.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    LocalizedText("Estimated next start", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatDay(next), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = accent)
                    Text(
                        localizeUiTemplate(
                            "An estimate only, based on {count} logged cycles. Bodies vary; this is not medical advice.",
                            language,
                            mapOf("count" to "${stats.cyclesUsed}")
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (stats.averageCycleLength == null) {
            LocalizedText(
                "Log at least two periods to see an average cycle and a gentle estimate of the next one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatTile(label: String, days: Int?, accent: Color, modifier: Modifier) {
    val language = LocalAppLanguage.current
    val value = days?.let { localizeUiTemplate("{count} days", language, mapOf("count" to "$it")) } ?: "—"
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            LocalizedText(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
private fun CycleCalendar(
    accent: Color,
    month: YearMonth,
    today: LocalDate,
    excused: Set<LocalDate>,
    ongoing: MuslimahPeriod?,
    estimate: LocalDate?,
    onMonthChange: (YearMonth) -> Unit,
    onToggle: (LocalDate) -> Unit
) {
    val locale = LocalAppLocale.current
    val firstDay = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val monthTitle = remember(month, locale) { month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)) }
    val excusedLabel = localizedUiText("Excused")
    val estimateLabel = localizedUiText("Estimated start")
    val todayLabel = localizedUiText("Today")
    ToolCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onMonthChange(month.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = localizedUiText("Previous month"))
            }
            Text(monthTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            IconButton(onClick = { onMonthChange(month.plusMonths(1)) }, enabled = month < YearMonth.from(today).plusMonths(2)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = localizedUiText("Next month"))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            (0 until 7).forEach { i ->
                Text(
                    firstDay.plus(i.toLong()).getDisplayName(DateTextStyle.NARROW, locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        val leading = (month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7
        val cells = leading + month.lengthOfMonth()
        val rows = (cells + 6) / 7
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (c in 0 until 7) {
                        val index = r * 7 + c - leading
                        if (index < 0 || index >= month.lengthOfMonth()) {
                            Spacer(Modifier.weight(1f).aspectRatio(1f))
                            continue
                        }
                        val date = month.atDay(index + 1)
                        val isExcused = date in excused
                        val inOngoing = ongoing != null && date in ongoing
                        val future = date.isAfter(today)
                        val isEstimate = estimate != null && date == estimate
                        val tappable = !future && !inOngoing
                        val states = buildList {
                            if (date == today) add(todayLabel)
                            if (isExcused) add(excusedLabel)
                            if (isEstimate) add(estimateLabel)
                        }.joinToString(", ")
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isExcused) accent.copy(alpha = if (inOngoing) 0.55f else 0.85f) else Color.Transparent)
                                .then(
                                    when {
                                        date == today -> Modifier.border(2.dp, accent, CircleShape)
                                        isEstimate -> Modifier.border(BorderStroke(1.5.dp, accent.copy(alpha = 0.7f)), CircleShape)
                                        else -> Modifier
                                    }
                                )
                                .clickable(enabled = tappable, role = Role.Checkbox) { onToggle(date) }
                                .semantics {
                                    contentDescription = formatDay(date, locale)
                                    if (states.isNotEmpty()) stateDescription = states
                                }
                        ) {
                            Text(
                                "${date.dayOfMonth}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isExcused -> MaterialTheme.colorScheme.surface
                                    future -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(Modifier.background(accent.copy(alpha = 0.85f), CircleShape), excusedLabel)
            LegendDot(Modifier.border(1.5.dp, accent.copy(alpha = 0.7f), CircleShape), estimateLabel)
        }
        LocalizedText(
            "Tap a past day to add or remove it. Days of an open period change when you end or edit it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (YearMonth.from(today) != month) {
            TextButton(onClick = { onMonthChange(YearMonth.from(today)) }) { LocalizedText("Back to this month") }
        }
    }
}

@Composable
private fun LegendDot(shape: Modifier, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).then(shape))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FastsCard(
    accent: Color,
    pending: Int,
    balance: Int,
    onLogPending: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val language = LocalAppLanguage.current
    ToolCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LocalizedText("Fasts to make up", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LocalizedText(
                    "Missed Ramadan fasts are made up later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Stepper(value = balance, onDecrement = onDecrement, onIncrement = onIncrement)
        }
        LocalizedText(
            "Shared with the Roza tracker.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnimatedVisibility(visible = pending > 0) {
            Surface(shape = MaterialTheme.shapes.medium, color = accent.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        localizeUiTemplate(
                            "{count} logged days fall in Ramadan and aren't in the Roza tracker yet.",
                            language,
                            mapOf("count" to "$pending")
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FilledTonalButton(onClick = onLogPending, modifier = Modifier.fillMaxWidth()) {
                        Text(localizeUiTemplate("Add {count} to fasts to make up", language, mapOf("count" to "$pending")))
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodRow(accent: Color, period: MuslimahPeriod, onEdit: () -> Unit, onDelete: () -> Unit) {
    val language = LocalAppLanguage.current
    ToolCard(contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(periodRangeText(period), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                val detail = if (period.ongoing) {
                    localizeUiTemplate("Ongoing · day {count}", language, mapOf("count" to "${period.length}"))
                } else {
                    localizeUiTemplate("{count} days", language, mapOf("count" to "${period.length}"))
                }
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = localizedUiText("Edit period")) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, contentDescription = localizedUiText("Delete period")) }
        }
    }
}

@Composable
private fun EditPeriodDialog(
    period: MuslimahPeriod,
    canStayOpen: Boolean,
    today: LocalDate,
    onDismiss: () -> Unit,
    onSave: (LocalDate, LocalDate?) -> Unit
) {
    var start by remember { mutableStateOf(period.start) }
    var end by remember { mutableStateOf(period.end) }
    var open by remember { mutableStateOf(period.ongoing) }
    var pick by remember { mutableStateOf<PickTarget?>(null) }
    if (end.isBefore(start)) end = start

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { LocalizedText("Edit period") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField("Start", start) { pick = PickTarget.START }
                if (!open) DateField("End", end) { pick = PickTarget.END }
                if (canStayOpen) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable(role = Role.Checkbox) { open = !open }
                    ) {
                        Checkbox(checked = open, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        LocalizedText("Still ongoing")
                    }
                }
                LocalizedText(
                    "If the new dates overlap another logged period, the two are joined.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(start, if (open) null else end) }) { LocalizedText("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } }
    )

    pick?.let { target ->
        MuslimahDatePicker(
            initial = if (target == PickTarget.START) start else end,
            earliest = if (target == PickTarget.END) start else null,
            today = today,
            onDismiss = { pick = null },
            onPick = { if (target == PickTarget.START) start = it else end = it; pick = null }
        )
    }
}

@Composable
private fun DateField(label: String, date: LocalDate, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LocalizedText(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDay(date), style = MaterialTheme.typography.bodyLarge)
            }
            Icon(Icons.Default.CalendarMonth, contentDescription = null)
        }
    }
}

/** Material date picker on civil dates: its UTC-midnight millis are converted without any zone shift. */
@Composable
private fun MuslimahDatePicker(
    initial: LocalDate,
    earliest: LocalDate?,
    today: LocalDate,
    onDismiss: () -> Unit,
    onPick: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.coerceAtMost(today).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val day = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                return !day.isAfter(today) && (earliest == null || !day.isBefore(earliest))
            }
            override fun isSelectableYear(year: Int) = year <= today.year
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let { onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) } },
                enabled = state.selectedDateMillis != null
            ) { LocalizedText("Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } }
    ) { DatePicker(state = state) }
}

// ---------------------------------------------------------------- helpers

private fun formatDay(date: LocalDate, locale: java.util.Locale): String =
    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

@Composable
private fun formatDay(date: LocalDate): String = formatDay(date, LocalAppLocale.current)

@Composable
private fun periodRangeText(period: MuslimahPeriod): String {
    val language = LocalAppLanguage.current
    return when {
        period.ongoing -> localizeUiTemplate("{start} – ongoing", language, mapOf("start" to formatDay(period.start)))
        period.start == period.end -> formatDay(period.start)
        else -> "${formatDay(period.start)} – ${formatDay(period.end)}"
    }
}

/** Ramadan check through the app's own Hijri calendar, so it agrees with the Roza tracker and home screen. */
private fun isRamadan(date: LocalDate): Boolean = runCatching {
    val zone = TimeZone.getDefault()
    val calendar = Calendar.getInstance(zone).apply {
        clear()
        set(date.year, date.monthValue - 1, date.dayOfMonth, 12, 0)
    }
    HijriCalendarHelper.getHijriDate(calendar).month == 9
}.getOrDefault(false)
