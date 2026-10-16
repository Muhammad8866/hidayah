package com.example.viewmodel

import android.app.Application
import android.app.LocaleManager
import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.GeomagneticField
import android.location.Location
import android.os.SystemClock
import android.location.Geocoder
import com.example.feature.citychallenge.CcLocalStore
import android.os.Build
import android.os.LocaleList
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.core.event.DomainEventBus
import com.example.core.event.LocationChangedEvent
import com.example.core.event.PrayerSettingsChangedEvent
import com.example.core.logging.SensitiveDataRedactor
import com.example.data.local.*
import com.example.domain.model.*
import com.example.domain.model.i18n.textIn
import com.example.festival.FestivalSchedule
import com.example.festival.IslamicFestivalRepository
import com.example.prayer.AstronomicalPrayerCalculator
import com.example.prayer.PrayerSettingsStore
import com.example.prayer.TimeZoneResolver
import com.example.prayer.HijriCalendarHelper
import com.example.prayer.HijriRegion
import com.example.prayer.PrayerNotificationReceiver
import com.example.prayer.QiblaCalculator
import com.example.prayer.domain.services.CompassHeadingFilter
import com.example.prayer.RamadanTimetable
import com.example.quran.QuranDatasetRepository
import com.example.quran.QuranAudioService
import com.example.quran.RemoteQuranRepository
import com.example.ui.localization.appLocale
import com.example.ui.localization.localizePhrase
import com.example.ui.localization.localizeUiText
import com.example.ui.localization.formatHijriDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil
import kotlin.math.roundToInt

data class MainUiState(
    val selectedCity: CityLocation = PredefinedLocations.DEFAULT_CITY,
    val hasConfirmedLocation: Boolean = false,
    val prayerConfig: PrayerTimeConfig = PrayerTimeConfig(),
    val rawPrayerTimes: AstronomicalPrayerCalculator.RawPrayerTimes? = null,
    val tomorrowPrayerTimes: AstronomicalPrayerCalculator.RawPrayerTimes? = null,
    val currentPrayer: PrayerType = PrayerType.ASR,
    val nextPrayer: PrayerType = PrayerType.MAGHRIB,
    val nextPrayerTimeFormatted: String = "",
    val minutesUntilNextPrayer: Long = 0,
    val prayerCalculationError: String? = null,
    /** Hijri date of today's civil date, the one calendars and moon-sighting announcements pair with it. */
    val hijriDate: HijriCalendarHelper.HijriDate = HijriCalendarHelper.getHijriDate(Calendar.getInstance()),
    /** After Maghrib, the Hijri date that has already begun for the coming night (null before Maghrib). */
    val hijriDateTonight: HijriCalendarHelper.HijriDate? = null,
    
    val pendingOutboxCount: Int = 0,
    
    // Quran Reader & Search (Canonical reference data)
    val allSurahs: List<Surah> = QuranDatasetRepository.AVAILABLE_SURAHS,
    val selectedSurah: Surah = QuranDatasetRepository.AVAILABLE_SURAHS.first(),
    val currentAyat: List<Ayah> = emptyList(),
    val quranSearchQuery: String = "",
    val quranSearchResults: List<Ayah> = emptyList(),
    val selectedWordForDetail: QuranWord? = null,
    val selectedAyahForTafsir: Ayah? = null,
    val quranReaderMode: QuranReaderMode = QuranReaderMode.UNDERSTAND,
    val quranPageNumber: Int = 1,
    val quranPageLines: List<MushafLine> = emptyList(),
    val quranSurahStartPages: Map<Int, Int> = emptyMap(),
    val quranPendingPage: Int? = null,
    val quranLoadError: String? = null,
    
    // Hifz (Personal progress - empty on fresh install)
    val hifzList: List<HifzProgressEntity> = emptyList(),
    val hifzConfidenceFilter: String = "All",
    
    // Duas & Adhkar (Canonical reference dataset)
    val duasScreenTab: DuasScreenTab = DuasScreenTab.DUAS,
    val selectedDuaCategory: String = DuaDatasetRepository.ALL_CATEGORY,
    val duaSearchQuery: String = "",
    val filteredDuas: List<Dua> = DuaDatasetRepository.DUAS,
    // Today's adhkar recitations, keyed "morning:<dua id>" / "evening:<dua id>"
    val adhkarProgress: Map<String, Int> = emptyMap(),
    val tasbeehCount: Int = 0,
    val tasbeehGoal: Int = 33,
    val tasbeehRounds: Int = 0,
    val tasbeehPhrase: String = TasbeehPreset.ALL.first().phrase,
    
    // Qibla
    val qiblaBearing: Double = 0.0,
    val distanceToMakkahKm: Double = 0.0,
    val deviceHeading: Float? = null,
    val magneticDeclination: Float? = null,
    val compassAccuracy: Int? = null,
    val isLiveLocation: Boolean = false,
    val locationStatus: String = "Detecting device location…",
    val locationIssue: LocationIssue? = null,
    val isQuranLoading: Boolean = true,
    
    // Islamic festivals (pending in the running Gregorian year)
    val festivalSchedule: FestivalSchedule? = null,
    val isFestivalsLoading: Boolean = false,

    // Ramadan timetable: non-null only while the live Hijri month is Ramadan
    val ramadanSchedule: RamadanTimetable.RamadanSchedule? = null,
    // Countdown to the next Ramadan: non-null whenever it is not Ramadan
    val upcomingRamadan: RamadanTimetable.UpcomingRamadan? = null,

    // Micro-learning
    val microLessons: List<MicroLesson> = LearningDatasetRepository.LESSONS,
    val selectedLesson: MicroLesson? = null,
    
    // Settings & Privacy
    val isDataSaverActive: Boolean = false,
    val isNotificationEnabled: Boolean = false,
    val notificationFeedbackMessage: String? = null,
    val hijriAdjustmentDays: Int = 0,
    /** Moon-sighting calendar chosen in Settings ([HijriRegion.id]); null follows the location. */
    val hijriCalendarId: String? = null,
    val languageCode: String = "en",
    /** True on a fresh install until the user confirms a language on the first-launch screen. */
    val needsLanguageSelection: Boolean = false,
    /** True until the feature tour has been finished or skipped once; More can start it again. */
    val needsFeatureTour: Boolean = false
)

