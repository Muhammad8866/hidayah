package com.example.ui.screens
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.heading
import com.example.ui.components.HeroSurface
import com.example.ui.components.islamicPattern
import com.example.ui.components.pressScale
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.border
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel
import com.example.quran.FullQuranCatalog
import com.example.quran.QuranAudioManager
import com.example.quran.QuranAudioState
import com.example.ui.components.PlaybackSpeedButton
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

internal val MushafPaper = Color(0xFFFFF9E8)
internal val MushafInk = Color(0xFF17120B)
internal val MushafGold = Color(0xFF9A7435)
internal val MushafBorder = Color(0xFFB99A62)
internal val IndoPakFont = com.example.ui.theme.ArabicFontFamily

@Composable
fun QuranScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {}
) {
    com.example.core.analytics.AnalyticsScreen("quran_screen")

    var section by rememberSaveable { mutableIntStateOf(0) }
    // Hoisted so the tab header can step aside while a book is open and give the page all the room.
    var openBookId by rememberSaveable { mutableStateOf<String?>(null) }
    val reading = section == 0 && openBookId != null
    val view = LocalView.current

    // System bars are hidden only while the Mushaf is read full screen.
    DisposableEffect(isImmersive, section) {
        val window = (view.context as? android.app.Activity)?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, view) }
        val fullScreen = isImmersive && section == 0
        controller?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (fullScreen) hide(WindowInsetsCompat.Type.systemBars()) else show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }
    LaunchedEffect(section) { if (section != 0) onImmersiveChange(false) }


    Column(modifier.fillMaxSize().background(MushafPaper).testTag("quran_screen")) {
        if (!reading) QuranHeader(section) { section = it }
        when (section) {
            0 -> QuranPdfLibrary(
                modifier = Modifier.weight(1f),
                openId = openBookId,
                onOpenIdChange = { openBookId = it },
                isImmersive = isImmersive,
                onImmersiveChange = onImmersiveChange
            )
            1 -> QuranListenLibrary(Modifier.weight(1f))
            2 -> HifzCoachScreen(Modifier.weight(1f))
            else -> QuranWordLearningScreen(Modifier.weight(1f))
        }
    }
}

internal val QuranGreen = Color(0xFF173F31)
internal val QuranGoldLight = Color(0xFFE4C982)
internal val QuranCard = Color(0xFFFFFDF6)
internal val QuranMuted = Color(0xFF6B6150)

/** Title band for the Quran reader, audio, memorisation, and word-learning modes. */
@Composable
private fun QuranHeader(section: Int, select: (Int) -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.verticalGradient(listOf(com.example.ui.theme.HeroGradientTop, com.example.ui.theme.HeroGradientBottom)))
            // The same faint geometric lattice as every hero surface in the app.
            .islamicPattern(Color.White.copy(alpha = 0.045f))
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LocalizedText("Al-Quran", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() })
                // The subtitle cross-fades as the mode changes instead of jumping.
                AnimatedContent(targetState = section, transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) }, label = "quranSubtitle") { shown ->
                    LocalizedText(
                        when (shown) { 0 -> "Read the Mushaf of your choice"; 1 -> "Listen to Quran recitation and tafseer"; 2 -> "Practice from memory"; else -> "Listen word by word and learn meanings" },
                        color = QuranGoldLight, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("القرآن الكريم", color = QuranGoldLight, fontFamily = IndoPakFont, fontSize = 24.sp)
        }
        Spacer(Modifier.height(14.dp))
        Surface(
            color = Color.Black.copy(alpha = .18f), shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color.White.copy(alpha = .08f))
        ) {
            Row(Modifier.padding(4.dp)) {
                QuranSegment("Read", Icons.AutoMirrored.Filled.MenuBook, section == 0, Modifier.weight(1f).testTag("quran_tab_read")) { select(0) }
                QuranSegment("Listen", Icons.Default.Headphones, section == 1, Modifier.weight(1f).testTag("quran_tab_audio")) { select(1) }
                QuranSegment("Coach", Icons.Default.Mic, section == 2, Modifier.weight(1f).testTag("quran_tab_coach")) { select(2) }
                QuranSegment("Words", Icons.AutoMirrored.Filled.MenuBook, section == 3, Modifier.weight(1f).testTag("quran_tab_words")) { select(3) }
            }
        }
    }
}

