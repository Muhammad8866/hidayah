package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.content.Intent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hadith.HadithBook
import com.example.ui.components.BackIconButton
import com.example.hadith.HadithCatalog
import com.example.hadith.HadithPdfStore
import com.example.hadith.HadithRepository
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.ArabicFontFamily
import kotlinx.coroutines.launch

private val HadithGreen = Color(0xFF173F31)
private val HadithGold = Color(0xFFD3B765)
private val HadithPaper = Color(0xFFF8F3E7)
private val HadithInk = Color(0xFF17221D)
private val HadithGreenDeep = Color(0xFF0E2A20)
private val HadithGoldDark = Color(0xFF8A6D1F)
private val HadithMuted = Color(0xFF66706A)
private val HadithLine = Color(0xFFE6DDC6)
private val HadithOffline = Color(0xFF2E7D32)

/** Official-source, multilingual Hadith PDF library. Religious text is never generated or edited. */
@Composable
fun HadithScreen(
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    remember { HadithRepository.init(context) }
    val catalog by HadithRepository.state.collectAsState()
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    val openBook = openId?.let { id -> catalog.books.firstOrNull { it.id == id } }

    LaunchedEffect(Unit) { HadithRepository.load() }
    LaunchedEffect(openBook) { if (openBook == null) onImmersiveChange(false) }

    if (openBook != null) {
        BackHandler {
            if (isImmersive) {
                onImmersiveChange(false)
            } else {
                openId = null
            }
        }
        HadithPdfReader(
            book = openBook,
            isImmersive = isImmersive,
            onImmersiveChange = onImmersiveChange,
            onBack = {
                onImmersiveChange(false)
                openId = null
            }
        )
        return
    }

    HadithLibrary(
        books = catalog.books,
        loading = catalog.loading,
        refreshing = catalog.refreshing,
        error = catalog.error,
        onRefresh = { HadithRepository.load(forceRefresh = true) },
        onOpen = { openId = it.id },
        onBack = onBack
    )
}

private data class CollectionInfo(val arabic: String, val compiler: String, val sittah: Boolean)

private val collectionInfo = mapOf(
    "bukhari" to CollectionInfo("صحیح البخاری", "Imam Muhammad ibn Ismail al-Bukhari · d. 256 AH", true),
    "muslim" to CollectionInfo("صحیح مسلم", "Imam Muslim ibn al-Hajjaj · d. 261 AH", true),
    "abu_dawud" to CollectionInfo("سنن ابی داؤد", "Imam Abu Dawud as-Sijistani · d. 275 AH", true),
    "tirmidhi" to CollectionInfo("جامع الترمذی", "Imam Abu Isa at-Tirmidhi · d. 279 AH", true),
    "nasai" to CollectionInfo("سنن النسائی", "Imam Ahmad ibn Shu'ayb an-Nasa'i · d. 303 AH", true),
    "ibn_majah" to CollectionInfo("سنن ابن ماجہ", "Imam Muhammad ibn Yazid Ibn Majah · d. 273 AH", true),
    "muwatta" to CollectionInfo("مؤطا امام مالک", "Imam Malik ibn Anas · d. 179 AH", false),
    "musnad_ahmad" to CollectionInfo("مسند امام احمد", "Imam Ahmad ibn Hanbal · d. 241 AH", false),
    "darimi" to CollectionInfo("سنن الدارمی", "Imam Abdullah ibn Abd ar-Rahman ad-Darimi · d. 255 AH", false)
)

private fun infoFor(collectionId: String, name: String) =
    collectionInfo[collectionId] ?: CollectionInfo(name, "", false)

private fun languageLabel(code: String) = when (code) {
    "ar" -> "العربية"
    "hi" -> "हिन्दी"
    "en" -> "English"
    "ur" -> "اردو"
    else -> code.uppercase()
}

private val LanguageOrder = listOf("ar", "ur", "en", "hi")

private data class HadithCollection(val id: String, val name: String, val books: List<HadithBook>) {
    val info get() = infoFor(id, name)
    val languages get() = books.map { it.languageCode }.distinct()
        .sortedBy { LanguageOrder.indexOf(it).let { i -> if (i < 0) 99 else i } }
}

private fun volumeLabel(book: HadithBook) =
    if (book.volumeCount == 1) "Complete edition" else "Volume ${book.volume} of ${book.volumeCount}"

