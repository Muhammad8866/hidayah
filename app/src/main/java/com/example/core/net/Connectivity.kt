package com.example.core.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
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
        trackDefaultNetwork(cm) { online.value = it }
    }

    /**
     * Registers a default-network callback that reports online/offline through [publish]. Kept apart
     * from [init] so tests can drive it with their own ConnectivityManager.
     */
    internal fun trackDefaultNetwork(
        cm: ConnectivityManager,
        publish: (Boolean) -> Unit
    ): ConnectivityManager.NetworkCallback {
        val state = DefaultNetworkState<Network>()
        val lock = Any()
        fun update(change: DefaultNetworkState<Network>.() -> Unit) = synchronized(lock) {
            state.change()
            publish(state.isOnline)
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            // Each event carries its own network and capabilities; re-reading activeNetwork here races
            // the callback and is null while the app is blocked, which left "offline" on screen.
            // A new or lost network can change which server address answers, so remembered choices go.
            override fun onAvailable(network: Network) {
                ReachableFirstDns.forgetAll()
                val seed = runCatching { cm.getNetworkCapabilities(network) }.getOrNull()
                val seedValidated = seed != null &&
                    seed.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    seed.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                update { onAvailable(network, seedValidated) }
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                update {
                    onCapabilitiesChanged(
                        network,
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    )
                }
            }

            override fun onLost(network: Network) {
                ReachableFirstDns.forgetAll()
                update { onLost(network) }
            }
        }
        runCatching { cm.registerDefaultNetworkCallback(callback) }
        return callback
    }

    /** True on mobile data or another metered network; unknown counts as metered, so data is spared. */
    fun isMetered(): Boolean = runCatching { manager?.isActiveNetworkMetered ?: true }.getOrDefault(true)

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
