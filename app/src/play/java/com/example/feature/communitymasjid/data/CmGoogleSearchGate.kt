package com.example.feature.communitymasjid.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.example.feature.communitymasjid.data.remote.CmFirebaseDataSource
import com.example.feature.communitymasjid.data.remote.CmGooglePlacesDataSource
import com.example.feature.communitymasjid.domain.CmSearchBudget
import com.example.feature.communitymasjid.domain.CmSearchPolicy
import com.example.feature.communitymasjid.model.CmMosque
import com.example.feature.communitymasjid.model.CmUserLocation
import com.example.feature.communitymasjid.domain.CmTimingRules
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Persistent request accounting plus an on-device copy of the last nearby results, so the same
 * area does not spend another Google search after the app restarts.
 */
class CmGoogleSearchGate(context: Context, private val remote: CmFirebaseDataSource) {
    private val google = CmGooglePlacesDataSource(context.applicationContext)
    private val prefs = context.getSharedPreferences("cm_search_budget", Context.MODE_PRIVATE)
    private val cache = context.getSharedPreferences("cm_nearby_cache", Context.MODE_PRIVATE)

    data class Result(val mosques: List<CmMosque>, val notice: String? = null)

    val isConfigured: Boolean get() = google.isConfigured

    /**
     * The last results when they are recent and from close by, re-sorted for [location]. Reopening the
     * screen uses these instead of spending one of the day's searches.
     */
    suspend fun recent(location: CmUserLocation, maxAgeMs: Long = REUSE_MS): List<CmMosque>? {
        val uid = remote.ensureUid()
        return mutex.withLock {
            loadSaved()
            val age = System.currentTimeMillis() - snapshotAtMs
            val fresh = snapshotUid == uid && snapshot.isNotEmpty() && age in 0 until maxAgeMs &&
                CmTimingRules.distanceMetres(snapshotLat, snapshotLon, location.latitude, location.longitude) < REUSE_METRES
            if (fresh) near(location).ifEmpty { null } else null
        }
    }

    suspend fun search(location: CmUserLocation): Result {
        check(google.isConfigured) { "Nearby Google masjids are not configured in this build." }
        val uid = remote.ensureUid()
        return mutex.withLock {
            loadSaved()
            val now = System.currentTimeMillis()
            val local = readBudget(uid)
            // Google results live only in memory; without them the once-per-area rule would leave the tab empty.
            val hasResults = snapshotUid == uid && snapshot.isNotEmpty()
            val block = CmSearchPolicy.blocked(now, location, local, hasResults)
            if (block != null && local != null) return@withLock cached(uid, location, CmSearchPolicy.message(block, now, local))

            // The transaction/rules enforce the daily cap and cooldown across restarts.
            val reservation = try {
                withTimeout(12_000) { remote.reserveNearbySearch(location, now, hasResults) }
            } catch (error: TimeoutCancellationException) {
                return@withLock cached(uid, location, "Could not check search availability. Please try again later.")
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                Log.w(TAG, "Search quota check failed", error)
                // The rules refuse a search inside the server-side cooldown (device clock may differ).
                val denied = (error as? FirebaseFirestoreException)?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                return@withLock cached(uid, location, if (denied) "Please wait a few minutes before another nearby search."
                    else "Could not check search availability. Please try again later.")
            }
            writeBudget(uid, reservation.budget)
            if (reservation.block != null) {
                return@withLock cached(uid, location, CmSearchPolicy.message(reservation.block, now, reservation.budget))
            }
            // Reserve before calling Google. Failed/timed-out attempts also consume a slot.
            val found = try {
                withTimeout(15_000) { google.nearby(location.latitude, location.longitude, 5_000) }
            } catch (error: TimeoutCancellationException) {
                return@withLock cached(uid, location, "Nearby search timed out. Showing available results.")
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                Log.w(TAG, "Places nearby search failed", error)
                return@withLock cached(uid, location, "Nearby search could not be refreshed. Showing available results.")
            }
            snapshotUid = uid
            snapshot = found
            snapshotAtMs = System.currentTimeMillis()
            snapshotLat = location.latitude
            snapshotLon = location.longitude
            saveSnapshot()
            Result(found)
        }
    }

