package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.analytics.AppAnalytics
import com.example.hadith.HadithPdfStore
import com.example.hadith.HadithRepository
import com.example.hadith.SiratUlJinanPdfs
import com.example.hadith.WorldLibrary
import com.example.ui.components.rememberIsOnline
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiTemplate
import com.example.ui.localization.localizedUiText
import kotlinx.coroutines.launch

private const val SCREEN = "quran_tafseer_pdfs"

/** Read-tab entry for Sirat-ul-Jinan: the Quran with its tafseer, in the publisher's own PDF volumes. */
@Composable
internal fun SiratUlJinanPdfCard(open: () -> Unit) {
    Surface(
        onClick = AppAnalytics.trackItemAction("quran_pdf_library", "open_tafseer_pdfs", "sirat_ul_jinan", open),
        modifier = Modifier.fillMaxWidth().testTag("quran_tafseer_pdfs_card"),
        color = QuranCard, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MushafGold.copy(alpha = .55f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MushafGold, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Sirat-ul-Jinan", color = QuranGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                LocalizedText("Quran with tafseer · official PDF in 10 jild", color = QuranMuted, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = QuranGreen)
        }
    }
}

/**
 * The Sirat-ul-Jinan volumes, each opened in the library's PDF reader exactly as published. A volume
 * streams page by page until it is downloaded; downloads are the Islamic Books library's own, so a
 * volume saved here is saved there too. [openBookId] is the open volume's book id, or null for the list.
 */
