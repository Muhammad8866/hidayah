@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import com.example.ui.components.IconBadge
import com.example.ui.components.islamicPattern
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizedUiText
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import kotlinx.coroutines.launch

private val TARGET_PRESETS = listOf(33L, 99L, 100L, 1000L)
private val STEP_PRESETS = listOf(1L, 5L, 10L, 100L)

private enum class TallyHaptic { TAP, ROUND, GOAL }

@Composable
internal fun TallyCounterScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("tally_counter_screen")
    val prefs = rememberToolPrefs()
    val store = remember(prefs) { TallyCounterStore(prefs) }
    var collection by remember(store) { mutableStateOf(store.load()) }
    var daily by remember(store) { mutableStateOf(store.loadDaily()) }
    val active = collection.active
    val locale = LocalAppLocale.current
    val language = LocalAppLanguage.current
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val view = LocalView.current
    var vibration by remember { mutableStateOf(prefs.getBoolean("tally_vibration", true)) }
    var keepAwake by remember { mutableStateOf(prefs.getBoolean("tally_keep_awake", false)) }
    var volumeKeys by remember { mutableStateOf(prefs.getBoolean("tally_volume_keys", false)) }
    var focus by rememberSaveable { mutableStateOf(false) }
    var showCounters by rememberSaveable { mutableStateOf(false) }
    var editId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var resetId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val defaultName = localizedUiText("My counter")
    fun name(counter: TallyCounter) = counter.name.ifBlank { defaultName }
    fun fmt(value: Long) = numberFormat.format(value)
    fun tr(template: String, vararg values: Pair<String, String>) = localizeUiTemplate(template, language, values.toMap())

    // Always derive the next state from the latest in-memory collection, never from a captured snapshot,
    // so rapid taps or a tap racing a dialog can't overwrite each other.
    fun save(next: TallyCollection) { collection = next; store.save(next, daily) }
    fun buzz(kind: TallyHaptic) {
        if (!vibration) return
        val constant = when (kind) {
            TallyHaptic.TAP -> HapticFeedbackConstants.KEYBOARD_TAP
            TallyHaptic.ROUND -> HapticFeedbackConstants.LONG_PRESS
            TallyHaptic.GOAL -> if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
    }
    fun change(transform: (TallyCounter) -> TallyCounter) {
        val before = collection.active
        val after = transform(before)
        if (after == before) return
        val day = tallyToday()
        val delta = after.todayOn(day) - before.todayOn(day)
        daily = TallyCounterStore.recordDaily(daily, day, delta)
        save(collection.update(after))
        val haptic = when {
            after.count <= before.count -> TallyHaptic.TAP
            after.goal > 0 && before.count < after.goal && after.count >= after.goal -> TallyHaptic.GOAL
            after.target > 0 && after.completedRounds > before.completedRounds -> TallyHaptic.ROUND
            else -> TallyHaptic.TAP
        }
        buzz(haptic)
        if (haptic == TallyHaptic.GOAL) scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(localizeUiTemplate("{name}: goal reached. May Allah accept it.", language,
                mapOf("name" to name(after))), duration = SnackbarDuration.Short)
        }
    }
    fun increment() = change { it.increment() }

    DisposableEffect(view, keepAwake) {
        val previous = view.keepScreenOn
        if (keepAwake) view.keepScreenOn = true
        onDispose { view.keepScreenOn = previous }
    }
    // Flush synchronously when the app goes to the background so a process kill can't lose the last taps.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, store) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) store.save(collection, daily, sync = true)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer); store.save(collection, daily, sync = true) }
    }
    val keyFocus = remember { FocusRequester() }
    val dialogOpen = showCounters || creating || editId != null || resetId != null || deleteId != null
    LaunchedEffect(volumeKeys, dialogOpen) {
        if (volumeKeys && !dialogOpen) runCatching { keyFocus.requestFocus() }
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp
    val dialMax = minOf(320, (screenHeight * 0.62f).toInt()).coerceAtLeast(180).dp

    Box(
        modifier.fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (!volumeKeys || dialogOpen) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.VolumeUp -> { if (event.type == KeyEventType.KeyDown) increment(); true }
                    Key.VolumeDown -> { if (event.type == KeyEventType.KeyDown) change { it.decrement() }; true }
                    else -> false
                }
            }
            .focusRequester(keyFocus).focusable()
    ) {
        ToolScreen(DailyTool.TALLY_COUNTER, onBack, Modifier) { accent ->
            item(key = "selection") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!focus) {
                        OutlinedButton(
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "counters_sheet"); showCounters = true },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("tally_select"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Layers, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(name(active), Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Badge(containerColor = accent.copy(alpha = 0.16f), contentColor = accent) {
                                Text(fmt(collection.counters.size.toLong()))
                            }
                            Icon(Icons.Default.KeyboardArrowDown, localizedUiText("Your counters"))
                        }
                    } else {
                        Text(name(active), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    FilledTonalIconButton(
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "focus_mode"); focus = !focus }, modifier = Modifier.size(52.dp).testTag("tally_focus")
                    ) {
                        Icon(if (focus) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            localizedUiText(if (focus) "Exit focus mode" else "Focus mode"))
                    }
                }
            }
            item(key = "counter") {
                val tapLabel = localizedUiText("Tap to count")
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
                ) {
                    Column(
                        Modifier.fillMaxWidth()
                            // The whole card is a tap target; buttons inside handle their own clicks.
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null,
                                role = Role.Button, onClickLabel = tapLabel) { increment() }
                            .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.12f), Color.Transparent)))
                            .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            LocalizedText("Total count", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!focus) {
                                IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "edit_counter"); editId = active.id }, modifier = Modifier.testTag("tally_edit")) {
                                    Icon(Icons.Default.Tune, localizedUiText("Edit counter"), tint = accent)
                                }
                            }
                        }
                        TallyDial(active, numberFormat, accent, dialMax, tapLabel, onTap = ::increment,
                            countDescription = tr("Count {count}", "count" to fmt(active.count)))
                        if (active.target > 0) {
                            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.10f)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    val status = when {
                                        active.goalReached -> localizedUiText("Goal reached")
                                        active.rounds > 0 -> tr("Round {current} of {total}",
                                            "current" to fmt(minOf(active.completedRounds + 1, active.rounds.toLong())),
                                            "total" to fmt(active.rounds.toLong()))
                                        active.count >= active.target -> localizedUiText("Target reached")
                                        else -> localizedUiText("Target progress")
                                    }
                                    Text(status, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
                                    Text("${fmt(active.inRound)} / ${fmt(active.target)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
                                        modifier = Modifier.testTag("tally_target"))
                                    if (active.completedRounds > 0) {
                                        Text(tr("{count} rounds completed", "count" to fmt(active.completedRounds)),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.testTag("tally_rounds"))
                                    }
                                }
                            }
                        } else if (!focus) {
                            TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "edit_counter"); editId = active.id }, modifier = Modifier.testTag("tally_add_target")) {
                                Icon(Icons.Default.Flag, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                LocalizedText("Set a target")
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "decrement"); change { it.decrement() } }, enabled = active.count > 0,
                                modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("tally_decrement")
                                    .semantics { contentDescription = tr("Subtract {step}", "step" to fmt(active.step)) },
                                shape = RoundedCornerShape(16.dp)
                            ) { Text("−${fmt(active.step)}", style = MaterialTheme.typography.labelLarge.copy(textDirection = TextDirection.Ltr)) }
                            FilledTonalButton(
                                onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "undo"); change { it.undo() } }, enabled = active.previousCounts.isNotEmpty(),
                                modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("tally_undo"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Undo, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp)); LocalizedText("Undo")
                            }
                        }
                        TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "reset"); resetId = active.id }, enabled = active.count > 0,
                            modifier = Modifier.testTag("tally_reset")) {
                            Icon(Icons.Default.RestartAlt, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp)); LocalizedText("Reset count")
                        }
                    }
                }
            }
            if (!focus) {
                item(key = "target") {
                    ToolCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                LocalizedText("Target per round", style = MaterialTheme.typography.titleSmall)
                                if (active.target > 0) Text(fmt(active.target), color = accent, style = MaterialTheme.typography.labelLarge)
                                else LocalizedText("Unlimited", color = accent, style = MaterialTheme.typography.labelLarge)
                            }
                            TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "edit_counter"); editId = active.id }) { LocalizedText("Custom") }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = active.target == 0L,
                                onClick = { save(collection.update(active.copy(target = 0, rounds = 0))) },
                                label = { Icon(Icons.Default.AllInclusive, localizedUiText("Unlimited"), Modifier.size(18.dp)) },
                                modifier = Modifier.testTag("tally_target_off"))
                            TARGET_PRESETS.forEach { preset ->
                                FilterChip(selected = active.target == preset,
                                    onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "target_preset", item = "t$preset"); save(collection.update(active.copy(target = preset))) },
                                    label = { Text(fmt(preset), textAlign = TextAlign.Center, maxLines = 1,
                                        modifier = Modifier.fillMaxWidth()) },
                                    modifier = Modifier.weight(1f).testTag("tally_target_$preset"))
                            }
                        }
                        if (active.target > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LocalizedText("Rounds", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { save(collection.update(active.copy(rounds = (active.rounds - 1).coerceAtLeast(0)))) },
                                    enabled = active.rounds > 0, modifier = Modifier.testTag("tally_rounds_minus")) {
                                    Icon(Icons.Default.Remove, localizedUiText("Fewer rounds"))
                                }
                                if (active.rounds > 0) Text(fmt(active.rounds.toLong()), style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.widthIn(min = 32.dp), textAlign = TextAlign.Center)
                                else LocalizedText("Open", style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { save(collection.update(active.copy(rounds = (active.rounds + 1).coerceAtMost(TALLY_MAX_ROUNDS)))) },
                                    modifier = Modifier.testTag("tally_rounds_plus")) {
                                    Icon(Icons.Default.Add, localizedUiText("More rounds"))
                                }
                            }
                        }
                    }
                }
                item(key = "steps") {
                    ToolCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                LocalizedText("Count per tap", style = MaterialTheme.typography.titleSmall)
                                Text("+${fmt(active.step)}", color = accent, style = MaterialTheme.typography.labelLarge.copy(textDirection = TextDirection.Ltr))
                            }
                            TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "edit_counter"); editId = active.id }) { LocalizedText("Custom") }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            STEP_PRESETS.forEach { step ->
                                FilterChip(selected = active.step == step,
                                    onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "step_preset", item = "s$step"); save(collection.update(active.copy(step = step))) },
                                    label = { Text("+${fmt(step)}", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge.copy(textDirection = TextDirection.Ltr), modifier = Modifier.fillMaxWidth()) },
                                    modifier = Modifier.weight(1f).testTag("tally_step_$step"))
                            }
                        }
                    }
                }
                item(key = "activity") {
                    TallyActivity(collection, daily, numberFormat, accent, locale)
                }
                item(key = "preferences") {
                    ToolCard {
                        TallySwitch("Tap vibration", Icons.Default.Vibration, vibration, "tally_vibration") {
                            vibration = it; prefs.edit().putBoolean("tally_vibration", it).apply()
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        TallySwitch("Keep screen awake", Icons.Default.LightMode, keepAwake, "tally_keep_awake") {
                            keepAwake = it; prefs.edit().putBoolean("tally_keep_awake", it).apply()
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        TallySwitch("Count with volume buttons", Icons.Default.VolumeUp, volumeKeys, "tally_volume_keys",
                            hint = "Volume up adds, volume down subtracts while this screen is open") {
                            volumeKeys = it; prefs.edit().putBoolean("tally_volume_keys", it).apply()
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
    }

    if (showCounters) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = { showCounters = false }) {
            val today = tallyToday()
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 520.dp).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LocalizedText("Your counters", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = {
                            scope.launch { sheetState.hide(); showCounters = false; creating = true }
                        }, modifier = Modifier.testTag("tally_new")) {
                            Icon(Icons.Default.Add, null); LocalizedText("New")
                        }
                    }
                }
                items(collection.counters, key = { it.id }) { counter ->
                    val selected = counter.id == active.id
                    Surface(onClick = { com.example.core.analytics.AppAnalytics.interaction("tally_counter_screen", "select_counter"); save(collection.select(counter.id)); showCounters = false },
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth().testTag("tally_counter_${counter.id}")) {
                        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(
                                icon = if (selected) Icons.Default.Check else Icons.Default.Layers,
                                accent = MaterialTheme.colorScheme.primary,
                                size = 36.dp, iconSize = 20.dp,
                                filled = selected
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(name(counter), fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                val detail = if (counter.target > 0) "${fmt(counter.count)} · ${fmt(counter.inRound)}/${fmt(counter.target)}"
                                    else fmt(counter.count)
                                Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr))
                                val todayCount = counter.todayOn(today)
                                if (todayCount > 0) Text(tr("Today: {count}", "count" to fmt(todayCount)),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                scope.launch { sheetState.hide(); showCounters = false; editId = counter.id }
                            }) {
                                Icon(Icons.Default.Edit, localizedUiText("Edit counter"))
                            }
                        }
                    }
                }
            }
        }
    }
    val editing = collection.counters.firstOrNull { it.id == editId }
    if (creating || editing != null) {
        TallyEditor(counter = editing, displayName = editing?.let(::name).orEmpty(),
            canDelete = editing != null && collection.counters.size > 1,
            onDismiss = { creating = false; editId = null },
            onSave = { counter ->
                save(if (creating) collection.add(counter) else collection.update(counter))
                creating = false; editId = null
            },
            onDelete = { deleteId = editing?.id; editId = null })
    }
    val resetting = collection.counters.firstOrNull { it.id == resetId }
    val deleting = collection.counters.firstOrNull { it.id == deleteId }
    if (resetting != null || deleting != null) {
        val isDelete = deleting != null
        AlertDialog(onDismissRequest = { resetId = null; deleteId = null },
            icon = { Icon(if (isDelete) Icons.Default.DeleteOutline else Icons.Default.RestartAlt, null) },
            title = { LocalizedText(if (isDelete) "Delete counter?" else "Reset count?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val target = deleting ?: resetting!!
                    Text(name(target), fontWeight = FontWeight.SemiBold)
                    Text(fmt(target.count), style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary)
                    LocalizedText(if (isDelete) "This counter and its saved count will be deleted." else "Your count will return to zero. You can undo this reset.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val undoLabel = localizeUiTemplate("Undo", language, emptyMap())
                    if (deleting != null) {
                        val before = collection
                        save(collection.delete(deleting.id))
                        scope.launch {
                            snackbar.currentSnackbarData?.dismiss()
                            val result = snackbar.showSnackbar(tr("Deleted {name}", "name" to name(deleting)),
                                actionLabel = undoLabel, duration = SnackbarDuration.Long)
                            if (result == SnackbarResult.ActionPerformed) save(before)
                        }
                    } else if (resetting != null) {
                        save(collection.update(resetting.reset()))
                        scope.launch {
                            snackbar.currentSnackbarData?.dismiss()
                            val result = snackbar.showSnackbar(tr("{name} reset to zero", "name" to name(resetting)),
                                actionLabel = undoLabel, duration = SnackbarDuration.Long)
                            if (result == SnackbarResult.ActionPerformed) {
                                val current = collection.counters.firstOrNull { it.id == resetting.id }
                                if (current != null && current.count == 0L) save(collection.update(current.undo()))
                            }
                        }
                    }
                    resetId = null; deleteId = null
                }, modifier = Modifier.testTag("tally_confirm"),
                    colors = if (isDelete) ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    else ButtonDefaults.textButtonColors()) { LocalizedText(if (isDelete) "Delete" else "Reset") }
            },
            dismissButton = { TextButton(onClick = { resetId = null; deleteId = null }) { LocalizedText("Cancel") } })
    }
}

