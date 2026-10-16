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
import androidx.compose.foundation.pager.VerticalPager
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import com.example.quran.QuranReaderPreferences
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
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.zIndex
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
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import com.example.ui.components.ErrorState
import com.example.ui.components.HeroSurface
import com.example.ui.components.IconBadge
import com.example.ui.components.LoadingState
import com.example.ui.components.islamicPattern
import com.example.ui.components.pressScale
import com.example.ui.components.rememberIsOnline
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip

private val ReaderGreen = QuranGreen
private val ReaderGoldLight = QuranGoldLight
private val OfflineGreen = Color(0xFF2E7D32)
private const val PAGE_TURN_MILLIS = 800

/** Library route for [QuranTranslationScreen]; never a [QuranPdf] id. */
internal const val TRANSLATION_READER_ID = "translation_reader"

/** Read tab: every Quran as a list; tapping one opens it. */
@Composable
internal fun QuranPdfLibrary(
    modifier: Modifier = Modifier,
    openId: String? = null,
    onOpenIdChange: (String?) -> Unit = {},
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {}
) {
    com.example.core.analytics.AnalyticsScreen("quran_pdf_library")

    val context = LocalContext.current
    remember { PdfQuran.init(context) }
    if (openId == TRANSLATION_READER_ID) {
        QuranTranslationScreen(modifier) { onImmersiveChange(false); onOpenIdChange(null) }
        return
    }
    val book = openId?.let(QuranPdfCatalog::byId)
    if (book != null) {
        BackHandler { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "reader_system_back", "tap"); onImmersiveChange(false); onOpenIdChange(null) }
        QuranPdfBook(book, modifier, isImmersive, onImmersiveChange) { onImmersiveChange(false); onOpenIdChange(null) }
        return
    }

    val progress by PdfQuran.progress.collectAsState()
    val changes by PdfQuran.changes.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var confirmDelete by remember { mutableStateOf<QuranPdf?>(null) }
    val failedText = localizedUiText("Download failed. Check your internet connection.")
    val isOnline by rememberIsOnline()
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
            item(key = "translation") { TranslationReaderCard { onOpenIdChange(TRANSLATION_READER_ID) } }
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
                                // Offline: say so at once instead of letting the fetch time out.
                                if (!isOnline && !PdfQuran.isOffline(item)) {
                                    snackbar.currentSnackbarData?.dismiss()
                                    snackbar.showSnackbar(failedText)
                                    return@launch
                                }
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
                    LocalizedText("Download a Mushaf once to read it without internet.", Modifier.weight(1f), fontSize = 12.sp, color = QuranMuted)
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
    confirmDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "dismiss_remove_download", "dismiss"); confirmDelete = null },
            title = { LocalizedText("Remove download?") },
            text = { LocalizedText("${item.title} will still open online.") },
            confirmButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "confirm_remove_download", "tap"); PdfQuran.delete(item); confirmDelete = null }) { LocalizedText("Remove") } },
            dismissButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "cancel_remove_download", "tap"); confirmDelete = null }) { LocalizedText("Cancel") } }
        )
    }
}

