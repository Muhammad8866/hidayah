package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quran.FullQuranCatalog
import com.example.quran.PdfQuran
import com.example.quran.QuranPdf
import com.example.quran.QuranPdfCatalog
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText
import androidx.core.content.FileProvider
import android.content.Intent
import kotlinx.coroutines.launch

private val ReaderGreen = QuranGreen
private val ReaderGoldLight = QuranGoldLight
private val OfflineGreen = Color(0xFF2E7D32)

/** Read tab: every Quran as a list; tapping one opens it. */
@Composable
internal fun QuranPdfLibrary(
    modifier: Modifier = Modifier,
    openId: String? = null,
    onOpenIdChange: (String?) -> Unit = {},
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    remember { PdfQuran.init(context) }
    val book = openId?.let(QuranPdfCatalog::byId)
    if (book != null) {
        BackHandler { onImmersiveChange(false); onOpenIdChange(null) }
        QuranPdfBook(book, modifier, isImmersive, onImmersiveChange) { onImmersiveChange(false); onOpenIdChange(null) }
        return
    }

    val progress by PdfQuran.progress.collectAsState()
    val changes by PdfQuran.changes.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var confirmDelete by remember { mutableStateOf<QuranPdf?>(null) }
    val failedText = localizedUiText("Download failed. Check your internet connection.")
    // Re-read whenever the library is shown again, so it reflects the page just read.
    val lastBook = remember { PdfQuran.lastBook() }

    Box(modifier.fillMaxSize().background(MushafPaper)) {
        LazyColumn(
            Modifier.fillMaxSize().testTag("quran_pdf_list"),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (lastBook != null) item(key = "continue") {
                ContinueReadingCard(lastBook, PdfQuran.lastPage(lastBook) + 1) { onOpenIdChange(lastBook.id) }
            }
            item(key = "title") {
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.Bottom) {
                    LocalizedText("Choose a Mushaf", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MushafInk, modifier = Modifier.weight(1f))
                    LocalizedText("${QuranPdfCatalog.all.size} books", fontSize = 12.sp, color = QuranMuted)
                }
            }
            items(QuranPdfCatalog.all, key = { it.id }) { item ->
                key(changes) {
                    QuranPdfRow(
                        book = item,
                        offline = PdfQuran.isOffline(item),
                        downloaded = PdfQuran.isDownloaded(item),
                        progress = progress[item.id],
                        lastPage = PdfQuran.lastPage(item),
                        open = { onOpenIdChange(item.id) },
                        download = {
                            scope.launch {
                                PdfQuran.download(item).onFailure { error ->
                                    snackbar.showSnackbar(if (item.id == "kanzul_iman") error.message ?: failedText else failedText)
                                }
                            }
                        },
                        delete = { confirmDelete = item }
                    )
                }
            }
            item(key = "hint") {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, null, tint = QuranMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Download a Mushaf once to read it without internet.", fontSize = 12.sp, color = QuranMuted)
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
    confirmDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { LocalizedText("Remove download?") },
            text = { LocalizedText("${item.title} will still open online.") },
            confirmButton = { TextButton({ PdfQuran.delete(item); confirmDelete = null }) { LocalizedText("Remove") } },
            dismissButton = { TextButton({ confirmDelete = null }) { LocalizedText("Cancel") } }
        )
    }
}

