package com.example.feature.communitymasjid.ui

import com.example.ui.components.ScreenHeader
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import com.example.ui.components.AppCard
import com.example.ui.components.IconBadge
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.components.groupedShape
import com.example.ui.components.LoadingState
import com.example.ui.components.OnReconnect
import com.example.ui.localization.localizedUiText
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.LocalAppLanguage
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.model.CmCommunityRecord
import com.example.feature.communitymasjid.model.CmMosque
import com.example.feature.communitymasjid.model.CmPrayer
import com.example.feature.communitymasjid.model.CmTimings
import com.example.feature.communitymasjid.model.CmDownReason
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText

/**
 * Entry point of the isolated Community Masjid feature. Owns its ViewModel and its internal
 * list → detail back stack; the host only supplies [onBack].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMasjidScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommunityMasjidViewModel = viewModel(key = "community_masjid")
) {
    com.example.core.analytics.AnalyticsScreen("community_masjid_screen")

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showAddMosque by rememberSaveable { mutableStateOf(false) }
    var showDeleteData by rememberSaveable { mutableStateOf(false) }
    var pendingContribution by remember { mutableStateOf<(() -> Unit)?>(null) }
    val requireGuidelines: (() -> Unit) -> Unit = { action ->
        if (state.guidelinesAccepted) action() else pendingContribution = action
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        viewModel.onPermissionResult(result.values.any { it })
    }

    val language = LocalAppLanguage.current
    LaunchedEffect(Unit) { viewModel.onScreenShown() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(localizeUiText(it, language))
            viewModel.consumeMessage()
        }
    }
    // A list or detail that failed offline reloads by itself once the connection returns.
    val needsReload = state.hasLocationPermission &&
        (state.showingCachedList || state.listError != null || state.communityError != null || state.detail?.error != null)
    OnReconnect(enabled = needsReload) {
        if (state.detail != null) viewModel.refreshDetail() else viewModel.refreshList(force = true)
    }
    // Detail → list is handled here, so the app-level back handler only sees list → More.
    BackHandler(enabled = state.detail != null) { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "close_masjid_detail_back", "back"); viewModel.closeMosque() }

    Box(modifier.fillMaxSize().testTag("community_masjid_screen")) {
        val detail = state.detail
        val content: @Composable () -> Unit = {
            if (detail == null) {
                CmListContent(
                    state = state,
                    onBack = onBack,
                    onRefresh = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "refresh_masjid_list", "refresh"); viewModel.refreshList(force = true) },
                    onOpen = viewModel::openMosque,
                    onAddMosque = { requireGuidelines { showAddMosque = true } },
                    onDeleteData = { showDeleteData = true },
                    onRequestPermission = {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                )
            } else {
                CmDetailContent(state = state, detail = detail, viewModel = viewModel, requireGuidelines = requireGuidelines)
            }
        }
        if (detail == null && !state.hasLocationPermission) {
            content()
        } else {
            PullToRefreshBox(
                isRefreshing = if (detail == null) state.isLoadingList else detail.isLoading,
                onRefresh = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "pull_to_refresh", "refresh");
                    if (detail == null) viewModel.refreshList(force = true) else viewModel.refreshDetail()
                },
                modifier = Modifier.fillMaxSize()
            ) { content() }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(12.dp))
    }

    if (showAddMosque) {
        CmAddMosqueDialog(
            isSubmitting = state.isAddingMosque,
            validate = viewModel::validateNewMosque,
            onSubmit = { name, address -> viewModel.addMosque(name, address) { ok -> if (ok) showAddMosque = false } },
            onDismiss = { showAddMosque = false }
        )
    }
    pendingContribution?.let { action ->
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "dismiss_guidelines", "dismiss"); pendingContribution = null },
            title = { LocalizedText("Community guidelines") },
            text = {
                LocalizedText(
                    "Share only accurate masjid names and timings, and only when you are at the masjid. " +
                        "No offensive, political or promotional content. What you add is public and anonymous; " +
                        "wrong or abusive entries are removed by votes and reports."
                )
            },
            confirmButton = {
                Button(onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "accept_guidelines", "tap"); viewModel.acceptGuidelines(); pendingContribution = null; action() }) { LocalizedText("I agree") }
            },
            dismissButton = { TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "cancel_guidelines", "tap"); pendingContribution = null }) { LocalizedText("Cancel") } }
        )
    }
    if (showDeleteData) {
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "dismiss_delete_data", "dismiss"); showDeleteData = false },
            title = { LocalizedText("Delete my community data") },
            text = {
                LocalizedText(
                    "This removes your City challenge board entries and deletes your anonymous community account. " +
                        "Timings, votes and masjids you already shared stay as anonymous community data and are no longer linked to you. " +
                        "This cannot be undone."
                )
            },
            confirmButton = {
                Button(onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "confirm_delete_data", "tap"); showDeleteData = false; viewModel.deleteMyCommunityData() }) { LocalizedText("Delete") }
            },
            dismissButton = { TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "cancel_delete_data", "tap"); showDeleteData = false }) { LocalizedText("Cancel") } }
        )
    }
}

@Composable
private fun CmHeader(title: String, subtitle: String, onBack: () -> Unit, trailing: @Composable () -> Unit = {}) {
    ScreenHeader(title, subtitle = subtitle, onBack = onBack) { trailing() }
}

/** Occupies the same 48dp slot as the refresh button it replaces, so the header does not jump. */
@Composable
private fun CmHeaderSpinner() {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
    }
}

