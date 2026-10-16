package com.example.ui.screens

import com.example.ui.components.ScreenHeader
import com.example.ui.components.AppCard
import com.example.ui.components.HeroSurface
import com.example.ui.components.IconBadge
import com.example.ui.components.Spacing
import com.example.ui.theme.GoldAccent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.Event
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
    com.example.core.analytics.AnalyticsScreen("islamic_calendar_screen")

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
    val currentSelectedDay = if (selectedDay == 0) todayHijri.day else selectedDay.coerceIn(1, daysInMonth)
    val selected = (month.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, currentSelectedDay - 1) }
    val selectedHijri = HijriCalendarHelper.getHijriDate(selected, region = region)
    val locale = com.example.ui.localization.LocalAppLocale.current
    val language = LocalAppLanguage.current
    // The grid starts on the first day of the week of the user's region (Sunday, Monday or Saturday).
    val firstDayOfWeek = remember(locale) { Calendar.getInstance(locale).firstDayOfWeek }
    val firstWeekday = (month.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek + 7) % 7
    val fridayColumn = (Calendar.FRIDAY - firstDayOfWeek + 7) % 7
    val weekdayLabels = remember(locale, firstDayOfWeek) {
        DateFormatSymbols(locale).shortWeekdays.let { names ->
            (0..6).map { names[(firstDayOfWeek - 1 + it) % 7 + 1] }
        }
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val timePattern = if (android.text.format.DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
    val monthFormatter = remember(locale, zone) { SimpleDateFormat("d MMM yyyy", locale).apply { timeZone = zone } }
    val dateFormatter = remember(locale, zone) { SimpleDateFormat("EEEE, d MMMM yyyy", locale).apply { timeZone = zone } }
    val timeFormatter = remember(locale, zone, timePattern) { SimpleDateFormat(timePattern, locale).apply { timeZone = zone } }
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("islamic_calendar_screen"),
        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            ScreenHeader("Islamic Calendar", onBack = onBack)
        }
        item {
            // Deep emerald hero in both themes, so white and gold always read.
            HeroSurface(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LocalizedText("Today • ${uiState.selectedCity.name}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = GoldAccent)
                    Text(formatHijriDate(todayHijri, language), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(dateFormatter.format(Date(now)), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                    if (uiState.hijriDateTonight != null) {
                        LocalizedText("After Maghrib: ${formatHijriDate(uiState.hijriDateTonight, language)}", color = GoldAccent, style = MaterialTheme.typography.bodySmall)
                    }
                    LocalizedText("Live local time ${timeFormatter.format(Date(now))}  •  ${region.label}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
        item {
            AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("islamic_calendar_screen", "previous_month", "tap"); monthOffset--; selectedDay = 1 }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = localizedUiText("Previous month"))
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${if (isRtlAppLanguage(language)) monthHijri.monthNameArabic else monthHijri.monthNameEnglish} $hijriYear ${if (isRtlAppLanguage(language)) "هـ" else "AH"}", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val lastDay = (month.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, daysInMonth - 1) }
                            Text("${monthFormatter.format(month.time)} – ${monthFormatter.format(lastDay.time)}", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                        }
                        IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("islamic_calendar_screen", "next_month", "tap"); monthOffset++; selectedDay = 1 }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = localizedUiText("Next month"))
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        weekdayLabels.forEachIndexed { index, label ->
                            Text(label, Modifier.weight(1f), textAlign = TextAlign.Center, maxLines = 1, style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (index == fridayColumn) FontWeight.Bold else FontWeight.Medium,
                                color = if (index == fridayColumn) primary else MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    val isFriday = weekday == fridayColumn
                                    Surface(
                                        selected = isSelected,
                                        onClick = { com.example.core.analytics.AppAnalytics.interaction("islamic_calendar_screen", "select_day", "tap"); selectedDay = day },
                                        modifier = Modifier.weight(1f).heightIn(min = 54.dp).padding(2.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = when {
                                            isSelected -> primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            isFriday -> primary.copy(alpha = 0.05f)
                                            else -> Color.Transparent
                                        },
                                        border = if (isToday && !isSelected) BorderStroke(1.5.dp, primary) else null
                                    ) {
                                        Column(Modifier.heightIn(min = 50.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                            Text("$day", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                            Text("${civil.get(Calendar.DAY_OF_MONTH)}", style = MaterialTheme.typography.labelSmall, color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                } else Spacer(Modifier.weight(1f).height(54.dp))
                            }
                        }
                    }
                }
            }
        }
        item {
            // Only offered once the user has moved away from today.
            AnimatedVisibility(visible = monthOffset != 0 || (selectedDay != 0 && selectedDay != todayHijri.day)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("islamic_calendar_screen", "go_to_today", "tap"); monthOffset = 0; selectedDay = 0 }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Default.Today, null)
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Go to today")
                }
            }
            }
        }
        item {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                outlined = false,
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Default.Event, accent = primary, size = 40.dp, iconSize = 22.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(formatHijriDate(selectedHijri, language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(dateFormatter.format(selected.time), style = MaterialTheme.typography.bodyMedium, color = primary)
                    }
                }
                Column(Modifier.fillMaxWidth().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    LocalizedText("Hijri month starts follow ${region.label}. Local moon sighting may change the date; corrections in Settings are reflected here.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
