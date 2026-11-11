package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.analytics.AnalyticsScreen
import com.example.core.analytics.AnalyticsSearch
import com.example.core.analytics.AppAnalytics
import com.example.hadith.BookSearchIndex
import com.example.hadith.DiIndexState
import com.example.hadith.HadithBook
import com.example.hadith.HadithCatalog
import com.example.hadith.HadithPdfStore
import com.example.hadith.HadithRepository
import com.example.hadith.IslamicBooksCatalog
import com.example.hadith.IslamicBooksCatalog.Shelf
import com.example.hadith.IslamicBooksCatalog.Work
import com.example.hadith.ProviderNames
import com.example.hadith.WorldCategory
import com.example.hadith.WorldItem
import com.example.hadith.WorldLibrary
import com.example.hadith.WorldSort
import com.example.ui.components.AppCard
import com.example.ui.components.AppSearchField
import com.example.ui.components.BackIconButton
import com.example.ui.components.EmptyState
import com.example.ui.components.ErrorState
import com.example.ui.components.HeroSurface
import com.example.ui.components.LoadingState
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.components.groupedShape
import com.example.ui.components.islamicPattern
import com.example.ui.components.pressScale
import com.example.ui.components.rememberIsOnline
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiTemplate
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HeroGradientBottom
import com.example.ui.theme.HeroGradientTop
import com.example.ui.tour.TourScreen
import com.example.ui.tour.tourTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

/*
 * Islamic Books: one library of Ahle Sunnat books in every language. Featured works (Bahar-e-Shariat
 * and the Ladies / Muslimah shelf) sit above the full book list, which is searched, filtered by
 * category and language, sorted and paged on the phone. A featured book and the same book in the
 * full list are one book: one download, one reading position. Pages are the publishers' original
 * PDFs and are never modified. Titles and authors of the full list keep the library's own words.
 */

/** The list stops paging past this many results; narrower searches reach the rest. */
private const val MAX_BROWSABLE = 10_000

/** The next page is asked for once the last visible row is this close to the end of the list. */
private const val PREFETCH_DISTANCE = 10

private const val SCREEN = "islamic_books_screen"
private const val BOOKS_PREFS = "islamic_books"
private const val KEY_SHELF = "shelf"
private val MuslimahAccent = Color(0xFF9A6286)

/** English source copy; every string here is shown through the app's translations. */
private object BooksCopy {
    const val TITLE = "Islamic Books"
    const val SUBTITLE = "Books by Ahle Sunnat scholars"
    const val SEARCH_HINT = "Search any book or author…"
    const val ALL_LANGUAGES = "All languages"
    const val ALL_BOOKS = "All books"
    const val MUSLIMAH = "Ladies / Muslimah"
    const val CLASSICAL = "Classical books"
    const val COLLECTIONS = "Collections"
    const val SAVED = "Saved offline"
    const val BOOKS = "Books"
    const val RESULTS = "Results"
    const val INSIDE = "Inside the books"
    const val READ = "Read"
    const val CONTINUE = "Continue reading"
    const val CLEAR = "Clear"
    const val LOADING = "Loading…"
    const val SEARCHING = "Searching…"
    const val UNREACHABLE = "Could not reach the library. Check your internet connection."
    const val NO_MATCH = "No book matches your search."
    const val NO_PAGE = "No page matches. Try another word or spelling."
    const val REMOVED_ITEM = "This book has been removed from its source, so it is no longer available here."
    const val NO_PDF = "This entry has no PDF to read."
    const val DOWNLOAD_FAILED = "Download failed. Check your internet connection."
    const val RETRY = "Retry"
    const val REMOVED_SAVED = "Saved books that were removed from their source have been removed here too."
    const val ABOUT = "About this library"
    // Says how the library behaves, never where its books are fetched from.
    const val ABOUT_TEXT = "Only books by Ahle Sunnat wal Jamaat scholars and institutions are shown. " +
        "The book list is refreshed every week. " +
        "When a book is removed from the library it is removed here as well, including any copy saved offline. " +
        "PDF pages are shown exactly as published and are never translated or modified."
    const val CLOSE = "Close"
    const val REMOVE_TITLE = "Remove download?"
    const val REMOVE = "Remove"
    const val CANCEL = "Cancel"
    const val LANGUAGE = "Language"
    const val SORT = "Sort"
    const val CATEGORY = "Category"
    const val ALL_CATEGORIES = "All categories"
    const val INDEX_LOADING = "Downloading the library list (about 12 MB, once a week)…"
    const val INDEX_FAILED = UNREACHABLE
    const val REFRESH = "Refresh the library list"
    const val REFRESH_FAILED = "Could not refresh the library list. The saved list is still shown."
    const val INDEX_STATUS = "Library list: {count} books, updated {date}."
}

/** One search as asked; a page that arrives for an older search is dropped. */
private data class BooksSearch(
    val needle: String,
    val language: String,
    val sort: WorldSort,
    val category: Int?
)

/** Deep, quiet cover tones; each book keeps the same one so it is easy to find again. */
private val CoverTones = listOf(
    Color(0xFF173F31) to Color(0xFF0E2A20),
    Color(0xFF1F4E5F) to Color(0xFF12313C),
    Color(0xFF5A2E2E) to Color(0xFF3A1C1C),
    Color(0xFF2E3A5F) to Color(0xFF1C243C),
    Color(0xFF5C4A1E) to Color(0xFF3B2F12)
)

