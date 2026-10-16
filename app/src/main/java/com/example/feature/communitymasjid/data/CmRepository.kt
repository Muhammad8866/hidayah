package com.example.feature.communitymasjid.data

import android.content.Context
import com.example.feature.communitymasjid.data.local.CmCommunityRecordEntity
import com.example.feature.communitymasjid.data.local.CmAreaCacheEntity
import com.example.feature.communitymasjid.data.local.CmDatabase
import com.example.feature.communitymasjid.data.local.CmLocalActionEntity
import com.example.feature.communitymasjid.data.local.CmCachedMosqueEntity
import com.example.feature.communitymasjid.data.local.CmNearbyQueryEntity
import com.example.feature.communitymasjid.data.remote.CmFirebaseDataSource
import com.example.feature.communitymasjid.data.remote.CmFirebaseDataSource.Companion.parseTimings
import com.example.feature.communitymasjid.data.remote.CmFirebaseDataSource.Companion.toMap
import com.example.feature.communitymasjid.data.remote.CmWriteOutcome
import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.domain.CmAreaCachePolicy
import com.example.feature.communitymasjid.model.CmCommunityRecord
import com.example.feature.communitymasjid.model.CmDownReason
import com.example.feature.communitymasjid.model.CmHistoryEntry
import com.example.feature.communitymasjid.model.CmMosque
import com.example.feature.communitymasjid.model.CmMyVotes
import com.example.feature.communitymasjid.model.CmPresence
import com.example.feature.communitymasjid.model.CmTimings
import com.example.feature.communitymasjid.model.CmUserLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.json.JSONArray
import java.util.UUID

/**
 * Single source of truth for the Community Masjid feature. Community discovery is cached by
 * search area for three days; Google Places content is never written to disk.
 */
class CmRepository(context: Context) {
    private val dao = CmDatabase.get(context).dao()
    private val remote = CmFirebaseDataSource(context.applicationContext)

    private val googleSearch = CmGoogleSearchGate(context.applicationContext, remote)

    val isCommunityConfigured: Boolean get() = remote.isConfigured

    data class NearbyResult(val mosques: List<CmMosque>, val fromCache: Boolean, val error: Throwable?, val notice: String? = null)

    /** The saved community list for this area, for showing something instantly. */
    suspend fun savedMosques(location: CmUserLocation?): List<CmMosque> {
        val now = System.currentTimeMillis()
        dao.pruneAreaCaches(now - CmAreaCachePolicy.TTL_MS)
        val from = location ?: dao.areaCaches().firstOrNull()?.let {
            CmUserLocation(it.latitude, it.longitude, 0f, it.fetchedAtMs)
        } ?: dao.nearbyQuery()?.let {
            CmUserLocation(it.latitude, it.longitude, 0f, it.fetchedAtMs)
        } ?: return emptyList()
        return areaCache(from, now)?.mosques ?: emptyList()
    }

