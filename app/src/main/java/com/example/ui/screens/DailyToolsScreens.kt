@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.components.BackIconButton
import com.example.ui.components.ProvenanceCard
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.ArabicFontFamily
import com.example.viewmodel.MainUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import kotlin.math.ceil
import kotlin.math.max

/** Each daily tool is its own tile on the More tab and its own full screen. */
enum class DailyTool(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val tag: String
) {
    SALAH("Namaz / Salah", "Prayer guide & special prayers", Icons.Default.Mosque, Color(0xFF237A63), "salah"),
    PRAYER_TRACKER("Prayer tracker", "Log salah & Qaza balance", Icons.Default.TaskAlt, Color(0xFF237A63), "prayer_tracker"),
    QURAN_GOAL("Quran goal", "Daily pages & khatm", Icons.Default.LocalLibrary, Color(0xFF3C6FA8), "quran_goal"),
    ZAKAT("Zakat calculator", "Estimate your 2.5% Zakat", Icons.Default.VolunteerActivism, Color(0xFFA9812A), "zakat"),
    HAJJ_UMRAH("Hajj & Umrah", "Step-by-step guide", Icons.Default.Luggage, Color(0xFF8A5A3C), "hajj_umrah"),
    MUSLIMAH("Muslimah mode", "Private cycle-aware log", Icons.Default.Spa, Color(0xFFB0507A), "muslimah"),
    QUIZ("Islamic quiz", "Test your knowledge", Icons.Default.Quiz, Color(0xFF6B5BAE), "quiz"),
    CHALLENGES("City challenges", "Your area vs the whole city", Icons.Default.LocationCity, Color(0xFF8A5A3C), "challenges")
}

// Storage keys are unchanged from the combined Daily tools screen so existing logs carry over.
private const val PREFS_NAME = "daily_tools"
internal val SuccessGreen = Color(0xFF2E8B57)

@Composable
fun DailyToolScreen(
    tool: DailyTool,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenQuran: () -> Unit = {},
    onOpenQibla: () -> Unit = {},
    uiState: MainUiState? = null
) {
    when (tool) {
        DailyTool.SALAH -> SalahScreen(onBack, onOpenQibla, modifier)
        DailyTool.PRAYER_TRACKER -> PrayerTrackerScreen(onBack, modifier)
        DailyTool.QURAN_GOAL -> QuranGoalScreen(onBack, onOpenQuran, modifier)
        DailyTool.ZAKAT -> ZakatScreen(onBack, modifier)
        DailyTool.HAJJ_UMRAH -> HajjUmrahScreen(onBack, modifier)
        DailyTool.MUSLIMAH -> MuslimahScreen(onBack, modifier)
        DailyTool.QUIZ -> QuizScreen(onBack, modifier)
        DailyTool.CHALLENGES -> CityChallengesScreen(onBack, uiState, modifier)
    }
}

// ---------------------------------------------------------------- Muslimah mode

