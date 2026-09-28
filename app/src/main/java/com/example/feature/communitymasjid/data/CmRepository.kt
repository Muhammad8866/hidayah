package com.example.feature.communitymasjid.data

import android.content.Context
import com.example.feature.communitymasjid.data.local.CmCommunityRecordEntity
import com.example.feature.communitymasjid.data.local.CmDatabase
import com.example.feature.communitymasjid.data.local.CmLocalActionEntity
import com.example.feature.communitymasjid.data.local.CmOsmMosqueEntity
import com.example.feature.communitymasjid.data.local.CmOsmQueryEntity
import com.example.feature.communitymasjid.data.remote.CmFirebaseDataSource
import com.example.feature.communitymasjid.data.remote.CmFirebaseDataSource.Companion.parseTimings
import com.example.feature.communitymasjid.data.remote.CmFirebaseDataSource.Companion.toMap
import com.example.feature.communitymasjid.data.remote.CmWriteOutcome
import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.model.CmCommunityRecord
import com.example.feature.communitymasjid.model.CmDownReason
import com.example.feature.communitymasjid.model.CmHistoryEntry
import com.example.feature.communitymasjid.model.CmMosque
import com.example.feature.communitymasjid.model.CmMyVotes
import com.example.feature.communitymasjid.model.CmPresence
import com.example.feature.communitymasjid.model.CmTimings
import com.example.feature.communitymasjid.model.CmUserLocation
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

/**
 * Single source of truth for the Community Masjid feature. Room is read first; Firestore is
 * only queried when the cached copy is older than its TTL or the user explicitly refreshes.
 */
class CmRepository(context: Context) {
    private val dao = CmDatabase.get(context).dao()
    private val remote = CmFirebaseDataSource(context.applicationContext)

    val isCommunityConfigured: Boolean get() = remote.isConfigured

    data class NearbyResult(val mosques: List<CmMosque>, val fromCache: Boolean, val error: Throwable?)

    /** The last saved list, for showing something instantly; distances from [location] or the saved query point. */
    suspend fun savedMosques(location: CmUserLocation?): List<CmMosque> {
        val from = location ?: dao.osmQuery()?.let { CmUserLocation(it.latitude, it.longitude, 0f, it.fetchedAtMs) } ?: return emptyList()
        return cachedMosques(from)
    }

    /**
     * Only masjids added by the community are listed; OpenStreetMap entries are not shown.
     * If Firestore fails, the last saved community list is returned instead.
     */
    suspend fun nearbyMosques(location: CmUserLocation, force: Boolean): NearbyResult {
        val now = System.currentTimeMillis()
        val cached = cachedMosques(location)
        if (!remote.isConfigured) return NearbyResult(emptyList(), fromCache = false, error = null)
        val userResult = runCatching {
            withTimeout(USER_MOSQUES_TIMEOUT_MS) {
                remote.fetchUserMosques(location.latitude, location.longitude, SEARCH_RADIUS_M.toDouble())
            }
        }
        val mosques = userResult.getOrElse { return NearbyResult(cached, fromCache = true, error = it) }
            .sortedBy { it.distanceMetres }
            .take(MAX_MOSQUES)
        dao.clearOsmMosques()
        dao.insertOsmMosques(mosques.map { it.toEntity(now) })
        dao.upsertOsmQuery(CmOsmQueryEntity(latitude = location.latitude, longitude = location.longitude, fetchedAtMs = now))
        return NearbyResult(mosques, fromCache = false, error = null)
    }

    private suspend fun cachedMosques(location: CmUserLocation): List<CmMosque> =
        dao.osmMosques().filter { it.isUserAdded }.map {
            CmMosque(
                docId = it.docId,
                name = it.name,
                address = it.address,
                latitude = it.latitude,
                longitude = it.longitude,
                distanceMetres = CmTimingRules.distanceMetres(location.latitude, location.longitude, it.latitude, it.longitude),
                isUserAdded = it.isUserAdded,
                addedByUid = it.addedByUid
            )
        }.sortedBy { it.distanceMetres }

    suspend fun cachedRecords(docIds: List<String>): Map<String, CmCommunityRecord> =
        if (docIds.isEmpty()) emptyMap() else dao.records(docIds).associate { it.docId to it.toModel() }

    /** Fetches only the records that are missing or stale; returns the merged cache. */
    suspend fun refreshRecords(docIds: List<String>, force: Boolean): Map<String, CmCommunityRecord> {
        val now = System.currentTimeMillis()
        val cached = cachedRecords(docIds)
        val stale = docIds.filter { id -> force || cached[id]?.let { now - it.fetchedAtMs > LIST_RECORD_TTL_MS } ?: true }
        if (stale.isNotEmpty()) {
            val fresh = remote.fetchRecords(stale.take(MAX_REMOTE_LIST_LOOKUPS), now)
            dao.upsertRecords(fresh.map { it.toEntity() })
        }
        return cachedRecords(docIds)
    }

    suspend fun record(docId: String, force: Boolean): CmCommunityRecord {
        val now = System.currentTimeMillis()
        val cached = cachedRecords(listOf(docId))[docId]
        if (!force && cached != null && now - cached.fetchedAtMs < DETAIL_TTL_MS) return cached
        val fresh = remote.fetchRecord(docId, now)
        dao.upsertRecords(listOf(fresh.toEntity()))
        return fresh
    }

