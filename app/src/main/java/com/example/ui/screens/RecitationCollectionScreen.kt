package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.search.AppSearch
import com.example.domain.model.Ayah
import com.example.domain.model.Dua
import com.example.domain.model.DuaDatasetRepository
import com.example.quran.HifzQuranTextRepository
import com.example.ui.components.AppCard
import com.example.ui.tour.TourScreen
import com.example.ui.tour.tourTarget
import com.example.ui.components.AppSearchField
import com.example.ui.components.HeroSurface
import com.example.ui.components.IconBadge
import com.example.ui.components.NeedsInternetChip
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.components.StateMessage
import com.example.ui.components.TonalPill
import com.example.ui.components.accentTint
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiText
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** One of the three collections: its name, look and the line that explains it. */
private data class RecitationTab(val title: String, val icon: ImageVector, val accent: Color, val blurb: String)

private val recitationTabs = listOf(
    RecitationTab("Manzil", Icons.Default.Shield, Color(0xFF2E8B6E),
        "Traditional Manzil collection of Quranic passages. Read in the order shown."),
    RecitationTab("Ruqyah", Icons.Default.VolunteerActivism, Color(0xFF3B6FA8),
        "Quranic passages and duas for seeking Allah's protection and healing."),
    RecitationTab("Wazaif", Icons.Default.AutoAwesome, Color(0xFFA9812A),
        "Daily adhkar and duas with their existing source references and recitation counts.")
)

// Labels that already have reviewed translations; kept as constants so they are looked up, not re-registered.
private const val MORE_LABEL = "Show more"
private const val EXPAND_LABEL = "Expand"
private const val COLLAPSE_LABEL = "Collapse"

private fun tabPassagesOf(tab: Int) = when (tab) {
    0 -> RecitationCollection.manzil
    1 -> RecitationCollection.ruqyah
    else -> emptyList()
}

private fun tabDuasOf(tab: Int) = when (tab) {
    1 -> RecitationCollection.ruqyahDuas
    2 -> RecitationCollection.wazaif
    else -> emptyList()
}

