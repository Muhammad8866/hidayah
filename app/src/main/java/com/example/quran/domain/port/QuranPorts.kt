package com.example.quran.domain.port

import com.example.core.id.AyahReference
import com.example.core.result.Result
import com.example.quran.domain.model.CanonicalAyah
import com.example.quran.domain.model.CanonicalSurahMeta
import com.example.quran.domain.verifier.QuranIntegrityFailure

interface QuranVerseReaderPort {
    fun getAyah(reference: AyahReference): Result<CanonicalAyah, QuranIntegrityFailure>
    fun getSurahAyahs(surahNumber: Int): Result<List<CanonicalAyah>, QuranIntegrityFailure>
    fun getSurahManifest(): List<CanonicalSurahMeta>
    fun verifyIntegrity(): Result<Unit, QuranIntegrityFailure>
}

interface HifzProgressStorePort {
    fun getCardState(reference: AyahReference): com.example.quran.domain.hifz.HifzCardState
    fun saveCardState(cardState: com.example.quran.domain.hifz.HifzCardState)
    fun getAllDueCards(currentTimestampMs: Long): List<com.example.quran.domain.hifz.HifzCardState>
}