@Composable
private fun QuranSegment(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    // The gold pill glides between segments rather than snapping.
    val container by animateColorAsState(if (selected) QuranGoldLight else Color.Transparent, tween(220), label = "segmentContainer")
    val content by animateColorAsState(if (selected) QuranGreen else Color.White.copy(alpha = .88f), tween(220), label = "segmentContent")
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = com.example.core.analytics.AppAnalytics.trackItemAction("quran_screen", "select_quran_tab", label, onClick),
        modifier = modifier.pressScale(interaction).semantics { this.selected = selected },
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content,
        interactionSource = interaction
    ) {
        Row(
            Modifier.heightIn(min = 40.dp).padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            LocalizedText(label, Modifier.weight(1f, fill = false), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun QuranReader(
    uiState: MainUiState,
    loadPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {}
) {
    com.example.core.analytics.AnalyticsScreen("quran_reader")
    com.example.core.analytics.AnalyticsReaderSession("mushaf_reader", uiState.quranPageNumber)

    var drag by remember { mutableFloatStateOf(0f) }
    var showPagePicker by rememberSaveable { mutableStateOf(false) }
    var showSurahPicker by rememberSaveable { mutableStateOf(false) }
    var showParaPicker by rememberSaveable { mutableStateOf(false) }
    fun turn(offset: Int) {
        if (!uiState.isQuranLoading) loadPage(uiState.quranPageNumber + offset)
    }

    Column(modifier.fillMaxSize().background(MushafPaper)) {
        if (!isImmersive) {
            MushafToolbar(
                uiState.quranPageNumber, { turn(-1) }, { turn(1) },
                { showPagePicker = true }, { showSurahPicker = true }, { showParaPicker = true },
                isImmersive, onImmersiveChange
            )
        }
        Box(
            Modifier.fillMaxSize()
                .padding(
                    horizontal = if (isImmersive) 0.dp else 8.dp,
                    vertical = if (isImmersive) 0.dp else 6.dp
                )
                .pointerInput(uiState.quranPageNumber, uiState.isQuranLoading) {
                    detectHorizontalDragGestures(
                        onDragStart = { drag = 0f },
                        onHorizontalDrag = { change, amount -> change.consume(); drag += amount },
                        onDragEnd = {
                            // Mushaf pages progress right-to-left: dragging the page from left to
                            // right reveals the next page, just like a physical Arabic mushaf.
                            if (drag >= 100f) turn(1)
                            if (drag <= -100f) turn(-1)
                            drag = 0f
                        },
                        onDragCancel = { drag = 0f }
                    )
                }
                // A single tap flips between the full-screen Mushaf and the normal chrome.
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { com.example.core.analytics.AppAnalytics.interaction("quran_screen", "toggle_full_screen_tap", "tap");
                    onImmersiveChange(!isImmersive)
                }
        ) {
            Surface(
                Modifier.fillMaxSize(), color = MushafPaper,
                shape = if (isImmersive) RoundedCornerShape(0.dp) else RoundedCornerShape(4.dp),
                border = if (isImmersive) null else BorderStroke(1.dp, MushafBorder)
            ) {
                when {
                    uiState.isQuranLoading && uiState.quranPageLines.isEmpty() -> MushafLoading()
                    uiState.quranPageLines.isEmpty() -> MushafLoadError(
                        uiState.quranLoadError,
                        retry = { loadPage(uiState.quranPendingPage ?: uiState.quranPageNumber) }
                    )
                    else -> MushafPage(uiState, isImmersive)
                }
            }
            if (uiState.isQuranLoading && uiState.quranPageLines.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).testTag("quran_page_loading"),
                    color = MushafGold,
                    trackColor = MushafPaper
                )
            }
            if (uiState.quranLoadError != null && uiState.quranPageLines.isNotEmpty()) {
                Snackbar(
                    modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                    action = {
                        TextButton({ com.example.core.analytics.AppAnalytics.interaction("quran_screen", "retry_load_page", "tap"); loadPage(uiState.quranPendingPage ?: uiState.quranPageNumber) }) {
                            LocalizedText("Retry")
                        }
                    }
                ) { LocalizedText(uiState.quranLoadError) }
            }
            if (isImmersive) {
                ImmersivePageControls(
                    label = "صفحہ ${arabicDigits(uiState.quranPageNumber)}",
                    hasPrevious = uiState.quranPageNumber > 1,
                    hasNext = uiState.quranPageNumber < 847,
                    previous = { turn(-1) },
                    next = { turn(1) },
                    exit = { onImmersiveChange(false) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
    if (showPagePicker) PagePickerDialog(uiState.quranPageNumber, { showPagePicker = false }) {
        showPagePicker = false
        loadPage(it)
    }
    if (showSurahPicker) SurahPickerDialog(uiState, { showSurahPicker = false }) {
        showSurahPicker = false; loadPage(it)
    }
    if (showParaPicker) ParaPickerDialog({ showParaPicker = false }) {
        showParaPicker = false; loadPage(it)
    }
}

/** Minimal page turner kept visible while the Mushaf fills the whole screen. */
@Composable
internal fun ImmersivePageControls(
    label: String, hasPrevious: Boolean, hasNext: Boolean,
    previous: () -> Unit, next: () -> Unit, exit: () -> Unit, modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier.padding(bottom = 6.dp).testTag("quran_immersive_controls"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xD9173F31), shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, QuranGoldLight.copy(alpha = .25f)), shadowElevation = 6.dp
            ) {
                Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(previous, enabled = hasPrevious, modifier = Modifier.testTag("quran_previous_page")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, localizedUiText("Previous page"), tint = Color.White)
                    }
                    LocalizedText(label, color = Color(0xFFE4C982), fontSize = 12.sp)
                    IconButton(next, enabled = hasNext, modifier = Modifier.testTag("quran_next_page")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, localizedUiText("Next page"), tint = Color.White)
                    }
                    IconButton(exit, modifier = Modifier.testTag("quran_exit_full_screen")) {
                        Icon(Icons.Default.FullscreenExit, localizedUiText("Exit full screen"), tint = Color.White)
                    }
                }
            }
        }
    }
}

