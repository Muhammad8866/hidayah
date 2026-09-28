package com.example.quran.data

import com.example.core.id.AyahReference
import com.example.core.result.Result
import com.example.quran.QuranDatasetRepository
import com.example.quran.domain.model.CanonicalAyah
import com.example.quran.domain.model.CanonicalSurahMeta
import com.example.quran.domain.model.CanonicalWord
import com.example.quran.domain.model.RevelationType
import com.example.quran.domain.port.QuranVerseReaderPort
import com.example.quran.domain.verifier.QuranIntegrityFailure
import com.example.quran.domain.verifier.QuranIntegrityVerifier

/**
 * Adapter bridging Quran dataset persistence to the pure QuranVerseReaderPort.
 */
class CanonicalQuranDatasetAdapter : QuranVerseReaderPort {

    private val cachedCanonicalFatihah: List<CanonicalAyah> by lazy {
        QuranDatasetRepository.AL_FATIHAH_AYAT.map { raw ->
            val ref = AyahReference.of(raw.surahNumber, raw.ayahNumber)
            CanonicalAyah(
                reference = ref,
                arabicText = raw.arabicText,
                transliteration = raw.transliteration,
                englishTranslation = raw.englishTranslation,
                urduTranslation = raw.urduTranslation,
                words = raw.words.map { w ->
                    CanonicalWord(
                        position = w.id,
                        arabicText = w.arabic,
                        transliteration = w.transliteration,
                        englishMeaning = w.englishMeaning,
                        urduMeaning = w.urduMeaning,
                        rootLetters = w.rootLetters ?: "",
                        grammarNote = w.grammarRole ?: ""
                    )
                },
                tafsirSummary = raw.tafsirSummary,
                tafsirSource = raw.tafsirSource,
                juzNumber = raw.juzNumber
            )
        }
    }

    override fun getAyah(reference: AyahReference): Result<CanonicalAyah, QuranIntegrityFailure> {
        val ayah = cachedCanonicalFatihah.find { it.reference == reference }
        return if (ayah != null) {
            Result.Success(ayah)
        } else {
            Result.Failure(QuranIntegrityFailure.MissingAyah(reference))
        }
    }

    override fun getSurahAyahs(surahNumber: Int): Result<List<CanonicalAyah>, QuranIntegrityFailure> {
        if (surahNumber == 1) {
            return Result.Success(cachedCanonicalFatihah)
        }
        return Result.Failure(
            QuranIntegrityFailure.InvalidStructure("Surah $surahNumber currently not loaded into memory")
        )
    }

    override fun getSurahManifest(): List<CanonicalSurahMeta> {
        return QuranDatasetRepository.SURAHS.map { s ->
            CanonicalSurahMeta(
                number = s.number,
                arabicName = s.arabicName,
                englishTransliteration = s.englishName,
                englishMeaning = s.englishMeaning,
                totalVerses = s.totalAyat,
                revelationType = if (s.revelationType.equals("Medinan", ignoreCase = true)) {
                    RevelationType.MEDINAN
                } else {
                    RevelationType.MECCAN
                },
                startJuz = s.startJuz
            )
        }
    }

    override fun verifyIntegrity(): Result<Unit, QuranIntegrityFailure> {
        return QuranIntegrityVerifier.verifySurahFatihah(cachedCanonicalFatihah)
    }
}
