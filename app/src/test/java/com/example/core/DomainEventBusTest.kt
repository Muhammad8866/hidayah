package com.example.core

import com.example.core.event.DomainEventBus
import com.example.core.event.LocationChangedEvent
import com.example.core.event.PrayerSettingsChangedEvent
import com.example.domain.model.AsrJuristicMethod
import com.example.domain.model.CalculationMethod
import com.example.domain.model.CityLocation
import com.example.domain.model.PrayerTimeConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

class DomainEventBusTest {

    @Test
    fun testPublishAndObservePrayerSettingsChangedEvent() = runBlocking {
        val newConfig = PrayerTimeConfig(
            method = CalculationMethod.MWL,
            asrMethod = AsrJuristicMethod.STANDARD
        )

        val flow = DomainEventBus.observe<PrayerSettingsChangedEvent>()
        val pending = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(5_000) { flow.first() }
        }
        // Publish event
        DomainEventBus.publish(PrayerSettingsChangedEvent(newConfig))

        // Collect first event
        val received = pending.await()
        assertEquals(CalculationMethod.MWL, received.newConfig.method)
        assertEquals(AsrJuristicMethod.STANDARD, received.newConfig.asrMethod)
        assertTrue("Event timestamp must be valid", received.occurredAtEpochMs > 0)
    }

    @Test
    fun testLocationChangedEventEmission() = runBlocking {
        val newLocation = CityLocation(
            name = "Istanbul",
            stateOrCountry = "Turkey",
            latitude = 41.0082,
            longitude = 28.9784,
            timeZoneId = "Europe/Istanbul"
        )

        val flow = DomainEventBus.observe<LocationChangedEvent>()
        val pending = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(5_000) { flow.first() }
        }
        DomainEventBus.publish(LocationChangedEvent(newLocation))

        val received = pending.await()
        assertEquals("Istanbul", received.newLocation.name)
        assertEquals(41.0082, received.newLocation.latitude, 0.0001)
    }
}