@Composable
private fun HadithLibrary(
    books: List<HadithBook>,
    loading: Boolean,
    refreshing: Boolean,
    error: String?,
    onRefresh: suspend () -> Unit,
    onOpen: (HadithBook) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val progress by HadithPdfStore.progress.collectAsState()
    val changes by HadithPdfStore.changes.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val downloadFailedText = localizedUiText("Download failed. Check your internet connection.")
    var query by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf(HadithCatalog.ALL_LANGUAGES) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var bulkCollection by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf<HadithBook?>(null) }
    var showSource by rememberSaveable { mutableStateOf(false) }

    val collections = remember(books) {
        books.groupBy { it.collectionId }.map { (id, list) -> HadithCollection(id, list.first().collectionName, list) }
    }
    val languages = remember(books) {
        books.map { it.languageCode }.distinct()
            .sortedBy { LanguageOrder.indexOf(it).let { i -> if (i < 0) 99 else i } }
    }
    val visibleCollections = remember(collections, query, language) {
        val needle = query.trim()
        collections.mapNotNull { c ->
            val matches = needle.isBlank() || c.name.contains(needle, true) ||
                c.info.arabic.contains(needle) || c.info.compiler.contains(needle, true)
            val shown = c.books.filter { language == HadithCatalog.ALL_LANGUAGES || it.languageCode == language }
            if (matches && shown.isNotEmpty()) c.copy(books = shown) else null
        }
    }
    val offline = remember(books, changes) { books.filter(HadithPdfStore::isDownloaded).map { it.id }.toSet() }
    val recent = remember(books, changes) {
        books.map { it to HadithPdfStore.lastOpened(it) }
            .filter { it.second > 0L }
            .sortedByDescending { it.second }
            .take(6)
            .map { it.first }
    }
    val selected = selectedId?.let { id -> collections.firstOrNull { it.id == id } }

    BackHandler(enabled = selected != null) { selectedId = null }

    fun downloadAll(key: String, targets: List<HadithBook>) {
        scope.launch {
            bulkCollection = key
            val failed = targets.count { HadithPdfStore.download(it).isFailure }
            bulkCollection = null
            snackbar.showSnackbar(
                if (failed == 0) "All volumes are available offline." else "$failed downloads failed; tap again to resume."
            )
        }
    }

    Box(Modifier.fillMaxSize().background(HadithPaper)) {
        Column(Modifier.fillMaxSize()) {
            HadithHero(
                title = selected?.name ?: "Hadith Library",
                subtitle = selected?.info?.compiler?.takeIf { it.isNotBlank() } ?: "Kutub al-Sittah and classical collections",
                onBack = if (selected != null) ({ selectedId = null }) else onBack,
                onInfo = { showSource = true },
                stats = if (selected == null && books.isNotEmpty()) listOf(
                    "${collections.size}" to "Collections",
                    "${books.size}" to "Volumes",
                    "${offline.size}" to "Offline"
                ) else emptyList()
            )

            when {
                loading && books.isEmpty() -> CatalogMessage("Loading the official Hadith catalog…", true)
                books.isEmpty() -> CatalogMessage(error ?: "No Hadith books are available.", false) {
                    scope.launch { onRefresh() }
                }
                selected != null -> CollectionDetail(
                    collection = selected,
                    language = language,
                    onLanguage = { language = it },
                    offline = offline,
                    progress = progress,
                    bulkBusy = bulkCollection == selected.id,
                    onDownloadAll = { downloadAll(selected.id, it) },
                    onOpen = onOpen,
                    onDownload = { book ->
                        scope.launch { HadithPdfStore.download(book).onFailure { snackbar.showSnackbar(downloadFailedText) } }
                    },
                    onDelete = { confirmDelete = it }
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize().testTag("hadith_book_list"),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth().testTag("hadith_search"),
                                singleLine = true,
                                placeholder = { LocalizedText("Search Bukhari, Muslim, Tirmidhi…", color = HadithMuted) },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = HadithGreen) },
                                trailingIcon = {
                                    if (query.isNotEmpty()) IconButton({ query = "" }) {
                                        Icon(Icons.Default.Clear, localizedUiText("Clear"))
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = HadithGreen,
                                    focusedTextColor = HadithInk,
                                    unfocusedTextColor = HadithInk
                                )
                            )
                            Spacer(Modifier.height(12.dp))
                            LanguageChips(
                                options = languages,
                                selected = language,
                                onSelect = { language = it },
                                modifier = Modifier.testTag("hadith_language_filter")
                            )
                        }
                    }

                    if (error != null) item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) { LocalizedText(error, Modifier.padding(12.dp), fontSize = 12.sp) }
                    }

                    if (recent.isNotEmpty() && query.isBlank()) {
                        item { SectionTitle("Continue reading") }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(recent, key = { "recent-${it.id}" }) { book ->
                                    ContinueCard(book, offline = book.id in offline, onClick = { onOpen(book) })
                                }
                            }
                        }
                    }

                    item {
                        SectionTitle(
                            if (query.isBlank()) "Collections" else "Results",
                            trailing = "${visibleCollections.size}"
                        )
                    }
                    items(visibleCollections, key = { it.id }) { collection ->
                        CollectionCard(
                            collection = collection,
                            offlineCount = collection.books.count { it.id in offline },
                            onClick = { selectedId = collection.id }
                        )
                    }
                    if (visibleCollections.isEmpty()) item {
                        Column(
                            Modifier.fillMaxWidth().padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, null, Modifier.size(40.dp), tint = HadithMuted)
                            Spacer(Modifier.height(8.dp))
                            LocalizedText("No collection matches your search.", textAlign = TextAlign.Center, color = HadithMuted)
                        }
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { showSource = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Verified, null, tint = HadithGreen, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            LocalizedText("Original publisher PDFs · text never modified", fontSize = 11.sp, color = HadithMuted)
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showSource) {
        AlertDialog(
            onDismissRequest = { showSource = false },
            icon = { Icon(Icons.Default.Verified, null, tint = HadithGreen) },
            title = { LocalizedText("Source and translation policy") },
            text = {
                LocalizedText(
                    "Complete Arabic sets come from Dawat-e-Islami's library, Hindi Kutub al-Sittah sets from the Ekummat collection, and English publisher editions from Emaan Library/OpenMaktaba. " +
                        "Downloads are saved byte-for-byte and PDF pages are never translated, rewritten, reordered, or modified. " +
                        "Collection, volume, chapter, and page navigation happens in the app around the original PDF."
                )
            },
            confirmButton = { TextButton({ showSource = false }) { LocalizedText("Close", color = HadithGreen) } }
        )
    }

    confirmDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { LocalizedText("Remove download?") },
            text = { LocalizedText("${book.romanTitle} will remain available online.") },
            confirmButton = {
                TextButton({ HadithPdfStore.delete(book); confirmDelete = null }) {
                    LocalizedText("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton({ confirmDelete = null }) { LocalizedText("Cancel") } }
        )
    }
}

