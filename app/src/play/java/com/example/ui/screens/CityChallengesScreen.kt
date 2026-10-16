@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.feature.citychallenge.CcArea
import com.example.feature.citychallenge.CcBoard
import com.example.feature.citychallenge.CcGoal
import com.example.feature.citychallenge.CcLocalStore
import com.example.feature.citychallenge.CcMember
import com.example.feature.citychallenge.CcProfile
import com.example.feature.citychallenge.CcRemote
import com.example.feature.citychallenge.CcScores
import com.example.feature.citychallenge.CcUnavailableException
import com.example.feature.citychallenge.OTHER_AREA
import com.example.feature.citychallenge.ccCleanName
import com.example.feature.citychallenge.ccDetectPlace
import com.example.feature.citychallenge.weekId
import com.example.feature.citychallenge.weekStart
import com.example.feature.communitymasjid.location.CmLocationProvider
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizedUiText
import com.example.viewmodel.MainUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val Gold = Color(0xFFC9A227)
private val Silver = Color(0xFF9AA4AE)
private val Bronze = Color(0xFFB0703C)

private const val SCREEN = "city_challenges_screen"

private sealed interface BoardState {
    data object Loading : BoardState
    data class Ready(val board: CcBoard) : BoardState
    /** [unavailable] means this build cannot reach the board at all, so retrying will not help. */
    data class Failed(val unavailable: Boolean) : BoardState
}

private fun CcGoal.icon(): ImageVector = when (this) {
    CcGoal.SALAH -> Icons.Default.Mosque
    CcGoal.QURAN -> Icons.AutoMirrored.Filled.MenuBook
    CcGoal.DHIKR -> Icons.Default.Fingerprint
    CcGoal.HIFZ -> Icons.Default.Psychology
}

@Composable
private fun fmt(n: Int): String = NumberFormat.getIntegerInstance(LocalAppLocale.current).format(n)

private fun scoreSignature(p: CcProfile, week: String, s: CcScores) =
    "${p.cityId}|$week|${p.displayName}|${p.areaKey}|${s.salah},${s.quran},${s.dhikr},${s.hifz}"

/**
 * Lays this phone's freshest totals over the server board, so a prayer or page logged a moment ago
 * shows on the board at once instead of after the next upload. The rank is recomputed only when the
 * loaded list is complete enough to know it.
 */
private fun CcBoard.withMine(profile: CcProfile, mine: CcScores): CcBoard {
    if (myUid.isBlank()) return this
    val me = CcMember(myUid, ccCleanName(profile.displayName, 24), ccCleanName(profile.areaName, 40).ifBlank { OTHER_AREA }, profile.areaKey, mine)
    val existed = members.any { it.uid == myUid }
    val merged = if (existed) members.map { if (it.uid == myUid) me else it } else members + me
    val sorted = merged.sortedByDescending { it.scores.points }
    val complete = members.size < CcRemote.TOP_LIMIT || existed
    val rank = if (complete) sorted.count { it.scores.points > mine.points } + 1 else myRank
    return copy(members = sorted, totalMembers = if (existed) totalMembers else totalMembers + 1, myRank = rank)
}

