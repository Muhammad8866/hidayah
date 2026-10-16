package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val BACKUP_VERSION = 1
private const val MAX_BACKUP_BYTES = 1024 * 1024
private const val MAX_NOTE_LENGTH = 500
private const val MAX_RANGE_DAYS = 60

internal object RozaBackup {
    fun export(store: RozaTrackerStore): String = JSONObject().apply {
        put("app", "roza_tracker")
        put("version", BACKUP_VERSION)
        put("balance", store.balance)
        put("logs", JSONObject().apply { store.logs().toSortedMap().forEach { (date, mark) -> put(date.toString(), mark.name) } })
        put("notes", JSONObject().apply { store.notes().toSortedMap().forEach { (date, note) -> put(date.toString(), note) } })
    }.toString(2)

    /** Validates the whole backup before touching the store; false means nothing changed. */
    fun import(store: RozaTrackerStore, json: String, today: LocalDate = LocalDate.now()): Boolean {
        val parsed = runCatching {
            val root = JSONObject(json)
            require(root.getInt("version") in 1..BACKUP_VERSION)
            // Reject fractional or out-of-range numbers instead of letting org.json coerce them.
            val balance = (root.get("balance") as? Int) ?: throw IllegalArgumentException()
            require(balance >= 0)
            val logs = root.getJSONObject("logs").let { obj ->
                obj.keys().asSequence().associate { key ->
                    val date = LocalDate.parse(key)
                    require(date <= today)
                    date to RozaMark.valueOf(obj.get(key) as String)
                }
            }
            val notes = root.optJSONObject("notes")?.let { obj ->
                obj.keys().asSequence().mapNotNull { key ->
                    val date = LocalDate.parse(key)
                    require(date <= today)
                    val note = (obj.get(key) as String).trim().take(MAX_NOTE_LENGTH)
                    if (note.isEmpty()) null else date to note
                }.toMap()
            }.orEmpty()
            Triple(balance, logs, notes)
        }.getOrNull() ?: return false
        store.replaceAll(parsed.first, parsed.second, parsed.third)
        return true
    }
}

private val summaryLabels = listOf(
    "Roza tracker", "Qaza roza balance", "Total fasts kept", "Ramadan fasts", "Nafl fasts",
    "Qaza fasts completed", "Missed fasts logged", "Current streak", "Longest streak"
)

private fun rozaSummary(balance: Int, stats: RozaStats, date: String, label: (String) -> String, number: (Int) -> String): String =
    listOf(
        "${label("Roza tracker")} - $date",
        "${label("Qaza roza balance")}: ${number(balance)}",
        "${label("Total fasts kept")}: ${number(stats.total)}",
        "${label("Ramadan fasts")}: ${number(stats.ramadan)}",
        "${label("Nafl fasts")}: ${number(stats.nafl)}",
        "${label("Qaza fasts completed")}: ${number(stats.qazaDone)}",
        "${label("Missed fasts logged")}: ${number(stats.missed)}",
        "${label("Current streak")}: ${number(stats.currentStreak)}",
        "${label("Longest streak")}: ${number(stats.longestStreak)}"
    ).joinToString("\n")

internal fun rozaSummaryText(balance: Int, stats: RozaStats, today: LocalDate): String =
    rozaSummary(balance, stats, today.toString(), { it }, { it.toString() })

