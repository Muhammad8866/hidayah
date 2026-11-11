package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.IconBadge
import com.example.ui.components.OnReconnect
import com.example.ui.components.rememberIsOnline
import com.example.ui.localization.LocalizedText
import com.example.ui.tour.tourTarget
import com.example.ui.localization.localizedUiText
import com.example.ui.localization.localizedUiTemplate

private fun speedLabel(speed: Float) = (if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()) + "×"

/**
 * Sticky mini player. Idle it is one slim "Play all" row; while reciting it shows what is playing,
 * how far through the collection it is, and the transport. Speed, repeat and the sleep timer are
 * tucked behind the tune button so the bar stays calm.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecitationPlayerBar(audio: RecitationAudio, passages: List<RecitationPassage>, modifier: Modifier = Modifier) {
    val state by audio.state.collectAsState()
    val haptics = LocalHapticFeedback.current
    val isOnline by rememberIsOnline()
    // A stream that died offline starts again by itself once the connection is back.
    OnReconnect(enabled = state.failed) { audio.retry() }
    val current = if (state.id == null) null else passages.firstOrNull { it.id == state.id } ?: audio.current
    if (current == null && passages.isEmpty()) return
    var showOptions by rememberSaveable { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth().testTag("collection_player_bar"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (current != null) 0.35f else 0.15f)),
        shadowElevation = if (current != null) 6.dp else 2.dp
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (current == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Default.Headphones, MaterialTheme.colorScheme.primary, size = 40.dp, iconSize = 22.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        LocalizedText("Play all", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        LocalizedText(ManzilRuqyahAudioCatalog.QURAN_RECITER, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    FilledIconButton(onClick = {
                        com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_play_all"); haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        audio.playAll(passages)
                    }, modifier = Modifier.size(48.dp).testTag("collection_play_all").tourTarget("recitation_play_all")) {
                        Icon(Icons.Default.PlayArrow, contentDescription = localizedUiText("Play all"))
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(
                        icon = if (state.playing) Icons.Default.GraphicEq else Icons.Default.Headphones,
                        accent = MaterialTheme.colorScheme.primary, size = 40.dp, iconSize = 22.dp, filled = state.playing
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        LocalizedText(current.title, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(localizedUiTemplate("Quran {surah}:{ayah}", "surah" to current.surah, "ayah" to (state.ayah ?: current.first)) +
                            if (state.queueSize > 1) "  •  ${state.queueIndex + 1} / ${state.queueSize}" else "",
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                    }
                    IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_prev_ayah"); audio.previousAyah() }, modifier = Modifier.tourTarget("recitation_prev_ayah")) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = localizedUiText("Previous ayah"))
                    }
                    FilledIconButton(onClick = {
                        com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_toggle"); haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        audio.toggle()
                    }, modifier = Modifier.size(48.dp).testTag("collection_player_toggle").tourTarget("recitation_player_toggle")) {
                        if (state.playing) Icon(Icons.Default.Pause, contentDescription = localizedUiText("Pause"))
                        else Icon(Icons.Default.PlayArrow, contentDescription = localizedUiText("Listen"))
                    }
                    IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_next_ayah"); audio.nextAyah() }, modifier = Modifier.tourTarget("recitation_next_ayah")) {
                        Icon(Icons.Default.SkipNext, contentDescription = localizedUiText("Next ayah"))
                    }
                }
                // How far through the queue the recitation is, ayah by ayah.
                val total = if (state.queueSize > 1) state.queueSize else 1
                val within = (((state.ayah ?: current.first) - current.first + 1).toFloat() /
                    (current.last - current.first + 1)).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { ((state.queueIndex + within) / total).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().tourTarget("recitation_player_progress").height(4.dp).clip(CircleShape),
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round, gapSize = 0.dp, drawStopIndicator = {}
                )
                if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp).clip(CircleShape))
                if (state.failed) Row(verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText(if (isOnline) "Audio could not play. Try again." else "You're offline",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_retry"); audio.retry() }) { LocalizedText("Retry") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // A quiet summary of the options; tapping it reveals them.
                    TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_options"); showOptions = !showOptions }, modifier = Modifier.weight(1f).tourTarget("recitation_player_options")) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            speedLabel(state.speed) + "  •  " + (if (state.repeat == 0) "∞" else "${state.repeat}×") +
                                (state.sleepMinutesLeft?.let { "  •  $it ${localizedUiText("min")}" } ?: ""),
                            style = MaterialTheme.typography.labelLarge, maxLines = 1
                        )
                        Spacer(Modifier.weight(1f))
                    }
                    TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_stop"); audio.stop() }, modifier = Modifier.testTag("collection_player_stop").tourTarget("recitation_player_stop")) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        LocalizedText("Stop")
                    }
                }
                AnimatedVisibility(showOptions, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_speed"); audio.setSpeed(recitationSpeeds.recitationNext(state.speed)) }, modifier = Modifier.tourTarget("recitation_speed"),
                            label = { Text("${localizedUiText("Speed")} ${speedLabel(state.speed)}") })
                        AssistChip(onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_repeat"); audio.setRepeat(recitationRepeats.recitationNext(state.repeat)) }, modifier = Modifier.tourTarget("recitation_repeat"),
                            label = { Text("${localizedUiText("Repeat")} ${if (state.repeat == 0) "∞" else "${state.repeat}×"}") })
                        FilterChip(selected = state.sleepMinutesLeft != null, modifier = Modifier.tourTarget("recitation_sleep_timer"),
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("recitation_collection_screen", "player_sleep_timer"); audio.setSleepTimer(nextRecitationSleep(state.sleepMinutesLeft)) },
                            label = { Text("${localizedUiText("Sleep timer")}: " + (state.sleepMinutesLeft
                                ?.let { "$it ${localizedUiText("min")}" } ?: localizedUiText("Off"))) })
                    }
                }
            }
        }
    }
}