/** Every item of a collection with how many recitations complete it. */
private fun tabEntriesOf(tab: Int): List<Pair<String, Int>> =
    tabPassagesOf(tab).map { it.id to 1 } + tabDuasOf(tab).map { it.id to it.recommendedCount.coerceAtLeast(1) }

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecitationCollectionScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("recitation_collection_screen")
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val haptics = LocalHapticFeedback.current
    val store = remember(context) { RecitationStore(context) }
    val stored by store.state.collectAsState()
    val settings = stored.settings
    val speech = rememberDuaSpeech()
    val audio = remember(context) { RecitationAudio(context) }
    val playback by audio.state.collectAsState()
    val speechState by speech.state.collectAsState()
    val recorded = remember(context) { RecordedDuaAudio(context) }
    LaunchedEffect(speechState.activeId) { if (speechState.activeId != null) { audio.stop(); recorded.stop() } }
    LaunchedEffect(playback.id) { if (playback.id != null) recorded.stop() }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(audio, recorded, store, owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) { audio.stop(); recorded.stop() }
            if (event == Lifecycle.Event.ON_RESUME) store.refreshDay()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); audio.close(); recorded.close() }
    }
    val view = LocalView.current
    DisposableEffect(view, settings.keepScreenOn) {
        view.keepScreenOn = settings.keepScreenOn
        onDispose { view.keepScreenOn = false }
    }
    val (arabicSize, arabicLine) = recitationFontSize(settings.fontStep)
    fun countFor(id: String, target: Int) = (stored.counts[id] ?: store.count(id, target)).coerceIn(0, target)
    var tab by rememberSaveable { mutableIntStateOf(store.state.value.lastTab.coerceIn(0, recitationTabs.lastIndex)) }
    // Hearing a passage through to its last ayah counts as reading it.
    DisposableEffect(audio, store, tab) {
        audio.onPassageFinished = { id -> store.setCount(id, 1, 1); store.markRead(tab, id) }
        onDispose { audio.onPassageFinished = null }
    }
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("recitation_collection_screen", query)
    var savedOnly by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    // Completed items fold into a slim row; these are the ones the reader opened again.
    var reopened by rememberSaveable { mutableStateOf(setOf<String>()) }
    val listState = rememberLazyListState()
    com.example.core.analytics.AnalyticsScroll("recitation_collection_screen", listState)
    val scope = rememberCoroutineScope()
    var verses by remember { mutableStateOf<Map<Int, List<Ayah>>>(emptyMap()) }
    var error by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(retry) {
        error = false
        try {
            verses = (RecitationCollection.manzil + RecitationCollection.ruqyah).map { it.surah }.distinct().associateWith {
                HifzQuranTextRepository.loadSurah(context, it).also { rows -> require(rows.isNotEmpty()) }
            }
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { error = true }
    }
    LaunchedEffect(tab) { audio.stop(); speech.stop(); recorded.stop() }
    LaunchedEffect(query, savedOnly, category) { audio.stop(); speech.stop(); recorded.stop() }
    fun versesOf(passage: RecitationPassage) =
        verses[passage.surah].orEmpty().filter { it.ayahNumber in passage.first..passage.last }
    val tabPassages = tabPassagesOf(tab)
    // Each collection is made searchable once (again when its verses load or the language changes),
    // so typing only searches. Results come best match first; saved and category filters narrow them.
    val passageSearch = remember(tab, verses, language) {
        AppSearch.Ranker(tabPassagesOf(tab)) { passage ->
            recitationPassageFields(passage, versesOf(passage).map(Ayah::arabicText), localizeUiText(passage.title, language))
        }
    }
    val duaSearch = remember(tab) { AppSearch.Ranker(tabDuasOf(tab)) { DuaDatasetRepository.searchFields(it) } }
    fun kept(id: String) = !savedOnly || id in stored.favorites
    val passages = passageSearch.search(query).filter { kept(it.id) }
    val duas = duaSearch.search(query).filter {
        (tab != 2 || category == null || it.category == category) && kept(it.id)
    }
    val tabEntries = tabEntriesOf(tab)
    val completed = tabEntries.count { countFor(it.first, it.second) == it.second }
    val listed = passages.map { it.id to 1 } + duas.map { it.id to it.recommendedCount.coerceAtLeast(1) }
    val versesLoaded = verses.isNotEmpty()
    val ready = tab == 2 || versesLoaded
    val showPlayer = tab != 2 && versesLoaded && passages.isNotEmpty()
    val showLoading = tab != 2 && !versesLoaded
    val showEmpty = passages.isEmpty() && duas.isEmpty() && ready
    // Items laid out before the first card; keeps scroll targets right when rows come and go.
    val headerCount = 4 + listOf(showPlayer, showLoading, showEmpty).count { it }
    val resumeIndex = recitationResumeIndex(listed.map { it.first }, stored.lastRead[tab]) { id ->
        val target = listed.first { it.first == id }.second
        countFor(id, target) >= target
    }
    LaunchedEffect(tab, completed == tabEntries.size) {
        if (tabEntries.isNotEmpty() && completed == tabEntries.size) store.recordCompletion(tab)
    }
    LaunchedEffect(playback.id) {
        val index = passages.indexOfFirst { it.id == playback.id }
        if (index >= 0 && listState.layoutInfo.visibleItemsInfo.none { it.index == headerCount + index })
            listState.animateScrollToItem(headerCount + index)
    }
    fun setCount(id: String, count: Int, target: Int) {
        if (settings.haptics && count > 0) haptics.performHapticFeedback(
            if (count >= target) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
        store.setCount(id, count, target)
        if (count > 0) store.markRead(tab, id)
        if (count < target) reopened = reopened - id
    }
    fun clearFilters() { query = ""; savedOnly = false; category = null }
    fun selectTab(index: Int) {
        if (index == tab) return
        tab = index; store.setTab(index); clearFilters(); reopened = emptySet()
        scope.launch { listState.scrollToItem(0) }
    }
    val accent = recitationTabs[tab].accent

    LazyColumn(modifier.fillMaxSize().imePadding().testTag("recitation_collection_screen"),
        state = listState,
        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = Spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        item(key = "header") {
            ScreenHeader("Manzil / Ruqyah / Wazaif", subtitle = "Quranic recitations & daily remembrance", onBack = onBack) {
                IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "open_settings"); showSettings = true }, modifier = Modifier.testTag("collection_settings").tourTarget("recitation_settings")) {
                    Icon(Icons.Default.Tune, contentDescription = localizeUiText("Reading options", language))
                }
            }
        }
        item(key = "collections") {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).tourTarget("recitation_collections"), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                recitationTabs.forEachIndexed { index, info ->
                    val entries = remember(index) { tabEntriesOf(index) }
                    val done = entries.count { countFor(it.first, it.second) == it.second }
                    CollectionTile(info, selected = tab == index, done = done, total = entries.size,
                        modifier = Modifier.weight(1f).fillMaxHeight().testTag("collection_tab_$index")) { selectTab(index) }
                }
            }
        }
        item(key = "hero") {
            CollectionHero(
                info = recitationTabs[tab], completed = completed, total = tabEntries.size, streak = stored.streak,
                canContinue = resumeIndex >= 0 && ready,
                canReset = tabEntries.any { countFor(it.first, it.second) > 0 },
                onContinue = { scope.launch { listState.animateScrollToItem(headerCount + resumeIndex) } },
                onReset = { confirmReset = true }
            )
        }
        item(key = "tools") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AppSearchField(query, { query = it }, "Search collection", Modifier.testTag("collection_search").tourTarget("recitation_search"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    item {
                        FilterChip(savedOnly, { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "favourites_filter", "toggle"); savedOnly = !savedOnly }, label = { LocalizedText("Favourites") },
                            leadingIcon = { Icon(if (savedOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, Modifier.size(16.dp)) },
                            shape = CircleShape, modifier = Modifier.testTag("collection_favourites_filter").tourTarget("recitation_favourites_filter"))
                    }
                    item {
                        FilterChip(settings.fontStep > 0, {
                            com.example.core.analytics.AppAnalytics.setting("recitation_large_text", (settings.fontStep == 0).toString())
                            store.updateSettings { it.copy(fontStep = if (it.fontStep > 0) 0 else 2) }
                        }, label = { LocalizedText("Large text") }, shape = CircleShape,
                            modifier = Modifier.tourTarget("recitation_large_text"))
                    }
                    if (tab != 0) item {
                        FilterChip(settings.showMeaning, {
                            com.example.core.analytics.AppAnalytics.setting("recitation_show_meaning", (!settings.showMeaning).toString())
                            store.updateSettings { it.copy(showMeaning = !it.showMeaning) }
                        }, label = { LocalizedText("Translation") }, shape = CircleShape,
                            modifier = Modifier.tourTarget("recitation_translation_chip"))
                    }
                }
                if (tab == 2) LazyRow(Modifier.tourTarget("recitation_categories"), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    item {
                        FilterChip(category == null, { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "category", "select", item = "all"); category = null }, label = { LocalizedText("All") },
                            leadingIcon = if (category == null) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null,
                            shape = CircleShape)
                    }
                    items(RecitationCollection.wazaifCategories, key = { it }) { name ->
                        FilterChip(category == name, { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "category", "select", item = name); category = if (category == name) null else name },
                            label = { LocalizedText(name) },
                            leadingIcon = if (category == name) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null,
                            shape = CircleShape, modifier = Modifier.testTag("collection_category_$name"))
                    }
                }
            }
        }
        if (showPlayer) stickyHeader(key = "player") {
            Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
                RecitationPlayerBar(audio, passages, Modifier.fillMaxWidth().padding(vertical = 4.dp).tourTarget("recitation_player_bar"))
            }
        }
        if (showLoading) item(key = "loading") {
            if (error) StateMessage(icon = Icons.Default.SearchOff, title = "Unable to load recitations", actionText = "Retry", onAction = { retry++ })
            else Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator()
                LocalizedText("Loading recitations…", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (showEmpty) item(key = "empty") {
            StateMessage(icon = Icons.Default.SearchOff, title = "No dua found.",
                message = if (savedOnly && query.isBlank()) "Tap the heart on any card to save it here." else null,
                actionText = "Clear filters", onAction = { clearFilters() })
        }
        items(if (versesLoaded) passages else emptyList(), key = { it.id }) { passage ->
            val selectedVerses = versesOf(passage)
            val source = "Quran ${passage.reference}"
            val arabic = selectedVerses.joinToString(" ") { "${it.arabicText} (${it.ayahNumber})" }
            val active = playback.id == passage.id
            val count = countFor(passage.id, 1)
            val done = count >= 1
            val shareText = { recitationShareText(localizeUiText(passage.title, language), source, arabic, "", "") }
            if (done && !active && passage.id !in reopened) {
                CompletedRow(passage.id, tabPassages.indexOf(passage) + 1, passage.title, passage.reference, accent,
                    Modifier.animateItem()) { reopened = reopened + passage.id }
            } else AppCard(
                Modifier.fillMaxWidth().animateItem().testTag("recitation_${passage.id}"),
                contentPadding = PaddingValues(0.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(Modifier.padding(Spacing.lg).animateContentSize(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    CardHeader(passage.id, tabPassages.indexOf(passage) + 1, passage.title, passage.reference, accent,
                        saved = passage.id in stored.favorites, onSave = { store.toggleFavorite(passage.id) },
                        count = count, onReset = { setCount(passage.id, 0, 1) }, shareText = shareText,
                        onCollapse = if (done) ({ reopened = reopened - passage.id }) else null)
                    MushafText(selectedVerses, playingAyah = if (active) playback.ayah else null,
                        fontSp = arabicSize, lineSp = arabicLine) { ayah ->
                        speech.stop(); audio.playFrom(passage, ayah); store.markRead(tab, passage.id)
                    }
                    if (active && playback.failed) Row(verticalAlignment = Alignment.CenterVertically) {
                        LocalizedText("Audio could not play. Try again.", color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        TextButton(onClick = { speech.stop(); audio.play(passage) }) { LocalizedText("Retry") }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        OutlinedButton(onClick = { speech.stop(); audio.play(passage); store.markRead(tab, passage.id) },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("collection_audio_${passage.id}").tourTarget("recitation_listen"),
                            contentPadding = PaddingValues(horizontal = 16.dp)) {
                            Icon(if (active && playback.playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            LocalizedText(if (active && playback.playing) "Pause" else "Listen", maxLines = 1)
                        }
                        // Only passages without a bundled copy stream; the chip appears for those while offline.
                        if (recitationPlaybackUris(passage).any { it.startsWith("http") }) Box(Modifier.tourTarget("recitation_needs_internet")) { NeedsInternetChip() }
                        Spacer(Modifier.weight(1f))
                        MarkReadButton(passage.id, count, 1, accent) { setCount(passage.id, it, 1) }
                    }
                }
            }
        }
        items(duas, key = { it.id }) { dua ->
            val target = dua.recommendedCount.coerceAtLeast(1)
            val count = countFor(dua.id, target)
            val done = count >= target
            if (done && dua.id !in reopened) {
                CompletedRow(dua.id, null, dua.displayTitle(language), dua.sourceReference, accent, Modifier.animateItem()) {
                    reopened = reopened + dua.id
                }
            } else DuaReadingCard(
                dua = dua, target = target, count = count, accent = accent, language = language,
                showMeaning = settings.showMeaning, showTransliteration = settings.showTransliteration,
                fontSp = arabicSize, lineSp = arabicLine, saved = dua.id in stored.favorites,
                onSave = { store.toggleFavorite(dua.id) }, onCount = { setCount(dua.id, it, target) },
                onCollapse = if (done) ({ reopened = reopened - dua.id }) else null,
                modifier = Modifier.animateItem(),
                audioContent = {
                    if (ManzilRuqyahAudioCatalog.hasDuaRecording(dua.id)) RecordedDuaControls(dua.id, recorded) {
                        speech.stop(); audio.stop(); store.markRead(tab, dua.id)
                    } else DuaAudioControls(dua.id, dua.arabicText, speech, Modifier.tourTarget("recitation_dua_voice"))
                }
            )
        }
    }

    if (confirmReset) AlertDialog(onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "dismiss_reset_all", "dismiss"); confirmReset = false },
        icon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
        title = { LocalizedText("Reset progress for this tab?") },
        text = { LocalizedText("This clears the counts of every item in this tab.") },
        confirmButton = {
            TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "confirm_reset_all"); store.resetAll(tabEntries.map { it.first }); reopened = emptySet(); confirmReset = false },
                modifier = Modifier.testTag("collection_reset_confirm")) {
                LocalizedText("Reset all", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "cancel_reset_all"); confirmReset = false }) { LocalizedText("Cancel") } })

    if (showSettings) ModalBottomSheet(onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "dismiss_settings", "dismiss"); showSettings = false }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            TourScreen("recitation_settings")
            LocalizedText("Reading options", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.sm).semantics { heading() })
            SheetSection("Text size")
            Row(Modifier.tourTarget("recitation_settings_text_size"), verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { com.example.core.analytics.AppAnalytics.setting("recitation_font_step", "smaller"); store.updateSettings { it.copy(fontStep = (it.fontStep - 1).coerceIn(0, 3)) } },
                    enabled = settings.fontStep > 0) {
                    Icon(Icons.Default.Remove, contentDescription = localizeUiText("Smaller text", language))
                }
                // Four dots show the step at a glance.
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                    repeat(4) { step ->
                        Box(Modifier.padding(horizontal = 4.dp).size(if (step == settings.fontStep) 12.dp else 8.dp).clip(CircleShape)
                            .background(if (step <= settings.fontStep) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant))
                    }
                }
                FilledTonalIconButton(onClick = { com.example.core.analytics.AppAnalytics.setting("recitation_font_step", "larger"); store.updateSettings { it.copy(fontStep = (it.fontStep + 1).coerceIn(0, 3)) } },
                    enabled = settings.fontStep < 3) {
                    Icon(Icons.Default.Add, contentDescription = localizeUiText("Larger text", language))
                }
            }
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()) {
                Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    fontFamily = ArabicFontFamily, textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = arabicSize.sp, lineHeight = arabicLine.sp, textDirection = TextDirection.Rtl))
            }
            SheetSection("Display")
            SettingSwitch("Translation", settings.showMeaning, tourKey = "recitation_settings_translation") { on -> store.updateSettings { it.copy(showMeaning = on) } }
            SettingSwitch("Transliteration", settings.showTransliteration, tourKey = "recitation_settings_transliteration") { on -> store.updateSettings { it.copy(showTransliteration = on) } }
            SettingSwitch("Keep screen on", settings.keepScreenOn, tourKey = "recitation_settings_screen_on") { on -> store.updateSettings { it.copy(keepScreenOn = on) } }
            HorizontalDivider(Modifier.padding(vertical = Spacing.xs), color = MaterialTheme.colorScheme.outlineVariant)
            SettingSwitch("Daily reset", settings.dailyReset, "Counts start fresh every day", tourKey = "recitation_settings_daily_reset") { on -> store.updateSettings { it.copy(dailyReset = on) } }
            SettingSwitch("Vibration on count", settings.haptics, tourKey = "recitation_settings_vibration") { on -> store.updateSettings { it.copy(haptics = on) } }
        }
    }
}

