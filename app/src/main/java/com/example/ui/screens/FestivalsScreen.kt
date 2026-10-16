@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.analytics.AppAnalytics
import com.example.festival.FestivalAamal
import com.example.festival.FestivalAamalProgress
import com.example.festival.FestivalCautions
import com.example.festival.IslamicFestival
import com.example.prayer.HijriCalendarHelper
import com.example.ui.components.AppCard
import com.example.ui.components.HeroSurface
import com.example.ui.components.IconBadge
import com.example.ui.components.LoadingState
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.components.StateMessage
import com.example.ui.components.TonalPill
import com.example.ui.components.groupedShape
import com.example.ui.components.pressScale
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.appLocale
import com.example.ui.localization.bilingualContent
import com.example.ui.localization.formatHijriDate
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.localization.localizePhrase
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.GoldAccent
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar

/** Stable identity of one occurrence: the same occasion can be listed for two Hijri years. */
private val IslamicFestival.key: String get() = "${hijriYear}-${definition.id}"

/**
 * Islamic occasions: the next one as a hero with its countdown, the coming year grouped by Hijri
 * month, and this year's earlier occasions folded away. Tapping any occasion opens its guidance:
 * a tickable checklist of recommended deeds (kept per Hijri year) and the things to avoid.
 */
@Composable
fun FestivalsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    com.example.core.analytics.AnalyticsScreen("festivals_screen")

    val context = LocalContext.current
    val progressStore = remember(context) { FestivalAamalProgress(FestivalAamalProgress.prefs(context)) }
    // Bumped whenever a deed is ticked, so rows and the hero re-read their checklist progress.
    var progressRevision by remember { mutableIntStateOf(0) }
    val schedule = uiState.festivalSchedule
    val festivals = schedule?.festivals.orEmpty()
    val upcoming = festivals.filter { it.daysRemaining >= 0 }
    val earlier = festivals.filter { it.daysRemaining < 0 }.sortedByDescending { it.daysRemaining }
    val next = upcoming.firstOrNull()
    // Later occasions grouped under their Hijri month, in date order.
    val laterByMonth = upcoming.drop(1).groupBy { it.hijriYear to it.definition.hijriMonth }.toList()
    var showEarlier by rememberSaveable { mutableStateOf(false) }
    var openKey by rememberSaveable { mutableStateOf<String?>(null) }
    val open = openKey?.let { key -> festivals.firstOrNull { it.key == key } }
    val openFestival: (IslamicFestival) -> Unit = { festival ->
        AppAnalytics.interaction("festivals_screen", "open_festival_details", "tap", item = festival.definition.id)
        openKey = festival.key
    }
    fun progressOf(festival: IslamicFestival): Pair<Int, Int> {
        if (progressRevision < 0) return 0 to 0
        val total = FestivalAamal.forFestival(festival.definition.id).size
        return progressStore.done(festival.definition.id, festival.hijriYear).count { it < total } to total
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("festivals_screen"),
        contentPadding = PaddingValues(start = Spacing.screen, top = Spacing.sm, end = Spacing.screen, bottom = Spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item(key = "header") {
            ScreenHeader(
                title = "Islamic occasions",
                subtitle = if (uiState.languageCode == "hi") "आज: ${formatHijriDate(uiState.hijriDate, "hi")} • ${uiState.selectedCity.name}"
                    else "Today: ${formatHijriDate(uiState.hijriDate, uiState.languageCode)} • ${uiState.selectedCity.name}",
                onBack = onBack
            ) {
                IconButton(
                    onClick = { AppAnalytics.interaction("festivals_screen", "refresh_festivals", "tap"); viewModel.refreshFestivals() },
                    enabled = !uiState.isFestivalsLoading
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = localizedUiText("Refresh festival dates"))
                }
            }
        }

        when {
            schedule == null && uiState.isFestivalsLoading -> item(key = "loading") { LoadingState() }
            // Dates failed to load: say so and offer a retry instead of an empty page.
            schedule == null || festivals.isEmpty() -> item(key = "empty") {
                StateMessage(
                    icon = Icons.Default.EventBusy,
                    title = "No occasions available for this year.",
                    actionText = "Retry",
                    onAction = { viewModel.refreshFestivals() }
                )
            }
            schedule != null -> {
                if (next != null) item(key = "next_${next.key}") {
                    val (done, total) = progressOf(next)
                    NextOccasionHero(next, done, total, onOpen = { openFestival(next) })
                }

                if (laterByMonth.isNotEmpty()) item(key = "upcoming_title") {
                    FestivalSectionLabel("Upcoming", Modifier.padding(top = Spacing.md))
                }
                laterByMonth.forEach { (month, inMonth) ->
                    item(key = "month_${month.first}_${month.second}") {
                        HijriMonthLabel(month.second, month.first)
                    }
                    items(inMonth, key = { it.key }) { festival ->
                        val (done, total) = progressOf(festival)
                        FestivalRow(
                            festival = festival,
                            done = done,
                            total = total,
                            shape = groupedShape(inMonth.indexOf(festival), inMonth.size),
                            onClick = { openFestival(festival) }
                        )
                    }
                }

                if (earlier.isNotEmpty()) {
                    item(key = "earlier_toggle") {
                        EarlierToggle(
                            count = earlier.size,
                            expanded = showEarlier,
                            onToggle = {
                                AppAnalytics.interaction("festivals_screen", "toggle_earlier_occasions", "tap")
                                showEarlier = !showEarlier
                            }
                        )
                    }
                    if (showEarlier) items(earlier, key = { "earlier_${it.key}" }) { festival ->
                        val (done, total) = progressOf(festival)
                        FestivalRow(
                            festival = festival,
                            done = done,
                            total = total,
                            shape = groupedShape(earlier.indexOf(festival), earlier.size),
                            onClick = { openFestival(festival) },
                            modifier = Modifier.alpha(0.72f)
                        )
                    }
                }

                item(key = "dates_note") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg, start = Spacing.xs, end = Spacing.xs),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp).size(16.dp)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        LocalizedText(
                            modifier = Modifier.weight(1f),
                            text = "${schedule.regionLabel}. Dates depend on local moon sighting. Ahl-e-Sunnat (Hanafi) guidance; not every occasion is an Eid.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (open != null) {
        FestivalDetailSheet(
            festival = open,
            done = remember(open.key, progressRevision) { progressStore.done(open.definition.id, open.hijriYear) },
            onToggleDeed = { index ->
                AppAnalytics.interaction("festivals_screen", "toggle_aamal", "tap", item = open.definition.id)
                progressStore.toggle(open.definition.id, open.hijriYear, index)
                progressRevision++
            },
            onDismiss = { openKey = null }
        )
    }
}

