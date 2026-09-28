package com.example.core

import com.example.core.logging.SensitiveDataRedactor
import org.junit.Assert.*
import org.junit.Test

class Phase13PrivacySecurityTest {

    @Test
    fun testEmailSanitizationInLogs() {
        val raw = "User muhammad.ali@domain.co.uk synced their prayer preferences"
        val clean = SensitiveDataRedactor.redact(raw)
        assertFalse("Raw email should not exist in sanitized logs", clean.contains("muhammad.ali@domain.co.uk"))
        assertTrue("Sanitized string should indicate redaction token", clean.contains("[REDACTED_"))
    }

    @Test
    fun testPhoneSanitizationInLogs() {
        val raw = "Mosque contact updated to +1 415-555-2671 by admin"
        val clean = SensitiveDataRedactor.redact(raw)
        assertFalse("Phone number should be redacted", clean.contains("415-555-2671"))
        assertTrue("Redaction token present", clean.contains("[REDACTED_"))
    }

    @Test
    fun testTokenAndBearerSanitization() {
        val raw = "Header Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 and secret: 998822aabb"
        val clean = SensitiveDataRedactor.redact(raw)
        assertFalse(clean.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
        assertFalse(clean.contains("998822aabb"))
    }

    @Test
    fun testCoarsenedCoordinatesLogging() {
        // High precision coordinates: 28.613921, 77.209012
        val coarsened = SensitiveDataRedactor.redactCoordinates(28.613921, 77.209012)
        assertEquals("28.6° N/S, 77.2° E/W", coarsened)
        assertFalse("High-precision digits should not be visible", coarsened.contains("28.613921"))
    }
}
