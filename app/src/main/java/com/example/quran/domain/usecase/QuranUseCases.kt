package com.example.quran.domain.usecase

import com.example.core.id.AyahReference
import com.example.core.result.Result
import com.example.quran.domain.hifz.HifzCardState
import com.example.quran.domain.hifz.HifzConfidenceLevel
import com.example.quran.domain.hifz.HifzSpacedRepetitionScheduler
import com.example.quran.domain.model.CanonicalAyah
import com.example.quran.domain.port.HifzProgressStorePort
import com.example.quran.domain.port.QuranVerseReaderPort
import com.example.quran.domain.verifier.QuranIntegrityFailure

class GetCanonicalAyahUseCase(
    private val verseReader: QuranVerseReaderPort
) {
    fun execute(reference: AyahReference): Result<CanonicalAyah, QuranIntegrityFailure> {
        return verseReader.getAyah(reference)
    }
}

class RecordHifzReviewUseCase(
    private val progressStore: HifzProgressStorePort
) {
    fun execute(
        reference: AyahReference,
        confidence: HifzConfidenceLevel,
        currentTimestampMs: Long
    ): HifzCardState {
        val currentCard = progressStore.getCardState(reference)
        val updatedCard = HifzSpacedRepetitionScheduler.calculateNextReview(
            current = currentCard,
            confidence = confidence,
            reviewTimestampMs = currentTimestampMs
        )
        progressStore.saveCardState(updatedCard)
        return updatedCard
    }
}
