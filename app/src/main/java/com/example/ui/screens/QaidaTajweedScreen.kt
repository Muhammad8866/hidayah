@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.analytics.AnalyticsScreen
import com.example.core.analytics.AnalyticsScroll
import com.example.core.analytics.AnalyticsSearch
import com.example.core.analytics.AppAnalytics
import com.example.ui.components.AppCard
import com.example.ui.components.HeroSurface
import com.example.ui.components.IconBadge
import com.example.ui.components.ListRowItem
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.components.accentTint
import com.example.ui.components.groupedShape
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.theme.GoldAccent

private const val QAIDA_REFERENCE_URL = "https://download.understandquran.com/fileadmin/user_upload/extras/tajweed/Tajweed_Rules.pdf"
private const val QAIDA_FONT_MIN = 24
private const val QAIDA_FONT_MAX = 44

@Composable
fun QaidaTajweedScreen(onBack: () -> Unit, onOpenQuran: () -> Unit = {}, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val audio = remember(context) { StreamingQaidaAudio(context.applicationContext) }
    QaidaTajweedContent(onBack, onOpenQuran, modifier, audio)
}

/**
 * Home is a learning path (hero with the next lesson, track switch, then every lesson as a step);
 * a lesson reads top to bottom: rule → listen → letters grid → read aloud → listening drill → quiz.
 */
