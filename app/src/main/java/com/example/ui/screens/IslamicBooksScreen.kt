package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.analytics.AppAnalytics
import com.example.hadith.BookSearchIndex
import com.example.hadith.HadithBook
import com.example.hadith.HadithPdfStore
import com.example.hadith.HadithRepository
import com.example.hadith.IslamicBooksCatalog
import com.example.hadith.IslamicBooksCatalog.Shelf
import com.example.hadith.IslamicBooksCatalog.Work
import com.example.hadith.WorldLibrary
import com.example.ui.components.AppCard
import com.example.ui.components.AppSearchField
import com.example.ui.components.EmptyState
import com.example.ui.components.HeroSurface
import com.example.ui.components.IconBadge
import com.example.ui.components.NeedsInternetChip
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.components.groupedShape
import com.example.ui.components.islamicPattern
import com.example.ui.components.pressScale
import com.example.ui.components.rememberIsOnline
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HeroGradientBottom
import com.example.ui.theme.HeroGradientTop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * Islamic Books: a bookshelf of classical Ahle Sunnat works and the Ladies / Muslimah collection.
 * Home shows what you were reading and the shelves; a work opens on its own page with every
 * language edition and volume. Pages are the publishers' original PDFs and are never modified.
 */

/** Wording of the source policy, shown from the header's info button. */
internal val BooksSourcePolicy =
    "Bahar-e-Shariat in Urdu comes from Dawat-e-Islami's library; the other editions are scanned books hosted on the Internet Archive. " +
        "Ladies / Muslimah titles and their publisher-listed language editions come from Dawat-e-Islami's library. " +
        "Only verified editions are listed; worldwide coverage is not exhaustive. " +
        "Downloads are saved byte-for-byte and PDF pages are never translated, rewritten, reordered, or modified."

private const val BOOKS_PREFS = "islamic_books"
private const val KEY_SHELF = "shelf"
private val MuslimahAccent = Color(0xFF9A6286)

/** Which shelves the home page shows: everything, or only the Ladies / Muslimah titles. */
private enum class BooksFilter { ALL, MUSLIMAH }

/** Bahar-e-Shariat, Anwar-e-Shariat, Shawahid-un-Nubuwwat, Maarij-un-Nubuwwat and the Muslimah shelf. */
@Composable
fun IslamicBooksScreen(
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    com.example.core.analytics.AnalyticsScreen("islamic_books_screen")

    val context = LocalContext.current
    remember { HadithRepository.init(context) }
    val prefs = remember(context) { context.getSharedPreferences(BOOKS_PREFS, Context.MODE_PRIVATE) }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    var openPage by rememberSaveable { mutableStateOf<Int?>(null) }
    var workId by rememberSaveable { mutableStateOf<String?>(null) }
    var filter by rememberSaveable {
        mutableStateOf(runCatching { BooksFilter.valueOf(prefs.getString(KEY_SHELF, null) ?: "") }.getOrDefault(BooksFilter.ALL))
    }
    var query by rememberSaveable { mutableStateOf("") }
    // Hoisted so the shelf and a work page keep their scroll positions while a book is open.
    val homeState = rememberLazyListState()
    val workState = rememberLazyListState()
    // Every id is looked up in the full catalogue, so a Muslimah title opens whichever shelf is shown.
    val openBook = IslamicBooksCatalog.book(openId)
    val work = IslamicBooksCatalog.work(workId)

    LaunchedEffect(openBook) { if (openBook == null) onImmersiveChange(false) }

    if (openBook != null) {
        BackHandler {
            AppAnalytics.interaction("hadith_screen", "reader_system_back", "tap")
            if (isImmersive) onImmersiveChange(false) else openId = null
        }
        HadithPdfReader(
            book = openBook,
            startPage = openPage,
            isImmersive = isImmersive,
            onImmersiveChange = onImmersiveChange,
            onBack = {
                AppAnalytics.interaction("hadith_screen", "close_reader", "back")
                onImmersiveChange(false)
                openId = null
            }
        )
        return
    }

    val openAt: (HadithBook, Int?) -> Unit = { book, page -> openPage = page; openId = book.id }
    BackHandler(enabled = work != null) {
        AppAnalytics.interaction("islamic_books_screen", "close_work_system_back", "back")
        workId = null
    }

    AnimatedContent(
        targetState = work,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(90)) },
        label = "books_page"
    ) { shown ->
        if (shown != null) {
            BookWorkPage(
                work = shown,
                listState = workState,
                prefs = prefs,
                onBack = { AppAnalytics.interaction("islamic_books_screen", "close_work", "back"); workId = null },
                onOpen = { book -> openAt(book, null) }
            )
        } else {
            BooksHome(
                listState = homeState,
                filter = filter,
                onFilter = {
                    filter = it
                    prefs.edit().putString(KEY_SHELF, it.name).apply()
                },
                query = query,
                onQuery = { query = it },
                onOpenWork = { opened ->
                    AppAnalytics.interaction("hadith_screen", "open_collection", "tap", item = opened.id)
                    // A different work starts at its top; returning from the reader keeps the place.
                    workState.requestScrollToItem(0)
                    workId = opened.id
                },
                onOpenBook = openAt,
                onBack = onBack
            )
        }
    }
}

