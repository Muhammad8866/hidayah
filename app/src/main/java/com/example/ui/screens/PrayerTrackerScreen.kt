@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.prayer.AstronomicalPrayerCalculator
import com.example.prayer.PrayerSettingsStore
import com.example.ui.components.ProvenanceCard
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import java.util.Calendar
import java.util.TimeZone

private val JamaatGold = Color(0xFFB07D12)
private val LateBlue = Color(0xFF4F74B0)

/** Today's prayer start times in the saved city, plus the current time there, in wall-clock hours. */
private data class TrackerTimes(val starts: Map<String, Double>, val nowHours: Double)

@Composable
internal fun PrayerTrackerScreen(onBack: () -> Unit, modifier: Modifier) {
    val prefs = rememberToolPrefs()
    val store = remember(prefs) { PrayerTrackerStore(prefs) }
    val locale = LocalAppLocale.current
    val today = remember { LocalDate.now() }
    var selectedEpochDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    val selected = LocalDate.ofEpochDay(selectedEpochDay)
    var revision by remember { mutableIntStateOf(0) }
    var lastBulk by remember { mutableStateOf<Pair<LocalDate, List<String>>?>(null) }
    var showAddDays by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    val times = rememberTrackerTimes(selected, today)

    val cache = remember(revision) { mutableMapOf<LocalDate, TrackerDay>() }
    val dayOf: (LocalDate) -> TrackerDay = { date -> cache.getOrPut(date) { store.day(date) } }
    val day = dayOf(selected)
    val streak = remember(revision) { currentStreak(today, dayOf) }
    val best = remember(revision) { bestStreak(today, dayOf) }
    val stats = remember(revision) { prayerStats(today, dayOf) }
    val qaza = remember(revision) { store.qazaBalance() }
    val madeUp = remember(revision) { store.qazaMadeUp }
    val setsPerDay = remember(revision) { store.qazaSetsPerDay }
    val autoQaza = remember(revision) { store.autoQaza }

    val isToday = selected == today
    fun isUpcoming(prayer: String): Boolean =
        isToday && times != null && times.starts[prayer]?.let { it > times.nowHours } == true
    val currentPrayer = if (isToday && times != null) TRACKED_PRAYERS.lastOrNull { !isUpcoming(it) } else null
    val dueUnlogged = TRACKED_PRAYERS.filter { !isUpcoming(it) && day.marks[it] == null }
    val nextUpcoming = TRACKED_PRAYERS.firstOrNull { isUpcoming(it) }

    fun select(date: LocalDate) {
        selectedEpochDay = date.coerceAtMost(today).toEpochDay()
        lastBulk = null
    }

    ToolScreen(DailyTool.PRAYER_TRACKER, onBack, modifier) { accent ->
        item(key = "dates") {
            DateStrip(selected, today, accent, dayOf, onSelect = ::select)
        }
        item(key = "hero") {
            HeroCard(accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(if (day.excused) 1f else day.prayed / 5f, accent, size = 92.dp, stroke = 9.dp) {
                        if (day.excused) {
                            Icon(Icons.Default.Spa, contentDescription = null, tint = accent, modifier = Modifier.size(32.dp))
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${day.prayed}/5", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                LocalizedText("prayed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (selected >= today.minusDays(1)) {
                            LocalizedText(
                                if (isToday) "Today" else "Yesterday",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            selected.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            heroMessage(day, isToday, dueUnlogged, nextUpcoming, times, locale),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isToday) {
                            TextButton(
                                onClick = { select(today) },
                                contentPadding = PaddingValues(horizontal = 0.dp),
                                modifier = Modifier.height(32.dp).testTag("tracker_back_to_today")
                            ) {
                                Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                LocalizedText("Back to today", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill(Icons.Default.LocalFireDepartment, "${localizedUiText("Streak")}: $streak", accent, Modifier.weight(1f))
                    StatPill(Icons.Default.EmojiEvents, "${localizedUiText("Best")}: $best", themedAccent(JamaatGold), Modifier.weight(1f))
                }
            }
        }
        if (day.excused) {
            item(key = "excused") { InfoNote("Muslimah mode: this day is marked as excused from salah. It won't break your streak.") }
        } else if (dueUnlogged.size >= 2 || lastBulk?.first == selected) {
            item(key = "bulk") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (dueUnlogged.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = {
                                lastBulk = selected to store.markRemainingPrayed(selected, dueUnlogged)
                                revision++
                            },
                            modifier = Modifier.weight(1f).testTag("mark_all_prayed")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            LocalizedText("Mark remaining as prayed", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    lastBulk?.takeIf { it.first == selected }?.let { (date, changed) ->
                        TextButton(onClick = {
                            changed.forEach { store.setMark(date, it, null) }
                            lastBulk = null
                            revision++
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            LocalizedText("Undo")
                        }
                    }
                }
            }
        }
        TRACKED_PRAYERS.forEach { prayer ->
            item(key = "prayer_$prayer") {
                PrayerCard(
                    prayer = prayer,
                    mark = day.marks[prayer],
                    startTime = times?.starts?.get(prayer)?.let { AstronomicalPrayerCalculator.formatHoursToAmPm(it % 24.0, locale) },
                    isCurrent = prayer == currentPrayer,
                    upcoming = isUpcoming(prayer),
                    excused = day.excused,
                    accent = accent,
                    onMark = { mark ->
                        store.setMark(selected, prayer, mark)
                        lastBulk = null
                        revision++
                    }
                )
            }
        }
        item(key = "stats") {
            StatsCard(stats, today, selected, accent, dayOf, onSelect = ::select)
        }
        item(key = "qaza") {
            QazaCard(
                balance = qaza,
                madeUp = madeUp,
                setsPerDay = setsPerDay,
                autoQaza = autoQaza,
                accent = accent,
                onAdd = { store.addQaza(it); revision++ },
                onMakeUp = { store.makeUpQaza(it); revision++ },
                onSetsPerDay = { store.qazaSetsPerDay = it; revision++ },
                onAutoQaza = { store.autoQaza = it; revision++ },
                onAddDays = { showAddDays = true },
                onClear = { confirmClear = true }
            )
        }
        item(key = "jamaat_hadith") {
            ProvenanceCard(
                sourceText = "Prayer in congregation is twenty-seven degrees better than prayer offered alone.",
                collectionOrReference = "Sahih al-Bukhari 645"
            )
        }
        item(key = "note") {
            InfoNote("Everything stays on your phone. Ask a trusted scholar about your own makeup prayers.")
        }
    }

    if (showAddDays) {
        AddMissedDaysDialog(
            onDismiss = { showAddDays = false },
            onConfirm = { days -> store.addQazaDays(days); showAddDays = false; revision++ }
        )
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { LocalizedText("Reset Qaza balance?") },
            text = { LocalizedText("All Qaza counts and made-up progress will go back to zero. Your daily logs stay.") },
            confirmButton = {
                TextButton(onClick = { store.clearQaza(); confirmClear = false; revision++ }) { LocalizedText("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { LocalizedText("Cancel") } }
        )
    }
}

@Composable
private fun heroMessage(
    day: TrackerDay,
    isToday: Boolean,
    dueUnlogged: List<String>,
    nextUpcoming: String?,
    times: TrackerTimes?,
    locale: java.util.Locale
): String = when {
    day.excused -> localizedUiText("Excused from salah on this day.")
    day.complete -> localizedUiText("All five prayers prayed. Alhamdulillah!")
    dueUnlogged.isNotEmpty() && isToday ->
        "${localizedUiText("Did you pray")} ${localizedUiText(dueUnlogged.first())}? ${localizedUiText("Log it below.")}"
    isToday && nextUpcoming != null && times != null ->
        "${localizedUiText("Next")}: ${localizedUiText(nextUpcoming)} · " +
            AstronomicalPrayerCalculator.formatHoursToAmPm(times.starts.getValue(nextUpcoming) % 24.0, locale)
    else -> localizedUiText("Tap a status to log each prayer.")
}

/**
 * Prayer start times for [date] in the saved city, only needed for today so future prayers can't
 * be logged early. The clock ticks every 30 seconds so a prayer unlocks when its time begins.
 */
@Composable
private fun rememberTrackerTimes(date: LocalDate, today: LocalDate): TrackerTimes? {
    if (date != today) return null
    val context = LocalContext.current
    val setup = remember(date) {
        runCatching {
            val prefs = PrayerSettingsStore.prefs(context)
            val city = PrayerSettingsStore.loadCity(prefs)
            val config = PrayerSettingsStore.loadConfig(prefs)
            val zone = ZoneId.of(city.timeZoneId)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(zone)).apply {
                set(date.year, date.monthValue - 1, date.dayOfMonth)
            }
            val raw = AstronomicalPrayerCalculator.calculateTimes(calendar, city.latitude, city.longitude, config)
            zone to mapOf(
                "Fajr" to raw.fajrHours, "Dhuhr" to raw.dhuhrHours, "Asr" to raw.asrHours,
                "Maghrib" to raw.maghribHours, "Isha" to raw.ishaHours
            )
        }.getOrNull()
    } ?: return null
    val (zone, starts) = setup
    fun nowHours(): Double = LocalTime.now(zone).let { it.hour + it.minute / 60.0 + it.second / 3600.0 }
    var now by remember { mutableDoubleStateOf(nowHours()) }
    LaunchedEffect(zone) {
        while (true) {
            delay(30_000)
            now = nowHours()
        }
    }
    return TrackerTimes(starts, now)
}

// ---------------------------------------------------------------- Date strip

@Composable
private fun DateStrip(
    selected: LocalDate,
    today: LocalDate,
    accent: Color,
    dayOf: (LocalDate) -> TrackerDay,
    onSelect: (LocalDate) -> Unit
) {
    val locale = LocalAppLocale.current
    // Keep the selected day near the middle of the week, without showing future days.
    val end = minOf(selected.plusDays(3), today)
    val days = (6 downTo 0).map { end.minusDays(it.toLong()) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onSelect(selected.minusDays(7)) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = localizedUiText("Previous week"))
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            days.forEach { date ->
                val record = dayOf(date)
                val isSelected = date == selected
                val container by animateColorAsState(
                    if (isSelected) accent else MaterialTheme.colorScheme.surface, label = "dayContainer"
                )
                val content = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(container)
                        .border(
                            1.dp,
                            if (date == today && !isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelect(date) }
                        .padding(vertical = 8.dp)
                        .testTag("tracker_day_$date"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, locale),
                        style = MaterialTheme.typography.labelSmall,
                        color = content.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                    Text("${date.dayOfMonth}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = content)
                    Spacer(Modifier.height(4.dp))
                    DayDots(record, if (isSelected) MaterialTheme.colorScheme.surface else accent)
                }
            }
        }
        IconButton(
            onClick = { onSelect(selected.plusDays(7)) },
            enabled = selected < today,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = localizedUiText("Next week"))
        }
    }
}

/** Five tiny dots, one per prayer: filled when prayed, hollow otherwise. */
@Composable
private fun DayDots(day: TrackerDay, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        TRACKED_PRAYERS.forEach { prayer ->
            val mark = day.marks[prayer]
            Box(
                Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            day.excused -> color.copy(alpha = 0.35f)
                            mark == PrayerMark.MISSED -> MaterialTheme.colorScheme.error
                            mark?.prayed == true -> color
                            else -> color.copy(alpha = 0.18f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun StatPill(icon: ImageVector, text: String, tint: Color, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = tint.copy(alpha = 0.14f), modifier = modifier) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = tint, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

// ---------------------------------------------------------------- Prayer card

private fun prayerIcon(prayer: String): ImageVector = when (prayer) {
    "Fajr" -> Icons.Default.WbTwilight
    "Dhuhr" -> Icons.Default.WbSunny
    "Asr" -> Icons.Default.LightMode
    "Maghrib" -> Icons.Default.Brightness4
    else -> Icons.Default.NightsStay
}

private fun PrayerMark.icon(): ImageVector = when (this) {
    PrayerMark.JAMAAT -> Icons.Default.Groups
    PrayerMark.ON_TIME -> Icons.Default.Check
    PrayerMark.LATE -> Icons.Default.Schedule
    PrayerMark.MISSED -> Icons.Default.Close
}

@Composable
private fun PrayerMark.color(): Color = when (this) {
    PrayerMark.JAMAAT -> themedAccent(JamaatGold)
    PrayerMark.ON_TIME -> themedAccent(SuccessGreen)
    PrayerMark.LATE -> themedAccent(LateBlue)
    PrayerMark.MISSED -> MaterialTheme.colorScheme.error
}

@Composable
private fun PrayerCard(
    prayer: String,
    mark: PrayerMark?,
    startTime: String?,
    isCurrent: Boolean,
    upcoming: Boolean,
    excused: Boolean,
    accent: Color,
    onMark: (PrayerMark?) -> Unit
) {
    val markColor = mark?.color()
    val borderColor = when {
        markColor != null -> markColor.copy(alpha = 0.6f)
        isCurrent -> accent
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    Card(
        modifier = Modifier.fillMaxWidth().testTag("prayer_card_$prayer"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = markColor?.copy(alpha = 0.07f)?.compositeOver(MaterialTheme.colorScheme.surface)
                ?: MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(if (isCurrent && mark == null) 2.dp else 1.dp, borderColor)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = (markColor ?: accent).copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                    Icon(prayerIcon(prayer), contentDescription = null, tint = markColor ?: accent, modifier = Modifier.padding(9.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LocalizedText(prayer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (isCurrent) {
                            Spacer(Modifier.width(8.dp))
                            Surface(shape = CircleShape, color = accent) {
                                LocalizedText(
                                    "Now",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.surface,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    val status = when {
                        excused -> localizedUiText("Excused")
                        mark != null -> localizedUiText(mark.label)
                        upcoming -> "${localizedUiText("Starts at")} ${startTime.orEmpty()}"
                        else -> localizedUiText("Not logged yet")
                    }
                    Text(
                        if (startTime != null && !upcoming) "$status · $startTime" else status,
                        style = MaterialTheme.typography.bodySmall,
                        color = markColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (mark != null) FontWeight.Medium else FontWeight.Normal
                    )
                }
                if (mark != null) {
                    Icon(mark.icon(), contentDescription = null, tint = markColor ?: accent, modifier = Modifier.size(22.dp))
                }
            }
            if (!excused) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PrayerMark.entries.forEach { option ->
                        MarkOption(
                            option = option,
                            selected = mark == option,
                            enabled = !upcoming,
                            modifier = Modifier.weight(1f).testTag("${option.name.lowercase()}_$prayer"),
                            onClick = { onMark(if (mark == option) null else option) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkOption(option: PrayerMark, selected: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = option.color()
    val container by animateColorAsState(
        when {
            selected -> color
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.6f else 0.3f)
        },
        label = "markContainer"
    )
    val content = when {
        selected -> Color.White
        enabled -> color
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    Column(
        modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(option.icon(), contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        LocalizedText(
            option.label,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------- Stats

@Composable
private fun StatsCard(
    stats: PrayerStats,
    today: LocalDate,
    selected: LocalDate,
    accent: Color,
    dayOf: (LocalDate) -> TrackerDay,
    onSelect: (LocalDate) -> Unit
) {
    ToolCard {
        SectionTitle("Last 30 days")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("${(stats.completion * 100).toInt()}%", "Prayed", accent, Modifier.weight(1f))
            StatTile("${stats.jamaat}", "Jama'at", themedAccent(JamaatGold), Modifier.weight(1f))
            StatTile("${stats.completeDays}", "Full days", themedAccent(SuccessGreen), Modifier.weight(1f))
        }
        if (stats.trackedDays == 0) {
            LocalizedText(
                "Start logging to see your progress here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            TRACKED_PRAYERS.forEach { prayer ->
                val share = stats.perPrayer.getValue(prayer)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText(
                        prayer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(76.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    LinearProgressIndicator(
                        progress = { share },
                        modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                        color = if (prayer == stats.weakest) MaterialTheme.colorScheme.error else accent,
                        trackColor = accent.copy(alpha = 0.12f),
                        strokeCap = StrokeCap.Round,
                        drawStopIndicator = {}
                    )
                    Text(
                        "${(share * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(44.dp)
                    )
                }
            }
            stats.weakest?.let { weakest ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${localizedUiText("Needs attention")}: ${localizedUiText(weakest)}. ${localizedUiText("Set an alert so you don't miss it.")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SectionTitle("Last 5 weeks")
        Heatmap(today, selected, accent, dayOf, onSelect)
    }
}

@Composable
private fun StatTile(value: String, label: String, color: Color, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.10f), modifier = modifier) {
        Column(Modifier.padding(vertical = 10.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            LocalizedText(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** 5 weeks × 7 days, oldest top-left; tap a day to open it. */
@Composable
private fun Heatmap(today: LocalDate, selected: LocalDate, accent: Color, dayOf: (LocalDate) -> TrackerDay, onSelect: (LocalDate) -> Unit) {
    val locale = LocalAppLocale.current
    val first = today.minusDays(34)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (0L..6L).forEach { offset ->
                Text(
                    first.plusDays(offset).dayOfWeek.getDisplayName(DateTextStyle.NARROW, locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        (0 until 5).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (0 until 7).forEach { weekday ->
                    val date = first.plusDays((week * 7 + weekday).toLong())
                    val record = dayOf(date)
                    val fill = when {
                        record.excused -> MaterialTheme.colorScheme.surfaceVariant
                        record.logged == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else -> accent.copy(alpha = 0.15f + 0.85f * record.prayed / 5f)
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1.4f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(fill)
                            .then(if (date == selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(6.dp)) else Modifier)
                            .clickable { onSelect(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${date.dayOfMonth}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (record.prayed >= 4 && !record.excused) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            LocalizedText("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            (0..5).forEach { count ->
                Box(
                    Modifier
                        .padding(horizontal = 1.dp)
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accent.copy(alpha = 0.15f + 0.85f * count / 5f))
                )
            }
            Spacer(Modifier.width(4.dp))
            LocalizedText("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------- Qaza

@Composable
private fun QazaCard(
    balance: Map<String, Int>,
    madeUp: Int,
    setsPerDay: Int,
    autoQaza: Boolean,
    accent: Color,
    onAdd: (String) -> Unit,
    onMakeUp: (String) -> Unit,
    onSetsPerDay: (Int) -> Unit,
    onAutoQaza: (Boolean) -> Unit,
    onAddDays: () -> Unit,
    onClear: () -> Unit
) {
    val total = balance.values.sum()
    ToolCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LocalizedText("Qaza balance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LocalizedText(
                    "Tap + for a missed prayer, − when you make one up.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$total", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent, modifier = Modifier.testTag("qaza_total"))
                LocalizedText("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (madeUp > 0 || total > 0) {
            LinearProgressIndicator(
                progress = { if (madeUp + total == 0) 0f else madeUp.toFloat() / (madeUp + total) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = themedAccent(SuccessGreen),
                trackColor = accent.copy(alpha = 0.12f),
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {}
            )
            Text(
                "${localizedUiText("Made up so far")}: $madeUp",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        QAZA_PRAYER_NAMES.forEach { prayer ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    LocalizedText(prayer, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    if (prayer == "Witr") {
                        LocalizedText("Wajib in Hanafi fiqh", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Stepper(value = balance.getValue(prayer), onDecrement = { onMakeUp(prayer) }, onIncrement = { onAdd(prayer) })
            }
        }
        OutlinedButton(onClick = onAddDays, modifier = Modifier.fillMaxWidth().testTag("qaza_add_days")) {
            Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            LocalizedText("Add missed days, months or years")
        }
        if (total > 0) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    LocalizedText("Daily Qaza plan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${localizedUiText("Sets of 6 per day")} · ${localizedUiText("about")} ${qazaDaysToFinish(balance, setsPerDay)} ${localizedUiText("days to finish")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Stepper(setsPerDay, onDecrement = { onSetsPerDay(setsPerDay - 1) }, onIncrement = { onSetsPerDay(setsPerDay + 1) }, minimum = 1)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LocalizedText("Add missed prayers automatically", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                LocalizedText(
                    "Marking a prayer as missed adds it to the Qaza balance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked = autoQaza, onCheckedChange = onAutoQaza, modifier = Modifier.testTag("qaza_auto_switch"))
        }
        if (total > 0 || madeUp > 0) {
            TextButton(onClick = onClear, contentPadding = PaddingValues(horizontal = 0.dp)) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText("Reset Qaza balance")
            }
        }
    }
}

private enum class PeriodUnit(val label: String, val days: Int) { DAYS("Days", 1), MONTHS("Months", 30), YEARS("Years", 365) }

@Composable
private fun AddMissedDaysDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var amount by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf(PeriodUnit.DAYS) }
    val days = (amount.toIntOrNull() ?: 0).toLong() * unit.days
    val valid = days in 1..36_500
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
        title = { LocalizedText("Add missed days") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LocalizedText(
                    "Each day adds one of every fard prayer and Witr to your balance.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input -> amount = input.filter(Char::isDigit).take(5) },
                    label = { LocalizedText("How many?") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("qaza_days_input")
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    PeriodUnit.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = unit == option,
                            onClick = { unit = option },
                            shape = SegmentedButtonDefaults.itemShape(index, PeriodUnit.entries.size)
                        ) { LocalizedText(option.label, maxLines = 1) }
                    }
                }
                if (valid) {
                    Text(
                        "= $days ${localizedUiText("days")} · ${days * QAZA_PRAYER_NAMES.size} ${localizedUiText("prayers")}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(days.toInt()) }, enabled = valid, modifier = Modifier.testTag("qaza_days_confirm")) {
                LocalizedText("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } }
    )
}