    /** Google results stay in memory; only community entries are stored in Room. */
    suspend fun nearbyMosques(location: CmUserLocation, force: Boolean): NearbyResult = coroutineScope {
        val now = System.currentTimeMillis()
        val cachedEntry = areaCache(location, now)
        val cached = cachedEntry?.mosques.orEmpty()
        val googleJob = async {
            try {
                Result.success(googleSearch.search(location))
            } catch (error: kotlinx.coroutines.TimeoutCancellationException) { Result.failure<CmGoogleSearchGate.Result>(error) }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) { Result.failure<CmGoogleSearchGate.Result>(error) }
        }
        val communityJob = async {
            if (!remote.isConfigured || (!force && cachedEntry != null)) Result.success(cached) else try {
                Result.success(withTimeout(USER_MOSQUES_TIMEOUT_MS) {
                    remote.fetchUserMosques(location.latitude, location.longitude, SEARCH_RADIUS_M.toDouble())
                })
            } catch (error: kotlinx.coroutines.TimeoutCancellationException) { Result.failure<List<CmMosque>>(error) }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) { Result.failure<List<CmMosque>>(error) }
        }
        val googleResult = googleJob.await()
        val communityResult = communityJob.await()
        if (communityResult.isSuccess && remote.isConfigured && (force || cachedEntry == null)) {
            val fetched = communityResult.getOrThrow()
            dao.upsertAreaCache(CmAreaCacheEntity(
                areaId = cachedEntry?.areaId ?: UUID.randomUUID().toString(),
                latitude = location.latitude,
                longitude = location.longitude,
                fetchedAtMs = now,
                mosquesJson = encodeCommunityMosques(fetched)
            ))
            dao.clearCachedMosques()
            dao.insertCachedMosques(fetched.map { it.toEntity(now) })
            dao.upsertNearbyQuery(CmNearbyQueryEntity(latitude = location.latitude, longitude = location.longitude, fetchedAtMs = now))
        }
        val community = communityResult.getOrElse { cached }
        val listError = googleResult.exceptionOrNull() ?: communityResult.exceptionOrNull()
        val discovered = googleResult.getOrNull()?.mosques.orEmpty()
        val mosques = (discovered + community)
            .distinctBy { it.docId }.sortedBy { it.distanceMetres }.take(MAX_MOSQUES)
        NearbyResult(mosques, cachedEntry != null || communityResult.isFailure, listError, googleResult.getOrNull()?.notice)
    }

    private data class AreaHit(val areaId: String, val mosques: List<CmMosque>)

    private suspend fun areaCache(location: CmUserLocation, now: Long): AreaHit? {
        dao.pruneAreaCaches(now - CmAreaCachePolicy.TTL_MS)
        val row = dao.areaCaches().firstOrNull { candidate ->
            CmAreaCachePolicy.isReusable(location, candidate.latitude, candidate.longitude, candidate.fetchedAtMs, now)
        }
        if (row != null) {
            val decoded = runCatching { decodeCommunityMosques(row.mosquesJson, location) }.getOrNull()
            if (decoded != null) return AreaHit(row.areaId, decoded)
            dao.deleteAreaCache(row.areaId)
        }
        // Keep the last-query snapshot from older app versions usable during its remaining TTL.
        val old = dao.nearbyQuery() ?: return null
        if (!CmAreaCachePolicy.isReusable(location, old.latitude, old.longitude, old.fetchedAtMs, now)) return null
        return AreaHit("legacy", legacyCachedMosques(location))
    }

    private suspend fun legacyCachedMosques(location: CmUserLocation): List<CmMosque> =
        dao.cachedMosques().filter { it.isUserAdded }.map {
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
        }.filter { (it.distanceMetres ?: Double.MAX_VALUE) <= SEARCH_RADIUS_M }
            .sortedBy { it.distanceMetres }

    private fun encodeCommunityMosques(mosques: List<CmMosque>): String = JSONArray().apply {
        mosques.filter { it.isUserAdded }.forEach { mosque ->
            put(JSONObject().put("docId", mosque.docId).put("name", mosque.name)
                .put("address", mosque.address).put("latitude", mosque.latitude)
                .put("longitude", mosque.longitude)
                .put("addedByUid", mosque.addedByUid ?: JSONObject.NULL))
        }
    }.toString()

    private fun decodeCommunityMosques(json: String, location: CmUserLocation): List<CmMosque> {
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val latitude = item.getDouble("latitude")
            val longitude = item.getDouble("longitude")
            CmMosque(
                docId = item.getString("docId"), name = item.getString("name"),
                address = item.getString("address"), latitude = latitude, longitude = longitude,
                distanceMetres = CmTimingRules.distanceMetres(location.latitude, location.longitude, latitude, longitude),
                isUserAdded = true,
                addedByUid = item.optString("addedByUid").takeUnless { it.isBlank() || it == "null" }
            )
        }.filter { (it.distanceMetres ?: Double.MAX_VALUE) <= SEARCH_RADIUS_M }.sortedBy { it.distanceMetres }
    }

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

    suspend fun report(docId: String, target: String, reason: String) = remote.report(docId, target, reason)

    val hasAccount: Boolean get() = remote.hasAccount

    suspend fun deleteAccount() = remote.deleteAnonymousAccount()

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
        dao.insertCachedMosques(listOf(mosque.toEntity(now)))
        updateAreaCaches { row, mosques ->
            if (CmTimingRules.distanceMetres(row.latitude, row.longitude, mosque.latitude, mosque.longitude) <= SEARCH_RADIUS_M)
                (mosques.filterNot { it.docId == mosque.docId } + mosque) else mosques
        }
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
                if (outcome.votedDown) {
                    dao.deleteCachedMosque(mosque.docId)
                    updateAreaCaches { _, mosques -> mosques.filterNot { it.docId == mosque.docId } }
                }
            }
        }

    private suspend fun logAction(kind: String, docId: String) {
        dao.insertAction(CmLocalActionEntity(kind = kind, mosqueDocId = docId, suggestionId = "", atMs = System.currentTimeMillis()))
    }

    private suspend fun updateAreaCaches(transform: (CmAreaCacheEntity, List<CmMosque>) -> List<CmMosque>) {
        for (row in dao.areaCaches()) {
            val mosques = runCatching {
                decodeCommunityMosques(row.mosquesJson, CmUserLocation(row.latitude, row.longitude, 0f, row.fetchedAtMs))
            }.getOrNull() ?: continue
            dao.upsertAreaCache(row.copy(mosquesJson = encodeCommunityMosques(transform(row, mosques))))
        }
    }

    private fun CmMosque.toEntity(now: Long) = CmCachedMosqueEntity(
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
