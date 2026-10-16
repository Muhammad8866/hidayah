@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayer.PrayerSettingsStore
import com.example.ui.components.AppCard
import com.example.salah.*
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.localizeUiText
import com.example.ui.theme.ArabicFontFamily
import kotlinx.coroutines.launch

private enum class SalahTab(val title: SalahText) {
    DAILY(SalahUi.TAB_DAILY), GUIDE(SalahUi.TAB_GUIDE), DUAS(SalahUi.TAB_RECITATIONS),
    RULES(SalahUi.TAB_RULES), SPECIAL(SalahUi.TAB_MORE)
}

/** Salah copy carries its own reviewed translations, so it never goes through word-level UI packs. */
@Composable
private fun SalahText.text(): String = inLanguage(LocalAppLanguage.current)

/** One-word controls fall back to the app-wide control packs, which cover every app language. */
@Composable
private fun SalahText.control(): String {
    val language = LocalAppLanguage.current
    return translations[language] ?: localizeUiText(en, language)
}

@Composable
fun SalahScreen(onBack: () -> Unit, onOpenQibla: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("salah_screen")

    val context = LocalContext.current
    val prefs = remember(context) { PrayerSettingsStore.prefs(context) }
    var madhhabName by rememberSaveable { mutableStateOf(SalahSettingsStore.load(prefs).name) }
    val madhhab = Madhhab.entries.firstOrNull { it.name == madhhabName } ?: Madhhab.HANAFI
    var showSchoolPicker by rememberSaveable { mutableStateOf(false) }
    var tabName by rememberSaveable { mutableStateOf(SalahTab.DAILY.name) }
    val tab = SalahTab.entries.firstOrNull { it.name == tabName } ?: SalahTab.DAILY
    var selectedPrayerId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTopicId by rememberSaveable { mutableStateOf<String?>(null) }
    var formName by rememberSaveable { mutableStateOf(GuideForm.FARD_2.name) }
    val form = GuideForm.fromKey(formName)
    var worshipperName by rememberSaveable { mutableStateOf(SalahSettingsStore.loadWorshipper(prefs).name) }
    val worshipper = Worshipper.entries.firstOrNull { it.name == worshipperName } ?: Worshipper.MAN
    var prayingAsName by rememberSaveable { mutableStateOf(PrayingAs.ALONE.name) }
    val prayingAs = PrayingAs.entries.firstOrNull { it.name == prayingAsName } ?: PrayingAs.ALONE
    var beginner by rememberSaveable { mutableStateOf(true) }
    var guideIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedPrayer = SalahContent.allPrayers.firstOrNull { it.id == selectedPrayerId }
    val selectedTopic = (SalahContent.rules + SalahContent.topics).firstOrNull { it.id == selectedTopicId }
    val guideSteps = remember(form, madhhab, worshipper, prayingAs) {
        SalahContent.prayerSteps(form, madhhab, worshipper, prayingAs)
    }
    // A recitation already shown in an earlier step starts collapsed, so long prayers stay readable.
    val repeatedRecitations = remember(guideSteps) {
        val seen = mutableSetOf<String>()
        guideSteps.associate { step -> step.id to step.recitationIds.filterNot(seen::add).toSet() }
    }
    val schoolNote = SalahContent.guideSchoolNotes[madhhab]
    val listState = rememberLazyListState()
    com.example.core.analytics.AnalyticsScroll("salah_screen", listState)
    val scope = rememberCoroutineScope()

    LaunchedEffect(tabName, selectedPrayerId, selectedTopicId) { listState.scrollToItem(0) }

    fun closeDetail() {
        if (selectedTopicId != null) selectedTopicId = null
        else if (selectedPrayerId != null) selectedPrayerId = null
        else onBack()
    }
    BackHandler(enabled = selectedTopicId != null || selectedPrayerId != null) { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "close_detail_system_back", "back"); closeDetail() }

    fun openGuide(target: GuideForm, behindImam: Boolean = false) {
        formName = target.name
        prayingAsName = (if (behindImam) PrayingAs.BEHIND_IMAM else PrayingAs.ALONE).name
        guideIndex = 0
        tabName = SalahTab.GUIDE.name
        selectedPrayerId = null
        selectedTopicId = null
    }

    fun showStep(index: Int) {
        guideIndex = index
        // header, school card, tabs and guide controls come before the step, plus the school note.
        scope.launch { listState.animateScrollToItem(if (schoolNote == null) 4 else 5) }
    }

    fun chooseSchool(school: Madhhab) {
        madhhabName = school.name
        SalahSettingsStore.save(prefs, school)
        com.example.core.analytics.AppAnalytics.setting("set_madhhab", school.name)
        com.example.core.analytics.AppAnalytics.userProperty("madhhab", school.name)
        showSchoolPicker = false
    }

    if (showSchoolPicker) AlertDialog(
        onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "dismiss_madhhab_picker", "dismiss"); showSchoolPicker = false },
        title = { Text(SalahUi.CHOOSE_MADHHAB.text()) },
        text = {
            Column {
                Madhhab.entries.forEach { school ->
                    Row(
                        Modifier.fillMaxWidth().clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "select_madhhab_row", "tap", item = school.name); chooseSchool(school) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = school == madhhab, onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "select_madhhab_radio", "tap", item = school.name); chooseSchool(school) })
                        Text(school.title.text(), modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "close_madhhab_picker", "tap"); showSchoolPicker = false }) { Text(SalahUi.CLOSE.control()) } }
    )

    ToolScreen(DailyTool.SALAH, ::closeDetail, modifier, listState) { accent ->
        item(key = "school") {
            HeroCard(accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(SalahUi.HERO_TITLE.text(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            SalahUi.SCHOOL_LINE.inLanguage(LocalAppLanguage.current, "school" to madhhab.title.text()),
                            style = MaterialTheme.typography.labelLarge,
                            color = accent
                        )
                    }
                    TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "open_madhhab_picker", "tap"); showSchoolPicker = true }, modifier = Modifier.testTag("salah_madhhab")) {
                        Text(SalahUi.CHANGE.control())
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    SalahContent.schoolSummaries.getValue(madhhab).text(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (selectedPrayer == null && selectedTopic == null) {
            item(key = "tabs") {
                ScrollableTabRow(selectedTabIndex = tab.ordinal, edgePadding = 0.dp, containerColor = MaterialTheme.colorScheme.background) {
                    SalahTab.entries.forEach { option ->
                        Tab(
                            selected = tab == option,
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "select_salah_tab", "tap", item = option.name); tabName = option.name },
                            text = { Text(option.title.text(), maxLines = 1) },
                            modifier = Modifier.testTag("salah_tab_${option.name.lowercase()}")
                        )
                    }
                }
            }
        }
        when {
            selectedPrayer != null -> item(key = "selected_prayer") {
                PrayerDetail(selectedPrayer, madhhab, accent) { target ->
                    // Jumu‘ah Fard is only ever prayed behind an imam.
                    openGuide(target, behindImam = selectedPrayer.id == "jumuah" && target.kind == SalahKind.FARD)
                }
            }
            selectedTopic != null -> item(key = "topic_detail") {
                TopicDetail(selectedTopic, madhhab, accent, onLearn = ::openGuide)
            }
            tab == SalahTab.DAILY -> {
                item(key = "daily_intro") { SalahNote(SalahContent.Notes.DAILY) }
                items(SalahContent.allPrayers, key = { it.id }) { prayer ->
                    PrayerCard(prayer, madhhab, accent) { selectedPrayerId = prayer.id }
                }
                item(key = "daily_aloud") { SalahNote(SalahContent.Notes.ALOUD) }
                item(key = "daily_hadith") { HadithQuote(SalahContent.Notes.TWELVE_SUNNAH, SalahContent.Notes.TWELVE_SUNNAH_SOURCE, accent) }
                item(key = "daily_times") { SalahNote(SalahContent.Notes.TIMES) }
            }
            tab == SalahTab.GUIDE -> {
                item(key = "guide_controls") {
                    GuideControls(
                        form = form,
                        worshipper = worshipper,
                        prayingAs = prayingAs,
                        beginner = beginner,
                        onForm = { formName = it.name; guideIndex = 0 },
                        onWorshipper = { worshipperName = it.name; SalahSettingsStore.saveWorshipper(prefs, it) },
                        onPrayingAs = { prayingAsName = it.name },
                        onBeginner = { beginner = it; guideIndex = 0 }
                    )
                }
                if (schoolNote != null) item(key = "guide_school") { SalahNote(schoolNote) }
                if (beginner) {
                    item(key = "beginner_step") {
                        val safeIndex = guideIndex.coerceIn(0, guideSteps.lastIndex)
                        val step = guideSteps[safeIndex]
                        val last = safeIndex == guideSteps.lastIndex
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            LinearProgressIndicator(
                                progress = { (safeIndex + 1).toFloat() / guideSteps.size },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = accent,
                                trackColor = accent.copy(alpha = 0.15f),
                                strokeCap = StrokeCap.Round,
                                drawStopIndicator = {}
                            )
                            Text(
                                SalahUi.STEP_OF.inLanguage(LocalAppLanguage.current, "n" to safeIndex + 1, "total" to guideSteps.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = accent,
                                modifier = Modifier.testTag("salah_step_count")
                            )
                            StepDetail(step, safeIndex + 1, accent, collapsed = repeatedRecitations[step.id].orEmpty())
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "previous_step", "tap"); showStep(safeIndex - 1) },
                                    enabled = safeIndex > 0,
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                ) { Text(SalahUi.PREVIOUS.control(), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                Button(
                                    onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "next_step", "tap"); showStep(if (last) 0 else safeIndex + 1) },
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("salah_next_step"),
                                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                                ) { Text(if (last) SalahUi.START_AGAIN.text() else SalahUi.NEXT.control(), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                        }
                    }
                } else {
                    items(guideSteps.size, key = { guideSteps[it].id }) { index ->
                        val step = guideSteps[index]
                        StepDetail(step, index + 1, accent, collapsed = repeatedRecitations[step.id].orEmpty())
                    }
                }
                item(key = "guide_note") { SalahNote(SalahContent.Notes.GUIDE) }
            }
            tab == SalahTab.DUAS -> {
                item(key = "dua_intro") { SalahNote(SalahContent.Notes.RECITATION) }
                RecitationGroup.entries.forEach { group ->
                    item(key = "group_${group.name}") {
                        Text(
                            group.title.text(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = accent,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(SalahContent.recitationList.filter { it.group == group }, key = { "recitation_${it.id}" }) { recitation ->
                        ToolCard { RecitationBody(recitation, accent) }
                    }
                }
            }
            tab == SalahTab.RULES -> {
                item(key = "rules_intro") { SalahNote(SalahContent.Notes.RULES) }
                topicCards(SalahContent.rules, madhhab, accent, showIcon = true) { selectedTopicId = it }
            }
            else -> {
                item(key = "qibla_action") {
                    ToolCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Default.Explore, accent, 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(SalahUi.QIBLA.control(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(SalahUi.QIBLA_HINT.text(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("salah_screen", "open_qibla", onOpenQibla), modifier = Modifier.testTag("salah_qibla")) { Text(SalahUi.OPEN.control()) }
                        }
                    }
                }
                topicCards(SalahContent.topics, madhhab, accent, showIcon = false) { selectedTopicId = it }
            }
        }
    }
}

// ---------------------------------------------------------------- Daily rak‘at

@Composable
private fun PrayerCard(prayer: DailyPrayer, madhhab: Madhhab, accent: Color, onOpen: () -> Unit) {
    AppCard(
        onClick = com.example.core.analytics.AppAnalytics.trackItemAction("salah_screen", "open_prayer", prayer.id, onOpen),
        modifier = Modifier.fillMaxWidth().testTag("salah_${prayer.id}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.Mosque, accent, 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(prayer.title.text(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    SalahUi.RAKAT_SUMMARY.inLanguage(LocalAppLanguage.current, "total" to prayer.total, "fard" to prayer.fard),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            prayer.units.forEach { unit ->
                Pill("${unit.countLabel(madhhab)} ${unit.kind.shortTitle.text()}", kindColor(unit.kind, madhhab))
            }
        }
    }
}

