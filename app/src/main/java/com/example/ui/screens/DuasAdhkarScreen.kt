package com.example.ui.screens

import com.example.ui.components.ScreenHeader
import com.example.ui.components.AppSearchField
import com.example.ui.components.AppCard
import com.example.ui.components.HeroSurface
import com.example.ui.components.IconBadge
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.components.accentTint
import com.example.ui.components.islamicPattern
import com.example.ui.theme.GoldAccent
import androidx.compose.material3.HorizontalDivider
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AdhkarDatasetRepository
import com.example.domain.model.Dua
import com.example.domain.model.DuaDatasetRepository
import com.example.domain.model.englishBenefit
import com.example.domain.model.englishContext
import com.example.domain.model.englishMeaning
import com.example.domain.model.i18n.textIn
import com.example.domain.model.PrayerType
import com.example.domain.model.TasbeehPreset
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeHadithGrade
import com.example.ui.localization.localizePhrase
import com.example.ui.localization.DisplayMeaning
import com.example.ui.localization.bilingualContent
import com.example.ui.localization.localizeUiText
import com.example.ui.theme.GoldContainerDark
import com.example.ui.theme.GoldContainerLight
import com.example.viewmodel.DuasScreenTab
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private val SCREEN_TABS = listOf(
    DuasScreenTab.DUAS to "Duas",
    DuasScreenTab.ADHKAR to "Adhkar",
    DuasScreenTab.TASBEEH to "Tasbeeh"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuasAdhkarScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    com.example.core.analytics.AnalyticsScreen("duas_adhkar_screen")

    // Kept by id so an open dua survives rotation.
    var openDuaId by rememberSaveable { mutableStateOf<String?>(null) }
    var arabicScale by rememberSaveable { mutableFloatStateOf(1f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("duas_adhkar_screen")
    ) {
        // The tabs sit on the page colour, so the screen reads as one calm surface.
        PrimaryTabRow(
            selectedTabIndex = uiState.duasScreenTab.ordinal,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            SCREEN_TABS.forEach { (tab, label) ->
                Tab(
                    selected = uiState.duasScreenTab == tab,
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "select_duas_tab", "tap", item = tab.name); viewModel.selectDuasScreenTab(tab) },
                    text = { LocalizedText(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("duas_tab_${tab.name.lowercase()}")
                )
            }
        }

        when (uiState.duasScreenTab) {
            DuasScreenTab.DUAS -> DuasTab(uiState = uiState, viewModel = viewModel, onDuaClick = { openDuaId = it.id })
            DuasScreenTab.ADHKAR -> AdhkarTab(uiState = uiState, viewModel = viewModel)
            DuasScreenTab.TASBEEH -> TasbeehTab(uiState = uiState, viewModel = viewModel)
        }
    }

    openDuaId?.let(DuaDatasetRepository::byId)?.let { dua ->
        DuaDetailSheet(
            dua = dua,
            siblings = uiState.filteredDuas,
            arabicScale = arabicScale,
            onArabicScaleChange = { arabicScale = it },
            onOpen = { openDuaId = it.id },
            onDismiss = { openDuaId = null }
        )
    }
}

private val DUA_COUNTS: Map<String, Int> by lazy {
    DuaDatasetRepository.DUAS.groupingBy { it.category }.eachCount() +
        (DuaDatasetRepository.ALL_CATEGORY to DuaDatasetRepository.DUAS.size)
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Morning & Evening" -> Icons.Default.WbTwilight
    "Prayer & Mosque" -> Icons.Default.Mosque
    "Sleep & Awakening" -> Icons.Default.Bedtime
    "Anxiety & Distress" -> Icons.Default.SelfImprovement
    "Travel & Leaving Home" -> Icons.Default.Luggage
    "Forgiveness & Istighfar" -> Icons.Default.VolunteerActivism
    "Parents & Family" -> Icons.Default.FamilyRestroom
    "Food & Gratitude" -> Icons.Default.Restaurant
    "Quranic Duas" -> Icons.Default.AutoStories
    "Protection & Healing" -> Icons.Default.HealthAndSafety
    "Weather & Nature" -> Icons.Default.Umbrella
    "Death & Janaza" -> Icons.Default.Spa
    "Daily Life & Manners" -> Icons.Default.Handshake
    "Hajj & Umrah" -> Icons.Default.Explore
    "Ramadan & Fasting" -> Icons.Default.NightsStay
    "Comprehensive Duas" -> Icons.Default.AutoAwesome
    else -> Icons.AutoMirrored.Filled.MenuBook
}

private fun countLabel(count: Int, language: String) = "$count ${localizeUiText("Duas", language)}"

@Composable
fun DuasTab(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onDuaClick: (Dua) -> Unit
) {
    com.example.core.analytics.AnalyticsScreen("duas_tab")

    val language = LocalAppLanguage.current
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    // "All Duas" is also the unfiltered state, so opening its full list is tracked here.
    com.example.core.analytics.AnalyticsScroll("duas_adhkar_screen", listState)
    var browsingAll by rememberSaveable { mutableStateOf(false) }
    val category = uiState.selectedDuaCategory
    val inCategory = category != DuaDatasetRepository.ALL_CATEGORY
    val searching = uiState.duaSearchQuery.isNotEmpty()
    com.example.core.analytics.AnalyticsSearch("duas_tab", uiState.duaSearchQuery)
    // With nothing chosen yet, the tab opens on a picker of occasions instead of a long list.
    val showOccasions = !inCategory && !browsingAll && !searching

    // A new filter starts from its first result, not from the previous scroll position.
    LaunchedEffect(category, uiState.duaSearchQuery) {
        listState.scrollToItem(0)
    }

    // Back steps out one level: first the search, then the list, before leaving the screen.
    BackHandler(enabled = !showOccasions) { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "dua_list_system_back", "back");
        when {
            searching -> viewModel.searchDuas("")
            else -> {
                viewModel.selectDuaCategory(DuaDatasetRepository.ALL_CATEGORY)
                browsingAll = false
            }
        }
    }

    // Results stay above the keyboard while searching.
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        AppSearchField(
            query = uiState.duaSearchQuery,
            onQueryChange = viewModel::searchDuas,
            placeholder = "Search duas",
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .testTag("dua_search_field"),
            keyboardActions = KeyboardActions(onSearch = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "submit_dua_search", "search_submit"); keyboard?.hide() })
        )

        if (showOccasions) {
            OccasionGrid(
                onPick = { picked ->
                    if (picked == DuaDatasetRepository.ALL_CATEGORY) browsingAll = true
                    viewModel.selectDuaCategory(picked)
                }
            )
        } else {
            DuaListContent(
                uiState = uiState,
                listState = listState,
                onBack = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "dua_list_back", "back");
                    viewModel.searchDuas("")
                    viewModel.selectDuaCategory(DuaDatasetRepository.ALL_CATEGORY)
                    browsingAll = false
                },
                onClearSearch = { viewModel.searchDuas("") },
                onDuaClick = onDuaClick
            )
        }
    }
}

