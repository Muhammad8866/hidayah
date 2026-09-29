@file:OptIn(ExperimentalLayoutApi::class)

package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.domain.model.AdhkarDatasetRepository
import com.example.domain.model.PrayerType
import com.example.domain.model.PredefinedLocations
import com.example.prayer.AstronomicalPrayerCalculator
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.formatHijriDate
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.LiveIndicatorGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DuasScreenTab
import com.example.viewmodel.LocationIssue
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/** Today's personal progress shown on Home. Read-only snapshot of the tracker, Quran goal and adhkar. */
@Immutable
internal data class TodayProgress(
    val salahPrayed: Int,
    val salahTotal: Int,
    val quranPagesRead: Int,
    val quranPagesTarget: Int,
    val adhkarDone: Int,
    val adhkarTotal: Int,
    val adhkarIsMorning: Boolean
)

/** Callbacks for everything Home can open, so [HomeContent] stays stateless and previewable. */
@Immutable
internal data class HomeActions(
    val onContinueQuran: () -> Unit = {},
    val onOpenAdhkar: () -> Unit = {},
    val onOpenTasbeeh: () -> Unit = {},
    val onOpenQibla: () -> Unit = {},
    val onOpenHadith: () -> Unit = {},
    val onOpenCalendar: () -> Unit = {},
    val onOpenRamadan: () -> Unit = {},
    val onOpenNaat: () -> Unit = {},
    val onOpenZakat: () -> Unit = {},
    val onOpenPrayerDetails: () -> Unit = {},
    val onOpenPrayerTracker: () -> Unit = {},
    val onOpenQuranGoal: () -> Unit = {},
    val onRetryPrayerTimes: () -> Unit = {},
    val onFixLocation: () -> Unit = {},
    val onEnablePrayerAlerts: () -> Unit = {},
    val onDismissPrayerAlertPrompt: () -> Unit = {}
)

@Composable
fun HomeScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onNavigateToQuran: () -> Unit,
    onNavigateToDuas: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onOpenPrayerTransparency: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDailyTool: (DailyTool) -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToRamadan: () -> Unit = {},
    onNavigateToNaat: () -> Unit = {}
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showCityPicker by rememberSaveable { mutableStateOf(false) }
    val prefs = remember { com.example.prayer.PrayerSettingsStore.prefs(context) }
    var alertPromptDismissed by remember { mutableStateOf(prefs.getBoolean(ALERT_PROMPT_DISMISSED_KEY, false)) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted && notificationPermissionPermanentlyDenied(context)) openAppNotificationSettings(context)
        // When denied this reports why alerts stayed off.
        viewModel.setNotificationsEnabled(true)
    }

    // Tracker and Quran goal live in SharedPreferences; re-read them whenever Home is resumed.
    var resumeCount by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { resumeCount++ }
    val progress = remember(resumeCount, uiState.adhkarProgress, uiState.currentPrayer, uiState.hijriDate) {
        readTodayProgress(context, uiState)
    }

    // Notification feedback is transient, so it is announced as a snackbar instead of a card.
    val feedback = uiState.notificationFeedbackMessage
    LaunchedEffect(feedback) {
        if (feedback != null) {
            snackbarHostState.showSnackbar(
                message = localizeUiText(feedback, language),
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            viewModel.clearNotificationFeedback()
        }
    }

    val actions = remember(viewModel, onNavigateToQuran, onNavigateToDuas, onNavigateToQibla,
        onOpenPrayerTransparency, onOpenDailyTool, onNavigateToHadith, onNavigateToCalendar,
        onNavigateToRamadan, onNavigateToNaat, uiState.locationIssue) {
        // Each Home entry point lands on the matching Duas sub-tab, not whichever was open last.
        val openDuasTab = { tab: DuasScreenTab ->
            viewModel.selectDuasScreenTab(tab)
            onNavigateToDuas()
        }
        HomeActions(
            onContinueQuran = onNavigateToQuran,
            onOpenAdhkar = { openDuasTab(DuasScreenTab.ADHKAR) },
            onOpenTasbeeh = { openDuasTab(DuasScreenTab.TASBEEH) },
            onOpenQibla = onNavigateToQibla,
            onOpenHadith = onNavigateToHadith,
            onOpenCalendar = onNavigateToCalendar,
            onOpenRamadan = onNavigateToRamadan,
            onOpenNaat = onNavigateToNaat,
            onOpenZakat = { onOpenDailyTool(DailyTool.ZAKAT) },
            onOpenPrayerDetails = onOpenPrayerTransparency,
            onOpenPrayerTracker = { onOpenDailyTool(DailyTool.PRAYER_TRACKER) },
            onOpenQuranGoal = { onOpenDailyTool(DailyTool.QURAN_GOAL) },
            onRetryPrayerTimes = viewModel::recalculatePrayerTimes,
            onFixLocation = { showCityPicker = true },
            onEnablePrayerAlerts = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else viewModel.setNotificationsEnabled(true)
            },
            onDismissPrayerAlertPrompt = {
                prefs.edit().putBoolean(ALERT_PROMPT_DISMISSED_KEY, true).apply()
                alertPromptDismissed = true
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeContent(
            uiState = uiState,
            progress = progress,
            actions = actions,
            showPrayerAlertPrompt = !alertPromptDismissed && uiState.hasConfirmedLocation && !uiState.isNotificationEnabled
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("notification_feedback_snackbar")
        )
    }
    if (showCityPicker) {
        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            title = { LocalizedText("Choose your city") },
            text = {
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(PredefinedLocations.CITIES.size, key = { i -> PredefinedLocations.CITIES[i].let { "${it.name}|${it.stateOrCountry}" } }) { index ->
                        val city = PredefinedLocations.CITIES[index]
                        TextButton(
                            onClick = {
                                viewModel.selectCity(city)
                                showCityPicker = false
                            },
                            modifier = Modifier.fillMaxWidth().testTag("city_option_${city.name.lowercase()}")
                        ) { LocalizedText("${city.name}, ${city.stateOrCountry}") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCityPicker = false
                    openLocationSettings(context, uiState.locationIssue)
                }) { LocalizedText("Use device location") }
            },
            dismissButton = {
                TextButton(onClick = { showCityPicker = false }) { LocalizedText("Cancel") }
            }
        )
    }
}

