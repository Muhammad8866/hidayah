@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.domain.model.Surah
import com.example.quran.FullQuranCatalog
import com.example.quran.RootInfo
import com.example.quran.SurahAudio
import com.example.quran.WbwAudioDownloads
import com.example.quran.WbwAyah
import com.example.quran.WbwDownloadState
import com.example.quran.WbwIndex
import com.example.quran.WbwLanguage
import com.example.quran.WbwWord
import com.example.quran.WordByWordRepository
import com.example.core.audio.PlaybackSpeed
import com.example.ui.components.PlaybackSpeedButton
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.persistedAppLanguage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private val LearnedGreen = Color(0xFF2E7D4F)
private val LearnedTint = Color(0xFFE6F2EA)
private const val BISMILLAH = "بِسۡمِ اللّٰهِ الرَّحۡمٰنِ الرَّحِيۡمِ"

private const val ARABIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"
private fun Int.toArabicDigits(): String = toString().map { ARABIC_DIGITS[it - '0'] }.joinToString("")
private fun Int.grouped(): String = "%,d".format(this)
private fun Long.asSize(): String = when {
    this >= 1_000_000_000L -> "%.1f GB".format(this / 1e9)
    this >= 1_000_000L -> "${(this / 1e6).toInt().coerceAtLeast(1)} MB"
    else -> "${(this / 1000).coerceAtLeast(1)} KB"
}

/** Reader preferences and learned vocabulary, persisted across sessions. */
private class WbwState(context: Context, appLanguage: String) {
    private val prefs = context.getSharedPreferences("quran_wbw", Context.MODE_PRIVATE)
    var learned by mutableStateOf(prefs.getStringSet("learned", emptySet())!!.toSet()); private set
    var lastSurah by mutableIntStateOf(prefs.getInt("last_surah", 0)); private set
    var lastAyah by mutableIntStateOf(prefs.getInt("last_ayah", 1)); private set
    var language by mutableStateOf(
        prefs.getString("language", null)?.let { runCatching { WbwLanguage.valueOf(it) }.getOrNull() } ?: WbwLanguage.forAppLanguage(appLanguage)
    ); private set
    var showMeaning by mutableStateOf(prefs.getBoolean("show_meaning", true)); private set
    var showTransliteration by mutableStateOf(prefs.getBoolean("show_translit", false)); private set
    var showTranslation by mutableStateOf(prefs.getBoolean("show_translation", appLanguage == "ur")); private set
    var hideLearnedMeanings by mutableStateOf(prefs.getBoolean("hide_learned", false)); private set
    var continuousPlay by mutableStateOf(prefs.getBoolean("continuous", true)); private set
    var arabicSize by mutableFloatStateOf(prefs.getFloat("arabic_size", 26f)); private set
    var playbackSpeed by mutableFloatStateOf(PlaybackSpeed.sanitize(prefs.getFloat("speed", PlaybackSpeed.NORMAL))); private set

    fun isLearned(word: WbwWord) = word.key in learned
    fun setLearned(word: WbwWord, value: Boolean) {
        learned = if (value) learned + word.key else learned - word.key
        prefs.edit().putStringSet("learned", learned).apply()
    }
    fun savePosition(surah: Int, ayah: Int) {
        if (surah == lastSurah && ayah == lastAyah) return
        lastSurah = surah; lastAyah = ayah
        prefs.edit().putInt("last_surah", surah).putInt("last_ayah", ayah).apply()
    }
    fun updateLanguage(value: WbwLanguage) { language = value; prefs.edit().putString("language", value.name).apply() }
    fun updateShowMeaning(value: Boolean) { showMeaning = value; prefs.edit().putBoolean("show_meaning", value).apply() }
    fun updateShowTransliteration(value: Boolean) { showTransliteration = value; prefs.edit().putBoolean("show_translit", value).apply() }
    fun updateShowTranslation(value: Boolean) { showTranslation = value; prefs.edit().putBoolean("show_translation", value).apply() }
    fun updateHideLearned(value: Boolean) { hideLearnedMeanings = value; prefs.edit().putBoolean("hide_learned", value).apply() }
    fun updateContinuousPlay(value: Boolean) { continuousPlay = value; prefs.edit().putBoolean("continuous", value).apply() }
    fun updateArabicSize(value: Float) { arabicSize = value; prefs.edit().putFloat("arabic_size", value).apply() }
    fun updatePlaybackSpeed(value: Float) { playbackSpeed = value; prefs.edit().putFloat("speed", value).apply() }

    /** Share of all word occurrences in the Quran whose dictionary form the reader has learned. */
    fun coverage(index: WbwIndex): Float =
        if (index.totalOccurrences == 0) 0f else learned.sumOf { index.lemmaCounts[it] ?: 0 } / index.totalOccurrences.toFloat()
}

/**
 * Plays the surah recitation (Alafasy) and follows it word by word using the bundled timings.
 * The downloaded file is used when present, otherwise the same file is streamed. A single word is
 * pronounced from its own clearly recorded clip; cutting it out of the flowing recitation (merged
 * sounds, clipped edges) is only the offline fallback.
 */