@Composable
internal fun QaidaTajweedContent(onBack: () -> Unit, onOpenQuran: () -> Unit = {}, modifier: Modifier = Modifier, audio: QaidaAudioController) {
    val context = LocalContext.current
    val audioState by audio.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(audio, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) audio.pause() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); audio.close() }
    }
    val appLanguage = LocalAppLanguage.current
    val prefs = remember(context) { context.getSharedPreferences("qaida_learning", Context.MODE_PRIVATE) }
    val progress = remember(prefs) { QaidaProgress(prefs) }
    val progressState by progress.state.collectAsState()
    var roman by rememberSaveable { mutableStateOf(prefs.getBoolean("roman", appLanguage in listOf("hi", "ur", "hinglish"))) }
    var tajweed by rememberSaveable { mutableStateOf(prefs.getBoolean("track_tajweed", false)) }
    var arabicSize by rememberSaveable { mutableStateOf(prefs.getInt("arabic_size", 32).coerceIn(QAIDA_FONT_MIN, QAIDA_FONT_MAX)) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var bookmarksOnly by rememberSaveable { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var referenceUnavailable by remember { mutableStateOf(false) }
    val label: QaidaLabel = { en, ur -> if (roman) ur else en }
    val lesson = qaidaLessons.find { it.id == selectedId }
    AnalyticsScreen(if (lesson == null) "qaida_tajweed_screen" else "qaida_lesson_screen")
    AnalyticsSearch("qaida_tajweed_screen", query)
    val searching = query.isNotBlank()
    val shown = (if (searching) qaidaSearch(query.trim(), roman) else qaidaLessons.filter { it.tajweed == tajweed })
        .filter { !bookmarksOnly || it.id in progressState.bookmarks }
    // Lesson numbers and previous/next always follow the lesson's own track.
    fun track(of: QaidaLesson) = qaidaLessons.filter { it.tajweed == of.tajweed }
    val columns = ((LocalConfiguration.current.screenWidthDp - 32) / 80).coerceIn(3, 8)
    val accent = accentTint(DailyTool.QAIDA_TAJWEED.accent)
    val listState = rememberLazyListState()
    AnalyticsScroll(if (lesson == null) "qaida_tajweed_screen" else "qaida_lesson_screen", listState)
    val close = { AppAnalytics.interaction("qaida_tajweed_screen", "back"); if (selectedId != null) selectedId = null else onBack() }
    BackHandler(enabled = selectedId != null || searching) { if (selectedId != null) selectedId = null else query = "" }
    LaunchedEffect(selectedId) {
        audio.stop()
        listState.scrollToItem(0)
        selectedId?.let { progress.opened(it); AppAnalytics.content("qaida_tajweed", it) }
    }
    LaunchedEffect(tajweed) { audio.stop() }
    fun setTrack(value: Boolean) { AppAnalytics.setting("qaida_track", if (value) "tajweed" else "qaida"); tajweed = value; prefs.edit().putBoolean("track_tajweed", value).apply() }
    fun setRoman(value: Boolean) { AppAnalytics.setting("qaida_language", if (value) "roman" else "english"); roman = value; prefs.edit().putBoolean("roman", value).apply() }
    fun setSize(value: Int) { AppAnalytics.interaction("qaida_lesson_screen", "arabic_size", if (value > arabicSize) "increase" else "decrease"); arabicSize = value.coerceIn(QAIDA_FONT_MIN, QAIDA_FONT_MAX); prefs.edit().putInt("arabic_size", arabicSize).apply() }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            icon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
            title = { Text(label("Reset progress?", "Progress reset karein?")) },
            text = { Text(label("Completed lessons, scores, bookmarks and your streak will be cleared. This cannot be undone.",
                "Mukammal sabaq, score, bookmark aur streak sab mit jayenge. Yeh wapas nahi hoga.")) },
            confirmButton = { TextButton(onClick = { AppAnalytics.interaction("qaida_tajweed_screen", "reset_confirm"); progress.reset(); bookmarksOnly = false; confirmReset = false }, modifier = Modifier.testTag("qaida_reset_confirm")) {
                Text(label("Reset", "Reset karein"), color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(label("Cancel", "Rehne dein")) } }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().imePadding().testTag("${DailyTool.QAIDA_TAJWEED.tag}_screen"),
        state = listState,
        contentPadding = PaddingValues(start = Spacing.screen, top = Spacing.sm, end = Spacing.screen, bottom = Spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        if (lesson == null) {
            item("header") {
                ScreenHeader(
                    title = DailyTool.QAIDA_TAJWEED.title,
                    subtitle = DailyTool.QAIDA_TAJWEED.subtitle,
                    onBack = close,
                    leading = { IconBadge(DailyTool.QAIDA_TAJWEED.icon, DailyTool.QAIDA_TAJWEED.accent) }
                ) { QaidaLanguageMenu(roman, ::setRoman) }
            }
            item("progress") {
                QaidaHero(progressState, tajweed, label) {
                    AppAnalytics.interaction("qaida_tajweed_screen", "continue_learning")
                    selectedId = qaidaNextLesson(progressState, tajweed).id
                }
            }
            item("tabs") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(selected = !tajweed, onClick = { setTrack(false) }, shape = SegmentedButtonDefaults.itemShape(0, 2),
                        modifier = Modifier.testTag("qaida_tab")) { Text("Noorani Qaida", maxLines = 1) }
                    SegmentedButton(selected = tajweed, onClick = { setTrack(true) }, shape = SegmentedButtonDefaults.itemShape(1, 2),
                        modifier = Modifier.testTag("tajweed_tab")) { Text("Tajweed", maxLines = 1) }
                }
            }
            item("search") {
                OutlinedTextField(
                    value = query, onValueChange = { query = it }, singleLine = true,
                    placeholder = { Text(label("Search lessons, rules or letters", "Sabaq, qaida ya harf talash karein"), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = { if (searching) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, contentDescription = label("Clear search", "Talash saaf karein")) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("qaida_search")
                )
            }
            item("filters") {
                Row(Modifier.fillMaxWidth().padding(top = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (searching) label("${shown.size} result(s) across both tracks", "Dono hisson mein ${shown.size} natija")
                        else label("All lessons", "Saare sabaq"),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).semantics { heading() }
                    )
                    FilterChip(
                        selected = bookmarksOnly,
                        onClick = { AppAnalytics.interaction("qaida_tajweed_screen", "bookmarks_filter", "toggle"); bookmarksOnly = !bookmarksOnly },
                        label = { Text(label("Bookmarks (${progressState.bookmarks.size})", "Bookmarks (${progressState.bookmarks.size})")) },
                        leadingIcon = { Icon(if (bookmarksOnly) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        shape = CircleShape,
                        modifier = Modifier.testTag("qaida_bookmarks_filter")
                    )
                }
            }
            if (shown.isEmpty()) item("empty") {
                QaidaEmptyNote(
                    if (searching) label("No lesson matches your search. Try a rule name such as Ikhfa or Madd.", "Koi sabaq nahi mila. Ikhfa ya Madd jaisa naam likh kar dekhein.")
                    else label("No bookmarked lessons here yet. Open a lesson and tap the bookmark icon.", "Yahan abhi koi bookmark nahi. Sabaq khol kar bookmark icon dabayein."),
                    if (searching) Icons.Default.SearchOff else Icons.Default.BookmarkBorder
                )
            }
            val upNext = qaidaNextLesson(progressState, tajweed).id
            itemsIndexed(shown, key = { _, entry -> entry.id }) { index, entry ->
                QaidaPathStep(
                    number = track(entry).indexOf(entry) + 1, lesson = entry, title = label(entry.title, entry.romanTitle),
                    done = entry.id in progressState.completed,
                    upNext = !searching && entry.id == upNext && entry.id !in progressState.completed,
                    inProgress = entry.id == progressState.lastLessonId && entry.id !in progressState.completed,
                    bookmarked = entry.id in progressState.bookmarks, score = progressState.scores[entry.id],
                    first = index == 0, last = index == shown.lastIndex,
                    accent = accent, label = label, onClick = { selectedId = entry.id }
                )
            }
            item("tools") {
                val canReset = progressState.completed.isNotEmpty() || progressState.bookmarks.isNotEmpty() || progressState.scores.isNotEmpty()
                val count = if (canReset) 3 else 2
                Column(Modifier.padding(top = Spacing.md), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ListRowItem(
                        title = label("Open Quran for recitation", "Tilawat ke liye Quran kholein"),
                        icon = Icons.AutoMirrored.Filled.MenuBook, accent = accent, localizeTitle = false,
                        shape = groupedShape(0, count),
                        onClick = AppAnalytics.trackAction("qaida_tajweed_screen", "open_quran", onOpenQuran)
                    )
                    ListRowItem(
                        title = label("Teaching reference · Understand Al-Qur'an", "Tajweed reference · Understand Al-Qur'an"),
                        subtitle = if (referenceUnavailable) label("A browser or PDF reader is needed to open the reference.", "Reference kholne ke liye browser ya PDF reader chahiye.") else null,
                        icon = Icons.AutoMirrored.Filled.OpenInNew, accent = accent, localizeTitle = false, localizeSubtitle = false,
                        shape = groupedShape(1, count),
                        onClick = {
                            AppAnalytics.interaction("qaida_tajweed_screen", "teaching_reference")
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(QAIDA_REFERENCE_URL)))
                            } catch (_: android.content.ActivityNotFoundException) { referenceUnavailable = true }
                        }
                    )
                    if (canReset) ListRowItem(
                        title = label("Reset progress?", "Progress reset karein?").removeSuffix("?"),
                        icon = Icons.Default.RestartAlt, accent = MaterialTheme.colorScheme.error, localizeTitle = false,
                        shape = groupedShape(2, count),
                        modifier = Modifier.testTag("qaida_reset"),
                        onClick = { AppAnalytics.interaction("qaida_tajweed_screen", "reset_request"); confirmReset = true }
                    )
                }
            }
            item("guidance") {
                InfoNote(label("Read each drill aloud with a teacher to check pronunciation. Roman text explains the rule; Arabic is the reading practice.", "Har mashq ustad ko suna kar talaffuz check karayein. Roman mein samjhaya gaya hai; parhne ki mashq Arabic mein hai."))
            }
        } else {
            val trackLessons = track(lesson)
            val position = trackLessons.indexOf(lesson)
            val previous = trackLessons.getOrNull(position - 1)
            val next = trackLessons.getOrNull(position + 1)
            val bookmarked = lesson.id in progressState.bookmarks
            val isLetters = lesson.id == "letters"
            val clips = qaidaLessonAudio(lesson.id).ifEmpty { if (isLetters) qaidaAlphabet.indices.map { qaidaLetterAudio(it) } else emptyList() }
            // Single letters and syllables are a grid of tiles; whole-ayah recordings are rows.
            val gridClips = if (isLetters) emptyList() else clips.filter { qaidaIsLetterClip(it) }
            val rowClips = if (isLetters) emptyList() else clips.filterNot { qaidaIsLetterClip(it) }
            val drillClips = clips.filter { qaidaIsLetterClip(it) }.distinctBy { it.arabic }
            val ruleLetters = lesson.letters.split(" ").filter { it.isNotBlank() }
            val source = if (clips.any { "quranfor.me" in it.url }) "Arabic pronunciation · QuranFor.me" else "Al-Husary · EveryAyah · Full ayah recording"

            item("lesson_bar") {
                QaidaLessonHeader(
                    trackName = if (lesson.tajweed) "Tajweed" else "Noorani Qaida",
                    position = position, total = trackLessons.size,
                    completed = lesson.id in progressState.completed,
                    title = label(lesson.title, lesson.romanTitle),
                    bookmarked = bookmarked, accent = accent, label = label,
                    onBack = { AppAnalytics.interaction("qaida_lesson_screen", "all_lessons"); selectedId = null },
                    onBookmark = { AppAnalytics.content("qaida_tajweed", lesson.id, if (!bookmarked) "bookmark" else "unbookmark"); progress.toggleBookmark(lesson.id) }
                )
            }
            item("rule") {
                QaidaRuleCard(
                    rule = label(lesson.rule, lesson.romanRule),
                    tip = if (lesson.tip.isNotBlank() || lesson.romanTip.isNotBlank()) label(lesson.tip, lesson.romanTip.ifBlank { lesson.tip }) else null,
                    letters = ruleLetters, accent = accent, label = label
                ) { letter ->
                    val index = qaidaAlphabet.indexOfFirst { it.first == letter }
                    if (index >= 0) audio.play(qaidaLetterAudio(index))
                }
            }
            item("audio_controls") { QaidaAudioBar(audioState, clips, source, accent, label, audio) }
            if (isLetters || gridClips.isNotEmpty()) item("grid_title") {
                QaidaSizeRow("", arabicSize, QAIDA_FONT_MIN, QAIDA_FONT_MAX, label, ::setSize, showTitle = false)
            }
            if (isLetters) {
                itemsIndexed(qaidaAlphabet.withIndex().chunked(columns), key = { row, _ -> "letters_$row" }) { _, letters ->
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            letters.forEach { (index, entry) ->
                                QaidaSoundCard(entry.first, entry.second, audioState.clipId == "name-$index", audioState, accent, arabicSize,
                                    "qaida_letter_$index", { audio.play(qaidaLetterAudio(index)) }, Modifier.weight(1f))
                            }
                            repeat(columns - letters.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            itemsIndexed(gridClips.chunked(columns), key = { row, _ -> "syllables_$row" }) { _, rowItems ->
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        rowItems.forEach { clip ->
                            QaidaSoundCard(clip.arabic, clip.title, audioState.clipId == clip.id, audioState, accent, arabicSize,
                                "qaida_audio_${clip.id}", { audio.play(clip) }, Modifier.weight(1f))
                        }
                        repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            itemsIndexed(rowClips, key = { _, clip -> "audio_${clip.id}" }) { _, clip -> QaidaClipRow(clip, audioState, accent, label) { audio.play(clip) } }
            item("practice_title") {
                QaidaSizeRow(label("Read aloud slowly", "Aahista awaaz se parhein"), arabicSize, QAIDA_FONT_MIN, QAIDA_FONT_MAX, label, ::setSize,
                    showControls = !(isLetters || gridClips.isNotEmpty()))
            }
            item("examples") { QaidaArabicExamples(lesson.examples, arabicSize) }
            if (drillClips.size >= 4) item("drill") {
                key(lesson.id) { QaidaListenDrill(lesson.id, drillClips, audio, accent, label) }
            }
            item("quiz") {
                key(lesson.id) {
                    QaidaQuizCard(lesson, roman, accent, label, hasNext = next != null,
                        onFinished = { correct, total -> progress.recordQuiz(lesson.id, correct, total); AppAnalytics.milestone("qaida_tajweed", "quiz_completed", lesson.id) },
                        onNext = { selectedId = next?.id })
                }
            }
            item("lesson_nav") {
                Row(Modifier.fillMaxWidth().padding(top = Spacing.xs), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedButton(onClick = { AppAnalytics.interaction("qaida_lesson_screen", "previous_lesson"); selectedId = previous?.id }, enabled = previous != null,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("qaida_nav_prev")) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(label("Previous", "Pichhla"), maxLines = 1)
                    }
                    FilledTonalButton(onClick = { AppAnalytics.interaction("qaida_lesson_screen", "next_lesson"); selectedId = next?.id }, enabled = next != null,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("qaida_nav_next")) {
                        Text(label("Next", "Agla"), maxLines = 1)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/** English / Roman Urdu switch, tucked into the header instead of a row of chips. */
@Composable
private fun QaidaLanguageMenu(roman: Boolean, onChange: (Boolean) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.testTag("qaida_language")) {
            Icon(Icons.Default.Translate, contentDescription = if (roman) "Roman Urdu / Hindi" else "English")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("English") }, onClick = { onChange(false); open = false },
                leadingIcon = { RadioButton(selected = !roman, onClick = null) })
            DropdownMenuItem(text = { Text("Roman Urdu / Hindi") }, onClick = { onChange(true); open = false },
                leadingIcon = { RadioButton(selected = roman, onClick = null) })
        }
    }
}

/** Emerald hero: track progress, streak and the one primary action — continue with the next lesson. */
@Composable
private fun QaidaHero(state: QaidaProgressState, tajweed: Boolean, label: QaidaLabel, onContinue: () -> Unit) {
    val (done, total) = qaidaTrackProgress(state, tajweed)
    val allDone = qaidaLessons.count { it.id in state.completed }
    val next = qaidaNextLesson(state, tajweed)
    val nextNumber = qaidaLessons.filter { it.tajweed == tajweed }.indexOf(next) + 1
    val fraction = if (total == 0) 0f else done.toFloat() / total
    val started = state.completed.isNotEmpty() || state.lastLessonId != null
    HeroSurface(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TonalPill(if (tajweed) "Tajweed" else "Noorani Qaida",
                containerColor = Color.White.copy(alpha = 0.14f), contentColor = Color.White)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                if (state.streakDays > 0) label("${state.streakDays}-day streak", "${state.streakDays} din ka silsila")
                else label("Start your streak today", "Aaj se silsila shuru karein"),
                style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f), maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Text(label("Learn to read, one step at a time", "Quran parhna qadam-ba-qadam seekhein"),
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(Spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label("$done / $total lessons completed", "$done / $total sabaq mukammal"),
                style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f).testTag("qaida_track_progress"))
            Text("${(fraction * 100).toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GoldAccent)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
            color = GoldAccent, trackColor = Color.White.copy(alpha = 0.18f),
            gapSize = 0.dp, drawStopIndicator = {}
        )
        Text(label("$allDone / ${qaidaLessons.size} overall", "Kul $allDone / ${qaidaLessons.size}"),
            style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(Spacing.lg))
        // What "Continue" will open, so the button never feels like a guess.
        Surface(shape = MaterialTheme.shapes.medium, color = Color.White.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(GoldAccent), contentAlignment = Alignment.Center) {
                    Text("$nextNumber", fontWeight = FontWeight.Bold, color = Color(0xFF0B2D25))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(label("Next lesson", "Agla sabaq"), style = MaterialTheme.typography.labelMedium, color = GoldAccent)
                    Text(label(next.title, next.romanTitle), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                        color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Spacer(Modifier.height(Spacing.md))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qaida_continue"),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF0B2D25))
        ) {
            Text(if (!started) label("Start learning", "Seekhna shuru karein") else label("Continue learning", "Seekhna jaari rakhein"),
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun QaidaEmptyNote(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    AppCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(Spacing.xl)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            IconBadge(icon, MaterialTheme.colorScheme.onSurfaceVariant, size = 52.dp, iconSize = 28.dp)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

/** Lesson top bar: back to the path, where this lesson sits in its track, title and bookmark. */
@Composable
private fun QaidaLessonHeader(
    trackName: String, position: Int, total: Int, completed: Boolean, title: String, bookmarked: Boolean,
    accent: Color, label: QaidaLabel, onBack: () -> Unit, onBookmark: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("qaida_all_lessons")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = label("All lessons", "Saare sabaq"))
            }
            Column(Modifier.weight(1f)) {
                Text("$trackName · " + label("Lesson ${position + 1} of $total", "Sabaq ${position + 1} / $total") +
                    if (completed) " · " + label("Completed", "Mukammal") else "",
                    style = MaterialTheme.typography.labelLarge, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() })
            }
            IconToggleButton(checked = bookmarked, onCheckedChange = { onBookmark() }, modifier = Modifier.testTag("qaida_bookmark")) {
                Icon(if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, tint = if (bookmarked) accent else LocalContentColor.current,
                    contentDescription = if (bookmarked) label("Remove bookmark", "Bookmark hatayein") else label("Bookmark this lesson", "Sabaq bookmark karein"))
            }
        }
        LinearProgressIndicator(
            progress = { (position + 1f) / total.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)),
            color = accent, trackColor = accent.copy(alpha = 0.14f), gapSize = 0.dp, drawStopIndicator = {}
        )
    }
}

/** The rule, its tip and its letters together in one calm card. */
@Composable
private fun QaidaRuleCard(rule: String, tip: String?, letters: List<String>, accent: Color, label: QaidaLabel, onLetter: (String) -> Unit) {
    AppCard(Modifier.fillMaxWidth().animateContentSize(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(rule, style = MaterialTheme.typography.bodyLarge)
        if (tip != null) {
            Surface(shape = MaterialTheme.shapes.medium, color = accent.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Lightbulb, contentDescription = label("Tip", "Mashwara"), tint = accent, modifier = Modifier.size(20.dp))
                    Text(tip, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (letters.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(label("Letters of this rule · tap to hear", "Is qaide ke huroof · sunne ke liye dabayein"),
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            QaidaLetterChips(letters, accent, onLetter)
        }
    }
}

/** Section title with the Arabic text-size controls at its end. */
@Composable
private fun QaidaSizeRow(
    title: String, size: Int, min: Int, max: Int, label: QaidaLabel, onSize: (Int) -> Unit,
    showTitle: Boolean = true, showControls: Boolean = true
) {
    if (!showTitle && !showControls) return
    Row(Modifier.fillMaxWidth().padding(top = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        if (showTitle) Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).semantics { heading() })
        else Spacer(Modifier.weight(1f))
        if (showControls) {
            IconButton(onClick = { onSize(size - 4) }, enabled = size > min, modifier = Modifier.testTag("qaida_font_smaller")) {
                Icon(Icons.Default.TextDecrease, contentDescription = label("Smaller Arabic text", "Arabic chhoti karein"))
            }
            IconButton(onClick = { onSize(size + 4) }, enabled = size < max, modifier = Modifier.testTag("qaida_font_larger")) {
                Icon(Icons.Default.TextIncrease, contentDescription = label("Larger Arabic text", "Arabic bari karein"))
            }
        }
    }
}
