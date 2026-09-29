package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.ui.localization.localizedUiText
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.audio.PlaybackSpeed
import com.example.core.audio.applySpeed
import com.example.core.id.AyahReference
import com.example.domain.model.Ayah
import com.example.quran.FullQuranCatalog
import com.example.quran.HifzAssessment
import com.example.quran.HifzAssessor
import com.example.quran.HifzQuranTextRepository
import com.example.quran.QuranAudioManager
import com.example.quran.domain.hifz.HifzCardState
import com.example.quran.domain.hifz.HifzCoachLogic
import com.example.quran.domain.hifz.HifzGrade
import com.example.quran.domain.hifz.HifzQueueItem
import com.example.quran.domain.hifz.HifzRevisionSession
import com.example.quran.domain.hifz.HifzSpacedRepetitionScheduler
import com.example.quran.domain.hifz.SmartHifzCard
import com.example.quran.domain.hifz.SmartHifzRevisionPlanner
import com.example.ui.components.PlaybackSpeedButton

private enum class CoachPage { HOME, PRACTICE, COMPLETE }
private enum class PracticeStep { LEARN, RECALL, CHECK }
private enum class SessionKind { REVISION, NEW, WEAK }

private const val TARGET_READS = 3
private val MistakeRed = Color(0xFFB73A32)

// Mushaf paper palette that follows the app theme (dark paper in dark mode).
private val hifzDark: Boolean @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.5f
private val HifzPaper: Color @Composable get() = if (hifzDark) Color(0xFF16140F) else MushafPaper
private val HifzCard: Color @Composable get() = if (hifzDark) Color(0xFF221F18) else QuranCard
private val HifzMuted: Color @Composable get() = if (hifzDark) Color(0xFFB9AE99) else QuranMuted
private val HifzGold: Color @Composable get() = if (hifzDark) Color(0xFFD9B878) else MushafGold
private val HifzAccent: Color @Composable get() = if (hifzDark) Color(0xFF8FD1AE) else QuranGreen

private data class GradeStyle(val grade: HifzGrade, val title: String, val description: String, val color: Color)
private val gradeStyles = listOf(
    GradeStyle(HifzGrade.FORGOT, "Forgot", "Couldn't recall it", MistakeRed),
    GradeStyle(HifzGrade.HARD, "Hard", "Recalled with effort", Color(0xFFB8741A)),
    GradeStyle(HifzGrade.GOOD, "Good", "Small pauses only", Color(0xFF2E7D57)),
    GradeStyle(HifzGrade.PERFECT, "Perfect", "Fluent, no mistakes", QuranGreen)
)

private val queueSaver = listSaver<List<HifzQueueItem>, String>(
    save = { queue -> queue.map { "${it.reference}|${it.learnFirst}|${it.isRetry}" } },
    restore = { saved ->
        saved.mapNotNull { line ->
            val p = line.split('|')
            AyahReference.parseOrNull(p[0])?.let { HifzQueueItem(it, p.getOrNull(1) == "true", p.getOrNull(2) == "true") }
        }
    }
)

private fun totalAyat(surah: Int) = FullQuranCatalog.surahs[surah - 1].totalAyat
private fun surahName(surah: Int) = FullQuranCatalog.surahs[surah - 1].englishName
private fun wordsOf(text: String) = text.split(Regex("\\s+")).filter(String::isNotBlank)