// region Pieces

/** "Today", "Tonight", "Tomorrow" or "12 days": when the occasion begins, as one short label. */
@Composable
private fun countdownLabel(festival: IslamicFestival): String = when {
    festival.daysRemaining == 0 -> localizedUiText("Today")
    // A night observance begins at Maghrib on the evening before its Hijri date.
    festival.daysRemaining == 1 && festival.definition.isNight -> localizedUiText("Tonight")
    festival.daysRemaining == 1 -> localizedUiText("Tomorrow")
    else -> "${festival.daysRemaining} " + localizedUiText("days")
}

private fun festivalCalendar(festival: IslamicFestival): Calendar = Calendar.getInstance().apply {
    clear()
    set(festival.gregorianYear, festival.gregorianMonth - 1, festival.gregorianDay)
}

@Composable
private fun festivalHeading(festival: IslamicFestival): String {
    val def = festival.definition
    // localName is the familiar South Asian name in Roman Hindi ("Rajab ka Aaghaz"); English readers
    // get the English name instead.
    return bilingualContent(def.localName, def.name, LocalAppLanguage.current).text
}

@Composable
private fun nightEveText(festival: IslamicFestival): String? {
    if (!festival.definition.isNight) return null
    val language = LocalAppLanguage.current
    val locale = appLocale(language)
    val date = festivalCalendar(festival)
    val eve = (date.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
    return localizeUiText("Night: ${SimpleDateFormat("EEE, d MMM", locale).format(eve.time)} after Maghrib", language)
}

@Composable
private fun NextOccasionHero(festival: IslamicFestival, done: Int, total: Int, onOpen: () -> Unit) {
    val def = festival.definition
    val language = LocalAppLanguage.current
    val locale = appLocale(language)
    val onHero = Color.White
    val dateText = SimpleDateFormat("EEE, d MMM yyyy", locale).format(festivalCalendar(festival).time)
    val hijri = formatHijriDate(HijriCalendarHelper.create(def.hijriDay, def.hijriMonth, festival.hijriYear), language)
    val eve = nightEveText(festival)

    HeroSurface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth().testTag("festival_card_${def.id}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (def.isNight) Icons.Default.NightsStay else Icons.Default.Celebration,
                contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(Spacing.sm))
            LocalizedText(
                "Next occasion",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = GoldAccent,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
        }
        Spacer(Modifier.height(Spacing.md))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Column(Modifier.padding(end = Spacing.lg).widthIn(max = 260.dp)) {
                Text(
                    festivalHeading(festival),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = onHero
                )
                Text(
                    def.arabicName,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = ArabicFontFamily,
                    color = onHero.copy(alpha = 0.8f)
                )
            }
            CountdownBlock(festival, onHero, Modifier.align(Alignment.CenterVertically))
        }
        Spacer(Modifier.height(Spacing.md))
        HorizontalDivider(color = onHero.copy(alpha = 0.14f))
        Spacer(Modifier.height(Spacing.md))
        HeroFact(Icons.Default.Event, "$dateText  •  ${ltr(hijri)}", onHero)
        if (eve != null) HeroFact(Icons.Default.NightsStay, eve, onHero, Modifier.padding(top = Spacing.xs))
        if (total > 0) {
            Spacer(Modifier.height(Spacing.md))
            Surface(
                onClick = onOpen,
                shape = RoundedCornerShape(50),
                color = onHero.copy(alpha = 0.14f),
                contentColor = onHero,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Checklist, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    LocalizedText("What to do", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = onHero)
                    Spacer(Modifier.width(Spacing.sm))
                    Text(ltr("$done/$total"), style = MaterialTheme.typography.labelLarge, color = GoldAccent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(Spacing.xs))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = onHero.copy(alpha = 0.8f))
                }
            }
        }
    }
}

