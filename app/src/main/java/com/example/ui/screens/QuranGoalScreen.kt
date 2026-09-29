@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quran.PdfQuran
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import kotlin.math.ceil

private enum class LogDay(val label: String, val offset: Long) { TODAY("Today", 0), YESTERDAY("Yesterday", 1) }

@Composable
internal fun QuranGoalScreen(onBack: () -> Unit, onOpenQuran: () -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val store = remember { quranGoalStore(context) }
    val locale = LocalAppLocale.current
    val today = remember { LocalDate.now() }
    var revision by remember { mutableIntStateOf(0) }
    var logDay by remember { mutableStateOf(LogDay.TODAY) }
    var customPages by rememberSaveable { mutableStateOf(false) }
    var editPosition by rememberSaveable { mutableStateOf(false) }
    var pickDate by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    var celebrate by remember { mutableStateOf(false) }

    // Every value below is re-read after each change.
    val target = remember(revision) { store.targetOn(today, today) }
    val pages = remember(revision) { store.pagesOn(today) }
    val readerPages = remember(revision) { store.readerPagesOn(today) }
    val logDate = today.minusDays(logDay.offset)
    val logPages = remember(revision, logDay) { store.pagesOn(logDate) }
    val streak = remember(revision) { store.currentStreak(today) }
    val best = remember(revision) { store.bestStreak(today) }
    val stats = remember(revision) { store.stats(today) }
    val position = remember(revision) { store.position }
    val completed = remember(revision) { store.completedKhatms }
    val forecast = remember(revision) { store.forecastFinish(today) }
    val khatmDates = remember(revision) { store.khatmDates }
    val mode = remember(revision) { store.mode }
    val finishBy = remember(revision) { store.finishBy }
    val daysLeft = remember(revision) { store.daysLeft(today) }
    val autoLog = remember(revision) { store.readerAutoLog }
    val dateFormat = remember(locale) { DateTimeFormatter.ofPattern("d MMM yyyy", locale) }

    fun log(delta: Int) {
        if (store.log(logDate, delta, today)) celebrate = true
        revision++
    }

    ToolScreen(DailyTool.QURAN_GOAL, onBack, modifier) { accent ->
        // Today at a glance.
        item(key = "hero") {
            HeroCard(accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(pages.toFloat() / target, accent, size = 118.dp, stroke = 11.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$pages", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("/ $target", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LocalizedText(
                            if (pages >= target) "Today's goal reached. MashaAllah!" else "Pages read today",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (pages < target) {
                            Text(
                                "${target - pages} ${localizedUiText("pages left today")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = accent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (readerPages > 0) {
                            Text(
                                "${localizedUiText("From Quran reader")}: $readerPages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            GoalPill(Icons.Default.LocalFireDepartment, "${localizedUiText("Day streak")}: $streak", accent)
                            if (best > 0) GoalPill(Icons.Default.EmojiEvents, "${localizedUiText("Best")}: $best", accent)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(onClick = onOpenQuran, modifier = Modifier.fillMaxWidth().testTag("quran_goal_open_reader")) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${localizedUiText("Continue reading")} · ${localizedUiText("Para")} ${madaniParaOf(position + 1)}, ${localizedUiText("Page")} ${position + 1}",
                        maxLines = 1
                    )
                }
            }
        }

        // Logging.
        item(key = "log") {
            ToolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("Log your reading")
                    Spacer(Modifier.weight(1f))
                    SingleChoiceSegmentedButtonRow {
                        LogDay.entries.forEachIndexed { index, day ->
                            SegmentedButton(
                                selected = logDay == day,
                                onClick = { logDay = day },
                                shape = SegmentedButtonDefaults.itemShape(index, LogDay.entries.size),
                                icon = {}
                            ) { LocalizedText(day.label, maxLines = 1, style = MaterialTheme.typography.labelMedium) }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { log(-1) }, enabled = logPages > 0, modifier = Modifier.size(52.dp).testTag("quran_remove_page")) {
                        Icon(Icons.Default.Remove, contentDescription = localizedUiText("Decrease"))
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$logPages", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = accent)
                        LocalizedText(if (logPages == 1) "page" else "pages", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilledIconButton(onClick = { log(1) }, modifier = Modifier.size(52.dp).testTag("quran_add_page")) {
                        Icon(Icons.Default.Add, contentDescription = localizedUiText("Increase"))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickAdd("+2", Modifier.weight(1f)) { log(2) }
                    QuickAdd("+5", Modifier.weight(1f)) { log(5) }
                    QuickAdd(localizedUiText("½ Para"), Modifier.weight(1f)) { log(PARA_PAGES / 2) }
                    QuickAdd(localizedUiText("1 Para"), Modifier.weight(1f)) { log(PARA_PAGES) }
                }
                OutlinedButton(onClick = { customPages = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    LocalizedText("Enter number of pages", maxLines = 1)
                }
            }
        }

        // Current khatm.
        item(key = "khatm") {
            ToolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        LocalizedText("Khatm progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${localizedUiText("Para")} ${madaniParaOf(position + 1)} · ${localizedUiText("Page")} ${position + 1} / $MADANI_PAGES",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("${position * 100 / MADANI_PAGES}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent)
                }
                ParaTrack(position, accent)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("${MADANI_PAGES - position}", "Pages left", Modifier.weight(1f))
                    MiniStat("${30 - position / PARA_PAGES}", "Paras left", Modifier.weight(1f))
                    MiniStat("$completed", "Khatms completed", Modifier.weight(1f))
                }
                if (forecast != null) {
                    GoalNote(Icons.Default.Schedule, "${localizedUiText("At your current pace, this khatm ends on")} ${forecast.format(dateFormat)}", accent)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { editPosition = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.EditLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        LocalizedText("Set position", maxLines = 1)
                    }
                    OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        LocalizedText("Start new khatm", maxLines = 1)
                    }
                }
            }
        }

        // Goal.
        item(key = "goal") {
            ToolCard {
                SectionTitle("Your goal")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    QuranGoalMode.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = mode == option,
                            onClick = {
                                store.mode = option
                                if (option == QuranGoalMode.FINISH_BY && store.finishBy == null) store.finishBy = today.plusDays(29)
                                revision++
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, QuranGoalMode.entries.size)
                        ) {
                            LocalizedText(if (option == QuranGoalMode.PAGES) "Pages per day" else "Finish by date", maxLines = 1)
                        }
                    }
                }
                if (mode == QuranGoalMode.PAGES) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LocalizedText("Daily goal", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Stepper(target, onDecrement = { store.dailyGoal = target - 1; revision++ }, onIncrement = { store.dailyGoal = target + 1; revision++ }, minimum = 1)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 4, 10, 20).forEach { preset ->
                            FilterChip(
                                selected = target == preset,
                                onClick = { store.dailyGoal = preset; revision++ },
                                label = { Text(if (preset == PARA_PAGES) localizedUiText("1 Para") else "$preset") }
                            )
                        }
                    }
                    GoalNote(
                        Icons.Default.Flag,
                        "${localizedUiText("Days to finish one khatm")}: ${ceil(MADANI_PAGES / target.toDouble()).toInt()} · " +
                            "${localizedUiText("Khatms in a year")}: ${"%.1f".format(locale, 365.0 * target / MADANI_PAGES)}",
                        accent
                    )
                } else {
                    Surface(
                        onClick = { pickDate = true },
                        shape = RoundedCornerShape(14.dp),
                        color = accent.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = accent)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                LocalizedText("Finish this khatm by", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(finishBy?.format(dateFormat) ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.Edit, contentDescription = localizedUiText("Change date"), tint = accent)
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7 to "1 week", 30 to "30 days", 60 to "2 months", 90 to "3 months", 365 to "1 year").forEach { (days, label) ->
                            FilterChip(
                                selected = finishBy == today.plusDays(days - 1L),
                                onClick = { store.finishBy = today.plusDays(days - 1L); revision++ },
                                label = { LocalizedText(label) }
                            )
                        }
                    }
                    if (daysLeft != null && daysLeft <= 0) {
                        GoalNote(Icons.Default.Warning, localizedUiText("This date has passed. Choose a new date."), MaterialTheme.colorScheme.error)
                    } else if (daysLeft != null) {
                        GoalNote(
                            Icons.Default.Flag,
                            "${localizedUiText("Read")} $target ${localizedUiText("pages a day")} · $daysLeft ${localizedUiText("days left")}",
                            accent
                        )
                    }
                }
            }
        }

        // History.
        item(key = "history") {
            ToolCard {
                SectionTitle("Last 5 weeks")
                ReadingHeatmap(today, accent) { day -> store.pagesOn(day) to store.targetOn(day, today) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("${stats.weekPages}", "This week", Modifier.weight(1f))
                    MiniStat("${stats.monthPages}", "30 days", Modifier.weight(1f))
                    MiniStat("${stats.activeDays}/30", "Active days", Modifier.weight(1f))
                }
            }
        }

        if (khatmDates.isNotEmpty()) {
            item(key = "khatms") {
                ToolCard {
                    SectionTitle("Completed khatms")
                    khatmDates.asReversed().forEachIndexed { index, date ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("${localizedUiText("Khatm")} ${completed - index}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(date.format(dateFormat), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        item(key = "auto") {
            ToolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        LocalizedText("Count pages from the Quran reader", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        LocalizedText(
                            "Each new page you turn in the Quran reader is added here, once a day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = autoLog,
                        onCheckedChange = { store.readerAutoLog = it; revision++ },
                        modifier = Modifier.testTag("quran_reader_auto")
                    )
                }
            }
        }

        item(key = "hadith") {
            InfoNote("The most beloved deeds to Allah are those done regularly, even if they are few. (Sahih al-Bukhari 6464, Sahih Muslim 783)")
        }
        item(key = "note") { InfoNote("Page counts follow the 604-page Madani mushaf; other editions differ.") }
    }

    if (customPages) {
        NumberDialog(
            title = "Add pages",
            label = "Pages",
            max = MADANI_PAGES,
            onDismiss = { customPages = false },
            onConfirm = { log(it); customPages = false }
        )
    }

    if (editPosition) {
        PositionDialog(
            current = position,
            onDismiss = { editPosition = false },
            onConfirm = { store.setPosition(it); revision++; editPosition = false }
        )
    }

    if (pickDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (finishBy ?: today.plusDays(29)).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isBefore(today)
            }
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { store.finishBy = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    revision++
                    pickDate = false
                }) { LocalizedText("Done") }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { LocalizedText("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { LocalizedText("Start new khatm?") },
            text = { LocalizedText("This khatm goes back to page 1. Completed khatms, daily logs and streak stay.") },
            confirmButton = {
                TextButton(onClick = { store.startNewKhatm(); revision++; confirmReset = false }) { LocalizedText("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { LocalizedText("Cancel") } }
        )
    }

    if (celebrate) {
        AlertDialog(
            onDismissRequest = { celebrate = false },
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(40.dp)) },
            title = { LocalizedText("Khatm complete. MashaAllah!", textAlign = TextAlign.Center) },
            text = {
                LocalizedText(
                    "May Allah accept your recitation. Your next khatm starts from page 1.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = { TextButton(onClick = { celebrate = false }) { LocalizedText("Alhamdulillah") } }
        )
    }
}

@Composable
private fun GoalPill(icon: ImageVector, text: String, accent: Color) {
    Surface(shape = CircleShape, color = accent.copy(alpha = 0.14f)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun GoalNote(icon: ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickAdd(label: String, modifier: Modifier, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = modifier.heightIn(min = 44.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
        Text(label, maxLines = 1, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), modifier = modifier) {
        Column(Modifier.padding(vertical = 10.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            LocalizedText(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

/** Thirty segments, one per para: done, current and still to read. */
@Composable
private fun ParaTrack(position: Int, accent: Color) {
    val currentPara = madaniParaOf(position + 1)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearProgressIndicator(
            progress = { position.toFloat() / MADANI_PAGES },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
            color = accent,
            trackColor = accent.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {}
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (para in 1..30) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                para < currentPara -> accent
                                para == currentPara -> accent.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            }
                        )
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Text("1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text("15", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text("30", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Five weeks of reading, Monday first; full colour when that day's goal was met. */
@Composable
private fun ReadingHeatmap(today: LocalDate, accent: Color, dayOf: (LocalDate) -> Pair<Int, Int>) {
    val locale = LocalAppLocale.current
    val start = today.minusDays(today.dayOfWeek.value - 1L).minusWeeks(4)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (offset in 0L until 7L) {
                Text(
                    start.plusDays(offset).dayOfWeek.getDisplayName(DateTextStyle.NARROW, locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        for (week in 0L until 5L) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (offset in 0L until 7L) {
                    val day = start.plusWeeks(week).plusDays(offset)
                    val future = day.isAfter(today)
                    val (count, goal) = if (future) 0 to 1 else dayOf(day)
                    val color = when {
                        future || count == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (future) 0.3f else 0.8f)
                        count >= goal -> accent
                        else -> accent.copy(alpha = 0.25f + 0.45f * count / goal.coerceAtLeast(1))
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .then(if (day == today) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp)) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!future) {
                            Text(
                                if (count > 0) "$count" else "${day.dayOfMonth}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    count >= goal && count > 0 -> Color.White
                                    count > 0 -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(accent.copy(alpha = 0.4f)))
            LocalizedText("Some reading", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(accent))
            LocalizedText("Goal met", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NumberDialog(title: String, label: String, max: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val value = text.toIntOrNull()?.takeIf { it in 1..max }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { LocalizedText(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { new -> text = new.filter(Char::isDigit).take(3) },
                label = { LocalizedText(label) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("quran_custom_pages")
            )
        },
        confirmButton = { TextButton(onClick = { value?.let(onConfirm) }, enabled = value != null) { LocalizedText("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } }
    )
}

/**
 * Sets where the reader is in the current khatm: by para, by Madani page number, or from the page
 * last opened in the Quran reader.
 */
@Composable
private fun PositionDialog(current: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var page by remember { mutableIntStateOf(current + 1) }
    var pageText by rememberSaveable { mutableStateOf("${current + 1}") }
    val readerPage = remember {
        runCatching { PdfQuran.lastBook()?.let { book -> toMadaniPage(PdfQuran.lastPage(book) + 1, book.paraPages, book.pageCount) } }.getOrNull()
    }
    fun select(newPage: Int) {
        page = newPage.coerceIn(1, MADANI_PAGES)
        pageText = "$page"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { LocalizedText("Where are you in the Quran?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (readerPage != null) {
                    Surface(
                        onClick = { select(readerPage) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                LocalizedText("Use Quran reader position", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${localizedUiText("Para")} ${madaniParaOf(readerPage)} · ${localizedUiText("Page")} $readerPage",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                LocalizedText("Choose a para", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = 6
                ) {
                    for (para in 1..30) {
                        val selected = madaniParaOf(page) == para
                        Box(
                            Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(role = Role.Button) { select(MADANI_PARA_STARTS[para - 1]) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$para",
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = pageText,
                    onValueChange = { new ->
                        pageText = new.filter(Char::isDigit).take(3)
                        pageText.toIntOrNull()?.takeIf { it in 1..MADANI_PAGES }?.let { page = it }
                    },
                    label = { LocalizedText("Page (1–604)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("quran_position_page")
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(page - 1) }) { LocalizedText("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } }
    )
}
