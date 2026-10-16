package com.example.feature.communitymasjid.model

/**
 * Community Masjid feature models. Deliberately separate from the Masjid tab's
 * [com.example.domain.model.Mosque] models so neither feature can break the other.
 */
/**
 * Prayers a masjid can publish. [hasAzan] = false (Eid, Taraweeh) means only the Jamaat time is
 * entered; it is stored as both azan and jamaat so the stored shape stays the same for every prayer.
 */
enum class CmPrayer(val key: String, val label: String, val isOptional: Boolean = false, val hasAzan: Boolean = true) {
    FAJR("fajr", "Fajr"),
    DHUHR("dhuhr", "Zuhr"),
    ASR("asr", "Asr"),
    MAGHRIB("maghrib", "Maghrib"),
    ISHA("isha", "Isha"),
    JUMUAH("jumuah", "Jumu'ah", isOptional = true),
    EID_FITR("eid_fitr", "Eid ul-Fitr", isOptional = true, hasAzan = false),
    EID_ADHA("eid_adha", "Eid ul-Adha", isOptional = true, hasAzan = false),
    TARAWEEH("taraweeh", "Taraweeh", isOptional = true, hasAzan = false);

    companion object {
        val daily = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
        val optional = entries.filter { it.isOptional }
        fun fromKey(key: String): CmPrayer? = entries.firstOrNull { it.key == key }
    }
}

/** Times are 24h "HH:mm" strings, the exact format stored in Firestore and checked by the rules. */
data class CmPrayerTime(val azan: String, val jamaat: String)

data class CmTimings(val times: Map<CmPrayer, CmPrayerTime>) {
    operator fun get(prayer: CmPrayer): CmPrayerTime? = times[prayer]
}

/**
 * A Google place (docId = "google_<placeId>") or a community masjid
 * (docId = "user_<id>"). Community contributions use the same stable document ID.
 */
data class CmMosque(
    val docId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMetres: Double? = null,
    val isUserAdded: Boolean = false,
    val addedByUid: String? = null
)

/** Community state of one masjid: its current timing (instantly set, vote-moderated) and its votes. */
data class CmCommunityRecord(
    val docId: String,
    val exists: Boolean,
    val current: CmTimings?,
    val currentVersion: Long,
    val updatedAtMs: Long?,
    val updatedByUid: String?,
    val timingUp: Int,
    val timingDown: Int,
    val upVotes: Int,
    val downVotes: Int,
    val fetchedAtMs: Long
)

/** This user's own votes on one masjid; 1 = up, -1 = down, 0 = none. [timingVote] is for the current version. */
data class CmMyVotes(val mosqueVote: Int = 0, val timingVote: Int = 0)

enum class CmDownReason(val key: String, val label: String) {
    NOT_HERE("not_here", "There is no masjid here"),
    DUPLICATE("duplicate", "Duplicate of another masjid"),
    WRONG_PLACE("wrong_place", "Wrong name or location")
}

data class CmHistoryEntry(
    val id: String,
    /** Null when a revert removed the only timing. */
    val timings: CmTimings?,
    val version: Long,
    val atMs: Long?,
    val isRevert: Boolean,
    val byRef: String
)

data class CmUserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMetres: Float,
    val fixedAtMs: Long
)

/** Proof-of-presence values sent along with a timing or a vote. */
data class CmPresence(val distanceMetres: Int, val accuracyMetres: Int)