internal val juzSurahRanges = listOf(
    1..2, 2..2, 2..3, 3..4, 4..4, 4..5, 5..6, 6..7, 7..8, 8..9,
    9..11, 11..12, 12..14, 15..16, 17..18, 18..20, 20..22, 23..25, 25..27, 27..29,
    29..33, 33..36, 36..39, 39..41, 41..45, 46..51, 51..57, 58..66, 67..77, 78..114
)

@Composable
internal fun QuranAudioLibrary(modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("quran_audio_library")

    val context = LocalContext.current
    // Shared engine: playback keeps running in the background service after leaving this screen.
    LaunchedEffect(Unit) { QuranAudioManager.init(context) }
    val state by QuranAudioManager.state.collectAsState()
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("quran_screen", query)
    val language = LocalAppLanguage.current
    val surahs = remember(query, language) {
        val q = query.trim()
        if (q.isEmpty()) FullQuranCatalog.surahs
        else FullQuranCatalog.surahs.filter {
            it.number.toString() == q || it.englishName.contains(q, true) ||
                it.englishMeaning.contains(q, true) || it.arabicName.contains(q) ||
                localizeUiText(it.englishName, language).contains(q, true) ||
                localizeUiText(it.englishMeaning, language).contains(q, true)
        }
    }
    Column(modifier.background(MushafPaper)) {
        state.error?.let { message ->
            Snackbar(Modifier.padding(8.dp), action = { TextButton(QuranAudioManager::clearError) { LocalizedText("OK") } }) { LocalizedText(message) }
        }
        LazyColumn(Modifier.weight(1f).testTag("quran_audio_list"), contentPadding = PaddingValues(bottom = 16.dp)) {
            val resumeSurah = state.resumeSurah
            if (state.playingSurah == null && resumeSurah != null) item { ResumeCard(resumeSurah, state.resumePositionMs) }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(mode == 0, { mode = 0 }, label = { LocalizedText("Surah wise") }, colors = quranChipColors())
                    Spacer(Modifier.width(8.dp))
                    FilterChip(mode == 1, { mode = 1 }, label = { LocalizedText("Para wise") }, colors = quranChipColors())
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.DownloadDone, null, tint = QuranMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    LocalizedText("${state.downloaded.size}/114", color = QuranMuted, fontSize = 12.sp)
                }
            }
            if (mode == 0) {
                item {
                    QuranSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        onClear = { com.example.core.analytics.AppAnalytics.interaction("quran_screen", "clear_audio_search", "tap"); query = "" },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).testTag("quran_audio_search")
                    ) { LocalizedText("Search surah by name or number") }
                }
                if (surahs.isEmpty()) item {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, null, tint = MushafGold.copy(alpha = .7f), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        LocalizedText("No surah found", color = QuranMuted, textAlign = TextAlign.Center)
                    }
                }
                items(surahs, key = { it.number }) { surah -> AudioSurahRow(surah, state) }
            } else {
                items(juzSurahRanges.indices.toList(), key = { "para$it" }) { index -> AudioParaRow(index + 1, juzSurahRanges[index].toList(), state) }
            }
        }
        if (state.playingSurah != null) NowPlayingBar(state)
    }
}

