@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.feature.citychallenge.CcArea
import com.example.feature.citychallenge.CcBoard
import com.example.feature.citychallenge.CcGoal
import com.example.feature.citychallenge.CcLocalStore
import com.example.feature.citychallenge.CcMember
import com.example.feature.citychallenge.CcProfile
import com.example.feature.citychallenge.CcRemote
import com.example.feature.citychallenge.CcScores
import com.example.feature.citychallenge.ccDetectPlace
import com.example.feature.citychallenge.weekId
import com.example.feature.citychallenge.weekStart
import com.example.feature.communitymasjid.location.CmLocationProvider
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import com.example.viewmodel.MainUiState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val Gold = Color(0xFFC9A227)
private val Silver = Color(0xFF9AA4AE)
private val Bronze = Color(0xFFB0703C)

private sealed interface BoardState {
    data object Loading : BoardState
    data class Ready(val board: CcBoard) : BoardState
    data class Failed(val message: String) : BoardState
}

private fun CcGoal.icon(): ImageVector = when (this) {
    CcGoal.SALAH -> Icons.Default.Mosque
    CcGoal.QURAN -> Icons.AutoMirrored.Filled.MenuBook
    CcGoal.DHIKR -> Icons.Default.Fingerprint
    CcGoal.HIFZ -> Icons.Default.Psychology
}

private fun fmt(n: Int): String = NumberFormat.getIntegerInstance().format(n)

