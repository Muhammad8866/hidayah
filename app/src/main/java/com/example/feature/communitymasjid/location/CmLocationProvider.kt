package com.example.feature.communitymasjid.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.feature.communitymasjid.model.CmUserLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * One-shot high-accuracy fixes for the Community Masjid feature. Independent of the app-wide
 * live-location stream so presence checks always use a fresh, accurate fix.
 */
class CmLocationProvider(context: Context) {
    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun hasFinePermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Cached device fix; good enough for listing nearby masjids, never used for presence checks. */
    @SuppressLint("MissingPermission")
    suspend fun lastKnownLocation(): CmUserLocation? {
        if (!hasPermission()) return null
        return withTimeoutOrNull(2_000) {
            suspendCancellableCoroutine { cont ->
                try {
                    client.lastLocation
                        .addOnSuccessListener { location ->
                            if (cont.isActive) cont.resume(location?.let {
                                CmUserLocation(it.latitude, it.longitude, it.accuracy, it.time)
                            })
                        }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                } catch (_: SecurityException) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }

    /**
     * Fast fix for listing masjids: a recent cached fix returns instantly, otherwise a
     * balanced-power fix (Wi-Fi/cell, usually < 2 s) is requested instead of waiting for GPS.
     */
    suspend fun quickLocation(maxAgeMs: Long = 3 * 60 * 60_000L): CmUserLocation? {
        // The list is refined by a precise fix afterwards, so an older cached fix is fine to start with.
        val last = lastKnownLocation()
        last?.takeIf { System.currentTimeMillis() - it.fixedAtMs <= maxAgeMs }?.let { return it }
        return currentLocation(timeoutMs = 5_000, highAccuracy = false) ?: last
    }

    /** [highAccuracy] uses GPS (needed for presence checks); false is much faster for listing. */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(timeoutMs: Long = 20_000, highAccuracy: Boolean = true): CmUserLocation? {
        if (!hasPermission()) return null
        val priority = if (highAccuracy && hasFinePermission()) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                val cancel = CancellationTokenSource()
                cont.invokeOnCancellation { cancel.cancel() }
                try {
                    client.getCurrentLocation(priority, cancel.token)
                        .addOnSuccessListener { location ->
                            if (cont.isActive) cont.resume(location?.let {
                                CmUserLocation(it.latitude, it.longitude, it.accuracy, System.currentTimeMillis())
                            })
                        }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                } catch (_: SecurityException) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }
}
