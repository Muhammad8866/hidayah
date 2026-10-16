package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.ScreenHeader
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.formatHijriDate
import com.example.ui.localization.localizedUiText
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.WeekFields
import java.text.NumberFormat

private const val ROZA_NOTE_LIMIT = 200

/** Each mark has an icon as well as a colour, so the calendar never relies on colour alone. */
private val RozaMark.icon: ImageVector
    get() = when (this) {
        RozaMark.RAMADAN -> Icons.Default.Star
        RozaMark.NAFL -> Icons.Default.Favorite
        RozaMark.MISSED -> Icons.Default.Close
        RozaMark.QAZA -> Icons.Default.Check
    }

@Composable
private fun RozaMark.color(): Color = when (this) {
    RozaMark.RAMADAN -> MaterialTheme.colorScheme.primary
    RozaMark.NAFL -> MaterialTheme.colorScheme.tertiary
    RozaMark.MISSED -> MaterialTheme.colorScheme.error
    RozaMark.QAZA -> MaterialTheme.colorScheme.secondary
}

@Composable
internal fun RozaTrackerScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("roza_tracker_screen")
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE) }
    val store = remember(prefs) { RozaTrackerStore(prefs) }
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || key.startsWith("roza_")) revision++
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            today = LocalDate.now()
            kotlinx.coroutines.delay(30_000)
        }
    }
    var selectedDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    val selected = LocalDate.ofEpochDay(selectedDay).coerceAtMost(today)
    val logs = remember(revision) { store.logs() }
    val notes = remember(revision) { store.notes() }
    val balance = remember(revision) { store.balance }
    val stats = remember(logs, today) { RozaLogic.stats(logs, today) }
    val ramadan = remember(logs, today) { RozaLogic.ramadanProgress(logs, today) }
    val locale = LocalAppLocale.current
    val numbers = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val dateFormat = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var count by rememberSaveable { mutableStateOf("") }
    var showClear by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val savedText = localizedUiText("Fast log saved")
    val undoText = localizedUiText("Undo")
    val conflictText = localizedUiText("This change would remove a missed fast already made up. Update its Qaza entry first.")
    val noDebtText = localizedUiText("Add missed fasts before logging a Qaza fast.")
    fun updateLog(next: RozaMark?) {
        val date = selected
        val previous = store.mark(date)
        if (previous == next) return
        if (!store.setMark(date, next, today)) {
            scope.launch { snackbar.showSnackbar(if (next == RozaMark.QAZA) noDebtText else conflictText) }
            return
        }
        revision++
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            if (snackbar.showSnackbar(savedText, undoText, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) {
                if (store.mark(date) == next && !store.setMark(date, previous, LocalDate.now())) {
                    snackbar.showSnackbar(conflictText)
                }
                revision++
            }
        }
    }
    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            ScreenHeader(
                DailyTool.ROZA_TRACKER.title,
                subtitle = DailyTool.ROZA_TRACKER.subtitle,
                onBack = onBack,
                leading = { IconBadge(DailyTool.ROZA_TRACKER.icon, themedAccent(DailyTool.ROZA_TRACKER.accent), 44.dp) },
                actions = {
                RozaActionsMenu(
                    store = store,
                    today = today,
                    onChanged = { revision++ },
                    onMessage = { message -> scope.launch { snackbar.showSnackbar(message) } }
                )
            })
            LazyColumn(
                modifier = Modifier.testTag("roza_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { RozaHeroCard(balance, stats, numbers, onAdd = { count = ""; showAdd = true }) }
                item { RozaStatsCard(stats, numbers) }
                if (ramadan != null) item { RozaRamadanCard(ramadan, locale) }
                item {
                    RozaDayEditor(
                        selected = selected, today = today, mark = logs[selected], balance = balance,
                        note = notes[selected], dateFormat = dateFormat,
                        onSelect = { selectedDay = it.toEpochDay() },
                        onMark = ::updateLog,
                        onNote = { store.setNote(selected, it); revision++ },
                        onClear = { showClear = true }
                    )
                }
                item {
                    RozaCalendarCard(
                        selected = selected, today = today, logs = logs, notes = notes,
                        numbers = numbers, dateFormat = dateFormat,
                        onSelect = { selectedDay = it.toEpochDay() }
                    )
                }
                item { RozaQazaPlanCard(balance, today, Modifier.fillMaxWidth()) }
                item { RozaSunnahFastsCard(today, Modifier.fillMaxWidth()) }
                item { RozaRecentDays(today, logs, dateFormat, onSelect = { selectedDay = it.toEpochDay() }) }
                item {
                    LocalizedText(
                        "Missed fasts add to Qaza. Completed Qaza fasts reduce the balance. Ramadan and Nafl fasts do not change it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    if (showClear) AlertDialog(onDismissRequest = { showClear = false }, title = { LocalizedText("Clear day's log") },
        text = { LocalizedText("Clearing this entry also updates your Qaza balance.") },
        confirmButton = { TextButton(onClick = { showClear = false; updateLog(null) }) { LocalizedText("Clear day's log") } },
        dismissButton = { TextButton(onClick = { showClear = false }) { LocalizedText("Cancel") } })
    if (showAdd) AlertDialog(
        onDismissRequest = { showAdd = false },
        title = { LocalizedText("Add previous missed fasts") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LocalizedText("Only add fasts that are not already recorded in your daily log.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(value = count, onValueChange = { value -> if (value.length <= 9 && value.all { it in '0'..'9' }) count = value },
                label = { LocalizedText("Number of fasts") }, singleLine = true,
                isError = count.isNotEmpty() && (count.toIntOrNull() ?: 0) <= 0,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        } },
        confirmButton = { TextButton(enabled = (count.toIntOrNull() ?: 0) > 0 && (count.toLongOrNull() ?: 0) + balance <= Int.MAX_VALUE, onClick = {
            store.addMissed(count.toInt()); revision++; showAdd = false
        }) { LocalizedText("Add") } },
        dismissButton = { TextButton(onClick = { showAdd = false }) { LocalizedText("Cancel") } }
    )
}

@Composable
private fun RozaHeroCard(balance: Int, stats: RozaStats, numbers: NumberFormat, onAdd: () -> Unit) {
    val accent = themedAccent(DailyTool.ROZA_TRACKER.accent)
    HeroCard(accent) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LocalizedText("Qaza roza balance", style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
            Text(
                numbers.format(balance),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (balance == 0) accent else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("roza_balance")
            )
            LocalizedText(
                if (balance == 0) "No Qaza fasts pending" else "Fasts to make up",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RozaStreak("Current streak", numbers.format(stats.currentStreak), Modifier.weight(1f))
                RozaStreak("Longest streak", numbers.format(stats.longestStreak), Modifier.weight(1f))
            }
            Button(
                onClick = onAdd,
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = MaterialTheme.colorScheme.surface)
            ) { LocalizedText("Add previous missed fasts") }
        }
    }
}