/** Weekly city leaderboard: people and areas of one city, scored from the app's own logs. */
@Composable
fun CityChallengesScreen(onBack: () -> Unit, uiState: MainUiState?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { CcLocalStore(context) }
    val remote = remember { CcRemote(context) }
    val today = remember { LocalDate.now() }
    val week = remember { weekId(today) }
    var profile by remember { mutableStateOf(store.profile) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf<BoardState>(BoardState.Loading) }
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val confidentAyahs = uiState?.hifzList?.count { it.confidence >= 3 } ?: 0
    val scores = remember(uiState?.tasbeehCount, confidentAyahs, refreshKey) { store.weekScores(today, confidentAyahs) }
    val activeDays = remember(refreshKey) { store.activeDays(today) }

    LaunchedEffect(profile, refreshKey) {
        val p = profile ?: return@LaunchedEffect
        if (state !is BoardState.Ready) state = BoardState.Loading
        state = try {
            remote.publish(p, week, scores)
            store.lastSyncMs = System.currentTimeMillis()
            BoardState.Ready(remote.board(p, week, scores.points))
        } catch (e: Exception) {
            BoardState.Failed(e.message ?: "")
        }
    }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
            title = { LocalizedText("Leave the city challenge?") },
            text = { LocalizedText("Your name and this week's score will be removed from the board. Your own logs stay on your phone.") },
            confirmButton = {
                TextButton(onClick = {
                    val p = profile
                    confirmLeave = false
                    if (p != null) scope.launch { remote.leave(p.cityId, week) }
                    store.profile = null
                    profile = null
                    state = BoardState.Loading
                }) { LocalizedText("Leave", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { LocalizedText("Cancel") } }
        )
    }

    ToolScreen(DailyTool.CHALLENGES, onBack, modifier) { accent ->
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
                        if (old != null && old.cityId != newProfile.cityId) scope.launch { remote.leave(old.cityId, week) }
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

        val board = (state as? BoardState.Ready)?.board
        item(key = "hero") {
            CityHero(
                accent = accent, profile = p, today = today, scores = scores, board = board,
                loading = state is BoardState.Loading, activeDays = activeDays,
                onRefresh = { refreshKey++ }
            )
        }
        item(key = "week") { MyWeekCard(accent, scores) }
        item(key = "board") {
            when (val s = state) {
                BoardState.Loading -> ToolCard {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accent) }
                }
                is BoardState.Failed -> ErrorCard(onRetry = { refreshKey++ })
                is BoardState.Ready -> BoardCard(accent, p, s.board)
            }
        }
        item(key = "actions") {
            val invite = localizedUiText("Join me in the weekly city challenge on HIDAYAH!")
            ToolCard {
                ActionRow(Icons.Default.Share, "Invite your neighbours", "More people from your area, more points for your area.") {
                    val place = listOf(p.areaName, p.cityName).filter { it.isNotBlank() }.joinToString(", ")
                    val text = "$invite\n📍 $place"
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ActionRow(Icons.Default.EditLocationAlt, "Change city or area", "${p.cityName}${if (p.areaName.isNotBlank()) " · ${p.areaName}" else ""}") { editing = true }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ActionRow(Icons.AutoMirrored.Filled.Logout, "Leave challenge", null, danger = true) { confirmLeave = true }
            }
        }
        item(key = "privacy") {
            InfoNote("Only your display name, area and weekly totals are shared. Scores come from your Prayer tracker, Quran goal, Tasbeeh and Hifz logs, and a new board starts every Monday.")
        }
        item(key = "niyyah") { NiyyahNote() }
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
                LocalizedText("Your city, one team", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
    var name by rememberSaveable { mutableStateOf(initial?.displayName ?: suggestedName) }
    var city by rememberSaveable { mutableStateOf(initial?.cityName ?: "") }
    var area by rememberSaveable { mutableStateOf(initial?.areaName ?: "") }
    var country by rememberSaveable { mutableStateOf(initial?.countryCode ?: "") }
    var detecting by remember { mutableStateOf(false) }
    var detectFailed by remember { mutableStateOf(false) }

    fun detect() {
        detecting = true; detectFailed = false
        scope.launch {
            val fix = CmLocationProvider(context).quickLocation()
            val latLon = fix?.let { it.latitude to it.longitude } ?: fallbackLatLon
            val place = latLon?.let { ccDetectPlace(context, it.first, it.second) }
            if (place != null) {
                city = place.first; area = place.second
                country = place.third.ifBlank { Locale.getDefault().country }
            } else detectFailed = true
            detecting = false
        }
    }
    LaunchedEffect(Unit) { if (initial == null && city.isBlank()) detect() }

    ToolCard(contentPadding = PaddingValues(18.dp)) {
        SectionTitle(if (initial == null) "Join your city" else "Change city or area")
        FilledTonalButton(onClick = ::detect, enabled = !detecting, modifier = Modifier.fillMaxWidth()) {
            if (detecting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Icon(Icons.Default.MyLocation, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            LocalizedText(if (detecting) "Finding your area…" else "Use my current location")
        }
        if (detectFailed) {
            LocalizedText(
                "Couldn't find your city automatically. Please type it below.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error
            )
        }
        OutlinedTextField(
            value = city, onValueChange = { city = it.take(40) }, singleLine = true,
            label = { LocalizedText("City") }, leadingIcon = { Icon(Icons.Default.LocationCity, null) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = area, onValueChange = { area = it.take(40) }, singleLine = true,
            label = { LocalizedText("Area / Mohalla") }, leadingIcon = { Icon(Icons.Default.Place, null) },
            supportingText = { LocalizedText("Your area's team score is the total of all its members.") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = name, onValueChange = { name = it.take(24) }, singleLine = true,
            label = { LocalizedText("Display name") }, leadingIcon = { Icon(Icons.Default.Person, null) },
            supportingText = { LocalizedText("Shown on the city board. A nickname is fine.") },
            modifier = Modifier.fillMaxWidth()
        )
        val valid = name.trim().length >= 2 && city.trim().length >= 2
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (onCancel != null) OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { LocalizedText("Cancel") }
            Button(
                onClick = {
                    onSave(CcProfile(name.trim(), city.trim(), country.ifBlank { Locale.getDefault().country }, area.trim()))
                },
                enabled = valid,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.weight(1f).testTag("create_challenge")
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
private fun CityHero(
    accent: Color,
    profile: CcProfile,
    today: LocalDate,
    scores: CcScores,
    board: CcBoard?,
    loading: Boolean,
    activeDays: Int,
    onRefresh: () -> Unit
) {
    val start = weekStart(today)
    val daysLeft = ChronoUnit.DAYS.between(today, start.plusDays(6)).toInt()
    val range = "${start.format(DateTimeFormatter.ofPattern("d MMM"))} – ${start.plusDays(6).format(DateTimeFormatter.ofPattern("d MMM"))}"
    HeroCard(accent) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationCity, null, tint = accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("City challenge", style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
                }
                Text(profile.cityName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (profile.areaName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(profile.areaName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "$range · " + if (daysLeft == 0) localizedUiText("Last day") else "$daysLeft ${localizedUiText("days left")}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val rank = board?.myRank
            val total = board?.totalMembers ?: 0
            val progress = if (rank != null && total > 0) 1f - (rank - 1).toFloat() / total else 0f
            ProgressRing(progress, accent, 84.dp, stroke = 7.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (loading && board == null) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = accent)
                    else {
                        Text(if (rank != null) "#$rank" else "–", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
                        Text("/ ${fmt(total)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DayDots(activeDays, today, accent)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRefresh, enabled = !loading) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Refresh, localizedUiText("Refresh"), tint = accent)
            }
        }
    }
}

/** Seven dots for Monday..Sunday; filled up to the number of active days, today outlined. */
@Composable
private fun DayDots(activeDays: Int, today: LocalDate, accent: Color) {
    val dayIndex = today.dayOfWeek.value - 1
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(7) { i ->
                val filled = i < activeDays
                Box(
                    Modifier.size(14.dp).clip(CircleShape)
                        .background(if (filled) accent else accent.copy(alpha = 0.12f))
                        .then(if (i == dayIndex) Modifier.border(1.5.dp, accent, CircleShape) else Modifier)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("🔥 $activeDays/${dayIndex + 1} ${localizedUiText("active days")}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.07f)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(goal.icon(), null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText(goal.label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f), maxLines = 1)
                Text("+${fmt(scores.pointsOf(goal))}", style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(fmt(value), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
        OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); LocalizedText("Try again")
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
            Text("${fmt(board.totalMembers)} ${localizedUiText("members")}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf("People" to Icons.Default.Person, "Areas" to Icons.Default.Map).forEachIndexed { i, (label, icon) ->
                SegmentedButton(
                    selected = tab == i, onClick = { tab = i },
                    shape = SegmentedButtonDefaults.itemShape(i, 2),
                    icon = { SegmentedButtonDefaults.Icon(tab == i) { Icon(icon, null, Modifier.size(18.dp)) } }
                ) { LocalizedText(label) }
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = goal == null, onClick = { goalName = null },
                label = { LocalizedText("All points") }, leadingIcon = { Icon(Icons.Default.Star, null, Modifier.size(16.dp)) }
            )
            CcGoal.entries.forEach { g ->
                FilterChip(
                    selected = goal == g, onClick = { goalName = g.name },
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
        Text("${localizedUiText("Be the first in")} $city!", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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
            TextButton(onClick = { expanded = !expanded }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
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
            else if (goal == null) Text("${localizedUiText("You")} · #${board.myRank}", color = accent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
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
            val color = listOf(Gold, Silver, Bronze)[idx]
            val mine = m.uid == myUid
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(if (idx == 0) 52.dp else 44.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)),
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
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(if (mine) accent.copy(alpha = 0.14f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$rank", modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
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
        Text("${fmt(value)} $unit", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
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
                    "${localizedUiText("Your area is")} #${myIndex + 1} · ${fmt(gap + 1)} $unit ${localizedUiText("to overtake")} ${areas[myIndex - 1].name}",
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
        shape = RoundedCornerShape(14.dp),
        color = if (mine) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = if (mine) BorderStroke(1.dp, accent.copy(alpha = 0.5f)) else null
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
                                LocalizedText("Your area", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text("${a.members} ${localizedUiText("members")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${fmt(value)} $unit", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { value.toFloat() / leader },
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
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            LocalizedText(title, color = tint, fontWeight = FontWeight.Medium)
            if (!subtitle.isNullOrBlank()) {
                Text(localizedUiText(subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