@Composable
private fun PrayerDetail(prayer: DailyPrayer, madhhab: Madhhab, accent: Color, onLearn: (GuideForm) -> Unit) {
    val language = LocalAppLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HeroCard(accent) {
            Text(prayer.title.text(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                SalahUi.RAKAT_SUMMARY.inLanguage(language, "total" to prayer.total, "fard" to prayer.fard),
                color = accent,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(prayer.note.text(), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "learn_fard", "tap"); GuideForm.forUnit(prayer.fard, SalahKind.FARD)?.let(onLearn) },
                modifier = Modifier.testTag("salah_learn_fard"),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) { Text(SalahUi.LEARN_FARD.inLanguage(language, "count" to prayer.fard)) }
        }
        prayer.units.forEachIndexed { index, part ->
            val fixedCount = part.countLabel(madhhab) == part.count.toString()
            ToolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        part.countLabel(madhhab),
                        style = MaterialTheme.typography.headlineMedium,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        KindBadge(part.kind, madhhab)
                        Text(part.whenPrayed.text(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    part.form?.let { target ->
                        TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "learn_prayer_unit", "tap"); onLearn(target) }, modifier = Modifier.testTag("salah_learn_unit_$index")) {
                            Text(SalahUi.LEARN_UNIT.text())
                        }
                    }
                }
                if (fixedCount) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(SalahUi.NIYYAH.text(), style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.SemiBold)
                    Text(SalahContent.niyyah(prayer, part, language), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        SalahNote(SalahContent.Notes.PRAYER_DETAIL)
    }
}

