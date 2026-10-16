package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import com.example.ui.components.EmptyState
import com.example.ui.components.ErrorState
import com.example.ui.components.InlineError
import com.example.ui.components.islamicPattern
import com.example.ui.components.pressScale
import com.example.ui.components.NeedsInternetChip
import com.example.ui.components.OnReconnect
import com.example.ui.components.rememberIsOnline
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hadith.BookIndex
import com.example.hadith.BookSearchIndex
import com.example.hadith.BookTitles
import com.example.hadith.IndexSort
import com.example.hadith.HadithBook
import com.example.ui.components.BackIconButton
import com.example.hadith.HadithCatalog
import com.example.hadith.IslamicBooksCatalog
import com.example.hadith.MuslimahBooksCatalog
import com.example.hadith.WorldLibrary
import com.example.hadith.HadithPdfStore
import com.example.hadith.HadithRepository
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.ArabicFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.ui.components.groupedShape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.graphics.vector.ImageVector

internal val HadithGreen = Color(0xFF173F31)
internal val HadithGold = Color(0xFFD3B765)
internal val HadithGreenDeep = Color(0xFF0E2A20)

// Paper palette follows the app theme so the library never glares in dark mode.
internal val hadithDark: Boolean @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.5f
internal val HadithPaper: Color @Composable get() = if (hadithDark) Color(0xFF151B18) else Color(0xFFF8F3E7)
internal val HadithCard: Color @Composable get() = if (hadithDark) Color(0xFF1F2824) else Color.White
internal val HadithInk: Color @Composable get() = if (hadithDark) Color(0xFFEFF5F2) else Color(0xFF17221D)
internal val HadithGoldDark: Color @Composable get() = if (hadithDark) Color(0xFFD3B765) else Color(0xFF8A6D1F)
internal val HadithMuted: Color @Composable get() = if (hadithDark) Color(0xFFA9B4AE) else Color(0xFF66706A)
internal val HadithLine: Color @Composable get() = if (hadithDark) Color(0xFF34403A) else Color(0xFFE6DDC6)
internal val HadithAccent: Color @Composable get() = if (hadithDark) Color(0xFF8FD1AE) else HadithGreen
internal val HadithOffline: Color @Composable get() = if (hadithDark) Color(0xFF81C784) else Color(0xFF2E7D32)

/** Official-source, multilingual Hadith PDF library. Religious text is never generated or edited. */
@Composable
fun HadithScreen(
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    com.example.core.analytics.AnalyticsScreen("hadith_screen")

    val context = LocalContext.current
    remember { HadithRepository.init(context) }
    val catalog by HadithRepository.state.collectAsState()
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    var openPage by rememberSaveable { mutableStateOf<Int?>(null) }
    val openBook = openId?.let { id -> catalog.books.firstOrNull { it.id == id } }

    val libraryState = rememberSaveableStateHolder()

    LaunchedEffect(Unit) { HadithRepository.load() }
    LaunchedEffect(openBook) { if (openBook == null) onImmersiveChange(false) }

    if (openBook != null) {
        BackHandler { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "reader_system_back", "tap");
            if (isImmersive) {
                onImmersiveChange(false)
            } else {
                openId = null
            }
        }
        HadithPdfReader(
            book = openBook,
            startPage = openPage,
            isImmersive = isImmersive,
            onImmersiveChange = onImmersiveChange,
            onBack = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "close_reader", "back");
                onImmersiveChange(false)
                openId = null
            }
        )
        return
    }

    // Keeps the search text, filters and scroll position while a book is open.
    libraryState.SaveableStateProvider("library") {
        HadithLibrary(
            books = catalog.books,
            loading = catalog.loading,
            refreshing = catalog.refreshing,
            error = catalog.error,
            onRefresh = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "refresh_catalog", "refresh"); HadithRepository.load(forceRefresh = true) },
            onOpen = { openPage = null; openId = it.id },
            onOpenAt = { book, page -> openPage = page; openId = book.id },
            onBack = onBack
        )
    }
}

/** Wording that differs between the Hadith library and the Islamic books library. */
internal data class LibraryCopy(
    val title: String,
    val subtitle: String,
    val searchHint: String,
    val collectionsLabel: String,
    val sourcePolicy: String
)

internal val HadithCopy = LibraryCopy(
    title = "Hadith Library",
    subtitle = "Kutub al-Sittah and classical collections",
    searchHint = "Search a book or a topic…",
    collectionsLabel = "Collections",
    sourcePolicy = "Complete Arabic sets come from Dawat-e-Islami's library, Hindi Kutub al-Sittah sets from the Ekummat collection, and English publisher editions from Emaan Library/OpenMaktaba. " +
        "Downloads are saved byte-for-byte and PDF pages are never translated, rewritten, reordered, or modified. " +
        "Collection, volume, chapter, and page navigation happens in the app around the original PDF."
)

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
    "darimi" to CollectionInfo("سنن الدارمی", "Imam Abdullah ibn Abd ar-Rahman ad-Darimi · d. 255 AH", false),
    IslamicBooksCatalog.BAHAR to CollectionInfo("بہارِ شریعت", "Sadr ash-Shariah Mufti Amjad Ali Azmi · d. 1367 AH", false),
    IslamicBooksCatalog.ANWAR to CollectionInfo("انوارِ شریعت", "Faqih-e-Millat Mufti Jalaluddin Ahmad Amjadi · d. 1422 AH", false),
    IslamicBooksCatalog.SHAWAHID to CollectionInfo("شواہد النبوۃ", "Mawlana Nur ad-Din Abd ar-Rahman Jami · d. 898 AH", false),
    IslamicBooksCatalog.MAARIJ to CollectionInfo("معارج النبوۃ", "Mulla Muin ad-Din Kashifi al-Harawi · d. 907 AH", false),
    // Authors as named on each book's page in Dawat-e-Islami's library; blank where it names none.
    "muslimah_islami_behno_ki_namaz" to CollectionInfo("اسلامی بہنوں کی نماز", "Ameer-e-Ahle Sunnat Maulana Muhammad Ilyas Attar Qadri", false),
    "muslimah_jannati_zevar" to CollectionInfo("جنتی زیور", "Allama Abdul Mustafa Azmi · Al-Madinatul Ilmiyah edition", false),
    "muslimah_sunni_bahishti_zewar" to CollectionInfo("سنی بہشتی زیور", "Khalil-e-Millat Mufti Muhammad Khalil Khan Qadri Barkati", false),
    "muslimah_khawateen_kay_masail" to CollectionInfo("خواتین کے مسائل", "", false),
    "muslimah_chahal_hadees_baraye_khawateen_e_islam" to CollectionInfo("چہل حدیث برائے خواتین اسلام", "", false),
    "muslimah_chadar_aur_char_diwari" to CollectionInfo("چادر اور چار دیواری", "Khalil-e-Millat Mufti Muhammad Khalil Khan Qadri Barkati", false),
    "muslimah_aurat_aur_parda" to CollectionInfo("عورت اور پردہ", "Professor Dr. Muhammad Masood Ahmad", false),
    "muslimah_aurat_aur_aazadi" to CollectionInfo("عورت اور آزادی", "", false),
    "muslimah_saas_bahu_main_sulha_ka_raaz" to CollectionInfo("ساس بہو میں صلح کا راز", "Al-Madinatul Ilmiyah · Dawat-e-Islami", false),
    "muslimah_jannati_khawateen" to CollectionInfo("جنتی خواتین", "Maulana Abu Usaid Ubaid Raza Attari Madani", false)
)

private fun infoFor(collectionId: String, name: String) =
    collectionInfo[collectionId] ?: CollectionInfo(name, "", false)

private fun languageLabel(code: String) = when (code) {
    "ar" -> "العربية"
    "hi" -> "हिन्दी"
    "en" -> "English"
    "ur" -> "اردو"
    "fa" -> "فارسی"
    "kn" -> "ಕನ್ನಡ"
    else -> WorldLibrary.languageName(code) ?: code.uppercase()
}

private val LanguageOrder = listOf("ar", "ur", "en", "hi", "kn", "fa")

private data class HadithCollection(val id: String, val name: String, val books: List<HadithBook>) {
    val info get() = infoFor(id, name)
    val languages get() = books.map { it.languageCode }.distinct()
        .sortedBy { LanguageOrder.indexOf(it).let { i -> if (i < 0) 99 else i } }
}

