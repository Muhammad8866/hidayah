package com.example.ui.screens

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
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText

@Composable
internal fun QuranListenLibrary(modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("quran_listen_library")

    var collection by rememberSaveable { mutableIntStateOf(0) }
    Column(modifier.background(MushafPaper)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
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

    val context = LocalContext.current
    val allTracks = remember(context) {
        TafsirAudioManager.init(context)
        TafsirAudioCatalog.tracks(context)
    }
    val tracks = allTracks
    val bySurah = remember(tracks) { tracks.groupBy { it.surah } }
    val state by TafsirAudioManager.state.collectAsState()
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("quran_tafsir_audio_screen", query)
    val language = LocalAppLanguage.current
    val surahs = remember(query, language) {
        val q = query.trim()
        if (q.isEmpty()) FullQuranCatalog.surahs
        else FullQuranCatalog.surahs.filter {
            it.number.toString() == q || it.englishName.contains(q, true) ||
                it.englishMeaning.contains(q, true) || it.arabicName.contains(q) ||
                localizeUiText(it.englishName, language).contains(q, true) ||
                localizeUiText(it.englishMeaning, language).contains(q, true)
        }
    }

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
                    FilterChip(mode == 0, { mode = 0 }, label = { LocalizedText("Surah wise") }, colors = quranChipColors())
                    Spacer(Modifier.width(8.dp))
                    FilterChip(mode == 1, { mode = 1 }, label = { LocalizedText("Para wise") }, colors = quranChipColors())
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.DownloadDone, null, tint = QuranMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    LocalizedText("${tracks.count { it.id in state.downloaded }}/114", color = QuranMuted, fontSize = 12.sp)
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText("Sirat ul Jinan • Urdu • Dawat-e-Islami", Modifier.weight(1f),
                        color = QuranMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    TextButton({ com.example.core.analytics.AppAnalytics.interaction("quran_tafsir_audio_screen", "open_tafsir_source", "tap"); context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.dawateislami.net/medialibrary/category/35/?category=18"))) },
                        colors = ButtonDefaults.textButtonColors(contentColor = QuranGreen)) {
                        LocalizedText("Source")
                    }
                }
            }
            if (mode == 0) {
                item {
                    QuranSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        onClear = { com.example.core.analytics.AppAnalytics.interaction("quran_tafsir_audio_screen", "clear_tafsir_search", "tap"); query = "" },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).testTag("tafsir_audio_search")
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
            .clickable(role = Role.Button, onClick = com.example.core.analytics.AppAnalytics.trackItemAction("quran_tafsir_audio_screen", "play_tafsir_surah", "surah_$surah", play))
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
            .testTag("tafsir_audio_surah_$surah"),
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
                    resumable -> "Resume from ${time(state.resumePositionMs)}"
                    tracks.size > 1 -> "Sirat ul Jinan • ${tracks.size} parts"
                    else -> "${meta.englishMeaning} • Tafseer"
                }, fontSize = 12.sp,
                color = if (downloading || resumable) MushafGold else QuranMuted, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (downloading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 4.dp), color = QuranGreen)
        }
        Spacer(Modifier.width(8.dp))
        Text(meta.arabicName, fontFamily = IndoPakFont, fontSize = 18.sp, color = QuranGreen)
        if (playable.isEmpty()) {
            FilledTonalIconButton(openVideo, modifier = Modifier.testTag("tafsir_open_surah_$surah"),
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
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = com.example.core.analytics.AppAnalytics.trackItemAction("quran_tafsir_audio_screen", "play_tafsir_para", "para_$para", play))
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
            .testTag("tafsir_audio_para_$para"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberBadge(para, active)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            LocalizedText("Para $para", fontWeight = FontWeight.SemiBold, color = MushafInk)
            LocalizedText("${QuranAudioManager.surahName(tracks.first().surah)} – " +
                QuranAudioManager.surahName(tracks.last().surah),
                fontSize = 12.sp, color = QuranMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LocalizedText(
                if (offline == playable.size) "Available offline" else "$offline/${playable.size} offline",
                fontSize = 11.sp, color = if (offline == playable.size) Color(0xFF2E7D32) else QuranMuted
            )
            if (tracks.any { !it.isStreamable }) LocalizedText("Surah 83 video: Surah wise list",
                fontSize = 11.sp, color = MushafGold)
        }
        FilledTonalIconButton(play,
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = QuranGreen, contentColor = Color.White)) {
            Icon(Icons.Default.PlayArrow, localizedUiText("Play Para"))
        }
        if (offline < playable.size) IconButton({ com.example.core.analytics.AppAnalytics.interaction("quran_tafsir_audio_screen", "download_tafsir_para", "tap", item = "para_$para"); TafsirAudioManager.downloadAll(playable) }) {
            Icon(Icons.Default.Download, localizedUiText("Download Para"), tint = QuranGreen)
        } else Spacer(Modifier.width(48.dp))
    }
    HorizontalDivider(Modifier.padding(start = 68.dp), color = MushafBorder.copy(alpha = .25f))
}

@Composable
private fun TafsirResumeCard(track: TafsirAudioTrack, positionMs: Int) {
    HeroSurface(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp),
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
                LocalizedText(if (track.surah > 0) "${track.surah}. ${QuranAudioManager.surahName(track.surah)}" else track.title,
                    fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LocalizedText("From ${time(positionMs)}", fontSize = 12.sp, color = Color.White.copy(alpha = .78f))
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
                    LocalizedText(if (track.surah > 0) "${track.surah}. ${QuranAudioManager.surahName(track.surah)}" else track.title,
                        color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                PlaybackSpeedButton(state.speed, TafsirAudioManager::setSpeed, color = QuranGoldLight)
                Spacer(Modifier.width(10.dp))
                if (track.surah > 0) Text(FullQuranCatalog.surahs[track.surah - 1].arabicName,
                    color = QuranGoldLight, fontFamily = IndoPakFont, fontSize = 20.sp)
            }
            Slider(
                value = state.positionMs.toFloat().coerceIn(0f, state.durationMs.coerceAtLeast(1).toFloat()),
                onValueChange = { TafsirAudioManager.seekTo(it.toInt()) },
                valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
                colors = SliderDefaults.colors(thumbColor = QuranGoldLight, activeTrackColor = QuranGoldLight,
                    inactiveTrackColor = Color.White.copy(alpha = .25f)),
                modifier = Modifier.fillMaxWidth().height(28.dp)
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LocalizedText(time(state.positionMs), color = QuranGoldLight, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                IconButton(TafsirAudioManager::stop) { Icon(Icons.Default.Stop, localizedUiText("Stop"), tint = Color.White) }
                IconButton(TafsirAudioManager::previous) {
                    Icon(Icons.Default.SkipPrevious, localizedUiText("Previous surah"), tint = Color.White)
                }
                FilledIconButton(TafsirAudioManager::toggle,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = QuranGoldLight, contentColor = QuranGreen),
                    modifier = Modifier.size(48.dp)) {
                    Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        localizedUiText(if (state.isPlaying) "Pause" else "Play"))
                }
                IconButton(TafsirAudioManager::next) {
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
