@file:OptIn(ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import com.example.ui.components.AppCard
import com.example.ui.components.HeroSurface
import com.example.ui.components.IconBadge
import com.example.ui.components.Spacing
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
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

// region Hero

@Composable
internal fun NextPrayerCard(
    uiState: MainUiState,
    onOpenPrayerDetails: () -> Unit,
    onBoundaryReached: () -> Unit = {}
) {
    val language = LocalAppLanguage.current
    val locale = LocalAppLocale.current
    val rtlLanguage = isRtlAppLanguage(language)
    val hasTimes = uiState.rawPrayerTimes != null && uiState.tomorrowPrayerTimes != null
    // A one-second clock local to the hero: only this card recomposes every tick.
    val now by rememberHomeNow(1_000L)
    val minuteKey = now / 60_000L
    val target = remember(uiState.rawPrayerTimes, uiState.tomorrowPrayerTimes, uiState.nextPrayer, uiState.selectedCity, minuteKey) {
        nextPrayerTargetMillis(uiState, now)
    }
    val windowStart = remember(uiState.rawPrayerTimes, uiState.currentPrayer, uiState.selectedCity, minuteKey) {
        currentPrayerStartMillis(uiState, now)
    }
    val remaining = target?.let { it - now } ?: (uiState.minutesUntilNextPrayer * 60_000L)
    val reached = target != null && remaining <= 0L
    // The moment a prayer begins, ask the ViewModel to advance instead of waiting for its next tick.
    LaunchedEffect(reached, target) { if (reached) onBoundaryReached() }

    val isTomorrow = hasTimes && remember(target, uiState.nextPrayer) { isNextFajrTomorrow(uiState, now) }
    val prayerName = if (rtlLanguage) uiState.nextPrayer.arabicName else uiState.nextPrayer.displayName
    val time = uiState.nextPrayerTimeFormatted.ifBlank { "--:--" }
    val countdown = formatLiveCountdown(remaining, language)
    val spoken = spokenCountdown(remaining, language)
    val fraction = if (target != null && windowStart != null && target > windowStart) {
        ((now - windowStart).toFloat() / (target - windowStart)).coerceIn(0f, 1f)
    } else null
    val urgent = remaining in 1..15 * 60_000L
    val onHero = Color.White
    val summary = localizeUiTemplate(
        "Next prayer {prayer} at {time}, starts in {countdown}", language,
        mapOf("prayer" to localizeUiText(prayerName, language), "time" to time, "countdown" to spoken)
    )
    val currentLabel = if (rtlLanguage) uiState.currentPrayer.arabicName else uiState.currentPrayer.displayName
    val currentTime = uiState.rawPrayerTimes?.let {
        AstronomicalPrayerCalculator.formatHoursToAmPm(it.homeHoursOf(uiState.currentPrayer), locale)
    }.orEmpty()

    HeroSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen)
            .shadow(10.dp, MaterialTheme.shapes.extraLarge, ambientColor = HeroGradientBottom, spotColor = HeroGradientBottom)
            .testTag("hero_prayer_card"),
        onClick = com.example.core.analytics.AppAnalytics.trackAction("home_prayer_sections", "open_prayer_details_hero", onOpenPrayerDetails),
        contentPadding = PaddingValues(start = Spacing.xl, top = Spacing.sm, end = Spacing.sm, bottom = 18.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(end = Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // A small gold dot marks the live card, like a "now" indicator.
                Box(Modifier.size(8.dp).clip(CircleShape).background(GoldAccent))
                Spacer(Modifier.width(Spacing.sm))
                LocalizedText("Next prayer", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold, color = GoldAccent,
                    modifier = Modifier.weight(1f).semantics { heading() })
                if (isTomorrow) HeroPill("Tomorrow", onHero)
                IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("home_prayer_sections", "open_prayer_details_next_card", onOpenPrayerDetails),
                    modifier = Modifier.offset(x = Spacing.md).size(48.dp).testTag("prayer_transparency_btn")) {
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
                    LocalizedText(prayerName, style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold, color = onHero)
                    if (!rtlLanguage) Text(uiState.nextPrayer.arabicName,
                        style = MaterialTheme.typography.titleSmall, fontFamily = ArabicFontFamily,
                        color = onHero.copy(alpha = 0.7f))
                }
                Text(ltr(time), style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold, color = onHero,
                    modifier = Modifier.align(Alignment.CenterVertically), maxLines = 1)
            }

            Spacer(Modifier.height(14.dp))
            // How far the current prayer window has run, on the same live clock as the countdown.
            fraction?.let { f ->
                val animatedFraction by animateFloatAsState(f, label = "prayerWindow")
                LinearProgressIndicator(
                    progress = { animatedFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .clearAndSetSemantics {},
                    color = GoldAccent,
                    trackColor = onHero.copy(alpha = 0.18f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
                Spacer(Modifier.height(10.dp))
            }
            // FlowRow drops the countdown under the current prayer when translations run long.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 12.dp)
                        .semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LocalizedText(
                        text = "Now",
                        style = MaterialTheme.typography.labelSmall, color = GoldAccent,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    LocalizedText(
                        text = currentLabel,
                        style = MaterialTheme.typography.bodySmall, color = onHero.copy(alpha = 0.85f))
                    if (currentTime.isNotEmpty()) Text("  •  " + ltr(currentTime), style = MaterialTheme.typography.bodySmall,
                        color = onHero.copy(alpha = 0.8f), maxLines = 1)
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(50))
                        .background(if (urgent) GoldAccent.copy(alpha = 0.3f) else onHero.copy(alpha = 0.14f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clearAndSetSemantics { contentDescription = summary }
                        .testTag("hero_countdown"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Timer, contentDescription = null, tint = GoldAccent,
                        modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Starts in", style = MaterialTheme.typography.bodySmall,
                        color = onHero.copy(alpha = 0.85f))
                    Spacer(Modifier.width(6.dp))
                    Text(ltr(countdown), style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold, color = onHero, maxLines = 1)
                }
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
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen)
            .testTag("prayer_times_error_card"),
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        outlined = false,
        contentPadding = PaddingValues(start = Spacing.xl, top = 18.dp, end = Spacing.xl, bottom = Spacing.md)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Default.ErrorOutline,
                    accent = MaterialTheme.colorScheme.error,
                    size = 36.dp,
                    iconSize = 20.dp
                )
                Spacer(Modifier.width(Spacing.md))
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
                    onClick = com.example.core.analytics.AppAnalytics.trackAction("home_prayer_sections", "prayer_times_fix_or_retry", if (locationIssue != null || needsLocation) onFixLocation else onRetry),
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
                    TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("home_prayer_sections", "retry_prayer_times", onRetry), modifier = Modifier.heightIn(min = 48.dp)) {
                        LocalizedText("Try again", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("home_prayer_sections", "open_calculation_details", onOpenDetails), modifier = Modifier.heightIn(min = 48.dp)) {
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
internal fun PrayerScheduleCard(
    uiState: MainUiState,
    onOpenPrayerDetails: () -> Unit,
    nowMillis: Long = System.currentTimeMillis()
) {
    val raw = uiState.rawPrayerTimes ?: return
    val locale = LocalAppLocale.current
    var showAll by rememberSaveable { mutableStateOf(false) }
    // nowMillis comes from Home's minute clock, so "passed" rows update without leaving the tab.
    val tomorrowFajr = isNextFajrTomorrow(uiState, nowMillis)
    val rows = if (showAll) PrayerType.entries else FARD_PRAYERS

    HomeCard(tag = "prayer_schedule_card") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = Icons.Outlined.Schedule, accent = MaterialTheme.colorScheme.primary, size = 36.dp, iconSize = 20.dp)
            Spacer(Modifier.width(Spacing.md))
            LocalizedText(
                text = "Today's Prayer Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            TextButton(
                onClick = { com.example.core.analytics.AppAnalytics.interaction("home_prayer_sections", "toggle_full_schedule", "tap"); showAll = !showAll },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("prayer_schedule_view_all")
            ) {
                LocalizedText(if (showAll) "Show less" else "View all", fontWeight = FontWeight.SemiBold)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        rows.forEach { type ->
            // After Isha the highlighted Fajr is tomorrow's, so show tomorrow's time on that row.
            val showsTomorrow = type == PrayerType.FAJR && tomorrowFajr
            val hours = if (showsTomorrow) uiState.tomorrowPrayerTimes?.fajrHours ?: raw.fajrHours else raw.homeHoursOf(type)
            PrayerRow(
                type = type,
                time = AstronomicalPrayerCalculator.formatHoursToAmPm(hours, locale),
                isNext = type != PrayerType.SUNRISE && type == uiState.nextPrayer,
                isPassed = !showsTomorrow &&
                    (prayerInstantToday(uiState, type, nowMillis)?.let { nowMillis >= it } ?: false),
                isTomorrow = showsTomorrow
            )
        }
        }

        AnimatedVisibility(visible = showAll) {
            TextButton(
                onClick = com.example.core.analytics.AppAnalytics.trackAction("home_prayer_sections", "open_prayer_details_schedule", onOpenPrayerDetails),
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
            .clip(MaterialTheme.shapes.medium)
            .background(if (isNext) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .heightIn(min = if (isNext) 56.dp else 48.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .semantics(mergeDescendants = true) {}
            .testTag("prayer_row_${type.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The next prayer gets a filled badge; the rest keep a light, quiet marker.
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isNext) MaterialTheme.colorScheme.primary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isSunrise -> Icons.Outlined.WbSunny
                    isNext -> Icons.Default.NotificationsActive
                    else -> Icons.Outlined.Schedule
                },
                contentDescription = if (isNext) localizedUiText("Next") else null,
                tint = when {
                    isNext -> MaterialTheme.colorScheme.onPrimary
                    isPassed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(Spacing.md))
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
                    color = (if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        .copy(alpha = if (isPassed) 0.6f else 0.9f),
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
            style = if (isNext) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
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
                .clickable(role = Role.Button, onClick = { com.example.core.analytics.AppAnalytics.interaction("home_prayer_sections", "toggle_other_islamic_times", "tap"); expanded = !expanded })
                .heightIn(min = 56.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .semantics { stateDescription = stateLabel }
                .testTag("other_times_toggle"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = Icons.Outlined.WbSunny, accent = MaterialTheme.colorScheme.tertiary, size = 36.dp, iconSize = 20.dp)
            Spacer(Modifier.width(Spacing.md))
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
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
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

/** Hairline-outlined surface card shared by the Home sections below the hero; grows smoothly when content expands. */
@Composable
internal fun HomeCard(tag: String, content: @Composable ColumnScope.() -> Unit) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen)
            .testTag(tag)
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = Spacing.sm),
        content = content
    )
}
