package com.example.ui.screens
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
    modifier: Modifier = Modifier
) {
    var showCalculationMethodDialog by rememberSaveable { mutableStateOf(false) }
    var showAsrMethodDialog by rememberSaveable { mutableStateOf(false) }
    var showHighLatitudeDialog by rememberSaveable { mutableStateOf(false) }
    var showHijriCalendarDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
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
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = localizedUiText("Back")
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    LocalizedText(
                        text = "Settings & Privacy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    LocalizedText(
                        text = "Privacy-first, account-optional architecture. No ads, no trackers, zero guilt mechanics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    LocalizedText(
                        text = "App Language",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))
                    SettingsClickableRow(
                        title = "Choose language",
                        subtitle = AppLanguage.supported.firstOrNull { it.code == uiState.languageCode }?.nativeName
                            ?: AppLanguage.supported.first().nativeName,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    LocalizedText("Salah guide", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(10.dp))
                    SettingsClickableRow(
                        title = "Madhhab",
                        subtitle = "${selectedMadhhab.label} · affects guide notes, not prayer-time calculations",
                        onClick = { showMadhhabDialog = true }
                    )
                }
            }
        }

        // Prayer Calculations Settings Group
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LocalizedText(
                        text = "Prayer Calculation Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Calculation Authority
                    SettingsClickableRow(
                        title = "Calculation Authority",
                        subtitle = uiState.prayerConfig.method.title,
                        onClick = { showCalculationMethodDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Asr Method
                    SettingsClickableRow(
                        title = "Asr Juristic Calculation",
                        subtitle = uiState.prayerConfig.asrMethod.title,
                        onClick = { showAsrMethodDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingsClickableRow(
                        title = "High-latitude adjustment",
                        subtitle = uiState.prayerConfig.highLatitudeRule.title,
                        onClick = { showHighLatitudeDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    val automaticHijriRegion = HijriRegion.automaticFor(uiState.selectedCity.timeZoneId)
                    SettingsClickableRow(
                        title = "Hijri calendar",
                        subtitle = HijriRegion.byId(uiState.hijriCalendarId)?.label
                            ?: "Automatic: ${automaticHijriRegion.label}",
                        onClick = { showHijriCalendarDialog = true }
                    )

                    // Hijri Date Manual Adjustment
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LocalizedText(
                                text = "Hijri Moon Sighting Adjustment",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            LocalizedText(
                                text = "${if (uiState.hijriAdjustmentDays > 0) "+${uiState.hijriAdjustmentDays}" else "${uiState.hijriAdjustmentDays}"} ${localizeUiText("Days", uiState.languageCode)} (${formatHijriDate(uiState.hijriDateTonight ?: uiState.hijriDate, uiState.languageCode)}${if (uiState.hijriDateTonight != null) ", ${localizeUiText("Tonight", uiState.languageCode)}" else ""})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { viewModel.setHijriAdjustmentDays(uiState.hijriAdjustmentDays - 1) },
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                LocalizedText("-1 d")
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { viewModel.setHijriAdjustmentDays(uiState.hijriAdjustmentDays + 1) },
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                LocalizedText("+1 d")
                            }
                        }
                    }
                }
            }
        }

        // Prayer Notifications Group
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LocalizedText(
                        text = "Prayer Notifications Service",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LocalizedText(
                                text = "Enable Prayer Alerts",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            LocalizedText(
                                text = "Sends on-device alerts when prayer enters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isNotificationEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setNotificationsEnabled(enabled)
                                }
                            },
                            modifier = Modifier.testTag("notification_switch")
                        )
                    }

                    if (uiState.isNotificationEnabled) {
                        ExactAlarmPermissionRow()
                        BatteryOptimizationHintRow()
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.triggerTestNotification() },
                        modifier = Modifier.fillMaxWidth().testTag("test_notification_btn")
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        LocalizedText("Send Test Prayer Alert Now")
                    }

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
        }

        // Content Governance & Sources Verification
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LocalizedText(
                        text = "Content Governance & Sources",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LocalizedText(
                        text = "Every ayah, hadith, dua, and lesson in HIDAYAH links to canonical sources with authenticated revision histories.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LocalizedText(
                        text = "Offline Mushaf integrity: SHA-256 verified before display.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showMadhhabDialog) {
        AlertDialog(
            onDismissRequest = { showMadhhabDialog = false },
            title = { LocalizedText("Choose madhhab") },
            text = {
                Column {
                    Madhhab.entries.forEach { school ->
                        Row(
                            Modifier.fillMaxWidth().clickable(role = Role.Button) {
                                selectedMadhhab = school
                                SalahSettingsStore.save(salahPrefs, school)
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
            confirmButton = { TextButton(onClick = { showMadhhabDialog = false }) { LocalizedText("Close") } }
        )
    }

    // Calculation Method Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { LocalizedText("Choose app language") },
            text = {
                Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                    AppLanguage.supported.forEach { language ->
                        Row(
                            Modifier.fillMaxWidth().clickable(role = Role.Button) {
                                viewModel.setLanguage(language.code)
                                showLanguageDialog = false
                            }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.languageCode == language.code,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(language.nativeName, fontWeight = FontWeight.SemiBold)
                                if (language.reviewPending) {
                                    Text(
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
            confirmButton = { TextButton({ showLanguageDialog = false }) { LocalizedText("Close") } }
        )
    }

    // Calculation Method Dialog
    if (showCalculationMethodDialog) {
        AlertDialog(
            onDismissRequest = { showCalculationMethodDialog = false },
            title = { LocalizedText("Choose Calculation Authority") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    CalculationMethod.values().forEach { method ->
                        val isSelected = method == uiState.prayerConfig.method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button) {
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
                TextButton(onClick = { showCalculationMethodDialog = false }) { LocalizedText("Close") }
            }
        )
    }

    // Asr Method Dialog
    if (showAsrMethodDialog) {
        AlertDialog(
            onDismissRequest = { showAsrMethodDialog = false },
            title = { LocalizedText("Choose Asr Juristic Method") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AsrJuristicMethod.values().forEach { asr ->
                        val isSelected = asr == uiState.prayerConfig.asrMethod
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button) {
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
                TextButton(onClick = { showAsrMethodDialog = false }) { LocalizedText("Close") }
            }
        )
    }

    if (showHighLatitudeDialog) {
        AlertDialog(
            onDismissRequest = { showHighLatitudeDialog = false },
            title = { LocalizedText("Choose high-latitude rule") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HighLatitudeRule.values().forEach { rule ->
                        val isSelected = rule == uiState.prayerConfig.highLatitudeRule
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button) {
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
                TextButton(onClick = { showHighLatitudeDialog = false }) { LocalizedText("Close") }
            }
        )
    }

    if (showHijriCalendarDialog) {
        val automatic = HijriRegion.automaticFor(uiState.selectedCity.timeZoneId)
        val choices = listOf<HijriRegion?>(null) + HijriRegion.ALL
        AlertDialog(
            onDismissRequest = { showHijriCalendarDialog = false },
            title = { LocalizedText("Choose Hijri calendar") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                                .clickable(role = Role.Button) {
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
                TextButton(onClick = { showHijriCalendarDialog = false }) { LocalizedText("Close") }
            }
        )
    }

}

@Composable
fun SettingsClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            LocalizedText(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            LocalizedText(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = localizedUiText("Change"),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        onClick = {
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
            onClick = {
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
            onClick = {
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