// region Home

@Composable
private fun BooksHome(
    listState: LazyListState,
    filter: BooksFilter,
    onFilter: (BooksFilter) -> Unit,
    query: String,
    onQuery: (String) -> Unit,
    onOpenWork: (Work) -> Unit,
    onOpenBook: (HadithBook, Int?) -> Unit,
    onBack: (() -> Unit)?
) {
    val changes by HadithPdfStore.changes.collectAsState()
    var showSource by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    com.example.core.analytics.AnalyticsSearch("islamic_books_screen", query)

    val works = IslamicBooksCatalog.works
    val offline = remember(changes) {
        IslamicBooksCatalog.books.filter(HadithPdfStore::isDownloaded).map { it.id }.toSet()
    }
    // Most recently opened first; only books actually opened count as "reading".
    val recent = remember(changes) {
        IslamicBooksCatalog.books.map { it to HadithPdfStore.lastOpened(it) }
            .filter { it.second > 0L }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(4)
    }
    val needle = query.trim()
    val matches = remember(needle) {
        if (needle.isEmpty()) emptyList() else works.filter { it.matches(needle) }
    }
    val classical = works.filter { it.shelf == Shelf.CLASSICAL }
    val muslimah = works.filter { it.shelf == Shelf.MUSLIMAH }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().testTag("islamic_books_list"),
        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item(key = "header") {
            ScreenHeader(
                title = "Islamic Books",
                subtitle = "Ahle Sunnat books and Muslimah library",
                onBack = onBack?.let { back -> { AppAnalytics.interaction("hadith_screen", "library_back", "back"); back() } },
                leading = { IconBadge(Icons.Default.CollectionsBookmark, GoldAccent, size = 40.dp, iconSize = 22.dp) }
            ) {
                IconButton(onClick = { AppAnalytics.interaction("hadith_screen", "open_source_info", "tap"); showSource = true }) {
                    Icon(Icons.Default.Info, contentDescription = localizedUiText("Source and translation policy"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item(key = "search") {
            AppSearchField(
                query = query,
                onQueryChange = onQuery,
                placeholder = "Search a book or a topic…",
                modifier = Modifier.testTag("hadith_search"),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
        }

        if (needle.isNotEmpty()) {
            searchResults(needle, matches, offline, onOpenWork, onOpenBook, onClear = { onQuery("") })
            return@LazyColumn
        }

        item(key = "shelves") {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                BooksFilter.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = filter == option,
                        onClick = {
                            AppAnalytics.interaction(
                                "hadith_screen",
                                if (option == BooksFilter.ALL) "filter_all_books" else "filter_muslimah_books", "tap"
                            )
                            onFilter(option)
                        },
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
                            .then(if (option == BooksFilter.MUSLIMAH) Modifier.testTag("muslimah_books_filter") else Modifier.testTag("all_books_filter"))
                    ) {
                        if (option == BooksFilter.ALL) LocalizedText("All books", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        else LocalizedText("Ladies / Muslimah", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        val shownRecent = recent.filter { filter == BooksFilter.ALL || it.collectionId in muslimah.map(Work::id) }
        if (shownRecent.isNotEmpty()) {
            item(key = "continue") {
                ContinueReadingHero(shownRecent.first(), offline = shownRecent.first().id in offline) {
                    AppAnalytics.interaction("hadith_screen", "continue_reading", "tap", item = shownRecent.first().id)
                    onOpenBook(shownRecent.first(), null)
                }
            }
            if (shownRecent.size > 1) {
                itemsIndexed(shownRecent.drop(1), key = { _, book -> "recent-${book.id}" }) { index, book ->
                    RecentBookRow(book, index, shownRecent.size - 1, offline = book.id in offline) {
                        AppAnalytics.interaction("hadith_screen", "continue_reading", "tap", item = book.id)
                        onOpenBook(book, null)
                    }
                }
            }
        }

        if (filter == BooksFilter.ALL) {
            item(key = "classical_title") { ShelfTitle("Books", classical.size) }
            // Two covers per row: the four classical works fill the first screen.
            items(classical.chunked(2), key = { row -> "classical-" + row.joinToString { it.id } }) { row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    row.forEach { item ->
                        WorkCoverCard(item, offlineCount = item.books.count { it.id in offline },
                            onClick = { onOpenWork(item) }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        item(key = "muslimah_title") { ShelfTitle("Ladies / Muslimah", muslimah.size) }
        itemsIndexed(muslimah, key = { _, item -> "muslimah-${item.id}" }) { index, item ->
            WorkRow(item, index, muslimah.size, offlineCount = item.books.count { it.id in offline }) { onOpenWork(item) }
        }

        item(key = "footer") { SourceFooter { AppAnalytics.interaction("hadith_screen", "open_source_info", "tap"); showSource = true } }
    }

    if (showSource) SourcePolicyDialog { showSource = false }
}

private fun Work.matches(needle: String): Boolean =
    name.contains(needle, true) || nativeTitle.contains(needle, true) || author.contains(needle, true) ||
        books.any { it.romanTitle.contains(needle, true) || it.nativeTitle?.contains(needle, true) == true }

/** Title matches first, then pages inside the books when the search index is on the phone. */
private fun androidx.compose.foundation.lazy.LazyListScope.searchResults(
    needle: String,
    matches: List<Work>,
    offline: Set<String>,
    onOpenWork: (Work) -> Unit,
    onOpenBook: (HadithBook, Int?) -> Unit,
    onClear: () -> Unit
) {
    if (matches.isNotEmpty()) {
        item(key = "results_title") { ShelfTitle("Results", matches.size) }
        itemsIndexed(matches, key = { _, item -> "result-${item.id}" }) { index, item ->
            WorkRow(item, index, matches.size, offlineCount = item.books.count { it.id in offline }) { onOpenWork(item) }
        }
    }
    item(key = "inside_books") { InsideBooksSearch(needle, onOpenBook, titleMatches = matches.size, onClear = onClear) }
}

/** Pages that answer the search, using the shared offline search index. */
@Composable
private fun InsideBooksSearch(needle: String, onOpenBook: (HadithBook, Int?) -> Unit, titleMatches: Int, onClear: () -> Unit) {
    val index by BookSearchIndex.state.collectAsState()
    val ready = index is BookSearchIndex.State.Ready
    var outcome by remember { mutableStateOf(BookSearchIndex.remembered(needle, IslamicBooksCatalog.books)) }
    var searching by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { BookSearchIndex.checkForUpdate() }
    LaunchedEffect(needle, ready) {
        if (needle.length < 2 || !ready) { outcome = null; searching = false; return@LaunchedEffect }
        BookSearchIndex.remembered(needle, IslamicBooksCatalog.books)?.let { outcome = it; searching = false; return@LaunchedEffect }
        searching = true
        delay(250)
        outcome = BookSearchIndex.search(needle, IslamicBooksCatalog.books)
        searching = false
    }
    val hits = outcome?.hits.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.animateContentSize()) {
        when {
            !ready -> SearchIndexOffer(index)
            hits.isNotEmpty() -> {
                ShelfTitle("Inside the books", hits.size)
                hits.forEachIndexed { i, hit ->
                    PageHitRow(hit, i, hits.size) {
                        AppAnalytics.interaction("hadith_screen", "open_page_search_hit", "tap", item = hit.book.id)
                        onOpenBook(hit.book, hit.page)
                    }
                }
            }
            searching || outcome == null -> Row(
                Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(Spacing.sm))
                BooksText("Searching…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            titleMatches == 0 -> EmptyState(
                title = "No collection matches your search.",
                icon = Icons.Default.SearchOff,
                actionText = "Clear search",
                onAction = onClear
            )
            else -> BooksText(
                "No page matches. Try another word or spelling.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.xs)
            )
        }
    }
}

@Composable
private fun SearchIndexOffer(index: BookSearchIndex.State) {
    val downloading = index as? BookSearchIndex.State.Downloading
    val failed = (index as? BookSearchIndex.State.Missing)?.failed == true
    AppCard(contentPadding = PaddingValues(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.ManageSearch, MaterialTheme.colorScheme.primary, size = 40.dp, iconSize = 22.dp)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                LocalizedText("Search inside the books", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LocalizedText(
                    "Type a topic in any language and open the book on the page about it. This needs a one-time download of about 25 MB.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(Spacing.md))
        if (downloading != null) {
            LinearProgressIndicator(
                progress = { downloading.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                strokeCap = StrokeCap.Round, gapSize = 0.dp, drawStopIndicator = {}
            )
        } else {
            if (failed) LocalizedText(
                "Download failed. Check your internet connection.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = { AppAnalytics.interaction("islamic_books_screen", "download_search_index", "tap"); BookSearchIndex.download() },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("hadith_search_index_download")
                ) {
                    Icon(Icons.Default.CloudDownload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    LocalizedText(if (failed) "Retry" else "Download")
                }
                Spacer(Modifier.width(Spacing.sm))
                NeedsInternetChip()
            }
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
        modifier = Modifier.fillMaxWidth().testTag("hadith_page_hit"),
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
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The book being read, with where you left off: one tap goes straight back to that page. */
@Composable
private fun ContinueReadingHero(book: HadithBook, offline: Boolean, onClick: () -> Unit) {
    val work = IslamicBooksCatalog.work(book.collectionId)
    val page = remember(book.id) { HadithPdfStore.lastPage(book) } + 1
    val fraction = if (book.pageCount > 0) (page.toFloat() / book.pageCount).coerceIn(0f, 1f) else null
    HeroSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("books_continue_reading"),
        contentPadding = PaddingValues(Spacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BookCover(work?.nativeTitle ?: book.nativeTitle ?: book.collectionName, width = 64.dp, height = 88.dp, compact = true)
            Spacer(Modifier.width(Spacing.lg))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LocalizedText("Continue reading", style = MaterialTheme.typography.labelLarge, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                Text(
                    localizedUiText(book.collectionName), style = MaterialTheme.typography.titleMedium,
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
private fun RecentBookRow(book: HadithBook, index: Int, count: Int, offline: Boolean, onClick: () -> Unit) {
    val page = remember(book.id) { HadithPdfStore.lastPage(book) } + 1
    AppCard(
        onClick = onClick,
        shape = groupedShape(index, count),
        outlined = false,
        modifier = Modifier.fillMaxWidth().testTag("books_recent_${book.id}"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BookCover(book.nativeTitle ?: book.collectionName, width = 34.dp, height = 46.dp, compact = true)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(localizedUiText(book.collectionName), style = MaterialTheme.typography.titleSmall,
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
            .testTag("hadith_collection_${work.id}"),
        contentPadding = PaddingValues(Spacing.sm)
    ) {
        BookCover(work.nativeTitle, modifier = Modifier.fillMaxWidth().aspectRatio(0.78f))
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
        modifier = Modifier.fillMaxWidth().testTag("hadith_collection_${work.id}"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BookCover(work.nativeTitle, width = 46.dp, height = 62.dp, compact = true,
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

/** Languages in their own script, the number of files, and how many are saved offline. */
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

@Composable
private fun ShelfTitle(text: String, count: Int? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = Spacing.sm, start = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LocalizedText(
            text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f, fill = false).semantics { heading() }
        )
        if (count != null) {
            Spacer(Modifier.width(Spacing.sm))
            TonalPill("$count")
        }
    }
}

@Composable
private fun SourceFooter(onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        LocalizedText(
            "Original publisher PDFs · text never modified",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SourcePolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { AppAnalytics.interaction("hadith_screen", "dismiss_source_info", "dismiss"); onDismiss() },
        icon = { Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary) },
        title = { LocalizedText("Source and translation policy") },
        text = { BooksText(BooksSourcePolicy) },
        confirmButton = { TextButton(onClick = { AppAnalytics.interaction("hadith_screen", "close_source_info", "tap"); onDismiss() }) { LocalizedText("Close") } }
    )
}

// endregion

// region Work page

/** One work: its cover, the edition language, and every volume with its download state. */
@Composable
private fun BookWorkPage(
    work: Work,
    listState: LazyListState,
    prefs: android.content.SharedPreferences,
    onBack: () -> Unit,
    onOpen: (HadithBook) -> Unit
) {
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
    var bulkBusy by remember(work.id) { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<HadithBook?>(null) }

    fun download(book: HadithBook) {
        scope.launch {
            while (HadithPdfStore.download(book).isFailure &&
                snackbar.showSnackbar(downloadFailedText, actionLabel = retryText) == SnackbarResult.ActionPerformed
            ) Unit
        }
    }

    fun downloadAll() {
        scope.launch {
            bulkBusy = true
            val failed = editions.filterNot { it.id in offline }.count { HadithPdfStore.download(it).isFailure }
            bulkBusy = false
            snackbar.showSnackbar(
                localizeUiText(
                    if (failed == 0) "All volumes are available offline." else "$failed downloads failed; tap again to resume.",
                    appLanguage
                )
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
                        AppAnalytics.interaction("hadith_screen", if (resume != null) "continue_reading" else "open_book", "tap", item = target.id)
                        onOpen(target)
                    },
                    onDownloadAll = {
                        AppAnalytics.interaction("hadith_screen", "download_all", "tap", item = work.id)
                        downloadAll()
                    }
                )
            }
            if (work.languages.size > 1) item(key = "languages") {
                EditionLanguagePicker(work.languages, language) {
                    AppAnalytics.interaction("islamic_books_screen", "select_edition_language", "tap", item = it)
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
                    onOpen = { AppAnalytics.interaction("hadith_screen", "open_book", "tap", item = book.id); onOpen(book) },
                    onDownload = { AppAnalytics.interaction("hadith_screen", "download_book", "tap", item = book.id); download(book) },
                    onDelete = { confirmDelete = book }
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(Spacing.lg))
    }

    confirmDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { AppAnalytics.interaction("hadith_screen", "dismiss_remove_download", "dismiss"); confirmDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { LocalizedText("Remove download?") },
            text = { LocalizedText("${book.romanTitle} will remain available online.") },
            confirmButton = {
                TextButton(onClick = {
                    AppAnalytics.interaction("hadith_screen", "confirm_remove_download", "tap")
                    HadithPdfStore.delete(book)
                    confirmDelete = null
                }) { LocalizedText("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { AppAnalytics.interaction("hadith_screen", "cancel_remove_download", "tap"); confirmDelete = null }) {
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
    HeroSurface(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BookCover(work.nativeTitle, width = 92.dp, height = 126.dp,
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
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("books_read"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = HeroGradientBottom)
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, null, Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
                LocalizedText(if (resume != null) "Resume" else "Read", fontWeight = FontWeight.Bold, maxLines = 1)
            }
            if (allOffline) {
                Row(
                    Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50))
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
                    modifier = Modifier.heightIn(min = 48.dp).testTag("books_download_all"),
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
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("books_language_picker")) {
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
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).testTag("books_language_picker"),
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
        modifier = Modifier.fillMaxWidth().pressScale(interaction, 0.98f).testTag("hadith_book_${book.id}")
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
                downloaded -> IconButton(onClick = onDelete, modifier = Modifier.testTag("books_remove_${book.id}")) {
                    Icon(Icons.Default.DownloadDone, contentDescription = localizedUiText("Remove download?"),
                        tint = MaterialTheme.colorScheme.primary)
                }
                else -> IconButton(onClick = onDownload, enabled = isOnline, modifier = Modifier.testTag("books_download_${book.id}")) {
                    Icon(Icons.Default.Download, contentDescription = localizedUiText("Download"))
                }
            }
        }
    }
}

// endregion

// region Shared pieces

/**
 * A drawn book cover: emerald binding, geometric pattern, a gold frame and the title in its own
 * script. [accent] tints the binding (the Muslimah shelf uses a soft rose).
 */
@Composable
private fun BookCover(
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

/** Text from a variable, translated at run time like the rest of the library's existing copy. */
@Composable
private fun BooksText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) = LocalizedText(text, modifier = modifier, style = style, color = color)

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
private fun volumeText(book: HadithBook): String = localizedUiText(
    book.volumeTitle ?: if (book.volumeCount == 1) "Complete edition" else "Volume ${book.volume} of ${book.volumeCount}"
)

// endregion
