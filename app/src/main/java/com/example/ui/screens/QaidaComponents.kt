@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppCard
import com.example.ui.components.IconBadge
import com.example.ui.components.OnReconnect
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.components.pressScale
import com.example.ui.theme.ArabicFontFamily

internal typealias QaidaLabel = (en: String, ur: String) -> String

/** Scores may be stored as a correct-answer count or a percentage; show either sensibly. */
internal fun qaidaScoreText(score: Int, total: Int): String =
    if (score > total) "$score%" else "$score/$total"

internal fun qaidaSpeedText(speed: Float): String =
    (if (speed == speed.toInt().toFloat()) speed.toInt().toString() else speed.toString()) + "×"

internal fun qaidaSpeedTag(speed: Float) = "qaida_audio_speed_${(speed * 100).toInt()}"

/**
 * One step of the learning path: a node on a vertical line (tick when done, ring when it is next)
 * beside the lesson card.
 */
@Composable
internal fun QaidaPathStep(
    number: Int, lesson: QaidaLesson, title: String, done: Boolean, upNext: Boolean, inProgress: Boolean,
    bookmarked: Boolean, score: Int?, first: Boolean, last: Boolean, accent: Color, label: QaidaLabel, onClick: () -> Unit
) {
    val status = when {
        done -> label("Completed · Review", "Mukammal · Dobara parhein")
        inProgress -> label("In progress", "Jaari hai")
        else -> label("Lesson & practice", "Sabaq aur mashq")
    }
    val line = MaterialTheme.colorScheme.outlineVariant
    val doneLine = accent.copy(alpha = 0.6f)
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // The rail: a connector through the node, solid accent along finished lessons.
        Box(
            Modifier.width(40.dp).fillMaxHeight().drawBehind {
                val x = size.width / 2
                val nodeY = 30.dp.toPx()
                val stroke = 2.dp.toPx()
                if (!first) drawLine(if (done) doneLine else line, Offset(x, -Spacing.md.toPx()), Offset(x, nodeY), stroke)
                if (!last) drawLine(if (done) doneLine else line, Offset(x, nodeY), Offset(x, size.height), stroke)
            },
            contentAlignment = Alignment.TopCenter
        ) {
            val ring = upNext || inProgress
            Box(
                Modifier.padding(top = 12.dp).size(36.dp).clip(CircleShape)
                    .background(
                        when {
                            done -> accent
                            ring -> accent.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .then(if (ring) Modifier.border(2.dp, accent, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (done) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                else Text("$number", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                    color = if (ring) accent else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(Spacing.sm))
        AppCard(
            modifier = Modifier.weight(1f).padding(vertical = 2.dp).testTag("qaida_lesson_${lesson.id}"),
            onClick = onClick,
            containerColor = if (upNext) accent.copy(alpha = 0.07f) else MaterialTheme.colorScheme.surface,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (upNext) TonalPill(label("Next lesson", "Agla sabaq"), containerColor = accent, contentColor = MaterialTheme.colorScheme.surface)
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        if (score != null) "$status · ${label("Best", "Behtareen")} ${qaidaScoreText(score, lesson.quizzes.size)}" else status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (done || inProgress) accent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (bookmarked) Icon(Icons.Default.Bookmark, contentDescription = label("Bookmarked", "Bookmark kiya hua"), tint = accent, modifier = Modifier.size(20.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Square tile for one letter or syllable: big Arabic, its name, and a live marker while it plays. */
@Composable
internal fun QaidaSoundCard(
    arabic: String, caption: String, active: Boolean, state: QaidaAudioState, accent: Color,
    fontSize: Int, tag: String, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.pressScale(interaction).testTag(tag).semantics { contentDescription = caption },
        shape = MaterialTheme.shapes.medium,
        color = if (active) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = if (active) BorderStroke(2.dp, accent) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        interactionSource = interaction
    ) {
        Box {
            Column(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp, start = 2.dp, end = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(arabic, fontFamily = ArabicFontFamily, fontSize = (fontSize - 2).sp, lineHeight = (fontSize * 1.6f).sp, textAlign = TextAlign.Center,
                    color = if (active) accent else MaterialTheme.colorScheme.onSurface)
                Text(caption, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (active) Box(Modifier.align(Alignment.TopStart).padding(4.dp).size(16.dp), contentAlignment = Alignment.Center) {
                if (state.loading) CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 2.dp, color = accent)
                else if (state.playing) Icon(Icons.Default.GraphicEq, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** Compact player: status, one big play button between previous/next, then repeat, stop and speed. */
@Composable
internal fun QaidaAudioBar(
    state: QaidaAudioState, clips: List<QaidaAudioClip>, source: String, accent: Color,
    label: QaidaLabel, audio: QaidaAudioController
) {
    val current = clips.firstOrNull { it.id == state.clipId }
    // A clip that failed to stream while offline plays again by itself once the connection is back.
    OnReconnect(enabled = state.failed && current != null) { current?.let(audio::play) }
    AppCard(Modifier.fillMaxWidth().animateContentSize(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.Headphones, accent, size = 40.dp, iconSize = 22.dp, filled = state.playing)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label("Listen & practise", "Sunein aur mashq karein"), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
                Text(
                    when {
                        clips.isEmpty() -> label("No recording for this lesson yet. Read the drills aloud to your teacher.", "Is sabaq ki recording abhi nahi hai. Mashq ustad ko sunayein.")
                        state.failed -> label("Audio unavailable. Check internet and retry.", "Audio nahi chala. Internet check karke dobara koshish karein.")
                        state.loading -> label("Loading audio…", "Audio load ho raha hai…")
                        current != null && state.queue.size > 1 && state.queueIndex >= 0 -> "${current.title} · ${state.queueIndex + 1}/${state.queue.size}"
                        current != null -> current.title
                        else -> label("Tap a letter or recording to listen. Internet needed for first playback.", "Harf ya recording par tap karke sunein. Pehli baar internet chahiye.")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("qaida_audio_status")
                )
            }
        }
        if (clips.isEmpty()) return@AppCard
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth().clip(RoundedCornerShape(50)), color = accent)
        val resume = { if (current != null) audio.play(current) else audio.playAll(clips) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { audio.previous() }, enabled = state.queue.size > 1 && state.queueIndex > 0, modifier = Modifier.testTag("qaida_audio_prev")) {
                Icon(Icons.Default.SkipPrevious, contentDescription = label("Previous recording", "Pichhli recording"))
            }
            Spacer(Modifier.width(Spacing.lg))
            FilledIconButton(onClick = resume, modifier = Modifier.size(60.dp).testTag("qaida_audio_toggle"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = accent, contentColor = MaterialTheme.colorScheme.surface)) {
                when {
                    state.failed -> Icon(Icons.Default.Refresh, contentDescription = label("Retry", "Dobara koshish"), modifier = Modifier.size(30.dp))
                    state.playing -> Icon(Icons.Default.Pause, contentDescription = label("Pause", "Rokein"), modifier = Modifier.size(30.dp))
                    else -> Icon(Icons.Default.PlayArrow, contentDescription = label("Play", "Sunein"), modifier = Modifier.size(30.dp))
                }
            }
            Spacer(Modifier.width(Spacing.lg))
            IconButton(onClick = { audio.next() }, enabled = state.queue.size > 1 && state.queueIndex < state.queue.lastIndex, modifier = Modifier.testTag("qaida_audio_next")) {
                Icon(Icons.Default.SkipNext, contentDescription = label("Next recording", "Agli recording"))
            }
        }
        if (clips.size > 1) {
            FilledTonalButton(onClick = { audio.playAll(clips) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qaida_audio_play_all")) {
                Icon(Icons.Default.PlaylistPlay, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(label("Play all (${clips.size})", "Sab sunein (${clips.size})"))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        // Secondary controls in one quiet row: speed on the start, repeat and stop on the end.
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label("Speed", "Raftaar"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically))
            qaidaAudioSpeeds.forEach { speed ->
                FilterChip(selected = state.speed == speed, onClick = { audio.speed(speed) }, shape = CircleShape,
                    label = { Text(qaidaSpeedText(speed)) }, modifier = Modifier.testTag(qaidaSpeedTag(speed)))
            }
            Spacer(Modifier.weight(1f))
            FilledTonalIconToggleButton(checked = state.repeat, onCheckedChange = { audio.repeat(it) }, modifier = Modifier.testTag("qaida_audio_repeat")) {
                Icon(Icons.Default.Repeat, contentDescription = label("Repeat", "Dobara chalayein"))
            }
            IconButton(onClick = { audio.stop() }, enabled = state.clipId != null, modifier = Modifier.testTag("qaida_audio_stop")) {
                Icon(Icons.Default.Stop, contentDescription = label("Stop", "Band karein"))
            }
        }
        Text(source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A whole-ayah recording: play state, its reference and the Arabic it demonstrates. */
@Composable
internal fun QaidaClipRow(clip: QaidaAudioClip, state: QaidaAudioState, accent: Color, label: QaidaLabel, onClick: () -> Unit) {
    val active = state.clipId == clip.id
    AppCard(
        modifier = Modifier.fillMaxWidth().testTag("qaida_audio_${clip.id}"),
        onClick = onClick,
        containerColor = if (active) accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).background(if (active) accent else accent.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) {
                val tint = if (active) MaterialTheme.colorScheme.surface else accent
                when {
                    active && state.loading -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = tint)
                    active && state.playing -> Icon(Icons.Default.Pause, contentDescription = label("Pause", "Rokein"), tint = tint)
                    else -> Icon(Icons.Default.PlayArrow, contentDescription = label("Play", "Sunein"), tint = tint)
                }
            }
            Text(clip.title, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (clip.arabic.isNotBlank()) Text(clip.arabic, fontFamily = ArabicFontFamily, fontSize = 24.sp, lineHeight = 42.sp,
                color = if (active) accent else MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Every reading drill in one card, separated by hairlines, right to left. */
@Composable
internal fun QaidaArabicExamples(examples: List<String>, fontSize: Int) {
    if (examples.isEmpty()) return
    AppCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm)) {
        examples.forEachIndexed { i, text ->
            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Text(text, fontFamily = ArabicFontFamily, fontSize = fontSize.sp, lineHeight = (fontSize * 1.75f).sp,
                textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineLarge.copy(textDirection = TextDirection.ContentOrRtl),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
        }
    }
}

/** Rule letters as tappable chips, laid out right to left. */
@Composable
internal fun QaidaLetterChips(letters: List<String>, accent: Color, onTap: (String) -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            letters.forEach { letter ->
                Surface(onClick = { onTap(letter) }, shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)), modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(letter, fontFamily = ArabicFontFamily, fontSize = 26.sp, lineHeight = 44.sp, color = accent,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
                }
            }
        }
    }
}

/** Answer tile shared by the quiz and the listening drill: green when right, red when the wrong pick. */
@Composable
internal fun QaidaAnswerTile(
    text: String, right: Boolean, wrong: Boolean, answered: Boolean, accent: Color, tag: String,
    arabic: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val error = MaterialTheme.colorScheme.error
    val container = when { right -> accent.copy(alpha = 0.16f); wrong -> error.copy(alpha = 0.12f); else -> MaterialTheme.colorScheme.surface }
    val content = when { right -> accent; wrong -> error; answered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f); else -> MaterialTheme.colorScheme.onSurface }
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick, enabled = !answered,
        modifier = modifier.heightIn(min = if (arabic) 64.dp else 52.dp).pressScale(interaction).testTag(tag),
        shape = MaterialTheme.shapes.medium, color = container, contentColor = content,
        border = BorderStroke(if (right || wrong) 2.dp else 1.dp, when { right -> accent; wrong -> error; else -> MaterialTheme.colorScheme.outlineVariant }),
        interactionSource = interaction
    ) {
        Box(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
            Text(text, fontFamily = if (arabic) ArabicFontFamily else null, fontSize = if (arabic) 30.sp else 18.sp,
                lineHeight = if (arabic) 48.sp else 24.sp, textAlign = TextAlign.Center, color = content)
            if (right || wrong) Icon(if (right) Icons.Default.CheckCircle else Icons.Default.Cancel, contentDescription = null, tint = content,
                modifier = Modifier.align(Alignment.TopEnd).size(16.dp))
        }
    }
}

/** Feedback line with an icon; [tag] keeps the text node addressable by tests. */
@Composable
internal fun QaidaFeedback(text: String, good: Boolean, accent: Color, tag: String) {
    val tint = if (good) accent else MaterialTheme.colorScheme.error
    Surface(shape = MaterialTheme.shapes.medium, color = tint.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(if (good) Icons.Default.CheckCircle else Icons.Default.Cancel, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Text(text, modifier = Modifier.testTag(tag), fontWeight = FontWeight.SemiBold, color = tint)
        }
    }
}

/**
 * One question at a time. Each question is answered once; the score counts correct answers and the
 * lesson completes on a perfect score. [onFinished] fires once per attempt.
 */
@Composable
internal fun QaidaQuizCard(
    lesson: QaidaLesson, roman: Boolean, accent: Color, label: QaidaLabel, hasNext: Boolean,
    onFinished: (correct: Int, total: Int) -> Unit, onNext: () -> Unit
) {
    val quizzes = lesson.quizzes
    val haptics = LocalHapticFeedback.current
    var index by rememberSaveable(lesson.id) { mutableStateOf(0) }
    var picked by rememberSaveable(lesson.id) { mutableStateOf<Int?>(null) }
    var correct by rememberSaveable(lesson.id) { mutableStateOf(0) }
    val quiz = quizzes[index.coerceIn(0, quizzes.lastIndex)]
    val last = index >= quizzes.lastIndex
    val finished = last && picked != null
    val perfect = finished && correct == quizzes.size
    // Short options (letters, numbers) sit side by side as big tiles; longer ones stack.
    val compact = quiz.options.all { it.length <= 4 }
    val arabicOptions = quiz.options.any { option -> option.any { it in '\u0600'..'\u06FF' } }
    AppCard(Modifier.fillMaxWidth().animateContentSize(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.Quiz, accent, size = 40.dp, iconSize = 22.dp)
            Spacer(Modifier.width(12.dp))
            Text(label("Quick practice", "Chhoti mashq"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).semantics { heading() })
            if (quizzes.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.semantics {
                    contentDescription = label("Question ${index + 1} of ${quizzes.size}", "Sawal ${index + 1} / ${quizzes.size}")
                }) {
                    quizzes.indices.forEach { i ->
                        Box(Modifier.size(if (i == index) 10.dp else 8.dp).background(
                            if (i <= index) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape))
                    }
                }
            }
        }
        Text(if (roman) quiz.romanQuestion else quiz.question, style = MaterialTheme.typography.titleMedium)
        val answer: (Int) -> Unit = { i ->
            picked = i
            if (i == quiz.answer) correct += 1 else haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (last) onFinished(correct, quizzes.size)
        }
        if (compact) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                quiz.options.forEachIndexed { i, option ->
                    QaidaAnswerTile(option, right = picked != null && i == quiz.answer, wrong = picked == i && i != quiz.answer,
                        answered = picked != null, accent = accent, tag = "qaida_answer_$i", arabic = arabicOptions, modifier = Modifier.weight(1f)) { answer(i) }
                }
            }
        } else {
            quiz.options.forEachIndexed { i, option ->
                QaidaAnswerTile(option, right = picked != null && i == quiz.answer, wrong = picked == i && i != quiz.answer,
                    answered = picked != null, accent = accent, tag = "qaida_answer_$i", arabic = arabicOptions && quiz.options.all { it.length <= 12 }, modifier = Modifier.fillMaxWidth()) { answer(i) }
            }
        }
        if (picked != null) {
            val ok = picked == quiz.answer
            QaidaFeedback(
                when {
                    perfect -> label("Correct! Lesson completed.", "Sahi! Sabaq mukammal.")
                    finished -> label("Not complete yet. Re-read the rule above and try again.", "Abhi mukammal nahi. Upar ka qaida parh kar dobara koshish karein.")
                    ok -> label("Correct!", "Sahi!")
                    else -> label("Not quite. The correct answer is highlighted.", "Ghalat. Sahi jawab highlight hai.")
                },
                good = perfect || (!finished && ok), accent = accent, tag = "qaida_feedback"
            )
        }
        if (finished) {
            if (quizzes.size > 1) Text(label("Score: $correct / ${quizzes.size}", "Score: $correct / ${quizzes.size}"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("qaida_score"))
            val retry = { index = 0; picked = null; correct = 0 }
            if (perfect) {
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qaida_next")) {
                    Text(if (hasNext) label("Next lesson", "Agla sabaq") else label("Back to lessons", "Sabaq ki list"))
                }
                TextButton(onClick = retry, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("qaida_retry")) { Text(label("Practise again", "Dobara mashq karein")) }
            } else {
                Button(onClick = retry, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qaida_retry")) { Text(label("Try again", "Dobara koshish karein")) }
            }
        } else if (picked != null) {
            Button(onClick = { index += 1; picked = null }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qaida_quiz_next")) {
                Text(label("Next question", "Agla sawal"))
            }
        }
    }
}
