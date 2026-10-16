package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppSearchField
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.components.StateMessage
import com.example.ui.components.TonalPill
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText

/** List filters. The two groups (6 Kalime, Iman) are shown as sections rather than filters. */
internal enum class KalimaFilter(val label: String, val tag: String) {
    ALL("All", "all"), FAVOURITES("Favourites", "favourites"), NOT_MEMORISED("Not memorised", "not_memorised")
}

internal enum class KalimaReadMode(val label: String, val tag: String) {
    FULL("Full text", "full"), PHRASES("Phrase by phrase", "phrases"), MEMORISE("Memorise", "memorise")
}

/** Section names, kept as values so they share the existing translations of the old filter chips. */
private val KalimaSectionTitles = mapOf(KalimaGroup.KALIMA to "6 Kalime", KalimaGroup.IMAN to "Iman")

/**
 * 6 Kalime & Iman: a calm list (progress, search, sections) that opens a focused reader for one
 * Kalima at a time. The reader swipes between all entries and remembers where you stopped.
 */
@Composable
fun KalimasScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("kalimas_screen")
    val context = LocalContext.current
    val store = remember(context) { KalimaStore(context.applicationContext) }
    val user by store.state.collectAsState()
    val audio = rememberKalimaAudio()
    val audioState by audio.state.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(KalimaFilter.ALL) }
    var mode by rememberSaveable { mutableStateOf(KalimaReadMode.FULL) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var quizOpen by rememberSaveable { mutableStateOf(false) }
    var readingId by rememberSaveable { mutableStateOf<String?>(null) }

    val english = user.meaning == KalimaMeaning.ENGLISH
    val openReader = { id: String -> store.setLastRead(id); readingId = id }

    AnimatedContent(
        targetState = readingId,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
        label = "kalimas_reader",
        modifier = modifier.fillMaxSize()
    ) { openId ->
        if (openId != null) {
            BackHandler { readingId = null }
            KalimaReader(
                startId = openId, user = user, store = store, mode = mode, onModeChange = { mode = it },
                audio = audio, audioState = audioState,
                onOpenSettings = { settingsOpen = true },
                onClose = { readingId = null }
            )
        } else {
            KalimaHub(
                user = user, store = store, audio = audio, audioState = audioState,
                query = query, onQueryChange = { query = it },
                filter = filter, onFilterChange = { filter = it },
                onOpen = openReader,
                onOpenSettings = { settingsOpen = true },
                onQuiz = { quizOpen = true },
                onBack = onBack
            )
        }
    }

    if (settingsOpen) KalimaSettingsSheet(user, store, onDismiss = { settingsOpen = false })
    if (quizOpen) KalimaQuizDialog(english = english, onDismiss = { quizOpen = false })
}

