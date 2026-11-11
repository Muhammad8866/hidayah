package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.tour.tourTarget
import com.example.ui.tour.TourScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.net.StreamPlayer
import com.example.quran.WordByWordRepository
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiTemplate
import com.example.ui.theme.ArabicFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** One drill cell. [ref] is a Quran word (surah:ayah:word) whose recitation plays on tap; letters have none. */
internal data class QaidahItem(val arabic: String, val transliteration: String, val ref: String?)

internal data class QaidahLesson(val id: String, val title: String, val note: String, val items: List<QaidahItem>) {
    val hasAudio: Boolean get() = items.any { it.ref != null }
}

/** Lessons built by tools/build_qaidah.py into assets/qaidah/lessons.json. */
internal object QaidahRepository {
    @Volatile private var lessons: List<QaidahLesson>? = null

    suspend fun lessons(context: Context): List<QaidahLesson> = lessons ?: withContext(Dispatchers.IO) {
        val json = context.assets.open("qaidah/lessons.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        parseQaidahLessons(json)
    }.also { lessons = it }
}

internal fun parseQaidahLessons(json: String): List<QaidahLesson> {
    val array = JSONObject(json).getJSONArray("lessons")
    return (0 until array.length()).map { i ->
        val lesson = array.getJSONObject(i)
        val items = lesson.getJSONArray("items")
        QaidahLesson(
            lesson.getString("id"), lesson.getString("title"), lesson.getString("note"),
            (0 until items.length()).map { j ->
                val item = items.getJSONObject(j)
                QaidahItem(item.getString("ar"), item.getString("tr"), item.optString("ref").ifEmpty { null })
            }
        )
    }
}

/** Splits "2:1:1" into surah, ayah and word position, or null when malformed. */
internal fun parseQaidahRef(ref: String): Triple<Int, Int, Int>? =
    ref.split(':').mapNotNull { it.toIntOrNull() }.takeIf { it.size == 3 }?.let { (s, a, w) -> Triple(s, a, w) }

private class QaidahProgress(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("qaidah", Context.MODE_PRIVATE)
    var done by mutableStateOf(prefs.getStringSet("done", emptySet()).orEmpty().toSet())
        private set

    fun toggle(id: String) {
        done = if (id in done) done - id else done + id
        prefs.edit().putStringSet("done", done).apply()
    }
}

/**
 * Plays Quran word recitations for word drills. [playing] is the index of the cell being heard;
 * [queue] plays the lesson's words one after another.
 */
private class QaidahPlayer(private val context: Context, private val scope: kotlinx.coroutines.CoroutineScope) {
    var playing by mutableStateOf<Int?>(null)
        private set
    private var player: ExoPlayer? = null
    private var urls: List<String> = emptyList()
    private var queue: List<Pair<Int, String>> = emptyList()

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) advance()
        }

        override fun onPlayerError(error: PlaybackException) {
            // Try the word's next host before moving on.
            if (urls.size > 1) start(urls.drop(1)) else advance()
        }
    }

    fun play(index: Int, ref: String) = playQueue(listOf(index to ref))

    fun playQueue(items: List<Pair<Int, String>>) {
        queue = items
        advance()
    }

    fun stop() {
        queue = emptyList()
        playing = null
        player?.stop()
    }

    private fun advance() {
        val next = queue.firstOrNull() ?: run { playing = null; return }
        queue = queue.drop(1)
        playing = next.first
        scope.launch {
            val (surah, ayah, word) = parseQaidahRef(next.second) ?: return@launch advance()
            val found = runCatching {
                WordByWordRepository.surah(context, surah).getOrNull(ayah - 1)?.words?.getOrNull(word - 1)?.audioUrls
            }.getOrNull()
            if (found.isNullOrEmpty()) advance() else start(found)
        }
    }

    private fun start(candidates: List<String>) {
        urls = candidates
        val p = player ?: StreamPlayer.build(context, exactSeek = true).also { it.addListener(listener); player = it }
        p.setMediaItem(MediaItem.fromUri(candidates.first()))
        p.prepare()
        p.play()
    }

    fun release() {
        player?.release()
        player = null
    }
}

