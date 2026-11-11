package com.example.ui.screens

import com.example.ui.tour.TourScreen
import com.example.ui.tour.tourTarget
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quran.FullQuranCatalog
import com.example.quran.QuranAudioManager
import com.example.quran.TafsirAudioCatalog
import com.example.quran.TafsirAudioManager
import com.example.quran.TafsirAudioState
import com.example.quran.TafsirAudioTrack
import com.example.ui.components.HeroSurface
import com.example.ui.components.PlaybackSpeedButton
import com.example.ui.components.islamicPattern
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import com.example.ui.localization.localizedUiTemplate
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.localizedUiText

@Composable
internal fun QuranListenLibrary(modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("quran_listen_library")

    var collection by rememberSaveable { mutableIntStateOf(0) }
    Column(modifier.background(MushafPaper)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).tourTarget("quranmodes_tafsir_collection")) {
            FilterChip(collection == 0, { collection = 0 }, label = { LocalizedText("Tilawat") }, colors = quranChipColors(),
                modifier = Modifier.testTag("quran_listen_tilawat"))
            Spacer(Modifier.width(8.dp))
            FilterChip(collection == 1, { collection = 1 }, label = { LocalizedText("Tafseer") }, colors = quranChipColors(),
                modifier = Modifier.testTag("quran_listen_tafseer"))
        }
        if (collection == 0) QuranAudioLibrary(Modifier.weight(1f))
        else TafsirAudioLibrary(Modifier.weight(1f))
    }
}

@Composable
private fun TafsirAudioLibrary(modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("tafsir_audio_library")
    TourScreen("quranmodes_tafsir_audio")

    val context = LocalContext.current
    // Asset parse + manager init run off the main thread; cached after the first visit so it is instant.
    val loadedTracks by produceState(
        initialValue = TafsirAudioCatalog.cachedTracks(),
        context
    ) {
        // init() is idempotent (returns at once after the first call); the catalog is only cached via init().
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            TafsirAudioManager.init(context)
            TafsirAudioCatalog.tracks(context)
        }
    }
    val tracks = loadedTracks ?: run {
        // Same container as the loaded screen, shown only for the first-ever load.
        Box(modifier.fillMaxSize().background(MushafPaper))
        return
    }
    val bySurah = remember(tracks) { tracks.groupBy { it.surah } }
    val state by TafsirAudioManager.state.collectAsState()
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("tafsir_audio_library", query)
    val language = LocalAppLanguage.current
    val surahs = remember(query, language) { com.example.quran.QuranSearch.surahs(query, language) }

    Column(modifier.background(MushafPaper)) {
        state.error?.let { error ->
            Snackbar(Modifier.padding(8.dp), action = {
                TextButton(TafsirAudioManager::clearError) { LocalizedText("OK") }
            }) { LocalizedText(error) }
        }
        LazyColumn(Modifier.weight(1f).testTag("tafsir_audio_list"), contentPadding = PaddingValues(bottom = 16.dp)) {
            state.resumeId?.let { id ->
                if (state.playingId == null) TafsirAudioManager.track(id)?.let { track ->
                    item { TafsirResumeCard(track, state.resumePositionMs) }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(mode == 0, { mode = 0 }, label = { LocalizedText("Surah wise") }, colors = quranChipColors(),
                        modifier = Modifier.tourTarget("quranmodes_tafsir_mode"))
                    Spacer(Modifier.width(8.dp))
                    FilterChip(mode == 1, { mode = 1 }, label = { LocalizedText("Para wise") }, colors = quranChipColors(),
                        modifier = Modifier.tourTarget("quranmodes_tafsir_mode"))
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.DownloadDone, null, tint = QuranMuted, modifier = Modifier.size(16.dp).tourTarget("quranmodes_tafsir_offline_count"))
                    Spacer(Modifier.width(4.dp))
                    LocalizedText("${tracks.count { it.id in state.downloaded }}/114", color = QuranMuted, fontSize = 12.sp)
                }
            }
            if (mode == 0) {
                item {
                    QuranSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        onClear = { com.example.core.analytics.AppAnalytics.interaction("tafsir_audio_library", "clear_tafsir_search", "tap"); query = "" },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).testTag("tafsir_audio_search").tourTarget("quranmodes_tafsir_search")
                    ) { LocalizedText("Search surah by name or number") }
                }
                if (surahs.isEmpty()) item {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, null, tint = MushafGold.copy(alpha = .7f), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        LocalizedText("No surah found", color = QuranMuted, textAlign = TextAlign.Center)
                    }
                }
                items(surahs, key = { it.number }) { surah ->
                    TafsirSurahRow(surah.number, bySurah[surah.number].orEmpty(), state)
                }
            } else {
                items(juzSurahRanges.indices.toList(), key = { "tafsir_para_$it" }) { index ->
                    TafsirParaRow(index + 1, juzSurahRanges[index].flatMap { bySurah[it].orEmpty() }, state)
                }
            }
        }
        if (state.playingId != null) TafsirNowPlaying(state)
    }
}

