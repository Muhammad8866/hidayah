package com.example.ui.screens

import com.example.ui.components.AppSearchField
import com.example.ui.components.EmptyState
import com.example.ui.components.IconBadge
import com.example.ui.components.Spacing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.SearchOff
import com.example.ui.components.ScreenHeader
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.azan.AzanAudioManager
import com.example.azan.AzanAudioState
import com.example.azan.AzanCatalog
import com.example.azan.AzanTrack
import com.example.prayer.PrayerSettingsStore
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiText
import java.util.Locale

private const val ALL = "All"
private const val FAJR = "Fajr"
private const val DOWNLOADED = "Downloaded"

private fun AzanTrack.details(): String = listOfNotNull(
    muezzin.takeIf { it != region },
    region,
    seconds.takeIf { it > 0 }?.let { String.format(Locale.US, "%d:%02d", it / 60, it % 60) },
    sizeBytes.takeIf { it > 0 }?.let { String.format(Locale.US, "%.1f MB", it / (1L shl 20).toDouble()) }
).joinToString(" • ")

private fun AzanTrack.fullName(): String = if (muezzin == region) title else "$title • $muezzin"

/** Azans from around the world: listen, keep offline, and use one as the prayer alert tone. */
@Composable
fun AzanScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("azan_screen")

    val context = LocalContext.current
    val language = LocalAppLanguage.current
    LaunchedEffect(Unit) { AzanAudioManager.init(context) }
    // Listening here is a preview, so it ends when the screen is left.
    DisposableEffect(Unit) { onDispose { AzanAudioManager.stop() } }
    val state by AzanAudioManager.state.collectAsStateWithLifecycle()
    val tracks = remember { AzanCatalog.load(context) }
    val alertsEnabled = remember { PrayerSettingsStore.notificationsEnabled(PrayerSettingsStore.prefs(context)) }
    var filter by rememberSaveable { mutableStateOf(ALL) }
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("azan_screen", query)
    val listState = rememberLazyListState()

    com.example.core.analytics.AnalyticsScroll("azan_screen", listState)
    val filters = remember(tracks) { listOf(ALL, FAJR, DOWNLOADED) + tracks.map(AzanTrack::region).distinct() }
    val shown = remember(tracks, filter, query, state.downloaded) {
        // Locale.ROOT: a Turkish device would otherwise lower-case "I" to a dotless "ı" and miss matches.
        val words = query.lowercase(Locale.ROOT).split(' ').filter(String::isNotBlank)
        tracks.filter { track ->
            when (filter) {
                ALL -> true
                FAJR -> track.fajr
                DOWNLOADED -> track.id in state.downloaded
                else -> track.region == filter
            } && "${track.title} ${track.muezzin} ${track.region}".lowercase(Locale.ROOT).let { text -> words.all(text::contains) }
        }
    }

    LaunchedEffect(filter) { listState.scrollToItem(0) }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().testTag("azan_screen"),
        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item {
            ScreenHeader(
                title = "Azan of the world",
                subtitle = "${tracks.size} ${localizeUiText("azans", language)} • ${state.downloaded.size} ${localizeUiText("offline", language)}",
                localizeSubtitle = false,
                onBack = onBack
            )
        }
        item {
            AlertToneCard(
                fajr = tracks.firstOrNull { it.id == state.fajrAlertId },
                other = tracks.firstOrNull { it.id == state.otherAlertId },
                alertsEnabled = alertsEnabled
            )
        }
        item {
            AppSearchField(query, { query = it }, "Search azan, muezzin or country", Modifier.testTag("azan_search"))
        }
        item {
            Row(Modifier.horizontalScroll(com.example.core.analytics.rememberAnalyticsScrollState("azan_screen")), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { name ->
                    FilterChip(
                        selected = name == filter,
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("azan_screen", "select_azan_filter", "tap", item = name); filter = name },
                        // Place names stay as written; only the three fixed filters are translated.
                        label = { if (name == ALL || name == FAJR || name == DOWNLOADED) LocalizedText(name) else Text(name) },
                        leadingIcon = if (name == DOWNLOADED) {
                            { Icon(Icons.Default.OfflinePin, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null,
                        modifier = Modifier.testTag("azan_filter_${name.lowercase()}")
                    )
                }
            }
        }
        state.error?.let { message -> item { AzanErrorBanner(message) } }
        if (shown.isEmpty()) item {
            EmptyState(
                title = if (filter == DOWNLOADED && query.isBlank()) "No downloads yet. Tap the download icon on any azan to keep it offline."
                else "No azan found.",
                icon = if (filter == DOWNLOADED && query.isBlank()) Icons.Default.OfflinePin else Icons.Default.SearchOff
            )
        }
        itemsIndexed(shown, key = { _, track -> track.id }) { index, track ->
            AzanRow(number = index + 1, track = track, state = state)
        }
        item {
            LocalizedText("Audio: Internet Archive. Downloaded files stay on this device.",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AlertToneCard(fajr: AzanTrack?, other: AzanTrack?, alertsEnabled: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("azan_alert_card"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Default.NotificationsActive, accent = MaterialTheme.colorScheme.onSecondaryContainer,
                    size = 32.dp, iconSize = 18.dp)
                Spacer(Modifier.width(10.dp))
                LocalizedText("Prayer alert tone", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            AlertToneRow("Fajr", fajr) { AzanAudioManager.clearAlert(fajr = true) }
            AlertToneRow("Dhuhr, Asr, Maghrib, Isha", other) { AzanAudioManager.clearAlert(fajr = false) }
            LocalizedText(
                if (alertsEnabled) "Tap the bell on an azan to play it when the prayer time enters. It stays quiet when the phone is on silent or vibrate."
                else "Tap the bell on an azan to choose it, then turn on prayer alerts in Settings to hear it at prayer time.",
                modifier = Modifier.padding(top = 4.dp, end = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AlertToneRow(prayers: String, track: AzanTrack?, onClear: () -> Unit) {
    val language = LocalAppLanguage.current
    // A minimum, not a fixed height: two lines of a tall script at a large font size need more.
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            LocalizedText(prayers, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(track?.fullName() ?: localizeUiText("Default notification sound", language),
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (track != null) IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("azan_screen", "clear_azan_alert", onClear)) {
            Icon(Icons.Default.Close, contentDescription = localizeUiText("Use default notification sound", language),
                tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun AzanRow(number: Int, track: AzanTrack, state: AzanAudioState) {
    val language = LocalAppLanguage.current
    val isActive = state.activeId == track.id
    val isDownloaded = track.id in state.downloaded
    val isDownloading = track.id in state.downloading
    val isAlert = track.id == state.fajrAlertId || track.id == state.otherAlertId
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("azan_${track.id}"),
        shape = MaterialTheme.shapes.large,
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            Modifier.clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("azan_screen", "play_azan", "tap", item = track.id); AzanAudioManager.play(track) }
                .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape)
                    .background(if (isActive) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isActive && state.buffering -> CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    isActive -> Icon(Icons.Default.Stop, contentDescription = localizeUiText("Stop", language),
                        tint = MaterialTheme.colorScheme.primary)
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
                    Text(track.details(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(
                onClick = { com.example.core.analytics.AppAnalytics.interaction("azan_screen", "toggle_azan_alert", "tap", item = track.id);
                    if (isAlert) AzanAudioManager.clearAlert(track.fajr) else AzanAudioManager.setAsAlert(track)
                },
                modifier = Modifier.testTag("azan_alert_${track.id}")
            ) {
                Icon(
                    if (isAlert) Icons.Default.NotificationsActive else Icons.Outlined.NotificationsNone,
                    contentDescription = "${localizeUiText(if (isAlert) "Use default notification sound" else "Use as prayer alert tone", language)} ${track.title}",
                    tint = if (isAlert) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                isDownloaded -> IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("azan_screen", "delete_azan_download", "tap", item = track.id); AzanAudioManager.delete(track) }) {
                    Icon(Icons.Default.DeleteOutline,
                        contentDescription = "${localizeUiText("Delete offline audio", language)} ${track.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("azan_screen", "download_azan", "tap", item = track.id); AzanAudioManager.download(track) }) {
                    Icon(Icons.Default.Download, contentDescription = "${localizeUiText("Download", language)} ${track.title}")
                }
            }
        }
    }
}

@Composable
private fun AzanErrorBanner(message: String) {
    val language = LocalAppLanguage.current
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.errorContainer) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            LocalizedText(message, modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("azan_screen", "dismiss_azan_error", AzanAudioManager::clearError)) {
                Icon(Icons.Default.Close, contentDescription = localizeUiText("Close", language), tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