/** Weekly city leaderboard: people and areas of one city, scored from the app's own logs. */
@Composable
fun CityChallengesScreen(onBack: () -> Unit, uiState: MainUiState?, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen(SCREEN)

    val context = LocalContext.current
    val store = remember { CcLocalStore(context) }
    val remote = remember { CcRemote(context) }
    val tools = remember { context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE) }
    // The date is re-read on every resume and once a minute, so the board rolls over at local
    // midnight (and to a new week on Monday) even while the screen stays open.
    var today by remember { mutableStateOf(LocalDate.now()) }
    var logTick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { today = LocalDate.now(); logTick++ }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            val now = LocalDate.now()
            if (now != today) { today = now; logTick++ }
        }
    }
    val week = weekId(today)
    var profile by remember { mutableStateOf(store.profile) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf<BoardState>(BoardState.Loading) }
    // The last board stays on screen while a refresh runs or fails, so the list never vanishes.
    var lastBoard by remember { mutableStateOf<CcBoard?>(null) }
    var lastFetchMs by remember { mutableLongStateOf(0L) }
    var confirmLeave by rememberSaveable { mutableStateOf(false) }

    val confidentAyahs = uiState?.hifzList?.count { it.confidence >= 3 } ?: 0
    val scores = remember(uiState?.tasbeehCount, confidentAyahs, today, logTick, refreshKey) { store.weekScores(today, confidentAyahs) }
    val activeDays = remember(today, logTick, refreshKey) { store.activeDayIndices(today) }

    // A leave that failed offline is finished the next time the screen opens.
    LaunchedEffect(Unit) {
        store.pendingLeave?.let { (city, wk) -> if (remote.leave(city, wk)) store.pendingLeave = null }
    }

    // Show the cached board for this city and week immediately, before the network answers.
    LaunchedEffect(profile?.cityId, week) {
        val p = profile ?: return@LaunchedEffect
        val cached = store.cachedBoard(p.cityId, week)
        if (cached != null && (lastBoard == null || lastBoard?.weekId != week)) {
            lastBoard = cached.first; lastFetchMs = cached.second
        } else if (lastBoard?.weekId != week) lastBoard = null
    }

    LaunchedEffect(profile, week, refreshKey) {
        val p = profile ?: return@LaunchedEffect
        state = BoardState.Loading
        state = try {
            val signature = scoreSignature(p, week, scores)
            if (!store.shouldSkipPublish(signature) && remote.publish(p, week, scores)) {
                store.markPublished(signature)
                store.lastSyncMs = System.currentTimeMillis()
            }
            val board = remote.board(p, week, scores.points)
            store.cacheBoard(p.cityId, board)
            lastBoard = board; lastFetchMs = System.currentTimeMillis()
            BoardState.Ready(board)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: CcUnavailableException) {
            BoardState.Failed(unavailable = true)
        } catch (e: Exception) {
            BoardState.Failed(unavailable = false)
        }
    }

    // New local logs reach the server a few seconds later, never faster than the rules allow.
    val signatureNow = profile?.let { scoreSignature(it, week, scores) }
    var firstSignature by remember { mutableStateOf(signatureNow) }
    LaunchedEffect(signatureNow) {
        if (signatureNow == null || signatureNow == firstSignature) return@LaunchedEffect
        delay(4_000)
        while (store.shouldSkipPublish(signatureNow)) delay(5_000)
        firstSignature = signatureNow
        refreshKey++
    }
    // A board that failed to load offline reloads by itself when the connection returns.
    com.example.ui.components.OnReconnect(enabled = profile != null && state is BoardState.Failed) { refreshKey++ }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction(SCREEN, "dismiss_leave_challenge", "dismiss"); confirmLeave = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
            title = { LocalizedText("Leave the city challenge?") },
            text = { LocalizedText("Your name and this week's score will be removed from the board. Your own logs stay on your phone.") },
            confirmButton = {
                TextButton(onClick = {
                    com.example.core.analytics.AppAnalytics.interaction(SCREEN, "confirm_leave_challenge", "tap")
                    val p = profile
                    confirmLeave = false
                    // Runs outside this screen so closing it straight away cannot cancel the removal.
                    if (p != null) remote.leaveInBackground(store, p.cityId, week)
                    com.example.core.analytics.AppAnalytics.milestone("city_challenge", "left")
                    store.profile = null
                    store.clearBoardCache()
                    store.clearPublished()
                    profile = null
                    lastBoard = null
                    state = BoardState.Loading
                }) { LocalizedText("Leave", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction(SCREEN, "cancel_leave_challenge", "tap"); confirmLeave = false }) { LocalizedText("Cancel") } }
        )
    }

    val p0 = profile
    val refreshing = p0 != null && !editing && state is BoardState.Loading && lastBoard != null
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            if (p0 != null && !editing) {
                com.example.core.analytics.AppAnalytics.interaction(SCREEN, "pull_to_refresh", "refresh")
                logTick++; refreshKey++
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        ToolScreen(DailyTool.CHALLENGES, onBack, Modifier) { accent ->
            val p = profile
            if (p == null || editing) {
                item(key = "intro") { if (p == null) IntroCard(accent) }
                item(key = "setup") {
                    SetupCard(
                        accent = accent,
                        initial = p,
                        suggestedName = store.suggestedName(context),
                        fallbackLatLon = uiState?.selectedCity?.let { it.latitude to it.longitude },
                        onCancel = if (p != null) ({ editing = false }) else null,
                        onSave = { newProfile ->
                            val old = profile
                            if (old != null && old.cityId != newProfile.cityId) {
                                remote.leaveInBackground(store, old.cityId, week)
                                store.clearBoardCache()
                                lastBoard = null
                            }
                            // A changed name or area must reach the board even if the scores did not change.
                            store.clearPublished()
                            com.example.core.analytics.AppAnalytics.milestone("city_challenge", if (old == null) "joined" else "profile_changed")
                            store.profile = newProfile
                            profile = newProfile
                            editing = false
                            state = BoardState.Loading
                            refreshKey++
                        }
                    )
                }
                item(key = "niyyah") { NiyyahNote() }
                return@ToolScreen
            }

            val server = (state as? BoardState.Ready)?.board ?: lastBoard
            val board = server?.withMine(p, scores)
            val failed = state as? BoardState.Failed
            item(key = "hero") {
                CityHero(
                    accent = accent, profile = p, today = today, scores = scores, board = board,
                    loading = state is BoardState.Loading, activeDays = activeDays,
                    lastFetchMs = lastFetchMs, stale = failed != null && board != null,
                    onRefresh = { com.example.core.analytics.AppAnalytics.interaction(SCREEN, "refresh_board", "refresh"); logTick++; refreshKey++ }
                )
            }
            item(key = "quicklog") {
                QuickLogCard(accent, today, tools, onLogged = { logTick++ })
            }
            item(key = "week") { MyWeekCard(accent, scores) }
            item(key = "board") {
                when {
                    failed?.unavailable == true -> UnavailableCard()
                    board == null && failed != null -> ErrorCard(onRetry = { refreshKey++ })
                    board == null -> ToolCard {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accent) }
                    }
                    else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (failed != null) com.example.ui.components.InlineError(localizedUiText("Couldn't load the city board"), onRetry = { refreshKey++ })
                        BoardCard(accent, p, board)
                    }
                }
            }
            item(key = "actions") {
                val invite = localizedUiText("Join me in the weekly city challenge on Hidayah!")
                ToolCard {
                    ActionRow(Icons.Default.Share, localizedUiText("Invite your neighbours"), localizedUiText("More people from your area, more points for your area.")) {
                        com.example.core.analytics.AppAnalytics.interaction(SCREEN, "invite_neighbours", "tap")
                        val place = listOf(p.areaName, p.cityName).filter { it.isNotBlank() }.joinToString(", ")
                        val text = "$invite\n📍 $place"
                        runCatching {
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ActionRow(Icons.Default.EditLocationAlt, localizedUiText("Change city or area"), listOf(p.cityName, p.areaName).filter { it.isNotBlank() }.joinToString(" · ")) {
                        com.example.core.analytics.AppAnalytics.interaction(SCREEN, "change_city_or_area", "tap"); editing = true
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ActionRow(Icons.AutoMirrored.Filled.Logout, localizedUiText("Leave challenge"), null, danger = true) {
                        com.example.core.analytics.AppAnalytics.interaction(SCREEN, "open_leave_challenge", "tap"); confirmLeave = true
                    }
                }
            }
            item(key = "privacy") {
                InfoNote("Only your display name, area and weekly totals are shared. Scores come from your Prayer tracker, Quran goal, Tasbeeh and Hifz logs, and a new board starts every Monday.")
            }
            item(key = "niyyah") { NiyyahNote() }
        }
    }
}