@Composable
private fun HadithHero(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)?,
    onInfo: () -> Unit,
    stats: List<Pair<String, String>>
) {
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(HadithGreen, HadithGreenDeep)))
            .padding(start = if (onBack != null) 4.dp else 16.dp, end = 8.dp, top = 10.dp, bottom = if (stats.isEmpty()) 14.dp else 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) BackIconButton(onBack, tint = Color.White)
            Column(Modifier.weight(1f)) {
                LocalizedText(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LocalizedText(subtitle, color = HadithGold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onInfo) {
                Icon(Icons.Default.Info, localizedUiText("Source information"), tint = Color.White.copy(alpha = .9f))
            }
        }
        if (stats.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().padding(start = if (onBack != null) 12.dp else 0.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stats.forEach { (value, label) ->
                    Column(
                        Modifier.weight(1f)
                            .background(Color.White.copy(alpha = .09f), RoundedCornerShape(14.dp))
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LocalizedText(value, color = HadithGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        LocalizedText(label, color = Color.White.copy(alpha = .8f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, trailing: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(width = 4.dp, height = 16.dp).background(HadithGold, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        LocalizedText(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HadithInk, modifier = Modifier.weight(1f))
        if (trailing != null) LocalizedText(trailing, fontSize = 12.sp, color = HadithMuted)
    }
}

@Composable
private fun LanguageChips(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (listOf(HadithCatalog.ALL_LANGUAGES) + options).forEach { code ->
            val active = code == selected
            FilterChip(
                selected = active,
                onClick = { onSelect(code) },
                label = {
                    LocalizedText(
                        if (code == HadithCatalog.ALL_LANGUAGES) "All languages" else languageLabel(code),
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (active) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null,
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    labelColor = HadithInk,
                    selectedContainerColor = HadithGreen,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = HadithGold
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = active,
                    borderColor = HadithLine,
                    selectedBorderColor = HadithGreen
                )
            )
        }
    }
}

@Composable
private fun ContinueCard(book: HadithBook, offline: Boolean, onClick: () -> Unit) {
    val page = HadithPdfStore.lastPage(book) + 1
    Card(
        onClick = onClick,
        modifier = Modifier.width(230.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HadithGreen)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = HadithGold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText(
                    languageLabel(book.languageCode) + if (offline) " · Offline" else "",
                    color = Color.White.copy(alpha = .75f), fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            LocalizedText(book.collectionName, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LocalizedText(volumeLabel(book), color = Color.White.copy(alpha = .8f), fontSize = 12.sp, maxLines = 1)
            Spacer(Modifier.height(10.dp))
            if (book.pageCount > 0) {
                LinearProgressIndicator(
                    progress = { (page.toFloat() / book.pageCount).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = HadithGold,
                    trackColor = Color.White.copy(alpha = .18f),
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {}
                )
                Spacer(Modifier.height(6.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LocalizedText(
                    if (book.pageCount > 0) "Page $page of ${book.pageCount}" else "Page $page",
                    color = HadithGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CollectionCard(collection: HadithCollection, offlineCount: Int, onClick: () -> Unit) {
    val info = collection.info
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("hadith_collection_${collection.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, HadithLine)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(56.dp)
                    .background(Brush.linearGradient(listOf(HadithGreen, HadithGreenDeep)), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(46.dp).border(1.dp, HadithGold.copy(alpha = .55f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.AutoStories, null, tint = HadithGold, modifier = Modifier.size(24.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText(
                        collection.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HadithInk,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
                    )
                    if (info.sittah) {
                        Spacer(Modifier.width(6.dp))
                        LocalizedText(
                            "Sihah Sittah",
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HadithGoldDark,
                            modifier = Modifier.background(HadithGold.copy(alpha = .18f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    info.arabic,
                    fontFamily = ArabicFontFamily,
                    fontSize = 18.sp,
                    lineHeight = 32.sp,
                    color = HadithGreen,
                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                )
                if (info.compiler.isNotBlank()) LocalizedText(
                    info.compiler, fontSize = 11.sp, color = HadithMuted, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    collection.languages.forEach { code ->
                        val volumes = collection.books.count { it.languageCode == code }
                        LocalizedText(
                            "${languageLabel(code)} · $volumes",
                            fontSize = 11.sp, color = HadithInk,
                            modifier = Modifier.background(HadithPaper, RoundedCornerShape(8.dp))
                                .border(1.dp, HadithLine, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (offlineCount > 0) Row(
                        Modifier.background(HadithOffline.copy(alpha = .12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DownloadDone, null, tint = HadithOffline, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        LocalizedText("$offlineCount", fontSize = 11.sp, color = HadithOffline, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = HadithMuted)
        }
    }
}

@Composable
private fun CollectionDetail(
    collection: HadithCollection,
    language: String,
    onLanguage: (String) -> Unit,
    offline: Set<String>,
    progress: Map<String, Float>,
    bulkBusy: Boolean,
    onDownloadAll: (List<HadithBook>) -> Unit,
    onOpen: (HadithBook) -> Unit,
    onDownload: (HadithBook) -> Unit,
    onDelete: (HadithBook) -> Unit
) {
    val appLanguage = LocalAppLanguage.current
    val activeLanguage = language.takeIf { it in collection.languages } ?: HadithCatalog.ALL_LANGUAGES
    val shown = collection.books.filter { activeLanguage == HadithCatalog.ALL_LANGUAGES || it.languageCode == activeLanguage }
    val grouped = collection.languages.mapNotNull { code ->
        shown.filter { it.languageCode == code }.takeIf { it.isNotEmpty() }?.let { code to it }
    }
    val pending = shown.filter { it.id !in offline }

    LazyColumn(
        Modifier.fillMaxSize().testTag("hadith_volume_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(1.dp, HadithGold.copy(alpha = .45f))
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        collection.info.arabic,
                        fontFamily = ArabicFontFamily,
                        fontSize = 30.sp,
                        lineHeight = 52.sp,
                        color = HadithGreen,
                        textAlign = TextAlign.Center,
                        style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                    )
                    Box(Modifier.size(width = 56.dp, height = 2.dp).background(HadithGold, RoundedCornerShape(1.dp)))
                    Spacer(Modifier.height(10.dp))
                    val pages = collection.books.sumOf { it.pageCount }
                    LocalizedText(
                        buildString {
                            append("${collection.books.size} volumes")
                            append(" · ${collection.languages.size} languages")
                            if (pages > 0) append(" · $pages+ pages")
                        },
                        fontSize = 12.sp, color = HadithMuted, textAlign = TextAlign.Center
                    )
                }
            }
        }
        if (collection.languages.size > 1) item {
            LanguageChips(collection.languages, activeLanguage, onLanguage)
        }
        item {
            Button(
                onClick = { onDownloadAll(pending) },
                enabled = pending.isNotEmpty() && !bulkBusy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("hadith_download_all"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HadithGreen,
                    contentColor = Color.White,
                    disabledContainerColor = HadithOffline.copy(alpha = .14f),
                    disabledContentColor = HadithOffline
                )
            ) {
                when {
                    bulkBusy -> CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    pending.isEmpty() -> Icon(Icons.Default.DownloadDone, null)
                    else -> Icon(Icons.Default.DownloadForOffline, null)
                }
                Spacer(Modifier.width(8.dp))
                LocalizedText(
                    when {
                        bulkBusy -> "Downloading…"
                        pending.isEmpty() -> "All ${shown.size} volumes saved offline"
                        else -> "Download ${pending.size} volumes for offline"
                    }
                )
            }
        }
        grouped.forEach { (code, list) ->
            if (grouped.size > 1) item(key = "header-$code") {
                LocalizedText(
                    languageLabel(code),
                    fontWeight = FontWeight.Bold, color = HadithGoldDark, fontSize = 13.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )
            }
            items(list, key = HadithBook::id) { book ->
                VolumeRow(
                    book = book,
                    title = book.displayTitle(appLanguage).let { if (it == book.romanTitle) volumeLabel(book) else it },
                    downloaded = book.id in offline,
                    progress = progress[book.id],
                    onOpen = { onOpen(book) },
                    onDownload = { onDownload(book) },
                    onDelete = { onDelete(book) }
                )
            }
        }
    }
}

@Composable
private fun CatalogMessage(text: String, loading: Boolean, retry: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) CircularProgressIndicator(color = HadithGreen) else Icon(
            Icons.Default.CloudOff,
            null,
            Modifier.size(42.dp),
            tint = HadithMuted
        )
        Spacer(Modifier.height(12.dp))
        LocalizedText(text, textAlign = TextAlign.Center, color = HadithMuted)
        if (retry != null) {
            Spacer(Modifier.height(12.dp))
            Button(retry, colors = ButtonDefaults.buttonColors(containerColor = HadithGreen)) { LocalizedText("Retry") }
        }
    }
}

@Composable
private fun VolumeRow(
    book: HadithBook,
    title: String,
    downloaded: Boolean,
    progress: Float?,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val lastPage = remember(book.id) { HadithPdfStore.lastPage(book) }
    val started = remember(book.id) { HadithPdfStore.lastOpened(book) > 0L }
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth().testTag("hadith_book_${book.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, HadithLine)
    ) {
        Row(Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.size(width = 50.dp, height = 58.dp)
                    .background(Brush.verticalGradient(listOf(HadithGreen, HadithGreenDeep)), RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 12.dp, bottomEnd = 12.dp)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LocalizedText(if (book.volumeCount == 1) "∗" else "${book.volume}", color = HadithGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                LocalizedText(if (book.volumeCount == 1) "FULL" else "JILD", color = Color.White.copy(alpha = .75f), fontSize = 8.sp, letterSpacing = 1.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(title, fontWeight = FontWeight.Bold, color = HadithInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (downloaded) Icons.Default.CheckCircle else Icons.Default.Cloud,
                        null,
                        Modifier.size(13.dp),
                        tint = if (downloaded) HadithOffline else HadithMuted
                    )
                    Spacer(Modifier.width(4.dp))
                    LocalizedText(
                        buildString {
                            append(if (downloaded) "Offline" else "Online")
                            append(" · ${languageLabel(book.languageCode)}")
                            if (book.pageCount > 0) append(" · ${book.pageCount} pages")
                        },
                        fontSize = 11.sp,
                        color = if (downloaded) HadithOffline else HadithMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (started && progress == null) {
                    Spacer(Modifier.height(4.dp))
                    LocalizedText(
                        "Continue from page ${lastPage + 1}",
                        fontSize = 11.sp, color = HadithGoldDark, fontWeight = FontWeight.SemiBold
                    )
                }
                if (progress != null) {
                    Spacer(Modifier.height(6.dp))
                    if (progress > 0f) LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = HadithGreen,
                        trackColor = HadithLine,
                        strokeCap = StrokeCap.Round,
                        drawStopIndicator = {}
                    ) else LinearProgressIndicator(
                        Modifier.fillMaxWidth().height(4.dp), color = HadithGreen, trackColor = HadithLine, strokeCap = StrokeCap.Round
                    )
                }
            }
            when {
                downloaded -> IconButton(onDelete, Modifier.testTag("hadith_delete_${book.id}")) {
                    Icon(Icons.Default.DeleteOutline, localizedUiText("Remove download"), tint = HadithMuted)
                }
                progress != null -> Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = HadithGreen, strokeWidth = 2.dp)
                }
                else -> IconButton(onDownload, Modifier.testTag("hadith_download_${book.id}")) {
                    Icon(Icons.Default.Download, localizedUiText("Download"), tint = HadithGreen)
                }
            }
        }
    }
}

@Composable
private fun HadithPdfReader(
    book: HadithBook,
    isImmersive: Boolean,
    onImmersiveChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var document by remember(book.id) { mutableStateOf<HadithPdfStore.Document?>(null) }
    var failed by remember(book.id) { mutableStateOf(false) }
    var attempt by remember(book.id) { mutableIntStateOf(0) }
    val progress by HadithPdfStore.progress.collectAsState()

    LaunchedEffect(book.id, attempt) {
        failed = false
        HadithPdfStore.open(book).onSuccess { document = it }.onFailure { failed = true }
    }

    document?.let {
        HadithPdfPager(book, it, isImmersive, onImmersiveChange, onBack)
        return
    }

    Column(Modifier.fillMaxSize().background(HadithPaper)) {
        HadithReaderBar(book.romanTitle, onBack)
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val fetched = progress[book.id]
            when {
                failed -> {
                    LocalizedText(
                        "Could not open this official PDF. Check your internet connection and try again.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Button({ attempt++ }, colors = ButtonDefaults.buttonColors(containerColor = HadithGreen)) {
                        LocalizedText("Retry")
                    }
                }
                fetched != null -> {
                    LocalizedText(
                        if (fetched > 0f) "Loading official PDF… ${(fetched * 100).toInt()}%" else "Loading official PDF…",
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    if (fetched > 0f) LinearProgressIndicator(
                        progress = { fetched }, Modifier.fillMaxWidth(), color = HadithGreen
                    ) else LinearProgressIndicator(Modifier.fillMaxWidth(), color = HadithGreen)
                }
                else -> CircularProgressIndicator(color = HadithGold)
            }
        }
    }
}

@Composable
private fun HadithReaderBar(
    title: String,
    onBack: () -> Unit,
    subtitle: String? = null,
    onSubtitleClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(HadithGreen, HadithGreenDeep)))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onBack, Modifier.testTag("hadith_reader_back")) {
            Icon(Icons.Default.Close, localizedUiText("Close"), tint = Color.White)
        }
        Column(
            Modifier.weight(1f)
                .then(if (onSubtitleClick != null) Modifier.clickable(onClick = onSubtitleClick).testTag("hadith_page_navigator") else Modifier)
                .padding(vertical = 4.dp)
        ) {
            LocalizedText(title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) LocalizedText(subtitle, color = HadithGold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        content()
    }
}

@Composable
private fun HadithPdfPager(
    book: HadithBook,
    document: HadithPdfStore.Document,
    isImmersive: Boolean,
    onImmersiveChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val count = document.pageCount
    val context = LocalContext.current
    val state = rememberPagerState(
        initialPage = HadithPdfStore.lastPage(book).coerceIn(0, (count - 1).coerceAtLeast(0))
    ) { count }
    val scope = rememberCoroutineScope()
    var zoomed by remember { mutableStateOf(false) }
    var showPageDialog by rememberSaveable { mutableStateOf(false) }
    var showChapterDialog by rememberSaveable { mutableStateOf(false) }
    var sharing by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val shareFailed = localizedUiText("Could not share this Hadith book. Check your internet connection and try again.")
    val last = count - 1
    val rtlBook = book.languageCode in setOf("ar", "ur", "si", "sd")

    LaunchedEffect(state) {
        snapshotFlow { state.settledPage }.collect { HadithPdfStore.saveLastPage(book, it) }
    }
    fun turn(offset: Int) {
        scope.launch { state.animateScrollToPage((state.currentPage + offset).coerceIn(0, last)) }
    }

    Column(Modifier.fillMaxSize().background(HadithPaper)) {
        if (!isImmersive) {
            HadithReaderBar(
                title = book.collectionName,
                subtitle = "${volumeLabel(book)} · Page ${state.currentPage + 1} of $count",
                onSubtitleClick = { showPageDialog = true },
                onBack = onBack
            ) {
                IconButton({ showChapterDialog = true }, Modifier.testTag("hadith_pdf_index")) {
                    Icon(Icons.Default.FormatListNumbered, localizedUiText("PDF index"), tint = Color.White)
                }
                IconButton(
                    onClick = {
                        if (!sharing) scope.launch {
                            sharing = true
                            HadithPdfStore.shareFile(book).onSuccess { file ->
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, book.romanTitle)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(send, context.getString(com.example.R.string.app_name)))
                            }.onFailure { snackbar.showSnackbar(shareFailed) }
                            sharing = false
                        }
                    },
                    enabled = !sharing,
                    modifier = Modifier.testTag("hadith_pdf_share")
                ) {
                    if (sharing) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Share, "Share Hadith PDF", tint = Color.White)
                }
                IconButton({ onImmersiveChange(true) }) {
                    Icon(Icons.Default.Fullscreen, localizedUiText("Full screen"), tint = Color.White)
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            HorizontalPager(
                state = state,
                modifier = Modifier.fillMaxSize().testTag("hadith_pdf_pager"),
                beyondViewportPageCount = 1,
                reverseLayout = rtlBook,
                userScrollEnabled = !zoomed,
                key = { it }
            ) { index ->
                HadithPdfPage(
                    document = document,
                    index = index,
                    active = index == state.currentPage,
                    onZoomChange = { zoomed = it },
                    onTap = { onImmersiveChange(!isImmersive) }
                )
            }
            if (isImmersive) {
                Surface(
                    color = HadithGreen.copy(alpha = .88f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton({ turn(-1) }, enabled = state.currentPage > 0) {
                            Icon(Icons.Default.NavigateBefore, localizedUiText("Previous page"), tint = Color.White)
                        }
                        LocalizedText("${state.currentPage + 1} / $count", color = Color.White, fontWeight = FontWeight.Bold)
                        IconButton({ turn(1) }, enabled = state.currentPage < last) {
                            Icon(Icons.Default.NavigateNext, localizedUiText("Next page"), tint = Color.White)
                        }
                        IconButton({ onImmersiveChange(false) }) {
                            Icon(Icons.Default.FullscreenExit, localizedUiText("Exit full screen"), tint = Color.White)
                        }
                    }
                }
            }
        }
        if (!isImmersive && count > 1) {
            var dragging by remember { mutableStateOf<Float?>(null) }
            Row(
                Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({ turn(-1) }, enabled = state.currentPage > 0) {
                    Icon(Icons.Default.NavigateBefore, localizedUiText("Previous page"), tint = HadithGreen)
                }
                Slider(
                    value = dragging ?: state.currentPage.toFloat(),
                    onValueChange = { dragging = it },
                    onValueChangeFinished = {
                        dragging?.let { target -> scope.launch { state.scrollToPage(target.toInt().coerceIn(0, last)) } }
                        dragging = null
                    },
                    valueRange = 0f..last.toFloat(),
                    modifier = Modifier.weight(1f).testTag("hadith_page_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = HadithGreen,
                        activeTrackColor = HadithGold,
                        inactiveTrackColor = HadithLine
                    )
                )
                LocalizedText(
                    "${(dragging?.toInt() ?: state.currentPage) + 1}",
                    fontWeight = FontWeight.Bold,
                    color = HadithGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 36.dp).clickable { showPageDialog = true }
                )
                IconButton({ turn(1) }, enabled = state.currentPage < last) {
                    Icon(Icons.Default.NavigateNext, localizedUiText("Next page"), tint = HadithGreen)
                }
            }
        }
    }
    SnackbarHost(snackbar, Modifier.fillMaxWidth())

    if (showPageDialog) {
        var pageText by rememberSaveable { mutableStateOf((state.currentPage + 1).toString()) }
        val target = pageText.toIntOrNull()?.takeIf { it in 1..count }
        AlertDialog(
            onDismissRequest = { showPageDialog = false },
            title = { LocalizedText(book.romanTitle, maxLines = 2) },
            text = {
                OutlinedTextField(
                    value = pageText,
                    onValueChange = { pageText = it.filter(Char::isDigit).take(5) },
                    label = { LocalizedText("Page (1 - $count)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("hadith_page_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        target?.let { page -> scope.launch { state.scrollToPage(page - 1) } }
                        showPageDialog = false
                    },
                    enabled = target != null
                ) { LocalizedText("Open") }
            },
            dismissButton = { TextButton({ showPageDialog = false }) { LocalizedText("Cancel") } }
        )
    }

    if (showChapterDialog) {
        var chapterQuery by rememberSaveable { mutableStateOf("") }
        var descending by rememberSaveable { mutableStateOf(false) }
        val indexed = remember(document.chapters, chapterQuery, descending) {
            val filtered = document.chapters.filter { it.title.contains(chapterQuery.trim(), ignoreCase = true) }
            if (descending) filtered.asReversed() else filtered
        }
        AlertDialog(
            onDismissRequest = { showChapterDialog = false },
            title = { LocalizedText("PDF index - ${book.collectionName}") },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = chapterQuery,
                        onValueChange = { chapterQuery = it },
                        singleLine = true,
                        label = { LocalizedText("Search chapters") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth().testTag("hadith_chapter_search")
                    )
                    TextButton({ descending = !descending }) {
                        Icon(if (descending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, null)
                        Spacer(Modifier.width(6.dp))
                        LocalizedText(if (descending) "Chapter last-first" else "Chapter first-last")
                    }
                    if (indexed.isEmpty()) {
                        LocalizedText(
                            "This original PDF does not contain embedded index/bookmarks. Use its printed contents pages or page navigator.",
                            Modifier.padding(vertical = 20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 430.dp)) {
                        items(indexed, key = { "${it.page}-${it.title}" }) { chapter ->
                            ListItem(
                                headlineContent = { LocalizedText(chapter.title, maxLines = 2) },
                                supportingContent = { LocalizedText("PDF page ${chapter.page}") },
                                modifier = Modifier.clickable {
                                    scope.launch { state.scrollToPage((chapter.page - 1).coerceIn(0, last)) }
                                    showChapterDialog = false
                                }.testTag("hadith_chapter_${chapter.page}")
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = { TextButton({ showChapterDialog = false }) { LocalizedText("Close") } }
        )
    }
}

/** A source PDF page fitted to any screen, with 1x–5x pinch zoom and double-tap zoom. */
@Composable
private fun HadithPdfPage(
    document: HadithPdfStore.Document,
    index: Int,
    active: Boolean,
    onZoomChange: (Boolean) -> Unit,
    onTap: () -> Unit
) {
    BoxWithConstraints(
        Modifier.fillMaxSize().testTag("hadith_pdf_page_${index + 1}"),
        contentAlignment = Alignment.Center
    ) {
        val boxWidth = constraints.maxWidth.toFloat()
        val boxHeight = constraints.maxHeight.toFloat()
        var scale by remember(index) { mutableFloatStateOf(1f) }
        var offset by remember(index) { mutableStateOf(Offset.Zero) }
        val renderWidth = (constraints.maxWidth * if (scale > 2.5f) 4 else 2).coerceAtMost(4096)
        var bitmap by remember(index) {
            mutableStateOf(document.cached(index, (constraints.maxWidth * 2).coerceAtMost(4096)))
        }

        LaunchedEffect(active) {
            if (!active) { scale = 1f; offset = Offset.Zero }
        }
        LaunchedEffect(scale, active) { if (active) onZoomChange(scale > 1f) }
        LaunchedEffect(index, renderWidth) { document.render(index, renderWidth)?.let { bitmap = it } }

        fun clamp(value: Offset, zoom: Float) = Offset(
            value.x.coerceIn(-(zoom - 1) * boxWidth / 2, (zoom - 1) * boxWidth / 2),
            value.y.coerceIn(-(zoom - 1) * boxHeight / 2, (zoom - 1) * boxHeight / 2)
        )

        val page = bitmap
        if (page == null) {
            CircularProgressIndicator(color = HadithGold)
        } else {
            val image = remember(page) { page.asImageBitmap() }
            Image(
                bitmap = image,
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
                                if (event.changes.size > 1 || scale > 1f) {
                                    val nextScale = (scale * event.calculateZoom()).coerceIn(1f, 5f)
                                    offset = clamp(offset + event.calculatePan() * nextScale, nextScale)
                                    scale = nextScale
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