@Composable
private fun MuslimahScreen(onBack: () -> Unit, modifier: Modifier) {
    val prefs = rememberToolPrefs()
    val today = remember { LocalDate.now() }
    var enabled by remember { mutableStateOf(prefs.getBoolean("muslimah_mode", false)) }
    var excused by remember { mutableStateOf(prefs.getBoolean("cycle_$today", false)) }
    var fastsToMakeUp by remember { mutableIntStateOf(prefs.getInt("roza_qaza", 0)) }
    val excusedThisMonth = remember(excused) {
        (1..today.dayOfMonth).count { prefs.getBoolean("cycle_${today.withDayOfMonth(it)}", false) }
    }

    ToolScreen(DailyTool.MUSLIMAH, onBack, modifier) { accent ->
        item {
            HeroCard(accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Default.Lock, accent, 44.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        LocalizedText("Private & on-device", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        LocalizedText(
                            "Nothing here leaves your phone or syncs anywhere.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            ToolCard {
                SwitchRow(
                    title = "Enable Muslimah mode",
                    description = "Mark days when you are excused from salah.",
                    checked = enabled,
                    onCheckedChange = { enabled = it; prefs.edit().putBoolean("muslimah_mode", it).apply() }
                )
            }
        }
        item {
            AnimatedVisibility(visible = enabled) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ToolCard {
                        SwitchRow(
                            title = "Excused from salah today",
                            description = "The prayer tracker will show today as excused.",
                            checked = excused,
                            onCheckedChange = { excused = it; prefs.edit().putBoolean("cycle_$today", it).apply() }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LocalizedText("Excused days this month", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("$excusedThisMonth", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
                        }
                    }
                    ToolCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                LocalizedText("Fasts to make up", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                LocalizedText(
                                    "Missed Ramadan fasts are made up later.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Stepper(
                                value = fastsToMakeUp,
                                onDecrement = { fastsToMakeUp = max(0, fastsToMakeUp - 1); prefs.edit().putInt("roza_qaza", fastsToMakeUp).apply() },
                                onIncrement = { fastsToMakeUp += 1; prefs.edit().putInt("roza_qaza", fastsToMakeUp).apply() }
                            )
                        }
                    }
                }
            }
        }
        item {
            ProvenanceCard(
                sourceText = "We were commanded to make up the fasts, but not the prayers.",
                collectionOrReference = "Sahih Muslim 335 · Aisha (RA)"
            )
        }
        item { InfoNote("For your own situation, ask a qualified scholar or a trusted mufti.") }
    }
}

@Composable
private fun SwitchRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            LocalizedText(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            LocalizedText(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ---------------------------------------------------------------- Quiz

@Composable
private fun QuizScreen(onBack: () -> Unit, modifier: Modifier) {
    val prefs = rememberToolPrefs()
    var round by rememberSaveable { mutableIntStateOf(0) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var picked by rememberSaveable { mutableStateOf<String?>(null) }
    // Question id -> the wrong option the user tapped, in answer order, for the review list.
    var wrongPicks by rememberSaveable { mutableStateOf(LinkedHashMap<String, String>()) }
    var bestScore by remember { mutableIntStateOf(prefs.getInt("quiz_best", 0)) }
    var roundsPlayed by remember { mutableIntStateOf(prefs.getInt("quiz_rounds", 0)) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    // Parsing the bundled Quran and hadith text takes a moment, so it happens off the main thread.
    val engine by produceState<QuizEngine?>(null, language) {
        value = withContext(Dispatchers.Default) { QuizEngine.create(context.applicationContext, prefs, language) }
    }
    var questionIds by rememberSaveable { mutableStateOf(ArrayList<String>()) }
    LaunchedEffect(engine) {
        val ready = engine ?: return@LaunchedEffect
        if (questionIds.isEmpty()) questionIds = ArrayList(withContext(Dispatchers.Default) { ready.planRound() })
    }
    val order = remember(questionIds, engine) { engine?.let { questionIds.mapNotNull(it::resolve) }.orEmpty() }
    val loading = order.isEmpty()

    // LazyColumn runs its content lambda lazily, so it must only see plain values captured here.
    // Reading `index` inside it could observe the post-click value with a stale `finished` flag
    // and index past the end of the round - that was the crash when the last question ended.
    val total = order.size
    val question = order.getOrNull(index)
    val finished = !loading && question == null
    val currentPick = picked
    val currentScore = score
    val review = wrongPicks.mapNotNull { (id, pick) -> order.firstOrNull { it.id == id }?.let { it to pick } }
    val options = remember(round, index, order) { question?.options?.shuffled().orEmpty() }
    LaunchedEffect(index, round) { listState.scrollToItem(0) }

    fun nextRound() {
        questionIds = ArrayList(engine?.planRound().orEmpty())
        round++
        index = 0
        score = 0
        picked = null
        wrongPicks = LinkedHashMap()
    }

    fun advance() {
        if (index >= total - 1) {
            roundsPlayed++
            bestScore = max(bestScore, score)
            prefs.edit().putInt("quiz_rounds", roundsPlayed).putInt("quiz_best", bestScore).apply()
        }
        index = (index + 1).coerceAtMost(total)
        picked = null
    }

    fun choose(q: QuizQuestion, option: String) {
        if (picked != null) return
        picked = option
        val correct = option == q.answer
        if (correct) score++ else wrongPicks = LinkedHashMap(wrongPicks).apply { put(q.id, option) }
        engine?.recordAnswer(q, correct)
    }

    Column(modifier.fillMaxSize()) {
        ToolScreen(DailyTool.QUIZ, onBack, Modifier.weight(1f), listState) { accent ->
            if (loading) {
                item(key = "quiz_loading") { QuizLoading(accent) }
            } else if (question == null) {
                quizResult(accent, currentScore, total, bestScore, roundsPlayed, review)
            } else {
                quizQuestion(accent, question, index, total, currentScore, currentPick, options) { choose(question, it) }
            }
        }
        if (finished || currentPick != null) {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (finished) {
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                            LocalizedText("Back", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Button(
                        onClick = { if (finished) nextRound() else advance() },
                        modifier = Modifier.weight(if (finished) 1.4f else 1f).heightIn(min = 48.dp).testTag("quiz_next")
                    ) {
                        LocalizedText(
                            when {
                                finished -> "Next round"
                                index >= total - 1 -> "See result"
                                else -> "Next question"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            if (finished) Icons.Default.Replay else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.quizQuestion(
    accent: Color,
    question: QuizQuestion,
    index: Int,
    total: Int,
    score: Int,
    picked: String?,
    options: List<String>,
    onPick: (String) -> Unit
) {
    item(key = "quiz_progress") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${localizedUiText("Question")} ${index + 1} / $total",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = CircleShape, color = accent.copy(alpha = 0.14f)) {
                    Text(
                        "${localizedUiText("Score")}: $score",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else (index + if (picked != null) 1 else 0).toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = accent,
                trackColor = accent.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {}
            )
        }
    }
    item(key = "quiz_question_${question.id}") {
        HeroCard(accent) {
            QuizPrompt(question, MaterialTheme.typography.titleLarge)
            question.passage?.let { passage ->
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)) {
                    QuizContentText(
                        passage, question, question.passageArabic, MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
    item(key = "quiz_options_${question.id}") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEachIndexed { optionIndex, option ->
                QuizOption(
                    text = option,
                    question = question,
                    letter = ('A' + optionIndex).toString(),
                    state = when {
                        picked == null -> OptionState.Idle
                        option == question.answer -> OptionState.Correct
                        option == picked -> OptionState.Wrong
                        else -> OptionState.Dimmed
                    },
                    onClick = { onPick(option) }
                )
            }
        }
    }
    if (picked != null) {
        item(key = "quiz_feedback_${question.id}") {
            val correct = picked == question.answer
            ToolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (correct) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (correct) SuccessGreen else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    LocalizedText(
                        if (correct) "Correct" else "Correct answer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!correct) {
                    QuizContentText(question.answer, question, question.optionsArabic, MaterialTheme.typography.bodyLarge, FontWeight.SemiBold, SuccessGreen)
                }
                ReferenceRow(question.reference, accent)
            }
        }
    }
}

private fun LazyListScope.quizResult(
    accent: Color,
    score: Int,
    total: Int,
    bestScore: Int,
    roundsPlayed: Int,
    review: List<Pair<QuizQuestion, String>>
) {
    val fraction = if (total == 0) 0f else score.toFloat() / total
    item(key = "quiz_result") {
        HeroCard(accent) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                LocalizedText("Round complete", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                ProgressRing(fraction, accent, 132.dp, stroke = 10.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$score / $total", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = accent)
                        Text("${(fraction * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))
                LocalizedText(
                    when {
                        total > 0 && score == total -> "Perfect score. MashaAllah!"
                        fraction >= 0.7f -> "Great work. Keep it up!"
                        fraction >= 0.4f -> "Good effort. Review the answers below."
                        else -> "Keep learning. Every question is a step forward."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    item(key = "quiz_stats") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuizStat("Correct", "$score", SuccessGreen, Icons.Default.CheckCircle, Modifier.weight(1f))
            QuizStat("Wrong", "${total - score}", MaterialTheme.colorScheme.error, Icons.Default.Cancel, Modifier.weight(1f))
            QuizStat("Best score", "$bestScore", accent, Icons.Default.EmojiEvents, Modifier.weight(1f))
        }
    }
    item(key = "quiz_rounds") {
        InfoNote("${localizedUiText("Rounds played")}: $roundsPlayed")
    }
    if (review.isEmpty()) {
        item(key = "quiz_no_review") {
            ToolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    LocalizedText("No mistakes this round.", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
    } else {
        item(key = "quiz_review_title") { SectionTitle("Review these questions") }
        items(review, key = { "quiz_review_${it.first.id}" }) { (missed, yourPick) ->
            ToolCard {
                QuizPrompt(missed, MaterialTheme.typography.titleMedium)
                missed.passage?.let { QuizContentText(it, missed, missed.passageArabic, MaterialTheme.typography.bodyLarge) }
                AnswerLine(Icons.Default.Cancel, MaterialTheme.colorScheme.error, "Your answer", yourPick, missed)
                AnswerLine(Icons.Default.CheckCircle, SuccessGreen, "Correct answer", missed.answer, missed)
                ReferenceRow(missed.reference, accent)
            }
        }
    }
}

@Composable
private fun QuizStat(label: String, value: String, color: Color, icon: ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            LocalizedText(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AnswerLine(icon: ImageVector, color: Color, label: String, answer: String, question: QuizQuestion) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(top = 2.dp).size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            LocalizedText(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            QuizContentText(answer, question, question.optionsArabic, MaterialTheme.typography.bodyLarge, FontWeight.SemiBold, color)
        }
    }
}

@Composable
private fun ReferenceRow(reference: String, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "${localizedUiText("Reference")}: $reference",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private enum class OptionState { Idle, Correct, Wrong, Dimmed }

/** Bank questions are English UI copy; generated prompts fill `{x}` after translation. */
@Composable
private fun QuizPrompt(question: QuizQuestion, style: TextStyle) {
    if (question.generated) {
        Text(localizedUiText(question.question).replace("{x}", question.promptArg.orEmpty()), style = style, fontWeight = FontWeight.SemiBold)
    } else {
        LocalizedText(question.question, style = style, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Generated text is already in the reader's language (or Arabic), so it is shown verbatim;
 * translating it word by word could change a religious meaning. Arabic uses the app's Arabic face.
 */
@Composable
private fun QuizContentText(
    text: String,
    question: QuizQuestion,
    arabic: Boolean,
    style: TextStyle,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    when {
        arabic -> Text(
            text,
            modifier = modifier,
            color = color,
            style = style.copy(
                fontFamily = ArabicFontFamily,
                fontSize = style.fontSize * 1.2f,
                lineHeight = style.fontSize * 2.2f,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Right
            )
        )
        question.generated -> Text(text, modifier = modifier, color = color, style = style, fontWeight = fontWeight)
        else -> LocalizedText(text, modifier = modifier, color = color, style = style, fontWeight = fontWeight)
    }
}

@Composable
private fun QuizLoading(accent: Color) {
    ToolCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = accent, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            LocalizedText("Preparing questions…", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun QuizOption(text: String, question: QuizQuestion, letter: String, state: OptionState, onClick: () -> Unit) {
    val error = MaterialTheme.colorScheme.error
    val borderColor = when (state) {
        OptionState.Correct -> SuccessGreen
        OptionState.Wrong -> error
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    val container = when (state) {
        OptionState.Correct -> SuccessGreen.copy(alpha = 0.12f)
        OptionState.Wrong -> error.copy(alpha = 0.10f)
        else -> MaterialTheme.colorScheme.surface
    }
    Card(
        onClick = onClick,
        enabled = state == OptionState.Idle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = container,
            disabledContainerColor = container,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (state == OptionState.Dimmed) 0.5f else 1f)
        ),
        border = BorderStroke(if (state == OptionState.Correct || state == OptionState.Wrong) 2.dp else 1.dp, borderColor)
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = if (state == OptionState.Correct) SuccessGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant) {
                Text(letter, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (state == OptionState.Correct) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
            Spacer(Modifier.width(12.dp))
            QuizContentText(text, question, question.optionsArabic, MaterialTheme.typography.bodyLarge, FontWeight.Medium, modifier = Modifier.weight(1f))
            when (state) {
                OptionState.Correct -> Icon(Icons.Default.CheckCircle, contentDescription = localizedUiText("Correct"), tint = SuccessGreen)
                OptionState.Wrong -> Icon(Icons.Default.Cancel, contentDescription = localizedUiText("Wrong"), tint = error)
                else -> Unit
            }
        }
    }
}

// ---------------------------------------------------------------- Shared building blocks

@Composable
internal fun rememberToolPrefs(): SharedPreferences {
    val context = LocalContext.current
    return remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
}

@Composable
internal fun ToolScreen(
    tool: DailyTool,
    onBack: () -> Unit,
    modifier: Modifier,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.(accent: Color) -> Unit
) {
    val accent = themedAccent(tool.accent)
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("${tool.tag}_screen"),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "header") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BackIconButton(onBack)
                Spacer(Modifier.width(4.dp))
                IconBadge(tool.icon, accent, 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    LocalizedText(
                        tool.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    LocalizedText(
                        tool.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        content(accent)
    }
}

@Composable
internal fun IconBadge(icon: ImageVector, accent: Color, size: Dp) {
    Surface(shape = RoundedCornerShape(size * 0.3f), color = accent.copy(alpha = 0.15f), modifier = Modifier.size(size)) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.padding(size * 0.22f))
    }
}

@Composable
internal fun HeroCard(accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.20f), accent.copy(alpha = 0.04f))))
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
internal fun ToolCard(
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(contentPadding), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
internal fun SectionTitle(text: String) {
    LocalizedText(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
internal fun InfoNote(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp)) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            LocalizedText(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun ProgressRing(
    progress: Float,
    accent: Color,
    size: Dp,
    stroke: Dp = 8.dp,
    center: @Composable () -> Unit
) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            color = accent,
            strokeWidth = stroke,
            trackColor = accent.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
        center()
    }
}

@Composable
internal fun Stepper(value: Int, onDecrement: () -> Unit, onIncrement: () -> Unit, minimum: Int = 0) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onDecrement, enabled = value > minimum, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Default.Remove, contentDescription = localizedUiText("Decrease"))
        }
        Text(
            "$value",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 40.dp)
        )
        FilledTonalIconButton(onClick = onIncrement, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Default.Add, contentDescription = localizedUiText("Increase"))
        }
    }
}
