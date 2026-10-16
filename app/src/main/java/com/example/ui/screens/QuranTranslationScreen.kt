package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quran.FullQuranCatalog
import com.example.quran.QuranTranslationCatalog
import com.example.quran.QuranTranslationRepository
import com.example.quran.TranslatedAyah
import com.example.quran.TranslationAudioPlayer
import com.example.quran.TranslationPlayMode
import com.example.quran.TranslationReaderSettings
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

/** Entry card in the Mushaf library for reading the Quran with a translation of its meanings. */
@Composable
internal fun TranslationReaderCard(open: () -> Unit) {
    val context = LocalContext.current
    val (lastSurah, lastAyah) = remember { QuranTranslationRepository.lastRead(context) }
    Surface(
        onClick = com.example.core.analytics.AppAnalytics.trackItemAction("quran_pdf_library", "open_translation_reader", "translation", open),
        modifier = Modifier.fillMaxWidth().testTag("quran_translation_card"),
        color = QuranGreen, shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Translate, null, tint = QuranGoldLight, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Quran with translation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                LocalizedText("Read and listen to the meaning in many languages", color = QuranGoldLight, fontSize = 12.sp)
                if (lastSurah != 1 || lastAyah != 1) Text(
                    localizeUiTemplate("Continue: {surah} {ayah}", LocalAppLanguage.current,
                        mapOf("surah" to FullQuranCatalog.surahs[lastSurah - 1].englishName, "ayah" to "$lastSurah:$lastAyah")),
                    color = Color.White.copy(alpha = .8f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp)
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = QuranGoldLight)
        }
    }
}

private enum class Sheet { SURAH, TEXT, AUDIO, SETTINGS, BOOKMARKS }

