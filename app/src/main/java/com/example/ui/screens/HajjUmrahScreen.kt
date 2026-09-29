@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProvenanceCard
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText

private enum class HajjTab(val label: String) { UMRAH("Umrah"), HAJJ("Hajj"), ESSENTIALS("Essentials") }

private const val KEY_TAB = "hajj_tab"
private const val KEY_TYPE = "hajj_type"
private const val KEY_PREP = "hajj_prep"
private fun doneKey(journey: String) = "hajj_done_$journey"
private fun countKey(journey: String, stepId: String) = "hajj_count_${journey}_$stepId"

@Composable
internal fun HajjUmrahScreen(onBack: () -> Unit, modifier: Modifier) {
    val prefs = rememberToolPrefs()
    var tab by rememberSaveable {
        mutableStateOf(HajjTab.entries.firstOrNull { it.name == prefs.getString(KEY_TAB, null) } ?: HajjTab.UMRAH)
    }
    var hajjType by rememberSaveable { mutableStateOf(HajjType.fromKey(prefs.getString(KEY_TYPE, null))) }

    ToolScreen(DailyTool.HAJJ_UMRAH, onBack, modifier) { accent ->
        item(key = "tabs") {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                HajjTab.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = tab == option,
                        onClick = { tab = option; prefs.edit().putString(KEY_TAB, option.name).apply() },
                        shape = SegmentedButtonDefaults.itemShape(index, HajjTab.entries.size),
                        modifier = Modifier.testTag("hajj_tab_${option.name.lowercase()}")
                    ) { LocalizedText(option.label, maxLines = 1) }
                }
            }
        }
        when (tab) {
            HajjTab.UMRAH -> {
                journeyContent(prefs, "umrah", "Your Umrah", umrahSteps, accent)
                item(key = "umrah_source") {
                    ProvenanceCard(sourceText = "Complete the Hajj and Umrah for Allah.", collectionOrReference = "Quran 2:196")
                }
            }
            HajjTab.HAJJ -> {
                item(key = "hajj_type") {
                    HajjTypePicker(hajjType, accent) {
                        hajjType = it
                        prefs.edit().putString(KEY_TYPE, it.name).apply()
                    }
                }
                journeyContent(prefs, "hajj_${hajjType.name.lowercase()}", "Your Hajj", hajjSteps(hajjType), accent)
                item(key = "hajj_source") {
                    ProvenanceCard(sourceText = "Hajj is Arafah.", collectionOrReference = "Jami' at-Tirmidhi 889")
                }
            }
            HajjTab.ESSENTIALS -> essentialsContent(prefs, accent)
        }
        item(key = "fiqh_note") {
            InfoNote("Rulings follow Hanafi fiqh. Learn the masail from a reliable Sunni scholar and follow your licensed group.")
        }
    }
}

// ---------------------------------------------------------------- Step-by-step journey

private fun LazyListScope.journeyContent(
    prefs: SharedPreferences,
    journey: String,
    heading: String,
    steps: List<RiteStep>,
    accent: Color
) {
    item(key = "journey_$journey") {
        JourneyTracker(prefs, journey, heading, steps, accent)
    }
}