/** The book last read, one tap away from the exact page it was left on. */
@Composable
private fun ContinueReadingCard(book: QuranPdf, page: Int, open: () -> Unit) {
    val surah = book.surahPages.indexOfLast { it <= page } + 1
    val para = book.paraPages.indexOfLast { it <= page } + 1
    Card(
        onClick = open,
        modifier = Modifier.fillMaxWidth().testTag("quran_continue_reading"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(com.example.ui.theme.HeroGradientTop, com.example.ui.theme.HeroGradientBottom)))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bookmark, null, tint = ReaderGoldLight, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Continue reading", color = ReaderGoldLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        LocalizedText(
                            if (surah > 0) "Surah ${FullQuranCatalog.surahs[surah - 1].englishName}" else book.title,
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1
                        )
                        LocalizedText(book.title, color = Color.White.copy(alpha = .75f), fontSize = 12.sp, maxLines = 1)
                    }
                    if (surah > 0) Text(FullQuranCatalog.surahs[surah - 1].arabicName, color = ReaderGoldLight, fontFamily = IndoPakFont, fontSize = 24.sp)
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { page / book.pageCount.toFloat() },
                    Modifier.fillMaxWidth().height(5.dp),
                    color = ReaderGoldLight, trackColor = Color.White.copy(alpha = .2f), strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText(
                        (if (para > 0) "Para $para • " else "") + "Page $page / ${book.pageCount}",
                        color = Color.White.copy(alpha = .85f), fontSize = 12.sp, modifier = Modifier.weight(1f)
                    )
                    Surface(color = ReaderGoldLight, shape = RoundedCornerShape(50)) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            LocalizedText("Continue", color = ReaderGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = ReaderGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranPdfRow(
    book: QuranPdf, offline: Boolean, downloaded: Boolean, progress: Float?, lastPage: Int,
    open: () -> Unit, download: () -> Unit, delete: () -> Unit
) {
    Card(
        onClick = open,
        modifier = Modifier.fillMaxWidth().testTag("quran_pdf_${book.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QuranCard),
        border = BorderStroke(1.dp, MushafGold.copy(alpha = .25f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(book)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(book.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MushafInk, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LocalizedText(book.subtitle, fontSize = 12.sp, color = QuranMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        if (offline) "Offline" else "Online",
                        if (offline) Icons.Default.CheckCircle else Icons.Default.Cloud,
                        if (offline) OfflineGreen else QuranMuted
                    )
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("${book.pageCount} pages • ${megabytes(book.sizeBytes)}", fontSize = 11.sp, color = QuranMuted, maxLines = 1)
                }
                if (lastPage > 0 && progress == null) {
                    Spacer(Modifier.height(4.dp))
                    LocalizedText("Last read: page ${lastPage + 1}", fontSize = 11.sp, color = MushafGold, fontWeight = FontWeight.SemiBold)
                }
                if (progress != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progress }, Modifier.weight(1f).height(5.dp),
                            color = ReaderGreen, trackColor = ReaderGoldLight.copy(alpha = .35f), strokeCap = StrokeCap.Round
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("${(progress * 100).toInt()}%", fontSize = 11.sp, color = ReaderGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            when {
                book.bundled -> Unit
                downloaded -> IconButton(delete, Modifier.testTag("quran_pdf_delete_${book.id}")) {
                    Icon(Icons.Default.Delete, localizedUiText("Remove download"), tint = QuranMuted)
                }
                progress != null -> Unit
                else -> FilledTonalIconButton(
                    download, Modifier.testTag("quran_pdf_download_${book.id}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = ReaderGreen.copy(alpha = .1f), contentColor = ReaderGreen)
                ) {
                    Icon(Icons.Default.Download, localizedUiText("Download"))
                }
            }
        }
    }
}

/** Small book spine showing the line count, so the editions are told apart at a glance. */
@Composable
private fun BookCover(book: QuranPdf) {
    val lines = Regex("(\\d+) Lines").find(book.title)?.groupValues?.get(1)
    Box(
        Modifier.size(width = 52.dp, height = 66.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF1F5A45), ReaderGreen)), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp))
            .border(1.dp, MushafGold.copy(alpha = .7f), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("القرآن", color = ReaderGoldLight, fontFamily = IndoPakFont, fontSize = 14.sp)
            if (lines != null) Text(lines, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            else Icon(Icons.Default.MenuBook, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun StatusPill(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Surface(color = tint.copy(alpha = .1f), shape = RoundedCornerShape(50)) {
        Row(Modifier.padding(horizontal = 7.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            LocalizedText(label, fontSize = 11.sp, color = tint, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun megabytes(bytes: Long) = "${(bytes + 524_288) / 1_048_576} MB"

/** Opens one book, fetching it first when it is only online. */
@Composable
private fun QuranPdfBook(
    book: QuranPdf, modifier: Modifier, isImmersive: Boolean,
    onImmersiveChange: (Boolean) -> Unit, back: () -> Unit
) {
    var document by remember(book.id) { mutableStateOf<PdfQuran.Document?>(null) }
    var failed by remember(book.id) { mutableStateOf(false) }
    var attempt by remember(book.id) { mutableIntStateOf(0) }
    val progress by PdfQuran.progress.collectAsState()
    LaunchedEffect(book.id, attempt) {
        failed = false
        PdfQuran.open(book).onSuccess { document = it }.onFailure { failed = true }
    }
    val loaded = document
    if (loaded != null) {
        PdfBookPager(book, loaded, modifier, isImmersive, onImmersiveChange, back)
        return
    }
    Column(modifier.fillMaxSize().background(MushafPaper)) {
        ReaderTopBar(book.title, back)
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val fetched = progress[book.id]
            when {
                failed -> {
                    LocalizedText(
                        if (PdfQuran.isOffline(book)) "Quran page could not be verified and was not displayed."
                        else "Could not load this Quran. Check your internet connection, or download it to read offline.",
                        textAlign = TextAlign.Center, color = MushafInk
                    )
                    Spacer(Modifier.height(12.dp))
                    Button({ attempt++ }, colors = ButtonDefaults.buttonColors(containerColor = ReaderGreen)) { LocalizedText("Retry") }
                }
                fetched != null -> {
                    LocalizedText("Loading online… ${(fetched * 100).toInt()}%", color = MushafInk)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { fetched }, Modifier.fillMaxWidth(), color = ReaderGreen)
                }
                else -> CircularProgressIndicator(color = MushafGold)
            }
        }
    }
}

@Composable
private fun ReaderTopBar(title: String, back: () -> Unit, content: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().background(ReaderGreen).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(back, Modifier.testTag("quran_pdf_back")) { Icon(Icons.Default.Close, localizedUiText("Close"), tint = Color.White) }
        if (title.isNotEmpty()) LocalizedText(title, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        content()
    }
}

/** Pages left to right in PDF order, one per screen; pinch or double-tap to zoom. */
@Composable
private fun PdfBookPager(
    book: QuranPdf, document: PdfQuran.Document, modifier: Modifier,
    isImmersive: Boolean, onImmersiveChange: (Boolean) -> Unit, back: () -> Unit
) {
    val count = document.pageCount
    val pagerState = rememberPagerState(initialPage = PdfQuran.lastPage(book).coerceIn(0, count - 1)) { count }
    val scope = rememberCoroutineScope()
    var zoomed by remember { mutableStateOf(false) }
    var showNavigator by rememberSaveable { mutableStateOf(false) }
    val lastIndex = count - 1
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { PdfQuran.saveLastPage(book, it) }
    }
    // The screen must not dim or lock in the middle of a page.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    fun turn(offset: Int) {
        scope.launch { pagerState.animateScrollToPage((pagerState.currentPage + offset).coerceIn(0, lastIndex)) }
    }
    val page = pagerState.currentPage + 1
    val para = book.paraPages.indexOfLast { it <= page } + 1
    val surah = book.surahPages.indexOfLast { it <= page } + 1
    val label = "$page / $count"
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var sharing by remember { mutableStateOf(false) }
    val shareFailed = localizedUiText("Could not share this Quran. Check your internet connection and try again.")
    // Pages read here count towards the Quran goal in Daily tools, scaled to Madani pages.
    val goalStore = remember(context) { quranGoalStore(context) }
    val khatmDone = localizedUiText("Khatm complete. MashaAllah!")
    LaunchedEffect(pagerState, book.id) {
        val pageWorth = madaniPagesPerPage(book.paraPages)
        snapshotFlow { pagerState.settledPage }.collect { index ->
            if (goalStore.recordReaderPage(book.id, index, book.paraPages.first() - 1, pageWorth)) snackbar.showSnackbar(khatmDone)
        }
    }

    Column(modifier.fillMaxSize().background(MushafPaper)) {
        if (!isImmersive) {
            ReaderTopBar("", back) {
                Surface(
                    onClick = { showNavigator = true }, color = Color.White.copy(alpha = .1f), shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(vertical = 6.dp).testTag("pdf_navigator")
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            LocalizedText(
                                if (surah > 0) FullQuranCatalog.surahs[surah - 1].englishName else book.title,
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1
                            )
                            LocalizedText(
                                if (para > 0) "Para $para • Page $label" else "Page $label",
                                color = ReaderGoldLight, fontSize = 11.sp, maxLines = 1
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowDown, localizedUiText("Go to surah, para or page"), tint = ReaderGoldLight)
                    }
                }
                IconButton(
                    onClick = {
                        if (!sharing) scope.launch {
                            sharing = true
                            PdfQuran.shareFile(book).onSuccess { file ->
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, book.title)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(send, context.getString(com.example.R.string.app_name)))
                            }.onFailure { snackbar.showSnackbar(shareFailed) }
                            sharing = false
                        }
                    },
                    enabled = !sharing,
                    modifier = Modifier.testTag("quran_pdf_share")
                ) {
                    if (sharing) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Share, localizedUiText("Share Quran PDF"), tint = Color.White)
                }
                IconButton({ onImmersiveChange(true) }) {
                    Icon(Icons.Default.Fullscreen, localizedUiText("Full screen"), tint = Color.White)
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag("pdf_pager"),
                beyondViewportPageCount = 1,
                // Mushaf order: the next page comes in from the left, as in an Arabic book.
                reverseLayout = true,
                userScrollEnabled = !zoomed,
                key = { it }
            ) { index ->
                ZoomablePdfPage(
                    document = document,
                    index = index,
                    active = index == pagerState.currentPage,
                    onZoomChange = { zoomed = it },
                    onTap = { onImmersiveChange(!isImmersive) }
                )
            }
            if (isImmersive) {
                ImmersivePageControls(
                    label = label,
                    hasPrevious = pagerState.currentPage > 0,
                    hasNext = pagerState.currentPage < lastIndex,
                    previous = { turn(-1) },
                    next = { turn(1) },
                    exit = { onImmersiveChange(false) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        if (!isImmersive) {
            ReaderBottomBar(
                page = page, count = count,
                hasPrevious = pagerState.currentPage > 0, hasNext = pagerState.currentPage < lastIndex,
                previous = { turn(-1) }, next = { turn(1) },
                jump = { target -> scope.launch { pagerState.scrollToPage((target - 1).coerceIn(0, lastIndex)) } }
            )
        }
    }
    SnackbarHost(snackbar, Modifier.fillMaxWidth())
    if (showNavigator) PdfNavigatorDialog(book, count, page, surah, para, { showNavigator = false }) { target ->
        showNavigator = false
        scope.launch { pagerState.scrollToPage((target - 1).coerceIn(0, lastIndex)) }
    }
}

/**
 * Page turner with a scrub bar. Everything runs right to left like the pager itself, so the next
 * page button and the slider's forward direction both point left.
 */
@Composable
private fun ReaderBottomBar(
    page: Int, count: Int, hasPrevious: Boolean, hasNext: Boolean,
    previous: () -> Unit, next: () -> Unit, jump: (Int) -> Unit
) {
    var dragging by remember { mutableStateOf<Float?>(null) }
    val shown = dragging?.roundToInt() ?: page
    Surface(color = ReaderGreen, modifier = Modifier.fillMaxWidth().testTag("pdf_bottom_bar")) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(previous, enabled = hasPrevious, modifier = Modifier.testTag("pdf_previous_page")) {
                    Icon(Icons.Default.ChevronRight, localizedUiText("Previous page"), tint = if (hasPrevious) Color.White else Color.White.copy(alpha = .3f))
                }
                if (count > 1) Slider(
                    value = dragging ?: page.toFloat(),
                    onValueChange = { dragging = it },
                    onValueChangeFinished = { dragging?.let { jump(it.roundToInt()) }; dragging = null },
                    valueRange = 1f..count.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = ReaderGoldLight, activeTrackColor = ReaderGoldLight, inactiveTrackColor = Color.White.copy(alpha = .25f)),
                    modifier = Modifier.weight(1f).testTag("pdf_page_slider")
                ) else Spacer(Modifier.weight(1f))
                Text(
                    "$shown",
                    color = ReaderGoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 40.dp)
                )
                IconButton(next, enabled = hasNext, modifier = Modifier.testTag("pdf_next_page")) {
                    Icon(Icons.Default.ChevronLeft, localizedUiText("Next page"), tint = if (hasNext) Color.White else Color.White.copy(alpha = .3f))
                }
            }
        }
    }
}

/**
 * One PDF page fitted to the screen. It is rendered at twice the screen width, and sharper again
 * once zoomed in, so text stays crisp. Zoom resets when the page is swiped away.
 */
@Composable
private fun ZoomablePdfPage(
    document: PdfQuran.Document, index: Int, active: Boolean,
    onZoomChange: (Boolean) -> Unit, onTap: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize().testTag("pdf_page_${index + 1}"), contentAlignment = Alignment.Center) {
        val boxWidth = constraints.maxWidth.toFloat()
        val boxHeight = constraints.maxHeight.toFloat()
        var scale by remember(index) { mutableFloatStateOf(1f) }
        var offset by remember(index) { mutableStateOf(Offset.Zero) }
        LaunchedEffect(active) { if (!active) { scale = 1f; offset = Offset.Zero } }
        LaunchedEffect(scale, active) { if (active) onZoomChange(scale > 1f) }

        val quality = if (scale > 2.5f) 4 else 2
        val renderWidth = (constraints.maxWidth * quality).coerceAtMost(4096)
        var bitmap by remember(index) { mutableStateOf(document.cached(index, (constraints.maxWidth * 2).coerceAtMost(4096))) }
        LaunchedEffect(index, renderWidth) {
            document.render(index, renderWidth)?.let { bitmap = it }
        }
        fun clamp(value: Offset, s: Float) = Offset(
            value.x.coerceIn(-(s - 1) * boxWidth / 2, (s - 1) * boxWidth / 2),
            value.y.coerceIn(-(s - 1) * boxHeight / 2, (s - 1) * boxHeight / 2)
        )

        val image = bitmap
        if (image == null) {
            CircularProgressIndicator(color = MushafGold)
        } else {
            val imageBitmap = remember(image) { image.asImageBitmap() }
            Image(
                imageBitmap,
                contentDescription = localizedUiText("Page ${index + 1}"),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.High,
                modifier = Modifier.fillMaxSize()
                    .pointerInput(index) {
                        detectTapGestures(
                            onTap = { onTap() },
                            onDoubleTap = { tap ->
                                if (scale > 1f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                    offset = clamp((Offset(boxWidth / 2, boxHeight / 2) - tap) * (scale - 1), scale)
                                }
                            }
                        )
                    }
                    .pointerInput(index) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                // One finger on an unzoomed page is left to the pager for page turns.
                                if (event.changes.size > 1 || scale > 1f) {
                                    val newScale = (scale * event.calculateZoom()).coerceIn(1f, 5f)
                                    offset = clamp(offset + event.calculatePan() * newScale, newScale)
                                    scale = newScale
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )
        }
    }
}

/** Jumps to a surah, a para or a page number; lists open at the one being read. */
@Composable
private fun PdfNavigatorDialog(
    book: QuranPdf, count: Int, currentPage: Int, currentSurah: Int, currentPara: Int,
    dismiss: () -> Unit, open: (Int) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val highlight = MaterialTheme.colorScheme.primary.copy(alpha = .1f)
    AlertDialog(
        onDismissRequest = dismiss,
        title = { LocalizedText(book.title) },
        text = {
            Column {
                TabRow(selectedTabIndex = tab) {
                    Tab(tab == 0, { tab = 0 }, Modifier.testTag("pdf_nav_surah"), text = { LocalizedText("Surah") })
                    Tab(tab == 1, { tab = 1 }, Modifier.testTag("pdf_nav_para"), text = { LocalizedText("Para") })
                    Tab(tab == 2, { tab = 2 }, Modifier.testTag("pdf_nav_page"), text = { LocalizedText("Page") })
                }
                when (tab) {
                    0 -> {
                        var query by rememberSaveable { mutableStateOf("") }
                        val language = LocalAppLanguage.current
                        val surahs = remember(query, language) {
                            val q = query.trim()
                            if (q.isEmpty()) FullQuranCatalog.surahs
                            else FullQuranCatalog.surahs.filter {
                                it.number.toString() == q || it.englishName.contains(q, true) || it.arabicName.contains(q) ||
                                    localizeUiText(it.englishName, language).contains(q, true)
                            }
                        }
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { LocalizedText("Search surah") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("pdf_surah_search")
                        )
                        val listState = rememberLazyListState((currentSurah - 3).coerceAtLeast(0))
                        LazyColumn(Modifier.heightIn(max = 380.dp).testTag("pdf_surah_list"), listState) {
                            items(surahs, key = { it.number }) { item ->
                                val page = book.surahPages[item.number - 1]
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(if (item.number == currentSurah) highlight else Color.Transparent)
                                        .clickable(role = Role.Button) { open(page) }
                                        .padding(vertical = 8.dp)
                                        .testTag("pdf_surah_${item.number}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NumberBadge(item.number, item.number == currentSurah)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        LocalizedText(item.englishName, fontWeight = FontWeight.SemiBold)
                                        LocalizedText(
                                            "Para ${book.paraPages.indexOfLast { it <= page } + 1} • Page $page",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                                        )
                                    }
                                    LocalizedText(item.arabicName, fontFamily = IndoPakFont, fontSize = 18.sp)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                            }
                        }
                    }
                    1 -> {
                        val listState = rememberLazyListState((currentPara - 3).coerceAtLeast(0))
                        LazyColumn(Modifier.heightIn(max = 420.dp).testTag("pdf_para_list"), listState) {
                            items((1..30).toList(), key = { it }) { para ->
                                val page = book.paraPages[para - 1]
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(if (para == currentPara) highlight else Color.Transparent)
                                        .clickable(role = Role.Button) { open(page) }
                                        .padding(vertical = 12.dp)
                                        .testTag("pdf_para_$para"),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LocalizedText("Para $para", fontWeight = FontWeight.SemiBold)
                                    LocalizedText("Page $page", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                            }
                        }
                    }
                    else -> {
                        var text by remember { mutableStateOf(currentPage.toString()) }
                        val target = text.toIntOrNull()?.takeIf { it in 1..count }
                        Column(Modifier.padding(top = 16.dp)) {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { value -> text = value.filter(Char::isDigit).take(4) },
                                label = { LocalizedText("Page (1 - $count)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("pdf_page_input")
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                { target?.let(open) }, enabled = target != null,
                                colors = ButtonDefaults.buttonColors(containerColor = ReaderGreen)
                            ) { LocalizedText("Open") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(dismiss) { LocalizedText("Close") } }
    )
}