@Composable
internal fun HomeContent(
    uiState: MainUiState,
    progress: TodayProgress,
    actions: HomeActions,
    modifier: Modifier = Modifier,
    showPrayerAlertPrompt: Boolean = false
) {
    val hasError = !uiState.hasConfirmedLocation || uiState.prayerCalculationError != null
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .testTag("home_screen_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        item(key = "header") { HomeHeader(uiState, onFixLocation = actions.onFixLocation) }

        item(key = "hero") {
            if (hasError) {
                PrayerTimesErrorCard(
                    locationIssue = uiState.locationIssue,
                    needsLocation = !uiState.hasConfirmedLocation,
                    onRetry = actions.onRetryPrayerTimes,
                    onFixLocation = actions.onFixLocation,
                    onOpenDetails = actions.onOpenPrayerDetails
                )
            } else {
                NextPrayerCard(uiState = uiState, onOpenPrayerDetails = actions.onOpenPrayerDetails)
            }
        }

        if (showPrayerAlertPrompt) {
            item(key = "prayer_alert_prompt") {
                PrayerAlertPromptCard(
                    onEnable = actions.onEnablePrayerAlerts,
                    onDismiss = actions.onDismissPrayerAlertPrompt
                )
            }
        }

        // Keep the four daily actions close to the next prayer without burying the schedule.
        item(key = "quick_actions") { QuickActionsSection(uiState, actions) }

        // The error card above already explains missing times, so no second "unavailable" state here.
        val showTimes = !hasError && uiState.rawPrayerTimes != null
        if (showTimes) {
            item(key = "schedule") { PrayerScheduleCard(uiState, onOpenPrayerDetails = actions.onOpenPrayerDetails) }
        }

        item(key = "progress") {
            TodayProgressSection(
                progress = progress,
                onOpenSalah = actions.onOpenPrayerTracker,
                onOpenQuran = actions.onOpenQuranGoal,
                onOpenAdhkar = actions.onOpenAdhkar
            )
        }

        if (showTimes) {
            item(key = "other_times") { OtherIslamicTimesCard(uiState) }
        }
    }
}

// region Header