// ---------------------------------------------------------------- Setup

@Composable
private fun IntroCard(accent: Color) {
    HeroCard(accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.LocationCity, accent, 52.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Your city, one team", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                LocalizedText(
                    "Race in good deeds with everyone in your city. Every member adds points to their area.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        LocalizedText("How points work", style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PointsChip(CcGoal.SALAH, "10 per prayer", accent)
            PointsChip(CcGoal.QURAN, "5 per page", accent)
            PointsChip(CcGoal.DHIKR, "1 per 10 dhikr", accent)
            PointsChip(CcGoal.HIFZ, "4 per ayah", accent)
        }
        Spacer(Modifier.height(10.dp))
        LocalizedText(
            "Points come only from what you log in the app. Daily and weekly limits keep the board fair.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PointsChip(goal: CcGoal, text: String, accent: Color) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(goal.icon(), null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("${localizedUiText(goal.label)} · ${localizedUiText(text)}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

private enum class DetectProblem { NO_PERMISSION, NO_FIX, NO_PLACE }

@Composable
private fun SetupCard(
    accent: Color,
    initial: CcProfile?,
    suggestedName: String,
    fallbackLatLon: Pair<Double, Double>?,
    onCancel: (() -> Unit)?,
    onSave: (CcProfile) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locator = remember { CmLocationProvider(context) }
    var name by rememberSaveable { mutableStateOf(initial?.displayName ?: suggestedName) }
    var city by rememberSaveable { mutableStateOf(initial?.cityName ?: "") }
    var area by rememberSaveable { mutableStateOf(initial?.areaName ?: "") }
    var country by rememberSaveable { mutableStateOf(initial?.countryCode ?: "") }
    var detecting by remember { mutableStateOf(false) }
    var problem by remember { mutableStateOf<DetectProblem?>(null) }
    var usedSavedCity by remember { mutableStateOf(false) }

    fun runDetect() {
        if (detecting) return
        detecting = true; problem = null; usedSavedCity = false
        scope.launch {
            val fix = if (locator.hasPermission()) locator.quickLocation() else null
            val latLon = fix?.let { it.latitude to it.longitude } ?: fallbackLatLon
            val place = latLon?.let { ccDetectPlace(context, it.first, it.second) }
            if (place != null) {
                city = place.first; area = place.second
                country = place.third.ifBlank { Locale.getDefault().country }
                usedSavedCity = fix == null
                if (fix == null && !locator.hasPermission()) problem = DetectProblem.NO_PERMISSION
            } else problem = when {
                !locator.hasPermission() -> DetectProblem.NO_PERMISSION
                latLon == null -> DetectProblem.NO_FIX
                else -> DetectProblem.NO_PLACE
            }
            detecting = false
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { runDetect() }
    fun detect() {
        if (locator.hasPermission()) runDetect()
        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    // On first open, fill the city silently from what we already know; never pop a permission prompt unasked.
    LaunchedEffect(Unit) { if (initial == null && city.isBlank()) runDetect() }

    val cleanName = ccCleanName(name, 24)
    val cleanCity = ccCleanName(city, 40)
    val nameError = name.isNotEmpty() && cleanName.length < 2
    val cityError = city.isNotEmpty() && cleanCity.length < 2
    val valid = cleanName.length >= 2 && cleanCity.length >= 2

    ToolCard(contentPadding = PaddingValues(18.dp)) {
        SectionTitle(if (initial == null) "Join your city" else "Change city or area")
        FilledTonalButton(
            onClick = com.example.core.analytics.AppAnalytics.trackAction(SCREEN, "detect_city", ::detect),
            enabled = !detecting, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            if (detecting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Icon(Icons.Default.MyLocation, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            LocalizedText(if (detecting) "Finding your area…" else "Use my current location")
        }
        val message = when (problem) {
            DetectProblem.NO_PERMISSION -> if (usedSavedCity) "Location permission is off, so we used your prayer-times city. Check it below." else "Location permission is off. Allow it, or type your city below."
            DetectProblem.NO_FIX -> "Couldn't get your location. Turn on location, or type your city below."
            DetectProblem.NO_PLACE -> "Couldn't find your city automatically. Please type it below."
            null -> if (usedSavedCity) "Filled from your prayer-times city. Check it below." else null
        }
        if (message != null) {
            LocalizedText(
                message, style = MaterialTheme.typography.bodySmall,
                color = if (problem != null && !usedSavedCity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        OutlinedTextField(
            value = city, onValueChange = { city = it.take(40) }, singleLine = true,
            label = { LocalizedText("City") }, leadingIcon = { Icon(Icons.Default.LocationCity, null) },
            isError = cityError,
            supportingText = if (cityError) ({ LocalizedText("Enter at least 2 letters.") }) else null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = area, onValueChange = { area = it.take(40) }, singleLine = true,
            label = { LocalizedText("Area / Mohalla") }, leadingIcon = { Icon(Icons.Default.Place, null) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            supportingText = { LocalizedText("Your area's team score is the total of all its members.") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = name, onValueChange = { name = it.take(24) }, singleLine = true,
            label = { LocalizedText("Display name") }, leadingIcon = { Icon(Icons.Default.Person, null) },
            isError = nameError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            supportingText = { LocalizedText(if (nameError) "Enter at least 2 letters." else "Shown on the city board. A nickname is fine.") },
            modifier = Modifier.fillMaxWidth()
        )
        if (initial != null && ccCleanName(city, 40) != initial.cityName && cleanCity.length >= 2) {
            LocalizedText(
                "Moving to a new city removes you from this week's board in your old city.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (onCancel != null) OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { LocalizedText("Cancel", maxLines = 1, overflow = TextOverflow.Ellipsis) }
            Button(
                onClick = {
                    com.example.core.analytics.AppAnalytics.interaction(SCREEN, "save_challenge_profile", "tap")
                    onSave(CcProfile(cleanName, cleanCity, country.ifBlank { Locale.getDefault().country }, ccCleanName(area, 40)))
                },
                enabled = valid && !detecting,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("create_challenge")
            ) {
                Icon(Icons.Default.Groups, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                LocalizedText(if (initial == null) "Join your city" else "Save", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun NiyyahNote() {
    InfoNote("Keep the intention for Allah alone. The board is only to encourage each other — not to show off.")
}

// ---------------------------------------------------------------- Dashboard

@Composable
private fun updatedLabel(lastFetchMs: Long): String? {
    if (lastFetchMs <= 0L) return null
    val lang = LocalAppLanguage.current
    val mins = ((System.currentTimeMillis() - lastFetchMs) / 60_000L).coerceAtLeast(0L)
    return when {
        mins < 1 -> localizedUiText("Updated just now")
        mins < 60 -> localizeUiTemplate("Updated {count} min ago", lang, mapOf("count" to "$mins"))
        mins < 48 * 60 -> localizeUiTemplate("Updated {count} h ago", lang, mapOf("count" to "${mins / 60}"))
        else -> localizeUiTemplate("Updated {count} d ago", lang, mapOf("count" to "${mins / (24 * 60)}"))
    }
}

@Composable
private fun CityHero(
    accent: Color,
    profile: CcProfile,
    today: LocalDate,
    scores: CcScores,
    board: CcBoard?,
    loading: Boolean,
    activeDays: Set<Int>,
    lastFetchMs: Long,
    stale: Boolean,
    onRefresh: () -> Unit
) {
    val lang = LocalAppLanguage.current
    val start = weekStart(today)
    val daysLeft = ChronoUnit.DAYS.between(today, start.plusDays(6)).toInt()
    val locale = LocalAppLocale.current
    // Day-month order follows the reader's language and region.
    val dayMonth = remember(locale) {
        DateTimeFormatter.ofPattern(android.text.format.DateFormat.getBestDateTimePattern(locale, "dMMM"), locale)
    }
    val range = "${start.format(dayMonth)} – ${start.plusDays(6).format(dayMonth)}"
    val left = when (daysLeft) {
        0 -> localizedUiText("Last day")
        1 -> localizedUiText("1 day left")
        else -> localizeUiTemplate("{count} days left", lang, mapOf("count" to "$daysLeft"))
    }
    HeroCard(accent) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationCity, null, tint = accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("City challenge", style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
                }
                Text(profile.cityName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.semantics { heading() })
                if (profile.areaName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(profile.areaName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("$range · $left", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(
                    progress = { (today.dayOfWeek.value / 7f).coerceIn(0f, 1f) },
                    color = accent, trackColor = accent.copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 6.dp).fillMaxWidth(0.8f).height(4.dp).clip(RoundedCornerShape(50))
                        .clearAndSetSemantics { contentDescription = "$range · $left" }
                )
            }
            val rank = board?.myRank
            val total = board?.totalMembers ?: 0
            val progress = if (rank != null && total > 0) 1f - (rank - 1).toFloat() / total else 0f
            val rankDescription = if (rank != null && total > 0)
                localizeUiTemplate("Your rank: {rank} of {total}", lang, mapOf("rank" to "$rank", "total" to "$total"))
            else localizedUiText("Rank not available yet")
            Box(Modifier.clearAndSetSemantics { contentDescription = rankDescription }) {
                ProgressRing(progress, accent, 84.dp, stroke = 7.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (loading && board == null) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = accent)
                        else {
                            Text(if (rank != null) "#${fmt(rank)}" else "–", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
                            if (total > 0) Text("/ ${fmt(total)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(fmt(scores.points), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            LocalizedText("points this week", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
        }
        NextGoalHint(accent, board, scores)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DayDots(activeDays, today, accent)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onRefresh, enabled = !loading) {
                    if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Refresh, localizedUiText("Refresh"), tint = accent)
                }
                updatedLabel(lastFetchMs)?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (stale) {
                            Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** One short line on how far the next person up the board is, so the next step is concrete. */
@Composable
private fun NextGoalHint(accent: Color, board: CcBoard?, scores: CcScores) {
    board ?: return
    val lang = LocalAppLanguage.current
    val ahead = board.members.filter { it.uid != board.myUid && it.scores.points > scores.points }.minByOrNull { it.scores.points }
    val text = when {
        board.members.size <= 1 -> return
        ahead == null -> localizedUiText("You are leading your city this week. MashaAllah!")
        else -> {
            val gap = ahead.scores.points - scores.points + 1
            val prayers = (gap + CcGoal.SALAH_POINTS - 1) / CcGoal.SALAH_POINTS
            localizeUiTemplate(
                "{points} pts to move up a place, about {prayers} prayers",
                lang, mapOf("points" to NumberFormat.getIntegerInstance(LocalAppLocale.current).format(gap), "prayers" to "$prayers")
            )
        }
    }
    Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.1f), modifier = Modifier.padding(top = 4.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TrendingUp, null, tint = accent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = accent)
        }
    }
}

/** Seven dots for Monday..Sunday: each filled on a day with anything logged, today outlined. */
@Composable
private fun DayDots(activeDays: Set<Int>, today: LocalDate, accent: Color) {
    val dayIndex = today.dayOfWeek.value - 1
    val label = localizeUiTemplate("{active}/{total} active days", LocalAppLanguage.current, mapOf("active" to "${activeDays.size}", "total" to "${dayIndex + 1}"))
    Column(Modifier.clearAndSetSemantics { contentDescription = label }) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(7) { i ->
                val filled = i in activeDays
                val future = i > dayIndex
                Box(
                    Modifier.size(14.dp).clip(CircleShape)
                        .background(
                            when {
                                filled -> accent
                                future -> accent.copy(alpha = 0.05f)
                                else -> accent.copy(alpha = 0.15f)
                            }
                        )
                        .then(if (i == dayIndex) Modifier.border(1.5.dp, accent, CircleShape) else Modifier)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("🔥 $label", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Logs today's prayers and Quran pages straight from the board. It writes the same logs the Prayer
 * tracker and Quran goal use, so every tap is idempotent there too: marking a prayed prayer again
 * changes nothing, and pages can be taken back with minus.
 */
@Composable
private fun QuickLogCard(accent: Color, today: LocalDate, tools: android.content.SharedPreferences, onLogged: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val lang = LocalAppLanguage.current
    val prayers = remember(tools) { PrayerTrackerStore(tools) }
    val quran = remember(tools) { QuranGoalStore(tools) }
    var tick by remember { mutableIntStateOf(0) }
    val day = remember(today, tick) { prayers.day(today) }
    val pages = remember(today, tick) { quran.pagesOn(today) }
    val dhikr = remember(today, tick) { tools.getInt("dhikr_$today", 0) }
    // Refresh when the screen comes back, in case the Prayer tracker changed in between.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }
    ToolCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, null, tint = accent)
            Spacer(Modifier.width(8.dp))
            SectionTitle("Log today")
        }
        LocalizedText("Tap a prayer once you have prayed it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (day.excused) {
            LocalizedText("Excused day: prayers are not expected today.", style = MaterialTheme.typography.bodySmall, color = accent)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TRACKED_PRAYERS.forEach { prayer ->
                val mark = day.marks[prayer]
                val prayed = mark?.prayed == true
                val name = localizedUiText(prayer)
                val stateText = localizedUiText(if (prayed) "Prayed" else "Not logged")
                FilterChip(
                    selected = prayed,
                    onClick = {
                        com.example.core.analytics.AppAnalytics.interaction(SCREEN, "quick_log_prayer", "tap", item = prayer)
                        // Taking back only clears a plain "on time" mark; Jama'at and Late stay as the tracker recorded them.
                        val next = when {
                            !prayed -> PrayerMark.ON_TIME
                            mark == PrayerMark.ON_TIME -> null
                            else -> mark
                        }
                        if (next != mark) {
                            prayers.setMark(today, prayer, next)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            tick++; onLogged()
                        }
                    },
                    label = { Text(name) },
                    leadingIcon = { Icon(if (prayed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.18f), selectedLeadingIconColor = accent),
                    modifier = Modifier.heightIn(min = 40.dp).semantics { contentDescription = "$name, $stateText" }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Quran pages today", style = MaterialTheme.typography.labelLarge)
                Text(
                    localizeUiTemplate("{count} pages · counts up to {cap} a day", lang, mapOf("count" to "$pages", "cap" to "${CcGoal.QURAN_DAILY_CAP}")),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            val logPages: (Int) -> Unit = { delta ->
                com.example.core.analytics.AppAnalytics.interaction(SCREEN, "quick_log_quran", "tap", item = "$delta")
                quran.log(today, delta, today)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                tick++; onLogged()
            }
            IconButton(onClick = { logPages(-1) }, enabled = pages > 0) { Icon(Icons.Default.Remove, localizedUiText("Remove one page")) }
            FilledTonalIconButton(onClick = { logPages(1) }) { Icon(Icons.Default.Add, localizedUiText("Add one page")) }
            Spacer(Modifier.width(4.dp))
            AssistChip(onClick = { logPages(5) }, label = { Text("+5") }, modifier = Modifier.semantics { contentDescription = localizeUiTemplate("Add {count} pages", lang, mapOf("count" to "5")) })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Fingerprint, null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                localizeUiTemplate("{count} dhikr today from your Tasbeeh", lang, mapOf("count" to NumberFormat.getIntegerInstance(LocalAppLocale.current).format(dhikr))),
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MyWeekCard(accent: Color, scores: CcScores) {
    ToolCard {
        SectionTitle("Your week")
        CcGoal.entries.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { goal -> GoalTile(goal, scores, accent, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun GoalTile(goal: CcGoal, scores: CcScores, accent: Color, modifier: Modifier) {
    val value = scores.of(goal)
    val full = value >= goal.weeklyCap
    val description = localizeUiTemplate(
        "{goal}: {value} of {cap} {unit}, {points} points",
        LocalAppLanguage.current,
        mapOf("goal" to localizedUiText(goal.label), "value" to "$value", "cap" to "${goal.weeklyCap}", "unit" to localizedUiText(goal.unit), "points" to "${scores.pointsOf(goal)}")
    )
    Surface(modifier = modifier.clearAndSetSemantics { contentDescription = description }, shape = MaterialTheme.shapes.medium, color = accent.copy(alpha = 0.07f), border = BorderStroke(1.dp, accent.copy(alpha = if (full) 0.5f else 0.14f))) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(goal.icon(), null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText(goal.label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("+${fmt(scores.pointsOf(goal))}", style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(fmt(value), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (full) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Verified, null, tint = accent, modifier = Modifier.size(16.dp))
                }
            }
            Text("/ ${fmt(goal.weeklyCap)} ${localizedUiText(goal.unit)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (value.toFloat() / goal.weeklyCap).coerceIn(0f, 1f) },
                color = accent, trackColor = accent.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun ErrorCard(onRetry: () -> Unit) {
    ToolCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Couldn't load the city board", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LocalizedText("Check your internet connection. Your own scores are safe on this phone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        OutlinedButton(onClick = com.example.core.analytics.AppAnalytics.trackAction(SCREEN, "retry_board", onRetry), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); LocalizedText("Try again")
        }
    }
}

@Composable
private fun UnavailableCard() {
    ToolCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("The city board is not available on this version", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LocalizedText("Your points still count on this phone. Update the app to see the board.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ---------------------------------------------------------------- Leaderboards

@Composable
private fun BoardCard(accent: Color, profile: CcProfile, board: CcBoard) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var goalName by rememberSaveable { mutableStateOf<String?>(null) }
    val goal = goalName?.let { CcGoal.valueOf(it) }
    ToolCard(contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, null, tint = Gold)
            Spacer(Modifier.width(8.dp))
            LocalizedText("Leaderboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(localizeUiTemplate("{count} members", LocalAppLanguage.current, mapOf("count" to fmt(board.totalMembers))), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf("People" to Icons.Default.Person, "Areas" to Icons.Default.Map).forEachIndexed { i, (label, icon) ->
                SegmentedButton(
                    selected = tab == i, onClick = { com.example.core.analytics.AppAnalytics.interaction(SCREEN, "select_board_tab", "tap", item = "tab_$i"); tab = i },
                    shape = SegmentedButtonDefaults.itemShape(i, 2),
                    icon = { SegmentedButtonDefaults.Icon(tab == i) { Icon(icon, null, Modifier.size(18.dp)) } }
                ) { LocalizedText(label) }
            }
        }
        Row(Modifier.horizontalScroll(com.example.core.analytics.rememberAnalyticsScrollState(SCREEN)), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = goal == null, onClick = { com.example.core.analytics.AppAnalytics.interaction(SCREEN, "filter_all_points", "tap"); goalName = null },
                label = { LocalizedText("All points") }, leadingIcon = { Icon(Icons.Default.Star, null, Modifier.size(16.dp)) }
            )
            CcGoal.entries.forEach { g ->
                FilterChip(
                    selected = goal == g, onClick = { com.example.core.analytics.AppAnalytics.interaction(SCREEN, "filter_goal", "tap", item = g.name); goalName = g.name },
                    label = { LocalizedText(g.label) }, leadingIcon = { Icon(g.icon(), null, Modifier.size(16.dp)) }
                )
            }
        }
        val unit = goal?.let { localizedUiText(it.unit) } ?: localizedUiText("pts")
        if (board.members.isEmpty()) {
            EmptyBoard(accent, profile.cityName)
        } else if (tab == 0) {
            PeopleBoard(accent, board, goal, unit)
        } else {
            AreaBoard(accent, profile, board, goal, unit)
        }
    }
}

@Composable
private fun EmptyBoard(accent: Color, city: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Groups, null, tint = accent, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(8.dp))
        Text(localizeUiTemplate("Be the first in {city}!", LocalAppLanguage.current, mapOf("city" to city)), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        LocalizedText("Invite friends from your area to start the board.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PeopleBoard(accent: Color, board: CcBoard, goal: CcGoal?, unit: String) {
    val ranked = remember(board, goal) { board.members.sortedByDescending { it.scores.of(goal) } }
    var expanded by rememberSaveable(goal) { mutableStateOf(false) }
    val shown = if (expanded) ranked.take(100) else ranked.take(10)
    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (ranked.size >= 3) {
            Podium(ranked.take(3), board.myUid, goal, unit)
            Spacer(Modifier.height(4.dp))
        }
        val startAt = if (ranked.size >= 3) 3 else 0
        shown.drop(startAt).forEachIndexed { i, m ->
            MemberRow(startAt + i + 1, m, m.uid == board.myUid, accent, m.scores.of(goal), unit)
        }
        if (ranked.size > 10) {
            TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction(SCREEN, "toggle_board_expanded", "tap"); expanded = !expanded }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                LocalizedText(if (expanded) "Show less" else "Show more")
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
            }
        }
        // Keep the user's own row in view when they are below the visible list.
        val myIndex = ranked.indexOfFirst { it.uid == board.myUid }
        if (myIndex >= shown.size || (myIndex < 0 && board.myRank != null)) {
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            val me = ranked.getOrNull(myIndex)
            if (me != null) MemberRow(myIndex + 1, me, true, accent, me.scores.of(goal), unit)
            else if (goal == null) Text("${localizedUiText("You")} · #${fmt(board.myRank ?: 0)}", color = accent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun Podium(top: List<CcMember>, myUid: String, goal: CcGoal?, unit: String) {
    // Second, first, third — the winner stands in the middle and tallest.
    val order = listOf(1 to 64.dp, 0 to 88.dp, 2 to 48.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        order.forEach { (idx, height) ->
            val m = top[idx]
            val color = themedAccent(listOf(Gold, Silver, Bronze)[idx])
            val mine = m.uid == myUid
            val spoken = localizeUiTemplate(
                "Rank {rank}, {name}, {area}, {value} {unit}", LocalAppLanguage.current,
                mapOf("rank" to "${idx + 1}", "name" to if (mine) localizedUiText("You") else m.name, "area" to m.area, "value" to "${m.scores.of(goal)}", "unit" to unit)
            )
            Column(Modifier.weight(1f).clearAndSetSemantics { contentDescription = spoken }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(if (idx == 0) 52.dp else 44.dp).clip(CircleShape).background(color.copy(alpha = 0.18f))
                        .then(if (mine) Modifier.border(2.dp, color, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text(m.name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (mine) localizedUiText("You") else m.name, style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(m.area, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier.fillMaxWidth().height(height)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(color.copy(alpha = if (mine) 0.45f else 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${idx + 1}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
                        Text("${fmt(m.scores.of(goal))} $unit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(rank: Int, m: CcMember, mine: Boolean, accent: Color, value: Int, unit: String) {
    val shape = RoundedCornerShape(12.dp)
    val spoken = localizeUiTemplate(
        "Rank {rank}, {name}, {area}, {value} {unit}", LocalAppLanguage.current,
        mapOf("rank" to "$rank", "name" to if (mine) localizedUiText("You") else m.name, "area" to m.area, "value" to "$value", "unit" to unit)
    )
    Row(
        Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = spoken }.clip(shape)
            .background(if (mine) accent.copy(alpha = 0.12f) else Color.Transparent)
            .then(if (mine) Modifier.border(1.dp, accent.copy(alpha = 0.45f), shape) else Modifier)
            .heightIn(min = 52.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(32.dp), contentAlignment = Alignment.CenterStart) {
            Text("$rank", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Text(m.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = accent)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (mine) "${m.name} (${localizedUiText("You")})" else m.name,
                fontWeight = if (mine) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(m.area, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text("${fmt(value)} $unit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (mine) accent else Color.Unspecified)
    }
}

@Composable
private fun AreaBoard(accent: Color, profile: CcProfile, board: CcBoard, goal: CcGoal?, unit: String) {
    val areas = remember(board, goal) { board.areas.sortedByDescending { it.scores.of(goal) } }
    val leader = areas.firstOrNull()?.scores?.of(goal)?.coerceAtLeast(1) ?: 1
    val myIndex = areas.indexOfFirst { it.key == profile.areaKey }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (myIndex > 0) {
            val gap = areas[myIndex - 1].scores.of(goal) - areas[myIndex].scores.of(goal)
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.1f)) {
                Text(
                    localizeUiTemplate(
                        "Your area is #{rank} · {gap} {unit} to overtake {area}", LocalAppLanguage.current,
                        mapOf("rank" to "${myIndex + 1}", "gap" to fmt(gap + 1), "unit" to unit, "area" to areas[myIndex - 1].name)
                    ),
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(10.dp)
                )
            }
        } else if (myIndex == 0 && areas.size > 1) {
            Surface(shape = RoundedCornerShape(12.dp), color = Gold.copy(alpha = 0.15f)) {
                LocalizedText("MashaAllah! Your area is leading the city.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(10.dp))
            }
        }
        areas.take(30).forEachIndexed { i, a -> AreaRow(i + 1, a, a.key == profile.areaKey, accent, goal, unit, leader) }
    }
}

@Composable
private fun AreaRow(rank: Int, a: CcArea, mine: Boolean, accent: Color, goal: CcGoal?, unit: String, leader: Int) {
    val value = a.scores.of(goal)
    val medal = when (rank) { 1 -> Gold; 2 -> Silver; 3 -> Bronze; else -> null }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (mine) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, if (mine) accent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background((medal ?: accent).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text("$rank", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = medal ?: accent) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(a.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        if (mine) {
                            Spacer(Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(50), color = accent) {
                                LocalizedText("Your area", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.surface, maxLines = 1, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text(localizeUiTemplate("{count} members", LocalAppLanguage.current, mapOf("count" to "${a.members}")), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(8.dp))
                Text("${fmt(value)} $unit", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (value.toFloat() / leader).coerceIn(0f, 1f) },
                color = medal ?: accent, trackColor = accent.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, subtitle: String?, danger: Boolean = false, onClick: () -> Unit) {
    val tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = onClick).heightIn(min = 48.dp).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, 36.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = tint, fontWeight = FontWeight.Medium)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
