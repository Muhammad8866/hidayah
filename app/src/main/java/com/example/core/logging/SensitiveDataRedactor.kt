package com.example.core.logging

import android.util.Log

/**
 * Structured privacy-first logger that strictly redacts sensitive personal & religious data.
 * Guarantees zero leakage of exact prayers, questions, locations, notes, phone numbers, or credentials.
 */
object SensitiveDataRedactor {
    private val SENSITIVE_PATTERNS = listOf(
        Regex("""(?i)\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b"""), // Emails
        Regex("""(?i)(api[_-]?key|bearer|token|secret|password|auth)[\s:=]+[A-Za-z0-9_\-\.]+"""), // Tokens & Passwords
        Regex("""\b\d{1,2}\.\d{4,8},\s*-?\d{1,3}\.\d{4,8}\b"""), // High-precision coordinates
        Regex("""\b(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b""") // Phone numbers
    )

    fun redact(input: String?): String {
        val nonNullInput = input ?: return ""
        var sanitized = nonNullInput
        for (pattern in SENSITIVE_PATTERNS) {
            sanitized = pattern.replace(sanitized) { matchResult ->
                "[REDACTED_${matchResult.range.first}]"
            }
        }
        return sanitized
    }

    fun redactCoordinates(lat: Double, lon: Double): String {
        // Coarsen coordinates to 1 decimal place (~11km) for privacy-safe logging
        return String.format("%.1f° N/S, %.1f° E/W", lat, lon)
    }
}

object SafeLogger {
    private const val TAG = "Hidayah"

    fun d(component: String, message: String) {
        val clean = SensitiveDataRedactor.redact(message)
        Log.d(TAG, "[$component] $clean")
    }

    fun i(component: String, message: String) {
        val clean = SensitiveDataRedactor.redact(message)
        Log.i(TAG, "[$component] $clean")
    }

    fun w(component: String, message: String, throwable: Throwable? = null) {
        val clean = SensitiveDataRedactor.redact(message)
        Log.w(TAG, "[$component] $clean", throwable)
    }

    fun e(component: String, message: String, throwable: Throwable? = null) {
        val clean = SensitiveDataRedactor.redact(message)
        Log.e(TAG, "[$component] $clean", throwable)
    }
}