private fun volumeLabel(book: HadithBook) =
    book.volumeTitle ?: if (book.volumeCount == 1) "Complete edition" else "Volume ${book.volume} of ${book.volumeCount}"

/** An edition is named in its own script; a set adds which volume this file is. */
private fun editionTitle(book: HadithBook): String {
    val native = book.nativeTitle ?: return volumeLabel(book)
    return if (book.volumeCount == 1 && book.volumeTitle == null) native else "$native · ${volumeLabel(book)}"
}

private enum class LibrarySort(val label: String) {
    LIBRARY("Library order"), TITLE_ASC("Title A-Z"), TITLE_DESC("Title Z-A"),
    VOLUMES("Most volumes"), OFFLINE("Offline first")
}

@Composable
internal fun HadithLibrary(
    books: List<HadithBook>,
    loading: Boolean,
    refreshing: Boolean,
    error: String?,
    onRefresh: suspend () -> Unit,
    onOpen: (HadithBook) -> Unit,
    onOpenAt: (HadithBook, Int) -> Unit,
    onBack: (() -> Unit)? = null,
    copy: LibraryCopy = HadithCopy,
    categoryContent: (@Composable () -> Unit)? = null
) {
    com.example.core.analytics.AnalyticsScreen("hadith_library")

    val progress by HadithPdfStore.progress.collectAsState()
    val changes by HadithPdfStore.changes.collectAsState()
    val reading by HadithPdfStore.readingChanges.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val downloadFailedText = localizedUiText("Download failed. Check your internet connection.")
    val retryText = localizedUiText("Retry")
    val appLanguage = LocalAppLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("hadith_screen", query)
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
    var sort by rememberSaveable { mutableStateOf(LibrarySort.LIBRARY) }
    // Checking which files are saved touches the disk, so it runs off the main thread and keeps the
    // previous answer on screen until the new one is ready.
    var offline by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(books, changes) {
        offline = withContext(Dispatchers.IO) { books.filter(HadithPdfStore::isDownloaded).map { it.id }.toSet() }
    }
    val visibleCollections = remember(collections, query, language, sort, offline) {
        val needle = query.trim()
        val byTitle = compareBy<HadithCollection, String>(BookTitles.naturalOrder) { it.name }
        // Every sort is stable, so ties keep the library's own order.
        when (sort) {
            LibrarySort.LIBRARY -> collections
            LibrarySort.TITLE_ASC -> collections.sortedWith(byTitle)
            LibrarySort.TITLE_DESC -> collections.sortedWith(byTitle.reversed())
            LibrarySort.VOLUMES -> collections.sortedByDescending { it.books.size }
            LibrarySort.OFFLINE -> collections.sortedByDescending { c -> c.books.count { it.id in offline } }
        }.mapNotNull { c ->
            val matches = needle.isBlank() || c.name.contains(needle, true) ||
                c.info.arabic.contains(needle) || c.info.compiler.contains(needle, true) ||
                c.books.any { it.romanTitle.contains(needle, true) || it.nativeTitle?.contains(needle, true) == true }
            val shown = c.books.filter { language == HadithCatalog.ALL_LANGUAGES || it.languageCode == language }
            if (matches && shown.isNotEmpty()) c.copy(books = shown) else null
        }
    }
    val recent = remember(books, reading) {
        books.map { it to HadithPdfStore.lastOpened(it) }
            .filter { it.second > 0L }
            .sortedByDescending { it.second }
            .take(6)
            .map { it.first }
    }
    val selected = selectedId?.let { id -> collections.firstOrNull { it.id == id } }

    val searchIndex by BookSearchIndex.state.collectAsState()
    val searchReady = searchIndex is BookSearchIndex.State.Ready
    val searchBooks = remember(books, language) {
        books.filter { language == HadithCatalog.ALL_LANGUAGES || it.languageCode == language }
    }
    // Starts from the last answer to the same search, so returning from a book shows it at once.
    var outcome by remember { mutableStateOf(BookSearchIndex.remembered(query, searchBooks)) }
    var searching by remember { mutableStateOf(false) }
    val pageHits = outcome?.hits.orEmpty()
    LaunchedEffect(Unit) { BookSearchIndex.checkForUpdate() }
    LaunchedEffect(query, searchBooks, searchReady) {
        val needle = query.trim()
        if (needle.length < 2 || !searchReady) {
            outcome = null
            searching = false
            return@LaunchedEffect
        }
        BookSearchIndex.remembered(needle, searchBooks)?.let {
            outcome = it
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(250)
        outcome = BookSearchIndex.search(needle, searchBooks)
        searching = false
    }
    // While searching, results are split between matching books and matching pages. A new query
    // starts on whichever side has answers; the reader can then switch freely.
    var searchScope by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(query) { searchScope = null }
    val activeScope = searchScope ?: if (visibleCollections.isNotEmpty()) 0 else 1

    BackHandler(enabled = selected != null) { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "close_collection_system_back", "back"); selectedId = null }

    fun downloadAll(key: String, targets: List<HadithBook>) {
        if (bulkCollection != null) return
        scope.launch {
            bulkCollection = key
            val failed = targets.count { HadithPdfStore.download(it).isFailure }
            bulkCollection = null
            snackbar.showSnackbar(
                localizeUiText(
                    if (failed == 0) "All volumes are available offline." else "$failed downloads failed; tap again to resume.",
                    appLanguage
                )
            )
        }
    }

    Box(Modifier.fillMaxSize().background(HadithPaper)) {
        Column(Modifier.fillMaxSize()) {
            HadithHero(
                title = selected?.name ?: copy.title,
                subtitle = selected?.info?.compiler?.takeIf { it.isNotBlank() } ?: copy.subtitle,
                onBack = com.example.core.analytics.AppAnalytics.trackOptionalAction("hadith_screen", "library_back", if (selected != null) ({ selectedId = null }) else onBack),
                onInfo = { showSource = true },
                stats = if (selected == null && books.isNotEmpty()) listOf(
                    "${collections.size}" to copy.collectionsLabel,
                    "${books.size}" to VolumesLabel,
                    "${offline.size}" to "Offline"
                ) else emptyList()
            )

            if (selected == null) categoryContent?.invoke()

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
                    reading = reading,
                    bulkBusy = bulkCollection == selected.id,
                    onDownloadAll = { downloadAll(selected.id, it) },
                    onOpen = onOpen,
                    onDownload = { book ->
                        scope.launch {
                            while (HadithPdfStore.download(book).isFailure &&
                                snackbar.showSnackbar(downloadFailedText, actionLabel = retryText) ==
                                androidx.compose.material3.SnackbarResult.ActionPerformed
                            ) Unit
                        }
                    },
                    onDelete = { confirmDelete = it }
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize().testTag("hadith_book_list"),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "search") {
                        val focusManager = LocalFocusManager.current
                        Column(
                            Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LibrarySearchField(
                                query = query,
                                onQueryChange = { query = it },
                                hint = copy.searchHint,
                                onSearch = { focusManager.clearFocus() },
                                onClear = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "clear_library_search", "tap"); query = "" },
                                modifier = Modifier.testTag("hadith_search")
                            )
                            // Two compact menus replace the old rows of language and sort chips.
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (languages.size > 1) {
                                    val choices = listOf(HadithCatalog.ALL_LANGUAGES) + languages
                                    LibraryMenuChip(
                                        label = if (language == HadithCatalog.ALL_LANGUAGES) localizedUiText(AllLanguagesLabel) else languageLabel(language),
                                        icon = Icons.Default.Translate,
                                        active = language != HadithCatalog.ALL_LANGUAGES,
                                        options = choices.map { if (it == HadithCatalog.ALL_LANGUAGES) localizedUiText(AllLanguagesLabel) else languageLabel(it) },
                                        selectedIndex = choices.indexOf(language),
                                        onSelect = { i ->
                                            com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "select_book_language", "tap", item = choices[i])
                                            language = choices[i]
                                        },
                                        modifier = Modifier.testTag("hadith_language_filter")
                                    )
                                }
                                LibraryMenuChip(
                                    label = localizedUiText(sort.label),
                                    icon = Icons.Default.SwapVert,
                                    active = sort != LibrarySort.LIBRARY,
                                    options = LibrarySort.entries.map { localizedUiText(it.label) },
                                    selectedIndex = sort.ordinal,
                                    onSelect = { i ->
                                        com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "select_sort", "tap", item = "sort_$i")
                                        sort = LibrarySort.entries[i]
                                    },
                                    modifier = Modifier.testTag("hadith_sort")
                                )
                            }
                            if (query.isBlank()) TopicChips(onPick = { query = it })
                        }
                    }

                    if (error != null) item(key = "error") {
                        InlineError(message = error, onRetry = null, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    if (query.isBlank()) {
                        if (recent.isNotEmpty()) {
                            item(key = "resume_title") { SectionTitle("Continue reading") }
                            item(key = "resume") {
                                val book = recent.first()
                                ResumeCard(
                                    book = book,
                                    offline = book.id in offline,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "continue_reading", "tap", item = book.analyticsId()); onOpen(book) }
                                )
                            }
                            if (recent.size > 1) item(key = "recent") {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(recent.drop(1), key = { "recent-${it.id}" }) { book ->
                                        RecentCard(book, onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "continue_reading", "tap", item = book.analyticsId()); onOpen(book) })
                                    }
                                }
                            }
                        }

                        // The six canonical books come first as a grid; every other collection follows as a list.
                        val sittah = visibleCollections.filter { it.info.sittah }
                        val others = visibleCollections.filterNot { it.info.sittah }
                        if (sittah.isNotEmpty()) {
                            item(key = "sittah_title") { SectionTitle("Sihah Sittah", trailing = "${sittah.size}") }
                            items(sittah.chunked(2), key = { row -> "sittah-" + row.joinToString { it.id } }) { row ->
                                Row(
                                    Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    row.forEach { collection ->
                                        SittahTile(
                                            collection = collection,
                                            offlineCount = collection.books.count { it.id in offline },
                                            modifier = Modifier.weight(1f).fillMaxHeight(),
                                            onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "open_collection", "tap", item = collection.id); selectedId = collection.id }
                                        )
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                        if (others.isNotEmpty()) {
                            item(key = "others_title") { SectionTitle(copy.collectionsLabel, trailing = "${others.size}") }
                            itemsIndexed(others, key = { _, c -> c.id }) { index, collection ->
                                CollectionCard(
                                    collection = collection,
                                    offlineCount = collection.books.count { it.id in offline },
                                    shape = groupedShape(index, others.size),
                                    onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "open_collection", "tap", item = collection.id); selectedId = collection.id }
                                )
                            }
                        }
                        if (visibleCollections.isEmpty()) item(key = "empty") {
                            EmptyState(title = "No collection matches your search.", icon = Icons.Default.SearchOff)
                        }
                    } else {
                        item(key = "scope") {
                            SearchScopeSwitch(
                                booksLabel = copy.collectionsLabel,
                                booksCount = visibleCollections.size,
                                pagesCount = if (searchReady) pageHits.size else null,
                                selected = activeScope,
                                onSelect = { searchScope = it }
                            )
                        }
                        if (activeScope == 0) {
                            if (visibleCollections.isEmpty()) item(key = "no_books") {
                                EmptyState(
                                    title = "No collection matches your search.",
                                    icon = Icons.Default.SearchOff,
                                    actionText = "Search inside the books",
                                    onAction = { searchScope = 1 }
                                )
                            }
                            itemsIndexed(visibleCollections, key = { _, c -> c.id }) { index, collection ->
                                CollectionCard(
                                    collection = collection,
                                    offlineCount = collection.books.count { it.id in offline },
                                    shape = groupedShape(index, visibleCollections.size),
                                    onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "open_collection", "tap", item = collection.id); selectedId = collection.id }
                                )
                            }
                        } else {
                            if (searchReady) {
                                item(key = "understood") { UnderstoodLine(outcome?.understood.orEmpty(), searching) }
                                itemsIndexed(pageHits, key = { index, _ -> "page-hit-$index" }) { _, hit ->
                                    PageHitCard(hit, onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "open_page_search_hit", "tap", item = hit.book.analyticsId()); onOpenAt(hit.book, hit.page) })
                                }
                                item(key = "index_note") {
                                    SearchIndexNote(
                                        empty = pageHits.isEmpty() && query.trim().length >= 2,
                                        searching = searching || outcome == null,
                                        updateAvailable = (searchIndex as? BookSearchIndex.State.Ready)?.updateAvailable == true,
                                        onUpdate = BookSearchIndex::download
                                    )
                                }
                            } else item(key = "index_card") {
                                SearchIndexCard(
                                    progress = (searchIndex as? BookSearchIndex.State.Downloading)?.progress,
                                    failed = (searchIndex as? BookSearchIndex.State.Missing)?.failed == true,
                                    onDownload = BookSearchIndex::download
                                )
                            }
                        }
                    }
                    item(key = "source") {
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "open_source_info", "tap"); showSource = true }
                                .heightIn(min = 48.dp)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Verified, null, tint = HadithAccent, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            LocalizedText(
                                "Original publisher PDFs · text never modified", fontSize = 11.sp, color = HadithMuted,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showSource) {
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "dismiss_source_info", "dismiss"); showSource = false },
            icon = { Icon(Icons.Default.Verified, null, tint = HadithAccent) },
            title = { LocalizedText("Source and translation policy") },
            text = {
                LocalizedText(copy.sourcePolicy)
            },
            confirmButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "close_source_info", "tap"); showSource = false }) { LocalizedText("Close", color = HadithAccent) } }
        )
    }

    confirmDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "dismiss_remove_download", "dismiss"); confirmDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { LocalizedText("Remove download?") },
            text = { LocalizedText("${book.romanTitle} will remain available online.") },
            confirmButton = {
                TextButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "confirm_remove_download", "tap"); HadithPdfStore.delete(book); confirmDelete = null }) {
                    LocalizedText("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "cancel_remove_download", "tap"); confirmDelete = null }) { LocalizedText("Cancel") } }
        )
    }
}