/** The book last read, one tap away from the exact page it was left on. */
@Composable
private fun ContinueReadingCard(book: QuranPdf, page: Int, open: () -> Unit) {
    val surah = book.surahPages.indexOfLast { it <= page } + 1
    val para = book.paraPages.indexOfLast { it <= page } + 1
    // Fraction read, eased in when the library opens so the bar visibly fills to where reading stopped.
    val target = (page / book.pageCount.toFloat()).coerceIn(0f, 1f)
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val fraction by animateFloatAsState(if (started) target else 0f, tween(700), label = "continueReading")
    HeroSurface(
        modifier = Modifier.fillMaxWidth().testTag("quran_continue_reading"),
        onClick = com.example.core.analytics.AppAnalytics.trackItemAction("quran_pdf_screens", "continue_reading", book.id, open),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bookmark, null, tint = ReaderGoldLight, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            LocalizedText("Continue reading", color = ReaderGoldLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    if (surah > 0) "Surah ${FullQuranCatalog.surahs[surah - 1].englishName}" else book.title,
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                LocalizedText(book.title, color = Color.White.copy(alpha = .75f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (surah > 0) {
                Spacer(Modifier.width(8.dp))
                Text(FullQuranCatalog.surahs[surah - 1].arabicName, color = ReaderGoldLight, fontFamily = IndoPakFont, fontSize = 28.sp, maxLines = 1)
            }
        }
        Spacer(Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { fraction },
            Modifier.fillMaxWidth().height(6.dp),
            color = ReaderGoldLight, trackColor = Color.White.copy(alpha = .18f), strokeCap = StrokeCap.Round,
            gapSize = 0.dp, drawStopIndicator = {}
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LocalizedText(
                (if (para > 0) "Para $para • " else "") + "Page $page / ${book.pageCount}",
                color = Color.White.copy(alpha = .85f), fontSize = 12.sp, modifier = Modifier.weight(1f)
            )
            Surface(color = ReaderGoldLight, shape = RoundedCornerShape(50)) {
                Row(Modifier.heightIn(min = 36.dp).padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText("Continue", color = ReaderGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = ReaderGreen, modifier = Modifier.size(16.dp))
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
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = com.example.core.analytics.AppAnalytics.trackItemAction("quran_pdf_screens", "open_quran_pdf", book.id, open),
        modifier = Modifier.fillMaxWidth().pressScale(interaction).testTag("quran_pdf_${book.id}"),
        shape = RoundedCornerShape(18.dp),
        color = QuranCard,
        border = BorderStroke(1.dp, MushafGold.copy(alpha = .22f)),
        interactionSource = interaction
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
                    LocalizedText("${book.pageCount} pages • ${megabytes(book.sizeBytes)}", fontSize = 11.sp, color = QuranMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            else Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = Color.White, modifier = Modifier.size(16.dp))
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
    LaunchedEffect(book.id) { com.example.core.analytics.AppAnalytics.content("quran_pdf", book.id) }
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
                // Offline the shared state explains itself and retries by itself once the connection is back.
                failed -> ErrorState(
                    onRetry = { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "retry_open_book", "tap"); attempt++ },
                    title = if (PdfQuran.isOffline(book)) "Quran page could not be verified and was not displayed."
                        else "Could not load this Quran. Check your internet connection, or download it to read offline.",
                    modifier = Modifier.testTag("quran_pdf_open_failed")
                )
                fetched != null -> {
                    LocalizedText(book.title, fontWeight = FontWeight.Bold, color = MushafInk, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    LocalizedText("Loading online… ${(fetched * 100).toInt()}%", color = MushafInk)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { fetched }, Modifier.fillMaxWidth(), color = ReaderGreen, trackColor = ReaderGoldLight.copy(alpha = .35f), strokeCap = StrokeCap.Round)
                    Spacer(Modifier.height(8.dp))
                    val sizeLabel = megabytes((fetched * book.sizeBytes).toLong()) + " / " + megabytes(book.sizeBytes)
                    Text(sizeLabel, fontSize = 12.sp, color = QuranMuted)
                }
                else -> LoadingState()
            }
        }
    }
}

@Composable
private fun ReaderTopBar(title: String, back: () -> Unit, content: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(com.example.ui.theme.HeroGradientTop, ReaderGreen)))
            .islamicPattern(Color.White.copy(alpha = 0.04f))
            .heightIn(min = 56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(back, Modifier.testTag("quran_pdf_back")) { Icon(Icons.Default.Close, localizedUiText("Close"), tint = Color.White) }
        if (title.isNotEmpty()) LocalizedText(title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
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
    com.example.core.analytics.AnalyticsReaderSession("quran_pdf", pagerState.currentPage)
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
    val page = pagerState.currentPage + 1
    val para = book.paraPages.indexOfLast { it <= page } + 1
    val surah = book.surahPages.indexOfLast { it <= page } + 1
    val label = "$page / $count"
    val context = LocalContext.current
    val choices = remember(context, book.id) { QuranReaderPreferences(context, book) }
    var bookmarks by remember(book.id) { mutableStateOf(choices.bookmarks()) }
    var readingMode by remember(book.id) { mutableStateOf(choices.readingMode()) }
    val scrollState = rememberLazyListState(pagerState.currentPage)
    LaunchedEffect(readingMode, scrollState) {
        if (readingMode == "scroll") snapshotFlow { scrollState.firstVisibleItemIndex }.collect { index ->
            pagerState.scrollToPage(index.coerceIn(0, lastIndex))
        }
    }
    fun goTo(index: Int) {
        scope.launch {
            val target = index.coerceIn(0, lastIndex)
            when {
                readingMode == "scroll" -> scrollState.scrollToItem(target)
                readingMode != "page" -> pagerState.animateScrollToPage(target)
                // Only a neighbouring page can be turned; further away the book just opens there.
                kotlin.math.abs(target - pagerState.currentPage) == 1 -> pagerState.animateScrollToPage(target, animationSpec = tween(PAGE_TURN_MILLIS))
                else -> pagerState.scrollToPage(target)
            }
        }
    }
    fun turn(offset: Int) = goTo(pagerState.currentPage + offset)
    LaunchedEffect(showNavigator) { if (!showNavigator) bookmarks = choices.bookmarks() }
    val snackbar = remember { SnackbarHostState() }
    var sharing by remember { mutableStateOf(false) }
    val shareFailed = localizedUiText("Could not share this Quran. Check your internet connection and try again.")
    fun sharePdf() {
        com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "share_pdf", "tap")
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
    }
    val shareAppText = localizedUiText("Read the Quran, duas and prayer times in one app:")
    fun shareApp() {
        com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "share_app", "tap")
        val appName = context.getString(com.example.R.string.app_name)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, appName)
            putExtra(Intent.EXTRA_TEXT, "$shareAppText\nhttps://play.google.com/store/apps/details?id=${context.packageName}")
        }
        context.startActivity(Intent.createChooser(send, appName))
    }
    val bookmarkAdded = localizedUiText("Bookmark added")
    val bookmarkRemoved = localizedUiText("Bookmark removed")
    fun toggleBookmark() {
        bookmarks = choices.toggleBookmark(page)
        val message = if (page in bookmarks) bookmarkAdded else bookmarkRemoved
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var navigatorTab by rememberSaveable { mutableIntStateOf(0) }
    // The guide opens by itself the first time a Quran is read.
    var showGuide by rememberSaveable { mutableStateOf(!choices.guideSeen()) }
    var volumeKeysMenu by remember { mutableStateOf(choices.volumeKeysMenu()) }
    DisposableEffect(volumeKeysMenu) {
        com.example.quran.ReaderKeys.volumeEnabled = volumeKeysMenu
        com.example.quran.ReaderKeys.onMenuKey = { showMenu = true }
        onDispose { com.example.quran.ReaderKeys.onMenuKey = null }
    }
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
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "open_navigator", "tap"); navigatorTab = 0; showNavigator = true }, color = Color.White.copy(alpha = .1f), shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(vertical = 6.dp).testTag("pdf_navigator")
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            LocalizedText(
                                if (surah > 0) FullQuranCatalog.surahs[surah - 1].englishName else book.title,
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            LocalizedText(
                                if (para > 0) "Para $para • Page $label" else "Page $label",
                                color = ReaderGoldLight, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowDown, localizedUiText("Go to surah, para or page"), tint = ReaderGoldLight)
                    }
                }
                IconButton(
                    onClick = { toggleBookmark() },
                    modifier = Modifier.testTag("pdf_bookmark_page")
                ) {
                    Icon(Icons.Default.Bookmark,
                        localizedUiText(if (page in bookmarks) "Remove bookmark" else "Bookmark page"),
                        tint = if (page in bookmarks) ReaderGoldLight else Color.White)
                }
                IconButton(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "open_reader_menu", "tap"); showMenu = true },
                    modifier = Modifier.testTag("pdf_reader_menu")
                ) {
                    if (sharing) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else Icon(Icons.Default.MoreVert, localizedUiText("Reader menu"), tint = Color.White)
                }
                IconButton({ com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "enter_full_screen", "tap"); onImmersiveChange(true) }) {
                    Icon(Icons.Default.Fullscreen, localizedUiText("Full screen"), tint = Color.White)
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            val pageContent: @Composable (Int) -> Unit = { index ->
                // Page turn: pages stay stacked like a book, earlier ones on top. The page being
                // left curls over from its outer corner and uncovers the one beneath.
                val turning = readingMode == "page"
                val side = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 1f else -1f
                fun turned() = ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                Box(Modifier.fillMaxSize()
                    .zIndex(if (turning) -index.toFloat() else 0f)
                    .graphicsLayer {
                        // A page already turned goes back off screen: left stacked on top it would
                        // take every touch meant for the page being read.
                        if (turning) turned().let { translationX = if (it >= 1f) 0f else -it * side * size.width }
                    }
                    .then(if (turning) Modifier.pageCurl(side, MushafPaper) { turned() } else Modifier)
                ) {
                    ZoomablePdfPage(
                        document = document,
                        index = index,
                        active = index == pagerState.currentPage,
                        onZoomChange = { zoomed = it },
                        onTap = { onImmersiveChange(!isImmersive) }
                    )
                }
            }
            if (readingMode == "scroll") {
                LazyColumn(state = scrollState, modifier = Modifier.fillMaxSize().testTag("pdf_pager"),
                    userScrollEnabled = !zoomed) {
                    items(count, key = { it }) { index ->
                        Box(Modifier.fillParentMaxSize()) { pageContent(index) }
                    }
                }
            } else if (readingMode == "vertical") {
                VerticalPager(
                    state = pagerState, modifier = Modifier.fillMaxSize().testTag("pdf_pager"),
                    beyondViewportPageCount = 1, userScrollEnabled = !zoomed, key = { it }
                ) { index -> pageContent(index) }
            } else {
                HorizontalPager(
                    state = pagerState, modifier = Modifier.fillMaxSize().testTag("pdf_pager"),
                    beyondViewportPageCount = 1, reverseLayout = true,
                    // A released page finishes turning unhurriedly, like a sheet of paper.
                    flingBehavior = if (readingMode == "page") PagerDefaults.flingBehavior(pagerState, snapAnimationSpec = tween(PAGE_TURN_MILLIS))
                        else PagerDefaults.flingBehavior(pagerState),
                    userScrollEnabled = !zoomed, key = { it }
                ) { index -> pageContent(index) }
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
                jump = { target -> goTo(target - 1) }
            )
        }
    }
    SnackbarHost(snackbar, Modifier.fillMaxWidth())
    if (showMenu) ReaderMenuDialog(
        bookmarked = page in bookmarks,
        volumeKeys = volumeKeysMenu,
        onVolumeKeysChange = { choices.saveVolumeKeysMenu(it); volumeKeysMenu = it },
        dismiss = { showMenu = false }
    ) { action ->
        showMenu = false
        when (action) {
            "bookmark" -> toggleBookmark()
            "bookmarks" -> { navigatorTab = 3; showNavigator = true }
            "page" -> { navigatorTab = 2; showNavigator = true }
            "mode" -> { navigatorTab = 5; showNavigator = true }
            "share_pdf" -> sharePdf()
            "share_app" -> shareApp()
            "guide" -> showGuide = true
        }
    }
    if (showGuide) ReaderGuideDialog { choices.markGuideSeen(); showGuide = false }
    if (showNavigator) PdfNavigatorDialog(book, count, page, surah, para, navigatorTab, readingMode,
        { mode -> scope.launch {
            if (mode == "scroll") scrollState.scrollToItem(pagerState.currentPage)
            choices.saveReadingMode(mode)
            readingMode = mode
        } }, { showNavigator = false }) { target ->
        showNavigator = false
        goTo(target - 1)
    }
}