/** Madani Qaidah: step-by-step lessons from single letters to ghunnah, with Quran word recitations. */
@Composable
fun QaidahScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("qaidah_screen")
    val context = LocalContext.current
    val lessons by produceState<List<QaidahLesson>?>(null) { value = QaidahRepository.lessons(context) }
    val progress = remember(context) { QaidahProgress(context) }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    val open = lessons?.firstOrNull { it.id == openId }

    if (open != null) {
        TourScreen("qaida_madani_lesson")
        BackHandler { openId = null }
        QaidahLessonView(
            lesson = open,
            number = lessons!!.indexOf(open) + 1,
            done = open.id in progress.done,
            onToggleDone = { progress.toggle(open.id) },
            onBack = { openId = null },
            modifier = modifier
        )
        return
    }

    Column(modifier.fillMaxSize().padding(horizontal = Spacing.md)) {
        ScreenHeader(
            title = "Madani Qaidah",
            subtitle = lessons?.let { localizedUiTemplate("{done} of {total} lessons done", "done" to progress.done.count { id -> it.any { l -> l.id == id } }, "total" to it.size) },
            localizeSubtitle = false,
            onBack = onBack
        )
        val list = lessons
        if (list == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.sm), contentPadding = PaddingValues(bottom = Spacing.lg)) {
            itemsIndexed(list, key = { _, l -> l.id }) { index, lesson ->
                QaidahLessonRow(index + 1, lesson, lesson.id in progress.done) { com.example.core.analytics.AppAnalytics.interaction("qaidah_screen", "lesson_open"); openId = lesson.id }
            }
        }
    }
}

@Composable
private fun QaidahLessonRow(number: Int, lesson: QaidahLesson, done: Boolean, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).testTag("qaidah_lesson_${lesson.id}").tourTarget("qaida_madani_row"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$number", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                LocalizedText(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LocalizedText(
                    if (lesson.hasAudio) "Tap a word to hear it" else "Read along with your teacher",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (done) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            else if (lesson.hasAudio) Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QaidahLessonView(
    lesson: QaidahLesson,
    number: Int,
    done: Boolean,
    onToggleDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val player = remember(lesson.id) { QaidahPlayer(context.applicationContext, scope) }
    DisposableEffect(player) { onDispose { player.release() } }
    var playingAll by remember(lesson.id) { mutableStateOf(false) }
    LaunchedEffect(player.playing) { if (player.playing == null) playingAll = false }

    Column(modifier.fillMaxSize().padding(horizontal = Spacing.md)) {
        ScreenHeader(
            title = lesson.title,
            subtitle = localizedUiTemplate("Lesson {number}", "number" to number),
            localizeSubtitle = false,
            onBack = { player.stop(); onBack() }
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = if (lesson.id == "joined") 150.dp else 96.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(bottom = Spacing.lg),
            modifier = Modifier.weight(1f)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    LocalizedText(lesson.note, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.tourTarget("qaida_madani_note"))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        if (lesson.hasAudio) {
                            FilledTonalButton(
                                onClick = {
                                    com.example.core.analytics.AppAnalytics.interaction("qaidah_screen", "lesson_play_all"); 
                                    if (playingAll) player.stop() else {
                                        playingAll = true
                                        player.playQueue(lesson.items.mapIndexedNotNull { i, it -> it.ref?.let { r -> i to r } })
                                    }
                                },
                                modifier = Modifier.testTag("qaidah_play_all").tourTarget("qaida_madani_play_all")
                            ) {
                                Icon(if (playingAll) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                LocalizedText(if (playingAll) "Stop" else "Play all")
                            }
                        }
                        FilterChip(
                            selected = done,
                            onClick = com.example.core.analytics.AppAnalytics.trackAction("qaidah_screen", "lesson_done", onToggleDone),
                            label = { LocalizedText("Lesson done") },
                            leadingIcon = if (done) ({ Icon(Icons.Filled.CheckCircle, contentDescription = null) }) else null,
                            modifier = Modifier.testTag("qaidah_done").tourTarget("qaida_madani_done")
                        )
                    }
                }
            }
            itemsIndexed(lesson.items) { index, item ->
                QaidahCell(item, playing = player.playing == index) {
                    item.ref?.let { com.example.core.analytics.AppAnalytics.interaction("qaidah_screen", "lesson_cell"); player.play(index, it) }
                }
            }
        }
    }
}

@Composable
private fun QaidahCell(item: QaidahItem, playing: Boolean, onTap: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onTap,
        modifier = Modifier.tourTarget("qaida_madani_cell"),
        enabled = item.ref != null,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (playing) 2.dp else 1.dp, if (playing) colors.primary else colors.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (playing) colors.primaryContainer else colors.surface,
            disabledContainerColor = colors.surface,
            disabledContentColor = colors.onSurface
        )
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = Spacing.sm, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                item.arabic,
                fontFamily = ArabicFontFamily,
                fontSize = 30.sp,
                lineHeight = 52.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                item.transliteration,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
