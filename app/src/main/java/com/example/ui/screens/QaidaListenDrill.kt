package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.core.analytics.AppAnalytics
import com.example.ui.components.AppCard
import com.example.ui.components.IconBadge
import com.example.ui.components.Spacing
import kotlin.random.Random

internal const val QAIDA_DRILL_ROUNDS = 5

/** One listening round: the clip to hear and four Arabic choices (the answer among them). */
internal data class QaidaDrillRound(val target: QaidaAudioClip, val options: List<QaidaAudioClip>)

/**
 * Builds round [round] of a drill from [pool] deterministically from [seed], so a rotation or
 * process restore shows the same question. Options are distinct by their Arabic text.
 */
internal fun qaidaDrillRound(pool: List<QaidaAudioClip>, seed: Int, round: Int): QaidaDrillRound {
    val distinct = pool.distinctBy { it.arabic }
    val random = Random(seed * 31 + round)
    val target = distinct[random.nextInt(distinct.size)]
    val options = (distinct.filter { it.id != target.id }.shuffled(random).take(3) + target).shuffled(random)
    return QaidaDrillRound(target, options)
}

/**
 * Hear → choose: plays a letter or syllable and asks which one it was. The played clip gets its own
 * id so the letter grid above does not light up and give the answer away.
 */
@Composable
internal fun QaidaListenDrill(
    lessonId: String, pool: List<QaidaAudioClip>, audio: QaidaAudioController, accent: Color, label: QaidaLabel
) {
    val haptics = LocalHapticFeedback.current
    var seed by rememberSaveable(lessonId) { mutableIntStateOf(Random.nextInt(1, 100_000)) }
    var round by rememberSaveable(lessonId) { mutableIntStateOf(0) }
    var picked by rememberSaveable(lessonId) { mutableStateOf<String?>(null) }
    var correct by rememberSaveable(lessonId) { mutableIntStateOf(0) }
    var heard by rememberSaveable(lessonId) { mutableStateOf(false) }
    val current = remember(pool, seed, round) { qaidaDrillRound(pool, seed, round) }
    val finished = round >= QAIDA_DRILL_ROUNDS - 1 && picked != null
    val play = {
        heard = true
        audio.play(current.target.copy(id = "drill-${current.target.id}"))
    }
    AppCard(Modifier.fillMaxWidth().animateContentSize().testTag("qaida_drill"), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.Hearing, accent, size = 40.dp, iconSize = 22.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label("Listen and choose", "Sun kar pehchanein"), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
                Text(label("Play the sound, then tap the letter you heard.", "Awaaz sunein, phir suna hua harf chunein."),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${round + 1}/$QAIDA_DRILL_ROUNDS", style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.Bold)
        }
        FilledTonalButton(onClick = play, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("qaida_drill_play")) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label("Play sound", "Awaaz sunein"))
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                current.options.forEachIndexed { i, option ->
                    val isAnswer = option.id == current.target.id
                    QaidaAnswerTile(
                        text = option.arabic,
                        right = picked != null && isAnswer,
                        wrong = picked == option.id && !isAnswer,
                        // Answers unlock once the sound has been played at least once.
                        answered = picked != null || !heard,
                        accent = accent, tag = "qaida_drill_option_$i", arabic = true,
                        modifier = Modifier.weight(1f)
                    ) {
                        picked = option.id
                        if (isAnswer) correct += 1 else haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (round >= QAIDA_DRILL_ROUNDS - 1) AppAnalytics.milestone("qaida_tajweed", "listening_drill_completed", lessonId)
                    }
                }
            }
        }
        val answer = picked
        if (answer != null) {
            val ok = answer == current.target.id
            QaidaFeedback(
                if (ok) label("Correct!", "Sahi!") else label("Not quite. The correct answer is highlighted.", "Ghalat. Sahi jawab highlight hai."),
                good = ok, accent = accent, tag = "qaida_drill_feedback"
            )
        }
        if (finished) {
            Text(label("Score: $correct / $QAIDA_DRILL_ROUNDS", "Score: $correct / $QAIDA_DRILL_ROUNDS"),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("qaida_drill_score"))
            Button(onClick = { seed += 1; round = 0; picked = null; correct = 0; heard = false },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qaida_drill_again")) {
                Text(label("Practise again", "Dobara mashq karein"))
            }
        } else if (picked != null) {
            Button(onClick = { round += 1; picked = null; heard = false },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qaida_drill_next")) {
                Text(label("Next question", "Agla sawal"))
            }
        }
    }
}
