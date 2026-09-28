package com.example.core

import com.example.core.logging.SensitiveDataRedactor
import org.junit.Assert.*
import org.junit.Test

class SensitiveDataRedactorTest {

    @Test
    fun testEmailRedaction() {
        val input = "User account logged in with email muhammad@example.org successfully"
        val redacted = SensitiveDataRedactor.redact(input)
        assertFalse("Email should not be present in output", redacted.contains("muhammad@example.org"))
        assertTrue("Redacted string should contain redaction token", redacted.contains("[REDACTED_"))
    }

    @Test
    fun testTokenAndApiKeyRedaction() {
        val input = "Authorization: bearer sk_live_9876543210abcdef and api_key: AIzaSyD987123"
        val redacted = SensitiveDataRedactor.redact(input)
        assertFalse("Bearer token should not be present", redacted.contains("sk_live_9876543210abcdef"))
        assertFalse("API key should not be present", redacted.contains("AIzaSyD987123"))
    }

    @Test
    fun testHighPrecisionCoordinateRedaction() {
        val input = "Current GPS fix: 28.6139210, 77.2090123 altitude 210m"
        val redacted = SensitiveDataRedactor.redact(input)
        assertFalse("High-precision coordinates should not be visible", redacted.contains("28.6139210, 77.2090123"))
    }

    @Test
    fun testSafeContentPassThrough() {
        val input = "Calculation finished for Fajr at 5:12 AM"
        val redacted = SensitiveDataRedactor.redact(input)
        assertEquals(input, redacted)
    }

    @Test
    fun testNullAndEmptyInput() {
        assertEquals("", SensitiveDataRedactor.redact(null))
        assertEquals("", SensitiveDataRedactor.redact(""))
    }
}
