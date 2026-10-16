package com.example.feature.communitymasjid.data.remote

import android.content.Context
import com.example.feature.communitymasjid.domain.CmGeohash
import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.model.CmCommunityRecord
import com.example.feature.communitymasjid.model.CmDownReason
import com.example.feature.communitymasjid.model.CmHistoryEntry
import com.example.feature.communitymasjid.model.CmMosque
import com.example.feature.communitymasjid.model.CmMyVotes
import com.example.feature.communitymasjid.model.CmPrayer
import com.example.feature.communitymasjid.model.CmPrayerTime
import com.example.feature.communitymasjid.model.CmPresence
import com.example.feature.communitymasjid.model.CmTimings
import com.example.feature.communitymasjid.model.CmUserLocation
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Outcome of a community write that reached Firestore. */
sealed interface CmWriteOutcome {
    data class TimingSet(val version: Long) : CmWriteOutcome
    /** [votedDown] = this vote hid the masjid or reverted the timing. */
    data class Voted(val up: Int, val down: Int, val votedDown: Boolean) : CmWriteOutcome
    /** Same timings as current, or the same vote as before. */
    data object Unchanged : CmWriteOutcome
    /** The timing changed meanwhile, or the masjid no longer exists; reload and retry. */
    data object Stale : CmWriteOutcome
    data object OwnContent : CmWriteOutcome
}

class CmCommunityUnavailableException(message: String) : Exception(message)

/**
 * Firestore + Anonymous Auth access for Community Masjid. Uses only the dedicated `cm_mosques`
 * and `cm_users` collections. Every Firebase call is lazy and guarded so a missing
 * google-services.json, disabled provider or network failure stays inside this feature.
 *
 * Masjids and timings are live as soon as they are written; votes moderate them afterwards.
 * Every write below mirrors a rule in firestore.rules exactly, so change both together.
 */
class CmFirebaseDataSource(private val context: Context) {