@Composable
internal fun quranChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = QuranGreen, selectedLabelColor = Color.White,
    containerColor = QuranCard, labelColor = QuranGreen
)

/** Offers to continue the last recitation from the exact second it was left at. */
@Composable
private fun ResumeCard(surah: Int, positionMs: Int) {
    HeroSurface(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(Color.White.copy(alpha = .12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.History, null, tint = QuranGoldLight, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Continue listening", fontSize = 12.sp, color = QuranGoldLight, fontWeight = FontWeight.SemiBold)
                LocalizedText("$surah. ${QuranAudioManager.surahName(surah)}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LocalizedText("From ${formatTime(positionMs)}", fontSize = 12.sp, color = Color.White.copy(alpha = .78f))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                FullQuranCatalog.surahs.getOrNull(surah - 1)?.arabicName.orEmpty(),
                color = QuranGoldLight, fontFamily = IndoPakFont, fontSize = 20.sp, maxLines = 1
            )
            Spacer(Modifier.width(10.dp))
            FilledIconButton(
                { QuranAudioManager.resumeLast() },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = QuranGoldLight, contentColor = QuranGreen),
                modifier = Modifier.size(48.dp)
            ) { Icon(Icons.Default.PlayArrow, localizedUiText("Resume")) }
        }
    }
}

/** Transport bar for whatever is currently playing, including a scrub bar. */
@Composable
private fun NowPlayingBar(state: QuranAudioState) {
    val surah = state.playingSurah ?: return
    Surface(
        color = QuranGreen, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), shadowElevation = 10.dp
    ) {
        Column(
            Modifier
                .background(Brush.verticalGradient(listOf(com.example.ui.theme.HeroGradientTop, com.example.ui.theme.HeroGradientBottom)))
                .islamicPattern(Color.White.copy(alpha = 0.04f))
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    LocalizedText("Now playing", color = QuranGoldLight, fontSize = 11.sp)
                    LocalizedText("$surah. ${QuranAudioManager.surahName(surah)}", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                PlaybackSpeedButton(state.speed, QuranAudioManager::setSpeed, color = QuranGoldLight)
                Spacer(Modifier.width(10.dp))
                Text(
                    FullQuranCatalog.surahs.getOrNull(surah - 1)?.arabicName.orEmpty(),
                    color = QuranGoldLight, fontFamily = IndoPakFont, fontSize = 20.sp
                )
            }
            Slider(
                value = state.positionMs.toFloat().coerceIn(0f, state.durationMs.coerceAtLeast(1).toFloat()),
                onValueChange = { QuranAudioManager.seekTo(it.toInt()) },
                valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
                colors = SliderDefaults.colors(thumbColor = QuranGoldLight, activeTrackColor = QuranGoldLight, inactiveTrackColor = Color.White.copy(alpha = .25f)),
                modifier = Modifier.fillMaxWidth().height(28.dp)
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LocalizedText(formatTime(state.positionMs), color = QuranGoldLight, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                IconButton(QuranAudioManager::stop) { Icon(Icons.Default.Stop, localizedUiText("Stop"), tint = Color.White) }
                IconButton(QuranAudioManager::previous) { Icon(Icons.Default.SkipPrevious, localizedUiText("Previous surah"), tint = Color.White) }
                FilledIconButton(
                    { QuranAudioManager.playOrPause(surah) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = QuranGoldLight, contentColor = QuranGreen),
                    modifier = Modifier.size(48.dp).testTag("quran_now_playing_toggle")
                ) {
                    // The stream is still being fetched: a spinner tells the listener the tap registered.
                    if (state.isBuffering && !state.isPlaying) CircularProgressIndicator(Modifier.size(22.dp), color = QuranGreen, strokeWidth = 2.dp)
                    else Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, localizedUiText(if (state.isPlaying) "Pause" else "Play"))
                }
                IconButton(QuranAudioManager::next) { Icon(Icons.Default.SkipNext, localizedUiText("Next surah"), tint = Color.White) }
                Spacer(Modifier.width(48.dp))
                Spacer(Modifier.weight(1f))
                LocalizedText(formatTime(state.durationMs), color = QuranGoldLight, fontSize = 11.sp)
            }
        }
    }
}

