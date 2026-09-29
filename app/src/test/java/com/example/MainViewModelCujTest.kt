package com.example

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.AsrJuristicMethod
import com.example.domain.model.CalculationMethod
import com.example.domain.model.CityLocation
import com.example.domain.model.PrayerTimeConfig
import com.example.domain.model.PrayerType
import com.example.domain.model.PredefinedLocations
import com.example.prayer.PrayerSettingsStore
import com.example.viewmodel.QuranReaderMode
import com.example.prayer.AstronomicalPrayerCalculator
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainViewModelCujTest {

    private lateinit var app: Application
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        val prefs = PrayerSettingsStore.prefs(app)
        prefs.edit().clear().commit()
        PrayerSettingsStore.saveCity(prefs, PredefinedLocations.DEFAULT_CITY)
        viewModel = MainViewModel(app)
    }

    @Test
    fun freshInstallDoesNotCalculateDelhiPrayerTimesBeforeLocationIsChosen() {
        PrayerSettingsStore.prefs(app).edit().clear().commit()
        val freshViewModel = MainViewModel(app)

        assertFalse(freshViewModel.uiState.value.hasConfirmedLocation)
        assertNull(freshViewModel.uiState.value.rawPrayerTimes)
        assertNotNull(freshViewModel.uiState.value.prayerCalculationError)
    }

    @Test
    fun cuj_prayerCalculationAndCityChangeUpdatesState() {
        val initialState = viewModel.uiState.value
        assertNotNull(initialState.selectedCity)
        assertNotNull(initialState.rawPrayerTimes)
        assertTrue(initialState.minutesUntilNextPrayer > 0)

        // User switches city to London
        val london = CityLocation(
            name = "London",
            stateOrCountry = "United Kingdom",
            latitude = 51.5074,
            longitude = -0.1278,
            timeZoneId = "Europe/London"
        )
        viewModel.selectCity(london)

        val updatedState = viewModel.uiState.value
        assertEquals("London", updatedState.selectedCity.name)
        assertNotNull(updatedState.rawPrayerTimes)
        assertFalse(viewModel.usesLiveLocation())
        viewModel.startLiveLocation()
        assertEquals("London", viewModel.uiState.value.selectedCity.name)
        assertFalse(viewModel.usesLiveLocation())
    }

    @Test
    fun cuj_juristicMethodChangeHanafiVsStandard() {
        val configStandard = PrayerTimeConfig(
            method = CalculationMethod.MWL,
            asrMethod = AsrJuristicMethod.STANDARD
        )
        viewModel.updatePrayerConfig(configStandard)
        val standardAsr = viewModel.uiState.value.rawPrayerTimes?.asrHours ?: 0.0

        val configHanafi = PrayerTimeConfig(
            method = CalculationMethod.MWL,
            asrMethod = AsrJuristicMethod.HANAFI
        )
        viewModel.updatePrayerConfig(configHanafi)
        val hanafiAsr = viewModel.uiState.value.rawPrayerTimes?.asrHours ?: 0.0

        assertTrue("Hanafi Asr must be later than Standard Asr", hanafiAsr > standardAsr)
    }

    @Test
    fun duaSearchUsesSelectedLanguageAndRefreshesWhenLanguageChanges() {
        viewModel.setLanguage("gu")
        viewModel.searchDuas("સવારનું સ્મરણ")
        assertTrue(viewModel.uiState.value.filteredDuas.any { it.id == "dua_morning_asbahna" })

        viewModel.setLanguage("en")
        assertTrue(viewModel.uiState.value.filteredDuas.none { it.id == "dua_morning_asbahna" })
    }

    @Test
    fun cuj_tasbeehIncrementAndReset() {
        viewModel.setTasbeehPreset("SubhanAllah", 33)
        assertEquals(0, viewModel.uiState.value.tasbeehCount)

        viewModel.incrementTasbeeh()
        viewModel.incrementTasbeeh()
        assertEquals(2, viewModel.uiState.value.tasbeehCount)

        viewModel.resetTasbeeh()
        assertEquals(0, viewModel.uiState.value.tasbeehCount)
    }

    @Test
    fun cuj_tasbeehShowsFullGoalThenStartsANewRound() {
        viewModel.setTasbeehPreset("Allahu Akbar (اللَّهُ أَكْبَرُ)", 3)
        repeat(3) { viewModel.incrementTasbeeh() }
        // The goal itself stays visible instead of silently wrapping to 0.
        assertEquals(3, viewModel.uiState.value.tasbeehCount)
        assertEquals(1, viewModel.uiState.value.tasbeehRounds)

        viewModel.incrementTasbeeh()
        assertEquals(1, viewModel.uiState.value.tasbeehCount)
        assertEquals(1, viewModel.uiState.value.tasbeehRounds)

        viewModel.resetTasbeeh()
        assertEquals(0, viewModel.uiState.value.tasbeehRounds)
    }

    @Test
    fun cuj_adhkarProgressStopsAtTargetAndPersists() {
        val key = "morning:dua_bismillah_la_yadurru"
        viewModel.resetAdhkar(listOf(key))
        repeat(5) { viewModel.countAdhkar(key, 3) }
        assertEquals(3, viewModel.uiState.value.adhkarProgress[key])

        // A new ViewModel (app restart) restores today's progress.
        assertEquals(3, MainViewModel(app).uiState.value.adhkarProgress[key])

        viewModel.resetAdhkar(listOf(key))
        assertEquals(null, viewModel.uiState.value.adhkarProgress[key])
    }

    @Test
    fun cuj_duaSearchStaysInsideSelectedCategory() {
        viewModel.selectDuaCategory("Sleep & Awakening")
        viewModel.searchDuas("bismika")
        val results = viewModel.uiState.value.filteredDuas
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.category == "Sleep & Awakening" })

        // Changing category keeps the typed query applied.
        viewModel.selectDuaCategory("Weather & Nature")
        assertEquals("bismika", viewModel.uiState.value.duaSearchQuery)
        assertTrue(viewModel.uiState.value.filteredDuas.isEmpty())

        viewModel.searchDuas("")
        assertTrue(viewModel.uiState.value.filteredDuas.all { it.category == "Weather & Nature" })
    }

    @Test
    fun cuj_quranSearchAndSelection() {
        viewModel.searchQuran("light")
        val stateWithSearch = viewModel.uiState.value
        assertEquals("light", stateWithSearch.quranSearchQuery)

        viewModel.setQuranReaderMode(QuranReaderMode.MEMORIZE)
        assertEquals(QuranReaderMode.MEMORIZE, viewModel.uiState.value.quranReaderMode)
    }

    @Test
    fun cuj_sourceConstrainedAiQueryExecution() = runBlocking {
        viewModel.askAiAssistant("What is the significance of Ramadan?")
        val response = viewModel.uiState.value.aiResponse
        assertNotNull(response)
        assertTrue(response!!.summaryAnswer.isNotBlank())
        assertTrue(response.primarySources.isNotEmpty())
    }

    @Test
    fun cuj_notificationToggleAndScheduling() {
        Shadows.shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        viewModel.setNotificationsEnabled(true)
        assertFalse(viewModel.uiState.value.isNotificationEnabled)

        Shadows.shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        viewModel.setNotificationsEnabled(true)
        assertTrue(viewModel.uiState.value.isNotificationEnabled)
        assertNotNull(viewModel.uiState.value.notificationFeedbackMessage)

        viewModel.setNotificationsEnabled(false)
        assertFalse(viewModel.uiState.value.isNotificationEnabled)
    }
}
