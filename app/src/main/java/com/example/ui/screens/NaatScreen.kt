package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.analytics.AppAnalytics
import com.example.naat.NaatAudioManager
import com.example.naat.NaatAudioState
import com.example.naat.NaatCatalog
import com.example.naat.NaatRepeat
import com.example.naat.NaatTrack
import com.example.ui.components.AppCard
import com.example.ui.components.AppSearchField
import com.example.ui.components.EmptyState
import com.example.ui.components.HeroSurface
import com.example.ui.components.PlaybackSpeedButton
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.components.accentTint
import com.example.ui.components.islamicPattern
import com.example.ui.components.pressScale
import com.example.ui.components.rememberIsOnline
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.ui.theme.GoldAccent
import java.util.Locale

private const val ARABIC_NASHEED = "Arabic Nasheed"
private const val ALL = "All"
private const val FAVOURITES = "Favourites"
private const val DOWNLOADED = "Downloaded"
private const val PLAY_LABEL = "Play"
private const val OFFLINE_READY_LABEL = "Offline ready"

private data class NaatKhawan(val name: String, val shortName: String, val initials: String, val accent: Color)

private val naatKhawans = listOf(
    NaatKhawan("Muhammad Owais Raza Qadri", "Owais Raza Qadri", "OR", Color(0xFF2A8C8C)),
    NaatKhawan("Hafiz Muhammad Tahir Qadri", "Tahir Qadri", "TQ", Color(0xFF4F6BB5)),
    NaatKhawan(ARABIC_NASHEED, ARABIC_NASHEED, "ع", Color(0xFF9A6A2F))
)

private fun NaatTrack.collection() = if (category == ARABIC_NASHEED) ARABIC_NASHEED else performer

private fun NaatTrack.khawan() = naatKhawans.firstOrNull { it.name == collection() }

/**
 * Roman Urdu is spelled many ways (Madina, Madinay, Medine), so search compares
 * consonants word by word: lower-case, drop vowels, y and h, and collapse doubled letters.
 */
private fun spellingKeys(text: String): List<String> =
    // Locale.ROOT: a Turkish device would otherwise lower-case "I" to a dotless "ı" and miss matches.
    text.lowercase(Locale.ROOT).split(Regex("[\\s-]+")).filter(String::isNotBlank)
        .map { it.replace(Regex("[aeiouyh'’()]"), "").replace(Regex("(.)\\1+"), "$1") }

private fun formatSize(bytes: Long): String = when {
    bytes <= 0 -> ""
    bytes >= 1L shl 30 -> String.format(Locale.US, "%.1f GB", bytes / (1L shl 30).toDouble())
    else -> String.format(Locale.US, "%.1f MB", bytes / (1L shl 20).toDouble())
}

private fun formatTime(ms: Int): String {
    val seconds = (ms / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60)
}

/** Pending confirmation shown in a dialog. */
private sealed interface NaatConfirm {
    data class DownloadMany(val tracks: List<NaatTrack>) : NaatConfirm
    data class Delete(val tracks: List<NaatTrack>) : NaatConfirm
}