private class RecitationPlayer(private val context: Context) {
    var currentWord by mutableStateOf<String?>(null); private set
    var playingAyah by mutableStateOf<String?>(null); private set
    var buffering by mutableStateOf(false); private set
    var continuous = true
    /** Word highlighting follows the media position, so it stays in sync at any speed. */
    var speed = PlaybackSpeed.NORMAL
        set(value) {
            if (field == value) return
            field = value
            player?.setPlaybackSpeed(value); single?.setPlaybackSpeed(value)
        }

    private var surah = 0
    private var ayahs: List<WbwAyah> = emptyList()
    private var remoteUrls: List<String> = emptyList()
    private var remoteChoice = 0
    private var player: ExoPlayer? = null
    private var playerSource: String? = null
    private var ayahIndex = -1
    private var stopAtMs = Int.MAX_VALUE
    private var single: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            tick()
            if (playingAyah != null || currentWord != null) handler.postDelayed(this, 40)
        }
    }

    fun bind(surah: Int, ayahs: List<WbwAyah>, audio: SurahAudio?) {
        val urls = audio?.urls.orEmpty()
        if (this.surah == surah && this.ayahs === ayahs && remoteUrls == urls) return
        release()
        this.surah = surah; this.ayahs = ayahs; remoteUrls = urls; remoteChoice = 0
    }

    fun toggleAyah(ayah: WbwAyah) {
        if (playingAyah == ayah.key) { stop(); return }
        stop()
        ayahIndex = ayahs.indexOfFirst { it.key == ayah.key }
        playingAyah = ayah.key
        play(ayah.startMs)
    }

    fun playWord(word: WbwWord) {
        stop()
        currentWord = word.id
        playSingle(word)
    }

    private fun playFromRecitation(word: WbwWord) {
        stopAtMs = word.endMs
        play(word.startMs)
    }

    private fun play(fromMs: Int) {
        val source = WbwAudioDownloads.localFile(surah)?.let { Uri.fromFile(it).toString() } ?: remoteUrls.getOrNull(remoteChoice) ?: return stop()
        val p = player?.takeIf { playerSource == source } ?: createPlayer(source)
        // ExoPlayer seeks CBR MP3s by exact byte offset, so the reported position matches the audio.
        p.seekTo(fromMs.toLong())
        p.playWhenReady = true
        startTicker()
    }

    private fun createPlayer(source: String): ExoPlayer {
        releasePlayer()
        val local = !source.startsWith("http")
        playerSource = source
        return ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), false)
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    buffering = state == Player.STATE_BUFFERING && playWhenReady
                    if (state == Player.STATE_ENDED) stop()
                }

                override fun onPlayerError(error: PlaybackException) {
                    val resumeAt = currentPosition.toInt()
                    releasePlayer()
                    if (!local && remoteChoice < remoteUrls.lastIndex) {
                        // This mirror failed; carry on from the next one without interrupting the user.
                        remoteChoice++
                        if (playingAyah != null || currentWord != null) play(resumeAt)
                        return
                    }
                    toast(if (local) "This download could not be played. Delete it and download again." else "No internet. Download this surah to listen offline.")
                    stop()
                }
            })
            setMediaItem(MediaItem.fromUri(source))
            setPlaybackSpeed(speed)
            prepare()
        }.also { player = it }
    }

    private fun tick() {
        val p = player ?: return
        if (!p.isPlaying) return
        val pos = p.currentPosition.toInt()
        if (pos >= stopAtMs) { stop(); return }
        if (playingAyah == null) return
        var ayah = ayahs.getOrNull(ayahIndex) ?: return stop()
        if (pos >= ayah.endMs) {
            if (continuous && ayahIndex < ayahs.lastIndex) {
                ayahIndex++; ayah = ayahs[ayahIndex]; playingAyah = ayah.key
            } else { stop(); return }
        }
        // Between two words keep the previous one lit so the highlight doesn't flicker.
        ayah.words.firstOrNull { it.isTimed && pos >= it.startMs && pos < it.endMs }?.let { currentWord = it.id }
    }

    private fun playSingle(word: WbwWord, source: Int = 0) {
        val url = word.audioUrls.getOrNull(source) ?: return stop()
        val clip = ExoPlayer.Builder(context).build()
        single = clip
        clip.setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), false)
        clip.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (single !== clip) return
                buffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_ENDED) stop()
            }

            override fun onPlayerError(error: PlaybackException) {
                if (single !== clip) return
                clip.release(); single = null
                when {
                    source < word.audioUrls.lastIndex -> playSingle(word, source + 1)
                    word.isTimed && WbwAudioDownloads.localFile(surah) != null -> playFromRecitation(word)
                    else -> { toast("No internet. This word's audio needs a connection."); stop() }
                }
            }
        })
        clip.setMediaItem(MediaItem.fromUri(url))
        clip.setPlaybackSpeed(speed)
        clip.prepare()
        clip.playWhenReady = true
    }

    private fun startTicker() { handler.removeCallbacks(ticker); handler.post(ticker) }

    fun stop() {
        handler.removeCallbacks(ticker)
        player?.playWhenReady = false
        single?.release(); single = null
        currentWord = null; playingAyah = null; buffering = false
        stopAtMs = Int.MAX_VALUE
    }

    private fun releasePlayer() {
        player?.release(); player = null; playerSource = null
    }

    fun release() { stop(); releasePlayer() }

    private fun toast(message: String) =
        Toast.makeText(context, localizeUiText(message, persistedAppLanguage(context)), Toast.LENGTH_SHORT).show()
}

