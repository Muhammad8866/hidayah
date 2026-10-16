package com.example.core.platform

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

fun liveLocationSource(context: Context): LiveLocationSource = FusedLiveLocation(context)

@SuppressLint("MissingPermission")
private class FusedLiveLocation(context: Context) : LiveLocationSource {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    private var callback: LocationCallback? = null
    private var currentRequest: CancellationTokenSource? = null

    override fun start(precise: Boolean, onLocation: (Location) -> Unit) {
        val priority = if (precise) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        if (callback == null) {
            callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let(onLocation)
                }
            }
            val request = LocationRequest.Builder(priority, 30_000)
                .setMinUpdateIntervalMillis(10_000).setWaitForAccurateLocation(false).build()
            // Permission can be revoked between the caller's check and this call; the dashboard then keeps its city.
            runCatching { client.requestLocationUpdates(request, callback!!, Looper.getMainLooper()) }
                .onFailure { callback = null }
        }
        // A fresh fix whenever the app returns to the foreground, so switching location on updates without a tap.
        currentRequest?.cancel()
        val token = CancellationTokenSource().also { currentRequest = it }
        runCatching { client.getCurrentLocation(priority, token.token).addOnSuccessListener { it?.let(onLocation) } }
        // Fall back to Android's cached fix while a fresh one is being acquired.
        runCatching { client.lastLocation.addOnSuccessListener { it?.let(onLocation) } }
    }

    override fun stop() {
        currentRequest?.cancel()
        currentRequest = null
        callback?.let { client.removeLocationUpdates(it) }
        callback = null
    }
}

/**
 * Updates are mandatory: when Play has a newer version, its full-screen update flow blocks the app
 * until it is installed. Offline, or installed from outside Play, the check fails and the app opens normally.
 */
class AppUpdateEnforcer(private val activity: ComponentActivity) {
    private val manager by lazy { AppUpdateManagerFactory.create(activity.applicationContext) }
    private var showing = false
    // Whatever the result, onResume runs next and asks Play again, so a cancelled update comes straight back.
    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { showing = false }

    fun check() {
        if (showing) return
        manager.appUpdateInfo.addOnSuccessListener { info ->
            val availability = info.updateAvailability()
            val pending = availability == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            // An update that was started and then left (app killed, Home pressed) must be resumed.
            val interrupted = availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            if (!showing && !activity.isFinishing && (pending || interrupted)) {
                showing = runCatching {
                    manager.startUpdateFlowForResult(info, launcher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
                }.getOrDefault(false)
            }
        }
    }
}