@Composable
private fun HomeHeader(uiState: MainUiState, onFixLocation: () -> Unit) {
    val locale = LocalAppLocale.current
    val language = LocalAppLanguage.current
    val rtl = isRtlAppLanguage(language)
    // Gregorian date in the app language; the formatter localizes month and weekday names itself.
    val gregorianDate = remember(locale, uiState.selectedCity.timeZoneId, uiState.hijriDate) {
        SimpleDateFormat("EEE, d MMM", locale)
            .apply { timeZone = TimeZone.getTimeZone(uiState.selectedCity.timeZoneId) }
            .format(Date())
    }
    val hijri = formatHijriDate(uiState.hijriDate, language).let { if (rtl) it else ltr(it) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // FlowRow lets a long city name drop below the greeting instead of being cut off.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LocalizedText(
                text = "السَّلَامُ عَلَيْكُمْ",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = ArabicFontFamily,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 12.dp)
                    .semantics { heading() }
            )
            LocationChip(
                uiState = uiState,
                onClick = onFixLocation,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        FlowRow {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 6.dp)
                    .size(16.dp)
            )
            LocalizedText(
                text = hijri,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "  •  " + ltr(gregorianDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // The Hijri day begins at Maghrib: after sunset show the date that has started for tonight.
        uiState.hijriDateTonight?.let { tonight ->
            LocalizedText(
                text = "Since Maghrib: " + formatHijriDate(tonight, language).let { if (rtl) it else ltr(it) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 22.dp)
            )
        }
    }
}

@Composable
private fun LocationChip(uiState: MainUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val issue = uiState.locationIssue
    val shape = RoundedCornerShape(50)
    val label = if (!uiState.hasConfirmedLocation) "Choose city" else when (issue) {
        LocationIssue.PERMISSION_DENIED -> "Allow location"
        LocationIssue.SERVICES_OFF -> "Turn on location"
        else -> uiState.selectedCity.name
    }
    Row(
        modifier = modifier
            .widthIn(max = 260.dp)
            // Only tappable when location needs fixing, which is when it needs a full touch target.
            .heightIn(min = if (issue != null || !uiState.hasConfirmedLocation) 48.dp else 36.dp)
            .clip(shape)
            .background(
                if (issue != null) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (issue != null) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = shape
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("city_selector_button"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (issue != null) Icons.Default.LocationOff else Icons.Default.LocationOn,
            contentDescription = localizedUiText("Location"),
            tint = if (issue != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        LocalizedText(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (issue != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (uiState.isLiveLocation && issue == null) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(LiveIndicatorGreen)
            )
        }
    }
}

// endregion

// region Today progress

@Composable
private fun TodayProgressSection(
    progress: TodayProgress,
    onOpenSalah: () -> Unit,
    onOpenQuran: () -> Unit,
    onOpenAdhkar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_progress"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HomeSectionTitle("Today's progress", Modifier.padding(horizontal = 16.dp))
        // One full-width row per habit: room for plain words instead of three cramped tiles.
        HomeCard(tag = "today_progress_card") {
            ProgressRow(
                label = "Salah",
                icon = Icons.Default.Mosque,
                accent = Color(0xFF237A63),
                value = "${progress.salahPrayed}/${progress.salahTotal}",
                detail = "prayed",
                fraction = progress.salahPrayed.toFloat() / progress.salahTotal.coerceAtLeast(1),
                onClick = onOpenSalah,
                tag = "progress_salah"
            )
            ProgressDivider()
            ProgressRow(
                label = "Quran",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                accent = Color(0xFF3C6FA8),
                value = "${progress.quranPagesRead}/${progress.quranPagesTarget}",
                detail = "pages",
                fraction = progress.quranPagesRead.toFloat() / progress.quranPagesTarget.coerceAtLeast(1),
                onClick = onOpenQuran,
                tag = "progress_quran"
            )
            ProgressDivider()
            ProgressRow(
                label = "Adhkar",
                icon = if (progress.adhkarIsMorning) Icons.Default.WbTwilight else Icons.Default.NightsStay,
                accent = Color(0xFFA9812A),
                value = "${progress.adhkarDone}/${progress.adhkarTotal}",
                detail = if (progress.adhkarIsMorning) "Morning" else "Evening",
                fraction = progress.adhkarDone.toFloat() / progress.adhkarTotal.coerceAtLeast(1),
                onClick = onOpenAdhkar,
                tag = "progress_adhkar"
            )
        }
    }
}

@Composable
private fun ProgressDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    )
}

