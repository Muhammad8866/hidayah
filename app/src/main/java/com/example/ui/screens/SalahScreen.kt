@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.prayer.PrayerSettingsStore
import com.example.salah.*
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.theme.ArabicFontFamily

private enum class SalahTab(val title: String) { DAILY("Daily"), GUIDE("How to pray"), DUAS("Recitations"), SPECIAL("More prayers") }

@Composable
fun SalahScreen(onBack: () -> Unit, onOpenQibla: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember(context) { PrayerSettingsStore.prefs(context) }
    var madhhabName by rememberSaveable { mutableStateOf(SalahSettingsStore.load(prefs).name) }
    val madhhab = Madhhab.entries.firstOrNull { it.name == madhhabName } ?: Madhhab.HANAFI
    var showSchoolPicker by remember { mutableStateOf(false) }
    var tabName by rememberSaveable { mutableStateOf(SalahTab.DAILY.name) }
    val tab = SalahTab.entries.firstOrNull { it.name == tabName } ?: SalahTab.DAILY
    var selectedPrayerId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTopicId by rememberSaveable { mutableStateOf<String?>(null) }
    var guideRakats by rememberSaveable { mutableIntStateOf(2) }
    var beginner by rememberSaveable { mutableStateOf(true) }
    var guideIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedPrayer = SalahContent.daily.firstOrNull { it.id == selectedPrayerId }
    val selectedTopic = SalahContent.topics.firstOrNull { it.id == selectedTopicId }
    val guideSteps = remember(guideRakats, madhhab) { SalahContent.prayerSteps(guideRakats, madhhab) }
    val listState = rememberLazyListState()

    LaunchedEffect(tabName, selectedPrayerId, selectedTopicId) { listState.scrollToItem(0) }

    fun closeDetail() {
        if (selectedTopicId != null) selectedTopicId = null
        else if (selectedPrayerId != null) selectedPrayerId = null
        else onBack()
    }
    BackHandler(enabled = selectedTopicId != null || selectedPrayerId != null) { closeDetail() }

    if (showSchoolPicker) AlertDialog(
        onDismissRequest = { showSchoolPicker = false },
        title = { LocalizedText("Choose madhhab") },
        text = {
            Column {
                Madhhab.entries.forEach { school ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            madhhabName = school.name
                            SalahSettingsStore.save(prefs, school)
                            showSchoolPicker = false
                        }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = school == madhhab, onClick = {
                            madhhabName = school.name
                            SalahSettingsStore.save(prefs, school)
                            showSchoolPicker = false
                        })
                        Text(school.label)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { showSchoolPicker = false }) { LocalizedText("Close") } }
    )

    ToolScreen(DailyTool.SALAH, ::closeDetail, modifier, listState) { accent ->
        item(key = "school") {
            HeroCard(accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        LocalizedText("Your prayer guide", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${madhhab.label} school · educational guide", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { showSchoolPicker = true }, modifier = Modifier.testTag("salah_madhhab")) {
                        LocalizedText("Change")
                    }
                }
                Text(SalahContent.schoolSummaries.getValue(madhhab), style = MaterialTheme.typography.bodySmall)
            }
        }
        if (selectedPrayer == null && selectedTopic == null) {
            item(key = "tabs") {
                ScrollableTabRow(selectedTabIndex = tab.ordinal, edgePadding = 0.dp, containerColor = MaterialTheme.colorScheme.background) {
                    SalahTab.entries.forEach { option ->
                        Tab(selected = tab == option, onClick = { tabName = option.name }, text = { LocalizedText(option.title, maxLines = 1) }, modifier = Modifier.testTag("salah_tab_${option.name.lowercase()}"))
                    }
                }
            }
        }
        when {
            selectedPrayer != null -> {
                item(key = "selected_prayer") { PrayerDetail(selectedPrayer, madhhab, accent) { tabName = SalahTab.GUIDE.name; guideRakats = selectedPrayer.fard; guideIndex = 0; selectedPrayerId = null } }
            }
            selectedTopic != null -> {
                item(key = "topic_detail") { TopicDetail(selectedTopic, madhhab, accent) }
            }
            tab == SalahTab.DAILY -> {
                item(key = "daily_intro") { InfoNote(SalahContent.Notes.DAILY) }
                items(SalahContent.daily, key = { it.id }) { prayer ->
                    Card(
                        onClick = { selectedPrayerId = prayer.id },
                        modifier = Modifier.fillMaxWidth().testTag("salah_${prayer.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Default.Mosque, accent, 42.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(prayer.titleIn(LocalAppLanguage.current), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${prayer.fard} Fard rak‘at · ${prayer.units.size} prayer parts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accent)
                        }
                    }
                }
                item(key = "daily_source") { InfoNote("Prayer times are available on Home. This guide does not change your prayer-time calculation or tracker.") }
            }
            tab == SalahTab.GUIDE -> {
                item(key = "guide_controls") {
                    ToolCard {
                        SectionTitle("Step-by-step Salah")
                        LocalizedText("Choose the Fard prayer length. The guide shows each position in order.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (2..4).forEach { count ->
                                FilterChip(selected = guideRakats == count, onClick = { guideRakats = count; guideIndex = 0 }, label = { Text("$count Rak‘at") }, modifier = Modifier.testTag("salah_${count}_rakat"))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                LocalizedText("Beginner Mode", style = MaterialTheme.typography.titleSmall)
                                LocalizedText("One step at a time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = beginner, onCheckedChange = { beginner = it; guideIndex = 0 }, modifier = Modifier.testTag("salah_beginner_switch"))
                        }
                    }
                }
                if (beginner) {
                    item(key = "beginner_step") {
                        val safeIndex = guideIndex.coerceIn(0, guideSteps.lastIndex)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            LinearProgressIndicator(progress = { (safeIndex + 1).toFloat() / guideSteps.size }, modifier = Modifier.fillMaxWidth())
                            Text("Step ${safeIndex + 1} of ${guideSteps.size}", style = MaterialTheme.typography.labelMedium, color = accent)
                            StepDetail(guideSteps[safeIndex], accent)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { guideIndex = (safeIndex - 1).coerceAtLeast(0) }, enabled = safeIndex > 0, modifier = Modifier.weight(1f)) { LocalizedText("Previous") }
                                Button(onClick = { guideIndex = if (safeIndex == guideSteps.lastIndex) 0 else safeIndex + 1 }, modifier = Modifier.weight(1f).testTag("salah_next_step")) { LocalizedText(if (safeIndex == guideSteps.lastIndex) "Start again" else "Next") }
                            }
                        }
                    }
                } else {
                    items(guideSteps, key = { it.id }) { step -> StepDetail(step, accent) }
                }
                item(key = "guide_note") { InfoNote(SalahContent.Notes.GUIDE) }
            }
            tab == SalahTab.DUAS -> {
                item(key = "dua_intro") { InfoNote(SalahContent.Notes.RECITATION) }
                items(SalahContent.recitations.values.toList(), key = { it.id }) { recitation -> RecitationCard(recitation) }
            }
            else -> {
                item(key = "qibla_action") { ToolCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = accent)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            SectionTitle("Qibla")
                            LocalizedText("Use the app's existing direction finder.", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = onOpenQibla, modifier = Modifier.testTag("salah_qibla")) { LocalizedText("Open") }
                    }
                } }
                items(SalahContent.topics, key = { it.id }) { topic ->
                    Card(onClick = { selectedTopicId = topic.id }, modifier = Modifier.fillMaxWidth().testTag("salah_topic_${topic.id}"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = MaterialTheme.shapes.large) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(topic.titleIn(LocalAppLanguage.current), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                topic.kindFor(madhhab)?.let { KindBadge(it, madhhab) }
                                Text(topic.summaryIn(LocalAppLanguage.current), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerDetail(prayer: DailyPrayer, madhhab: Madhhab, accent: androidx.compose.ui.graphics.Color, onLearn: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HeroCard(accent) {
            Text(prayer.titleIn(LocalAppLanguage.current), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${prayer.fard} obligatory rak‘at", color = accent, style = MaterialTheme.typography.titleMedium)
            Button(onClick = onLearn, modifier = Modifier.testTag("salah_learn_fard")) { LocalizedText("Learn ${prayer.fard} Rak‘at") }
        }
        prayer.units.forEach { part ->
            ToolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(part.countLabel(madhhab), style = if (part.kind == SalahKind.WITR && madhhab != Madhhab.HANAFI) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium, color = accent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        KindBadge(part.kind, madhhab)
                        Text(part.whenPrayedIn(LocalAppLanguage.current), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        InfoNote(SalahContent.Notes.PRAYER_DETAIL)
    }
}

@Composable
private fun KindBadge(kind: SalahKind, madhhab: Madhhab) {
    val label = if (kind == SalahKind.WITR) "Witr · ${if (madhhab == Madhhab.HANAFI) "Wajib" else "Sunnah"}" else kind.label
    Surface(color = if (kind == SalahKind.FARD || (kind == SalahKind.WITR && madhhab == Madhhab.HANAFI)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StepDetail(step: GuideStep, accent: androidx.compose.ui.graphics.Color) {
    ToolCard {
        Text(step.title.inLanguage(LocalAppLanguage.current), style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold)
        Text(step.instruction.inLanguage(LocalAppLanguage.current), style = MaterialTheme.typography.bodyMedium)
        step.recitationIds.forEach { id -> SalahContent.recitations[id]?.let { RecitationCard(it) } }
    }
}

@Composable
private fun RecitationCard(recitation: Recitation) {
    ToolCard {
        Text(recitation.title.inLanguage(LocalAppLanguage.current), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(recitation.arabic, style = MaterialTheme.typography.titleLarge.copy(fontFamily = ArabicFontFamily), textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
        Text(recitation.transliteration, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        Text(recitation.meaning.inLanguage(LocalAppLanguage.current), style = MaterialTheme.typography.bodyMedium)
        Text(recitation.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TopicDetail(topic: SalahTopic, madhhab: Madhhab, accent: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HeroCard(accent) {
            Text(topic.titleIn(LocalAppLanguage.current), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            topic.kindFor(madhhab)?.let { KindBadge(it, madhhab) }
            Text(topic.summaryIn(LocalAppLanguage.current), style = MaterialTheme.typography.bodyMedium)
        }
        topic.stepsIn(LocalAppLanguage.current).forEachIndexed { index, instruction ->
            ToolCard {
                Row(verticalAlignment = Alignment.Top) {
                    Text("${index + 1}.", color = accent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Text(instruction, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        topic.schoolNotes[madhhab]?.let { note -> InfoNote("${madhhab.label}: $note") }
        Text("Reference: ${topic.source}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        InfoNote(SalahContent.Notes.TOPIC)
    }
}