private fun formatTime(millis: Int): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    return "%d:%02d".format(java.util.Locale.ROOT, totalSeconds / 60, totalSeconds % 60)
}

/**
 * Gold-framed eight-pointed star (the Rub el Hizb mark printed in the mushaf margin) holding a surah
 * or para number. Filled green while that item is playing or open.
 */
@Composable
internal fun NumberBadge(number: Int, active: Boolean = false) {
    val fill by animateColorAsState(if (active) QuranGreen else Color(0xFFF6EBCB), tween(200), label = "badgeFill")
    val edge = MushafGold.copy(alpha = if (active) .9f else .65f)
    Box(
        Modifier.size(40.dp).drawWithCache {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 1.dp.toPx()
            fun square(startDegrees: Double) = Path().apply {
                for (i in 0 until 4) {
                    val angle = Math.toRadians(startDegrees + 90.0 * i)
                    val x = center.x + radius * cos(angle).toFloat()
                    val y = center.y + radius * sin(angle).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            val star = Path().apply { op(square(0.0), square(45.0), PathOperation.Union) }
            val outline = Stroke(width = 1.dp.toPx())
            onDrawBehind {
                drawPath(star, fill)
                drawPath(star, edge, style = outline)
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$number", color = if (active) Color.White else QuranGreen, fontWeight = FontWeight.Bold,
            fontSize = if (number >= 100) 11.sp else 13.sp, maxLines = 1
        )
    }
}

/** Pill search box on the fixed mushaf paper; light colours stay legible in dark theme too. */
@Composable
internal fun QuranSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = placeholder,
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClear) { Icon(Icons.Default.Close, localizedUiText("Clear")) }
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = QuranGreen, unfocusedBorderColor = MushafBorder.copy(alpha = .45f),
            focusedContainerColor = QuranCard, unfocusedContainerColor = QuranCard,
            // The field sits on fixed light paper, so dark-theme defaults would be unreadable.
            focusedTextColor = MushafInk, unfocusedTextColor = MushafInk, cursorColor = QuranGreen,
            focusedPlaceholderColor = QuranMuted, unfocusedPlaceholderColor = QuranMuted,
            focusedLeadingIconColor = QuranGreen, unfocusedLeadingIconColor = QuranMuted,
            focusedTrailingIconColor = QuranMuted, unfocusedTrailingIconColor = QuranMuted
        )
    )
}

