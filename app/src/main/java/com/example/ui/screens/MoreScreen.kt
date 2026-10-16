package com.example.ui.screens

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.FilledTonalButton
import com.example.ui.components.AppSearchField
import com.example.ui.components.IconBadge
import com.example.ui.components.TonalPill
import com.example.ui.components.pressScale
import com.example.ui.localization.localizedUiText
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButton
import com.example.ui.localization.localizeUiText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedMoreSubtitle

/** Groups of rows the feature tour scrolls to and spotlights, in tour order. */
enum class MoreTourSection { DAILY_FIRST, DAILY_SECOND, EXPLORE_FIRST, EXPLORE_SECOND, EXPLORE_THIRD, SETTINGS }

private data class MoreFeature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val tag: String,
    val onClick: () -> Unit
)

@Composable
fun MoreScreen(
    onNavigateToRamadan: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToFestivals: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToNaat: () -> Unit = {},
    onNavigateToAzan: () -> Unit = {},
    onNavigateToAllahNames: () -> Unit = {},
    onNavigateToBabyNames: () -> Unit = {},
    onNavigateToProphetNames: () -> Unit = {},
    onNavigateToRabbanaDuas: () -> Unit = {},
    onNavigateToKalimas: () -> Unit = {},
    onNavigateToRecitationCollection: () -> Unit = {},
    onNavigateToBooks: () -> Unit = {},
    onNavigateToWorldLibrary: () -> Unit = {},
    onOpenDailyTool: (DailyTool) -> Unit = {},
    onStartTour: () -> Unit = {},
    tourSection: MoreTourSection? = null,
    onTourSpotlight: (Rect) -> Unit = {}
) {
    com.example.core.analytics.AnalyticsScreen("more_screen")

    val dailyTools = DailyTool.entries.map { tool ->
        MoreFeature(tool.title, tool.subtitle, tool.icon, tool.accent, "more_${tool.tag}") { onOpenDailyTool(tool) }
    }
    val features = listOf(
        MoreFeature("Qibla Direction", "Direction of the Kaaba", Icons.Default.Explore,
            Color(0xFF2E8B6E), "more_qibla", onNavigateToQibla),
        MoreFeature("Islamic Calendar", "Hijri dates & key days", Icons.Default.CalendarMonth,
            Color(0xFF3F7CAC), "more_calendar", onNavigateToCalendar),
        MoreFeature("Ramzan Timetable", "Sehri & Iftar times", Icons.Default.NightsStay,
            Color(0xFF6B5BAE), "more_ramadan", onNavigateToRamadan),
        MoreFeature("Hadith", "Sayings of the Prophet ﷺ", Icons.Default.AutoStories,
            Color(0xFFB08A2E), "more_hadith", onNavigateToHadith),
        MoreFeature("99 Names of Allah", "Asma ul Husna with meanings", Icons.Default.AutoAwesome,
            Color(0xFF4A8F5C), "more_allah_names", onNavigateToAllahNames),
        MoreFeature("99 Names of the Prophet ﷺ", "Asma un-Nabi ﷺ with meanings", Icons.Default.Stars,
            Color(0xFF2F7D5B), "more_prophet_names", onNavigateToProphetNames),
        MoreFeature("40 Rabbana Duas", "Quranic duas with meaning", Icons.Default.VolunteerActivism,
            Color(0xFF3B6FA8), "more_rabbana", onNavigateToRabbanaDuas),
        MoreFeature("Islamic Books", "Bahar-e-Shariat & more", Icons.Default.CollectionsBookmark,
            Color(0xFF8A6D1F), "more_books", onNavigateToBooks),
        MoreFeature("World Library", "Books in every language", Icons.Default.Public,
            Color(0xFF2E7D6B), "more_world_library", onNavigateToWorldLibrary),
        MoreFeature("Naat (without music)", "Voice-only recitations", Icons.Default.Headphones,
            Color(0xFF2A8C8C), "more_naat", onNavigateToNaat),
        MoreFeature("Azan of the world", "Listen & set as alert tone", Icons.Default.Campaign,
            Color(0xFF8A5A9E), "more_azan", onNavigateToAzan),
        MoreFeature("Manzil / Ruqyah / Wazaif", "Quranic recitations & daily remembrance", Icons.Default.CollectionsBookmark,
            Color(0xFF2E8B6E), "more_recitation_collection", onNavigateToRecitationCollection),
        MoreFeature("Festivals", "Islamic occasions & aamal", Icons.Default.Celebration,
            Color(0xFFC0664A), "more_festivals", onNavigateToFestivals),
        MoreFeature("6 Kalime & Iman", "Iman-e-Mujmal aur Iman-e-Mufassal", Icons.Default.AutoStories,
            Color(0xFF2E8B6E), "more_kalimas", onNavigateToKalimas),
        MoreFeature("Islamic Baby Names", "Baby names with meanings", Icons.Default.VolunteerActivism,
            Color(0xFF9A6286), "more_baby_names", onNavigateToBabyNames)
    )

    var showCredits by rememberSaveable { mutableStateOf(false) }
    var showSupport by rememberSaveable { mutableStateOf(false) }
    var showPrivacyPolicy by rememberSaveable { mutableStateOf(false) }

    val appRows = listOf(
        MoreFeature("Settings & Privacy", "Language, alerts & prayer times", Icons.Default.Settings,
            SettingsAccent, "more_settings", onOpenSettings),
        MoreFeature("App tour", "A quick guide to every feature", Icons.Default.Explore,
            SettingsAccent, "more_app_tour", onStartTour),
        MoreFeature("Privacy Policy", "What data the app uses and why", Icons.Default.PrivacyTip,
            SettingsAccent, "more_privacy_policy") { showPrivacyPolicy = true },
        MoreFeature("Sources & Credits", "Where the Quran, audio and books come from", Icons.Default.Info,
            SettingsAccent, "more_credits") { showCredits = true },
        MoreFeature("Support the Developer", "Voluntary contribution, any amount you wish", Icons.Default.VolunteerActivism,
            SettingsAccent, "more_support_developer") { showSupport = true }
    )

    // Typing in the search box swaps the grouped lists for one flat list of matches.
    val language = LocalAppLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("more_screen", query)
    val needle = if (tourSection != null) "" else query.trim().lowercase()
    val matches = if (needle.isEmpty()) emptyList() else (dailyTools + features + appRows).filter { feature ->
        feature.title.lowercase().contains(needle) ||
            feature.subtitle.lowercase().contains(needle) ||
            localizeUiText(feature.title, language).lowercase().contains(needle) ||
            localizedMoreSubtitle(feature.subtitle, language)?.lowercase()?.contains(needle) == true
    }

    // Lazy item positions: header, search, "Daily tools" label, its rows, "Explore" label, its rows, a gap, then the app rows.
    val dailyStart = 3
    val exploreStart = dailyStart + dailyTools.size + 1
    val settingsStart = exploreStart + features.size + 1
    val tourRows = when (tourSection) {
        null -> null
        MoreTourSection.DAILY_FIRST -> dailyStart..dailyStart + 3
        MoreTourSection.DAILY_SECOND -> dailyStart + 4..<dailyStart + dailyTools.size
        MoreTourSection.EXPLORE_FIRST -> exploreStart..exploreStart + 3
        MoreTourSection.EXPLORE_SECOND -> exploreStart + 4..exploreStart + 7
        MoreTourSection.EXPLORE_THIRD -> exploreStart + 8..<exploreStart + features.size
        MoreTourSection.SETTINGS -> settingsStart..settingsStart + 1
    }
    val listState = rememberLazyListState()
    com.example.core.analytics.AnalyticsScroll("more_screen", listState)
    val rowCoordinates = remember { mutableStateMapOf<Int, LayoutCoordinates>() }
    val trackRow: (Int) -> Modifier = { index ->
        Modifier.onGloballyPositioned { if (rowCoordinates[index] !== it) rowCoordinates[index] = it }
    }
    var tourSettled by remember { mutableStateOf(false) }
    LaunchedEffect(tourRows) {
        val rows = tourRows ?: return@LaunchedEffect
        if (!tourSettled) {
            // The screen scales in when it opens; bounds read before that ends would be slightly off.
            delay(320)
            tourSettled = true
        }
        listState.animateScrollToItem(rows.first)
        snapshotFlow {
            listState.firstVisibleItemScrollOffset
            if (listState.isScrollInProgress) null
            else rows.map { rowCoordinates[it]?.takeIf { c -> c.isAttached }?.boundsInRoot() }
                .takeIf { rects -> rects.none { it == null } }
                ?.filterNotNull()
                ?.reduce { a, b -> Rect(min(a.left, b.left), min(a.top, b.top), max(a.right, b.right), max(a.bottom, b.bottom)) }
        }.collect { rect -> if (rect != null) onTourSpotlight(rect) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("more_screen"),
        state = listState,
        // Extra room during the tour so even the last rows can scroll up clear of the tour card.
        contentPadding = PaddingValues(
            start = 16.dp, top = 20.dp, end = 16.dp, bottom = if (tourSection != null) 420.dp else 24.dp
        ),
        // Rows of one group sit close together so each group reads as a single block.
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    LocalizedText(
                        text = "More",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.semantics { heading() }
                    )
                    MoreSubtitle(
                        text = "Tools & resources",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                // Settings is one tap from the header as well as at the bottom of the list.
                IconButton(
                    onClick = com.example.core.analytics.AppAnalytics.trackAction("more_screen", "header_settings", onOpenSettings),
                    modifier = Modifier.testTag("more_header_settings")
                ) {
                    Icon(
                        Icons.Default.Settings,
                        // Same label as the Settings row below, so it shares that row's translation.
                        contentDescription = localizeUiText(appRows.first().title, language),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item { AppSearchField(query, { query = it }, "Search", Modifier.testTag("more_search")) }
        if (needle.isNotEmpty()) {
            item { Spacer(Modifier.height(12.dp)) }
            if (matches.isEmpty()) item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconBadge(
                        icon = Icons.Default.SearchOff,
                        accent = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 72.dp,
                        iconSize = 36.dp
                    )
                    FilledTonalButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("more_screen", "clear_feature_search", "tap"); query = "" }, modifier = Modifier.heightIn(min = 48.dp)) {
                        LocalizedText("Clear search")
                    }
                }
            }
            featureRows(matches, 0) { Modifier }
        } else {
            item { MoreSectionLabel("Daily tools", dailyTools.size) }
            featureRows(dailyTools, dailyStart, trackRow)
            item { MoreSectionLabel("Explore", features.size) }
            featureRows(features, exploreStart, trackRow)
            item { Spacer(Modifier.height(22.dp)) }
            featureRows(appRows, settingsStart, trackRow)
        }
    }
    if (showCredits) CreditsDialog(onDismiss = { showCredits = false })
    if (showSupport) SupportDeveloperDialog(onDismiss = { showSupport = false })
    if (showPrivacyPolicy) PrivacyPolicyDialog(onDismiss = { showPrivacyPolicy = false })
}