/** Existing library wording, kept in one place because several controls show it. */
private const val AllLanguagesLabel = "All languages"
private const val InsideBooksLabel = "Inside the books"
private const val SearchingLabel = "Searching…"
private const val VolumesLabel = "Volumes"

/** A compact chip that opens a menu of choices: one tap target instead of a row of chips. */
@Composable
private fun LibraryMenuChip(
    label: String,
    icon: ImageVector,
    active: Boolean,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        FilterChip(
            selected = active,
            onClick = { open = true },
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal) },
            leadingIcon = { Icon(icon, null, Modifier.size(16.dp)) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(18.dp)) },
            shape = RoundedCornerShape(50),
            modifier = Modifier.heightIn(min = 40.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = HadithCard,
                labelColor = HadithInk,
                iconColor = HadithAccent,
                selectedContainerColor = HadithGreen,
                selectedLabelColor = Color.White,
                selectedLeadingIconColor = HadithGold,
                selectedTrailingIconColor = Color.White
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = active,
                borderColor = HadithLine,
                selectedBorderColor = HadithGreen
            )
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal) },
                    onClick = { open = false; onSelect(index) },
                    leadingIcon = if (index == selectedIndex) ({ Icon(Icons.Default.Check, null, tint = HadithAccent) }) else null
                )
            }
        }
    }
}

/** While searching: matching books on one side, matching pages inside the books on the other. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScopeSwitch(booksLabel: String, booksCount: Int, pagesCount: Int?, selected: Int, onSelect: (Int) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("hadith_search_scope")) {
        listOf(
            localizedUiText(booksLabel) + " · " + booksCount,
            localizedUiText(InsideBooksLabel) + (pagesCount?.let { " · $it" } ?: "")
        ).forEachIndexed { index, label ->
            SegmentedButton(
                selected = selected == index,
                onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "select_search_scope", "tap", item = "scope_$index"); onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index, 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = HadithGreen,
                    activeContentColor = Color.White,
                    inactiveContainerColor = HadithCard,
                    inactiveContentColor = HadithInk,
                    activeBorderColor = HadithGreen,
                    inactiveBorderColor = HadithLine
                )
            ) { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

@Composable
internal fun HadithHero(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)?,
    onInfo: () -> Unit,
    stats: List<Pair<String, String>>
) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.verticalGradient(listOf(HadithGreen, HadithGreenDeep)))
            // The faint star lattice and a gold glow give the library its own manuscript feel.
            .islamicPattern(Color.White.copy(alpha = .05f))
            .background(
                Brush.radialGradient(
                    listOf(HadithGold.copy(alpha = .16f), Color.Transparent),
                    center = Offset(Float.POSITIVE_INFINITY, 0f),
                    radius = 520f
                )
            )
            .padding(start = if (onBack != null) 4.dp else 16.dp, end = 8.dp, top = 8.dp, bottom = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) BackIconButton(onBack, tint = Color.White)
            Column(Modifier.weight(1f)) {
                LocalizedText(title, color = Color.White, fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                LocalizedText(subtitle, color = HadithGold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onInfo) {
                Icon(Icons.Default.Info, localizedUiText("Source information"), tint = Color.White.copy(alpha = .9f))
            }
        }
        if (stats.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            // One quiet strip instead of three boxed tiles: the numbers matter, not the chrome.
            Row(
                Modifier.padding(start = if (onBack != null) 12.dp else 0.dp, end = 8.dp)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = .08f), RoundedCornerShape(14.dp))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                stats.forEachIndexed { index, (value, label) ->
                    if (index > 0) Box(Modifier.size(width = 1.dp, height = 26.dp).background(Color.White.copy(alpha = .16f)))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(value, color = HadithGold, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        LocalizedText(
                            label, color = Color.White.copy(alpha = .8f), fontSize = 11.sp, lineHeight = 14.sp,
                            textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/** Pill search box on the paper palette, shared by the Hadith, Islamic Books and World libraries. */