/** A pending download or delete the user has to confirm. */
private data class AudioRequest(val title: String, val surahs: List<Int>, val bytes: Long, val delete: Boolean)

@Composable
internal fun QuranWordLearningScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appLanguage = LocalAppLanguage.current
    val state = remember(context) { WbwState(context, appLanguage) }
    val player = remember(context) { RecitationPlayer(context) }
    DisposableEffect(player) { onDispose { player.release() } }
    remember(context) { WbwAudioDownloads.init(context) }
    val downloads by WbwAudioDownloads.state.collectAsState()
    val index by produceState<WbwIndex?>(null) { value = WordByWordRepository.index(context) }
    var request by remember { mutableStateOf<AudioRequest?>(null) }

    var openSurah by rememberSaveable { mutableIntStateOf(0) }
    var startAyah by rememberSaveable { mutableIntStateOf(1) }
    fun open(surah: Int, ayah: Int) { player.stop(); startAyah = ayah; openSurah = surah }
    BackHandler(openSurah != 0) { open(0, 1) }

    Box(modifier.fillMaxSize().background(MushafPaper)) {
        if (openSurah == 0) {
            SurahPicker(state, index, downloads, ::open, onRequest = { request = it })
        } else key(openSurah) {
            SurahReader(
                openSurah, startAyah, state, index, downloads, player,
                onBack = { open(0, 1) }, onOpenSurah = { open(it, 1) }, onRequest = { request = it }
            )
        }
    }

    request?.let { r ->
        AudioRequestDialog(r, onDismiss = { request = null }) {
            request = null
            if (r.delete) WbwAudioDownloads.delete(r.surahs)
            else index?.let { WbwAudioDownloads.enqueue(r.surahs, it.audio) }
        }
    }
}

// ---------------------------------------------------------------- Surah, Juz & Downloads lists

@Composable
private fun SurahPicker(
    state: WbwState, index: WbwIndex?, downloads: WbwDownloadState,
    onOpen: (surah: Int, ayah: Int) -> Unit, onRequest: (AudioRequest) -> Unit
) {
    val surahs = FullQuranCatalog.surahs
    var query by rememberSaveable { mutableStateOf("") }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val needle = query.trim().lowercase()
    val filtered = remember(needle) {
        val plain = needle.replace("-", " ").replace("'", "")
        if (needle.isEmpty()) surahs else surahs.filter {
            it.number.toString() == needle || it.arabicName.contains(needle) || it.englishMeaning.lowercase().contains(needle) ||
                it.englishName.lowercase().replace("-", " ").replace("'", "").contains(plain)
        }
    }
    fun surahRequest(surah: Surah, delete: Boolean) = AudioRequest(
        "Surah ${surah.englishName}", listOf(surah.number), index?.audio?.get(surah.number - 1)?.bytes ?: 0, delete
    )
    val surahRow: @Composable (Surah) -> Unit = { surah ->
        SurahRow(
            surah, index?.audio?.get(surah.number - 1)?.bytes, downloads,
            onClick = { onOpen(surah.number, 1) },
            onDownload = { onRequest(surahRequest(surah, delete = false)) },
            onCancel = { WbwAudioDownloads.cancel(listOf(surah.number)) },
            onDelete = { onRequest(surahRequest(surah, delete = true)) }
        )
    }

    LazyColumn(
        Modifier.fillMaxSize().testTag("quran_word_learning"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ProgressHeader(state, index) }
        if (state.lastSurah in 1..114) {
            item {
                val surah = surahs[state.lastSurah - 1]
                Surface(
                    onClick = { onOpen(surah.number, state.lastAyah) },
                    color = QuranGoldLight.copy(alpha = .55f), shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MushafGold.copy(alpha = .5f)),
                    modifier = Modifier.fillMaxWidth().testTag("quran_wbw_continue")
                ) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            LocalizedText("Continue learning", color = QuranMuted, fontSize = 12.sp)
                            LocalizedText("Surah ${surah.englishName} · Ayah ${state.lastAyah}", color = QuranGreen, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = QuranGreen)
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                query, { query = it }, Modifier.fillMaxWidth().testTag("quran_wbw_search"),
                placeholder = { LocalizedText("Search surah name or number") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = if (query.isNotEmpty()) {{ IconButton({ query = "" }) { Icon(Icons.Default.Close, "Clear") } }} else null,
                singleLine = true, shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = QuranCard, unfocusedContainerColor = QuranCard, focusedBorderColor = QuranGreen)
            )
        }
        if (needle.isEmpty()) {
            item {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("Surah", "Juz", "Downloads").forEachIndexed { i, label ->
                        SegmentedButton(
                            selected = tab == i, onClick = { tab = i },
                            shape = SegmentedButtonDefaults.itemShape(i, 3),
                            colors = SegmentedButtonDefaults.colors(activeContainerColor = QuranGoldLight, activeContentColor = QuranGreen),
                            modifier = Modifier.testTag("quran_wbw_tab_$i")
                        ) { LocalizedText(label, fontWeight = FontWeight.SemiBold, maxLines = 1) }
                    }
                }
            }
        }
        when {
            needle.isNotEmpty() || tab == 0 -> {
                if (filtered.isEmpty()) item {
                    LocalizedText("No surah found.", color = QuranMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp))
                }
                items(filtered, key = { "s${it.number}" }) { surahRow(it) }
            }
            tab == 1 -> {
                val starts = index?.juzStarts.orEmpty()
                items(starts.size, key = { "juz$it" }) { i ->
                    val juz = i + 1
                    val (s, a) = starts[i]
                    val members = index!!.surahsInJuz(juz)
                    val bytes = members.sumOf { index.audio[it - 1].bytes }
                    fun juzRequest(delete: Boolean) = AudioRequest(
                        "Juz $juz", if (delete) members.filter(downloads::isDownloaded) else members,
                        if (delete) members.filter(downloads::isDownloaded).sumOf { index.audio[it - 1].bytes } else members.filterNot(downloads::isDownloaded).sumOf { index.audio[it - 1].bytes },
                        delete
                    )
                    JuzRow(
                        juz, surahs[s - 1], a, members, bytes, downloads, index,
                        onClick = { onOpen(s, a) },
                        onDownload = { onRequest(juzRequest(delete = false)) },
                        onCancel = { WbwAudioDownloads.cancel(members) },
                        onDelete = { onRequest(juzRequest(delete = true)) }
                    )
                }
            }
            else -> {
                item { DownloadsSummary(downloads, index, onRequest) }
                val mine = surahs.filter { downloads.isDownloaded(it.number) || downloads.isActive(it.number) }
                if (mine.isEmpty()) item {
                    LocalizedText(
                        "No audio downloaded yet. Tap the download button next to any surah or juz to listen without internet.",
                        color = QuranMuted, textAlign = TextAlign.Center, fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 20.dp)
                    )
                }
                items(mine, key = { "d${it.number}" }) { surahRow(it) }
            }
        }
    }
}

