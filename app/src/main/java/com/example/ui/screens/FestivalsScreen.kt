package com.example.ui.screens
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.LocalizedPhraseText
import com.example.ui.localization.localizedUiText

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.festival.FestivalAamal
import com.example.festival.FestivalCautions
import com.example.festival.IslamicFestival
import com.example.prayer.HijriCalendarHelper
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.appLocale
import com.example.ui.localization.bilingualContent
import com.example.ui.localization.formatHijriDate
import com.example.ui.localization.localizePhrase
import com.example.ui.localization.localizeUiText
import com.example.ui.components.BackIconButton
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar

@Composable
fun FestivalsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val schedule = uiState.festivalSchedule
    var upcomingOnly by rememberSaveable { mutableStateOf(false) }
    val visibleFestivals = schedule?.festivals?.filter { !upcomingOnly || it.daysRemaining >= 0 }.orEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("festivals_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) BackIconButton(onBack)
                Column(modifier = Modifier.weight(1f)) {
                    LocalizedText(
                        text = "Islamic occasions ${schedule?.gregorianYear ?: Calendar.getInstance().get(Calendar.YEAR)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LocalizedText(
                        text = if (uiState.languageCode == "hi") "आज: ${formatHijriDate(uiState.hijriDate, "hi")} • ${uiState.selectedCity.name}"
                            else "Today: ${formatHijriDate(uiState.hijriDate, uiState.languageCode)} • ${uiState.selectedCity.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.refreshFestivals() }, enabled = !uiState.isFestivalsLoading) {
                    Icon(Icons.Default.Refresh, contentDescription = localizedUiText("Refresh festival dates"))
                }
            }
        }

        if (schedule != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        LocalizedText(
                            modifier = Modifier.weight(1f),
                        text = "${schedule.regionLabel}. Dates depend on local moon sighting. Ahl-e-Sunnat (Hanafi) guidance; not every occasion is an Eid.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        LocalizedText(
                            if (uiState.languageCode == "hi") "साल भर में ${schedule.festivals.size} इस्लामी अवसर"
                            else "${schedule.festivals.size} occasions throughout the year",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
                        )
                        LocalizedText("Tap a card for practices and things to avoid", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilterChip(selected = upcomingOnly, onClick = { upcomingOnly = !upcomingOnly }, label = { LocalizedText(if (upcomingOnly) "Upcoming" else "All year") })
                }
            }
        }

        when {
            schedule == null && uiState.isFestivalsLoading -> item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            schedule != null && visibleFestivals.isEmpty() -> item {
                LocalizedText(
                    text = if (upcomingOnly) "No more upcoming occasions this year. Select All year to see the full calendar." else "No occasions available for this year.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            schedule != null -> items(visibleFestivals, key = { "${it.hijriYear}-${it.definition.id}" }) { festival ->
                FestivalCard(festival)
            }
        }
    }
}

@Composable
private fun FestivalCard(festival: IslamicFestival) {
    val def = festival.definition
    val date = Calendar.getInstance().apply {
        clear()
        set(festival.gregorianYear, festival.gregorianMonth - 1, festival.gregorianDay)
    }
    val language = LocalAppLanguage.current
    val locale = appLocale(language)
    val dateText = SimpleDateFormat("EEE, d MMM yyyy", locale).format(date.time)
    val eveText = if (def.isNight) {
        val eve = (date.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
        localizeUiText("Night: ${SimpleDateFormat("EEE, d MMM", locale).format(eve.time)} after Maghrib", language)
    } else null
    val hijriMonth = HijriCalendarHelper.create(def.hijriDay, def.hijriMonth, festival.hijriYear)
    val isToday = festival.daysRemaining == 0
    val aamal = FestivalAamal.forFestival(def.id)
    val cautions = FestivalCautions.forFestival(def.id)
    var expanded by rememberSaveable(def.id, festival.hijriYear) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, enabled = aamal.isNotEmpty() || cautions.isNotEmpty()) { expanded = !expanded }
            .testTag("festival_card_${def.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    LocalizedText(
                        text = formatHijriDate(hijriMonth, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                LocalizedText(
                    text = when (festival.daysRemaining) {
                        0 -> "Today"
                        1 -> "Tomorrow"
                        in Int.MIN_VALUE..-1 -> "Passed"
                        else -> if (language == "hi") "${festival.daysRemaining} दिन शेष" else "${festival.daysRemaining} days remaining"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            // localName is the familiar South Asian name in Roman Hindi ("Rajab ka Aaghaz"); English
            // readers get the English name as the heading instead of a repeat below it.
            val heading = bilingualContent(def.localName, def.name, language).text
            Text(text = heading, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = if (heading == def.name) def.arabicName else "${localizePhrase(def.name, language)} • ${def.arabicName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                LocalizedText(text = dateText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            if (eveText != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    LocalizedText(text = eveText, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val description = localizePhrase(def.description, language)
            if (description == def.description && language != "en") FallbackLanguageLabel("English")
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (aamal.isNotEmpty() || cautions.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    LocalizedText(
                        text = if (expanded) "Hide guidance" else "What to do · What to avoid",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                        LocalizedText("What to do", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        val aamalDraft = aamal.map { item ->
                            Triple(
                                bilingualContent(item.title, item.englishTitle, language),
                                bilingualContent(item.detail, item.englishDetail, language),
                                bilingualContent(item.source, item.englishSource, language).text
                            )
                        }
                        val guidanceLanguage = if (aamalDraft.any { it.first.fallbackLanguage != null || it.second.fallbackLanguage != null }) {
                            if (language in setOf("hi", "ur", "hinglish")) "hinglish" else "en"
                        } else language
                        val aamalText = if (guidanceLanguage == language) aamalDraft else aamal.map { item ->
                            Triple(bilingualContent(item.title, item.englishTitle, guidanceLanguage),
                                bilingualContent(item.detail, item.englishDetail, guidanceLanguage),
                                bilingualContent(item.source, item.englishSource, guidanceLanguage).text)
                        }
                        if (guidanceLanguage != language) FallbackLanguageLabel(if (guidanceLanguage == "en") "English" else "Hinglish (Roman Hindi)")
                        aamalText.forEachIndexed { index, (title, detail, source) ->
                            Column {
                                Row {
                                    Text("${index + 1}. ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = title.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(text = detail.text, style = MaterialTheme.typography.bodySmall)
                                ReferenceText(source, language)
                            }
                        }
                        if (cautions.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            LocalizedText("What to avoid", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            val cautionDraft = cautions.map { caution ->
                                bilingualContent(caution.advice, caution.englishAdvice, language) to
                                    bilingualContent(caution.source, caution.englishSource, language).text
                            }
                            val cautionLanguage = if (cautionDraft.any { it.first.fallbackLanguage != null }) {
                                if (language in setOf("hi", "ur", "hinglish")) "hinglish" else "en"
                            } else language
                            val cautionText = if (cautionLanguage == language) cautionDraft else cautions.map { caution ->
                                bilingualContent(caution.advice, caution.englishAdvice, cautionLanguage) to
                                    bilingualContent(caution.source, caution.englishSource, cautionLanguage).text
                            }
                            if (cautionLanguage != language) FallbackLanguageLabel(if (cautionLanguage == "en") "English" else "Hinglish (Roman Hindi)")
                            cautionText.forEach { (advice, source) ->
                                Column {
                                    Row {
                                        Text("• ", style = MaterialTheme.typography.bodySmall)
                                        Text(advice.text, style = MaterialTheme.typography.bodySmall)
                                    }
                                    ReferenceText(source, language)
                                }
                            }
                        }
                    }
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
