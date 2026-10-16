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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
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
    AZAN("Azan", Icons.Filled.Campaign, Icons.Outlined.Campaign),
    ALLAH_NAMES("99 Names of Allah", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BABY_NAMES("Islamic Baby Names", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    PROPHET_NAMES("99 Names of the Prophet ﷺ", Icons.Filled.Stars, Icons.Outlined.Stars),
    RECITATION_COLLECTION("Manzil / Ruqyah / Wazaif", Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
    RABBANA_DUAS("40 Rabbana Duas", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism),
    KALIMAS("6 Kalime & Iman", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    BOOKS("Islamic Books", Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
    WORLD_LIBRARY("World Library", Icons.Filled.Public, Icons.Outlined.Public),
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
    AppDestination.AZAN,
    AppDestination.ALLAH_NAMES,
    AppDestination.BABY_NAMES,
    AppDestination.PROPHET_NAMES,
    AppDestination.RABBANA_DUAS,
    AppDestination.KALIMAS,
    AppDestination.RECITATION_COLLECTION,
    AppDestination.BOOKS,
    AppDestination.WORLD_LIBRARY,
    AppDestination.DAILY_TOOLS
)

/** How deep a screen sits: bottom tabs are 0, screens opened from them 1, the Qibla overlay (null) 2. */
private fun navigationDepth(destination: AppDestination?): Int = when {
    destination == null -> 2
    destination in moreDestinations -> 1
    else -> 0
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val appUpdateManager by lazy {
        com.google.android.play.core.appupdate.AppUpdateManagerFactory.create(applicationContext)
    }
    private var updateFlowShowing = false
    // Whatever the result, onResume runs next and asks Play again, so a cancelled update comes straight back.
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { updateFlowShowing = false }

    override fun onResume() {
        super.onResume()
        enforceLatestVersion()
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean =
        com.example.quran.ReaderKeys.handle(event) || super.dispatchKeyEvent(event)

    /**
     * Updates are mandatory: when Play has a newer version, its full-screen update flow blocks the app
     * until it is installed. Offline, or installed from outside Play, the check fails and the app opens normally.
     */
    private fun enforceLatestVersion() {
        if (updateFlowShowing) return
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val availability = info.updateAvailability()
            val pending = availability == com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE)
            // An update that was started and then left (app killed, Home pressed) must be resumed.
            val interrupted = availability ==
                com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            if (!updateFlowShowing && !isFinishing && (pending || interrupted)) {
                updateFlowShowing = runCatching {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateLauncher,
                        com.google.android.play.core.appupdate.AppUpdateOptions.newBuilder(
                            com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
                        ).build()
                    )
                }.getOrDefault(false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        com.example.core.analytics.AppAnalytics.foreground()
        // Read before the refreshes below so prayer times follow the device's 12/24-hour setting.
        com.example.prayer.AstronomicalPrayerCalculator.use24HourClock =
            android.text.format.DateFormat.is24HourFormat(this)
        viewModel.startCompass()
        viewModel.syncPlatformLanguage()
        viewModel.refreshRealtimeDate()
        viewModel.refreshNotificationPermission()
        // Re-check on every foreground entry. This covers the common flow where the
        // user enables device location in Android Settings and then returns here.
        viewModel.startLiveLocation()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        reportLaunch(intent)
    }

    /** Reports whether the launcher icon, a prayer alert or the home-screen widget opened the app. */
    private fun reportLaunch(intent: android.content.Intent?) {
        val source = intent?.getStringExtra(com.example.prayer.PrayerNotificationReceiver.EXTRA_LAUNCH_SOURCE)
        com.example.core.analytics.AppAnalytics.launch(source ?: "launcher")
        when (source) {
            com.example.prayer.PrayerNotificationReceiver.SOURCE_PRAYER_ALERT -> com.example.core.analytics.AppAnalytics.prayerAlert(
                "opened", intent.getStringExtra(com.example.prayer.PrayerNotificationReceiver.EXTRA_PRAYER_NAME) ?: "prayer")
            com.example.prayer.PrayerNotificationReceiver.SOURCE_WIDGET -> com.example.core.analytics.AppAnalytics.widget("opened")
        }
    }

    override fun onStop() {
        com.example.core.analytics.AppAnalytics.background()
        viewModel.stopCompass()
        viewModel.pauseLiveLocation()
        super.onStop()
    }

    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A rotation or restore re-creates the Activity with the same intent; that is not a new launch.
        if (savedInstanceState == null) reportLaunch(intent)
        enableEdgeToEdge()
        com.example.prayer.AstronomicalPrayerCalculator.use24HourClock =
            android.text.format.DateFormat.is24HourFormat(this)
        // Playback outlives this Activity, so the engine is wired to the application context.
        QuranAudioManager.init(this)
        com.example.core.net.FastNet.init(this)
        com.example.core.net.Connectivity.init(this)

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
                // Fresh installs pick a language first; the location prompt and the app follow it.
                if (uiState.needsLanguageSelection) {
                    LanguageSelectionScreen(
                        detectedLanguageCode = uiState.languageCode,
                        onLanguageChosen = viewModel::completeLanguageSelection
                    )
                    return@CompositionLocalProvider
                }
                val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
                // One snackbar host for app-wide notices; the first is "Back online" after an outage.
                val snackbarHostState = remember { SnackbarHostState() }
                val isOnline by com.example.ui.components.rememberIsOnline()
                val backOnlineText = localizeUiText("Back online", uiState.languageCode)
                var wasOffline by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(isOnline) {
                    if (!isOnline) wasOffline = true
                    else if (wasOffline) {
                        wasOffline = false
                        snackbarHostState.showSnackbar(backOnlineText, duration = SnackbarDuration.Short)
                    }
                }
                var currentDestination by rememberSaveable { mutableStateOf(AppDestination.HOME) }
                var showPrayerTransparency by remember { mutableStateOf(false) }
                var showQibla by rememberSaveable { mutableStateOf(false) }
                var showMoreSettings by rememberSaveable { mutableStateOf(false) }
                var activeDailyTool by rememberSaveable { mutableStateOf(DailyTool.PRAYER_TRACKER) }
                // Screens listed under More can also be opened from Home; Back returns to wherever they came from.
                var moreBackTarget by rememberSaveable { mutableStateOf(AppDestination.MORE) }
                val openSecondary = { destination: AppDestination, from: AppDestination ->
                    moreBackTarget = from
                    currentDestination = destination
                }
                val backFromSecondary = { currentDestination = moreBackTarget }
                // Full-screen PDF readers hide every piece of app chrome around the source page.
                var readerImmersive by rememberSaveable { mutableStateOf(false) }
                BackHandler(enabled = readerImmersive || showQibla || currentDestination != AppDestination.HOME) { com.example.core.analytics.AppAnalytics.interaction("main_activity", "system_back", "back");
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
                    if (currentDestination != AppDestination.QURAN && currentDestination != AppDestination.HADITH &&
                        currentDestination != AppDestination.BOOKS &&
                        currentDestination != AppDestination.WORLD_LIBRARY
                    ) {
                        readerImmersive = false
                    }
                }

                val context = LocalContext.current
                val haptics = LocalHapticFeedback.current
                var useCurrentLocationAfterPermission by remember { mutableStateOf(false) }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    com.example.core.analytics.AppAnalytics.permission("location", permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
                    if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                        viewModel.startLiveLocation(userRequested = useCurrentLocationAfterPermission)
                    }
                    useCurrentLocationAfterPermission = false
                }

                LaunchedEffect(Unit) {
                    val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (viewModel.usesLiveLocation()) {
                        if (!hasLocation) permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        else viewModel.startLiveLocation()
                    }
                }

                // The feature tour walks through the real tabs, so each step picks the screen behind it.
                var tourStep by rememberSaveable { mutableIntStateOf(0) }
                // Where each bottom tab sits on screen, so the tour can spotlight it.
                val navItemBounds = remember { mutableStateMapOf<AppDestination, Rect>() }
                // Steps inside More spotlight its tiles instead; More reports where they ended up.
                val tourSection = if (uiState.needsFeatureTour) featureTourSteps[tourStep].moreSection else null
                var moreTourRect by remember { mutableStateOf<Rect?>(null) }
                LaunchedEffect(tourSection) { if (tourSection == null) moreTourRect = null }
                val finishTour = {
                    tourStep = 0
                    showMoreSettings = false
                    currentDestination = AppDestination.HOME
                    viewModel.completeFeatureTour()
                }
                LaunchedEffect(uiState.needsFeatureTour, tourStep) {
                    if (uiState.needsFeatureTour) {
                        showQibla = false
                        showMoreSettings = false
                        readerImmersive = false
                        currentDestination = featureTourSteps[tourStep].destination
                    }
                }

                Box(Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = if (readerImmersive) WindowInsets(0, 0, 0, 0)
                    else WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    ),
                    snackbarHost = { if (!readerImmersive) SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (readerImmersive) return@Scaffold
                        // A hairline separates the bar from content instead of a tonal shadow.
                        val navDividerColor = MaterialTheme.colorScheme.outlineVariant
                        NavigationBar(
                            modifier = Modifier
                                .testTag("bottom_nav_bar")
                                .drawBehind {
                                    drawLine(navDividerColor, androidx.compose.ui.geometry.Offset(0f, 0f),
                                        androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                                },
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
                                    onClick = { com.example.core.analytics.AppAnalytics.interaction("main_activity", "bottom_nav_tab", "tap", item = dest.name);
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
                                            // Tall scripts (Devanagari, Urdu, Bengali) clip below 14sp.
                                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp)
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
                                    modifier = Modifier
                                        .testTag("nav_${dest.name.lowercase()}")
                                        .onGloballyPositioned {
                                            val bounds = it.boundsInRoot()
                                            if (navItemBounds[dest] != bounds) navItemBounds[dest] = bounds
                                        }
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
                    // Slides run toward the reading direction's end edge, so they mirror for RTL languages.
                    val slideSign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
                    val pageContent: @Composable () -> Unit = {
                        // Tablets, foldables and landscape keep a readable column; PDF readers use the full width.
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        AnimatedContent(
                            targetState = screenKey,
                            modifier = if (readerImmersive) Modifier.fillMaxSize()
                            else Modifier.fillMaxHeight().widthIn(max = 720.dp).fillMaxWidth(),
                            transitionSpec = {
                                val from = navigationDepth(initialState)
                                val to = navigationDepth(targetState)
                                when {
                                    // Opening a sub-screen: it arrives from the end edge over the page it came from.
                                    to > from -> (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { slideSign * it / 4 } +
                                        fadeIn(tween(200, delayMillis = 30)))
                                        .togetherWith(slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { -slideSign * it / 12 } +
                                            fadeOut(tween(120)))
                                    // Going back: the sub-screen leaves toward the end edge, the parent settles in.
                                    to < from -> (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { -slideSign * it / 12 } +
                                        fadeIn(tween(200, delayMillis = 30)))
                                        .togetherWith(slideOutHorizontally(tween(240, easing = FastOutSlowInEasing)) { slideSign * it / 4 } +
                                            fadeOut(tween(160)))
                                    // Switching bottom tabs: a quick fade-through.
                                    else -> (fadeIn(tween(170, delayMillis = 50)) + scaleIn(tween(170, delayMillis = 50), initialScale = 0.985f))
                                        .togetherWith(fadeOut(tween(70)))
                                }
                            },
                            label = "destination"
                        ) { destination ->
                        when (destination) {
                            null -> QiblaScreen(
                                uiState = uiState,
                                onBack = { com.example.core.analytics.AppAnalytics.interaction("main_activity", "qibla_back", "back"); showQibla = false },
                                onUseCurrentLocation = {
                                    val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (!hasLocation) {
                                        useCurrentLocationAfterPermission = true
                                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    } else if (uiState.locationIssue == LocationIssue.SERVICES_OFF) {
                                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                    } else {
                                        viewModel.startLiveLocation(userRequested = true)
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
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "ramadan_back", backFromSecondary)
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
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "hadith_back", backFromSecondary)
                            )

                            AppDestination.DUAS -> DuasAdhkarScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )

                            AppDestination.FESTIVALS -> FestivalsScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "festivals_back", backFromSecondary)
                            )

                            AppDestination.CALENDAR -> IslamicCalendarScreen(
                                uiState = uiState,
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "calendar_back", backFromSecondary)
                            )

                            AppDestination.COMMUNITY_MASJID -> CommunityMasjidScreen(
                                onBack = { com.example.core.analytics.AppAnalytics.interaction("main_activity", "community_masjid_back", "back"); currentDestination = AppDestination.HOME }
                            )

                            AppDestination.NAAT -> NaatScreen(
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "naat_back", backFromSecondary)
                            )

                            AppDestination.AZAN -> AzanScreen(
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "azan_back", backFromSecondary)
                            )

                            AppDestination.BABY_NAMES -> BabyNamesScreen(onBack = backFromSecondary)

                            AppDestination.ALLAH_NAMES -> AllahNamesScreen(
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "allah_names_back", backFromSecondary)
                            )

                            AppDestination.PROPHET_NAMES -> ProphetNamesScreen(
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "prophet_names_back", backFromSecondary)
                            )

                            AppDestination.RECITATION_COLLECTION -> RecitationCollectionScreen(onBack = backFromSecondary)

                            AppDestination.RABBANA_DUAS -> RabbanaDuasScreen(
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "rabbana_duas_back", backFromSecondary)
                            )

                            AppDestination.KALIMAS -> KalimasScreen(onBack = backFromSecondary)

                            AppDestination.BOOKS -> IslamicBooksScreen(
                                isImmersive = readerImmersive,
                                onImmersiveChange = { readerImmersive = it },
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "books_back", backFromSecondary)
                            )

                            AppDestination.WORLD_LIBRARY -> WorldLibraryScreen(
                                isImmersive = readerImmersive,
                                onImmersiveChange = { readerImmersive = it },
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "world_library_back", backFromSecondary)
                            )

                            AppDestination.DAILY_TOOLS -> DailyToolScreen(
                                tool = activeDailyTool,
                                onBack = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "daily_tool_back", backFromSecondary),
                                onOpenQuran = { currentDestination = AppDestination.QURAN },
                                onOpenQibla = { showQibla = true },
                                uiState = uiState
                            )

                            AppDestination.MORE -> if (showMoreSettings) {
                                SettingsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onBack = { com.example.core.analytics.AppAnalytics.interaction("main_activity", "settings_back", "back"); showMoreSettings = false },
                                    // Back from the Azan screen returns to Settings, which stays open under More.
                                    onOpenAzan = { openSecondary(AppDestination.AZAN, AppDestination.MORE) }
                                )
                            } else {
                                MoreScreen(
                                    onNavigateToRamadan = { openSecondary(AppDestination.RAMADAN, AppDestination.MORE) },
                                    onNavigateToHadith = { openSecondary(AppDestination.HADITH, AppDestination.MORE) },
                                    onNavigateToFestivals = { openSecondary(AppDestination.FESTIVALS, AppDestination.MORE) },
                                    onNavigateToCalendar = { openSecondary(AppDestination.CALENDAR, AppDestination.MORE) },
                                    onNavigateToNaat = { openSecondary(AppDestination.NAAT, AppDestination.MORE) },
                                    onNavigateToAzan = { openSecondary(AppDestination.AZAN, AppDestination.MORE) },
                                    onNavigateToAllahNames = { openSecondary(AppDestination.ALLAH_NAMES, AppDestination.MORE) },
                                    onNavigateToBabyNames = { openSecondary(AppDestination.BABY_NAMES, AppDestination.MORE) },
                                    onNavigateToProphetNames = { openSecondary(AppDestination.PROPHET_NAMES, AppDestination.MORE) },
                                    onNavigateToRecitationCollection = { openSecondary(AppDestination.RECITATION_COLLECTION, AppDestination.MORE) },
                                    onNavigateToRabbanaDuas = { openSecondary(AppDestination.RABBANA_DUAS, AppDestination.MORE) },
                                    onNavigateToKalimas = { openSecondary(AppDestination.KALIMAS, AppDestination.MORE) },
                                    onNavigateToBooks = { openSecondary(AppDestination.BOOKS, AppDestination.MORE) },
                                    onNavigateToWorldLibrary = { openSecondary(AppDestination.WORLD_LIBRARY, AppDestination.MORE) },
                                    onOpenDailyTool = { tool ->
                                        activeDailyTool = tool
                                        openSecondary(AppDestination.DAILY_TOOLS, AppDestination.MORE)
                                    },
                                    onNavigateToQibla = { showQibla = true },
                                    onOpenSettings = { showMoreSettings = true },
                                    onStartTour = viewModel::startFeatureTour,
                                    tourSection = tourSection,
                                    onTourSpotlight = { moreTourRect = it }
                                )
                            }
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
                    Column(contentModifier) {
                        // Every screen shares one offline notice; full-screen readers keep the page clean.
                        if (!readerImmersive) com.example.ui.components.OfflineBanner()
                        val pageModifier = Modifier.fillMaxWidth().weight(1f)
                        if (refreshEnabled) {
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = com.example.core.analytics.AppAnalytics.trackAction("main_activity", "pull_to_refresh", viewModel::refreshAll),
                                modifier = pageModifier
                            ) { pageContent() }
                        } else {
                            Box(pageModifier) { pageContent() }
                        }
                    }
                }
                if (uiState.needsFeatureTour) {
                    // Until More has scrolled its tiles into place, the More tab itself is highlighted.
                    val tilesRect = moreTourRect.takeIf { tourSection != null }
                    FeatureTourOverlay(
                        stepIndex = tourStep,
                        spotlight = tilesRect ?: navItemBounds[featureTourSteps[tourStep].destination],
                        cardBottomY = if (tilesRect != null) navItemBounds[AppDestination.MORE]?.top else null,
                        onNext = {
                            com.example.core.analytics.AppAnalytics.interaction("feature_tour", "tour_next", item = "step_${tourStep + 1}")
                            if (tourStep < featureTourSteps.lastIndex) tourStep++ else {
                                com.example.core.analytics.AppAnalytics.milestone("onboarding", "tour_completed")
                                finishTour()
                            }
                        },
                        onSkip = {
                            com.example.core.analytics.AppAnalytics.milestone("onboarding", "tour_skipped", "step_${tourStep + 1}")
                            finishTour()
                        }
                    )
                    // Back steps through the tour instead of navigating the screens underneath it.
                    BackHandler { com.example.core.analytics.AppAnalytics.interaction("main_activity", "feature_tour_back", "tap"); if (tourStep > 0) tourStep-- else finishTour() }
                }
                }
                }
            }
        }
    }
}