/** One of the three collection switches: icon, name and today's progress, tinted when selected. */
@Composable
private fun CollectionTile(info: RecitationTab, selected: Boolean, done: Int, total: Int, modifier: Modifier, onClick: () -> Unit) {
    val tint = accentTint(info.accent)
    val finished = total > 0 && done == total
    Surface(
        modifier = modifier.clip(MaterialTheme.shapes.large)
            .selectable(selected = selected, role = Role.Tab,
                onClick = com.example.core.analytics.AppAnalytics.trackItemAction("recitation_collection_screen", "tab", info.title, onClick)),
        shape = MaterialTheme.shapes.large,
        color = if (selected) tint.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp,
            if (selected) tint else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            IconBadge(if (finished) Icons.Default.CheckCircle else info.icon, info.accent, size = 40.dp, iconSize = 22.dp, filled = selected)
            LocalizedText(info.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                color = if (selected) tint else MaterialTheme.colorScheme.onSurface)
            Text(done.toString() + "/" + total, style = MaterialTheme.typography.labelMedium, maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else done.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = tint, trackColor = tint.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round, gapSize = 0.dp, drawStopIndicator = {}
            )
        }
    }
}

/** The collection's summary: what it is, how far today has got, the streak and the one main action. */
@Composable
private fun CollectionHero(info: RecitationTab, completed: Int, total: Int, streak: Int, canContinue: Boolean,
                           canReset: Boolean, onContinue: () -> Unit, onReset: () -> Unit) {
    val allDone = total > 0 && completed == total
    HeroSurface(Modifier.fillMaxWidth().testTag("collection_progress").tourTarget("recitation_progress")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LocalizedText(info.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = Color.White, modifier = Modifier.weight(1f).semantics { heading() })
            if (streak > 0) Box(Modifier.tourTarget("recitation_streak")) { TonalPill("$streak  " + localizeUiText("Day streak", LocalAppLanguage.current),
                icon = Icons.Default.LocalFireDepartment,
                containerColor = GoldAccent.copy(alpha = 0.22f), contentColor = GoldAccent) }
        }
        Spacer(Modifier.height(4.dp))
        LocalizedText(info.blurb, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.78f))
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(completed.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(" / " + total, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 3.dp))
            Spacer(Modifier.width(8.dp))
            LocalizedText("Completed", style = MaterialTheme.typography.labelLarge, color = GoldAccent,
                modifier = Modifier.padding(bottom = 4.dp))
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else completed.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = GoldAccent, trackColor = Color.White.copy(alpha = 0.18f),
            strokeCap = StrokeCap.Round, gapSize = 0.dp, drawStopIndicator = {}
        )
        Spacer(Modifier.height(14.dp))
        if (allDone) LocalizedText("All done for today. May Allah accept it.", style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold, color = GoldAccent)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (canContinue) Button(onClick = com.example.core.analytics.AppAnalytics.trackAction("recitation_collection_screen", "continue_reading", onContinue), modifier = Modifier.heightIn(min = 48.dp).tourTarget("recitation_continue"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF0F382C))) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                LocalizedText("Continue reading", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.weight(1f))
            if (canReset) TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("recitation_collection_screen", "open_reset_all", onReset), modifier = Modifier.testTag("collection_reset_all").tourTarget("recitation_reset_all"),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.85f))) {
                Icon(Icons.Default.RestartAlt, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                LocalizedText("Reset all")
            }
        }
    }
}