    private fun cached(uid: String, location: CmUserLocation, message: String): Result {
        val items = if (snapshotUid == uid) near(location) else emptyList()
        return Result(items, "$message You can also use Google Maps below.")
    }

    private fun near(location: CmUserLocation) = snapshot.map { it.copy(distanceMetres = CmTimingRules.distanceMetres(
        location.latitude, location.longitude, it.latitude, it.longitude
    )) }.filter { (it.distanceMetres ?: Double.MAX_VALUE) <= 5_000 }.sortedBy { it.distanceMetres }

    /** Restores the saved results once per process; a corrupt copy is dropped, never fatal. */
    private fun loadSaved() {
        if (savedLoaded) return
        savedLoaded = true
        val raw = cache.getString(KEY_SNAPSHOT, null) ?: return
        runCatching {
            val root = JSONObject(raw)
            val atMs = root.getLong("atMs")
            if (System.currentTimeMillis() - atMs !in 0 until MAX_SAVED_MS) error("expired")
            val items = root.getJSONArray("items")
            snapshot = (0 until minOf(items.length(), MAX_SAVED_ITEMS)).map { i ->
                val o = items.getJSONObject(i)
                CmMosque(o.getString("docId"), o.getString("name").take(120), o.optString("address").take(300),
                    o.getDouble("lat"), o.getDouble("lon"))
            }.filter { it.docId.startsWith("google_") && it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
            snapshotUid = root.getString("uid")
            snapshotAtMs = atMs
            snapshotLat = root.getDouble("lat")
            snapshotLon = root.getDouble("lon")
        }.onFailure { cache.edit().remove(KEY_SNAPSHOT).apply() }
    }

    private fun saveSnapshot() {
        val items = JSONArray()
        snapshot.take(MAX_SAVED_ITEMS).forEach {
            items.put(JSONObject().put("docId", it.docId).put("name", it.name).put("address", it.address)
                .put("lat", it.latitude).put("lon", it.longitude))
        }
        val root = JSONObject().put("uid", snapshotUid).put("atMs", snapshotAtMs)
            .put("lat", snapshotLat).put("lon", snapshotLon).put("items", items)
        cache.edit().putString(KEY_SNAPSHOT, root.toString()).apply()
    }

    private fun readBudget(uid: String): CmSearchBudget? {
        if (!prefs.contains("$uid.day")) return null
        return CmSearchBudget(prefs.getLong("$uid.day", 0), prefs.getInt("$uid.count", 0),
            prefs.getLong("$uid.last", 0), Double.fromBits(prefs.getLong("$uid.lat", 0)),
            Double.fromBits(prefs.getLong("$uid.lon", 0)))
    }

    private fun writeBudget(uid: String, budget: CmSearchBudget) {
        check(prefs.edit().putLong("$uid.day", budget.day).putInt("$uid.count", budget.count)
            .putLong("$uid.last", budget.lastAtMs).putLong("$uid.lat", budget.latitude.toBits())
            .putLong("$uid.lon", budget.longitude.toBits()).commit()) { "Could not save search usage. Please try again." }
    }

    companion object {
        private val mutex = Mutex()
        private var snapshotUid: String? = null
        private var snapshot: List<CmMosque> = emptyList()
        private var snapshotAtMs = 0L
        private var snapshotLat = 0.0
        private var snapshotLon = 0.0
        private var savedLoaded = false
        /** Masjids rarely change; reuse the same area's list for a week before searching again. */
        const val REUSE_MS = 7 * 24 * 60 * 60_000L
        /** Pull-to-refresh only searches again once the saved list is older than a day. */
        const val FORCE_REFRESH_AFTER_MS = 24 * 60 * 60_000L
        const val MAX_SAVED_MS = 30 * 24 * 60 * 60_000L
        private const val MAX_SAVED_ITEMS = 40
        private const val KEY_SNAPSHOT = "snapshot"
        /** Within 1 km of the last search the same list still covers the area well. */
        private const val REUSE_METRES = 1_000.0
        private const val TAG = "CmGoogleSearch"
    }
}