@Composable
private fun AudioSurahRow(surah: com.example.domain.model.Surah, state: QuranAudioState) {
    val number = surah.number
    val active = state.playingSurah == number
    val downloading = number in state.downloading
    val downloaded = number in state.downloaded
    val resumable = !active && state.resumeSurah == number && state.resumePositionMs > 5000
    val play = { if (resumable) QuranAudioManager.resumeLast() else QuranAudioManager.playOrPause(number) }
    val rowColor by animateColorAsState(if (active) QuranGoldLight.copy(alpha = .28f) else Color.Transparent, tween(220), label = "audioRow")
    Row(
        Modifier.fillMaxWidth()
            .background(rowColor)
            .clickable(role = Role.Button, onClick = com.example.core.analytics.AppAnalytics.trackItemAction("quran_screen", "play_surah", "surah_$number", play))
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
            .testTag("quran_audio_surah_$number"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberBadge(number, active)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            LocalizedText(surah.englishName, fontWeight = FontWeight.SemiBold, color = MushafInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LocalizedText(
                when {
                    downloading -> "Downloading ${state.progress[number] ?: 0}%"
                    resumable -> "Resume from ${formatTime(state.resumePositionMs)}"
                    else -> "${surah.englishMeaning} • ${surah.totalAyat} ayat"
                },
                fontSize = 12.sp, color = if (downloading || resumable) MushafGold else QuranMuted, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (downloading) {
                LinearProgressIndicator(
                    progress = { (state.progress[number] ?: 0) / 100f },
                    Modifier.fillMaxWidth().padding(top = 4.dp), color = QuranGreen, trackColor = QuranGoldLight.copy(alpha = .3f)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(surah.arabicName, fontFamily = IndoPakFont, fontSize = 18.sp, color = QuranGreen)
        AudioRowActions(active && state.isPlaying, downloading, downloaded, play,
            download = { QuranAudioManager.download(number) }, delete = { QuranAudioManager.delete(number) },
            buffering = active && state.isBuffering && !state.isPlaying,
            cancelDownload = { QuranAudioManager.cancelDownload(number) })
    }
    HorizontalDivider(Modifier.padding(start = 68.dp), color = MushafBorder.copy(alpha = .25f))
}

@Composable
private fun AudioParaRow(para: Int, chapters: List<Int>, state: QuranAudioState) {
    val offline = chapters.count { it in state.downloaded }
    val active = state.playingSurah in chapters
    Row(
        Modifier.fillMaxWidth()
            .clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("quran_screen", "play_para", "tap", item = "para_$para"); QuranAudioManager.playQueue(chapters) }
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberBadge(para, active)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            LocalizedText("Para $para", fontWeight = FontWeight.SemiBold, color = MushafInk)
            LocalizedText(
                // Recitation files are whole surahs, so a Para that starts or ends mid-surah plays those surahs in full.
                "${QuranAudioManager.surahName(chapters.first())} – ${QuranAudioManager.surahName(chapters.last())} (${localizedUiText("full surahs")})",
                fontSize = 12.sp, color = QuranMuted, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            LocalizedText(
                if (offline == chapters.size) "Available offline" else "$offline/${chapters.size} offline",
                fontSize = 11.sp, color = if (offline == chapters.size) Color(0xFF2E7D32) else QuranMuted
            )
        }
        FilledTonalIconButton(
            { QuranAudioManager.playQueue(chapters) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = QuranGreen, contentColor = Color.White)
        ) { Icon(Icons.Default.PlayArrow, localizedUiText("Play Para")) }
        if (offline < chapters.size) IconButton({ com.example.core.analytics.AppAnalytics.interaction("quran_screen", "download_para", "tap", item = "para_$para"); QuranAudioManager.downloadAll(chapters) }) {
            Icon(Icons.Default.Download, localizedUiText("Download Para"), tint = QuranGreen)
        } else Spacer(Modifier.width(48.dp))
    }
    HorizontalDivider(Modifier.padding(start = 68.dp), color = MushafBorder.copy(alpha = .25f))
}

@Composable
internal fun AudioRowActions(
    playing: Boolean, downloading: Boolean, downloaded: Boolean,
    play: () -> Unit, download: () -> Unit, delete: () -> Unit,
    removeMessage: String = "This recitation will still play online.",
    /** Stream is still being fetched for this row, so the play button shows a spinner. */
    buffering: Boolean = false,
    /** Tapping the download spinner stops the download when this is given. */
    cancelDownload: (() -> Unit)? = null
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    if (confirmDelete) AlertDialog(
        onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("quran_screen", "dismiss_remove_download", "dismiss"); confirmDelete = false },
        title = { LocalizedText("Remove download?") },
        text = { LocalizedText(removeMessage) },
        confirmButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("quran_screen", "confirm_remove_download", "tap"); delete(); confirmDelete = false }) { LocalizedText("Remove") } },
        dismissButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("quran_screen", "cancel_remove_download", "tap"); confirmDelete = false }) { LocalizedText("Cancel") } }
    )
    Spacer(Modifier.width(4.dp))
    FilledTonalIconButton(
        { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); play() },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (playing || buffering) QuranGreen else QuranGreen.copy(alpha = .1f),
            contentColor = if (playing || buffering) Color.White else QuranGreen
        )
    ) {
        if (buffering) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        else Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, localizedUiText(if (playing) "Pause" else "Play"))
    }
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        when {
            downloading && cancelDownload != null -> IconButton({ com.example.core.analytics.AppAnalytics.interaction("quran_screen", "cancel_download", "tap"); cancelDownload?.invoke() }) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(26.dp), color = QuranGreen, strokeWidth = 2.dp)
                    Icon(Icons.Default.Close, localizedUiText("Cancel"), tint = QuranGreen, modifier = Modifier.size(14.dp))
                }
            }
            downloading -> CircularProgressIndicator(Modifier.size(22.dp), color = QuranGreen, strokeWidth = 2.dp)
            downloaded -> IconButton({ com.example.core.analytics.AppAnalytics.interaction("quran_screen", "open_remove_download", "tap"); confirmDelete = true }) {
                Icon(Icons.Default.DownloadDone, localizedUiText("Delete offline audio"), tint = Color(0xFF2E7D32))
            }
            else -> IconButton(download) {
                Icon(Icons.Default.Download, localizedUiText("Download for offline"), tint = QuranMuted)
            }
        }
    }
}