@Composable
internal fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth().border(1.dp, HadithLine, shape),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        placeholder = { LocalizedText(hint, color = HadithMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = HadithAccent) },
        trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClear) {
                Icon(Icons.Default.Clear, localizedUiText("Clear"))
            }
        },
        shape = shape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = HadithCard,
            unfocusedContainerColor = HadithCard,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = HadithAccent,
            focusedTextColor = HadithInk,
            unfocusedTextColor = HadithInk
        )
    )
}

/** Section heading on the paper palette: title and an optional quiet count. */
@Composable
internal fun LibrarySectionTitle(text: String, trailing: String? = null, localizeTrailing: Boolean = true) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LocalizedText(
            text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HadithInk,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        if (trailing != null) {
            val pill = Modifier
                .background(HadithGold.copy(alpha = .16f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 3.dp)
            if (localizeTrailing) LocalizedText(trailing, pill, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HadithGoldDark, maxLines = 1)
            else Text(trailing, pill, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HadithGoldDark, maxLines = 1)
        }
    }
}

@Composable
private fun SectionTitle(text: String, trailing: String? = null) = LibrarySectionTitle(text, trailing)

/** One-of-many sort choice, shown as a row that scrolls when it is wider than the screen. */
@Composable
internal fun SortChips(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(com.example.core.analytics.rememberAnalyticsScrollState("hadith_screen")),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.SwapVert, localizedUiText("Sort"), Modifier.size(18.dp), tint = HadithMuted)
        labels.forEachIndexed { index, label ->
            val active = index == selected
            FilterChip(
                selected = active,
                onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "select_sort", "tap", item = "sort_$index"); onSelect(index) },
                label = { LocalizedText(label, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = HadithCard,
                    labelColor = HadithInk,
                    selectedContainerColor = HadithGold.copy(alpha = .28f),
                    selectedLabelColor = HadithInk
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = active,
                    borderColor = HadithLine,
                    selectedBorderColor = HadithGold
                )
            )
        }
    }
}

@Composable
internal fun LanguageChips(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(com.example.core.analytics.rememberAnalyticsScrollState("hadith_screen")),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (listOf(HadithCatalog.ALL_LANGUAGES) + options).forEach { code ->
            val active = code == selected
            FilterChip(
                selected = active,
                onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "select_book_language", "tap", item = code); onSelect(code) },
                label = {
                    LocalizedText(
                        if (code == HadithCatalog.ALL_LANGUAGES) AllLanguagesLabel else languageLabel(code),
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (active) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null,
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = HadithCard,
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

/** Reading position of a book as a 0..1 fraction, or null before it was opened. */
private fun readingFraction(book: HadithBook): Float? {
    if (HadithPdfStore.lastOpened(book) <= 0L || book.pageCount <= 0) return null
    return ((HadithPdfStore.lastPage(book) + 1).toFloat() / book.pageCount).coerceIn(0f, 1f)
}

/** The one book to pick up again: the dominant card at the top of the library. */
@Composable
private fun ResumeCard(book: HadithBook, offline: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val page = HadithPdfStore.lastPage(book) + 1
    val press = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().pressScale(press).testTag("hadith_resume"),
        shape = RoundedCornerShape(22.dp),
        color = HadithGreen,
        shadowElevation = 4.dp,
        interactionSource = press
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(HadithGreen, HadithGreenDeep)))
                .islamicPattern(Color.White.copy(alpha = .05f), cell = 30.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = HadithGold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        languageLabel(book.languageCode) + if (offline) " · " + localizedUiText("Offline") else "",
                        color = Color.White.copy(alpha = .85f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(6.dp))
                LocalizedText(book.collectionName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LocalizedText(volumeLabel(book), color = Color.White.copy(alpha = .8f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                if (book.pageCount > 0) {
                    LinearProgressIndicator(
                        progress = { (page.toFloat() / book.pageCount).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50)),
                        color = HadithGold,
                        trackColor = Color.White.copy(alpha = .18f),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                    Spacer(Modifier.height(6.dp))
                }
                LocalizedText(
                    if (book.pageCount > 0) "Page $page of ${book.pageCount}" else "Page $page",
                    color = HadithGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.width(14.dp))
            Box(
                Modifier.size(48.dp).background(HadithGold, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, localizedUiText("Continue reading"), tint = HadithGreenDeep)
            }
        }
    }
}

/** A smaller card for the other recently opened books. */
@Composable
private fun RecentCard(book: HadithBook, onClick: () -> Unit) {
    val fraction = readingFraction(book)
    val press = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = Modifier.width(180.dp).pressScale(press),
        shape = RoundedCornerShape(16.dp),
        color = HadithCard,
        border = BorderStroke(1.dp, HadithLine),
        interactionSource = press
    ) {
        Column(Modifier.padding(12.dp)) {
            LocalizedText(book.collectionName, color = HadithInk, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LocalizedText(volumeLabel(book), color = HadithMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            if (fraction != null) LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)),
                color = HadithAccent,
                trackColor = HadithLine,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

/** A chapter or page that answers the search; opens the PDF there. */
@Composable
private fun PageHitCard(hit: BookSearchIndex.Hit, onClick: () -> Unit) {
    val accent = HadithAccent
    val rtl = hit.heading != null && hit.book.languageCode in setOf("ar", "ur", "fa")
    val excerpt = remember(hit, accent) {
        buildAnnotatedString {
            append(hit.snippet)
            hit.highlights.forEach { range ->
                addStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold), range.first, range.last + 1)
            }
        }
    }
    val press = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).pressScale(press, pressedScale = 0.98f).testTag("hadith_page_hit"),
        shape = RoundedCornerShape(16.dp),
        color = HadithCard,
        border = BorderStroke(1.dp, HadithLine),
        interactionSource = press
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    "${hit.book.collectionName} · ${languageLabel(hit.book.languageCode)}",
                    fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HadithInk,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                LocalizedText(
                    (if (hit.heading != null) "Chapter · " else "") + "${volumeLabel(hit.book)} · Page ${hit.page + 1}",
                    fontSize = 12.sp, color = HadithGoldDark, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                if (rtl) Text(
                    excerpt, fontFamily = ArabicFontFamily, fontSize = 17.sp, lineHeight = 30.sp, color = HadithInk,
                    maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(),
                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                ) else Text(
                    excerpt, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 4, overflow = TextOverflow.Ellipsis,
                    color = if (hit.heading != null) HadithInk else HadithMuted
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = HadithMuted)
        }
    }
}

/** Offers the one-time search index download, or shows its progress. */
@Composable
private fun SearchIndexCard(progress: Float?, failed: Boolean, onDownload: () -> Unit) {
    val isOnline by rememberIsOnline()
    // A download that failed offline starts again by itself once the connection is back.
    OnReconnect(enabled = failed && progress == null) { onDownload() }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = HadithCard,
        border = BorderStroke(1.dp, HadithLine)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).background(HadithGold.copy(alpha = .16f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Search, null, tint = HadithGoldDark) }
                Spacer(Modifier.width(12.dp))
                LocalizedText("Search inside the books", fontWeight = FontWeight.Bold, color = HadithInk, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            LocalizedText(
                "Type a topic in any language and open the book on the page about it. This needs a one-time download of about 25 MB.",
                fontSize = 13.sp, color = HadithMuted
            )
            if (failed) {
                Spacer(Modifier.height(6.dp))
                LocalizedText(
                    "Download failed. Check your internet connection.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(12.dp))
            if (progress == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onDownload, Modifier.heightIn(min = 48.dp).testTag("hadith_search_index_download"),
                        enabled = isOnline,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = HadithGreen)
                    ) {
                        Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        LocalizedText(if (failed) "Retry" else "Download")
                    }
                    Spacer(Modifier.width(10.dp))
                    NeedsInternetChip()
                }
            } else {
                LocalizedText("Downloading… ${(progress * 100).toInt()}%", fontSize = 12.sp, color = HadithInk)
                Spacer(Modifier.height(6.dp))
                if (progress > 0f) LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth(), color = HadithAccent)
                else LinearProgressIndicator(Modifier.fillMaxWidth(), color = HadithAccent)
            }
        }
    }
}