@Composable private fun ProgressHeader(state: WbwState, index: WbwIndex?) {
    val coverage = index?.let(state::coverage) ?: 0f
    val percent = coverage * 100
    Surface(color = QuranGreen, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    LocalizedText("Word by word Quran", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    LocalizedText("Text works offline · audio online or downloaded", color = QuranGoldLight, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (percent > 0f && percent < 1f) "<1%" else "${percent.toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    LocalizedText("of Quran words", color = QuranGoldLight, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { coverage }, Modifier.fillMaxWidth().height(6.dp),
                color = QuranGoldLight, trackColor = Color.White.copy(alpha = .18f), strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(6.dp))
            LocalizedText(
                if (index == null) "Loading…"
                else "${state.learned.size.grouped()} of ${index.vocabularySize.grouped()} words learned. Tap a word and press “I know this”.",
                color = Color.White.copy(alpha = .8f), fontSize = 11.sp
            )
        }
    }
}

@Composable private fun DownloadsSummary(downloads: WbwDownloadState, index: WbwIndex?, onRequest: (AudioRequest) -> Unit) {
    val remaining = (1..114).filterNot(downloads::isDownloaded)
    Surface(color = QuranCard, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MushafBorder.copy(alpha = .3f))) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.OfflinePin, null, tint = LearnedGreen)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    LocalizedText("${downloads.downloaded.size} of 114 surahs offline", color = MushafInk, fontWeight = FontWeight.SemiBold)
                    LocalizedText("${downloads.usedBytes.asSize()} used on this phone · Mishary Alafasy", color = QuranMuted, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (index != null && remaining.isNotEmpty()) {
                    OutlinedButton(
                        { onRequest(AudioRequest("the whole Quran", remaining, remaining.sumOf { index.audio[it - 1].bytes }, delete = false)) },
                        Modifier.weight(1f)
                    ) { Icon(Icons.Default.Download, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); LocalizedText("Download all", maxLines = 1) }
                }
                if (downloads.downloaded.isNotEmpty()) {
                    OutlinedButton(
                        { onRequest(AudioRequest("all downloads", downloads.downloaded.toList(), downloads.usedBytes, delete = true)) },
                        Modifier.weight(1f)
                    ) { Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); LocalizedText("Delete all", maxLines = 1) }
                }
            }
        }
    }
}

