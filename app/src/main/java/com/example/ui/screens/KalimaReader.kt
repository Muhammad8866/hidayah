package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppCard
import com.example.ui.components.Spacing
import com.example.ui.components.islamicPattern
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch

/**
 * Focused reader for one Kalima at a time. Swipe (or use the arrows) to move between all eight;
 * the page you settle on becomes "Continue" on the list. Audio, the reading mode and "memorised"
 * sit in a fixed bottom bar so they never scroll away.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KalimaReader(
    startId: String, user: KalimaUserState, store: KalimaStore,
    mode: KalimaReadMode, onModeChange: (KalimaReadMode) -> Unit,
    audio: KalimaAudio, audioState: KalimaAudioState,
    onOpenSettings: () -> Unit, onClose: () -> Unit
) {
    val entries = KalimasContent.entries
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val english = user.meaning == KalimaMeaning.ENGLISH
    val pager = rememberPagerState(initialPage = entries.indexOfFirst { it.id == startId }.coerceAtLeast(0)) { entries.size }
    val entry = entries[pager.settledPage.coerceIn(0, entries.lastIndex)]
    val favourite = entry.id in user.favorites
    val memorised = entry.id in user.memorized

    // The settled page is "where you stopped"; a single recitation of another Kalima stops on swipe.
    LaunchedEffect(pager.settledPage) {
        val current = entries[pager.settledPage]
        store.setLastRead(current.id)
        val playing = audio.state.value
        if (!playing.playingAll && playing.activeId != null && playing.activeId != current.id) audio.stop()
    }
    // "Play all" turns the pages along with the recitation.
    LaunchedEffect(audioState.activeId, audioState.playingAll) {
        val id = audioState.activeId ?: return@LaunchedEffect
        if (!audioState.playingAll) return@LaunchedEffect
        val index = entries.indexOfFirst { it.id == id }
        if (index >= 0 && index != pager.currentPage) pager.animateScrollToPage(index)
    }

    Column(Modifier.fillMaxSize().testTag("kalimas_reader")) {
        // Top bar: back, position, and the per-Kalima actions.
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("kalimas_reader_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizedUiText("Back"))
            }
            Column(Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.semantics { heading() })
                Text((pager.settledPage + 1).toString() + " / " + entries.size, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconToggleButton(favourite, { store.toggleFavorite(entry.id) }, Modifier.testTag("kalimas_reader_fav")) {
                Icon(if (favourite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = localizedUiText(if (favourite) "Remove from Favourites" else "Add to Favourites"),
                    tint = if (favourite) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { shareKalima(context, entry, english) }, modifier = Modifier.testTag("kalima_share_${entry.id}")) {
                Icon(Icons.Default.Share, contentDescription = localizedUiText("Share"))
            }
            IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("kalimas_reader_settings")) {
                Icon(Icons.Default.Tune, contentDescription = localizedUiText("Reading settings"))
            }
        }

        // Reading mode: whole text, phrase by phrase (tap to hear), or memorise (hidden until revealed).
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = Spacing.screen)) {
            KalimaReadMode.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = mode == option,
                    onClick = { onModeChange(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, KalimaReadMode.entries.size),
                    modifier = Modifier.testTag("kalimas_mode_${option.tag}"),
                    label = { LocalizedText(option.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        HorizontalPager(
            state = pager,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            key = { entries[it].id },
            beyondViewportPageCount = 0
        ) { page ->
            KalimaReaderPage(
                entry = entries[page], user = user, store = store, english = english, mode = mode,
                audio = audio, audioState = audioState
            )
        }

        // Fixed bottom bar: audio for this Kalima, then previous · memorised · next.
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = Spacing.screen, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                KalimaAudioControls(entry, audio)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { scope.launch { pager.animateScrollToPage(pager.settledPage - 1) } },
                        enabled = pager.settledPage > 0,
                        modifier = Modifier.testTag("kalimas_reader_prev")
                    ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = localizedUiText("Previous")) }
                    val container by animateColorAsState(
                        if (memorised) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        label = "kalima_memorised"
                    )
                    OutlinedButton(
                        onClick = { store.setMemorized(entry.id, !memorised) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("kalimas_reader_memorised"),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = container)
                    ) {
                        Icon(if (memorised) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(localizedUiText(if (memorised) "Memorised" else "Mark as memorised"),
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(
                        onClick = { scope.launch { pager.animateScrollToPage(pager.settledPage + 1) } },
                        enabled = pager.settledPage < entries.lastIndex,
                        modifier = Modifier.testTag("kalimas_reader_next")
                    ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = localizedUiText("Next")) }
                }
            }
        }
    }
}

@Composable
private fun KalimaReaderPage(
    entry: KalimaEntry, user: KalimaUserState, store: KalimaStore, english: Boolean, mode: KalimaReadMode,
    audio: KalimaAudio, audioState: KalimaAudioState
) {
    val scale = user.arabicScale.coerceIn(KalimaStore.MIN_SCALE, KalimaStore.MAX_SCALE)
    val memorised = entry.id in user.memorized
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen, vertical = Spacing.lg)
            .testTag("kalimas_reader_page_${entry.id}"),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Name of the Kalima: medallion, Arabic title and what the name means.
        Row(verticalAlignment = Alignment.CenterVertically) {
            KalimaMedallion(entry, memorised, size = 52.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                if (entry.titleArabic.isNotBlank()) {
                    Text(entry.titleArabic, style = kalimaArabicStyle(1f, base = 24), color = MaterialTheme.colorScheme.primary)
                }
                val nameMeaning = if (english) entry.nameMeaningEnglish else entry.nameMeaning
                if (nameMeaning.isNotBlank()) Text(nameMeaning, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        when {
            mode == KalimaReadMode.FULL || entry.phrases.isEmpty() -> KalimaFullText(entry, user, english, scale)
            else -> KalimaPhraseList(entry, user, english, scale, mode == KalimaReadMode.MEMORISE, memorised,
                audio, audioState, onMemorised = { store.setMemorized(entry.id, true) })
        }

        val note = if (english) entry.noteEnglish else entry.note
        if (note.isNotBlank() || entry.reference.isNotBlank()) {
            AppCard(Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                outlined = false, contentPadding = PaddingValues(14.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp).size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (note.isNotBlank()) Text(note, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                        if (entry.reference.isNotBlank()) Text(localizedUiText("Reference") + ": " + entry.reference,
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        KalimaPageActions(entry, english, user.counts[entry.id] ?: 0,
            onCount = { store.incrementCount(entry.id) }, onResetCount = { store.resetCount(entry.id) })
    }
}

@Composable
private fun KalimaFullText(entry: KalimaEntry, user: KalimaUserState, english: Boolean, scale: Float) {
    SelectionContainer {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(entry.arabic,
                    Modifier.fillMaxWidth().islamicPattern(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    style = kalimaArabicStyle(scale, base = 30), color = MaterialTheme.colorScheme.onSurface)
            }
            if (user.showRoman) {
                Text(entry.roman, style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                    fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.primary)
            }
            if (user.showMeaning) {
                Text(if (english) entry.english else entry.urdu,
                    style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun KalimaPhraseList(
    entry: KalimaEntry, user: KalimaUserState, english: Boolean, scale: Float, memorise: Boolean, memorised: Boolean,
    audio: KalimaAudio, audioState: KalimaAudioState, onMemorised: () -> Unit
) {
    val revealed = remember(entry.id, memorise) { mutableStateListOf<Int>() }
    val allRevealed = revealed.size == entry.phrases.size
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        if (memorise) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LocalizedText("Recite from memory, then tap to check.", Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = {
                    if (allRevealed) revealed.clear()
                    else { revealed.clear(); revealed.addAll(entry.phrases.indices) }
                }) { Text(localizedUiText(if (allRevealed) "Hide all" else "Reveal all")) }
            }
        }
        entry.phrases.forEachIndexed { index, phrase ->
            val active = audioState.activeId == entry.id && audioState.activePhrase == index
            val hidden = memorise && index !in revealed
            val label = localizedUiText(if (memorise) (if (hidden) "Tap to reveal" else "Hide") else "Listen")
            val container by animateColorAsState(
                if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                label = "kalima_phrase"
            )
            Surface(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                    .clickable(role = Role.Button, onClickLabel = label) {
                        if (memorise) { if (hidden) revealed.add(index) else revealed.remove(index) }
                        else if (audioState.ready) audio.playPhrase(entry, index)
                    }
                    .testTag("kalima_${entry.id}_phrase_$index"),
                shape = MaterialTheme.shapes.medium,
                color = container,
                contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            ) {
                Column(Modifier.padding(14.dp).heightIn(min = 48.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (hidden) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("${index + 1}. " + localizedUiText("Tap to reveal"), color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Text(phrase.arabic, Modifier.fillMaxWidth(), style = kalimaArabicStyle(scale))
                        if (user.showRoman && phrase.roman.isNotBlank()) {
                            Text(phrase.roman, style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
                                fontStyle = FontStyle.Italic, color = LocalContentColor.current.copy(alpha = 0.75f))
                        }
                    }
                    // In memorise mode the meaning stays visible as the cue.
                    val meaning = if (english) phrase.english else phrase.urdu
                    if ((user.showMeaning || hidden) && meaning.isNotBlank()) {
                        Text(meaning, style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr))
                    }
                }
            }
        }
        // Having checked every phrase from memory, offer to record it.
        AnimatedVisibility(memorise && allRevealed && !memorised, enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()) {
            Button(onClick = onMemorised, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("kalima_${entry.id}_memorise_done")) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(localizedUiText(if (memorised) "Memorised" else "Mark as memorised"))
            }
        }
    }
}

/** Tasbih-style counter for repeating the Kalima, plus copy. */
@Composable
private fun KalimaPageActions(entry: KalimaEntry, english: Boolean, count: Int, onCount: () -> Unit, onResetCount: () -> Unit) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val counterLabel = localizedUiText("Tasbih counter")
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FilledTonalButton(
            onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onCount() },
            modifier = Modifier.heightIn(min = 48.dp).testTag("kalima_count_${entry.id}")
                .semantics { contentDescription = "$counterLabel: $count" }
        ) {
            Text("$count", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            LocalizedText("Count", maxLines = 1)
        }
        if (count > 0) {
            IconButton(onClick = onResetCount) {
                Icon(Icons.Default.Refresh, contentDescription = localizedUiText("Reset counter"))
            }
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { copyKalima(context, entry, english) }, modifier = Modifier.testTag("kalima_copy_${entry.id}")) {
            Icon(Icons.Default.ContentCopy, contentDescription = localizedUiText("Copy"))
        }
    }
}