@OptIn(FlowPreview::class)
@Composable
internal fun QuranTranslationScreen(modifier: Modifier = Modifier, close: () -> Unit) {
    com.example.core.analytics.AnalyticsScreen("quran_translation")
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val initial = remember { QuranTranslationRepository.lastRead(context) }
    var surahNumber by rememberSaveable { mutableIntStateOf(initial.first) }
    // Ayah to bring into view after the next load (resume, bookmark, or audio continuing).
    var pendingAyah by rememberSaveable { mutableIntStateOf(initial.second) }
    var translationId by rememberSaveable { mutableStateOf(QuranTranslationRepository.selectedId(context)) }
    var audioId by rememberSaveable { mutableStateOf(QuranTranslationRepository.selectedAudioId(context)) }
    var settings by remember { mutableStateOf(QuranTranslationRepository.settings(context)) }
    var bookmarks by remember { mutableStateOf(QuranTranslationRepository.bookmarks(context).toSet()) }
    var ayahs by remember { mutableStateOf<List<TranslatedAyah>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    var autoPlay by remember { mutableStateOf(false) }
    var sheet by rememberSaveable { mutableStateOf<Sheet?>(null) }
    val audioState by TranslationAudioPlayer.state.collectAsState()
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    val surah = FullQuranCatalog.surahs[surahNumber - 1]
    val translation = QuranTranslationCatalog.byId(translationId)
    val audio = QuranTranslationCatalog.audioById(audioId) ?: QuranTranslationCatalog.audio.first()
    val hasBismillah = surahNumber != 1 && surahNumber != 9
    val headerItems = if (hasBismillah) 2 else 1

    fun updateSettings(s: TranslationReaderSettings) { settings = s; QuranTranslationRepository.saveSettings(context, s) }
    fun play(index: Int) = TranslationAudioPlayer.play(context, ayahs, audio, index, settings.playMode, settings.speed, settings.repeatAyah)
    fun openSurah(number: Int, ayah: Int = 1, keepPlaying: Boolean = false) {
        if (!keepPlaying) TranslationAudioPlayer.stop()
        autoPlay = keepPlaying
        pendingAyah = ayah
        surahNumber = number
        sheet = null
    }
    fun leave() { TranslationAudioPlayer.stop(); close() }

    BackHandler { if (sheet != null) sheet = null else leave() }
    DisposableEffect(Unit) { onDispose { TranslationAudioPlayer.stop() } }

    LaunchedEffect(surahNumber, translationId, retry) {
        loading = true; failed = false
        ayahs = try {
            QuranTranslationRepository.loadSurah(surahNumber, surah.totalAyat, translationId)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { failed = true; emptyList() }
        loading = false
        if (ayahs.isEmpty()) return@LaunchedEffect
        val target = pendingAyah.coerceIn(1, ayahs.size)
        listState.scrollToItem(if (target == 1) 0 else headerItems + target - 1)
        if (autoPlay) { autoPlay = false; play(target - 1) }
        QuranTranslationRepository.prefetch(surahNumber + 1, translationId)
    }
    // Remember where the reader is, so the tab resumes there next time.
    LaunchedEffect(surahNumber, ayahs) {
        if (ayahs.isEmpty()) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged().debounce(400)
            .collect { first ->
                val ayah = (first - headerItems + 1).coerceIn(1, ayahs.size)
                pendingAyah = ayah
                QuranTranslationRepository.saveLastRead(context, surahNumber, ayah)
            }
    }
    // Keep the verse being spoken in view.
    LaunchedEffect(audioState.ayah) {
        val ayah = audioState.ayah ?: return@LaunchedEffect
        if (audioState.surah == surahNumber) listState.animateScrollToItem(headerItems + ayah - 1)
    }
    // Carry on into the next surah when the current one finishes.
    LaunchedEffect(audioState.finishedSurah) {
        val done = audioState.finishedSurah ?: return@LaunchedEffect
        TranslationAudioPlayer.consumeFinished()
        if (settings.continueNextSurah && done == surahNumber && done < 114) openSurah(done + 1, keepPlaying = true)
    }
    LaunchedEffect(audioState.error) {
        if (audioState.error) Toast.makeText(context, localizeUiText("Translation audio could not be played.", language), Toast.LENGTH_SHORT).show()
    }

    Column(modifier.fillMaxSize().background(MushafPaper).testTag("quran_translation_screen")) {
        TranslationHeader(
            surah = surah,
            translationLabel = "${localizedUiText(translation.language)} · ${translation.translator}",
            audioLabel = "${localizedUiText(audio.language)} · ${audio.voice}",
            back = ::leave,
            pickSurah = { sheet = Sheet.SURAH },
            pickText = { sheet = Sheet.TEXT },
            pickAudio = { sheet = Sheet.AUDIO },
            openBookmarks = { sheet = Sheet.BOOKMARKS },
            openSettings = { sheet = Sheet.SETTINGS }
        )
        Box(Modifier.weight(1f)) {
            when {
                loading && ayahs.isEmpty() -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MushafGold)
                    Spacer(Modifier.height(10.dp))
                    LocalizedText("Loading surah…", color = QuranMuted, fontSize = 13.sp)
                }
                failed -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    LocalizedText("Surah could not be loaded. Check your internet connection and try again.", textAlign = TextAlign.Center, color = MushafInk)
                    Spacer(Modifier.height(8.dp))
                    Button({ retry++ }, colors = ButtonDefaults.buttonColors(containerColor = QuranGreen)) { LocalizedText("Retry") }
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize().testTag("quran_translation_list"), state = listState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(key = "note") {
                        LocalizedText("Only the Arabic is the Quran; translations convey its meaning.", fontSize = 11.sp, color = QuranMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    if (hasBismillah) item(key = "bismillah") {
                        Text(
                            "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ", fontFamily = IndoPakFont, fontSize = 26.sp, color = QuranGreen,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        )
                    }
                    itemsIndexed(ayahs, key = { _, it -> it.globalNumber }) { index, ayah ->
                        val active = audioState.surah == ayah.surah && audioState.ayah == ayah.number
                        TranslatedAyahCard(
                            ayah, translation.language, settings, active,
                            playing = active && audioState.isPlaying,
                            bookmarked = (ayah.surah to ayah.number) in bookmarks,
                            play = { if (active) TranslationAudioPlayer.toggle() else play(index) },
                            bookmark = {
                                val added = QuranTranslationRepository.toggleBookmark(context, ayah.surah, ayah.number)
                                bookmarks = QuranTranslationRepository.bookmarks(context).toSet()
                                Toast.makeText(context, localizeUiText(if (added) "Bookmark added" else "Bookmark removed", language), Toast.LENGTH_SHORT).show()
                            },
                            copy = {
                                clipboard.setText(AnnotatedString(ayahShareText(ayah, surah.englishName, translation.translator, language)))
                                Toast.makeText(context, localizeUiText("Copied", language), Toast.LENGTH_SHORT).show()
                            },
                            share = {
                                val send = Intent(Intent.ACTION_SEND).setType("text/plain")
                                    .putExtra(Intent.EXTRA_TEXT, ayahShareText(ayah, surah.englishName, translation.translator, language))
                                context.startActivity(Intent.createChooser(send, null))
                            }
                        )
                    }
                    item(key = "nav") {
                        SurahNavigation(
                            previous = FullQuranCatalog.surahs.getOrNull(surahNumber - 2)?.englishName,
                            next = FullQuranCatalog.surahs.getOrNull(surahNumber)?.englishName,
                            goPrevious = { openSurah(surahNumber - 1) },
                            goNext = { openSurah(surahNumber + 1) }
                        )
                    }
                }
            }
            if (loading && ayahs.isNotEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter), color = MushafGold)
        }
        TranslationPlayerBar(
            surahActive = audioState.surah == surahNumber,
            playing = audioState.surah == surahNumber && audioState.isPlaying,
            buffering = audioState.isBuffering,
            ayah = audioState.ayah.takeIf { audioState.surah == surahNumber },
            total = surah.totalAyat,
            enabled = ayahs.isNotEmpty(),
            settings = settings,
            play = {
                if (audioState.surah == surahNumber) TranslationAudioPlayer.toggle()
                else play((pendingAyah - 1).coerceIn(0, (ayahs.size - 1).coerceAtLeast(0)))
            },
            previous = TranslationAudioPlayer::previous,
            next = TranslationAudioPlayer::next,
            stop = TranslationAudioPlayer::stop,
            toggleRepeat = {
                updateSettings(settings.copy(repeatAyah = !settings.repeatAyah))
                TranslationAudioPlayer.setRepeat(settings.repeatAyah)
            },
            cycleSpeed = {
                val speeds = TranslationReaderSettings.SPEEDS
                val nextSpeed = speeds[(speeds.indexOf(settings.speed) + 1) % speeds.size]
                updateSettings(settings.copy(speed = nextSpeed))
                TranslationAudioPlayer.setSpeed(nextSpeed)
            }
        )
    }

    when (sheet) {
        Sheet.SURAH -> SurahPickerSheet(surahNumber, { sheet = null }) { openSurah(it) }
        Sheet.TEXT -> ReaderSheet("Choose translation", { sheet = null }) {
            QuranTranslationCatalog.languages.forEach { lang ->
                item(key = "lang_$lang") { SectionLabel(lang) }
                items(QuranTranslationCatalog.text.filter { it.language == lang }, key = { it.id }) { t ->
                    PickerRow(t.translator, null, null, t.id == translationId) {
                        TranslationAudioPlayer.stop()
                        translationId = t.id; QuranTranslationRepository.saveSelectedId(context, t.id); sheet = null
                    }
                }
            }
        }
        Sheet.AUDIO -> ReaderSheet("Translation audio", { sheet = null }) {
            item(key = "audio_hint") {
                LocalizedText("Each ayah can be heard in Arabic, followed by its spoken translation.", fontSize = 12.sp, color = QuranMuted, modifier = Modifier.padding(bottom = 6.dp))
            }
            items(QuranTranslationCatalog.audio, key = { it.id }) { a ->
                PickerRow(a.voice, null, localizedUiText(a.language), a.id == audioId) {
                    TranslationAudioPlayer.stop()
                    audioId = a.id; QuranTranslationRepository.saveSelectedAudioId(context, a.id); sheet = null
                }
            }
        }
        Sheet.BOOKMARKS -> ReaderSheet("Bookmarks", { sheet = null }) {
            if (bookmarks.isEmpty()) item(key = "empty") {
                LocalizedText("No bookmarks yet. Tap the bookmark icon on any ayah to save it.", color = QuranMuted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 16.dp))
            }
            items(bookmarks.sortedWith(compareBy({ it.first }, { it.second })), key = { "${it.first}:${it.second}" }) { (s, a) ->
                val info = FullQuranCatalog.surahs[s - 1]
                PickerRow("${info.englishName} $s:$a", info.arabicName, null, s == surahNumber) { openSurah(s, a) }
            }
        }
        Sheet.SETTINGS -> ReaderSettingsSheet(settings, { sheet = null }) { s ->
            if (s.playMode != settings.playMode) TranslationAudioPlayer.stop()
            updateSettings(s)
        }
        null -> Unit
    }
}