@Composable
internal fun RozaActionsMenu(store: RozaTrackerStore, today: LocalDate, onChanged: () -> Unit, onMessage: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locale = LocalAppLocale.current
    val numbers = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val dateFormat = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    val labels = summaryLabels.associateWith { localizedUiText(it) }
    val shareTitle = localizedUiText("Share summary")
    val backupSaved = localizedUiText("Backup saved")
    val backupFailed = localizedUiText("Could not save the backup")
    val readFailed = localizedUiText("Could not read the backup file")
    val restored = localizedUiText("Backup restored")
    val invalidBackup = localizedUiText("This file is not a valid Roza tracker backup")
    val rangeSaved = localizedUiText("Fast log saved")
    val rangeFailed = localizedUiText("Could not mark these days. Check your Qaza balance and try again.")
    val balanceUpdated = localizedUiText("Qaza balance updated")

    var expanded by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    var showRange by rememberSaveable { mutableStateOf(false) }
    var rangeFrom by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    var rangeTo by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    var rangeMark by rememberSaveable { mutableStateOf(RozaMark.RAMADAN.name) }
    var showCorrect by rememberSaveable { mutableStateOf(false) }
    var count by rememberSaveable { mutableStateOf("") }

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    val json = RozaBackup.export(store)
                    (context.contentResolver.openOutputStream(uri, "wt") ?: error("no stream")).use { it.write(json.toByteArray()) }
                }.isSuccess
            }
            onMessage(if (ok) backupSaved else backupFailed)
        }
    }
    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val out = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (out.size() <= MAX_BACKUP_BYTES) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            out.write(buffer, 0, read)
                        }
                        if (out.size() > MAX_BACKUP_BYTES) null else out.toString("UTF-8")
                    }
                }.getOrNull()
            }
            if (text == null) onMessage(readFailed) else pendingRestore = text
        }
    }

    Box(modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.testTag("roza_menu")) {
            Icon(Icons.Default.MoreVert, localizedUiText("More options"))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { LocalizedText("Share summary") }, onClick = {
                expanded = false
                val text = rozaSummary(store.balance, RozaLogic.stats(store.logs(), today), today.format(dateFormat),
                    { labels[it] ?: it }, { numbers.format(it) })
                runCatching {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), shareTitle))
                }
            })
            DropdownMenuItem(text = { LocalizedText("Save backup") }, onClick = {
                expanded = false
                runCatching { saveLauncher.launch("roza-backup-$today.json") }.onFailure { onMessage(backupFailed) }
            })
            DropdownMenuItem(text = { LocalizedText("Restore backup") }, onClick = {
                expanded = false
                runCatching { openLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }.onFailure { onMessage(readFailed) }
            })
            DropdownMenuItem(text = { LocalizedText("Mark a date range") }, onClick = {
                expanded = false; rangeFrom = today.toEpochDay(); rangeTo = today.toEpochDay(); showRange = true
            })
            DropdownMenuItem(text = { LocalizedText("Correct Qaza balance") }, enabled = store.balance > 0, onClick = {
                expanded = false; count = ""; showCorrect = true
            })
        }
    }

    pendingRestore?.let { json ->
        AlertDialog(onDismissRequest = { pendingRestore = null }, title = { LocalizedText("Restore backup") },
            text = { LocalizedText("This replaces your current fast log, notes and Qaza balance with the backup.") },
            confirmButton = { TextButton(onClick = {
                pendingRestore = null
                if (RozaBackup.import(store, json, today)) { onChanged(); onMessage(restored) } else onMessage(invalidBackup)
            }) { LocalizedText("Restore") } },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { LocalizedText("Cancel") } })
    }

    if (showRange) {
        val from = LocalDate.ofEpochDay(rangeFrom).coerceAtMost(today)
        val to = LocalDate.ofEpochDay(rangeTo).coerceAtMost(today)
        val days = to.toEpochDay() - from.toEpochDay() + 1
        val valid = days in 1..MAX_RANGE_DAYS
        fun pick(current: LocalDate, onPicked: (LocalDate) -> Unit) {
            DatePickerDialog(context, { _, year, monthIndex, day -> onPicked(LocalDate.of(year, monthIndex + 1, day)) },
                current.year, current.monthValue - 1, current.dayOfMonth).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }
        AlertDialog(onDismissRequest = { showRange = false }, title = { LocalizedText("Mark a date range") },
            text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        LocalizedText("From", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { pick(from) { rangeFrom = it.toEpochDay() } }, modifier = Modifier.fillMaxWidth().testTag("roza_range_from")) { Text(from.format(dateFormat)) }
                    }
                    Column(Modifier.weight(1f)) {
                        LocalizedText("To", style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(onClick = { pick(to) { rangeTo = it.toEpochDay() } }, modifier = Modifier.fillMaxWidth().testTag("roza_range_to")) { Text(to.format(dateFormat)) }
                    }
                }
                when {
                    days < 1 -> LocalizedText("The end date must not be before the start date.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    days > MAX_RANGE_DAYS -> LocalizedText("You can mark up to 60 days at a time.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    else -> Text("${localizedUiText("Days selected")}: ${numbers.format(days)}", style = MaterialTheme.typography.bodySmall)
                }
                RozaMark.entries.forEach { option ->
                    val active = rangeMark == option.name
                    FilterChip(selected = active, onClick = { rangeMark = option.name }, label = { LocalizedText(option.label, modifier = Modifier.padding(vertical = 8.dp)) },
                        leadingIcon = if (active) { { Icon(Icons.Default.CheckCircle, null) } } else null,
                        modifier = Modifier.fillMaxWidth().testTag("roza_range_${option.name.lowercase()}"), shape = RoundedCornerShape(16.dp))
                }
                LocalizedText("Days already logged in this range will be replaced.", style = MaterialTheme.typography.bodySmall)
            } },
            confirmButton = { TextButton(enabled = valid, onClick = {
                showRange = false
                val mark = RozaMark.entries.firstOrNull { it.name == rangeMark } ?: RozaMark.RAMADAN
                val dates = (0 until days).map { from.plusDays(it) }
                if (store.setMarks(dates, mark, today)) { onChanged(); onMessage(rangeSaved) } else onMessage(rangeFailed)
            }) { LocalizedText("Apply") } },
            dismissButton = { TextButton(onClick = { showRange = false }) { LocalizedText("Cancel") } })
    }

    if (showCorrect) {
        val balance = store.balance
        val value = count.toIntOrNull() ?: 0
        AlertDialog(onDismissRequest = { showCorrect = false }, title = { LocalizedText("Correct Qaza balance") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LocalizedText("Use this only to fix a count you entered by mistake. To record a Qaza fast you kept, log it on its day instead.", style = MaterialTheme.typography.bodyMedium)
                Text("${localizedUiText("Qaza roza balance")}: ${numbers.format(balance)}", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = count, onValueChange = { input -> if (input.length <= 9 && input.all { it in '0'..'9' }) count = input },
                    label = { LocalizedText("Number of fasts to remove") }, singleLine = true,
                    isError = count.isNotEmpty() && (value <= 0 || value > balance),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.testTag("roza_correct_count"))
            } },
            confirmButton = { TextButton(enabled = value in 1..balance, onClick = {
                showCorrect = false
                if (store.reduceBalance(value)) { onChanged(); onMessage(balanceUpdated) }
            }) { LocalizedText("Remove") } },
            dismissButton = { TextButton(onClick = { showCorrect = false }) { LocalizedText("Cancel") } })
    }
}
