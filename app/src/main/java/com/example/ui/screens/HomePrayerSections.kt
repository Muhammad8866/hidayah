@file:OptIn(ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.PrayerType
import com.example.prayer.AstronomicalPrayerCalculator
import com.example.prayer.OtherIslamicTimes
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HeroGradientBottom
import com.example.ui.theme.HeroGradientMiddle
import com.example.ui.theme.HeroGradientTop
import com.example.viewmodel.LocationIssue
import com.example.viewmodel.MainUiState
import java.util.Calendar
import java.util.TimeZone

private fun AstronomicalPrayerCalculator.RawPrayerTimes.hoursOf(type: PrayerType): Double = when (type) {
    PrayerType.FAJR -> fajrHours
    PrayerType.SUNRISE -> sunriseHours
    PrayerType.DHUHR -> dhuhrHours
    PrayerType.ASR -> asrHours
    PrayerType.MAGHRIB -> maghribHours
    PrayerType.ISHA -> ishaHours
}

/** True once Isha has started, when the next Fajr is tomorrow's. */
private fun isNextFajrTomorrow(uiState: MainUiState): Boolean {
    val raw = uiState.rawPrayerTimes ?: return false
    if (uiState.nextPrayer != PrayerType.FAJR) return false
    val now = Calendar.getInstance(TimeZone.getTimeZone(uiState.selectedCity.timeZoneId))
    return now.timeInMillis >= AstronomicalPrayerCalculator.formatHoursToTimeInMillis(now, raw.ishaHours)
}

private fun countdownText(minutes: Long): String =
    if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "$minutes min"

// region Hero

@Composable
internal fun NextPrayerCard(uiState: MainUiState, onOpenPrayerDetails: () -> Unit) {
    val rtlLanguage = isRtlAppLanguage(LocalAppLanguage.current)
    val hasTimes = uiState.rawPrayerTimes != null && uiState.tomorrowPrayerTimes != null
    val isTomorrow = hasTimes && uiState.currentPrayer == PrayerType.ISHA && isNextFajrTomorrow(uiState)
    val prayerName = if (rtlLanguage) uiState.nextPrayer.arabicName else uiState.nextPrayer.displayName
    val time = uiState.nextPrayerTimeFormatted.ifBlank { "--:--" }
    val countdown = countdownText(uiState.minutesUntilNextPrayer)
    val onHero = Color.White
    val summary = listOf(
        localizedUiText("Next prayer"), localizedUiText(prayerName), time,
        localizedUiText("Starts in") + " " + countdown
    ).joinToString(", ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("hero_prayer_card"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(HeroGradientTop, HeroGradientMiddle, HeroGradientBottom)))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LocalizedText("Next prayer", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold, color = GoldAccent,
                    modifier = Modifier.weight(1f).semantics { heading() })
                if (isTomorrow) HeroPill("Tomorrow", onHero)
                IconButton(onClick = onOpenPrayerDetails,
                    modifier = Modifier.testTag("prayer_transparency_btn")) {
                    Icon(Icons.Outlined.Info, localizedUiText("Prayer Calculation Transparency"),
                        tint = onHero.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }
            }

            if (!hasTimes) {
                LocalizedText(
                    text = "Calculating prayer times…",
                    style = MaterialTheme.typography.titleLarge,
                    color = onHero,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50)),
                    color = GoldAccent,
                    trackColor = onHero.copy(alpha = 0.16f)
                )
                return@Column
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth()
                    .semantics(mergeDescendants = true) { contentDescription = summary },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.padding(end = 16.dp)) {
                    LocalizedText(prayerName, style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = onHero)
                    if (!rtlLanguage) Text(uiState.nextPrayer.arabicName,
                        style = MaterialTheme.typography.titleSmall, fontFamily = ArabicFontFamily,
                        color = onHero.copy(alpha = 0.7f))
                }
                Text(ltr(time), style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = onHero,
                    modifier = Modifier.align(Alignment.CenterVertically), maxLines = 1)
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Timer, contentDescription = null, tint = GoldAccent,
                    modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText("Starts in", style = MaterialTheme.typography.bodySmall,
                    color = onHero.copy(alpha = 0.8f))
                Spacer(Modifier.width(6.dp))
                Text(ltr(countdown), style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = onHero)
            }

        }
    }
}