private fun ayahShareText(ayah: TranslatedAyah, surahName: String, translator: String, language: String) =
    "${ayah.arabic}\n\n${ayah.translation}\n\n— " +
        localizeUiTemplate("Quran {surah}:{ayah}", language, mapOf("surah" to "${ayah.surah}", "ayah" to "${ayah.number}")) +
        " · $surahName ($translator)"

private fun speedLabel(speed: Float): String = speed.toString().removeSuffix(".0") + "×"

@Composable
private fun TranslationHeader(
    surah: com.example.domain.model.Surah,
    translationLabel: String,
    audioLabel: String,
    back: () -> Unit,
    pickSurah: () -> Unit,
    pickText: () -> Unit,
    pickAudio: () -> Unit,
    openBookmarks: () -> Unit,
    openSettings: () -> Unit
) {
    Surface(color = QuranGreen) {
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(QuranGreen, QuranGreen.copy(alpha = .92f))))
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, localizedUiText("Back"), tint = Color.White) }
                LocalizedText("Quran with translation", color = QuranGoldLight, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                IconButton(openBookmarks, Modifier.testTag("quran_translation_bookmarks")) {
                    Icon(Icons.Default.Bookmarks, localizedUiText("Bookmarks"), tint = Color.White)
                }
                IconButton(openSettings, Modifier.testTag("quran_translation_settings")) {
                    Icon(Icons.Default.TextFields, localizedUiText("Reading settings"), tint = Color.White)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = .08f)).clickable(onClick = pickSurah).padding(12.dp)
                    .testTag("quran_translation_pick_surah"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(QuranGoldLight), contentAlignment = Alignment.Center) {
                    Text("${surah.number}", color = QuranGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(surah.englishName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Icon(Icons.Default.KeyboardArrowDown, localizedUiText("Choose surah"), tint = QuranGoldLight)
                    }
                    Text(
                        "${localizedUiText(surah.englishMeaning)} · ${localizedUiText(surah.revelationType)} · " +
                            localizeUiTemplate("{count} ayahs", LocalAppLanguage.current, mapOf("count" to "${surah.totalAyat}")),
                        color = QuranGoldLight, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Text(surah.arabicName, color = QuranGoldLight, fontFamily = IndoPakFont, fontSize = 24.sp)
            }
            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderChip(Icons.AutoMirrored.Filled.MenuBook, translationLabel, pickText, Modifier.weight(1f).testTag("quran_translation_pick_text"))
                HeaderChip(Icons.Default.Headphones, audioLabel, pickAudio, Modifier.weight(1f).testTag("quran_translation_pick_audio"))
            }
        }
    }
}