    private val app: FirebaseApp? by lazy {
        com.example.core.firebase.FirebaseSetup.app(context)
    }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance(requireApp()) }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance(requireApp()) }

    val isConfigured: Boolean get() = app != null

    private fun requireApp(): FirebaseApp =
        app ?: throw CmCommunityUnavailableException("Community timings are not configured on this build")

    private fun mosques() = firestore.collection(MOSQUES)

    /** Invisible sign-in: the user never sees a login screen. */
    suspend fun ensureUid(): String {
        requireApp()
        auth.currentUser?.let { return it.uid }
        val result = auth.signInAnonymously().awaitCm()
        return result.user?.uid ?: throw CmCommunityUnavailableException("Anonymous sign-in failed")
    }

    data class SearchReservation(
        val budget: com.example.feature.communitymasjid.domain.CmSearchBudget,
        val block: com.example.feature.communitymasjid.domain.CmSearchPolicy.Block? = null
    )

    suspend fun reserveNearbySearch(location: CmUserLocation, nowMs: Long): SearchReservation {
        val uid = ensureUid()
        val ref = firestore.collection("cm_search_quotas").document(uid)
        return firestore.runTransaction { tx ->
            val old = tx.get(ref)
            val budget = if (old.exists()) com.example.feature.communitymasjid.domain.CmSearchBudget(
                day = old.getLong("day") ?: -1,
                count = (old.getLong("count") ?: 0).toInt(),
                lastAtMs = old.getTimestamp("lastSearchAt")?.toDate()?.time ?: 0,
                latitude = old.getDouble("latitude") ?: 0.0,
                longitude = old.getDouble("longitude") ?: 0.0
            ) else null
            val block = com.example.feature.communitymasjid.domain.CmSearchPolicy.blocked(nowMs, location, budget)
            if (block != null && budget != null) SearchReservation(budget, block) else {
                val next = com.example.feature.communitymasjid.domain.CmSearchPolicy.next(nowMs, location, budget)
                tx.set(ref, mapOf("day" to next.day, "count" to next.count,
                    "lastSearchAt" to FieldValue.serverTimestamp(), "latitude" to next.latitude, "longitude" to next.longitude))
                SearchReservation(next)
            }
        }.awaitCm()
    }

    /** A moderation report; reports are write-only for clients and reviewed in the Firebase console. */
    suspend fun report(docId: String, target: String, reason: String) {
        val uid = ensureUid()
        val reportRef = firestore.collection(REPORTS).document()
        val userRef = firestore.collection(USERS).document(uid)
        firestore.runTransaction { tx ->
            val userSnap = tx.get(userRef)
            tx.set(
                reportRef,
                mapOf(
                    "mosqueId" to docId,
                    "target" to target,
                    "reason" to reason,
                    "uid" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            writeRateDoc(tx, userRef, userSnap)
        }.awaitCm()
    }

    /** True once the anonymous identity exists; there is nothing to delete before that. */
    val hasAccount: Boolean get() = app != null && auth.currentUser != null

    /**
     * Deletes the anonymous Firebase user. Votes, timings and masjids already written stay as
     * anonymous community data: their uid no longer maps to any account or device.
     */
    suspend fun deleteAnonymousAccount() {
        requireApp()
        auth.currentUser?.delete()?.awaitCm()
        auth.signOut()
    }

    /** One query per 30 IDs, run in parallel; missing documents come back as `exists = false`. */
    suspend fun fetchRecords(docIds: List<String>, nowMs: Long): List<CmCommunityRecord> = coroutineScope {
        ensureUid()
        val found = docIds.distinct().chunked(30)
            .map { chunk -> async { mosques().whereIn(FieldPath.documentId(), chunk).get().awaitCm().documents } }
            .awaitAll().flatten()
            .associate { it.id to it.toRecord(nowMs) }
        docIds.distinct().map { id -> found[id] ?: emptyRecord(id, nowMs) }
    }

    suspend fun fetchRecord(docId: String, nowMs: Long): CmCommunityRecord {
        ensureUid()
        val snap = mosques().document(docId).get().awaitCm()
        return if (snap.exists()) snap.toRecord(nowMs) else emptyRecord(docId, nowMs)
    }

    /** This user's own votes; the timing vote only counts if it was cast on [currentVersion]. */
    suspend fun fetchMyVotes(docId: String, currentVersion: Long): CmMyVotes {
        val uid = ensureUid()
        val mosqueRef = mosques().document(docId)
        val (mosqueVote, timingVote) = coroutineScope {
            val mosque = async { mosqueRef.collection(MOSQUE_VOTES).document(uid).get().awaitCm() }
            val timing = async { mosqueRef.collection(TIMING_VOTES).document(uid).get().awaitCm() }
            mosque.await() to timing.await()
        }
        return CmMyVotes(
            mosqueVote = (mosqueVote.getLong("vote") ?: 0L).toInt(),
            timingVote = if (timingVote.getLong("version") == currentVersion) (timingVote.getLong("vote") ?: 0L).toInt() else 0
        )
    }

    suspend fun currentUid(): String = ensureUid()

    suspend fun fetchHistory(docId: String): List<CmHistoryEntry> {
        ensureUid()
        val snap = mosques().document(docId).collection(HISTORY)
            .orderBy("version", Query.Direction.DESCENDING)
            .limit(15)
            .get().awaitCm()
        return snap.documents.map { doc ->
            CmHistoryEntry(
                id = doc.id,
                timings = parseTimings(doc.get("timings")),
                version = doc.getLong("version") ?: 0,
                atMs = (doc.getTimestamp("at") ?: doc.getTimestamp("acceptedAt"))?.toDate()?.time,
                isRevert = doc.getString("kind") == KIND_REVERT,
                byRef = userRef(doc.getString("by") ?: doc.getString("suggestedBy"))
            )
        }
    }

    /**
     * Community-added masjids within [radiusMetres], found through their precision-5 geohash.
     * Masjids hidden by down votes are dropped.
     */
    suspend fun fetchUserMosques(latitude: Double, longitude: Double, radiusMetres: Double): List<CmMosque> {
        ensureUid()
        return coroutineScope {
            CmGeohash.coveringCells(latitude, longitude, radiusMetres).chunked(30)
                .map { chunk -> async { mosques().whereIn(GEO5, chunk).get().awaitCm().documents } }
                .awaitAll().flatten()
        }.mapNotNull { it.toUserMosque(latitude, longitude) }
            .distinctBy { it.docId }
            .filter { (it.distanceMetres ?: Double.MAX_VALUE) <= radiusMetres }
    }

    /** Adds a masjid at the caller's current position. It is visible to everyone at once. */
    suspend fun addMosque(name: String, address: String, location: CmUserLocation, accuracyMetres: Int): CmMosque {
        val uid = ensureUid()
        val docId = USER_MOSQUE_PREFIX + mosques().document().id
        val mosqueRef = mosques().document(docId)
        val userRef = firestore.collection(USERS).document(uid)
        firestore.runTransaction { tx ->
            val userSnap = tx.get(userRef)
            tx.set(
                mosqueRef,
                mapOf(
                    "source" to SOURCE_USER,
                    "name" to name,
                    "address" to address,
                    "lat" to location.latitude,
                    "lon" to location.longitude,
                    GEO5 to CmGeohash.encode(location.latitude, location.longitude),
                    "addedBy" to uid,
                    "accuracyM" to accuracyMetres.toLong(),
                    "createdAt" to FieldValue.serverTimestamp()
                ) + emptyMutableState()
            )
            writeRateDoc(tx, userRef, userSnap)
        }.awaitCm()
        return CmMosque(
            docId = docId,
            name = name,
            address = address,
            latitude = location.latitude,
            longitude = location.longitude,
            distanceMetres = 0.0,
            isUserAdded = true,
            addedByUid = uid
        )
    }

    /** Makes [timings] current immediately and records the change in history. */
    suspend fun setTiming(mosque: CmMosque, timings: CmTimings, presence: CmPresence): CmWriteOutcome {
        val uid = ensureUid()
        val mosqueRef = mosques().document(mosque.docId)
        val userRef = firestore.collection(USERS).document(uid)
        return firestore.runTransaction { tx ->
            val mosqueSnap = tx.get(mosqueRef)
            val userSnap = tx.get(userRef)
            if (!mosqueSnap.exists() && mosque.isUserAdded) return@runTransaction CmWriteOutcome.Stale
            val currentRaw = mosqueSnap.get("current")
            if (CmTimingRules.isSameAs(timings, parseTimings(currentRaw))) return@runTransaction CmWriteOutcome.Unchanged
            val version = mosqueSnap.getLong("currentVersion") ?: 0L
            val historyRef = mosqueRef.collection(HISTORY).document()
            val newState = mapOf(
                "current" to timings.toMap(),
                "currentVersion" to version + 1,
                "updatedAt" to FieldValue.serverTimestamp(),
                "updatedBy" to uid,
                "historyId" to historyRef.id,
                "timingUp" to 0L,
                "timingDown" to 0L
            )
            if (mosqueSnap.exists()) {
                tx.update(mosqueRef, newState)
            } else {
                tx.set(mosqueRef, googleIdentity(mosque) + emptyMutableState() + newState)
            }
            tx.set(
                historyRef,
                mapOf(
                    "timings" to timings.toMap(),
                    "previous" to currentRaw,
                    "parentId" to mosqueSnap.historyId(),
                    "version" to version + 1,
                    "kind" to KIND_SET,
                    "by" to uid,
                    "at" to FieldValue.serverTimestamp(),
                    "distanceM" to presence.distanceMetres.toLong(),
                    "accuracyM" to presence.accuracyMetres.toLong()
                )
            )
            writeRateDoc(tx, userRef, userSnap)
            CmWriteOutcome.TimingSet(version + 1)
        }.awaitCm()
    }

    /**
     * Votes on the current timing (one vote per user per version, changeable). When down votes
     * reach the threshold the timing is reverted to the previous one in the same commit.
     */
    suspend fun voteTiming(docId: String, expectedVersion: Long, vote: Int, presence: CmPresence): CmWriteOutcome {
        require(vote == 1 || vote == -1)
        val uid = ensureUid()
        val mosqueRef = mosques().document(docId)
        val voteRef = mosqueRef.collection(TIMING_VOTES).document(uid)
        val userRef = firestore.collection(USERS).document(uid)
        return firestore.runTransaction { tx ->
            // Firestore transactions need every read before the first write.
            val mosqueSnap = tx.get(mosqueRef)
            val voteSnap = tx.get(voteRef)
            val userSnap = tx.get(userRef)
            val version = mosqueSnap.getLong("currentVersion") ?: 0L
            if (!mosqueSnap.exists() || mosqueSnap.get("current") == null || version != expectedVersion) {
                return@runTransaction CmWriteOutcome.Stale
            }
            if (mosqueSnap.getString("updatedBy") == uid) return@runTransaction CmWriteOutcome.OwnContent
            val entryId = mosqueSnap.historyId()
            val entrySnap = entryId?.let { tx.get(mosqueRef.collection(HISTORY).document(it)) }
            val parentId = entrySnap?.getString("parentId")
            val parentSnap = parentId?.let { tx.get(mosqueRef.collection(HISTORY).document(it)) }

            val before = if (voteSnap.exists() && voteSnap.getLong("version") == version) (voteSnap.getLong("vote") ?: 0L).toInt() else 0
            if (before == vote) return@runTransaction CmWriteOutcome.Unchanged
            val up = (mosqueSnap.getLong("timingUp") ?: 0L).toInt() + (if (vote == 1) 1 else 0) - (if (before == 1) 1 else 0)
            val down = (mosqueSnap.getLong("timingDown") ?: 0L).toInt() + (if (vote == -1) 1 else 0) - (if (before == -1) 1 else 0)
            val revert = CmTimingRules.isVotedDown(up, down)
            if (revert && entrySnap?.exists() != true) return@runTransaction CmWriteOutcome.Stale

            tx.set(
                voteRef,
                mapOf(
                    "uid" to uid,
                    "version" to version,
                    "vote" to vote.toLong(),
                    "at" to FieldValue.serverTimestamp(),
                    "distanceM" to presence.distanceMetres.toLong(),
                    "accuracyM" to presence.accuracyMetres.toLong()
                )
            )
            if (revert) {
                val restored = entrySnap!!.get("previous")
                val revertRef = mosqueRef.collection(HISTORY).document()
                tx.update(
                    mosqueRef,
                    mapOf(
                        "current" to restored,
                        "currentVersion" to version + 1,
                        "updatedAt" to FieldValue.serverTimestamp(),
                        "updatedBy" to uid,
                        "historyId" to revertRef.id,
                        "timingUp" to 0L,
                        "timingDown" to 0L
                    )
                )
                tx.set(
                    revertRef,
                    mapOf(
                        "timings" to restored,
                        "previous" to parentSnap?.get("previous"),
                        "parentId" to parentSnap?.getString("parentId"),
                        "version" to version + 1,
                        "kind" to KIND_REVERT,
                        "by" to uid,
                        "at" to FieldValue.serverTimestamp(),
                        "distanceM" to presence.distanceMetres.toLong(),
                        "accuracyM" to presence.accuracyMetres.toLong()
                    )
                )
            } else {
                tx.update(mosqueRef, mapOf("timingUp" to up.toLong(), "timingDown" to down.toLong()))
            }
            writeRateDoc(tx, userRef, userSnap)
            CmWriteOutcome.Voted(up, down, votedDown = revert)
        }.awaitCm()
    }

    /** Votes on the masjid itself (one vote per user, changeable). Enough down votes hide it. */
    suspend fun voteMosque(mosque: CmMosque, vote: Int, reason: CmDownReason?, presence: CmPresence): CmWriteOutcome {
        require(vote == 1 || vote == -1)
        require(vote == 1 || reason != null)
        val uid = ensureUid()
        val mosqueRef = mosques().document(mosque.docId)
        val voteRef = mosqueRef.collection(MOSQUE_VOTES).document(uid)
        val userRef = firestore.collection(USERS).document(uid)
        return firestore.runTransaction { tx ->
            val mosqueSnap = tx.get(mosqueRef)
            val voteSnap = tx.get(voteRef)
            val userSnap = tx.get(userRef)
            if (!mosqueSnap.exists() && mosque.isUserAdded) return@runTransaction CmWriteOutcome.Stale
            if (mosqueSnap.getString("addedBy") == uid) return@runTransaction CmWriteOutcome.OwnContent
            val before = (voteSnap.getLong("vote") ?: 0L).toInt()
            if (before == vote) return@runTransaction CmWriteOutcome.Unchanged
            val up = (mosqueSnap.getLong("upVotes") ?: 0L).toInt() + (if (vote == 1) 1 else 0) - (if (before == 1) 1 else 0)
            val down = (mosqueSnap.getLong("downVotes") ?: 0L).toInt() + (if (vote == -1) 1 else 0) - (if (before == -1) 1 else 0)

            tx.set(
                voteRef,
                mapOf(
                    "uid" to uid,
                    "vote" to vote.toLong(),
                    "reason" to if (vote == -1) reason!!.key else "",
                    "at" to FieldValue.serverTimestamp(),
                    "distanceM" to presence.distanceMetres.toLong(),
                    "accuracyM" to presence.accuracyMetres.toLong()
                )
            )
            val counters = mapOf("upVotes" to up.toLong(), "downVotes" to down.toLong())
            if (mosqueSnap.exists()) {
                tx.update(mosqueRef, counters)
            } else {
                tx.set(mosqueRef, googleIdentity(mosque) + emptyMutableState() + counters)
            }
            writeRateDoc(tx, userRef, userSnap)
            CmWriteOutcome.Voted(up, down, votedDown = CmTimingRules.isVotedDown(up, down))
        }.awaitCm()
    }

    private fun googleIdentity(mosque: CmMosque): Map<String, Any?> {
        require(mosque.docId.startsWith("google_")) { "Only Google places can create a discovered masjid record" }
        return mapOf("placeId" to mosque.docId.removePrefix("google_"), "createdAt" to FieldValue.serverTimestamp())
    }

    /** Mirrors `defaults()` in firestore.rules. */
    private fun emptyMutableState(): Map<String, Any?> = mapOf(
        "current" to null,
        "currentVersion" to 0L,
        "updatedAt" to null,
        "updatedBy" to null,
        "historyId" to null,
        "timingUp" to 0L,
        "timingDown" to 0L,
        "upVotes" to 0L,
        "downVotes" to 0L
    )

    /** Documents from the earlier confirmation system keyed history by `acceptedSuggestionId`. */
    private fun DocumentSnapshot.historyId(): String? = getString("historyId") ?: getString("acceptedSuggestionId")

    /** Server-side throttle record; the rules reject writes that don't update it correctly. */
    private fun writeRateDoc(tx: Transaction, userRef: DocumentReference, userSnap: DocumentSnapshot) {
        val windowStart = userSnap.getTimestamp("windowStart")
        val windowCount = userSnap.getLong("windowCount") ?: 0L
        val withinWindow = userSnap.exists() && windowStart != null &&
            System.currentTimeMillis() - windowStart.toDate().time < DAY_MS
        tx.set(
            userRef,
            mapOf(
                "lastActionAt" to FieldValue.serverTimestamp(),
                "windowStart" to if (withinWindow) windowStart else FieldValue.serverTimestamp(),
                "windowCount" to if (withinWindow) windowCount + 1 else 1L
            )
        )
    }

    private fun DocumentSnapshot.toRecord(nowMs: Long) = CmCommunityRecord(
        docId = id,
        exists = true,
        current = parseTimings(get("current")),
        currentVersion = getLong("currentVersion") ?: 0,
        updatedAtMs = getTimestamp("updatedAt")?.toDate()?.time,
        updatedByUid = getString("updatedBy"),
        timingUp = (getLong("timingUp") ?: 0).toInt(),
        timingDown = (getLong("timingDown") ?: 0).toInt(),
        upVotes = (getLong("upVotes") ?: 0).toInt(),
        downVotes = (getLong("downVotes") ?: 0).toInt(),
        fetchedAtMs = nowMs
    )

    private fun DocumentSnapshot.toUserMosque(fromLat: Double, fromLon: Double): CmMosque? {
        if (!exists() || getString("source") != SOURCE_USER) return null
        val lat = getDouble("lat") ?: return null
        val lon = getDouble("lon") ?: return null
        val up = (getLong("upVotes") ?: 0).toInt()
        val down = (getLong("downVotes") ?: 0).toInt()
        if (CmTimingRules.isVotedDown(up, down)) return null
        return CmMosque(
            docId = id,
            name = getString("name").orEmpty().ifBlank { "Masjid" },
            address = getString("address").orEmpty(),
            latitude = lat,
            longitude = lon,
            distanceMetres = CmTimingRules.distanceMetres(fromLat, fromLon, lat, lon),
            isUserAdded = true,
            addedByUid = getString("addedBy")
        )
    }

    companion object {
        const val MOSQUES = "cm_mosques"
        const val MOSQUE_VOTES = "cm_mosque_votes"
        const val TIMING_VOTES = "cm_timing_votes"
        const val HISTORY = "cm_history"
        const val USERS = "cm_users"
        const val REPORTS = "cm_reports"
        const val GEO5 = "geo5"
        const val SOURCE_USER = "user"
        const val USER_MOSQUE_PREFIX = "user_"
        const val KIND_SET = "set"
        const val KIND_REVERT = "revert"
        private const val DAY_MS = 24L * 60 * 60 * 1000

        fun emptyRecord(docId: String, nowMs: Long) =
            CmCommunityRecord(docId, false, null, 0, null, null, 0, 0, 0, 0, nowMs)

        fun CmTimings.toMap(): Map<String, Map<String, String>> =
            times.entries.associate { (prayer, time) -> prayer.key to mapOf("azan" to time.azan, "jamaat" to time.jamaat) }

        fun parseTimings(raw: Any?): CmTimings? {
            val map = raw as? Map<*, *> ?: return null
            val times = map.entries.mapNotNull { (key, value) ->
                val prayer = CmPrayer.fromKey(key as? String ?: return@mapNotNull null) ?: return@mapNotNull null
                val entry = value as? Map<*, *> ?: return@mapNotNull null
                val azan = entry["azan"] as? String ?: return@mapNotNull null
                val jamaat = entry["jamaat"] as? String ?: return@mapNotNull null
                prayer to CmPrayerTime(azan, jamaat)
            }.toMap()
            return if (times.isEmpty()) null else CmTimings(times)
        }

        /** Short pseudonymous reference shown in the UI instead of the full anonymous UID. */
        fun userRef(uid: String?): String = uid?.take(6)?.let { "User #$it" } ?: "Anonymous"
    }
}

internal suspend fun <T> Task<T>.awaitCm(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            cont.resume(task.result as T)
        } else {
            cont.resumeWithException(task.exception ?: IllegalStateException("Firebase task failed"))
        }
    }
}
