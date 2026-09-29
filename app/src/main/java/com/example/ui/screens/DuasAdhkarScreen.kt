package com.example.ui.screens

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
    // Kept by id so an open dua survives rotation.
    var openDuaId by rememberSaveable { mutableStateOf<String?>(null) }
    var arabicScale by rememberSaveable { mutableFloatStateOf(1f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("duas_adhkar_screen")
    ) {
        PrimaryTabRow(selectedTabIndex = uiState.duasScreenTab.ordinal) {
            SCREEN_TABS.forEach { (tab, label) ->
                Tab(
                    selected = uiState.duasScreenTab == tab,
                    onClick = { viewModel.selectDuasScreenTab(tab) },
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
    val language = LocalAppLanguage.current
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    // "All Duas" is also the unfiltered state, so opening its full list is tracked here.
    var browsingAll by rememberSaveable { mutableStateOf(false) }
    val category = uiState.selectedDuaCategory
    val inCategory = category != DuaDatasetRepository.ALL_CATEGORY
    val searching = uiState.duaSearchQuery.isNotEmpty()
    // With nothing chosen yet, the tab opens on a picker of occasions instead of a long list.
    val showOccasions = !inCategory && !browsingAll && !searching

    // A new filter starts from its first result, not from the previous scroll position.
    LaunchedEffect(category, uiState.duaSearchQuery) {
        listState.scrollToItem(0)
    }

    // Back steps out one level: first the search, then the list, before leaving the screen.
    BackHandler(enabled = !showOccasions) {
        when {
            searching -> viewModel.searchDuas("")
            else -> {
                viewModel.selectDuaCategory(DuaDatasetRepository.ALL_CATEGORY)
                browsingAll = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.duaSearchQuery,
            onValueChange = viewModel::searchDuas,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .testTag("dua_search_field"),
            placeholder = { LocalizedText("Search duas", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.duaSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchDuas("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = localizeUiText("Clear search", language)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() })
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
                onBack = {
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
                        onClick = { onDuaClick(dua) }
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
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dua_card_${dua.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                GradeBadge(dua)
            }
            if (language != "en" && title == dua.title) {
                Text("English", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (showCategory) {
                LocalizedText(
                    text = dua.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            ArabicText(text = dua.arabicText, fontSize = 20.sp, lineHeight = 36.sp, maxLines = 2)

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = meaning.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            meaning.fallbackLanguage?.let { label ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            TextButton(onClick = onClearSearch) { LocalizedText("Clear search") }
        }
    }
}

@Composable
private fun OccasionGrid(onPick: (String) -> Unit) {
    val language = LocalAppLanguage.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("dua_occasion_grid"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            LocalizedText(
                text = "Choose an occasion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        gridItems(DuaDatasetRepository.CATEGORIES, key = { it }) { category ->
            val isAll = category == DuaDatasetRepository.ALL_CATEGORY
            val colors = MaterialTheme.colorScheme
            Card(
                onClick = { onPick(category) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAll) colors.primaryContainer else colors.surface
                ),
                border = BorderStroke(1.dp, colors.outline.copy(alpha = if (isAll) 0f else 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 118.dp)
                    .testTag("dua_occasion_${DuaDatasetRepository.CATEGORIES.indexOf(category)}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isAll) colors.primary else colors.primaryContainer,
                        contentColor = if (isAll) colors.onPrimary else colors.onPrimaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(categoryIcon(category), contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    LocalizedText(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = countLabel(DUA_COUNTS[category] ?: 0, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun DuaListHeader(category: String, count: Int, onBack: () -> Unit) {
    val language = LocalAppLanguage.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .testTag("dua_list_header")
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("dua_list_back")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeUiText("Back", language))
        }
        Icon(
            categoryIcon(category),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LocalizedText(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = countLabel(count, language),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/** A small caption naming the part of the dua below it ("Pronunciation", "Translation"). */
@Composable
private fun SectionLabel(text: String) {
    LocalizedText(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
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
    val position = siblings.indexOfFirst { it.id == dua.id }
    val scrollState = rememberScrollState()
    // Moving to the next dua starts reading it from the top.
    LaunchedEffect(dua.id) { scrollState.scrollTo(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                        Text("English", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GradeBadge(dua)
                        LocalizedText(
                            text = dua.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
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
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (dua.recommendedCount > 1) RepeatChip(dua.recommendedCount)
                        Spacer(modifier = Modifier.weight(1f))
                        val sizeLabel = localizeUiText("Text size", language)
                        IconButton(
                            onClick = { onArabicScaleChange((arabicScale - ARABIC_SCALE_STEP).coerceAtLeast(MIN_ARABIC_SCALE)) },
                            enabled = arabicScale > MIN_ARABIC_SCALE + 0.01f,
                            modifier = Modifier.testTag("dua_text_smaller")
                        ) {
                            Icon(Icons.Default.TextDecrease, contentDescription = "$sizeLabel −")
                        }
                        IconButton(
                            onClick = { onArabicScaleChange((arabicScale + ARABIC_SCALE_STEP).coerceAtMost(MAX_ARABIC_SCALE)) },
                            enabled = arabicScale < MAX_ARABIC_SCALE - 0.01f,
                            modifier = Modifier.testTag("dua_text_larger")
                        ) {
                            Icon(Icons.Default.TextIncrease, contentDescription = "$sizeLabel +")
                        }
                    }
                    SelectionContainer {
                        ArabicText(
                            text = dua.arabicText,
                            fontSize = (26 * arabicScale).sp,
                            lineHeight = (48 * arabicScale).sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            SelectionContainer {
                Column {
                    SectionLabel("Pronunciation")
                    Text(
                        text = dua.transliteration,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SectionLabel("Translation")
                    Text(
                        text = meaning.text,
                        style = MaterialTheme.typography.bodyLarge
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
                color = MaterialTheme.colorScheme.surfaceVariant
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
                    onClick = { copyDua(context, dua, language) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dua_copy_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    LocalizedText("Copy dua", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(
                    onClick = { shareDua(context, dua, language) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dua_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    LocalizedText("Share dua", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            // Step through the list the dua was opened from without closing the sheet.
            if (position >= 0 && siblings.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { onOpen(siblings[position - 1]) },
                        enabled = position > 0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dua_previous_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                        LocalizedText("Previous", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(
                        text = "${position + 1} / ${siblings.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { onOpen(siblings[position + 1]) },
                        enabled = position < siblings.lastIndex,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dua_next_button")
                    ) {
                        LocalizedText("Next", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    Surface(color = color, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
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
    textAlign: TextAlign = TextAlign.Right
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
        color = MaterialTheme.colorScheme.primary,
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