@Composable
private fun MushafToolbar(
    page: Int, previous: () -> Unit, next: () -> Unit, pick: () -> Unit, surah: () -> Unit, para: () -> Unit,
    isImmersive: Boolean = false, onImmersiveChange: (Boolean) -> Unit = {}
) {
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(com.example.ui.theme.HeroGradientTop, com.example.ui.theme.HeroGradientBottom)))
            .islamicPattern(Color.White.copy(alpha = 0.04f))
    ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(previous, enabled = page > 1, modifier = Modifier.testTag("quran_previous_page")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, localizedUiText("Previous page"), tint = Color.White)
            }
            Surface(onClick = com.example.core.analytics.AppAnalytics.trackAction("quran_screen", "open_page_picker", pick), color = Color.Transparent) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = Color(0xFFE4C982), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LocalizedText("القرآن الكريم", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        LocalizedText("صفحہ ${arabicDigits(page)} / ٨٤٧", color = Color(0xFFE4C982), fontSize = 11.sp)
                    }
                }
            }
            IconButton(next, enabled = page < 847, modifier = Modifier.testTag("quran_next_page")) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, localizedUiText("Next page"), tint = Color.White)
            }
        }
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("quran_screen", "open_surah_picker", surah)) { LocalizedText("Surah ▾", color = Color.White, fontWeight = FontWeight.SemiBold) }
        TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("quran_screen", "open_para_picker", para)) { LocalizedText("Para ▾", color = Color.White, fontWeight = FontWeight.SemiBold) }
        TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("quran_screen", "toggle_full_screen", "tap"); onImmersiveChange(!isImmersive) }) {
            Icon(if (isImmersive) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            LocalizedText("Full screen", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
    }
}

@Composable
private fun MushafPage(state: MainUiState, isImmersive: Boolean = false) {
    // Every line is scaled to the space actually available, so one page always fills the screen
    // exactly without scrolling or clipping, on any device size.
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val lineCount = state.quranPageLines.size.coerceAtLeast(1)
        val headerHeight = if (isImmersive) 0.dp else 34.dp
        val immersiveControlsInset = if (isImmersive) 58.dp else 0.dp
        val lineHeight = ((maxHeight - headerHeight - immersiveControlsInset) / lineCount)
        Column(
            Modifier.fillMaxSize()
                .padding(
                    start = if (isImmersive) 6.dp else 10.dp,
                    end = if (isImmersive) 6.dp else 10.dp,
                    top = if (isImmersive) 2.dp else 6.dp,
                    bottom = if (isImmersive) immersiveControlsInset else 6.dp
                )
                .testTag("mushaf_page_${state.quranPageNumber}"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isImmersive) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LocalizedText("مصحف ہند و پاک • ۱۳ سطر", color = MushafGold, fontSize = 12.sp)
                    LocalizedText("صفحہ ${arabicDigits(state.quranPageNumber)}", color = MushafGold, fontSize = 12.sp)
                }
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MushafBorder)
            }
            Column(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                state.quranPageLines.forEach { line ->
                    when (line.type) {
                        "surah_name" -> SurahOrnament(
                            state.allSurahs.firstOrNull { it.number == line.surahNumber }?.arabicName.orEmpty().repairLegacyEncoding()
                        )
                        "basmallah" -> MushafTextLine("بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِیْمِ", true, lineHeight)
                        else -> MushafTextLine(line.text, line.isCentered, lineHeight)
                    }
                }
            }
            if (!isImmersive) LocalizedText(arabicDigits(state.quranPageNumber), color = MushafGold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SurahOrnament(name: String) {
    Surface(
        Modifier.fillMaxWidth().padding(vertical = 5.dp), color = Color(0xFFF3E4B9),
        border = BorderStroke(1.dp, MushafGold), shape = RoundedCornerShape(50)
    ) {
        LocalizedText("۞  سُورَةُ $name  ۞", Modifier.padding(vertical = 4.dp, horizontal = 12.dp), textAlign = TextAlign.Center, fontFamily = IndoPakFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MushafInk)
    }
}

/**
 * Arabic line that shrinks itself until it fits the page width on a single line, starting from the
 * height each line may occupy. Drawing is held back until the fitted size is found, so no oversized
 * frame is ever shown.
 */
@Composable
private fun MushafTextLine(text: String, centered: Boolean, lineHeight: Dp) {
    val start = (lineHeight.value * 0.62f).coerceIn(12f, 30f)
    var fontSize by remember(text, lineHeight) { mutableFloatStateOf(start) }
    var fitted by remember(text, lineHeight) { mutableStateOf(false) }
    LocalizedText(
        text,
        Modifier.fillMaxWidth().drawWithContent { if (fitted) drawContent() },
        color = MushafInk, maxLines = 1, softWrap = false,
        fontFamily = IndoPakFont, fontSize = fontSize.sp, lineHeight = (fontSize * 1.25f).sp,
        textAlign = if (centered) TextAlign.Center else TextAlign.Right,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize > 9f) fontSize -= 0.5f else fitted = true
        }
    )
}

