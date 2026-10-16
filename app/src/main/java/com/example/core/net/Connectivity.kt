package com.example.core.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

/**
 * The app's one view of whether the phone can reach the internet. Screens show an offline notice
 * from it, and downloads or live data wait on [awaitOnline] instead of failing straight away.
 *
 * "Online" means a network that Android has validated for internet access, so a Wi-Fi hotspot
 * with no uplink counts as offline. Before [init] runs (unit tests, previews) the app is online.
 */
object Connectivity {
    private val online = MutableStateFlow(true)

    /** True while a validated internet connection exists. Safe to collect from Compose. */
    val isOnline: StateFlow<Boolean> get() = online

    private var manager: ConnectivityManager? = null

    @Synchronized
    fun init(context: Context) {
        if (manager != null) return
        val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return
        manager = cm
        online.value = currentlyOnline(cm)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { refresh() }
            override fun onLost(network: Network) { refresh() }
            override fun onUnavailable() { refresh() }
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) { refresh() }
        }
        runCatching {
            cm.registerNetworkCallback(
                NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                callback
            )
        }
    }

    /** Re-reads the system state; cheap, and the callback path already funnels through here. */
    fun refresh() {
        val cm = manager ?: return
        online.value = currentlyOnline(cm)
    }

    /** Suspends until a validated connection exists. Returns immediately when already online. */
    suspend fun awaitOnline() {
        if (online.value) return
        online.first { it }
    }

    private fun currentlyOnline(cm: ConnectivityManager): Boolean = runCatching {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }.getOrDefault(false)
}