/**
 * Draws a page as paper being turned: [turned] 0 is flat and 1 fully turned over the spine, which
 * is the right edge when [side] is positive. The outer bottom corner is lifted along a low arc;
 * everything on its side of the fold is cut away to show the page beneath, and comes back
 * mirrored across the fold as the pale underside of the sheet.
 */
private fun Modifier.pageCurl(side: Float, paper: Color, turned: () -> Float): Modifier = drawWithContent {
    val t = turned()
    if (t >= 1f) return@drawWithContent
    val w = size.width
    val h = size.height
    val spineX = if (side > 0f) w else 0f
    val outerX = w - spineX
    val cornerX = outerX + (spineX - outerX) * 2f * t
    val cornerY = h - h * .12f * kotlin.math.sin(Math.PI * t).toFloat()
    val length = kotlin.math.hypot(cornerX - outerX, cornerY - h)
    if (t <= 0f || length < 1f) {
        drawContent()
        return@drawWithContent
    }
    // The fold is the perpendicular bisector between where the corner was and where it is now.
    val nx = (cornerX - outerX) / length
    val ny = (cornerY - h) / length
    val mx = (outerX + cornerX) / 2f
    val my = (h + cornerY) / 2f
    val reach = (w + h) * 2f
    val lifted = Path().apply {
        moveTo(mx - ny * reach, my + nx * reach)
        lineTo(mx + ny * reach, my - nx * reach)
        lineTo(mx + ny * reach - nx * reach, my - nx * reach - ny * reach)
        lineTo(mx - ny * reach - nx * reach, my + nx * reach - ny * reach)
        close()
    }
    clipPath(lifted, ClipOp.Difference) { this@drawWithContent.drawContent() }
    // Shadow the fold throws on the page beneath.
    clipPath(lifted) {
        val depth = w * .1f
        drawRect(Brush.linearGradient(
            listOf(Color.Black.copy(alpha = .35f), Color.Transparent),
            start = Offset(mx, my), end = Offset(mx - nx * depth, my - ny * depth)
        ))
    }
    val shift = 2f * (mx * nx + my * ny)
    val mirror = Matrix().apply {
        this[0, 0] = 1f - 2f * nx * nx; this[0, 1] = -2f * nx * ny
        this[1, 0] = -2f * nx * ny; this[1, 1] = 1f - 2f * ny * ny
        this[3, 0] = shift * nx; this[3, 1] = shift * ny
    }
    withTransform({ transform(mirror); clipPath(lifted); clipRect(0f, 0f, w, h) }) {
        drawRect(paper)
        // The print shows faintly through the back of the sheet.
        this@drawWithContent.drawContent()
        drawRect(paper.copy(alpha = .9f))
        drawRect(Brush.linearGradient(
            0f to Color.Black.copy(alpha = .3f), .35f to Color.Transparent,
            .8f to Color.Transparent, 1f to Color.Black.copy(alpha = .18f),
            start = Offset(mx, my), end = Offset(mx - nx * length / 2f, my - ny * length / 2f)
        ))
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
            Row(
                Modifier
                    .background(Brush.verticalGradient(listOf(ReaderGreen, com.example.ui.theme.HeroGradientBottom)))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(previous, enabled = hasPrevious, modifier = Modifier.testTag("pdf_previous_page")) {
                    Icon(Icons.Default.ChevronRight, localizedUiText("Previous page"), tint = if (hasPrevious) Color.White else Color.White.copy(alpha = .3f))
                }
                if (count > 1) Slider(
                    value = dragging ?: page.toFloat(),
                    onValueChange = { dragging = it },
                    onValueChangeFinished = { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "page_slider", "change_complete"); dragging?.let { jump(it.roundToInt()) }; dragging = null },
                    valueRange = 1f..count.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = ReaderGoldLight, activeTrackColor = ReaderGoldLight, inactiveTrackColor = Color.White.copy(alpha = .25f)),
                    modifier = Modifier.weight(1f).testTag("pdf_page_slider")
                ) else Spacer(Modifier.weight(1f))
                // The page number sits in a pill so it reads as the scrubber's live value.
                Surface(color = Color.White.copy(alpha = .12f), shape = RoundedCornerShape(50)) {
                    Text(
                        "$shown",
                        color = ReaderGoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 44.dp).padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                IconButton(next, enabled = hasNext, modifier = Modifier.testTag("pdf_next_page")) {
                    Icon(Icons.Default.ChevronLeft, localizedUiText("Next page"), tint = if (hasNext) Color.White else Color.White.copy(alpha = .3f))
                }
            }
        }
    }
}