@Composable
private fun ProgressRow(
    label: String,
    icon: ImageVector,
    accent: Color,
    value: String,
    detail: String,
    fraction: Float,
    onClick: () -> Unit,
    tag: String
) {
    val done = fraction >= 1f
    val tint = themedAccent(accent)
    val a11yLabel = listOfNotNull(
        localizedUiText(label), value, localizedUiText(detail), if (done) localizedUiText("Done") else null
    ).joinToString(", ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) { contentDescription = a11yLabel }
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (done) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LocalizedText(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Row(Modifier.align(Alignment.CenterVertically), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ltr(value),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = tint,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(4.dp))
                    LocalizedText(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            LinearProgressIndicator(
                progress = { fraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = tint,
                // A tinted track stays visible on dark surfaces, where surfaceVariant nearly vanishes.
                trackColor = tint.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
        Spacer(Modifier.width(8.dp))
        if (done) {
            LocalizedText(
                text = "Done",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(tint.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        } else {
            // The chevron tells first-time users the row opens something; it mirrors in RTL.
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// endregion

// region Quick actions

private data class HomeShortcut(
    val title: String,
    val subtitle: String?,
    val icon: ImageVector,
    val accent: Color,
    val tag: String,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionsSection(uiState: MainUiState, actions: HomeActions) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    // The reader opens on page 1 for a new user, so only show a page once reading has started.
    val quranContext = uiState.quranPageNumber.takeIf { it > 1 }?.let { "Page $it" }
    val dailyShortcuts = listOf(
        HomeShortcut("Quran", quranContext, Icons.AutoMirrored.Filled.MenuBook, Color(0xFF237A63),
            "action_continue_quran", actions.onContinueQuran),
        HomeShortcut("Adhkar", null, Icons.Default.WbTwilight, Color(0xFFA9812A),
            "action_adhkar", actions.onOpenAdhkar),
        HomeShortcut("Qibla", null, Icons.Default.Explore, Color(0xFF2A7FA8), "action_qibla", actions.onOpenQibla),
        HomeShortcut("Tasbeeh", null, Icons.Default.TouchApp, Color(0xFFB0507A), "action_tasbeeh", actions.onOpenTasbeeh)
    )
    val moreShortcuts = listOf(
        HomeShortcut("Hadith", null, Icons.Default.AutoStories, Color(0xFFB08A2E), "action_hadith", actions.onOpenHadith),
        HomeShortcut("Calendar", null, Icons.Default.CalendarMonth, Color(0xFF3F5FAC), "action_calendar", actions.onOpenCalendar),
        HomeShortcut("Ramzan", null, Icons.Default.NightsStay, Color(0xFF6B5BAE), "action_ramadan", actions.onOpenRamadan),
        HomeShortcut("Naat", null, Icons.Default.Headphones, Color(0xFF2A8C8C), "action_naat", actions.onOpenNaat),
        HomeShortcut("Zakat", null, Icons.Default.VolunteerActivism, Color(0xFFC0664A), "action_zakat", actions.onOpenZakat)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("quick_actions"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HomeSectionTitle("Quick access", Modifier.weight(1f))
            TextButton(
                onClick = { showAll = !showAll },
                modifier = Modifier.heightIn(min = 48.dp).testTag("quick_actions_toggle")
            ) {
                LocalizedText(if (showAll) "Show less" else "View all", fontWeight = FontWeight.SemiBold)
                Icon(
                    imageVector = if (showAll) Icons.Default.ExpandLess else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        // Two columns leave enough width for translated labels and large text settings.
        (dailyShortcuts + if (showAll) moreShortcuts else emptyList()).chunked(2).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (row.size == 1) {
                    QuickActionTile(row.first(), Modifier.fillMaxWidth())
                } else {
                    row.forEach { shortcut ->
                        QuickActionTile(shortcut, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(shortcut: HomeShortcut, modifier: Modifier = Modifier) {
    val tint = themedAccent(shortcut.accent)
    Card(
        onClick = shortcut.onClick,
        modifier = modifier
            .heightIn(min = 72.dp)
            .testTag(shortcut.tag),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = shortcut.icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    text = shortcut.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (shortcut.subtitle != null) LocalizedText(
                    text = shortcut.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    maxLines = 1
                )
            }
        }
    }
}

// endregion

@Composable
internal fun HomeSectionTitle(text: String, modifier: Modifier = Modifier) {
    LocalizedText(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .padding(start = 4.dp)
            .semantics { heading() }
    )
}

/** Wraps a Latin-script run in a first-strong isolate so bidi reordering leaves it intact. */
internal fun ltr(text: String): String = "⁨$text⁩"

private fun openLocationSettings(context: android.content.Context, issue: LocationIssue?) {
    val intent = when (issue) {
        LocationIssue.PERMISSION_DENIED -> Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        else -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Evening adhkar are recited from Asr onwards, morning adhkar from Fajr; mirrors the Adhkar tab. */
private fun isMorningAdhkar(currentPrayer: PrayerType) =
    currentPrayer !in setOf(PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA)

private fun readTodayProgress(context: android.content.Context, uiState: MainUiState): TodayProgress {
    val today = LocalDate.now()
    val prefs = context.getSharedPreferences("daily_tools", android.content.Context.MODE_PRIVATE)
    val salah = PrayerTrackerStore(prefs).day(today)
    val quran = quranGoalStore(context)
    val morning = isMorningAdhkar(uiState.currentPrayer)
    val session = if (morning) "morning" else "evening"
    val adhkar = if (morning) AdhkarDatasetRepository.MORNING_ADHKAR else AdhkarDatasetRepository.EVENING_ADHKAR
    return TodayProgress(
        salahPrayed = salah.prayed,
        salahTotal = TRACKED_PRAYERS.size,
        quranPagesRead = quran.pagesOn(today),
        quranPagesTarget = quran.targetOn(today, today),
        adhkarDone = adhkar.count { (uiState.adhkarProgress["$session:${it.id}"] ?: 0) >= it.recommendedCount },
        adhkarTotal = adhkar.size,
        adhkarIsMorning = morning
    )
}

// region Previews

private val previewProgress = TodayProgress(
    salahPrayed = 3, salahTotal = 5,
    quranPagesRead = 1, quranPagesTarget = 2,
    adhkarDone = 18, adhkarTotal = 18, adhkarIsMorning = true
)

private fun previewState(): MainUiState {
    val state = MainUiState()
    val city = state.selectedCity
    val today = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId))
    val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    return state.copy(
        hasConfirmedLocation = true,
        rawPrayerTimes = AstronomicalPrayerCalculator.calculateTimes(today, city.latitude, city.longitude, state.prayerConfig),
        tomorrowPrayerTimes = AstronomicalPrayerCalculator.calculateTimes(tomorrow, city.latitude, city.longitude, state.prayerConfig),
        currentPrayer = PrayerType.DHUHR,
        nextPrayer = PrayerType.ASR,
        nextPrayerTimeFormatted = "4:32 PM",
        minutesUntilNextPrayer = 83,
        quranPageNumber = 45
    )
}

@Preview(name = "Home · light", showBackground = true, heightDp = 1400)
@Composable
private fun HomeContentPreview() {
    MyApplicationTheme(darkTheme = false) {
        HomeContent(uiState = previewState(), progress = previewProgress, actions = HomeActions())
    }
}

@Preview(name = "Home · dark, large font", showBackground = true, heightDp = 1600, fontScale = 1.5f)
@Composable
private fun HomeContentDarkPreview() {
    MyApplicationTheme(darkTheme = true) {
        HomeContent(uiState = previewState(), progress = previewProgress, actions = HomeActions())
    }
}

@Preview(name = "Home · error", showBackground = true, heightDp = 900)
@Composable
private fun HomeContentErrorPreview() {
    MyApplicationTheme {
        HomeContent(
            uiState = MainUiState(
                prayerCalculationError = "Prayer times could not be calculated.",
                locationIssue = LocationIssue.PERMISSION_DENIED
            ),
            progress = previewProgress.copy(salahPrayed = 0, adhkarDone = 0),
            actions = HomeActions()
        )
    }
}

// endregion

private const val ALERT_PROMPT_DISMISSED_KEY = "home_prayer_alert_prompt_dismissed"

/** Invites users to turn on prayer alerts from Home; otherwise they live only in More > Settings. */
@Composable
private fun PrayerAlertPromptCard(onEnable: () -> Unit, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("prayer_alert_prompt"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Filled.NotificationsActive, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    "Get prayer time alerts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                LocalizedText(
                    "A gentle notification when each prayer time begins.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onEnable, modifier = Modifier.testTag("prayer_alert_prompt_enable")) {
                        LocalizedText("Turn on alerts")
                    }
                    TextButton(onClick = onDismiss) { LocalizedText("Not now") }
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** After a second denial Android stops showing the dialog; only system settings can grant it then. */
internal fun notificationPermissionPermanentlyDenied(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    val activity = context.findActivity() ?: return false
    return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
        !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
}

internal fun openAppNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    }
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