@Composable
fun HifzCoachScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("hifz_coach", 0) }
    val store = remember { HifzRevisionPreferences(prefs) }

    // Where "Memorise new ayahs" continues from.
    var nextNew by remember {
        val s = prefs.getInt("last_surah", 1).coerceIn(1, 114)
        mutableStateOf(AyahReference.of(s, prefs.getInt("last_ayah", 1).coerceIn(1, totalAyat(s))))
    }
    var sessionLength by remember { mutableIntStateOf(prefs.getInt("session_length", 5)) }
    var revisionVersion by remember { mutableIntStateOf(0) }

    // Session state survives rotation.
    var page by rememberSaveable { mutableStateOf(CoachPage.HOME) }
    var kind by rememberSaveable { mutableStateOf(SessionKind.NEW) }
    var queue by rememberSaveable(stateSaver = queueSaver) { mutableStateOf(emptyList()) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var step by rememberSaveable { mutableStateOf(PracticeStep.LEARN) }
    var reads by rememberSaveable { mutableIntStateOf(0) }
    var revealed by rememberSaveable { mutableIntStateOf(0) }
    var grades by rememberSaveable { mutableStateOf(listOf<Int>()) }

    var chooseSurah by rememberSaveable { mutableStateOf(false) }
    var chooseAyahFor by remember { mutableStateOf<Int?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }
    val loaded = remember { mutableStateMapOf<Int, List<Ayah>>() }
    var loadError by remember { mutableStateOf(false) }

    var listening by remember { mutableStateOf(false) }
    var checkingVoice by remember { mutableStateOf(false) }
    var speechError by remember { mutableStateOf<String?>(null) }
    var transcript by rememberSaveable { mutableStateOf("") }
    var assessment by remember { mutableStateOf<HifzAssessment?>(null) }
    var activeSpeech by remember { mutableStateOf<AyahReference?>(null) }
    val recognizer = remember { if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null }

    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var audioState by remember { mutableStateOf(AudioState.IDLE) }
    var speed by remember { mutableFloatStateOf(PlaybackSpeed.sanitize(prefs.getFloat("audio_speed", PlaybackSpeed.NORMAL))) }
    fun changeSpeed(value: Float) {
        speed = value
        prefs.edit().putFloat("audio_speed", value).apply()
        if (audioState == AudioState.PLAYING) player?.applySpeed(value)
    }
    DisposableEffect(Unit) { onDispose { recognizer?.destroy(); player?.release() } }

    val item = if (page == CoachPage.PRACTICE) queue.getOrNull(index) else null
    val ref = item?.reference
    LaunchedEffect(ref?.surah?.value, retryKey) {
        val surah = ref?.surah?.value ?: return@LaunchedEffect
        if (loaded[surah] != null) return@LaunchedEffect
        loadError = false
        val text = runCatching { HifzQuranTextRepository.loadSurah(context, surah) }.getOrDefault(emptyList())
        if (text.isEmpty()) loadError = true else loaded[surah] = text
    }
    val current = ref?.let { loaded[it.surah.value]?.getOrNull(it.ayah.value - 1) }

    val cards = remember(revisionVersion) { store.cards() }
    val summary = remember(revisionVersion) { store.summary() }
    val weakCards = remember(cards) { cards.filter(SmartHifzRevisionPlanner::isWeak).sortedBy { it.averageAccuracy } }

    fun stopAudio() {
        player?.release(); player = null
        audioState = AudioState.IDLE
    }
    fun playAyah(target: AyahReference) {
        stopAudio()
        if (QuranAudioManager.state.value.isPlaying) runCatching { QuranAudioManager.pause() }
        audioState = AudioState.LOADING
        val mp = MediaPlayer()
        player = mp
        runCatching {
            mp.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            mp.setDataSource("https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3".format(target.surah.value, target.ayah.value))
            mp.setOnPreparedListener { if (player === mp) { audioState = AudioState.PLAYING; it.start(); it.applySpeed(speed) } }
            mp.setOnCompletionListener { if (player === mp) stopAudio() }
            mp.setOnErrorListener { _, _, _ -> if (player === mp) { stopAudio(); audioState = AudioState.ERROR }; true }
            mp.prepareAsync()
        }.onFailure { stopAudio(); audioState = AudioState.ERROR }
    }
    fun stopSpeech() {
        recognizer?.cancel()
        activeSpeech = null
        listening = false
        checkingVoice = false
    }
    fun resetItemState() {
        stopSpeech(); stopAudio()
        assessment = null; transcript = ""; speechError = null
        reads = 0; revealed = 0
    }
    fun showItem(i: Int, q: List<HifzQueueItem> = queue) {
        resetItemState()
        index = i
        step = if (q[i].learnFirst) PracticeStep.LEARN else PracticeStep.RECALL
    }
    fun begin(newKind: SessionKind, items: List<HifzQueueItem>) {
        if (items.isEmpty()) return
        kind = newKind
        queue = items
        grades = emptyList()
        page = CoachPage.PRACTICE
        showItem(0, items)
    }
    fun leave() { resetItemState(); page = CoachPage.HOME; revisionVersion++ }
    fun setNextNew(target: AyahReference) {
        nextNew = target
        prefs.edit().putInt("last_surah", target.surah.value).putInt("last_ayah", target.ayah.value).apply()
    }
    fun startNewPassage() = begin(SessionKind.NEW,
        HifzCoachLogic.newPassage(nextNew, sessionLength, ::totalAyat, cards.map { it.reference }.toSet()))
    fun grade(g: HifzGrade) {
        val cur = item ?: return
        store.record(cur.reference, g.score)
        revisionVersion++
        grades = grades + g.ordinal
        if (kind == SessionKind.NEW && !cur.isRetry) setNextNew(HifzCoachLogic.nextAyah(cur.reference, ::totalAyat))
        val updated = HifzCoachLogic.afterGrade(queue, index, g)
        queue = updated
        if (index + 1 < updated.size) showItem(index + 1, updated)
        else { resetItemState(); page = CoachPage.COMPLETE }
    }
    fun startListening() {
        val verse = current ?: return
        val target = ref ?: return
        if (recognizer == null) { speechError = "Voice check isn't available on this phone. Rate yourself instead."; return }
        stopAudio()
        activeSpeech = target
        speechError = null; assessment = null; transcript = ""
        checkingVoice = true
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { if (activeSpeech == target) { listening = true; checkingVoice = false } }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { if (activeSpeech == target) { listening = false; checkingVoice = true } }
            override fun onError(code: Int) {
                if (activeSpeech != target) return
                activeSpeech = null; listening = false; checkingVoice = false
                speechError = when (code) {
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Couldn't hear clearly. Tap the mic and try again."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Voice check needs internet. You can still rate yourself."
                    else -> "Voice check failed. You can still rate yourself."
                }
            }
            override fun onResults(results: Bundle?) {
                if (activeSpeech != target) return
                activeSpeech = null; listening = false; checkingVoice = false
                val heard = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (heard.isBlank()) { speechError = "No words recognised. Tap the mic and try again."; return }
                transcript = heard
                assessment = HifzAssessor.assess(verse.arabicText, heard)
                step = PracticeStep.CHECK
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        runCatching {
            recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            })
        }.onFailure { stopSpeech(); speechError = "Voice check could not start. You can still rate yourself." }
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && page == CoachPage.PRACTICE && step == PracticeStep.RECALL) startListening()
        else if (!granted) speechError = "Microphone permission is off. You can still rate yourself."
    }
    fun toggleMic() {
        when {
            listening -> { recognizer?.stopListening(); listening = false; checkingVoice = true }
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> startListening()
            else -> micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    BackHandler(enabled = page != CoachPage.HOME) { leave() }

    if (chooseSurah) SurahPickerDialog(
        onPick = { number -> chooseSurah = false; chooseAyahFor = number },
        onDismiss = { chooseSurah = false }
    )
    chooseAyahFor?.let { surah ->
        AyahPickerDialog(surah = surah, initial = if (surah == nextNew.surah.value) nextNew.ayah.value else 1,
            onPick = { ayah -> setNextNew(AyahReference.of(surah, ayah)); chooseAyahFor = null },
            onDismiss = { chooseAyahFor = null })
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(HifzPaper).testTag("hifz_coach"),
        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (page) {
            CoachPage.HOME -> {
                item {
                    Text("Hifz Coach", color = HifzAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Memorise a little every day, revise what you know.", color = HifzMuted, fontSize = 14.sp)
                }
                if (cards.isNotEmpty()) item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile("${summary.streakDays}", "day streak", Modifier.weight(1f), Icons.Default.LocalFireDepartment)
                        StatTile("${summary.completedToday}", "practised today", Modifier.weight(1f))
                        StatTile("${cards.count(HifzCoachLogic::isStrong)}", "ayahs strong", Modifier.weight(1f))
                    }
                }
                if (cards.isNotEmpty()) item {
                    CoachCard {
                        SectionLabel("TODAY'S REVISION")
                        val due = summary.dueCards.size
                        if (due > 0) {
                            Text("$due ayah${if (due == 1) "" else "s"} to revise", color = HifzAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            val batch = minOf(due, HifzCoachLogic.MAX_REVISION)
                            Text("About $batch min · revising keeps your hifz safe", color = HifzMuted, fontSize = 13.sp)
                            if (summary.totalToday > 0) LinearProgressIndicator(progress = { summary.progress }, modifier = Modifier.fillMaxWidth().clip(CircleShape),
                                color = HifzAccent, trackColor = QuranGoldLight.copy(alpha = .4f))
                            PrimaryButton(if (due > batch) "Revise next $batch" else "Start revision", Modifier.testTag("hifz_start_due")) {
                                begin(SessionKind.REVISION, HifzCoachLogic.revisionQueue(summary.dueCards))
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = HifzAccent, modifier = Modifier.size(26.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("All revised for today", color = HifzAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            val next = cards.minOfOrNull { it.schedule.nextReviewEpochMs }
                            if (next != null) {
                                val days = ((SmartHifzRevisionPlanner.dayKey(next) - SmartHifzRevisionPlanner.dayKey(System.currentTimeMillis())) / 86_400_000L).toInt()
                                Text("Next revision ${HifzCoachLogic.intervalLabel(days)}. Memorise something new below.", color = HifzMuted, fontSize = 13.sp)
                            }
                        }
                    }
                }
                item {
                    CoachCard {
                        SectionLabel("MEMORISE NEW AYAHS")
                        val s = FullQuranCatalog.surahs[nextNew.surah.value - 1]
                        val end = minOf(s.totalAyat, nextNew.ayah.value + sessionLength - 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(s.englishName, color = HifzAccent, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                                Text(if (end > nextNew.ayah.value) "Ayah ${nextNew.ayah.value}–$end of ${s.totalAyat}" else "Ayah ${nextNew.ayah.value} of ${s.totalAyat}",
                                    color = HifzMuted, fontSize = 13.sp)
                            }
                            Text(s.arabicName, color = HifzAccent, fontFamily = IndoPakFont, fontSize = 24.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { chooseSurah = true }, modifier = Modifier.weight(1f)) { Text("Change surah", maxLines = 1) }
                            OutlinedButton(onClick = { chooseAyahFor = nextNew.surah.value }, modifier = Modifier.weight(1f)) { Text("Change ayah", maxLines = 1) }
                        }
                        Text("Ayahs per session", color = HifzMuted, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 3, 5, 10).forEach { length ->
                                FilterChip(selected = sessionLength == length, onClick = {
                                    sessionLength = length; prefs.edit().putInt("session_length", length).apply()
                                }, label = { Text("$length") })
                            }
                        }
                        PrimaryButton("Start memorising", Modifier.testTag("hifz_start_passage")) { startNewPassage() }
                    }
                }
                if (cards.isEmpty()) item { HowItWorksCard() }
                if (weakCards.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Needs attention", color = HifzAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Ayahs you found hard recently", color = HifzMuted, fontSize = 12.sp)
                            }
                            TextButton(onClick = {
                                begin(SessionKind.WEAK, weakCards.take(HifzCoachLogic.MAX_REVISION).map { HifzQueueItem(it.reference, learnFirst = true) })
                            }) { Text("Practise all", color = HifzGold, fontWeight = FontWeight.Bold) }
                        }
                    }
                    items(weakCards.take(5), key = { it.reference.toString() }) { card ->
                        Surface(onClick = { begin(SessionKind.WEAK, listOf(HifzQueueItem(card.reference, learnFirst = true))) }, color = HifzCard,
                            shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MushafBorder.copy(alpha = .3f)), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("${surahName(card.reference.surah.value)} · Ayah ${card.reference.ayah.value}", color = HifzAccent, fontWeight = FontWeight.SemiBold)
                                    Text("Recall ${card.averageAccuracy}% · ${card.attempts} tr${if (card.attempts == 1) "y" else "ies"}", color = HifzMuted, fontSize = 12.sp)
                                }
                                Text("Practise", color = HifzGold, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                item {
                    TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://tanzil.net"))) }) {
                        Text("Arabic text: Tanzil Quran Text (Simple 1.1)", fontSize = 12.sp, color = HifzMuted)
                    }
                }
            }
            CoachPage.PRACTICE -> {
                val cur = item ?: return@LazyColumn
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = ::leave) { Icon(Icons.Default.Close, "End session", tint = HifzAccent) }
                        Column(Modifier.weight(1f)) {
                            Text("${surahName(cur.reference.surah.value)} · Ayah ${cur.reference.ayah.value}", color = HifzAccent, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("Ayah ${index + 1} of ${queue.size}", color = HifzMuted, fontSize = 12.sp)
                        }
                        val badge = when {
                            cur.isRetry -> "Once more"
                            cur.learnFirst && kind == SessionKind.NEW -> "New"
                            kind == SessionKind.WEAK -> "Practice"
                            else -> "Revision"
                        }
                        Surface(color = QuranGoldLight.copy(alpha = .45f), shape = CircleShape) {
                            Text(badge, color = HifzAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(progress = { if (queue.isEmpty()) 0f else index.toFloat() / queue.size },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = HifzAccent, trackColor = QuranGoldLight.copy(alpha = .35f))
                }
                item { StepIndicator(step, showRead = cur.learnFirst) }
                if (current == null) item {
                    CoachCard {
                        if (loadError) {
                            Text("Quran text could not open.", color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { retryKey++ }) { Text("Try again") }
                        } else Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp); Spacer(Modifier.width(10.dp))
                            Text("Opening ayah…", color = HifzMuted)
                        }
                    }
                } else when (step) {
                    PracticeStep.LEARN -> item {
                        CoachCard {
                            Text("Read it carefully, then read it aloud a few times.", color = HifzMuted, fontSize = 14.sp)
                            AyahText(buildAnnotatedString { append(current.arabicText) })
                            ListenButton(audioState, speed, ::changeSpeed, onPlay = { playAyah(cur.reference) }, onStop = ::stopAudio)
                            HorizontalDivider(color = MushafBorder.copy(alpha = .25f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Read aloud $TARGET_READS times", color = HifzAccent, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                                        repeat(TARGET_READS) { dot ->
                                            Box(Modifier.size(14.dp).clip(CircleShape).background(if (dot < reads) QuranGreen else QuranGoldLight.copy(alpha = .45f)))
                                        }
                                    }
                                }
                                OutlinedButton(onClick = { if (reads < TARGET_READS) reads++ }, enabled = reads < TARGET_READS,
                                    modifier = Modifier.testTag("hifz_count_read")) {
                                    Text(if (reads < TARGET_READS) "I read it" else "Done ✓")
                                }
                            }
                            PrimaryButton(if (reads >= TARGET_READS) "Hide it & test me" else "I know it — test me", Modifier.testTag("hifz_test_me")) {
                                stopAudio(); step = PracticeStep.RECALL
                            }
                        }
                    }
                    PracticeStep.RECALL -> item {
                        val words = wordsOf(current.arabicText)
                        CoachCard {
                            Text("Recite this ayah from memory", color = HifzAccent, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                            Box(Modifier.fillMaxWidth().heightIn(min = 150.dp).clip(RoundedCornerShape(14.dp))
                                .background(HifzPaper).border(1.dp, MushafBorder.copy(alpha = .3f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center) {
                                if (revealed == 0) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("• • •", color = HifzGold, fontSize = 26.sp)
                                    Text("Ayah is hidden", color = HifzMuted, fontSize = 13.sp)
                                } else Text(words.take(revealed).joinToString(" ") + if (revealed < words.size) " …" else "",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp), color = HifzAccent,
                                    fontFamily = IndoPakFont, fontSize = 27.sp, lineHeight = 44.sp, textAlign = TextAlign.Right)
                            }
                            if (recognizer != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilledIconButton(onClick = ::toggleMic, enabled = !checkingVoice, modifier = Modifier.size(56.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (listening) MistakeRed else QuranGreen)) {
                                        Icon(if (listening) Icons.Default.Stop else Icons.Default.Mic, localizedUiText(if (listening) "Finish" else "Check with microphone"), tint = Color.White)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(when {
                                        listening -> "Listening… recite now, tap to finish"
                                        checkingVoice -> "Checking your recitation…"
                                        else -> "Tap the mic to recite aloud and get checked (optional)"
                                    }, color = HifzMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                }
                            }
                            speechError?.let { e -> Text(e, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { if (revealed < words.size) revealed++ }, enabled = revealed < words.size,
                                    modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Lightbulb, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                                    Text(if (revealed == 0) "Hint" else "Next word")
                                }
                                Button(onClick = { stopSpeech(); step = PracticeStep.CHECK }, modifier = Modifier.weight(1.4f).testTag("hifz_reveal"),
                                    colors = ButtonDefaults.buttonColors(containerColor = QuranGreen)) {
                                    Icon(Icons.Default.Visibility, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                                    Text("Show ayah")
                                }
                            }
                        }
                    }
                    PracticeStep.CHECK -> {
                        val words = wordsOf(current.arabicText)
                        val a = assessment
                        item {
                            CoachCard {
                                Text("Compare with what you recited", color = HifzMuted, fontSize = 14.sp)
                                AyahText(if (a != null) buildAnnotatedString {
                                    a.words.forEachIndexed { i, word ->
                                        if (i > 0) append(' ')
                                        val wrong = i in a.incorrectIndices
                                        withStyle(SpanStyle(color = if (wrong) MistakeRed else HifzAccent, fontWeight = if (wrong) FontWeight.Bold else FontWeight.Normal)) { append(word) }
                                    }
                                } else buildAnnotatedString { append(current.arabicText) })
                                if (a != null) {
                                    Surface(color = QuranGoldLight.copy(alpha = .25f), shape = RoundedCornerShape(12.dp)) {
                                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                            Text("Voice match ${a.score}%", color = HifzAccent, fontWeight = FontWeight.Bold)
                                            Text(if (a.incorrectIndices.isEmpty()) "Every word matched. MashaAllah!"
                                                else "Words in red may have been missed. Speech recognition isn't perfect, so trust your own judgement.",
                                                color = HifzMuted, fontSize = 12.sp)
                                        }
                                    }
                                }
                                if (revealed > 0) Text("You used ${revealed} hint word${if (revealed == 1) "" else "s"}.", color = HifzGold, fontSize = 13.sp)
                                Row {
                                    ListenButton(audioState, speed, ::changeSpeed, onPlay = { playAyah(cur.reference) }, onStop = ::stopAudio)
                                    Spacer(Modifier.weight(1f))
                                    TextButton(onClick = { resetItemState(); step = PracticeStep.RECALL }) { Text("Try again", color = HifzAccent) }
                                }
                            }
                        }
                        item {
                            Text("How well did you remember it?", color = HifzAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("This decides when the ayah comes back for revision.", color = HifzMuted, fontSize = 13.sp)
                            Spacer(Modifier.height(10.dp))
                            val suggested = HifzCoachLogic.suggestedGrade(a?.score, revealed, words.size)
                            val existing = cards.firstOrNull { c -> c.reference == cur.reference } ?: SmartHifzCard(cur.reference)
                            val now = System.currentTimeMillis()
                            gradeStyles.chunked(2).forEach { pair ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    pair.forEach { style ->
                                        val next = HifzSpacedRepetitionScheduler.calculateNextReview(existing.schedule,
                                            SmartHifzRevisionPlanner.confidenceFor(style.grade.score), now)
                                        val whenText = if (style.grade == HifzGrade.FORGOT && !cur.isRetry) "Again in this session"
                                            else "Next: ${HifzCoachLogic.intervalLabel(next.intervalDays)}"
                                        GradeButton(style, whenText, style.grade == suggested, Modifier.weight(1f)) { grade(style.grade) }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
            CoachPage.COMPLETE -> {
                item {
                    CoachCard {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(52.dp).align(Alignment.CenterHorizontally), tint = HifzAccent)
                        Text("Session complete", color = HifzAccent, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                        val reviewed = queue.map { q -> q.reference }.distinct().size
                        Text("MashaAllah! You practised $reviewed ayah${if (reviewed == 1) "" else "s"}.", color = HifzMuted,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            gradeStyles.forEach { style ->
                                val count = grades.count { g -> g == style.grade.ordinal }
                                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(style.color.copy(alpha = .1f)).padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$count", color = style.color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(localizedUiText(style.title), color = HifzMuted, fontSize = 11.sp)
                                }
                            }
                        }
                        Text("Each ayah will return for revision at the right time, so it stays in your memory.",
                            color = HifzMuted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        val due = summary.dueCards.size
                        when {
                            due > 0 -> PrimaryButton("Revise $due due ayah${if (due == 1) "" else "s"}") {
                                begin(SessionKind.REVISION, HifzCoachLogic.revisionQueue(summary.dueCards))
                            }
                            kind == SessionKind.NEW -> PrimaryButton("Memorise next $sessionLength") { startNewPassage() }
                        }
                        OutlinedButton(onClick = ::leave, modifier = Modifier.fillMaxWidth()) { Text("Back to Coach", color = HifzAccent) }
                    }
                }
            }
        }
    }
}

private enum class AudioState { IDLE, LOADING, PLAYING, ERROR }

@Composable
private fun ListenButton(state: AudioState, speed: Float, onSpeedChange: (Float) -> Unit, onPlay: () -> Unit, onStop: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = if (state == AudioState.IDLE || state == AudioState.ERROR) onPlay else onStop) {
            if (state == AudioState.LOADING) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = HifzAccent)
            else Icon(if (state == AudioState.PLAYING) Icons.Default.Stop else Icons.Default.Headphones, null, tint = HifzAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(when (state) { AudioState.PLAYING -> "Stop"; AudioState.LOADING -> "Loading…"; else -> "Listen" }, color = HifzAccent)
        }
        // The error hint takes the speed pill's place so the row still fits beside "Try again".
        if (state == AudioState.ERROR) Text("Needs internet", color = HifzMuted, fontSize = 12.sp)
        else PlaybackSpeedButton(speed, onSpeedChange, color = HifzAccent)
    }
}

@Composable
private fun AyahText(text: AnnotatedString) {
    Text(text, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = HifzAccent,
        fontFamily = IndoPakFont, fontSize = 29.sp, lineHeight = 50.sp, textAlign = TextAlign.Right)
}

@Composable
private fun StepIndicator(step: PracticeStep, showRead: Boolean) {
    val steps = buildList {
        if (showRead) add(PracticeStep.LEARN to "Read")
        add(PracticeStep.RECALL to "Recite"); add(PracticeStep.CHECK to "Rate")
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { i, (s, label) ->
            val active = s == step
            val done = s.ordinal < step.ordinal
            Row(Modifier.weight(1f).clip(CircleShape).background(when {
                active -> QuranGreen; done -> QuranGreen.copy(alpha = .15f); else -> QuranGoldLight.copy(alpha = .3f)
            }).padding(vertical = 7.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(if (done) "✓ $label" else "${i + 1}  $label", color = if (active) Color.White else HifzAccent,
                    fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun GradeButton(style: GradeStyle, whenText: String, suggested: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.testTag("hifz_grade_${style.grade.name.lowercase()}"),
        color = if (suggested) style.color.copy(alpha = .1f) else HifzCard, shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (suggested) 2.dp else 1.dp, if (suggested) style.color else MushafBorder.copy(alpha = .45f))) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(style.color))
                Spacer(Modifier.width(8.dp))
                Text(localizedUiText(style.title), color = HifzAccent, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                if (suggested) Text(localizedUiText("Suggested"), color = style.color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(localizedUiText(style.description), color = HifzMuted, fontSize = 12.sp)
            Text(whenText, color = HifzGold, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HowItWorksCard() {
    CoachCard {
        SectionLabel("HOW IT WORKS")
        listOf(
            "Read" to "Read the ayah aloud a few times. Tap Listen to hear it.",
            "Recite" to "The ayah hides. Recite it from memory — use a hint if stuck.",
            "Rate" to "Say how well you remembered. The Coach brings it back for revision at the right time."
        ).forEachIndexed { i, (title, body) ->
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(26.dp).clip(CircleShape).background(QuranGreen), contentAlignment = Alignment.Center) {
                    Text("${i + 1}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, color = HifzAccent, fontWeight = FontWeight.Bold)
                    Text(body, color = HifzMuted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Surface(color = HifzCard, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MushafBorder.copy(alpha = .3f)), modifier = modifier) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) Icon(icon, null, tint = Color(0xFFD9822B), modifier = Modifier.size(18.dp))
                Text(value, color = HifzAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text(label, color = HifzMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SectionLabel(text: String) = Text(text, color = HifzGold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

@Composable
private fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = 50.dp), shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = QuranGreen)) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun SurahPickerDialog(onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a surah") },
        text = {
            Column {
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) }, label = { Text("Name or number") }, singleLine = true)
                val q = query.trim()
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(FullQuranCatalog.surahs.filter { q.isEmpty() || it.englishName.contains(q, true) || it.arabicName.contains(q) || it.number.toString() == q }, key = { it.number }) { s ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(role = Role.Button) { onPick(s.number) }.padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("${s.number}", color = HifzGold, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                            Column(Modifier.weight(1f)) {
                                Text(s.englishName, color = HifzAccent, fontWeight = FontWeight.SemiBold)
                                Text("${s.totalAyat} ayahs", color = HifzMuted, fontSize = 12.sp)
                            }
                            Text(s.arabicName, color = HifzAccent, fontFamily = IndoPakFont, fontSize = 18.sp)
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun AyahPickerDialog(surah: Int, initial: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val total = totalAyat(surah)
    var input by rememberSaveable { mutableStateOf(initial.toString()) }
    val value = input.toIntOrNull()?.takeIf { it in 1..total }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${surahName(surah)}: start at which ayah?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(input, { input = it.filter(Char::isDigit).take(3) }, label = { Text("Ayah 1 to $total") },
                    singleLine = true, isError = input.isNotEmpty() && value == null)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { input = "1" }, label = { Text("From start") })
                }
            }
        },
        confirmButton = { TextButton(onClick = { value?.let(onPick) }, enabled = value != null) { Text("Select") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CoachCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = HifzCard, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, MushafBorder.copy(alpha = .35f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

/** Uses the same keys as earlier Coach versions so existing revision history survives. */
private class HifzRevisionPreferences(private val prefs: android.content.SharedPreferences) {
    private val cardPrefix = "smart_hifz_card_"
    private val sessionsKey = "smart_hifz_sessions"

    fun cards(): List<SmartHifzCard> = prefs.all.mapNotNull { (key, value) ->
        if (!key.startsWith(cardPrefix)) null else decodeCard(key.removePrefix(cardPrefix), value as? String)
    }
    fun summary(now: Long = System.currentTimeMillis()) = SmartHifzRevisionPlanner.dailySummary(cards(), sessions(), now)
    fun record(reference: AyahReference, accuracy: Int, now: Long = System.currentTimeMillis()) {
        val existing = cards().firstOrNull { it.reference == reference } ?: SmartHifzCard(reference)
        val updated = SmartHifzRevisionPlanner.record(existing, accuracy, now)
        val recent = (sessions() + HifzRevisionSession(now, 1, accuracy.coerceIn(0, 100))).takeLast(365)
        prefs.edit().putString(cardPrefix + reference, encodeCard(updated))
            .putString(sessionsKey, recent.joinToString(";") { "${it.reviewedAtEpochMs},${it.completedTasks},${it.accuracy}" }).apply()
    }
    private fun encodeCard(card: SmartHifzCard): String = listOf(card.schedule.repetitions, card.schedule.easeFactor, card.schedule.intervalDays,
        card.schedule.lastReviewedEpochMs, card.schedule.nextReviewEpochMs, card.averageAccuracy, card.attempts).joinToString("|")
    private fun decodeCard(key: String, encoded: String?): SmartHifzCard? = runCatching {
        val ref = AyahReference.parseOrNull(key) ?: return null
        val p = encoded?.split("|") ?: return null
        if (p.size != 7) return null
        SmartHifzCard(ref, HifzCardState(ref, p[0].toInt(), p[1].toDouble(), p[2].toInt(), p[3].toLong(), p[4].toLong()), p[5].toInt(), p[6].toInt())
    }.getOrNull()
    private fun sessions(): List<HifzRevisionSession> = prefs.getString(sessionsKey, "").orEmpty().split(';').mapNotNull { item ->
        val p = item.split(','); if (p.size == 3) runCatching { HifzRevisionSession(p[0].toLong(), p[1].toInt(), p[2].toInt()) }.getOrNull() else null
    }
}