/** Big day count for the hero, or a single word for today / tonight / tomorrow. */
@Composable
private fun CountdownBlock(festival: IslamicFestival, onHero: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.End) {
        if (festival.daysRemaining >= 2) {
            Text(
                festival.daysRemaining.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = onHero,
                maxLines = 1
            )
            LocalizedText(
                "days remaining",
                style = MaterialTheme.typography.labelMedium,
                color = onHero.copy(alpha = 0.8f),
                textAlign = TextAlign.End
            )
        } else {
            Text(
                countdownLabel(festival),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HeroFact(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onHero: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = onHero.copy(alpha = 0.75f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = onHero.copy(alpha = 0.9f))
    }
}

@Composable
private fun FestivalSectionLabel(text: String, modifier: Modifier = Modifier) {
    LocalizedText(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(start = Spacing.xs).semantics { heading() }
    )
}

/** Small Hijri month heading ("Rajab 1448") that groups the occasions under it. */
@Composable
private fun HijriMonthLabel(month: Int, hijriYear: Int) {
    val language = LocalAppLanguage.current
    val date = HijriCalendarHelper.create(1, month, hijriYear)
    val rtl = isRtlAppLanguage(language)
    val label = if (rtl) "${date.monthNameArabic} $hijriYear هـ" else "${date.monthNameEnglish} $hijriYear AH"
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = Spacing.xs, top = Spacing.sm, bottom = Spacing.xxs)
    )
}