@Composable private fun SurahRow(
    surah: Surah, bytes: Long?, downloads: WbwDownloadState,
    onClick: () -> Unit, onDownload: () -> Unit, onCancel: () -> Unit, onDelete: () -> Unit
) {
    val downloaded = downloads.isDownloaded(surah.number)
    Surface(onClick = onClick, color = QuranCard, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MushafBorder.copy(alpha = .25f)), modifier = Modifier.fillMaxWidth().testTag("quran_wbw_surah_${surah.number}")) {
        Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(QuranGoldLight.copy(alpha = .45f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Text("${surah.number}", color = QuranGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(surah.englishName, color = MushafInk, fontWeight = FontWeight.SemiBold, maxLines = 1)
                LocalizedText("${surah.englishMeaning} · ${surah.totalAyat} ayat", color = QuranMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (downloaded) LocalizedText("Audio offline", color = LearnedGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                else if (bytes != null && !downloads.isActive(surah.number)) Text("Audio ${bytes.asSize()}", color = QuranMuted, fontSize = 11.sp)
            }
            Text(surah.arabicName, fontFamily = IndoPakFont, fontSize = 21.sp, color = QuranGreen)
            DownloadControl(downloaded, downloads.active[surah.number], onDownload, onCancel, onDelete, Modifier.testTag("quran_wbw_download_${surah.number}"))
        }
    }
}

@Composable private fun JuzRow(
    juz: Int, surah: Surah, ayah: Int, members: List<Int>, bytes: Long, downloads: WbwDownloadState, index: WbwIndex,
    onClick: () -> Unit, onDownload: () -> Unit, onCancel: () -> Unit, onDelete: () -> Unit
) {
    val downloaded = members.all(downloads::isDownloaded)
    val progress = if (members.none(downloads::isActive)) null else {
        val done = members.sumOf { s ->
            val size = index.audio[s - 1].bytes
            when {
                downloads.isDownloaded(s) -> size
                else -> ((downloads.active[s] ?: 0f) * size).toLong()
            }
        }
        (done.toFloat() / bytes).coerceIn(0f, 1f)
    }
    Surface(onClick = onClick, color = QuranCard, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MushafBorder.copy(alpha = .25f)), modifier = Modifier.fillMaxWidth().testTag("quran_wbw_juz_$juz")) {
        Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(QuranGreen, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Text("$juz", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Juz $juz", color = MushafInk, fontWeight = FontWeight.SemiBold)
                LocalizedText("Starts at ${surah.englishName} · Ayah $ayah", color = QuranMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (downloaded) LocalizedText("Audio offline", color = LearnedGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                else if (progress == null) Text("Audio ${bytes.asSize()}", color = QuranMuted, fontSize = 11.sp)
            }
            Text("پارہ ${juz.toArabicDigits()}", fontFamily = IndoPakFont, fontSize = 19.sp, color = QuranGreen)
            DownloadControl(downloaded, progress, onDownload, onCancel, onDelete, Modifier.testTag("quran_wbw_juz_download_$juz"))
        }
    }
}

/** Download → progress ring (tap to cancel) → offline badge (tap to remove). */
@Composable private fun DownloadControl(
    downloaded: Boolean, progress: Float?, onDownload: () -> Unit, onCancel: () -> Unit, onDelete: () -> Unit,
    modifier: Modifier = Modifier, size: Dp = 44.dp
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        when {
            progress != null -> Box(
                Modifier.size(size).clip(CircleShape).clickable(role = Role.Button, onClick = onCancel), contentAlignment = Alignment.Center
            ) {
                if (progress > 0f) CircularProgressIndicator(progress = { progress }, Modifier.size(28.dp), color = QuranGreen, strokeWidth = 3.dp, trackColor = MushafBorder.copy(alpha = .25f))
                else CircularProgressIndicator(Modifier.size(28.dp), color = QuranGreen, strokeWidth = 3.dp)
                Icon(Icons.Default.Close, "Cancel download", tint = QuranGreen, modifier = Modifier.size(14.dp))
            }
            downloaded -> IconButton(onDelete) { Icon(Icons.Default.OfflinePin, "Downloaded · tap to remove", tint = LearnedGreen) }
            else -> IconButton(onDownload) { Icon(Icons.Default.Download, "Download audio", tint = QuranMuted) }
        }
    }
}

@Composable private fun AudioRequestDialog(request: AudioRequest, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val names = request.surahs.map { FullQuranCatalog.surahs[it - 1].englishName }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(if (request.delete) Icons.Default.DeleteOutline else Icons.Default.Download, null, tint = QuranGreen) },
        title = { LocalizedText(if (request.delete) "Remove audio?" else "Download audio?") },
        text = {
            Column {
                LocalizedText(
                    if (request.delete) "Remove the downloaded recitation of ${request.title} and free ${request.bytes.asSize()}. The text and meanings stay available offline."
                    else "Download the recitation of ${request.title} (Mishary Alafasy) to listen without internet. Size: ${request.bytes.asSize()}. Wi-Fi is recommended.",
                    color = MushafInk
                )
                if (!request.delete && request.title.startsWith("Juz") && names.size > 1) {
                    Spacer(Modifier.height(8.dp))
                    LocalizedText("Audio is saved per surah, so this includes the full surahs: ${names.joinToString(", ")}", color = QuranMuted, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onConfirm, colors = ButtonDefaults.buttonColors(containerColor = QuranGreen), modifier = Modifier.testTag("quran_wbw_confirm_audio")) {
                LocalizedText(if (request.delete) "Remove" else "Download")
            }
        },
        dismissButton = { TextButton(onDismiss) { LocalizedText("Cancel") } }
    )
}

// ---------------------------------------------------------------- Reader

@Composable
private fun SurahReader(
    number: Int, startAyah: Int, state: WbwState, index: WbwIndex?, downloads: WbwDownloadState, player: RecitationPlayer,
    onBack: () -> Unit, onOpenSurah: (Int) -> Unit, onRequest: (AudioRequest) -> Unit
) {
    val context = LocalContext.current
    val surah = FullQuranCatalog.surahs[number - 1]
    val ayahs by produceState<List<WbwAyah>?>(null, number) { value = WordByWordRepository.surah(context, number) }
    val headerItems = if (number != 1 && number != 9) 1 else 0
    fun itemFor(ayah: Int) = if (ayah <= 1) 0 else ayah - 1 + headerItems
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = itemFor(startAyah))
    val scope = rememberCoroutineScope()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showJump by rememberSaveable { mutableStateOf(false) }
    var openWord by rememberSaveable { mutableStateOf<String?>(null) }
    val audio = index?.audio?.get(number - 1)
    player.continuous = state.continuousPlay
    player.speed = state.playbackSpeed

    LaunchedEffect(ayahs, audio) { ayahs?.let { player.bind(number, it, audio) } }
    LaunchedEffect(ayahs) {
        if (ayahs == null) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }.distinctUntilChanged().collect { i ->
            state.savePosition(number, (i - headerItems + 1).coerceIn(1, surah.totalAyat))
        }
    }
    // Keep the ayah being recited on screen while playback moves on.
    LaunchedEffect(player.playingAyah) {
        val playing = player.playingAyah ?: return@LaunchedEffect
        val target = itemFor(playing.substringAfter(':').toInt())
        val visible = listState.layoutInfo.visibleItemsInfo
        val fullyVisible = visible.any { it.index == target && it.offset >= 0 && it.offset + it.size <= listState.layoutInfo.viewportEndOffset }
        if (!fullyVisible) listState.animateScrollToItem(target)
    }

    Column(Modifier.fillMaxSize()) {
        Surface(color = QuranCard, shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack, Modifier.testTag("quran_wbw_back")) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = QuranGreen) }
                Column(Modifier.weight(1f).clickable(role = Role.Button) { showJump = true }.testTag("quran_wbw_jump")) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LocalizedText("${surah.number}. ${surah.englishName}", color = MushafInk, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Icon(Icons.Default.UnfoldMore, "Go to ayah", tint = QuranMuted, modifier = Modifier.size(18.dp))
                    }
                    LocalizedText(
                        if (downloads.isDownloaded(number)) "${surah.totalAyat} ayat · audio offline" else "${surah.totalAyat} ayat · ${surah.englishMeaning}",
                        color = if (downloads.isDownloaded(number)) LearnedGreen else QuranMuted, fontSize = 11.sp, maxLines = 1
                    )
                }
                if (audio != null) {
                    val request = { delete: Boolean -> AudioRequest("Surah ${surah.englishName}", listOf(number), audio.bytes, delete) }
                    DownloadControl(
                        downloads.isDownloaded(number), downloads.active[number],
                        onDownload = { onRequest(request(false)) }, onCancel = { WbwAudioDownloads.cancel(listOf(number)) },
                        onDelete = { onRequest(request(true)) }, modifier = Modifier.testTag("quran_wbw_reader_download")
                    )
                }
                PlaybackSpeedButton(state.playbackSpeed, state::updatePlaybackSpeed, Modifier.padding(start = 4.dp), color = QuranGreen)
                IconButton({ showSettings = true }, Modifier.testTag("quran_wbw_settings")) { Icon(Icons.Default.Tune, "Display settings", tint = QuranGreen) }
            }
        }

        val loaded = ayahs
        if (loaded == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = QuranGreen) }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().testTag("quran_wbw_reader"), state = listState,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (headerItems == 1) item(key = "bismillah") {
                    Text(BISMILLAH, fontFamily = IndoPakFont, fontSize = 26.sp, color = QuranGreen, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
                }
                items(loaded, key = { it.key }) { ayah ->
                    AyahCard(ayah, state, player, onWord = { openWord = it.id })
                }
                if (number < 114) item(key = "next") {
                    val next = FullQuranCatalog.surahs[number]
                    Button(
                        { onOpenSurah(next.number) }, Modifier.fillMaxWidth().padding(top = 6.dp).testTag("quran_wbw_next_surah"),
                        colors = ButtonDefaults.buttonColors(containerColor = QuranGreen)
                    ) {
                        LocalizedText("Next surah: ${next.englishName}")
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                    }
                }
            }

            val flat = remember(loaded) { loaded.flatMap { it.words } }
            val selected = flat.indexOfFirst { it.id == openWord }
            if (selected >= 0) {
                WordSheet(flat, selected, state, index, player, onNavigate = { openWord = flat[it].id }, onDismiss = { openWord = null })
            }
        }
    }

    if (showSettings) DisplaySettingsSheet(state) { showSettings = false }
    if (showJump) {
        JumpToAyahDialog(surah.totalAyat, onDismiss = { showJump = false }) { ayah ->
            showJump = false
            scope.launch { listState.scrollToItem(itemFor(ayah)) }
        }
    }
}