data class AppLanguage(
    val code: String,
    val nativeName: String,
    /**
     * True for languages added by best-effort machine translation without scholarly/native
     * review. The picker surfaces this so users aren't misled about accuracy, especially for
     * religious wording. False (default) means the original reviewed Indian-subcontinent packs.
     */
    val reviewPending: Boolean = false
) {
    companion object {
        /**
         * Languages with an app-wide pack. Every non-English language must translate the
         * same UI copy as the Hindi reference pack (enforced by AppLocalizationTest); a
         * partially translated UI is worse than a clear fallback.
         */
        val supported = listOf(
            AppLanguage("en", "English"),
            AppLanguage("hinglish", "Hinglish (Roman Hindi)"),
            AppLanguage("hi", "हिन्दी (Hindi)"),
            AppLanguage("ur", "اردو (Urdu)"),
            AppLanguage("as", "অসমীয়া (Assamese)", reviewPending = true),
            AppLanguage("bn", "বাংলা (Bengali)", reviewPending = true),
            AppLanguage("brx", "बड़ो (Bodo)", reviewPending = true),
            AppLanguage("doi", "डोगरी (Dogri)", reviewPending = true),
            AppLanguage("gu", "ગુજરાતી (Gujarati)", reviewPending = true),
            AppLanguage("kn", "ಕನ್ನಡ (Kannada)", reviewPending = true),
            AppLanguage("ks", "کٲشُر (Kashmiri)", reviewPending = true),
            AppLanguage("kok", "कोंकणी (Konkani)", reviewPending = true),
            AppLanguage("mai", "मैथिली (Maithili)", reviewPending = true),
            AppLanguage("ml", "മലയാളം (Malayalam)", reviewPending = true),
            AppLanguage("mni", "মৈতৈলোন্ (Manipuri)", reviewPending = true),
            AppLanguage("mr", "मराठी (Marathi)", reviewPending = true),
            AppLanguage("ne", "नेपाली (Nepali)", reviewPending = true),
            AppLanguage("or", "ଓଡ଼ିଆ (Odia)", reviewPending = true),
            AppLanguage("pa", "ਪੰਜਾਬੀ (Punjabi)", reviewPending = true),
            AppLanguage("sa", "संस्कृतम् (Sanskrit)", reviewPending = true),
            AppLanguage("sat", "ᱥᱟᱱᱛᱟᱲᱤ (Santali)", reviewPending = true),
            AppLanguage("sd", "سنڌي (Sindhi)", reviewPending = true),
            AppLanguage("ta", "தமிழ் (Tamil)", reviewPending = true),
            AppLanguage("te", "తెలుగు (Telugu)", reviewPending = true),
            // World languages added by best-effort machine translation (reviewPending = true).
            AppLanguage("ar", "العربية (Arabic)", reviewPending = true),
            AppLanguage("fa", "فارسی (Persian)", reviewPending = true),
            AppLanguage("tr", "Türkçe (Turkish)", reviewPending = true),
            AppLanguage("id", "Bahasa Indonesia (Indonesian)", reviewPending = true),
            AppLanguage("fr", "Français (French)", reviewPending = true),
            AppLanguage("es", "Español (Spanish)", reviewPending = true),
            AppLanguage("ru", "Русский (Russian)", reviewPending = true),
            AppLanguage("de", "Deutsch (German)", reviewPending = true),
            AppLanguage("pt", "Português (Portuguese)", reviewPending = true),
            AppLanguage("zh", "中文 (Chinese)", reviewPending = true),
            AppLanguage("sw", "Kiswahili (Swahili)", reviewPending = true),
            AppLanguage("ha", "Hausa", reviewPending = true),
            AppLanguage("ms", "Bahasa Melayu (Malay)", reviewPending = true),
            AppLanguage("it", "Italiano (Italian)", reviewPending = true),
            AppLanguage("nl", "Nederlands (Dutch)", reviewPending = true),
            AppLanguage("vi", "Tiếng Việt (Vietnamese)", reviewPending = true),
            AppLanguage("th", "ไทย (Thai)", reviewPending = true),
            AppLanguage("tl", "Filipino (Tagalog)", reviewPending = true),
            AppLanguage("ps", "پښتو (Pashto)", reviewPending = true),
            AppLanguage("ja", "日本語 (Japanese)", reviewPending = true),
            AppLanguage("ko", "한국어 (Korean)", reviewPending = true),
            AppLanguage("el", "Ελληνικά (Greek)", reviewPending = true),
            AppLanguage("pl", "Polski (Polish)", reviewPending = true),
            AppLanguage("uk", "Українська (Ukrainian)", reviewPending = true),
            AppLanguage("so", "Soomaali (Somali)", reviewPending = true),
            AppLanguage("am", "አማርኛ (Amharic)", reviewPending = true),
            AppLanguage("uz", "Oʻzbekcha (Uzbek)", reviewPending = true),
            AppLanguage("he", "עברית (Hebrew)", reviewPending = true),
            AppLanguage("si", "සිංහල (Sinhala)", reviewPending = true)
        )

        fun normalize(code: String?): String {
            val candidate = code?.trim()?.lowercase(Locale.ROOT)?.replace('_', '-') ?: return "en"
            if (candidate == "hi-latn" || candidate.startsWith("hi-latn-")) return "hinglish"
            val base = candidate.substringBefore('-')
            return supported.firstOrNull { it.code == candidate }?.code
                ?: supported.firstOrNull { it.code == base }?.code
                ?: "en"
        }

        fun fromLocale(locale: Locale?): String? {
            locale ?: return null
            if (locale.language.equals("hi", ignoreCase = true) &&
                locale.script.equals("Latn", ignoreCase = true)
            ) return "hinglish"
            // Android still reports some languages under their retired ISO codes.
            val language = when (val raw = locale.language.lowercase(Locale.ROOT)) {
                "in" -> "id"
                "iw" -> "he"
                "fil" -> "tl"
                else -> raw
            }
            return supported.firstOrNull { it.code == language }?.code
        }
    }
}

/** Why live GPS is unavailable, so the dashboard can show an actionable pill instead of spinning forever. */
enum class LocationIssue {
    PERMISSION_DENIED,
    SERVICES_OFF
}

enum class QuranReaderMode {
    READ,
    UNDERSTAND,
    MEMORIZE
}