/** One occasion: a calendar leaf with the Gregorian day, its names, when it begins and checklist progress. */
@Composable
private fun FestivalRow(
    festival: IslamicFestival,
    done: Int,
    total: Int,
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val def = festival.definition
    val language = LocalAppLanguage.current
    val locale = appLocale(language)
    val date = festivalCalendar(festival)
    val past = festival.daysRemaining < 0
    val soon = festival.daysRemaining in 0..7
    val accent = MaterialTheme.colorScheme.primary
    val hijriShort = "${def.hijriDay} " + (if (isRtlAppLanguage(language)) HijriCalendarHelper.create(1, def.hijriMonth, festival.hijriYear).monthNameArabic
        else HijriCalendarHelper.create(1, def.hijriMonth, festival.hijriYear).monthNameEnglish)
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .testTag("festival_card_${def.id}")
    ) {
        Row(
            Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md).heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calendar leaf: the Gregorian day and month at a glance.
            Column(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (soon) accent else accent.copy(alpha = 0.1f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    festival.gregorianDay.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (soon) MaterialTheme.colorScheme.onPrimary else accent,
                    maxLines = 1
                )
                Text(
                    SimpleDateFormat("MMM", locale).format(date.time),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (soon) MaterialTheme.colorScheme.onPrimary else accent,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    festivalHeading(festival),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (def.isNight) {
                        Icon(Icons.Default.NightsStay, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        "${ltr(hijriShort)}  •  ${def.arabicName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (total > 0 && done > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (done >= total) Icons.Default.CheckCircle else Icons.Default.Checklist,
                            contentDescription = null, tint = accent, modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(ltr("$done/$total"), style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.width(Spacing.sm))
            if (!past) {
                TonalPill(
                    text = countdownLabel(festival),
                    containerColor = if (festival.daysRemaining <= 1) accent else accent.copy(alpha = 0.1f),
                    contentColor = if (festival.daysRemaining <= 1) MaterialTheme.colorScheme.onPrimary else accent
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EarlierToggle(count: Int, expanded: Boolean, onToggle: () -> Unit) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "earlier_chevron")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.md)
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onToggle)
            .heightIn(min = 48.dp)
            .padding(horizontal = Spacing.xs)
            .testTag("festivals_earlier"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LocalizedText(
            "Earlier this year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.width(Spacing.sm))
        TonalPill(count.toString())
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Default.ExpandMore, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.rotate(rotation)
        )
    }
}

// endregion

// region Detail

@Composable
private fun FestivalDetailSheet(
    festival: IslamicFestival,
    done: Set<Int>,
    onToggleDeed: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val def = festival.definition
    val language = LocalAppLanguage.current
    val locale = appLocale(language)
    val aamal = FestivalAamal.forFestival(def.id)
    val cautions = FestivalCautions.forFestival(def.id)
    val dateText = SimpleDateFormat("EEE, d MMM yyyy", locale).format(festivalCalendar(festival).time)
    val hijri = formatHijriDate(HijriCalendarHelper.create(def.hijriDay, def.hijriMonth, festival.hijriYear), language)
    val eve = nightEveText(festival)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("festival_detail_${def.id}")
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = Spacing.xl, end = Spacing.xl, bottom = Spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Title block
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    if (def.isNight) Icons.Default.NightsStay else Icons.Default.Celebration,
                    accent = MaterialTheme.colorScheme.primary, size = 48.dp, iconSize = 26.dp
                )
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        festivalHeading(festival),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        def.arabicName,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = ArabicFontFamily,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (festival.daysRemaining >= 0) TonalPill(countdownLabel(festival))
            }

            // When
            AppCard(contentPadding = PaddingValues(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                DetailFact(Icons.Default.Event, dateText)
                DetailFact(Icons.Default.CalendarMonth, ltr(hijri))
                if (eve != null) DetailFact(Icons.Default.NightsStay, eve)
            }

            // Significance
            val description = localizePhrase(def.description, language)
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                if (description == def.description && language != "en") FallbackLanguageLabel("English")
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }

            if (aamal.isNotEmpty()) AamalChecklist(def.id, aamal, done, onToggleDeed)
            if (cautions.isNotEmpty()) CautionsCard(def.id)
        }
    }
}