@Composable
private fun JourneyTracker(
    prefs: SharedPreferences,
    journey: String,
    heading: String,
    steps: List<RiteStep>,
    accent: Color
) {
    var done by remember(journey) { mutableStateOf(prefs.getStringSet(doneKey(journey), emptySet()).orEmpty().toSet()) }
    val next = nextPendingStep(steps, done)
    var expanded by rememberSaveable(journey) { mutableStateOf(next?.id) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    fun save(updated: Set<String>) {
        done = updated
        prefs.edit().putStringSet(doneKey(journey), updated).apply()
    }

    fun toggle(step: RiteStep) {
        if (step.id in done) {
            save(done - step.id)
        } else {
            val updated = done + step.id
            save(updated)
            expanded = nextPendingStep(steps, updated)?.id
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        JourneyHero(heading, steps, done, next, accent, onReset = { confirmReset = true })
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            steps.forEachIndexed { index, step ->
                StepCard(
                    number = index + 1,
                    step = step,
                    isDone = step.id in done,
                    isNext = step.id == next?.id,
                    isExpanded = expanded == step.id,
                    accent = accent,
                    counterKey = countKey(journey, step.id),
                    prefs = prefs,
                    onToggleExpand = { expanded = if (expanded == step.id) null else step.id },
                    onToggleDone = { toggle(step) }
                )
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { LocalizedText("Start again?") },
            text = { LocalizedText("This clears the ticked steps and round counters for this guide.") },
            confirmButton = {
                TextButton(onClick = {
                    val editor = prefs.edit().remove(doneKey(journey))
                    steps.forEach { editor.remove(countKey(journey, it.id)) }
                    editor.apply()
                    done = emptySet()
                    expanded = steps.firstOrNull()?.id
                    confirmReset = false
                }) { LocalizedText("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { LocalizedText("Cancel") } }
        )
    }
}

@Composable
private fun JourneyHero(
    heading: String,
    steps: List<RiteStep>,
    done: Set<String>,
    next: RiteStep?,
    accent: Color,
    onReset: () -> Unit
) {
    val completed = steps.count { it.id in done }
    val progress by animateFloatAsState(if (steps.isEmpty()) 0f else completed.toFloat() / steps.size, label = "journey")
    HeroCard(accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LocalizedText(heading, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
                Text(
                    "⁦$completed / ${steps.size}⁩ ${localizedUiText("steps done")}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("hajj_progress")
                )
                if (next != null) {
                    Text(
                        "${localizedUiText("Next step")}: ${localizedUiText(next.title)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LocalizedText(
                        "All steps done. May Allah accept it from you!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            ProgressRing(progress, accent, 72.dp) {
                if (next == null) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(32.dp))
                } else {
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
                }
            }
        }
        LocalizedText(
            "Tap a step to see how to do it, its dua and a round counter. Tick it when done — your progress is saved on this phone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp)
        )
        if (completed > 0) {
            TextButton(onClick = onReset, contentPadding = PaddingValues(horizontal = 0.dp)) {
                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText("Start again")
            }
        }
    }
}

@Composable
private fun StepCard(
    number: Int,
    step: RiteStep,
    isDone: Boolean,
    isNext: Boolean,
    isExpanded: Boolean,
    accent: Color,
    counterKey: String,
    prefs: SharedPreferences,
    onToggleExpand: () -> Unit,
    onToggleDone: () -> Unit
) {
    val borderColor by animateColorAsState(
        when {
            isDone -> SuccessGreen.copy(alpha = 0.55f)
            isNext -> accent
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        label = "stepBorder"
    )
    val chevron by animateFloatAsState(if (isExpanded) 180f else 0f, label = "chevron")
    Card(
        modifier = Modifier.fillMaxWidth().testTag("hajj_step_${step.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) SuccessGreen.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(if (isNext) 1.5.dp else 1.dp, borderColor)
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onToggleExpand)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepBadge(number, isDone, accent)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        step.badge?.let { Chip(it, accent) }
                        step.ruling?.let { RulingChip(it) }
                    }
                    LocalizedText(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (!isExpanded) {
                        LocalizedText(
                            step.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = localizedUiText(if (isExpanded) "Collapse" else "Expand"),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevron)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LocalizedText(step.detail, style = MaterialTheme.typography.bodyMedium)
                    // The counter sits first so it stays in reach while walking a Tawaf or Sa'i.
                    step.counter?.let { kind ->
                        RoundCounter(kind, counterKey, prefs, accent, canMarkDone = !isDone, onMarkDone = onToggleDone)
                    }
                    if (step.points.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            step.points.forEach { GuideBullet(GuideLine(it, GuideTone.INFO), accent) }
                        }
                    }
                    step.dua?.let { DuaBlock(it, accent) }
                    if (isDone) {
                        OutlinedButton(onClick = onToggleDone, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            LocalizedText("Mark as not done")
                        }
                    } else {
                        Button(
                            onClick = onToggleDone,
                            modifier = Modifier.fillMaxWidth().testTag("hajj_done_${step.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            LocalizedText("Mark step as done")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepBadge(number: Int, isDone: Boolean, accent: Color) {
    val color = if (isDone) SuccessGreen else accent
    Surface(
        shape = CircleShape,
        color = if (isDone) SuccessGreen else accent.copy(alpha = 0.15f),
        border = BorderStroke(1.5.dp, color),
        modifier = Modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = localizedUiText("Done"), tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("$number", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = accent)
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Surface(shape = CircleShape, color = color.copy(alpha = 0.14f)) {
        LocalizedText(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun RulingChip(ruling: RiteRuling) {
    val color = when (ruling) {
        RiteRuling.SHART, RiteRuling.FARD -> MaterialTheme.colorScheme.error
        RiteRuling.WAJIB -> Color(0xFFB7791F)
        RiteRuling.SUNNAH -> SuccessGreen
    }
    Chip(ruling.label, color)
}

@Composable
private fun DuaBlock(dua: RiteDua, accent: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoStories, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText("Recite", style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
            }
            Text(
                dua.arabic,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontSize = 22.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    textDirection = TextDirection.Rtl
                ),
                color = MaterialTheme.colorScheme.primary
            )
            LocalizedText(dua.meaning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(dua.reference, style = MaterialTheme.typography.labelSmall, color = accent)
        }
    }
}

// ---------------------------------------------------------------- Round counter

@Composable
private fun RoundCounter(
    kind: CounterKind,
    storageKey: String,
    prefs: SharedPreferences,
    accent: Color,
    canMarkDone: Boolean,
    onMarkDone: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var count by remember(storageKey) { mutableIntStateOf(prefs.getInt(storageKey, 0).coerceIn(0, kind.total)) }
    fun update(value: Int) {
        count = value.coerceIn(0, kind.total)
        prefs.edit().putInt(storageKey, count).apply()
    }
    val stage = counterStage(kind, count)
    val finished = count >= kind.total
    val progress by animateFloatAsState(count.toFloat() / kind.total, label = "counter")

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LocalizedText(
                if (kind == CounterKind.TAWAF || kind == CounterKind.SAI) "Round counter" else "Pebble counter",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            ProgressRing(progress, if (finished) SuccessGreen else accent, 116.dp, stroke = 10.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$count",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (finished) SuccessGreen else accent,
                        modifier = Modifier.testTag("hajj_counter_value")
                    )
                    Text(
                        "/ ${kind.total}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (kind == CounterKind.RAMI_THREE && !finished) {
                LinearProgressIndicator(
                    progress = { stage.subCount.toFloat() / stage.subTotal },
                    modifier = Modifier.fillMaxWidth(0.6f).height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {}
                )
            }
            if (!finished && kind.total == 7) {
                Text(
                    "${localizedUiText(kind.unit)} ${count + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LocalizedText(
                stage.hint,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (finished) SuccessGreen else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("hajj_counter_hint")
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { update(count - 1) }, enabled = count > 0, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = localizedUiText("Undo"))
                }
                if (finished && canMarkDone) {
                    Button(
                        onClick = onMarkDone,
                        modifier = Modifier.height(52.dp).widthIn(min = 180.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        LocalizedText("Mark step as done")
                    }
                } else {
                    Button(
                        onClick = {
                            update(count + 1)
                            haptics.performHapticFeedback(
                                if (count >= kind.total) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                            )
                        },
                        enabled = !finished,
                        modifier = Modifier.height(52.dp).widthIn(min = 180.dp).testTag("hajj_counter_add"),
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        LocalizedText(kind.countLabel)
                    }
                }
                FilledTonalIconButton(onClick = { update(0) }, enabled = count > 0, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Default.Replay, contentDescription = localizedUiText("Reset"))
                }
            }
            LocalizedText(
                "Saved automatically, even if you close the app.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------- Hajj type

@Composable
private fun HajjTypePicker(selected: HajjType, accent: Color, onSelect: (HajjType) -> Unit) {
    ToolCard {
        SectionTitle("Type of Hajj")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            HajjType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, HajjType.entries.size),
                    modifier = Modifier.testTag("hajj_type_${type.name.lowercase()}")
                ) { LocalizedText(type.label, maxLines = 1) }
            }
        }
        LocalizedText(selected.summary, style = MaterialTheme.typography.bodyMedium)
        LocalizedText(
            "Most pilgrims from the subcontinent perform Tamattu'. In Hanafi fiqh Qiran is the most virtuous, then Tamattu', then Ifrad. Each type keeps its own progress.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------- Essentials

private fun LazyListScope.essentialsContent(prefs: SharedPreferences, accent: Color) {
    item(key = "prep") { PreparationCard(prefs, accent) }
    items(essentialSections, key = { it.id }) { section -> GuideSectionCard(section, accent) }
    item(key = "madinah") { MadinahCard(accent) }
}

@Composable
private fun CollapsibleCard(
    icon: ImageVector,
    title: String,
    summary: String,
    accent: Color,
    initiallyExpanded: Boolean = false,
    testTag: String,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by rememberSaveable(testTag) { mutableStateOf(initiallyExpanded) }
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, label = "sectionChevron")
    ToolCard(contentPadding = PaddingValues(0.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { expanded = !expanded }
                .padding(16.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon, accent, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LocalizedText(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = localizedUiText(if (expanded) "Collapse" else "Expand"),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(chevron)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        }
    }
}

@Composable
private fun PreparationCard(prefs: SharedPreferences, accent: Color) {
    var ready by remember { mutableStateOf(prefs.getStringSet(KEY_PREP, emptySet()).orEmpty().toSet()) }
    val total = preparationChecklist.sumOf { it.items.size }
    val readyCount = preparationChecklist.sumOf { group -> group.items.count { it.id in ready } }
    CollapsibleCard(
        icon = Icons.Default.Checklist,
        title = "Preparation checklist",
        summary = "⁦$readyCount / $total⁩ ${localizedUiText("ready")}",
        accent = accent,
        initiallyExpanded = true,
        testTag = "hajj_prep"
    ) {
        LinearProgressIndicator(
            progress = { readyCount.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = accent,
            trackColor = accent.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {}
        )
        preparationChecklist.forEach { group ->
            LocalizedText(group.title, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
            group.items.forEach { item ->
                val checked = item.id in ready
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) {
                            ready = if (checked) ready - item.id else ready + item.id
                            prefs.edit().putStringSet(KEY_PREP, ready).apply()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(8.dp))
                    LocalizedText(
                        item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideSectionCard(section: GuideSection, accent: Color) {
    val icon = when (section.id) {
        "pillars" -> Icons.Default.AccountBalance
        "ihram_rules" -> Icons.Default.Block
        "women" -> Icons.Default.Face3
        else -> Icons.Default.WarningAmber
    }
    CollapsibleCard(icon, section.title, section.summary, accent, testTag = "hajj_section_${section.id}") {
        section.lines.forEach { GuideBullet(it, accent) }
        Text(section.reference, style = MaterialTheme.typography.labelSmall, color = accent)
    }
}

@Composable
private fun GuideBullet(line: GuideLine, accent: Color) {
    val (icon, tint) = when (line.tone) {
        GuideTone.DO -> Icons.Default.CheckCircle to SuccessGreen
        GuideTone.DONT -> Icons.Default.Cancel to MaterialTheme.colorScheme.error
        GuideTone.INFO -> Icons.Default.Circle to accent
    }
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(if (line.tone == GuideTone.INFO) 7.dp else 18.dp))
        }
        Spacer(Modifier.width(10.dp))
        LocalizedText(line.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MadinahCard(accent: Color) {
    CollapsibleCard(
        icon = Icons.Default.Mosque,
        title = "Ziyarat of Madinah",
        summary = "Presenting salam to the Prophet ﷺ",
        accent = accent,
        testTag = "hajj_section_madinah"
    ) {
        LocalizedText(
            "Presenting yourself at the blessed Rauda of the Prophet ﷺ is among the greatest blessings of the journey. Go with adab, lower your voice and send abundant durood.",
            style = MaterialTheme.typography.bodyMedium
        )
        DuaBlock(
            RiteDua(
                MADINAH_SALAM_ARABIC,
                "Blessings and peace be upon you, O Messenger of Allah.",
                "Quran 49:2–3 · Muwatta Imam Malik (Ibn Umar RA) · Bahar-e-Shariat, Hissa 6"
            ),
            accent
        )
        madinahPlaces.forEach { place ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LocalizedText(place.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LocalizedText(place.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(place.reference, style = MaterialTheme.typography.labelSmall, color = accent)
            }
        }
    }
}
