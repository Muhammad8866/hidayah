package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppCard
import com.example.ui.components.HeroSurface
import com.example.ui.components.Spacing
import com.example.ui.components.pressScale
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText
import com.example.ui.localization.persistedAppLanguage
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.GoldAccent

internal fun kalimaArabicStyle(scale: Float, base: Int = 26) = TextStyle(
    fontFamily = ArabicFontFamily,
    fontSize = (base * scale).sp,
    lineHeight = (base * 1.85f * scale).sp,
    textDirection = TextDirection.Rtl,
    textAlign = TextAlign.Start
)

/** Number medallion: gold ring around the number (or a shield for Iman), filled once memorised. */
@Composable
internal fun KalimaMedallion(entry: KalimaEntry, memorised: Boolean, size: Dp = 44.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(
            if (memorised) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer)
            .border(1.5.dp, GoldAccent.copy(alpha = if (memorised) 0.9f else 0.55f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val tint = if (memorised) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
        when {
            memorised -> Icon(Icons.Default.Check, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
            entry.number != null -> Text(entry.number.toString(), color = tint, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            else -> Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.45f))
        }
    }
}

/**
 * One row of the list: medallion, name, short meaning and a one-line Arabic preview. Tapping opens
 * the reader; the star and the memorised check are the only inline actions.
 */
@Composable
internal fun KalimaCard(
    entry: KalimaEntry, english: Boolean, favourite: Boolean, memorised: Boolean,
    playing: Boolean, isLastRead: Boolean,
    onOpen: () -> Unit, onFavourite: () -> Unit, onMemorised: () -> Unit
) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Surface(
        onClick = onOpen,
        interactionSource = interaction,
        modifier = Modifier.fillMaxWidth().pressScale(interaction).testTag("kalima_${entry.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (playing) 1.5.dp else 1.dp,
            if (playing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        ),
        shadowElevation = if (playing) 4.dp else 0.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KalimaMedallion(entry, memorised)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.title, Modifier.weight(1f, fill = false), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (playing) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp))
                    }
                }
                val nameMeaning = if (english) entry.nameMeaningEnglish else entry.nameMeaning
                if (nameMeaning.isNotBlank()) {
                    Text(nameMeaning, style = MaterialTheme.typography.bodySmall,
                        color = if (isLastRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(entry.phrases.firstOrNull()?.arabic ?: entry.arabic, Modifier.fillMaxWidth().padding(top = 2.dp),
                    style = kalimaArabicStyle(1f, base = 19), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconToggleButton(favourite, { onFavourite() }, Modifier.testTag("kalima_fav_${entry.id}")) {
                    Icon(if (favourite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = localizedUiText(if (favourite) "Remove from Favourites" else "Add to Favourites"),
                        tint = if (favourite) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconToggleButton(memorised, { onMemorised() }, Modifier.testTag("kalima_memorised_${entry.id}")) {
                    Icon(if (memorised) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = localizedUiText(if (memorised) "Memorised" else "Mark as memorised"),
                        tint = if (memorised) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * Top of the list: memorisation progress and the one main action, opening the reader where you
 * stopped (or the next Kalima to learn). Listening and the quiz sit underneath as quieter actions.
 */
@Composable
internal fun KalimaProgressCard(
    memorised: Int, total: Int, resume: KalimaEntry, resumeIsContinue: Boolean, onResume: () -> Unit,
    canPlayAll: Boolean, playingAll: Boolean, onPlayAll: () -> Unit, onQuiz: () -> Unit
) {
    val language = LocalAppLanguage.current
    // Stays deep emerald in both themes, so its text and controls use fixed white and gold.
    HeroSurface(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        localizeUiTemplate("{memorised} of {total} memorised", language,
                            mapOf("memorised" to memorised.toString(), "total" to total.toString())),
                        Modifier.testTag("kalimas_progress"),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
                Text("${if (total == 0) 0 else memorised * 100 / total}%", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = GoldAccent)
            }
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else memorised.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = GoldAccent,
                trackColor = Color.White.copy(alpha = 0.18f),
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
            // Primary action: one wide, light button that reads clearly on the dark hero.
            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("kalimas_resume"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF0B2D25))
            ) {
                Text(
                    localizedUiText(if (resumeIsContinue) "Continue" else "Read") + ": " + resume.title,
                    Modifier.weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KalimaHeroAction(
                    icon = if (playingAll) Icons.Default.Stop else Icons.Default.PlayArrow,
                    label = if (playingAll) "Stop" else "Play all",
                    enabled = canPlayAll || playingAll, onClick = onPlayAll,
                    modifier = Modifier.weight(1f).testTag("kalimas_play_all")
                )
                KalimaHeroAction(
                    icon = Icons.Default.Quiz, label = "Test yourself", enabled = true, onClick = onQuiz,
                    modifier = Modifier.weight(1f).testTag("kalimas_quiz")
                )
            }
        }
    }
}

@Composable
private fun KalimaHeroAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, enabled: Boolean,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick, enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = Color.White, disabledContentColor = Color.White.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        LocalizedText(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Reading preferences live in a sheet, so the list and the reader stay uncluttered. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KalimaSettingsSheet(user: KalimaUserState, store: KalimaStore, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = Spacing.xl, end = Spacing.xl, bottom = Spacing.xxl)
                .testTag("kalimas_settings_panel"),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            LocalizedText("Reading settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            AppCard(Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), outlined = false) {
                Text("لَا إِلٰهَ إِلَّا اللّٰهُ", Modifier.fillMaxWidth(), style = kalimaArabicStyle(user.arabicScale))
            }
            LocalizedText("Arabic text size", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = user.arabicScale.coerceIn(KalimaStore.MIN_SCALE, KalimaStore.MAX_SCALE),
                onValueChange = store::setArabicScale,
                valueRange = KalimaStore.MIN_SCALE..KalimaStore.MAX_SCALE, steps = 9,
                modifier = Modifier.testTag("kalimas_arabic_size")
            )
            LocalizedText("Meaning language", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(user.meaning == KalimaMeaning.ROMAN_URDU, { store.setMeaning(KalimaMeaning.ROMAN_URDU) },
                    label = { Text("Roman Urdu") }, shape = CircleShape,
                    modifier = Modifier.heightIn(min = 40.dp).testTag("kalimas_meaning_urdu"))
                FilterChip(user.meaning == KalimaMeaning.ENGLISH, { store.setMeaning(KalimaMeaning.ENGLISH) },
                    label = { Text("English") }, shape = CircleShape,
                    modifier = Modifier.heightIn(min = 40.dp).testTag("kalimas_meaning_english"))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            KalimaSwitchRow("Show transliteration", user.showRoman, store::setShowRoman)
            KalimaSwitchRow("Show meaning", user.showMeaning, store::setShowMeaning)
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("kalimas_settings_done")) {
                LocalizedText("Done")
            }
        }
    }
}

@Composable
private fun KalimaSwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Switch) { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        LocalizedText(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked, onChange)
    }
}

/** What the collection is and where the text comes from; the source list folds away. */
@Composable
internal fun KalimaAboutCard(english: Boolean, sourcesOpen: Boolean, onToggleSources: () -> Unit) {
    AppCard(Modifier.fillMaxWidth().padding(top = Spacing.sm), contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp).size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (english) KalimasContent.ABOUT_ENGLISH else KalimasContent.ABOUT,
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LocalizedText("Traditional statements of faith and remembrance, presented for learning. Available offline.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        TextButton(onClick = onToggleSources, modifier = Modifier.heightIn(min = 48.dp)) {
            LocalizedText("Sources")
            Spacer(Modifier.width(4.dp))
            Icon(if (sourcesOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null,
                modifier = Modifier.size(18.dp))
        }
        if (sourcesOpen) Text(KalimasContent.SOURCES, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun copyKalima(context: Context, entry: KalimaEntry, english: Boolean) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(entry.title, KalimasContent.shareText(entry, english)))
    // Android 13+ shows its own clipboard confirmation.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, localizeUiText("Copied", persistedAppLanguage(context)), Toast.LENGTH_SHORT).show()
    }
}

internal fun shareKalima(context: Context, entry: KalimaEntry, english: Boolean) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain")
        .putExtra(Intent.EXTRA_SUBJECT, entry.title)
        .putExtra(Intent.EXTRA_TEXT, KalimasContent.shareText(entry, english))
    runCatching { context.startActivity(Intent.createChooser(send, entry.title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

/** A short multiple-choice round: one question at a time with clear right/wrong feedback and a score. */
@Composable
internal fun KalimaQuizDialog(english: Boolean, onDismiss: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val questions = remember(round, english) { KalimaQuiz.questions(System.nanoTime(), english = english) }
    var index by remember(round) { mutableIntStateOf(0) }
    var selected by remember(round, index) { mutableStateOf<Int?>(null) }
    var score by remember(round) { mutableIntStateOf(0) }
    val finished = index >= questions.size
    val question = questions.getOrNull(index)
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("kalimas_quiz_dialog"),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LocalizedText("Test yourself")
                if (!finished && questions.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { (index + 1).toFloat() / questions.size },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        gapSize = 0.dp, drawStopIndicator = {}
                    )
                    Text("${index + 1} / ${questions.size}", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (question == null) {
                    Text("$score / ${questions.size}", Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    LocalizedText(
                        when {
                            questions.isEmpty() -> "No questions available."
                            score == questions.size -> "MashaAllah! Every answer was correct."
                            score * 2 >= questions.size -> "Well done. Revise the ones you missed and try again."
                            else -> "Keep practising. Read each Kalima with its meaning and try again."
                        },
                        Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(question.prompt, style = MaterialTheme.typography.titleMedium)
                    question.arabic?.takeIf(String::isNotBlank)?.let {
                        Surface(shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)) {
                            Text(it, Modifier.fillMaxWidth().padding(12.dp), style = kalimaArabicStyle(1f, base = 22))
                        }
                    }
                    question.options.forEachIndexed { optionIndex, option ->
                        val answered = selected != null
                        val correct = optionIndex == question.correctIndex
                        val chosen = selected == optionIndex
                        val container = when {
                            answered && correct -> MaterialTheme.colorScheme.primaryContainer
                            answered && chosen -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val content = when {
                            answered && correct -> MaterialTheme.colorScheme.onPrimaryContainer
                            answered && chosen -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(
                            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                                .clickable(enabled = !answered, role = Role.RadioButton) {
                                    selected = optionIndex
                                    if (correct) score++
                                }
                                .testTag("kalimas_quiz_option_$optionIndex"),
                            shape = MaterialTheme.shapes.medium, color = container, contentColor = content
                        ) {
                            Row(Modifier.padding(12.dp).heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(option, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                if (answered && correct) Icon(Icons.Default.CheckCircle, contentDescription = localizedUiText("Correct"))
                            }
                        }
                    }
                    selected?.let { choice ->
                        val right = choice == question.correctIndex
                        LocalizedText(if (right) "Correct" else "Incorrect", fontWeight = FontWeight.Bold,
                            color = if (right) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            if (question == null) {
                if (questions.isNotEmpty()) TextButton(onClick = { round++ }) { LocalizedText("Try again") }
            } else {
                Button(onClick = { index++ }, enabled = selected != null) {
                    LocalizedText(if (index == questions.lastIndex) "Finish" else "Next")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Close") } }
    )
}
