package com.example.core.platform

import android.location.Location

/** Live device location, backed by Play services or by the platform LocationManager per flavor. */
interface LiveLocationSource {
    /** Keeps 30-second updates running and asks for a fresh fix plus the cached one. */
    fun start(precise: Boolean, onLocation: (Location) -> Unit)
    fun stop()
}