@Composable
private fun MushafLoading() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = MushafGold); Spacer(Modifier.height(12.dp)); LocalizedText("مصحف کا صفحہ کھل رہا ہے…", color = MushafInk)
    }
}

@Composable
private fun MushafLoadError(message: String?, retry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        LocalizedText(
            message ?: "Quran page could not be verified and was not displayed.",
            textAlign = TextAlign.Center,
            color = MushafInk
        )
        Spacer(Modifier.height(12.dp)); Button(retry) { LocalizedText("Retry") }
    }
}

@Composable
private fun PagePickerDialog(current: Int, dismiss: () -> Unit, open: (Int) -> Unit) {
    var value by remember(current) { mutableStateOf(current.toString()) }
    val page = value.toIntOrNull()
    AlertDialog(
        onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("quran_screen", "dismiss_page_picker", dismiss), title = { LocalizedText("Open Mushaf page") },
        text = { OutlinedTextField(value, { value = it.filter(Char::isDigit).take(3) }, label = { LocalizedText("Page 1–847") }, singleLine = true, isError = page == null || page !in 1..847, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) },
        confirmButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("quran_screen", "go_to_page", "tap"); page?.takeIf { it in 1..847 }?.let(open) }, enabled = page in 1..847) { LocalizedText("Open") } },
        dismissButton = { TextButton(dismiss) { LocalizedText("Cancel") } }
    )
}

@Composable
private fun SurahPickerDialog(state: MainUiState, dismiss: () -> Unit, open: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("quran_screen", "dismiss_surah_picker", dismiss),
        title = { LocalizedText("Select Surah") },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(state.allSurahs, key = { it.number }) { surah ->
                    val page = state.quranSurahStartPages[surah.number]
                    Row(
                        Modifier.fillMaxWidth().clickable(role = Role.Button, enabled = page != null) { com.example.core.analytics.AppAnalytics.interaction("quran_screen", "pick_surah", "tap", item = "surah_${surah.number}"); page?.let(open) }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LocalizedText("${surah.number}", Modifier.width(36.dp), color = MaterialTheme.colorScheme.primary)
                        LocalizedText(surah.englishName, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        LocalizedText(surah.arabicName.repairLegacyEncoding(), fontFamily = IndoPakFont, fontSize = 18.sp)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                }
            }
        },
        confirmButton = { TextButton(dismiss) { LocalizedText("Close") } }
    )
}

private val paraStartPages = listOf(
    1, 30, 59, 87, 115, 143, 169, 199, 227, 255,
    282, 311, 339, 367, 395, 423, 451, 479, 507, 535,
    563, 591, 619, 647, 675, 703, 731, 759, 787, 817
)

@Composable
private fun ParaPickerDialog(dismiss: () -> Unit, open: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("quran_screen", "dismiss_para_picker", dismiss),
        title = { LocalizedText("Select Para (Juz)") },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(paraStartPages.indices.toList(), key = { it }) { index ->
                    Row(
                        Modifier.fillMaxWidth().clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("quran_screen", "pick_para", "tap", item = "para_${index + 1}"); open(paraStartPages[index]) }.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LocalizedText("Para ${index + 1}", fontWeight = FontWeight.SemiBold)
                        LocalizedText("Page ${paraStartPages[index]}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                }
            }
        },
        confirmButton = { TextButton(dismiss) { LocalizedText("Close") } }
    )
}

private fun arabicDigits(number: Int): String = number.toString().map { "٠١٢٣٤٥٦٧٨٩"[it - '0'] }.joinToString("")

/** Repairs Arabic catalogue strings that were previously saved as UTF-8 bytes decoded as Latin-1. */
private fun String.repairLegacyEncoding(): String = runCatching {
    if ('Ø' in this || 'Ù' in this) toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8) else this
}.getOrDefault(this)
