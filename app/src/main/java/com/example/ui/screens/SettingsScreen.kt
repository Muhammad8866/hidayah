package com.example.ui.screens
import com.example.ui.components.ScreenHeader
import com.example.ui.components.AppCard
import com.example.ui.components.IconBadge
import com.example.ui.components.Spacing
import com.example.prayer.HijriRegion
import com.example.prayer.PrayerNotificationReceiver
import com.example.prayer.PrayerSettingsStore
import com.example.salah.Madhhab
import com.example.salah.SalahSettingsStore
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import com.example.ui.localization.formatHijriDate
import com.example.ui.localization.localizeUiText

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AsrJuristicMethod
import com.example.domain.model.CalculationMethod
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.PredefinedLocations
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenAzan: () -> Unit = {}
) {
    com.example.core.analytics.AnalyticsScreen("settings_screen")

    var showCalculationMethodDialog by rememberSaveable { mutableStateOf(false) }
    var showAsrMethodDialog by rememberSaveable { mutableStateOf(false) }
    var showHighLatitudeDialog by rememberSaveable { mutableStateOf(false) }
    var showHijriCalendarDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        com.example.core.analytics.AppAnalytics.permission("notifications", granted)
        if (!granted && notificationPermissionPermanentlyDenied(context)) openAppNotificationSettings(context)
        viewModel.setNotificationsEnabled(true)
    }
    val salahPrefs = remember { PrayerSettingsStore.prefs(viewModel.getApplication()) }
    var selectedMadhhab by remember { mutableStateOf(SalahSettingsStore.load(salahPrefs)) }
    var showMadhhabDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.section)
    ) {
        item {
            ScreenHeader(
                title = "Settings & Privacy",
                subtitle = "No ads. Usage analytics helps improve Hidayah.",
                onBack = onBack,
                backTestTag = "settings_back_btn"
            )
        }

        item {
            SettingsSection("App Language") {
                SettingsClickableRow(
                    title = "Choose language",
                    subtitle = AppLanguage.supported.firstOrNull { it.code == uiState.languageCode }?.nativeName
                        ?: AppLanguage.supported.first().nativeName,
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "open_language_dialog", "tap"); showLanguageDialog = true },
                    icon = Icons.Default.Language
                )
            }
        }

        // Prayer Notifications Group
        item {
            SettingsSection("Prayer Notifications Service") {
                val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
                val onAlertsToggle: (Boolean) -> Unit = { enabled ->
                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setNotificationsEnabled(enabled)
                    }
                }
                // The whole row toggles, so the label is part of the touch target and TalkBack reads it with the switch.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .toggleable(
                            value = uiState.isNotificationEnabled,
                            role = Role.Switch,
                            onValueChange = onAlertsToggle
                        )
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIconBadge(Icons.Default.Notifications)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        LocalizedText(
                            text = "Enable Prayer Alerts",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        LocalizedText(
                            text = "Sends on-device alerts when prayer enters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = uiState.isNotificationEnabled,
                        onCheckedChange = null,
                        modifier = Modifier.testTag("notification_switch")
                    )
                }

                if (uiState.isNotificationEnabled) {
                    ExactAlarmPermissionRow()
                    BatteryOptimizationHintRow()
                }

                SettingsDivider()

                SettingsActionRow(
                    title = "Choose Azan for prayer alerts",
                    icon = Icons.Default.Campaign,
                    onClick = com.example.core.analytics.AppAnalytics.trackAction("settings_screen", "open_azan_tones", onOpenAzan),
                    modifier = Modifier.testTag("azan_tone_btn")
                )

                SettingsDivider()

                SettingsActionRow(
                    title = "Send Test Prayer Alert Now",
                    icon = Icons.Default.NotificationsActive,
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "send_test_prayer_alert", "tap"); viewModel.triggerTestNotification() },
                    modifier = Modifier.testTag("test_notification_btn"),
                    showChevron = false
                )

                if (uiState.notificationFeedbackMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LocalizedText(
                        text = uiState.notificationFeedbackMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Prayer Calculations Settings Group
        item {
            SettingsSection("Prayer Calculation Settings") {
                // Calculation Authority
                SettingsClickableRow(
                    title = "Calculation Authority",
                    subtitle = uiState.prayerConfig.method.title,
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "open_calculation_method_dialog", "tap"); showCalculationMethodDialog = true },
                    icon = Icons.Default.Calculate
                )

                SettingsDivider()

                // Asr Method
                SettingsClickableRow(
                    title = "Asr Juristic Calculation",
                    subtitle = uiState.prayerConfig.asrMethod.title,
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "open_asr_method_dialog", "tap"); showAsrMethodDialog = true },
                    icon = Icons.Default.Schedule
                )

                SettingsDivider()

                SettingsClickableRow(
                    title = "High-latitude adjustment",
                    subtitle = uiState.prayerConfig.highLatitudeRule.title,
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "open_high_latitude_dialog", "tap"); showHighLatitudeDialog = true },
                    icon = Icons.Default.Public
                )

                SettingsDivider()

                val automaticHijriRegion = HijriRegion.automaticFor(uiState.selectedCity.timeZoneId)
                SettingsClickableRow(
                    title = "Hijri calendar",
                    subtitle = HijriRegion.byId(uiState.hijriCalendarId)?.label
                        ?: "Automatic: ${automaticHijriRegion.label}",
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "open_hijri_calendar_dialog", "tap"); showHijriCalendarDialog = true },
                    icon = Icons.Default.CalendarMonth
                )

                SettingsDivider()

                // Hijri Date Manual Adjustment
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIconBadge(Icons.Default.NightsStay)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        LocalizedText(
                            text = "Hijri Moon Sighting Adjustment",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        LocalizedText(
                            text = "${if (uiState.hijriAdjustmentDays > 0) "+${uiState.hijriAdjustmentDays}" else "${uiState.hijriAdjustmentDays}"} ${localizeUiText("Days", uiState.languageCode)} (${formatHijriDate(uiState.hijriDateTonight ?: uiState.hijriDate, uiState.languageCode)}${if (uiState.hijriDateTonight != null) ", ${localizeUiText("Tonight", uiState.languageCode)}" else ""})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "hijri_adjust_minus", "tap"); viewModel.setHijriAdjustmentDays(uiState.hijriAdjustmentDays - 1) },
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier.widthIn(min = 56.dp).heightIn(min = 48.dp)
                        ) {
                            LocalizedText("-1 d")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        FilledTonalButton(
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "hijri_adjust_plus", "tap"); viewModel.setHijriAdjustmentDays(uiState.hijriAdjustmentDays + 1) },
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier.widthIn(min = 56.dp).heightIn(min = 48.dp)
                        ) {
                            LocalizedText("+1 d")
                        }
                    }
                }
            }
        }

        item {
            SettingsSection("Salah guide") {
                SettingsClickableRow(
                    title = "Madhhab",
                    subtitle = "${selectedMadhhab.label} · affects guide notes, not prayer-time calculations",
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "open_madhhab_dialog", "tap"); showMadhhabDialog = true },
                    icon = Icons.Default.Mosque
                )
            }
        }

        // Content Governance & Sources Verification
        item {
            SettingsSection("Content Governance & Sources") {
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                    SettingsIconBadge(Icons.Default.Verified)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        LocalizedText(
                            text = "Every ayah, hadith, dua, and lesson in Hidayah links to canonical sources with authenticated revision histories.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LocalizedText(
                            text = "Offline Mushaf integrity: SHA-256 verified before display.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showMadhhabDialog) {
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "dismiss_madhhab_dialog", "dismiss"); showMadhhabDialog = false },
            title = { LocalizedText("Choose madhhab") },
            text = {
                Column {
                    Madhhab.entries.forEach { school ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(selected = selectedMadhhab == school, role = Role.RadioButton) { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "select_madhhab", "tap", item = school.name);
                                selectedMadhhab = school
                                SalahSettingsStore.save(salahPrefs, school)
                                com.example.core.analytics.AppAnalytics.setting("set_madhhab", school.name)
                                com.example.core.analytics.AppAnalytics.userProperty("madhhab", school.name)
                                showMadhhabDialog = false
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedMadhhab == school, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(school.label)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "close_madhhab_dialog", "tap"); showMadhhabDialog = false }) { LocalizedText("Close") } }
        )
    }

    // Calculation Method Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "dismiss_language_dialog", "dismiss"); showLanguageDialog = false },
            title = { LocalizedText("Choose app language") },
            text = {
                Column(Modifier.heightIn(max = 460.dp).verticalScroll(com.example.core.analytics.rememberAnalyticsScrollState("settings_screen"))) {
                    AppLanguage.supported.forEach { language ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(selected = uiState.languageCode == language.code, role = Role.RadioButton) { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "select_language", "tap", item = language.code);
                                viewModel.setLanguage(language.code)
                                showLanguageDialog = false
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.languageCode == language.code,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(language.nativeName, fontWeight = FontWeight.SemiBold)
                                if (language.reviewPending) {
                                    com.example.ui.localization.LocalizedText(
                                        "Machine-translated — review pending",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton({ com.example.core.analytics.AppAnalytics.interaction("settings_screen", "close_language_dialog", "tap"); showLanguageDialog = false }) { LocalizedText("Close") } }
        )
    }

    // Calculation Method Dialog
    if (showCalculationMethodDialog) {
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "dismiss_calculation_method_dialog", "dismiss"); showCalculationMethodDialog = false },
            title = { LocalizedText("Choose Calculation Authority") },
            text = {
                Column(modifier = Modifier.verticalScroll(com.example.core.analytics.rememberAnalyticsScrollState("settings_screen"))) {
                    CalculationMethod.values().forEach { method ->
                        val isSelected = method == uiState.prayerConfig.method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = isSelected, role = Role.RadioButton) { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "select_calculation_method", "tap", item = method.name);
                                    viewModel.updatePrayerConfig(uiState.prayerConfig.copy(method = method))
                                    showCalculationMethodDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                LocalizedText(
                                    text = method.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                LocalizedText(
                                    text = method.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = localizedUiText("Selected"),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "close_calculation_method_dialog", "tap"); showCalculationMethodDialog = false }) { LocalizedText("Close") }
            }
        )
    }

    // Asr Method Dialog
    if (showAsrMethodDialog) {
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "dismiss_asr_method_dialog", "dismiss"); showAsrMethodDialog = false },
            title = { LocalizedText("Choose Asr Juristic Method") },
            text = {
                Column(modifier = Modifier.verticalScroll(com.example.core.analytics.rememberAnalyticsScrollState("settings_screen"))) {
                    AsrJuristicMethod.values().forEach { asr ->
                        val isSelected = asr == uiState.prayerConfig.asrMethod
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = isSelected, role = Role.RadioButton) { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "select_asr_method", "tap", item = asr.name);
                                    viewModel.updatePrayerConfig(uiState.prayerConfig.copy(asrMethod = asr))
                                    showAsrMethodDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                LocalizedText(
                                    text = asr.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                LocalizedText(
                                    text = asr.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = localizedUiText("Selected"),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "close_asr_method_dialog", "tap"); showAsrMethodDialog = false }) { LocalizedText("Close") }
            }
        )
    }

    if (showHighLatitudeDialog) {
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "dismiss_high_latitude_dialog", "dismiss"); showHighLatitudeDialog = false },
            title = { LocalizedText("Choose high-latitude rule") },
            text = {
                Column(modifier = Modifier.verticalScroll(com.example.core.analytics.rememberAnalyticsScrollState("settings_screen"))) {
                    HighLatitudeRule.values().forEach { rule ->
                        val isSelected = rule == uiState.prayerConfig.highLatitudeRule
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = isSelected, role = Role.RadioButton) { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "select_high_latitude_rule", "tap", item = rule.name);
                                    viewModel.updatePrayerConfig(
                                        uiState.prayerConfig.copy(highLatitudeRule = rule)
                                    )
                                    showHighLatitudeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LocalizedText(
                                text = rule.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = localizedUiText("Selected"),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "close_high_latitude_dialog", "tap"); showHighLatitudeDialog = false }) { LocalizedText("Close") }
            }
        )
    }

    if (showHijriCalendarDialog) {
        val automatic = HijriRegion.automaticFor(uiState.selectedCity.timeZoneId)
        val choices = listOf<HijriRegion?>(null) + HijriRegion.ALL
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "dismiss_hijri_calendar_dialog", "dismiss"); showHijriCalendarDialog = false },
            title = { LocalizedText("Choose Hijri calendar") },
            text = {
                Column(modifier = Modifier.verticalScroll(com.example.core.analytics.rememberAnalyticsScrollState("settings_screen"))) {
                    LocalizedText(
                        text = "Follow the moon-sighting authority your community follows. Automatic uses your location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    choices.forEach { region ->
                        val isSelected = region?.id == uiState.hijriCalendarId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = isSelected, role = Role.RadioButton) { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "select_hijri_calendar", "tap", item = region?.id ?: "automatic");
                                    viewModel.setHijriCalendar(region)
                                    showHijriCalendarDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LocalizedText(
                                text = region?.label ?: "Automatic (${automatic.label})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = localizedUiText("Selected"),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "close_hijri_calendar_dialog", "tap"); showHijriCalendarDialog = false }) { LocalizedText("Close") }
            }
        )
    }

}

