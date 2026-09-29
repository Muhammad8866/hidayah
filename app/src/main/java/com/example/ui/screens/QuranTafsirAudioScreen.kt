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
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quran.FullQuranCatalog
import com.example.quran.QuranAudioManager
import com.example.quran.TafsirAudioCatalog
import com.example.quran.TafsirAudioManager
import com.example.quran.TafsirAudioState
import com.example.quran.TafsirAudioTrack
import com.example.ui.components.PlaybackSpeedButton
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText

@Composable
internal fun QuranListenLibrary(modifier: Modifier = Modifier) {
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
    val context = LocalContext.current
    val tracks = remember(context) {
        TafsirAudioManager.init(context)
        TafsirAudioCatalog.tracks(context)
    }
    val bySurah = remember(tracks) { tracks.groupBy { it.surah } }
    val state by TafsirAudioManager.state.collectAsState()
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
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
                    LocalizedText("${state.downloaded.size}/114", color = QuranMuted, fontSize = 12.sp)
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText("Sirat ul Jinan • Urdu • Dawat-e-Islami", Modifier.weight(1f),
                        color = QuranMuted, fontSize = 11.sp, maxLines = 1)
                    TextButton({ context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.dawateislami.net/medialibrary/category/35/?category=18"))) }) {
                        LocalizedText("Source")
                    }
                }
            }
            if (mode == 0) {
                item {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("tafsir_audio_search"),
                        placeholder = { LocalizedText("Search surah by name or number") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = { if (query.isNotEmpty()) IconButton({ query = "" }) {
                            Icon(Icons.Default.Close, localizedUiText("Clear"))
                        } },
                        singleLine = true, shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QuranGreen, unfocusedBorderColor = MushafBorder.copy(alpha = .6f),
                            focusedContainerColor = QuranCard, unfocusedContainerColor = QuranCard
                        )
                    )
                }
                if (surahs.isEmpty()) item {
                    LocalizedText("No surah found", Modifier.fillMaxWidth().padding(32.dp),
                        color = QuranMuted, textAlign = TextAlign.Center)
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
    Row(
        Modifier.fillMaxWidth()
            .background(if (active) QuranGoldLight.copy(alpha = .25f) else Color.Transparent)
            .clickable(role = Role.Button, onClick = play)
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
            .testTag("tafsir_audio_surah_$surah"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberBadge(surah, active)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            LocalizedText(meta.englishName, fontWeight = FontWeight.SemiBold, color = MushafInk, maxLines = 1)
            LocalizedText(
                when {
                    playable.isEmpty() -> "Official video • Open to listen"
                    downloading -> "Downloading tafseer…"
                    resumable -> "Resume from ${time(state.resumePositionMs)}"
                    tracks.size > 1 -> "Sirat ul Jinan • ${tracks.size} parts"
                    else -> "${meta.englishMeaning} • Tafseer"
                }, fontSize = 12.sp,
                color = if (downloading || resumable) MushafGold else QuranMuted, maxLines = 1
            )
            if (downloading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 4.dp), color = QuranGreen)
        }
        Spacer(Modifier.width(8.dp))
        Text(meta.arabicName, fontFamily = IndoPakFont, fontSize = 18.sp, color = QuranGreen)
        if (playable.isEmpty()) {
            FilledTonalIconButton(openVideo, modifier = Modifier.testTag("tafsir_open_surah_$surah"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = QuranGreen, contentColor = Color.White)) {
                Icon(Icons.Default.OpenInNew, "Open official tafseer video")
            }
            Spacer(Modifier.width(48.dp))
        } else AudioRowActions(active && state.isPlaying, downloading, downloaded, play,
            download = { TafsirAudioManager.downloadAll(playable) },
            delete = { playable.forEach(TafsirAudioManager::delete) },
            removeMessage = "This tafseer will still play online.")
    }
    HorizontalDivider(Modifier.padding(start = 68.dp), color = MushafBorder.copy(alpha = .25f))
}

@Composable
private fun TafsirParaRow(para: Int, tracks: List<TafsirAudioTrack>, state: TafsirAudioState) {
    val playable = tracks.filter { it.isStreamable }
    val offline = playable.count { it.id in state.downloaded }
    val active = tracks.any { it.id == state.playingId }
    val play = { TafsirAudioManager.playQueue(playable) }
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = play)
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
                fontSize = 12.sp, color = QuranMuted, maxLines = 1)
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
        if (offline < playable.size) IconButton({ TafsirAudioManager.downloadAll(playable) }) {
            Icon(Icons.Default.Download, localizedUiText("Download Para"), tint = QuranGreen)
        } else Spacer(Modifier.width(48.dp))
    }
    HorizontalDivider(Modifier.padding(start = 68.dp), color = MushafBorder.copy(alpha = .25f))
}

@Composable
private fun TafsirResumeCard(track: TafsirAudioTrack, positionMs: Int) {
    Surface(
        color = Color(0xFFF3E4B9), modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp),
        shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MushafGold.copy(alpha = .4f))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, null, tint = QuranGreen, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Continue listening", fontSize = 12.sp, color = QuranMuted)
                LocalizedText("${track.surah}. ${QuranAudioManager.surahName(track.surah)}",
                    fontWeight = FontWeight.Bold, color = QuranGreen, maxLines = 1)
                LocalizedText("From ${time(positionMs)}", fontSize = 12.sp, color = MushafInk)
            }
            FilledIconButton(TafsirAudioManager::resumeLast,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = QuranGreen, contentColor = Color.White),
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
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), shadowElevation = 8.dp) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    LocalizedText(if (state.isBuffering) "Loading tafseer audio…" else "Now playing • Tafseer",
                        color = QuranGoldLight, fontSize = 11.sp)
                    LocalizedText("${track.surah}. ${QuranAudioManager.surahName(track.surah)}",
                        color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                PlaybackSpeedButton(state.speed, TafsirAudioManager::setSpeed, color = QuranGoldLight)
                Spacer(Modifier.width(10.dp))
                Text(FullQuranCatalog.surahs[track.surah - 1].arabicName,
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

private fun time(ms: Int): String = "%d:%02d".format((ms / 1000).coerceAtLeast(0) / 60, (ms / 1000).coerceAtLeast(0) % 60)