/**
 * One PDF page fitted to the screen. Show a screen-width preview first, then refine the active
 * page at higher resolution. Zoom resets when the page is swiped away.
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
        val previewWidth = constraints.maxWidth.coerceAtMost(4096)
        var bitmap by remember(document, index) {
            mutableStateOf(document.cached(index, renderWidth) ?: document.cached(index, previewWidth))
        }
        // A page streamed over HTTP ranges cannot render offline; the retry re-runs the effect.
        var renderFailed by remember(document, index) { mutableStateOf(false) }
        var renderAttempt by remember(document, index) { mutableIntStateOf(0) }
        LaunchedEffect(document, index, renderWidth, active, renderAttempt) {
            if (!active) {
                // Let the visible page take the renderer first, then warm its neighbours.
                delay(300)
                if (bitmap == null) document.render(index, previewWidth)?.let { bitmap = it }
                return@LaunchedEffect
            }
            if (bitmap == null) {
                val preview = document.render(index, previewWidth)
                if (preview != null) bitmap = preview else { renderFailed = true; return@LaunchedEffect }
            }
            renderFailed = false
            if (renderWidth > previewWidth) {
                document.render(index, renderWidth)?.let { bitmap = it }
            }
        }
        fun clamp(value: Offset, s: Float) = Offset(
            value.x.coerceIn(-(s - 1) * boxWidth / 2, (s - 1) * boxWidth / 2),
            value.y.coerceIn(-(s - 1) * boxHeight / 2, (s - 1) * boxHeight / 2)
        )

        val image = bitmap
        if (image == null) {
            if (renderFailed && active) ErrorState(
                onRetry = { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "retry_render_page", "tap"); renderAttempt++ },
                title = "Could not load this Quran. Check your internet connection, or download it to read offline.",
                modifier = Modifier.testTag("pdf_page_failed")
            ) else CircularProgressIndicator(color = MushafGold)
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

/** Reader actions in one place; also opened by the Menu and Volume keys. */
@Composable
private fun ReaderMenuDialog(
    bookmarked: Boolean, volumeKeys: Boolean, onVolumeKeysChange: (Boolean) -> Unit,
    dismiss: () -> Unit, action: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { LocalizedText("Reader menu") },
        text = {
            Column {
                listOf(
                    Triple("bookmark", if (bookmarked) "Remove bookmark" else "Bookmark page", Icons.Default.Bookmark),
                    Triple("bookmarks", "Bookmarks", Icons.Default.Star),
                    Triple("page", "Go to page", Icons.Default.Search),
                    Triple("mode", "Reading mode", Icons.AutoMirrored.Filled.MenuBook),
                    Triple("share_pdf", "Share Quran PDF", Icons.Default.Share),
                    Triple("share_app", "Share this app", Icons.Default.Share),
                    Triple("guide", "User guide", Icons.Default.Info)
                ).forEach { (id, title, icon) ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button) { action(id) }
                            .padding(horizontal = 4.dp, vertical = 6.dp).testTag("pdf_menu_$id"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(icon = icon, accent = MaterialTheme.colorScheme.primary, size = 36.dp, iconSize = 20.dp)
                        Spacer(Modifier.width(14.dp))
                        LocalizedText(title, fontWeight = FontWeight.Medium)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText("Volume keys open this menu", Modifier.weight(1f), fontSize = 13.sp)
                    Switch(volumeKeys, onVolumeKeysChange, Modifier.testTag("pdf_menu_volume_keys"))
                }
            }
        },
        confirmButton = { TextButton(dismiss) { LocalizedText("Close") } }
    )
}