@Composable
private fun HeroPill(text: String, onHero: Color) {
    LocalizedText(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = onHero,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(onHero.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

/** The one place Home explains missing prayer times, with a way to recover. */
@Composable
internal fun PrayerTimesErrorCard(
    locationIssue: LocationIssue?,
    needsLocation: Boolean = false,
    onRetry: () -> Unit,
    onFixLocation: () -> Unit,
    onOpenDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("prayer_times_error_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.width(10.dp))
                LocalizedText(
                    text = "Prayer times unavailable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.semantics { heading() }
                )
            }
            LocalizedText(
                text = if (locationIssue != null || needsLocation) "Choose a city or allow location to see prayer times for where you are."
                else "We couldn't work out prayer times for this location. Please try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Button(
                    onClick = if (locationIssue != null || needsLocation) onFixLocation else onRetry,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("prayer_times_retry"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = if (locationIssue != null || needsLocation) Icons.Default.LocationOn else Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    LocalizedText(if (locationIssue != null || needsLocation) "Choose city" else "Try again")
                }
                if (locationIssue != null && !needsLocation) {
                    TextButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
                        LocalizedText("Try again", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                TextButton(onClick = onOpenDetails, modifier = Modifier.heightIn(min = 48.dp)) {
                    LocalizedText("Calculation details", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

// endregion

// region Schedule

private val FARD_PRAYERS = listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA)

@Composable
internal fun PrayerScheduleCard(uiState: MainUiState, onOpenPrayerDetails: () -> Unit) {
    val raw = uiState.rawPrayerTimes ?: return
    val locale = LocalAppLocale.current
    var showAll by rememberSaveable { mutableStateOf(false) }
    val now = Calendar.getInstance(TimeZone.getTimeZone(uiState.selectedCity.timeZoneId))
    val tomorrowFajr = isNextFajrTomorrow(uiState)
    val rows = if (showAll) PrayerType.entries else FARD_PRAYERS

    HomeCard(tag = "prayer_schedule_card") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocalizedText(
                text = "Today's Prayer Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            TextButton(
                onClick = { showAll = !showAll },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("prayer_schedule_view_all")
            ) {
                LocalizedText(if (showAll) "Show less" else "View all", fontWeight = FontWeight.SemiBold)
            }
        }

        rows.forEach { type ->
            // After Isha the highlighted Fajr is tomorrow's, so show tomorrow's time on that row.
            val showsTomorrow = type == PrayerType.FAJR && tomorrowFajr
            val hours = if (showsTomorrow) uiState.tomorrowPrayerTimes?.fajrHours ?: raw.fajrHours else raw.hoursOf(type)
            PrayerRow(
                type = type,
                time = AstronomicalPrayerCalculator.formatHoursToAmPm(hours, locale),
                isNext = type != PrayerType.SUNRISE && type == uiState.nextPrayer,
                isPassed = !showsTomorrow &&
                    now.timeInMillis >= AstronomicalPrayerCalculator.formatHoursToTimeInMillis(now, raw.hoursOf(type)),
                isTomorrow = showsTomorrow
            )
        }

        AnimatedVisibility(visible = showAll) {
            TextButton(
                onClick = onOpenPrayerDetails,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                LocalizedText("Calculation details")
            }
        }
    }
}

@Composable
private fun PrayerRow(type: PrayerType, time: String, isNext: Boolean, isPassed: Boolean, isTomorrow: Boolean) {
    val rtlLanguage = isRtlAppLanguage(LocalAppLanguage.current)
    val isSunrise = type == PrayerType.SUNRISE
    val contentColor = when {
        isNext -> MaterialTheme.colorScheme.onPrimaryContainer
        isPassed || isSunrise -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(if (isNext) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .heightIn(min = 44.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics(mergeDescendants = true) {}
            .testTag("prayer_row_${type.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                isSunrise -> Icons.Outlined.WbSunny
                isNext -> Icons.Default.NotificationsActive
                else -> Icons.Outlined.Schedule
            },
            contentDescription = if (isNext) localizedUiText("Next") else null,
            tint = when {
                isNext -> MaterialTheme.colorScheme.primary
                isPassed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            },
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LocalizedText(
                    text = if (rtlLanguage) type.arabicName else type.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                if (!rtlLanguage) Text(
                    text = type.arabicName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = ArabicFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isPassed) 0.6f else 0.9f),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            if (isTomorrow) LocalizedText(
                text = "Tomorrow",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = ltr(time),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

// endregion

// region Other Islamic times

private data class ExtraTime(val label: String, val hours: Double?, val hint: String)

@Composable
internal fun OtherIslamicTimesCard(uiState: MainUiState) {
    val raw = uiState.rawPrayerTimes ?: return
    val locale = LocalAppLocale.current
    val city = uiState.selectedCity
    var expanded by rememberSaveable { mutableStateOf(false) }
    val times = remember(raw, city) {
        val today = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId))
        val solarNoon = AstronomicalPrayerCalculator.calculateSolarParameters(today, city.latitude, city.longitude).solarNoonHours
        runCatching { OtherIslamicTimes.from(raw, solarNoon) }.getOrNull()
    } ?: return
    val rows = listOf(
        ExtraTime("Sehri ends", times.sehriStopHours, "Finish eating before this time"),
        ExtraTime("Ishraq (approx.)", times.ishraqGuideHours, "About 20 minutes after sunrise"),
        ExtraTime("Chasht (approx.)", times.chashtGuideHours, "Mid-morning, before Zawal"),
        ExtraTime("Zawal (solar noon)", times.solarNoonHours, "Avoid praying at this time"),
        ExtraTime("Iftar", times.iftarHours, "Open your fast at Maghrib")
    )
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "otherTimesChevron")
    val stateLabel = localizedUiText(if (expanded) "Show less" else "Show more")

    HomeCard(tag = "other_times_card") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable(role = Role.Button, onClick = { expanded = !expanded })
                .heightIn(min = 56.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .semantics { stateDescription = stateLabel }
                .testTag("other_times_toggle"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    text = "Other Islamic Times",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() }
                )
                if (!expanded) LocalizedText(
                    text = "Sehri, Ishraq, Chasht, Zawal and Iftar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(rotation)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .semantics(mergeDescendants = true) {},
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            LocalizedText(
                                text = row.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            LocalizedText(
                                text = row.hint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = row.hours?.let { ltr(AstronomicalPrayerCalculator.formatHoursToAmPm(it, locale)) } ?: "—",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                    }
                }
                LocalizedText(
                    text = "Ishraq is about 20 minutes after sunrise; Chasht is a mid-morning guide. Check local guidance for prayer windows.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// endregion

/** Outlined surface card shared by the Home sections below the hero. */
@Composable
internal fun HomeCard(tag: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp), content = content)
    }
}