enum class DuasScreenTab {
    DUAS,
    ADHKAR,
    TASBEEH
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appPreferences = PrayerSettingsStore.prefs(application)
    /** Excluded from backup, so it describes this install only and is empty after a reinstall. */
    private val installPreferences = application.getSharedPreferences(
        "install_state",
        android.content.Context.MODE_PRIVATE
    )
    private val platformLocaleManagedKey = "platform_locale_managed"
    private val languageSelectionDoneKey = "language_selection_done"
    private val featureTourDoneKey = "feature_tour_done"

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val sensorManager = application.getSystemService(SensorManager::class.java)
    private var locationCallback: LocationCallback? = null
    private var currentLocationRequest: CancellationTokenSource? = null
    @Volatile private var liveLocationGeneration = 0L
    @Volatile private var useLiveLocation = PrayerSettingsStore.useLiveLocation(appPreferences)
    private var rotationSensor: Sensor? = null
    private var sensorListener: SensorEventListener? = null
    private val headingFilter = CompassHeadingFilter()
    private var festivalRefreshGeneration = 0L
    private var quranPageLoadJob: Job? = null
    private var quranPageLoadGeneration = 0L
    private val db: DeenDatabase by lazy {
        Room.databaseBuilder(
            application.applicationContext,
            DeenDatabase::class.java,
            "deen_companion.db"
        ).addMigrations(DeenDatabase.MIGRATION_2_3).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    private val _uiState = MutableStateFlow(loadPersistedState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /** Restores user choices before the first UI frame. App features/content remain in source. */
    private fun loadPersistedState(): MainUiState {
        PrayerSettingsStore.restoreHijriPreference(appPreferences)
        val defaults = MainUiState()
        val city = PrayerSettingsStore.loadCity(appPreferences, defaults.selectedCity)
        val config = PrayerSettingsStore.loadConfig(appPreferences, defaults.prayerConfig)
        return defaults.copy(
            selectedCity = city,
            hasConfirmedLocation = PrayerSettingsStore.hasSavedCity(appPreferences),
            prayerConfig = config,
            isNotificationEnabled = PrayerSettingsStore.notificationsEnabled(appPreferences),
            languageCode = loadLanguageCode(),
            needsLanguageSelection = loadNeedsLanguageSelection(),
            needsFeatureTour = !appPreferences.getBoolean(featureTourDoneKey, false),
            quranReaderMode = enumPreference("quran_reader_mode", defaults.quranReaderMode),
            quranPageNumber = appPreferences.getInt("quran_page", 1).coerceIn(1, 847),
            tasbeehCount = appPreferences.getInt("tasbeeh_count", 0).coerceAtLeast(0),
            tasbeehGoal = appPreferences.getInt("tasbeeh_goal", 33).coerceAtLeast(1),
            tasbeehRounds = appPreferences.getInt("tasbeeh_rounds", 0).coerceAtLeast(0),
            tasbeehPhrase = appPreferences.getString("tasbeeh_phrase", defaults.tasbeehPhrase)
                ?: defaults.tasbeehPhrase,
            adhkarProgress = loadAdhkarProgress(),
            hijriCalendarId = HijriRegion.preferred?.id
        )
    }

    /** Adhkar progress belongs to one calendar day; a new day starts from zero. */
    private fun adhkarDayStamp(): Int = Calendar.getInstance().let {
        it.get(Calendar.YEAR) * 1000 + it.get(Calendar.DAY_OF_YEAR)
    }

    private fun isAdhkarProgressFromToday(): Boolean =
        appPreferences.getInt("adhkar_progress_day", -1) == adhkarDayStamp()

    private fun loadAdhkarProgress(): Map<String, Int> {
        if (!isAdhkarProgressFromToday()) return emptyMap()
        return appPreferences.getString("adhkar_progress", null).orEmpty()
            .split(';')
            .mapNotNull { entry ->
                val key = entry.substringBefore('=', "")
                val count = entry.substringAfter('=', "").toIntOrNull()
                if (key.isEmpty() || count == null || count <= 0) null else key to count
            }
            .toMap()
    }

    private fun saveAdhkarProgress(progress: Map<String, Int>) {
        appPreferences.edit()
            .putInt("adhkar_progress_day", adhkarDayStamp())
            .putString("adhkar_progress", progress.entries.joinToString(";") { "${it.key}=${it.value}" })
            .apply()
        _uiState.update { it.copy(adhkarProgress = progress) }
    }

    private fun todaysAdhkarProgress(): Map<String, Int> =
        if (isAdhkarProgressFromToday()) _uiState.value.adhkarProgress else emptyMap()

    /**
     * Android 13's per-app language setting is authoritative when present. On a fresh install the
     * app follows a supported device language; an explicit in-app choice is persisted thereafter.
     */
    private fun loadLanguageCode(): String {
        val platformLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching {
                getApplication<Application>().getSystemService(LocaleManager::class.java)
                    ?.applicationLocales
                    ?.takeUnless { it.isEmpty }
                    ?.get(0)
            }.getOrNull()
        } else null
        AppLanguage.fromLocale(platformLocale)?.let { return it }

        appPreferences.getString("language_code", null)?.let { return AppLanguage.normalize(it) }
        val deviceLocale = getApplication<Application>().resources.configuration.locales.get(0)
        return AppLanguage.fromLocale(deviceLocale) ?: "en"
    }

    /**
     * The language screen opens the app until a language has been picked on this install. Only
     * the non-backed-up flag is trusted: a backup restore brings the main preferences back before
     * the first launch, and an install over an older build has never asked at all.
     */
    private fun loadNeedsLanguageSelection(): Boolean =
        !installPreferences.getBoolean(languageSelectionDoneKey, false)

    /**
     * Applies the language picked on the first-launch screen, never shows that screen again, and
     * hands over to the feature tour so it runs in the language that was just chosen.
     */
    fun completeLanguageSelection(code: String) {
        com.example.core.analytics.AppAnalytics.setting("complete_language_selection", code)
        com.example.core.analytics.AppAnalytics.milestone("onboarding", "language_selected", code)
        installPreferences.edit().putBoolean(languageSelectionDoneKey, true).apply()
        // A restored backup may already carry the "done" flag; the tour still follows the picker.
        appPreferences.edit().putBoolean(featureTourDoneKey, false).apply()
        _uiState.update { it.copy(needsLanguageSelection = false, needsFeatureTour = true) }
        setLanguage(code)
    }

    /** Finishing and skipping the tour both end it for good. */
    fun completeFeatureTour() {
        com.example.core.analytics.AppAnalytics.setting("complete_feature_tour")
        appPreferences.edit().putBoolean(featureTourDoneKey, true).apply()
        _uiState.update { it.copy(needsFeatureTour = false) }
    }

    /** Replays the tour from More without forgetting that it has already been seen. */
    fun startFeatureTour() {
        com.example.core.analytics.AppAnalytics.setting("start_feature_tour")
        _uiState.update { it.copy(needsFeatureTour = true) }
    }

    private fun confirmedHijriMonthsKey(region: HijriRegion) = PrayerSettingsStore.confirmedHijriMonthsKey(region)

    private companion object {
        const val HIJRI_CALENDAR_KEY = PrayerSettingsStore.HIJRI_CALENDAR_KEY
    }

    private inline fun <reified T : Enum<T>> enumPreference(key: String, default: T): T =
        appPreferences.getString(key, null)?.let { saved ->
            enumValues<T>().firstOrNull { it.name == saved }
        } ?: default

    private fun persistCity(city: CityLocation) = PrayerSettingsStore.saveCity(appPreferences, city)

    private fun persistPrayerConfig(config: PrayerTimeConfig) = PrayerSettingsStore.saveConfig(appPreferences, config)

    fun setLanguage(code: String) {
        com.example.core.analytics.AppAnalytics.setting("set_language", code)
        val normalized = AppLanguage.supported
            .firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }
            ?.code
            ?: AppLanguage.fromLocale(Locale.forLanguageTag(code.trim().replace('_', '-')))
            ?: return
        appPreferences.edit()
            .putString("language_code", normalized)
            .putBoolean(platformLocaleManagedKey, Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            .apply()
        _uiState.update {
            it.copy(
                languageCode = normalized,
                filteredDuas = filterDuas(it.selectedDuaCategory, it.duaSearchQuery, normalized)
            )
        }
        recalculatePrayerTimes()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching {
                getApplication<Application>().getSystemService(LocaleManager::class.java)
                    ?.applicationLocales = LocaleList.forLanguageTags(appLocale(normalized).toLanguageTag())
            }
        }
        PrayerNotificationReceiver.createNotificationChannel(getApplication())
        QuranAudioService.update(getApplication())
        com.example.core.analytics.AnalyticsProfile.sync(getApplication())
    }