/**
 * Islamic Books. Search comes first; category, language and order sit in compact pickers above
 * the list. With nothing typed the page shows what you were reading, your offline shelf, the
 * featured shelves and then every book; a typed search shows matching featured works, the pages
 * inside them that answer it, and every matching book of the library.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicBooksScreen(
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    AnalyticsScreen("islamic_books_screen")
    val context = LocalContext.current
    remember {
        HadithRepository.init(context)
        WorldLibrary.init(context)
    }
    val prefs = remember(context) { context.getSharedPreferences(BOOKS_PREFS, Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val saved by WorldLibrary.saved.collectAsState()
    val recent by WorldLibrary.recentSearches.collectAsState()
    val progress by HadithPdfStore.progress.collectAsState()
    val changes by HadithPdfStore.changes.collectAsState()

    // Held here, above the reader, so closing a book returns to the same list and search.
    var query by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf(WorldLibrary.preferredLanguage()) }
    var sortName by rememberSaveable { mutableStateOf(WorldLibrary.preferredSort().name) }
    val sort = WorldSort.entries.firstOrNull { it.name == sortName } ?: WorldSort.POPULAR
    var category by rememberSaveable { mutableStateOf(WorldLibrary.preferredCategory()) }
    var filterName by rememberSaveable { mutableStateOf(BooksFilter.fromName(prefs.getString(KEY_SHELF, null)).name) }
    val filter = BooksFilter.fromName(filterName)
    val diIndex by WorldLibrary.diIndexState.collectAsState()
    val categories = remember(diIndex) { WorldLibrary.diCategories() }
    val languageOptions = WorldLibrary.languages
    // A language no longer offered falls back to every language.
    val activeLanguage = language.takeIf { code ->
        code == HadithCatalog.ALL_LANGUAGES || languageOptions.any { it.code == code }
    } ?: HadithCatalog.ALL_LANGUAGES
    // A category missing from a refreshed list is dropped.
    val activeCategory = category.takeIf { id -> id != null && (categories.isEmpty() || categories.any { it.id == id }) }
    // Featured works the library still lists; a refreshed list can drop one.
    val works = remember(diIndex) { IslamicBooksCatalog.availableWorks() }
    val featured = remember(works, activeLanguage, activeCategory) {
        IslamicBooksHome.featuredFor(works, activeLanguage, categoryActive = activeCategory != null)
    }
    val needle = query.trim()
    AnalyticsSearch("islamic_books_screen", needle)
    val numbers = rememberNumberFormat()
    val locale = LocalAppLocale.current

    var results by remember { mutableStateOf<List<WorldItem>>(emptyList()) }
    var total by remember { mutableStateOf<Int?>(null) }
    var pages by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var moreFailed by remember { mutableStateOf(false) }
    // Set when a page comes back empty, so paging stops instead of asking for empty pages forever.
    var endReached by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<WorldItem?>(null) }
    var volumes by remember { mutableStateOf<Result<List<HadithBook>?>?>(null) }
    var volumesAttempt by remember { mutableIntStateOf(0) }
    var workId by rememberSaveable { mutableStateOf<String?>(null) }
    // The open book: its id survives a configuration change; a book only listed online is kept here.
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    var openPage by rememberSaveable { mutableStateOf<Int?>(null) }
    var openedBook by remember { mutableStateOf<HadithBook?>(null) }
    var confirmDelete by remember { mutableStateOf<HadithBook?>(null) }
    var showSource by remember { mutableStateOf(false) }
    var showLanguages by remember { mutableStateOf(false) }
    var showCategories by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    var searchKey by remember { mutableStateOf<BooksSearch?>(null) }
    // Hoisted so the home list and a work page keep their scroll positions while a book is open.
    val listState = rememberLazyListState()
    val workState = rememberLazyListState()
    com.example.core.analytics.AnalyticsScroll("islamic_books_screen", listState)
    com.example.core.analytics.AnalyticsScroll("islamic_books_screen", workState)

    val downloadFailedText = localizedUiText(BooksCopy.DOWNLOAD_FAILED)
    val retryText = localizedUiText(BooksCopy.RETRY)
    val removedText = localizedUiText(BooksCopy.REMOVED_SAVED)
    val refreshFailedText = localizedUiText(BooksCopy.REFRESH_FAILED)

    // Saved books from other sources are deleted once by the library; say so when it happened.
    LaunchedEffect(Unit) {
        if (WorldLibrary.takeRemovedCount() > 0) snackbar.showSnackbar(removedText)
    }
    // A list refreshed in the background changes this, so the books on screen are searched again in it.
    val listVersion = (diIndex as? DiIndexState.Ready)?.updatedAt
    val listRefreshing = (diIndex as? DiIndexState.Ready)?.refreshing == true
    // Keyed on the trimmed text, so a trailing space does not start a new search. A new key cancels
    // the search still running for the old one.
    LaunchedEffect(needle, activeLanguage, sort, activeCategory, reload, listVersion) {
        // The list is searched on the phone, so it answers almost at once; typing settles briefly first.
        if (needle.isNotEmpty()) delay(120)
        val key = BooksSearch(needle, activeLanguage, sort, activeCategory)
        // The same search run again on a refreshed list keeps as many pages as were shown, so the
        // list does not jump back to the top.
        val keepPages = if (key == searchKey) pages.coerceAtLeast(1) else 1
        searchKey = key
        loading = true
        failed = false
        moreFailed = false
        endReached = false
        val first = WorldLibrary.search(key.needle, key.language, 1, key.sort, key.category).getOrNull()
        if (first != null) {
            var items = first.items
            var loaded = 1
            while (loaded < keepPages) {
                val next = WorldLibrary.search(key.needle, key.language, loaded + 1, key.sort, key.category).getOrNull()?.items
                if (next.isNullOrEmpty()) break
                items = items + next
                loaded++
            }
            results = items.distinctBy(WorldItem::identifier)
            total = first.total
            pages = loaded
        } else {
            results = emptyList()
            total = null
            failed = true
        }
        loading = false
    }
    LaunchedEffect(selected, volumesAttempt) {
        val item = selected ?: return@LaunchedEffect
        volumes = null
        volumes = WorldLibrary.volumes(item).onSuccess { found ->
            if (found == null) results = results.filterNot { it.identifier == item.identifier }
        }
    }

    val openBook = openId?.let { id ->
        openedBook?.takeIf { it.id == id } ?: IslamicBooksCatalog.book(id) ?: saved.firstOrNull { it.id == id }
    }
    LaunchedEffect(openBook) { if (openBook == null) onImmersiveChange(false) }
    // A book listed only online cannot be found again after the app was closed; the list shows instead.
    LaunchedEffect(openId, openBook) { if (openId != null && openBook == null) openId = null }

    fun openReader(book: HadithBook, page: Int? = null) {
        openedBook = book
        openPage = page
        openId = book.id
    }

    fun loadMore() {
        if (loadingMore || endReached) return
        val search = searchKey ?: return
        // Marked before the launch, so a second call in the same frame cannot ask for the same page twice.
        loadingMore = true
        moreFailed = false
        scope.launch {
            try {
                val result = WorldLibrary.search(search.needle, search.language, pages + 1, search.sort, search.category)
                // A page that arrives after the search changed belongs to the old list and is dropped.
                if (search == searchKey) result.onSuccess {
                    // Only an empty page ends paging: a page of books already listed still moves on.
                    if (it.items.isEmpty()) endReached = true
                    results = (results + it.items).distinctBy(WorldItem::identifier)
                    total = it.total
                    pages++
                }.onFailure { moreFailed = true }
            } finally {
                loadingMore = false
            }
        }
    }

    fun selectCategory(id: Int?) {
        AppAnalytics.interaction(SCREEN, "select_category", "tap", item = id?.toString() ?: "all")
        category = id
        WorldLibrary.setPreferredCategory(id)
    }

    fun selectLanguage(code: String) {
        AppAnalytics.interaction(SCREEN, "select_language", "tap", item = code)
        language = code
        WorldLibrary.setPreferredLanguage(code)
    }

    /** Downloads the book list again now; the list already saved stays if this fails. */
    fun refreshIndex() {
        if (refreshing) return
        AppAnalytics.interaction(SCREEN, "refresh_index", "tap")
        scope.launch {
            refreshing = true
            val result = WorldLibrary.refreshDiIndex(force = true)
            refreshing = false
            if (result.isSuccess) reload++ else snackbar.showSnackbar(refreshFailedText)
        }
    }

    fun download(book: HadithBook) {
        AppAnalytics.interaction(SCREEN, "download_volume", "tap", item = book.slug)
        scope.launch {
            while (WorldLibrary.download(book).isFailure &&
                snackbar.showSnackbar(downloadFailedText, actionLabel = retryText) == SnackbarResult.ActionPerformed
            ) Unit
        }
    }

    fun openItem(item: WorldItem) {
        AppAnalytics.interaction(SCREEN, "open_book", "tap", item = item.identifier)
        if (needle.isNotEmpty()) WorldLibrary.rememberSearch(needle)
        volumes = null
        selected = item
    }

    fun openWork(work: Work) {
        AppAnalytics.interaction(SCREEN, "open_collection", "tap", item = work.id)
        if (needle.isNotEmpty()) WorldLibrary.rememberSearch(needle)
        // A different work starts at its top; returning from the reader keeps the place.
        workState.requestScrollToItem(0)
        workId = work.id
    }

    if (openBook != null) {
        BackHandler {
            AppAnalytics.interaction(SCREEN, "reader_system_back", "tap")
            if (isImmersive) onImmersiveChange(false) else openId = null
        }
        HadithPdfReader(
            book = openBook,
            startPage = openPage,
            isImmersive = isImmersive,
            onImmersiveChange = onImmersiveChange,
            onBack = {
                AppAnalytics.interaction(SCREEN, "close_reader", "back")
                onImmersiveChange(false)
                openId = null
            }
        )
        return
    }

    val work = IslamicBooksCatalog.work(workId)
    val item = selected
    BackHandler(enabled = work != null || item != null) {
        AppAnalytics.interaction(SCREEN, "close_book_system_back", "back")
        if (work != null) workId = null else selected = null
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            work != null -> BookWorkPage(
                work = work,
                listState = workState,
                prefs = prefs,
                onBack = { AppAnalytics.interaction(SCREEN, "close_work", "back"); workId = null },
                onOpen = { book -> openReader(book) }
            )
            item != null -> WorldBookDetail(
                item = item,
                volumes = volumes,
                saved = saved,
                progress = progress,
                onBack = { AppAnalytics.interaction(SCREEN, "close_book_detail", "back"); selected = null },
                onRetry = { volumesAttempt++ },
                onOpen = { openReader(it) },
                onDownload = ::download,
                onDelete = { confirmDelete = it }
            )
            else -> {
                val focusManager = LocalFocusManager.current
                // Most recently opened first; a typed search keeps the best match first instead.
                val shelf = remember(saved, needle, changes) {
                    val matched = WorldLibrary.matchSaved(saved, needle)
                    if (needle.isEmpty()) matched.sortedByDescending { HadithPdfStore.lastOpened(it) } else matched
                }
                val offline = remember(changes, saved, works) {
                    (works.flatMap { it.books } + saved).filter(HadithPdfStore::isDownloaded).map { it.id }.toSet()
                }
                // Most recently opened first; only books actually opened count as "reading".
                val reading = remember(changes, saved, works, filter) {
                    val candidates = works.flatMap { it.books } + saved
                    IslamicBooksHome.forFilter(
                        IslamicBooksHome.continueReading(candidates, limit = 8) { HadithPdfStore.lastOpened(it) },
                        filter, works
                    ).take(4)
                }
                val shelves = remember(featured, filter) { IslamicBooksHome.shelves(featured, filter) }
                // Best match first; featured works step aside while a category narrows the list.
                val featuredMatches = remember(needle, featured) {
                    if (needle.isEmpty()) emptyList() else IslamicBooksCatalog.search(needle, featured)
                }
                val featuredBooks = remember(works) { works.flatMap { it.books } }
                // The next page is asked for while some rows are still below the screen, so scrolling
                // rarely reaches a spinner. Nothing is asked for while a search or page is loading, after
                // a failed page (its Retry asks again), or once every book of the search is listed.
                LaunchedEffect(listState) {
                    snapshotFlow {
                        val info = listState.layoutInfo
                        val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
                        info.totalItemsCount > 0 && last >= info.totalItemsCount - 1 - PREFETCH_DISTANCE &&
                            !loading && !loadingMore && !moreFailed && !endReached && results.isNotEmpty() &&
                            results.size < minOf(total ?: 0, MAX_BROWSABLE)
                    }.distinctUntilChanged().collect { nearEnd -> if (nearEnd) loadMore() }
                }
                Column(Modifier.fillMaxSize()) {
                    LibraryHeader(
                        title = BooksCopy.TITLE,
                        subtitle = BooksCopy.SUBTITLE,
                        localize = true,
                        onBack = onBack,
                        actions = {
                            IconButton(
                                onClick = ::refreshIndex,
                                // A refresh already running in the background is not started a second time.
                                enabled = !refreshing && !listRefreshing,
                                modifier = Modifier.testTag("world_library_refresh").tourTarget("library_world_refresh")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = localizedUiText(BooksCopy.REFRESH))
                            }
                            IconButton(
                                onClick = { AppAnalytics.interaction(SCREEN, "about_source", "tap"); showSource = true },
                                modifier = Modifier.testTag("islamic_books_about").tourTarget("library_books_source_info")
                            ) {
                                Icon(Icons.Outlined.Info, contentDescription = localizedUiText(BooksCopy.ABOUT))
                            }
                        }
                    )
                    // Search and the pickers stay put while the list scrolls under them.
                    Column(Modifier.padding(horizontal = Spacing.screen)) {
                        AppSearchField(
                            query = query,
                            onQueryChange = { query = it },
                            placeholder = BooksCopy.SEARCH_HINT,
                            modifier = Modifier.testTag("world_library_search").tourTarget("library_books_search"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                focusManager.clearFocus()
                                WorldLibrary.rememberSearch(needle)
                            })
                        )
                        // Three pickers: the row scrolls sideways on narrow screens instead of squeezing them.
                        Row(
                            Modifier.fillMaxWidth().padding(top = Spacing.sm).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            PickerPill(
                                icon = Icons.Default.Category,
                                // Category names are the library's own words and are not translated.
                                label = activeCategory?.let { id ->
                                    categories.firstOrNull { it.id == id }?.name ?: localizedUiText(BooksCopy.CATEGORY)
                                } ?: localizedUiText(BooksCopy.ALL_CATEGORIES),
                                description = localizedUiText(BooksCopy.CATEGORY),
                                active = activeCategory != null,
                                modifier = Modifier.widthIn(max = 220.dp).testTag("world_library_category").tourTarget("library_world_category"),
                                onClick = { AppAnalytics.interaction(SCREEN, "open_category_sheet", "tap"); showCategories = true }
                            )
                            PickerPill(
                                icon = Icons.Default.Translate,
                                label = if (activeLanguage == HadithCatalog.ALL_LANGUAGES) localizedUiText(BooksCopy.ALL_LANGUAGES)
                                    else languageOptions.firstOrNull { it.code == activeLanguage }?.name
                                        ?: WorldLibrary.languageName(activeLanguage) ?: activeLanguage,
                                description = localizedUiText(BooksCopy.LANGUAGE),
                                active = activeLanguage != HadithCatalog.ALL_LANGUAGES,
                                modifier = Modifier.widthIn(max = 220.dp).testTag("world_library_language").tourTarget("library_world_language"),
                                onClick = { AppAnalytics.interaction(SCREEN, "open_language_sheet", "tap"); showLanguages = true }
                            )
                            SortPicker(
                                sort = sort,
                                onSort = {
                                    AppAnalytics.interaction(SCREEN, "select_sort", "tap", item = it.name)
                                    sortName = it.name
                                    WorldLibrary.setPreferredSort(it)
                                }
                            )
                        }
                    }
                    // A thin bar while a new search replaces the list already on screen, or while the
                    // book list is refreshed behind the saved one that is shown.
                    Box(Modifier.fillMaxWidth().height(3.dp).padding(top = 1.dp)) {
                        if ((loading && results.isNotEmpty()) || refreshing || listRefreshing) LinearProgressIndicator(
                            Modifier.fillMaxWidth().height(2.dp),
                            color = HadithAccent,
                            trackColor = Color.Transparent
                        )
                    }
                    LazyColumn(
                        Modifier.fillMaxSize().testTag("islamic_books_list"),
                        state = listState,
                        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        if (needle.isEmpty()) {
                            if (recent.isNotEmpty()) item(key = "recent") {
                                RecentSearches(
                                    recent = recent,
                                    onPick = {
                                        AppAnalytics.interaction(SCREEN, "recent_search", "tap")
                                        query = it
                                    },
                                    onClear = {
                                        AppAnalytics.interaction(SCREEN, "clear_recent_searches", "tap")
                                        WorldLibrary.clearRecentSearches()
                                    }
                                )
                            }
                            if (reading.isNotEmpty()) {
                                val first = reading.first()
                                item(key = "continue") {
                                    ContinueReadingHero(first, IslamicBooksHome.workOf(first, works), offline = first.id in offline) {
                                        AppAnalytics.interaction(SCREEN, "continue_reading", "tap", item = first.id)
                                        openReader(first)
                                    }
                                }
                                if (reading.size > 1) itemsIndexed(reading.drop(1), key = { _, book -> "reading-${book.id}" }) { index, book ->
                                    RecentBookRow(book, IslamicBooksHome.workOf(book, works), index, reading.size - 1, offline = book.id in offline) {
                                        AppAnalytics.interaction(SCREEN, "continue_reading", "tap", item = book.id)
                                        openReader(book)
                                    }
                                }
                            }
                        }
                        if (shelf.isNotEmpty()) item(key = "shelf") {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                SectionHeading(BooksCopy.SAVED, shelf.size)
                                LazyRow(
                                    Modifier.fillMaxWidth().testTag("world_library_shelf").tourTarget("library_world_shelf"),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                                ) {
                                    items(shelf, key = { "shelf-${it.id}" }) { book ->
                                        ShelfBook(
                                            book = book,
                                            onOpen = {
                                                AppAnalytics.interaction(SCREEN, "open_saved", "tap", item = book.slug)
                                                openReader(book)
                                            },
                                            onRemove = { AppAnalytics.interaction(SCREEN, "shelf_long_press_remove", "long_press"); confirmDelete = book }
                                        )
                                    }
                                }
                            }
                        }
                        if (needle.isEmpty()) {
                            // The featured shelves, unless a category or language leaves none of them.
                            if (featured.isNotEmpty()) {
                                item(key = "shelves") {
                                    ShelfToggle(filter) { option ->
                                        AppAnalytics.interaction(
                                            SCREEN, if (option == BooksFilter.ALL) "filter_all_books" else "filter_muslimah_books", "tap"
                                        )
                                        filterName = option.name
                                        prefs.edit().putString(KEY_SHELF, option.name).apply()
                                    }
                                }
                            }
                            if (shelves.classical.isNotEmpty()) {
                                item(key = "classical_title") { SectionHeading(BooksCopy.CLASSICAL, shelves.classical.size) }
                                // Two covers per row.
                                items(shelves.classical.chunked(2), key = { row -> "classical-" + row.joinToString { it.id } }) { row ->
                                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                        row.forEach { shown ->
                                            WorkCoverCard(shown, offlineCount = shown.books.count { it.id in offline },
                                                onClick = { openWork(shown) }, modifier = Modifier.weight(1f).fillMaxHeight())
                                        }
                                        if (row.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                            if (shelves.muslimah.isNotEmpty()) {
                                item(key = "muslimah_title") { SectionHeading(BooksCopy.MUSLIMAH, shelves.muslimah.size) }
                                itemsIndexed(shelves.muslimah, key = { _, shown -> "muslimah-${shown.id}" }) { index, shown ->
                                    WorkRow(shown, index, shelves.muslimah.size, offlineCount = shown.books.count { it.id in offline }) { openWork(shown) }
                                }
                            }
                        } else {
                            if (featuredMatches.isNotEmpty()) {
                                item(key = "collections_title") { SectionHeading(BooksCopy.COLLECTIONS, featuredMatches.size) }
                                itemsIndexed(featuredMatches, key = { _, shown -> "match-${shown.id}" }) { index, shown ->
                                    WorkRow(shown, index, featuredMatches.size, offlineCount = shown.books.count { it.id in offline }) { openWork(shown) }
                                }
                            }
                            if (featuredBooks.isNotEmpty()) item(key = "inside_books") {
                                InsideBooksSearch(needle, featuredBooks, titleMatches = featuredMatches.size + results.size) { book, page ->
                                    AppAnalytics.interaction(SCREEN, "open_page_search_hit", "tap", item = book.id)
                                    openReader(book, page)
                                }
                            }
                        }
                        item(key = "heading") {
                            SectionHeading(if (needle.isEmpty()) BooksCopy.BOOKS else BooksCopy.RESULTS, total)
                        }
                        when {
                            loading && results.isEmpty() -> {
                                // The first search waits for its book list; say so plainly.
                                if (diIndex is DiIndexState.Loading) item(key = "index-loading") { IndexLoadingNote() }
                                items(4, key = { "skeleton-$it" }) { SkeletonCard() }
                            }
                            failed -> item(key = "error") {
                                val indexFailed = diIndex is DiIndexState.Failed
                                ErrorState(
                                    onRetry = {
                                        if (indexFailed) scope.launch {
                                            WorldLibrary.refreshDiIndex(force = true)
                                            reload++
                                        } else reload++
                                    },
                                    title = if (indexFailed) BooksCopy.INDEX_FAILED else BooksCopy.UNREACHABLE,
                                    retryText = BooksCopy.RETRY
                                )
                            }
                            // Matching featured works are already listed above, so no "no match" under them.
                            results.isEmpty() && featuredMatches.isNotEmpty() -> Unit
                            results.isEmpty() -> item(key = "empty") {
                                // A narrower category or language is the usual reason for no match; offer to widen it.
                                EmptyState(
                                    title = BooksCopy.NO_MATCH,
                                    icon = Icons.Default.SearchOff,
                                    actionText = when {
                                        activeCategory != null -> BooksCopy.ALL_CATEGORIES
                                        activeLanguage != HadithCatalog.ALL_LANGUAGES -> BooksCopy.ALL_LANGUAGES
                                        else -> null
                                    },
                                    onAction = {
                                        if (activeCategory != null) selectCategory(null)
                                        else selectLanguage(HadithCatalog.ALL_LANGUAGES)
                                    }
                                )
                            }
                            else -> {
                                items(results, key = WorldItem::identifier) { entry ->
                                    WorldItemCard(
                                        item = entry,
                                        offlineCount = saved.count { it.slug == entry.identifier },
                                        onClick = { openItem(entry) }
                                    )
                                }
                                if (!endReached && results.size < minOf(total ?: 0, MAX_BROWSABLE)) item(key = "more-$pages") {
                                    if (moreFailed) TextButton(::loadMore, Modifier.fillMaxWidth().heightIn(min = 48.dp).tourTarget("library_world_load_more_retry")) {
                                        LocalizedText(BooksCopy.RETRY, color = HadithAccent)
                                    } else {
                                        // The next page is already being asked for by the scroll watcher above.
                                        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(Modifier.size(22.dp), color = HadithAccent, strokeWidth = 2.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showLanguages) {
        LanguageSheet(
            selected = activeLanguage,
            options = languageOptions,
            onDismiss = { AppAnalytics.interaction(SCREEN, "dismiss_language_sheet", "dismiss"); showLanguages = false },
            onSelect = { code ->
                selectLanguage(code)
                showLanguages = false
            }
        )
    }

    if (showCategories) {
        CategorySheet(
            selected = activeCategory,
            categories = categories,
            onDismiss = { AppAnalytics.interaction(SCREEN, "dismiss_category_sheet", "dismiss"); showCategories = false },
            onSelect = { id ->
                selectCategory(id)
                showCategories = false
            }
        )
    }

    if (showSource) {
        AlertDialog(
            onDismissRequest = { AppAnalytics.interaction(SCREEN, "dismiss_source_info", "dismiss"); showSource = false },
            icon = { Icon(Icons.Default.Verified, null, tint = HadithAccent) },
            title = { LocalizedText(BooksCopy.ABOUT) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    LocalizedText(BooksCopy.ABOUT_TEXT)
                    (diIndex as? DiIndexState.Ready)?.let { ready ->
                        val date = remember(ready.updatedAt, locale) {
                            DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(ready.updatedAt))
                        }
                        Text(
                            localizedUiTemplate(BooksCopy.INDEX_STATUS, "count" to numbers.format(ready.count), "date" to date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = { TextButton({ showSource = false }) { LocalizedText(BooksCopy.CLOSE, color = HadithAccent) } }
        )
    }

    confirmDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { AppAnalytics.interaction(SCREEN, "dismiss_remove_download", "dismiss"); confirmDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { LocalizedText(BooksCopy.REMOVE_TITLE) },
            text = { Text(book.romanTitle) },
            confirmButton = {
                TextButton({
                    AppAnalytics.interaction(SCREEN, "remove_download", "tap", item = book.slug)
                    WorldLibrary.delete(book)
                    confirmDelete = null
                }) {
                    LocalizedText(BooksCopy.REMOVE, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton({ confirmDelete = null }) { LocalizedText(BooksCopy.CANCEL) } }
        )
    }
}

// region Home

/** Every featured shelf, or only the Ladies / Muslimah one; the choice is remembered. */
@Composable
private fun ShelfToggle(filter: BooksFilter, onFilter: (BooksFilter) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().tourTarget("library_books_shelves")) {
        BooksFilter.entries.forEachIndexed { index, option ->
            SegmentedButton(
                selected = filter == option,
                onClick = { onFilter(option) },
                shape = SegmentedButtonDefaults.itemShape(index, BooksFilter.entries.size),
                icon = {
                    SegmentedButtonDefaults.Icon(filter == option) {
                        Icon(
                            if (option == BooksFilter.ALL) Icons.Default.CollectionsBookmark else Icons.Default.LocalFlorist,
                            contentDescription = null, modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier.heightIn(min = 48.dp)
                    .testTag(if (option == BooksFilter.MUSLIMAH) "muslimah_books_filter" else "all_books_filter")
            ) {
                LocalizedText(if (option == BooksFilter.ALL) BooksCopy.ALL_BOOKS else BooksCopy.MUSLIMAH,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Pages of the featured books that answer the search, from the shared offline search index. */
@Composable
private fun InsideBooksSearch(needle: String, books: List<HadithBook>, titleMatches: Int, onOpenBook: (HadithBook, Int?) -> Unit) {
    var outcome by remember { mutableStateOf(BookSearchIndex.remembered(needle, books)) }
    var searching by remember { mutableStateOf(false) }
    LaunchedEffect(needle, books) {
        if (needle.length < 2) { outcome = null; searching = false; return@LaunchedEffect }
        BookSearchIndex.remembered(needle, books)?.let {
            outcome = it
            searching = false
            outcome = BookSearchIndex.withExcerpts(needle, books, it)
            return@LaunchedEffect
        }
        searching = true
        delay(250)
        val found = BookSearchIndex.search(needle, books)
        outcome = found
        searching = false
        // The pages are listed at once; their preview text follows when the network allows.
        outcome = BookSearchIndex.withExcerpts(needle, books, found)
    }
    val hits = outcome?.hits.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.animateContentSize()) {
        when {
            hits.isNotEmpty() -> {
                SectionHeading(BooksCopy.INSIDE, hits.size)
                hits.forEachIndexed { i, hit ->
                    PageHitRow(hit, i, hits.size) { onOpenBook(hit.book, hit.page) }
                }
            }
            needle.length < 2 -> Unit
            searching || outcome == null -> Row(
                Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(Spacing.sm))
                LocalizedText(BooksCopy.SEARCHING, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // With no book matching either, the list's own "no match" says it once.
            titleMatches > 0 -> LocalizedText(
                BooksCopy.NO_PAGE,
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.xs)
            )
            else -> Unit
        }
    }
}

@Composable
private fun PageHitRow(hit: BookSearchIndex.Hit, index: Int, count: Int, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val rtl = hit.snippet.isArabicScript()
    val excerpt = remember(hit, accent) {
        buildAnnotatedString {
            append(hit.snippet)
            hit.highlights.forEach { range ->
                addStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold), range.first, range.last + 1)
            }
        }
    }
    AppCard(
        onClick = onClick,
        shape = groupedShape(index, count),
        outlined = false,
        modifier = Modifier.fillMaxWidth().testTag("hadith_page_hit").tourTarget("library_books_page_hit"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${localizedUiText(hit.book.collectionName)} · ${bookLanguageLabel(hit.book.languageCode)}",
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${volumeText(hit.book)} · ${localizedUiText("Page")} ${hit.page + 1}",
                    style = MaterialTheme.typography.labelMedium, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                // A page's preview text is missing while offline; the book and page still lead there.
                if (hit.snippet.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.xs))
                    if (rtl) Text(
                        excerpt, fontFamily = ArabicFontFamily, fontSize = 17.sp, lineHeight = 30.sp, maxLines = 3,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(),
                        style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                    ) else Text(
                        excerpt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * A book's name: a featured work's name through the app's translations, any other book with the
 * library's own title, never translated word by word.
 */
@Composable
private fun bookName(book: HadithBook, work: Work?): String = work?.let { localizedUiText(it.name) } ?: book.collectionName

/** The book being read, with where you left off: one tap goes straight back to that page. */
@Composable
private fun ContinueReadingHero(book: HadithBook, work: Work?, offline: Boolean, onClick: () -> Unit) {
    val page = remember(book.id) { HadithPdfStore.lastPage(book) } + 1
    val fraction = if (book.pageCount > 0) (page.toFloat() / book.pageCount).coerceIn(0f, 1f) else null
    HeroSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("books_continue_reading").tourTarget("library_books_continue"),
        contentPadding = PaddingValues(Spacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WorkCover(work?.nativeTitle ?: book.nativeTitle ?: book.collectionName, width = 64.dp, height = 88.dp, compact = true)
            Spacer(Modifier.width(Spacing.lg))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LocalizedText(BooksCopy.CONTINUE, style = MaterialTheme.typography.labelLarge, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                Text(
                    bookName(book, work), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${bookLanguageLabel(book.languageCode)} · ${volumeText(book)}",
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Spacing.xs)) {
                    Text(
                        "${localizedUiText("Page")} $page" + (if (book.pageCount > 0) " / ${book.pageCount}" else ""),
                        style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold
                    )
                    if (offline) {
                        Spacer(Modifier.width(Spacing.sm))
                        Icon(Icons.Default.DownloadDone, contentDescription = localizedUiText("Offline"),
                            tint = GoldAccent, modifier = Modifier.size(16.dp))
                    }
                }
                if (fraction != null) LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs).height(5.dp).clip(RoundedCornerShape(50)),
                    color = GoldAccent, trackColor = Color.White.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round, gapSize = 0.dp, drawStopIndicator = {}
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Surface(shape = CircleShape, color = GoldAccent, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = localizedUiText("Resume"),
                        tint = HeroGradientBottom, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun RecentBookRow(book: HadithBook, work: Work?, index: Int, count: Int, offline: Boolean, onClick: () -> Unit) {
    val page = remember(book.id) { HadithPdfStore.lastPage(book) } + 1
    AppCard(
        onClick = onClick,
        shape = groupedShape(index, count),
        outlined = false,
        modifier = Modifier.fillMaxWidth().testTag("books_recent_${book.id}").tourTarget("library_books_recent"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WorkCover(work?.nativeTitle ?: book.nativeTitle ?: book.collectionName, width = 34.dp, height = 46.dp, compact = true)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(bookName(book, work), style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${bookLanguageLabel(book.languageCode)} · ${volumeText(book)} · ${localizedUiText("Page")} $page",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            if (offline) Icon(Icons.Default.DownloadDone, localizedUiText("Offline"),
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** A classical work as a book on a shelf: its cover, name, author and the languages it is in. */
@Composable
private fun WorkCoverCard(work: Work, offlineCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val a11y = listOfNotNull(
        localizedUiText(work.name), work.author.takeIf { it.isNotBlank() },
        work.languages.joinToString(", ") { bookLanguageLabel(it) }
    ).joinToString(", ")
    AppCard(
        onClick = onClick,
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = a11y }
            .testTag("hadith_collection_${work.id}").tourTarget("library_books_cover"),
        contentPadding = PaddingValues(Spacing.sm)
    ) {
        WorkCover(work.nativeTitle, modifier = Modifier.fillMaxWidth().aspectRatio(0.78f))
        Column(Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.sm), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LocalizedText(work.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (work.author.isNotBlank()) Text(
                work.author.substringBefore(" · "), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.weight(1f, fill = false))
        WorkMetaRow(work, offlineCount, Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.xs))
    }
}

/** A Ladies / Muslimah title (or a search result) as one compact row. */
@Composable
private fun WorkRow(work: Work, index: Int, count: Int, offlineCount: Int, onClick: () -> Unit) {
    AppCard(
        onClick = onClick,
        shape = groupedShape(index, count),
        outlined = false,
        modifier = Modifier.fillMaxWidth().testTag("hadith_collection_${work.id}").tourTarget("library_books_row"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WorkCover(work.nativeTitle, width = 46.dp, height = 62.dp, compact = true,
                accent = if (work.shelf == Shelf.MUSLIMAH) MuslimahAccent else null)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LocalizedText(work.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (work.nativeTitle != work.name) Text(
                    work.nativeTitle, style = MaterialTheme.typography.bodyMedium,
                    fontFamily = if (work.nativeTitle.isArabicScript()) ArabicFontFamily else null,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                WorkMetaRow(work, offlineCount)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Languages in their own script, and how many files are saved offline. */
@Composable
private fun WorkMetaRow(work: Work, offlineCount: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Default.Translate, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Text(
            work.languages.joinToString(" · ") { bookLanguageLabel(it) },
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
        )
        if (offlineCount > 0) TonalPill(
            text = "$offlineCount", icon = Icons.Default.DownloadDone,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/** Back arrow, title and subtitle; [localize] is false for a book's own title and author. */
@Composable
private fun LibraryHeader(
    title: String,
    subtitle: String?,
    localize: Boolean,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 4.dp, end = 4.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) BackIconButton(onBack) else Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(end = 4.dp)) {
            Text(
                text = if (localize) localizedUiText(title) else title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() }
            )
            if (!subtitle.isNullOrBlank()) Text(
                text = if (localize) localizedUiText(subtitle) else subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        actions()
    }
}

/** A compact picker button: what is chosen now, and a chevron saying it opens a list. */
@Composable
private fun PickerPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val container = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val content = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = CircleShape,
        color = container,
        contentColor = content,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = description, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SortPicker(sort: WorldSort, onSort: (WorldSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        PickerPill(
            icon = Icons.AutoMirrored.Filled.Sort,
            label = localizedUiText(sort.label),
            description = localizedUiText(BooksCopy.SORT),
            active = sort != WorldSort.POPULAR,
            modifier = Modifier.widthIn(max = 220.dp).testTag("world_library_sort").tourTarget("library_world_sort"),
            onClick = { AppAnalytics.interaction(SCREEN, "open_sort_menu", "tap"); open = true }
        )
        DropdownMenu(expanded = open, onDismissRequest = { AppAnalytics.interaction(SCREEN, "dismiss_sort_menu", "dismiss"); open = false }) {
            WorldSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { LocalizedText(option.label, fontWeight = if (option == sort) FontWeight.Bold else FontWeight.Normal) },
                    leadingIcon = {
                        if (option == sort) Icon(Icons.Default.Check, null, tint = HadithAccent)
                        else Spacer(Modifier.size(24.dp))
                    },
                    onClick = { open = false; onSort(option) },
                    modifier = Modifier.testTag("world_library_sort_${option.name.lowercase()}")
                )
            }
        }
    }
}

/** One row of a picker sheet: a name, an optional count and a tick on the chosen one. */
@Composable
private fun SheetOption(label: String, count: String?, active: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) HadithAccent else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (count != null) {
            TonalPill(count)
            Spacer(Modifier.width(Spacing.sm))
        }
        if (active) Icon(Icons.Default.Check, null, tint = HadithAccent)
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(
        localizedUiText(text),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).semantics { heading() }
    )
}

/** Every language of the library in one sheet; names are shown in their own script. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSheet(
    selected: String,
    options: List<WorldLibrary.Language>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        SheetTitle(BooksCopy.LANGUAGE)
        LazyColumn(
            Modifier.fillMaxWidth().testTag("world_library_languages"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item(key = HadithCatalog.ALL_LANGUAGES) {
                SheetOption(localizedUiText(BooksCopy.ALL_LANGUAGES), null, selected == HadithCatalog.ALL_LANGUAGES) {
                    onSelect(HadithCatalog.ALL_LANGUAGES)
                }
            }
            // A list that names one code twice would break the list's keys, so each code shows once.
            items(options.distinctBy { it.code }, key = { it.code }) { language ->
                SheetOption(language.name, null, language.code == selected) { onSelect(language.code) }
            }
        }
    }
}

/** The library's categories with their book counts; names keep the library's own words. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySheet(
    selected: Int?,
    categories: List<WorldCategory>,
    onDismiss: () -> Unit,
    onSelect: (Int?) -> Unit
) {
    val numbers = rememberNumberFormat()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        SheetTitle(BooksCopy.CATEGORY)
        LazyColumn(
            Modifier.fillMaxWidth().testTag("world_library_categories"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item(key = "all") {
                SheetOption(localizedUiText(BooksCopy.ALL_CATEGORIES), null, selected == null) { onSelect(null) }
            }
            items(categories.distinctBy { it.id }, key = { it.id }) { category ->
                SheetOption(category.name, numbers.format(category.count), category.id == selected) { onSelect(category.id) }
            }
        }
    }
}

/** Shown above the placeholders while the book list downloads for the first time. */
@Composable
private fun IndexLoadingNote() {
    Row(
        Modifier.fillMaxWidth().testTag("world_library_index_loading"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(Modifier.size(18.dp), color = HadithAccent, strokeWidth = 2.dp)
        Spacer(Modifier.width(Spacing.sm))
        LocalizedText(
            BooksCopy.INDEX_LOADING,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A title in its own script, laid out right to left when it is Arabic, Urdu or Sindhi. */
@Composable
private fun NativeTitle(text: String, style: androidx.compose.ui.text.TextStyle, maxLines: Int) {
    Text(
        text,
        style = style.copy(textDirection = TextDirection.Content),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Recent searches as tappable chips; the trash button forgets them all. */
@Composable
private fun RecentSearches(recent: List<String>, onPick: (String) -> Unit, onClear: () -> Unit) {
    Row(Modifier.fillMaxWidth().tourTarget("library_world_recent"), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Spacing.sm))
        LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            items(recent, key = { it }) { text ->
                SuggestionChip(
                    onClick = { onPick(text) },
                    label = { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    shape = CircleShape
                )
            }
        }
        IconButton(onClick = onClear, modifier = Modifier.testTag("world_library_clear_recent").tourTarget("library_world_clear_recent")) {
            Icon(Icons.Default.DeleteSweep, contentDescription = localizedUiText(BooksCopy.CLEAR),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeading(title: String, count: Int?) {
    val numbers = rememberNumberFormat()
    Row(Modifier.fillMaxWidth().padding(top = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        LocalizedText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f, fill = false).semantics { heading() }
        )
        if (count != null) {
            Spacer(Modifier.width(Spacing.sm))
            TonalPill(numbers.format(count))
        }
    }
}

@Composable
private fun rememberNumberFormat(): NumberFormat {
    val locale = LocalAppLocale.current
    return remember(locale) { NumberFormat.getIntegerInstance(locale) }
}

/** A drawn cover: a tone picked from the book's id, the geometric lattice and the title's first letter. */
@Composable
private fun BookCover(seed: String, title: String, width: Dp, height: Dp, modifier: Modifier = Modifier) {
    val (top, bottom) = CoverTones[(seed.hashCode() and Int.MAX_VALUE) % CoverTones.size]
    val initial = title.trim().firstOrNull { it.isLetterOrDigit() }?.uppercase()
    Box(
        modifier.size(width, height)
            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 12.dp, bottomEnd = 12.dp))
            .background(Brush.linearGradient(listOf(top, bottom)))
            .islamicPattern(HadithGold.copy(alpha = .14f), cell = width / 3),
        contentAlignment = Alignment.Center
    ) {
        // The spine: a slim darker band on the binding side.
        Box(Modifier.align(Alignment.CenterStart).fillMaxHeight().width(width / 12).background(Color.Black.copy(alpha = .18f)))
        if (initial != null) Text(
            initial,
            color = HadithGold,
            fontWeight = FontWeight.Bold,
            fontSize = (width.value * 0.42f).sp,
            textAlign = TextAlign.Center
        ) else Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = HadithGold, modifier = Modifier.size(width / 2))
    }
}

/** Titles and authors keep the library's own words and are never run through the app's translations. */
@Composable
private fun WorldItemCard(item: WorldItem, offlineCount: Int, onClick: () -> Unit) {
    val numbers = rememberNumberFormat()
    AppCard(
        modifier = Modifier.fillMaxWidth().testTag("world_library_item").tourTarget("library_world_item"),
        onClick = onClick,
        contentPadding = PaddingValues(Spacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BookCover(item.identifier, item.title, 52.dp, 70.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                item.nativeTitle?.takeIf { it.isNotBlank() && it != item.title }?.let {
                    NativeTitle(it, MaterialTheme.typography.bodyMedium, maxLines = 1)
                }
                val author = ProviderNames.hideIn(item.creator)
                if (author.isNotBlank()) Text(
                    author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorldLibrary.languageName(item.languageCode)?.let { TonalPill(it) }
                    if (item.downloads > 0) MetaIcon(Icons.Default.Visibility, numbers.format(item.downloads))
                    if (offlineCount > 0) MetaIcon(Icons.Default.DownloadDone, numbers.format(offlineCount), HadithOffline)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetaIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(3.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1)
    }
}

/** A downloaded book on the offline shelf: tap to read, long-press to remove. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShelfBook(book: HadithBook, onOpen: () -> Unit, onRemove: () -> Unit) {
    val press = remember { MutableInteractionSource() }
    val lastPage = remember(book.id) { HadithPdfStore.lastPage(book) }
    Column(
        Modifier.width(104.dp)
            .pressScale(press)
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                interactionSource = press,
                indication = LocalIndication.current,
                role = Role.Button,
                onLongClickLabel = localizedUiText(BooksCopy.REMOVE_TITLE),
                onLongClick = onRemove,
                onClick = onOpen
            )
            .padding(4.dp)
            .testTag("world_library_saved_${book.id}").tourTarget("library_world_saved_book"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box {
            BookCover(book.slug, book.collectionName, 96.dp, 128.dp)
            if (lastPage > 0) Row(
                Modifier.align(Alignment.BottomEnd).padding(6.dp)
                    .background(Color.Black.copy(alpha = .55f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bookmark, null, tint = HadithGold, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(2.dp))
                Text((lastPage + 1).toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            if (book.volumeCount == 1) book.romanTitle else book.volumeTitle ?: book.romanTitle,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** A static placeholder in the shape of a result card; no endless animation while the list loads. */
@Composable
private fun SkeletonCard() {
    val block = MaterialTheme.colorScheme.surfaceVariant
    AppCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp, 70.dp).clip(RoundedCornerShape(8.dp)).background(block))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth(0.85f).height(14.dp).clip(CircleShape).background(block))
                Box(Modifier.fillMaxWidth(0.5f).height(10.dp).clip(CircleShape).background(block))
                Box(Modifier.width(64.dp).height(18.dp).clip(CircleShape).background(block))
            }
        }
    }
}

// endregion

// region Book detail

/** A book of the full list: what it is, one clear Read action, and its file with download state. */
@Composable
private fun WorldBookDetail(
    item: WorldItem,
    volumes: Result<List<HadithBook>?>?,
    saved: List<HadithBook>,
    progress: Map<String, Float>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpen: (HadithBook) -> Unit,
    onDownload: (HadithBook) -> Unit,
    onDelete: (HadithBook) -> Unit
) {
    TourScreen("library_world_detail")
    val numbers = rememberNumberFormat()
    val found = volumes?.getOrNull()
    // The publisher's server takes seconds to answer each request, so the volume the Read button
    // opens starts loading its first pages now, while this page is read. A saved copy needs nothing.
    LaunchedEffect(found) {
        val list = found?.takeIf { it.isNotEmpty() } ?: return@LaunchedEffect
        val target = list.filter { HadithPdfStore.lastOpened(it) > 0L }.maxByOrNull { HadithPdfStore.lastOpened(it) }
            ?: list.first()
        if (saved.none { it.id == target.id }) HadithPdfStore.prefetch(target)
    }
    Column(Modifier.fillMaxSize()) {
        LibraryHeader(title = item.title, subtitle = null, localize = false, onBack = onBack)
        LazyColumn(
            Modifier.fillMaxSize().testTag("world_library_volumes"),
            contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.xs, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(key = "about") {
                Row(Modifier.tourTarget("library_world_detail_about"), verticalAlignment = Alignment.Top) {
                    BookCover(item.identifier, item.title, 88.dp, 118.dp)
                    Spacer(Modifier.width(Spacing.lg))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.nativeTitle?.takeIf { it.isNotBlank() && it != item.title }?.let {
                            NativeTitle(it, MaterialTheme.typography.titleMedium, maxLines = 3)
                        }
                        val author = ProviderNames.hideIn(item.creator)
                        if (author.isNotBlank()) Text(
                            author,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                            WorldLibrary.languageName(item.languageCode)?.let { TonalPill(it) }
                            if (item.downloads > 0) MetaIcon(Icons.Default.Visibility, numbers.format(item.downloads))
                        }
                        if (found != null && found.size > 1) MetaIcon(Icons.AutoMirrored.Filled.LibraryBooks, numbers.format(found.size))
                    }
                }
            }
            when {
                volumes == null -> item(key = "loading") { LoadingState(message = BooksCopy.LOADING) }
                volumes.isFailure -> item(key = "error") {
                    ErrorState(onRetry = onRetry, title = BooksCopy.UNREACHABLE, retryText = BooksCopy.RETRY)
                }
                found == null -> item(key = "removed") {
                    EmptyState(title = BooksCopy.REMOVED_ITEM, icon = Icons.Default.CloudOff)
                }
                found.isEmpty() -> item(key = "nopdf") {
                    EmptyState(title = BooksCopy.NO_PDF, icon = Icons.Default.Description)
                }
                else -> {
                    // One clear action: carry on with the volume read last, or start with the first.
                    val resume = found.filter { HadithPdfStore.lastOpened(it) > 0L }.maxByOrNull { HadithPdfStore.lastOpened(it) }
                    item(key = "read") {
                        Button(
                            onClick = {
                                AppAnalytics.interaction(SCREEN, if (resume != null) "continue_reading" else "read", "tap", item = item.identifier)
                                onOpen(resume ?: found.first())
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("world_library_read").tourTarget("library_world_read"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = HadithGreen, contentColor = Color.White)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(Spacing.sm))
                            LocalizedText(if (resume != null) BooksCopy.CONTINUE else BooksCopy.READ, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    items(found, key = HadithBook::id) { book ->
                        Box(Modifier.tourTarget("library_world_volume")) {
                            VolumeRow(
                                book = book,
                                title = if (book.volumeCount == 1) book.romanTitle else book.volumeTitle ?: book.romanTitle,
                                downloaded = saved.any { it.id == book.id },
                                progress = progress[book.id],
                                onOpen = { onOpen(book) },
                                onDownload = { onDownload(book) },
                                onDelete = { onDelete(book) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// endregion

// region Work page

/** One featured work: its cover, the edition language, and every volume with its download state. */
@Composable
private fun BookWorkPage(
    work: Work,
    listState: LazyListState,
    prefs: android.content.SharedPreferences,
    onBack: () -> Unit,
    onOpen: (HadithBook) -> Unit
) {
    TourScreen("library_books_work")
    val appLanguage = LocalAppLanguage.current
    val progress by HadithPdfStore.progress.collectAsState()
    val changes by HadithPdfStore.changes.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val downloadFailedText = localizedUiText("Download failed. Check your internet connection.")
    val retryText = localizedUiText("Retry")
    val prefKey = "lang_${work.id}"
    var language by remember(work.id) {
        mutableStateOf(IslamicBooksCatalog.preferredLanguage(work, prefs.getString(prefKey, null), appLanguage))
    }
    val editions = remember(work.id, language) { work.editions(language) }
    val offline = remember(changes, editions) { editions.filter(HadithPdfStore::isDownloaded).map { it.id }.toSet() }
    // The volume read most recently in this language, so the main button resumes rather than restarts.
    val resume = remember(changes, editions) {
        editions.filter { HadithPdfStore.lastOpened(it) > 0L }.maxByOrNull { HadithPdfStore.lastOpened(it) }
    }
    // Same warm-up as the World Library detail: only the volume Read opens, never the whole list.
    val warmTarget = resume ?: editions.firstOrNull()
    LaunchedEffect(warmTarget?.id) {
        val target = warmTarget ?: return@LaunchedEffect
        if (!HadithPdfStore.isDownloaded(target)) HadithPdfStore.prefetch(target)
    }
    var bulkBusy by remember(work.id) { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<HadithBook?>(null) }

    // Through the library, so a download joins the offline shelf and leaves with a book the library removes.
    fun download(book: HadithBook) {
        scope.launch {
            while (WorldLibrary.download(book).isFailure &&
                snackbar.showSnackbar(downloadFailedText, actionLabel = retryText) == SnackbarResult.ActionPerformed
            ) Unit
        }
    }

    fun downloadAll() {
        scope.launch {
            bulkBusy = true
            val failed = editions.filterNot { it.id in offline }.count { WorldLibrary.download(it).isFailure }
            bulkBusy = false
            snackbar.showSnackbar(
                if (failed == 0) localizeUiText("All volumes are available offline.", appLanguage)
                else localizeUiTemplate("{count} downloads failed; tap again to resume.", appLanguage, mapOf("count" to failed.toString()))
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().testTag("books_work_${work.id}"),
            contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(key = "header") {
                ScreenHeader(
                    title = work.name,
                    subtitle = work.author.takeIf { it.isNotBlank() },
                    localizeSubtitle = false,
                    onBack = onBack,
                    backTestTag = "books_work_back"
                )
            }
            item(key = "hero") {
                WorkHero(
                    work = work,
                    language = language,
                    volumes = editions.size,
                    offlineCount = offline.size,
                    resume = resume,
                    bulkBusy = bulkBusy,
                    onRead = {
                        val target = resume ?: editions.first()
                        AppAnalytics.interaction(SCREEN, if (resume != null) "continue_reading" else "open_book", "tap", item = target.id)
                        onOpen(target)
                    },
                    onDownloadAll = {
                        AppAnalytics.interaction(SCREEN, "download_all", "tap", item = work.id)
                        downloadAll()
                    }
                )
            }
            if (work.languages.size > 1) item(key = "languages") {
                EditionLanguagePicker(work.languages, language) {
                    AppAnalytics.interaction(SCREEN, "select_edition_language", "tap", item = it)
                    language = it
                    prefs.edit().putString(prefKey, it).apply()
                }
            }
            itemsIndexed(editions, key = { _, book -> book.id }) { index, book ->
                VolumeItem(
                    book = book,
                    index = index,
                    count = editions.size,
                    downloaded = book.id in offline,
                    progress = progress[book.id],
                    current = book.id == resume?.id,
                    onOpen = { AppAnalytics.interaction(SCREEN, "open_book", "tap", item = book.id); onOpen(book) },
                    onDownload = { AppAnalytics.interaction(SCREEN, "download_book", "tap", item = book.id); download(book) },
                    onDelete = { AppAnalytics.interaction(SCREEN, "open_remove_volume", "tap"); confirmDelete = book }
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(Spacing.lg))
    }

    confirmDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { AppAnalytics.interaction(SCREEN, "dismiss_remove_download", "dismiss"); confirmDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { LocalizedText("Remove download?") },
            text = { LocalizedText(localizedUiTemplate("{title} will remain available online.", "title" to book.romanTitle)) },
            confirmButton = {
                TextButton(onClick = {
                    AppAnalytics.interaction(SCREEN, "confirm_remove_download", "tap")
                    WorldLibrary.delete(book)
                    confirmDelete = null
                }) { LocalizedText("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { AppAnalytics.interaction(SCREEN, "cancel_remove_download", "tap"); confirmDelete = null }) {
                    LocalizedText("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WorkHero(
    work: Work,
    language: String,
    volumes: Int,
    offlineCount: Int,
    resume: HadithBook?,
    bulkBusy: Boolean,
    onRead: () -> Unit,
    onDownloadAll: () -> Unit
) {
    val allOffline = volumes > 0 && offlineCount == volumes
    HeroSurface(modifier = Modifier.fillMaxWidth().tourTarget("library_books_work_hero"), contentPadding = PaddingValues(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WorkCover(work.nativeTitle, width = 92.dp, height = 126.dp,
                accent = if (work.shelf == Shelf.MUSLIMAH) MuslimahAccent else null)
            Spacer(Modifier.width(Spacing.lg))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    work.nativeTitle,
                    fontFamily = if (work.nativeTitle.isArabicScript()) ArabicFontFamily else null,
                    style = MaterialTheme.typography.titleLarge, color = Color.White,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text(bookLanguageLabel(language), style = MaterialTheme.typography.labelLarge, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    TonalPill(
                        "$volumes", icon = Icons.Default.CollectionsBookmark,
                        containerColor = Color.White.copy(alpha = 0.14f), contentColor = Color.White
                    )
                    if (offlineCount > 0) TonalPill(
                        "$offlineCount", icon = Icons.Default.DownloadDone,
                        containerColor = GoldAccent.copy(alpha = 0.22f), contentColor = GoldAccent
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onRead,
                enabled = volumes > 0,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("books_read").tourTarget("library_books_read"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = HeroGradientBottom)
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, null, Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
                LocalizedText(if (resume != null) "Resume" else "Read", fontWeight = FontWeight.Bold, maxLines = 1)
            }
            if (allOffline) {
                Row(
                    Modifier.tourTarget("library_books_all_offline").heightIn(min = 48.dp).clip(RoundedCornerShape(50))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DownloadDone, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Offline", color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
            } else if (volumes > 1) {
                OutlinedButton(
                    onClick = onDownloadAll,
                    enabled = !bulkBusy,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("books_download_all").tourTarget("library_books_download_all"),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White, disabledContentColor = Color.White.copy(alpha = 0.6f))
                ) {
                    if (bulkBusy) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    else Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Download all", maxLines = 1)
                }
            }
        }
    }
}

/** Edition languages named in their own script; a few fit a segmented row, more scroll. */
@Composable
private fun EditionLanguagePicker(languages: List<String>, selected: String, onSelect: (String) -> Unit) {
    if (languages.size <= 3) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("books_language_picker").tourTarget("library_books_language")) {
            languages.forEachIndexed { index, code ->
                SegmentedButton(
                    selected = code == selected,
                    onClick = { onSelect(code) },
                    shape = SegmentedButtonDefaults.itemShape(index, languages.size),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("books_language_$code")
                ) { Text(bookLanguageLabel(code), maxLines = 1) }
            }
        }
    } else {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).testTag("books_language_picker").tourTarget("library_books_language"),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            languages.forEach { code ->
                FilterChip(
                    selected = code == selected,
                    onClick = { onSelect(code) },
                    label = { Text(bookLanguageLabel(code)) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.heightIn(min = 40.dp).testTag("books_language_$code")
                )
            }
        }
    }
}

/** One file of the work: what it is, where you stopped, and its download state with one action. */
@Composable
private fun VolumeItem(
    book: HadithBook,
    index: Int,
    count: Int,
    downloaded: Boolean,
    progress: Float?,
    current: Boolean,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val isOnline by rememberIsOnline()
    val started = remember(book.id) { HadithPdfStore.lastOpened(book) > 0L }
    val lastPage = remember(book.id) { HadithPdfStore.lastPage(book) } + 1
    val status = buildList {
        if (started) add("${localizedUiText("Page")} $lastPage")
        if (book.pageCount > 0) add("${book.pageCount} ${localizedUiText("Pages")}")
    }.joinToString(" · ")
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onOpen,
        interactionSource = interaction,
        shape = groupedShape(index, count),
        color = if (current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = if (current) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null,
        modifier = Modifier.fillMaxWidth().pressScale(interaction, 0.98f).testTag("hadith_book_${book.id}").tourTarget("library_books_volume")
    ) {
        Row(
            Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp).heightIn(min = 52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(HeroGradientTop, HeroGradientBottom))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (book.volumeCount > 1) "${book.volume}" else "1",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GoldAccent
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(volumeText(book), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                when {
                    progress != null -> LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, end = Spacing.sm).height(4.dp).clip(RoundedCornerShape(50)),
                        strokeCap = StrokeCap.Round, gapSize = 0.dp, drawStopIndicator = {}
                    )
                    status.isNotEmpty() -> Text(status, style = MaterialTheme.typography.bodySmall,
                        color = if (started) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            when {
                progress != null -> Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                }
                downloaded -> IconButton(onClick = onDelete, modifier = Modifier.testTag("books_remove_${book.id}").tourTarget("library_books_volume_remove")) {
                    Icon(Icons.Default.DownloadDone, contentDescription = localizedUiText("Remove download?"),
                        tint = MaterialTheme.colorScheme.primary)
                }
                else -> IconButton(onClick = onDownload, enabled = isOnline, modifier = Modifier.testTag("books_download_${book.id}").tourTarget("library_books_volume_download")) {
                    Icon(Icons.Default.Download, contentDescription = localizedUiText("Download"))
                }
            }
        }
    }
}

// endregion

// region Shared pieces

/**
 * A drawn cover for a featured work: emerald binding, geometric pattern, a gold frame and the
 * title in its own script. [accent] tints the binding (the Muslimah shelf uses a soft rose).
 */
@Composable
private fun WorkCover(
    title: String,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null,
    compact: Boolean = false,
    accent: Color? = null
) {
    val top = accent?.let { androidx.compose.ui.graphics.lerp(it, HeroGradientTop, 0.35f) } ?: HeroGradientTop
    val bottom = accent?.let { androidx.compose.ui.graphics.lerp(it, HeroGradientBottom, 0.6f) } ?: HeroGradientBottom
    val sized = if (width != null && height != null) modifier.size(width, height) else modifier
    val corner = if (compact) 6.dp else 10.dp
    Box(
        sized.clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = corner, bottomEnd = corner))
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .islamicPattern(GoldAccent.copy(alpha = 0.12f), cell = if (compact) 14.dp else 26.dp),
        contentAlignment = Alignment.Center
    ) {
        // The spine: a darker band on the binding edge.
        Box(Modifier.align(Alignment.CenterStart).fillMaxHeight().width(if (compact) 4.dp else 7.dp).background(Color.Black.copy(alpha = 0.22f)))
        if (!compact) {
            Box(
                Modifier.fillMaxSize().padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
                    .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            )
        }
        Text(
            title,
            fontFamily = if (title.isArabicScript()) ArabicFontFamily else null,
            color = GoldAccent,
            textAlign = TextAlign.Center,
            fontSize = if (compact) 9.sp else 20.sp,
            lineHeight = if (compact) 13.sp else 32.sp,
            maxLines = if (compact) 3 else 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = if (compact) 6.dp else 22.dp, end = if (compact) 3.dp else 16.dp)
        )
    }
}

private fun String.isArabicScript(): Boolean = any { it in '؀'..'ۿ' || it in 'ݐ'..'ݿ' }

/** An edition's language named in its own script, so no translation is needed to pick it. */
internal fun bookLanguageLabel(code: String): String = when (code) {
    "ar" -> "العربية"
    "hi" -> "हिन्दी"
    "en" -> "English"
    "ur" -> "اردو"
    "fa" -> "فارسی"
    "kn" -> "ಕನ್ನಡ"
    "sd" -> "سنڌي"
    "bn" -> "বাংলা"
    "gu" -> "ગુજરાતી"
    "te" -> "తెలుగు"
    "pt" -> "Português"
    else -> WorldLibrary.languageName(code) ?: code.uppercase()
}

/** "Jild 1 · Part A", "Part 3 of 20", or "Complete edition": what this file holds. */
@Composable
private fun volumeText(book: HadithBook): String = book.volumeTitle?.let { localizedUiText(it) }
    ?: if (book.volumeCount == 1) localizedUiText("Complete edition")
    else localizedUiTemplate("Volume {volume} of {total}", "volume" to book.volume, "total" to book.volumeCount)

// endregion
