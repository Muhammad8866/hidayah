package com.example.core.platform

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal

fun liveLocationSource(context: Context): LiveLocationSource = PlatformLiveLocation(context)

/** Uses Android's own LocationManager, so the F-Droid build needs no Google Play services. */
@SuppressLint("MissingPermission")
private class PlatformLiveLocation(context: Context) : LiveLocationSource {
    private val appContext = context.applicationContext
    private val manager = context.getSystemService(LocationManager::class.java)
    private var listener: LocationListener? = null
    private var currentRequest: CancellationSignal? = null

    override fun start(precise: Boolean, onLocation: (Location) -> Unit) {
        val manager = manager ?: return
        val providers = buildList {
            if (precise) add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }.filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        if (listener == null) {
            val next = LocationListener { onLocation(it) }
            listener = next
            providers.forEach { provider ->
                runCatching { manager.requestLocationUpdates(provider, 30_000L, 0f, next, Looper.getMainLooper()) }
            }
        }
        currentRequest?.cancel()
        val signal = CancellationSignal().also { currentRequest = it }
        providers.firstOrNull()?.let { provider ->
            runCatching {
                LocationManagerCompat.getCurrentLocation(manager, provider, signal,
                    ContextCompat.getMainExecutor(appContext)) { it?.let(onLocation) }
            }
        }
        providers.mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.elapsedRealtimeNanos }?.let(onLocation)
    }

    override fun stop() {
        currentRequest?.cancel()
        currentRequest = null
        listener?.let { runCatching { manager?.removeUpdates(it) } }
        listener = null
    }
}

/** F-Droid delivers updates itself; there is no in-app update flow. */
@Suppress("UNUSED_PARAMETER")
class AppUpdateEnforcer(activity: ComponentActivity) {
    fun check() = Unit
}