@Composable
private fun TafsirSurahRow(surah: Int, tracks: List<TafsirAudioTrack>, state: TafsirAudioState) {
    val context = LocalContext.current
    val meta = FullQuranCatalog.surahs[surah - 1]
    val active = tracks.any { it.id == state.playingId }
    val playable = tracks.filter { it.isStreamable }
    val downloading = playable.any { it.id in state.downloading }
    val downloaded = playable.isNotEmpty() && playable.all { it.id in state.downloaded }
    val resumable = !active && tracks.any { it.id == state.resumeId } && state.resumePositionMs > 5000
    val openVideo = {
        TafsirAudioManager.stop()
        QuranAudioManager.stop()
        tracks.firstOrNull()?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.sourceUrl))) }
        Unit
    }
    val play = {
        when {
            playable.isEmpty() -> openVideo()
            active -> TafsirAudioManager.toggle()
            resumable -> TafsirAudioManager.resumeLast()
            else -> TafsirAudioManager.playQueue(playable)
        }
    }
    val rowColor by animateColorAsState(if (active) QuranGoldLight.copy(alpha = .28f) else Color.Transparent, tween(220), label = "tafsirRow")
    Row(
        Modifier.fillMaxWidth()
            .background(rowColor)
            .clickable(role = Role.Button, onClick = com.example.core.analytics.AppAnalytics.trackItemAction("tafsir_audio_library", "play_tafsir_surah", "surah_$surah", play))
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
            .testTag("tafsir_audio_surah_$surah").tourTarget("quranmodes_tafsir_surah_row"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberBadge(surah, active)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            LocalizedText(meta.englishName, fontWeight = FontWeight.SemiBold, color = MushafInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LocalizedText(
                when {
                    playable.isEmpty() -> "Official video • Open to listen"
                    downloading -> "Downloading tafseer…"
                    resumable -> localizedUiTemplate("Resume from {time}", "time" to time(state.resumePositionMs))
                    tracks.size > 1 -> localizedUiTemplate("Sirat ul Jinan • {count} parts", "count" to tracks.size)
                    else -> localizedUiTemplate("{meaning} • Tafseer", "meaning" to localizedUiText(meta.englishMeaning))
                }, fontSize = 12.sp,
                color = if (downloading || resumable) MushafGold else QuranMuted, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (downloading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 4.dp), color = QuranGreen)
        }
        Spacer(Modifier.width(8.dp))
        Text(meta.arabicName, fontFamily = IndoPakFont, fontSize = 18.sp, color = QuranGreen)
        if (playable.isEmpty()) {
            // Same gap AudioRowActions leaves, so the Arabic name does not run into the button.
            Spacer(Modifier.width(4.dp))
            FilledTonalIconButton(openVideo, modifier = Modifier.testTag("tafsir_open_surah_$surah").tourTarget("quranmodes_tafsir_open_video"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = QuranGreen, contentColor = Color.White)) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, localizedUiText("Open official tafseer video"))
            }
            Spacer(Modifier.width(48.dp))
        } else AudioRowActions(active && state.isPlaying, downloading, downloaded, play,
            download = { TafsirAudioManager.downloadAll(playable) },
            delete = { playable.forEach(TafsirAudioManager::delete) },
            removeMessage = "This tafseer will still play online.",
            buffering = active && state.isBuffering && !state.isPlaying,
            cancelDownload = { TafsirAudioManager.cancelDownloads(playable) })
    }
    HorizontalDivider(Modifier.padding(start = 68.dp), color = MushafBorder.copy(alpha = .25f))
}

@Composable
private fun TafsirParaRow(para: Int, tracks: List<TafsirAudioTrack>, state: TafsirAudioState) {
    val playable = tracks.filter { it.isStreamable }
    val offline = playable.count { it.id in state.downloaded }
    val active = tracks.any { it.id == state.playingId }
    // Nothing to list when the tafseer catalogue could not be read.
    if (tracks.isEmpty()) return
    val play = { TafsirAudioManager.playQueue(playable) }
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = com.example.core.analytics.AppAnalytics.trackItemAction("tafsir_audio_library", "play_tafsir_para", "para_$para", play))
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
            .testTag("tafsir_audio_para_$para").tourTarget("quranmodes_tafsir_para_row"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberBadge(para, active)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            LocalizedText(localizedUiTemplate("Para {para}", "para" to para), fontWeight = FontWeight.SemiBold, color = MushafInk)
            Text("${localizedUiText(QuranAudioManager.surahName(tracks.first().surah))} – " +
                localizedUiText(QuranAudioManager.surahName(tracks.last().surah)),
                fontSize = 12.sp, color = QuranMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LocalizedText(
                if (offline == playable.size) "Available offline" else localizedUiTemplate("{done}/{total} offline", "done" to offline, "total" to playable.size),
                fontSize = 11.sp, color = if (offline == playable.size) Color(0xFF2E7D32) else QuranMuted
            )
            if (tracks.any { !it.isStreamable }) LocalizedText("Surah 83 video: Surah wise list",
                fontSize = 11.sp, color = MushafGold)
        }
        FilledTonalIconButton(play, modifier = Modifier.tourTarget("quranmodes_tafsir_para_play"),
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = QuranGreen, contentColor = Color.White)) {
            Icon(Icons.Default.PlayArrow, localizedUiText("Play Para"))
        }
        if (offline < playable.size) IconButton({ com.example.core.analytics.AppAnalytics.interaction("tafsir_audio_library", "download_tafsir_para", "tap", item = "para_$para"); TafsirAudioManager.downloadAll(playable) }, modifier = Modifier.tourTarget("quranmodes_tafsir_para_download")) {
            Icon(Icons.Default.Download, localizedUiText("Download Para"), tint = QuranGreen)
        } else Spacer(Modifier.width(48.dp))
    }
    HorizontalDivider(Modifier.padding(start = 68.dp), color = MushafBorder.copy(alpha = .25f))
}

