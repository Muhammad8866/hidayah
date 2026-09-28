package com.example.core.id

/**
 * Strongly typed value objects preventing primitive obsession.
 * Enforces domain invariants at instantiation boundaries.
 */

@JvmInline
value class SurahNumber(val value: Int) {
    init {
        require(value in 1..114) { "SurahNumber must be between 1 and 114 inclusive. Received: $value" }
    }
}

@JvmInline
value class AyahNumber(val value: Int) {
    init {
        require(value in 1..286) { "AyahNumber must be positive and <= 286. Received: $value" }
    }
}

data class AyahReference(
    val surah: SurahNumber,
    val ayah: AyahNumber
) {
    override fun toString(): String = "${surah.value}:${ayah.value}"

    companion object {
        fun of(surah: Int, ayah: Int): AyahReference =
            AyahReference(SurahNumber(surah), AyahNumber(ayah))

        fun parseOrNull(refString: String): AyahReference? {
            val parts = refString.split(":")
            if (parts.size != 2) return null
            val s = parts[0].trim().toIntOrNull() ?: return null
            val a = parts[1].trim().toIntOrNull() ?: return null
            return runCatching { of(s, a) }.getOrNull()
        }
    }
}

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
    }
}

@JvmInline
value class BookmarkId(val value: String)

@JvmInline
value class NoteId(val value: String)

@JvmInline
value class HifzSegmentId(val value: String)

@JvmInline
value class TasbeehCounterId(val value: String)

data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90 degrees. Received: $latitude" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180 degrees. Received: $longitude" }
    }
}