@Composable
private fun CmListContent(
    state: CmUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpen: (CmMosque) -> Unit,
    onAddMosque: () -> Unit,
    onDeleteData: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Spacing.screen, top = Spacing.sm, end = Spacing.screen, bottom = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            CmHeader(
                title = "Masjid",
                subtitle = "Azan & Jamaat timings shared by people at the masjid",
                onBack = onBack
            ) {
                if (state.hasLocationPermission && state.communityConfigured) {
                    IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "open_add_masjid_header", onAddMosque)) { Icon(Icons.Filled.AddLocationAlt, contentDescription = localizedUiText("Add Masjid")) }
                }
                if (state.isLoadingList) CmHeaderSpinner()
                else IconButton(onClick = onRefresh, enabled = state.hasLocationPermission) { Icon(Icons.Filled.Refresh, contentDescription = localizedUiText("Refresh")) }
                if (state.communityConfigured) {
                    IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "open_delete_data", onDeleteData), enabled = !state.isDeletingData) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = localizedUiText("Delete my community data"))
                    }
                }
            }
        }
        state.location?.let { location ->
            item {
                OutlinedButton(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "find_masjids_in_google_maps", "tap");
                        val uri = android.net.Uri.parse("https://www.google.com/maps/search/").buildUpon()
                            .appendQueryParameter("api", "1")
                            .appendQueryParameter("query", "mosques near ${location.latitude},${location.longitude}")
                            .build()
                        runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri)) }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    LocalizedText("Find nearby masjids in Google Maps", maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (!state.communityConfigured || state.communityError != null) {
            item {
                CmNoticeCard(
                    text = if (!state.communityConfigured) "Community timings are not available on this build. Nearby masjids are still shown."
                    else "Community timings could not be refreshed (${state.communityError}). Showing saved data."
                )
            }
        }
        if (state.listError != null && state.visibleMosques.isNotEmpty()) item { CmNoticeCard(state.listError) }
        if (state.showingCachedList) item { CmNoticeCard("Offline — showing the last saved list of masjids.", icon = Icons.Filled.CloudOff) }

        when {
            !state.hasLocationPermission -> item {
                CmEmptyCard(
                    icon = Icons.Filled.LocationOff,
                    text = "Location permission is needed to find masjids near you and to verify you are at the masjid.",
                    action = "Allow location",
                    onAction = onRequestPermission,
                    // A permanently denied permission can only be granted from the app's settings page.
                    secondaryAction = "Settings",
                    onSecondaryAction = {
                        runCatching {
                            context.startActivity(
                                android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(android.net.Uri.fromParts("package", context.packageName, null))
                            )
                        }
                    }
                )
            }
            state.visibleMosques.isEmpty() && state.isLoadingList -> item {
                LoadingState(message = "Finding masjids near your location…")
            }
            state.visibleMosques.isEmpty() -> item {
                // No fix at all usually means device location is switched off; offer the switch directly.
                val noFix = state.location == null
                CmEmptyCard(
                    icon = Icons.Filled.MyLocation,
                    text = state.listError ?: "No nearby masjids found within 5 km. Try refreshing or add one below.",
                    action = "Retry",
                    onAction = onRefresh,
                    secondaryAction = if (noFix) "Location" else null,
                    onSecondaryAction = {
                        runCatching { context.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                    }
                )
            }
            else -> items(state.visibleMosques, key = { it.docId }) { mosque ->
                CmMosqueCard(
                    mosque, state.records[mosque.docId],
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "open_masjid", "tap"); onOpen(mosque) },
                    modifier = Modifier.animateItem()
                )
            }
        }
        if (state.hasLocationPermission && state.communityConfigured && !(state.visibleMosques.isEmpty() && state.isLoadingList)) {
            item { CmAddMosqueCard(onAddMosque) }
        }
    }
}

