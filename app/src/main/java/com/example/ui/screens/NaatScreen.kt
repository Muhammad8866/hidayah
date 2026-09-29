package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.naat.NaatAudioManager
import com.example.naat.NaatAudioState
import com.example.naat.NaatCatalog
import com.example.naat.NaatRepeat
import com.example.naat.NaatTrack
import com.example.ui.components.BackIconButton
import com.example.ui.components.PlaybackSpeedButton
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiText
import java.util.Locale

private const val ARABIC_NASHEED = "Arabic Nasheed"
private const val ALL = "All"
private const val DOWNLOADED = "Downloaded"

private data class NaatKhawan(val name: String, val shortName: String, val initials: String, val accent: Color)

private val naatKhawans = listOf(
    NaatKhawan("Muhammad Owais Raza Qadri", "Owais Raza Qadri", "OR", Color(0xFF2A8C8C)),
    NaatKhawan("Hafiz Muhammad Tahir Qadri", "Tahir Qadri", "TQ", Color(0xFF4F6BB5)),
    NaatKhawan(ARABIC_NASHEED, ARABIC_NASHEED, "ع", Color(0xFF9A6A2F))
)

private fun NaatTrack.collection() = if (category == ARABIC_NASHEED) ARABIC_NASHEED else performer

/**
 * Roman Urdu is spelled many ways (Madina, Madinay, Medine), so search compares
 * consonants word by word: lower-case, drop vowels, y and h, and collapse doubled letters.
 */
private fun spellingKeys(text: String): List<String> =
    text.lowercase().split(Regex("[\\s-]+")).filter(String::isNotBlank)
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