@Composable
private fun TallyDial(
    counter: TallyCounter, format: NumberFormat, accent: Color, maxSize: androidx.compose.ui.unit.Dp,
    tapLabel: String, countDescription: String, onTap: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, spring(dampingRatio = 0.6f), label = "tally_press")
    val progress = if (counter.target > 0) (counter.inRound.toDouble() / counter.target).coerceIn(0.0, 1.0).toFloat() else 0f
    val animatedProgress by animateFloatAsState(progress, label = "tally_progress")
    Box(
        Modifier.widthIn(max = maxSize).fillMaxWidth().aspectRatio(1f)
            .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.12f), Color.Transparent)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (counter.target > 0) {
            CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxSize()
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) },
                color = accent, trackColor = accent.copy(alpha = 0.12f), strokeWidth = 10.dp, strokeCap = StrokeCap.Round,
                gapSize = 0.dp)
        } else {
            Box(Modifier.fillMaxSize().padding(3.dp).border(2.dp, accent.copy(alpha = 0.16f), CircleShape))
        }
        Surface(onClick = onTap, interactionSource = interaction, shape = CircleShape,
            color = accent, contentColor = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp, modifier = Modifier.fillMaxSize().padding(18.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }.testTag("tally_increment")
                .semantics { contentDescription = tapLabel }) {
            Column(Modifier.fillMaxSize()
                .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)))
                .islamicPattern(Color.White.copy(alpha = 0.07f), cell = 34.dp)
                .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(format.format(counter.count),
                    modifier = Modifier.fillMaxWidth().testTag("tally_count")
                        .semantics { liveRegion = LiveRegionMode.Polite; contentDescription = countDescription },
                    style = (when { counter.count < 10_000 -> MaterialTheme.typography.displayLarge
                        counter.count < 10_000_000 -> MaterialTheme.typography.displaySmall
                        counter.count < 1_000_000_000_000 -> MaterialTheme.typography.headlineMedium
                        else -> MaterialTheme.typography.titleMedium }).copy(textDirection = TextDirection.Ltr),
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                Spacer(Modifier.height(10.dp))
                Surface(color = Color.White.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.surface, shape = CircleShape) {
                    Text("+${format.format(counter.step)}", Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.Ltr), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                LocalizedText("Tap to count", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun TallyActivity(
    collection: TallyCollection, daily: Map<Long, Long>, format: NumberFormat, accent: Color, locale: java.util.Locale
) {
    val today = tallyToday()
    val days = (6 downTo 0).map { today - it }
    val values = days.map { daily[it] ?: 0L }
    val peak = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    ToolCard {
        LocalizedText("Activity", style = MaterialTheme.typography.titleSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TallyStat("Today", format.format(maxOf(daily[today] ?: 0L, collection.todayTotal(today))), accent, Modifier.weight(1f))
            TallyStat("This counter today", format.format(collection.active.todayOn(today)), accent, Modifier.weight(1f))
            TallyStat("All time", format.format(collection.active.lifetime), accent, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().height(84.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom) {
            days.forEachIndexed { index, day ->
                val value = values[index]
                val label = LocalDate.ofEpochDay(day).dayOfWeek.getDisplayName(TextStyle.NARROW, locale)
                Column(Modifier.weight(1f).fillMaxHeight()
                    .semantics(mergeDescendants = true) { contentDescription = "$label ${format.format(value)}" },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Box(Modifier.fillMaxWidth().fillMaxHeight((value.toFloat() / peak * 0.8f).coerceAtLeast(0.04f))
                        .background(if (day == today) accent else accent.copy(alpha = 0.35f), RoundedCornerShape(6.dp)))
                    Spacer(Modifier.height(4.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall,
                        color = if (day == today) accent else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TallyStat(title: String, value: String, accent: Color, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.08f)) {
        Column(Modifier.padding(10.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            LocalizedText(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TallySwitch(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
                        checked: Boolean, tag: String, hint: String? = null, onChange: (Boolean) -> Unit) {
    Row(Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            LocalizedText(title, style = MaterialTheme.typography.bodyMedium)
            if (hint != null) LocalizedText(hint, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.testTag(tag))
    }
}

@Composable
private fun TallyEditor(counter: TallyCounter?, displayName: String, canDelete: Boolean,
                        onDismiss: () -> Unit, onSave: (TallyCounter) -> Unit, onDelete: () -> Unit) {
    var name by rememberSaveable(counter?.id) { mutableStateOf(displayName) }
    var count by rememberSaveable(counter?.id) { mutableStateOf((counter?.count ?: 0).toString()) }
    var target by rememberSaveable(counter?.id) { mutableStateOf(counter?.target?.takeIf { it > 0 }?.toString().orEmpty()) }
    var rounds by rememberSaveable(counter?.id) { mutableStateOf(counter?.rounds?.takeIf { it > 0 }?.toString().orEmpty()) }
    var step by rememberSaveable(counter?.id) { mutableStateOf((counter?.step ?: 1).toString()) }
    fun digits(input: String) = input.filter(Char::isDigit).take(18)
    val parsedCount = count.toLongOrNull()?.takeIf { it >= 0 }
    val parsedTarget = if (target.isBlank()) 0L else target.toLongOrNull()?.takeIf { it > 0 }
    val parsedRounds = if (rounds.isBlank()) 0 else rounds.toIntOrNull()?.takeIf { it in 1..TALLY_MAX_ROUNDS }
    val parsedStep = step.toLongOrNull()?.takeIf { it in 1..TALLY_MAX_STEP }
    val valid = name.isNotBlank() && name.length <= TALLY_MAX_NAME && parsedCount != null && parsedTarget != null &&
        parsedStep != null && parsedRounds != null
    AlertDialog(onDismissRequest = onDismiss,
        title = { LocalizedText(if (counter == null) "New counter" else "Edit counter") },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it.take(TALLY_MAX_NAME) }, label = { LocalizedText("Counter name") },
                    singleLine = true, isError = name.isBlank(),
                    modifier = Modifier.fillMaxWidth().testTag("tally_name_input"))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("SubhanAllah", "Alhamdulillah", "Allahu Akbar", "Astaghfirullah").forEach { suggestion ->
                        val localizedSuggestion = localizedUiText(suggestion)
                        if (counter == null) SuggestionChip(onClick = { name = localizedSuggestion },
                            label = { Text(localizedSuggestion, maxLines = 1, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                OutlinedTextField(count, { count = digits(it) }, label = { LocalizedText("Current count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                    isError = parsedCount == null, modifier = Modifier.fillMaxWidth().testTag("tally_count_input"))
                OutlinedTextField(target, { target = digits(it) }, label = { LocalizedText("Target (optional)") },
                    supportingText = { LocalizedText("Leave empty for unlimited counting") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                    isError = parsedTarget == null, modifier = Modifier.fillMaxWidth().testTag("tally_target_input"))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TARGET_PRESETS.forEach { preset ->
                        FilterChip(selected = target == preset.toString(), onClick = { target = preset.toString() },
                            label = { Text(preset.toString()) })
                    }
                }
                if (parsedTarget != null && parsedTarget > 0) {
                    OutlinedTextField(rounds, { rounds = digits(it).take(5) }, label = { LocalizedText("Rounds (optional)") },
                        supportingText = { LocalizedText("Leave empty to keep counting rounds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                        isError = parsedRounds == null, modifier = Modifier.fillMaxWidth().testTag("tally_rounds_input"))
                }
                OutlinedTextField(step, { step = digits(it) }, label = { LocalizedText("Count per tap") },
                    supportingText = { Text("1 – 1,000,000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                    isError = parsedStep == null, modifier = Modifier.fillMaxWidth().testTag("tally_step_input"))
                if (canDelete) TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); LocalizedText("Delete counter")
                }
            }
        },
        confirmButton = { TextButton(onClick = {
            if (valid) onSave((counter ?: TallyCounter()).setCount(parsedCount!!)
                .copy(name = name.trim(), target = parsedTarget!!, step = parsedStep!!,
                    rounds = if (parsedTarget > 0) parsedRounds!! else 0))
        }, enabled = valid, modifier = Modifier.testTag("tally_save")) { LocalizedText("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } })
}
