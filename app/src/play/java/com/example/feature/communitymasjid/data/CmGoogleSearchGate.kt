package com.example.feature.communitymasjid.data

import android.content.Context
import com.example.feature.communitymasjid.data.remote.CmFirebaseDataSource
import com.example.feature.communitymasjid.data.remote.CmGooglePlacesDataSource
import com.example.feature.communitymasjid.domain.CmSearchBudget
import com.example.feature.communitymasjid.domain.CmSearchPolicy
import com.example.feature.communitymasjid.model.CmMosque
import com.example.feature.communitymasjid.model.CmUserLocation
import com.example.feature.communitymasjid.domain.CmTimingRules
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

/** Persistent request accounting; Google content is only retained in process memory. */
class CmGoogleSearchGate(context: Context, private val remote: CmFirebaseDataSource) {
    private val google = CmGooglePlacesDataSource(context.applicationContext)
    private val prefs = context.getSharedPreferences("cm_search_budget", Context.MODE_PRIVATE)

    data class Result(val mosques: List<CmMosque>, val notice: String? = null)

    suspend fun search(location: CmUserLocation): Result {
        check(google.isConfigured) { "Nearby Google masjids are not configured in this build." }
        val uid = remote.ensureUid()
        return mutex.withLock {
            val now = System.currentTimeMillis()
            val local = readBudget(uid)
            val block = CmSearchPolicy.blocked(now, location, local)
            if (block != null && local != null) return@withLock cached(uid, location, CmSearchPolicy.message(block, now, local))

            // The transaction/rules enforce the daily cap and cooldown across restarts.
            val reservation = try {
                withTimeout(12_000) { remote.reserveNearbySearch(location, now) }
            } catch (error: TimeoutCancellationException) {
                return@withLock cached(uid, location, "Could not check search availability. Please try again later.")
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                return@withLock cached(uid, location, "Could not check search availability. Please try again later.")
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
                return@withLock cached(uid, location, "Nearby search could not be refreshed. Showing available results.")
            }
            snapshotUid = uid
            snapshot = found
            Result(found)
        }
    }

    private fun cached(uid: String, location: CmUserLocation, message: String): Result {
        val items = if (snapshotUid == uid) snapshot.map { it.copy(distanceMetres = CmTimingRules.distanceMetres(
            location.latitude, location.longitude, it.latitude, it.longitude
        )) }.filter { (it.distanceMetres ?: Double.MAX_VALUE) <= 5_000 }.sortedBy { it.distanceMetres } else emptyList()
        return Result(items, "$message You can also use Google Maps below.")
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
    }
}