/** How the reader works, shown on first use and from the reader menu. */
@Composable
private fun ReaderGuideDialog(dismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { LocalizedText("User guide") },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(
                    listOf(
                        "Swipe to turn pages, or use the arrows and the slider at the bottom.",
                        "Pinch or double-tap a page to zoom in and out.",
                        "Tap the surah name at the top to open the Surah and Para index, or to go to a page number.",
                        "Tap the bookmark icon to save the page. Saved pages are in the Bookmarks list.",
                        "Tap the star beside a surah to add it to Favorites.",
                        "Reading mode offers Gallery, Page turn, Scroll and Vertical slide.",
                        "Tap a page for full screen. Tap again to bring the controls back.",
                        "Press a Volume key or the Menu key to open the reader menu.",
                        "The app remembers your page. Use Continue reading to resume."
                    )
                ) { line ->
                    Row {
                        Text("•  ", color = MushafGold, fontWeight = FontWeight.Bold)
                        LocalizedText(line, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(dismiss) { LocalizedText("Got it") } }
    )
}

/** Jumps to a surah, a para or a page number; lists open at the one being read. */
@Composable
private fun PdfNavigatorDialog(
    book: QuranPdf, count: Int, currentPage: Int, currentSurah: Int, currentPara: Int,
    initialTab: Int, readingMode: String, setReadingMode: (String) -> Unit,
    dismiss: () -> Unit, open: (Int) -> Unit
) {
    val context = LocalContext.current
    val choices = remember(context, book.id) { QuranReaderPreferences(context, book) }
    var bookmarks by remember(book.id) { mutableStateOf(choices.bookmarks()) }
    var favorites by remember(book.id) { mutableStateOf(choices.favorites()) }
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    val highlight = MaterialTheme.colorScheme.primary.copy(alpha = .1f)
    AlertDialog(
        onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("quran_pdf_screens", "dismiss_navigator", dismiss),
        title = { LocalizedText(book.title) },
        text = {
            Column {
                ScrollableTabRow(selectedTabIndex = tab, edgePadding = 0.dp) {
                    Tab(tab == 0, { tab = 0 }, Modifier.testTag("pdf_nav_surah"), text = { LocalizedText("Surah") })
                    Tab(tab == 1, { tab = 1 }, Modifier.testTag("pdf_nav_para"), text = { LocalizedText("Para") })
                    Tab(tab == 2, { tab = 2 }, Modifier.testTag("pdf_nav_page"), text = { LocalizedText("Page") })
                    Tab(tab == 3, { tab = 3 }, Modifier.testTag("pdf_nav_bookmarks"), text = { LocalizedText("Bookmarks") })
                    Tab(tab == 4, { tab = 4 }, Modifier.testTag("pdf_nav_favorites"), text = { LocalizedText("Favorites") })
                    Tab(tab == 5, { tab = 5 }, Modifier.testTag("pdf_nav_modes"), text = { LocalizedText("Reading mode") })
                }
                when (tab) {
                    0 -> {
                        var query by rememberSaveable { mutableStateOf("") }
                        com.example.core.analytics.AnalyticsSearch("quran_pdf_screens", query)
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
                        com.example.core.analytics.AnalyticsScroll("quran_pdf_screens", listState)
                        LazyColumn(Modifier.heightIn(max = 380.dp).testTag("pdf_surah_list"), listState) {
                            items(surahs, key = { it.number }) { item ->
                                val page = book.surahPages[item.number - 1]
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(if (item.number == currentSurah) highlight else Color.Transparent)
                                        .clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "navigator_open_surah", "tap", item = "surah_${item.number}"); open(page) }
                                        .padding(vertical = 8.dp)
                                        .testTag("pdf_surah_${item.number}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NumberBadge(item.number, item.number == currentSurah)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        LocalizedText(item.englishName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        LocalizedText(
                                            "Para ${book.paraPages.indexOfLast { it <= page } + 1} • Page $page",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                                        )
                                    }
                                    IconButton({ favorites = choices.toggleFavorite(item.number) },
                                        Modifier.testTag("pdf_favorite_surah_${item.number}")) {
                                        Icon(Icons.Default.Star,
                                            localizedUiText(if (item.number in favorites) "Remove favorite" else "Add favorite"),
                                            tint = if (item.number in favorites) MushafGold else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    LocalizedText(item.arabicName, fontFamily = IndoPakFont, fontSize = 18.sp)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .15f))
                            }
                        }
                    }
                    1 -> {
                        val listState = rememberLazyListState((currentPara - 3).coerceAtLeast(0))
                        com.example.core.analytics.AnalyticsScroll("quran_pdf_screens", listState)
                        LazyColumn(Modifier.heightIn(max = 420.dp).testTag("pdf_para_list"), listState) {
                            items((1..30).toList(), key = { it }) { para ->
                                val page = book.paraPages[para - 1]
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(if (para == currentPara) highlight else Color.Transparent)
                                        .clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "navigator_open_para", "tap", item = "para_$para"); open(page) }
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
                    5 -> {
                        Column {
                            listOf("gallery" to "Gallery", "page" to "Page turn", "scroll" to "Scroll", "vertical" to "Vertical slide").forEach { (mode, title) ->
                                Row(Modifier.fillMaxWidth().clickable(role = Role.RadioButton) { setReadingMode(mode) }
                                    .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = mode == readingMode, onClick = { setReadingMode(mode) })
                                    LocalizedText(title)
                                }
                            }
                        }
                    }
                    3 -> {
                        if (bookmarks.isEmpty()) Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Bookmark, null, tint = MushafGold.copy(alpha = .6f), modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            LocalizedText("No bookmarks yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LazyColumn(Modifier.heightIn(max = 380.dp)) {
                            items(bookmarks.sorted(), key = { it }) { savedPage ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    TextButton({ open(savedPage) }, Modifier.weight(1f)) { LocalizedText("Page $savedPage") }
                                    IconButton({ bookmarks = choices.toggleBookmark(savedPage) }) {
                                        Icon(Icons.Default.Delete, localizedUiText("Remove bookmark"))
                                    }
                                }
                            }
                        }
                    }
                    4 -> {
                        if (favorites.isEmpty()) Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Star, null, tint = MushafGold.copy(alpha = .6f), modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            LocalizedText("No favorites yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LazyColumn(Modifier.heightIn(max = 380.dp)) {
                            items(FullQuranCatalog.surahs.filter { it.number in favorites }, key = { it.number }) { item ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    TextButton({ open(book.surahPages[item.number - 1]) }, Modifier.weight(1f)) {
                                        LocalizedText("${item.number}. ${item.englishName}")
                                    }
                                    IconButton({ favorites = choices.toggleFavorite(item.number) }) {
                                        Icon(Icons.Default.Star, localizedUiText("Remove favorite"), tint = MushafGold)
                                    }
                                }
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
                                isError = text.isNotEmpty() && target == null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(onGo = { target?.let(open) }),
                                modifier = Modifier.fillMaxWidth().testTag("pdf_page_input")
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                { com.example.core.analytics.AppAnalytics.interaction("quran_pdf_screens", "navigator_go_to_page", "tap"); target?.let(open) }, enabled = target != null,
                                colors = ButtonDefaults.buttonColors(containerColor = ReaderGreen, contentColor = Color.White)
                            ) { LocalizedText("Open") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(dismiss) { LocalizedText("Close") } }
    )
}