@Composable
fun NaatScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    LaunchedEffect(Unit) { NaatAudioManager.init(context) }
    val state by NaatAudioManager.state.collectAsStateWithLifecycle()
    val tracks by NaatCatalog.current.collectAsStateWithLifecycle()
    var khawanName by rememberSaveable { mutableStateOf(naatKhawans.first().name) }
    var filter by rememberSaveable { mutableStateOf(ALL) }
    var query by rememberSaveable { mutableStateOf("") }
    var confirm by remember { mutableStateOf<NaatConfirm?>(null) }
    val listState = rememberLazyListState()

    val collection = remember(tracks, khawanName) { tracks.filter { it.collection() == khawanName } }
    val filters = remember(collection) {
        val categories = collection.map(NaatTrack::category).distinct()
        listOf(ALL) + (if (categories.size > 1) categories else emptyList()) + DOWNLOADED
    }
    val shown = remember(collection, filter, query, state.downloaded) {
        val words = spellingKeys(query)
        collection.filter { track ->
            when (filter) {
                ALL -> true
                DOWNLOADED -> track.id in state.downloaded
                else -> track.category == filter
            } && spellingKeys(track.title).let { title -> words.all { word -> title.any { it.startsWith(word) } } }
        }
    }
    val pending = shown.filter { it.id !in state.downloaded && it.id !in state.downloading }
    val offline = shown.filter { it.id in state.downloaded }
    val activeTrack = tracks.firstOrNull { it.id == state.activeId }

    LaunchedEffect(khawanName, filter) { listState.scrollToItem(0) }

    Box(modifier.fillMaxSize().testTag("naat_screen")) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp,
                bottom = if (activeTrack != null) 176.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BackIconButton(onBack)
                    Column(Modifier.weight(1f)) {
                        LocalizedText("Naat • Without music", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${tracks.size} ${localizeUiText("naats", language)} • ${state.downloaded.size} ${localizeUiText("offline", language)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    naatKhawans.forEach { khawan ->
                        val all = tracks.filter { it.collection() == khawan.name }
                        KhawanCard(
                            khawan = khawan,
                            count = all.size,
                            offline = all.count { it.id in state.downloaded },
                            selected = khawan.name == khawanName,
                            onClick = { khawanName = khawan.name; filter = ALL; query = "" }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().testTag("naat_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = localizeUiText("Clear search", language))
                        }
                    },
                    placeholder = { LocalizedText("Search naat") }
                )
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEach { name ->
                        FilterChip(
                            selected = name == filter,
                            onClick = { filter = name },
                            label = { LocalizedText(name) },
                            leadingIcon = if (name == DOWNLOADED) {
                                { Icon(Icons.Default.OfflinePin, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null,
                            modifier = Modifier.testTag("naat_filter_${name.lowercase()}")
                        )
                    }
                }
            }
            item {
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
            state.error?.let { message ->
                item {
                    ErrorBanner(message)
                }
            }
            if (shown.isEmpty()) item {
                EmptyState(
                    if (filter == DOWNLOADED && query.isBlank()) "No downloads yet. Tap the download icon on any naat to keep it offline."
                    else "No naat found."
                )
            }
            itemsIndexed(shown, key = { _, track -> track.id }) { index, track ->
                TrackRow(
                    number = index + 1,
                    track = track,
                    state = state,
                    onPlay = { NaatAudioManager.play(track, shown) },
                    onDownload = { NaatAudioManager.download(track) },
                    onDelete = { confirm = NaatConfirm.Delete(listOf(track)) }
                )
            }
            item {
                LocalizedText("Audio: alhajowaisrazaqadri.com and Internet Archive • Catalog: GitHub. Downloaded files stay on this device.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        AnimatedVisibility(
            visible = activeTrack != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            activeTrack?.let { MiniPlayer(it, state) }
        }
    }

    confirm?.let { request -> ConfirmDialog(request, onDismiss = { confirm = null }) }
}

@Composable
private fun KhawanCard(khawan: NaatKhawan, count: Int, offline: Int, selected: Boolean, onClick: () -> Unit) {
    val language = LocalAppLanguage.current
    Surface(
        onClick = onClick,
        modifier = Modifier.width(204.dp).testTag("naat_khawan_${khawan.initials.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) khawan.accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp,
            if (selected) khawan.accent else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(khawan.accent, khawan.accent.copy(alpha = 0.6f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(khawan.initials, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(if (khawan.name == ARABIC_NASHEED) localizeUiText(khawan.name, language) else khawan.shortName,
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$count ${localizeUiText("naats", language)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (offline > 0) Text("$offline ${localizeUiText("offline", language)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    if (state.shuffle) NaatAudioManager.toggleShuffle()
                    shown.firstOrNull()?.let { NaatAudioManager.play(it, shown) }
                },
                enabled = shown.isNotEmpty(),
                modifier = Modifier.testTag("naat_play_all")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                LocalizedText("Play all")
            }
            OutlinedIconButton(
                onClick = {
                    if (!state.shuffle) NaatAudioManager.toggleShuffle()
                    shown.randomOrNull()?.let { NaatAudioManager.play(it, shown) }
                },
                enabled = shown.size > 1
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = localizeUiText("Shuffle", language))
            }
            Spacer(Modifier.weight(1f))
            when {
                downloadingHere > 0 -> FilledTonalButton(onClick = NaatAudioManager::cancelDownloads) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    LocalizedText("Stop")
                }
                isDownloadedFilter && offline.isNotEmpty() -> FilledTonalButton(onClick = { onConfirm(NaatConfirm.Delete(offline)) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    LocalizedText("Delete all")
                }
                pending.isNotEmpty() -> FilledTonalButton(
                    onClick = { onConfirm(NaatConfirm.DownloadMany(pending)) },
                    modifier = Modifier.testTag("naat_download_all")
                ) {
                    Icon(Icons.Default.DownloadForOffline, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    LocalizedText("Download all")
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
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
            )
        } else if (shown.isNotEmpty()) {
            Text("${shown.size} ${localizeUiText("naats", language)} • ${offline.size} ${localizeUiText("offline", language)}" +
                formatSize(offline.sumOf(NaatTrack::sizeBytes)).let { if (it.isEmpty()) "" else " ($it)" },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TrackRow(
    number: Int,
    track: NaatTrack,
    state: NaatAudioState,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val language = LocalAppLanguage.current
    val isActive = state.activeId == track.id
    val isDownloaded = track.id in state.downloaded
    val isDownloading = track.id in state.downloading
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("naat_${track.id}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (isActive) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier.clickable(role = Role.Button, onClick = onPlay).padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                when {
                    isActive && state.buffering -> CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    isActive && state.playing -> Equalizer(MaterialTheme.colorScheme.primary)
                    isActive -> Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    else -> Text("$number", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(track.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDownloaded) {
                        Icon(Icons.Default.OfflinePin, contentDescription = null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(listOf(
                        if (track.category == ARABIC_NASHEED) track.performer else localizeUiText(track.category, language),
                        formatSize(track.sizeBytes),
                        localizeUiText(if (isDownloaded) "Offline ready" else "Online", language)
                    ).filter(String::isNotEmpty).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            when {
                isDownloading -> Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    val percent = state.progress[track.id]
                    if (percent == null || percent <= 0) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    else {
                        CircularProgressIndicator(progress = { percent / 100f }, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                        Text("$percent", style = MaterialTheme.typography.labelSmall)
                    }
                }
                isDownloaded -> IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline,
                        contentDescription = "${localizeUiText("Delete offline audio", language)} ${track.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> IconButton(onClick = onDownload) {
                    Icon(Icons.Default.Download, contentDescription = "${localizeUiText("Download", language)} ${track.title}")
                }
            }
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

@Composable
private fun MiniPlayer(track: NaatTrack, state: NaatAudioState) {
    val language = LocalAppLanguage.current
    val position by NaatAudioManager.position.collectAsStateWithLifecycle()
    var dragging by remember { mutableStateOf<Float?>(null) }
    val duration = position.durationMs.coerceAtLeast(1)
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("naat_player"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        Column(Modifier.navigationBarsPadding().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(track.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(track.performer, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                PlaybackSpeedButton(state.speed, NaatAudioManager::setSpeed, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = NaatAudioManager::stop) {
                    Icon(Icons.Default.Close, contentDescription = localizeUiText("Close player", language))
                }
            }
            Slider(
                value = dragging ?: (position.positionMs.toFloat() / duration).coerceIn(0f, 1f),
                onValueChange = { dragging = it },
                onValueChangeFinished = {
                    dragging?.let { NaatAudioManager.seekTo((it * duration).toInt()) }
                    dragging = null
                },
                enabled = position.durationMs > 0,
                modifier = Modifier.fillMaxWidth().height(28.dp).padding(end = 8.dp)
            )
            Row(Modifier.fillMaxWidth().padding(end = 8.dp)) {
                Text(formatTime(dragging?.let { (it * duration).toInt() } ?: position.positionMs),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(if (position.durationMs > 0) formatTime(position.durationMs) else "--:--",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                ToggleIcon(Icons.Default.Shuffle, state.shuffle, localizeUiText("Shuffle", language), NaatAudioManager::toggleShuffle)
                IconButton(onClick = NaatAudioManager::previous) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = localizeUiText("Previous", language), modifier = Modifier.size(30.dp))
                }
                FilledIconButton(onClick = NaatAudioManager::togglePlayPause, modifier = Modifier.size(56.dp)) {
                    if (state.buffering) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    else Icon(if (state.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = localizeUiText(if (state.playing) "Pause" else "Play", language),
                        modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = NaatAudioManager::next, enabled = state.hasNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = localizeUiText("Next", language), modifier = Modifier.size(30.dp))
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

@Composable
private fun ToggleIcon(icon: ImageVector, on: Boolean, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = label,
            tint = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.errorContainer) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            LocalizedText(message, modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            IconButton(onClick = NaatAudioManager::clearError) {
                Icon(Icons.Default.Close, contentDescription = "OK", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Spacer(Modifier.height(8.dp))
        LocalizedText(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        onDismissRequest = onDismiss,
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
                when (request) {
                    is NaatConfirm.DownloadMany -> NaatAudioManager.downloadAll(request.tracks)
                    is NaatConfirm.Delete -> NaatAudioManager.deleteAll(request.tracks)
                }
                onDismiss()
            }) { LocalizedText(if (request is NaatConfirm.Delete) "Delete" else "Download") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } }
    )
}
