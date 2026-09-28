package com.example.quran.domain.verifier

import com.example.core.id.AyahReference
import com.example.core.result.Result
import com.example.quran.domain.model.CanonicalAyah
import java.security.MessageDigest

sealed class QuranIntegrityFailure {
    data class ChecksumMismatch(val expected: String, val actual: String) : QuranIntegrityFailure()
    data class MissingAyah(val reference: AyahReference) : QuranIntegrityFailure()
    data class InvalidStructure(val reason: String) : QuranIntegrityFailure()
}

/**
 * Validates Quran content integrity.
 * Rule: Fail-closed. Never silently proceed if canonical Quran text has been modified or corrupted.
 */
object QuranIntegrityVerifier {

    /**
     * Computes deterministic SHA-256 hash of a canonical text segment.
     */
    fun computeSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies that all Ayahs in Al-Fatihah are sequentially contiguous (1 to 7)
     * and non-empty in Arabic script.
     */
    fun verifySurahFatihah(ayat: List<CanonicalAyah>): Result<Unit, QuranIntegrityFailure> {
        if (ayat.size != 7) {
            return Result.Failure(
                QuranIntegrityFailure.InvalidStructure("Surah Al-Fatihah must contain exactly 7 Ayahs, found ${ayat.size}")
            )
        }

        for (i in 1..7) {
            val ayah = ayat.find { it.reference.surah.value == 1 && it.reference.ayah.value == i }
            if (ayah == null) {
                return Result.Failure(
                    QuranIntegrityFailure.MissingAyah(AyahReference.of(1, i))
                )
            }
            if (ayah.arabicText.isBlank()) {
                return Result.Failure(
                    QuranIntegrityFailure.InvalidStructure("Arabic text for Surah 1, Ayah $i cannot be blank")
                )
            }
        }

        return Result.Success(Unit)
    }
}
