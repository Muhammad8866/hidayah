package com.example.ui.screens
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.prayer.RamadanTimetable
import com.example.prayer.RamadanTimetable.Phase
import com.example.ui.components.BackIconButton
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.formatHijriDate
import com.example.ui.localization.appLocale
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val SHAWWAL_MONTH = 10

@Composable
fun RamadanScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    if (!uiState.hasConfirmedLocation) {
        Column(modifier = modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            LocalizedText("Choose a city on Home for Sehri and Iftar times.",
                style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
        return
    }
    val timeZone = TimeZone.getTimeZone(uiState.selectedCity.timeZoneId)

    // Live clock for the Sehri/Iftar (or Ramadan) countdown.
    var now by remember(timeZone.id) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timeZone.id) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val schedule = uiState.ramadanSchedule
    if (schedule == null) {
        uiState.upcomingRamadan?.let { UpcomingRamadanContent(it, now, uiState, viewModel, timeZone, modifier, onBack) }
        return
    }
    RamadanContent(schedule, now, uiState, viewModel, timeZone, modifier, onBack)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RamadanContent(
    schedule: RamadanTimetable.RamadanSchedule,
    now: Long,
    uiState: MainUiState,
    viewModel: MainViewModel,
    timeZone: TimeZone,
    modifier: Modifier,
    onBack: (() -> Unit)?
) {
    // Derived from the clock (not the cached Hijri day) so the screen moves on at Sehri and Iftar by itself.
    val live = RamadanTimetable.liveStatus(schedule.days, now)
    val activeRoza = live?.day?.roza ?: schedule.currentRoza

    // Once an Iftar passes the Hijri day rolls over, so let the ViewModel recompute (e.g. Ramadan -> Eid).
    val statusKey = live?.let { it.day.roza * 2 + it.phase.ordinal } ?: -1
    var seenStatusKey by remember { mutableIntStateOf(statusKey) }
    LaunchedEffect(statusKey) {
        if (statusKey != seenStatusKey) {
            seenStatusKey = statusKey
            viewModel.refreshRealtimeDate()
        }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val topSections = buildList {
        add("header")
        add(if (live != null) "hero" else "complete")
        add("progress")
        if (live != null && (RamadanTimetable.isOddNight(live.upcomingNight) || live.day.roza >= 20)) add("qadr")
        add("dua")
    }
    val entries = timetableEntries(schedule.days)
    // +1 for the sticky timetable header.
    val todayIndex = topSections.size + 1 + entries.indexOfFirst { it is TimetableEntry.Day && it.day.roza == activeRoza }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("ramadan_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(topSections, key = { it }) { section ->
            when (section) {
                "header" -> RamadanHeader(
                    title = "Ramzan Mubarak",
                    hijriYear = schedule.hijriYear,
                    subtitle = template("Roza {n}", "n" to "$activeRoza") + " · " + uiState.selectedCity.name,
                    onBack = onBack,
                    onRefresh = { viewModel.refreshRealtimeDate() }
                )
                "hero" -> live?.let { LiveCountdownCard(it, now) }
                "complete" -> MessageCard(
                    icon = Icons.Default.Celebration,
                    title = "Eid Mubarak",
                    text = "Ramadan is complete. May Allah accept your fasts and worship.",
                    highlighted = true
                )
                "progress" -> MonthProgressCard(activeRoza, completedFasts = live?.let { it.day.roza - 1 } ?: schedule.days.size)
                "qadr" -> live?.let { LaylatulQadrCard(it.upcomingNight) }
                "dua" -> DuaCard()
            }
        }

        stickyHeader(key = "timetable_header") {
            TimetableHeader(
                title = "Ramzan Timetable",
                onJumpToToday = if (live != null && todayIndex > topSections.size) {
                    { scope.launch { listState.animateScrollToItem(todayIndex, scrollOffset = -160) }; Unit }
                } else null
            )
        }

        timetableItems(entries, timeZone) { day ->
            when {
                live == null || day.iftarMillis <= now -> RowState.PAST
                day.roza == live.day.roza -> RowState.TODAY
                else -> RowState.UPCOMING
            }
        }

        item(key = "footer") {
            Footnotes(
                "Sehri ends at Fajr and Iftar begins at Maghrib, based on your location and prayer settings. " +
                    "As a precaution, finish Sehri 5-10 minutes early. Ramadan begins and ends with moon sighting; " +
                    "adjust the Hijri date in Settings if needed. * The 30th fast applies only if the Eid moon is not sighted after the 29th fast."
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UpcomingRamadanContent(
    upcoming: RamadanTimetable.UpcomingRamadan,
    now: Long,
    uiState: MainUiState,
    viewModel: MainViewModel,
    timeZone: TimeZone,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val firstDay = upcoming.days.first()
    val language = LocalAppLanguage.current
    val (firstFastText, firstNightText) = remember(firstDay.dateMillis, upcoming.startsAtMillis, timeZone, language) {
        val format = SimpleDateFormat("EEEE, d MMMM yyyy", appLocale(language)).apply { this.timeZone = timeZone }
        format.format(firstDay.dateMillis) to format.format(upcoming.startsAtMillis)
    }
    val hijriNow = uiState.hijriDateTonight ?: uiState.hijriDate
    val isEid = hijriNow.month == SHAWWAL_MONTH && hijriNow.day == 1
    val isShawwal = hijriNow.month == SHAWWAL_MONTH

    // When the countdown reaches Maghrib of the first night, switch to the live timetable.
    val started = now >= upcoming.startsAtMillis
    LaunchedEffect(started) { if (started) viewModel.refreshRealtimeDate() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ramadan_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            RamadanHeader(
                title = "Ramzan",
                hijriYear = upcoming.hijriYear,
                subtitle = localizedUiText("Today") + " " + formatHijriDate(uiState.hijriDate, language) + " · " + uiState.selectedCity.name,
                onBack = onBack,
                onRefresh = { viewModel.refreshRealtimeDate() }
            )
        }

        if (isEid) {
            item(key = "eid") {
                MessageCard(
                    icon = Icons.Default.Celebration,
                    title = "Eid Mubarak",
                    text = "Ramadan is complete. May Allah accept your fasts and worship.",
                    highlighted = true
                )
            }
        }

        item(key = "countdown") {
            // Split by local calendar days: Ramadan starts at Maghrib, not midnight,
            // so dividing elapsed milliseconds by 24 hours shows a misleading day count.
            val (remainingDays, remainingTime) = calendarCountdown(now, upcoming.startsAtMillis, timeZone)
            HeroCard(testTag = "ramadan_countdown") {
                HeroLabel("Ramadan begins in")
                Text(
                    text = template("{n} days", "n" to "$remainingDays"),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = formatCountdown(remainingTime),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                HeroInfoLine(Icons.Default.NightsStay, template("First Taraweeh night begins at Maghrib: {date}", "date" to firstNightText))
                HeroInfoLine(Icons.Default.WbSunny, template("Estimated first fast: {date}", "date" to firstFastText))
            }
        }

        item(key = "first_times") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeCard("First Sehri (ends)", firstDay.sehriFormatted, Icons.Default.DarkMode, Modifier.weight(1f))
                TimeCard("First Iftar", firstDay.iftarFormatted, Icons.Default.WbTwilight, Modifier.weight(1f))
            }
        }

        if (isShawwal) {
            item(key = "shawwal") {
                MessageCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "Six fasts of Shawwal",
                    text = "Whoever fasts Ramadan and follows it with six days of Shawwal, it is as if he fasted the whole year.",
                    reference = "Sahih Muslim 1164"
                )
            }
        }

        stickyHeader(key = "timetable_header") {
            TimetableHeader(title = "Estimated Ramadan Timetable", onJumpToToday = null)
        }

        timetableItems(timetableEntries(upcoming.days), timeZone) { RowState.UPCOMING }

        item(key = "footer") {
            Footnotes(
                "The countdown ends on the first night of Ramadan at Maghrib, when Taraweeh begins. It is an estimate based on the Hijri calendar. " +
                    "The actual start depends on moon sighting and may differ by one day. " +
                    "Adjust the Hijri date in Settings if needed."
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Sections
// ---------------------------------------------------------------------------------------------

@Composable
private fun RamadanHeader(title: String, hijriYear: Int, subtitle: String, onBack: (() -> Unit)?, onRefresh: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) BackIconButton(onBack)
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                LocalizedText(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("$hijriYear AH", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = localizedUiText("Refresh Ramadan timings"))
        }
    }
}

@Composable
private fun LiveCountdownCard(live: RamadanTimetable.LiveStatus, now: Long) {
    val day = live.day
    val fasting = live.phase == Phase.FASTING
    HeroCard(testTag = "ramadan_countdown") {
        HeroLabel(if (fasting) "Iftar in" else "Sehri ends in")
        Text(
            text = formatCountdown(live.targetMillis - now),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = if (fasting) template("Roza {n} · Iftar at {time}", "n" to "${day.roza}", "time" to day.iftarFormatted)
            else template("Roza {n} · Sehri ends at {time}", "n" to "${day.roza}", "time" to day.sehriFormatted),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Today's fast from Sehri to Iftar; empty until Sehri ends.
        LinearProgressIndicator(
            progress = { live.fastProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.onPrimary,
            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HeroEndpoint(Icons.Default.DarkMode, "Sehri", day.sehriFormatted, Alignment.Start, Modifier.weight(1f))
            Text(
                text = formatDuration(day.fastDurationMillis),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            HeroEndpoint(Icons.Default.WbTwilight, "Iftar", day.iftarFormatted, Alignment.End, Modifier.weight(1f))
        }
        if (!fasting) {
            Spacer(modifier = Modifier.height(10.dp))
            HeroInfoLine(Icons.Default.Info, localizedUiText("Finish Sehri 5-10 minutes early as a precaution."))
        }
    }
}

@Composable
private fun MonthProgressCard(activeRoza: Int, completedFasts: Int) {
    val ashra = RamadanTimetable.ashraOf(activeRoza)
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().testTag("ramadan_progress")) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = template("Roza {n} of {total}", "n" to "$activeRoza", "total" to "30"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = template("{n} fasts completed", "n" to "${completedFasts.coerceIn(0, 30)}"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 30 segments grouped into the three Ashras.
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (group in 0 until 3) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (offset in 1..10) {
                            val roza = group * 10 + offset
                            val color = when {
                                roza <= completedFasts -> MaterialTheme.colorScheme.primary
                                roza == activeRoza -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }.let { if (roza == 30) it.copy(alpha = it.alpha * 0.6f) else it }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (roza == activeRoza) 14.dp else 10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Rahmat", "Maghfirat", "Nijat").forEachIndexed { index, name ->
                    LocalizedText(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (index + 1 == ashra) FontWeight.Bold else FontWeight.Normal,
                        color = if (index + 1 == ashra) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    LocalizedText(ashraTitle(ashra), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Sahih Ibn Khuzaymah 1887",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LaylatulQadrCard(upcomingNight: Int) {
    if (RamadanTimetable.isOddNight(upcomingNight)) {
        MessageCard(
            icon = Icons.Default.AutoAwesome,
            title = "Laylatul Qadr",
            text = template("Tonight is night {n}, one of the odd nights of the last Ashra. Seek Laylatul Qadr with extra worship.", "n" to "$upcomingNight"),
            reference = "Sahih al-Bukhari 2017",
            highlighted = true
        )
    } else {
        MessageCard(
            icon = Icons.Default.AutoAwesome,
            title = "Laylatul Qadr",
            text = "Last Ashra: Seek Laylatul Qadr on the odd nights of 21, 23, 25, 27 and 29.",
            reference = "Sahih al-Bukhari 2017"
        )
    }
}

@Composable
private fun DuaCard() {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                LocalizedText("Sehri & Iftar Duas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            LocalizedText("Intention for fasting (commonly recited):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text("وَبِصَوْمِ غَدٍ نَّوَيْتُ مِنْ شَهْرِ رَمَضَانَ", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            HorizontalDivider()
            LocalizedText("Dua when breaking the fast:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text("اللَّهُمَّ إِنِّي لَكَ صُمْتُ وَبِكَ آمَنْتُ وَعَلَيْكَ تَوَكَّلْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            LocalizedText("Reported in Abu Dawood (2358); wording beyond 'laka sumtu wa ala rizqika aftartu' is customary.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Timetable
// ---------------------------------------------------------------------------------------------

private sealed interface TimetableEntry {
    data class AshraDivider(val ashra: Int) : TimetableEntry
    data class Day(val day: RamadanTimetable.RamadanDay) : TimetableEntry
}

private enum class RowState { PAST, TODAY, UPCOMING }

private fun timetableEntries(days: List<RamadanTimetable.RamadanDay>): List<TimetableEntry> = buildList {
    days.forEach { day ->
        if ((day.roza - 1) % 10 == 0) add(TimetableEntry.AshraDivider(day.ashra))
        add(TimetableEntry.Day(day))
    }
}

private fun LazyListScope.timetableItems(
    entries: List<TimetableEntry>,
    timeZone: TimeZone,
    stateOf: (RamadanTimetable.RamadanDay) -> RowState
) {
    items(entries, key = {
        when (it) {
            is TimetableEntry.AshraDivider -> "ashra_${it.ashra}"
            is TimetableEntry.Day -> "roza_${it.day.roza}"
        }
    }) { entry ->
        when (entry) {
            is TimetableEntry.AshraDivider -> AshraDividerRow(entry.ashra)
            is TimetableEntry.Day -> TimetableRow(entry.day, stateOf(entry.day), timeZone)
        }
    }
}

@Composable
private fun TimetableHeader(title: String, onJumpToToday: (() -> Unit)?) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 40.dp)) {
                LocalizedText(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (onJumpToToday != null) {
                    TextButton(onClick = onJumpToToday, modifier = Modifier.testTag("ramadan_jump_today")) {
                        Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        LocalizedText("Go to today")
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                ColumnLabel("Roza", Modifier.width(44.dp))
                ColumnLabel("Date", Modifier.weight(1f))
                ColumnLabel("Sehri", Modifier.width(76.dp), TextAlign.End)
                ColumnLabel("Iftar", Modifier.width(76.dp), TextAlign.End)
            }
        }
    }
}

@Composable
private fun ColumnLabel(text: String, modifier: Modifier, textAlign: TextAlign = TextAlign.Start) {
    LocalizedText(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
private fun AshraDividerRow(ashra: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    ) {
        LocalizedText(
            text = ashraTitle(ashra),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    }
}

@Composable
private fun TimetableRow(day: RamadanTimetable.RamadanDay, state: RowState, timeZone: TimeZone) {
    val language = LocalAppLanguage.current
    val (dateText, isFriday) = remember(day.dateMillis, timeZone, language) {
        val calendar = Calendar.getInstance(timeZone).apply { timeInMillis = day.dateMillis }
        SimpleDateFormat("EEE, d MMM", appLocale(language)).apply { this.timeZone = timeZone }.format(day.dateMillis) to
            (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
    }
    val isToday = state == RowState.TODAY
    val contentColor = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (state) {
            RowState.TODAY -> MaterialTheme.colorScheme.primaryContainer
            RowState.PAST -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            RowState.UPCOMING -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        contentColor = contentColor,
        border = if (isToday) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (state == RowState.PAST) 0.6f else 1f)
            .testTag("ramadan_day_${day.roza}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = if (isToday) 14.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.width(44.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (day.dependsOnMoonSighting) "${day.roza}*" else "${day.roza}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                when {
                    state == RowState.PAST -> Icon(
                        Icons.Default.CheckCircle, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 2.dp).size(12.dp)
                    )
                    day.oddNightBefore -> Icon(
                        Icons.Default.Star, contentDescription = localizedUiText("Odd night"),
                        tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(start = 2.dp).size(12.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isFriday || isToday) FontWeight.SemiBold else FontWeight.Normal
                )
                if (isToday) {
                    LocalizedText("Today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = day.sehriFormatted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.width(76.dp)
            )
            Text(
                text = day.iftarFormatted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (state == RowState.PAST) contentColor else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(76.dp)
            )
        }
    }
}

@Composable
private fun Footnotes(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(6.dp))
            LocalizedText(
                "Odd night of the last Ashra (the night before that fast)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LocalizedText(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---------------------------------------------------------------------------------------------
// Building blocks
// ---------------------------------------------------------------------------------------------

@Composable
private fun HeroCard(testTag: String, content: @Composable ColumnScope.() -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(primary, lerp(primary, Color.Black, 0.35f))))
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
private fun HeroLabel(text: String) {
    LocalizedText(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
    )
}

@Composable
private fun HeroInfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
    }
}

@Composable
private fun HeroEndpoint(icon: ImageVector, label: String, time: String, alignment: Alignment.Horizontal, modifier: Modifier) {
    Column(horizontalAlignment = alignment, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            LocalizedText(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
        }
        Text(time, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
private fun TimeCard(label: String, time: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            LocalizedText(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = time, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MessageCard(icon: ImageVector, title: String, text: String, reference: String? = null, highlighted: Boolean = false) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Icon(
                icon, contentDescription = null,
                tint = if (highlighted) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LocalizedText(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                // Templated text is already localized; LocalizedText leaves unknown phrases untouched.
                LocalizedText(text = text, style = MaterialTheme.typography.bodySmall)
                if (reference != null) {
                    Text(reference, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Formatting
// ---------------------------------------------------------------------------------------------

/** Localizes a whole phrase with `{name}` placeholders, then fills them in. */
@Composable
private fun template(phrase: String, vararg values: Pair<String, String>): String =
    values.fold(localizedUiText(phrase)) { text, (name, value) -> text.replace("{$name}", value) }

private fun ashraTitle(ashra: Int): String = when (ashra) {
    1 -> "1st Ashra · Rahmat (Mercy)"
    2 -> "2nd Ashra · Maghfirat (Forgiveness)"
    else -> "3rd Ashra · Nijat (Freedom from Hellfire)"
}

@Composable
private fun formatDuration(millis: Long): String {
    val totalMinutes = (millis / 60_000).coerceAtLeast(0)
    return template("{h}h {m}m", "h" to "${totalMinutes / 60}", "m" to "${totalMinutes % 60}")
}

private fun calendarCountdown(fromMillis: Long, toMillis: Long, timeZone: TimeZone): Pair<Int, Long> {
    if (toMillis <= fromMillis) return 0 to 0L
    val from = Calendar.getInstance(timeZone).apply { timeInMillis = fromMillis }
    val to = Calendar.getInstance(timeZone).apply { timeInMillis = toMillis }
    val secondsInDay = 24L * 60 * 60
    val fromSeconds = from.get(Calendar.HOUR_OF_DAY) * 3600L + from.get(Calendar.MINUTE) * 60L + from.get(Calendar.SECOND)
    val toSeconds = to.get(Calendar.HOUR_OF_DAY) * 3600L + to.get(Calendar.MINUTE) * 60L + to.get(Calendar.SECOND)
    listOf(from, to).forEach {
        it.set(Calendar.HOUR_OF_DAY, 0); it.set(Calendar.MINUTE, 0)
        it.set(Calendar.SECOND, 0); it.set(Calendar.MILLISECOND, 0)
    }
    var days = 0
    while (from.before(to) && days < 400) { from.add(Calendar.DAY_OF_MONTH, 1); days++ }
    var seconds = toSeconds - fromSeconds
    if (seconds < 0) { days--; seconds += secondsInDay }
    return days.coerceAtLeast(0) to (seconds * 1000L)
}

private fun formatCountdown(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60)
}