/** One settings group: a small accent heading above a hairline card whose rows are split by [SettingsDivider]. */
@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        LocalizedText(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp).semantics { heading() }
        )
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            content = content
        )
    }
}

/** Starts after the icon column, so rows read as one list rather than separate boxes. */
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp, top = 2.dp, bottom = 2.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingsIconBadge(icon: ImageVector) {
    IconBadge(icon = icon, accent = MaterialTheme.colorScheme.primary, size = 40.dp, iconSize = 22.dp)
}

/** A setting that performs an action rather than showing a value: icon, label and an optional chevron. */
@Composable
private fun SettingsActionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button) { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBadge(icon)
        Spacer(modifier = Modifier.width(14.dp))
        LocalizedText(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A tappable setting: icon, name, the current value underneath, and a chevron that mirrors in RTL. */
@Composable
fun SettingsClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Tune
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button) { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBadge(icon)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            LocalizedText(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            LocalizedText(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = localizedUiText("Change"),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

/** Without "Alarms & reminders" (Android 12+) the system may deliver prayer alerts minutes late. */
@Composable
private fun ExactAlarmPermissionRow() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val context = LocalContext.current
    var allowed by remember { mutableStateOf(PrayerNotificationReceiver.canScheduleExactAlarms(context)) }
    LifecycleResumeEffect(Unit) {
        allowed = PrayerNotificationReceiver.canScheduleExactAlarms(context)
        onPauseOrDispose { }
    }
    if (allowed) return
    Spacer(modifier = Modifier.height(10.dp))
    LocalizedText(
        text = "Allow \"Alarms & reminders\" so the Azan alert arrives exactly on time.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedButton(
        onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "open_exact_alarm_settings", "tap");
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        },
        modifier = Modifier.fillMaxWidth().testTag("exact_alarm_btn")
    ) {
        LocalizedText("Allow exact prayer alerts")
    }
}

/** Aggressive OEM battery savers (Xiaomi, Oppo, Vivo, Realme...) can kill scheduled prayer alerts. */
@Composable
private fun BatteryOptimizationHintRow() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prayer_hints", android.content.Context.MODE_PRIVATE) }
    var dismissed by remember { mutableStateOf(prefs.getBoolean("battery_hint_dismissed", false)) }
    var exempt by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }
    LifecycleResumeEffect(Unit) {
        exempt = isIgnoringBatteryOptimizations(context)
        onPauseOrDispose { }
    }
    if (dismissed || exempt) return
    Spacer(modifier = Modifier.height(10.dp))
    LocalizedText(
        text = "If Azan alerts stop or come late, set this app's battery usage to \"Unrestricted\". " +
            "On Xiaomi, Oppo, Vivo or Realme phones also enable \"Autostart\" for this app in the phone's settings.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "open_battery_settings", "tap");
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }.onFailure {
                    runCatching {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            },
            modifier = Modifier.weight(1f).testTag("battery_hint_btn")
        ) {
            LocalizedText("Battery settings")
        }
        OutlinedButton(
            onClick = { com.example.core.analytics.AppAnalytics.interaction("settings_screen", "dismiss_battery_hint", "tap");
                prefs.edit().putBoolean("battery_hint_dismissed", true).apply()
                dismissed = true
            },
            modifier = Modifier.weight(1f)
        ) {
            LocalizedText("Dismiss")
        }
    }
}

private fun isIgnoringBatteryOptimizations(context: android.content.Context): Boolean {
    val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager ?: return true
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}
