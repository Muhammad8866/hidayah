package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.prayer.HijriCalendarHelper
import com.example.prayer.HijriRegion
import com.example.ui.components.BackIconButton
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.formatHijriDate
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.localization.localizedUiText
import com.example.viewmodel.MainUiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@Composable
fun IslamicCalendarScreen(uiState: MainUiState, onBack: () -> Unit) {
    val zone = remember(uiState.selectedCity.timeZoneId) { TimeZone.getTimeZone(uiState.selectedCity.timeZoneId) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(zone) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000)
        }
    }
    val today = remember(now, zone) { Calendar.getInstance(zone).apply { timeInMillis = now } }
    var monthOffset by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableIntStateOf(0) }
    val region = HijriRegion.forTimeZone(zone.id)
    val todayHijri = HijriCalendarHelper.getHijriDate(today, region = region)
    val monthIndex = todayHijri.year * 12 + todayHijri.month - 1 + monthOffset
    val hijriYear = Math.floorDiv(monthIndex, 12)
    val hijriMonth = Math.floorMod(monthIndex, 12) + 1
    val monthHijri = HijriCalendarHelper.create(1, hijriMonth, hijriYear)
    val startJdn = HijriCalendarHelper.toJulianDay(hijriYear, hijriMonth, 1, region)
    val nextIndex = monthIndex + 1
    val nextJdn = HijriCalendarHelper.toJulianDay(Math.floorDiv(nextIndex, 12), Math.floorMod(nextIndex, 12) + 1, 1, region)
    val daysInMonth = (nextJdn - startJdn).toInt().coerceIn(29, 30)
    val (startYear, startMonth, startDay) = HijriCalendarHelper.julianDayToGregorian(startJdn)
    val month = Calendar.getInstance(zone).apply { set(startYear, startMonth - 1, startDay, 12, 0, 0) }
    val firstWeekday = (month.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val currentSelectedDay = if (selectedDay == 0) todayHijri.day else selectedDay.coerceIn(1, daysInMonth)
    val selected = (month.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, currentSelectedDay - 1) }
    val selectedHijri = HijriCalendarHelper.getHijriDate(selected, region = region)
    val locale = com.example.ui.localization.LocalAppLocale.current
    val language = LocalAppLanguage.current
    val weekdayLabels = remember(locale) {
        DateFormatSymbols(locale).shortWeekdays.let { names ->
            listOf(2, 3, 4, 5, 6, 7, 1).map { names[it] }
        }
    }
    val monthFormatter = remember(locale, zone) { SimpleDateFormat("d MMM yyyy", locale).apply { timeZone = zone } }
    val dateFormatter = remember(locale, zone) { SimpleDateFormat("EEEE, d MMMM yyyy", locale).apply { timeZone = zone } }
    val timeFormatter = remember(locale, zone) { SimpleDateFormat("h:mm a", locale).apply { timeZone = zone } }
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("islamic_calendar_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackIconButton(onBack)
                LocalizedText("Islamic Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    LocalizedText("Today • ${uiState.selectedCity.name}", style = MaterialTheme.typography.labelMedium, color = primary)
                    Text(formatHijriDate(todayHijri, language), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(dateFormatter.format(Date(now)), style = MaterialTheme.typography.bodyMedium)
                    if (uiState.hijriDateTonight != null) {
                        LocalizedText("After Maghrib: ${formatHijriDate(uiState.hijriDateTonight, language)}", color = primary, style = MaterialTheme.typography.bodySmall)
                    }
                    LocalizedText("Live local time ${timeFormatter.format(Date(now))}  •  ${region.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { monthOffset--; selectedDay = 1 }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = localizedUiText("Previous month"))
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${if (isRtlAppLanguage(language)) monthHijri.monthNameArabic else monthHijri.monthNameEnglish} $hijriYear ${if (isRtlAppLanguage(language)) "هـ" else "AH"}", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val lastDay = (month.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, daysInMonth - 1) }
                            Text("${monthFormatter.format(month.time)} – ${monthFormatter.format(lastDay.time)}", style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { monthOffset++; selectedDay = 1 }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = localizedUiText("Next month"))
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        weekdayLabels.forEachIndexed { index, label ->
                            Text(label, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = if (index == 4) primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    repeat((firstWeekday + daysInMonth + 6) / 7) { week ->
                        Row(Modifier.fillMaxWidth()) {
                            repeat(7) { weekday ->
                                val day = week * 7 + weekday - firstWeekday + 1
                                if (day in 1..daysInMonth) {
                                    val civil = (month.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, day - 1) }
                                    val isToday = monthOffset == 0 && day == todayHijri.day
                                    val isSelected = day == currentSelectedDay
                                    Surface(
                                        modifier = Modifier.weight(1f).height(62.dp).padding(2.dp).clickable(role = Role.Button) { selectedDay = day },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) primary else if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        border = if (isToday && !isSelected) BorderStroke(1.dp, primary) else null
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                            Text("$day", fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                            Text("${civil.get(Calendar.DAY_OF_MONTH)}", style = MaterialTheme.typography.labelSmall, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                } else Spacer(Modifier.weight(1f).height(62.dp))
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { monthOffset = 0; selectedDay = 0 }) {
                    Icon(Icons.Default.Today, null)
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Go to today")
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(formatHijriDate(selectedHijri, language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(dateFormatter.format(selected.time), style = MaterialTheme.typography.bodyLarge, color = primary)
                    LocalizedText("Hijri month starts follow ${region.label}. Local moon sighting may change the date; corrections in Settings are reflected here.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