@Composable
private fun HeaderChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = .1f), border = BorderStroke(1.dp, QuranGoldLight.copy(alpha = .4f))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = QuranGoldLight, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private val rtlLanguages = setOf("Urdu", "Sindhi", "Pashto", "Dhivehi", "Uyghur", "Kurdish")

@Composable
private fun TranslatedAyahCard(
    ayah: TranslatedAyah,
    language: String,
    settings: TranslationReaderSettings,
    active: Boolean,
    playing: Boolean,
    bookmarked: Boolean,
    play: () -> Unit,
    bookmark: () -> Unit,
    copy: () -> Unit,
    share: () -> Unit
) {
    Surface(
        color = if (active) QuranGoldLight.copy(alpha = .28f) else QuranCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (active) 1.5.dp else 1.dp, if (active) MushafGold else MushafBorder.copy(alpha = .35f)),
        modifier = Modifier.testTag("quran_translation_ayah_${ayah.number}")
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(30.dp).clip(CircleShape).background(if (active) MushafGold else QuranGreen.copy(alpha = .08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${ayah.number}", color = if (active) Color.White else QuranGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Text("  ${ayah.surah}:${ayah.number}", color = QuranMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                IconButton(bookmark, Modifier.size(36.dp)) {
                    Icon(
                        if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        localizedUiText(if (bookmarked) "Remove bookmark" else "Add bookmark"),
                        tint = if (bookmarked) MushafGold else QuranMuted, modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(copy, Modifier.size(36.dp)) { Icon(Icons.Default.ContentCopy, localizedUiText("Copy"), tint = QuranMuted, modifier = Modifier.size(18.dp)) }
                IconButton(share, Modifier.size(36.dp)) { Icon(Icons.Default.Share, localizedUiText("Share"), tint = QuranMuted, modifier = Modifier.size(18.dp)) }
                IconButton(play, Modifier.size(36.dp)) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        localizedUiText(if (playing) "Pause" else "Play translation audio"), tint = QuranGreen
                    )
                }
            }
            Column(Modifier.padding(end = 8.dp)) {
                if (settings.showArabic) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            ayah.arabic, fontFamily = IndoPakFont, fontSize = settings.arabicSize.sp, lineHeight = (settings.arabicSize * 1.85f).sp,
                            color = MushafInk, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), textAlign = TextAlign.Start
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MushafBorder.copy(alpha = .25f))
                }
                val rtl = language in rtlLanguages
                Text(
                    ayah.translation, fontSize = settings.translationSize.sp,
                    lineHeight = (settings.translationSize * if (rtl) 1.9f else 1.5f).sp, color = MushafInk,
                    modifier = Modifier.fillMaxWidth(),
                    style = LocalTextStyle.current.copy(textDirection = if (rtl) TextDirection.Rtl else TextDirection.Content)
                )
            }
        }
    }
}

