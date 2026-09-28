package com.example.core.error

/**
 * Domain error taxonomy representing categorized failure states.
 * Eliminates generic raw string errors across internal layers.
 */
sealed class AppFailure(open val messageKey: String, open val technicalDetails: String? = null) {

    sealed class PrayerFailure(messageKey: String, technicalDetails: String? = null) : AppFailure(messageKey, technicalDetails) {
        data class InvalidCoordinates(val lat: Double, val lon: Double) :
            PrayerFailure("error_prayer_invalid_coordinates", "lat=$lat, lon=$lon out of bounds")
        data class CalculationError(val cause: String) :
            PrayerFailure("error_prayer_calculation_failed", cause)
        data class TimeZoneResolutionFailure(val zoneId: String) :
            PrayerFailure("error_prayer_timezone_failed", "Unable to resolve $zoneId")
    }

    sealed class QuranFailure(messageKey: String, technicalDetails: String? = null) : AppFailure(messageKey, technicalDetails) {
        data class ChecksumMismatch(val expected: String, val actual: String) :
            QuranFailure("error_quran_integrity_failed", "Checksum expected=$expected, got=$actual")
        data class VerseNotFound(val surah: Int, val ayah: Int) :
            QuranFailure("error_quran_verse_not_found", "Reference $surah:$ayah")
        data class PackageCorrupted(val reason: String) :
            QuranFailure("error_quran_package_corrupted", reason)
    }

    sealed class AiFailure(messageKey: String, technicalDetails: String? = null) : AppFailure(messageKey, technicalDetails) {
        data class InsufficientEvidence(val query: String) :
            AiFailure("error_ai_insufficient_evidence", "No verified Quran/Hadith backing for query")
        data class CitationValidationFailed(val failedCitation: String) :
            AiFailure("error_ai_citation_mismatch", "Citation $failedCitation did not match corpus")
        data class PolicyRefusal(val reason: String) :
            AiFailure("error_ai_sensitive_policy_refusal", reason)
        data class ServiceUnavailable(val reason: String) :
            AiFailure("error_ai_service_unavailable", reason)
    }

    sealed class StorageFailure(messageKey: String, technicalDetails: String? = null) : AppFailure(messageKey, technicalDetails) {
        data class DatabaseError(val error: Throwable) :
            StorageFailure("error_storage_db_failure", error.localizedMessage)
        data class DiskFull(val bytesRequired: Long) :
            StorageFailure("error_storage_disk_full", "Required $bytesRequired bytes")
    }
}
