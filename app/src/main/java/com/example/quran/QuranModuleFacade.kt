package com.example.quran

import com.example.core.id.AyahReference
import com.example.core.result.Result
import com.example.quran.data.CanonicalQuranDatasetAdapter
import com.example.quran.data.InMemoryHifzProgressStore
import com.example.quran.domain.hifz.HifzCardState
import com.example.quran.domain.hifz.HifzConfidenceLevel
import com.example.quran.domain.model.CanonicalAyah
import com.example.quran.domain.model.CanonicalSurahMeta
import com.example.quran.domain.port.HifzProgressStorePort
import com.example.quran.domain.port.QuranVerseReaderPort
import com.example.quran.domain.usecase.GetCanonicalAyahUseCase
import com.example.quran.domain.usecase.RecordHifzReviewUseCase
import com.example.quran.domain.verifier.QuranIntegrityFailure

/**
 * Public facade exposing Quran and Hifz capabilities to other modules.
 * In accordance with the Architecture Master Prompt:
 * Other modules (such as Hifz UI, Search, AI) import only through this contract.
 */
class QuranModuleFacade(
    val verseReader: QuranVerseReaderPort = CanonicalQuranDatasetAdapter(),
    val hifzStore: HifzProgressStorePort = InMemoryHifzProgressStore()
) {
    private val getAyahUseCase = GetCanonicalAyahUseCase(verseReader)
    private val recordReviewUseCase = RecordHifzReviewUseCase(hifzStore)

    fun getAyah(reference: AyahReference): Result<CanonicalAyah, QuranIntegrityFailure> {
        return getAyahUseCase.execute(reference)
    }

    fun getSurahManifest(): List<CanonicalSurahMeta> {
        return verseReader.getSurahManifest()
    }

    fun verifyIntegrity(): Result<Unit, QuranIntegrityFailure> {
        return verseReader.verifyIntegrity()
    }

    fun recordHifzReview(
        reference: AyahReference,
        confidence: HifzConfidenceLevel,
        currentTimestampMs: Long
    ): HifzCardState {
        return recordReviewUseCase.execute(reference, confidence, currentTimestampMs)
    }

    fun getCardState(reference: AyahReference): HifzCardState {
        return hifzStore.getCardState(reference)
    }

    fun getDueHifzCards(currentTimestampMs: Long): List<HifzCardState> {
        return hifzStore.getAllDueCards(currentTimestampMs)
    }
}