@Composable
private fun DuaListContent(
    uiState: MainUiState,
    listState: LazyListState,
    onBack: () -> Unit,
    onClearSearch: () -> Unit,
    onDuaClick: (Dua) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DuaListHeader(
            category = uiState.selectedDuaCategory,
            count = uiState.filteredDuas.size,
            onBack = onBack
        )

        if (uiState.filteredDuas.isEmpty()) {
            EmptyDuaResults(
                showClearSearch = uiState.duaSearchQuery.isNotEmpty(),
                onClearSearch = onClearSearch
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("dua_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredDuas, key = { it.id }) { dua ->
                    DuaCard(
                        dua = dua,
                        showCategory = uiState.selectedDuaCategory == DuaDatasetRepository.ALL_CATEGORY,
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "open_dua", "tap", item = dua.category); onDuaClick(dua) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DuaCard(dua: Dua, showCategory: Boolean, onClick: () -> Unit) {
    val language = LocalAppLanguage.current
    val title = dua.displayTitle(language)
    val meaning = dua.displayMeaning(language)
    AppCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(Spacing.lg),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dua_card_${dua.id}")
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (language != "en" && title == dua.title) {
                    com.example.ui.localization.LocalizedText("English", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (showCategory) {
                    LocalizedText(
                        text = dua.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = accentTint(categoryAccent(dua.category)),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            GradeBadge(dua)
        }

        // The Arabic sits on its own soft panel so it reads as the heart of the card.
        Spacer(modifier = Modifier.height(Spacing.md))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            ArabicText(text = dua.arabicText, fontSize = 21.sp, lineHeight = 38.sp, maxLines = 2)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = meaning.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        meaning.fallbackLanguage?.let { label ->
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            LocalizedText(
                text = dua.displaySource(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (dua.recommendedCount > 1) {
                Spacer(modifier = Modifier.width(8.dp))
                RepeatChip(dua.recommendedCount)
            }
        }
    }
}

@Composable
private fun EmptyDuaResults(showClearSearch: Boolean, onClearSearch: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp)
            .testTag("dua_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LocalizedText(
            text = "No duas found",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (showClearSearch) {
            TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("duas_adhkar_screen", "clear_dua_search", onClearSearch), modifier = Modifier.heightIn(min = 48.dp)) { LocalizedText("Clear search") }
        }
    }
}

/** Each occasion gets its own calm accent, so the grid scans by colour as well as by name. */
private fun categoryAccent(category: String): Color = when (category) {
    "Morning & Evening" -> Color(0xFFA9812A)
    "Prayer & Mosque" -> Color(0xFF237A63)
    "Sleep & Awakening" -> Color(0xFF5B5BAE)
    "Anxiety & Distress" -> Color(0xFF2A7FA8)
    "Travel & Leaving Home" -> Color(0xFF8A5A3C)
    "Forgiveness & Istighfar" -> Color(0xFF2E8B6E)
    "Parents & Family" -> Color(0xFFB0507A)
    "Food & Gratitude" -> Color(0xFFC0664A)
    "Quranic Duas" -> Color(0xFF14614B)
    "Protection & Healing" -> Color(0xFF3B6FA8)
    "Weather & Nature" -> Color(0xFF2A8C8C)
    "Death & Janaza" -> Color(0xFF5E7268)
    "Daily Life & Manners" -> Color(0xFF8A6D1F)
    "Hajj & Umrah" -> Color(0xFF6B5BAE)
    "Ramadan & Fasting" -> Color(0xFF4A5C9E)
    "Comprehensive Duas" -> Color(0xFF9A6286)
    else -> Color(0xFF14614B)
}

@Composable
private fun OccasionGrid(onPick: (String) -> Unit) {
    val language = LocalAppLanguage.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 156.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("dua_occasion_grid"),
        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = Spacing.lg, bottom = Spacing.xxl),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val openCategory = { category: String ->
            com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "open_dua_category", "tap", item = category)
            onPick(category)
        }
        fun tagOf(category: String) = "dua_occasion_${DuaDatasetRepository.CATEGORIES.indexOf(category)}"
        // "All Duas" leads the grid as one wide banner; every occasion below it is a compact tile.
        item(key = DuaDatasetRepository.ALL_CATEGORY, span = { GridItemSpan(maxLineSpan) }) {
            AllDuasBanner(
                count = countLabel(DUA_COUNTS[DuaDatasetRepository.ALL_CATEGORY] ?: 0, language),
                onClick = { openCategory(DuaDatasetRepository.ALL_CATEGORY) },
                modifier = Modifier.fillMaxWidth().testTag(tagOf(DuaDatasetRepository.ALL_CATEGORY))
            )
        }
        item(key = "occasions_title", span = { GridItemSpan(maxLineSpan) }) {
            LocalizedText(
                text = "Choose an occasion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 2.dp)
            )
        }
        gridItems(
            DuaDatasetRepository.CATEGORIES.filter { it != DuaDatasetRepository.ALL_CATEGORY },
            key = { it }
        ) { category ->
            OccasionTile(
                category = category,
                count = countLabel(DUA_COUNTS[category] ?: 0, language),
                onClick = { openCategory(category) },
                modifier = Modifier.fillMaxWidth().testTag(tagOf(category))
            )
        }
    }
}

/** The wide "All Duas" entry: emerald hero with the full count, above the occasion tiles. */
@Composable
private fun AllDuasBanner(count: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    HeroSurface(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoldAccent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon(DuaDatasetRepository.ALL_CATEGORY), contentDescription = null, tint = GoldAccent, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                LocalizedText(
                    text = DuaDatasetRepository.ALL_CATEGORY,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent,
                    maxLines = 1
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/** A compact occasion tile: tinted badge beside the name, with the count underneath. */
@Composable
private fun OccasionTile(category: String, count: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
    ) {
        // Icon beside the name keeps each tile short, so more occasions fit on one screen.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = categoryIcon(category), accent = categoryAccent(category), size = 42.dp, iconSize = 22.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                LocalizedText(
                    text = category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.labelMedium,
                    color = accentTint(categoryAccent(category)),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DuaListHeader(category: String, count: Int, onBack: () -> Unit) {
    val language = LocalAppLanguage.current
    ScreenHeader(
        title = category,
        modifier = Modifier
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .testTag("dua_list_header"),
        onBack = onBack,
        backTestTag = "dua_list_back",
        leading = {
            IconBadge(icon = categoryIcon(category), accent = categoryAccent(category), size = 40.dp, iconSize = 22.dp)
        }
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        val accent = accentTint(categoryAccent(category))
        TonalPill(
            text = countLabel(count, language),
            containerColor = accent.copy(alpha = 0.12f),
            contentColor = accent
        )
    }
}

/** A small caption naming the part of the dua below it ("Pronunciation", "Translation"). */
@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 14.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        LocalizedText(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}

private const val MIN_ARABIC_SCALE = 0.8f
private const val MAX_ARABIC_SCALE = 1.6f
private const val ARABIC_SCALE_STEP = 0.2f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuaDetailSheet(
    dua: Dua,
    siblings: List<Dua>,
    arabicScale: Float,
    onArabicScaleChange: (Float) -> Unit,
    onOpen: (Dua) -> Unit,
    onDismiss: () -> Unit
) {
    val language = LocalAppLanguage.current
    val title = dua.displayTitle(language)
    val meaning = dua.displayMeaning(language)
    val context = LocalContext.current
    val duaAudio = rememberDuaSpeech()
    androidx.compose.runtime.DisposableEffect(dua.id) {
        onDispose { duaAudio.stop() }
    }
    val position = siblings.indexOfFirst { it.id == dua.id }
    val scrollState = rememberScrollState()
    // Moving to the next dua starts reading it from the top.
    com.example.core.analytics.AnalyticsScroll("duas_adhkar_screen", scrollState)
    LaunchedEffect(dua.id) { scrollState.scrollTo(0) }

    ModalBottomSheet(
        onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("duas_adhkar_screen", "dismiss_dua_detail", onDismiss),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        // The default sheet colour is Material's lavender tint, not the app's palette.
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
                .testTag("dua_detail_sheet")
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (language != "en" && title == dua.title) {
                        com.example.ui.localization.LocalizedText("English", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GradeBadge(dua)
                        Icon(
                            categoryIcon(dua.category),
                            contentDescription = null,
                            tint = accentTint(categoryAccent(dua.category)),
                            modifier = Modifier.size(16.dp)
                        )
                        LocalizedText(
                            text = dua.category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = accentTint(categoryAccent(dua.category)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("duas_adhkar_screen", "close_dua_detail", onDismiss)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = localizeUiText("Close", language)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dua.displayContext(language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            // The Arabic is the centrepiece: a soft emerald panel with a faint geometric motif behind it.
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .islamicPattern(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), cell = 40.dp)
                        .padding(start = 18.dp, end = 18.dp, bottom = 20.dp, top = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (dua.recommendedCount > 1) RepeatChip(dua.recommendedCount)
                        Spacer(modifier = Modifier.weight(1f))
                        val sizeLabel = localizeUiText("Text size", language)
                        IconButton(
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "dua_text_smaller", "tap"); onArabicScaleChange((arabicScale - ARABIC_SCALE_STEP).coerceAtLeast(MIN_ARABIC_SCALE)) },
                            enabled = arabicScale > MIN_ARABIC_SCALE + 0.01f,
                            modifier = Modifier.testTag("dua_text_smaller")
                        ) {
                            Icon(Icons.Default.TextDecrease, contentDescription = "$sizeLabel −")
                        }
                        IconButton(
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "dua_text_larger", "tap"); onArabicScaleChange((arabicScale + ARABIC_SCALE_STEP).coerceAtMost(MAX_ARABIC_SCALE)) },
                            enabled = arabicScale < MAX_ARABIC_SCALE - 0.01f,
                            modifier = Modifier.testTag("dua_text_larger")
                        ) {
                            Icon(Icons.Default.TextIncrease, contentDescription = "$sizeLabel +")
                        }
                    }
                    SelectionContainer {
                        ArabicText(
                            text = dua.arabicText,
                            fontSize = (28 * arabicScale).sp,
                            lineHeight = (52 * arabicScale).sp
                        )
                    }
                }
            }

            DuaAudioControls("dua_${dua.id}", dua.arabicText, duaAudio)
            Spacer(modifier = Modifier.height(16.dp))
            SelectionContainer {
                Column {
                    SectionLabel("Pronunciation")
                    Text(
                        text = dua.transliteration,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SectionLabel("Translation")
                    Text(
                        text = meaning.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    meaning.fallbackLanguage?.let { label ->
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            InfoPanel(
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp)) },
                color = goldContainer()
            ) {
                LocalizedText(
                    text = "Virtue / Benefit:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = dua.displayBenefit(language), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(10.dp))
            InfoPanel(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = localizeUiText("Source citation", language),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                // Provenance stays quiet: a faint panel, small type.
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                LocalizedText(
                    text = "${dua.sourceCollection}, ${dua.sourceReference}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                // The badge already shows a plain grade; only a fuller scholarly note adds anything.
                if (dua.authenticityGrading != dua.gradeLabel) {
                    Text(
                        text = localizePhrase(dua.authenticityGrading, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "copy_dua", "tap"); copyDua(context, dua, language) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dua_copy_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    LocalizedText("Copy dua", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                }
                Button(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "share_dua", "tap"); shareDua(context, dua, language) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dua_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    LocalizedText("Share dua", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                }
            }

            // Step through the list the dua was opened from without closing the sheet.
            if (position >= 0 && siblings.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "previous_dua", "tap"); onOpen(siblings[position - 1]) },
                        enabled = position > 0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dua_previous_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                        LocalizedText("Previous", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    }
                    Text(
                        text = "${position + 1} / ${siblings.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("duas_adhkar_screen", "next_dua", "tap"); onOpen(siblings[position + 1]) },
                        enabled = position < siblings.lastIndex,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dua_next_button")
                    ) {
                        LocalizedText("Next", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPanel(
    icon: @Composable () -> Unit,
    color: Color,
    content: @Composable () -> Unit
) {
    Surface(color = color, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.padding(top = 1.dp)) { icon() }
            Spacer(modifier = Modifier.width(10.dp))
            Column { content() }
        }
    }
}

/** Vowelled Arabic in the system Naskh face, always right-to-left whatever the app language. */
@Composable
internal fun ArabicText(
    text: String,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Right,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = FontWeight.Medium,
            textAlign = textAlign,
            textDirection = TextDirection.Rtl
        ),
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun GradeBadge(dua: Dua) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (dua.gradeLabel) {
        "Quran" -> goldContainer() to colors.onSurface
        "Mauqoof" -> colors.surfaceVariant to colors.onSurfaceVariant
        else -> colors.primaryContainer to colors.onPrimaryContainer
    }
    Surface(color = container, contentColor = content, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = localizeHadithGrade(dua.gradeLabel, LocalAppLanguage.current),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/** How many times the dua is recited, e.g. "×3". */
@Composable
internal fun RepeatChip(times: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        contentColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "×$times", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun goldContainer(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) GoldContainerDark else GoldContainerLight

// Roman Hindi vs English selection is shared with other bundled content; see bilingualContent.
// A language's own dua pack (DuaTranslations) wins; otherwise the shared Roman Hindi/English choice.
internal fun Dua.displayTitle(languageCode: String): String =
    if (languageCode == "en" || languageCode == "hinglish") title
    else textIn(languageCode)?.title ?: localizePhrase(title, languageCode)

internal fun Dua.displayMeaning(languageCode: String): DisplayMeaning =
    if (languageCode == "ur") DisplayMeaning(urduTranslation)
    else textIn(languageCode)?.meaning?.let { DisplayMeaning(it) }
        ?: bilingualContent(englishTranslation, englishMeaning, languageCode)

private fun Dua.displayContext(languageCode: String): String =
    textIn(languageCode)?.context ?: bilingualContent(context, englishContext, languageCode).text

internal fun Dua.displayBenefit(languageCode: String): String =
    textIn(languageCode)?.benefit ?: bilingualContent(benefit, englishBenefit, languageCode).text

// The badge already says "Quran", so Quranic sources show just the surah and ayah.
internal fun Dua.displaySource(): String =
    if (sourceCollection == Dua.QURAN_COLLECTION) sourceReference else "$sourceCollection • $sourceReference"

private fun Dua.shareText(language: String): String = buildString {
    val displayedTitle = displayTitle(language)
    appendLine(displayedTitle)
    if (language != "en" && displayedTitle == title) appendLine("English")
    appendLine()
    appendLine(arabicText)
    appendLine()
    appendLine(transliteration)
    appendLine()
    val meaning = displayMeaning(language)
    appendLine(meaning.text)
    meaning.fallbackLanguage?.let { appendLine(it) }
    appendLine()
    append("— $sourceCollection, $sourceReference ($authenticityGrading)")
}

private fun copyDua(context: Context, dua: Dua, language: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(dua.title, dua.shareText(language)))
    // Android 13+ shows its own confirmation when something is copied.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, localizeUiText("Copied", language), Toast.LENGTH_SHORT).show()
    }
}

private fun shareDua(context: Context, dua: Dua, language: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, dua.displayTitle(language))
        putExtra(Intent.EXTRA_TEXT, dua.shareText(language))
    }
    val chooser = Intent.createChooser(send, null)
    val activity = context.findActivity()
    if (activity == null) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    (activity ?: context).startActivity(chooser)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