@Composable
private fun SupportDeveloperDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("more_screen", "dismiss_support_dialog", onDismiss),
        modifier = Modifier.testTag("support_developer_dialog"),
        title = { LocalizedText("Support the Developer") },
        text = {
            // Scrolls so both options stay reachable at large font sizes and in landscape.
            Column(
                modifier = Modifier.verticalScroll(com.example.core.analytics.rememberAnalyticsScrollState("more_screen")),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoreSettingsRow(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("more_screen", "support_razorpay", "tap"); openPaymentPage(context, SUPPORT_DEVELOPER_URL) },
                    title = "Razorpay",
                    subtitle = "India: UPI, cards, netbanking",
                    icon = Icons.Default.VolunteerActivism,
                    tag = "support_razorpay"
                )
                MoreSettingsRow(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("more_screen", "support_paypal", "tap"); openPaymentPage(context, SUPPORT_DEVELOPER_PAYPAL_URL) },
                    title = "PayPal",
                    subtitle = "For supporters outside India",
                    icon = Icons.Default.Public,
                    tag = "support_paypal"
                )
            }
        },
        confirmButton = { TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("more_screen", "close_support_dialog", onDismiss)) { LocalizedText("Close") } }
    )
}

/** [firstIndex] is the lazy-list position of the first row; [rowModifier] receives each row's position. */
private fun LazyListScope.featureRows(
    features: List<MoreFeature>,
    firstIndex: Int,
    rowModifier: (Int) -> Modifier
) {
    itemsIndexed(features, key = { _, feature -> feature.tag }) { index, feature ->
        MoreFeatureRow(
            feature = feature,
            first = index == 0,
            last = index == features.lastIndex,
            modifier = rowModifier(firstIndex + index)
        )
    }
}