@Composable
private fun CmAddMosqueCard(onAddMosque: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
        outlined = false,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.AddLocationAlt, MaterialTheme.colorScheme.primary, size = 40.dp, iconSize = 22.dp)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                LocalizedText("Masjid not in the list?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LocalizedText(
                    "Stand at the masjid and add it. Everyone nearby sees it at once.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        FilledTonalButton(
            onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "open_add_masjid_card", onAddMosque),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("cm_add_mosque")
        ) {
            Icon(Icons.Filled.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            LocalizedText("Add Masjid")
        }
    }
}

@Composable
private fun CmAddMosqueDialog(
    isSubmitting: Boolean,
    validate: (String, String) -> List<String>,
    onSubmit: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val problems = validate(name, address)
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        showErrors = true
        if (problems.isEmpty() && !isSubmitting) {
            keyboard?.hide()
            onSubmit(name, address)
        }
    }
    val firstProblem = if (showErrors) problems.firstOrNull() else null
    AlertDialog(
        onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "dismiss_add_masjid", "dismiss"); if (!isSubmitting) onDismiss() },
        icon = { Icon(Icons.Filled.AddLocationAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { LocalizedText("Add Masjid") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(Spacing.md),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    LocalizedText(
                        "Add only while you are at the masjid. Your current GPS location becomes the masjid's location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(CmTimingRules.MOSQUE_NAME_MAX) },
                    label = { LocalizedText("Masjid name") },
                    singleLine = true,
                    isError = firstProblem != null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag("cm_add_mosque_name")
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it.take(CmTimingRules.MOSQUE_ADDRESS_MAX) },
                    label = { LocalizedText("Area / address (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedVisibility(visible = firstProblem != null) {
                    firstProblem?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            LocalizedText(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "submit_add_masjid", "tap"); submit() },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else LocalizedText("Add here")
            }
        },
        dismissButton = {
            TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "cancel_add_masjid", onDismiss), enabled = !isSubmitting) { LocalizedText("Cancel") }
        }
    )
}

/** Label shared by the list card and the detail view; the same phrase the screen has always shown. */
private const val CM_LAST_UPDATED = "Last Updated"

/** Shared timings older than this are shown in the warning tone so people double-check them. */
private const val CM_STALE_AFTER_MS = 30L * 24 * 60 * 60 * 1000

/** Minutes since local midnight, for picking the next Jamaat. */
private fun cmNowMinutes(): Int {
    val now = java.util.Calendar.getInstance()
    return now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
}

/** The next daily Jamaat after [nowMinutes]; after Isha it wraps to tomorrow's first Jamaat. */
internal fun cmNextJamaat(timings: CmTimings, nowMinutes: Int): CmPrayer? {
    val daily = CmPrayer.daily.mapNotNull { prayer ->
        timings[prayer]?.jamaat?.takeIf { CmTimingRules.isValidHm(it) }?.let { prayer to CmTimingRules.minutes(it) }
    }.sortedBy { it.second }
    if (daily.isEmpty()) return null
    return (daily.firstOrNull { it.second >= nowMinutes } ?: daily.first()).first
}

