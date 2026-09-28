package com.example.prayer

import com.example.ai.AiAssistantResponse
import com.example.ai.AiCitation
import com.example.ai.policy.AiCitationValidationPolicy
import com.example.domain.model.HighLatitudeRule
import com.example.prayer.domain.policy.HighLatitudePolicy
import org.junit.Assert.*
import org.junit.Test

class Phase11FailClosedPoliciesTest {

    @Test
    fun testHighLatitudeExtremeFallback() {
        // Tromsø, Norway (> 65° N): with no twilight end, Automatic falls back to the middle of the night.
        val (fajrOffset, ishaOffset) = HighLatitudePolicy.calculateFallbackTwilightOffsets(
            latitudeDeg = 69.6492,
            nightDurationHours = 7.0,
            fajrAngle = 18.0,
            ishaAngle = 18.0
        )

        assertEquals("Fajr may start no earlier than the middle of the night", 3.5, fajrOffset, 0.001)
        assertEquals("Isha may start no later than the middle of the night", 3.5, ishaOffset, 0.001)
    }

    @Test
    fun testHighLatitudeAngleBasedFallback() {
        // Explicit angle-based rule: each angle is converted to a fraction of 60.
        val (fajrPortion, ishaPortion) = HighLatitudePolicy.nightPortions(
            rule = HighLatitudeRule.TWILIGHT_ANGLE,
            latitudeDeg = 51.5074,
            fajrAngle = 18.0,
            ishaAngle = 17.0
        )

        assertEquals(18.0 / 60.0, fajrPortion, 0.001)
        assertEquals(17.0 / 60.0, ishaPortion, 0.001)
    }

    @Test
    fun automaticHighLatitudeRuleOnlyCapsAtMiddleOfNight() {
        val portions = HighLatitudePolicy.nightPortions(
            rule = HighLatitudeRule.AUTOMATIC,
            latitudeDeg = 51.5074,
            fajrAngle = 18.0,
            ishaAngle = 17.0
        )

        assertEquals(0.5, portions.first, 0.0001)
        assertEquals(0.5, portions.second, 0.0001)
    }

    @Test
    fun testAiCitationValidationPolicy_PassesAuthenticCitations() {
        val validResponse = AiAssistantResponse(
            query = "What is the virtue of Ramadan fasting?",
            summaryAnswer = "Fasting in Ramadan is one of the five pillars of Islam.",
            primarySources = listOf(
                AiCitation(
                    sourceTitle = "The Noble Quran",
                    referenceText = "Surah Al-Baqarah (2:183)",
                    collectionOrChapter = "Al-Baqarah",
                    authenticityOrType = "Canonical Quranic Ayah"
                ),
                AiCitation(
                    sourceTitle = "Sahih al-Bukhari",
                    referenceText = "Hadith 38",
                    collectionOrChapter = "Book of Faith",
                    authenticityOrType = "Sahih"
                )
            ),
            requiresPersonalScholar = false
        )

        val result = AiCitationValidationPolicy.validate(validResponse)
        assertTrue("Valid response must pass verification", result is AiCitationValidationPolicy.VerificationResult.Verified)
    }

    @Test
    fun testAiCitationValidationPolicy_RejectsHighStakesRulings() {
        val highStakesResponse = AiAssistantResponse(
            query = "How to divide inheritance between three siblings?",
            summaryAnswer = "Generic text",
            primarySources = emptyList(),
            requiresPersonalScholar = true
        )

        val result = AiCitationValidationPolicy.validate(highStakesResponse)
        assertTrue("High stakes questions must fail-closed to personal scholar", result is AiCitationValidationPolicy.VerificationResult.RejectedHighStakes)
    }

    @Test
    fun testAiCitationValidationPolicy_RejectsUnverifiedSources() {
        val unverifiedResponse = AiAssistantResponse(
            query = "What is the ruling on fasting without suhoor?",
            summaryAnswer = "Claimed ruling with doubtful attribution",
            primarySources = listOf(
                AiCitation(
                    sourceTitle = "Random Internet Blog",
                    referenceText = "Unknown author claim",
                    collectionOrChapter = "Blog Post",
                    authenticityOrType = "Unverified"
                )
            ),
            requiresPersonalScholar = false
        )

        val result = AiCitationValidationPolicy.validate(unverifiedResponse)
        assertTrue("Unverified sources must trigger safe fallback", result is AiCitationValidationPolicy.VerificationResult.FallbackToSearch)
    }
}