@Composable private fun AyahCard(ayah: WbwAyah, state: WbwState, player: RecitationPlayer, onWord: (WbwWord) -> Unit) {
    val playing = player.playingAyah == ayah.key
    Surface(
        color = QuranCard, shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (playing) 2.dp else 1.dp, if (playing) MushafGold else MushafBorder.copy(alpha = .3f)),
        modifier = Modifier.fillMaxWidth().testTag("quran_wbw_ayah_${ayah.key}")
    ) {
        Column(Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = QuranGreen.copy(alpha = .08f), shape = RoundedCornerShape(8.dp)) {
                    Text(ayah.key, color = QuranGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Spacer(Modifier.weight(1f))
                FilledTonalButton(
                    { player.toggleAyah(ayah) }, Modifier.height(32.dp).testTag("quran_ayah_play_${ayah.key}"),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (playing) QuranGreen else QuranGoldLight.copy(alpha = .5f), contentColor = if (playing) Color.White else QuranGreen)
                ) {
                    if (playing && player.buffering) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else Icon(if (playing) Icons.Default.Stop else Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    LocalizedText(if (playing) "Stop" else "Play", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            // Arabic flows right-to-left, so the first word of the ayah sits on the right.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ayah.words.forEach { word ->
                        val learned = state.isLearned(word)
                        WordTile(
                            word, learned, speaking = player.currentWord == word.id,
                            meaning = if (state.showMeaning && !(learned && state.hideLearnedMeanings)) word.gloss(state.language) else null,
                            showTransliteration = state.showTransliteration, arabicSize = state.arabicSize
                        ) { onWord(word) }
                    }
                    Text(
                        "﴿${ayah.number.toArabicDigits()}﴾", fontFamily = IndoPakFont, fontSize = 20.sp, color = MushafGold,
                        modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 4.dp)
                    )
                }
            }
            if (state.showTranslation) {
                HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MushafBorder.copy(alpha = .25f))
                Text(
                    ayah.translation, fontSize = 16.sp, lineHeight = 30.sp, color = MushafInk,
                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl), modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable private fun WordTile(word: WbwWord, learned: Boolean, speaking: Boolean, meaning: String?, showTransliteration: Boolean, arabicSize: Float, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = when { speaking -> QuranGoldLight; learned -> LearnedTint; else -> MushafPaper },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (speaking) 2.dp else 1.dp, when { speaking -> MushafGold; learned -> LearnedGreen.copy(alpha = .4f); else -> MushafBorder.copy(alpha = .3f) }),
        modifier = Modifier.widthIn(min = 48.dp, max = 140.dp).testTag("quran_word_${word.id}")
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(word.arabic, fontFamily = IndoPakFont, fontSize = arabicSize.sp, color = MushafInk, textAlign = TextAlign.Center)
            if (showTransliteration) {
                Text(word.transliteration, fontSize = 11.sp, color = QuranGreen, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (meaning != null) {
                Text(meaning, fontSize = 11.sp, color = QuranMuted, textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 14.sp)
            }
        }
    }
}

