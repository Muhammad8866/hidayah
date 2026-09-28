package com.example.ai.policy

import com.example.ai.AiAssistantResponse
import com.example.ai.AiCitation

/**
 * Fail-Closed Citation Verification Engine.
 * Enforces that no AI response containing religious claims or citations reaches the user
 * unless every citation passes validation against canonical sources.
 */
object AiCitationValidationPolicy {

    sealed class VerificationResult {
        data class Verified(val response: AiAssistantResponse) : VerificationResult()
        data class FallbackToSearch(val safeFallbackMessage: String, val searchKeyword: String) : VerificationResult()
        data class RejectedHighStakes(val referralMessage: String) : VerificationResult()
    }

    fun validate(response: AiAssistantResponse): VerificationResult {
        // Rule 1: High Stakes Triage (Divorce, Complex Inheritance, Fatwas) -> Fail-Closed to Personal Scholar
        if (response.requiresPersonalScholar) {
            return VerificationResult.RejectedHighStakes(
                "This topic involves formal personal or legal Islamic rulings (Fatwa/Talaq/Inheritance) and cannot be answered by automated systems. Please consult a qualified local Islamic scholar."
            )
        }

        // Rule 2: Minimum Citation Requirement for Theological Claims
        if (response.primarySources.isEmpty()) {
            return VerificationResult.FallbackToSearch(
                safeFallbackMessage = "This answer could not be verified against authentic scholarly sources. Showing verified verses and Duas instead.",
                searchKeyword = response.query
            )
        }

        // Rule 3: Validate each citation has recognized authentic provenance
        val hasInvalidCitation = response.primarySources.any { citation ->
            val auth = citation.authenticityOrType.lowercase()
            val text = citation.referenceText.lowercase()
            text.isBlank() || !(auth.contains("quran") || auth.contains("sahih") || auth.contains("divine") || auth.contains("hasan") || auth.contains("canonical"))
        }

        if (hasInvalidCitation) {
            return VerificationResult.FallbackToSearch(
                safeFallbackMessage = "Unverified citation detected. For religious safety, automated answer was suppressed.",
                searchKeyword = response.query
            )
        }

        return VerificationResult.Verified(response)
    }
}