@Composable
internal fun SiratUlJinanPdfLibrary(
    openBookId: String?,
    onOpenBook: (String?) -> Unit,
    onClose: () -> Unit,
    isImmersive: Boolean,
    onImmersiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    com.example.core.analytics.AnalyticsScreen(SCREEN)
    val context = LocalContext.current
    remember {
        HadithRepository.init(context)
        WorldLibrary.init(context)
    }

    val opened = SiratUlJinanPdfs.byId(openBookId)
    if (opened != null) {
        BackHandler {
            AppAnalytics.interaction(SCREEN, "reader_system_back", "tap")
            if (isImmersive) onImmersiveChange(false) else onOpenBook(null)
        }
        HadithPdfReader(
            book = opened.book,
            startPage = null,
            isImmersive = isImmersive,
            onImmersiveChange = onImmersiveChange,
            onBack = {
                AppAnalytics.interaction(SCREEN, "close_reader", "back")
                onImmersiveChange(false)
                onOpenBook(null)
            }
        )
        return
    }
    // A saved route to a volume this list no longer has shows the list instead.
    LaunchedEffect(openBookId) { if (openBookId != null) onOpenBook(null) }
    BackHandler {
        AppAnalytics.interaction(SCREEN, "close_system_back", "back")
        onClose()
    }

    val progress by HadithPdfStore.progress.collectAsState()
    val changes by HadithPdfStore.changes.collectAsState()
    val indexState by WorldLibrary.diIndexState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val downloadFailedText = localizedUiText("Download failed. Check your internet connection.")
    val retryText = localizedUiText("Retry")
    val isOnline by rememberIsOnline()
    var confirmDelete by remember { mutableStateOf<SiratUlJinanPdfs.Volume?>(null) }
    // Volumes the library list has dropped are left out, as on the library's own shelves.
    val urdu = remember(indexState) { SiratUlJinanPdfs.available(SiratUlJinanPdfs.urdu) }
    val hindi = remember(indexState) { SiratUlJinanPdfs.available(SiratUlJinanPdfs.hindi) }
    val hindiFirst = LocalAppLanguage.current == "hi"

    // Through the library, so a download joins its offline shelf, as the library's own volume list does.
    fun download(volume: SiratUlJinanPdfs.Volume) {
        AppAnalytics.interaction(SCREEN, "download_book", "tap", item = volume.book.id)
        scope.launch {
            if (!isOnline) {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(downloadFailedText)
                return@launch
            }
            while (WorldLibrary.download(volume.book).isFailure &&
                snackbar.showSnackbar(downloadFailedText, actionLabel = retryText) == SnackbarResult.ActionPerformed
            ) Unit
        }
    }

    Column(modifier.fillMaxSize().background(MushafPaper).testTag("quran_tafseer_pdfs")) {
        TafseerPdfHeader { AppAnalytics.interaction(SCREEN, "back", "tap"); onClose() }
        Box(Modifier.weight(1f)) {
            LazyColumn(
                Modifier.fillMaxSize().testTag("quran_tafseer_pdf_list"),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val sections = if (hindiFirst) listOf("Hindi" to hindi, "Urdu" to urdu) else listOf("Urdu" to urdu, "Hindi" to hindi)
                sections.forEach { (language, volumes) ->
                    if (volumes.isEmpty()) return@forEach
                    item(key = "label_$language") {
                        LocalizedText(language, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MushafInk, modifier = Modifier.padding(top = 6.dp))
                    }
                    if (volumes === hindi) item(key = "hindi_note") {
                        LocalizedText("Only the first 3 jild are published in Hindi so far.", fontSize = 12.sp, color = QuranMuted)
                    }
                    items(volumes, key = { it.book.id }) { volume ->
                        key(changes) {
                            TafseerVolumeRow(
                                volume = volume,
                                downloaded = HadithPdfStore.isDownloaded(volume.book),
                                progress = progress[volume.book.id],
                                open = {
                                    AppAnalytics.interaction(SCREEN, "open_book", "tap", item = volume.book.id)
                                    onOpenBook(volume.book.id)
                                },
                                download = { download(volume) },
                                delete = {
                                    AppAnalytics.interaction(SCREEN, "open_remove_volume", "tap")
                                    confirmDelete = volume
                                }
                            )
                        }
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }

    confirmDelete?.let { volume ->
        AlertDialog(
            onDismissRequest = { AppAnalytics.interaction(SCREEN, "dismiss_remove_download", "dismiss"); confirmDelete = null },
            title = { LocalizedText("Remove download?") },
            text = { LocalizedText(localizedUiTemplate("{title} will remain available online.", "title" to volume.book.romanTitle)) },
            confirmButton = {
                TextButton({
                    AppAnalytics.interaction(SCREEN, "confirm_remove_download", "tap")
                    WorldLibrary.delete(volume.book)
                    confirmDelete = null
                }) { LocalizedText("Remove") }
            },
            dismissButton = {
                TextButton({ AppAnalytics.interaction(SCREEN, "cancel_remove_download", "tap"); confirmDelete = null }) {
                    LocalizedText("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TafseerPdfHeader(back: () -> Unit) {
    Surface(color = QuranGreen) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(back, Modifier.testTag("quran_tafseer_pdfs_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, localizedUiText("Back"), tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                LocalizedText("Sirat-ul-Jinan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                LocalizedText("Quran with tafseer · official PDF in 10 jild", color = QuranGoldLight, fontSize = 12.sp)
            }
            Text("صراط الجنان", color = QuranGoldLight, fontFamily = IndoPakFont, fontSize = 22.sp, maxLines = 1)
        }
    }
}

/** One published volume: its jild and paras, its own title, size, where reading stopped, and its download state. */
@Composable
private fun TafseerVolumeRow(
    volume: SiratUlJinanPdfs.Volume,
    downloaded: Boolean,
    progress: Float?,
    open: () -> Unit,
    download: () -> Unit,
    delete: () -> Unit
) {
    val book = volume.book
    val started = remember(book.id) { HadithPdfStore.lastOpened(book) > 0L }
    val lastPage = remember(book.id) { HadithPdfStore.lastPage(book) } + 1
    val title = book.nativeTitle ?: book.romanTitle
    Surface(
        onClick = open,
        modifier = Modifier.fillMaxWidth().testTag("quran_tafseer_pdf_${book.id}"),
        shape = RoundedCornerShape(18.dp),
        color = QuranCard,
        border = BorderStroke(1.dp, MushafGold.copy(alpha = .22f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(width = 46.dp, height = 58.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF1F5A45), QuranGreen)),
                        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp)
                    )
                    .border(1.dp, MushafGold.copy(alpha = .7f), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("${volume.jild}", color = QuranGoldLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    localizedUiTemplate("Jild {number}", "number" to volume.jild) + " · " +
                        localizedUiTemplate("Para {para}", "para" to "${volume.firstPara}–${volume.lastPara}"),
                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MushafInk, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    title, fontFamily = if (book.languageCode == "ur") IndoPakFont else null,
                    fontSize = 13.sp, color = MushafInk, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    tafseerSize(volume.sizeBytes) + if (downloaded) " · " + localizedUiText("Offline") else "",
                    fontSize = 11.sp, color = QuranMuted, maxLines = 1
                )
                if (started && progress == null) {
                    LocalizedText(localizedUiTemplate("Last read: page {page}", "page" to lastPage), fontSize = 11.sp, color = MushafGold, fontWeight = FontWeight.SemiBold)
                }
                if (progress != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progress }, Modifier.weight(1f).height(5.dp),
                            color = QuranGreen, trackColor = QuranGoldLight.copy(alpha = .35f), strokeCap = StrokeCap.Round
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("${(progress * 100).toInt()}%", fontSize = 11.sp, color = QuranGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            when {
                downloaded -> IconButton(delete, Modifier.testTag("quran_tafseer_pdf_delete_${book.id}")) {
                    Icon(Icons.Default.Delete, localizedUiText("Remove download"), tint = QuranMuted)
                }
                progress != null -> Unit
                else -> FilledTonalIconButton(
                    download, Modifier.testTag("quran_tafseer_pdf_download_${book.id}"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = QuranGreen.copy(alpha = .1f), contentColor = QuranGreen)
                ) {
                    Icon(Icons.Default.Download, localizedUiText("Download"))
                }
            }
        }
    }
}

/** The published file's size, as the Mushaf list shows sizes. */
private fun tafseerSize(bytes: Long) = "${(bytes + 524_288) / 1_048_576} MB"