/** One masjid in the list: name, distance, the next Jamaat at a glance, then the full day and freshness. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CmMosqueCard(mosque: CmMosque, record: CmCommunityRecord?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val is24h = cmIs24Hour()
    val current = record?.current
    val next = current?.let { cmNextJamaat(it, cmNowMinutes()) }
    AppCard(
        modifier = modifier.fillMaxWidth().testTag("cm_mosque_${mosque.docId}"),
        onClick = onClick,
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(Icons.Filled.Mosque, MaterialTheme.colorScheme.primary, size = 44.dp)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(mosque.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (mosque.address.isNotBlank()) {
                    Text(mosque.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (mosque.isUserAdded) {
                    LocalizedText("Added by the community", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
                if (mosque.docId.startsWith("google_")) {
                    Text("Google Maps", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                FlowRow(
                    modifier = Modifier.padding(top = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    mosque.distanceMetres?.let {
                        TonalPill(
                            cmDistance(it),
                            icon = Icons.Filled.NearMe,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (next != null) {
                        TonalPill(
                            localizedUiText(next.label) + " · " + cmFormatHm(current?.get(next)?.jamaat, is24h),
                            icon = Icons.Filled.Schedule,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.xs, top = Spacing.md)
            )
        }
        if (current != null) {
            CmJamaatStrip(current, next, is24h)
            val stale = record.updatedAtMs?.let { System.currentTimeMillis() - it > CM_STALE_AFTER_MS } == true
            val freshnessColor = if (stale) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.History, contentDescription = null, tint = freshnessColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    localizedUiText(CM_LAST_UPDATED) + ": " + cmDate(record.updatedAtMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = freshnessColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                CmVoteCount(Icons.Filled.ThumbUp, record.timingUp)
                Spacer(Modifier.width(Spacing.sm))
                CmVoteCount(Icons.Filled.ThumbDown, record.timingDown)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText(
                    if (record == null) "Tap to see community timings" else "No timings yet — tap to add them",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CmVoteCount(icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(3.dp))
        Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** The five daily Jamaat times side by side, the next one highlighted; a single line at large text sizes. */