@Composable
private fun DetailFact(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

/** Recommended deeds as a checklist; ticks are kept for this occurrence's Hijri year. */
@Composable
private fun AamalChecklist(
    festivalId: String,
    aamal: List<com.example.festival.Aamal>,
    done: Set<Int>,
    onToggle: (Int) -> Unit
) {
    val language = LocalAppLanguage.current
    val draft = aamal.map { item ->
        Triple(
            bilingualContent(item.title, item.englishTitle, language),
            bilingualContent(item.detail, item.englishDetail, language),
            bilingualContent(item.source, item.englishSource, language).text
        )
    }
    // If any deed lacks a reviewed translation, show the whole list in one fallback language.
    val guidanceLanguage = if (draft.any { it.first.fallbackLanguage != null || it.second.fallbackLanguage != null }) {
        if (language in setOf("hi", "ur", "hinglish")) "hinglish" else "en"
    } else language
    val shown = if (guidanceLanguage == language) draft else aamal.map { item ->
        Triple(
            bilingualContent(item.title, item.englishTitle, guidanceLanguage),
            bilingualContent(item.detail, item.englishDetail, guidanceLanguage),
            bilingualContent(item.source, item.englishSource, guidanceLanguage).text
        )
    }
    val count = done.count { it < aamal.size }
    val fraction by animateFloatAsState(count.toFloat() / aamal.size.coerceAtLeast(1), label = "aamal_progress")

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.animateContentSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LocalizedText(
                "What to do",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            Text(ltr("$count/${aamal.size}"), style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        if (guidanceLanguage != language) FallbackLanguageLabel(if (guidanceLanguage == "en") "English" else "Hinglish (Roman Hindi)")
        shown.forEachIndexed { index, (title, detail, source) ->
            val checked = index in done
            Surface(
                onClick = { onToggle(index) },
                shape = groupedShape(index, shown.size, outer = 18.dp, inner = 6.dp),
                color = if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().testTag("festival_aamal_${festivalId}_$index")
            ) {
                Row(Modifier.padding(start = Spacing.xs, end = Spacing.md, top = Spacing.xs, bottom = Spacing.md), verticalAlignment = Alignment.Top) {
                    Checkbox(checked = checked, onCheckedChange = { onToggle(index) })
                    Column(Modifier.weight(1f).padding(top = Spacing.md), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(title.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(detail.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ReferenceText(source, language)
                    }
                }
            }
        }
    }
}

@Composable
private fun CautionsCard(festivalId: String) {
    val language = LocalAppLanguage.current
    val cautions = FestivalCautions.forFestival(festivalId)
    val draft = cautions.map { caution ->
        bilingualContent(caution.advice, caution.englishAdvice, language) to
            bilingualContent(caution.source, caution.englishSource, language).text
    }
    val cautionLanguage = if (draft.any { it.first.fallbackLanguage != null }) {
        if (language in setOf("hi", "ur", "hinglish")) "hinglish" else "en"
    } else language
    val shown = if (cautionLanguage == language) draft else cautions.map { caution ->
        bilingualContent(caution.advice, caution.englishAdvice, cautionLanguage) to
            bilingualContent(caution.source, caution.englishSource, cautionLanguage).text
    }
    AppCard(
        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
        outlined = false,
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.fillMaxWidth().testTag("festival_cautions_$festivalId")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ReportGmailerrorred, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.sm))
            LocalizedText(
                "What to avoid",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { heading() }
            )
        }
        if (cautionLanguage != language) FallbackLanguageLabel(if (cautionLanguage == "en") "English" else "Hinglish (Roman Hindi)")
        shown.forEach { (advice, source) ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.padding(top = 7.dp).size(6.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(advice.text, style = MaterialTheme.typography.bodyMedium)
                    ReferenceText(source, language)
                }
            }
        }
    }
}

@Composable
private fun FallbackLanguageLabel(languageName: String) {
    Text(languageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ReferenceText(source: String, language: String) {
    Text(
        text = "${localizeUiText("Reference", language)}: $source",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// endregion