/** A finished item folded to one line; tapping it opens the full card again. */
@Composable
private fun CompletedRow(id: String, number: Int?, title: String, reference: String, accent: Color,
                         modifier: Modifier = Modifier, onExpand: () -> Unit) {
    val language = LocalAppLanguage.current
    val tint = accentTint(accent)
    Surface(
        modifier = modifier.fillMaxWidth().testTag("recitation_$id").tourTarget("recitation_completed_row").clip(MaterialTheme.shapes.large)
            .clickable(onClickLabel = localizeUiText(EXPAND_LABEL, language), role = Role.Button,
                onClick = { com.example.core.analytics.AppAnalytics.content("recitation_collection", id); onExpand() }),
        shape = MaterialTheme.shapes.large,
        color = tint.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.22f))
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp).heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(tint), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(number?.toString(), reference.takeIf { it.isNotBlank() }).joinToString("  •  "),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            // The count control stays addressable while folded and reads as finished.
            Box(Modifier.testTag("collection_count_$id").semantics { disabled() }) {
                TonalPill(localizeUiText("Completed", language), containerColor = tint.copy(alpha = 0.16f), contentColor = tint)
            }
            Icon(Icons.Default.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp))
        }
    }
}

/** Number, title, reference, favourite and the overflow menu (copy, share, reset) of a reading card. */
@Composable
private fun CardHeader(id: String, number: Int?, title: String, reference: String, accent: Color, saved: Boolean,
                       onSave: () -> Unit, count: Int, onReset: () -> Unit, shareText: () -> String,
                       onCollapse: (() -> Unit)?) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val tint = accentTint(accent)
    var menu by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (number != null) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center) {
                Text("$number", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = tint)
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            LocalizedText(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (reference.isNotBlank()) Text(reference, style = MaterialTheme.typography.labelMedium, color = tint,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (onCollapse != null) IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("recitation_collection_screen", "collapse", onCollapse), modifier = Modifier.tourTarget("recitation_collapse")) {
            Icon(Icons.Default.CheckCircle, contentDescription = localizeUiText(COLLAPSE_LABEL, language), tint = tint)
        }
        IconToggleButton(saved, { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "favourite", "toggle", item = id); onSave() }, Modifier.testTag("collection_favourite_$id").tourTarget("recitation_favourite")) {
            Icon(if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = localizeUiText(if (saved) "Remove from favourites" else "Add to favourites", language),
                tint = if (saved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box {
            IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "open_item_menu"); menu = true }, modifier = Modifier.tourTarget("recitation_item_menu")) {
                Icon(Icons.Default.MoreVert, contentDescription = localizeUiText(MORE_LABEL, language))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "dismiss_item_menu", "dismiss"); menu = false }) {
                DropdownMenuItem(text = { LocalizedText("Copy") }, leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                    modifier = Modifier.testTag("collection_copy_$id"), onClick = {
                        com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "copy", item = id);
                        menu = false
                        context.getSystemService(ClipboardManager::class.java)
                            ?.setPrimaryClip(ClipData.newPlainText(title, shareText()))
                        // Android 13+ shows its own confirmation.
                        if (Build.VERSION.SDK_INT < 33) Toast.makeText(context, localizeUiText("Copied", language), Toast.LENGTH_SHORT).show()
                    })
                DropdownMenuItem(text = { LocalizedText("Share") }, leadingIcon = { Icon(Icons.Default.Share, null) }, onClick = {
                    com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "share", item = id);
                    menu = false
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, com.example.core.share.AppShareLink.withLink(context, shareText()))
                    runCatching { context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                })
                DropdownMenuItem(text = { LocalizedText("Reset") }, leadingIcon = { Icon(Icons.Default.RestartAlt, null) },
                    enabled = count > 0, onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "reset_item", item = id); menu = false; onReset() })
            }
        }
    }
}

