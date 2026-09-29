package com.example.core.event

import com.example.domain.model.CityLocation
import com.example.domain.model.PrayerTimeConfig

/**
 * Marker interface for all domain-level and application-level events.
 */
interface DomainEvent {
    val occurredAtEpochMs: Long
}

data class PrayerSettingsChangedEvent(
    val newConfig: PrayerTimeConfig,
    override val occurredAtEpochMs: Long = System.currentTimeMillis()
) : DomainEvent

data class LocationChangedEvent(
    val newLocation: CityLocation,
    val liveLocationGeneration: Long? = null,
    override val occurredAtEpochMs: Long = System.currentTimeMillis()
) : DomainEvent

data class DataSaverModeToggledEvent(
    val isDataSaverActive: Boolean,
    override val occurredAtEpochMs: Long = System.currentTimeMillis()
) : DomainEvent