private val SalahKind.shortTitle: SalahText
    get() = when (this) {
        SalahKind.SUNNAH_MUAKKADAH, SalahKind.SUNNAH_GHAIR_MUAKKADAH -> SalahKind.SUNNAH.title
        else -> title
    }

/** Binding prayers stand out; optional ones stay quiet. */
@Composable
private fun kindColor(kind: SalahKind, madhhab: Madhhab): Color = when {
    kind == SalahKind.FARD || kind == SalahKind.FARD_KIFAYAH -> MaterialTheme.colorScheme.error
    kind == SalahKind.WAJIB || (kind == SalahKind.WITR && madhhab == Madhhab.HANAFI) -> MaterialTheme.colorScheme.tertiary
    kind == SalahKind.NAFL || kind == SalahKind.SUNNAH_GHAIR_MUAKKADAH -> MaterialTheme.colorScheme.onSurfaceVariant
    else -> SuccessGreen
}

@Composable
private fun Pill(text: String, color: Color) {
    Surface(shape = CircleShape, color = color.copy(alpha = 0.14f)) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun KindBadge(kind: SalahKind, madhhab: Madhhab) {
    val label = if (kind == SalahKind.WITR) {
        "${kind.title.text()} · ${(if (madhhab == Madhhab.HANAFI) SalahKind.WAJIB else SalahKind.SUNNAH).title.text()}"
    } else kind.title.text()
    Pill(label, kindColor(kind, madhhab))
}

// ---------------------------------------------------------------- Step-by-step guide

@Composable
private fun GuideControls(
    form: GuideForm,
    worshipper: Worshipper,
    prayingAs: PrayingAs,
    beginner: Boolean,
    onForm: (GuideForm) -> Unit,
    onWorshipper: (Worshipper) -> Unit,
    onPrayingAs: (PrayingAs) -> Unit,
    onBeginner: (Boolean) -> Unit
) {
    ToolCard {
        Text(SalahUi.GUIDE_TITLE.text(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(SalahUi.GUIDE_HINT.text(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GuideForm.entries.forEach { option ->
                FilterChip(
                    selected = form == option,
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "select_guide_form", "tap", item = option.name); onForm(option) },
                    label = { Text(option.title.text(), maxLines = 1) },
                    modifier = Modifier.testTag("salah_form_${option.name.lowercase()}")
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Worshipper.entries.forEach { option ->
                FilterChip(
                    selected = worshipper == option,
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "select_worshipper", "tap"); onWorshipper(option) },
                    label = { Text(option.title.text(), maxLines = 1) },
                    leadingIcon = { Icon(if (option == Worshipper.MAN) Icons.Default.Face else Icons.Default.Face3, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("salah_for_${option.name.lowercase()}")
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrayingAs.entries.forEach { option ->
                FilterChip(
                    selected = prayingAs == option,
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "select_praying_as", "tap", item = option.name); onPrayingAs(option) },
                    label = { Text(option.title.text(), maxLines = 1) },
                    leadingIcon = { Icon(if (option == PrayingAs.ALONE) Icons.Default.Person else Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("salah_as_${option.name.lowercase()}")
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(SalahUi.BEGINNER.text(), style = MaterialTheme.typography.titleSmall)
                Text(SalahUi.BEGINNER_HINT.text(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = beginner, onCheckedChange = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "toggle_beginner_mode", "toggle"); onBeginner(it) }, modifier = Modifier.testTag("salah_beginner_switch"))
        }
    }
}

@Composable
private fun StepDetail(step: GuideStep, number: Int, accent: Color, collapsed: Set<String>) {
    ToolCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepNumberBadge(number, accent, filled = true, size = 34.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (step.rakah != null || step.ruling != null) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        step.rakah?.let { Pill(SalahUi.RAKAH_N.inLanguage(LocalAppLanguage.current, "n" to it), accent) }
                        step.ruling?.let { Pill(it.title.text(), rulingColor(it)) }
                    }
                }
                Text(step.title.text(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Text(step.instruction.text(), style = MaterialTheme.typography.bodyLarge)
        step.points.forEach { Bullet(it.text(), accent) }
        step.recitationIds.forEach { id ->
            SalahContent.recitations[id]?.let { RecitationBlock(it, accent, startExpanded = id !in collapsed) }
        }
    }
}

@Composable
private fun rulingColor(ruling: Ruling): Color = when (ruling) {
    Ruling.CONDITION, Ruling.FARD -> MaterialTheme.colorScheme.error
    Ruling.WAJIB -> MaterialTheme.colorScheme.tertiary
    Ruling.SUNNAH, Ruling.MUSTAHAB -> SuccessGreen
}

@Composable
private fun Bullet(text: String, accent: Color, marker: String? = null) {
    Row(verticalAlignment = Alignment.Top) {
        if (marker == null) {
            Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Circle, contentDescription = null, tint = accent, modifier = Modifier.size(7.dp))
            }
        } else {
            Text(marker, color = accent, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

// ---------------------------------------------------------------- Recitations

/** A recitation inside a step or lesson; repeats can be folded away. */
@Composable
private fun RecitationBlock(recitation: Recitation, accent: Color, startExpanded: Boolean = true) {
    var expanded by rememberSaveable(recitation.id, startExpanded) { mutableStateOf(startExpanded) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "toggle_recitation", "tap"); expanded = !expanded }.heightIn(min = 48.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoStories, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(recitation.title.text(), style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text((if (expanded) SalahUi.HIDE else SalahUi.SHOW).control(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecitationBody(recitation, accent, showTitle = false)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.RecitationBody(recitation: Recitation, accent: Color, showTitle: Boolean = true) {
    if (showTitle) Text(recitation.title.text(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    recitation.note?.let { Text(it.text(), style = MaterialTheme.typography.labelMedium, color = accent) }
    Text(
        recitation.arabic,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
            fontFamily = ArabicFontFamily,
            fontSize = 26.sp,
            lineHeight = 52.sp,
            textAlign = TextAlign.Center,
            textDirection = TextDirection.Rtl
        ),
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        recitation.transliteration,
        style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
    )
    Text(recitation.meaning.text(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(recitation.source, style = MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr), color = accent, modifier = Modifier.fillMaxWidth())
}

// ---------------------------------------------------------------- Masail and special prayers

private fun LazyListScope.topicCards(
    topics: List<SalahTopic>,
    madhhab: Madhhab,
    accent: Color,
    showIcon: Boolean,
    onOpen: (String) -> Unit
) {
    items(topics, key = { it.id }) { topic ->
        AppCard(
            onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "open_topic", "tap", item = topic.id); onOpen(topic.id) },
            modifier = Modifier.fillMaxWidth().testTag("salah_topic_${topic.id}"),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (showIcon) {
                    IconBadge(ruleIcon(topic.id), accent, 40.dp)
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(topic.title.text(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    topic.kindFor(madhhab)?.let { KindBadge(it, madhhab) }
                    Text(topic.summary.text(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun ruleIcon(id: String): ImageVector = when (id) {
    "sharait" -> Icons.Default.Checklist
    "faraiz" -> Icons.Default.AccountBalance
    "wajibat" -> Icons.Default.Verified
    "sunan" -> Icons.Default.Star
    "mufsidat" -> Icons.Default.Cancel
    "makruhat" -> Icons.Default.WarningAmber
    "times" -> Icons.Default.Schedule
    "wudu", "wudu_breakers", "ghusl", "tayammum" -> Icons.Default.WaterDrop
    "women" -> Icons.Default.Face3
    "jamaat", "masbuq" -> Icons.Default.Groups
    "sahw" -> Icons.Default.Replay
    "qaza" -> Icons.Default.History
    "musafir" -> Icons.Default.Luggage
    else -> Icons.Default.Mosque
}

@Composable
private fun TopicDetail(topic: SalahTopic, madhhab: Madhhab, accent: Color, onLearn: (GuideForm) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HeroCard(accent) {
            Text(topic.title.text(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            topic.kindFor(madhhab)?.let {
                Spacer(Modifier.height(6.dp))
                KindBadge(it, madhhab)
            }
            Spacer(Modifier.height(6.dp))
            Text(topic.summary.text(), style = MaterialTheme.typography.bodyMedium)
            topic.learnForm?.let { target ->
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("salah_screen", "learn_from_topic", "tap"); onLearn(target) },
                    modifier = Modifier.testTag("salah_topic_learn"),
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) { Text(SalahUi.LEARN.text()) }
            }
        }
        ToolCard {
            topic.steps.forEachIndexed { index, line ->
                Bullet(line.text(), accent, marker = if (topic.numbered) "${index + 1}." else null)
            }
        }
        topic.footnote?.let { SalahNote(it) }
        topic.schoolNotes[madhhab]?.let { note ->
            ToolCard {
                Text(madhhab.title.text(), style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
                Text(note.text(), style = MaterialTheme.typography.bodyMedium)
            }
        }
        topic.recitationIds.forEach { id ->
            SalahContent.recitations[id]?.let { RecitationBlock(it, accent) }
        }
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "${SalahUi.REFERENCE.text()}: ${topic.source}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
        SalahNote(SalahContent.Notes.TOPIC)
    }
}

// ---------------------------------------------------------------- Shared pieces

@Composable
private fun SalahNote(text: SalahText) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp)) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(text.text(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HadithQuote(text: SalahText, source: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp)) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text.text(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(source, style = MaterialTheme.typography.labelSmall, color = accent)
            }
        }
    }
}