/** Shortcuts to common topics, which also show that the box takes more than book names. */
@Composable
private fun TopicChips(onPick: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(com.example.core.analytics.rememberAnalyticsScrollState("hadith_screen")),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Lightbulb, null, tint = HadithGoldDark, modifier = Modifier.size(16.dp))
        listOf("Namaz", "Wuzu", "Roza", "Zakat", "Hajj", "Nikah", "Dua", "Janaza", "Tauba", "Sabr").forEach { topic ->
            Text(
                topic,
                fontSize = 13.sp,
                color = HadithAccent,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "pick_search_topic", "tap", item = topic); onPick(topic) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

/** Shows how the question was read, so a surprising result can be traced to a word. */
@Composable
private fun UnderstoodLine(understood: List<String>, searching: Boolean) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        if (understood.isNotEmpty()) Text(
            localizedUiText("Searching for") + ": " + understood.joinToString(" · "),
            fontSize = 12.sp, color = HadithMuted, maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        if (searching) LinearProgressIndicator(
            Modifier.fillMaxWidth().padding(top = 6.dp).height(2.dp), color = HadithAccent, trackColor = HadithLine
        )
    }
}

@Composable
private fun SearchIndexNote(empty: Boolean, searching: Boolean, updateAvailable: Boolean, onUpdate: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (empty) {
            if (searching) {
                LocalizedText(SearchingLabel, fontSize = 13.sp, color = HadithMuted, textAlign = TextAlign.Center)
            } else {
                EmptyState(title = "No page matches. Try another word or spelling.", icon = Icons.Default.SearchOff)
            }
            Spacer(Modifier.height(6.dp))
        }
        LocalizedText(
            "Scanned editions (Urdu, Hindi, Persian, Kannada) cannot be searched inside. Arabic books are matched by chapter title.",
            fontSize = 11.sp, color = HadithMuted, textAlign = TextAlign.Center
        )
        if (updateAvailable) TextButton(onUpdate) { LocalizedText("Update search index", color = HadithAccent) }
    }
}

/** Small "pattern medallion" that stands for a collection: gold book on deep green. */
@Composable
private fun CollectionMedallion(size: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier.size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(Brush.linearGradient(listOf(HadithGreen, HadithGreenDeep)))
            .islamicPattern(HadithGold.copy(alpha = .16f), cell = 18.dp),
        contentAlignment = Alignment.Center
    ) { Icon(Icons.Default.AutoStories, null, tint = HadithGold, modifier = Modifier.size(size * 0.46f)) }
}

