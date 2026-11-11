package com.example.core.net

/**
 * What the default-network callback has reported, kept as plain values so it is testable without
 * Android. Every event brings its own network; nothing is re-read from ConnectivityManager inside a
 * callback, because those reads race the callback and return null while the app is blocked.
 */
internal class DefaultNetworkState<N : Any> {
    private var network: N? = null
    private var validated = false

    /** A default network exists and Android validated its internet access. */
    val isOnline: Boolean get() = network != null && validated

    /** A new default network. [validatedSeed] is a first guess; the capabilities event that follows replaces it. */
    fun onAvailable(network: N, validatedSeed: Boolean) {
        this.network = network
        validated = validatedSeed
    }

    fun onCapabilitiesChanged(network: N, hasInternet: Boolean, isValidated: Boolean) {
        this.network = network
        validated = hasInternet && isValidated
    }

    /** Losing an earlier network after a switch does not touch the current default. */
    fun onLost(network: N) {
        if (network != this.network) return
        this.network = null
        validated = false
    }
}