    /** Picks up changes made from Android's Settings > App languages while the app was away. */
    fun syncPlatformLanguage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        // Nothing has been chosen yet; the first-launch screen sets the language.
        if (_uiState.value.needsLanguageSelection) return
        val application = getApplication<Application>()
        val localeManager = runCatching {
            application.getSystemService(LocaleManager::class.java)
        }.getOrNull() ?: return
        val platformLocales = runCatching { localeManager.applicationLocales }.getOrNull() ?: return

        val code = if (platformLocales.isEmpty) null else AppLanguage.fromLocale(platformLocales.get(0))
        if (code == null) {
            // The platform holds no usable per-app language (restored backup, a choice made before
            // Android 13 per-app locales, a cleared setting). The language chosen in the app stays
            // and is handed back to the platform instead of falling back to the device language.
            val chosen = _uiState.value.languageCode
            runCatching {
                localeManager.applicationLocales =
                    LocaleList.forLanguageTags(appLocale(chosen).toLanguageTag())
            }
            appPreferences.edit()
                .putString("language_code", chosen)
                .putBoolean(platformLocaleManagedKey, true)
                .apply()
            return
        }

        appPreferences.edit()
            .putString("language_code", code)
            .putBoolean(platformLocaleManagedKey, true)
            .apply()
        if (_uiState.value.languageCode == code) return
        _uiState.update {
            it.copy(languageCode = code, filteredDuas = filterDuas(it.selectedDuaCategory, it.duaSearchQuery, code))
        }
        recalculatePrayerTimes()
        PrayerNotificationReceiver.createNotificationChannel(application)
        QuranAudioService.update(application)
    }

    val bookmarks: StateFlow<List<BookmarkEntity>> = db.deenDao().getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        recalculatePrayerTimes()
        recalculateQibla()
        observeHifz()
        observeOutboxQueue()
        observeDomainEvents()
        loadQuranPage(_uiState.value.quranPageNumber)
        refreshFestivals()
        // Setting choices double as report dimensions; city and coordinates are never part of them.
        viewModelScope.launch {
            uiState.map { listOf(it.languageCode, it.prayerConfig, it.hijriCalendarId, it.isNotificationEnabled,
                it.quranReaderMode, it.isLiveLocation, it.hasConfirmedLocation) }
                .distinctUntilChanged()
                .collect { com.example.core.analytics.AnalyticsProfile.sync(getApplication()) }
        }
        viewModelScope.launch {
            var lastHijriBoundary = currentHijriBoundaryKey()
            while (true) {
                delay(60_000)
                val boundary = currentHijriBoundaryKey()
                if (boundary != lastHijriBoundary) {
                    lastHijriBoundary = boundary
                    recalculatePrayerTimes()
                    refreshFestivals()
                } else {
                    refreshPrayerProgress()
                }
            }
        }
    }

    private fun observeDomainEvents() {
        viewModelScope.launch {
            DomainEventBus.observe<PrayerSettingsChangedEvent>().collect { event ->
                persistPrayerConfig(event.newConfig)
                _uiState.update { it.copy(prayerConfig = event.newConfig) }
                recalculatePrayerTimes()
            }
        }
        viewModelScope.launch {
            DomainEventBus.observe<LocationChangedEvent>().collect { event ->
                if (event.liveLocationGeneration != null &&
                    (!useLiveLocation || event.liveLocationGeneration != liveLocationGeneration)) return@collect
                persistCity(event.newLocation)
                _uiState.update {
                    it.copy(
                        selectedCity = event.newLocation,
                        hasConfirmedLocation = true,
                        isLiveLocation = event.liveLocationGeneration != null,
                        locationIssue = null,
                        locationStatus = if (event.liveLocationGeneration != null) "Live GPS • ${event.newLocation.name}"
                            else "Selected city • ${event.newLocation.name}"
                    )
                }
                recalculatePrayerTimes()
                recalculateQibla()
                refreshFestivals()
            }
        }
    }

    private fun observeHifz() {
        viewModelScope.launch {
            db.deenDao().getAllHifz().collect { hifzList ->
                _uiState.update { it.copy(hifzList = hifzList) }
            }
        }
    }

    private fun observeOutboxQueue() {
        viewModelScope.launch {
            db.deenDao().getPendingOutboxMutations().collect { pending ->
                _uiState.update { it.copy(pendingOutboxCount = pending.size) }
            }
        }
    }

    fun selectCity(city: CityLocation) {
        com.example.core.analytics.AppAnalytics.setting("set_location", "manual_city")
        stopLiveLocation()
        viewModelScope.launch {
            DomainEventBus.publish(LocationChangedEvent(city))
        }
    }

    private fun stopLiveLocation() {
        useLiveLocation = false
        PrayerSettingsStore.saveUseLiveLocation(appPreferences, false)
        liveLocationGeneration++
        currentLocationRequest?.cancel()
        currentLocationRequest = null
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
        _uiState.update { it.copy(isLiveLocation = false, magneticDeclination = cityDeclination(it.selectedCity), locationIssue = null) }
    }

    /** Stops GPS while the app is in the background; [startLiveLocation] resumes it on return. */
    fun pauseLiveLocation() {
        currentLocationRequest?.cancel()
        currentLocationRequest = null
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    fun usesLiveLocation(): Boolean = useLiveLocation

    /** Called after the user grants precise location. Updates GPS every 30 seconds while app is open. */
    fun startLiveLocation(userRequested: Boolean = false) {
        if (userRequested && !useLiveLocation) {
            com.example.core.analytics.AppAnalytics.setting("set_location", "gps")
            useLiveLocation = true
            PrayerSettingsStore.saveUseLiveLocation(appPreferences, true)
            liveLocationGeneration++
        }
        if (!useLiveLocation) return
        val fineGranted = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            _uiState.update { it.copy(isLiveLocation = false, magneticDeclination = cityDeclination(it.selectedCity), locationIssue = LocationIssue.PERMISSION_DENIED) }
            return
        }
        val locationManager = getApplication<Application>().getSystemService(android.location.LocationManager::class.java)
        if (locationManager != null && !androidx.core.location.LocationManagerCompat.isLocationEnabled(locationManager)) {
            _uiState.update { it.copy(isLiveLocation = false, magneticDeclination = cityDeclination(it.selectedCity), locationIssue = LocationIssue.SERVICES_OFF) }
        } else {
            _uiState.update { it.copy(locationIssue = null) }
        }
        val priority = if (fineGranted) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    updateLiveLocation(location)
                }
            }
            val request = LocationRequest.Builder(priority, 30_000)
                .setMinUpdateIntervalMillis(10_000).setWaitForAccurateLocation(false).build()
            // Permission can be revoked between the check above and this call; the dashboard then keeps its city.
            runCatching {
                fusedLocationClient.requestLocationUpdates(request, locationCallback!!, android.os.Looper.getMainLooper())
            }.onFailure { locationCallback = null }
        }

        // Ask for a fresh fix whenever the app returns to the foreground. If Android
        // location was just switched on this updates the dashboard without a tap.
        currentLocationRequest?.cancel()
        val cancellationTokenSource = CancellationTokenSource().also { currentLocationRequest = it }
        runCatching {
            fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                    location?.let { updateLiveLocation(it) }
                }
        }

        // Fall back to Android's cached fix while a fresh one is being acquired.
        runCatching {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { updateLiveLocation(it) }
            }
        }
    }

    private fun updateLiveLocation(location: Location) {
        if (!useLiveLocation) return
        val generation = liveLocationGeneration
        // A stale cached fix can put the compass's magnetic correction in the wrong place.
        val ageNanos = SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos
        if (ageNanos < 0 || ageNanos > 10L * 60L * 1_000_000_000L) return
        val latitude = location.latitude
        val longitude = location.longitude
        val declination = GeomagneticField(
            latitude.toFloat(), longitude.toFloat(), location.altitude.toFloat(), System.currentTimeMillis()
        ).declination
        _uiState.update { it.copy(magneticDeclination = declination) }
        val previous = _uiState.value.selectedCity
        // Ignore GPS jitter so the dashboard does not repeatedly change/recalculate.
        if (_uiState.value.isLiveLocation && kotlin.math.abs(previous.latitude - latitude) < 0.002 && kotlin.math.abs(previous.longitude - longitude) < 0.002) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val address = runCatching {
                val geocoder = Geocoder(getApplication(), appLocale(_uiState.value.languageCode))
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            }.getOrNull()
            val area = address?.subLocality ?: address?.locality ?: address?.subAdminArea ?: address?.adminArea
            val cityName = address?.locality
            val displayName = listOfNotNull(area, cityName?.takeUnless { it.equals(area, ignoreCase = true) })
                .distinct()
                .joinToString(", ")
                .ifBlank { "Current location" }
            val region = listOfNotNull(address?.adminArea, address?.countryName)
                .distinct()
                .joinToString(", ")
                .ifBlank { "Live GPS" }
            val zoneId = TimeZoneResolver.resolve(longitude, address?.countryCode)
            val city = CityLocation(displayName, region, latitude, longitude, zoneId)
            if (useLiveLocation && generation == liveLocationGeneration) {
                DomainEventBus.publish(LocationChangedEvent(city, liveLocationGeneration = generation))
            }
        }
    }

    fun startCompass() {
        if (sensorListener != null) return
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotation = FloatArray(9)
                val orientation = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                // Held upright, azimuth of the flat-phone axes becomes unstable; use the
                // direction the back of the phone faces instead.
                val matrix = if (kotlin.math.abs(rotation[8]) < 0.5f) {
                    FloatArray(9).also { SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, it) }
                } else rotation
                SensorManager.getOrientation(matrix, orientation)
                val magneticHeading = Math.toDegrees(orientation[0].toDouble())
                val heading = headingFilter.filter(magneticHeading).toFloat()
                _uiState.update { it.copy(deviceHeading = heading, compassAccuracy = event.accuracy) }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                _uiState.update { it.copy(compassAccuracy = accuracy) }
            }
        }
        rotationSensor?.let { sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopCompass() {
        sensorListener?.let { sensorManager?.unregisterListener(it) }
        sensorListener = null
        headingFilter.reset()
        _uiState.update { it.copy(deviceHeading = null, compassAccuracy = null) }
    }

    fun setCustomCoordinates(name: String, latitude: Double, longitude: Double, timeZoneId: String? = null) {
        com.example.core.analytics.AppAnalytics.setting("set_location", "custom_coordinates")
        stopLiveLocation()
        val sanitizedName = SensitiveDataRedactor.redact(name)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // The coordinates may be in another country, so the phone's zone is not assumed.
            val zoneId = timeZoneId ?: run {
                val countryCode = runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(getApplication(), Locale.ROOT).getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()?.countryCode
                }.getOrNull()
                TimeZoneResolver.resolve(longitude, countryCode)
            }
            val customLoc = CityLocation(
                name = sanitizedName,
                stateOrCountry = "Custom Coordinates",
                latitude = latitude,
                longitude = longitude,
                timeZoneId = zoneId
            )
            DomainEventBus.publish(LocationChangedEvent(customLoc))
        }
    }

    /** Follows [region]'s moon-sighting calendar wherever the user is; null goes back to the location's own. */
    fun setHijriCalendar(region: HijriRegion?) {
        com.example.core.analytics.AppAnalytics.setting("set_hijri_calendar", region?.id ?: "automatic")
        HijriRegion.preferred = region
        appPreferences.edit().putString(HIJRI_CALENDAR_KEY, region?.id).apply()
        _uiState.update { it.copy(hijriCalendarId = region?.id) }
        recalculatePrayerTimes()
        refreshFestivals()
    }

    /** Corrects today's Hijri date to a local moon-sighting announcement that differs from the prediction. */
    fun setHijriAdjustmentDays(days: Int) {
        com.example.core.analytics.AppAnalytics.setting("set_hijri_adjustment_days", if (days < 0) "minus_${-days}" else "plus_$days")
        val state = _uiState.value
        val region = HijriRegion.forTimeZone(state.selectedCity.timeZoneId)
        val cal = Calendar.getInstance(TimeZone.getTimeZone(state.selectedCity.timeZoneId))
        HijriCalendarHelper.adjustDate(cal, maghribRollover(cal, state.rawPrayerTimes), days.coerceIn(-2, 2), region)
        appPreferences.edit().putString(confirmedHijriMonthsKey(region), HijriCalendarHelper.confirmedMonths(region)).apply()
        recalculatePrayerTimes()
        refreshFestivals()
    }

    /** In the Islamic calendar a new day begins at Maghrib, not at civil midnight. */
    private fun maghribRollover(cal: Calendar, raw: AstronomicalPrayerCalculator.RawPrayerTimes?): Int {
        val currentHour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0
        return if (raw != null && currentHour >= raw.maghribHours) 1 else 0
    }

    fun refreshFestivals(): kotlinx.coroutines.Job {
        val generation = ++festivalRefreshGeneration
        val state = _uiState.value
        _uiState.update { it.copy(isFestivalsLoading = true) }
        return viewModelScope.launch {
            val schedule = runCatching {
                IslamicFestivalRepository.load(state.selectedCity.timeZoneId)
            }.getOrNull()
            if (generation == festivalRefreshGeneration) {
                _uiState.update { it.copy(festivalSchedule = schedule ?: it.festivalSchedule, isFestivalsLoading = false) }
            }
        }
    }

    private fun currentHijriBoundaryKey(): Int {
        val state = _uiState.value
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(state.selectedCity.timeZoneId))
        val maghrib = state.rawPrayerTimes?.maghribHours ?: 24.0
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0
        return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR) * 2 +
            if (currentHour >= maghrib) 1 else 0
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** Pull-to-refresh: reloads location, prayer times, Hijri date, and festivals. */
    fun refreshAll() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        viewModelScope.launch {
            val started = SystemClock.elapsedRealtime()
            try {
                val festivals = refreshRealtimeDate()
                runCatching { startLiveLocation() }
                festivals.join()
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                // A refresh that fails leaves the previous data in place; nothing to surface here.
            } finally {
                // The indicator stays visible long enough to read as a completed refresh, not a flicker.
                val elapsed = SystemClock.elapsedRealtime() - started
                if (elapsed < 500) kotlinx.coroutines.delay(500 - elapsed)
                _isRefreshing.value = false
            }
        }
    }

    /** Refreshes time-sensitive dashboard data whenever the app enters the foreground. */
    fun refreshRealtimeDate(): kotlinx.coroutines.Job {
        recalculatePrayerTimes()
        return refreshFestivals()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        val allowed = !enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        val active = enabled && allowed
        com.example.core.analytics.AppAnalytics.setting("set_notifications_enabled", if (active) "on" else if (enabled) "blocked" else "off")
        PrayerSettingsStore.saveNotificationsEnabled(appPreferences, active)
        _uiState.update { it.copy(isNotificationEnabled = active) }
        if (active) {
            schedulePrayerNotifications()
            _uiState.update { it.copy(notificationFeedbackMessage =
                if (it.hasConfirmedLocation) "Prayer notifications enabled and scheduled."
                else "Prayer notifications enabled. Choose a city to schedule alerts.") }
        } else {
            PrayerNotificationReceiver.cancelPrayerAlerts(getApplication())
            _uiState.update { it.copy(notificationFeedbackMessage =
                if (enabled) "Allow notifications to enable prayer alerts." else "Prayer notifications disabled.") }
        }
    }

    fun refreshNotificationPermission() {
        if (_uiState.value.isNotificationEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            setNotificationsEnabled(false)
        }
    }

    fun clearNotificationFeedback() {
        _uiState.update { it.copy(notificationFeedbackMessage = null) }
    }

    fun triggerTestNotification() {
        if (!_uiState.value.hasConfirmedLocation) {
            _uiState.update { it.copy(notificationFeedbackMessage = "Choose a city before testing prayer alerts.") }
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            _uiState.update { it.copy(notificationFeedbackMessage = "Allow notifications to enable prayer alerts.") }
            return
        }
        val city = _uiState.value.selectedCity
        val hijriFormatted = formatHijriDate(_uiState.value.hijriDate, _uiState.value.languageCode)
        PrayerNotificationReceiver.deliverPrayerAlert(
            getApplication(),
            PrayerNotificationReceiver.alertDetails(
                prayerName = _uiState.value.nextPrayer.displayName,
                prayerArabic = _uiState.value.nextPrayer.arabicName,
                prayerTime = _uiState.value.nextPrayerTimeFormatted,
                location = city.name,
                hijriDate = hijriFormatted
            ),
            test = true
        )
        _uiState.update { it.copy(notificationFeedbackMessage = "Test prayer alert sent.") }
    }

    private fun schedulePrayerNotifications() {
        val raw = _uiState.value.rawPrayerTimes ?: return
        val city = _uiState.value.selectedCity
        val cal = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId))
        PrayerNotificationReceiver.schedulePrayerAlerts(
            context = getApplication(),
            rawTimes = raw,
            config = _uiState.value.prayerConfig,
            city = city,
            calendar = cal
        )
    }

    fun updatePrayerConfig(config: PrayerTimeConfig) {
        val previous = _uiState.value.prayerConfig
        when {
            config.method != previous.method -> com.example.core.analytics.AppAnalytics.setting("set_prayer_method", config.method.name)
            config.asrMethod != previous.asrMethod -> com.example.core.analytics.AppAnalytics.setting("set_asr_method", config.asrMethod.name)
            config.highLatitudeRule != previous.highLatitudeRule ->
                com.example.core.analytics.AppAnalytics.setting("set_high_latitude_rule", config.highLatitudeRule.name)
            else -> com.example.core.analytics.AppAnalytics.setting("update_prayer_config")
        }
        viewModelScope.launch {
            DomainEventBus.publish(PrayerSettingsChangedEvent(config))
        }
    }

    fun selectSurah(surah: Surah) {
        _uiState.update {
            it.copy(
                selectedSurah = surah,
                currentAyat = emptyList(),
                isQuranLoading = true,
                selectedWordForDetail = null,
                selectedAyahForTafsir = null
            )
        }
        loadKanzulImanSurah(surah)
    }

    fun loadQuranPage(page: Int) {
        val safePage = page.coerceIn(1, 847)
        appPreferences.edit().putInt("quran_page", safePage).apply()
        val generation = ++quranPageLoadGeneration
        quranPageLoadJob?.cancel()
        _uiState.update {
            it.copy(
                quranPendingPage = safePage,
                quranLoadError = null,
                isQuranLoading = true
            )
        }
        quranPageLoadJob = viewModelScope.launch {
            val result = try {
                val lines = RemoteQuranRepository.loadMushafPage(getApplication(), safePage)
                val surahPages = if (_uiState.value.quranSurahStartPages.size == 114) {
                    _uiState.value.quranSurahStartPages
                } else {
                    RemoteQuranRepository.loadSurahStartPages(getApplication())
                }
                check(lines.isNotEmpty()) { "Verified Quran page cannot be empty" }
                Result.success(lines to surahPages)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                Result.failure(error)
            }
            if (generation != quranPageLoadGeneration) return@launch
            _uiState.update { current ->
                result.fold(
                    onSuccess = { (lines, surahPages) ->
                    current.copy(
                        quranPageNumber = safePage,
                        quranPageLines = lines,
                        quranSurahStartPages = surahPages,
                        quranPendingPage = null,
                        quranLoadError = null,
                        isQuranLoading = false
                    )
                    },
                    onFailure = {
                        current.copy(
                            quranPendingPage = safePage,
                            quranLoadError = "Quran page could not be verified and was not displayed.",
                            isQuranLoading = false
                        )
                    }
                )
            }
        }
    }

    private fun loadKanzulImanSurah(surah: Surah) {
        viewModelScope.launch {
            val ayat = runCatching { RemoteQuranRepository.loadSurah(surah.number, surah.totalAyat) }
                .getOrDefault(emptyList())
            _uiState.update { current ->
                if (current.selectedSurah.number != surah.number) current
                else current.copy(
                    currentAyat = ayat,
                    isQuranLoading = false,
                    quranLoadError = if (ayat.isEmpty()) "Surah could not be loaded. Check your internet connection and try again." else null
                )
            }
        }
    }

    fun setQuranReaderMode(mode: QuranReaderMode) {
        com.example.core.analytics.AppAnalytics.setting("set_quran_reader_mode", mode.name)
        appPreferences.edit().putString("quran_reader_mode", mode.name).apply()
        _uiState.update { it.copy(quranReaderMode = mode) }
    }

    fun openWordDetail(word: QuranWord?) {
        _uiState.update { it.copy(selectedWordForDetail = word) }
    }

    fun openAyahTafsir(ayah: Ayah?) {
        _uiState.update { it.copy(selectedAyahForTafsir = ayah) }
    }

    fun searchQuran(query: String) {
        val results = QuranDatasetRepository.searchQuran(query)
        _uiState.update { it.copy(quranSearchQuery = query, quranSearchResults = results) }
    }

    fun selectDuasScreenTab(tab: DuasScreenTab) {
        _uiState.update { it.copy(duasScreenTab = tab) }
    }

    /** Search runs inside the selected category, so both filters always apply together. */
    private fun filterDuas(category: String, query: String, language: String): List<Dua> =
        DuaDatasetRepository.filter(category, query) { dua ->
            listOf(
                localizePhrase(dua.title, language),
                localizeUiText(dua.category, language),
                localizePhrase(dua.context, language),
                localizePhrase(if (language == "ur") dua.urduTranslation else dua.englishTranslation, language),
                localizePhrase(dua.benefit, language),
                localizePhrase(dua.authenticityGrading, language),
                dua.englishMeaning,
                dua.englishContext,
                dua.englishBenefit,
                dua.textIn(language)?.let { "${it.title} ${it.meaning.orEmpty()} ${it.context} ${it.benefit}" }.orEmpty()
            ).joinToString(" ")
        }

    fun searchDuas(query: String) {
        _uiState.update {
            it.copy(
                duaSearchQuery = query,
                filteredDuas = filterDuas(it.selectedDuaCategory, query, it.languageCode)
            )
        }
    }

    fun selectDuaCategory(category: String) {
        _uiState.update {
            it.copy(
                selectedDuaCategory = category,
                filteredDuas = filterDuas(category, it.duaSearchQuery, it.languageCode)
            )
        }
    }

    /** Counts one recitation of an adhkar entry, never past its [target]. */
    fun countAdhkar(key: String, target: Int) {
        val progress = todaysAdhkarProgress()
        val current = progress[key] ?: 0
        if (current >= target) return
        if (current + 1 == target) com.example.core.analytics.AppAnalytics.milestone("adhkar", "dhikr_completed")
        saveAdhkarProgress(progress + (key to current + 1))
    }

    fun resetAdhkar(keys: Collection<String>) {
        saveAdhkarProgress(todaysAdhkarProgress() - keys.toSet())
    }

    /** Clears yesterday's adhkar ticks when the screen is opened on a new day. */
    fun refreshAdhkarDay() {
        if (!isAdhkarProgressFromToday() && _uiState.value.adhkarProgress.isNotEmpty()) {
            saveAdhkarProgress(emptyMap())
        }
    }

    fun selectLesson(lesson: MicroLesson?) {
        _uiState.update { it.copy(selectedLesson = lesson) }
    }

    /**
     * Counts up to the goal and stays there so the full count is visible; the next tap starts a
     * new round at 1. A round is recorded the moment the goal is reached.
     */
    fun incrementTasbeeh() {
        val state = _uiState.value
        val next = if (state.tasbeehCount >= state.tasbeehGoal) 1 else state.tasbeehCount + 1
        val rounds = if (next == state.tasbeehGoal) state.tasbeehRounds + 1 else state.tasbeehRounds
        // Only that a round was finished: not the phrase, the goal or the running count.
        if (next == state.tasbeehGoal) com.example.core.analytics.AppAnalytics.milestone("tasbeeh", "round_completed")
        appPreferences.edit()
            .putInt("tasbeeh_count", next)
            .putInt("tasbeeh_rounds", rounds)
            .apply()
        CcLocalStore.addDhikr(getApplication())
        _uiState.update { it.copy(tasbeehCount = next, tasbeehRounds = rounds) }
    }

    /** Takes back the last count after a stray tap; a round that tap completed is taken back too. */
    fun undoTasbeeh() {
        val state = _uiState.value
        if (state.tasbeehCount <= 0) return
        val previous = state.tasbeehCount - 1
        val rounds = if (state.tasbeehCount == state.tasbeehGoal) {
            (state.tasbeehRounds - 1).coerceAtLeast(0)
        } else {
            state.tasbeehRounds
        }
        appPreferences.edit()
            .putInt("tasbeeh_count", previous)
            .putInt("tasbeeh_rounds", rounds)
            .apply()
        _uiState.update { it.copy(tasbeehCount = previous, tasbeehRounds = rounds) }
    }

    fun resetTasbeeh() {
        com.example.core.analytics.AppAnalytics.setting("reset_tasbeeh")
        appPreferences.edit()
            .putInt("tasbeeh_count", 0)
            .putInt("tasbeeh_rounds", 0)
            .apply()
        _uiState.update { it.copy(tasbeehCount = 0, tasbeehRounds = 0) }
    }

    fun setTasbeehPreset(phrase: String, goal: Int) {
        com.example.core.analytics.AppAnalytics.setting("set_tasbeeh_preset")
        val safeGoal = goal.coerceAtLeast(1)
        appPreferences.edit()
            .putString("tasbeeh_phrase", phrase)
            .putInt("tasbeeh_goal", safeGoal)
            .putInt("tasbeeh_count", 0)
            .putInt("tasbeeh_rounds", 0)
            .apply()
        _uiState.update {
            it.copy(tasbeehPhrase = phrase, tasbeehGoal = safeGoal, tasbeehCount = 0, tasbeehRounds = 0)
        }
    }


    fun toggleBookmark(surah: Surah, ayah: Ayah) {
        viewModelScope.launch {
            val id = "${surah.number}:${ayah.ayahNumber}"
            val existing = bookmarks.value.find { it.id == id }
            if (existing != null) {
                db.deenDao().deleteBookmark(id)
            } else {
                db.deenDao().insertBookmark(
                    BookmarkEntity(
                        id = id,
                        surahNumber = surah.number,
                        ayahNumber = ayah.ayahNumber,
                        surahName = surah.englishName,
                        ayahSnippet = ayah.englishTranslation.let { if (it.length > 60) it.take(60) + "..." else it }
                    )
                )
            }
        }
    }

    fun updateHifzConfidence(surahNumber: Int, ayahNumber: Int, confidence: Int) {
        viewModelScope.launch {
            val key = "$surahNumber:$ayahNumber"
            val status = when (confidence) {
                1 -> HifzStatus.NEW.name
                2 -> HifzStatus.LEARNING.name
                3 -> HifzStatus.NEEDS_REVIEW.name
                else -> HifzStatus.STRONG.name
            }
            db.deenDao().insertHifz(
                HifzProgressEntity(
                    key = key,
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    status = status,
                    confidence = confidence
                )
            )
        }
    }

    fun recalculatePrayerTimes() {
        com.example.prayer.PrayerTimesWidget.refresh(getApplication())
        if (!_uiState.value.hasConfirmedLocation) {
            _uiState.update {
                it.copy(
                    rawPrayerTimes = null,
                    tomorrowPrayerTimes = null,
                    nextPrayerTimeFormatted = "",
                    minutesUntilNextPrayer = 0,
                    prayerCalculationError = "Choose a city or allow location to see prayer times."
                )
            }
            return
        }
        val city = _uiState.value.selectedCity
        val config = _uiState.value.prayerConfig
        val cal = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId))

        val tomorrow = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val calculated = runCatching {
            AstronomicalPrayerCalculator.calculateTimes(
                calendar = cal,
                latitude = city.latitude,
                longitude = city.longitude,
                config = config
            ) to AstronomicalPrayerCalculator.calculateTimes(
                calendar = tomorrow,
                latitude = city.latitude,
                longitude = city.longitude,
                config = config
            )
        }.getOrElse { error ->
            _uiState.update {
                it.copy(
                    rawPrayerTimes = null,
                    tomorrowPrayerTimes = null,
                    nextPrayerTimeFormatted = "",
                    minutesUntilNextPrayer = 0,
                    prayerCalculationError = error.message ?: "Prayer times could not be calculated."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                rawPrayerTimes = calculated.first,
                tomorrowPrayerTimes = calculated.second,
                prayerCalculationError = null
            )
        }
        refreshPrayerProgress(cal)
        refreshRamadanSchedule()

        if (_uiState.value.isNotificationEnabled) {
            schedulePrayerNotifications()
        }
    }

    /** Keeps the live countdown accurate without recalculating the solar ephemeris every minute. */
    private fun refreshPrayerProgress(
        cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone(_uiState.value.selectedCity.timeZoneId))
    ) {
        val raw = _uiState.value.rawPrayerTimes ?: return
        val tomorrowRaw = _uiState.value.tomorrowPrayerTimes ?: return
        val tomorrow = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val nowMillis = cal.timeInMillis
        fun instant(hours: Double): Long =
            AstronomicalPrayerCalculator.formatHoursToTimeInMillis(cal, hours)
        fun minutesUntil(targetMillis: Long): Long =
            ceil((targetMillis - nowMillis) / 60_000.0).toLong().coerceAtLeast(0)

        val fajrMillis = instant(raw.fajrHours)
        val sunriseMillis = instant(raw.sunriseHours)
        val dhuhrMillis = instant(raw.dhuhrHours)
        val asrMillis = instant(raw.asrHours)
        val maghribMillis = instant(raw.maghribHours)
        val ishaMillis = instant(raw.ishaHours)
        val tomorrowFajrMillis = AstronomicalPrayerCalculator.formatHoursToTimeInMillis(
            tomorrow, tomorrowRaw.fajrHours
        )
        val locale = appLocale(_uiState.value.languageCode)
        val (current, next, nextFormatted, minsRemaining) = when {
            nowMillis < fajrMillis -> {
                Quad(
                    PrayerType.ISHA, PrayerType.FAJR,
                    AstronomicalPrayerCalculator.formatHoursToAmPm(raw.fajrHours, locale),
                    minutesUntil(fajrMillis)
                )
            }
            nowMillis < sunriseMillis -> {
                Quad(
                    PrayerType.FAJR, PrayerType.DHUHR,
                    AstronomicalPrayerCalculator.formatHoursToAmPm(raw.dhuhrHours, locale),
                    minutesUntil(dhuhrMillis)
                )
            }
            nowMillis < dhuhrMillis -> {
                Quad(
                    PrayerType.SUNRISE, PrayerType.DHUHR,
                    AstronomicalPrayerCalculator.formatHoursToAmPm(raw.dhuhrHours, locale),
                    minutesUntil(dhuhrMillis)
                )
            }
            nowMillis < asrMillis -> {
                Quad(
                    PrayerType.DHUHR, PrayerType.ASR,
                    AstronomicalPrayerCalculator.formatHoursToAmPm(raw.asrHours, locale),
                    minutesUntil(asrMillis)
                )
            }
            nowMillis < maghribMillis -> {
                Quad(
                    PrayerType.ASR, PrayerType.MAGHRIB,
                    AstronomicalPrayerCalculator.formatHoursToAmPm(raw.maghribHours, locale),
                    minutesUntil(maghribMillis)
                )
            }
            nowMillis < ishaMillis -> {
                Quad(
                    PrayerType.MAGHRIB, PrayerType.ISHA,
                    AstronomicalPrayerCalculator.formatHoursToAmPm(raw.ishaHours, locale),
                    minutesUntil(ishaMillis)
                )
            }
            else -> {
                Quad(
                    PrayerType.ISHA, PrayerType.FAJR,
                    AstronomicalPrayerCalculator.formatHoursToAmPm(tomorrowRaw.fajrHours, locale),
                    minutesUntil(tomorrowFajrMillis)
                )
            }
        }

        val hijriDayOffset = maghribRollover(cal, raw)
        val hijri = HijriCalendarHelper.getHijriDate(cal)
        val hijriTonight = if (hijriDayOffset == 1) HijriCalendarHelper.getHijriDate(cal, 1) else null
        val hijriAdjustment = HijriCalendarHelper.adjustmentDays(cal, hijriDayOffset)

        _uiState.update {
            it.copy(
                rawPrayerTimes = raw,
                tomorrowPrayerTimes = tomorrowRaw,
                currentPrayer = current,
                nextPrayer = next,
                nextPrayerTimeFormatted = nextFormatted,
                minutesUntilNextPrayer = minsRemaining.coerceAtLeast(1),
                hijriDate = hijri,
                hijriDateTonight = hijriTonight,
                hijriAdjustmentDays = hijriAdjustment
            )
        }
    }

    /** Live timetable during Ramadan; otherwise a countdown to the next one. */
    private fun refreshRamadanSchedule() {
        val state = _uiState.value
        if (!state.hasConfirmedLocation) {
            _uiState.update { it.copy(ramadanSchedule = null, upcomingRamadan = null) }
            return
        }
        val schedule = runCatching {
            RamadanTimetable.build(state.hijriDateTonight ?: state.hijriDate, state.selectedCity, state.prayerConfig, locale = appLocale(state.languageCode))
        }.getOrNull()
        val upcoming = runCatching {
            RamadanTimetable.buildUpcoming(state.hijriDateTonight ?: state.hijriDate, state.selectedCity, state.prayerConfig, locale = appLocale(state.languageCode))
        }.getOrNull()
        _uiState.update { it.copy(ramadanSchedule = schedule, upcomingRamadan = upcoming) }
    }

    private fun recalculateQibla() {
        if (!_uiState.value.hasConfirmedLocation) return
        val city = _uiState.value.selectedCity
        val bearing = QiblaCalculator.calculateQiblaBearing(city.latitude, city.longitude)
        val dist = QiblaCalculator.calculateDistanceToMakkahKm(city.latitude, city.longitude)
        _uiState.update {
            // Live GPS keeps its own fix; otherwise the saved city is close enough for the
            // magnetic correction, so the compass works with device location switched off.
            it.copy(qiblaBearing = bearing, distanceToMakkahKm = dist,
                magneticDeclination = if (it.isLiveLocation && it.magneticDeclination != null) it.magneticDeclination
                    else cityDeclination(city))
        }
    }

    private fun cityDeclination(city: CityLocation): Float =
        GeomagneticField(city.latitude.toFloat(), city.longitude.toFloat(), 0f, System.currentTimeMillis()).declination

    override fun onCleared() {
        currentLocationRequest?.cancel()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        stopCompass()
        super.onCleared()
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