    suspend fun myVotes(docId: String, currentVersion: Long): CmMyVotes = remote.fetchMyVotes(docId, currentVersion)

    suspend fun currentUid(): String = remote.currentUid()

    suspend fun history(docId: String): List<CmHistoryEntry> = remote.fetchHistory(docId)

    /** Client-side spam guard; the Firestore rules enforce the same limits server-side. */
    suspend fun localRateLimitProblem(kind: String): String? {
        val now = System.currentTimeMillis()
        dao.pruneActions(now - 30 * DAY_MS)
        val today = dao.actionsSince(now - DAY_MS)
        val last = today.firstOrNull()
        return when {
            last != null && now - last.atMs < MIN_ACTION_GAP_MS ->
                "Please wait ${((MIN_ACTION_GAP_MS - (now - last.atMs)) / 1000) + 1}s before the next action"
            today.size >= MAX_ACTIONS_PER_DAY -> "Daily limit reached. Please try again tomorrow"
            kind == KIND_ADD_MOSQUE && today.count { it.kind == KIND_ADD_MOSQUE } >= MAX_MOSQUE_ADDS_PER_DAY ->
                "You can add at most $MAX_MOSQUE_ADDS_PER_DAY masjids per day"
            else -> null
        }
    }

    suspend fun addMosque(name: String, address: String, location: CmUserLocation, accuracyMetres: Int): CmMosque {
        val mosque = remote.addMosque(name, address, location, accuracyMetres)
        val now = System.currentTimeMillis()
        dao.insertOsmMosques(listOf(mosque.toEntity(now)))
        logAction(KIND_ADD_MOSQUE, mosque.docId)
        return mosque
    }

    suspend fun setTiming(mosque: CmMosque, timings: CmTimings, presence: CmPresence): CmWriteOutcome =
        remote.setTiming(mosque, timings, presence).also { if (it is CmWriteOutcome.TimingSet) logAction(KIND_SET_TIMING, mosque.docId) }

    suspend fun voteTiming(docId: String, expectedVersion: Long, vote: Int, presence: CmPresence): CmWriteOutcome =
        remote.voteTiming(docId, expectedVersion, vote, presence).also { if (it is CmWriteOutcome.Voted) logAction(KIND_VOTE, docId) }

    suspend fun voteMosque(mosque: CmMosque, vote: Int, reason: CmDownReason?, presence: CmPresence): CmWriteOutcome =
        remote.voteMosque(mosque, vote, reason, presence).also { outcome ->
            if (outcome is CmWriteOutcome.Voted) {
                logAction(KIND_VOTE, mosque.docId)
                // A masjid hidden by votes leaves the cached list at once.
                if (outcome.votedDown) dao.deleteOsmMosque(mosque.docId)
            }
        }

    private suspend fun logAction(kind: String, docId: String) {
        dao.insertAction(CmLocalActionEntity(kind = kind, mosqueDocId = docId, suggestionId = "", atMs = System.currentTimeMillis()))
    }

    private fun CmMosque.toEntity(now: Long) = CmOsmMosqueEntity(
        docId = docId,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        fetchedAtMs = now,
        isUserAdded = isUserAdded,
        addedByUid = addedByUid
    )

    private fun CmCommunityRecord.toEntity() = CmCommunityRecordEntity(
        docId = docId,
        exists = exists,
        currentJson = current?.let { JSONObject(it.toMap()).toString() },
        currentVersion = currentVersion,
        updatedAtMs = updatedAtMs,
        updatedByUid = updatedByUid,
        timingUp = timingUp,
        timingDown = timingDown,
        upVotes = upVotes,
        downVotes = downVotes,
        fetchedAtMs = fetchedAtMs
    )

    private fun CmCommunityRecordEntity.toModel() = CmCommunityRecord(
        docId = docId,
        exists = exists,
        current = currentJson?.let { parseTimings(jsonToMap(it)) },
        currentVersion = currentVersion,
        updatedAtMs = updatedAtMs,
        updatedByUid = updatedByUid,
        timingUp = timingUp,
        timingDown = timingDown,
        upVotes = upVotes,
        downVotes = downVotes,
        fetchedAtMs = fetchedAtMs
    )

    private fun jsonToMap(json: String): Map<String, Any?> = runCatching {
        val root = JSONObject(json)
        root.keys().asSequence().associateWith { key ->
            val inner = root.getJSONObject(key)
            inner.keys().asSequence().associateWith { inner.getString(it) }
        }
    }.getOrDefault(emptyMap())

    companion object {
        const val KIND_SET_TIMING = "set_timing"
        const val KIND_VOTE = "vote"
        const val KIND_ADD_MOSQUE = "add_mosque"
        private const val DAY_MS = 24L * 60 * 60 * 1000
        private const val USER_MOSQUES_TIMEOUT_MS = 10_000L
        private const val SEARCH_RADIUS_M = 5_000
        private const val MAX_MOSQUES = 40
        private const val MAX_REMOTE_LIST_LOOKUPS = 30
        private const val LIST_RECORD_TTL_MS = 6L * 60 * 60 * 1000
        private const val DETAIL_TTL_MS = 10L * 60 * 1000
        private const val MIN_ACTION_GAP_MS = 20_000L
        private const val MAX_ACTIONS_PER_DAY = 20
        private const val MAX_MOSQUE_ADDS_PER_DAY = 2
    }
}
