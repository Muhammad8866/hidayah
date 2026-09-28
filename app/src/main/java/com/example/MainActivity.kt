package com.example
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.appLocale
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.localization.withAppLocale

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.communitymasjid.ui.CommunityMasjidScreen
import com.example.quran.QuranAudioManager
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LocationIssue
import com.example.viewmodel.MainViewModel

enum class AppDestination(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    /** Timetable during Ramadan, countdown to the next Ramadan the rest of the year. */
    RAMADAN("Ramzan", Icons.Filled.NightsStay, Icons.Outlined.NightsStay),
    QURAN("Quran", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    HADITH("Hadith", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    COMMUNITY_MASJID("Masjid", Icons.Filled.Mosque, Icons.Outlined.Mosque),
    DUAS("Duas", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    FESTIVALS("Festivals", Icons.Filled.Celebration, Icons.Outlined.Celebration),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    NAAT("Naat", Icons.Filled.Headphones, Icons.Outlined.Headphones),
    DAILY_TOOLS("Daily tools", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    MORE("More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}

private val primaryDestinations = listOf(
    AppDestination.HOME,
    AppDestination.QURAN,
    AppDestination.DUAS,
    AppDestination.COMMUNITY_MASJID,
    AppDestination.MORE
)

private val refreshableDestinations = setOf(
    AppDestination.HOME,
    AppDestination.RAMADAN,
    AppDestination.FESTIVALS,
    AppDestination.CALENDAR
)

private val moreDestinations = setOf(
    AppDestination.RAMADAN,
    AppDestination.HADITH,
    AppDestination.FESTIVALS,
    AppDestination.CALENDAR,
    AppDestination.NAAT,
    AppDestination.DAILY_TOOLS
)

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        viewModel.startCompass()
        viewModel.syncPlatformLanguage()
        viewModel.refreshRealtimeDate()
        // Re-check on every foreground entry. This covers the common flow where the
        // user enables device location in Android Settings and then returns here.
        viewModel.startLiveLocation()
    }

    override fun onStop() {
        viewModel.stopCompass()
        super.onStop()
    }

    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Playback outlives this Activity, so the engine is wired to the application context.
        QuranAudioManager.init(this)

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val baseContext = LocalContext.current
                val baseConfiguration = LocalConfiguration.current
                val locale = remember(uiState.languageCode) { appLocale(uiState.languageCode) }
                val localizedContext = remember(baseContext, baseConfiguration, uiState.languageCode) {
                    baseContext.withAppLocale(uiState.languageCode)
                }
                val localizedConfiguration = localizedContext.resources.configuration
                val appLayoutDirection = if (isRtlAppLanguage(uiState.languageCode)) LayoutDirection.Rtl else LayoutDirection.Ltr
                CompositionLocalProvider(
                    LocalContext provides localizedContext,
                    LocalConfiguration provides localizedConfiguration,
                    LocalLayoutDirection provides appLayoutDirection,
                    LocalAppLanguage provides uiState.languageCode,
                    LocalAppLocale provides locale
                ) {
                val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
                var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
                var showPrayerTransparency by remember { mutableStateOf(false) }
                var showQibla by remember { mutableStateOf(false) }
                var showMoreSettings by remember { mutableStateOf(false) }
                var activeDailyTool by remember { mutableStateOf(DailyTool.PRAYER_TRACKER) }
                // Screens listed under More can also be opened from Home; Back returns to wherever they came from.
                var moreBackTarget by remember { mutableStateOf(AppDestination.MORE) }
                val openSecondary = { destination: AppDestination, from: AppDestination ->
                    moreBackTarget = from
                    currentDestination = destination
                }
                val backFromSecondary = { currentDestination = moreBackTarget }
                // Full-screen PDF readers hide every piece of app chrome around the source page.
                var readerImmersive by remember { mutableStateOf(false) }
                BackHandler(enabled = readerImmersive || showQibla || currentDestination != AppDestination.HOME) {
                    when {
                        readerImmersive -> readerImmersive = false
                        showQibla -> showQibla = false
                        currentDestination in moreDestinations -> currentDestination = moreBackTarget
                        currentDestination == AppDestination.MORE && showMoreSettings -> showMoreSettings = false
                        currentDestination == AppDestination.MORE -> currentDestination = AppDestination.HOME
                        else -> currentDestination = AppDestination.HOME
                    }
                }

                LaunchedEffect(currentDestination) {
                    if (currentDestination != AppDestination.QURAN && currentDestination != AppDestination.HADITH) {
                        readerImmersive = false
                    }
                }

                val context = LocalContext.current
                val haptics = LocalHapticFeedback.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true) {
                        viewModel.setNotificationsEnabled(true)
                    }
                    if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) viewModel.startLiveLocation()
                }

                LaunchedEffect(Unit) {
                    val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    } else {
                        if (!hasLocation) permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                    if (hasLocation) viewModel.startLiveLocation()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = if (readerImmersive) WindowInsets(0, 0, 0, 0)
                    else WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    ),
                    bottomBar = {
                        if (readerImmersive) return@Scaffold
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            primaryDestinations.forEach { dest ->
                                val selectedDestination = if (currentDestination in moreDestinations) {
                                    moreBackTarget
                                } else {
                                    currentDestination
                                }
                                val isSelected = selectedDestination == dest
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        // Re-tapping the tab that is already showing does nothing; from a sub-screen it goes back to the tab.
                                        if (currentDestination == dest && !showQibla && !showMoreSettings) return@NavigationBarItem
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showQibla = false
                                        if (dest == AppDestination.MORE) showMoreSettings = false
                                        currentDestination = dest
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = localizeUiText(dest.title, uiState.languageCode)
                                        )
                                    },
                                    label = {
                                        // "Community Masjid" wraps to two short lines instead of being cut off.
                                        LocalizedText(
                                            text = dest.title,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp)
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_${dest.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // Qibla is a full-screen overlay, so it gets its own transition key.
                    val screenKey = if (showQibla) null else currentDestination
                    // Only install the gesture where this ViewModel refreshes the visible page.
                    val refreshEnabled = !showQibla && currentDestination in refreshableDestinations
                    val contentModifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                    val pageContent: @Composable () -> Unit = {
                        AnimatedContent(
                            targetState = screenKey,
                            transitionSpec = {
                                (fadeIn(tween(220, delayMillis = 60)) + scaleIn(tween(220, delayMillis = 60), initialScale = 0.98f))
                                    .togetherWith(fadeOut(tween(90)))
                            },
                            label = "destination"
                        ) { destination ->
                        when (destination) {
                            null -> QiblaScreen(
                                uiState = uiState,
                                onBack = { showQibla = false },
                                onUseCurrentLocation = {
                                    val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (!hasLocation) {
                                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    } else if (uiState.locationIssue == LocationIssue.SERVICES_OFF) {
                                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                    } else {
                                        viewModel.startLiveLocation()
                                    }
                                }
                            )

                            AppDestination.HOME -> HomeScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onNavigateToQuran = { currentDestination = AppDestination.QURAN },
                                // Home picks the Duas sub-tab (Adhkar or Tasbeeh) before navigating.
                                onNavigateToDuas = { currentDestination = AppDestination.DUAS },
                                onNavigateToQibla = { showQibla = true },
                                onOpenPrayerTransparency = { showPrayerTransparency = true },
                                onOpenDailyTool = { tool ->
                                    activeDailyTool = tool
                                    openSecondary(AppDestination.DAILY_TOOLS, AppDestination.HOME)
                                },
                                onNavigateToHadith = { openSecondary(AppDestination.HADITH, AppDestination.HOME) },
                                onNavigateToCalendar = { openSecondary(AppDestination.CALENDAR, AppDestination.HOME) },
                                onNavigateToRamadan = { openSecondary(AppDestination.RAMADAN, AppDestination.HOME) },
                                onNavigateToNaat = { openSecondary(AppDestination.NAAT, AppDestination.HOME) }
                            )

                            AppDestination.RAMADAN -> RamadanScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onBack = backFromSecondary
                            )

                            AppDestination.QURAN -> QuranScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                isImmersive = readerImmersive,
                                onImmersiveChange = { readerImmersive = it }
                            )

                            AppDestination.HADITH -> HadithScreen(
                                isImmersive = readerImmersive,
                                onImmersiveChange = { readerImmersive = it },
                                onBack = backFromSecondary
                            )

                            AppDestination.DUAS -> DuasAdhkarScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )

                            AppDestination.FESTIVALS -> FestivalsScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onBack = backFromSecondary
                            )

                            AppDestination.CALENDAR -> IslamicCalendarScreen(
                                uiState = uiState,
                                onBack = backFromSecondary
                            )

                            AppDestination.COMMUNITY_MASJID -> CommunityMasjidScreen(
                                onBack = { currentDestination = AppDestination.HOME }
                            )

                            AppDestination.NAAT -> NaatScreen(
                                onBack = backFromSecondary
                            )

                            AppDestination.DAILY_TOOLS -> DailyToolScreen(
                                tool = activeDailyTool,
                                onBack = backFromSecondary,
                                onOpenQuran = { currentDestination = AppDestination.QURAN },
                                onOpenQibla = { showQibla = true },
                                uiState = uiState
                            )

                            AppDestination.MORE -> if (showMoreSettings) {
                                SettingsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onBack = { showMoreSettings = false }
                                )
                            } else {
                                MoreScreen(
                                    onNavigateToRamadan = { openSecondary(AppDestination.RAMADAN, AppDestination.MORE) },
                                    onNavigateToHadith = { openSecondary(AppDestination.HADITH, AppDestination.MORE) },
                                    onNavigateToFestivals = { openSecondary(AppDestination.FESTIVALS, AppDestination.MORE) },
                                    onNavigateToCalendar = { openSecondary(AppDestination.CALENDAR, AppDestination.MORE) },
                                    onNavigateToNaat = { openSecondary(AppDestination.NAAT, AppDestination.MORE) },
                                    onOpenDailyTool = { tool ->
                                        activeDailyTool = tool
                                        openSecondary(AppDestination.DAILY_TOOLS, AppDestination.MORE)
                                    },
                                    onNavigateToQibla = { showQibla = true },
                                    onOpenSettings = { showMoreSettings = true }
                                )
                            }
                        }
                        }

                        if (showPrayerTransparency) {
                            PrayerTransparencyBottomSheet(
                                uiState = uiState,
                                viewModel = viewModel,
                                onDismiss = { showPrayerTransparency = false }
                            )
                        }
                    }
                    if (refreshEnabled) {
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = viewModel::refreshAll,
                            modifier = contentModifier
                        ) { pageContent() }
                    } else {
                        Box(contentModifier) { pageContent() }
                    }
                }
                }
            }
        }
    }
}
