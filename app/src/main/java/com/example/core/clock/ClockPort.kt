package com.example.core.clock

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Clock port abstraction for deterministic time-dependent calculations
 * (Prayer times, Next prayer resolution, Hijri conversion, Hifz intervals).
 * Production uses [SystemClockAdapter]. Unit tests use [DeterministicFakeClock].
 */
interface ClockPort {
    fun currentInstant(): Instant
    fun currentLocalDate(zoneId: ZoneId = systemZoneId()): LocalDate
    fun currentLocalTime(zoneId: ZoneId = systemZoneId()): LocalTime
    fun systemZoneId(): ZoneId
}

class SystemClockAdapter : ClockPort {
    override fun currentInstant(): Instant = Instant.now()
    override fun currentLocalDate(zoneId: ZoneId): LocalDate = LocalDate.now(zoneId)
    override fun currentLocalTime(zoneId: ZoneId): LocalTime = LocalTime.now(zoneId)
    override fun systemZoneId(): ZoneId = ZoneId.systemDefault()
}

class DeterministicFakeClock(
    private var fixedInstant: Instant,
    private val fixedZoneId: ZoneId = ZoneId.of("Asia/Kolkata")
) : ClockPort {
    override fun currentInstant(): Instant = fixedInstant
    override fun currentLocalDate(zoneId: ZoneId): LocalDate = LocalDate.ofInstant(fixedInstant, zoneId)
    override fun currentLocalTime(zoneId: ZoneId): LocalTime = LocalTime.ofInstant(fixedInstant, zoneId)
    override fun systemZoneId(): ZoneId = fixedZoneId

    fun advanceMinutes(minutes: Long) {
        fixedInstant = fixedInstant.plusSeconds(minutes * 60)
    }

    fun setInstant(instant: Instant) {
        fixedInstant = instant
    }
}
