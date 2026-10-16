package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.analytics.AnalyticsScreen
import com.example.core.analytics.AnalyticsSearch
import com.example.core.analytics.AppAnalytics
import com.example.hadith.HadithBook
import com.example.hadith.HadithCatalog
import com.example.hadith.HadithPdfStore
import com.example.hadith.WorldItem
import com.example.hadith.WorldLibrary
import com.example.hadith.WorldSort
import com.example.ui.components.AppCard
import com.example.ui.components.AppSearchField
import com.example.ui.components.BackIconButton
import com.example.ui.components.EmptyState
import com.example.ui.components.ErrorState
import com.example.ui.components.LoadingState
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.components.islamicPattern
import com.example.ui.components.pressScale
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat

/** The source stops paging past this many results; narrower searches reach the rest. */
private const val MAX_BROWSABLE = 10_000

private const val SCREEN = "world_library_screen"

/** English source copy; every string here is shown through the app's translations. */
private object WorldCopy {
    const val TITLE = "World Library"
    const val SUBTITLE = "Books by Ahle Sunnat scholars"
    const val SEARCH_HINT = "Search any book or author…"
    const val ALL_LANGUAGES = "All languages"
    const val SAVED = "Saved offline"
    const val BOOKS = "Books"
    const val RESULTS = "Results"
    const val READ = "Read"
    const val CONTINUE = "Continue reading"
    const val CLEAR = "Clear"
    const val SOURCE = "View at the source"
    const val LOADING = "Loading…"
    const val UNREACHABLE = "Could not reach the library. Check your internet connection."
    const val NO_MATCH = "No book matches your search."
    const val REMOVED_ITEM = "This book has been removed from its source, so it is no longer available here."
    const val NO_PDF = "This entry has no PDF to read."
    const val DOWNLOAD_FAILED = "Download failed. Check your internet connection."
    const val RETRY = "Retry"
    const val REMOVED_SAVED = "Saved books that were removed from their source have been removed here too."
    const val ABOUT = "About this library"
    const val ABOUT_TEXT = "These books are read live from the Internet Archive (archive.org), a free public library. " +
        "Only books whose author or publisher is on this app's list of Ahle Sunnat wal Jamaat scholars and institutions are shown, matched by the author name recorded at the source. " +
        "When a book is removed from the Internet Archive it is removed here as well, including any copy saved offline. " +
        "PDF pages are shown exactly as published and are never translated or modified."
    const val CLOSE = "Close"
    const val REMOVE_TITLE = "Remove download?"
    const val REMOVE = "Remove"
    const val CANCEL = "Cancel"
    const val LANGUAGE = "Language"
    const val SORT = "Sort"
}

/** Deep, quiet cover tones; each book keeps the same one so it is easy to find again. */
private val CoverTones = listOf(
    Color(0xFF173F31) to Color(0xFF0E2A20),
    Color(0xFF1F4E5F) to Color(0xFF12313C),
    Color(0xFF5A2E2E) to Color(0xFF3A1C1C),
    Color(0xFF2E3A5F) to Color(0xFF1C243C),
    Color(0xFF5C4A1E) to Color(0xFF3B2F12)
)

