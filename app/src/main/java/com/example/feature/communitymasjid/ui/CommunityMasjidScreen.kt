package com.example.feature.communitymasjid.ui

import com.example.ui.localization.localizedUiText
import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.model.CmCommunityRecord
import com.example.feature.communitymasjid.model.CmMosque
import com.example.feature.communitymasjid.model.CmPrayer
import com.example.feature.communitymasjid.model.CmDownReason
import com.example.ui.components.BackIconButton
import com.example.ui.localization.LocalizedText
import kotlin.math.roundToInt

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

    LaunchedEffect(Unit) { viewModel.onScreenShown() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }
    // Detail → list is handled here, so the app-level back handler only sees list → More.
    BackHandler(enabled = state.detail != null) { viewModel.closeMosque() }

    Box(modifier.fillMaxSize().testTag("community_masjid_screen")) {
        val detail = state.detail
        val content: @Composable () -> Unit = {
            if (detail == null) {
                CmListContent(
                    state = state,
                    onBack = onBack,
                    onRefresh = { viewModel.refreshList(force = true) },
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
                onRefresh = {
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
            onDismissRequest = { pendingContribution = null },
            title = { LocalizedText("Community guidelines") },
            text = {
                LocalizedText(
                    "Share only accurate masjid names and timings, and only when you are at the masjid. " +
                        "No offensive, political or promotional content. What you add is public and anonymous; " +
                        "wrong or abusive entries are removed by votes and reports."
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.acceptGuidelines(); pendingContribution = null; action() }) { LocalizedText("I agree") }
            },
            dismissButton = { TextButton(onClick = { pendingContribution = null }) { LocalizedText("Cancel") } }
        )
    }
    if (showDeleteData) {
        AlertDialog(
            onDismissRequest = { showDeleteData = false },
            title = { LocalizedText("Delete my community data") },
            text = {
                LocalizedText(
                    "This removes your City challenge board entries and deletes your anonymous community account. " +
                        "Timings, votes and masjids you already shared stay as anonymous community data and are no longer linked to you. " +
                        "This cannot be undone."
                )
            },
            confirmButton = {
                Button(onClick = { showDeleteData = false; viewModel.deleteMyCommunityData() }) { LocalizedText("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteData = false }) { LocalizedText("Cancel") } }
        )
    }
}

@Composable
private fun CmHeader(title: String, subtitle: String, onBack: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BackIconButton(onBack)
        Column(Modifier.weight(1f)) {
            LocalizedText(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LocalizedText(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        trailing()
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CmHeader(
                title = "Masjid",
                subtitle = "Azan & Jamaat timings shared by people at the masjid",
                onBack = onBack
            ) {
                if (state.hasLocationPermission && state.communityConfigured) {
                    IconButton(onClick = onAddMosque) { Icon(Icons.Filled.AddLocationAlt, contentDescription = localizedUiText("Add Masjid")) }
                }
                if (state.isLoadingList) CircularProgressIndicator(Modifier.size(22.dp).padding(end = 4.dp), strokeWidth = 2.dp)
                else IconButton(onClick = onRefresh, enabled = state.hasLocationPermission) { Icon(Icons.Filled.Refresh, contentDescription = localizedUiText("Refresh")) }
                if (state.communityConfigured) {
                    IconButton(onClick = onDeleteData, enabled = !state.isDeletingData) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = localizedUiText("Delete my community data"))
                    }
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
        if (state.showingCachedList) item { CmNoticeCard("Offline — showing the last saved list of masjids.") }

        when {
            !state.hasLocationPermission -> item {
                CmEmptyCard(
                    icon = { Icon(Icons.Filled.LocationOff, null, tint = MaterialTheme.colorScheme.primary) },
                    text = "Location permission is needed to find masjids near you and to verify you are at the masjid.",
                    action = "Allow location",
                    onAction = onRequestPermission
                )
            }
            state.visibleMosques.isEmpty() && state.isLoadingList -> item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        LocalizedText("Finding masjids near your location…")
                    }
                }
            }
            state.visibleMosques.isEmpty() -> item {
                CmEmptyCard(
                    icon = { Icon(Icons.Filled.MyLocation, null, tint = MaterialTheme.colorScheme.primary) },
                    text = state.listError ?: "No community-added masjids within 5 km yet. Add one below.",
                    action = "Retry",
                    onAction = onRefresh
                )
            }
            else -> items(state.visibleMosques, key = { it.docId }) { mosque ->
                CmMosqueCard(mosque, state.records[mosque.docId], onClick = { onOpen(mosque) })
            }
        }
        if (state.hasLocationPermission && state.communityConfigured && !(state.visibleMosques.isEmpty() && state.isLoadingList)) {
            item { CmAddMosqueCard(onAddMosque) }
        }
    }
}

@Composable
private fun CmAddMosqueCard(onAddMosque: () -> Unit) {
    CmSectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AddLocationAlt, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Masjid not in the list?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LocalizedText(
                    "Stand at the masjid and add it. Everyone nearby sees it at once.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onAddMosque, modifier = Modifier.fillMaxWidth().testTag("cm_add_mosque")) {
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
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { LocalizedText("Add Masjid") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LocalizedText(
                    "Add only while you are at the masjid. Your current GPS location becomes the masjid's location.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(CmTimingRules.MOSQUE_NAME_MAX) },
                    label = { LocalizedText("Masjid name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth().testTag("cm_add_mosque_name")
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it.take(CmTimingRules.MOSQUE_ADDRESS_MAX) },
                    label = { LocalizedText("Area / address (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showErrors) problems.firstOrNull()?.let {
                    LocalizedText(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { showErrors = true; if (problems.isEmpty()) onSubmit(name, address) },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else LocalizedText("Add here")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { LocalizedText("Cancel") }
        }
    )
}

@Composable
private fun CmMosqueCard(mosque: CmMosque, record: CmCommunityRecord?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("cm_mosque_${mosque.docId}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(mosque.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                mosque.distanceMetres?.let { Text(cmDistance(it), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
            }
            if (mosque.address.isNotBlank()) {
                Text(mosque.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (mosque.isUserAdded) {
                LocalizedText("Added by the community", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
            val current = record?.current
            val is24h = cmIs24Hour()
            if (current != null) {
                Text(
                    CmPrayer.daily.joinToString("  ·  ") { p -> "${p.label} ${cmFormatHm(current[p]?.jamaat, is24h)}" },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Updated ${cmFormatDate(record.updatedAtMs)}  ·  👍 ${record.timingUp}  👎 ${record.timingDown}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
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
private fun CmDetailContent(
    state: CmUiState,
    detail: CmDetailState,
    viewModel: CommunityMasjidViewModel,
    requireGuidelines: (() -> Unit) -> Unit
) {
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var showReportDialog by rememberSaveable { mutableStateOf(false) }
    var showContentReport by rememberSaveable { mutableStateOf(false) }
    val proximity = viewModel.proximityTo(detail.mosque, state.location)
    val record = detail.record
    val canAct = state.communityConfigured && !detail.isSubmitting && proximity?.canAct == true

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("cm_detail"),
        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CmHeader(
                title = detail.mosque.name,
                subtitle = detail.mosque.address.ifBlank {
                    if (detail.mosque.isUserAdded) "Added by the community" else "OpenStreetMap ${detail.mosque.docId.replace('_', ' ')}"
                },
                onBack = viewModel::closeMosque
            ) {
                if (detail.isLoading) CircularProgressIndicator(Modifier.size(22.dp).padding(end = 4.dp), strokeWidth = 2.dp)
                else IconButton(onClick = viewModel::refreshDetail) { Icon(Icons.Filled.Refresh, contentDescription = localizedUiText("Refresh")) }
                if (state.communityConfigured) {
                    IconButton(onClick = { showContentReport = true }, modifier = Modifier.testTag("cm_report_content")) {
                        Icon(Icons.Filled.Flag, contentDescription = localizedUiText("Report content"))
                    }
                }
            }
        }
        item { CmProximityCard(proximity, state.isLocating, onUpdate = viewModel::refreshLocation) }
        detail.error?.let { item { CmNoticeCard(it) } }

        item {
            CmSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LocalizedText("Current Timings", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (record?.current != null && record.timingDown > 0) {
                        CmStatusBadge("Disputed", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (record?.current != null) {
                    CmTimingTable(record.current)
                    Spacer(Modifier.height(8.dp))
                    CmInfoRow("Last Updated", cmFormatDate(record.updatedAtMs))
                    Spacer(Modifier.height(10.dp))
                    LocalizedText("Are these timings correct?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
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
                        LocalizedText("You set these timings, so you cannot vote on them.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LocalizedText(
                        if (detail.isLoading && record == null) "Loading…" else "No timings yet. Be the first to add them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { requireGuidelines { showEditor = true } },
                    enabled = canAct,
                    modifier = Modifier.fillMaxWidth().testTag("cm_update_timings")
                ) {
                    Icon(Icons.Filled.EditCalendar, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    LocalizedText(if (record?.current == null) "Add Timings" else "Update Timings")
                }
            }
        }

        item {
            CmSectionCard {
                LocalizedText("Is this masjid really here?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
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
                Spacer(Modifier.height(6.dp))
                LocalizedText(
                    if (detail.isOwnMosque) "You added this masjid, so you cannot vote on it."
                    else "It is removed from the list when ${CmTimingRules.VOTE_THRESHOLD}+ people report it and reports outnumber 👍.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            CmSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    LocalizedText("Update History", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (detail.history == null) {
                        TextButton(onClick = viewModel::loadHistory, enabled = !detail.isHistoryLoading && state.communityConfigured) {
                            LocalizedText(if (detail.isHistoryLoading) "Loading…" else "Show")
                        }
                    }
                }
                val is24h = cmIs24Hour()
                detail.history?.let { history ->
                    if (history.isEmpty()) {
                        LocalizedText("No updates yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    history.forEach { entry ->
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Text(
                            "v${entry.version} · ${cmFormatDate(entry.atMs)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (entry.isRevert) "Reverted by community votes" else "Set by ${entry.byRef}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.isRevert) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            entry.timings?.let { timings ->
                                CmPrayer.entries.mapNotNull { p -> timings[p]?.let { "${p.label} ${cmFormatHm(it.jamaat, is24h)}" } }.joinToString("  ·  ")
                            } ?: "No timings",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            LocalizedText(
                "Timings are shared by the community and shown at once. Add and vote only when you are at the masjid " +
                    "(within ${CmTimingRules.PRESENCE_RADIUS_METRES} m). If ${CmTimingRules.VOTE_THRESHOLD}+ people vote timings wrong " +
                    "and they outnumber 👍, the previous timings come back.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
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
        onDismissRequest = onDismiss,
        title = { LocalizedText("Report content") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (hasTimings) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = target == "mosque", onClick = { target = "mosque" }, label = { LocalizedText("Masjid") })
                        FilterChip(selected = target == "timing", onClick = { target = "timing" }, label = { LocalizedText("Timings") })
                    }
                }
                LocalizedText("What is wrong?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                reasons.forEach { (key, label) ->
                    OutlinedButton(onClick = { onReport(target, key) }, modifier = Modifier.fillMaxWidth()) { LocalizedText(label) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } }
    )
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
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val upContent: @Composable () -> Unit = {
            Icon(Icons.Filled.ThumbUp, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            LocalizedText(upLabel)
            Text("  $up")
        }
        val downContent: @Composable () -> Unit = {
            Icon(Icons.Filled.ThumbDown, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            LocalizedText(downLabel)
            Text("  $down")
        }
        if (myVote == 1) Button(onClick = onUp, enabled = false, modifier = Modifier.weight(1f)) { upContent() }
        else OutlinedButton(onClick = onUp, enabled = enabled, modifier = Modifier.weight(1f)) { upContent() }
        if (myVote == -1) Button(onClick = onDown, enabled = false, modifier = Modifier.weight(1f)) { downContent() }
        else OutlinedButton(onClick = onDown, enabled = enabled, modifier = Modifier.weight(1f)) { downContent() }
    }
}

@Composable
private fun CmReportDialog(onReport: (CmDownReason) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { LocalizedText("Report this masjid") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LocalizedText("What is wrong?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CmDownReason.entries.forEach { reason ->
                    OutlinedButton(onClick = { onReport(reason) }, modifier = Modifier.fillMaxWidth()) {
                        LocalizedText(reason.label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } }
    )
}

@Composable
private fun CmProximityCard(proximity: CmProximity?, isLocating: Boolean, onUpdate: () -> Unit) {
    val ok = proximity?.canAct == true
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (ok) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (ok) Icons.Filled.Groups else Icons.Filled.MyLocation, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
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
            if (isLocating) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else TextButton(onClick = onUpdate) { LocalizedText("Update") }
        }
    }
}

@Composable
private fun CmSectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) { content() }
    }
}

@Composable
private fun CmInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        LocalizedText(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CmNoticeCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        LocalizedText(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun CmEmptyCard(icon: @Composable () -> Unit, text: String, action: String, onAction: () -> Unit) {
    CmSectionCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Spacer(Modifier.height(8.dp))
            LocalizedText(text, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onAction) { LocalizedText(action) }
        }
    }
}

private fun cmDistance(metres: Double): String =
    if (metres < 1000) "${metres.roundToInt()} m" else "%.1f km".format(metres / 1000)