@Composable
private fun MoreSectionLabel(text: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LocalizedText(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.width(8.dp))
        // How many entries the group holds, so the length of the list is clear before scrolling.
        TonalPill(text = count.toString())
    }
}

/** Mid-tone accents are lifted toward white on dark surfaces so icons keep their contrast. */
@Composable
internal fun themedAccent(accent: Color): Color =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) lerp(accent, Color.White, 0.3f) else accent

@Composable
private fun MoreSubtitle(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    // Subtitles are hidden, not shown in English, for languages without a reviewed translation.
    val localized = localizedMoreSubtitle(text, LocalAppLanguage.current) ?: return
    Text(
        text = localized,
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

private val SettingsAccent = Color(0xFF14614B)
private val GroupOuterCorner = 22.dp
private val GroupInnerCorner = 6.dp

/** One feature per row: icon, name, a short line saying what it does, and a chevron. */
@Composable
private fun MoreFeatureRow(feature: MoreFeature, first: Boolean, last: Boolean, modifier: Modifier = Modifier) {
    val top = if (first) GroupOuterCorner else GroupInnerCorner
    val bottom = if (last) GroupOuterCorner else GroupInnerCorner
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = com.example.core.analytics.AppAnalytics.trackItemAction("more_screen", "open_feature", feature.tag, feature.onClick),
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.985f)
            .testTag(feature.tag),
        shape = RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom),
        color = MaterialTheme.colorScheme.surface,
        interactionSource = interaction
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = feature.icon, accent = feature.accent, size = 42.dp, iconSize = 22.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                MoreSubtitle(
                    text = feature.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                // The whole row is the button; announcing the chevron as well is noise for TalkBack.
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MoreSettingsRow(
    onClick: () -> Unit,
    title: String = "Settings & Privacy",
    subtitle: String = "Language, alerts & prayer times",
    icon: ImageVector = Icons.Default.Settings,
    tag: String = "more_settings",
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .testTag(tag),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = icon, accent = MaterialTheme.colorScheme.primary, size = 44.dp, iconSize = 22.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                MoreSubtitle(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                // The whole card is the button; announcing the chevron as well is noise for TalkBack.
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