/**
 * Books by Ahle Sunnat scholars from the Internet Archive in every language. Search comes first; the
 * language and order sit in two compact pickers, downloads stay on a shelf that works offline, and a
 * book opens to its volumes with one clear Read action. Titles and authors keep the source's words.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldLibraryScreen(
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    AnalyticsScreen(SCREEN)
    val context = LocalContext.current
    remember { WorldLibrary.init(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val saved by WorldLibrary.saved.collectAsState()
    val recent by WorldLibrary.recentSearches.collectAsState()
    val progress by HadithPdfStore.progress.collectAsState()

    // Held here, above the reader, so closing a book returns to the same list and search.
    var query by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf(WorldLibrary.preferredLanguage()) }
    var sortName by rememberSaveable { mutableStateOf(WorldLibrary.preferredSort().name) }
    val sort = WorldSort.entries.firstOrNull { it.name == sortName } ?: WorldSort.POPULAR
    val needle = query.trim()
    AnalyticsSearch(SCREEN, needle)

    var results by remember { mutableStateOf<List<WorldItem>>(emptyList()) }
    var total by remember { mutableStateOf<Int?>(null) }
    var pages by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var moreFailed by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<WorldItem?>(null) }
    var volumes by remember { mutableStateOf<Result<List<HadithBook>?>?>(null) }
    var volumesAttempt by remember { mutableIntStateOf(0) }
    var openBook by remember { mutableStateOf<HadithBook?>(null) }
    var confirmDelete by remember { mutableStateOf<HadithBook?>(null) }
    var showSource by remember { mutableStateOf(false) }
    var showLanguages by remember { mutableStateOf(false) }

    val downloadFailedText = localizedUiText(WorldCopy.DOWNLOAD_FAILED)
    val retryText = localizedUiText(WorldCopy.RETRY)
    val removedText = localizedUiText(WorldCopy.REMOVED_SAVED)

    LaunchedEffect(Unit) {
        if (WorldLibrary.syncRemovals() > 0) snackbar.showSnackbar(removedText)
    }
    // Keyed on the trimmed text, so a trailing space does not start a new search. A new key cancels
    // the search still running for the old one.
    LaunchedEffect(needle, language, sort, reload) {
        if (needle.isNotEmpty()) delay(300)
        loading = true
        failed = false
        moreFailed = false
        WorldLibrary.search(needle, language, 1, sort).onSuccess {
            results = it.items
            total = it.total
            pages = 1
        }.onFailure {
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
    LaunchedEffect(openBook) { if (openBook == null) onImmersiveChange(false) }

    fun loadMore() {
        if (loadingMore) return
        val search = Triple(needle, language, sort)
        scope.launch {
            loadingMore = true
            moreFailed = false
            val result = WorldLibrary.search(search.first, search.second, pages + 1, search.third)
            // A page that arrives after the search changed belongs to the old list and is dropped.
            if (search == Triple(query.trim(), language, sort)) result.onSuccess {
                results = (results + it.items).distinctBy(WorldItem::identifier)
                total = it.total
                pages++
            }.onFailure { moreFailed = true }
            loadingMore = false
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

    openBook?.let { book ->
        BackHandler {
            if (isImmersive) onImmersiveChange(false) else openBook = null
        }
        HadithPdfReader(
            book = book,
            startPage = null,
            isImmersive = isImmersive,
            onImmersiveChange = onImmersiveChange,
            onBack = {
                onImmersiveChange(false)
                openBook = null
            }
        )
        return
    }

    BackHandler(enabled = selected != null) { selected = null }

    val item = selected
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (item != null) {
            WorldBookDetail(
                item = item,
                volumes = volumes,
                saved = saved,
                progress = progress,
                onBack = { selected = null },
                onRetry = { volumesAttempt++ },
                onOpen = { openBook = it },
                onDownload = ::download,
                onDelete = { confirmDelete = it }
            )
        } else {
            val focusManager = LocalFocusManager.current
            val shelf = remember(saved, needle) {
                WorldLibrary.matchSaved(saved, needle).sortedByDescending { HadithPdfStore.lastOpened(it) }
            }
            val listState = rememberLazyListState()
            Column(Modifier.fillMaxSize()) {
                WorldHeader(
                    title = WorldCopy.TITLE,
                    subtitle = WorldCopy.SUBTITLE,
                    localize = true,
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = { AppAnalytics.interaction(SCREEN, "about_source", "tap"); showSource = true }) {
                            Icon(Icons.Outlined.Info, contentDescription = localizedUiText(WorldCopy.ABOUT))
                        }
                    }
                )
                // Search and the two pickers stay put while the list scrolls under them.
                Column(Modifier.padding(horizontal = Spacing.screen)) {
                    AppSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = WorldCopy.SEARCH_HINT,
                        modifier = Modifier.testTag("world_library_search"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            WorldLibrary.rememberSearch(needle)
                        })
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        PickerPill(
                            icon = Icons.Default.Translate,
                            label = if (language == HadithCatalog.ALL_LANGUAGES) localizedUiText(WorldCopy.ALL_LANGUAGES)
                                else WorldLibrary.languageName(language) ?: language,
                            description = localizedUiText(WorldCopy.LANGUAGE),
                            active = language != HadithCatalog.ALL_LANGUAGES,
                            modifier = Modifier.weight(1f, fill = false).testTag("world_library_language"),
                            onClick = { showLanguages = true }
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
                // A thin bar while a new search replaces the list already on screen.
                Box(Modifier.fillMaxWidth().height(3.dp).padding(top = 1.dp)) {
                    if (loading && results.isNotEmpty()) LinearProgressIndicator(
                        Modifier.fillMaxWidth().height(2.dp),
                        color = HadithAccent,
                        trackColor = Color.Transparent
                    )
                }
                LazyColumn(
                    Modifier.fillMaxSize().testTag("world_library_list"),
                    state = listState,
                    contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.sm, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    if (needle.isEmpty() && recent.isNotEmpty()) item(key = "recent") {
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
                    if (shelf.isNotEmpty()) item(key = "shelf") {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            SectionHeading(WorldCopy.SAVED, shelf.size)
                            LazyRow(
                                Modifier.fillMaxWidth().testTag("world_library_shelf"),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                items(shelf, key = { "shelf-${it.id}" }) { book ->
                                    ShelfBook(
                                        book = book,
                                        onOpen = {
                                            AppAnalytics.interaction(SCREEN, "open_saved", "tap", item = book.slug)
                                            openBook = book
                                        },
                                        onRemove = { confirmDelete = book }
                                    )
                                }
                            }
                        }
                    }
                    item(key = "heading") {
                        SectionHeading(if (needle.isEmpty()) WorldCopy.BOOKS else WorldCopy.RESULTS, total)
                    }
                    when {
                        loading && results.isEmpty() -> items(4, key = { "skeleton-$it" }) { SkeletonCard() }
                        failed -> item(key = "error") {
                            ErrorState(
                                onRetry = { reload++ },
                                title = WorldCopy.UNREACHABLE,
                                retryText = WorldCopy.RETRY
                            )
                        }
                        results.isEmpty() -> item(key = "empty") {
                            // A narrower language is the usual reason for no match; offer to widen it.
                            EmptyState(
                                title = WorldCopy.NO_MATCH,
                                icon = Icons.Default.SearchOff,
                                actionText = if (language != HadithCatalog.ALL_LANGUAGES) WorldCopy.ALL_LANGUAGES else null,
                                onAction = {
                                    language = HadithCatalog.ALL_LANGUAGES
                                    WorldLibrary.setPreferredLanguage(HadithCatalog.ALL_LANGUAGES)
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
                            if (results.size < minOf(total ?: 0, MAX_BROWSABLE)) item(key = "more-$pages") {
                                if (moreFailed) TextButton(::loadMore, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                                    LocalizedText(WorldCopy.RETRY, color = HadithAccent)
                                } else {
                                    // Reaching the end of the list asks for the next page.
                                    LaunchedEffect(results) { loadMore() }
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
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showLanguages) {
        LanguageSheet(
            selected = language,
            onDismiss = { showLanguages = false },
            onSelect = { code ->
                AppAnalytics.interaction(SCREEN, "select_language", "tap", item = code)
                language = code
                WorldLibrary.setPreferredLanguage(code)
                showLanguages = false
            }
        )
    }

    if (showSource) {
        AlertDialog(
            onDismissRequest = { showSource = false },
            icon = { Icon(Icons.Default.Public, null, tint = HadithAccent) },
            title = { LocalizedText(WorldCopy.ABOUT) },
            text = { LocalizedText(WorldCopy.ABOUT_TEXT) },
            confirmButton = { TextButton({ showSource = false }) { LocalizedText(WorldCopy.CLOSE, color = HadithAccent) } }
        )
    }

    confirmDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { LocalizedText(WorldCopy.REMOVE_TITLE) },
            text = { Text(book.romanTitle) },
            confirmButton = {
                TextButton({
                    AppAnalytics.interaction(SCREEN, "remove_download", "tap", item = book.slug)
                    WorldLibrary.delete(book)
                    confirmDelete = null
                }) {
                    LocalizedText(WorldCopy.REMOVE, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton({ confirmDelete = null }) { LocalizedText(WorldCopy.CANCEL) } }
        )
    }
}

// region Browse

/** Back arrow, title and subtitle; [localize] is false for a book's own title and author. */
@Composable
private fun WorldHeader(
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
            description = localizedUiText(WorldCopy.SORT),
            active = sort != WorldSort.POPULAR,
            modifier = Modifier.testTag("world_library_sort"),
            onClick = { open = true }
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
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

/** Every language in one sheet instead of a long row of chips; names are shown in their own script. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSheet(selected: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            localizedUiText(WorldCopy.LANGUAGE),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).semantics { heading() }
        )
        val options = listOf(HadithCatalog.ALL_LANGUAGES) + WorldLibrary.languages.map { it.code }
        LazyColumn(
            Modifier.fillMaxWidth().testTag("world_library_languages"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(options, key = { it }) { code ->
                val active = code == selected
                Row(
                    Modifier.fillMaxWidth()
                        .clickable(role = Role.RadioButton) { onSelect(code) }
                        .heightIn(min = 52.dp)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (code == HadithCatalog.ALL_LANGUAGES) localizedUiText(WorldCopy.ALL_LANGUAGES)
                        else WorldLibrary.languageName(code) ?: code,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) HadithAccent else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (active) Icon(Icons.Default.Check, null, tint = HadithAccent)
                }
            }
        }
    }
}

/** Recent searches as tappable chips; the trash button forgets them all. */
@Composable
private fun RecentSearches(recent: List<String>, onPick: (String) -> Unit, onClear: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
        IconButton(onClick = onClear, modifier = Modifier.testTag("world_library_clear_recent")) {
            Icon(Icons.Default.DeleteSweep, contentDescription = localizedUiText(WorldCopy.CLEAR),
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
            modifier = Modifier.semantics { heading() }
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

/** Titles and authors keep the source's own words and are never run through the app's translations. */
@Composable
private fun WorldItemCard(item: WorldItem, offlineCount: Int, onClick: () -> Unit) {
    val numbers = rememberNumberFormat()
    AppCard(
        modifier = Modifier.fillMaxWidth().testTag("world_library_item"),
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
                if (item.creator.isNotBlank()) Text(
                    item.creator,
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

/** A downloaded volume on the offline shelf: tap to read, long-press to remove. */
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
                onLongClickLabel = localizedUiText(WorldCopy.REMOVE_TITLE),
                onLongClick = onRemove,
                onClick = onOpen
            )
            .padding(4.dp)
            .testTag("world_library_saved_${book.id}"),
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

/** A static placeholder in the shape of a result card; no endless animation while the source answers. */
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
    val numbers = rememberNumberFormat()
    val found = volumes?.getOrNull()
    Column(Modifier.fillMaxSize()) {
        WorldHeader(title = item.title, subtitle = null, localize = false, onBack = onBack)
        LazyColumn(
            Modifier.fillMaxSize().testTag("world_library_volumes"),
            contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.xs, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(key = "about") {
                Row(verticalAlignment = Alignment.Top) {
                    BookCover(item.identifier, item.title, 88.dp, 118.dp)
                    Spacer(Modifier.width(Spacing.lg))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.creator.isNotBlank()) Text(
                            item.creator,
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
                        if (found != null && found.size > 1) MetaIcon(Icons.Default.LibraryBooks, numbers.format(found.size))
                    }
                }
            }
            when {
                volumes == null -> item(key = "loading") { LoadingState(message = WorldCopy.LOADING) }
                volumes.isFailure -> item(key = "error") {
                    ErrorState(onRetry = onRetry, title = WorldCopy.UNREACHABLE, retryText = WorldCopy.RETRY)
                }
                found == null -> item(key = "removed") {
                    EmptyState(title = WorldCopy.REMOVED_ITEM, icon = Icons.Default.CloudOff)
                }
                found.isEmpty() -> item(key = "nopdf") {
                    EmptyState(title = WorldCopy.NO_PDF, icon = Icons.Default.Description)
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
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("world_library_read"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = HadithGreen, contentColor = Color.White)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(Spacing.sm))
                            LocalizedText(if (resume != null) WorldCopy.CONTINUE else WorldCopy.READ, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    items(found, key = HadithBook::id) { book ->
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
                    item(key = "source") {
                        val uriHandler = LocalUriHandler.current
                        TextButton(
                            { runCatching { uriHandler.openUri(found.first().sourcePageUrl) } },
                            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, null, Modifier.size(16.dp), tint = HadithAccent)
                            Spacer(Modifier.width(6.dp))
                            LocalizedText(WorldCopy.SOURCE, color = HadithAccent)
                        }
                    }
                }
            }
        }
    }
}

// endregion
