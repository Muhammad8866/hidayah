package com.example.feature.communitymasjid.ui

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.model.CmPrayer
import com.example.feature.communitymasjid.model.CmPrayerTime
import com.example.feature.communitymasjid.model.CmTimings
import com.example.ui.localization.LocalizedText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formats a stored "HH:mm" value in the device's 12h/24h preference. */
@Composable
internal fun cmDisplayTime(hm: String?): String = cmFormatHm(hm, cmIs24Hour())

@Composable
internal fun cmIs24Hour(): Boolean = DateFormat.is24HourFormat(LocalContext.current)

internal fun cmFormatHm(hm: String?, is24h: Boolean): String {
    if (hm == null || !CmTimingRules.isValidHm(hm)) return "--:--"
    if (is24h) return hm
    val minutes = CmTimingRules.minutes(hm)
    val hour = minutes / 60
    val suffix = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour % 12 == 0) 12 else hour % 12
    return "%d:%02d %s".format(displayHour, minutes % 60, suffix)
}

internal fun cmFormatDate(epochMs: Long?): String =
    epochMs?.let { SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(it)) } ?: "—"

@Composable
internal fun CmStatusBadge(text: String, container: androidx.compose.ui.graphics.Color, content: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(50), color = container) {
        LocalizedText(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = content,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Prayer | Azan | Jamaat table. Values that differ from [compareTo] are highlighted. */
@Composable
internal fun CmTimingTable(timings: CmTimings, compareTo: CmTimings? = null) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            LocalizedText("Prayer", Modifier.weight(1.2f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LocalizedText("Azan", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LocalizedText("Jamaat", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        CmPrayer.entries.forEach { prayer ->
            val time = timings[prayer] ?: return@forEach
            val old = compareTo?.get(prayer)
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                LocalizedText(prayer.label, Modifier.weight(1.2f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (prayer.hasAzan) {
                    CmTimeCell(time.azan, changed = compareTo != null && old?.azan != time.azan, modifier = Modifier.weight(1f))
                } else {
                    Text("—", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                CmTimeCell(time.jamaat, changed = compareTo != null && old?.jamaat != time.jamaat, modifier = Modifier.weight(1f), bold = true)
            }
        }
    }
}

@Composable
private fun CmTimeCell(hm: String, changed: Boolean, modifier: Modifier, bold: Boolean = false) {
    Text(
        text = cmDisplayTime(hm),
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (bold || changed) FontWeight.Bold else FontWeight.Normal,
        color = if (changed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
    )
}

/** Editor for new timings; validation runs live so invalid timings can't be submitted. */
@Composable
internal fun CmSuggestDialog(
    initial: CmTimings?,
    isSubmitting: Boolean,
    validate: (CmTimings) -> List<String>,
    onSubmit: (CmTimings) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val azan = remember { mutableStateMapOf<CmPrayer, String>().apply { initial?.times?.forEach { (p, t) -> put(p, t.azan) } } }
    val jamaat = remember { mutableStateMapOf<CmPrayer, String>().apply { initial?.times?.forEach { (p, t) -> put(p, t.jamaat) } } }
    val included = remember { mutableStateMapOf<CmPrayer, Boolean>().apply { CmPrayer.optional.forEach { put(it, initial?.get(it) != null) } } }
    var attempted by remember { mutableStateOf(false) }

    fun build(): CmTimings? {
        val prayers = CmPrayer.daily + CmPrayer.optional.filter { included[it] == true }
        val map = prayers.associateWith { p ->
            val jamaatTime = jamaat[p] ?: return null
            // Prayers without an Azan store the Jamaat time in both fields.
            CmPrayerTime(if (p.hasAzan) azan[p] ?: return null else jamaatTime, jamaatTime)
        }
        return CmTimings(map)
    }

    val built = build()
    val problems = when {
        built == null -> listOf("Please set the time for every prayer shown")
        else -> validate(built)
    }

    fun pick(current: String?, onPicked: (String) -> Unit) {
        val start = current?.takeIf { CmTimingRules.isValidHm(it) }?.let { CmTimingRules.minutes(it) } ?: (12 * 60)
        TimePickerDialog(
            context,
            { _, hour, minute -> onPicked(CmTimingRules.format(hour * 60 + minute)) },
            start / 60, start % 60, DateFormat.is24HourFormat(context)
        ).show()
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { LocalizedText("Update Timings", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LocalizedText(
                    "New timings are shown to everyone right away. If ${CmTimingRules.VOTE_THRESHOLD}+ people at the masjid vote them wrong, the previous timings come back.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    LocalizedText("Azan", Modifier.width(92.dp), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Jamaat", Modifier.width(92.dp), style = MaterialTheme.typography.labelMedium)
                }
                CmPrayer.entries.forEach { prayer ->
                    if (prayer.isOptional) {
                        if (prayer == CmPrayer.optional.first()) {
                            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LocalizedText("Include ${prayer.label}", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = included[prayer] == true, onCheckedChange = { included[prayer] = it })
                        }
                        if (included[prayer] != true) return@forEach
                    }
                    Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                        LocalizedText(prayer.label, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        if (prayer.hasAzan) {
                            OutlinedButton(onClick = { pick(azan[prayer]) { azan[prayer] = it } }, modifier = Modifier.width(92.dp)) {
                                Text(cmDisplayTime(azan[prayer]), maxLines = 1, style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            Text("—", Modifier.width(92.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(6.dp))
                        OutlinedButton(onClick = { pick(jamaat[prayer] ?: azan[prayer]) { jamaat[prayer] = it } }, modifier = Modifier.width(92.dp)) {
                            Text(cmDisplayTime(jamaat[prayer]), maxLines = 1, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                if (attempted || built != null) {
                    problems.forEach {
                        Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = {
                    attempted = true
                    if (built != null && problems.isEmpty()) onSubmit(built)
                }
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.width(18.dp).heightIn(max = 18.dp), strokeWidth = 2.dp)
                else LocalizedText("Submit")
            }
        },
        dismissButton = { TextButton(enabled = !isSubmitting, onClick = onDismiss) { LocalizedText("Cancel") } }
    )
}