@Composable
private fun RozaStreak(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LocalizedText(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RozaStatsCard(stats: RozaStats, numbers: NumberFormat) {
    ToolCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("All-time summary")
            RozaStat("Fasts completed", numbers.format(stats.total), MaterialTheme.colorScheme.onSurface, null)
            listOf(
                listOf(RozaMark.RAMADAN to stats.ramadan, RozaMark.NAFL to stats.nafl),
                listOf(RozaMark.QAZA to stats.qazaDone, RozaMark.MISSED to stats.missed)
            ).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (mark, value) ->
                        val label = when (mark) {
                            RozaMark.QAZA -> "Qaza completed"
                            RozaMark.MISSED -> "Missed fasts"
                            else -> mark.label
                        }
                        RozaStat(label, numbers.format(value), mark.color(), mark.icon, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RozaStat(label: String, value: String, color: Color, icon: ImageVector?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon != null) Icon(icon, null, Modifier.size(18.dp), tint = color)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = color)
        }
        LocalizedText(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RozaRamadanCard(progress: RozaRamadanProgress, locale: java.util.Locale) {
    val plain = remember(locale) { NumberFormat.getIntegerInstance(locale).apply { isGroupingUsed = false } }
    ToolCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(RozaMark.RAMADAN.icon, null, Modifier.size(20.dp), tint = RozaMark.RAMADAN.color())
                LocalizedText("Ramadan progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(plain.format(progress.hijriYear), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RozaStat("Fasted", plain.format(progress.fasted), LocalContentColor.current, null, Modifier.weight(1f))
                RozaStat("Missed fasts", plain.format(progress.missed), LocalContentColor.current, null, Modifier.weight(1f))
                RozaStat("Not logged yet", plain.format(progress.unlogged), LocalContentColor.current, null, Modifier.weight(1f))
            }
            val elapsed = progress.daysElapsed.coerceAtLeast(1)
            LinearProgressIndicator(
                progress = { (progress.fasted.toFloat() / elapsed).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {}
            )
            Text("${plain.format(progress.fasted)} / ${plain.format(progress.daysElapsed)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RozaDayEditor(
    selected: LocalDate,
    today: LocalDate,
    mark: RozaMark?,
    balance: Int,
    note: String?,
    dateFormat: DateTimeFormatter,
    onSelect: (LocalDate) -> Unit,
    onMark: (RozaMark) -> Unit,
    onNote: (String?) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val suggested = remember(selected) { RozaLogic.suggestedMark(selected) }
    val occasion = remember(selected) { RozaSunnahFasts.occasion(selected) }
    val hijriText = remember(selected, language) { formatHijriDate(RozaLogic.hijri(selected), language) }
    ToolCard {
        Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Daily fast log")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onSelect(selected.minusDays(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, localizedUiText("Previous day"))
                }
                TextButton(modifier = Modifier.weight(1f), onClick = {
                    DatePickerDialog(context, { _, year, monthIndex, day ->
                        onSelect(LocalDate.of(year, monthIndex + 1, day))
                    }, selected.year, selected.monthValue - 1, selected.dayOfMonth).apply {
                        datePicker.maxDate = System.currentTimeMillis()
                    }.show()
                }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(selected.format(dateFormat), style = MaterialTheme.typography.titleMedium)
                        Text(hijriText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = { onSelect(selected.plusDays(1)) }, enabled = selected < today) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, localizedUiText("Next day"))
                }
            }
            if (selected != today) TextButton(onClick = { onSelect(today) }) { LocalizedText("Today") }
            if (occasion != null) RozaOccasion(occasion)
            LocalizedText(mark?.label ?: "Not logged yet", color = mark?.color() ?: MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold)
            RozaMark.entries.forEach { option ->
                val active = mark == option
                RozaMarkOption(
                    option = option,
                    active = active,
                    enabled = option != RozaMark.QAZA || balance.toLong() - (mark?.debt ?: 0) > 0 || active,
                    suggested = option == suggested && mark == null,
                    onClick = { onMark(option) }
                )
            }
            val warning = mark?.let { RozaLogic.warning(selected, it) }
            if (warning != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    LocalizedText(warning.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            if (balance == 0 && mark != RozaMark.QAZA) LocalizedText("Add missed fasts before logging a Qaza fast.", style = MaterialTheme.typography.bodySmall)
            RozaNoteField(selected, note, onNote)
            if (mark != null) TextButton(onClick = onClear) { LocalizedText("Clear day's log") }
        }
    }
}

@Composable
private fun RozaOccasion(occasion: RozaSunnahDay) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LocalizedText(occasion.title, style = MaterialTheme.typography.titleSmall)
            LocalizedText(occasion.reference, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RozaMarkOption(option: RozaMark, active: Boolean, enabled: Boolean, suggested: Boolean, onClick: () -> Unit) {
    val color = option.color()
    val container by animateColorAsState(if (active) color.copy(alpha = 0.16f) else Color.Transparent, label = "roza_option")
    val description = when (option) {
        RozaMark.RAMADAN, RozaMark.NAFL -> "Does not change Qaza balance"
        RozaMark.MISSED -> "Adds one fast to Qaza balance"
        RozaMark.QAZA -> "Makes up one missed fast"
    }
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth()
            .testTag("roza_${option.name.lowercase()}")
            .alpha(if (enabled) 1f else 0.38f)
            .clip(shape)
            .background(container)
            .border(BorderStroke(if (active) 2.dp else 1.dp, if (active) color else MaterialTheme.colorScheme.outlineVariant), shape)
            .selectable(selected = active, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(36.dp).background(color.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(option.icon, null, Modifier.size(20.dp), tint = color)
        }
        Column(Modifier.weight(1f)) {
            LocalizedText(option.label, style = MaterialTheme.typography.titleSmall)
            LocalizedText(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (suggested) LocalizedText("Suggested", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        if (active) Icon(Icons.Default.CheckCircle, null, tint = color)
    }
}

/** Saved when the keyboard's Done is pressed or the field loses focus. */
@Composable
private fun RozaNoteField(date: LocalDate, stored: String?, onSave: (String?) -> Unit) {
    var text by remember(date, stored) { mutableStateOf(stored.orEmpty()) }
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    fun save() {
        val next = text.trim()
        if (next != stored.orEmpty()) onSave(next.ifEmpty { null })
    }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it.take(ROZA_NOTE_LIMIT) },
        label = { LocalizedText("Note (optional)") },
        supportingText = { Text("${text.length} / $ROZA_NOTE_LIMIT") },
        maxLines = 3,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { save(); focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth().testTag("roza_note").onFocusChanged {
            if (focused && !it.isFocused) save()
            focused = it.isFocused
        }
    )
}

@Composable
private fun RozaCalendarCard(
    selected: LocalDate,
    today: LocalDate,
    logs: Map<LocalDate, RozaMark>,
    notes: Map<LocalDate, String>,
    numbers: NumberFormat,
    dateFormat: DateTimeFormatter,
    onSelect: (LocalDate) -> Unit
) {
    val locale = LocalAppLocale.current
    val monthFormat = remember(locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val month = YearMonth.from(selected)
    val monthlyLogs = logs.filterKeys { YearMonth.from(it) == month && it <= today }
    val fasts = monthlyLogs.values.count { it != RozaMark.MISSED }
    val qaza = monthlyLogs.values.count { it == RozaMark.QAZA }
    val firstDay = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val weekdays = remember(firstDay) { (0L..6L).map { firstDay.plus(it) } }
    val cells = remember(month, firstDay) {
        val blanks = (month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7
        (List<LocalDate?>(blanks) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }).chunked(7)
    }
    val occasions = remember(month) { (1..month.lengthOfMonth()).map { month.atDay(it) }.filter { RozaSunnahFasts.occasion(it) != null }.toSet() }
    ToolCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onSelect(selected.minusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, localizedUiText("Previous month"))
                }
                Text(selected.format(monthFormat), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                IconButton(enabled = month < YearMonth.from(today), onClick = {
                    onSelect(selected.plusMonths(1).coerceAtMost(today))
                }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, localizedUiText("Next month")) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) { Text(numbers.format(fasts), style = MaterialTheme.typography.headlineMedium); LocalizedText("Fasts completed") }
                Column(Modifier.weight(1f)) { Text(numbers.format(qaza), style = MaterialTheme.typography.headlineMedium); LocalizedText("Qaza completed") }
            }
            LocalizedText("Days logged", style = MaterialTheme.typography.labelLarge)
            val elapsed = if (month == YearMonth.from(today)) today.dayOfMonth else month.lengthOfMonth()
            LinearProgressIndicator(
                progress = { monthlyLogs.size.toFloat() / elapsed },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {}
            )
            Text("${numbers.format(monthlyLogs.size)} / ${numbers.format(elapsed)}", style = MaterialTheme.typography.bodySmall)
            LocalizedText("Select a day to view or edit", style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                weekdays.forEach { day ->
                    Text(
                        day.getDisplayName(java.time.format.TextStyle.SHORT, locale),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            cells.forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { date ->
                        if (date == null) Spacer(Modifier.weight(1f)) else RozaDayCell(
                            date = date, mark = logs[date], isSelected = date == selected, isToday = date == today,
                            enabled = date <= today, hasDot = date in notes || date in occasions,
                            numbers = numbers, dateFormat = dateFormat, onClick = { onSelect(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            RozaLegend()
        }
    }
}

@Composable
private fun RozaDayCell(
    date: LocalDate,
    mark: RozaMark?,
    isSelected: Boolean,
    isToday: Boolean,
    enabled: Boolean,
    hasDot: Boolean,
    numbers: NumberFormat,
    dateFormat: DateTimeFormatter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val markColor = mark?.color()
    val container by animateColorAsState(
        when {
            isSelected -> scheme.primary
            markColor != null -> markColor.copy(alpha = 0.16f)
            else -> Color.Transparent
        }, label = "roza_day"
    )
    val content = when {
        isSelected -> scheme.onPrimary
        else -> scheme.onSurface
    }
    val label = localizedUiText(mark?.label ?: "Not logged yet")
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier
            .testTag("roza_day_$date")
            .heightIn(min = 48.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .clip(shape)
            .background(container)
            .then(if (isToday) Modifier.border(2.dp, scheme.primary, shape) else Modifier)
            .selectable(selected = isSelected, enabled = enabled, onClick = onClick)
            .semantics { contentDescription = "${date.format(dateFormat)}, $label" }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(numbers.format(date.dayOfMonth), color = content, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else null, maxLines = 1, softWrap = false)
        Row(Modifier.height(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (mark != null) Icon(mark.icon, null, Modifier.size(12.dp), tint = if (isSelected) content else markColor ?: content)
            if (hasDot) Box(Modifier.size(5.dp).background(if (isSelected) content else scheme.tertiary, CircleShape))
        }
    }
}

@Composable
private fun RozaLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        RozaMark.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { mark ->
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(mark.icon, null, Modifier.size(14.dp), tint = mark.color())
                        LocalizedText(mark.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.padding(horizontal = 4.dp).size(6.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
            LocalizedText("Note or Sunnah fast day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RozaRecentDays(today: LocalDate, logs: Map<LocalDate, RozaMark>, dateFormat: DateTimeFormatter, onSelect: (LocalDate) -> Unit) {
    Column {
        SectionTitle("Last 7 days")
        repeat(7) { offset ->
            val date = today.minusDays(offset.toLong())
            val mark = logs[date]
            TextButton(onClick = { onSelect(date) }, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(28.dp).background((mark?.color() ?: MaterialTheme.colorScheme.outline).copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center) {
                    if (mark != null) Icon(mark.icon, null, Modifier.size(16.dp), tint = mark.color())
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(date.format(dateFormat))
                    LocalizedText(mark?.label ?: "Not logged yet", style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }
        }
    }
}