/**
 * Voice-only naats. One reciter at a time, with search and filters; the last naat heard can be
 * resumed from the top, and a compact player expands into a full "Now playing" sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaatScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("naat_screen")

    val context = LocalContext.current
    val language = LocalAppLanguage.current
    LaunchedEffect(Unit) { NaatAudioManager.init(context) }
    val state by NaatAudioManager.state.collectAsStateWithLifecycle()
    val tracks by NaatCatalog.current.collectAsStateWithLifecycle()
    var khawanName by rememberSaveable { mutableStateOf(naatKhawans.first().name) }
    var filter by rememberSaveable { mutableStateOf(ALL) }
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("naat_screen", query)
    var confirm by remember { mutableStateOf<NaatConfirm?>(null) }
    var showNowPlaying by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    com.example.core.analytics.AnalyticsScroll("naat_screen", listState)
    // Measured, so the last naat clears the player at any font size.
    var playerHeight by remember { mutableIntStateOf(0) }
    val playerPadding = with(LocalDensity.current) { playerHeight.toDp() }.coerceAtLeast(96.dp) + 16.dp

    val byId = remember(tracks) { tracks.associateBy(NaatTrack::id) }
    val collection = remember(tracks, khawanName) { tracks.filter { it.collection() == khawanName } }
    val filters = remember(collection) {
        val categories = collection.map(NaatTrack::category).distinct()
        listOf(ALL) + (if (categories.size > 1) categories else emptyList()) + FAVOURITES + DOWNLOADED
    }
    val shown = remember(collection, filter, query, state.downloaded, state.favorites) {
        val words = spellingKeys(query)
        collection.filter { track ->
            when (filter) {
                ALL -> true
                DOWNLOADED -> track.id in state.downloaded
                FAVOURITES -> track.id in state.favorites
                else -> track.category == filter
            } && spellingKeys(track.title).let { title -> words.all { word -> title.any { it.startsWith(word) } } }
        }
    }
    val pending = shown.filter { it.id !in state.downloaded && it.id !in state.downloading }
    val offline = shown.filter { it.id in state.downloaded }
    val activeTrack = state.activeId?.let(byId::get)
    val resumeTrack = state.resumeId?.let(byId::get)?.takeIf { state.activeId == null }
    val recentTracks = remember(state.recent, byId) { state.recent.mapNotNull(byId::get) }
    val browsing = filter == ALL && query.isBlank()

    LaunchedEffect(activeTrack) { if (activeTrack == null) showNowPlaying = false }

    // Items above the reciter row: header and, when there is one, the resume card.
    val controlsIndex = 2 + if (resumeTrack != null) 1 else 0
    LaunchedEffect(khawanName, filter) {
        // Keep the reciters and filters in view after a switch, instead of a jump to the very top.
        if (listState.firstVisibleItemIndex > controlsIndex) listState.scrollToItem(controlsIndex)
    }

    Box(modifier.fillMaxSize().testTag("naat_screen")) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = 8.dp,
                bottom = if (activeTrack != null) playerPadding else 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header") {
                ScreenHeader(
                    title = "Naat • Without music",
                    subtitle = localizeUiTemplate("{count} naats", language, mapOf("count" to "${tracks.size}")) + " • " +
                        localizeUiTemplate("{count} offline", language, mapOf("count" to "${state.downloaded.size}")),
                    localizeSubtitle = false,
                    onBack = onBack
                )
            }
            if (resumeTrack != null) item(key = "continue") {
                ContinueCard(
                    track = resumeTrack,
                    positionMs = state.resumePositionMs,
                    onResume = {
                        AppAnalytics.interaction("naat_screen", "continue_listening", "tap")
                        // The queue is the reciter it belongs to, so next and previous stay with that reciter.
                        NaatAudioManager.resumeLast(tracks.filter { it.collection() == resumeTrack.collection() })
                    }
                )
            }
            item(key = "reciters") {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(com.example.core.analytics.rememberAnalyticsScrollState("naat_screen")),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    naatKhawans.forEach { khawan ->
                        val all = tracks.filter { it.collection() == khawan.name }
                        KhawanCard(
                            khawan = khawan,
                            count = all.size,
                            offline = all.count { it.id in state.downloaded },
                            selected = khawan.name == khawanName,
                            onClick = {
                                AppAnalytics.interaction("naat_screen", "select_khawan", "tap", item = khawan.name)
                                khawanName = khawan.name; filter = ALL; query = ""
                            }
                        )
                    }
                }
            }
            item(key = "search") {
                AppSearchField(query, { query = it }, "Search naat", Modifier.testTag("naat_search"))
            }
            item(key = "filters") {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(com.example.core.analytics.rememberAnalyticsScrollState("naat_screen")),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { name ->
                        val icon = when (name) {
                            DOWNLOADED -> Icons.Default.OfflinePin
                            FAVOURITES -> Icons.Default.Favorite
                            else -> null
                        }
                        FilterChip(
                            selected = name == filter,
                            onClick = { AppAnalytics.interaction("naat_screen", "select_naat_filter", "tap", item = name); filter = name },
                            label = { LocalizedText(name) },
                            leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) } },
                            shape = CircleShape,
                            modifier = Modifier.testTag("naat_filter_${name.lowercase()}")
                        )
                    }
                }
            }
            item(key = "actions") {
                ActionBar(
                    shown = shown,
                    pending = pending,
                    offline = offline,
                    state = state,
                    downloadingHere = collection.count { it.id in state.downloading },
                    isDownloadedFilter = filter == DOWNLOADED,
                    onConfirm = { confirm = it }
                )
            }
            state.error?.let { message -> item(key = "error") { ErrorBanner(message) } }
            if (browsing && recentTracks.size > 1) item(key = "recent") {
                RecentRow(recentTracks, state) { track ->
                    AppAnalytics.interaction("naat_screen", "play_recent", "tap")
                    NaatAudioManager.play(track, recentTracks)
                }
            }
            if (shown.isEmpty()) item(key = "empty") {
                when {
                    query.isNotBlank() -> EmptyState(
                        title = "No naat found.",
                        icon = Icons.Default.SearchOff,
                        actionText = "Clear search",
                        onAction = { query = "" }
                    )
                    filter == FAVOURITES -> EmptyState(
                        title = "Tap the heart on any naat to keep it in Favourites.",
                        icon = Icons.Default.FavoriteBorder
                    )
                    filter == DOWNLOADED -> EmptyState(
                        title = "No downloads yet. Tap the download icon on any naat to keep it offline.",
                        icon = Icons.Default.DownloadForOffline
                    )
                    else -> EmptyState(title = "No naat found.", icon = Icons.Default.SearchOff)
                }
            }
            itemsIndexed(shown, key = { _, track -> track.id }) { index, track ->
                TrackRow(
                    number = index + 1,
                    track = track,
                    state = state,
                    onPlay = { NaatAudioManager.play(track, shown) },
                    onFavorite = { NaatAudioManager.toggleFavorite(track.id) },
                    onDownload = { NaatAudioManager.download(track) },
                    onDelete = { confirm = NaatConfirm.Delete(listOf(track)) }
                )
            }
            item(key = "footer") {
                LocalizedText("Audio: alhajowaisrazaqadri.com and Internet Archive • Catalog: GitHub. Downloaded files stay on this device.",
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        AnimatedVisibility(
            visible = activeTrack != null,
            modifier = Modifier.align(Alignment.BottomCenter).onSizeChanged { playerHeight = it.height },
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            activeTrack?.let { MiniPlayer(it, state, onExpand = {
                AppAnalytics.interaction("naat_screen", "open_now_playing", "tap")
                showNowPlaying = true
            }) }
        }
    }

    if (showNowPlaying && activeTrack != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showNowPlaying = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NowPlaying(activeTrack, state, onDelete = { confirm = NaatConfirm.Delete(listOf(activeTrack)) })
        }
    }

    confirm?.let { request -> ConfirmDialog(request, onDismiss = { confirm = null }) }
}

// region Browse

@Composable
private fun ContinueCard(track: NaatTrack, positionMs: Int, onResume: () -> Unit) {
    HeroSurface(
        modifier = Modifier.fillMaxWidth().testTag("naat_continue"),
        onClick = onResume,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LocalizedText("Continue listening", style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(track.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(track.performer, formatTime(positionMs).takeIf { positionMs > 3_000 }).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.78f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(GoldAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = localizeUiText(PLAY_LABEL, LocalAppLanguage.current),
                    tint = Color(0xFF0B2D25), modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun KhawanCard(khawan: NaatKhawan, count: Int, offline: Int, selected: Boolean, onClick: () -> Unit) {
    val language = LocalAppLanguage.current
    val accent = accentTint(khawan.accent)
    val interaction = remember { MutableInteractionSource() }
    Surface(
        selected = selected,
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier.width(168.dp).pressScale(interaction).testTag("naat_khawan_${khawan.initials.lowercase()}"),
        shape = MaterialTheme.shapes.large,
        color = if (selected) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp,
            if (selected) accent else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KhawanAvatar(khawan, size = 44)
                Spacer(Modifier.weight(1f))
                if (selected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Text(if (khawan.name == ARABIC_NASHEED) localizeUiText(khawan.name, language) else khawan.shortName,
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                localizeUiTemplate("{count} naats", language, mapOf("count" to "$count")) +
                    if (offline > 0) " • " + localizeUiTemplate("{count} offline", language, mapOf("count" to "$offline")) else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun KhawanAvatar(khawan: NaatKhawan?, size: Int, playing: Boolean = false) {
    val accent = khawan?.accent ?: MaterialTheme.colorScheme.primary
    Box(
        Modifier.size(size.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.6f)))),
        contentAlignment = Alignment.Center
    ) {
        if (playing) Equalizer(Color.White)
        else Text(khawan?.initials ?: "♪", color = Color.White, fontWeight = FontWeight.Bold,
            fontSize = (size * 0.36f).sp)
    }
}

@Composable
private fun ActionBar(
    shown: List<NaatTrack>,
    pending: List<NaatTrack>,
    offline: List<NaatTrack>,
    state: NaatAudioState,
    downloadingHere: Int,
    isDownloadedFilter: Boolean,
    onConfirm: (NaatConfirm) -> Unit
) {
    val language = LocalAppLanguage.current
    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    AppAnalytics.interaction("naat_screen", "play_all", "tap")
                    if (state.shuffle) NaatAudioManager.toggleShuffle()
                    shown.firstOrNull()?.let { NaatAudioManager.play(it, shown) }
                },
                enabled = shown.isNotEmpty(),
                modifier = Modifier.heightIn(min = 48.dp).testTag("naat_play_all")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                LocalizedText("Play all", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedIconButton(
                onClick = {
                    AppAnalytics.interaction("naat_screen", "shuffle_all", "tap")
                    if (!state.shuffle) NaatAudioManager.toggleShuffle()
                    shown.randomOrNull()?.let { NaatAudioManager.play(it, shown) }
                },
                enabled = shown.size > 1,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = localizeUiText("Shuffle", language))
            }
            Spacer(Modifier.weight(1f))
            when {
                downloadingHere > 0 -> FilledTonalButton(
                    onClick = AppAnalytics.trackAction("naat_screen", "stop_downloads", NaatAudioManager::cancelDownloads),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    LocalizedText("Stop", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                isDownloadedFilter && offline.isNotEmpty() -> FilledTonalIconButton(
                    onClick = { AppAnalytics.interaction("naat_screen", "delete_downloads", "tap"); onConfirm(NaatConfirm.Delete(offline)) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = localizeUiText("Delete all", language))
                }
                pending.isNotEmpty() -> FilledTonalIconButton(
                    onClick = { AppAnalytics.interaction("naat_screen", "download_all", "tap"); onConfirm(NaatConfirm.DownloadMany(pending)) },
                    modifier = Modifier.size(48.dp).testTag("naat_download_all")
                ) {
                    Icon(Icons.Default.DownloadForOffline, contentDescription = localizeUiText("Download all", language))
                }
            }
        }
        if (downloadingHere > 0) {
            val done = state.progress.values.filter { it > 0 }.sum() / 100f
            Text("${localizeUiText("Downloading", language)}: $downloadingHere • " +
                "${localizeUiText("Offline", language)} ${offline.size} / ${shown.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
            LinearProgressIndicator(
                progress = { ((offline.size + done) / shown.size.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        } else if (shown.isNotEmpty()) {
            Text(localizeUiTemplate("{count} naats", language, mapOf("count" to "${shown.size}")) + " • " +
                localizeUiTemplate("{count} offline", language, mapOf("count" to "${offline.size}")) +
                formatSize(offline.sumOf(NaatTrack::sizeBytes)).let { if (it.isEmpty()) "" else " ($it)" },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp))
        }
    }
}

/** The last naats heard, newest first, as small cards that play straight away. */
@Composable
private fun RecentRow(recent: List<NaatTrack>, state: NaatAudioState, onPlay: (NaatTrack) -> Unit) {
    Column(Modifier.padding(top = 4.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            LocalizedText("Recently played", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() })
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.testTag("naat_recent")) {
            items(recent, key = { it.id }) { track ->
                val active = state.activeId == track.id
                AppCard(
                    modifier = Modifier.width(176.dp),
                    onClick = { onPlay(track) },
                    containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KhawanAvatar(track.khawan(), size = 36, playing = active && state.playing && !state.buffering)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(track.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(track.khawan()?.shortName ?: track.performer, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    number: Int,
    track: NaatTrack,
    state: NaatAudioState,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val language = LocalAppLanguage.current
    val isOnline by rememberIsOnline()
    val haptics = LocalHapticFeedback.current
    val isActive = state.activeId == track.id
    val isDownloaded = track.id in state.downloaded
    val isFavourite = track.id in state.favorites
    val tapPlay = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onPlay() }
    AppCard(
        modifier = Modifier.fillMaxWidth().testTag("naat_${track.id}"),
        onClick = AppAnalytics.trackAction("naat_screen", "play_naat", tapPlay),
        shape = MaterialTheme.shapes.large,
        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface,
        outlined = !isActive,
        contentPadding = PaddingValues(start = 10.dp, end = 2.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isActive && state.buffering -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    isActive && state.playing -> Equalizer(MaterialTheme.colorScheme.onPrimary)
                    isActive -> Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    else -> Text("$number", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(track.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDownloaded) {
                        Icon(Icons.Default.OfflinePin, contentDescription = localizeUiText(OFFLINE_READY_LABEL, language),
                            modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(listOf(
                        if (track.category == ARABIC_NASHEED) track.performer else localizeUiText(track.category, language),
                        formatSize(track.sizeBytes),
                        // Only the case that matters is spelled out: offline, this naat cannot play.
                        if (!isDownloaded && !isOnline) localizeUiText("Needs internet", language) else ""
                    ).filter(String::isNotEmpty).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            FavouriteButton(isFavourite, track.title, "naat_favourite_${track.id}", onFavorite)
            DownloadButton(track, state, onDownload, onDelete)
        }
    }
}

@Composable
private fun FavouriteButton(isFavourite: Boolean, title: String, tag: String, onToggle: () -> Unit) {
    val language = LocalAppLanguage.current
    val haptics = LocalHapticFeedback.current
    IconButton(
        onClick = {
            AppAnalytics.interaction("naat_screen", if (isFavourite) "unfavourite_naat" else "favourite_naat", "tap")
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onToggle()
        },
        modifier = Modifier.testTag(tag)
    ) {
        Icon(
            if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = localizeUiText(if (isFavourite) "Remove from favourites" else "Add to favourites", language) + " " + title,
            tint = if (isFavourite) Color(0xFFD0485E) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Download state for one naat: download, progress, retry after a failure, or delete once saved. */
@Composable
private fun DownloadButton(track: NaatTrack, state: NaatAudioState, onDownload: () -> Unit, onDelete: () -> Unit) {
    val language = LocalAppLanguage.current
    when {
        track.id in state.downloading -> Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            val percent = state.progress[track.id]
            if (percent == null || percent <= 0) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp)
            else {
                CircularProgressIndicator(progress = { percent / 100f }, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                Text("$percent", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
            }
        }
        track.id in state.downloaded -> IconButton(onClick = AppAnalytics.trackAction("naat_screen", "delete_naat_download", onDelete)) {
            Icon(Icons.Default.DeleteOutline,
                contentDescription = "${localizeUiText("Delete offline audio", language)} ${track.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        track.id in state.failed -> IconButton(onClick = AppAnalytics.trackAction("naat_screen", "retry_naat_download", onDownload)) {
            Icon(Icons.Default.Refresh, contentDescription = "${localizeUiText("Retry", language)} ${track.title}",
                tint = MaterialTheme.colorScheme.error)
        }
        else -> IconButton(onClick = AppAnalytics.trackAction("naat_screen", "download_naat", onDownload)) {
            Icon(Icons.Default.Download, contentDescription = "${localizeUiText("Download", language)} ${track.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Equalizer(color: Color) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    Row(Modifier.height(18.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        listOf(420, 300, 520).forEach { duration ->
            val level by transition.animateFloat(0.25f, 1f,
                infiniteRepeatable(tween(duration), RepeatMode.Reverse), label = "bar")
            Box(Modifier.width(4.dp).fillMaxHeight(level).clip(RoundedCornerShape(2.dp)).background(color))
        }
    }
}

// endregion

// region Player

/** Compact player above the bottom edge: tap it to open the full "Now playing" sheet. */
@Composable
private fun MiniPlayer(track: NaatTrack, state: NaatAudioState, onExpand: () -> Unit) {
    val language = LocalAppLanguage.current
    val position by NaatAudioManager.position.collectAsStateWithLifecycle()
    val fraction = if (position.durationMs > 0) (position.positionMs.toFloat() / position.durationMs).coerceIn(0f, 1f) else 0f
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("naat_player"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Column {
            Row(
                Modifier.clickable(role = Role.Button, onClickLabel = localizeUiText("Now playing", language), onClick = onExpand)
                    .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtworkTile(track, state, Modifier.size(46.dp), corner = 14)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(track.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(state.error?.let { localizeUiText(it, language) } ?: track.performer,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                PlayPauseButton(state, size = 48)
                IconButton(onClick = AppAnalytics.trackAction("naat_screen", "player_next", NaatAudioManager::next), enabled = state.hasNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = localizeUiText("Next", language))
                }
                IconButton(onClick = AppAnalytics.trackAction("naat_screen", "close_player", NaatAudioManager::stop)) {
                    Icon(Icons.Default.Close, contentDescription = localizeUiText("Close player", language),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // A hairline of progress along the bottom edge, like every music app.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
private fun ArtworkTile(track: NaatTrack, state: NaatAudioState, modifier: Modifier, corner: Int, large: Boolean = false) {
    val khawan = track.khawan()
    val accent = khawan?.accent ?: MaterialTheme.colorScheme.primary
    Box(
        modifier.clip(RoundedCornerShape(corner.dp))
            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.55f), Color(0xFF0B2D25))))
            .islamicPattern(Color.White.copy(alpha = if (large) 0.08f else 0.05f), cell = if (large) 40.dp else 18.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.playing && !state.buffering && !large -> Equalizer(Color.White)
            large -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(khawan?.initials ?: "♪", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 56.sp)
                Spacer(Modifier.height(8.dp))
                if (state.playing && !state.buffering) Equalizer(GoldAccent)
                else Icon(Icons.Default.Headphones, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
            }
            else -> Icon(Icons.Default.Headphones, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun PlayPauseButton(state: NaatAudioState, size: Int) {
    val language = LocalAppLanguage.current
    FilledIconButton(
        onClick = AppAnalytics.trackAction("naat_screen", "player_play_pause", NaatAudioManager::togglePlayPause),
        modifier = Modifier.size(size.dp)
    ) {
        if (state.buffering) CircularProgressIndicator(Modifier.size((size * 0.42f).dp), strokeWidth = 2.5.dp,
            color = MaterialTheme.colorScheme.onPrimary)
        else Icon(if (state.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = localizeUiText(if (state.playing) "Pause" else "Play", language),
            modifier = Modifier.size((size * 0.55f).dp))
    }
}

/** Full player: artwork, title, seek bar with times, and every playback control. */
@Composable
private fun NowPlaying(track: NaatTrack, state: NaatAudioState, onDelete: () -> Unit) {
    val language = LocalAppLanguage.current
    val position by NaatAudioManager.position.collectAsStateWithLifecycle()
    var dragging by remember { mutableStateOf<Float?>(null) }
    val duration = position.durationMs.coerceAtLeast(1)
    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            .testTag("naat_now_playing"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArtworkTile(track, state, Modifier.widthIn(max = 260.dp).fillMaxWidth().aspectRatio(1f), corner = 28, large = true)
        Spacer(Modifier.height(20.dp))
        Text(track.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(track.performer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        state.error?.let {
            Spacer(Modifier.height(8.dp))
            TonalPill(localizeUiText(it, language), icon = Icons.Default.ErrorOutline,
                containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FavouriteButton(track.id in state.favorites, track.title, "naat_now_playing_favourite") { NaatAudioManager.toggleFavorite(track.id) }
            DownloadButton(track, state, onDownload = { NaatAudioManager.download(track) }, onDelete = onDelete)
            PlaybackSpeedButton(state.speed, NaatAudioManager::setSpeed, color = MaterialTheme.colorScheme.primary)
        }
        // Seek bar and transport controls keep the left-to-right order used by media players everywhere.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column {
                Slider(
                    value = dragging ?: (position.positionMs.toFloat() / duration).coerceIn(0f, 1f),
                    onValueChange = { dragging = it },
                    onValueChangeFinished = {
                        AppAnalytics.interaction("naat_screen", "seek_slider", "change_complete")
                        dragging?.let { NaatAudioManager.seekTo((it * duration).toInt()) }
                        dragging = null
                    },
                    enabled = position.durationMs > 0,
                    modifier = Modifier.fillMaxWidth().testTag("naat_seek")
                )
                Row(Modifier.fillMaxWidth()) {
                    Text(formatTime(dragging?.let { (it * duration).toInt() } ?: position.positionMs),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text(if (position.durationMs > 0) formatTime(position.durationMs) else "--:--",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    ToggleIcon(Icons.Default.Shuffle, state.shuffle, localizeUiText("Shuffle", language), NaatAudioManager::toggleShuffle)
                    IconButton(onClick = AppAnalytics.trackAction("naat_screen", "player_previous", NaatAudioManager::previous),
                        modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = localizeUiText("Previous", language), modifier = Modifier.size(34.dp))
                    }
                    PlayPauseButton(state, size = 72)
                    IconButton(onClick = AppAnalytics.trackAction("naat_screen", "player_next", NaatAudioManager::next),
                        enabled = state.hasNext, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.SkipNext, contentDescription = localizeUiText("Next", language), modifier = Modifier.size(34.dp))
                    }
                    ToggleIcon(
                        if (state.repeat == NaatRepeat.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        state.repeat != NaatRepeat.OFF,
                        localizeUiText("Repeat", language),
                        NaatAudioManager::cycleRepeat
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleIcon(icon: ImageVector, on: Boolean, label: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = if (on) IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.primary
        ) else IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        Icon(icon, contentDescription = label)
    }
}

// endregion

@Composable
private fun ErrorBanner(message: String) {
    val language = LocalAppLanguage.current
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.errorContainer) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            LocalizedText(message, modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            IconButton(onClick = AppAnalytics.trackAction("naat_screen", "dismiss_naat_error", NaatAudioManager::clearError)) {
                Icon(Icons.Default.Close, contentDescription = localizeUiText("Close", language), tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun ConfirmDialog(request: NaatConfirm, onDismiss: () -> Unit) {
    val language = LocalAppLanguage.current
    val tracks = when (request) {
        is NaatConfirm.DownloadMany -> request.tracks
        is NaatConfirm.Delete -> request.tracks
    }
    val size = formatSize(tracks.sumOf(NaatTrack::sizeBytes)).let { if (it.isEmpty()) "" else " ($it)" }
    val single = tracks.singleOrNull()?.title
    AlertDialog(
        onDismissRequest = AppAnalytics.trackAction("naat_screen", "dismiss_bulk_confirm", onDismiss),
        icon = {
            Icon(if (request is NaatConfirm.Delete) Icons.Default.DeleteOutline else Icons.Default.DownloadForOffline, contentDescription = null)
        },
        title = {
            LocalizedText(if (request is NaatConfirm.Delete) "Delete offline audio?" else "Download for offline?")
        },
        text = {
            Text(when (request) {
                is NaatConfirm.DownloadMany -> "${tracks.size} ${localizeUiText("naats", language)}$size. " +
                    localizeUiText("Wi-Fi is recommended. You can stop anytime.", language)
                is NaatConfirm.Delete -> (single ?: "${tracks.size} ${localizeUiText("naats", language)}") + "$size. " +
                    localizeUiText("You can still listen online.", language)
            })
        },
        confirmButton = {
            TextButton(onClick = {
                AppAnalytics.interaction("naat_screen", "confirm_bulk_action", "tap")
                when (request) {
                    is NaatConfirm.DownloadMany -> NaatAudioManager.downloadAll(request.tracks)
                    is NaatConfirm.Delete -> NaatAudioManager.deleteAll(request.tracks)
                }
                onDismiss()
            }) { LocalizedText(if (request is NaatConfirm.Delete) "Delete" else "Download") }
        },
        dismissButton = { TextButton(onClick = AppAnalytics.trackAction("naat_screen", "cancel_bulk_confirm", onDismiss)) { LocalizedText("Cancel") } }
    )
}