@Composable
private fun KalimaHub(
    user: KalimaUserState, store: KalimaStore, audio: KalimaAudio, audioState: KalimaAudioState,
    query: String, onQueryChange: (String) -> Unit,
    filter: KalimaFilter, onFilterChange: (KalimaFilter) -> Unit,
    onOpen: (String) -> Unit, onOpenSettings: () -> Unit, onQuiz: () -> Unit, onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val english = user.meaning == KalimaMeaning.ENGLISH
    var sourceExpanded by rememberSaveable { mutableStateOf(false) }
    val shown = remember(query, filter, user.favorites, user.memorized) {
        KalimasContent.search(query).filter {
            when (filter) {
                KalimaFilter.FAVOURITES -> it.id in user.favorites
                KalimaFilter.NOT_MEMORISED -> it.id !in user.memorized
                KalimaFilter.ALL -> true
            }
        }
    }
    val total = KalimasContent.entries.size
    val memorisedCount = KalimasContent.entries.count { it.id in user.memorized }
    val resume = user.lastReadId?.let(KalimasContent::byId)
    // Next one to learn: the first entry not yet memorised, in teaching order.
    val nextToLearn = KalimasContent.entries.firstOrNull { it.id !in user.memorized }

    Box(Modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            Modifier.fillMaxSize().testTag("kalimas_screen"),
            state = listState,
            contentPadding = PaddingValues(start = Spacing.screen, top = Spacing.sm, end = Spacing.screen, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(key = "header") {
                ScreenHeader("6 Kalime & Iman", subtitle = "Iman-e-Mujmal aur Iman-e-Mufassal", onBack = onBack) {
                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("kalimas_settings")) {
                        Icon(Icons.Default.Tune, contentDescription = localizedUiText("Reading settings"))
                    }
                }
            }
            item(key = "progress") {
                KalimaProgressCard(
                    memorised = memorisedCount, total = total,
                    resume = resume ?: nextToLearn ?: KalimasContent.entries.first(),
                    resumeIsContinue = resume != null,
                    onResume = { (resume ?: nextToLearn ?: KalimasContent.entries.first()).let { onOpen(it.id) } },
                    canPlayAll = audioState.ready,
                    playingAll = audioState.playingAll,
                    onPlayAll = { if (audioState.playingAll) audio.stop() else audio.playAll(KalimasContent.entries) },
                    onQuiz = onQuiz
                )
            }
            item(key = "search") {
                AppSearchField(query, onQueryChange, "Search", Modifier.testTag("kalimas_search"))
            }
            item(key = "filters") {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).testTag("kalimas_filters"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KalimaFilter.entries.forEach { option ->
                        val count = when (option) {
                            KalimaFilter.ALL -> null
                            KalimaFilter.FAVOURITES -> user.favorites.size
                            KalimaFilter.NOT_MEMORISED -> total - memorisedCount
                        }
                        FilterChip(
                            selected = filter == option, onClick = { onFilterChange(option) },
                            label = { Text(localizedUiText(option.label) + (count?.let { " · $it" } ?: "")) },
                            leadingIcon = if (filter == option) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null,
                            shape = CircleShape,
                            modifier = Modifier.heightIn(min = 40.dp).testTag("kalimas_filter_${option.tag}")
                        )
                    }
                }
            }
            if (shown.isEmpty()) item(key = "empty") {
                if (query.isNotBlank()) {
                    StateMessage(icon = Icons.Default.SearchOff, title = "No results found",
                        actionText = "Clear search", onAction = { onQueryChange("") })
                } else {
                    StateMessage(icon = Icons.Default.StarBorder, title = "Nothing here yet",
                        message = if (filter == KalimaFilter.FAVOURITES) "Tap the star on a card to add it to Favourites." else null,
                        actionText = "Show all", onAction = { onFilterChange(KalimaFilter.ALL) })
                }
            }
            KalimaGroup.entries.forEach { group ->
                val inGroup = shown.filter { it.group == group }
                if (inGroup.isNotEmpty()) {
                    item(key = "section_${group.name}") {
                        KalimaSectionTitle(KalimaSectionTitles.getValue(group),
                            inGroup.count { it.id in user.memorized }, inGroup.size)
                    }
                    items(inGroup, key = { it.id }) { entry ->
                        KalimaCard(
                            entry = entry, english = english,
                            favourite = entry.id in user.favorites,
                            memorised = entry.id in user.memorized,
                            playing = audioState.activeId == entry.id,
                            isLastRead = entry.id == user.lastReadId,
                            onOpen = { onOpen(entry.id) },
                            onFavourite = { store.toggleFavorite(entry.id) },
                            onMemorised = { store.setMemorized(entry.id, entry.id !in user.memorized) }
                        )
                    }
                }
            }
            item(key = "footer") {
                KalimaAboutCard(english, sourceExpanded) { sourceExpanded = !sourceExpanded }
            }
        }
        KalimaNowPlayingBar(audio, KalimasContent.entries, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun KalimaSectionTitle(title: String, memorised: Int, total: Int) {
    Row(Modifier.fillMaxWidth().padding(top = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        LocalizedText(
            title, Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
        )
        TonalPill("$memorised/$total")
    }
}