/** Volumes and saved-offline counts as icon + number, readable in every language. */
@Composable
private fun CollectionCounts(volumes: Int, offlineCount: Int, languages: List<String>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.LibraryBooks, localizedUiText(VolumesLabel), tint = HadithMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(3.dp))
        Text(volumes.toString(), fontSize = 12.sp, color = HadithMuted, fontWeight = FontWeight.SemiBold)
        if (offlineCount > 0) {
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Default.DownloadDone, localizedUiText("Offline"), tint = HadithOffline, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(3.dp))
            Text(offlineCount.toString(), fontSize = 12.sp, color = HadithOffline, fontWeight = FontWeight.Bold)
        }
        if (languages.isNotEmpty()) {
            Spacer(Modifier.width(10.dp))
            Text(
                languages.joinToString(" · ") { languageLabel(it) },
                fontSize = 12.sp, color = HadithMuted, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** One of the Sihah Sittah: Arabic title first, as it appears on the book's spine. */
@Composable
private fun SittahTile(collection: HadithCollection, offlineCount: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val info = collection.info
    val press = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.pressScale(press).testTag("hadith_collection_${collection.id}"),
        shape = RoundedCornerShape(20.dp),
        color = HadithCard,
        border = BorderStroke(1.dp, HadithLine),
        interactionSource = press
    ) {
        Column(
            Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CollectionMedallion(36.dp)
            Spacer(Modifier.height(2.dp))
            Text(
                info.arabic,
                fontFamily = ArabicFontFamily,
                fontSize = 19.sp,
                lineHeight = 32.sp,
                color = HadithAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
            )
            LocalizedText(
                collection.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HadithInk,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            CollectionCounts(collection.books.size, offlineCount, emptyList())
        }
    }
}

@Composable
private fun CollectionCard(
    collection: HadithCollection,
    offlineCount: Int,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp)
) {
    val info = collection.info
    val press = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).pressScale(press, pressedScale = 0.98f).testTag("hadith_collection_${collection.id}"),
        shape = shape,
        color = HadithCard,
        interactionSource = press
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            CollectionMedallion(44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText(
                        collection.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = HadithInk,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
                    )
                    if (info.sittah) {
                        Spacer(Modifier.width(6.dp))
                        LocalizedText(
                            "Sihah Sittah",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HadithGoldDark, maxLines = 1,
                            modifier = Modifier.background(HadithGold.copy(alpha = .18f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (info.arabic != collection.name) Text(
                    info.arabic,
                    fontFamily = ArabicFontFamily,
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    color = HadithAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                )
                if (info.compiler.isNotBlank()) LocalizedText(
                    info.compiler, fontSize = 11.sp, color = HadithMuted, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                CollectionCounts(collection.books.size, offlineCount, collection.languages)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = HadithMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionDetail(
    collection: HadithCollection,
    language: String,
    onLanguage: (String) -> Unit,
    offline: Set<String>,
    progress: Map<String, Float>,
    reading: Int,
    bulkBusy: Boolean,
    onDownloadAll: (List<HadithBook>) -> Unit,
    onOpen: (HadithBook) -> Unit,
    onDownload: (HadithBook) -> Unit,
    onDelete: (HadithBook) -> Unit
) {
    val activeLanguage = language.takeIf { it in collection.languages } ?: HadithCatalog.ALL_LANGUAGES
    val shown = collection.books.filter { activeLanguage == HadithCatalog.ALL_LANGUAGES || it.languageCode == activeLanguage }
        .sortedBy { it.volume }
    val grouped = collection.languages.mapNotNull { code ->
        shown.filter { it.languageCode == code }.takeIf { it.isNotEmpty() }?.let { code to it }
    }
    val pending = shown.filter { it.id !in offline }
    // The volume of this collection read most recently, so the detail page can offer to continue it.
    val resume = remember(collection, reading) {
        collection.books.filter { HadithPdfStore.lastOpened(it) > 0L }.maxByOrNull { HadithPdfStore.lastOpened(it) }
    }
    val isOnline by rememberIsOnline()
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        Modifier.fillMaxSize().testTag("hadith_volume_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "title") {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = HadithCard,
                border = BorderStroke(1.dp, HadithGold.copy(alpha = .45f))
            ) {
                Column(
                    Modifier.fillMaxWidth()
                        .islamicPattern(HadithGold.copy(alpha = .10f), cell = 30.dp)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        collection.info.arabic,
                        fontFamily = ArabicFontFamily,
                        fontSize = 28.sp,
                        lineHeight = 50.sp,
                        color = HadithAccent,
                        textAlign = TextAlign.Center,
                        style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                    )
                    Box(Modifier.size(width = 56.dp, height = 2.dp).background(HadithGold, RoundedCornerShape(1.dp)))
                    Spacer(Modifier.height(10.dp))
                    // Icon + number reads the same in every language.
                    val pages = collection.books.sumOf { it.pageCount }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        DetailStat(Icons.Default.LibraryBooks, "${collection.books.size}", VolumesLabel)
                        DetailStat(Icons.Default.Translate, "${collection.languages.size}", "Language")
                        if (pages > 0) DetailStat(Icons.Default.Description, "$pages", "Page")
                    }
                }
            }
        }
        if (resume != null) item(key = "resume") {
            ResumeCard(
                book = resume,
                offline = resume.id in offline,
                onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "continue_reading", "tap", item = resume.analyticsId()); onOpen(resume) }
            )
        }
        if (collection.languages.size > 1) item(key = "languages") {
            if (collection.languages.size <= 3) {
                val choices = listOf(HadithCatalog.ALL_LANGUAGES) + collection.languages
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("hadith_collection_languages")) {
                    choices.forEachIndexed { index, code ->
                        SegmentedButton(
                            selected = activeLanguage == code,
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "select_book_language", "tap", item = code); onLanguage(code) },
                            shape = SegmentedButtonDefaults.itemShape(index, choices.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = HadithGreen,
                                activeContentColor = Color.White,
                                inactiveContainerColor = HadithCard,
                                inactiveContentColor = HadithInk,
                                activeBorderColor = HadithGreen,
                                inactiveBorderColor = HadithLine
                            ),
                            icon = {}
                        ) {
                            Text(
                                if (code == HadithCatalog.ALL_LANGUAGES) localizedUiText(AllLanguagesLabel) else languageLabel(code),
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else LanguageChips(collection.languages, activeLanguage, onLanguage)
        }
        item(key = "download_all") {
            // Reading is the main action once a volume is started; saving everything is secondary then.
            val colors = if (resume == null) ButtonDefaults.buttonColors(
                containerColor = HadithGreen,
                contentColor = Color.White,
                disabledContainerColor = HadithOffline.copy(alpha = .14f),
                disabledContentColor = HadithOffline
            ) else ButtonDefaults.buttonColors(
                containerColor = HadithCard,
                contentColor = HadithAccent,
                disabledContainerColor = HadithOffline.copy(alpha = .14f),
                disabledContentColor = HadithOffline
            )
            Column {
                Button(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "download_collection", "tap"); haptics.performHapticFeedback(HapticFeedbackType.LongPress); onDownloadAll(pending) },
                    enabled = pending.isNotEmpty() && !bulkBusy && isOnline,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("hadith_download_all"),
                    shape = RoundedCornerShape(50),
                    border = if (resume != null) BorderStroke(1.dp, HadithLine) else null,
                    colors = colors
                ) {
                    when {
                        bulkBusy -> CircularProgressIndicator(Modifier.size(18.dp), color = if (resume == null) Color.White else HadithAccent, strokeWidth = 2.dp)
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
                if (pending.isNotEmpty()) NeedsInternetChip(Modifier.padding(top = 6.dp))
            }
        }
        grouped.forEach { (code, list) ->
            if (grouped.size > 1) item(key = "header-$code") {
                Text(
                    languageLabel(code),
                    fontWeight = FontWeight.Bold, color = HadithGoldDark, fontSize = 13.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp).semantics { heading() }
                )
            }
            itemsIndexed(list, key = { _, book -> book.id }) { index, book ->
                VolumeRow(
                    book = book,
                    title = editionTitle(book),
                    downloaded = book.id in offline,
                    progress = progress[book.id],
                    onOpen = { onOpen(book) },
                    onDownload = { onDownload(book) },
                    onDelete = { onDelete(book) },
                    shape = groupedShape(index, list.size, outer = 18.dp, inner = 6.dp)
                )
            }
        }
    }
}

/** A labelled number in the collection header; the label is read out but kept off screen. */
@Composable
private fun DetailStat(icon: ImageVector, value: String, label: String) {
    val description = localizedUiText(label) + ": " + value
    Row(
        Modifier.semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = HadithGoldDark, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HadithInk)
    }
}

@Composable
internal fun CatalogMessage(text: String, loading: Boolean, retry: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) CircularProgressIndicator(color = HadithAccent) else Box(
            Modifier.size(72.dp).background(HadithGold.copy(alpha = .14f), RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.CloudOff, null, Modifier.size(34.dp), tint = HadithGoldDark) }
        Spacer(Modifier.height(12.dp))
        LocalizedText(text, textAlign = TextAlign.Center, color = HadithMuted)
        if (retry != null) {
            Spacer(Modifier.height(12.dp))
            Button(retry, Modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = HadithGreen)) { LocalizedText("Retry") }
            NeedsInternetChip(Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
internal fun VolumeRow(
    book: HadithBook,
    title: String,
    downloaded: Boolean,
    progress: Float?,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(18.dp)
) {
    val reading by HadithPdfStore.readingChanges.collectAsState()
    val lastPage = remember(book.id, reading) { HadithPdfStore.lastPage(book) }
    val fraction = remember(book.id, reading) { readingFraction(book) }
    val isOnline by rememberIsOnline()
    val haptics = LocalHapticFeedback.current
    val press = remember { MutableInteractionSource() }
    Surface(
        onClick = com.example.core.analytics.AppAnalytics.trackItemAction("hadith_screen", "open_book", book.analyticsId(), onOpen),
        modifier = Modifier.fillMaxWidth().pressScale(press, pressedScale = 0.98f).testTag("hadith_book_${book.id}"),
        color = HadithCard,
        shape = shape,
        border = if (downloaded) BorderStroke(1.dp, HadithOffline.copy(alpha = .35f)) else null,
        interactionSource = press
    ) {
        Row(Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            // Spine-like volume badge: the volume number, or a star for a one-file edition.
            Box(
                Modifier.size(width = 44.dp, height = 52.dp)
                    .background(Brush.verticalGradient(listOf(HadithGreen, HadithGreenDeep)), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 10.dp, bottomEnd = 10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (book.volumeCount == 1) "∗" else "${book.volume}", color = HadithGold, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = HadithInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when {
                            downloaded -> Icons.Default.CheckCircle
                            isOnline -> Icons.Default.Cloud
                            else -> Icons.Default.CloudOff
                        },
                        null,
                        Modifier.size(13.dp),
                        tint = if (downloaded) HadithOffline else HadithMuted
                    )
                    Spacer(Modifier.width(4.dp))
                    LocalizedText(
                        buildString {
                            append(if (downloaded) "Offline" else if (isOnline) "Online" else "Needs internet")
                            append(" · ${languageLabel(book.languageCode)}")
                            if (book.pageCount > 0) append(" · ${book.pageCount} pages")
                        },
                        fontSize = 11.sp,
                        color = if (downloaded) HadithOffline else HadithMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (fraction != null && progress == null) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(50)),
                            color = HadithGold,
                            trackColor = HadithLine,
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp,
                            drawStopIndicator = {}
                        )
                        Spacer(Modifier.width(8.dp))
                        LocalizedText(
                            "Continue from page ${lastPage + 1}",
                            fontSize = 11.sp, color = HadithGoldDark, fontWeight = FontWeight.SemiBold, maxLines = 1
                        )
                    }
                }
                if (progress != null) {
                    Spacer(Modifier.height(6.dp))
                    if (progress > 0f) LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = HadithAccent,
                        trackColor = HadithLine,
                        strokeCap = StrokeCap.Round,
                        drawStopIndicator = {}
                    ) else LinearProgressIndicator(
                        Modifier.fillMaxWidth().height(4.dp), color = HadithAccent, trackColor = HadithLine, strokeCap = StrokeCap.Round
                    )
                }
            }
            when {
                downloaded -> IconButton({ haptics.performHapticFeedback(HapticFeedbackType.LongPress); onDelete() }, Modifier.testTag("hadith_delete_${book.id}")) {
                    Icon(Icons.Default.DeleteOutline, localizedUiText("Remove download"), tint = HadithMuted)
                }
                progress != null -> Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = HadithAccent, strokeWidth = 2.dp)
                }
                else -> IconButton(
                    { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onDownload() },
                    Modifier.testTag("hadith_download_${book.id}"),
                    enabled = isOnline
                ) {
                    Icon(Icons.Default.Download, localizedUiText("Download"), tint = if (isOnline) HadithAccent else HadithMuted)
                }
            }
        }
    }
}

/** The app's own collection ID; World Library books come from an open catalog and share one label. */
private fun HadithBook.analyticsId(): String =
    if (collectionId.startsWith("wl-")) "world_library" else collectionId