/**
 * The passage as one continuous mushaf-style paragraph with ornate ayah numbers. Tapping an ayah plays
 * from it; the ayah being recited is highlighted.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MushafText(verses: List<Ayah>, playingAyah: Int?, fontSp: Int, lineSp: Int, onPlayAyah: (Int) -> Unit) {
    val highlight = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    val markColor = accentTint(Color(0xFFA9812A))
    val ranges = HashMap<Int, IntRange>()
    val text = buildAnnotatedString {
        verses.forEachIndexed { index, verse ->
            ranges[verse.ayahNumber] = length until length + verse.arabicText.length
            withLink(LinkAnnotation.Clickable("ayah_${verse.ayahNumber}",
                linkInteractionListener = LinkInteractionListener { onPlayAyah(verse.ayahNumber) })) {
                withStyle(if (verse.ayahNumber == playingAyah) SpanStyle(background = highlight) else SpanStyle()) {
                    append(verse.arabicText)
                }
            }
            append(" ")
            withStyle(SpanStyle(color = markColor, fontSize = (fontSp * 0.8f).sp)) { append(recitationAyahMark(verse.ayahNumber)) }
            if (index < verses.lastIndex) append(" ")
        }
    }
    // Long passages run past the screen: keep the lines of the ayah being recited in view.
    val requester = remember { BringIntoViewRequester() }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    LaunchedEffect(playingAyah, layout) {
        val result = layout ?: return@LaunchedEffect
        val range = ranges[playingAyah ?: return@LaunchedEffect] ?: return@LaunchedEffect
        if (range.isEmpty() || range.last >= result.layoutInput.text.length) return@LaunchedEffect
        val top = result.getLineTop(result.getLineForOffset(range.first))
        val bottom = result.getLineBottom(result.getLineForOffset(range.last))
        requester.bringIntoView(Rect(0f, top, result.size.width.toFloat(), bottom))
    }
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
        modifier = Modifier.fillMaxWidth().tourTarget("recitation_mushaf_text")) {
        Text(text, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).bringIntoViewRequester(requester),
            onTextLayout = { layout = it },
            color = MaterialTheme.colorScheme.onSurface, fontFamily = ArabicFontFamily,
            style = TextStyle(fontSize = fontSp.sp, lineHeight = lineSp.sp, textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Justify))
    }
}

/** Single-read items get "Mark as read"; counted adhkar get a tap counter with a ring. */
@Composable
private fun MarkReadButton(id: String, count: Int, target: Int, accent: Color, onCount: (Int) -> Unit) {
    val done = count >= target
    val tint = accentTint(accent)
    Button(onClick = {
        if (count < target) {
            if (count + 1 == target) com.example.core.analytics.AppAnalytics.milestone("recitation_collection", "target_reached", id)
            onCount(count + 1)
        }
    }, enabled = !done,
        modifier = Modifier.heightIn(min = 48.dp).testTag("collection_count_$id").tourTarget("recitation_mark_read"),
        colors = ButtonDefaults.buttonColors(containerColor = tint, contentColor = MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 16.dp)) {
        Icon(if (done) Icons.Default.CheckCircle else Icons.Default.Check, null, Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        LocalizedText(if (done) "Completed" else "Mark as read", maxLines = 1)
    }
}

/** A dua or dhikr: Arabic, transliteration, meaning, its audio and a counter toward its recommended count. */
@Composable
private fun DuaReadingCard(dua: Dua, target: Int, count: Int, accent: Color, language: String, showMeaning: Boolean,
                           showTransliteration: Boolean, fontSp: Int, lineSp: Int, saved: Boolean, onSave: () -> Unit,
                           onCount: (Int) -> Unit, onCollapse: (() -> Unit)?, modifier: Modifier = Modifier,
                           audioContent: @Composable () -> Unit) {
    val done = count >= target
    val tint = accentTint(accent)
    val source = "${dua.sourceCollection} • ${dua.sourceReference}"
    val meaning = if (!showMeaning) "" else dua.displayMeaning(language).text
    val transliteration = if (showTransliteration) dua.transliteration else ""
    val shareText = { recitationShareText(dua.displayTitle(language), source, dua.arabicText, transliteration, meaning) }
    AppCard(modifier.fillMaxWidth().testTag("recitation_${dua.id}"), contentPadding = PaddingValues(0.dp)) {
        Column(Modifier.padding(Spacing.lg).animateContentSize(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            CardHeader(dua.id, null, dua.displayTitle(language), source, accent, saved, onSave, count, onReset = { onCount(0) },
                shareText = shareText, onCollapse = onCollapse)
            if (dua.context.isNotBlank()) LocalizedText(dua.context, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(shape = MaterialTheme.shapes.medium, color = tint.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth().tourTarget("recitation_dua_arabic")) {
                Text(dua.arabicText, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    fontFamily = ArabicFontFamily, color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(fontSize = fontSp.sp, lineHeight = lineSp.sp, textDirection = TextDirection.Rtl))
            }
            if (transliteration.isNotBlank()) Text(transliteration, style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic, color = tint)
            if (meaning.isNotBlank()) Text(meaning, style = MaterialTheme.typography.bodyLarge)
            audioContent()
            if (target > 1) DhikrCounter(dua.id, count, target, tint, onCount)
            else Row { Spacer(Modifier.weight(1f)); MarkReadButton(dua.id, count, 1, accent, onCount) }
        }
    }
}

/** Full-width tap target that counts toward a dhikr's recommended number, with a ring. */
@Composable
private fun DhikrCounter(id: String, count: Int, target: Int, tint: Color, onCount: (Int) -> Unit) {
    val done = count >= target
    Surface(
        onClick = {
        if (count < target) {
            if (count + 1 == target) com.example.core.analytics.AppAnalytics.milestone("recitation_collection", "target_reached", id)
            onCount(count + 1)
        }
    }, enabled = !done,
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("collection_count_$id").tourTarget("recitation_dhikr_counter"),
        shape = MaterialTheme.shapes.large,
        color = if (done) tint.copy(alpha = 0.16f) else tint.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { count.toFloat() / target }, modifier = Modifier.size(40.dp),
                    color = tint, strokeWidth = 4.dp, trackColor = tint.copy(alpha = 0.18f), strokeCap = StrokeCap.Round, gapSize = 0.dp)
                if (done) Icon(Icons.Default.Check, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("$count / $target", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = tint)
                LocalizedText(if (done) "Completed" else "Tap to count", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.Add, contentDescription = null, tint = tint)
        }
    }
}

@Composable
private fun SheetSection(title: String) {
    LocalizedText(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xs))
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, hint: String? = null, tourKey: String = "",
                          onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).then(if (tourKey.isEmpty()) Modifier else Modifier.tourTarget(tourKey)), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            LocalizedText(label, style = MaterialTheme.typography.bodyLarge)
            if (hint != null) LocalizedText(hint, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked, onChange, Modifier.testTag("collection_setting_$label"))
    }
}
