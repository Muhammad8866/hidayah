package com.example.feature.communitymasjid.ui

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.example.ui.localization.localizedUiText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.model.CmPrayer
import com.example.feature.communitymasjid.model.CmPrayerTime
import com.example.feature.communitymasjid.model.CmTimings
import com.example.ui.localization.LocalizedText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

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
    // Latin digits, matching the 24-hour branch above.
    return "%d:%02d %s".format(Locale.ROOT, displayHour, minutes % 60, suffix)
}

/** Date and time in the order the user's region writes them, honouring the device's 12h/24h setting. */
internal fun cmFormatDate(epochMs: Long?, is24h: Boolean = false, locale: Locale = Locale.getDefault()): String {
    epochMs ?: return "—"
    val pattern = DateFormat.getBestDateTimePattern(locale, if (is24h) "d MMM yyyy Hm" else "d MMM yyyy hm")
    return SimpleDateFormat(pattern, locale).format(Date(epochMs))
}

private val cmImperialRegions = setOf("US", "GB", "LR", "MM")

/** Metres/kilometres, or feet/miles in the few regions that use them on the road. */
internal fun cmFormatDistance(metres: Double, locale: Locale): String {
    if (locale.country.uppercase(Locale.ROOT) in cmImperialRegions) {
        val miles = metres / 1609.344
        return if (miles < 0.1) "%d ft".format(locale, (metres * 3.28084).roundToInt())
        else "%.1f mi".format(locale, miles)
    }
    return if (metres < 1000) "%d m".format(locale, metres.roundToInt()) else "%.1f km".format(locale, metres / 1000)
}

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

/**
 * Prayer | Azan | Jamaat table. Values that differ from [compareTo] are highlighted, and the
 * [highlight] row (the next Jamaat) is tinted so it can be found at a glance.
 */
@Composable
internal fun CmTimingTable(timings: CmTimings, compareTo: CmTimings? = null, highlight: CmPrayer? = null) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)) {
            LocalizedText("Prayer", Modifier.weight(1.2f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LocalizedText("Azan", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LocalizedText("Jamaat", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(Modifier.padding(bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
        val nextLabel = localizedUiText("Next prayer")
        CmPrayer.entries.forEach { prayer ->
            val time = timings[prayer] ?: return@forEach
            val old = compareTo?.get(prayer)
            val isNext = prayer == highlight
            val onRow = if (isNext) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(if (isNext) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .then(if (isNext) Modifier.semantics { stateDescription = nextLabel } else Modifier)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                    if (isNext) {
                        Box(Modifier.padding(end = 6.dp).size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    }
                    LocalizedText(prayer.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = onRow, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (prayer.hasAzan) {
                    CmTimeCell(time.azan, changed = compareTo != null && old?.azan != time.azan, modifier = Modifier.weight(1f), color = onRow)
                } else {
                    Text("—", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                CmTimeCell(time.jamaat, changed = compareTo != null && old?.jamaat != time.jamaat, modifier = Modifier.weight(1f), bold = true, color = onRow)
            }
        }
    }
}

@Composable
private fun CmTimeCell(hm: String, changed: Boolean, modifier: Modifier, bold: Boolean = false, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text(
        text = cmDisplayTime(hm),
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (bold || changed) FontWeight.Bold else FontWeight.Normal,
        color = if (changed) MaterialTheme.colorScheme.tertiary else color,
        maxLines = 1
    )
}

/** Time chooser button: tonal once a time is set, outlined while it is still "--:--". */
@Composable
private fun CmTimeButton(hm: String?, onClick: () -> Unit) {
    val content: @Composable RowScope.() -> Unit = {
        Text(cmDisplayTime(hm), maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
    val modifier = Modifier.width(92.dp).heightIn(min = 48.dp)
    val padding = PaddingValues(horizontal = 4.dp)
    if (hm != null && CmTimingRules.isValidHm(hm)) FilledTonalButton(onClick = onClick, modifier = modifier, contentPadding = padding, content = content)
    else OutlinedButton(onClick = onClick, modifier = modifier, contentPadding = padding, content = content)
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
        onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("cm_timing_components", "dismiss_timings_editor", "dismiss"); if (!isSubmitting) onDismiss() },
        icon = { Icon(Icons.Filled.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { LocalizedText("Update Timings", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 1.dp).size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    LocalizedText(
                        "New timings are shown to everyone right away. If ${CmTimingRules.VOTE_THRESHOLD}+ people at the masjid vote them wrong, the previous timings come back.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    LocalizedText("Azan", Modifier.width(92.dp), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Jamaat", Modifier.width(92.dp), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                CmPrayer.entries.forEach { prayer ->
                    if (prayer.isOptional) {
                        if (prayer == CmPrayer.optional.first()) {
                            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .toggleable(value = included[prayer] == true, role = Role.Switch, onValueChange = { included[prayer] = it }),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LocalizedText("Include ${prayer.label}", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(8.dp))
                            Switch(checked = included[prayer] == true, onCheckedChange = null)
                        }
                        if (included[prayer] != true) return@forEach
                    }
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                        LocalizedText(prayer.label, Modifier.weight(1f).padding(end = 6.dp), fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (prayer.hasAzan) {
                            CmTimeButton(azan[prayer]) { com.example.core.analytics.AppAnalytics.interaction("cm_timing_components", "pick_azan_time", "tap"); pick(azan[prayer]) { azan[prayer] = it } }
                        } else {
                            Text("—", Modifier.width(92.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.width(6.dp))
                        CmTimeButton(jamaat[prayer]) { com.example.core.analytics.AppAnalytics.interaction("cm_timing_components", "pick_jamaat_time", "tap"); pick(jamaat[prayer] ?: azan[prayer]) { jamaat[prayer] = it } }
                    }
                }
                // Problems sit in one tinted block so they read as "fix these before submitting".
                AnimatedVisibility(visible = (attempted || built != null) && problems.isNotEmpty()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        problems.forEach {
                            LocalizedText("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = { com.example.core.analytics.AppAnalytics.interaction("cm_timing_components", "submit_timings", "tap");
                    attempted = true
                    if (built != null && problems.isEmpty()) onSubmit(built)
                }
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.width(18.dp).heightIn(max = 18.dp), strokeWidth = 2.dp)
                else LocalizedText("Submit")
            }
        },
        dismissButton = { TextButton(enabled = !isSubmitting, onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "cancel_timings_editor", onDismiss)) { LocalizedText("Cancel") } }
    )
}