@Composable
internal fun HadithPdfReader(
    book: HadithBook,
    startPage: Int?,
    isImmersive: Boolean,
    onImmersiveChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    com.example.core.analytics.AnalyticsScreen("hadith_pdf_reader")
    LaunchedEffect(book.id) { com.example.core.analytics.AppAnalytics.content("pdf_book", book.analyticsId()) }

    var document by remember(book.id) { mutableStateOf<HadithPdfStore.Document?>(null) }
    var failed by remember(book.id) { mutableStateOf(false) }
    var attempt by remember(book.id) { mutableIntStateOf(0) }
    val progress by HadithPdfStore.progress.collectAsState()

    LaunchedEffect(book.id, attempt) {
        failed = false
        HadithPdfStore.open(book).onSuccess { document = it }.onFailure { failed = true }
    }
    DisposableEffect(document) {
        val opened = document
        onDispose { opened?.let { HadithPdfStore.release(book, it) } }
    }

    document?.let {
        HadithPdfPager(book, it, startPage, isImmersive, onImmersiveChange, onBack)
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
                // Offline it explains the situation instead, and retries by itself on reconnect.
                failed -> ErrorState(
                    onRetry = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "retry_open_book", "tap"); attempt++ },
                    message = "Could not open this official PDF. Check your internet connection and try again."
                )
                fetched != null -> {
                    LocalizedText(
                        if (fetched > 0f) "Loading official PDF… ${(fetched * 100).toInt()}%" else "Loading official PDF…",
                        textAlign = TextAlign.Center, color = HadithInk
                    )
                    Spacer(Modifier.height(10.dp))
                    if (fetched > 0f) LinearProgressIndicator(
                        progress = { fetched }, Modifier.fillMaxWidth(), color = HadithAccent
                    ) else LinearProgressIndicator(Modifier.fillMaxWidth(), color = HadithAccent)
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
    detail: String? = null,
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
                .then(if (onSubtitleClick != null) Modifier.clickable(role = Role.Button, onClick = com.example.core.analytics.AppAnalytics.trackAction("hadith_screen", "open_page_dialog_title", onSubtitleClick)).testTag("hadith_page_navigator") else Modifier)
                .padding(vertical = 4.dp)
        ) {
            LocalizedText(title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) LocalizedText(subtitle, color = HadithGold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            // The chapter being read, straight from the PDF's own bookmarks; never translated.
            if (detail != null) Text(
                detail, color = Color.White.copy(alpha = .75f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = LocalTextStyle.current.copy(textDirection = TextDirection.Content)
            )
        }
        content()
    }
}

@Composable
private fun HadithPdfPager(
    book: HadithBook,
    document: HadithPdfStore.Document,
    /** Page a search result asked for; otherwise reading resumes where it stopped. */
    startPage: Int?,
    isImmersive: Boolean,
    onImmersiveChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val count = document.pageCount
    val chapters by document.chapterUpdates.collectAsState()
    val context = LocalContext.current
    val state = rememberPagerState(
        initialPage = (startPage ?: HadithPdfStore.lastPage(book)).coerceIn(0, (count - 1).coerceAtLeast(0))
    ) { count }
    com.example.core.analytics.AnalyticsReaderSession("pdf_book", state.currentPage)
    val scope = rememberCoroutineScope()
    var zoomed by remember { mutableStateOf(false) }
    var showPageDialog by rememberSaveable { mutableStateOf(false) }
    var showChapterDialog by rememberSaveable { mutableStateOf(false) }
    var sharing by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val shareFailed = localizedUiText("Could not share this Hadith book. Check your internet connection and try again.")
    val last = count - 1
    val haptics = LocalHapticFeedback.current
    var marks by remember(book.id) { mutableStateOf(HadithPdfStore.bookmarks(book)) }
    val marked = state.currentPage in marks
    val entries = remember(chapters, count) { BookIndex.entries(chapters, count) }
    val chapterNow = remember(entries, state.currentPage) { BookIndex.current(entries, state.currentPage + 1)?.title }
    var showMenu by remember { mutableStateOf(false) }
    fun shareBook() {
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
    }
    // Books in right-to-left scripts turn pages the other way. Sinhala ("si") is left-to-right.
    val rtlBook = book.languageCode in setOf("ar", "ur", "sd", "fa", "ps", "ks", "ug", "he", "dv")

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
                detail = chapterNow,
                onBack = onBack
            ) {
                IconButton(
                    {
                        com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "toggle_bookmark", "tap")
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        HadithPdfStore.toggleBookmark(book, state.currentPage)
                        marks = HadithPdfStore.bookmarks(book)
                    },
                    Modifier.testTag("hadith_bookmark")
                ) {
                    Icon(
                        if (marked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        if (marked) localizedUiText("Remove bookmark") else localizedUiText("Add bookmark"),
                        tint = if (marked) HadithGold else Color.White
                    )
                }
                IconButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "open_chapter_index", "tap"); showChapterDialog = true }, Modifier.testTag("hadith_pdf_index")) {
                    Icon(Icons.Default.FormatListNumbered, localizedUiText("PDF index"), tint = Color.White)
                }
                // Share and full screen are occasional actions, so they live in one menu.
                Box {
                    IconButton({ showMenu = true }, Modifier.testTag("hadith_reader_menu")) {
                        if (sharing) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Icon(Icons.Default.MoreVert, localizedUiText("More"), tint = Color.White)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { LocalizedText("Share Hadith PDF") },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                            enabled = !sharing,
                            onClick = { showMenu = false; com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "share_pdf", "tap"); shareBook() },
                            modifier = Modifier.testTag("hadith_pdf_share")
                        )
                        DropdownMenuItem(
                            text = { LocalizedText("Full screen") },
                            leadingIcon = { Icon(Icons.Default.Fullscreen, null) },
                            onClick = { showMenu = false; com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "enter_full_screen", "tap"); onImmersiveChange(true) }
                        )
                    }
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
                    color = HadithGreen.copy(alpha = .9f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, HadithGold.copy(alpha = .35f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "previous_page_full_screen", "tap"); turn(-1) }, enabled = state.currentPage > 0) {
                            Icon(Icons.AutoMirrored.Filled.NavigateBefore, localizedUiText("Previous page"), tint = Color.White)
                        }
                        LocalizedText("${state.currentPage + 1} / $count", color = Color.White, fontWeight = FontWeight.Bold)
                        IconButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "next_page_full_screen", "tap"); turn(1) }, enabled = state.currentPage < last) {
                            Icon(Icons.AutoMirrored.Filled.NavigateNext, localizedUiText("Next page"), tint = Color.White)
                        }
                        IconButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "exit_full_screen", "tap"); onImmersiveChange(false) }) {
                            Icon(Icons.Default.FullscreenExit, localizedUiText("Exit full screen"), tint = Color.White)
                        }
                    }
                }
            }
        }
        if (!isImmersive && count > 1) {
            var dragging by remember { mutableStateOf<Float?>(null) }
            HorizontalDivider(color = HadithLine)
            Row(
                Modifier.fillMaxWidth().background(HadithCard).padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "previous_page", "tap"); turn(-1) }, enabled = state.currentPage > 0) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, localizedUiText("Previous page"), tint = HadithAccent)
                }
                Slider(
                    value = dragging ?: state.currentPage.toFloat(),
                    onValueChange = { dragging = it },
                    onValueChangeFinished = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "page_slider", "change_complete");
                        dragging?.let { target -> scope.launch { state.scrollToPage(target.toInt().coerceIn(0, last)) } }
                        dragging = null
                    },
                    valueRange = 0f..last.toFloat(),
                    modifier = Modifier.weight(1f).testTag("hadith_page_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = HadithAccent,
                        activeTrackColor = HadithGold,
                        inactiveTrackColor = HadithLine
                    )
                )
                LocalizedText(
                    "${(dragging?.toInt() ?: state.currentPage) + 1}",
                    fontWeight = FontWeight.Bold,
                    color = HadithAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 48.dp).clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "open_page_dialog", "tap"); showPageDialog = true }
                        .padding(vertical = 12.dp)
                )
                IconButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "next_page", "tap"); turn(1) }, enabled = state.currentPage < last) {
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, localizedUiText("Next page"), tint = HadithAccent)
                }
            }
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { SnackbarHost(snackbar) }

    if (showPageDialog) {
        var pageText by rememberSaveable { mutableStateOf((state.currentPage + 1).toString()) }
        val target = pageText.toIntOrNull()?.takeIf { it in 1..count }
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "dismiss_page_dialog", "dismiss"); showPageDialog = false },
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
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "go_to_page", "tap");
                        target?.let { page -> scope.launch { state.scrollToPage(page - 1) } }
                        showPageDialog = false
                    },
                    enabled = target != null
                ) { LocalizedText("Open") }
            },
            dismissButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "cancel_page_dialog", "tap"); showPageDialog = false }) { LocalizedText("Cancel") } }
        )
    }

    if (showChapterDialog) {
        var indexLoading by remember { mutableStateOf(chapters.isEmpty()) }
        LaunchedEffect(document) {
            HadithPdfStore.loadChapters(book, document)
            indexLoading = false
        }
        var chapterQuery by rememberSaveable { mutableStateOf("") }
        com.example.core.analytics.AnalyticsSearch("hadith_screen", chapterQuery)
        var indexSort by rememberSaveable { mutableStateOf(IndexSort.PAGE_ASC) }
        // The dialog opens on bookmarks when the book has some and no chapter index of its own.
        var indexTab by rememberSaveable { mutableIntStateOf(if (entries.isEmpty() && marks.isNotEmpty()) 1 else 0) }
        val indexed = remember(entries, chapterQuery, indexSort) { BookIndex.visible(entries, chapterQuery, indexSort) }
        // Fixed when the index opens, so the list does not jump while it is being read.
        val reading = remember(entries) { BookIndex.current(entries, state.currentPage + 1) }
        val listState = rememberLazyListState()
        com.example.core.analytics.AnalyticsScroll("hadith_screen", listState)
        LaunchedEffect(indexed) {
            val at = indexed.indexOfFirst { it.title == reading?.title && it.page == reading.page }
            listState.scrollToItem((at - 1).coerceAtLeast(0))
        }
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "dismiss_chapter_index", "dismiss"); showChapterDialog = false },
            title = { LocalizedText("PDF index - ${book.collectionName}") },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    IndexTabs(selected = indexTab, bookmarkCount = marks.size, onSelect = { indexTab = it })
                    Spacer(Modifier.height(10.dp))
                    if (indexTab == 1) {
                        BookmarkList(
                            pages = marks,
                            current = state.currentPage,
                            onOpen = { page ->
                                com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "open_bookmark", "tap")
                                scope.launch { state.scrollToPage(page.coerceIn(0, last)) }
                                showChapterDialog = false
                            },
                            onRemove = { page ->
                                com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "remove_bookmark", "tap")
                                HadithPdfStore.toggleBookmark(book, page)
                                marks = HadithPdfStore.bookmarks(book)
                            }
                        )
                        return@Column
                    }
                    if (indexLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = chapterQuery,
                        onValueChange = { chapterQuery = it },
                        singleLine = true,
                        label = { LocalizedText("Search chapters") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth().testTag("hadith_chapter_search")
                    )
                    Spacer(Modifier.height(6.dp))
                    SortChips(
                        labels = IndexSort.entries.map { it.label },
                        selected = indexSort.ordinal,
                        onSelect = { indexSort = IndexSort.entries[it] },
                        modifier = Modifier.testTag("hadith_chapter_sort")
                    )
                    when {
                        entries.isEmpty() && !indexLoading -> LocalizedText(
                            "This original PDF does not contain embedded index/bookmarks. Use its printed contents pages or page navigator.",
                            Modifier.padding(vertical = 20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        indexed.isEmpty() && !indexLoading -> LocalizedText(
                            "No chapter matches your search.",
                            Modifier.padding(vertical = 20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        indexed.isNotEmpty() -> LocalizedText(
                            "${indexed.size} of ${entries.size} entries",
                            Modifier.padding(top = 2.dp, bottom = 4.dp),
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 430.dp), state = listState) {
                        items(indexed, key = { "${it.page}-${it.title}" }) { chapter ->
                            val active = chapter.title == reading?.title && chapter.page == reading.page
                            // Titles sit in one column and page numbers in another, whatever the nesting or script.
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(if (active) HadithGold.copy(alpha = .16f) else Color.Transparent)
                                    .clickable(role = Role.Button) { com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "open_chapter", "tap");
                                        scope.launch { state.scrollToPage((chapter.page - 1).coerceIn(0, last)) }
                                        showChapterDialog = false
                                    }
                                    .testTag("hadith_chapter_${chapter.page}")
                                    .padding(start = (4 + 14 * chapter.depth.coerceAtMost(4)).dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LocalizedText(
                                    chapter.title,
                                    modifier = Modifier.weight(1f),
                                    fontSize = if (chapter.depth == 0) 14.sp else 13.sp,
                                    fontWeight = if (active || chapter.depth == 0) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Content)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "${chapter.page}",
                                    modifier = Modifier.widthIn(min = 38.dp),
                                    textAlign = TextAlign.End,
                                    fontSize = 12.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) HadithGoldDark else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "close_chapter_index", "tap"); showChapterDialog = false }) { LocalizedText("Close") } }
        )
    }
}