@Composable
private fun SurahNavigation(previous: String?, next: String?, goPrevious: () -> Unit, goNext: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (previous != null) OutlinedButton(goPrevious, Modifier.weight(1f), border = BorderStroke(1.dp, MushafBorder)) {
            Icon(Icons.Default.ChevronLeft, null, tint = QuranGreen)
            Text(previous, color = QuranGreen, maxLines = 1, overflow = TextOverflow.Ellipsis)
        } else Spacer(Modifier.weight(1f))
        if (next != null) Button(goNext, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = QuranGreen)) {
            Text(next, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Default.ChevronRight, null)
        } else Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun TranslationPlayerBar(
    surahActive: Boolean,
    playing: Boolean,
    buffering: Boolean,
    ayah: Int?,
    total: Int,
    enabled: Boolean,
    settings: TranslationReaderSettings,
    play: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    stop: () -> Unit,
    toggleRepeat: () -> Unit,
    cycleSpeed: () -> Unit
) {
    Surface(color = QuranGreen, shadowElevation = 8.dp) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            if (ayah != null) LinearProgressIndicator(
                progress = { ayah / total.toFloat() }, modifier = Modifier.fillMaxWidth().height(3.dp),
                color = QuranGoldLight, trackColor = Color.White.copy(alpha = .15f)
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(start = 6.dp)) {
                    Text(
                        if (ayah != null) localizeUiTemplate("Ayah {ayah} of {total}", LocalAppLanguage.current, mapOf("ayah" to "$ayah", "total" to "$total"))
                        else localizedUiText("Translation audio"),
                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                    LocalizedText(
                        when (settings.playMode) {
                            TranslationPlayMode.ARABIC_AND_TRANSLATION -> "Arabic + translation"
                            TranslationPlayMode.TRANSLATION_ONLY -> "Translation only"
                            TranslationPlayMode.ARABIC_ONLY -> "Arabic only"
                        },
                        color = QuranGoldLight, fontSize = 11.sp
                    )
                }
                TextButton(cycleSpeed, contentPadding = PaddingValues(horizontal = 6.dp)) {
                    Text(speedLabel(settings.speed), color = QuranGoldLight, fontSize = 12.sp)
                }
                IconButton(toggleRepeat, Modifier.testTag("quran_translation_repeat")) {
                    Icon(Icons.Default.Repeat, localizedUiText("Repeat ayah"), tint = if (settings.repeatAyah) QuranGoldLight else Color.White.copy(alpha = .45f))
                }
                IconButton(previous, enabled = surahActive) { Icon(Icons.Default.SkipPrevious, localizedUiText("Previous ayah"), tint = Color.White) }
                Box(contentAlignment = Alignment.Center) {
                    FilledIconButton(
                        play, enabled = enabled, modifier = Modifier.size(46.dp).testTag("quran_translation_play"),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = QuranGoldLight, contentColor = QuranGreen)
                    ) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, localizedUiText(if (playing) "Pause" else "Play")) }
                    if (buffering) CircularProgressIndicator(Modifier.size(50.dp), color = Color.White, strokeWidth = 2.dp)
                }
                IconButton(next, enabled = surahActive) { Icon(Icons.Default.SkipNext, localizedUiText("Next ayah"), tint = Color.White) }
                if (surahActive) IconButton(stop) { Icon(Icons.Default.Stop, localizedUiText("Stop"), tint = QuranGoldLight) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSheet(title: String, dismiss: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    ModalBottomSheet(onDismissRequest = dismiss, containerColor = MushafPaper, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LocalizedText(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = QuranGreen, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 560.dp), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp), content = content)
    }
}