@Composable
private fun TafsirResumeCard(track: TafsirAudioTrack, positionMs: Int) {
    HeroSurface(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp).tourTarget("quranmodes_tafsir_resume"),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(Color.White.copy(alpha = .12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.History, null, tint = QuranGoldLight, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Continue listening", fontSize = 12.sp, color = QuranGoldLight, fontWeight = FontWeight.SemiBold)
                LocalizedText(if (track.surah > 0) "${track.surah}. ${localizedUiText(QuranAudioManager.surahName(track.surah))}" else track.title,
                    fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LocalizedText(localizedUiTemplate("From {time}", "time" to time(positionMs)), fontSize = 12.sp, color = Color.White.copy(alpha = .78f))
            }
            Spacer(Modifier.width(10.dp))
            FilledIconButton(TafsirAudioManager::resumeLast,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = QuranGoldLight, contentColor = QuranGreen),
                modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.PlayArrow, localizedUiText("Resume"))
            }
        }
    }
}

@Composable
private fun TafsirNowPlaying(state: TafsirAudioState) {
    val track = TafsirAudioManager.track(state.playingId) ?: return
    Surface(color = QuranGreen, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), shadowElevation = 10.dp) {
        Column(
            Modifier
                .background(Brush.verticalGradient(listOf(com.example.ui.theme.HeroGradientTop, com.example.ui.theme.HeroGradientBottom)))
                .islamicPattern(Color.White.copy(alpha = 0.04f))
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    LocalizedText(if (state.isBuffering) "Loading tafseer audio…" else "Now playing • Tafseer",
                        color = QuranGoldLight, fontSize = 11.sp)
                    LocalizedText(if (track.surah > 0) "${track.surah}. ${localizedUiText(QuranAudioManager.surahName(track.surah))}" else track.title,
                        color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                PlaybackSpeedButton(state.speed, TafsirAudioManager::setSpeed, Modifier.tourTarget("quranmodes_tafsir_speed"), color = QuranGoldLight)
                Spacer(Modifier.width(10.dp))
                if (track.surah > 0) Text(FullQuranCatalog.surahs[track.surah - 1].arabicName,
                    color = QuranGoldLight, fontFamily = IndoPakFont, fontSize = 20.sp)
            }
            Slider(
                value = state.positionMs.toFloat().coerceIn(0f, state.durationMs.coerceAtLeast(1).toFloat()),
                onValueChange = { TafsirAudioManager.seekTo(it.toInt()) },
                onValueChangeFinished = { com.example.core.analytics.AppAnalytics.media("tafsir_audio", "seek") },
                valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
                colors = SliderDefaults.colors(thumbColor = QuranGoldLight, activeTrackColor = QuranGoldLight,
                    inactiveTrackColor = Color.White.copy(alpha = .25f)),
                modifier = Modifier.fillMaxWidth().height(28.dp).tourTarget("quranmodes_tafsir_seek")
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LocalizedText(time(state.positionMs), color = QuranGoldLight, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                IconButton(TafsirAudioManager::stop, Modifier.tourTarget("quranmodes_tafsir_stop")) { Icon(Icons.Default.Stop, localizedUiText("Stop"), tint = Color.White) }
                IconButton(TafsirAudioManager::previous, Modifier.tourTarget("quranmodes_tafsir_previous")) {
                    Icon(Icons.Default.SkipPrevious, localizedUiText("Previous surah"), tint = Color.White)
                }
                FilledIconButton(TafsirAudioManager::toggle,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = QuranGoldLight, contentColor = QuranGreen),
                    modifier = Modifier.size(48.dp).tourTarget("quranmodes_tafsir_play_pause")) {
                    Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        localizedUiText(if (state.isPlaying) "Pause" else "Play"))
                }
                IconButton(TafsirAudioManager::next, Modifier.tourTarget("quranmodes_tafsir_next")) {
                    Icon(Icons.Default.SkipNext, localizedUiText("Next surah"), tint = Color.White)
                }
                Spacer(Modifier.width(48.dp))
                Spacer(Modifier.weight(1f))
                LocalizedText(time(state.durationMs), color = QuranGoldLight, fontSize = 11.sp)
            }
        }
    }
}

private fun time(ms: Int): String = "%d:%02d".format(java.util.Locale.ROOT, (ms / 1000).coerceAtLeast(0) / 60, (ms / 1000).coerceAtLeast(0) % 60)