// ---------------------------------------------------------------- Word detail

@Composable private fun WordSheet(
    words: List<WbwWord>, index: Int, state: WbwState, wbwIndex: WbwIndex?, player: RecitationPlayer,
    onNavigate: (Int) -> Unit, onDismiss: () -> Unit
) {
    val word = words[index]
    val learned = state.isLearned(word)
    val root: RootInfo? = wbwIndex?.roots?.get(word.root)
    val occurrences = wbwIndex?.lemmaCounts?.get(word.lemma)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = QuranCard) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp).testTag("quran_word_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocalizedText("Ayah ${word.surah}:${word.ayah} · Word ${word.position}", color = QuranMuted, fontSize = 12.sp)
            Text(word.arabic, fontFamily = IndoPakFont, fontSize = 48.sp, color = MushafInk, textAlign = TextAlign.Center, lineHeight = 80.sp)
            Text(word.transliteration, color = QuranGreen, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(word.gloss(state.language), color = MushafInk, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (state.language != WbwLanguage.English) {
                Text(word.gloss(WbwLanguage.English), color = QuranMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
            if (occurrences != null && occurrences > 1) {
                Spacer(Modifier.height(6.dp))
                Surface(color = QuranGoldLight.copy(alpha = .45f), shape = RoundedCornerShape(50)) {
                    LocalizedText(
                        "Appears ${occurrences.grouped()} times in the Quran", color = QuranGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            Surface(color = MushafPaper, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            LocalizedText("Root", color = QuranMuted, fontSize = 13.sp)
                            if (root != null) LocalizedText("${root.occurrences.grouped()} words from this root", color = QuranMuted, fontSize = 11.sp)
                        }
                        if (word.root.isBlank()) LocalizedText("Particle / pronoun", color = MushafInk, fontSize = 13.sp)
                        else Text(word.root.toList().joinToString(" "), fontFamily = IndoPakFont, fontSize = 24.sp, color = MushafGold, fontWeight = FontWeight.Bold)
                    }
                    val related = root?.examples?.filter { it.arabic != word.arabic }.orEmpty()
                    if (related.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        LocalizedText("Related words", color = QuranMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                related.forEach { r ->
                                    Surface(color = QuranCard, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, MushafBorder.copy(alpha = .3f))) {
                                        Column(Modifier.widthIn(max = 130.dp).padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(r.arabic, fontFamily = IndoPakFont, fontSize = 19.sp, color = MushafInk)
                                            Text(r.gloss(state.language), fontSize = 10.sp, color = QuranMuted, maxLines = 2, textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton({ player.playWord(word) }, Modifier.weight(1f).testTag("quran_word_pronounce")) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); LocalizedText("Listen")
                }
                Button(
                    {
                        state.setLearned(word, !learned)
                        if (!learned && index < words.lastIndex) onNavigate(index + 1)
                    },
                    Modifier.weight(1f).testTag("quran_word_mark_learned"),
                    colors = ButtonDefaults.buttonColors(containerColor = if (learned) LearnedGreen else QuranGreen)
                ) {
                    Icon(if (learned) Icons.Default.CheckCircle else Icons.Default.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                    LocalizedText(if (learned) "Learned" else "I know this", maxLines = 1)
                }
            }
            Spacer(Modifier.height(8.dp))
            // Arabic reads right-to-left: the next word lies to the left, so the arrows follow the text.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton({ onNavigate(index + 1) }, enabled = index < words.lastIndex) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null); LocalizedText("Next")
                }
                Text("${index + 1} / ${words.size}", color = QuranMuted, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                TextButton({ onNavigate(index - 1) }, enabled = index > 0) {
                    LocalizedText("Previous"); Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Settings & navigation

@Composable private fun DisplaySettingsSheet(state: WbwState, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = QuranCard) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            LocalizedText("Display", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MushafInk)
            Spacer(Modifier.height(12.dp))
            LocalizedText("Meaning language", color = QuranMuted, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WbwLanguage.entries.forEach { lang ->
                    FilterChip(state.language == lang, { state.updateLanguage(lang) }, { Text(lang.label) },
                        leadingIcon = if (state.language == lang) {{ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }} else null)
                }
            }
            Spacer(Modifier.height(8.dp))
            SettingSwitch("Word meaning", state.showMeaning, state::updateShowMeaning)
            SettingSwitch("Pronunciation (transliteration)", state.showTransliteration, state::updateShowTransliteration)
            SettingSwitch("Hide meanings of words I know", state.hideLearnedMeanings, state::updateHideLearned)
            SettingSwitch("Ayah translation · Kanz-ul-Iman (Urdu)", state.showTranslation, state::updateShowTranslation)
            SettingSwitch("Keep playing the next ayah", state.continuousPlay, state::updateContinuousPlay)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LocalizedText("Arabic size", color = MushafInk, modifier = Modifier.weight(1f))
                Text("بِسۡمِ", fontFamily = IndoPakFont, fontSize = state.arabicSize.sp, color = MushafInk)
            }
            Slider(
                state.arabicSize, state::updateArabicSize, valueRange = 20f..38f, steps = 8,
                colors = SliderDefaults.colors(thumbColor = QuranGreen, activeTrackColor = QuranGreen)
            )
            Spacer(Modifier.height(4.dp))
            LocalizedText(
                "Recitation: Mishary Rashid Alafasy. Word meanings: Quran.com word-by-word. Roots: Quranic Arabic Corpus.",
                color = QuranMuted, fontSize = 11.sp
            )
        }
    }
}

@Composable private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(role = Role.Switch) { onChange(!checked) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        LocalizedText(label, color = MushafInk, modifier = Modifier.weight(1f))
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedTrackColor = QuranGreen))
    }
}

@Composable private fun JumpToAyahDialog(total: Int, onDismiss: () -> Unit, onJump: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val value = text.toIntOrNull()
    val valid = value != null && value in 1..total
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { LocalizedText("Go to ayah") },
        text = {
            OutlinedTextField(
                text, { text = it.filter(Char::isDigit).take(3) }, singleLine = true,
                label = { LocalizedText("Ayah (1–$total)") },
                isError = text.isNotEmpty() && !valid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = { TextButton({ value?.let(onJump) }, enabled = valid) { LocalizedText("Go") } },
        dismissButton = { TextButton(onDismiss) { LocalizedText("Cancel") } }
    )
}