@Composable
private fun SurahPickerSheet(current: Int, dismiss: () -> Unit, choose: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val matches = FullQuranCatalog.surahs.filter {
        q.isEmpty() || it.number.toString() == q || it.englishName.contains(q, true) ||
            it.englishMeaning.contains(q, true) || it.arabicName.contains(q)
    }
    ReaderSheet("Choose surah", dismiss) {
        item(key = "search") {
            OutlinedTextField(
                query, { query = it }, Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("quran_translation_surah_search"),
                placeholder = { LocalizedText("Search by name or number") },
                leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(12.dp)
            )
        }
        items(matches, key = { it.number }) { s ->
            PickerRow(
                "${s.number}. ${s.englishName}", s.arabicName,
                "${localizedUiText(s.englishMeaning)} · ${localizeUiTemplate("{count} ayahs", LocalAppLanguage.current, mapOf("count" to "${s.totalAyat}"))}",
                s.number == current
            ) { choose(s.number) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsSheet(settings: TranslationReaderSettings, dismiss: () -> Unit, change: (TranslationReaderSettings) -> Unit) {
    ModalBottomSheet(onDismissRequest = dismiss, containerColor = MushafPaper) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            LocalizedText("Reading settings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = QuranGreen)
            Spacer(Modifier.height(12.dp))
            SettingSwitch("Show Arabic text", settings.showArabic) { change(settings.copy(showArabic = it)) }
            if (settings.showArabic) SizeSlider("Arabic text size", settings.arabicSize, TranslationReaderSettings.ARABIC_SIZES) { change(settings.copy(arabicSize = it)) }
            SizeSlider("Translation text size", settings.translationSize, TranslationReaderSettings.TRANSLATION_SIZES) { change(settings.copy(translationSize = it)) }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MushafBorder.copy(alpha = .3f))
            LocalizedText("Audio plays", fontWeight = FontWeight.SemiBold, color = MushafInk)
            listOf(
                TranslationPlayMode.ARABIC_AND_TRANSLATION to "Arabic + translation",
                TranslationPlayMode.TRANSLATION_ONLY to "Translation only",
                TranslationPlayMode.ARABIC_ONLY to "Arabic only"
            ).forEach { (mode, label) ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { change(settings.copy(playMode = mode)) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(settings.playMode == mode, { change(settings.copy(playMode = mode)) }, colors = RadioButtonDefaults.colors(selectedColor = QuranGreen))
                    LocalizedText(label, color = MushafInk)
                }
            }
            SettingSwitch("Continue to next surah", settings.continueNextSurah) { change(settings.copy(continueNextSurah = it)) }
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        LocalizedText(label, color = MushafInk, modifier = Modifier.weight(1f))
        Switch(checked, change, colors = SwitchDefaults.colors(checkedTrackColor = QuranGreen))
    }
}

@Composable
private fun SizeSlider(label: String, value: Int, range: IntRange, change: (Int) -> Unit) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row {
            LocalizedText(label, color = MushafInk, modifier = Modifier.weight(1f))
            Text("$value", color = MushafGold, fontWeight = FontWeight.Bold)
        }
        Slider(
            value.toFloat(), { change(it.toInt()) }, valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
            colors = SliderDefaults.colors(thumbColor = QuranGreen, activeTrackColor = QuranGreen)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    LocalizedText(text, fontWeight = FontWeight.Bold, color = MushafGold, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 2.dp))
}

@Composable
private fun PickerRow(title: String, trailing: String?, subtitle: String?, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(10.dp))
            .background(if (selected) QuranGoldLight.copy(alpha = .35f) else Color.Transparent)
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = MushafInk, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = QuranMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) Text(trailing, fontFamily = IndoPakFont, fontSize = 18.sp, color = MushafGold)
    }
}