/** Switches the index dialog between the PDF's own chapters and the reader's bookmarks. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IndexTabs(selected: Int, bookmarkCount: Int, onSelect: (Int) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("hadith_index_tabs")) {
        listOf(
            localizedUiText("PDF index"),
            localizedUiText("Bookmarks") + if (bookmarkCount > 0) " · $bookmarkCount" else ""
        ).forEachIndexed { index, label ->
            SegmentedButton(
                selected = selected == index,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index, 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = HadithGreen,
                    activeContentColor = Color.White,
                    activeBorderColor = HadithGreen,
                    inactiveBorderColor = HadithLine
                ),
                icon = {}
            ) { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

/** The pages the reader bookmarked in this book; tap to open, or remove one. */
@Composable
private fun BookmarkList(pages: List<Int>, current: Int, onOpen: (Int) -> Unit, onRemove: (Int) -> Unit) {
    if (pages.isEmpty()) {
        Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.BookmarkBorder, null, tint = HadithMuted, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            LocalizedText("No bookmarks yet", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        return
    }
    val pageLabel = localizedUiText("Page")
    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 430.dp).testTag("hadith_bookmark_list")) {
        items(pages, key = { it }) { page ->
            val active = page == current
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) HadithGold.copy(alpha = .16f) else Color.Transparent)
                    .clickable(role = Role.Button) { onOpen(page) }
                    .heightIn(min = 48.dp)
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bookmark, null, tint = HadithGoldDark, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    pageLabel + " " + (page + 1),
                    modifier = Modifier.weight(1f),
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
                IconButton({ onRemove(page) }) {
                    Icon(Icons.Default.Close, localizedUiText("Remove bookmark"), tint = HadithMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
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
        val renderWidth = (constraints.maxWidth * when {
            scale > 2.5f -> 4
            scale > 1f -> 2
            else -> 1
        }).coerceAtMost(4096)
        var bitmap by remember(index) {
            mutableStateOf(document.cached(index, constraints.maxWidth.coerceAtMost(4096)))
        }
        var pageFailed by remember(index) { mutableStateOf(false) }
        var pageAttempt by remember(index) { mutableIntStateOf(0) }

        LaunchedEffect(active) {
            if (!active) { scale = 1f; offset = Offset.Zero }
        }
        LaunchedEffect(scale, active) { if (active) onZoomChange(scale > 1f) }
        LaunchedEffect(index, renderWidth, pageAttempt) {
            pageFailed = false
            val rendered = document.render(index, renderWidth)
            if (rendered != null) bitmap = rendered else pageFailed = true
        }
        // A streamed page that failed offline is fetched again as soon as the connection returns.
        OnReconnect(enabled = pageFailed && active) { pageAttempt++ }

        fun clamp(value: Offset, zoom: Float) = Offset(
            value.x.coerceIn(-(zoom - 1) * boxWidth / 2, (zoom - 1) * boxWidth / 2),
            value.y.coerceIn(-(zoom - 1) * boxHeight / 2, (zoom - 1) * boxHeight / 2)
        )

        val page = bitmap
        if (page == null) {
            if (pageFailed) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    LocalizedText("Could not load this page. Check your internet connection and try again.", textAlign = TextAlign.Center, color = HadithInk)
                    Spacer(Modifier.height(12.dp))
                    Button({ com.example.core.analytics.AppAnalytics.interaction("hadith_screen", "retry_page", "tap"); pageAttempt++ }, colors = ButtonDefaults.buttonColors(containerColor = HadithGreen)) { LocalizedText("Retry") }
                }
            } else CircularProgressIndicator(color = HadithGold)
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