@Composable
private fun CmJamaatStrip(timings: CmTimings, next: CmPrayer?, is24h: Boolean) {
    if (LocalDensity.current.fontScale > 1.3f) {
        Text(
            CmPrayer.daily.map { p -> localizedUiText(p.label) + " " + cmFormatHm(timings[p]?.jamaat, is24h) }.joinToString("  ·  "),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        return
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CmPrayer.daily.forEach { prayer ->
            val isNext = prayer == next
            Column(
                Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(if (isNext) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LocalizedText(
                    prayer.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    cmFormatHm(timings[prayer]?.jamaat, is24h),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CmDetailContent(
    state: CmUiState,
    detail: CmDetailState,
    viewModel: CommunityMasjidViewModel,
    requireGuidelines: (() -> Unit) -> Unit
) {
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var showReportDialog by rememberSaveable { mutableStateOf(false) }
    var showContentReport by rememberSaveable { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val proximity = viewModel.proximityTo(detail.mosque, state.location)
    val record = detail.record
    val canAct = state.communityConfigured && !detail.isSubmitting && proximity?.canAct == true

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("cm_detail"),
        contentPadding = PaddingValues(start = Spacing.screen, top = Spacing.sm, end = Spacing.screen, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            CmHeader(
                title = detail.mosque.name,
                subtitle = detail.mosque.address.ifBlank {
                    if (detail.mosque.isUserAdded) "Added by the community" else "Google Maps"
                },
                onBack = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "close_masjid_detail", viewModel::closeMosque)
            ) {
                if (detail.isLoading) CmHeaderSpinner()
                else IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "refresh_masjid_detail", viewModel::refreshDetail)) { Icon(Icons.Filled.Refresh, contentDescription = localizedUiText("Refresh")) }
                if (state.communityConfigured) {
                    IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "open_report_content", "tap"); showContentReport = true }, modifier = Modifier.testTag("cm_report_content")) {
                        Icon(Icons.Filled.Flag, contentDescription = localizedUiText("Report content"))
                    }
                }
            }
        }
        item {
            FilledTonalButton(
                onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "open_directions", "tap");
                    val mosque = detail.mosque
                    val uri = android.net.Uri.parse("https://www.google.com/maps/dir/").buildUpon()
                        .appendQueryParameter("api", "1")
                        .appendQueryParameter("destination", "${mosque.latitude},${mosque.longitude}")
                        .apply { if (mosque.docId.startsWith("google_")) appendQueryParameter("destination_place_id", mosque.docId.removePrefix("google_")) }
                        .build()
                    runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri)) }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
                LocalizedText("Directions in Google Maps", maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        item { CmProximityCard(proximity, state.isLocating, onUpdate = viewModel::refreshLocation) }
        detail.error?.let { item { CmNoticeCard(it, icon = Icons.Filled.ErrorOutline) } }

        item {
            CmSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Filled.Schedule, MaterialTheme.colorScheme.primary, size = 36.dp, iconSize = 20.dp)
                    Spacer(Modifier.width(Spacing.md))
                    LocalizedText("Current Timings", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (record?.current != null && record.timingDown > 0) {
                        CmStatusBadge("Disputed", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
                Spacer(Modifier.height(Spacing.md))
                if (record?.current != null) {
                    CmTimingTable(record.current, highlight = cmNextJamaat(record.current, cmNowMinutes()))
                    Spacer(Modifier.height(Spacing.sm))
                    CmInfoRow(CM_LAST_UPDATED, cmDate(record.updatedAtMs))
                    HorizontalDivider(Modifier.padding(vertical = Spacing.md), color = MaterialTheme.colorScheme.outlineVariant)
                    LocalizedText("Are these timings correct?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Spacing.sm))
                    CmVoteButtons(
                        up = record.timingUp,
                        down = record.timingDown,
                        myVote = detail.myVotes.timingVote,
                        enabled = canAct && !detail.isOwnTiming,
                        upLabel = "Correct",
                        downLabel = "Wrong",
                        onUp = { viewModel.voteTiming(1) },
                        onDown = { viewModel.voteTiming(-1) }
                    )
                    if (detail.isOwnTiming) {
                        Spacer(Modifier.height(Spacing.xs))
                        LocalizedText("You set these timings, so you cannot vote on them.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (detail.isLoading && record == null) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(Spacing.md))
                        LocalizedText(
                            if (detail.isLoading && record == null) "Loading…" else "No timings yet. Be the first to add them.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.md))
                Button(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "open_timings_editor", "tap"); requireGuidelines { showEditor = true } },
                    enabled = canAct,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("cm_update_timings")
                ) {
                    Icon(Icons.Filled.EditCalendar, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    LocalizedText(if (record?.current == null) "Add Timings" else "Update Timings")
                }
            }
        }

        item {
            CmSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Filled.Groups, MaterialTheme.colorScheme.primary, size = 36.dp, iconSize = 20.dp)
                    Spacer(Modifier.width(Spacing.md))
                    LocalizedText("Is this masjid really here?", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(Spacing.md))
                CmVoteButtons(
                    up = record?.upVotes ?: 0,
                    down = record?.downVotes ?: 0,
                    myVote = detail.myVotes.mosqueVote,
                    enabled = canAct && !detail.isOwnMosque,
                    upLabel = "Yes",
                    downLabel = "Report",
                    onUp = { viewModel.voteMosque(1, null) },
                    onDown = { showReportDialog = true }
                )
                Spacer(Modifier.height(Spacing.sm))
                LocalizedText(
                    if (detail.isOwnMosque) "You added this masjid, so you cannot vote on it."
                    else "It is removed from the list when ${CmTimingRules.VOTE_THRESHOLD}+ people report it and reports outnumber 👍.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            CmSectionCard(Modifier.animateContentSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Filled.History, MaterialTheme.colorScheme.primary, size = 36.dp, iconSize = 20.dp)
                    Spacer(Modifier.width(Spacing.md))
                    LocalizedText("Update History", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (detail.history == null) {
                        TextButton(
                            onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "load_timing_history", viewModel::loadHistory),
                            enabled = !detail.isHistoryLoading && state.communityConfigured,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            LocalizedText(if (detail.isHistoryLoading) "Loading…" else "Show")
                        }
                    }
                }
                val is24h = cmIs24Hour()
                detail.history?.let { history ->
                    if (history.isEmpty()) {
                        Spacer(Modifier.height(Spacing.sm))
                        LocalizedText("No updates yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    history.forEachIndexed { index, entry ->
                        val dotColor = if (entry.isRevert) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        Row(Modifier.fillMaxWidth().padding(top = if (index == 0) Spacing.md else Spacing.sm)) {
                            // A small timeline dot keeps successive versions easy to follow.
                            Box(
                                Modifier
                                    .padding(top = 5.dp, end = Spacing.md)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "v${entry.version} · ${cmDate(entry.atMs)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                LocalizedText(
                                    if (entry.isRevert) "Reverted by community votes" else "Set by ${entry.byRef}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (entry.isRevert) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(Spacing.xxs))
                                Text(
                                    entry.timings?.let { timings ->
                                        CmPrayer.entries.mapNotNull { p -> timings[p]?.let { localizedUiText(p.label) + " " + cmFormatHm(it.jamaat, is24h) } }.joinToString("  ·  ")
                                    } ?: localizedUiText("No timings"),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (index < history.lastIndex) {
                            HorizontalDivider(Modifier.padding(top = Spacing.sm, start = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.padding(horizontal = Spacing.xs), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                Spacer(Modifier.width(Spacing.sm))
                LocalizedText(
                    "Timings are shared by the community and shown at once. Add and vote only when you are at the masjid " +
                        "(within ${CmTimingRules.PRESENCE_RADIUS_METRES} m). If ${CmTimingRules.VOTE_THRESHOLD}+ people vote timings wrong " +
                        "and they outnumber 👍, the previous timings come back.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showEditor) {
        CmSuggestDialog(
            initial = record?.current,
            isSubmitting = detail.isSubmitting,
            validate = { viewModel.validate(detail.mosque, it) },
            onSubmit = { timings -> viewModel.submitTiming(timings) { ok -> if (ok) showEditor = false } },
            onDismiss = { showEditor = false }
        )
    }
    if (showReportDialog) {
        CmReportDialog(
            onReport = { reason -> showReportDialog = false; viewModel.voteMosque(-1, reason) },
            onDismiss = { showReportDialog = false }
        )
    }
    if (showContentReport) {
        CmContentReportDialog(
            hasTimings = record?.current != null,
            onReport = { target, reason -> showContentReport = false; viewModel.report(target, reason) },
            onDismiss = { showContentReport = false }
        )
    }
}

/** Moderation report, separate from votes: works anywhere and goes to cm_reports for review. */
@Composable
private fun CmContentReportDialog(hasTimings: Boolean, onReport: (target: String, reason: String) -> Unit, onDismiss: () -> Unit) {
    var target by rememberSaveable { mutableStateOf("mosque") }
    val reasons = listOf(
        "wrong_info" to "Wrong information",
        "offensive" to "Offensive or abusive",
        "spam" to "Spam or promotion",
        "other" to "Something else"
    )
    AlertDialog(
        onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "dismiss_content_report", onDismiss),
        icon = { Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { LocalizedText("Report content") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                if (hasTimings) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        FilterChip(selected = target == "mosque", onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "report_target_masjid", "tap"); target = "mosque" }, label = { LocalizedText("Masjid") })
                        FilterChip(selected = target == "timing", onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "report_target_timings", "tap"); target = "timing" }, label = { LocalizedText("Timings") })
                    }
                }
                LocalizedText("What is wrong?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                reasons.forEachIndexed { index, (key, label) ->
                    CmChoiceRow(
                        label = label,
                        shape = groupedShape(index, reasons.size, outer = 16.dp, inner = 4.dp),
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "submit_content_report", "tap", item = key); onReport(target, key) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "cancel_content_report", onDismiss)) { LocalizedText("Cancel") } }
    )
}

/** One tappable answer in a report dialog; stacked rows read as a single grouped list. */
@Composable
private fun CmChoiceRow(label: String, shape: androidx.compose.ui.graphics.Shape, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
    ) {
        Row(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            LocalizedText(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 👍 / 👎 pair; the user's own current vote is shown filled. */
@Composable
private fun CmVoteButtons(
    up: Int,
    down: Int,
    myVote: Int,
    enabled: Boolean,
    upLabel: String,
    downLabel: String,
    onUp: () -> Unit,
    onDown: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val tapUp = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onUp() }
    val tapDown = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onDown() }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        // Long translated labels ellipsize instead of pushing the count out of the button.
        val upContent: @Composable RowScope.() -> Unit = {
            Icon(Icons.Filled.ThumbUp, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            LocalizedText(upLabel, Modifier.weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("  $up", maxLines = 1)
        }
        val downContent: @Composable RowScope.() -> Unit = {
            Icon(Icons.Filled.ThumbDown, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            LocalizedText(downLabel, Modifier.weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("  $down", maxLines = 1)
        }
        val votePadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm)
        val voteModifier = Modifier.weight(1f).heightIn(min = 48.dp)
        if (myVote == 1) Button(onClick = onUp, enabled = false, modifier = voteModifier, contentPadding = votePadding) { upContent() }
        else OutlinedButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "vote_up", tapUp), enabled = enabled, modifier = voteModifier, contentPadding = votePadding) { upContent() }
        if (myVote == -1) Button(onClick = onDown, enabled = false, modifier = voteModifier, contentPadding = votePadding) { downContent() }
        else OutlinedButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "vote_down", tapDown), enabled = enabled, modifier = voteModifier, contentPadding = votePadding) { downContent() }
    }
}

@Composable
private fun CmReportDialog(onReport: (CmDownReason) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "dismiss_downvote_reason", onDismiss),
        icon = { Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { LocalizedText("Report this masjid") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                LocalizedText("What is wrong?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val reasons = CmDownReason.entries
                reasons.forEachIndexed { index, reason ->
                    CmChoiceRow(
                        label = reason.label,
                        shape = groupedShape(index, reasons.size, outer = 16.dp, inner = 4.dp),
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("community_masjid_screen", "submit_downvote_reason", "tap", item = reason.name); onReport(reason) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "cancel_downvote_reason", onDismiss)) { LocalizedText("Cancel") } }
    )
}

@Composable
private fun CmProximityCard(proximity: CmProximity?, isLocating: Boolean, onUpdate: () -> Unit) {
    val ok = proximity?.canAct == true
    AppCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        containerColor = if (ok) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        outlined = !ok,
        contentPadding = PaddingValues(start = Spacing.md, top = Spacing.md, end = Spacing.xs, bottom = Spacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                if (ok) Icons.Filled.Groups else Icons.Filled.MyLocation,
                MaterialTheme.colorScheme.primary,
                size = 40.dp,
                iconSize = 22.dp,
                filled = ok
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    when {
                        proximity == null -> "Location not verified yet"
                        else -> "You are ${cmDistance(proximity.distanceMetres.toDouble())} away (±${proximity.accuracyMetres} m)"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LocalizedText(
                    when {
                        proximity == null -> "Update your location to add timings or vote."
                        ok -> "You are at the masjid — you can add timings and vote."
                        !proximity.isAccurateEnough -> "Location is not precise enough. Move to open sky and update."
                        else -> "Go within ${CmTimingRules.PRESENCE_RADIUS_METRES} m of the masjid to add timings or vote."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isLocating) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            } else {
                TextButton(
                    onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "refresh_my_location", onUpdate),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { LocalizedText("Update") }
            }
        }
    }
}

@Composable
private fun CmSectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    AppCard(modifier = modifier.fillMaxWidth(), content = content)
}

@Composable
private fun CmInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        LocalizedText(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CmNoticeCard(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Info) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = 14.dp, vertical = Spacing.md),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(top = 1.dp).size(18.dp))
        Spacer(Modifier.width(10.dp))
        LocalizedText(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CmEmptyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    action: String,
    onAction: () -> Unit,
    secondaryAction: String? = null,
    onSecondaryAction: () -> Unit = {}
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.xxl)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            IconBadge(icon, MaterialTheme.colorScheme.primary, size = 56.dp, iconSize = 28.dp)
            Spacer(Modifier.height(Spacing.lg))
            LocalizedText(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.lg))
            // Wraps the second button under the first in long translations.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Button(
                    onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "empty_state_action", onAction),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { LocalizedText(action) }
                if (secondaryAction != null) {
                    OutlinedButton(
                        onClick = com.example.core.analytics.AppAnalytics.trackAction("community_masjid_screen", "empty_state_secondary", onSecondaryAction),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        LocalizedText(secondaryAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun cmDistance(metres: Double): String = cmFormatDistance(metres, LocalAppLocale.current)

@Composable
private fun cmDate(epochMs: Long?): String = cmFormatDate(epochMs, cmIs24Hour(), LocalAppLocale.current)
